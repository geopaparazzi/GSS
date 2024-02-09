from django.db.models.signals import post_save, post_delete
from django.dispatch import receiver
from data.models import Form
from formlayers.models_registry import modelsRegistry
import logging
from django.core.management import call_command

logger = logging.getLogger(__name__)



### post save and delete signals
@receiver(post_save, sender=Form)
def create_form(sender, instance, **kwargs):
    logger.info("Post Form saved: %s", instance.name)
    # trigger model registry to generate the table
    modelsRegistry.updateFormsRegistry()

    call_command('makemigrations', interactive=False)
    call_command('migrate', interactive=False)

@receiver(post_delete, sender=Form)
def delete_form(sender, instance, **kwargs):
    logger.info("Post Form deleted: %s", instance.name)
    # trigger model registry to delete the table
    modelsRegistry.updateFormsRegistry()

    call_command('makemigrations', interactive=False)
    call_command('migrate', interactive=False)