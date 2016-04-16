package eu.newsreader.eventcoreference.output;

import eu.newsreader.eventcoreference.input.EsoReader;
import eu.newsreader.eventcoreference.util.EsoTreeStaticHtml;
import org.apache.tools.bzip2.CBZip2InputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

/**
 * Created by piek on 15/04/16.
 */
public class DataSetEventHierarchy {

    static public void main (String[] args) {
        String esoPath = "";
        String cntPath = "";
        esoPath = "/Users/piek/Desktop/NWR/eso/ESO.v2/ESO_V2_Final.owl";
        cntPath = "/Users/piek/Desktop/NWR-INC/financialtimes/FT Brexit stats 2/mostFrequentEventESOtypes_results.csv";
        EsoReader esoReader = new EsoReader();
        esoReader.parseFile(esoPath);
        HashMap<String, Integer> cnt = readEventCount(cntPath, "eso");
        ArrayList<String> tops = esoReader.getTops();
        int maxDepth = esoReader.getMaxDepth(tops, 1);
        String str = EsoTreeStaticHtml.header+EsoTreeStaticHtml.bodyStart;
        str += "<div id=\"container\">\n";
        str += esoReader.htmlTableTree("eso:",tops, 1, cnt, maxDepth);
        str += "</div>\n";
        str += EsoTreeStaticHtml.bodyEnd;
        //System.out.println(str);
        //esoReader.printTree(tops, 0, cnt);

        try {
            OutputStream fos = new FileOutputStream(cntPath+".html");
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
