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
from django.http import HttpResponse, HttpResponseBadRequest

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


def _getModelFormUserProject(request, form_name):
    user = Utilities.getRestAuthenticatedUser(request)
    if not user or not user.is_authenticated:
        # return auth error
        return HttpResponseBadRequest(f"User not authenticated.")

    model = modelsRegistry.getModel(form_name)
    if not model:
        # make sure they are all loaded and registered
        modelsRegistry.checkModelsExist()
        model = modelsRegistry.getModel(form_name)
    if not model:
        return HttpResponseBadRequest(f"No model {form_name} exists.")

    # now check permissions to access that model
    projectId = request.GET.get(DbNamings.API_PARAM_PROJECT)
    if projectId is None:
        # the project parameter is mandatory to get the data
        return HttpResponseBadRequest(f"The parameter 'project' is mandatory.")
    else:
        projectId = int(projectId)
        
        if user.is_superuser:
            projectModel = Project.objects.filter(id=projectId).first()
        else:
            projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
        if projectModel is None:
            # the project parameter is mandatory to get the data
            return HttpResponseBadRequest(f"The project with id {projectId} does not exist.")
    
    # get form by name
    form = Form.objects.filter(name=form_name, project=projectModel).first()
    # check if the project has access to the form
    if form is None:
        return HttpResponseBadRequest(f"The current form is not available in project: {projectModel.name}.")
    return model, form, user, projectModel


@require_GET
def layers(request):
    """
    Get the list of layers for the given project in form of a dictionary 
    with name and form definition.

    Make sure that the user has access to the project and that the form
    is regeistered to the given project.
    """
    modelsDict = {}
    # now check permissions to access that model
    projectId = request.GET.get(DbNamings.API_PARAM_PROJECT)
    if not projectId:
        return HttpResponseBadRequest(f"Missing project.")
    else:
        projectId = int(projectId)
        user = Utilities.getRestAuthenticatedUser(request)
        if not user or not user.is_authenticated:
            # return auth error
            return HttpResponseBadRequest(f"User not authenticated.")
        
        if user.is_superuser:
            projectModel = Project.objects.filter(id=projectId).first()
        else:
            projectModel = Project.objects.filter(id=projectId, groups__user__username=user.username).first()
        if projectModel is None:
            # the project parameter is mandatory to get the data
            return HttpResponseBadRequest(f"No project found for given id.")
            
    dynamicLayers = modelsRegistry.getDynamicLayerSpecs()
    for name, form in dynamicLayers.items():
        form = Form.objects.filter(name=name, project=projectModel).first()
        if form:
            # fields = modelsRegistry.fieldsFromDefinition(form)
            modelsDict[name] = form.definition

    return JsonResponse(modelsDict, json_dumps_params={'indent': 4})


@method_decorator(csrf_exempt, name='dispatch')
class DataListView(View):
    """
    The datalist view allows to list existing data and add new data to the database.
    """

    def get(self, request, form_name, form_id=None):
        """
        GET method that allows for:

        - /formlayers/data/form_name/?project=id
            to get list of all items for the given form/layer and project
        - /formlayers/data/form_name/form_id?project=id
            to get the single item for the given form/layer and id and project
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
                return HttpResponseBadRequest(f"No object found with id {form_id}.")

            dataMap = {}
            item = list(querySet)[0]
            for key, value in formDef.items():
                value = getattr(item, key)
                dataMap[key] = value

            # also add id
            id = getattr(item, "id")
            dataMap["id"] = id

            return JsonResponse(dataMap, safe=False, json_dumps_params={'indent': 4})
        else:
            querySet = model.objects.all()

            dataList = []
            for item in querySet:
                dataMap = {}
                # for each key in formDef, get the attribute with the same name from the 
                # item and add it to the dataMap
                for key, value in formDef.items():
                    value = getattr(item, key)
                    dataMap[key] = value

                # also add id
                id = getattr(item, "id")
                dataMap["id"] = id

                dataList.append(dataMap)

            return JsonResponse(dataList, safe=False, json_dumps_params={'indent': 4})

    def post(self, request, form_name):
        """
        POST method that allows for:
        
        - /formlayers/data/form_name/?project=id
            to create new items for the given form/layer and project. The body is the list 
            of dictionaries with the data to create.
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
        
        createdIds = []
        for data in dataList:
            modelObj = model()
            for key, value in data.items():
                if key == "id":
                    continue
                if not value:
                    continue
                setattr(modelObj, key, value)
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

        data = json.loads(request.body)

        # extract the data from the list/dictionary and create the model object
        if isinstance(data, list):
            dataList = data
        else:
            dataList = [data]

        # check that all items have an id
        for data in dataList:
            if not data.get("id"):
                return HttpResponseBadRequest(f"Missing id in data items: {data}")

        updatedIds = []
        for data in dataList:
            id = data["id"]
            modelObj = model.objects.get(pk=id)
            for key, value in data.items():
                if key == "id":
                    continue
                setattr(modelObj, key, value)
            modelObj.save()
            updatedIds.append(str(modelObj.id))

        return JsonResponse({'message': f'Updated {len(updatedIds)} {form_name} objects (ids {", ".join(updatedIds)})'}, status=201)
        

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

        return JsonResponse({'message': f'Deleted {len(deletedIds)} {form_name} objects (ids {", ".join(deletedIds)})'}, status=201)
                
