DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
LIB="$ROOT"/lib

#pass naf file as input stream and catch the naf output stream
# for example> "cat example-naf.xml | event-coreference-lemma.sh > example-naf.coref.xml"

java -Xmx812m -cp "$LIB/EventCoreference-v3.1.2-jar-with-dependencies.jar" eu.newsreader.eventcoreference.naf.EventCorefLemmaBaseline

