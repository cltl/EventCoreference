package eu.newsreader.eventcoreference.objects;

import eu.newsreader.eventcoreference.coref.ComponentMatch;
import eu.newsreader.eventcoreference.util.Util;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by piek on 4/23/14.
 */
public class CompositeEvent implements Serializable{

    private SemObject event;
    private ArrayList<SemTime> myDocTimes;
    private ArrayList<SemTime> mySemTimes;
    private ArrayList<SemPlace> mySemPlaces;
    private ArrayList<SemActor> mySemActors;
    private ArrayList<SemRelation> mySemRelations;
    private ArrayList<SemRelation> mySemFactRelations;

    public CompositeEvent() {
        this.event = new SemObject();
        this.myDocTimes = new ArrayList<SemTime>();
        this.mySemTimes = new ArrayList<SemTime>();
        this.mySemPlaces = new ArrayList<SemPlace>();
        this.mySemActors = new ArrayList<SemActor>();
        this.mySemRelations = new ArrayList<SemRelation>();
        this.mySemFactRelations = new ArrayList<SemRelation>();
    }

    public CompositeEvent(SemEvent event,
                          SemTime semTime,
                          ArrayList<SemActor> mySemActors,
                          ArrayList<SemPlace> mySemPlaces,
                          ArrayList<SemTime> mySemTimes,
                          ArrayList<SemRelation> mySemRelations,
                          ArrayList<SemRelation> mySemFactRelations
                          ) {
        this.myDocTimes = new ArrayList<SemTime>();
        this.myDocTimes.add(semTime);
        this.event = event;
        this.mySemTimes = mySemTimes;
        this.mySemPlaces = mySemPlaces;
        this.mySemActors = mySemActors;
        this.mySemRelations = mySemRelations;
        this.mySemFactRelations = mySemFactRelations;
    }


    public SemObject getEvent() {
        return event;
    }

    public void setEvent(SemObject event) {
        this.event = event;
    }

    public ArrayList<SemTime> getMySemTimes() {
        return mySemTimes;
    }

    public void setMySemTimes(ArrayList<SemTime> mySemTimes) {
        this.mySemTimes = mySemTimes;
    }

    public void addMySemTime(SemTime mySemTime) {
        this.mySemTimes.add(mySemTime);
    }

    public void setMyDocTimes(ArrayList<SemTime> mySemTimes) {
        this.myDocTimes = mySemTimes;
    }

    public void addMyDocTime(SemTime mySemTime) {
        this.myDocTimes.add(mySemTime);
    }


    public ArrayList<SemTime> getMyDocTimes() {
        return myDocTimes;
    }

    public ArrayList<SemPlace> getMySemPlaces() {
        return mySemPlaces;
    }

    public void setMySemPlaces(ArrayList<SemPlace> mySemPlaces) {
        this.mySemPlaces = mySemPlaces;
    }

    public void addMySemPlace(SemPlace mySemPlace) {
        this.mySemPlaces.add(mySemPlace);
    }

    public ArrayList<SemActor> getMySemActors() {
        return mySemActors;
    }

    public void setMySemActors(ArrayList<SemActor> mySemActors) {
        this.mySemActors = mySemActors;
    }

    public void addMySemActor(SemActor mySemActor) {
        this.mySemActors.add(mySemActor);
    }

    public ArrayList<SemRelation> getMySemRelations() {
        return mySemRelations;
    }

    public void setMySemRelations(ArrayList<SemRelation> mySemRelations) {
        this.mySemRelations = mySemRelations;
    }

    public void addMySemRelation(SemRelation mySemRelation) {
        this.mySemRelations.add(mySemRelation);
    }

    public ArrayList<SemRelation> getMySemFactRelations() {
        return mySemFactRelations;
    }

    public void setMySemFactRelations(ArrayList<SemRelation> mySemFactRelations) {
        this.mySemFactRelations = mySemFactRelations;
    }

    public void addMySemFactRelation(SemRelation mySemFactRelation) {
        this.mySemFactRelations.add(mySemFactRelation);
    }

