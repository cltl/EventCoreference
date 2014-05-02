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
        String pathToEventFolder = "/Code/vu/newsreader/EventCoreference/LN_football_test_out-tiny/events/other";
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
        processEventFolder( new File(pathToEventFolder),conceptMatchThreshold, phraseMatchThreshold, sourceMetaHashMap);

    }


    public static HashMap<String, ArrayList<CompositeEvent>> readLemmaEventHashMapFromObjectFile (File file) {
        HashMap<String, ArrayList<CompositeEvent>> eventMap = new HashMap<String, ArrayList<CompositeEvent>>();
        if (file.exists() ) {
            try {
             //   System.out.println("file.getName() = " + file.getName());
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois =  new ObjectInputStream(fis);
                Object obj = null;
                while ((obj = ois.readObject()) != null) {
                        if (obj instanceof  CompositeEvent) {
                            CompositeEvent compositeEvent = (CompositeEvent) obj;
                          //  System.out.println("compositeEvent.getEvent().getPhrase() = " + compositeEvent.getEvent().getPhrase());

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
                }
                ois.close();
            } catch (Exception e) {
             //   e.printStackTrace();
            }
        }

        return eventMap;
    }

    public static void processEventFolder (File pathToEventFolder, double conceptMatchThreshold,
                                      double phraseMatchThreshold,
                                      HashMap<String, SourceMeta> sourceMetaHashMap

    ) {
        HashMap<String, ArrayList<CompositeEvent>> semEvents = new HashMap<String, ArrayList<CompositeEvent>>();
        ArrayList<File> files = Util.makeRecursiveFileList(pathToEventFolder);

        //System.out.println("files.size() = " + files.size());
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            if (i % 5 == 0) {
                System.out.println("i = " + i);
                //  System.out.println("file.getName() = " + file.getAbsolutePath());
                System.out.println("semEvents = " + semEvents.size());
            }

            HashMap<String, ArrayList<CompositeEvent>> eventMap = readLemmaEventHashMapFromObjectFile(file);
            Set keySet = eventMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String lemma = (String) keys.next();
                if (semEvents.containsKey(lemma)) {
                    ArrayList<CompositeEvent> compositeEvents = semEvents.get(lemma);
                    ArrayList<CompositeEvent> myCompositeEvents = eventMap.get(lemma);
                    for (int j = 0; j < myCompositeEvents.size(); j++) {
                        boolean match = false;
                        CompositeEvent compositeEvent = myCompositeEvents.get(j);
                        for (int k = 0; k < compositeEvents.size(); k++) {
                            CompositeEvent event = compositeEvents.get(k);
                            if (ComponentMatch.compareCompositeEvent(compositeEvent, event)) {
                                match = true;
                                System.out.println("we have got a match event.getEvent().getPhrase() = " + event.getEvent().getPhrase());
                                event.getEvent().mergeSemObject(compositeEvent.getEvent());
                                event.mergeRelations(compositeEvent);
                                event.mergeFactRelations(compositeEvent);
                                break;
                            }
                        }
                        if (!match) {
                            semEvents.put(lemma, myCompositeEvents);
                        }
                    }
                }
                else {
                    ArrayList<CompositeEvent> myCompositeEvents = eventMap.get(lemma);
                    semEvents.put(lemma, myCompositeEvents);
                }
            }
        }
        try {
            //System.out.println("pathToNafFolder = " + pathToNafFolder);
            System.out.println("final semEvents = " + semEvents.size());
            FileOutputStream fos = new FileOutputStream(pathToEventFolder+"/sem.trig");
            GetSemFromNafFile.serializeJenaEvents(fos,  semEvents, sourceMetaHashMap);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
