set -x

VERSION=`git describe --tags --abbrev=0`

# docker image rm moovida/gss-docker:$VERSION
# docker build -t moovida/gss-docker:$VERSION -f docker/Dockerfile .

set +x
read -p "Push moovida/gss-docker:$VERSION to Docker Hub? [y/N] " PUSH_ANSWER
set -x
if [[ "$PUSH_ANSWER" =~ ^[Yy]$ ]]; then
    docker push moovida/gss-docker:$VERSION
fi
