package eu.newsreader.eventcoreference.util;

import eu.kyotoproject.kaf.CorefTarget;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafTerm;
import eu.kyotoproject.kaf.KafWordForm;

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

    static public void getMentionUriArrayList (KafSaxParser kafSaxParser,
                                                             ArrayList<CorefTarget> corefTargetArrayList) {
        for (int i = 0; i < corefTargetArrayList.size(); i++) {
            CorefTarget corefTarget = corefTargetArrayList.get(i);
            getMentionUriCorefTarget(kafSaxParser, corefTarget);
        }
    }

    static public void getMentionUriArrayArrayList (KafSaxParser kafSaxParser,
                                                             ArrayList<ArrayList<CorefTarget>> corefTargetArrayList) {
        for (int i = 0; i < corefTargetArrayList.size(); i++) {
            ArrayList<CorefTarget> corefTargets = corefTargetArrayList.get(i);
            getMentionUriArrayList(kafSaxParser, corefTargets);
        }
    }

    /**
     *      Mention URI = News URI + "#char=START_OFFSET,END_OFFSET"
     * @param kafSaxParser
     * @param corefTarget
     */
    static public void getMentionUriCorefTarget (KafSaxParser kafSaxParser, CorefTarget corefTarget) {
        int firstOffSet = -1;
        int highestOffSet = -1;
        int lengthOffSet = -1;
        String wordTokenString = "";
        KafTerm kafTerm = kafSaxParser.getTerm(corefTarget.getId());
        if (kafTerm==null) {
           // System.out.println("corefTarget = " + corefTarget.getId());
        }
        else {
            for (int i = 0; i < kafTerm.getSpans().size(); i++) {
                String tokenId = kafTerm.getSpans().get(i);
                KafWordForm kafWordForm = kafSaxParser.getWordForm(tokenId);
                if (wordTokenString.isEmpty()) {
                    wordTokenString = kafWordForm.getWid();
                }
                else {
                    wordTokenString +=","+ kafWordForm.getWid();
                }
                if (!kafWordForm.getCharOffset().isEmpty()) {
                    int offSet = Integer.parseInt(kafWordForm.getCharOffset());
                    int length = Integer.parseInt(kafWordForm.getCharLength());
                    if (firstOffSet==-1 || firstOffSet>offSet) {
                        firstOffSet = offSet;
                    }
                    if (highestOffSet==-1 ||offSet>highestOffSet) {
                        highestOffSet = offSet;
                        lengthOffSet = length;
                    }
                }
            }

            if (firstOffSet>-1 && highestOffSet>-1) {
                int end_offset = highestOffSet+lengthOffSet;
               // corefTarget.setId(corefTarget.getId()+"#char="+firstOffSet+","+end_offset);
                corefTarget.setId("char="+firstOffSet+","+end_offset+"&word="+wordTokenString+"&term="+corefTarget.getId());
            }
        }
    }

    /**
     *      Mention URI = News URI + "#char=START_OFFSET,END_OFFSET"
     */
/*    static public String getMentionUri (KafSaxParser kafSaxParser, String targetTerm) {
        String mentionTarget = targetTerm;
        int firstOffSet = -1;
        int highestOffSet = -1;
        int lengthOffSet = -1;
        KafTerm kafTerm = kafSaxParser.getTerm(targetTerm);
        for (int i = 0; i < kafTerm.getSpans().size(); i++) {
            String tokenId = kafTerm.getSpans().get(i);
            KafWordForm kafWordForm = kafSaxParser.getWordForm(tokenId);
            if (!kafWordForm.getCharOffset().isEmpty()) {
                int offSet = Integer.parseInt(kafWordForm.getCharOffset());
                int length = Integer.parseInt(kafWordForm.getCharLength());
                if (firstOffSet==-1 || firstOffSet>offSet) {
                    firstOffSet = offSet;
                }
                if (highestOffSet==-1 ||offSet>highestOffSet) {
                    highestOffSet = offSet;
                    lengthOffSet = length;
                }
            }
        }

        if (firstOffSet>-1 && highestOffSet>-1) {
            int end_offset = highestOffSet+lengthOffSet;
            mentionTarget += "#char="+firstOffSet+","+end_offset;
        }
        return mentionTarget;
    }*/

    static public String cleanUri (String uri) {
        String cleanUri = "";
        int idx = uri.lastIndexOf("/");
        for (int i = 0; i < uri.toCharArray().length; i++) {
            char c = uri.toCharArray()[i];
            if ((i>idx) || idx==-1) {
                if ((c!='.') &&
                    (c!='(') &&
                    (c!=',') &&
                    (c!='\'') &&
                    (c!=')')
                        ){
                   cleanUri+=c;
                }
            }
            else {
                cleanUri +=c;
            }
        }
        cleanUri = cleanUri.replaceAll("%23","");
        return cleanUri;
    }

    static public String getTermIdFromCorefTarget (CorefTarget corefTarget, String ID_SEPARATOR) {

       // http://www.newsreader-project.eu/2004_4_26_4C7M-RB90-01K9-42PW.xml#char=174,182&word=w30&term=t30
        /// ID-HACK
        String id = corefTarget.getId();
        int idx = id.lastIndexOf(ID_SEPARATOR);
        if (idx>-1) {
            id = id.substring(idx+1);
        }
//        /
        // char=174,182&word=w30&term=t30

        ///// ofset HACK
        idx = id.lastIndexOf("=");
        if (idx>-1) {
            id = id.substring(idx+1);
        }
        return id;
    }
    
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
