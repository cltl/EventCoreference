DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/lib
RESOURCES="$( cd $ROOT && cd .. && pwd)"/vua-resources
DEMO="$( cd $ROOT && cd .. && pwd)"/Storyteller/UncertaintyVisualization/app/data/
# assumes vua-resources is installed next to this installation
# git clone https://github.com/cltl/vua-resources.git

ENTITY=$1
EVENT=$2
MERGE=$3
TIME=$4
ONT=$5
ONTGRAN=$6
ACTOR=$7
INTERSECT=$8
TOPIC=$9
CLIMAX=$10

echo "TIME GRANULARITY = $TIME. Options Y=year, M=month, W=week, D=day, N=time disabled"
echo "ACTION ONTOLOGY = $ONT. Options fn, eso, ili, any or N=ontology disabled"
echo "ACTION SIMILARITY THRESHOLD = $ONTGRAN."
echo "ACTOR COUNT = $ACTOR"
echo "ACTOR INTERSECT = $INTERSECT"
echo "TOPIC INTERSECT = $TOPIC"
echo "CLIMAX THRESHOLD = $CLIMAX"
echo "Entity = $ENTITY"
echo "Event = $EVENT"

java -Xmx4000m -cp $LIB/EventCoreference-v3.0-jar-with-dependencies.jar eu.newsreader.eventcoreference.storyline.TrigToJsonStoryPerspectives --raw-text "../../data/rawtext.idx"  --climax-level $CLIMAX   --ks-limit 500 --story-limit 500 --actors any $MERGE --time $TIME --action-ont $ONT --action-sim $ONTGRAN --actor-cnt $ACTOR --actor-intersect $INTERSECT --topic-level $TOPIC --ft "../../data/poll.data"  --blacklist "../../data/blacklist.txt" --eurovoc "$RESOURCES/mapping_eurovoc_skos.csv.gz" --eurovoc-blacklist "../../data/eurovoc-blacklist.txt" --project my-project  --service https://knowledgestore2.fbk.eu --ks "nwr/ks-node" --ks-user ks_user --ks-pass ks_passw --entity $ENTITY  --event $EVENT

# Command line example
#./storyteller-ks-entity.sh "Cameron" "eso:Buying" --merge W ili 5 5 2 50 50 "fn;eso"
 
cp "$ROOT/contextual.timeline.json" "$DEMO/contextual.timeline.json"

