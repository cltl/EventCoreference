package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.util.EuroVoc;
import eu.newsreader.eventcoreference.util.RoleLabels;
import eu.newsreader.eventcoreference.util.TimeLanguage;
import eu.newsreader.eventcoreference.util.Util;

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
public class GetSemFromNaf {

    static public boolean DOCTIME = true;
    static public boolean CONTEXTTIME = true;
    static public boolean POCUS = true;
    static public boolean DOMINANTURI = true;
    static public int MINEVENTLABELSIZE = 1;
    static final public String ID_SEPARATOR = "#";
    static final public String URI_SEPARATOR = "_";
    static public HashMap<String, String> eurovoc = new HashMap<String, String>();

    static public void initEurovoc (String path, String lang) {
        EuroVoc.readEuroVoc(path, lang);
        eurovoc = EuroVoc.labelUriMap;
    }


    /**
     * This is the standard function to interpret a NAF file to create SemObjects and SemRelations
     * @param project
     * @param kafSaxParser
     * @param semEvents
     * @param semActors
     * @param semTimes
     * @param semRelations
     */
    static public void processNafFile(String project, KafSaxParser kafSaxParser,
                                      ArrayList<SemObject> semEvents,
                                      ArrayList<SemObject> semActors,
                                      ArrayList<SemTime> semTimes,
                                      ArrayList<SemRelation> semRelations,
                                      boolean ADDITIONALROLES,
                                      boolean doctime,
                                      boolean contexttime
    ) {

        DOCTIME = doctime;
        CONTEXTTIME = contexttime;
        /// @deprecated since it is included in the event-coref module for NAF
        //// THIS FIX IS NEEDED BECAUSE SOME OF THE COREF SETS ARE TOO BIG
        //fixEventCoreferenceSets(kafSaxParser);

        //// THIS IS NEEDED TO FILTER ESO MAPPING AND IGNORE OTHERS
        //fixExternalReferencesSrl(kafSaxParser);
        //// THIS IS NEEDED TO USE RERANKING OF DBPEDIA URIs

        //// ALSO PREFERS ENGLISH REFERENCES
        ///fixExternalReferencesEntities(kafSaxParser);

        /// if CROSSLINGUAL
        //useEnglishExternalReferences(kafSaxParser);


        TimeLanguage.setLanguage(kafSaxParser.getLanguage());
        String baseUrl = kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
        String entityUri = ResourcesUri.nwrdata + project + "/entities/";
        if (!baseUrl.toLowerCase().startsWith("http")) {
           //  System.out.println("baseUrl = " + baseUrl);
            baseUrl = ResourcesUri.nwrdata + project + "/" + kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
        }
        processNafFileForEntityCoreferenceSets(entityUri, baseUrl, kafSaxParser, semActors);
        if (ADDITIONALROLES) {
            processSrlForRemainingFramenetRoles(project, kafSaxParser, semActors);
        }
        processNafFileForTimeInstances(baseUrl, kafSaxParser, semTimes);

        processNafFileForEventCoreferenceSets(baseUrl, kafSaxParser, semEvents);

        //// THIS FIX IS NEEDED BECAUSE SOMETIMES SRL GENERATES IDENTICAL SPANS FOR PREDICATES AND ACTORS. WE REMOVE EVENTS THAT ARE IDENTICAL WITH ACTORS
        //Util.filterOverlapEventsEntities(semEvents, semActors);

        processNafFileForRelations(baseUrl,
                kafSaxParser, semEvents, semActors, semTimes, semRelations);
    }




