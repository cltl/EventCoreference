package eu.newsreader.eventcoreference.objects;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import eu.newsreader.eventcoreference.naf.ResourcesUri;
import eu.newsreader.eventcoreference.util.RoleLabels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/15/13
 * Time: 5:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class SemRelation implements Serializable {
/*
    <semRelation id="TOPIC_44_EVENT_COREFERENCE_CORPUS/2413"
    predicate="semHasTime"
    subject="TOPIC_44_EVENT_COREFERENCE_CORPUS/e53"
    object="TOPIC_44_EVENT_COREFERENCE_CORPUS/t246">

    <target id ="TOPIC_44_EVENT_COREFERENCE_CORPUS/TOPIC_8_EVENT_COREFERENCE_CORPUS/s19"/>
    </semRelation>
*/
    private String id;
    private ArrayList<String> predicates;
    private String subject;
    private String object;
    private ArrayList<NafMention> nafMentions;



    public SemRelation() {
        this.nafMentions = new ArrayList<NafMention>();
        this.id = "";
        this.object = "";
        this.subject = "";
        this.predicates = new ArrayList<String>();
    }

    public ArrayList<String> getPredicates() {
        return predicates;
    }

    public void setPredicates(ArrayList<String> predicates) {
        this.predicates = predicates;
    }

    public void addPredicate(String predicate) {
        if (!containsPredicateIgnoreCase(predicate)) {
            this.predicates.add(predicate);
        }
    }

    public boolean containsPredicateIgnoreCase (String predicate) {
        for (int i = 0; i < predicates.size(); i++) {
            String pred = predicates.get(i);
            if (pred.equalsIgnoreCase(predicate)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<NafMention> getNafMentions() {
        return nafMentions;
    }

    public void setNafMentions(ArrayList<NafMention> nafMentions) {
        this.nafMentions = nafMentions;
    }

    public void addMention(NafMention mention) {
        if (!mention.hasMention(this.getNafMentions())) {
            this.nafMentions.add(mention);
        }
    }

    public void addMentions(ArrayList<NafMention> mentions) {
        for (int i = 0; i < mentions.size(); i++) {
            NafMention nafMention = mentions.get(i);
            this.addMention(nafMention);
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }



    public Property getSemRelationProperty (String type) {
        if (type.equals(Sem.hasTime.getLocalName())) {
            return Sem.hasTime;
        }
        if (type.equals(Sem.hasAtTime.getLocalName())) {
            return Sem.hasAtTime;
        }
        else if (type.equals(Sem.hasBeginTime.getLocalName())) {
            return Sem.hasBeginTime;
        }
        else if (type.equals(Sem.hasEndTime.getLocalName())) {
            return Sem.hasEndTime;
        }
        else if (type.equals(Sem.hasFutureTime.getLocalName())) {
            return Sem.hasFutureTime;
        }
        else if (type.equals(Sem.hasEarliestBeginTime.getLocalName())) {
            return Sem.hasEarliestBeginTime;
        }
        else if (type.equals(Sem.hasEarliestEndTime.getLocalName())) {
            return Sem.hasEarliestEndTime;
        }
        else if (type.equals(Sem.hasFutureTimeStamp.getLocalName())) {
            return Sem.hasFutureTimeStamp;
        }
        else if (type.equals(Sem.hasBeginTimeStamp.getLocalName())) {
            //BiographyNet uses sem:hasBeginTimeStamp
            return Sem.hasBeginTimeStamp;
        }
        else if (type.equals(Sem.hasEndTimeStamp.getLocalName())) {
            return Sem.hasEndTimeStamp;
        }
        else if (type.equals(Sem.hasEarliestBeginTimeStamp.getLocalName())) {
          //  return Sem.hasFutureTimeStamp;
            return Sem.hasEarliestBeginTimeStamp;
        }
        else if (type.equals(Sem.hasEarliestEndTimeStamp.getLocalName())) {
          //  return Sem.hasFutureTimeStamp;
            return Sem.hasEarliestEndTimeStamp;
        }
        else if (type.equals(Sem.hasPlace.getLocalName())) {
            return Sem.hasPlace;
        }
        else if (type.equals(Sem.hasActor.getLocalName())) {
            return Sem.hasActor;
        }
        else {
            return Sem.hasSubType;
        }
    }

    static public boolean isTemporalSemRelationProperty (String type) {
        if (type.endsWith(Sem.hasTime.getLocalName())) {
            return true;
        }
        if (type.endsWith(Sem.hasAtTime.getLocalName())) {
            return true;
        }
        else if (type.endsWith(Sem.hasBeginTime.getLocalName())) {
            return true;
        }
        else if (type.endsWith(Sem.hasEndTime.getLocalName())) {
            return true;
        }
        else if (type.endsWith(Sem.hasFutureTime.getLocalName())) {
            return true;
        }
        else if (type.endsWith(Sem.hasEarliestBeginTime.getLocalName())) {
            return true;
        }
        else if (type.equals(Sem.hasEarliestEndTime.getLocalName())) {
            return true;
        }
        else if (type.endsWith(Sem.hasFutureTimeStamp.getLocalName())) {
            return true;
        }
        else if (type.endsWith(Sem.hasBeginTimeStamp.getLocalName())) {
            //BiographyNet uses sem:hasBeginTimeStamp
            return true;
        }
        else if (type.endsWith(Sem.hasEndTimeStamp.getLocalName())) {
            return true;
        }
        else if (type.endsWith(Sem.hasEarliestBeginTimeStamp.getLocalName())) {
          //  return Sem.hasFutureTimeStamp;
            return true;
        }
        else if (type.endsWith(Sem.hasEarliestEndTimeStamp.getLocalName())) {
          //  return Sem.hasFutureTimeStamp;
            return true;
        }
        return false;
    }


    public String getRoleRelation (String role) {
        String  rel = "";
        String [] fields = role.split(":");
        if (fields.length==2) {
            String source = fields[0].trim();
            String value = fields[1].trim();
            if (source.isEmpty()) {
                rel = ResourcesUri.pb+value;
            }
            else if (source.equalsIgnoreCase("propbank")) {
                if (value.indexOf("@")==-1) {
                    /// skipping propbank/say.01@1
                    rel = ResourcesUri.pb + value;
                }
            }
            else if (source.equalsIgnoreCase("framenet")) {
                rel = ResourcesUri.fn+value;
            }
            else if (source.equalsIgnoreCase("verbnet")) {
             //   rel = ResourcesUri.vn+value;
            }
            else if (source.equalsIgnoreCase("eso")) {
                //// IN CASE THE ESO CONSTRAINTS DO NOT INCLUDE THE VERB TYPE
                int idx = value.indexOf("@");
                //e.g Removing@translocation-theme
                if (idx>-1) {
                    value = value.substring(idx+1);
                }
                rel = ResourcesUri.eso+value;
            }
            else if (source.equalsIgnoreCase("nombank")) {
             //   rel = ResourcesUri.nb+value;
            }
        }
        else {
            if (role.indexOf("@")==-1) {
                /// skipping propbank/say.01@1
                rel = ResourcesUri.pb + role;
            }
        }
        return rel;
    }

    public String getFramenetRoleRelation (String role) {
        String  rel = "";
        String [] fields = role.split(":");
        if (fields.length==2) {
            String source = fields[0].trim();
            String value = fields[1].trim();
            if (source.equalsIgnoreCase("framenet")) {
                rel = ResourcesUri.fn+value;
            }
        }
        return rel;
    }

    public String getPropBankRoleRelation (String role) {
        String  rel = "";
        String [] fields = role.split(":");
        if (fields.length==2) {
            String source = fields[0].trim();
            String value = fields[1].trim();
            if (source.equalsIgnoreCase("propbank")) {
                if (value.indexOf("@")==-1) {
                    /// skipping propbank/say.01@1
                    rel = ResourcesUri.pb + value;
                }
            }
        }
        return rel;
    }


    public void addToJenaDataSet (Dataset ds, Model provenanceModel) {

        Model relationModel = ds.getNamedModel(this.id);

        Resource subject = relationModel.createResource(this.getSubject());
        Resource object = relationModel.createResource(this.getObject());


        /// since we no longer distinguish places from actors, we now check the predicates for propbank AM-LOC
        /// if so we use sem:hasPlace otherwise we take the semType value from the hassem predicate
        Property semProperty = null;
        boolean PLACE = false;
        boolean ACTOR = false;
        for (int i = 0; i < predicates.size(); i++) {
            String predicate = predicates.get(i);
            if (predicate.equalsIgnoreCase("hasFactBankValue")) {
                Property factProperty = relationModel.createProperty(ResourcesUri.nwrvalue + predicate);
                subject.addProperty(factProperty, this.getObject()); /// creates the literal as value
            }else {
                semProperty = getSemRelationProperty(predicate);
                if (isTemporalSemRelationProperty(predicate)) {
                        subject.addProperty(semProperty, object);
                        subject.addProperty(Sem.hasTime, object); /// additional hasTime relation to generalize
                }
                else {
                    if (!semProperty.getLocalName().equals(Sem.hasActor.getLocalName()) &&
                        !semProperty.getLocalName().equals(Sem.hasPlace.getLocalName())) {
                        predicate = getRoleRelation(predicate);
                        if (!predicate.isEmpty()) {
                            Property srlProperty = relationModel.createProperty(predicate);
                            subject.addProperty(srlProperty, object);

                            String predicateValue = predicate;
                            int idx = predicateValue.lastIndexOf("/");
                            if (idx > -1) predicateValue = predicateValue.substring(idx + 1);
                            if (RoleLabels.isLOCATION(predicateValue)) {
                                PLACE = true;
                            } else {
                                ACTOR = true;
                            }
                        }
                    }
                }

            }
        }
        //// if at the end of the loop we detected a location role
        if (PLACE) {
                subject.addProperty(Sem.hasPlace, object);
        }
        //// if at the end of the loop we detect an actor role
        if (ACTOR) {
            subject.addProperty(Sem.hasActor, object);
        }

        Resource provenanceResource = provenanceModel.createResource(this.id);

        for (int i = 0; i < nafMentions.size(); i++) {
            NafMention nafMention = nafMentions.get(i);
            Property property = provenanceModel.createProperty(ResourcesUri.gaf+"denotedBy");
            Resource targetResource = provenanceModel.createResource(nafMention.toString());
            provenanceResource.addProperty(property, targetResource);

        }
    }
    public void addToJenaDataSetSimple (Dataset ds) {

        Model relationModel = ds.getNamedModel(this.id);

        Resource subject = relationModel.createResource(this.getSubject());
        Resource object = relationModel.createResource(this.getObject());


        /// since we no longer distinguish places from actors, we now check the predicates for propbank AM-LOC
        /// if so we use sem:hasPlace otherwise we take the semType value from the hassem predicate
        Property semProperty = null;
        for (int i = 0; i < predicates.size(); i++) {
            String predicate = predicates.get(i);
            semProperty = getSemRelationProperty(predicate);
            if (isTemporalSemRelationProperty(predicate)) {
                    subject.addProperty(semProperty, object);
            }
            else {
                if (!semProperty.getLocalName().equals(Sem.hasActor.getLocalName()) &&
                    !semProperty.getLocalName().equals(Sem.hasPlace.getLocalName())) {
                    predicate = getRoleRelation(predicate);
                    System.err.println("predicate:"+predicate);
                    //predicate = getFramenetRoleRelation(predicates.get(i));
                    if (!predicate.isEmpty()) {
                        Property srlProperty = relationModel.createProperty(predicate);
                        subject.addProperty(srlProperty, object);
                    }
                    /*else {
                        predicate = getPropBankRoleRelation(predicates.get(i));
                        if (!predicate.isEmpty()) {
                            Property srlProperty = relationModel.createProperty(predicate);
                            subject.addProperty(srlProperty, object);
                        }
                    }*/
                }
            }
        }

    }

    public void addToJenaDataSet (Dataset ds, Model provenanceModel,
                                  HashMap<String, SourceMeta> sourceMetaHashMap) {

        addToJenaDataSet(ds, provenanceModel);
        Resource provenanceResource = provenanceModel.createResource(this.id);

        for (int i = 0; i < nafMentions.size(); i++) {
            NafMention nafMention = nafMentions.get(i);

            //http://www.newsreader-project.eu/data/cars/2003/10/10/49RC-C8V0-01D6-W1FX.xml
            //http://www.newsreader-project.eu/data/cars/2003/01/01/47KF-XY70-010F-G3GG.xml
            //System.out.println("nafMention.getBaseUriWithoutId() = " + nafMention.getBaseUriWithoutId());
            if (sourceMetaHashMap.containsKey(nafMention.getBaseUriWithoutId())) {
                //System.out.println("nafMention.getBaseUriWithoutId() = " + nafMention.getBaseUriWithoutId());

                SourceMeta sourceMeta = sourceMetaHashMap.get(nafMention.getBaseUriWithoutId());
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
                //System.out.println("No meta nafMention.getBaseUriWithoutId() = " + nafMention.getBaseUriWithoutId());
                //System.out.println("sourceMetaHashMap = " + sourceMetaHashMap.size());
            }
        }
    }




    public SemRelation (SemRelation semRelation) {
        this.setSubject(semRelation.getSubject());
        this.setObject(semRelation.getObject());
        this.setPredicates(semRelation.getPredicates());
        this.setNafMentions(semRelation.getNafMentions());
    }

    public boolean match (SemRelation semRelation) {
        if (!this.getSubject().equals(semRelation.getSubject())) {
            return  false;
        }
        if (!this.getObject().equals(semRelation.getObject())) {
            return  false;
        }
        for (int i = 0; i < predicates.size(); i++) {
            String pred1 =  predicates.get(i);
            boolean matchPredicate = false;
            for (int j = 0; j < semRelation.getPredicates().size(); j++) {
                String pred2 = semRelation.getPredicates().get(j);
                if (pred1.equalsIgnoreCase(pred2)) {
                    matchPredicate = true;
                    break;
                }
            }
            return matchPredicate;
        }
        return true;
    }

    public String toString () {
        String str = "";
        for (int i = 0; i < predicates.size(); i++) {
            String pred = predicates.get(i);
            str +=  subject+"#"+pred+"#"+object;
        }
        return str;
    }
}
