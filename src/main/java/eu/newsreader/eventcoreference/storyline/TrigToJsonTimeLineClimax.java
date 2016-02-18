package eu.newsreader.eventcoreference.storyline;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.newsreader.eventcoreference.input.*;
import eu.newsreader.eventcoreference.util.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 1/3/14.
 */
public class TrigToJsonTimeLineClimax {

    static TrigTripleData trigTripleData = new TrigTripleData();
    static Dataset dataset = TDBFactory.createDataset();
    static HashMap<String, ArrayList<String>> iliMap = new HashMap<String, ArrayList<String>>();
    static ArrayList<String> blacklist = new ArrayList<String>();
    static String ACTORNAMESPACES = "";
    static boolean ALL = false; /// if true we do not filter events
    static EsoReader esoReader = new EsoReader();
    static FrameNetReader frameNetReader = new FrameNetReader();
    static ArrayList<String> topFrames = new ArrayList<String>();
    static int fnLevel = 0;
    static int esoLevel = 0;
    static int climaxThreshold = 0;
    static String entityFilter = "";
    static HashMap <String, Integer> actorCount = new HashMap<String, Integer>();
    static Integer actorThreshold = -1;
    static int topicThreshold = 0;
    static int nEvents = 0;
    static int nActors = 0;
    static int nStories = 0;
    static String year = "";
    static String KS = "nwr/wikinews-new";

