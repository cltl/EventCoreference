package eu.newsreader.eventcoreference.evaluation;

import eu.kyotoproject.kaf.CorefTarget;
import eu.kyotoproject.kaf.KafCoreferenceSet;
import eu.kyotoproject.kaf.KafTerm;
import eu.kyotoproject.kaf.KafWordForm;
import eu.newsreader.eventcoreference.util.Util;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by piek on 10/23/14.
 */
public class CatCoref extends DefaultHandler {

    /**
     * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
     <co-ref-sets corpus="eecb1.0">
     <co-refs id="eecb1.0/44_52">
     <target termId="t102"/>
     <target termId="t99”/>
     </co-refs>
     <co-refs id="eecb1.0/44_51">
     <target termId="t43"/>
     <target termId="t66" />
     </co-refs>
     </co-ref-sets>

     */
    /*
    <Document doc_name="3806_Apple_unveils_new_Intel-based_Mac.txt">
<token t_id="1" sentence="0" number="0">Apple</token>
<token t_id="2" sentence="0" number="1">unveils</token>
<token t_id="3" sentence="0" number="2">new</token>
<token t_id="4" sentence="0" number="3">Intel-based</token>
<token t_id="5" sentence="0" number="4">Mac</token>

<token t_id="44" sentence="3" number="43">be</token>

<EVENT_MENTION m_id="31" time="NON_FUTURE" special_cases="GEN" aspect="NONE" certainty="POSSIBLE" polarity="POS" tense="INFINITIVE" modality="" pred="be" comment="" pos="VERB"  >
<token_anchor t_id="44"/>
</EVENT_MENTION>
<EVENT_MENTION m_id="69" time="NON_FUTURE" special_cases="NONE" aspect="NONE" certainty="CERTAIN" polarity="POS" tense="PRESENT" modality="" pred="unveil" comment="" pos="VERB"  >
<token_anchor t_id="2"/>
</EVENT_MENTION>

<EVENT m_id="52" RELATED_TO="" TAG_DESCRIPTOR="fast" class="OTHER" comment="" external_ref="" />
<EVENT m_id="53" RELATED_TO="" TAG_DESCRIPTOR="be" class="GRAMMATICAL" comment="" external_ref="" />
<EVENT m_id="54" RELATED_TO="" TAG_DESCRIPTOR="use" class="OTHER" comment="" external_ref="" />

<REFERS_TO r_id="243207" comment="" >
<source m_id="31" />
<target m_id="53" />
</REFERS_TO>

     */
    String value = "";
    ArrayList<KafCoreferenceSet> kafCoreferenceSetArrayList;
    ArrayList<CorefTarget> corefTargetArrayList;
    KafCoreferenceSet kafCoreferenceSet;
    CorefTarget corefTarget;
    HashMap<String, ArrayList<String>> eventTokenMap;
    KafWordForm  kafWordForm;
    ArrayList<KafWordForm> kafWordFormArrayList;
    KafTerm kafTerm;
    ArrayList<KafTerm> kafTermArrayList;
    ArrayList<String> spans;
    String span = "";
    String source = "";
    boolean REFERSTO = false;

    void init() {
        value = "";
        kafCoreferenceSetArrayList = new ArrayList<KafCoreferenceSet>();
        corefTargetArrayList = new ArrayList<CorefTarget>();
        kafCoreferenceSet = new KafCoreferenceSet();
        corefTarget = new CorefTarget();
        eventTokenMap = new HashMap<String, ArrayList<String>>();
        kafWordForm = new KafWordForm();
        kafWordFormArrayList = new ArrayList<KafWordForm>();
        kafCoreferenceSetArrayList = new ArrayList<KafCoreferenceSet>();
        kafTerm = new KafTerm();
        kafTermArrayList = new ArrayList<KafTerm>();
        REFERSTO = false;
    }

