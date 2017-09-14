DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/lib
EVENTS="$1"
PROJECT="$2"
RESOURCES="$( cd $ROOT && cd .. && pwd)"/vua-resources
# assumes vua-resources is installed next to this installation
# git clone https://github.com/cltl/vua-resources.git
# the software assume that the input files are in NAF format minially with the following layers:
# - tokens, terms, entities, coreference for events, srl, timeExpressions
# to create an event coreference layer, use the event-coreference scripts


java -Xmx2000m -cp "$LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar" eu.newsreader.eventcoreference.naf.ClusterEventObjects --naf-folder "$EVENTS" --event-folder "$EVENTS" --extension ".xml" --non-entities --project "$PROJECT" --all --eurovoc-en "$RESOURCES/mapping_eurovoc_skos.csv.gz" --source-frames "$RESOURCES/source.txt" --grammatical-frames "$RESOURCES/grammatical.txt" --contextual-frames "$RESOURCES/contextual.txt"

java -Xmx2000m -cp "$LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar" eu.newsreader.eventcoreference.naf.MatchEventObjects --event-folder "$EVENTS/events/contextualEvent" --match-type ililemma --roles "anyrole" --concept-match 30 --phrase-match 30 --hypers --lcs --ili "$RESOURCES//ili.ttl.gz"

java -Xmx2000m -cp "$LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar" eu.newsreader.eventcoreference.naf.MatchEventObjects --event-folder "$EVENTS/events/sourceEvent" --match-type ililemma --roles "a0" --concept-match 30 --phrase-match 30  --ili "$RESOURCES//ili.ttl.gz"

java -Xmx2000m -cp "$LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar" eu.newsreader.eventcoreference.naf.MatchEventObjects --event-folder "$EVENTS/events/grammaticalEvent" --match-type lemma --roles "a1" --phrase-match 30 --ili "$RESOURCES/ili.ttl.gz"

java -Xmx2000m -cp "$LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar" eu.newsreader.eventcoreference.naf.MatchEventObjects --event-folder "$EVENTS/events/futureEvent" --match-type lemma --roles "all" --phrase-match 30 --ili "$RESOURCES/ili.ttl.gz"
