package eu.newsreader.eventcoreference.objects;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import eu.newsreader.eventcoreference.naf.ResourcesUri;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 3/28/12
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class SemTime extends SemObject implements Serializable {

   private OwlTime owlTime;
   private OwlTime owlTimeBegin;
   private OwlTime owlTimeEnd;
    private String functionInDocument;

   public SemTime() {
       functionInDocument = "";
       owlTime = new OwlTime();
       owlTimeBegin = new OwlTime();
       owlTimeEnd = new OwlTime();
    }



    public String toString () {
        String str = "<semTime id=\""+this.getId()+"\" lcs=\""+this.getLabel()+"\" score=\""+this.getScore()+"\" label=\""+this.getLabel()+"\" mentions=\""+this.getNafMentions().size()+"\">\n";
        str += "<mentions>\n";
        for (int i = 0; i < this.getNafMentions().size(); i++) {
            NafMention s = this.getNafMentions().get(i);
            str += s.toString()+"\n";
        }
        str += "</mentions>\n";
        str += "</semEvent>\n";
        return str;
    }

    public String getFunctionInDocument() {
        return functionInDocument;
    }

    public void setFunctionInDocument(String functionInDocument) {
        this.functionInDocument = functionInDocument;
    }

    public OwlTime getOwlTime() {
        return owlTime;
    }

    public void setOwlTime(OwlTime owlTime) {
        this.owlTime = owlTime;
    }

    public OwlTime getOwlTimeBegin() {
        return owlTimeBegin;
    }

    public void setOwlTimeBegin(OwlTime owlTimeBegin) {
        this.owlTimeBegin = owlTimeBegin;
    }

    public OwlTime getOwlTimeEnd() {
        return owlTimeEnd;
    }

    public void setOwlTimeEnd(OwlTime owlTimeEnd) {
        this.owlTimeEnd = owlTimeEnd;
    }


    public void addToJenaModelTimeInstant(Model model, OwlTime owlTime) {
        this.getOwlTime().addToJenaModelOwlTimeInstant(model);

        Resource resource = model.createResource(this.getURI());
        for (int i = 0; i < this.getPhraseCounts().size(); i++) {
            PhraseCount phraseCount = this.getPhraseCounts().get(i);
            resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhrase()));
        }

        resource.addProperty(RDF.type, Sem.Time);

        Resource aResource = model.createResource(ResourcesUri.owltime + "Instant");
        resource.addProperty(RDF.type, aResource);

        Resource value = model.createResource(owlTime.getDateString());
        Property property = model.createProperty(ResourcesUri.owltime + "inDateTime");
        resource.addProperty(property, value);

        for (int i = 0; i < this.getNafMentions().size(); i++) {
            NafMention nafMention = this.getNafMentions().get(i);
            Property gaf = model.createProperty(ResourcesUri.gaf + "denotedBy");
            Resource targetResource = model.createResource(nafMention.toString());
            resource.addProperty(gaf, targetResource);
        }
    }

    public void addToJenaModelDocTimeInstant(Model model) {

        this.getOwlTime().addToJenaModelOwlTimeInstant(model);

        Resource resource = model.createResource(this.getURI());
        resource.addProperty(RDFS.label, model.createLiteral(this.getTopPhraseAsLabel()));

        /*for (int i = 0; i < phraseCounts.size(); i++) {
            PhraseCount phraseCount = phraseCounts.get(i);
            resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhrase()));
        }*/

        //resource.addProperty(RDF.type, Sem.Time);
        // System.out.println("this.getOwlTime().toString() = " + this.getOwlTime().toString());
        Resource interval = model.createResource(ResourcesUri.owltime + "Instant");
        resource.addProperty(RDF.type, interval);

        Resource value = model.createResource(this.getOwlTime().getDateString());
        Property property = model.createProperty(ResourcesUri.owltime + "inDateTime");
        resource.addProperty(property, value);

        for (int i = 0; i < this.getNafMentions().size(); i++) {
            NafMention nafMention = this.getNafMentions().get(i);
            Property gaf = model.createProperty(ResourcesUri.gaf + "denotedBy");
            Resource targetResource = model.createResource(nafMention.toString());
            resource.addProperty(gaf, targetResource);

        }


    }

    public void addToJenaModelTimeInterval(Model model) {
        this.getOwlTime().addToJenaModelOwlTimeInstant(model);

        Resource resource = model.createResource(this.getURI());
        for (int i = 0; i < this.getPhraseCounts().size(); i++) {
            PhraseCount phraseCount = this.getPhraseCounts().get(i);
            resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhrase()));
        }

        resource.addProperty(RDF.type, Sem.Time);

        Resource interval = model.createResource(ResourcesUri.owltime + "Interval");
        resource.addProperty(RDF.type, interval);

        Resource value = model.createResource(this.getOwlTime().getDateString());
        Property property = model.createProperty(ResourcesUri.owltime + "inDateTime");
        resource.addProperty(property, value);

        for (int i = 0; i < this.getNafMentions().size(); i++) {
            NafMention nafMention = this.getNafMentions().get(i);
            Property gaf = model.createProperty(ResourcesUri.gaf + "denotedBy");
            Resource targetResource = model.createResource(nafMention.toString());
            resource.addProperty(gaf, targetResource);

        }

    }


