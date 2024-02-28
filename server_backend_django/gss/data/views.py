from datetime import datetime, timezone
from django.contrib.auth import authenticate
from django.contrib.auth.models import Group, User
from django.views.decorators.csrf import csrf_exempt
from rest_framework import permissions, status, viewsets
from rest_framework.authtoken.models import Token
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.response import Response
from rest_framework.status import (HTTP_200_OK, HTTP_400_BAD_REQUEST,
                                    HTTP_403_FORBIDDEN, HTTP_404_NOT_FOUND)
from django.db.models import Max
import os
from data.models import DbNamings, GpsLog, Image, Note, Project, ProjectData, UserConfiguration, WmsSource, TmsSource, Form
from data.permission import IsCoordinator, IsSuperUser, IsSurveyor, IsWebuser, IsFormbuilder
from data.serializers import (GpslogSerializer, GroupSerializer,
                              ImageSerializer,LastUserPositionSerializer, NoteSerializer,
                              ProjectSerializer, RenderNoteSerializer,ProjectNameSerializer,ProjectDataSerializer,
                              UserSerializer, RenderImageSerializer, WmsSourceSerializer, FormSerializer,FormNameSerializer,
                              TmsSourceSerializer, UserConfigurationSerializer, LastUserPosition)
from owslib.wmts import WebMapTileService
from owslib.wms import WebMapService
from django.contrib.gis.geos import Point
from django.http import HttpRequest
from django.http import HttpResponse
from rest_framework import status
from rest_framework.response import Response
from typing import Optional
import io
import mimetypes
import logging
from django.http import JsonResponse
from django.middleware.csrf import get_token
import json


LOGGER = logging.getLogger(__name__)

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
        LOGGER.warning(f"Unable to login with user '{username}'")
        return Response({'error': 'Invalid Credentials'},
                        status=HTTP_404_NOT_FOUND)
    # now check if the user is in the project
    project = Project.objects.filter(id=projectId).first()
    if not project:
        LOGGER.warning(f"Unable to find project for ID {projectId}")
        return Response({'error': 'Invalid Project Id'},status=HTTP_404_NOT_FOUND)
    if project.hasUser(user):
        token, _ = Token.objects.get_or_create(user=user)
        return Response({'token': token.key, 'id': user.id},
                        status=HTTP_200_OK)
    else:
        LOGGER.warning(f"User '{username}' tried to login to project '{project}', but is not part of it.")
        return Response({'error': f'User is not part of project "{project}"'},status=HTTP_403_FORBIDDEN)

class StandardPermissionsViewSet(viewsets.ModelViewSet):
    """
    Permissions most views need to set.
    """
    def get_permissions(self):
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsCoordinator | IsWebuser | IsSurveyor | IsFormbuilder, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsCoordinator, permissions.IsAuthenticated]
        else:
            permission_classes = [IsSuperUser | IsCoordinator, permissions.IsAuthenticated]
        return [permission() for permission in permission_classes]

class ListonlyPermissionsViewSet(viewsets.ModelViewSet):
    """
    Permissions most views need to set.
    """
    def get_permissions(self):
        if self.action in ["list"]:
            permission_classes = [IsCoordinator | IsWebuser | IsSurveyor | IsFormbuilder, permissions.IsAuthenticated]
        return [permission() for permission in permission_classes]

class SurveyPermissionsViewSet(viewsets.ModelViewSet):
    """
    Permissions that need to set for survey items.

    The surveyor can create data, but all people from the project can read them.
    """
    def get_permissions(self):
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsCoordinator | IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsCoordinator | IsSurveyor, permissions.IsAuthenticated]
        else:
            permission_classes = [IsSuperUser | IsCoordinator, permissions.IsAuthenticated]
        return [permission() for permission in permission_classes]

class FormPermissionsViewSet(viewsets.ModelViewSet):
    """
    Permissions that need to set for form items.

    Only the formbuilder can write forms.
    """
    def get_permissions(self):
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsCoordinator | IsWebuser | IsSurveyor | IsFormbuilder, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsCoordinator | IsFormbuilder, permissions.IsAuthenticated]
        else:
            permission_classes = [IsSuperUser | IsCoordinator | IsFormbuilder, permissions.IsAuthenticated]
        return [permission() for permission in permission_classes]

class WebPermissionsViewSet(viewsets.ModelViewSet):
    """
    Permissions that need to set for survey items.
    """
    def get_permissions(self):
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsCoordinator | IsWebuser, permissions.IsAuthenticated]
        elif self.action in ["create", "update"]:
            permission_classes = [IsCoordinator | IsWebuser, permissions.IsAuthenticated]
        else:
            permission_classes = [IsSuperUser | IsCoordinator, permissions.IsAuthenticated]

        return [permission() for permission in permission_classes]


