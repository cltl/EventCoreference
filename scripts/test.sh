#!/bin/bash

FILES="../nafs_to_process/*.xml"
for f in $FILES ;
	do
	echo $f
	cat $f | java -Xmx2000m -cp ../target/EventCoreference-1.0-SNAPSHOT-jar-with-dependencies.jar eu.newsreader.eventcoreference.naf.GetSemFromNafStream --project cars --source-frames "../resources/source.txt" --grammatical-frames "../resources/grammatical.txt" --contextual-frames "../resources/contextual.txt" --non-entities --timex-max 5 | java -Xmx2000m -cp ../target/EventCoreference-1.0-SNAPSHOT-jar-with-dependencies.jar eu.newsreader.eventcoreference.naf.ProcessEventObjectsStream --contextual-match-type "ILILEMMA" --contextual-lcs --source-match-type "ILILEMMA" --source-roles "a0" --grammatical-match-type "LEMMA" --grammatical-roles "a1" --concept-match 25 --phrase-match 25 > ../trigs/$f.trig

	#mv $f;
	done
