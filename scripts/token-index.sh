#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/lib
RESOURCES="$( cd $ROOT && cd .. && pwd)"/vua-resources

#Recursively scans the folder for NAF files and creates an index file with the URI and the token layer
#May take a while to complete.....depending on the number of NAF files.
#Folder with *.naf files
NAF=$1
EXT=".naf"

java -Xmx3000m -cp $LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar eu.newsreader.eventcoreference.storyline --folder $NAF --extension $EXT



