package eu.newsreader.eventcoreference.storyline;

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
        if (!this.attribution.contains(attribution)) {
            this.attribution.add(attribution);
        }
    }

    public void addAttribution(ArrayList<String> attributions) {
        for (int i = 0; i < attributions.size(); i++) {
            String a =  attributions.get(i);
            this.addAttribution(a);

        }
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCite() {
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
}
