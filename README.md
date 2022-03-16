[![DOI](https://zenodo.org/badge/280122006.svg)](https://zenodo.org/badge/latestdoi/280122006)

# GSS installation instructions

* download the latest release from the releases page
* extract the archive on the disk
* make sure java >= 11 is installed and on the PATH (you can check with java -version)
* run the run.sh script

## The run script

By default the run script works with a H2GIS database and doesn't need PostGIS to be installed.

Here below three versions of the script:

1. no database defined, uses H2GIS

```
VERSION=3.3.0
WORKSPACE=/home/hydrologis/TMP/GSS/
MEM=-Xmx4g
java $MEM -jar gss-backbone-$VERSION.jar -w $WORKSPACE -mp testPwd
```

2. PostGIS database used:

```
VERSION=3.3.0
WORKSPACE=/home/hydrologis/TMP/GSS/
DBHOST=localhost
DBNAME=testgss
MEM=-Xmx4g
java $MEM -jar gss-backbone-$VERSION.jar -w $WORKSPACE -mp testPwd -p $DBHOST:5433/$DBNAME -pu test -pp test
```

3. To use a different port. Ex run it on port 8083

```
VERSION=3.3.0
WORKSPACE=/home/hydrologis/TMP/GSS/
DBHOST=localhost
DBNAME=testgss
MEM=-Xmx4g
java $MEM -jar gss-backbone-$VERSION.jar -w $WORKSPACE -mp testPwd -p $DBHOST:5433/$DBNAME -pu test -pp test -pt 8083
```


## All options

```
usage: gss
 -mp,--mobilepwd <arg>   The password used by mobile devices to connect
                         (defaults to testPwd).
 -p,--psql_url <arg>     The optional url to enable postgis database use
                         (disables H2GIS).
 -pp,--psql_pwd <arg>    The optional postgis password (defaults to test).
                         Mandatory if the url is defined.
 -pt,--port <arg>        The optional port to use (defaults to 8080).
 -pts,--sslport <arg>    The optional port to use for ssl (defaults to
                         443).
 -pu,--psql_user <arg>   The optional postgis user (defaults to test).
                         Mandatory if the url is defined.
 -s,--ssl <arg>          The optional path to the keystore file for ssl.
 -sp,--ssl_pwd <arg>     The optional password for the keystore file.
                         Mandatory if the keystore file is defined.
 -w,--workspace <arg>    The path to the workspace.
 ```

