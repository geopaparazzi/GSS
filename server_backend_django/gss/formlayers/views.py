from django.shortcuts import render

# Create your views here.
from django.db import models
from .models_registry import modelsRegistry
from data.models import Form
from gss.utils import Utilities
from django.http import JsonResponse
from django.views import View
import json
# Create your views here.
from django.http import HttpResponse

from django.contrib.auth.decorators import login_required
from django.views.decorators.http import require_GET
from django.views import View
from django.contrib.auth.mixins import LoginRequiredMixin
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_protect, csrf_exempt
from django.utils.decorators import method_decorator
from data.models import DbNamings, Project
from rest_framework.authentication import TokenAuthentication, SessionAuthentication
from rest_framework.permissions import IsAuthenticated
from rest_framework.decorators import authentication_classes, permission_classes
from hydrologis_utils.geojson_utils import HyGeojsonUtils
from hydrologis_utils.geom_utils import HyGeomUtils
import datetime
import base64


def _getModelFormUserProject(request, form_name):
    user = Utilities.getRestAuthenticatedUser(request)
    if not user or not user.is_authenticated:
        # return auth error
        return Utilities.toHttpResponseWithError(f"User not authenticated.")

    model = modelsRegistry.getModel(form_name)
    if not model:
        # make sure they are all loaded and registered
        modelsRegistry.checkModelsExist()
        model = modelsRegistry.getModel(form_name)
    if not model:
        return Utilities.toHttpResponseWithError(f"No model {form_name} exists.")

    # now check permissions to access that model
    projectId = request.GET.get(DbNamings.API_PARAM_PROJECT)
    if projectId is None:
        # the project parameter is mandatory to get the data
        return Utilities.toHttpResponseWithError(f"The parameter 'project' is mandatory.")
    else:
        projectId = int(projectId)
        
        if user.is_superuser:
            projectModel = Project.objects.filter(id=projectId).first()
        else:
            projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
        if projectModel is None:
            # the project parameter is mandatory to get the data
            return Utilities.toHttpResponseWithError(f"The project with id {projectId} does not exist.")
    
    # get form by name
    form = Form.objects.filter(name=form_name, project=projectModel).first()
    # check if the project has access to the form
    if form is None:
        return Utilities.toHttpResponseWithError(f"The current form is not available in project: {projectModel.name}.")
    return model, form, user, projectModel


@require_GET
def layers(request):
    """
    Get the list of layers for the given project in form of a dictionary 
    with name and form definition.

    Make sure that the user has access to the project and that the form
    is regeistered to the given project.
    """
    modelsList = []
    # now check permissions to access that model
    projectId = request.GET.get(DbNamings.API_PARAM_PROJECT)
    if not projectId:
        return Utilities.toHttpResponseWithError(f"Missing project.")
    else:
        projectId = int(projectId)
        user = Utilities.getRestAuthenticatedUser(request)
        if not user or not user.is_authenticated:
            # return auth error
            return Utilities.toHttpResponseWithError(f"User not authenticated.")
        
        if user.is_superuser:
            projectModel = Project.objects.filter(id=projectId).first()
        else:
            projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
        if projectModel is None:
            # the project parameter is mandatory to get the data
            return Utilities.toHttpResponseWithError(f"No project found for given id.")
            
    dynamicLayers = modelsRegistry.getDynamicLayerSpecs()
    for name, form in dynamicLayers.items():
        form = Form.objects.filter(name=name, project=projectModel).first()
        if form:
            modelsDict = {}
            modelsDict["name"] = name
            modelsDict["geometrytype"] = form.geometrytype
            # fields = modelsRegistry.fieldsFromDefinition(form)
            modelsDict["form"] = form.definition
            modelsList.append(modelsDict)

    return JsonResponse(modelsList, safe=False, json_dumps_params={'indent': 4})


