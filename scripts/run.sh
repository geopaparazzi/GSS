#!/bin/bash -e

WAIT_HOSTS="${POSTGRES_URL%/*}" /wait

args=(-w /workspace)
[ ! -z "$KEYSTORE" ] && args+=(-s "$KEYSTORE")
[ ! -z "$KEYSTORE_PASSWORD" ] && args+=(-sp "$KEYSTORE_PASSWORD")

exec java -jar gss-backbone.jar ${args[*]}
