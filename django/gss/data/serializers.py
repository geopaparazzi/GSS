from datetime import datetime
from rest_framework import serializers
from .models import Note, DbNamings, Project
from django.contrib.auth.models import User, Group


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