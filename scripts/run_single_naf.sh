#!/bin/bash

f="$1"

if [ -z $1 ]; then
	echo "Please specify a file. Exiting now..."
	exit
fi

base=$(basename $f)
echo "File to process: $base"
cat $f | java -Xmx2000m -cp ../target/EventCoreference-v3.1.2-jar-with-dependencies.jar eu.newsreader.eventcoreference.naf.GetSemFromNafStream --project end-to-end --source-frames "../resources/source.txt" --grammatical-frames "../resources/grammatical.txt" --contextual-frames "../resources/contextual.txt" --non-entities --perspective --ili ../resources/ili.ttl.gz --eurovoc-en "../resources/mapping_eurovoc_skos.csv" | java -Xmx2000m -cp ../target/EventCoreference-3.0-jar-with-dependencies.jar eu.newsreader.eventcoreference.naf.ProcessEventObjectsStream --source-roles "pb\:A0,pb\:A1" --contextual-match-type "ILILEMMA" --ks https://myknowledgestore --user me --passw mypassw > "../trigs/$base.trig"

echo "Trig created. Now inserting into the KS"
wget -O /dev/null --post-file "../trigs/$base.trig" --header 'Content-type: application/x-trig' https://knowledgestore2.fbk.eu/nwr/aitor/custom/naf2sem 
if [ $? -ne 0 ]; then
	echo "Error occurred while inserting into the KS. Please review the naf and the trig files..."
else
	echo "Insertion done"
	mv $f ../nafs_processed/
fi
