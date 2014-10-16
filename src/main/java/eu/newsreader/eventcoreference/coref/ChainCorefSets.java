package eu.newsreader.eventcoreference.coref;

import eu.newsreader.eventcoreference.objects.CorefMatch;
import eu.newsreader.eventcoreference.objects.CorefResultSet;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 2/17/13
 * Time: 6:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChainCorefSets {

    static public ArrayList<CorefResultSet> chainResultSets (ArrayList<CorefResultSet> corefMatchList)  {
        ArrayList<CorefResultSet> condensedSets = new ArrayList<CorefResultSet>();
        for (int i = 0; i < corefMatchList.size(); i++) {
            CorefResultSet candidateCorefResultSet = corefMatchList.get(i);
            boolean CHAINIT = false;
            for (int j = 0; j < condensedSets.size(); j++) {
                CorefResultSet coreferenceSet = condensedSets.get(j);
                /// do we need to do something with the sources???????
/*                if (coreferenceSet.hasSpanAndLowestCommonSubsumer(corefMatch)) {
                    //if (coreferenceSet.hasSpan(corefMatch.getCorefTargets())) {
                    //// there is a match so start adding the corefResultSet to the KafCoreferenceSet
                    CHAINIT = true;
                    break;
                }
                else {
                    //if span matches but not the lcs we need to choose one of the other
                }*/
                for (int k = 0; k < candidateCorefResultSet.getTargets().size(); k++) {
                    CorefMatch corefMatch = candidateCorefResultSet.getTargets().get(k);
                    if (coreferenceSet.hasSpanAndLowestCommonSubsumer(corefMatch)) {
                        //if (coreferenceSet.hasSpan(corefMatch.getCorefTargets())) {
                        //// there is a match so start adding the corefResultSet to the KafCoreferenceSet
                        CHAINIT = true;
                        break;
                    }
                    else {
                        //if span matches but not the lcs we need to choose one of the other
                    }
                }
                if (CHAINIT) {
                    for (int k = 0; k < candidateCorefResultSet.getTargets().size(); k++) {
                        CorefMatch corefMatch = candidateCorefResultSet.getTargets().get(k);
                        coreferenceSet.addTargetScore(corefMatch);
                    }
                   // coreferenceSet.addSourceCoref(candidateCorefResultSet.getSources());
                    /// we are done with this set and can break the loop
                    break;
                }
            }
            if (!CHAINIT) {
                condensedSets.add(candidateCorefResultSet);
            }
        }
        return condensedSets;
    }



}
