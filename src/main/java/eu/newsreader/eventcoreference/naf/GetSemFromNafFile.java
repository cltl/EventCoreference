package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.output.JenaSerialization;
import eu.newsreader.eventcoreference.util.EntityTypes;
import eu.newsreader.eventcoreference.util.RoleLabels;
import eu.newsreader.eventcoreference.util.TimeLanguage;
import eu.newsreader.eventcoreference.util.Util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

    static final int MINEVENTLABELSIZE = 3;
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

        /// @deprecated since it is included in the event-coref module for NAF
        //// THIS FIX IS NEEDED BECAUSE SOME OF THE COREF SETS ARE TOO BIG
        fixEventCoreferenceSets(kafSaxParser);

        //// THIS IS NEEDED TO FILTER ESO MAPPING AND IGNORE OTHERS
        fixExternalReferencesSrl(kafSaxParser);
        //// THIS IS NEEDED TO USE RERANKING OF DBPEDIA URIs

        //// ALSO PREFERS ENGLISH REFERENCES
        ///fixExternalReferencesEntities(kafSaxParser);

        /// if CROSSLINGUAL
        //useEnglishExternalReferences(kafSaxParser);


        TimeLanguage.setLanguage(kafSaxParser.getLanguage());
        String baseUrl = kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
        String entityUri = ResourcesUri.nwrdata+project+"/entities/";
        if (!baseUrl.toLowerCase().startsWith("http")) {
            baseUrl = ResourcesUri.nwrdata + project + "/" + kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
        }
        //processNafFileForActorPlaceInstances(baseUrl, kafSaxParser, semActors, semPlaces);
        //processNafFileForActorInstances(baseUrl, kafSaxParser, semActors);
        processNafFileForEntityCoreferenceSets(entityUri, baseUrl, kafSaxParser, semActors);
        SemTime docSemTime = processNafFileForTimeInstances(baseUrl, kafSaxParser, semTimes);
        //processNafFileForEventInstances(baseUrl, kafSaxParser, semEvents);
        processNafFileForEventCoreferenceSets(baseUrl, kafSaxParser, semEvents);
        //System.out.println("before semEvents = " + semEvents.size());
        filterOverlapEventsEntities(semEvents, semActors);
        //System.out.println("after semEvents = " + semEvents.size());
        processNafFileForRelations(baseUrl, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations, docSemTime);
    }

    static public void processNafFileWithAdditionalRoles (String project, KafSaxParser kafSaxParser,
                                       ArrayList<SemObject> semEvents,
                                       ArrayList<SemObject> semActors,
                                       ArrayList<SemObject> semPlaces,
                                       ArrayList<SemObject> semTimes,
                                       ArrayList<SemRelation> semRelations,
                                       ArrayList<SemRelation> factRelations
    ) {

        /// @deprecated since it is included in the event-coref module for NAF
        //// THIS FIX IS NEEDED BECAUSE SOME OF THE COREF SETS ARE TOO BIG
        fixEventCoreferenceSets(kafSaxParser);

        //// THIS IS NEEDED TO FILTER ESO MAPPING AND IGNORE OTHERS
        //// fixExternalReferencesSrl(kafSaxParser);

        //// THIS IS NEEDED TO USE RERANKING OF DBPEDIA URIs
        //// ALSO PREFERS ENGLISH REFERENCES
       //  fixExternalReferencesEntities(kafSaxParser);

        /// if CROSSLINGUAL
        //useEnglishExternalReferences(kafSaxParser);

        TimeLanguage.setLanguage(kafSaxParser.getLanguage());
        String baseUrl = kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
        String entityUri = ResourcesUri.nwrdata+project+"/entities/";
        if (!baseUrl.toLowerCase().startsWith("http")) {
            baseUrl = ResourcesUri.nwrdata + project + "/" + kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
        }
        //processNafFileForActorPlaceInstances(baseUrl, kafSaxParser, semActors, semPlaces);
        //processNafFileForActorInstances(baseUrl, kafSaxParser, semActors);
        processNafFileForEntityCoreferenceSets(entityUri, baseUrl, kafSaxParser, semActors);
        processSrlForRemainingFramenetRoles(project, kafSaxParser, semActors);

        SemTime docSemTime = processNafFileForTimeInstances(baseUrl, kafSaxParser, semTimes);
        //processNafFileForEventInstances(baseUrl, kafSaxParser, semEvents);
        processNafFileForEventCoreferenceSets(baseUrl, kafSaxParser, semEvents);
        //System.out.println("before semEvents = " + semEvents.size());
        filterOverlapEventsEntities(semEvents, semActors);
        //System.out.println("after semEvents = " + semEvents.size());
        processNafFileForRelations(baseUrl, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations, docSemTime);
    }


    /**
     * Needed because SRL and NERC can independently claim a mention as an entity or event. In that case, we give preference to the entity status
     * @param semEvents
     * @param semActors
     */
    static void filterOverlapEventsEntities (ArrayList<SemObject> semEvents, ArrayList<SemObject> semActors) {
        for (int i = 0; i < semEvents.size(); i++) {
            SemObject semEvent = semEvents.get(i);
            for (int j = 0; j < semActors.size(); j++) {
                SemObject semActor = semActors.get(j);
                for (int k = 0; k < semEvent.getNafMentions().size(); k++) {
                    NafMention nafMention = semEvent.getNafMentions().get(k);
                    if (Util.hasMention(semActor.getNafMentions(), nafMention)) {
                        semEvents.remove(semEvent);
                        break;
                    }
                }
            }
        }
    }

    static void fixEventCoreferenceSets (KafSaxParser kafSaxParser) {
        ArrayList<KafCoreferenceSet> fixedSets = new ArrayList<KafCoreferenceSet>();
        for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
            KafCoreferenceSet kafCoreferenceSet = kafSaxParser.kafCorefenceArrayList.get(i);
            if (kafCoreferenceSet.getType().toLowerCase().startsWith("event")) {
                if (kafCoreferenceSet.getExternalReferences().size()>3) {
                    HashMap<String, KafCoreferenceSet> corefMap = new HashMap<String, KafCoreferenceSet>();
                    int nSubSets = 0;
                    for (int j = 0; j < kafCoreferenceSet.getSetsOfSpans().size(); j++) {
                        ArrayList<CorefTarget> corefTargets = kafCoreferenceSet.getSetsOfSpans().get(j);
                        String lemma = "";
                        for (int k = 0; k < corefTargets.size(); k++) {
                            CorefTarget corefTarget = corefTargets.get(k);
                            KafTerm kafTerm = kafSaxParser.getTerm(corefTarget.getId());
                            if (kafTerm!=null) {
                                lemma += kafTerm.getLemma()+" ";
                            }
                        }
                        lemma = lemma.trim();
                        if (corefMap.containsKey(lemma)) {
                            KafCoreferenceSet kafCoreferenceSetNew = corefMap.get(lemma);
                            kafCoreferenceSetNew.addSetsOfSpans(corefTargets);
                            corefMap.put(lemma, kafCoreferenceSetNew);
                        }
                        else {
                            nSubSets++;
                            KafCoreferenceSet kafCoreferenceSetNew = new KafCoreferenceSet();
                            String corefId = kafCoreferenceSet.getCoid()+"_"+nSubSets;
                            kafCoreferenceSetNew.setCoid(corefId);
                            kafCoreferenceSetNew.setType(kafCoreferenceSet.getType());
                            kafCoreferenceSetNew.addSetsOfSpans(corefTargets);
                            corefMap.put(lemma, kafCoreferenceSetNew);
                        }
                    }
                    Set keySet = corefMap.keySet();
                    Iterator<String> keys = keySet.iterator();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        KafCoreferenceSet kafCoreferenceSetNew = corefMap.get(key);
                        fixedSets.add(kafCoreferenceSetNew);
                    }
                }
                else {
                    fixedSets.add(kafCoreferenceSet);
                }
            }
            else {
                fixedSets.add(kafCoreferenceSet);
            }
        }
        kafSaxParser.kafCorefenceArrayList = fixedSets;
    }

    static void fixExternalReferencesSrl (KafSaxParser kafSaxParser) {
        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent event = kafSaxParser.getKafEventArrayList().get(i);
            fixExternalReferences(event);
            for (int j = 0; j < event.getParticipants().size(); j++) {
                KafParticipant kafParticipant = event.getParticipants().get(j);
                fixExternalReferences(kafParticipant);
            }
        }
    }
    static void fixExternalReferencesEntities (KafSaxParser kafSaxParser) {
            for (int i = 0; i < kafSaxParser.kafEntityArrayList.size(); i++) {
                KafEntity entity = kafSaxParser.kafEntityArrayList.get(i);
                fixExternalReferences(entity);
            }
        }

    static void fixExternalReferences(KafEvent kafEvent) {
        ArrayList<KafSense> newKafSenses = new ArrayList<KafSense>();
        for (int i = 0; i < kafEvent.getExternalReferences().size(); i++) {
            KafSense kafSense = kafEvent.getExternalReferences().get(i);
            if (kafSense.getResource().endsWith("+")) {
                kafSense.setResource(kafSense.getResource().substring(0, kafSense.getResource().length() - 1));
                // System.out.println("kafSense.getResource() = " + kafSense.getResource());
            }
            if (!kafSense.getResource().endsWith("-")) {
               newKafSenses.add(kafSense);
            }
        }
        kafEvent.setExternalReferences(newKafSenses);
    }

    static void fixExternalReferences (KafEntity kafEntity) {
        boolean RERANK = false;
        ArrayList<KafSense> newKafSenses = new ArrayList<KafSense>();
        for (int i = 0; i < kafEntity.getExternalReferences().size(); i++) {
            KafSense kafSense = kafEntity.getExternalReferences().get(i);
            if (kafSense.getResource().toLowerCase().startsWith("vua-type-reranker")) {
                newKafSenses.add(kafSense);
                RERANK = true;
            }
            else {
                if (kafSense.getChildren().size()>0) {
                   /*
                   In this case, we assume that the child is the English equivalent.
                   We prefer the English equivalent over the non-English reference
                   <externalRef confidence="1.0" reference="http://nl.dbpedia.org/resource/Allerzielen" reftype="nl" resource="spotlight_v1">
                        <externalRef confidence="1.0" reference="http://dbpedia.org/resource/All_Souls'_Day" reftype="en" resource="wikipedia-db-nlEn"/>
                   </externalRef>
                   */
                    for (int j = 0; j < kafSense.getChildren().size(); j++) {
                        KafSense sense = kafSense.getChildren().get(j);
                        if (sense.getRefType().equals("en")) {
                            newKafSenses.add(sense);
                            RERANK = true;
                        }
                    }
                }
            }
        }
        if (RERANK) {
          //  System.out.println("RERANKED");
            kafEntity.setExternalReferences(newKafSenses);
        }
    }


    static void useEnglishExternalReferences (KafEntity kafEntity) {
        boolean RERANK = false;
        ArrayList<KafSense> newKafSenses = new ArrayList<KafSense>();
        for (int i = 0; i < kafEntity.getExternalReferences().size(); i++) {
            KafSense kafSense = kafEntity.getExternalReferences().get(i);
            if (kafSense.getChildren().size()>0) {
               /*
               In this case, we assume that the child is the English equivalent.
               We prefer the English equivalent over the non-English reference
               <externalRef confidence="1.0" reference="http://nl.dbpedia.org/resource/Allerzielen" reftype="nl" resource="spotlight_v1">
                    <externalRef confidence="1.0" reference="http://dbpedia.org/resource/All_Souls'_Day" reftype="en" resource="wikipedia-db-nlEn"/>
               </externalRef>
               */
                for (int j = 0; j < kafSense.getChildren().size(); j++) {
                    KafSense sense = kafSense.getChildren().get(j);
                    if (sense.getRefType().equals("en")) {
                        newKafSenses.add(sense);
                        RERANK = true;
                    }
                }
            }
        }
        if (RERANK) {
          //  System.out.println("RERANKED");
            kafEntity.setExternalReferences(newKafSenses);
        }
    }


    static void fixExternalReferences(KafParticipant kafParticipant) {
        ArrayList<KafSense> newKafSenses = new ArrayList<KafSense>();
        for (int i = 0; i < kafParticipant.getExternalReferences().size(); i++) {
            KafSense kafSense = kafParticipant.getExternalReferences().get(i);
            if (kafSense.getResource().endsWith("+")) {
                kafSense.setResource(kafSense.getResource().substring(0, kafSense.getResource().length() - 1));
                // System.out.println("kafSense.getResource() = " + kafSense.getResource());
            }
            if (!kafSense.getResource().endsWith("-")) {
               newKafSenses.add(kafSense);
            }
        }
        kafParticipant.setExternalReferences(newKafSenses);
    }

    static void processNafFileForEventCoreferenceSets (String baseUrl, KafSaxParser kafSaxParser,
                                                   ArrayList<SemObject> semEvents

    ) {


        /**
         * Event instances
         */
        for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
            KafCoreferenceSet kafCoreferenceSet = kafSaxParser.kafCorefenceArrayList.get(i);
            if (kafCoreferenceSet.getType().toLowerCase().startsWith("event")) {
                //// this is an event coreference set
                //// no we get all the predicates for this set.
                SemEvent semEvent = new SemEvent();
                semEvent.addLcses(kafCoreferenceSet.getExternalReferences());
                for (int j = 0; j < kafSaxParser.getKafEventArrayList().size(); j++) {
                    KafEvent event = kafSaxParser.getKafEventArrayList().get(j);
                    if (Util.hasCorefTargetArrayList(event.getSpans(), kafCoreferenceSet.getSetsOfSpans())) {
                        /// we want the event data
                        ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListFromPredicatesAndCoreferences(baseUrl, kafSaxParser, event);
                        semEvent.addNafMentions(mentionArrayList);
                        semEvent.addConcepts(event.getExternalReferences());
                    }
                }
                semEvent.addPhraseCountsForMentions(kafSaxParser);
                String eventName = semEvent.getTopPhraseAsLabel();
                //if (Util.hasAlphaNumeric(eventName)) {
                if (eventName.length()>=MINEVENTLABELSIZE) {
                    //semEvent.setId(baseUrl+event.getId());
                    //semEvent.setId(baseUrl + eventName + "Event");
                    String eventId = kafCoreferenceSet.getCoid().replace("coevent", "ev") ;
                    semEvent.setId(baseUrl + eventId);   // shorter form for triple store
                    semEvent.setFactuality(kafSaxParser);
                    semEvent.setIdByDBpediaReference();
                    semEvents.add(semEvent);
                }
            }
        }
    }


    static void processNafFileForEventWithoutCoreferenceSets (String baseUrl, KafSaxParser kafSaxParser,
                                                   ArrayList<SemObject> semEvents

    ) {


        /**
         * Event instances
         */
        for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
            KafEvent kafEvent = kafSaxParser.kafEventArrayList.get(i);
            SemEvent semEvent = new SemEvent();
            ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListForTermIds(baseUrl, kafSaxParser, kafEvent.getSpanIds());
                    /*new ArrayList<NafMention>();
            NafMention nafMention = new NafMention();
            nafMention.setTermsIds(kafEvent.getSpanIds());
            mentionArrayList.add(nafMention);*/
            semEvent.addNafMentions(mentionArrayList);
            semEvent.addConcepts(kafEvent.getExternalReferences());
            semEvent.addPhraseCountsForMentions(kafSaxParser);
            String eventName = semEvent.getTopPhraseAsLabel();
            //if (Util.hasAlphaNumeric(eventName)) {
            if (eventName.length()>=MINEVENTLABELSIZE) {
                //semEvent.setId(baseUrl+event.getId());
                //semEvent.setId(baseUrl + eventName + "Event");
                String eventId = kafEvent.getId() ;
                semEvent.setId(baseUrl + eventId);   // shorter form for triple store
                semEvent.setFactuality(kafSaxParser);
                semEvent.setIdByDBpediaReference();
                semEvents.add(semEvent);
            }
        }
    }




    /**
     *
     * @param baseUrl
     * @param kafSaxParser
     * @param semActors
     */
    static void processNafFileForEntityCoreferenceSets (String entityUri, String baseUrl, KafSaxParser kafSaxParser,
                                                        ArrayList<SemObject> semActors
    ) {

        /**
         * Entity instances
         */

        ArrayList<String> coveredEntities = new ArrayList<String>();

        /**
         * We first groups entities with the same URI
         */

        HashMap<String, ArrayList<KafEntity>> kafEntityActorUriMap = new HashMap<String, ArrayList<KafEntity>>();

        for (int j = 0; j < kafSaxParser.kafEntityArrayList.size(); j++) {
            KafEntity kafEntity = kafSaxParser.kafEntityArrayList.get(j);
            String uri = Util.getBestEntityUri(kafEntity);
            if (uri.isEmpty()) {
                kafEntity.setTokenStrings(kafSaxParser);
                if (Util.hasAlphaNumeric(kafEntity.getTokenString())) {
                    try {
                        uri = URLEncoder.encode(kafEntity.getTokenString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        //  e.printStackTrace();
                    }
                }
            }
            if (!uri.isEmpty()) {
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

        /**
         * Next we iterate over all the coreference sets that are not events
         */

        for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
            KafCoreferenceSet kafCoreferenceSet = kafSaxParser.kafCorefenceArrayList.get(i);
            if (!kafCoreferenceSet.getType().toLowerCase().startsWith("event")) {
                //// this is an entity coreference set
                //// no we get all the entities for this set.
                int topMatches = 0;
                String topEntity = "";

               //// we first decide which entity has the highest number of span overlap with the coreference targets
                Set keySet = kafEntityActorUriMap.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    ArrayList<KafEntity> entities = kafEntityActorUriMap.get(key);
                    int nMatches = 0;
                    for (int j = 0; j < entities.size(); j++) {
                        KafEntity kafEntity = entities.get(j);
                        nMatches += Util.countIntersectingSetOfSpans(kafEntity.getSetsOfSpans(), kafCoreferenceSet.getSetsOfSpans());
                    }
                    if (nMatches > topMatches) {
                        topMatches = nMatches;
                        topEntity = key;
                    }
                }
                if (!topEntity.isEmpty())  {
                        if (coveredEntities.contains(topEntity)) {
                          //  System.out.println("duplicating = " + topEntity);
                        }
                        coveredEntities.add(topEntity);
                        ArrayList<KafEntity> entities = kafEntityActorUriMap.get(topEntity);
                        ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListFromEntitiesAndCoreferences(baseUrl, kafSaxParser, entities);
                        String entityId = "";
                        entityId = Util.getEntityLabelUriFromEntities(entities);
                        /*for (int e = 0; e < entities.size(); e++) {
                            KafEntity kafEntity = entities.get(e);
                            entityId += kafEntity.getId();
                        }*/
                        SemActor semActor = new SemActor();
                        semActor.setId(entityUri + entityId);
                        semActor.setNafMentions(mentionArrayList);
                        semActor.addPhraseCountsForMentions(kafSaxParser);
                        semActor.addConcepts(Util.getExternalReferences(entities));
                        semActor.setIdByDBpediaReference();
                        Util.addObject(semActors, semActor);
                }
                else {
                    ///// coreference sets without any entity matches are ignored so far!!!!!!!!!
                 //   System.out.println("not matched kafCoreferenceSet.getCoid() = " + kafCoreferenceSet.getCoid());
                }
            }
        }

        //// There could be entities not matched with coreference sets. These are now added separately
        Set keySet  = kafEntityActorUriMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String uri =  keys.next();
            if (!coveredEntities.contains(uri)) {
                //   System.out.println("actor uri = " + uri);
                ArrayList<KafEntity> entities = kafEntityActorUriMap.get(uri);
                ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListFromEntities(baseUrl, kafSaxParser, entities);
                String entityId = "";
                entityId = Util.getEntityLabelUriFromEntities(entities);
               /* for (int i = 0; i < entities.size(); i++) {
                    KafEntity kafEntity = entities.get(i);
                    entityId += kafEntity.getId();
                }*/
                SemActor semActor = new SemActor();
                semActor.setId(entityUri + entityId);
                semActor.setNafMentions(mentionArrayList);
                semActor.addPhraseCountsForMentions(kafSaxParser);
                semActor.addConcepts(Util.getExternalReferences(entities));
                semActor.setIdByDBpediaReference();
                Util.addObject(semActors, semActor);
            }
        }
    }

    /**
     *
     * @param baseUrl
     * @param kafSaxParser
     * @param semActors
     */
    static void processNafFileForEntityCoreferenceSetsOrg (String entityUri, String baseUrl, KafSaxParser kafSaxParser,
                                                        ArrayList<SemObject> semActors
    ) {

        /**
         * Entity instances
         */

        ArrayList<String> coveredEntities = new ArrayList<String>();

        /**
         * We first groups entities with the same URI
         */

        HashMap<String, ArrayList<KafEntity>> kafEntityActorUriMap = new HashMap<String, ArrayList<KafEntity>>();

        for (int j = 0; j < kafSaxParser.kafEntityArrayList.size(); j++) {
            KafEntity kafEntity = kafSaxParser.kafEntityArrayList.get(j);
            String uri = Util.getBestEntityUri(kafEntity);
            if (uri.isEmpty()) {
                kafEntity.setTokenStrings(kafSaxParser);
                if (Util.hasAlphaNumeric(kafEntity.getTokenString())) {
                    try {
                        uri = URLEncoder.encode(kafEntity.getTokenString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        //  e.printStackTrace();
                    }
                }
            }
            if (!uri.isEmpty()) {
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

        /**
         * Next we iterate over all the coreference sets that are not events
         */

        for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
            KafCoreferenceSet kafCoreferenceSet = kafSaxParser.kafCorefenceArrayList.get(i);
            if (!kafCoreferenceSet.getType().toLowerCase().startsWith("event")) {
                //// this is an entity coreference set
                //// no we get all the entities for this set.
                int topMatches = 0;
                String topEntity = "";

               //// we first decide which entity has the highest number of span overlap with the coreference targets
                Set keySet = kafEntityActorUriMap.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    ArrayList<KafEntity> entities = kafEntityActorUriMap.get(key);
                    int nMatches = 0;
                    for (int j = 0; j < entities.size(); j++) {
                        KafEntity kafEntity = entities.get(j);
                        nMatches += Util.countIntersectingSetOfSpans(kafEntity.getSetsOfSpans(), kafCoreferenceSet.getSetsOfSpans());
                    }
                    if (nMatches > topMatches) {
                        topMatches = nMatches;
                        topEntity = key;
                    }
                }
                if (!topEntity.isEmpty())  {
                        if (coveredEntities.contains(topEntity)) {
                          //  System.out.println("duplicating = " + topEntity);
                        }
                        coveredEntities.add(topEntity);
                        ArrayList<KafEntity> entities = kafEntityActorUriMap.get(topEntity);
                        ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListFromEntitiesAndCoreferences(baseUrl, kafSaxParser, entities);
                        String entityId = "";
                        entityId = Util.getEntityLabelUriFromEntities(entities);
                        /*for (int e = 0; e < entities.size(); e++) {
                            KafEntity kafEntity = entities.get(e);
                            entityId += kafEntity.getId();
                        }*/
                        SemActor semActor = new SemActor();
                        semActor.setId(entityUri + entityId);
                        semActor.setNafMentions(mentionArrayList);
                        semActor.addPhraseCountsForMentions(kafSaxParser);
                        semActor.addConcepts(Util.getExternalReferences(entities));
                        semActor.setIdByDBpediaReference();
                        Util.addObject(semActors, semActor);
                }
                else {
                    ///// coreference sets without any entity matches are ignored so far!!!!!!!!!
                 //   System.out.println("not matched kafCoreferenceSet.getCoid() = " + kafCoreferenceSet.getCoid());
                }
            }
        }

        //// There could be entities not matched with coreference sets. These are now added separately
        Set keySet  = kafEntityActorUriMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String uri =  keys.next();
            if (!coveredEntities.contains(uri)) {
                //   System.out.println("actor uri = " + uri);
                ArrayList<KafEntity> entities = kafEntityActorUriMap.get(uri);
                ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListFromEntities(baseUrl, kafSaxParser, entities);
                String entityId = "";
                entityId = Util.getEntityLabelUriFromEntities(entities);
               /* for (int i = 0; i < entities.size(); i++) {
                    KafEntity kafEntity = entities.get(i);
                    entityId += kafEntity.getId();
                }*/
                SemActor semActor = new SemActor();
                semActor.setId(entityUri + entityId);
                semActor.setNafMentions(mentionArrayList);
                semActor.addPhraseCountsForMentions(kafSaxParser);
                semActor.addConcepts(Util.getExternalReferences(entities));
                semActor.setIdByDBpediaReference();
                Util.addObject(semActors, semActor);
            }
        }
    }


    /**
     *
     * @param baseUrl
     * @param kafSaxParser
     * @param semActors
     */
    static void processNafFileForEntityWithoutCoreferenceSets (String entityUri, String baseUrl, KafSaxParser kafSaxParser,
                                                        ArrayList<SemObject> semActors
    ) {

        /**
         * Entity instances
         */


        /**
         * We first groups entities with the same URI
         */

        HashMap<String, ArrayList<KafEntity>> kafEntityActorUriMap = new HashMap<String, ArrayList<KafEntity>>();

        for (int j = 0; j < kafSaxParser.kafEntityArrayList.size(); j++) {
            KafEntity kafEntity = kafSaxParser.kafEntityArrayList.get(j);
            String uri = Util.getBestEntityUri(kafEntity);
            if (uri.isEmpty()) {
                kafEntity.setTokenStrings(kafSaxParser);
                if (Util.hasAlphaNumeric(kafEntity.getTokenString())) {
                    try {
                        uri = URLEncoder.encode(kafEntity.getTokenString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        //  e.printStackTrace();
                    }
                }
            }
            if (!uri.isEmpty()) {
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



        Set keySet  = kafEntityActorUriMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String uri =  keys.next();
            //   System.out.println("actor uri = " + uri);
            ArrayList<KafEntity> entities = kafEntityActorUriMap.get(uri);
            ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListFromEntities(baseUrl, kafSaxParser, entities);
            String entityId = "";
            entityId = Util.getEntityLabelUriFromEntities(entities);
            SemActor semActor = new SemActor();
            semActor.setId(entityUri + entityId);
            semActor.setNafMentions(mentionArrayList);
            semActor.addPhraseCountsForMentions(kafSaxParser);
            semActor.addConcepts(Util.getExternalReferences(entities));
            semActor.setIdByDBpediaReference();
            Util.addObject(semActors, semActor);
        }
    }


    /**
     * We should only run this function if we know which roles are not already covered by the entities and coreference sets.
     * The Util.addObject is supposed to check that!
     * @param project
     * @param kafSaxParser
     * @param semActors
     */
    static void processSrlForRemainingFramenetRoles (String project,
                                                     KafSaxParser kafSaxParser,
                                                     ArrayList<SemObject> semActors) {
        /*
            - We are missing good actors in predicates and coreference sets that are not entities
            - iterate over the SRL for roles with particular labels: A0, A1, A2, LOC, etc..
            - get the span:
            - check all actors for span match or span head match
            - if none create a new actor or place
        */
        HashMap<String, ArrayList<ArrayList<CorefTarget>>> mentionMap = new HashMap<String, ArrayList<ArrayList<CorefTarget>>>();
        String baseUrl = kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
        if (!baseUrl.toLowerCase().startsWith("http")) {
            baseUrl = ResourcesUri.nwrdata + project + "/" + kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
        }
        String entityUri = ResourcesUri.nwrdata+project+"/non-entities/";
        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
            for (int k = 0; k < kafEvent.getParticipants().size(); k++) {
                KafParticipant kafParticipant = kafEvent.getParticipants().get(k);

                //// SKIP LARGE PHRASES
                if (kafParticipant.getSpans().size()>Util.SPANMAXPARTICIPANT) {
                    continue;
                }
                if (kafParticipant.getSpans().size()<Util.SPANMINPARTICIPANT) {
                    continue;
                }

                // CERTAIN ROLES ARE NOT PROCESSED AND CAN BE SKIPPED
                if (!RoleLabels.validRole(kafParticipant.getRole())) {
                    continue;
                }
                if (!RoleLabels.hasFrameNetRole(kafParticipant)) {
                    ///// SKIP ROLE WITHOUT FRAMENET
                    continue;
                }
                //// we take all objects above threshold
                ArrayList<SemObject> semObjects = Util.getAllMatchingObject(kafSaxParser, kafParticipant, semActors);
                if (semObjects.size()==0) {
                    ///we have a missing additional actor
                    kafParticipant.setTokenStrings(kafSaxParser);
                    if (Util.hasAlphaNumeric(kafParticipant.getTokenString())) {
                        String uri = "";
                        KafMarkable kafMarkable = Util.getBestMatchingMarkable(kafSaxParser, kafParticipant);
                        if (kafMarkable!=null) {
                          //  System.out.println("kafMarkable.getId() = " + kafMarkable.getId());
                            uri = Util.getBestMarkableUri(kafMarkable);
                        }
                        else {
                            try {
                                uri = entityUri+URLEncoder.encode(kafParticipant.getTokenString(), "UTF-8").toLowerCase();
                            } catch (UnsupportedEncodingException e) {
                                //  e.printStackTrace();
                            }
                        }
                        if (!uri.isEmpty()) {
                            if (mentionMap.containsKey(uri)) {
                                //   System.out.println("srl = " + srl);
                                ArrayList<ArrayList<CorefTarget>> srlTargets = mentionMap.get(uri);
                                srlTargets.add(kafParticipant.getSpans());
                                mentionMap.put(uri, srlTargets);
                            } else {
                                ArrayList<ArrayList<CorefTarget>> srlTargets = new ArrayList<ArrayList<CorefTarget>>();
                                srlTargets.add(kafParticipant.getSpans());
                                mentionMap.put(uri, srlTargets);
                            }
                        }
                        else {
                        }
                    }
                    else {
                    }
                }
            }
        }
      //  System.out.println("mentionMap = " + mentionMap.size());
        Set keySet = mentionMap.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();  /// participant uri
          //  System.out.println("key = " + key);
            ArrayList<ArrayList<CorefTarget>> corefTargetArrayList = mentionMap.get(key);
            SemActor semActor = new SemActor();
            semActor.setId(key);
            ArrayList<NafMention> mentions = Util.getNafMentionArrayList(baseUrl, kafSaxParser, corefTargetArrayList);
            semActor.setNafMentions(mentions);
            semActor.addPhraseCountsForMentions(kafSaxParser);
            semActor.addConcepts(Util.getExternalReferencesSrlParticipants(kafSaxParser, key));
            semActor.setIdByDBpediaReference();
            Util.addObject(semActors, semActor); /// always add since there may be phrases that embed entity references
/*
            if (!Util.hasMentionAndSpanIntersect(semActors, semActor)) {
                Util.addObject(semActors, semActor);
            }
            else {
                System.out.println("IGNORING DUE TO MENTION OVERLAP");
            }
*/
        }
    }



    /**
     * @Deprecated: locations and actors are no longer separated
     * @param baseUrl
     * @param kafSaxParser
     * @param semActors
     * @param semPlaces
     */
    static void processNafFileForActorPlaceInstances (String baseUrl, KafSaxParser kafSaxParser,
                                       ArrayList<SemObject> semActors,
                                       ArrayList<SemObject> semPlaces
    ) {


   /*    DONE
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
          //  System.out.println("uri = " + uri);
            if (uri.isEmpty()) {
                kafEntity.setTokenStrings(kafSaxParser);

                if (Util.hasAlphaNumeric(kafEntity.getTokenString())) {
                    try {
                        uri = URLEncoder.encode(kafEntity.getTokenString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                       //  e.printStackTrace();
                    }
                }
                //uri = Util.alphaNumericUri(kafEntity.getTokenString());

               // System.out.println("uri = " + uri);
            }
            if (!uri.isEmpty()) {
                if (EntityTypes.isLOCATION(kafEntity.getType())) {
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
            /// next function replaces the id by the dbSpotLight URI if there is any
            semPlace.setIdByDBpediaReference();
            Util.addObject(semPlaces, semPlace);
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
            SemActor semActor = new SemActor();
            semActor.setId(baseUrl + entityId);
            semActor.setNafMentions(mentionArrayList);
            semActor.addPhraseCountsForMentions(kafSaxParser);
            semActor.addConcepts(Util.getExternalReferences(entities));
            semActor.setIdByDBpediaReference();
            Util.addObject(semActors, semActor);
        }


        /*
            - We are missing actors in predicates and coreference sets that are not entities
            - iterate over the SRL for roles with particular labels: A0, A1, A2, LOC, etc..
            - get the span:
            - check all actors for span match or span head match
            - if none create a new actor or place
         */
        /*HashMap<String, ArrayList<ArrayList<CorefTarget>>> locationReference = Util.getLocationMentionsHashMapFromSrl(kafSaxParser);
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
            Util.addObject(semPlaces, semPlace);

        }*/

       // System.out.println("semActors = " + semActors.size());
        /*HashMap<String, ArrayList<ArrayList<CorefTarget>>> actorReferences = Util.getActorCoreftargetSetsHashMapFromSrl(kafSaxParser);
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
            Util.addObject(semActors, semActor);
        }*/
       // System.out.println("semActors = " + semActors.size());
    }


    /**
     *
     * @param baseUrl
     * @param kafSaxParser
     * @param semActors
     */
    static void processNafFileForActorInstances (String baseUrl, KafSaxParser kafSaxParser,
                                       ArrayList<SemObject> semActors
    ) {


   /*    DONE
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
          //  System.out.println("uri = " + uri);
            if (uri.isEmpty()) {
                kafEntity.setTokenStrings(kafSaxParser);

                if (Util.hasAlphaNumeric(kafEntity.getTokenString())) {
                    try {
                        uri = URLEncoder.encode(kafEntity.getTokenString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                       //  e.printStackTrace();
                    }
                }
                //uri = Util.alphaNumericUri(kafEntity.getTokenString());

               // System.out.println("uri = " + uri);
            }
            if (!uri.isEmpty()) {
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


        Set keySet  = kafEntityActorUriMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String uri =  keys.next();
         //   System.out.println("actor uri = " + uri);
            ArrayList<KafEntity> entities = kafEntityActorUriMap.get(uri);
            ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListFromEntitiesAndCoreferences(baseUrl, kafSaxParser, entities);
            String entityId = "";
            for (int i = 0; i < entities.size(); i++) {
                KafEntity kafEntity = entities.get(i);
                entityId += kafEntity.getId();
            }
            SemActor semActor = new SemActor();
            semActor.setId(baseUrl + entityId);
            semActor.setNafMentions(mentionArrayList);
            semActor.addPhraseCountsForMentions(kafSaxParser);
            semActor.addConcepts(Util.getExternalReferences(entities));
            semActor.setIdByDBpediaReference();
            Util.addObject(semActors, semActor);
        }
    }


    /**
     * @Deprecated: locations and actors are no longer separated
     * @param baseUrl
     * @param kafSaxParser
     * @param semActors
     * @param semPlaces
     */
    static void processNafFileForActorPlaceCoreferenceSets (String baseUrl, KafSaxParser kafSaxParser,
                                       ArrayList<SemObject> semActors,
                                       ArrayList<SemObject> semPlaces
    ) {


   /*    DONE
          - iterate over all coreference sets for entities
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

        HashMap<String, ArrayList<KafEntity>> kafEntityUriMap = new HashMap<String, ArrayList<KafEntity>>();
        HashMap<String, ArrayList<KafEntity>> kafEntityLocationUriMap = new HashMap<String, ArrayList<KafEntity>>();
        HashMap<String, ArrayList<KafEntity>> kafEntityActorUriMap = new HashMap<String, ArrayList<KafEntity>>();

        for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
            KafCoreferenceSet kafCoreferenceSet = kafSaxParser.kafCorefenceArrayList.get(i);
            if (!kafCoreferenceSet.getType().toLowerCase().startsWith("event")) {
                //// this is an entity coreference set
                //// no we get all the predicates for this set.
                SemObject semObject = new SemObject();
                for (int j = 0; j < kafSaxParser.getKafEventArrayList().size(); j++) {
                    KafEvent event = kafSaxParser.getKafEventArrayList().get(j);
                    if (Util.hasCorefTargetArrayList(event.getSpans(), kafCoreferenceSet.getSetsOfSpans())) {
                        /// we want the event data
                        ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListFromPredicatesAndCoreferences(baseUrl, kafSaxParser, event);
                        semObject.addNafMentions(mentionArrayList);
                        semObject.addConcepts(event.getExternalReferences());
                    }

                }
                semObject.addPhraseCountsForMentions(kafSaxParser);
                String eventName = semObject.getTopPhraseAsLabel();
                //if (Util.hasAlphaNumeric(eventName)) {
                if (eventName.length()>=MINEVENTLABELSIZE) {
                    //semEvent.setId(baseUrl+event.getId());
                    //semEvent.setId(baseUrl + eventName + "Event");
                    String eventId = kafCoreferenceSet.getCoid() ;
                    semObject.setId(baseUrl + eventId);   // shorter form for triple store
                    semObject.setFactuality(kafSaxParser);
                    semObject.setIdByDBpediaReference();
                    semActors.add(semObject);
                }
            }
        }
        for (int j = 0; j < kafSaxParser.kafEntityArrayList.size(); j++) {
            KafEntity kafEntity = kafSaxParser.kafEntityArrayList.get(j);
            String uri = kafEntity.getFirstUriReference();
          //  System.out.println("uri = " + uri);
            if (uri.isEmpty()) {
                kafEntity.setTokenStrings(kafSaxParser);

                if (Util.hasAlphaNumeric(kafEntity.getTokenString())) {
                    try {
                        uri = URLEncoder.encode(kafEntity.getTokenString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                       //  e.printStackTrace();
                    }
                }
                //uri = Util.alphaNumericUri(kafEntity.getTokenString());

               // System.out.println("uri = " + uri);
            }
            if (!uri.isEmpty()) {
                if (EntityTypes.isLOCATION(kafEntity.getType())) {
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
            /// next function replaces the id by the dbSpotLight URI if there is any
            semPlace.setIdByDBpediaReference();
            Util.addObject(semPlaces, semPlace);
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
            SemActor semActor = new SemActor();
            semActor.setId(baseUrl + entityId);
            semActor.setNafMentions(mentionArrayList);
            semActor.addPhraseCountsForMentions(kafSaxParser);
            semActor.addConcepts(Util.getExternalReferences(entities));
            semActor.setIdByDBpediaReference();
            Util.addObject(semActors, semActor);
        }

    }


    /**
     * We get the actors from the SRL layer that are not represented
     * @param baseUrl
     * @param kafSaxParser
     * @param semActors
     */
    static void processNafFileForRemainingSrlActors (String entityUri, String baseUrl, KafSaxParser kafSaxParser,
                                       ArrayList<SemObject> semActors
    ) {


       // System.out.println("semActors = " + semActors.size());
        HashMap<String, ArrayList<ArrayList<CorefTarget>>> actorReferences = Util.getActorCoreftargetSetsHashMapFromSrl(kafSaxParser);
        Set keySet = actorReferences.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();  /// role id from srl

            ArrayList<ArrayList<CorefTarget>> corefTargetArrayList = actorReferences.get(key);
            SemActor semActor = new SemActor();
            semActor.setId(entityUri + key);
            ArrayList<NafMention> mentions = Util.getNafMentionArrayList(baseUrl, kafSaxParser, corefTargetArrayList);
            semActor.setNafMentions(mentions);
            semActor.addPhraseCountsForMentions(kafSaxParser);
            semActor.addConcepts(Util.getExternalReferencesSrlParticipants(kafSaxParser, key));
            semActor.setIdByDBpediaReference();
            Util.addObject(semActors, semActor);
        }
       // System.out.println("semActors = " + semActors.size());
    }

    /**
     * We get the actors from the SRL layer that are not represented
     * @param baseUrl
     * @param kafSaxParser
     * @param semActors
     */
    static void processNafFileForRemainingSrlPlaces (String entityUri, String baseUrl, KafSaxParser kafSaxParser,
                                       ArrayList<SemObject> semActors
    ) {

        HashMap<String, ArrayList<ArrayList<CorefTarget>>> locationReference = Util.getLocationMentionsHashMapFromSrl(kafSaxParser);
        Set keySet = locationReference.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();  /// role id from srl
            ArrayList<ArrayList<CorefTarget>> corefTargetArrayList = locationReference.get(key);
            SemPlace semPlace = new SemPlace();
            //semPlace.setId(baseUrl + key);
            semPlace.setId(entityUri + key);
            ArrayList<NafMention> mentions = Util.getNafMentionArrayList(baseUrl, kafSaxParser, corefTargetArrayList);
            semPlace.setNafMentions(mentions);
            semPlace.addPhraseCountsForMentions(kafSaxParser);
            semPlace.addConcepts(Util.getExternalReferencesSrlParticipants(kafSaxParser, key));
            semPlace.setIdByDBpediaReference();
            Util.addObject(semActors, semPlace);

        }
       // System.out.println("semActors = " + semActors.size());
    }


    static SemTime processNafFileForTimeInstances (String baseUrl, KafSaxParser kafSaxParser,
                                       ArrayList<SemObject> semTimes
    ) {
        SemTime docSemTime = new SemTime();
        if (!kafSaxParser.getKafMetaData().getCreationtime().isEmpty() &&
            !kafSaxParser.getKafMetaData().getCreationtime().equals("T000-00-0ZT00:00:00")) {
            //// we first store the publication date as a time
            docSemTime = new SemTime();
            //docSemTime.setId(baseUrl + "nafHeader" + "_" + "fileDesc" + "_" + "creationtime");
            docSemTime.setId(baseUrl + "dct"); // shorter form for triple store
            docSemTime.addPhraseCounts(kafSaxParser.getKafMetaData().getCreationtime());
            if (!docSemTime.getOwlTime().getDateString().isEmpty()) {
               // System.out.println("docSemTime.getOwlTime().getDateString() = " + docSemTime.getOwlTime().getDateString());
                //NafMention mention = new NafMention(baseUrl + "nafHeader" + "_" + "fileDesc" + "_" + "creationtime");
                NafMention mention = new NafMention(baseUrl + "dctm"); // shorter form for triple store
                docSemTime.addMentionUri(mention);
                docSemTime.getOwlTime().parsePublicationDate(kafSaxParser.getKafMetaData().getCreationtime());
                Util.addObject(semTimes, docSemTime);
            }
        }


        for (int i = 0; i < kafSaxParser.kafTimexLayer.size(); i++) {
            KafTimex timex = kafSaxParser.kafTimexLayer.get(i);
            if (!timex.getValue().trim().isEmpty() && !timex.getValue().startsWith("XXXX-")) {
               //System.out.println("timex.getValue() = " + timex.getId()+":"+timex.getValue());
                OwlTime aTime =new OwlTime();
                if (aTime.parseTimeExValue(timex.getValue(), docSemTime.getOwlTime())>-1) {
                    ArrayList<String> tokenSpanIds = timex.getSpans();
                    ArrayList<String> termSpanIds = kafSaxParser.convertTokensSpanToTermSpan(tokenSpanIds);
                    ArrayList<NafMention> mentions = Util.getNafMentionArrayListForTermIds(baseUrl, kafSaxParser, termSpanIds);
                    SemTime semTimeRole = new SemTime();
                    semTimeRole.setId(baseUrl + timex.getId());
                    semTimeRole.setNafMentions(mentions);
                    semTimeRole.addPhraseCountsForMentions(kafSaxParser);
                    semTimeRole.setOwlTime(aTime);
                    Util.addObject(semTimes, semTimeRole);
                }
            }
        }

        //// WE CAN TAKE THIS OUT IF TIMEX COVERS ALL
        //// we get time references from the SRL layer
/*        HashMap<String, ArrayList<ArrayList<CorefTarget>>> timeReferences = Util.getTimeMentionsHashMapFromSrl (kafSaxParser, docSemTime.getOwlTime());
        Set keySet = timeReferences.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if ((Util.hasAlphaNumeric(key))) {
                OwlTime aTime = new OwlTime();
                // System.out.println("key = " + key);
                if (aTime.parseStringDateWithDocTimeYearFallBack(key, docSemTime.getOwlTime()) > -1) {
                    ArrayList<ArrayList<CorefTarget>> corefTargetArrayList = timeReferences.get(key);
                    SemTime semTimeRole = new SemTime();
                    String id = "";
                    try {
                        id = URLEncoder.encode(key, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        // e.printStackTrace();
                    }
                    if (!id.isEmpty()) {
                        semTimeRole.setId(baseUrl + id);
                        //semTimeRole.setId(baseUrl + Util.alphaNumericUri(key));
                        ArrayList<NafMention> mentions = Util.getNafMentionArrayList(baseUrl, kafSaxParser, corefTargetArrayList);
                        semTimeRole.setNafMentions(mentions);
                        semTimeRole.addPhraseCountsForMentions(kafSaxParser);
                        semTimeRole.setOwlTime(aTime);
                        Util.addObject(semTimes, semTimeRole);
                    }
                } else {
                    //// phrase contains no useable info
                }
            }
            //System.out.println("aTime.toString() = " + aTime.toString());
        }*/

        return docSemTime;
    }


    /**
     * Main function to get the SEM relations.
     * @param baseUrl
     * @param kafSaxParser
     * @param semEvents
     * @param semActors
     * @param semPlaces
     * @param semTimes
     * @param semRelations
     * @param factRelations
     * @param docSemTime
     */
    static void processNafFileForRelations (String baseUrl, KafSaxParser kafSaxParser,
                                       ArrayList<SemObject> semEvents,
                                       ArrayList<SemObject> semActors,
                                       ArrayList<SemObject> semPlaces,
                                       ArrayList<SemObject> semTimes,
                                       ArrayList<SemRelation> semRelations,
                                       ArrayList<SemRelation> factRelations,
                                       SemTime docSemTime
    ) {

        /*   We check the factuality of each event mention by checking NafMention against the span of
             values in the factuality layer. The factuality layer version 1 uses tokens.
             If we have a value,
             we create a nwr:hasFactBankValue relation between the event and the value
             The URI of the relation is made unique by using a counter, e.g. factValue1, factValue2
         */

        for (int i = 0; i < semEvents.size(); i++) {
            SemObject semObject = semEvents.get(i);
            for (int j = 0; j < semObject.getNafMentions().size(); j++) {
                NafMention nafMention = semObject.getNafMentions().get(j);
                if (!nafMention.getFactuality().getPrediction().isEmpty()) {
                    Util.addMentionToFactRelations(nafMention, factRelations, nafMention.getFactuality().getPrediction(), baseUrl, semObject.getId());
                }
            }
        }

        /*
          We create mappings between the SemTime objects and the events. SemTime objects come either from the TimeEx layer
          or from the SRL layer. If they come from the Timex layer we have no information on how they relate to the event.
          We therefore check if they occur in the same sentence.
          Other options:
          - same + preceding sentence
          - same + preceding + following sentence
          - make a difference for SRL and Timex
         */
        int docTimeRelationCount = 0;
        int timexRelationCount = 0;
        for (int i = 0; i < semEvents.size(); i++) {
            SemObject semEvent = semEvents.get(i);
            boolean timeAnchor = false;

            for (int l = 0; l < semTimes.size(); l++) {
                SemObject semTime = semTimes.get(l);
                ArrayList<String> termIds = Util.sameSentenceRange(semEvent, semTime);
                if (termIds.size()>0) {
                    /// create sem relations
                    timexRelationCount++;
                    SemRelation semRelation = new SemRelation();
                    //String relationInstanceId = baseUrl+"timeRelation_"+timexRelationCount;
                    String relationInstanceId = baseUrl + "tr" + timexRelationCount;  // shorter form for triple store
                    semRelation.setId(relationInstanceId);
                   // System.out.println(semTime.getId() + ": termsIds.toString() = " + termIds.toString());
                    NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, termIds);
                    semRelation.addMention(mention);
                    semRelation.addPredicate("hasSemTime");
                    semRelation.setSubject(semEvent.getId());
                    semRelation.setObject(semTime.getId());
                    semRelations.add(semRelation);
                    // System.out.println("semRelation = " + semRelation.getSubject());
                    // System.out.println("semRelation.getObject() = " + semRelation.getObject());
                    timeAnchor = true;
                  //  break;
                }
            }

            if (!timeAnchor) {
                for (int l = 0; l < semTimes.size(); l++) {
                    SemObject semTime = semTimes.get(l);
                    ArrayList<String> termIds = Util.range1SentenceRange(semEvent, semTime);
                    if (termIds.size() > 0) {
                        /// create sem relations
                        timexRelationCount++;
                        SemRelation semRelation = new SemRelation();
                        //String relationInstanceId = baseUrl+"timeRelation_"+timexRelationCount;
                        String relationInstanceId = baseUrl + "tr" + timexRelationCount;  // shorter form for triple store
                        semRelation.setId(relationInstanceId);
                        //System.out.println(semTime.getId() + ": termsIds.toString() = " + termIds.toString());
                        NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, termIds);
                        semRelation.addMention(mention);
                        semRelation.addPredicate("hasSemTime");
                        semRelation.setSubject(semEvent.getId());
                        semRelation.setObject(semTime.getId());
                        semRelations.add(semRelation);
                        // System.out.println("semRelation = " + semRelation.getSubject());
                        // System.out.println("semRelation.getObject() = " + semRelation.getObject());
                        timeAnchor = true;
                      //  break;
                    }
                }
            }
            if (!timeAnchor) {
                for (int l = 0; l < semTimes.size(); l++) {
                    SemObject semTime = semTimes.get(l);
                    ArrayList<String> termIds = Util.rangemin2plus1SentenceRange(semEvent, semTime);
                    if (termIds.size() > 0) {
                        /// create sem relations
                        timexRelationCount++;
                        SemRelation semRelation = new SemRelation();
                        //String relationInstanceId = baseUrl+"timeRelation_"+timexRelationCount;
                        String relationInstanceId = baseUrl + "tr" + timexRelationCount;  // shorter form for triple store
                        semRelation.setId(relationInstanceId);
                        //System.out.println(semTime.getId() + ": termsIds.toString() = " + termIds.toString());
                        NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, termIds);
                        semRelation.addMention(mention);
                        semRelation.addPredicate("hasSemTime");
                        semRelation.setSubject(semEvent.getId());
                        semRelation.setObject(semTime.getId());
                        semRelations.add(semRelation);
                        // System.out.println("semRelation = " + semRelation.getSubject());
                        // System.out.println("semRelation.getObject() = " + semRelation.getObject());
                        timeAnchor = true;
                       // break;
                    }
                }
            }
            /*
               If there is no anchoring of an event to an SemTime object, we connect it here to the document time
             */
            if (!timeAnchor) {
                /// timeless event
                /// in all cases there is no time relations we link it to the docTime
                // System.out.println("docSemTime.toString() = " + docSemTime.toString());
                if (!docSemTime.getPhrase().isEmpty()) {
                    /// in all cases there is no time relations we link it to the docTime
                    docTimeRelationCount++;
                    SemRelation semRelation = new SemRelation();
                    // String relationInstanceId = baseUrl + "docTime_" + docTimeRelationCount;
                    String relationInstanceId = baseUrl + "dt" + docTimeRelationCount; // shorter form for triple store
                    semRelation.setId(relationInstanceId);
                    //// Since the doctime has no reference in the text, we use the mentions of the events to point to
                    semRelation.setNafMentions(semEvent.getNafMentions());
                    semRelation.addPredicate("hasSemTime");
                    semRelation.setSubject(semEvent.getId());
                    semRelation.setObject(docSemTime.getId());
                    semRelations.add(semRelation);
                }
            }
        }


        // NEXT WE RELATE ACTORS AND PLACES TO EVENTS
        ///
        // THIS IS NOT EASY DUE TO THE COMPLEX OVERLAP IN SPAN ACROSS ENTITIES AND ROLES IN SRL
        // IF THE SEMOBJECT IS BASED ON AN PARTICIPANT (SRL ROLE) IT IS OK
        // IF IT IS BASED ON AN ENTITY OR COREFSET WE NEED TO BE CAREFUL
        // THERE HAS TO BE SUFFICIENT OVERLAP OF THE CONTENT WORDS
        // THERE CAN STILL BE DIFFERENCES DUE TO THE FACT THAT MENTIONS ARE ANALYSED DIFFERENTLY
        // THE EXTENT CAN BE A WHOLE PHRASE OR JUST TH EHEAD OF A PHRASE DEPENDING ON THE MODULE
        //
        // WE DEFINED A FUNCTION THAT CHECKS THE OVERLAP WITH ALL THE SEMOBJECTS AND RETURNS THE ONE WITH THE HIGHEST OVERLAP
        // GIVEN A THRESHOLD FOR A MINIMUM OVERLAP. FOR THE OVERLAP WE CAN CALCULATE ALL THE SPAN ELEMENTS OR THE CONTENT WORDS.
        /*
            - iterate over de SRL layers
            - represent predicates and participants
            - check if they overlap with semObjects
            - if so use the instanceId
            - if not create a new instanceId
         */
        // DONE BUT STILL NEEDS TO BE TESTED

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
                // SHOULD NEVER OCCUR
            }
            else {
                for (int k = 0; k < kafEvent.getParticipants().size(); k++) {
                    KafParticipant kafParticipant = kafEvent.getParticipants().get(k);
                    // CERTAIN ROLES ARE NOT PROCESSED AND CAN BE SKIPPED
                    if (!RoleLabels.validRole(kafParticipant.getRole())) {
                        continue;
                    }
                    //// we take all objects above threshold
                    ArrayList<SemObject> semObjects = Util.getAllMatchingObject(kafSaxParser, kafParticipant, semActors);
                    for (int l = 0; l < semObjects.size(); l++) {
                        SemObject semObject = semObjects.get(l);
                        if (semObject!=null) {
                            SemRelation semRelation = new SemRelation();
                            String relationInstanceId = baseUrl + kafEvent.getId() + "," + kafParticipant.getId();
                            semRelation.setId(relationInstanceId);
/*
                            if (kafParticipant.getId().equals("rl130")) {
                                System.out.println(semObject.getId());
                            }
*/
                            ArrayList<String> termsIds = kafEvent.getSpanIds();
                            for (int j = 0; j < kafParticipant.getSpanIds().size(); j++) {
                                String s = kafParticipant.getSpanIds().get(j);
                                termsIds.add(s);
                            }
                            NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, termsIds);
                            semRelation.addMention(mention);
                            semRelation.addPredicate("hasSemActor");
                            //// check the source and prefix accordingly
                            semRelation.addPredicate(RoleLabels.normalizeProbBankValue(kafParticipant.getRole()));
                            for (int j = 0; j < kafParticipant.getExternalReferences().size(); j++) {
                                KafSense kafSense = kafParticipant.getExternalReferences().get(j);
                                semRelation.addPredicate(kafSense.getResource() + ":" + kafSense.getSensecode());
                            }
                            semRelation.setSubject(semEventId);
                            semRelation.setObject(semObject.getId());
                            semRelations.add(semRelation);
                        }
                    }
                    semObjects = Util.getAllMatchingObject(kafSaxParser, kafParticipant, semPlaces);
                    for (int l = 0; l < semObjects.size(); l++) {
                        SemObject semObject = semObjects.get(l);
                        if (semObject!=null) {
                            SemRelation semRelation = new SemRelation();
                            String relationInstanceId = baseUrl + kafEvent.getId() + "," + kafParticipant.getId();
                            semRelation.setId(relationInstanceId);

                            ArrayList<String> termsIds = kafEvent.getSpanIds();
                            for (int j = 0; j < kafParticipant.getSpanIds().size(); j++) {
                                String s = kafParticipant.getSpanIds().get(j);
                                termsIds.add(s);
                            }
                            NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, termsIds);
                            semRelation.addMention(mention);
                            semRelation.addPredicate("hasSemPlace");
                            semRelation.addPredicate(RoleLabels.normalizeProbBankValue(kafParticipant.getRole()));
                            for (int j = 0; j < kafParticipant.getExternalReferences().size(); j++) {
                                KafSense kafSense = kafParticipant.getExternalReferences().get(j);
                                semRelation.addPredicate(kafSense.getResource() + ":" + kafSense.getSensecode());
                            }
                            semRelation.setSubject(semEventId);
                            semRelation.setObject(semObject.getId());
                            semRelations.add(semRelation);
                        }
                    }
                }
            }
        }
    }



    static public void main (String [] args) {
        String pathToNafFile = "";
        pathToNafFile = "/Users/piek/Desktop/English Pipeline/naf3.0/obama_v3_event_coref_in.xml";
        String project = "cars";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--naf-file") && args.length>(i+1)) {
                pathToNafFile = args[i+1];
            }
            else if (arg.equals("--project") && args.length>(i+1)) {
                project = args[i+1];
            }
        }
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        ArrayList<SemRelation> factRelations = new ArrayList<SemRelation>();
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(pathToNafFile);
        processNafFileWithAdditionalRoles(project, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations);
       // System.out.println("semActors.size() = " + semActors.size());
       // processSrlForRemainingFramenetRoles(project, kafSaxParser, semActors);
       // System.out.println("semActors.size() = " + semActors.size());
        try {
           // System.out.println("semEvents = " + semEvents.size());
            String pathToTrigFile = pathToNafFile+".trig";
            OutputStream fos = new FileOutputStream(pathToTrigFile);
            JenaSerialization.serializeJena(fos,
                    semEvents, semActors, semPlaces, semTimes, semRelations, factRelations, null, false);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
