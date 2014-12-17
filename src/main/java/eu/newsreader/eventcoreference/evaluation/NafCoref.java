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
        String type = "";
        String fileExtension = "";
        String format = "";
        String pathToNafFile = "";
        String folder = "";

        /*** test parameters to run without setting parameters
        ///  COMMENT OUT BEFORE RELEASE
         ***/
        // type = "EVENT-GRAMMATICAL";
        // type = "EVENT-SPEECH_COGNITIVE";
        //type = "EVENT-OTHER";
        // type= "event";
        // format = "conll";
        //format = "coref";
       // pathToNafFile = "/Users/piek/Desktop/NWR/corpus_NAF_output_281114/corpus_apple_event_based/9549_Reactions_to_Apple.naf";
        // folder = "/Users/piek/Desktop/NWR/corpus_NAF_output_281114/corpus_apple";
        //fileExtension = ".naf";
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
            } else if (arg.equalsIgnoreCase("--format") && args.length > (i + 1)) {
                format = args[i + 1];
            }
        }
        if (!pathToNafFile.isEmpty()) {
            kafSaxParser.parseFile(pathToNafFile);
            switchToTokenIds(kafSaxParser);
            String fileName = new File (pathToNafFile).getName();
            int idx = fileName.indexOf(".");
            if (idx>-1) {
                fileName = fileName.substring(0, idx);
            }
            if (format.equalsIgnoreCase("coref")) {
                serializeToCorefSet(System.out, kafSaxParser, fileName , type);
            }
            else if (format.equalsIgnoreCase("conll")) {
                CoNLL.serializeToCoNLL(System.out, fileName, type, kafSaxParser.kafWordFormList, kafSaxParser.kafCorefenceArrayList);
            }
            else {
                System.out.println("Unknown format:"+ format);
            }
        }
        else if (!folder.isEmpty()) {
            ArrayList<File> files = Util.makeFlatFileList(new File(folder), fileExtension);
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                kafSaxParser.parseFile(file.getAbsolutePath());
                try {
                    OutputStream fos = new FileOutputStream(file.getAbsolutePath() + "." + type + ".response");
                    switchToTokenIds(kafSaxParser);
                    String fileName = file.getName();
                    int idx = fileName.indexOf(".");
                    if (idx>-1) {
                        fileName = fileName.substring(0, idx);
                    }

                    if (format.equalsIgnoreCase("coref")) {
                        serializeToCorefSet(fos, kafSaxParser, fileName, type);
                    }
                    else if (format.equalsIgnoreCase("conll")) {
                        CoNLL.serializeToCoNLL(fos, fileName, type, kafSaxParser.kafWordFormList, kafSaxParser.kafCorefenceArrayList);
                    }
                    else {
                        System.out.println("Unknown format:"+ format);
                    }
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
            if (coreferenceSet.getType().isEmpty()) {
                coreferenceSet.setType("ENTITY");
            }
            String coid = coreferenceSet.getCoid();
            if (coid.startsWith("coevent")) {
               coreferenceSet.setCoid(coid.substring(7));
            }
            else if (coid.startsWith("co")) {
                coreferenceSet.setCoid(coid.substring(3));
            }
           // System.out.println("coreferenceSet.getType() = " + coreferenceSet.getType());
            for (int j = 0; j < coreferenceSet.getSetsOfSpans().size(); j++) {
                ArrayList<CorefTarget> corefTargets = coreferenceSet.getSetsOfSpans().get(j);
                for (int k = 0; k < corefTargets.size(); k++) {
                    CorefTarget target = corefTargets.get(k);
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
        }
    }

    public static void switchToTokenIdsAndEventTypes (KafSaxParser kafSaxParser) {
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




    /**
     *
     * @param stream
     * @param kafSaxParser
     * @param fileName
     * @param type
     */
/*    static public void serializeToCoNLL (OutputStream stream, KafSaxParser kafSaxParser, String fileName, String type) {
        try {
            String str = "#begin document ("+fileName+");";
            stream.write(str.getBytes());
            str  = "";
            boolean COREFERRING = false;
            boolean NEWSENTENCE = false;
            String currentSentence = "";

            for (int i = 0; i < kafSaxParser.getKafWordFormList().size(); i++) {
                KafWordForm kafWordForm = kafSaxParser.getKafWordFormList().get(i);
                /// insert sentence separator
                if (!currentSentence.isEmpty() && !currentSentence.equals(kafWordForm.getSent()))  {
                    NEWSENTENCE = true;
                    currentSentence = kafWordForm.getSent();
                }
                else if (currentSentence.isEmpty()) {
                    /// first sentence
                    currentSentence = kafWordForm.getSent();
                }
                else {
                    NEWSENTENCE = false;
                }
                String corefId = getCoreferenceSetId(kafSaxParser, kafWordForm.getWid(), type);
                //// First we need to handle the previous line if any
                //// After that we can process the current
                /// check previous conditions and terminate properly

                if (corefId.isEmpty()) {
                    //// current is not a coreferring token
                    if (COREFERRING) {
                       //// previous was coreferring so we need to terminate the previous with ")"
                       str += ")";
                    }
                    /// always terminate the previous token
                    str += "\n";
                    COREFERRING = false;
                    /// we started a new sentence so we insert a blank line
                    if (NEWSENTENCE) str+= "\n";
                    /// add the info for the current token
                    str += fileName+"\t"+kafWordForm.getSent()+"\t"+kafWordForm.getWid()+"\t"+kafWordForm.getWf() +"\t"+"-";
                }
                else {
                    if (NEWSENTENCE) {
                        /// we started a new sentence so we insert a blank line
                        if (COREFERRING) {
                            /// end of sentence implies ending coreference as well
                            str += ")\n";
                        }
                        else {
                            str += "\n";
                        }
                        str+= "\n";
                    }
                    else {
                        str+= "\n";
                    }
                    /// add the info for the current token
                    str += fileName+"\t"+kafWordForm.getSent()+"\t"+kafWordForm.getWid()+"\t"+kafWordForm.getWf() +"\t";
                    if (!COREFERRING) {
                        str += "("+corefId;
                        COREFERRING = true;
                    }
                    else {
                        str += corefId;
                    }
                }
            }
            ///check the status of the last token
            if (COREFERRING) {
                str += ")\n";
            }
            else {
                str += "\n";
            }
            stream.write(str.getBytes());
            str = "#end document\n";
            stream.write(str.getBytes());

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    static public String getCoreferenceSetId (KafSaxParser kafSaxParser, String tokenId, String type) {
        String corefId = "";
        for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
            KafCoreferenceSet corefSet  = kafSaxParser.kafCorefenceArrayList.get(i);
           // System.out.println("coref.getType() = " + corefSet.getType());
            if (type.isEmpty() || corefSet.getType().equalsIgnoreCase(type)) {
                for (int j = 0; j < corefSet.getSetsOfSpans().size(); j++) {
                    ArrayList<CorefTarget> corefTargets = corefSet.getSetsOfSpans().get(j);
                    for (int k = 0; k < corefTargets.size(); k++) {
                        CorefTarget corefTarget = corefTargets.get(k);
                        if (corefTarget.getId().equals(tokenId)) {
                            corefId = corefSet.getCoid();
                            break;
                        }
                    }
                    if (!corefId.isEmpty()) {
                        break;
                    }
                }
            }
            else {
              //  if (!corefSet.getType().isEmpty()) System.out.println("coref.getType() = " + corefSet.getType());
            }
            if (!corefId.isEmpty()) {
                break;
            }
        }
        return corefId;
    }*/
}
