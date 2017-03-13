package eu.newsreader.eventcoreference.util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 24/11/2016.
 */
public class TopicStatsFromTrig {
    static public void main (String[] args) {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        String folderpath = "";
        folderpath = "/Users/piek/Desktop/NWR-INC/dasym/dasym_trig";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--trig-folder") && args.length>(i+1)) {
                folderpath = args[i+1];
            }
        }
        File inputFolder = new File(folderpath);
        System.out.println("inputFolder = " + inputFolder);
        ArrayList<File> files = Util.makeRecursiveFileList(inputFolder, ".trig");
        System.out.println(".trig files size() = " + files.size());
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            if (i % 500 == 0) {
                System.out.println("i = " + i);
            }
            try {
                InputStreamReader isr = null;
                FileInputStream fis = new FileInputStream(file);
                isr = new InputStreamReader(fis);
                if (isr!=null) {
                    BufferedReader in = new BufferedReader(isr);
                    String inputLine;
                    while (in.ready() && (inputLine = in.readLine()) != null) {
                        //skos:relatedMatch  eurovoc:216005 , eurovoc:212927 , eurovoc:213012 , eurovoc:213025 , eurovoc:215838 , eurovoc:212632 .
                        inputLine = inputLine.trim();
                        int idx = inputLine.indexOf("eurovoc:");
                        if (idx > -1) {
                            String fieldString = inputLine.substring(idx);
                            String[] fields = fieldString.split(",");
                            for (int j = 0; j < fields.length; j++) {
                                String field = fields[j];
                                if (field.endsWith(".")) {
                                    field = field.substring(0, field.length()-1);
                                }
                                field = field.trim();
                                if (map.containsKey(field)) {
                                    Integer cnt = map.get(field);
                                    cnt++;
                                    map.put(field, cnt);
                                }
                                else {
                                    map.put(field, 1);
                                }
                            }
                        }
                    }
                }
                fis.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
        File folderParent = inputFolder.getParentFile();
        String outputFile = folderParent.getAbsolutePath() + "/" + inputFolder.getName() + ".topics.xls";

        try {
            OutputStream fos = new FileOutputStream(outputFile);
            Set keySet = map.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                Integer cnt = map.get(key);
                String str = key+"\t"+cnt+"\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
