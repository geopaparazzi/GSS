from django.core.management.base import BaseCommand

from formlayers.models_registry import modelsRegistry

class Command(BaseCommand):
    help = 'Make sure every enabled formlayer missing a table gets one, and applies any pending migration. Safe to run any time, even while the server is live (serialized via an advisory lock). Does not touch disabled forms.'

    def add_arguments(self, parser):
        pass

    def handle(self, *args, **options):
        modelsRegistry.checkModelsExistAndMigrate()
