#!/usr/bin/env bash
export DEST=../api-fight/src/main/resources/META-INF/resources/super-heroes
ng build --prod --base-href "."
rm -Rf ${DEST}
cp -R dist/* ${DEST}

