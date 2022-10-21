from datetime import datetime, timezone
from io import BytesIO
from rest_framework import serializers
from .models import Note, DbNamings, Project, GpsLog, GpsLogData, Image, ImageData, Utilities, WmsSource, TmsSource, UserConfiguration, LastUserPosition, ProjectData
from django.contrib.auth.models import User, Group
from django.db import transaction
from django.contrib.gis.geos import LineString, Point
from django.contrib.gis.measure import D
import json
from PIL import Image as PilImage
from PIL import ImageOps as PilOps
import base64

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
        fields = '__all__'

class ProjectDataSerializer(serializers.ModelSerializer):
    class Meta:
        model = ProjectData
        fields = '__all__'

class ProjectNameSerializer(serializers.ModelSerializer):
    class Meta:
        model = Project
        fields = [DbNamings.PROJECT_NAME]


class RenderNoteSerializer(serializers.ModelSerializer):
    label = serializers.SerializerMethodField( )

    def get_label( self, obj ):
        form = obj.form
        if form:
            labelString = []
            Utilities.collectIsLabelValue(form, labelString)
            if labelString:
                return labelString[0]
            else:
                return obj.text
        else:
            return obj.text    

    class Meta:
        model = Note
        fields = ['id', 'label', 'the_geom', 'marker', 'size', 'color']
    


class NoteSerializer(serializers.ModelSerializer):
    def to_internal_value(self, data):
        dataconv = json.loads(data)
        images = None
        if DbNamings.NOTE_IMAGES in dataconv:
            images = dataconv[DbNamings.NOTE_IMAGES]
        internal_value = super(NoteSerializer, self).to_internal_value(dataconv)
        if images:
            internal_value[DbNamings.NOTE_IMAGES] = images
        return internal_value
    
    def create(self, validated_data):
        user = User.objects.filter(username = validated_data[DbNamings.USER]).first()
        project = Project.objects.filter(id = validated_data[DbNamings.PROJECT].id).first()

        if user:
            with transaction.atomic():
                point = Point.from_ewkt(validated_data[DbNamings.GEOM])
                
                # check if a previous note exists in place
                parentNote = Note.objects.filter(the_geom__dwithin=(point, 0.000001)).first()
                previous = None
                if parentNote:
                    previous = parentNote


                dt = datetime.now(tz=timezone.utc)
                tsStr = dt.strftime("%Y-%m-%d %H:%M:%S")

                form = validated_data[DbNamings.NOTE_FORM]
                formDict = None
                if form:
                    formDict = json.loads(form)

                note = Note.objects.create(
                    the_geom=point,
                    previous = previous,
                    altim = validated_data[DbNamings.NOTE_ALTIM],
                    ts = validated_data[DbNamings.NOTE_TS],
                    uploadts = tsStr,
                    description = validated_data[DbNamings.NOTE_DESCRIPTION],
                    text = validated_data[DbNamings.NOTE_TEXT],
                    marker = validated_data[DbNamings.NOTE_MARKER],
                    size = validated_data[DbNamings.NOTE_SIZE],
                    rotation = validated_data[DbNamings.NOTE_ROTATION],
                    color = validated_data[DbNamings.NOTE_COLOR],
                    accuracy = validated_data[DbNamings.NOTE_ACCURACY],
                    heading = validated_data[DbNamings.NOTE_HEADING],
                    speed = validated_data[DbNamings.NOTE_SPEED],
                    speedaccuracy = validated_data[DbNamings.NOTE_SPEEDACCURACY],
                    form = formDict,
                    user = user,
                    project = project
                )

                if formDict and DbNamings.NOTE_IMAGES in validated_data:
                    # the note has internal images, which we need to set straight
                    imagesList = validated_data[DbNamings.NOTE_IMAGES],
                    old2NewIdsMap = {}
                    for oldImageId, data in imagesList[0].items():
                        imageDataBase64 = data[DbNamings.IMAGE_IMAGEDATA][DbNamings.IMAGEDATA_DATA].encode('UTF-8')
                        imageDataByteArray = base64.b64decode(imageDataBase64)

                        # create a thumbnail
                        pilImage = PilImage.open(BytesIO(imageDataByteArray))
                        w = pilImage.size[0]
                        h = pilImage.size[1]
                        thumbW = 150.0
                        thumbH = thumbW * h / w
                        thumbnail = PilOps.fit(pilImage, (round(thumbW), round(thumbH)), PilImage.ANTIALIAS)
                        thumbByteArray = BytesIO()
                        thumbnail.save(thumbByteArray, format='PNG')
                        thumbByteArray = thumbByteArray.getvalue()

                        imageData = ImageData.objects.create(
                            data = imageDataByteArray
                        )
                        image = Image.objects.create(
                            the_geom = Point.from_ewkt(data[DbNamings.GEOM]),
                            altim = data[DbNamings.IMAGE_ALTIM],
                            ts = data[DbNamings.IMAGE_TIMESTAMP],
                            uploadts = tsStr,
                            azimuth = data[DbNamings.IMAGE_AZIMUTH],
                            text = data[DbNamings.IMAGE_TEXT],
                            thumbnail = thumbByteArray,
                            imagedata = imageData,
                            user = user,
                            project = project,
                            notes = note,
                        )
                        newImageId = image.id
                        old2NewIdsMap[int(oldImageId)] = newImageId
                    
                    # now update form with new ids
                    Utilities.updateImageIds(formDict, old2NewIdsMap)
                    # newForm = json.dumps(formDict)
                    note.form = formDict
                    note.save()
                    
        return note

    class Meta:
        model = Note
        fields = '__all__'

