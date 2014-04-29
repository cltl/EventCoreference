package eu.newsreader.eventcoreference.objects;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by piek on 4/23/14.
 */
public class CompositeEvent {

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
                          ArrayList<SemObject> mySemPlaces,
                          ArrayList<SemObject> mySemActors,
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

    public void serialize (ObjectOutputStream fos) {
        try {
            fos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
