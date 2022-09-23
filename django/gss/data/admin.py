from django.contrib import admin
from .models import Note,Project, GpsLog, GpsLogData, Image, ImageData, Device, UserDeviceAssociation
from leaflet.admin import LeafletGeoAdmin

admin.site.register(Note, LeafletGeoAdmin)
admin.site.register(Project)
admin.site.register(GpsLog, LeafletGeoAdmin)
admin.site.register(GpsLogData, LeafletGeoAdmin)
admin.site.register(Image, LeafletGeoAdmin)
admin.site.register(ImageData)
admin.site.register(Device)
admin.site.register(UserDeviceAssociation)

admin.site.site_header = 'GSS Admin'
admin.site.site_title = 'GSS Admin'
# admin.site.index_title = "<your_index_title>"

