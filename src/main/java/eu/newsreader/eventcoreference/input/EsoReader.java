package eu.newsreader.eventcoreference.input;

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
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 10/05/15.
 */
public class EsoReader extends DefaultHandler {
    String value = "";
    String subClass = "";
    String superClass = "";
    public HashMap<String, String> subToSuper = new HashMap<String, String>();
    public HashMap<String, ArrayList<String>> superToSub = new HashMap<String, ArrayList<String>>();

    static public void main (String[] args) {
        String esoPath = "";
      //  esoPath = "/Users/piek/Desktop/NWR/NWR-ontology/version-0.6/ESO_version_0.6.owl";
        esoPath = "/Users/piek/Desktop/ESO_extended_June17.owl";
        EsoReader esoReader = new EsoReader();
        esoReader.parseFile(esoPath);
        ArrayList<String> tops = esoReader.getTops();
        System.out.println("tops.toString() = " + tops.toString());
        esoReader.printTree(tops, 0);
    }


    public EsoReader () {
        init();
    }

    public ArrayList<String> getTops () {
        ArrayList<String> tops = new ArrayList<String>();
        Set keySet = superToSub.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!subToSuper.containsKey(key)) {
                if (!tops.contains(key)) tops.add(key);
            }
        }
        return tops;
    }



    public void getParentChain (String c, ArrayList<String> parents) {
        if (subToSuper.containsKey(c)) {
            String p = subToSuper.get(c);
            if (!parents.contains(p)) {
                parents.add(p);
                getParentChain(p, parents);
            }
        }
    }


    public void getDescendants (String c, ArrayList<String> decendants) {
        if (superToSub.containsKey(c)) {
            ArrayList<String> subs = superToSub.get(c);
            for (int i = 0; i < subs.size(); i++) {
                String sub = subs.get(i);
                if (!decendants.contains(sub)) {
                    decendants.add(sub);
                    getDescendants(sub, decendants);
                }
            }
        }
    }

    public void printTree (ArrayList<String> tops, int level) {
        level++;
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            String str = "";
            for (int j = 0; j < level; j++) {
                str += "  ";

            }
            if (superToSub.containsKey(top)) {
                ArrayList<String> children = superToSub.get(top);
                str += top + ":" + children.size();
                System.out.println(str);
                printTree(children, level);
            }
            else {
                str += top;
                System.out.println(str);
            }
        }
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
        subToSuper = new HashMap<String,String>();
        superToSub = new HashMap<String, ArrayList<String>>();
    }

    /**
     * <owl:Class rdf:about="http://www.newsreader-project.eu/domain-ontology#Arriving">
     <rdfs:label xml:lang="en">Arriving</rdfs:label>
     <rdfs:subClassOf rdf:resource="http://www.newsreader-project.eu/domain-ontology#Translocation"/>
     <correspondToSUMOClass>http://www.ontologyportal.org/SUMO.owl#Arriving</correspondToSUMOClass>
     <correspondToFrameNetFrame>http://www.newsreader-project.eu/framenet#Vehicle_landing</correspondToFrameNetFrame>
     <correspondToFrameNetFrame>http://www.newsreader-project.eu/framenet#Arriving</correspondToFrameNetFrame>
     <rdfs:comment xml:lang="en">the subclass of Translocation where someone or something arrives at a location.</rdfs:comment>
     </owl:Class>
     */


    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {

        if (qName.equalsIgnoreCase("owl:Class")) {
            subClass = "";
            superClass = "";
            for (int i = 0; i < attributes.getLength(); i++) {
                String name = attributes.getQName(i);
                if (name.equalsIgnoreCase("rdf:about")) {
                    subClass = attributes.getValue(i).trim();
                    int idx = subClass.lastIndexOf("#");
                    if (idx>-1) {
                        subClass = subClass.substring(idx+1);
                    }
                }
            }
        }
        if (qName.equalsIgnoreCase("rdfs:subClassOf")) {
            superClass = "";
            for (int i = 0; i < attributes.getLength(); i++) {
                String name = attributes.getQName(i);
                if (name.equalsIgnoreCase("rdf:resource")) {
                    superClass = attributes.getValue(i).trim();
                    int idx = superClass.lastIndexOf("#");
                    if (idx>-1) {
                        superClass = superClass.substring(idx+1);
                    }
                    subToSuper.put(subClass, superClass);
                    if (superToSub.containsKey(superClass)) {
                        ArrayList<String> subs = superToSub.get(superClass);
                        if (!subs.contains(subClass)) {
                            subs.add(subClass);
                            superToSub.put(superClass, subs);
                        }
                    }
                    else {
                        ArrayList<String> subs = new ArrayList<String>();
                        subs.add(subClass);
                        superToSub.put(superClass, subs);
                    }
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
