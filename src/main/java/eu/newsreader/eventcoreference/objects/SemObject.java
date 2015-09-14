package eu.newsreader.eventcoreference.objects;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.naf.ResourcesUri;
import eu.newsreader.eventcoreference.util.FrameTypes;
import eu.newsreader.eventcoreference.util.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 3/28/12
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class SemObject implements Serializable {

    private String id;                              //
    private String type;
    private String uri;                             //
    private ArrayList<String> nafIds;
    private double score;
    private ArrayList<KafSense> concepts;          //
    private ArrayList<KafTopic> topics;
    private ArrayList<PhraseCount> phraseCounts;   //
    private ArrayList<KafSense> lcs;
    private String label;
    private ArrayList<NafMention> nafMentions;    //

    public SemObject() {
        this.nafMentions = new ArrayList<NafMention>();
        this.id = "";
        this.type = "";
        this.nafIds = new ArrayList<String>();
        this.label = "";
        this.uri = "";
        this.lcs = new ArrayList<KafSense>();
        this.score = 0;
        this.concepts = new ArrayList<KafSense>();
        this.phraseCounts = new ArrayList<PhraseCount>();
        this.topics = new ArrayList<KafTopic>();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<String> getNafIds() {
        return nafIds;
    }

    public void addNafId(String nafId) {
        if (!this.nafIds.contains(nafId)) this.nafIds.add(nafId);
    }

    public ArrayList<KafTopic> getTopics() {
        return topics;
    }

    public void setTopics(ArrayList<KafTopic> topics) {
        this.topics = topics;
    }

    public boolean hasTopic(KafTopic topic) {
        for (int i = 0; i < topics.size(); i++) {
            KafTopic kafTopic = topics.get(i);
            if (kafTopic.getTopic().equals(topic.getTopic())) {
                return true;
            }
        }
        return false;
    }

    public void addTopic(KafTopic topic) {
        if (!hasTopic(topic)) {
            this.topics.add(topic);
        }
    }

    public ArrayList<String> getTermIds () {
        ArrayList<String> termIds = new ArrayList<String>();
        for (int i = 0; i < nafMentions.size(); i++) {
            NafMention nafMention = nafMentions.get(i);
            for (int j = 0; j < nafMention.getTermsIds().size(); j++) {
                String termId = nafMention.getTermsIds().get(j);
                termIds.add(termId);
            }
        }
        return termIds;
    }

    public void addFactuality(KafSaxParser kafSaxParser) {
        for (int i = 0; i < kafSaxParser.kafFactualityLayer.size(); i++) {
            KafFactuality kafFactuality = kafSaxParser.kafFactualityLayer.get(i);
            for (int j = 0; j < nafMentions.size(); j++) {
                NafMention nafMention = nafMentions.get(j);
                nafMention.addFactuality(kafSaxParser);
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

    public void addNafMentions(ArrayList<NafMention> nafMentions) {
        for (int i = 0; i < nafMentions.size(); i++) {
            NafMention nafMention = nafMentions.get(i);
            this.nafMentions.add(nafMention);
        }
    }

    public void addNafMention(NafMention nafMention) {
            this.nafMentions.add(nafMention);
    }

    public void addMentionUri(NafMention mentionUri) {
        this.nafMentions.add(mentionUri);
    }

    public ArrayList<KafSense> getLcs() {
        return lcs;
    }

    public void addLcses(KafSense concept) {
        if (!concept.getSensecode().isEmpty()) {
            this.lcs.add(concept);
        }
    }

    public void addLcses(ArrayList<KafSense> lcses) {
        for (int i = 0; i < lcses.size(); i++) {
            KafSense kafSense = lcses.get(i);
            this.addLcses(kafSense);
        }
    }

    public void setLcs(ArrayList<KafSense> lcs) {
        this.lcs = lcs;
    }

    public String getId() {
        return id;
    }

    public void setIdByDBpediaReferenceRerank() {
        //// We first check if there has been a rerank of the references if not
        //// then we are getting the highest scoring external reference, which is the first
        /*                                                                             -
              <externalReferences>
        <externalRef resource="spotlight_v1" reference="http://dbpedia.org/resource/Michigan" confidence="1.0" reftype="en"/>
        <externalRef resource="spotlight_v1" reference="http://dbpedia.org/resource/List_of_United_States_Senators_from_Michigan" confidence="9.9521784E-14" reftype="en"/>
              <externalRef resource="vua-type-reranker-v1.0" reference="http://dbpedia.org/resource/North_Dakota" confidence="17"/>
             vua-type-reranker
      </externalReferences>

         */

        boolean RERANK = false;
        for (int i = 0; i < concepts.size(); i++) {
            KafSense kafSense = concepts.get(i);
            if (kafSense.getResource().toLowerCase().startsWith("vua-type-reranker")) {
                id = getNameSpaceTypeReference(kafSense);
                RERANK = true;
                break;
            }
        }
        if (!RERANK) {
            setIdByDBpediaReference();
        }
    }

    public void setIdByDBpediaReference() {
        KafSense topSense = Util.getBestScoringExternalReference(concepts);
        if (topSense!=null) {
            if ((topSense.getResource().equalsIgnoreCase("spotlight_v1")) ||
                    (topSense.getSensecode().indexOf("dbpedia.org/") > -1)) {
                /*
                (5) DBpedia resources are used as classes via rdf:type triples, while
                    they should be treated as instances, by either:
                    - using them as the subject of extracted triples (suggested), or
                    - linking them to entity/event URIs using owl:sameAs triples
                 */
                id = getNameSpaceTypeReference(topSense);
            }
        }
    }

    public String getReference() {
        String reference = "";
        for (int i = 0; i < concepts.size(); i++) {
            KafSense kafSense = concepts.get(i);
            if ((kafSense.getResource().equalsIgnoreCase("spotlight_v1")) ||
                    (kafSense.getResource().toLowerCase().startsWith("vua-type-reranker")) ||
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
            if (phraseCount.getPhrase().length()>2 || label.isEmpty()) {
                if (goodPhrase(phraseCount)) {
                    if (phraseCount.getCount() > top) {
                        if (Util.hasAlphaNumeric(phraseCount.getPhrase())) {
                            label = phraseCount.getPhrase();
/*                            try {
                                label = URLEncoder.encode(phraseCount.getPhrase(), "UTF-8");
                                //  System.out.println("label = " + label);
                            } catch (UnsupportedEncodingException e) {
                                //  e.printStackTrace();
                            }*/
                        } else {
                            //  System.out.println("phraseCount.getPhrase() = " + phraseCount.getPhrase());
                        }
                        // label = Util.alphaNumericUri(phraseCount.getPhrase().replace(" ", "-"));
                    }
                }
            }
        }
        return label;
    }


    public boolean goodPhrase (PhraseCount phraseCount) {
        if (phraseCount.getPhrase().equals("him") ||
                phraseCount.getPhrase().equals("her") ||
                phraseCount.getPhrase().equals("they") ||
                phraseCount.getPhrase().equals("who") ||
                phraseCount.getPhrase().equals("which") ||
                phraseCount.getPhrase().equals("whom") ||
                phraseCount.getPhrase().equals("that") ||
                phraseCount.getPhrase().equals("its") ||
                phraseCount.getPhrase().equals("she")
                ) {
            return false;
        }
        else if (phraseCount.getPhrase().length()<=2) {
            return false;
        }
        else {
            return true;
        }
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
        for (int i = 0; i < concepts.size(); i++) {
            KafSense kafSense = concepts.get(i);
            boolean HAS = false;
            for (int j = 0; j < this.concepts.size(); j++) {
                KafSense sense = this.concepts.get(j);
                if (kafSense.getSensecode().equals(sense.getSensecode())) {
                    HAS = true;
                    break;
                }
            }
            if (!HAS) {
                addConcept(kafSense);
            }
        }
        this.concepts.addAll(concepts);
    }

    public void addConceptsExcept(ArrayList<KafSense> concepts, String resource) {
        for (int i = 0; i < concepts.size(); i++) {
            KafSense kafSense = concepts.get(i);
            if (!kafSense.getResource().equalsIgnoreCase(resource)) {
                boolean HAS = false;
                for (int j = 0; j < this.concepts.size(); j++) {
                    KafSense sense = this.concepts.get(j);
                    if (kafSense.getSensecode().equals(sense.getSensecode())) {
                        HAS = true;
                        break;
                    }
                }
                if (!HAS) {
                    addConcept(kafSense);
                }
            }
        }
        this.concepts.addAll(concepts);
    }

    public void addConcept(KafSense concept) {
        if (!concept.getSensecode().isEmpty()) {
            this.concepts.add(concept);
        }
    }


    public void addToJenaModel(Model model, Resource type, boolean VERBOSE_MENTION) {
        Resource resource = model.createResource(this.getURI());


        //// Top phrase
        String topLabel = this.getTopPhraseAsLabel();
        if (!topLabel.isEmpty()) {
            resource.addProperty(RDFS.label, model.createLiteral(this.getTopPhraseAsLabel()));
            //// instead of
            for (int i = 0; i < phraseCounts.size(); i++) {
                PhraseCount phraseCount = phraseCounts.get(i);
                // resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhraseCount()));
                if (!phraseCount.getPhrase().equalsIgnoreCase(getTopPhraseAsLabel()) && goodPhrase(phraseCount)) {
                    resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhrase()));
                }
            }
        }

        if (type.getLocalName().equalsIgnoreCase("Event")) {
            resource.addProperty(RDF.type, type);
        }

        addConceptsToResource(resource, model);

        for (int i = 0; i < nafMentions.size(); i++) {
            NafMention nafMention = nafMentions.get(i);
            Property property = model.createProperty(ResourcesUri.gaf + "denotedBy");
            Resource targetResource = null;
            if (VERBOSE_MENTION) {
                targetResource = model.createResource(nafMention.toStringFull());
            }
            else {
                targetResource = model.createResource(nafMention.toString());

            }
            resource.addProperty(property, targetResource);
        }
    }

    void addConceptsToResource (Resource resource, Model model) {
        for (int i = 0; i < concepts.size(); i++) {
            KafSense kafSense = concepts.get(i);

            /// skipping conditions
            if (kafSense.getResource().equalsIgnoreCase("verbnet")) {
                continue;
            }
            if (kafSense.getResource().equalsIgnoreCase("wordnet")) {
              //  continue;
            }
            if (kafSense.getResource().equalsIgnoreCase("propbank")) {
                continue;
            }
            if (kafSense.getResource().equalsIgnoreCase("nombank")) {
                continue;
            }
            if (this.getURI().indexOf("entities/")==-1) {
                if (kafSense.getResource().toLowerCase().startsWith("vua-type-reranker")) {
                    continue;
                }
                if (kafSense.getResource().isEmpty() && this.getURI().startsWith("http://dbpedia.org")) {
                    continue;
                }
                if (kafSense.getResource().toLowerCase().startsWith("spotlight")) {
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
                }
            }
            String nameSpaceType = getNameSpaceTypeReference(kafSense);
            if (!nameSpaceType.isEmpty()) {
              //  System.out.println("nameSpaceType = " + nameSpaceType);
                Resource conceptResource = model.createResource(nameSpaceType);
                resource.addProperty(RDF.type, conceptResource);
            }

        }
    }

    static public String getNameSpaceTypeReference(KafSense kafSense) {
        String ref = "";
        if (kafSense.getResource().equalsIgnoreCase("verbnet")) {
            ref = ResourcesUri.vn + kafSense.getSensecode();
        }
        else if (kafSense.getResource().equalsIgnoreCase("wordnet")) {
            String senseCode = kafSense.getSensecode();
            if (senseCode.toLowerCase().startsWith("ili-30-")) {
                senseCode = "eng"+senseCode.substring(6);
            }
          //  System.out.println(senseCode);
            ref = ResourcesUri.wn + senseCode;
        }
        else if (kafSense.getSensecode().toLowerCase().startsWith("ili-30-")) {
            /// this is needed since we get very different resource values from WSD:
            /**
             <externalRef resource="wn30g.bin64" reference="ili-30-14564306-n" confidence="0.5717984"/>
             <externalRef resource="WordNet-3.0" reference="ili-30-13260936-n" confidence="8.0"/>
             */
            String senseCode = kafSense.getSensecode();
            senseCode = "eng"+senseCode.substring(6);
           // System.out.println(senseCode);
            ref = ResourcesUri.wn + senseCode;
        }
        else if (kafSense.getSensecode().toLowerCase().startsWith("eng-30-")) {
            /// this is needed since we get very different resource values from WSD:
            /**
             <externalRef resource="wn30g.bin64" reference="ili-30-14564306-n" confidence="0.5717984"/>
             <externalRef resource="WordNet-3.0" reference="ili-30-13260936-n" confidence="8.0"/>
             */
            String senseCode = kafSense.getSensecode();
            senseCode = "eng"+senseCode.substring(6);
           // System.out.println(senseCode);
            ref = ResourcesUri.wn + senseCode;
        }
        else if (kafSense.getResource().equalsIgnoreCase("cornetto")) {
            ref = ResourcesUri.cornetto + kafSense.getSensecode();
        } else if (kafSense.getResource().equalsIgnoreCase("framenet")) {
            ref = ResourcesUri.fn + kafSense.getSensecode();
        } else if (kafSense.getResource().equalsIgnoreCase("framenet+")) {
            ref = ResourcesUri.fn + kafSense.getSensecode();
        } else if (kafSense.getResource().equalsIgnoreCase("framenet-")) {
            ref = ResourcesUri.fn + kafSense.getSensecode();
        } else if (kafSense.getResource().equalsIgnoreCase("eso")) {
            ref = ResourcesUri.eso + kafSense.getSensecode();
        } else if (kafSense.getResource().equalsIgnoreCase("eso+")) {
            ref = ResourcesUri.eso + kafSense.getSensecode();
        } else if (kafSense.getResource().equalsIgnoreCase("eso-")) {
            ref = ResourcesUri.eso + kafSense.getSensecode();
        } else if (kafSense.getResource().equalsIgnoreCase("propbank")) {
            ref = ResourcesUri.pb + kafSense.getSensecode();
        } else if (kafSense.getResource().equalsIgnoreCase("nombank")) {
            ref = ResourcesUri.nb + kafSense.getSensecode();
        } else if (kafSense.getResource().toLowerCase().startsWith("spotlight")
                   ||
                   kafSense.getResource().toLowerCase().startsWith("vua-type-reranker")
                  ) {
            ref = kafSense.getSensecode(); /// keep it as it is since the dbpedia URL is complete as it comes from spotlight
         // ref = Util.cleanDbpediaUri(kafSense.getSensecode(), ResourcesUri.dbp);
        }
        //// checking the senseCode

        else if (kafSense.getSensecode().indexOf(ResourcesUri.dbp) > -1) {
            ref = kafSense.getSensecode(); /// keep it as it is since the dbpedia URL is complete as it comes from spotlight
          //ref =  Util.cleanDbpediaUri(kafSense.getSensecode(), ResourcesUri.dbp);
        }
        else if (kafSense.getSensecode().equalsIgnoreCase("source")) {
            ref = ResourcesUri.nwrontology + FrameTypes.SOURCE;
        } else if (kafSense.getSensecode().equalsIgnoreCase("cognition")) {
            ref = ResourcesUri.nwrontology + FrameTypes.SOURCE;
        } else if (kafSense.getSensecode().toLowerCase().startsWith("speech")) {
            ref = ResourcesUri.nwrontology + FrameTypes.SOURCE;
        } else if (kafSense.getSensecode().equalsIgnoreCase("communication")) {
            ref = ResourcesUri.nwrontology + FrameTypes.SOURCE;
        } else if (kafSense.getSensecode().equalsIgnoreCase("grammatical")) {
            ref = ResourcesUri.nwrontology + FrameTypes.GRAMMATICAL;
        } else if (kafSense.getSensecode().equalsIgnoreCase("contextual")) {
            ref = ResourcesUri.nwrontology + FrameTypes.CONTEXTUAL;
        } else if (kafSense.getSensecode().equalsIgnoreCase("other")) {
            ref = ResourcesUri.nwrontology + FrameTypes.CONTEXTUAL;
        }

        else if (kafSense.getSensecode().isEmpty()) {
          //  System.out.println("kafSense.toString() = " + kafSense.toString());
            ref = ResourcesUri.nwrontology + "MISC";

        }
        else {
            ref = ResourcesUri.nwrontology + kafSense.getSensecode();
        }
       // System.out.println("ref = " + ref);

        //// PATCH TO FIX WRONG URIs GENERATED BY DUTCH PIPELINE
        //// 16 June 2015
            if (ref.contains("DBpedia:Organisation")) {
                ref = ResourcesUri.nwrontology + "ORG";
            }
            else if (ref.contains("DBpedia:Person")) {
                ref = ResourcesUri.nwrontology + "PER";
            }
            else if (ref.contains("DBpedia:Place")) {
                ref = ResourcesUri.nwrontology + "LOC";
            }
            else if (ref.contains("DBpedia:")) {
                ref = ResourcesUri.nwrontology + "MISC";
            }
        //System.out.println("ref = " + ref);
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