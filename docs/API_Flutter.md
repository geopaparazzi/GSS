This is a tentative to document the existing API of GSS `flutter_server` and `flutter_server_backbone`.

# API

Headers (god/god credentials):
```
Authorization: Basic Z29kOmdvZA==
```

Headers (client):
```
Authorization: Basic MzdiMjlkZWI0ZWIxNTNmNjp0ZXN0UHdk
```
Decoded: `37b29deb4eb153f6:testPwd`

Method | URL
-------|--------|
GET    | /login
POST   | /data
POST   | /usersettings
GET    | /usersettings/bookmarks
GET    | /tiles/mapforge
GET    | /list/surveyors
GET    | /list/webusers
GET    | /list/projects
POST   | /update/webusers
POST   | /delete/webusers
GET    | /datadownload
GET    | /tagsdownload
GET    | /dbinfo
POST   | /upload

## Examples

### /login

Response:
```json
{"mapcenter_xyz":"0.0;0.0;6","hasPermission":true,"basemap":"Openstreetmap","isAdmin":true}
```

### /data

Response:
```json
{
  "images": [
    {
      "dataid": 1,
      "data": "ENCODED_IMAGE_HERE",
      "x": 10.4062847,
      "name": "IMG_20220502_122547.jpg",
      "y": 63.4143236,
      "project": "smash_20210622_215210",
      "surveyor": "37b29deb4eb153f6",
      "id": 1,
      "ts": 1651487147959
    }
  ],
  "notes": [
    {
      "size": 36,
      "color": "#ff004ba0",
      "form": true,
      "marker": "fileAlt",
      "name": "feltapp botanikk",
      "x": 10.406334566615975,
      "y": 63.41422932928229,
      "project": "smash_20210622_215210",
      "surveyor": "37b29deb4eb153f6",
      "id": 5,
      "ts": 1651487958052
    },
    {
      "size": 36,
      "color": "#ff004ba0",
      "form": true,
      "marker": "fileAlt",
      "name": "feltapp botanikk",
      "x": 10.158587760354068,
      "y": 59.13724150688555,
      "project": "smash_20210622_215210",
      "surveyor": "37b29deb4eb153f6",
      "id": 3,
      "ts": 1631882608356
    },
    {
      "size": 36,
      "color": "#ff004ba0",
      "form": true,
      "marker": "fileAlt",
      "name": "0 - custom",
      "x": 10.283933595265038,
      "y": 59.49139768108293,
      "project": "smash_20210622_215210",
      "surveyor": "37b29deb4eb153f6",
      "id": 2,
      "ts": 1631876446738
    },
    {
      "size": 36,
      "color": "#ff004ba0",
      "form": true,
      "marker": "fileAlt",
      "name": "feltapp botanikk",
      "x": 10.282244746537453,
      "y": 59.492298093415044,
      "project": "smash_20210622_215210",
      "surveyor": "37b29deb4eb153f6",
      "id": 1,
      "ts": 1624444237868
    }
  ],
  "logs": []
}
```

### /usersettings

Request to set bookmarks:
```
bookmarks: test:-90,90,-45,45@earth:-160.0,160.0,-85.0,85.0
```

Requests to open registration:
```
GSS_KEY_AUTOMATIC_REGISTRATION: 1651495860665
```

Response:
```
OK
```

Response (failure):
```
{
  "code": 403,
  "message": "No permission for request.",
  "timestamp": "2022-05-02 12:55:57"
}
```

### /usersettings/bookmarks

Response:
```
test:-90,90,-45,45@earth:-160.0,160.0,-85.0,85.0
```

### /list/surveyors

Response:
```json
{"surveyors":[{"contact":"","name":"37b29deb4eb153f6","active":1,"id":1,"deviceid":"37b29deb4eb153f6"}]}
```

### /list/projects

Response
```json
{"projects":[]}
```

### /list/webusers

Response:
```json
{
  "webusers": [
    {
      "uniquename": "god",
      "group_id": "administrators",
      "name": "HydroloGIS S.r.l.",
      "id": 1,
      "email": "info@hydrologis.com"
    },
    {
      "uniquename": "user",
      "group_id": "users",
      "name": "Normal User",
      "id": 2,
      "email": "info@hydrologis.com"
    }
  ]
}
```

