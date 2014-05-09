package eu.newsreader.eventcoreference.naf;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.util.Util;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/30/13
 * Time: 7:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class GetSemFromNafFile {

    /* @TODO
    - namespace http://www.newsreader-project.eu/data/cars
    - namespace http://www.newsreader-project.eu/authors
    - namespace http://www.newsreader-project.eu/publishers

        - read meta file, field 6 and 9
        - 6 = author
        - 9 = publisher

            <http://www.newsreader-project.eu/2003_1_1_47VH-FG40-010D-Y403.xml#pr1,rl1>
            gaf:denotedBy  <http://www.newsreader-project.eu/data/car/2003_1_1/47VH-FG40-010D-Y403.xml#rl1> ,
http://www.newsreader-project.eu/2003/10/10/49RC-4970-018S-21S2.xml	49RC-4970-018S-21S2	2003-10-10	WHEELS; Pg. D9	The Record (Kitchener-Waterloo, Ontario)	GREG SCHNEIDER		U.S. auto companies get low marks in new survey	Copyright 2003 Metroland Media Group Ltd	3315


           <http://www.newsreader-project.eu/2003_1_1_47VH-FG40-010D-Y403.xml#pr1,rl1>
            prov:wasAttributedTo  <http://www.newsreader-project.eu/authors/GREG_SCHNEIDER> ,
            <http://www.newsreader-project.eu/publisher/Metroland_Media_Group_Ltd> .


        - for each factuality statement that is not CT+, intersect with event word tokens

        <factvalue confidence="0.8264118229513245" id="w71" prediction="CT+"/>

        <http://www.newsreader-project.eu/2003_1_1_47VH-FG40-010D-Y403.xml#pr1,rl1>
            nwr:hasFactBankValue  <http://www.newsreader-project.eu/values/CT+> .
    */

    static SemTime docSemTime = new SemTime();
    static OwlTime docOwlTime = new OwlTime();

    static final public String ID_SEPARATOR = "#";
    static final public String URI_SEPARATOR = "_";

    static public void processNafFile (String project, KafSaxParser kafSaxParser,
                                       ArrayList<SemObject> semEvents,
                                       ArrayList<SemObject> semActors,
                                       ArrayList<SemObject> semPlaces,
                                       ArrayList<SemObject> semTimes,
                                       ArrayList<SemRelation> semRelations,
                                       ArrayList<SemRelation> factRelations
    ) {
        //String baseUrl = ResourcesUri.nwr+kafSaxParser.getKafMetaData().getUrl().replace("/", URI_SEPARATOR)+ID_SEPARATOR;
        String baseUrl = kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
        if (!baseUrl.toLowerCase().startsWith("http")) {
            baseUrl = ResourcesUri.nwrdata + project + "/" + kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
        }
        processNafFileForActorPlaceInstances(baseUrl, kafSaxParser, semActors, semPlaces);
        processNafFileForTimeInstances(baseUrl, kafSaxParser, semTimes);
        processNafFileForEventInstances(baseUrl, kafSaxParser, semEvents);
        processNafFileForRelations(baseUrl, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations);
    }


    static public void processNafFileForEntities (String project, KafSaxParser kafSaxParser,
                                                  ArrayList<SemObject> semActors,
                                                  ArrayList<SemObject> semPlaces,
                                                  ArrayList<SemObject> semTimes
    ) {
        String baseUrl = kafSaxParser.getKafMetaData().getUrl()+ID_SEPARATOR;
        if (!baseUrl.toLowerCase().startsWith("http"))     {
            baseUrl = ResourcesUri.nwrdata+project+"/"+kafSaxParser.getKafMetaData().getUrl()+ID_SEPARATOR;
        }
        processNafFileForActorPlaceInstances(baseUrl, kafSaxParser, semActors, semPlaces);
        processNafFileForTimeInstances(baseUrl, kafSaxParser, semTimes);
    }

    static public void processNafFileForEventInstances (String baseUrl, KafSaxParser kafSaxParser,
                                                   ArrayList<SemObject> semEvents

    ) {


        /**
         * Event instances
         */

        for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
            KafCoreferenceSet coreferenceSet = kafSaxParser.kafCorefenceArrayList.get(i);
            //// these mentions are too coarse and need to intersect with the SRL elements!!!!!
            ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayList(baseUrl, kafSaxParser, coreferenceSet.getSetsOfSpans());
            KafSense sense = new KafSense();
            sense.setRefType("corefType");
            sense.setSensecode(coreferenceSet.getType());
            if (coreferenceSet.getType().equalsIgnoreCase("event")) {
                SemEvent semEvent = new SemEvent();
                semEvent.setId(baseUrl+coreferenceSet.getCoid());
                semEvent.setNafMentions(mentionArrayList);
                semEvent.addPhraseCountsForMentions(kafSaxParser);
                semEvent.setFactuality(kafSaxParser);
                semEvent.setConcept(Util.getExternalReferencesSrlEvents(kafSaxParser, coreferenceSet));
                semEvent.setIdByDBpediaReference();
                semEvents.add(semEvent);
            }
        }

    }


    static public void processNafFileForActorPlaceInstances (String baseUrl, KafSaxParser kafSaxParser,
                                       ArrayList<SemObject> semActors,
                                       ArrayList<SemObject> semPlaces
    ) {


   /*    @TODO
          - iterate over all entities
            - merge entities with the same URI: entity recognition is mention based
          - iterate over all sets of entities
            - expand the spans using the coreference sets
          - create a single SemObject for an entity set with extended spans. All the spans are used for creating mentions
          - add the URIs
          - later we add the URIs from the SRLs
            */
        /*
           - We first create a HashMap for each URI in the entity layer pointing to all the entities that have the same URI.
           - If the URI is not there as an external reference, we take the tokenString reference
           - Note that the same URI can be typed as a location, person, organization or misc. If it is a location we store it in the location HashMap
             otherwise it is stored in the actor HashMap
         */
        HashMap<String, ArrayList<KafEntity>> kafEntityLocationUriMap = new HashMap<String, ArrayList<KafEntity>>();
        HashMap<String, ArrayList<KafEntity>> kafEntityActorUriMap = new HashMap<String, ArrayList<KafEntity>>();
        for (int j = 0; j < kafSaxParser.kafEntityArrayList.size(); j++) {
            KafEntity kafEntity = kafSaxParser.kafEntityArrayList.get(j);
            String uri = kafEntity.getFirstUriReference();
            if (uri.isEmpty()) {
                kafEntity.setTokenStrings(kafSaxParser);
                uri = Util.cleanUri(kafEntity.getTokenString());

               // System.out.println("uri = " + uri);
            }
            if (!uri.isEmpty()) {
                if (kafEntity.getType().equalsIgnoreCase("location")) {
                    if (kafEntityLocationUriMap.containsKey(uri)) {
                        ArrayList<KafEntity> entities = kafEntityLocationUriMap.get(uri);
                        entities.add(kafEntity);
                        kafEntityLocationUriMap.put(uri, entities);
                    }
                    else {
                        ArrayList<KafEntity> entities = new ArrayList<KafEntity>();
                        entities.add(kafEntity);
                        kafEntityLocationUriMap.put(uri, entities);
                    }
                }
                else {
                    /// type is person, organisation, or misc. New types may be added by the NERC module in the future
                    if (kafEntityActorUriMap.containsKey(uri)) {
                        ArrayList<KafEntity> entities = kafEntityActorUriMap.get(uri);
                        entities.add(kafEntity);
                        kafEntityActorUriMap.put(uri, entities);
                    }
                    else {
                        ArrayList<KafEntity> entities = new ArrayList<KafEntity>();
                        entities.add(kafEntity);
                        kafEntityActorUriMap.put(uri, entities);
                    }
                }
            }
        }


        Set keySet = kafEntityLocationUriMap.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String uri = (String) keys.next();
            ArrayList<KafEntity> entities = kafEntityLocationUriMap.get(uri);
            ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListFromEntitiesAndCoreferences(baseUrl, kafSaxParser, entities);
            String entityId = "";
            for (int i = 0; i < entities.size(); i++) {
                KafEntity kafEntity = entities.get(i);
                entityId += kafEntity.getId();
            }
            SemPlace semPlace = new SemPlace();
            semPlace.setId(baseUrl + entityId);
            semPlace.setNafMentions(mentionArrayList);
            semPlace.addPhraseCountsForMentions(kafSaxParser);
            semPlace.addConcepts(Util.getExternalReferences(entities));
            semPlace.setIdByDBpediaReference();
            semPlaces.add(semPlace);
/*            if (semPlace.getURI().endsWith("Brazil")) {
                System.out.println("semActor.getId() = " + semPlace.getId());
                System.out.println("semActor.getNafMentions().toString() = " + semPlace.getNafMentions().toString());
            }*/

        }

        keySet = kafEntityActorUriMap.keySet();
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String uri = (String) keys.next();
         //   System.out.println("actor uri = " + uri);
            ArrayList<KafEntity> entities = kafEntityActorUriMap.get(uri);
            ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListFromEntitiesAndCoreferences(baseUrl, kafSaxParser, entities);
            String entityId = "";
            for (int i = 0; i < entities.size(); i++) {
                KafEntity kafEntity = entities.get(i);
                entityId += kafEntity.getId();
            }
            SemPlace semActor = new SemPlace();

