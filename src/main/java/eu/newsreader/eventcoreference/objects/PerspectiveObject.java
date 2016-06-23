package eu.newsreader.eventcoreference.objects;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;
import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.naf.ResourcesUri;
import eu.newsreader.eventcoreference.util.Util;
import org.openrdf.model.vocabulary.RDF;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by piek on 16/03/15.
 */
public class PerspectiveObject {

    private NafMention nafMention;
    private NafMention cueMention;
    private String documentUri;
    private String eventString;
    private String predicateId;
    private ArrayList<String> predicateSpanIds;
    private ArrayList<KafSense> predicateConcepts;
    private KafParticipant source;
    private KafParticipant target;
    private SemActor sourceEntity;
    private ArrayList<NafMention> targetEventMentions;


    public NafMention getCueMention() {
        return cueMention;
    }

    public void setCueMention(String baseUri, KafSaxParser kafSaxParser, ArrayList<String> eventTermIds) {
        this.cueMention = Util.getNafMentionForTermIdArrayList(baseUri, kafSaxParser, eventTermIds);
    }

    public void setNafMention(NafMention nafMention) {
        this.nafMention = nafMention;
    }

    public PerspectiveObject() {
        this.predicateSpanIds = new ArrayList<String>();
        this.documentUri = "";
        this.eventString = "";
        this.nafMention = new NafMention();
        this.cueMention = new NafMention();
        this.predicateConcepts = new ArrayList<KafSense>();
        this.predicateId = "";
        this.source = new KafParticipant();
        this.sourceEntity = new SemActor(SemObject.ENTITY);
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

    public void addToJenaDataSet1 (Dataset ds) {

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
                    Property property = namedGraph.createProperty(ResourcesUri.grasp , "hasAttribution");
                    Property factPropertyValue = namedGraph.createProperty(ResourcesUri.nwrvalue + "defaultAttr="+KafFactuality.defaultAttribution);
                    subject.addProperty(property, factPropertyValue); /// creates the literal as value
                }
                else {
                    for (int j = 0; j < mention.getFactuality().size(); j++) {
                        KafFactuality kafFactuality = mention.getFactuality().get(j);
                        Property property = namedGraph.createProperty(ResourcesUri.grasp, "hasAttribution");
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


    public void addToJenaDataSet (Model model, String ns, String attrId) {
        /*
        mentionId2      hasAttribution         attributionId1
                        gaf:generatedBy        mentionId3
        attributionId1  rdf:value              CERTAIN_POS_FUTURE
                        rdf:value              POSITIVE
                        prov:wasAttributedTo   doc-uri
                        gaf:wasAttributedTo    dbp:Zetsche

         */
        /*
http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=254,261
	gafAttribution:CERTAIN,NON_FUTURE
		http://dbpedia.org/resource/Caesars_Entertainment_Corporation ;
		gaf:generatedBy http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=201,209.


http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=201,209
			gafAttribution:CERTAIN,NON_FUTURE
				doc-uri;

doc-uri
	prov:wasAttributedTo author;
	prov:wasAttributedTo journal.
         */

        if ((targetEventMentions.size()>0)) {
            Resource factualityValue = null;
            ArrayList<KafFactuality> allFactualities = new ArrayList<KafFactuality>();
            for (int i = 0; i < targetEventMentions.size(); i++) {
                NafMention mention = targetEventMentions.get(i);
                for (int j = 0; j < mention.getFactuality().size(); j++) {
                    KafFactuality kafFactuality = mention.getFactuality().get(j);
                    allFactualities.add(kafFactuality);
                }
            }
            KafFactuality kafFactuality = NafMention.getDominantFactuality(allFactualities);

            if (allFactualities.size() == 0) {
                //// default perspective
                factualityValue = model.createResource(ResourcesUri.grasp+KafFactuality.defaultAttribution);
            }
            else {
                factualityValue = model.createResource(ResourcesUri.grasp+kafFactuality.getPrediction());
            }

            Resource sentimentValue = null;
            String sentiment = NafMention.getDominantOpinion(targetEventMentions);
            if (!sentiment.isEmpty()) {
                if (sentiment.equals("+")) {
                    sentiment = "positive";
                }
                else if (sentiment.equals("-")) {
                    sentiment ="negative";
                }
                sentimentValue = model.createResource(ResourcesUri.grasp+sentiment);
            }

            for (int i = 0; i < targetEventMentions.size(); i++) {
                NafMention mention = targetEventMentions.get(i);
                /// the mention of the target event is the subject
                Resource mentionSubject = model.createResource(mention.toString());
                mentionSubject.addProperty(RDFS.label, model.createLiteral(mention.getSentenceText()));

                Resource targetResource = null;
                if (sourceEntity.getURI().isEmpty()) {
                    String uri = "";
                    try {
                        uri = URLEncoder.encode(source.getTokenString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        //  e.printStackTrace();
                    }
                    if (!uri.isEmpty()) {
                        //targetResource = ds.getDefaultModel().createResource(uri);
                        targetResource = model.createResource(uri);
                    }
                }
                else {
                   // targetResource = ds.getDefaultModel().createResource(sourceEntity.getURI());
                    targetResource = model.createResource(sourceEntity.getURI());
                }
                if (targetResource!=null) {
                    Resource attributionSubject = model.createResource(attrId);
                    Property property = model.createProperty(ResourcesUri.grasp,"hasAttribution" );
                    mentionSubject.addProperty(property, attributionSubject);
                    if (!cueMention.toString().isEmpty()) {
                        property = model.createProperty(ResourcesUri.grasp, "generatedBy");
                        //Resource object = ds.getDefaultModel().createResource(this.cueMention.toString());
                        Resource object = model.createResource(this.cueMention.toString());
                        mentionSubject.addProperty(property, object);
                        mentionSubject.addProperty(RDFS.comment, model.createLiteral(cueMention.getSentenceText()));
                    }
                    property = model.createProperty(ns, "wasAttributedTo");
                    attributionSubject.addProperty(property, targetResource);
                    if (factualityValue!=null)  {
                        property = model.createProperty(RDF.NAMESPACE, RDF.VALUE.getLocalName());
                        //  System.out.println("kafFactuality = " + kafFactuality.getPrediction());
                        attributionSubject.addProperty(property, factualityValue);

                    }
                    if (sentimentValue!=null) {
                            property = model.createProperty(RDF.NAMESPACE, RDF.VALUE.getLocalName());
                            attributionSubject.addProperty(property, sentimentValue); /// creates the literal as value
                    }
                }
            }
        }
    }

    public void addToJenaDataSet2 (Model model, String ns, String attrId) {
        /*
        mentionId2      hasAttribution         attributionId1
                        gaf:generatedBy        mentionId3
        attributionId1  rdf:value              CERTAIN_POS_FUTURE
                        rdf:value              POSITIVE
                        prov:wasAttributedTo   doc-uri
                        gaf:wasAttributedTo    dbp:Zetsche

         */
        /*
http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=254,261
	gafAttribution:CERTAIN,NON_FUTURE
		http://dbpedia.org/resource/Caesars_Entertainment_Corporation ;
		gaf:generatedBy http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=201,209.


http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=201,209
			gafAttribution:CERTAIN,NON_FUTURE
				doc-uri;

doc-uri
	prov:wasAttributedTo author;
	prov:wasAttributedTo journal.
         */

        if ((targetEventMentions.size()>0)) {

         //   Model model = ds.getDefaultModel();
            for (int i = 0; i < targetEventMentions.size(); i++) {
                NafMention mention = targetEventMentions.get(i);
                /// the mention of the target event is the subject
                Resource mentionSubject = model.createResource(mention.toString());
                mentionSubject.addProperty(RDFS.label, model.createLiteral(mention.getSentenceText()));

                Resource targetResource = null;
                if (sourceEntity.getURI().isEmpty()) {
                    String uri = "";
                    try {
                        uri = URLEncoder.encode(source.getTokenString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        //  e.printStackTrace();
                    }
                    if (!uri.isEmpty()) {
                        //targetResource = ds.getDefaultModel().createResource(uri);
                        targetResource = model.createResource(uri);
                    }
                }
                else {
                   // targetResource = ds.getDefaultModel().createResource(sourceEntity.getURI());
                    targetResource = model.createResource(sourceEntity.getURI());
                }
                if (targetResource!=null) {
                    Resource attributionSubject = model.createResource(attrId);
                    Property property = model.createProperty(ResourcesUri.grasp,"hasAttribution" );
                    mentionSubject.addProperty(property, attributionSubject);
                    if (!cueMention.toString().isEmpty()) {
                        property = model.createProperty(ResourcesUri.grasp, "generatedBy");
                        //Resource object = ds.getDefaultModel().createResource(this.cueMention.toString());
                        Resource object = model.createResource(this.cueMention.toString());
                        mentionSubject.addProperty(property, object);
                        mentionSubject.addProperty(RDFS.comment, model.createLiteral(cueMention.getSentenceText()));
                    }
                    property = model.createProperty(ns, "wasAttributedTo");
                    attributionSubject.addProperty(property, targetResource);

                    if (mention.getFactuality().size() == 0) {
                        //// default perspective
                        property = model.createProperty(RDF.NAMESPACE, RDF.VALUE.getLocalName());
                        Resource factualityValue = model.createResource(ResourcesUri.grasp+KafFactuality.defaultAttribution);
                        attributionSubject.addProperty(property, factualityValue);
                    }
                    else {
                        KafFactuality kafFactuality = mention.getDominantFactuality();
                        property = model.createProperty(RDF.NAMESPACE, RDF.VALUE.getLocalName());
                        Resource factualityValue = model.createResource(ResourcesUri.grasp+kafFactuality.getPrediction());
                        //  System.out.println("kafFactuality = " + kafFactuality.getPrediction());
                        attributionSubject.addProperty(property, factualityValue);
/*
                        for (int j = 0; j < mention.getFactuality().size(); j++) {
                            KafFactuality kafFactuality = mention.getFactuality().get(j);
                            property = model.createProperty(RDF.NAMESPACE, RDF.VALUE.getLocalName());
                            Resource factualityValue = model.createResource(ResourcesUri.grasp+kafFactuality.getPrediction());
                          //  System.out.println("kafFactuality = " + kafFactuality.getPrediction());
                            attributionSubject.addProperty(property, factualityValue);
                        }
*/
                    }
                    if (mention.getOpinions().size()>0) {
                        for (int j = 0; j < mention.getOpinions().size(); j++) {
                            KafOpinion kafOpinion = mention.getOpinions().get(j);
                            property = model.createProperty(RDF.NAMESPACE, RDF.VALUE.getLocalName());
                            String sentiment = kafOpinion.getOpinionSentiment().getPolarity();
                            if (sentiment.equals("+")) {
                                sentiment = "positive";
                            }
                            else if (sentiment.equals("-")) {
                                sentiment ="negative";
                            }
                            Resource sentimentValue = model.createResource(ResourcesUri.grasp+sentiment);
                            //System.out.println("sentiment = " + sentiment);
                            attributionSubject.addProperty(property, sentimentValue); /// creates the literal as value
                        }
                    }
                }
            }
        }

    }


}
