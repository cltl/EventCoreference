package eu.newsreader.eventcoreference.output;

import eu.newsreader.eventcoreference.objects.CoRefSet;
import eu.newsreader.eventcoreference.objects.CorefTarget;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafTerm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 3/15/12
 * Time: 1:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class CorefTargetsInKafSentences {

    static public void main (String args[]) {
        try {
            File lF = new File(args[0]);
            File[] theFileList = null;
            if ((lF.canRead()) && lF.isDirectory()) {
                theFileList = lF.listFiles();
            }
            else {
                theFileList = new File[1];
                theFileList[0]=lF;
            }
            if (theFileList!=null) {
                FileOutputStream fos = new FileOutputStream(lF.getAbsolutePath()+"/coref-sentences.xml");
                String str ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
                str += "<COREF source-folder=\""+lF.getAbsolutePath()+"\">"+"\n";
                fos.write(str.getBytes());
                KafSaxParser parser = new KafSaxParser();
                for (int f = 0; f < theFileList.length; f++) {
                    File kaf = theFileList[f];
                    System.out.println("kaf.getName() = " + kaf.getName());
                    str = "<co-ref-sets file=\""+kaf.getName()+"\">\n";
                    fos.write(str.getBytes());
                    parser.parseFile(kaf);
                    HashMap<String, CoRefSet> eventSets = new HashMap<String, CoRefSet>();
                    for (int i = 0; i < parser.kafTermList.size(); i++) {
                        KafTerm kafTerm = parser.kafTermList.get(i);
                        if ((kafTerm.getPos().toLowerCase().startsWith("n")) ||
                                (kafTerm.getPos().toLowerCase().startsWith("v")) ||
                                (kafTerm.getPos().toLowerCase().startsWith("pp"))  // personal pronouns: he, she, they, we, us, it, them
                                ) {
                            String sentenceId = parser.getSentenceId(kafTerm);
                            if (eventSets.containsKey(sentenceId)) {
                                CoRefSet corefSet = eventSets.get(sentenceId);
                                CorefTarget target = new CorefTarget();
                                target.setDocId(kaf.getName());
                                target.setTermId(kafTerm.getTid());
                                corefSet.addTarget(target);
                                eventSets.put(sentenceId, corefSet);
                            }
                            else {
                                CoRefSet corefSet = new CoRefSet();
                                corefSet.setId(sentenceId);
                                CorefTarget target = new CorefTarget();
                                target.setDocId(kaf.getName());
                                target.setTermId(kafTerm.getTid());
                                corefSet.addTarget(target);
                                eventSets.put(sentenceId, corefSet);
                            }
                        }
                    }
                    Set keySet = eventSets.keySet();
                    Iterator keys = keySet.iterator();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        CoRefSet corefs = eventSets.get(key);
                        str = corefs.toString();
                        fos.write(str.getBytes());
                    }
                    str = "</co-ref-sets>\n";
                    fos.write(str.getBytes());
                }
                str = "</COREF>\n";
                fos.write(str.getBytes());
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
