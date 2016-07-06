package eu.newsreader.eventcoreference.output;

import com.hp.hpl.jena.rdf.model.Statement;
import eu.newsreader.eventcoreference.input.TrigTripleData;
import eu.newsreader.eventcoreference.input.TrigTripleReader;
import eu.newsreader.eventcoreference.objects.PhraseCount;
import eu.newsreader.eventcoreference.storyline.JsonFromRdf;
import eu.newsreader.eventcoreference.storyline.JsonStoryUtil;
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
    static HashMap<String, ArrayList<String>> entityMap = new HashMap<String, ArrayList<String>>();
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
    static void updateMentionMap (String key,  HashMap<String, ArrayList<String>> map, ArrayList<String> mentions) {
        if (map.containsKey(key)) {
            ArrayList<String> mMentions = map.get(key);
            for (int i = 0; i < mentions.size(); i++) {
                String mention = mentions.get(i);
                if (!mMentions.contains(mention)) {
                    mMentions.add(mention);
                    map.put(key, mMentions);
                }
            }
        }
        else {
            map.put(key, mentions);
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

    static void dumpSortedMap (HashMap<String, ArrayList<String>> mentionMap, HashMap<String, PhraseCount> map, String outputPath) {
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
                String str = pcount.getPhrase()+"\t"+pcount.getCount();
                if (mentionMap.containsKey(pcount.getPhrase())) {
                    ArrayList<String> mentions = mentionMap.get(pcount.getPhrase());
                    for (int i = 0; i < mentions.size(); i++) {
                        String mention = mentions.get(i);
                        str += "\t"+mention;
                    }
                }
                str +="\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void dumpTypeMap (HashMap<String, ArrayList<String>> map, HashMap<String, PhraseCount> mentionMap, String outputPath) {
        try {
            OutputStream fos = new FileOutputStream(outputPath);
            Set keySet = map.keySet();
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
        String type = "instance";
        folderpath = "/Users/piek/Desktop/NWR-INC/financialtimes/data/brexit6.naf";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--trig-folder") && args.length>(i+1)) {
                folderpath = args[i+1];
            }
            else if (arg.equals("--type") && args.length>(i+1)) {
                type = args[i+1];
            }
        }
        File inputFolder = new File(folderpath);
        System.out.println("inputFolder = " + inputFolder);
        ArrayList<File> files = Util.makeRecursiveFileList(inputFolder, ".trig");
        System.out.println(".trig files size() = " + files.size());
        if (type.equals("instance")) processInstances(inputFolder, files);
        if (type.equals("grasp")) processGrasp(inputFolder, files);

    }

    static void processInstances (File inputFolder, ArrayList<File> files) {
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
                ArrayList<String> mentionLabels = getLabelsFromInstanceStatement(instanceTriples);
                updateMentionMap(key, entityMap, mentionLabels);
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
        String outputFile = "";
        outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".entities.xls";
        dumpSortedMap(enMap, outputFile);
        outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".nonentities.xls";
        dumpSortedMap(neMap, outputFile);
        outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".dbp.xls";
        dumpSortedMap(entityMap, dbpMap, outputFile);
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

    static void processGrasp (File inputFolder, ArrayList<File> files) {
        TrigTripleData trigTripleData = TrigTripleReader.readGraspTripleFromTrigFiles(files);
        getSourceStats(trigTripleData);
        normaliseMap(citeMap);
        File folderParent = inputFolder.getParentFile();
        String outputFile = "";
        outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".cited.xls";
        dumpSortedMap(citeMap, outputFile);
        outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".author.xls";
        dumpSortedMap(authMap, outputFile);
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



    public static void getSourceStats(TrigTripleData trigTripleData) {
        Set keySet = trigTripleData.tripleMapGrasp.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            ArrayList<Statement> perspectiveTriples = trigTripleData.tripleMapGrasp.get(key);
            for (int i = 0; i < perspectiveTriples.size(); i++) {
                Statement statement = perspectiveTriples.get(i);
                String predicate = statement.getPredicate().getURI();
                String object = statement.getObject().toString();
                if (predicate.endsWith("#hasAttribution")) {
                    if (trigTripleData.tripleMapGrasp.containsKey(object)) {
                        ArrayList<Statement> perspectiveValues = trigTripleData.tripleMapGrasp.get(object);
                        for (int j = 0; j < perspectiveValues.size(); j++) {
                            Statement statement1 = perspectiveValues.get(j);
                            if (statement1.getPredicate().getURI().endsWith("#wasAttributedTo")) {
                                if (trigTripleData.tripleMapGrasp.containsKey(statement1.getObject().toString())) {
                                    //// this means the source has properties so it is likely to be the document with an author
                                    ArrayList<Statement> provStatements = trigTripleData.tripleMapGrasp.get(statement1.getObject().toString());
                                    for (int k = 0; k < provStatements.size(); k++) {
                                        Statement statement2 = provStatements.get(k);
                                        String authorString = statement2.getObject().toString();
                                        int idx = authorString.lastIndexOf("/");
                                        if (idx > -1) {
                                            authorString = authorString.substring(idx + 1);
                                        }
                                       // System.out.println("authorString = " + authorString);
                                        ArrayList<String> authorAnd = Util.splitSubstring(authorString, "+and+");
                                        for (int l = 0; l < authorAnd.size(); l++) {
                                            String subauthor = authorAnd.get(l);
                                            ArrayList<String> subauthorfields= Util.splitSubstring(subauthor, "%2C+");
                                            for (int m = 0; m < subauthorfields.size(); m++) {
                                                String subfield = subauthorfields.get(m);
                                               // System.out.println("subfield = " + subfield);
                                                if (!subfield.toLowerCase().endsWith("correspondent")
                                                        && !subfield.toLowerCase().endsWith("reporter")
                                                        && !subfield.toLowerCase().endsWith("editor")
                                                        && !subfield.toLowerCase().startsWith("in+")
                                                        ) {
                                                    String author = JsonStoryUtil.normalizeSourceValue(subfield);
                                                    author =  JsonStoryUtil.cleanAuthor(author);
                                                    updateMap(author, 1, authMap);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    //// it is not the document so a cited source
                                    String cite = statement1.getObject().toString();
                                    int idx = cite.lastIndexOf("/");
                                    if (idx > -1) {
                                        cite = cite.substring(idx + 1);
                                    }
/*                                  THIS DOES NOT WORK: PRONOUNS, RECEPTIONIST, ETC...
                                    //// There can be source documents without meta data.
                                    //// In that case, there are no triples for in tripleMapGrasp with this subject but it is still a document
                                    //// The next hack checks for upper case characters in the URI
                                    //// If they are present, we assume it is somebody otherwise we assume it is a document and we assign it to the meta string

                                    if (cite.toLowerCase().equals(cite)) {
                                        //// no uppercase characters
                                        cite = meta;
                                    }
*/
                                    if (cite.toLowerCase().equals(cite)) {
                                        //// no uppercase characters
                                        cite = "someone";
                                    }
                                    cite= JsonStoryUtil.normalizeSourceValue(cite);
                                    cite = JsonStoryUtil.cleanCite(cite);
                                    updateMap(cite, 1, citeMap);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static public HashMap<String, String> getFullNameMap (HashMap<String, PhraseCount> map) {
        HashMap<String, String> fullNameMap = new HashMap<String, String>();
        Vector<String> singleNames = new Vector<String>();
        Set keySet = map.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            String [] fields = key.split("_");
            if (fields.length==1) {
                String lastName = Util.getCamelName(key);
                if (lastName.isEmpty()) {
                    singleNames.add(key);
                }
            }
        }
        System.out.println("singleNames = " + singleNames.toString());
        /// we now have last name only entries
        /// we iterate again
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            String [] fields = key.split("_");
            if (fields.length>1) {
                String lastField = fields[fields.length-1];
                if (singleNames.contains(lastField)) {
                    fullNameMap.put(lastField, key);
                }
            }
            else {
                String lastName = Util.getCamelName(key);
                if (!lastName.isEmpty()) {
                    if (singleNames.contains(lastName)) {
                        fullNameMap.put(lastName, key);
                    }
                }
            }
        }
        return fullNameMap;
    }

    static public void normaliseMap (HashMap<String, PhraseCount> map) {
        System.out.println("map.size() = " + map.size());
        HashMap<String, PhraseCount> newMap = new HashMap<String, PhraseCount>();
        Vector<String> normalisedKey = new Vector<String>();
        HashMap<String, String> fullNameMap = getFullNameMap(map);
        Set keySet = map.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            PhraseCount phraseCount = map.get(key);
            if (fullNameMap.containsKey(key)) {
                String fullName = fullNameMap.get(key);
                PhraseCount fullPhraseCount = map.get(fullName);
                fullPhraseCount.addCount(phraseCount.getCount());
                map.put(fullName, fullPhraseCount);
                normalisedKey.add(key);
            }
        }
        /// we now have last name only entries
        /// we iterate again
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            PhraseCount phraseCount = map.get(key);
            if (!normalisedKey.contains(key)) {
                newMap.put(key, phraseCount);
            }
        }
        System.out.println("newMap.size() = " + newMap.size());
        map = newMap;
    }

}
