#!/usr/bin/env bash
docker run --ulimit memlock=-1:-1 -it --rm=true \
    --memory-swappiness=0 \
    --name heroes-database \
    -e POSTGRES_USER=superman \
    -e POSTGRES_PASSWORD=superman \
    -e POSTGRES_DB=heroes-database \
    -p 5433:5432 \
    postgres:10.5
