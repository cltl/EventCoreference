package eu.newsreader.eventcoreference.storyline;

import eu.newsreader.eventcoreference.input.GetNAF;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;

/**
 * Created by piek on 09/02/16.
 */
public class MentionResolver {
    final String USER_AGENT = "Mozilla/5.0";
    public static String serviceBase = "https://knowledgestore2.fbk.eu/";
    public static String serviceEndpoint = "https://knowledgestore2.fbk.eu/nwr/cars3/files?id=";
    public static String user = "nwr_partner";
    public static String pass = "ks=2014!";


    public static String getKSNafContent(String urlString) throws Exception {
        String content = "";
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass.toCharArray());
            }
        });
        content = GetNAF.getFile(urlString);
        return content;
    }

    public static String makeRequestUrl (String knowledgeStore, String mentionUri) {
        //https://knowledgestore2.fbk.eu/nwr/wikinews-new/files?id=<
        //knowledgestore2.fbk.eu/nwr/wikinews-new
       // knowledgeStore = "nwr/wikinews-new";
        String str = serviceBase + knowledgeStore+"/files?id=<"+mentionUri+".naf>";
        return str;
    }



    static void createRawTextIndexFromMentions (ArrayList<JSONObject> objects, JSONObject timeLineObject, String KS) {
        ArrayList<String> sourceUriList = new ArrayList<String>();
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.get(i);
            try {
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mObject  = mentions.getJSONObject(j);
                    String uString = mObject.getJSONArray("uri").getString(0);
                    if (!sourceUriList.contains(uString)) {
                        sourceUriList.add(uString);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Getting sourcedocuments for unique sources = " + sourceUriList.size());
        long startTime = System.currentTimeMillis();
        Authenticator.setDefault(new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass.toCharArray());
            }
        });
        for (int i = 0; i < sourceUriList.size(); i++) {
            String sourceUri = sourceUriList.get(i);
            try {
                String nafURI = makeRequestUrl(KS, sourceUri);
                String text = GetNAF.getFile(nafURI);
                JSONObject jsonSnippetObject = new JSONObject();
                jsonSnippetObject.put("uri", sourceUri);
                jsonSnippetObject.put("text", text);
                timeLineObject.append("sources", jsonSnippetObject);
            } catch (Exception e) {
               // e.printStackTrace();
                JSONObject jsonSnippetObject = new JSONObject();
                try {
                    jsonSnippetObject.put("uri", sourceUri);
                    jsonSnippetObject.put("text", "NAF file not found");
                    timeLineObject.append("sources", jsonSnippetObject);
                } catch (JSONException e1) {
                   // e1.printStackTrace();
                }
            }
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time elapsed to get sources from KS:");
        System.out.println(estimatedTime/1000.0);

    }


}
