set -x 

docker image rm gss-docker
docker build -t gss-docker:4.0 -f docker/Dockerfile .

