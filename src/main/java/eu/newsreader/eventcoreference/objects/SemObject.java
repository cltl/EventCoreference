package eu.newsreader.eventcoreference.objects;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import eu.kyotoproject.kaf.CorefTarget;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import eu.kyotoproject.kaf.KafTerm;

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
        if (!phrase.isEmpty()) {
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
    }

    public void addPhraseCountsForMentions (KafSaxParser kafSaxParser) {
        for (int i = 0; i < mentions.size(); i++) {
            ArrayList<CorefTarget> corefTarget = mentions.get(i);
            String phrase = "";
            for (int j = 0; j < corefTarget.size(); j++) {
                CorefTarget target = corefTarget.get(j);
                /// ID-HACK
                String id = target.getId();
                int idx = target.getId().lastIndexOf("/");
                if (idx>-1) {
                    id = id.substring(idx+1);
                }
                KafTerm kafTerm = kafSaxParser.getTerm(id);
                if (kafTerm!=null) {
                    phrase += " "+kafTerm.getLemma();
                }
                else {
                   // System.out.println("no KafTerm for id = " + id);
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

    public void setMentions(String baseUrl, ArrayList<ArrayList<eu.kyotoproject.kaf.CorefTarget>> mentions) {
        for (int i = 0; i < mentions.size(); i++) {
            ArrayList<CorefTarget> corefTargetArrayList = mentions.get(i);
            for (int j = 0; j < corefTargetArrayList.size(); j++) {
                CorefTarget corefTarget = corefTargetArrayList.get(j);
                corefTarget.setId(baseUrl+"/"+corefTarget.getId());
            }
        }
        this.mentions = mentions;
    }

    public void addMentions(String baseUrl, ArrayList<eu.kyotoproject.kaf.CorefTarget> mentions) {
        for (int i = 0; i < mentions.size(); i++) {
            CorefTarget corefTarget = mentions.get(i);
            corefTarget.setId(baseUrl+"/"+corefTarget.getId());
        }
        this.mentions.add(mentions);
    }

    public void addMention(String baseUrl, ArrayList<eu.kyotoproject.kaf.CorefTarget> mention) {
        for (int i = 0; i < mention.size(); i++) {
            CorefTarget corefTarget = mention.get(i);
            corefTarget.setId(baseUrl+"/"+corefTarget.getId());
        }
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
                this.label = phraseCount.getPhrase()+":"+phraseCount.getCount();
            }
        }
    }

    public String getTopPhraseAsLabel () {
        Integer top = 0;
        String label = "";
        for (int i = 0; i < phraseCounts.size(); i++) {
            PhraseCount phraseCount = phraseCounts.get(i);
            if (phraseCount.getCount()>top) {
                label = phraseCount.getPhrase().replace(" ", "-")+":"+phraseCount.getCount();
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
        if (!concept.getSensecode().isEmpty()) {
            this.concepts.add(concept);
        }
    }


    public Resource toJenaRdfResource (Model model) {
        Resource resource = model.createResource(this.getURI());
        for (int i = 0; i < phraseCounts.size(); i++) {
            PhraseCount phraseCount = phraseCounts.get(i);
            resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhraseCount()));
        }
        //resource.addProperty(RDFS.label, model.createLiteral(getTopPhraseAsLabel()));
        for (int i = 0; i < concepts.size(); i++) {
            KafSense kafSense = concepts.get(i);
            String nameSpaceType = getNameSpaceTypeReference(kafSense);
            resource.addProperty(RDF.type, nameSpaceType);
        }
        for (int i = 0; i < mentions.size(); i++) {
            ArrayList<CorefTarget> corefTargets = mentions.get(i);
            for (int j = 0; j < corefTargets.size(); j++) {
                CorefTarget corefTarget = corefTargets.get(j);
                Property property = model.createProperty("gaf:denotedBy");
                resource.addProperty(property, corefTarget.getId());
            }
        }
        return resource;
    }

    static public String getNameSpaceTypeReference (KafSense kafSense) {
        String ref = "";
        if (kafSense.getResource().equalsIgnoreCase("verbnet")) {
            ref = "vn:"+kafSense.getSensecode();
        }
        else if (kafSense.getResource().equalsIgnoreCase("wordnet")) {
            ref = "wn:"+kafSense.getSensecode();
        }
        else if (kafSense.getResource().equalsIgnoreCase("framenet")) {
            ref = "fn:"+kafSense.getSensecode();
        }
        else if (kafSense.getResource().equalsIgnoreCase("propbank")) {
            ref = "pb:"+kafSense.getSensecode();
        }
        else if (kafSense.getResource().equalsIgnoreCase("nombank")) {
            ref = "nb:"+kafSense.getSensecode();
        }
        else if (kafSense.getResource().equalsIgnoreCase("eventype")) {
            ref = "nwr:"+kafSense.getSensecode();
        }
        else if (kafSense.getResource().equalsIgnoreCase("spotlight_v1")) {
            ref = kafSense.getSensecode();
        }
        else {
            ref = "nwr:"+kafSense.getSensecode();
        }

        return ref;
    }


    public double matchObjectByConcepts (SemObject anObject) {
        double score = -1;
        int nMatches = 0;
        for (int i = 0; i < concepts.size(); i++) {
            KafSense kafSense = concepts.get(i);
            if (!kafSense.getSensecode().isEmpty()) {
                for (int j = 0; j < anObject.getConcepts().size(); j++) {
                    KafSense sense = anObject.getConcepts().get(j);
                    if (sense.getSensecode().equals(kafSense.getSensecode())) {
                        nMatches++;
                    }
                }
            }
        }
        score = ((double) nMatches/concepts.size()) * ((double) nMatches/anObject.getConcepts().size());
        return score;
    }
    public double matchObjectByPhrases (SemObject anObject) {
        double score = -1;
        int nMatches = 0;
        for (int i = 0; i < phraseCounts.size(); i++) {
            PhraseCount phraseCount = phraseCounts.get(i);
            if (!phraseCount.getPhrase().isEmpty()) {
                for (int j = 0; j < anObject.getPhraseCounts().size(); j++) {
                    PhraseCount aPhraseCount = anObject.getPhraseCounts().get(j);
                    if (aPhraseCount.getPhrase().equals(phraseCount.getPhrase())) {
                        nMatches++;
                    }
                }
            }
        }
        score = ((double) nMatches/getPhraseCounts().size()) * ((double) nMatches/anObject.getPhraseCounts().size());
        return score;
    }

    public void mergeSemObject (SemObject anObject) {
        for (int i = 0; i < anObject.getConcepts().size(); i++) {
            KafSense kafSense = anObject.getConcepts().get(i);
            boolean match = false;
            for (int j = 0; j < concepts.size(); j++) {
                KafSense sense = concepts.get(j);
                if (sense.getSensecode().equals(kafSense.getSensecode())) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                concepts.add(kafSense);
            }
        }
        for (int i = 0; i < anObject.getPhraseCounts().size(); i++) {
            PhraseCount phraseCount = anObject.getPhraseCounts().get(i);
            boolean match = false;
            for (int j = 0; j < phraseCounts.size(); j++) {
                PhraseCount count = phraseCounts.get(j);
                if (count.getPhrase().equals(phraseCount.getPhrase())) {
                    count.addCount(phraseCount.getCount());
                    match = true;
                    break;
                }
            }
            if (!match) {
                phraseCounts.add(phraseCount);
            }
        }
        for (int i = 0; i < anObject.getMentions().size(); i++) {
            ArrayList<CorefTarget> corefTargetArrayList = anObject.getMentions().get(i);
            mentions.add(corefTargetArrayList);
        }
    }

}

