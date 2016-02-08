package eu.newsreader.eventcoreference.naf;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.kyotoproject.kaf.KafEvent;
import eu.kyotoproject.kaf.KafParticipant;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.newsreader.eventcoreference.objects.NafMention;
import eu.newsreader.eventcoreference.objects.PerspectiveObject;
import eu.newsreader.eventcoreference.objects.SemActor;
import eu.newsreader.eventcoreference.objects.SemObject;
import eu.newsreader.eventcoreference.output.JenaSerialization;
import eu.newsreader.eventcoreference.util.FrameTypes;
import eu.newsreader.eventcoreference.util.RoleLabels;
import eu.newsreader.eventcoreference.util.Util;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by piek on 11/03/15.
 */
public class GetPerspectiveRelations {
        static public boolean FILTERA0 = true;

        static public void main (String[] args) {
            String comFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-eventcoreference_v2_2014/resources/communication.txt";
            String contextualFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-eventcoreference_v2_2014/resources/contextual.txt";
            String grammaticalFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-eventcoreference_v2_2014/resources/grammatical.txt";
            Vector<String> communicationVector = Util.ReadFileToStringVector(comFrameFile);
            Vector<String> grammaticalVector = Util.ReadFileToStringVector(grammaticalFrameFile);
            Vector<String> contextualVector = Util.ReadFileToStringVector(contextualFrameFile);

            String project = "test";
            String nafFilePath = "";
            //nafFilePath = args[0];
            nafFilePath = "/Users/piek/Desktop/NWR/NWR-DATA/LN_cars_NAF-2003_1001-2000/2003/1/1";
            KafSaxParser kafSaxParser = new KafSaxParser();
            ArrayList<File> files = Util.makeRecursiveFileList(new File(nafFilePath));
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                kafSaxParser.parseFile(file);
                String baseUri = kafSaxParser.getKafMetaData().getUrl() + GetSemFromNaf.ID_SEPARATOR;
                ArrayList<SemObject> semActors = new ArrayList<SemObject>();
                String entityUri = ResourcesUri.nwrdata+project+"/entities/";
                if (!baseUri.toLowerCase().startsWith("http")) {
                    baseUri = ResourcesUri.nwrdata + project + "/" + kafSaxParser.getKafMetaData().getUrl() + GetSemFromNaf.ID_SEPARATOR;
                }
                GetSemFromNaf.processNafFileForEntityCoreferenceSets(entityUri, baseUri, kafSaxParser, semActors);
                GetSemFromNaf.processSrlForRemainingFramenetRoles(project, kafSaxParser, semActors);

                ArrayList<PerspectiveObject> perspectives = getPerspective(baseUri, kafSaxParser, contextualVector, communicationVector, grammaticalVector);
                perspectives = selectSourceEntityToPerspectives(kafSaxParser, perspectives, semActors);
                for (int j = 0; j < perspectives.size(); j++) {
                    PerspectiveObject perspectiveObject = perspectives.get(j);
                  //  System.out.println("perspectiveObject.toString() = " + perspectiveObject.toString());
                }
            }
        }


    /**
     * @Get perspective objects from sources in the text
     * @param kafSaxParser
     * @param project
     * @param semActors
     * @param contextualVector
     * @param communicationVector
     * @param grammaticalVector
     * @return
     */
        static public ArrayList<PerspectiveObject> getSourcePerspectives (KafSaxParser kafSaxParser, String project,
                                                                   ArrayList<SemObject> semActors,
                                    Vector<String> contextualVector, 
                                    Vector<String> communicationVector,
                                    Vector<String> grammaticalVector) {
            String baseUri = kafSaxParser.getKafMetaData().getUrl() + GetSemFromNaf.ID_SEPARATOR;
            if (!baseUri.toLowerCase().startsWith("http")) {
                baseUri = ResourcesUri.nwrdata + project + "/" + kafSaxParser.getKafMetaData().getUrl() + GetSemFromNaf.ID_SEPARATOR;
            }
            ArrayList<PerspectiveObject> perspectiveObjects = getPerspective(baseUri, kafSaxParser,
                    contextualVector,
                    communicationVector,
                    grammaticalVector);
            if (FILTERA0) perspectiveObjects = selectSourceEntityToPerspectives(kafSaxParser, perspectiveObjects, semActors);
            return perspectiveObjects;
        }

    /**
     * @Get perspective objects assigned to authors, publisher, magazine, which is represented through the document itself
     * @param kafSaxParser
     * @param perspectiveObjects
     * @return
     */
        static public ArrayList<PerspectiveObject> getAuthorPerspectives (KafSaxParser kafSaxParser,String project,
                                                                          ArrayList<PerspectiveObject> perspectiveObjects)
        {
            String baseUrl = kafSaxParser.getKafMetaData().getUrl() + GetSemFromNaf.ID_SEPARATOR;
            if (!baseUrl.toLowerCase().startsWith("http")) {
                baseUrl = ResourcesUri.nwrdata + project + "/" + kafSaxParser.getKafMetaData().getUrl() + GetSemFromNaf.ID_SEPARATOR;
            }

            SemActor semActor = new SemActor(SemObject.ENTITY);
            semActor.setId(kafSaxParser.getKafMetaData().getUrl());
            ArrayList<PerspectiveObject> authorPerspectiveObjects = new ArrayList<PerspectiveObject>();
            for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
                KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
                kafEvent.setTokenStrings(kafSaxParser);
                NafMention eventMention = Util.getNafMentionForTermIdArrayList(baseUrl, kafSaxParser, kafEvent.getSpanIds());
                boolean hasPerspective = false;
                for (int j = 0; j < perspectiveObjects.size(); j++) {
                    PerspectiveObject perspectiveObject = perspectiveObjects.get(j);
                    for (int k = 0; k < perspectiveObject.getTargetEventMentions().size(); k++) {
                        NafMention nafMention = perspectiveObject.getTargetEventMentions().get(k);
                        if (nafMention.sameMention(eventMention)) {
                           hasPerspective = true;
                            break;
                        }
                    }
                }
                if (!hasPerspective) {
                    PerspectiveObject perspectiveObject = new PerspectiveObject();
                    perspectiveObject.setDocumentUri(baseUrl);
                    perspectiveObject.setSourceEntity(semActor);
                    perspectiveObject.setPredicateId(kafEvent.getId());
                    perspectiveObject.setEventString(kafEvent.getTokenString());
                    perspectiveObject.setPredicateConcepts(kafEvent.getExternalReferences());
                    perspectiveObject.setPredicateSpanIds(kafEvent.getSpanIds());
                    perspectiveObject.setNafMention(baseUrl, kafSaxParser, kafEvent.getSpanIds());
                    eventMention.addFactuality(kafSaxParser);
                    eventMention.addOpinion(kafSaxParser);
                    perspectiveObject.addTargetEventMention(eventMention);
                    authorPerspectiveObjects.add(perspectiveObject);
                }
            }
            return authorPerspectiveObjects;
        }

       static public void getPerspective(KafSaxParser kafSaxParser, String project, ArrayList<PerspectiveObject> perspectives,
                                    Vector<String> contextualVector,
                                    Vector<String> communicationVector,
                                    Vector<String> grammaticalVector) {
            String baseUri = kafSaxParser.getKafMetaData().getUrl() + GetSemFromNaf.ID_SEPARATOR;
            if (!baseUri.toLowerCase().startsWith("http")) {
                baseUri = ResourcesUri.nwrdata + project + "/" + kafSaxParser.getKafMetaData().getUrl() + GetSemFromNaf.ID_SEPARATOR;
            }
            ArrayList<PerspectiveObject> perspectiveObjects = getPerspective(baseUri,kafSaxParser, contextualVector, communicationVector, grammaticalVector);
            perspectives.addAll(perspectiveObjects);
        }

        static public ArrayList<PerspectiveObject> getPerspective (String baseUri,
                                                                   KafSaxParser kafSaxParser,
                                    Vector<String> contextualVector,
                                    Vector<String> communicationVector,
                                    Vector<String> grammaticalVector) {
            ArrayList<PerspectiveObject> perspectiveObjectArrayList = new ArrayList<PerspectiveObject>();
            for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
                KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
                kafEvent.setTokenStrings(kafSaxParser);
                String eventType = FrameTypes.getEventTypeString(kafEvent.getExternalReferences(), contextualVector, communicationVector, grammaticalVector);
                if (!eventType.isEmpty()) {
                    if (eventType.equalsIgnoreCase(FrameTypes.SOURCE)) {
                        KafParticipant sourceParticipant = new KafParticipant();
                        KafParticipant targetParticipant = new KafParticipant();
                        /// next we get the A0 and message roles

                        for (int k = 0; k < kafEvent.getParticipants().size(); k++) {
                            KafParticipant kafParticipant = kafEvent.getParticipants().get(k);
                            if (RoleLabels.hasSourceTarget(kafParticipant, communicationVector)) {
                                targetParticipant = kafParticipant;
                            }
                            else if (RoleLabels.isPRIMEPARTICIPANT(kafParticipant.getRole())) {
                                sourceParticipant = kafParticipant;
                            }
                        }
                      //  if (sourceParticipant!=null && targetParticipant !=null) {
                          //  System.out.println("targetParticipant.toString() = " + targetParticipant.toString());

                            sourceParticipant.setTokenStrings(kafSaxParser);
                            targetParticipant.setTokenStrings(kafSaxParser);
                            PerspectiveObject perspectiveObject = new PerspectiveObject();
                            perspectiveObject.setDocumentUri(baseUri);
                            perspectiveObject.setPredicateId(kafEvent.getId());
                            perspectiveObject.setEventString(kafEvent.getTokenString());
                            perspectiveObject.setPredicateConcepts(kafEvent.getExternalReferences());
                            perspectiveObject.setPredicateSpanIds(kafEvent.getSpanIds());
                            perspectiveObject.setSource(sourceParticipant);
                            perspectiveObject.setTarget(targetParticipant);
                            perspectiveObject.setCueMention(baseUri, kafSaxParser, kafEvent.getSpanIds());
                            perspectiveObject.setNafMention(baseUri, kafSaxParser, kafEvent.getSpanIds());
                            for (int j = 0; j < kafSaxParser.getKafEventArrayList().size(); j++) {
                                if (j!=i) {
                                    KafEvent event = kafSaxParser.getKafEventArrayList().get(j);
                                    if (!Collections.disjoint(targetParticipant.getSpanIds(), event.getSpanIds())) {
                                        /// this event is embedded inside the target
                                        NafMention nafMention = Util.getNafMentionForTermIdArrayList(baseUri, kafSaxParser, event.getSpanIds());
                                        nafMention.addFactuality(kafSaxParser);
                                        nafMention.addOpinion(kafSaxParser);
                                        perspectiveObject.addTargetEventMention(nafMention);
                                    }
                                }
                            }
                        for (int j = 0; j < perspectiveObject.getTargetEventMentions().size(); j++) {
                            NafMention nafMention = perspectiveObject.getTargetEventMentions().get(j);
                           // System.out.println("nafMention.getFactuality().size() = " + nafMention.getFactuality().size());
                        }
                            perspectiveObjectArrayList.add(perspectiveObject);
                      //}
                    }
                }
            }
            return perspectiveObjectArrayList;
        }

    static public HashMap<String, ArrayList<PerspectiveObject>> getPerspectiveMap (String baseUri,
                                                                   KafSaxParser kafSaxParser,
                                    Vector<String> contextualVector,
                                    Vector<String> communicationVector,
                                    Vector<String> grammaticalVector) {
            HashMap<String, ArrayList<PerspectiveObject>> perspectiveObjectHashMap = new HashMap<String, ArrayList<PerspectiveObject>>();
            for (int i = 0; i < kafSaxParser.getKafEventArrayList().size(); i++) {
                KafEvent kafEvent = kafSaxParser.getKafEventArrayList().get(i);
                kafEvent.setTokenStrings(kafSaxParser);
                String eventType = FrameTypes.getEventTypeString(kafEvent.getExternalReferences(), contextualVector, communicationVector, grammaticalVector);
                if (!eventType.isEmpty()) {
                    if (eventType.equalsIgnoreCase(FrameTypes.SOURCE)) {
                        KafParticipant sourceParticipant = new KafParticipant();
                        KafParticipant targetParticipant = new KafParticipant();
                        /// next we get the A0 and message roles

                        for (int k = 0; k < kafEvent.getParticipants().size(); k++) {
                            KafParticipant kafParticipant = kafEvent.getParticipants().get(k);
                            if (RoleLabels.hasSourceTarget(kafParticipant, communicationVector)) {
                                targetParticipant = kafParticipant;
                            }
                            else if (RoleLabels.isPRIMEPARTICIPANT(kafParticipant.getRole())) {
                                sourceParticipant = kafParticipant;
                            }
                        }
                      //  if (sourceParticipant!=null && targetParticipant !=null) {
                          //  System.out.println("targetParticipant.toString() = " + targetParticipant.toString());

                            sourceParticipant.setTokenStrings(kafSaxParser);
                            targetParticipant.setTokenStrings(kafSaxParser);
                            PerspectiveObject perspectiveObject = new PerspectiveObject();
                            perspectiveObject.setDocumentUri(baseUri);
                            perspectiveObject.setPredicateId(kafEvent.getId());
                            perspectiveObject.setEventString(kafEvent.getTokenString());
                            perspectiveObject.setPredicateConcepts(kafEvent.getExternalReferences());
                            perspectiveObject.setPredicateSpanIds(kafEvent.getSpanIds());
                            perspectiveObject.setSource(sourceParticipant);
                            perspectiveObject.setTarget(targetParticipant);
                            perspectiveObject.setCueMention(baseUri, kafSaxParser, kafEvent.getSpanIds());
                            perspectiveObject.setNafMention(baseUri, kafSaxParser, kafEvent.getSpanIds());
                            for (int j = 0; j < kafSaxParser.getKafEventArrayList().size(); j++) {
                                if (j!=i) {
                                    KafEvent event = kafSaxParser.getKafEventArrayList().get(j);
                                    if (!Collections.disjoint(targetParticipant.getSpanIds(), event.getSpanIds())) {
                                        /// this event is embedded inside the target
                                        NafMention nafMention = Util.getNafMentionForTermIdArrayList(baseUri, kafSaxParser, event.getSpanIds());
                                        nafMention.addFactuality(kafSaxParser);
                                        perspectiveObject.addTargetEventMention(nafMention);
                                    }
                                }
                            }
                        for (int j = 0; j < perspectiveObject.getTargetEventMentions().size(); j++) {
                            NafMention nafMention = perspectiveObject.getTargetEventMentions().get(j);
                            if (perspectiveObjectHashMap.containsKey(nafMention.toString())) {
                                ArrayList<PerspectiveObject> perspectiveObjectArrayList = perspectiveObjectHashMap.get(nafMention.toString());
                                perspectiveObjectArrayList.add(perspectiveObject);
                                perspectiveObjectHashMap.put(nafMention.toString(), perspectiveObjectArrayList);
                            }
                            else {
                                ArrayList<PerspectiveObject> perspectiveObjectArrayList = new ArrayList<PerspectiveObject>();
                                perspectiveObjectArrayList.add(perspectiveObject);
                                perspectiveObjectHashMap.put(nafMention.toString(), perspectiveObjectArrayList);
                            }
                        }
                      //}
                    }
                }
            }
            return perspectiveObjectHashMap;
        }

    /**
     * Filters perspectives to select those that match with a given actor
     * @param kafSaxParser
     * @param perspectives
     * @param actors
     * @return
     */
        static public ArrayList<PerspectiveObject> selectSourceEntityToPerspectives (KafSaxParser kafSaxParser,
                                                                                     ArrayList<PerspectiveObject> perspectives,
                                                                                     ArrayList<SemObject> actors) {
            ArrayList<PerspectiveObject> sourcePerspectives = new ArrayList<PerspectiveObject>();
            for (int i = 0; i < perspectives.size(); i++) {
                PerspectiveObject perspectiveObject = perspectives.get(i);
                for (int j = 0; j < actors.size(); j++) {
                    SemObject semActor = actors.get(j);
                    if (Util.matchAllSpansOfAnObjectMentionOrTheRoleHead(kafSaxParser, perspectiveObject.getSource(), semActor)) {
                        //System.out.println("semObject.getURI() = " + semActor.getURI());
                        perspectiveObject.setSourceEntity((SemActor)semActor);
                        sourcePerspectives.add(perspectiveObject);
                    }
                }
            }
            return sourcePerspectives;
        }

    public static void perspectiveRelationsToTrig (String pathToTrigFile, ArrayList<PerspectiveObject> perspectiveObjects) {
        try {
            OutputStream fos = new FileOutputStream(pathToTrigFile);
            Dataset ds = TDBFactory.createDataset();
            Model defaultModel = ds.getDefaultModel();
            //ResourcesUri.prefixModel(defaultModel);
          //  Model provenanceModel = ds.getNamedModel("http://www.newsreader-project.eu/perspective");
            ResourcesUri.prefixModelGaf(defaultModel);
            String attrBase = pathToTrigFile+"_";
            JenaSerialization.addJenaPerspectiveObjects(attrBase, ResourcesUri.gaf, perspectiveObjects);
            RDFDataMgr.write(fos, ds, RDFFormat.TRIG_PRETTY);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void perspectiveRelationsToTrig (String pathToTrigFile,
                                                   KafSaxParser kafSaxParser,
                                                   ArrayList<PerspectiveObject> sourcePerspectiveObjects,
                                                   ArrayList<PerspectiveObject> authorPerspectiveObjects) {
            try {
                OutputStream fos = new FileOutputStream(pathToTrigFile);
                Dataset ds = TDBFactory.createDataset();
                Model defaultModel = ds.getDefaultModel();
                ResourcesUri.prefixModelGaf(defaultModel);
                ResourcesUri.prefixModelNwr(defaultModel);
                defaultModel.setNsPrefix("rdf", ResourcesUri.rdf);
                defaultModel.setNsPrefix("rdfs", ResourcesUri.rdfs);

                JenaSerialization.addDocMetaData(ds, kafSaxParser);
                String attrBase = kafSaxParser.getKafMetaData().getUrl()+"_"+"s";
                JenaSerialization.addJenaPerspectiveObjects(attrBase, ResourcesUri.gaf, sourcePerspectiveObjects);
                attrBase = kafSaxParser.getKafMetaData().getUrl()+"_"+"d";
                JenaSerialization.addJenaPerspectiveObjects(attrBase, ResourcesUri.prov, authorPerspectiveObjects);
                RDFDataMgr.write(fos, ds, RDFFormat.TRIG_PRETTY);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
    }

    public static void perspectiveRelationsToTrigStream (OutputStream fos, String uri, ArrayList<PerspectiveObject> perspectiveObjects) {

                Dataset ds = TDBFactory.createDataset();
                Model defaultModel = ds.getDefaultModel();
                ResourcesUri.prefixModel(defaultModel);
              //  Model provenanceModel = ds.getNamedModel("http://www.newsreader-project.eu/perspective");
                ResourcesUri.prefixModelGaf(defaultModel);
                String attrBase = uri+"_";
                JenaSerialization.addJenaPerspectiveObjects(attrBase, ResourcesUri.gaf, perspectiveObjects);
                RDFDataMgr.write(fos, ds, RDFFormat.TRIG_PRETTY);
    }


}
