EventCoreference
================
version 3.0
Copyright: VU University Amsterdam, Piek Vossen
email: piek.vossen@vu.nl
website: www.newsreader-project.eu
website: cltl.nl

SOURCE CODE:
https://github.com/cltl/EventCoreference

INSTALLATION
1. git clone https://github.com/cltl/EventCoreference
2. cd EventCoreference
3. chmod +wrx install.sh
4. run the install.sh script

The install.sh will build the binary through apache-maven-2.2.1 and the pom.xml and move it to the "lib" folder.

REQUIREMENTS
EventCoreference is developed in Java 1.6 and can run on any platform that supports Java 1.6

LICENSE
    EventCoreference is free software: you can redistribute it and/or modify
    it under the terms of the The Apache License, Version 2.0:
        http://www.apache.org/licenses/LICENSE-2.0.txt.

    EventCoreference is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.


DESCRIPTION
Compares descriptions of events within and across documents to decide if they refer to the same events. For the within document mode, it creates an event-coreference layer within NAF. For cross document mode it creates RDF-TRiG according
to the SEM and GRASP model.


1. Within document event coreference:

Requires the presences of a SRL layer in NAF and for some scripts a term layer with wordnet synsets and a wordnet LMF resource.

The standard scripts for creating an event coreference layer inside the NAF file is:

- event-coreference-en.sh (for English texts)
- event-coreference-nl.sh (for Dutch texts)
- event-coreference-lemma.sh (any language)
- event-coreference-singleton.sh (any language)

The scripts for English and Dutch use a wordnet in LMF format for determining similarity across events and a list of FrameNet frames to determine which events qualify for event coreference. The lemma script only groups events with the same lemma (baseline) and the singleton script creates singleton coreference sets from all predicates in the SRL. These last two scripts do not require a wordnet and can be ran on any language.

Call any of the above scripts as follows:

cat ../example/wikinews_1173_en.naf | ./event-coreference-en.sh
cat ../example/wikinews_1173_nl.naf | ./event-coreference-nl.sh
cat ../example/wikinews_1173_en.naf | ./event-coreference-lemma.sh
cat ../example/wikinews_1173_en.naf | ./event-coreference-singleton.sh

The result of the with document event coreference is a NAF file where the coreference layer contains the event coreference sets.

The scripts can be adapted to run the functions with different settings by setting parameters. These parameters are explained in the usage documentation.


2. Cross document event coreference and conversion from NAF to SEM-GRaSP RDF

Another set of functions reads the NAF files and creates SEM-GRasSP TRiG files (RDF format). While doing this it converts the mention-based representations in NAF into instance based representation. There are two sets of functions:

2.1 Multiple document conversion

It reads NAF files and extracts Composite Events from each NAF to store them in cluster files as binary object data. Composite events are data structures that contain the action, all the participants and timex anchors relevant for the event. In a second step it reads the binary object data to compare the event descriptions. If identity is established the event descriptions are merged. If not they remain separate. At the end the results is serialized to SEM-GRaSP TRiG files, where each cluster folder gets a single TRiG file as a result. Multiple document conversion is typically used for large batches of NAF files. Scripts:

naf2sem-batch-cluster.sh
naf2sem-batch-nocluster.sh

The naf2sem-batch-cluster.sh script creates subfolders "contextualEvent", "sourceEvent", "grammaticalEvent" and "futureEvent". Within each of the folders, it creates temporal buckets with the events that have the same temporal anchoring. All events within a bucket are compared and a single TRiG file is created as output in each bucket.

The naf2sem-batch-nocluster.sh script creates a single cluster folder "all" and compare all events.

Call any of these scripts as follows:

./naf2sem-batch-cluster.sh ../example test
./naf2sem-batch-nocluster.sh ../example test

The scripts can be adapted to run the functions with different settings by setting parameters. These parameters are explained in the usage documentation.


2.2 Single document conversion

Instead of cross-document extraction, there is also a function that takes a NAF input streams and directly creates the SEM instance and GRaSP perspective representations. For each input stream an output stream is generates in RDF-TRiG format.
In a next step the RDF TRiG output stream can be send to a KnowledgeStore and the KnowledgeStore is queried for similar events.
For identical events owl:sameAs links are created that are also stored in the KnowledgeStore. This set up can be used for
an end-to-end streaming set up. Scripts:

naf2sem-grasp.sh
run_single_naf.sh

The naf2sem-grasp.sh script reads a NAF file and generates the RDF-TRiG output stream. Example usage:

cat ../example/wikinews_1173_en.naf.coref.xml | ./naf2sem-grasp.sh

The run_single_naf.sh script first call the GetSemFromNafStream function to get the RDF-TRiG output and then calls the KnowledgeStore
for population and determining equivalence with events in the KnowledgeStore. This script requires access to a runing KnowledgeStore.
Example usage:

cat ../example/wikinews_1173_en.naf.coref.xml | ./run_single_naf.sh

The scripts can be adapted to run the functions with different settings by setting parameters. These parameters are explained in the usage documentation.

REFERENCES:
P. Vossen, R. Agerri, I. Aldabe, A. Cybulska, M. van Erp, A. Fokkens, E. Laparra, A. Minard, A. P. Aprosio, G. Rigau, M. Rospocher, and R. Segers, “Newsreader: how semantic web helps natural language processing helps semantic web,” Special issue knowledge based systems, elsevier, to appear. 

M. Rospocher, M. van Erp, P. Vossen, A. Fokkens, I. Aldabe, G. Rigau, A. Soroa, T. Ploeger, and T. Bogaard, “Building event-centric knowledge graphs from news,” Journal of web semantics, 2016. 

See also:
http://kyoto.let.vu.nl/newsreader_deliverables/NWR-D5-1-3.pdf