### /update/webusers

Request (URLEncoded form):
```
uniquename: god
name:       HydroloGIS S.r.l.
group_id:   users
email:      info@hydrologis.com
id:         1
```

`group_id` can be `users` or `administrators`.
`password` is an optional field, which changes the password, sent as plain text.

Response:
```json
{
  "code": 200,
  "message": "Ok",
  "timestamp": "2022-05-02 12:52:47"
}
```

### /delete/webusers

Request (URLEncoded form):
`id: 1`

Response: see `/update/webusers`.

### /datadownload

Response:
```json
{"projects":[],"maps":[]}
```

### /tagsdownload

Response:
```json
{"tags":[]}
```

### /dbinfo

```
PostgreSQL 13.5 (Debian 13.5-1.pgdg110+1) on x86_64-pc-linux-gnu, compiled by gcc (Debian 10.2.1-6) 10.2.1 20210110, 64-bit
POSTGIS="3.1.4 ded6c34" [EXTENSION] PGSQL="130" GEOS="3.9.0-CAPI-1.16.2" PROJ="7.2.1" LIBXML="2.9.10" LIBJSON="0.15" LIBPROTOBUF="1.3.3" WAGYU="0.5.0 (Internal)" TOPOLOGY
```

### /upload

multipart/form-data with a boundary.

Request:
```
----dio-boundary-0720193152
content-disposition: form-data; name="type"

note
----dio-boundary-0720193152
content-disposition: form-data; name="PROJECT_NAME"

smash_20210622_215210
----dio-boundary-0720193152
content-disposition: form-data; name="_id"

12
----dio-boundary-0720193152
content-disposition: form-data; name="text"

feltapp botanikk
----dio-boundary-0720193152
content-disposition: form-data; name="description"

POI
----dio-boundary-0720193152
content-disposition: form-data; name="ts"

1624444237868
----dio-boundary-0720193152
content-disposition: form-data; name="lon"

10.282244746537453
----dio-boundary-0720193152
content-disposition: form-data; name="lat"

59.492298093415044
----dio-boundary-0720193152
content-disposition: form-data; name="altim"

-1.0
----dio-boundary-0720193152
content-disposition: form-data; name="form"
content-type: text/plain; charset=utf-8
content-transfer-encoding: binary

{"sectionname":"feltapp botanikk","sectiondescription":"feltapp botanikk","forms":[{"formname":"specie","formitems":[{"key":"specie","values":{"items":[{"item":"0 - custom - tilpasset"},{"item":"126795 - ctenocephalides felis"},{"item":"143758 - bradysia impatiens"}]},"value":"","type":"autocompletestringcombo","mandatory":"yes"},{"key":"custom specie","value":"","type":"string"}]},{"formname":"abundance","formitems":[{"key":"abundance","value":"","type":"stringarea","mandatory":"yes"}]},{"formname":"comment","formitems":[{"key":"comment","value":"","type":"stringarea"}]},{"formname":"pictures","formitems":[{"key":"a picture archive","value":"","type":"pictures"}]}]}
----dio-boundary-0720193152
content-disposition: form-data; name="marker"

fileAlt
----dio-boundary-0720193152
content-disposition: form-data; name="size"

36.0
----dio-boundary-0720193152
content-disposition: form-data; name="rotation"

0.0
----dio-boundary-0720193152
content-disposition: form-data; name="color"

#ff004ba0
----dio-boundary-0720193152
content-disposition: form-data; name="accuracy"

null
----dio-boundary-0720193152
content-disposition: form-data; name="heading"

null
----dio-boundary-0720193152
content-disposition: form-data; name="speed"

null
----dio-boundary-0720193152
content-disposition: form-data; name="speedaccuracy"

null
----dio-boundary-0720193152--
```

Response:
```json
{
  "code": 200,
  "message": "Data properly inserted in the server.\\nNotes: {0}\\nGps Logs: {1}\\nImages: {2}",
  "timestamp": "2022-05-02 13:28:20"
}
```

# Static files

```
/ -> /index.html
/loading.png
/main.dart.js
/favicon.png
/flutter_service_worker.js
/assets/*
```
