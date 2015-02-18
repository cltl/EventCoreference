package eu.newsreader.eventcoreference.input;

import eu.kyotoproject.kaf.KafSense;
import eu.newsreader.eventcoreference.objects.SemObject;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by piek on 2/18/15.
 */
public class FrameNetReader extends DefaultHandler {


    public HashMap<String, ArrayList<String>> subToSuperFrame = new HashMap<String, ArrayList<String>>();
    public HashMap<String, ArrayList<String>> superToSubFrame = new HashMap<String, ArrayList<String>>();
    String subFrame = "";
    String superFrame = "";
    String value = "";

    public FrameNetReader () {
        init();
    }

    public boolean frameNetConnected (SemObject event1, SemObject event2) {
        for (int i = 0; i < event1.getConcepts().size(); i++) {
            KafSense kafSense1 = event1.getConcepts().get(i);
            if (kafSense1.getResource().equalsIgnoreCase("framenet")) {
                for (int j = 0; j < event2.getConcepts().size(); j++) {
                    KafSense kafSense2 =  event2.getConcepts().get(j);
                    if (kafSense2.getResource().equalsIgnoreCase("framenet")) {
                        if (kafSense1.getSensecode().equals(kafSense2.getSensecode())) {
                            return true;
                        }
                        else {
                            if (subToSuperFrame.containsKey(kafSense1.getSensecode())) {
                                ArrayList<String> superFrames = subToSuperFrame.get(kafSense1.getSensecode());
                                if (superFrames.contains(kafSense2.getSensecode())) {
                                    return true;
                                }
                                for (int k = 0; k < superFrames.size(); k++) {
                                    String frame = superFrames.get(k);
                                    if (superToSubFrame.containsKey(frame)) {
                                        ArrayList<String> subFrames = superToSubFrame.get(frame);
                                        if (subFrames.contains(kafSense2.getSensecode())) {
                                            return true;
                                        }
                                    }
                                }
                            }
                            if (superToSubFrame.containsKey(kafSense1.getSensecode())) {
                                ArrayList<String> subFrames = superToSubFrame.get(kafSense1.getSensecode());
                                if (subFrames.contains(kafSense2.getSensecode())) {
                                    return true;
                                }
                            }

                        }
                    }
                }

            }
        }
        return false;
    }


    public void parseFile(String filePath) {
    String myerror = "";
    try {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();
        InputSource inp = new InputSource (new FileReader(filePath));
        parser.parse(inp, this);
    } catch (SAXParseException err) {
        myerror = "\n** Parsing error" + ", line " + err.getLineNumber()
                + ", uri " + err.getSystemId();
        myerror += "\n" + err.getMessage();
        System.out.println("myerror = " + myerror);
    } catch (SAXException e) {
        Exception x = e;
        if (e.getException() != null)
            x = e.getException();
        myerror += "\nSAXException --" + x.getMessage();
        System.out.println("myerror = " + myerror);
    } catch (Exception eee) {
        eee.printStackTrace();
        myerror += "\nException --" + eee.getMessage();
        System.out.println("myerror = " + myerror);
    }
}//--c

    public void init () {
        subToSuperFrame = new HashMap<String, ArrayList<String>>();
        superToSubFrame = new HashMap<String, ArrayList<String>>();
        subFrame = "";
        superFrame = "";
    }

    //    <frameRelation subID="171" supID="82" subFrameName="Commerce_buy" superFrameName="Commerce_scenario" ID="360">

    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {

        if (qName.equalsIgnoreCase("frameRelation")) {
            subFrame = "";
            superFrame = "";
            for (int i = 0; i < attributes.getLength(); i++) {
                String name = attributes.getQName(i);
                if (name.equalsIgnoreCase("subFrameName")) {
                    subFrame = attributes.getValue(i).trim();
                }
                else if (name.equalsIgnoreCase("superFrameName")) {
                    superFrame = attributes.getValue(i).trim();
                }
            }
            if (!subFrame.isEmpty() && !superFrame.isEmpty()) {
                if (subToSuperFrame.containsKey(subFrame)) {
                    ArrayList<String> frames = subToSuperFrame.get(subFrame);
                    frames.add(superFrame);
                    subToSuperFrame.put(subFrame, frames);
                }
                else {
                    ArrayList<String> frames = new ArrayList<String>();
                    frames.add(superFrame);
                    subToSuperFrame.put(subFrame, frames);
                }
                if (superToSubFrame.containsKey(superFrame)) {
                    ArrayList<String> frames = superToSubFrame.get(superFrame);
                    frames.add(subFrame);
                    superToSubFrame.put(superFrame, frames);
                }
                else {
                    ArrayList<String> frames = new ArrayList<String>();
                    frames.add(subFrame);
                    superToSubFrame.put(superFrame, frames);
                }
            }
        }

        value = "";
    }//--startElement

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
    }

    public void characters(char ch[], int start, int length)
            throws SAXException {
        value += new String(ch, start, length);
        // System.out.println("tagValue:"+value);
    }
}
