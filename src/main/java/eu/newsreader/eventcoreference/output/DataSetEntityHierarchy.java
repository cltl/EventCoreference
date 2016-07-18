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
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--ont") && args.length>(i+1)) {
                hierarchyPath = args[i+1];
            }
            else if (arg.equals("--ent") && args.length>(i+1)) {
                entityPath = args[i+1];
            }
            else if (arg.equals("--title") && args.length>(i+1)) {
                title = args[i+1];
            }
        }
        SimpleTaxonomy simpleTaxonomy = new SimpleTaxonomy();
        simpleTaxonomy.readSimpleTaxonomyFromDbpFile(hierarchyPath);
        ArrayList<String> tops = simpleTaxonomy.getTops();
        System.out.println("tops.toString() = " + tops.toString());
        HashMap<String, ArrayList<PhraseCount>> cntPredicates = readEntityCountTypeTsv (simpleTaxonomy, entityPath, "//dbpedia.org/");
        HashMap<String, Integer> cnt = cntPhrases(cntPredicates);
        System.out.println("cntPredicates.size() = " + cntPredicates.size());
        System.out.println("Cumulating scores");
        simpleTaxonomy.cumulateScores("dbp:", tops, cnt);
        //int maxDepth = simpleTaxonomy.getMaxDepth(tops, 1);
        System.out.println("Building hierarchy");


        try {
            OutputStream fos = new FileOutputStream(entityPath+".words.html");
            //String scripts = TreeStaticHtml.makeScripts(cnt.size(), cntPredicates.size());
            String str = TreeStaticHtml.makeHeader(title)+ TreeStaticHtml.makeBodyStart(title, 0, 0, 0, 0);
            str += "<div id=\"Events\" class=\"tabcontent\">\n";
            str += "<div id=\"container\">\n";
            fos.write(str.getBytes());
            simpleTaxonomy.htmlTableTree(fos, "entity", "dbp:",tops, 1, cnt, cntPredicates);
            str = "</div></div>\n";
            str += TreeStaticHtml.bodyEnd;
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
       /* String str = TreeStaticHtml.makeHeader(title)+ TreeStaticHtml.bodyStart;
        str += "<div id=\"container\">\n";
        str += simpleTaxonomy.htmlTableTreeOverview("dbp:",tops, 1, cnt, cntPredicates);
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
        }*/

    }

    static public HashMap<String, Integer> cntPhrases (HashMap<String, ArrayList<PhraseCount>> map) {
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

    static public int totalPhrases (HashMap<String, ArrayList<PhraseCount>> map) {
        int n = 0;
        Set keySet = map.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            ArrayList<PhraseCount> phrases = map.get(key);
            n+= phrases.size();
        }
        return n;
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
                String nextEntity = "";
                String nextCount = "";
                String nextType = "";
                ArrayList<String> parents = new ArrayList<String>();
                while (in.ready() && (inputLine = in.readLine()) != null) {
                    // System.out.println(inputLine);
                    String[] fields = inputLine.split("\t");
                    if (fields.length == 3) {
                        nextEntity = fields[0];
                        nextCount = fields[1];
                        nextType = fields[2];
                        if (nextType.indexOf(prefix) > -1) {
                            int idx = nextType.lastIndexOf("/");
                            if (idx > -1) nextType = "dbp:" + nextType.substring(idx + 1);
                            if (entity.isEmpty()) {
                                /// special case for the first line
                                entity = nextEntity;
                                count = nextCount;
                                parents.add(nextType);
                            } else if (!entity.equals(nextEntity)) {
                                /// we have a new nextEntity so we need to save the entity data first
                                String child = simpleTaxonomy.getMostSpecificChild(parents);
                                // System.out.println("entity = "+entity+"count = "+count+" type = " + type);

                                /*if (entity.endsWith("Wiedeking")) {
                                    System.out.println("parents = " + parents);
                                    System.out.println("child = " + child);
                                }*/
                                if (!child.isEmpty()) {
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
                                /// now we clean the stuff to start a new data structure
                                parents = new ArrayList<String>();
                                parents.add(nextType);
                                entity = nextEntity;
                                count = nextCount;
                            } else {
                                //// this entity is the same so we just add the parent
                                //// the name and counts are the same
                                parents.add(nextType);
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
                System.out.println("last entity = " + entity);
                System.out.println("Entity map.size() = " + map.size());
            }
        } catch (IOException e) {
            e.printStackTrace();
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
