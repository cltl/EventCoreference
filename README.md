EventCoreference
================
version 3.1.2
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

The install.sh will build the binary through apache-maven-3.x and the pom.xml and move it to the "lib" folder.

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
EventCoreference is a package that contain 3 main functionalities:

1. NAF event-coreference: resolving event coreference within a single document in NAF format and adding event coreference sets to the coreferece layer in NAF
2. NAF to SEM-GRaSP RDF-TRiG: converting NAF files to SEM/GRaSP RDF-TRiG files in which events, participants and time are represented as instances with relations with pointers to their mentions in the text. 
Each mention of an instance or relation is enriched with information on the source and the source perspective. Possibly these functions can handle cross-document
event coreference: representing the event data across identicanl events across documents in a single instance representation.
3. SEM-GRaSP RDF-TRiG to Storyline JSON: creating JSON storylines from RDF data that can be rendered by the Storyteller visualisation package.

Possibly the 3 functions can be called in sequence, assuming that there is a collection of NAF files or a NAF stream with the necessary layers present:

NAF -<NAF-event-coreference>--> NAF -<NAF2SEM-GRaSP>--> RDF-TRiG -<RDF-TRiG2StoryLines>--> JSON -<StoryTeller>--> Browser Visualisation

Depending on the configuration, the processing can be done for single NAF files, a stream of NAF files, a batch folder with NAF files or through interaction with a KnowledgeStore repository in the back.
The same is true for processing RDF-TRiG data. Interaction with the KnowledgeStore requires population of the KnowledgeStore node with the NAF files anf the RDF-TRiG files.

We further describe the 3 main functions in more detail below.

1. NAF-event-coreference (within-document event-coreference):

Requires the presences of a SRL layer in NAF and for some scripts a term layer with wordnet synsets and a wordnet LMF resource.

The standard scripts for creating an event coreference layer inside the NAF file is:

- event-coreference-en.sh (for English texts)
- event-coreference-nl.sh (for Dutch texts)
- event-coreference-lemma.sh (any language)
- event-coreference-singleton.sh (any language)

The scripts for English and Dutch use a wordnet in LMF format for determining similarity across events and 
a list of FrameNet frames to determine which events qualify for event coreference. 
The lemma script only groups events with the same lemma (baseline) and the singleton script creates singleton coreference sets 
from all predicates in the SRL. These last two scripts do not require a wordnet and can be ran on any language.

Call any of the above scripts as follows:

cat ../example/wikinews_1173_en.naf | ./event-coreference-en.sh
cat ../example/wikinews_1173_nl.naf | ./event-coreference-nl.sh
cat ../example/wikinews_1173_en.naf | ./event-coreference-lemma.sh
cat ../example/wikinews_1173_en.naf | ./event-coreference-singleton.sh

The result of the with document event coreference is a NAF file where the coreference layer contains the event coreference sets.

The scripts can be adapted to run the functions with different settings by setting parameters. These parameters are explained in the usage documentation.

2. Conversion from NAF to SEM-GRaSP RDF-TRiG and cross-document event-coreference

Another set of functions reads the NAF files and creates SEM-GRasSP TRiG files (RDF format). 
While doing this it converts the mention-based representations in NAF into instance-based representations. There are two sets of functions:

2.1 Multiple NAF document conversion

It reads a batch of NAF files and extracts Composite Events from each NAF to store them in cluster files as binary object data. 
Composite events are data structures that contain the action, all the participants and timex anchors relevant for the event. 
In a second step it reads the binary object data to compare the event descriptions. 
If identity is established the event descriptions are merged. 
If not they remain separate. At the end the results is serialized to SEM-GRaSP TRiG files, 
where each cluster folder gets a single TRiG file as a result. 
Multiple document conversion is typically used for large batches of NAF files. Scripts:

naf2sem-batch-cluster.sh
naf2sem-batch-nocluster.sh

The naf2sem-batch-cluster.sh script creates subfolders "contextualEvent", "sourceEvent", "grammaticalEvent" and "futureEvent". Within each of the folders, it creates temporal buckets with the events that have the same temporal anchoring. All events within a bucket are compared and a single TRiG file is created as output in each bucket.

