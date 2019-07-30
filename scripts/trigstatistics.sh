#!/usr/bin/env bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/target

RESOURCES="$( cd $ROOT && cd .. && pwd)"/vua-resources

PATH_TO_TRIG=$1
TYPE="instance"
# instance or grasp

#STAT=
# can be empty (dumps all) or "en=entities", "ne=non-entities", "dbp=dbpedia" , "event" or "topic"

# limit the freqquency
N="0"

java -Xmx2000m -cp "$LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar" eu.newsreader.eventcoreference.output.TrigStats --trig-folder $PATH_TO_TRIG --type $TYPE  --n $N
#--stat $STAT

