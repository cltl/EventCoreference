package eu.newsreader.eventcoreference.input;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;

/**
 * Created by piek on 09/02/16.
 */
public class MentionResolver {
    final String USER_AGENT = "Mozilla/5.0";
    public static String serviceEndpoint = "https://knowledgestore2.fbk.eu/nwr/cars3/files?id=";
    public static String user = "nwr_partner";
    public static String pass = "ks=2014!";
    HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());

    //    //wget --http-user=nwr_partner --http-password=ks=2014! https://knowledgestore2.fbk.eu/nwr/cars3/files?id=http%3A%2F%2Fwww.newsreader-project.eu%2Fdata%2Fcars%2F2004%2F10%2F18%2F4DKT-30W0-00S0-W39B.xml


    // HTTP GET request
    /*static String sendGet(String url) throws Exception {

        URL obj = new URL(url);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
     //   List<NameValuePair> postParams =  http.getFormParams(page, "username","password");

        HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());

        // optional default is GET
        con.setRequestMethod("GET");
        con.set
        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        System.out.println(response.toString());
        return response.toString();

    }*/



    private static String getNafFile(String urlString, String fileName) throws Exception {

        URLConnection request = new URL(urlString).openConnection();
        InputStream in = request.getInputStream();

//        File downloadedFile = File.createTempFile("tempfile", "txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        FileOutputStream out = new FileOutputStream(fileName);
        byte[] buffer = new byte[1024];
        int len = in.read(buffer);
        while (len != -1) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
        in.close();
        out.close();
        byte[] response = out.toByteArray();

        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(response);
        fos.close();
        return "success";
    }

    static void readRawTextFromKS(String docId){
        TrigTripleData trigTripleData = new TrigTripleData();
        HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());
        QueryExecution x = QueryExecutionFactory.sparqlService(serviceEndpoint, docId, authenticator);
        ResultSet resultset = x.execSelect();
        HashSet<String> objectSet = new HashSet<String>();
        while (resultset.hasNext()) {
            QuerySolution solution = resultset.nextSolution();

        }


    }
     //https://knowledgestore2.fbk.eu/nwr/cars3/files?id=http%3A%2F%2Fwww.newsreader-project.eu%2Fdata%2Fcars%2F2004%2F10%2F18%2F4DKT-30W0-00S0-W39B.xml
    static public void main (String[] args) {
        String url = "http://www.newsreader-project.eu/data/cars/2004/10/18/4DKT-30W0-00S0-W39B.xml";
        readRawTextFromKS(url);
    }
}
