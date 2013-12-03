package eu.newsreader.eventcoreference.naf;

import com.hp.hpl.jena.rdf.model.*;
import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.objects.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/30/13
 * Time: 7:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class GetSemFromNafFile {


    static public void processNafFile (String pathToNafFile,
                                       ArrayList<SemObject> semEvents,
                                       ArrayList<SemObject> semActors,
                                       ArrayList<SemObject> semTimes,
                                       ArrayList<SemObject> semPlaces,
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

        KafSense dateSense = new KafSense();
        dateSense.setRefType("publication date");
        dateSense.setSensecode(kafSaxParser.getKafMetaData().getDateString());
        SemTime aSemTime = new SemTime();
        aSemTime.addConcept(dateSense);
        aSemTime.setId(dateSense.getSensecode());
        semTimes.add(aSemTime);
        ArrayList<ArrayList<CorefTarget>> timeReferences = getTimeMentionsFromSrl(kafSaxParser);
        for (int i = 0; i < timeReferences.size(); i++) {
            ArrayList<CorefTarget> corefTargetArrayList = timeReferences.get(i);
            SemTime semTimeRole = new SemTime();
            semTimeRole.addMentions(corefTargetArrayList);
            semTimeRole.addPhraseCountsForMentions(kafSaxParser);
            KafSense sense = new KafSense();
            sense.setRefType("roleType");
            sense.setSensecode("TMP");
            semTimeRole.addConcept(sense);
            semTimeRole.setId(semTimeRole.getTopPhraseAsLabel());
            semTimes.add(semTimeRole);
        }
        for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
            KafCoreferenceSet coreferenceSet = kafSaxParser.kafCorefenceArrayList.get(i);
            KafSense sense = new KafSense();
            sense.setRefType("corefType");
            sense.setSensecode(coreferenceSet.getType());
            if (coreferenceSet.getType().equalsIgnoreCase("event")) {
                SemEvent semEvent = new SemEvent();
                semEvent.setId(coreferenceSet.getCoid());
                semEvent.setMentions(coreferenceSet.getSetsOfSpans());
                semEvent.addPhraseCountsForMentions(kafSaxParser);
                semEvent.addConcept(sense);
                semEvent.setConcept(getExternalReferencesSrlEvents(kafSaxParser, coreferenceSet));
                semEvents.add(semEvent);
            }
            else if (coreferenceSet.getType().equalsIgnoreCase("location")) {
                SemPlace semPlace = new SemPlace();
                semPlace.setId(coreferenceSet.getCoid());
                semPlace.setMentions(coreferenceSet.getSetsOfSpans());
                semPlace.addPhraseCountsForMentions(kafSaxParser);
                semPlace.addConcept(sense);
               // semPlace.setLabel("");
                semPlace.addConcepts(getExternalReferencesSrlParticipants(kafSaxParser, coreferenceSet));
                semPlace.addConcepts(getExternalReferencesEntities(kafSaxParser, coreferenceSet));
                semPlaces.add(semPlace);
            }
            else  {
                /// assume it is an actor
                SemActor semActor = new SemActor();
                semActor.setId(coreferenceSet.getCoid());
                semActor.setMentions(coreferenceSet.getSetsOfSpans());
                semActor.addPhraseCountsForMentions(kafSaxParser);
                semActor.addConcept(sense);
                semActor.setConcept(getExternalReferencesSrlParticipants(kafSaxParser, coreferenceSet));
                semActor.setConcept(getExternalReferencesEntities(kafSaxParser, coreferenceSet));
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


        //@TODO get SemRelations
        /*
            - iterate over de SRL layers
            - represent predicates and participants
            - check if they overlap with semObjects
            - if so use the instanceId
            - if not create a new instanceId
         */

        for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
            KafEvent kafEvent =  kafSaxParser.getKafEventArrayList().get(i);
            //// we need to get the corresponding semEvent first
            // check the SemEvents
            for (int j = 0; j < semEvents.size(); j++) {
                SemObject semEvent = semEvents.get(j);
                if (match(kafEvent.getSpanIds(), semEvent)) {
                    for (int k = 0; k < kafEvent.getParticipants().size(); k++) {
                        KafParticipant kafParticipant = kafEvent.getParticipants().get(k);
                        boolean match = false;
                        for (int l = 0; l < semActors.size(); l++) {
                            SemObject semActor = semActors.get(l);
                            if (match(kafParticipant.getSpanIds(), semActor)) {
                                /// create sem relations
                                SemRelation semRelation = new SemRelation();
                                String relationInstanceId = kafSaxParser.getDocId()+"/relation"+semRelations.size()+1;
                                semRelation.setId(relationInstanceId);
                                CorefTarget corefTarget = new CorefTarget();
                                corefTarget.setId(kafEvent.getId());
                                semRelation.addCorefTarget(corefTarget);
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
                                if (match(kafParticipant.getSpanIds(), semPlace)) {
                                    /// create sem relations
                                    SemRelation semRelation = new SemRelation();
                                    String relationInstanceId = kafSaxParser.getDocId()+"/relation"+semRelations.size()+1;
                                    semRelation.setId(relationInstanceId);
                                    CorefTarget corefTarget = new CorefTarget();
                                    corefTarget.setId(kafEvent.getId());
                                    semRelation.addCorefTarget(corefTarget);
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
                                if (match(kafParticipant.getSpanIds(), semTime)) {
                                    /// create sem relations
                                    SemRelation semRelation = new SemRelation();
                                    String relationInstanceId = kafSaxParser.getDocId()+"/relation"+semRelations.size()+1;
                                    semRelation.setId(relationInstanceId);
                                    CorefTarget corefTarget = new CorefTarget();
                                    corefTarget.setId(kafEvent.getId());
                                    semRelation.addCorefTarget(corefTarget);
                                    semRelation.setPredicate("hasSemTime");
                                    semRelation.setSubject(semEvent.getId());
                                    semRelation.setObject(semTime.getId());
                                    semRelations.add(semRelation);
                                    match = true;
                                    break;
                                }
                            }
                        }
                        if (!match) {
                            /// we could not find any instance match......
                        }
                    }
                }
                else {
                    //// event that is not in coreference set structure.....
                }
            }
        }
    }


    static boolean match (ArrayList<String> spans, SemObject semObject) {
        for (int i = 0; i < semObject.getMentions().size(); i++) {
            ArrayList<ArrayList<CorefTarget>> mentions = semObject.getMentions();
            for (int j = 0; j < mentions.size(); j++) {
                ArrayList<CorefTarget> corefTargetArrayList = mentions.get(j);
                for (int k = 0; k < corefTargetArrayList.size(); k++) {
                    CorefTarget corefTarget = corefTargetArrayList.get(k);
                    if (spans.contains(corefTarget.getId())) {
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
                for (int k = 0; k < kafParticipant.getSpanIds().size(); k++) {
                    String termId = kafParticipant.getSpanIds().get(k);
                    for (int l = 0; l < kafCoreferenceSet.getSetsOfSpans().size(); l++) {
                        ArrayList<CorefTarget> corefTargets = kafCoreferenceSet.getSetsOfSpans().get(l);
                        for (int m = 0; m < corefTargets.size(); m++) {
                            CorefTarget corefTarget = corefTargets.get(m);
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
                            if (corefTarget.getId().equals(entityCorefTarget.getId())) {
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
                                      ArrayList<SemObject> semTimes,
                                      ArrayList<SemObject> semPlaces,
                                      ArrayList<SemRelation> semRelations) {



        // create an empty Model
        Model model = ModelFactory.createDefaultModel();
        String nwr = "http://www.newsreader-project.eu/";
        String wn = "http://example.org/wordnet3.0/";
        String fn = "http://example.org/framenet/";
        String vn = "http://example.org/verbnet/";
        String pb = "http://example.org/propbank/";

        model.setNsPrefix("nwr", nwr);
        model.setNsPrefix("wn", wn);
        model.setNsPrefix("fn", fn);
        model.setNsPrefix("vn", vn);
        model.setNsPrefix("pb", pb);
        Bag events = model.createBag("http://www.newsreader-project/semEvents");
        for (int i = 0; i < semEvents.size(); i++) {
            SemObject semEvent = semEvents.get(i);
            Resource resource = semEvent.toJenaRdfResource();
            events.add(resource);
        }

        Bag actors = model.createBag("http://www.newsreader-project/semActors");
        for (int i = 0; i < semActors.size(); i++) {
            SemObject semActor = semActors.get(i);
            Resource resource = semActor.toJenaRdfResource();
            actors.add(resource);
        }

        Bag places = model.createBag("http://www.newsreader-project/semPlaces");
        for (int i = 0; i < semPlaces.size(); i++) {
            SemObject semPlace = semPlaces.get(i);
            Resource resource = semPlace.toJenaRdfResource();
            places.add(resource);
        }

        Bag times = model.createBag("http://www.newsreader-project/semTimes");
        for (int i = 0; i < semTimes.size(); i++) {
            SemObject semTime = semTimes.get(i);
            Resource resource = semTime.toJenaRdfResource();
            times.add(resource);
        }

        Bag relations = model.createBag("http://www.newsreader-project/semRelations");
        for (int i = 0; i < semRelations.size(); i++) {
            SemRelation semRelation = semRelations.get(i);
            Statement statement = semRelation.toJenaRdfStatement();
            relations.add(statement);
        }

      //  model.write(stream);
      //  model.write(stream, "data.trig");
     //   model.write(stream, "N-TRIPLES");
        RDFDataMgr.write(stream, model, RDFFormat.TRIG_PRETTY);

    }


    static public void main (String [] args) {
        //String pathToNafFile = args[0];
        String pathToNafFile = "/Users/kyoto/Desktop/NWR-DATA/50_docs_test/57G4-MK21-F15F-1218.xml_357abd8739615e7f4afc60ade1f2847a.naf.coref";
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        processNafFile(pathToNafFile, semEvents, semActors, semPlaces, semTimes, semRelations);
        serializeJena(System.out, semEvents, semActors, semPlaces, semTimes, semRelations);
    }
}
