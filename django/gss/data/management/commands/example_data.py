from django.core.management.base import BaseCommand, CommandError

from django.conf import settings
from django.core.management import call_command
from data.models import DbNamings, Project, Note, GpsLogData, GpsLog
from django.contrib.auth.models import User
from django.contrib.gis.geos import Point, LineString
from datetime import datetime

from django.db import connection

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

        if Note.objects.count() == 0:
            note = Note.objects.create(
                the_geom=Point(11.0, 46.0),
                altim = 322,
                ts = "2022-09-23 10:00:00",
                uploadts = "2022-09-23 16:50:00",
                description = "A test note",
                text = "Test Note",
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

 


    
