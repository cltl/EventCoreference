package eu.newsreader.eventcoreference.input;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.newsreader.eventcoreference.naf.CreateMicrostory;
import eu.newsreader.eventcoreference.objects.JsonEvent;
import eu.newsreader.eventcoreference.objects.PhraseCount;
import eu.newsreader.eventcoreference.util.RoleLabels;
import eu.newsreader.eventcoreference.util.Util;
import org.apache.jena.riot.RDFDataMgr;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by piek on 1/3/14.
 */
public class TrigToJsonTimeLineClimax {


    public static class Triple {
        private String subject;
        private String predicate;
        private String object;

        public Triple() {
            this.subject = "";
            this.object = "";
            this.predicate = "";
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getObject() {
            return object;
        }

        public void setObject(String object) {
            this.object = object;
        }

        public String getPredicate() {
            return predicate;
        }

        public void setPredicate(String predicate) {
            this.predicate = predicate;
        }
    }

    static Dataset dataset = TDBFactory.createDataset();
    static final String provenanceGraph = "http://www.newsreader-project.eu/provenance";
    static final String instanceGraph = "http://www.newsreader-project.eu/instances";
    static HashMap<String, ArrayList<Statement>> tripleMapProvenance = new HashMap<String, ArrayList<Statement>>();
    static HashMap<String, ArrayList<Statement>> tripleMapInstances = new HashMap<String, ArrayList<Statement>>();
    static HashMap<String, ArrayList<Statement>> tripleMapOthers = new HashMap<String, ArrayList<Statement>>();
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



