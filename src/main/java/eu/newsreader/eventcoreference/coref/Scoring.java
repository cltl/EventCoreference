package eu.newsreader.eventcoreference.coref;

import eu.newsreader.eventcoreference.objects.CorefMatch;

import java.util.ArrayList;

/**
 * Created by piek on 10/13/14.
 */
public class Scoring {
    static public ArrayList<CorefMatch> pruneCoref (ArrayList<CorefMatch> corefsets, int threshold) {
        ArrayList<CorefMatch> newCorefMatches = new ArrayList<CorefMatch>();
        for (int i = 0; i < corefsets.size(); i++) {
            CorefMatch corefMatch = corefsets.get(i);
            if (corefMatch.getScore()*100>=threshold) {
                newCorefMatches.add(corefMatch);
            }
            else {
              //  System.out.println("removing corefMatch = " + corefMatch.getScore());
            }
        }
        return newCorefMatches;
    }

    static public void normalizeCorefMatch (ArrayList<CorefMatch> coRefSets) {
        double maxScore = 0;
        double minScore = 0;
        for (int c = 0; c < coRefSets.size(); c++) {
            CorefMatch corefMatch = coRefSets.get(c);
            double corefScore = corefMatch.getScore();
            if (corefScore>maxScore) {
                maxScore = corefScore;
            }
            else if (corefScore<minScore) {
                minScore = corefScore;
            }
        }
        for (int c = 0; c < coRefSets.size(); c++) {
            CorefMatch corefMatch = coRefSets.get(c);
            double corefScore = corefMatch.getScore();
            corefScore = corefScore/maxScore;
            corefMatch.setScore(corefScore);
        }
    }

}
