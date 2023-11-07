#!/bin/bash

set -ueo pipefail

pdm run ./manage.py gss_makemigrations
pdm run ./manage.py gss_migrate
pdm run ./manage.py collectstatic --noinput
exec "$@"
