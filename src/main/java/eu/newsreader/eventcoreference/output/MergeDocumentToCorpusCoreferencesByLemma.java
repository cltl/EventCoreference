package eu.newsreader.eventcoreference.output;

import eu.newsreader.eventcoreference.input.CorefSaxParser;
import eu.newsreader.eventcoreference.objects.CoRefSet;
import eu.newsreader.eventcoreference.objects.CorefTarget;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 10/11/13
 * Time: 11:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class MergeDocumentToCorpusCoreferencesByLemma {

    static public void main (String[] args) {
        String corefFilePath = "/Users/kyoto/Desktop/Events/ECB/ECBcorpus_StanfordAnnotation/EECB1.0/results-1/lemma-cross-corpus-all/eecb-events-kyoto-first-n-v-token-3.xml.sim.word-baseline.0.coref.xml.cross-corpus.xml";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--coref-file") && (args.length>i))  {
                corefFilePath = args[i+1];
            }
        }
        if (corefFilePath.isEmpty()) {
            System.out.println("Missing argument --event <path to coreference events file");
            return;
        }
        else {
            try {
                CorefSaxParser corefSaxParser = new CorefSaxParser();
                corefSaxParser.parseFile(corefFilePath);
                /// we first iterate over the map with file identifiers and the event coref maps
                HashMap<String, CoRefSet> lemmaMap = new HashMap<String, CoRefSet>();
                Set keySet = corefSaxParser.corefMap.keySet();
                Iterator keys = keySet.iterator();
                int setCount = 0;
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    /// keys are file identifiers
                    // We now get the components for the key (= particular file identifier), so just for one file
                    ArrayList<CoRefSet> coRefSetsEvents = corefSaxParser.corefMap.get(key);
                    for (int i = 0; i < coRefSetsEvents.size(); i++) {
                        CoRefSet coRefSet = coRefSetsEvents.get(i);
                        for (int j = 0; j < coRefSet.getTargets().size(); j++) {
                            CorefTarget corefTarget = coRefSet.getTargets().get(j);
                            if (lemmaMap.containsKey(corefTarget.getWord())) {
                                CoRefSet coRefSetLemma = lemmaMap.get(corefTarget.getWord());
                                coRefSetLemma.addTarget(corefTarget);
                                lemmaMap.put(corefTarget.getWord(), coRefSetLemma);
                            }
                            else {
                                CoRefSet coRefSetLemma = new CoRefSet();
                                setCount++;
                                coRefSetLemma.setId(corefSaxParser.corpusName+"/"+setCount);
                                coRefSetLemma.addTarget(corefTarget);
                                lemmaMap.put(corefTarget.getWord(), coRefSetLemma);
                            }
                        }
                    }

                }
                String outputPath = corefFilePath+".cross-corpus-lemma.xml";
                serializeCrossCorpus(outputPath, corefSaxParser, lemmaMap);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void serializeCrossCorpus (String outputFilePath, CorefSaxParser corefSaxParser, HashMap<String, CoRefSet> lemmaMap) {
        try {
            FileOutputStream fos = new FileOutputStream(outputFilePath);
            String str ="";
            str ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
            str += "<co-ref-sets corpus=\""+corefSaxParser.corpusName+"\"";
            if (!corefSaxParser.method.isEmpty()) str += " method=\""+corefSaxParser.method+"\"";
            if (!corefSaxParser.threshold.isEmpty()) str += " threshold=\""+corefSaxParser.threshold+"\"";
            str += ">\n";
            fos.write(str.getBytes());
            Set keySet = lemmaMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                CoRefSet coRefSet = lemmaMap.get(key);
                str = coRefSet.toString();
                fos.write(str.getBytes());
            }
            str = "</co-ref-sets>\n";
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
