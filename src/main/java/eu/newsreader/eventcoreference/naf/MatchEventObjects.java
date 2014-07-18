package eu.newsreader.eventcoreference.naf;

import eu.newsreader.eventcoreference.coref.ComponentMatch;
import eu.newsreader.eventcoreference.objects.CompositeEvent;
import eu.newsreader.eventcoreference.objects.SourceMeta;
import eu.newsreader.eventcoreference.util.ReadSourceMetaFile;
import eu.newsreader.eventcoreference.util.Util;

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

    static public void main (String [] args) {

        HashMap<String, SourceMeta> sourceMetaHashMap = null;
        double conceptMatchThreshold = 0;
        double phraseMatchThreshold = 1;
        String pathToEventFolder = "/Users/piek/Desktop/NWR-DATA/worldcup/events/other";
        //String pathToEventFolder = "/Code/vu/newsreader/EventCoreference/LN_football_test_out-tiny/events/other";
       //String pathToEventFolder = "/Code/vu/newsreader/EventCoreference/LN_football_test_out/events/other";
        String pathToSourceDataFile = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--event-folder") && args.length>(i+1)) {
                pathToEventFolder = args[i+1];
            }
            else if (arg.equals("--concept-match") && args.length>(i+1)) {
                conceptMatchThreshold = Double.parseDouble(args[i+1]);
            }
            else if (arg.equals("--phrase-match") && args.length>(i+1)) {
                phraseMatchThreshold = Double.parseDouble(args[i+1]);
            }
            else if (arg.equals("--source-data") && args.length>(i+1)) {
                pathToSourceDataFile = args[i+1];
                sourceMetaHashMap = ReadSourceMetaFile.readSourceFile(pathToSourceDataFile);
               // System.out.println("sourceMetaHashMap = " + sourceMetaHashMap.size());
            }
        }
        processEventFiles(new File(pathToEventFolder), conceptMatchThreshold, phraseMatchThreshold, sourceMetaHashMap);
    }


    public static HashMap<String, ArrayList<CompositeEvent>> readLemmaEventHashMapFromObjectFile (File file) {
        HashMap<String, ArrayList<CompositeEvent>> eventMap = new HashMap<String, ArrayList<CompositeEvent>>();
        if (file.exists() ) {
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois =  new ObjectInputStream(fis);
                Object obj = null;
                while ((obj = ois.readObject()) != null) {
                        if (obj instanceof  CompositeEvent) {
                            CompositeEvent compositeEvent = (CompositeEvent) obj;
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
            } catch (Exception e) {
             //   e.printStackTrace();
            }
        }

        return eventMap;
    }

    public static void processEventFiles (File pathToEventFolder, double conceptMatchThreshold,
                                      double phraseMatchThreshold,
                                      HashMap<String, SourceMeta> sourceMetaHashMap

    ) {
        int nMatches = 0;
        ArrayList<File> files = Util.makeRecursiveFileList(pathToEventFolder, ".obj");
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
/*            if (!file.getName().equals("events-2014-01-01.obj")) {
            //if (!file.getName().equals("events-2022--.obj")) {
                continue;
            }*/
            HashMap<String, ArrayList<CompositeEvent>> finalEventMap = new HashMap<String, ArrayList<CompositeEvent>>();
            HashMap<String, ArrayList<CompositeEvent>> localEventMap = readLemmaEventHashMapFromObjectFile(file);
           // System.out.println("file.getName() = " + file.getName());
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
                       // if (true) {
                        if (ComponentMatch.compareCompositeEvent(myCompositeEvent, finalCompositeEvent)) {
                            nMatches++;
                            match = true;
                            finalCompositeEvent.getEvent().mergeSemObject(myCompositeEvent.getEvent());
                            finalCompositeEvent.mergeObjects(myCompositeEvent);
                            finalCompositeEvent.mergeRelations(myCompositeEvent);
                            finalCompositeEvent.mergeFactRelations(myCompositeEvent);
                            break;
                        }
                    }
                    if (!match) {
                        finalCompositeEvents.add(myCompositeEvent);
                    }
                }
                finalEventMap.put(lemma, finalCompositeEvents);
            }
            try {
                    OutputStream fos = new FileOutputStream(file+".sem.trig");
                    GetSemFromNafFile.serializeJenaCompositeEvents(fos,  finalEventMap, sourceMetaHashMap);
                    fos.close();

                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

        }
        System.out.println("nMatches = " + nMatches);
    }


    public static void processEventFilesOrg (File pathToEventFolder, double conceptMatchThreshold,
                                      double phraseMatchThreshold,
                                      HashMap<String, SourceMeta> sourceMetaHashMap

    ) {
        ArrayList<File> files = Util.makeRecursiveFileList(pathToEventFolder, ".obj");

        //System.out.println("files.size() = " + files.size());
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            if (!file.getName().equals("events-2014-01-01.obj")) {

                ///Code/vu/newsreader/EventCoreference/LN_football_test_out/events/other
                //events-2014-01-01.obj
                continue;
            }
            HashMap<String, ArrayList<CompositeEvent>> finalEventMap = new HashMap<String, ArrayList<CompositeEvent>>();
            HashMap<String, ArrayList<CompositeEvent>> localEventMap = readLemmaEventHashMapFromObjectFile(file);
            System.out.println("file.getName() = " + file.getName());
            System.out.println("localEventMap.size() = " + localEventMap.size());
            Set keySet = localEventMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String lemma = (String) keys.next();
                ArrayList<CompositeEvent> myCompositeFinalEvents = new ArrayList<CompositeEvent>();
                ArrayList<CompositeEvent> myCompositeEvents = localEventMap.get(lemma);
                for (int j = 0; j < myCompositeEvents.size(); j++) {
                    boolean match = false;
                    CompositeEvent compositeEvent = myCompositeEvents.get(j);
                    for (int k = 0; k < myCompositeFinalEvents.size(); k++) {
                        CompositeEvent finalCompositeEvent = myCompositeFinalEvents.get(k);
                        if (true) {
                       // if (ComponentMatch.compareCompositeEvent(compositeEvent, event)) {
                            match = true;
                            System.out.println("lemma = " + lemma);
                            System.out.println("myCompositeEvents = " + myCompositeEvents.size());
                            System.out.println("we have got a match event.getEvent().getPhrase() = " + finalCompositeEvent.getEvent().getPhrase());
                            finalCompositeEvent.getEvent().mergeSemObject(compositeEvent.getEvent());
                            System.out.println("finalCompositeEvent.getMySemRelations().size() = " + finalCompositeEvent.getMySemRelations().size());
                            finalCompositeEvent.mergeRelations(compositeEvent);
                            System.out.println("new finalCompositeEvent.getMySemRelations().size() = " + finalCompositeEvent.getMySemRelations().size());
                            finalCompositeEvent.mergeFactRelations(compositeEvent);
                            myCompositeFinalEvents.add(finalCompositeEvent);
                            break;
                        }
                    }
                    if (!match) {
                        myCompositeFinalEvents.add(compositeEvent);
                    }
                }
                finalEventMap.put(lemma, myCompositeFinalEvents);
            }
            try {
                    //System.out.println("pathToNafFolder = " + pathToNafFolder);
                   // System.out.println("final semEvents = " + finalSemEvents.size());
                    FileOutputStream fos = new FileOutputStream(file+".sem.trig");
                    GetSemFromNafFile.serializeJenaCompositeEvents(fos, finalEventMap, sourceMetaHashMap);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }
    }



}
