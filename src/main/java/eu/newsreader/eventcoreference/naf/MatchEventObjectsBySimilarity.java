package eu.newsreader.eventcoreference.naf;

import eu.newsreader.eventcoreference.coref.ComponentMatch;
import eu.newsreader.eventcoreference.objects.CompositeEvent;
import eu.newsreader.eventcoreference.objects.SourceMeta;
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
public class MatchEventObjectsBySimilarity {
    static public void main (String [] args) {
        String eventType = "speech";
        HashMap<String, SourceMeta> sourceMetaHashMap = null;
         WordnetData wordnetData = null;
        double conceptMatchThreshold = 0;
        double phraseMatchThreshold = 1;
        String pathToEventFolder = "/Users/piek/Desktop/NWR/NWR-DATA/cars/events/speech";
       // String pathToEventFolder = "/Users/piek/Desktop/NWR/NWR-DATA/cars/events/other";
       // String pathToEventFolder = "/Users/piek/Desktop/NWR/NWR-DATA/cars/events/other";
       // String pathToEventFolder = "/Users/piek/Desktop/NWR/NWR-DATA/cars/events/grammatical";
        //String pathToEventFolder = "/Code/vu/newsreader/EventCoreference/LN_football_test_out-tiny/events/other";
       //String pathToEventFolder = "/Code/vu/newsreader/EventCoreference/LN_football_test_out/events/other";
        String pathToSourceDataFile = "";
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
            else if (arg.equals("--event-type") && args.length>(i+1)) {
                eventType = args[i+1];
            }
            else if (arg.equals("--source-data") && args.length>(i+1)) {
                pathToSourceDataFile = args[i+1];
                sourceMetaHashMap = ReadSourceMetaFile.readSourceFile(pathToSourceDataFile);
               // System.out.println("sourceMetaHashMap = " + sourceMetaHashMap.size());
            }
        }
        processEventFiles(new File(pathToEventFolder), conceptMatchThreshold, phraseMatchThreshold, sourceMetaHashMap, wordnetData, eventType);
    }


    public static HashMap<String, ArrayList<CompositeEvent>> readLemmaEventHashMapFromObjectFile (File file) {
        HashMap<String, ArrayList<CompositeEvent>> eventMap = new HashMap<String, ArrayList<CompositeEvent>>();
        if (file.exists() ) {
            int cnt = 0;
            System.out.println("file = " + file.getName());
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois =  new ObjectInputStream(fis);
                Object obj = null;
                while ((obj = ois.readObject()) != null) {
                        cnt++;
                        if (obj instanceof  CompositeEvent) {
                            CompositeEvent compositeEvent = (CompositeEvent) obj;
                           // System.out.println("compositeEvent.getEvent().getPhrase() = " + compositeEvent.getEvent().getPhrase());
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
                            System.out.println("Unknown object obj.getClass() = " + obj.getClass());
                        }
                }
                ois.reset();
                ois.close();
                fis.close();
            } catch (Exception e) {
             //   e.printStackTrace();
            }
            System.out.println("nr objects read = " + cnt);
        }

        return eventMap;
    }

    public static void processEventFiles (File pathToEventFolder, double conceptMatchThreshold,
                                      double phraseMatchThreshold,
                                      HashMap<String, SourceMeta> sourceMetaHashMap,
                                      WordnetData wordnetData,
                                      String eventType

    ) {
        int nMatches = 0;
        ArrayList<File> files = Util.makeRecursiveFileList(pathToEventFolder, ".obj");
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
           // System.out.println("file.getName() = " + file.getName());
            HashMap<String, ArrayList<CompositeEvent>> finalLemmaEventMap = new HashMap<String, ArrayList<CompositeEvent>>();
            HashMap<String, ArrayList<CompositeEvent>> localEventMap = readLemmaEventHashMapFromObjectFile(file);
           // System.out.println("localEventMap.size() = " + localEventMap.size());
            Set keySet = localEventMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String lemma = (String) keys.next();
                ArrayList<CompositeEvent> finalCompositeEvents = new ArrayList<CompositeEvent>();
                ArrayList<CompositeEvent> myCompositeEvents = localEventMap.get(lemma);
                for (int j = 0; j < myCompositeEvents.size(); j++) {
                    boolean match = false;
                    CompositeEvent myCompositeEvent = myCompositeEvents.get(j);
                    for (int k = 0; k < finalCompositeEvents.size(); k++) {
                        CompositeEvent finalCompositeEvent = finalCompositeEvents.get(k);
                        //if (true) {
                        if (ComponentMatch.compareCompositeEvent(myCompositeEvent, finalCompositeEvent, eventType)) {
                            nMatches++;
                            match = true;
                            finalCompositeEvent.getEvent().mergeSemObject(myCompositeEvent.getEvent());
                            finalCompositeEvent.mergeObjects(myCompositeEvent);
                            finalCompositeEvent.mergeRelations(myCompositeEvent);
                            finalCompositeEvent.mergeFactRelations(myCompositeEvent);
                            /// we thus merge with the first matching event and do not consider others that may be bettter!!!!!
                            System.out.println("finalCompositeEvent.toString() = " + finalCompositeEvent.toString());
                            break;
                        }
                    }
                    if (!match) {
                        finalCompositeEvents.add(myCompositeEvent);
                    }
                }
                finalLemmaEventMap.put(lemma, finalCompositeEvents);
            }
/*            HashMap<String, ArrayList<CompositeEvent>> finalConceptEventMap = new HashMap<String, ArrayList<CompositeEvent>>();
            keySet = finalLemmaEventMap.keySet();
            keys = keySet.iterator();
            while (keys.hasNext()) {
                String lemma = (String) keys.next();
                ArrayList<CompositeEvent> finalCompositeEvents = new ArrayList<CompositeEvent>();
                ArrayList<CompositeEvent> myCompositeEvents = finalLemmaEventMap.get(lemma);
                for (int j = 0; j < myCompositeEvents.size(); j++) {
                    boolean match = false;
                    CompositeEvent myCompositeEvent = myCompositeEvents.get(j);
                    for (int k = 0; k < finalCompositeEvents.size(); k++) {
                        CompositeEvent finalCompositeEvent = finalCompositeEvents.get(k);
                        //if (true) {
                        if (ComponentMatch.compareCompositeEvent(myCompositeEvent, finalCompositeEvent, eventType)) {
                            nMatches++;
                            match = true;
                            finalCompositeEvent.getEvent().mergeSemObject(myCompositeEvent.getEvent());
                            finalCompositeEvent.mergeObjects(myCompositeEvent);
                            finalCompositeEvent.mergeRelations(myCompositeEvent);
                            finalCompositeEvent.mergeFactRelations(myCompositeEvent);
                            /// we thus merge with the first matching event and do not consider others that may be bettter!!!!!
                            System.out.println("finalCompositeEvent.toString() = " + finalCompositeEvent.toString());
                            break;
                        }
                    }
                    if (!match) {
                        finalCompositeEvents.add(myCompositeEvent);
                    }
                }
                finalLemmaEventMap.put(lemma, finalCompositeEvents);
            }*/
            try {
                    OutputStream fos = new FileOutputStream(file+".sem.trig");
                    GetSemFromNafFile.serializeJenaCompositeEvents(fos,  finalLemmaEventMap, sourceMetaHashMap);
                    fos.close();
                  //  GetSemFromNafFile.serializeJenaCompositeEvents(System.out,  finalEventMap, sourceMetaHashMap);


            } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

        }
       // System.out.println("nMatches = " + nMatches);
    }



}