FROM python:3.10

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get -qy install nginx libgdal32

WORKDIR /basefolder
COPY server_backend_django/pyproject.toml server_backend_django/pdm.lock ./

COPY server_backend_django/gss ./gss
RUN rm -f ./gss/config.sh

# this assumes that the flutter part has been built locally
# and copied into the local static folder already
COPY server_backend_django/static ./static

RUN mkdir ./media ./logs

RUN python -m venv env && . env/bin/activate && pip install .

# go in the working folder
WORKDIR /basefolder/gss

COPY docker/https/nginx.conf /basefolder/gss/nginx.conf
COPY docker/https/nginx-selfsigned.crt /basefolder/gss/nginx-selfsigned.crt
COPY docker/https/nginx-selfsigned.key /basefolder/gss/nginx-selfsigned.key
RUN chmod 664 /basefolder/gss/nginx-selfsigned.crt
RUN chmod 644 /basefolder/gss/nginx-selfsigned.key


# start the server using the gss.sh bash script
EXPOSE 8000/tcp
# Expose port 80 and 443 for Nginx
EXPOSE 80
EXPOSE 443
ENTRYPOINT ["/bin/bash", "gss_nginx.sh", "start"]
