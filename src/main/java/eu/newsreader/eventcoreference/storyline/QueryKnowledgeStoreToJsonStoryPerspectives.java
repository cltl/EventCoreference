package eu.newsreader.eventcoreference.storyline;

import eu.newsreader.eventcoreference.input.EsoReader;
import eu.newsreader.eventcoreference.input.FrameNetReader;
import eu.newsreader.eventcoreference.input.TrigKSTripleReader;
import eu.newsreader.eventcoreference.util.EuroVoc;
import eu.newsreader.eventcoreference.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 1/3/14.
 */
@Deprecated
public class QueryKnowledgeStoreToJsonStoryPerspectives {

    static boolean STRICTSTRING = true;
    static HashMap<String, ArrayList<String>> iliMap = new HashMap<String, ArrayList<String>>();
    static ArrayList<String> blacklist = new ArrayList<String>();
    static int DEBUG = 0;
    static boolean ALL = false; /// if true we do not filter events
    static boolean SKIPPEVENTS = false; /// if true we we exclude perspective events from the stories
    static boolean MERGE = false;
    static String timeGran = "D";
    static String actionOnt = "";
    static int actionSim = 1;
    static int interSect = 1;
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
    static String KSSERVICE = ""; //https://knowledgestore2.fbk.eu";
    static String KS = ""; //"nwr/wikinews-new";
    static String KSuser = ""; //"nwr/wikinews-new";
    static String KSpass = ""; //"nwr/wikinews-new";
    static String EVENTSCHEMA = "";
    static EuroVoc euroVoc = new EuroVoc();
    static EuroVoc euroVocBlackList = new EuroVoc();
    static String log = "";

