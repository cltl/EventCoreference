package eu.newsreader.eventcoreference.input;

import eu.newsreader.eventcoreference.objects.CorefTarget;
import eu.newsreader.eventcoreference.objects.EventMention;
import eu.newsreader.eventcoreference.util.Anaphor;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 3/28/12
 * Time: 4:04 PM
 * To change this template use File | Settings | File Templates.
 *
 * This program reads a SemEvent file and creates new coreference sets for event mentions on the basis of the overlap of event components
 */
public class SemDomParser {
    static String language = "en";
    static int threshold = 0;
    static String method = "component-factor";
    static final String USAGE = "This program reads a SemEvent file and extracts new coreference sets for events in the basis of the contribution of the event components." +
            "The SemEvent file gives lists of coreference sets of events, with for each event the overlap in participants, time and location.\n" +
            "Usage\n:" +
            "--sem-file <path to the sem-file>\n" +
            "--threshold <score above 100 above which the coref sets are maintained>\n" +
            "--method <way that the components contribute to the final score>\n" +
            "--language <nl, en>\n";

    static public void main (String [] args) {
        try {
            String pathToSemFile = "";
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.equalsIgnoreCase("--sem-file") && (i+1)<args.length) {
                    pathToSemFile = args[i+1];
                }
                else if (arg.equalsIgnoreCase("--threshold") && (i+1)<args.length) {
                    try {
                        threshold = Integer.parseInt(args[i+1]);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                else if (arg.equalsIgnoreCase("--method") && (i+1)<args.length) {
                    method = args[i+1];
                }
                else if (arg.equalsIgnoreCase("--language") && (i+1)<args.length) {
                    language = args[i+1];
                }
            }

            if (!new File(pathToSemFile).exists()) {
                System.out.println("Cannot find path to sem-file = " + pathToSemFile);
                System.out.println(USAGE);
            }


            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pathToSemFile);
            doc.getDocumentElement().normalize();
            int idx = pathToSemFile.lastIndexOf(".xml");
            String outputPath = pathToSemFile+method+"-"+threshold+".corefs.xml";
            if (idx>-1) {
                outputPath = pathToSemFile.substring(0, idx+1)+method+"-"+threshold+".corefs.xml";
            }
            FileOutputStream fos = new FileOutputStream(outputPath);
            String str = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<COREF method=\"combined\" threshold=\"50\">\n";
            fos.write(str.getBytes());
            Node sem = getSubNode(doc, "SEM");
            processDoc(sem, fos);
            str = "\"</COREF>\n";
            fos.write(str.getBytes());
            fos.close();

/*
            // Use a Transformer for output
            TransformerFactory tFactory =
                    TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(pathToSemFile+".result.xml");
            transformer.transform(source, result);
*/
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SAXException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

/*
        catch (TransformerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
*/
    }


