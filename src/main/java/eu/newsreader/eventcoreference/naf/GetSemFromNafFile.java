package eu.newsreader.eventcoreference.naf;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.util.Util;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

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
        String baseUrl = kafSaxParser.getKafMetaData().getUrl()+ID_SEPARATOR;
        if (!baseUrl.toLowerCase().startsWith("http"))     {
            baseUrl = ResourcesUri.nwrdata+project+"/"+kafSaxParser.getKafMetaData().getUrl()+ID_SEPARATOR;
        }

        //// we first store the publication date as a time
        KafSense dateSense = new KafSense();
        dateSense.setRefType("publication date");
        dateSense.setSensecode(kafSaxParser.getKafMetaData().getCreationtime());
        docSemTime = new SemTime();
        //docSemTime.addConcept(dateSense);


        //docSemTime.setId(ResourcesUri.nwrtime+dateSense.getSensecode());
        docSemTime.setId(baseUrl+"nafHeader"+"_"+"fileDesc"+"_"+"creationtime");
        docSemTime.addPhraseCounts(dateSense.getSensecode());
        NafMention mention = new NafMention(baseUrl+"nafHeader"+"_"+"fileDesc"+"_"+"creationtime");
        docSemTime.addMentionUri(mention);
        //semTimes.add(docSemTime);

        //// we get time references from the SRL layer
        // HACK FUNCTION BECAUSE THERE IS YET NO COREFERENCE SET FOR TIME, WHEN THIS IS IN NAF WE CAN DEPRECATE THIS FUNCTION
        HashMap<String, ArrayList<ArrayList<CorefTarget>>> timeReferences = getTimeMentionsHashMapFromSrl (kafSaxParser);
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

/*
        HashMap<String, ArrayList<ArrayList<CorefTarget>>> locationReferences = getLocationMentionsHashMapFromSrl (kafSaxParser);
        keySet = timeReferences.keySet();
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
*/

        for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
            KafCoreferenceSet coreferenceSet = kafSaxParser.kafCorefenceArrayList.get(i);
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
                semEvent.setConcept(getExternalReferencesSrlEvents(kafSaxParser, coreferenceSet));
                semEvent.setIdByReference();
                semEvents.add(semEvent);
            }
            else if (coreferenceSet.getType().equalsIgnoreCase("location")) {
                //// problem... the coref sets do not have a type for entities when created by EHU
                SemPlace semPlace = new SemPlace();
                semPlace.setId(baseUrl + coreferenceSet.getCoid());
                semPlace.setNafMentions(mentionArrayList);
                semPlace.addPhraseCountsForMentions(kafSaxParser);
                semPlace.addConcept(sense);
                semPlace.addConcepts(getExternalReferencesSrlParticipants(kafSaxParser, coreferenceSet));
                semPlace.addConcepts(getExternalReferencesEntities(kafSaxParser, coreferenceSet));
                semPlace.setIdByReference();
                semPlaces.add(semPlace);
            }
            else  {

                SemActor semActor = new SemActor();
                semActor.setId(baseUrl  + coreferenceSet.getCoid());
                semActor.setNafMentions(mentionArrayList);
                semActor.addPhraseCountsForMentions(kafSaxParser);
                semActor.addConcept(sense);
                semActor.addConcepts(getExternalReferencesSrlParticipants(kafSaxParser, coreferenceSet));
                semActor.addConcepts(getExternalReferencesEntities(kafSaxParser, coreferenceSet));
                semActor.setIdByReference();
                semActors.add(semActor);
            }
        }

        //// THERE SHOULD NOT BE ANY
        //// check for any events and roles not covered by the coreference sets
