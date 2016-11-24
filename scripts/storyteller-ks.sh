DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
#LIB=$ROOT/naf2sem/lib
LIB="/Code/vu/newsreader/EventCoreference/target"

RESOURCES=$ROOT/vua-resources
#DEMO=/dist/app/data/
DEMO=$ROOT/UncertaintyVisualization/app/data
# assumes vua-resources is installed next to this installation
# git clone https://github.com/cltl/vua-resources.git

MERGE="--no-merge"
TIME="W"
ONT="ili"
ONTGRAN="30"
ACTOR=1
INTERSECT=1
TOPIC=60
CLIMAX=1
ACTIONSCHEMA=eso
QUERY=$1
LIMIT=3000

#Dasym data
SERVER="http://130.37.53.45:50053"
#SERVER="http://145.100.58.136:50053"

KS="nwr/wikinews-new"
KSUSER="wikinews"
KSPASS="wikinews"

#echo "TIME GRANULARITY = $TIME. Options Y=year, M=month, W=week, D=day, N=time disabled"
#echo "ACTION ONTOLOGY = $ONT. Options fn, eso, ili, any or N=ontology disabled"
#echo "ACTION SIMILARITY THRESHOLD = $ONTGRAN."
#echo "ACTOR COUNT = $ACTOR"
#echo "ACTOR INTERSECT = $INTERSECT"
#echo "TOPIC INTERSECT = $TOPIC"
#echo "CLIMAX THRESHOLD = $CLIMAX"
#echo "ACTION SCHEMA = $ACTIONSCHEMA"
#echo "QUERY = $QUERY"

java -Xmx4000m -cp $LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar eu.newsreader.eventcoreference.storyline.QueryKnowledgeStoreToJsonStoryPerspectives --climax-level $CLIMAX --ks-limit $LIMIT --story-limit $LIMIT $MERGE --time $TIME --action-ont $ONT --action-sim $ONTGRAN --actor-cnt $ACTOR --actor-intersect $INTERSECT --topic-level $TOPIC --blacklist "data/blacklist.txt" --eurovoc "$RESOURCES/mapping_eurovoc_skos.csv.gz" --eurovoc-blacklist "data/eurovoc-blacklist-ft.txt" --project wikinews --action-schema "$ACTIONSCHEMA" --service $SERVER $QUERY --tokens "token2.index"
#
#--ks $KS --ks-user $KSUSER --ks-pass $KSPASS

cp "contextual.timeline.json" "$DEMO/brexit2/contextual.timeline.json"
