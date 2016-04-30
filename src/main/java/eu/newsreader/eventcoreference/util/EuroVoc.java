package eu.newsreader.eventcoreference.util;

import org.apache.tools.bzip2.CBZip2InputStream;

import java.io.*;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

/**
 * Created by piek on 04/02/16.
 */
public class EuroVoc {
     public HashMap<String, String> labelUriMap = new HashMap<String, String>();
     public HashMap<String, String> uriLabelMap = new HashMap<String, String>();

    //market gardening	en	http://eurovoc.europa.eu/219401

    public EuroVoc () {
        labelUriMap = new HashMap<String, String>();
        uriLabelMap = new HashMap<String, String>();

    }
    public void readEuroVoc (String filePath, String language) {
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
                        if (fields.length == 3) {
                            String key = fields[0];
                            String lg = fields[1];
                            String uri = fields[2];
                            if (lg.equalsIgnoreCase(language)) {
                                labelUriMap.put(key, uri);
                                uriLabelMap.put(uri, key);
                            }
                        } else {
                            //  System.out.println("fields.length = " + fields.length);
                        }
                    }
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
}
