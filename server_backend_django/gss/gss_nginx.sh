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
# WEB_CONCURRENCY, when set (e.g. via docker-compose), picks the gunicorn worker
# count directly. Otherwise fall back to the host's CPU core count - note this
# reads /proc/cpuinfo, which reflects the underlying host, not any container CPU
# limit, so on a big host this can spawn far more workers than intended.
if [ -n "${WEB_CONCURRENCY:-}" ] && [[ "$WEB_CONCURRENCY" =~ ^[0-9]+$ ]]; then
    CORES=$WEB_CONCURRENCY
elif [ -f /proc/cpuinfo ]; then
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
# GSS_DYNAMICMIGRATIONSFOLDER is bind-mounted over formlayers/migrations for
# persistence across image upgrades. Bind mounts to a host path are never
# pre-populated from the image (unlike named volumes), so on a fresh/empty host
# folder this mount silently hides the __init__.py baked in by the Dockerfile.
# Without it, formlayers/migrations becomes a namespace package and Django's
# migration loader treats the whole app as unmigrated - makemigrations/migrate
# then silently no-op for every dynamic form, with no error, forever (until this
# is restored). Recreate it unconditionally so this can't happen regardless of
# what's mounted here.
touch formlayers/migrations/__init__.py
echo "ENSURE MINIMAL DB SETUP"
# bootstrap the core schema first (e.g. data_form, auth tables) - required before
# gss_makemigrations can even query the Form model, which matters on a fresh/empty
# database.
python manage.py migrate
# then back-fill any enabled formlayer whose table/migration is missing (e.g. after
# restoring/mounting a migrations folder that doesn't have it yet) - safe/idempotent,
# a no-op if nothing is missing. This also runs migrate internally.
python manage.py gss_makemigrations
python manage.py populate_for_gss
echo "RUN COLLECTSTATIC"
python manage.py collectstatic --noinput
echo "START NGINX"
nginx -c /basefolder/gss/nginx.conf
sleep 5
echo "START SERVER"
gunicorn gss.wsgi  -w $CORES --bind 0.0.0.0:8000 --timeout 0
