package eu.newsreader.eventcoreference.output;

import eu.newsreader.eventcoreference.objects.PhraseCount;
import eu.newsreader.eventcoreference.util.TreeStaticHtml;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Created by piek on 29/05/16.
 */
public class DataSetConceptHierarchy {


    static public void main (String[] args) {
        String hierarchyPath = "";
        String conceptPath = "";
        String title = "";
        String querypath = "";
        hierarchyPath = "/Code/vu/newsreader/vua-resources/instance_types_en.ttl.bz2";
        conceptPath = "/Users/piek/Desktop/Vaccins/trig.nonentities.xls";
        title = "PostNL DBp ontology for entities";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--ont") && args.length>(i+1)) {
                hierarchyPath = args[i+1];
            }
            else if (arg.equals("--concepts") && args.length>(i+1)) {
                conceptPath = args[i+1];
            }
            else if (arg.equals("--title") && args.length>(i+1)) {
                title = args[i+1];
            }
            else if (arg.equals("--path") && args.length>(i+1)) {
                querypath = args[i+1];
            }
        }

        //// we make a map from type to concepts
        HashMap<String, ArrayList<PhraseCount>> cntPredicates = readConceptCountTypeTsv (conceptPath);

        SimpleTaxonomy simpleTaxonomy = new SimpleTaxonomy();
        simpleTaxonomy.readSimpleTaxonomyFromDbpFile(hierarchyPath, cntPredicates.keySet());
        ArrayList<String> tops = simpleTaxonomy.getTops();
        /*
        tops = new ArrayList<String>();
        tops.add("Organisation");
        tops.add("Place");
        tops.add("Person");*/
        System.out.println("tops.toString() = " + tops.toString());
        HashMap<String, Integer> cnt = cntPhrases(cntPredicates);
        System.out.println("cntPredicates.size() = " + cntPredicates.size());
        System.out.println("Cumulating scores");
        simpleTaxonomy.cumulateScores("dbp:", tops, cnt);
        //int maxDepth = simpleTaxonomy.getMaxDepth(tops, 1);
        System.out.println("Building hierarchy");


        try {
            OutputStream fos = new FileOutputStream(conceptPath+".words.html");
            String str = TreeStaticHtml.makeHeader(title)+ TreeStaticHtml.makeBodyStart(title, querypath, 0, 0, 0, 0);
            str += "<div id=\"Entities\" class=\"tabcontent\">\n";
            str += "<div id=\"container\">\n";
            fos.write(str.getBytes());
            simpleTaxonomy.htmlTableTree(fos, "entity", "dbp:",tops, 1, cnt, cntPredicates);
            str = "</div></div>\n";
            fos.write(str.getBytes());
            str = TreeStaticHtml.bodyEnd;
            fos.write(str.getBytes());
            fos.close();

            OutputStream jsonOut = new FileOutputStream(conceptPath+".words.json");
            JSONObject tree = new JSONObject();
            simpleTaxonomy.jsonTree(tree, "entity", "dbp:", tops, 1, cnt, cntPredicates, null);
            //jsonOut.write(tree.toString().getBytes());
            jsonOut.write(tree.toString(0).getBytes());
            jsonOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
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
http://www.newsreader-project.eu/data/wikinews/non-entities/none+nada+nothing+science+prove	1	dbp:0_(number)
http://www.newsreader-project.eu/data/wikinews/non-entities/report+dog+bite	2	dbp:11_Commission_Report
http://www.newsreader-project.eu/data/wikinews/non-entities/no+early+a.m.	1	dbp:12-hour_clock
http://www.newsreader-project.eu/data/wikinews/non-entities/category+hurricane	1	dbp:1926_Miami_hurricane
http://www.newsreader-project.eu/data/wikinews/non-entities/year+human	1	dbp:1963
http://www.newsreader-project.eu/data/wikinews/non-entities/group+e+5im+n	1	dbp:1994_FIFA_World_Cup
     */
    static public HashMap<String, ArrayList<PhraseCount>> readConceptCountTypeTsv (String filePath) {
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
                String concept = "";
                String count = "";
                String type = "";
                ArrayList<String> parents = new ArrayList<String>();
                while (in.ready() && (inputLine = in.readLine()) != null) {
                    // System.out.println(inputLine);
                    String[] fields = inputLine.split("\t");
                    if (fields.length == 3) {
                        concept = fields[0].trim();
                        count = fields[1].trim();
                        type = fields[2].trim();
                        PhraseCount phraseCount = new PhraseCount(concept, Integer.parseInt(count));
                        if (phraseCount.getCount() > 1) {
                            if (map.containsKey(type)) {
                                ArrayList<PhraseCount> phrases = map.get(type);
                                phrases.add(phraseCount);
                                map.put(type, phrases);
                            } else {
                                ArrayList<PhraseCount> phrases = new ArrayList<PhraseCount>();
                                phrases.add(phraseCount);
                                map.put(type, phrases);
                            }
                        }
                    }
                }
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