class StandardListRetrieveOnlyViewSet(StandardPermissionsViewSet):
    def create(self, request):
        response = {'message': 'Create function is not offered in this path.'}
        return Response(response, status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def update(self, request, pk=None):
        response = {'message': 'Update function is not offered in this path.'}
        return Response(response, status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def destroy(self, request, pk=None):
        response = {'message': 'Delete function is not offered in this path.'}
        return Response(response, status=status.HTTP_405_METHOD_NOT_ALLOWED)

class SurveyListRetrieveOnlyViewSet(SurveyPermissionsViewSet):
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

class FormViewSet(FormPermissionsViewSet):
    """
    API endpoint that allows forms to be viewed or edited.
    """
    serializer_class = FormSerializer

    def get_queryset(self):
        projectId = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        pk = self.kwargs.get('pk') # get the form id

        if projectId is None:
            # the project parameter is mandatory to get the data
            return Form.objects.none()
        else:
            user = self.request.user
            if user.is_superuser:
                projectModel = Project.objects.filter(id=projectId).first()
            else:
                projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
            if projectModel:

                if pk:
                    return projectModel.forms.filter(id=pk).all()
                else:
                    filteredForms = []
                    for form in projectModel.forms.all():
                        if form.show_in_projectdata:
                            filteredForms.append(form)

                    return filteredForms
            else:
                return Form.objects.none()
            
    # post support
    def create(self, request):
        projectId = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if projectId is None:
            response = {'message': 'Project id is mandatory.'}
            return Response(response, status=status.HTTP_400_BAD_REQUEST)
        else:
            user = self.request.user
            if user.is_superuser:
                projectModel = Project.objects.filter(id=projectId).first()
            else:
                projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
            if projectModel:
                data = json.loads(request.body)
                # if is list get dict out of it
                map = data[0] if isinstance(data, list) else data
                jsonFormDefinition = json.loads(map['definition'])
                form = Form.objects.create(
                    name = map['name'],
                    definition = [jsonFormDefinition],
                    geometrytype = map['geometrytype'],
                    add_userinfo =  map['add_userinfo'],
                    add_timestamp = map['add_timestamp'],
                    enabled = map['enabled'],
                    show_in_projectdata = map['show_in_projectdata']
                )
                # also add the form to the current project
                projectModel.forms.add(form)
                projectModel.save()

                responseJson = {"id": form.id}
                return Response(responseJson, status=status.HTTP_201_CREATED)
            else:
                response = {'message': f'No Project found for id {projectId}.'}
                return Response(response, status=status.HTTP_400_BAD_REQUEST)
            
    # put support
    def update(self, request, pk=None):
        projectId = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        # formId = self.request.query_params.get(DbNamings.API_PARAM_ID)
        if projectId is None or pk is None:
            response = {'message': 'Project id and form id are mandatory.'}
            return Response(response, status=status.HTTP_400_BAD_REQUEST)
        else:
            user = self.request.user
            if user.is_superuser:
                projectModel = Project.objects.filter(id=projectId).first()
            else:
                projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
            if projectModel:
                # get the form
                form = projectModel.forms.get(id=pk)
                if form:
                    map = json.loads(request.body)
                    if 'name' in map:
                        form.name = map['name']
                    if 'definition' in map:
                        jsonFormDefinition = json.loads(map['definition'])
                        form.definition = [jsonFormDefinition]
                    if 'geometrytype' in map:
                        form.geometrytype = map['geometrytype']
                    if 'add_userinfo' in map:
                        form.add_userinfo = map['add_userinfo']
                    if 'add_timestamp' in map:
                        form.add_timestamp = map['add_timestamp']
                    if 'enabled' in map:
                        form.enabled = map['enabled']
                    if 'show_in_projectdata' in map:
                        form.show_in_projectdata = map['show_in_projectdata']
                        
                    form.save()
                    
                    return Response(status=status.HTTP_200_OK)
                else:
                    response = {'message': 'Form not found.'}
                    return Response(response, status=status.HTTP_400_BAD_REQUEST)
            else:
                response = {'message': f'No Project found for id {projectId}.'}
                return Response(response, status=status.HTTP_400_BAD_REQUEST)

class FormNamesViewSet(ListonlyPermissionsViewSet):
    """
    API endpoint that allows form names to be viewed.
    """
    serializer_class = FormNameSerializer

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
                return projectModel.forms.all()
            else:
                return Form.objects.none()

class ProjectDataViewSet(StandardListRetrieveOnlyViewSet):
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
                    file = pd.file
                    localPath = file.path
                    name = os.path.basename(localPath)

                    url = f"/api/projectdatas/{pd.id}"
                    newPd = ProjectData(
                        id = pd.id,
                        label = name,
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

class RenderNoteViewSet(StandardListRetrieveOnlyViewSet):
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

class NoteViewSet(SurveyPermissionsViewSet):
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

class ImageViewSet(SurveyPermissionsViewSet):
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

class RenderImageViewSet(StandardListRetrieveOnlyViewSet):
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

class RenderSimpleImageViewSet(StandardListRetrieveOnlyViewSet):
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

class GpslogViewSet(SurveyPermissionsViewSet):
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

class WmsSourceViewSet(StandardListRetrieveOnlyViewSet):
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

class TmsSourceViewSet(StandardListRetrieveOnlyViewSet):
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

class UserConfigurationViewSet(WebPermissionsViewSet):
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
                queryset = UserConfiguration.objects.filter(project=projectModel, user=user)

                # check also if the user is of the group frombuilders
                isFormbuilder = False
                if user.groups.filter(name=DbNamings.GROUP_FORMBUILDERS).exists():
                    isFormbuilder = True
                if isFormbuilder:
                    # add an additional dynamically create configuration for the formbuilder: formbuilder=true
                    formbuilderConfig = UserConfiguration(
                        key=DbNamings.USERCONFIG_KEY_FORMBUILDER, 
                        value = "true",
                        user=user, 
                        project=projectModel
                    )
                    queryset = list(queryset)
                    queryset.append(formbuilderConfig)

                return queryset
            else:
                return UserConfiguration.objects.none()
    
    def create(self, request, pk=None):
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

class LastUserPositionViewSet(SurveyPermissionsViewSet):
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
    
    def create(self, request, pk=None):
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

