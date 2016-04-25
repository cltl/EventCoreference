package eu.newsreader.eventcoreference.storyline;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.newsreader.eventcoreference.input.*;
import eu.newsreader.eventcoreference.util.EuroVoc;
import eu.newsreader.eventcoreference.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by piek on 1/3/14.
 */
public class TrigToJsonPerspectiveStoriesFT {

    static TrigTripleData trigTripleData = new TrigTripleData();
    static Dataset dataset = TDBFactory.createDataset();
    static HashMap<String, ArrayList<String>> iliMap = new HashMap<String, ArrayList<String>>();
    static ArrayList<String> blacklist = new ArrayList<String>();
    static String ACTORTYPE = "";
    static boolean ALL = false; /// if true we do not filter events
    static boolean MERGE = false;
    static String timeGran = "D";
    static String actionOnt = "";
    static int actionSim = 1;
    static int interSect = 1;
    static boolean PERSPECTIVE = false;
    static boolean COMBINE = false;
    static EsoReader esoReader = new EsoReader();
    static FrameNetReader frameNetReader = new FrameNetReader();
    static ArrayList<String> topFrames = new ArrayList<String>();
    static int fnLevel = 0;
    static int esoLevel = 0;
    static int climaxThreshold = 0;
    static String entityFilter = "";
    static Integer actorThreshold = -1;
    static int topicThreshold = 0;
    static int nEvents = 0;
    static int nActors = 0;
    static int nMentions = 0;
    static int nStories = 0;
    static String year = "";
    static String KS = "nwr/wikinews-new";
    static String KSuser = "nwr/wikinews-new";
    static String KSpass = "nwr/wikinews-new";
    static String EVENTSCHEMA = "";
    static EuroVoc euroVoc;

