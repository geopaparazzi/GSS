from django.shortcuts import render

# Create your views here.
from django.db import models
from .models_registry import modelsRegistry
from data.models import Form
from django.http import JsonResponse
from django.views import View
import json
# Create your views here.
from django.http import HttpResponse, HttpResponseBadRequest

def layers(request):

    modelsDict = {}
    forms = Form.objects.all()
    for form in forms:
        name = form.name
        model = modelsRegistry.getModel(name)
        if not model:
            # if the model is not in registry, it needs to be created
            
            # first gather the fields needed
            fields = modelsRegistry.fieldsFromDefinition(form.definition)

            # convert them to django fields
            djangoFields = modelsRegistry.djangoFieldsFromFields(fields)

            # then create the model and register it (also migrate if necessary)
            model = modelsRegistry.registerModel(name, djangoFields)
            modelsDict[name] = {
                'creation':'created new',
                'fields': fields
            }
        else:
            fields = modelsRegistry.fieldsFromDefinition(form.definition)
            modelsDict[name] = {
                'creation':'already existing',
                'fields': fields
            }

    return JsonResponse(modelsDict, json_dumps_params={'indent': 4})




class DataListView(View):
    def get(self, request, form_name):
        model = modelsRegistry.getModel(form_name)
        if not model:
            # make sure they are all loaded
            layers(request)
            model = modelsRegistry.getModel(form_name)

        if not model:
            return HttpResponseBadRequest(f"No model {form_name} exists.")
        
        querySet = model.objects.all()
        serializedItems = [ item for item in querySet]

        return JsonResponse(serializedItems, safe=False)

    def post(self, request, form_name):
        model = modelsRegistry.getModel(form_name)
        if not model:
            # make sure they are all loaded
            layers(request)
            model = modelsRegistry.getModel(form_name)

        if not model:
            return HttpResponseBadRequest(f"No model {form_name} exists.")
        
        data = json.loads(request.body)
        # ! TODO 
        
        return JsonResponse({'message': 'Item created'}, status=201)

class DataDetailView(View):
    def get(self, request, item_id):
        item = {'id': item_id, 'name': 'Item {}'.format(item_id)}
        return JsonResponse(item)

    def put(self, request, item_id):
        data = json.loads(request.body)
        # Process and update the item
        return JsonResponse({'message': 'Item updated'})

    def delete(self, request, item_id):
        # Delete the item
        return JsonResponse({'message': 'Item deleted'})