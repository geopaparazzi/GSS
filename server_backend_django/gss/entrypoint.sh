#!/bin/bash

set -ueo pipefail

pdm run ./manage.py migrate
pdm run ./manage.py loaddata data/fixtures/fixtures-basesystem.yaml
pdm run ./manage.py collectstatic --noinput
exec "$@"
