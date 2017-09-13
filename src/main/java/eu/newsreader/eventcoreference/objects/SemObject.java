package eu.newsreader.eventcoreference.objects;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.naf.ResourcesUri;
import eu.newsreader.eventcoreference.output.JenaSerialization;
import eu.newsreader.eventcoreference.util.FrameTypes;
import eu.newsreader.eventcoreference.util.Util;
import org.openrdf.model.vocabulary.SKOS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 3/28/12
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class SemObject implements Serializable {
    /*
    @TODO remove serialVersionUID
 */
    private static final long serialVersionUID = 5418194900752121897L;

    static public final String EVENT = "EVENT";
    static public final String ENTITY = "ENTITY";
    static public final String TIME = "TIME";
    static public final String NONENTITY = "NONENTITY";

    private String id;                              //
    private String type;
    private String uri;                             //
    private ArrayList<String> nafIds;
    private double score;
    private ArrayList<KafSense> concepts;          //
    private ArrayList<KafTopic> topics;
    private ArrayList<PhraseCount> phraseCounts;   //
    private ArrayList<KafSense> lcs;
    private ArrayList<KafSense> hypers;
    private String label;
    private ArrayList<NafMention> nafMentions;    //

    public SemObject(String type) {
        this.nafMentions = new ArrayList<NafMention>();
        this.id = "";
        this.type = type;
        this.nafIds = new ArrayList<String>();
        this.label = "";
        this.uri = "";
        this.lcs = new ArrayList<KafSense>();
        this.hypers = new ArrayList<KafSense>();
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

    public void getUriForTopicLabel(HashMap<String, String> uriMap) {
        for (int i = 0; i < topics.size(); i++) {
            KafTopic kafTopic = topics.get(i);
            if (!kafTopic.getTopic().isEmpty()) {
                if (uriMap.containsKey(kafTopic.getTopic())) {
                    String uri = uriMap.get(kafTopic.getTopic());
                    kafTopic.setUri(uri);
                } else {
                    if (kafTopic.getUri().isEmpty() && !kafTopic.getTopic().isEmpty()) {
                        try {
                            String aUri = ResourcesUri.nwrontology + "topic/"+URLEncoder.encode(kafTopic.getTopic(), "UTF-8");
                            kafTopic.setUri(aUri);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
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
        for (int j = 0; j < nafMentions.size(); j++) {
            NafMention nafMention = nafMentions.get(j);
            nafMention.addFactuality(kafSaxParser);
            nafMention.addOpinion(kafSaxParser);
        }
    }

    public void addPastFactuality() {
        for (int i = 0; i < nafMentions.size(); i++) {
            NafMention nafMention = nafMentions.get(i);
            if (nafMention.getFactuality().size()==0) {
                KafFactuality kafFactuality = new KafFactuality();
                KafFactValue kafFactValue = new KafFactValue();
                kafFactValue.setResource(KafFactValue.resourceAttributionTense);
                kafFactValue.setValue(KafFactValue.PAST);
                kafFactuality.addFactValue(kafFactValue);
                nafMention.getFactuality().add(kafFactuality);

            }
            else {
                for (int j = 0; j < nafMention.getFactuality().size(); j++) {
                    KafFactuality kafFactuality = nafMention.getFactuality().get(j);
                    boolean SET = false;
                    for (int k = 0; k < kafFactuality.getFactValueArrayList().size(); k++) {
                        KafFactValue kafFactValue = kafFactuality.getFactValueArrayList().get(k);
                        if (kafFactValue.getResource().equals(KafFactValue.resourceAttributionTense)) {
                            kafFactValue.setValue(KafFactValue.PAST);
                            SET = true;
                        }
                    }
                    if (!SET) {
                        KafFactValue kafFactValue = new KafFactValue();
                        kafFactValue.setResource(KafFactValue.resourceAttributionTense);
                        kafFactValue.setValue(KafFactValue.PAST);
                        kafFactuality.addFactValue(kafFactValue);
                    }
                }
            }
            /*for (int j = 0; j < nafMention.getFactuality().size(); j++) {
                KafFactuality kafFactuality = nafMention.getFactuality().get(j);
                System.out.println("kafFactuality.getPrediction() = " + kafFactuality.getPrediction());
            }*/
        }

    }

    public void addRecentFactuality() {
        for (int i = 0; i < nafMentions.size(); i++) {
            NafMention nafMention = nafMentions.get(i);
            if (nafMention.getFactuality().size()==0) {
                KafFactuality kafFactuality = new KafFactuality();
                KafFactValue kafFactValue = new KafFactValue();
                kafFactValue.setResource(KafFactValue.resourceAttributionTense);
                kafFactValue.setValue(KafFactValue.RECENT);
                kafFactuality.addFactValue(kafFactValue);
                nafMention.getFactuality().add(kafFactuality);

            }
            else {
                for (int j = 0; j < nafMention.getFactuality().size(); j++) {
                    KafFactuality kafFactuality = nafMention.getFactuality().get(j);
                //    System.out.println("kafFactuality.getPrediction() = " + kafFactuality.getPrediction());
                    boolean SET = false;
                    for (int k = 0; k < kafFactuality.getFactValueArrayList().size(); k++) {
                        KafFactValue kafFactValue = kafFactuality.getFactValueArrayList().get(k);
                        if (kafFactValue.getResource().equals(KafFactValue.resourceAttributionTense)) {
                            /// we overwrite any other tense value!!!!!
                            kafFactValue.setValue(KafFactValue.RECENT);
                            SET = true;
                        }
                    }
                    if (!SET) {
                        KafFactValue kafFactValue = new KafFactValue();
                        kafFactValue.setResource(KafFactValue.resourceAttributionTense);
                        kafFactValue.setValue(KafFactValue.RECENT);
                        kafFactuality.addFactValue(kafFactValue);
                    }
                 //   System.out.println("kafFactuality.getPrediction() = " + kafFactuality.getPrediction());
                }
            }
        }

    }

    public void setConcepts(ArrayList<KafSense> concepts) {
        this.concepts = concepts;
    }

    public String getPhrase() {
        String phrase = "";
        for (int i = 0; i < phraseCounts.size(); i++) {
            PhraseCount phraseCount = phraseCounts.get(i);
            if (phrase.isEmpty()) {
                phraseCount.getPhrase();
            }
            else {
                break;
            }
        }
        return phrase;
    }

    public ArrayList<PhraseCount> getPhraseCounts() {
        return phraseCounts;
    }

    public ArrayList<String> getUniquePhrases() {
        ArrayList<String> phrases =  new ArrayList<String>();
        for (int i = 0; i < phraseCounts.size(); i++) {
            PhraseCount phraseCount = phraseCounts.get(i);
            if (!phraseCount.getPhrase().isEmpty()) {
                if (!phrases.contains(phraseCount.getPhrase())) {
                    phrases.add(phraseCount.getPhrase());
                }
            }
        }
        return phrases;
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
                    System.err.println("no KafTerm for id = " + termId);
                }
            }
            if (!phrase.isEmpty()) {
                addPhraseCounts(phrase.trim());
            }
            else {
                System.err.println("Empty phrase for mention = "+ nafMention.toStringFull());
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
            boolean NEW = true;
            for (int j = 0; j < this.nafMentions.size(); j++) {
                NafMention mention = this.nafMentions.get(j);
                if (mention.sameMention(nafMention)) {
                    NEW = false;
                    break;
                }
            }
            if (NEW) {
                this.nafMentions.add(nafMention);
            }
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

    public void addHyper(KafSense concept) {
            if (!concept.getSensecode().isEmpty()) {
                this.hypers.add(concept);
            }
        }

        public void addHypers(ArrayList<KafSense> hypers) {
            for (int i = 0; i < hypers.size(); i++) {
                KafSense kafSense = hypers.get(i);
                this.addHyper(kafSense);
            }
        }

        public void setHypers(ArrayList<KafSense> hypers) {
            this.hypers = hypers;
        }

    public ArrayList<KafSense> getHypers() {
        return hypers;
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
            if (topSense.getChildren().size()>0) {
                //// Crosslingual mapping
/*
                <externalRef confidence="1.0" reference="http://es.dbpedia.org/resource/Fuerza_Aérea_de_los_Estados_Unidos" reftype="es" resource="spotlight_v1" source="es">
                <externalRef confidence="1.0" reference="http://dbpedia.org/resource/United_States_Air_Force" reftype="en" resource="wikipedia-db-esEn" source="es"/>
                </externalRef>

               <externalRef resource="spotlight_v1" reference="http://it.dbpedia.org/resource/Apple" confidence="1.0" reftype="it" source="it">
               <externalRef resource="wikipedia-db-itEn" reference="http://dbpedia.org/resource/Apple_Inc." confidence="1.0" reftype="en" source="it" />
        </externalRef>

*/
                for (int i = 0; i < topSense.getChildren().size(); i++) {
                    KafSense kafSense = topSense.getChildren().get(i);
                    //  System.out.println("kafSense.getRefType() = " + kafSense.getRefType());
                    if (kafSense.getRefType().equals("en")) {
                        id = getNameSpaceTypeReference(kafSense); //kafSense.getSensecode();
                        // System.out.println("uri = " + uri);
                        break;
                    }
                }

            }
            else {
                if ((topSense.getResource().equalsIgnoreCase("spotlight_v1"))
                        || (topSense.getSensecode().indexOf("dbpedia.org/") > -1)
                        ) {
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
        Integer top = -1;
        String label = "";
        for (int i = 0; i < phraseCounts.size(); i++) {
            PhraseCount phraseCount = phraseCounts.get(i);
            if (phraseCount.getPhrase().length()>2 || label.isEmpty()) {
                if (goodPhrase(phraseCount) || label.isEmpty()) {
                    if (phraseCount.getCount() > top) {
                        top = phraseCount.getCount();
                        label = phraseCount.getPhrase();
/*
                        if (Util.hasAlphaNumeric(phraseCount.getPhrase())) {
                            top = phraseCount.getCount();
                            label = phraseCount.getPhrase();
                        } else {
                        }
*/
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
            Property property = model.createProperty(ResourcesUri.skos+SKOS.PREF_LABEL.getLocalName());
            resource.addProperty(property, model.createLiteral(this.getTopPhraseAsLabel()));
            //// instead of
            for (int i = 0; i < phraseCounts.size(); i++) {
                PhraseCount phraseCount = phraseCounts.get(i);
                // resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhraseCount()));
/*                if (!phraseCount.getPhrase().equalsIgnoreCase(getTopPhraseAsLabel()) && goodPhrase(phraseCount)) {
                    resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhrase()));
                }*/
                if (goodPhrase(phraseCount)) {
                    resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhrase()));
                }
            }
        }

        if (type.getLocalName().equalsIgnoreCase("Event")) {
            resource.addProperty(RDF.type, type);
        }

        addConceptsToResource(resource, model);

        for (int i = 0; i < this.getTopics().size(); i++) {
            KafTopic kafTopic = this.getTopics().get(i);
            if (!kafTopic.getUri().isEmpty()) {
                Property property = model.createProperty(ResourcesUri.skos + SKOS.RELATED_MATCH.getLocalName());
                // resource.addProperty(property, model.createLiteral(kafTopic.getUri()));
                Resource topicResource = model.createResource(kafTopic.getUri());
                if (!topicResource.getURI().isEmpty() && !topicResource.getURI().equals("<>")) {
                    resource.addProperty(property, topicResource);
                }
            }
        }

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

    public void addToJenaSimpleModel(Model model, Resource type) {
        Resource resource = model.createResource(this.getURI());
        //// Top phrase
        String topLabel = this.getTopPhraseAsLabel();
        if (!topLabel.isEmpty()) {
            //Property property = model.createProperty(ResourcesUri.skos+SKOS.PREF_LABEL.getLocalName());
            //resource.addProperty(property, model.createLiteral(this.getTopPhraseAsLabel()));
            //// instead of

            if (type.equals(Sem.Event) ) resource = model.createResource(this.getURI()+"_"+topLabel);
            for (int i = 0; i < phraseCounts.size(); i++) {
                PhraseCount phraseCount = phraseCounts.get(i);
                // resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhraseCount()));
/*                if (!phraseCount.getPhrase().equalsIgnoreCase(getTopPhraseAsLabel()) && goodPhrase(phraseCount)) {
                    resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhrase()));
                }*/
                if (goodPhrase(phraseCount)) {
                    resource.addProperty(RDFS.label, model.createLiteral(phraseCount.getPhrase()));
                }
            }
        }

        addSimpleConceptsToResource(resource, model);


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
                if (kafSense.getResource().toLowerCase().startsWith("doublelinkentities")) {
                    continue;
                }
                if (kafSense.getResource().toLowerCase().startsWith("dominantentities")) {
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
                if (type.equals(NONENTITY)) {
                     //  System.out.println("nameSpaceType = " + nameSpaceType);
                  //  Property property = model.createProperty(SKOS.RELATED_MATCH.getLocalName());
                    Property property = model.createProperty(SKOS.RELATED_MATCH.toString());
                    Resource conceptResource = model.createResource(nameSpaceType);
                    resource.addProperty(property, conceptResource);
                    conceptResource = model.createResource(ResourcesUri.nwrontology+SemObject.NONENTITY);
                    resource.addProperty(RDF.type, conceptResource);

                }
                /*else if (type.equals(ENTITY)) {
                    Property property = model.createProperty(SKOS.RELATED_MATCH.toString());
                    Resource conceptResource = model.createResource(nameSpaceType);
                    resource.addProperty(property, conceptResource);
                    conceptResource = model.createResource(ResourcesUri.nwrontology+SemObject.ENTITY);
                    resource.addProperty(RDF.type, conceptResource);
                }*/
                else {
                    Resource conceptResource = model.createResource(nameSpaceType);
                    resource.addProperty(RDF.type, conceptResource);
                }
            }
        }
    }

    void addSimpleConceptsToResource (Resource resource, Model model) {
        for (int i = 0; i < concepts.size(); i++) {
            KafSense kafSense = concepts.get(i);

            if (this.getURI().indexOf("entities/")==-1) {
                if (kafSense.getResource().toLowerCase().startsWith("vua-type-reranker")) {
                    continue;
                }
                if (kafSense.getResource().toLowerCase().startsWith("doublelinkentities")) {
                    continue;
                }
                if (kafSense.getResource().toLowerCase().startsWith("dominantentities")) {
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
                if (type.equals(NONENTITY)) {
                     //  System.out.println("nameSpaceType = " + nameSpaceType);
                  //  Property property = model.createProperty(SKOS.RELATED_MATCH.getLocalName());
                    Property property = model.createProperty(SKOS.RELATED_MATCH.toString());
                    Resource conceptResource = model.createResource(nameSpaceType);
                    resource.addProperty(property, conceptResource);
                    conceptResource = model.createResource(ResourcesUri.nwrontology+SemObject.NONENTITY);
                    resource.addProperty(RDF.type, conceptResource);

                }
                /*else if (type.equals(ENTITY)) {
                    Property property = model.createProperty(SKOS.RELATED_MATCH.toString());
                    Resource conceptResource = model.createResource(nameSpaceType);
                    resource.addProperty(property, conceptResource);
                    conceptResource = model.createResource(ResourcesUri.nwrontology+SemObject.ENTITY);
                    resource.addProperty(RDF.type, conceptResource);
                }*/
                else {
                    Resource conceptResource = model.createResource(nameSpaceType);
                    if (conceptResource.getNameSpace().indexOf("framenet")>-1) {
                        resource.addProperty(RDF.type, conceptResource);
                    }
                }
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
            if (JenaSerialization.iliReader!=null) {
                    if (JenaSerialization.iliReader.synsetToILIMap.containsKey(senseCode)) {
                        senseCode = JenaSerialization.iliReader.synsetToILIMap.get(senseCode);
                        ref = ResourcesUri.ili + senseCode;
                    } else {
                        ref = ResourcesUri.wn + senseCode;
                    }
            }
        }
        else if (kafSense.getSensecode().toLowerCase().startsWith("ili-30-")) {
            /// this is needed since we get very different resource values from WSD:
            /**
             <externalRef resource="wn30g.bin64" reference="ili-30-14564306-n" confidence="0.5717984"/>
             <externalRef resource="WordNet-3.0" reference="ili-30-13260936-n" confidence="8.0"/>
             */
            String senseCode = kafSense.getSensecode();
            senseCode = "eng"+senseCode.substring(6);
            if (JenaSerialization.iliReader!=null) {
                if (JenaSerialization.iliReader.synsetToILIMap.containsKey(senseCode)) {
                    senseCode = JenaSerialization.iliReader.synsetToILIMap.get(senseCode);
                    ref = ResourcesUri.ili + senseCode;
                } else {
                    ref = ResourcesUri.wn + senseCode;
                }
            }
        }
        else if (kafSense.getSensecode().toLowerCase().startsWith("eng-30-")) {
            /// this is needed since we get very different resource values from WSD:
            /**
             <externalRef resource="wn30g.bin64" reference="ili-30-14564306-n" confidence="0.5717984"/>
             <externalRef resource="WordNet-3.0" reference="ili-30-13260936-n" confidence="8.0"/>
             */
            String senseCode = kafSense.getSensecode();
            senseCode = "eng"+senseCode.substring(6);
            if (JenaSerialization.iliReader!=null) {
                if (JenaSerialization.iliReader.synsetToILIMap.containsKey(senseCode)) {
                    senseCode = JenaSerialization.iliReader.synsetToILIMap.get(senseCode);
                    ref = ResourcesUri.ili + senseCode;
                } else {
                    ref = ResourcesUri.wn + senseCode;
                }
            }
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
        } else if (kafSense.getResource().toLowerCase().startsWith("spotlight") ||
                   kafSense.getSource().toLowerCase().startsWith("spotlight")
                   ||
                   kafSense.getResource().toLowerCase().startsWith("vua-type-reranker") ||
                   kafSense.getSource().toLowerCase().startsWith("vua-type-reranker")
                   ||
                   kafSense.getResource().toLowerCase().startsWith("wikipedia") ||
                   kafSense.getSource().toLowerCase().startsWith("wikipedia")
                  ) {
            ref = kafSense.getSensecode(); /// keep it as it is since the dbpedia URL is complete as it comes from spotlight
         // ref = Util.cleanDbpediaUri(kafSense.getSensecode(), ResourcesUri.dbp);
        }
        //// checking the senseCode

       // else if (kafSense.getSensecode().indexOf(ResourcesUri.dbp) > -1) {
        else if (kafSense.getSensecode().indexOf("dbpedia.org") > -1) {
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