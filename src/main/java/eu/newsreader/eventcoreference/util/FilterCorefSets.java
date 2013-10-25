package eu.newsreader.eventcoreference.util;

import eu.newsreader.eventcoreference.input.CorefSaxParser;
import eu.newsreader.eventcoreference.objects.CoRefSet;
import eu.newsreader.eventcoreference.objects.CorefTarget;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 3/18/13
 * Time: 7:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class FilterCorefSets {


    static public void main (String[] args) {
        //String pathToFilterFile = args[0];
        //String pathToCorefFile = args[1];
        String pathToFilterFile = "/Users/kyoto/Desktop/Event structure/ISI-2/version4/ellipticals_final_list.txt";
        String pathToCorefFile = "/Users/kyoto/Desktop/Event structure/ISI-2/" +
                "isi-gold-standard/coref-gold-standard.xml";
        CorefSaxParser corefSaxParser = new CorefSaxParser();
       // corefSaxParser.namesubstring = 21;
        corefSaxParser.parseFile(pathToCorefFile);
        try {
            FileInputStream fis = new FileInputStream(pathToFilterFile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            String filteredCorefFile = pathToCorefFile+".filtered.xml";
            while (in.ready()&&(inputLine = in.readLine()) != null) {
                if (inputLine.trim().startsWith("t")) {
                    //t2221	XIN_ENG_20030919.0267
                    String[] fields = inputLine.split("\t");
                    if (fields.length==2) {
                        String tid = fields[0];
                        String fileName = fields[1].substring(0, 21);
                        System.out.println("fileName = " + fileName);
/*                        if (!fileName.startsWith("NYT_ENG_20050312.0073")) {
                            continue;
                        }*/
                        Set keySet = corefSaxParser.corefMap.keySet();
                        Iterator keys = keySet.iterator();
                        while (keys.hasNext()) {
                            String key = (String) keys.next();
                            if (key.startsWith(fileName)) {
                                ArrayList<CoRefSet> coRefSet = corefSaxParser.corefMap.get(key);
                                boolean match = false;
                                for (int j = 0; j < coRefSet.size(); j++) {
                                    CoRefSet set = coRefSet.get(j);
                                    for (int k = 0; k < set.getTargets().size(); k++) {
                                        CorefTarget corefTarget = set.getTargets().get(k);
                                        // System.out.println("corefTarget.getTermId() = " + corefTarget.getTermId());
                                        if (corefTarget.getTermId().equals(tid)) {
                                            System.out.println("BAD corefTarget.getTermId() = " + corefTarget.getTermId());
                                            System.out.println("fileName = " + fileName);
                                            set.getTargets().remove(k);
                                            match = true;
                                            break;
                                        }
                                    }
                                    if (match) {
                                        break;
                                    }
                                }
                                if (!match) {
                                    System.out.println("No matches for = " + tid);
                                }
                            }
                        }
                        /*if (corefSaxParser.corefMap.containsKey(fileName)) {
                            ArrayList<CoRefSet> coRefSet = corefSaxParser.corefMap.get(fileName);
                            boolean match = false;
                            for (int j = 0; j < coRefSet.size(); j++) {
                                CoRefSet set = coRefSet.get(j);
                                for (int k = 0; k < set.getTargets().size(); k++) {
                                    CorefTarget corefTarget = set.getTargets().get(k);
                                   // System.out.println("corefTarget.getTermId() = " + corefTarget.getTermId());
                                    if (corefTarget.getTermId().equals(tid)) {
                                        System.out.println("BAD corefTarget.getTermId() = " + corefTarget.getTermId());
                                        System.out.println("fileName = " + fileName);
                                        set.getTargets().remove(k);
                                        match = true;
                                        break;
                                    }
                                }
                                if (match) {
                                    break;
                                }
                            }
                            if (!match) {
                                    System.out.println("No matches for = " + tid);
                            }
                        }
                        else {
                            System.out.println("cannot find data for fileName = " + fileName);
                        }*/
                    }
                }
            }
            corefSaxParser.serialize(filteredCorefFile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
