FROM fischerscode/flutter:3.3.0 AS flutter-builder

COPY --chown=flutter server_frontend_flutter flutter_server
WORKDIR /home/flutter/flutter_server
RUN flutter create .
RUN flutter build web --release

FROM nginx:1.23

COPY --from=flutter-builder /home/flutter/flutter_server/build/web /var/www
COPY docker/default.conf.template /etc/nginx/templates/default.conf.template
