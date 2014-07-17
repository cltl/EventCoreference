package eu.newsreader.eventcoreference.naf;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.util.EntityTypes;
import eu.newsreader.eventcoreference.util.RoleLabels;
import eu.newsreader.eventcoreference.util.TimeLanguage;
import eu.newsreader.eventcoreference.util.Util;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.*;
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


    static SemTime docSemTime = new SemTime();
  //  static OwlTime docOwlTime = new OwlTime();

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
        TimeLanguage.setLanguage(kafSaxParser.getLanguage());
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
        TimeLanguage.setLanguage(kafSaxParser.getLanguage());
        String baseUrl = kafSaxParser.getKafMetaData().getUrl()+ID_SEPARATOR;
        if (!baseUrl.toLowerCase().startsWith("http"))     {
            baseUrl = ResourcesUri.nwrdata+project+"/"+kafSaxParser.getKafMetaData().getUrl()+ID_SEPARATOR;
        }
        processNafFileForActorPlaceInstances(baseUrl, kafSaxParser, semActors, semPlaces);
        processNafFileForTimeInstances(baseUrl, kafSaxParser, semTimes);
    }

    static void processNafFileForEventInstances (String baseUrl, KafSaxParser kafSaxParser,
                                                   ArrayList<SemObject> semEvents

    ) {


        /**
         * Event instances
         */

        for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
            KafEvent event = kafSaxParser.kafEventArrayList.get(i);
            ArrayList<NafMention> mentionArrayList = Util.getNafMentionArrayListFromPredicatesAndCoreferences(baseUrl, kafSaxParser, event);
            SemEvent semEvent = new SemEvent();
            semEvent.setNafMentions(mentionArrayList);
            semEvent.addPhraseCountsForMentions(kafSaxParser);
            String eventName = semEvent.getTopPhraseAsLabel();
           // System.out.println("eventName = " + eventName);

            if (Util.hasAlphaNumeric(eventName)) {
                //semEvent.setId(baseUrl+event.getId());
                semEvent.setId(baseUrl+eventName+"Event");
                semEvent.setFactuality(kafSaxParser);
                semEvent.setConcept(event.getExternalReferences());
                semEvent.setIdByDBpediaReference();
                semEvents.add(semEvent);
            }
        }

    }


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
            if (uri.isEmpty()) {
                kafEntity.setTokenStrings(kafSaxParser);

                if (Util.hasAlphaNumeric(kafEntity.getTokenString())) {
                    try {
                        uri = URLEncoder.encode(kafEntity.getTokenString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        // e.printStackTrace();
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
            Util.addObject(semPlaces, semPlace);

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
            Util.addObject(semActors, semActor);
        }
    }


    static void processNafFileForTimeInstances (String baseUrl, KafSaxParser kafSaxParser,
                                       ArrayList<SemObject> semTimes
    ) {

        if (!kafSaxParser.getKafMetaData().getCreationtime().isEmpty()) {
            //// we first store the publication date as a time
            docSemTime = new SemTime();
            docSemTime.setId(baseUrl + "nafHeader" + "_" + "fileDesc" + "_" + "creationtime");
            docSemTime.addPhraseCounts(kafSaxParser.getKafMetaData().getCreationtime());
            NafMention mention = new NafMention(baseUrl + "nafHeader" + "_" + "fileDesc" + "_" + "creationtime");
            docSemTime.addMentionUri(mention);
            docSemTime.getOwlTime().parsePublicationDate(kafSaxParser.getKafMetaData().getCreationtime());
        }

        for (int i = 0; i < kafSaxParser.kafTimexLayer.size(); i++) {
            KafTimex timex = kafSaxParser.kafTimexLayer.get(i);
            if (!timex.getValue().isEmpty()) {
             //  System.out.println("timex.getValue() = " + timex.getValue());
                OwlTime aTime =new OwlTime();
                if (aTime.parseTimeExValue(timex.getValue(), docSemTime.getOwlTime())>-1) {
                    ArrayList<String> tokenSpanIds = timex.getSpans();
                    ArrayList<String> termSpanIds = kafSaxParser.covertTokensSpanToTermSPan(tokenSpanIds);
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


        //// we get time references from the SRL layer
        HashMap<String, ArrayList<ArrayList<CorefTarget>>> timeReferences = Util.getTimeMentionsHashMapFromSrl (kafSaxParser, docSemTime.getOwlTime());
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
        }
    }



    static void processNafFileForRelations (String baseUrl, KafSaxParser kafSaxParser,
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
                        if (!RoleLabels.validRole(kafParticipant.getRole())) {
                            continue;
                        }

                        boolean match = false;
                        for (int l = 0; l < semActors.size(); l++) {
                            SemObject semActor = semActors.get(l);
                            if (Util.matchAllSpansOfAnObjectMentionOrTheRoleHead(kafSaxParser, kafParticipant, semActor)) {
                               // System.out.println("semActor.getPhrase() = " + semActor.getPhrase());
                                /// create sem relations
                                SemRelation semRelation = new SemRelation();
                                String relationInstanceId = baseUrl+kafEvent.getId()+","+kafParticipant.getId();
                                semRelation.setId(relationInstanceId);

                                ArrayList<String> termsIds = kafEvent.getSpanIds();
                                for (int j = 0; j < kafParticipant.getSpanIds().size(); j++) {
                                    String s = kafParticipant.getSpanIds().get(j);
                                    termsIds.add(s);
                                }
                                NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, termsIds);
                                semRelation.addMention(mention);
                                semRelation.setPredicate("hasSemActor");
                                //// check the source and prefix accordingly
                                semRelation.addPredicate(kafParticipant.getRole());
                                for (int j = 0; j < kafParticipant.getExternalReferences().size(); j++) {
                                    KafSense kafSense = kafParticipant.getExternalReferences().get(j);
                                    semRelation.addPredicate(kafSense.getResource()+":"+kafSense.getSensecode());
                                }
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

                                    ArrayList<String> termsIds = kafEvent.getSpanIds();
                                    for (int j = 0; j < kafParticipant.getSpanIds().size(); j++) {
                                        String s = kafParticipant.getSpanIds().get(j);
                                        termsIds.add(s);
                                    }
                                    NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, termsIds);
                                    semRelation.addMention(mention);
                                    semRelation.setPredicate("hasSemPlace");
                                    semRelation.addPredicate(kafParticipant.getRole());
                                    for (int j = 0; j < kafParticipant.getExternalReferences().size(); j++) {
                                        KafSense kafSense = kafParticipant.getExternalReferences().get(j);
                                        semRelation.addPredicate(kafSense.getResource()+":"+kafSense.getSensecode());
                                    }
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

                                    SemRelation semRelation = new SemRelation();
                                    String relationInstanceId = baseUrl+kafEvent.getId()+","+kafParticipant.getId();
                                    semRelation.setId(relationInstanceId);

                                    ArrayList<String> termsIds = kafEvent.getSpanIds();
                                    for (int j = 0; j < kafParticipant.getSpanIds().size(); j++) {
                                        String s = kafParticipant.getSpanIds().get(j);
                                        termsIds.add(s);
                                    }
                                    NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, termsIds);
                                    semRelation.addMention(mention);
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
        ResourcesUri.prefixModel(defaultModel);

        Model provenanceModel = ds.getNamedModel("http://www.newsreader-project.eu/provenance");
        ResourcesUri.prefixModelGaf(provenanceModel);

        Model instanceModel = ds.getNamedModel("http://www.newsreader-project.eu/instances");
        ResourcesUri.prefixModel(instanceModel);

        // System.out.println("EVENTS");
        for (int i = 0; i < semEvents.size(); i++) {
            SemObject semEvent = semEvents.get(i);
            semEvent.addToJenaModel(instanceModel, Sem.Event);
        }

      //  System.out.println("ACTORS");
        for (int i = 0; i < semActors.size(); i++) {
            SemObject semActor = semActors.get(i);
            semActor.addToJenaModel(instanceModel, Sem.Actor);
        }

      //  System.out.println("PLACES");
        for (int i = 0; i < semPlaces.size(); i++) {
            SemObject semPlace = semPlaces.get(i);
            semPlace.addToJenaModel(instanceModel, Sem.Place);
        }
        if (!docSemTime.getPhrase().isEmpty()) {
            docSemTime.addToJenaModelDocTimeInterval(instanceModel);
        }
        else {
          //  System.out.println("empty phrase for docSemTime = " + docSemTime.getId());
        }
       // System.out.println("TIMES");
        for (int i = 0; i < semTimes.size(); i++) {
            SemTime semTime = (SemTime) semTimes.get(i);
            //semTime.addToJenaModel(instanceModel, Sem.Time);
            semTime.addToJenaModelTimeInterval(instanceModel);
        }

        //System.out.println("RELATIONS");
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

       // System.out.println("FACTUALITIES");
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

    static public void serializeJenaCompositeEvents (OutputStream stream,HashMap<String, ArrayList<CompositeEvent>> semEvents,
                                      HashMap <String, SourceMeta> sourceMetaHashMap) {



        // create an empty Model

        Dataset ds = TDBFactory.createDataset();
        Model defaultModel = ds.getDefaultModel();
        ResourcesUri.prefixModel(defaultModel);

        Model provenanceModel = ds.getNamedModel("http://www.newsreader-project.eu/provenance");
        ResourcesUri.prefixModelGaf(provenanceModel);

        Model instanceModel = ds.getNamedModel("http://www.newsreader-project.eu/instances");
        ResourcesUri.prefixModel(instanceModel);
        //ResourcesUri.prefixModelNwr(instanceModel);

        Set keySet = semEvents.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String lemma = (String) keys.next();
            ArrayList<CompositeEvent> compositeEvents = semEvents.get(lemma);
            for (int c = 0; c < compositeEvents.size(); c++) {
                CompositeEvent compositeEvent = compositeEvents.get(c);
                compositeEvent.getEvent().addToJenaModel(instanceModel, Sem.Event);

                //  System.out.println("ACTORS");
                for (int  i = 0; i < compositeEvent.getMySemActors().size(); i++) {
                    SemActor semActor = (SemActor) compositeEvent.getMySemActors().get(i);
                    semActor.addToJenaModel(instanceModel, Sem.Actor);
                }

                //  System.out.println("PLACES");
                for (int i = 0; i < compositeEvent.getMySemPlaces().size(); i++) {
                    SemPlace semPlace = (SemPlace) compositeEvent.getMySemPlaces().get(i);
                    semPlace.addToJenaModel(instanceModel, Sem.Place);
                }

                if (compositeEvent.getMyDocTimes().size()>0) {
                    for (int i = 0; i < compositeEvent.getMyDocTimes().size(); i++) {
                        SemTime semTime = compositeEvent.getMyDocTimes().get(i);
                        semTime.addToJenaModelDocTimeInterval(instanceModel);
                    }
                }
               // System.out.println("TIMES");
               // System.out.println("compositeEvent.getMySemTimes().size() = " + compositeEvent.getMySemTimes().size());
                for (int i = 0; i < compositeEvent.getMySemTimes().size(); i++) {
                    SemTime semTime = (SemTime) compositeEvent.getMySemTimes().get(i);
                    //semTime.addToJenaModel(instanceModel, Sem.Time);
                    semTime.addToJenaModelTimeInterval(instanceModel);
                }

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
        ResourcesUri.prefixModel(defaultModel);

        Model instanceModel = ds.getNamedModel("http://www.newsreader-project.eu/instances");
        ResourcesUri.prefixModel(instanceModel);


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
        //String pathToNafFile = "/Projects/NewsReader/collaboration/bulgarian/razni11-01.event-coref.naf";
        String pathToNafFile = "/Projects/NewsReader/collaboration/bulgarian/fifa.naf";
        String project = "worldcup";
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
        processNafFile(project, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations);
        try {
           // System.out.println("semEvents = " + semEvents.size());
            String pathToTrigFile = pathToNafFile+".trig";
            FileOutputStream fos = new FileOutputStream(pathToTrigFile);
            serializeJena(fos, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations, null);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
