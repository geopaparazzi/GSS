import json
import os
import sqlite3
from base64 import b64decode, b64encode
from datetime import datetime, timezone
from email.mime import image

import requests
from data.models import (DbNamings, GpsLog, GpsLogData, Image, ImageData, Note,
                         Project, ProjectData, TmsSource, Utilities, WmsSource)
from django.conf import settings
from django.contrib.auth.models import Group, Permission, User
from django.contrib.gis.geos import LineString, Point
from django.core.management import call_command
from django.core.management.base import BaseCommand, CommandError
from django.db import connection
from requests.auth import HTTPBasicAuth


class Command(BaseCommand):
    help = 'Populate the database with example data as a surveyor.'

    def add_arguments(self, parser):
        parser.add_argument('-f', '--folder', type=str, help='The optional path to a folder containing smash/geopaparazzi projects to import.')
        parser.add_argument('-d', '--delete', action='store_true', help='Flag that enables clearing of the tables first.')

    def handle(self, *args, **options):
        if settings.DEBUG == False:
            self.stderr.write("This command is available only when in debug mode. Exiting.")
            return

        doDelete = options['delete']
        if doDelete:
            self.stdout.write("Clearing data from tables.")
            GpsLogData.objects.all().delete()
            GpsLog.objects.all().delete()
            ImageData.objects.all().delete()
            Image.objects.all().delete()
            Note.objects.all().delete()
            ProjectData.objects.all().delete()
            WmsSource.objects.all().delete()
            TmsSource.objects.all().delete()

        inputFolder = options['folder']
        if inputFolder:
            self.stdout.write("Importing existing SMASH projects found in provided folder, using the API.")

            # create surveyors group
            surveyorsGroup = Group.objects.filter(name=DbNamings.GROUP_SURVEYORS).first()
            if not surveyorsGroup:
                self.stderr.write(f"Surveyor group does not exist.")

            surveyorUser = User.objects.filter(username="surveyor").first()
            if not surveyorUser:
                self.stderr.write(f"Surveyor user does not exist.")

            filesNamesList = os.listdir(inputFolder)
            for fileName in filesNamesList:
                if fileName.endswith(".gpap") and not fileName.endswith('example_project.gpap'):
                    self.stdout.write(f"Importing: {fileName}")
                    try:
                        # get/create the project
                        pName = os.path.splitext(fileName)[0]

                        # remove underscores and capitalize
                        pName = pName.replace("_", " ")
                        pName = pName.capitalize()


                        newProject = Project.objects.filter(name=pName).first()
                        if not newProject:
                            newProject = Project(name=pName, description=pName)
                            newProject.save()

                            newProject.groups.add(surveyorsGroup)
                            newProject.save()


                        filePath = os.path.join(inputFolder, fileName)
                        conn = sqlite3.connect(filePath)
                        cursor = conn.cursor()
                        
                        tokenAuthHeader = {}
                        base = "http://localhost:8000/api"

                        loginUrl = f"{base}/login/"
                        r = requests.post(url = loginUrl, data = {
                            "username": "surveyor",
                            "password": "surveyor",
                            "project": pName,
                        })
                        if r.status_code != 200:
                            self.stderr.write(r.text)
                            return
                        else:
                            token = json.loads(r.text)['token']
                            tokenAuthHeader['Authorization'] = f"Token {token}"
                            
                        if len(tokenAuthHeader) > 0:
                            self.insertNotes(cursor, tokenAuthHeader, base, surveyorUser, newProject)
                            self.insertSimpleImages(cursor, tokenAuthHeader, base, surveyorUser, newProject)
                            self.insertGpslogs(cursor, tokenAuthHeader, base, surveyorUser, newProject)
                    except sqlite3.Error as error:
                        self.stderr.write('Error occured - ', error)
                    finally:
                        if conn:
                            conn.close()

        else:
            self.generateExampleData()

    def insertGpslogs(self, cursor, tokenAuthHeader, base, surveyorUser, newProject):
        gpslogsUrl = f"{base}/gpslogs/"
        cursor.execute("""
                        SELECT g._id,g.startts,g.endts,g.text,glp.color,glp.width 
                        FROM gpslogs g left join gpslogsproperties glp on g._id=glp.logid
                    """)
        result = cursor.fetchall()
        for row in result:
            id = row[0]
            startts = row[1]
            endts = row[2]
            text = row[3]
            color = row[4]
            width = row[5]

            dt = datetime.fromtimestamp(startts/1000, timezone.utc)
            starttsStr = dt.strftime("%Y-%m-%d %H:%M:%S")
            dt = datetime.fromtimestamp(endts/1000, timezone.utc)
            endtsStr = dt.strftime("%Y-%m-%d %H:%M:%S")


            cursor.execute("""
                            SELECT g._id,g.lon,g.lat,g.altim,g.ts
                            FROM gpslogsdata g where g.logid=1
                        """)
            subResult = cursor.fetchall()

            gpslogdata = []
            coords = []
            for row in subResult:
                subid = row[0]
                lon = row[1]
                lat = row[2]
                altim = row[3]
                ts2 = row[4]
                dt2 = datetime.fromtimestamp(ts2/1000, timezone.utc)
                ts2Str = dt2.strftime("%Y-%m-%d %H:%M:%S")
                gpslogdata.append({
                            DbNamings.GEOM: f"SRID=4326;POINT ({lon} {lat} {altim})", 
                            DbNamings.GPSLOGDATA_TIMESTAMP: ts2Str,
                        })
                coords.append((lon, lat))
                    

            line = LineString(coords, srid=4326)
            newGpslog = {
                DbNamings.GPSLOG_NAME: text,
                DbNamings.GPSLOG_STARTTS: starttsStr,
                DbNamings.GPSLOG_ENDTS: endtsStr,
                DbNamings.GEOM: line.ewkt,
                DbNamings.GPSLOG_WIDTH: width,
                DbNamings.GPSLOG_COLOR: color,
                DbNamings.USER: surveyorUser.id, 
                DbNamings.PROJECT: newProject.id
            }
            newGpslog["gpslogdata"] = gpslogdata

            # headers = {
            #             "Content-Type":"application/json",
            #             "Accept":"application/json",
            #         }
            self.stdout.write(f"Uploading Gpslog '{text}' with id: {id}")
            r = requests.post(url = gpslogsUrl,headers=tokenAuthHeader, json=json.dumps(newGpslog))
            if r.status_code != 201:
                self.stderr.write("Gpslog could not be uploaded:")
                self.stderr.write(r.json())

    def insertSimpleImages(self, cursor, tokenAuthHeader, base, surveyorUser, newProject):
        imagesUrl = f"{base}/images/"
        cursor.execute("""
                    SELECT i._id, i.lon,i.lat,i.altim,i.azim,i.ts,i.text,id.data 
                    FROM images i left join imagedata id on i.imagedata_id=id._id 
                    where i.note_id is null
                    """)
        result = cursor.fetchall()

        for row in result:
            id = row[0]
            lon = row[1]
            lat = row[2]
            altim = row[3]
            azim = row[4]
            ts = row[5]
            text = row[6]
            data = row[7]

            dt = datetime.fromtimestamp(ts/1000, timezone.utc)
            tsStr = dt.strftime("%Y-%m-%d %H:%M:%S")

            newImage = {
                DbNamings.GEOM: f'SRID=4326;POINT ({lon} {lat})', 
                DbNamings.IMAGE_ALTIM: altim, 
                DbNamings.IMAGE_TIMESTAMP: tsStr, 
                DbNamings.IMAGE_AZIMUTH: azim,
                DbNamings.IMAGE_TEXT: text, 
                DbNamings.IMAGE_IMAGEDATA: {
                    DbNamings.IMAGEDATA_DATA: b64encode(data).decode('UTF-8')
                },
                DbNamings.USER: surveyorUser.id, 
                DbNamings.PROJECT: newProject.id
            }
            self.stdout.write(f"Uploading Image '{text}' with id: {id}")
            imageB64 = json.dumps(newImage)
            r = requests.post(url = imagesUrl, json=imageB64, headers=tokenAuthHeader)
            if r.status_code != 201:
                self.stderr.write("Image could not be uploaded:")
                self.stderr.write(r.json())
    

    def insertNotes(self, cursor, tokenAuthHeader, base, surveyorUser, newProject):
        notesUrl = f"{base}/notes/"
        cursor.execute("""
                    SELECT n._id,n.lon,n.lat,n.altim,n.ts,n.description,n.text,n.form,
                    ne.marker,ne.size,ne.rotation,ne.color,ne.accuracy,ne.heading,ne.speed,ne.speedaccuracy
                    FROM notes n left join notesext ne on n._id=ne.noteid
                    """)
        result = cursor.fetchall()
        
        firstFormDone = False
        for row in result:
            id = row[0]
            lon = row[1]
            lat = row[2]
            altim = row[3]
            ts = row[4]
            description = row[5]
            text = row[6]
            form = row[7]
            marker = row[8]
            size = row[9]
            rotation = row[10]
            color = row[11]
            accuracy = row[12]
            heading = row[13]
            speed = row[14]
            speedaccuracy = row[15]

            if not accuracy:
                accuracy = -1
            if not heading:
                heading = -1
            if not speed:
                speed = -1
            if not speedaccuracy:
                speedaccuracy = -1

            dt = datetime.fromtimestamp(ts/1000, timezone.utc)
            tsStr = dt.strftime("%Y-%m-%d %H:%M:%S")
            uploadtsStr = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            if not description:
                description = " - nv - "

            newNote = {
                DbNamings.GEOM: f'SRID=4326;POINT ({lon} {lat})', 
                DbNamings.NOTE_ID: id, 
                DbNamings.NOTE_ALTIM: altim, 
                DbNamings.NOTE_TS: tsStr, 
                DbNamings.NOTE_UPLOADTS: uploadtsStr, 
                DbNamings.NOTE_DESCRIPTION: description, 
                DbNamings.NOTE_TEXT: text, 
                DbNamings.NOTE_MARKER: marker, 
                DbNamings.NOTE_SIZE: size, 
                DbNamings.NOTE_ROTATION: rotation, 
                DbNamings.NOTE_COLOR: color, 
                DbNamings.NOTE_ACCURACY: accuracy, 
                DbNamings.NOTE_HEADING: heading, 
                DbNamings.NOTE_SPEED: speed, 
                DbNamings.NOTE_SPEEDACCURACY: speedaccuracy, 
                DbNamings.NOTE_FORM: form, 
                DbNamings.USER: surveyorUser.id, 
                DbNamings.PROJECT: newProject.id
            }
            if form != None and len(form.strip()) != 0:
                # find images connected to notes
                imageIds = []
                formDict = json.loads(form)
                Utilities.collectImageIds(formDict, imageIds)
                if imageIds:
                    wherePart = ",".join([str(i) for i in imageIds])
                    cursor.execute(f"""
                                SELECT i._id, i.lon,i.lat,i.altim,i.azim,i.ts,i.text,id.data 
                                FROM images i left join imagedata id on i.imagedata_id=id._id 
                                where i._id in ({wherePart})
                                """)
                    result = cursor.fetchall()

                    imagesMap = {}
                    for row in result:
                        imageId = row[0]
                        lon = row[1]
                        lat = row[2]
                        altim = row[3]
                        azim = row[4]
                        ts = row[5]
                        text = row[6]
                        data = row[7]

                        dt = datetime.fromtimestamp(ts/1000, timezone.utc)
                        tsStr = dt.strftime("%Y-%m-%d %H:%M:%S")

                        newImage = {
                            DbNamings.GEOM: f'SRID=4326;POINT ({lon} {lat})', 
                            DbNamings.IMAGE_ALTIM: altim, 
                            DbNamings.IMAGE_TIMESTAMP: tsStr, 
                            DbNamings.IMAGE_AZIMUTH: azim,
                            DbNamings.IMAGE_TEXT: text, 
                            DbNamings.IMAGE_IMAGEDATA: {
                                DbNamings.IMAGEDATA_DATA: b64encode(data).decode('UTF-8')
                            },
                            DbNamings.USER: surveyorUser.id, 
                            DbNamings.PROJECT: newProject.id
                        }
                        imagesMap[imageId] = newImage
                    
                    newNote[DbNamings.NOTE_IMAGES] = imagesMap
            
            self.stdout.write(f"Uploading Note '{text}' with id: {id}")
            noteJson = json.dumps(newNote)
            r = requests.post(url = notesUrl, json = noteJson, headers=tokenAuthHeader)
            if r.status_code != 201:
                self.stderr.write("Note could not be uploaded:")
                self.stderr.write(r.json())
            
            if not firstFormDone:
                # add a duplicated note
                firstFormDone = True
                now = datetime.now()
                newNote[DbNamings.NOTE_TS] = now.strftime("%Y-%m-%d %H:%M:%S")
                newNote[DbNamings.NOTE_UPLOADTS] = now.strftime("%Y-%m-%d %H:%M:%S")
                
                noteJson = json.dumps(newNote)
                r = requests.post(url = notesUrl, json = noteJson, headers=tokenAuthHeader)
                if r.status_code != 201:
                    self.stderr.write("Note could not be uploaded:")
                    self.stderr.write(r.json())

                



    def generateExampleData(self):
        self.stdout.write("Generating some serverside example data using models and the API.")
            # get default project
        defaultProject = Project.objects.filter(name=DbNamings.PROJECT_DEFAULT).first()
        if not defaultProject:
            self.stderr.write("The Default project is not available, check your data. Exiting.")

            # get surveyor user
        surveyorUser = User.objects.filter(username="surveyor").first()
        if not surveyorUser:
            self.stderr.write("The Surveyor user is not available, check your data. Exiting.")

            # insert some data using models
        if Note.objects.count() == 0:
            note = Note.objects.create(
                    the_geom=Point(11.0, 46.0),
                    altim = 322,
                    ts = "2022-09-23 10:00:00",
                    uploadts = "2022-09-23 16:50:00",
                    description = "A test note, inserted using models",
                    text = "Test Note - models",
                    marker = "circle",
                    size = 10,
                    rotation = 0,
                    color = "#FF0000",
                    accuracy = 0,
                    heading = 0,
                    speed = 0,
                    speedaccuracy = 0,
                    form = "",
                    user = surveyorUser,
                    project = defaultProject
                )
            note.save()

        if GpsLogData.objects.count() == 0:
            gpsLog = GpsLog.objects.create(
                    name = "Test GpsLog",
                    startts = "2022-09-23 10:10:00",
                    endts = "2022-09-23 10:10:09",
                    the_geom = LineString((11.1, 46.1), (11.2, 46.2), (11.4, 46.0), srid=4326),
                    width = 3,
                    color = "#FF0000",
                    user = surveyorUser,
                    project = defaultProject,
                )
            GpsLogData.objects.create(
                    the_geom=Point(11.1, 46.1, 325),
                    ts = "2022-09-23 10:10:00",
                    gpslogid=gpsLog
                )
            GpsLogData.objects.create(
                    the_geom=Point(11.2, 46.2, 356),
                    ts = "2022-09-23 10:10:05",
                    gpslogid=gpsLog
                )
            GpsLogData.objects.create(
                    the_geom=Point(11.4, 46.0, 382),
                    ts = "2022-09-23 10:10:09",
                    gpslogid=gpsLog
                )

        # insert some data using the rest api

        # get list of existing notes
        base = "http://localhost:8000/api"
        surveyorAuth = HTTPBasicAuth('surveyor', 'surveyor')

        notesUrl = f"{base}/notes/"
        r = requests.get(url = notesUrl,auth = surveyorAuth)
        notesList = r.json()
        if len(notesList) == 1:
            newNote = {'id': 1, 'previd': None, 
                            'the_geom': 'SRID=4326;POINT (11.11 46.11)', 
                            'altim': 360.0, 
                            'ts': '2022-09-23T10:00:00Z', 
                            'uploadts': '2022-09-25T16:50:00Z', 
                            'description': 'A test note, inserted via API', 
                            'text': 'Test Note - API', 
                            'marker': 'square', 
                            'size': 12.0, 
                            'rotation': 0.0, 
                            'color': '#00FF00', 
                            'accuracy': 0.0, 
                            'heading': 0.0, 
                            'speed': 0.0, 
                            'speedaccuracy': 0.0, 
                            'form': None, 
                            'user': 2, 
                            'project': 1
                        }
            r = requests.post(url = notesUrl, data = newNote, auth = surveyorAuth)
            if r.status_code != 200:
                print(r.json())

        gpslogsUrl = f"{base}/gpslogs/"
        r = requests.get(url = gpslogsUrl, auth = surveyorAuth)
        gpslogsList = r.json()
        if len(gpslogsList) == 1:
            newGpslog = {
                    "name": "Test GpsLog - API",
                    "startts": "2022-09-25T10:10:00Z",
                    "endts": "2022-09-25T10:10:09Z",
                    "the_geom": "SRID=4326;LINESTRING (11.15 46.15, 11.220552 46.08421, 11.45 46.05)",
                    "width": 5.0,
                    "color": "#00FF00",
                    "user": 2,
                    "project": 1,
                    "gpslogdata": [
                        {
                            "the_geom": "SRID=4326;POINT (11.15 46.15 425)", 
                            "ts": "2022-09-25 10:10:00",
                        },
                        {
                            "the_geom": "SRID=4326;POINT (11.2 46.2 356)", 
                            "ts": "2022-09-25 10:10:05",
                        },
                        {
                            "the_geom": "SRID=4326;POINT (11.45 46.05 482)", 
                            "ts": "2022-09-25 10:10:09",
                        }
                    ]
                }

            print(newGpslog['gpslogdata'][0]['ts'])
            headers = {
                    "Content-Type":"application/json",
                    "Accept":"application/json",
                }
            r = requests.post(url = gpslogsUrl,headers=headers, json=json.dumps(newGpslog), auth = surveyorAuth)
            if r.status_code != 200:
                print(r.json())


    