/*
        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);

        }
*/





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
            for (int j = 0; j < semEvents.size(); j++) {
                SemObject semEvent = semEvents.get(j);
                if (matchSpans(kafEvent.getSpanIds(), semEvent)) {
                    for (int k = 0; k < kafEvent.getParticipants().size(); k++) {
                        KafParticipant kafParticipant = kafEvent.getParticipants().get(k);
                        boolean match = false;
                        for (int l = 0; l < semActors.size(); l++) {
                            SemObject semActor = semActors.get(l);
                            if (matchSpans(kafParticipant.getSpanIds(), semActor)) {
                                /// create sem relations
                                SemRelation semRelation = new SemRelation();
                                String relationInstanceId = baseUrl+kafEvent.getId()+","+kafParticipant.getId();
                                semRelation.setId(relationInstanceId);
                                NafMention relNafMention = new NafMention(baseUrl+kafParticipant.getId());
                                semRelation.addMention(relNafMention);
                                semRelation.setPredicate("hasSemActor");
                                semRelation.setSubject(semEvent.getId());
                                semRelation.setObject(semActor.getId());
                                semRelations.add(semRelation);
                                match = true;
                                break;
                            }
                        }
                        if (!match) {
                            for (int l = 0; l < semPlaces.size(); l++) {
                                SemObject semPlace = semPlaces.get(l);
                                if (matchSpans(kafParticipant.getSpanIds(), semPlace)) {
                                    /// create sem relations
                                    SemRelation semRelation = new SemRelation();
                                    String relationInstanceId = baseUrl+kafEvent.getId()+","+kafParticipant.getId();
                                    semRelation.setId(relationInstanceId);
                                    NafMention relNafMention = new NafMention(baseUrl+kafParticipant.getId());
                                    semRelation.addMention(relNafMention);
                                    semRelation.setPredicate("hasSemPlace");
                                    semRelation.setSubject(semEvent.getId());
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
                                if (matchSpans(kafParticipant.getSpanIds(), semTime)) {
                                    /// create sem relations
                                    SemRelation semRelation = new SemRelation();
                                    String relationInstanceId = baseUrl+kafEvent.getId()+","+kafParticipant.getId();
                                    semRelation.setId(relationInstanceId);
                                    NafMention relNafMention = new NafMention(baseUrl+kafParticipant.getId());
                                    semRelation.addMention(relNafMention);
                                    semRelation.setPredicate("hasSemTime");
                                    semRelation.setSubject(semEvent.getId());
                                    semRelation.setObject(semTime.getId());
                                    semRelations.add(semRelation);
                                    timedSemEventIds.add(semEvent.getId());
                                    match = true;
                                    break;
                                }
                            }
                        }
                        if (!match) {
                            /// we could not find any instance matchSpans......
                        }
                    }
                }
                else {
                    //// event that is not in coreference set structure.....
                }
            }
        }

        /// in all cases there is no time relations we link it to the docTime
        int docTimeRelationCount = 0;
        for (int i = 0; i < semEvents.size(); i++) {
            SemObject semEvent = semEvents.get(i);
            if (!timedSemEventIds.contains(semEvent.getId())) {
              /// timeless event
                docTimeRelationCount++;
                SemRelation semRelation = new SemRelation();
                String relationInstanceId = baseUrl+"docTime_"+docTimeRelationCount;
                semRelation.setId(relationInstanceId);
                //// Since the doctime has no reference in the text, we use the mentions of the events to point to
                semRelation.setNafMentions(semEvent.getNafMentions());
                semRelation.setPredicate("hasSemTime");
                semRelation.setSubject(semEvent.getId());
                semRelation.setObject(docSemTime.getId());
                semRelations.add(semRelation);
            }

        }
