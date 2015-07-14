package eu.newsreader.eventcoreference.objects;

import eu.kyotoproject.kaf.*;
import vu.wntools.wordnet.WordnetData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 10/10/14.
 */
public class CorefResultSet {
    private String sourceLemma;
    private ArrayList<ArrayList<CorefTarget>> sources;
    private ArrayList<CorefMatch> targets;
    private ArrayList<KafSense> bestSenses;

    public CorefResultSet(String sourceLemma,ArrayList<CorefTarget> source) {
        this.sourceLemma = sourceLemma;
        this.sources = new ArrayList<ArrayList<CorefTarget>>();
        this.sources.add(source);
        this.targets = new ArrayList<CorefMatch>();
        this.bestSenses = new ArrayList<KafSense>();
    }

    public String getSourceLemma() {
        return sourceLemma;
    }

    public ArrayList<ArrayList<CorefTarget>> getSources() {
        return sources;
    }

    public ArrayList<CorefMatch> getTargets() {
        return targets;
    }

    public CorefTarget getLastSource () {
        if (sources.isEmpty()) {
            return null;
        }
        else {
            ArrayList<CorefTarget> set = sources.get(sources.size()-1);
            return set.get(set.size()-1);
        }
    }
    public void setTargets(ArrayList<CorefMatch> targets) {
        this.targets = targets;
    }

    public void addTarget(CorefMatch target) {
        this.targets.add(target);
    }
    public void addSource(ArrayList<CorefTarget> corefSets) {
            sources.add(corefSets);
    }

    public ArrayList<KafSense> getBestSenses() {
        return bestSenses;
    }

    public void setBestSenses(ArrayList<KafSense> bestSenses) {
        this.bestSenses = bestSenses;
    }

