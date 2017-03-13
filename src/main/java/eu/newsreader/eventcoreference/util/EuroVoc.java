package eu.newsreader.eventcoreference.util;

import org.apache.tools.bzip2.CBZip2InputStream;

import java.io.*;
import java.util.HashMap;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

/**
 * Created by piek on 04/02/16.
 */
public class EuroVoc {
     public HashMap<String, String> labelUriMap = new HashMap<String, String>();
     public HashMap<String, String> uriLabelMap = new HashMap<String, String>();
     public Vector<String> substrings = new Vector<String>();

    //market gardening	en	http://eurovoc.europa.eu/219401

    public EuroVoc () {
        labelUriMap = new HashMap<String, String>();
        uriLabelMap = new HashMap<String, String>();
        substrings = new Vector<String>();
    }

    public boolean startsWith (String topic) {
        if (substrings.size()==0) return false;
        else {
            for (int i = 0; i < substrings.size(); i++) {
                String s = substrings.get(i);
                if (topic.startsWith(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void readEuroVoc (String filePath, String language) {
      //  System.out.println("filePath = " + filePath);
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
                    //parliamentary control	en	http://eurovoc.europa.eu/209346
                    inputLine = inputLine.trim();
                  //  System.out.println("inputLine = " + inputLine);
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
                            if (!substrings.contains(inputLine.trim())) {
                                substrings.add(inputLine.trim());
                            }
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
