package eu.newsreader.eventcoreference.util;

import eu.newsreader.eventcoreference.naf.InterDocumentEventCoref;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by kyoto on 12/13/13.
 */
public class ProcessNewsReaderCarBatch {
    static String corefExtension = ".coref";
    static double conceptMatchThreshold = 0.6;
    static double phraseMatchThreshold = 0.6;

    static public void main (String [] args) {
        //String pathToProcessFile = args[0];
        String pathToClusterFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-eventcoreference/publicationDatesFiles.txt";
        String pathToNafClusters = "";
        String projectName  = "";
        boolean DO_entitycoref = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--cluster-file") && args.length>(i+1)) {
                pathToClusterFile = args[i+1];
            }
            else if (arg.equals("--cluster-folders") && args.length>(i+1)) {
                pathToNafClusters = args[i+1];
            }
            else if (arg.equals("--project") && args.length>(i+1)) {
                projectName = args[i+1];
            }
            else if (arg.equals("--concept-match") && args.length>(i+1)) {
                conceptMatchThreshold = Double.parseDouble(args[i+1]);
            }
            else if (arg.equals("--phrase-match") && args.length>(i+1)) {
                phraseMatchThreshold = Double.parseDouble(args[i+1]);
            }
            else if (arg.equals("--entity-coref")) {
                DO_entitycoref = true;
            }
        }
        if (DO_entitycoref) {
            File entitycoref = new File("entitycoref");
            if (!entitycoref.exists()) {
                entitycoref.mkdir();
            }
            if (entitycoref.exists()) {
                HashMap<File, ArrayList<String>> dataMapOut = new HashMap<File, ArrayList<String>>();

                HashMap<String, ArrayList<String>> dataMapIn = Util.ReadFileToStringHashMap(pathToClusterFile);
                Set keySet = dataMapIn.keySet();
                Iterator keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                   // System.out.println("key = " + key);
                    File keyFolder = new File(entitycoref.getAbsolutePath()+"/"+key);
                    if (!keyFolder.exists()) {
                        keyFolder.mkdir();
                    }
                    ArrayList<String> resultFiles = new ArrayList<String>();
                    ArrayList<String> mappings = dataMapIn.get(key);
                   // System.out.println("mappings.size() = " + mappings.size());
                    for (int i = 0; i < mappings.size(); i++) {
                        String pathToFile = mappings.get(i);
                        File file = new File (pathToFile);

                        String outputFile = keyFolder.getAbsolutePath()+"/"+file.getName()+corefExtension;
                        resultFiles.add(outputFile);
                       // System.out.println("outputFile = " + outputFile);


                        /// REMOVE QUOTES TO GET THE COREFS

                      /* try {
                            FileOutputStream fos = new FileOutputStream(outputFile);
                            EntityCorefReferenceBaseline.processNafFile(fos, file.getAbsolutePath());
                            fos.close();

                        } catch (IOException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }*/
                    }
                    dataMapOut.put(keyFolder, resultFiles);
                }
                keySet = dataMapOut.keySet();
                keys = keySet.iterator();
                while (keys.hasNext()) {
                    File pathToNafFolder = (File) keys.next();
                   // System.out.println("pathToNafFolder = " + pathToNafFolder);
                    InterDocumentEventCoref.processFolder (projectName, pathToNafFolder, corefExtension, conceptMatchThreshold, phraseMatchThreshold);
                }
            }
        }
        else {
            File nafClusterFolder = new File (pathToNafClusters);
            if (nafClusterFolder.exists()) {
                ArrayList<File> folders = Util.makeFolderList(nafClusterFolder);
                for (int i = 0; i < folders.size(); i++) {
                    File pathToNafFolder =  folders.get(i);
                    InterDocumentEventCoref.processFolder (projectName, pathToNafFolder, corefExtension, conceptMatchThreshold, phraseMatchThreshold);
                }
            }
        }
    }


}