class GpslogSerializer(serializers.ModelSerializer):
    LOGDATALABEL = 'gpslogdata'

    def to_internal_value(self, data):
        dataconv = json.loads(data)
        internal_value = super(GpslogSerializer, self).to_internal_value(dataconv)

        if self.LOGDATALABEL in dataconv:
            gpslogData = dataconv.get(self.LOGDATALABEL)
            internal_value.update({
                self.LOGDATALABEL: gpslogData
            })
        return internal_value

    def create(self, validated_data):
        user = User.objects.filter(username = validated_data[DbNamings.USER]).first()
        project = Project.objects.filter(id = validated_data[DbNamings.PROJECT].id).first()
        if user:
            with transaction.atomic():
                gpsLog = GpsLog.objects.create(
                    name = validated_data[DbNamings.GPSLOG_NAME],
                    startts = str(validated_data[DbNamings.GPSLOG_STARTTS]),
                    endts = str(validated_data[DbNamings.GPSLOG_ENDTS]),
                    the_geom = LineString.from_ewkt(validated_data[DbNamings.GEOM]),
                    width = validated_data[DbNamings.GPSLOG_WIDTH],
                    color = validated_data[DbNamings.GPSLOG_COLOR],
                    user = user,
                    project = project,
                )

                if self.LOGDATALABEL in validated_data:
                    # gpslogs are usually sent with log data
                    data = validated_data[self.LOGDATALABEL],
                    if len(data[0]) > 1:
                        logdataList = []
                        for record in data[0]:
                            logdataList.append( GpsLogData(
                                the_geom=Point.from_ewkt(record[DbNamings.GEOM]),
                                ts = record[DbNamings.GPSLOGDATA_TIMESTAMP],
                                gpslogid=gpsLog
                            ))
                        GpsLogData.objects.bulk_create(
                            logdataList
                        )
            return gpsLog

    class Meta:
        model = GpsLog
        fields = '__all__'


class ImageDataSerializer(serializers.ModelSerializer):
    def to_internal_value(self, data):
        imageDataB64 = data[DbNamings.IMAGEDATA_DATA]
        internal_value = super(ImageDataSerializer, self).to_internal_value(data)
        internal_value[DbNamings.IMAGEDATA_DATA] = imageDataB64
        return internal_value

    class Meta:
        model = ImageData
        fields = '__all__'

class RenderImageSerializer(serializers.ModelSerializer):
    class Meta:
        model = Image
        fields = ['id', 'text', 'the_geom', 'thumbnail']

class ImageSerializer(serializers.ModelSerializer):
    imagedata = ImageDataSerializer(required=False )

    def to_internal_value(self, data):
        mutableData = json.loads(data)
        internal_value = super(ImageSerializer, self).to_internal_value(mutableData)
        return internal_value

    def create(self, validated_data):
        user = User.objects.filter(username = validated_data[DbNamings.USER]).first()
        project = Project.objects.filter(id = validated_data[DbNamings.PROJECT].id).first()
        if user:
            with transaction.atomic():
                imageDataBase64 = validated_data[DbNamings.IMAGE_IMAGEDATA][DbNamings.IMAGEDATA_DATA].encode('UTF-8')
                imageDataByteArray = base64.b64decode(imageDataBase64)

                # create a thumbnail
                pilImage = PilImage.open(BytesIO(imageDataByteArray))
                w = pilImage.size[0]
                h = pilImage.size[1]
                thumbW = 150.0
                thumbH = thumbW * h / w
                thumbnail = PilOps.fit(pilImage, (round(thumbW), round(thumbH)), PilImage.ANTIALIAS)
                thumbByteArray = BytesIO()
                thumbnail.save(thumbByteArray, format='PNG')
                thumbByteArray = thumbByteArray.getvalue()

                dt = datetime.now(tz=timezone.utc)
                tsStr = dt.strftime("%Y-%m-%d %H:%M:%S")

                imageData = ImageData.objects.create(
                    data = imageDataByteArray
                )

                image = Image.objects.create(
                    the_geom = Point.from_ewkt(validated_data[DbNamings.GEOM]),
                    altim = validated_data[DbNamings.IMAGE_ALTIM],
                    ts = validated_data[DbNamings.IMAGE_TIMESTAMP],
                    uploadts = tsStr,
                    azimuth = validated_data[DbNamings.IMAGE_AZIMUTH],
                    text = validated_data[DbNamings.IMAGE_TEXT],
                    thumbnail = thumbByteArray,
                    imagedata = imageData,
                    user = user,
                    project = project,
                    notes = None,
                )
                return image

    class Meta:
        model = Image
        fields = '__all__'

        
class WmsSourceSerializer(serializers.ModelSerializer):
    class Meta:
        model = WmsSource
        fields = '__all__'

class TmsSourceSerializer(serializers.ModelSerializer):
    class Meta:
        model = TmsSource
        fields = '__all__'


class UserConfigurationSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserConfiguration
        fields = '__all__'


class LastUserPositionSerializer(serializers.ModelSerializer):
    class Meta:
        model = LastUserPosition
        fields = '__all__'