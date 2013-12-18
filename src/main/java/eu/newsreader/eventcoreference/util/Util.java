package eu.newsreader.eventcoreference.util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/14/13
 * Time: 7:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class Util {

    static public ArrayList<File> makeRecursiveFileList(File inputFile, String theFilter) {
        ArrayList<File> acceptedFileList = new ArrayList<File>();
        File[] theFileList = null;
        if ((inputFile.canRead()) && inputFile.isDirectory()) {
            theFileList = inputFile.listFiles();
            for (int i = 0; i < theFileList.length; i++) {
                File newFile = theFileList[i];
                if (newFile.isDirectory()) {
                    ArrayList<File> nextFileList = makeRecursiveFileList(newFile, theFilter);
                    acceptedFileList.addAll(nextFileList);
                } else {
                    if (newFile.getName().endsWith(theFilter)) {
                        acceptedFileList.add(newFile);
                    }
                }
            }
        } else {
            System.out.println("Cannot access file:" + inputFile + "#");
            if (!inputFile.exists()) {
                System.out.println("File does not exist!");
            }
        }
        return acceptedFileList;
    }

    static public ArrayList<File> makeFolderList(File inputFile) {
        ArrayList<File> folderList = new ArrayList<File>();
        File[] theFileList = null;
        if ((inputFile.canRead()) && inputFile.isDirectory()) {
            theFileList = inputFile.listFiles();
            for (int i = 0; i < theFileList.length; i++) {
                File newFile = theFileList[i];
                if (newFile.isDirectory()) {
                    folderList.add(newFile);
                }
            }
        } else {
            System.out.println("Cannot access file:" + inputFile + "#");
            if (!inputFile.exists()) {
                System.out.println("File does not exist!");
            }
        }
        return folderList;
    }


    static public void getStats(String fileName) {
        /*
        2013-04-30/sem.trig:            a                dbp:India , dbp:Jeep_Grand_Cherokee , dbp:Chrysler , sem:Place ,
        2013-04-30/sem.trig:            a                sem:Place , <nwr:location> , <pb:locate.01> , dbp:New_Jersey ;
         */
        HashMap<String, Integer> anyMap = new HashMap<String, Integer>();
        HashMap<String, Integer> placeMap = new HashMap<String, Integer>();
        HashMap<String, Integer> actorMap = new HashMap<String, Integer>();
        //HashMap<String, ArrayList<String>> timeMap = new HashMap<String, ArrayList<String>>();

        if (new File(fileName).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        int idx_s = inputLine.indexOf("@prefix");
                        if (idx_s==-1) {
                            String typeString = inputLine.substring(49).trim();
                            String [] fields = typeString.split(",");
                            boolean p = false;
                            boolean a = false;
                            for (int i = 0; i < fields.length; i++) {
                                String field = fields[i].trim();
                                if (field.startsWith("sem:Place")) {
                                    p = true;
                                }
                                else if (field.startsWith("sem:Actor")) {
                                    a = true;
                                }
                            }
                            for (int i = 0; i < fields.length; i++) {
                                String field = fields[i].trim();
                                if (field.endsWith(";")) {
                                    field = field.substring(0, field.length()-1).trim();
                                   // System.out.println("field = " + field);
                                }
                                if (field.startsWith("dbp:")) {
                                    if (anyMap.containsKey(field)) {
                                        Integer cnt = anyMap.get(field);
                                        cnt++;
                                        anyMap.put(field, cnt);
                                    }
                                    else {
                                        anyMap.put(field, 1);
                                    }
                                    if (a) {
                                        if (actorMap.containsKey(field)) {
                                            Integer cnt = actorMap.get(field);
                                            cnt++;
                                            actorMap.put(field, cnt);
                                        }
                                        else {
                                            actorMap.put(field, 1);
                                        }
                                    }
                                    if (p) {
                                        if (placeMap.containsKey(field)) {
                                            Integer cnt = placeMap.get(field);
                                            cnt++;
                                            placeMap.put(field, cnt);
                                        }
                                        else {
                                            placeMap.put(field, 1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                FileOutputStream fos = new FileOutputStream(fileName+".xls");
                String str = "\tDBP\tsem:Actor\tsem:Place\n";
                str += "ALL ENTITIES\t"+anyMap.size()+"\t"+actorMap.size()+"\t"+placeMap.size()+"\n";
                fos.write(str.getBytes());
                Set keySet = anyMap.keySet();
                Iterator keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    Integer cnt = anyMap.get(key);
                    Integer aCnt = 0;
                    Integer pCnt = 0;
                    if (actorMap.containsKey(key)) {
                       aCnt = actorMap.get(key);
                    }
                    if (placeMap.containsKey(key)) {
                       pCnt = placeMap.get(key);
                    }
                    str = key+"\t"+cnt+"\t"+aCnt+"\t"+pCnt+"\n";
                    fos.write(str.getBytes());
                }
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static public void main (String [] args) {
        //String file = args[0];
        String file = "/Users/kyoto/Desktop/NWR-DATA/trig/dbp.lst";
        getStats(file);
    }

}
