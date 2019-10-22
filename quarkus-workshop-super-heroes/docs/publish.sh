#!/bin/bash
# DO NOT USE
# OR RATHER READ IT AND THEN USE
# ONLY WORKS FROM THE DOCS DIR
# WILL PUBLISH STUFF ON THE WEBSITE, DON'T SCREW UP
export old=`pwd`
#build doc
mvn install
#publish doc
cd /tmp
rm -fR /tmp/quarkus-workshops
git clone git@github.com:quarkusio/quarkus-workshops.git
cd quarkus-workshops
git co gh-pages
cd $old
rm -fR /tmp/quarkus-workshops/super-heros
rsync -avz ./target/generated-asciidoc/ /tmp/quarkus-workshops/super-heros
cp /tmp/quarkus-workshops/super-heros/spine.html /tmp/quarkus-workshops/super-heros/index.html 
cd /tmp/quarkus-workshops
git add .
git commit -m "update Quarkus workshop"
git push origin gh-pages
cd $old
