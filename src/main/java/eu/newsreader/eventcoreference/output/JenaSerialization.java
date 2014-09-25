package eu.newsreader.eventcoreference.output;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.newsreader.eventcoreference.naf.ResourcesUri;
import eu.newsreader.eventcoreference.objects.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 9/25/14.
 */
public class JenaSerialization {

    static public void serializeJena (OutputStream stream,
                                      ArrayList<SemObject> semEvents,
                                      ArrayList<SemObject> semActors,
                                      ArrayList<SemObject> semPlaces,
                                      ArrayList<SemObject> semTimes,
                                      ArrayList<SemRelation> semRelations,
                                      ArrayList<SemRelation> factRelations,
                                      SemTime docSemTime,
                                      HashMap<String, SourceMeta> sourceMetaHashMap) {



        // create an empty Model

        Dataset ds = TDBFactory.createDataset();
        Model defaultModel = ds.getDefaultModel();
        ResourcesUri.prefixModel(defaultModel);

        Model provenanceModel = ds.getNamedModel("http://www.newsreader-project.eu/provenance");
        ResourcesUri.prefixModelGaf(provenanceModel);

        Model instanceModel = ds.getNamedModel("http://www.newsreader-project.eu/instances");
        ResourcesUri.prefixModel(instanceModel);

        // System.out.println("EVENTS");
        for (int i = 0; i < semEvents.size(); i++) {
            SemObject semEvent = semEvents.get(i);
            semEvent.addToJenaModel(instanceModel, Sem.Event);
        }

        //  System.out.println("ACTORS");
        for (int i = 0; i < semActors.size(); i++) {
            SemObject semActor = semActors.get(i);
            semActor.addToJenaModel(instanceModel, Sem.Actor);
        }

        //  System.out.println("PLACES");
        for (int i = 0; i < semPlaces.size(); i++) {
            SemObject semPlace = semPlaces.get(i);
            semPlace.addToJenaModel(instanceModel, Sem.Place);
        }
        if (!docSemTime.getPhrase().isEmpty()) {
            docSemTime.addToJenaModelDocTimeInterval(instanceModel);
        }
        else {
            //  System.out.println("empty phrase for docSemTime = " + docSemTime.getId());
        }
        // System.out.println("TIMES");
        for (int i = 0; i < semTimes.size(); i++) {
            SemTime semTime = (SemTime) semTimes.get(i);
            //semTime.addToJenaModel(instanceModel, Sem.Time);
            semTime.addToJenaModelTimeInterval(instanceModel);
        }

        //System.out.println("RELATIONS");
        for (int i = 0; i < semRelations.size(); i++) {
            SemRelation semRelation = semRelations.get(i);
            if (sourceMetaHashMap!=null) {
                semRelation.addToJenaDataSet(ds, provenanceModel, sourceMetaHashMap);

            }
            else {
                semRelation.addToJenaDataSet(ds, provenanceModel);
            }

            ///** Next version adds relations to one single relation graph
            //semRelation.addToJenaDataSet(ds, relationModel, provenanceModel);
        }

        // System.out.println("FACTUALITIES");
        for (int i = 0; i < factRelations.size(); i++) {
            SemRelation semRelation = factRelations.get(i);
            if (sourceMetaHashMap!=null) {
                semRelation.addToJenaDataSet(ds, provenanceModel, sourceMetaHashMap);

            }
            else {
                semRelation.addToJenaDataSet(ds, provenanceModel);
            }
        }

        RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);
        // defaultModel.write(stream);
        //  RDFWriter writer = defaultModel.getWriter();
        // writer.
        //writer.setErrorHandler(myErrorHandler);
/*        writer.setProperty("showXmlDeclaration","true");
        writer.setProperty("tab","8");
        writer.setProperty("relativeURIs","same-document,relative");
        for (int i = 0; i < ds..size(); i++) {
            Object o =  ds..get(i);

        }
        writer.write(defaultModel, stream);*/
    }

    static public void addJenaCompositeEvents (
            Dataset ds ,
            Model provenanceModel ,
            Model instanceModel,
            HashMap<String, ArrayList<CompositeEvent>> semEvents,
            HashMap <String, SourceMeta> sourceMetaHashMap) {


        Set keySet = semEvents.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String lemma = (String) keys.next();
            ArrayList<CompositeEvent> compositeEvents = semEvents.get(lemma);
            for (int c = 0; c < compositeEvents.size(); c++) {
                CompositeEvent compositeEvent = compositeEvents.get(c);
                compositeEvent.getEvent().addToJenaModel(instanceModel, Sem.Event);

                //  System.out.println("ACTORS");
                for (int  i = 0; i < compositeEvent.getMySemActors().size(); i++) {
                    SemActor semActor = (SemActor) compositeEvent.getMySemActors().get(i);
                    semActor.addToJenaModel(instanceModel, Sem.Actor);
                }

                //  System.out.println("PLACES");
                for (int i = 0; i < compositeEvent.getMySemPlaces().size(); i++) {
                    SemPlace semPlace = (SemPlace) compositeEvent.getMySemPlaces().get(i);
                    semPlace.addToJenaModel(instanceModel, Sem.Place);
                }

                if (compositeEvent.getMyDocTimes().size()>0) {
                    for (int i = 0; i < compositeEvent.getMyDocTimes().size(); i++) {
                        SemTime semTime = compositeEvent.getMyDocTimes().get(i);
                        semTime.addToJenaModelDocTimeInterval(instanceModel);
                    }
                }
                // System.out.println("TIMES");
                // System.out.println("compositeEvent.getMySemTimes().size() = " + compositeEvent.getMySemTimes().size());
                for (int i = 0; i < compositeEvent.getMySemTimes().size(); i++) {
                    SemTime semTime = (SemTime) compositeEvent.getMySemTimes().get(i);
                    //semTime.addToJenaModel(instanceModel, Sem.Time);
                    semTime.addToJenaModelTimeInterval(instanceModel);
                }

                for (int j = 0; j < compositeEvent.getMySemRelations().size(); j++) {
                    SemRelation semRelation = compositeEvent.getMySemRelations().get(j);
                    if (sourceMetaHashMap!=null) {
                        semRelation.addToJenaDataSet(ds, provenanceModel, sourceMetaHashMap);

                    }
                    else {
                        semRelation.addToJenaDataSet(ds, provenanceModel);
                    }
                }
                for (int j = 0; j < compositeEvent.getMySemFactRelations().size(); j++) {
                    SemRelation semRelation = compositeEvent.getMySemFactRelations().get(j);
                    if (sourceMetaHashMap!=null) {
                        semRelation.addToJenaDataSet(ds, provenanceModel, sourceMetaHashMap);

                    }
                    else {
                        semRelation.addToJenaDataSet(ds, provenanceModel);
                    }
                }
            }
        }


    }

    static public void serializeJenaCompositeEvents (OutputStream stream,HashMap<String, ArrayList<CompositeEvent>> semEvents,
                                                     HashMap <String, SourceMeta> sourceMetaHashMap) {



        // create an empty Model

        Dataset ds = TDBFactory.createDataset();
        Model defaultModel = ds.getDefaultModel();
        ResourcesUri.prefixModel(defaultModel);

        Model provenanceModel = ds.getNamedModel("http://www.newsreader-project.eu/provenance");
        ResourcesUri.prefixModelGaf(provenanceModel);

        Model instanceModel = ds.getNamedModel("http://www.newsreader-project.eu/instances");
        ResourcesUri.prefixModel(instanceModel);
        //ResourcesUri.prefixModelNwr(instanceModel);

        addJenaCompositeEvents(ds, instanceModel, provenanceModel, semEvents, sourceMetaHashMap);

        RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);


    }


    static public void serializeJenaEntities (OutputStream stream,
                                              ArrayList<SemObject> semActors,
                                              ArrayList<SemObject> semPlaces,
                                              ArrayList<SemObject> semTimes) {



        // create an empty Model

        Dataset ds = TDBFactory.createDataset();
        Model defaultModel = ds.getDefaultModel();
        ResourcesUri.prefixModel(defaultModel);

        Model instanceModel = ds.getNamedModel("http://www.newsreader-project.eu/instances");
        ResourcesUri.prefixModel(instanceModel);

        addJenaEntities(instanceModel, semActors, semPlaces, semTimes);

        RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);
    }


    static public void addJenaEntities (Model instanceModel,
                                            ArrayList<SemObject> semActors,
                                            ArrayList<SemObject> semPlaces,
                                            ArrayList<SemObject> semTimes) {
        for (int i = 0; i < semActors.size(); i++) {
            SemObject semActor = semActors.get(i);
            semActor.addToJenaModel(instanceModel, Sem.Actor);
        }

        for (int i = 0; i < semPlaces.size(); i++) {
            SemObject semPlace = semPlaces.get(i);
            semPlace.addToJenaModel(instanceModel, Sem.Place);
        }

        for (int i = 0; i < semTimes.size(); i++) {
            SemTime semTime = (SemTime) semTimes.get(i);
            //semTime.addToJenaModel(instanceModel, Sem.Time);
            semTime.addToJenaModelTimeInterval(instanceModel);
        }

    }
}
