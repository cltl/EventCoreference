package eu.newsreader.eventcoreference.naf;

import eu.newsreader.eventcoreference.objects.SourceMeta;
import eu.newsreader.eventcoreference.util.ReadSourceMetaFile;
import eu.newsreader.eventcoreference.util.Util;
import vu.wntools.wordnet.WordnetData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by piek on 16/07/15.
 */
public class Naf2SemTest {

    static Vector<String> communicationVector = null;
    static Vector<String> grammaticalVector = null;
    static Vector<String> contextualVector = null;

    static boolean ADDITIONALROLES = false;

    static String MATCHTYPE= "ILILEMMA";  // ILI OR ILILEMMA
    static boolean VERBOSEMENTIONS = false;

    static public void main (String[] args) {

            String pathToNafFolder = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v3_2015/test";
            String pathToEventFolder ="/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v3_2015/test";
            String projectName  = "cars";
            String extension = ".xml";
            String comFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v3_2015/resources/communication.txt";
            String contextualFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v3_2015/resources/contextual.txt";
            String grammaticalFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v3_2015/resources/grammatical.txt";
            ADDITIONALROLES = true;

            //// read resources
            communicationVector = Util.ReadFileToStringVector(comFrameFile);
            grammaticalVector = Util.ReadFileToStringVector(grammaticalFrameFile);
            contextualVector = Util.ReadFileToStringVector(contextualFrameFile);
            try {
                ClusterEventObjects.communicationVector = communicationVector;
                ClusterEventObjects.grammaticalVector = grammaticalVector;
                ClusterEventObjects.contextualVector = contextualVector;
                ClusterEventObjects.ADDITIONALROLES = ADDITIONALROLES;

                ClusterEventObjects.processFolderEvents(projectName, new File(pathToNafFolder), new File(pathToEventFolder), extension);
            } catch (IOException e) {
                e.printStackTrace();
            }

        ArrayList<String> roleArrayList0 = new ArrayList<String>();
        roleArrayList0.add("a0");
        ArrayList<String> roleArrayList1 = new ArrayList<String>();
        roleArrayList1.add("a0");
        roleArrayList1.add("a1");
        ArrayList<String> roleArrayList2 = new ArrayList<String>();
        roleArrayList1.add("a0");
        roleArrayList1.add("a1");
        roleArrayList1.add("a2");
        ArrayList<String> roleArrayList3 = new ArrayList<String>();
        roleArrayList1.add("a0");
        roleArrayList1.add("a1");
        roleArrayList1.add("a2");
        roleArrayList1.add("a3");
        ArrayList<String> roleArrayList4 = new ArrayList<String>();
        roleArrayList1.add("a0");
        roleArrayList1.add("a1");
        roleArrayList1.add("a2");
        roleArrayList1.add("a3");
        roleArrayList1.add("a4");
        String eventType = "";
        HashMap<String, SourceMeta> sourceMetaHashMap = null;
        WordnetData wordnetData = null;
        double conceptMatchThreshold = 0;
        double phraseMatchThreshold = 1;
        String pathToSourceDataFile = "";
        pathToSourceDataFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v3_2015/resources/LN-coremetadata.txt";

        if (!pathToSourceDataFile.isEmpty()) {
            sourceMetaHashMap = ReadSourceMetaFile.readSourceFile(pathToSourceDataFile);
          //  System.out.println("sourceMetaHashMap = " + sourceMetaHashMap.size());
        }
        MatchEventObjects.MATCHTYPE = MATCHTYPE;
        MatchEventObjects.VERBOSEMENTIONS = VERBOSEMENTIONS;
      //  MatchEventObjects.DEBUG = true;

        String pathToObjEventFolder = pathToEventFolder+"/events/contextual";
        eventType = "contextual";
        MatchEventObjects.processEventFoldersSingleOutputFile(new File(pathToObjEventFolder),
                conceptMatchThreshold, phraseMatchThreshold,
                sourceMetaHashMap, wordnetData, eventType, roleArrayList1);


        pathToObjEventFolder = pathToEventFolder+"/events/source";
        eventType = "source";
        MatchEventObjects.processEventFoldersSingleOutputFile(new File(pathToObjEventFolder),
                conceptMatchThreshold, phraseMatchThreshold,
                sourceMetaHashMap, wordnetData, eventType,roleArrayList2);

        pathToObjEventFolder = pathToEventFolder + "/events/grammatical";
        eventType = "grammatical";
        MatchEventObjects.processEventFoldersSingleOutputFile(new File(pathToObjEventFolder),
                conceptMatchThreshold, phraseMatchThreshold,
                sourceMetaHashMap, wordnetData, eventType, roleArrayList3);

        pathToObjEventFolder = pathToEventFolder + "/events/future";
        eventType = "future";
        MatchEventObjects.processEventFoldersSingleOutputFile(new File(pathToObjEventFolder),
                conceptMatchThreshold, phraseMatchThreshold,
                sourceMetaHashMap, wordnetData, eventType, roleArrayList3);
    }
}
