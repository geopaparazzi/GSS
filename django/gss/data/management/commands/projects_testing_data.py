from django.core.management.base import BaseCommand

from django.conf import settings
from data.models import Project, Note, GpsLogData, GpsLog, Image, ImageData, ProjectData, WmsSource, TmsSource, DbNamings
from django.contrib.gis.geos import Point, LineString
from django.contrib.auth.models import User, Group

"""
Create a set of projects and project data to do proper checks.

Projects:
* Test Project A
* Test Project B
* Test Project C

Groups:
* Test Group A -> of Test Project A
* Test Group B -> of Test Project B
* Test Group C -> of Test Project C
* Test Group D -> of Test Project A, B, C

Users:
* Test User A1 -> of Test Group A,Surveyor -> should see only OWN data of Project A
* Test User A2 -> of Test Group A,Surveyor -> should see only OWN data of Project A
* Test User A3 -> of Test Group A,Webuser -> should see all data of Project A
* Test User B -> of Test Group B,Surveyor -> should see only OWN data of Project B
* Test User C -> of Test Group C,Surveyor -> should see only OWN data of Project C
* Test User D -> of Test Group D,Webuser -> should see all data of Projects A, B, C

Notes:
* Test Note 1 -> Test User A1 -> Test Project A
* Test Note 2 -> Test User A2 -> Test Project A
* Test Note 3 -> Test User B -> Test Project B
* Test Note 4 -> Test User C -> Test Project C

Logs:
* Test Log 1 -> Test User A1 -> Test Project A
* Test Log 2 -> Test User A2 -> Test Project A
* Test Log 3 -> Test User B -> Test Project B
* Test Log 4 -> Test User C -> Test Project C
"""


