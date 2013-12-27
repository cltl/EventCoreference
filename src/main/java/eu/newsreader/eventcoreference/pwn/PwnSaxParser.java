package eu.newsreader.eventcoreference.pwn;

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
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 2/27/12
 * Time: 10:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class PwnSaxParser extends DefaultHandler {

    static public HashMap<String, ArrayList<String>> hyperRelations = new HashMap<String, ArrayList<String>>();
    static public HashMap<String, ArrayList<String>> otherRelations = new HashMap<String, ArrayList<String>>();
    static String value = "";
    static String sourceId = "";
    static String targetId= "";
    String type = "";
    ArrayList<String> hypers = new ArrayList<String>();
    ArrayList<String> others = new ArrayList<String>();
    static public int nAverageNounDepth = 0;
    static public int nAverageVerbDepth = 0;
    static public int nAverageAdjectiveDepth = 0;

    public PwnSaxParser() {
        initParser();
    }

    public void initParser () {
        hyperRelations = new HashMap<String, ArrayList<String>>();
        otherRelations = new HashMap<String, ArrayList<String>>();
        nAverageNounDepth = 0;
        nAverageVerbDepth = 0;
        nAverageAdjectiveDepth = 0;
    }

    public void parseFile(String filePath) {
        System.out.println("filePath = " + filePath);
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
        System.out.println("myerror = " + myerror);
    }//--c



    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {

        if (qName.equalsIgnoreCase("ILR")) {
            type = attributes.getValue("type");
        }
        else {
        }
        value = "";
    }//--startElement


    /*

<SYNSET>
<ID>eng-30-00001740-a</ID>
<POS>a</POS><SYNONYM>
<LITERAL sense="1">able</LITERAL>
<WORD>able</WORD></SYNONYM>
<ILR type="near_antonym">eng-30-00002098-a</ILR>
<ILR type="be_in_state">eng-30-05200169-n</ILR>
<ILR type="be_in_state">eng-30-05616246-n</ILR>
<ILR type="eng_derivative">eng-30-05200169-n</ILR>
<ILR type="eng_derivative">eng-30-05616246-n</ILR>
<DEF>(usually followed by `to') having the necessary means or skill or know-how or authority to do something</DEF>
<USAGE>able to swim</USAGE><USAGE>she was able to program her computer</USAGE>
<USAGE>we were at last able to buy a car</USAGE>
<USAGE>able to get a grant for the project</USAGE>
</SYNSET>

    */

    /*
    <SYNSET>
    <STAMP>fra82e3 2010-10-20 15:22:44</STAMP>
    <RIGIDITY rigidScore="0" rigid="false" nonRigidScore="0"/>
    <ILR type="hypernym">eng-30-10058777-n</ILR>
    <ILR type="eng_derivative">eng-30-01097031-v</ILR>
    <ILR type="eng_derivative">eng-30-01518694-a</ILR>
    <ILR type="eng_derivative">eng-30-05640184-n</ILR>
    <ID>eng-30-10622053-n</ID>
    <SYNONYM><LITERAL sense="1">soldier</LITERAL></SYNONYM>
    <DEF>an enlisted man or woman who serves in an army</DEF>
    <USAGE>the soldiers stood at attention</USAGE>
    <VERSION>642</VERSION><POS>n</POS>
    </SYNSET>
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equalsIgnoreCase("ID")) {
            sourceId = value.trim();
            if (sourceId.equals("eng-30-00007846-n")) {
             //   System.out.println("person sourceId = " + sourceId);

            }
            if (sourceId.equals("eng-30-10622053-n")) {
             //   System.out.println("soldier:1 sourceId = " + sourceId);
            }
        }
        else if (qName.equalsIgnoreCase("ILR")) {
            if (type.equalsIgnoreCase("hypernym")) {
                targetId = value.trim();
                hypers.add(targetId);
            }
            else if (type.equalsIgnoreCase("eng_derivative")) {
                targetId = value.trim();
                others.add(targetId);
            }
            type = "";
        }
        else if (qName.equalsIgnoreCase("SYNSET")) {
            if ((!sourceId.isEmpty()) && hypers.size()>0) {
                hyperRelations.put(sourceId, hypers);
            }
            if ((!sourceId.isEmpty()) && others.size()>0) {
                otherRelations.put(sourceId, others);
            }
            sourceId = "";
            others = new ArrayList<String>();
            hypers = new ArrayList<String>();
        }
    }

    public void characters(char ch[], int start, int length)
            throws SAXException {
        value += new String(ch, start, length);
        // System.out.println("tagValue:"+value);
    }

    public void getRelationChain (String source, ArrayList<String> targetChain) {
        if (hyperRelations.containsKey(source)) {
            ArrayList<String> targets = hyperRelations.get(source);
            for (int i = 0; i < targets.size(); i++) {
                String target =  targets.get(i);
                if (!target.equals(source)) {
                    if (!targetChain.contains(target)) {
                        targetChain.add(target);
                    //    System.out.println("source = " + source);
                    //    System.out.println("target = " + target);
                        getRelationChain(target, targetChain);
                    }
                    else {
                        ///circular
                        break;
                    }
                }
                else {
                    /// circular
                    break;
                }
            }
        }
    }

    public void getXposRelationChain (String source, ArrayList<String> targetChain) {
        /// first add the non-transitives
        if (otherRelations.containsKey(source)) {
            ArrayList<String> others = otherRelations.get(source);
            for (int i = 0; i < others.size(); i++) {
                String other =  others.get(i);
                if (!other.equals(source)) {
                    if (!targetChain.contains(other)) {
                      //  System.out.println("other = " + other);
                        targetChain.add(other);
                    }
                }
            }
        }
        if (hyperRelations.containsKey(source)) {
            ArrayList<String> targets = hyperRelations.get(source);
            for (int i = 0; i < targets.size(); i++) {
                String target =  targets.get(i);
                if (!target.equals(source)) {
                    if (!targetChain.contains(target)) {
                        targetChain.add(target);
                    //    System.out.println("source = " + source);
                    //    System.out.println("target = " + target);
                        getRelationChain(target, targetChain);
                    }
                    else {
                        ///circular
                        break;
                    }
                }
                else {
                    /// circular
                    break;
                }
            }
        }
    }

    //[bombard,     eng-30-01507914-v, eng-30-05045208-n, eng-30-10413429-n, eng-30-01508368-v, eng-30-00104539-n, eng-30-10709529-n, eng-30-01511706-v, eng-30-00045250-n, eng-30-00104249-n, eng-30-00809790-a, eng-30-00842550-a, eng-30-03563460-n, eng-30-04011827-n, eng-30-14691822-n, eng-30-01850315-v, eng-30-00279835-n, eng-30-00280586-n, eng-30-01523724-a, eng-30-01526062-a, eng-30-08478482-n, eng-30-10336234-n]
    //[bombardment, eng-30-00978413-n, eng-30-01131902-v, eng-30-00972621-n, eng-30-01118449-v, eng-30-01119169-v, eng-30-00955060-n, eng-30-01109863-v, eng-30-00407535-n, eng-30-00927373-a, eng-30-01515280-a, eng-30-00030358-n, eng-30-01643657-v, eng-30-01649999-v, eng-30-02367363-v, eng-30-00029378-n, eng-30-00023100-n, eng-30-00002137-n, eng-30-00692329-v, eng-30-00001740-n]
}