@method_decorator(csrf_exempt, name='dispatch')
class DataListView(View):
    """
    The datalist view allows to list existing data and add new data to the database.
    """

    def _checkValue(self, modelObj, key, value):
        if not value or not key:
            return None
        if not hasattr(modelObj, key):
            return None
        field = modelObj._meta.get_field(key)
        fieldType = field.get_internal_type()
        if len(value) == 0:
            return None
        # if field is a DateTimeField and value is a string, make sure the string is properly formatted
        if fieldType == "DateField" and isinstance(value, str):
            value = datetime.datetime.strptime(value, "%Y-%m-%d")
        # if field is a BinaryField and value is a string, assume it is a base64 image and convert it to bytes
        elif fieldType == "BinaryField" and isinstance(value, str):
            # base 64 conversion
            value = base64.b64decode(value)
        # if field is a TimeField and value is a string, make sure the string is properly formatted
        elif fieldType == "TimeField" and isinstance(value, str):
            value = datetime.datetime.strptime(value, "%H:%M:%S")
        
        return value
    

    def get(self, request, form_name, form_id=None):
        """
        GET method that allows for:

        - /formlayers/data/form_name/?project=id
            to get a geojson featurecollection of all items for the given form/layer and project
        - /formlayers/data/form_name/form_id?project=id
            to get the single geojson feature for the given form/layer and id and project
        """
        modelFormUserProject = _getModelFormUserProject(request, form_name)
        if isinstance(modelFormUserProject, HttpResponse):
            return modelFormUserProject
        else:
            model, form, user, projectModel = modelFormUserProject

        # passed here means we have access
        formDefinition = form.definition
        formDef = modelsRegistry.fieldsFromDefinition(formDefinition)
        if form_id:
            # get the object with the given id
            querySet = model.objects.filter(id=form_id).all()
            if querySet.count() != 1:
                return Utilities.toHttpResponseWithError(f"No object found with id {form_id}.")

            dataMap = {}
            item = list(querySet)[0]
            for key, value in formDef.items():
                value = getattr(item, key)
                if key != "id" and key != DbNamings.GEOM:
                    dataMap[key] = value

            # also add id
            id = getattr(item, "id")
            geom = getattr(item, DbNamings.GEOM)
            shapelyGeom = HyGeomUtils.fromWkt(geom.wkt)
            if shapelyGeom.is_empty:
                return Utilities.toHttpResponseWithError(f"Geometry is empty for object with id {form_id}.")

            feature = HyGeojsonUtils.mapToFeature(properties=dataMap, geometry=shapelyGeom, id=id)
            geojsonString = HyGeojsonUtils.featureToString(feature)

            return HttpResponse(geojsonString, content_type="application/json")
        else:
            querySet = model.objects.all()

            featuresList = []
            for item in querySet:
                dataMap = {}
                # for each key in formDef, get the attribute with the same name from the 
                # item and add it to the dataMap
                for key, value in formDef.items():
                    value = getattr(item, key)
                    if key != "id" and key != DbNamings.GEOM:
                        # check if value is a datetime.time object
                        if isinstance(value, datetime.time):
                            value = value.strftime("%H:%M:%S")
                        # check if value is a datetime.date object
                        elif isinstance(value, datetime.date):
                            value = value.strftime("%Y-%m-%d")
                        # check if value is a datetime.datetime object
                        elif isinstance(value, datetime.datetime):
                            value = value.strftime("%Y-%m-%d %H:%M:%S")
                        else:
                            if value:
                                field = model._meta.get_field(key)
                                fieldType = field.get_internal_type()
                                if fieldType == "BinaryField":
                                    # base 64 conversion
                                    value = base64.b64encode(value).decode("utf-8")

                        dataMap[key] = value

                # also add id
                id = getattr(item, "id")
                geom = getattr(item, DbNamings.GEOM)
                shapelyGeom = HyGeomUtils.fromWkt(geom.wkt)
                if shapelyGeom.is_empty:
                    return Utilities.toHttpResponseWithError(f"Geometry is empty for object with id {form_id}.")

                feature = HyGeojsonUtils.mapToFeature(properties=dataMap, geometry=shapelyGeom, id=id)

                featuresList.append(feature)

            geojsonString = HyGeojsonUtils.featuresListToString(featuresList)

            return HttpResponse(geojsonString, content_type="application/json")

    def post(self, request, form_name):
        """
        POST method that allows for:
        
        - /formlayers/data/form_name/?project=id
            to create new items for the given form/layer and project. The body is a geojson 
            featurecollection.
        """
        
        modelUserProject = _getModelFormUserProject(request, form_name)
        if isinstance(modelUserProject, HttpResponse):
            return modelUserProject
        else:
            model, form, user, projectModel = modelUserProject
        
        featureCollection = HyGeojsonUtils.stringToFeatureCollection(request.body)

        addUser = form.add_userinfo
        addTimestamp = form.add_timestamp
        
        createdIds = []
        for feature in featureCollection.features:
            modelObj = model()
            data = feature.properties
            for key, value in data.items():
                if key == "id":
                    continue
                value = self._checkValue(modelObj, key, value)
                if not value:
                    continue
                setattr(modelObj, key, value)
                
            geom = HyGeomUtils.fromGeoJson(str(feature.geometry))
            setattr(modelObj, DbNamings.GEOM, geom.wkt)

            # when a new one is created, add user and timestamp
            # and use the same user and timestamp for lastedit
            if addUser:
                setattr(modelObj, DbNamings.USER, user)
                setattr(modelObj, DbNamings.LASTEDIT_USER, user)
            if addTimestamp:
                setattr(modelObj, DbNamings.CREATION_TIMESTAMP, datetime.datetime.now())
                setattr(modelObj, DbNamings.LASTEDIT_TIMESTAMP, datetime.datetime.now())
            
            modelObj.save()

            # after saving the id is available
            createdIds.append(str(modelObj.id))
        
        return JsonResponse({'message': f'Created {len(createdIds)} {form_name} objects (ids {", ".join(createdIds)})'}, status=201)
    
    def put(self, request, form_name):
        """
        PUT method that allows for:

        - /formlayers/data/form_name/?project=id
            to update a list or single items for the given form/layer and project. 
            The body is the list of dictionaries or a single dictionary with the data to update.        
        """
        modelUserProject = _getModelFormUserProject(request, form_name)
        if isinstance(modelUserProject, HttpResponse):
            return modelUserProject
        else:
            model, form, user, projectModel = modelUserProject

        addUser = form.add_userinfo
        addTimestamp = form.add_timestamp

        featureCollection = HyGeojsonUtils.stringToFeatureCollection(request.body)
        
        updatedIds = []
        for feature in featureCollection.features:
            modelObj = model()
            data = feature.properties
            id = feature.get('id', None)
            if id == None:
                return Utilities.toHttpResponseWithError(f"Missing id in features.")
            modelObj = model.objects.get(pk=id)
            for key, value in data.items():
                if key == "id":
                    continue
                
                value = self._checkValue(modelObj, key, value)
                if not value:
                    continue
                setattr(modelObj, key, value)
            
            
            geom = HyGeomUtils.fromGeoJson(str(feature.geometry))
            setattr(modelObj, DbNamings.GEOM, geom.wkt)

            if addUser:
                setattr(modelObj, DbNamings.LASTEDIT_USER, user)
            if addTimestamp:
                setattr(modelObj, DbNamings.LASTEDIT_TIMESTAMP, datetime.datetime.now())
            
            modelObj.save()
            updatedIds.append(str(modelObj.id))

        return JsonResponse({'message': f'Updated {len(updatedIds)} {form_name} objects (ids {", ".join(updatedIds)})'}, status=200)
        

    def delete(self, request, form_name):
        """
        DELETE method that allows for:

        - /formlayers/data/form_name/?project=id
            to delete a list of items defined by the ids of the payload for the given form/layer and project.

            Format is:
            [
                {
                    "id": 1
                },
                {
                    "id": 2
                }
            ]
        """
        modelUserProject = _getModelFormUserProject(request, form_name)
        if isinstance(modelUserProject, HttpResponse):
            return modelUserProject
        else:
            model, form, user, projectModel = modelUserProject

        data = json.loads(request.body)

        # extract the data from the list/dictionary and create the model object
        if isinstance(data, list):
            dataList = data
        else:
            dataList = [data]

        # check that all items have an id
        deletedIds = []
        for data in dataList:
            id = data.get("id")
            if id:
                modelObj = model.objects.get(pk=id)
                if modelObj:
                    deletedIds.append(str(modelObj.id))
                    modelObj.delete()

        return JsonResponse({'message': f'Deleted {len(deletedIds)} {form_name} objects (ids {", ".join(deletedIds)})'}, status=200)
                
