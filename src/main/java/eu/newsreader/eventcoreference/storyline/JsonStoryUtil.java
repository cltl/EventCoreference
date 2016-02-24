package eu.newsreader.eventcoreference.storyline;

import eu.newsreader.eventcoreference.naf.CreateMicrostory;
import eu.newsreader.eventcoreference.objects.PhraseCount;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by piek on 17/02/16.
 */
public class JsonStoryUtil {

    static ArrayList<JSONObject> createStoryLinesForJSONArrayList (ArrayList<JSONObject> jsonObjects,
                                                                   int topicThreshold,
                                                                   int climaxThreshold,
                                                                   int eventLimit,
                                                                   String entityFilter
                                                                   )  throws JSONException {
        boolean DEBUG = false;
        ArrayList<JSONObject> groupedObjects = new ArrayList<JSONObject>(); /// keeps track which events are already in a story so that the same event does not end up in multiple stories
        ArrayList<JSONObject> singletonObjects = new ArrayList<JSONObject>(); /// keeps track of singleton stories, without bridging relations
        ArrayList<JSONObject> selectedEvents  = new ArrayList<JSONObject>();/// set of events above the climax threshold sorted according to this threshold
        ArrayList<JSONObject> storyObjects = new ArrayList<JSONObject>(); /// data struture for a story

        /// We build up a climax index over all the events
        //Vector<Integer> climaxIndex = new Vector<Integer>();
        //1. We determine the climax score for each individual event
        // We sum the inverse sentence numbers of all mentions
        TreeSet climaxObjects = determineClimaxValues(jsonObjects, climaxThreshold);
        //TreeSet climaxObjects = determineClimaxValuesFirstMentionOnly(jsonObjects);
        Iterator<JSONObject> sortedObjects = climaxObjects.iterator();
        while (sortedObjects.hasNext()) {
            JSONObject jsonObject = sortedObjects.next();
            selectedEvents.add(jsonObject);
        }


        System.out.println("Events above climax threshold = " + climaxObjects.size());
        int eventCount = 0;
        sortedObjects = climaxObjects.iterator();
        while (sortedObjects.hasNext()) {
            if (eventCount>eventLimit && eventLimit>-1) {
                //// the storyteller quits when it reaches the event limit. this is to prevent the visualisation to be cluttered with to many stories
                break;
            }
            JSONObject jsonObject = sortedObjects.next();
            if (!hasObject(groupedObjects, jsonObject)) {
                //// this event is not yet part of a story and is the next event with climax value
                //// we use this to create a new story by adding other events with bridging relations into the storyObjects ArrayList
                try {
                    storyObjects = new ArrayList<JSONObject>(); /// initialise the ArrayList for the story events

                    /// create the administrative fields in the JSON structure for a event that define story membership
                    Integer groupClimax = Integer.parseInt(jsonObject.get("climax").toString());
                    Float size = new Float(1);
                    if (groupClimax > 0) {
                        // size = 5 * Float.valueOf((float) (1.0 * groupClimax / groupClimax));
                        size = Float.valueOf((float) groupClimax / 4);
                    }
                    jsonObject.put("size", size.toString());
                    String group = climaxString(groupClimax) + ":" + jsonObject.get("labels").toString();
                    String groupName = jsonObject.get("labels").toString();
                    String groupScore = climaxString(groupClimax);
                    group += getfirstActorByRoleFromEvent(jsonObject, "pb/A1"); /// for representation purposes
                    groupName += getfirstActorByRoleFromEvent(jsonObject, "pb/A1");
                    jsonObject.put("group", group);
                    jsonObject.put("groupName", groupName);
                    jsonObject.put("groupScore", groupScore);

                    //// add the climax event to the story ArrayList
                    storyObjects.add(jsonObject);


                    //// now we look for other events with bridging relations
                    ArrayList<JSONObject> bridgedEvents = new ArrayList<JSONObject>();

                    ArrayList<JSONObject> coevents =  new ArrayList<JSONObject>();
                    if (entityFilter.isEmpty()) {
                        coevents = CreateMicrostory.getEventsThroughCoparticipation(selectedEvents, jsonObject);
                    }
                    else {
                        coevents = CreateMicrostory.getEventsThroughCoparticipation(entityFilter, selectedEvents, jsonObject);
                    }
                    if (coevents.size()>0) {
                        bridgedEvents = coevents;
                        System.out.println("coevents.size() = " + coevents.size());
                    }

                    ArrayList<JSONObject> topicevents = new ArrayList<JSONObject>();
                    if (topicThreshold>0) {
                        topicevents = CreateMicrostory.getEventsThroughTopicBridging(selectedEvents, jsonObject, topicThreshold);
                    }
                    if (topicevents.size()>0) {
                        System.out.println("topicevents = " + topicevents.size());
                    }
                    if (topicevents.size()>0) {
                        if (coevents.size()==0) {
                            bridgedEvents = topicevents;
                        }
                        else {
/*
                            bridgedEvents = mergeEventObjects(coevents, topicevents);
                            if (bridgedEvents.size()>0) {
                                System.out.println("merged coevents and topicevents.size() = " + bridgedEvents.size());
                            }
*/
                            bridgedEvents = intersectEventObjects(coevents, topicevents);
                            if (bridgedEvents.size()>0) {
                                System.out.println("intersection coevents and topicevents.size() = " + bridgedEvents.size());
                            }
                        }
                    }
                    else {
                        //// stick to coevents only
                    }
                    if (bridgedEvents.size()>0) {
                        System.out.println("final set of bridged events.size() = " + bridgedEvents.size());
                    }


                    for (int i = 0; i < bridgedEvents.size(); i++) {
                        JSONObject object = bridgedEvents.get(i);
                        if (!hasObject(groupedObjects, object)) {
                            eventCount++;
                            addObjectToGroup(
                                    storyObjects,
                                    group,
                                    groupName,
                                    groupScore,
                                    object,
                                    8,
                                    climaxThreshold);
                        }
                        else {
                            ///// this means that the bridged event was already consumed by another story
                            ///// that's a pity for this story. It is already consumed
                        }
                    }

                    //// use this code if singleton stories are represented also
/*
                    for (int i = 0; i < storyObjects.size(); i++) {
                        JSONObject object = storyObjects.get(i);
                        groupedObjects.add(object);
                    }
*/

                    //// use this code if singletons are treated separately
                    if (storyObjects.size()>1) {
                        for (int i = 0; i < storyObjects.size(); i++) {
                            JSONObject object = storyObjects.get(i);
                            groupedObjects.add(object);
                        }
                    }
                    else {
                        for (int i = 0; i < storyObjects.size(); i++) {
                            JSONObject object = storyObjects.get(i);
                            singletonObjects.add(object);
                        }
                    }

                } catch (JSONException e) {
                    // e.printStackTrace();
                }

            }
            //  break;

        } // end of while objects in sorted climaxObjects

        //// now we handle the singleton events
        String group = "001:unrelated events";
        String groupName = "unrelated event";
        String groupScore = "001";
        climaxObjects = determineClimaxValues(singletonObjects, climaxThreshold);
        sortedObjects = climaxObjects.iterator();
        while (sortedObjects.hasNext()) {
            JSONObject jsonObject = sortedObjects.next();
            jsonObject.put("group", group);
            jsonObject.put("groupName", groupName);
            jsonObject.put("groupScore", groupScore);
            eventCount++;
            addObjectToGroup(
                    storyObjects,
                    group,
                    groupName,
                    groupScore,
                    jsonObject,
                    2,
                    climaxThreshold);
        }

        return groupedObjects;
    }

