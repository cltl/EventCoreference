package eu.newsreader.eventcoreference.objects;

import eu.kyotoproject.kaf.CorefTarget;

import java.util.ArrayList;

/**
 * Created by piek on 10/10/14.
 */
public class CorefMatch {
    private double score;
    private String lowestCommonSubsumer;
    private String targetLemma;
    private ArrayList<ArrayList<CorefTarget>> corefTargets;

    public CorefMatch() {
        this.score = -1;
        this.lowestCommonSubsumer = "";
        this.targetLemma = "";
        this.corefTargets = new ArrayList<ArrayList<CorefTarget>>();
    }

    public String getTargetLemma() {
        return targetLemma;
    }

    public void setTargetLemma(String targetLemma) {
        this.targetLemma = targetLemma;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void addScore(double score) {
        this.score +=score;
    }

    public String getLowestCommonSubsumer() {
        return lowestCommonSubsumer;
    }

    public void setLowestCommonSubsumer(String lowestCommonSubsumer) {
        this.lowestCommonSubsumer = lowestCommonSubsumer;
    }

    public ArrayList<ArrayList<CorefTarget>> getCorefTargets() {
        return corefTargets;
    }

    public void addCorefTargets(ArrayList<CorefTarget> corefTargets) {
        this.corefTargets.add(corefTargets);
    }

    public void setCorefTargets(ArrayList<ArrayList<CorefTarget>> corefTargets) {
        this.corefTargets = corefTargets;
    }

    public boolean hasSpan (ArrayList<CorefTarget> spans) {
        for (int i = 0; i < corefTargets.size(); i++) {
            ArrayList<CorefTarget> targets = corefTargets.get(i);
            int overlap = CorefTarget.overlapSetOfSpans(targets, spans);
            if (overlap==corefTargets.size() && (overlap==spans.size())) {
                /// exact match
                return true;
            }
        }
        return false;
    }

    public boolean hasIntersection (ArrayList<CorefTarget> spans) {
        for (int i = 0; i < corefTargets.size(); i++) {
            ArrayList<CorefTarget> targets = corefTargets.get(i);
            int overlap = CorefTarget.overlapSetOfSpans(targets, spans);
            if (overlap>0) {
                /// exact match
                return true;
            }
        }
        return false;
    }
}
