package eu.newsreader.eventcoreference.util;

import eu.kyotoproject.kaf.CorefTarget;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafTerm;
import eu.kyotoproject.kaf.KafWordForm;

import java.io.File;
import java.util.ArrayList;

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


}
