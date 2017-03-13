DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/lib
RESOURCES="$( cd $ROOT && cd .. && pwd)"/vua-resources
DEMO="$( cd $ROOT && cd .. && pwd)"/Storyteller/UncertaintyVisualization/app/data/
# assumes vua-resources is installed next to this installation
# git clone https://github.com/cltl/vua-resources.git

INPUT=$1
MERGE=$2
TIME=$3
ONT=$4
ONTGRAN=$5
ACTOR=$6
INTERSECT=$7
TOPIC=$8
CLIMAX=$9
ACTIONSCHEMA=$10

echo "TIME GRANULARITY = $TIME. Options Y=year, M=month, W=week, D=day, N=time disabled"
echo "ACTION ONTOLOGY = $ONT. Options fn, eso, ili, any or N=ontology disabled"
echo "ACTION SIMILARITY THRESHOLD = $ONTGRAN."
echo "ACTOR COUNT = $ACTOR"
echo "ACTOR INTERSECT = $INTERSECT"
echo "TOPIC INTERSECT = $TOPIC"
echo "CLIMAX THRESHOLD = $CLIMAX"
echo "ACTION SCHEMA = $ACTIONSCHEMA"

java -Xmx4000m -cp $LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar eu.newsreader.eventcoreference.storyline.TrigToJsonStoryPerspectives --trig-folder "$INPUT" --raw-text "../../data/rawtext.idx"  --climax-level $CLIMAX  --story-limit 50000 --actors any $MERGE --time $TIME --action-ont $ONT --action-sim $ONTGRAN --actor-cnt $ACTOR --actor-intersect $INTERSECT --topic-level $TOPIC --ft "../../data/poll.data"  --blacklist "../../data/blacklist.txt" --eurovoc "$RESOURCES/mapping_eurovoc_skos.csv.gz" --eurovoc-blacklist "../../data/eurovoc-blacklist.txt" --project my-project --action-schema "$ACTIONSCHEMA"

cp "$INPUT/contextual.timeline.json" "$DEMO/contextual.timeline.json"

# Command line example
#./trig-story.sh ../../data --merge W ili 5 5 2 50 50 "fn;eso"

