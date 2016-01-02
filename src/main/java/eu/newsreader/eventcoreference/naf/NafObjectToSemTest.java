package eu.newsreader.eventcoreference.naf;

import eu.newsreader.eventcoreference.objects.SourceMeta;
import eu.newsreader.eventcoreference.util.Util;
import vu.wntools.wordnet.WordnetData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by piek on 23/07/15.
 */
public class NafObjectToSemTest {
    static Vector<String> communicationVector = null;
    static Vector<String> grammaticalVector = null;
    static Vector<String> contextualVector = null;

    static boolean ADDITIONALROLES = false;

    static String MATCHTYPE= "ILI";  // ILI OR ILILEMMA
    static boolean VERBOSEMENTIONS = false;

    static public void main (String[] args) {

       // String pathToEventFolder ="/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v3_2015/test";
        String pathToEventFolder ="/Users/piek/Desktop/NWR/NWR-ontology/wikinews_v3_out/test";



        String comFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v3_2015/resources/communication.txt";
        String contextualFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v3_2015/resources/contextual.txt";
        String grammaticalFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v3_2015/resources/grammatical.txt";
        ADDITIONALROLES = true;

        //// read resources
        communicationVector = Util.ReadFileToStringVector(comFrameFile);
        grammaticalVector = Util.ReadFileToStringVector(grammaticalFrameFile);
        contextualVector = Util.ReadFileToStringVector(contextualFrameFile);


        String eventType = "";
        HashMap<String, SourceMeta> sourceMetaHashMap = null;
        WordnetData wordnetData = null;
        double conceptMatchThreshold = 0;
        double phraseMatchThreshold = 1;

        ArrayList<String> roleArrayList0 = new ArrayList<String>();
        roleArrayList0.add("a0");
        ArrayList<String> roleArrayList01 = new ArrayList<String>();
        roleArrayList01.add("a0");
        roleArrayList01.add("a1");
        ArrayList<String> roleArrayList1 = new ArrayList<String>();
        roleArrayList1.add("a1");
        ArrayList<String> roleArrayList012 = new ArrayList<String>();
        roleArrayList012.add("a0");
        roleArrayList012.add("a1");
        roleArrayList012.add("a2");
        ArrayList<String> roleArrayList12 = new ArrayList<String>();
        roleArrayList12.add("a1");
        roleArrayList12.add("a2");
        ArrayList<String> roleArrayList0123 = new ArrayList<String>();
        roleArrayList0123.add("a0");
        roleArrayList0123.add("a1");
        roleArrayList0123.add("a2");
        roleArrayList0123.add("a3");
        ArrayList<String> roleArrayList123 = new ArrayList<String>();
        roleArrayList123.add("a1");
        roleArrayList123.add("a2");
        roleArrayList123.add("a3");


        MatchEventObjects.VERBOSEMENTIONS = VERBOSEMENTIONS;
        MatchEventObjects.MATCHTYPE = "ILILEMMA";
        MatchEventObjects.LCS = false;
        MatchEventObjects.DEBUG = 0;

        String pathToObjEventFolder = pathToEventFolder+"/events/contextual";
/*      @Deprecated
        MatchEventObjects.processEventFoldersHashMap(new File(pathToObjEventFolder),
                conceptMatchThreshold, phraseMatchThreshold,
                sourceMetaHashMap, wordnetData, roleArrayList1);
*/


      /*  MatchEventObjects.MATCHTYPE = "LEMMA";
        MatchEventObjects.LCS = false;

        pathToObjEventFolder = pathToEventFolder+"/events/source";
        eventType = "source";
        MatchEventObjects.processEventFoldersSingleOutputFile(new File(pathToObjEventFolder),
                conceptMatchThreshold, phraseMatchThreshold,
                sourceMetaHashMap, wordnetData, eventType,roleArrayList12);

        MatchEventObjects.MATCHTYPE = "LEMMA";
        MatchEventObjects.LCS = false;

        pathToObjEventFolder = pathToEventFolder + "/events/grammatical";
        eventType = "grammatical";
        MatchEventObjects.processEventFoldersSingleOutputFile(new File(pathToObjEventFolder),
                conceptMatchThreshold, phraseMatchThreshold,
                sourceMetaHashMap, wordnetData, eventType, roleArrayList12);

        MatchEventObjects.MATCHTYPE = "LEMMA";
        MatchEventObjects.LCS = false;
        pathToObjEventFolder = pathToEventFolder + "/events/future";
        eventType = "future";
        MatchEventObjects.processEventFoldersSingleOutputFile(new File(pathToObjEventFolder),
                conceptMatchThreshold, phraseMatchThreshold,
                sourceMetaHashMap, wordnetData, eventType, null);*/
    }
}
