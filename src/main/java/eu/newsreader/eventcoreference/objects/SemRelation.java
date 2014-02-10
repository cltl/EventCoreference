package eu.newsreader.eventcoreference.objects;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import eu.newsreader.eventcoreference.naf.ResourcesUri;

import java.util.ArrayList;
import java.util.HashMap;

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
    //private ArrayList<CorefTarget> corefTargets;
    private ArrayList<NafMention> nafMentions;


    public void SemRelation () {

    }

    public SemRelation() {
        this.nafMentions = new ArrayList<NafMention>();
        //this.corefTargets = new ArrayList<CorefTarget>();
        this.id = "";
        this.object = "";
        this.predicate = "";
        this.subject = "";
    }

    public ArrayList<NafMention> getNafMentions() {
        return nafMentions;
    }

    public void setNafMentions(ArrayList<NafMention> nafMentions) {
        this.nafMentions = nafMentions;
    }

    public void addMention(NafMention mention) {
            this.nafMentions.add(mention);
    }

    public void addMentions(ArrayList<NafMention> mentions) {
        for (int i = 0; i < mentions.size(); i++) {
            NafMention nafMention = mentions.get(i);
            this.addMention(nafMention);
        }
    }


    /*    public ArrayList<CorefTarget> getCorefTarget() {
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
    }*/

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

        Model relationModel = ds.getNamedModel(this.id);

        Resource subject = relationModel.createResource(this.getSubject());
        if (this.getPredicate().equalsIgnoreCase("nwr:hasFactBankValue")) {
            Property factProperty = relationModel.createProperty(this.getPredicate());
          //  Resource object = (Resource) relationModel.createLiteral(this.getObject());
            subject.addProperty(factProperty, this.getObject());
        }
        else {
            Resource object = relationModel.createResource(this.getObject());
            Property semProperty = getSemRelationType(this.getPredicate());
            subject.addProperty(semProperty, object);
        }


        Resource provenanceResource = provenanceModel.createResource(this.id);

        for (int i = 0; i < nafMentions.size(); i++) {
            NafMention nafMention = nafMentions.get(i);
            Property property = provenanceModel.createProperty(ResourcesUri.gaf+"denotedBy");
            Resource targetResource = provenanceModel.createResource(nafMention.toString());
            provenanceResource.addProperty(property, targetResource);

        }
    }

    public void addToJenaDataSet (Dataset ds, Model provenanceModel,
                                  HashMap<String, SourceMeta> sourceMetaHashMap) {

        Model relationModel = ds.getNamedModel(this.id);

        Resource subject = relationModel.createResource(this.getSubject());
        if (this.getPredicate().equalsIgnoreCase("nwr:hasFactBankValue")) {
            Property factProperty = relationModel.createProperty(this.getPredicate());
            //  Resource object = (Resource) relationModel.createLiteral(this.getObject());
            subject.addProperty(factProperty, this.getObject());
        }
        else {
            Resource object = relationModel.createResource(this.getObject());
            Property semProperty = getSemRelationType(this.getPredicate());
            subject.addProperty(semProperty, object);
        }



        Resource provenanceResource = provenanceModel.createResource(this.id);

        for (int i = 0; i < nafMentions.size(); i++) {
            NafMention nafMention = nafMentions.get(i);
            Property property = provenanceModel.createProperty(ResourcesUri.gaf+"denotedBy");
            Resource targetResource = provenanceModel.createResource(nafMention.toString());
            provenanceResource.addProperty(property, targetResource);

        }

        for (int i = 0; i < nafMentions.size(); i++) {
            NafMention nafMention = nafMentions.get(i);
            if (sourceMetaHashMap.containsKey(nafMention.getBaseUri())) {
                SourceMeta sourceMeta = sourceMetaHashMap.get(nafMention.getBaseUri());
                Property property = provenanceModel.createProperty(ResourcesUri.prov+"wasAttributedTo");
                if (!sourceMeta.getAuthor().isEmpty()) {
                    Resource targetResource = provenanceModel.createResource(ResourcesUri.nwrauthor+sourceMeta.getAuthor());
                    provenanceResource.addProperty(property, targetResource);
                }
                if (!sourceMeta.getOwner().isEmpty()) {
                    Resource targetResource = provenanceModel.createResource(ResourcesUri.nwrsourceowner+sourceMeta.getOwner());
                    provenanceResource.addProperty(property, targetResource);
                }
            }
            else {
              //  System.out.println("No meta nafMention.getBaseUri() = " + nafMention.getBaseUri());
            }
        }
    }
     //nwr:hasFactBankValue

    public void addToJenaDataSet (Dataset ds, Model relationModel, Model provenanceModel) {



        Model namedRelation = ds.getNamedModel(this.id);

        Resource subject = namedRelation.createResource(this.getSubject());
        Resource object = namedRelation.createResource(this.getObject());
        Property semProperty = getSemRelationType(this.getPredicate());
        subject.addProperty(semProperty, object);
        relationModel.add(namedRelation);

        Resource provenanceResource = provenanceModel.createResource(this.id);

        for (int i = 0; i < nafMentions.size(); i++) {
            NafMention nafMention = nafMentions.get(i);
            Property property = provenanceModel.createProperty(ResourcesUri.gaf+"denotedBy");
            Resource targetResource = provenanceModel.createResource(nafMention.toString());
            provenanceResource.addProperty(property, targetResource);

        }
    }



    public void addToJenaDataSet (Model relationModel, Model provenanceModel) {

        Resource subject = relationModel.createResource(this.getSubject());
        Resource object = relationModel.createResource(this.getObject());
        Property semProperty = getSemRelationType(this.getPredicate());
        subject.addProperty(semProperty, object);


        Resource provenanceResource = provenanceModel.createResource(this.id);

        for (int i = 0; i < nafMentions.size(); i++) {
            NafMention nafMention = nafMentions.get(i);
            Property property = provenanceModel.createProperty(ResourcesUri.gaf+"denotedBy");
            Resource targetResource = provenanceModel.createResource(nafMention.toString());
            provenanceResource.addProperty(property, targetResource);

        }
    }


    public SemRelation (SemRelation semRelation) {
        this.setSubject(semRelation.getSubject());
        this.setObject(semRelation.getObject());
        this.setPredicate(semRelation.getPredicate());
        //this.setCorefTargets(semRelation.getCorefTarget());
        this.setNafMentions(semRelation.getNafMentions());
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
