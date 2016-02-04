package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.newsreader.eventcoreference.coref.ComponentMatch;
import eu.newsreader.eventcoreference.input.FrameNetReader;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Piek
 * Date: 11/14/13
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class NoClusterEventObjects {



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
            "--frame-level          <integer><@DEPRECATED Depth of path for the FrameNet relations>\n" +
            "--frame-relations      <path>   <@DEPRECATED path to FrameNet file with relations>\n" +
            "--microstories         <integer><@DEPRECATED Number of sentences to restrict the analysis>\n" +
            "--bridging                      <@DEPRECATED Whether or not microstories are extended through bridging relations>\n";
    static public Vector<String> sourceVector = null;
    static public Vector<String> grammaticalVector = null;
    static public Vector<String> contextualVector = null;
    static public FrameNetReader frameNetReader = new FrameNetReader();
    static public final int TIMEEXPRESSIONMAX = 5;
    static public boolean MICROSTORIES = false;
    static public Integer SENTENCERANGE = 0;
    static public boolean BRIDGING = false;
    static public boolean FIXCOREF = false;
    static public String done = "";
    static public boolean ADDITIONALROLES = false;
    static public boolean PERSPECTIVE = false;
    static public boolean ALL = false;
    static public boolean RAWTEXTINDEX = false;


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
        String sourceFrameFile = "";
        String contextualFrameFile = "";
        String grammaticalFrameFile = "";
        String fnFile = "";
        int fnLevel = 0;
/*
        extension = ".naf";
        sourceFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-eventcoreference_v2_2014/resources/communication.txt";
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
            else if (arg.equals("--fix-coref")) {
                FIXCOREF = true;
            }
            else if (arg.equals("--perspective")) {
                PERSPECTIVE = true;
            }
            else if (arg.equals("--all")) {
                ALL = true;
            }
            else if (arg.equals("--raw-text")) {
                RAWTEXTINDEX = true;
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
            else if (arg.equals("--source-frames") && args.length>(i+1)) {
                sourceFrameFile = args[i+1];
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
            else if (arg.equals("--eurovoc-en") && args.length > (i + 1)) {
                String pathToEurovocFile = args[i+1];
                GetSemFromNaf.initEurovoc(pathToEurovocFile, "en");
            }
            else if (arg.equals("--eurovoc-nl") && args.length > (i + 1)) {
                String pathToEurovocFile = args[i+1];
                GetSemFromNaf.initEurovoc(pathToEurovocFile, "nl");
            }
            else if (arg.equals("--eurovoc-es") && args.length > (i + 1)) {
                String pathToEurovocFile = args[i+1];
                GetSemFromNaf.initEurovoc(pathToEurovocFile, "es");
            }
            else if (arg.equals("--eurovoc-it") && args.length > (i + 1)) {
                String pathToEurovocFile = args[i+1];
                GetSemFromNaf.initEurovoc(pathToEurovocFile, "it");
            }
        }

        if (!fnFile.isEmpty()) {
            frameNetReader.parseFile(fnFile);
            frameNetReader.flatRelations(fnLevel);
            System.out.println("frameNetReader sub= " + frameNetReader.subToSuperFrame.size());
            System.out.println("frameNetReader super= " + frameNetReader.superToSubFrame.size());
        }
        //// read resources
        sourceVector = Util.ReadFileToStringVector(sourceFrameFile);
        grammaticalVector = Util.ReadFileToStringVector(grammaticalFrameFile);
        contextualVector = Util.ReadFileToStringVector(contextualFrameFile);
/*
        System.out.println("sourceVector = " + sourceVector.size());
        System.out.println("contextualVector = " + contextualVector.size());
        System.out.println("grammaticalVector = " + grammaticalVector.size());
*/

        try {
            processFolderEvents(projectName, new File(pathToNafFolder), new File(pathToEventFolder), extension);
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
        File allFolder = new File(eventFolder + "/" + "all");
        if (!allFolder.exists()) {
            allFolder.mkdir();
        }
        if (!allFolder.exists()) {
            System.out.println("Cannot create the allFolder = " + allFolder);
            return;
        }

        KafSaxParser kafSaxParser = new KafSaxParser();



        ArrayList<File> files = Util.makeRecursiveFileList(pathToNafFolder, extension);

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


          //  System.out.println("file.getName() = " + file.getName());
            if (!file.getAbsolutePath().toLowerCase().endsWith(".gz")) {
                kafSaxParser.parseFile(file.getAbsolutePath());
            }
            else {
                InputStream fileStream = new FileInputStream(file.getAbsolutePath());
                InputStream gzipStream = new GZIPInputStream(fileStream);
                kafSaxParser.parseFile(gzipStream);
            }


            if (kafSaxParser.getKafMetaData().getUrl().isEmpty()) {
                System.out.println("file.getName() = " + file.getName());
                kafSaxParser.getKafMetaData().setUrl(file.getName());
                System.out.println("WARNING! Replacing empty url in header NAF with the file name!");
            }
            if (FIXCOREF) {
                //// NEED A FIX
                Util.fixSourceEventCoreferenceSets(kafSaxParser, contextualVector, sourceVector, grammaticalVector);
            }

          //  System.out.println("kafSaxParser.getKafMetaData().getUrl() = " + kafSaxParser.getKafMetaData().getUrl());
            processKafSaxParserOutputFolder(file.getAbsolutePath(), project,
                    kafSaxParser, allFolder);
            if (!done.isEmpty()) {
                File doneFile = new File(file.getAbsolutePath() + done);
                file.renameTo(doneFile);
            }



        }

    }

    static void processKafSaxParserOutputFolder(String nafFilePath,
                                                String project,
                                                KafSaxParser kafSaxParser,
                                                File allFolder)
            throws IOException {
        String nafFileName = new File (nafFilePath).getName();
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemTime> semTimes = new ArrayList<SemTime>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
      //  System.out.println("nafFileName = " + nafFileName);
        GetSemFromNaf.processNafFile(project, kafSaxParser, semEvents, semActors, semTimes, semRelations, ADDITIONALROLES);


        // We need to create output objects that are more informative than the Trig output and store these in files per date
        //System.out.println("semTimes = " + semTimes.size());
        for (int j = 0; j < semEvents.size(); j++) {
            SemEvent mySemEvent = (SemEvent) semEvents.get(j);
            ArrayList<SemTime> myTimes = ComponentMatch.getMySemTimes(mySemEvent, semRelations, semTimes);
            //   System.out.println("myTimes.size() = " + myTimes.size());
            ArrayList<SemActor> myActors =ComponentMatch.getMySemActors(mySemEvent, semRelations, semActors);
            ArrayList<SemRelation> myRelations = ComponentMatch.getMySemRelations(mySemEvent, semRelations);
           // ArrayList<SemRelation> myFacts = ComponentMatch.getMySemRelations(mySemEvent, factRelations);
            CompositeEvent compositeEvent = new CompositeEvent(mySemEvent, myActors, myTimes, myRelations);
            if (!compositeEvent.isValid() && !ALL) {
                continue;
            }
           // System.out.println("lcs:"+compositeEvent.getEvent().getLcs().toString());
          //  System.out.println("hypers:"+compositeEvent.getEvent().getHypers().toString());

            //    System.out.println("appending to timeFile.getName() = " + timeFile.getName());
            File randomFile = null;
            if (!nafFileName.isEmpty()) {
                randomFile = new File(allFolder.getAbsolutePath() + "/" + nafFileName + ".obj");
            }
            else {
                randomFile = File.createTempFile("event", ".obj", allFolder);
            }
            ////////// NOTE /////////////////////////////
            ////////// To write to object file, all the classes need to be defined with "implements Serializable {"
            ////////// If not you get a wirte object error for that class:
            ////// java.io.NotSerializableException: eu.kyotoproject.kaf.KafFactValue
            //////  at java.io.ObjectOutputStream.writeObject0(ObjectOutputStream.java:1165)
            //////  at java.io.ObjectOutputStream.writeObject(ObjectOutputStream.java:329)

            if (randomFile!=null && randomFile.exists()) {
                 //  System.out.println("appending to timeFile.getName() = " + timeFile.getName());
                OutputStream os = new FileOutputStream(randomFile, true);
                Util.AppendableObjectOutputStream eventFos = new Util.AppendableObjectOutputStream(os);
                try {
                    eventFos.writeObject(compositeEvent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                os.flush();
                os.close();
                eventFos.flush();
                eventFos.close();
            }
            else if (randomFile!=null) {
               //   System.out.println("timeFile.getName() = " + timeFile.getName());
                OutputStream os = new FileOutputStream(randomFile);
                ObjectOutputStream eventFos = new ObjectOutputStream(os);
                try {
                    eventFos.writeObject(compositeEvent);
                } catch (IOException e) {
                     e.printStackTrace();
                }
                os.flush();
                os.close();
                eventFos.flush();
                eventFos.close();
            }
            else {
            }
        }

        if (RAWTEXTINDEX) {
            String rawTextIndexFilePath = allFolder.getParent()+"/"+"rawtext.idx";

            String rawText = kafSaxParser.rawText;
           // System.out.println("rawText = " + rawText);
            rawText = rawText.replaceAll("\n", "_");
            rawText = rawText.replaceAll("\"", "^");

            String uri = kafSaxParser.getKafMetaData().getUrl();
            if (!uri.toLowerCase().startsWith("http")) {
              //  System.out.println("uri = " + uri);
                uri = ResourcesUri.nwrdata  + uri.substring(uri.indexOf("/")+1);
              //  System.out.println("uri = " + uri);
            }

            String str = uri+"\t"+rawText+"\n";
            File rawTextIndexFile = new File(rawTextIndexFilePath);
            OutputStream os = null;
            if (rawTextIndexFile!=null && rawTextIndexFile.exists()) {
                os = new FileOutputStream(rawTextIndexFile, true);
            }
            else if (rawTextIndexFile!=null) {
                os = new FileOutputStream(rawTextIndexFile);
            }
            try {
                os.write(str.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            os.flush();
            os.close();
        }

        if (PERSPECTIVE) {
            ArrayList<PerspectiveObject> perspectiveObjects = new ArrayList<PerspectiveObject>();

            perspectiveObjects = GetPerspectiveRelations.getSourcePerspectives(kafSaxParser,
                    project,
                    semActors,
                    contextualVector,
                    sourceVector,
                    grammaticalVector);

            if (perspectiveObjects.size() > 0) {
                String perspectiveFilePath = nafFilePath + ".perspective.trig";
                GetPerspectiveRelations.perspectiveRelationsToTrig(perspectiveFilePath, perspectiveObjects);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// OLD CODE BELOW //////////////////////////////////////////////////
    /////////////////////////////////// OLD CODE BELOW //////////////////////////////////////////////////
    /////////////////////////////////// OLD CODE BELOW //////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////

/*    public static void processFolderEventsORG (String project, File pathToNafFolder, File eventParentFolder, String extension

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
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        ArrayList<PerspectiveObject> perspectiveObjects = new ArrayList<PerspectiveObject>();

        ArrayList<File> files = Util.makeRecursiveFileList(pathToNafFolder, extension);

        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
           // System.out.println("file.getName() = " + file.getName());
*//*            if (!file.getName().startsWith("7YXG-CS51-2RYC-J2CN.xml")) {
                     continue;
            }*//*

            if (i % 500 == 0) {
               // System.out.println("i = " + i);
                //  System.out.println("file.getName() = " + file.getAbsolutePath());
            }

            semEvents = new ArrayList<SemObject>();
            semActors = new ArrayList<SemObject>();
            semTimes = new ArrayList<SemObject>();
            semRelations = new ArrayList<SemRelation>();
            perspectiveObjects = new ArrayList<PerspectiveObject>();

          //  System.out.println("file.getName() = " + file.getName());
            kafSaxParser.parseFile(file.getAbsolutePath());
            if (kafSaxParser.getKafMetaData().getUrl().isEmpty()) {
                System.out.println("file.getName() = " + file.getName());
                System.out.println("SKIPPING NAF due to empty url in header kafSaxParser.getKafMetaData().getUrl() = " + kafSaxParser.getKafMetaData().getUrl());
            }
            else {
              //  System.out.println("kafSaxParser.getKafMetaData().getUrl() = " + kafSaxParser.getKafMetaData().getUrl());
                processKafSaxParserOutputFolderORG(file.getName(), project,
                        kafSaxParser, perspectiveObjects, speechFolder, otherFolder, grammaticalFolder,
                        semEvents, semActors, semTimes, semRelations);
                if (!done.isEmpty()) {
                    File doneFile = new File(file.getAbsolutePath() + done);
                    file.renameTo(doneFile);
                }
            }

            if (perspectiveObjects.size()>0) {
                String perspectiveFilePath = file.getAbsolutePath()+".perspective.trig";
                GetPerspectiveRelations.perspectiveRelationsToTrig(perspectiveFilePath, perspectiveObjects);
            }
        }

    }




    static void processKafSaxParserOutputFolderORG(String nafFileName, String project, KafSaxParser kafSaxParser, ArrayList<PerspectiveObject> perspectives,
                                    File speechFolder, File otherFolder, File grammaticalFolder,
                                    ArrayList<SemObject> semEvents ,
                                    ArrayList<SemObject> semActors,
                                    ArrayList<SemObject> semTimes,
                                    ArrayList<SemRelation> semRelations
    ) throws IOException {

        if (MICROSTORIES) {
            GetSemFromNafFile.processNafFile(project, kafSaxParser, semEvents, semActors, semTimes, semRelations, ADDITIONALROLES);

            System.out.println("all semEvents.size() = " + semEvents.size());
            System.out.println("all semActors.size() = " + semActors.size());
            ArrayList<SemObject> microSemEvents = CreateMicrostory.getMicroEvents(SENTENCERANGE, semEvents);
            ArrayList<SemObject> microSemActors = CreateMicrostory.getMicroActors(SENTENCERANGE, semActors);

            System.out.println("microSemEvents (sentence range:"+SENTENCERANGE+ ") = " + microSemEvents.size());
            System.out.println("microSemActors (sentence range:"+SENTENCERANGE+ ") = " + microSemActors.size());

*//*            for (int i = 0; i < microSemEvents.size(); i++) {
                SemObject semObject = microSemEvents.get(i);
                System.out.println("micro semEvent.getURI() = " + semObject.getURI());
            }*//*
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
*//*            for (int i = 0; i < semEvents.size(); i++) {
                SemObject semObject = semEvents.get(i);
                System.out.println("Final semEvent.getURI() = " + semObject.getURI());
            }*//*
        }

        GetSemFromNafFile.processNafFile(project, kafSaxParser, semEvents, semActors, semTimes, semRelations, ADDITIONALROLES);

        perspectives = GetPerspectiveRelations.getPerspective(kafSaxParser,
                project,
                semActors,
                contextualVector,
                sourceVector,
                grammaticalVector);



        // We need to create output objects that are more informative than the Trig output and store these in files per date
        //System.out.println("semTimes = " + semTimes.size());
        for (int j = 0; j < semEvents.size(); j++) {
            SemEvent mySemEvent = (SemEvent) semEvents.get(j);
            ArrayList<SemTime> myTimes = Util.castToTime(ComponentMatch.getMySemObjects(mySemEvent, semRelations, semTimes));
            //   System.out.println("myTimes.size() = " + myTimes.size());
            ArrayList<SemActor> myActors = Util.castToActor(ComponentMatch.getMySemObjects(mySemEvent, semRelations, semActors));
            ArrayList<SemRelation> myRelations = ComponentMatch.getMySemRelations(mySemEvent, semRelations);
            if (myRelations.size() == 0) {
                continue;
            }
            CompositeEvent compositeEvent = new CompositeEvent(mySemEvent, myActors, myTimes, myRelations);
            File folder = otherFolder;
            String eventType = FrameTypes.getEventTypeString(mySemEvent.getConcepts(), contextualVector, sourceVector, grammaticalVector);
            if (!eventType.isEmpty()) {
                if (eventType.equalsIgnoreCase("source")) {
                    folder = speechFolder;
                } else if (eventType.equalsIgnoreCase("grammatical")) {
                    folder = grammaticalFolder;
                }
            }
            File timeFile = null;

            ArrayList<SemTime> outputTimes = myTimes;
            /// now we need to write the event data and relations to the proper time folder for comparison
            TreeSet<String> treeSet = new TreeSet<String>();

            if (outputTimes.size() == 0) {
                    String timePhrase = "timeless";
                    treeSet.add(timePhrase);
            }
            *//*else if (outputTimes.size() == 1) {
                /// time: same year or exact?
                SemTime myTime = outputTimes.get(0);
                String timePhrase = myTime.getOwlTime().toString();
                treeSet.add(timePhrase);
            }*//*
            else if (outputTimes.size() <= TIMEEXPRESSIONMAX) {
                /// special case if multiple times, what to do? create a period?
                //// ?????

                String timePhrase = "";
                /// we first create the periods
                ArrayList<String> beginPoints = new ArrayList<String>();
                ArrayList<String> endPoints = new ArrayList<String>();
                for (int i = 0; i < myRelations.size(); i++) {
                    SemRelation semRelation = myRelations.get(i);
                    for (int k = 0; k < semRelation.getPredicates().size(); k++) {
                        String predicate = semRelation.getPredicates().get(k);
                        if (predicate.toLowerCase().endsWith("begintime")) {
                             beginPoints.add(semRelation.getObject());
                        }
                        else if (predicate.toLowerCase().endsWith("endtime")) {
                            endPoints.add(semRelation.getObject());
                        }
                    }
                }
                System.out.println("beginPoints = " + beginPoints.toString());
                System.out.println("endPoints = " + endPoints.toString());
                /// we first add the beginpoints to the time phrase
                for (int i = 0; i < outputTimes.size(); i++) {
                    SemTime semTime = outputTimes.get(i);
                    if (beginPoints.contains(semTime.getId())) {
                        if (!timePhrase.isEmpty()) timePhrase += "-";
                        timePhrase += semTime.getOwlTime().toString();
                    }
                }
                /// next we add the endpoints to the time phrase
                for (int i = 0; i < outputTimes.size(); i++) {
                    SemTime semTime = outputTimes.get(i);
                    if (beginPoints.contains(semTime.getId())) {
                        if (!timePhrase.isEmpty()) timePhrase += "-";
                        timePhrase += semTime.getOwlTime().toString();
                    }
                }
                if (!timePhrase.isEmpty()) {
                    /// we now have a indication of a period with at least a begin and end point and possibly both
                    if (!treeSet.contains(timePhrase)) {
                        treeSet.add(timePhrase);
                    }
                }
                else {
                    ///// the time info is not considered a period and therefore we create separate time buckets for each timex associated with the event
                    //// we now duplicate the event to multiple time folders
                    for (int k = 0; k < outputTimes.size(); k++) {
                        SemTime semTime = (SemTime) outputTimes.get(k);
                        timePhrase = semTime.getOwlTime().toString();
                        if (!treeSet.contains(timePhrase)) {
                            treeSet.add(timePhrase);
                        }
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
    }*/






}