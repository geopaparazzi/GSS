"""gss URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/4.0/topics/http/urls/
Examples:
Function viewsself.the_geom
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import include, path
from rest_framework import routers
import data.views

from django.conf.urls import  include
from django.views.generic import RedirectView

router = routers.DefaultRouter()
router.register(r'users', data.views.UserViewSet)
router.register(r'groups', data.views.GroupViewSet)
router.register(r'projects', data.views.ProjectViewSet)
router.register(r'projectnames', data.views.ProjectNameViewSet, 'projectnames')
router.register(r'rendernotes', data.views.RenderNoteViewSet, 'rendernotes')
router.register(r'notes', data.views.NoteViewSet, 'notes')
router.register(r'gpslogs', data.views.GpslogViewSet, 'gpslogs')
router.register(r'images', data.views.ImageViewSet, 'images')
router.register(r'renderimages', data.views.RenderImageViewSet, 'renderimages')
router.register(r'rendersimpleimages', data.views.RenderSimpleImageViewSet, 'rendersimpleimages')
router.register(r'wmssources', data.views.WmsSourceViewSet, 'wmssources')
router.register(r'tmssources', data.views.TmsSourceViewSet, 'tmssources')
router.register(r'userconfigurations', data.views.UserConfigurationViewSet, 'userconfigurations')
router.register(r'lastuserpositions', data.views.LastUserPositionViewSet, 'lastuserpositions')
router.register(r'projectdatas', data.views.ProjectDataViewSet, 'projectdatas')
router.register(r'forms', data.views.FormViewSet, 'forms')


urlpatterns = [
    path('', RedirectView.as_view(url='/static/index.html'), name='index'),
    path('admin/', admin.site.urls),
    path('formlayers/', include('formlayers.urls')),
    path('api/', include(router.urls)),
    path('api-auth/', include('rest_framework.urls')),
    path('api/login/', data.views.login),

    path('accounts/login/', data.views.login),
]
