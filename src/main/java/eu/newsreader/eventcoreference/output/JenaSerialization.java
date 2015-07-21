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

    static public boolean USEILIURI = false; //// used for testing cross-lingual extraction
    static public void serializeJena (OutputStream stream,
                                      ArrayList<SemObject> semEvents,
                                      ArrayList<SemObject> semActors,
                                      ArrayList<SemObject> semTimes,
                                      ArrayList<SemRelation> semRelations,
                                      HashMap<String, SourceMeta> sourceMetaHashMap,
                                      boolean VERBOSE_MENTIONS) {



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
            //semTime.addToJenaModel(instanceModel, Sem.Time);
            semTime.addToJenaModelTimeIntervalCondensed(instanceModel);
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
            IliUri = ResourcesUri.nwrontology+IliUri;
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
            //System.out.println("perspectiveObject.toString() = " + perspectiveObject.toString());
            perspectiveObject.addToJenaDataSet(ds);
        }
    }


    static public void addJenaCompositeEvents (
            Dataset ds ,
            Model instanceModel,
            Model provenanceModel ,
            HashMap<String, ArrayList<CompositeEvent>> semEvents,
            HashMap <String, SourceMeta> sourceMetaHashMap,
            boolean VERBOSE_MENTIONS) {


        Set keySet = semEvents.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String lemma = (String) keys.next();
            ArrayList<CompositeEvent> compositeEvents = semEvents.get(lemma);
            for (int c = 0; c < compositeEvents.size(); c++) {
                CompositeEvent compositeEvent = compositeEvents.get(c);
                if (USEILIURI) {
                    replaceEventIdsWithILIids(compositeEvent);
                }

                //compositeEvent.getEvent().addToJenaModel(instanceModel, Sem.Event);
                compositeEvent.getEvent().addToJenaModel(instanceModel, Sem.Event, VERBOSE_MENTIONS);

                //  System.out.println("ACTORS");
                for (int  i = 0; i < compositeEvent.getMySemActors().size(); i++) {
                    SemActor semActor = (SemActor) compositeEvent.getMySemActors().get(i);
                   // semActor.addToJenaModel(instanceModel, Sem.Actor);
                    semActor.addToJenaModel(instanceModel, Sem.Actor, VERBOSE_MENTIONS);
                }


                // System.out.println("TIMES");
                // System.out.println("compositeEvent.getMySemTimes().size() = " + compositeEvent.getMySemTimes().size());
                for (int i = 0; i < compositeEvent.getMySemTimes().size(); i++) {
                    SemTime semTime = (SemTime) compositeEvent.getMySemTimes().get(i);
                    //semTime.addToJenaModelTimeInterval(instanceModel);
                    if (semTime.getType().equalsIgnoreCase(TimeTypes.YEAR)) {
                        semTime.addToJenaModelTimeIntervalCondensed(instanceModel);
                    }
                    else if (semTime.getType().equalsIgnoreCase(TimeTypes.QUARTER)) {
                        semTime.addToJenaModelTimeIntervalCondensed(instanceModel);
                    }
                    else if (semTime.getType().equalsIgnoreCase(TimeTypes.MONTH)) {
                        semTime.addToJenaModelTimeIntervalCondensed(instanceModel);
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
        }


    }

    static public void serializeJenaCompositeEvents (OutputStream stream,HashMap<String, ArrayList<CompositeEvent>> semEvents,
                                                     HashMap <String, SourceMeta> sourceMetaHashMap, boolean VERBOSE_MENTIONS) {



        // create an empty Model

        Dataset ds = TDBFactory.createDataset();
        Model defaultModel = ds.getDefaultModel();
        ResourcesUri.prefixModel(defaultModel);

        Model provenanceModel = ds.getNamedModel("http://www.newsreader-project.eu/provenance");
        ResourcesUri.prefixModelGaf(provenanceModel);

        Model instanceModel = ds.getNamedModel("http://www.newsreader-project.eu/instances");
        ResourcesUri.prefixModel(instanceModel);
        //ResourcesUri.prefixModelNwr(instanceModel);

        addJenaCompositeEvents(ds, instanceModel, provenanceModel, semEvents, sourceMetaHashMap, VERBOSE_MENTIONS);

        RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);


    }





}
