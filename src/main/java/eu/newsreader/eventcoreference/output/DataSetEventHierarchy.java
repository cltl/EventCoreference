package eu.newsreader.eventcoreference.output;

import eu.newsreader.eventcoreference.input.EsoReader;
import eu.newsreader.eventcoreference.objects.PhraseCount;
import eu.newsreader.eventcoreference.util.TreeStaticHtml;
import org.apache.tools.bzip2.CBZip2InputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import static eu.newsreader.eventcoreference.output.DataSetEntityHierarchy.cntPhrases;
import static eu.newsreader.eventcoreference.output.DataSetEntityHierarchy.totalPhrases;

/**
 * Created by piek on 15/04/16.
 */
public class DataSetEventHierarchy {



    static public void main (String[] args) {
        String esoPath = "";
        String eventPath = "";
        String title = "";
        esoPath = "/Users/piek/Desktop/NWR/eso/ESO.v2/ESO_V2_Final.owl";
        eventPath = "/Users/piek/Desktop/NWR-INC/dasym/stats/dump.topic.trig.event.xls";
        title = "PostNL ESO ontology events";

        eventPath = "/Users/piek/Desktop/NWR-INC/financialtimes/stats/brexit4-ne.event.xls";
        title = "Brexit ESO ontology events";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--eso") && args.length>(i+1)) {
                esoPath = args[i+1];
            }
            else if (arg.equals("--event") && args.length>(i+1)) {
                eventPath = args[i+1];
            }
            else if (arg.equals("--title") && args.length>(i+1)) {
                title = args[i+1];
            }
        }
        System.out.println("esoPath = " + esoPath);
        System.out.println("counts for eventPath = " + eventPath);
        System.out.println("title = " + title);
        EsoReader esoReader = new EsoReader();
        esoReader.parseFile(esoPath);
        HashMap<String, ArrayList<PhraseCount>> cntPredicates = readEventCountTypeTsv (eventPath, "eso");
        HashMap<String, Integer> cnt = cntPhrases(cntPredicates);
        System.out.println("cntPredicates.size() = " + cntPredicates.size());
        System.out.println("Cumulating scores");
        ArrayList<String> tops = esoReader.simpleTaxonomy.getTops();
        esoReader.simpleTaxonomy.cumulateScores("eso:", tops, cnt);
       // int maxDepth = esoReader.simpleTaxonomy.getMaxDepth(tops, 1);


        try {
            OutputStream fos = new FileOutputStream(eventPath+".words.html");
            int nPhrases = totalPhrases(cntPredicates);
            //String scripts = TreeStaticHtml.makeScripts();
            String str = TreeStaticHtml.makeHeader(title)+ TreeStaticHtml.makeBodyStart(title, 0, 0, 0, 0);
            str += "<div id=\"Events\" class=\"tabcontent\">\n";
            str += "<div id=\"container\">\n";
            fos.write(str.getBytes());
            //str += esoReader.htmlTableTree("eso:",tops, 1, cnt, maxDepth);
            esoReader.simpleTaxonomy.htmlTableTree(fos, "event", "eso:",tops, 1, cnt, cntPredicates);
            str = "</div></div>\n";
            str += TreeStaticHtml.formEnd;
            str += TreeStaticHtml.bodyEnd;
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
/*        str = TreeStaticHtml.makeHeader(title)+ TreeStaticHtml.bodyStart;
        str += "<div id=\"container\">\n";
        str += esoReader.simpleTaxonomy.htmlTableTree("eso:",tops, 1, cnt);
        str += "</div>\n";
        str += TreeStaticHtml.bodyEnd;
        //System.out.println(str);
        //esoReader.printTree(tops, 0, cnt);

        try {
            OutputStream fos = new FileOutputStream(eventPath+".overview.html");
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }


    //"http://www.newsreader-project.eu/domain-ontology#Motion","""33280""^^<http://www.w3.org/2001/XMLSchema#int>"
    static public HashMap<String, Integer> readEventCount (String filePath, String prefix) {
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
                        String[] fields = inputLine.split(",");
                        if (fields.length == 2) {
                            String f1 = fields[0];
                            String f2 = fields[1];
                            int idx = f1.indexOf("#");
                            if (idx>-1) {
                                f1 = f1.substring(idx+1);
                            }
                            idx = f2.indexOf("^");
                            if (idx>-1) {
                                f2 = f2.substring(0, idx);
                            }
                            f1 = prefix+":"+removeQoutes(f1);
                            f2 = removeQoutes(f2);
                           // System.out.println("f1 = " + f1);
                           // System.out.println("f2 = " + f2);
                            map.put(f1, Integer.parseInt(f2));
                        }

                    }
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return map;
    }

    static public HashMap<String, ArrayList<PhraseCount>> readEventCountTypeTsv (String filePath, String prefix) {
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
                String inputLine;
                while (in.ready() && (inputLine = in.readLine()) != null) {
                    // System.out.println(inputLine);
                    inputLine = inputLine.trim();
                    if (inputLine.trim().length() > 0) {
                        String[] fields = inputLine.split("\t");
                        if (fields.length > 2) {
                            String f1 = fields[0];
                            String f2 = fields[1];
                            PhraseCount phraseCount = new PhraseCount(f1,Integer.parseInt(f2));
                            for (int i = 2; i < fields.length; i++) {
                                String field = fields[i];
                                if (field.startsWith(prefix)) {
                                    if (map.containsKey(field)) {
                                        ArrayList<PhraseCount> phrases = map.get(field);
                                        phrases.add(phraseCount);
                                        map.put(field, phrases);
                                    }
                                    else {
                                        ArrayList<PhraseCount> phrases = new ArrayList<PhraseCount>();
                                        phrases.add(phraseCount);
                                        map.put(field, phrases);
                                    }
                                }

                            }
                        }

                    }
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return map;
    }

    //"http://www.newsreader-project.eu/domain-ontology#Motion","""33280""^^<http://www.w3.org/2001/XMLSchema#int>"
    static public HashMap<String, Integer> readEventCountTsv (String filePath, String prefix) {
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
                        if (fields.length >= 2) {
                            String f1 = fields[0];
                            String f2 = fields[1];
                            map.put(f1, Integer.parseInt(f2));
                        }

                    }
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return map;
    }
    static private String removeQoutes(String str) {
        String str2 = "";
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c!='\"') {
                str2 += c;
            }

        }
        return str2;
    }
}
