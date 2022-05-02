from django.db import models

class Note(models.Model):
    project_name = models.CharField(max_length=100)
    text = models.CharField(max_length=100)
    description = models.TextField()
    ts = models.DateTimeField()
    lon = models.FloatField()
    lat = models.FloatField()
    altim = models.FloatField()
    form = models.JSONField()
    marker = models.CharField(max_length=50)
    size = models.FloatField()
    rotation = models.FloatField()
    color = models.CharField(max_length=9)
    accuracy = models.FloatField()
    heading = models.FloatField()
    speed = models.FloatField()
    speedaccuracy = models.FloatField()