/*
            KafSense sense = new KafSense();
            sense.setRefType("corefType");
            sense.setSensecode("actor");
            semActor.addConcept(sense);
*/

            semActor.setId(baseUrl + entityId);
            semActor.setNafMentions(mentionArrayList);
            semActor.addPhraseCountsForMentions(kafSaxParser);
            semActor.addConcepts(Util.getExternalReferences(entities));
            semActor.setIdByDBpediaReference();
            semActors.add(semActor);
/*            if (semActor.getURI().endsWith("Brazil")) {
                System.out.println("semActor.getId() = " + semActor.getId());
                System.out.println("semActor.getNafMentions().toString() = " + semActor.getNafMentions().toString());
            }*/

        }


        /*
            - We are missing actors in predicates and coreference sets that are not entities
            - iterate over the SRL for roles with particular labels: A0, A1, A2 and LOC
            - get the span:
            - check all actors for span match or span head match
            - if none create a new actor or place
         */
        HashMap<String, ArrayList<ArrayList<CorefTarget>>> locationReference = Util.getLocationMentionsHashMapFromSrl(kafSaxParser);
        keySet = locationReference.keySet();
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();  /// role id from srl
            ArrayList<ArrayList<CorefTarget>> corefTargetArrayList = locationReference.get(key);
            SemPlace semPlace = new SemPlace();
            semPlace.setId(baseUrl + key);
            ArrayList<NafMention> mentions = Util.getNafMentionArrayList(baseUrl, kafSaxParser, corefTargetArrayList);
            semPlace.setNafMentions(mentions);
            semPlace.addPhraseCountsForMentions(kafSaxParser);
            semPlace.addConcepts(Util.getExternalReferencesSrlParticipants(kafSaxParser, key));
            semPlace.setIdByDBpediaReference();
            if (!Util.hasObject(semPlaces, semPlace)) {
             //   System.out.println("adding mentions = " + mentions);
                semPlaces.add(semPlace);
            }
            else {
             //   System.out.println("skipping mentions = " + mentions);
            }
        }

        HashMap<String, ArrayList<ArrayList<CorefTarget>>> actorReferences = Util.getActorMentionsHashMapFromSrl(kafSaxParser);
        keySet = actorReferences.keySet();
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();  /// role id from srl
            ArrayList<ArrayList<CorefTarget>> corefTargetArrayList = actorReferences.get(key);
            SemActor semActor = new SemActor();
            semActor.setId(baseUrl + key);
            ArrayList<NafMention> mentions = Util.getNafMentionArrayList(baseUrl, kafSaxParser, corefTargetArrayList);
            semActor.setNafMentions(mentions);
            semActor.addPhraseCountsForMentions(kafSaxParser);
            semActor.addConcepts(Util.getExternalReferencesSrlParticipants(kafSaxParser, key));
            semActor.setIdByDBpediaReference();
            if (!Util.hasObject(semActors, semActor)) {
                //System.out.println("adding mentions = " + mentions);
                semActors.add(semActor);
            }
            else {
                //System.out.println("skipping mentions = " + mentions);
            }
        }
    }


    static public void processNafFileForTimeInstances (String baseUrl, KafSaxParser kafSaxParser,
                                       ArrayList<SemObject> semTimes
    ) {

       // System.out.println("kafSaxParser.getKafMetaData().getCreationtime() = " + kafSaxParser.getKafMetaData().getCreationtime());
        if (!kafSaxParser.getKafMetaData().getCreationtime().isEmpty()) {
            //// we first store the publication date as a time
            docSemTime = new SemTime();
/*            KafSense dateSense = new KafSense();
            dateSense.setRefType("publication date");
            dateSense.setSensecode(kafSaxParser.getKafMetaData().getCreationtime());
            docSemTime.addConcept(dateSense);*/


            docSemTime.setId(baseUrl + "nafHeader" + "_" + "fileDesc" + "_" + "creationtime");
            docSemTime.addPhraseCounts(kafSaxParser.getKafMetaData().getCreationtime());
            NafMention mention = new NafMention(baseUrl + "nafHeader" + "_" + "fileDesc" + "_" + "creationtime");
            docSemTime.addMentionUri(mention);
            docOwlTime = new OwlTime();
            docOwlTime.parsePublicationDate(kafSaxParser.getKafMetaData().getCreationtime());
        }

        for (int i = 0; i < kafSaxParser.kafTimexLayer.size(); i++) {
            KafTimex timex = kafSaxParser.kafTimexLayer.get(i);
            if (!timex.getValue().isEmpty()) {
               //System.out.println("timex.getValue() = " + timex.getValue());
                OwlTime aTime =new OwlTime();
                if (aTime.parseTimeExValue(timex.getValue(), docOwlTime)>-1) {
                    ArrayList<String> tokenSpanIds = timex.getSpans();
                    ArrayList<String> termSpanIds = kafSaxParser.covertTokensSpanToTermSPan(tokenSpanIds);
                    ArrayList<NafMention> mentions = Util.getNafMentionArrayListForTermIds(baseUrl, kafSaxParser, termSpanIds);
                    SemTime semTimeRole = new SemTime();
                    semTimeRole.setId(baseUrl + timex.getId());
                    semTimeRole.setNafMentions(mentions);
                    semTimeRole.addPhraseCountsForMentions(kafSaxParser);
                    semTimeRole.setOwlTime(aTime);
                    semTimes.add(semTimeRole);
                }
            }
        }


        //// we get time references from the SRL layer
        HashMap<String, ArrayList<ArrayList<CorefTarget>>> timeReferences = Util.getTimeMentionsHashMapFromSrl (kafSaxParser);
        Set keySet = timeReferences.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            OwlTime aTime =new OwlTime();
            if (aTime.parseStringDateWithDocTimeYearFallBack(key, docOwlTime)>-1) {
                // System.out.println("key = " + key);
                ArrayList<ArrayList<CorefTarget>> corefTargetArrayList = timeReferences.get(key);
                SemTime semTimeRole = new SemTime();
                semTimeRole.setId(baseUrl + Util.cleanUri(key));
                ArrayList<NafMention> mentions = Util.getNafMentionArrayList(baseUrl, kafSaxParser, corefTargetArrayList);
                semTimeRole.setNafMentions(mentions);
                semTimeRole.addPhraseCountsForMentions(kafSaxParser);
                semTimeRole.setOwlTime(aTime);
                if (!Util.hasObject(semTimes, semTimeRole)) {
                    semTimes.add(semTimeRole);
                }
            }
            else {
            }
            //System.out.println("aTime.toString() = " + aTime.toString());
        }
    }



    static public void processNafFileForRelations (String baseUrl, KafSaxParser kafSaxParser,
                                       ArrayList<SemObject> semEvents,
                                       ArrayList<SemObject> semActors,
                                       ArrayList<SemObject> semPlaces,
                                       ArrayList<SemObject> semTimes,
                                       ArrayList<SemRelation> semRelations,
                                       ArrayList<SemRelation> factRelations
    ) {

        /*   We check the factuality of each mention. If we have a value, we create a nwr:hasFactBankValue relation between the event and the value
             The id of the relation is defined a loca counter
         */
        int factualityCounter = 0;

        for (int i = 0; i < semEvents.size(); i++) {
            SemObject semObject = semEvents.get(i);
            for (int j = 0; j < semObject.getNafMentions().size(); j++) {
                NafMention nafMention = semObject.getNafMentions().get(j);
                if (!nafMention.getFactuality().getPrediction().isEmpty()) {
                    factualityCounter++;
                    SemRelation semRelation = new SemRelation();
                    String relationInstanceId = baseUrl+"facValue_"+factualityCounter;
                    semRelation.setId(relationInstanceId);
                    semRelation.addMention(nafMention);
                    semRelation.setPredicate("hasFactBankValue");
                    semRelation.setSubject(semObject.getId());
                    semRelation.setObject(nafMention.getFactuality().getPrediction());
                    factRelations.add(semRelation);
                }
            }
        }

        /*
            - iterate over de SRL layers
            - represent predicates and participants
            - check if they overlap with semObjects
            - if so use the instanceId
            - if not create a new instanceId
         */
        ArrayList<String> timedSemEventIds = new ArrayList<String>();

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent =  kafSaxParser.getKafEventArrayList().get(i);
            //// we need to get the corresponding semEvent first
            // check the SemEvents
            String semEventId = "";
            for (int j = 0; j < semEvents.size(); j++) {
                SemObject semEvent = semEvents.get(j);
                // if (matchAtLeastASingleSpan(kafEvent.getSpanIds(), semEvent)) {
                if (Util.matchAllOfAnyMentionSpans(kafEvent.getSpanIds(), semEvent)) {
                    semEventId = semEvent.getId();
                    break;
                }
            }
            if (semEventId.isEmpty()) {

               //// this is an event without SRL representation, which is not allowed
               // System.out.println("empty kafEvent.getId() = " + kafEvent.getId());
            }
            else {
                    for (int k = 0; k < kafEvent.getParticipants().size(); k++) {
                        KafParticipant kafParticipant = kafEvent.getParticipants().get(k);
                        if (!Util.validRole(kafParticipant.getRole())) {
                            continue;
                        }
                        boolean match = false;
                        for (int l = 0; l < semActors.size(); l++) {
                            SemObject semActor = semActors.get(l);
                            //if (Util.matchAtLeastASingleSpan(kafParticipant.getSpanIds(), semActor)) {
                            if (Util.matchAllSpansOfAnObjectMentionOrTheRoleHead(kafSaxParser, kafParticipant, semActor)) {
                                /*if (semActor.getURI().endsWith("Brazil")) {
                                    System.out.println("semEvent = " + semEventId);
                                    System.out.println("kafEvent = " + kafEvent.getId());
                                    System.out.println("semActor.getURI() = " + semActor.getURI());
                                    System.out.println("kafParticipant.getId() = " + kafParticipant.getId());
                                    System.out.println("kafParticipant spans = " + kafParticipant.getSpanIds().toString());
                                    System.out.println("semActor.getNafMentions().toString() = " + semActor.getNafMentions().toString());
                                    System.out.println("kafParticipant.getHeadId() = " + kafParticipant.getHeadId());
                                }*/
                               // System.out.println("semActor.getPhrase() = " + semActor.getPhrase());
                                /// create sem relations
                                SemRelation semRelation = new SemRelation();
                                String relationInstanceId = baseUrl+kafEvent.getId()+","+kafParticipant.getId();
                                semRelation.setId(relationInstanceId);
                                NafMention relNafMention = new NafMention(baseUrl+kafParticipant.getId());
                                semRelation.addMention(relNafMention);
                                semRelation.setPredicate("hasSemActor");
                                semRelation.setSubject(semEventId);
                                semRelation.setObject(semActor.getId());
                                semRelations.add(semRelation);
                                match = true;
                                break;
                            }
                        }
                        if (!match) {
                            for (int l = 0; l < semPlaces.size(); l++) {
                                SemObject semPlace = semPlaces.get(l);
                               // if (Util.matchAtLeastASingleSpan(kafParticipant.getSpanIds(), semPlace)) {
                                if (Util.matchAllSpansOfAnObjectMentionOrTheRoleHead(kafSaxParser, kafParticipant, semPlace)) {
                                    /// create sem relations
/*
                                    if (semPlace.getURI().endsWith("Brazil")) {
                                        System.out.println("semPlace.getURI() = " + semPlace.getURI());
                                        System.out.println("kafParticipant.getId() = " + kafParticipant.getId());
                                        System.out.println("semPlace.getNafMentions().toString() = " + semPlace.getNafMentions().toString());
                                        System.out.println("kafParticipant.getHeadId() = " + kafParticipant.getHeadId());
                                    }
*/

                                    //System.out.println("semPlace.getPhrase() = " + semPlace.getPhrase());
                                    if (semPlace.getPhrase().endsWith("Valcke")) {
                                        System.out.println("semPlace.getURI() = " + semPlace.getURI());
                                        System.out.println("kafParticipant.getId() = " + kafParticipant.getId());
                                        System.out.println("semPlace.getNafMentions().toString() = " + semPlace.getNafMentions().toString());
                                        System.out.println("kafParticipant.getHeadId() = " + kafParticipant.getHeadId());
                                    }

                                    SemRelation semRelation = new SemRelation();
                                    String relationInstanceId = baseUrl+kafEvent.getId()+","+kafParticipant.getId();
                                    semRelation.setId(relationInstanceId);
                                    NafMention relNafMention = new NafMention(baseUrl+kafParticipant.getId());
                                    semRelation.addMention(relNafMention);
                                    semRelation.setPredicate("hasSemPlace");
                                    semRelation.setSubject(semEventId);
                                    semRelation.setObject(semPlace.getId());
                                    semRelations.add(semRelation);
                                    match = true;
                                    break;
                                }
                            }
                        }
                        if (!match) {
                            for (int l = 0; l < semTimes.size(); l++) {
                                SemObject semTime = semTimes.get(l);
                               // if (Util.matchAtLeastASingleSpan(kafParticipant.getSpanIds(), semTime)) {
                                if (Util.matchAllSpansOfAnObjectMentionOrTheRoleHead(kafSaxParser, kafParticipant, semTime)) {
                                    /// create sem relations
/*
                                    System.out.println("semPlace.getURI() = " + semTime.getURI());
                                    System.out.println("kafParticipant.getId() = " + kafParticipant.getId());
                                    System.out.println("semTime.getNafMentions().toString() = " + semTime.getNafMentions().toString());
*/

                                    SemRelation semRelation = new SemRelation();
                                    String relationInstanceId = baseUrl+kafEvent.getId()+","+kafParticipant.getId();
                                    semRelation.setId(relationInstanceId);
                                    NafMention relNafMention = new NafMention(baseUrl+kafParticipant.getId());
                                    semRelation.addMention(relNafMention);
                                    semRelation.setPredicate("hasSemTime");
                                    semRelation.setSubject(semEventId);
                                    semRelation.setObject(semTime.getId());
                                    semRelations.add(semRelation);
                                    timedSemEventIds.add(semEventId);
                                    match = true;
                                    break;
                                }
                            }
                        }
                        if (!match) {
                            /// we could not find any instance matchAtLeastASingleSpan......
                        }
                    }
                }
        }

        /// in all cases there is no time relations we link it to the docTime

        if (!docSemTime.getPhrase().isEmpty()) {
            /// in all cases there is no time relations we link it to the docTime
            int docTimeRelationCount = 0;
            for (int i = 0; i < semEvents.size(); i++) {
                SemObject semEvent = semEvents.get(i);
                if (!timedSemEventIds.contains(semEvent.getId())) {
                    /// timeless event
                    docTimeRelationCount++;
                    SemRelation semRelation = new SemRelation();
                    String relationInstanceId = baseUrl + "docTime_" + docTimeRelationCount;
                    semRelation.setId(relationInstanceId);
                    //// Since the doctime has no reference in the text, we use the mentions of the events to point to
                    semRelation.setNafMentions(semEvent.getNafMentions());
                    semRelation.setPredicate("hasSemTime");
                    semRelation.setSubject(semEvent.getId());
                    semRelation.setObject(docSemTime.getId());
                    semRelations.add(semRelation);
                }

            }
        }
    }

    static public void serializeJena (OutputStream stream,
                                      ArrayList<SemObject> semEvents,
                                      ArrayList<SemObject> semActors,
                                      ArrayList<SemObject> semPlaces,
                                      ArrayList<SemObject> semTimes,
                                      ArrayList<SemRelation> semRelations,
                                      ArrayList<SemRelation> factRelations,
                                      HashMap <String, SourceMeta> sourceMetaHashMap) {



        // create an empty Model

        Dataset ds = TDBFactory.createDataset();
        Model defaultModel = ds.getDefaultModel();
        defaultModel.setNsPrefix("nwr", ResourcesUri.nwr);
        defaultModel.setNsPrefix("gaf", ResourcesUri.gaf);

        defaultModel.setNsPrefix("nwr", ResourcesUri.nwr);
        defaultModel.setNsPrefix("fn", ResourcesUri.fn);
/*      //REMOVED DUE TO ILLEGAL CHARACTERS
        defaultModel.setNsPrefix("wn", ResourcesUri.wn);
        defaultModel.setNsPrefix("vn", ResourcesUri.vn);
        defaultModel.setNsPrefix("pb", ResourcesUri.pb);
        defaultModel.setNsPrefix("nb", ResourcesUri.nb);
*/
        defaultModel.setNsPrefix("sem", ResourcesUri.sem);
        defaultModel.setNsPrefix("gaf", ResourcesUri.gaf);
       // defaultModel.setNsPrefix("dbp", ResourcesUri.dbp);          /// removed because of dot problem in dbpedia URIs
        defaultModel.setNsPrefix("owl", ResourcesUri.owl);
        defaultModel.setNsPrefix("time", ResourcesUri.owltime);
        defaultModel.setNsPrefix("rdf", ResourcesUri.rdf);
        defaultModel.setNsPrefix("rdfs", ResourcesUri.rdfs);
       // defaultModel.setNsPrefix("tl", ResourcesUri.tl);

        Model provenanceModel = ds.getNamedModel("http://www.newsreader-project.eu/provenance");
        provenanceModel.setNsPrefix("nwr", ResourcesUri.nwr);
        provenanceModel.setNsPrefix("gaf", ResourcesUri.gaf);
        provenanceModel.setNsPrefix("nwrauthor", ResourcesUri.nwrauthor);
        provenanceModel.setNsPrefix("nwrsourceowner", ResourcesUri.nwrsourceowner);

        Model instanceModel = ds.getNamedModel("http://www.newsreader-project.eu/instances");
        instanceModel.setNsPrefix("nwr", ResourcesUri.nwr);
        instanceModel.setNsPrefix("fn", ResourcesUri.fn);
/*      //REMOVED DUE TO ILLEGAL CHARACTERS
        instanceModel.setNsPrefix("wn", ResourcesUri.wn);
        instanceModel.setNsPrefix("vn", ResourcesUri.vn);
        instanceModel.setNsPrefix("pb", ResourcesUri.pb);
        instanceModel.setNsPrefix("nb", ResourcesUri.nb);
*/
        instanceModel.setNsPrefix("sem", ResourcesUri.sem);
        instanceModel.setNsPrefix("gaf", ResourcesUri.gaf);
        instanceModel.setNsPrefix("owl", ResourcesUri.owl);
        //  instanceModel.setNsPrefix("tl", ResourcesUri.tl);
        instanceModel.setNsPrefix("time", ResourcesUri.owltime);

     //   instanceModel.setNsPrefix("dbp", ResourcesUri.dbp);       /// removed because of dot problem in dbpedia URIs

        for (int i = 0; i < semEvents.size(); i++) {
            SemObject semEvent = semEvents.get(i);
            semEvent.addToJenaModel(instanceModel, Sem.Event);
        }


        for (int i = 0; i < semActors.size(); i++) {
            SemObject semActor = semActors.get(i);
            semActor.addToJenaModel(instanceModel, Sem.Actor);
        }

        for (int i = 0; i < semPlaces.size(); i++) {
            SemObject semPlace = semPlaces.get(i);
            semPlace.addToJenaModel(instanceModel, Sem.Place);
        }
        if (!docSemTime.getPhrase().isEmpty()) {
            docSemTime.addToJenaModelDocTimeInterval(instanceModel);
        }
        else {
            System.out.println("empty phrase for docSemTime = " + docSemTime.getId());
        }
        for (int i = 0; i < semTimes.size(); i++) {
            SemTime semTime = (SemTime) semTimes.get(i);
            //semTime.addToJenaModel(instanceModel, Sem.Time);
            semTime.addToJenaModelTimeInterval(instanceModel);
        }

        for (int i = 0; i < semRelations.size(); i++) {
            SemRelation semRelation = semRelations.get(i);
            if (sourceMetaHashMap!=null) {
                semRelation.addToJenaDataSet(ds, provenanceModel, sourceMetaHashMap);

            }
            else {
                semRelation.addToJenaDataSet(ds, provenanceModel);
            }

            ///** Next version adds relations to one single relation graph
            //semRelation.addToJenaDataSet(ds, relationModel, provenanceModel);
        }


        for (int i = 0; i < factRelations.size(); i++) {
            SemRelation semRelation = factRelations.get(i);
            if (sourceMetaHashMap!=null) {
                semRelation.addToJenaDataSet(ds, provenanceModel, sourceMetaHashMap);

            }
            else {
                semRelation.addToJenaDataSet(ds, provenanceModel);
            }
        }

        RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);
    }

    static public void serializeJenaEvents (OutputStream stream,HashMap<String, ArrayList<CompositeEvent>> semEvents,
                                      HashMap <String, SourceMeta> sourceMetaHashMap) {



        // create an empty Model

        Dataset ds = TDBFactory.createDataset();
        Model defaultModel = ds.getDefaultModel();
        defaultModel.setNsPrefix("nwr", ResourcesUri.nwr);
        defaultModel.setNsPrefix("gaf", ResourcesUri.gaf);

        defaultModel.setNsPrefix("nwr", ResourcesUri.nwr);
        defaultModel.setNsPrefix("fn", ResourcesUri.fn);
/*      //REMOVED DUE TO ILLEGAL CHARACTERS
        defaultModel.setNsPrefix("wn", ResourcesUri.wn);
        defaultModel.setNsPrefix("vn", ResourcesUri.vn);
        defaultModel.setNsPrefix("pb", ResourcesUri.pb);
        defaultModel.setNsPrefix("nb", ResourcesUri.nb);
*/
        defaultModel.setNsPrefix("sem", ResourcesUri.sem);
        defaultModel.setNsPrefix("gaf", ResourcesUri.gaf);
       // defaultModel.setNsPrefix("dbp", ResourcesUri.dbp);          /// removed because of dot problem in dbpedia URIs
        defaultModel.setNsPrefix("owl", ResourcesUri.owl);
        defaultModel.setNsPrefix("time", ResourcesUri.owltime);
        defaultModel.setNsPrefix("rdf", ResourcesUri.rdf);
        defaultModel.setNsPrefix("rdfs", ResourcesUri.rdfs);
       // defaultModel.setNsPrefix("tl", ResourcesUri.tl);

        Model provenanceModel = ds.getNamedModel("http://www.newsreader-project.eu/provenance");
        provenanceModel.setNsPrefix("nwr", ResourcesUri.nwr);
        provenanceModel.setNsPrefix("gaf", ResourcesUri.gaf);
        provenanceModel.setNsPrefix("nwrauthor", ResourcesUri.nwrauthor);
        provenanceModel.setNsPrefix("nwrsourceowner", ResourcesUri.nwrsourceowner);

        Model instanceModel = ds.getNamedModel("http://www.newsreader-project.eu/instances");
        instanceModel.setNsPrefix("nwr", ResourcesUri.nwr);
        instanceModel.setNsPrefix("fn", ResourcesUri.fn);
/*      //REMOVED DUE TO ILLEGAL CHARACTERS
        instanceModel.setNsPrefix("wn", ResourcesUri.wn);
        instanceModel.setNsPrefix("vn", ResourcesUri.vn);
        instanceModel.setNsPrefix("pb", ResourcesUri.pb);
        instanceModel.setNsPrefix("nb", ResourcesUri.nb);
*/
        instanceModel.setNsPrefix("sem", ResourcesUri.sem);
        instanceModel.setNsPrefix("gaf", ResourcesUri.gaf);
        instanceModel.setNsPrefix("owl", ResourcesUri.owl);
        //  instanceModel.setNsPrefix("tl", ResourcesUri.tl);
        instanceModel.setNsPrefix("time", ResourcesUri.owltime);

     //   instanceModel.setNsPrefix("dbp", ResourcesUri.dbp);       /// removed because of dot problem in dbpedia URIs

        Set keySet = semEvents.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String lemma = (String) keys.next();
            ArrayList<CompositeEvent> compositeEvents = semEvents.get(lemma);
            for (int i = 0; i < compositeEvents.size(); i++) {
                CompositeEvent compositeEvent = compositeEvents.get(i);
                compositeEvent.getEvent().addToJenaModel(instanceModel, Sem.Event);
                for (int j = 0; j < compositeEvent.getMySemRelations().size(); j++) {
                    SemRelation semRelation = compositeEvent.getMySemRelations().get(j);
                    if (sourceMetaHashMap!=null) {
                        semRelation.addToJenaDataSet(ds, provenanceModel, sourceMetaHashMap);

                    }
                    else {
                        semRelation.addToJenaDataSet(ds, provenanceModel);
                    }
                }
                for (int j = 0; j < compositeEvent.getMySemFactRelations().size(); j++) {
                    SemRelation semRelation = compositeEvent.getMySemFactRelations().get(j);
                    if (sourceMetaHashMap!=null) {
                        semRelation.addToJenaDataSet(ds, provenanceModel, sourceMetaHashMap);

                    }
                    else {
                        semRelation.addToJenaDataSet(ds, provenanceModel);
                    }
                }
            }
        }
        RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);
    }


    static public void serializeJenaEntities (OutputStream stream,
                                      ArrayList<SemObject> semActors,
                                      ArrayList<SemObject> semPlaces,
                                      ArrayList<SemObject> semTimes) {



        // create an empty Model

        Dataset ds = TDBFactory.createDataset();
        Model defaultModel = ds.getDefaultModel();
        defaultModel.setNsPrefix("nwr", ResourcesUri.nwr);
        defaultModel.setNsPrefix("gaf", ResourcesUri.gaf);

        defaultModel.setNsPrefix("nwr", ResourcesUri.nwr);
        defaultModel.setNsPrefix("fn", ResourcesUri.fn);
/*      //REMOVED DUE TO ILLEGAL CHARACTERS
        defaultModel.setNsPrefix("wn", ResourcesUri.wn);
        defaultModel.setNsPrefix("vn", ResourcesUri.vn);
        defaultModel.setNsPrefix("pb", ResourcesUri.pb);
        defaultModel.setNsPrefix("nb", ResourcesUri.nb);
*/
        defaultModel.setNsPrefix("sem", ResourcesUri.sem);
        defaultModel.setNsPrefix("gaf", ResourcesUri.gaf);
       // defaultModel.setNsPrefix("dbp", ResourcesUri.dbp);          /// removed because of dot problem in dbpedia URIs
        defaultModel.setNsPrefix("owl", ResourcesUri.owl);
        defaultModel.setNsPrefix("time", ResourcesUri.owltime);
        defaultModel.setNsPrefix("rdf", ResourcesUri.rdf);
        defaultModel.setNsPrefix("rdfs", ResourcesUri.rdfs);
       // defaultModel.setNsPrefix("tl", ResourcesUri.tl);

        Model instanceModel = ds.getNamedModel("http://www.newsreader-project.eu/instances");
        instanceModel.setNsPrefix("nwr", ResourcesUri.nwr);
        instanceModel.setNsPrefix("fn", ResourcesUri.fn);
/*      //REMOVED DUE TO ILLEGAL CHARACTERS
        instanceModel.setNsPrefix("wn", ResourcesUri.wn);
        instanceModel.setNsPrefix("vn", ResourcesUri.vn);
        instanceModel.setNsPrefix("pb", ResourcesUri.pb);
        instanceModel.setNsPrefix("nb", ResourcesUri.nb);
*/
        instanceModel.setNsPrefix("sem", ResourcesUri.sem);
        instanceModel.setNsPrefix("gaf", ResourcesUri.gaf);
        instanceModel.setNsPrefix("owl", ResourcesUri.owl);
        //  instanceModel.setNsPrefix("tl", ResourcesUri.tl);
        instanceModel.setNsPrefix("time", ResourcesUri.owltime);

     //   instanceModel.setNsPrefix("dbp", ResourcesUri.dbp);       /// removed because of dot problem in dbpedia URIs


        for (int i = 0; i < semActors.size(); i++) {
            SemObject semActor = semActors.get(i);
            semActor.addToJenaModel(instanceModel, Sem.Actor);
        }

        for (int i = 0; i < semPlaces.size(); i++) {
            SemObject semPlace = semPlaces.get(i);
            semPlace.addToJenaModel(instanceModel, Sem.Place);
        }

        docSemTime.addToJenaModelDocTimeInterval(instanceModel);

        for (int i = 0; i < semTimes.size(); i++) {
            SemTime semTime = (SemTime) semTimes.get(i);
            //semTime.addToJenaModel(instanceModel, Sem.Time);
            semTime.addToJenaModelTimeInterval(instanceModel);
        }

        RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);
    }


    static public void main (String [] args) {
        //String pathToNafFile = args[0];
        String pathToNafFile = "/Code/vu/newsreader/EventCoreference/example/56VW-T8H1-DXCW-D3F2.xml_8a28e3b9ede9d243cf55b6db8d41a21c.naf";
        String project = "cars";
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        ArrayList<SemRelation> factRelations = new ArrayList<SemRelation>();
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(pathToNafFile);
        processNafFile(project, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations);
        try {
            String pathToTrigFile = pathToNafFile+".trig";
            FileOutputStream fos = new FileOutputStream(pathToTrigFile);
            serializeJena(fos, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations, null);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //serializeJena(System.out, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations, null);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/*    static public void processNafFileOrg (String project, KafSaxParser kafSaxParser,
                                          ArrayList<SemObject> semEvents,
                                          ArrayList<SemObject> semActors,
                                          ArrayList<SemObject> semPlaces,
                                          ArrayList<SemObject> semTimes,
                                          ArrayList<SemRelation> semRelations,
                                          ArrayList<SemRelation> factRelations
    ) {
        //String baseUrl = ResourcesUri.nwr+kafSaxParser.getKafMetaData().getUrl().replace("/", URI_SEPARATOR)+ID_SEPARATOR;
        String baseUrl = kafSaxParser.getKafMetaData().getUrl()+ID_SEPARATOR;
        if (!baseUrl.toLowerCase().startsWith("http"))     {
            baseUrl = ResourcesUri.nwrdata+project+"/"+kafSaxParser.getKafMetaData().getUrl()+ID_SEPARATOR;
        }

        if (!kafSaxParser.getKafMetaData().getCreationtime().isEmpty()) {
            //// we first store the publication date as a time
            KafSense dateSense = new KafSense();
            dateSense.setRefType("publication date");
            dateSense.setSensecode(kafSaxParser.getKafMetaData().getCreationtime());
            docSemTime = new SemTime();
            //docSemTime.addConcept(dateSense);


            //docSemTime.setId(ResourcesUri.nwrtime+dateSense.getSensecode());
            docSemTime.setId(baseUrl + "nafHeader" + "_" + "fileDesc" + "_" + "creationtime");
            docSemTime.addPhraseCounts(dateSense.getSensecode());
            NafMention mention = new NafMention(baseUrl + "nafHeader" + "_" + "fileDesc" + "_" + "creationtime");
            docSemTime.addMentionUri(mention);
            //semTimes.add(docSemTime);
        }

        //// we get time references from the SRL layer
        // HACK FUNCTION BECAUSE THERE IS YET NO COREFERENCE SET FOR TIME, WHEN THIS IS IN NAF WE CAN DEPRECATE THIS FUNCTION
        HashMap<String, ArrayList<ArrayList<CorefTarget>>> timeReferences = Util.getTimeMentionsHashMapFromSrl (kafSaxParser);
        Set keySet = timeReferences.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            ArrayList<ArrayList<CorefTarget>> corefTargetArrayList = timeReferences.get(key);
            SemTime semTimeRole = new SemTime();
            semTimeRole.setId(baseUrl + key);
            ArrayList<NafMention> mentions = Util.getNafMentionArrayList(baseUrl, kafSaxParser, corefTargetArrayList);
            semTimeRole.setNafMentions(mentions);
            semTimeRole.addPhraseCountsForMentions(kafSaxParser);
            String phrase = semTimeRole.getPhraseCounts().get(0).getPhrase();
            if (!OwlTime.getYearFromString(phrase).isEmpty()) {
                semTimes.add(semTimeRole);
            }
        }



*//*
        HashMap<String, ArrayList<ArrayList<CorefTarget>>> locationReferences = getLocationMentionsHashMapFromSrl (kafSaxParser);
        keySet = locationReferences.keySet();
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            ArrayList<ArrayList<CorefTarget>> corefTargetArrayList = locationReferences.get(key);
            SemPlace semPlaceRole = new SemPlace();
            semPlaceRole.setId(baseUrl + key);
            ArrayList<NafMention> mentions = Util.getNafMentionArrayList(baseUrl, kafSaxParser, corefTargetArrayList);
            semPlaceRole.setNafMentions(mentions);
            semPlaceRole.addPhraseCountsForMentions(kafSaxParser);
            semPlaces.add(semPlaceRole);
        }
