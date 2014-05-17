package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import eu.newsreader.eventcoreference.coref.ComponentMatch;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/14/13
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClusterEventObjects {

    static public void main (String [] args) {

        String pathToNafFolder = "/Code/vu/newsreader/EventCoreference/LN_football_test_out-tiny";
       // String pathToNafFolder = "/Code/vu/newsreader/EventCoreference/LN_football_test_out";
        String projectName  = "worldcup";
        String extension = ".naf";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--naf-folder") && args.length>(i+1)) {
                pathToNafFolder = args[i+1];
            }
            else if (arg.equals("--extension") && args.length>(i+1)) {
                extension = args[i+1];
            }
            else if (arg.equals("--project") && args.length>(i+1)) {
                projectName = args[i+1];
            }
        }
        try {
            processFolderEvents(projectName, new File(pathToNafFolder), extension);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void processFolderEvents (String project, File pathToNafFolder, String extension

    ) throws IOException {
        File eventFolder = new File(pathToNafFolder + "/events");
        if (!eventFolder.exists()) {
            eventFolder.mkdir();
        }
        File speechFolder = new File(eventFolder + "/" + "speech");
        if (!speechFolder.exists()) {
            speechFolder.mkdir();
        }
        File otherFolder = new File(eventFolder + "/" + "other");
        if (!otherFolder.exists()) {
            otherFolder.mkdir();
        }
        File grammaticalFolder = new File(eventFolder + "/" + "grammatical");
        if (!grammaticalFolder.exists()) {
            grammaticalFolder.mkdir();
        }

        KafSaxParser kafSaxParser = new KafSaxParser();
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        ArrayList<SemRelation> factRelations = new ArrayList<SemRelation>();

        ArrayList<File> files = Util.makeRecursiveFileList(pathToNafFolder, extension);
        //System.out.println("files.size() = " + files.size());
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            if (!file.getName().startsWith("56H9-0MG1-J9XT-P1YN.xml")) {
                //     continue;
            }
            if (!file.getName().startsWith("56VW-T8H1-DXCW-D3F2.")) {
                //     continue;
            }
            if (i % 100 == 0) {
                System.out.println("i = " + i);
                //  System.out.println("file.getName() = " + file.getAbsolutePath());
            }
            semEvents = new ArrayList<SemObject>();
            semActors = new ArrayList<SemObject>();
            semTimes = new ArrayList<SemObject>();
            semPlaces = new ArrayList<SemObject>();
            semRelations = new ArrayList<SemRelation>();
            factRelations = new ArrayList<SemRelation>();
            System.out.println("file.getName() = " + file.getName());
            kafSaxParser.parseFile(file.getAbsolutePath());
            GetSemFromNafFile.processNafFile(project, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations);
            //System.out.println("semTimes = " + semTimes.size());
            // We need to create output objects that are more informative than the Trig output and store these in files per date

            for (int j = 0; j < semEvents.size(); j++) {
                SemEvent mySemEvent = (SemEvent) semEvents.get(j);
                ArrayList<SemTime> myTimes =  Util.castToTime(ComponentMatch.getMySemObjects(mySemEvent, semRelations, semTimes));
                //System.out.println("myTimes.size() = " + myTimes.size());
                ArrayList<SemPlace> myPlaces = Util.castToPlace(ComponentMatch.getMySemObjects(mySemEvent, semRelations, semPlaces));
                ArrayList<SemActor> myActors = Util.castToActor(ComponentMatch.getMySemObjects(mySemEvent, semRelations, semActors));
                ArrayList<SemRelation> myRelations = ComponentMatch.getMySemRelations(mySemEvent, semRelations);
                ArrayList<SemRelation> myFacts = ComponentMatch.getMySemRelations(mySemEvent, factRelations);
                CompositeEvent compositeEvent = new CompositeEvent(mySemEvent, GetSemFromNafFile.docSemTime, myActors, myPlaces, myTimes, myRelations, myFacts);
                File folder = otherFolder;
                for (int k = 0; k < mySemEvent.getConcepts().size(); k++) {
                    KafSense kafSense = mySemEvent.getConcepts().get(k);
                    if (kafSense.getSensecode().equalsIgnoreCase("speech-cognition")) {
                        folder = speechFolder;
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("communication")) {
                        folder = speechFolder;
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("cognition")) {
                        folder = speechFolder;
                        break;
                    } else if (kafSense.getSensecode().equalsIgnoreCase("grammatical")) {
                        folder = grammaticalFolder;
                        break;
                    }
                }
                File timeFile = null;

                ArrayList<SemTime> outputTimes = myTimes;
                // eventFos.writeObject(compositeEvent);
                /// now we need to write the event data and relations to the proper time folder for comparison
                if (outputTimes.size() == 0) {
                    /// we use the doc times as fall back;
                    outputTimes = compositeEvent.getMyDocTimes();
                }
                if (outputTimes.size() == 0) {
                    /// timeless
                        timeFile = new File(folder.getAbsolutePath() + "/" + "events-" + "timeless" + ".obj");
                }
                else if (outputTimes.size() == 1) {
                    /// time: same year or exact?
                    SemTime myTime = (SemTime) outputTimes.get(0);
                    String timePhrase = "-" + myTime.getOwlTime().toString();
                    timeFile = new File(folder.getAbsolutePath() + "/" + "events" + timePhrase + ".obj");
                }
                else {
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
                    timeFile = new File(folder.getAbsolutePath() + "/" + "events" + timePhrase + ".obj");
                }
                if (timeFile != null) {
                    if (timeFile.exists()) {
                        //  System.out.println("appending to timeFile.getName() = " + timeFile.getName());
                        OutputStream os = new FileOutputStream(timeFile, true);
                        Util.AppendableObjectOutputStream eventFos = new Util.AppendableObjectOutputStream(os);
                        try {
                            eventFos.writeObject(compositeEvent);
                        } catch (IOException e) {
                            //e.printStackTrace();
                        }
                        os.close();
                        eventFos.close();
                    } else {
                       // System.out.println("timeFile.getName() = " + timeFile.getName());
                        OutputStream os = new FileOutputStream(timeFile);
                        ObjectOutputStream eventFos = new ObjectOutputStream(os);
                        try {
                            eventFos.writeObject(compositeEvent);
                        } catch (IOException e) {
                            // e.printStackTrace();
                        }
                        os.close();
                        eventFos.close();
                    }
                }
            }
        }

    }


}
