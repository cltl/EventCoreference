package eu.newsreader.eventcoreference.objects;

import com.hp.hpl.jena.query.Dataset;
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

    public void setCorefTargetsWithMentions(ArrayList<ArrayList<CorefTarget>> mentions) {
        for (int i = 0; i < mentions.size(); i++) {
            ArrayList<CorefTarget> targets = mentions.get(i);
            addCorefTargets(targets);
        }
    }

    public void addCorefTarget(CorefTarget corefTarget) {
        this.corefTargets.add(corefTarget);
    }

    public void addCorefTargets(ArrayList<CorefTarget> corefTargets) {
        for (int i = 0; i < corefTargets.size(); i++) {
            CorefTarget corefTarget = corefTargets.get(i);
            this.corefTargets.add(corefTarget);
        }
    }

    public void setCorefTargets(String baseUrl, ArrayList<CorefTarget> corefTargets) {
        for (int i = 0; i < corefTargets.size(); i++) {
            CorefTarget corefTarget = corefTargets.get(i);
            corefTarget.setId(baseUrl+"/"+corefTarget.getId());
        }
        this.corefTargets = corefTargets;
    }

    public void addCorefTarget(String baseUrl, CorefTarget corefTarget) {
        corefTarget.setId(baseUrl+"/"+corefTarget.getId());
        this.corefTargets.add(corefTarget);
    }

    public void addCorefTargets(String baseUrl, ArrayList<CorefTarget> corefTargets) {
        for (int i = 0; i < corefTargets.size(); i++) {
            CorefTarget corefTarget = corefTargets.get(i);
            corefTarget.setId(baseUrl+"/"+corefTarget.getId());
            this.corefTargets.add(corefTarget);
        }
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


    public Property getSemRelationType (String type) {
        if (type.equalsIgnoreCase("hassemtime")) {
            return Sem.hasTime;
        }
        else if (type.equalsIgnoreCase("hassemplace")) {
            return Sem.hasPlace;
        }
        else if (type.equalsIgnoreCase("hassemactor")) {
            return Sem.hasActor;
        }
        else {
            return Sem.hasSubType;
        }
    }

    public void addToJenaDataSet (Dataset ds, Model provenanceModel) {

        Model relationModel = ds.getNamedModel("http://www.newsreader-project.eu/relation/"+this.id);

        Resource subject = relationModel.createResource(this.getSubject());
        Resource object = relationModel.createResource(this.getObject());
        Property semProperty = getSemRelationType(this.getPredicate());
        subject.addProperty(semProperty, object);


        Resource provenanceResource = provenanceModel.createResource("http://www.newsreader-project.eu/relation/"+this.id);
        for (int i = 0; i < corefTargets.size(); i++) {
                CorefTarget corefTarget = corefTargets.get(i);
                Property property = provenanceModel.createProperty("gaf:denotedBy");
                Resource targerResource = provenanceModel.createResource("nwr:"+corefTarget.getId());
                provenanceResource.addProperty(property, targerResource);
        }
    }

    public SemRelation (SemRelation semRelation) {
        this.setSubject(semRelation.getSubject());
        this.setObject(semRelation.getObject());
        this.setPredicate(semRelation.getPredicate());
        this.setCorefTargets(semRelation.getCorefTarget());
    }

    public boolean match (SemRelation semRelation) {
        if (!this.getSubject().equals(semRelation.getSubject())) {
            return  false;
        }
        if (!this.getObject().equals(semRelation.getObject())) {
            return  false;
        }
        if (!this.getPredicate().equals(semRelation.getPredicate())) {
            return  false;
        }
        return true;
    }
}
