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

from data.models import DbNamings, GpsLog, GpsLogData, Image, ImageData, Note, Project
from data.permission import IsCoordinator, IsSuperUser, IsSurveyor, IsWebuser
from data.serializers import (GpslogSerializer, GroupSerializer,
                              ImageSerializer, NoteSerializer,
                              ProjectSerializer, RenderNoteSerializer,ProjectNameSerializer,
                              UserSerializer, RenderImageSerializer)


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
            permission_classes = [IsSuperUser, permissions.IsAuthenticated]
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
            permission_classes = [IsSuperUser, permissions.IsAuthenticated]
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
            permission_classes = [IsSuperUser, permissions.IsAuthenticated]
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
            queryset = Note.objects.filter(project__name=project)
            return queryset
        
    def get_permissions(self):
        """
        Instantiates and returns the list of permissions that this view requires.
        """
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsCoordinator | IsSurveyor, permissions.IsAuthenticated]
        else:
            permission_classes = [IsSuperUser, permissions.IsAuthenticated]
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
            queryset = Note.objects.filter(project__name=project)
            return queryset

    def get_permissions(self):
        """
        Instantiates and returns the list of permissions that this view requires.
        """
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsCoordinator | IsSurveyor, permissions.IsAuthenticated]
        else:
            permission_classes = [IsSuperUser, permissions.IsAuthenticated]
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
            queryset = Image.objects.filter(project__name=project)
            return queryset

    def get_permissions(self):
        """
        Instantiates and returns the list of permissions that this view requires.
        """
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsCoordinator | IsSurveyor, permissions.IsAuthenticated]
        else:
            permission_classes = [IsSuperUser, permissions.IsAuthenticated]
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
            queryset = Image.objects.filter(project__name=project)
            return queryset

    def get_permissions(self):
        """
        Instantiates and returns the list of permissions that this view requires.
        """
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsCoordinator | IsSurveyor, permissions.IsAuthenticated]
        else:
            permission_classes = [IsSuperUser, permissions.IsAuthenticated]
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
            queryset = GpsLog.objects.filter(project__name=project)
            return queryset

    def get_permissions(self):
        """
        Instantiates and returns the list of permissions that this view requires.
        """
        if self.action in ["list", "retrieve"]:
            permission_classes = [IsWebuser | IsSurveyor, permissions.IsAuthenticated]
        elif self.action == "create":
            permission_classes = [IsCoordinator | IsSurveyor, permissions.IsAuthenticated]
        else:
            permission_classes = [IsSuperUser, permissions.IsAuthenticated]
        return [permission() for permission in permission_classes]

