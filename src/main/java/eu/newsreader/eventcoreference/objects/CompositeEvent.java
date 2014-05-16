package eu.newsreader.eventcoreference.objects;

import eu.newsreader.eventcoreference.coref.ComponentMatch;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by piek on 4/23/14.
 */
public class CompositeEvent implements Serializable{

    private SemObject event;
    private OwlTime docTime;
    private ArrayList<SemTime> mySemTimes;
    private ArrayList<SemPlace> mySemPlaces;
    private ArrayList<SemActor> mySemActors;
    private ArrayList<SemRelation> mySemRelations;
    private ArrayList<SemRelation> mySemFactRelations;

    public CompositeEvent() {
        this.docTime = new OwlTime();
        this.event = new SemObject();
        this.mySemTimes = new ArrayList<SemTime>();
        this.mySemPlaces = new ArrayList<SemPlace>();
        this.mySemActors = new ArrayList<SemActor>();
        this.mySemRelations = new ArrayList<SemRelation>();
        this.mySemFactRelations = new ArrayList<SemRelation>();
    }

    public CompositeEvent(SemEvent event,
                          OwlTime docTime,
                          ArrayList<SemActor> mySemActors,
                          ArrayList<SemPlace> mySemPlaces,
                          ArrayList<SemTime> mySemTimes,
                          ArrayList<SemRelation> mySemRelations,
                          ArrayList<SemRelation> mySemFactRelations
                          ) {
        this.docTime = docTime;
        this.event = event;
        this.mySemTimes = mySemTimes;
        this.mySemPlaces = mySemPlaces;
        this.mySemActors = mySemActors;
        this.mySemRelations = mySemRelations;
        this.mySemFactRelations = mySemFactRelations;
    }

    public OwlTime getDocTime() {
        return docTime;
    }

    public void setDocTime(OwlTime docTime) {
        this.docTime = docTime;
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
                 //   this.getMySemTimes();
                    if (matchTimeReference(this.getMySemTimes(), event.mySemTimes, relation.getObject(), semRelation.getObject())) {
                        //// true time match, represent one
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

    public boolean matchTimeReference (ArrayList<SemTime> times1, ArrayList<SemTime> times2, String time1Id, String time2Id) {
         boolean match = false;
         return match;
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

}
