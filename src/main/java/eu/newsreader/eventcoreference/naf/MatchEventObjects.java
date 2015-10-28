package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSense;
import eu.newsreader.eventcoreference.coref.ComponentMatch;
import eu.newsreader.eventcoreference.objects.CompositeEvent;
import eu.newsreader.eventcoreference.objects.SemObject;
import eu.newsreader.eventcoreference.objects.SourceMeta;
import eu.newsreader.eventcoreference.output.JenaSerialization;
import eu.newsreader.eventcoreference.util.ReadSourceMetaFile;
import eu.newsreader.eventcoreference.util.Util;
import vu.wntools.wordnet.WordnetData;
import vu.wntools.wordnet.WordnetLmfSaxParser;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/14/13
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class MatchEventObjects {

    static boolean DEBUG = false;
    public static String MATCHTYPE= "ililemma";  // ili OR lemma OR ililemma OR none OR split
    public static boolean LCS = false;
    public static boolean ILIURI = false;
    public static boolean VERBOSEMENTIONS = false;
    public static String CHAINING = "3";

    static final String usage = "MatchEventObjects reads obj files stored in time-folders with CompositeEventObjects and outputs a single RDF-TRiG file\n" +
            "The parameters are:\n" +
            "--event-folder  <path>     <Path to the event folder that has subfolders for each time-description, e.g. \"e-2012-03-29\". Object file (*.obj) should be stored in these subfolders\n" +
            "--wordnet-lmf   <path>     <(OPTIONAL, not yet used) Path to a WordNet-LMF file\n>" +
            "--concept-match <int>      <threshold for conceptual matches of events, default is 50>\n" +
            "--phrase-match  <int>      <threshold for phrase matches of events, default is 50>\n" +
            "--lcs                      <(OPTIONAL, not used yet) use lowest-common-subsumers\n"+
            "--match-type    <string>   <(OPTIONAL) Indicates what is used to match events across resources. Default value is \"LEMMA\". Values:\"LEMMA\", \"ILI\", \"ILILEMMA\">\n" +
            "--ili                  <(OPTIONAL) Path to ILI.ttl file to convert wordnet-synsets identifiers to ILI identifiers>\n" +
            "--source-data   <path>     <(OPTIONAL, Deprecated) Path to LexisNexis meta data on owners and authors to enrich the provenance>\n" +
            "--roles  <string>          <String with PropbBank roles, separated by \",\" for which there minimally needs to be a match, e.g. \"a0,a1\". This is especially relevant for sourceEvent, grammaticalEvent. If value is \"all\", then all participants need to match. This can be used for futureEvent"+
            "--verbose                  <(OPTIONAL) representation of mentions is extended with token ids, terms ids and sentence number\n"+
            "--ili-uri                  <(OPTIONAL) If used, the ILI-identifiers are used to represents events. This is necessary for cross-lingual extraction>\n" +
            "--debug                    <(OPTIONAL)>\n";


    static public void main (String [] args) {
        ArrayList<String> roleNeededArrayList = new ArrayList<String>();
        HashMap<String, SourceMeta> sourceMetaHashMap = null;
        WordnetData wordnetData = null;
        int conceptMatchThreshold = 50;
        int phraseMatchThreshold = 50;
        String pathToEventFolder = "";
        String pathToSourceDataFile = "";

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
            else if (arg.equals("--debug")) {
                DEBUG = true;
            }
            else if (arg.equals("--verbose")) {
                VERBOSEMENTIONS = true;
            }
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
        }
        return iliReferences;
    }


    /**
     * if MATCHTYPE==none No comparison and events are serialized to separate TRiG files per object file
     * @param pathToEventFolder
     */
    public static void serializeEventFoldersHashMap (File pathToEventFolder) {
        HashMap<String, CompositeEvent> events = new HashMap<String, CompositeEvent>();
        ArrayList<File> eventFolders = Util.makeFolderList(pathToEventFolder);
        for (int f = 0; f < eventFolders.size(); f++) {
            File nextEventFolder =  eventFolders.get(f);
            try {
                ArrayList<File> files = Util.makeRecursiveFileList(nextEventFolder, ".obj");
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    String filePath = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("."))+"sem.trig";
                    OutputStream fos = new FileOutputStream(filePath);
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
        HashMap<String, CompositeEvent> events = new HashMap<String, CompositeEvent>();
        ArrayList<File> eventFolders = Util.makeFolderList(pathToEventFolder);
        if (DEBUG) System.out.println("eventFolders.size() = " + eventFolders.size());
        for (int f = 0; f < eventFolders.size(); f++) {
            File nextEventFolder =  eventFolders.get(f);
            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                System.out.println("Start reading obj files:"+dateFormat.format(date));
                OutputStream fos = new FileOutputStream(nextEventFolder.getAbsolutePath()+"/sem.trig");

                /// events is initialised outside the loop so that events are compared against the total list
                events = new HashMap<String, CompositeEvent>();
                ArrayList<File> files = Util.makeRecursiveFileList(nextEventFolder, ".obj");
                //if (DEBUG)
                    System.out.println("files.size() = " + files.size());
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                  //  System.out.println("file.getName() = " + file.getName());
                    readCompositeEventArrayListFromObjectFile(file, events);
                  //  System.out.println("events.size() = " + events.size());
                }
                /// we create a =n ArrayList with the event ids so that we can call the recursive chaining function
                ArrayList<String> eventIds = new ArrayList<String>();
                Set keySet = events.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String id = keys.next();
                    eventIds.add(id);
                }

                date = new Date();
                System.out.println("End reading object files:"+dateFormat.format(date));

                //if (DEBUG) 
                    System.out.println("events before chaining = " + events.size());
                if (CHAINING.equals("1")) {
                    chaining1(events, eventIds,
                            phraseMatchThreshold,
                            conceptMatchThreshold,
                            roleNeededArrayList);
                }
                else if (CHAINING.equals("2")) {
                                    chaining2(events, eventIds, eventIds,
                                            phraseMatchThreshold,
                                            conceptMatchThreshold,
                                            roleNeededArrayList);
                }
                else if (CHAINING.equals("3")) {
                    HashMap<String, ArrayList<String>> conceptEventMap = buildConceptEventMap(events);
                    System.out.println("conceptEventMap.size() = " + conceptEventMap.size());

                                    chaining3(events, conceptEventMap, eventIds,
                                            phraseMatchThreshold,
                                            conceptMatchThreshold,
                                            roleNeededArrayList);
                }
                //if (DEBUG)
                System.out.println("events after chaining = " + events.size());
                date = new Date();
                System.out.println("End chaining:"+dateFormat.format(date));

                JenaSerialization.serializeJenaSingleCompositeEvents(fos,
                        events,
                        sourceMetaHashMap,
                        ILIURI,
                        VERBOSEMENTIONS);
                date = new Date();
                System.out.println("End writing sem.trig:"+dateFormat.format(date));
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }



    public static void readCompositeEventArrayListFromObjectFile (File file, HashMap<String,CompositeEvent> events) {
        if (file.exists() ) {
            int cnt = 0;
            if (DEBUG) System.out.println("file = " + file.getName());
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois =  new ObjectInputStream(fis);
                Object obj = null;
                while (fis.available()>0) {
                    while ((obj = ois.readObject()) != null) {
                        cnt++;
                        if (obj instanceof CompositeEvent) {
                            CompositeEvent compositeEvent = (CompositeEvent) obj;
                            if (DEBUG) System.out.println("compositeEvent.getEvent().getPhrase() = " + compositeEvent.getEvent().getPhrase());
                            events.put(compositeEvent.getEvent().getId(), compositeEvent);
                        } else {
                            if (DEBUG) System.out.println("Unknown object obj.getClass() = " + obj.getClass());
                        }
                    }
                    ois.reset();
                    ois.close();
                    fis.close();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    System.out.println("file = " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }
            if (DEBUG) System.out.println(file.getName()+" nr objects read = " + cnt);
        }
    }

    static String matchEvents (CompositeEvent myCompositeEvent,
                                HashMap<String, CompositeEvent> myCompositeEvents,
                                ArrayList<String> eventMapIds,
                                int idx,
                                int phraseMatchThreshold,
                                int conceptMatchThreshold,
                                ArrayList<String> roleNeededArrayList) {
        String mergedEventId = "";
        for (int j = idx; j < eventMapIds.size(); j++) {
            String targetEventId = eventMapIds.get(j);
            if (!myCompositeEvents.containsKey(targetEventId)) {
                continue;
            }
            CompositeEvent targetEvent = myCompositeEvents.get(targetEventId);
            if (targetEvent == null) {
                continue;
            }
            boolean EVENTMATCH = false;

            if (MATCHTYPE.equalsIgnoreCase("lemma")) {
                if (ComponentMatch.compareEventLabelReference(myCompositeEvent, targetEvent, phraseMatchThreshold)) {
                    EVENTMATCH = true;
                }
            } else {
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
                if (ComponentMatch.compareCompositeEvent(myCompositeEvent, targetEvent, roleNeededArrayList)) {
                    targetEvent.getEvent().mergeSemObject(myCompositeEvent.getEvent());
                    targetEvent.mergeObjects(myCompositeEvent);
                    targetEvent.mergeRelations(myCompositeEvent);
                    // myCompositeEvents.put(eventId, null);
                    myCompositeEvents.remove(myCompositeEvent.getEvent().getId());
                    mergedEventId = targetEvent.getEvent().getId();
                    break;
                }
            }
        }
        return mergedEventId;
    }

    static HashMap<String, ArrayList<String>> buildConceptEventMap (HashMap<String, CompositeEvent> myCompositeEvents) {
        HashMap<String, ArrayList<String>> conceptEventMap = new HashMap<String, ArrayList<String>>();
        Set keySet = myCompositeEvents.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            CompositeEvent myCompositeEvent = myCompositeEvents.get(key);
            if (MATCHTYPE.equalsIgnoreCase("ililemma") || MATCHTYPE.equals("ili")) {
                ArrayList<KafSense> iliReferences = getILIreferences(myCompositeEvent.getEvent());
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
            if ((MATCHTYPE.equals("ililemma") || MATCHTYPE.equals("lemma"))) {
                ArrayList<String> phrases = myCompositeEvent.getEvent().getUniquePhrases();
                for (int j = 0; j < phrases.size(); j++) {
                    String phrase = phrases.get(j);
                    if (conceptEventMap.containsKey(phrase)) {
                        ArrayList<String> eventMapIds = conceptEventMap.get(phrase);
                        if (eventMapIds.contains(key)) {
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

    static void chaining3 (HashMap<String, CompositeEvent> myCompositeEvents,
                           HashMap<String, ArrayList<String>> conceptEventMap,
                           ArrayList<String> eventIds,
                           int phraseMatchThreshold,
                           int conceptMatchThreshold,
                           ArrayList<String> roleNeededArrayList) {
        ArrayList<String> modifiedEvents = new ArrayList<String>();
        ArrayList<String> processedEvents = new ArrayList<String>();
        for (int i = 0; i < eventIds.size(); i++) {
            String eventId = eventIds.get(i);
            if (!processedEvents.contains(eventId)) {
                processedEvents.add(eventId);
            }
            CompositeEvent myCompositeEvent = myCompositeEvents.get(eventId);
            if (myCompositeEvent==null) {
                continue;
            }
            String mergedEventId = "";
            if (MATCHTYPE.equalsIgnoreCase("ililemma") || MATCHTYPE.equals("ili")) {
                ArrayList<KafSense> iliReferences = getILIreferences(myCompositeEvent.getEvent());
                if (iliReferences.size() > 0) {
                    for (int j = 0; j < iliReferences.size(); j++) {
                        KafSense kafSense = iliReferences.get(j);
                        if (conceptEventMap.containsKey(kafSense.getSensecode())) {
                            ArrayList<String> eventMapIds = Util.getDifference(conceptEventMap.get(kafSense.getSensecode()), processedEvents);
                        //    System.out.println("eventMapIds.size() = " + eventMapIds.size());
                            mergedEventId = matchEvents(myCompositeEvent, myCompositeEvents, eventMapIds, 0, phraseMatchThreshold, conceptMatchThreshold, roleNeededArrayList);
                            if (!mergedEventId.isEmpty()) {
                                break;
                            }
                        } else {
                            ///we have a problem.....
                            System.out.println("No event for kafSense.getSensecode() = " + kafSense.getSensecode());
                        }
                    }
                }
            }

            if (mergedEventId.isEmpty() && (MATCHTYPE.equals("ililemma") || MATCHTYPE.equals("lemma"))) {
                ArrayList<String> phrases = myCompositeEvent.getEvent().getUniquePhrases();
                for (int j = 0; j < phrases.size(); j++) {
                    String phrase = phrases.get(j);
                    if (conceptEventMap.containsKey(phrase)) {
                        ArrayList<String> eventMapIds = Util.getDifference(conceptEventMap.get(phrase), processedEvents);
                      //  System.out.println("eventMapIds.size() = " + eventMapIds.size());
                        mergedEventId = matchEvents(myCompositeEvent, myCompositeEvents, eventMapIds, 0, phraseMatchThreshold, conceptMatchThreshold, roleNeededArrayList);
                        if (!mergedEventId.isEmpty()) {
                            break;
                        }
                    } else {
                        ///we have a problem.....
                        System.out.println("No event for phrase = " + phrase);
                    }
                }
            }
            if (mergedEventId.isEmpty()) {
                if (DEBUG) System.out.println("NO MATCH");
               // remainingEvents.add(eventId);
            }
            else {
                if (!modifiedEvents.contains(mergedEventId)) modifiedEvents.add(mergedEventId);
                if (DEBUG)
                    System.out.println("MATCH");
            }
        }

        if (modifiedEvents.size()>0) {
            /// something was merged so we need to compare again
            ///iterate
            // System.out.println("ITERATING:"+modifiedEvents.size());
            chaining3(myCompositeEvents, conceptEventMap, modifiedEvents, phraseMatchThreshold, conceptMatchThreshold, roleNeededArrayList);
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
            mergedEventId = matchEvents(myCompositeEvent, myCompositeEvents, eventMapIds, i+1, phraseMatchThreshold, conceptMatchThreshold, roleNeededArrayList);
            if (mergedEventId.isEmpty()) {
                if (DEBUG) System.out.println("NO MATCH");
                remainingEvents.add(eventId);
            }
            else {
                if (!modifiedEvents.contains(mergedEventId)) modifiedEvents.add(mergedEventId);
                if (DEBUG) System.out.println("MATCH");
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

    //// OLD MATCHCING CODE
    /*            for (int j = i+1; j < eventMapIds.size(); j++) {
                String targetEventId = eventMapIds.get(j);
                CompositeEvent targetEvent = myCompositeEvents.get(targetEventId);
                if (targetEvent==null) {
                    continue;
                }
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

*//* THIS IS TOO RISKY
                    if (ComponentMatch.compareEventLCSReference(myCompositeEvent, targetEvent)) {
                        EVENTMATCH=true;
                    }
*//*

                if (EVENTMATCH) {
                    if (ComponentMatch.compareCompositeEvent(myCompositeEvent, targetEvent, roleNeededArrayList)) {
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
            }*/

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

/* THIS IS TOO RISKY
                    if (ComponentMatch.compareEventLCSReference(myCompositeEvent, targetEvent)) {
                        EVENTMATCH=true;
                    }
*/

                if (EVENTMATCH) {
                    if (ComponentMatch.compareCompositeEvent(myCompositeEvent, targetEvent, roleNeededArrayList)) {
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
                if (DEBUG) System.out.println("NO MATCH");
            }
            else {
               // if (!modifiedEvents.contains(mergedEventId)) modifiedEvents.add(mergedEventId);
                if (DEBUG) System.out.println("MATCH");
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

    static void splitting (HashMap<String, String> eventFolderMap,
                           HashMap<String, CompositeEvent> myCompositeEvents,
                           File parentEventFolder, String objectFileName) {
        Set keySet = myCompositeEvents.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            CompositeEvent myCompositeEvent = myCompositeEvents.get(key);
            if (myCompositeEvent == null) {
                continue;
            }
            ArrayList<KafSense> iliConcepts = getILIreferences(myCompositeEvent.getEvent());
            String targetFolderName = "";
            if (iliConcepts.size()>0) {
                ArrayList<String> targetFolderArrayList = new ArrayList<String>();
                for (int i = 0; i < iliConcepts.size(); i++) {
                    KafSense kafSense = iliConcepts.get(i);
                    if (eventFolderMap.containsKey(kafSense.getSensecode())) {
                        String folder = eventFolderMap.get(kafSense.getSensecode());
                        if (targetFolderArrayList.contains(folder)) {
                            targetFolderArrayList.add(folder);
                        }
                    }
                    else {
                        /// we pick one of the concepts to store it. It does not matter which one
                        targetFolderName = kafSense.getSensecode();
                    }
                }
                if (targetFolderArrayList.isEmpty()) {
                   //// there was nothing there so we create a target folder
                    if (targetFolderName.isEmpty()) {
                        System.out.println("WE HAVE NOT TARGET FOLDER TO STORE THIS. WE ARE LOSING INFORMATION....");
                    }
                    else {
                        String targetFolderPath = parentEventFolder+"/"+targetFolderName;
                        File targetFolder = new File(targetFolderPath);
                        if (!targetFolder.exists()) {
                            targetFolder.mkdir();
                        }
                        if (targetFolder.exists()) {
                            String targetFilePath = targetFolderPath + "/" + objectFileName;
                            File targetFile = new File(targetFilePath);
                            try {
                                saveCompositeEvent(myCompositeEvent, targetFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

/*            for (int i = 0; i < compositeEvent1.getEvent().getPhraseCounts().size(); i++) {
                PhraseCount phraseCount1 = compositeEvent1.getEvent().getPhraseCounts().get(i);
                for (int j = 0; j < compositeEvent2.getEvent().getPhraseCounts().size(); j++) {
                    PhraseCount phraseCount2 = compositeEvent2.getEvent().getPhraseCounts().get(j);
                    if (phraseCount1.getPhrase().equalsIgnoreCase(phraseCount2.getPhrase())) {
                        nMatches++;
                    }
                }
            }*/


            if (MATCHTYPE.equalsIgnoreCase("lemma")) {
            } else {
                    if (MATCHTYPE.equalsIgnoreCase("ililemma")) {
                    }
            }
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
