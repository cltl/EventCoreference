package eu.newsreader.eventcoreference.coref;

import eu.newsreader.eventcoreference.objects.CoRefSet;
import eu.newsreader.eventcoreference.objects.CorefTarget;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 2/18/13
 * Time: 11:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class NormalizeScores {


    static public void normalize (ArrayList<CoRefSet> coRefSets) {
        double maxSim = 0;
        double maxGran = 0;
        double maxDom = 0;
        double maxSetScore = 0;
        double maxScore = 0;
        double minSim = 0;
        double minGran = 0;
        double minDom = 0;
        double minSetScore = 0;
        double minScore = 0;
        for (int c = 0; c < coRefSets.size(); c++) {
            CoRefSet coRefSet = coRefSets.get(c);
            double corefScore = coRefSet.getScore();
            if (corefScore>maxSetScore) {
                maxSetScore = corefScore;
            }
            else if (corefScore<minSetScore) {
                minSetScore = corefScore;
            }
            ArrayList<CorefTarget> targets = coRefSet.getTargets();
            for (int i = 0; i < targets.size(); i++) {
                CorefTarget corefTarget = targets.get(i);
                double simScore= corefTarget.getSimScore();
                if (simScore>maxSim) {
                    maxSim = simScore;
                }
                else if (simScore<minSim) {
                    minSim = simScore;
                }
                double granScore= corefTarget.getGranScore();
                if (granScore>maxGran) {
                    maxGran = granScore;
                }
                else if (granScore<minGran) {
                    minScore = granScore;
                }
                double domScore= corefTarget.getDomScore();
                if (domScore>maxDom) {
                    maxDom = domScore;
                }
                else if (domScore<minDom) {
                    minDom = domScore;
                }
                double targetCorefScore= corefTarget.getCorefScore();
                if (targetCorefScore>maxScore) {
                    maxScore = targetCorefScore;
                }
                else if (targetCorefScore<minScore) {
                    minScore = targetCorefScore;
                }
            }
        }
        for (int c = 0; c < coRefSets.size(); c++) {
            CoRefSet coRefSet = coRefSets.get(c);
            double corefScore = coRefSet.getScore();
            corefScore = corefScore/maxScore;
            coRefSet.setScore(corefScore);
            ArrayList<CorefTarget> targets = coRefSet.getTargets();
            for (int i = 0; i < targets.size(); i++) {
                CorefTarget corefTarget = targets.get(i);
                double simScore= corefTarget.getSimScore();
                simScore = simScore/maxSim;
                corefTarget.setSimScore(simScore);
                double granScore= corefTarget.getGranScore();
                granScore = granScore/maxGran;
                corefTarget.setGranScore(granScore);
                double domScore= corefTarget.getDomScore();
                domScore = domScore/maxDom;
                corefTarget.setDomScore(domScore);
                double targetCorefScore= corefTarget.getCorefScore();
                targetCorefScore= targetCorefScore/maxScore;
                corefTarget.setCorefScore(targetCorefScore);
            }
        }
    }


}