    /*
    <file name="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" eventCount="23" participantCount="26" timeCount="3" locationCount="12">
  <semEvent id="e30" lcs="raid" score="2.4849066497880004" synset="eng-30-02020027-v" label="raid" mentions="2">
	<mentions>
	<event-mention>
		<event>
			<target termId="t285" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.0" synset="eng-30-02020027-v" rank="0.257681" word="raid"/>
		<event>
		<participants>
			<participant id="p30" lcs="eng-30-00007846-n" score="2.639057329615259" synset="eng-30-10210137-n" label="rebel" mentions="26">
					<target termId="t288" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.5596157879354228" synset="eng-30-11346710-n" rank="0.227748" word="town"/>
			</participant>
			<participant id="p93" lcs="" score="0.0" synset="" label="Khalanga" mentions="1">
					<target termId="t2810" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.0" synset="" rank="0.0" word="Khalanga"/>
			</participant>
			<participant id="p34" lcs="eng-30-08008335-n" score="2.639057329615259" synset="eng-30-08209687-n" label="police" mentions="16">
					<target termId="t2827" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.5596157879354228" synset="eng-30-08337324-n" rank="0.143377" word="office"/>
					<target termId="t2830" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.5596157879354228" synset="eng-30-08051946-n" rank="0.0895559" word="court"/>
			</participant>
		</participants>
		<times>
			<time id="e3" lcs="eng-30-15163157-n" score="2.890371757896165" synset="eng-30-15163979-n" label="Monday" mentions="9">
					<target termId="t284" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.0" synset="eng-30-15164570-n" rank="1.0" word="Saturday"/>
			</time>
		</times>
		<locations>
		</locations>
	</event-mention>
	<event-mention>
		<event>
			<target termId="t3119" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="31" corefScore="2.4849066497880004" synset="eng-30-00976953-n" rank="0.512844" word="raid"/>
		<event>
		<participants>
			<participant id="p30" lcs="eng-30-00007846-n" score="2.639057329615259" synset="eng-30-10210137-n" label="rebel" mentions="26">
					<target termId="t3115" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="31" corefScore="0.5596157879354228" synset="eng-30-09924742-n" rank="1.0" word="civilian"/>
			</participant>
			<participant id="p34" lcs="eng-30-08008335-n" score="2.639057329615259" synset="eng-30-08209687-n" label="police" mentions="16">
					<target termId="t3121" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="31" corefScore="2.639057329615259" synset="eng-30-08209687-n" rank="1.0" word="police"/>
			</participant>
			<participant id="p47" lcs="soldier" score="2.639057329615259" synset="eng-30-10622053-n" label="soldier" mentions="4">
					<target termId="t3112mw" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="31" corefScore="0.5596157879354228" synset="eng-30-09925459-n" rank="1.0" word="civil servant"/>
			</participant>
		</participants>
		<times>
			<time id="e11" lcs="" score="0.0" synset="eng-30-06824227-n" label="capital" mentions="1">
					<target termId="t315" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="31" corefScore="0.0"
					synset="eng-30-06824227-n" rank="0.132532" word="capital"/>
			</time>
		</times>
		<locations>
		</locations>
	</event-mention>
	</mentions>

     */


    /**
     *
     * @param eventMention
     * @param componentName
     * @return ArrayList<String>
     *
     */

    static ArrayList<String> getComponentIdsForEventMention(Node eventMention, String componentName) {
        ArrayList<String> ids = new ArrayList<String>();
        Node participantsNode = getSubNode(eventMention, componentName);
        if (participantsNode!=null) {
          //  System.out.println("participantsNode = " + participantsNode.getNodeName());
            NodeList participants = participantsNode.getChildNodes();
            if (participants!=null) {
                for (int i = 0; i < participants.getLength(); i++) {
                    Node p =  (Node) participants.item(i);
                 //   System.out.println("p.getNodeName() = " + p.getNodeName());
                    if (p.hasAttributes()) {
                        NamedNodeMap attributes = p.getAttributes();
                        boolean anaphor = false;
                        Node labelNode = attributes.getNamedItem("label");
                        if (labelNode!=null) {
                            anaphor = Anaphor.anaphor(labelNode.getNodeValue(), language);
/*
                            if (anaphor) {
                                ids.add("*");
                            }
*/
                        }
                        //// we ignore all anaphors, alternatively we can add them....
                        if (!anaphor) {
                            Node id = attributes.getNamedItem("id");
                          //  System.out.println("id.getNodeName() = " + id.getNodeName());
                          //  System.out.println("id.getNodeValue() = " + id.getNodeValue());
                            if (id!=null) {
                                ids.add(id.getNodeValue());
                            }
                        }
                        else {
                            /// anaphors are ignored for creating the matches.
                        }
                    }
                }
            }
        }
        else {
            //System.out.println("participantsNode = " + participantsNode.getNodeName());
            //System.out.println("cannot find componentName = " + componentName);
        }
      //  System.out.println("ids.toString() = " + ids.toString());
        return ids;
    }

