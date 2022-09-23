from datetime import datetime
from django.db import models
from django.contrib.gis.db import models as geomodels
from django.contrib.gis.geos import Point, LineString
from django.contrib.auth.models import User, Group

class DbNamings():
    GROUP_COORDINATORS = "Coordinators"
    GROUP_SURVEYORS = "Surveyors"
    GROUP_WEBUSERS = "Webusers"
    GROUP_DEFAULT = "Default"
    PROJECT_DEFAULT = "Default"

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
    NOTE_USER = "userid"
    NOTE_PROJECT = "projectid"

    GPSLOG_ID = "id" 
    GPSLOG_NAME = "name" 
    GPSLOG_STARTTS = "startts" 
    GPSLOG_ENDTS = "endts" 
    GPSLOG_UPLOADTIMESTAMP = "uploadts" 
    GPSLOG_USER = "userid" 
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
    IMAGE_USER = "userid" 
    IMAGE_PROJECT = "projectid" 

    IMAGEDATA_ID = "id" 
    IMAGEDATA_DATA = "data" 
    IMAGEDATA_USER = "userid"

    DEVICE_ID = "id"
    DEVICE_NAME = "name"
    DEVICE_UNIQUE_ID = "uniqueid"
    DEVICE_CONTACT = "contact"
    DEVICE_ACTIVE = "active"

    U_D_ASS_ID = "id"
    U_D_ASS_USERID = "userid"
    U_D_ASS_DEVICEID = "deviceid"
    U_D_ASS_FROMDATE = "fromdate"
    U_D_ASS_TODATE = "todate"

    PROJECT_ID = "id"
    PROJECT_NAME = "name"
    PROJECT_DESCRIPTION = "description"
    PROJECT_GROUPS = "groups"
class Project(models.Model):
    name = models.CharField(name=DbNamings.PROJECT_NAME, max_length=200, null=False)
    description = models.TextField(name=DbNamings.PROJECT_DESCRIPTION,  null=True, default="")
    groups = models.ManyToManyField(Group, name=DbNamings.PROJECT_GROUPS)
    # TODO projectdata, configurations, webmaplayers
    

class Note(models.Model):
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
    rotation = models.FloatField(name=DbNamings.NOTE_ROTATION, null=True, blank=True)
    color = models.CharField(max_length=9,name=DbNamings.NOTE_COLOR, null=False, default="#FF0000")
    accuracy = models.FloatField(name=DbNamings.NOTE_ACCURACY, null=False, default=0)
    heading = models.FloatField(name=DbNamings.NOTE_HEADING, null=False, default=0)
    speed = models.FloatField(name=DbNamings.NOTE_SPEED, null=False, default=0)
    speedaccuracy = models.FloatField(name=DbNamings.NOTE_SPEEDACCURACY, null=False, default=0)
    form = models.JSONField(name=DbNamings.NOTE_FORM, null=True, blank=True)

    user = models.ForeignKey(User, on_delete=models.CASCADE, null=False, name=DbNamings.NOTE_USER, default=-1)
    project = models.ForeignKey(Project, on_delete=models.CASCADE, null=False, name=DbNamings.NOTE_PROJECT, default=-1)


    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.NOTE_PREVID]),
            models.Index(fields=[DbNamings.NOTE_TS]),
            models.Index(fields=[DbNamings.NOTE_UPLOADTS]),
            models.Index(fields=[DbNamings.NOTE_USER]),
            models.Index(fields=[DbNamings.NOTE_PROJECT]),
        ]

