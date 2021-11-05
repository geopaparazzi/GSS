VERSION=3.3.0
WORKSPACE=/home/hydrologis/TMP/GSS/
#KEYSTORE=""
#DBHOST=localhost
#DBNAME=test
MEM=-Xmx4g
java $MEM -jar gss-backbone-$VERSION.jar -w $WORKSPACE -mp testPwd #$KEYSTORE -p jdbc:postgresql://$DBHOST:5432/$DBNAME -pu postgresuser -pp postgrespwd
