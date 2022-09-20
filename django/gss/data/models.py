from datetime import datetime
from django.db import models
from django.contrib.gis.db import models as geomodels
from django.contrib.gis.geos import Point, LineString


class DbNamings():
    GEOM = "the_geom"

    NOTE_ID = "id"
    NOTE_PREVID = "previd"
    NOTE_ALTIM = "altim"
    NOTE_TS = "ts"
    NOTE_UPLOADTS = "uploadts"
    NOTE_DESCRIPTION = "description"
    NOTE_TEXT = "text"
    NOTE_MARKER = "marker"
    NOTE_SIZE = "size"
    NOTE_ROTATION = "rotation"
    NOTE_COLOR = "color"
    NOTE_ACCURACY = "accuracy"
    NOTE_HEADING = "heading"
    NOTE_SPEED = "speed"
    NOTE_SPEEDACCURACY = "speedaccuracy"
    NOTE_FORM = "form"
    NOTE_SURVEYOR = "surveyorid"
    NOTE_PROJECT = "projectid"

    SURVEYOR_ID = "id"
    SURVEYOR_NAME = "name"
    SURVEYOR_DEVICE_ID = "deviceid"
    SURVEYOR_CONTACT = "contact"
    SURVEYOR_ACTIVE = "active"

    PROJECT_ID = "id"
    PROJECT_NAME = "name"

    GPSLOG_ID = "id" 
    GPSLOG_NAME = "name" 
    GPSLOG_STARTTS = "startts" 
    GPSLOG_ENDTS = "endts" 
    GPSLOG_UPLOADTIMESTAMP = "uploadts" 
    GPSLOG_SURVEYOR = "surveyorid" 
    GPSLOG_PROJECT = "projectid" 
    GPSLOG_COLOR = "color" 
    GPSLOG_WIDTH = "width"

    GPSLOGDATA_ID = "id" 
    GPSLOGDATA_ALTIM = "altim" 
    GPSLOGDATA_TIMESTAMP = "ts" 
    GPSLOGDATA_GPSLOGS = "gpslogsid" 

    IMAGE_ID = "id" 
    IMAGE_ALTIM = "altim" 
    IMAGE_TIMESTAMP = "ts" 
    IMAGE_UPLOADTIMESTAMP = "uploadts" 
    IMAGE_AZIMUTH = "azimuth" 
    IMAGE_TEXT = "text" 
    IMAGE_THUMB = "thumbnail" 
    IMAGE_IMAGEDATA = "imagedataid" 
    IMAGE_NOTE = "notesid" 
    IMAGE_SURVEYOR = "surveyorid" 
    IMAGE_PROJECT = "projectid" 

    IMAGEDATA_ID = "id" 
    IMAGEDATA_DATA = "data" 
    IMAGEDATA_SURVEYOR = "surveyorid"

class Surveyor(models.Model):
    id = models.IntegerField(primary_key=True, name=DbNamings.SURVEYOR_ID)
    deviceId = models.CharField(name=DbNamings.SURVEYOR_DEVICE_ID, max_length=100, null=False, unique=True)
    name = models.CharField(name=DbNamings.SURVEYOR_NAME, max_length=100, null=False)
    contact = models.CharField(name=DbNamings.SURVEYOR_CONTACT, max_length=100, null=True)
    isActive = models.BooleanField(name=DbNamings.SURVEYOR_ACTIVE, null=False)

class Project(models.Model):
    id = models.IntegerField(primary_key=True, name=DbNamings.PROJECT_ID)
    name = models.CharField(name=DbNamings.PROJECT_NAME, max_length=200, null=False)

class Note(models.Model):
    id = models.IntegerField(primary_key=True, name=DbNamings.NOTE_ID)
    previousId = models.IntegerField(
        name=DbNamings.NOTE_PREVID, null=True, blank=True)
    geometry = geomodels.PointField(
        name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=Point())
    altim = models.FloatField(name=DbNamings.NOTE_ALTIM, null=False, default=-1)
    timestamp = models.DateTimeField(name=DbNamings.NOTE_TS, null=False, default=datetime.now)
    uploadTimestamp = models.DateTimeField(name=DbNamings.NOTE_UPLOADTS, null=False, default=datetime.now)
    description = models.TextField(name=DbNamings.NOTE_DESCRIPTION, null=False, default="")
    text = models.TextField(name=DbNamings.NOTE_TEXT, null=False, default="")
    marker = models.CharField(name=DbNamings.NOTE_MARKER, max_length=50, null=False, default="circle")
    size = models.FloatField(name=DbNamings.NOTE_SIZE, null=False,default=10)
    rotation = models.FloatField(name=DbNamings.NOTE_ROTATION, null=True)
    color = models.CharField(max_length=9,name=DbNamings.NOTE_COLOR, null=False, default="#FF0000")
    accuracy = models.FloatField(name=DbNamings.NOTE_ACCURACY, null=False, default=0)
    heading = models.FloatField(name=DbNamings.NOTE_HEADING, null=False, default=0)
    speed = models.FloatField(name=DbNamings.NOTE_SPEED, null=False, default=0)
    speedaccuracy = models.FloatField(name=DbNamings.NOTE_SPEEDACCURACY, null=False, default=0)
    form = models.JSONField(name=DbNamings.NOTE_FORM, null=True)

    surveyor = models.ForeignKey(Surveyor, on_delete=models.CASCADE, null=False, name=DbNamings.NOTE_SURVEYOR, default=-1)
    project = models.ForeignKey(Project, on_delete=models.CASCADE, null=False, name=DbNamings.NOTE_PROJECT, default=-1)


    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.NOTE_PREVID]),
            models.Index(fields=[DbNamings.NOTE_TS]),
            models.Index(fields=[DbNamings.NOTE_UPLOADTS]),
            models.Index(fields=[DbNamings.NOTE_SURVEYOR]),
            models.Index(fields=[DbNamings.NOTE_PROJECT]),
        ]

