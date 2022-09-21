from django.contrib import admin
from .models import Note,Surveyor,Project, GpsLog, GpsLogData, Image, ImageData
from leaflet.admin import LeafletGeoAdmin

admin.site.register(Note, LeafletGeoAdmin)
admin.site.register(Surveyor)
admin.site.register(Project)
admin.site.register(GpsLog)
admin.site.register(GpsLogData)
admin.site.register(Image)
admin.site.register(ImageData)


admin.site.site_header = 'GSS Admin'
