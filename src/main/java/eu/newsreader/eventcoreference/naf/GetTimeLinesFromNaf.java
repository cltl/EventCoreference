package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.util.RoleLabels;
import eu.newsreader.eventcoreference.util.TimeLanguage;
import eu.newsreader.eventcoreference.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by piek on 10/22/14.
 */
public class GetTimeLinesFromNaf {
    /**
     * TimeLine:
     Steve Jobs
     1        2004                  18315-7-fighting    18355-4-fighting
     2        2005-06-05      1664-2-keynote
     3        2011-01            18315-7-leave
     4        2011-08-24      18315-2-step_down
     */
    static final public String ID_SEPARATOR = "#";
    static final public String URI_SEPARATOR = "_";

    static public void main (String [] args) {
        //String pathToNafFile = args[0];
        // String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-ontology/test/scale-test.naf";
        //String pathToNafFile = "/Users/piek/Desktop/NWR/timeline/1514-trialNWR20.naf";
        String pathToNafFile = "/Users/piek/Desktop/NWR/timeline/1514-trialPiekCoref.naf";
        //String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-ontology/test/possession-test.naf";
        //String pathToNafFile = "/Projects/NewsReader/collaboration/bulgarian/example/razni11-01.event-coref.naf";
        //String pathToNafFile = "/Projects/NewsReader/collaboration/bulgarian/fifa.naf";
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
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(pathToNafFile);
        String timeLines = processNafFile(new File(pathToNafFile).getName(), project, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations);
        try {
            OutputStream fos = new FileOutputStream(pathToNafFile+".tml");
            fos.write(timeLines.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
       // System.out.println(timeLines);

    }

    static public String processNafFile (String fileName, String project, KafSaxParser kafSaxParser,
                                       ArrayList<SemObject> semEvents,
                                       ArrayList<SemObject> semActors,
                                       ArrayList<SemObject> semPlaces,
                                       ArrayList<SemObject> semTimes,
                                       ArrayList<SemRelation> semRelations
    ) {
        String timeLine = "";
        TimeLanguage.setLanguage(kafSaxParser.getLanguage());
        String baseUrl = kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
        if (!baseUrl.toLowerCase().startsWith("http")) {
            baseUrl = ResourcesUri.nwrdata + project + "/" + kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
        }
        GetSemFromNafFile.processNafFileForActorPlaceInstances(baseUrl, kafSaxParser, semActors, semPlaces);
        SemTime docSemTime = GetSemFromNafFile.processNafFileForTimeInstances(baseUrl, kafSaxParser, semTimes);
        GetSemFromNafFile.processNafFileForEventInstances(baseUrl, kafSaxParser, semEvents);
        processNafFileForRelations(baseUrl, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations);
        for (int i = 0; i < semActors.size(); i++) {
            SemObject semObject = semActors.get(i);
            timeLine += semObject.getId()+"\n";
            timeLine += "\t";
            for (int j = 0; j < semObject.getNafMentions().size(); j++) {
                NafMention nafMention = semObject.getNafMentions().get(j);
                timeLine += nafMention.getTermsIds().toString()+"["+nafMention.getPhraseFromMention(kafSaxParser)+"];";
            }
            timeLine += "\n";
            ArrayList<String> coveredEvents = new ArrayList<String>();
            for (int j = 0; j < semRelations.size(); j++) {
                SemRelation semRelation = semRelations.get(j);
                if (semRelation.getObject().equals(semObject.getId())) {
                    /// we have an event involving the object
                    if (RoleLabels.hasPRIMEPARTICIPANT(semRelation.getPredicates()) || RoleLabels.hasSECONDPARTICIPANT(semRelation.getPredicates())) {
                        String eventId = semRelation.getSubject();

                        SemEvent semEvent = null;
                        for (int l = 0; l < semEvents.size(); l++) {
                            if (semEvents.get(l).getId().equals(eventId)) {
                                semEvent = (SemEvent) semEvents.get(l);
                                break;
                            }
                        }
                        if ((semEvent!=null) && (!coveredEvents.contains(semEvent.getId()))) {
                            coveredEvents.add(semEvent.getId());
                            /// This is the event involving this actor
                            /// Now get the time
                            //System.out.println("semEvent.getTopPhraseAsLabel() = " + semEvent.getTopPhraseAsLabel());
                            ArrayList<String> coveredTimes = new ArrayList<String>();
                            for (int m = 0; m < semRelations.size(); m++) {
                                SemRelation relation = semRelations.get(m);
                                if (relation.getSubject().equals(eventId))  {
                                    if (relation.getPredicates().contains("hasSemTime")) {
                                        String timeId = relation.getObject();
                                        for (int n = 0; n < semTimes.size(); n++) {
                                            SemTime semTime = (SemTime) semTimes.get(n);
                                            if (semTime.getId().equals(timeId)) {
                                                String timeString = semTime.getOwlTime().toString();
                                                if (!coveredTimes.contains(timeString)) {
                                                    coveredTimes.add(timeString);
                                                    timeLine += "\t" + timeString;
                                                    for (int o = 0; o < semEvent.getNafMentions().size(); o++) {
                                                        NafMention nafMention = semEvent.getNafMentions().get(o);
                                                        String sentenceId = "";
                                                        for (int p = 0; p < nafMention.getTokensIds().size(); p++) {
                                                            String tokenId = nafMention.getTokensIds().get(p);
                                                            KafWordForm kafWordForm = kafSaxParser.getWordForm(tokenId);
                                                            sentenceId += kafWordForm.getSent();
                                                        }
                                                        timeLine += "\t" + fileName + "-" + sentenceId.trim() + "-" + semEvent.getTopPhraseAsLabel()+nafMention.getTermsIds().toString();
                                                    }
                                                    timeLine += "\n";
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (coveredTimes.size()==0) {
                                timeLine += "\tNOTIMEX";
                                for (int o = 0; o < semEvent.getNafMentions().size(); o++) {
                                    NafMention nafMention = semEvent.getNafMentions().get(o);
                                    String sentenceId = "";
                                    for (int p = 0; p < nafMention.getTokensIds().size(); p++) {
                                        String tokenId = nafMention.getTokensIds().get(p);
                                        KafWordForm kafWordForm = kafSaxParser.getWordForm(tokenId);
                                        sentenceId += kafWordForm.getSent();
                                    }
                                    timeLine += "\t" + fileName + "-" + sentenceId.trim() + "-" + semEvent.getTopPhraseAsLabel()+nafMention.getTermsIds().toString();
                                }
                                timeLine += "\n";
                            }
                        }
                        else {
                          //  System.out.println("NO EVENTS");
                        }
                    }
                }
            }
            timeLine += "\n";
        }
        return timeLine;
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
     */
    static void processNafFileForRelations (String baseUrl, KafSaxParser kafSaxParser,
                                            ArrayList<SemObject> semEvents,
                                            ArrayList<SemObject> semActors,
                                            ArrayList<SemObject> semPlaces,
                                            ArrayList<SemObject> semTimes,
                                            ArrayList<SemRelation> semRelations
    ) {


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
            //// separate check for time that can be and should be derived from the timex layer....
            for (int l = 0; l < semTimes.size(); l++) {
                SemObject semTime = semTimes.get(l);
                //System.out.println("semTime.toString() = " + semTime.toString());
                // if (Util.matchAtLeastASingleSpan(kafParticipant.getSpanIds(), semTime)) {
                if (Util.sameSentence(kafSaxParser, semTime, semEvent)) {
                    /// create sem relations
                    timexRelationCount++;
                    SemRelation semRelation = new SemRelation();
                    String relationInstanceId = baseUrl+"timeRelation_"+timexRelationCount;
                    semRelation.setId(relationInstanceId);

                    ArrayList<String> termsIds = new ArrayList<String>();
                    for (int j = 0; j < semEvent.getNafMentions().size(); j++) {
                        NafMention nafMention = semEvent.getNafMentions().get(j);
                        termsIds.addAll(nafMention.getTermsIds());
                    }
                    for (int j = 0; j < semTime.getNafMentions().size(); j++) {
                        NafMention nafMention = semTime.getNafMentions().get(j);
                        termsIds.addAll(nafMention.getTermsIds());
                    }

                    NafMention mention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, termsIds);
                    semRelation.addMention(mention);
                    semRelation.addPredicate("hasSemTime");
                    semRelation.setSubject(semEvent.getId());
                    semRelation.setObject(semTime.getId());
                    semRelations.add(semRelation);
                    // System.out.println("semRelation = " + semRelation.getSubject());
                    // System.out.println("semRelation.getObject() = " + semRelation.getObject());
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
                            semRelation.addPredicate(kafParticipant.getRole());
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
                            semRelation.addPredicate(kafParticipant.getRole());
                            for (int j = 0; j < kafParticipant.getExternalReferences().size(); j++) {
                                KafSense kafSense = kafParticipant.getExternalReferences().get(j);
                                semRelation.addPredicate(kafSense.getResource() + ":" + kafSense.getSensecode());
                            }
                            semRelation.setSubject(semEventId);
                            semRelation.setObject(semObject.getId());
                            semRelations.add(semRelation);
                        }

                    }
/*
                    SemObject semObject = Util.getBestMatchingObject(kafSaxParser, kafParticipant, semActors);
                    if (semObject!=null) {
                        if (semObject.getId().endsWith("e4")) {
                            System.out.println(semObject.getURI());
                        }
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
                        semRelation.addPredicate("hasSemActor");
                        //// check the source and prefix accordingly
                        semRelation.addPredicate(kafParticipant.getRole());
                        for (int j = 0; j < kafParticipant.getExternalReferences().size(); j++) {
                            KafSense kafSense = kafParticipant.getExternalReferences().get(j);
                            semRelation.addPredicate(kafSense.getResource() + ":" + kafSense.getSensecode());
                        }
                        semRelation.setSubject(semEventId);
                        semRelation.setObject(semObject.getId());
                        semRelations.add(semRelation);
                    }
                    semObject = Util.getBestMatchingObject(kafSaxParser, kafParticipant, semPlaces);
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
                        semRelation.addPredicate(kafParticipant.getRole());
                        for (int j = 0; j < kafParticipant.getExternalReferences().size(); j++) {
                            KafSense kafSense = kafParticipant.getExternalReferences().get(j);
                            semRelation.addPredicate(kafSense.getResource() + ":" + kafSense.getSensecode());
                        }
                        semRelation.setSubject(semEventId);
                        semRelation.setObject(semObject.getId());
                        semRelations.add(semRelation);
                    }
*/

                }
            }
        }
    }



}
