from django.db.models.signals import post_save, post_delete
from django.dispatch import receiver
from data.models import Form
from formlayers.models_registry import modelsRegistry



### post save and delete signals
@receiver(post_save, sender=Form)
def create_form(sender, instance, **kwargs):
    # trigger model registry to generate the table
    modelsRegistry.checkModelsExist()

@receiver(post_delete, sender=Form)
def delete_form(sender, instance, **kwargs):
    # trigger model registry to delete the table
    modelsRegistry.onFormDelete(instance.name)