/*
        /// in all cases there is no time relations we link it to the docTime
        int docTimeRelationCount = 0;
        for (int i = 0; i < semEvents.size(); i++) {
            SemObject semEvent = semEvents.get(i);
            if (!timedSemEventIds.contains(semEvent.getId())) {
              /// timeless event
                docTimeRelationCount++;
                SemRelation semRelation = new SemRelation();
                String relationInstanceId = baseUrl+"docTime_"+docTimeRelationCount;
                semRelation.setId(relationInstanceId);
                //// Since the doctime has no reference in the text, we use the mentions of the events to point to
                semRelation.setNafMentions(semEvent.getNafMentions());
                semRelation.setPredicate("hasSemTime");
                semRelation.setSubject(semEvent.getId());
                semRelation.setObject(docSemTime.getId());
                semRelations.add(semRelation);
            }

        }
*/
    }


    static boolean matchSpans(ArrayList<String> spans, SemObject semObject) {
        for (int i = 0; i < semObject.getNafMentions().size(); i++) {
            ArrayList<NafMention> mentions = semObject.getNafMentions();
            for (int j = 0; j < mentions.size(); j++) {
                NafMention nafMention = mentions.get(j);
                for (int k = 0; k < nafMention.getTermsIds().size(); k++) {
                    String id = nafMention.getTermsIds().get(k);
                    if (spans.contains(id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static ArrayList<KafSense> getExternalReferencesSrlEvents (KafSaxParser kafSaxParser, KafCoreferenceSet kafCoreferenceSet) {
        ArrayList<KafSense> references = new ArrayList<KafSense>();

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
            boolean match = false;
            for (int j = 0; j < kafEvent.getSpanIds().size(); j++) {
                String termId = kafEvent.getSpanIds().get(j);
                for (int k = 0; k < kafCoreferenceSet.getSetsOfSpans().size(); k++) {
                    ArrayList<CorefTarget> corefTargets = kafCoreferenceSet.getSetsOfSpans().get(k);
                    for (int l = 0; l < corefTargets.size(); l++) {
                        CorefTarget corefTarget = corefTargets.get(l);

                        if (corefTarget.getId().equals(termId)) {
                            match = true;
                            break;
                        }

                    }
                }
            }
            if (match) {
                references.addAll(kafEvent.getExternalReferences());
            }
        }

        return references;
    }

    static ArrayList<KafSense> getExternalReferencesSrlParticipants (KafSaxParser kafSaxParser, KafCoreferenceSet kafCoreferenceSet) {
        ArrayList<KafSense> references = new ArrayList<KafSense>();

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
            for (int j = 0; j < kafEvent.getParticipants().size(); j++) {
                KafParticipant kafParticipant =  kafEvent.getParticipants().get(j);
                boolean match = false;
                for (int k = 0; k < kafParticipant.getSpans().size(); k++) {
                    CorefTarget corefTargetParticipant = kafParticipant.getSpans().get(k);
                    for (int l = 0; l < kafCoreferenceSet.getSetsOfSpans().size(); l++) {
                        ArrayList<CorefTarget> corefTargets = kafCoreferenceSet.getSetsOfSpans().get(l);
                        for (int m = 0; m < corefTargets.size(); m++) {
                            CorefTarget corefTarget = corefTargets.get(m);
                            if (!corefTargetParticipant.getHead().isEmpty())  {

                                if (corefTarget.getId().equals(corefTargetParticipant.getId())) {
                                    match = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (match) {
                    references.addAll(kafParticipant.getExternalReferences());
                }

            }
        }

        return references;
    }

    static ArrayList<ArrayList<CorefTarget>> getTimeMentionsFromSrl (KafSaxParser kafSaxParser) {
        ArrayList<ArrayList<CorefTarget>> mentions = new ArrayList<ArrayList<CorefTarget>>();

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
            for (int j = 0; j < kafEvent.getParticipants().size(); j++) {
                KafParticipant kafParticipant =  kafEvent.getParticipants().get(j);
                if (kafParticipant.getRole().endsWith("-TMP")) {
                    mentions.add(kafParticipant.getSpans());
                }
            }
        }
        return mentions;
    }


    /**
     * HACK FUNCTION BECASUE THERE IS YET NO COREFERENCE SET FOR TIME, WHEN THIS IS IN NAF WE CAN DEPRECATE THIS FUNCTION
     * @param kafSaxParser
     * @return
     */
    static HashMap<String, ArrayList<ArrayList<CorefTarget>>> getTimeMentionsHashMapFromSrl (KafSaxParser kafSaxParser) {
        HashMap<String, ArrayList<ArrayList<CorefTarget>>> mentions = new HashMap<String, ArrayList<ArrayList<CorefTarget>>>();

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
            for (int j = 0; j < kafEvent.getParticipants().size(); j++) {
                KafParticipant kafParticipant =  kafEvent.getParticipants().get(j);
                if (kafParticipant.getRole().endsWith("-TMP")) {
                    String srl = kafParticipant.getId();
                    if (mentions.containsKey(srl)) {
                        ArrayList<ArrayList<CorefTarget>> srlTargets = mentions.get(srl);
                        srlTargets.add(kafParticipant.getSpans());
                        mentions.put(srl, srlTargets);
                    }
                    else {
                        ArrayList<ArrayList<CorefTarget>> srlTargets = new ArrayList<ArrayList<CorefTarget>>();
                        srlTargets.add(kafParticipant.getSpans());
                        mentions.put(srl,srlTargets);
                    }
                }
            }
        }
        return mentions;
    }

  /**

     */
    static HashMap<String, ArrayList<ArrayList<CorefTarget>>> getLocationMentionsHashMapFromSrl (KafSaxParser kafSaxParser) {
        HashMap<String, ArrayList<ArrayList<CorefTarget>>> mentions = new HashMap<String, ArrayList<ArrayList<CorefTarget>>>();

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
            for (int j = 0; j < kafEvent.getParticipants().size(); j++) {
                KafParticipant kafParticipant =  kafEvent.getParticipants().get(j);
                if (kafParticipant.getRole().endsWith("-LOC")) {
                    String srl = kafParticipant.getId();
                    if (mentions.containsKey(srl)) {
                        ArrayList<ArrayList<CorefTarget>> srlTargets = mentions.get(srl);
                        srlTargets.add(kafParticipant.getSpans());
                        mentions.put(srl, srlTargets);
                    }
                    else {
                        ArrayList<ArrayList<CorefTarget>> srlTargets = new ArrayList<ArrayList<CorefTarget>>();
                        srlTargets.add(kafParticipant.getSpans());
                        mentions.put(srl,srlTargets);
                    }
                }
            }
        }
        return mentions;
    }

    static HashMap<String, ArrayList<CorefTarget>> getTimeMentionsHashMapFromSrl_old (KafSaxParser kafSaxParser) {
        HashMap<String, ArrayList<CorefTarget>> mentions = new HashMap<String, ArrayList<CorefTarget>>();

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
            for (int j = 0; j < kafEvent.getParticipants().size(); j++) {
                KafParticipant kafParticipant =  kafEvent.getParticipants().get(j);
                if (kafParticipant.getRole().endsWith("-TMP")) {
                    String srl = kafParticipant.getId();
                    if (mentions.containsKey(srl)) {
                        ArrayList<CorefTarget> srlTargets = mentions.get(srl);
                        srlTargets.addAll(kafParticipant.getSpans());
                        mentions.put(srl, srlTargets);
                    }
                    else {
                        mentions.put(srl, kafParticipant.getSpans());
                    }
                }
            }
        }
        return mentions;
    }

    static ArrayList<KafSense> getExternalReferencesEntities (KafSaxParser kafSaxParser, KafCoreferenceSet kafCoreferenceSet) {
        ArrayList<KafSense> references = new ArrayList<KafSense>();
        for (int i = 0; i < kafSaxParser.kafEntityArrayList.size(); i++) {
            KafEntity kafEntity = kafSaxParser.kafEntityArrayList.get(i);
            boolean match = false;
            for (int j = 0; j < kafEntity.getSetsOfSpans().size(); j++) {
                ArrayList<CorefTarget> entityCorefTargets = kafEntity.getSetsOfSpans().get(j);
                for (int k = 0; k < entityCorefTargets.size(); k++) {
                    CorefTarget entityCorefTarget = entityCorefTargets.get(k);
                    for (int l = 0; l < kafCoreferenceSet.getSetsOfSpans().size(); l++) {
                        ArrayList<CorefTarget> corefTargets = kafCoreferenceSet.getSetsOfSpans().get(l);
                        for (int m = 0; m < corefTargets.size(); m++) {
                            CorefTarget corefTarget = corefTargets.get(m);
                            if (entityCorefTarget.getId().equals(corefTarget.getId())) {
                                match = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (match) {
                references.addAll(kafEntity.getExternalReferences());
            }
        }
        return references;
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

        docSemTime.addToJenaModelDocTimeInterval(instanceModel);

        for (int i = 0; i < semTimes.size(); i++) {
            SemObject semTime = semTimes.get(i);
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
            semRelation.addToJenaDataSet(ds, provenanceModel);
            if (sourceMetaHashMap!=null) {
                semRelation.addToJenaDataSet(ds, provenanceModel, sourceMetaHashMap);

            }
            else {
                semRelation.addToJenaDataSet(ds, provenanceModel);
            }
        }

        RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);
    }


    static public void main (String [] args) {
        //String pathToNafFile = args[0];
        String pathToNafFile = "/Users/piek/Desktop/NWR-DATA/trig/test2/2003-01-01/57FS-KV01-F0J6-D1H8.xml_082dbf3073807d0c4a49bbbea19242b2.naf.coref";
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
        serializeJena(System.out, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations, null);
    }
}
