#!/usr/bin/env bash
export DEST=src/main/resources/META-INF/resources
ng build --prod --base-href "."
rm -Rf ${DEST}
cp -R dist/* ${DEST}