    static public void main (String[] args) {
        String project = "NewsReader storyline";
        String pathToILIfile = "";
        String sparqlQuery = "";
        String eventQuery = "";
        String topicQuery = "";
        String wordQuery = "";
        String graspQuery = "";
        String authorQuery = "";
        String citeQuery = "";
        String entityQuery = "";
        String kslimit = "500";
        String pathToFtDataFile = "";
        String blackListFile = "";
        String fnFile = "";
        String esoFile = "";
        String euroVocFile = "";
        String euroVocBlackListFile = "";
        String pathToTokenIndex = "";
        log = "";
        fnLevel = 0;
        esoLevel = 0;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--sparql") && args.length>(i+1)) {
                sparqlQuery = args[i+1];
            }
            else if (arg.equals("--word") && args.length>(i+1)) {
                wordQuery = args[i+1];
            }
            else if (arg.equals("--substring")) {
                STRICTSTRING = false;
            }
            else if (arg.equals("--event") && args.length>(i+1)) {
                eventQuery = args[i+1];
            }
            else if (arg.equals("--entity") && args.length>(i+1)) {
                entityQuery = args[i+1];
            }
            else if (arg.equals("--topic") && args.length>(i+1)) {
                topicQuery = args[i+1];
            }
            else if (arg.equals("--tokens") && args.length>(i+1)) {
                pathToTokenIndex = args[i+1];
            }
            else if (arg.equals("--author") && args.length>(i+1)) {
                authorQuery = args[i+1];
            }
            else if (arg.equals("--cite") && args.length>(i+1)) {
                citeQuery = args[i+1];
            }
            else if (arg.equals("--grasp") && args.length>(i+1)) {
                graspQuery = args[i+1];
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
            else if (arg.equals("--debug") && args.length>(i+1)) {
                try {
                    DEBUG = Integer.parseInt(args[i+1]);
                    TrigKSTripleReader.DEBUG = DEBUG;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
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
            else if (arg.equals("--eurovoc") && args.length>(i+1)) {
                euroVocFile = args[i+1];
                euroVoc.readEuroVoc(euroVocFile,"en");
            }
            else if (arg.equals("--eurovoc-blacklist") && args.length>(i+1)) {
                euroVocBlackListFile = args[i+1];
                euroVocBlackList.readEuroVoc(euroVocBlackListFile, "en");
               // System.out.println("euroVocBlackList = " + euroVocBlackList.uriLabelMap.size());
            }
            else if (arg.equals("--service") && args.length>(i+1)) {
                KSSERVICE = args[i+1];
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
            else if (arg.equals("--project") && args.length>(i+1)) {
                project = args[i+1];
            }
            else if (arg.equals("--ili") && args.length>(i+1)) {
                pathToILIfile = args[i+1];
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
        /*System.out.println("climaxThreshold = " + climaxThreshold);
        System.out.println("topicThreshold = " + topicThreshold);
        System.out.println("actionOnt = " + actionOnt);
        System.out.println("actionSim = " + actionSim);
        System.out.println("actorThreshold = " + actorThreshold);
        System.out.println("actor interSect = " + interSect);
        System.out.println("max results for KnowledgeStore = " + kslimit);
        System.out.println("pathToRawTextIndexFile = " + pathToRawTextIndexFile);
        System.out.println("MERGE = " + MERGE);*/
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

        if (!KSSERVICE.isEmpty()) {
            if (KSuser.isEmpty()) {
                TrigKSTripleReader.setServicePoint(KSSERVICE, KS);
            }
            else {
                TrigKSTripleReader.setServicePoint(KSSERVICE, KS, KSuser, KSpass);
            }
        }
        if (!kslimit.isEmpty()) {
            TrigKSTripleReader.limit = kslimit;
        }

        long startTime = System.currentTimeMillis();
        String ksQueryError = "";
        if (!eventQuery.isEmpty()) {
            log += " -- queried for event = " + eventQuery;
        }
        if (!entityQuery.isEmpty()) {
            log +=  " -- queried for entity = " + entityQuery;
        }
        if (!wordQuery.isEmpty()) {
            log += " -- queried for word = " + wordQuery;
        }
        if (!topicQuery.isEmpty()) {
            log += " -- queried for topic = " + topicQuery;
        }
        if (!authorQuery.isEmpty()) {
            log += " -- queried for author = " + authorQuery;
        }
        if (!citeQuery.isEmpty()) {
            log += " -- queried for cite sources = " + citeQuery;
        }
        if (!year.isEmpty()) {
            log += " -- queried for year = " + year;
        }
        if (!graspQuery.isEmpty()) {
            log += " -- queried for perspective = " + graspQuery;
        }

        if (!sparqlQuery.isEmpty()) {
            //  System.out.println(" * queried with SPARQL = " + sparqlQuery);
            try {
                TrigKSTripleReader.readTriplesFromKs(sparqlQuery);
            } catch (Exception e) {
                ksQueryError = e.getMessage();
                ksQueryError += e.getCause();
            }
        }
        else if (!wordQuery.isEmpty()) {
            try {
                if (wordQuery.indexOf("*")>-1)  {
                    String labels = wordQuery.replace("*", "");
                    TrigKSTripleReader.readTriplesFromKSforSurfaceSubString(labels);
                }
                else if (STRICTSTRING) TrigKSTripleReader.readTriplesFromKSforSurfaceString(wordQuery);
                else {TrigKSTripleReader.readTriplesFromKSforSurfaceSubString(wordQuery); }

            } catch (Exception e) {
                ksQueryError = e.getMessage();
                ksQueryError += e.getCause();
            }
        }
        else {
            String sparql = TrigKSTripleReader.makeSparqlQueryInit();

            //@TODO We cannot combine different types of constraints per facet as OR
            if (!eventQuery.isEmpty()) {
                //@split query into labels and types
                String labels = TrigKSTripleReader.getLabelQueryforEvent(eventQuery);
                String types = TrigKSTripleReader.getTypeQueryforEvent(eventQuery);
                sparql += "{";

                if (!labels.isEmpty()) {
                    if (STRICTSTRING) sparql += TrigKSTripleReader.makeLabelConstraint("?event", labels);
                    else {sparql += TrigKSTripleReader.makeSubStringLabelFilter("?event", labels); }
                   // sparql += "?event rdfs:label ?eventlabel .\n" ;
                }
                if (!types.isEmpty()) {
                    if (!labels.isEmpty()) {
                        sparql += " UNION ";
                    }
                    sparql += TrigKSTripleReader.makeTypeFilter("?event", types);
                }
                sparql += "}";
            }

            //@TODO We cannot combine different types of constraints per facet as OR
            if (!entityQuery.isEmpty()) {
                //split query into types, instances and labels
                //
                String labels = TrigKSTripleReader.getLabelQueryforEntity(entityQuery);
                String types = TrigKSTripleReader.getTypeQueryforEntity(entityQuery);
                String instances = TrigKSTripleReader.getInstanceQueryforEntity(entityQuery);
                sparql += "?event sem:hasActor ?ent .\n";
                sparql += "{";
                if (!labels.isEmpty()) {
                    //makeLabelFilter("?entlabel",entityLabel) +
                    if (labels.indexOf("*")>-1)  {
                        labels = labels.replace("*", "");
                            sparql += TrigKSTripleReader.makeSubStringLabelFilter("?entlabel", labels);
                            sparql += "?ent rdfs:label ?entlabel .\n" ;
                    }
                    else if (STRICTSTRING) {
                        sparql += TrigKSTripleReader.makeLabelConstraint("?ent", labels);
                    }
                    else {
                        sparql += TrigKSTripleReader.makeSubStringLabelFilter("?entlabel", labels);
                        sparql += "?ent rdfs:label ?entlabel .\n" ;
                    }
                }
                if (!instances.isEmpty()) {
                    if (!labels.isEmpty()) {
                        sparql += " UNION ";
                    }
                    sparql += TrigKSTripleReader.makeInstanceFilter("?event", instances);
                           // "?event sem:hasActor ?ent .";

                }
                if (!types.isEmpty()) {
                    if (!labels.isEmpty() || !instances.isEmpty()) {
                        sparql += " UNION ";
                    }
                    sparql += TrigKSTripleReader.makeTypeFilter("?ent", types) ;
                }
                sparql += "}";
            }

            if (!topicQuery.isEmpty()) {
                sparql += TrigKSTripleReader.makeTopicFilter("?event", topicQuery);
            }

         /*
          @TODO implement period filter for events
         */
            if (!year.isEmpty()) {
                sparql += TrigKSTripleReader.makeYearFilter("?time", year) +                            "?ent rdfs:label ?entlabel .\n" +
                        "?event sem:hasTime ?time .\n";
            }

            /*
            <https://twitter.com/139786938/status/529378953065422848>
            prov:wasAttributedTo  nwrauthor:Twitter .

            <https://twitter.com/139786938/status/529378953065422848/source_attribution/Attr10>
            rdf:value              grasp:CERTAIN_NON_FUTURE_POS , grasp:positive ;
            grasp:wasAttributedTo  <http://www.newsreader-project.eu/data/Dasym-Pilot/non-entities/hij> .
             */
            if (!authorQuery.isEmpty()) {
                String sources = TrigKSTripleReader.getsource(authorQuery);
                if (!sources.isEmpty()) {
                    sparql +=
                            "?event gaf:denotedBy ?mention.\n" +
                                    "?mention grasp:hasAttribution ?attribution.\n" +
                                    "?attribution prov:wasAttributedTo ?doc .\n" ;
                                    //"?doc prov:wasAttributedTo ?author .\n";
/*
                    if (STRICTSTRING) sparql += TrigKSTripleReader.makeLabelConstraint("?author", sources);
                    else {sparql += TrigKSTripleReader.makeSubStringLabelFilter("?author", sources); }
*/

                    //sparql += TrigKSTripleReader.makeSubStringLabelFilter("?author", sources);
                    sparql += TrigKSTripleReader.makeAuthorConstraint("?doc", sources);
                }
            }

            if (!citeQuery.isEmpty()) {
                String sources = TrigKSTripleReader.getsource(citeQuery);
                if (!sources.isEmpty()) {
                    sparql +=
                            "?event gaf:denotedBy ?mention.\n" +
                                    "?mention grasp:hasAttribution ?attribution.\n" +
                                    "?attribution grasp:wasAttributedTo ?cite.\n";
/*
                    if (STRICTSTRING) sparql += TrigKSTripleReader.makeLabelConstraint("?cite", sources);
                    else {sparql += TrigKSTripleReader.makeSubStringLabelFilter("?cite", sources); }
*/

                    sparql += TrigKSTripleReader.makeSubStringLabelFilter("?cite", sources);

                }
            }

            /// rdf:value grasp:CERTAIN_NON_FUTURE_POS , grasp:positive ;
            ///graspQuery = NEG;UNCERTAIN;positive;
            if (!graspQuery.isEmpty()) {
                boolean UNION = false;
                sparql += "?event gaf:denotedBy ?mention.\n" +
                        "?mention grasp:hasAttribution ?attribution.\n" +
                        "?attribution rdf:value ?value .\n" ;
                sparql += "{\n";
                if (graspQuery.indexOf("negative") >-1) {
                    sparql +=  "{ ?attribution rdf:value grasp:negative } \n";
                    UNION = true;
                }
                if (graspQuery.indexOf("positive") >-1) {
                    if (UNION) sparql += " UNION ";
                    sparql +=  "{ ?attribution rdf:value grasp:positive }\n";
                    UNION = true;
                }
                String [] fields = graspQuery.split(";");
                for (int i = 0; i < fields.length; i++) {
                    String field = fields[i];
                    if (!field.toLowerCase().equals(field)) {
                        ///upper case field
                        if (UNION) sparql += " UNION ";
                        sparql +=  "{ "+TrigKSTripleReader.makeSubStringLabelUnionFilter("?value", field) +" }"+ "\n";
                        UNION = true;
                    }
                }
                sparql += " }\n";
                if (graspQuery.indexOf("FUTURE") > -1) {
                    sparql += "FILTER(!CONTAINS(STR(?value), \"NONFUTURE\"))\n";
                }
            }

            sparql += TrigKSTripleReader.makeSparqlQueryEnd();
            if (DEBUG>0) System.out.println("sparql = " + sparql);
            try {
                TrigKSTripleReader.readTriplesFromKs(sparql);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (DEBUG>0) {
                System.out.println(" * instance statements = "+TrigKSTripleReader.trigTripleData.tripleMapInstances.size());
                System.out.println(" * sem statements = " + TrigKSTripleReader.trigTripleData.tripleMapOthers.size());
            }
        }

        if (!ksQueryError.isEmpty())  {
            log +=  " -- Error querying KnowledgeStore:"+ksQueryError;
        }
        else {
            long estimatedTime = System.currentTimeMillis() - startTime;

            log += " -- Time elapsed to get results from KS:" + estimatedTime / 1000.0;

            try {
                ArrayList<JSONObject> jsonObjects = JsonStoryUtil.getJSONObjectArray(TrigKSTripleReader.trigTripleData,
                        ALL, SKIPPEVENTS, EVENTSCHEMA, blacklist, iliMap, fnLevel, frameNetReader, topFrames, esoLevel, esoReader);
               // System.out.println(" * Events in SEM-RDF = " + jsonObjects.size());
                if (blacklist.size() > 0) {
                    jsonObjects = JsonStoryUtil.filterEventsForBlackList(jsonObjects, blacklist);
                    //   System.out.println("Events after blacklist filter= " + jsonObjects.size());
                }
                if (actorThreshold > 0) {
                    jsonObjects = JsonStoryUtil.filterEventsForActors(jsonObjects, entityFilter, actorThreshold);
                    //   System.out.println("Events after actor count filter = " + jsonObjects.size());
                }
                //JsonStoryUtil.removeTinyActors(jsonObjects);

/*
            jsonObjects = JsonStoryUtil.removePerspectiveEvents(trigTripleData, jsonObjects);
            System.out.println("Events after removing perspective events = " + jsonObjects.size());
*/

                jsonObjects = JsonStoryUtil.createStoryLinesForJSONArrayList(jsonObjects,
                        topicThreshold,
                        climaxThreshold,
                        entityFilter, MERGE,
                        timeGran,
                        actionOnt,
                        actionSim,
                        interSect);
                //   System.out.println("Events after storyline filter = " + jsonObjects.size());
                //JsonStoryUtil.augmentEventLabelsWithArguments(jsonObjects);

                JsonStoryUtil.minimalizeActors(jsonObjects);
                // System.out.println("eurovoc = " + euroVoc.uriLabelMap.size());
                if (euroVoc.uriLabelMap.size() > 0) {
                    JsonStoryUtil.renameStories(jsonObjects, euroVoc, euroVocBlackList);
                }
                else {
                    System.out.println("no mappings eurovoc = " + euroVoc.uriLabelMap.size() );
                }
                ArrayList<JSONObject> rawTextArrayList = new ArrayList<JSONObject>();
                ArrayList<JSONObject> structuredEvents = new ArrayList<JSONObject>();
                if (jsonObjects.size() > 0) {
                    TrigKSTripleReader.integrateAttributionFromKs(jsonObjects);
                }


                if (!pathToFtDataFile.isEmpty()) {
                    HashMap<String, ArrayList<ReadFtData.DataFt>> dataFtMap = ReadFtData.readData(pathToFtDataFile);
                    structuredEvents = ReadFtData.convertFtDataToJsonEventArray(dataFtMap);
                }

                if (pathToTokenIndex.isEmpty()) {
                    MentionResolver.createSnippetIndexFromMentions(jsonObjects, KSSERVICE, KS, KSuser, KSpass);
                }
                else {
                    log += MentionResolver.createSnippetIndexFromMentions(jsonObjects, pathToTokenIndex);
                }

                nEvents = jsonObjects.size();
                nActors = JsonStoryUtil.countActors(jsonObjects);
                nMentions = JsonStoryUtil.countMentions(jsonObjects);
                nStories = JsonStoryUtil.countGroups(jsonObjects);

                JsonSerialization.writeJsonObjectArrayWithStructuredData("", "", project,
                        jsonObjects, rawTextArrayList, nEvents, nStories, nActors, nMentions, "polls", structuredEvents);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            log += " -- story_cnt = " + nStories+ ", event_cnt = " + nEvents + ", mention_cnt = " + nMentions + ", actor_cnt = " + nActors;
        }
        System.out.print(log);
    }

    static void splitStories (ArrayList<JSONObject> events,
                              ArrayList<JSONObject> rawTextArrayList,
                              ArrayList<JSONObject> structuredEvents,
                              String project,
                              String trigFolder
    ) {

        HashMap<String, ArrayList<JSONObject>> storyMap = new HashMap<String, ArrayList<JSONObject>>();
        for (int i = 0; i < events.size(); i++) {
            JSONObject event = events.get(i);
            try {
                String group = event.getString("group");
                if (storyMap.containsKey(group)) {
                    ArrayList<JSONObject> groupEvents = storyMap.get(group);
                    groupEvents.add(event);
                    storyMap.put(group,groupEvents);
                }
                else {
                    ArrayList<JSONObject> groupEvents = new ArrayList<JSONObject>();
                    groupEvents.add(event);
                    storyMap.put(group,groupEvents);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Set keySet = storyMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while ((keys.hasNext())) {
            String group = keys.next();
            ArrayList<JSONObject> groupEvents = storyMap.get(group);
            int nActors = JsonStoryUtil.countActors(groupEvents);
            int nMentions = JsonStoryUtil.countMentions(groupEvents);
           // System.out.println("group = " + group);
            JsonSerialization.writeJsonObjectArrayWithStructuredData(trigFolder, group, project,
                    groupEvents, rawTextArrayList, groupEvents.size(), 1, nActors, nMentions, "polls", structuredEvents);

        }
    }


}
