set -x 

flutter clean
flutter build web

rm -rf ../server_backend_django/static/assets
rm -rf ../server_backend_django/static/canvaskit
cp -rv build/web/* ../server_backend_django/static/

