VERSION=3.3.0
WORKSPACE=/home/hydrologis/TMP/GSS/
DBHOST=localhost
DBNAME=testgss
MEM=-Xmx4g
java $MEM -jar gss-backbone-$VERSION.jar -w $WORKSPACE -mp testPwd -p $DBHOST:5432/$DBNAME -pu test -pp test
