from datetime import datetime
from rest_framework import serializers
from .models import Note, DbNamings, Project, GpsLog, GpsLogData
from django.contrib.auth.models import User, Group
from django.db import transaction
from django.contrib.gis.geos import LineString, Point
import json

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

    def to_internal_value(self, data):
        dataconv = json.loads(data)
        internal_value = super(GpslogSerializer, self).to_internal_value(dataconv)

        if 'gpslogdata' in dataconv:
            gpslogData = dataconv.get("gpslogdata")
            internal_value.update({
                "gpslogdata": gpslogData
            })
        return internal_value

    def create(self, validated_data):
        user = User.objects.filter(username = validated_data['userid']).first()
        project = Project.objects.filter(id = validated_data['projectid'].id).first()
        if user:
            with transaction.atomic():
                gpsLog = GpsLog.objects.create(
                    name = validated_data['name'],
                    startts = str(validated_data['startts']),
                    endts = str(validated_data['endts']),
                    the_geom = LineString.from_ewkt(validated_data['the_geom']),
                    width = validated_data['width'],
                    color = validated_data['color'],
                    userid = user,
                    projectid = project,
                )

                if 'gpslogdata' in validated_data:
                    # if data were send, let's suppose it is gps log points
                    data = validated_data['gpslogdata'],
                    for record in data[0]:
                        GpsLogData.objects.create(
                            the_geom=Point.from_ewkt(record['the_geom']),
                            ts = record['ts'],
                            gpslogid=gpsLog
                        )
            return gpsLog

    class Meta:
        model = GpsLog
        fields = '__all__'
