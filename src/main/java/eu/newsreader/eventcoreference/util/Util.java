package eu.newsreader.eventcoreference.util;

import eu.kyotoproject.kaf.CorefTarget;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafTerm;
import eu.kyotoproject.kaf.KafWordForm;
import eu.newsreader.eventcoreference.objects.NafMention;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/14/13
 * Time: 7:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class Util {

    static public ArrayList<NafMention> getNafMentionArrayList (String baseUri, KafSaxParser kafSaxParser,
                                                        ArrayList<ArrayList<CorefTarget>> corefTargetArrayList) {
        ArrayList<NafMention> mentionURIs = new ArrayList<NafMention>();
        for (int i = 0; i < corefTargetArrayList.size(); i++) {
            ArrayList<CorefTarget> corefTargets = corefTargetArrayList.get(i);
            NafMention mention = getNafMentionForCorefTargets(baseUri, kafSaxParser, corefTargets);
            mentionURIs.add(mention);
        }
        return mentionURIs;
    }


    /**
     *      Mention URI = News URI + "#char=START_OFFSET,END_OFFSET"
     * @param kafSaxParser
     * @param corefTargets
     */
    static public NafMention getNafMentionForCorefTargets (String baseUri, KafSaxParser kafSaxParser, ArrayList<CorefTarget> corefTargets) {
        NafMention mention = new NafMention();
        int firstOffSet = -1;
        int highestOffSet = -1;
        int lengthOffSet = -1;
        mention.setBaseUri(baseUri);
        for (int j = 0; j < corefTargets.size(); j++) {
            CorefTarget corefTarget = corefTargets.get(j);
            KafTerm kafTerm = kafSaxParser.getTerm(corefTarget.getId());
            mention.addTermsId(corefTarget.getId());
            if (kafTerm==null) {
                // System.out.println("corefTarget = " + corefTarget.getId());
            }
            else {
                for (int i = 0; i < kafTerm.getSpans().size(); i++) {
                    String tokenId = kafTerm.getSpans().get(i);
                    KafWordForm kafWordForm = kafSaxParser.getWordForm(tokenId);
                    mention.addTokensId(kafWordForm.getWid());
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
            }
        }
        if (firstOffSet>-1 && highestOffSet>-1) {
            int end_offset = highestOffSet+lengthOffSet;
            mention.setOffSetStart(new Integer (firstOffSet).toString());
            mention.setOffSetEnd(new Integer(end_offset).toString());
        }
        return mention;
    }


    static public NafMention getNafMentionForTermId (String baseUri, KafSaxParser kafSaxParser, String termId) {
        NafMention mention = new NafMention();
        mention.setBaseUri(baseUri);
        int firstOffSet = -1;
        int highestOffSet = -1;
        int lengthOffSet = -1;
        mention.setBaseUri(baseUri);
        mention.addTermsId(termId);
        KafTerm kafTerm = kafSaxParser.getTerm(termId);

        if (kafTerm==null) {
            // System.out.println("corefTarget = " + corefTarget.getId());
        }
        else {
            for (int i = 0; i < kafTerm.getSpans().size(); i++) {
                String tokenId = kafTerm.getSpans().get(i);
                KafWordForm kafWordForm = kafSaxParser.getWordForm(tokenId);
                mention.addTokensId(kafWordForm.getWid());
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
        }
        if (firstOffSet>-1 && highestOffSet>-1) {
            int end_offset = highestOffSet+lengthOffSet;
            mention.setOffSetStart(new Integer (firstOffSet).toString());
            mention.setOffSetEnd(new Integer(end_offset).toString());
        }
        return mention;
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

    static public String cleanDbpediaUri(String uri, String ns) {
        String cleanUri = ns;
        // <http://dbpedia.org/resource/MG_F_/_MG_TF>
        if (uri.startsWith(ns)) {
            for (int i = ns.length(); i < uri.toCharArray().length; i++) {
                char c = uri.toCharArray()[i];
                if ((c!='.') &&
                    (c!='&') &&
                    (c!='*') &&
                    (c!=':') &&
                    (c!='!') &&
                    (c!='!') &&
                    (c!='+') &&
                    (c!='-') &&
                    (c!='–') &&
                    (c!='–') &&
                    (c!='(') &&
                    (c!='/') &&
                    (c!='!') &&
                    (c!=',') &&
                    (c!='\'') &&
                    (c!=')')
                        ) {
                   cleanUri+=c;
                }
                else {
                     if (c=='-') { cleanUri += "_"; }
                     if (c=='–') { cleanUri += "_"; }
                }
            }
            cleanUri = cleanUri.replaceAll("%23","");
            cleanUri = cleanUri.replaceAll("%3F","");
            cleanUri = cleanUri.replaceAll("%7C","");
            cleanUri = cleanUri.replaceAll("%22","");
        }
        else {
            System.out.println("uri = " + uri);
        }
        return cleanUri;
    }

/*
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
*/

    static public String getTermIdFromMention (String mention, String ID_SEPARATOR) {

       // http://www.newsreader-project.eu/2004_4_26_4C7M-RB90-01K9-42PW.xml#char=174,182&word=w30&term=t30
        /// ID-HACK
        String id = mention;
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

    static public ArrayList<File> makeFlatFileList(File inputFile, String theFilter) {
        ArrayList<File> acceptedFileList = new ArrayList<File>();
        File[] theFileList = null;
        if ((inputFile.canRead()) && inputFile.isDirectory()) {
            theFileList = inputFile.listFiles();
            for (int i = 0; i < theFileList.length; i++) {
                File newFile = theFileList[i];
                if (!newFile.isDirectory()) {
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

    static String getMentionReferenceFromCorefs(ArrayList<CorefTarget> corefs) {
        String mentionReference = "";
        String offset = "";
        String token = "";
        String term = "";
        for (int i = 0; i < corefs.size(); i++) {
            CorefTarget corefTarget = corefs.get(i);
            String [] fields = corefTarget.getId().split("&");
            if (fields.length==3) {
                String f1 = fields[0];
                String f2 = fields[1];
                String f3 = fields[2];

            }

        }
        return mentionReference;
    }

    static public HashMap<String, ArrayList<File>> makeFolderGroupList(File inputFile, int length, String filter) {
        HashMap<String, ArrayList<File>> folderList = new HashMap<String, ArrayList<File>>();
        File[] theFileList = null;
        if ((inputFile.canRead()) && inputFile.isDirectory()) {
            theFileList = inputFile.listFiles();
            for (int i = 0; i < theFileList.length; i++) {
                File newFile = theFileList[i];
                if (newFile.isDirectory()) {
                    ArrayList<File> files = makeRecursiveFileList(newFile, filter);
                    String subs = newFile.getName();
                    if (subs.length()>length) {
                        subs = subs.substring(0, length);
                    }
                    if (folderList.containsKey(subs)) {
                        ArrayList<File> storedFiles = folderList.get(subs);
                        storedFiles.addAll(files);
                        folderList.put(subs, storedFiles);
                    }
                    else {
                        folderList.put(subs, files);
                    }
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
                        int idx_s = inputLine.indexOf("\t");
                        if (idx_s>-1) {
                            String key = inputLine.substring(0, idx_s).trim();
                            String value = inputLine.substring(idx_s+1).trim();
                            if (lineHashMap.containsKey(key)) {
                                ArrayList<String> files = lineHashMap.get(key);
                                files.add(value);
                                lineHashMap.put(key, files);
                            }
                            else {
                                ArrayList<String> files = new ArrayList<String>();
                                files.add(value);
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


    static public int copyFile(File inputFile, File outputFile) {
        if (!inputFile.exists()) {
            return -1;
        }
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(inputFile));
            byte[] buffer = new byte[(int) inputFile.length()];
            in.readFully(buffer);
            in.close();
            DataOutputStream out = new DataOutputStream(new FileOutputStream(outputFile));
            out.write(buffer);
        } catch (IOException e) {
            return -3;
        }
        return 0;
    }

}
