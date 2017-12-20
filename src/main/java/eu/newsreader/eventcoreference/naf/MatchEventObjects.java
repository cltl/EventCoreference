package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSense;
import eu.newsreader.eventcoreference.coref.ComponentMatch;
import eu.newsreader.eventcoreference.objects.CompositeEvent;
import eu.newsreader.eventcoreference.objects.NafMention;
import eu.newsreader.eventcoreference.objects.SemObject;
import eu.newsreader.eventcoreference.objects.SourceMeta;
import eu.newsreader.eventcoreference.output.JenaSerialization;
import eu.newsreader.eventcoreference.util.ReadSourceMetaFile;
import eu.newsreader.eventcoreference.util.Util;
import org.apache.jena.atlas.logging.Log;
import vu.wntools.wordnet.WordnetData;
import vu.wntools.wordnet.WordnetLmfSaxParser;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/14/13
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class MatchEventObjects {
    static int nEventMatches = 0;
    static boolean CROSSDOC = false;
    static boolean FIXURI = false;
    static boolean GZIP = false;
    static boolean SUBFOLDER = false;
    static int DEBUG = 0;
    static ArrayList<String> tokenIds = new ArrayList<String>();
    public static String MATCHTYPE= "ililemma";  // ili OR lemma OR ililemma OR none OR split
    public static boolean HYPERS = false;
    public static boolean LCS = false;
    public static boolean ILIURI = false;
    public static boolean VERBOSEMENTIONS = false;
    public static String CHAINING = "3";
    static ArrayList<String> crossDocCorefSet = new ArrayList<String>(); /// just for debugging
    static public String TIME = "";

    static final String usage = "MatchEventObjects reads obj files stored in time-folders with CompositeEventObjects and outputs a single RDF-TRiG file\n" +
            "The parameters are:\n" +
            "--event-folder  <path>     <Path to the event folder that has subfolders for each time-description, e.g. \"e-2012-03-29\". Object file (*.obj) should be stored in these subfolders\n" +
            "--gz                       <OPTIONAL for reading gzipped object files with gz extension and writing trig.gz" +
            "--wordnet-lmf   <path>     <(OPTIONAL, not yet used) Path to a WordNet-LMF file\n>" +
            "--concept-match <int>      <threshold for conceptual matches of events, default is 50>\n" +
            "--phrase-match  <int>      <threshold for phrase matches of events, default is 50>\n" +
            "--hypers                   <(OPTIONAL) use hypernyms to match events\n"+
            "--lcs                      <(OPTIONAL) use lowest-common-subsumers to match events\n"+
            "--chaining                 <Determines the chaining function used: 1, 2, 3, 4. Default value is 3\n"+
            "--match-type    <string>   <(OPTIONAL) Indicates what is used to match events across resources. Default value is \"LEMMA\". Values:\"LEMMA\", \"ILI\", \"ILILEMMA\">\n" +
            "--ili                      <(OPTIONAL) Path to ILI.ttl file to convert wordnet-synsets identifiers to ILI identifiers>\n" +
            "--source-data   <path>     <(OPTIONAL, Deprecated) Path to LexisNexis meta data on owners and authors to enrich the provenance>\n" +
            "--roles  <string>          <String with PropbBank roles, separated by \",\" for which there minimally needs to be a match, e.g. \"a0,a1\". This is especially relevant for sourceEvent, grammaticalEvent. If value is \"all\", then all participants need to match. This can be used for futureEvent"+
            "--verbose                  <(OPTIONAL) representation of mentions is extended with token ids, terms ids and sentence number\n"+
            "--time     <string>        <(OPTIONAL) year, month or day indicate granularity of temporal match. If empty time is not matched\n"+
            "--ili-uri                  <(OPTIONAL) If used, the ILI-identifiers are used to represents events. This is necessary for cross-lingual extraction>\n" +
            "--subfolder                <(OPTIONAL) Processes any subfolder>\n" +
            "--debug                    <(OPTIONAL) default=0, 1=minimal, 2=max>\n";

    static String testArguments = "--event-folder /Users/piek/Desktop/Yassine/s1b2b/events/all --concept-match 80 --phrase-match 68 --ili /Code/vu/newsreader/vua-resources/ili.ttl.gz --hypers --lcs --chaining 3 --match-type ILILEMMA --verbose --debug 1 --time year --token-id /Users/piek/Desktop/Yassine/s1b2b.key.tokens";
    static public void main (String [] args) {
        Log.setLog4j("jena-log4j.properties");
        ArrayList<String> roleNeededArrayList = new ArrayList<String>();
        HashMap<String, SourceMeta> sourceMetaHashMap = null;
        WordnetData wordnetData = null;
        int conceptMatchThreshold = 50;
        int phraseMatchThreshold = 50;
        String pathToEventFolder = "";
        String pathToSourceDataFile = "";
        if (args.length==0) args = testArguments.split(" ");
        if (args.length==1 && args[0].equals("--help")) {
            System.out.println("usage = " + usage);
            return;
        }
        if (args.length==1 && args[0].equals("--usage")) {
            System.out.println("usage = " + usage);
            return;
        }
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--event-folder") && args.length>(i+1)) {
                pathToEventFolder = args[i+1];
            }
            else if (arg.equals("--wordnet-lmf") && args.length>(i+1)) {
                WordnetLmfSaxParser wordnetLmfSaxParser = new WordnetLmfSaxParser();
                wordnetLmfSaxParser.parseFile(args[i + 1]);
                wordnetData = wordnetLmfSaxParser.wordnetData;
            }
            else if (arg.equals("--concept-match") && args.length>(i+1)) {
                try {
                    conceptMatchThreshold = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else if (arg.equals("--token-id") && args.length>(i+1)) {
                String tokenidPath = args[i + 1];
                tokenIds = Util.ReadFileToStringArrayList(tokenidPath);
                System.out.println("tokenIds = " + tokenIds.size());
            }
            else if (arg.equals("--subfolder")) {
                SUBFOLDER = true;
            }
            else if (arg.equals("--cross-doc")) {
                CROSSDOC = true;
            }
            else if (arg.equals("--uri-fix")) {
                FIXURI = true;
            }
            else if (arg.equals("--phrase-match") && args.length>(i+1)) {
                try {
                    phraseMatchThreshold = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else if (arg.equals("--lcs")) {
                LCS = true;
            }
            else if (arg.equals("--hypers")) {
                HYPERS = true;
            }
            else if (arg.equals("--roles") && args.length>(i+1)) {
                String [] fields = args[i+1].split(",");
                for (int j = 0; j < fields.length; j++) {
                    String field = fields[j].trim().toLowerCase();
                    roleNeededArrayList.add(field);
                }
            }
            else if (arg.equals("--ili") && args.length > (i + 1)) {
                String pathToILIFile = args[i+1];
                JenaSerialization.initILI(pathToILIFile);
            }
            else if (arg.equals("--match-type") && args.length > (i + 1)) {
                MATCHTYPE = args[i+1];
            }
            else if (arg.equals("--chaining") && args.length > (i + 1)) {
                CHAINING = args[i+1];
            }
            else if (arg.equals("--ili-uri")) {
                ILIURI = true;
            }
            else if (arg.equals("--source-data") && args.length>(i+1)) {
                pathToSourceDataFile = args[i+1];
            }
            else if (arg.equals("--time") && args.length>(i+1)) {
                TIME = args[i+1];
            }
            else if (arg.equals("--debug")&& args.length>(i+1)) {
                DEBUG = Integer.parseInt(args[i+1]);
            }
            else if (arg.equals("--verbose")) {
                VERBOSEMENTIONS = true;
            }
        }
        if (DEBUG>0) {
            System.out.println("DEBUG = " + DEBUG);
        }
        if (!pathToSourceDataFile.isEmpty()) {
            sourceMetaHashMap = ReadSourceMetaFile.readSourceFile(pathToSourceDataFile);
         //   System.out.println("sourceMetaHashMap = " + sourceMetaHashMap.size());
        }
        if (MATCHTYPE.equalsIgnoreCase("none")) {
            serializeEventFoldersHashMap(new File(pathToEventFolder));
        }
        else if (MATCHTYPE.equalsIgnoreCase("split")) {
            splitEventFoldersHashMap(new File(pathToEventFolder));
        }
        else {
            processEventFoldersHashMap(new File(pathToEventFolder),
                    conceptMatchThreshold,
                    phraseMatchThreshold,
                    sourceMetaHashMap,
                    wordnetData,
                    roleNeededArrayList);
        }
       // System.out.println("nEventMatches = " + nEventMatches);

    }



    public static ArrayList<KafSense> getILIreferences(SemObject semEvent) {
        ArrayList<KafSense> iliReferences = new ArrayList<KafSense>();
        for (int i = 0; i < semEvent.getConcepts().size(); i++) {
            KafSense kafSense = semEvent.getConcepts().get(i);
            if (kafSense.getSensecode().toLowerCase().startsWith("ili-")) {
                iliReferences.add(kafSense);
            }
            else if (kafSense.getSensecode().toLowerCase().startsWith("eng-")) {
                iliReferences.add(kafSense);
            }
            else if (kafSense.getResource().toLowerCase().startsWith("cornetto")) {
                iliReferences.add(kafSense);
            }
            else if (kafSense.getResource().toLowerCase().startsWith("wordnet")) {
                iliReferences.add(kafSense);
            }
            else if (kafSense.getResource().toLowerCase().startsWith("wn")) {
                iliReferences.add(kafSense);
            }
            else if (kafSense.getResource().toLowerCase().startsWith("ili")) {
                iliReferences.add(kafSense);
            }
        }
        return iliReferences;
    }

    public static ArrayList<KafSense> getHyperILIreferences(SemObject semEvent) {
        ArrayList<KafSense> iliReferences = new ArrayList<KafSense>();
        if (semEvent.getHypers()!=null) {
            for (int i = 0; i < semEvent.getHypers().size(); i++) {
                KafSense kafSense = semEvent.getHypers().get(i);
                iliReferences.add(kafSense);
            }
        }
        return iliReferences;
    }

    public static ArrayList<KafSense> getLcsILIreferences(SemObject semEvent) {
        ArrayList<KafSense> iliReferences = new ArrayList<KafSense>();
        if (semEvent.getLcs()!=null) {
            for (int i = 0; i < semEvent.getLcs().size(); i++) {
                KafSense kafSense = semEvent.getLcs().get(i);
                iliReferences.add(kafSense);
            }
        }
        return iliReferences;
    }


    /**
     * if MATCHTYPE==none No comparison and events are serialized to separate TRiG files per object file
     * @param pathToEventFolder
     */
    public static void serializeEventFoldersHashMap (File pathToEventFolder) {
        HashMap<String, CompositeEvent> events = new HashMap<String, CompositeEvent>();

            try {

                ArrayList<File> files = new ArrayList<File>();
                if (GZIP) {
                    files = Util.makeRecursiveFileList(pathToEventFolder, ".obj.gz");
                }
                else {
                    files = Util.makeRecursiveFileList(pathToEventFolder, ".obj");
                }
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    OutputStream fos = null;
                    String filePath = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("."))+"sem.trig";

                    if (GZIP) {
                        filePath = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("."))+"sem.trig.gz";

                        OutputStream fileOutStream  = new FileOutputStream(filePath);
                        fos = new GZIPOutputStream(fileOutStream);

                    }
                    else {
                        fos = new FileOutputStream(filePath);
                    }

                    events = new HashMap<String, CompositeEvent>();
                    readCompositeEventArrayListFromObjectFile(file, events);
                    JenaSerialization.serializeJenaSingleCompositeEvents(fos,
                            events,
                            null,
                            ILIURI,
                            VERBOSEMENTIONS);
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

    }

    /**
     * if MATCHTYPE==none No comparison and events are serialized to separate TRiG files per object file
     * @param pathToEventFolder
     */
    public static void serializeEventFoldersHashMapList (File pathToEventFolder) {
        HashMap<String, CompositeEvent> events = new HashMap<String, CompositeEvent>();
        ArrayList<File> eventFolders = Util.makeFolderList(pathToEventFolder);
        for (int f = 0; f < eventFolders.size(); f++) {
            File nextEventFolder =  eventFolders.get(f);
            try {

                ArrayList<File> files = new ArrayList<File>();
                if (GZIP) {
                    files = Util.makeRecursiveFileList(nextEventFolder, ".obj.gz");
                }
                else {
                    files = Util.makeRecursiveFileList(nextEventFolder, ".obj");
                }
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    OutputStream fos = null;
                    String filePath = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("."))+"sem.trig";

                    if (GZIP) {
                        filePath = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("."))+"sem.trig.gz";

                        OutputStream fileOutStream  = new FileOutputStream(filePath);
                        fos = new GZIPOutputStream(fileOutStream);

                    }
                    else {
                        fos = new FileOutputStream(filePath);
                    }

                    events = new HashMap<String, CompositeEvent>();
                    readCompositeEventArrayListFromObjectFile(file, events);
                    JenaSerialization.serializeJenaSingleCompositeEvents(fos,
                            events,
                            null,
                            ILIURI,
                            VERBOSEMENTIONS);
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }

    public static void splitEventFoldersHashMap (File pathToEventFolder) {
        HashMap<String, CompositeEvent> events = new HashMap<String, CompositeEvent>();
        ArrayList<File> eventFolders = Util.makeFolderList(pathToEventFolder);
        for (int f = 0; f < eventFolders.size(); f++) {
            File nextEventFolder =  eventFolders.get(f);
            ArrayList<File> files = Util.makeRecursiveFileList(nextEventFolder, ".obj");
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                events = new HashMap<String, CompositeEvent>();
                readCompositeEventArrayListFromObjectFile(file, events);
              //  splitting(events,nextEventFolder, pathToEventFolder, file.getName());
            }
        }
    }

    public static void processEventFoldersHashMap (File pathToEventFolder, int conceptMatchThreshold,
                                                            int phraseMatchThreshold,
                                                            HashMap<String, SourceMeta> sourceMetaHashMap,
                                                            WordnetData wordnetData,
                                                            ArrayList<String> roleNeededArrayList


                                                   ) {
        HashMap<String, CompositeEvent> allCompositeEvents = new HashMap<String, CompositeEvent>();
        ArrayList<File> eventFolders = new ArrayList<File>();
        if (SUBFOLDER) {
                eventFolders.add(pathToEventFolder);
        }
        else {
            eventFolders = Util.makeFolderList(pathToEventFolder);
            if (eventFolders.size() == 0) {
                eventFolders.add(pathToEventFolder);
            }
        }
        if (DEBUG==2) System.out.println("eventFolders.size() = " + eventFolders.size());
        for (int f = 0; f < eventFolders.size(); f++) {
            File nextEventFolder =  eventFolders.get(f);
            if (DEBUG==2) System.out.println("nextEventFolder = " + nextEventFolder.getName());

            try {
                if (DEBUG==1) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    Date date = new Date();
                    System.out.println("Start reading obj files:" + dateFormat.format(date));
                }

                OutputStream fos = null;
                if (GZIP) {
                    OutputStream fileOutStream  = new FileOutputStream(nextEventFolder.getAbsolutePath()+"/sem.trig.gz");
                    fos = new GZIPOutputStream(fileOutStream);

                }
                else {
                    fos = new FileOutputStream(nextEventFolder.getAbsolutePath()+"/sem.trig");
                }

                /// events is initialised outside the loop so that events are compared against the total list
                allCompositeEvents = new HashMap<String, CompositeEvent>();
                HashMap<String, CompositeEvent> crossDocEvents = new HashMap<String, CompositeEvent>();
                ArrayList<File> files = new ArrayList<File>();
                if (GZIP) {
                    files = Util.makeRecursiveFileList(nextEventFolder, ".obj.gz");
                }
                else {
                    files = Util.makeRecursiveFileList(nextEventFolder, ".obj");
                }
                if (DEBUG==1)
                    System.out.println("files.size() = " + files.size());
                if (files.size()>0) {
                    if (DEBUG == 1) {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        System.out.println("Before reading object files:" + dateFormat.format(date));
                    }
                    for (int i = 0; i < files.size(); i++) {
                        File file = files.get(i);
                        if (DEBUG == 2) System.out.println("file.getName() = " + file.getName());
                        readCompositeEventArrayListFromObjectFile(file, allCompositeEvents);
                        if (DEBUG == 2) System.out.println("events.size() = " + allCompositeEvents.size());
                    }

                    if (tokenIds.size()>0) {
                        System.out.println("allCompositeEvents = " + allCompositeEvents.size());
                        allCompositeEvents = removeEventsWithoutTokenIds(allCompositeEvents);
                        System.out.println("relevant events  = " + allCompositeEvents.size());

                    }
                    /// we create an ArrayList with the event ids so that we can call the recursive chaining function
                    ArrayList<String> eventIds = new ArrayList<String>();
                    Set keySet = allCompositeEvents.keySet();
                    Iterator<String> keys = keySet.iterator();
                    while (keys.hasNext()) {
                        String id = keys.next();
                        eventIds.add(id);
                    }

                    if (DEBUG == 1) {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        System.out.println("End reading object files:" + dateFormat.format(date));
                    }

                    if (DEBUG == 1) System.out.println("events before chaining = " + allCompositeEvents.size());
                    if (CHAINING.equals("1")) {
                        chaining1(allCompositeEvents, eventIds,
                                phraseMatchThreshold,
                                conceptMatchThreshold,
                                roleNeededArrayList);
                    } else if (CHAINING.equals("2")) {
                        chaining2(allCompositeEvents, eventIds, eventIds,
                                phraseMatchThreshold,
                                conceptMatchThreshold,
                                roleNeededArrayList);
                    } else if (CHAINING.equals("3")) {
                        /**
                         * We first build a map from each sense code to the event identifiers that have this sense code
                         * This maps determines what events are compared with what other events
                         * The map contains sense codes and lemmas. The sense code can be based on the ILI references or the LCS references or the HYPERNYMS depending on the setting
                         */
                        HashMap<String, ArrayList<String>> conceptEventMap = buildConceptEventMap(allCompositeEvents);
                        if (DEBUG == 2) System.out.println("conceptEventMap.size() = " + conceptEventMap.size());

                        chaining3(allCompositeEvents, conceptEventMap, eventIds,
                                phraseMatchThreshold,
                                conceptMatchThreshold,
                                roleNeededArrayList);
                        if (CROSSDOC) {
                            for (int i = 0; i < crossDocCorefSet.size(); i++) {
                                String s = crossDocCorefSet.get(i);
                                if (allCompositeEvents.containsKey(s)) {
                                    CompositeEvent compositeEvent = allCompositeEvents.get(s);
                                    crossDocEvents.put(s, compositeEvent);
                                }

                            }
                        }
                    } else if (CHAINING.equals("4")) {
                        /**
                         * We first build a map from each sense code to the event identifiers that have this sense code
                         * This maps determines what events are compared with what other events
                         * The map contains sense codes and lemmas. The sense code can be based on the ILI references or the LCS references or the HYPERNYMS depending on the setting
                         */
                        HashMap<String, ArrayList<String>> conceptEventMap = buildConceptEventMap(allCompositeEvents);
                        if (DEBUG == 2) System.out.println("conceptEventMap.size() = " + conceptEventMap.size());

                        chaining4(allCompositeEvents, conceptEventMap, eventIds,
                                phraseMatchThreshold,
                                conceptMatchThreshold,
                                roleNeededArrayList);
                        if (CROSSDOC) {
                            for (int i = 0; i < crossDocCorefSet.size(); i++) {
                                String s = crossDocCorefSet.get(i);
                                if (allCompositeEvents.containsKey(s)) {
                                    CompositeEvent compositeEvent = allCompositeEvents.get(s);
                                    crossDocEvents.put(s, compositeEvent);
                                }

                            }
                        }
                    }
                    if (DEBUG == 1) {
                        System.out.println("events after chaining = " + allCompositeEvents.size());
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        System.out.println("End chaining:" + dateFormat.format(date));
                    }

                    if (CROSSDOC) {
                        JenaSerialization.serializeJenaSingleCompositeEvents(fos,
                                crossDocEvents,
                                sourceMetaHashMap,
                                ILIURI,
                                VERBOSEMENTIONS);
                    } else {

                        System.out.println("###################################");
                        System.out.println("FINAL NUMBER OF EVENTS = " + allCompositeEvents.size());
                        if (DEBUG>1) {
                            JenaSerialization.DEBUG = true;
                        }
                        JenaSerialization.serializeJenaSingleCompositeEvents(fos,
                                allCompositeEvents,
                                sourceMetaHashMap,
                                ILIURI,
                                VERBOSEMENTIONS);
                    }
                    if (DEBUG == 1) {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        System.out.println("End writing sem.trig:" + dateFormat.format(date));
                    }
                    fos.close();
                }
            } catch (IOException e) {
               // e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }

    static HashMap<String, CompositeEvent> removeEventsWithoutTokenIds (HashMap<String, CompositeEvent> allCompositeEvents) {
        HashMap<String, CompositeEvent> selectedEvents = new HashMap<String, CompositeEvent>();
        Set keySet = allCompositeEvents.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            CompositeEvent compositeEvent = allCompositeEvents.get(key);
            SemObject event = compositeEvent.getEvent();
            boolean match = false;
            for (int i = 0; i < event.getNafMentions().size(); i++) {
                NafMention nafMention = event.getNafMentions().get(i);
                for (int j = 0; j < tokenIds.size(); j++) {
                    String s = tokenIds.get(j);
                    String [] fields = s.split("\t");
                    if (fields.length==3) {
/*                        if (nafMention.getBaseUri().indexOf(fields[0])>-1) {
                            System.out.println("fields[0] = " + fields[0]);
                            System.out.println("fields[1] = " + fields[1]);
                            System.out.println("fields[2] = " + fields[2]);
                            System.out.println("nafMention: ");
                            System.out.println(" = " + nafMention.getBaseUri());
                            System.out.println(" = " + nafMention.getSentence());
                            System.out.println(" = " + nafMention.getTokensIds().toString());
                        }*/
                        if (nafMention.getBaseUri().indexOf(fields[0])>-1
                                &&
                                nafMention.getSentence().equals(fields[1])
                                &&
                                nafMention.getTokensIds().contains("w"+fields[2])) {
                            //// we have a match
                            match = true;
                            break;
                        }
                    }
                }
                if (match) {
                    selectedEvents.put(key, compositeEvent);
                    break;
                }
            }
        }
        System.out.println("selectedEvents = " + selectedEvents.size());
        return selectedEvents;
    }

    public static void readCompositeEventArrayListFromObjectFile (File file, HashMap<String,CompositeEvent> events) {
        if (file.exists() ) {
            int cnt = 0;
            if (DEBUG==1) System.out.println("file = " + file.getName());
            try {
                InputStream fis = new FileInputStream(file);

                if (GZIP) {
                    InputStream fileStream = new FileInputStream(file);
                    fis = new GZIPInputStream(fileStream);
                }

                ObjectInputStream ois =  new ObjectInputStream(fis);

                Object obj = null;
                while (fis.available()>0) {
                    while ((obj = ois.readObject()) != null) {
                        cnt++;
                        if (obj instanceof CompositeEvent) {
                            CompositeEvent compositeEvent = (CompositeEvent) obj;
                            if (FIXURI) Util.fixUriCompositeEvent(compositeEvent);
                            events.put(compositeEvent.getEvent().getId(), compositeEvent);
                        } else {
                            if (DEBUG==2) System.out.println("Unknown object obj.getClass() = " + obj.getClass());
                        }
                    }
                    ois.reset();
                    ois.close();
                    fis.close();
                }
            } catch (Exception e) {
                if (DEBUG==2) {
                 //   System.out.println("file = " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }
            if (DEBUG==1) System.out.println(file.getName()+" nr objects read = " + cnt);
        }
    }

    /**
     * @param myCompositeEvent
     * @param allCompositeEvents
     * @param eventMapIds
     * @param idx
     * @param phraseMatchThreshold
     * @param conceptMatchThreshold
     * @param roleNeededArrayList
     * @return
     */
    static String matchEvents (CompositeEvent myCompositeEvent,
                                HashMap<String, CompositeEvent> allCompositeEvents,
                                ArrayList<String> eventMapIds,
                                int idx,
                                int conceptMatchThreshold,
                                int phraseMatchThreshold,
                                ArrayList<String> roleNeededArrayList) {
        String mergedEventId = "";
        for (int j = idx; j < eventMapIds.size(); j++) {
            String targetEventId = eventMapIds.get(j);
            if (!allCompositeEvents.containsKey(targetEventId)) {
                continue;
            }
            CompositeEvent targetEvent = allCompositeEvents.get(targetEventId);
            if (targetEvent == null) {
                continue;
            }
            boolean EVENTMATCH = false;

            if (MATCHTYPE.equalsIgnoreCase("lemma")) {

                if (ComponentMatch.compareEventLabelReference(myCompositeEvent, targetEvent, phraseMatchThreshold)) {
                    EVENTMATCH = true;
                }
            }
            else {
                //// it should be ILI or ILILEMMA
                //// We then first check the ILI matches
                ArrayList<KafSense> event1References = getILIreferences(myCompositeEvent.getEvent());
                ArrayList<KafSense> event2References = getILIreferences(targetEvent.getEvent());
                if (LCS) {
                    ArrayList<KafSense> event1Lcs = getLcsILIreferences(myCompositeEvent.getEvent());
                    ArrayList<KafSense> event2Lcs = getLcsILIreferences(targetEvent.getEvent());
                    Util.addNewReferences(event1Lcs, event1References);
                    Util.addNewReferences(event2Lcs, event1References);
/*
                    event1References.addAll(event1Lcs);
                    event2References.addAll(event2Lcs);
*/
                }
                if (HYPERS) {
                    ArrayList<KafSense> event1Hyper = getHyperILIreferences(myCompositeEvent.getEvent());
                    ArrayList<KafSense> event2Hyper = getHyperILIreferences(targetEvent.getEvent());
                    Util.addNewReferences(event1Hyper, event1References);
                    Util.addNewReferences(event2Hyper, event1References);
/*
                    event1References.addAll(event1Hyper);
                    event2References.addAll(event2Hyper);
*/
                }
                if (ComponentMatch.compareReference(event1References, event2References, conceptMatchThreshold)) {
                    EVENTMATCH = true;
                }
                else {
                    if (MATCHTYPE.equalsIgnoreCase("ililemma")) {
                        /// if one of the two or both have no synsets then we compare the lemmas
                        if (ComponentMatch.compareEventLabelReference(myCompositeEvent, targetEvent, phraseMatchThreshold)) {
                            EVENTMATCH = true;
                        }
                    }
                }
            }
            if (EVENTMATCH) {
                if (DEBUG>1) System.out.println("myCompositeEvent = " + myCompositeEvent.getEvent().getId());
                if (DEBUG>1) System.out.println("myCompositeEvent = " + myCompositeEvent.getEvent().getPhraseCounts().toString());
                if (DEBUG>1) System.out.println("myCompositeEvent = " + myCompositeEvent.getEvent().getNafMentions().toString());
                if (DEBUG>1) System.out.println("targetEvent = " + targetEvent.getEvent().getId());
                if (DEBUG>1) System.out.println("targetEvent = " + targetEvent.getEvent().getPhraseCounts().toString());
                if (DEBUG>1) System.out.println("targetEvent = " + targetEvent.getEvent().getNafMentions().toString());
                if (roleNeededArrayList.contains("none") || roleNeededArrayList.size()==0) {
                    if (!TIME.isEmpty()) {
                        if (DEBUG>1) System.out.println("TIME = " + TIME);
                        if (DEBUG>1) System.out.println("myCompositeEvent = " + myCompositeEvent.getEvent().getId());
                        if (DEBUG>1) System.out.println("compositeEvent1.getMySemTimes().size() = " + myCompositeEvent.getMySemTimes().size());
                        if (DEBUG>1) System.out.println("targetEvent = " + targetEvent.getEvent().getId());
                        if (DEBUG>1) System.out.println("targetEvent.getMySemTimes().size() = " + targetEvent.getMySemTimes().size());
                        if (ComponentMatch.compareTimeCompositeEvent(myCompositeEvent, targetEvent, TIME, (DEBUG>0))) {
                            if (DEBUG>1) System.out.println("MATCHED BY TIME");
                            targetEvent.getEvent().mergeSemObject(myCompositeEvent.getEvent());
                            targetEvent.mergeObjects(myCompositeEvent);
                            targetEvent.mergeRelations(myCompositeEvent);
                            allCompositeEvents.remove(myCompositeEvent.getEvent().getId());
                            mergedEventId = targetEvent.getEvent().getId();
                            break;
                        }
                        else {
                            if (DEBUG>1) System.out.println("TIME MISMATCH");
                        }
                    }
                    else {
                        targetEvent.getEvent().mergeSemObject(myCompositeEvent.getEvent());
                        targetEvent.mergeObjects(myCompositeEvent);
                        targetEvent.mergeRelations(myCompositeEvent);
                        allCompositeEvents.remove(myCompositeEvent.getEvent().getId());
                        mergedEventId = targetEvent.getEvent().getId();
                        break;
                    }
                }
                else  if (ComponentMatch.compareCompositeEvent(myCompositeEvent, targetEvent, roleNeededArrayList, DEBUG>0)) {
                    if (!TIME.isEmpty()) {
                        if (DEBUG>1) System.out.println("MATCHED BY PARTICIPANT, NEXT MATCH BY TIME:"+TIME);
                        if (DEBUG>1) System.out.println("compositeEvent1.getMySemTimes().size() = " + myCompositeEvent.getMySemTimes().size());
                        if (DEBUG>1) System.out.println("targetEvent.getMySemTimes().size() = " + targetEvent.getMySemTimes().size());
                        if (ComponentMatch.compareTimeCompositeEvent(myCompositeEvent, targetEvent, TIME,(DEBUG>0))) {
                            if (DEBUG>1) System.out.println("MATCHED BY TIME");
                            targetEvent.getEvent().mergeSemObject(myCompositeEvent.getEvent());
                            targetEvent.mergeObjects(myCompositeEvent);
                            targetEvent.mergeRelations(myCompositeEvent);
                            allCompositeEvents.remove(myCompositeEvent.getEvent().getId());
                            mergedEventId = targetEvent.getEvent().getId();
                            break;
                        }
                        else {
                            if (DEBUG>1) System.out.println("TIME MISMATCH");
                        }
                    }
                    else {
                        if (DEBUG>1) System.out.println("MATCHED BY PARTICIPANT");
                        targetEvent.getEvent().mergeSemObject(myCompositeEvent.getEvent());
                        targetEvent.mergeObjects(myCompositeEvent);
                        targetEvent.mergeRelations(myCompositeEvent);
                        allCompositeEvents.remove(myCompositeEvent.getEvent().getId());
                        mergedEventId = targetEvent.getEvent().getId();
                        break;
                    }
                }
            }
            else {
                if (DEBUG>1) System.out.println("NO EVENTMATCH");
            }
        }
        return mergedEventId;
    }

    /**
     * Variant that swallows all events that match and return true if there was a match
     * @param myCompositeEvent
     * @param allCompositeEvents
     * @param eventMapIds
     * @param idx
     * @param phraseMatchThreshold
     * @param conceptMatchThreshold
     * @param roleNeededArrayList
     * @return
     */
    static ArrayList<String> matchAndSwallowEvents (CompositeEvent myCompositeEvent,
                                HashMap<String, CompositeEvent> allCompositeEvents,
                                ArrayList<String> eventMapIds,
                                int idx,
                                int conceptMatchThreshold,
                                int phraseMatchThreshold,
                                ArrayList<String> roleNeededArrayList) {
        ArrayList<String> swallowedEvents = new ArrayList<String>();
        for (int j = idx; j < eventMapIds.size(); j++) {
            String targetEventId = eventMapIds.get(j);
            if (!allCompositeEvents.containsKey(targetEventId)) {
                continue;
            }
            CompositeEvent targetEvent = allCompositeEvents.get(targetEventId);
            if (targetEvent == null) {
                continue;
            }
            boolean EVENTMATCH = false;

            if (MATCHTYPE.equalsIgnoreCase("lemma")) {

                if (ComponentMatch.compareEventLabelReference(myCompositeEvent, targetEvent, phraseMatchThreshold)) {
                    EVENTMATCH = true;
                }
            }
            else {
                //// it should be ILI or ILILEMMA
                //// We then first check the ILI matches
                ArrayList<KafSense> event1References = getILIreferences(myCompositeEvent.getEvent());
                ArrayList<KafSense> event2References = getILIreferences(targetEvent.getEvent());
                if (LCS) {
                    ArrayList<KafSense> event1Lcs = getLcsILIreferences(myCompositeEvent.getEvent());
                    ArrayList<KafSense> event2Lcs = getLcsILIreferences(targetEvent.getEvent());
                    event1References.addAll(event1Lcs);
                    event2References.addAll(event2Lcs);
                }
                if (HYPERS) {
                    ArrayList<KafSense> event1Hyper = getHyperILIreferences(myCompositeEvent.getEvent());
                    ArrayList<KafSense> event2Hyper = getHyperILIreferences(targetEvent.getEvent());
                    event1References.addAll(event1Hyper);
                    event2References.addAll(event2Hyper);
                }
                if (ComponentMatch.compareReference(event1References, event2References, conceptMatchThreshold)) {
                    EVENTMATCH = true;
                }
                else {
                    if (MATCHTYPE.equalsIgnoreCase("ililemma")) {
                        /// if one of the two or both have no synsets then we compare the lemmas
                        if (ComponentMatch.compareEventLabelReference(myCompositeEvent, targetEvent, phraseMatchThreshold)) {
                            EVENTMATCH = true;
                        }
                    }
                }
            }
            if (EVENTMATCH) {
                if (roleNeededArrayList.contains("none") || roleNeededArrayList.size()==0) {
                    if (!TIME.isEmpty()) {
                        if (ComponentMatch.compareTimeCompositeEvent(myCompositeEvent, targetEvent, TIME, (DEBUG>0))) {

                            myCompositeEvent.getEvent().mergeSemObject(targetEvent.getEvent());
                            myCompositeEvent.mergeObjects(targetEvent);
                            myCompositeEvent.mergeRelations(targetEvent);
                            allCompositeEvents.remove(targetEvent.getEvent().getId());
                            swallowedEvents.add(targetEvent.getEvent().getId());
                        }
                    }
                    else {
                        myCompositeEvent.getEvent().mergeSemObject(targetEvent.getEvent());
                        myCompositeEvent.mergeObjects(targetEvent);
                        myCompositeEvent.mergeRelations(targetEvent);
                        allCompositeEvents.remove(targetEvent.getEvent().getId());
                        swallowedEvents.add(targetEvent.getEvent().getId());
                    }
                }
                else if (ComponentMatch.compareCompositeEvent(myCompositeEvent, targetEvent, roleNeededArrayList,DEBUG>0)) {
                    if (!TIME.isEmpty()) {
                        if (ComponentMatch.compareTimeCompositeEvent(myCompositeEvent, targetEvent, TIME,(DEBUG>0))) {
                            myCompositeEvent.getEvent().mergeSemObject(targetEvent.getEvent());
                            myCompositeEvent.mergeObjects(targetEvent);
                            myCompositeEvent.mergeRelations(targetEvent);
                            allCompositeEvents.remove(targetEvent.getEvent().getId());
                            swallowedEvents.add(targetEvent.getEvent().getId());
                        }
                    }
                    else {
                        myCompositeEvent.getEvent().mergeSemObject(targetEvent.getEvent());
                        myCompositeEvent.mergeObjects(targetEvent);
                        myCompositeEvent.mergeRelations(targetEvent);
                        allCompositeEvents.remove(targetEvent.getEvent().getId());
                        swallowedEvents.add(targetEvent.getEvent().getId());
                    }
                }

            }
            else {
               // System.out.println("NO EVENTMATCH");
            }
        }
        return swallowedEvents;
    }

    static HashMap<String, ArrayList<String>> buildConceptEventMap (HashMap<String, CompositeEvent> myCompositeEvents) {
        HashMap<String, ArrayList<String>> conceptEventMap = new HashMap<String, ArrayList<String>>();
        Set keySet = myCompositeEvents.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            CompositeEvent myCompositeEvent = myCompositeEvents.get(key);
            if (MATCHTYPE.equalsIgnoreCase("ililemma") || MATCHTYPE.equalsIgnoreCase("ili")) {
                ArrayList<KafSense> iliReferences = getILIreferences(myCompositeEvent.getEvent());
                if (LCS) {
                    ArrayList<KafSense> lcsReferences = getLcsILIreferences(myCompositeEvent.getEvent());
                    Util.addNewReferences(lcsReferences, iliReferences);
                    //iliReferences.addAll(lcsReferences);
                }
                if (HYPERS) {
                    ArrayList<KafSense> hyperReferences = getHyperILIreferences(myCompositeEvent.getEvent());
                    Util.addNewReferences(hyperReferences, iliReferences);
                   // iliReferences.addAll(hyperReferences);
                }
                if (iliReferences.size() > 0) {
                    for (int j = 0; j < iliReferences.size(); j++) {
                        KafSense kafSense = iliReferences.get(j);
                        if (conceptEventMap.containsKey(kafSense.getSensecode())) {
                            ArrayList<String> eventMapIds = conceptEventMap.get(kafSense.getSensecode());
                            if (!eventMapIds.contains(key)) {
                                eventMapIds.add(key);
                                conceptEventMap.put(kafSense.getSensecode(), eventMapIds);
                            }
                        }
                        else {
                            ArrayList<String> eventMapIds = new ArrayList<String>();
                            eventMapIds.add(key);
                            conceptEventMap.put(kafSense.getSensecode(), eventMapIds);
                        }
                    }
                }
            }
            if ((MATCHTYPE.equalsIgnoreCase("ililemma") || MATCHTYPE.equalsIgnoreCase("lemma"))) {
                ArrayList<String> phrases = myCompositeEvent.getEvent().getUniquePhrases();
                for (int j = 0; j < phrases.size(); j++) {
                    String phrase = phrases.get(j);
                    if (conceptEventMap.containsKey(phrase)) {
                        ArrayList<String> eventMapIds = conceptEventMap.get(phrase);
                        if (!eventMapIds.contains(key)) {
                            eventMapIds.add(key);
                            conceptEventMap.put(phrase, eventMapIds);
                        }
                    }
                    else {
                        ArrayList<String> eventMapIds = new ArrayList<String>();
                        eventMapIds.add(key);
                        conceptEventMap.put(phrase, eventMapIds);
                    }
                }
            }
        }
        return conceptEventMap;
    }

    static void chaining3WithoutBreak (HashMap<String, CompositeEvent> allCompositeEvents,
                           HashMap<String, ArrayList<String>> conceptEventMap,
                           ArrayList<String> eventIds,
                           int phraseMatchThreshold,
                           int conceptMatchThreshold,
                           ArrayList<String> roleNeededArrayList) {
        if (DEBUG==1) System.out.println("eventIds = " + eventIds.size());
        ArrayList<String> modifiedEvents = new ArrayList<String>();
        ArrayList<String> processedEvents = new ArrayList<String>();
        for (int i = 0; i < eventIds.size(); i++) {
            String eventId = eventIds.get(i);
            /// make sure we do not do duplicate work and do not compare the event with itself
            if (!processedEvents.contains(eventId)) {
                processedEvents.add(eventId);
            }
            CompositeEvent myCompositeEvent = allCompositeEvents.get(eventId);
            if (myCompositeEvent==null) {
                continue;
            }
            if (DEBUG>1) System.out.println("eventId = " + eventId);
            String mergedEventId = "";

            /// We first match this event with all other events that have the same ILI references
            if (MATCHTYPE.equalsIgnoreCase("ililemma") || MATCHTYPE.equalsIgnoreCase("ili")) {
                ArrayList<KafSense> iliReferences = getILIreferences(myCompositeEvent.getEvent());
                if (LCS) {
                    ArrayList<KafSense> lcsReferences = getLcsILIreferences(myCompositeEvent.getEvent());
                    Util.addNewReferences(lcsReferences, iliReferences);
                    //iliReferences.addAll(lcsReferences);
                }
                if (HYPERS) {
                    ArrayList<KafSense> hyperReferences = getHyperILIreferences(myCompositeEvent.getEvent());
                    Util.addNewReferences(hyperReferences, iliReferences);
                    //iliReferences.addAll(hyperReferences);
                }
                if (DEBUG>0) {
                    System.out.println("iliReferences = " + iliReferences.size());
                }
                if (iliReferences.size() > 0) {
                    ArrayList<String> eventMapIds = new ArrayList<String>();
                    for (int j = 0; j < iliReferences.size(); j++) {
                        KafSense kafSense = iliReferences.get(j);
                        if (conceptEventMap.containsKey(kafSense.getSensecode())) {
                            /// we get the list of event Ids to which the sensecode gives access but take the difference with the list of eventIds already processed
                            ArrayList<String> newEventMapIds = Util.getDifference(conceptEventMap.get(kafSense.getSensecode()), processedEvents);
                            for (int k = 0; k < newEventMapIds.size(); k++) {
                                String id = newEventMapIds.get(k);
                                if (!eventMapIds.contains(id)) eventMapIds.add(id);
                            }
                        }
                        else {
                            ///we have a problem.....
                            System.out.println("No event in conceptEventMap for kafSense.getSensecode() = " + kafSense.getSensecode());
                        }
                    }
                    if (DEBUG>0) System.out.println("Targets events to compare based on ILI= " + eventMapIds.size());
                    mergedEventId = matchEvents(myCompositeEvent,
                            allCompositeEvents,
                            eventMapIds,
                            0,
                            conceptMatchThreshold,
                            phraseMatchThreshold,
                            roleNeededArrayList);
                    if (!mergedEventId.isEmpty()) {
                        /// we found another eventId that fits the identity criteria
                        //break;
                    }
                }
            }
            //// We do the same for the phrase based index. Note that the processedEvents list grows
            if (mergedEventId.isEmpty() && (MATCHTYPE.equalsIgnoreCase("ililemma") || MATCHTYPE.equalsIgnoreCase("lemma"))) {
                ArrayList<String> phrases = myCompositeEvent.getEvent().getUniquePhrases();
                ArrayList<String> eventMapIds = new ArrayList<String>();
                for (int j = 0; j < phrases.size(); j++) {
                    String phrase = phrases.get(j);
                    //System.out.println("phrase = " + phrase);
                    if (conceptEventMap.containsKey(phrase)) {
                        ArrayList<String> newEventMapIds = Util.getDifference(conceptEventMap.get(phrase), processedEvents);
                        for (int k = 0; k < newEventMapIds.size(); k++) {
                            String id = newEventMapIds.get(k);
                            if (!eventMapIds.contains(id)) eventMapIds.add(id);
                        }
                    } else {
                        ///we have a problem.....
                        System.out.println("No event for phrase = " + phrase);
                    }
                }
                if (DEBUG>0) System.out.println("Targets events to compare based on phrase = " + eventMapIds.size());

                mergedEventId = matchEvents(myCompositeEvent,
                        allCompositeEvents,
                        eventMapIds, 0,
                        conceptMatchThreshold,
                        phraseMatchThreshold,
                        roleNeededArrayList);
                if (!mergedEventId.isEmpty()) {
                   // break;
                }
            }
            if (mergedEventId.isEmpty()) {
                //if (DEBUG==2) System.out.println("NO MATCH");
            }
            else {
                if (!modifiedEvents.contains(mergedEventId)) modifiedEvents.add(mergedEventId);
                /// for debugging
                if (CROSSDOC) {
                    if (!crossDocCorefSet.contains(mergedEventId)) crossDocCorefSet.add(mergedEventId);
                }
               // if (DEBUG==2) System.out.println("MATCH");
                //System.out.println("mergedEventId = " + mergedEventId);
            }
            if (DEBUG>1) System.out.println("Event i = " + i);
        }

        if (modifiedEvents.size()>0) {
            if (DEBUG>1) System.out.println("matched events = " + modifiedEvents.size());
            /// something was merged so we need to compare again
            ///iterate
            //System.out.println("ITERATING:"+modifiedEvents.size());
            chaining3WithoutBreak(allCompositeEvents, conceptEventMap, modifiedEvents, phraseMatchThreshold, conceptMatchThreshold, roleNeededArrayList);
        }
        else {
            /// no merge so no change and we are done
        }
    }

    static void chaining3 (HashMap<String, CompositeEvent> allCompositeEvents,
                           HashMap<String, ArrayList<String>> conceptEventMap,
                           ArrayList<String> eventIds,
                           int phraseMatchThreshold,
                           int conceptMatchThreshold,
                           ArrayList<String> roleNeededArrayList) {
        if (DEBUG>0) System.out.println("eventIds = " + eventIds.size());
        ArrayList<String> modifiedEvents = new ArrayList<String>();
        ArrayList<String> processedEvents = new ArrayList<String>();
        for (int i = 0; i < eventIds.size(); i++) {
            String eventId = eventIds.get(i);
            /// make sure we do not do duplicate work and do not compare the event with itself
            if (!processedEvents.contains(eventId)) {
                processedEvents.add(eventId);
            }
            CompositeEvent myCompositeEvent = allCompositeEvents.get(eventId);
            if (myCompositeEvent==null) {
                continue;
            }
            if (DEBUG>1) System.out.println("eventId = " + eventId);
            String mergedEventId = "";

            /// We first match this event with all other events that have the same ILI references
            if (MATCHTYPE.equalsIgnoreCase("ililemma") || MATCHTYPE.equalsIgnoreCase("ili")) {
                ArrayList<KafSense> iliReferences = getILIreferences(myCompositeEvent.getEvent());
                if (LCS) {
                    ArrayList<KafSense> lcsReferences = getLcsILIreferences(myCompositeEvent.getEvent());
                    Util.addNewReferences(lcsReferences, iliReferences);
                    //iliReferences.addAll(lcsReferences);
                }
                if (HYPERS) {
                    ArrayList<KafSense> hyperReferences = getHyperILIreferences(myCompositeEvent.getEvent());
                    Util.addNewReferences(hyperReferences, iliReferences);
                    //iliReferences.addAll(hyperReferences);
                }
                if (DEBUG>0) {
                    System.out.println("iliReferences = " + iliReferences.size());
                }
                if (iliReferences.size() > 0) {
                    ArrayList<String> eventMapIds = new ArrayList<String>();
                    for (int j = 0; j < iliReferences.size(); j++) {
                        KafSense kafSense = iliReferences.get(j);
                        if (conceptEventMap.containsKey(kafSense.getSensecode())) {
                            /// we get the list of event Ids to which the sensecode gives access but take the difference with the list of eventIds already processed
                            /// event already processed were already rejected by the matching criteria and do not need to be checked again
                            /// in this way we only check events once even if they have more than one ili reference as overlap
                            ArrayList<String> newEventMapIds = Util.getDifference(conceptEventMap.get(kafSense.getSensecode()), processedEvents);
                            for (int k = 0; k < newEventMapIds.size(); k++) {
                                String id = newEventMapIds.get(k);
                                if (!eventMapIds.contains(id)) eventMapIds.add(id);
                            }
                        }
                        else {
                            ///we have a problem.....
                            System.out.println("No event in conceptEventMap for kafSense.getSensecode() = " + kafSense.getSensecode());
                        }
                    }
                    if (DEBUG>0) System.out.println("Targets events to compare based on ILI= " + eventMapIds.size());
                    mergedEventId = matchEvents(myCompositeEvent,
                            allCompositeEvents,
                            eventMapIds,
                            0,
                            conceptMatchThreshold,
                            phraseMatchThreshold,
                            roleNeededArrayList);
                    if (!mergedEventId.isEmpty()) {
                        /// we found another eventId that fits the identity criteria
                        /// we have a match and the event is absorbed, no need to continue
                        if (DEBUG>0) {
                            System.out.println("SYNSET MATCH");
                            System.out.println("mergedEventId = " + mergedEventId);
                        }
                    }
                    else {
                        if (DEBUG>0) {
                            System.out.println("NO MATCH");
                        }
                    }
                }
            }
            //// We do the same for the phrase based index. Note that the processedEvents list grows
            if (mergedEventId.isEmpty() && (MATCHTYPE.equalsIgnoreCase("ililemma") || MATCHTYPE.equalsIgnoreCase("lemma"))) {
                ArrayList<String> phrases = myCompositeEvent.getEvent().getUniquePhrases();
                ArrayList<String> eventMapIds = new ArrayList<String>();
                for (int j = 0; j < phrases.size(); j++) {
                    String phrase = phrases.get(j);
                    //System.out.println("phrase = " + phrase);
                    if (conceptEventMap.containsKey(phrase)) {
                        ArrayList<String> newEventMapIds = Util.getDifference(conceptEventMap.get(phrase), processedEvents);
                        for (int k = 0; k < newEventMapIds.size(); k++) {
                            String id = newEventMapIds.get(k);
                            if (!eventMapIds.contains(id)) eventMapIds.add(id);
                        }
                    } else {
                        ///we have a problem.....
                        System.out.println("No event for phrase = " + phrase);
                    }
                }
                if (DEBUG>0) System.out.println("Targets events to compare based on phrase = " + eventMapIds.size());

                mergedEventId = matchEvents(myCompositeEvent,
                        allCompositeEvents,
                        eventMapIds, 0,
                        conceptMatchThreshold,
                        phraseMatchThreshold,
                        roleNeededArrayList);
                if (!mergedEventId.isEmpty()) {
                    /// we have a match and the event is absorbed, no need to continue
                    if (DEBUG>0) {
                        System.out.println("LEMMA MATCH");
                        System.out.println("mergedEventId = " + mergedEventId);
                    }
                }
            }
            if (mergedEventId.isEmpty()) {
                if (DEBUG>1) System.out.println("NO MATCH");
            }
            else {
                if (!modifiedEvents.contains(mergedEventId)) modifiedEvents.add(mergedEventId);
                /// for debugging
                if (CROSSDOC) {
                    if (!crossDocCorefSet.contains(mergedEventId)) crossDocCorefSet.add(mergedEventId);
                }
                if (DEBUG>1) {
                    System.out.println("MATCH");
                    System.out.println("mergedEventId = " + mergedEventId);
                }
            }
            if (DEBUG>1) System.out.println("Event i = " + i);
        }

        if (modifiedEvents.size()>0) {
            if (DEBUG>1) System.out.println("matched events = " + modifiedEvents.size());
            /// something was merged so we need to compare again
            ///iterate
            if (DEBUG>1) System.out.println("ITERATING:"+modifiedEvents.size());
            chaining3(allCompositeEvents, conceptEventMap, modifiedEvents, phraseMatchThreshold, conceptMatchThreshold, roleNeededArrayList);
        }
        else {
            /// no merge so no change and we are done
        }
    }

    static void chaining4 (HashMap<String, CompositeEvent> allCompositeEvents,
                           HashMap<String, ArrayList<String>> conceptEventMap,
                           ArrayList<String> eventIds,
                           int phraseMatchThreshold,
                           int conceptMatchThreshold,
                           ArrayList<String> roleNeededArrayList) {
        if (DEBUG==1) System.out.println("eventIds = " + eventIds.size());
        ArrayList<String> modifiedEvents = new ArrayList<String>();
        ArrayList<String> processedEvents = new ArrayList<String>();
        for (int i = 0; i < eventIds.size(); i++) {
            String eventId = eventIds.get(i);
            /// make sure we do not do duplicate work and do not compare the event with itself
            if (!processedEvents.contains(eventId)) {
                processedEvents.add(eventId);
            }
            CompositeEvent myCompositeEvent = allCompositeEvents.get(eventId);
            if (myCompositeEvent==null) {
                continue;
            }
            //System.out.println("eventId = " + eventId);
            boolean MATCH = false;

            /// We first match this event with all other events that have the same ILI references
            ArrayList<KafSense> iliReferences = getILIreferences(myCompositeEvent.getEvent());
            if (MATCHTYPE.equalsIgnoreCase("ililemma") || MATCHTYPE.equalsIgnoreCase("ili")) {
                if (LCS) {
                    ArrayList<KafSense> lcsReferences = getLcsILIreferences(myCompositeEvent.getEvent());
                    iliReferences.addAll(lcsReferences);
                }
                if (HYPERS) {
                    ArrayList<KafSense> hyperReferences = getHyperILIreferences(myCompositeEvent.getEvent());
                    iliReferences.addAll(hyperReferences);
                }
                if (DEBUG==1) {
                    System.out.println("iliReferences = " + iliReferences.size());
                }
                if (iliReferences.size() > 0) {
                    ArrayList<String> eventMapIds = new ArrayList<String>();

                    for (int j = 0; j < iliReferences.size(); j++) {
                        KafSense kafSense = iliReferences.get(j);
                        if (conceptEventMap.containsKey(kafSense.getSensecode())) {
                            /// we get the list of event Ids to which the sensecode gives access but take the difference with the list of eventIds already processed
                            ArrayList<String> newEventMapIds = Util.getDifference(conceptEventMap.get(kafSense.getSensecode()), processedEvents);
                            for (int k = 0; k < newEventMapIds.size(); k++) {
                                String id = newEventMapIds.get(k);
                                if (!eventMapIds.contains(id)) eventMapIds.add(id);
                            }
                        }
                        else {
                            ///we have a problem.....
                            System.out.println("No event in conceptEventMap for kafSense.getSensecode() = " + kafSense.getSensecode());
                        }
                    }
                    ArrayList<String> swallowedEvents = matchAndSwallowEvents(myCompositeEvent,
                            allCompositeEvents,
                            eventMapIds,
                            0,
                            conceptMatchThreshold,
                            phraseMatchThreshold,
                            roleNeededArrayList);
                    if (swallowedEvents.size()>0) {
                        MATCH=true;
                        for (int k = 0; k < swallowedEvents.size(); k++) {
                            String swallowedEventId = swallowedEvents.get(k);
                            processedEvents.add(swallowedEventId);
                        }
                    }
                }
                else {
                  //////
                }
            }


            //// We do the same for the phrase based index. Note that the processedEvents list grows
            if (!MATCH && iliReferences.size()==0 && (MATCHTYPE.equalsIgnoreCase("ililemma") || MATCHTYPE.equalsIgnoreCase("lemma"))) {
                ArrayList<String> phrases = myCompositeEvent.getEvent().getUniquePhrases();
                ArrayList<String> eventMapIds = new ArrayList<String>();
                for (int j = 0; j < phrases.size(); j++) {
                    String phrase = phrases.get(j);
                    //System.out.println("phrase = " + phrase);
                    if (conceptEventMap.containsKey(phrase)) {
                        ArrayList<String> newEventMapIds = Util.getDifference(conceptEventMap.get(phrase), processedEvents);
                        for (int k = 0; k < newEventMapIds.size(); k++) {
                            String id = newEventMapIds.get(k);
                            if (!eventMapIds.contains(id)) eventMapIds.add(id);
                        }
                    } else {
                        ///we have a problem.....
                        System.out.println("No event for phrase = " + phrase);
                    }
                }
                ArrayList<String> swallowedEvents = matchAndSwallowEvents(myCompositeEvent,
                        allCompositeEvents,
                        eventMapIds, 0,
                        conceptMatchThreshold,
                        phraseMatchThreshold,
                        roleNeededArrayList);
                if (swallowedEvents.size()>0) {
                    MATCH=true;
                    for (int k = 0; k < swallowedEvents.size(); k++) {
                        String swallowedEventId = swallowedEvents.get(k);
                        processedEvents.add(swallowedEventId);
                    }
                }
            }

            //////////////
            if (!MATCH) {
               // if (DEBUG==2) System.out.println("NO MATCH");
            }
            else {
                if (!modifiedEvents.contains(myCompositeEvent.getEvent().getId())) modifiedEvents.add(myCompositeEvent.getEvent().getId());
                /// for debugging
                if (CROSSDOC) {
                    if (!crossDocCorefSet.contains(myCompositeEvent.getEvent().getId())) crossDocCorefSet.add(myCompositeEvent.getEvent().getId());
                }
              //  if (DEBUG==2) System.out.println("MATCH");
                //System.out.println("mergedEventId = " + mergedEventId);
            }
        }

        if (modifiedEvents.size()>0) {
            if (DEBUG>0) System.out.println("matched events = " + modifiedEvents.size());
            /// something was merged so we need to compare again
            ///iterate
            if (DEBUG>0) System.out.println("ITERATING:"+modifiedEvents.size());
            chaining4(allCompositeEvents, conceptEventMap, modifiedEvents, phraseMatchThreshold, conceptMatchThreshold, roleNeededArrayList);
        }
        else {
            /// no merge so no change and we are done
        }
    }

    static void chaining2 (HashMap<String, CompositeEvent> myCompositeEvents,
                           ArrayList<String> eventIds,
                           ArrayList<String> eventMapIds,
                           int phraseMatchThreshold,
                           int conceptMatchThreshold,
                           ArrayList<String> roleNeededArrayList) {
        ArrayList<String> modifiedEvents = new ArrayList<String>();
        ArrayList<String> remainingEvents = new ArrayList<String>();
        for (int i = 0; i < eventIds.size(); i++) {
            String mergedEventId = "";
            String eventId = eventIds.get(i);
            if (!myCompositeEvents.containsKey(eventId)) {
                continue;
            }
            CompositeEvent myCompositeEvent = myCompositeEvents.get(eventId);
            if (myCompositeEvent==null) {
                continue;
            }
            mergedEventId = matchEvents(myCompositeEvent, myCompositeEvents, eventMapIds, i+1, conceptMatchThreshold, phraseMatchThreshold, roleNeededArrayList);
            if (mergedEventId.isEmpty()) {
                if (DEBUG==2) System.out.println("NO MATCH");
                remainingEvents.add(eventId);
            }
            else {
                if (!modifiedEvents.contains(mergedEventId)) modifiedEvents.add(mergedEventId);
                if (DEBUG==1) System.out.println("MATCH");
            }
        }
        if (modifiedEvents.size()>0) {
            /// something was merged so we need to compare again
            ///iterate
            // System.out.println("ITERATING:"+modifiedEvents.size());
            chaining2(myCompositeEvents, modifiedEvents, remainingEvents, phraseMatchThreshold, conceptMatchThreshold, roleNeededArrayList);
        }
        else {
            /// no merge so no change and we are done
        }
    }


    static void chaining1 (HashMap<String, CompositeEvent> myCompositeEvents,
                           ArrayList<String> eventIds,
                           int phraseMatchThreshold,
                           int conceptMatchThreshold,
                           ArrayList<String> roleNeededArrayList) {
        ArrayList<String> modifiedEvents = new ArrayList<String>();
        for (int i = 0; i < eventIds.size(); i++) {
            boolean match = false;
            String eventId = eventIds.get(i);
            CompositeEvent myCompositeEvent = myCompositeEvents.get(eventId);
            if (myCompositeEvent==null) {
                continue;
            }
            Set keySet = myCompositeEvents.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key.equals(eventId)) {
                    continue;
                }
                if (!myCompositeEvents.containsKey(key)) {
                    continue;
                }
                CompositeEvent targetEvent = myCompositeEvents.get(key);
                if (targetEvent==null) {
                    continue;
                }
                boolean EVENTMATCH = false;

                if (MATCHTYPE.equalsIgnoreCase("lemma")) {
                    if (ComponentMatch.compareEventLabelReference(myCompositeEvent, targetEvent, phraseMatchThreshold)) {
                        EVENTMATCH = true;
                    }
                }
                else {
                    //// it should be ILI or ILILEMMA
                    //// We then first check the ILI matches
                    if (getILIreferences(myCompositeEvent.getEvent()).size() > 0 &&
                            getILIreferences(targetEvent.getEvent()).size() > 0) {
                        //// we first check the synsets if there are any
                        //// different senses of the same word are not considered a match
                        if (ComponentMatch.compareEventWordNetReference(myCompositeEvent, targetEvent, conceptMatchThreshold)) {
                            EVENTMATCH = true;
                        }
                    } else {
                        if (MATCHTYPE.equalsIgnoreCase("ililemma")) {
                            /// if one of the two or both have no synsets then we compare the lemmas
                            if (ComponentMatch.compareEventLabelReference(myCompositeEvent, targetEvent, phraseMatchThreshold)) {
                                EVENTMATCH = true;
                            }
                        }
                    }
                }
                if (EVENTMATCH) {
                    if (ComponentMatch.compareCompositeEvent(myCompositeEvent, targetEvent, roleNeededArrayList, DEBUG>0)) {
                        match = true;
                        targetEvent.getEvent().mergeSemObject(myCompositeEvent.getEvent());
                        targetEvent.mergeObjects(myCompositeEvent);
                        targetEvent.mergeRelations(myCompositeEvent);
                       // myCompositeEvents.put(eventId, null);
                        myCompositeEvents.remove(eventId);
                        modifiedEvents.add(targetEvent.getEvent().getId());
                        break;
                    }
                }
            }
            if (!match) {
                if (DEBUG==2) System.out.println("NO MATCH");
            }
            else {
               // if (!modifiedEvents.contains(mergedEventId)) modifiedEvents.add(mergedEventId);
                if (DEBUG==2) System.out.println("MATCH");
            }
        }
        if (modifiedEvents.size()>0) {
            /// something was merged so we need to compare again
            ///iterate
            // System.out.println("ITERATING:"+modifiedEvents.size());
            chaining1(myCompositeEvents, modifiedEvents, phraseMatchThreshold, conceptMatchThreshold, roleNeededArrayList);
        }
        else {
            /// no merge so no change and we are done
        }
    }


    static void saveCompositeEvent (CompositeEvent compositeEvent, File targetFile) throws IOException {
        if (targetFile!=null && targetFile.exists()) {
            OutputStream os = new FileOutputStream(targetFile, true);
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
        else if (targetFile!=null) {
            OutputStream os = new FileOutputStream(targetFile);
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
            System.out.println("Could not create the naf.object file in:"+targetFile);
        }
    }
}