class Command(BaseCommand):
    help = 'Populate the database with data to test a multiproject environment.'

    def handle(self, *args, **options):

        if settings.DEBUG == False:
            self.stderr.write("This command is available only when in debug mode. Exiting.")
            return

        self.stdout.write("Clearing data from tables.")
        GpsLogData.objects.all().delete()
        GpsLog.objects.all().delete()
        ImageData.objects.all().delete()
        Image.objects.all().delete()
        Note.objects.all().delete()
        ProjectData.objects.all().delete()
        WmsSource.objects.all().delete()
        TmsSource.objects.all().delete()

        wms1 = WmsSource.objects.create(
            label = "Bolzano Ortofoto",
            version = "1.3.0",
            transparent = True,
            imageformat = "image/png",
            getcapabilities = "http://geoservices.buergernetz.bz.it/mapproxy/ows",
            layername = "p_bz-Orthoimagery:Aerial-2020-RGB",
            opacity = 1.0,
            attribution = "Copyright Province Bolzano",
            epsg = 3857
        )
        wms2 = WmsSource.objects.create(
            label = "Trento CTP",
            version = "1.3.0",
            transparent = True,
            imageformat = "image/png",
            getcapabilities = "https://siat.provincia.tn.it/geoserver/stem/ctp2020_bn_00/wms",
            layername = "ctp2020_bn_00",
            opacity = 1.0,
            attribution = "Copyright Province Trento",
            epsg = 4326
        )
        tms1 = TmsSource.objects.create(
            label = "OSM Mapnik",
            urltemplate = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
            opacity = 1.0,
            subdomains = "a,b,c",
            maxzoom = 19,
            attribution = 'OpenStreetMap, ODbL'
        )

        surveyorsGroup = Group.objects.filter(name=DbNamings.GROUP_SURVEYORS).first()
        webusersGroup = Group.objects.filter(name=DbNamings.GROUP_WEBUSERS).first()

        """
        Users:
        * Test User A1 -> of Test Group A,Surveyor -> should see only OWN data of Project A
        * Test User A2 -> of Test Group A,Surveyor -> should see only OWN data of Project A
        * Test User A3 -> of Test Group A,Webuser -> should see all data of Project A
        * Test User B -> of Test Group B,Surveyor -> should see only OWN data of Project B
        * Test User C -> of Test Group C,Surveyor -> should see only OWN data of Project C
        * Test User D -> of Test Group D,Webuser -> should see all data of Projects A, B, C
        """
        # fist delete existing test users
        User.objects.filter(username__startswith="Test User").delete()

        # then insert new
        userA1 = User(username = "a1", first_name="Test User A1", last_name="User A1", 
            is_staff=True, is_active=True, email="A1@gss.com")
        userA1.set_password("a1")
        userA1.save()
        userA2 = User(username = "a2", first_name="Test User A2", last_name="User A2", 
            is_staff=True, is_active=True, email="A2@gss.com")
        userA2.set_password("a2")
        userA2.save()
        userA3 = User(username = "a3", first_name="Test User A3", last_name="User A3", 
            is_staff=True, is_active=True, email="A3@gss.com")
        userA3.set_password("a3")
        userA3.save()
        userB = User(username = "b", first_name="Test User B", last_name="User B", 
            is_staff=True, is_active=True, email="B@gss.com")
        userB.set_password("b")
        userB.save()
        userC = User(username = "c", first_name="Test User C", last_name="User C", 
            is_staff=True, is_active=True, email="C@gss.com")
        userC.set_password("c")
        userC.save()
        userD = User(username = "d", first_name="Test User D", last_name="User D", 
            is_staff=True, is_active=True, email="D@gss.com")
        userD.set_password("d")
        userD.save()

        """
        Groups:
            * Test Group A -> of Test Project A
            * Test Group B -> of Test Project B
            * Test Group C -> of Test Project C
            * Test Group D -> of Test Project A, B, C
        """
        # fist delete existing test groups
        Group.objects.filter(name__startswith="Test Group").delete()

        # then insert new
        groupA = Group.objects.create(name="Test Group A")
        groupB = Group.objects.create(name="Test Group B")
        groupC = Group.objects.create(name="Test Group C")
        groupD = Group.objects.create(name="Test Group D")

        groupA.user_set.add(userA1)
        groupA.user_set.add(userA2)
        groupA.user_set.add(userA3)
        groupB.user_set.add(userB)
        groupC.user_set.add(userC)
        groupD.user_set.add(userD)
        surveyorsGroup.user_set.add(userA1)
        surveyorsGroup.user_set.add(userA2)
        surveyorsGroup.user_set.add(userB)
        surveyorsGroup.user_set.add(userC)
        webusersGroup.user_set.add(userA3)
        webusersGroup.user_set.add(userD)
        

        """
        Projects:
            * Test Project A
            * Test Project B
            * Test Project C
        """
        # fist delete existing test projects
        Project.objects.filter(name__startswith="Test Project").delete()

        # then insert new
        projectA = Project.objects.create(name="Test Project A", description="Test Project A Description")
        projectB = Project.objects.create(name="Test Project B", description="Test Project B Description")
        projectC = Project.objects.create(name="Test Project C", description="Test Project C Description")

        projectA.wmssources.add(wms1)
        projectB.wmssources.add(wms2)
        projectC.tmssources.add(tms1)
        projectA.groups.add(groupA)
        projectA.groups.add(groupD)
        projectA.save()
        projectB.groups.add(groupB)
        projectB.groups.add(groupD)
        projectB.save()
        projectC.groups.add(groupC)
        projectC.groups.add(groupD)
        projectC.save()
        
        """
        Notes:
        * Test Note 1 -> Test User A1 -> Test Project A
        * Test Note 2 -> Test User A2 -> Test Project A
        * Test Note 3 -> Test User B -> Test Project B
        * Test Note 4 -> Test User C -> Test Project C
        """
        Note.objects.create(
            text = "Test Note 1", description = "Test Note 1 Description", marker = "circle", color = "#FF0000",
            the_geom=Point(11.0, 46.0), altim = 322, ts = "2022-09-23 10:00:00", uploadts = "2022-09-23 16:50:00",
            size = 10, rotation = 0, accuracy = 0, heading = 0, speed = 0, speedaccuracy = 0,
            form = None, user = userA1, project = projectA
        )
        Note.objects.create(
            text = "Test Note 2", description = "Test Note 2 Description", marker = "square", color = "#FF0000",
            the_geom=Point(11.001, 46.001), altim = 422, ts = "2022-09-23 10:00:00", uploadts = "2022-09-23 16:50:00",
            size = 10, rotation = 0, accuracy = 0, heading = 0, speed = 0, speedaccuracy = 0,
            form = None, user = userA2, project = projectA
        )
        Note.objects.create(
            text = "Test Note 3", description = "Test Note 3 Description", marker = "circle", color = "#00FF00",
            the_geom=Point(11.002, 46.002), altim = 522, ts = "2022-09-23 10:00:00", uploadts = "2022-09-23 16:50:00",
            size = 10, rotation = 0, accuracy = 0, heading = 0, speed = 0, speedaccuracy = 0,
            form = None, user = userB, project = projectB
        )
        Note.objects.create(
            text = "Test Note 4", description = "Test Note 4 Description", marker = "circle", color = "#0000FF",
            the_geom=Point(11.002, 46.002), altim = 522, ts = "2022-09-23 10:00:00", uploadts = "2022-09-23 16:50:00",
            size = 10, rotation = 0, accuracy = 0, heading = 0, speed = 0, speedaccuracy = 0,
            form = None, user = userC, project = projectC
        )

        """
        Logs:
        * Test Log 1 -> Test User A1 -> Test Project A
        * Test Log 2 -> Test User A2 -> Test Project A
        * Test Log 3 -> Test User B -> Test Project B
        * Test Log 4 -> Test User C -> Test Project C
        """
        GpsLog.objects.create(
            name = "Test Log 1", startts = "2022-09-23 10:10:00", endts = "2022-09-23 10:10:09",
            the_geom = LineString((11.1, 46.1), (11.2, 46.2), (11.4, 46.0), srid=4326),
            width = 3, color = "#FF00FF",
            user = userA1, project = projectA,
        )
        GpsLog.objects.create(
            name = "Test Log 2", startts = "2022-09-23 10:10:00", endts = "2022-09-23 10:10:09",
            the_geom = LineString((11.1001, 46.1001), (11.2001, 46.2001), (11.4001, 46.0001), srid=4326),
            width = 3, color = "#FF0000",
            user = userA2, project = projectA,
        )
        GpsLog.objects.create(
            name = "Test Log 3", startts = "2022-09-23 10:10:00", endts = "2022-09-23 10:10:09",
            the_geom = LineString((11.1002, 46.1002), (11.2002, 46.2002), (11.4002, 46.0002), srid=4326),
            width = 3, color = "#00FF00",
            user = userB, project = projectB,
        )
        GpsLog.objects.create(
            name = "Test Log 4", startts = "2022-09-23 10:10:00", endts = "2022-09-23 10:10:09",
            the_geom = LineString((11.1003, 46.1003), (11.2003, 46.2003), (11.4003, 46.0003), srid=4326),
            width = 3, color = "#0000FF",
            user = userC, project = projectC,
        )



    