    public boolean hasBestSense (KafSense kafSense) {
        for (int i = 0; i < bestSenses.size(); i++) {
            KafSense sense = bestSenses.get(i);
            if (sense.getSensecode().equals(kafSense.getSensecode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * This function assumes that a specific WSD output is used or all senses are considered from multiple systems
     * takes all senses from that resource
     * @param kafSaxParser
     * @param WNRESOURCE
     */
    public void getAllSenses (KafSaxParser kafSaxParser, String WNRESOURCE) {
        double bestScore = 0;
        ArrayList<KafSense> kafSenses = new ArrayList<KafSense>();
        for (int i = 0; i < sources.size(); i++) {
            ArrayList<CorefTarget> corefTargets = sources.get(i);
            for (int j = 0; j < corefTargets.size(); j++) {
                CorefTarget corefTarget = corefTargets.get(j);
                KafTerm kafTerm = kafSaxParser.getTerm(corefTarget.getId());
                if (kafTerm!=null) {
                    for (int k = 0; k < kafTerm.getSenseTags().size(); k++) {
                        KafSense kafSense = kafTerm.getSenseTags().get(k);
                        if (!WNRESOURCE.isEmpty()) {
                            //// next function should be changed to getSource when this attribute is used
                            if ((kafSense.getResource().toLowerCase().indexOf(WNRESOURCE)==-1) &&
                                (kafSense.getSource().toLowerCase().indexOf(WNRESOURCE)==-1)){
                                continue;
                                //// this sense comes from the wrong resource
                            }
                        }
                        else {
                            //// all senses are valid
                        }
                        kafSenses.add(kafSense); // adding any KafSense
                        if (kafSense.getConfidence()> bestScore) {
                            bestScore = kafSense.getConfidence();
                            //  kafSenses.add(kafSense); /// assume that first references are easier to resolve
                        }

                    }
                }
            }
        }
        for (int i = 0; i < kafSenses.size(); i++) {
            KafSense kafSense = kafSenses.get(i);
            if (!hasBestSense(kafSense)) {
                this.bestSenses.add(kafSense);
            }
        }
    }

    /**
     * This function takes the senses that exceed the proportional threshold or are equal to it.
     * It considers all the CorefTargets in a coreference set. Each target matches a term  and each term has senses with scores from various WSD systems
     * @param kafSaxParser
     * @param BESTSENSETHRESHOLD
     * @param WNRESOURCE
     */
  /*  public void getBestSenses (KafSaxParser kafSaxParser, double BESTSENSETHRESHOLD, String WNRESOURCE) {
        double bestScore = 0;
        ArrayList<KafSense> kafSenses = new ArrayList<KafSense>();
        for (int i = 0; i < sources.size(); i++) {
            ArrayList<CorefTarget> corefTargets = sources.get(i);
            for (int j = 0; j < corefTargets.size(); j++) {
                CorefTarget corefTarget = corefTargets.get(j);
                KafTerm kafTerm = kafSaxParser.getTerm(corefTarget.getId());
                if (kafTerm!=null) {
                    for (int k = 0; k < kafTerm.getSenseTags().size(); k++) {
                        KafSense kafSense = kafTerm.getSenseTags().get(k);
                        if (!WNRESOURCE.isEmpty()) {
                            //// next function should be changed to getSource when this attribute is used
                            if ((kafSense.getResource().toLowerCase().indexOf(WNRESOURCE)==-1) &&
                                    (kafSense.getSource().toLowerCase().indexOf(WNRESOURCE)==-1)){
                                continue;
                                //// this sense comes from the wrong resource
                            }
                        }
                        else {
                            /// all senses are valid
                        }
                        kafSenses.add(kafSense); // adding any KafSense
                        if (kafSense.getConfidence()> bestScore) {
                            bestScore = kafSense.getConfidence();
                            //  kafSenses.add(kafSense); /// assume that first references are easier to resolve
                        }

                    }
                }
            }
        }
        for (int i = 0; i < kafSenses.size(); i++) {
            KafSense kafSense = kafSenses.get(i);
            double proportionBestScore = kafSense.getConfidence()/bestScore;
            if (proportionBestScore>=BESTSENSETHRESHOLD) {
               // System.out.println("this.getSources().toString() = " + this.getSources().toString());
               // System.out.println("proportionBestScore = " + proportionBestScore);
                this.bestSenses.add(kafSense);
            }
        }
        //System.out.println("bestSenses = " + bestSenses.toString());
    }*/

    /**
     * If no specific WSD system is selected, it builds the sets of sense per WSD system and determines the best score for each
     * Next, it keeps the senses with the scores proportional to the top score that are equal or above the threshold
     * It then takes the average proportional scores across all WSD systems for each sense and
     * checks if these are still above or equal to the threshold. In this way, the top senses of all system combined are selected
     * and other drops out.
     * @param kafSaxParser
     * @param BESTSENSETHRESHOLD
     */
/*    public void getBestSenses (KafSaxParser kafSaxParser, double BESTSENSETHRESHOLD) {
        HashMap<String, ArrayList<KafSense>> resourceSenseMap = new HashMap<String, ArrayList<KafSense>>();
        HashMap<String, Double> resourceScoreMap = new HashMap<String, Double>();

        for (int i = 0; i < sources.size(); i++) {
            ArrayList<CorefTarget> corefTargets = sources.get(i);
            for (int j = 0; j < corefTargets.size(); j++) {
                CorefTarget corefTarget = corefTargets.get(j);
                KafTerm kafTerm = kafSaxParser.getTerm(corefTarget.getId());
                if (kafTerm!=null) {
                    for (int k = 0; k < kafTerm.getSenseTags().size(); k++) {
                        KafSense kafSense = kafTerm.getSenseTags().get(k);
                        String resource = kafSense.getResource();
                        if (resourceSenseMap.containsKey(resource)) {
                            ArrayList<KafSense> senses = resourceSenseMap.get(resource);
                            senses.add(kafSense);
                            resourceSenseMap.put(resource, senses);
                        }
                        else {
                            ArrayList<KafSense> senses = new ArrayList<KafSense>();
                            senses.add(kafSense);
                            resourceSenseMap.put(resource, senses);
                        }
                        if (resourceScoreMap.containsKey(resource)) {
                            Double score = resourceScoreMap.get(resource);
                            if (kafSense.getConfidence()> score) {
                                resourceScoreMap.put(resource, kafSense.getConfidence());
                            }
                        }
                        else {
                            resourceScoreMap.put(resource, kafSense.getConfidence());
                        }
                    }
                }
            }
        }
        HashMap<String, ArrayList<Double>> senseScores = new HashMap<String, ArrayList<Double>>();
        Set keySet = resourceSenseMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            Double topScore = resourceScoreMap.get(key);
            ArrayList<KafSense> kafSenses = resourceSenseMap.get(key);
            for (int i = 0; i < kafSenses.size(); i++) {
                KafSense kafSense = kafSenses.get(i);
                double proportionBestScore = kafSense.getConfidence()/topScore;
                if (proportionBestScore>=BESTSENSETHRESHOLD) {
                    // System.out.println("this.getSources().toString() = " + this.getSources().toString());
                    // System.out.println("proportionBestScore = " + proportionBestScore);
                    if (senseScores.containsKey(kafSense.getSensecode())) {
                        ArrayList<Double> scores = senseScores.get(kafSense.getSensecode());
                        scores.add(proportionBestScore);
                        senseScores.put(kafSense.getSensecode(), scores);
                    }
                    else {
                        ArrayList<Double> scores = new ArrayList<Double>();
                        scores.add(proportionBestScore);
                        senseScores.put(kafSense.getSensecode(), scores);
                    }
                }
            }
        }
        keySet = senseScores.keySet();
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            Double combinedScore = new Double(0);
            ArrayList<Double> topScores = senseScores.get(key);
            for (int i = 0; i < topScores.size(); i++) {
                Double aDouble = topScores.get(i);
                combinedScore += aDouble;
            }
            combinedScore = combinedScore/topScores.size();
            if (combinedScore>=BESTSENSETHRESHOLD) {
                KafSense kafSense = new KafSense();
                kafSense.setSensecode(key);
                kafSense.setConfidence(combinedScore);
                this.bestSenses.add(kafSense);
            }
        }
    }*/

    /**
     * We cumulate the scores of the senses across all targets of the corefset and per WSD system.
     * If WNSOURCE is empty, the map has different sets for each resource present in the term layer.
     * IF WNSOURCE is NOT empty, we only consider the matching output of the WSD system
     * We make the score proportional to the best scoring sense per system and we average over the different systems.
     * Only the senses proportionally over the threshold are considered.
     * @param kafSaxParser
     * @param BESTSENSETHRESHOLD
     */
    public void getBestSensesAfterCumulation (KafSaxParser kafSaxParser, double BESTSENSETHRESHOLD, String WNRESOURCE) {
        HashMap<String, ArrayList<KafSense>> resourceSenseMap = new HashMap<String, ArrayList<KafSense>>();
        HashMap<String, Double> resourceScoreMap = new HashMap<String, Double>();

        for (int i = 0; i < sources.size(); i++) {
            ArrayList<CorefTarget> corefTargets = sources.get(i);
            for (int j = 0; j < corefTargets.size(); j++) {
                CorefTarget corefTarget = corefTargets.get(j);
                KafTerm kafTerm = kafSaxParser.getTerm(corefTarget.getId());
                if (kafTerm!=null) {
                    for (int k = 0; k < kafTerm.getSenseTags().size(); k++) {
                        KafSense kafSense = kafTerm.getSenseTags().get(k);
                        String resource = kafSense.getResource();
                        if (!WNRESOURCE.isEmpty()) {
                            //// next function should be changed to getSource when this attribute is used
                            if ((kafSense.getResource().toLowerCase().indexOf(WNRESOURCE)==-1) &&
                                    (kafSense.getSource().toLowerCase().indexOf(WNRESOURCE)==-1)){
                                continue;
                                //// this sense comes from the wrong resource
                            }
                        }
                        if (resourceSenseMap.containsKey(resource)) {
                            ArrayList<KafSense> senses = resourceSenseMap.get(resource);
                            boolean MATCH = false;
                            for (int l = 0; l < senses.size(); l++) {
                                KafSense sense = senses.get(l);
                                if (sense.getSensecode().equals(kafSense.getSensecode())) {
                                    /// we add the confidence score
                                    MATCH = true;
                                    sense.setConfidence((sense.getConfidence()+kafSense.getConfidence()));
                                    kafSense = sense;
                                    break;
                                }
                            }
                            if (!MATCH) senses.add(kafSense);
                            resourceSenseMap.put(resource, senses);
                        }
                        else {
                            ArrayList<KafSense> senses = new ArrayList<KafSense>();
                            senses.add(kafSense);
                            resourceSenseMap.put(resource, senses);
                        }
                        if (resourceScoreMap.containsKey(resource)) {
                            Double score = resourceScoreMap.get(resource);
                            if (kafSense.getConfidence()> score) {
                                resourceScoreMap.put(resource, kafSense.getConfidence());
                            }
                        }
                        else {
                            resourceScoreMap.put(resource, kafSense.getConfidence());
                        }
                    }
                }
            }
        }
        HashMap<String, ArrayList<Double>> senseScores = new HashMap<String, ArrayList<Double>>();
        Set keySet = resourceSenseMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            Double topScore = resourceScoreMap.get(key);
            ArrayList<KafSense> kafSenses = resourceSenseMap.get(key);
            for (int i = 0; i < kafSenses.size(); i++) {
                KafSense kafSense = kafSenses.get(i);
                double proportionBestScore = kafSense.getConfidence()/topScore;
                if (proportionBestScore>=BESTSENSETHRESHOLD) {
                    // System.out.println("this.getSources().toString() = " + this.getSources().toString());
                    // System.out.println("proportionBestScore = " + proportionBestScore);
                    if (senseScores.containsKey(kafSense.getSensecode())) {
                        ArrayList<Double> scores = senseScores.get(kafSense.getSensecode());
                        scores.add(proportionBestScore);
                        senseScores.put(kafSense.getSensecode(), scores);
                    }
                    else {
                        ArrayList<Double> scores = new ArrayList<Double>();
                        scores.add(proportionBestScore);
                        senseScores.put(kafSense.getSensecode(), scores);
                    }
                }
            }
        }
        keySet = senseScores.keySet();
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            Double combinedScore = new Double(0);
            ArrayList<Double> topScores = senseScores.get(key);
            for (int i = 0; i < topScores.size(); i++) {
                Double aDouble = topScores.get(i);
                combinedScore += aDouble;
            }
            combinedScore = combinedScore/topScores.size();
            if (combinedScore>=BESTSENSETHRESHOLD) {
                KafSense kafSense = new KafSense();
                kafSense.setSensecode(key);
                kafSense.setConfidence(combinedScore);
                this.bestSenses.add(kafSense);
            }
        }
    }

    public double getAverageMatchScore () {
        double avg = 0;
        for (int i = 0; i < targets.size(); i++) {
            CorefMatch corefMatch = targets.get(i);
            avg += corefMatch.getScore();
        }
        avg = avg/targets.size();
        return avg;
    }

    public ArrayList<String> getLowestSubsumers () {
        ArrayList<String> lcsList = new ArrayList<String>();
        for (int i = 0; i < targets.size(); i++) {
            CorefMatch corefMatch = targets.get(i);
            if (!corefMatch.getLowestCommonSubsumer().isEmpty()) {
                lcsList.add(corefMatch.getLowestCommonSubsumer());
/*
                if (!lcsList.contains(corefMatch.getLowestCommonSubsumer())) {
                    lcsList.add(corefMatch.getLowestCommonSubsumer());
                }
*/
            }
        }
        return lcsList;
    }


    public KafCoreferenceSet castToKafCoreferenceSet (WordnetData wordnetData) {
        KafCoreferenceSet kafCoreferenceSet = new KafCoreferenceSet();
        ArrayList<String> lcsList = getLowestSubsumers();
        KafSense kafSense = null;
        //System.out.println("lcsList = " + lcsList.toString());
        if (lcsList.size()==1) {
            //// there is only one subsumer so we trust it
            kafSense = new KafSense();
            kafSense.setSensecode(lcsList.get(0));
            kafSense.setConfidence(1);
            kafSense.setResource(wordnetData.getResource());
        }
        else if (lcsList.size()>1) {
            /// we need to choose from many
            kafSense = wordnetData.GetLowestCommonSubsumer(lcsList);
        }
        /// we add all the lemma sources
        for (int i = 0; i < sources.size(); i++) {
            ArrayList<CorefTarget> corefTargets = sources.get(i);
            kafCoreferenceSet.addSetsOfSpans(corefTargets);
        }
        if (kafSense!=null) {
            kafCoreferenceSet.addExternalReferences(kafSense);
            for (int i = 0; i < targets.size(); i++) {
                CorefMatch corefMatch = targets.get(i);
                for (int j = 0; j < corefMatch.getCorefTargets().size(); j++) {
                    ArrayList<CorefTarget> corefTargets = corefMatch.getCorefTargets().get(j);
                    kafCoreferenceSet.addSetsOfSpans(corefTargets);
                }
            }
        }
        else {
            /// no subsumers so only lemma matches are kept
        }
        return kafCoreferenceSet;
    }

    public KafCoreferenceSet castToKafCoreferenceSet (String wnResource) {
        KafCoreferenceSet kafCoreferenceSet = new KafCoreferenceSet();
        /// we add all the lemma sources
        for (int i = 0; i < sources.size(); i++) {
            ArrayList<CorefTarget> corefTargets = sources.get(i);
            kafCoreferenceSet.addSetsOfSpans(corefTargets);
        }
        for (int i = 0; i < targets.size(); i++) {
            CorefMatch corefMatch = targets.get(i);
            for (int j = 0; j < corefMatch.getCorefTargets().size(); j++) {
                ArrayList<CorefTarget> corefTargets = corefMatch.getCorefTargets().get(j);
                kafCoreferenceSet.addSetsOfSpans(corefTargets);
            }
            if (!corefMatch.getLowestCommonSubsumer().isEmpty()) {
                KafSense kafSense = new KafSense();
                kafSense.setSensecode(corefMatch.getLowestCommonSubsumer());
                kafSense.setConfidence(corefMatch.getScore());
                kafSense.setResource(wnResource);
                boolean hasSense = false;
                for (int j = 0; j < kafCoreferenceSet.getExternalReferences().size(); j++) {
                    KafSense sense = kafCoreferenceSet.getExternalReferences().get(j);
                    if (sense.getSensecode().equals(kafSense.getSensecode())) {
                        hasSense = true;
                        if (kafSense.getConfidence()>sense.getConfidence()) {
                            sense.setConfidence(kafSense.getConfidence());
                        }
                    }
                }
                if (!hasSense) {
                    kafCoreferenceSet.addExternalReferences(kafSense);
                }
            }
        }
        return kafCoreferenceSet;
    }

    public KafCoreferenceSet castToKafCoreferenceSet () {
        KafCoreferenceSet kafCoreferenceSet = new KafCoreferenceSet();
        /// we add all the lemma sources
        for (int i = 0; i < sources.size(); i++) {
            ArrayList<CorefTarget> corefTargets = sources.get(i);
            kafCoreferenceSet.addSetsOfSpans(corefTargets);
        }
        for (int i = 0; i < targets.size(); i++) {
            CorefMatch corefMatch = targets.get(i);
            for (int j = 0; j < corefMatch.getCorefTargets().size(); j++) {
                ArrayList<CorefTarget> corefTargets = corefMatch.getCorefTargets().get(j);
                kafCoreferenceSet.addSetsOfSpans(corefTargets);
            }
        }
        return kafCoreferenceSet;
    }

    public boolean hasSpan (ArrayList<CorefTarget> spans) {
        for (int i = 0; i < sources.size(); i++) {
            ArrayList<CorefTarget> corefTargets = sources.get(i);
            if (CorefTarget.hasSpan(spans, corefTargets)) {
                return true;
            }
        }
        for (int i = 0; i < targets.size(); i++) {
            CorefMatch corefMatch = targets.get(i);
            for (int j = 0; j < corefMatch.getCorefTargets().size(); j++) {
                ArrayList<CorefTarget> corefTargets = corefMatch.getCorefTargets().get(j);
                if (CorefTarget.hasSpan(spans, corefTargets)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasSpanAndLowestCommonSubsumer (CorefMatch match) {
        for (int i = 0; i < targets.size(); i++) {
            CorefMatch corefMatch = targets.get(i);
            for (int j = 0; j < corefMatch.getCorefTargets().size(); j++) {
                ArrayList<CorefTarget> corefTargets = corefMatch.getCorefTargets().get(j);
                if (match.hasSpan(corefTargets)) {
                    if (corefMatch.getLowestCommonSubsumer().equals(match.getLowestCommonSubsumer())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public void addTargetScore (CorefMatch target) {
        boolean NEW = true;
        for (int i = 0; i < targets.size(); i++) {
            CorefMatch corefMatch = targets.get(i);
            for (int j = 0; j < corefMatch.getCorefTargets().size(); j++) {
                ArrayList<CorefTarget> corefTargets = corefMatch.getCorefTargets().get(j);
                if (corefMatch.hasSpan(corefTargets)) {
                    NEW = false;
                    if (target.getScore()>corefMatch.getScore()) {
                        corefMatch.setScore(target.getScore());
                        corefMatch.setLowestCommonSubsumer(target.getLowestCommonSubsumer());
                    }
                    break;
                }
            }
        }
        if (NEW) {
            targets.add(target);
        }
    }


}
