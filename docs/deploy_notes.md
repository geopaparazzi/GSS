# Docker image

1. commit the changes to git
2. create and assign a tag in the format `n.nn+BB`
3. push everything (the tag, too)

then, exec:

4. `./scripts/release_sync_version.sh`
5. `./prepare_for_docker.sh`
6. `./build_docker.sh`
7. `docker save -o Geopaparazzi_GSS_n.nn_BB.tar bluebiloba/gss-docker:n.nn_BB`
8. `scp Geopaparazzi_GSS_n.nn_BB.tar.gz user@remote_ip:/home/user/`

then, on the remote:

9. `docker load -i ./Geopaparazzi_GSS_n.nn_BB.tar`


# Android App (APK)

1. `export ANDROID_HOME="$HOME/.buildozer/android/platform/android-sdk"`
2. `export ANDROID_SDK_ROOT="$ANDROID_HOME"`
3. `flutter doctor -v`
4. `flutter build apk --no-tree-shake-icons --release`
