package eu.newsreader.eventcoreference.storyline;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import eu.newsreader.eventcoreference.input.GetNAF;
import eu.newsreader.eventcoreference.input.TrigTripleData;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by piek on 09/02/16.
 */
public class MentionResolver {
    final String USER_AGENT = "Mozilla/5.0";
    public static String serviceBase = "https://knowledgestore2.fbk.eu/";
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



    public static String getNafFile(String urlString, String fileName) throws Exception {

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
/*
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(out.toString());
        System.out.println("kafSaxParser.rawText = " + kafSaxParser.rawText);
*/
        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(response);
        fos.close();
        return "success";
    }

    public static String getNafContent(String urlString) throws Exception {

        URLConnection request = new URL(urlString).openConnection();
        InputStream in = request.getInputStream();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
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
        return response.toString();

    }
    public static String getKSNafContent(String urlString) throws Exception {
        String content = "";
        long startTime = System.currentTimeMillis();
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass.toCharArray());
            }
        });
        content = GetNAF.getFile(urlString);
                //"https://knowledgestore2.fbk.eu/nwr/cars3/files?id=%3Chttp%3A%2F%2Fwww.newsreader-project.eu%2Fdata%2Fcars%2F2004%2F10%2F18%2F4DKT-30W0-00S0-W39B.xml.naf%3E"));
        long estimatedTime = System.currentTimeMillis() - startTime;
        return content;

    }

    public static String makeRequestUrl (String knowledgeStore, String mentionUri) {
        //https://knowledgestore2.fbk.eu/nwr/wikinews-new/files?id=<
        //knowledgestore2.fbk.eu/nwr/wikinews-new
       // knowledgeStore = "nwr/wikinews-new";
        String str = serviceBase + knowledgeStore+"/files?id=<"+mentionUri+".naf>";
        return str;
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


        //String url = "http://www.newsreader-project.eu/data/cars/2004/10/18/4DKT-30W0-00S0-W39B.xml";
        //String url = "http://en.wikinews.org/wiki/Mexican_president_defends_emigration.naf";
/*
        String url = "https://knowledgestore2.fbk.eu/nwr/wikinews-new/files?id=<http://en.wikinews.org/wiki/Mexican_president_defends_emigration.naf>";
        try {
            getNafFile(url, "test.naf");
        } catch (Exception e) {
            e.printStackTrace();
        }
*/

        try {
            System.out.println(getNafFile("https://knowledgestore2.fbk.eu/nwr/cars3/files?id=%3Chttp%3A%2F%2Fwww.newsreader-project.eu%2Fdata%2Fcars%2F2004%2F10%2F18%2F4DKT-30W0-00S0-W39B.xml.naf%3E", "test.naf"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Integer climax = 1+climaxIndex.size()-climaxIndex.indexOf(sentenceNr);
     Float size = 1+Float.valueOf(((float)((5*climaxIndex.size()-5*climaxIndex.indexOf(sentenceNr))/(float)climaxIndex.size())));
     //
     */

    static void createRawTextIndexFromMentionsOrg (ArrayList<JSONObject> objects, JSONObject timeLineObject, String KS) {
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.get(i);
            try {
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mObject  = mentions.getJSONObject(j);
                    //System.out.println("mObject.toString() = " + mObject.toString());
                    //mObject.toString() = {"char":["23","29"],"uri":["http://en.wikinews.org/wiki/Porsche_and_Volkswagen_automakers_agree_to_merger"]}
                    //String uString = mObject.getString("uri");
                    String uString = mObject.getJSONArray("uri").getString(0);
                    String nafURI = makeRequestUrl(KS, uString);
                    try {
                        String rawtext = MentionResolver.getNafContent(nafURI);
                        JSONObject jsonSnippetObject = new JSONObject();
                        jsonSnippetObject.put("uri", uString);
                        jsonSnippetObject.put("text", rawtext);
                        timeLineObject.append("sources", jsonSnippetObject);
                        System.out.println("rawtext = " + rawtext);
                    } catch (Exception e) {
                        e.printStackTrace();
                        JSONObject jsonSnippetObject = new JSONObject();
                        jsonSnippetObject.put("uri", uString);
                        jsonSnippetObject.put("text", "BLAHBLAH");
                        timeLineObject.append("sources", jsonSnippetObject);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    static void createRawTextIndexFromMentions (ArrayList<JSONObject> objects, JSONObject timeLineObject, String KS) {
        ArrayList<String> sourceUriList = new ArrayList<String>();
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.get(i);
            try {
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mObject  = mentions.getJSONObject(j);
                    //System.out.println("mObject.toString() = " + mObject.toString());
                    //mObject.toString() = {"char":["23","29"],"uri":["http://en.wikinews.org/wiki/Porsche_and_Volkswagen_automakers_agree_to_merger"]}
                    //String uString = mObject.getString("uri");
                    String uString = mObject.getJSONArray("uri").getString(0);
                    if (!sourceUriList.contains(uString)) {
                        sourceUriList.add(uString);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < sourceUriList.size(); i++) {
            String sourceUri = sourceUriList.get(i);
            try {
                String nafURI = makeRequestUrl(KS, sourceUri);
                String rawtext = MentionResolver.getKSNafContent(nafURI);
                JSONObject jsonSnippetObject = new JSONObject();
                jsonSnippetObject.put("uri", sourceUri);
                jsonSnippetObject.put("text", rawtext);
                timeLineObject.append("sources", jsonSnippetObject);
                System.out.println("rawtext = " + rawtext);
            } catch (Exception e) {
               // e.printStackTrace();
                JSONObject jsonSnippetObject = new JSONObject();
                try {
                    jsonSnippetObject.put("uri", sourceUri);
                    jsonSnippetObject.put("text", "Source file not found");
                    timeLineObject.append("sources", jsonSnippetObject);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


}
