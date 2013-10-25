package eu.newsreader.eventcoreference.objects;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 10/11/13
 * Time: 6:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class Triple {

    private String id;
    private String predicate;
    private String subject;
    private String object;
    private ArrayList<String> mentions;
    private String comment;

    public Triple() {
        this.id = "";
        this.object = "";
        this.predicate = "";
        this.subject = "";
        this.comment = "";
        this.mentions = new ArrayList<String>();
    }

    public Triple(String predicate, String subject, String object) {
        this.id = "";
        this.comment = "";
        this.object = object;
        this.predicate = predicate;
        this.subject = subject;
        this.mentions = new ArrayList<String>();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void addComment(String comment) {
        this.comment += comment;
    }

    public ArrayList<String> getMentions() {
        return mentions;
    }

    public void setMentions(ArrayList<String> mentions) {
        this.mentions = mentions;
    }

    public void addMentions(String mention) {
        this.mentions.add(mention);
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String toString () {
        String str  = "\t<semRelation id=\""+id+"\""+
                " predicate=\""+this.getPredicate()+"\""+
                " subject=\""+this.getSubject()+"\"" +
                " object=\""+this.getObject()+"\"" +
                ">\n";
       // str += "<!-- "+comment+" ->\n";
        for (int i = 0; i < mentions.size(); i++) {
            String s = mentions.get(i);
            str += "\t\t"+s+"\n";
        }
        str += "\t</semRelation>\n";
        return str;
    }
}
