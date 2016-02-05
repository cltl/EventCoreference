package eu.newsreader.eventcoreference.objects;

/**
 * Created by piek on 05/02/16.
 */
public class Triple {
    private String subject;
    private String predicate;
    private String object;

    public Triple() {
        this.subject = "";
        this.object = "";
        this.predicate = "";
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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
}
