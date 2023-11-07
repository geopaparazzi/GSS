from django.core.management.base import BaseCommand

from django.conf import settings
from django.core.management import call_command
from formlayers.models_registry import modelsRegistry

from django.db import connection

class Command(BaseCommand):
    help = 'Delete GSS test database. This command works only if in debug mode and is meant for devel purposes.'

    def add_arguments(self, parser):
        pass

    def handle(self, *args, **options):

        # first check that the dynamic layer models are created 
        # to avoid deleting of existing dynamic models models
        modelsRegistry.checkModelsExist()
        
        call_command('migrate')
        



    