The naf2sem-batch-nocluster.sh script creates a single cluster folder "all" and compare all events.

Call any of these scripts as follows:

./naf2sem-batch-cluster.sh ../example test
./naf2sem-batch-nocluster.sh ../example test

The scripts can be adapted to run the functions with different settings by setting parameters. 
These parameters are explained in the usage documentation.


2.2 Single document conversion

* Note: Make sure that you have your KnowledgeStore (KS) docker set up prior to running this. You can get the docker from https://github.com/dkmfbk/knowledgestore-docker/. Once your KS docker is running, update the script with the correct KS endpoint for NAF2SEM, following the pattern: $YOUR_SERVER_URL/custom/naf2sem . *

Instead of cross-document extraction, there is also a function that takes a single NAF input stream or file and directly creates the SEM instance and GRaSP perspective representations. 
For each input stream an output stream is generates in RDF-TRiG format (similar for single files)
In a next step the RDF TRiG output stream can be send to a KnowledgeStore (direct population of the RDF-TRiG) and the KnowledgeStore is queried for similar events.
For identical events owl:sameAs links are created that are also stored in the KnowledgeStore. This set up can be used for
an end-to-end streaming set up. Scripts:

naf2sem-grasp.sh (file or stream)
run_single_naf.sh (NAF stream and direct interaction with the KnowledgeStore)

The naf2sem-grasp.sh script reads a NAF file and generates the RDF-TRiG output stream. Example usage:

cat ../example/wikinews_1173_en.naf.coref.xml | ./naf2sem-grasp.sh

The run_single_naf.sh script first call the GetSemFromNafStream function to get the RDF-TRiG output and then calls the KnowledgeStore
for population and determining equivalence with events in the KnowledgeStore. This script requires access to a runing KnowledgeStore.
Example usage:

cat ../example/wikinews_1173_en.naf.coref.xml | ./run_single_naf.sh

The scripts can be adapted to run the functions with different settings by setting parameters. 
These parameters are explained in the usage documentation.

3. SEM-GRaSP RDF-TRiG to Storyline JSON
The third set of functions takes RDF-TRiG as an input and creates a JSON file with storyline data that can be visualised in the Storyteller interface.
Storylines are groupings of events on a timeline based on bridging relations. Bridging relations can be based on co-participation, causal relations
or topical relations. Events are further scored on the basis of their significance, which is reflected by a climax score. The more often an event
is mentioned in the more prominent positions, the higher the climax score. Sequences of bridged events shows a built up in climax score towards
the most significant event, followed by a fade out phase. The Storyteller interface shows these structures in combination with the perspective of the sources
of the information.

There are two modes of running these functions: taking a batch or RDF-TRiG files and NAF source files as input OR querying a KnowledgeStore that contains
both the RDF data and the NAF source files. We will dicuss each of these modes in more detail with the corresponding scripts. 
The scripts all use the same main function (eu.newsreader.eventcoreference.storyline.TrigToJsonStoryPerspectives)
but with different parameters.

All the functions generate a file "contextual-timeline.json" that is copied to the data folder in the Storyteller installation. This location is defined
in each script and can be adapted for any local configuration. To install the Storyteller locally you need to install it from:

git clone https://github.com/NLeSC/UncertaintyVisualization

and follow the instruction in the README. If Storyteller is correctly installed and started using "grunt serve" it loads the JSON file from the
location that is specified in the file constants.js:

UncertaintyVisualization/app/scripts/core/constants.js

You may need to adapt the line:

      DATA_JSON_URL: 'file:data/contextual.timeline.json'
      
to make the Storyteller load your file.

After pushing the new output file to the right location, you need to reload the interface in the browser to see the new data. Due to caching, you may need
to restart the browser or re-launch the server to get the new results. Note that large data files need a log time to load and also lead to cluttering of the visualisation and low performance.

It is therefore better to controll the amount of data through querying and setting the thresholds.

