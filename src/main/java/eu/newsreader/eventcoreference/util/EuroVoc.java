package eu.newsreader.eventcoreference.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by piek on 04/02/16.
 */
public class EuroVoc {
    static public HashMap<String, String> labelUriMap = new HashMap<String, String>();

    //market gardening	en	http://eurovoc.europa.eu/219401

    static public void readEuroVoc (String filePath, String language) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            InputStreamReader isr = new InputStreamReader(fis);
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
                        }
                    } else {
                        //  System.out.println("fields.length = " + fields.length);
                    }
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
}
