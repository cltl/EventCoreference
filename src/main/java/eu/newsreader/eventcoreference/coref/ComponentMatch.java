package eu.newsreader.eventcoreference.coref;

import eu.newsreader.eventcoreference.objects.CompositeEvent;
import eu.newsreader.eventcoreference.objects.OwlTime;
import eu.newsreader.eventcoreference.objects.SemObject;
import eu.newsreader.eventcoreference.objects.SemRelation;

import java.util.ArrayList;

/**
 * Created by piek on 5/2/14.
 */
public class ComponentMatch {

    public static boolean compareTime (ArrayList<SemObject> mySemTimes,
                                       ArrayList<SemObject> semTimes) {

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

    public static boolean comparePlace (ArrayList<SemObject> mySemPlaces,
                                        ArrayList<SemObject> semPlaces) {

        for (int i = 0; i < mySemPlaces.size(); i++) {
            SemObject mySemPlace = mySemPlaces.get(i);
            for (int j = 0; j < semPlaces.size(); j++) {
                SemObject semPlace = semPlaces.get(j);
                if (mySemPlace.getURI().equals(semPlace.getURI())) {
               //     System.out.println("semPlace.getURI() = " + semPlace.getURI());
                    return true;
                }
                else if (semPlace.getReference().equals(semPlace.getReference()) && !semPlace.getReference().isEmpty()) {
               //     System.out.println("semPlace.getReference() = " + semPlace.getReference());
                    return true;
                }
                else if (semPlace.getTopPhraseAsLabel().equals(semPlace.getTopPhraseAsLabel())) {
                    System.out.println("semPlace.getTopPhraseAsLabel() = " + semPlace.getTopPhraseAsLabel());
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean compareActor (ArrayList<SemObject> mySemActors,
                                        ArrayList<SemObject> semActors) {

        for (int i = 0; i < mySemActors.size(); i++) {
            SemObject mySemActor = mySemActors.get(i);
            for (int j = 0; j < semActors.size(); j++) {
                SemObject semActor = semActors.get(j);
                if (semActor.getURI().equals(mySemActor.getURI())) {
                   // System.out.println("semActor.getURI() = " + semActor.getURI());
                    return true;
                }
                else if (semActor.getReference().equals(mySemActor.getReference()) && !semActor.getReference().isEmpty()) {
                  //  System.out.println("semActor.getReference() = " + semActor.getReference());
                    return true;
                }
                else if (semActor.getTopPhraseAsLabel().equals(mySemActor.getTopPhraseAsLabel())) {
                    System.out.println("semActor.getTopPhraseAsLabel() = " + semActor.getTopPhraseAsLabel());
                      return true;
                }
            }
        }
        return false;
    }

    public static boolean compareCompositeEvent (CompositeEvent compositeEvent1, CompositeEvent compositeEvent2) {
        if (! compareActor (compositeEvent1.getMySemActors(), compositeEvent2.getMySemActors())) {
            return  false;
        }
        if (compositeEvent1.getMySemPlaces().size()> 0 && compositeEvent2.getMySemPlaces().size()>0) {
            if (!comparePlace(compositeEvent1.getMySemPlaces(), compositeEvent2.getMySemPlaces())) {
                return false;
            }
        }
        return true;
    }

    public static boolean compareSemRelation (SemRelation semRelation1, SemRelation semRelation2) {
        if (semRelation1.getPredicate().equals(semRelation2.getPredicate())
                &&
                semRelation1.getObject().equals(semRelation2.getObject())) {
            return true;
        } else {
            return false;
        }
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

        ArrayList<SemObject> mySemTimes = getMySemObjects(mySemEvent, mySemRelations, theSemTimes);
        ArrayList<SemObject> oSemTimes = getMySemObjects(semEvent, semRelations, semTimes);
        if (!compareTime(mySemTimes, oSemTimes)) {
            return false;
        }
        ArrayList<SemObject> mySemPlaces = getMySemObjects(mySemEvent, mySemRelations, theSemPlaces);
        ArrayList<SemObject> oSemPlaces = getMySemObjects(semEvent, semRelations, semPlaces);
        if (mySemPlaces.size()>0 && oSemPlaces.size()>0) {
            if (!comparePlace(mySemPlaces, oSemPlaces)) {
                return false;
            }
        }
        ArrayList<SemObject> mySemActors = getMySemObjects(mySemEvent, mySemRelations, theSemActors);
        ArrayList<SemObject> oSemActors = getMySemObjects(semEvent, semRelations, semActors);
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
                mySemRelations.add(semRelation);
            }
        }
        return mySemRelations;
    }

    public static ArrayList<SemObject> getMySemObjects (SemObject event, ArrayList<SemRelation> semRelations, ArrayList<SemObject> semObjects) {
        ArrayList<SemObject> mySemObjects = new ArrayList<SemObject>();
        for (int i = 0; i < semRelations.size(); i++) {
            SemRelation semRelation = semRelations.get(i);
            for (int j = 0; j < semObjects.size(); j++) {
                SemObject semObject = semObjects.get(j);
                if (semRelation.getSubject().equals(event.getId()) &&
                        semRelation.getObject().equals(semObject.getId())) {
/*
                    System.out.println("semRelation = " + semRelation.getSubject());
                    System.out.println("semRelation = " + semRelation.getPredicate());
                    System.out.println("semRelation = " + semRelation.getObject());
*/
                    mySemObjects.add(semObject);
                }
            }
        }
        return mySemObjects;
    }
}