class GpsLog(models.Model):
    name = models.CharField(name=DbNamings.GPSLOG_NAME, max_length=200, null=False)
    startTimestamp = models.DateTimeField(name=DbNamings.GPSLOG_STARTTS, null=False, default=datetime.now)
    endTimestamp = models.DateTimeField(name=DbNamings.GPSLOG_ENDTS, null=False, default=datetime.now)
    uploadTimestamp = models.DateTimeField(name=DbNamings.GPSLOG_UPLOADTIMESTAMP, null=False, default=datetime.now)
    geometry = geomodels.LineStringField(
        name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=LineString())
    width = models.FloatField(name=DbNamings.GPSLOG_WIDTH, null=False,default=3)
    color = models.CharField(max_length=9,name=DbNamings.GPSLOG_COLOR, null=False, default="#FF0000")

    user = models.ForeignKey(User, on_delete=models.CASCADE, null=False, name=DbNamings.GPSLOG_USER, default=-1)
    project = models.ForeignKey(Project, on_delete=models.CASCADE, null=False, name=DbNamings.GPSLOG_PROJECT, default=-1)

    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.GPSLOG_UPLOADTIMESTAMP]),
            models.Index(fields=[DbNamings.GPSLOG_USER]),
            models.Index(fields=[DbNamings.GPSLOG_PROJECT]),
        ]

class GpsLogData(models.Model):
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
    data = models.BinaryField(name=DbNamings.IMAGEDATA_DATA, null=False, default=bytearray([]))
    user = models.ForeignKey(User, on_delete=models.CASCADE, null=False, name=DbNamings.IMAGEDATA_USER, default=-1)

    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.IMAGEDATA_USER]),
        ]

class Image(models.Model):
    geometry = geomodels.PointField(
        name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=Point())
    altimetry = models.FloatField(name=DbNamings.IMAGE_ALTIM, null=False, default=-1)
    timestamp = models.DateTimeField(name=DbNamings.IMAGE_TIMESTAMP, null=False, default=datetime.now)
    uploadTimestamp = models.DateTimeField(name=DbNamings.IMAGE_UPLOADTIMESTAMP, null=False, default=datetime.now)
    azimuth = models.FloatField(name=DbNamings.IMAGE_AZIMUTH, null=False,default=0)
    text = models.TextField(name=DbNamings.IMAGE_TEXT, null=False, default="")
    thumbnail = models.BinaryField(name=DbNamings.IMAGE_THUMB, null=False, default=bytearray([]))

    note = models.ForeignKey(Note, on_delete=models.CASCADE, null=True, blank=True, name=DbNamings.IMAGE_NOTE, default=-1)
    imageData = models.ForeignKey(ImageData, on_delete=models.CASCADE, null=False, name=DbNamings.IMAGE_IMAGEDATA, default=-1)
    user = models.ForeignKey(User, on_delete=models.CASCADE, null=False, name=DbNamings.IMAGE_USER, default=-1)
    project = models.ForeignKey(Project, on_delete=models.CASCADE, null=False, name=DbNamings.IMAGE_PROJECT, default=-1)


    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.IMAGE_TIMESTAMP]),
            models.Index(fields=[DbNamings.IMAGE_UPLOADTIMESTAMP]),
            models.Index(fields=[DbNamings.IMAGE_NOTE]),
            models.Index(fields=[DbNamings.IMAGE_IMAGEDATA]),
            models.Index(fields=[DbNamings.IMAGE_USER]),
            models.Index(fields=[DbNamings.IMAGE_PROJECT]),
        ]

class Device(models.Model):
    uniqueId = models.CharField(name=DbNamings.DEVICE_UNIQUE_ID, max_length=100, null=False, unique=True)
    name = models.CharField(name=DbNamings.DEVICE_NAME, max_length=100, null=False)
    isActive = models.BooleanField(name=DbNamings.DEVICE_ACTIVE, null=False)

class UserDeviceAssociation(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, null=False, name=DbNamings.U_D_ASS_USERID, default=-1)
    device = models.ForeignKey(Device, on_delete=models.CASCADE, null=False, name=DbNamings.U_D_ASS_DEVICEID, default=-1)
    fromDate = models.DateTimeField(name=DbNamings.U_D_ASS_FROMDATE, null=False, default=datetime.now)
    toDate = models.DateTimeField(name=DbNamings.U_D_ASS_TODATE, null=True, default=datetime.now)

    class Meta:
        indexes = [
            models.Index(fields=[DbNamings.U_D_ASS_USERID]),
            models.Index(fields=[DbNamings.U_D_ASS_DEVICEID]),
        ]

