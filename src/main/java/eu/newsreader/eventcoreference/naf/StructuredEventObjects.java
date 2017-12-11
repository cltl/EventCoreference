package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.newsreader.eventcoreference.coref.ComponentMatch;
import eu.newsreader.eventcoreference.input.FrameNetReader;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Piek
 * Date: 11/14/13
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class StructuredEventObjects {



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
            "--local-context                 <Dark entities and non-entities have local context semantics>\n" +
            "--non-entities                  <If used, additional FrameNet roles and non-entity phrases are included>\n" +
            "--contextual-frames    <path>   <Path to a file with the FrameNet frames considered contextual>\n" +
            "--communication-frames <path>   <Path to a file with the FrameNet frames considered source>\n" +
            "--grammatical-frames   <path>   <Path to a file with the FrameNet frames considered grammatical>\n" +
            "--frame-level          <integer><@DEPRECATED Depth of path for the FrameNet relations>\n" +
            "--frame-relations      <path>   <@DEPRECATED path to FrameNet file with relations>\n" +
            "--microstories         <integer><@DEPRECATED Number of sentences to restrict the analysis>\n" +
            "--bridging                      <@DEPRECATED Whether or not microstories are extended through bridging relations>\n";
    static public FrameNetReader frameNetReader = new FrameNetReader();
    static public boolean MICROSTORIES = false;
    static public Integer SENTENCERANGE = 0;
    static public boolean BRIDGING = false;
    static public boolean FIXCOREF = false;
    static public String done = "";
    static public boolean RAWTEXTINDEX = false;
    static NafSemParameters nafSemParameters = new NafSemParameters();


    static public void main(String[] args) {
        nafSemParameters = new NafSemParameters(args);
        if (args.length == 0) {
            System.out.println(USAGE);
            System.out.println("NOW RUNNING WITH DEFAULT SETTINGS");
            //  return;
        }
        String pathToNafFolder = "";
        String pathToEventFolder = "";
        String extension = "";
        String fnFile = "";
        int fnLevel = 0;


        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--naf-folder") && args.length > (i + 1)) {
                pathToNafFolder = args[i + 1];
            } else if (arg.equals("--event-folder") && args.length > (i + 1)) {
                pathToEventFolder = args[i + 1];
            } else if (arg.equals("--extension") && args.length > (i + 1)) {
                extension = args[i + 1];
            } else if (arg.equals("--frame-relations") && args.length > (i + 1)) {
                fnFile = args[i + 1];
            } else if (arg.equals("--frame-level") && args.length > (i + 1)) {
                try {
                    fnLevel = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else if (arg.equals("--rename") && args.length > (i + 1)) {
                done = args[i + 1];
            }
        }

        if (!fnFile.isEmpty()) {
            frameNetReader.parseFile(fnFile);
            frameNetReader.flatRelations(fnLevel);
            System.out.println("frameNetReader sub= " + frameNetReader.subToSuperFrame.size());
            System.out.println("frameNetReader super= " + frameNetReader.superToSubFrame.size());
        }
        //// read resources
/*
        System.out.println("sourceVector = " + sourceVector.size());
        System.out.println("contextualVector = " + contextualVector.size());
        System.out.println("grammaticalVector = " + grammaticalVector.size());
*/

        try {
            processFolderEvents(nafSemParameters.getPROJECT(), new File(pathToNafFolder), new File(pathToEventFolder), extension);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void processFolderEvents(String project, File pathToNafFolder, File eventParentFolder, String extension

    ) throws IOException {
        File eventFolder = new File(eventParentFolder + "/events");
        if (!eventFolder.exists()) {
            eventFolder.mkdir();
        }
        if (!eventFolder.exists()) {
            System.out.println("Cannot create the eventFolder = " + eventFolder);
            return;
        }

        KafSaxParser kafSaxParser = new KafSaxParser();


        ArrayList<File> files = Util.makeRecursiveFileList(pathToNafFolder, extension);

        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);

            if (i % 500 == 0) {
                // System.out.println("i = " + i);
                //  System.out.println("file.getName() = " + file.getAbsolutePath());
            }

            if (!file.getAbsolutePath().toLowerCase().endsWith(".gz")) {
                kafSaxParser.parseFile(file.getAbsolutePath());
            } else {
                InputStream fileStream = new FileInputStream(file.getAbsolutePath());
                InputStream gzipStream = new GZIPInputStream(fileStream);
                kafSaxParser.parseFile(gzipStream);
            }


            if (kafSaxParser.getKafMetaData().getUrl().isEmpty()) {
                System.out.println("file.getName() = " + file.getName());
                kafSaxParser.getKafMetaData().setUrl(file.getName());
                kafSaxParser.fileName = file.getName();
                System.out.println("WARNING! Replacing empty url in header NAF with the file name!");
            }


            /// We create subfolders on the basis of the incident identifier used for naming the files.
            /// Files are named as: 3-59589-0a3e49ad0467c6545e36d754cc08d312.naf
            /// task = 3
            /// incident = 59589
            
            String incidentId = file.getName();
            int idx = incidentId.indexOf("---");
            if (idx > -1) {
                incidentId = incidentId.substring(0, idx);
            }
            File incidentFolder = new File(eventFolder + "/" + incidentId);

            if (!incidentFolder.exists()) {
                incidentFolder.mkdir();
            }
            if (!incidentFolder.exists()) {
                System.out.println("Cannot create the incidentFolder = " + incidentFolder);
                return;
            }
            //  System.out.println("kafSaxParser.getKafMetaData().getUrl() = " + kafSaxParser.getKafMetaData().getUrl());
            processKafSaxParserOutputFolder(file.getAbsolutePath(),
                    kafSaxParser, incidentFolder);
            if (!done.isEmpty()) {
                File doneFile = new File(file.getAbsolutePath() + done);
                file.renameTo(doneFile);
            }
        }

    }

    static void processKafSaxParserOutputFolder(String nafFilePath,
                                                KafSaxParser kafSaxParser,
                                                File allFolder)
            throws IOException {
        String nafFileName = new File(nafFilePath).getName();
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemTime> semTimes = new ArrayList<SemTime>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        // System.out.println("nafFileName = " + nafFileName);
        GetSemFromNaf.processNafFile(nafSemParameters, kafSaxParser, semEvents, semActors, semTimes, semRelations);

        // We need to create output objects that are more informative than the Trig output and store these in files per date
        //System.out.println("semTimes = " + semTimes.size());
        for (int j = 0; j < semEvents.size(); j++) {
            SemEvent mySemEvent = (SemEvent) semEvents.get(j);
            ArrayList<SemTime> myTimes = ComponentMatch.getMySemTimes(mySemEvent, semRelations, semTimes);
            ArrayList<SemActor> myActors = ComponentMatch.getMySemActors(mySemEvent, semRelations, semActors);
            ArrayList<SemRelation> myRelations = ComponentMatch.getMySemRelations(mySemEvent, semRelations);
            CompositeEvent compositeEvent = new CompositeEvent(mySemEvent, myActors, myTimes, myRelations);
            if (!compositeEvent.isValid() && !nafSemParameters.isALL()) {
                continue;
            }
            File randomFile = null;
            if (!nafFileName.isEmpty()) {
                randomFile = new File(allFolder.getAbsolutePath() + "/" + nafFileName + ".obj");
            } else {
                randomFile = File.createTempFile("event", ".obj", allFolder);
            }
            ////////// NOTE /////////////////////////////
            ////////// To write to object file, all the classes need to be defined with "implements Serializable {"
            ////////// If not you get a wirte object error for that class:
            ////// java.io.NotSerializableException: eu.kyotoproject.kaf.KafFactValue
            //////  at java.io.ObjectOutputStream.writeObject0(ObjectOutputStream.java:1165)
            //////  at java.io.ObjectOutputStream.writeObject(ObjectOutputStream.java:329)

            if (randomFile != null && randomFile.exists()) {
                //  System.out.println("appending to timeFile.getName() = " + timeFile.getName());
                OutputStream os = new FileOutputStream(randomFile, true);
                Util.AppendableObjectOutputStream eventFos = new Util.AppendableObjectOutputStream(os);
                try {
                    eventFos.writeObject(compositeEvent);
                } catch (IOException e) {
                    //  e.printStackTrace();
                }
                os.flush();
                os.close();
                eventFos.flush();
                eventFos.close();
            } else if (randomFile != null) {
                //   System.out.println("timeFile.getName() = " + timeFile.getName());
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
            } else {
            }
        }
    }

}
