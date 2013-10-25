package eu.newsreader.eventcoreference.coref;

import eu.newsreader.eventcoreference.objects.CoRefSet;
import eu.newsreader.eventcoreference.objects.CorefTarget;

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

    static public void pruneCoref (ArrayList<CoRefSet> corefsets, int threshold) {
        for (int i = 0; i < corefsets.size(); i++) {
            /// start with i=1 to keep the first target
            CoRefSet coRefSet = corefsets.get(i);
            ArrayList<CorefTarget> newTargets = new ArrayList<CorefTarget>();
            newTargets.add(coRefSet.getTargets().get(0));
            /// we always keep the first
            for (int j = 1; j < coRefSet.getTargets().size(); j++) {
                CorefTarget corefTarget =  coRefSet.getTargets().get(j);
                if (corefTarget.getCorefScore()*100>=threshold) {
                    newTargets.add(corefTarget);
                }
            }
            coRefSet.setTargets(newTargets);
        }
    }

}
