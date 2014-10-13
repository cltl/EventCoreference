package eu.newsreader.eventcoreference.objects;

import eu.kyotoproject.kaf.CorefTarget;

import java.util.ArrayList;

/**
 * Created by piek on 10/10/14.
 */
public class CorefMatch {
    private double score;
    private String lowestCommonSubsumer;
    private ArrayList<CorefTarget> corefTargets;

    public CorefMatch() {
        this.score = -1;
        this.lowestCommonSubsumer = "";
        this.corefTargets = new ArrayList<CorefTarget>();
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

    public ArrayList<CorefTarget> getCorefTargets() {
        return corefTargets;
    }

    public void setCorefTargets(ArrayList<CorefTarget> corefTargets) {
        this.corefTargets = corefTargets;
    }


    public boolean hasSpan (ArrayList<CorefTarget> spans) {
        int overlap = CorefTarget.overlapSetOfSpans(corefTargets, spans);
        if (overlap==corefTargets.size() && (overlap==spans.size())) {
            /// exact match
            return true;
        }
        return false;
    }

    public boolean hasIntersection (ArrayList<CorefTarget> spans) {
        int overlap = CorefTarget.overlapSetOfSpans(corefTargets, spans);
        if (overlap>0) {
            /// exact match
            return true;
        }
        return false;
    }
}
