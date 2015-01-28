package eu.newsreader.eventcoreference.util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 1/4/14.
 */
public class Statistics {

    static boolean DBPENTITY = false;
    static boolean NEWENTITY = false;
    static boolean ILIEVENT = false;
    static boolean LEMMAEVENT = false;
    static boolean PREDICATE = false;
    static boolean TRIPLE = false;

    static ArrayList<String> fileMap = new ArrayList<String>();
    static HashMap<String, ArrayList<Integer>> dbpEntityMap = new HashMap<String, ArrayList<Integer>>();
    static HashMap<String, ArrayList<Integer>> newEntityMap = new HashMap<String, ArrayList<Integer>>();
    static HashMap<String, ArrayList<Integer>> iliEventMap = new HashMap<String, ArrayList<Integer>>();
    static HashMap<String, ArrayList<Integer>> predicateMap = new HashMap<String, ArrayList<Integer>>();
    static HashMap<String, ArrayList<Integer>> tripleMap = new HashMap<String, ArrayList<Integer>>();


    static void setALLFALSE () {
        DBPENTITY = false;
        NEWENTITY = false;
        ILIEVENT = false;
        LEMMAEVENT = false;
        PREDICATE = false;
        TRIPLE = false;
    }

    static void updateMap (String key, Integer count, HashMap<String, ArrayList<Integer>> map, int fileNr, int nrFiles) {
           if (map.containsKey(key)) {
               ArrayList<Integer> counts = map.get(key);
               counts.set(fileNr, count);
               map.put(key, counts);
           }
           else {
               ArrayList<Integer> counts = new ArrayList<Integer>();
               for (int i = 0; i < nrFiles; i++) {
                   counts.add(0);
               }
               counts.set(fileNr, count);
               map.put(key, counts);
           }
    }

    static void getStats(File file, int fileNr, int nrFiles) {

        if (file.exists() ) {
            fileMap.add(fileNr, file.getName());
            try {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                   // System.out.println(inputLine);
                    inputLine = inputLine.trim();
                    if (inputLine.startsWith("CROSSLINGUAL TRIPLES")) {
                        setALLFALSE();
                        TRIPLE = true;
                    }
                    else if (inputLine.startsWith("MENTIONS OF DBP entities")) {
                        setALLFALSE();
                        DBPENTITY = true;
                    }
                    else if (inputLine.startsWith("MENTIONS OF NEW entities")) {
                        setALLFALSE();
                        NEWENTITY = true;
                    }
                    else if (inputLine.startsWith("MENTIONS OF ILI events")) {
                        setALLFALSE();
                        ILIEVENT = true;
                    }
                    else if (inputLine.startsWith("PREDICATES")) {
                        setALLFALSE();
                        PREDICATE = true;
                    }
                    else if (inputLine.startsWith("MENTIONS OF LEMMA Events")) {
                        setALLFALSE();
                        PREDICATE = true;
                    }
                    if (inputLine.trim().length()>0) {
                         String[] fields = inputLine.split("\t");
                        if (fields.length==2) {
                            String key = fields[0];
                            Integer count = Integer.parseInt(fields[1]);
                            if (DBPENTITY) {
                                updateMap(key, count, dbpEntityMap, fileNr, nrFiles);
                            }
                            else if (NEWENTITY) {
                                updateMap(key, count, newEntityMap, fileNr, nrFiles);
                            }
                            else if (ILIEVENT) {
                                updateMap(key, count, iliEventMap, fileNr, nrFiles);
                            }
                            else if (PREDICATE) {
                                updateMap(key, count, predicateMap, fileNr, nrFiles);
                            }
                        }
                        else if (fields.length==4) {
                            String subject = fields[0];
                            String predicate = fields[1];
                            String object = fields[2];
                            String triple = subject+":"+predicate+":"+object;
                            Integer count = Integer.parseInt(fields[3]);
                            if (TRIPLE) {
                                updateMap(triple, count, tripleMap, fileNr, nrFiles);
                            }
                        }
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    static public void main (String [] args) {
        String folder = "/Users/piek/Desktop/NWR/Cross-lingual/stats/instances/airbus";
        ArrayList<File> files = Util.makeFlatFileList(new File(folder), ".xls");
        fileMap = new ArrayList<String>(files.size());
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            getStats(file, i, files.size());
        }
        try {
            OutputStream fos = new FileOutputStream(folder+"/"+"crosslingual.stats.xls");
            String str = "";

            if (dbpEntityMap.size()>0) {
                str = "MENTIONS OF DBP entities\n";
                str += "\t";
                for (int i = 0; i < fileMap.size(); i++) {
                    String fileName = fileMap.get(i);
                    str += fileName + "\t";
                }
                str += "\n";
                Set keySet = dbpEntityMap.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    ArrayList<Integer> counts = dbpEntityMap.get(key);
                    str += key + "\t";
                    for (int i = 0; i < counts.size(); i++) {
                        Integer integer = counts.get(i);
                        str += integer.toString() + "\t";
                    }
                    str += "\n";
                }
                str += "\n";
                fos.write(str.getBytes());
            }

            if (newEntityMap.size()>0) {
                str = "MENTIONS OF NEW entities\n";
                str += "\t";
                for (int i = 0; i < fileMap.size(); i++) {
                    String fileName = fileMap.get(i);
                    str += fileName + "\t";
                }
                str += "\n";
                Set keySet = newEntityMap.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    ArrayList<Integer> counts = newEntityMap.get(key);
                    str += key + "\t";
                    for (int i = 0; i < counts.size(); i++) {
                        Integer integer = counts.get(i);
                        str += integer.toString() + "\t";
                    }
                    str += "\n";
                }
                str += "\n";
                fos.write(str.getBytes());
            }

            if (iliEventMap.size()>0) {
                str = "MENTIONS OF ILI events\n";
                str += "\t";
                for (int i = 0; i < fileMap.size(); i++) {
                    String fileName = fileMap.get(i);
                    str += fileName + "\t";
                }
                str += "\n";
                Set keySet = iliEventMap.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    ArrayList<Integer> counts = iliEventMap.get(key);
                    str += key + "\t";
                    for (int i = 0; i < counts.size(); i++) {
                        Integer integer = counts.get(i);
                        str += integer.toString() + "\t";
                    }
                    str += "\n";
                }
                str += "\n";
                fos.write(str.getBytes());
            }

            if (predicateMap.size()>0) {
                str = "PREDICATES\n";
                str += "\t";
                for (int i = 0; i < fileMap.size(); i++) {
                    String fileName = fileMap.get(i);
                    str += fileName + "\t";
                }
                str += "\n";
                Set keySet = predicateMap.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    ArrayList<Integer> counts = predicateMap.get(key);
                    str += key + "\t";
                    for (int i = 0; i < counts.size(); i++) {
                        Integer integer = counts.get(i);
                        str += integer.toString() + "\t";
                    }
                    str += "\n";
                }
                str += "\n";
                fos.write(str.getBytes());
            }

            if (tripleMap.size()>0) {
                str = "TRIPLES\n";
                str += "\t";
                for (int i = 0; i < fileMap.size(); i++) {
                    String fileName = fileMap.get(i);
                    str += fileName + "\t";
                }
                str += "\n";
                Set keySet = tripleMap.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    ArrayList<Integer> counts = tripleMap.get(key);
                    str += key + "\t";
                    for (int i = 0; i < counts.size(); i++) {
                        Integer integer = counts.get(i);
                        str += integer.toString() + "\t";
                    }
                    str += "\n";
                }
                str += "\n";
                fos.write(str.getBytes());
            }

            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
