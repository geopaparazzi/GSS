set -x 

docker image rm gss-docker
docker build -t gss-docker -f docker/Dockerfile .