/*
    public void addToJenaModelFutureCondensed(Model model) {
        this.getOwlTimeBegin().addToJenaModelOwlTimeInstant(model);

        Resource resource = model.createResource(this.getURI());
        resource.addProperty(RDFS.label, model.createLiteral(this.getTopPhraseAsLabel()));

        Resource interval = model.createResource(ResourcesUri.owltime + "Interval");
        resource.addProperty(RDF.type, interval);

        Resource value = model.createResource(this.getOwlTimeBegin().getDateString());
        Property property = model.createProperty(ResourcesUri.owltime + "hasBeginning");
        resource.addProperty(property, value);

    }
*/

    public void addToJenaModelTimeIntervalCondensed(Model model) {
        this.getOwlTimeBegin().addToJenaModelOwlTimeInstant(model);
        this.getOwlTimeEnd().addToJenaModelOwlTimeInstant(model);

        Resource resource = model.createResource(this.getURI());
        resource.addProperty(RDFS.label, model.createLiteral(this.getTopPhraseAsLabel()));

        /*for (int i = 0; i < phraseCounts.size(); i++) {
            PhraseCount phraseCount = phraseCounts.get(i);
            resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhrase()));
        }*/

        Resource interval = model.createResource(ResourcesUri.owltime + "Interval");
        resource.addProperty(RDF.type, interval);

        Resource value = model.createResource(this.getOwlTimeBegin().getDateString());
        Property property = model.createProperty(ResourcesUri.owltime + "hasBeginning");
        resource.addProperty(property, value);

        value = model.createResource(this.getOwlTimeEnd().getDateString());
        property = model.createProperty(ResourcesUri.owltime + "hasEnd");
        resource.addProperty(property, value);

        for (int i = 0; i < this.getNafMentions().size(); i++) {
            NafMention nafMention = this.getNafMentions().get(i);
            Property gaf = model.createProperty(ResourcesUri.gaf + "denotedBy");
            Resource targetResource = model.createResource(nafMention.toString());
            resource.addProperty(gaf, targetResource);

        }
    }

    public void addToJenaModelDocTimeInterval(Model model) {
        if (this.getPhraseCounts().size() > 0) {
            OwlTime owlTime = new OwlTime();
            owlTime.parsePublicationDate(getPhrase());
            owlTime.addToJenaModelOwlTimeInstant(model);

            Resource resource = model.createResource(this.getURI());
            for (int i = 0; i < this.getPhraseCounts().size(); i++) {
                PhraseCount phraseCount = this.getPhraseCounts().get(i);
                resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhrase()));
            }

            resource.addProperty(RDF.type, Sem.Time);
            Resource interval = model.createResource(ResourcesUri.owltime + "Interval");
            resource.addProperty(RDF.type, interval);

            Resource value = model.createResource(owlTime.getDateString());
            Property property = model.createProperty(ResourcesUri.owltime + "inDateTime");
            resource.addProperty(property, value);
        }
    }

    /*
    :DurationDescription
      a       owl:Class ;
      rdfs:subClassOf
              [ a       owl:Restriction ;
                owl:maxCardinality 1 ;
                owl:onProperty :seconds
              ] ;
      rdfs:subClassOf
              [ a       owl:Restriction ;
                owl:maxCardinality 1 ;
                owl:onProperty :minutes
              ] ;
      rdfs:subClassOf
              [ a       owl:Restriction ;
                owl:maxCardinality 1 ;
                owl:onProperty :hours
              ] ;
      rdfs:subClassOf
              [ a       owl:Restriction ;
                owl:maxCardinality 1 ;
                owl:onProperty :days
              ] ;
      rdfs:subClassOf
              [ a       owl:Restriction ;
                owl:maxCardinality 1 ;
                owl:onProperty :weeks
              ] ;
      rdfs:subClassOf
              [ a       owl:Restriction ;
                owl:maxCardinality 1 ;
                owl:onProperty :months
              ] ;
      rdfs:subClassOf
              [ a       owl:Restriction ;
                owl:maxCardinality 1 ;
                owl:onProperty :years
              ] .
     */



    public SemRelation createSemTimeRelation (String baseUrl,
                                              int timexRelationCount,
                                              String predicate,
                                              String semEventId,
                                              NafMention mention) {
        SemRelation semRelation = new SemRelation();
        String relationInstanceId = baseUrl + "tr" + timexRelationCount;  // shorter form for triple store
        semRelation.setId(relationInstanceId);
        semRelation.addMention(mention);
        semRelation.addPredicate(predicate);
        semRelation.setSubject(semEventId);
        semRelation.setObject(this.getId());
        return semRelation;
    }

    public SemRelation createSemTimeRelation (String baseUrl,
                                              int timexRelationCount,
                                              String predicate,
                                              String semEventId) {
        SemRelation semRelation = new SemRelation();
        String relationInstanceId = baseUrl + "tr" + timexRelationCount;  // shorter form for triple store
        semRelation.setId(relationInstanceId);
        semRelation.addPredicate(predicate);
        semRelation.setSubject(semEventId);
        semRelation.setObject(this.getId());
        return semRelation;
    }

    public void interpretQuarterAsPeriod () {
        this.owlTimeBegin.setYear(owlTime.getYear());
        this.owlTimeBegin.setDay("1");
        this.owlTimeEnd.setYear(owlTime.getYear());
        if (!this.owlTime.getMonth().isEmpty()) {
            if (this.owlTime.getMonth().equalsIgnoreCase("q1")) {
                this.owlTimeBegin.setMonth("1");
                this.owlTimeEnd.setMonth("3");
                this.owlTimeEnd.setDay("30");
            }
            else if (this.owlTime.getMonth().equalsIgnoreCase("q2")) {
                this.owlTimeBegin.setMonth("4");
                this.owlTimeEnd.setMonth("6");
                this.owlTimeEnd.setDay("30");
            }
            else if (this.owlTime.getMonth().equalsIgnoreCase("q3")) {

                this.owlTimeBegin.setMonth("7");
                this.owlTimeEnd.setMonth("9");
                this.owlTimeEnd.setDay("30");
            }
            else if (this.owlTime.getMonth().equalsIgnoreCase("q4")) {

                this.owlTimeBegin.setMonth("10");
                this.owlTimeEnd.setMonth("12");
                this.owlTimeEnd.setDay("31");
            }
        }
    }

    public void interpretMonthAsPeriod () {
                 /*
             nwr:20010101
        owltime:day "1"^^xsd:int ;
        owltime:month "1"^^xsd:int ;
        owltime:year "2001"^^xsd:int .
          */

        this.owlTimeBegin.setYear(owlTime.getYear());
        this.owlTimeBegin.setMonth(owlTime.getMonth());
        this.owlTimeBegin.setDay("1");
        this.owlTimeEnd.setYear(owlTime.getYear());
        this.owlTimeEnd.setMonth(owlTime.getMonth());
        if (!this.owlTime.getMonth().isEmpty()) {
            if (this.owlTime.getMonth().equalsIgnoreCase("1") ||
                    this.owlTime.getMonth().equalsIgnoreCase("4") ||
                    this.owlTime.getMonth().equalsIgnoreCase("7") ||
                    this.owlTime.getMonth().equalsIgnoreCase("8") ||
                    this.owlTime.getMonth().equalsIgnoreCase("10") ||
                    this.owlTime.getMonth().equalsIgnoreCase("12")
                    ) {
                this.owlTimeEnd.setDay("31");
            }
            else if (this.owlTime.getMonth().equalsIgnoreCase("2")) {
                this.owlTimeEnd.setDay("28");
            }
            else {
                this.owlTimeEnd.setDay("30");
            }
        }
    }

    public void interpretYearAsPeriod () {
        this.owlTimeBegin.setYear(owlTime.getYear());
        this.owlTimeBegin.setDay("1");
        this.owlTimeBegin.setMonth("1");
        this.owlTimeEnd.setYear(owlTime.getYear());
        this.owlTimeEnd.setDay("1");
        this.owlTimeEnd.setMonth("31");
    }


}
