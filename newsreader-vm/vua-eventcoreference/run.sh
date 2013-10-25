#### Within document event coreference baseline (lemma matches)
#java -Xmx812m -cp "./lib/EventCoreference-1.0-SNAPSHOT.jar":"./lib/KyotoKafSaxParser-1.0-jar-with-dependencies.jar" eu.newsreader.eventcoreference.naf.EventCorefLemmaBaseline --naf-file $1

java -Xmx812m -cp "./lib/EventCoreference-1.0-SNAPSHOT.jar":"./lib/KyotoKafSaxParser-1.0-jar-with-dependencies.jar" eu.newsreader.eventcoreference.naf.EventCorefLemmaBaseline
