package eu.newsreader.eventcoreference.objects;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by piek on 12/2/14.
 */
public class JsonEvent {
    public static JSONObject createJsonDate (String startDate,
                                              String endDate,
                                              String text) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("startDate", startDate);
        jsonObject.put("endDate", endDate);
        jsonObject.put("text", text);
        //jsonObject.append("asset", new JSONObject("asset"));
        return jsonObject;
    }

    public static JSONObject createJsonDate (String startDate,
                                              String endDate,
                                              String headline,
                                              String text,
                                              String tag,
                                              String classname,
                                              JSONObject asset) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("startDate", startDate);
        jsonObject.put("endDate", endDate);
        jsonObject.put("headline", headline);
        jsonObject.put("text", text);
        jsonObject.put("tag", tag);
        jsonObject.put("classname", classname);
        jsonObject.append("asset", asset);
        return jsonObject;
    }

    public static JSONObject createJsonAsset (String media,
                                              String thumbnail,
                                              String caption,
                                              String credit) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("media", media);
        jsonObject.put("thumbnail", thumbnail);
        jsonObject.put("caption", caption);
        jsonObject.put("credit", credit);
        return jsonObject;
    }

    public static JSONObject createTimeLineProperty (String headline,
                                             String text)  throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("headline", headline);
        jsonObject.put("type", "default");
        jsonObject.put("text", text);
        return jsonObject;

    }
}


/**
 *
 *
 *
 * "timeline":
 {
 "headline":"The Main Timeline Headline Goes here",
 "type":"default",
 "text":"<p>Intro body text goes here, some HTML is ok</p>",
 "asset": {
 "media":"http://yourdomain_or_socialmedialink_goes_here.jpg",
 "credit":"Credit Name Goes Here",
 "caption":"Caption text goes here"
 },
 "date": [
 {
 "startDate":"2011,12,10",
 "endDate":"2011,12,11",
 "headline":"Headline Goes Here",
 "text":"<p>Body text goes here, some HTML is OK</p>",
 "tag":"This is Optional",
 "classname":"optionaluniqueclassnamecanbeaddedhere",
 "asset": {
 "media":"http://twitter.com/ArjunaSoriano/status/164181156147900416",
 "thumbnail":"optional-32x32px.jpg",
 "credit":"Credit Name Goes Here",
 "caption":"Caption text goes here"
 }
 }
 ],
 "era": [
 {
 "startDate":"2011,12,10",
 "endDate":"2011,12,11",
 "headline":"Headline Goes Here",
 "text":"<p>Body text goes here, some HTML is OK</p>",
 "tag":"This is Optional"
 }

 ]
 */
