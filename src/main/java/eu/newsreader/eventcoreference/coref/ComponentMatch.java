package eu.newsreader.eventcoreference.coref;

import eu.kyotoproject.kaf.KafSense;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.util.EventTypes;
import eu.newsreader.eventcoreference.util.RoleLabels;
import eu.newsreader.eventcoreference.util.Util;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by piek on 5/2/14.
 */
public class ComponentMatch {

    public static boolean compareEventLabelReference(CompositeEvent compositeEvent1,
                                                     CompositeEvent compositeEvent2,
                                                     int threshold) {
        int nMatches = 0;
        for (int i = 0; i < compositeEvent1.getEvent().getUniquePhrases().size(); i++) {
            String phrase1 = compositeEvent1.getEvent().getUniquePhrases().get(i);
            for (int j = 0; j < compositeEvent2.getEvent().getUniquePhrases().size(); j++) {
                String phrase2 = compositeEvent2.getEvent().getUniquePhrases().get(j);
                if (phrase1.equalsIgnoreCase(phrase2)) {
                    nMatches++;
                }
            }
        }
        int m1 = nMatches * 100 / compositeEvent1.getEvent().getUniquePhrases().size();
        int m2 = nMatches * 100 / compositeEvent2.getEvent().getUniquePhrases().size();
        /*if (compositeEvent1.getEvent().getUniquePhrases().contains("check") &&
                compositeEvent2.getEvent().getUniquePhrases().contains("check")) {
            System.out.println("threshold = " + threshold);
            System.out.println("nMatches:"+nMatches);
            System.out.println("m1 = " + m1);
            System.out.println("m2 = " + m2);
            System.out.println("compositeEvent1 = " + compositeEvent1.getEvent().getUniquePhrases().toString());
            System.out.println("compositeEvent2 = " + compositeEvent2.getEvent().getUniquePhrases().toString());
        }*/
        if (( m1>= threshold) &&
                (m2 >= threshold)) {
            return true;
        } else {
            /*if (compositeEvent1.getEvent().getUniquePhrases().contains("check") &&
                    compositeEvent2.getEvent().getUniquePhrases().contains("check")) {
                System.out.println("nMatches:"+nMatches);
                System.out.println("m1 = " + m1);
                System.out.println("m2 = " + m2);
                System.out.println("compositeEvent1 = " + compositeEvent1.getEvent().getUniquePhrases().toString());
                System.out.println("compositeEvent2 = " + compositeEvent2.getEvent().getUniquePhrases().toString());
            }*/
            return false;
        }
    }

    public static ArrayList<SemActor> getSemActorsFromCompositeEvent (CompositeEvent event, ArrayList<String> actorURIs) {
        ArrayList<SemActor> actors = new ArrayList<SemActor>();
        for (int i = 0; i < event.getMySemActors().size(); i++) {
            SemActor semActor = event.getMySemActors().get(i);
            if (actorURIs.contains(semActor.getURI())) {
                actors.add(semActor);
            }
        }
        return actors;
    }


