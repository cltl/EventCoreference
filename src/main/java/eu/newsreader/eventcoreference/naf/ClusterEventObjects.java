package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.newsreader.eventcoreference.coref.ComponentMatch;
import eu.newsreader.eventcoreference.input.FrameNetReader;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.util.FrameTypes;
import eu.newsreader.eventcoreference.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: Piek
 * Date: 11/14/13
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClusterEventObjects {



    /*
        @TODO
        1. proper reference to the ontologies (even if not there yet)
        7. parametrize the module to get high-precision or high-recall TriG
        8. entities that are not part of events are not in the output
     */

    static final String USAGE = "This program processes NAF files and stores binary objects for events with all related data in different object files based on the event type and the date\n" +
            "The program has the following arguments:\n" +
            "--naf-folder           <path>   <Folder with the NAF files to be processed. Reads NAF files recursively>\n" +
            "--event-folder         <path>   <Folder below which the event folders are created that hold the object file. " +
            "                                 The output structure is event/other, event/grammatical and event/speech.>\n" +
            "--extension            <string> <File extension to select the NAF files .>\n" +
            "--project              <string> <The name of the project for creating URIs>\n" +
            "--non-entities                  <If used, additional FrameNet roles and non-entity phrases are included>\n" +
            "--contextual-frames    <path>   <Path to a file with the FrameNet frames considered contextual>\n" +
            "--communication-frames <path>   <Path to a file with the FrameNet frames considered source>\n" +
            "--grammatical-frames   <path>   <Path to a file with the FrameNet frames considered grammatical>\n" +
            "--frame-level          <integer><Depth of path for the FrameNet relations>\n" +
            "--frame-relations      <path>   <path to FrameNet file with relations>\n" +
            "--microstories         <integer><Number of sentences to restrict the analysis>\n" +
            "--bridging                      <Whether or not microstories are extended through bridging relations>\n";
    static Vector<String> communicationVector = null;
    static Vector<String> grammaticalVector = null;
    static Vector<String> contextualVector = null;
    static FrameNetReader frameNetReader = new FrameNetReader();
    static final int TIMEEXPRESSIONMAX = 5;
    static boolean MICROSTORIES = false;
    static Integer SENTENCERANGE = 0;
    static boolean BRIDGING = false;
    static String done = "";
    static boolean ADDITIONALROLES = false;
    static public void main (String [] args) {
        if (args.length==0) {
            System.out.println(USAGE);
            System.out.println("NOW RUNNING WITH DEFAULT SETTINGS");
          //  return;
        }
       // String pathToNafFolder = "/Users/piek/Desktop/NWR/NWR-ontology/reasoning/change-of-scale";
       // String pathToEventFolder = "/Users/piek/Desktop/NWR/NWR-ontology/reasoning/change-of-scale";
        String pathToNafFolder = "";
        String pathToEventFolder = "";
        //String pathToNafFolder = "/Users/piek/Desktop/NWR/NWR-DATA/worldcup/ian-test";
        //String pathToEventFolder = "/Users/piek/Desktop/NWR/NWR-DATA/worldcup";
       // String pathToNafFolder = "/Code/vu/newsreader/EventCoreference/LN_football_test_out-tiny";
        String projectName  = "";
        String extension = "";
        String comFrameFile = "";
        String contextualFrameFile = "";
        String grammaticalFrameFile = "";
        String fnFile = "";
        int fnLevel = 0;
/*
        extension = ".naf";
        comFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-eventcoreference_v2_2014/resources/communication.txt";
        contextualFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-eventcoreference_v2_2014/resources/contextual.txt";
        grammaticalFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-eventcoreference_v2_2014/resources/grammatical.txt";
*/

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--naf-folder") && args.length>(i+1)) {
                pathToNafFolder = args[i+1];
            }
            else if (arg.equals("--event-folder") && args.length>(i+1)) {
                pathToEventFolder = args[i+1];
            }
            else if (arg.equals("--extension") && args.length>(i+1)) {
                extension = args[i+1];
            }
            else if (arg.equals("--project") && args.length>(i+1)) {
                projectName = args[i+1];
            }
            else if (arg.equals("--non-entities")) {
                ADDITIONALROLES = true;
            }
            else if (arg.equals("--bridging")) {
                BRIDGING = true;
            }
            else if (arg.equals("--microstories")&& args.length>(i+1)) {
                MICROSTORIES = true;
                SENTENCERANGE = Integer.parseInt(args[i+1]);
            }
            else if (arg.equals("--frame-relations") && args.length>(i+1)) {
                fnFile = args[i+1];
            }
            else if (arg.equals("--frame-level") && args.length>(i+1)) {
                try {
                    fnLevel = Integer.parseInt(args[i+1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
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
            else if (arg.equals("--rename") && args.length>(i+1)) {
                done = args[i+1];
            }
        }

        if (!fnFile.isEmpty()) {
            frameNetReader.parseFile(fnFile);
            frameNetReader.flatRelations(fnLevel);
            System.out.println("frameNetReader sub= " + frameNetReader.subToSuperFrame.size());
            System.out.println("frameNetReader super= " + frameNetReader.superToSubFrame.size());
        }
        //// read resources
        communicationVector = Util.ReadFileToStringVector(comFrameFile);
        grammaticalVector = Util.ReadFileToStringVector(grammaticalFrameFile);
        contextualVector = Util.ReadFileToStringVector(contextualFrameFile);
/*
        System.out.println("communicationVector = " + communicationVector.size());
        System.out.println("contextualVector = " + contextualVector.size());
        System.out.println("grammaticalVector = " + grammaticalVector.size());
*/

        try {
            if (pathToNafFolder.isEmpty())  {
                processStreamEvents(projectName, System.in, new File(pathToEventFolder));
            }
            else{
                processFolderEvents(projectName, new File(pathToNafFolder), new File(pathToEventFolder), extension);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void processFolderEvents (String project, File pathToNafFolder, File eventParentFolder, String extension

    ) throws IOException {
        File eventFolder = new File(eventParentFolder + "/events");
        if (!eventFolder.exists()) {
            eventFolder.mkdir();
        }
        if (!eventFolder.exists()) {
            System.out.println("Cannot create the eventFolder = " + eventFolder);
            return;
        }
        File speechFolder = new File(eventFolder + "/" + "source");
        if (!speechFolder.exists()) {
            speechFolder.mkdir();
        }
        if (!speechFolder.exists()) {
            System.out.println("Cannot create the speechFolder = " + speechFolder);
            return;
        }
        File otherFolder = new File(eventFolder + "/" + "contextual");
        if (!otherFolder.exists()) {
            otherFolder.mkdir();
        }
        if (!otherFolder.exists()) {
            System.out.println("Cannot create the otherFolder = " + otherFolder);
            return;
        }
        File grammaticalFolder = new File(eventFolder + "/" + "grammatical");
        if (!grammaticalFolder.exists()) {
            grammaticalFolder.mkdir();
        }
        if (!grammaticalFolder.exists()) {
            System.out.println("Cannot create the grammaticalFolder = " + grammaticalFolder);
            return;
        }

        KafSaxParser kafSaxParser = new KafSaxParser();

        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        ArrayList<SemRelation> factRelations = new ArrayList<SemRelation>();
        ArrayList<PerspectiveObject> perspectiveObjects = new ArrayList<PerspectiveObject>();

        ArrayList<File> files = Util.makeRecursiveFileList(pathToNafFolder, extension);
        System.out.println("files.size() = " + files.size());

        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
           // System.out.println("file.getName() = " + file.getName());
/*            if (!file.getName().startsWith("7YXG-CS51-2RYC-J2CN.xml")) {
                     continue;
            }*/

            if (i % 500 == 0) {
               // System.out.println("i = " + i);
                //  System.out.println("file.getName() = " + file.getAbsolutePath());
            }

            semEvents = new ArrayList<SemObject>();
            semActors = new ArrayList<SemObject>();
            semTimes = new ArrayList<SemObject>();
            semPlaces = new ArrayList<SemObject>();
            semRelations = new ArrayList<SemRelation>();
            factRelations = new ArrayList<SemRelation>();

          //  System.out.println("file.getName() = " + file.getName());
            kafSaxParser.parseFile(file.getAbsolutePath());
            if (kafSaxParser.getKafMetaData().getUrl().isEmpty()) {
                System.out.println("SKIPPING NAF due to empty url in header kafSaxParser.getKafMetaData().getUrl() = " + kafSaxParser.getKafMetaData().getUrl());
            }
            else {
              //  System.out.println("kafSaxParser.getKafMetaData().getUrl() = " + kafSaxParser.getKafMetaData().getUrl());
                processKafSaxParserOutputFolder(file.getName(), project,
                        kafSaxParser, perspectiveObjects, speechFolder, otherFolder, grammaticalFolder,
                        semEvents, semActors, semPlaces, semTimes, semRelations, factRelations);
                if (!done.isEmpty()) {
                    File doneFile = new File(file.getAbsolutePath() + done);
                    file.renameTo(doneFile);
                }
            }
        }
        if (perspectiveObjects.size()>0) {
            GetPerspectiveRelations.perspectiveRelationsToTrig(eventFolder, perspectiveObjects);
        }

    }

    public static void processStreamEvents (String project, InputStream nafStream, File eventParentFolder

    ) throws IOException {
        File eventFolder = new File(eventParentFolder + "/events");
        if (!eventFolder.exists()) {
            eventFolder.mkdir();
        }
        if (!eventFolder.exists()) {
            System.out.println("Cannot create the eventFolder = " + eventFolder);
            return;
        }
        File speechFolder = new File(eventFolder + "/" + "source");
        if (!speechFolder.exists()) {
            speechFolder.mkdir();
        }
        if (!speechFolder.exists()) {
            System.out.println("Cannot create the speechFolder = " + speechFolder);
            return;
        }
        File otherFolder = new File(eventFolder + "/" + "contextual");
        if (!otherFolder.exists()) {
            otherFolder.mkdir();
        }
        if (!otherFolder.exists()) {
            System.out.println("Cannot create the otherFolder = " + otherFolder);
            return;
        }
        File grammaticalFolder = new File(eventFolder + "/" + "grammatical");
        if (!grammaticalFolder.exists()) {
            grammaticalFolder.mkdir();
        }
        if (!grammaticalFolder.exists()) {
            System.out.println("Cannot create the grammaticalFolder = " + grammaticalFolder);
            return;
        }
        ArrayList<PerspectiveObject> perspectiveObjects = new ArrayList<PerspectiveObject>();
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(nafStream);
        if (kafSaxParser.getKafMetaData().getUrl().isEmpty()) {
            System.out.println("SKIPPING NAF due to empty url in header kafSaxParser.getKafMetaData().getUrl() = " + kafSaxParser.getKafMetaData().getUrl());
        }
        else {
            processKafSaxParser(project, kafSaxParser, perspectiveObjects, speechFolder, otherFolder, grammaticalFolder);
        /*if (perspectiveObjects.size()>0) {
            GetPerspectiveRelations.perspectiveRelationsToTrig(eventParentFolder, perspectiveObjects);
        }*/
        }
    }

    static void processKafSaxParser(String project, KafSaxParser kafSaxParser, ArrayList<PerspectiveObject> perspectiveObjects,
                        File speechFolder,
                        File otherFolder,
                        File grammaticalFolder) throws IOException {
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        ArrayList<SemRelation> factRelations = new ArrayList<SemRelation>();
        processKafSaxParserOutputFolder("", project, kafSaxParser, perspectiveObjects,speechFolder, otherFolder, grammaticalFolder,
                semEvents, semActors, semPlaces, semTimes, semRelations, factRelations);
    }


    static void processKafSaxParserOutputFolder(String nafFileName, String project, KafSaxParser kafSaxParser, ArrayList<PerspectiveObject> perspectives,
                                    File speechFolder, File otherFolder, File grammaticalFolder,
                                    ArrayList<SemObject> semEvents ,
                                    ArrayList<SemObject> semActors,
                                    ArrayList<SemObject> semPlaces,
                                    ArrayList<SemObject> semTimes,
                                    ArrayList<SemRelation> semRelations,
                                    ArrayList<SemRelation> factRelations
    ) throws IOException {

        if (MICROSTORIES) {
            GetSemFromNafFile.processNafFileWithAdditionalRoles(project, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations);

            System.out.println("all semEvents.size() = " + semEvents.size());
            System.out.println("all semActors.size() = " + semActors.size());
            ArrayList<SemObject> microSemEvents = CreateMicrostory.getMicroEvents(SENTENCERANGE, semEvents);
            ArrayList<SemObject> microSemActors = CreateMicrostory.getMicroActors(SENTENCERANGE, semActors);

            System.out.println("microSemEvents (sentence range:"+SENTENCERANGE+ ") = " + microSemEvents.size());
            System.out.println("microSemActors (sentence range:"+SENTENCERANGE+ ") = " + microSemActors.size());

/*            for (int i = 0; i < microSemEvents.size(); i++) {
                SemObject semObject = microSemEvents.get(i);
                System.out.println("micro semEvent.getURI() = " + semObject.getURI());
            }*/
            if (BRIDGING) {
                ArrayList<SemObject> coparticipationEvents = CreateMicrostory.getEventsThroughCoparticipation(semEvents, microSemEvents, microSemActors, semRelations);
                ArrayList<SemObject> coparticipationActors = CreateMicrostory.getActorsThroughCoparticipation(microSemEvents, semActors, microSemActors, semRelations);
                System.out.println("events after co-participation = " + coparticipationEvents.size());
                System.out.println("actors after co-participation = " + coparticipationActors.size());
                ArrayList<SemObject> fnBridgingEvents = new ArrayList<SemObject>();
                if (frameNetReader.subToSuperFrame.size()>0) {
                    fnBridgingEvents = CreateMicrostory.getEventsThroughFrameNetBridging(semEvents, microSemEvents, frameNetReader);
                    System.out.println("events after fn bridging = " + fnBridgingEvents.size());
                }

                ArrayList<SemObject> eventRelationEvents = CreateMicrostory.getEventsThroughNafEventRelations(semEvents, microSemEvents, kafSaxParser);
                System.out.println("events after bridging through NAF event relations = " + eventRelationEvents.size());

                semEvents = microSemEvents;
                semActors = microSemActors;


                for (int i = 0; i < coparticipationEvents.size(); i++) {
                    SemObject semEvent = coparticipationEvents.get(i);
                    if (!Util.hasObjectUri(semEvents, semEvent.getURI())) {
                        semEvents.add(semEvent);
                    }
                }
                for (int i = 0; i < fnBridgingEvents.size(); i++) {
                    SemObject semEvent = fnBridgingEvents.get(i);
                    if (!Util.hasObjectUri(semEvents, semEvent.getURI())) {
                        semEvents.add(semEvent);
                    }
                }
                for (int i = 0; i < eventRelationEvents.size(); i++) {
                    SemObject semEvent = eventRelationEvents.get(i);
                    if (!Util.hasObjectUri(semEvents, semEvent.getURI())) {
                        semEvents.add(semEvent);
                    }
                }

                for (int i = 0; i < coparticipationActors.size(); i++) {
                    SemObject semObject = coparticipationActors.get(i);
                    if (!Util.hasObjectUri(semActors, semObject.getURI())) {
                        semActors.add(semObject);
                    }
                }
            }
            else {
                semEvents = microSemEvents;
                semActors = microSemActors;
            }


            System.out.println("final microstory semEvents = " + semEvents.size());
            System.out.println("final microstory semActors = " + semActors.size());
/*            for (int i = 0; i < semEvents.size(); i++) {
                SemObject semObject = semEvents.get(i);
                System.out.println("Final semEvent.getURI() = " + semObject.getURI());
            }*/
        }
        else if (ADDITIONALROLES) {

            // if cross-lingual useEnglishExternalReferences
            GetSemFromNafFile.processNafFileWithAdditionalRoles(project, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations);
        }
        else {
            GetSemFromNafFile.processNafFile(project, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations);
        }

        //// create the perspective output here...
        //
        //
        //
        /*GetPerspectiveRelations.getPerspective (kafSaxParser,
                project ,   perspectives,
                semActors,
                contextualVector,
                communicationVector,
                grammaticalVector);
*/

        // We need to create output objects that are more informative than the Trig output and store these in files per date
        //System.out.println("semTimes = " + semTimes.size());
        for (int j = 0; j < semEvents.size(); j++) {
            SemEvent mySemEvent = (SemEvent) semEvents.get(j);
            ArrayList<SemTime> myTimes = Util.castToTime(ComponentMatch.getMySemObjects(mySemEvent, semRelations, semTimes));
            //   System.out.println("myTimes.size() = " + myTimes.size());
            ArrayList<SemPlace> myPlaces = Util.castToPlace(ComponentMatch.getMySemObjects(mySemEvent, semRelations, semPlaces));
            ArrayList<SemActor> myActors = Util.castToActor(ComponentMatch.getMySemObjects(mySemEvent, semRelations, semActors));
            ArrayList<SemRelation> myRelations = ComponentMatch.getMySemRelations(mySemEvent, semRelations);
            if (myRelations.size() == 0) {
                continue;
            }
            ArrayList<SemRelation> myFacts = ComponentMatch.getMySemRelations(mySemEvent, factRelations);
            CompositeEvent compositeEvent = new CompositeEvent(mySemEvent, myActors, myPlaces, myTimes, myRelations, myFacts);
            File folder = otherFolder;
            String eventType = FrameTypes.getEventTypeString(mySemEvent.getConcepts(), contextualVector, communicationVector, grammaticalVector);
            if (!eventType.isEmpty()) {
                if (eventType.equalsIgnoreCase("source")) {
                    folder = speechFolder;
                } else if (eventType.equalsIgnoreCase("grammatical")) {
                    folder = grammaticalFolder;
                }
            }
            File timeFile = null;

            ArrayList<SemTime> outputTimes = myTimes;
            // eventFos.writeObject(compositeEvent);
            /// now we need to write the event data and relations to the proper time folder for comparison
/*                if (outputTimes.size() == 0) {
                    /// we use the doc times as fall back;
                    outputTimes = compositeEvent.getMyDocTimes();
                }*/
            TreeSet<String> treeSet = new TreeSet<String>();

            if (outputTimes.size() == 0) {
                /// timeless
                String timePhrase = "timeless";
                treeSet.add(timePhrase);
            } else if (outputTimes.size() == 1) {
                /// time: same year or exact?
                SemTime myTime = outputTimes.get(0);
                String timePhrase = myTime.getOwlTime().toString();
                treeSet.add(timePhrase);
            } else if (outputTimes.size() <= TIMEEXPRESSIONMAX) {
                /// special case if multiple times, what to do? create a period?
                //// ?????

                //// we now duplicate the event to multiple time folders
                String timePhrase = "";
                for (int k = 0; k < outputTimes.size(); k++) {
                    SemTime semTime = (SemTime) outputTimes.get(k);
                    timePhrase = semTime.getOwlTime().toString();
                    if (!treeSet.contains(timePhrase)) {
                        treeSet.add(timePhrase);
                    }
                }
            }
            if (treeSet.size()>0) {
                Iterator keys = treeSet.iterator();
                while (keys.hasNext()) {
                    String timePhrase = "-" + keys.next();
                    timeFile = new File(folder.getAbsolutePath() + "/" + "e" + timePhrase);
                    if (!timeFile.exists()) {
                        timeFile.mkdir();
                    }
                    if (timeFile.exists()) {
                        //    System.out.println("appending to timeFile.getName() = " + timeFile.getName());
                        File randomFile = null;
                        if (!nafFileName.isEmpty()) {
                            randomFile = new File(timeFile.getAbsolutePath() + "/" + nafFileName + ".obj");
                        }
                        else {
                            randomFile = File.createTempFile("event", ".obj", timeFile);
                        }
                        if (randomFile!=null && randomFile.exists()) {
                            //    System.out.println("appending to timeFile.getName() = " + timeFile.getName());
                            OutputStream os = new FileOutputStream(randomFile, true);
                            Util.AppendableObjectOutputStream eventFos = new Util.AppendableObjectOutputStream(os);
                            try {
                                eventFos.writeObject(compositeEvent);
                            } catch (IOException e) {
                                //e.printStackTrace();
                            }
                            os.flush();
                            os.close();
                            eventFos.flush();
                            eventFos.close();
                        } else {
                            //  System.out.println("timeFile.getName() = " + timeFile.getName());
                            OutputStream os = new FileOutputStream(randomFile);
                            ObjectOutputStream eventFos = new ObjectOutputStream(os);
                            try {
                                eventFos.writeObject(compositeEvent);
                            } catch (IOException e) {
                                // e.printStackTrace();
                            }
                            os.flush();
                            os.close();
                            eventFos.flush();
                            eventFos.close();
                        }
                    }
                }

            } else {
                //   System.out.println("timeFile = " + timeFile);
            }
        }
    }

    /**
     * @Deprecated
     * @param nafFileName
     * @param project
     * @param kafSaxParser
     * @param speechFolder
     * @param otherFolder
     * @param grammaticalFolder
     * @param semEvents
     * @param semActors
     * @param semPlaces
     * @param semTimes
     * @param semRelations
     * @param factRelations
     * @throws IOException
     */
    static void processKafSaxParserOutputFolderOrg(String nafFileName, String project, KafSaxParser kafSaxParser,
                                    File speechFolder, File otherFolder, File grammaticalFolder,
                                    ArrayList<SemObject> semEvents ,
                                    ArrayList<SemObject> semActors,
                                    ArrayList<SemObject> semPlaces,
                                    ArrayList<SemObject> semTimes,
                                    ArrayList<SemRelation> semRelations,
                                    ArrayList<SemRelation> factRelations
    ) throws IOException {

        if (MICROSTORIES) {
            GetSemFromNafFile.processNafFileWithAdditionalRoles(project, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations);

            System.out.println("all semEvents.size() = " + semEvents.size());
            System.out.println("all semActors.size() = " + semActors.size());
            ArrayList<SemObject> microSemEvents = CreateMicrostory.getMicroEvents(SENTENCERANGE, semEvents);
            ArrayList<SemObject> microSemActors = CreateMicrostory.getMicroActors(SENTENCERANGE, semActors);

            System.out.println("microSemEvents (sentence range:"+SENTENCERANGE+ ") = " + microSemEvents.size());
            System.out.println("microSemActors (sentence range:"+SENTENCERANGE+ ") = " + microSemActors.size());

/*            for (int i = 0; i < microSemEvents.size(); i++) {
                SemObject semObject = microSemEvents.get(i);
                System.out.println("micro semEvent.getURI() = " + semObject.getURI());
            }*/
            if (BRIDGING) {
                ArrayList<SemObject> coparticipationEvents = CreateMicrostory.getEventsThroughCoparticipation(semEvents, microSemEvents, microSemActors, semRelations);
                ArrayList<SemObject> coparticipationActors = CreateMicrostory.getActorsThroughCoparticipation(microSemEvents, semActors, microSemActors, semRelations);
                System.out.println("events after co-participation = " + coparticipationEvents.size());
                System.out.println("actors after co-participation = " + coparticipationActors.size());
                ArrayList<SemObject> fnBridgingEvents = new ArrayList<SemObject>();
                if (frameNetReader.subToSuperFrame.size()>0) {
                    fnBridgingEvents = CreateMicrostory.getEventsThroughFrameNetBridging(semEvents, microSemEvents, frameNetReader);
                    System.out.println("events after fn bridging = " + fnBridgingEvents.size());
                }

                ArrayList<SemObject> eventRelationEvents = CreateMicrostory.getEventsThroughNafEventRelations(semEvents, microSemEvents, kafSaxParser);
                System.out.println("events after bridging through NAF event relations = " + eventRelationEvents.size());

                semEvents = microSemEvents;
                semActors = microSemActors;


                for (int i = 0; i < coparticipationEvents.size(); i++) {
                    SemObject semEvent = coparticipationEvents.get(i);
                    if (!Util.hasObjectUri(semEvents, semEvent.getURI())) {
                        semEvents.add(semEvent);
                    }
                }
                for (int i = 0; i < fnBridgingEvents.size(); i++) {
                    SemObject semEvent = fnBridgingEvents.get(i);
                    if (!Util.hasObjectUri(semEvents, semEvent.getURI())) {
                        semEvents.add(semEvent);
                    }
                }
                for (int i = 0; i < eventRelationEvents.size(); i++) {
                    SemObject semEvent = eventRelationEvents.get(i);
                    if (!Util.hasObjectUri(semEvents, semEvent.getURI())) {
                        semEvents.add(semEvent);
                    }
                }

                for (int i = 0; i < coparticipationActors.size(); i++) {
                    SemObject semObject = coparticipationActors.get(i);
                    if (!Util.hasObjectUri(semActors, semObject.getURI())) {
                        semActors.add(semObject);
                    }
                }
            }
            else {
                semEvents = microSemEvents;
                semActors = microSemActors;
            }


            System.out.println("final microstory semEvents = " + semEvents.size());
            System.out.println("final microstory semActors = " + semActors.size());
/*            for (int i = 0; i < semEvents.size(); i++) {
                SemObject semObject = semEvents.get(i);
                System.out.println("Final semEvent.getURI() = " + semObject.getURI());
            }*/
        }
        else if (ADDITIONALROLES) {
            GetSemFromNafFile.processNafFileWithAdditionalRoles(project, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations);
        }
        else {
                GetSemFromNafFile.processNafFile(project, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations);
        }
        // We need to create output objects that are more informative than the Trig output and store these in files per date
        //System.out.println("semTimes = " + semTimes.size());
        for (int j = 0; j < semEvents.size(); j++) {
            SemEvent mySemEvent = (SemEvent) semEvents.get(j);
            ArrayList<SemTime> myTimes = Util.castToTime(ComponentMatch.getMySemObjects(mySemEvent, semRelations, semTimes));
            //   System.out.println("myTimes.size() = " + myTimes.size());
            ArrayList<SemPlace> myPlaces = Util.castToPlace(ComponentMatch.getMySemObjects(mySemEvent, semRelations, semPlaces));
            ArrayList<SemActor> myActors = Util.castToActor(ComponentMatch.getMySemObjects(mySemEvent, semRelations, semActors));
            ArrayList<SemRelation> myRelations = ComponentMatch.getMySemRelations(mySemEvent, semRelations);
            if (myRelations.size() == 0) {
                continue;
            }
            ArrayList<SemRelation> myFacts = ComponentMatch.getMySemRelations(mySemEvent, factRelations);
            CompositeEvent compositeEvent = new CompositeEvent(mySemEvent, myActors, myPlaces, myTimes, myRelations, myFacts);
            File folder = otherFolder;
            String eventType = FrameTypes.getEventTypeString(mySemEvent.getConcepts(), contextualVector, communicationVector, grammaticalVector);
            if (!eventType.isEmpty()) {
                if (eventType.equalsIgnoreCase("source")) {
                    folder = speechFolder;
                } else if (eventType.equalsIgnoreCase("grammatical")) {
                    folder = grammaticalFolder;
                }
            }
            File timeFile = null;

            ArrayList<SemTime> outputTimes = myTimes;
            // eventFos.writeObject(compositeEvent);
            /// now we need to write the event data and relations to the proper time folder for comparison
/*                if (outputTimes.size() == 0) {
                    /// we use the doc times as fall back;
                    outputTimes = compositeEvent.getMyDocTimes();
                }*/
            if (outputTimes.size() == 0) {
                /// timeless
                timeFile = new File(folder.getAbsolutePath() + "/" + "e-" + "timeless");
            } else if (outputTimes.size() == 1) {
                /// time: same year or exact?
                SemTime myTime = outputTimes.get(0);
                String timePhrase = "-" + myTime.getOwlTime().toString();
                timeFile = new File(folder.getAbsolutePath() + "/" + "e" + timePhrase);
            } else if (outputTimes.size() <= TIMEEXPRESSIONMAX) {
                /// special case if multiple times, what to do? create a period?
                //// ?????
                TreeSet<String> treeSet = new TreeSet<String>();
                String timePhrase = "";
                for (int k = 0; k < outputTimes.size(); k++) {
                    SemTime semTime = (SemTime) outputTimes.get(k);
                    timePhrase = semTime.getOwlTime().toString();
                    if (!treeSet.contains(timePhrase)) {
                        treeSet.add(timePhrase);
                    }
                }
                timePhrase = "";
                Iterator keys = treeSet.iterator();
                while (keys.hasNext()) {
                    timePhrase += "-" + keys.next();
                }
                timeFile = new File(folder.getAbsolutePath() + "/" + "e" + timePhrase);
            }
            if (timeFile != null) {
                if (!timeFile.exists()) {
                    timeFile.mkdir();
                }
                if (timeFile.exists()) {
                    //    System.out.println("appending to timeFile.getName() = " + timeFile.getName());
                    File randomFile = null;
                    if (!nafFileName.isEmpty()) {
                        randomFile = new File(timeFile.getAbsolutePath() + "/" + nafFileName + ".obj");
                    }
                    else {
                        randomFile = File.createTempFile("event", ".obj", timeFile);
                    }
                    if (randomFile!=null && randomFile.exists()) {
                        //    System.out.println("appending to timeFile.getName() = " + timeFile.getName());
                        OutputStream os = new FileOutputStream(randomFile, true);
                        Util.AppendableObjectOutputStream eventFos = new Util.AppendableObjectOutputStream(os);
                        try {
                            eventFos.writeObject(compositeEvent);
                        } catch (IOException e) {
                            //e.printStackTrace();
                        }
                        os.flush();
                        os.close();
                        eventFos.flush();
                        eventFos.close();
                    } else {
                        //  System.out.println("timeFile.getName() = " + timeFile.getName());
                        OutputStream os = new FileOutputStream(randomFile);
                        ObjectOutputStream eventFos = new ObjectOutputStream(os);
                        try {
                            eventFos.writeObject(compositeEvent);
                        } catch (IOException e) {
                            // e.printStackTrace();
                        }
                        os.flush();
                        os.close();
                        eventFos.flush();
                        eventFos.close();
                    }
                }
            } else {
                //   System.out.println("timeFile = " + timeFile);
            }
        }
    }


}