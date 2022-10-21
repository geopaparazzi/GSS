from django.contrib import admin
from .models import (Note,Project, GpsLog, GpsLogData, Image, ImageData, Device, 
                        UserDeviceAssociation, ImageDataAdmin, WmsSource, ProjectData, 
                        TmsSource, UserConfiguration)
from leaflet.admin import LeafletGeoAdmin
from django_json_widget.widgets import JSONEditorWidget
from django.db import models

# admin.site.register(Note, LeafletGeoAdmin)
admin.site.register(Project)
admin.site.register(GpsLog, LeafletGeoAdmin)
# admin.site.register(GpsLogData, LeafletGeoAdmin)
admin.site.register(Image, LeafletGeoAdmin)
admin.site.register(ImageData, ImageDataAdmin)
admin.site.register(Device)
admin.site.register(UserDeviceAssociation)
admin.site.register(WmsSource)
admin.site.register(TmsSource)
admin.site.register(ProjectData)
admin.site.register(UserConfiguration)

@admin.register(Note)
class NoteAdmin(LeafletGeoAdmin):
    formfield_overrides = {
        models.JSONField: {'widget': JSONEditorWidget(mode='tree')},
    }

admin.site.site_header = 'GSS Admin'
admin.site.site_title = 'GSS Admin'
# admin.site.index_title = "<your_index_title>"

