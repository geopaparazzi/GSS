############################################################
# Dockerfile to run gss war files in tomcat
############################################################


FROM moovida/alpinetomcat:jdk8tom9033
MAINTAINER Andrea Antonello (www.hydrologis.com)

RUN mkdir -p /home/gssworkspace

COPY ./ROOT.war /home/tomcat/webapps/

CMD ["catalina.sh", "run"]

# TO BE RUN AS
# docker run -d -v /path/to/folder/on/destination/:/home/gssworkspace -e JAVA_OPTS="-Xms1256m -Xmx4g -Dstage.globalfolder=/home/gssworkspace/" -p 8080:8080 -p 9092:9092 imagename
