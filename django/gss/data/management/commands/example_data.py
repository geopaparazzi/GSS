from django.core.management.base import BaseCommand, CommandError

from django.conf import settings
from django.core.management import call_command
from data.models import DbNamings, Project, Note, GpsLogData, GpsLog
from django.contrib.auth.models import User
from django.contrib.gis.geos import Point, LineString
from datetime import datetime
import requests
from requests.auth import HTTPBasicAuth
from django.db import connection
import json

class Command(BaseCommand):
    help = 'Populate the database with example data as a surveyor.'

    def add_arguments(self, parser):
        pass

    def handle(self, *args, **options):


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
                userid = surveyorUser,
                projectid = defaultProject
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
                userid = surveyorUser,
                projectid = defaultProject,
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
                        'userid': 2, 
                        'projectid': 1
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
                "userid": 2,
                "projectid": 1,
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


    
