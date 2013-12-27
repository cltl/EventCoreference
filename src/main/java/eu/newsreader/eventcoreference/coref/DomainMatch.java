package eu.newsreader.eventcoreference.coref;

import eu.newsreader.eventcoreference.objects.CorefTargetAgata;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 2/17/13
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class DomainMatch {

    static HashMap<String, ArrayList<String>> domainMap = new HashMap<String, ArrayList<String>>();

    static public ArrayList<String> getDomainChain (String domain) {
        ArrayList<String> chain = new ArrayList<String>();
        if (domainMap.containsKey(domain)) {
            chain = domainMap.get(domain);
        }
        chain.add(0, domain);
        return chain;
    }

    /**
     *                                     vu.wnsimilarity.measures.LeacockChodorow.match = "";

     * @return
     */

    static public double getDomainChain (String domain1, String domain2) {
            double score = 0;
            ArrayList<String> chain1 = getDomainChain(domain1);
            ArrayList<String> chain2 = getDomainChain(domain2);
            int averagedepth = (chain1.size()+chain2.size())/2;
         //   score = vu.wntools.wnsimilarity.measures.LeacockChodorow.GetDistance(averagedepth,chain1,chain2);
            return score;
        }

    static public double getDomainExactScore (CorefTargetAgata target1, CorefTargetAgata target2) {
        double score = 0;
        if (target1.getDomain().isEmpty() || target2.getDomain().isEmpty()) {
            return  0;
        }
        if (target1.getDomain().equals(target2.getDomain())) {
            return 1;
        }
        return score;
    }

    static public double getDomainChainScore (CorefTargetAgata target1, CorefTargetAgata target2) {
        double score = 0;
        if (target1.getDomain().isEmpty() || target2.getDomain().isEmpty()) {
            return  0;
        }
        String [] dom1 = target1.getDomain().split(" ");
        String [] dom2 = target2.getDomain().split(" ");
        for (int i = 0; i < dom1.length; i++) {
            String d1 = dom1[i];
            if (!d1.equalsIgnoreCase("factotum")) {
                for (int j = 0; j < dom2.length; j++) {
                    String d2 = dom2[j];
                    double chainScore = getDomainChain(d1, d2);
                    if (chainScore>score) {
                        score = chainScore;
                    }
                }
            }
        }
        return score;
    }

    static public double getDomainSubScore (CorefTargetAgata target1, CorefTargetAgata target2) {
        if (target1.getDomain().isEmpty() || target2.getDomain().isEmpty()) {
            return  0;
        }
        String [] dom1 = target1.getDomain().split(" ");
        String [] dom2 = target2.getDomain().split(" ");
        for (int i = 0; i < dom1.length; i++) {
            String d1 = dom1[i];
            if (!d1.equalsIgnoreCase("factotum")) {
                for (int j = 0; j < dom2.length; j++) {
                    String d2 = dom2[j];
                    if (d1.equals(d2)) {
                        return 1;
                    }
                }
            }
        }
        return 0;
    }

    /**
     * art
     art	dance
     art	drawing
     art	drawing	painting
     art	drawing	philately
     */


    static public void readDomainHierarchy (String fileName) {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine = "";
            while (in.ready()&&(inputLine = in.readLine()) != null) {
                if (inputLine.trim().length()>0) {
                    String [] fields = inputLine.split("\t");
                    if (fields.length>1) {
                        for (int i = 0; i < fields.length; i++) {
                            if (i>0) {
                                String field = fields[i];
                                ArrayList<String> parents = new ArrayList<String>();
                                for (int j = i-1; j >=0; j--) {
                                    String parent = fields[j];
                                    parents.add(parent);
                                }
                                domainMap.put(field,parents);
                               // System.out.println("parents.toString() = " + parents.toString());
                            }

                        }
                    }
                    else {
                      //  System.out.println("inputLine = " + inputLine);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    static public void main (String [] args) {
        String file = "./resources/domain_parent";
        readDomainHierarchy(file);
    }

    static public double getDomainScore (CorefTargetAgata target1, CorefTargetAgata target2) {
        double score = 0;
        if (target1.getDomain().equalsIgnoreCase(target2.getDomain())) {
            score = 5;
        }
        else {
            String [] dom1 = target1.getDomain().split(" ");
            String [] dom2 = target2.getDomain().split(" ");
            int nMatchesDom1 = 0;
            int nMatchesDom2 = 0;
            for (int i = 0; i < dom1.length; i++) {
                String d1 = dom1[i];
                for (int j = 0; j < dom2.length; j++) {
                    String d2 = dom2[j];
                    if (d1.equalsIgnoreCase(d2)) {
                        nMatchesDom1++;
                    }
                    else if (d1.equalsIgnoreCase("factotum")) {
                        nMatchesDom1++;
                    }
                }
            }
            for (int i = 0; i < dom2.length; i++) {
                String d2 = dom2[i];
                for (int j = 0; j < dom1.length; j++) {
                    String d1 = dom2[j];
                    if (d1.equalsIgnoreCase(d2)) {
                        nMatchesDom2++;
                    }
                    else if (d1.equalsIgnoreCase("factotum")) {
                        nMatchesDom1++;
                    }
                }
            }
            score = ((nMatchesDom1/dom1.length)+(nMatchesDom2/dom2.length))/2;
        }
        return score;
    }


}