3.1 File interaction
In the case the storylines are built from a batch of local RDF-TRiG files, the software assumes 
that an index is built from the original text in the NAF files so that mentions of events can be resolved by highlighting in the text.
In addition to the TRiG file, this mode expects a raw text index file with the URI of the source and the raw text as was presented in the
original NAF file. The index needs to be created first with the script:

    storyteller-rawtext-index.sh <folder-with-naf-files> <extension of the files, e.g. ".naf"> <name of the project>
 
This script requires 3 parameters: 1) the path to the folder with the naf files, 2) the extension of the naf files , e.g. ".naf" or ".xml" 
and 3) the name of project used to create the original RDF-TRiG files. The latter parameter is needed to make sure that the mention references
in the RDF-TRiG file are compliant with the URIs in the index.

The script generates a file with the name <rawtext.idx> in the input folder. This file can be placed anywhere and is used in the next script to create the
JSON storylines:
   
    storyteller-trig-files.sh <taking 10 parameters> 
    
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
 
The storyteller script takes 10 parameters, of which the first is the path to the folder with the RDF-TRiG files. 
In this folder, it creates a file with the name "contextual.timeline.json", which is copied to the data folder of
the Storyteller demo that is installed. You can specify the target location of your local installation in the script.

The other parameters are used to fine-tune the storylines and filter the data.

MERGE
    applies cross-document event-coreference on the output using the parameters TIME and ONT    
    values: --merge or --no-merge
TIME
    in combination with MERGE, defines the granularity of the time boundaries to compare different events by year, month, week or day, or not to consider the time
    values: Y=year, M=month, W=week, D=day, N=time disabled"
ONT
    in combination with MERGE, defines the ontology used to determine the granularity of the action similarity
    values: fn, eso, ili, any or N=ontology disabled"
ONTGRAN
    in combination with MERGE and ONT, defines the proportional match across actions using labels from the ontology to be sufficiently similar for merging
    
ACTOR
    sets a threshold for the frequency of the actors involved in the events. Actors that are too infrequent are removed from the data 
    and events without actors are also removed
    values: integer zero or higher
INTERSECT
    number of intersecting actors required to group events in a story, typically set to 1 or 2
    values: 0, 1, 2
TOPIC
    proportion of topic labels that needs to be matched across events to be grouped in a story
    values: between 0 and 100
CLIMAX
    threshold for the climax score of an event to be included in the data. Values need to be above 0 and value 100 only selects to top story.
    values: between 1 and 100
