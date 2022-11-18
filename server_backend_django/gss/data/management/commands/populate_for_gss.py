from django.core.management.base import BaseCommand, CommandError

from django.conf import settings
from django.core.management import call_command
from data.models import DbNamings, Project
from django.contrib.auth.models import User, Group, Permission

from django.db import connection

class Command(BaseCommand):
    help = 'Populate the database with base users and groups.'

    def add_arguments(self, parser):
        pass

    def handle(self, *args, **options):

        existsingMandatoryGroups = 3

        coordsGroup = Group.objects.filter(name=DbNamings.GROUP_COORDINATORS).first()
        # create coordinators group if it doesn't exist, since it is necessary
        if not coordsGroup:
            existsingMandatoryGroups-=1
            coordsGroup = Group.objects.create(name=DbNamings.GROUP_COORDINATORS)

        surveyorsGroup = Group.objects.filter(name=DbNamings.GROUP_SURVEYORS).first()
        # create surveyors group if it doesn't exist, since it is necessary
        if not surveyorsGroup:
            existsingMandatoryGroups-=1
            surveyorsGroup = Group.objects.create(name=DbNamings.GROUP_SURVEYORS)

        webusersGroup = Group.objects.filter(name=DbNamings.GROUP_WEBUSERS).first()
        # create webusers group if it doesn't exist, since it is necessary
        if not webusersGroup:
            existsingMandatoryGroups-=1
            webusersGroup = Group.objects.create(name=DbNamings.GROUP_WEBUSERS)
        
        defaultGroup = Group.objects.filter(name=DbNamings.GROUP_DEFAULT).first()
        # create default group if it doesn't exist, since it is necessary
        if not defaultGroup:
            defaultGroup = Group.objects.create(name=DbNamings.GROUP_DEFAULT)
        
        projectsCount = Project.objects.count()
        if projectsCount == 0:
            # create default project
            defaultProject = Project(name=DbNamings.PROJECT_DEFAULT, description=DbNamings.PROJECT_DEFAULT)
            defaultProject.save()

            defaultProject.groups.add(defaultGroup)
            defaultProject.save()

        coordsCount = User.objects.filter(groups__name=DbNamings.GROUP_COORDINATORS).count()
        surveyorsCount = User.objects.filter(groups__name=DbNamings.GROUP_SURVEYORS).count()
        webusersCount = User.objects.filter(groups__name=DbNamings.GROUP_WEBUSERS).count()
        
        # we need at least a coordinator
        if coordsCount == 0:
            coordUser = User.objects.filter(username="coordinator").first()
            if not coordUser:
                coordUser = User(username = "coordinator", first_name="Mary", last_name="Coordinator", 
                            is_staff=True, is_active=True, email="mary@gss.com")
                coordUser.set_password("coordinator")
                coordUser.save()
                coordsGroup.user_set.add(coordUser)
                webusersGroup.user_set.add(coordUser)
                defaultGroup.user_set.add(coordUser)
            else:
                coordsGroup.user_set.add(coordUser)
        
        # we need at least one surveyor
        if surveyorsCount == 0:
            surveyorUser = User.objects.filter(username="surveyor").first()
            if not surveyorUser:
                surveyorUser = User(username = "surveyor", first_name="Jack", last_name="Surveyor", 
                            is_staff=True, is_active=True, email="jack@gss.com")
                surveyorUser.set_password("surveyor")
                surveyorUser.save()
                surveyorsGroup.user_set.add(surveyorUser)
                webusersGroup.user_set.add(surveyorUser)
                defaultGroup.user_set.add(surveyorUser)
            else:
                surveyorsGroup.user_set.add(surveyorUser)
        
        # we need at least one webuser apart of the coordinator and surveyor
        if webusersCount == 0:
            webuserUser = User.objects.filter(username="webuser").first()
            if not webuserUser:
                webuserUser = User(username = "webuser", first_name="Lazy", last_name="Webuser", 
                            is_staff=True, is_active=True, email="lazy@gss.com")
                webuserUser.set_password("webuser")
                webuserUser.save()
                webusersGroup.user_set.add(webuserUser)
                defaultGroup.user_set.add(webuserUser)
            else:
                webusersGroup.user_set.add(webuserUser)
        
        superUser = User.objects.filter(username="admin").first()
        # check if the admin has been created
        if not superUser:
            superUser = User(username = "admin", is_staff=True, is_superuser=True)
            superUser.set_password("admin")
            superUser.save()

        # if the necessary groups were not ALL already made, also set base GSS permissions
        if existsingMandatoryGroups == 0:
            permissions = Permission.objects.all()
            for permission in permissions:
                codename = permission.codename

                if codename == "add_device":
                    coordsGroup.permissions.add(permission)
                elif codename == "change_device":
                    coordsGroup.permissions.add(permission)
                elif codename == "delete_device":
                    coordsGroup.permissions.add(permission)
                elif codename == "view_device":
                    coordsGroup.permissions.add(permission)
                    webusersGroup.permissions.add(permission)
                elif codename == "add_gpslog":
                    coordsGroup.permissions.add(permission)
                    surveyorsGroup.permissions.add(permission)
                elif codename == "change_gpslog":
                    coordsGroup.permissions.add(permission)
                elif codename == "delete_gpslog":
                    coordsGroup.permissions.add(permission)
                elif codename == "view_gpslog":
                    coordsGroup.permissions.add(permission)
                    surveyorsGroup.permissions.add(permission)
                    webusersGroup.permissions.add(permission)
                elif codename == "add_userinfo":
                    coordsGroup.permissions.add(permission)
                elif codename == "change_userinfo":
                    coordsGroup.permissions.add(permission)
                elif codename == "delete_userinfo":
                    coordsGroup.permissions.add(permission)
                elif codename == "view_userinfo":
                    coordsGroup.permissions.add(permission)
                    webusersGroup.permissions.add(permission)
                elif codename == "add_userdeviceassociation":
                    coordsGroup.permissions.add(permission)
                elif codename == "change_userdeviceassociation":
                    coordsGroup.permissions.add(permission)
                elif codename == "delete_userdeviceassociation":
                    coordsGroup.permissions.add(permission)
                elif codename == "view_userdeviceassociation":
                    coordsGroup.permissions.add(permission)
                    webusersGroup.permissions.add(permission)
                elif codename == "add_project":
                    coordsGroup.permissions.add(permission)
                elif codename == "change_project":
                    coordsGroup.permissions.add(permission)
                elif codename == "delete_project":
                    coordsGroup.permissions.add(permission)
                elif codename == "view_project":
                    coordsGroup.permissions.add(permission)
                    surveyorsGroup.permissions.add(permission)
                    webusersGroup.permissions.add(permission)
                elif codename == "add_note":
                    coordsGroup.permissions.add(permission)
                    surveyorsGroup.permissions.add(permission)
                elif codename == "change_note":
                    coordsGroup.permissions.add(permission)
                elif codename == "delete_note":
                    coordsGroup.permissions.add(permission)
                elif codename == "view_note":
                    coordsGroup.permissions.add(permission)
                    surveyorsGroup.permissions.add(permission)
                    webusersGroup.permissions.add(permission)
                elif codename == "add_imagedata":
                    coordsGroup.permissions.add(permission)
                    surveyorsGroup.permissions.add(permission)
                elif codename == "change_imagedata":
                    coordsGroup.permissions.add(permission)
                elif codename == "delete_imagedata":
                    coordsGroup.permissions.add(permission)
                elif codename == "view_imagedata":
                    coordsGroup.permissions.add(permission)
                    surveyorsGroup.permissions.add(permission)
                    webusersGroup.permissions.add(permission)
                elif codename == "add_image":
                    coordsGroup.permissions.add(permission)
                    surveyorsGroup.permissions.add(permission)
                elif codename == "change_image":
                    coordsGroup.permissions.add(permission)
                elif codename == "delete_image":
                    coordsGroup.permissions.add(permission)
                elif codename == "view_image":
                    coordsGroup.permissions.add(permission)
                    surveyorsGroup.permissions.add(permission)
                    webusersGroup.permissions.add(permission)
                elif codename == "add_gpslogdata":
                    coordsGroup.permissions.add(permission)
                    surveyorsGroup.permissions.add(permission)
                elif codename == "change_gpslogdata":
                    coordsGroup.permissions.add(permission)
                elif codename == "delete_gpslogdata":
                    coordsGroup.permissions.add(permission)
                elif codename == "view_gpslogdata":
                    coordsGroup.permissions.add(permission)
                    surveyorsGroup.permissions.add(permission)
                    webusersGroup.permissions.add(permission)
                elif codename == "add_logentry":
                    coordsGroup.permissions.add(permission)
                elif codename == "change_logentry":
                    coordsGroup.permissions.add(permission)
                elif codename == "delete_logentry":
                    coordsGroup.permissions.add(permission)
                elif codename == "view_logentry":
                    coordsGroup.permissions.add(permission)
                elif codename == "add_permission":
                    coordsGroup.permissions.add(permission)
                elif codename == "change_permission":
                    coordsGroup.permissions.add(permission)
                elif codename == "delete_permission":
                    coordsGroup.permissions.add(permission)
                elif codename == "view_permission":
                    coordsGroup.permissions.add(permission)
                elif codename == "add_group":
                    coordsGroup.permissions.add(permission)
                elif codename == "change_group":
                    coordsGroup.permissions.add(permission)
                elif codename == "delete_group":
                    coordsGroup.permissions.add(permission)
                elif codename == "view_group":
                    coordsGroup.permissions.add(permission)
                elif codename == "add_user":
                    coordsGroup.permissions.add(permission)
                elif codename == "change_user":
                    coordsGroup.permissions.add(permission)
                elif codename == "delete_user":
                    coordsGroup.permissions.add(permission)
                elif codename == "view_user":
                    coordsGroup.permissions.add(permission)
                elif codename == "add_lastuserposition":
                    coordsGroup.permissions.add(permission)
                    surveyorsGroup.permissions.add(permission)
                elif codename == "change_lastuserposition":
                    coordsGroup.permissions.add(permission)
                    surveyorsGroup.permissions.add(permission)
                elif codename == "delete_lastuserposition":
                    coordsGroup.permissions.add(permission)
                    surveyorsGroup.permissions.add(permission)
                elif codename == "view_lastuserposition":
                    coordsGroup.permissions.add(permission)
                    surveyorsGroup.permissions.add(permission)
                    webusersGroup.permissions.add(permission)
                elif codename == "add_tmssource":
                    coordsGroup.permissions.add(permission)
                elif codename == "change_tmssource":
                    coordsGroup.permissions.add(permission)
                elif codename == "delete_tmssource":
                    coordsGroup.permissions.add(permission)
                elif codename == "view_tmssource":
                    coordsGroup.permissions.add(permission)
                    webusersGroup.permissions.add(permission)
                elif codename == "add_userconfiguration":
                    coordsGroup.permissions.add(permission)
                    webusersGroup.permissions.add(permission)
                elif codename == "change_userconfiguration":
                    coordsGroup.permissions.add(permission)
                    webusersGroup.permissions.add(permission)
                elif codename == "delete_userconfiguration":
                    coordsGroup.permissions.add(permission)
                    webusersGroup.permissions.add(permission)
                elif codename == "view_userconfiguration":
                    coordsGroup.permissions.add(permission)
                    webusersGroup.permissions.add(permission)
                elif codename == "add_wmssource":
                    coordsGroup.permissions.add(permission)
                elif codename == "change_wmssource":
                    coordsGroup.permissions.add(permission)
                elif codename == "delete_wmssource":
                    coordsGroup.permissions.add(permission)
                elif codename == "view_wmssource":
                    coordsGroup.permissions.add(permission)
                    webusersGroup.permissions.add(permission)
                elif codename == "add_contenttype":
                    continue
                elif codename == "change_contenttype":
                    continue
                elif codename == "delete_contenttype":
                    continue
                elif codename == "view_contenttype":
                    continue
                elif codename == "add_session":
                    continue
                elif codename == "change_session":
                    continue
                elif codename == "delete_session":
                    continue
                elif codename == "view_session":
                    continue

 


    
