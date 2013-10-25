package eu.newsreader.eventcoreference.coref;

import eu.newsreader.eventcoreference.input.CorefSaxParser;
import eu.newsreader.eventcoreference.objects.CoRefSet;
import eu.newsreader.eventcoreference.objects.CorefTarget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 2/17/13
 * Time: 6:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChainCorefSets {

    static public void main (String[] args) {
        //String pathToFolder = args[0];
        //String extension = args[1];
        String pathToFolder = "/Users/kyoto/Desktop/Events/ECB/ECBcorpus_StanfordAnnotation/EECB1.0/results-1/merged-all-gran";
        String extension = ".corefs.xml";
        File folder = new File(pathToFolder);
        if (folder.isDirectory()) {
            ArrayList<String> files = makeFileList(folder, extension);
            for (int i = 0; i < files.size(); i++) {
                File file = new File(files.get(i));
                if (!file.isDirectory()) {
                    processCorefFile(file.getAbsolutePath());
                }
            }
        }
    }

    static void processCorefFile (String pathToCorefSet) {
        try {
            String pathToChainedCorefeSet = pathToCorefSet+".chained.xml";
            CorefSaxParser corefSaxParser = new CorefSaxParser();
            corefSaxParser.parseFile(pathToCorefSet);
            String str = "";
            FileOutputStream coreferenceOutputStream = new FileOutputStream(pathToChainedCorefeSet);
            str ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
            str += "<COREF method=\""+corefSaxParser.method+"\" threshold=\""+corefSaxParser.threshold+"\">"+"\n";
            coreferenceOutputStream.write(str.getBytes());
            Set keySet = corefSaxParser.corefMap.keySet();
            Iterator keys = keySet.iterator();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                ArrayList<CoRefSet> coRefSets = corefSaxParser.corefMap.get(key);
                ArrayList<CoRefSet> condensedSets = ChainCorefSets.chainSets(coRefSets);
                while (condensedSets.size()<coRefSets.size()) {
                    coRefSets = condensedSets;
                    condensedSets = ChainCorefSets.chainSets(coRefSets);
                }
                str = "<co-ref-sets file=\""+key+"\">\n";
                coreferenceOutputStream.write(str.getBytes());

                //  System.out.println("condensedSets.size() = " + condensedSets.size());
                for (int j = 0; j < condensedSets.size(); j++) {
                    CoRefSet coRefSet = condensedSets.get(j);
                    coRefSet.setScore(coRefSet.getAverageCoref()); /// recalculate the score for the set as a whole
                    str = coRefSet.toString();
                    coreferenceOutputStream.write(str.getBytes());
                }
                str = "</co-ref-sets>\n";
                coreferenceOutputStream.write(str.getBytes());
            }
            str = "</COREF>\n";
            coreferenceOutputStream.write(str.getBytes());
            coreferenceOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    static public ArrayList<String> makeFileList(File folder, String extension) {
        ArrayList<String> acceptedFileList = new ArrayList<String>();
        File[] theFileList = null;
        if ((folder.canRead()) && folder.isDirectory()) {
            theFileList = folder.listFiles();
            for (int i = 0; i < theFileList.length; i++) {
                String newFilePath = theFileList[i].getAbsolutePath();
                if (!theFileList[i].isDirectory()) {
                    if (newFilePath.endsWith(extension)) {
                        acceptedFileList.add(newFilePath);
                    }
                }
            }
        }
        return acceptedFileList;
    }

    static public ArrayList<CoRefSet> chainSets(ArrayList<CoRefSet> coRefSets) {
        ArrayList<CoRefSet> condensedSets = new ArrayList<CoRefSet>();
        for (int j = 0; j < coRefSets.size(); j++) {
            CoRefSet coRefSet1 = coRefSets.get(j);
            if (coRefSet1.getTargets().size()>0) {
                for (int k = j+1; k < coRefSets.size(); k++) {
                        CoRefSet coRefSet2 = coRefSets.get(k);
                        int overlap = coRefSet1.sizeOverlap((coRefSet2));
                        if (overlap>0) {
                           // System.out.println("coRefSet1 = " + coRefSet1);
                           // System.out.println("coRefSet2 = " + coRefSet2);
                            //// There is an overlap in targets between the coref sets
                            //// Now coRefSet1 will swallow coRefSet2, coRefSet2 will be destroyed
                            for (int l = 0; l < coRefSet2.getTargets().size(); l++) {
                                CorefTarget corefTarget2 = coRefSet2.getTargets().get(l);
                                CorefTarget corefTarget1 = coRefSet1.getTargetForTermId(corefTarget2.getTermId());
                                if (corefTarget1==null) {
                                    ///// chaining effect of overlap: coRefSet1 does not have coreftarget2 so we add it
                                    coRefSet1.addTarget(corefTarget2);
                                }
                                else {
                                    //// Here we have overlapping targets, we only need one and therefore copy the highest score.
                                    //// we take the highest score for overlapping sets
                                    if (corefTarget2.getCorefScore()>corefTarget1.getCorefScore()) {
                                        corefTarget1.setCorefScore(corefTarget2.getCorefScore());
                                    }
                                }
                            }
                            //// since the targets of coRefSet2 are absorbed by coRefSet1, we can destroy coRefSet2
                            coRefSet2.setTargets(new ArrayList<CorefTarget>()); // we empty the targets
                            /// we take the highest score for the whole set
                            if (coRefSet2.getScore()>coRefSet1.getScore()) {
                                coRefSet1.setScore(coRefSet2.getScore());
                                coRefSet1.setLcs(coRefSet2.getLcs());
                            }
                        }
                        else {

                            ///// there is no overlap in targets and we continue to the next corefset
                        }
                }
                //// At the end of te process coRefSet1 may have changed (either the same or bigger by swallowing
                //// We now added to the condensed set
                condensedSets.add(coRefSet1);
            }
            else {
                //// was emptied before......
                //  System.out.println("coRefSet1.getId() = " + coRefSet1.getId());
            }
        }
        return condensedSets;
    }
}
