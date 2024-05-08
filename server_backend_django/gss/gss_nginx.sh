#!/bin/bash

set -ueo pipefail
# set -x

echo "###################################"
echo "#   Starting gss server."
echo "###################################"

# source activate venv if the file exists
if [ -f ../env/bin/activate ]; then
    echo "Activating virtual environment"
    source ../env/bin/activate
fi

# source config file if it exists
if [ -f ./config.sh ]; then
    echo "Sourcing config.sh"
    source ./config.sh
fi

set -x
# get the number of cores of the host machine
if [ -f /proc/cpuinfo ]; then
    CORES=$(grep -c ^processor /proc/cpuinfo)
    # make sure it is a number
    if ! [[ "$CORES" =~ ^[0-9]+$ ]]; then
        CORES=4
    fi
else
    CORES=4
fi
echo "USING $CORES CORES."

echo "WAIT FOR DB TO STARTUP..."
sleep 20
echo "ENSURE MINIMAL DB SETUP"
python manage.py migrate
python manage.py populate_for_gss 
echo "RUN COLLECTSTATIC"
python manage.py collectstatic --noinput
echo "START NGINX"
nginx -c /basefolder/gss/nginx.conf
sleep 5
echo "START SERVER"
gunicorn gss.wsgi  -w $CORES --bind 0.0.0.0:8000 --timeout 0
