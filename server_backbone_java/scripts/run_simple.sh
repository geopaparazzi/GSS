VERSION=3.3.0
WORKSPACE=/home/hydrologis/TMP/GSS/
MEM=-Xmx4g
java $MEM -jar gss-backbone-$VERSION.jar -w $WORKSPACE -mp testPwd
