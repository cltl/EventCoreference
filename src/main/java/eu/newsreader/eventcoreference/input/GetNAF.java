package eu.newsreader.eventcoreference.input;

import eu.kyotoproject.kaf.KafSaxParser;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
/**
 * Created by filipilievski on 2/16/16.
 */
public class GetNAF {
    public static String user = "nwr_partner";
    public static String pass = "ks=2014!";

    static public void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("--u") && args.length>(i+1)) {
                user = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--p") && args.length>(i+1)) {
                pass = args[i+1];
            }
        }
        Authenticator.setDefault(new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass.toCharArray());
            }
        });


        System.out.println(getFile("https://knowledgestore2.fbk.eu/nwr/cars3/files?id=%3Chttp%3A%2F%2Fwww.newsreader-project.eu%2Fdata%2Fcars%2F2004%2F10%2F18%2F4DKT-30W0-00S0-W39B.xml.naf%3E"));
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time elapsed:");
        System.out.println(estimatedTime/1000.0);
    }

    public static String getFile(String stringUrl) throws Exception {
        //stringUrl = "https://knowledgestore2.fbk.eu/nwr/cars3/files?id=%3Chttp%3A%2F%2Fwww.newsreader-project.eu%2Fdata%2Fcars%2F2004%2F10%2F18%2F4DKT-30W0-00S0-W39B.xml.naf%3E";

        URL url = new URL(stringUrl);
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
      // connection.setRequestProperty("Accept", "application/xml");
        connection.setRequestProperty("Accept", "application/octet-stream");
        InputStream xml = connection.getInputStream();
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(xml);
        String rawText =  kafSaxParser.rawText;
/*      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(xml);
        return getStringFromDocument(doc);*/
        return rawText;
    }


    public static String getStringFromDocument(Document doc) throws TransformerException {
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }

}
