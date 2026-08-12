from django.db.models.signals import post_save, post_delete
from django.dispatch import receiver
from data.models import Form
from formlayers.models_registry import modelsRegistry
import logging

logger = logging.getLogger(__name__)


### post save and delete signals
@receiver(post_save, sender=Form)
def create_form(sender, instance, **kwargs):
    logger.info("Post Form saved: %s", instance.name)
    modelsRegistry.rebuildAndMigrate()

@receiver(post_delete, sender=Form)
def delete_form(sender, instance, **kwargs):
    logger.info("Post Form deleted: %s", instance.name)
    modelsRegistry.rebuildAndMigrate()