    public void parseFile(String filePath) {
        String myerror = "";
        init();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            SAXParser parser = factory.newSAXParser();
            InputSource inp = new InputSource (new FileReader(filePath));
            parser.parse(inp, this);
            switchToTokenIds();
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
        //System.out.println("myerror = " + myerror);
    }//--c



    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {

        if (qName.equalsIgnoreCase("token")) {
            kafWordForm = new KafWordForm();
            kafWordForm.setWid(attributes.getValue("t_id"));
            kafWordForm.setSent(attributes.getValue("sentence"));
        }
        else if (qName.equalsIgnoreCase("EVENT_MENTION")) {
            kafTerm = new KafTerm();
            kafTerm.setType("EVENT");
            kafTerm.setTid(attributes.getValue("m_id"));
        }
        else if (qName.equalsIgnoreCase("ENTITY_MENTION")) {
            kafTerm = new KafTerm();
            kafTerm.setType("ENTITY");
            kafTerm.setTid(attributes.getValue("m_id"));
        }
        else if (qName.equalsIgnoreCase("source")) {
            if (REFERSTO) {
                corefTarget = new CorefTarget();
                corefTarget.setId(attributes.getValue("m_id"));
                corefTargetArrayList.add(corefTarget);
            }
        }
        else if (qName.equalsIgnoreCase("token_anchor")) {
            span = attributes.getValue("t_id");
        }
        else if (qName.equalsIgnoreCase("REFERS_TO")) {
            REFERSTO = true;
            kafCoreferenceSet = new KafCoreferenceSet();
            kafCoreferenceSet.setCoid(attributes.getValue("r_id"));
        }
        value = "";
    }//--startElement


    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equalsIgnoreCase("token")) {
            kafWordForm.setWf(value.trim());
            kafWordFormArrayList.add(kafWordForm);
            kafWordForm = new KafWordForm();
        }
        else if (qName.equalsIgnoreCase("EVENT_MENTION")) {
            kafTerm.addSpans(span);
            span = "";
            kafTermArrayList.add(kafTerm);
            kafTerm = new KafTerm();
        }
        else if (qName.equalsIgnoreCase("ENTITY_MENTION")) {
            kafTerm.addSpans(span);
            span = "";
            kafTermArrayList.add(kafTerm);
            kafTerm = new KafTerm();
        }
        else if (qName.equalsIgnoreCase("REFERS_TO")) {
            REFERSTO = false;
            kafCoreferenceSet.addSetsOfSpans(corefTargetArrayList);
            corefTargetArrayList = new ArrayList<CorefTarget>();
            kafCoreferenceSetArrayList.add(kafCoreferenceSet);
            kafCoreferenceSet = new KafCoreferenceSet();
        }
    }

    public void switchToTokenIds () {
        for (int i = 0; i < kafCoreferenceSetArrayList.size(); i++) {
            KafCoreferenceSet coreferenceSet = kafCoreferenceSetArrayList.get(i);
            String type = "";
            for (int j = 0; j < coreferenceSet.getSetsOfSpans().size(); j++) {
                ArrayList<CorefTarget> corefTargets = coreferenceSet.getSetsOfSpans().get(j);
                for (int k = 0; k < corefTargets.size(); k++) {
                    CorefTarget target = corefTargets.get(k);
                    for (int l = 0; l < kafTermArrayList.size(); l++) {
                        KafTerm term = kafTermArrayList.get(l);
                        if (term.getTid().equals(target.getId())) {
                            String tokenId = term.getSpans().get(0);
                            for (int w = 0; w < kafWordFormArrayList.size(); w++) {
                                KafWordForm wordForm = kafWordFormArrayList.get(w);
                                if (wordForm.getWid().equals(tokenId)) {
                                    target.setId(tokenId);
                                    type = term.getType();
                                    break;
                                }
                            }
                        }
                    }

                }
            }
            coreferenceSet.setType(type);
        }
    }

    public void characters(char ch[], int start, int length)
            throws SAXException {
        value += new String(ch, start, length);
        // System.out.println("tagValue:"+value);
    }

    public void serializeToCorefSet (OutputStream stream, String corpus) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation impl = builder.getDOMImplementation();

            Document xmldoc = impl.createDocument(null, "co-ref-sets", null);
            xmldoc.setXmlStandalone(false);
            Element root = xmldoc.getDocumentElement();
            root.setAttribute("corpus", corpus);
            if (kafCoreferenceSetArrayList.size()>0) {
                Element coreferences = xmldoc.createElement("coreferences");
                for (int i = 0; i < this.kafCoreferenceSetArrayList.size(); i++) {
                    KafCoreferenceSet kaf  = kafCoreferenceSetArrayList.get(i);
                    if (kaf.getType().equals("EVENT")) {
                        coreferences.appendChild(kaf.toNafXML(xmldoc));
                    }
                }
                root.appendChild(coreferences);
            }

            DOMSource domSource = new DOMSource(xmldoc);
            TransformerFactory tf = TransformerFactory.newInstance();
            //tf.setAttribute("indent-number", 4);
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.INDENT,"yes");
            serializer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StreamResult streamResult = new StreamResult(new OutputStreamWriter(stream));
            serializer.transform(domSource, streamResult);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    static public void main (String[] args) {
        String pathToCatFile = "/Users/piek/Desktop/NWR/NWR-Annotation/corpus_CAT_ref/corpus_apple/30414_Apple_unveils_new_Intel-based_Mac.txt.xml";
        CatCoref catCoref = new CatCoref();

        //catCoref.parseFile(pathToCatFile);
        //catCoref.serializeToCorefSet(System.out, "test");

        String folder = "/Users/piek/Desktop/NWR/NWR-Annotation/corpus_CAT_ref/corpus_apple/";
        ArrayList<File> files = Util.makeFlatFileList(new File(folder), ".xml");
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            catCoref.parseFile(file.getAbsolutePath());
            try {
                OutputStream fos = new FileOutputStream(file.getAbsolutePath()+".coref");
                catCoref.serializeToCorefSet(fos, "test");
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
