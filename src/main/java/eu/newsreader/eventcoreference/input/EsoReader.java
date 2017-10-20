package eu.newsreader.eventcoreference.input;

import eu.newsreader.eventcoreference.output.SimpleTaxonomy;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 10/05/15.
 */
public class EsoReader extends DefaultHandler {
    String value = "";
    String subClass = "";
    String superClass = "";
    public SimpleTaxonomy simpleTaxonomy;
/*
    public HashMap<String, String> subToSuper = new HashMap<String, String>();
    public HashMap<String, ArrayList<String>> superToSub = new HashMap<String, ArrayList<String>>();
*/

    static public void main (String[] args) {
        String esoPath = "";
      //  esoPath = "/Users/piek/Desktop/NWR/NWR-ontology/version-0.6/ESO_version_0.6.owl";
       // esoPath = "/Users/piek/Desktop/ESO_extended_June17.owl";
        //esoPath = "/Users/piek/Desktop/NWR/eso/ESO.v2/ESO_V2_Final.owl";
        esoPath = "/Code/vu/eso-and-ceo/CEO_version_07.owl";
        EsoReader esoReader = new EsoReader();
        esoReader.parseFile(esoPath);
/*
        ArrayList<String> tops = esoReader.getTops();
        System.out.println("tops.toString() = " + tops.toString());
        esoReader.printTree(tops, 0);
*/
        try {
            OutputStream fos = new FileOutputStream(esoPath+".wn-lmf");
            esoReader.writeToWnLmFRelation(fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        esoReader.simpleTaxonomy.printTree();
    }

    /**
     * <Synset id="eng-30-00995103-v">
     <SynsetRelation relType="result" target="eng-30-06349597-n"/>
     </Synset>
     */
    public void writeToWnLmFRelation (OutputStream fos) throws IOException {
        Set keySet = simpleTaxonomy.subToSuper.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            String superKey = simpleTaxonomy.subToSuper.get(key);
            String str = "<Synset id=\""+key+"\">\n";
            str += "<SynsetRelation relType=\"has_hyperonym\" target=\""+superKey+"\"/>\n";
            str += "</Synset>\n";
            fos.write(str.getBytes());
        }
    }

    public EsoReader () {
        init();
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
        simpleTaxonomy = new SimpleTaxonomy();
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
                    subClass = "eso:"+attributes.getValue(i).trim();
                    int idx = subClass.lastIndexOf("#");
                    if (idx>-1) {
                        subClass = "eso:"+subClass.substring(idx+1);
                    }
                }
            }
        }
        else if (qName.equalsIgnoreCase("rdfs:subClassOf")) {
            superClass = "";
            for (int i = 0; i < attributes.getLength(); i++) {
                String name = attributes.getQName(i);
                if (name.equalsIgnoreCase("rdf:resource")) {
                    superClass = "eso:"+attributes.getValue(i).trim();
                    int idx = superClass.lastIndexOf("#");
                    if (idx>-1) {
                        superClass = "eso:"+superClass.substring(idx+1);
                    }
                    simpleTaxonomy.subToSuper.put(subClass, superClass);
                    if (simpleTaxonomy.superToSub.containsKey(superClass)) {
                        ArrayList<String> subs = simpleTaxonomy.superToSub.get(superClass);
                        if (!subs.contains(subClass)) {
                            subs.add(subClass);
                            simpleTaxonomy.superToSub.put(superClass, subs);
                        }
                    }
                    else {
                        ArrayList<String> subs = new ArrayList<String>();
                        subs.add(subClass);
                        simpleTaxonomy.superToSub.put(superClass, subs);
                    }
                }
            }
        }

        value = "";
    }//--startElement

    public void endElement(String uri, String localName, String qName)
            throws SAXException {

              /*
            <owl:Class rdf:about="http://www.newsreader-project.eu/domain-ontology#Arriving">
        <rdfs:label xml:lang="en">Arriving</rdfs:label>
        <rdfs:subClassOf rdf:resource="http://www.newsreader-project.eu/domain-ontology#Translocation"/>
        <correspondToFrameNetFrame_closeMatch>http://www.newsreader-project.eu/framenet#Arriving</correspondToFrameNetFrame_closeMatch>
        <correspondToSUMOClass_closeMatch>http://www.ontologyportal.org/SUMO.owl#Arriving</correspondToSUMOClass_closeMatch>
        <correspondToFrameNetFrame_closeMatch>http://www.newsreader-project.eu/framenet#Vehicle_landing</correspondToFrameNetFrame_closeMatch>
        <rdfs:comment xml:lang="en">The subclass of Translocation where someone or something arrives at a location.</rdfs:comment>
    </owl:Class>
         */
         if (qName.equalsIgnoreCase("correspondToFrameNetFrame_relatedMatch") ||
                qName.equalsIgnoreCase("correspondToFrameNetFrame_closeMatch")
                ) {
             String valueName = "fn:"+value;
             int idx = value.lastIndexOf("#");
             if (idx>-1) {
                 valueName = "fn:"+value.substring(idx+1);
             }
             simpleTaxonomy.subToSuper.put(valueName, subClass);
            if (simpleTaxonomy.superToSub.containsKey(subClass)) {
                ArrayList<String> subs = simpleTaxonomy.superToSub.get(subClass);
                if (!subs.contains(valueName)) {
                    subs.add(valueName);
                    simpleTaxonomy.superToSub.put(subClass, subs);
                }
            }
            else {
                ArrayList<String> subs = new ArrayList<String>();
                subs.add(valueName);
                simpleTaxonomy.superToSub.put(subClass, subs);
            }
        }
        else if (qName.equalsIgnoreCase("correspondToFrameNetFrame_broadMatch")
                ) {
             String valueName = "fn-broad:"+value;
             int idx = value.lastIndexOf("#");
             if (idx>-1) {
                 valueName = "fn-broad:"+value.substring(idx+1);
             }

             simpleTaxonomy.subToSuper.put(valueName, subClass);
             if (simpleTaxonomy.superToSub.containsKey(subClass)) {
                 ArrayList<String> subs = simpleTaxonomy.superToSub.get(subClass);
                 if (!subs.contains(valueName)) {
                     subs.add(valueName);
                     simpleTaxonomy.superToSub.put(subClass, subs);
                 }
             }
             else {
                 ArrayList<String> subs = new ArrayList<String>();
                 subs.add(valueName);
                 simpleTaxonomy.superToSub.put(subClass, subs);
             }
        }
        /*
        else if (qName.equalsIgnoreCase("correspondToFrameNetFrame_broadMatch")
                ) {
             String valueName = "fn:"+value;
             int idx = value.lastIndexOf("#");
             if (idx>-1) {
                 valueName = "fn:"+value.substring(idx+1);
             }
             simpleTaxonomy.subToSuper.put(subClass, valueName);
            if (simpleTaxonomy.superToSub.containsKey(valueName)) {
                ArrayList<String> subs = simpleTaxonomy.superToSub.get(valueName);
                if (!subs.contains(subClass)) {
                    subs.add(valueName);
                    simpleTaxonomy.superToSub.put(valueName, subs);
                }
            }
            else {
                ArrayList<String> subs = new ArrayList<String>();
                subs.add(subClass);
                simpleTaxonomy.superToSub.put(valueName, subs);
            }
        }*/
        else if (
                qName.equalsIgnoreCase("correspondToSUMOClass_relatedMatch")  ||
                qName.equalsIgnoreCase("correspondToSUMOClass_closeMatch")
                ) {
             String valueName = "sumo:"+value;
             int idx = value.lastIndexOf("#");
             if (idx>-1) {
                 valueName = "sumo:"+value.substring(idx+1);
             }
             simpleTaxonomy.subToSuper.put(valueName, subClass);
            if (simpleTaxonomy.superToSub.containsKey(subClass)) {
                ArrayList<String> subs = simpleTaxonomy.superToSub.get(subClass);
                if (!subs.contains(valueName)) {
                    subs.add(valueName);
                    simpleTaxonomy.superToSub.put(subClass, subs);
                }
            }
            else {
                ArrayList<String> subs = new ArrayList<String>();
                subs.add(valueName);
                simpleTaxonomy.superToSub.put(subClass, subs);
            }
        }/*
        else if (
                qName.equalsIgnoreCase("correspondToSUMOClass_broadMatch")
                ) {
             String valueName = "sumo:"+value;
             int idx = value.lastIndexOf("#");
             if (idx>-1) {
                 valueName = "sumo:"+value.substring(idx+1);
             }
             simpleTaxonomy.subToSuper.put(subClass, valueName);
            if (simpleTaxonomy.superToSub.containsKey(valueName)) {
                ArrayList<String> subs = simpleTaxonomy.superToSub.get(valueName);
                if (!subs.contains(subClass)) {
                    subs.add(valueName);
                    simpleTaxonomy.superToSub.put(valueName, subs);
                }
            }
            else {
                ArrayList<String> subs = new ArrayList<String>();
                subs.add(subClass);
                simpleTaxonomy.superToSub.put(valueName, subs);
            }
        }*/
         else if (
                 qName.equalsIgnoreCase("correspondToSUMOClass_broadMatch")
                 ) {
             String valueName = "sumo-broad:"+value;
             int idx = value.lastIndexOf("#");
             if (idx>-1) {
                 valueName = "sumo-broad:"+value.substring(idx+1);
             }

             simpleTaxonomy.subToSuper.put(valueName, subClass);
             if (simpleTaxonomy.superToSub.containsKey(subClass)) {
                 ArrayList<String> subs = simpleTaxonomy.superToSub.get(subClass);
                 if (!subs.contains(valueName)) {
                     subs.add(valueName);
                     simpleTaxonomy.superToSub.put(subClass, subs);
                 }
             }
             else {
                 ArrayList<String> subs = new ArrayList<String>();
                 subs.add(valueName);
                 simpleTaxonomy.superToSub.put(subClass, subs);
             }
         }
    }

    public void characters(char ch[], int start, int length)
            throws SAXException {
        value += new String(ch, start, length);
        // System.out.println("tagValue:"+value);
    }


}
