package eu.newsreader.eventcoreference.util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 2/11/14.
 */
public class SplitTechCrunch {



    static public void main (String[] args) {
       String pathToTechCrunchFiles = "";
        String pathToClusterFile = "";

        boolean PERMONTH = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--cluster-file") && args.length>(i+1)) {
                pathToClusterFile = args[i+1];
            }
            else if (arg.equals("--source-files") && args.length>(i+1)) {
                pathToTechCrunchFiles = args[i+1];
            }
            else if (arg.equals("--month")) {
                PERMONTH = true;
            }
        }
       // String pathToTechCrunchFiles = "/Users/piek/Desktop/NWR-DATA/techcrunch/1_3000";
       // String pathToClusterFile = "/Users/piek/Desktop/NWR-DATA/techcrunch/TechCrunchClustersSortedByDate_cleaned.txt";
        File targetFolder = null;
        if (PERMONTH) {
            targetFolder = new File (pathToClusterFile+"_"+"month_clusters");
        }
        else {
            targetFolder = new File (pathToClusterFile+"_"+"day_clusters");
        }
        if (!targetFolder.exists()) {
            targetFolder.mkdir();
        }
        if (targetFolder.exists()) {
            /// create target clusters
            ArrayList<File> folders = Util.makeFolderList(new File (pathToTechCrunchFiles));
            HashMap<String, File> allfileMap = new HashMap<String, File>();
            for (int i = 0; i < folders.size(); i++) {
                File file = folders.get(i);
                addFileList(file, allfileMap);
            }
            HashMap<String, ArrayList<String>> dataMapIn = ReadFileToStringHashMap(pathToClusterFile);
            System.out.println("dataMapIn.size() = " + dataMapIn.size());
            Set keySet = dataMapIn.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();   /// number that identifies a file
             //   System.out.println("key = " + key);
                ArrayList<String> mappings = dataMapIn.get(key);
                if (mappings.size()>=2) {
                    String dateString = mappings.get(0);
                    String topic = mappings.get(1);
                    String clusterName = "";
                    if (PERMONTH) {
                        //2005-07-31
                        int idx = dateString.lastIndexOf("-");
                        if (idx>-1) {
                            clusterName = dateString.substring(0, idx)+"_"+topic;
                        }
                        else {
                            clusterName = dateString+"_"+ topic;
                        }
                    }
                    else {
                        clusterName = dateString+"_"+ topic;
                    }
                    File clusterFolder = new File(targetFolder.getAbsolutePath()+"/"+clusterName);
                    if (!clusterFolder.exists()) {
                        clusterFolder.mkdir();
                    }
                    if (clusterFolder.exists()) {
                        if (allfileMap.containsKey(key)) {
                            File inputFile = allfileMap.get(key);
                            File outputFile = new File(clusterFolder+"/"+inputFile.getName());
                            inputFile.renameTo(outputFile);
                        }
                        else {
                           // System.out.println("Cannot find file for key = " + key);
                        }
                    }
                }
            }
        }
    }

    // 2955.xml_bcfcf332b66b295f7f3f49427932e01a.naf
    static public HashMap<String, File> makeFileList(File inputFile) {
        HashMap<String, File> fileMap = new HashMap<String, File>();
        File[] theFileList = null;
        if ((inputFile.canRead()) && inputFile.isDirectory()) {
            theFileList = inputFile.listFiles();
            for (int i = 0; i < theFileList.length; i++) {
                File file = theFileList[i];
                if (!file.isDirectory()) {
                    int idx = file.getName().indexOf(".");
                    if (idx>-1) {
                        String key = file.getName().substring(0, idx);
                        fileMap.put(key, file);
                    }
                }
            }
        } else {
            System.out.println("Cannot access file:" + inputFile + "#");
            if (!inputFile.exists()) {
                System.out.println("File does not exist!");
            }
        }
        return fileMap;
    }

    static public void addFileList(File inputFile, HashMap<String, File> fileMap) {
        File[] theFileList = null;
        if ((inputFile.canRead()) && inputFile.isDirectory()) {
            theFileList = inputFile.listFiles();
            for (int i = 0; i < theFileList.length; i++) {
                File file = theFileList[i];
                if (!file.isDirectory()) {
                    int idx = file.getName().indexOf(".");
                    if (idx>-1) {
                        String key = file.getName().substring(0, idx);
                        fileMap.put(key, file);
                    }
                }
            }
        } else {
            System.out.println("Cannot access file:" + inputFile + "#");
            if (!inputFile.exists()) {
                System.out.println("File does not exist!");
            }
        }
    }

    /*
    2005-07-01	boingboing	931
2005-07-01	gizmo	932
2005-07-01	gizmoproject	932
2005-07-01	userplane	933
2005-07-02	google	934
2005-07-02	google_earth	934
2005-07-03	dinnerbuzz	936
     */

    /**
     * Store date en topics for a key, first field is the date
     * @param fileName
     * @return
     */
    static public HashMap ReadFileToStringHashMap(String fileName) {
        HashMap<String, ArrayList<String>> lineHashMap = new HashMap<String, ArrayList<String>>();
        if (new File(fileName).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        String [] fields = inputLine.split("\t");
                        if (fields.length==3) {
                            String key = fields[2];
                            String date = fields[0];
                            String topic = fields[1];
                            if (lineHashMap.containsKey(key)) {
                                ///ignoring multiple-topics per source, the first is the most general one
/*                                ArrayList<String> files = lineHashMap.get(key);
                                files.add(topic);
                                lineHashMap.put(key, files);*/
                            }
                            else {
                                ArrayList<String> files = new ArrayList<String>();
                                files.add(date);
                                files.add(topic);
                                lineHashMap.put(key, files);
                            }
                        }
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lineHashMap;
    }
}
