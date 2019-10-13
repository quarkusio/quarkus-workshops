#!/usr/bin/env bash
# tag::adocShell
export DEST=src/main/resources/META-INF/resources
ng build --prod --base-href "."
rm -Rf ${DEST}
cp -R dist/* ${DEST}
# end::adocShell

