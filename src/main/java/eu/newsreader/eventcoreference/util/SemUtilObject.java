package eu.newsreader.eventcoreference.util;

import java.util.ArrayList;

/**
 * Created by piek on 1/3/14.
 */
public class SemUtilObject {
    private String uri;
    private ArrayList<String> types;
    private ArrayList<String> labels;
    private Integer dispersion;
    private Integer mentions;
    private Integer singletons;

    public SemUtilObject() {
        this.uri = "";
        this.types = new ArrayList<String>();
        this.labels =  new ArrayList<String>();
        this.dispersion = 0;
        this.mentions = 0;
        this.singletons = 0;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public ArrayList<String> getTypes() {
        return types;
    }

    public void setTypes(ArrayList<String> types) {
        this.types = types;
    }

    public void addTypes(String type) {
        this.types.add(type);
    }

    public ArrayList<String> getLabels() {
        return labels;
    }

    public void setLabels(ArrayList<String> labels) {
        this.labels = labels;
    }

    public void addLabels(String label) {
        this.labels.add(label);
    }

    public Integer getDispersion() {
        return dispersion;
    }

    public void setDispersion(Integer dispersion) {
        this.dispersion = dispersion;
    }

    public void addDispersion(Integer dispersion) {
        this.dispersion += dispersion;
    }

    public Integer getMentions() {
        return mentions;
    }

    public void setMentions(Integer mentions) {
        this.mentions = mentions;
    }

    public void addMentions(Integer mentions) {
        this.mentions += mentions;
    }

    public Integer getSingletons() {
        return singletons;
    }

    public void setSingletons(Integer singletons) {
        this.singletons = singletons;
    }

    public void mergeObject(SemUtilObject semUtilObject) {
        for (int i = 0; i < semUtilObject.getLabels().size(); i++) {
            String s = semUtilObject.getLabels().get(i);
            this.addLabels(s);
        }
        for (int i = 0; i < semUtilObject.getTypes().size(); i++) {
            String s = semUtilObject.getTypes().get(i);
            this.addTypes(s);
        }
        this.addDispersion(semUtilObject.getDispersion());
        this.addMentions(semUtilObject.getMentions());
    }

    public String toStatString () {
        String str = this.getUri()+"\t"+this.getMentions()+"\t"+this.getDispersion()+"\t"+this.getLabels().size()+"\t"+this.getTypes().size();
        return str;
    }
}
