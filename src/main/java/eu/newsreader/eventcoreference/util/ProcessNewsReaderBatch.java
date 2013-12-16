package eu.newsreader.eventcoreference.util;

import eu.newsreader.eventcoreference.naf.EntityCorefReferenceBaseline;
import eu.newsreader.eventcoreference.naf.InterDocumentEventCoref;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by kyoto on 12/13/13.
 */
public class ProcessNewsReaderBatch {
    static final String corefExtension = ".coref";
    static final double conceptMatchThreshold = 0.6;
    static final double phraseMatchThreshold = 0.6;

    static public void main (String [] args) {
        String pathToProcessFile = args[0];
        File entitycoref = new File("entitycoref");
        if (!entitycoref.exists()) {
            entitycoref.mkdir();
        }
        if (entitycoref.exists()) {
            HashMap<File, ArrayList<String>> dataMapOut = new HashMap<File, ArrayList<String>>();

            HashMap<String, ArrayList<String>> dataMapIn = ReadFileToStringHashMap(pathToProcessFile);
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
                    /// REMOVE QUOTES TO GET THE COREFS

                    try {
                        String outputFile = keyFolder.getAbsolutePath()+"/"+file.getName()+corefExtension;
                        resultFiles.add(outputFile);

                        FileOutputStream fos = new FileOutputStream(outputFile);
                        EntityCorefReferenceBaseline.processNafFile(fos, file.getAbsolutePath());
                        fos.close();

                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement ßuse File | Settings | File Templates.
                    }
                }
                dataMapOut.put(keyFolder, resultFiles);
            }
            keySet = dataMapOut.keySet();
            keys = keySet.iterator();
            while (keys.hasNext()) {
                File pathToNafFolder = (File) keys.next();
                InterDocumentEventCoref.processFolder (pathToNafFolder, corefExtension, conceptMatchThreshold, phraseMatchThreshold);
            }
        }
    }


    /**
     * 2003-01-01	/home/vanerp/NewsReaderProcessed/Batch1/47K9-W260-006F-01XY.xml_dc767673993538525fb781e3dff2b0aa.naf
     2003-01-01	/home/vanerp/NewsReaderProcessed/Batch1/47KF-GV00-00VR-R53W.xml_aa38e56214ddf44297b6afe6d0128dd2.naf
     */
    /**
     *
     * @param fileName
     * @return
     */
    static public HashMap ReadFileToStringHashMap(String fileName) {
        HashMap<String, ArrayList<String>> lineHashMap = new HashMap<String, ArrayList<String>>();
        if (new File(fileName).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        int idx_s = inputLine.indexOf("\t");
                        if (idx_s>-1) {
                            String key = inputLine.substring(0, idx_s).trim();

                            String value = inputLine.substring(idx_s+1).trim();
                            if (lineHashMap.containsKey(key)) {
                                ArrayList<String> files = lineHashMap.get(key);
                                files.add(value);
                                lineHashMap.put(key, files);
                            }
                            else {
                                ArrayList<String> files = new ArrayList<String>();
                                files.add(value);
                                lineHashMap.put(key, files);
                            }
                        }
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lineHashMap;
    }


}
