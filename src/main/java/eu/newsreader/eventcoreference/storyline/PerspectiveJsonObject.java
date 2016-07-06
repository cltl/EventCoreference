package eu.newsreader.eventcoreference.storyline;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by piek on 14/04/16.
 */
public class PerspectiveJsonObject {

    private String event = "";
    private String mention = "";
    private ArrayList<String> attribution = new ArrayList<String>();
    private String cite = "";
    private String author = "";
    private String label = "";
    private String comment = "";
    private JSONObject targeEvent = new JSONObject();


    public PerspectiveJsonObject() {
        this.targeEvent = new JSONObject();
        this.attribution = new ArrayList<String>();
        this.author = "";
        this.cite = "";
        this.comment = "";
        this.event = "";
        this.label = "";
        this.mention = "";
    }

    public PerspectiveJsonObject(ArrayList<String> attributions,
                                 String author,
                                 String cite,
                                 String comment,
                                 String event,
                                 String label,
                                 String mention,
                                 JSONObject targeEvent) {
        this.attribution = attributions;
        this.author = author;
        this.cite = cite;
        this.comment = comment;
        this.event = event;
        this.label = label;
        this.mention = mention;
        this.targeEvent = targeEvent;
    }

    public ArrayList<String> getAttribution() {
        return attribution;
    }

    public void setAttribution(ArrayList<String> attribution) {
        this.attribution = attribution;
    }

    public void addAttribution(String attribution) {
        if (!this.attribution.contains(attribution.toLowerCase())) {
            this.attribution.add(attribution.toLowerCase());
        }
    }

    public void addAttribution(ArrayList<String> attributions) {
        for (int i = 0; i < attributions.size(); i++) {
            String a =  attributions.get(i).toLowerCase();
            this.addAttribution(a);

        }
    }

