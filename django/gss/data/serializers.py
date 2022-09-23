from datetime import datetime
from rest_framework import serializers
from .models import Note, DbNamings, Project, GpsLog, GpsLogData
from django.contrib.auth.models import User, Group
from django.db import transaction
from django.contrib.gis.geos import LineString, Point

class UserSerializer(serializers.HyperlinkedModelSerializer):
    class Meta:
        model = User
        fields = ['url', 'username', 'groups', 'first_name', 'last_name', 'is_active', 'email']


class GroupSerializer(serializers.HyperlinkedModelSerializer):
    class Meta:
        model = Group
        fields = ['url', 'name']


class ProjectSerializer(serializers.ModelSerializer):
    class Meta:
        model = Project
        fields = (DbNamings.PROJECT_NAME, DbNamings.PROJECT_DESCRIPTION, DbNamings.PROJECT_GROUPS)


class NoteSerializer(serializers.ModelSerializer):
    class Meta:
        model = Note
        fields = '__all__'

class GpslogSerializer(serializers.ModelSerializer):
    def create(self, validated_data):
        user = User.objects.filter(username = validated_data['username'])
        project = Project.objects.filter(id = validated_data['projectid'])
        if user:
            with transaction.atomic():
                gpsLog = GpsLog.objects.create(
                    name = validated_data['name'],
                    startts = validated_data['startts'],
                    endts = validated_data['endts'],
                    the_geom = LineString.from_ewkt(validated_data['the_geom']),
                    width = validated_data['width'],
                    color = validated_data['color'],
                    userid = user,
                    projectid = project,
                )
                data = validated_data['data'],
                for record in data:
                    GpsLogData.objects.create(
                        the_geom=Point.from_ewkt(record['the_geom']),
                        ts = record['ts'],
                        gpslogid=gpsLog
                    )


    class Meta:
        model = GpsLog
        fields = [ DbNamings.GPSLOG_NAME,
                DbNamings.GPSLOG_STARTTS,
                DbNamings.GPSLOG_ENDTS,
                DbNamings.GPSLOG_USER,
                DbNamings.GPSLOG_PROJECT,
                DbNamings.GPSLOG_COLOR,
                DbNamings.GPSLOG_WIDTH,
                DbNamings.GPSLOG_DATA,
                ]