    static public void main (String[] args) {
        String project = "NewsReader timeline";
        String pathToILIfile = "";
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
        System.out.println("trigfolder = " + trigfolder);
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
        if (trigfile.isEmpty()) {
            trigFiles = Util.makeRecursiveFileList(new File(trigfolder), ".trig");
        }
        else {
            trigFiles.add(new File(trigfile));
        }
        System.out.println("trigFiles.size() = " + trigFiles.size());
        for (int i = 0; i < trigFiles.size(); i++) {
            File file = trigFiles.get(i);
            //System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
            dataset = RDFDataMgr.loadDataset(file.getAbsolutePath());
            Iterator<String> it = dataset.listNames();
            while (it.hasNext()) {
                String name = it.next();
                // System.out.println("name = " + name);
                if (name.equals(provenanceGraph)) {
                    Model namedModel = dataset.getNamedModel(name);
                    StmtIterator siter = namedModel.listStatements();
                    while (siter.hasNext()) {
                        Statement s = siter.nextStatement();
                        String subject = s.getSubject().getURI();
                        if (tripleMapProvenance.containsKey(subject)) {
                            ArrayList<Statement> triples = tripleMapProvenance.get(subject);
                            triples.add(s);
                            tripleMapProvenance.put(subject, triples);
                        }
                        else {

                            ArrayList<Statement> triples = new ArrayList<Statement>();
                            triples.add(s);
                            tripleMapProvenance.put(subject, triples);
                        }
                    }
                }
                else if (name.equals(instanceGraph)) {
                    Model namedModel = dataset.getNamedModel(name);
                    StmtIterator siter = namedModel.listStatements();
                    while (siter.hasNext()) {
                        Statement s = siter.nextStatement();
                        String subject = s.getSubject().getURI();
                        if (tripleMapInstances.containsKey(subject)) {
                            ArrayList<Statement> triples = tripleMapInstances.get(subject);
                            triples.add(s);
                            tripleMapInstances.put(subject, triples);
                        }
                        else {

                            ArrayList<Statement> triples = new ArrayList<Statement>();
                            triples.add(s);
                            tripleMapInstances.put(subject, triples);
                        }
                    }
                }
                else {
                    Model namedModel = dataset.getNamedModel(name);
                    StmtIterator siter = namedModel.listStatements();
                    while (siter.hasNext()) {
                        Statement s = siter.nextStatement();
                        String subject = s.getSubject().getURI();
                        if (tripleMapOthers.containsKey(subject)) {
                            ArrayList<Statement> triples = tripleMapOthers.get(subject);
                            triples.add(s);
                            tripleMapOthers.put(subject, triples);
                        } else {

                            ArrayList<Statement> triples = new ArrayList<Statement>();
                            triples.add(s);
                            tripleMapOthers.put(subject, triples);
                        }
                    }
                }
            }
            dataset.close();
            dataset = null;
        }
        try {
            ArrayList<JSONObject> jsonObjects = getJSONObjectArray();
           // System.out.println("pathToRawTextIndexFile = " + pathToRawTextIndexFile);
            if (!pathToRawTextIndexFile.isEmpty()) {
                ArrayList<String> rawTextArrayList = Util.ReadFileToStringArrayList(pathToRawTextIndexFile);
                writeJsonObjectArray(trigfolder, project, jsonObjects, rawTextArrayList);

            }//System.out.println("jsonObjects.size() = " + jsonObjects.size());
            else {
                if (trigfile.isEmpty()) {
                    writeJsonObjectArray(trigfolder, project, jsonObjects);
                }
                else {
                    writeJsonObjectArray(trigfile, project, jsonObjects);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static void getEntityEvents (Dataset dataset, String name) {
        ArrayList<String> events = new ArrayList<String>();
        Model namedModel = dataset.getNamedModel(name);
        StmtIterator siter = namedModel.listStatements();
        while (siter.hasNext()) {
            Statement s = siter.nextStatement();
            String subject = s.getSubject().getURI();
            if (TrigUtil.getObjectValue(s).toLowerCase().indexOf(entityFilter.toLowerCase()) >-1) {
                if (!events.contains(subject)) {
                    events.add(subject);
                }
            }
        }
        siter = namedModel.listStatements();
        while (siter.hasNext()) {
            Statement s = siter.nextStatement();
            String subject = s.getSubject().getURI();
            if (events.contains(subject)) {
                if (tripleMapOthers.containsKey(subject)) {
                    ArrayList<Statement> triples = tripleMapOthers.get(subject);
                    triples.add(s);
                    tripleMapOthers.put(subject, triples);
                } else {

                    ArrayList<Statement> triples = new ArrayList<Statement>();
                    triples.add(s);
                    tripleMapOthers.put(subject, triples);
                }
            }
        }
    }

    static String getInstanceType (String subject) {
        String type = "";
        if (subject.indexOf("dbpedia.org")>-1) {
            type = "DBP";
        }
        else if (subject.indexOf("/entities/")>-1) {
            type = "ENT";
        }
        else if (subject.indexOf("ili-30")>-1) {
            type = "IEV";
        }
        else if (subject.indexOf("#ev")>-1) {
            type = "LEV";
        }
        else if (subject.indexOf("http://www.newsreader-project.eu/time/")>-1) {
            type = "DATE";
        }

        return type;
    }


    static boolean hasActor (ArrayList<Statement> triples) {
        for (int i = 0; i < triples.size(); i++) {
            Statement s = triples.get(i);
            String predicate = s.getPredicate().getURI();
            if (predicate.toLowerCase().endsWith("hasactor")) {
                return true;
            }
            else {
              //  System.out.println("predicate = " + predicate);
            }
        }
        return false;
    }

    static boolean hasILI (ArrayList<Statement> triples) {
        for (int i = 0; i < triples.size(); i++) {
            Statement statement = triples.get(i);
            String object = "";
            if (statement.getObject().isLiteral()) {
                object = statement.getObject().asLiteral().toString();
            } else if (statement.getObject().isURIResource()) {
                object = statement.getObject().asResource().getURI();
            }
            if (object.indexOf("ili-")>-1) {
                return true;
            }
        }
        return false;
    }

    static boolean hasFrameNet (ArrayList<Statement> triples) {
        for (int i = 0; i < triples.size(); i++) {
            Statement statement = triples.get(i);
            String object = "";
            if (statement.getObject().isLiteral()) {
                object = statement.getObject().asLiteral().toString();
            } else if (statement.getObject().isURIResource()) {
                object = statement.getObject().asResource().getURI();
            }
            if (object.indexOf("framenet")>-1) {
                return true;
            }
        }
        return false;
    }

    static String getTimeAnchor (ArrayList<Statement> triples) {
        for (int i = 0; i < triples.size(); i++) {
            Statement statement = triples.get(i);
            String predicate = statement.getPredicate().getURI();
            if (predicate.toLowerCase().endsWith("time")) {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                if (tripleMapInstances.containsKey( object)) {
                    ArrayList<Statement> instanceTriples = tripleMapInstances.get(object);
                    for (int j = 0; j < instanceTriples.size(); j++) {
                        Statement timeStatement = instanceTriples.get(j);
                        /**    WHAT TO DO WITH PERIODS????
                         *             time:hasBeginning  <http://www.newsreader-project.eu/time/20100125> ;
                                       time:hasEnd        <http://www.newsreader-project.eu/time/20100125> .

                         */
                        if (timeStatement.getPredicate().getURI().toLowerCase().endsWith("indatetime")
                            ||timeStatement.getPredicate().getURI().toLowerCase().endsWith("hasbeginning")
                            || timeStatement.getPredicate().getURI().toLowerCase().endsWith("hasend")) {
                            if (timeStatement.getObject().isLiteral()) {
                                return timeStatement.getObject().asLiteral().toString();
                            } else if (statement.getObject().isURIResource()) {
                                return timeStatement.getObject().asResource().getURI();
                            }
                        }
                    }
                }

            }
        }
        return "NOTIMEANCHOR";
    }

    //        TimeLanguage.setLanguage(kafSaxParser.getLanguage());

    static public String getSynsetsFromIli (String key) {
        String synset = "";
        int idx = key.lastIndexOf("/");
        if (idx>-1) key = key.substring(idx+1);
        synset = key;
        ArrayList<String> synsetArray = new ArrayList<String>();
        String [] ilis = key.split("-and-");
        for (int i = 0; i < ilis.length; i++) {
            String ili = ilis[i];
            if (iliMap.containsKey(ili)) {
                ArrayList<String> syns = iliMap.get(ili);
                for (int j = 0; j < syns.size(); j++) {
                    String s = syns.get(j);
                    s = s.substring(0, s.indexOf("%"));
                    if (!synsetArray.contains(s)) {
                        synsetArray.add(s);
                    }
                }
            }
        }
        if (synsetArray.size()>0) {
            synset = "";
            for (int i = 0; i < synsetArray.size(); i++) {
                String s = synsetArray.get(i);
                if (!synset.isEmpty()) {
                    synset+= ";";
                }
                synset += s;
            }
        }
        return synset;
    }

    static ArrayList<JSONObject> getJSONObjectArray() throws JSONException {
        ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<JSONObject>();

        Set keySet = tripleMapInstances.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next(); //// this is the subject of the triple which should point to an event
            ArrayList<Statement> instanceTriples = tripleMapInstances.get(key);
            if (hasILI(instanceTriples) || hasFrameNet(instanceTriples) || ALL) {
                if (tripleMapOthers.containsKey( key)) {
                    ArrayList<Statement> otherTriples = tripleMapOthers.get(key);
                    if (hasActor(otherTriples) || ALL) {
                        /// we ignore events without actors.....
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("event", getValue(getSynsetsFromIli(key)));
                       // jsonObject.put("instance", getValue(key));
                        jsonObject.put("instance", key); /// needs to be the full key otherwise not unique
                        String timeAnchor = getTimeAnchor(otherTriples);
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

                            JSONObject jsonClasses = getClassesJSONObjectFromInstanceStatement(instanceTriples);
                            if (jsonClasses.keys().hasNext()) {
                                /// TAKE THIS OUT TO SAVE SPACE
                                  jsonObject.put("classes", jsonClasses);
                            }

                            if (fnLevel > 0) {
                                getFrameNetSuperFramesJSONObjectFromInstanceStatement(jsonObject, instanceTriples);
                            } else if (esoLevel > 0) {
                                getEsoSuperClassesJSONObjectFromInstanceStatement(jsonObject, instanceTriples);
                            }

                            JSONObject jsonLabels = getLabelsJSONObjectFromInstanceStatement(instanceTriples);
                            if (jsonLabels.keys().hasNext()) {
                                jsonObject.put("labels", jsonLabels.get("labels"));
                            }
                            JSONObject jsonprefLabels = getPrefLabelsJSONObjectFromInstanceStatement(instanceTriples);
                            if (jsonprefLabels.keys().hasNext()) {
                                jsonObject.put("prefLabel", jsonprefLabels.get("prefLabel"));
                            }
                            JSONObject jsonMentions = getMentionsJSONObjectFromInstanceStatement(instanceTriples);
                            if (jsonMentions.keys().hasNext()) {
                                jsonObject.put("mentions", jsonMentions.get("mentions"));
                            }
                            JSONObject actors = getActorsJSONObjectFromInstanceStatement(otherTriples);
                            if (actors.keys().hasNext()) {
                                jsonObject.put("actors", actors);
                            }
                            JSONObject topics = getTopicsJSONObjectFromInstanceStatement(instanceTriples);
                            if (topics.keys().hasNext()) {
                              //  System.out.println("topics.length() = " + topics.length());
                                jsonObject.put("topics", topics.get("topics"));
                            }
                            jsonObjectArrayList.add(jsonObject);
                        }

                    }
                }
            }
        }
        try {

            System.out.println("all events = " + jsonObjectArrayList.size());
            jsonObjectArrayList = filterEventsForBlackList (jsonObjectArrayList);
            System.out.println("filter by blacklist = " + jsonObjectArrayList.size());
            jsonObjectArrayList = filterEventsForActors(jsonObjectArrayList);
            System.out.println("filtered by actors = " + jsonObjectArrayList.size());
            jsonObjectArrayList = createStoryLinesForJSONArrayList(jsonObjectArrayList);
            System.out.println("filtered by stories = " + jsonObjectArrayList.size());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObjectArrayList;
    }

    static ArrayList<JSONObject> filterEventsForActors(ArrayList<JSONObject> events) throws JSONException {
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
                        if (ACTORNAMESPACES.equalsIgnoreCase("all") || ACTORNAMESPACES.isEmpty() || ACTORNAMESPACES.indexOf(ns)>-1) {
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

    static ArrayList<JSONObject> filterEventsForBlackList(ArrayList<JSONObject> events) throws JSONException {
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


    /*
                        if (values.length==4) {
                        JSONObject mObject = new JSONObject();
                        String charOffset = values[0];
                        String tokens = values[1];
                        String terms = values[2];
                        String sentence = values[3];
                        mObject.append("uri", uri);
                        addValuesFromMention(mObject, "char", charOffset);
                        addValuesFromMention(mObject, "tokens", tokens);
                        addValuesFromMention(mObject, "terms", terms);
                        addValuesFromMention(mObject, "sentence", sentence);
                        jsonClassesObject.append("mentions", mObject);
     */

        /**
         * Determines the climax values by summing the inverse values of the sentence nr of each mention
         * @param jsonObjects
         * @return
         */
    static TreeSet determineClimaxValues (ArrayList<JSONObject> jsonObjects) {
        //1. We determine the climax score for each individual event and return a sorted list by climax
        // We sum the inverse sentence numbers of all mentions
        TreeSet climaxObjects = new TreeSet(new climaxCompare());
        Double maxClimax = 0.0;
        for (int i = 0; i < jsonObjects.size(); i++) {
            JSONObject jsonObject = jsonObjects.get(i);
           // System.out.println("jsonObject.toString() = " + jsonObject.toString());
            try {
                double sumClimax =0.0;
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                int earliestEventMention = -1;
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mentionObject = (JSONObject) mentions.get(j);
/*                    String sentenceValue = mentionObject.getString("sentence");
                    System.out.println("sentenceValue = " + sentenceValue);
                    if (sentenceValue!=null && !sentenceValue.isEmpty()){*/
                    JSONArray sentences = mentionObject.getJSONArray("sentence");
                    if (sentences!=null ){
                        String sentenceValue = sentences.get(0).toString();
                        int sentenceNr = Integer.parseInt(sentenceValue);
                        if (sentenceNr < earliestEventMention || earliestEventMention == -1) {
                            earliestEventMention = sentenceNr;
                            jsonObject.put("sentence", sentenceValue);
                        }
                        sumClimax += 1.0 / sentenceNr;
                        //String mention = mentions.get(j).toString();
                    }
                    else {
                        JSONArray charValues = mentionObject.getJSONArray("char");
                        //"["4160","4165"]"
                        if (charValues!=null ) {
                            String charValue = charValues.get(0).toString();
                         //   System.out.println("charValue = " + charValue);
                            int sentenceNr = Integer.parseInt(charValue);
                            if (sentenceNr < earliestEventMention || earliestEventMention == -1) {
                                earliestEventMention = sentenceNr;
                                jsonObject.put("sentence", charValue);
                            }
                            sumClimax += 1.0 / sentenceNr;
                        }
                        else {
                         //   System.out.println("charValues = null");
                        }
                    }

/*                    System.out.println("mention = " + mention);
                    int idx = mention.indexOf("sentence=");
                    if (idx >-1) {
                        idx = mention.lastIndexOf("=");
                        int sentenceNr = Integer.parseInt(mention.substring(idx+1));
                        if (sentenceNr<earliestEventMention || earliestEventMention==-1) {
                            earliestEventMention = sentenceNr;
                            jsonObject.put("sentence", mention.substring(idx + 1));
                        }
                        sumClimax += 1.0/sentenceNr;
                    }
                    else {
                        //mention = http://www.newsreader-project.eu/data/2008/07/03/6479113.xml#char=359
                        // if the sentence is not part of the mention than we take the char value
                        idx = mention.indexOf("char=");
                        if (idx >-1) {
                            idx = mention.lastIndexOf("=");
                            int sentenceNr = Integer.parseInt(mention.substring(idx+1));
                            if (sentenceNr<earliestEventMention || earliestEventMention==-1) {
                                earliestEventMention = sentenceNr;
                                jsonObject.put("sentence", mention.substring(idx + 1));
                            }
                            sumClimax += 1.0/sentenceNr;
                        }
                       // System.out.println("mention = " + mention);
                    }*/
                }
                if (sumClimax>maxClimax) {
                    maxClimax = sumClimax;
                }
             //   System.out.println("sumClimax = " + sumClimax);
                jsonObject.put("climax", sumClimax);
            } catch (JSONException e) {
                 //  e.printStackTrace();
                try {
                    jsonObject.put("climax", "0");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

            }
        }
       // System.out.println("maxClimax = " + maxClimax);
        /// next we normalize the climax values and store it in the tree
        for (int i = 0; i < jsonObjects.size(); i++) {
            JSONObject jsonObject = jsonObjects.get(i);
            try {
                Double climax = Double.parseDouble(jsonObject.get("climax").toString());
                Double proportion = climax/maxClimax;
                Integer climaxInteger = new Integer ((int)(100*proportion));
                if (climaxInteger>=climaxThreshold) {
               //     System.out.println("climaxInteger = " + climaxInteger);
                    jsonObject.put("climax", climaxInteger);
                    climaxObjects.add(jsonObject);
                }

             /*   System.out.println("jsonObject.get(\"labels\").toString() = " + jsonObject.get("labels").toString());
                System.out.println("jsonObject.get(\"climax\").toString() = " + jsonObject.get("climax").toString());
                System.out.println("\tmaxClimax = " + maxClimax);
                System.out.println("\tclimax = " + climax);
                System.out.println("\tpropertion = " + propertion);
                System.out.println("\tclimaxInteger = " + climaxInteger);*/

            } catch (JSONException e) {
                //   e.printStackTrace();
            }
        }
        return climaxObjects;
    }

    /**
         * Determines the climax values by summing the inverse values of the sentence nr of each mention
         * @param jsonObjects
         * @return
         */
    static TreeSet determineClimaxValues_org_using_old_mention_structure (ArrayList<JSONObject> jsonObjects) {
        //1. We determine the climax score for each individual event and return a sorted list by climax
        // We sum the inverse sentence numbers of all mentions
        TreeSet climaxObjects = new TreeSet(new climaxCompare());
        Double maxClimax = 0.0;
        for (int i = 0; i < jsonObjects.size(); i++) {
            JSONObject jsonObject = jsonObjects.get(i);
            try {
                double sumClimax =0.0;
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                int earliestEventMention = -1;
                for (int j = 0; j < mentions.length(); j++) {
                    String mention =  mentions.get(j).toString();
                    int idx = mention.indexOf("sentence=");
                    if (idx >-1) {
                        idx = mention.lastIndexOf("=");
                        int sentenceNr = Integer.parseInt(mention.substring(idx+1));
                        if (sentenceNr<earliestEventMention || earliestEventMention==-1) {
                            earliestEventMention = sentenceNr;
                            jsonObject.put("sentence", mention.substring(idx + 1));
                        }
                        sumClimax += 1.0/sentenceNr;
                    }
                    else {
                        //mention = http://www.newsreader-project.eu/data/2008/07/03/6479113.xml#char=359
                        // if the sentence is not part of the mention than we take the char value
                        idx = mention.indexOf("char=");
                        if (idx >-1) {
                            idx = mention.lastIndexOf("=");
                            int sentenceNr = Integer.parseInt(mention.substring(idx+1));
                            if (sentenceNr<earliestEventMention || earliestEventMention==-1) {
                                earliestEventMention = sentenceNr;
                                jsonObject.put("sentence", mention.substring(idx + 1));
                            }
                            sumClimax += 1.0/sentenceNr;
                        }
                       // System.out.println("mention = " + mention);
                    }
                }
                if (sumClimax>maxClimax) {
                    maxClimax = sumClimax;
                }
            //    System.out.println("sumClimax = " + sumClimax);
                jsonObject.put("climax", sumClimax);
            } catch (JSONException e) {
                //   e.printStackTrace();
            }
        }
       // System.out.println("maxClimax = " + maxClimax);
        /// next we normalize the climax values and store it in the tree
        for (int i = 0; i < jsonObjects.size(); i++) {
            JSONObject jsonObject = jsonObjects.get(i);
            try {
                Double climax = Double.parseDouble(jsonObject.get("climax").toString());
                Double proportion = climax/maxClimax;
                Integer climaxInteger = new Integer ((int)(100*proportion));
                if (climaxInteger>=climaxThreshold) {
               //     System.out.println("climaxInteger = " + climaxInteger);
                    jsonObject.put("climax", climaxInteger);
                    climaxObjects.add(jsonObject);
                }

             /*   System.out.println("jsonObject.get(\"labels\").toString() = " + jsonObject.get("labels").toString());
                System.out.println("jsonObject.get(\"climax\").toString() = " + jsonObject.get("climax").toString());
                System.out.println("\tmaxClimax = " + maxClimax);
                System.out.println("\tclimax = " + climax);
                System.out.println("\tpropertion = " + propertion);
                System.out.println("\tclimaxInteger = " + climaxInteger);*/

            } catch (JSONException e) {
                //   e.printStackTrace();
            }
        }
        return climaxObjects;
    }

    static TreeSet determineClimaxValuesFirstMentionOnly (ArrayList<JSONObject> jsonObjects) {
        //1. We determine the climax score for each individual event and return a sorted list by climax
        // We sum the inverse sentence numbers of all mentions
        TreeSet climaxObjects = new TreeSet(new climaxCompare());
        Integer maxClimax = 0;
        for (int i = 0; i < jsonObjects.size(); i++) {
            JSONObject jsonObject = jsonObjects.get(i);
            try {
                int firstMention = -1;
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                int earliestEventMention = -1;
                for (int j = 0; j < mentions.length(); j++) {
                    String mention =  mentions.get(j).toString();
                    int idx = mention.indexOf("sentence=");
                    if (idx >-1) {
                        idx = mention.lastIndexOf("=");
                        int sentenceNr = Integer.parseInt(mention.substring(idx+1));
                        if (sentenceNr<earliestEventMention || earliestEventMention==-1) {
                            earliestEventMention = sentenceNr;
                            jsonObject.put("sentence", mention.substring(idx + 1));
                            if (sentenceNr < firstMention || firstMention == -1) {
                                firstMention = sentenceNr;
                            }
                        }
                    }
                }

                /// we have the first mention for an event
                /// we calculate the climax in combination with the nr. of mentions
                /// calculate the climax score and save it
                Integer climax = 1+(1/firstMention)*mentions.length();
                jsonObject.put("climax", climax);
                if (climax>maxClimax) {
                    maxClimax = climax;
                }
                climaxObjects.add(jsonObject);

            } catch (JSONException e) {
                //   e.printStackTrace();
            }
        }
        return climaxObjects;
    }

    static boolean hasObject (ArrayList<JSONObject> objects, JSONObject object) {
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.get(i);
            try {
               // System.out.println("jsonObject.get(\"instance\").toString() = " + jsonObject.get("instance").toString());

                if (jsonObject.get("instance").toString().equals(object.get("instance").toString())) {
                    return true;
                }
            } catch (JSONException e) {
              //  e.printStackTrace();
            }
        }
        return false;
    }

    static String getActorByRoleFromEvent (JSONObject event, String role) throws JSONException {
        String actor = "";
        JSONObject actorObject = event.getJSONObject("actors");
        Iterator keys = actorObject.sortedKeys();
        while (keys.hasNext()) {
            String key = keys.next().toString(); //role
            //  System.out.println("key = " + key);
            if (key.equalsIgnoreCase(role)) {
                JSONArray actors = actorObject.getJSONArray(key);
                for (int j = 0; j < actors.length(); j++) {
                    String nextActor = actors.getString(j);
                    nextActor = nextActor.substring(nextActor.lastIndexOf("/")+1);
                    if (actor.indexOf(nextActor)==-1) {
                        actor += ":" +nextActor;
                    }
                    //break;
                }
            }
        }
        return actor;
    }

    static ArrayList<String> getActorsByRoleFromEvent (JSONObject event, String role) throws JSONException {
        ArrayList<String> actorList = new ArrayList<String>();
        JSONObject actorObject = event.getJSONObject("actors");
        Iterator keys = actorObject.sortedKeys();
        while (keys.hasNext()) {
            String key = keys.next().toString(); //role
            //  System.out.println("key = " + key);
            if (key.equalsIgnoreCase(role)) {
                JSONArray actors = actorObject.getJSONArray(key);
                for (int j = 0; j < actors.length(); j++) {
                    String nextActor = actors.getString(j);
                    nextActor = nextActor.substring(nextActor.lastIndexOf("/")+1);
                    if (!actorList.contains(nextActor)) {
                        actorList.add(nextActor);
                    }
                }
            }
        }
        return actorList;
    }

    static String getfirstActorByRoleFromEvent (JSONObject event, String role) throws JSONException {
        String actor = "";
        JSONObject actorObject = event.getJSONObject("actors");
        Iterator keys = actorObject.sortedKeys();
        while (keys.hasNext()) {
            String key = keys.next().toString(); //role
            //  System.out.println("key = " + key);
            if (key.equalsIgnoreCase(role)) {
                JSONArray actors = actorObject.getJSONArray(key);
                for (int j = 0; j < actors.length(); j++) {
                    String nextActor = actors.getString(j);
                    nextActor = nextActor.substring(nextActor.lastIndexOf("/")+1);
                    actor += ":" + nextActor;
                    break;
                }
            }
        }
        return actor;
    }

    static String getActorFromEvent (JSONObject event, String actorString) throws JSONException {
        String actor = "";
        JSONObject actorObject = event.getJSONObject("actors");
        Iterator keys = actorObject.sortedKeys();
        while (keys.hasNext()) {
            String key = keys.next().toString(); //role
            //  System.out.println("key = " + key);
                JSONArray actors = actorObject.getJSONArray(key);
                for (int j = 0; j < actors.length(); j++) {
                    String nextActor = actors.getString(j);
                    nextActor = nextActor.substring(nextActor.lastIndexOf("/")+1);
                    if (nextActor.indexOf(actorString)>-1) {
                        if (actor.indexOf(nextActor)==-1) {
                            actor += ":" +nextActor;
                        }
                    }
                }
        }
        return actor;
    }

    static boolean hasActorInEvent (JSONObject event, ArrayList<String> actorList) throws JSONException {
        JSONObject actorObject = event.getJSONObject("actors");
        Iterator keys = actorObject.sortedKeys();
        while (keys.hasNext()) {
            String key = keys.next().toString(); //role
            //  System.out.println("key = " + key);
                JSONArray actors = actorObject.getJSONArray(key);
                for (int j = 0; j < actors.length(); j++) {
                    String nextActor = actors.getString(j);
                    nextActor = nextActor.substring(nextActor.lastIndexOf("/") + 1);
                    if (actorList.contains(nextActor)) {
                        return true;
                    }
                }
        }
        return false;
    }

    static ArrayList<JSONObject> intersectEventObjects(ArrayList<JSONObject> set1, ArrayList<JSONObject> set2) throws JSONException {
        ArrayList<JSONObject> intersection = new ArrayList<JSONObject>();
        for (int i = 0; i < set1.size(); i++) {
            JSONObject object1 = set1.get(i);
            for (int j = 0; j < set2.size(); j++) {
                JSONObject object2 = set2.get(j);
                if (object1.get("instance").toString().equals(object2.get("instance").toString())) {
                   intersection.add(object1);
                }
            }
        }
        return  intersection;
    }


/*    static void addObjectToGroup (ArrayList<JSONObject> groupObjects,
                                  String groupName,
                                  JSONObject object,
                                  int divide) throws JSONException {
        Float size = null;
        object.put("group", groupName);
        Double climax = 0.0;
        try {
            climax = Double.parseDouble(object.get("climax").toString());
        } catch (NumberFormatException e) {
          //  e.printStackTrace();
        } catch (JSONException e) {
          //  e.printStackTrace();
        }
        if (climax >= climaxThreshold) {
            // size = 5 * Float.valueOf((float) (1.0 * climax / groupClimax));
            try {
                if (climax==0) {
                    size = new Float(1);

                }
                else {
                    size = Float.valueOf((float) (climax / divide));
                }
                object.put("size", size.toString());
                groupObjects.add(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/

    static void addObjectToGroup (ArrayList<JSONObject> groupObjects,
                                  String group, String groupName, String groupScore,
                                  JSONObject object,
                                  int divide) throws JSONException {
        Float size = null;
        object.put("group", group);
        object.put("groupName", groupName);
        object.put("groupScore", groupScore);
        Double climax = 0.0;
        try {
            climax = Double.parseDouble(object.get("climax").toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (climax >= climaxThreshold) {
            // size = 5 * Float.valueOf((float) (1.0 * climax / groupClimax));
            try {
                if (climax==0) {
                    size = new Float(1);

                }
                else {
                    size = Float.valueOf((float) (climax / divide));
                }
                object.put("size", size.toString());
                groupObjects.add(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static String climaxString (Integer climax) {
        String str = "";
        if (climax==100) {
            str  = climax.toString();
        }
        else if (climax>9){
            str = "0"+climax.toString();
        }
        else {
            str = "00"+climax.toString();
        }
        return str;
    }

    static ArrayList<JSONObject> createStoryLinesForJSONArrayList (ArrayList<JSONObject> jsonObjects)  throws JSONException {
        int nGroups = 0;
        String entity = "Airbus";
        String entityTimeLine = entity+"\n";
        String entityMatch = "";
        String debugStr = "";
        boolean PRINTEXAMPLE = false;
        boolean DEBUG = false;
        /*
        	2004-10	4-killed[t85]	10-leveled[t182]	36-charges[t803]
	        2004-10	4-favored[t97]	31-favor[t689]
         */

        ArrayList<JSONObject> groupedObjects = new ArrayList<JSONObject>();
        /// We build up a climax index over all the events
        //Vector<Integer> climaxIndex = new Vector<Integer>();

        //1. We determine the climax score for each individual event
        // We sum the inverse sentence numbers of all mentions
        TreeSet climaxObjects = determineClimaxValues(jsonObjects);
        ArrayList<JSONObject> selectedEvents  = new ArrayList<JSONObject>();
        Iterator<JSONObject> sortedObjects = climaxObjects.iterator();
        while (sortedObjects.hasNext()) {
            JSONObject jsonObject = sortedObjects.next();
            selectedEvents.add(jsonObject);
        }

        //TreeSet climaxObjects = determineClimaxValuesFirstMentionOnly(jsonObjects);

        System.out.println("events above climax threshold = " + climaxObjects.size());
        ArrayList<JSONObject> storyObjects = new ArrayList<JSONObject>();
        ArrayList<JSONObject> singletonObjects = new ArrayList<JSONObject>();
        sortedObjects = climaxObjects.iterator();
        while (sortedObjects.hasNext()) {
            JSONObject jsonObject = sortedObjects.next();
            if (!hasObject(groupedObjects, jsonObject)) {
                try {
                    storyObjects = new ArrayList<JSONObject>();
                    Integer groupClimax = Integer.parseInt(jsonObject.get("climax").toString());
                    Float size = new Float(1);
                    if (groupClimax > 0) {
                       // size = 5 * Float.valueOf((float) (1.0 * groupClimax / groupClimax));
                        size = Float.valueOf((float) groupClimax / 4);
                    }
                    //Float size = 1 + Float.valueOf((float) (10*climax / maxClimax));
                    jsonObject.put("size", size.toString());
                    String group = climaxString(groupClimax) + ":" + jsonObject.get("labels").toString();
                    String groupName = jsonObject.get("labels").toString();
                    String groupScore = climaxString(groupClimax);
                    group += getfirstActorByRoleFromEvent(jsonObject, "pb/A1"); /// for representation purposes
                    groupName += getfirstActorByRoleFromEvent(jsonObject, "pb/A1");
                    if (DEBUG) debugStr = group+":"+groupClimax+":"+size+"\n";

                    jsonObject.put("group", group);
                    jsonObject.put("groupName", groupName);
                    jsonObject.put("groupScore", groupScore);
                    storyObjects.add(jsonObject);
                    nGroups++;
                    ArrayList<String> coparticipantsA0 = getActorsByRoleFromEvent(jsonObject, "pb/A0");
                    ArrayList<String> coparticipantsA1 = getActorsByRoleFromEvent(jsonObject, "pb/A1");
                    ArrayList<String> coparticipantsA2 = getActorsByRoleFromEvent(jsonObject, "pb/A2");

                    if (PRINTEXAMPLE) {
                        /////// FOR EXAMPLE OUTPUT
                        entityMatch = getActorFromEvent(jsonObject, entity);
                        if (!entityMatch.isEmpty()) {
                            entityTimeLine += "\n" + entityMatch + "\n";
                            String time = jsonObject.get("time").toString();
                            if (time.isEmpty()) {
                                time = "NOTIMEX";
                            }
                            String event = jsonObject.get("labels").toString();
                            event += getActorByRoleFromEvent(jsonObject, "pb/A0");
                            event += getActorByRoleFromEvent(jsonObject, "pb/A1");
                            event += getActorByRoleFromEvent(jsonObject, "pb/A2");
                            entityTimeLine += "[C]\t" + groupClimax + "\t" + time + "\t" + event + "\n";
                        }
                        /////// FOR EXAMPLE OUTPUT
                    }

                    ArrayList<JSONObject> coevents = CreateMicrostory.getEventsThroughCoparticipation(selectedEvents, jsonObject);
                  //  System.out.println("coevents.size() = " + coevents.size());
                    ArrayList<JSONObject> topicevents = CreateMicrostory.getEventsThroughTopicBridging(selectedEvents, jsonObject, topicThreshold);
                   // System.out.println("topicevents = " + topicevents.size());
                    ArrayList<JSONObject> intersection = intersectEventObjects(coevents, topicevents);
                  //  ArrayList<JSONObject> intersection = CreateMicrostory.getEventsThroughTopicBridging(selectedEvents, jsonObject, topicThreshold);

                    for (int i = 0; i < intersection.size(); i++) {
                        JSONObject object = intersection.get(i);
                        if (!hasObject(groupedObjects, object)) {
                            addObjectToGroup(
                                    storyObjects,
                                    group,
                                    groupName,
                                    groupScore,
                                    object,
                                    8);
                        }
                    }

                    /*ArrayList<JSONObject> coevents = CreateMicrostory.getEventsThroughCoparticipation(selectedEvents, jsonObject);
                    for (int i = 0; i < coevents.size(); i++) {
                        JSONObject object = coevents.get(i);
                        if (!hasObject(groupedObjects, object)) {
                            addObjectToGroup(
                                    storyObjects,
                                    group,
                                    groupName,
                                    groupScore,
                                    object,
                                    8);
                            if (PRINTEXAMPLE) {
                                if (!entityMatch.isEmpty()) {
                                    /////// FOR EXAMPLE OUTPUT
                                    /// without forcing coparticipation of the target entity
                                    Integer climax = Integer.parseInt(object.get("climax").toString());
                                    String event = object.get("labels").toString();
                                    event += getActorByRoleFromEvent(object, "pb/A0");
                                    event += getActorByRoleFromEvent(object, "pb/A1");
                                    event += getActorByRoleFromEvent(object, "pb/A2");
                                    String time = object.get("time").toString();
                                    if (time.isEmpty()) {
                                        time = "NOTIMEX";
                                    }
                                    entityTimeLine += "\t" + climax + "\t" + time + "\t" + event + "\n";

                                }
                            }
                        }
                    }


                    if (topicThreshold>0) {
                        coevents = CreateMicrostory.getEventsThroughTopicBridging(selectedEvents, jsonObject, topicThreshold);

                        System.out.println("coevents.size() = " + coevents.size());
                        for (int i = 0; i < coevents.size(); i++) {
                            JSONObject object = coevents.get(i);
                            if (!hasObject(groupedObjects, object)) {
                                addObjectToGroup(
                                        storyObjects,
                                        group,
                                        groupName,
                                        groupScore,
                                        object,
                                        8);
                            }
                        }
                    }*/
                    /*
                    ArrayList<JSONObject> fnevents = CreateMicrostory.getEventsThroughFrameNetBridging(jsonObjects, jsonObject, frameNetReader);
                    //ArrayList<JSONObject> fnevents = CreateMicrostory.getEventsThroughEsoBridging(jsonObjects, jsonObject, frameNetReader);
                    //  System.out.println("coevents = " + coevents.size());
                    //  System.out.println("fnevents = " + fnevents.size());

                  //  ArrayList<JSONObject> intersection = new ArrayList<JSONObject>();
                    ArrayList<JSONObject> intersection = intersectEventObjects(coevents, fnevents);

                    for (int j = 0; j < intersection.size(); j++) {
                        JSONObject object = intersection.get(j);
                        *//*
                        if (!hasActorInEvent(object, coparticipantsA0)
                        &&
                        !hasActorInEvent(object, coparticipantsA1)
                        &&
                        !hasActorInEvent(object, coparticipantsA2)) {
                            continue;
                        }*//*
                       *//* String copartipation = getActorFromEvent(object, entity);
                        if (copartipation.isEmpty()) {
                            continue;
                        }*//*


                        if (!hasObject(groupedObjects, object)) {
                            addObjectToGroup(
                                    storyObjects,
                                    group, groupName, groupScore,
                                    object,
                                    6);
                            if (PRINTEXAMPLE) {
                                if (!entityMatch.isEmpty()) {
                                    /////// FOR EXAMPLE OUTPUT

                                    /// without forcing coparticipation of the target entity
                                    Integer climax = Integer.parseInt(object.get("climax").toString());
                                    String event = object.get("labels").toString();
                                    event += getActorByRoleFromEvent(object, "pb/A0");
                                    event += getActorByRoleFromEvent(object, "pb/A1");
                                    event += getActorByRoleFromEvent(object, "pb/A2");
                                    String time = object.get("time").toString();
                                    if (time.isEmpty()) {
                                        time = "NOTIMEX";
                                    }
                                    entityTimeLine += "\t" + climax + "\t" + time + "\t" + event + "\n";

                                }
                            }
                        }

                    }

                    //// The less strict version continues with non-intersecting events....
                    //// use next commented block to get more...
                    if (intersection.size()==0) {


                    }*/

                    if (DEBUG) System.out.println(debugStr);

                    for (int i = 0; i < storyObjects.size(); i++) {
                        JSONObject object = storyObjects.get(i);
                        groupedObjects.add(object);
                    }

/*
                    if (storyObjects.size()>1) {
                        for (int i = 0; i < storyObjects.size(); i++) {
                            JSONObject object = storyObjects.get(i);
                            groupedObjects.add(object);
                        }
                    }
                    else {
                        //// these are now ignored
                        singletonObjects.add(storyObjects.get(0));
                    }
*/
                } catch (JSONException e) {
                    // e.printStackTrace();
                }

            }
            //  break;

        } // end of while objects in sorted climaxObjects



        if (PRINTEXAMPLE) {
            /////// FOR EXAMPLE OUTPUT
            System.out.println("entityTimeLine = " + entityTimeLine);
            /////// FOR EXAMPLE OUTPUT
        }
        System.out.println("Nr of stories = " + nGroups);
        return groupedObjects;
    }

    static ArrayList<JSONObject> createStoryLinesForJSONArrayListOrg (ArrayList<JSONObject> jsonObjects)  throws JSONException {

        String entity = "Airbus";
        String entityTimeLine = entity+"\n";
        String entityMatch = "";
        String debugStr = "";
        boolean PRINTEXAMPLE = false;
        boolean DEBUG = false;
        /*
        	2004-10	4-killed[t85]	10-leveled[t182]	36-charges[t803]
	        2004-10	4-favored[t97]	31-favor[t689]
         */

        ArrayList<JSONObject> groupedObjects = new ArrayList<JSONObject>();
        /// We build up a climax index over all the events
        //Vector<Integer> climaxIndex = new Vector<Integer>();

        //1. We determine the climax score for each individual event
        // We sum the inverse sentence numbers of all mentions
        TreeSet climaxObjects = determineClimaxValues(jsonObjects);
        //TreeSet climaxObjects = determineClimaxValuesFirstMentionOnly(jsonObjects);


        ArrayList<JSONObject> storyObjects = new ArrayList<JSONObject>();
        ArrayList<JSONObject> singletonObjects = new ArrayList<JSONObject>();
        Iterator<JSONObject> sortedObjects = climaxObjects.iterator();
        while (sortedObjects.hasNext()) {
            JSONObject jsonObject = sortedObjects.next();
            if (!hasObject(groupedObjects, jsonObject)) {
                try {
                    storyObjects = new ArrayList<JSONObject>();
                    Integer groupClimax = Integer.parseInt(jsonObject.get("climax").toString());
                    Float size = new Float(1);
                    if (groupClimax > 0) {
                       // size = 5 * Float.valueOf((float) (1.0 * groupClimax / groupClimax));
                        size = Float.valueOf((float) groupClimax / 4);
                    }
                    //Float size = 1 + Float.valueOf((float) (10*climax / maxClimax));
                    jsonObject.put("size", size.toString());
                    String group = climaxString(groupClimax) + ":" + jsonObject.get("labels").toString();
                    String groupName = jsonObject.get("labels").toString();
                    String groupScore = climaxString(groupClimax);
                    group += getfirstActorByRoleFromEvent(jsonObject, "pb/A1"); /// for representation purposes
                    groupName += getfirstActorByRoleFromEvent(jsonObject, "pb/A1");
                    if (DEBUG) debugStr = group+":"+groupClimax+":"+size+"\n";

                    jsonObject.put("group", group);
                    jsonObject.put("groupName", groupName);
                    jsonObject.put("groupScore", groupScore);
                    storyObjects.add(jsonObject);

                    ArrayList<String> coparticipantsA0 = getActorsByRoleFromEvent(jsonObject, "pb/A0");
                    ArrayList<String> coparticipantsA1 = getActorsByRoleFromEvent(jsonObject, "pb/A1");
                    ArrayList<String> coparticipantsA2 = getActorsByRoleFromEvent(jsonObject, "pb/A2");

                    if (PRINTEXAMPLE) {
                        /////// FOR EXAMPLE OUTPUT
                        entityMatch = getActorFromEvent(jsonObject, entity);
                        if (!entityMatch.isEmpty()) {
                            entityTimeLine += "\n" + entityMatch + "\n";
                            String time = jsonObject.get("time").toString();
                            if (time.isEmpty()) {
                                time = "NOTIMEX";
                            }
                            String event = jsonObject.get("labels").toString();
                            event += getActorByRoleFromEvent(jsonObject, "pb/A0");
                            event += getActorByRoleFromEvent(jsonObject, "pb/A1");
                            event += getActorByRoleFromEvent(jsonObject, "pb/A2");
                            entityTimeLine += "[C]\t" + groupClimax + "\t" + time + "\t" + event + "\n";
                        }
                        /////// FOR EXAMPLE OUTPUT
                    }


                    ArrayList<JSONObject> coevents = CreateMicrostory.getEventsThroughCoparticipation(jsonObjects, jsonObject);
                    ArrayList<JSONObject> fnevents = CreateMicrostory.getEventsThroughFrameNetBridging(jsonObjects, jsonObject, frameNetReader);
                    //ArrayList<JSONObject> fnevents = CreateMicrostory.getEventsThroughEsoBridging(jsonObjects, jsonObject, frameNetReader);
                    //  System.out.println("coevents = " + coevents.size());
                    //  System.out.println("fnevents = " + fnevents.size());

                  //  ArrayList<JSONObject> intersection = new ArrayList<JSONObject>();
                    ArrayList<JSONObject> intersection = intersectEventObjects(coevents, fnevents);

                    for (int j = 0; j < intersection.size(); j++) {
                        JSONObject object = intersection.get(j);
                        /*
                        if (!hasActorInEvent(object, coparticipantsA0)
                        &&
                        !hasActorInEvent(object, coparticipantsA1)
                        &&
                        !hasActorInEvent(object, coparticipantsA2)) {
                            continue;
                        }*/
                       /* String copartipation = getActorFromEvent(object, entity);
                        if (copartipation.isEmpty()) {
                            continue;
                        }*/


                        if (!hasObject(groupedObjects, object)) {
                            addObjectToGroup(
                                    storyObjects,
                                    group, groupName, groupScore,
                                    object,
                                    6);
                            if (PRINTEXAMPLE) {
                                if (!entityMatch.isEmpty()) {
                                    /////// FOR EXAMPLE OUTPUT

                                    /// without forcing coparticipation of the target entity
                                    Integer climax = Integer.parseInt(object.get("climax").toString());
                                    String event = object.get("labels").toString();
                                    event += getActorByRoleFromEvent(object, "pb/A0");
                                    event += getActorByRoleFromEvent(object, "pb/A1");
                                    event += getActorByRoleFromEvent(object, "pb/A2");
                                    String time = object.get("time").toString();
                                    if (time.isEmpty()) {
                                        time = "NOTIMEX";
                                    }
                                    entityTimeLine += "\t" + climax + "\t" + time + "\t" + event + "\n";

                                }
                            }
                        }

                    }

                    //// The less strict version continues with non-intersecting events....
                    //// use next commented block to get more...
                    if (intersection.size()==0) {
                        for (int i = 0; i < coevents.size(); i++) {
                            JSONObject object = coevents.get(i);
                            if (!hasObject(groupedObjects, object)) {
                                addObjectToGroup(
                                        storyObjects,
                                        group, groupName, groupScore,
                                        object,
                                        8);
                                if (PRINTEXAMPLE) {
                                    if (!entityMatch.isEmpty()) {
                                        /////// FOR EXAMPLE OUTPUT

                                        /// without forcing coparticipation of the target entity
                                        Integer climax = Integer.parseInt(object.get("climax").toString());
                                        String event = object.get("labels").toString();
                                        event += getActorByRoleFromEvent(object, "pb/A0");
                                        event += getActorByRoleFromEvent(object, "pb/A1");
                                        event += getActorByRoleFromEvent(object, "pb/A2");
                                        String time = object.get("time").toString();
                                        if (time.isEmpty()) {
                                            time = "NOTIMEX";
                                        }
                                        entityTimeLine += "\t" + climax + "\t" + time + "\t" + event + "\n";

                                    }
                                }
                            }
                        }
                        for (int i = 0; i < fnevents.size(); i++) {
                            JSONObject object = fnevents.get(i);
                            if (!hasObject(groupedObjects, object)) {
                                addObjectToGroup(
                                        storyObjects,
                                        group,groupName, groupScore,
                                        object,
                                        8);
                                if (PRINTEXAMPLE) {
                                    if (!entityMatch.isEmpty()) {
                                        /////// FOR EXAMPLE OUTPUT

                                        /// without forcing coparticipation of the target entity
                                        Integer climax = Integer.parseInt(object.get("climax").toString());
                                        String event = object.get("labels").toString();
                                        event += getActorByRoleFromEvent(object, "pb/A0");
                                        event += getActorByRoleFromEvent(object, "pb/A1");
                                        event += getActorByRoleFromEvent(object, "pb/A2");
                                        String time = object.get("time").toString();
                                        if (time.isEmpty()) {
                                            time = "NOTIMEX";
                                        }
                                        entityTimeLine += "\t" + climax + "\t" + time + "\t" + event + "\n";

                                    }
                                }
                            }
                        }
                    }

                    if (DEBUG) System.out.println(debugStr);

                    if (storyObjects.size()>1) {
                        for (int i = 0; i < storyObjects.size(); i++) {
                            JSONObject object = storyObjects.get(i);
                            groupedObjects.add(object);
                        }
                    }
                    else {
                        singletonObjects.add(storyObjects.get(0));
                    }
                } catch (JSONException e) {
                    // e.printStackTrace();
                }

            }
            //  break;

        } // end of while objects in sorted climaxObjects

        //// now we handle the singleton events
        //// we assign them to the unrelated events group and recalculate the climax score
/*
        climaxObjects = determineClimaxValues(singletonObjects);
        sortedObjects = climaxObjects.iterator();
        while (sortedObjects.hasNext()) {
            JSONObject jsonObject = sortedObjects.next();
            String group = "?:unrelated events";
            addObjectToGroup(groupedObjects,
                    group,
                    jsonObject,
                    10);
        }
*/


        if (PRINTEXAMPLE) {
            /////// FOR EXAMPLE OUTPUT
            System.out.println("entityTimeLine = " + entityTimeLine);
            /////// FOR EXAMPLE OUTPUT
        }

        return groupedObjects;
    }
    /**
     * Integer climax = 1+climaxIndex.size()-climaxIndex.indexOf(sentenceNr);
     Float size = 1+Float.valueOf(((float)((5*climaxIndex.size()-5*climaxIndex.indexOf(sentenceNr))/(float)climaxIndex.size())));
     //
     */


    static public class climaxCompare implements Comparator {
        public int compare (Object aa, Object bb) {
            try {
                Integer a = Integer.parseInt(((JSONObject) aa).get("climax").toString());
                Integer b = Integer.parseInt(((JSONObject)bb).get("climax").toString());
                if (a <= b) {
                    return 1;
                }
                else {
                    return -1;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return -1;
        }
    }


    static ArrayList<JSONObject> createGroupsForJSONArrayList (ArrayList<JSONObject> jsonObjects) {
        ArrayList<JSONObject> groupedObjects = new ArrayList<JSONObject>();
        HashMap<String, ArrayList<JSONObject>> frameMap = new HashMap<String, ArrayList<JSONObject>>();
        for (int i = 0; i < jsonObjects.size(); i++) {
            JSONObject jsonObject = jsonObjects.get(i);
            try {
                //JSONArray superFrames = (JSONArray) jsonObject.get("fnsuperframes");
                JSONArray superFrames = (JSONArray) jsonObject.get("esosuperclasses");
                for (int j = 0; j < superFrames.length(); j++) {
                    String frame = (String) superFrames.get(j);
                    if (frameMap.containsKey(frame)) {
                        ArrayList<JSONObject> objects = frameMap.get(frame);
                        objects.add(jsonObject);
                        frameMap.put(frame, objects);
                    }
                    else {
                        ArrayList<JSONObject> objects = new ArrayList<JSONObject>();
                        objects.add(jsonObject);
                        frameMap.put(frame, objects);
                    }
                }
            } catch (JSONException e) {
              //  e.printStackTrace();
                //JSONArray frames = (JSONArray)jsonObject.get("frames");

                if (frameMap.containsKey("noframe")) {
                    ArrayList<JSONObject> objects = frameMap.get("noframe");
                    objects.add(jsonObject);
                    frameMap.put("noframe", objects);
                }
                else {
                    ArrayList<JSONObject> objects = new ArrayList<JSONObject>();
                    objects.add(jsonObject);
                    frameMap.put("noframe", objects);
                }
            }
        }
        SortedSet<PhraseCount> list = new TreeSet<PhraseCount>(new PhraseCount.Compare());
        Set keySet = frameMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            ArrayList<JSONObject> objects = frameMap.get(key);
            PhraseCount pcount = new PhraseCount(key, objects.size());
            list.add(pcount);
        }
        for (PhraseCount pcount : list) {
            ArrayList<JSONObject> allObjects = frameMap.get(pcount.getPhrase());
            int firstMention = -1;
            Vector<Integer> climaxIndex = new Vector<Integer>();
            /// filter out objects already covered from the group
            ArrayList<JSONObject> objects = new ArrayList<JSONObject>();
            for (int i = 0; i < allObjects.size(); i++) {
                JSONObject object = allObjects.get(i);
                if (!groupedObjects.contains(object)) {
                    objects.add(object);
                }
            }
            for (int i = 0; i < objects.size(); i++) {
                JSONObject jsonObject = objects.get(i);
                try {
                    JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                    int earliestEventMention = -1;
                    for (int j = 0; j < mentions.length(); j++) {
                        String mention =  mentions.get(j).toString();
                        int idx = mention.indexOf("sentence=");
                        if (idx >-1) {
                            idx = mention.lastIndexOf("=");
                            int sentenceNr = Integer.parseInt(mention.substring(idx+1));
                            if (sentenceNr<earliestEventMention || earliestEventMention==-1) {
                                earliestEventMention = sentenceNr;
                                jsonObject.put("sentence", mention.substring(idx + 1));
                                if (sentenceNr < firstMention || firstMention == -1) {
                                    firstMention = sentenceNr;
                                }
                            }
                        }
                    }
                    if (!climaxIndex.contains(earliestEventMention)) {
                        climaxIndex.add(earliestEventMention);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Collections.sort(climaxIndex);
/*
            for (int i = 0; i < climaxIndex.size(); i++) {
                Integer integer = climaxIndex.get(i);
                System.out.println("integer = " + integer);
            }
*/
            for (int i = 0; i < objects.size(); i++) {
                JSONObject jsonObject = objects.get(i);
                try {
                    // JSONObject sentenceObject = (JSONObject) jsonObject.get("sentence");
                    int sentenceNr = Integer.parseInt((String) jsonObject.get("sentence"));
                    // Integer climax = sentenceNr-firstMention;
                    Integer climax = 1+climaxIndex.size()-climaxIndex.indexOf(sentenceNr);
                    Float size = 1+Float.valueOf(((float)((5*climaxIndex.size()-5*climaxIndex.indexOf(sentenceNr))/(float)climaxIndex.size())));
                //    System.out.println("climax.toString() = " + climax.toString());
                //    System.out.println("size.toString() = " + size.toString());

/*                  
                    String combinedKey = pcount.getPhrase()+"."+climax.toString();
                    jsonObject.put("climax", combinedKey);*/
                    jsonObject.put("size", size.toString());
                    jsonObject.put("climax", climax.toString());
                    jsonObject.put("group", pcount.getPhrase());
                    if (!groupedObjects.contains(jsonObject)) {
                        groupedObjects.add(jsonObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return groupedObjects;
    }

    static ArrayList<JSONObject> getJSONObjectArrayRDF() throws JSONException {
        ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<JSONObject>();

        Set keySet = tripleMapOthers.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next(); //// this is the subject of the triple which should point to an event
            ArrayList<Statement> otherTriples = tripleMapOthers.get(key);
            if (!hasActor(otherTriples)) {
               /// we ignore events without actors.....
            }
            else {

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("event", key);
                String timeAnchor = getTimeAnchor(otherTriples);
                // String timeString = semTime.getOwlTime().toString().replaceAll("-", ",");

                jsonObject.put("time", timeAnchor);
                if (tripleMapInstances.containsKey( key)) {
                    ArrayList<Statement> instanceTriples = tripleMapInstances.get(key);
                    for (int i = 0; i < instanceTriples.size(); i++) {
                        Statement statement = instanceTriples.get(i);
                        String predicate = statement.getPredicate().getURI();
                      //  if (predicate.)
                        String object = "";
                        if (statement.getObject().isLiteral()) {
                            object = statement.getObject().asLiteral().toString();
                        } else if (statement.getObject().isURIResource()) {
                            object = statement.getObject().asResource().getURI();
                        }
                        jsonObject.put(predicate, object);
                    }
                }
                for (int i = 0; i < otherTriples.size(); i++) {
                    Statement statement = otherTriples.get(i);
                    String predicate = statement.getPredicate().getURI();
                    if (!predicate.toLowerCase().endsWith("hastime")) {
                        String object = "";
                        if (statement.getObject().isLiteral()) {
                            object = statement.getObject().asLiteral().toString();
                        } else if (statement.getObject().isURIResource()) {
                            object = statement.getObject().asResource().getURI();
                        }
                        jsonObject.put(predicate, object);
                    }
                }
                jsonObjectArrayList.add(jsonObject);
            }
        }
        return jsonObjectArrayList;
    }

    static void writeJsonObjectArray (String pathToFolder, String project, ArrayList<JSONObject> objects) {
        try {
            try {
                File folder = new File(pathToFolder);
                OutputStream jsonOut = new FileOutputStream(folder.getAbsoluteFile() + "/" + "contextual.timeline.json");
               // OutputStream jsonOut = new FileOutputStream(folder.getParentFile() + "/" + folder.getName()+".timeline.json");
                JSONObject timeLineObject = JsonEvent.createTimeLineProperty(new File(pathToFolder).getName(), project);
                /*Set keySet = actorCount.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Integer cnt = actorCount.get(key);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", key);
                    jsonObject.put("event_count", cnt);
                    timeLineObject.append("actors", jsonObject);
                }*/
                for (int j = 0; j < objects.size(); j++) {
                    JSONObject jsonObject = objects.get(j);
                    timeLineObject.append("events", jsonObject);
                }

                String str = "{ \"timeline\":\n";
                jsonOut.write(str.getBytes());
                jsonOut.write(timeLineObject.toString(1).getBytes());
                str ="}\n";
                jsonOut.write(str.getBytes());
                jsonOut.close();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void writeJsonObjectArray (String pathToFolder,
                                      String project,
                                      ArrayList<JSONObject> objects,
                                      ArrayList<String> rawTextArrayList) {
        try {
            try {
               // OutputStream jsonOut = new FileOutputStream(folder.getParentFile() + "/" + folder.getName()+".timeline.json");
                JSONObject timeLineObject = JsonEvent.createTimeLineProperty(new File(pathToFolder).getName(), project);
                /*Set keySet = actorCount.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Integer cnt = actorCount.get(key);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", key);
                    jsonObject.put("event_count", cnt);
                    timeLineObject.append("actors", jsonObject);
                }*/
                for (int j = 0; j < objects.size(); j++) {
                    JSONObject jsonObject = objects.get(j);
                    timeLineObject.append("events", jsonObject);
                }
                if (rawTextArrayList.size()>0) {

                    for (int i = 0; i < rawTextArrayList.size(); i++) {
                        String string = rawTextArrayList.get(i);
                        String[] fields = string.split("\t");
                        if (fields.length > 1) {
                            String uri = fields[0];
                            String text = fields[1];
                            //System.out.println("string = " + string);
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("uri", uri);
                            jsonObject.put("text", text);
                            timeLineObject.append("sources", jsonObject);
                        }
                    }
                }
                File folder = new File(pathToFolder);
                OutputStream jsonOut = new FileOutputStream(folder.getAbsolutePath() + "/" + "contextual.timeline.json");

                String str = "{ \"timeline\":\n";
                jsonOut.write(str.getBytes());
                jsonOut.write(timeLineObject.toString(0).getBytes());
                str ="}\n";
                jsonOut.write(str.getBytes());
                //// OR simply
               // jsonOut.write(timeLineObject.toString().getBytes());
                jsonOut.close();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    static String getNameSpaceString (String value) {
        String property = "";
        if (value.indexOf("/framenet/") > -1) {
            property = "fn";
        }
        else if (value.indexOf("/propbank/") > -1) {
            property = "pb";
        }
        else if (value.indexOf("/sem/") > -1) {
            property = "sem";
        }
        else if (value.indexOf("/sumo/") > -1) {
            property = "sumo";
        }
        else if (value.indexOf("/eso/") > -1) {
            property = "eso";
        }
        else if (value.indexOf("/domain-ontology") > -1) {
            property = "eso";
        }
        else if (value.indexOf("ili-30") > -1) {
            property = "wn";
        }
        else if (value.indexOf("/dbpedia") > -1) {
            property = "dbp";
        }
        else if (value.indexOf("es.dbpedia") > -1) {
            property = "es.dbp";
        }
        else if (value.indexOf("nl.dbpedia") > -1) {
            property = "nl.dbp";
        }
        else if (value.indexOf("it.dbpedia") > -1) {
            property = "it.dbp";
        }
        else if (value.indexOf("/entities/") > -1) {
            property = "en";
        }
        else if (value.indexOf("/non-entities/") > -1) {
            property = "ne";
        }
        return property;
    }

    static String getValue (String predicate) {
        int idx = predicate.lastIndexOf("#");
        if (idx>-1) {
            return predicate.substring(idx + 1);
        }
        else {
            idx = predicate.lastIndexOf("/");
            if (idx>-1) {
                return predicate.substring(idx + 1);
            }
            else {
                return predicate;
            }
        }
    }

    static String getValueWithoutFrame (String predicate) {
        int idx = predicate.lastIndexOf("@");
        if (idx>-1) {
            return predicate.substring(idx + 1);
        }
        else {
                return predicate;
        }
    }

    static JSONObject getClassesJSONObjectFromInstanceStatement (ArrayList<Statement> statements) throws JSONException {
        JSONObject jsonClassesObject = new JSONObject();
        ArrayList<String> coveredValues = new ArrayList<String>();

        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            if (predicate.endsWith("#type")) {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String [] values = object.split(",");
                for (int j = 0; j < values.length; j++) {
                    String value = values[j];
                    String property = getNameSpaceString(value);
                    if (!property.isEmpty() && !property.equalsIgnoreCase("sem")) {
                        value = getValue(value);
                        if (!coveredValues.contains(property+value)) {
                            coveredValues.add(property+value);
                            jsonClassesObject.append(property, value);
                        }
                    }
                }
            }
        }
        return jsonClassesObject;
    }

    static void getFrameNetSuperFramesJSONObjectFromInstanceStatement (JSONObject parent,
                                                                       ArrayList<Statement> statements
    ) throws JSONException {
        ArrayList<String> coveredValues = new ArrayList<String>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            if (predicate.endsWith("#type")) {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String [] values = object.split(",");
                for (int j = 0; j < values.length; j++) {
                    String value = values[j];
                    String property = getNameSpaceString(value);
                    if (property.equalsIgnoreCase("fn")) {
                        value = getValue(value);
                       // System.out.println("value = " + value);
                        ArrayList<String> parents = new ArrayList<String>();

                        frameNetReader.getParentChain(value, parents, topFrames);
                        if (parents.size()==0) {
                            parent.append("fnsuperframes", value);
                        }
                        else {
                            for (int k = 0; k < parents.size(); k++) {
                                String parentFrame = parents.get(k);
                                if (!coveredValues.contains(parentFrame)) {
                                    coveredValues.add(parentFrame);
                                    parent.append("fnsuperframes", parentFrame);
                                }
                            }
                        }
                      //  System.out.println("value = " + value);
                      //  System.out.println("\tparents = " + parents.toString());

                       /*   String superFrame = "";
                            if (frameNetReader.subToSuperFrame.containsKey(value)) {
                            ArrayList<String> superFrames = frameNetReader.subToSuperFrame.get(value);
                            for (int k = 0; k < superFrames.size(); k++) {
                                superFrame =  superFrames.get(k);
                                if (!coveredValues.contains(superFrame)) {
                                    coveredValues.add(superFrame);
                                    parent.append("fnsuperframes", superFrame);
                                }

                            }
                        }*/
                    }
                }
            }
        }
    }

    static void getEsoSuperClassesJSONObjectFromInstanceStatement (JSONObject parent,
                                                                       ArrayList<Statement> statements
    ) throws JSONException {
        ArrayList<String> coveredValues = new ArrayList<String>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            if (predicate.endsWith("#type")) {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String [] values = object.split(",");
                for (int j = 0; j < values.length; j++) {
                    String value = values[j];
                    String property = getNameSpaceString(value);
                    if (property.equalsIgnoreCase("eso")) {
                        value = getValue(value);
                        ArrayList<String> parents = new ArrayList<String>();
                        String parentClass = "";
                        esoReader.getParentChain(value, parents);
                        if (parents.size()==0) {
                            parentClass = value;
                        }
                        else {
                            if (esoLevel>parents.size()) {
                               // parentClass = parents.get(parents.size()-1)+"_"+value;
                                parentClass = value;
                            }
                            else {
                                int p = parents.size()-esoLevel;
                                parentClass = parents.get(p)+"_"+value;
                            }

                          //  parent.append("esosuperclasses", parents.get(parents.size()));

/*
                            for (int k = 0; k < parents.size(); k++) {
                                String parentFrame = parents.get(k);
                                if (!coveredValues.contains(parentFrame)) {
                                    coveredValues.add(parentFrame);
                                    parent.append("esosuperclasses", parentFrame);
                                }
                            }
*/
                        }
/*
                        System.out.println("value = " + value);
                        System.out.println("parents.toString() = " + parents.toString());
                        System.out.println("parentClass = " + parentClass);
*/
                        parent.append("esosuperclasses", parentClass);

                    }
                }
            }
        }
    }

    static JSONObject getActorsJSONObjectFromInstanceStatementORG (ArrayList<Statement> statements) throws JSONException {
        JSONObject jsonActorsObject = new JSONObject();
        ArrayList<String> esoActors = new ArrayList<String>();
        ArrayList<String> fnActors = new ArrayList<String>();
        ArrayList<String> pbActors = new ArrayList<String>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            if (predicate.toLowerCase().endsWith("time")) {
                ///
            }
            else if (predicate.toLowerCase().endsWith("hasactor")) {
                ///
            }
            else {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String property = getNameSpaceString(predicate);
                if (!property.isEmpty()) {


                    if (ACTORNAMESPACES.indexOf(property)>-1 || ACTORNAMESPACES.isEmpty()) {
                        if (property.equalsIgnoreCase("pb")) {
                            predicate = property + "/" + RoleLabels.normalizeProbBankValue(getValue(predicate));
                        }
                        else {
                            predicate = property + "/" + getValueWithoutFrame(getValue(predicate));
                           // System.out.println("property = " + property);
                        }
                        String[] values = object.split(",");
                        ArrayList<String> coveredValues = new ArrayList<String>();
                        for (int j = 0; j < values.length; j++) {
                            String value = values[j];
                            String ns = getNameSpaceString(value);
                            if (!ns.isEmpty()) {
                                value = ns+":"+ getValue(value);
                            }
                            value = value.replace("+", "_"); //// just to make it look nicer
                            if (!coveredValues.contains(value)) {
                                coveredValues.add(value);
                                jsonActorsObject.append(predicate, value);
                                if (actorCount.containsKey(value)) {
                                    Integer cnt = actorCount.get(value);
                                    cnt++;
                                    actorCount.put(value, cnt);
                                }
                                else {
                                    actorCount.put(value,1);
                                }
                            }
                        }
                    }
                }
            }
        }
        return jsonActorsObject;
    }

    static JSONObject getActorsJSONObjectFromInstanceStatement (ArrayList<Statement> statements) throws JSONException {
        ArrayList<String> coveredActors = new ArrayList<String>();
        JSONObject jsonActorsObject = new JSONObject();
        ArrayList<Triple> eventTriples = new ArrayList<Triple>();
        ArrayList<String> esoActors = new ArrayList<String>();
        ArrayList<String> fnActors = new ArrayList<String>();
        ArrayList<String> pbActors = new ArrayList<String>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            if (predicate.toLowerCase().endsWith("time")) {
                ///
            }
            else if (predicate.toLowerCase().endsWith("hasactor")) {
                ///
            }
            else {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String property = getNameSpaceString(predicate);
                if (!property.isEmpty()) {
                        if (property.equalsIgnoreCase("pb")) {
                            predicate = property + "/" + RoleLabels.normalizeProbBankValue(getValue(predicate));
                        }
                        else {
                            predicate = property + "/" + getValueWithoutFrame(getValue(predicate));
                            // System.out.println("property = " + property);
                        }
                        String[] values = object.split(",");
                        for (int j = 0; j < values.length; j++) {
                            String value = values[j];
                            String ns = getNameSpaceString(value);
                            if (!ns.isEmpty()) {
                                value = ns + ":" + getValue(value);
                            }
                            value = value.replace("+", "_"); //// just to make it look nicer
                            if (property.equalsIgnoreCase("pb")) {
                                if (!pbActors.contains(value)) {
                                    pbActors.add(value);
                                }
                            }
                            else if (property.equalsIgnoreCase("fn")) {
                                if (!fnActors.contains(value)) {
                                    fnActors.add(value);
                                }
                            }
                            else if (property.equalsIgnoreCase("eso")) {
                                if (!esoActors.contains(value)) {
                                    esoActors.add(value);
                                }
                            }
                            Triple triple = new Triple();
                            triple.setPredicate(predicate);
                            triple.setObject(value);
                            eventTriples.add(triple);

                            if (actorCount.containsKey(value)) {
                                Integer cnt = actorCount.get(value);
                                cnt++;
                                actorCount.put(value, cnt);
                            }
                            else {
                                actorCount.put(value,1);
                            }
                        }
                    }
            }
        }
        //// ESO takes priority, then FN then all other roles
        /// we only add actors with pb if the object does not have a fn relation
        /// and we add actors with fn relations only if they do not have a eso relation
        /// finally add all others
        for (int i = 0; i < eventTriples.size(); i++) {
            Triple triple = eventTriples.get(i);
            if (triple.getPredicate().startsWith("eso")) {
                if (!coveredActors.contains(triple.getObject())) {
                    jsonActorsObject.append(triple.getPredicate(), triple.getObject());
                    coveredActors.add(triple.getObject());
                }
            }
            else if (!esoActors.contains((triple.getObject())) && triple.getPredicate().startsWith("fn")) {
                if (!coveredActors.contains(triple.getObject())) {
                    jsonActorsObject.append(triple.getPredicate(), triple.getObject());
                    coveredActors.add(triple.getObject());
                }
            }
            else if (!esoActors.contains((triple.getObject())) && !fnActors.contains((triple.getObject()))) {
                    if (!coveredActors.contains(triple.getObject())) {
                        jsonActorsObject.append(triple.getPredicate(), triple.getObject());
                        coveredActors.add(triple.getObject());
                    }
            }
        }

        /// we first add eso roles, then fn roles finally al others
        /// currently makes no difference since Maarten sorts in his own way
 /*       for (int i = 0; i < eventTriples.size(); i++) {
            Triple triple = eventTriples.get(i);
            if (triple.getPredicate().startsWith("eso")) {
                jsonActorsObject.append(triple.getPredicate(), triple.getObject());
            }
        }
        for (int i = 0; i < eventTriples.size(); i++) {
            Triple triple = eventTriples.get(i);
            if (!esoActors.contains((triple.getObject())) && triple.getPredicate().startsWith("fn")) {
                jsonActorsObject.append(triple.getPredicate(), triple.getObject());
            }
        } for (int i = 0; i < eventTriples.size(); i++) {
            Triple triple = eventTriples.get(i);
            if (!esoActors.contains((triple.getObject())) && !fnActors.contains((triple.getObject()))) {
                jsonActorsObject.append(triple.getPredicate(), triple.getObject());
            }
        }*/

        return jsonActorsObject;
    }


    static JSONObject getUniqueActorsJSONObjectFromInstanceStatement (ArrayList<Statement> statements) throws JSONException {
        JSONObject jsonActorsObject = new JSONObject();

        HashMap<String, ArrayList<Statement>> actorMap = new HashMap<String, ArrayList<Statement>>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            if (predicate.toLowerCase().endsWith("hastime")) {
                ///
            }
            else if (predicate.toLowerCase().endsWith("hasactor")) {
                ///
            }
            else {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                if (actorMap.containsKey(object)) {
                    ArrayList<Statement> actorStatements = actorMap.get(object);
                    actorStatements.add(statement);
                    actorMap.put(object, actorStatements);
                }
                else {
                    ArrayList<Statement> actorStatements = new ArrayList<Statement>();
                    actorStatements.add(statement);
                    actorMap.put(object, actorStatements);
                }
            }
        }
        Set keySet = actorMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            ArrayList<Statement> actorStatements = actorMap.get(key);
            String combinedPredicate = "";
            for (int i = 0; i < actorStatements.size(); i++) {
                Statement statement = actorStatements.get(i);
                String predicate = statement.getPredicate().getURI();
                String property = getNameSpaceString(predicate);
                if (!property.isEmpty()) {
                    if (ACTORNAMESPACES.indexOf(property)>-1 || ACTORNAMESPACES.isEmpty()) {
                        if (property.equalsIgnoreCase("pb")) {
                            predicate = property + "/" + RoleLabels.normalizeProbBankValue(getValue(predicate));
                        }
                        else {
                            predicate = property + "/" + getValue(predicate);
                        }
                        if (!combinedPredicate.isEmpty()) {
                            combinedPredicate+="+";
                        }
                        combinedPredicate += predicate;
                        /*String[] values = key.split(",");
                        ArrayList<String> coveredValues = new ArrayList<String>();
                        for (int j = 0; j < values.length; j++) {
                            String value = values[j];
                            if (!coveredValues.contains(value)) {
                                coveredValues.add(value);
                                jsonActorsObject.append(predicate, value);
                            }
                        }*/
                    }
                }
              //  break; //// only takes the first
            }
            if (!combinedPredicate.isEmpty()) {
                String[] values = key.split(",");
                ArrayList<String> coveredValues = new ArrayList<String>();
                for (int j = 0; j < values.length; j++) {
                    String value = values[j];
                    if (!coveredValues.contains(value)) {
                        coveredValues.add(value);
                        jsonActorsObject.append(combinedPredicate, value);
                    }
                }
            }
        }
        return jsonActorsObject;
    }

    static JSONObject getMentionsJSONObjectFromInstanceStatement (ArrayList<Statement> statements) throws JSONException {
        JSONObject jsonClassesObject = new JSONObject();
        ArrayList<String> coveredValues = new ArrayList<String>();

        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            if (predicate.endsWith("#denotedBy")) {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
               // System.out.println("object = " + object);
                /*
                object = http://en.wikinews.org/wiki/Aeroflot_negotiates_purchase_of_22_new_Boeing_787_Dreamliner_aircraft#char=20,28&word=w3&term=t3&sentence=1
object = http://en.wikinews.org/wiki/Aeroflot_negotiates_purchase_of_22_new_Boeing_787_Dreamliner_aircraft#char=2227,2235&word=w391&term=t391&sentence=18
object = http://en.wikinews.org/wiki/Aeroflot_negotiates_purchase_of_22_new_Boeing_787_Dreamliner_aircraft#char=135,143&word=w22&term=t22&sentence=3
object = http://en.wikinews.org/wiki/Aeroflot_negotiates_purchase_of_22_new_Boeing_787_Dreamliner_aircraft#char=20,28&word=w3&term=t3&sentence=1
object = http://en.wikinews.org/wiki/Aeroflot_negotiates_purchase_of_22_new_Boeing_787_Dreamliner_aircraft#char=2227,2235&word=w391&term=t391&sentence=18
object = http://en.wikinews.org/wiki/Aeroflot_negotiates_purchase_of_22_new_Boeing_787_Dreamliner_aircraft#char=135,143&word=w22&term=t22&sentence=3
object = http://en.wikinews.org/wiki/Aeroflot_negotiates_purchase_of_22_new_Boeing_787_Dreamliner_aircraft#char=217,228&word=w36&term=t36&sentence=3
object = http://en.wikinews.org/wiki/Aeroflot_negotiates_purchase_of_22_new_Boeing_787_Dreamliner_aircraft#char=217,228&word=w36&term=t36&sentence=3
object = http://en.wikinews.org/wiki/Boeing_rolls_out_first_787_Dreamliner_to_go_into_service#char=744,750&word=w144&term=t144&sentence=10
object = http://en.wikinews.org/wiki/Boeing_rolls_out_first_787_Dreamliner_to_go_into_service#char=775,779&word=w149&term=t149&sentence=10
object = http://en.wikinews.org/wiki/Boeing_pushes_back_737_replacement_development#char=659,667&word=w111&term=t111&sentence=6
                 */
/*
                "http://en.wikinews.org/wiki/Indonesia's_transport_minister_tells_airlines_not_to_buy_European_aircraft_due_to_EU_ban#char=85",
"88&word=w15&term=t15&sentence=1",
"http://en.wikinews.org/wiki/Indonesia's_transport_minister_tells_airlines_not_to_buy_European_aircraft_due_to_EU_ban#char=462",
"465&word=w88&term=t88&sentence=5",
"http://en.wikinews.org/wiki/Indonesia's_transport_minister_tells_airlines_not_to_buy_European_aircraft_due_to_EU_ban#char=415",
"421&word=w77&term=t77&sentence=4",
"http://en.wikinews.org/wiki/Indonesia's_transport_minister_tells_airlines_not_to_buy_European_aircraft_due_to_EU_ban#char=1122",
"1128&word=w222&term=t222&sentence=7"
                 */

                //object = http://en.wikinews.org/wiki/Boeing_pushes_back_737_replacement_development#char=659,667&word=w111&term=t111&sentence=6
                int idx = object.lastIndexOf("#");
                if (idx >-1) {
                    String uri = object.substring(0, idx);
                    String mentionString = object.substring(idx+1);
                    String[] values = object.split("&");
                    if (values.length==4) {
                        JSONObject mObject = new JSONObject();
                        String charOffset = values[0];
                        String tokens = values[1];
                        String terms = values[2];
                        String sentence = values[3];
                        mObject.append("uri", uri);
                        addValuesFromMention(mObject, "char", charOffset);
                        addValuesFromMention(mObject, "tokens", tokens);
                        addValuesFromMention(mObject, "terms", terms);
                        addValuesFromMention(mObject, "sentence", sentence);
                        jsonClassesObject.append("mentions", mObject);
                    }
                }
              //  jsonClassesObject.append("mentions", object);

            }
        }
        return jsonClassesObject;
    }

    static void addValuesFromMention (JSONObject object, String key, String str) throws JSONException {
        int idx_s = str.indexOf("=");
        String [] v = str.substring(idx_s+1).split(",");
        for (int i = 0; i < v.length; i++) {
            String s = v[i];
            object.append(key, s);
        }
    }

    static ArrayList<String> getValuesFromMention (String str) {
        ArrayList<String> values = new ArrayList<String>();
        int idx_s = str.indexOf("=");
        String [] v = str.substring(idx_s).split(",");
        for (int i = 0; i < v.length; i++) {
            String s = v[i];
            values.add(s);
        }
        return values;
    }

    static JSONObject getLabelsJSONObjectFromInstanceStatement (ArrayList<Statement> statements) throws JSONException {
        JSONObject jsonClassesObject = new JSONObject();
        ArrayList<String> coveredValues = new ArrayList<String>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            if (predicate.endsWith("#label")) {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String [] values = object.split(",");
                for (int j = 0; j < values.length; j++) {
                    String value = values[j];
                    if (!coveredValues.contains(value)) {
                        coveredValues.add(value);
                        jsonClassesObject.append("labels", value);
                    }
                }
            }
        }
        return jsonClassesObject;
    }

    static JSONObject getPrefLabelsJSONObjectFromInstanceStatement (ArrayList<Statement> statements) throws JSONException {
        JSONObject jsonClassesObject = new JSONObject();
        ArrayList<String> coveredValues = new ArrayList<String>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            if (predicate.endsWith("#prefLabel")) {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String [] values = object.split(",");
                for (int j = 0; j < values.length; j++) {
                    String value = values[j];
                    if (!coveredValues.contains(value)) {
                        coveredValues.add(value);
                        jsonClassesObject.append("prefLabel", value);
                    }
                }
            }
        }
        return jsonClassesObject;
    }

    static JSONObject getTopicsJSONObjectFromInstanceStatement (ArrayList<Statement> statements) throws JSONException {

        // skos:related    "air transport" , "aeronautical industry" , "transport regulations" , "air law" , "carrier" , "air safety" .
        JSONObject jsonClassesObject = new JSONObject();
        ArrayList<String> coveredValues = new ArrayList<String>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            if (predicate.endsWith("skos/core#related")) {
            //    System.out.println("predicate = " + predicate);
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String [] values = object.split(",");
                for (int j = 0; j < values.length; j++) {
                    String value = values[j];
                    if (!coveredValues.contains(value)) {
                        coveredValues.add(value);
                        jsonClassesObject.append("topics", value);
                    }
                }
            }
        }
        return jsonClassesObject;
    }


}
