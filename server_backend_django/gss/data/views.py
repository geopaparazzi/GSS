from datetime import datetime, timezone
from django.contrib.auth import authenticate
from django.contrib.auth.models import Group, User
from django.views.decorators.csrf import csrf_exempt
from requests import Response
from rest_framework import permissions, status, viewsets
from rest_framework.authtoken.models import Token
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework.status import (HTTP_200_OK, HTTP_400_BAD_REQUEST,
                                    HTTP_403_FORBIDDEN, HTTP_404_NOT_FOUND)
from django.db.models import Max
from data.models import DbNamings, GpsLog, GpsLogData, Image, ImageData, Note, Project, UserConfiguration, WmsSource, TmsSource
from data.permission import IsCoordinator, IsSuperUser, IsSurveyor, IsWebuser
from data.serializers import (GpslogSerializer, GroupSerializer,
                              ImageSerializer, LastUserPositionSerializer, NoteSerializer,
                              ProjectSerializer, RenderNoteSerializer,ProjectNameSerializer,
                              UserSerializer, RenderImageSerializer, WmsSourceSerializer, 
                              TmsSourceSerializer, UserConfigurationSerializer, LastUserPosition)


@csrf_exempt
@api_view(["POST"])
@permission_classes((AllowAny,))
def login(request):
    username = request.data.get("username")
    password = request.data.get("password")
    projectName = request.data.get("project")
    if username is None or password is None or projectName is None:
        return Response({'error': 'Please provide username, password and project of choice.'},
                        status=HTTP_400_BAD_REQUEST)
    user = authenticate(username=username, password=password)
    if not user:
        return Response({'error': 'Invalid Credentials'},
                        status=HTTP_404_NOT_FOUND)
    # now check if the user is in the project
    project = Project.objects.filter(name=projectName).first()
    if not project:
        return Response({'error': 'Invalid Project Name'},status=HTTP_404_NOT_FOUND)
    if project.hasUser(user):
        token, _ = Token.objects.get_or_create(user=user)
        return Response({'token': token.key},
                        status=HTTP_200_OK)
    else:
        return Response({'error': f'User is not part of project "{project}"'},status=HTTP_403_FORBIDDEN)



class ListRetrieveOnlyViewSet(viewsets.ModelViewSet):

    def create(self, request):
        response = {'message': 'Create function is not offered in this path.'}
        return Response(response, status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def update(self, request, pk=None):
        response = {'message': 'Update function is not offered in this path.'}
        return Response(response, status=status.HTTP_405_METHOD_NOT_ALLOWED)

    def destroy(self, request, pk=None):
        response = {'message': 'Delete function is not offered in this path.'}
        return Response(response, status=status.HTTP_405_METHOD_NOT_ALLOWED)

class UserViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows users to be viewed or edited.
    """
    queryset = User.objects.all().order_by('-date_joined')
    serializer_class = UserSerializer

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


class GroupViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows groups to be viewed or edited.
    """
    queryset = Group.objects.all()
    serializer_class = GroupSerializer
    
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


class ProjectViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows projects to be viewed or edited.
    """
    queryset = Project.objects.all()
    serializer_class = ProjectSerializer

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

class ProjectNameViewSet(ListRetrieveOnlyViewSet):
    """
    API endpoint that allows projects to be viewed or edited.
    """
    queryset = Project.objects.all()
    serializer_class = ProjectNameSerializer
    permission_classes = [AllowAny]

class RenderNoteViewSet(ListRetrieveOnlyViewSet):
    
    """
    API endpoint to get notes with minimal info for rendering.
    """
    serializer_class = RenderNoteSerializer

    def get_queryset(self):
        project = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if project is None:
            # the project parameter is mandatory to get the data
            return Note.objects.none()
        else:
            user = self.request.user
            projectModel = Project.objects.filter(name=project, groups__user__username=user.username).first()
            if projectModel:
                aggregation = Note.objects.filter(project__name=project).values(DbNamings.GEOM).annotate(id=Max(DbNamings.NOTE_ID)).values_list(DbNamings.NOTE_ID),
                queryset = Note.objects.filter(id__in=aggregation)
                return queryset
            else:
                return Note.objects.none()

        
    def get_permissions(self):
        """
        Instantiates and returns the list of permissions that this view requires.
        """
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsCoordinator | IsSurveyor, permissions.IsAuthenticated]
        else:
            permission_classes = [IsSuperUser | IsCoordinator, permissions.IsAuthenticated]
        return [permission() for permission in permission_classes]

class NoteViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows projects to be viewed or edited.
    """
    serializer_class = NoteSerializer

    def get_queryset(self):
        project = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if project is None:
            # the project parameter is mandatory to get the data
            return Note.objects.none()
        else:
            user = self.request.user
            projectModel = Project.objects.filter(name=project, groups__user__username=user.username).first()
            if projectModel:
                # aggregation = Note.objects.filter(project__name=project).values(DbNamings.GEOM).annotate(id=Max(DbNamings.NOTE_ID)).values_list(DbNamings.NOTE_ID),
                # queryset = Note.objects.filter(id__in=aggregation)
                queryset = Note.objects.filter(project__name=project)
                return queryset
            else:
                return Note.objects.none()

    def get_permissions(self):
        """
        Instantiates and returns the list of permissions that this view requires.
        """
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsCoordinator | IsSurveyor, permissions.IsAuthenticated]
        else:
            permission_classes = [IsSuperUser | IsCoordinator, permissions.IsAuthenticated]
        return [permission() for permission in permission_classes]

class ImageViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows images to be viewed or edited.
    """
    serializer_class = ImageSerializer

    def get_queryset(self):
        project = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if project is None:
            # the project parameter is mandatory to get the data
            return Image.objects.none()
        else:
            user = self.request.user
            projectModel = Project.objects.filter(name=project, groups__user__username=user.username).first()
            if projectModel:
                queryset = Image.objects.filter(project__name=project)
                return queryset
            else:
                return Image.objects.none()


    def get_permissions(self):
        """
        Instantiates and returns the list of permissions that this view requires.
        """
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsCoordinator | IsSurveyor, permissions.IsAuthenticated]
        else:
            permission_classes = [IsSuperUser | IsCoordinator, permissions.IsAuthenticated]
        return [permission() for permission in permission_classes]

class RenderImageViewSet(ListRetrieveOnlyViewSet):
    """
    API endpoint to get images with minimal info for rendering.
    """
    serializer_class = RenderImageSerializer

    def get_queryset(self):
        project = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if project is None:
            # the project parameter is mandatory to get the data
            return Image.objects.none()
        else:
            user = self.request.user
            projectModel = Project.objects.filter(name=project, groups__user__username=user.username).first()
            if projectModel:
                queryset = Image.objects.filter(project__name=project)
                return queryset
            else:
                return Image.objects.none()


    def get_permissions(self):
        """
        Instantiates and returns the list of permissions that this view requires.
        """
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsCoordinator | IsSurveyor, permissions.IsAuthenticated]
        else:
            permission_classes = [IsSuperUser | IsCoordinator, permissions.IsAuthenticated]
        return [permission() for permission in permission_classes]

class GpslogViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows gpslogs to be viewed or edited.
    """
    serializer_class = GpslogSerializer

    def get_queryset(self):
        project = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if project is None:
            # the project parameter is mandatory to get the data
            return GpsLog.objects.none()
        else:
            user = self.request.user
            projectModel = Project.objects.filter(name=project, groups__user__username=user.username).first()
            if projectModel:
                queryset = GpsLog.objects.filter(project__name=project)
                return queryset
            else:
                return GpsLog.objects.none()


    def get_permissions(self):
        """
        Instantiates and returns the list of permissions that this view requires.
        """
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsCoordinator | IsSurveyor, permissions.IsAuthenticated]
        else:
            permission_classes = [IsSuperUser | IsCoordinator, permissions.IsAuthenticated]
        return [permission() for permission in permission_classes]

