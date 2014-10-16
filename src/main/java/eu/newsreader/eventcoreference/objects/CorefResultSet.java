package eu.newsreader.eventcoreference.objects;

import eu.kyotoproject.kaf.CorefTarget;
import eu.kyotoproject.kaf.KafCoreferenceSet;
import eu.kyotoproject.kaf.KafSense;
import vu.wntools.wordnet.WordnetData;

import java.util.ArrayList;

/**
 * Created by piek on 10/10/14.
 */
public class CorefResultSet {

    private String sourceLemma;
    private ArrayList<ArrayList<CorefTarget>> sources;
    private ArrayList<CorefMatch> targets;

    public CorefResultSet(String sourceLemma,ArrayList<CorefTarget> source) {
        this.sourceLemma = sourceLemma;
        this.sources = new ArrayList<ArrayList<CorefTarget>>();
        this.sources.add(source);
        this.targets = new ArrayList<CorefMatch>();
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

    public void setTargets(ArrayList<CorefMatch> targets) {
        this.targets = targets;
    }

    public void addTarget(CorefMatch target) {
        this.targets.add(target);
    }
    public void addSource(ArrayList<CorefTarget> corefSets) {
            sources.add(corefSets);
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
