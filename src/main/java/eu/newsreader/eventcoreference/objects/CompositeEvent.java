package eu.newsreader.eventcoreference.objects;

import eu.newsreader.eventcoreference.coref.ComponentMatch;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by piek on 4/23/14.
 */
public class CompositeEvent implements Serializable{

    private SemObject event;
    private ArrayList<SemObject> mySemTimes;
    private ArrayList<SemObject> mySemPlaces;
    private ArrayList<SemObject> mySemActors;
    private ArrayList<SemRelation> mySemRelations;
    private ArrayList<SemRelation> mySemFactRelations;

    public CompositeEvent() {
        this.event = new SemObject();
        this.mySemTimes = new ArrayList<SemObject>();
        this.mySemPlaces = new ArrayList<SemObject>();
        this.mySemActors = new ArrayList<SemObject>();
        this.mySemRelations = new ArrayList<SemRelation>();
        this.mySemFactRelations = new ArrayList<SemRelation>();
    }

    public CompositeEvent(SemObject event,
                          ArrayList<SemObject> mySemTimes,
                          ArrayList<SemObject> mySemActors,
                          ArrayList<SemObject> mySemPlaces,
                          ArrayList<SemRelation> mySemRelations,
                          ArrayList<SemRelation> mySemFactRelations
                          ) {
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

    public ArrayList<SemObject> getMySemTimes() {
        return mySemTimes;
    }

    public void setMySemTimes(ArrayList<SemObject> mySemTimes) {
        this.mySemTimes = mySemTimes;
    }

    public void addMySemTime(SemObject mySemTime) {
        this.mySemTimes.add(mySemTime);
    }

    public ArrayList<SemObject> getMySemPlaces() {
        return mySemPlaces;
    }

    public void setMySemPlaces(ArrayList<SemObject> mySemPlaces) {
        this.mySemPlaces = mySemPlaces;
    }

    public void addMySemPlace(SemObject mySemPlace) {
        this.mySemPlaces.add(mySemPlace);
    }

    public ArrayList<SemObject> getMySemActors() {
        return mySemActors;
    }

    public void setMySemActors(ArrayList<SemObject> mySemActors) {
        this.mySemActors = mySemActors;
    }

    public void addMySemActor(SemObject mySemActor) {
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

    public void mergeRelations (CompositeEvent event) {
         for (int i = 0; i < event.getMySemRelations().size(); i++) {
            SemRelation semRelation = event.getMySemRelations().get(i);
            boolean match = false;
            for (int j = 0; j < this.getMySemRelations().size(); j++) {
                SemRelation relation = this.getMySemRelations().get(j);
                if (ComponentMatch.compareSemRelation(semRelation, relation)) {
                    relation.addMentions(semRelation.getNafMentions());
                    match = true;
                    break;
                }
            }
            if (!match) {
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
                if (ComponentMatch.compareSemRelation(semRelation, relation)) {
                    relation.addMentions(semRelation.getNafMentions());
                    match = true;
                    break;
                }
            }
            if (!match) {
                this.addMySemFactRelation(semRelation);
            }
        }
    }

}
