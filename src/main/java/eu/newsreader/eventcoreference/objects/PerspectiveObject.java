package eu.newsreader.eventcoreference.objects;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import eu.kyotoproject.kaf.KafFactuality;
import eu.kyotoproject.kaf.KafParticipant;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import eu.newsreader.eventcoreference.naf.ResourcesUri;
import eu.newsreader.eventcoreference.util.Util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by piek on 16/03/15.
 */
public class PerspectiveObject {

    private NafMention nafMention;
    private String documentUri;
    private String eventString;
    private String predicateId;
    private ArrayList<String> predicateSpanIds;
    private ArrayList<KafSense> predicateConcepts;
    private KafParticipant source;
    private KafParticipant target;
    private SemActor sourceEntity;
    private ArrayList<NafMention> targetEventMentions;


    public PerspectiveObject() {
        this.predicateSpanIds = new ArrayList<String>();
        this.documentUri = "";
        this.eventString = "";
        this.nafMention = new NafMention();
        this.predicateConcepts = new ArrayList<KafSense>();
        this.predicateId = "";
        this.source = new KafParticipant();
        this.sourceEntity = new SemActor();
        this.target = new KafParticipant();
        this.targetEventMentions = new ArrayList<NafMention>();
    }

/*    public void getFactValue () {
        if (!nafMention.getFactuality().getPrediction().isEmpty()) {
            Util.addMentionToFactRelations(nafMention, factRelations, nafMention.getFactuality().getPrediction(), baseUrl, semObject.getId());
        }
    }*/

    public ArrayList<String> getPredicateSpanIds() {
        return predicateSpanIds;
    }

    public void setPredicateSpanIds(ArrayList<String> predicateSpanIds) {
        this.predicateSpanIds = predicateSpanIds;
    }

    public String getDocumentUri() {
        return documentUri;
    }

    public void setDocumentUri(String documentUri) {
        this.documentUri = documentUri;
    }

    public String getEventString() {
        return eventString;
    }

    public void setEventString(String eventString) {
        this.eventString = eventString;
    }

    public NafMention getNafMention() {
        return nafMention;
    }

    public void setNafMention(String baseUri, KafSaxParser kafSaxParser, ArrayList<String> eventTermIds) {
        ArrayList<String> termIds = eventTermIds;
        for (int j = 0; j < source.getSpanIds().size(); j++) {
            String s = source.getSpanIds().get(j);
            termIds.add(s);
        }
        for (int j = 0; j < target.getSpanIds().size(); j++) {
            String s = target.getSpanIds().get(j);
            termIds.add(s);
        }
        this.nafMention = Util.getNafMentionForTermIdArrayList(baseUri, kafSaxParser, termIds);
    }

    public ArrayList<KafSense> getPredicateConcepts() {
        return predicateConcepts;
    }

    public void setPredicateConcepts(ArrayList<KafSense> predicateConcepts) {
        this.predicateConcepts = predicateConcepts;
    }

    public void addPredicateConcepts(KafSense predicateConcept) {
        this.predicateConcepts.add(predicateConcept);
    }

    public String getPredicateId() {
        return predicateId;
    }

    public void setPredicateId(String predicateId) {
        this.predicateId = predicateId;
    }

    public KafParticipant getSource() {
        return source;
    }

    public void setSource(KafParticipant source) {
        this.source = source;
    }

    public SemActor getSourceEntity() {
        return sourceEntity;
    }

    public void setSourceEntity(SemActor sourceEntity) {
        this.sourceEntity = sourceEntity;
    }

    public KafParticipant getTarget() {
        return target;
    }

    public void setTarget(KafParticipant target) {
        this.target = target;
    }

    public ArrayList<NafMention> getTargetEventMentions() {
        return targetEventMentions;
    }

    public void setTargetEventMentions(ArrayList<NafMention> targetEventMentions) {
        this.targetEventMentions = targetEventMentions;
    }

    public void addTargetEventMention(NafMention targetEventMention) {
        this.targetEventMentions.add(targetEventMention);
    }

