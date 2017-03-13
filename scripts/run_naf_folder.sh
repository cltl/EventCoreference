#!/bin/bash

extensions=(naf)

for ext in "${extensions[@]}"
do
	echo "Look for files with extension: $ext"
	if [ -e ../nafs_to_process/*.$ext ]; then
		echo "Files with extension $ext exist. Now processing them ..."
		for f in ../nafs_to_process/*.$ext ;
		do
			base=$(basename $f)
			echo "File: $base"
			cat $f | java -Xmx2000m -cp ../target/EventCoreference-v3.1.2-jar-with-dependencies.jar eu.newsreader.eventcoreference.naf.GetSemFromNafStream --project cars --source-frames "../resources/source.txt" --grammatical-frames "../resources/grammatical.txt" --contextual-frames "../resources/contextual.txt" --non-entities --timex-max 5 --ili ../resources/ili.ttl | java -Xmx2000m -cp ../target/EventCoreference-1.0-SNAPSHOT-jar-with-dependencies.jar eu.newsreader.eventcoreference.naf.ProcessEventObjectsStream --source-roles "pb\:A0,pb\:A1" --contextual-match-type "LEMMA" > "../trigs/$base.trig"
			echo "Trig created. Now inserting into the KS"
			wget -O /dev/null --post-file "../trigs/$base.trig" --header 'Content-type: application/x-trig' https://knowledgestore2.fbk.eu/nwr/aitor/custom/naf2sem 

			if [ $? -ne 0 ]; then
				echo "Error occurred while inserting into the KS. Please review the naf and the trig files..."
			else
				echo "Insertion done"
				mv $f ../nafs_processed/
			fi

		done
	else
		echo "No files with extension $ext exist in the directory ../nafs_to_process! "
	fi
done
