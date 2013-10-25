package eu.newsreader.eventcoreference.output;

import eu.newsreader.eventcoreference.input.CorefSaxParser;
import eu.newsreader.eventcoreference.objects.CoRefSet;
import eu.newsreader.eventcoreference.objects.CorefTarget;

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
 * Time: 1:12 PM
 * To change this template use File | Settings | File Templates.
 *
 * Reads a file with coreference sets for actions and files with coreferences sets for other event components.
 * It creates an output file of so-called SemEvents. A SemEvent is a structure with a list of event mentions
 *  and for each event mention a list of components shared in the same sentence. Each component is represented as
 *  an extensional entity with a list of mentions.
 *
 *
 *
 */
public class CombineCorefOutputPerEvent {
    
    static final String usage = "\nTakes the following arguments\n" +
            "\t--event\t<path to file with event coreferences with sentence identifiers>\n"+
            "\t--participant\t<path to file with participant coreferences and sentence identifiers\n"+
            "\t--location\t<path to file with location coreferences and sentence identifiers\n"+
            "\t--time\t<path to file with time coreferences and sentence identifiers\n";

    static public void main (String[] args) {
        boolean STOP = false;
        int sentenceRange = 0;
        String eventFilePath = "";
        String participantFilePath = "";
        String locationFilePath = "";
        String timeFilePath = "";
        String componentId = ".";
        String outputlabel = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--event") && (args.length>i))  {
                eventFilePath = args[i+1];
                componentId+="e";
            }
            else if (arg.equals("--range") && (args.length>i))  {
                try {
                    sentenceRange = Integer.parseInt(args[i+1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            else if (arg.equals("--participant") && (args.length>i))  {
                participantFilePath = args[i+1];
                componentId+="p";
            }
            else if (arg.equals("--time") && (args.length>i))  {
                timeFilePath = args[i+1];
                componentId+="t";
            }
            else if (arg.equals("--location") && (args.length>i))  {
                locationFilePath = args[i+1];
                componentId+="l";
            }
            else if (arg.equals("--label") && (args.length>i))  {
                outputlabel = args[i+1];
            }
        }
        if (eventFilePath.isEmpty()) {
            System.out.println("Missing argument --event <path to coreference events file");
            STOP = true;
        }
        if (participantFilePath.isEmpty()) {
            System.out.println("Missing argument --participant <path to coreference participants file");
        }
        if (timeFilePath.isEmpty()) {
            System.out.println("Missing argument --time <path to coreference time file");
        }
        if (locationFilePath.isEmpty()) {
            System.out.println("Missing argument --location <path to coreference location file");
        }
        if (!STOP) {
            try {
               // String outputFilePath = eventFilePath+componentId+".sentenceRange."+sentenceRange+"."+outputlabel+"-semevent.xml";
                String outputFilePath = new File(eventFilePath).getParent()+"/"+outputlabel+".sentenceRange."+sentenceRange+"."+"semevent.xml";
                FileOutputStream fos = new FileOutputStream(outputFilePath);
                String str ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
                str += "<SEM>"+"\n";
                fos.write(str.getBytes());


                // we read the events and their components using a coreference parser
                // the parser builds up a HashMap with file identifiers as keys and arraylists with elements as data within that file

                CorefSaxParser events = new CorefSaxParser();
                events.parseFile(eventFilePath);

                CorefSaxParser participants = new CorefSaxParser();
                if (new File(participantFilePath).exists()) {
                    participants.parseFile(participantFilePath);
                }

                CorefSaxParser times = new CorefSaxParser();
                if (new File(timeFilePath).exists()) {
                    times.parseFile(timeFilePath);
                }

                CorefSaxParser locations = new CorefSaxParser();
                if (new File(locationFilePath).exists()) {
                    locations.parseFile(locationFilePath);
                }


                /// we first iterate over the map with file identifiers and the event coref maps
                Set keySet = events.corefMap.keySet();
                Iterator keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    /// keys are file identifiers
                    // We now get the components for the key (= particular file identifier), so just for one file
                    ArrayList<CoRefSet> coRefSetsEvents = events.corefMap.get(key);
                    ArrayList<CoRefSet> participantSets = participants.corefMap.get(key);
                    ArrayList<CoRefSet> timeSets = times.corefMap.get(key);
                    ArrayList<CoRefSet> locationSets = locations.corefMap.get(key);

                    /// we create the initial output string
                    str = "<file name=\""+key+"\" eventCount=\""+coRefSetsEvents.size()+"\"";
                    str += " participantCount=\"";
                    if (participantSets!=null) {
                        str += participantSets.size()+"\"";
                    }
                    else {
                        str += "0\"";
                    }
                    str += " timeCount=\"";
                    if (timeSets!=null) {
                        str += timeSets.size()+"\"";
                    }
                    else {
                        str += "0\"";
                    }
                    str += " locationCount=\"";
                    if (locationSets!=null) {
                        str += locationSets.size()+"\"";
                    }
                    else {
                        str += "0\"";
                    }
                    str += ">\n";
                    fos.write(str.getBytes());

                    /// we iterate over the events of a single file
                    for (int i = 0; i < coRefSetsEvents.size(); i++) {
                        CoRefSet coRefSet = coRefSetsEvents.get(i);
                        str = "  <semEvent id=\""+coRefSet.getId()+"\" lcs=\""+coRefSet.getLcs()+"\" score=\""+coRefSet.getScore()+"\" synset=\""+coRefSet.getMostFrequentSynset()+"\" label=\""+coRefSet.getMostFrequentLemma()+"\" mentions=\""+coRefSet.getTargets().size()+"\">\n";
                        str += "\t<mentions>\n";
                        fos.write(str.getBytes());
                        for (int j = 0; j < coRefSet.getTargets().size(); j++) {
                            CorefTarget eventTarget = coRefSet.getTargets().get(j);
                            str = "\t<event-mention>\n";
                            str += "\t\t<event>\n";
                            str += "\t"+eventTarget.toString();
                            str += "\t\t</event>\n";
                            fos.write(str.getBytes());

                            /// we obtain the sentence ids for the targets of the coreference set of the events
                            /// this sentence range determines which components belong to the event.
                            /// by passing in the sentenceRange parameter you can indicate the number of sentences before and after that are also valid contexts
                            //// if zero the context is restricted to the same sentence
                            ArrayList<String> rangeOfSentenceIds = getSentenceRange(eventTarget.getSentenceId(), sentenceRange);

                            if (participantSets!=null) {
                                str ="\t\t<participants>\n";
                                for (int s = 0; s < participantSets.size(); s++) {
                                    CoRefSet refSet = participantSets.get(s);
          /*                          if (refSet.containsTargetSentenceId(eventTarget.getSentenceId())) {
                                       str += "\t\t\t<participant id=\""+refSet.getParticipantId()+"\" lcs=\""+refSet.getLcs()+"\" score=\""+refSet.getScore()+"\" synset=\""+refSet.getMostFrequentSynset()+"\" label=\""+refSet.getMostFrequentLemma()+"\" mentions=\""+refSet.getTargets().size()+"\">\n";
                                        for (int p = 0; p < refSet.getTargets().size(); p++) {
                                            CorefTarget target = refSet.getTargets().get(p);
                                            if (eventTarget.getSentenceId().equals(target.getSentenceId())) {
                                                str += "\t\t\t"+target.toString();
                                            }
                                        }
                                        str += "\t\t\t</participant>\n";
                                    }*/
                                    //// loop to add results for range of sentences
                                    for (int k = 0; k < rangeOfSentenceIds.size(); k++) {
                                        String sentenceId = rangeOfSentenceIds.get(k);
                                        if (refSet.containsTargetSentenceId(sentenceId)) {
                                            String targetString = "";
                                            for (int p = 0; p < refSet.getTargets().size(); p++) {
                                                CorefTarget target = refSet.getTargets().get(p);
                                                if (eventTarget.getDocId().equals(target.getDocId())) {
                                                    if (sentenceId.equals(target.getSentenceId())) {
                                                        targetString += "\t\t\t"+target.toString();
                                                    }
                                                }
                                            }
                                            if (!targetString.isEmpty()) {
                                                str += "\t\t\t<participant id=\""+refSet.getParticipantId()+"\" lcs=\""+refSet.getLcs()+"\" score=\""+refSet.getScore()+"\" synset=\""+refSet.getMostFrequentSynset()+"\" label=\""+refSet.getMostFrequentLemma()+"\" mentions=\""+refSet.getTargets().size()+"\">\n";
                                                str += targetString;
                                                str += "\t\t\t</participant>\n";
                                            }
                                        }

                                    }
                                }
                                str +="\t\t</participants>\n";
                                fos.write(str.getBytes());
                            }
                            else {
                                str = "\t\t<participants/>\n";
                                fos.write(str.getBytes());

                            }
                            if (timeSets!=null) {
                                str ="\t\t<times>\n";
                                for (int s = 0; s < timeSets.size(); s++) {
                                    CoRefSet refSet = timeSets.get(s);
/*                                    if (refSet.containsTargetSentenceId(eventTarget.getSentenceId())) {
                                        str += "\t\t\t<time id=\""+refSet.getTimeId()+"\" lcs=\""+refSet.getLcs()+"\" score=\""+refSet.getScore()+"\" synset=\""+refSet.getMostFrequentSynset()+"\" label=\""+refSet.getMostFrequentLemma()+"\" mentions=\""+refSet.getTargets().size()+"\">\n";
                                        for (int p = 0; p < refSet.getTargets().size(); p++) {
                                            CorefTarget target = refSet.getTargets().get(p);
                                            if (eventTarget.getSentenceId().equals(target.getSentenceId())) {
                                                str += "\t\t\t"+target.toString();
                                            }
                                        }
                                        str += "\t\t\t</time>\n";
                                    }*/
                                    //// loop to add results for range of sentences
                                    for (int k = 0; k < rangeOfSentenceIds.size(); k++) {
                                        String sentenceId = rangeOfSentenceIds.get(k);
                                        if (refSet.containsTargetSentenceId(sentenceId)) {

                                            String targetString = "";
                                            for (int p = 0; p < refSet.getTargets().size(); p++) {
                                                CorefTarget target = refSet.getTargets().get(p);
                                                if (eventTarget.getDocId().equals(target.getDocId())) {
                                                    if (sentenceId.equals(target.getSentenceId())) {
                                                        targetString += "\t\t\t"+target.toString();
                                                    }
                                                }
                                            }
                                            if (!targetString.isEmpty()) {
                                                str += "\t\t\t<time id=\""+refSet.getParticipantId()+"\" lcs=\""+refSet.getLcs()+"\" score=\""+refSet.getScore()+"\" synset=\""+refSet.getMostFrequentSynset()+"\" label=\""+refSet.getMostFrequentLemma()+"\" mentions=\""+refSet.getTargets().size()+"\">\n";
                                                str += targetString;
                                                str += "\t\t\t</time>\n";
                                            }


/*
                                            str += "\t\t\t<time id=\""+refSet.getTimeId()+"\" lcs=\""+refSet.getLcs()+"\" score=\""+refSet.getScore()+"\" synset=\""+refSet.getMostFrequentSynset()+"\" label=\""+refSet.getMostFrequentLemma()+"\" mentions=\""+refSet.getTargets().size()+"\">\n";
                                            for (int p = 0; p < refSet.getTargets().size(); p++) {
                                                CorefTarget target = refSet.getTargets().get(p);
                                                if (sentenceId.equals(target.getSentenceId())) {
                                                    str += "\t\t\t"+target.toString();
                                                }
                                            }
                                            str += "\t\t\t</time>\n";
*/
                                        }
                                    }
                                }
                                str +="\t\t</times>\n";
                                fos.write(str.getBytes());
                            }
                            else {
                                str = "\t\t<times/>\n";
                                fos.write(str.getBytes());

                            }
                            if (locationSets!=null) {
                                str ="\t\t<locations>\n";
                                for (int s = 0; s < locationSets.size(); s++) {
                                    CoRefSet refSet = locationSets.get(s);
/*                                    if (refSet.containsTargetSentenceId(eventTarget.getSentenceId())) {
                                        str += "\t\t\t<location id=\""+refSet.getLocationId()+"\" lcs=\""+refSet.getLcs()+"\" score=\""+refSet.getScore()+"\" synset=\""+refSet.getMostFrequentSynset()+"\" label=\""+refSet.getMostFrequentLemma()+"\" mentions=\""+refSet.getTargets().size()+"\">\n";
                                        for (int p = 0; p < refSet.getTargets().size(); p++) {
                                            CorefTarget target = refSet.getTargets().get(p);
                                            if (eventTarget.getSentenceId().equals(target.getSentenceId())) {
                                                str += "\t\t\t"+target.toString();
                                            }
                                        }
                                        str += "\t\t\t</location>\n";
                                    }*/
                                    //// loop to add results for range of sentences
                                    for (int k = 0; k < rangeOfSentenceIds.size(); k++) {
                                        String sentenceId = rangeOfSentenceIds.get(k);
                                        if (refSet.containsTargetSentenceId(sentenceId)) {

                                            String targetString = "";
                                            for (int p = 0; p < refSet.getTargets().size(); p++) {
                                                CorefTarget target = refSet.getTargets().get(p);
                                                if (eventTarget.getDocId().equals(target.getDocId())) {
                                                    if (sentenceId.equals(target.getSentenceId())) {
                                                        targetString += "\t\t\t"+target.toString();
                                                    }
                                                }
                                            }
                                            if (!targetString.isEmpty()) {
                                                str += "\t\t\t<location id=\""+refSet.getParticipantId()+"\" lcs=\""+refSet.getLcs()+"\" score=\""+refSet.getScore()+"\" synset=\""+refSet.getMostFrequentSynset()+"\" label=\""+refSet.getMostFrequentLemma()+"\" mentions=\""+refSet.getTargets().size()+"\">\n";
                                                str += targetString;
                                                str += "\t\t\t</location>\n";
                                            }

/*
                                            str += "\t\t\t<location id=\""+refSet.getLocationId()+"\" lcs=\""+refSet.getLcs()+"\" score=\""+refSet.getScore()+"\" synset=\""+refSet.getMostFrequentSynset()+"\" label=\""+refSet.getMostFrequentLemma()+"\" mentions=\""+refSet.getTargets().size()+"\">\n";
                                            for (int p = 0; p < refSet.getTargets().size(); p++) {
                                                CorefTarget target = refSet.getTargets().get(p);
                                                if (sentenceId.equals(target.getSentenceId())) {
                                                    str += "\t\t\t"+target.toString();
                                                }
                                            }
                                            str += "\t\t\t</location>\n";
*/
                                        }
                                    }
                                }
                                str +="\t\t</locations>\n";
                                fos.write(str.getBytes());
                            }
                            else {
                                str = "\t\t<locations/>\n";
                                fos.write(str.getBytes());

                            }
                            str = "\t</event-mention>\n";
                            fos.write(str.getBytes());
                        }
                        str = "\t</mentions>\n";
                        fos.write(str.getBytes());
                        str = "</semEvent>\n";
                        fos.write(str.getBytes());
                    }
                    str = "</file>\n";
                    fos.write(str.getBytes());
                }
                str = "  </SEM>\n";
                fos.write(str.getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    static ArrayList<CoRefSet> getCorefSetFromRange (HashMap<String, ArrayList<CoRefSet>> corefMap, String sentenceIdString, int sentenceRange) {
        ArrayList<CoRefSet> coRefSets = null;
        coRefSets = corefMap.get(sentenceIdString);
        if (sentenceRange>0) {
            /// we assume that the sentence id is an integer
            Integer sentenceId = Integer.parseInt(sentenceIdString);
            if (sentenceId!=null) {
                for (int i = sentenceId; i < sentenceRange; i++) {
                    ArrayList<CoRefSet> nextSet = corefMap.get(sentenceId+i);
                    if (nextSet!=null) {
                        for (int j = 0; j < nextSet.size(); j++) {
                            CoRefSet coRefSet = nextSet.get(j);
                            coRefSets.add(coRefSet);
                        }
                    }
                }
/*                for (int i = sentenceId; i < sentenceRange; i++) {
                    ArrayList<CoRefSet> nextSet = corefMap.get(sentenceId-i);
                    if (nextSet!=null) {
                        for (int j = 0; j < nextSet.size(); j++) {
                            CoRefSet coRefSet = nextSet.get(j);
                            coRefSets.add(coRefSet);
                        }
                    }
                }*/
            }
        }
        return coRefSets;
    }

    static ArrayList<String> getSentenceRange (String sentenceIdString, int sentenceRange) {
        ArrayList<String> sentenceIdRange = new ArrayList<String>();
        sentenceIdRange.add(sentenceIdString);
        if (sentenceRange>0) {
            /// we assume that the sentence id is an integer
            Integer sentenceId = null;
            try {
                sentenceId = Integer.parseInt(sentenceIdString);
            } catch (NumberFormatException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            if (sentenceId!=null) {
                for (int i = sentenceId; i < sentenceRange; i++) {
                    sentenceIdRange.add(new Integer(i).toString());
                }
                for (int i = sentenceId; i < sentenceRange; i++) {
                    sentenceIdRange.add(new Integer(i).toString());
                }
            }
        }
        return sentenceIdRange;
    }
}