    /*
    @TODO fix true time value matches
     */
    public void mergeRelations (CompositeEvent event) {
         for (int i = 0; i < event.getMySemRelations().size(); i++) {
            SemRelation semRelation = event.getMySemRelations().get(i);
            boolean match = false;
            for (int j = 0; j < this.getMySemRelations().size(); j++) {
                SemRelation relation = this.getMySemRelations().get(j);
                if ((relation.getPredicate().equalsIgnoreCase("hasTime")) && (semRelation.getPredicate().equalsIgnoreCase("hasTime")))  {
                    //// make sure the doctime is also considered
                    ArrayList<SemTime> times1 = this.getMySemTimes();
                    for (int k = 0; k < this.getMyDocTimes().size(); k++) {
                        SemTime docTime =  this.getMyDocTimes().get(k);
                        times1.add(docTime);
                    }
                    ArrayList<SemTime> times2 = event.getMySemTimes();
                    for (int k = 0; k < event.getMyDocTimes().size(); k++) {
                        SemTime docTime = event.getMyDocTimes().get(k);
                        times2.add(docTime);
                    }
                    if (Util.matchTimeReference(times1, times2, relation.getObject(), semRelation.getObject())) {
                        relation.addMentions(semRelation.getNafMentions());
                       // System.out.println("relation.getNafMentions().toString() = " + relation.getNafMentions().toString());
                        match = true;
                        break;
                    }
                    else {
                        /////
                    }
                }
                else if (ComponentMatch.equalSemRelation(semRelation, relation)) {
                    /// we already have this relation so we add the mentions
                   // System.out.println("relation.getNafMentions().size() = " + relation.getNafMentions().size());
                    relation.addMentions(semRelation.getNafMentions());
                   // System.out.println("relation.getNafMentions().size() = " + relation.getNafMentions().size());
                    match = true;
                    break;
                }
            }
            if (!match) {
               // System.out.println("adding semRelation = " + semRelation.toString());
                semRelation.setSubject(this.getEvent().getId());
               // System.out.println("new semRelation = " + semRelation.toString());
                this.addMySemRelation(semRelation);
            }
        }
    }



    public void mergeFactRelations (CompositeEvent event) {
         for (int i = 0; i < event.getMySemFactRelations().size(); i++) {
            SemRelation semRelation = event.getMySemFactRelations().get(i);
            boolean match = false;
            for (int j = 0; j < this.getMySemFactRelations().size(); j++) {
                SemRelation relation = this.getMySemFactRelations().get(j);
                if (ComponentMatch.equalSemRelation(semRelation, relation)) {
                    relation.addMentions(semRelation.getNafMentions());
                    match = true;
                    break;
                }
            }
            if (!match) {
              //  System.out.println("semRelation = " + semRelation.toString());
                semRelation.setSubject(event.getEvent().getId());
                this.addMySemFactRelation(semRelation);
            }
        }
    }

    public void mergeObjects (CompositeEvent event) {
         for (int i = 0; i < event.getMySemActors().size(); i++) {
            SemActor semActor1 = event.getMySemActors().get(i);
            boolean match = false;
            for (int j = 0; j < this.getMySemActors().size(); j++) {
                SemActor semActor2 = this.getMySemActors().get(j);
                if (semActor1.getURI().equals(semActor2.getURI())) {
                  //  System.out.println("adding semActor1 = " + semActor1.getURI());
                  //  System.out.println("adding semActor2 = " + semActor2.getURI());
                    semActor2.mergeSemObject(semActor1);
                    match = true;
                    break;
                }
            }
            if (!match) {
               //  System.out.println("adding semActor1 = " + semActor1.getURI());
                 this.mySemActors.add(semActor1);
            }
        }

        for (int i = 0; i < event.getMySemPlaces().size(); i++) {
            SemPlace semPlace1 = event.getMySemPlaces().get(i);
            boolean match = false;
            for (int j = 0; j < this.getMySemPlaces().size(); j++) {
                SemPlace semPlace2 = this.getMySemPlaces().get(j);
                if (semPlace1.getURI().equals(semPlace2.getURI())) {
                    semPlace2.mergeSemObject(semPlace1);

                    match = true;
                    break;
                }
            }
            if (!match) {
               //  System.out.println("adding semPlace1 = " + semPlace1.toString());
                 this.mySemPlaces.add(semPlace1);
            }
        }
        for (int i = 0; i < event.getMySemTimes().size(); i++) {
            SemTime semTime1 = event.getMySemTimes().get(i);
            boolean match = false;
            for (int j = 0; j < this.getMySemTimes().size(); j++) {
                SemTime semTime2 = this.getMySemTimes().get(j);
                if (semTime1.getOwlTime().matchTimeExact(semTime2.getOwlTime())) {
                 //   System.out.println("semTime1 = " + semTime1.getURI());
                 //   System.out.println("semTime2 = " + semTime2.getURI());
                    semTime2.mergeSemObject(semTime1);
                    match = true;
                    break;
                }
            }
            if (!match) {
              //   System.out.println("adding semTime1 = " + semTime1.getURI());
                 this.mySemTimes.add(semTime1);
            }
        }
        for (int i = 0; i < event.getMyDocTimes().size(); i++) {
            SemTime semTime1 = event.getMyDocTimes().get(i);
            boolean match = false;
            for (int j = 0; j < this.getMyDocTimes().size(); j++) {
                SemTime semTime2 = this.getMyDocTimes().get(j);
                if (semTime1.getOwlTime().matchTimeExact(semTime2.getOwlTime())) {
                //    System.out.println("semTime1 = " + semTime1.getURI());
                //    System.out.println("semTime2 = " + semTime2.getURI());
                    semTime2.mergeSemObject(semTime1);
                    match = true;
                    break;
                }
            }
            if (!match) {
              //   System.out.println("adding semTime1 = " + semTime1.getURI());
                 this.getMyDocTimes().add(semTime1);
            }
        }
       // System.out.println("this.getMySemTimes().size() = " + this.getMySemTimes().size());

    }

}
