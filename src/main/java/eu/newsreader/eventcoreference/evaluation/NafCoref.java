package eu.newsreader.eventcoreference.evaluation;

import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.util.Util;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by piek on 10/24/14.
 */
public class NafCoref {
    static public void main (String[] args) {
        //String type = "EVENT-GRAMMATICAL";
        //String type = "EVENT-SPEECH_COGNITIVE";
        String type = "EVENT-OTHER";
        //String pathToNafFile = "";
        String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-Annotation/corpus_NAF_output/corpus_apple/10450_Apple_Computer_CEO_Steve_Jobs_gives_opening_keynote_to_WWDC_2005.naf";
        String folder = "/Users/piek/Desktop/NWR/NWR-Annotation/corpus_NAF_output/corpus_apple/";
        String fileExtension = ".xml";
        KafSaxParser kafSaxParser = new KafSaxParser();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("--naf-file") && args.length > (i + 1)) {
                pathToNafFile = args[i + 1];
            } else if (arg.equalsIgnoreCase("--folder") && args.length > (i + 1)) {
                folder = args[i + 1];
            } else if (arg.equalsIgnoreCase("--file-extension") && args.length > (i + 1)) {
                fileExtension = args[i + 1];
            } else if (arg.equalsIgnoreCase("--coref-type") && args.length > (i + 1)) {
                type = args[i + 1];
            }
        }
        if (!pathToNafFile.isEmpty()) {
            kafSaxParser.parseFile(pathToNafFile);
            switchToTokenIds(kafSaxParser);
            serializeToCorefSet(System.out, kafSaxParser, "test", type);
        }
        else if (!folder.isEmpty()) {
            ArrayList<File> files = Util.makeFlatFileList(new File(folder), ".xml");
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                kafSaxParser.parseFile(file.getAbsolutePath());
                try {
                    OutputStream fos = new FileOutputStream(file.getAbsolutePath() + "." + type + ".coref");
                    switchToTokenIds(kafSaxParser);
                    serializeToCorefSet(fos, kafSaxParser, "test", type);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void switchToTokenIds (KafSaxParser kafSaxParser) {
        for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
            KafCoreferenceSet coreferenceSet = kafSaxParser.kafCorefenceArrayList.get(i);
            String type = "";
            for (int j = 0; j < coreferenceSet.getSetsOfSpans().size(); j++) {
                ArrayList<CorefTarget> corefTargets = coreferenceSet.getSetsOfSpans().get(j);
                for (int k = 0; k < corefTargets.size(); k++) {
                    CorefTarget target = corefTargets.get(k);
                    for (int l = 0; l < kafSaxParser.kafEventArrayList.size(); l++) {
                        KafEvent event = kafSaxParser.kafEventArrayList.get(l);
                        if (event.getSpanIds().contains(target.getId())) {
                            for (int m = 0; m < event.getExternalReferences().size(); m++) {
                                KafSense kafSense = event.getExternalReferences().get(m);
                                if (kafSense.getResource().equalsIgnoreCase("EventType")) {
                                    if (kafSense.getSensecode().equalsIgnoreCase("contextual")) {
                                        type = "EVENT"+"-"+"OTHER";
                                    }
                                    else if (kafSense.getSensecode().equalsIgnoreCase("grammatical")) {
                                        type = "EVENT"+"-"+"GRAMMATICAL";
                                    }
                                    else if (kafSense.getSensecode().equalsIgnoreCase("communication")) {
                                        type = "EVENT"+"-"+"SPEECH_COGNITIVE";
                                    }
                                    else if (kafSense.getSensecode().equalsIgnoreCase("cognitive")) {
                                        type = "EVENT"+"-"+"SPEECH_COGNITIVE";
                                    }
                                    else  {
                                        type = "EVENT"+"-"+"OTHER" ;
                                    }
                                    //System.out.println("type = " + type);
                                    break;
                                }
                                else {
                                 //   System.out.println("kafSense.getResource() = " + kafSense.getResource());
                                }
                            }
                        }
                    }
                    for (int l = 0; l < kafSaxParser.kafTermList.size(); l++) {
                        KafTerm term = kafSaxParser.kafTermList.get(l);
                        if (term.getTid().equals(target.getId())) {
                            String tokenId = term.getSpans().get(0);
                            for (int w = 0; w < kafSaxParser.kafWordFormList.size(); w++) {
                                KafWordForm wordForm = kafSaxParser.kafWordFormList.get(w);
                                if (wordForm.getWid().equals(tokenId)) {
                                    target.setId(tokenId);
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

    static public void serializeToCorefSet (OutputStream stream, KafSaxParser kafSaxParser, String corpus, String type) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation impl = builder.getDOMImplementation();

            Document xmldoc = impl.createDocument(null, "co-ref-sets", null);
            xmldoc.setXmlStandalone(false);
            Element root = xmldoc.getDocumentElement();
            root.setAttribute("corpus", corpus);
            if (kafSaxParser.kafCorefenceArrayList.size()>0) {
                Element coreferences = xmldoc.createElement("coreferences");
                for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
                    KafCoreferenceSet corefSet  = kafSaxParser.kafCorefenceArrayList.get(i);
                    if (corefSet.getType().isEmpty()) {
                        coreferences.appendChild(corefSet.toNafXML(xmldoc));
                    }
                    else {
                        if (corefSet.getType().equalsIgnoreCase(type)) {
                            coreferences.appendChild(corefSet.toNafXML(xmldoc));
                        }
                        else {
                            System.out.println("coref.getType() = " + corefSet.getType());
                        }
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
}
