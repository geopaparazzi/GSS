FROM fischerscode/flutter:2.0.6 AS flutter-builder

COPY --chown=flutter flutter_server flutter_server
WORKDIR /home/flutter/flutter_server
RUN flutter build web --release

FROM maven:3.6-openjdk-14 AS java-builder

WORKDIR /usr/src/gss
COPY flutter_server_backbone/pom.xml .
RUN mvn -B verify clean
COPY flutter_server_backbone/src src
RUN mvn -B -D finalName=gss-backbone package

FROM openjdk:14

WORKDIR /usr/gss
COPY --from=flutter-builder /home/flutter/flutter_server/build/web public
COPY --from=java-builder /usr/src/gss/target/lib lib
COPY --from=java-builder /usr/src/gss/target/gss-backbone.jar .
COPY docker/run.sh .
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.8.0/wait /wait
RUN chmod +x /wait
RUN mkdir /workspace

EXPOSE 8080

CMD ["./run.sh"]
