package eu.newsreader.eventcoreference.output;

import eu.newsreader.eventcoreference.input.CorefSaxParser;
import eu.newsreader.eventcoreference.objects.CoRefSetAgata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 10/11/13
 * Time: 11:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class DocumentToCorpusCoreferences {

    static public void main (String[] args) {
       // String corefFilePath = "/Users/kyoto/Desktop/Events/ECB/ECBcorpus_StanfordAnnotation/EECB1.0/gold-standard/eecb-events-gold-standard-old-first-n-v-token-3.xml";
       // String corefFilePath = "/Users/kyoto/Desktop/Events/ECB/ECBcorpus_StanfordAnnotation/EECB1.0/gold-standard/eecb-entities-gold-standard-old-first-n-v-token-3.xml";
       // String corefFilePath = "/Users/kyoto/Desktop/Events/ECB/ECBcorpus_StanfordAnnotation/EECB1.0/results-1/lemma-cross-corpus-all/eecb-events-kyoto-first-n-v-token-3.xml.sim.word-baseline.0.coref.xml";
        //String corefFilePath = "/Users/kyoto/Desktop/Events/ECB/ECBcorpus_StanfordAnnotation/EECB1.0/results-1/lemma-cross-corpus-all/Time-26-jul-2013.xml.sim.word-baseline.0.coref.xml";
        //String corefFilePath = "/Users/kyoto/Desktop/Events/ECB/ECBcorpus_StanfordAnnotation/EECB1.0/results-1/lemma-cross-corpus-all/participants-30-july-2013.xml.sim.word-baseline.0.coref.xml";
        String corefFilePath = "/Users/kyoto/Desktop/Events/ECB/ECBcorpus_StanfordAnnotation/EECB1.0/results-1/lemma-cross-corpus-all/Location-26-jul-2013.xml.sim.word-baseline.0.coref.xml";
        String corpus = "eecb1.0";
        boolean addCorpusToId = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--coref-file") && (args.length>i))  {
                corefFilePath = args[i+1];
            }
            else if (arg.equals("--corpus") && (args.length>i))  {
                corpus = args[i+1];
            }
            else if (arg.equals("--add-corpus"))  {
                addCorpusToId = true;
            }
        }
        if (corefFilePath.isEmpty()) {
            System.out.println("Missing argument --coref-file <path to coreference file>");
            return;
        }
        else {
            try {
                CorefSaxParser corefSaxParser = new CorefSaxParser();
                corefSaxParser.parseFile(corefFilePath);
                /// we first iterate over the map with file identifiers and the event coref maps
                Set keySet = corefSaxParser.corefMap.keySet();
                Iterator keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    /// keys are file identifiers
                    // We now get the components for the key (= particular file identifier), so just for one file
                    ArrayList<CoRefSetAgata> coRefSetAgatas = corefSaxParser.corefMap.get(key);
                    for (int i = 0; i < coRefSetAgatas.size(); i++) {
                        CoRefSetAgata coRefSetAgata = coRefSetAgatas.get(i);
                        String newId = coRefSetAgata.getId();
                        if (addCorpusToId) {
                            newId = corpus+"/"+newId;
                        }
                        coRefSetAgata.setId(newId);
                    }
                }
                String outputFilePath = corefFilePath+"."+"cross-corpus.xml";
                corefSaxParser.serializeCrossCorpus(outputFilePath, corpus);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