    static void processNafFileForEventCoreferenceSets(String baseUrl, KafSaxParser kafSaxParser,
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
                semEvent.addHypers(kafCoreferenceSet.getHypernymFromExternalReferences());
                semEvent.addLcses(kafCoreferenceSet.getLcsFromExternalReferences());
                semEvent.addConcepts(kafCoreferenceSet.getDirectExternalReferences());
                for (int j = 0; j < kafSaxParser.getKafEventArrayList().size(); j++) {
                    KafEvent event = kafSaxParser.getKafEventArrayList().get(j);
                    if (Util.hasCorefTargetArrayList(event.getSpans(), kafCoreferenceSet.getSetsOfSpans())) {
                        /// we want the event data
                        ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListFromPredicatesAndCoreferences(baseUrl, kafSaxParser, event);
                        semEvent.addNafMentions(mentionArrayList);
                        semEvent.addNafId(event.getId());/// needed to connect to timeAnchors that have predicate ids as spans
                        semEvent.addConcepts(event.getExternalReferences());  /// these are all concepts added by the SRL
                        semEvent.setTopics(kafSaxParser.kafTopicsArrayList);
                        semEvent.getUriForTopicLabel(eurovoc);
                     //   semEvent.addConceptsExcept(event.getExternalReferences(), "WordNet");  /// these are concepts added by the SRL except for the WordNet references since we assume they come from the coreference sets
                    }
                }
                semEvent.addPhraseCountsForMentions(kafSaxParser);
                String eventName = semEvent.getTopPhraseAsLabel();
                if (eventName.length() >= MINEVENTLABELSIZE) {
                    //semEvent.setId(baseUrl+event.getId());
                    //semEvent.setId(baseUrl + eventName + "Event");
                    String eventId = kafCoreferenceSet.getCoid().replace("coevent", "ev");
                    semEvent.setId(baseUrl + eventId);   // shorter form for triple store
                    semEvent.addFactuality(kafSaxParser);
                    semEvent.setIdByDBpediaReference();
                    semEvents.add(semEvent);
                }
            }
        }
       // System.out.println("semEvents.size() = " + semEvents.size());
    }


    static void processNafFileForEventWithoutCoreferenceSets(String baseUrl, KafSaxParser kafSaxParser,
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
            semEvent.addNafId(kafEvent.getId());/// needed to connect to timeAnchors that have predicate ids as spans
            semEvent.addNafMentions(mentionArrayList);
            semEvent.addConcepts(kafEvent.getExternalReferences());
            semEvent.addPhraseCountsForMentions(kafSaxParser);
            String eventName = semEvent.getTopPhraseAsLabel();
            //if (Util.hasAlphaNumeric(eventName)) {
            if (eventName.length() >= MINEVENTLABELSIZE) {
                //semEvent.setId(baseUrl+event.getId());
                //semEvent.setId(baseUrl + eventName + "Event");
                String eventId = kafEvent.getId();
                semEvent.setId(baseUrl + eventId);   // shorter form for triple store
                semEvent.addFactuality(kafSaxParser);
                semEvent.setIdByDBpediaReference();
                semEvents.add(semEvent);
            }
        }
    }


    /**
     * @param baseUrl
     * @param kafSaxParser
     * @param semActors
     */
    static void processNafFileForEntityCoreferenceSets(String entityUri, String baseUrl, KafSaxParser kafSaxParser,
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
        HashMap<String, ArrayList<KafCoreferenceSet>> kafCoreferenceUriMap = new HashMap<String, ArrayList<KafCoreferenceSet>>();

        for (int j = 0; j < kafSaxParser.kafEntityArrayList.size(); j++) {
            KafEntity kafEntity = kafSaxParser.kafEntityArrayList.get(j);
            String uri = "";
            if (DOMINANTURI) {
                uri = Util.getDominantUri(kafEntity).getSensecode();
            }
            if (uri.isEmpty() && POCUS) {
                if (!Util.hasPocusUri(kafEntity) && Util.supersededByPocus(kafSaxParser, kafEntity)) {
                    continue;
                }
                else {
                    uri = Util.getBestEntityUriPreferPocus(kafEntity);
                }
            }
            else {
                uri = Util.getBestEntityUri(kafEntity);
            }
            if (uri.isEmpty()) {
                KafMarkable kafMarkable = Util.getBestMatchingMarkable(kafSaxParser, kafEntity.getTermIds());
                if (kafMarkable != null) {
                    //  System.out.println("kafMarkable.getId() = " + kafMarkable.getId());
                    uri = Util.getBestMarkableUri(kafMarkable);
                }
                if (uri.isEmpty()) {
                    kafEntity.setTokenStrings(kafSaxParser);
                    if (Util.hasAlphaNumeric(kafEntity.getTokenString())) {
                        try {
                            uri = URLEncoder.encode(kafEntity.getTokenString(), "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            /*System.out.println("kafEntity.getTokenString() = " + kafEntity.getTokenString());
                            System.out.println("uri = " + uri);
                              e.printStackTrace();*/
                        }
                    }
                }
            }
            if (!uri.isEmpty()) {
                if (uri.toLowerCase().indexOf("/dbpedia")>-1) {
                  //  System.out.println("dbpedia uri = " + uri);
                }
                else {
                   // System.out.println("other uri = " + uri);
                    continue;
                }
                if (kafEntityActorUriMap.containsKey(uri)) {
                    ArrayList<KafEntity> entities = kafEntityActorUriMap.get(uri);
                    entities.add(kafEntity);
                    kafEntityActorUriMap.put(uri, entities);
                } else {
                    ArrayList<KafEntity> entities = new ArrayList<KafEntity>();
                    entities.add(kafEntity);
                    kafEntityActorUriMap.put(uri, entities);
                }
            }
        }

        /**
         * Next we iterate over all the coreference sets that are not events
         * WE then select the entity which has most overlap with the spans.
         * We add the coreference set to the mentions of the entity
         */

/*        for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
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
                if (!topEntity.isEmpty()) {
                    coveredEntities.add(topEntity);
                    ArrayList<KafEntity> entities = kafEntityActorUriMap.get(topEntity);
                    ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListFromEntitiesAndCoreferenceSet(baseUrl, kafSaxParser, entities, kafCoreferenceSet);
                    //ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListFromEntitiesAndCoreferences(baseUrl, kafSaxParser, entities);
                    String entityId = "";
                    entityId = Util.getEntityLabelUriFromEntities(kafSaxParser, entities);
                    if (entityId.isEmpty()) {
                        entityId = baseUrl+"e"+semActors.size();
                    }
                    else {
                        entityId = entityUri+entityId;
                    }
                    SemActor semActor = new SemActor(SemObject.ENTITY);
                    semActor.setId(entityId);
                    semActor.setNafMentions(mentionArrayList);
                    semActor.addPhraseCountsForMentions(kafSaxParser);
                    semActor.addConcepts(Util.getExternalReferences(entities));
                    semActor.setIdByDBpediaReference();
                    Util.addObject(semActors, semActor);
                } else {
                    ///// coreference sets without any entity matches are ignored so far!!!!!!!!!
                    //   System.out.println("not matched kafCoreferenceSet.getCoid() = " + kafCoreferenceSet.getCoid());
                }
            }
        }

        //// There could be entities not matched with coreference sets. These are now added separately
        Set keySet = kafEntityActorUriMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String uri = keys.next();
          //  System.out.println("uri = " + uri);
            if (!coveredEntities.contains(uri)) {
                ArrayList<KafEntity> entities = kafEntityActorUriMap.get(uri);
                ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListFromEntities(baseUrl, kafSaxParser, entities);
                String entityId = "";
                entityId = Util.getEntityLabelUriFromEntities(kafSaxParser, entities);
                if (entityId.isEmpty()) {
                    entityId = baseUrl+"e"+semActors.size();
                }
                else {
                    entityId = entityUri+entityId;
                }
                SemActor semActor = new SemActor(SemObject.ENTITY);
                semActor.setId(entityId);
                semActor.setNafMentions(mentionArrayList);
                semActor.addPhraseCountsForMentions(kafSaxParser);
                semActor.addConcepts(Util.getExternalReferences(entities));
                semActor.setIdByDBpediaReference();
                Util.addObject(semActors, semActor);
            }
        }

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
                if (!topEntity.isEmpty()) {
                    if (kafCoreferenceUriMap.containsKey(topEntity)) {
                        ArrayList<KafCoreferenceSet> set = kafCoreferenceUriMap.get(topEntity);
                        set.add(kafCoreferenceSet);
                        kafCoreferenceUriMap.put(topEntity, set);
                    } else {
                        ArrayList<KafCoreferenceSet> set = new ArrayList<KafCoreferenceSet>();
                        set.add(kafCoreferenceSet);
                        kafCoreferenceUriMap.put(topEntity, set);
                    }
                }
            }
        }

        Set keySet = kafEntityActorUriMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String uri = keys.next();
            //  System.out.println("uri = " + uri);
                ArrayList<KafEntity> entities = kafEntityActorUriMap.get(uri);
                ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListFromEntities(baseUrl, kafSaxParser, entities);
                if (kafCoreferenceUriMap.containsKey(uri)) {
                    ArrayList<KafCoreferenceSet> coreferenceSets = kafCoreferenceUriMap.get(uri);
                    for (int i = 0; i < coreferenceSets.size(); i++) {
                        KafCoreferenceSet kafCoreferenceSet = coreferenceSets.get(i);
                        for (int j = 0; j < kafCoreferenceSet.getSetsOfSpans().size(); j++) {
                            ArrayList<CorefTarget> corefTargets = kafCoreferenceSet.getSetsOfSpans().get(j);
                            if (corefTargets.size()<=Util.SPANMAXCOREFERENTSET) {
                                NafMention mention = Util.getNafMentionForCorefTargets(baseUrl, kafSaxParser, corefTargets);
                                if (!Util.hasMention(mentionArrayList, mention)) {
                                    // System.out.println("corefTargets.toString() = " + corefTargets.toString());
                                    mentionArrayList.add(mention);
                                }
                            }
                        }

                    }
                }
                String entityId = "";
                entityId = Util.getEntityLabelUriFromEntities(kafSaxParser, entities);
                if (entityId.isEmpty()) {
                    entityId = baseUrl+"e"+semActors.size();
                }
                else {
                    entityId = entityUri+entityId;
                }
                SemActor semActor = new SemActor(SemObject.ENTITY);
                semActor.setId(entityId);
                semActor.setNafMentions(mentionArrayList);
                semActor.addPhraseCountsForMentions(kafSaxParser);
                semActor.addConcepts(Util.getExternalReferences(entities));
                semActor.setIdByDBpediaReference();
                Util.addObject(semActors, semActor);
        }


/*
        for (int i = 0; i < semActors.size(); i++) {
            SemObject semObject = semActors.get(i);
            System.out.println("semObject.getId() = " + semObject.getId());
            System.out.println("semObject.getURI() = " + semObject.getURI());
        }
*/
    }




 /*   *//**
     * @param baseUrl
     * @param kafSaxParser
     * @param semActors
     *//*
    static void processNafFileForEntityWithoutCoreferenceSets(String entityUri, String baseUrl, KafSaxParser kafSaxParser,
                                                              ArrayList<SemObject> semActors
    ) {

        *//**
         * Entity instances
         *//*


        *//**
         * We first groups entities with the same URI
         *//*

        HashMap<String, ArrayList<KafEntity>> kafEntityActorUriMap = new HashMap<String, ArrayList<KafEntity>>();

        for (int j = 0; j < kafSaxParser.kafEntityArrayList.size(); j++) {
            KafEntity kafEntity = kafSaxParser.kafEntityArrayList.get(j);
            String uri = Util.getBestEntityUri(kafEntity);
            if (uri.isEmpty()) {
                KafMarkable kafMarkable = Util.getBestMatchingMarkable(kafSaxParser, kafEntity.getTermIds());
                if (kafMarkable != null) {
                    //  System.out.println("kafMarkable.getId() = " + kafMarkable.getId());
                    uri = Util.getBestMarkableUri(kafMarkable);
                }
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
            }
            if (!uri.isEmpty()) {
                if (kafEntityActorUriMap.containsKey(uri)) {
                    ArrayList<KafEntity> entities = kafEntityActorUriMap.get(uri);
                    entities.add(kafEntity);
                    kafEntityActorUriMap.put(uri, entities);
                } else {
                    ArrayList<KafEntity> entities = new ArrayList<KafEntity>();
                    entities.add(kafEntity);
                    kafEntityActorUriMap.put(uri, entities);
                }
            }
        }


        Set keySet = kafEntityActorUriMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String uri = keys.next();
            //   System.out.println("actor uri = " + uri);
            ArrayList<KafEntity> entities = kafEntityActorUriMap.get(uri);
            ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListFromEntities(baseUrl, kafSaxParser, entities);
            String entityId = "";
            entityId = Util.getEntityLabelUriFromEntities(kafSaxParser, entities);
            SemActor semActor = new SemActor(SemObject.ENTITY);
            semActor.setId(entityUri + entityId);
            semActor.setNafMentions(mentionArrayList);
            semActor.addPhraseCountsForMentions(kafSaxParser);
            semActor.addConcepts(Util.getExternalReferences(entities));
            semActor.setIdByDBpediaReference();
            Util.addObject(semActors, semActor);
        }
    }*/


    /**
     * We should only run this function if we know which roles are not already covered by the entities and coreference sets.
     * The Util.addObject is supposed to check that!
     *
     * @param project
     * @param kafSaxParser
     * @param semActors
     */
    static void processSrlForRemainingFramenetRoles(String project,
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
        String entityUri = ResourcesUri.nwrdata + project + "/non-entities/";
        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
            for (int k = 0; k < kafEvent.getParticipants().size(); k++) {
                KafParticipant kafParticipant = kafEvent.getParticipants().get(k);

                //// SKIP LARGE PHRASES
/*
                if (kafParticipant.getSpans().size() > Util.SPANMAXPARTICIPANT) {
                    continue;
                }
*/
/*
                if (kafParticipant.getSpans().size() < Util.SPANMINPARTICIPANT) {
                    continue;
                }
*/

                // CERTAIN ROLES ARE NOT PROCESSED AND CAN BE SKIPPED
                if (!RoleLabels.validRole(kafParticipant.getRole())) {
                    continue;
                }
                if (!RoleLabels.hasFrameNetRole(kafParticipant)
                        &&!RoleLabels.isPRIMEPARTICIPANT(kafParticipant.getRole()) &&
                        !RoleLabels.isSECONDPARTICIPANT(kafParticipant.getRole()) &&
                        !RoleLabels.hasESORole(kafParticipant)
                        ) {
                    ///// SKIP ROLE WITHOUT FRAMENET PRIME OR ESO
                    continue;
                }
                //// we take all objects above threshold
                ArrayList<SemObject> semObjects = Util.getAllMatchingObject(kafSaxParser, kafParticipant, semActors);
                if (semObjects.size() == 0) {
                    ///we have a missing additional actor
                    kafParticipant.setTokenStrings(kafSaxParser);
                    if (Util.hasAlphaNumeric(kafParticipant.getTokenString())) {
                        try {
                            String uri = entityUri + URLEncoder.encode(kafParticipant.getTokenString(), "UTF-8").toLowerCase();
                            SemActor semActor = new SemActor(SemObject.NONENTITY);
                            semActor.setId(uri);
                            ArrayList<ArrayList<CorefTarget>> srlTargets = new ArrayList<ArrayList<CorefTarget>>();
                            srlTargets.add(kafParticipant.getSpans());
                            ArrayList<NafMention> mentions = Util.getNafMentionArrayList(baseUrl, kafSaxParser, srlTargets);
                            semActor.setNafMentions(mentions);
                            semActor.addPhraseCountsForMentions(kafSaxParser);
                            KafMarkable kafMarkable = Util.getBestMatchingMarkable(kafSaxParser, kafParticipant.getSpanIds());
                            if (kafMarkable != null) {
                                //  System.out.println("kafMarkable.getId() = " + kafMarkable.getId());
                                KafSense sense = Util.getBestScoringExternalReference(kafMarkable.getExternalReferences());
                                semActor.addConcept(sense);
                            }
                            Util.addObject(semActors, semActor); /// always add since there may be phrases that embed entity references

                        } catch (UnsupportedEncodingException e) {
                            //  e.printStackTrace();
                        }
                    }
                    else {
                     //   System.out.println("kafParticipant.getTokenString() = " + kafParticipant.getTokenString());
                    }
                }
                else {
                  //  System.out.println("semObjects.size() = " + semObjects.size());
                }
            }
        }
    }

    /**
     * We should only run this function if we know which roles are not already covered by the entities and coreference sets.
     * The Util.addObject is supposed to check that!
     *
     * @param project
     * @param kafSaxParser
     * @param semActors
     */
