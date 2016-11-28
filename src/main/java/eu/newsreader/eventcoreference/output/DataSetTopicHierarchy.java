package eu.newsreader.eventcoreference.output;

import eu.newsreader.eventcoreference.objects.PhraseCount;
import eu.newsreader.eventcoreference.util.EuroVoc;
import eu.newsreader.eventcoreference.util.TreeStaticHtml;
import org.apache.tools.bzip2.CBZip2InputStream;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by piek on 29/05/16.
 */
public class DataSetTopicHierarchy {
    static EuroVoc euroVoc = new EuroVoc();

    /*
    if (!eurovocBlackList.startsWith(topic)) {
                                if (!eurovocBlackList.uriLabelMap.containsKey(topic)) {
                                    count(totalTopicCount, topic);
                                    givenTopics.add(topic);
                                } else {
                                 //   System.out.println("blacklist topic = " + topic);
                                }
                            }
     */

    static public void main (String[] args) {
        String euroVocFile = "";
        String topicPath = "";
        String hierarchyPath = "";
        String title = "";
        String querypath = "";
        hierarchyPath = "/Code/vu/newsreader/vua-resources/eurovoc_in_skos_core_concepts.rdf.gz";
        euroVocFile = "/Code/vu/newsreader/vua-resources/mapping_eurovoc_skos.csv.gz";
        euroVocFile = "/Code/vu/newsreader/vua-resources/mapping_eurovoc_skos.label.concept.gz";
        //topicPath = "/Users/piek/Desktop/NWR-INC/dasym/dasym_trig.topics.xls";
        topicPath = "/Users/piek/Desktop/NWR-INC/dasym/topics/topic.cnt";
        title = "PostNL topics";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--eurovoc") && args.length>(i+1)) {
                euroVocFile = args[i+1];
            }
            else if (arg.equals("--skos") && args.length>(i+1)) {
                hierarchyPath = args[i+1];
            }
            else if (arg.equals("--topic") && args.length>(i+1)) {
                topicPath = args[i+1];
            }
            else if (arg.equals("--title") && args.length>(i+1)) {
                title = args[i+1];
            }
            else if (arg.equals("--path") && args.length>(i+1)) {
                querypath = args[i+1];
            }
        }


        euroVoc.readEuroVoc(euroVocFile, "en");

        SimpleTaxonomy simpleTaxonomy = new SimpleTaxonomy();
        simpleTaxonomy.readSimpleTaxonomyFromSkosFile(hierarchyPath);
        //simpleTaxonomy.printTree();

        ArrayList<String> tops = simpleTaxonomy.getTops();

        System.out.println("tops = " + tops.size());
        HashMap<String, ArrayList<PhraseCount>> cntPredicates = readTopicCountTypeGrep(simpleTaxonomy, topicPath, euroVoc);
       // HashMap<String, ArrayList<PhraseCount>> cntPredicates = readTopicCountTypeTsv(simpleTaxonomy, topicPath, euroVoc);
        HashMap<String, Integer> cnt = cntPhrases(cntPredicates);
        System.out.println("cntPredicates.size() = " + cntPredicates.size());
        System.out.println("Cumulating scores");
       // simpleTaxonomy.cumulateScores("", tops, cnt);
        //int maxDepth = simpleTaxonomy.getMaxDepth(tops, 1);
        System.out.println("Building hierarchy");


        try {
            OutputStream fos = new FileOutputStream(topicPath+".words.html");
            String str = TreeStaticHtml.makeHeader(title)+ TreeStaticHtml.makeBodyStart(title, querypath, 0, 0, 0, 0);
            str += "<div id=\"Topics\" class=\"tabcontent\">\n";
            str += "<div id=\"container\">\n";
            fos.write(str.getBytes());
            simpleTaxonomy.htmlTableTopicTree(fos, "topic",tops, 1, cnt, cntPredicates);

//            htmlTableList(fos, "topic", cnt);
            str = "</div></div>\n";
            fos.write(str.getBytes());
            str = TreeStaticHtml.bodyEnd;
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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



    static public HashMap<String, Integer> readSimpleTaxonomyFromEuroVocFile (String filePath) {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
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
                String inputLine;
                while (in.ready() && (inputLine = in.readLine()) != null) {
                    // System.out.println(inputLine);
                    inputLine = inputLine.trim();
                    if (inputLine.trim().length() > 0) {
                        String[] fields = inputLine.split("\t");
                        if (fields.length == 2) {
                            /// eurovoc:675528
                            String concept = fields[0].replace("eurovoc:", "http://eurovoc.europa.eu/");
                           // System.out.println("concept = " + concept);
                            String contString = fields[1];
                            System.out.println("contString = " + contString);
                            Integer cnt = -1;
                            try {
                                cnt = Integer.parseInt(contString);
                            } catch (NumberFormatException e) {
                            }
                            if (cnt>1) {
                                //// ignore topics with frequency 1
                                /*if (euroVoc.uriLabelMap.containsKey(concept)) {
                                    concept = euroVoc.uriLabelMap.get(concept);
                                }
                                else {
                                   // System.out.println("concept = " + concept);
                                }*/
                                System.out.println("concept = " + concept+":"+cnt);
                                map.put(concept,cnt);
                            }
                        }
                        else {
                            System.out.println("Skipping line:"+inputLine);
                        }
                    }
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return map;
    }

    public static void  htmlTableList(OutputStream fos, String type,
                                      HashMap<String, Integer> typeCounts) throws IOException {
        String str = "";
        ArrayList<PhraseCount> countedTops = new ArrayList<PhraseCount>();
        Set keySet = typeCounts.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            Integer cnt = typeCounts.get(key);
            PhraseCount phraseCount = new PhraseCount(key, cnt);
            countedTops.add(phraseCount);
        }
        Collections.sort(countedTops, new Comparator<PhraseCount>() {
            @Override
            public int compare(PhraseCount p1, PhraseCount p2) {

                return p2.getCount().compareTo(p1.getCount());
            }
        });
        Collections.sort(countedTops, new Comparator<PhraseCount>() {
            @Override
            public int compare(PhraseCount p1, PhraseCount p2) {

                return p2.getPhrase().compareTo(p1.getPhrase());
            }
        });

        for (int i = 0; i < countedTops.size(); i++) {
            PhraseCount topCount = countedTops.get(i);
            str = "<h2>\n";
            String tb = TreeStaticHtml.makeTickBox(type, topCount.getPhrase() );
            str += "<div id=\"cell\">" + topCount.getPhraseCount();
            str += tb;
            str +=  "</div>";
            str += "\n</h2>\n";
            fos.write(str.getBytes());
        }
    }

    static public HashMap<String, ArrayList<PhraseCount>> readTopicCountTypeTsv (SimpleTaxonomy simpleTaxonomy, String filePath, EuroVoc euroVoc) {
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
                while (in.ready() && (inputLine = in.readLine()) != null) {
                    // System.out.println(inputLine);
                    inputLine = inputLine.trim();
                    if (inputLine.trim().length() > 0) {
                        String[] fields = inputLine.split("\t");
                        if (fields.length == 2) {
                            /// eurovoc:675528
                            String concept = fields[0].replace("eurovoc:", "http://eurovoc.europa.eu/");
                            // System.out.println("concept = " + concept);
                            String contString = fields[1];
                            Integer cnt = -1;
                            try {
                                cnt = Integer.parseInt(contString);
                            } catch (NumberFormatException e) {
                            }
                            if (cnt>1) {
                                //// ignore topics with frequency 1
                                /*if (euroVoc.uriLabelMap.containsKey(concept)) {
                                    concept = euroVoc.uriLabelMap.get(concept);
                                }
                                else {
                                   // System.out.println("concept = " + concept);
                                }*/
                                if (euroVoc.uriLabelMap.containsKey(concept)) {
                                    String label = euroVoc.uriLabelMap.get(concept);
                                    if (simpleTaxonomy.labelToConcept.containsKey(label)) {
                                        String type = simpleTaxonomy.labelToConcept.get(label);
                                        PhraseCount phraseCount = new PhraseCount(label, cnt);
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
                        else {
                            System.out.println("Skipping line:"+inputLine);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    static public HashMap<String, ArrayList<PhraseCount>> readTopicCountTypeGrep (SimpleTaxonomy simpleTaxonomy, String filePath, EuroVoc euroVoc) {
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
                while (in.ready() && (inputLine = in.readLine()) != null) {
                    // System.out.println(inputLine);
                    inputLine = inputLine.trim();
                    if (inputLine.trim().length() > 0) {
                        String[] fields = inputLine.split("skos:relatedMatch");
                        if (fields.length == 2) {
                            /// eurovoc:675528
                            String concept = fields[1].trim();
                            int idx_e = concept.lastIndexOf(">");
                            if (idx_e>-1) {
                                concept = concept.substring(1,idx_e);
                            }
                            System.out.println("concept = " + concept);
                            String countString = fields[0].trim();
                            System.out.println("countString = " + countString);
                            Integer cnt = -1;
                            try {
                                cnt = Integer.parseInt(countString);
                            } catch (NumberFormatException e) {
                            }
                            if (cnt>1) {
                                //// ignore topics with frequency 1
                                /*if (euroVoc.uriLabelMap.containsKey(concept)) {
                                    concept = euroVoc.uriLabelMap.get(concept);
                                }
                                else {
                                   // System.out.println("concept = " + concept);
                                }*/
                                if (euroVoc.uriLabelMap.containsKey(concept)) {
                                    String label = euroVoc.uriLabelMap.get(concept);
                                    if (simpleTaxonomy.labelToConcept.containsKey(label)) {
                                        String type = simpleTaxonomy.labelToConcept.get(label);
                                        PhraseCount phraseCount = new PhraseCount(label, cnt);
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
                                    else {
                                        System.out.println("Could not find label = " + label);
                                    }
                                }
                                else {
                                    System.out.println("Could not find concept = " + concept);
                                }
                            }
                        }
                        else {
                            System.out.println("Skipping line:"+inputLine);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

}
