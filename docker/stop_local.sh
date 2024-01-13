#! /bin/sh
environment="local"

cd `dirname $0`

docker compose -f ./$environment/compose.yaml stop