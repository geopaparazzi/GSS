set -x 

flutter clean
GSS_GIT_TAG=$(grep -E '^version = "' ../server_backend_django/pyproject.toml | cut -d '"' -f2)
IMAGEGRID_SHOW_ALL=${GSS_IMAGEGRID_SHOW_ALL:-true}
flutter build web --dart-define=GSS_GIT_TAG="$GSS_GIT_TAG" --dart-define=IMAGEGRID_SHOW_ALL="$IMAGEGRID_SHOW_ALL"

rm -rf ../server_backend_django/static/assets
rm -rf ../server_backend_django/static/canvaskit
cp -rv build/web/* ../server_backend_django/static/
