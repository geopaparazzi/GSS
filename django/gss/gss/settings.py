"""
Django settings for gss project.

Generated by 'django-admin startproject' using Django 4.0.4.

For more information on this file, see
https://docs.djangoproject.com/en/4.0/topics/settings/

For the full list of settings and their values, see
https://docs.djangoproject.com/en/4.0/ref/settings/
"""

from pathlib import Path
import os

DEBUG = True #os.getenv("DEBUG", False) == "True"
USE_LOCALDATA = os.getenv("USE_LOCALDATA", "True") == "True"
if USE_LOCALDATA:
    POSTGRES_DBNAME = "gsstest"
    POSTGRES_USER = "postgres"
    POSTGRES_PASS = "postgres"
    POSTGRES_HOST = "localhost"
    POSTGRES_PORT = "5432"
else:
    POSTGRES_DBNAME = os.getenv('GSS_POSTGRES_DBNAME', "test")
    POSTGRES_USER = os.getenv('GSS_POSTGRES_USER', "postgres")
    POSTGRES_PASS = os.getenv('GSS_POSTGRES_PASS', "postgres")
    POSTGRES_HOST = os.getenv('GSS_POSTGRES_HOST', "localhost")
    POSTGRES_PORT = os.getenv('GSS_POSTGRES_PORT', "5432")


# Build paths inside the project like this: BASE_DIR / 'subdir'.
BASE_DIR = Path(__file__).resolve().parent.parent


# Quick-start development settings - unsuitable for production
# See https://docs.djangoproject.com/en/4.0/howto/deployment/checklist/

# SECURITY WARNING: keep the secret key used in production secret!
SECRET_KEY = os.getenv('SECRET_KEY', 'django-insecure-3l%jlj1iw@+1v(o-8v!ef^a)mrr$^&7b=0o-45e2%9_q^64d7+')

ALLOWED_HOSTS = [

]

LEAFLET_CONFIG = {
    'DEFAULT_CENTER': (46.0, 11),
    'DEFAULT_ZOOM': 6,
    'MAX_ZOOM': 20,
    'MIN_ZOOM':3,
    'SCALE': 'both'
}


# Application definition

INSTALLED_APPS = [
    'data',
    'leaflet',
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'django.contrib.gis',
    'django_cleanup.apps.CleanupConfig',
    'django_json_widget',
    'rest_framework',
]

MIDDLEWARE = [
    'django.middleware.security.SecurityMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
]

ROOT_URLCONF = 'gss.urls'

TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': [],
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.debug',
                'django.template.context_processors.request',
                'django.contrib.auth.context_processors.auth',
                'django.contrib.messages.context_processors.messages',
            ],
        },
    },
]

WSGI_APPLICATION = 'gss.wsgi.application'


# Database
# https://docs.djangoproject.com/en/4.0/ref/settings/#databases

# DATABASES = {
#     'default': {
#         'ENGINE': 'django.db.backends.sqlite3',
#         'NAME': BASE_DIR / 'db.sqlite3',
#     }
# }
DATABASES = {
    'default': {
        'ENGINE': 'django.contrib.gis.db.backends.postgis',
        'NAME': POSTGRES_DBNAME,
        'USER': POSTGRES_USER,
        'PASSWORD': POSTGRES_PASS,
        'HOST': POSTGRES_HOST,
        'PORT': POSTGRES_PORT,
    }
}

# Password validation
# https://docs.djangoproject.com/en/4.0/ref/settings/#auth-password-validators

AUTH_PASSWORD_VALIDATORS = [
    {
        'NAME': 'django.contrib.auth.password_validation.UserAttributeSimilarityValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.MinimumLengthValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.CommonPasswordValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.NumericPasswordValidator',
    },
]


# Internationalization
# https://docs.djangoproject.com/en/4.0/topics/i18n/

LANGUAGE_CODE = 'en-us'

TIME_ZONE = 'UTC'

USE_I18N = True

USE_TZ = False


# Static files (CSS, JavaScript, Images)
# https://docs.djangoproject.com/en/4.0/howto/static-files/

STATIC_URL = 'static/'

# Default primary key field type
# https://docs.djangoproject.com/en/4.0/ref/settings/#default-auto-field

DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'

# REST framework

REST_FRAMEWORK = {
    'DEFAULT_RENDERER_CLASSES': [
        'rest_framework.renderers.JSONRenderer',
        'rest_framework.renderers.BrowsableAPIRenderer',
    ],
    'DEFAULT_PARSER_CLASSES': [
        'rest_framework.parsers.FormParser',
        'rest_framework.parsers.MultiPartParser',
        'rest_framework.parsers.JSONParser',
    ],
#    'DATETIME_FORMATS': [
#        '%s000',
#    ],
    'DEFAULT_AUTHENTICATION_CLASSES': [
        'rest_framework.authentication.BasicAuthentication',
        'rest_framework.authentication.SessionAuthentication',
    ]
}


# absolute server path
MEDIA_ROOT = os.path.join(BASE_DIR.parent, 'media')
# relative browser URL to access media
MEDIA_URL = '/media/'

DATA_UPLOAD_MAX_MEMORY_SIZE = 50 * 1014 * 1024
FILE_UPLOAD_MAX_MEMORY_SIZE = 2000 * 1024 * 1024