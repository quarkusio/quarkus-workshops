#!/usr/bin/env bash
docker run --ulimit memlock=-1:-1 -it --rm=true \
    --memory-swappiness=0 \
    --name villains-database \
    -e POSTGRES_USER=superbad \
    -e POSTGRES_PASSWORD=superbad \
    -e POSTGRES_DB=villains-database \
    -p 5434:5432 \
    postgres:10.5
