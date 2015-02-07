package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.newsreader.eventcoreference.objects.NafMention;
import eu.newsreader.eventcoreference.objects.SemObject;
import eu.newsreader.eventcoreference.objects.SemRelation;
import eu.newsreader.eventcoreference.output.JenaSerialization;

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
                            microEvents.add(semEvent);
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
        return microRelations;
    }

    static public void getMicrostory (     Integer sentenceRange,
                                           ArrayList<SemObject> semEvents,
                                           ArrayList<SemObject> semActors,
                                           ArrayList<SemObject> semTimes,
                                           ArrayList<SemRelation> semRelations

    ) {
        ArrayList<SemObject> semMicroEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semMicroActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semMicroTimes = new ArrayList<SemObject>();
        ArrayList<SemRelation> semMicroRelations = new ArrayList<SemRelation>();

        for (int i = 0; i < semEvents.size(); i++) {
            SemObject semEvent =  semEvents.get(i);
            for (int j = 0; j < semEvent.getNafMentions().size(); j++) {
                NafMention nafMention = semEvent.getNafMentions().get(j);
                String sentenceId = nafMention.getSentence();
                if (!sentenceId.isEmpty()) {
                    try {
                        Integer sentenceInteger = Integer.parseInt(sentenceId);
                        if (sentenceInteger <= sentenceRange) {
                            semMicroEvents.add(semEvent);
                        }
                    } catch (NumberFormatException e) {
                      //  e.printStackTrace();
                    }
                }
            }
        }
        for (int i = 0; i < semActors.size(); i++) {
            SemObject semActor =  semActors.get(i);
            for (int j = 0; j < semActor.getNafMentions().size(); j++) {
                NafMention nafMention = semActor.getNafMentions().get(j);
                String sentenceId = nafMention.getSentence();
                if (!sentenceId.isEmpty()) {
                    try {
                        Integer sentenceInteger = Integer.parseInt(sentenceId);
                        if (sentenceInteger <= sentenceRange) {
                            semMicroActors.add(semActor);
                        }
                    } catch (NumberFormatException e) {
                      //  e.printStackTrace();
                    }
                }
            }
        }
        for (int i = 0; i < semTimes.size(); i++) {
            SemObject semTime =  semTimes.get(i);
            for (int j = 0; j < semTime.getNafMentions().size(); j++) {
                NafMention nafMention = semTime.getNafMentions().get(j);
                String sentenceId = nafMention.getSentence();
                if (!sentenceId.isEmpty()) {
                    try {
                        Integer sentenceInteger = Integer.parseInt(sentenceId);
                        if (sentenceInteger <= sentenceRange) {
                            semMicroTimes.add(semTime);
                        }
                    } catch (NumberFormatException e) {
                     //   e.printStackTrace();
                    }
                }
            }
        }
        for (int i = 0; i < semRelations.size(); i++) {
            SemRelation semRelation =  semRelations.get(i);
            for (int j = 0; j < semRelation.getNafMentions().size(); j++) {
                NafMention nafMention = semRelation.getNafMentions().get(j);
                String sentenceId = nafMention.getSentence();
                if (!sentenceId.isEmpty()) {
                    Integer sentenceInteger = null;
                    try {
                        sentenceInteger = Integer.parseInt(sentenceId);
                        if (sentenceInteger <= sentenceRange) {
                            semMicroRelations.add(semRelation);
                        }
                    } catch (NumberFormatException e) {
                       // e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("semEvents = " + semEvents.size());/*
        System.out.println("semMicroEvents = " + semMicroEvents.size());
        System.out.println("semActors = " + semActors.size());
        System.out.println("semMicroActors = " + semMicroActors.size());
        System.out.println("semTimes = " + semTimes.size());
        System.out.println("semMicroTimes = " + semMicroTimes.size());
        System.out.println("semRelations = " + semRelations.size());
        System.out.println("semMicroRelations = " + semMicroRelations.size());*/
        semEvents = new ArrayList<SemObject>();
        semEvents = semMicroEvents;
        semActors = semMicroActors;
        semTimes = semMicroTimes;
        semRelations = semMicroRelations;
    }


}
