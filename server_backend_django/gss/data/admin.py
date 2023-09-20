from django.contrib import admin
from .models import (Note, Project, GpsLog, GpsLogData, Image, ImageData, Device,
                     UserDeviceAssociation,  WmsSource, ProjectData,
                     TmsSource, UserConfiguration, Form)
from .serializers import NoteSerializer
from leaflet.admin import LeafletGeoAdmin
from django_json_widget.widgets import JSONEditorWidget
from django.db import models
from django import forms
from django.utils.html import format_html
from django.urls import reverse
from django.utils.safestring import mark_safe
from django.contrib.admin.widgets import AdminFileWidget
from django.db.models.fields.files import FileField
from django.http import HttpResponse
from django.contrib import messages
import json
from gss.utils import Utilities



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
                        </table>""".format(value.url, new_url, new_url, super(forms.FileInput, self).render(name, value, attrs))
            output.append(stringForm)
        else:
            output.append(
                super(forms.FileInput, self).render(name, value, attrs))

        return mark_safe(''.join(output))


@admin.register(ProjectData)
class ProjectDataAdmin(admin.ModelAdmin):
    # Modifies the Change List
    list_display = ('file_name', 'local_path', 'download_url')

    formfield_overrides = {
        FileField: {'widget': ProjectDataAdminFileWidget},
    }

    def file_name(self, obj):
        return obj.label

    def local_path(self, obj):
        return obj.file

    def download_url(self, obj):
        myurl = f"/api/projectdatas/{obj.id}"
        return format_html('<a href="{}">{}</a>', myurl, myurl)


@admin.register(Form)
class FormAdmin(admin.ModelAdmin):
    list_display = ('name', 'enabled')

    formfield_overrides = {
        models.JSONField: {'widget': JSONEditorWidget(mode='tree')},
    }

# admin.site.register(Note, LeafletGeoAdmin)
# admin.site.register(Form)
admin.site.register(Project)
# admin.site.register(GpsLog, LeafletGeoAdmin)
# admin.site.register(GpsLogData, LeafletGeoAdmin)
# admin.site.register(Image, LeafletGeoAdmin)
# admin.site.register(ImageData, ImageDataAdmin)
admin.site.register(Device)
admin.site.register(UserDeviceAssociation)
admin.site.register(WmsSource)
admin.site.register(TmsSource)
# admin.site.register(ProjectData)
admin.site.register(UserConfiguration)


@admin.register(GpsLog)
class GpsLogAdmin(LeafletGeoAdmin):
    list_display = ('log_id', 'project', 'surveyor', 'date')

    list_filter = ['project', 'user', 'startts']

    def log_id(self, obj):
        return obj.id

    def project(self, obj):
        return obj.project

    def surveyor(self, obj):
        return obj.user.username

    def date(self, obj):
        return str(obj.startts)[:-9]

@admin.register(Image)
class ImageAdmin(LeafletGeoAdmin):
    list_display = ['image_id', 'project', 'surveyor', 'date', 'parent_note']

    list_filter = ['project', 'user', 'ts']

    readonly_fields = ["imagedata"]

    def get_queryset(self, request):
        return super(ImageAdmin, self).get_queryset(request).select_related(
            'project', 'user', 'notes').all()
    
    # def imagedata(self, obj):
    #     return obj.imagedata.data_image

    def image_id(self, obj):
        return obj.id

    def project(self, obj):
        return obj.project

    def surveyor(self, obj):
        return obj.user

    def date(self, obj):
        return str(obj.ts)[:-9]

    def parent_note(self, obj):
        if obj.notes:
            return mark_safe('<a href="{}">{}</a>'.format(
                            reverse("admin:data_note_change", args=(obj.notes.id,)),
                            obj.notes.id
                        ))
        else:
            return ""
        
@admin.register(ImageData)
class ImageDataAdmin(admin.ModelAdmin):
    list_display = ['id']
    readonly_fields = ["data_thumb"]

    def get_queryset(self, request):
        return super(ImageDataAdmin, self).get_queryset(request).defer(
            'data').all()


class NoteTypeFilter(admin.SimpleListFilter):
    title = 'type'

    parameter_name = 'text'

    def lookups(self, request, model_admin):
        formNames = set([c.text for c in model_admin.model.objects.all() if c.form != None])
        return [(fn, fn) for fn in formNames] + [
          ('simple note', 'simple note')]
        

    def queryset(self, request, queryset):
        if self.value():
            if self.value() == 'simple note':
                return queryset.filter(form__isnull=True)
            else:
                return queryset.filter(text=self.value())
        else:
            return queryset.all()

@admin.register(Note)
class NoteAdmin(LeafletGeoAdmin):
    list_display = ('note_id', 'project', 'surveyor', 'date',
                    'images', 'parent')  # Modifies the Change List

    list_filter = ['project', 'user', NoteTypeFilter, 'ts']
    actions = ['export_as_geojson']

    # list_select_related = True

    def get_queryset(self, request):
        return super(NoteAdmin, self).get_queryset(request).select_related(
            'project', 'user', 'previous').all()

    formfield_overrides = {
        models.JSONField: {'widget': JSONEditorWidget(mode='tree')},
    }

    def note_id(self, obj):
        return obj.id

    def project(self, obj):
        return obj.project

    def surveyor(self, obj):
        return obj.user.username

    def date(self, obj):
        return str(obj.ts)[:-9]


    def parent(self, obj):
        if obj.previous != None:
            return mark_safe('<a href="{}">{}</a>'.format(
                            reverse("admin:data_note_change", args=(obj.previous.id,)),
                            obj.previous.id
                        ))
        return ""

    def images(self, obj):
        queryset = Image.objects.filter(notes=obj.id).all()
        if queryset:
            idsList = []
            for q in queryset:
                idsList.append(
                    '<a href="{}">{}</a>'.format(
                        reverse("admin:data_image_change", args=(q.id,)),
                        q.id
                    )
                )
            return mark_safe(",".join(idsList))
        else:
            return ""
        
    @admin.action(description='Export selected form notes as geojson')
    def export_as_geojson(self, request, queryset):
        dataMap = {"type": "FeatureCollection"}
        featuresList = []
        dataMap["features"] = featuresList
        for q in queryset:
            if q.form != None:
                geom = q.the_geom
                properties = {
                    "formtype": q.text,
                    "note_id": q.id,
                    "project": q.project_id,
                    "user": q.user_id,
                    "ts": Utilities.toStringWithSeconds(q.ts),
                    "uploadts": Utilities.toStringWithSeconds(q.uploadts),
                    "description": q.description,
                    "marker": q.marker,
                    "size": q.size,
                    "rotation": q.rotation,
                    "color": q.color,
                    "accuracy": q.accuracy,
                    "heading": q.heading,
                    "speed": q.speed,
                    "speedaccuracy": q.speedaccuracy,
                    "altim": q.altim,
                    }
                feature = {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            geom.x,
                            geom.y
                        ]
                    },
                    "properties": properties
                }
                # extract form data
                form = q.form
                formsList = form["forms"]
                # here each form is a tab
                for f in formsList:
                    formItemList = f["formitems"]
                    for fi in formItemList:
                        key = fi["key"]
                        value = fi["value"]
                        # ENABLE THIS IF pictures are toi be exported in json
                        # if fi['type'] == 'pictures':
                        #     imageID = int(value)
                        #     # get image from Images
                        #     image = Image.objects.select_related("imagedata").get(id=imageID)
                        #     imageData = image.imagedata.getAsBase64()
                        #     properties[key] = imageData
                        # else:
                        properties[key] = value

                featuresList.append(feature)


        if dataMap:
            response = HttpResponse(content=json.dumps(dataMap), content_type='text/json')
            response['Content-Disposition'] = 'attachment; filename=gss_notes_dump.json'
            return response 
        else:
            self.message_user(request, "No form notes selected. Check your selection.", messages.WARNING)




admin.site.site_header = 'GSS Admin'
admin.site.site_title = 'GSS Admin'
# admin.site.index_title = "<your_index_title>"
