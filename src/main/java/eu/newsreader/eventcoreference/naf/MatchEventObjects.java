package eu.newsreader.eventcoreference.naf;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.kyotoproject.kaf.KafSense;
import eu.newsreader.eventcoreference.coref.ComponentMatch;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.output.JenaSerialization;
import eu.newsreader.eventcoreference.util.ReadSourceMetaFile;
import eu.newsreader.eventcoreference.util.Util;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
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
    public static boolean VERBOSEMENTIONS = false;
    static final String usage = "MatchEventObjects reads obj files stored in time-folders with CompositeEventObjects and outputs a single RDF-TRiG file\n" +
            "The parameters are:\n" +
            "--event-folder  <path>     <Path to the event folder that has subfolders for each time-description, e.g. \"e-2012-03-29\". Object file (*.obj) should be stored in these subfolders\n" +
            "--wordnet-lmf   <path>     <(OPTIONAL, not yet used) Path to a WordNet-LMF file\n>" +
            "--concept-match <double>   <(OPTIONAL, not yet used) threshold for conceptual matches of events>\n" +
            "--phrase-match  <double>   <(OPTIONAL, not yet used) threshold for phrase matches of events>\n" +
            "--lcs                      <(OPTIONAL) use lowest-common-subsumers\n"+
            "--match-type    <string>   <(OPTIONAL) Indicates what is used to match events across resources. Default value is \"LEMMA\". Values:\"LEMMA\", \"ILI\", \"ILILEMMA\">\n" +
            "--ili-uri                  <(OPTIONAL) If used, the ILI-identifiers are used to represents events. This is necessary for cross-lingual extraction>\n" +
            "--event-type    <string>   <(OPTIONAL) Indicate the type of events for establishing event coreference more or less strict. Values are \"contetxual\", \"source\", \"grammatical\">\n" +
            "--source-data   <path>     <(OPTIONAL) Path to LexisNexis meta data on owners and authors to enrich the provenance>\n" +
            "--roles  <string>   <(OPTIONAL) String with PropbBank roles for which there needs to be a match, e.g. \"a1,a2,a3,a4\""+
            "--debug                    <(OPTIONAL)>\n";


    static public void main (String [] args) {

        String eventType = "";
        ArrayList<String> roleArrayList = new ArrayList<String>();
        HashMap<String, SourceMeta> sourceMetaHashMap = null;
        WordnetData wordnetData = null;
        double conceptMatchThreshold = 0;
        double phraseMatchThreshold = 1;
        String pathToEventFolder = "";
       // String pathToEventFolder = "/Users/piek/Desktop/NWR/NWR-DATA/cars/events/speech";
       // String pathToEventFolder = "/Users/piek/Desktop/NWR/NWR-DATA/cars/events/contextual";
       // String pathToEventFolder = "/Users/piek/Desktop/NWR/NWR-DATA/cars-2/1/events/source";
        //String pathToEventFolder = "/Users/piek/Desktop/NWR/NWR-ontology/reasoning/change-of-scale/events/contextual";
       // String pathToEventFolder = "/Users/piek/Desktop/NWR/NWR-DATA/cars/events/grammatical";
        //String pathToEventFolder = "/Code/vu/newsreader/EventCoreference/LN_football_test_out-tiny/events/other";
       //String pathToEventFolder = "/Code/vu/newsreader/EventCoreference/LN_football_test_out/events/other";
        String pathToSourceDataFile = "";
        //String pathToSourceDataFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v1_2014/resources/LN-coremetadata.txt";

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
                    conceptMatchThreshold = Double.parseDouble(args[i+1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else if (arg.equals("--phrase-match") && args.length>(i+1)) {
                try {
                    phraseMatchThreshold = Double.parseDouble(args[i+1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else if (arg.equals("--lcs")) {
                LCS = true;
            }
            else if (arg.equals("--match-type") && args.length>(i+1)) {
                MATCHTYPE = args[i+1];
            }
            else if (arg.equals("--roles") && args.length>(i+1)) {
                String [] fields = args[i+1].split(",");
                for (int j = 0; j < fields.length; j++) {
                    String field = fields[j].trim();
                    roleArrayList.add(field);
                }
            }
            else if (arg.equals("--ili-uri")) {
                JenaSerialization.USEILIURI = true;
            }
            else if (arg.equals("--event-type") && args.length>(i+1)) {
                eventType = args[i+1];
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
        processEventFoldersSingleOutputFile(new File(pathToEventFolder), conceptMatchThreshold, phraseMatchThreshold, sourceMetaHashMap, wordnetData, eventType, roleArrayList);

    }

    static void chaining (HashMap<String, ArrayList<CompositeEvent>> finalLemmaEventMap,
                          String eventType, ArrayList<String> roleArrayList) {
        Set keySet = finalLemmaEventMap.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String lemma = (String) keys.next();
            ArrayList<CompositeEvent> myCompositeEvents = finalLemmaEventMap.get(lemma);
            if (DEBUG) System.out.println("BEFORE CHAINING myCompositeEvents.size() = " + myCompositeEvents.size());
            myCompositeEvents = chaining(myCompositeEvents, eventType, roleArrayList);
            if (DEBUG) System.out.println("AFTER CHAINING myCompositeEvents.size() = " + myCompositeEvents.size());
            finalLemmaEventMap.put(lemma, myCompositeEvents);
        }
    }

    static ArrayList<CompositeEvent> chaining (ArrayList<CompositeEvent> myCompositeEvents,
                                               String eventType, ArrayList<String> roleArrayList) {
        ArrayList<CompositeEvent> finalCompositeEvents = new ArrayList<CompositeEvent>();
        for (int j = 0; j < myCompositeEvents.size(); j++) {
                boolean match = false;
                boolean same = false;
                CompositeEvent myCompositeEvent = myCompositeEvents.get(j);
                if (DEBUG) System.out.println("myCompositeEvent.getEvent().getURI() = " + myCompositeEvent.getEvent().getURI());
                for (int k = 0; k < finalCompositeEvents.size(); k++) {
                    CompositeEvent finalCompositeEvent = finalCompositeEvents.get(k);
                    // checkCompositeEvents(myCompositeEvent, finalCompositeEvent);
                    if (DEBUG) {
                        System.out.println("finalCompositeEvent.getEvent().getURI() = " + finalCompositeEvent.getEvent().getURI());
                        for (int i = 0; i < myCompositeEvent.getMySemActors().size(); i++) {
                            SemActor semActor = myCompositeEvent.getMySemActors().get(i);
                            System.out.println("my semActor.getURI() = " + semActor.getURI());
                        }
                        for (int i = 0; i < finalCompositeEvent.getMySemActors().size(); i++) {
                            SemActor semActor = finalCompositeEvent.getMySemActors().get(i);
                            System.out.println("final semActor.getURI() = " + semActor.getURI());
                        }
                    }
                    if (myCompositeEvent.getEvent().getId().equals(finalCompositeEvent.getEvent().getId())) {
                        same = true;
                        //// to be sure we do not duplicate events already stored
                        //// the ids are unique across documents and within documents
                    }
                    else if (ComponentMatch.compareCompositeEvent(  myCompositeEvent, finalCompositeEvent, eventType, roleArrayList)) {
                        match = true;

                        if (DEBUG) {
                            if (!myCompositeEvent.getEvent().getId().substring(0, myCompositeEvent.getEvent().getId().lastIndexOf("#")).equals(finalCompositeEvent.getEvent().getId().substring(0, finalCompositeEvent.getEvent().getId().lastIndexOf("#")))) {
                                System.out.println("myCompositeEvent = " + myCompositeEvent.toString());
                                System.out.println("finalCompositeEvent.toString() = " + finalCompositeEvent.toString());
                            }
                        }
                        finalCompositeEvent.getEvent().mergeSemObject(myCompositeEvent.getEvent());
                        finalCompositeEvent.mergeObjects(myCompositeEvent);
                        finalCompositeEvent.mergeRelations(myCompositeEvent);
                        break;
                    }
                }
                if (!match && !same) {
                    if (DEBUG) System.out.println("NO MATCH");
                    finalCompositeEvents.add(myCompositeEvent);
                }
                else {
                    if (DEBUG) System.out.println("MATCH");
                }
        }
        if (finalCompositeEvents.size()==myCompositeEvents.size()) {
            /// no changes
            return myCompositeEvents;
        }
        else {
            ///iterate
            finalCompositeEvents = chaining(finalCompositeEvents, eventType, roleArrayList);
        }
        return finalCompositeEvents;
    }

    static void compareObjectFileWithFinalEvents (File file, HashMap<String, ArrayList<CompositeEvent>> finalLemmaEventMap, String eventType) {
        HashMap<String, ArrayList<CompositeEvent>> localEventMap = new HashMap<String, ArrayList<CompositeEvent>>();
        if (MATCHTYPE.equalsIgnoreCase("LEMMA")) {
            localEventMap = readLemmaEventHashMapFromObjectFile(file);
        }
        else if (MATCHTYPE.equalsIgnoreCase("ILILEMMA")) {
            localEventMap = readLemmaIliEventHashMapFromObjectFile(file);
        }
        else if (MATCHTYPE.equalsIgnoreCase("ILI")) {
            localEventMap = readIliEventHashMapFromObjectFile(file);
        }
        else {
            System.out.println("UNKNOWN MATCH TYPE:"+ MATCHTYPE);
        }
        if (DEBUG) System.out.println("localEventMap.size() = " + localEventMap.size());
        Set keySet = localEventMap.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String lemma = (String) keys.next();
            ArrayList<CompositeEvent> finalCompositeEvents = new ArrayList<CompositeEvent>();

            /////// WE ARE ONLY COMPARING EVENTS WITH THE SAME PHRASE!!!!!!!
            /////// IF THERE ARE ILI CONCEPTS AND WE READ THEM (MATCHTYPE), THE lemma CAN BE A ILI CONCEPT
            /////// EVENTS ARE SPLIT OVER MULTIPLE CONCEPTS IF THERE IS MORE THAN ONE
            if (finalLemmaEventMap.containsKey(lemma)) {
                finalCompositeEvents = finalLemmaEventMap.get(lemma);
            }

            if (DEBUG) {
                System.out.println("lemma = " + lemma);
                System.out.println("finalCompositeEvents.size() = " + finalCompositeEvents.size());
                for (int i = 0; i < finalCompositeEvents.size(); i++) {
                    CompositeEvent compositeEvent = finalCompositeEvents.get(i);
                    System.out.println("compositeEvent.getEvent().getURI() = " + compositeEvent.getEvent().getURI());
                }
            }

            ArrayList<CompositeEvent> myCompositeEvents = localEventMap.get(lemma);

            /// WE NOW JUST ADD EVENTS ACROSS DOCUMENTS AND DO THE MATCHING LATER WHEN CHAINING IN A RECURSIVE FUNCTION
            for (int i = 0; i < myCompositeEvents.size(); i++) {
                CompositeEvent compositeEvent = myCompositeEvents.get(i);
                finalCompositeEvents.add(compositeEvent);
            }

            /// we update the finalLemmaEventMap. finalCompositeEvents have either been extended or merged with existing
            finalLemmaEventMap.put(lemma, finalCompositeEvents);
        }
        if (DEBUG) System.out.println();
    }


    public static ArrayList<KafSense> getILIreferences(SemObject semEvent) {
        ArrayList<KafSense> iliReferences = new ArrayList<KafSense>();
        for (int i = 0; i < semEvent.getConcepts().size(); i++) {
            KafSense kafSense = semEvent.getConcepts().get(i);
            if (kafSense.getSensecode().toLowerCase().startsWith("ili")) {
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

    public static HashMap<String, ArrayList<CompositeEvent>> readLemmaIliEventHashMapFromObjectFile (File file) {
        HashMap<String, ArrayList<CompositeEvent>> eventMap = new HashMap<String, ArrayList<CompositeEvent>>();
        if (file.exists() ) {
            int cnt = 0;
            if (DEBUG) System.out.println("file = " + file.getName());
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois =  new ObjectInputStream(fis);
                Object obj = null;
                if (DEBUG) {
                    if (fis.available() <= 0) {
                        System.out.println("fis.available() = " + fis.available());
                    }
                }
                while (fis.available()>0) {
                    while ((obj = ois.readObject()) != null) {
                        cnt++;
                        if (DEBUG) System.out.println("cnt = " + cnt);
                        if (obj instanceof CompositeEvent) {
                            CompositeEvent compositeEvent = (CompositeEvent) obj;
                            if (DEBUG) {
                                System.out.println("compositeEvent.getEvent().getPhrase() = " + compositeEvent.getEvent().getPhrase());
                                System.out.println("compositeEvent.getEvent().getPhraseCounts().size() = " + compositeEvent.getEvent().getPhraseCounts().size());
                            }
                            ArrayList<KafSense> iliReferences = compositeEvent.getEvent().getConcepts();
                            if (iliReferences.size()>0) {
                                for (int i = 0; i < iliReferences.size(); i++) {
                                    KafSense kafSense = iliReferences.get(i);
                                    if (eventMap.containsKey(kafSense.getSensecode())) {
                                        ArrayList<CompositeEvent> events = eventMap.get(kafSense.getSensecode());
                                        events.add(compositeEvent);
                                        eventMap.put(kafSense.getSensecode(), events);
                                    } else {
                                        ArrayList<CompositeEvent> events = new ArrayList<CompositeEvent>();
                                        events.add(compositeEvent);
                                        eventMap.put(kafSense.getSensecode(), events);
                                    }
                                }
                            }
                            if (LCS) {
                                ArrayList<KafSense> lcsReferences = compositeEvent.getEvent().getLcs();
                                if (lcsReferences.size() > 0) {
                                    for (int i = 0; i < lcsReferences.size(); i++) {
                                        KafSense kafSense = lcsReferences.get(i);
                                        if (eventMap.containsKey(kafSense.getSensecode())) {
                                            ArrayList<CompositeEvent> events = eventMap.get(kafSense.getSensecode());
                                            events.add(compositeEvent);
                                            eventMap.put(kafSense.getSensecode(), events);
                                        } else {
                                            ArrayList<CompositeEvent> events = new ArrayList<CompositeEvent>();
                                            events.add(compositeEvent);
                                            eventMap.put(kafSense.getSensecode(), events);
                                        }
                                    }
                                }
                            }
                            /// IF THERE ARE NO ILI-REFERENCES THEN LEMMAS ARE USED TO COMPARE EVENTS
                            /// ILI REFERENCES CAN KEEP LEMMAS SEPARATE IF THEY POINT TO DIFFERENT SENSE
                            if (iliReferences.size()==0) {
                                ArrayList<PhraseCount> phrases = compositeEvent.getEvent().getPhraseCounts();
                                for (int i = 0; i < phrases.size(); i++) {
                                    PhraseCount phraseCount = phrases.get(i);
                                    if (eventMap.containsKey(phraseCount.getPhrase())) {
                                        ArrayList<CompositeEvent> events = eventMap.get(phraseCount.getPhrase());
                                        events.add(compositeEvent);
                                        eventMap.put(phraseCount.getPhrase(), events);
                                    } else {
                                        ArrayList<CompositeEvent> events = new ArrayList<CompositeEvent>();
                                        events.add(compositeEvent);
                                        eventMap.put(phraseCount.getPhrase(), events);
                                    }
                                }

                            }
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
                    System.out.println("Error reading object file = " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }
            if (DEBUG) System.out.println(file.getName()+" nr objects read = " + cnt);
        }
        else {
            if (DEBUG) System.out.println("Does not exist file.getAbsolutePath() = " + file.getAbsolutePath());
        }
        return eventMap;
    }

    public static HashMap<String, ArrayList<CompositeEvent>> readIliEventHashMapFromObjectFile (File file) {
        HashMap<String, ArrayList<CompositeEvent>> eventMap = new HashMap<String, ArrayList<CompositeEvent>>();
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
/*                            if (!compositeEvent.getEvent().getPhrase().equals("production")) {
                                continue;
                            }*/
                            if (DEBUG)
                                System.out.println("compositeEvent.getEvent().getPhrase() = " + compositeEvent.getEvent().getPhrase());

                            ArrayList<KafSense> iliReferences = compositeEvent.getEvent().getConcepts();
                            if (iliReferences.size()>0) {
                                for (int i = 0; i < iliReferences.size(); i++) {
                                    KafSense kafSense = iliReferences.get(i);
                                    if (eventMap.containsKey(kafSense.getSensecode())) {
                                        ArrayList<CompositeEvent> events = eventMap.get(kafSense.getSensecode());
                                        events.add(compositeEvent);
                                        eventMap.put(kafSense.getSensecode(), events);
                                    } else {
                                        ArrayList<CompositeEvent> events = new ArrayList<CompositeEvent>();
                                        events.add(compositeEvent);
                                        eventMap.put(kafSense.getSensecode(), events);
                                    }
                                }
                            }
                            if (LCS) {
                                ArrayList<KafSense> lcsReferences = compositeEvent.getEvent().getLcs();
                                if (lcsReferences.size() > 0) {
                                    for (int i = 0; i < lcsReferences.size(); i++) {
                                        KafSense kafSense = lcsReferences.get(i);
                                        if (eventMap.containsKey(kafSense.getSensecode())) {
                                            ArrayList<CompositeEvent> events = eventMap.get(kafSense.getSensecode());
                                            events.add(compositeEvent);
                                            eventMap.put(kafSense.getSensecode(), events);
                                        } else {
                                            ArrayList<CompositeEvent> events = new ArrayList<CompositeEvent>();
                                            events.add(compositeEvent);
                                            eventMap.put(kafSense.getSensecode(), events);
                                        }
                                    }
                                }
                            }
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

        return eventMap;
    }

    public static HashMap<String, ArrayList<CompositeEvent>> readLemmaEventHashMapFromObjectFile (File file) {
       // DEBUG = true;
        HashMap<String, ArrayList<CompositeEvent>> eventMap = new HashMap<String, ArrayList<CompositeEvent>>();
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
                            if (eventMap.containsKey(compositeEvent.getEvent().getPhrase())) {
                                ArrayList<CompositeEvent> events = eventMap.get(compositeEvent.getEvent().getPhrase());
                                events.add(compositeEvent);
                                eventMap.put(compositeEvent.getEvent().getPhrase(), events);
                            } else {
                                ArrayList<CompositeEvent> events = new ArrayList<CompositeEvent>();
                                events.add(compositeEvent);
                                eventMap.put(compositeEvent.getEvent().getPhrase(), events);
                            }
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
      //  DEBUG = false;
        return eventMap;
    }

    public static void addLemmaEventHashMapFromObjectFile (File file,
                                                           HashMap<String, ArrayList<CompositeEvent>> eventMap) {
        if (file.exists() ) {
            int cnt = 0;
            if (DEBUG) System.out.println("file = " + file.getName());
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois =  new ObjectInputStream(fis);
                Object obj = null;
                while ((obj = ois.readObject()) != null) {
                        cnt++;
                        if (obj instanceof  CompositeEvent) {
                            CompositeEvent compositeEvent = (CompositeEvent) obj;
/*                            if (!compositeEvent.getEvent().getPhrase().equals("production")) {
                                continue;
                            }*/
                            if (DEBUG) System.out.println("compositeEvent.getEvent().getPhrase() = " + compositeEvent.getEvent().getPhrase());
                            if (eventMap.containsKey(compositeEvent.getEvent().getPhrase())) {
                               ArrayList<CompositeEvent> events = eventMap.get(compositeEvent.getEvent().getPhrase());
                               events.add(compositeEvent);
                               eventMap.put(compositeEvent.getEvent().getPhrase(), events);
                            }
                            else {
                                ArrayList<CompositeEvent> events = new ArrayList<CompositeEvent>();
                                events.add(compositeEvent);
                                eventMap.put(compositeEvent.getEvent().getPhrase(), events);
                            }
                        }
                        else {
                            if (DEBUG) System.out.println("Unknown object obj.getClass() = " + obj.getClass());
                        }
                }
                ois.reset();
                ois.close();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (DEBUG) System.out.println(file.getName()+" nr objects read = " + cnt);
        }
    }


    public static void processEventFoldersSingleOutputFile (File pathToEventFolder, double conceptMatchThreshold,
                                      double phraseMatchThreshold,
                                      HashMap<String, SourceMeta> sourceMetaHashMap,
                                      WordnetData wordnetData,
                                      String eventType,
                                      ArrayList<String> roleArrayList

    ) {
        ArrayList<File> eventFolders = Util.makeFolderList(pathToEventFolder);
        for (int f = 0; f < eventFolders.size(); f++) {
            File nextEventFolder =  eventFolders.get(f);
            if (DEBUG) System.out.println("nextEventFolder.getName() = " + nextEventFolder.getName());
            try {
                OutputStream fos = new FileOutputStream(nextEventFolder.getAbsolutePath()+"/sem.trig");

                Dataset ds = TDBFactory.createDataset();
                Model defaultModel = ds.getDefaultModel();
                ResourcesUri.prefixModel(defaultModel);

                Model provenanceModel = ds.getNamedModel("http://www.newsreader-project.eu/provenance");
                ResourcesUri.prefixModelGaf(provenanceModel);

                Model instanceModel = ds.getNamedModel("http://www.newsreader-project.eu/instances");
                ResourcesUri.prefixModel(instanceModel);

                /// finalLemmeEventMap is defined outside the loop so that events are compared against the total list
                HashMap<String, ArrayList<CompositeEvent>> finalLemmaEventMap = new HashMap<String, ArrayList<CompositeEvent>>();

                ArrayList<File> files = Util.makeRecursiveFileList(nextEventFolder, ".obj");
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    compareObjectFileWithFinalEvents(file, finalLemmaEventMap, eventType);
                    if (DEBUG) System.out.println("finalLemmaEventMap = " + finalLemmaEventMap.size());
                }
                if (DEBUG) System.out.println("finalLemmaEventMap = " + finalLemmaEventMap.size());
                chaining(finalLemmaEventMap, eventType, roleArrayList);
                JenaSerialization.addJenaCompositeEvents(ds,
                        instanceModel,
                        provenanceModel,
                        finalLemmaEventMap,
                        sourceMetaHashMap,
                        VERBOSEMENTIONS);
                RDFDataMgr.write(fos, ds, RDFFormat.TRIG_PRETTY);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }


    static void checkCompositeEvents (CompositeEvent compositeEvent1, CompositeEvent compositeEvent2) {
        for (int m = 0; m < compositeEvent1.getEvent().getNafMentions().size(); m++) {
            NafMention nafMention = compositeEvent1.getEvent().getNafMentions().get(m);
            for (int l = 0; l < compositeEvent2.getEvent().getNafMentions().size(); l++) {
                NafMention mention = compositeEvent2.getEvent().getNafMentions().get(l);
                if (mention.sameMentionForDifferentSource(nafMention)) {
                    String fileName1 =  nafMention.getBaseUriWithoutId().substring(nafMention.getBaseUriWithoutId().lastIndexOf("/"));
                    String fileName2 =  mention.getBaseUriWithoutId().substring(mention.getBaseUriWithoutId().lastIndexOf("/"));
                    System.out.println("diff "+fileName1+" "+fileName2);
                }
            }
        }

    }

}
