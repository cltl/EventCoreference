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
    private ArrayList<SemTime> mySemTimes;
    private ArrayList<SemActor> mySemActors;
    private ArrayList<SemRelation> mySemRelations;

    public CompositeEvent() {
        this.event = new SemObject();
        this.mySemTimes = new ArrayList<SemTime>();
        this.mySemActors = new ArrayList<SemActor>();
        this.mySemRelations = new ArrayList<SemRelation>();
    }



    public CompositeEvent(SemEvent event,
                          ArrayList<SemActor> mySemActors,
                          ArrayList<SemTime> mySemTimes,
                          ArrayList<SemRelation> mySemRelations
                          ) {
        this.event = event;
        this.mySemTimes = mySemTimes;
        this.mySemActors = mySemActors;
        this.mySemRelations = mySemRelations;
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


    /*
    @TODO fix true time value matches
     */
    public void mergeRelations (CompositeEvent event) {
         for (int i = 0; i < event.getMySemRelations().size(); i++) {
            SemRelation semRelation = event.getMySemRelations().get(i);
            boolean match = false;
            for (int j = 0; j < this.getMySemRelations().size(); j++) {
                SemRelation relation = this.getMySemRelations().get(j);
                if (      (relation.containsPredicateIgnoreCase(Sem.hasTime.getLocalName()) && semRelation.containsPredicateIgnoreCase(Sem.hasTime.getLocalName()))
                        ||(relation.containsPredicateIgnoreCase(Sem.hasBeginTimeStamp.getLocalName()) && semRelation.containsPredicateIgnoreCase(Sem.hasBeginTimeStamp.getLocalName()))
                        ||(relation.containsPredicateIgnoreCase(Sem.hasEndTimeStamp.getLocalName()) && semRelation.containsPredicateIgnoreCase(Sem.hasEndTimeStamp.getLocalName()))
                        ||(relation.containsPredicateIgnoreCase(Sem.hasEarliestBeginTimeStamp.getLocalName()) && semRelation.containsPredicateIgnoreCase(Sem.hasEarliestBeginTimeStamp.getLocalName()))
                        ||(relation.containsPredicateIgnoreCase(Sem.hasEarliestEndTimeStamp.getLocalName()) && semRelation.containsPredicateIgnoreCase(Sem.hasEarliestEndTimeStamp.getLocalName()))

                )  {
                    //// make sure the doctime is also considered
                    if (Util.matchTimeReference(this.getMySemTimes(), event.getMySemTimes(), relation.getObject(), semRelation.getObject())) {
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
                    relation.addMentions(semRelation.getNafMentions());
                    match = true;
                    break;
                }
            }
            if (!match) {
                semRelation.setSubject(this.getEvent().getId());
               // System.out.println("new semRelation = " + semRelation.toString());
                this.addMySemRelation(semRelation);
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
                else if (semTime1.getOwlTimeBegin().matchTimeExact(semTime2.getOwlTimeBegin())) {
                 //   System.out.println("semTime1 = " + semTime1.getURI());
                 //   System.out.println("semTime2 = " + semTime2.getURI());
                    semTime2.mergeSemObject(semTime1);
                    match = true;
                    break;
                }
                else if (semTime1.getOwlTimeEnd().matchTimeExact(semTime2.getOwlTimeEnd())) {
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
    }

    public String toString () {
        String str = this.event.getId();
        str += this.event.getPhrase()+"\n";
        for (int i = 0; i < mySemActors.size(); i++) {
            SemActor semActor = mySemActors.get(i);
            str += "\t"+semActor.getId()+"\n";
        }
        for (int i = 0; i < mySemTimes.size(); i++) {
            SemTime semTime = mySemTimes.get(i);
            str += "\t"+semTime.getId()+"\n";
        }
        for (int i = 0; i < mySemRelations.size(); i++) {
            SemRelation semRelation = mySemRelations.get(i);
            str += "\t"+semRelation.getSubject()+":"+semRelation.getPredicates().toString()+":"+semRelation.getObject()+"\n";
        }
        return str;
    }

}
