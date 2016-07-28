package eu.newsreader.eventcoreference.storyline;

import eu.kyotoproject.kaf.KafWordForm;
import eu.newsreader.eventcoreference.input.GetNAF;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.*;

/**
 * Created by piek on 09/02/16.
 */
public class MentionResolver {
    static final int nContext = 75;
    final String USER_AGENT = "Mozilla/5.0";
    public static String serviceBase = "https://knowledgestore2.fbk.eu/";
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

    public static String makeRequestUrl (String SERVICE, String knowledgeStore, String mentionUri) {
        //https://knowledgestore2.fbk.eu/nwr/wikinews-new/files?id=<
        //knowledgestore2.fbk.eu/nwr/wikinews-new
       // knowledgeStore = "nwr/wikinews-new";
        String str = "";
        if (knowledgeStore.isEmpty()) {
            str = SERVICE+"/files?id=<"+mentionUri+".naf>";
        }
        else {
            str = SERVICE + "/"+knowledgeStore+"/files?id=<"+mentionUri+".naf>";
        }
        return str;
    }

    public static String makeTextRequestUrl (String knowledgeStore, String mentionUri) {
        //https://knowledgestore2.fbk.eu/nwr/wikinews-new/files?id=<
        //knowledgestore2.fbk.eu/nwr/wikinews-new
       // knowledgeStore = "nwr/wikinews-new";
        String str = "";
        if (knowledgeStore.isEmpty()) {
            str = serviceBase+"/files?id=<"+mentionUri+".naf>";
        }
        else {
            str = serviceBase + knowledgeStore+"/files?id=<"+mentionUri+".naf>";
        }
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
                    String uString = mObject.getString("uri");
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
                String nafURI = makeTextRequestUrl(KS, sourceUri);
                //String nafURI = makeRequestUrl(KS, sourceUri);
                //String text = GetNAF.getFile(nafURI);
                String text = GetNAF.getText(nafURI);
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

    static void createRawTextIndexFromMentions (ArrayList<JSONObject> objects, JSONObject timeLineObject, String SERVICE, String KS, final String KSuser, final String KSpass) {
        ArrayList<String> sourceUriList = new ArrayList<String>();
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.get(i);
            try {
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mObject  = mentions.getJSONObject(j);
                    String uString = mObject.getString("uri");
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
                return new PasswordAuthentication(KSuser, KSpass.toCharArray());
            }
        });
        for (int i = 0; i < sourceUriList.size(); i++) {
            String sourceUri = sourceUriList.get(i);
            try {
                String nafURI = makeRequestUrl(SERVICE, KS, sourceUri);
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

    static ArrayList<JSONObject> createRawTextIndexFromMentions (ArrayList<JSONObject> objects, String KS) {
        ArrayList<JSONObject> sourceObjects = new ArrayList<JSONObject>();
        ArrayList<String> sourceUriList = new ArrayList<String>();
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.get(i);
            try {
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mObject  = mentions.getJSONObject(j);
                    String uString = mObject.getString("uri");
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
                String nafURI = makeTextRequestUrl(KS, sourceUri);
                //String nafURI = makeRequestUrl(KS, sourceUri);
                //String text = GetNAF.getFile(nafURI);
                String text = GetNAF.getText(nafURI);
                JSONObject jsonSnippetObject = new JSONObject();
                jsonSnippetObject.put("uri", sourceUri);
                jsonSnippetObject.put("text", text);
                sourceObjects.add(jsonSnippetObject);
            } catch (Exception e) {
               // e.printStackTrace();
                JSONObject jsonSnippetObject = new JSONObject();
                try {
                    jsonSnippetObject.put("uri", sourceUri);
                    jsonSnippetObject.put("text", "NAF file not found");
                    sourceObjects.add(jsonSnippetObject);
                } catch (JSONException e1) {
                   // e1.printStackTrace();
                }
            }
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time elapsed to get sources from KS:");
        System.out.println(estimatedTime/1000.0);
        return sourceObjects;
    }

    static ArrayList<JSONObject> createRawTextIndexFromMentions (ArrayList<JSONObject> objects, String SERVICE, String KS, final String KSuser, final String KSpass) {
        ArrayList<JSONObject> sourceObjects = new ArrayList<JSONObject>();
        ArrayList<String> sourceUriList = new ArrayList<String>();
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.get(i);
            try {
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mObject  = mentions.getJSONObject(j);
                    String uString = mObject.getString("uri");
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
                return new PasswordAuthentication(KSuser, KSpass.toCharArray());
            }
        });
        for (int i = 0; i < sourceUriList.size(); i++) {
            String sourceUri = sourceUriList.get(i);
            try {
                String nafURI = makeRequestUrl(SERVICE, KS, sourceUri);
                String text = GetNAF.getFile(nafURI);
                JSONObject jsonSnippetObject = new JSONObject();
                jsonSnippetObject.put("uri", sourceUri);
                jsonSnippetObject.put("text", text);
                sourceObjects.add(jsonSnippetObject);
            } catch (Exception e) {
               // e.printStackTrace();
                JSONObject jsonSnippetObject = new JSONObject();
                try {
                    jsonSnippetObject.put("uri", sourceUri);
                    jsonSnippetObject.put("text", "NAF file not found");
                    sourceObjects.add(jsonSnippetObject);
                } catch (JSONException e1) {
                   // e1.printStackTrace();
                }
            }
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time elapsed to get sources from KS:");
        System.out.println(estimatedTime/1000.0);
        return sourceObjects;
    }


    static void createSnippetIndexFromMentions (ArrayList<JSONObject> objects,
                                                String pathToTokenIndex) throws JSONException {
        HashMap<String, ArrayList<String>> sourceUriList = new HashMap<String, ArrayList<String>>();
        Vector<String> urls = new Vector<String>();
        HashMap<String, Integer> eventIdObjectMap = new HashMap<String, Integer>();
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.get(i);
            try {
                String eventId = jsonObject.getString("instance");
                eventIdObjectMap.put(eventId, i);
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mObject  = mentions.getJSONObject(j);
                    String uString = mObject.getString("uri");
                    if (!urls.contains(uString)) {
                        urls.add(uString);
                    }
                    if (sourceUriList.containsKey(uString)) {
                        ArrayList<String> eventIds = sourceUriList.get(uString);
                        if (!eventIds.contains(eventId)) {
                            eventIds.add(eventId);
                            sourceUriList.put(uString, eventIds);
                        }
                    }
                    else {
                        ArrayList<String> eventIds = new ArrayList<String>();
                        eventIds.add(eventId);
                        sourceUriList.put(uString, eventIds);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        System.out.println(" * Getting sourcedocuments for unique sources = " + sourceUriList.size());
        long startTime = System.currentTimeMillis();

        NafTokenLayerIndex nafTokenLayerIndex = new NafTokenLayerIndex(urls);
        //NafTokenLayerIndex nafTokenLayerIndex = new NafTokenLayerIndex();
        nafTokenLayerIndex.parseFile(pathToTokenIndex);
        //System.out.println("nafTokenLayerIndex.tokenMap.size() = " + nafTokenLayerIndex.tokenMap.size());
        Set keySet = sourceUriList.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            /// we first get the tokens for the single NAF file.
            /// next we serve each event with mentions in this NAF file
            //System.out.println("key = " + key);
            //key = http://www.coprocom.go.cr/resoluciones/2013/voto-18-2013-%20recurso-reconsideracion-lanco-sumario.pdf
            ArrayList<KafWordForm> wordForms = null;
            if (nafTokenLayerIndex.tokenMap.containsKey(key)) {
                wordForms = nafTokenLayerIndex.tokenMap.get(key);
            }
            ArrayList<String> eventIds = sourceUriList.get(key);
            for (int i = 0; i < eventIds.size(); i++) {
                String eventId = eventIds.get(i);
                //   System.out.println("eventId = " + eventId);
                int idx = eventIdObjectMap.get(eventId);
                JSONObject eventObject = objects.get(idx);
                JSONArray mentions = (JSONArray) eventObject.get("mentions");
                //  System.out.println("mentions.length() = " + mentions.length());
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mObject  = mentions.getJSONObject(j);
                    String uString = mObject.getString("uri");
                    JSONArray offsetArray = mObject.getJSONArray("char");
                    Integer offsetBegin =  null;
                    try {
                        offsetBegin = Integer.parseInt(offsetArray.getString(0));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (uString.equals(key) && offsetBegin!=null &wordForms!=null) {
                        for (int k = 0; k < wordForms.size(); k++) {
                            KafWordForm kafWordForm = wordForms.get(k);
                            Integer kafOffset = Integer.parseInt(kafWordForm.getCharOffset());
                            if (kafOffset>offsetBegin) {
                                break;
                            }
                            if (kafOffset.equals(offsetBegin)) {
                                // we found the sentence and the word, now make the snippet
                                String wf = kafWordForm.getWf();
                                String sentenceId = kafWordForm.getSent();
                                String newText = kafWordForm.getWf();
                                if (k > 0) {
                                    int m = k-1;
                                    KafWordForm kafWordForm2 = wordForms.get(m);
                                    String sentenceId2 = kafWordForm2.getSent();
                                    while (sentenceId2.equals(sentenceId)) {
                                        newText = kafWordForm2.getWf() + " " + newText;
                                        m--;
                                        if (m >= 0) {
                                            kafWordForm2 = wordForms.get(m);
                                            sentenceId2 = kafWordForm2.getSent();
                                        }
                                        else {
                                            break;
                                        }
                                    }
                                }
                                offsetBegin = newText.lastIndexOf(wf);
                                int offsetEnd = offsetBegin + wf.length();
                                if ((k + 1) < wordForms.size()) {
                                    int m = k + 1;
                                    KafWordForm kafWordForm2 = wordForms.get(m);
                                    String sentenceId2 = sentenceId;
                                    while (sentenceId2.equals(sentenceId)) {
                                        newText = newText + " " + kafWordForm2.getWf();
                                        m++;
                                        if (m < wordForms.size()) {
                                            kafWordForm2 = wordForms.get(m);
                                            sentenceId2 = kafWordForm2.getSent();
                                        } else {
                                            break;
                                        }
                                    }

                                }
                               /* System.out.println("offsetBegin = " + offsetBegin);
                                System.out.println("offsetEnd = " + offsetEnd);
                                System.out.println("final newText = " + newText);*/
                                mObject.append("snippet", newText);
                                mObject.append("snippet_char", offsetBegin);
                                mObject.append("snippet_char", offsetEnd);

                                break;

                            } else {
                                ///not the word
                            }
                        }
                    }
                    else if (wordForms==null || offsetBegin==null) {
                        mObject.append("snippet", "Could not find the original text.");
                        mObject.append("snippet_char", 0);
                        mObject.append("snippet_char", 0);
                    }
                }
            }
            //break;
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(" * Time elapsed to get text snippets from KS:"+estimatedTime/1000.0);


    }

    static void createSnippetIndexFromMentions (ArrayList<JSONObject> objects,
                                                String SERVICE,
                                                String KS,
                                                final String KSuser,
                                                final String KSpass) throws JSONException {
        HashMap<String, ArrayList<String>> sourceUriList = new HashMap<String, ArrayList<String>>();
        HashMap<String, Integer> eventIdObjectMap = new HashMap<String, Integer>();
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.get(i);
            try {
                String eventId = jsonObject.getString("instance");
                eventIdObjectMap.put(eventId, i);
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mObject  = mentions.getJSONObject(j);
                    String uString = mObject.getString("uri");
                    if (sourceUriList.containsKey(uString)) {
                        ArrayList<String> eventIds = sourceUriList.get(uString);
                        if (!eventIds.contains(eventId)) {
                            eventIds.add(eventId);
                            sourceUriList.put(uString, eventIds);
                        }
                    }
                    else {
                        ArrayList<String> eventIds = new ArrayList<String>();
                        eventIds.add(eventId);
                        sourceUriList.put(uString, eventIds);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        System.out.println(" * Getting sourcedocuments for unique sources = " + sourceUriList.size());
        long startTime = System.currentTimeMillis();
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(KSuser, KSpass.toCharArray());
            }
        });
        Set keySet = sourceUriList.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            /// we first get the tokens for the single NAF file.
            /// next we serve each event with mentions in this NAF file
            //System.out.println("key = " + key);
            ArrayList<KafWordForm> wordForms = null;
            try {
                String nafURI = makeRequestUrl(SERVICE, KS, key);
              //  System.out.println("nafURI = " + nafURI);
                wordForms = GetNAF.getNafWordFormsFile(nafURI);
/*                for (int i = 0; i < wordForms.size(); i++) {
                    KafWordForm kafWordForm = wordForms.get(i);
                    System.out.println("kafWordForm.getCharOffset() = " + kafWordForm.getCharOffset());
                    System.out.println("kafWordForm.toString() = " + kafWordForm.toString());
                }*/
            } catch (Exception e) {
                // e.printStackTrace();
            }
            ArrayList<String> eventIds = sourceUriList.get(key);
            for (int i = 0; i < eventIds.size(); i++) {
                String eventId = eventIds.get(i);
             //   System.out.println("eventId = " + eventId);
                int idx = eventIdObjectMap.get(eventId);
                JSONObject eventObject = objects.get(idx);
                JSONArray mentions = (JSONArray) eventObject.get("mentions");
              //  System.out.println("mentions.length() = " + mentions.length());
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mObject  = mentions.getJSONObject(j);
                    String uString = mObject.getString("uri");
                    JSONArray offsetArray = mObject.getJSONArray("char");
                    Integer offsetBegin =  null;
                    try {
                        offsetBegin = Integer.parseInt(offsetArray.getString(0));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (uString.equals(key) && offsetBegin!=null &wordForms!=null) {
                        for (int k = 0; k < wordForms.size(); k++) {
                            KafWordForm kafWordForm = wordForms.get(k);
                            Integer kafOffset = Integer.parseInt(kafWordForm.getCharOffset());
                            if (kafOffset>offsetBegin) {
                                break;
                            }
                            if (kafOffset.equals(offsetBegin)) {
                                // we found the sentence and the word, now make the snippet
                                String wf = kafWordForm.getWf();
                                String sentenceId = kafWordForm.getSent();
                                String newText = kafWordForm.getWf();
                                if (k > 0) {
                                    int m = k-1;
                                    KafWordForm kafWordForm2 = wordForms.get(m);
                                    String sentenceId2 = kafWordForm2.getSent();
                                    while (sentenceId2.equals(sentenceId)) {
                                        newText = kafWordForm2.getWf() + " " + newText;
                                        m--;
                                        if (m >= 0) {
                                            kafWordForm2 = wordForms.get(m);
                                            sentenceId2 = kafWordForm2.getSent();
                                        }
                                        else {
                                            break;
                                        }
                                    }
                                }
                                offsetBegin = newText.lastIndexOf(wf);
                                int offsetEnd = offsetBegin + wf.length();
                                if ((k + 1) < wordForms.size()) {
                                    int m = k + 1;
                                    KafWordForm kafWordForm2 = wordForms.get(m);
                                    String sentenceId2 = sentenceId;
                                    while (sentenceId2.equals(sentenceId)) {
                                        newText = newText + " " + kafWordForm2.getWf();
                                        m++;
                                        if (m < wordForms.size()) {
                                            kafWordForm2 = wordForms.get(m);
                                            sentenceId2 = kafWordForm2.getSent();
                                        } else {
                                            break;
                                        }
                                    }

                                }
                               /* System.out.println("offsetBegin = " + offsetBegin);
                                System.out.println("offsetEnd = " + offsetEnd);
                                System.out.println("final newText = " + newText);*/
                                mObject.append("snippet", newText);
                                mObject.append("snippet_char", offsetBegin);
                                mObject.append("snippet_char", offsetEnd);

                                break;

                            } else {
                                ///not the word
                            }
                        }
                    }
                    else if (wordForms==null || offsetBegin==null) {
                        mObject.append("snippet", "Could not find the original text.");
                        mObject.append("snippet_char", 0);
                        mObject.append("snippet_char", 0);
                    }
                }
            }
            //break;
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(" * Time elapsed to get text snippets from KS:"+estimatedTime/1000.0);
    }

 /*   static void addSnippetToEventMentions (JSONObject jsonObject) throws JSONException {
            JSONArray myMentions = jsonObject.getJSONArray("mentions");
            for (int m = 0; m < myMentions.length(); m++) {
                JSONObject mentionObject = (JSONObject) myMentions.get(m);
                String uri = mentionObject.getString("uri");
                JSONArray offsetArray = mentionObject.getJSONArray("char");
                Integer offsetBegin =  null;
                try {
                    offsetBegin = Integer.parseInt(offsetArray.getString(0));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                Integer offsetEnd = null;
                try {
                    offsetEnd = Integer.parseInt(offsetArray.getString(1));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (rawTextMap.containsKey(uri) && offsetBegin!=null && offsetEnd!=null) {
                    String text = rawTextMap.get(uri);
                    String newText = text;
                    int offsetLength = (offsetEnd-offsetBegin);
                    int textStart = 0;
                    int textEnd = offsetBegin+offsetLength+nContext;
                    //System.out.println("offsetBegin = " + offsetBegin);
                    //System.out.println("offsetEnd = " + offsetEnd);
                    //System.out.println("offsetLength = " + offsetLength);
                    if (offsetBegin>nContext) {
                        textStart = offsetBegin-nContext;
                        int idx = text.lastIndexOf(" ",textStart);
                        if (idx>-1 && idx<textStart) {
                            //  System.out.println("idx = " + idx);
                            //  System.out.println("textStart = " + textStart);
                            if (offsetBegin>(textStart-idx)+nContext) {
                                offsetBegin = (textStart-idx)+nContext;
                                textStart = idx;
                            }
                            else {
                                textStart = 0;
                            }
                        }
                        else {
                            offsetBegin = nContext;
                        }
                        offsetEnd = offsetBegin+offsetLength;
                    }
                    int idx = text.indexOf(" ", textEnd);
                    if (idx>-1) textEnd = idx;
                    if (text.length()<=textEnd) {
                        textEnd = text.length()-1;
                    }

                    newText = text.substring(textStart, textEnd);
                    if (offsetEnd>=newText.length()) {
                        offsetBegin = newText.length()-offsetLength;
                        offsetEnd = newText.length()-1;
                    }
                    try {
                        mentionObject.append("snippet", newText);
                        mentionObject.append("snippet_char", offsetBegin);
                        mentionObject.append("snippet_char", offsetEnd);
                        //jsonObject.append("mentions", newMentionObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    // jsonObject.append("mentions", mentionObject);
                }
            }
    }*/

    static public void ReadFileToUriTextArrayList(String fileName, ArrayList<JSONObject> events) {
        HashMap<String, String> rawTextMap = new HashMap<String, String>();
        if (new File(fileName).exists() ) {
            try {
                InputStream fis = new FileInputStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis, "UTF8");
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                String uri = "";
                String text = "";

                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        if (inputLine.startsWith("http:")) {
                            //  System.out.println("inputLine = " + inputLine);
                            String[] fields = inputLine.split("\t");
                            if (fields.length > 1) {
                                if (!uri.isEmpty() && !text.isEmpty()) {
                                    rawTextMap.put(uri, text);
                                    uri = ""; text = "";
                                }
                                uri = fields[0];
                                text = fields[1];
                            }
                        }
                        else {
                            text += "\n"+inputLine;
                        }
                    }
                }
                if (!uri.isEmpty() && !text.isEmpty()) {
                    rawTextMap.put(uri, text);
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < events.size(); i++) {
                JSONObject jsonObject = events.get(i);

                try {
                    JSONArray myMentions = jsonObject.getJSONArray("mentions");
                    for (int m = 0; m < myMentions.length(); m++) {
                        JSONObject mentionObject = (JSONObject) myMentions.get(m);
                        String uri = mentionObject.getString("uri");
                        JSONArray offsetArray = mentionObject.getJSONArray("char");
                        Integer offsetBegin =  null;
                        try {
                            offsetBegin = Integer.parseInt(offsetArray.getString(0));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        Integer offsetEnd = null;
                        try {
                            offsetEnd = Integer.parseInt(offsetArray.getString(1));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        if (rawTextMap.containsKey(uri) && offsetBegin!=null && offsetEnd!=null) {
                            String text = rawTextMap.get(uri);
                            String newText = text;
                            int offsetLength = (offsetEnd-offsetBegin);
                            int textStart = 0;
                            int textEnd = offsetBegin+offsetLength+nContext;
                            //System.out.println("offsetBegin = " + offsetBegin);
                            //System.out.println("offsetEnd = " + offsetEnd);
                            //System.out.println("offsetLength = " + offsetLength);
                            if (offsetBegin>nContext) {
                                textStart = offsetBegin-nContext;
                                int idx = text.lastIndexOf(" ",textStart);
                                if (idx>-1 && idx<textStart) {
                                    //  System.out.println("idx = " + idx);
                                    //  System.out.println("textStart = " + textStart);
                                    if (offsetBegin>(textStart-idx)+nContext) {
                                        offsetBegin = (textStart-idx)+nContext;
                                        textStart = idx;
                                    }
                                    else {
                                        textStart = 0;
                                    }
                                }
                                else {
                                    offsetBegin = nContext;
                                }
                                offsetEnd = offsetBegin+offsetLength;
                            }
                            int idx = text.indexOf(" ", textEnd);
                            if (idx>-1) textEnd = idx;
                            if (text.length()<=textEnd) {
                                textEnd = text.length()-1;
                            }

                            newText = text.substring(textStart, textEnd);
                            if (offsetEnd>=newText.length()) {
                                offsetBegin = newText.length()-offsetLength;
                                offsetEnd = newText.length()-1;
                            }
/*
                            System.out.println("newText = " + newText);
                            System.out.println("offsetBegin = " + offsetBegin);
                            System.out.println("offsetEnd = " + offsetEnd);
*/
                            // System.out.println("mention = " + newText.substring(offsetBegin, offsetEnd));
                            //System.out.println("newText = " + newText);
                            try {
                                mentionObject.append("snippet", newText);
                                mentionObject.append("snippet_char", offsetBegin);
                                mentionObject.append("snippet_char", offsetEnd);
                                //jsonObject.append("mentions", newMentionObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            // jsonObject.append("mentions", mentionObject);
                        }
                    }
                    // jsonObject.put("mentions", newMentions);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
        else {
            System.out.println("Cannot find fileName = " + fileName);
        }
    }

    static public ArrayList<JSONObject> ReadFileToUriTextArrayList(String fileName) {
        ArrayList<JSONObject> vector = new ArrayList<JSONObject>();
        if (new File(fileName).exists() ) {
            try {
                InputStream fis = new FileInputStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis, "UTF8");
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                String uri = "";
                String text = "";

                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        if (inputLine.startsWith("http:")) {
                            //  System.out.println("inputLine = " + inputLine);
                            String[] fields = inputLine.split("\t");
                            if (fields.length > 1) {
                                if (!uri.isEmpty() && !text.isEmpty()) {
                                    try {
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("uri", uri);
                                        jsonObject.put("text", text);
                                        vector.add(jsonObject);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    uri = ""; text = "";
                                }
                                uri = fields[0];
                                text = fields[1];
                                //System.out.println("string = " + string);

                            }
                        }
                        else {
                            text += "\n"+inputLine;
                        }
                    }
                }
                if (!uri.isEmpty() && !text.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("uri", uri);
                        jsonObject.put("text", text);
                        vector.add(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    uri = ""; text = "";
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Cannot find fileName = " + fileName);
        }
        return vector;
    }

    static public ArrayList<JSONObject> ReadFileToUriTextArrayListOrg(String fileName) {
        ArrayList<JSONObject> vector = new ArrayList<JSONObject>();
        if (new File(fileName).exists() ) {
            try {
/*                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(fileName), "UTF8"));*/

                InputStream fis = new FileInputStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis, "UTF8");
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        //  System.out.println("inputLine = " + inputLine);
                        String[] fields = inputLine.split("\t");
                        if (fields.length > 1) {
                            String uri = fields[0];
                            String text = fields[1];
                            //System.out.println("string = " + string);
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("uri", uri);
                                jsonObject.put("text", text);
                                vector.add(jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Cannot find fileName = " + fileName);
        }
        return vector;
    }

}