ACTIONSCHEMA
    list of ontology classes that need to be present for events to be included in the data separated with ";", 
    e.g. "fn;eso" means events need to have a FrameNet type or an ESO type to be included.
    values: ili (wordnet); fn (FrameNet; eso

An example of calling this script is shown here:

    storyteller-trig-files.sh ../../data --merge W ili 5 5 2 50 50 "fn;eso"


3.2 KnowledgeStore interaction
When iteracting with a KnowledgeStore it is not necessary to build a raw text index since the raw text layer can be retrieved from the KnowledgeStore itself.
However for some purposes or constellations it may be more efficient to use a local raw text index to retrieve the text snippets. This may ne becessary if
retrieving the source documents from the KnowledgeStore is too slow. The index file can be built as described for 3.1 and when calling the KnowledgeStore
script you can either specify the raw text index file or leave it out. In the latter case, the source text is retrieved from the KnowledgeStore.

We provide 4 different scripts to create a JSON storylines through querying the KnowledgeStore:

    storyteller-ks-entity           query=Substring matching with a surface form of an entity participating in an event
    storyteller-ks-event            query=event ontology class, e.g. eso:Buying or fn:Buying
    storyteller-ks-entity-event     requires both an entity and event query
    storyteller-ks-sparql           query is a sparql request in the proper format

The four scripts calls the same main function eu.newsreader.eventcoreference.storyline.TrigToJsonStoryPerspectives but with slightly different parameters and effects.
All the parameters described before can also be used for the interaction with the KnowledgeStore except for the INPUT parameter.
Instead of a path to the folder with RDF-TRiG files, a query needs to passed as parameter. In the KnowledgeStore scripts, we have added
extra parameters that point to the address of the KnowledgeStore SPARQL end point, provide the correct username and password:

    --service "url of the service"
    --ks "nwr/ks-node"
    --ks-user ks_user
    --ks-pass ks_passw

To run the scripts you need to specify:

    - a known KnowledgeStore service address, ,e.g.  --service https://knowledgestore2.fbk.eu, 
    - a database available at this address e.g. --ks "/nwr/wikinews"
    - a username, e.g. --ks "my user name"
    - a password, e.g. --ks-pass "my pass word"
    
Please consult a KnowledgeStore manager for the credentials. If you leave out the specifications to the service point and database, the query is
submitted by default to the wikinews database (English) store at FBK in Trento. 

To avoid overloading system and since the demo can only show a limited amount of data you can set a limit on the number of events extracted from
the KnowledgeStore (this is a hard limit on the SPARQL query) as well as a limit on the events in the storyline:

    --ks-limit 500
    --story-limit 500

The JSON result file is stored in the same folder as the scripts and copied to the specified DEMO folder in the script file.

storyteller-ks-entity
In the case of the entity script, the query is passed to the --entity parameter as a value 
and extracts all events in which an entity is involved with a surface form that matches the query as a substring.
A query such as "Cameron" will thus match entities for which have "Cameron" as one of its surface forms or in which it is a substring.
This query is case-sensitive.

An example of calling this script is shown here:

    storyteller-ks-entity Cameron --merge W ili 5 5 2 50 50 "fn;eso"


storyteller-ks-event
In the case of the event script, the query should contain an event TYPE or CLASS, where the ontology should be given as a name space prefix,
which is passed in as a the first parameter and assigned to the --event parameter of the Java command line API. Currently for the name space prefixes, we support "ili", "eso" and "fn" as event ontologies,
where "ili" requires a concept in the the GlobalWordnet InterlingualIndex, "eso" a class from the ESO ontology, "fn" a frame from FrameNet. 
A query thus represents a combination of the namespace, followed by ":" and the type, as in "ili:i25092", "eso:Buying", "fn:Selling". 
Note that the name space also defines the value for the ACTIONSCHEMA since we already restrict the events to a specific
ontology type. The ACTIONSCHEMA parameter should therefore not be used when querying for types of events.

An example of calling this script is shown here:

    storyteller-ks-event "eso:Buying" --merge W ili 5 5 2 50 50

storyteller-ks-entity-event
In this script, the first parameter is the entity surface form query and the second parameter is the event type query. All other parameters follow
in the order given above. Again the ACTIONSCHEMA should be left out since it is covered by the name space of the event type.

An example of calling this script is shown here:

    storyteller-ks-entity-event Cameron eso:Buying --merge W ili 5 5 2 50 50


storyteller-ks-sparql
This script should only be used if you know what you are doing and you have tested the query using the KnowledgeStore UI. In this case, a SPARQL query
is given as the first parameter of the script, which is given as as value for the --sparql parameter of the Java command line API.
The same function will now send the SPARQL to the KnowledgeStore and deal with the output as the query defined it. If the output contains
events this may result in a storyline, if not we cannot create a story. All the other parameters stay the same and are applied on top of the
output of the SPARQL query.

    storyteller-ks-sparql "my sparql" --merge W ili 5 5 2 50 50 "fn;eso"


REFERENCES:
P. Vossen, R. Agerri, I. Aldabe, A. Cybulska, M. van Erp, A. Fokkens, E. Laparra, A. Minard, A. P. Aprosio, G. Rigau, M. Rospocher, and R. Segers, “Newsreader: how semantic web helps natural language processing helps semantic web,” Special issue knowledge based systems, elsevier, to appear. 

M. Rospocher, M. van Erp, P. Vossen, A. Fokkens, I. Aldabe, G. Rigau, A. Soroa, T. Ploeger, and T. Bogaard, “Building event-centric knowledge graphs from news,” Journal of web semantics, 2016. 

See also:
http://kyoto.let.vu.nl/newsreader_deliverables/NWR-D5-1-3.pdf


