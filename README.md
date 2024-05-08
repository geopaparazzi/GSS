[![DOI](https://zenodo.org/badge/280122006.svg)](https://zenodo.org/badge/latestdoi/280122006)

# How to setup a GSS server in few minutes

To setup a new GSS environment with postgis database the simplest way is to use the docker compose.

## What you need

The domain name or ip address of the server you will run the GSS on. If that one is wrong, you will
not be able to access the GSS from the browser.

In the following steps we will assume that our server is: http://gisaster.com
which btw is the demo server for the GSS.

## Step 1: create the docker yml 

To get started you can use [the docker compose yml available on this repo](https://github.com/geopaparazzi/GSS/blob/3a5b48c4b93d30e1549aa132f898c2952cc4bc74/docker/docker-compose.yml), 
which already is configured with the proper versions to work nicely.
Download it to your server and store it as **docker-compose.yml** file.

## Step 2: create the environment file

Create an .env file in the same folder as the docker-compose.yml file. This file is needed to configure the GSS for your needs.

This is how it should look like:

```
# if you want debug active
DEBUG=true
# logging level
DJANGO_LOG_LEVEL=DEBUG

# main shared volume: change the base folder as you need it
GSS_VOLUME=/home/hydrologis/TMP/GSS_TEST/
GSS_POSTGRESFOLDER=/home/hydrologis/TMP/GSS_TEST/data/
GSS_MEDIAFOLDER=/home/hydrologis/TMP/GSS_TEST/media/
GSS_DYNAMICMIGRATIONSFOLDER=/home/hydrologis/TMP/GSS_TEST/dynamicmigrations

# database connection parameters
GSS_POSTGRES_USER=testuser
GSS_POSTGRES_PASSWORD=testpwd
GSS_POSTGRES_DB=testgssdb
GSS_POSTGRES_PORT=5432

# deploy: IMPORTANT: make sure that you add your server name/address
GSS_ALLOWED_HOSTS=gisaster.com 0.0.0.0
GSS_ALLOWED_ORIGINS=https://gisaster.com
GSS_CORS_ALLOWED_ORIGINS=https://gisaster.com
GSS_CSRF_TRUSTED_ORIGINS=https://gisaster.com
GSS_SECRETKEY="django-verysecuresochangeit-89)jfnethf$ngh2a3r4c@^u*#9u&_jiby^^0g@)=u7dk17tlg)sh"
```

Also for this there is an [example file to download](https://github.com/geopaparazzi/GSS/blob/master/docker/env.example) in the repo.

## Step 3: start the server

To start the server you can run:

```
docker compose -f docker-compose.yml --env-file .env up
```

or with older versions of docker using the docker-compose command:

```
docker-compose -f docker-compose.yml up
```

The startup will:

* create a new postgis database if not already available
* populate the database with the groups and minimum users mandatory for the GSS to work properly
* if not already existing, default users **admin, coordinator, webuser and surveyor** will be created with the
  password equal to the username. So make sure that you change the poasswords.

Once the server is up, open http://gisaster.com (or whatever your server is) and you should see the login window.
The admin interface can be accessed directly at: https://gisaster.com/admin/

## Step 4: 

Connect SMASH to the server by filling in the **Settings -> GSS Connection Settings**: 

![image](https://github.com/geopaparazzi/GSS/assets/390250/65b94f2f-138d-46fb-8ab0-216c93d3fbc4)

Steps are:

* insert the server url
* refresh the projects list. If the server is setup correctly, you will find at least the **Default** project
* login to aquire the connection token

Now you are ready to export your project to the server.

If you want to use the new formbuilder from web and dynamically created layers, [have a look at this video](https://www.youtube.com/watch?v=lRXou2QnE3s).

## Step 5: shutdown the server

To stop the server you can run:

```
docker compose -f docker-compose.yml --env-file .env down
```

or 

```
docker-compose -f docker-compose.yml down
```


