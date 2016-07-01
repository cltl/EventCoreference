DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/lib
RESOURCES="$( cd $ROOT && cd .. && pwd)"/vua-resources
DEMO="$( cd $ROOT && cd .. && pwd)"/Storyteller/UncertaintyVisualization/app/data/
FOLDER=$1
EXTENSION=$2
PROJECT=$3
# assumes vua-resources is installed next to this installation
# git clone https://github.com/cltl/vua-resources.git

java -Xmx2000m -cp $LIB/EventCoreference-v3.0-jar-with-dependencies.jar eu.newsreader.eventcoreference.naf.RawTextIndex --naf-folder "$FOLDER" --extension "$EXTENSION" --project "$PROJECT"



