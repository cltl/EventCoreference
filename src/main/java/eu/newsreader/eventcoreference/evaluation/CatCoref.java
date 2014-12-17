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
import java.util.Iterator;
import java.util.Set;

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

<EVENT_MENTION m_id="31" time="NON_FUTURE" special_cases="GEN" aspect="NONE"
certainty="POSSIBLE" polarity="POS" tense="INFINITIVE" modality="" pred="be" comment="" pos="VERB"  >
<token_anchor t_id="44"/>
</EVENT_MENTION>
<EVENT_MENTION m_id="69" time="NON_FUTURE" special_cases="NONE" aspect="NONE"
certainty="CERTAIN" polarity="POS" tense="PRESENT" modality="" pred="unveil" comment="" pos="VERB"  >
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
    static public HashMap<String, KafCoreferenceSet> coreferenceHashMap;
    static public ArrayList<KafCoreferenceSet> kafCoreferenceSetArrayList;
    ArrayList<CorefTarget> corefTargetArrayList;
    KafCoreferenceSet kafCoreferenceSet;
    CorefTarget corefTarget;
    HashMap<String, String> eventMentionTypeMap;
    KafWordForm  kafWordForm;
    static public ArrayList<KafWordForm> kafWordFormArrayList;
    KafTerm kafTerm;
    static public ArrayList<KafTerm> kafTermArrayList;
    ArrayList<String> spans;
    String span = "";
    String target = "";
    boolean REFERSTO = false;

    void init() {
        value = "";
        coreferenceHashMap = new HashMap<String, KafCoreferenceSet>();
        kafCoreferenceSetArrayList = new ArrayList<KafCoreferenceSet>();
        corefTargetArrayList = new ArrayList<CorefTarget>();
        kafCoreferenceSet = new KafCoreferenceSet();
        corefTarget = new CorefTarget();
        eventMentionTypeMap = new HashMap<String, String>();
        kafWordForm = new KafWordForm();
        kafWordFormArrayList = new ArrayList<KafWordForm>();
        kafTerm = new KafTerm();
        kafTermArrayList = new ArrayList<KafTerm>();
        REFERSTO = false;
        target = "";
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
            Integer sentenceInt = Integer.parseInt(attributes.getValue("sentence"));
            sentenceInt++;
            kafWordForm.setSent(sentenceInt.toString());
        }
        else if (qName.equalsIgnoreCase("EVENT")) {
            String type = attributes.getValue("class");
            String mention = attributes.getValue("m_id");
            if (type!=null && mention!=null && !type.isEmpty() && !mention.isEmpty()) {
                eventMentionTypeMap.put(mention, type);
            }
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
            //// sources are mentions that match with the same target. The same target can occur in different refers to mappings
            if (REFERSTO) {
                /// sources and targets can also occur for other relations than refersto
                corefTarget = new CorefTarget();
                corefTarget.setId(attributes.getValue("m_id"));
                corefTargetArrayList.add(corefTarget);
            }
        }
        else if (qName.equalsIgnoreCase("target")) {
            if (REFERSTO) {
                /// sources and targets can also occur for other relations than refersto
                target = attributes.getValue("m_id");
/*
                corefTarget = new CorefTarget();
                corefTarget.setId(attributes.getValue("m_id"));
                corefTargetArrayList.add(corefTarget);
*/
            }
        }
        else if (qName.equalsIgnoreCase("token_anchor")) {
            span = attributes.getValue("t_id");
            kafTerm.addSpans(span);
        }
        else if (qName.equalsIgnoreCase("REFERS_TO")) {
            REFERSTO = true;
            corefTargetArrayList = new ArrayList<CorefTarget>();
            target = "";
            /// it appears that these do not make a unique coreference set
           // kafCoreferenceSet = new KafCoreferenceSet();
           // kafCoreferenceSet.setCoid(attributes.getValue("r_id"));
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
            kafTermArrayList.add(kafTerm);
            kafTerm = new KafTerm();
        }
        else if (qName.equalsIgnoreCase("ENTITY_MENTION")) {
            kafTermArrayList.add(kafTerm);
            kafTerm = new KafTerm();
        }
        else if (qName.equalsIgnoreCase("REFERS_TO")) {
            REFERSTO = false;
            String type = "";
            if (eventMentionTypeMap.containsKey(target)) {
                type = eventMentionTypeMap.get(target);
            }
            if (!target.isEmpty()) {
                if (coreferenceHashMap.containsKey(target)) {
                    KafCoreferenceSet set = coreferenceHashMap.get(target);
                    set.addSetsOfSpans(corefTargetArrayList);
                    coreferenceHashMap.put(target, set);
                }
                else {
                    KafCoreferenceSet set = new KafCoreferenceSet();
                    set.setCoid(target);
                    set.setType(type);
                    set.addSetsOfSpans(corefTargetArrayList);
                    coreferenceHashMap.put(target, set);
                }
            }

//            corefTargetArrayList = new ArrayList<CorefTarget>();
//            kafCoreferenceSetArrayList.add(kafCoreferenceSet);
            corefTargetArrayList = new ArrayList<CorefTarget>();
            kafCoreferenceSet = new KafCoreferenceSet();
            target = "";
        }
    }


    public void switchToTokenIds () {
        Set keySet = coreferenceHashMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            KafCoreferenceSet coreferenceSet = coreferenceHashMap.get(key);
            KafCoreferenceSet newCorefSet = new KafCoreferenceSet();
            newCorefSet.setCoid(key);
         //   System.out.println("newCorefSet.getCoid() = " + newCorefSet.getCoid());
            for (int j = 0; j < coreferenceSet.getSetsOfSpans().size(); j++) {
                ArrayList<CorefTarget> corefTargets = coreferenceSet.getSetsOfSpans().get(j);
                ArrayList<CorefTarget> newCorefTargets = new ArrayList<CorefTarget>();
                for (int k = 0; k < corefTargets.size(); k++) {
                    CorefTarget target = corefTargets.get(k);
                   // System.out.println("target.getId() = " + target.getId());
                    for (int l = 0; l < kafTermArrayList.size(); l++) {
                        KafTerm term = kafTermArrayList.get(l);
                        if (term.getTid().equals(target.getId())) {
                            /// we found the term that includes the target id
                            /// now we should take all the tokens of the term to create the span
                            for (int m = 0; m < term.getSpans().size(); m++) {
                                String span = term.getSpans().get(m);
                                CorefTarget newTarget = new CorefTarget();
                                newTarget.setId(span);
                                /// just to get the word
                                for (int w = 0; w < kafWordFormArrayList.size(); w++) {
                                    KafWordForm wordForm = kafWordFormArrayList.get(w);
                                    if (wordForm.getWid().equals(span)) {
                                        newTarget.setTokenString(wordForm.getWf());
                                        break;
                                    }
                                }
                                newCorefTargets.add(newTarget);
                            }
                            if (newCorefSet.getType().isEmpty()) {
                                if (!term.getType().isEmpty())  {
                                    newCorefSet.setType(term.getType());
                                }
                            }
                        }
                    }

                }
               // System.out.println("newCorefSet.getType() = " + newCorefSet.getType());
                newCorefSet.addSetsOfSpans(newCorefTargets);
            }
            kafCoreferenceSetArrayList.add(newCorefSet);
        }
    }

    public void characters(char ch[], int start, int length)
            throws SAXException {
        value += new String(ch, start, length);
        // System.out.println("tagValue:"+value);
    }

    public void serializeToCorefSet (OutputStream stream, String corpus, String type) {
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
                    if (kaf.getType().equalsIgnoreCase(type)) {
                        coreferences.appendChild(kaf.toNafXML(xmldoc));
                    }
                    else {
                      //  System.out.println("coref.getType() = " + kaf.getType());
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
        String type = "";
        String format = "";
        String folder = "";
        String pathToCatFile = "";
        String fileExtension = "";
        // type = "EVENT-GRAMMATICAL";
        // type = "EVENT-SPEECH_COGNITIVE";
        // type = "EVENT-OTHER";
       // type = "ENTITY";
        //pathToCatFile = "/Users/piek/Desktop/NWR/NWR-benchmark/coreference/corpus_CAT_GS_201412/corpus_apple/9549_Reactions_to_Apple.xml";
        //folder = "/Users/piek/Desktop/NWR/NWR-benchmark/coreference/corpus_CAT_GS_201412/corpus_apple/";
        ///Users/piek/Desktop/NWR/NWR-Annotation/corpus_CAT_ref/corpus_apple
        //fileExtension = ".xml";
        //format = "conll";
        CatCoref catCoref = new CatCoref();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("--cat-file") && args.length>(i+1)) {
                pathToCatFile = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--folder") && args.length>(i+1)) {
                folder = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--file-extension") && args.length>(i+1)) {
                fileExtension = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--coref-type") && args.length>(i+1)) {
                type = args[i+1];

            } else if (arg.equalsIgnoreCase("--format") && args.length > (i + 1)) {
                format = args[i + 1];
            }
        }
        System.out.println("format = " + format);
        System.out.println("type = " + type);
        System.out.println("fileExtension = " + fileExtension);
        System.out.println("folder = " + folder);
        if (!pathToCatFile.isEmpty()) {
            catCoref.parseFile(pathToCatFile);
            OutputStream fos = null;
            try {
                fos = new FileOutputStream(pathToCatFile+"."+type+".key");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String fileName = new File (pathToCatFile).getName();
            int idx = fileName.indexOf(".");
            if (idx>-1) {
                fileName = fileName.substring(0, idx);
            }

            if (format.equals("coref")) {
                catCoref.serializeToCorefSet(fos, fileName, type);
                //catCoref.serializeToCorefSet(System.out, new File (pathToCatFile).getName(), type);
            }
            else if (format.equals("conll")) {
                CoNLL.serializeToCoNLL(fos, fileName, type, kafWordFormArrayList, kafCoreferenceSetArrayList);
               // CoNLL.serializeToCoNLL(System.out, new File(pathToCatFile).getName(), type, kafWordFormArrayList, kafCoreferenceSetArrayList);
            }
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (!folder.isEmpty()){
            ArrayList<File> files = Util.makeFlatFileList(new File(folder), fileExtension);
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                //System.out.println("file.getName() = " + file.getName());
                catCoref.parseFile(file.getAbsolutePath());

                String fileName = file.getName();
                int idx = fileName.indexOf(".");
                if (idx>-1) {
                    fileName = fileName.substring(0, idx);
                }
               // System.out.println("fileName = " + fileName);
                try {
                    OutputStream fos = new FileOutputStream(file.getAbsolutePath()+"."+type+".key");
                    if (format.equals("coref")) {
                        catCoref.serializeToCorefSet(fos, fileName, type);
                    }
                    else if (format.equals("conll")) {
                        CoNLL.serializeToCoNLL(fos, fileName, type, kafWordFormArrayList, kafCoreferenceSetArrayList);
                    }

                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //break;
            }
        }
    }



}
