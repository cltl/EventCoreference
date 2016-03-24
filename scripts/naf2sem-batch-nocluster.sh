DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/lib
EVENTS="$1"
PROJECT="$2"
RESOURCES="$( cd $ROOT && cd .. && pwd)"/vua-resources
# assumes vua-resources is installed next to this installation
# git clone https://github.com/cltl/vua-resources.git

java -Xmx2000m -cp "$LIB/EventCoreference-v3.0-jar-with-dependencies.jar" eu.newsreader.eventcoreference.naf.NoClusterEventObjects --naf-folder "$EVENTS" --event-folder "$EVENTS" --extension ".xml" --non-entities --project "$PROJECT" --all --eurovoc-en "$RESOURCES/mapping_eurovoc_skos.csv.gz"

java -Xmx2000m -cp "$LIB/EventCoreference-v3.0-jar-with-dependencies.jar" eu.newsreader.eventcoreference.naf.MatchEventObjects --event-folder "$EVENTS"  --match-type ililemma --roles "anyrole" --concept-match 30 --phrase-match 30 --ili  "$RESOURCES/ili.ttl.gz"  --hypers --lcs

