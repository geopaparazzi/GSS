[![DOI](https://zenodo.org/badge/280122006.svg)](https://zenodo.org/badge/latestdoi/280122006)

# GSS installation instructions

* download the latest release from the releases page
* extract the archive on the disk
* make sure java >= 11 is installed and on the PATH (you can check with java -version)
* run the run.sh script

## The run script

By default the run script works with a H2GIS database and doesn't need PostGIS to be installed.

Here below two versions of the script:

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





