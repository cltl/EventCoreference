package eu.newsreader.eventcoreference.storyline;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafWordForm;
import eu.newsreader.eventcoreference.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by piek on 20/07/16.
 */
public class NafTokenLayerIndex extends DefaultHandler {

    public HashMap<String, ArrayList<KafWordForm>>  tokenMap;
    private String value = "";
    private KafWordForm kafWordForm;
    private ArrayList<KafWordForm> kafWordForms;
    private String urlString;
    private Vector<String> uriFilter;

    static public void main (String[] args) {
        String folder = "";
        String filter = "";
        folder = "/Users/piek/Desktop/NWR-INC/WorldBank/data/spanish/output-7-v2";
        filter = ".naf";
        try {
            createTokenIndex(new File(folder), filter);
        }  catch (IOException e) {
            e.printStackTrace();
        }
/*
        String file = "/Users/piek/Desktop/NWR-INC/query/worldbank/data/en_token.index";
        NafTokenLayerIndex nafTokenLayerIndex = new NafTokenLayerIndex();
        nafTokenLayerIndex.parseFile(file);
        System.out.println("nafTokenLayerIndex.tokenMap.size() = " + nafTokenLayerIndex.tokenMap.size());
*/
    }

    public NafTokenLayerIndex () {
        tokenMap = new HashMap<String, ArrayList<KafWordForm>>();
        init();
    }

    public NafTokenLayerIndex (Vector<String> uriList) {
        tokenMap = new HashMap<String, ArrayList<KafWordForm>>();
        init();
        uriFilter = uriList;
       // System.out.println("uriList.toString() = " + uriList.toString());
    }

    void init() {
        kafWordForms = new ArrayList<KafWordForm>();
        kafWordForm = new KafWordForm();
        urlString = "";
        uriFilter = new Vector<String>();
    }

    public boolean parseFile(String source)
    {
        return parseFile(new File (source));
    }

    public boolean parseFile(File source)
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            SAXParser parser = factory.newSAXParser();
            parser.parse(source, this);
            return true;
        }
        catch (FactoryConfigurationError factoryConfigurationError)
        {
            factoryConfigurationError.printStackTrace();
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            //System.out.println("last value = " + previousvalue);
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // e.printStackTrace();
        }
        return false;
    }

    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {
        value = "";
        if ((qName.equalsIgnoreCase("text"))) {
            kafWordForms = new ArrayList<KafWordForm>();
        }
        else if (qName.equalsIgnoreCase("wf")) {
            kafWordForm = new KafWordForm();
            String wid = "";
            String sentenceId = "";
            for (int i = 0; i < attributes.getLength(); i++) {
                String name = attributes.getQName(i);
                if (name.equalsIgnoreCase("wid")) {
                    wid = attributes.getValue(i).trim();
                    kafWordForm.setWid(wid);
                }
                else if (name.equalsIgnoreCase("id")) {
                    wid = attributes.getValue(i).trim();
                    kafWordForm.setWid(wid);
                }
                else if (name.equalsIgnoreCase("para")) {
                    kafWordForm.setPara(attributes.getValue(i).trim());
                }
                else if (name.equalsIgnoreCase("page")) {
                    kafWordForm.setPage(attributes.getValue(i).trim());
                }
                else if (name.equalsIgnoreCase("sent")) {
                    sentenceId = attributes.getValue(i).trim();
                    kafWordForm.setSent(sentenceId);
                }
                else if (name.equalsIgnoreCase("charoffset")) {
                    kafWordForm.setCharOffset(attributes.getValue(i).trim());
                }
                else if (name.equalsIgnoreCase("charlength")) {
                    kafWordForm.setCharLength(attributes.getValue(i).trim());
                }
                else if (name.equalsIgnoreCase("offset")) {
                    kafWordForm.setCharOffset(attributes.getValue(i).trim());
                }
                else if (name.equalsIgnoreCase("length")) {
                    kafWordForm.setCharLength(attributes.getValue(i).trim());
                }
                else {
                    //  System.out.println("414 ********* FOUND UNKNOWN Attribute " + name + " *****************");
                }
            }
        }
    }


    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equals("text")) {

            if (uriFilter.contains(urlString)) {
                tokenMap.put(urlString, kafWordForms);
            }
            else {
            }
            urlString = "";
            kafWordForms = new ArrayList<KafWordForm>();
        }
        else if (qName.equals("wf")) {
            kafWordForm.setWf(value);
            kafWordForms.add(kafWordForm);
            kafWordForm = new KafWordForm();
        }
        else if (qName.equals("url")) {
            urlString = value;
        }
    }

    public void characters(char ch[], int start, int length)
            throws SAXException {
        value += new String(ch, start, length);
    }

    static void createTokenIndex (File folder, String filter) throws IOException {
        File indexFile = new File(folder+"/"+"token.index");
        OutputStream stream = new FileOutputStream(indexFile);

        String str = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<index>\n";
        stream.write(str.getBytes());

        ArrayList<File> nafFiles = Util.makeRecursiveFileList(folder, filter);

        KafSaxParser kafSaxParser = new KafSaxParser();
        for (int f = 0; f < nafFiles.size(); f++) {
            File file = nafFiles.get(f);
            if (f%100==0) System.out.println("file.getName() = " + file.getName());
            kafSaxParser.parseFile(file);

            String uri = kafSaxParser.getKafMetaData().getUrl();
            if (kafSaxParser.kafWordFormList.size()>0) {
                str = "<text>\n";
                str += "<url><![CDATA["+uri+"]]></url>\n";
                for (int i = 0; i < kafSaxParser.kafWordFormList.size(); i++) {
                    KafWordForm kaf  = kafSaxParser.kafWordFormList.get(i);
                    //<wf id="w1" length="10" offset="0" para="1" sent="1">Resolucion</wf>
                    str += "<wf id=\""+kaf.getWid()+"\" length=\""+kaf.getCharLength()+"\" offset=\""+kaf.getCharOffset()+"\"><![CDATA["+kaf.getWf()+"]]></wf>\n";
                }
                str += "</text>\n";
                stream.write(str.getBytes());
            }
        }
        str = "</index>\n";
        stream.write(str.getBytes());
    }


/*    public void writeNafToStream(OutputStream stream, String uri, ArrayList<KafWordForm> tokens)
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation impl = builder.getDOMImplementation();

            Document xmldoc = impl.createDocument(null, "NAF", null);
            xmldoc.setXmlStandalone(false);
            Element root = xmldoc.getDocumentElement();

            if (tokens.size()>0) {
                Element text = xmldoc.createElement("text");
                text.setAttribute("uri", uri);
                for (int i = 0; i < tokens.size(); i++) {
                    KafWordForm kaf  = tokens.get(i);
                    text.appendChild(kaf.toNafXML(xmldoc));
                }
                root.appendChild(text);
            }

            // Serialisation through Tranform.
            DOMSource domSource = new DOMSource(xmldoc);
            TransformerFactory tf = TransformerFactory.newInstance();
            //tf.setAttribute("indent-number", 4);
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.INDENT,"yes");
            //serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
            serializer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            //serializer.setParameter("format-pretty-print", Boolean.TRUE);
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            StreamResult streamResult = null;
            if (encoding.isEmpty()) {
                streamResult = new StreamResult(new OutputStreamWriter(stream));
            }
            else {
                streamResult = new StreamResult(new OutputStreamWriter(stream, encoding));
            }
            serializer.transform(domSource, streamResult);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }*/


}
