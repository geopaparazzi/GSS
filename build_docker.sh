set -x 

VERSION=`git describe --tags --abbrev=0`

docker image rm moovida/gss-docker:$VERSION
docker build -t moovida/gss-docker:$VERSION -f docker/Dockerfile .

