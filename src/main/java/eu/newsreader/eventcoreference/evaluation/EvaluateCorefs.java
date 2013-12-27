package eu.newsreader.eventcoreference.evaluation;

import eu.newsreader.eventcoreference.objects.CoRefSetAgata;
import eu.newsreader.eventcoreference.objects.CorefTargetAgata;
import eu.newsreader.eventcoreference.input.CorefSaxParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kyoto
 * Date: 2/29/12
 * Time: 2:22 PM
 * To change this template use File | Settings | File Templates.
 *
 * Implements the B3 evaluation measure if cardinality>0 then B3 strictly, if c>1 singletons are excluded
 */
public class EvaluateCorefs {
    private boolean DEBUG = false;
    private ArrayList<String> totalIds = new ArrayList<String>();
    private ArrayList<String> totalSysIds = new ArrayList<String>();
    private ArrayList<String> totalGoldIds = new ArrayList<String>();

    public EvaluateCorefs () {
        totalIds = new ArrayList<String>();
        totalSysIds = new ArrayList<String>();
        totalGoldIds = new ArrayList<String>();
    }

    public void init(ArrayList<CoRefSetAgata> corefGoldSets, ArrayList<CoRefSetAgata> corefSysSets) {
        totalIds = new ArrayList<String>();
        totalSysIds = new ArrayList<String>();
        totalGoldIds = new ArrayList<String>();
        /// add all the system event ids to the totalIds set and the totalSysIds set
        for (int i = 0; i < corefSysSets.size(); i++) {
            CoRefSetAgata strings = corefSysSets.get(i);
            for (int j = 0; j < strings.getTargets().size(); j++) {
                CorefTargetAgata s = strings.getTargets().get(j);
                if (!totalSysIds.contains(s.getTermId()))  {
                    totalSysIds.add(s.getTermId());
                }
                if (!totalIds.contains(s.getTermId())) {
                    totalIds.add(s.getTermId());
                }
            }
        }

        //// add all the gold ids to the totalIds set and the totalGoldIds set
        for (int i = 0; i < corefGoldSets.size(); i++) {
            CoRefSetAgata strings = corefGoldSets.get(i);
            for (int j = 0; j < strings.getTargets().size(); j++) {
                CorefTargetAgata s = strings.getTargets().get(j);
                if (!totalGoldIds.contains(s.getTermId()))  {
                    totalGoldIds.add(s.getTermId());
                }
                if (!totalIds.contains(s.getTermId())) {
                    totalIds.add(s.getTermId());
                }
            }
        }
    }

