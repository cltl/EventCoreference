package eu.newsreader.eventcoreference.output;

import com.hp.hpl.jena.rdf.model.Statement;
import eu.newsreader.eventcoreference.input.TrigTripleData;
import eu.newsreader.eventcoreference.input.TrigTripleReader;
import eu.newsreader.eventcoreference.objects.PhraseCount;
import eu.newsreader.eventcoreference.storyline.JsonFromRdf;
import eu.newsreader.eventcoreference.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static eu.newsreader.eventcoreference.storyline.JsonFromRdf.getValue;

/**
 * Created by piek on 12/05/16.
 */
public class TrigStats {

    static HashMap<String, PhraseCount> dbpMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> enMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> neMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> authMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> citeMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> eventLabelMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> eventLabelWithoutESOMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> eventLabelWithoutFNMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> eventLabelWithoutILIMap = new HashMap<String, PhraseCount>();
    static HashMap<String, ArrayList<String>> eventMap = new HashMap<String, ArrayList<String>>();
    static HashMap<String, PhraseCount> esoMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> fnMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> iliMap = new HashMap<String, PhraseCount>();

    static void updateMap (String key, int nr, HashMap<String, PhraseCount> map) {
        if (map.containsKey(key)) {
            PhraseCount cnt = map.get(key);
            cnt.addCount(nr);
            map.put(key, cnt);
        }
        else {
            map.put(key, new PhraseCount(key, nr));
        }
    }

    static void dumpSortedMap (HashMap<String, PhraseCount> map, String outputPath) {
        SortedSet<PhraseCount> treeSet = new TreeSet<PhraseCount>(new PhraseCount.Compare());
        Set keySet = map.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            PhraseCount cnt = map.get(key);
            treeSet.add(cnt);
        }
        try {
            OutputStream fos = new FileOutputStream(outputPath);
            for (PhraseCount pcount : treeSet) {
                String str = pcount.getPhrase()+"\t"+pcount.getCount()+"\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void dumpTypeMap (HashMap<String, ArrayList<String>> map, HashMap<String, PhraseCount> mentionMap, String outputPath) {
        try {
            OutputStream fos = new FileOutputStream(outputPath); Set keySet = map.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                ArrayList<String> types = map.get(key);
                Collections.sort(types);
                Integer cnt = 0;
                if (mentionMap.containsKey(key)) {
                    PhraseCount pcount = mentionMap.get(key);
                    cnt = pcount.getCount();
                }
                String str = key+"\t"+cnt;
                for (int i = 0; i < types.size(); i++) {
                    String type = types.get(i);
                    str += "\t"+type;
                }
                str += "\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void main (String[] args) {
        String folderpath = "";
        folderpath = args[0];
        //folderpath = "/Users/piek/Desktop/NWR-INC/financialtimes/brexit4-ne";
        File inputFolder = new File(folderpath);
        System.out.println("inputFolder = " + inputFolder);
        ArrayList<File> files = Util.makeRecursiveFileList(inputFolder, ".trig");
        System.out.println(".trig files size() = " + files.size());
        TrigTripleData trigTripleData = TrigTripleReader.readInstanceTripleFromTrigFiles(files);
        Set keySet = trigTripleData.tripleMapInstances.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next(); //// this is the subject of the triple which should point to an event
            ArrayList<Statement> instanceTriples = trigTripleData.tripleMapInstances.get(key);
            int m = countMentions(instanceTriples);
            String type = getInstanceType(key);
            if (type.equalsIgnoreCase("dbp") || type.toLowerCase().endsWith(".dbp")) {
                updateMap(key, m, dbpMap);
            }
            else if (type.equalsIgnoreCase("en")) {
                updateMap(key, m, enMap);
            }
            else if (type.equalsIgnoreCase("ne")) {
                updateMap(key, m, neMap);
            }
            else if (key.indexOf("#ev")>-1) {
                ///this is an event

                ArrayList<String> eso = getTypeValuesFromInstanceStatement(instanceTriples, "eso");
                for (int i = 0; i < eso.size(); i++) {
                    String s = eso.get(i);
                    updateMap(s, m, esoMap);
                }
                ArrayList<String> fn = getTypeValuesFromInstanceStatement(instanceTriples, "fn");
                for (int i = 0; i < fn.size(); i++) {
                    String s = fn.get(i);
                    updateMap(s, m, fnMap);
                }
                ArrayList<String> ili = getTypeValuesFromInstanceStatement(instanceTriples, "ili");
                for (int i = 0; i < ili.size(); i++) {
                    String s = ili.get(i);
                    updateMap(s, m, iliMap);
                }
                ArrayList<String> labels = getLabelsFromInstanceStatement(instanceTriples);
                for (int i = 0; i < labels.size(); i++) {
                    String s = labels.get(i);
                    updateMap(s, m, eventLabelMap);
                    if (ili.size()==0) {
                        updateMap(s, m, eventLabelWithoutILIMap);
                    }
                    if (eso.size()==0) {
                        updateMap(s, m, eventLabelWithoutESOMap);
                    }
                    if (fn.size()==0) {
                        updateMap(s, m, eventLabelWithoutFNMap);
                    }
                    ArrayList<String> types = new ArrayList<String>();
                    if (eventMap.containsKey(s)) {
                        types = eventMap.get(s);
                    }
                    for (int j = 0; j < ili.size(); j++) {
                        String s1 = ili.get(j);
                        if (!types.contains(s1)) {
                            types.add(s1);
                        }
                    }
                    for (int j = 0; j < eso.size(); j++) {
                        String s1 = eso.get(j);
                        if (!types.contains(s1)) {
                            types.add(s1);
                        }
                    }
                    for (int j = 0; j < fn.size(); j++) {
                        String s1 = fn.get(j);
                        if (!types.contains(s1)) {
                            types.add(s1);
                        }
                    }
                    eventMap.put(s, types);
                }
            }
        }
        File folderParent = inputFolder.getParentFile();
        String outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".entities.xls";
        dumpSortedMap(enMap, outputFile);
        outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".nonentities.xls";
        dumpSortedMap(neMap, outputFile);
        outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".dbp.xls";
        dumpSortedMap(dbpMap, outputFile);
        outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".eventlabels.xls";
        dumpSortedMap(eventLabelMap, outputFile);
        outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".eventlabelsNOeso.xls";
        dumpSortedMap(eventLabelWithoutESOMap, outputFile);
        outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".eventlabelsNOfn.xls";
        dumpSortedMap(eventLabelWithoutFNMap, outputFile);
        outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".eventlabelsNOili.xls";
        dumpSortedMap(eventLabelWithoutILIMap, outputFile);
        outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".eso.xls";
        dumpSortedMap(esoMap, outputFile);
        outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".fn.xls";
        dumpSortedMap(fnMap, outputFile);
        outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".ili.xls";
        dumpSortedMap(iliMap, outputFile);
        outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".event.xls";
        dumpTypeMap(eventMap, eventLabelMap, outputFile);
    }

    static int countMentions (ArrayList<Statement> statements) {
        int n = 0;
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            String predicate = statement.getPredicate().getURI();
            if (predicate.endsWith("#denotedBy")) {
                n++;
            }
        }
        return n;
    }

