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

    private ArrayList<CorefTarget> source;
    private ArrayList<CorefMatch> targets;

    public CorefResultSet(ArrayList<CorefTarget> source) {
        this.source = source;
        this.targets = new ArrayList<CorefMatch>();
    }

    public ArrayList<CorefTarget> getSource() {
        return source;
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
                if (!lcsList.contains(corefMatch.getLowestCommonSubsumer())) {
                    lcsList.add(corefMatch.getLowestCommonSubsumer());
                }
            }
        }
        return lcsList;
    }


    public KafCoreferenceSet castToKafCoreferenceSet (WordnetData wordnetData) {
        KafCoreferenceSet kafCoreferenceSet = new KafCoreferenceSet();
        ArrayList<String> lcsList = getLowestSubsumers();
        KafSense kafSense = null;
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
        if (kafSense!=null) {
            kafCoreferenceSet.addExternalReferences(kafSense);
        }
        kafCoreferenceSet.addSetsOfSpans(source);
        for (int i = 0; i < targets.size(); i++) {
            CorefMatch corefMatch = targets.get(i);
            kafCoreferenceSet.addSetsOfSpans(corefMatch.getCorefTargets());
        }
        return kafCoreferenceSet;
    }

    public boolean hasSpan (ArrayList<CorefTarget> spans) {
        if (CorefTarget.hasSpan(spans, source)) {
            return true;
        }
        for (int i = 0; i < targets.size(); i++) {
            CorefMatch corefMatch = targets.get(i);
            if (CorefTarget.hasSpan(spans, corefMatch.getCorefTargets())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSpanAndLowestCommonSubsumer (CorefMatch match) {
            for (int i = 0; i < targets.size(); i++) {
                CorefMatch corefMatch = targets.get(i);
                if ((CorefTarget.hasSpan(match.getCorefTargets(), corefMatch.getCorefTargets()))
                     &&
                    (corefMatch.getLowestCommonSubsumer().equals(match.getLowestCommonSubsumer()))){
                    return true;
                }
            }
            return false;
    }

    public boolean hasSpanAndLowestCommonSubsumer (ArrayList<CorefMatch> matches) {
            for (int i = 0; i < targets.size(); i++) {
                CorefMatch corefMatch = targets.get(i);
                for (int j = 0; j < matches.size(); j++) {
                    CorefMatch match = matches.get(j);
                    if ((CorefTarget.hasSpan(match.getCorefTargets(), corefMatch.getCorefTargets()))
                         &&
                        (corefMatch.getLowestCommonSubsumer().equals(match.getLowestCommonSubsumer()))){
                        return true;
                    }
                }
            }
            return false;
    }

    public void addTargetScore (CorefMatch target) {
        boolean NEW = true;
        for (int i = 0; i < targets.size(); i++) {
            CorefMatch corefMatch = targets.get(i);
            if (corefMatch.hasSpan(target.getCorefTargets())) {
                NEW = false;
                if (target.getScore()>corefMatch.getScore()) {
                    corefMatch.setScore(target.getScore());
                    corefMatch.setLowestCommonSubsumer(target.getLowestCommonSubsumer());
                }
                break;
            }
            else {
             //////
            }
        }
        if (NEW) {
            targets.add(target);
        }
    }

    public void addSourceCoref (ArrayList<CorefTarget> source) {
        boolean NEW = true;
        for (int i = 0; i < targets.size(); i++) {
            CorefMatch corefMatch = targets.get(i);
            if (CorefTarget.hasSpan(corefMatch.getCorefTargets(), source)) {
                NEW = false;
                break;
            }
            else {
             //////
            }
        }
        if (NEW) {
            CorefMatch corefMatch = new CorefMatch();
            corefMatch.setCorefTargets(source);
            targets.add(corefMatch);
        }
    }
}
