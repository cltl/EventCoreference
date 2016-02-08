package eu.newsreader.eventcoreference.output;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import eu.newsreader.eventcoreference.naf.ResourcesUri;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.pwn.ILIReader;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 9/25/14.
 */
public class JenaSerialization {

    static Dataset ds = null;
    static Model graspModel = null;
    static Model provenanceModel = null;
    static Model instanceModel = null;
    static public ILIReader iliReader = null;

    static public void initILI (String pathToILI) {
        iliReader = new ILIReader();
        iliReader.readILIFile(pathToILI);
    }

    static public void createModels () {
        ds = TDBFactory.createDataset();
        graspModel = ds.getNamedModel(ResourcesUri.nwr + "grasp");
        provenanceModel = ds.getNamedModel(ResourcesUri.nwr + "provenance");
        instanceModel = ds.getNamedModel(ResourcesUri.nwr+"instances");
        prefixModels();
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

    static public void addJenaPerspectiveObjects(String attrBase, String namespace,
                                            ArrayList<PerspectiveObject> perspectiveObjects) {
        for (int i = 0; i < perspectiveObjects.size(); i++) {
            PerspectiveObject perspectiveObject = perspectiveObjects.get(i);
            String attrId = attrBase+"Attr"+i;
            perspectiveObject.addToJenaDataSet(graspModel, namespace, attrId);
        }
    }

    static public void addDocMetaData(KafSaxParser kafSaxParser) {
        String docId = kafSaxParser.getKafMetaData().getUrl();
        Resource subject = graspModel.createResource(docId);
        Property property = graspModel.createProperty(ResourcesUri.prov, "wasAttributedTo");
        String author = kafSaxParser.getKafMetaData().getAuthor();
        String magazine = kafSaxParser.getKafMetaData().getMagazine();
        String publisher = kafSaxParser.getKafMetaData().getPublisher();
        if (!author.isEmpty()) {
            try {
                author = URLEncoder.encode(author, "UTF-8");
                Resource object = graspModel.createResource(ResourcesUri.nwrauthor+author);
                subject.addProperty(property, object);
            } catch (UnsupportedEncodingException e) {
                //  e.printStackTrace();
            }
        }
        if (!magazine.isEmpty()) {
            try {
                magazine = URLEncoder.encode(magazine, "UTF-8");
                Resource object = graspModel.createResource(ResourcesUri.nwrmagazine+magazine);
                subject.addProperty(property, object);
            } catch (UnsupportedEncodingException e) {
                //  e.printStackTrace();
            }
        }
        if (!publisher.isEmpty()) {
            try {
                publisher = URLEncoder.encode(publisher, "UTF-8");
                Resource object = graspModel.createResource(ResourcesUri.nwrpublisher+publisher);
                subject.addProperty(property, object);
            } catch (UnsupportedEncodingException e) {
                //  e.printStackTrace();
            }
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
        try {
            RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);
        } catch (Exception e) {
          //  e.printStackTrace();
        }
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
        try {
            RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);
        } catch (Exception e) {
           // e.printStackTrace();
        }
    }

    static public void serializeJenaCompositeEvents (OutputStream stream,
                                                     ArrayList<CompositeEvent> semEvents,
                                                     HashMap <String, SourceMeta> sourceMetaHashMap,
                                                     boolean ILIURI,
                                                     boolean VERBOSE_MENTIONS) {




        createModels();

        addJenaCompositeEvents(semEvents, sourceMetaHashMap, ILIURI, VERBOSE_MENTIONS);

        try {
            RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);
        } catch (Exception e) {
          //  e.printStackTrace();
        }


    }

    static public void serializeJenaCompositeEventsAndPerspective (OutputStream stream,
                                                                    ArrayList<CompositeEvent> semEvents,
                                                                   KafSaxParser kafSaxParser,
                                                                   ArrayList<PerspectiveObject> sourcePerspectiveObjects,
                                                                   ArrayList<PerspectiveObject> authorPerspectiveObjects) {




            createModels();
            addJenaCompositeEvents(semEvents, null, false, false);
            addDocMetaData(kafSaxParser);
            String attrBase = kafSaxParser.getKafMetaData().getUrl()+"/"+"source_attribution/";
            addJenaPerspectiveObjects(attrBase, ResourcesUri.gaf, sourcePerspectiveObjects);
            attrBase = kafSaxParser.getKafMetaData().getUrl()+"/"+"doc_attribution/";
            addJenaPerspectiveObjects(attrBase, ResourcesUri.prov, authorPerspectiveObjects);
        try {
            RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);
        } catch (Exception e) {
          //  e.printStackTrace();
        }
    }





}
