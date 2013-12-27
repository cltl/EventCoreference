package eu.newsreader.eventcoreference.output;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafTerm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 3/15/12
 * Time: 1:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class CoreferenceBaseline {

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
                FileOutputStream fos = new FileOutputStream(lF.getAbsolutePath()+"/coref-word-baseline.xml");
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
                    HashMap<String, ArrayList<String>> eventSets = new HashMap<String, ArrayList<String>>();
                    for (int i = 0; i < parser.kafTermList.size(); i++) {
                        KafTerm kafTerm = parser.kafTermList.get(i);
                        if ((kafTerm.getPos().toLowerCase().startsWith("n")) ||
                                (kafTerm.getPos().toLowerCase().startsWith("v"))) {
                            if (eventSets.containsKey(kafTerm.getLemma())) {
                                ArrayList<String> corefSet = eventSets.get(kafTerm.getLemma());
                                corefSet.add(kafTerm.getTid());
                                eventSets.put(kafTerm.getLemma(), corefSet);
                            }
                            else {
                                ArrayList<String> corefSet = new ArrayList<String>();
                                corefSet.add(kafTerm.getTid());
                                eventSets.put(kafTerm.getLemma(), corefSet);
                            }
                        }
                    }
                    Set keySet = eventSets.keySet();
                    Iterator keys = keySet.iterator();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        ArrayList<String> corefs = eventSets.get(key);
                        str = "\t<co-refs>\n";
                        for (int i = 0; i < corefs.size(); i++) {
                            String s = corefs.get(i);
                            str += "\t\t<target id=\""+s+"\"/>\n";
                        }
                        str += "\t</co-refs>\n";
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
