from django.core.management.base import BaseCommand, CommandError

from django.conf import settings
from django.core.management import call_command
import psycopg2

from django.db import connection

class Command(BaseCommand):
    help = 'Delete GSS test database. This command works only if in debug mode and is meant for devel purposes.'

    def add_arguments(self, parser):
        pass

    def handle(self, *args, **options):

        if settings.DEBUG == False:
            self.stderr.write("Database deletion is available only when in debug mode. Exiting.")
            return
        else:    
            self.stdout.write(f"Dropping database {settings.POSTGRES_DBNAME}")


            connection = psycopg2.connect(
                host=settings.POSTGRES_HOST,
                user=settings.POSTGRES_USER,
                password=settings.POSTGRES_PASS,
                port=settings.POSTGRES_PORT,
            )
            connection.autocommit = True
            cursor = connection.cursor()
            cursor.execute(f"drop database if exists {settings.POSTGRES_DBNAME}")
            cursor.execute(f"create database {settings.POSTGRES_DBNAME}")
            connection.close
            
            self.stdout.write("Database dropped.")



    