    static public void main (String[] args) {
        trigTripleData = new TrigTripleData();
        String project = "NewsReader storyline";
        String pathToILIfile = "";
        String eventQuery = "";
        String entityQuery = "";
        String kslimit = "500";
        String trigfolder = "";
        String trigfile = "";
        String pathToRawTextIndexFile = "";
        String pathToFtDataFile = "";
        String blackListFile = "";
        String fnFile = "";
        String esoFile = "";
        String euroVocFile = "";
        fnLevel = 0;
        esoLevel = 0;
        //pathToILIfile = "/Users/piek/Desktop/NWR/timeline/vua-naf2jsontimeline_2015/resources/wn3-ili-synonyms.txt";
        //fnFile = "/Users/piek/Desktop/NWR/timeline/vua-naf2jsontimeline_2015/resources/frRelation.xml";
       // fnLevel = 3;
       // esoLevel = 2;
       // trigfolder = "/Users/piek/Desktop/NWR/NWR-ontology/wikinews_NAF_input_noTok_uriOK_0915_v3processed/corpus_stock/events/contextualEvent";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--trig-folder") && args.length>(i+1)) {
                trigfolder = args[i+1];
            }
            else if (arg.equals("--event") && args.length>(i+1)) {
                eventQuery = args[i+1];
            }
            else if (arg.equals("--entity") && args.length>(i+1)) {
                entityQuery = args[i+1];
            }
            else if (arg.equals("--year") && args.length>(i+1)) {
                year = args[i+1];
            }
            else if (arg.equals("--ft") && args.length>(i+1)) {
                pathToFtDataFile = args[i+1];
            }
            else if (arg.equals("--time") && args.length>(i+1)) {
                timeGran = args[i+1];
            }
            else if (arg.equals("--actor-intersect") && args.length>(i+1)) {
                try {
                    interSect = Integer.parseInt(args[i+1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else if (arg.equals("--action-sim") && args.length>(i+1)) {
                try {
                    actionSim = Integer.parseInt(args[i+1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else if (arg.equals("--action-ont") && args.length>(i+1)) {
                actionOnt = args[i+1];
            }
            else if (arg.equals("--action-schema") && args.length>(i+1)) {
                EVENTSCHEMA = args[i+1];
            }
            else if (arg.equals("--merge")) {
                MERGE = true;
            }
            else if (arg.equals("--perspective")) {
                PERSPECTIVE = true;
            }
            else if (arg.equals("--eurovoc") && args.length>(i+1)) {
                euroVocFile = args[i+1];
                euroVoc.readEuroVoc(euroVocFile,"en");
            }
            else if (arg.equals("--combine-with-sem")) {
                COMBINE = true;
            }
            else if (arg.equals("--ks") && args.length>(i+1)) {
                KS = args[i+1];
            }
            else if (arg.equals("--ks-user") && args.length>(i+1)) {
                KSuser = args[i+1];
            }
            else if (arg.equals("--ks-pass") && args.length>(i+1)) {
                KSpass = args[i+1];
            }
            else if (arg.equals("--ks-limit") && args.length>(i+1)) {
                kslimit = args[i+1];
            }
            else if (arg.equals("--trig-file") && args.length>(i+1)) {
                trigfile = args[i+1];
            }
            else if (arg.equals("--ili") && args.length>(i+1)) {
                pathToILIfile = args[i+1];
            }
            else if (arg.equals("--raw-text") && args.length>(i+1)) {
                pathToRawTextIndexFile = args[i+1];
            }
            else if (arg.equals("--black-list") && args.length>(i+1)) {
                blackListFile = args[i+1];
            }
            else if (arg.equals("--actor-cnt") && args.length>(i+1)) {
                actorThreshold = Integer.parseInt(args[i+1]);
            }
            else if (arg.equals("--all")){
                ALL = true;
            }
            else if (arg.equals("--actors") && args.length>(i+1)) {
                ACTORTYPE = args[i+1];
               // System.out.println("ACTORNAMESPACES = " + ACTORNAMESPACES);
            }
            else if (arg.equals("--frame-relations") && args.length>(i+1)) {
                fnFile = args[i+1];
            }
            else if (arg.equals("--frame-level") && args.length>(i+1)) {
                try {
                    fnLevel = Integer.parseInt(args[i+1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else if (arg.equals("--eso-relations") && args.length>(i+1)) {
                esoFile = args[i+1];
            }
            else if (arg.equals("--eso-level") && args.length>(i+1)) {
                try {
                    esoLevel = Integer.parseInt(args[i+1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else if (arg.equals("--climax-level") && args.length>(i+1)) {
                try {
                    climaxThreshold = Integer.parseInt(args[i+1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else if (arg.equals("--topic-level") && args.length>(i+1)) {
                try {
                    topicThreshold = Integer.parseInt(args[i+1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        //trigfolder = "/tmp/naf2jsonWulzvC/events/contextual";
        //System.out.println("fnFile = " + fnFile);
        System.out.println("climaxThreshold = " + climaxThreshold);
        System.out.println("topicThreshold = " + topicThreshold);
        System.out.println("actionOnt = " + actionOnt);
        System.out.println("actionSim = " + actionSim);
        System.out.println("actor type = " + ACTORTYPE);
        System.out.println("actorThreshold = " + actorThreshold);
        System.out.println("actor interSect = " + interSect);
        System.out.println("max results for KnowledgeStore = " + kslimit);
        System.out.println("pathToRawTextIndexFile = " + pathToRawTextIndexFile);
        System.out.println("MERGE = " + MERGE);
        System.out.println("PERSPECTIVE = " + PERSPECTIVE);
        if (!blackListFile.isEmpty()) {
            blacklist = Util.ReadFileToStringArrayList(blackListFile);
        }

        if (!fnFile.isEmpty()) {
            frameNetReader.parseFile(fnFile);
            topFrames = frameNetReader.getTopsFrameNetTree();
            frameNetReader.flatRelations(fnLevel);
        }
        if (!esoFile.isEmpty()) {
            esoReader.parseFile(esoFile);
        }
        iliMap = Util.ReadFileToStringHashMap(pathToILIfile);
        ArrayList<File> trigFiles = new ArrayList<File>();
        if (!trigfolder.isEmpty()) {
            System.out.println("trigfolder = " + trigfolder);
            trigFiles = Util.makeRecursiveFileList(new File(trigfolder), ".trig");
        }
        else if (!trigfile.isEmpty()) {
            System.out.println("trigfile = " + trigfile);
            trigFiles.add(new File(trigfile));
        }
        if (trigFiles.size()>0) {
            System.out.println("trigFiles.size() = " + trigFiles.size());
            trigTripleData = TrigTripleReader.readTripleFromTrigFiles(trigFiles);
        }
        else if (!eventQuery.isEmpty() || !entityQuery.isEmpty()) {
            if (!eventQuery.isEmpty()) {
                System.out.println("querying KnowledgeStore for event = " + eventQuery);
            }
            if (!entityQuery.isEmpty()) {
                System.out.println("querying KnowledgeStore for entity = " + entityQuery);
            }
            long startTime = System.currentTimeMillis();
            if (!KS.isEmpty()) {
                if (KSuser.isEmpty()) {
                    TrigKSTripleReader.setServicePoint(KS);
                }
                else {
                    TrigKSTripleReader.setServicePoint(KS, KSuser, KSpass);
                }
            }
            if (!kslimit.isEmpty()) {
                TrigKSTripleReader.limit = kslimit;
            }
            if (!entityQuery.isEmpty() && eventQuery.isEmpty()) {
                if (ALL) {
                    trigTripleData = TrigKSTripleReader.readTriplesFromKSforEntity(entityQuery);
                }
                else {
                    trigTripleData = TrigKSTripleReader.readTriplesFromKSforEntity(entityQuery, ACTORTYPE.toLowerCase());
                }
            }
            else if (entityQuery.isEmpty() && !eventQuery.isEmpty()) {
                trigTripleData = TrigKSTripleReader.readTriplesFromKSforEventType(eventQuery);
            }
            else if (!entityQuery.isEmpty() && !eventQuery.isEmpty()) {
                trigTripleData = TrigKSTripleReader.readTriplesFromKSforEventEntityType(eventQuery, entityQuery);
            }
            long estimatedTime = System.currentTimeMillis() - startTime;

            System.out.println("Time elapsed:");
            System.out.println(estimatedTime/1000.0);
        }
        else {
            System.out.println("NO INPUT. NOTHING TO TELL");
        }
        try {
            ArrayList<JSONObject> jsonObjects = JsonStoryUtil.getJSONObjectArray(trigTripleData,
                    ALL,EVENTSCHEMA, blacklist, iliMap, fnLevel, frameNetReader, topFrames, esoLevel, esoReader);
            System.out.println("Events in SEM-RDF files = " + jsonObjects.size());
            if (blacklist.size()>0) {
                jsonObjects = JsonStoryUtil.filterEventsForBlackList(jsonObjects, blacklist);
                System.out.println("Events after blacklist filter= " + jsonObjects.size());
            }
            if (actorThreshold>0) {
                jsonObjects = JsonStoryUtil.filterEventsForActors(jsonObjects, entityFilter, actorThreshold);
                System.out.println("Events after actor count filter = " + jsonObjects.size());
            }

            jsonObjects = JsonStoryUtil.createStoryLinesForJSONArrayList(jsonObjects,
                    topicThreshold,
                    climaxThreshold,
                    entityFilter, MERGE,
                    timeGran,
                    actionOnt,
                    actionSim,
                    interSect);
            System.out.println("Events after storyline filter = " + jsonObjects.size());

            JsonStoryUtil.minimalizeActors(jsonObjects);
            System.out.println("eurovoc = " + euroVoc.uriLabelMap.size());
            if (euroVoc.uriLabelMap.size()>0) {
                JsonStoryUtil.renameStories(jsonObjects, euroVoc);
            }
            nEvents = jsonObjects.size();

            nActors = JsonStoryUtil.countActors(jsonObjects);
            nMentions = JsonStoryUtil.countMentions(jsonObjects);
            nStories = JsonStoryUtil.countGroups(jsonObjects);
            ArrayList<JSONObject> rawTextArrayList = new ArrayList<JSONObject>();
            ArrayList<JSONObject> perspectiveEvents = new ArrayList<JSONObject>();
            ArrayList<JSONObject> structuredEvents = new ArrayList<JSONObject>();
            if (PERSPECTIVE) {
                if (!entityQuery.isEmpty() || !eventQuery.isEmpty()) {
                    System.out.println("Getting perspectives for: " + jsonObjects.size() + " events");
                    if (COMBINE) {
                        TrigKSTripleReader.integrateAttributionFromKs(jsonObjects);
                    }
                    else {
                        perspectiveEvents = TrigKSTripleReader.readAttributionFromKs(jsonObjects);
                    }
                }
                else {
                    if (COMBINE) {
                        JsonStoryUtil.integratePerspectivesInEventObjects(trigTripleData, jsonObjects);
                    }
                    else {
                        perspectiveEvents = JsonStoryUtil.getPerspectiveEvents(trigTripleData, jsonObjects);
                    }
                }
            }


            if (!pathToFtDataFile.isEmpty()) {
                HashMap<String, ArrayList<ReadFtData.DataFt>> dataFtMap = ReadFtData.readData(pathToFtDataFile);
                structuredEvents = ReadFtData.convertFtDataToJsonEventArray(dataFtMap);
            }


            if (!pathToRawTextIndexFile.isEmpty()) {
                rawTextArrayList = Util.ReadFileToUriTextArrayList(pathToRawTextIndexFile);
/*                JsonSerialization.writeJsonObjectArray(trigfolder, project, jsonObjects, rawTextArrayList,
                        nEvents, nStories, nActors, nMentions);*/
            }
            else {
                if (!eventQuery.isEmpty() || !entityQuery.isEmpty()) {
                    rawTextArrayList = MentionResolver.createRawTextIndexFromMentions(jsonObjects, KS, KSuser, KSpass);
/*
                    JsonSerialization.writeJsonObjectArrayForQuery(KS, eventQuery+entityQuery, project, jsonObjects,
                            nEvents, nStories, nActors, nMentions, KSuser, KSpass);
*/
                }

            }

            System.out.println("jsonObjects = " + jsonObjects.size());
            System.out.println("rawTextArrayList = " + rawTextArrayList.size());
            System.out.println("structuredEvents.size() = " + structuredEvents.size());
            JsonSerialization.writeJsonObjectArrayWithStructuredData(trigfolder, project,
                    jsonObjects, rawTextArrayList, nEvents, nStories, nActors, nMentions, "polls", structuredEvents);


            if (!COMBINE) {
                if (PERSPECTIVE && perspectiveEvents.size()>0) {
                    JsonSerialization.writeJsonPerspectiveArray(trigfolder, project, perspectiveEvents);
                }
                if (!pathToFtDataFile.isEmpty() && structuredEvents.size()>0) {
                    JsonSerialization.writeJsonStructuredArray(trigfolder, project, structuredEvents);
                }
            }
/*            if (rawTextArrayList.size()==0) {
                if (trigfile.isEmpty()) {
                    JsonSerialization.writeJsonObjectArray(trigfolder, project, jsonObjects,
                            nEvents, nStories, nActors, nMentions);
                } else {
                    JsonSerialization.writeJsonObjectArray(trigfile, project, jsonObjects,
                            nEvents, nStories, nActors, nMentions);
                }
            }
            else {
                if (COMBINE && structuredEvents!=null && structuredEvents.size()>0) {
                    JsonSerialization.writeJsonObjectArray(trigfolder, project, jsonObjects,
                            rawTextArrayList,
                            nEvents, nStories, nActors, nMentions);
                }
            }*/



        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("story_cnt = " + nStories);
        System.out.println("event_cnt = " + nEvents);
        System.out.println("mention_cnt = "+ nMentions);
        System.out.println("actor_cnt = " + nActors);
    }




}