    public String toString () {
        String str = "";
        str += "perspective uri = " +documentUri + predicateId+","+source.getId()+","+target.getId()+"\n";
        str += "eventString = " + eventString+"\n";
        str += "mention = " + nafMention+"\n";

        str += "source = " + source.getTokenString()+"\n";
        if (!sourceEntity.getURI().isEmpty()) {
            str += "sourceEntity = " + sourceEntity.getURI()+"\n";
        }

        str += "target = " + target.getTokenString()+"\n";
        if (targetEventMentions.size()>0) {
            for (int i = 0; i < targetEventMentions.size(); i++) {
                NafMention mention = targetEventMentions.get(i);
                str += "target event mention = " + mention+"\n";
                for (int j = 0; j < mention.getFactuality().size(); j++) {
                    KafFactuality kafFactuality = mention.getFactuality().get(j);
                    str += "fv = " + kafFactuality.getPrediction() + "\n";
                }
            }
        }
        for (int i = 0; i < nafMention.getFactuality().size(); i++) {
            KafFactuality kafFactuality = nafMention.getFactuality().get(i);
            str += "perspective = "+ kafFactuality.getPrediction()+"\n";
        }
        return str;
    }

    public void addToJenaDataSet (Dataset ds) {

        /*
        :NGZetscheAtt {
nwr:/data/cars/2003/01/01/47VH-FG30-010D-Y3YG.naf#mention_pr_16 nwr:hasAttribution nwrontology:attrPOSCERTNF
.
nwr:/data/cars/2003/01/01/47VH-FG30-010D-Y3YG.naf#mention_pr_16_r1
nwr:hasAttribution nwrontology:attrPOSCERTNF .
nwr:/data/cars/2003/01/01/47VH-FG30-010D-Y3YG.naf#mention_pr_16_r2
nwr:hasAttribution nwrontology:attrPOSCERTNF .
:NGChryslerAtt nwr:hasAttribution nwrontology:attrPOSCERTNF .
}
         */

        if ((targetEventMentions.size()>0)) {
       // if ((targetEventMentions.size()>0) && !sourceEntity.getURI().isEmpty()) {
            String id = documentUri + predicateId+","+source.getId()+","+target.getId();

            /// we create a named graph to store all the attribution values for all the target events associated with the perspective
            Model namedGraph = ds.getNamedModel(id);
            for (int i = 0; i < targetEventMentions.size(); i++) {
                NafMention mention = targetEventMentions.get(i);
                /// the mention of the target event is the subject
                Resource subject = namedGraph.createResource(mention.toString());
              //  System.out.println("mention.toStringFull() = " + mention.toStringFull());
                if (mention.getFactuality().size()==0) {
                    //// default perspective
                    Property property = namedGraph.createProperty(ResourcesUri.gaf , "hasAttribution");
                    Property factPropertyValue = namedGraph.createProperty(ResourcesUri.nwrvalue + "defaultAttr="+KafFactuality.defaultAttribution);
                    subject.addProperty(property, factPropertyValue); /// creates the literal as value
                }
                else {
                    for (int j = 0; j < mention.getFactuality().size(); j++) {
                        KafFactuality kafFactuality = mention.getFactuality().get(j);
                        Property property = namedGraph.createProperty(ResourcesUri.gaf, "hasAttribution");
                        Property factPropertyValue = namedGraph.createProperty(ResourcesUri.nwrvalue + "attr="+kafFactuality.getPrediction());
                        subject.addProperty(property, factPropertyValue); /// creates the literal as value
                    }
                }
            }
            //:NGZetsche prov:wasAttributedTo dbpedia:Dieter_Zetsche .
            Resource subject = ds.getDefaultModel().createResource(id);
            Property property = ds.getDefaultModel().createProperty(ResourcesUri.prov, "wasAttributedTo");
            if (sourceEntity.getURI().isEmpty()) {
                String uri = "";
                try {
                    uri = URLEncoder.encode(source.getTokenString(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    //  e.printStackTrace();
                }
                if (!uri.isEmpty()) {
                    Resource targetResource = ds.getDefaultModel().createResource(uri);
                    subject.addProperty(property, targetResource);
                }
            }
            else {
                Resource targetResource = ds.getDefaultModel().createResource(sourceEntity.getURI());
                subject.addProperty(property, targetResource);
            }
        }

    }

}
