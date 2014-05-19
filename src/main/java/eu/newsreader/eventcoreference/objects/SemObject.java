package eu.newsreader.eventcoreference.objects;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import eu.kyotoproject.kaf.KafFactuality;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import eu.kyotoproject.kaf.KafTerm;
import eu.newsreader.eventcoreference.naf.ResourcesUri;
import eu.newsreader.eventcoreference.util.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 3/28/12
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class SemObject implements Serializable {

    String id;
    String uri;
    double score;
    ArrayList<KafSense> concepts;
    ArrayList<PhraseCount> phraseCounts;
    String lcs;
    String label;
    ArrayList<NafMention> nafMentions;

    public SemObject() {
        this.nafMentions = new ArrayList<NafMention>();
        this.id = "";
        this.label = "";
        this.uri = "";
        this.lcs = "";
        this.score = 0;
        this.concepts = new ArrayList<KafSense>();
        this.phraseCounts = new ArrayList<PhraseCount>();
    }


    public void setFactuality(KafSaxParser kafSaxParser) {
        for (int i = 0; i < kafSaxParser.kafFactualityLayer.size(); i++) {
            KafFactuality kafFactuality = kafSaxParser.kafFactualityLayer.get(i);
            for (int j = 0; j < nafMentions.size(); j++) {
                NafMention nafMention = nafMentions.get(j);
                if (nafMention.getTokensIds().contains(kafFactuality.getId())) {
                    // System.out.println("nafMention.toString() = " + nafMention.toString());
                    nafMention.setFactuality(kafFactuality);
                }
            }
        }
    }

    public void setConcepts(ArrayList<KafSense> concepts) {
        this.concepts = concepts;
    }

    public String getPhrase() {
        String phrase = "";
        if (phraseCounts.size() > 0) {
            phrase = phraseCounts.get(0).getPhrase();
        }
        return phrase;
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

    public void addPhraseCountsForMentions(KafSaxParser kafSaxParser) {
        for (int i = 0; i < nafMentions.size(); i++) {
            NafMention nafMention = nafMentions.get(i);
            String phrase = "";
            for (int j = 0; j < nafMention.getTermsIds().size(); j++) {
                String termId = nafMention.getTermsIds().get(j);
                KafTerm kafTerm = kafSaxParser.getTerm(termId);
                if (kafTerm != null) {
                    phrase += " " + kafTerm.getLemma();
                } else {
                    System.out.println("no KafTerm for id = " + termId);
                }
            }
            if (!phrase.isEmpty()) {
                addPhraseCounts(phrase.trim());
            }
        }
    }

    public ArrayList<NafMention> getNafMentions() {
        return nafMentions;
    }

    public void setNafMentions(ArrayList<NafMention> nafMentions) {
        this.nafMentions = nafMentions;
    }

    public void addMentionUri(NafMention mentionUri) {
        this.nafMentions.add(mentionUri);
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

    public void setIdByDBpediaReference() {
        for (int i = 0; i < concepts.size(); i++) {
            KafSense kafSense = concepts.get(i);
            if ((kafSense.getResource().equalsIgnoreCase("spotlight_v1")) ||
                    (kafSense.getSensecode().indexOf("dbpedia.org/") > -1)) {
                /*
                (5) DBpedia resources are used as classes via rdf:type triples, while
                    they should be treated as instances, by either:
                    - using them as the subject of extracted triples (suggested), or
                    - linking them to entity/event URIs using owl:sameAs triples
                 */
                id = getNameSpaceTypeReference(kafSense);
                break;
            }
        }
    }

    public String getReference() {
        String reference = "";
        for (int i = 0; i < concepts.size(); i++) {
            KafSense kafSense = concepts.get(i);
            if ((kafSense.getResource().equalsIgnoreCase("spotlight_v1")) ||
                    (kafSense.getSensecode().indexOf("dbpedia.org/") > -1)) {
                /*
                (5) DBpedia resources are used as classes via rdf:type triples, while
                    they should be treated as instances, by either:
                    - using them as the subject of extracted triples (suggested), or
                    - linking them to entity/event URIs using owl:sameAs triples
                 */
                reference = getNameSpaceTypeReference(kafSense);
                break;
            }
        }
        return reference;
    }

    public String getURI() {
        if (uri.isEmpty()) {
            uri = this.getId();
        }
        return uri;
    }

    public String getURI(String nameSpace) {
        if (uri.isEmpty()) {
            uri = nameSpace + id;
        }
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setTopPhraseAsLabel() {
        Integer top = 0;
        for (int i = 0; i < phraseCounts.size(); i++) {
            PhraseCount phraseCount = phraseCounts.get(i);
            if (phraseCount.getCount() > top) {
                this.label = phraseCount.getPhrase() + ":" + phraseCount.getCount();
            }
        }
    }

    public String getTopPhraseAsLabel() {
        Integer top = 0;
        String label = "";
        for (int i = 0; i < phraseCounts.size(); i++) {
            PhraseCount phraseCount = phraseCounts.get(i);
            if (phraseCount.getCount() > top) {
                if (Util.hasAlphaNumeric(phraseCount.getPhrase())) {
                    try {
                        label = URLEncoder.encode(phraseCount.getPhrase().replace(" ", "-"), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                      //  e.printStackTrace();
                    }
                }
               // label = Util.alphaNumericUri(phraseCount.getPhrase().replace(" ", "-"));
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



    public void addToJenaModel(Model model, Resource type) {
        Resource resource = model.createResource(this.getURI());
        for (int i = 0; i < phraseCounts.size(); i++) {
            PhraseCount phraseCount = phraseCounts.get(i);
            // resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhraseCount()));
            resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhrase()));
        }

        resource.addProperty(RDF.type, type);

        for (int i = 0; i < concepts.size(); i++) {
            KafSense kafSense = concepts.get(i);
            if (kafSense.getResource().equalsIgnoreCase("verbnet")) {
                continue;
            } else if (kafSense.getResource().equalsIgnoreCase("wordnet")) {
                continue;
            } else if (kafSense.getResource().equalsIgnoreCase("propbank")) {
                continue;
            } else if (kafSense.getResource().equalsIgnoreCase("nombank")) {
                continue;
            } else if (kafSense.getResource().equalsIgnoreCase("spotlight_v1")) {
                /*
                (5) DBpedia resources are used as classes via rdf:type triples, while
                    they should be treated as instances, by either:
                    - using them as the subject of extracted triples (suggested), or
                    - linking them to entity/event URIs using owl:sameAs triples
                 */
/*                String nameSpaceType = getNameSpaceTypeReference(kafSense);
                Resource conceptResource = model.createResource(nameSpaceType);
                resource.addProperty(OWL.sameAs, conceptResource);*/
                /// we now use dbpedia to create the URI of the instance so we do not need to the sameAs mapping anymore
                continue;
            } else {
                String nameSpaceType = getNameSpaceTypeReference(kafSense);
                Resource conceptResource = model.createResource(nameSpaceType);
/*                if (!conceptResource.isURIResource()) {
                    System.out.println("conceptResource.getURI() = " + conceptResource.getURI()); 
                }*/
                resource.addProperty(RDF.type, conceptResource);
            }
        }

        for (int i = 0; i < nafMentions.size(); i++) {
            NafMention nafMention = nafMentions.get(i);
            Property property = model.createProperty(ResourcesUri.gaf + "denotedBy");
            Resource targetResource = model.createResource(nafMention.toString());
            resource.addProperty(property, targetResource);

        }
    }

    static public String getNameSpaceTypeReference(KafSense kafSense) {
        String ref = "";
        /**
         *         String nwr = "http://www.newsreader-project.eu/";
         String wn = "http://www.newsreader-project.eu/wordnet3.0/";
         String fn = "http://www.newsreader-project.eu/framenet/";
         String vn = "http://www.newsreader-project.eu/verbnet/";
         String pb = "http://www.newsreader-project.eu/propbank/";
         String gaf = "http://groundedannotationframework.org/";
         String sem = "http://semanticweb.cs.vu.nl/2009/11/sem/";
         */
        if (kafSense.getResource().equalsIgnoreCase("verbnet")) {
            ref = ResourcesUri.vn + kafSense.getSensecode();
        } else if (kafSense.getResource().equalsIgnoreCase("wordnet")) {
            ref = ResourcesUri.wn + kafSense.getSensecode();
        } else if (kafSense.getResource().equalsIgnoreCase("framenet")) {
            ref = ResourcesUri.fn + kafSense.getSensecode();
        } else if (kafSense.getResource().equalsIgnoreCase("propbank")) {
            ref = ResourcesUri.pb + kafSense.getSensecode();
        } else if (kafSense.getResource().equalsIgnoreCase("nombank")) {
            ref = ResourcesUri.nb + kafSense.getSensecode();
        } else if (kafSense.getResource().equalsIgnoreCase("eventtype")) {
            ref = ResourcesUri.nwr + kafSense.getSensecode();
        } else if (kafSense.getResource().equalsIgnoreCase("spotlight_v1")) {
            ref = kafSense.getSensecode(); /// keep it as it is since the dbpedia URL is complete as it comes from spotlight
            // ref = ResourcesUri.dbp+kafSense.getSensecode();
            // ref = Util.cleanDbpediaUri(kafSense.getSensecode(), ResourcesUri.dbp);
        } else {
            if (kafSense.getSensecode().indexOf(ResourcesUri.dbp) > -1) {
                // ref = ResourcesUri.dbp+kafSense.getSensecode();
                ref = kafSense.getSensecode(); /// keep it as it is since the dbpedia URL is complete as it comes from spotlight
                //ref =  Util.cleanDbpediaUri(kafSense.getSensecode(), ResourcesUri.dbp);
            } else {
                if (kafSense.getSensecode().equalsIgnoreCase("cognition")) {
                    kafSense.setSensecode("SPEECH_COGNITIVE");
                } else if (kafSense.getSensecode().equalsIgnoreCase("communication")) {
                    kafSense.setSensecode("SPEECH_COGNITIVE");
                } else if (kafSense.getSensecode().equalsIgnoreCase("grammatical")) {
                    kafSense.setSensecode("GRAMMATICAL");
                } else if (kafSense.getSensecode().equalsIgnoreCase("contextual")) {
                    kafSense.setSensecode("OTHER");
                }
                ref = ResourcesUri.nwr + kafSense.getSensecode();
            }
        }

        return ref;
    }


    public double matchObjectByConcepts(SemObject anObject) {
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
        score = ((double) nMatches / concepts.size()) * ((double) nMatches / anObject.getConcepts().size());
        return score;
    }

    public double matchObjectByPhrases(SemObject anObject) {
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
        score = ((double) nMatches / getPhraseCounts().size()) * ((double) nMatches / anObject.getPhraseCounts().size());
        return score;
    }

    public void mergeSemObject(SemObject anObject) {
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
            PhraseCount anObjectphraseCount = anObject.getPhraseCounts().get(i);
            boolean match = false;
            for (int j = 0; j < phraseCounts.size(); j++) {
                PhraseCount myCount = phraseCounts.get(j);
                if (myCount.getPhrase().equals(anObjectphraseCount.getPhrase())) {
                    myCount.addCount(anObjectphraseCount.getCount());
                    match = true;
                    break;
                }
            }
            if (!match) {
                phraseCounts.add(anObjectphraseCount);
            }
        }
       // System.out.println("this.nafMentions.toString() = " + this.nafMentions.toString());
       // System.out.println("anObject.nafMentions.toString() = " + anObject.nafMentions.toString());
        for (int i = 0; i < anObject.getNafMentions().size(); i++) {
            NafMention nafMention = anObject.getNafMentions().get(i);
            if (!Util.hasMention(this.nafMentions, nafMention)) {
               // System.out.println("New mention added !!! nafMention.toString() = " + nafMention.toString());
                this.nafMentions.add(nafMention);
            }
        }
    }

    public boolean hasMention (NafMention nafMention) {
        for (int i = 0; i < nafMentions.size(); i++) {
            NafMention mention = nafMentions.get(i);
            if (mention.toString().equals(nafMention.toString())) {
                return true;
            }
        }
        return false;
    }

    public Element toXML(Document xmldoc) {
        Element root = xmldoc.createElement("object");
        if (!this.getURI().isEmpty())
            root.setAttribute("uri", this.getURI());
        return root;
    }

}