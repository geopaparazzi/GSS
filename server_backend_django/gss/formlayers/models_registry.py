from django.apps import apps
from django.db import models
from django.core.management import call_command
from django.db import connection
from django.db.utils import OperationalError
import logging
from data.models import Form
from django.contrib.gis.db import models as geomodels
from django.contrib.gis.geos import Point, LineString, Polygon
from data.models import DbNamings
from django.contrib.auth.models import User, Group
from datetime import datetime

LOGGER = logging.getLogger(__name__)

class _ModelsRegistry:
    """
    Models registry helper class.
    
    This is meant to be a singleton and to be called only through its
    instance [modelsRegistry].
    """
    def __init__(self):
        self.appName = 'formlayers'

    def _getEnabledForms(self) -> list[Form]:
        """
        Get the list of enabled forms.
        
        Returns
        -------
        list: the list of enabled forms.
        """
        formsQs = Form.objects.filter(enabled=True).all()
        forms = []
        for form in formsQs:
            forms.append(form)
        return forms
    

    def onFormSaved(self) -> None:
        """
        Trigger the model registry to generate the table if necessary and anyways migrate.
        """
        forms = self._getEnabledForms()
        for form in forms:
            name = form.name
            # model = modelsRegistry.getModel(name)
            # if not model:
            # if the model is not in registry, it needs to be created
                
            # first gather the fields needed
            fields = modelsRegistry.fieldsFromDefinition(form.definition)

            # convert them to django fields
            djangoFields = modelsRegistry.djangoFieldsFromFields(fields)
            # add the geometry field
            if form.geometrytype == Form.GEOMETRYTYPES[0][0]:
                geometry = geomodels.PointField(name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=Point())
            elif form.geometrytype == Form.GEOMETRYTYPES[1][0]:
                geometry = geomodels.LineStringField(name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=LineString())
            elif form.geometrytype == Form.GEOMETRYTYPES[2][0]:
                geometry = geomodels.PolygonField(name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=Polygon())
            
            if geometry:
                djangoFields[DbNamings.GEOM] = geometry

            # also add user and timestamp fields if requested
            if form.add_userinfo:
                djangoFields[DbNamings.USER] = models.ForeignKey(User, on_delete=models.DO_NOTHING, null=False, name=DbNamings.USER, default=-1)
                djangoFields[DbNamings.LASTEDIT_USER] = models.ForeignKey(User, on_delete=models.DO_NOTHING, null=False, name=DbNamings.LASTEDIT_USER, default=-1)
            if form.add_timestamp:
                djangoFields[DbNamings.CREATION_TIMESTAMP] = models.DateTimeField(name=DbNamings.CREATION_TIMESTAMP, null=False, default=datetime.now())
                djangoFields[DbNamings.LASTEDIT_TIMESTAMP] = models.DateTimeField(name=DbNamings.LASTEDIT_TIMESTAMP, null=False, default=datetime.now())

            # then create the model and register it (also migrate if necessary)
            modelsRegistry.registerModel(name, djangoFields)
    
    def updateFormsRegistry(self) -> None:
        """
        Trigger the model registry to generate the table if necessary and anyways migrate.
        """

        # remove all models from registry
        apps.all_models[self.appName].clear()
        # then add the upddated list
        forms = Form.objects.all()
        for form in forms:
            name = form.name
                
            # first gather the fields needed
            fields = modelsRegistry.fieldsFromDefinition(form.definition)

            # convert them to django fields
            djangoFields = modelsRegistry.djangoFieldsFromFields(fields)
            # add the geometry field
            if form.geometrytype == Form.GEOMETRYTYPES[0][0]:
                geometry = geomodels.PointField(name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=Point())
            elif form.geometrytype == Form.GEOMETRYTYPES[1][0]:
                geometry = geomodels.LineStringField(name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=LineString())
            elif form.geometrytype == Form.GEOMETRYTYPES[2][0]:
                geometry = geomodels.PolygonField(name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=Polygon())
            
            if geometry:
                djangoFields[DbNamings.GEOM] = geometry

            # also add user and timestamp fields if requested
            if form.add_userinfo:
                djangoFields[DbNamings.USER] = models.ForeignKey(User, on_delete=models.DO_NOTHING, null=False, name=DbNamings.USER, default=-1)
                djangoFields[DbNamings.LASTEDIT_USER] = models.ForeignKey(User, on_delete=models.DO_NOTHING, null=False, name=DbNamings.LASTEDIT_USER, default=-1)
            if form.add_timestamp:
                djangoFields[DbNamings.CREATION_TIMESTAMP] = models.DateTimeField(name=DbNamings.CREATION_TIMESTAMP, null=False, default=datetime.now())
                djangoFields[DbNamings.LASTEDIT_TIMESTAMP] = models.DateTimeField(name=DbNamings.LASTEDIT_TIMESTAMP, null=False, default=datetime.now())

            # then create the model and register it (also migrate if necessary)
            modelsRegistry.registerModel(name, djangoFields)


    def checkModelsExist(self) -> None:
        """
        Check if the dynamic models of the Form table exist and create them if not.
        """
        forms = self._getEnabledForms()
        for form in forms:
            name = form.name
            model = modelsRegistry.getModel(name)
            if not model:
                # if the model is not in registry, it needs to be created
                
                # first gather the fields needed
                fields = modelsRegistry.fieldsFromDefinition(form.definition)

                # convert them to django fields
                djangoFields = modelsRegistry.djangoFieldsFromFields(fields)
                # add the geometry field
                if form.geometrytype == Form.GEOMETRYTYPES[0][0]:
                    geometry = geomodels.PointField(name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=Point())
                elif form.geometrytype == Form.GEOMETRYTYPES[1][0]:
                    geometry = geomodels.LineStringField(name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=LineString())
                elif form.geometrytype == Form.GEOMETRYTYPES[2][0]:
                    geometry = geomodels.PolygonField(name=DbNamings.GEOM, srid=4326, spatial_index=True, null=False, default=Polygon())
                
                if geometry:
                    djangoFields[DbNamings.GEOM] = geometry

                # also add user and timestamp fields if requested
                if form.add_userinfo:
                    djangoFields[DbNamings.USER] = models.ForeignKey(User, on_delete=models.DO_NOTHING, null=False, name=DbNamings.USER, default=-1)
                    djangoFields[DbNamings.LASTEDIT_USER] = models.ForeignKey(User, on_delete=models.DO_NOTHING, null=False, name=DbNamings.LASTEDIT_USER, default=-1)
                if form.add_timestamp:
                    djangoFields[DbNamings.CREATION_TIMESTAMP] = models.DateTimeField(name=DbNamings.CREATION_TIMESTAMP, null=False, default=datetime.now())
                    djangoFields[DbNamings.LASTEDIT_TIMESTAMP] = models.DateTimeField(name=DbNamings.LASTEDIT_TIMESTAMP, null=False, default=datetime.now())

                # then create the model and register it (also migrate if necessary)
                model = modelsRegistry.registerModel(name, djangoFields)

    def getDynamicLayerSpecs(self) -> dict:
        """
        Get the dictionary of dynamic layers names and form definition.
        
        Returns
        -------
        dict: the dictionary of dynamic layers names and form definition.
        """
        self.checkModelsExist()
        forms = self._getEnabledForms()
        formSpecsDict = {}
        for form in forms:
            formSpecsDict[form.name] = form.definition
        return formSpecsDict
    
    def getAllAppModels(self) -> list:
        """
        Get all models currently registered with the app.
        
        Returns
        -------
        list: the list of models registered.
        """
        return [m for m in apps.all_models[self.appName].values()]
    
    def getModel(self, modelName: str) -> models.Model:
        """
        Get a dynamic model by its name.
        
        Parameters
        ----------
        modelName: str
            the name of the model to pick.
        
        Returns
        -------
        Model:
            the model found or None.
        """
        models4app = apps.all_models[self.appName]
        rightModel = models4app.get(modelName)
        if not rightModel:
            # get keys of the dict
            keys = models4app.keys()
            # get the model that has the same name, even if case insensitive
            for key in keys:
                if key.lower() == modelName.lower():
                    rightModel = models4app[key]
                    break
        return rightModel


    def registerModel(self, modelName: str, fields: dict) -> models.Model:
        """
        Register a new model with the app config.
        
        Parameters
        ----------
        modelName: str
            the name of the model to register.
        fields: dict
            the fields of the new model to create.
        forceMigration: bool
            if true, force an execution of the makemigration and migration commands.
        
        Returns
        -------
        Model:
            the created or existing model or None.
        """
        model = self.getModel(modelName)
        if model:
            # if the model exists, delete it to replace it
            self.onFormDelete(modelName)

        # add the module
        fields.update({'__module__':f'{self.appName}.models'})

        model = type(modelName, (models.Model,), fields)
        # You can also set the app_label and db_table attributes if needed
        model._meta.app_label = self.appName
        # DynamicModel._meta.db_table = 'dynamic_table'

        # if it exists but is not in db, do migrations
        # hasModelInDB = _ModelsRegistry.isModelTableCreated(model)
        # if not hasModelInDB:
        #     apps.all_models[self.appName][model.__name__] = model

        # TODO not able to create admin part
        # if admin.site.is_registered(model):
        #     admin.site.unregister(model)        
        # admin.site.register(model)
        # updateUrlPatterns()


        return model
    
    def getRightModelName(self, modelName: str) -> str:
        """
        Get the right model name from the given one. (there might be case issues)
        
        Parameters
        ----------
        modelName: str
            the model name to check.
        
        Returns
        -------
        str:
            the right model name if found.
        """
        models4app = apps.all_models[self.appName]
        rightModelName = models4app.get(modelName)
        if not rightModelName:
            # get keys of the dict
            keys = models4app.keys()
            # get the model that has the same name, even if case insensitive
            for key in keys:
                if key.lower() == modelName.lower():
                    rightModelName = key
                    break
        return rightModelName
    
    def onFormDelete(self, modelName: str):
        rightModelName = self.getRightModelName(modelName)
        if rightModelName:
            del apps.all_models[self.appName][rightModelName]
            print(f"deleted model {rightModelName}")    
            # remove model from app models
            # if modelName in apps.all_models[self.appName]:
            #     del apps.all_models[self.appName][rightModelName]

   
    
    def fieldsFromDefinition(self, formDefinition:list) -> dict:
        """
        Get fields from the [Form] model definition.
        
        Parameters
        ----------
        formDefinition: list
            the form definition that comes from Form. It is a list with a single geopaparazzi form in it.
        
        Returns
        -------
        dict:
            the dictionary containing the {name: type} pairs for the given definition. Here the types 
            are geopaparazzi form types. Use [djangoFieldsFromFields] to get django types.
        """
        fields = {}
        formMap = formDefinition[0]
        forms = formMap['forms']
        for form in forms:
            formItems = form['formitems']
            for formItem in formItems:
                itemKey = formItem.get('key')
                if itemKey:
                    itemType = formItem['type']
                    # itemMandatory = formItem.get('mandatory')
                    fields[itemKey] = itemType
        return fields
    
    def djangoFieldsFromFields(self, fields:dict) -> dict:
        """
        Convert geopaparazzi fields into django model fields.
        
        Parameters
        ----------
        fields: dict
            the geopaparazzi form fields.
        
        Returns
        -------
        dict:
            the django models fields that can be used to generate dynamic models.
        """
        djangoFields = {}

        for k, v in fields.items():
            if v.startswith('string') or v.endswith('string'):
                djangoFields[k] = models.TextField(null=True, blank=True)
            elif v.startswith('int'):
                djangoFields[k] = models.IntegerField(null=True, blank=True)
            elif v.startswith('double'):
                djangoFields[k] = models.FloatField(null=True, blank=True)
            elif v == 'date':
                djangoFields[k] = models.DateField(null=True, blank=True)
            elif v == 'time':
                djangoFields[k] = models.TimeField(null=True, blank=True)
            elif v == 'boolean':
                djangoFields[k] = models.BooleanField(null=True, blank=True)
            elif v == 'pictures' or v == 'sketch':
                djangoFields[k] = models.BinaryField(null=True, blank=True)
            elif v == 'connectedstringcombo' or v == 'autocompletestringcombo' or v == 'autocompleteconnectedstringcombo':
                djangoFields[k] = models.TextField(null=True, blank=True)
            elif v == 'multistringcombo' or v == 'multiintcombo':
                djangoFields[k] = models.TextField(null=True, blank=True)
            else:
                LOGGER.info(f"ignored unknown field type: {v} for {k}")
        
        return djangoFields


    @staticmethod
    def isModelTableCreated(model: models.Model):
        """
        Check if a model has already a created table in the database. 
        If not it needs migrations to be applied.
        
        Parameters
        ----------
        model: Model
            the model to check on.
        
        Returns
        -------
        bool:
            true if the model has a tabled already created in the db.
        """
        try:
            # Use the connection's introspection to check if the table exists
            return model._meta.db_table in connection.introspection.table_names()
        except OperationalError:
            # Handle potential OperationalError (database not reachable)
            return False

modelsRegistry = _ModelsRegistry()
