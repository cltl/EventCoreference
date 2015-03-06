package eu.newsreader.eventcoreference.util;

import eu.newsreader.eventcoreference.input.TrigReader;

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
    static HashMap<String, ArrayList<Integer>> tripleSemMap = new HashMap<String, ArrayList<Integer>>();
    static HashMap<String, ArrayList<Integer>> subjectObjectMap = new HashMap<String, ArrayList<Integer>>();

    static public void main (String [] args) {
        String  folderPath = "";
//        folderPath = args[0];
        folderPath = "/Users/piek/Desktop/NWR/Cross-lingual/stats.4/relations/airbus";
        File folder = new File(folderPath);
        ArrayList<File> files = Util.makeFlatFileList(folder, ".xls");
        fileMap = new ArrayList<String>(files.size());
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            getStats(file, i, files.size());
        }
        try {
            OutputStream fos1 = new FileOutputStream(folder.getParent()+"/"+folder.getName()+"_crosslingual.table.stats.xls");
            OutputStream fos2 = new FileOutputStream(folder.getParent()+"/"+folder.getName()+"_crosslingual.overview.stats.xls");
            String str = "";
            if (dbpEntityMap.size()>0) {
                str = "MENTIONS OF DBP entities\n";
                fos1.write(str.getBytes());
                fos2.write(str.getBytes());
                writeMapToStream(fos1, fos2, dbpEntityMap);
            }

            if (newEntityMap.size()>0) {
                str = "MENTIONS OF NEW entities\n";
                fos1.write(str.getBytes());
                fos2.write(str.getBytes());
                writeMapToStream(fos1, fos2, newEntityMap);
            }

            if (iliEventMap.size()>0) {
                str = "MENTIONS OF ILI events\n";
                fos1.write(str.getBytes());
                fos2.write(str.getBytes());
                writeMapToStream(fos1, fos2, iliEventMap);
            }

            if (predicateMap.size()>0) {
                str = "PREDICATES\n";
                fos1.write(str.getBytes());
                fos2.write(str.getBytes());
                writeMapToStream(fos1,fos2, predicateMap);
            }

            if (tripleSemMap.size()>0) {
                str = "SEM TRIPLES\n";
                fos1.write(str.getBytes());
                fos2.write(str.getBytes());
                writeMapToStream(fos1, fos2, tripleSemMap);
            }

            if (tripleMap.size()>0) {
                str = "TRIPLES\n";
                fos1.write(str.getBytes());
                fos2.write(str.getBytes());
                writeMapToStream(fos1, fos2, tripleMap);
            }

            if (subjectObjectMap.size()>0) {
                str = "SUBJECT OBJECT PAIRS TRIPLES\n";
                fos1.write(str.getBytes());
                fos2.write(str.getBytes());
                writeMapToStream(fos1, fos2, subjectObjectMap);
            }

            fos1.close();
            fos2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static void setALLFALSE () {
        DBPENTITY = false;
        NEWENTITY = false;
        ILIEVENT = false;
        LEMMAEVENT = false;
        PREDICATE = false;
        TRIPLE = false;
    }

    static boolean intersectingILI (String ili_1, String ili_2) {
        String ili1 = ili_1;
        String ili2 = ili_2;

        int idx_1 = ili_1.indexOf("[");
        if (idx_1>-1) {
            ili1 = ili_1.substring(0, idx_1);
        }
        int idx_2 = ili_2.indexOf("[");
        if (idx_2>-1) {
            ili2 = ili_2.substring(0, idx_2);
        }
        String [] ili_1_fields = ili1.split("-and-");
        String [] ili_2_fields = ili2.split("-and-");
        for (int i = 0; i < ili_1_fields.length; i++) {
            String ili_1_field = ili_1_fields[i];
            for (int j = 0; j < ili_2_fields.length; j++) {
                String ili_2_field = ili_2_fields[j];
                if (ili_1_field.equals(ili_2_field)) {
                    return true;
                }
            }
        }
        return false;
    }

    static void updateMapUsingSubstringIntersection (String key, Integer count, HashMap<String, ArrayList<Integer>> map, int fileNr, int nrFiles) {
           if (map.containsKey(key)) {
               ArrayList<Integer> counts = map.get(key);
               counts.set(fileNr, count);
               map.put(key, counts);
           }
           else {
               boolean match = false;
               Set keySet = map.keySet();
               Iterator<String> keys = keySet.iterator();
               while (keys.hasNext()) {
                   String iliKey = keys.next();
                   if (intersectingILI(iliKey, key)) {
                       ArrayList<Integer> counts = map.get(iliKey);
                       counts.set(fileNr, count);
                       map.put(iliKey, counts);
                       match = true;
                       break;
                   }
               }
               if (!match) {
                   ArrayList<Integer> counts = new ArrayList<Integer>();
                   for (int i = 0; i < nrFiles; i++) {
                       counts.add(0);
                   }
                   counts.set(fileNr, count);
                   map.put(key, counts);
               }
           }
    }

    static void updateMapUsingTripleSubjectSubstringIntersection (String key, String triple, Integer count, HashMap<String, ArrayList<Integer>> map, int fileNr, int nrFiles) {
           if (map.containsKey(triple)) {
               ArrayList<Integer> counts = map.get(triple);
               counts.set(fileNr, count);
               map.put(triple, counts);
           }
           else {
               boolean match = false;
               Set keySet = map.keySet();
               Iterator<String> keys = keySet.iterator();
               while (keys.hasNext()) {
                   String iliTriple = keys.next();
                   String [] fields = iliTriple.split(":");
                   if (fields.length==3) {
                       String iliKey = fields[0];
                       String iliDuple = fields[1] + ":" + fields[2];
                       if (triple.endsWith(iliDuple)) {
                           if (intersectingILI(iliKey, key)) {
                               ArrayList<Integer> counts = map.get(iliTriple);
                               counts.set(fileNr, count);
                               map.put(iliTriple, counts);
                               match = true;
                               break;
                           }
                       }
                   }
                   else if (fields.length==2) {
                       String iliKey = fields[0];
                       String iliDuple = fields[1];
                       if (triple.endsWith(iliDuple)) {
                           if (intersectingILI(iliKey, key)) {
                               ArrayList<Integer> counts = map.get(iliTriple);
                               counts.set(fileNr, count);
                               map.put(iliTriple, counts);
                               match = true;
                               break;
                           }
                       }
                   }
               }
               if (!match) {
                   ArrayList<Integer> counts = new ArrayList<Integer>();
                   for (int i = 0; i < nrFiles; i++) {
                       counts.add(0);
                   }
                   counts.set(fileNr, count);
                   map.put(triple, counts);
               }
           }
    }
 static void updateMapUsingTripleSubjectSubstringIntersectionDebug (String key, String triple, Integer count, HashMap<String, ArrayList<Integer>> map, int fileNr, int nrFiles) {
           if (map.containsKey(triple)) {
               ArrayList<Integer> counts = map.get(triple);
               counts.set(fileNr, count);
               map.put(triple, counts);
           }
           else {
               boolean match = false;
               Set keySet = map.keySet();
               Iterator<String> keys = keySet.iterator();
               while (keys.hasNext()) {
                   String iliTriple = keys.next();
                   String [] fields = iliTriple.split(":");
                   if (fields.length==3) {
                       String iliKey = fields[0];
                       String iliDuple = fields[1] + ":" + fields[2];
                       if (triple.endsWith(iliDuple)) {
                           if (intersectingILI(iliKey, key)) {
                               ArrayList<Integer> counts = map.get(iliTriple);
                               counts.set(fileNr, count);
                               map.put(iliTriple, counts);
                               match = true;
                               break;
                           }
                       }
                   }
               }
               if (!match) {
                   ArrayList<Integer> counts = new ArrayList<Integer>();
                   for (int i = 0; i < nrFiles; i++) {
                       counts.add(0);
                   }
                   counts.set(fileNr, count);
                   map.put(triple, counts);
               }
           }
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
                        LEMMAEVENT = true;
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
                                updateMapUsingSubstringIntersection(key, count, iliEventMap, fileNr, nrFiles);
                               // updateMap(key, count, iliEventMap, fileNr, nrFiles);
                            }
                            else if (PREDICATE) {
                                updateMap(normalizePredicate(key), count, predicateMap, fileNr, nrFiles);
                            }
                        }
                        else if (fields.length==4) {
                            if (TRIPLE) {
                                String subject = fields[0];
                                if (TrigReader.getInstanceType(subject).equals("IEV")) {
                                    String predicate = normalizePredicate(fields[1]);
                                    String object = fields[2];
                                    String triple = subject + ":" + predicate + ":" + object;
                                    String so_pair = subject+":"+object;
                                    Integer count = Integer.parseInt(fields[3]);
                                    updateMapUsingTripleSubjectSubstringIntersection(subject, triple, count, tripleMap, fileNr, nrFiles);
                                    if (predicate.toLowerCase().endsWith("hasactor")
                                            ||
                                            predicate.toLowerCase().endsWith("hasplace")
                                            ) {
                                        updateMapUsingTripleSubjectSubstringIntersection(subject, triple, count, tripleSemMap, fileNr, nrFiles);
                                    }
                                    updateMapUsingTripleSubjectSubstringIntersection(subject, so_pair, count, subjectObjectMap, fileNr, nrFiles);
                                   // updateMap(triple, count, tripleMap, fileNr, nrFiles);
                                }
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


    static String normalizePredicate (String predicate) {
        String normalizedPredicate = predicate;
        if (predicate.equalsIgnoreCase("arg0")) {
            normalizedPredicate = "A0";
        }
        else if (predicate.equalsIgnoreCase("arg1")) {
            normalizedPredicate = "A1";
        }
        else if (predicate.equalsIgnoreCase("arg2")) {
            normalizedPredicate = "A2";
        }
        else if (predicate.equalsIgnoreCase("arg3")) {
            normalizedPredicate = "A3";
        }
        else if (predicate.equalsIgnoreCase("arg4")) {
            normalizedPredicate = "A4";
        }
        else {
            int idx = predicate.indexOf("@");
            if (idx>-1) {
                normalizedPredicate = predicate.substring(idx+1);
            }
        }
        return normalizedPredicate;
    }


    static void writeMapToStream(OutputStream tableFos, OutputStream overviewFos, HashMap<String, ArrayList<Integer>> map) throws IOException {
        String str = "";
        str += "\t";
        for (int i = 0; i < fileMap.size(); i++) {
            String fileName = fileMap.get(i);
            str += fileName + "\t";
            if (i>0) {
                str += "\t";
            }
        }
        str += "\n";

        tableFos.write(str.getBytes());

        int nReferenceKeys = 0;
        str = "\t\t";
        for (int i = 0; i < fileMap.size(); i++) {
            String fileName = fileMap.get(i);
            str += fileName + "\t"+"\t";
            if (i>0) {
                str += "\t\t\t";
            }
        }
        str += "\n";
        str += "\tTotal values\t";
        for (int i = 0; i < fileMap.size(); i++) {
            str += "Values\tReferences\t";
            if (i>0) {
                str += "Intersect"+"\t"+"macro C"+"\t"+"micro C"+"\t";
            }
        }
        str += "\n";
        overviewFos.write(str.getBytes());
        str = "";
        ArrayList<Integer> totalValues = new ArrayList<Integer>();
        ArrayList<Integer> totalReferences = new ArrayList<Integer>();
        ArrayList<Integer> correctCounts = new ArrayList<Integer>();
        ArrayList<Integer> proportionSum = new ArrayList<Integer>();
        for (int i = 0; i < fileMap.size(); i++) {
            proportionSum.add(0);
        }
        for (int i = 0; i < fileMap.size(); i++) {
            correctCounts.add(0);
        }
        for (int i = 0; i < fileMap.size(); i++) {
            totalValues.add(0);
        }
        for (int i = 0; i < fileMap.size(); i++) {
            totalReferences.add(0);
        }


        Set keySet = map.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            ArrayList<Integer> counts = map.get(key);
            str += key + "\t";
            int refCount = counts.get(0);
            str += refCount + "\t";
            if (refCount>0) {
                nReferenceKeys++;
                correctCounts.set(0, correctCounts.get(0)+refCount);
                if (correctCounts.get(0)>0) {
                    totalValues.set(0, totalValues.get(0)+1);
                    totalReferences.set(0, totalReferences.get(0)+correctCounts.get(0));
                }
            }

            for (int i = 1; i < counts.size(); i++) {
                Integer integer = counts.get(i);
                Double prop = 0.0;
                if (refCount==0) {
                    prop = 0.0;
                }
                else if (integer==0) {
                    prop=0.0;
                }
                else {
                    prop = (100*(double)integer)/(double)refCount;
                   // System.out.println("prop = " + prop);
                    if (prop>100) {
                        correctCounts.set(i, correctCounts.get(i)+refCount);
                        prop=100.0;
                    }
                    else {
                        correctCounts.set(i, correctCounts.get(i)+integer);
                    }
                    proportionSum.set(i, proportionSum.get(i)+prop.intValue());
                }
                if (integer>0) {
                    totalValues.set(i, totalValues.get(i)+1);
                    totalReferences.set(i, totalReferences.get(i)+integer);
                }
                str += integer.toString() + "\t"+prop+"\t";
            }
            str += "\n";
        }
        str += "\n";
        tableFos.write(str.getBytes());
        str = "";
        Integer baseline = correctCounts.get(0);
        str += "COVERAGE\t"+map.size()+"\t"+totalValues.get(0)+"\t"+correctCounts.get(0);
        for (int i = 1; i < proportionSum.size(); i++) {
            Integer sum = proportionSum.get(i);
            Integer correct = correctCounts.get(i);
            double macrorecall = (double)sum/nReferenceKeys;
            double microrecall = 100*(double)correct/(double)baseline;
            str +="\t"+totalValues.get(i)+"\t"+totalReferences.get(i)+"\t"+correct+"\t"+macrorecall+"\t"+microrecall;
        }
        str += "\n\n";

        overviewFos.write(str.getBytes());
    }

}
