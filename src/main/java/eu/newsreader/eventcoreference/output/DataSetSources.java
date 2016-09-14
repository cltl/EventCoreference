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
public class DataSetSources {


    static public void main (String[] args) {
        String citedPath = "";
        String authorPath = "";
        String title = "";
        String querypath = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--cite") && args.length>(i+1)) {
                citedPath = args[i+1];
            }
            else if (arg.equals("--author") && args.length>(i+1)) {
                authorPath = args[i+1];
            }
            else if (arg.equals("--title") && args.length>(i+1)) {
                title = args[i+1];
            }
            else if (arg.equals("--path") && args.length>(i+1)) {
                querypath = args[i+1];
            }
        }
        HashMap<String, ArrayList<PhraseCount>> cntPredicates = new HashMap<String, ArrayList<PhraseCount>>();
        ArrayList<String> tops = new ArrayList<String>();
        tops.add("cite");
        tops.add("author");
        readSourceCountTsv (cntPredicates, citedPath, tops.get(0));
        readSourceCountTsv (cntPredicates, authorPath, tops.get(1));
        HashMap<String, Integer> cnt = cntPhrases(cntPredicates);
        System.out.println("cntPredicates.size() = " + cntPredicates.size());
        System.out.println("Building hierarchy");
        SimpleTaxonomy simpleTaxonomy = new SimpleTaxonomy();

        try {
            OutputStream fos = new FileOutputStream(citedPath+".words.html");
            //String scripts = TreeStaticHtml.makeScripts(cnt.size(), cntPredicates.size());
            String str = TreeStaticHtml.makeHeader(title)+ TreeStaticHtml.makeBodyStart(title, querypath, 0, 0, 0, 0);
            str += "<div id=\"Events\" class=\"tabcontent\">\n";
            str += "<div id=\"container\">\n";
            fos.write(str.getBytes());
            simpleTaxonomy.htmlTableTree(fos, "source", "",tops, 1, cnt, cntPredicates);
            str = "</div></div>\n";
            str += TreeStaticHtml.bodyEnd;
            fos.write(str.getBytes());
            fos.close();
            OutputStream jsonOut = new FileOutputStream(citedPath+".words.json");
            JSONObject tree = new JSONObject();
            simpleTaxonomy.jsonTree(tree, "source", "",tops, 1, cnt, cntPredicates, null);
            //jsonOut.write(tree.toString().getBytes());
            jsonOut.write(tree.toString(0).getBytes());
            jsonOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

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




    static public void readSourceCountTsv (HashMap<String, ArrayList<PhraseCount>> map, String filePath, String type) {
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
                            String f1 = fields[0];
                            String f2 = fields[1];
                            PhraseCount phraseCount = new PhraseCount(f1,Integer.parseInt(f2));
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
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }



}
