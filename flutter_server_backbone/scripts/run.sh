
VERSION=3.0.0
WORKSPACE=/Users/hydrologis/TMP/GSSSERVER/
KEYSTORE=""
DBHOST=localhost
DBNAME=test

java -Xmx4g -jar gss-backbone-$VERSION.jar $WORKSPACE testPwd $KEYSTORE jdbc:postgresql://$DBHOST:5432/$DBNAME