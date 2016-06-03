package eu.newsreader.eventcoreference.output;

import eu.newsreader.eventcoreference.objects.PhraseCount;
import eu.newsreader.eventcoreference.util.TreeStaticHtml;
import org.apache.tools.bzip2.CBZip2InputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Created by piek on 29/05/16.
 */
public class DataSetEntityHierarchy {


    static public void main (String[] args) {
        String hierarchyPath = "";
        String entityPath = "";
        String title = "";
        hierarchyPath = "/Users/piek/Desktop/NWR-INC/dasym/stats/counted_types_unsorted.tsv";
        entityPath = "/Users/piek/Desktop/NWR-INC/dasym/stats/dump.dbp.types.tsv";
        title = "PostNL DBp ontology for entities";

        SimpleTaxonomy simpleTaxonomy = new SimpleTaxonomy();
        simpleTaxonomy.readSimpleTaxonomyFromDbpFile(hierarchyPath);
        ArrayList<String> tops = simpleTaxonomy.getTops();
        System.out.println("tops.toString() = " + tops.toString());
        HashMap<String, ArrayList<PhraseCount>> cntPredicates = readEntityCountTypeTsv (simpleTaxonomy, entityPath, "//dbpedia.org/ontology");
        HashMap<String, Integer> cnt = cntEntities(cntPredicates);
        System.out.println("cntPredicates.size() = " + cntPredicates.size());
        simpleTaxonomy.cumulateScores("dbp:", tops, cnt);
        int maxDepth = simpleTaxonomy.getMaxDepth(tops, 1);
        String str = TreeStaticHtml.makeHeader(title)+ TreeStaticHtml.bodyStart;
        str += "<div id=\"container\">\n";
        //str += esoReader.htmlTableTree("eso:",tops, 1, cnt, maxDepth);
        str += simpleTaxonomy.htmlTableTree("dbp:",tops, 1, cnt, cntPredicates, maxDepth);
        str += "</div>\n";
        str += TreeStaticHtml.bodyEnd;
        //System.out.println(str);
        //esoReader.printTree(tops, 0, cnt);

        try {
            OutputStream fos = new FileOutputStream(entityPath+".words.html");
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        str = TreeStaticHtml.makeHeader(title)+ TreeStaticHtml.bodyStart;
        str += "<div id=\"container\">\n";
        str += simpleTaxonomy.htmlTableTree("dbp:",tops, 1, cnt, maxDepth);
        str += "</div>\n";
        str += TreeStaticHtml.bodyEnd;
        //System.out.println(str);
        //esoReader.printTree(tops, 0, cnt);

        try {
            OutputStream fos = new FileOutputStream(entityPath+".overview.html");
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static public HashMap<String, Integer> cntEntities (HashMap<String, ArrayList<PhraseCount>> map) {
        HashMap<String, Integer> countMap = new HashMap<String, Integer>();
        Set keySet = map.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            ArrayList<PhraseCount> phrases = map.get(key);
            Integer sum = 0;
            for (int i = 0; i < phrases.size(); i++) {
                PhraseCount phraseCount = phrases.get(i);
                sum += phraseCount.getCount();
            }
            countMap.put(key, sum);
        }
        return countMap;
    }

    /*
    http://nl.dbpedia.org/resource/Maxime_Verhagen	96	http://www.w3.org/2002/07/owl#Thing
http://nl.dbpedia.org/resource/Maxime_Verhagen	96	http://xmlns.com/foaf/0.1/Person
http://nl.dbpedia.org/resource/Maxime_Verhagen	96	http://dbpedia.org/ontology/Agent
http://nl.dbpedia.org/resource/Maxime_Verhagen	96	http://dbpedia.org/ontology/Person
http://nl.dbpedia.org/resource/Maxime_Verhagen	96	http://dbpedia.org/ontology/Politician
http://nl.dbpedia.org/resource/Maxime_Verhagen	96	http://schema.org/Person
http://nl.dbpedia.org/resource/Maxime_Verhagen	96	http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Agent
http://nl.dbpedia.org/resource/Maxime_Verhagen	96	http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#NaturalPerson
http://nl.dbpedia.org/resource/Maxime_Verhagen	96	http://wikidata.dbpedia.org/resource/Q215627
http://nl.dbpedia.org/resource/Maxime_Verhagen	96	http://wikidata.dbpedia.org/resource/Q5
http://nl.dbpedia.org/resource/Maxime_Verhagen	96	http://wikidata.dbpedia.org/resource/Q82955
     */
    static public HashMap<String, ArrayList<PhraseCount>> readEntityCountTypeTsv (SimpleTaxonomy simpleTaxonomy, String filePath, String prefix) {
        HashMap<String, ArrayList<PhraseCount>> map = new HashMap<String, ArrayList<PhraseCount>>();
        try {
            InputStreamReader isr = null;
            if (filePath.toLowerCase().endsWith(".gz")) {
                try {
                    InputStream fileStream = new FileInputStream(filePath);
                    InputStream gzipStream = new GZIPInputStream(fileStream);
                    isr = new InputStreamReader(gzipStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (filePath.toLowerCase().endsWith(".bz2")) {
                try {
                    InputStream fileStream = new FileInputStream(filePath);
                    InputStream gzipStream = new CBZip2InputStream(fileStream);
                    isr = new InputStreamReader(gzipStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                FileInputStream fis = new FileInputStream(filePath);
                isr = new InputStreamReader(fis);
            }
            if (isr!=null) {
                BufferedReader in = new BufferedReader(isr);
                String inputLine = "";
                String entity = "";
                String count = "";
                ArrayList<String> parents = new ArrayList<String>();
                while (in.ready() && (inputLine = in.readLine()) != null) {
                    // System.out.println(inputLine);
                    String[] fields = inputLine.split("\t");
                    if (fields.length == 3) {
                        String nextEntity = fields[0];
                        String nextCount = fields[1];
                        String nextType = fields[2];
                        if (nextType.indexOf(prefix) > -1) {
                            int idx = nextEntity.lastIndexOf("/");
                            //if (idx>-1) nextEntity = "dbp:"+nextEntity.substring(idx+1);
                            idx = nextType.lastIndexOf("/");
                            if (idx>-1) nextType = "dbp:"+nextType.substring(idx+1);
                            parents.add(nextType);
                            if (entity.isEmpty()) {
                                entity = nextEntity;
                                count = nextCount;
                            }
                            else if (!entity.equals(nextEntity)) {
                                String child = simpleTaxonomy.getMostSpecificChild(parents);
                                // System.out.println("entity = "+entity+"count = "+count+" type = " + type);
                                PhraseCount phraseCount = new PhraseCount(entity, Integer.parseInt(count));
                                if (map.containsKey(child)) {
                                    ArrayList<PhraseCount> phrases = map.get(child);
                                    phrases.add(phraseCount);
                                    map.put(child, phrases);
                                } else {
                                    ArrayList<PhraseCount> phrases = new ArrayList<PhraseCount>();
                                    phrases.add(phraseCount);
                                    map.put(child, phrases);
                                }
                                parents = new ArrayList<String>();
                                entity = nextEntity;
                                count = nextCount;
                            }
                        }
                    }
                }
                if (!entity.isEmpty()) {
                    String child = simpleTaxonomy.getMostSpecificChild(parents);
                    // System.out.println("entity = "+entity+"count = "+count+" type = " + type);
                    PhraseCount phraseCount = new PhraseCount(entity, Integer.parseInt(count));
                    if (map.containsKey(child)) {
                        ArrayList<PhraseCount> phrases = map.get(child);
                        phrases.add(phraseCount);
                        map.put(child, phrases);
                    } else {
                        ArrayList<PhraseCount> phrases = new ArrayList<PhraseCount>();
                        phrases.add(phraseCount);
                        map.put(child, phrases);
                    }
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return map;
    }



    static public HashMap<String, ArrayList<PhraseCount>> readEntityCountTypeTsv (String filePath, String prefix) {
        HashMap<String, ArrayList<PhraseCount>> map = new HashMap<String, ArrayList<PhraseCount>>();
        try {
            InputStreamReader isr = null;
            if (filePath.toLowerCase().endsWith(".gz")) {
                try {
                    InputStream fileStream = new FileInputStream(filePath);
                    InputStream gzipStream = new GZIPInputStream(fileStream);
                    isr = new InputStreamReader(gzipStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (filePath.toLowerCase().endsWith(".bz2")) {
                try {
                    InputStream fileStream = new FileInputStream(filePath);
                    InputStream gzipStream = new CBZip2InputStream(fileStream);
                    isr = new InputStreamReader(gzipStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                FileInputStream fis = new FileInputStream(filePath);
                isr = new InputStreamReader(fis);
            }
            if (isr!=null) {
                BufferedReader in = new BufferedReader(isr);
                String inputLine = "";
                String previousLine = "";
                while (in.ready() && (inputLine = in.readLine()) != null) {
                    // System.out.println(inputLine);
                    String[] fields = inputLine.split("\t");
                    if (fields.length == 3) {
                        String entity = fields[0];
                        String count = fields[1];
                        String type = fields[2];
                        if (type.indexOf(prefix) > -1) {
                            previousLine = inputLine.trim();
                        } else if (!previousLine.isEmpty()) {
                            fields = previousLine.split("\t");
                            if (fields.length == 3) {
                                entity = fields[0];
                                int idx = entity.lastIndexOf("/");
                                if (idx>-1) entity = "dbp:"+entity.substring(idx+1);
                                count = fields[1];
                                type = fields[2];
                                idx = type.lastIndexOf("/");
                                if (idx>-1) type = "dbp:"+type.substring(idx+1);
                               // System.out.println("entity = "+entity+"count = "+count+" type = " + type);
                                PhraseCount phraseCount = new PhraseCount(entity, Integer.parseInt(count));
                                if (map.containsKey(type)) {
                                    ArrayList<PhraseCount> phrases = map.get(type);
                                    phrases.add(phraseCount);
                                    map.put(type, phrases);
                                } else {
                                    ArrayList<PhraseCount> phrases = new ArrayList<PhraseCount>();
                                    phrases.add(phraseCount);
                                    map.put(type, phrases);
                                }
                                previousLine = "";
                            }
                        }
                    }
                }
                if (!previousLine.isEmpty()) {
                    String[] fields = previousLine.split("\t");
                    if (fields.length == 3) {
                        String entity = fields[0];
                        int idx = entity.lastIndexOf("/");
                        if (idx>-1) entity = "dbp:"+entity.substring(idx+1);
                        String count = fields[1];
                        String type = fields[2];
                        idx = type.lastIndexOf("/");
                        if (idx>-1) type = "dbp:"+type.substring(idx+1);
                        PhraseCount phraseCount = new PhraseCount(entity,Integer.parseInt(count));
                        if (map.containsKey(type)) {
                            ArrayList<PhraseCount> phrases = map.get(type);
                            phrases.add(phraseCount);
                            map.put(type, phrases);
                        }
                        else {
                            ArrayList<PhraseCount> phrases = new ArrayList<PhraseCount>();
                            phrases.add(phraseCount);
                            map.put(type, phrases);
                        }
                    }
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return map;
    }


}
