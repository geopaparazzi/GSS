from datetime import datetime, timezone
from django.contrib.auth import authenticate
from django.contrib.auth.models import Group, User
from django.views.decorators.csrf import csrf_exempt
from rest_framework import permissions, status, viewsets
from rest_framework.authtoken.models import Token
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework.status import (HTTP_200_OK, HTTP_400_BAD_REQUEST,
                                    HTTP_403_FORBIDDEN, HTTP_404_NOT_FOUND)
from django.db.models import Max
import os
from django.http import FileResponse
from django.contrib.auth.decorators import login_required
from django.shortcuts import get_object_or_404
from data.models import DbNamings, GpsLog, GpsLogData, Image, ImageData, Note, Project, ProjectData, UserConfiguration, WmsSource, TmsSource
from data.permission import IsCoordinator, IsSuperUser, IsSurveyor, IsWebuser
from data.serializers import (GpslogSerializer, GroupSerializer,
                              ImageSerializer,ImageDataSerializer, LastUserPositionSerializer, NoteSerializer,
                              ProjectSerializer, RenderNoteSerializer,ProjectNameSerializer,ProjectDataSerializer,
                              UserSerializer, RenderImageSerializer, WmsSourceSerializer, 
                              TmsSourceSerializer, UserConfigurationSerializer, LastUserPosition)
from owslib.wmts import WebMapTileService
from owslib.wms import WebMapService
from django.contrib.gis.geos import LineString, Point
from django.http import HttpRequest
from django.http import HttpResponse
from rest_framework import status
from rest_framework.exceptions import NotFound, NotAcceptable
from rest_framework.response import Response
from rest_framework.generics import get_object_or_404
from typing import Optional
import io
import mimetypes



@csrf_exempt
@api_view(["POST"])
@permission_classes((AllowAny,))
def login(request):
    username = request.data.get("username")
    password = request.data.get("password")
    projectId = request.data.get("project")
    if username is None or password is None or projectId is None:
        return Response({'error': 'Please provide username, password and project of choice.'},
                        status=HTTP_400_BAD_REQUEST)
    user = authenticate(username=username, password=password)
    if not user:
        return Response({'error': 'Invalid Credentials'},
                        status=HTTP_404_NOT_FOUND)
    # now check if the user is in the project
    project = Project.objects.filter(id=projectId).first()
    if not project:
        return Response({'error': 'Invalid Project Name'},status=HTTP_404_NOT_FOUND)
    if project.hasUser(user):
        token, _ = Token.objects.get_or_create(user=user)
        return Response({'token': token.key, 'id': user.id},
                        status=HTTP_200_OK)
    else:
        return Response({'error': f'User is not part of project "{project}"'},status=HTTP_403_FORBIDDEN)


class StandardPermissionsViewSet(viewsets.ModelViewSet):

    def get_permissions(self):
        """
        Instantiates and returns the list of permissions that this view requires.
        """
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsCoordinator, permissions.IsAuthenticated]
        else:
            permission_classes = [IsSuperUser | IsCoordinator, permissions.IsAuthenticated]
        return [permission() for permission in permission_classes]