    static String getInstanceType (String key) {
        String type = JsonFromRdf.getNameSpaceString(key);
        if (type.isEmpty()) {
            type = "event";
        }
        return type;
    }

    static ArrayList<String> getTypeValuesFromInstanceStatement (ArrayList<Statement> statements, String type) {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            String predicate = statement.getPredicate().getURI();
            if (predicate.endsWith("#type")) {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String[] values = object.split(",");
                for (int j = 0; j < values.length; j++) {
                    String value = values[j];
                    String property = JsonFromRdf.getNameSpaceString(value);
                    if (property.equalsIgnoreCase(type)) {
                        value = property + ":" + getValue(value);
                        if (!result.contains(value)) {
                            result.add(value);
                        }
                    }
                }
            }
        }
        return result;
    }

    static ArrayList<String>  getLabelsFromInstanceStatement (ArrayList<Statement> statements) {
            ArrayList<String> result = new ArrayList<String>();
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);

                String predicate = statement.getPredicate().getURI();
                if (predicate.endsWith("#label")) {
                    String object = "";
                    if (statement.getObject().isLiteral()) {
                        object = statement.getObject().asLiteral().toString();
                    } else if (statement.getObject().isURIResource()) {
                        object = statement.getObject().asResource().getURI();
                    }
                    String [] values = object.split(",");
                    for (int j = 0; j < values.length; j++) {
                        String value = values[j];
                        if (!result.contains(value)) {
                            result.add(value);
                        }
                    }
                }
            }
            return result;
    }

}
