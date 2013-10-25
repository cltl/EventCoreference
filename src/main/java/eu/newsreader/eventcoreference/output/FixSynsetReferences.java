package eu.newsreader.eventcoreference.output;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 3/26/12
 * Time: 9:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class FixSynsetReferences {


    static HashMap<String, ArrayList<String>> getWnLex (String lexFile) {
        HashMap<String, ArrayList<String>> lex = new HashMap<String, ArrayList<String>>();
        try {
            FileInputStream fis = new FileInputStream(lexFile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            while (in.ready()&&(inputLine = in.readLine()) != null) {
                if (inputLine.trim().length()>0) {
                    //1000000000000 ENG-30-13752443-n:0 ENG-30-13752172-n:0
                    String [] fields = inputLine.split(" ");
                    if (fields.length>1) {
                        String lemma = fields[0].trim().toLowerCase();
                        ArrayList<String> synsets = new ArrayList<String>();
                        for (int i = 1; i < fields.length; i++) {
                            String field = fields[i];
                            int idx = field.lastIndexOf(":");
                            if (idx>-1) {
                                field = field.substring(0, idx);
                            }
                            synsets.add(field);
                        }
                        lex.put(lemma, synsets);
                    }
                }
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return lex;
    }
    static public void main (String[] args) {
        try {
            String kybotOutputFile = args[0];
            String kybotOutputFileTest = kybotOutputFile+".synset-fix.xml";
            String ukbLexiconFile = args[1];
            HashMap<String, ArrayList<String>> lex = getWnLex(ukbLexiconFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(kybotOutputFile);
            doc.getDocumentElement().normalize();

            //NodeList list = doc.getChildNodes();
            NodeList list = doc.getElementsByTagName("*");
            for (int i = 0; i < list.getLength(); i++) {
                Node node = (Node) list.item(i);
                NamedNodeMap attributes = node.getAttributes();
                Node attLemma = attributes.getNamedItem("lemma");
                Node attSynset = attributes.getNamedItem("synset");
                if ((attLemma!=null) && (attSynset!=null)) {
                    String lemma =  attLemma.getNodeValue().toLowerCase();
                    String synset = attSynset.getNodeValue().toLowerCase();
                    if (synset.isEmpty()) {
                        if (lex.containsKey(lemma)) {
                            ArrayList<String> synsets = lex.get(lemma);
                            attSynset.setNodeValue(synsets.get(0).toLowerCase());
/*
                            System.out.println("lemma = " + lemma);
                            System.out.println("synset = " + synset);
*/
                        }
                    }
                }
            }

            // Use a Transformer for output
            TransformerFactory tFactory =
                    TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(kybotOutputFileTest);
            transformer.transform(source, result);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SAXException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TransformerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    
}