*//*

        for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
            KafCoreferenceSet coreferenceSet = kafSaxParser.kafCorefenceArrayList.get(i);
            //// these mentions are too coarse and need to intersect with the SRL elements!!!!!
            ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayList(baseUrl, kafSaxParser, coreferenceSet.getSetsOfSpans());
            KafSense sense = new KafSense();
            sense.setRefType("corefType");
            sense.setSensecode(coreferenceSet.getType());
            if (coreferenceSet.getType().equalsIgnoreCase("event")) {
                SemEvent semEvent = new SemEvent();
                semEvent.setId(baseUrl+coreferenceSet.getCoid());
                semEvent.setNafMentions(mentionArrayList);
                semEvent.addPhraseCountsForMentions(kafSaxParser);
                semEvent.setFactuality(kafSaxParser);
                semEvent.setConcept(Util.getExternalReferencesSrlEvents(kafSaxParser, coreferenceSet));
                semEvent.setIdByDBpediaReference();
                semEvents.add(semEvent);
            }
            else if (coreferenceSet.getType().equalsIgnoreCase("location")) {
                //// problem... the coref sets do not have a type for entities when created by EHU
                SemPlace semPlace = new SemPlace();
                semPlace.setId(baseUrl + coreferenceSet.getCoid());
                semPlace.setNafMentions(mentionArrayList);
                semPlace.addPhraseCountsForMentions(kafSaxParser);
                semPlace.addConcept(sense);
                semPlace.addConcepts(Util.getExternalReferencesSrlParticipants(kafSaxParser, coreferenceSet));
                // semPlace.addConcepts(getExternalReferencesEntities(kafSaxParser, coreferenceSet));
                semPlace.addConcepts(Util.getExternalReferencesEntities(kafSaxParser, coreferenceSet));
                semPlace.setIdByDBpediaReference();
                semPlaces.add(semPlace);
            }
            else  {
                //// if there is no location assigned to the coreference type we get it from the entities
                boolean location = false;
                for (int j = 0; j < kafSaxParser.kafEntityArrayList.size(); j++) {
                    KafEntity kafEntity = kafSaxParser.kafEntityArrayList.get(j);
                    /// choose either loose or strict
                    if (Util.intersectingAtLeastOneSetOfSpans(kafEntity.getSetsOfSpans(), coreferenceSet.getSetsOfSpans())) {
                        //if (Util.matchingSpans(kafEntity.getSetsOfSpans(), coreferenceSet.getSetsOfSpans())) {
                        if (kafEntity.getType().equals("location")) {
                            SemPlace semPlace = new SemPlace();
                            semPlace.setId(baseUrl + coreferenceSet.getCoid());
                            semPlace.setNafMentions(mentionArrayList);
                            semPlace.addPhraseCountsForMentions(kafSaxParser);
                            semPlace.addConcept(sense);
                            semPlace.addConcepts(Util.getExternalReferencesSrlParticipants(kafSaxParser, coreferenceSet));
                            // semPlace.addConcepts(getExternalReferencesEntities(kafSaxParser, coreferenceSet));
                            semPlace.addConcepts(kafEntity.getExternalReferences());
                            semPlace.setIdByDBpediaReference();
                            semPlaces.add(semPlace);
                            location = true;
                            break;
                        }
                    }
                }
                //// if still not a location, we assume it is an actor
                if (!location) {
                    for (int j = 0; j < kafSaxParser.kafEntityArrayList.size(); j++) {
                        KafEntity kafEntity = kafSaxParser.kafEntityArrayList.get(j);
                        /// choose either loose or strict
                        /// either too loose or to strict
                        if (Util.intersectingAtLeastOneSetOfSpans(kafEntity.getSetsOfSpans(), coreferenceSet.getSetsOfSpans())) {
                            //if (Util.matchingSpans(kafEntity.getSetsOfSpans(), coreferenceSet.getSetsOfSpans())) {
                            if (!kafEntity.getType().equals("location")) {
                                SemActor semActor = new SemActor();
                                semActor.setId(baseUrl + coreferenceSet.getCoid());
                                semActor.setNafMentions(mentionArrayList);
                                semActor.addPhraseCountsForMentions(kafSaxParser);
                                semActor.addConcept(sense);
                                semActor.addConcepts(Util.getExternalReferencesSrlParticipants(kafSaxParser, coreferenceSet));
                                //semActor.addConcepts(getExternalReferencesEntities(kafSaxParser, coreferenceSet));
                                semActor.addConcepts(kafEntity.getExternalReferences());
                                semActor.setIdByDBpediaReference();
                                semActors.add(semActor);

                                if (semActor.getURI().endsWith("Brazil")) {
                                    System.out.println("coreferenceSet = " + coreferenceSet.getCoid());
                                    System.out.println("semActor.getId() = " + semActor.getId());
                                    System.out.println("semActor.getNafMentions().toString() = " + semActor.getNafMentions().toString());
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        //// THERE SHOULD NOT BE ANY
        //// check for any events and roles not covered by the coreference sets
*//*
        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);

        }
*//*





        *//*   We check the factuality of each mention. If we have a value, we create a nwr:hasFactBankValue relation between the event and the value
             The id of the relation is defined a loca counter
         *//*
        int factualityCounter = 0;

        for (int i = 0; i < semEvents.size(); i++) {
            SemObject semObject = semEvents.get(i);
            for (int j = 0; j < semObject.getNafMentions().size(); j++) {
                NafMention nafMention = semObject.getNafMentions().get(j);
                if (!nafMention.getFactuality().getPrediction().isEmpty()) {
                    factualityCounter++;
                    SemRelation semRelation = new SemRelation();
                    String relationInstanceId = baseUrl+"facValue_"+factualityCounter;
                    semRelation.setId(relationInstanceId);
                    semRelation.addMention(nafMention);
                    semRelation.setPredicate("hasFactBankValue");
                    semRelation.setSubject(semObject.getId());
                    semRelation.setObject(nafMention.getFactuality().getPrediction());
                    factRelations.add(semRelation);
                }
            }
        }

        *//*
            - iterate over de SRL layers
            - represent predicates and participants
            - check if they overlap with semObjects
            - if so use the instanceId
            - if not create a new instanceId
         *//*
        ArrayList<String> timedSemEventIds = new ArrayList<String>();

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent =  kafSaxParser.getKafEventArrayList().get(i);
            //// we need to get the corresponding semEvent first
            // check the SemEvents
            String semEventId = "";
            for (int j = 0; j < semEvents.size(); j++) {
                SemObject semEvent = semEvents.get(j);
                // if (matchAtLeastASingleSpan(kafEvent.getSpanIds(), semEvent)) {
                if (Util.matchAllOfAnyMentionSpans(kafEvent.getSpanIds(), semEvent)) {
                    semEventId = semEvent.getId();
                    break;
                }
            }
            if (semEventId.isEmpty()) {

                //// this is an event without SRL representation, which is not allowed
                System.out.println("empty kafEvent.getId() = " + kafEvent.getId());
            }
            else {
                for (int k = 0; k < kafEvent.getParticipants().size(); k++) {
                    KafParticipant kafParticipant = kafEvent.getParticipants().get(k);
                    if (kafParticipant.getRole().equalsIgnoreCase("AM-MOD")) {
                        continue;
                    }
                    boolean match = false;
                    for (int l = 0; l < semActors.size(); l++) {
                        SemObject semActor = semActors.get(l);
                        if (Util.matchAtLeastASingleSpan(kafParticipant.getSpanIds(), semActor)) {
                            if (semActor.getURI().endsWith("Brazil")) {
                                System.out.println("semEvent = " + semEventId);
                                System.out.println("kafEvent = " + kafEvent.getId());
                                System.out.println("semActor.getURI() = " + semActor.getURI());
                                System.out.println("kafParticipant.getId() = " + kafParticipant.getId());
                                System.out.println("kafParticipant spans = " + kafParticipant.getSpanIds().toString());
                                System.out.println("semActor.getNafMentions().toString() = " + semActor.getNafMentions().toString());
                                System.out.println("kafParticipant.getHeadId() = " + kafParticipant.getHeadId());
                            }

                            /// create sem relations
                            SemRelation semRelation = new SemRelation();
                            String relationInstanceId = baseUrl+kafEvent.getId()+","+kafParticipant.getId();
                            semRelation.setId(relationInstanceId);
                            NafMention relNafMention = new NafMention(baseUrl+kafParticipant.getId());
                            semRelation.addMention(relNafMention);
                            semRelation.setPredicate("hasSemActor");
                            semRelation.setSubject(semEventId);
                            semRelation.setObject(semActor.getId());
                            semRelations.add(semRelation);
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        for (int l = 0; l < semPlaces.size(); l++) {
                            SemObject semPlace = semPlaces.get(l);
                            if (Util.matchAtLeastASingleSpan(kafParticipant.getSpanIds(), semPlace)) {
                                /// create sem relations
*//*
                                    if (semPlace.getURI().endsWith("Brazil")) {
                                        System.out.println("semPlace.getURI() = " + semPlace.getURI());
                                        System.out.println("kafParticipant.getId() = " + kafParticipant.getId());
                                        System.out.println("semPlace.getNafMentions().toString() = " + semPlace.getNafMentions().toString());
                                        System.out.println("kafParticipant.getHeadId() = " + kafParticipant.getHeadId());
                                    }
*//*
                                SemRelation semRelation = new SemRelation();
                                String relationInstanceId = baseUrl+kafEvent.getId()+","+kafParticipant.getId();
                                semRelation.setId(relationInstanceId);
                                NafMention relNafMention = new NafMention(baseUrl+kafParticipant.getId());
                                semRelation.addMention(relNafMention);
                                semRelation.setPredicate("hasSemPlace");
                                semRelation.setSubject(semEventId);
                                semRelation.setObject(semPlace.getId());
                                semRelations.add(semRelation);
                                match = true;
                                break;
                            }
                        }
                    }
                    if (!match) {
                        for (int l = 0; l < semTimes.size(); l++) {
                            SemObject semTime = semTimes.get(l);
                            if (Util.matchAtLeastASingleSpan(kafParticipant.getSpanIds(), semTime)) {
                                /// create sem relations
*//*
                                    System.out.println("semPlace.getURI() = " + semTime.getURI());
                                    System.out.println("kafParticipant.getId() = " + kafParticipant.getId());
                                    System.out.println("semTime.getNafMentions().toString() = " + semTime.getNafMentions().toString());
*//*

                                SemRelation semRelation = new SemRelation();
                                String relationInstanceId = baseUrl+kafEvent.getId()+","+kafParticipant.getId();
                                semRelation.setId(relationInstanceId);
                                NafMention relNafMention = new NafMention(baseUrl+kafParticipant.getId());
                                semRelation.addMention(relNafMention);
                                semRelation.setPredicate("hasSemTime");
                                semRelation.setSubject(semEventId);
                                semRelation.setObject(semTime.getId());
                                semRelations.add(semRelation);
                                timedSemEventIds.add(semEventId);
                                match = true;
                                break;
                            }
                        }
                    }
                    if (!match) {
                        /// we could not find any instance matchAtLeastASingleSpan......
                    }
                }
            }
        }

        /// in all cases there is no time relations we link it to the docTime

        if (!docSemTime.getPhrase().isEmpty()) {
            /// in all cases there is no time relations we link it to the docTime
            int docTimeRelationCount = 0;
            for (int i = 0; i < semEvents.size(); i++) {
                SemObject semEvent = semEvents.get(i);
                if (!timedSemEventIds.contains(semEvent.getId())) {
                    /// timeless event
                    docTimeRelationCount++;
                    SemRelation semRelation = new SemRelation();
                    String relationInstanceId = baseUrl + "docTime_" + docTimeRelationCount;
                    semRelation.setId(relationInstanceId);
                    //// Since the doctime has no reference in the text, we use the mentions of the events to point to
                    semRelation.setNafMentions(semEvent.getNafMentions());
                    semRelation.setPredicate("hasSemTime");
                    semRelation.setSubject(semEvent.getId());
                    semRelation.setObject(docSemTime.getId());
                    semRelations.add(semRelation);
                }

            }
        }
    }*/

}
