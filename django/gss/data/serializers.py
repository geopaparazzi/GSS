from datetime import datetime
from io import BytesIO
from rest_framework import serializers
from .models import Note, DbNamings, Project, GpsLog, GpsLogData, Image, ImageData
from django.contrib.auth.models import User, Group
from django.db import transaction
from django.contrib.gis.geos import LineString, Point
import json
import PIL
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
        fields = (DbNamings.PROJECT_NAME, DbNamings.PROJECT_DESCRIPTION, DbNamings.PROJECT_GROUPS)


class NoteSerializer(serializers.ModelSerializer):
    # def to_internal_value(self, data):
    #     dataconv = json.loads(data)
    #     internal_value = super(NoteSerializer, self).to_internal_value(dataconv)

    #     if 'images' in dataconv:
    #         images = dataconv.get("images")
    #         internal_value.update({
    #             "images": images
    #         })
    #     return internal_value
    
    # def create(self, validated_data):
    #     user = User.objects.filter(username = validated_data[DbNamings.NOTE_USER]).first()
    #     project = Project.objects.filter(id = validated_data[DbNamings.NOTE_PROJECT].id).first()

    #     # ! here we need to check if images are sent with the notes

        # if user:
        #     with transaction.atomic():
        #         gpsLog = GpsLog.objects.create(
        #             name = validated_data['name'],
        #             startts = str(validated_data['startts']),
        #             endts = str(validated_data['endts']),
        #             the_geom = LineString.from_ewkt(validated_data['the_geom']),
        #             width = validated_data['width'],
        #             color = validated_data['color'],
        #             user = user,
        #             project = project,
        #         )

        #         if 'gpslogdata' in validated_data:
        #             # gpslogs are usually sent with log data
        #             data = validated_data['gpslogdata'],
        #             for record in data[0]:
        #                 GpsLogData.objects.create(
        #                     the_geom=Point.from_ewkt(record['the_geom']),
        #                     ts = record['ts'],
        #                     gpslogid=gpsLog
        #                 )
        #     return gpsLog

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
        user = User.objects.filter(username = validated_data[DbNamings.NOTE_USER]).first()
        project = Project.objects.filter(id = validated_data[DbNamings.NOTE_PROJECT].id).first()
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
                    for record in data[0]:
                        GpsLogData.objects.create(
                            the_geom=Point.from_ewkt(record[DbNamings.GEOM]),
                            ts = record[DbNamings.GPSLOGDATA_TIMESTAMP],
                            gpslogid=gpsLog
                        )
            return gpsLog

    class Meta:
        model = GpsLog
        fields = '__all__'

class ImageSerializer(serializers.ModelSerializer):
    IMAGEDATALABEL = 'imagedata'

    def create(self, validated_data):
        user = User.objects.filter(username = validated_data[DbNamings.NOTE_USER]).first()
        project = Project.objects.filter(id = validated_data[DbNamings.NOTE_PROJECT].id).first()
        if user:
            with transaction.atomic():
                # first extract the image data. They will get a new id, and that is the one that goes into
                # the image later
                imageDataDict = validated_data[self.IMAGEDATALABEL]

                if imageDataDict:
                    imageDataBase64 = imageDataDict[DbNamings.IMAGEDATA_DATA]
                    imageDataByteArray = base64.decode(imageDataBase64)

                    imageData = ImageData.objects.create(
                        data = imageDataByteArray,
                        user = user,
                    )

                    pilImage = PIL.Image.open(BytesIO(imageDataByteArray))
                    w = pilImage.size[0]
                    h = pilImage.size[1]
                    thumbW = 150
                    thumbH = thumbW * h / w
                    thumbnail = PIL.ImageOps.fit(pilImage, (thumbW, thumbH), PIL.Image.ANTIALIAS)
                    thumbByteArray = BytesIO()
                    thumbnail.save(thumbByteArray, format='PNG')
                    thumbByteArray = thumbByteArray.getvalue()

                    image = Image.objects.create(
                        the_geom = Point.from_ewkt(validated_data[DbNamings.GEOM]),
                        altim = validated_data[DbNamings.IMAGE_ALTIM],
                        ts = validated_data[DbNamings.IMAGE_TIMESTAMP],
                        uploadts = validated_data[DbNamings.IMAGE_UPLOADTIMESTAMP],
                        azimuth = validated_data[DbNamings.IMAGE_AZIMUTH],
                        text = validated_data[DbNamings.IMAGE_TEXT],
                        thumbnail = thumbByteArray,
                        imageData = imageData,
                        user = user,
                        project = project,
                    )
                    return image
                else:
                    # ! if no imagedata is available, the image can't be inserted
                    # ! it is mandatory that images are inserted with their bytes
                    # TODO check how to best propagate such an error
                    return None

    class Meta:
        model = Image
        fields = '__all__'

        

class ImageDataSerializer(serializers.ModelSerializer):
    class Meta:
        model = ImageData
        fields = '__all__'

