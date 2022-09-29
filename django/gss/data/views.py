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
                                   HTTP_404_NOT_FOUND)

from data.models import GpsLog, GpsLogData, Image, ImageData, Note, Project
from data.permission import IsCoordinator, IsSuperUser, IsSurveyor, IsWebuser
from data.serializers import (GpslogSerializer, GroupSerializer,
                              ImageSerializer, NoteSerializer,
                              ProjectSerializer, RenderNoteSerializer,
                              UserSerializer, RenderImageSerializer)


@csrf_exempt
@api_view(["POST"])
@permission_classes((AllowAny,))
def login(request):
    username = request.data.get("username")
    password = request.data.get("password")
    if username is None or password is None:
        return Response({'error': 'Please provide both username and password'},
                        status=HTTP_400_BAD_REQUEST)
    user = authenticate(username=username, password=password)
    if not user:
        return Response({'error': 'Invalid Credentials'},
                        status=HTTP_404_NOT_FOUND)
    token, _ = Token.objects.get_or_create(user=user)
    return Response({'token': token.key},
                    status=HTTP_200_OK)


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

class RenderNoteViewSet(ListRetrieveOnlyViewSet):
    
    """
    API endpoint to get notes with minimal info for rendering.
    """
    queryset = Note.objects.all()
    serializer_class = RenderNoteSerializer
    
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
    queryset = Note.objects.all()
    serializer_class = NoteSerializer

    # def get_queryset(self):
    #     mode = self.request.query_params.get('mode')
    #     if mode is not None and mode == 'mini':
    #         queryset = queryset.filter(purchaser__username=username)
    #     queryset = self.queryset
    #     query_set = queryset.filter(user=self.request.user)
    #     return query_set

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
    queryset = Image.objects.all()
    serializer_class = ImageSerializer

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
    queryset = Image.objects.all()
    serializer_class = RenderImageSerializer

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
    queryset = GpsLog.objects.all()
    serializer_class = GpslogSerializer

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

