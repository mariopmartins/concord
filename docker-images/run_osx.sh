#!/bin/bash

VERSION="0.68.2"

if [ -z $CONCORD_CFG_FILE ]; then
    CONCORD_CFG_FILE="/opt/concord/conf/server.conf"
fi
echo "CONCORD_CFG_FILE: ${CONCORD_CFG_FILE}"

docker rm -f db dind agent server console

docker run -d \
--name db \
-e 'POSTGRES_PASSWORD=q1' \
-e 'PGDATA=/var/lib/postgresql/data/pgdata' \
--mount source=concordDB,target=/var/lib/postgresql/data \
-p 5432:5432 \
hub.docker.prod.walmart.com/library/postgres:latest

docker run -d \
--link db \
--name server \
-p 8001:8001 \
-v /tmp:/tmp \
-v "${CONCORD_CFG_FILE}:${CONCORD_CFG_FILE}:ro" \
-e "CONCORD_CFG_FILE=$CONCORD_CFG_FILE" \
-e 'DB_URL=jdbc:postgresql://db:5432/postgres' \
docker.prod.walmart.com/walmartlabs/concord-server:${VERSION}

docker run -d \
--privileged \
--name dind \
-v /tmp:/tmp \
docker.prod.walmart.com/walmartlabs/concord-dind

docker run -d \
--name agent \
--link dind \
--link server \
-v /tmp:/tmp \
-e 'CONCORD_DOCKER_LOCAL_MODE=false' \
docker.prod.walmart.com/walmartlabs/concord-agent:${VERSION}

docker run -d \
--name console \
--link server \
-p 8080:8080 \
docker.prod.walmart.com/walmartlabs/concord-console:${VERSION}
