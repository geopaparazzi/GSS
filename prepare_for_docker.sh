set -x

cd server_backend_django
source env/bin/activate

rm -rf ./static
mkdir static


cd gss
# source config file if it exists
if [ -f ./config.sh ]; then
    echo "Sourcing config.sh"
    source ./config.sh
fi
./manage.py collectstatic --noinput

# go build the frontend
cd ../../server_frontend_flutter/
flutter clean
flutter build web

# copy the frontend to the static folder
cp -rv build/web/* ../server_backend_django/static/

cd ..
