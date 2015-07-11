package eu.newsreader.eventcoreference.evaluation;

import eu.kyotoproject.kaf.CorefTarget;
import eu.kyotoproject.kaf.KafCoreferenceSet;
import eu.kyotoproject.kaf.KafWordForm;
import eu.newsreader.eventcoreference.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 12/13/14.
 */
public class CoNLL {

     static public class CorefStatistics {
         private int nCorefSets;
         private int nMentions;
         private int nLemmas;
         private int singletons;
         private int multitons;

         public CorefStatistics() {
             this.nCorefSets = 0;
             this.multitons = 0;
             this.nLemmas = 0;
             this.nMentions = 0;
             this.singletons = 0;
         }

         public int getnCorefSets() {
             return nCorefSets;
         }

         public void addCorefSets(int n) {
             this.nCorefSets+=n;
         }


         public int getMultitons() {
             return multitons;
         }

         public void addMultitons(int n) {
             this.multitons+=n;
         }

         public double getAvergageLemmas() {
             return (double) nLemmas/multitons;
         }

         public void addLemmas(int n) {
             this.nLemmas+=n;
         }

         public double getAverageMentions() {
             return (double) nMentions/multitons;
         }

         public void addMentions(int n) {
             this.nMentions+=n;
         }

         public int getSingletons() {
             return singletons;
         }

         public void addSingletons(int n) {
             this.singletons+=n;
         }

         public String toString() {
             String str = "# coref sets\t"+this.getnCorefSets()+"\n";
             str += "# singletons\t"+this.getSingletons()+"\n";
             str += "# multitons\t"+this.getMultitons()+"\n";
             str += "Average mentions\t"+this.getAverageMentions()+"\n";
             str += "Average lemmas\t"+this.getAvergageLemmas()+"\n";
             return str;
         }
     }

