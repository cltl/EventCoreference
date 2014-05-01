package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.util.ReadSourceMetaFile;
import eu.newsreader.eventcoreference.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/14/13
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class LargeInterDocumentEventCoref {
    static final String layer = "sem-instances";
    static final String name = "vua-event-coref-interdoc-time-lemma-baseline";
    static final String version = "1.0";

    static public void main (String [] args) {

        HashMap<String, SourceMeta> sourceMetaHashMap = null;
        double conceptMatchThreshold = 0;
        double phraseMatchThreshold = 1;
        String projectName = "";
        String pathToSourceDataFile = "";
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
            else if (arg.equals("--project") && args.length>(i+1)) {
                projectName = args[i+1];
            }
            else if (arg.equals("--source-data") && args.length>(i+1)) {
                pathToSourceDataFile = args[i+1];
                sourceMetaHashMap = ReadSourceMetaFile.readSourceFile(pathToSourceDataFile);
               // System.out.println("sourceMetaHashMap = " + sourceMetaHashMap.size());
            }
        }
       // processFolderForEntities(projectName, new File(pathToNafFolder), extension, conceptMatchThreshold, phraseMatchThreshold);
        try {
            processFolderEvents(projectName, new File(pathToNafFolder), extension);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static boolean compareTime (ArrayList<SemObject> mySemTimes,
                                       ArrayList<SemObject> semTimes) {

        for (int i = 0; i < mySemTimes.size(); i++) {
            SemObject mySemTime = mySemTimes.get(i);
            OwlTime myOwlTime = new OwlTime();
            myOwlTime.parseStringDate(mySemTime.getPhrase());
            for (int j = 0; j < semTimes.size(); j++) {
                SemObject semTime = semTimes.get(j);
                OwlTime owlTime = new OwlTime();
                owlTime.parseStringDate(semTime.getPhrase());
                /// replace this by exact time matches....
                if (myOwlTime.matchTimeEmbedded(owlTime)) {
                  //  System.out.println("myOwlTime.getDateString() = " + myOwlTime.getDateString());
                  //  System.out.println("owlTime.getDateString() = " + owlTime.getDateString());

                   return true;
                }
            }

        }
        return false;
    }

    public static boolean comparePlace (ArrayList<SemObject> mySemPlaces,
                                       ArrayList<SemObject> semPlaces) {

        for (int i = 0; i < mySemPlaces.size(); i++) {
            SemObject mySemPlace = mySemPlaces.get(i);
            for (int j = 0; j < semPlaces.size(); j++) {
                SemObject semPlace = semPlaces.get(j);
                if (mySemPlace.getURI().equals(semPlace)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean compareActor (ArrayList<SemObject> mySemActors,
                                       ArrayList<SemObject> semActors) {

        for (int i = 0; i < mySemActors.size(); i++) {
            SemObject mySemActor = mySemActors.get(i);
            for (int j = 0; j < semActors.size(); j++) {
                SemObject semActor = semActors.get(j);
                if (semActor.getURI().equals(semActor)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean compareComponents (SemObject mySemEvent,
                                          ArrayList<SemObject> theSemActors,
                                          ArrayList<SemObject> theSemPlaces,
                                          ArrayList<SemObject> theSemTimes,
                                          ArrayList<SemRelation> mySemRelations,
                                          SemObject semEvent,
                                          ArrayList<SemObject> semActors,
                                          ArrayList<SemObject> semPlaces,
                                          ArrayList<SemObject> semTimes,
                                          ArrayList<SemRelation> semRelations) {

        ArrayList<SemObject> mySemTimes = getMySemObjects(mySemEvent, mySemRelations, theSemTimes);
        ArrayList<SemObject> oSemTimes = getMySemObjects(semEvent, semRelations, semTimes);
        if (!compareTime(mySemTimes, oSemTimes)) {
           return false;
        }
        ArrayList<SemObject> mySemPlaces = getMySemObjects(mySemEvent, mySemRelations, theSemPlaces);
        ArrayList<SemObject> oSemPlaces = getMySemObjects(semEvent, semRelations, semPlaces);
        if (mySemPlaces.size()>0 && oSemPlaces.size()>0) {
            if (!comparePlace(mySemPlaces, oSemPlaces)) {
                return false;
            }
        }
        ArrayList<SemObject> mySemActors = getMySemObjects(mySemEvent, mySemRelations, theSemActors);
        ArrayList<SemObject> oSemActors = getMySemObjects(semEvent, semRelations, semActors);
        if (!compareActor(mySemActors, oSemActors)) {
            return false;
        }
        return true;
    }

    public static ArrayList<SemRelation> getMySemRelations (SemObject event, ArrayList<SemRelation> semRelations) {
        ArrayList<SemRelation> mySemRelations = new ArrayList<SemRelation>();
        for (int i = 0; i < semRelations.size(); i++) {
            SemRelation semRelation = semRelations.get(i);
            if (semRelation.getSubject().equals(event.getId())) {
                mySemRelations.add(semRelation);
            }
        }
        return mySemRelations;
    }

    public static ArrayList<SemObject> getMySemObjects (SemObject event, ArrayList<SemRelation> semRelations, ArrayList<SemObject> semObjects) {
        ArrayList<SemObject> mySemObjects = new ArrayList<SemObject>();
        for (int i = 0; i < semRelations.size(); i++) {
            SemRelation semRelation = semRelations.get(i);
            for (int j = 0; j < semObjects.size(); j++) {
                SemObject semObject = semObjects.get(j);
                if (semRelation.getSubject().equals(event.getId()) &&
                    semRelation.getObject().equals(semObject.getId())) {
/*
                    System.out.println("semRelation = " + semRelation.getSubject());
                    System.out.println("semRelation = " + semRelation.getPredicate());
                    System.out.println("semRelation = " + semRelation.getObject());
*/
                    mySemObjects.add(semObject);
                }
            }
        }
        return mySemObjects;
    }


    public static void processFolder (String project, File pathToNafFolder, String extension, double conceptMatchThreshold,
                                      double phraseMatchThreshold,
                                      HashMap<String, SourceMeta> sourceMetaHashMap

    ) {
        KafSaxParser kafSaxParser = new KafSaxParser();
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        ArrayList<SemRelation> factRelations = new ArrayList<SemRelation>();
        ArrayList<SemObject> mySemEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> mySemActors = new ArrayList<SemObject>();
        ArrayList<SemObject> mySemTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> mySemPlaces = new ArrayList<SemObject>();
        ArrayList<SemRelation> mySemRelations = new ArrayList<SemRelation>();
        ArrayList<SemRelation> myFactRelations = new ArrayList<SemRelation>();
        HashMap<String, String> localToGlobalEventMap = new HashMap<String, String>();
        HashMap<String, String> localToGlobalActorMap = new HashMap<String, String>();
        HashMap<String, String> localToGlobalPlaceMap = new HashMap<String, String>();
        HashMap<String, String> localToGlobalTimeMap = new HashMap<String, String>();

        //FileOutputStream timelessEventsFos = new FileOutputStream(pathToNafFolder+"/timelesssem.trig");
        ArrayList<File> files = Util.makeRecursiveFileList(pathToNafFolder, extension);
        //System.out.println("files.size() = " + files.size());
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            if (i%50==0) {
                System.out.println("i = " + i);
                System.out.println("file.getName() = " + file.getAbsolutePath());
                System.out.println("semEvents = " + semEvents.size());
                System.out.println("semActors = " + semActors.size());
                System.out.println("semTimes = " + semTimes.size());
                System.out.println("semPlaces = " + semPlaces.size());
                System.out.println("semRelations = " + semRelations.size());
            }
            mySemEvents = new ArrayList<SemObject>();
            mySemActors = new ArrayList<SemObject>();
            mySemTimes = new ArrayList<SemObject>();
            mySemPlaces = new ArrayList<SemObject>();
            mySemRelations = new ArrayList<SemRelation>();
            myFactRelations = new ArrayList<SemRelation>();
            kafSaxParser.parseFile(file.getAbsolutePath());
            GetSemFromNafFile.processNafFile(project, kafSaxParser, mySemEvents, mySemActors, mySemPlaces, mySemTimes, mySemRelations,myFactRelations);
            localToGlobalEventMap = new HashMap<String, String>();
            localToGlobalActorMap = new HashMap<String, String>();
            localToGlobalPlaceMap = new HashMap<String, String>();
            localToGlobalTimeMap = new HashMap<String, String>();

            ArrayList<SemObject> times = new ArrayList<SemObject>();
            for (int j = 0; j < mySemEvents.size(); j++) {
                SemObject mySemEvent = mySemEvents.get(j);
/*                times = getMySemObjects(mySemEvent, mySemRelations, mySemTimes);
                if (times.size()==0) {

                    ArrayList<SemObject> theSemEvent = new ArrayList<SemObject>(); theSemEvent.add(mySemEvent);
                    ArrayList<SemObject> myTimes = getMySemObjects(mySemEvent, mySemRelations, mySemTimes);
                    ArrayList<SemObject> myPlaces = getMySemObjects(mySemEvent, mySemRelations, mySemPlaces);
                    ArrayList<SemObject> myActors = getMySemObjects(mySemEvent, mySemRelations, mySemActors);
                    ArrayList<SemRelation> myRelations = getMySemRelations(mySemEvent, mySemRelations);
                    ArrayList<SemRelation> myFacts = getMySemRelations(mySemEvent, myFactRelations);
                    GetSemFromNafFile.serializeJena(timelessEventsFos,  theSemEvent, myActors, myPlaces, myTimes, myRelations, myFacts, sourceMetaHashMap);
                    continue;
                }*/

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
                        if (compareComponents(mySemEvent, mySemActors, mySemPlaces, times, mySemRelations,
                                              semEvent, semActors, semPlaces, semTimes, semRelations)) {
                            merge = true;
                            semEvent.mergeSemObject(mySemEvent);
                            localToGlobalEventMap.put(mySemEvent.getId(), semEvent.getId());
                            /// merge and check relations
                            /// but we can only check the relations after we checked the objects as well
                            break;
                        }
                        else {
                           // System.out.println("NOT MERGING THESE semEvent.getPhrase() = " + semEvent.getPhrase());
                        }
                    }
                    else {
                        if (conceptMatchThreshold>0)  {
                            double conceptMatch = mySemEvent.matchObjectByConcepts((semEvent));
                            if (conceptMatch>=conceptMatchThreshold) {
                                if (compareComponents(mySemEvent, mySemActors, mySemPlaces, mySemTimes, mySemRelations,
                                        semEvent, semActors, semPlaces, semTimes, semRelations)) {
                                    merge = true;
                                    semEvent.mergeSemObject(mySemEvent);
                                    localToGlobalEventMap.put(mySemEvent.getId(), semEvent.getId());
                                    break;
                                }
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

           // System.out.println("myFactRelations = " + myFactRelations.size());
            for (int j = 0; j < myFactRelations.size(); j++) {
                SemRelation semRelation = myFactRelations.get(j);
               // System.out.println("semRelation.getSubject() = " + semRelation.getSubject());

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
                for (int k = 0; k < factRelations.size(); k++) {
                    SemRelation relation = factRelations.get(k);
                    if (globalSemRelationCandidate.match(relation)) {
                        //// merge mentions

                        relation.addMentions(globalSemRelationCandidate.getNafMentions());
                        merge = true;
                        break;
                    }
                }

                if (!merge) {
                    factRelations.add(globalSemRelationCandidate);
                }
            }
        }
        try {
            //System.out.println("pathToNafFolder = " + pathToNafFolder);
            FileOutputStream fos = new FileOutputStream(pathToNafFolder+"/sem.trig");
            GetSemFromNafFile.serializeJena(fos,  semEvents, semActors, semPlaces, semTimes, semRelations, factRelations, sourceMetaHashMap);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void processFolderEvents (String project, File pathToNafFolder, String extension

    ) throws IOException {
        File eventFolder = new File (pathToNafFolder+"/events");
        if (!eventFolder.exists()) {
            eventFolder.mkdir();
        }
        File speechFolder = new File (eventFolder+"/"+"speech");
        if (!speechFolder.exists()) {
            speechFolder.mkdir();
        }
        File otherFolder = new File (eventFolder+"/"+"other");
        if (!otherFolder.exists()) {
            otherFolder.mkdir();
        }
        File grammaticalFolder = new File (eventFolder+"/"+"grammatical");
        if (!grammaticalFolder.exists()) {
            grammaticalFolder.mkdir();
        }

        KafSaxParser kafSaxParser = new KafSaxParser();
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        ArrayList<SemRelation> factRelations = new ArrayList<SemRelation>();

        ArrayList<File> files = Util.makeRecursiveFileList(pathToNafFolder, extension);
        //System.out.println("files.size() = " + files.size());
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            if (!file.getName().startsWith("59V3-4CT1-JB32-V0V2.xml"))  {
           //     continue;
            }
            if (i % 50 == 0) {
                System.out.println("i = " + i);
                System.out.println("file.getName() = " + file.getAbsolutePath());
            }
            semEvents = new ArrayList<SemObject>();
            semActors = new ArrayList<SemObject>();
            semTimes = new ArrayList<SemObject>();
            semPlaces = new ArrayList<SemObject>();
            semRelations = new ArrayList<SemRelation>();
            factRelations = new ArrayList<SemRelation>();
            kafSaxParser.parseFile(file.getAbsolutePath());
            GetSemFromNafFile.processNafFile(project, kafSaxParser, semEvents, semActors, semPlaces, semTimes, semRelations, factRelations);

            // We need to create output objects that are more informative than the Trig output and store these in files per date

            ArrayList<SemObject> times = new ArrayList<SemObject>();
            for (int j = 0; j < semEvents.size(); j++) {
                    SemObject mySemEvent = semEvents.get(j);
                    ArrayList<SemObject> myTimes = getMySemObjects(mySemEvent, semRelations, semTimes);
                    ArrayList<SemObject> myPlaces = getMySemObjects(mySemEvent, semRelations, semPlaces);
                    ArrayList<SemObject> myActors = getMySemObjects(mySemEvent, semRelations, semActors);
                    ArrayList<SemRelation> myRelations = getMySemRelations(mySemEvent, semRelations);
                    ArrayList<SemRelation> myFacts = getMySemRelations(mySemEvent, factRelations);
                    CompositeEvent compositeEvent = new CompositeEvent(mySemEvent, myActors, myPlaces, myTimes, myRelations, myFacts);
                    File folder = otherFolder;
                    for (int k = 0; k < mySemEvent.getConcepts().size(); k++) {
                        KafSense kafSense = mySemEvent.getConcepts().get(k);
                        if (kafSense.getSensecode().equalsIgnoreCase("speech-cognition")) {
                            folder = speechFolder;
                            break;
                        }
                        else if (kafSense.getSensecode().equalsIgnoreCase("communication")) {
                            folder = speechFolder;
                            break;
                        }
                        else if (kafSense.getSensecode().equalsIgnoreCase("cognition")) {
                            folder = speechFolder;
                            break;
                        }
                        else if (kafSense.getSensecode().equalsIgnoreCase("grammatical")) {
                            folder = grammaticalFolder;
                            break;
                        }
                    }
                    File timeFile  = null;

                   // eventFos.writeObject(compositeEvent);
                    /// now we need to write the event data and relations to the proper time folder for comparison
                    if (myTimes.size()==0) {
                        /// timeless
                        timeFile = new File(folder.getAbsolutePath() + "/" + "events-"+"timeless"+ ".obj");

                    }
                    else if (myTimes.size()==1) {
                        /// time: same year or exact?
                        String timePhrase = OwlTime.getYearFromString(myTimes.get(0).getPhrase());
                        timeFile = new File(folder.getAbsolutePath() + "/" + "events"+timePhrase+ ".obj");
                    }
                    else {
                        /// special case if multiple times, what to do? create a period?
                        //// ?????
                        //ArrayList<String> coveredYears = new ArrayList<String>();
                        TreeSet<String> treeSet = new TreeSet<String>();
                        String timePhrase = "";
                        for (int k = 0; k < myTimes.size(); k++) {
                            SemObject semObject = myTimes.get(k);
                            String year = OwlTime.getYearFromString(semObject.getPhrase());
                            if (!treeSet.contains(year)) {
                                treeSet.add(year);
                            }
                        }
                        if (treeSet.size()>4 && folder.getName().equals("other")) {
                          //  System.out.println("mySemEvent = " + mySemEvent.getPhrase());
                            for (int k = 0; k < mySemEvent.getNafMentions().size(); k++) {
                                NafMention nafMention = mySemEvent.getNafMentions().get(k);
                             //   System.out.println("nafMention.toString() = " + nafMention.toString());
                            }
                            for (int k = 0; k < myTimes.size(); k++) {
                                SemObject semObject = myTimes.get(k);
                                for (int l = 0; l < semObject.getNafMentions().size(); l++) {
                                    NafMention nafMention = semObject.getNafMentions().get(l);
                              //      System.out.println("time nafMention.toString() = " + nafMention.toString());
                                }
                            }
                        }
                        Iterator keys = treeSet.iterator();
                        while (keys.hasNext()) {
                            timePhrase +="-"+keys.next();
                        }
                        timeFile = new File(folder.getAbsolutePath() + "/" + "events"+timePhrase+ ".obj");
                    }
                    if (timeFile!=null) {
                        if (timeFile.exists()) {
                          //  System.out.println("appending to timeFile.getName() = " + timeFile.getName());
                            OutputStream os = new FileOutputStream(timeFile, true);
                            ObjectOutputStream eventFos = new ObjectOutputStream(os);
                            try {
                                eventFos.writeObject(compositeEvent);
                            } catch (IOException e) {
                                //e.printStackTrace();
                            }
                            os.close();
                            eventFos.close();
                        } else {
                         //   System.out.println("timeFile.getName() = " + timeFile.getName());
                            OutputStream os = new FileOutputStream(timeFile);
                            ObjectOutputStream eventFos = new ObjectOutputStream(os);
                            try {
                                eventFos.writeObject(compositeEvent);
                            } catch (IOException e) {
                               // e.printStackTrace();
                            }
                            os.close();
                            eventFos.close();
                        }
                    }
                }
        }

    }

    public static void processFolderForEntities (String project, File pathToNafFolder, String extension, double conceptMatchThreshold,
                                      double phraseMatchThreshold

    ) {
        KafSaxParser kafSaxParser = new KafSaxParser();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> semPlaces = new ArrayList<SemObject>();
        ArrayList<SemObject> mySemActors = new ArrayList<SemObject>();
        ArrayList<SemObject> mySemTimes = new ArrayList<SemObject>();
        ArrayList<SemObject> mySemPlaces = new ArrayList<SemObject>();

        //FileOutputStream timelessEventsFos = new FileOutputStream(pathToNafFolder+"/timelesssem.trig");
        ArrayList<File> files = Util.makeRecursiveFileList(pathToNafFolder, extension);
        //System.out.println("files.size() = " + files.size());
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            if (i%50==0) {
                System.out.println("i = " + i);
                System.out.println("file.getName() = " + file.getAbsolutePath());
                System.out.println("semActors = " + semActors.size());
                System.out.println("semTimes = " + semTimes.size());
                System.out.println("semPlaces = " + semPlaces.size());
            }
            mySemActors = new ArrayList<SemObject>();
            mySemTimes = new ArrayList<SemObject>();
            mySemPlaces = new ArrayList<SemObject>();
            kafSaxParser.parseFile(file.getAbsolutePath());
            GetSemFromNafFile.processNafFileForEntities(project, kafSaxParser, mySemActors, mySemPlaces, mySemTimes);

            for (int j = 0; j < mySemActors.size(); j++) {
                SemObject mySemActor = mySemActors.get(j);
                boolean merge = false;
                for (int k = 0; k < semActors.size(); k++) {
                    SemObject semActor = semActors.get(k);
                    double phraseMatch = mySemActor.matchObjectByPhrases((semActor));
                    if (phraseMatch>=phraseMatchThreshold) {
                        merge = true;
                        semActor.mergeSemObject(mySemActor);
                        break;
                    }
                    else {
                        if (conceptMatchThreshold>0)  {
                            double conceptMatch = mySemActor.matchObjectByConcepts((semActor));
                            if (conceptMatch>=conceptMatchThreshold) {
                                merge = true;
                                semActor.mergeSemObject(mySemActor);
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
                        break;
                    }
                    else {
                        if (conceptMatchThreshold>0)  {
                            double conceptMatch = mySemPlace.matchObjectByConcepts((semPlace));
                            if (conceptMatch>=conceptMatchThreshold) {
                                merge = true;
                                semPlace.mergeSemObject(mySemPlace);
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
                        break;
                    }
                    else {
                        if (conceptMatchThreshold>0)  {
                            double conceptMatch = mySemTime.matchObjectByConcepts((semTime));
                            if (conceptMatch>=conceptMatchThreshold) {
                                merge = true;
                                semTime.mergeSemObject(mySemTime);
                                break;
                            }
                        }
                    }
                }

                if (!merge) {
                    semTimes.add(mySemTime);
                }
            }
        }
        try {
            //System.out.println("pathToNafFolder = " + pathToNafFolder);
            FileOutputStream fos = new FileOutputStream(pathToNafFolder+"/sem-entities.trig");
            GetSemFromNafFile.serializeJenaEntities(fos, semActors, semPlaces, semTimes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


}