    public String getAuthor() {
        if (!author.isEmpty()) {
            int idx = author.lastIndexOf("/");
            if (idx > -1) { author =  author.substring(idx + 1); }
        }
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCite() {
        if (!cite.isEmpty()) {
            int idx = cite.lastIndexOf("/");
            if (idx > -1) { cite = cite.substring(idx + 1); }
        }
        return cite;
    }

    public void setCite(String cite) {
        this.cite = cite;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getMention() {
        return mention;
    }

    public void setMention(String mention) {
        this.mention = mention;
    }

    public JSONObject getTargeEvent() {
        return targeEvent;
    }

    public void setTargeEvent(JSONObject targeEvent) {
        this.targeEvent = targeEvent;
    }

    public String getSource () {
        String source = "";
        if (!cite.isEmpty()) {
            int idx = cite.lastIndexOf("/");
            if (idx > -1) { source = "cite:" + cite.substring(idx + 1); }
            else { source = "cite:" + cite; }
        }
        if (!author.isEmpty()) {
            int idx = author.lastIndexOf("/");
            if (idx > -1) { source = "author:" + author.substring(idx + 1); }
            else { source = "author:"+author;  }
        }
        return source;
    }

    public JSONObject getJSONObject () throws JSONException {
        JSONObject object = new JSONObject();
        if (attribution.contains("certain")) {
            object.put("certainty", "certain");
        }
        else if (attribution.contains("uncertain")) {
            object.put("certainty", "uncertain");
        }
        else {
            object.put("certainty", "certain");
        }

        if (attribution.contains("unlikely")) {
            object.put("possibility", "unlikely");
        }
        else if (attribution.contains("likely")) {
            object.put("possibility", "likely");
        }
        else {
            object.put("possibility", "likely");
        }

        if (attribution.contains("denial")) {
            object.put("belief", "deny");
        }
        else if (attribution.contains("deny")) {
            object.put("belief", "deny");
        }
        else if (attribution.contains("confirm")) {
            object.put("belief", "confirm");
        }

        if (attribution.contains("positive")) {
            object.put("sentiment", "positive");
        }
        else if (attribution.contains("negative")) {
            object.put("sentiment", "negative");
        }
        else {
            object.put("sentiment", "neutral");
        }

        if (attribution.contains("future")) {
            object.put("when", "future");
        }
        else if (attribution.contains("recent")) {
            object.put("when", "recent");
        }
        else if (attribution.contains("past")) {
            object.put("when", "past");
        }
        else {
            object.put("when", "recent");
        }
        return object;
    }
    public void setDefaultPerspectiveValue () {
        attribution.add("certain");
        attribution.add("confirm");
        attribution.add("recent");
    }

    static public ArrayList<String> normalizePerspectiveValue (String value) {
        ArrayList<String> normValues = new ArrayList<String>();
        String normValue = "";

        // if (!value.equals("u_u_u") && !value.equals("CERTAIN_NON_FUTURE_POS")) {

        //System.out.println("value = " + value);
        if (value.toLowerCase().equals("u_u_u") || (value.equals("u_u_u_u"))) {
            normValues.add("certain");
            normValues.add("confirm");
            normValues.add("recent");
        }
        else if (value.toLowerCase().indexOf("negative")>-1) {
            // normValue="-";
            // normValue=":(";
            normValue="negative";
            normValues.add(normValue);
        }
        else if (value.toLowerCase().indexOf("positive")>-1) {
            // normValue="+";
            //  normValue=":)";
            normValue="positive";
            normValues.add(normValue);
        }
        else {

            //// polarity
            if (value.toLowerCase().indexOf("_neg")>-1) {
              //  System.out.println("value = " + value);
                normValue= "deny";
                normValues.add(normValue);
            }
            else if (value.toLowerCase().indexOf("_pos")>-1) {
                normValue= "confirm";
                normValues.add(normValue);
            }
            else if (value.toLowerCase().indexOf("_underspecified")>-1) {
                normValue= "confirm";
                normValues.add(normValue);
            }



            //// tense
            if (value.toLowerCase().indexOf("non_future")>-1) {
                normValue= "now";
                normValues.add(normValue);
            }
            else if (value.toLowerCase().indexOf("future")>-1) {
                normValue= "future";
                normValues.add(normValue);
            }
            else if (value.toLowerCase().indexOf("past")>-1) {
                normValue= "past";
                normValues.add(normValue);
            }
            else if (value.toLowerCase().indexOf("recent")>-1) {
                normValue= "recent";
                normValues.add(normValue);
            }

            //// certainty
            if (value.toLowerCase().indexOf("uncertain")>-1) {
                normValue= "uncertain";
                normValues.add(normValue);
            }
            else if (value.toLowerCase().indexOf("certain")>-1) {
                normValue= "certain";
                normValues.add(normValue);
            }
            else if (value.toLowerCase().indexOf("improbable")>-1) {
                normValue= "certain";
                normValues.add(normValue);
            }
            else if (value.toLowerCase().indexOf("impossible")>-1) {
                normValue= "certain";
                normValues.add(normValue);
            }
            else if (value.toLowerCase().indexOf("possible")>-1) {
                normValue= "uncertain";
                normValues.add(normValue);
            }
            else if (value.toLowerCase().indexOf("probable")>-1) {
                normValue= "certain";
                normValues.add(normValue);
            }
        }

        //System.out.println("normValues = " + normValues.toString());
        return normValues;
    }

    /**
     * 118189       <factVal resource="factbank" value="CT+"/>
     4354       <factVal resource="factbank" value="CT-"/>
     1791       <factVal resource="factbank" value="PR+"/>
     51       <factVal resource="factbank" value="PR-"/>
     1932       <factVal resource="factbank" value="PS+"/>
     37       <factVal resource="factbank" value="PS-"/>
     23409       <factVal resource="factbank" value="Uu"/>
     122543       <factVal resource="nwr:attributionCertainty" value="CERTAIN"/>
     941       <factVal resource="nwr:attributionCertainty" value="POSSIBLE"/>
     1838       <factVal resource="nwr:attributionCertainty" value="PROBABLE"/>
     1551       <factVal resource="nwr:attributionCertainty" value="UNCERTAIN"/>
     22890       <factVal resource="nwr:attributionCertainty" value="UNDERSPECIFIED"/>
     5831       <factVal resource="nwr:attributionPolarity" value="NEG"/>
     120977       <factVal resource="nwr:attributionPolarity" value="POS"/>
     22955       <factVal resource="nwr:attributionPolarity" value="UNDERSPECIFIED"/>
     11947       <factVal resource="nwr:attributionTense" value="FUTURE"/>
     62878       <factVal resource="nwr:attributionTense" value="NON_FUTURE"/>
     74938       <factVal resource="nwr:attributionTense" value="UNDERSPECIFIED"/>
     */
}
