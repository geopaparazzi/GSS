from django.contrib import admin
from .models import Note,Surveyor,Project, GpsLog, GpsLogData, Image, ImageData

admin.site.register(Note)
admin.site.register(Surveyor)
admin.site.register(Project)
admin.site.register(GpsLog)
admin.site.register(GpsLogData)
admin.site.register(Image)
admin.site.register(ImageData)
