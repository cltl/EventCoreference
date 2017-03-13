package eu.newsreader.eventcoreference.output;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.kyotoproject.kaf.KafFactuality;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import eu.newsreader.eventcoreference.naf.ResourcesUri;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.pwn.ILIReader;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.openrdf.model.vocabulary.RDF;

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

    static public void addJenaPerspectiveObjects(String attrBase, String namespace, String property,
                                            ArrayList<PerspectiveObject> perspectiveObjects, int cnt) {
        HashMap<String, ArrayList<PerspectiveObject>> map = new HashMap<String, ArrayList<PerspectiveObject>>();
        for (int i = 0; i < perspectiveObjects.size(); i++) {
            PerspectiveObject perspectiveObject = perspectiveObjects.get(i);
            String source = perspectiveObject.getSourceEntity().getURI();
            if (map.containsKey(source)) {
                ArrayList<PerspectiveObject> sourcePerspectives = map.get(source);
                sourcePerspectives.add(perspectiveObject);
                map.put(source, sourcePerspectives);
            }
            else {
                ArrayList<PerspectiveObject> sourcePerspectives = new ArrayList<PerspectiveObject>();
                sourcePerspectives.add(perspectiveObject);
                map.put(source, sourcePerspectives);
            }
        }

        Set keySet = map.keySet();
        Iterator<String> keys = keySet.iterator();
        int kCnt = 0;
        while (keys.hasNext()) {
            String key = keys.next();
            kCnt++;
            String attrId = attrBase + "attr"+kCnt+"_" + cnt;
            ArrayList<PerspectiveObject> sourcePerspectives = map.get(key);
            addToJenaDataSet(graspModel, namespace, property, attrId, sourcePerspectives, key);
        }
    }


    static public void addToJenaDataSet (Model model, String ns, String property,
                                         String attrId, ArrayList<PerspectiveObject> perspectives, String sourceURI) {
        /*
        mentionId2      hasAttribution         attributionId1
                        gaf:generatedBy        mentionId3
        attributionId1  rdf:value              CERTAIN_POS_FUTURE
                        rdf:value              POSITIVE
                        prov:wasAttributedTo   doc-uri
                        gaf:wasAttributedTo    dbp:Zetsche

         */
        /*
http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=254,261
	gafAttribution:CERTAIN,NON_FUTURE
		http://dbpedia.org/resource/Caesars_Entertainment_Corporation ;
		gaf:generatedBy http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=201,209.


http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=201,209
			gafAttribution:CERTAIN,NON_FUTURE
				doc-uri;

doc-uri
	prov:wasAttributedTo author;
	prov:wasAttributedTo journal.
         */

        // System.out.println("sourceURI = " + sourceURI);
        // System.out.println("perspectives = " + perspectives.size());
        HashMap<String, ArrayList<NafMention>> mentionMap = new HashMap<String, ArrayList<NafMention>>();
        for (int p = 0; p < perspectives.size(); p++) {
            PerspectiveObject perspectiveObject = perspectives.get(p);
            if ((perspectiveObject.getTargetEventMentions().size()>0)) {
                //// We collect all factualities from all the target mentions for this perspective
                ArrayList<KafFactuality> allFactualities = new ArrayList<KafFactuality>();
                for (int i = 0; i < perspectiveObject.getTargetEventMentions().size(); i++) {
                    NafMention mention = perspectiveObject.getTargetEventMentions().get(i);
                    ////
                    for (int j = 0; j < mention.getFactuality().size(); j++) {
                        KafFactuality kafFactuality = mention.getFactuality().get(j);
                        allFactualities.add(kafFactuality);
                    }
                }
                KafFactuality kafFactuality = NafMention.getDominantFactuality(allFactualities);
                ArrayList<String> factualityStringArray = KafFactuality.castToDefault();
                //System.out.println("factualityStringArray.toString() = " + factualityStringArray.toString());
                if (allFactualities.size() > 0) {
                    factualityStringArray= kafFactuality.getPredictionArrayList();
                    // System.out.println("factualityStringArray.toString() = " + factualityStringArray.toString());
                }
                String sentiment = NafMention.getDominantOpinion(perspectiveObject.getTargetEventMentions());
                if (!sentiment.isEmpty()) {
                    if (sentiment.equals("+")) {
                        factualityStringArray.add("positive");
                    } else if (sentiment.equals("-")) {
                        factualityStringArray.add("negative");
                    }
                }

                if (factualityStringArray.size()>0) {
                    String valueAray = "";
                    for (int i = 0; i < factualityStringArray.size(); i++) {
                        String v = factualityStringArray.get(i);
                        if (!valueAray.isEmpty())  valueAray+=",";
                        valueAray += v;
                    }
                    if (mentionMap.containsKey(valueAray)) {
                        ArrayList<NafMention> mentions = mentionMap.get(valueAray);
                        mentions.addAll(perspectiveObject.getTargetEventMentions());
                        mentionMap.put(valueAray, mentions);
                    }
                    else {
                        ArrayList<NafMention> mentions = perspectiveObject.getTargetEventMentions();
                        mentionMap.put(valueAray, mentions);
                    }
                }
                else {
                    //   System.out.println(" No perspectives for:"+sourceURI);
                }
            }
            else {
                //   System.out.println("no target mentions");
            }
        }
        // System.out.println("mentionMap.size() = " + mentionMap.size());
        if (mentionMap.size()>0) {
            int nAttribution = 0;
            Set keySet = mentionMap.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                nAttribution++;
                Resource sourceResource = model.createResource(sourceURI);
                Property aProperty = model.createProperty(ns, property);
                Resource attributionSubject = model.createResource(attrId+"_"+nAttribution);
                attributionSubject.addProperty(aProperty, sourceResource);
                // System.out.println("key = " + key);
                ArrayList<NafMention> mentions = mentionMap.get(key);
                String[] factValues = key.split(",");
                for (int i = 0; i < factValues.length; i++) {
                    String factValue = factValues[i];
                    Resource factualityResource = model.createResource(ResourcesUri.grasp + factValue);
                    aProperty = model.createProperty(RDF.NAMESPACE, RDF.VALUE.getLocalName());
                    attributionSubject.addProperty(aProperty, factualityResource);
                }
                for (int j = 0; j < mentions.size(); j++) {
                    NafMention nafMention = mentions.get(j);
                    ////
                    Resource mentionObject = model.createResource(nafMention.toString());
                    aProperty = model.createProperty(ResourcesUri.grasp, "isAttributionFor");
                    attributionSubject.addProperty(aProperty, mentionObject);
                }
            }
        }
    }

    static public void addToJenaDataSet1 (Model model, String ns, String property,
                                         String attrId, ArrayList<PerspectiveObject> perspectives, String sourceURI) {
        /*
        mentionId2      hasAttribution         attributionId1
                        gaf:generatedBy        mentionId3
        attributionId1  rdf:value              CERTAIN_POS_FUTURE
                        rdf:value              POSITIVE
                        prov:wasAttributedTo   doc-uri
                        gaf:wasAttributedTo    dbp:Zetsche

         */
        /*
http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=254,261
	gafAttribution:CERTAIN,NON_FUTURE
		http://dbpedia.org/resource/Caesars_Entertainment_Corporation ;
		gaf:generatedBy http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=201,209.


http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=201,209
			gafAttribution:CERTAIN,NON_FUTURE
				doc-uri;

doc-uri
	prov:wasAttributedTo author;
	prov:wasAttributedTo journal.
         */

        // System.out.println("sourceURI = " + sourceURI);
        // System.out.println("perspectives = " + perspectives.size());
        HashMap<String, ArrayList<NafMention>> mentionMap = new HashMap<String, ArrayList<NafMention>>();
        for (int p = 0; p < perspectives.size(); p++) {
            PerspectiveObject perspectiveObject = perspectives.get(p);
            if ((perspectiveObject.getTargetEventMentions().size()>0)) {
                //// We collect all factualities from all the target mentions for this perspective
                ArrayList<KafFactuality> allFactualities = new ArrayList<KafFactuality>();
                for (int i = 0; i < perspectiveObject.getTargetEventMentions().size(); i++) {
                    NafMention mention = perspectiveObject.getTargetEventMentions().get(i);
                    ////
                    for (int j = 0; j < mention.getFactuality().size(); j++) {
                        KafFactuality kafFactuality = mention.getFactuality().get(j);
                        allFactualities.add(kafFactuality);
                    }
                }
                KafFactuality kafFactuality = NafMention.getDominantFactuality(allFactualities);
                ArrayList<String> factualityStringArray = KafFactuality.castToDefault();
                //System.out.println("factualityStringArray.toString() = " + factualityStringArray.toString());
                if (allFactualities.size() > 0) {
                    factualityStringArray= kafFactuality.getPredictionArrayList();
                    // System.out.println("factualityStringArray.toString() = " + factualityStringArray.toString());
                }
                String sentiment = NafMention.getDominantOpinion(perspectiveObject.getTargetEventMentions());
                if (!sentiment.isEmpty()) {
                    if (sentiment.equals("+")) {
                        factualityStringArray.add("positive");
                    } else if (sentiment.equals("-")) {
                        factualityStringArray.add("negative");
                    }
                }

                if (factualityStringArray.size()>0) {
                    String valueAray = "";
                    for (int i = 0; i < factualityStringArray.size(); i++) {
                        String v = factualityStringArray.get(i);
                        if (!valueAray.isEmpty())  valueAray+=",";
                        valueAray += v;
                    }
                    if (mentionMap.containsKey(valueAray)) {
                        ArrayList<NafMention> mentions = mentionMap.get(valueAray);
                        mentions.addAll(perspectiveObject.getTargetEventMentions());
                        mentionMap.put(valueAray, mentions);
                    }
                    else {
                        ArrayList<NafMention> mentions = perspectiveObject.getTargetEventMentions();
                        mentionMap.put(valueAray, mentions);
                    }
                }
                else {
                    //   System.out.println(" No perspectives for:"+sourceURI);
                }
            }
            else {
                //   System.out.println("no target mentions");
            }
        }
        // System.out.println("mentionMap.size() = " + mentionMap.size());
        if (mentionMap.size()>0) {
            Resource sourceResource = model.createResource(sourceURI);
            Property aProperty = model.createProperty(ns, property);
            Resource attributionSubject = model.createResource(attrId);
            attributionSubject.addProperty(aProperty, sourceResource);

            Set keySet = mentionMap.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                // System.out.println("key = " + key);
                ArrayList<NafMention> mentions = mentionMap.get(key);
                String[] factValues = key.split(",");
                for (int i = 0; i < factValues.length; i++) {
                    String factValue = factValues[i];
                    Resource factualityResource = model.createResource(ResourcesUri.grasp + factValue);
                    aProperty = model.createProperty(RDF.NAMESPACE, RDF.VALUE.getLocalName());
                    attributionSubject.addProperty(aProperty, factualityResource);
                }
                for (int j = 0; j < mentions.size(); j++) {
                    NafMention nafMention = mentions.get(j);
                    ////
                    Resource mentionObject = model.createResource(nafMention.toString());
                    aProperty = model.createProperty(ResourcesUri.grasp, "isAttributionFor");
                    attributionSubject.addProperty(aProperty, mentionObject);
                }
            }
        }
    }

    static public void addToJenaDataSet2 (Model model, String ns,
                                          String attrId, ArrayList<PerspectiveObject> perspectives,
                                          String sourceURI) {
        /*
        mentionId2      hasAttribution         attributionId1
                        gaf:generatedBy        mentionId3
        attributionId1  rdf:value              CERTAIN_POS_FUTURE
                        rdf:value              POSITIVE
                        prov:wasAttributedTo   doc-uri
                        gaf:wasAttributedTo    dbp:Zetsche

         */
        /*
http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=254,261
	gafAttribution:CERTAIN,NON_FUTURE
		http://dbpedia.org/resource/Caesars_Entertainment_Corporation ;
		gaf:generatedBy http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=201,209.


http://www.newsreader-project.eu/data/2006/10/02/4M1J-3MC0-TWKJ-V1W8.xml#char=201,209
			gafAttribution:CERTAIN,NON_FUTURE
				doc-uri;

doc-uri
	prov:wasAttributedTo author;
	prov:wasAttributedTo journal.
         */

        Resource sourceResource = model.createResource(sourceURI);
        Property property = model.createProperty(ResourcesUri.grasp, "hasAttribution");
        Resource attributionSubject = model.createResource(attrId);
        sourceResource.addProperty(property, attributionSubject);

        HashMap<String, ArrayList<NafMention>> mentionMap = new HashMap<String, ArrayList<NafMention>>();
        for (int p = 0; p < perspectives.size(); p++) {
            PerspectiveObject perspectiveObject = perspectives.get(p);
            if ((perspectiveObject.getTargetEventMentions().size()>0)) {
                ArrayList<KafFactuality> allFactualities = new ArrayList<KafFactuality>();
                for (int i = 0; i < perspectiveObject.getTargetEventMentions().size(); i++) {
                    NafMention mention = perspectiveObject.getTargetEventMentions().get(i);
                    Resource mentionObject = model.createResource(mention.toString());
                    property = model.createProperty(ResourcesUri.grasp, "attributedTo");
                    attributionSubject.addProperty(property, mentionObject);

                    for (int j = 0; j < mention.getFactuality().size(); j++) {
                        KafFactuality kafFactuality = mention.getFactuality().get(j);
                        allFactualities.add(kafFactuality);
                    }
                }
                KafFactuality kafFactuality = NafMention.getDominantFactuality(allFactualities);
                Resource factualityValue = null;
                ArrayList<String> factualityStringArray = KafFactuality.castToDefault();
                if (allFactualities.size() > 0) {
                    factualityStringArray= kafFactuality.getPredictionArrayList();

                 //   factualityValue = model.createResource(ResourcesUri.grasp + kafFactuality.getPrediction());
                }
                Resource sentimentValue = null;
                String sentiment = NafMention.getDominantOpinion(perspectiveObject.getTargetEventMentions());
                if (!sentiment.isEmpty()) {
                    if (sentiment.equals("+")) {
                        factualityStringArray.add("positive");
                    } else if (sentiment.equals("-")) {
                        factualityStringArray.add("negative");
                    }
                    //sentimentValue = model.createResource(ResourcesUri.grasp + sentiment);
                }
                property = model.createProperty(RDF.NAMESPACE, RDF.VALUE.getLocalName());

                for (int i = 0; i < factualityStringArray.size(); i++) {
                    String value = factualityStringArray.get(i);
                    attributionSubject.addProperty(property, value);
                }
            }
        }
    }


    static public void addJenaPerspectiveObjectsArray(String attrBase, String namespace,
                                            ArrayList<PerspectiveObject> perspectiveObjects, int cnt) {
        for (int i = 0; i < perspectiveObjects.size(); i++) {
            PerspectiveObject perspectiveObject = perspectiveObjects.get(i);
            String attrId = attrBase+"Attr"+cnt+i;
            perspectiveObject.addToJenaDataSet(graspModel, namespace, attrId);
        }
    }

    static public void addDocMetaData(String docId, KafSaxParser kafSaxParser, String project) {
       // String docId = kafSaxParser.getKafMetaData().getUrl();
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
        if (!project.isEmpty()) {
            try {
                property = graspModel.createProperty(ResourcesUri.rdfs, "comment");
                project = URLEncoder.encode(project, "UTF-8");
                Resource object = graspModel.createResource(ResourcesUri.nwrproject+project);
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
                                                                   String project,
                                                                   ArrayList<PerspectiveObject> sourcePerspectiveObjects,
                                                                   ArrayList<PerspectiveObject> authorPerspectiveObjects) {




            createModels();
            addJenaCompositeEvents(semEvents, null, false, false);
            String docId = kafSaxParser.getKafMetaData().getUrl().replaceAll("#", "HASH");
            if (!docId.toLowerCase().startsWith("http")) {
                docId = ResourcesUri.nwrdata + project + "/" + docId;
            }
            addDocMetaData(docId, kafSaxParser, project);

            String attrBase = docId+"/"+"source_attribution/";
            addJenaPerspectiveObjects(attrBase, ResourcesUri.grasp, "wasAttributedTo",sourcePerspectiveObjects, 1);
            attrBase = kafSaxParser.getKafMetaData().getUrl()+"/"+"doc_attribution/";
            addJenaPerspectiveObjects(attrBase, ResourcesUri.prov, "wasDerivedFrom", authorPerspectiveObjects, sourcePerspectiveObjects.size()+1);
        try {
            RDFDataMgr.write(stream, ds, RDFFormat.TRIG_PRETTY);
        } catch (Exception e) {
          //  e.printStackTrace();
        }
    }





}
