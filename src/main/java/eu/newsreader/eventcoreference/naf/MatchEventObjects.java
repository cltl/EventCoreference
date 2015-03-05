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
    static String MATCHTYPE= "LEMMA";  // ILI OR ILILEMMA

    static public void main (String [] args) {

        String eventType = "";
        HashMap<String, SourceMeta> sourceMetaHashMap = null;
        WordnetData wordnetData = null;
        double conceptMatchThreshold = 0;
        double phraseMatchThreshold = 1;
        boolean singleOutput = false;
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
            else if (arg.equals("--match-type") && args.length>(i+1)) {
                MATCHTYPE = args[i+1];
            }
            else if (arg.equals("--ili-uri")) {
                JenaSerialization.USEILIURI = true;
            }
            else if (arg.equals("--event-type") && args.length>(i+1)) {
                eventType = args[i+1];
            }
            else if (arg.equals("--single-output")) {
                singleOutput = true;
            }
            else if (arg.equals("--source-data") && args.length>(i+1)) {
                pathToSourceDataFile = args[i+1];
            }
            else if (arg.equals("--debug")) {
                DEBUG = true;
            }
        }
        if (!pathToSourceDataFile.isEmpty()) {
            sourceMetaHashMap = ReadSourceMetaFile.readSourceFile(pathToSourceDataFile);
            System.out.println("sourceMetaHashMap = " + sourceMetaHashMap.size());
        }
        if (singleOutput) {
            //processEventFilesSingleOutputFile(new File(pathToEventFolder), conceptMatchThreshold, phraseMatchThreshold, sourceMetaHashMap, wordnetData, eventType);
            processEventFoldersSingleOutputFile(new File(pathToEventFolder), conceptMatchThreshold, phraseMatchThreshold, sourceMetaHashMap, wordnetData, eventType);
        }
        else {
            processEventFilesSeparateOutputFiles(new File(pathToEventFolder), conceptMatchThreshold, phraseMatchThreshold, sourceMetaHashMap, wordnetData, eventType);
        }
    }

    static void chaining (HashMap<String, ArrayList<CompositeEvent>> finalLemmaEventMap, String eventType) {
        Set keySet = finalLemmaEventMap.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String lemma = (String) keys.next();
            ArrayList<CompositeEvent> myCompositeEvents = finalLemmaEventMap.get(lemma);
            if (DEBUG) System.out.println("BEFORE CHAINING myCompositeEvents.size() = " + myCompositeEvents.size());
            myCompositeEvents = chaining(myCompositeEvents, eventType);
            if (DEBUG) System.out.println("AFTER CHAINING myCompositeEvents.size() = " + myCompositeEvents.size());
            finalLemmaEventMap.put(lemma, myCompositeEvents);
        }
    }

    static ArrayList<CompositeEvent> chaining (ArrayList<CompositeEvent> myCompositeEvents, String eventType) {
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
                    else if (ComponentMatch.compareCompositeEvent(myCompositeEvent, finalCompositeEvent, eventType)) {
                        match = true;
                        finalCompositeEvent.getEvent().mergeSemObject(myCompositeEvent.getEvent());
                        finalCompositeEvent.mergeObjects(myCompositeEvent);
                        finalCompositeEvent.mergeRelations(myCompositeEvent);
                        finalCompositeEvent.mergeFactRelations(myCompositeEvent);
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
            finalCompositeEvents = chaining(finalCompositeEvents, eventType);
        }
        return finalCompositeEvents;
    }

    static void compareObjectFileWithFinalEvents (File file, HashMap<String, ArrayList<CompositeEvent>> finalLemmaEventMap, String eventType) {
       // System.out.println("file.getName() = " + file.getName());
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

            if (lemma.equals("ili-30-01451842-v")) {
                DEBUG = false;
            }
            else {
                DEBUG = false;
                continue;
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
            //// NEXT IS REPLACED BY CHAINING
            /*for (int j = 0; j < myCompositeEvents.size(); j++) {
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
                    }
                    else if (ComponentMatch.compareCompositeEvent(myCompositeEvent, finalCompositeEvent, eventType)) {
                        match = true;
                        finalCompositeEvent.getEvent().mergeSemObject(myCompositeEvent.getEvent());
                        finalCompositeEvent.mergeObjects(myCompositeEvent);
                        finalCompositeEvent.mergeRelations(myCompositeEvent);
                        finalCompositeEvent.mergeFactRelations(myCompositeEvent);
                        /// we thus merge with the first matching event and do not consider others that may be better!!!!!
                        /// In theory: there cannot be another candidate because we do a logical match: if match always merge and all are merged
                        //// If we do a scoring match we need to adjust this:
                        /// the comparison and merge loop needs to be adjusted to get the best candidate for merge
                        /// we could score each candidate and maintain the top scoring one
                        //  System.out.println("finalCompositeEvent.toString() = " + finalCompositeEvent.toString());
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
            }*/
            /// we update the finalLemmaEventMap. finalCompositeEvents have either been extended or merged with existing
            finalLemmaEventMap.put(lemma, finalCompositeEvents);
        }
        if (DEBUG) System.out.println();
    }

    public static ArrayList<KafSense> getILIreferences (SemObject semEvent) {
        ArrayList<KafSense> iliReferences = new ArrayList<KafSense>();
        for (int i = 0; i < semEvent.getConcepts().size(); i++) {
            KafSense kafSense = semEvent.getConcepts().get(i);
            if (kafSense.getSensecode().toLowerCase().startsWith("ili")) {
                iliReferences.add(kafSense);
            }
        }
        return iliReferences;
    }

    public static HashMap<String, ArrayList<CompositeEvent>> readLemmaIliEventHashMapFromObjectFile (File file) {
        HashMap<String, ArrayList<CompositeEvent>> eventMap = new HashMap<String, ArrayList<CompositeEvent>>();
        if (file.exists() ) {
            int cnt = 0;
            DEBUG = false;
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
                            if (DEBUG) {
                                System.out.println("compositeEvent.getEvent().getPhrase() = " + compositeEvent.getEvent().getPhrase());
                                System.out.println("compositeEvent.getEvent().getPhraseCounts().size() = " + compositeEvent.getEvent().getPhraseCounts().size());
                            }
                            ArrayList<KafSense> iliReferences = getILIreferences(compositeEvent.getEvent());
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
                            else {
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
               //  System.out.println("file = " + file.getAbsolutePath());
              //   e.printStackTrace();
            }
            if (DEBUG) System.out.println(file.getName()+" nr objects read = " + cnt);
        }
        DEBUG = false;
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

                            ArrayList<KafSense> iliReferences = getILIreferences(compositeEvent.getEvent());
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

        return eventMap;
    }

    public static void addLemmaEventHashMapFromObjectFile (File file,  HashMap<String, ArrayList<CompositeEvent>> eventMap) {
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

    public static void processEventFilesSingleOutputFile (File pathToEventFolder, double conceptMatchThreshold,
                                      double phraseMatchThreshold,
                                      HashMap<String, SourceMeta> sourceMetaHashMap,
                                      WordnetData wordnetData,
                                      String eventType

    ) {
        try {
            OutputStream fos = new FileOutputStream(pathToEventFolder.getAbsolutePath()+"/sem.trig");

            Dataset ds = TDBFactory.createDataset();
            Model defaultModel = ds.getDefaultModel();
            ResourcesUri.prefixModel(defaultModel);

            Model provenanceModel = ds.getNamedModel("http://www.newsreader-project.eu/provenance");
            ResourcesUri.prefixModelGaf(provenanceModel);

            Model instanceModel = ds.getNamedModel("http://www.newsreader-project.eu/instances");
            ResourcesUri.prefixModel(instanceModel);


            ArrayList<File> files = Util.makeRecursiveFileList(pathToEventFolder, ".obj");
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                // System.out.println("file.getName() = " + file.getName());
                HashMap<String, ArrayList<CompositeEvent>> finalLemmaEventMap = new HashMap<String, ArrayList<CompositeEvent>>();
                compareObjectFileWithFinalEvents(file, finalLemmaEventMap, eventType);
                JenaSerialization.addJenaCompositeEvents(ds, instanceModel, provenanceModel, finalLemmaEventMap, sourceMetaHashMap);
               // System.out.println("finalLemmaEventMap = " + finalLemmaEventMap.size());
                //  GetSemFromNafFile.serializeJenaCompositeEvents(System.out,  finalEventMap, sourceMetaHashMap);

            }
            RDFDataMgr.write(fos, ds, RDFFormat.TRIG_PRETTY);
            fos.close();
        } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


       // System.out.println("nMatches = " + nMatches);
    }


    public static void processEventFoldersSingleOutputFile (File pathToEventFolder, double conceptMatchThreshold,
                                      double phraseMatchThreshold,
                                      HashMap<String, SourceMeta> sourceMetaHashMap,
                                      WordnetData wordnetData,
                                      String eventType

    ) {
        ArrayList<File> eventFolders = Util.makeFolderList(pathToEventFolder);
        for (int f = 0; f < eventFolders.size(); f++) {
            File nextEventFolder =  eventFolders.get(f);
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
                    //System.out.println("finalLemmaEventMap = " + finalLemmaEventMap.size());
                }
                chaining(finalLemmaEventMap, eventType);
                JenaSerialization.addJenaCompositeEvents(ds, instanceModel, provenanceModel, finalLemmaEventMap, sourceMetaHashMap);
                RDFDataMgr.write(fos, ds, RDFFormat.TRIG_PRETTY);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }



       // System.out.println("nMatches = " + nMatches);
    }

    public static void processEventFilesSeparateOutputFiles (File pathToEventFolder, double conceptMatchThreshold,
                                          double phraseMatchThreshold,
                                          HashMap<String, SourceMeta> sourceMetaHashMap,
                                          WordnetData wordnetData,
                                          String eventType

        ) {
        try {


            ArrayList<File> files = Util.makeRecursiveFileList(pathToEventFolder, ".obj");
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                // System.out.println("file.getName() = " + file.getName());
                HashMap<String, ArrayList<CompositeEvent>> finalLemmaEventMap = new HashMap<String, ArrayList<CompositeEvent>>();
                compareObjectFileWithFinalEvents(file, finalLemmaEventMap, eventType);
                OutputStream fos = new FileOutputStream(file.getAbsolutePath()+".trig");
                JenaSerialization.serializeJenaCompositeEvents(fos, finalLemmaEventMap, sourceMetaHashMap);
                fos.close();
            }
        } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


       // System.out.println("nMatches = " + nMatches);
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