class ListRetrieveOnlyViewSet(StandardPermissionsViewSet):

    def create(self, request):
        response = {'message': 'Create function is not offered in this path.'}
        return Response(response, status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def update(self, request, pk=None):
        response = {'message': 'Update function is not offered in this path.'}
        return Response(response, status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def destroy(self, request, pk=None):
        response = {'message': 'Delete function is not offered in this path.'}
        return Response(response, status=status.HTTP_405_METHOD_NOT_ALLOWED)

class UserViewSet(StandardPermissionsViewSet):
    """
    API endpoint that allows users to be viewed or edited.
    """
    queryset = User.objects.all().order_by('-date_joined')
    serializer_class = UserSerializer

class GroupViewSet(StandardPermissionsViewSet):
    """
    API endpoint that allows groups to be viewed or edited.
    """
    queryset = Group.objects.all()
    serializer_class = GroupSerializer

class ProjectViewSet(StandardPermissionsViewSet):
    """
    API endpoint that allows projects to be viewed or edited.
    """
    queryset = Project.objects.all()
    serializer_class = ProjectSerializer

class ProjectNameViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows projects to be viewed or edited.
    """
    queryset = Project.objects.all()
    serializer_class = ProjectNameSerializer
    permission_classes = [AllowAny]

    def create(self, request):
        response = {'message': 'Create function is not offered in this path.'}
        return Response(response, status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def update(self, request, pk=None):
        response = {'message': 'Update function is not offered in this path.'}
        return Response(response, status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def destroy(self, request, pk=None):
        response = {'message': 'Delete function is not offered in this path.'}
        return Response(response, status=status.HTTP_405_METHOD_NOT_ALLOWED)

class ProjectDataViewSet(ListRetrieveOnlyViewSet):
    """
    API endpoint that allows projectsdata to be downloaded.
    """
    serializer_class = ProjectDataSerializer

    def get_queryset(self):
        projectId = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if projectId is None:
            # the project parameter is mandatory to get the data
            return ProjectData.objects.none()
        else:
            user = self.request.user
            if user.is_superuser:
                projectModel = Project.objects.filter(id=projectId).first()
            else:
                projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
            if projectModel:
                newProjectData = []
                for pd in projectModel.projectdata.all():
                    url = f"/api/projectdatas/{pd.id}"
                    newPd = ProjectData(
                        id = pd.id,
                        label =  pd.label,
                        file = url
                    )
                    newProjectData.append(newPd)

                return newProjectData
            else:
                return ProjectData.objects.none()
    
    def retrieve(
        self,
        request: HttpRequest,
        pk: Optional[int] = None,
    ):
        # projectId = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        # if projectId is None:
        #     raise NotAcceptable("The project parameter is mandatory to retrieve images.")
        # user = self.request.user
        # if user.is_superuser:
        #     projectModel = Project.objects.filter(id=projectId).first()
        # else:
        #     projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
        # if projectModel:
        projectData = ProjectData.objects.get(id=pk)
        if projectData:
            file = projectData.file
            localPath = file.path
            file_mime = mimetypes.guess_type(localPath)
            name = os.path.basename(localPath)

            response = HttpResponse(io.open(localPath, mode="rb").read(), content_type=file_mime[0])
            response['Content-Disposition'] = f'attachment; filename="{name}"'
            return response
        else:
            response = {'message': 'No data for given id available.'}
            return Response(response, status=status.HTTP_400_BAD_REQUEST)

        
        # data = FieldImage.objects.filter(field=field).get(id=pk)
        # path = data.image.path
        # # resize image to 800 to send less bytes
        # im = Image.open(path)
        # im.convert("RGB")
        # # to rescale images longer side to 800
        # coef = 800 / max(im.size)
        # newsize = (int(im.size[0] * coef), int(im.size[1] * coef))
        # thumb_io = BytesIO()
        # out = im.resize(newsize)  # Resize to size
        # out.save(thumb_io, "JPEG", quality=100)
        # mimetype = "image/{}".format("jpg")
        # return HttpResponse(thumb_io.getvalue(), content_type=mimetype)
        # ## old method to return image in binary64
        # # seri = FieldImageBase64Serializer(data)
        # # return Response(seri.data)

class RenderNoteViewSet(ListRetrieveOnlyViewSet):
    """
    API endpoint to get notes with minimal info for rendering.
    """
    serializer_class = RenderNoteSerializer

    def get_queryset(self):
        projectId = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if projectId is None:
            # the project parameter is mandatory to get the data
            return Note.objects.none()
        else:
            user = self.request.user
            if user.is_superuser:
                projectModel = Project.objects.filter(id=projectId).first()
            else:
                projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
            if projectModel:
                aggregation = Note.objects.filter(project__id=projectId).values(DbNamings.GEOM).annotate(id=Max(DbNamings.NOTE_ID)).values_list(DbNamings.NOTE_ID),
                queryset = Note.objects.filter(id__in=aggregation)
                return queryset
            else:
                return Note.objects.none()

class NoteViewSet(StandardPermissionsViewSet):
    """
    API endpoint that allows projects to be viewed or edited.
    """
    serializer_class = NoteSerializer

    def get_queryset(self):
        projectId = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if projectId is None:
            # the project parameter is mandatory to get the data
            return Note.objects.none()
        else:
            user = self.request.user
            if user.is_superuser:
                projectModel = Project.objects.filter(id=projectId).first()
            else:
                projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
            if projectModel:
                # aggregation = Note.objects.filter(project__name=project).values(DbNamings.GEOM).annotate(id=Max(DbNamings.NOTE_ID)).values_list(DbNamings.NOTE_ID),
                # queryset = Note.objects.filter(id__in=aggregation)
                queryset = Note.objects.filter(project__id=projectId)
                return queryset
            else:
                return Note.objects.none()

class ImageViewSet(StandardPermissionsViewSet):
    """
    API endpoint that allows images to be viewed or edited.
    """
    serializer_class = ImageSerializer

    def get_queryset(self):
        projectId = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if projectId is None:
            # the project parameter is mandatory to get the data
            return Image.objects.none()
        else:
            user = self.request.user
            if user.is_superuser:
                projectModel = Project.objects.filter(id=projectId).first()
            else:
                projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
            if projectModel:
                queryset = Image.objects.filter(project__id=projectId)
                return queryset
            else:
                return Image.objects.none()

class RenderImageViewSet(ListRetrieveOnlyViewSet):
    """
    API endpoint to get images with minimal info for rendering.
    """
    serializer_class = RenderImageSerializer

    def get_queryset(self):
        projectId = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if projectId is None:
            # the project parameter is mandatory to get the data
            return Image.objects.none()
        else:
            user = self.request.user
            if user.is_superuser:
                projectModel = Project.objects.filter(id=projectId).first()
            else:
                projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
            if projectModel:
                queryset = Image.objects.filter(project__id=projectId)
                return queryset
            else:
                return Image.objects.none()

class RenderSimpleImageViewSet(ListRetrieveOnlyViewSet):
    """
    API endpoint to get simple images (i.e. only non form) with minimal info for rendering.
    """
    serializer_class = RenderImageSerializer

    def get_queryset(self):
        projectId = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if projectId is None:
            # the project parameter is mandatory to get the data
            return Image.objects.none()
        else:
            user = self.request.user
            if user.is_superuser:
                projectModel = Project.objects.filter(id=projectId).first()
            else:
                projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
            if projectModel:
                queryset = Image.objects.filter(project__id=projectId, notes__isnull=True)
                return queryset
            else:
                return Image.objects.none()

class GpslogViewSet(StandardPermissionsViewSet):
    """
    API endpoint that allows gpslogs to be viewed or edited.
    """
    serializer_class = GpslogSerializer

    def get_queryset(self):
        projectId = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if projectId is None:
            # the project parameter is mandatory to get the data
            return GpsLog.objects.none()
        else:
            user = self.request.user
            if user.is_superuser:
                projectModel = Project.objects.filter(id=projectId).first()
            else:
                projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
            if projectModel:
                queryset = GpsLog.objects.filter(project__id=projectId)
                return queryset
            else:
                return GpsLog.objects.none()

class WmsSourceViewSet(ListRetrieveOnlyViewSet):
    """
    API endpoint to get WmsSource info.
    """
    serializer_class = WmsSourceSerializer

    def get_queryset(self):
        projectId = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if projectId is None:
            # the project parameter is mandatory to get the data
            return WmsSource.objects.none()
        else:
            user = self.request.user
            if user.is_superuser:
                projectModel = Project.objects.filter(id=projectId).first()
            else:
                projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
            if projectModel:
                # check for wmts data collections
                wmsSources = projectModel.wmssources.all()
                sources = []
                extraSourcesId = 20000001
                for wmsSource in wmsSources:
                    urlTemplate = wmsSource.getcapabilities
                    if "getcapabilities" in urlTemplate.lower() and "service=wms" in urlTemplate.lower():
                        # slashIndex = urlTemplate.index("?")
                        # host = urlTemplate[:slashIndex]
                        # if host.endswith("/"):
                        #     host = host[:-1]

                        wms = WebMapService(urlTemplate)
                        getMethod = wms.getOperationByName('GetMap').methods
                        url = getMethod[0]['url']
                        imageFormats = wms.getOperationByName('GetMap').formatOptions
                        for key, layer in wms.contents.items():
                            version = wms.identification.version
                            layerName = key
                            imgFormat = imageFormats[0]

                            source = WmsSource(
                                label = layerName,
                                version = version,
                                transparent = True,
                                imageformat = imgFormat,
                                getcapabilities = url,
                                layername = key,
                                opacity = 1.0,
                                attribution = wmsSource.attribution,
                                epsg = 3857
                            )
                            sources.append(source)
                            extraSourcesId+=1
                    else:
                        sources.append(wmsSource)
                
                return sources
                # return projectModel.wmssources
            else:
                return WmsSource.objects.none()

class TmsSourceViewSet(ListRetrieveOnlyViewSet):
    """
    API endpoint to get TmsSource info.
    """
    serializer_class = TmsSourceSerializer

    def get_queryset(self):
        projectId = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if projectId is None:
            # the project parameter is mandatory to get the data
            return TmsSource.objects.none()
        else:
            user = self.request.user
            if user.is_superuser:
                projectModel = Project.objects.filter(id=projectId).first()
            else:
                projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
            if projectModel:
                # check for wmts data collections
                tmsSources = projectModel.tmssources.all()
                sources = []
                extraSourcesId = 10000001
                for tmsSource in tmsSources:
                    urlTemplate = tmsSource.urltemplate
                    if "getcapabilities" in urlTemplate.lower() and "service=wmts" in urlTemplate.lower():
                        slashIndex = urlTemplate.index("?")
                        host = urlTemplate[:slashIndex]
                        if host.endswith("/"):
                            host = host[:-1]
                        wmts = WebMapTileService(urlTemplate)
                        for key, layer in wmts.contents.items():
                            version = wmts.identification.version
                            layerName = key
                            style = list(layer.styles)[0]
                            imgFormat = layer.formats[0]
                            
                            tileMatrix = "{z}"
                            tileRow = "{y}"
                            tileCol = "{x}"
                            
                            url = f"{host}/?SERVICE=WMTS&REQUEST=GetTile&VERSION={version}&LAYER={layerName}&STYLE={style}&FORMAT={imgFormat}&TILEMATRIXSET=EPSG:3857&TILEMATRIX={tileMatrix}&TILEROW={tileRow}&TILECOL={tileCol}"
                            
                            source = TmsSource(
                                id = extraSourcesId,
                                label = layerName,
                                urltemplate = url,
                                opacity = 1.0,
                                maxzoom = 21,
                                attribution = tmsSource.attribution,
                            )
                            sources.append(source)
                            extraSourcesId+=1
                    else:
                        sources.append(tmsSource)
                
                return sources
                # return projectModel.tmssources
            else:
                return TmsSource.objects.none()

class UserConfigurationViewSet(StandardPermissionsViewSet):
    """
    API endpoint to get and edit UserConfigurations.
    """
    serializer_class = UserConfigurationSerializer

    def get_queryset(self):
        projectId = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        user = self.request.user
        if projectId is None:
            # the project parameter is mandatory to get the data
            return UserConfiguration.objects.none()
        else:
            user = self.request.user
            if user.is_superuser:
                projectModel = Project.objects.filter(id=projectId).first()
            else:
                projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
            if projectModel:
                return UserConfiguration.objects.filter(project=projectModel, user=user);
            else:
                return UserConfiguration.objects.none()
    
    def put(self, request, *args, **kwargs):
        configList = request.data['configurations']
        user = self.request.user
        projectId = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if projectId is None:
            response = {'message': 'The project parameter is mandatory to update configurations.'}
            return Response(response, status=status.HTTP_400_BAD_REQUEST)

        user = self.request.user
        if user.is_superuser:
            projectModel = Project.objects.filter(id=projectId).first()
        else:
            projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
        if projectModel:
            instances = []
            for config in configList:
                configObj = UserConfiguration.objects.filter(key=config['key'], user=user, project=projectModel).first()
                if configObj:
                    configObj.value = config['value']
                    configObj.save()
                else:
                    configObj = UserConfiguration.objects.create(
                        key=config['key'], 
                        value = config['value'],
                        user=user, 
                        project=projectModel
                    )
                instances.append(configObj)
            serializer = UserConfigurationSerializer(instances, many=True)
            return Response(serializer.data)
        else: 
            response = {'message': 'The current user does not have access to the project.'}
            return Response(response, status=status.HTTP_401_UNAUTHORIZED)

class LastUserPositionViewSet(StandardPermissionsViewSet):
    """
    API endpoint to get and send LastUserPosition.
    """
    serializer_class = LastUserPositionSerializer

    def get_queryset(self):
        projectId = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        user = self.request.user
        if projectId is None:
            # the project parameter is mandatory to get the data
            return LastUserPosition.objects.none()
        else:
            user = self.request.user
            if user.is_superuser:
                projectModel = Project.objects.filter(id=projectId).first()
            else:
                projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
            if projectModel:
                return LastUserPosition.objects.filter(project=projectModel);
            else:
                return LastUserPosition.objects.none()
    
    def put(self, request, *args, **kwargs):
        user = self.request.user
        projectId = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if projectId is None:
            response = {'message': 'The project parameter is mandatory to update configurations.'}
            return Response(response, status=status.HTTP_400_BAD_REQUEST)

        if user.is_superuser:
            projectModel = Project.objects.filter(id=projectId).first()
        else:
            projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
        if projectModel:
            ts = request.data['ts']
            geometry = Point.from_ewkt(request.data['the_geom'])
            
            dt = datetime.now(tz=timezone.utc)
            tsStr = dt.strftime("%Y-%m-%d %H:%M:%S")
            lastUserPosition = LastUserPosition.objects.filter(user=user, project=projectModel).first()
            if lastUserPosition:
                lastUserPosition.ts = ts
                lastUserPosition.uploadTimestamp = tsStr
                lastUserPosition.geometry = geometry
                lastUserPosition.save()
            else:
                lastUserPosition = LastUserPosition.objects.create(
                    the_geom = geometry,
                    ts=ts,
                    uploadts = tsStr,
                    user=user, 
                    project=projectModel
                )
            
            serializer = LastUserPositionSerializer(lastUserPosition)
            return Response(serializer.data)
        else: 
            response = {'message': 'The current user does not have access to the project.'}
            return Response(response, status=status.HTTP_401_UNAUTHORIZED)