    static public void main (String[] args) {
        String pathToKeyFolder = "";
        String pathToResponseFolder = "";
        pathToKeyFolder = "/Users/piek/Desktop/NWR/NWR-benchmark/coreference/corpus_CONLL-wn-sim-2.5-wsd8-ims-ukb-top/corpus_stock_market/events/key/";
        pathToResponseFolder = "/Users/piek/Desktop/NWR/NWR-benchmark/coreference/corpus_CONLL-wn-sim-2.5-wsd8-ims-ukb-top/corpus_stock_market/events/response/";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--key-folder") && args.length>(i+1)) {
                pathToKeyFolder = args[i+1];
            }
            else if (arg.equals("--response-folder") && args.length>(i+1)) {
                pathToResponseFolder = args[i+1];
            }
        }

        File inputKeyFolder = new File(pathToKeyFolder);
        File inputResponseFolder = new File(pathToResponseFolder);
        CorefStatistics corefKeyStatistics = new CorefStatistics();
        CorefStatistics corefResponseStatistics = new CorefStatistics();
        try {
            OutputStream fos = new FileOutputStream(inputKeyFolder.getParentFile()+"/"+"corefOverview.csv");
            ArrayList<File> keyFiles = Util.makeRecursiveFileList(inputKeyFolder);
            for (int i = 0; i < keyFiles.size(); i++) {
                File file = keyFiles.get(i);
                readCorefSetFromCoNLL(file, fos, corefKeyStatistics, "key");
            }
            ArrayList<File> responseFiles = Util.makeRecursiveFileList(inputResponseFolder);
            for (int i = 0; i < responseFiles.size(); i++) {
                File file = responseFiles.get(i);
                readCorefSetFromCoNLL(file, fos, corefResponseStatistics, "response");
            }
            String str = "KEY:\n"+corefKeyStatistics.toString();
            fos.write(str.getBytes());
            str = "RESPONSE:\n"+corefResponseStatistics.toString();
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void readCorefSetFromCoNLL (File file, OutputStream fos, CorefStatistics corefStatistics, String result){
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            String fileName = file.getName();
            int idx = fileName.indexOf(".");
            if (idx>-1) {
                fileName = fileName.substring(0, idx);
            }
            HashMap<String, ArrayList<String>> labelSet = new HashMap<String, ArrayList<String>>();
            HashMap<String, ArrayList<String>> tokenIdSet = new HashMap<String, ArrayList<String>>();
            if (in.ready()&&(inputLine = in.readLine()) != null) {
                ///skip first line
            }
            while (in.ready()&&(inputLine = in.readLine()) != null) {
                if (inputLine.trim().length()>0) {
                    String [] fields = inputLine.split("\t");
                    if (fields.length==5) {
                        String token = fields[3];
                        String tokenId = fields[2];
                        String corefId = fields[4];
                        if (corefId.startsWith("(")) {
                            corefId= corefId.substring(1);
                        }
                        if (corefId.endsWith(")")) {
                            corefId = corefId.substring(0, corefId.length()-1);
                        }
                        if (!corefId.equals("-")) {
                            if (labelSet.containsKey(corefId)) {
                                ArrayList<String> mentions = labelSet.get(corefId);
                                mentions.add(token);
                                labelSet.put(corefId, mentions);
                            }
                            else {
                                ArrayList<String> mentions = new ArrayList<String>();
                                mentions.add(token);
                                labelSet.put(corefId, mentions);
                            }
                            if (tokenIdSet.containsKey(corefId)) {
                                ArrayList<String> tokenIds = tokenIdSet.get(corefId);
                                tokenIds.add(tokenId);
                                tokenIdSet.put(corefId, tokenIds);
                            }
                            else {
                                ArrayList<String> tokenIds = new ArrayList<String>();
                                tokenIds.add(tokenId);
                                tokenIdSet.put(corefId, tokenIds);
                            }
                        }
                    }
                }
            }
            in.close();
            Set keySet = labelSet.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                corefStatistics.addCorefSets(1);
                ArrayList<String> tokens = labelSet.get(key);
                ArrayList<String> tokenIds = tokenIdSet.get(key);
                if (tokens.size()==1) {
                    corefStatistics.addSingletons(1);
                }
                else {
                    corefStatistics.addMultitons(1);
                    corefStatistics.addMentions(tokens.size());
                    ArrayList<String> lemmas = new ArrayList<String>();
                    for (int i = 0; i < tokens.size(); i++) {
                        String token = tokens.get(i);
                        if (!lemmas.contains(token)) lemmas.add(token);
                    }
                    corefStatistics.addLemmas(lemmas.size());
                }

                String str = fileName+"\t"+key+"\t"+result;
                for (int i = 0; i < tokens.size(); i++) {
                    String token = tokens.get(i);
                    String tokenId = tokenIds.get(i);
                    str += "\t"+tokenId+"\t"+token;
                }
                str += "\n";
                fos.write(str.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * #begin document (LuoTestCase);
     test1	0	0	a1	(0
     test1	0	1	a2	0)
     test1	0	2	junk	-
     test1	0	3	b1	(1
     test1	0	4	b2	-
     test1	0	5	b3	-
     test1	0	6	b4	1)
     test1	0	7	jnk	-
     test1	0	8	.	-

     test2	0	0	c	(1)
     test2	0	1	jnk	-
     test2	0	2	d1	(2
     test2	0	3	d2	2)
     test2	0	4	jnk	-
     test2	0	5	e	(2)
     test2	0	6	jnk	-
     test2	0	7	f1	(2
     test2	0	8	f2	-
     test2	0	9	f3	2)
     test2	0	10	.	-
     #end document
     */

    /**
     *
     * @param stream
     * @param fileName
     * @param type
     */
    static public void serializeToCoNLL (OutputStream stream,  String fileName, String type,
                                         ArrayList<KafWordForm> kafWordFormArrayList,
                                         ArrayList<KafCoreferenceSet> kafCoreferenceSetArrayList) {
        try {
            String str = "#begin document ("+fileName+");";
            stream.write(str.getBytes());
            str  = "";
            boolean COREFERRING = false;
            boolean NEWSENTENCE = false;
            String currentSentence = "";
            String currentReference = "";

            for (int i = 0; i < kafWordFormArrayList.size(); i++) {
                KafWordForm kafWordForm = kafWordFormArrayList.get(i);
                /// insert sentence separator
                if (!currentSentence.isEmpty() && !currentSentence.equals(kafWordForm.getSent()))  {
                    NEWSENTENCE = true;
                    currentSentence = kafWordForm.getSent();
                }
                else if (currentSentence.isEmpty()) {
                    /// first sentence
                    currentSentence = kafWordForm.getSent();
                }
                else {
                    NEWSENTENCE = false;
                }
                String corefId = getCoreferenceSetId(kafCoreferenceSetArrayList,kafWordForm.getWid(), type);
                //System.out.println(kafWordForm.getWid()+":" + corefId);
                //// First we need to handle the previous line if any
                //// After that we can process the current
                /// check previous conditions and terminate properly

                if (corefId.isEmpty()) {
                    //// current is not a coreferring token
                    if (COREFERRING) {
                        //// previous was coreferring so we need to terminate the previous with ")"
                        str += ")";
                    }
                    /// always terminate the previous token
                    str += "\n";
                    COREFERRING = false;
                    /// we started a new sentence so we insert a blank line
                    if (NEWSENTENCE) str+= "\n";
                    /// add the info for the current token
                    String tokenId = kafWordForm.getWid();
                    if (tokenId.startsWith("w")) {
                        tokenId = tokenId.substring(1);
                    }
                    str += fileName+"\t"+kafWordForm.getSent()+"\t"+tokenId+"\t"+kafWordForm.getWf() +"\t"+"-";
                }
                else {
                    if (NEWSENTENCE) {
                        /// we started a new sentence so we insert a blank line
                        if (COREFERRING) {
                            /// end of sentence implies ending coreference as well
                            str += ")\n";
                        }
                        else {
                            str += "\n";
                        }
                        str+= "\n";
                    }
                    else {
                        if (COREFERRING && !currentReference.equals(corefId))  {
                            str += ")\n";
                        }
                        else {
                            str += "\n";
                        }
                    }
                    /// add the info for the current token
                    String tokenId = kafWordForm.getWid();
                    if (tokenId.startsWith("w")) {
                        tokenId = tokenId.substring(1);
                    }
                    str += fileName+"\t"+kafWordForm.getSent()+"\t"+tokenId+"\t"+kafWordForm.getWf() +"\t";
                    if (!COREFERRING) {
                        str += "("+corefId;
                        COREFERRING = true;
                    }
                    else {
                        if (!currentReference.equals(corefId)) {
                            str += "("+corefId;
                        }
                        else {
                            str += corefId;
                        }
                    }
                    currentReference = corefId;
                }
            }
            ///check the status of the last token
            if (COREFERRING) {
                str += ")\n";
            }
            else {
                str += "\n";
            }
            stream.write(str.getBytes());
            str = "#end document\n";
            stream.write(str.getBytes());

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    static public String getCoreferenceSetId (ArrayList<KafCoreferenceSet> kafCoreferenceSetArrayList, String tokenId, String type) {
        String corefId = "";
        for (int i = 0; i < kafCoreferenceSetArrayList.size(); i++) {
            KafCoreferenceSet corefSet  = kafCoreferenceSetArrayList.get(i);
            // System.out.println("coref.getType() = " + corefSet.getType());
            if (type.isEmpty() || corefSet.getType().toLowerCase().startsWith(type.toLowerCase())) {
                for (int j = 0; j < corefSet.getSetsOfSpans().size(); j++) {
                    ArrayList<CorefTarget> corefTargets = corefSet.getSetsOfSpans().get(j);
                    for (int k = 0; k < corefTargets.size(); k++) {
                        CorefTarget corefTarget = corefTargets.get(k);
                        if (corefTarget.getId().equals(tokenId)) {
                            corefId = corefSet.getCoid();
                            //System.out.println("corefTarget.getId() = " + corefTarget.getId());
                            //System.out.println("tokenId = " + tokenId);
                            break;
                        }
                    }
                    if (!corefId.isEmpty()) {
                        break;
                    }
                }
            }
            else {
                //  if (!corefSet.getType().isEmpty()) System.out.println("coref.getType() = " + corefSet.getType());
            }
            if (!corefId.isEmpty()) {
                break;
            }
        }
       // if (!corefId.isEmpty()) System.out.println("corefId = " + corefId);
        return corefId;
    }
}