    public static boolean phraseMatch (SemActor semActor1, ArrayList<SemActor> actors2) {
        String topPhrase = semActor1.getTopPhraseAsLabel();
        for (int j = 0; j < actors2.size(); j++) {
            SemActor semActor2 = actors2.get(j);
            for (int l = 0; l < semActor2.getPhraseCounts().size(); l++) {
                PhraseCount phraseCount2 = semActor2.getPhraseCounts().get(l);
                if (topPhrase.equalsIgnoreCase(phraseCount2.getPhrase())) {
                   // System.out.println("topPhrase = " + topPhrase);
                    return true;
                }
            }
        }
        int idx = topPhrase.lastIndexOf(" ");
        if (idx>-1) {
            String lastPhrase = topPhrase.substring(idx).trim();
            if (lastPhrase.length()>2) {
                for (int j = 0; j < actors2.size(); j++) {
                    SemActor semActor2 = actors2.get(j);
                    for (int l = 0; l < semActor2.getPhraseCounts().size(); l++) {
                        PhraseCount phraseCount2 = semActor2.getPhraseCounts().get(l);
                        if (lastPhrase.equalsIgnoreCase(phraseCount2.getPhrase())) {
                          //  System.out.println("top subPhrase = " + topPhrase);
                            return true;
                        }
                    }
                }
            }
        }
        idx = topPhrase.lastIndexOf("'s");
        if (idx>-1 && topPhrase.endsWith("'s")) {
            String lastPhrase = topPhrase.substring(0, idx-1).trim();
            if (lastPhrase.length()>2) {
                for (int j = 0; j < actors2.size(); j++) {
                    SemActor semActor2 = actors2.get(j);
                    for (int l = 0; l < semActor2.getPhraseCounts().size(); l++) {
                        PhraseCount phraseCount2 = semActor2.getPhraseCounts().get(l);
                        if (lastPhrase.equalsIgnoreCase(phraseCount2.getPhrase())) {
                            // System.out.println("topPhrase's = " + topPhrase);

                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean compareActorPrefLabelReference(CompositeEvent compositeEvent1,
                                                         CompositeEvent compositeEvent2,
                                                        ArrayList<String> roleObjects1,
                                                        ArrayList<String> roleObjects2) {
            ArrayList<SemActor> actors1 = getSemActorsFromCompositeEvent(compositeEvent1, roleObjects1);
            ArrayList<SemActor> actors2 = getSemActorsFromCompositeEvent(compositeEvent2, roleObjects2);
            for (int i = 0; i < actors1.size(); i++) {
                SemActor semActor1 = actors1.get(i);
                if (phraseMatch(semActor1, actors2)) {
                    return true;
                }
            }
            for (int i = 0; i < actors2.size(); i++) {
                SemActor semActor2 = actors2.get(i);
                if (phraseMatch(semActor2, actors1)) {
                    return true;
                }
            }
        return false;
    }

    public static boolean compareEventWordNetReference(CompositeEvent compositeEvent1,
                                                       CompositeEvent compositeEvent2,
                                                       int threshold) {

        int nMatches = 0;
        int wn1 = 0;
        int wn2 = 0;

        for (int j = 0; j < compositeEvent1.getEvent().getConcepts().size(); j++) {
            KafSense kafSense1 = compositeEvent1.getEvent().getConcepts().get(j);
            if (kafSense1.getResource().equalsIgnoreCase("wordnet") ||
                    kafSense1.getSensecode().startsWith("ili")) {
                wn1++;
            }
        }
        for (int j = 0; j < compositeEvent2.getEvent().getConcepts().size(); j++) {
            KafSense kafSense2 = compositeEvent2.getEvent().getConcepts().get(j);
            if (kafSense2.getResource().equalsIgnoreCase("wordnet") ||
                    kafSense2.getSensecode().startsWith("ili")) {
                wn2++;
            }
        }
        if (wn1==0 || wn2==0) {
            return false;
        }

        for (int i = 0; i < compositeEvent1.getEvent().getConcepts().size(); i++) {
            KafSense kafSense1 = compositeEvent1.getEvent().getConcepts().get(i);
            if (kafSense1.getResource().equalsIgnoreCase("wordnet") ||
                    kafSense1.getSensecode().startsWith("ili")) {
                for (int j = 0; j < compositeEvent2.getEvent().getConcepts().size(); j++) {
                    KafSense kafSense2 = compositeEvent2.getEvent().getConcepts().get(j);
                    if (kafSense2.getResource().equalsIgnoreCase("wordnet") ||
                            kafSense2.getSensecode().startsWith("ili")) {
                        if (kafSense1.getSensecode().equals(kafSense2.getSensecode())) {
                            nMatches++;
                        }
                    }
                }
            }
        }
        if ((nMatches * 100 / wn1 >= threshold) &&
                (nMatches * 100 / wn2 >= threshold)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean compareReference(ArrayList<KafSense> ref1, ArrayList<KafSense> ref2,
                                                       int threshold) {

        int nMatches = 0;
        if (ref1.size()==0 || ref2.size()==0) {
            return false;
        }
        for (int i = 0; i < ref1.size(); i++) {
            KafSense kafSense1 = ref1.get(i);
            for (int j = 0; j < ref2.size(); j++) {
                KafSense kafSense2 = ref2.get(j);
                if (kafSense1.getSensecode().equals(kafSense2.getSensecode())) {
                    nMatches++;
                }
            }
        }
        if ((nMatches * 100 / ref1.size() >= threshold) &&
                (nMatches * 100 / ref2.size() >= threshold)) {
            return true;
        } else {
            return false;
        }
    }



    @Deprecated
    public static boolean compareCompositeEventDeprecated(CompositeEvent compositeEvent1,
                                                CompositeEvent compositeEvent2,
                                                String eventType,
                                                ArrayList<String> roleArrayList) {

        ArrayList<String> neededRoles = new ArrayList<String>();
        if (EventTypes.isCONTEXTUAL(eventType)) {
            return compareCompositeEvent(compositeEvent1, compositeEvent2, neededRoles, false);
        } else if (EventTypes.isCOMMUNICATION(eventType)) {
            neededRoles.add("a0");
            return compareCompositeEvent(compositeEvent1, compositeEvent2, neededRoles, false);
        } else if (EventTypes.isGRAMMATICAL(eventType)) {
            neededRoles.add("a1");
            return compareCompositeEvent(compositeEvent1, compositeEvent2, neededRoles, false);
        } else if (EventTypes.isFUTURE(eventType)) {
            ///// For FUTURE events we demand that all participants match except the temporal relations
            return compareCompositeEventToMatchAllProbBankRoles(compositeEvent1, compositeEvent2);
        }
        return false;

    }

    /**
     * @Deprecated: new version uses PreferedLabel phrase matching
     * For each of the roles in the role ArrayList there needs to be match
     *
     * @param compositeEvent1
     * @param compositeEvent2
     * @return
     */
/*    public static boolean compareCompositeEvent(CompositeEvent compositeEvent1,
                                                CompositeEvent compositeEvent2,
                                                ArrayList<String> minimallyRequiredRoles
                                                ) {
        if (compositeEvent1.getMySemActors().size() == 0 && compositeEvent2.getMySemActors().size() == 0) {
            return false;
        }
        if (minimallyRequiredRoles.contains("all")) {
            return compareCompositeEventToMatchAllProbBankRoles(compositeEvent1, compositeEvent2);
        }
        for (int i = 0; i < minimallyRequiredRoles.size(); i++) {
            String requiredRole = minimallyRequiredRoles.get(i);
            ArrayList roleObjects1 = Util.getObjectsForPredicate(compositeEvent1.getMySemRelations(), requiredRole);
            ArrayList roleObjects2 = Util.getObjectsForPredicate(compositeEvent2.getMySemRelations(), requiredRole);
            if (roleObjects1.size() == 0 || roleObjects2.size() == 0) {
                return false;
            } else if (Collections.disjoint(roleObjects1, roleObjects2)) {
                return false;
            } else {
                /// we found a match for this required role
            }
        }
        /////  FOR ALL REQUIRED ROLES WE FOUND A MATCH
        return true;
    }*/

    /**
     * For each of the roles in the role ArrayList there needs to be match
     *
     * @param compositeEvent1
     * @param compositeEvent2
     * @return
     */
    public static boolean compareCompositeEvent(CompositeEvent compositeEvent1,
                                                CompositeEvent compositeEvent2,
                                                ArrayList<String> minimallyRequiredRoles, boolean DEBUG) {

        if (DEBUG) System.out.println("minimallyRequiredRoles = " + minimallyRequiredRoles.toString());
        if (DEBUG) System.out.println("compositeEvent1.getMySemActors().size() = " + compositeEvent1.getMySemActors().size());
        if (DEBUG) System.out.println("compositeEvent2.getMySemActors().size() = " + compositeEvent2.getMySemActors().size());
            if (minimallyRequiredRoles.contains("none") || minimallyRequiredRoles.size()==0) {
                if (DEBUG) System.out.println("NOTHING TO MATCH NOTHING TO MATCH");
            return true;
        }
        if (compositeEvent1.getMySemActors().size() == 0 || compositeEvent2.getMySemActors().size() == 0) {
            if (DEBUG) System.out.println("ONE IS EMPTY NO MATCH");
                return false;
        }
       // System.out.println("minimallyRequiredRoles = " + minimallyRequiredRoles.toString());
        if (minimallyRequiredRoles.contains("all")) {
            return compareCompositeEventToMatchAllProbBankRoles(compositeEvent1, compositeEvent2);
        }
        int nMatches = 0;
        if (minimallyRequiredRoles.size()>0
                && !minimallyRequiredRoles.contains("none")
                && !minimallyRequiredRoles.contains("anyrole")
                && !minimallyRequiredRoles.contains("any")
                ) {
            for (int i = 0; i < minimallyRequiredRoles.size(); i++) {
                String requiredRole = minimallyRequiredRoles.get(i);
                ArrayList roleObjects1 = Util.getObjectsForPredicate(compositeEvent1.getMySemRelations(), requiredRole);
                ArrayList roleObjects2 = Util.getObjectsForPredicate(compositeEvent2.getMySemRelations(), requiredRole);
              //  System.out.println("roleObjects1 = " + roleObjects1.size());
              //  System.out.println("roleObjects2 = " + roleObjects2.size());
                if (!Collections.disjoint(roleObjects1, roleObjects2)) {
                    //// URI MATCH
                    if (DEBUG) System.out.println("URI MATCH");
                    nMatches++;
                } else if (compareActorPrefLabelReference(compositeEvent1, compositeEvent2, roleObjects1, roleObjects2)) {
                    if (DEBUG) System.out.println("LABEL MATCH");

                    nMatches++;
                }
            }
            if (nMatches == minimallyRequiredRoles.size()) {
                /////  FOR ALL REQUIRED ROLES WE FOUND A MATCH
                if (DEBUG) System.out.println("EACH TYPE OF ROLE HAS A MATCH");

                return true;
            } else {
                return false;
            }
        }
        else if (minimallyRequiredRoles.contains("anyrole") ||
                 minimallyRequiredRoles.contains("any")) {
            /// IF NO SPECIFIC ROLES NEED TO BE PRESENT, WE GET ALL ROLES EXCEPT TIME AND CHECK IF THERE IS ANY MATCH
            ArrayList<String> roleObjects1 = Util.getObjectsForPredicate(compositeEvent1.getMySemRelations());
            ArrayList<String> roleObjects2 = Util.getObjectsForPredicate(compositeEvent2.getMySemRelations());
         //   System.out.println("roleObjects1 = " + roleObjects1.size());
         //   System.out.println("roleObjects2 = " + roleObjects2.size());
            if (!Collections.disjoint(roleObjects1, roleObjects2)) {
                //// URI MATCH
               // System.out.println("roleObjects1 = " + roleObjects1.toString());
               // System.out.println("roleObjects2 = " + roleObjects2.toString());
/*                for (int i = 0; i < roleObjects1.size(); i++) {
                    String s = roleObjects1.get(i);
                    if (roleObjects2.contains(s)) {
                        System.out.println("s = " + s);
                    }
                }*/
                if (DEBUG) System.out.println("URI MATCH");
                return true;
            }
            else if (compareActorPrefLabelReference(compositeEvent1, compositeEvent2, roleObjects1, roleObjects2)) {
                if (DEBUG) System.out.println("LABEL MATCH");
                /// PREF LABEL MATCH
                return true;
            }
            else {
                return false;
            }
        }
        else {
            /// should never occur
            if (DEBUG) System.out.println("This should never happen minimallyRequiredRoles = " + minimallyRequiredRoles.toString());
            return false;
        }
    }


     /**
         * Compare time
         *
         * @param compositeEvent1
         * @param compositeEvent2
         * @return
         */
        public static boolean compareTimeCompositeEvent(CompositeEvent compositeEvent1,
                                                    CompositeEvent compositeEvent2,
                                                    String TIME
                                                    ) {

           // System.out.println("compositeEvent1 = " + compositeEvent1.getEvent().getId());
           // System.out.println("compositeEvent1.getMySemTimes().size() = " + compositeEvent1.getMySemTimes().size());
           // System.out.println("compositeEvent2 = " + compositeEvent2.getEvent().getId());
           // System.out.println("compositeEvent2.getMySemTimes().size() = " + compositeEvent2.getMySemTimes().size());
            //// if no time we do not know
            if (compositeEvent1.getMySemTimes().size() == 0 || compositeEvent2.getMySemTimes().size() == 0) {
             //   System.out.println("NO TIME ASSOCIATED.");
                return true;
            }
            if (TIME.equalsIgnoreCase("year")) {
                return compareTimeYear(compositeEvent1.getMySemTimes(), compositeEvent2.getMySemTimes());
            }
            else if (TIME.equalsIgnoreCase("month")) {
                return compareTimeMonth(compositeEvent1.getMySemTimes(), compositeEvent2.getMySemTimes());
            }
            else if (TIME.equalsIgnoreCase("day")) {
                return compareTime(compositeEvent1.getMySemTimes(), compositeEvent2.getMySemTimes());
            }
            else {
                /// no known TIME value
              //  System.out.println("NO PROPER TIME PARAMATER:"+TIME);
                return true;
            }
        }


    /**
     * @Deprecated
     * For each of the roles in the role ArrayList there needs to be match
     *
     * @param compositeEvent1
     * @param compositeEvent2
     * @return
     */
    public static boolean compareCompositeEventDeprecated(CompositeEvent compositeEvent1,
                                                CompositeEvent compositeEvent2,
                                                ArrayList<String> minimallyRequiredRoles,
                                                ArrayList<String> roleArrayList
                                                ) {
        int roleMatchCount = 0;
        // System.out.println("roleArrayList = " + roleArrayList.toString());
        if (compositeEvent1.getMySemActors().size() == 0 && compositeEvent2.getMySemActors().size() == 0) {
            return false;
        } else {
            for (int i = 0; i < roleArrayList.size(); i++) {
                String propBankRole = roleArrayList.get(i);
                //   System.out.println("propBankRole = " + propBankRole);
                ArrayList roleObjects1 = Util.getObjectsForPredicate(compositeEvent1.getMySemRelations(), propBankRole);
                ArrayList roleObjects2 = Util.getObjectsForPredicate(compositeEvent2.getMySemRelations(), propBankRole);
                if (roleObjects1.size() == 0 && roleObjects2.size() == 0) {
                    /// both events do not have this participant
                    if (!minimallyRequiredRoles.contains(propBankRole) || minimallyRequiredRoles.size() == 0) {
                        roleMatchCount++;
                        /// we count this as a match unless it is a minimallyRequired role
                    }
                } else {
                    if (!Collections.disjoint(roleObjects1, roleObjects2)) {
                        //  System.out.println("NOT DISJOINT");
                        //  System.out.println("roleObjects1 = " + roleObjects1.toString());
                        //    System.out.println("roleObjects2 = " + roleObjects1.toString());
                        roleMatchCount++;
                    } else {
                        //   System.out.println("DISJOINT");
                        //   System.out.println("roleObjects1 = " + roleObjects1.toString());
                        //    System.out.println("roleObjects2 = " + roleObjects1.toString());
                    }
                }

            }
        }
        if (roleMatchCount == roleArrayList.size()) {
            // System.out.println("roleMatchCount = " + roleMatchCount);
            return true;
        } else {
            return false;
        }
    }


    /**
     * For each of the propbank roles in the role ArrayList there needs to be a match
     *
     * @param compositeEvent1
     * @param compositeEvent2
     * @return
     */
    public static boolean compareCompositeEventToMatchAllProbBankRoles(CompositeEvent compositeEvent1,
                                                                       CompositeEvent compositeEvent2) {
        int roleMatchCount = 0;
        ArrayList<String> roleArrayList1 = new ArrayList<String>();
        ArrayList<String> roleArrayList2 = new ArrayList<String>();
        if (compositeEvent1.getMySemActors().size() == 0 || compositeEvent2.getMySemActors().size() == 0) {
            return false;
        } else {
            /// We make a list of the propbank roles that are associated with the event of compositeEvent1
            for (int i = 0; i < compositeEvent1.getMySemRelations().size(); i++) {
                SemRelation semRelation = compositeEvent1.getMySemRelations().get(i);
                for (int j = 0; j < semRelation.getPredicates().size(); j++) {
                    String predicate = semRelation.getPredicates().get(j);
                    if (predicate.toLowerCase().indexOf("propbank") > -1) {
                        if (!roleArrayList1.contains(predicate)) {
                            roleArrayList1.add(predicate);
                        }
                    }
                }
            }
            /// We make a list of the propbank roles that are associated with the event of compositeEvent2
            for (int i = 0; i < compositeEvent2.getMySemRelations().size(); i++) {
                SemRelation semRelation = compositeEvent2.getMySemRelations().get(i);
                for (int j = 0; j < semRelation.getPredicates().size(); j++) {
                    String predicate = semRelation.getPredicates().get(j);
                    if (predicate.toLowerCase().indexOf("propbank") > -1) {
                        if (!roleArrayList2.contains(predicate)) {
                            roleArrayList2.add(predicate);
                        }
                    }
                }
            }
            /// if the lists of propbank roles labels do not match, it makes not sense to continue
            if (!Collections.disjoint(roleArrayList1, roleArrayList2)) {
                return false;
            }
            /// Next we check the objects for each of the roles to see if there is a match
            for (int i = 0; i < roleArrayList1.size(); i++) {
                String propBankRole = roleArrayList1.get(i);
                // System.out.println("propBankRole = " + propBankRole);
                ArrayList roleObjects1 = Util.getObjectsForPredicate(compositeEvent1.getMySemRelations(), propBankRole);
                ArrayList roleObjects2 = Util.getObjectsForPredicate(compositeEvent2.getMySemRelations(), propBankRole);
                if (!Collections.disjoint(roleObjects1, roleObjects2)) {
                    //  System.out.println("roleObjects1 = " + roleObjects1.toString());
                    //  System.out.println("roleObjects2 = " + roleObjects1.toString());
                    roleMatchCount++;
                } else if (compareActorPrefLabelReference(compositeEvent1, compositeEvent2, roleObjects1, roleObjects2)) {
                        roleMatchCount++;
                    //  System.out.println("DISJOINT");
                    //  System.out.println("roleObjects1 = " + roleObjects1.toString());
                    //  System.out.println("roleObjects2 = " + roleObjects1.toString());
                }
            }
        }
        if (roleMatchCount == roleArrayList1.size()) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Compares two time objects to determine if they exclude each other or not
     *
     * @param mySemTimes
     * @param semTimes
     * @return
     */
    public static boolean compareTime(ArrayList<SemTime> mySemTimes,
                                      ArrayList<SemTime> semTimes) {

        for (int i = 0; i < mySemTimes.size(); i++) {
            SemTime mySemTime = mySemTimes.get(i);
           // System.out.println("myOwlTime.getDateStringURI() = " + mySemTime.getOwlTime().getDateStringURI());

            for (int j = 0; j < semTimes.size(); j++) {
                SemTime semTime = semTimes.get(j);
               // System.out.println("owlTime.getDateStringURI() = " + semTime.getOwlTime().getDateStringURI());

                /// replace this by exact time matches....
                if (mySemTime.getOwlTime().matchTimeEmbedded(semTime.getOwlTime())) {
                    //  System.out.println("myOwlTime.getDateStringURI() = " + mySemTime.getOwlTime().getDateStringURI());
                    //  System.out.println("owlTime.getDateStringURI() = " + semTime.getOwlTime().getDateStringURI());

                    return true;
                } else if (mySemTime.getOwlTimeBegin().matchTimeEmbedded(semTime.getOwlTimeBegin())) {
                    //  System.out.println("begin myOwlTime.getDateStringURI() = " + mySemTime.getOwlTime().getDateStringURI());
                    //  System.out.println("end owlTime.getDateStringURI() = " + semTime.getOwlTime().getDateStringURI());

                    return true;
                } else if (mySemTime.getOwlTimeEnd().matchTimeEmbedded(semTime.getOwlTimeEnd())) {
                    //  System.out.println("end myOwlTime.getDateStringURI() = " + mySemTime.getOwlTime().getDateStringURI());
                    //  System.out.println("end owlTime.getDateStringURI() = " + semTime.getOwlTime().getDateStringURI());

                    return true;
                }
            }

        }
        return false;
    }

    static boolean valueMatch(String value1, String value2) {
        if (value1.equals(value2) && !value1.isEmpty()) {
            return true;
        }
        return false;
    }
    /**
     * Compares two time objects to determine if they exclude each other or not
     *
     * @param mySemTimes
     * @param semTimes
     * @return
     */
    public static boolean compareTimeYearOld(ArrayList<SemTime> mySemTimes,
                                      ArrayList<SemTime> semTimes) {

        for (int i = 0; i < mySemTimes.size(); i++) {
            SemTime mySemTime = mySemTimes.get(i);
            for (int j = 0; j < semTimes.size(); j++) {
                SemTime semTime = semTimes.get(j);
                if (mySemTime.getOwlTime().getYear().equals(semTime.getOwlTime().getYear())) {
                    System.out.println("mySemTime.getOwlTime().getYear() = " + mySemTime.getOwlTime().getYear());
                    System.out.println("semTime.getOwlTime().getYear() = " + semTime.getOwlTime().getYear());
                    return true;
                }
                else if (mySemTime.getOwlTimeBegin().getYear().equals(semTime.getOwlTimeBegin().getYear())) {
                    return true;
                }
                else if (mySemTime.getOwlTimeEnd().getYear().equals(semTime.getOwlTimeEnd().getYear())) {
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * Compares two time objects to determine if they exclude each other or not
     *
     * @param mySemTimes
     * @param semTimes
     * @return
     */
    public static boolean compareTimeYear(ArrayList<SemTime> mySemTimes,
                                      ArrayList<SemTime> semTimes) {

        for (int i = 0; i < mySemTimes.size(); i++) {
            SemTime mySemTime = mySemTimes.get(i);
            for (int j = 0; j < semTimes.size(); j++) {
                SemTime semTime = semTimes.get(j);
                if (valueMatch(mySemTime.getOwlTime().getYear(), semTime.getOwlTime().getYear())) {
                    System.out.println("mySemTime.getOwlTime().getYear() = " + mySemTime.getOwlTime().getYear());
                    System.out.println("semTime.getOwlTime().getYear() = " + semTime.getOwlTime().getYear());
                    return true;
                }
                else if (valueMatch(mySemTime.getOwlTimeBegin().getYear(),semTime.getOwlTimeBegin().getYear())) {
                    return true;
                }
                else if (valueMatch(mySemTime.getOwlTimeEnd().getYear(), semTime.getOwlTimeEnd().getYear())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Compares two time objects to determine if they exclude each other or not
     *
     * @param mySemTimes
     * @param semTimes
     * @return
     */
    public static boolean compareTimeMonth(ArrayList<SemTime> mySemTimes,
                                      ArrayList<SemTime> semTimes) {

        for (int i = 0; i < mySemTimes.size(); i++) {
            SemTime mySemTime = mySemTimes.get(i);
            for (int j = 0; j < semTimes.size(); j++) {
                SemTime semTime = semTimes.get(j);
                if (valueMatch(mySemTime.getOwlTime().getMonth(),semTime.getOwlTime().getMonth()) &&
                        valueMatch(mySemTime.getOwlTime().getYear(), semTime.getOwlTime().getYear()))
                    {
                    return true;
                } else if (valueMatch(mySemTime.getOwlTimeBegin().getMonth(), semTime.getOwlTimeBegin().getMonth()) &&
                        valueMatch(mySemTime.getOwlTimeBegin().getYear(),semTime.getOwlTimeBegin().getYear()))
                    {
                    return true;
                } else if (valueMatch(mySemTime.getOwlTimeEnd().getMonth(), semTime.getOwlTimeEnd().getMonth()) &&
                        valueMatch(mySemTime.getOwlTimeEnd().getYear(), semTime.getOwlTimeEnd().getYear())) {

                    return true;
                }
            }

        }
        return false;
    }

    /**
     * Compares two lists of places to determine of they exclude each other. Comparison is based on URIs, external references and the most frequent label found as a mention
     * If there is a single match across the arrays, the function returns true.
     *
     * @param mySemPlaces
     * @param semPlaces
     * @return
     */
    public static boolean comparePlace(ArrayList<SemPlace> mySemPlaces,
                                       ArrayList<SemPlace> semPlaces) {

        for (int i = 0; i < mySemPlaces.size(); i++) {
            SemObject mySemPlace = mySemPlaces.get(i);
            for (int j = 0; j < semPlaces.size(); j++) {
                SemObject semPlace = semPlaces.get(j);
                if (mySemPlace.getURI().equals(semPlace.getURI()) && !semPlace.getURI().isEmpty()) {
                    //     System.out.println("semPlace.getURI() = " + semPlace.getURI());
                    return true;
                } else if (semPlace.getReference().equals(semPlace.getReference()) && !semPlace.getReference().isEmpty()) {
                    //     System.out.println("semPlace.getReference() = " + semPlace.getReference());
                    return true;
                } else if (semPlace.getTopPhraseAsLabel().equals(semPlace.getTopPhraseAsLabel())) {
                    //    System.out.println("semPlace.getTopPhraseAsLabel() = " + semPlace.getTopPhraseAsLabel());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Compares two lists of actors to determine of they exclude each other. Comparison is based on URIs, external references and the most frequent label found as a mention
     * If there is a single match across the arrays, the function returns true.
     *
     * @param mySemActors
     * @param semActors
     * @return
     */
    public static boolean compareActor(ArrayList<SemActor> mySemActors,
                                       ArrayList<SemActor> semActors) {

        for (int i = 0; i < mySemActors.size(); i++) {
            SemObject mySemActor = mySemActors.get(i);
            for (int j = 0; j < semActors.size(); j++) {
                SemObject semActor = semActors.get(j);
                if (semActor.getURI().equals(mySemActor.getURI()) && !semActor.getURI().isEmpty()) {
                    // System.out.println("semActor.getURI() = " + semActor.getURI());
                    return true;
                } else if (semActor.getReference().equals(mySemActor.getReference()) && !semActor.getReference().isEmpty()) {
                    //  System.out.println("semActor.getReference() = " + semActor.getReference());
                    return true;
                } else if (semActor.getTopPhraseAsLabel().equals(mySemActor.getTopPhraseAsLabel())) {
                    // System.out.println("semActor.getTopPhraseAsLabel() = " + semActor.getTopPhraseAsLabel());
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Compares two list of actors by URIs for full equivalence
     *
     * @param mySemActors
     * @param semActors
     * @return
     */
    public static boolean compareAllActorsByURI(ArrayList<SemActor> mySemActors,
                                                ArrayList<SemActor> semActors) {

        boolean match = true;
        for (int i = 0; i < mySemActors.size(); i++) {
            SemObject mySemActor = mySemActors.get(i);
            for (int j = 0; j < semActors.size(); j++) {
                SemObject semActor = semActors.get(j);
                if (!semActor.getURI().equals(mySemActor.getURI()) && !semActor.getURI().isEmpty()) {
                    // System.out.println("semActor.getURI() = " + semActor.getURI());
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Compares two list of actors by external references for full equivalence
     *
     * @param mySemActors
     * @param semActors
     * @return
     */
    public static boolean compareAllActorsByReferences(ArrayList<SemActor> mySemActors,
                                                       ArrayList<SemActor> semActors) {

        boolean match = true;
        for (int i = 0; i < mySemActors.size(); i++) {
            SemObject mySemActor = mySemActors.get(i);
            for (int j = 0; j < semActors.size(); j++) {
                SemObject semActor = semActors.get(j);
                if (!semActor.getReference().equals(mySemActor.getReference()) && !semActor.getReference().isEmpty()) {
                    //  System.out.println("semActor.getReference() = " + semActor.getReference());
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Compares two list of actors by most frequent labels for full equivalence
     *
     * @param mySemActors
     * @param semActors
     * @return
     */
    public static boolean compareAllActorsByTopPhrase(ArrayList<SemActor> mySemActors,
                                                      ArrayList<SemActor> semActors) {

        boolean match = true;
        for (int i = 0; i < mySemActors.size(); i++) {
            SemObject mySemActor = mySemActors.get(i);
            for (int j = 0; j < semActors.size(); j++) {
                SemObject semActor = semActors.get(j);
                if (!semActor.getTopPhraseAsLabel().equals(mySemActor.getTopPhraseAsLabel())) {
                    // System.out.println("semActor.getTopPhraseAsLabel() = " + semActor.getTopPhraseAsLabel());
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * @DEPRECATED
     * Main function for comparing composite events subdivided by the eventType. For each composite event we assume that the event type matches,
     * the event itself matches given a threshold and matching function and the time matches (possibly also the place)
     * @param compositeEvent1
     * @param compositeEvent2
     * @param eventType
     * @return
     */
/*    public static boolean compareCompositeEvent (CompositeEvent compositeEvent1, CompositeEvent compositeEvent2, String eventType, ArrayList<String> roleArrayList) {
        if (EventTypes.isCONTEXTUAL(eventType)) {
           return compareCompositeEventContextual(compositeEvent1, compositeEvent2);
        }
        else if (EventTypes.isCOMMUNICATION(eventType)) {
            return compareCompositeEventCommunicationCognition(compositeEvent1, compositeEvent2);
        }
        else if (EventTypes.isGRAMMATICAL(eventType)) {
                return compareCompositeEventGrammatical(compositeEvent1, compositeEvent2);
        }
        return false;
    }*/

    /**
     * In the case of a communication or cognition event, we require that the PRIMEPARTICIPANT is identical.
     * The other participants are less well defined and not compared.
     *
     * @param compositeEvent1
     * @param compositeEvent2
     * @return
     */
    public static boolean compareCompositeEventCommunicationCognition(CompositeEvent compositeEvent1, CompositeEvent compositeEvent2) {
        if (matchingCompositeEventSemActor(compositeEvent1, compositeEvent2, RoleLabels.PRIMEPARTICIPANT)) {
            return true;
        }
        return false;
    }

    /**
     * Grammatical events are more complex expressions and we require that at least two participants match
     * of which one is the prime participant and one is another participant
     *
     * @param compositeEvent1
     * @param compositeEvent2
     * @return
     */
    public static boolean compareCompositeEventGrammatical(CompositeEvent compositeEvent1, CompositeEvent compositeEvent2) {
        if (matchingCompositeEventSemActor(compositeEvent1, compositeEvent2, RoleLabels.PRIMEPARTICIPANT) &&
                matchingCompositeEventSemActor(compositeEvent1, compositeEvent2, RoleLabels.NONPRIMEPARTICIPANT)) {
            return true;
        }
        return false;
    }


    /**
     * Contextual events need at least 1 participant to match and 1 location
     *
     * @param compositeEvent1
     * @param compositeEvent2
     * @return
     */
    public static boolean compareCompositeEventContextual(CompositeEvent compositeEvent1,
                                                          CompositeEvent compositeEvent2) {
        if (compositeEvent1.getMySemActors().size() == 0 && compositeEvent2.getMySemActors().size() == 0) {
            return false;
        } else {
            /// match at least one actor
            if (!compareActor(compositeEvent1.getMySemActors(), compositeEvent2.getMySemActors())) {
                return false;
            }
        }
        /// if we get up to here we can assume a match
        return true;
    }

    /**
     * Determines if the objects of two predicates are the same for a set of relations, where the URI of the object should match
     * and the role should match a set of roles. The role of a SemRelation is either the predicate or a set of predicates
     *
     * @param compositeEvent1
     * @param compositeEvent2
     * @param roles
     * @return
     */
    public static boolean matchingCompositeEventSemActor(CompositeEvent compositeEvent1, CompositeEvent compositeEvent2, String[] roles) {
        for (int i = 0; i < compositeEvent1.getMySemRelations().size(); i++) {
            SemRelation semRelation1 = compositeEvent1.getMySemRelations().get(i);
            if (RoleLabels.isROLE(semRelation1.getPredicates(), roles)) {
                for (int j = 0; j < compositeEvent2.getMySemRelations().size(); j++) {
                    SemRelation semRelation2 = compositeEvent2.getMySemRelations().get(j);
                    if (RoleLabels.isROLE(semRelation2.getPredicates(), roles)) {
                        if (semRelation1.getObject().equals(semRelation2.getObject())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if all objects of two predicates are the same for a set of relations, where the URI of the object should match
     * and the role should match a set of roles. The role of a SemRelation is either the predicate or a set of predicates
     *
     * @param compositeEvent1
     * @param compositeEvent2
     * @param roles
     * @return
     */
    public static boolean matchingCompositeEventSemActorAll(CompositeEvent compositeEvent1, CompositeEvent compositeEvent2, String[] roles) {
        boolean ALLMATCH = false;
        for (int i = 0; i < compositeEvent1.getMySemRelations().size(); i++) {
            SemRelation semRelation1 = compositeEvent1.getMySemRelations().get(i);
            if (RoleLabels.isROLE(semRelation1.getPredicates(), roles)) {
                //  System.out.println("semRelation1.getPredicate() = " + semRelation1.getPredicate());
                for (int j = 0; j < compositeEvent2.getMySemRelations().size(); j++) {
                    SemRelation semRelation2 = compositeEvent2.getMySemRelations().get(j);
                    if (RoleLabels.isROLE(semRelation2.getPredicates(), roles)) {
                        //// both events have the role that is required and can potentially match
                        if (!semRelation1.getObject().equals(semRelation2.getObject())) {
                            //  System.out.println("semRelation2.getObject() = " + semRelation2.getObject());
                            /// all objects should match so if there one that does not then we can bail out.
                            return false;
                        } else {
                            ALLMATCH = true;
                        }
                    }
                }
                if (ALLMATCH) {
                    /// there is at least one matching role and all the objects match
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean equalSemRelation(SemRelation semRelation1, SemRelation semRelation2) {
        for (int i = 0; i < semRelation1.getPredicates().size(); i++) {
            String pred1 = semRelation1.getPredicates().get(i);
            for (int j = 0; j < semRelation2.getPredicates().size(); j++) {
                String pred2 = semRelation2.getPredicates().get(j);
                if (pred1.equals(pred2)
                        &&
                        semRelation1.getObject().equals(semRelation2.getObject())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean compareComponents(SemObject mySemEvent,
                                            ArrayList<SemObject> theSemActors,
                                            ArrayList<SemObject> theSemPlaces,
                                            ArrayList<SemObject> theSemTimes,
                                            ArrayList<SemRelation> mySemRelations,
                                            SemObject semEvent,
                                            ArrayList<SemObject> semActors,
                                            ArrayList<SemObject> semPlaces,
                                            ArrayList<SemObject> semTimes,
                                            ArrayList<SemRelation> semRelations) {

        ArrayList<SemTime> mySemTimes = Util.castToTime(getMySemObjects(mySemEvent, mySemRelations, theSemTimes));
        ArrayList<SemTime> oSemTimes = Util.castToTime(getMySemObjects(semEvent, semRelations, semTimes));
        if (!compareTime(mySemTimes, oSemTimes)) {
            return false;
        }
        ArrayList<SemPlace> mySemPlaces = Util.castToPlace(getMySemObjects(mySemEvent, mySemRelations, theSemPlaces));
        ArrayList<SemPlace> oSemPlaces = Util.castToPlace(getMySemObjects(semEvent, semRelations, semPlaces));
        if (mySemPlaces.size() > 0 && oSemPlaces.size() > 0) {
            if (!comparePlace(mySemPlaces, oSemPlaces)) {
                return false;
            }
        }
        ArrayList<SemActor> mySemActors = Util.castToActor(getMySemObjects(mySemEvent, mySemRelations, theSemActors));
        ArrayList<SemActor> oSemActors = Util.castToActor(getMySemObjects(semEvent, semRelations, semActors));
        if (!compareActor(mySemActors, oSemActors)) {
            return false;
        }
        return true;
    }

    public static ArrayList<SemRelation> getMySemRelations(SemObject event, ArrayList<SemRelation> semRelations) {
        ArrayList<SemRelation> mySemRelations = new ArrayList<SemRelation>();
        for (int i = 0; i < semRelations.size(); i++) {
            SemRelation semRelation = semRelations.get(i);
            if (semRelation.getSubject().equals(event.getId())) {
                boolean has = false;
                for (int j = 0; j < mySemRelations.size(); j++) {
                    SemRelation mySemRelation = mySemRelations.get(j);
                    if (mySemRelation.getObject().equals(semRelation.getObject())) {
                        has = true;
                        break;
                    }
                }
                if (!has) {
                    mySemRelations.add(semRelation);
                }
            }
        }
        return mySemRelations;
    }

    public static ArrayList<SemObject> getMySemObjects(SemObject event, ArrayList<SemRelation> semRelations, ArrayList<SemObject> semObjects) {
        ArrayList<SemObject> mySemObjects = new ArrayList<SemObject>();

        for (int i = 0; i < semRelations.size(); i++) {
            SemRelation semRelation = semRelations.get(i);
            if (semRelation.getSubject().equals(event.getId())) {
                //// this SemRelation applies to our event
                /// we get the objects of the relation and add them to the list
                for (int j = 0; j < semObjects.size(); j++) {
                    SemObject semObject = semObjects.get(j);

                    if (semObject != null && semRelation.getObject().equals(semObject.getId())) {
                        boolean has = false;
                        for (int k = 0; k < mySemObjects.size(); k++) {
                            SemObject object = mySemObjects.get(k);
                            if (object.getId().equals(semObject.getId())) {
                                has = true;
                                break;
                            }
                        }
                        if (!has) {
                            mySemObjects.add(semObject);
                        }
                    }
                }
            }
        }
        return mySemObjects;
    }

    public static ArrayList<SemActor> getMySemActors(SemObject event, ArrayList<SemRelation> semRelations, ArrayList<SemObject> semObjects) {
        ArrayList<SemActor> mySemObjects = new ArrayList<SemActor>();

        for (int i = 0; i < semRelations.size(); i++) {
            SemRelation semRelation = semRelations.get(i);
            if (semRelation.getSubject().equals(event.getId())) {
                //// this SemRelation applies to our event
                /// we get the objects of the relation and add them to the list
                for (int j = 0; j < semObjects.size(); j++) {
                    SemObject semObject = semObjects.get(j);

                    if (semObject != null && semRelation.getObject().equals(semObject.getId())) {
                        boolean has = false;
                        for (int k = 0; k < mySemObjects.size(); k++) {
                            SemObject object = mySemObjects.get(k);
                            if (object.getId().equals(semObject.getId())) {
                                has = true;
                                break;
                            }
                        }
                        if (!has) {
                            mySemObjects.add((SemActor) semObject);
                        }
                    }
                }
            }
        }
        return mySemObjects;
    }

    public static ArrayList<SemTime> getMySemTimes(SemObject event, ArrayList<SemRelation> semRelations, ArrayList<SemTime> semObjects) {
        ArrayList<SemTime> mySemObjects = new ArrayList<SemTime>();
        if (semObjects != null) {
            for (int i = 0; i < semRelations.size(); i++) {
                SemRelation semRelation = semRelations.get(i);
                if (semRelation.getSubject().equals(event.getId())) {
                    //// this SemRelation applies to our event
                    /// we get the objects of the relation and add them to the list
                    for (int j = 0; j < semObjects.size(); j++) {
                        SemTime semObject = semObjects.get(j);

                        if (semObject != null & semRelation.getObject() != null && semRelation.getObject().equals(semObject.getId())) {
                            boolean has = false;
                            for (int k = 0; k < mySemObjects.size(); k++) {
                                SemObject object = mySemObjects.get(k);
                                if (object.getId().equals(semObject.getId())) {
                                    has = true;
                                    break;
                                }
                            }
                            if (!has) {
                                mySemObjects.add(semObject);
                            }
                        }
                    }
                }
            }
        }
        return mySemObjects;
    }

    static public boolean hasSemTime(ArrayList<SemTime> times, SemTime myTime) {
        for (int i = 0; i < times.size(); i++) {
            SemTime semTime = times.get(i);
            if (!semTime.getOwlTime().getDateLabel().isEmpty()) {
                if (semTime.getOwlTime().getDateLabel().equals(myTime.getOwlTime().getDateLabel())) {
                   return true;
                }
            }
            if (!semTime.getOwlTimeBegin().getDateLabel().isEmpty()) {
                boolean begin = false;
                boolean end = false;
                if (semTime.getOwlTimeBegin().getDateLabel().equals(myTime.getOwlTimeBegin().getDateLabel())) {
                   begin = true;
                }
                if (semTime.getOwlTimeEnd().getDateLabel().equals(myTime.getOwlTimeEnd().getDateLabel())) {
                   end =  true;
                }
                if (begin && end) {
                    return true;
                }
            }
        }
        return false;
    }

}