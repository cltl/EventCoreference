#!/usr/bin/env bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/lib

RESOURCES="$( cd $ROOT && cd .. && pwd)"/vua-resources
# assumes vua-resources is installed next to this installation
# git clone https://github.com/cltl/vua-resources.git
#
# the software assume that the input files are in NAF format minially with the following layers:
# - tokens, terms, entities, coreference for events, srl, timeExpressions
# to create an event coreference layer, use the event-coreference scripts

#DUTCH
ontology ="$RESOURCES"/dbpedia_nl_types.tsv.gz
#ENGLISH
ontology ="$RESOURCES"/instance_types_en.ttl.gz
java -Xmx2000m -cp "$LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar" eu.newsreader.eventcoreference.naf.GetSimpleSemFromNafFolder --naf-folder $1 --extension ".naf" --ontology $ontology
