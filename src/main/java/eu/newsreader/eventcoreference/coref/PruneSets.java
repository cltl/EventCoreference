package eu.newsreader.eventcoreference.coref;

import eu.newsreader.eventcoreference.objects.CoRefSetAgata;
import eu.newsreader.eventcoreference.objects.CorefTargetAgata;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 2/18/13
 * Time: 6:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class PruneSets {
    //// we remove all targets below the threshold except for the first target

    static public void pruneCoref (ArrayList<CoRefSetAgata> corefsets, int threshold) {
        for (int i = 0; i < corefsets.size(); i++) {
            /// start with i=1 to keep the first target
            CoRefSetAgata coRefSetAgata = corefsets.get(i);
            ArrayList<CorefTargetAgata> newTargets = new ArrayList<CorefTargetAgata>();
            newTargets.add(coRefSetAgata.getTargets().get(0));
            /// we always keep the first
            for (int j = 1; j < coRefSetAgata.getTargets().size(); j++) {
                CorefTargetAgata corefTargetAgata =  coRefSetAgata.getTargets().get(j);
                if (corefTargetAgata.getCorefScore()*100>=threshold) {
                    newTargets.add(corefTargetAgata);
                }
            }
            coRefSetAgata.setTargets(newTargets);
        }
    }

}
