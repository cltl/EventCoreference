package eu.newsreader.eventcoreference.coref;

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

    public static boolean compareCompositeEvent(CompositeEvent compositeEvent1,
                                                CompositeEvent compositeEvent2,
                                                String eventType,
                                                ArrayList<String> roleArrayList) {
        ArrayList<String> neededRoles = new ArrayList<String>();
        if (EventTypes.isCONTEXTUAL(eventType)) {
            return compareCompositeEvent(compositeEvent1, compositeEvent2, roleArrayList, neededRoles);
        }
        else if (EventTypes.isCOMMUNICATION(eventType)) {
            neededRoles.add("a0");
            return compareCompositeEvent(compositeEvent1, compositeEvent2, roleArrayList, neededRoles);
        }
        else if (EventTypes.isGRAMMATICAL(eventType)) {
            neededRoles.add("a1");
            return compareCompositeEvent(compositeEvent1, compositeEvent2, roleArrayList, neededRoles);
        }
        else if (EventTypes.isFUTURE(eventType)) {
            neededRoles.add("a0");
            neededRoles.add("a1");
            return compareCompositeEvent(compositeEvent1, compositeEvent2, roleArrayList,neededRoles);
        }
        return false;

    }
    /**
     * For each of the roles in the role ArrayList there needs to be match
     * @param compositeEvent1
     * @param compositeEvent2
     * @return
     */
    public static boolean compareCompositeEvent(CompositeEvent compositeEvent1,
                                                CompositeEvent compositeEvent2,
                                                ArrayList<String> roleArrayList, ArrayList<String> neededRoles) {
        int roleMatchCount = 0;
        if (compositeEvent1.getMySemActors().size()==0 && compositeEvent2.getMySemActors().size()==0) {
            return false;
        }
        else {
            for (int i = 0; i < roleArrayList.size(); i++) {
                String propBankRole = roleArrayList.get(i);
                // System.out.println("propBankRole = " + propBankRole);
                ArrayList roleObjects1 = Util.getObjectsForPredicate(compositeEvent1.getMySemRelations(), propBankRole);
                ArrayList roleObjects2 = Util.getObjectsForPredicate(compositeEvent2.getMySemRelations(), propBankRole);
                if (roleObjects1.size()==0 && roleObjects2.size()==0) {
                    /// both events do not have this participant
                    if (!neededRoles.contains(propBankRole) || neededRoles.size()==0) {
                        roleMatchCount++;
                        /// we count this as a match
                    }
                }
                else {
                    if (!Collections.disjoint(roleObjects1, roleObjects2)) {
                        //  System.out.println("roleObjects1 = " + roleObjects1.toString());
                        //  System.out.println("roleObjects2 = " + roleObjects1.toString());
                        roleMatchCount++;
                    } else {
                        //  System.out.println("DISJOINT");
                        //  System.out.println("roleObjects1 = " + roleObjects1.toString());
                        //  System.out.println("roleObjects2 = " + roleObjects1.toString());
                    }
                }

            }
        }
        if (roleMatchCount==roleArrayList.size()) {
            return true;
        }
        else {
            return false;
        }
    }



    /**
     * Compares two time objects to determine if they exclude each other or not
     * @param mySemTimes
     * @param semTimes
     * @return
     */
    public static boolean compareTime (ArrayList<SemTime> mySemTimes,
                                       ArrayList<SemTime> semTimes) {

        for (int i = 0; i < mySemTimes.size(); i++) {
            SemObject mySemTime = mySemTimes.get(i);
            OwlTime myOwlTime = new OwlTime();
            myOwlTime.parseStringDate(mySemTime.getPhrase());
            for (int j = 0; j < semTimes.size(); j++) {
                SemObject semTime = semTimes.get(j);
                OwlTime owlTime = new OwlTime();
                owlTime.parseStringDate(semTime.getPhrase());
                /// replace this by exact time matches....
                if (myOwlTime.matchTimeEmbedded(owlTime)) {
                    //  System.out.println("myOwlTime.getDateString() = " + myOwlTime.getDateString());
                    //  System.out.println("owlTime.getDateString() = " + owlTime.getDateString());

                    return true;
                }
            }

        }
        return false;
    }

    /**
     * Compares two lists of places to determine of they exclude each other. Comparison is based on URIs, external references and the most frequent label found as a mention
     * If there is a single match across the arrays, the function returns true.
     * @param mySemPlaces
     * @param semPlaces
     * @return
     */
    public static boolean comparePlace (ArrayList<SemPlace> mySemPlaces,
                                        ArrayList<SemPlace> semPlaces) {

        for (int i = 0; i < mySemPlaces.size(); i++) {
            SemObject mySemPlace = mySemPlaces.get(i);
            for (int j = 0; j < semPlaces.size(); j++) {
                SemObject semPlace = semPlaces.get(j);
                if (mySemPlace.getURI().equals(semPlace.getURI())&& !semPlace.getURI().isEmpty()) {
               //     System.out.println("semPlace.getURI() = " + semPlace.getURI());
                    return true;
                }
                else if (semPlace.getReference().equals(semPlace.getReference()) && !semPlace.getReference().isEmpty()) {
               //     System.out.println("semPlace.getReference() = " + semPlace.getReference());
                    return true;
                }
                else if (semPlace.getTopPhraseAsLabel().equals(semPlace.getTopPhraseAsLabel())) {
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
     * @param mySemActors
     * @param semActors
     * @return
     */
    public static boolean compareActor (ArrayList<SemActor> mySemActors,
                                        ArrayList<SemActor> semActors) {

        for (int i = 0; i < mySemActors.size(); i++) {
            SemObject mySemActor = mySemActors.get(i);
            for (int j = 0; j < semActors.size(); j++) {
                SemObject semActor = semActors.get(j);
                if (semActor.getURI().equals(mySemActor.getURI()) && !semActor.getURI().isEmpty()) {
                   // System.out.println("semActor.getURI() = " + semActor.getURI());
                    return true;
                }
                else if (semActor.getReference().equals(mySemActor.getReference()) && !semActor.getReference().isEmpty()) {
                  //  System.out.println("semActor.getReference() = " + semActor.getReference());
                    return true;
                }
                else if (semActor.getTopPhraseAsLabel().equals(mySemActor.getTopPhraseAsLabel())) {
                     // System.out.println("semActor.getTopPhraseAsLabel() = " + semActor.getTopPhraseAsLabel());
                      return true;
                }
            }
        }
        return false;
    }


    /**
     * Compares two list of actors by URIs for full equivalence
     * @param mySemActors
     * @param semActors
     * @return
     */
    public static boolean compareAllActorsByURI (ArrayList<SemActor> mySemActors,
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
     * @param mySemActors
     * @param semActors
     * @return
     */
    public static boolean compareAllActorsByReferences (ArrayList<SemActor> mySemActors,
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
     * @param mySemActors
     * @param semActors
     * @return
     */
    public static boolean compareAllActorsByTopPhrase (ArrayList<SemActor> mySemActors,
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
     * @param compositeEvent1
     * @param compositeEvent2
     * @return
     */
    public static boolean compareCompositeEventCommunicationCognition (CompositeEvent compositeEvent1, CompositeEvent compositeEvent2) {
        if (matchingCompositeEventSemActor(compositeEvent1, compositeEvent2, RoleLabels.PRIMEPARTICIPANT)) {
            return true;
        }
        return false;
    }

    /**
     * Grammatical events are more complex expressions and we require that at least two participants match
     * of which one is the prime participant and one is another participant
     * @param compositeEvent1
     * @param compositeEvent2
     * @return
     */
    public static boolean compareCompositeEventGrammatical (CompositeEvent compositeEvent1, CompositeEvent compositeEvent2) {
        if (matchingCompositeEventSemActor(compositeEvent1, compositeEvent2, RoleLabels.PRIMEPARTICIPANT) &&
            matchingCompositeEventSemActor(compositeEvent1, compositeEvent2, RoleLabels.NONPRIMEPARTICIPANT))
                {
            return true;
        }
        return false;
    }


    /**
     * Contextual events need at least 1 participant to match and 1 location
     * @param compositeEvent1
     * @param compositeEvent2
     * @return
     */
    public static boolean compareCompositeEventContextual(CompositeEvent compositeEvent1,
                                                          CompositeEvent compositeEvent2) {
        if (compositeEvent1.getMySemActors().size()==0 && compositeEvent2.getMySemActors().size()==0) {
            return false;
        }
        else {
            /// match at least one actor
            if (! compareActor (compositeEvent1.getMySemActors(), compositeEvent2.getMySemActors())) {
                return  false;
            }
        }
        /// if we get up to here we can assume a match
        return true;
    }

    /**
     * Determines if the objects of two predicates are the same for a set of relations, where the URI of the object should match
     * and the role should match a set of roles. The role of a SemRelation is either the predicate or a set of predicates
     * @param compositeEvent1
     * @param compositeEvent2
     * @param roles
     * @return
     */
    public static boolean matchingCompositeEventSemActor (CompositeEvent compositeEvent1, CompositeEvent compositeEvent2, String[] roles) {
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
     * @param compositeEvent1
     * @param compositeEvent2
     * @param roles
     * @return
     */
    public static boolean matchingCompositeEventSemActorAll (CompositeEvent compositeEvent1, CompositeEvent compositeEvent2, String[] roles) {
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
                        }
                        else {
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
                String pred2 =  semRelation2.getPredicates().get(j);
                if (pred1.equals(pred2)
                        &&
                        semRelation1.getObject().equals(semRelation2.getObject())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean compareComponents (SemObject mySemEvent,
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
        if (mySemPlaces.size()>0 && oSemPlaces.size()>0) {
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

    public static ArrayList<SemRelation> getMySemRelations (SemObject event, ArrayList<SemRelation> semRelations) {
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

    public static ArrayList<SemObject> getMySemObjects (SemObject event, ArrayList<SemRelation> semRelations, ArrayList<SemObject> semObjects) {
        ArrayList<SemObject> mySemObjects = new ArrayList<SemObject>();
        
        for (int i = 0; i < semRelations.size(); i++) {
            SemRelation semRelation = semRelations.get(i);
            if (semRelation.getSubject().equals(event.getId())) {
                //// this SemRelation applies to our event
                /// we get the objects of the relation and add them to the list
                for (int j = 0; j < semObjects.size(); j++) {
                    SemObject semObject = semObjects.get(j);

                    if (semRelation.getObject().equals(semObject.getId())) {
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

    public static ArrayList<SemActor> getMySemActors (SemObject event, ArrayList<SemRelation> semRelations, ArrayList<SemObject> semObjects) {
        ArrayList<SemActor> mySemObjects = new ArrayList<SemActor>();

        for (int i = 0; i < semRelations.size(); i++) {
            SemRelation semRelation = semRelations.get(i);
            if (semRelation.getSubject().equals(event.getId())) {
                //// this SemRelation applies to our event
                /// we get the objects of the relation and add them to the list
                for (int j = 0; j < semObjects.size(); j++) {
                    SemObject semObject = semObjects.get(j);

                    if (semRelation.getObject().equals(semObject.getId())) {
                        boolean has = false;
                        for (int k = 0; k < mySemObjects.size(); k++) {
                            SemObject object = mySemObjects.get(k);
                            if (object.getId().equals(semObject.getId())) {
                                has = true;
                                break;
                            }
                        }
                        if (!has) {
                            mySemObjects.add((SemActor)semObject);
                        }
                    }
                }
            }
        }
        return mySemObjects;
    }

    public static ArrayList<SemTime> getMySemTimes (SemObject event, ArrayList<SemRelation> semRelations, ArrayList<SemTime> semObjects) {
        ArrayList<SemTime> mySemObjects = new ArrayList<SemTime>();

        for (int i = 0; i < semRelations.size(); i++) {
            SemRelation semRelation = semRelations.get(i);
            if (semRelation.getSubject().equals(event.getId())) {
                //// this SemRelation applies to our event
                /// we get the objects of the relation and add them to the list
                for (int j = 0; j < semObjects.size(); j++) {
                    SemTime semObject = semObjects.get(j);

                    if (semRelation.getObject().equals(semObject.getId())) {
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
}
