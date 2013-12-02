package eu.newsreader.eventcoreference.objects;

import com.hp.hpl.jena.rdf.model.*;
import eu.kyotoproject.kaf.CorefTarget;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/15/13
 * Time: 5:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class SemRelation {
/*
    <semRelation id="TOPIC_44_EVENT_COREFERENCE_CORPUS/2413"
    predicate="semHasTime"
    subject="TOPIC_44_EVENT_COREFERENCE_CORPUS/e53"
    object="TOPIC_44_EVENT_COREFERENCE_CORPUS/t246">

    <target id ="TOPIC_44_EVENT_COREFERENCE_CORPUS/TOPIC_8_EVENT_COREFERENCE_CORPUS/s19"/>
    </semRelation>
*/
    private String id;
    private String predicate;
    private String subject;
    private String object;
    private ArrayList<CorefTarget> corefTargets;


    public void SemRelation () {

    }

    public SemRelation() {
        this.corefTargets = new ArrayList<CorefTarget>();
        this.id = "";
        this.object = "";
        this.predicate = "";
        this.subject = "";
    }

    public ArrayList<CorefTarget> getCorefTarget() {
        return corefTargets;
    }

    public void setCorefTargets(ArrayList<CorefTarget> corefTargets) {
        this.corefTargets = corefTargets;
    }

    public void addCorefTarget(CorefTarget corefTarget) {
        this.corefTargets.add(corefTarget);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Statement toJenaRdfStatement () {
        Model model = ModelFactory.createDefaultModel();
        Resource subject = model.createResource(this.getSubject());
        Property predicate = model.createProperty(this.getId());
        RDFNode object = model.createResource(this.getObject());

        Statement statement = model.createStatement(subject, predicate, object);
        return statement;
    }
}