class WmsSourceViewSet(ListRetrieveOnlyViewSet):
    """
    API endpoint to get WmsSource info.
    """
    serializer_class = WmsSourceSerializer

    def get_queryset(self):
        project = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if project is None:
            # the project parameter is mandatory to get the data
            return WmsSource.objects.none()
        else:
            user = self.request.user
            projectModel = Project.objects.filter(name=project, groups__user__username=user.username).first()
            if projectModel:
                return projectModel.wmssources
            else:
                return TmsSource.objects.none()

    def get_permissions(self):
        """
        Instantiates and returns the list of permissions that this view requires.
        """
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsCoordinator | IsSurveyor, permissions.IsAuthenticated]
        else:
            permission_classes = [IsSuperUser | IsCoordinator, permissions.IsAuthenticated]
        return [permission() for permission in permission_classes]

class TmsSourceViewSet(ListRetrieveOnlyViewSet):
    """
    API endpoint to get TmsSource info.
    """
    serializer_class = TmsSourceSerializer

    def get_queryset(self):
        project = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if project is None:
            # the project parameter is mandatory to get the data
            return TmsSource.objects.none()
        else:
            user = self.request.user
            projectModel = Project.objects.filter(name=project, groups__user__username=user.username).first()
            if projectModel:
                return projectModel.tmssources
            else:
                return TmsSource.objects.none()
            

    def get_permissions(self):
        """
        Instantiates and returns the list of permissions that this view requires.
        """
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsCoordinator | IsSurveyor, permissions.IsAuthenticated]
        else:
            permission_classes = [IsSuperUser | IsCoordinator, permissions.IsAuthenticated]
        return [permission() for permission in permission_classes]

class UserConfigurationViewSet(viewsets.ModelViewSet):
    """
    API endpoint to get and edit UserConfigurations.
    """
    serializer_class = UserConfigurationSerializer

    def get_queryset(self):
        project = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        user = self.request.user
        if project is None:
            # the project parameter is mandatory to get the data
            return UserConfiguration.objects.none()
        else:
            user = self.request.user
            projectModel = Project.objects.filter(name=project, groups__user__username=user.username).first()
            if projectModel:
                return UserConfiguration.objects.filter(project=projectModel, user=user);
            else:
                return UserConfiguration.objects.none()
    
    def put(self, request, *args, **kwargs):
        configList = request.data['configurations']
        user = self.request.user
        project = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if project is None:
            response = {'message': 'The project parameter is mandatory to update configurations.'}
            return Response(response, status=status.HTTP_400_BAD_REQUEST)

        user = self.request.user
        projectModel = Project.objects.filter(name=project, groups__user__username=user.username).first()
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

    def get_permissions(self):
        """
        Instantiates and returns the list of permissions that this view requires.
        """
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        else:
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        return [permission() for permission in permission_classes]

class LastUserPositionViewSet(viewsets.ModelViewSet):
    """
    API endpoint to get and send LastUserPosition.
    """
    serializer_class = LastUserPositionSerializer

    def get_queryset(self):
        project = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        user = self.request.user
        if project is None:
            # the project parameter is mandatory to get the data
            return LastUserPosition.objects.none()
        else:
            user = self.request.user
            projectModel = Project.objects.filter(name=project, groups__user__username=user.username).first()
            if projectModel:
                return LastUserPosition.objects.filter(project=projectModel);
            else:
                return LastUserPosition.objects.none()
    
    def put(self, request, *args, **kwargs):
        user = self.request.user
        project = self.request.query_params.get(DbNamings.API_PARAM_PROJECT)
        if project is None:
            response = {'message': 'The project parameter is mandatory to update configurations.'}
            return Response(response, status=status.HTTP_400_BAD_REQUEST)

        projectModel = Project.objects.filter(name=project, groups__user__username=user.username).first()
        if projectModel:
            ts = request.data['ts']
            geometry = request.data['the_geom']
            
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

    def get_permissions(self):
        """
        Instantiates and returns the list of permissions that this view requires.
        """
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        else:
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        return [permission() for permission in permission_classes]