    static public void main (String[] args) {
        trigTripleData = new TrigTripleData();
        String project = "NewsReader storyline";
        String pathToILIfile = "";
        String query = "";
        String kslimit = "";
        String trigfolder = "";
        String trigfile = "";
        String pathToRawTextIndexFile = "";
        String blackListFile = "";
        String fnFile = "";
        String esoFile = "";
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
            else if (arg.equals("--query") && args.length>(i+1)) {
                query = args[i+1];
                entityFilter = query;
            }
            else if (arg.equals("--year") && args.length>(i+1)) {
                year = args[i+1];
            }
            else if (arg.equals("--ks") && args.length>(i+1)) {
                KS = args[i+1];
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
                ACTORNAMESPACES = args[i+1];
               // System.out.println("ACTORNAMESPACES = " + ACTORNAMESPACES);
            }

            else if (arg.equals("--entity") && args.length>(i+1)) {
                entityFilter = args[i+1];
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
        System.out.println("ACTORNAMESPACES = " + ACTORNAMESPACES.toString());
        System.out.println("actorThreshold = " + actorThreshold);
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
        else if (!query.isEmpty()) {
            System.out.println("querying KnowledgeStore for stories = " + query);
            long startTime = System.currentTimeMillis();
            if (!KS.isEmpty()) {
                TrigKSTripleReader.setServicePoint(KS);
            }
            if (!kslimit.isEmpty()) {
                TrigKSTripleReader.limit = kslimit;
            }
            if (ALL) {
                trigTripleData = TrigKSTripleReader.readTriplesFromKSforEntity(query, "");
            }
            else {
                trigTripleData = TrigKSTripleReader.readTriplesFromKSforEntity(query, ACTORNAMESPACES.toLowerCase());
            }

            long estimatedTime = System.currentTimeMillis() - startTime;

            System.out.println("Time elapsed:");
            System.out.println(estimatedTime/1000.0);
        }
        else {
            System.out.println("NO INPUT. NOTHING TO TELL");
        }
        try {
            ArrayList<JSONObject> jsonObjects = getJSONObjectArray();
           // System.out.println("pathToRawTextIndexFile = " + pathToRawTextIndexFile);


            int nActors = JsonStoryUtil.countActors(jsonObjects);
            int nMentions = JsonStoryUtil.countMentions(jsonObjects);

            if (!query.isEmpty()) {
                JsonSerialization.writeJsonObjectArrayForQuery(KS, query, project, jsonObjects, nEvents, nStories, nActors, nMentions);
            }
            else {
                if (!pathToRawTextIndexFile.isEmpty()) {
                    ArrayList<String> rawTextArrayList = Util.ReadFileToStringArrayList(pathToRawTextIndexFile);
                    JsonSerialization.writeJsonObjectArray(trigfolder, project, jsonObjects, rawTextArrayList, nEvents, nStories, nActors, nMentions);

                }
                else {
                    if (trigfile.isEmpty()) {
                        JsonSerialization.writeJsonObjectArray(trigfolder, project, jsonObjects, nEvents, nStories, nActors, nMentions);
                    } else {
                        JsonSerialization.writeJsonObjectArray(trigfile, project, jsonObjects, nEvents, nStories, nActors, nMentions);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    static ArrayList<JSONObject> getJSONObjectArray() throws JSONException {
        ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<JSONObject>();

        Set keySet = trigTripleData.tripleMapInstances.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next(); //// this is the subject of the triple which should point to an event
            ArrayList<Statement> instanceTriples = trigTripleData.tripleMapInstances.get(key);
            if (trigTripleData.tripleMapOthers.containsKey( key)) {
                /// this means it is an instance and has semrelations
                ArrayList<Statement> otherTriples = trigTripleData.tripleMapOthers.get(key);
                if (JsonFromRdf.hasActor(otherTriples) || ALL) {
                    /// we ignore events without actors.....
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("event", JsonFromRdf.getValue(JsonFromRdf.getSynsetsFromIli(key, iliMap)));
                    // jsonObject.put("instance", getValue(key));
                    jsonObject.put("instance", key); /// needs to be the full key otherwise not unique
                    String timeAnchor = JsonFromRdf.getTimeAnchor(trigTripleData.tripleMapInstances, otherTriples);
                    //System.out.println("timeAnchor = " + timeAnchor);
                    int idx = timeAnchor.lastIndexOf("/");
                    if (idx>-1) {
                        timeAnchor = timeAnchor.substring(idx+1);
                    }
                    if (timeAnchor.length()==6) {
                        //// this is a month so we pick the first day of the month
                        timeAnchor+= "01";
                    }if (timeAnchor.length()==4) {
                        //// this is a year so we pick the first day of the year
                        timeAnchor+= "0101";
                    }
                    if (timeAnchor.length()==3 || timeAnchor.length()==5 || timeAnchor.length()==7) {
                        ///date error, e.g. 12-07-198"
                        continue;
                    }
                    ///skipping historic events
                    if (timeAnchor.startsWith("19") || timeAnchor.startsWith("20")) {
                        jsonObject.put("time", timeAnchor);

                        JSONObject jsonClasses = JsonFromRdf.getClassesJSONObjectFromInstanceStatement(instanceTriples);
                        if (jsonClasses.keys().hasNext()) {
                            /// TAKE THIS OUT TO SAVE SPACE
                            jsonObject.put("classes", jsonClasses);
                        }

                        if (fnLevel > 0) {
                            JsonFromRdf.getFrameNetSuperFramesJSONObjectFromInstanceStatement(frameNetReader, topFrames, jsonObject, instanceTriples);
                        } else if (esoLevel > 0) {
                            JsonFromRdf.getEsoSuperClassesJSONObjectFromInstanceStatement(esoReader, esoLevel, jsonObject, instanceTriples);
                        }

                        JSONObject jsonLabels = JsonFromRdf.getLabelsJSONObjectFromInstanceStatement(instanceTriples);
                        if (jsonLabels.keys().hasNext()) {
                            jsonObject.put("labels", jsonLabels.get("labels"));
                        }
                        JSONObject jsonprefLabels = JsonFromRdf.getPrefLabelsJSONObjectFromInstanceStatement(instanceTriples);
                        if (jsonprefLabels.keys().hasNext()) {
                            jsonObject.put("prefLabel", jsonprefLabels.get("prefLabel"));
                        }
                        JSONObject jsonMentions = JsonFromRdf.getMentionsJSONObjectFromInstanceStatement(instanceTriples);
                        if (jsonMentions.keys().hasNext()) {
                            jsonObject.put("mentions", jsonMentions.get("mentions"));
                        }
                        JSONObject actors = JsonFromRdf.getActorsJSONObjectFromInstanceStatement(otherTriples);
                        if (actors.keys().hasNext()) {
                            jsonObject.put("actors", actors);
                        }
                        JSONObject topics = JsonFromRdf.getTopicsJSONObjectFromInstanceStatement(instanceTriples);
                        if (topics.keys().hasNext()) {
                            //  System.out.println("topics.length() = " + topics.length());
                            jsonObject.put("topics", topics.get("topics"));
                        }
                        jsonObjectArrayList.add(jsonObject);
                    }

                }
                else {
                  //  System.out.println("no actor relations in otherTriples.size() = " + otherTriples.size());
                }
            }
            else {
              //  System.out.println("No sem relations for = " + key);
            }
        }
        try {

            System.out.println("Events in SEM-RDF files = " + jsonObjectArrayList.size());
            jsonObjectArrayList = filterEventsForBlackList (jsonObjectArrayList, blacklist);
            System.out.println("Events after blacklist filter= " + jsonObjectArrayList.size());
            //jsonObjectArrayList = filterEventsForActors(jsonObjectArrayList);
            //System.out.println("Events after actor count filter = " + jsonObjectArrayList.size());
            jsonObjectArrayList = JsonStoryUtil.createStoryLinesForJSONArrayList(jsonObjectArrayList,
                    topicThreshold,
                    climaxThreshold,
                    entityFilter,
                    nStories);
            System.out.println("Events after storyline filter = " + jsonObjectArrayList.size());
            nEvents = jsonObjectArrayList.size();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObjectArrayList;
    }

    static ArrayList<JSONObject> filterEventsForActors(ArrayList<JSONObject> events,
                                                       String namespace,
                                                       int actorThreshold) throws JSONException {
        ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<JSONObject>();
        /*
        "actors":{"pb/A0":["http://www.newsreader-project.eu/data/timeline/non-entities/to_a_single_defense_contractor"]}
         */
        for (int i = 0; i < events.size(); i++) {
            JSONObject event = events.get(i);
            try {
                JSONObject actors = (JSONObject) event.get("actors");
                if (actors!=null) {
                    /// check event for actor namespace
                    boolean hasActorNs = false;
                    boolean hasActorCount = false;
                    Iterator<String> keys = actors.keys();
                    while (keys.hasNext()) {
                        String key = keys.next(); /// this is role
                        /* key = pb/A0
                        key = eso/employment-employee
                        key = fn/Agent
                        key = fn/Item
                        */
                        String ns = key.substring(0, key.indexOf("/"));
                        if (namespace.equalsIgnoreCase("all") ||
                                namespace.isEmpty() ||
                                namespace.indexOf(ns)>-1) {
                            hasActorNs = true;
                        }
                        String actor = (String) actors.getJSONArray(key).get(0); /// this is actor ID

                        if (actorCount.containsKey(actor)) {
                            Integer cnt = actorCount.get(actor);
                            if (cnt>actorThreshold) {
                                hasActorCount = true;
                            }
                        }
                    }
                    if (hasActorNs && hasActorCount) {
                        jsonObjectArrayList.add(event);
                    }
                    else {
                      //  System.out.println("hasActorNs = " + hasActorNs);
                     //   System.out.println("hasActorCount = " + hasActorCount);
                    }
                }
                else {
    /*
                    System.out.println("No actors");
                    System.out.println("jsonObject = " + jsonObject.toString());
    */
                }
            } catch (JSONException e) {
             //   e.printStackTrace();
            }
        }

        return jsonObjectArrayList;
    }

    static ArrayList<JSONObject> filterEventsForBlackList(ArrayList<JSONObject> events, ArrayList<String> blacklist) throws JSONException {
        ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<JSONObject>();
        for (int i = 0; i < events.size(); i++) {
            JSONObject jsonObject = events.get(i);
            JSONArray labels = null;
            try {
                labels = (JSONArray) jsonObject.get("labels");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (labels != null) {
                    for (int j = 0; j < labels.length(); j++) {
                        String label = labels.getString(j);
                       // System.out.println("label = " + label);
                        if (!blacklist.contains(label)) {
                            jsonObjectArrayList.add(jsonObject);
                            break;
                        }
                    }

            } else {

            }
        }
        return jsonObjectArrayList;
    }



}
