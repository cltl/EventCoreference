package eu.newsreader.eventcoreference.output;

import org.apache.tools.bzip2.CBZip2InputStream;

import java.io.*;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

/**
 * Created by piek on 06/12/2016.
 */
public class GetTypeHierarchyForConcepts {


    static public void main (String[] args) {
        try {
            String typePath = "/Code/vu/newsreader/vua-resources/instance_types_en.ttl.gz";
            String conceptPath = "/Users/piek/Desktop/Vaccins/stats/trigs.non-dark-light.tsv";
            Vector<String> types =  readSimpleTaxonomyFromDbpFile(conceptPath);
            System.out.println("types.size() = " + types.size());
            OutputStream fos = new FileOutputStream(conceptPath+".parents");
            extractParentsFromTypeFile(fos, typePath, types);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static public Vector<String> readSimpleTaxonomyFromDbpFile (String filePath) {
        Vector<String> types = new Vector<String>();
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
                        //group+f+n	1	http://dbpedia.org/resource/2013â€“14_UEFA_Champions_League
                        String[] fields = inputLine.split("\t");
                        if (fields.length == 3) {
                                String type = fields[2].trim();
                            type = "<"+type+">";
                            if (!types.contains(type)) types.add(type);
                        }
                        else {
                           // System.out.println("Skipping line:"+inputLine);
                        }
                    }
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return types;
    }

    static void extractParentsFromTypeFile (OutputStream fos, String filePath, Vector<String> types) {
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
                int cnt = 00;
                while (in.ready() && (inputLine = in.readLine()) != null) {
                    inputLine = inputLine.trim();
                    if (inputLine.trim().length() > 0) {
                       // System.out.println("inputLine = " + inputLine);
                        //<http://dbpedia.org/resource/Larenz_Tate__1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/PersonFunction> .
                        cnt++;
                        if (cnt%1000==0) {
                            System.out.println("cnt = " + cnt);
                        }
                        String[] fields = inputLine.split(" ");
                        if (fields.length == 4) {
                            String key = fields[0].trim();
                            String parent = fields[2].trim();
                            if (parent.indexOf("dbpedia.org")>-1) {
                                if (types.contains(key)) {
                                    if (!types.contains(parent)) {
                                        types.add(parent);
                                    }
                                    key = key.substring(1, key.length()-1);
                                    parent = parent.substring(1, parent.length()-1);
                                    String str = parent + "\t" + key + "\n";
                                    fos.write(str.getBytes());
                                } else {
                                   // System.out.println("not interested in key = " + key);
                                }
                            }
                        }
                        else {
                          //  System.out.println("Skipping line:"+inputLine);
                        }
                    }
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

}
