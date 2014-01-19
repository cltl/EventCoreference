package eu.newsreader.eventcoreference.util;

import java.util.ArrayList;

/**
 * Created by piek on 1/3/14.
 */
public class SemUtilObject {


    private String uri;
    private ArrayList<String> types;
    private ArrayList<LabelCount> labels;
    private Integer dispersion;
    private Integer mentions;
    private Integer singletons;
    public ArrayList<String> mentionStrings;
    public ArrayList<String> sourceStrings;

    public SemUtilObject() {
        this.uri = "";
        this.types = new ArrayList<String>();
        this.labels =  new ArrayList<LabelCount>();
        this.dispersion = 0;
        this.mentions = 0;
        this.singletons = 0;
        this.mentionStrings = new ArrayList<String>();
        this.sourceStrings = new ArrayList<String>();
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
        if (!this.types.contains(type)) {
            this.types.add(type);
        }
    }

    public ArrayList<LabelCount> getLabels() {
        return labels;
    }

    public void setLabels(ArrayList<LabelCount> labels) {
        this.labels = labels;
    }

    public void addLabels(String label) {
        boolean match = false;
        for (int i = 0; i < labels.size(); i++) {
            LabelCount labelCount = labels.get(i);
           // System.out.println("label = " + label);
           // System.out.println("labelCount.getLabel() = " + labelCount.getLabel());
            if (labelCount.getLabel().equals(label)) {
                labelCount.addCnt(1);
                match = true;
                break;
            }
        }
        if (!match) {
            LabelCount labelCount = new LabelCount(label, 1);
            this.labels.add(labelCount);
        }
    }

    public void addLabelCount(LabelCount label) {
        boolean match = false;
        for (int i = 0; i < labels.size(); i++) {
            LabelCount labelCount = labels.get(i);
            if (labelCount.getLabel().equals(label.getLabel())) {
                labelCount.addCnt(label.getCnt());
                match = true;
                break;
            }
        }
        if (!match) {
            this.labels.add(label);
        }
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
            LabelCount labelCount = semUtilObject.getLabels().get(i);
            this.addLabelCount(labelCount);
        }
        for (int i = 0; i < semUtilObject.getTypes().size(); i++) {
            String s = semUtilObject.getTypes().get(i);
            this.addTypes(s);
        }
        for (int i = 0; i < semUtilObject.mentionStrings.size(); i++) {
            String s = semUtilObject.mentionStrings.get(i);
            if (!this.mentionStrings.contains(s)) {
                this.mentionStrings.add(s);
            }
        }
        for (int i = 0; i < semUtilObject.sourceStrings.size(); i++) {
            String s = semUtilObject.sourceStrings.get(i);
            if (!this.sourceStrings.contains(s)) {
                this.sourceStrings.add(s);
            }
        }
        this.addDispersion(semUtilObject.getDispersion());
        this.addMentions(semUtilObject.getMentions());
    }


    public String toStatString () {
        String str = this.getUri()+"\t"+this.getMentions()+"\t"+this.getDispersion()+"\t"+this.getLabels().size()+"\t"+this.getTypes().size();
        return str;
    }
}
