set -x

VERSION=$(git describe --tags --abbrev=0)
DOCKER_VERSION=${VERSION//+/_}

docker image rm bluebiloba/gss-docker:$DOCKER_VERSION
docker build -t bluebiloba/gss-docker:$DOCKER_VERSION -f docker/Dockerfile .
