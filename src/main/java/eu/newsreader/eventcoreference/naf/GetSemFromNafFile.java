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

    static final public String ID_SEPARATOR = "_";

    static public void processNafFile (String pathToNafFile,
                                       ArrayList<SemObject> semEvents,
                                       ArrayList<SemObject> semActors,
                                       ArrayList<SemObject> semPlaces,
                                       ArrayList<SemObject> semTimes,
                                       ArrayList<SemRelation> semRelations) {
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(pathToNafFile);
/*
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(System.currentTimeMillis());
        String strdate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (date != null) {
            strdate = sdf.format(date.getTime());
        }
*/
        String baseUrl = ResourcesUri.nwr+kafSaxParser.getKafMetaData().getUrl().replace("/", ID_SEPARATOR)+ID_SEPARATOR;
       // String baseUrl = ResourcesUri.nwr;

        //// we first store the publication date as a time
        KafSense dateSense = new KafSense();
        dateSense.setRefType("publication date");
        dateSense.setSensecode(kafSaxParser.getKafMetaData().getCreationtime());
        SemTime docSemTime = new SemTime();
        //docSemTime.addConcept(dateSense);
        docSemTime.setId(ResourcesUri.tl+dateSense.getSensecode());
        docSemTime.addPhraseCounts(dateSense.getSensecode());
        CorefTarget dateCorefTarget = new CorefTarget();
        dateCorefTarget.setId(baseUrl+"nafHeader"+"/"+"fileDesc"+"#"+"creationtime");
        ArrayList<CorefTarget> targets = new ArrayList<CorefTarget>();
        targets.add(dateCorefTarget);
        docSemTime.addMentions(targets);
        semTimes.add(docSemTime);

        //// we get time references from the SRL layer
        HashMap<String, ArrayList<CorefTarget>> timeReferences = getTimeMentionsHashMapFromSrl (kafSaxParser);
        Set keySet = timeReferences.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            ArrayList<CorefTarget> corefTargetArrayList = timeReferences.get(key);
            Util.getMentionUriArrayList(kafSaxParser, corefTargetArrayList);
            SemTime semTimeRole = new SemTime();
            semTimeRole.setId(baseUrl + key);
            semTimeRole.addMentions(baseUrl, corefTargetArrayList);
            semTimeRole.addPhraseCountsForMentions(kafSaxParser);
            semTimes.add(semTimeRole);
        }



        for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
            KafCoreferenceSet coreferenceSet = kafSaxParser.kafCorefenceArrayList.get(i);
            Util.getMentionUriArrayArrayList(kafSaxParser, coreferenceSet.getSetsOfSpans());
            KafSense sense = new KafSense();
            sense.setRefType("corefType");
            sense.setSensecode(coreferenceSet.getType());
            if (coreferenceSet.getType().equalsIgnoreCase("event")) {
                SemEvent semEvent = new SemEvent();
                semEvent.setId(baseUrl+coreferenceSet.getCoid());
                semEvent.setMentions(baseUrl, coreferenceSet.getSetsOfSpans());
                semEvent.addPhraseCountsForMentions(kafSaxParser);
                semEvent.setConcept(getExternalReferencesSrlEvents(kafSaxParser, coreferenceSet));
                semEvent.setIdByReference();
                semEvents.add(semEvent);
            }
            else if (coreferenceSet.getType().equalsIgnoreCase("location")) {
                SemPlace semPlace = new SemPlace();
                semPlace.setId(baseUrl + coreferenceSet.getCoid());
                semPlace.setMentions(baseUrl, coreferenceSet.getSetsOfSpans());
                semPlace.addPhraseCountsForMentions(kafSaxParser);
                semPlace.addConcept(sense);
                semPlace.addConcepts(getExternalReferencesSrlParticipants(kafSaxParser, coreferenceSet));
                semPlace.addConcepts(getExternalReferencesEntities(kafSaxParser, coreferenceSet));
                semPlace.setIdByReference();
                semPlaces.add(semPlace);
            }
            else  {
                /// assume it is an actor
                SemActor semActor = new SemActor();
                semActor.setId(baseUrl  + coreferenceSet.getCoid());
                semActor.setMentions(baseUrl, coreferenceSet.getSetsOfSpans());
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
                                CorefTarget corefTarget = new CorefTarget();
                                corefTarget.setId(kafParticipant.getId());
                                Util.getMentionUriCorefTarget(kafSaxParser, corefTarget);
                                semRelation.addCorefTarget(baseUrl, corefTarget);
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
                                    CorefTarget corefTarget = new CorefTarget();
                                    corefTarget.setId(kafParticipant.getId());
                                    Util.getMentionUriCorefTarget(kafSaxParser, corefTarget);
                                    semRelation.addCorefTarget(baseUrl, corefTarget);
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
                                    CorefTarget corefTarget = new CorefTarget();
                                    corefTarget.setId(kafParticipant.getId());
                                    Util.getMentionUriCorefTarget(kafSaxParser, corefTarget);
                                    semRelation.addCorefTarget(baseUrl, corefTarget);
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
                semRelation.setCorefTargetsWithMentions(semEvent.getMentions());
                semRelation.setPredicate("hasSemTime");
                semRelation.setSubject(semEvent.getId());
                semRelation.setObject(docSemTime.getId());
                semRelations.add(semRelation);
            }

        }
    }


    static boolean matchSpans(ArrayList<String> spans, SemObject semObject) {
        for (int i = 0; i < semObject.getMentions().size(); i++) {
            ArrayList<ArrayList<CorefTarget>> mentions = semObject.getMentions();
            for (int j = 0; j < mentions.size(); j++) {
                ArrayList<CorefTarget> corefTargetArrayList = mentions.get(j);
                for (int k = 0; k < corefTargetArrayList.size(); k++) {
                    CorefTarget corefTarget = corefTargetArrayList.get(k);
                   // System.out.println("spans.toString() = " + spans.toString());
                   // System.out.println("corefTarget = " + corefTarget.getId());
                    /// ID-HACK
                    String id = corefTarget.getId();
                    int idx = id.lastIndexOf(ID_SEPARATOR);
                    if (idx>-1) {
                        id = id.substring(idx+1);
                    }

                    // System.out.println("id = " + id);
                    //id = t582#char=2856,2863
                    ///// ofset HACK
                    idx = id.indexOf("#");
                    if (idx>-1) {
                        id = id.substring(0, idx);
                    }
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
                        /// ID-HACK
                        String id = corefTarget.getId();
                        int idx = corefTarget.getId().lastIndexOf(ID_SEPARATOR);
                        if (idx>-1) {
                            id = id.substring(idx+1);
                        }

                        // System.out.println("id = " + id);
                        //id = t582#char=2856,2863
                        ///// ofset HACK
                        idx = id.indexOf("#");
                        if (idx>-1) {
                            id = id.substring(0, idx);
                        }


                        if (id.equals(termId)) {
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
                                /// ID-HACK
                                String id = corefTarget.getId();
                                int idx = corefTarget.getId().lastIndexOf(ID_SEPARATOR);
                                if (idx>-1) {
                                    id = id.substring(idx+1);
                                }

                                // System.out.println("id = " + id);
                                //id = t582#char=2856,2863
                                ///// ofset HACK
                                idx = id.indexOf("#");
                                if (idx>-1) {
                                    id = id.substring(0, idx);
                                }


                                if (id.equals(corefTargetParticipant.getId())) {
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

    static HashMap<String, ArrayList<CorefTarget>> getTimeMentionsHashMapFromSrl (KafSaxParser kafSaxParser) {
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

                            /// ID-HACK
                            String id = corefTarget.getId();
                            int idx = corefTarget.getId().lastIndexOf(ID_SEPARATOR);
                            if (idx>-1) {
                                id = id.substring(idx+1);
                            }

                            // System.out.println("id = " + id);
                            //id = t582#char=2856,2863
                            ///// ofset HACK
                            idx = id.indexOf("#");
                            if (idx>-1) {
                                id = id.substring(0, idx);
                            }


                            if (id.equals(entityCorefTarget.getId())) {
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
                                      ArrayList<SemRelation> semRelations) {



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
        defaultModel.setNsPrefix("dbp", ResourcesUri.dbp);
        defaultModel.setNsPrefix("owl", ResourcesUri.owl);
        defaultModel.setNsPrefix("rdf", ResourcesUri.rdf);
        defaultModel.setNsPrefix("rdfs", ResourcesUri.rdfs);
        defaultModel.setNsPrefix("tl", ResourcesUri.tl);

        Model provenanceModel = ds.getNamedModel("http://www.newsreader-project.eu/provenance");
        provenanceModel.setNsPrefix("nwr", ResourcesUri.nwr);
        provenanceModel.setNsPrefix("gaf", ResourcesUri.gaf);

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
        instanceModel.setNsPrefix("tl", ResourcesUri.tl);
        instanceModel.setNsPrefix("gaf", ResourcesUri.gaf);
        instanceModel.setNsPrefix("owl", ResourcesUri.owl);
        instanceModel.setNsPrefix("dbp", ResourcesUri.dbp);
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


        for (int i = 0; i < semTimes.size(); i++) {
            SemObject semTime = semTimes.get(i);
            semTime.addToJenaModel(instanceModel, Sem.Time);
          //  semTime.addTimeToJenaModel(instanceModel, Sem.Time, ResourcesUri.tl);
        }

        for (int i = 0; i < semRelations.size(); i++) {
            SemRelation semRelation = semRelations.get(i);
            semRelation.addToJenaDataSet(ds, provenanceModel);
        }

        RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);
    }


    static public void main (String [] args) {
        //String pathToNafFile = args[0];
        String pathToNafFile = "/Users/kyoto/Desktop/NWR-DATA/2004-04-26/4C7M-RB90-01K9-42PW.xml_5f4b83812169f9f993f044fd3cb58ab3.naf.coref";
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        processNafFile(pathToNafFile, semEvents, semActors, semPlaces, semTimes, semRelations);
        serializeJena(System.out, semEvents, semActors, semPlaces, semTimes, semRelations);
    }
}
