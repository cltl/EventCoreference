package eu.newsreader.eventcoreference.naf;

import eu.newsreader.eventcoreference.objects.SemObject;
import eu.newsreader.eventcoreference.objects.SemRelation;
import eu.newsreader.eventcoreference.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/14/13
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class InterDocumentEventCoref {
    static final String layer = "sem-instances";
    static final String name = "vua-event-coref-interdoc-time-lemma-baseline";
    static final String version = "1.0";
/*
    static ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
    static ArrayList<SemObject> semActors = new ArrayList<SemObject>();
    static ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
    static ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
    static ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
*/

    static public void main (String [] args) {
/*
        semEvents = new ArrayList<SemObject>();
        semActors = new ArrayList<SemObject>();
        semTimes = new ArrayList<SemObject>();
        semPlaces = new ArrayList<SemObject>();
        semRelations = new ArrayList<SemRelation>();
*/

        double conceptMatchThreshold = 0;
        double phraseMatchThreshold = 0;

        String pathToNafFolder = "";
        String extension = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--naf-folder") && args.length>(i+1)) {
                pathToNafFolder = args[i+1];
            }
            else if (arg.equals("--extension") && args.length>(i+1)) {
                extension = args[i+1];
            }
            else if (arg.equals("--concept-match") && args.length>(i+1)) {
                conceptMatchThreshold = Double.parseDouble(args[i+1]);
            }
            else if (arg.equals("--phrase-match") && args.length>(i+1)) {
                phraseMatchThreshold = Double.parseDouble(args[i+1]);
            }
        }
        processFolder (new File(pathToNafFolder), extension, conceptMatchThreshold, phraseMatchThreshold);
    }


    public static void processFolder (File pathToNafFolder, String extension, double conceptMatchThreshold,
                                      double phraseMatchThreshold

    ) {
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();

        ArrayList<File> files = Util.makeRecursiveFileList(pathToNafFolder, extension);
        //System.out.println("files.size() = " + files.size());
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            //System.out.println("file.getName() = " + file.getAbsolutePath());
            ArrayList<SemObject> mySemEvents = new ArrayList<SemObject>();
            ArrayList<SemObject> mySemActors = new ArrayList<SemObject>();
            ArrayList<SemObject> mySemTimes = new ArrayList<SemObject>();
            ArrayList<SemObject> mySemPlaces = new ArrayList<SemObject>();
            ArrayList<SemRelation> mySemRelations = new ArrayList<SemRelation>();
            GetSemFromNafFile.processNafFile(file.getAbsolutePath(), mySemEvents, mySemActors, mySemPlaces, mySemTimes, mySemRelations);

            HashMap<String, String> localToGlobalEventMap = new HashMap<String, String>();
            HashMap<String, String> localToGlobalActorMap = new HashMap<String, String>();
            HashMap<String, String> localToGlobalPlaceMap = new HashMap<String, String>();
            HashMap<String, String> localToGlobalTimeMap = new HashMap<String, String>();

            for (int j = 0; j < mySemEvents.size(); j++) {
                SemObject mySemEvent = mySemEvents.get(j);
                boolean merge = false;
                for (int k = 0; k < semEvents.size(); k++) {
                    SemObject semEvent = semEvents.get(k);
                    double phraseMatch = mySemEvent.matchObjectByPhrases((semEvent));
    /*
                        System.out.println("phraseMatch = " + phraseMatch);
                        System.out.println("semEvent.getPhraseCounts().toString() = " + semEvent.getPhraseCounts().toString());
                        System.out.println("mySemEvent.getPhraseCounts().toString() = " + mySemEvent.getPhraseCounts().toString());
    */
                    if (phraseMatch>=phraseMatchThreshold) {
                        merge = true;
                        semEvent.mergeSemObject(mySemEvent);
                        localToGlobalEventMap.put(mySemEvent.getId(), semEvent.getId());
                        /// merge and check relations
                        /// but we can only check the relations after we checked the objects as well
                        break;
                    }
                    else {
                        if (conceptMatchThreshold>0)  {
                            double conceptMatch = mySemEvent.matchObjectByConcepts((semEvent));
                            if (conceptMatch>=conceptMatchThreshold) {
                                merge = true;
                                semEvent.mergeSemObject(mySemEvent);
                                localToGlobalEventMap.put(mySemEvent.getId(), semEvent.getId());
                                break;
                            }
                        }
                    }
                }

                if (!merge) {
                    semEvents.add(mySemEvent);
                }
            }

            for (int j = 0; j < mySemActors.size(); j++) {
                SemObject mySemActor = mySemActors.get(j);
                boolean merge = false;
                for (int k = 0; k < semActors.size(); k++) {
                    SemObject semActor = semActors.get(k);
                    double phraseMatch = mySemActor.matchObjectByPhrases((semActor));
                    if (phraseMatch>=phraseMatchThreshold) {
                        merge = true;
                        semActor.mergeSemObject(mySemActor);
                        localToGlobalEventMap.put(mySemActor.getId(), semActor.getId());
                        break;
                    }
                    else {
                        if (conceptMatchThreshold>0)  {
                            double conceptMatch = mySemActor.matchObjectByConcepts((semActor));
                            if (conceptMatch>=conceptMatchThreshold) {
                                merge = true;
                                semActor.mergeSemObject(mySemActor);
                                localToGlobalActorMap.put(mySemActor.getId(), semActor.getId());
                                break;
                            }
                        }
                    }
                }

                if (!merge) {
                    semActors.add(mySemActor);
                }
            }


            for (int j = 0; j < mySemPlaces.size(); j++) {
                SemObject mySemPlace = mySemPlaces.get(j);
                boolean merge = false;
                for (int k = 0; k < semPlaces.size(); k++) {
                    SemObject semPlace = semPlaces.get(k);
                    double phraseMatch = mySemPlace.matchObjectByPhrases((semPlace));
                    if (phraseMatch>=phraseMatchThreshold) {
                        merge = true;
                        semPlace.mergeSemObject(mySemPlace);
                        localToGlobalEventMap.put(mySemPlace.getId(), semPlace.getId());
                        break;
                    }
                    else {
                        if (conceptMatchThreshold>0)  {
                            double conceptMatch = mySemPlace.matchObjectByConcepts((semPlace));
                            if (conceptMatch>=conceptMatchThreshold) {
                                merge = true;
                                semPlace.mergeSemObject(mySemPlace);
                                localToGlobalPlaceMap.put(mySemPlace.getId(), semPlace.getId());
                                break;
                            }
                        }
                    }
                }

                if (!merge) {
                    semPlaces.add(mySemPlace);
                }
            }

            for (int j = 0; j < mySemTimes.size(); j++) {
                SemObject mySemTime = mySemTimes.get(j);
                boolean merge = false;
                for (int k = 0; k < semTimes.size(); k++) {
                    SemObject semTime = semTimes.get(k);
                    double phraseMatch = mySemTime.matchObjectByPhrases((semTime));
                    if (phraseMatch>=phraseMatchThreshold) {
                        merge = true;
                        semTime.mergeSemObject(mySemTime);
                        localToGlobalEventMap.put(mySemTime.getId(), semTime.getId());
                        break;
                    }
                    else {
                        if (conceptMatchThreshold>0)  {
                            double conceptMatch = mySemTime.matchObjectByConcepts((semTime));
                            if (conceptMatch>=conceptMatchThreshold) {
                                merge = true;
                                semTime.mergeSemObject(mySemTime);
                                localToGlobalTimeMap.put(mySemTime.getId(), semTime.getId());
                                break;
                            }
                        }
                    }
                }

                if (!merge) {
                    semTimes.add(mySemTime);
                }
            }
            // @TODO  HANDLE WEAK EVENT MATCHES IF RELATION MATCHES ARE STRONG
            //// now we need to evaluate the relations......
                /*
                 a relation is present in the global data if:
                 * First we check if the subject and object are in the local and global maps
                 * If so, we can check if there is already a relation of the same type
                 *      - if yes, merge it mentions with the mentions of the global relation
                 *      - if no, we add with the global identifiers
                 * If not, it is a new relation so we add the relation as is
                 *
                 * Future work..... if events have a weak concept map, we could compensate for string actor, time, place matches
                 *
                  */

            for (int j = 0; j < mySemRelations.size(); j++) {
                SemRelation semRelation = mySemRelations.get(j);
                //System.out.println("semRelation.getSubject() = " + semRelation.getSubject());
                SemRelation globalSemRelationCandidate = new SemRelation(semRelation);
                globalSemRelationCandidate.setId(semRelation.getId());
                if (localToGlobalEventMap.containsKey(semRelation.getSubject())) {
                    String globalId = localToGlobalEventMap.get(semRelation.getSubject());
                    globalSemRelationCandidate.setSubject(globalId);
                }
                if (localToGlobalActorMap.containsKey(semRelation.getObject())) {
                    String globalId = localToGlobalActorMap.get(semRelation.getObject());
                    globalSemRelationCandidate.setObject(globalId);
                }
                else if (localToGlobalPlaceMap.containsKey(semRelation.getObject())) {
                    String globalId = localToGlobalPlaceMap.get(semRelation.getObject());
                    globalSemRelationCandidate.setObject(globalId);
                }
                else if (localToGlobalTimeMap.containsKey(semRelation.getObject())) {
                    String globalId = localToGlobalTimeMap.get(semRelation.getObject());
                    globalSemRelationCandidate.setObject(globalId);
                }
                boolean merge = false;
                for (int k = 0; k < semRelations.size(); k++) {
                    SemRelation relation = semRelations.get(k);
                    if (globalSemRelationCandidate.match(relation)) {
                        //// merge mentions

                        relation.addMentions(globalSemRelationCandidate.getNafMentions());
                        merge = true;
                        break;
                    }
                }

                if (!merge) {
                    semRelations.add(globalSemRelationCandidate);
                }
            }
        }
        try {
            //System.out.println("pathToNafFolder = " + pathToNafFolder);
            FileOutputStream fos = new FileOutputStream(pathToNafFolder+"/sem.trig");
            GetSemFromNafFile.serializeJena(fos,  semEvents, semActors, semPlaces, semTimes, semRelations);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


}