 /*   static ArrayList<JSONObject> createStoryLinesForJSONArrayListOrg (ArrayList<JSONObject> jsonObjects,
                                                                      FrameNetReader frameNetReader,
                                                                      int climaxThreshold)  throws JSONException {

        String entity = "Airbus";
        String entityTimeLine = entity+"\n";
        String entityMatch = "";
        String debugStr = "";
        boolean PRINTEXAMPLE = false;
        boolean DEBUG = false;
        *//*
        	2004-10	4-killed[t85]	10-leveled[t182]	36-charges[t803]
	        2004-10	4-favored[t97]	31-favor[t689]
         *//*

        ArrayList<JSONObject> groupedObjects = new ArrayList<JSONObject>();
        /// We build up a climax index over all the events
        //Vector<Integer> climaxIndex = new Vector<Integer>();

        //1. We determine the climax score for each individual event
        // We sum the inverse sentence numbers of all mentions
        TreeSet climaxObjects = determineClimaxValues(jsonObjects, climaxThreshold);
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
                                    6,
                                    climaxThreshold);
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
                                        8,
                                        climaxThreshold);
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
                                        8,
                                        climaxThreshold);
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
*//*
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
*//*


        if (PRINTEXAMPLE) {
            /////// FOR EXAMPLE OUTPUT
            System.out.println("entityTimeLine = " + entityTimeLine);
            /////// FOR EXAMPLE OUTPUT
        }

        return groupedObjects;
    }*/



