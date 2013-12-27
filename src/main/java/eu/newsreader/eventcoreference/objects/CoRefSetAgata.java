package eu.newsreader.eventcoreference.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 3/9/12
 * Time: 3:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class CoRefSetAgata {
    
    private String id;
    private double score;
    private String lowestCommonSubsumer;
    private ArrayList<CorefTargetAgata> targets;

    public CoRefSetAgata() {
        this.id = "";
        this.targets = new ArrayList<CorefTargetAgata>();
        this.lowestCommonSubsumer = "";
        this.score = 0;
    }

    public String getId() {
        return id;
    }

    public String getParticipantId() {
        if (id.startsWith("e")) {
            String pid = "p"+this.id.substring(1);
            return pid;
        }
        else {
            return id;
        }
    }

    public String getLocationId() {
        if (id.startsWith("e")) {
            String pid = "l"+this.id.substring(1);
            return pid;
        }
        else {
            return id;
        }
    }

    public String getTimeId() {
        if (id.startsWith("e")) {
            String pid = "t"+this.id.substring(1);
            return pid;
        }
        else {
            return id;
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getLcs() {
        return lowestCommonSubsumer;
    }

    public void setLcs(String lcs) {
        this.lowestCommonSubsumer = lcs;
    }

    public ArrayList<CorefTargetAgata> getTargets() {
        return targets;
    }

    public void setTargets(ArrayList<CorefTargetAgata> targets) {
        this.targets = targets;
    }

    public boolean containsTarget (CorefTargetAgata target) {
        for (int i = 0; i < targets.size(); i++) {
            CorefTargetAgata corefTargetAgata = targets.get(i);
            if (corefTargetAgata.getTermId().equals(target.getTermId())) {
              //  System.out.println("corefTarget.getTermId() = " + corefTarget.getTermId());
              //  System.out.println("target.getTermId() = " + target.getTermId());
                return true;
            }
        }
        return false;
    }

    public boolean hasOverlap (CoRefSetAgata otherSet) {
        for (int i = 0; i < targets.size(); i++) {
            CorefTargetAgata corefTargetAgata = targets.get(i);
            if (otherSet.containsTarget(corefTargetAgata)) {
              //  System.out.println("corefTarget.getTermId() = " + corefTarget.getTermId());
                return true;
            }
        }
        return false;
    }

    public int sizeOverlap (CoRefSetAgata otherSet) {
        int overlap = 0;
        for (int i = 0; i < targets.size(); i++) {
            CorefTargetAgata corefTargetAgata = targets.get(i);
            if (otherSet.containsTarget(corefTargetAgata)) {
              //  System.out.println("corefTarget.getTermId() = " + corefTarget.getTermId());
                overlap++;
            }
        }
        return overlap;
    }

    public boolean containsTargetTermId(String id) {
        for (int i = 0; i < targets.size(); i++) {
            CorefTargetAgata corefTargetAgata = targets.get(i);
            if (corefTargetAgata.getTermId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public CorefTargetAgata getTargetForTermId(String id) {
        for (int i = 0; i < targets.size(); i++) {
            CorefTargetAgata corefTargetAgata = targets.get(i);
            if (corefTargetAgata.getTermId().equals(id)) {
                return corefTargetAgata;
            }
        }
        return null;
    }

    public boolean containsTargetSentenceId(String id) {
        for (int i = 0; i < targets.size(); i++) {
            CorefTargetAgata corefTargetAgata = targets.get(i);
            if (corefTargetAgata.getSentenceId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public void addTarget(CorefTargetAgata target) {
        this.targets.add(target);
    }
    
    public String toString () {
        String str = "\t<co-refs id=\""+id+"\"";
        if (!lowestCommonSubsumer.isEmpty()) str +=" lcs=\""+lowestCommonSubsumer+"\"";
        if (score>0) str +=" score=\""+score+"\"";
        str += ">\n";
        for (int k = 0; k < targets.size(); k++) {
            CorefTargetAgata target= targets.get(k);
            str += target.toString();
        }
        str += "\t</co-refs>\n";
        return str;
    }

    public String getMostFrequentLemma () {
        String bestLemma = "";
        HashMap<String, Integer> cnts = new HashMap<String, Integer>();
        for (int k = 0; k < targets.size(); k++) {
            CorefTargetAgata target= targets.get(k);
            if (cnts.containsKey(target.getWord())) {
                Integer cnt = cnts.get(target.getWord());
                cnt++;
                cnts.put(target.getWord(), cnt);
            }
            else {
                cnts.put(target.getWord(), 1);
            }
        }
        Integer top = 0;
        Set keySet = cnts.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            Integer cnt = cnts.get(key);
            if (cnt>top) {
                bestLemma = key;
                top = cnt;
            }
        }        
        return bestLemma;
    }
    
    public String getMostFrequentSynset () {
        String bestLemma = "";
        HashMap<String, Integer> cnts = new HashMap<String, Integer>();
        for (int k = 0; k < targets.size(); k++) {
            CorefTargetAgata target= targets.get(k);
            if (cnts.containsKey(target.getSynset())) {
                Integer cnt = cnts.get(target.getSynset());
                cnt++;
                cnts.put(target.getSynset(), cnt);
            }
            else {
                cnts.put(target.getSynset(), 1);
            }
        }
        Integer top = 0;
        Set keySet = cnts.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            Integer cnt = cnts.get(key);
            if (cnt>top) {
                bestLemma = key;
                top = cnt;
            }
        }        
        return bestLemma;
    }

    public String printTargetSet () {
        String str = "{";
        for (int i = 0; i < targets.size(); i++) {
            if (i>0) str+= ",";
            CorefTargetAgata corefTargetAgata = targets.get(i);
            str+= corefTargetAgata.getTermId();
        }
        str += "}";
        return str;
    }


    public double getAverageCoref () {
        double score = 0;
        for (int i = 0; i < targets.size(); i++) {
            CorefTargetAgata corefTargetAgata = targets.get(i);
            score+= corefTargetAgata.getCorefScore();
        }
        score = score/targets.size();
        return score;
    }

    public double getAverageSimScore () {
        double score = 0;
        for (int i = 0; i < targets.size(); i++) {
            CorefTargetAgata corefTargetAgata = targets.get(i);
            score+= corefTargetAgata.getSimScore();
        }
        score = score/targets.size();
        return score;
    }

    public double getAverageGranScore () {
        double score = 0;
        for (int i = 0; i < targets.size(); i++) {
            CorefTargetAgata corefTargetAgata = targets.get(i);
            score+= corefTargetAgata.getGranScore();
        }
        score = score/targets.size();
        return score;
    }

    public double getAverageDomScore () {
        double score = 0;
        for (int i = 0; i < targets.size(); i++) {
            CorefTargetAgata corefTargetAgata = targets.get(i);
            score+= corefTargetAgata.getDomScore();
        }
        score = score/targets.size();
        return score;
    }
}
