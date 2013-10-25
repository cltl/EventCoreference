package eu.newsreader.eventcoreference.coref;

import eu.newsreader.eventcoreference.objects.CorefTarget;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 2/15/13
 * Time: 6:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class GranularityMatch {

    /**
     *
     </event>
     <event eid="e14" lemma="authority" pos="N.NS" target="t230" profile_id="Participant_dummy_Pnoun,Participant_Pnoun_dummy"
     score="0.0" freq="0" domain="administration" gran_nr="plural" ontology="" granType="gran_group" s
     ynset="eng-30-08337324-n" rank="0.142765">
     </event>
     <event eid="e33" lemma="Barakat" pos="N.P" target="t310" profile_id="participant_Nname2,participant_Nname_dummy,participant_Nname3"
     score="0.0" freq="0" domain="" gran_nr="singular" ontology="" granType="" synset="" rank="0.0">
     </event>
     <event eid="e38" lemma="Dahdah" pos="N.P" target="t72" profile_id="participant_Nname2,participant_Nname_dummy"
     score="0.0" freq="0" domain="" gran_nr="singular" ontology="" granType="" synset="" rank="0.0">
     </event>
     <event eid="e13" lemma="chief" pos="N.N" target="t13" profile_id="Participant_dummy_Pnoun,Participant_Pnoun_dummy"
     score="0.0" freq="0" domain="person" gran_nr="singular" ontology="" granType="gran_person" synset="eng-30-10162991-n" rank="0.355791">
     </event>
     */
    static String [] valuesNr = {"singular", "plural"};
    static String [] valuesType = { "gran_person", "gran_instance", "gran_group"};

    static int getNrValue (String value) {
        for (int i = 0; i < valuesNr.length; i++) {
            String s = valuesNr[i];
            if (s.equalsIgnoreCase(value)) {
                return i;
            }
        }
        return -1;
    }
    static int getTypeValue (String value) {
        for (int i = 0; i < valuesType.length; i++) {
            String s = valuesType[i];
            if (s.equalsIgnoreCase(value)) {
                return i;
            }
        }
        return -1;
    }

    static public int getGranularityNrScore (CorefTarget target1, CorefTarget target2) {
        int score = 0;
        if (!target1.getGranularityNumber().isEmpty() && !target2.getGranularityNumber().isEmpty()) {
            if (target1.getGranularityNumber().equalsIgnoreCase(target2.getGranularityNumber())) {
                score = valuesNr.length;
            }
            else {
                int idx1 = getNrValue(target1.getGranularityNumber());
                int idx2 = getNrValue(target2.getGranularityNumber());
                score = Math.abs(idx1-idx2);
            }
        }
        return score;
    }
    static public int getGranularityTypeScore (CorefTarget target1, CorefTarget target2) {
        int score = 0;
        if (target1.getGranularityType().isEmpty()) {
            if ((target1.getPos().equals("N.P"))  || (target1.getPos().equals("N.NPS"))) { /// treetagger style
                target1.setGranularityType("gran_instance");
            }
            else if ((target1.getPos().equals("NNP"))  || (target1.getPos().equals("NNPS"))) { /// tsanford style
                target1.setGranularityType("gran_instance");
            }
        }
        if (target2.getGranularityType().isEmpty()) {
            if ((target2.getPos().equals("N.P"))  || (target2.getPos().equals("N.NPS"))) { /// tretagger style
                target2.setGranularityType("gran_instance");
            }
            else if ((target2.getPos().equals("NNP"))  || (target2.getPos().equals("NNPS"))) { /// stanford style
                target2.setGranularityType("gran_instance");
            }
        }

        if (!target1.getGranularityType().isEmpty() && !target2.getGranularityType().isEmpty()) {
            if (target1.getGranularityType().equalsIgnoreCase(target2.getGranularityType())) {
                score = valuesType.length;
            }
            else {
                int idx1 = getTypeValue(target1.getGranularityType());
                int idx2 = getTypeValue(target2.getGranularityType());
                score = Math.abs(idx1-idx2);
            }
        }
        return score;
    }

    static public int getGranularityScore (CorefTarget target1, CorefTarget target2) {
        int score = 0;
        score += getGranularityNrScore(target1, target2);
        score += getGranularityTypeScore(target1, target2);
        return score;
    }
}
