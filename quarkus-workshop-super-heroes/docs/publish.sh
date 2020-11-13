#!/bin/bash
## DO NOT USE
## OR RATHER READ IT AND THEN USE
## ONLY WORKS FROM THE DOCS DIR
## WILL PUBLISH STUFF ON THE WEBSITE, DON'T SCREW UP
export old=`pwd`
#build doc
#mvn clean generate-resources
#publish doc
cd /tmp
rm -fR /tmp/bbvahackathon.github.io
git clone git@github.com:bbvahackathon/bbvahackathon.github.io.git
cd $old
#rm -fR /tmp/bbvahackathon.github.io
rsync -avz ./target/generated-asciidoc/ /tmp/bbvahackathon.github.io
#cp -r ./target/generated-asciidoc/ /tmp/bbvahackathon.github.io
cp /tmp/bbvahackathon.github.io/generated-asciidoc/spine.html /tmp/bbvahackathon.github.io/index.html
cd /tmp/bbvahackathon.github.io
git add .
git commit -m "update Quarkus workshop"
git push origin master
cd $old