class GpsLog(models.Model):
    id = models.IntegerField(primary_key=True, name=DbNamings.GPSLOG_ID)
    name = models.CharField(name=DbNamings.GPSLOG_NAME, max_length=200, null=False)
    startTimestamp = models.DateTimeField(name=DbNamings.GPSLOG_STARTTS, null=False, default=datetime.now)
    endTimestamp = models.DateTimeField(name=DbNamings.GPSLOG_ENDTS, null=False, default=datetime.now)
    uploadTimestamp = models.DateTimeField(name=DbNamings.GPSLOG_UPLOADTIMESTAMP, null=False, default=datetime.now)
    geometry = geomodels.LineStringField(
        name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=LineString())
    width = models.FloatField(name=DbNamings.GPSLOG_WIDTH, null=False,default=3)
    color = models.CharField(max_length=9,name=DbNamings.GPSLOG_COLOR, null=False, default="#FF0000")

    surveyor = models.ForeignKey(Surveyor, on_delete=models.CASCADE, null=False, name=DbNamings.GPSLOG_SURVEYOR, default=-1)
    project = models.ForeignKey(Project, on_delete=models.CASCADE, null=False, name=DbNamings.GPSLOG_PROJECT, default=-1)

    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.GPSLOG_UPLOADTIMESTAMP]),
            models.Index(fields=[DbNamings.GPSLOG_SURVEYOR]),
            models.Index(fields=[DbNamings.GPSLOG_PROJECT]),
        ]

class GpsLogData(models.Model):
    id = models.IntegerField(primary_key=True, name=DbNamings.GPSLOGDATA_ID)
    geometry = geomodels.PointField(
        name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=Point(), dim=3)
    timestamp = models.DateTimeField(name=DbNamings.GPSLOGDATA_TIMESTAMP, null=False, default=datetime.now)

    gpsLog = models.ForeignKey(GpsLog, on_delete=models.CASCADE, null=False, name=DbNamings.GPSLOGDATA_GPSLOGS, default=-1)

    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.GPSLOGDATA_TIMESTAMP]),
            models.Index(fields=[DbNamings.GPSLOGDATA_GPSLOGS]),
        ]

class ImageData(models.Model):
    id = models.IntegerField(primary_key=True, name=DbNamings.IMAGEDATA_ID)
    data = models.BinaryField(name=DbNamings.IMAGEDATA_DATA, null=False, default=bytearray([]))
    surveyor = models.ForeignKey(Surveyor, on_delete=models.CASCADE, null=False, name=DbNamings.IMAGEDATA_SURVEYOR, default=-1)

    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.IMAGEDATA_SURVEYOR]),
        ]

class Image(models.Model):
    id = models.IntegerField(primary_key=True, name=DbNamings.IMAGE_ID)
    geometry = geomodels.PointField(
        name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=Point())
    altimetry = models.FloatField(name=DbNamings.IMAGE_ALTIM, null=False, default=-1)
    timestamp = models.DateTimeField(name=DbNamings.IMAGE_TIMESTAMP, null=False, default=datetime.now)
    uploadTimestamp = models.DateTimeField(name=DbNamings.IMAGE_UPLOADTIMESTAMP, null=False, default=datetime.now)
    azimuth = models.FloatField(name=DbNamings.IMAGE_AZIMUTH, null=False,default=0)
    text = models.TextField(name=DbNamings.IMAGE_TEXT, null=False, default="")
    thumbnail = models.BinaryField(name=DbNamings.IMAGE_THUMB, null=False, default=bytearray([]))

    note = models.ForeignKey(Note, on_delete=models.CASCADE, null=True, name=DbNamings.IMAGE_NOTE, default=-1)
    imageData = models.ForeignKey(ImageData, on_delete=models.CASCADE, null=False, name=DbNamings.IMAGE_IMAGEDATA, default=-1)
    surveyor = models.ForeignKey(Surveyor, on_delete=models.CASCADE, null=False, name=DbNamings.IMAGE_SURVEYOR, default=-1)
    project = models.ForeignKey(Project, on_delete=models.CASCADE, null=False, name=DbNamings.IMAGE_PROJECT, default=-1)


    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.IMAGE_TIMESTAMP]),
            models.Index(fields=[DbNamings.IMAGE_UPLOADTIMESTAMP]),
            models.Index(fields=[DbNamings.IMAGE_NOTE]),
            models.Index(fields=[DbNamings.IMAGE_IMAGEDATA]),
            models.Index(fields=[DbNamings.IMAGE_SURVEYOR]),
            models.Index(fields=[DbNamings.IMAGE_PROJECT]),
        ]
