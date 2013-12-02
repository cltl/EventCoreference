package eu.newsreader.eventcoreference.objects;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import eu.kyotoproject.kaf.CorefTarget;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import eu.kyotoproject.kaf.KafTerm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 3/28/12
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class SemObject {

   private String id;
   private double score;
   private ArrayList<KafSense> concepts;
   private ArrayList<PhraseCount> phraseCounts;
   private String lcs;
   private String label;
   private ArrayList<ArrayList<eu.kyotoproject.kaf.CorefTarget>> mentions;

   public SemObject() {
        this.mentions = new ArrayList<ArrayList<eu.kyotoproject.kaf.CorefTarget>>();;
        this.id = "";
        this.label = "";
        this.lcs = "";
        this.score = 0;
        this.concepts = new ArrayList<KafSense>();
        this.phraseCounts = new ArrayList<PhraseCount>();
    }

    public void setConcepts(ArrayList<KafSense> concepts) {
        this.concepts = concepts;
    }

    public ArrayList<PhraseCount> getPhraseCounts() {
        return phraseCounts;
    }

    public void setPhraseCounts(ArrayList<PhraseCount> phraseCounts) {
        this.phraseCounts = phraseCounts;
    }

    public void addPhraseCounts(String phrase) {
        boolean match = false;
        for (int i = 0; i < phraseCounts.size(); i++) {
            PhraseCount count = phraseCounts.get(i);
            if (count.getPhrase().equals(phrase)) {
                count.incrementCount();
                match = true;
                break;
            }
        }
        if (!match) {
            phraseCounts.add(new PhraseCount(phrase, 1));
        }
    }

    public void addPhraseCountsForMentions (KafSaxParser kafSaxParser) {
        for (int i = 0; i < mentions.size(); i++) {
            ArrayList<CorefTarget> corefTarget = mentions.get(i);
            String phrase = "";
            for (int j = 0; j < corefTarget.size(); j++) {
                CorefTarget target = corefTarget.get(j);
                KafTerm kafTerm = kafSaxParser.getTerm(target.getId());
                if (kafTerm!=null) {
                    phrase += " "+kafTerm.getLemma();
                }
            }
            if (!phrase.isEmpty()) {
                addPhraseCounts(phrase.trim());
            }
        }
    }
    public ArrayList<ArrayList<eu.kyotoproject.kaf.CorefTarget>> getMentions() {
        return mentions;
    }

    public void setMentions(ArrayList<ArrayList<eu.kyotoproject.kaf.CorefTarget>> mentions) {
        this.mentions = mentions;
    }

    public void addMentions(ArrayList<eu.kyotoproject.kaf.CorefTarget> mentions) {
        this.mentions.add(mentions);
    }

    public void addMention(ArrayList<eu.kyotoproject.kaf.CorefTarget> mention) {
        this.mentions.add(mention);
    }

    public String getLcs() {
        return lcs;
    }

    public void setLcs(String lcs) {
        this.lcs = lcs;
    }

    public String getId() {
        return id;
    }

    public String getURI() {
        String uri = "http://www.newsreader-project.eu/"+id;
        return uri;
    }

    public String getURI(String nameSpace) {
        String uri = nameSpace+":"+id;
        return uri;
    }

    public void setTopPhraseAsLabel () {
        Integer top = 0;
        for (int i = 0; i < phraseCounts.size(); i++) {
            PhraseCount phraseCount = phraseCounts.get(i);
            if (phraseCount.getCount()>top) {
                this.label = phraseCount.getPhrase();
            }
        }
    }

    public String getTopPhraseAsLabel () {
        Integer top = 0;
        String label = "";
        for (int i = 0; i < phraseCounts.size(); i++) {
            PhraseCount phraseCount = phraseCounts.get(i);
            if (phraseCount.getCount()>top) {
                label = phraseCount.getPhrase().replace(" ", "-");
            }
        }
        return label;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public ArrayList<KafSense> getConcepts() {
        return concepts;
    }

    public void setConcept(ArrayList<KafSense> concepts) {
        this.concepts = concepts;
    }

    public void addConcepts(ArrayList<KafSense> concepts) {
        this.concepts.addAll(concepts);
    }

    public void addConcept(KafSense concept) {
        this.concepts.add(concept);
    }

    public Element toRdf (Document xmldoc, String name) {
        Element root = xmldoc.createElement(name);
        if (this.getId() != null)
            root.setAttribute("rdf:about", this.getId());

        for (int i = 0; i < concepts.size(); i++) {
            KafSense kafSense = concepts.get(i);
            Element conceptUri = xmldoc.createElement("naf:uri");
            conceptUri.setAttribute("rdf:resource", kafSense.getResource()+"#"+kafSense.getSensecode());
            root.appendChild(conceptUri);
        }

        for (int i = 0; i < mentions.size(); i++) {
            ArrayList<CorefTarget> corefTargetArrayList = mentions.get(i);
            Element mentionElement = xmldoc.createElement("mentions");
            for (int j = 0; j < corefTargetArrayList.size(); j++) {
                CorefTarget corefTarget = corefTargetArrayList.get(j);
                mentionElement.appendChild(corefTarget.toXML(xmldoc));
            }
            root.appendChild(mentionElement);
        }
        return root;
    }

    public Resource toJenaRdfResource () {
        Model model = ModelFactory.createDefaultModel();
        Resource resource = model.createResource(this.getURI());
        resource.addProperty(RDFS.label, model.createLiteral(getTopPhraseAsLabel()));
        for (int i = 0; i < concepts.size(); i++) {
            KafSense kafSense = concepts.get(i);
            resource.addProperty(RDF.type, kafSense.getSensecode());
        }
        return resource;
    }

    public Resource toJenaRdfResource (String nameSpace) {
        Model model = ModelFactory.createDefaultModel();
        Resource resource = model.createResource(this.getURI(nameSpace));
        resource.addProperty(RDFS.label, model.createLiteral(getTopPhraseAsLabel()));
        for (int i = 0; i < concepts.size(); i++) {
            KafSense kafSense = concepts.get(i);
            resource.addProperty(RDF.type, kafSense.getSensecode());
        }
        return resource;
    }


}

