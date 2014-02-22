package eu.newsreader.eventcoreference.util;

import eu.newsreader.eventcoreference.objects.SourceMeta;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by piek on 2/5/14.
 */
public class ReadSourceMetaFile {
   //http://www.newsreader-project.eu/2003/10/10/49RC-4970-018S-21S2.xml	49RC-4970-018S-21S2	2003-10-10	WHEELS; Pg. D9	The Record (Kitchener-Waterloo, Ontario)	GREG SCHNEIDER		U.S. auto companies get low marks in new survey	Copyright 2003 Metroland Media Group Ltd	3315

    static public HashMap<String, SourceMeta> readSourceFile(String pathToSourceFile) {
        HashMap<String, SourceMeta> data = new HashMap<String, SourceMeta>();
        try {
            FileInputStream fis = new FileInputStream(pathToSourceFile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine;
            while (in.ready()&&(inputLine = in.readLine()) != null) {
                // System.out.println(inputLine);
                inputLine = inputLine.trim();
                if (inputLine.trim().length()>0) {
                    String[] fields = inputLine.split("\t");
                    if (fields.length==10) {
/*                        for (int i = 0; i < fields.length; i++) {
                            String field = fields[i];
                            System.out.println("field "+i+" = " + field);
                        }*/
                        String key = fields[0];
                       // System.out.println("key = " + key);
                        SourceMeta sourceMeta = new SourceMeta(fields);
                       // System.out.println("key = " + key);
                        data.put(key,sourceMeta);
                    }
                    else {
                      //  System.out.println("fields.length = " + fields.length);
                    }
                }
            }
        }
        catch (IOException e) {
            //e.printStackTrace();
        }
        return  data;
    }
}
