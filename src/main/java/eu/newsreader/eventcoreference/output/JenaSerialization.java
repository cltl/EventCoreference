package eu.newsreader.eventcoreference.output;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.kyotoproject.kaf.KafSense;
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

    static Dataset ds = null;
    static Model provenanceModel = null;
    static Model instanceModel = null;

    static public void createModels () {
        ds = TDBFactory.createDataset();
        provenanceModel = ds.getNamedModel(ResourcesUri.nwr+"provenance");
        instanceModel = ds.getNamedModel(ResourcesUri.nwr+"instances");
        prefixModels ();
    }

    static void prefixModels () {
        Model defaultModel = ds.getDefaultModel();
        ResourcesUri.prefixModel(defaultModel);
        ResourcesUri.prefixModelNwr(defaultModel);
        ResourcesUri.prefixModelGaf(defaultModel);

        ResourcesUri.prefixModelGaf(provenanceModel);

        ResourcesUri.prefixModel(instanceModel);
        ResourcesUri.prefixModelNwr(instanceModel);
        ResourcesUri.prefixModelGaf(instanceModel);

    }

    static public void serializeJena (OutputStream stream,
                                      ArrayList<SemObject> semEvents,
                                      ArrayList<SemObject> semActors,
                                      ArrayList<SemTime> semTimes,
                                      ArrayList<SemRelation> semRelations,
                                      HashMap<String, SourceMeta> sourceMetaHashMap,
                                      boolean ILIURI,
                                      boolean VERBOSE_MENTIONS) {



        // create an empty Model
        createModels();

        // System.out.println("EVENTS");
        for (int i = 0; i < semEvents.size(); i++) {
            SemObject semEvent = semEvents.get(i);
            //semEvent.addToJenaModel(instanceModel, Sem.Event);
            semEvent.addToJenaModel(instanceModel, Sem.Event, VERBOSE_MENTIONS);
        }

        //  System.out.println("ACTORS");
        for (int i = 0; i < semActors.size(); i++) {
            SemObject semActor = semActors.get(i);
           // semActor.addToJenaModel(instanceModel, Sem.Actor);
            semActor.addToJenaModel(instanceModel, Sem.Actor, VERBOSE_MENTIONS);
        }

        // System.out.println("TIMES");
        for (int i = 0; i < semTimes.size(); i++) {
            SemTime semTime = (SemTime) semTimes.get(i);
            if (semTime.getType().equalsIgnoreCase(TimeTypes.YEAR)) {
                semTime.addToJenaModelDocTimeInstant(instanceModel);
                //OR
                // semTime.addToJenaModelTimeIntervalCondensed(instanceModel);
            }
            else if (semTime.getType().equalsIgnoreCase(TimeTypes.QUARTER)) {
                semTime.addToJenaModelTimeIntervalCondensed(instanceModel);
            }
            else if (semTime.getType().equalsIgnoreCase(TimeTypes.MONTH)) {
                semTime.addToJenaModelDocTimeInstant(instanceModel);
                //OR
                // semTime.addToJenaModelTimeIntervalCondensed(instanceModel);
            }
            else if (semTime.getType().equalsIgnoreCase(TimeTypes.DURATION)) {
                semTime.addToJenaModelTimeIntervalCondensed(instanceModel);
            }
            else  { /// DATE
                semTime.addToJenaModelDocTimeInstant(instanceModel);
            }
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


        RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);
       // RDFDataMgr.write(stream, ds, RDFFormat.RDFJSON);
       // RDFWriter writer = ds.getDefaultModel().getWriter();
       // writer.write(ds.getDefaultModel(), stream, RDFFormat.RDFJSON);
       // writer.write(ds.getDefaultModel(), );
        // defaultModel.write(stream);

    }

    static void replaceEventIdsWithILIids (CompositeEvent compositeEvent) {
        String IliUri = "";
        for (int i = 0; i < compositeEvent.getEvent().getConcepts().size(); i++) {
            KafSense kafSense = compositeEvent.getEvent().getConcepts().get(i);
            if (kafSense.getResource().equalsIgnoreCase("wordnet")) {
                if (IliUri.indexOf(kafSense.getSensecode())==-1) {
                    if (!IliUri.isEmpty()) {
                            IliUri += "-and-";
                    }
                    IliUri += kafSense.getSensecode();
                }
            }
        }
       // System.out.println("IliUri = " + IliUri);
        if (!IliUri.isEmpty()) {
            IliUri = ResourcesUri.ili+IliUri;
            compositeEvent.getEvent().setId(IliUri);
            compositeEvent.getEvent().setUri(IliUri);
            for (int j = 0; j < compositeEvent.getMySemRelations().size(); j++) {
                SemRelation semRelation = compositeEvent.getMySemRelations().get(j);
                semRelation.setSubject(IliUri);
            }
        }
      //  System.out.println("compositeEvent = " + compositeEvent.getEvent().getId());
      //  System.out.println("compositeEvent = " + compositeEvent.getEvent().getURI());
    }

    static public void addJenaPerspectiveObjects(Dataset ds,
                                            ArrayList<PerspectiveObject> perspectiveObjects) {
        for (int i = 0; i < perspectiveObjects.size(); i++) {
            PerspectiveObject perspectiveObject = perspectiveObjects.get(i);
           // System.out.println("perspectiveObject.toString() = " + perspectiveObject.toString());
            perspectiveObject.addToJenaDataSet(ds);
        }
    }




    static public void addJenaCompositeEvent (
            CompositeEvent compositeEvent,
            HashMap <String, SourceMeta> sourceMetaHashMap,
            boolean ILIURI,
            boolean VERBOSE_MENTIONS) {

            // System.out.println("compositeEvent.toString() = " + compositeEvent.toString());
            if (ILIURI) {
                replaceEventIdsWithILIids(compositeEvent);
            }

            compositeEvent.getEvent().addToJenaModel(instanceModel, Sem.Event, VERBOSE_MENTIONS);

            //  System.out.println("ACTORS");
            for (int  i = 0; i < compositeEvent.getMySemActors().size(); i++) {
                SemActor semActor = (SemActor) compositeEvent.getMySemActors().get(i);
                semActor.addToJenaModel(instanceModel, Sem.Actor, VERBOSE_MENTIONS);
            }


            // System.out.println("TIMES");
            // System.out.println("compositeEvent.getMySemTimes().size() = " + compositeEvent.getMySemTimes().size());
            for (int i = 0; i < compositeEvent.getMySemTimes().size(); i++) {
                SemTime semTime = (SemTime) compositeEvent.getMySemTimes().get(i);
                //semTime.addToJenaModelTimeInterval(instanceModel);
                if (semTime.getType().equalsIgnoreCase(TimeTypes.YEAR)) {
                    semTime.addToJenaModelDocTimeInstant(instanceModel);
                    //OR
                    // semTime.addToJenaModelTimeIntervalCondensed(instanceModel);
                }
                else if (semTime.getType().equalsIgnoreCase(TimeTypes.QUARTER)) {
                    semTime.addToJenaModelTimeIntervalCondensed(instanceModel);
                }
                else if (semTime.getType().equalsIgnoreCase(TimeTypes.MONTH)) {
                    semTime.addToJenaModelDocTimeInstant(instanceModel);
                    //OR
                    // semTime.addToJenaModelTimeIntervalCondensed(instanceModel);
                }
                else if (semTime.getType().equalsIgnoreCase(TimeTypes.DURATION)) {
                    semTime.addToJenaModelTimeIntervalCondensed(instanceModel);
                }
                else  { /// DATE
                    semTime.addToJenaModelDocTimeInstant(instanceModel);
                }
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
               /* for (int j = 0; j < compositeEvent.getMySemFactRelations().size(); j++) {
                    SemRelation semRelation = compositeEvent.getMySemFactRelations().get(j);
                    if (sourceMetaHashMap!=null) {
                        semRelation.addToJenaDataSet(ds, provenanceModel, sourceMetaHashMap);

                    }
                    else {
                        semRelation.addToJenaDataSet(ds, provenanceModel);
                    }
                }*/
    }

    static public void addJenaCompositeEvents (
            ArrayList<CompositeEvent> compositeEvents,
            HashMap <String, SourceMeta> sourceMetaHashMap,
            boolean ILIURI,
            boolean VERBOSE_MENTIONS) {
        for (int c = 0; c < compositeEvents.size(); c++) {
            CompositeEvent compositeEvent = compositeEvents.get(c);
            addJenaCompositeEvent(compositeEvent, sourceMetaHashMap, ILIURI, VERBOSE_MENTIONS);
        }
    }

    static public void addJenaCompositeEvents (
            HashMap<String, ArrayList<CompositeEvent>> semEvents,
            HashMap <String, SourceMeta> sourceMetaHashMap,
            boolean ILIURI,
            boolean VERBOSE_MENTIONS) {


        Set keySet = semEvents.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String lemma = (String) keys.next();
            ArrayList<CompositeEvent> compositeEvents = semEvents.get(lemma);
            addJenaCompositeEvents(compositeEvents, sourceMetaHashMap, ILIURI, VERBOSE_MENTIONS);
        }


    }

    static public void serializeJenaCompositeEvents (OutputStream stream,
                                                     HashMap<String, ArrayList<CompositeEvent>> semEvents,
                                                     HashMap <String, SourceMeta> sourceMetaHashMap,
                                                     boolean ILIURI,
                                                     boolean VERBOSE_MENTIONS) {



        createModels();
        addJenaCompositeEvents(semEvents, sourceMetaHashMap, ILIURI,VERBOSE_MENTIONS);
        RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);
    }

    static public void serializeJenaSingleCompositeEvents (OutputStream stream,
                                                     HashMap<String, CompositeEvent> semEvents,
                                                     HashMap <String, SourceMeta> sourceMetaHashMap,
                                                           boolean ILIURI,
                                                           boolean VERBOSE_MENTIONS) {



        createModels();
        Set keySet = semEvents.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            CompositeEvent semEvent = semEvents.get(key);
            if (semEvent!=null) {
                addJenaCompositeEvent(semEvent, sourceMetaHashMap, ILIURI, VERBOSE_MENTIONS);
            }
        }
        RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);
    }

    static public void serializeJenaCompositeEvents (OutputStream stream,
                                                     ArrayList<CompositeEvent> semEvents,
                                                     HashMap <String, SourceMeta> sourceMetaHashMap,
                                                     boolean ILIURI,
                                                     boolean VERBOSE_MENTIONS) {




        createModels();

        addJenaCompositeEvents(semEvents, sourceMetaHashMap, ILIURI, VERBOSE_MENTIONS);

        RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);


    }





}
