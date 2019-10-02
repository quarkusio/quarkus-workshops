#!/usr/bin/env bash
docker run --ulimit memlock=-1:-1 -it --rm=true \
    --memory-swappiness=0 \
    --name fights-database \
    -e POSTGRES_USER=superfight \
    -e POSTGRES_PASSWORD=superfight \
    -e POSTGRES_DB=fights-database \
    -p 5432:5432 \
    postgres:10.5
