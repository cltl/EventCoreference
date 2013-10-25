package eu.newsreader.eventcoreference.input;

import eu.newsreader.eventcoreference.objects.CoRefSet;
import eu.newsreader.eventcoreference.objects.CorefTarget;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 2/29/12
 * Time: 2:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class CorefSaxParser extends DefaultHandler{
    int cardinality=0;
    public int namesubstring = -1;
    String value;
    public String fileName;
    public String corpusName;
    String orgFileName;
    public HashMap<String, ArrayList<CoRefSet>> corefMap;
    CoRefSet corefSet;
    public String method = "";
    public String threshold = "";

    public CorefSaxParser (int n) {
        init();
        cardinality = n;
    }
    
    public CorefSaxParser (int n, int namesubstring) {
        init();
        this.cardinality = n;
        this.namesubstring = namesubstring;
    }

    public CorefSaxParser () {
         init();
    }
    
    void init () {
        corefMap = new HashMap<String, ArrayList<CoRefSet>>();
        corefSet = new CoRefSet();
        fileName = "";
        corpusName = "";
        orgFileName = "";
        method = "";
        threshold = "";
        cardinality=0;
        namesubstring = -1;
    }
    
    public void parseFile(String filePath) {
        String myerror = "";
      //  System.out.println("filePath = " + filePath);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            SAXParser parser = factory.newSAXParser();
            InputSource inp = new InputSource (new FileReader(filePath));
            parser.parse(inp, this);
           // System.out.println("corefMap.size() = " + corefMap.size());
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
       // System.out.println("myerror = " + myerror);
       // System.out.println("this.corefMap.size() = " + this.corefMap.size());
    }//--c



    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {

        if (qName.equalsIgnoreCase("coref")) {
            for (int i = 0; i < attributes.getLength(); i++) {
                String name = attributes.getQName(i);
                if (name.equalsIgnoreCase("method")) {
                    method = attributes.getValue(i).trim();
                }
                else if (name.equalsIgnoreCase("threshold")) {
                    threshold = attributes.getValue(i).trim();
                }
            }
        }
        else if (qName.equalsIgnoreCase("co-ref-sets")) {
            for (int i = 0; i < attributes.getLength(); i++) {
                String name = attributes.getQName(i);
                if (name.equalsIgnoreCase("file")) {

                    orgFileName = attributes.getValue(i).trim();
                    fileName = orgFileName;
                    if (fileName.length()>=namesubstring) {
                        if (namesubstring>-1) {
                            fileName = fileName.substring(0, namesubstring);
                          //  System.out.println("fileName = " + fileName);
                        }
                    }
                    else {
                        System.out.println("fileName = " + fileName);
                    }
                }
                else if (name.equalsIgnoreCase("method")) {
                    method = attributes.getValue(i).trim();
                }
                else if (name.equalsIgnoreCase("corpus")) {
                    corpusName = attributes.getValue(i).trim();
                }
                else if (name.equalsIgnoreCase("threshold")) {
                    threshold = attributes.getValue(i).trim();
                }
            }
        }
        else if (qName.equalsIgnoreCase("event")) {
            //event score="1.1527777777777777" pScore="0.1527777777777778" tScore="0.0" lScore="0.0" nP="4" nL="1" nT="1"
        }
        else if (qName.equalsIgnoreCase("co-refs")) {
            corefSet = new CoRefSet();
           // corefSet.setId(orgFileName);
            for (int i = 0; i < attributes.getLength(); i++) {
                String name = attributes.getQName(i);
                if (name.equalsIgnoreCase("id")) {
                   corefSet.setId(attributes.getValue(i).trim());
                }
                else if (name.equalsIgnoreCase("cid")) {
                   corefSet.setId(attributes.getValue(i).trim());
                }
                else if (name.equalsIgnoreCase("lcs")) {
                    corefSet.setLcs(attributes.getValue(i).trim());
                }
                else if (name.equalsIgnoreCase("score")) {
                    try {
                        double score = Double.parseDouble(attributes.getValue(i).trim());
                        corefSet.setScore(score);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }
        else if (qName.equalsIgnoreCase("target")) {
            CorefTarget target = new CorefTarget();
            for (int i = 0; i < attributes.getLength(); i++) {
                String name = attributes.getQName(i);
                if (name.equalsIgnoreCase("termId")) {
                    target.setTermId(attributes.getValue(i).trim());
                    int idx_e = target.getTermId().lastIndexOf("/");
                    if (idx_e>-1) {
                        target.setDocId(target.getTermId().substring(0, idx_e));
                    }
                }
/*                else if (name.equalsIgnoreCase("docId")) {
                    target.setDocId(attributes.getValue(i).trim());
                }*/
                else if (name.equalsIgnoreCase("sentenceId")) {
                    target.setSentenceId(attributes.getValue(i).trim());
                }
                else if (name.equalsIgnoreCase("sentence")) {
                    target.setSentenceId(attributes.getValue(i).trim());
                }
                else if (name.equalsIgnoreCase("word")) {
                    target.setWord(attributes.getValue(i).trim());
                }
                else if (name.equalsIgnoreCase("comment")) {
                    target.setWord(attributes.getValue(i).trim());
                }
                else if (name.equalsIgnoreCase("synset")) {
                    target.setSynset(attributes.getValue(i).trim());
                }
                else if (name.equalsIgnoreCase("rank")) {
                    try {
                        double score = Double.parseDouble(attributes.getValue(i).trim());
                        target.setSynsetScore(score);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }
                else if (name.equalsIgnoreCase("corefScore")) {
                    try {
                        double score = Double.parseDouble(attributes.getValue(i).trim());
                        target.setCorefScore(score);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }
            }
            if (!hasTarget(corefSet, target)) {
                corefSet.addTarget(target);
            }
        }
        value = "";
    }//--startElement


    boolean hasTarget (CoRefSet corefSet, CorefTarget corefTarget) {
        for (int i = 0; i < corefSet.getTargets().size(); i++) {
            CorefTarget target = corefSet.getTargets().get(i);
            if ((target.getTermId().equals(corefTarget.getTermId())) &&
                (target.getSentenceId().equals(corefTarget.getSentenceId())) &&
                (target.getDocId().equals(corefTarget.getDocId()))
                    ) {
                return true;
            }
        }
        return false;
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equalsIgnoreCase("co-refs")) {
            if (corefSet.getTargets().size()>cardinality) {
                //// we only take coref sets with size > cardinality
                if (corefMap.containsKey(fileName)) {
                    ArrayList<CoRefSet> sets = corefMap.get(fileName);
                    sets.add(corefSet);
                    corefMap.put(fileName, sets);
                }
                else {
                    ArrayList<CoRefSet> sets = new ArrayList<CoRefSet>();
                    sets.add(corefSet);
                    corefMap.put(fileName, sets);
                }
            }
            else {
            //    System.out.println("Ignore corefSet = " + corefSet);
            }
        }
    }

    public void characters(char ch[], int start, int length)
            throws SAXException {
        value += new String(ch, start, length);
        // System.out.println("tagValue:"+value);
    }
    
    public void serialize (String outputFilePath) {
        try {
            FileOutputStream fos = new FileOutputStream(outputFilePath);
            String str ="";
            str ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
            str += "<COREF method=\""+method+"\" threshold=\""+threshold+"\">"+"\n";
            fos.write(str.getBytes());
            Set keySet = corefMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                str = "<co-ref-sets file=\""+key+"\">\n";
                fos.write(str.getBytes());
                ArrayList<CoRefSet> coRefSets = corefMap.get(key);
                for (int i = 0; i < coRefSets.size(); i++) {
                    CoRefSet coRefSet = coRefSets.get(i);
                    str = coRefSet.toString();
                    fos.write(str.getBytes());
                }
                str = "</co-ref-sets>\n";
                fos.write(str.getBytes());
            }
            str = "</COREF>\n";
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void serializeCrossCorpus (String outputFilePath, String corpusId) {
        try {
            FileOutputStream fos = new FileOutputStream(outputFilePath);
            String str ="";
            str ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
            str += "<co-ref-sets corpus=\""+corpusId+"\"";
            if (!method.isEmpty()) str += " method=\""+method+"\"";
            if (!threshold.isEmpty()) str += " threshold=\""+threshold+"\"";
            str += ">\n";
            fos.write(str.getBytes());
            Set keySet = corefMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                ArrayList<CoRefSet> coRefSets = corefMap.get(key);
                for (int i = 0; i < coRefSets.size(); i++) {
                    CoRefSet coRefSet = coRefSets.get(i);
                    str = coRefSet.toString();
                    fos.write(str.getBytes());
                }
            }
            str = "</co-ref-sets>\n";
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    public void serializeCrossCorpus (String outputFilePath) {
        try {
            FileOutputStream fos = new FileOutputStream(outputFilePath);
            String str ="";
            str ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
            str += "<co-ref-sets corpus=\""+corpusName+"\"";
            if (!method.isEmpty()) str += " method=\""+method+"\"";
            if (!threshold.isEmpty()) str += " threshold=\""+threshold+"\"";
            str += ">\n";
            fos.write(str.getBytes());
            Set keySet = corefMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                ArrayList<CoRefSet> coRefSets = corefMap.get(key);
                for (int i = 0; i < coRefSets.size(); i++) {
                    CoRefSet coRefSet = coRefSets.get(i);
                    str = coRefSet.toString();
                    fos.write(str.getBytes());
                }
            }
            str = "</co-ref-sets>\n";
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