    /**
     * Determines the climax values by summing the inverse values of the sentence nr of each mention
     * @param jsonObjects
     * @return
     */
    static TreeSet determineClimaxValues (ArrayList<JSONObject> jsonObjects, int climaxThreshold) {
        //1. We determine the climax score for each individual event and return a sorted list by climax
        // We sum the inverse sentence numbers of all mentions
        TreeSet climaxObjects = new TreeSet(new climaxCompare());
        Double maxClimax = -1.0;
        for (int i = 0; i < jsonObjects.size(); i++) {
            JSONObject jsonObject = jsonObjects.get(i);
            // System.out.println("jsonObject.toString() = " + jsonObject.toString());
            try {
                Double sumClimax =0.0;
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                int earliestEventMention = -1;
                for (int j = 0; j < mentions.length(); j++) {
                    JSONObject mentionObject = (JSONObject) mentions.get(j);
                    // System.out.println("charValue = " + mentionObject.getString("char"));
                    // System.out.println("sentenceValue = " + mentionObject.getString("sentence"));
                    JSONArray sentences = null;
                    try {
                        sentences = mentionObject.getJSONArray("sentence");
                    } catch (JSONException e) {
                        //  e.printStackTrace();
                    }
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
                        JSONArray charValues = null;
                        try {
                            charValues = mentionObject.getJSONArray("char");
                        } catch (JSONException e) {
                            //     e.printStackTrace();
                        }
                        //"["4160","4165"]"
                        if (charValues!=null ) {
                            String charValue = charValues.get(0).toString();
                           // System.out.println("charValue = " + charValue);
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
                }

                //System.out.println("sumClimax = " + sumClimax);
                if (Double.isInfinite(sumClimax)) {
                    jsonObject.put("climax", "0");
                }
                else {
                    if (sumClimax>maxClimax) {
                        maxClimax = sumClimax;
                    }
                    jsonObject.put("climax", sumClimax);
                }
            } catch (JSONException e) {
               //   e.printStackTrace();
                try {
                    jsonObject.put("climax", "0");
                } catch (JSONException e1) {
                 //   e1.printStackTrace();
                }

            }
        }
        //System.out.println("maxClimax = " + maxClimax);
        /// next we normalize the climax values and store it in the tree
        for (int i = 0; i < jsonObjects.size(); i++) {
            JSONObject jsonObject = jsonObjects.get(i);
            try {
                Double climax = Double.parseDouble(jsonObject.get("climax").toString());
                // Double proportion = Math.log(climax/maxClimax);
                Double proportion = climax/maxClimax;
                Integer climaxInteger = new Integer ((int)(100*proportion));
                if (climaxInteger==0) {
                    climaxInteger=1;
                }
/*                System.out.println("climax = " + climax);
                System.out.println("proportion = " + proportion);
                System.out.println("climaxInteger = " + climaxInteger);*/
                if (climaxInteger>=climaxThreshold) {
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
                   e.printStackTrace();
            }
        }
        return climaxObjects;
    }

    /**
     * Determines the climax values by summing the inverse values of the sentence nr of each mention
     * @param jsonObjects
     * @return
     */
    static TreeSet determineClimaxValues_org_using_old_mention_structure (ArrayList<JSONObject> jsonObjects, int climaxThreshold) {
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
                // Double proportion = Math.log(climax/maxClimax);
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


    static int countMentions(ArrayList<JSONObject> objects) {
        int nMentions = 0;
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.get(i);
            try {
                JSONArray mentions = (JSONArray) jsonObject.get("mentions");
                nMentions+=mentions.length();
            } catch (JSONException e) {
               // e.printStackTrace();
            }
        }
        return nMentions;
    }
    static int countGroups(ArrayList<JSONObject> events) {
        ArrayList<String> groups = new ArrayList<String>();
        for (int i = 0; i < events.size(); i++) {
            JSONObject jsonObject = events.get(i);
            try {
                String groupValue = jsonObject.get("group").toString();
               // System.out.println("groupValue = " + groupValue);
                if (!groups.contains(groupValue)) {
                    groups.add(groupValue);
                }
            } catch (JSONException e) {
             //   e.printStackTrace();
            }
        }
        return groups.size();
    }

    public static int countActors(ArrayList<JSONObject> events) {
        ArrayList<String> actorNames = new ArrayList<String>();
        for (int i = 0; i < events.size(); i++) {
            JSONObject oEvent = events.get(i);
            JSONObject oActorObject = null;
            try {
                oActorObject = oEvent.getJSONObject("actors");
                Iterator oKeys = oActorObject.sortedKeys();
                while (oKeys.hasNext()) {
                    String oKey = oKeys.next().toString();
                    try {
                        JSONArray actors = oActorObject.getJSONArray(oKey);
                        for (int j = 0; j < actors.length(); j++) {
                            String nextActor = actors.getString(j);
                            nextActor = nextActor.substring(nextActor.lastIndexOf("/") + 1);
                            if (!actorNames.contains(nextActor)) {
                                actorNames.add(nextActor);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                // e.printStackTrace();
            }
        }
        return actorNames.size();
    }

    public static HashMap <String, Integer> createActorCount (ArrayList<JSONObject> events) {
        HashMap <String, Integer> actorCount = new HashMap<String, Integer>();
        for (int i = 0; i < events.size(); i++) {
            JSONObject oEvent = events.get(i);
            JSONObject oActorObject = null;
            try {
                oActorObject = oEvent.getJSONObject("actors");
                Iterator oKeys = oActorObject.sortedKeys();
                while (oKeys.hasNext()) {
                    String oKey = oKeys.next().toString();
                    try {
                        JSONArray actors = oActorObject.getJSONArray(oKey);
                        for (int j = 0; j < actors.length(); j++) {
                            String nextActor = actors.getString(j);
                            nextActor = nextActor.substring(nextActor.lastIndexOf("/") + 1);
                            if (actorCount.containsKey(nextActor)) {
                                Integer cnt = actorCount.get(nextActor);
                                cnt++;
                                actorCount.put(nextActor, cnt);
                            }
                            else {
                                actorCount.put(nextActor,1);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                // e.printStackTrace();
            }
        }
        return actorCount;
    }


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
    static ArrayList<JSONObject> mergeEventObjects(ArrayList<JSONObject> set1, ArrayList<JSONObject> set2) throws JSONException {
        ArrayList<JSONObject> merge = set2;
        for (int i = 0; i < set1.size(); i++) {
            JSONObject object1 = set1.get(i);
            boolean has = false;
            for (int j = 0; j < merge.size(); j++) {
                JSONObject object2 = merge.get(j);
                if (object1.get("instance").toString().equals(object2.get("instance").toString())) {
                    has = true;
                    break;
                }
            }
            merge.add(object1);
        }
        return  merge;
    }


    static void addObjectToGroup (ArrayList<JSONObject> groupObjects,
                                  String group,
                                  String groupName,
                                  String groupScore,
                                  JSONObject object,
                                  int divide,
                                  int climaxThreshold) throws JSONException {
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
}
