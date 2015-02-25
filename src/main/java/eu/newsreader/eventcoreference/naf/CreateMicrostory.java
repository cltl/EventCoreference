package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafEventRelation;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.newsreader.eventcoreference.input.FrameNetReader;
import eu.newsreader.eventcoreference.objects.NafMention;
import eu.newsreader.eventcoreference.objects.SemObject;
import eu.newsreader.eventcoreference.objects.SemRelation;
import eu.newsreader.eventcoreference.output.JenaSerialization;
import eu.newsreader.eventcoreference.util.Util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by piek on 2/4/15.
 */
public class CreateMicrostory {

    static public void main (String [] args) {
        //String pathToNafFile = args[0];
        // String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-ontology/test/scale-test.naf";
        //String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-ontology/reasoning/increase-example/57VV-5311-F111-G0HJ.xml_7684191f264a9e21af56de7ec51cf2d5.naf.coref";
        //String pathToNafFile = "/Users/piek/newsreader-deliverables/papers/maplex/47P9-DCM0-0092-K267.xml";
        //String pathToNafFile = "/Users/piek/Desktop/MapLex/47T0-YSP0-018S-20DV.xml";
        // String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-DATA/cars-2/47T0-B4V0-01D6-Y3WM.xml";
        // String pathToNafFile = "/Code/vu/newsreader/EventCoreference/example/naf_and_trig/5C37-HGT1-JBJ4-2472.xml_fb5a69273e6b8028fa2b9796eb62483b.naf";
        // String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-DATA/cars-2/1/47KD-4MN0-009F-S2JG.xml";
        //String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-DATA/cars-2/1/47R9-0JG0-015B-31P6.xml";
        // String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-DATA/cars-2/1/4PG2-TTJ0-TXVX-P0FV.xml";
        //String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-DATA/cars-2/1/47KD-4MN0-009F-S2JG.xml";
        String pathToNafFile = "/Users/piek/Desktop/NWR/Cross-lingual/test.srl.lexicalunits.pm.fn.ecoref.naf";
        //String pathToNafFile = "/Users/piek/Desktop/NEDRerankedTest/51Y9-WY41-DYVC-J27G_reranked.naf";
        //String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-ontology/test/possession-test.naf";
        //String pathToNafFile = "/Projects/NewsReader/collaboration/bulgarian/example/razni11-01.event-coref.naf";
        //String pathToNafFile = "/Projects/NewsReader/collaboration/bulgarian/fifa.naf";
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
        GetSemFromNafFile.processNafFile(project, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations);
        try {
            // System.out.println("semEvents = " + semEvents.size());
            String pathToTrigFile = pathToNafFile+".trig";
            OutputStream fos = new FileOutputStream(pathToTrigFile);
            JenaSerialization.serializeJena(fos, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations, null);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static ArrayList<SemObject> getMicroEvents (Integer sentenceRange, ArrayList<SemObject> semEvents) {
        ArrayList<SemObject> microEvents = new ArrayList<SemObject>();
        for (int i = 0; i < semEvents.size(); i++) {
            SemObject semEvent =  semEvents.get(i);
            for (int j = 0; j < semEvent.getNafMentions().size(); j++) {
                NafMention nafMention = semEvent.getNafMentions().get(j);
                String sentenceId = nafMention.getSentence();
                if (!sentenceId.isEmpty()) {
                    try {
                        Integer sentenceInteger = Integer.parseInt(sentenceId);
                        if (sentenceInteger <= sentenceRange) {
                            if (!Util.hasObjectUri(microEvents, semEvent.getURI())) {
                                microEvents.add(semEvent);
                            }
                        }
                    } catch (NumberFormatException e) {
                        //  e.printStackTrace();
                    }
                }
            }
        }
        return microEvents;
    }

    static ArrayList<SemObject> getMicroActors (Integer sentenceRange, ArrayList<SemObject> semActors) {
        ArrayList<SemObject> microEvents = new ArrayList<SemObject>();
        for (int i = 0; i < semActors.size(); i++) {
            SemObject semActor =  semActors.get(i);
            for (int j = 0; j < semActor.getNafMentions().size(); j++) {
                NafMention nafMention = semActor.getNafMentions().get(j);
                String sentenceId = nafMention.getSentence();
                if (!sentenceId.isEmpty()) {
                    try {
                        Integer sentenceInteger = Integer.parseInt(sentenceId);
                        if (sentenceInteger <= sentenceRange) {
                            microEvents.add(semActor);
                        }
                    } catch (NumberFormatException e) {
                        //  e.printStackTrace();
                    }
                }
            }
        }
        return microEvents;
    }
     static ArrayList<SemObject> getMicroTimes (Integer sentenceRange, ArrayList<SemObject> semTimes) {
        ArrayList<SemObject> microTimes = new ArrayList<SemObject>();
         for (int i = 0; i < semTimes.size(); i++) {
             SemObject semTime =  semTimes.get(i);
             for (int j = 0; j < semTime.getNafMentions().size(); j++) {
                 NafMention nafMention = semTime.getNafMentions().get(j);
                 String sentenceId = nafMention.getSentence();
                 if (!sentenceId.isEmpty()) {
                     try {
                         Integer sentenceInteger = Integer.parseInt(sentenceId);
                         if (sentenceInteger <= sentenceRange) {
                             microTimes.add(semTime);
                         }
                     } catch (NumberFormatException e) {
                         //   e.printStackTrace();
                     }
                 }
             }
         }
        return microTimes;
    }

     static ArrayList<SemRelation> getMicroRelations (Integer sentenceRange, ArrayList<SemRelation> semRelations) {
        ArrayList<SemRelation> microRelations = new ArrayList<SemRelation>();
         for (int i = 0; i < semRelations.size(); i++) {
             SemRelation semRelation =  semRelations.get(i);
            // System.out.println("semRelation.getId() = " + semRelation.getId());
             for (int j = 0; j < semRelation.getNafMentions().size(); j++) {
                 NafMention nafMention = semRelation.getNafMentions().get(j);
                 String sentenceId = nafMention.getSentence();
                 if (!sentenceId.isEmpty()) {
                     Integer sentenceInteger = null;
                     try {
                         sentenceInteger = Integer.parseInt(sentenceId);
                         if (sentenceInteger <= sentenceRange) {
                             microRelations.add(semRelation);
                         }
                     } catch (NumberFormatException e) {
                         // e.printStackTrace();
                     }
                 }
             }
         }
        /*for (int i = 0; i < microRelations.size(); i++) {
             SemRelation microRelation = microRelations.get(i);
             System.out.println("microRelation.getId() = " + microRelation.getId());
         }*/
        return microRelations;
    }

    /**
     * Obtain events and participants through FN relations
     * @param semEvents
     * @param microSemEvents
     * @param frameNetReader
     */
    static void addEventsThroughFrameNetBridging (ArrayList<SemObject> semEvents,
                                                  ArrayList<SemObject> microSemEvents,
                                                  FrameNetReader frameNetReader
    ) {
        for (int i = 0; i < microSemEvents.size(); i++) {
            SemObject microEvent = microSemEvents.get(i);
            for (int k = 0; k < semEvents.size(); k++) {
                SemObject event = semEvents.get(k);
                if (!event.getURI().equals(microEvent.getURI())) {
                    if (frameNetReader.frameNetConnected(microEvent, event) ||
                            frameNetReader.frameNetConnected(event, microEvent)) {
                        if (!Util.hasObjectUri(microSemEvents, event.getURI())) {
                            microSemEvents.add(event);
                        }
                    }
                }
            }
        }
    }

    static ArrayList<SemObject> getEventsThroughFrameNetBridging (ArrayList<SemObject> semEvents,
                                                  ArrayList<SemObject> microSemEvents,
                                                  FrameNetReader frameNetReader
    ) { ArrayList<SemObject> fnRelatedEvents = new ArrayList<SemObject>();
        for (int i = 0; i < microSemEvents.size(); i++) {
            SemObject microEvent = microSemEvents.get(i);
            for (int k = 0; k < semEvents.size(); k++) {
                SemObject event = semEvents.get(k);
                if (!event.getURI().equals(microEvent.getURI())) {
                    if (frameNetReader.frameNetConnected(microEvent, event) ||
                            frameNetReader.frameNetConnected(event, microEvent)) {
                        if (!Util.hasObjectUri(microSemEvents, event.getURI()) &&
                            !Util.hasObjectUri(fnRelatedEvents, event.getURI())) {
                            fnRelatedEvents.add(event);
                        }
                    }
                }
            }
        }
        return fnRelatedEvents;
    }

    /**
     * Obtain events and participants through NAF event relations
     * @param semEvents
     * @param microSemEvents
     * @param kafSaxParser
     */
    static void addEventsThroughNafEventRelations (ArrayList<SemObject> semEvents,
                                                  ArrayList<SemObject> microSemEvents,
                                                  KafSaxParser kafSaxParser
    ) {
        for (int i = 0; i < microSemEvents.size(); i++) {
            SemObject microEvent = microSemEvents.get(i);
            ArrayList<String> microPredicateIds = Util.getPredicateIdsForNafMentions(microEvent.getNafMentions(), kafSaxParser);
            for (int k = 0; k < semEvents.size(); k++) {
                SemObject event = semEvents.get(k);
                if (!event.getURI().equals(microEvent.getURI())) {
                    ArrayList<String> eventPredicateIds = Util.getPredicateIdsForNafMentions(microEvent.getNafMentions(), kafSaxParser);
                    for (int j = 0; j < kafSaxParser.kafTlinks.size(); j++) {
                        KafEventRelation kafEventRelation = kafSaxParser.kafTlinks.get(j);
                        if (microPredicateIds.contains(kafEventRelation.getFrom()) ||
                                eventPredicateIds.contains(kafEventRelation.getTo())) {
                            if (!Util.hasObjectUri(microSemEvents, event.getURI())) {
                                microSemEvents.add(event);
                            }
                            break;
                        }
                        if (eventPredicateIds.contains(kafEventRelation.getFrom()) ||
                                microPredicateIds.contains(kafEventRelation.getTo())) {
                            if (!Util.hasObjectUri(microSemEvents, event.getURI())) {
                                microSemEvents.add(event);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    static ArrayList<SemObject> getEventsThroughNafEventRelations (ArrayList<SemObject> semEvents,
                                                  ArrayList<SemObject> microSemEvents,
                                                  KafSaxParser kafSaxParser
    ) {
        ArrayList<SemObject> relatedEvents = new ArrayList<SemObject>();
        for (int i = 0; i < microSemEvents.size(); i++) {
            SemObject microEvent = microSemEvents.get(i);
            ArrayList<String> microPredicateIds = Util.getPredicateIdsForNafMentions(microEvent.getNafMentions(), kafSaxParser);
            for (int k = 0; k < semEvents.size(); k++) {
                SemObject event = semEvents.get(k);
                if (!event.getURI().equals(microEvent.getURI())) {
                    ArrayList<String> eventPredicateIds = Util.getPredicateIdsForNafMentions(microEvent.getNafMentions(), kafSaxParser);
                    for (int j = 0; j < kafSaxParser.kafTlinks.size(); j++) {
                        KafEventRelation kafEventRelation = kafSaxParser.kafTlinks.get(j);
                        if (microPredicateIds.contains(kafEventRelation.getFrom()) &&
                                eventPredicateIds.contains(kafEventRelation.getTo())) {
                            if (!Util.hasObjectUri(microSemEvents, event.getURI()) &&
                                    !Util.hasObjectUri(relatedEvents, event.getURI())) {
                                relatedEvents.add(event);
                            }
                            break;
                        }
                        if (eventPredicateIds.contains(kafEventRelation.getFrom()) &&
                                microPredicateIds.contains(kafEventRelation.getTo())) {
                            if (!Util.hasObjectUri(microSemEvents, event.getURI()) &&
                                !Util.hasObjectUri(relatedEvents, event.getURI())) {
                                relatedEvents.add(event);
                            }
                            break;
                        }
                    }
                }
            }
        }
        return relatedEvents;
    }


    /**
     * Obtain events and participants through bridging relations
     * @param semEvents
     * @param microSemEvents
     * @param microSemActors
     * @param semRelations
     */
    static void addEventsThroughCoparticipation(ArrayList<SemObject> semEvents,
                                                ArrayList<SemObject> microSemEvents,
                                                ArrayList<SemObject> microSemActors,
                                                ArrayList<SemRelation> semRelations
    ) {
        for (int i = 0; i < microSemActors.size(); i++) {
            SemObject semObject = microSemActors.get(i);
            for (int j = 0; j < semRelations.size(); j++) {
                SemRelation semRelation = semRelations.get(j);
                if (semRelation.getObject().equals(semObject.getURI())) {
                    for (int k = 0; k < semEvents.size(); k++) {
                        SemObject event = semEvents.get(k);
                        if (event.getURI().equals(semRelation.getSubject())) {
                            if (!Util.hasObjectUri(microSemEvents, event.getURI())) {
                                microSemEvents.add(event);
                            }
                        }
                    }
                }
            }
        }
    }

    static ArrayList<SemObject> getEventsThroughCoparticipation(ArrayList<SemObject> semEvents,
                                                ArrayList<SemObject> microSemEvents,
                                                ArrayList<SemObject> microSemActors,
                                                ArrayList<SemRelation> semRelations
    ) {
        ArrayList<SemObject> coparticipationEvents = new ArrayList<SemObject>();
        for (int i = 0; i < microSemActors.size(); i++) {
            SemObject semObject = microSemActors.get(i);
            for (int j = 0; j < semRelations.size(); j++) {
                SemRelation semRelation = semRelations.get(j);
                if (semRelation.getObject().equals(semObject.getURI())) {
                    for (int k = 0; k < semEvents.size(); k++) {
                        SemObject event = semEvents.get(k);
                        if (event.getURI().equals(semRelation.getSubject())) {
                            if (!Util.hasObjectUri(microSemEvents, event.getURI()) &&
                                !Util.hasObjectUri(coparticipationEvents, event.getURI())) {
                                coparticipationEvents.add(event);
                            }
                        }
                    }
                }
            }
        }
        return coparticipationEvents;
    }


    /**
     * Obtain participants through bridging relations
     * @param microSemEvents
     * @param semActors
     * @param microSemActors
     * @param semRelations
     */
    static void addActorsThroughCoparticipation(ArrayList<SemObject> microSemEvents,
                                                ArrayList<SemObject> semActors,
                                                ArrayList<SemObject> microSemActors,
                                                ArrayList<SemRelation> semRelations
    ) {
        for (int i = 0; i < microSemEvents.size(); i++) {
            SemObject semEvent = microSemEvents.get(i);
            for (int j = 0; j < semRelations.size(); j++) {
                SemRelation semRelation = semRelations.get(j);
                if (semRelation.getSubject().equals(semEvent.getURI())) {
                    for (int k = 0; k < semActors.size(); k++) {
                        SemObject actor = semActors.get(k);
                        if (actor.getURI().equals(semRelation.getObject())) {
                            if (!Util.hasObjectUri(microSemActors, actor.getURI())) {
                                microSemActors.add(actor);
                            }
                                //Util.addObject(microSemActors, actor);
                            }
                        }
                    }
                }
            }
    }

    static ArrayList<SemObject> getActorsThroughCoparticipation(ArrayList<SemObject> microSemEvents,
                                                ArrayList<SemObject> semActors,
                                                ArrayList<SemObject> microSemActors,
                                                ArrayList<SemRelation> semRelations
    ) {
        ArrayList<SemObject> coparticipantActors = new ArrayList<SemObject>();
        for (int i = 0; i < microSemEvents.size(); i++) {
            SemObject semEvent = microSemEvents.get(i);
            for (int j = 0; j < semRelations.size(); j++) {
                SemRelation semRelation = semRelations.get(j);
                if (semRelation.getSubject().equals(semEvent.getURI())) {
                    for (int k = 0; k < semActors.size(); k++) {
                        SemObject actor = semActors.get(k);
                        if (actor.getURI().equals(semRelation.getObject())) {
                            if (!Util.hasObjectUri(microSemActors, actor.getURI()) &&
                                !Util.hasObjectUri(coparticipantActors, actor.getURI())) {
                                coparticipantActors.add(actor);
                            }
                        }
                    }
                }
            }
        }
        return coparticipantActors;
    }


}
