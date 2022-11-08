from django.contrib import admin
from .models import (Note,Project, GpsLog, GpsLogData, Image, ImageData, Device, 
                        UserDeviceAssociation, ImageDataAdmin, WmsSource, ProjectData, 
                        TmsSource, UserConfiguration)
from leaflet.admin import LeafletGeoAdmin
from django_json_widget.widgets import JSONEditorWidget
from django.db import models
from django import forms
from django.utils.html import format_html
from django.utils.safestring import mark_safe
from django.contrib.admin.widgets import AdminFileWidget
from django.db.models.fields.files import FileField

class ProjectDataAdminFileWidget(forms.FileInput):
    def render(self, name, value, attrs=None, renderer=None):
        output = []
        if value and getattr(value, "url", None):
            new_url = f"/api/projectdatas/{value.instance.id}"
            stringForm = """<table>
                        <tr>
                            <th>Local path</th>
                            <th>{0}</th>
                        </tr>
                        <tr>
                            <th>Download url</th>
                            <th><a href="{1}" target="_blank">{2}</a></th>
                        </tr>
                        <tr>
                            <th>Change file</th>
                            <th>{3}</th>
                        </tr>
                        </table>""".format(value.url,new_url, new_url,super(forms.FileInput, self).render(name, value, attrs))
            output.append(stringForm)
        else:
            output.append(super(forms.FileInput, self).render(name, value, attrs))
        
        return mark_safe(''.join(output))

@admin.register(ProjectData)
class ProjectDataAdmin(admin.ModelAdmin):
    list_display = ('file_name', 'local_path', 'download_url') # Modifies the Change List
    
    formfield_overrides = {
        FileField: {'widget': ProjectDataAdminFileWidget},
    }


    def file_name(self, obj):
        return obj.label
    
    def local_path(self, obj):
        return  obj.file
        
    def download_url(self, obj):
        myurl = f"/api/projectdatas/{obj.id}"
        return format_html('<a href="{}">{}</a>', myurl, myurl)

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
# admin.site.register(ProjectData)
admin.site.register(UserConfiguration)

@admin.register(Note)
class NoteAdmin(LeafletGeoAdmin):
    formfield_overrides = {
        models.JSONField: {'widget': JSONEditorWidget(mode='tree')},
    }





admin.site.site_header = 'GSS Admin'
admin.site.site_title = 'GSS Admin'
# admin.site.index_title = "<your_index_title>"