    static public void main (String [] args) {
        EvaluateCorefs evaluateCorefs = new EvaluateCorefs();
        String goldFile = "";
        String sysFile = "";
        int cardinality = 0;
        int nameSubString = -1;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("--goldstandard")) {
                if (args.length>i+1) {
                    goldFile = args[i+1];
                }
            }
            else if (arg.equalsIgnoreCase("--system")) {
                if (args.length>i+1) {
                    sysFile = args[i+1];
                }
            }
            else if (arg.equalsIgnoreCase("--namesubstring")) {
                if (args.length>i+1) {
                    nameSubString = Integer.parseInt(args[i+1]);
                }
            }
            else if (arg.equalsIgnoreCase("--debug")) {
                evaluateCorefs.DEBUG = true;
            }
            else if (arg.equalsIgnoreCase("--cardinality")) {
                if (args.length>i+1) {
                    cardinality = Integer.parseInt(args[i+1]);
                }
            }
        }
        boolean stop = false;
        if (goldFile.isEmpty()) {
            System.out.println("specify --goldstandard");
            stop = true;
        }
        if (sysFile.isEmpty()) {
            System.out.println("specify --system");
            stop = true;
        }/*
        if (nameSubString==-1) {
            System.out.println("specify --namesubstring");
            stop = true;
        }*/
         if (stop) {
             return;
         }

        System.out.println("goldFile = " + goldFile);
        CorefSaxParser goldParser = new CorefSaxParser(cardinality, nameSubString);
        goldParser.parseFile(goldFile);
        System.out.println("sysFile = " + sysFile);
        CorefSaxParser sysParser = new CorefSaxParser(cardinality, nameSubString);
        sysParser.parseFile(sysFile);
        System.out.println("cardinality = " + cardinality);
        try {
            int keyCoRefTotal = 0;
            int responseCoRefTotal = 0;
            int keyCoRefSingletons = 0;
            int responseCoRefSingletons = 0;
            double microAveragePrecision = 0;
            double microAverageRecall = 0;
            int nEvaluations = 0;
            int idx = sysFile.lastIndexOf(".xml");
            String outputFile = sysFile+".eval."+cardinality+".xls";
            if (idx>-1) {
                outputFile = sysFile.substring(0, idx)+".eval."+cardinality+".xls";
            }
            FileOutputStream fos = new FileOutputStream(outputFile);
            String str = "Gold standard file\t"+new File(goldFile).getName()+"\t"+goldParser.corefMap.size()+" files\n";
            str += "System file\t"+new File(sysFile).getName()+"\t"+sysParser.corefMap.size()+" files\n\n";
            fos.write(str.getBytes());
            String header = "File";
            String row1 = "Nr of system coref sets";
            String row2 = "Nr of gold coref sets";
            String row3 = "Precision";
            String row4 = "Recall";
            String row5 = "F-measure";
            Set keySet = sysParser.corefMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (evaluateCorefs.DEBUG) System.out.println("key = " + key);
                header += "\t"+key;
                ArrayList<CoRefSetAgata> corefSysSets = sysParser.corefMap.get(key);
                responseCoRefTotal += corefSysSets.size();
                responseCoRefSingletons += nrOfSingletons(corefSysSets);
                row1 += "\t"+ corefSysSets.size();
                if (goldParser.corefMap.containsKey(key)) {
                    nEvaluations++;
                    ArrayList<CoRefSetAgata> corefGoldSets = goldParser.corefMap.get(key);
                    keyCoRefTotal += corefGoldSets.size();
                    keyCoRefSingletons += nrOfSingletons(corefGoldSets);
                    evaluateCorefs.init(corefGoldSets, corefSysSets);
                    row2 += "\t"+ corefGoldSets.size();
                    /// next step will modify the corefSysSets
                    ArrayList<CoRefSetAgata> adaptedSysSets = evaluateCorefs.CaiStrubePrepareSets(corefSysSets);
                    double precision = evaluateCorefs.CorefB3Precision(adaptedSysSets, corefGoldSets);
                    double recall = evaluateCorefs.CorefB3Recall(adaptedSysSets, corefGoldSets);
                    double  f = 2*(precision*recall)/(precision+recall);
                    row3 += "\t"+precision;
                    row4 += "\t"+recall;
                    row5 += "\t"+f;
                    microAveragePrecision+=precision;
                    microAverageRecall+=recall;
                }
                else {
                    row2 += "\t"+0;
                    row3 += "\t";
                    row4 += "\t";
                    row5 += "\t";
                }
            }
            microAveragePrecision = microAveragePrecision/nEvaluations;
            microAverageRecall = microAverageRecall/nEvaluations;
            str = "Micro average precision\t"+microAveragePrecision+"\n";
            str += "Micro average recall\t"+microAverageRecall+"\n";
            double microF = 2*(microAveragePrecision*microAverageRecall)/(microAveragePrecision+microAverageRecall);
            str += "Micro average F-measure\t"+microF+"\n\n";
            str += "\t\tKey-co-ref-sets\tKey-singletons\tKey-multiforms\tResponse-co-ref-sets\tResponse-singletons\tResponse-multiforms\tMicro average recall\tMicro average precision\tMicro average F-measure\n";
            str +=  "\tGREP"+"\t"+keyCoRefTotal+"\t"+keyCoRefSingletons+"\t"+(keyCoRefTotal-keyCoRefSingletons)+"\t"
                    +responseCoRefTotal+"\t"+responseCoRefSingletons+"\t"+(responseCoRefTotal-responseCoRefSingletons)+"\t"
                    +microAverageRecall+"\t"+microAveragePrecision+"\t"+microF+"\n\n";

            header+="\n";
            row1 +="\n";
            row2 +="\n";
            row3 +="\n";
            row4 +="\n";
            row5 +="\n";
            str += header+row1+row2+row3+row4+row5;
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    static public int nrOfSingletons (ArrayList<CoRefSetAgata> set) {
        int s = 0;
        for (int i = 0; i < set.size(); i++) {
            CoRefSetAgata coRefSetAgata = set.get(i);
            if (coRefSetAgata.getTargets().size()==1) {
                s++;
            }
        }
        return s;
    }

    //CAI and STRUBE
    /*
   Preparations
   1.  Discard all the singletons twinless system mention in response;
   2.  Put all the twinless annotated mentions (key mentions without matching set) into response;
   3.  if calculating precision then
   4.    Merge all the remaining twinless system mentions with key to form key-p;
   5.    Use response to form response-p
   6.    Through key-p and response-p;
   7.    Calculate B3 precision P .
   8.  end if
   9.  if calculating recall then
   10.   Discard all the remaining twinless system mentions in response to from response-r;
   11.   Use key to form key-r
   12.   Through key-r and response-r;
   13.   Calculate B3 recall R
   14. end if
   15. Calculate F-score F
    */

    public ArrayList<CoRefSetAgata> CaiStrubePrepareSets (ArrayList<CoRefSetAgata> corefSysSets) {
        if (DEBUG) {
            System.out.println("PREPARING SYSTEM SET ACCORDING TO CAI AND STRUBE");
        }
        ArrayList<CoRefSetAgata> adaptedCorefSysSets  = new ArrayList<CoRefSetAgata>();

        /// 1. Discard all the singletons twinless system mention in response;
        //     we discard all twinless singletons in system set (response)
        for (int i = 0; i < corefSysSets.size(); i++) {
            CoRefSetAgata coRefSetAgata = corefSysSets.get(i);
            if (coRefSetAgata.getTargets().size()==1) {
                /// this is a singleton
                if (totalGoldIds.contains(coRefSetAgata.getTargets().get(0).getTermId())) {
                    adaptedCorefSysSets.add(coRefSetAgata);
                }
                else {
                    /// this singleton is not in the gold standard set so we ignore it
                }
            }
            else {
                /// multisets are all added
                    adaptedCorefSysSets.add(coRefSetAgata);
            }
        }
        if (DEBUG) {
            System.out.println("Original system sets:"+corefSysSets.size());
            for (int i = 0; i < corefSysSets.size(); i++) {
                CoRefSetAgata set = corefSysSets.get(i);
                System.out.println(set.printTargetSet());
            }
            System.out.println("Adapted system sets, discarding singleton twins:"+adaptedCorefSysSets.size());
            for (int i = 0; i < adaptedCorefSysSets.size(); i++) {
                CoRefSetAgata set = adaptedCorefSysSets.get(i);
                System.out.println(set.printTargetSet());
            }
        }

     //   2. Put all the twinless annotated mentions (key mentions without matching mention) into response;
        for (int i = 0; i < totalGoldIds.size(); i++) {
            String mention = totalGoldIds.get(i);
            if (!totalSysIds.contains(mention)) {
                CoRefSetAgata coRefSetAgata = new CoRefSetAgata();
                CorefTargetAgata t = new CorefTargetAgata(mention);
                coRefSetAgata.addTarget(t);
                adaptedCorefSysSets.add(coRefSetAgata);
            }
        }

        if (DEBUG) {
            System.out.println("Adapted system sets, adding twinless gold mentions:"+adaptedCorefSysSets.size());
            for (int i = 0; i < adaptedCorefSysSets.size(); i++) {
                CoRefSetAgata set = adaptedCorefSysSets.get(i);
                System.out.println(set.printTargetSet());
            }
        }
        return adaptedCorefSysSets;
    }


    public double CorefB3Recall (ArrayList<CoRefSetAgata> corefSysSets, ArrayList<CoRefSetAgata> corefGoldSets) {
        /*
           9.  if calculating recall then
           10.   Discard all the remaining twinless system mentions in response to from response-r;
           11.   Use key to form key-r
           12.   Through key-r and response-r;
           13.   Calculate B3 recall R
           14. end if
         */
        if (DEBUG) System.out.println("\nRECALL");

        /// 10.   Discard all the remaining twinless system mentions in response to from response-r;
        ///

        ArrayList<CoRefSetAgata> adaptedCorefSysSets  = new ArrayList<CoRefSetAgata>();

        for (int i = 0; i < corefSysSets.size(); i++) {
            CoRefSetAgata coRefSetAgata = corefSysSets.get(i);
            CoRefSetAgata adaptedCoRefSysSet = new CoRefSetAgata();
            for (int j = 0; j < coRefSetAgata.getTargets().size(); j++) {
                CorefTargetAgata corefTargetAgata = coRefSetAgata.getTargets().get(j);
                if (totalGoldIds.contains(corefTargetAgata.getTermId())) {
                    //// target is covered by some gold standard set (key set)
                    adaptedCoRefSysSet.addTarget(corefTargetAgata);
                }
            }
            if (adaptedCoRefSysSet.getTargets().size()>0) {
                adaptedCorefSysSets.add(adaptedCoRefSysSet);
            }
            else {
                /// THE WHOLE SET WAS EMPTIED
            }
        }

        if (DEBUG) {
            System.out.println("System sets for recall after preparation:"+corefSysSets.size());
            for (int i = 0; i < corefSysSets.size(); i++) {
                CoRefSetAgata set = corefSysSets.get(i);
                System.out.println(set.printTargetSet());
            }
            System.out.println("Adapted system sets for recall, discarding remaining twinless system mentions:"+adaptedCorefSysSets.size());
            for (int i = 0; i < adaptedCorefSysSets.size(); i++) {
                CoRefSetAgata set = adaptedCorefSysSets.get(i);
                System.out.println(set.printTargetSet());
            }
        }

        double recall = -1;
        /// now we modify the ref sets again for recall
        /// we take the original unmodified gold set
        /// we filter all Ids from expanded Sys that did not occur in gold
        //// for all the gold ids we now calculate the same score of correct system ids for each gold set
        /// gold (key) = {a b c}
        /// system (response = {a b} {c}
        /// recall = 1/3(2/3+2/3+1/3) = 0.582
        ///


        double totalScore = 0;
        //// we iterate over the golds ids
        for (int t = 0; t < totalGoldIds.size(); t++) {
            String id = totalGoldIds.get(t);
            if (DEBUG) System.out.println("key mention id = " + id);
            for (int i = 0; i < adaptedCorefSysSets.size(); i++) {
                CoRefSetAgata sysSet = adaptedCorefSysSets.get(i);
                if (sysSet.containsTargetTermId(id)) {
                    if (DEBUG) System.out.println("sysSet.printTargetSet() = " + sysSet.printTargetSet());
                    for (int j = 0; j < corefGoldSets.size(); j++) {
                        CoRefSetAgata goldSet = corefGoldSets.get(j);
                        if (goldSet.containsTargetTermId(id)) {
                            if (DEBUG) System.out.println("goldSet.getTargets() = " + goldSet.printTargetSet());
                            double score = setRecallScore(sysSet, goldSet);
                            if (DEBUG) System.out.println("recall score = " + score);
                            totalScore += score;
                            /// we assume there can only be one!
                            break;
                        }
                    }
                    // we can continue with the next ID
                    break;
                }
                else {
                   // if (DEBUG) System.out.println("Not in sysSet.printTargetSet() = " + sysSet.printTargetSet());
                }
            }
        }
        recall = totalScore*(1.0/totalGoldIds.size());
        if (DEBUG) System.out.println("recall = " + recall);
        return recall;
    }

    public double CorefB3Precision (ArrayList<CoRefSetAgata> corefSysSets,
                                           ArrayList<CoRefSetAgata> corefGoldSets) {

    /*

       3.  if calculating precision then
       4.    Merge all the remaining twinless system mentions with key to form key-p;
       5.    Use response to form response-p
       6.    Through key-p and response-p;
       7.    Calculate B3 precision P .
       8.  end if
    */
        if (DEBUG) System.out.println("PRECISION");

        /// 4.    Merge all the remaining twinless system mentions with key to form key-p;
        /// we add ids that are unique in system as singleton sets to the gold sets
        ArrayList<CoRefSetAgata> adaptedGoldSets = new ArrayList<CoRefSetAgata>();
        for (int i = 0; i < corefGoldSets.size(); i++) {
            CoRefSetAgata coRefSetAgata = corefGoldSets.get(i);
            adaptedGoldSets.add(coRefSetAgata);
        }
        for (int i = 0; i < totalSysIds.size(); i++) {
            String sysId = totalSysIds.get(i);
            if (!totalGoldIds.contains(sysId)) {
                /// add to corefSysSets as singleton
                CoRefSetAgata singleton = new CoRefSetAgata();
                CorefTargetAgata t = new CorefTargetAgata(sysId);
                singleton.addTarget(t);
                adaptedGoldSets.add(singleton);
            }
        }

        if (DEBUG) {
            System.out.println("Original gold sets for precision:"+corefGoldSets.size());
            for (int i = 0; i < corefGoldSets.size(); i++) {
                CoRefSetAgata set = corefGoldSets.get(i);
                System.out.println(set.printTargetSet());
            }
            System.out.println("Adapted gold sets for precision, adding remaing twinless system mentions:"+adaptedGoldSets.size());
            for (int i = 0; i < adaptedGoldSets.size(); i++) {
                CoRefSetAgata set = adaptedGoldSets.get(i);
                System.out.println(set.printTargetSet());
            }
        }

        //// to we calculate the precision for each mention in the expanded system output
        //// we first find the system set in which it is part
        //// then we look for the gold set that has overlap with the system match
        //// we count the number of correct in the gold set and divide by the total number in the gold set
        //// we keep the highest score
        //// gold (key) = {a b c }
        //// system (response)  = {a b d} {i j}
        //// after expanding we get:
        //// gold-exp = {a b c } {d} {i} {j}
        //// system-exp   = {a b d} {i j} {c}
        //// precision = 1/6 (2/3+2/3+1/3+1/2+1/2+1) = 0.611
        //// 1/6 = total number of IDs
        //// 2/3 = (sys-set INTERSECT gold-set)/sys-set {a b d} / {a b c} for a
        //// 2/3 = {a b d} int {a b c} for b
        //// 1 = {c} int {a b c} for c
        //// 1/2 = {i j} int {i} for i
        /////1/2 = {i j} int {j} for j
        //// 1/3   = {a b d} int {d} for d

        double precision = -1;
        double totalScore = 0;
        for (int t = 0; t < totalIds.size(); t++) {
            String id = totalIds.get(t);
            if (DEBUG) System.out.println("id = " + id);
            for (int i = 0; i < corefSysSets.size(); i++) {
                CoRefSetAgata sysSet = corefSysSets.get(i);
                if (sysSet.containsTargetTermId(id)) {
                    if (DEBUG) System.out.println("sysSet.printTargetSet() = " + sysSet.printTargetSet());
                    for (int j = 0; j < adaptedGoldSets.size(); j++) {
                        CoRefSetAgata goldSet = adaptedGoldSets.get(j);
                        if (goldSet.containsTargetTermId(id)) {
                            if (DEBUG) System.out.println("goldSet.getTargets() = " + goldSet.printTargetSet());
                            double score = setPrecisionScore(sysSet, goldSet);
                            if (DEBUG) System.out.println("precision score = " + score);
                            totalScore += score;
                            /// we assume there can only be one!
                            break;
                        }
                    }
                    // we can continue with the next ID
                    break;
                }
                else {
                    if (DEBUG) System.out.println("Not in sysSet.printTargetSet() = " + sysSet.printTargetSet());
                }
            }
        }
        precision = totalScore*(1.0/totalIds.size());
        if (DEBUG) System.out.println("precision = " + precision);
        return precision;
    }
    
    //// we count the number of
    public double setPrecisionScore (CoRefSetAgata sysSet, CoRefSetAgata goldSet)   {
        double score = 0;
        for (int i = 0; i < sysSet.getTargets().size(); i++) {
            CorefTargetAgata s = sysSet.getTargets().get(i);
            if (goldSet.containsTarget(s)) {
                score++;
            }
        }
        if (DEBUG) {
            System.out.println("sysSet = " + sysSet.printTargetSet());
            System.out.println("goldSet = " + goldSet.printTargetSet());
            System.out.println("score = " + score);
            System.out.println("score = " + score / sysSet.getTargets().size());
        }
        return score/sysSet.getTargets().size();
    }

    //// we count the number of
    public double setRecallScore (CoRefSetAgata sysSet, CoRefSetAgata goldSet)   {
        double score = 0;
        for (int i = 0; i < sysSet.getTargets().size(); i++) {
            CorefTargetAgata s = sysSet.getTargets().get(i);
            if (goldSet.containsTarget(s)) {
                score++;
            }
        }
        if (DEBUG) {
            System.out.println("sysSet = " + sysSet.printTargetSet());
            System.out.println("goldSet = " + goldSet.printTargetSet());
            System.out.println("score = " + score);
            System.out.println("score = " + score / goldSet.getTargets().size());
        }
        return score/goldSet.getTargets().size();
    }
}
