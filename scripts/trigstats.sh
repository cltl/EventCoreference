#!/usr/bin/env bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/lib
RESOURCES="$( cd $ROOT && cd .. && pwd)"/vua-resources

#Folder with *.trig files
TRIG=$1

java -Xmx3000m -cp $LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar eu.newsreader.eventcoreference.output.TrigStats --trig-folder $TRIG --type instance

java -Xmx3000m -cp $LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar eu.newsreader.eventcoreference.output.TrigStats --trig-folder $TRIG --type grasp

#TSV file with entity ontology hierarchy separated by TABs & LINEs
ONTENT=$2
#TSV file with entity label, count and ontology type separated by TABs & LINES
COUNTENT=$3
java -Xmx3000m -cp $LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar eu.newsreader.eventcoreference.output.DataSetEntityHierarchy --ont $ONTENT --ent $COUNTENT title "Brexit DBpedia ontology of entities"

#TSV file with event label, count and ontology types separated by TABs & LINES
COUNTEVENT=$4
COUNTYPEVENT=$5 
java -Xmx3000m -cp $LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar eu.newsreader.eventcoreference.output.DataSetEventHierarchy --eso $RESOURCES/ESO_Version2.owl --event $COUNTYPEVENT --cnt $COUNTEVENT --title "Brexit ESO ontology for events"

#TSV file with entity ontology hierarchy separated by TABs & LINEs
#ONTENT=$2
#ONTENT="/Users/piek/Desktop/NWR-INC/WorldBank/stats/entities/dbpHierarchy.tsv"
ONTENT="data/DBpediaHierarchy_parent_child.tsv"
#TSV file with entity label, count and ontology type separated by TABs & LINES
#COUNTENT=$3
COUNTENT="data/en.dbpTypes.darkTypes.bank.tsv"
java -Xmx3000m -cp $LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar eu.newsreader.eventcoreference.output.DataSetEntityHierarchy --ont $ONTENT --ent $COUNTENT --title "DBpedia ontology of entities"

#TSV file with event label, count and ontology types separated by TABs & LINES
#COUNTEVENT=$4
#COUNTYPEVENT=$5
#COUNTEVENT="/Users/piek/Desktop/NWR-INC/WorldBank/stats/en.event.xls"
#COUNTYPEVENT="/Users/piek/Desktop/NWR-INC/WorldBank/stats/en.event.eso.xls"
COUNTEVENT="data/en_es.event.xls"
COUNTYPEVENT="data/en_es.event.eso.xls"
java -Xmx3000m -cp $LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar eu.newsreader.eventcoreference.output.DataSetEventHierarchy --eso $RESOURCES/ESO_Version2.owl --event $COUNTEVENT --type $COUNTYPEVENT --title "ESO ontology for events"

CITE="data/en_es.cited.xls"
AUTHOR="data/en_es.author.xls"
java -Xmx3000m -cp $LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar eu.newsreader.eventcoreference.output.DataSetSources --cite $CITE --author $AUTHOR --title "Sources"