#!/bin/bash

set -ueo pipefail

python3 manage.py migrate
python3 manage.py loaddata data/fixtures/fixtures-dev.yaml
python3 manage.py collectstatic --noinput
exec "$@"
