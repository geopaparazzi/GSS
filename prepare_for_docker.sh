set -x

cd server_backend_django
source .venv/bin/activate

rm -rf ./static
mkdir static


read -p "Press Enter to continue..."


cd gss
# source config file if it exists
if [ -f ./config.sh ]; then
    echo "Sourcing config.sh"
    source ./config.sh
fi
./manage.py collectstatic --noinput


read -p "Press Enter to continue..."


# go build the frontend
cd ../../server_frontend_flutter/
flutter clean
GSS_GIT_TAG=$(grep -E '^version = "' ../server_backend_django/pyproject.toml | cut -d '"' -f2)
IMAGEGRID_SHOW_ALL=${GSS_IMAGEGRID_SHOW_ALL:-true}
IMAGEGRID_DEBUG=${GSS_IMAGEGRID_DEBUG:-false}
flutter build web --dart-define=IMAGEGRID_DEBUG="$IMAGEGRID_DEBUG" --dart-define=GSS_GIT_TAG="$GSS_GIT_TAG" --dart-define=IMAGEGRID_SHOW_ALL="$IMAGEGRID_SHOW_ALL"

read -p "Press Enter to continue..."


# copy the frontend to the static folder
cp -rv build/web/* ../server_backend_django/static/

cd ..
