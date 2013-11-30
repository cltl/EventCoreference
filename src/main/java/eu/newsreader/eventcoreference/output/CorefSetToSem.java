package eu.newsreader.eventcoreference.output;

import eu.newsreader.eventcoreference.input.CorefSaxParser;
import eu.newsreader.eventcoreference.objects.CoRefSetAgata;
import eu.newsreader.eventcoreference.objects.CorefTargetAgata;
import eu.newsreader.eventcoreference.objects.Triple;

import java.io.File;
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
 * Time: 11:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class CorefSetToSem {

    static public void main (String[] args) {
        boolean STOP = false;
        int sentenceRange = 0;
        String eventFilePath = "/Users/kyoto/Desktop/Events/ECB/ECBcorpus_StanfordAnnotation/EECB1.0/results-1/lemma-cross-document-in-topic/" +
                "eecb-events-kyoto-first-n-v-token-3.xml.sim.word-baseline.0.coref.xml";
        String participantFilePath = "/Users/kyoto/Desktop/Events/ECB/ECBcorpus_StanfordAnnotation/EECB1.0/results-1/lemma-cross-document-in-topic/" +
                "participants-30-july-2013.xml.sim.word-baseline.0.coref.xml";
        String locationFilePath = "/Users/kyoto/Desktop/Events/ECB/ECBcorpus_StanfordAnnotation/EECB1.0/results-1/lemma-cross-document-in-topic/" +
                "Location-26-jul-2013.xml.sim.word-baseline.0.coref.xml";
        String timeFilePath = "/Users/kyoto/Desktop/Events/ECB/ECBcorpus_StanfordAnnotation/EECB1.0/results-1/lemma-cross-document-in-topic/" +
                "Time-26-jul-2013.xml.sim.word-baseline.0.coref.xml";
        String componentId = ".";
        String outputlabel = "test";
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
                    ArrayList<CoRefSetAgata> coRefSetsEventAgatas = events.corefMap.get(key);
                    ArrayList<CoRefSetAgata> participantSets = participants.corefMap.get(key);
                    ArrayList<CoRefSetAgata> timeSets = times.corefMap.get(key);
                    ArrayList<CoRefSetAgata> locationSets = locations.corefMap.get(key);

                    /// we create the initial output string
                    str = "<topic name=\""+key+"\" eventCount=\""+ coRefSetsEventAgatas.size()+"\"";
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

                    if (coRefSetsEventAgatas !=null) {
                        /// we iterate over the events of a single file
                        str = "<semEvents>\n";
                        fos.write(str.getBytes());
                        for (int i = 0; i < coRefSetsEventAgatas.size(); i++) {
                            CoRefSetAgata coRefSetAgata = coRefSetsEventAgatas.get(i);
                            str = "\t<semEvent id=\""+key+"/e"+ coRefSetAgata.getId()+"\" lcs=\""+ coRefSetAgata.getLcs()+"\" score=\""+ coRefSetAgata.getScore()+"\" synset=\""+ coRefSetAgata.getMostFrequentSynset()+"\" label=\""+ coRefSetAgata.getMostFrequentLemma()+"\" mentions=\""+ coRefSetAgata.getTargets().size()+"\">\n";
                            fos.write(str.getBytes());
                            for (int j = 0; j < coRefSetAgata.getTargets().size(); j++) {
                                CorefTargetAgata eventTarget = coRefSetAgata.getTargets().get(j);
                                str = "\t"+eventTarget.toString();
                                fos.write(str.getBytes());
                            }
                            str = "\t</semEvent>\n";
                            fos.write(str.getBytes());
                        }
                        str = "</semEvents>\n";
                        fos.write(str.getBytes());
                    }

                    if (participantSets!=null) {
                        /// we iterate over the participants of a single file
                        str = "<semAgents>\n";
                        fos.write(str.getBytes());
                        for (int i = 0; i < participantSets.size(); i++) {
                            CoRefSetAgata coRefSetAgata = participantSets.get(i);
                            str = "\t<semAgent id=\""+key+"/a"+ coRefSetAgata.getId()+"\" lcs=\""+ coRefSetAgata.getLcs()+"\" score=\""+ coRefSetAgata.getScore()+"\" synset=\""+ coRefSetAgata.getMostFrequentSynset()+"\" label=\""+ coRefSetAgata.getMostFrequentLemma()+"\" mentions=\""+ coRefSetAgata.getTargets().size()+"\">\n";
                            fos.write(str.getBytes());
                            for (int j = 0; j < coRefSetAgata.getTargets().size(); j++) {
                                CorefTargetAgata eventTarget = coRefSetAgata.getTargets().get(j);
                                str = "\t"+eventTarget.toString();
                                fos.write(str.getBytes());
                            }
                            str = "\t</semAgent>\n";
                            fos.write(str.getBytes());
                        }
                        str = "</semAgents>\n";
                        fos.write(str.getBytes());
                    }

                    if (locationSets!=null) {
                        /// we iterate over the locations of a single file
                        str = "<semPlaces>\n";
                        fos.write(str.getBytes());
                        for (int i = 0; i < locationSets.size(); i++) {
                            CoRefSetAgata coRefSetAgata = locationSets.get(i);
                            str = "\t<semPlace id=\""+key+"/p"+ coRefSetAgata.getId()+"\" lcs=\""+ coRefSetAgata.getLcs()+"\" score=\""+ coRefSetAgata.getScore()+"\" synset=\""+ coRefSetAgata.getMostFrequentSynset()+"\" label=\""+ coRefSetAgata.getMostFrequentLemma()+"\" mentions=\""+ coRefSetAgata.getTargets().size()+"\">\n";
                            fos.write(str.getBytes());
                            for (int j = 0; j < coRefSetAgata.getTargets().size(); j++) {
                                CorefTargetAgata eventTarget = coRefSetAgata.getTargets().get(j);
                                str = "\t"+eventTarget.toString();
                                fos.write(str.getBytes());
                            }
                            str = "\t</semPlace>\n";
                            fos.write(str.getBytes());
                        }
                        str = "</semPlaces>\n";
                        fos.write(str.getBytes());
                    }

                    if (timeSets!=null) {
                        /// we iterate over the time of a single file
                        str = "<semTimes>\n";
                        fos.write(str.getBytes());
                        for (int i = 0; i < timeSets.size(); i++) {
                            CoRefSetAgata coRefSetAgata = timeSets.get(i);
                            str = "  <semTime id=\""+key+"/t"+ coRefSetAgata.getId()+"\" lcs=\""+ coRefSetAgata.getLcs()+"\" score=\""+ coRefSetAgata.getScore()+"\" synset=\""+ coRefSetAgata.getMostFrequentSynset()+"\" label=\""+ coRefSetAgata.getMostFrequentLemma()+"\" mentions=\""+ coRefSetAgata.getTargets().size()+"\">\n";
                            fos.write(str.getBytes());
                            for (int j = 0; j < coRefSetAgata.getTargets().size(); j++) {
                                CorefTargetAgata eventTarget = coRefSetAgata.getTargets().get(j);
                                str = "\t"+eventTarget.toString();
                                fos.write(str.getBytes());
                            }
                            str = "\t</semTime>\n";
                            fos.write(str.getBytes());
                        }
                        str = "</semTimes>\n";
                        fos.write(str.getBytes());
                    }

                    /// now we get the relations
                    getRelations(fos, events.fileName, coRefSetsEventAgatas, participantSets, timeSets, locationSets, sentenceRange, key);
                    str = "</topic>\n";
                    fos.write(str.getBytes());
                }
                str = "</SEM>\n";
                fos.write(str.getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    /**
     * <semEvents>
     <semEvent id="eecb1.0/456>
     <target id="corpus/filename_url/t115"/>
     </semEvent>
     </semEvents>
     <semAgents>
     <semAgent id="eecb1.0/2">
     <target id="corpus/filename_url/t111"/>
     </semAgent>
     </semAgents>

     <semTimes>
     <semTime id="eecb1.0/1"/>
     </semTimes>

     <semPlaces>
     <semPlace id="eecb1.0/6"/>
     </semPlaces>
     <semRelations>
     <semRelation id = "eecb1.0/75698" predicate="semHasAgent" subject="eecb1.0/456" object="eecb1.0/2>
     <target id="corpus/filename_url/pr23r3"/>
     </semRelation>
     <semRelation id = "eecb1.0/75698" predicate="semHasTime" subject="eecb1.0/456" object="eecb1.0/2/>
     <semRelation id = "eecb1.0/75698" predicate="semHasPlace" subject="eecb1.0/456" object="eecb1.0/2/>
     </semRelations>
     */



    static void getRelations (FileOutputStream fos, String fileName,
            ArrayList<CoRefSetAgata> coRefSetsEventAgatas,
            ArrayList<CoRefSetAgata> participantSets,
            ArrayList<CoRefSetAgata> timeSets,
            ArrayList<CoRefSetAgata> locationSets,
            int sentenceRange,
            String key
    ) throws IOException {
        String str = "<semRelations>\n";
        fos.write(str.getBytes());
        int relationCounter = 0;
        /// we iterate over the events of a single file
        ArrayList<Triple> triplesA = new ArrayList<Triple>();
        ArrayList<Triple> triplesP = new ArrayList<Triple>();
        ArrayList<Triple> triplesT = new ArrayList<Triple>();

        for (int i = 0; i < coRefSetsEventAgatas.size(); i++) {
            CoRefSetAgata coRefSetAgata = coRefSetsEventAgatas.get(i);
            for (int j = 0; j < coRefSetAgata.getTargets().size(); j++) {
                CorefTargetAgata eventTarget = coRefSetAgata.getTargets().get(j);

                /// we obtain the sentence ids for the targets of the coreference set of the events
                /// this sentence range determines which components belong to the event.
                /// by passing in the sentenceRange parameter you can indicate the number of sentences before and after that are also valid contexts
                //// if zero the context is restricted to the same sentence
                ArrayList<String> rangeOfSentenceIds = getSentenceRange(eventTarget.getSentenceId(), sentenceRange);
                if (participantSets!=null) {
                   // System.out.println("PARTICIPANTS");
                    for (int s = 0; s < participantSets.size(); s++) {
                        CoRefSetAgata refSet = participantSets.get(s);
                        //// loop to add results for range of sentences
                        for (int k = 0; k < rangeOfSentenceIds.size(); k++) {
                            String sentenceId = rangeOfSentenceIds.get(k);
                            if (refSet.containsTargetSentenceId(sentenceId)) {
                                for (int l = 0; l < refSet.getTargets().size(); l++) {
                                    CorefTargetAgata corefTargetAgata = refSet.getTargets().get(l);
                                    if (eventTarget.getDocId().equals(corefTargetAgata.getDocId())) {
                                        String predicate = "semHasAgent";
                                        String subject = key+"/e"+ coRefSetAgata.getId();
                                        String object =  key+"/a"+ refSet.getId();
                                        Triple triple = new Triple(predicate, subject, object);
                                        String target = "\t\t<target id =\""+ eventTarget.getDocId()+"/"+eventTarget.getSentenceId()+"\""+"/>";
                                        target += " <!-- "+eventTarget.getWord()+"--"+predicate+"--"+ corefTargetAgata.getWord()+" -->";
                                        int givenTriple = getTriple(triplesA, triple);
                                        if (givenTriple==-1) {
                                            relationCounter++;
                                            String id = key+"/"+relationCounter;
                                            triple.setId(id);
                                            triple.addMentions(target);
                                            triplesA.add(triple);
                                        }
                                        else {
                                            if (!triplesA.get(givenTriple).getMentions().contains(target)) {
                                                triplesA.get(givenTriple).addMentions(target);
                                            }
                                        }
                                    }
                                    else {
                                      //  System.out.println("corefTarget.getDocId() = " + corefTarget.getDocId());
                                      //  System.out.println("eventTarget.getDocId() = " + eventTarget.getDocId());
                                    }
                                }
                            }
                        }
                    }
                }
                if (timeSets!=null) {
                   // System.out.println("TIME");
                    for (int s = 0; s < timeSets.size(); s++) {
                        CoRefSetAgata refSet = timeSets.get(s);
                        //// loop to add results for range of sentences
                        for (int k = 0; k < rangeOfSentenceIds.size(); k++) {
                            String sentenceId = rangeOfSentenceIds.get(k);
                            if (refSet.containsTargetSentenceId(sentenceId)) {
                                for (int l = 0; l < refSet.getTargets().size(); l++) {
                                    CorefTargetAgata corefTargetAgata = refSet.getTargets().get(l);
                                    if (eventTarget.getDocId().equals(corefTargetAgata.getDocId())) {
                                        String predicate = "semHasTime";
                                        String subject = key+"/e"+ coRefSetAgata.getId();
                                        String object =  key+"/t"+ refSet.getId();
                                        Triple triple = new Triple(predicate, subject, object);
                                        String target = "\t\t<target id =\""+ eventTarget.getDocId()+"/"+eventTarget.getSentenceId()+"\""+"/>";
                                        target += " <!-- "+eventTarget.getWord()+"--"+predicate+"--"+ corefTargetAgata.getWord()+" -->";
                                        int givenTriple = getTriple(triplesA, triple);
                                        if (givenTriple==-1) {
                                            relationCounter++;
                                            String id = key+"/"+relationCounter;
                                            triple.setId(id);
                                            triple.addMentions(target);
                                            triplesA.add(triple);
                                        }
                                        else {
                                            if (!triplesA.get(givenTriple).getMentions().contains(target)) {
                                                triplesA.get(givenTriple).addMentions(target);
                                            }
                                        }
                                    }
                                    else {
                                        //  System.out.println("corefTarget.getDocId() = " + corefTarget.getDocId());
                                        //  System.out.println("eventTarget.getDocId() = " + eventTarget.getDocId());
                                    }
                                }
                            }
                        }
                    }
                }
                if (locationSets!=null) {
                   // System.out.println("PLACES");
                    for (int s = 0; s < locationSets.size(); s++) {
                        CoRefSetAgata refSet = locationSets.get(s);
                        //// loop to add results for range of sentences
                        for (int k = 0; k < rangeOfSentenceIds.size(); k++) {
                            String sentenceId = rangeOfSentenceIds.get(k);
                            if (refSet.containsTargetSentenceId(sentenceId)) {
                                for (int l = 0; l < refSet.getTargets().size(); l++) {
                                    CorefTargetAgata corefTargetAgata = refSet.getTargets().get(l);
                                    if (eventTarget.getDocId().equals(corefTargetAgata.getDocId())) {
                                        String predicate = "semHasPlace";
                                        String subject = key+"/e"+ coRefSetAgata.getId();
                                        String object =  key+"/p"+ refSet.getId();
                                        Triple triple = new Triple(predicate, subject, object);
                                        String target = "\t\t<target id =\""+ eventTarget.getDocId()+"/"+eventTarget.getSentenceId()+"\""+"/>";
                                        target += " <!-- "+eventTarget.getWord()+"--"+predicate+"--"+ corefTargetAgata.getWord()+" -->";
                                        int givenTriple = getTriple(triplesA, triple);
                                        if (givenTriple==-1) {
                                            relationCounter++;
                                            String id = key+"/"+relationCounter;
                                            triple.setId(id);
                                            triple.addMentions(target);
                                            triplesA.add(triple);
                                        }
                                        else {
                                            if (!triplesA.get(givenTriple).getMentions().contains(target)) {
                                                triplesA.get(givenTriple).addMentions(target);
                                            }
                                        }
                                    }
                                    else {
                                        //  System.out.println("corefTarget.getDocId() = " + corefTarget.getDocId());
                                        //  System.out.println("eventTarget.getDocId() = " + eventTarget.getDocId());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (int k = 0; k < triplesA.size(); k++) {
            Triple triple = triplesA.get(k);
            str = triple.toString();
            fos.write(str.getBytes());
        }
        for (int k = 0; k < triplesP.size(); k++) {
            Triple triple = triplesP.get(k);
            str = triple.toString();
            fos.write(str.getBytes());
        }
        for (int k = 0; k < triplesT.size(); k++) {
            Triple triple = triplesT.get(k);
            str = triple.toString();
            fos.write(str.getBytes());

        }


    }

    static int getTriple (ArrayList<Triple> triples, Triple triple) {
        for (int i = 0; i < triples.size(); i++) {
            Triple triple1 = triples.get(i);
/*
            System.out.println("triple1.toString() = " + triple1.toString());
            System.out.println("triple.toString() = " + triple.toString());
*/
/*
            if (triple.getSubject().equals("TOPIC_44_EVENT_COREFERENCE_CORPUS/e35")) {
                System.out.println(triple.getObject()+":"+triple1.getObject());
            }
*/
            if ((triple1.getPredicate().equalsIgnoreCase(triple.getPredicate())) &&
                (triple1.getSubject().equalsIgnoreCase(triple.getSubject())) &&
                (triple1.getObject().equalsIgnoreCase(triple.getObject()))
                    ) {
                return i;
            }
        }
        return -1;
    }
    /**
     *
     * @param corefMap
     * @param sentenceIdString
     * @param sentenceRange
     * @return
     */
    static ArrayList<CoRefSetAgata> getCorefSetFromRange (HashMap<String, ArrayList<CoRefSetAgata>> corefMap, String sentenceIdString, int sentenceRange) {
        ArrayList<CoRefSetAgata> coRefSetAgatas = null;
        coRefSetAgatas = corefMap.get(sentenceIdString);
        if (sentenceRange>0) {
            /// we assume that the sentence id is an integer
            Integer sentenceId = Integer.parseInt(sentenceIdString);
            if (sentenceId!=null) {
                for (int i = sentenceId; i < sentenceRange; i++) {
                    ArrayList<CoRefSetAgata> nextSet = corefMap.get(sentenceId+i);
                    if (nextSet!=null) {
                        for (int j = 0; j < nextSet.size(); j++) {
                            CoRefSetAgata coRefSetAgata = nextSet.get(j);
                            coRefSetAgatas.add(coRefSetAgata);
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
        return coRefSetAgatas;
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