/*    static void processSrlForRemainingFramenetRolesOrg(String project,
                                                    KafSaxParser kafSaxParser,
                                                    ArrayList<SemObject> semActors) {
        *//*
            - We are missing good actors in predicates and coreference sets that are not entities
            - iterate over the SRL for roles with particular labels: A0, A1, A2, LOC, etc..
            - get the span:
            - check all actors for span match or span head match
            - if none create a new actor or place
        *//*
        HashMap<String, ArrayList<ArrayList<CorefTarget>>> mentionMap = new HashMap<String, ArrayList<ArrayList<CorefTarget>>>();
        String baseUrl = kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
        if (!baseUrl.toLowerCase().startsWith("http")) {
            baseUrl = ResourcesUri.nwrdata + project + "/" + kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
        }
        String entityUri = ResourcesUri.nwrdata + project + "/non-entities/";
        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
            for (int k = 0; k < kafEvent.getParticipants().size(); k++) {
                KafParticipant kafParticipant = kafEvent.getParticipants().get(k);

                //// SKIP LARGE PHRASES
                if (kafParticipant.getSpans().size() > Util.SPANMAXPARTICIPANT) {
                    continue;
                }
                if (kafParticipant.getSpans().size() < Util.SPANMINPARTICIPANT) {
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
                if (semObjects.size() == 0) {
                    ///we have a missing additional actor
                    kafParticipant.setTokenStrings(kafSaxParser);
                    if (Util.hasAlphaNumeric(kafParticipant.getTokenString())) {
                        String uri = "";
                        KafMarkable kafMarkable = Util.getBestMatchingMarkable(kafSaxParser, kafParticipant.getSpanIds());
                        if (kafMarkable != null) {
                            //  System.out.println("kafMarkable.getId() = " + kafMarkable.getId());
                            uri = Util.getBestMarkableUri(kafMarkable);
                        } else {
                            try {
                                uri = entityUri + URLEncoder.encode(kafParticipant.getTokenString(), "UTF-8").toLowerCase();
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
                        } else {
                        }
                    } else {
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
            SemActor semActor = new SemActor(SemObject.NONENTITY);
            semActor.setId(key);
            ArrayList<NafMention> mentions = Util.getNafMentionArrayList(baseUrl, kafSaxParser, corefTargetArrayList);
            semActor.setNafMentions(mentions);
            semActor.addPhraseCountsForMentions(kafSaxParser);
            semActor.addConcepts(Util.getExternalReferencesSrlParticipants(kafSaxParser, key));
            semActor.setIdByDBpediaReference();
            Util.addObject(semActors, semActor); /// always add since there may be phrases that embed entity references
        }
    }*/


    /** @Deprecated
     * This function interprets all timex elements and returns the document creation time as a SemTime object (which is also inserted as a the
     * first SemTime object in the array
     * @param baseUrl
     * @param kafSaxParser
     * @param semTimes
     * @return
     */
    static SemTime processNafFileForTimeInstancesAndDocumentCreationTime(String baseUrl, KafSaxParser kafSaxParser,
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
            if (!docSemTime.getOwlTime().getDateStringURI().isEmpty()) {
                // System.out.println("docSemTime.getOwlTime().getDateStringURI() = " + docSemTime.getOwlTime().getDateStringURI());
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
                OwlTime aTime = new OwlTime();
                if (aTime.parseTimeExValue(timex.getValue()) > -1) {
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

        for (int i = 0; i < semTimes.size(); i++) {
            SemObject semObject = semTimes.get(i);
            System.out.println("semObject.getId() = " + semObject.getId());
            System.out.println("semObject.getTermIds().toString() = " + semObject.getTermIds().toString());
        }

        return docSemTime;
    }


    /**
     * This function interprets all timex elements and does NOT return the document creation time as a SemTime object.
     * We assume that the first SemTime object is tmx0, which represents the document creation time
     * @param baseUrl
     * @param kafSaxParser
     * @param semTimes
     * @return
     */
    static void processNafFileForTimeInstances(String baseUrl, KafSaxParser kafSaxParser,
                                                  ArrayList<SemTime> semTimes
    ) {
        if (kafSaxParser.kafTimexLayer.size()>0) {
            for (int i = 0; i < kafSaxParser.kafTimexLayer.size(); i++) {
                KafTimex timex = kafSaxParser.kafTimexLayer.get(i);
                if (timex.getFunctionInDocument().equals(KafTimex.functionInDocumentCreationTime) && !DOCTIME) {
                    continue;
                }
                if (!timex.getValue().trim().isEmpty() &&
                        (!timex.getValue().startsWith("XXXX-"))  /// year unknown
                         &&
                        (!timex.getValue().equalsIgnoreCase("PRESENT_REF"))
                         &&
                        (!timex.getValue().equalsIgnoreCase("FUTURE_REF"))
                         &&
                        (!timex.getValue().equalsIgnoreCase("PAST_REF"))
                        )
                {
                    if (timex.getType().equalsIgnoreCase("duration")) {  /// periods, e.g. "PXY", "P9M", "P4Y",
                        SemTime semTime = new SemTime();
                        semTime.setType(TimeTypes.DURATION);
                        if (!timex.getBeginPoint().isEmpty()) {
                            OwlTime aTime = new OwlTime();
                            String value = Util.getValueForTimex(kafSaxParser.kafTimexLayer, timex.getBeginPoint());
                            if (aTime.parseTimeExValue(value) > -1) {
                                semTime.setOwlTimeBegin(aTime);
                            }
                        }
                        if (!timex.getEndPoint().isEmpty()) {
                            OwlTime aTime = new OwlTime();
                            String value = Util.getValueForTimex(kafSaxParser.kafTimexLayer, timex.getEndPoint());
                            if (aTime.parseTimeExValue(value) > -1) {
                                semTime.setOwlTimeEnd(aTime);
                            }
                        }
                        if (timex.getBeginPoint().isEmpty() && timex.getEndPoint().isEmpty()) {
                            //// This duration is not anchored so we ignore it
                            /*if (semTime.getOwlTime().getMonth().isEmpty()) {
                                semTime.interpretYearAsPeriod();
                            } else if (semTime.getOwlTime().getDay().isEmpty()) {
                                semTime.interpretMonthAsPeriod();
                            }*/
                        }
                        else {
                            semTime.setId(baseUrl + timex.getId());

                            ArrayList<String> tokenSpanIds = timex.getSpans();
                            if (tokenSpanIds.size() > 0) {
                                ArrayList<String> termSpanIds = kafSaxParser.convertTokensSpanToTermSpan(tokenSpanIds);
/*                                ArrayList<NafMention> mentions = Util.getNafMentionArrayListForTermIds(baseUrl, kafSaxParser, termSpanIds);
                                semTime.setNafMentions(mentions);*/
                                NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, termSpanIds);
                                semTime.addNafMention(mention);
                                semTime.addPhraseCountsForMentions(kafSaxParser);
                            }
                            semTimes.add(semTime);
                        }
                    }
                    else {
                        OwlTime aTime = new OwlTime();
                        if (aTime.parseTimeExValue(timex.getValue()) > -1) {
                            SemTime semTime = new SemTime();
                            semTime.setOwlTime(aTime);

                            //// if these are interpreted as proper time anchors, we can take out the next conditionals
                            if (aTime.getMonth().toLowerCase().startsWith("q")) {
                                semTime.setType(TimeTypes.QUARTER);
                                semTime.interpretQuarterAsPeriod();
                            } else if (timex.getType().equalsIgnoreCase("date")) {
                                semTime.setType(TimeTypes.DATE);
                            }

                            /////////////////////////////////////////////////////////////////////////////////////////

                            semTime.setId(baseUrl + timex.getId());

                            ArrayList<String> tokenSpanIds = timex.getSpans();
                            if (tokenSpanIds.size() == 0) {
                                /// maybe tmx0 or another timex without spans (apparently there are cases like that)
/*                                if (!aTime.getDateLabel().isEmpty()) {
                                    semTime.addPhraseCounts(aTime.getDateLabel());
                                }
                                else {
                                    //// we ignore this timex. We could not set the date info and there is no span......
                                }*/
                            } else {
                                ArrayList<String> termSpanIds = kafSaxParser.convertTokensSpanToTermSpan(tokenSpanIds);

                               /* ArrayList<NafMention> mentions = Util.getNafMentionArrayListForTermIds(baseUrl, kafSaxParser, termSpanIds);
                                semTime.setNafMentions(mentions);*/
                                NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, termSpanIds);
                                semTime.addNafMention(mention);
                                semTime.addPhraseCountsForMentions(kafSaxParser);
                            }
                            semTimes.add(semTime);

                        }
                    }
                }
            }
            /// PRINT CHECK
            /*for (int i = 0; i < semTimes.size(); i++) {
                SemTime semObject = semTimes.get(i);
                System.out.println("semObject.getId() = " + semObject.getId());
                System.out.println("semObject.getTermIds().toString() = " + semObject.getTermIds().toString());
                System.out.println("semObject.getOwlTime().getDateLabel() = " + semObject.getOwlTime().getDateLabel());
                System.out.println("semObject.getOwlTimeBegin().getDateLabel() = " + semObject.getOwlTimeBegin().getDateLabel());
                System.out.println("semObject.getOwlTimeEnd().getDateLabel() = " + semObject.getOwlTimeEnd().getDateLabel());
            }*/
        }
    }


    /**
     * Main function to get the SEM relations.
     *
     * @param baseUrl
     * @param kafSaxParser
     * @param semEvents
     * @param semActors
     * @param semTimes
     * @param semRelations
     */
    static void processNafFileForRelations(String baseUrl, KafSaxParser kafSaxParser,
                                           ArrayList<SemObject> semEvents,
                                           ArrayList<SemObject> semActors,
                                           ArrayList<SemTime> semTimes,
                                           ArrayList<SemRelation> semRelations
    ) {
        ////@DEPRECATED factuality is now extracted through the perspective module separately
        /*   We check the factuality of each event mention by checking NafMention against the span of
             values in the factuality layer. The factuality layer version 1 uses tokens.
             If we have a value,
             we create a nwr:hasFactBankValue relation between the event and the value
             The URI of the relation is made unique by using a counter, e.g. factValue1, factValue2
         */

       /* for (int i = 0; i < semEvents.size(); i++) {
            SemObject semObject = semEvents.get(i);
            for (int j = 0; j < semObject.getNafMentions().size(); j++) {
                NafMention nafMention = semObject.getNafMentions().get(j);
                if (!nafMention.getFactuality().getPrediction().isEmpty()) {
                    Util.addMentionToFactRelations(nafMention, factRelations, nafMention.getFactuality().getPrediction(), baseUrl, semObject.getId());
                }
            }
        }*/

        /*
          We create mappings between the SemTime objects and the events. SemTime objects come either from the TimeEx layer
          or from the SRL layer. If they come from the Timex layer we have no information on how they relate to the event.
          We therefore check if they occur in the same sentence.
          Other options:
          - same + preceding sentence
          - same + preceding + following sentence
          - make a difference for SRL and Timex
         */
        SemTime docSemTime = Util.getDocumentCreationTime(semTimes);

        ///hack in case the functionInDocument attribute has not been used by TimePro
        if (docSemTime==null) {
            for (int j = 0; j < semTimes.size(); j++) {
                SemTime semTime = (SemTime) semTimes.get(j);
                if (semTime.getId().toLowerCase().endsWith("tmx0")) {
                    docSemTime = semTime;
                }
            }
        }
        if (docSemTime==null) {
            docSemTime = Util.getDocumentCreationTimeFromNafHeader(baseUrl,kafSaxParser);
           // System.out.println("header docSemTime.getOwlTime().getDateLabel() = " + docSemTime.getOwlTime().getDateStringURI());
        }
        if (DOCTIME) {
            if (docSemTime != null) {
                semTimes.add(docSemTime);
            } else {
/*
            docSemTime = Util.getBirthOfJC(baseUrl);
          //  System.out.println("BJC docSemTime.getOwlTime().getDateLabel() = " + docSemTime.getOwlTime().getDateLabel());
            if (docSemTime!=null) {
                semTimes.add(docSemTime);
            }
*/
            }
        }

        int timexRelationCount = 0;
        for (int i = 0; i < semEvents.size(); i++) {
            SemObject semEvent = semEvents.get(i);
            boolean timeAnchor = false;
            for (int j = 0; j < kafSaxParser.kafPredicateAnchorArrayList.size(); j++) {
                KafPredicateAnchor kafPredicateAnchor = kafSaxParser.kafPredicateAnchorArrayList.get(j);
                for (int k = 0; k < kafPredicateAnchor.getSpans().size(); k++) {
                    String predicateSpan =  kafPredicateAnchor.getSpans().get(k);
                    if (semEvent.getNafIds().contains(predicateSpan)) {
                        SemTime semBeginTime = (SemTime) Util.getSemTime(semTimes, kafPredicateAnchor.getBeginPoint());
                        SemTime semEndTime = (SemTime) Util.getSemTime(semTimes, kafPredicateAnchor.getEndPoint());
                        SemTime semAnchorTime = (SemTime) Util.getSemTime(semTimes, kafPredicateAnchor.getAnchorTime());
                        if (semBeginTime!=null) {
                            timexRelationCount++;
                            NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, semBeginTime.getTermIds());
                            SemRelation semRelation = semBeginTime.createSemTimeRelation(baseUrl,
                                    timexRelationCount,Sem.hasEarliestBeginTime.getLocalName(), semEvent.getId(), mention);
                            semRelations.add(semRelation);
                           // System.out.println("semRelation.getPredicates().toString() = " + semRelation.getPredicates().toString());
                            timeAnchor = true;
                        }
                        if (semEndTime!=null) {
                            timexRelationCount++;
                            NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, semEndTime.getTermIds());
                            SemRelation semRelation = semEndTime.createSemTimeRelation(baseUrl,
                                    timexRelationCount,Sem.hasEarliestEndTime.getLocalName(), semEvent.getId(), mention);
                            semRelations.add(semRelation);
                           // System.out.println("semRelation.getPredicates().toString() = " + semRelation.getPredicates().toString());
                            timeAnchor = true;
                        }
                        if (semAnchorTime!=null) {
                            timexRelationCount++;
                            NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, semAnchorTime.getTermIds());
                            SemRelation semRelation = semAnchorTime.createSemTimeRelation(baseUrl,
                                    timexRelationCount,Sem.hasAtTime.getLocalName(), semEvent.getId(), mention);
                            semRelations.add(semRelation);
                            timeAnchor = true;
                        }
                    }
                }
            }

            if (!timeAnchor && DOCTIME) {
                /// we assume that events without an explicit time anchor and having nwr:AttributionTime value FUTURE are timeless events
                KafFactuality kafFactuality = Util.futureEvent(semEvent);
                if (kafFactuality!=null) {
                    timexRelationCount++;
                    NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, kafFactuality.getSpans());
                    SemRelation semRelation = docSemTime.createSemTimeRelation(baseUrl,
                            timexRelationCount, Sem.hasFutureTime.getLocalName(), semEvent.getId(), mention);
                    semRelations.add(semRelation);
                    timeAnchor = true;
                }
            }
            if (CONTEXTTIME) {
                if (!timeAnchor) {
                    for (int l = 0; l < semTimes.size(); l++) {
                        SemTime semTime = (SemTime) semTimes.get(l);
                        if (semTime != null && semTime.getNafMentions() != null) {
                            ArrayList<String> termIds = Util.sameSentenceRange(semEvent, semTime);
                            if (termIds.size() > 0) {
                                /// create sem relations
                                timexRelationCount++;
                                NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, termIds);
                                SemRelation semRelation = semTime.createSemTimeRelation(baseUrl,
                                        timexRelationCount, Sem.hasAtTime.getLocalName(), semEvent.getId(), mention);
                                semRelations.add(semRelation);
                                timeAnchor = true;
                                //  break;*//*
                            }
                        }
                    }
                }

                if (!timeAnchor) {
                    for (int l = 0; l < semTimes.size(); l++) {
                        SemTime semTime = (SemTime) semTimes.get(l);
                        if (semTime != null && semTime.getNafMentions() != null) {
                            ArrayList<String> termIds = Util.range1SentenceRange(semEvent, semTime);
                            if (termIds.size() > 0) {
                                /// create sem relations
                                timexRelationCount++;
                                NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, termIds);
                                SemRelation semRelation = semTime.createSemTimeRelation(baseUrl,
                                        timexRelationCount, Sem.hasAtTime.getLocalName(), semEvent.getId(), mention);
                                semRelations.add(semRelation);
                                timeAnchor = true;
                                //  break;*//*
                            }
                        }
                    }
                }
                if (!timeAnchor) {
                    for (int l = 0; l < semTimes.size(); l++) {
                        SemTime semTime = (SemTime) semTimes.get(l);
                        if (semTime != null && semTime.getNafMentions() != null) {
                            ArrayList<String> termIds = Util.rangemin2plus1SentenceRange(semEvent, semTime);
                            if (termIds.size() > 0) {
                                /// create sem relations
                                timexRelationCount++;
                                NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, termIds);
                                SemRelation semRelation = semTime.createSemTimeRelation(baseUrl,
                                        timexRelationCount, Sem.hasAtTime.getLocalName(), semEvent.getId(), mention);
                                semRelations.add(semRelation);
                                timeAnchor = true;
                                // break;
                            }
                        }
                    }
                }
            }
            /*
               If there is no anchoring of an event to an SemTime object, we connect it here to the document time.
               Previous version derived the document creation time from the NAF header. We now leave this up to the TimePro
               module which creates the tmx0 for the default anchoring of the document
               
             */
            if (!timeAnchor && DOCTIME) {
                /// timeless event
                /// in all cases that there is no time relations we link it to the docTime
               // System.out.println("docSemTime.toString() = " + docSemTime.toString());
               // System.out.println("docSemTime.getDateLabel() = " + docSemTime.getOwlTime().getDateLabel());
               // System.out.println("semEvent = " + semEvent.getId());
               // System.out.println("docSemTime.getId() = " + docSemTime.getId());
                if (docSemTime!=null) {
                    timexRelationCount++;
                   // System.out.println("docSemTime = " + docSemTime.getOwlTime().toString());
                   // System.out.println("semEvent = " + semEvent.getTopPhraseAsLabel());
                    SemRelation semRelation = docSemTime.createSemTimeRelation(baseUrl,
                            timexRelationCount, Sem.hasAtTime.getLocalName(), semEvent.getId());
                    semRelations.add(semRelation);
                }
            }
            else {
              //  System.out.println("anchored semEvent = " + semEvent.getTopPhraseAsLabel());
            }
        }


        /// print test of time relations
        /*for (int i = 0; i < semRelations.size(); i++) {
            SemRelation semRelation = semRelations.get(i);
            System.out.println(semRelation.getSubject()+":"+semRelation.getPredicates().toString()+":"+semRelation.getObject());
        }*/


        // NEXT WE RELATE ACTORS TO EVENTS
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

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
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
           // System.out.println("semEventId = " + semEventId);
            if (semEventId.isEmpty()) {
                //// this is an event without SRL representation, which is not allowed
                // SHOULD NEVER OCCUR
            } else {
               // System.out.println("kafEvent.getParticipants().size() = " + kafEvent.getParticipants().size());
                for (int k = 0; k < kafEvent.getParticipants().size(); k++) {
                    KafParticipant kafParticipant = kafEvent.getParticipants().get(k);
                    // CERTAIN ROLES ARE NOT PROCESSED AND CAN BE SKIPPED
                    //System.out.println(kafParticipant.getSpanIds().toString()+": kafParticipant.getRole() = " + kafParticipant.getRole());
/*                    if (!RoleLabels.validRole(kafParticipant.getRole()) && !Util.hasEsoReference(kafParticipant.getExternalReferences())) {
                        // System.out.println("invalid kafParticipant.getRole() = " + kafParticipant.getRole());
                        continue;
                    }*/
                    if (!RoleLabels.validRole(kafParticipant.getRole())
                            ) {
                        // System.out.println("invalid kafParticipant.getRole() = " + kafParticipant.getRole());
                        continue;
                    }
                    else {
                      //  System.out.println("valid kafParticipant.getRole() = " + kafParticipant.getRole());

                    }

                        //// we take all objects above threshold
                    ArrayList<SemObject> semObjects = Util.getAllMatchingObject(kafSaxParser, kafParticipant, semActors);
                    //  System.out.println("semObjects.size() = " + semObjects.size());
                    for (int l = 0; l < semObjects.size(); l++) {
                        SemObject semObject = semObjects.get(l);
                     //   System.out.println("semObject.getUniquePhrases().toString() = " + semObject.getUniquePhrases().toString());
                        if (semObject != null) {
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
                            semRelation.addPredicate(Sem.hasActor.getLocalName());
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
                        else {
                            System.out.println();
                        }
                    }
                }
            }
        }
    }


}