    static CorefTarget getEventMention(Node eventMention) {
        CorefTarget event = new CorefTarget();
        Node eventNode = getSubNode(eventMention, "event");
        if (eventNode!=null) {
            Node targetNode = getSubNode(eventNode, "target");
            if (targetNode!=null) {
                NamedNodeMap attributes = targetNode.getAttributes();
                Node att = attributes.getNamedItem("termId");
                if (att!=null) {
                    event.setTermId(att.getNodeValue());
                }
                att = attributes.getNamedItem("docId");
                if (att!=null) {
                    event.setDocId(att.getNodeValue());
                }
                att = attributes.getNamedItem("sentenceId");
                if (att!=null) {
                    event.setSentenceId(att.getNodeValue());
                }
                att = attributes.getNamedItem("corefScore");
                if (att!=null) {
                    try {
                        String intString = att.getNodeValue();
                        double n = Double.parseDouble(intString);
                        event.setCorefScore(n);
                    } catch (DOMException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (NumberFormatException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                att = attributes.getNamedItem("synset");
                if (att!=null) {
                    event.setSynset(att.getNodeValue());
                }
                att = attributes.getNamedItem("rank");
                if (att!=null) {
                    try {
                        String intString = att.getNodeValue();
                        double n = Double.parseDouble(intString);
                        event.setSynsetScore(n);
                    } catch (DOMException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (NumberFormatException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                att = attributes.getNamedItem("word");
                if (att!=null) {
                    event.setWord(att.getNodeValue());
                }
            }
        }
        return event;
    }

    static int getNumberOfMentions (NodeList mentions) {
        int n = 0;
        for (int i = 0; i < mentions.getLength(); i++) {
            Node mentionNode = (Node) mentions.item(i);
            if (mentionNode!=null) {
                CorefTarget eventMentionTarget = getEventMention(mentionNode);
                if (!eventMentionTarget.getTermId().isEmpty()) {
                   n++;
                }
            }
        }
        return n;
    }

    static String processMentions (Node semEvent) {
        String str = "";
        Node mentionsNode = getSubNode(semEvent, "mentions");
        if (mentionsNode!=null) {
            NodeList mentions = mentionsNode.getChildNodes();
            if (mentions!=null) {
                int nMentions = getNumberOfMentions(mentions);
                ArrayList<EventMention> eventMentionBelowThresholdArrayList = new ArrayList<EventMention>();
                ArrayList<EventMention> eventMentionOriginalSingletonArrayList = new ArrayList<EventMention>();
                ArrayList<EventMention> eventMentionArrayList = new ArrayList<EventMention>();
                for (int i = 0; i < mentions.getLength(); i++) {
                    Node mentionNode = (Node) mentions.item(i);
                    if (mentionNode!=null) {
                        CorefTarget eventMentionTarget = getEventMention(mentionNode);
                        if (!eventMentionTarget.getTermId().isEmpty()) {
                            EventMention eventMention = new EventMention();
                            eventMention.setEvent(eventMentionTarget);
                            //check the participants
                            double matches = 0;
                            ArrayList<String> idsForEventmentionds = getComponentIdsForEventMention(mentionNode, "participants");
                            eventMention.setnP(idsForEventmentionds.size());
                            if (idsForEventmentionds.size()>0) {
                                //// we check all the other event mentions
                                for (int j = 0; j < mentions.getLength(); j++) {
                                    if (j!=i) {
                                        //// it is not the same
                                        Node oMention = (Node) mentions.item(j);
                                        ArrayList<String> opIds = getComponentIdsForEventMention(oMention, "participants");
                                        for (int k = 0; k < opIds.size(); k++) {
                                            String opId = opIds.get(k);
                                            if (idsForEventmentionds.contains(opId)) {
                                                //// we count the number of matching participants
                                               matches++;
                                            }
                                        }
                                    }
                                }
                                /// we calculate the boost factor of the mention on the basis of the number of matches divided by the maximum of possible matches
                                int max = (nMentions-1)*idsForEventmentionds.size();
                                double score = matches/max;
                                /// the value is stored for the mention as the pScore.
                                eventMention.setpScore(score);
                            }
                            else {
                                /// there is only one mentions so there is nothing we can do
                            }

                            // check the times
                            matches = 0;
                            idsForEventmentionds = getComponentIdsForEventMention(mentionNode, "times");
                            eventMention.setnT(idsForEventmentionds.size());
                            if (idsForEventmentionds.size()>0) {
                                for (int j = 0; j < mentions.getLength(); j++) {
                                    if (j!=i) {
                                        Node oMention = (Node) mentions.item(j);
                                        ArrayList<String> opIds = getComponentIdsForEventMention(oMention, "times");
                                        for (int k = 0; k < opIds.size(); k++) {
                                            String opId = opIds.get(k);
                                            if (idsForEventmentionds.contains(opId)) {
                                               matches++;
                                            }
                                        }
                                    }
                                }
                                int max = (nMentions-1)*idsForEventmentionds.size();
                                double score = matches/max;
                                eventMention.settScore(score);
                            }

                            // check the locations
                            matches = 0;
                            idsForEventmentionds = getComponentIdsForEventMention(mentionNode, "locations");
                            eventMention.setnL(idsForEventmentionds.size());
                            if (idsForEventmentionds.size()>0) {
                                for (int j = 0; j < mentions.getLength(); j++) {
                                    if (j!=i) {
                                        Node oMention = (Node) mentions.item(j);
                                        ArrayList<String> opIds = getComponentIdsForEventMention(oMention, "locations");
                                        for (int k = 0; k < opIds.size(); k++) {
                                            String opId = opIds.get(k);
                                            if (idsForEventmentionds.contains(opId)) {
                                               matches++;
                                            }
                                        }
                                    }
                                }
                                int max = (nMentions-1)*idsForEventmentionds.size();
                                double score = matches/max;
                                eventMention.setlScore(score);
                            }
                            eventMention.scoreEventMention(method);
                            if (nMentions==1){
                                ///singleton set
                              //  System.out.println("singleton eventMention = " + eventMention.toString());
                                eventMentionOriginalSingletonArrayList.add(eventMention);
                            }
                            else if (eventMention.getIntScore()>=threshold) {
                                eventMentionArrayList.add(eventMention);
                            }
                            else {
                                //// mention in a multiform set that scores below the threshold
                              //  System.out.println("below threshold eventMention = " + eventMention.toString());
                                eventMentionBelowThresholdArrayList.add(eventMention);
                            }
                        }
                    }
                }
                if (eventMentionArrayList.size()>0) {
                    str = "\t<co-refs>\n";
                    for (int i = 0; i < eventMentionArrayList.size(); i++) {
                        EventMention eventMention = eventMentionArrayList.get(i);
                        str += eventMention.toString();
                    }
                    str += "\t</co-refs>\n";
                }
                if (eventMentionOriginalSingletonArrayList.size()>0) {
                    str = "\t<co-refs> <!-- original singleton -->\n";
                    for (int i = 0; i < eventMentionOriginalSingletonArrayList.size(); i++) {
                        EventMention eventMention = eventMentionOriginalSingletonArrayList.get(i);
                        str += eventMention.toString();
                    }
                    str += "\t</co-refs>\n";
                }
                if (eventMentionBelowThresholdArrayList.size()>0) {
                    for (int i = 0; i < eventMentionBelowThresholdArrayList.size(); i++) {
                        EventMention eventMention = eventMentionBelowThresholdArrayList.get(i);
                        str += "\t<co-refs> <!-- below threshold -->\n";
                        str += eventMention.toString();
                        str += "\t</co-refs>\n";
                    }
                }

            }
        }
        return str;
    }


    static void processDoc (Node sem, FileOutputStream fos) throws IOException {
        String str = "";
        NodeList list = sem.getChildNodes();
        //// all the results per document;
        for (int i = 0; i < list.getLength(); i++) {
            Node node = (Node) list.item(i);
           // System.out.println("node.getNodeName() = " + node.getNodeName());
            if (!node.hasAttributes()) {
                continue;
            }
            NamedNodeMap attributes = node.getAttributes();
            Node fileNode = attributes.getNamedItem("name");
            if (fileNode!=null) {
                // <file name="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" eventCount="23" participantCount="26" timeCount="3" locationCount="12">
                String fileName = fileNode.getNodeValue();
                str = "<co-ref-sets file=\""+fileName+"\">\n";
                fos.write(str.getBytes());
                /// we iterate over all SemEvents to see if they need to be split.
                NodeList semEvents = node.getChildNodes();
                for (int j = 0; j < semEvents.getLength(); j++) {
                    Node semEvent =  (Node) semEvents.item(j);
                    if (!semEvent.hasAttributes()) {
                        continue;
                    }
                    NamedNodeMap semEventAttributes = semEvent.getAttributes();
                    // <semEvent id="e30" lcs="raid" score="2.4849066497880004" synset="eng-30-02020027-v" label="raid" mentions="2">
                    Node mentions = semEventAttributes.getNamedItem("mentions");
                    if (mentions!=null) {
                        int mentionsInt = Integer.parseInt(mentions.getNodeValue());
                        //// CHANGE NEXT TEST TO EXLCUDE SINGLETONS
                        if (mentionsInt>0) {
                            ////we have more than one event mention. We need to decide whether to split or to keep
                            /// we first check for incompatible times and locations
    
                            /// if there are still events with multiple mentions, we try to boost the score on the basis of overlap according to the method
                            ////
                            str = processMentions(semEvent);
                            fos.write(str.getBytes());
                            /// finally we split all below the threshold
                        }
                        else {
                           /// ignore events with one mention for the time being
                        }
                    }
                }
                str = "</co-ref-sets>\n";
                fos.write(str.getBytes());
            }
        }

    }
    
    static String getSingleEventCoref (Node node) {
        /*
        <semEvent id="e30" lcs="raid" score="2.4849066497880004" synset="eng-30-02020027-v" label="raid" mentions="2">
<mentions>
<event-mention>
<event>
<target termId="t285" docId="AFP_ENG_20040823.0382.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="28" corefScore="0.0" synset="eng-30-02020027-v" rank="0.257681" word="raid"/>
<event>
        */
        String str ="";
        return str;
    }
    /*
    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<COREF method="leacock-chodorow" threshold="50">
<co-ref-sets file="AFP_ENG_20050715.0342.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf">
	<co-refs id="e19" lcs="" score="0.0">
		<target termId="t718" docId="AFP_ENG_20050715.0342.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="" corefScore="0.0" synset="eng-30-03525252-n" rank="0.122392" word="hold"/>
	</co-refs>
	<co-refs id="e18" lcs="" score="0.0">
		<target termId="t67" docId="AFP_ENG_20050715.0342.src.xml.txt.blk.tok.stp.tbf.xml.isi-term.ont.kaf" sentenceId="" corefScore="0.0" synset="eng-30-00918872-v" rank="0.134402" word="determine"/>
	</co-refs>
     */

    static public Node getSubNode (Node node, String name) {
        NodeList children = node.getChildNodes();
        for (int i=0;i<children.getLength();i++) {
            Node child = children.item(i);
            if (child.getNodeName().equalsIgnoreCase(name)) {
              //  System.out.println("child.getNodeName() = " + child.getNodeName());
                return child;
            }
            else {
                Node subnode = getSubNode (child, name);
                if (subnode!=null) {
                    return subnode;
                }
            }
        }
        return null;
    }
}
