package eu.newsreader.eventcoreference.referencenet;

//import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.newsreader.eventcoreference.objects.PhraseCount;
import vu.wntools.wnsimilarity.measures.SimilarityPair;
import vu.wntools.wordnet.WordnetLmfSaxParser;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by piek on 20/10/2017.
 */
public class ReferenceAnnotation {
    static WordnetLmfSaxParser wordnetLmfSaxParser;
  //  static StanfordCoreNLP pipeline;
    static DecimalFormat df = new DecimalFormat("#.00");
    static HashMap<String, ArrayList<PhraseCount>> typeLex = new HashMap<String, ArrayList<PhraseCount>>();
    static ArrayList<PhraseCount> dataLex = new ArrayList<PhraseCount>();

    static public void main (String [] args) {
        String filePath = "/Users/piek/Desktop/SemEval2018/Annotation/referencesets-sorted.txt";
        //String wnPath = "/Code/vu/newsreader/vua-resources/wneng-30.lmf.xml.xpos.gz";
        String wnPath = "/Code/vu/newsreader/vua-resources/wneng-30.lmf.xml.xpos.gz";
        wordnetLmfSaxParser = new WordnetLmfSaxParser();
        wordnetLmfSaxParser.parseFile(wnPath);
        //System.out.println("wordnetLmfSaxParser = " + wordnetLmfSaxParser.wordnetData.getHyperRelations().size());
       // Util.initStandfordCoreNLP(pipeline);
        File file = new File(filePath);
        readData(file);
    }

    /**
     * For each word we make a comparison to all other words using WN SIM
     * This generates a Sim vector for the length of the array
     * For each word, we calculate a centrality score by taking the dot product of the vector
     * @param phraseCountArrayList
     */
    static ArrayList<Double> getDistanceMatrix (SortedSet<PhraseCount> phraseCountArrayList) {
        ArrayList<Double> matrix = new ArrayList<Double>();
       /* if (phraseCountArrayList.size()==1) {
            matrix.add(new Double(1));
        }*/
        // For each phrase we create a vector comparison to the others
        for (PhraseCount pcount1 : phraseCountArrayList) {
            Double simScore = 1.0;
            for (PhraseCount pcount2 : phraseCountArrayList) {
                    ArrayList<SimilarityPair> similarityPairArrayList = vu.wntools.wnsimilarity.WordnetSimilarityApi.wordLeacockChodorowSimilarity(wordnetLmfSaxParser.wordnetData, pcount1.getPhrase(), pcount2.getPhrase());
                    SimilarityPair similarityPair = vu.wntools.wnsimilarity.WordnetSimilarityApi.getTopScoringSimilarityPair(similarityPairArrayList);
                    simScore += similarityPair.getScore();
            }
            /// average similarity score is added to the matrix
            matrix.add(simScore/(phraseCountArrayList.size()));

        }

       /* Double maxSimScore = 0.0;

        for (int i = 0; i < matrix.size(); i++) {
            Double aDouble = matrix.get(i);
            if (aDouble>maxSimScore) {
                maxSimScore = aDouble;
            }
        }
        for (int i = 0; i < matrix.size(); i++) {
            Double aDouble = matrix.get(i);
            aDouble = aDouble/maxSimScore;
            System.out.println("aDouble = " + aDouble);
            matrix.set(i,aDouble);
        }*/
        return matrix;
    }


    static void readData (File file) {
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                int cnt = 0;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    cnt++;
                    //if (cnt==10) break;
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        //178149#h#1#UNK#(Shooting,shooting,struck,shot,shooting,struck,shot)
                        String [] fields = inputLine.split("#");
                        String str = "";
                        for (int i = 0; i < fields.length; i++) {
                            str += fields[i] + "#";
                        }
                        String type = fields[1].trim();
                        String mentionField = fields[4].substring(1, fields[4].length()-1);
                        String [] mentions = mentionField.split(",");
                        ArrayList<PhraseCount> phraseCountArrayList = new ArrayList<PhraseCount>();
                        for (int j = 0; j < mentions.length; j++) {
                            String lemma = "";
                          //lemma = Util.getLemmaCoreNLP(pipeline, mentions[j].toLowerCase());
                            boolean match = false;
                            for (int k = 0; k < phraseCountArrayList.size(); k++) {
                                PhraseCount phraseCount = phraseCountArrayList.get(k);
                                if (phraseCount.getPhrase().equals(lemma))  {
                                    phraseCount.incrementCount();
                                    match = true;
                                    break;
                                }
                            }
                            if (!match) phraseCountArrayList.add(new PhraseCount(lemma,1));
                        }
                       // SortedSet<PhraseCount> treeSet = freqSortPhraseCountArrayList (phraseCountArrayList);
                        SortedSet<PhraseCount> treeSet = Util.formSortPhraseCountArrayList (phraseCountArrayList);

                        ArrayList<Double> matrix = getDistanceMatrix(treeSet);
                        int nForms = treeSet.size();
                        int nMentions = mentions.length;
                        str += nForms+"#"+nMentions+"#";
                        int i = 0;
                        for (PhraseCount phraseCount : treeSet) {
                            Double centralityScore = matrix.get(i);
                            str += phraseCount.getPhraseCount()+":"+df.format(centralityScore)+";";
                            i++;
                            if (typeLex.containsKey(type)) {
                                ArrayList<PhraseCount> pCounts = typeLex.get(type);
                                phraseCount.addToArrayList(pCounts);
                                typeLex.put(type, pCounts);
                            }
                            else {
                                ArrayList<PhraseCount> lex = new ArrayList<PhraseCount>();
                                lex.add(phraseCount);
                                typeLex.put(type, lex);
                            }
                            phraseCount.addToArrayList(dataLex);
                        }
                        System.out.println(str);
                    }
                }
                Set keySet = typeLex.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    ArrayList<PhraseCount> phraseCountArrayList = typeLex.get(key);
                    String str = key+"#"+phraseCountArrayList.size()+"#";
                    int tokens = 0;
                    SortedSet<PhraseCount> treeSet = Util.freqSortPhraseCountArrayList (phraseCountArrayList);
                    for (PhraseCount phraseCount : treeSet) {
                        tokens += phraseCount.getCount();
                    }
                    str += tokens+"#";
                    for (PhraseCount phraseCount : treeSet) {
                        str += phraseCount.getPhraseCount()+";";
                    }
                    System.out.println(str);
                }
                SortedSet<PhraseCount> treeSet = Util.freqSortPhraseCountArrayList (dataLex);
                for (PhraseCount phraseCount : treeSet) {
                    System.out.println(phraseCount.getPhraseCount());
                }
               
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /*static ArrayList<ReferenceData> readCSVfile (String filePath, String esoFilter, String fnFilter, Integer threshold)  {
        java.util.ArrayList<ReferenceNet.ReferenceData> data = new ArrayList<ReferenceNet.ReferenceData>();
        System.out.println("filePath = " + filePath);
        if (new File(filePath).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(filePath);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                int cnt = 0;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    cnt++;
                    //if (cnt==10) break;
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        ReferenceNet.ReferenceData referenceData = csvToData(inputLine.trim());
                        if (!esoFilter.isEmpty() || !fnFilter.isEmpty()) {
                            if (referenceData.checkEsoFilter(esoFilter) || referenceData.checkFnFilter(fnFilter)) {
                                data.add(referenceData);
                            }
                            else {
                                //  data.add(referenceData);
                            }
                        }
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }*/
}
