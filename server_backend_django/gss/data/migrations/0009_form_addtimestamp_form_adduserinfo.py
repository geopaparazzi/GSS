# Generated by Django 4.2.7 on 2023-11-15 07:24

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('data', '0008_form_geometrytype'),
    ]

    operations = [
        migrations.AddField(
            model_name='form',
            name='addtimestamp',
            field=models.BooleanField(default=True),
        ),
        migrations.AddField(
            model_name='form',
            name='adduserinfo',
            field=models.BooleanField(default=True),
        ),
    ]