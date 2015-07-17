package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.timeline.EntityTimeLine;
import eu.newsreader.eventcoreference.util.RoleLabels;
import eu.newsreader.eventcoreference.util.TimeLanguage;
import eu.newsreader.eventcoreference.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by piek on 10/22/14.
 */
public class GetTimeLinesFromNaf {

    //NEEDS TO BE REBUILT. GETSEMFROMNAF IS OUT OF DATE

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
    static HashMap<String, EntityTimeLine> entitiesTimeLineHashMap;
    static HashMap<String, EntityTimeLine> entityTimeLineHashMap;
    static Vector<String> communicationVector = null;
    static Vector<String> grammaticalVector = null;
    static Vector<String> contextualVector = null;
    static boolean NOENTITYCOREF = false;
    static boolean NOEVENTCOREF = false;


    static void reduceSentenceIdentifiers (KafSaxParser kafSaxParser) {
        for (int i = 0; i < kafSaxParser.getKafWordFormList().size(); i++) {
            KafWordForm kafWordForm = kafSaxParser.getKafWordFormList().get(i);
            Integer sentenceInt = Integer.parseInt(kafWordForm.getSent());
            sentenceInt--;
            kafWordForm.setSent(sentenceInt.toString());
        }
    }
    static public void main (String [] args) {
        //String pathToNafFile = args[0];
        String pathToNafFile = "";
        String processType = "";
        String extension = "";
        String project = "";
        String pathToFolder = "";

        processType = "event";
        //processType = "entity";
        // pathToNafFile = "/Users/piek/Desktop/NWR/timeline/naf_file_raw_out-2/17174-Apple_executive_leaves.xml.naf";
        // pathToNafFile = "/Users/piek/Desktop/NWR/timeline/1514-trialNWR20.naf";
        // pathToNafFile = "/Users/piek/Desktop/NWR/timeline/1514-trialPiekCoref.naf";
        // pathToNafFile = "/Users/piek/Desktop/NWR/NWR-ontology/test/possession-test.naf";
        // pathToNafFile = "/Projects/NewsReader/collaboration/bulgarian/example/razni11-01.event-coref.naf";
        // pathToNafFile = "/Projects/NewsReader/collaboration/bulgarian/fifa.naf";
        //pathToNafFile = "/Users/piek/Desktop/NWR/timeline/corpus_NAF_output_281114/corpus_apple_event_based/9549_Reactions_to_Apple.naf";
        //pathToFolder = "/Users/piek/Desktop/NWR/timeline/corpus_NAF_output_141214/corpus_airbus-entity-based-3";
        //pathToFolder = "/Users/piek/Desktop/NWR/timeline/corpus_NAF_output_141214/corpus_apple-entity-based-3";
        //pathToFolder = "/Users/piek/Desktop/NWR/timeline/corpus_NAF_output_141214/corpus_gm_chrysler_ford-entity-based-3";

        //pathToFolder = "/Users/piek/Desktop/NWR/timeline/corpus_NAF_output_141214/corpus_airbus_event_based_4";
        //pathToFolder = "/Users/piek/Desktop/NWR/timeline/corpus_NAF_output_141214/corpus_apple_event_based_4";
        //pathToFolder = "/Users/piek/Desktop/NWR/timeline/corpus_NAF_output_141214/corpus_gm_chrysler_ford_event_based_4";
       // pathToFolder = "/Users/piek/Desktop/NWR/timeline/corpus_NAF_output_141214/corpus_stock_market_event_based_4";
        //pathToFolder = "/Users/piek/Desktop/WNACP/corpus_airbus_NAF/test/";
        //pathToFolder = "/Users/piek/Desktop/NWR/timeline/wiki_bio_out";
        //pathToFolder = "/Users/piek/Desktop/NWR/timeline/corpus_NAF_output_141214/corpus_airbus-event-based-3";
        //pathToFolder = "/Users/piek/Desktop/NWR/timeline/corpus_NAF_output_141214/corpus_apple-event-based-3";
        //pathToFolder = "/Users/piek/Desktop/NWR/timeline/corpus_NAF_output_141214/corpus_gm_chrysler_ford-event-based-3";
        //pathToFolder = "/Users/piek/Desktop/NWR/timeline/corpus_NAF_output_141214/corpus_stock_market-event-based-3";
       // pathToFolder = "/Users/piek/Desktop/NWR/timeline/test";
        extension = ".naf";
        project = "semeval_timeline";

        String comFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-eventcoreference_v2_2014/resources/communication.txt";
        String contextualFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-eventcoreference_v2_2014/resources/contextual.txt";
        String grammaticalFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-eventcoreference_v2_2014/resources/grammatical.txt";

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--naf-file") && args.length>(i+1)) {
                pathToNafFile = args[i+1];
            }
            else if (arg.equals("--project") && args.length>(i+1)) {
                project = args[i+1];
            }
            else if (arg.equals("--folder") && args.length>(i+1)) {
                pathToFolder = args[i+1];
            }
            else if (arg.equals("--extension") && args.length>(i+1)) {
                extension = args[i+1];
            }
            else if (arg.equals("--process-type") && args.length>(i+1)) {
                processType = args[i+1];
            }
            else if (arg.equals("--communication-frames") && args.length>(i+1)) {
                comFrameFile = args[i+1];
            }
            else if (arg.equals("--grammatical-frames") && args.length>(i+1)) {
                grammaticalFrameFile = args[i+1];
            }
            else if (arg.equals("--contextual-frames") && args.length>(i+1)) {
                contextualFrameFile = args[i+1];
            }
            else if (arg.equals("--noentity-coref")) {
                NOENTITYCOREF = true;
            }
            else if (arg.equals("--noevent-coref")) {
                NOEVENTCOREF = true;
            }
        }

        //// read resources
        communicationVector = Util.ReadFileToStringVector(comFrameFile);
        grammaticalVector = Util.ReadFileToStringVector(grammaticalFrameFile);
        contextualVector = Util.ReadFileToStringVector(contextualFrameFile);

        if (!pathToNafFile.isEmpty()) {
            KafSaxParser kafSaxParser = new KafSaxParser();
            kafSaxParser.parseFile(pathToNafFile);
            //// THIS FIX IS NEEDED BECAUSE SOME OF THE COREF SETS ARE TOO BIG
            Util.fixEventCoreferenceSets(kafSaxParser);
            //// THIS IS NEEDED TO FILTER ESO MAPPING AND IGNORE OTHERS
            Util.fixExternalReferencesSrl(kafSaxParser);
            reduceSentenceIdentifiers(kafSaxParser);
            String timeLines = processNafFile(new File(pathToNafFile), project, kafSaxParser, processType);
            try {
                OutputStream fos = new FileOutputStream(pathToNafFile + ".tml");
                fos.write(timeLines.getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
/*
            timeLines = tripleNafFile(new File(pathToNafFile), project, kafSaxParser, processType);
            try {
                OutputStream fos = new FileOutputStream(pathToNafFile + ".trp");
                fos.write(timeLines.getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
*/
        }
        else if (!pathToFolder.isEmpty()) {

                    KafSaxParser kafSaxParser = new KafSaxParser();
                    ArrayList<File> files = Util.makeFlatFileList(new File(pathToFolder), extension);
                    for (int i = 0; i < files.size(); i++) {
                        File file = files.get(i);
                        // System.out.println("file.getName() = " + file.getName());
                        kafSaxParser.parseFile(file);
                        //// THIS FIX IS NEEDED BECAUSE SOME OF THE COREF SETS ARE TOO BIG
                        ///  GetSemFromNafFile.fixEventCoreferenceSets(kafSaxParser);
                        //// THIS IS NEEDED TO FILTER ESO MAPPING AND IGNORE OTHERS
                        Util.fixExternalReferencesSrl(kafSaxParser);
                        //GetSemFromNafFile.fixExternalReferencesEntities(kafSaxParser);
                        reduceSentenceIdentifiers(kafSaxParser);

                        /// process single filew
                        String timeLines = processNafFile(file, project, kafSaxParser, processType);
                        // String timeLines = processNafFileEventBased(file, project, kafSaxParser);
                        try {
                            OutputStream fos = new FileOutputStream(file + ".tml");
                            fos.write(timeLines.getBytes());
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

/*
                        timeLines = tripleNafFile(new File(pathToNafFile), project, kafSaxParser, processType);
                       // System.out.println("timeLines = " + timeLines);
                        try {
                            OutputStream fos2 = new FileOutputStream(file + ".trp");
                            fos2.write(timeLines.getBytes());
                            fos2.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
*/
                    }
        }

       // System.out.println(timeLines);

    }

    static String getEventTypeString (SemEvent semEvent) {
        String eventTypes = "";
        for (int k = 0; k < semEvent.getConcepts().size(); k++) {
            KafSense kafSense = semEvent.getConcepts().get(k);
            if (kafSense.getResource().equalsIgnoreCase("framenet")) {
                //eventTypes += "fn:"+kafSense.getSensecode()+";";
                if (communicationVector!=null && communicationVector.contains(kafSense.getSensecode().toLowerCase())) {
                    eventTypes+="fn:communication@"+kafSense.getSensecode()+";";
                }
                else if (grammaticalVector!=null && grammaticalVector.contains(kafSense.getSensecode().toLowerCase())) {
                    eventTypes+="fn:grammatical@"+kafSense.getSensecode()+";";
                }
                if (contextualVector!=null && contextualVector.contains(kafSense.getSensecode().toLowerCase())) {
                    eventTypes+="fn:contextual@"+kafSense.getSensecode()+";";
                }
            }
            else if (kafSense.getResource().equalsIgnoreCase("eso")) {
                eventTypes += "eso:"+kafSense.getSensecode()+";";
            }
            else if (kafSense.getResource().equalsIgnoreCase("eventtype")) {
                //  eventTypes += "nwr:"+kafSense.getSensecode()+";";
            }
            else if (kafSense.getResource().equalsIgnoreCase("wordnet")) {
                eventTypes += "wn:"+kafSense.getSensecode()+";";
            }
            else {
            }
        }
        return eventTypes;
    }



    static public String processNafFile (File file, String project, KafSaxParser kafSaxParser, String processType) {
        String timeline = "";
        TimeLanguage.setLanguage(kafSaxParser.getLanguage());
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        String baseUrl = "";
        String entityUri = ResourcesUri.nwrdata+project+"/entities/";
        if (!kafSaxParser.getKafMetaData().getUrl().isEmpty()) {
            baseUrl = kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
            if (!baseUrl.toLowerCase().startsWith("http")) {
                baseUrl = ResourcesUri.nwrdata + project + "/" + kafSaxParser.getKafMetaData().getUrl() + ID_SEPARATOR;
            }
        }
        else {
            baseUrl = ResourcesUri.nwrdata + project + "/" + file.getName() + ID_SEPARATOR;
        }
        if (NOENTITYCOREF) {
            GetSemFromNafFile.processNafFileForEntityWithoutCoreferenceSets(entityUri, baseUrl, kafSaxParser, semActors);
        }
        else {
            GetSemFromNafFile.processNafFileForEntityCoreferenceSets(entityUri, baseUrl, kafSaxParser, semActors);
        }
        //GetSemFromNafFile.processNafFileForRemainingSrlActors(entityUri, baseUrl, kafSaxParser, semActors);
        SemTime docSemTime = GetSemFromNafFile.processNafFileForTimeInstances(baseUrl, kafSaxParser, semTimes);
        if (NOEVENTCOREF) {
            GetSemFromNafFile.processNafFileForEventWithoutCoreferenceSets(baseUrl, kafSaxParser, semEvents);
        }
        else {
            GetSemFromNafFile.processNafFileForEventCoreferenceSets(baseUrl, kafSaxParser, semEvents);
        }
        //GetSemFromNafFile.filterOverlapEventsEntities(semEvents, semActors);
        processNafFileForRelations(baseUrl, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations);
/*        try {
            OutputStream fos = new FileOutputStream(file.getAbsolutePath()+".trg");
            JenaSerialization.serializeJena(fos, semEvents, semActors, semPlaces, semTimes, semRelations, null, null);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        if (processType.equalsIgnoreCase("entity")) {
            timeline = processNafFileEntityBased(semEvents, semActors, semTimes, semRelations, kafSaxParser);
        }
        else if (processType.equalsIgnoreCase("event")) {
            timeline = processNafFileEventBased(semEvents, semActors, semTimes, semRelations, kafSaxParser);
           // timeline = getEventActorRelations(semEvents, semActors, semTimes, semRelations, kafSaxParser);

        }
        return timeline;
    }

    /**
     * Creates a timeline with the date string and all mentions for an event and all the associated event types
     * @param timeString
     * @param semEvent
     * @param kafSaxParser
     * @return
     */
    static String makeTimeLineStringForDateString (String timeString, SemEvent semEvent, KafSaxParser kafSaxParser) {
        String timeLineString = "";
        ArrayList<String> coveredEventMentions = new ArrayList<String>();
        timeLineString = "\t" + timeString;
        String eventTypes = getEventTypeString(semEvent);
        for (int o = 0; o < semEvent.getNafMentions().size(); o++) {
            NafMention nafMention = semEvent.getNafMentions().get(o);
            String sentenceId = "";
            String wordForm = "";
            for (int p = 0; p < nafMention.getTokensIds().size(); p++) {
                String tokenId = nafMention.getTokensIds().get(p);
                KafWordForm kafWordForm = kafSaxParser.getWordForm(tokenId);
                wordForm = kafWordForm.getWf();
                sentenceId += kafWordForm.getSent();
            }
            String value = "\t" + sentenceId.trim() + "-" + wordForm+nafMention.getTermsIds().toString();
            if (!coveredEventMentions.contains(value)) {
                timeLineString += value;
                coveredEventMentions.add(value); //// avoid duplicates
            }
        }
        timeLineString +="\tTYPES:"+eventTypes;
        return timeLineString;
    }


    static String makeListOfActorMentions (SemEvent semEvent,
                                           ArrayList<SemRelation> semRelations,
                                           ArrayList<SemObject> semActors,
                                           KafSaxParser kafSaxParser) {

        String actorMentions = "";
        for (int j = 0; j < semRelations.size(); j++) {
            SemRelation semRelation = semRelations.get(j);
            if (semRelation.getSubject().equals(semEvent.getId())) {
                /// we have an object involved in this event the object
                String role = "";
                if (RoleLabels.hasPRIMEPARTICIPANT(semRelation.getPredicates())) {
                    role = "A0";
                }
                else if (RoleLabels.hasSECONDPARTICIPANT(semRelation.getPredicates())) {
                    role = "A2";
                }
                else if (RoleLabels.hasTHIRDPARTICIPANT(semRelation.getPredicates())) {
                    role = "A3";
                }
                if (!role.isEmpty()) {
                    String actorId = semRelation.getObject();
                    for (int k = 0; k < semActors.size(); k++) {
                        SemObject semObject = semActors.get(k);
                        if (actorId.equals(semObject.getId())) {
                            actorMentions += "\t" + role + ":" + semObject.getId();
                            actorMentions += "\t";
                            for (int l = 0; l < semObject.getNafMentions().size(); l++) {
                                NafMention nafMention = semObject.getNafMentions().get(l);
                                actorMentions += nafMention.getTermsIds().toString() + "[" + nafMention.getPhraseFromMention(kafSaxParser) + "]";
                            }
                            if (k < (semObject.getNafMentions().size() - 1)) {
                                actorMentions += ";";
                            }
                        }
                    }
                }
            }
        }
        return actorMentions;
    }

    static String getTimeLineString (SemEvent semEvent,
                                     ArrayList<SemRelation> semRelations,
                                     ArrayList<SemObject> semTimes,
                                     String entityRoles,
                                     KafSaxParser kafSaxParser) {
        String timeLine = "";
        ArrayList<String> coveredTimes = new ArrayList<String>();
        for (int m = 0; m < semRelations.size(); m++) {
            SemRelation relation = semRelations.get(m);
            if (relation.getSubject().equals(semEvent.getId()))  {
                if (relation.getPredicates().contains("hasSemTime")) {
                    String timeId = relation.getObject();
                    //// iterate ove the time objects
                    for (int n = 0; n < semTimes.size(); n++) {
                        SemTime semTime = (SemTime) semTimes.get(n);
                        if (semTime.getId().equals(timeId)) {
                            String timeString = semTime.getOwlTime().toString();
                            if (!coveredTimes.contains(timeString)) { /// avoid duplicates
                                coveredTimes.add(timeString);
                                timeLine += makeTimeLineStringForDateString(timeString, semEvent, kafSaxParser);
                                if (!entityRoles.isEmpty()) {
                                    timeLine+=entityRoles;
                                }
                                timeLine += "\n";
                                break;
                            }
                        }
                    }
                }
            }
            else {
                //// not a time relation
            }
        }
        if (coveredTimes.size()==0) {
            String timeString = "NOTIMEX";
            timeLine = makeTimeLineStringForDateString(timeString, semEvent, kafSaxParser);
            if (!entityRoles.isEmpty()) {
                timeLine+=entityRoles;
            }
            timeLine += "\n";
        }
        return timeLine;
    }

    static public String processNafFileEntityBased (ArrayList<SemObject> semEvents,
                                                    ArrayList<SemObject> semActors ,
                                                    ArrayList<SemObject> semTimes,
                                                    ArrayList<SemRelation> semRelations,
                                                    KafSaxParser kafSaxParser
                                                     ) {
        String timeLine = "";

        for (int i = 0; i < semActors.size(); i++) {
            SemObject semObject = semActors.get(i);
            timeLine += semObject.getId()+"\n";
            timeLine += "\t";
            for (int j = 0; j < semObject.getNafMentions().size(); j++) {
                NafMention nafMention = semObject.getNafMentions().get(j);
                timeLine += nafMention.getTermsIds().toString()+"["+nafMention.getPhraseFromMention(kafSaxParser)+"];";
            }
            timeLine += "\n\tTIMELINE\n";
            ArrayList<String> coveredEvents = new ArrayList<String>();
            for (int j = 0; j < semRelations.size(); j++) {
                SemRelation semRelation = semRelations.get(j);
                if (semRelation.getObject().equals(semObject.getId())) {
                    /// we have an event involving the object
                    if (RoleLabels.hasPRIMEPARTICIPANT(semRelation.getPredicates()) ||
                            RoleLabels.hasSECONDPARTICIPANT(semRelation.getPredicates()) ||
                            RoleLabels.hasTHIRDPARTICIPANT(semRelation.getPredicates())
                            ) {
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

                            timeLine += getTimeLineString(semEvent, semRelations, semTimes, "", kafSaxParser);
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

    static public String processNafFileEventBased (ArrayList<SemObject> semEvents,
                                                   ArrayList<SemObject> semActors ,
                                                   ArrayList<SemObject> semTimes,
                                                   ArrayList<SemRelation> semRelations,
                                                   KafSaxParser kafSaxParser
    ) {
        String timeLine = "";
        ArrayList<String> coveredEvents = new ArrayList<String>();
        for (int i = 0; i < semEvents.size(); i++) {
            SemEvent semEvent = (SemEvent) semEvents.get(i);
            if (!coveredEvents.contains(semEvent.getId())) {
                coveredEvents.add(semEvent.getId());
                String entityRoles = makeListOfActorMentions(semEvent, semRelations, semActors, kafSaxParser);
                timeLine += getTimeLineString(semEvent, semRelations, semTimes, entityRoles, kafSaxParser);
            }
        }
        return timeLine;
    }


    /**
     * @Deprecated I have no idea why we used this function!
     * @param semEvents
     * @param semActors
     * @param semTimes
     * @param semRelations
     * @param kafSaxParser
     * @return
     */
    static public String getEventActorRelations (ArrayList<SemObject> semEvents,
                                                   ArrayList<SemObject> semActors ,
                                                   ArrayList<SemObject> semTimes,
                                                   ArrayList<SemRelation> semRelations,
                                                   KafSaxParser kafSaxParser
    ) {
        String timeLine = "";
        for (int i = 0; i < semEvents.size(); i++) {
            SemEvent semEvent = (SemEvent) semEvents.get(i);
            String typeCheck = getEventTypeString(semEvent);
            //// this check restricts it to communication events only
            if (typeCheck.indexOf("fn:communication")==-1){
               continue;
            }
            ArrayList<String> actorIds = new ArrayList<String>();
            ArrayList<String> actorRoles = new ArrayList<String>();
            ArrayList<String> dateStrings = new ArrayList<String>();

            /// iterates over all relations to get a list of actor IDs with the appropriate roles and any time IDs
            for (int j = 0; j < semRelations.size(); j++) {
                SemRelation semRelation = semRelations.get(j);
                if (semRelation.getSubject().equals(semEvent.getId())) {
                    /// we have an event involving the object
                    if (RoleLabels.hasPRIMEPARTICIPANT(semRelation.getPredicates()) ||
                            RoleLabels.hasSECONDPARTICIPANT(semRelation.getPredicates()) ||
                            RoleLabels.hasTHIRDPARTICIPANT(semRelation.getPredicates())

                            ) {
                        String role = "";
                        if (RoleLabels.hasPRIMEPARTICIPANT(semRelation.getPredicates())) {
                            role = "A0";
                        }
                        else if (RoleLabels.hasSECONDPARTICIPANT(semRelation.getPredicates())) {
                            role = "A2";
                        }
                        else if (RoleLabels.hasTHIRDPARTICIPANT(semRelation.getPredicates())) {
                            role = "A3";
                        }
                        String objectId = semRelation.getObject();
                        if ((!actorIds.contains(objectId))) {
                            actorIds.add(objectId);
                            actorRoles.add(role);
                        }
                    }
                    else if (semRelation.getPredicates().contains("hasSemTime")) {
                        String timeId = semRelation.getObject();
                        for (int n = 0; n < semTimes.size(); n++) {
                            SemTime semTime = (SemTime) semTimes.get(n);
                            if (semTime.getId().equals(timeId)) {
                                String timeString = semTime.getOwlTime().toString();
                                if (!dateStrings.contains(timeString)) {
                                    dateStrings.add(timeString);
                                }
                            }
                        }
                    }
                }
            }
            if (dateStrings.isEmpty()) {
                timeLine += "\t" + "NOTIMEX"+"\t";
                String eventTypes = getEventTypeString(semEvent);
                for (int o = 0; o < semEvent.getNafMentions().size(); o++) {
                    NafMention nafMention = semEvent.getNafMentions().get(o);
                    String sentenceId = "";
                    for (int p = 0; p < nafMention.getTokensIds().size(); p++) {
                        String tokenId = nafMention.getTokensIds().get(p);
                        KafWordForm kafWordForm = kafSaxParser.getWordForm(tokenId);
                        sentenceId += kafWordForm.getSent();
                    }
                    String value =  sentenceId.trim() + "-" + semEvent.getTopPhraseAsLabel()+nafMention.getTermsIds().toString();
                    if (o<(semEvent.getNafMentions().size()-1)) {
                        value +=";";
                    }
                    timeLine += value;
                }
                timeLine +="\tTYPES:"+eventTypes;
                for (int j = 0; j < actorIds.size(); j++) {
                    String actorId = actorIds.get(j);
                    String actorRole = actorRoles.get(j);
                    for (int k = 0; k < semActors.size(); k++) {
                        SemObject semObject = semActors.get(k);
                        if (actorId.equals(semObject.getId())) {
                            timeLine += "\t" + actorRole+":"+semObject.getId();
                            timeLine += "\t";
                            for (int l = 0; l < semObject.getNafMentions().size(); l++) {
                                NafMention nafMention = semObject.getNafMentions().get(l);
                                timeLine += nafMention.getTermsIds().toString() + "[" + nafMention.getPhraseFromMention(kafSaxParser) + "]";
                            }
                            if (k<(semObject.getNafMentions().size()-1)) {
                                timeLine +=";";
                            }
                        }
                    }
                }
                timeLine += "\n";
            }
            else {
                for (int m= 0; m < dateStrings.size(); m++) {
                    String dateString =  dateStrings.get(m);


                    timeLine += "\t" + dateString+"\t";
                    String eventTypes = getEventTypeString(semEvent);

                    for (int o = 0; o < semEvent.getNafMentions().size(); o++) {
                        NafMention nafMention = semEvent.getNafMentions().get(o);
                        String sentenceId = "";
                        for (int p = 0; p < nafMention.getTokensIds().size(); p++) {
                            String tokenId = nafMention.getTokensIds().get(p);
                            KafWordForm kafWordForm = kafSaxParser.getWordForm(tokenId);
                            sentenceId += kafWordForm.getSent();
                        }
                        String value = sentenceId.trim() + "-" + semEvent.getTopPhraseAsLabel()+nafMention.getTermsIds().toString();
                        if (o<(semEvent.getNafMentions().size()-1)) {
                            value +=";";
                        }
                        timeLine += value;
                    }
                    timeLine +="\tTYPES:"+eventTypes;

                    /// adding the actor mentions here
                    for (int j = 0; j < actorIds.size(); j++) {
                        String actorId = actorIds.get(j);
                        String actorRole = actorRoles.get(j);
                        for (int k = 0; k < semActors.size(); k++) {
                            SemObject semObject = semActors.get(k);
                            if (actorId.equals(semObject.getId())) {
                                timeLine += "\t" + actorRole+":"+semObject.getId();
                                timeLine += "\t";
                                for (int l = 0; l < semObject.getNafMentions().size(); l++) {
                                    NafMention nafMention = semObject.getNafMentions().get(l);
                                    timeLine += nafMention.getTermsIds().toString() + "[" + nafMention.getPhraseFromMention(kafSaxParser) + "]";
                                }
                                if (k<(semObject.getNafMentions().size()-1)) {
                                    timeLine +=";";
                                }
                            }
                        }
                    }
                    timeLine += "\n";
                }
            }
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
                    }
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
/*
            if (kafEvent.getId().equals("pr1")) {
                System.out.println("semEventId = " + semEventId);
            }
*/

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
/*
                    if (kafEvent.getId().equals("pr1")) {
                        System.out.println("kafParticipant = " + kafParticipant.getId());
                        System.out.println("semObjects.toString() = " + semObjects.toString());
                    }
*/
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
                }
            }
        }
    }



}
