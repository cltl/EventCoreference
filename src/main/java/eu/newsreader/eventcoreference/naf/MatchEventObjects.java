package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSense;
import eu.newsreader.eventcoreference.coref.ComponentMatch;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.output.JenaSerialization;
import eu.newsreader.eventcoreference.util.ReadSourceMetaFile;
import eu.newsreader.eventcoreference.util.Util;
import vu.wntools.wordnet.WordnetData;
import vu.wntools.wordnet.WordnetLmfSaxParser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/14/13
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class MatchEventObjects {

    static boolean DEBUG = false;
    public static String MATCHTYPE= "ILILEMMA";  // ILI OR ILILEMMA
    public static boolean LCS = false;
    public static boolean ILIURI = false;
    public static boolean VERBOSEMENTIONS = false;
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
                JenaSerialization.iliReader.readILIFile(pathToILIFile);
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
      //  processEventFoldersSingleOutputFile(new File(pathToEventFolder), conceptMatchThreshold, phraseMatchThreshold, sourceMetaHashMap, wordnetData, eventType, roleArrayList);
      //  processEventFolders(new File(pathToEventFolder), conceptMatchThreshold, phraseMatchThreshold, sourceMetaHashMap, wordnetData, eventType, roleArrayList);
        processEventFoldersHashMap(new File(pathToEventFolder),
                conceptMatchThreshold,
                phraseMatchThreshold,
                sourceMetaHashMap,
                wordnetData,
                roleNeededArrayList);

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



    public static void processEventFoldersHashMap (File pathToEventFolder, int conceptMatchThreshold,
                                                            int phraseMatchThreshold,
                                                            HashMap<String, SourceMeta> sourceMetaHashMap,
                                                            WordnetData wordnetData,
                                                            ArrayList<String> roleNeededArrayList


                                                   ) {
        HashMap<String, CompositeEvent> events = new HashMap<String, CompositeEvent>();
        ArrayList<File> eventFolders = Util.makeFolderList(pathToEventFolder);
        for (int f = 0; f < eventFolders.size(); f++) {
            File nextEventFolder =  eventFolders.get(f);
            try {
                OutputStream fos = new FileOutputStream(nextEventFolder.getAbsolutePath()+"/sem.trig");

                /// events is initialised outside the loop so that events are compared against the total list
                events = new HashMap<String, CompositeEvent>();
                ArrayList<File> files = Util.makeRecursiveFileList(nextEventFolder, ".obj");
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
               // System.out.println("eventIds = " + eventIds.size());
                chaining(events, eventIds,
                        phraseMatchThreshold,
                        conceptMatchThreshold,
                        roleNeededArrayList);

                JenaSerialization.serializeJenaSingleCompositeEvents(fos,
                        events,
                        sourceMetaHashMap,
                        ILIURI,
                        VERBOSEMENTIONS);
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
                            if (DEBUG)
                                System.out.println("compositeEvent.getEvent().getPhrase() = " + compositeEvent.getEvent().getPhrase());
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
                //  System.out.println("file = " + file.getAbsolutePath());
                //   e.printStackTrace();
            }
            if (DEBUG) System.out.println(file.getName()+" nr objects read = " + cnt);
        }
    }


    static void chaining (HashMap<String, CompositeEvent> myCompositeEvents,
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
                    if (getILIreferences(myCompositeEvent.getEvent()).size() > 0 && getILIreferences(targetEvent.getEvent()).size() > 0) {
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
                        myCompositeEvents.put(eventId, null);
                        modifiedEvents.add(targetEvent.getEvent().getId());
                        break;
                    }
                }
            }
            if (!match) {
                if (DEBUG) System.out.println("NO MATCH");
            }
            else {

                if (DEBUG) System.out.println("MATCH");
            }
        }
        if (modifiedEvents.size()>0) {
            /// something was merged so we need to compare again
            ///iterate
            // System.out.println("ITERATING:"+modifiedEvents.size());
            chaining(myCompositeEvents, modifiedEvents, phraseMatchThreshold, conceptMatchThreshold, roleNeededArrayList);
        }
        else {
            /// no merge so no change and we are done
        }
    }
}
