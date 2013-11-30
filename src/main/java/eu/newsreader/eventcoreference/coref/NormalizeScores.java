package eu.newsreader.eventcoreference.coref;

import eu.newsreader.eventcoreference.objects.CoRefSetAgata;
import eu.newsreader.eventcoreference.objects.CorefTargetAgata;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 2/18/13
 * Time: 11:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class NormalizeScores {


    static public void normalize (ArrayList<CoRefSetAgata> coRefSetAgatas) {
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
        for (int c = 0; c < coRefSetAgatas.size(); c++) {
            CoRefSetAgata coRefSetAgata = coRefSetAgatas.get(c);
            double corefScore = coRefSetAgata.getScore();
            if (corefScore>maxSetScore) {
                maxSetScore = corefScore;
            }
            else if (corefScore<minSetScore) {
                minSetScore = corefScore;
            }
            ArrayList<CorefTargetAgata> targets = coRefSetAgata.getTargets();
            for (int i = 0; i < targets.size(); i++) {
                CorefTargetAgata corefTargetAgata = targets.get(i);
                double simScore= corefTargetAgata.getSimScore();
                if (simScore>maxSim) {
                    maxSim = simScore;
                }
                else if (simScore<minSim) {
                    minSim = simScore;
                }
                double granScore= corefTargetAgata.getGranScore();
                if (granScore>maxGran) {
                    maxGran = granScore;
                }
                else if (granScore<minGran) {
                    minScore = granScore;
                }
                double domScore= corefTargetAgata.getDomScore();
                if (domScore>maxDom) {
                    maxDom = domScore;
                }
                else if (domScore<minDom) {
                    minDom = domScore;
                }
                double targetCorefScore= corefTargetAgata.getCorefScore();
                if (targetCorefScore>maxScore) {
                    maxScore = targetCorefScore;
                }
                else if (targetCorefScore<minScore) {
                    minScore = targetCorefScore;
                }
            }
        }
        for (int c = 0; c < coRefSetAgatas.size(); c++) {
            CoRefSetAgata coRefSetAgata = coRefSetAgatas.get(c);
            double corefScore = coRefSetAgata.getScore();
            corefScore = corefScore/maxScore;
            coRefSetAgata.setScore(corefScore);
            ArrayList<CorefTargetAgata> targets = coRefSetAgata.getTargets();
            for (int i = 0; i < targets.size(); i++) {
                CorefTargetAgata corefTargetAgata = targets.get(i);
                double simScore= corefTargetAgata.getSimScore();
                simScore = simScore/maxSim;
                corefTargetAgata.setSimScore(simScore);
                double granScore= corefTargetAgata.getGranScore();
                granScore = granScore/maxGran;
                corefTargetAgata.setGranScore(granScore);
                double domScore= corefTargetAgata.getDomScore();
                domScore = domScore/maxDom;
                corefTargetAgata.setDomScore(domScore);
                double targetCorefScore= corefTargetAgata.getCorefScore();
                targetCorefScore= targetCorefScore/maxScore;
                corefTargetAgata.setCorefScore(targetCorefScore);
            }
        }
    }


}
