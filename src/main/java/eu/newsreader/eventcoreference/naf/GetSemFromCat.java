package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafTerm;
import eu.kyotoproject.kaf.KafWordForm;
import eu.newsreader.eventcoreference.objects.JsonEvent;
import eu.newsreader.eventcoreference.storyline.JsonStoryUtil;
import eu.newsreader.eventcoreference.storyline.PerspectiveJsonObject;
import eu.newsreader.eventcoreference.util.Util;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;

/**
 * Created by piek on 22/05/17.
 */

//@TODO Adapt this class to create RDF-TRiG
public class GetSemFromCat extends DefaultHandler {
    public class CatLink {
        private String source;
        private String target;
        private String linkId;
        private String relType;

        public CatLink() {
            init();
        }

        public void init() {
            this.linkId = "";
            this.source = "";
            this.relType = "";
            this.target = "";
        }

        public String getRelType() {
            return relType;
        }

        public void setRelType(String relType) {
            if (relType != null) this.relType = relType;
        }

        public String getLinkId() {
            return linkId;
        }

        public void setLinkId(String linkId) {
            this.linkId = linkId;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            if (source != null) this.source = source;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            if (target != null) this.target = target;
        }
    }

    public class PlotLink extends CatLink {
        /*
        <PLOT_LINK r_id="547121" CAUSED_BY="FALSE" CAUSES="TRUE" relType="FALLING_ACTION" SIGNAL="" >
<source m_id="27" />
<target m_id="25" />
</PLOT_LINK>
         */
        private String caused_by;
        private String causes;
        private String signal;

        public PlotLink() {
            init();
            caused_by = "FALSE";
            causes = "FALSE";
            signal = "";
        }

        public String getCaused_by() {
            return caused_by;
        }

        public void setCaused_by(String caused_by) {
            this.caused_by = caused_by;
        }

        public String getCauses() {
            return causes;
        }

        public void setCauses(String causes) {
            this.causes = causes;
        }


        public String getSignal() {
            return signal;
        }

        public void setSignal(String signal) {
            this.signal = signal;
        }
    }

    public class TimeLink extends CatLink {
        /*
        <TLINK r_id="547988" relType="BEFORE" comment="" contextualModality="ACT" >
<source m_id="23" />
<target m_id="59" />
</TLINK>
         */
        private String contextualModality;

        public TimeLink() {
            init();
            contextualModality = "";
        }

        public String getContextualModality() {
            return contextualModality;
        }

        public void setContextualModality(String contextualModality) {
            this.contextualModality = contextualModality;
        }
    }

    public class LocationLink extends CatLink {
    }

    public class ActorLink extends CatLink {
    }

    public class TimeTerm extends KafTerm {
        private String anchorTimeId;
        private boolean dct;
        private String value;

        public TimeTerm() {
            anchorTimeId = "";
            dct = false;
            value = "";
        }

        public String getAnchorTimeId() {
            return anchorTimeId;
        }

        public void setAnchorTimeId(String anchorTimeId) {
            this.anchorTimeId = anchorTimeId;
        }

        public boolean isDct() {
            return dct;
        }

        public void setDct(boolean dct) {
            this.dct = dct;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    String value = "";
    String span = "";
    String source = "";
    String target = "";
    String relType = "";
    String causedBy = "";
    String causes = "";
    String contextualModality = "";
    String signal = "";
    String crossDocId = "";
    String singletonId = "";

    KafWordForm kafWordForm;
    KafTerm kafTerm;
    TimeTerm timeTerm;
    boolean TIME;
    ArrayList<String> spans;
    public HashMap<String, String> termToCorefMap;
    public HashMap<String, String> eventToStoryMap;
    public HashMap<String, String> instanceToStoryMap;
    public HashMap<String, ArrayList<KafTerm>> eventCorefMap;
    public HashMap<String, ArrayList<String>> storyMap;
    public HashMap<String, ArrayList<ActorLink>> eventActorLinksMap;
    public HashMap<String, ArrayList<LocationLink>> eventLocationLinksMap;
    public HashMap<String, ArrayList<TimeLink>> eventTimeLinksMap;
    public HashMap<String, ArrayList<PlotLink>> eventPlotLinksMap;
    public HashMap<String, JSONObject> eventMentionMap;
    public ArrayList<KafWordForm> kafWordFormArrayList;
    public ArrayList<KafTerm> eventTermArrayList;

    public HashMap<String, KafTerm> actorMap;
    public HashMap<String, KafTerm> locationMap;
    public HashMap<String, TimeTerm> timeMap;

    public ArrayList<KafTerm> actorTermArrayList;
    public ArrayList<KafTerm> locationTermArrayList;
    public ArrayList<TimeTerm> timeTermArrayList;
    public ArrayList<TimeLink> timeLinks;
    public ArrayList<LocationLink> locationLinks;
    public ArrayList<PlotLink> plotLinks;
    public ArrayList<ActorLink> actorLinks;
    String fileName;
    boolean REFERSTO = false;


    public GetSemFromCat() {
        initAll();
    }


    static public void main(String[] args) {
        String demo = "/Users/piek/Desktop/WorkshopsConferences/StorylineWorkshop/UncertaintyVisualizationGold/app/data";
        String folder = "";
        String pathToCatFile = "";
        String fileExtension = "";

        //pathToCatFile = "/Users/piek/Desktop/StorylineWorkshop/ECB-manual/ECBplus_Topic37CAT/37_9ecbplus.xml";
        folder = "/Users/piek/Desktop/WorkshopsConferences/StorylineWorkshop/ECB-manual/ECBStar-mergeT37";
        fileExtension = ".xml";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("--cat-file") && args.length > (i + 1)) {
                pathToCatFile = args[i + 1];
            } else if (arg.equalsIgnoreCase("--folder") && args.length > (i + 1)) {
                folder = args[i + 1];
            } else if (arg.equalsIgnoreCase("--file-extension") && args.length > (i + 1)) {
                fileExtension = args[i + 1];
            }
        }
        System.out.println("fileExtension = " + fileExtension);
        System.out.println("folder = " + folder);
        GetSemFromCat jsonFromCat = new GetSemFromCat();

        if (!pathToCatFile.isEmpty()) {
            File catFile = new File(pathToCatFile);
            folder = catFile.getParent();
            jsonFromCat.parseFile(pathToCatFile);
            jsonFromCat.getPhrases();
            jsonFromCat.getActorLinks();
            jsonFromCat.buildEventCorefMap();
            jsonFromCat.buildLinkMaps();
            jsonFromCat.buildTermMaps();
            try {
                jsonFromCat.buildMentionMap();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonFromCat.makeGroupsForInstance();
            System.out.println("jsonFromCat.eventTermArrayList = " + jsonFromCat.eventTermArrayList.size());
            System.out.println("jsonFromCat.eventCorefMap = " + jsonFromCat.eventCorefMap.size());
            ArrayList<JSONObject> events = null;
            try {
                events = jsonFromCat.getJsonObjectsFromCorefsets();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonFromCat.writeJsonObjectArray(demo, "ecb*", events);
        } else if (!folder.isEmpty()) {
            ArrayList<File> files = Util.makeFlatFileList(new File(folder), fileExtension);
            ArrayList<JSONObject> events = new ArrayList<JSONObject>();
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                //System.out.println("file.getName() = " + file.getName());
                jsonFromCat.parseFile(file.getAbsolutePath());
                jsonFromCat.getPhrases();
                jsonFromCat.getActorLinks();
                jsonFromCat.buildEventCorefMap();
                jsonFromCat.buildLinkMaps();
                jsonFromCat.buildTermMaps();
                try {
                    jsonFromCat.buildMentionMap();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //System.out.println("jsonFromCat plotlinks = " + jsonFromCat.plotLinks.size());

            }
            try {
                jsonFromCat.makeGroupsForInstance();
                events = jsonFromCat.getJsonObjectsFromCorefsets();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println("jsonFromCat.eventCorefMap = " + jsonFromCat.eventCorefMap.size());
            System.out.println("jsonFromCat.instanceToStoryMap.size() = " + jsonFromCat.instanceToStoryMap.size());
            jsonFromCat.writeJsonObjectArray(demo, "ecb*", events);
        }
        System.out.println("DONE.");
    }

    public void makeGroupsForInstance() {
        HashMap<String, ArrayList<String>> groupings = new HashMap<String, ArrayList<String>>();
        Set keySet = eventCorefMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String corefId1 = keys.next();
            ArrayList<String> group = new ArrayList<String>();
            group.add(corefId1);
            Iterator<String> keys2 = keySet.iterator();
            while (keys2.hasNext()) {
                String corefId2 = keys2.next();
                if (!corefId1.equals(corefId2)) {
                    if (areRelated(corefId1, corefId2)) {
                        group.add(corefId2);
                    }
                }
            }
            groupings.put(corefId1, group);
        }
        System.out.println("before chaining groupings.size() = " + groupings.size());
        chainGroupings(groupings);
        System.out.println("after chaining groupings = " + groupings.size());
        keySet = groupings.keySet();
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            ArrayList<String> corefIds = groupings.get(key);
            if (corefIds.size() > 0) {
                String climax = getClimaxEvent(corefIds);
                if (climax.isEmpty()) {
                    climax = key;
                }
                if (!climax.isEmpty()) {
                    System.out.println("climax = " + climax);
                    for (int i = 0; i < corefIds.size(); i++) {
                        String coref = corefIds.get(i);
                        instanceToStoryMap.put(coref, climax);
                    }
                } else {

                }
            } else {
                // System.out.println("absorbed key = " + key);
            }
        }
    }

    public int getClimaxScore(String corefId) {
        int cnt = 0;
        if (eventCorefMap.containsKey(corefId)) {
            ArrayList<KafTerm> eventTerms = eventCorefMap.get(corefId);
            for (int j = 0; j < eventTerms.size(); j++) {
                KafTerm eventTerm = eventTerms.get(j);
                String eventId = eventTerm.getTid();
                if (eventPlotLinksMap.containsKey(eventId)) {
                    ArrayList<PlotLink> plotLinks = eventPlotLinksMap.get(eventId);
                    for (int k = 0; k < plotLinks.size(); k++) {
                        PlotLink plotLink = plotLinks.get(k);
                        if (plotLink.getSource().equals(eventId) && plotLink.getRelType().equalsIgnoreCase("FALLING_ACTION")) {
                            cnt++;
                        } else if (plotLink.getTarget().equals(eventId) && plotLink.getRelType().equalsIgnoreCase("PRECONDITION")) {
                            cnt++;
                        }
                    }
                }
            }
        }
        return cnt;
    }

    public String getClimaxEvent(ArrayList<String> corefIds) {
        String climax = "";
        int max = 0;
        for (int i = 0; i < corefIds.size(); i++) {
            String corefId = corefIds.get(i);
            int cnt = getClimaxScore(corefId);
            if (cnt > max) {
                climax = corefId;
                max = cnt;
            }
        }
        return climax;
    }


    public void chainGroupings(HashMap<String, ArrayList<String>> groupings) {
        boolean CHAIN = false;
        Set keySet = groupings.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key1 = keys.next();
            ArrayList<String> group1 = groupings.get(key1);
            Iterator<String> keys2 = keySet.iterator();
            while (keys2.hasNext()) {
                String key2 = keys2.next();
                if (!key2.equals(key1)) {
                    ArrayList<String> group2 = groupings.get(key2);
                    if (!Collections.disjoint(group1, group2)) {
                        ///absorb;
                        for (int i = 0; i < group2.size(); i++) {
                            String corefId = group2.get(i);
                            if (!group1.contains(corefId)) {
                                //System.out.println("adding corefId = " + corefId);
                                group1.add(corefId);
                                CHAIN = true;
                            }
                        }
                        groupings.put(key2, new ArrayList<String>());
                        groupings.put(key1, group1);
                    }
                }
            }
        }
        if (CHAIN) {
            chainGroupings(groupings);
        }
    }

    public boolean areRelated(String corefId1, String corefId2) {
        if (this.eventCorefMap.containsKey(corefId1) && this.eventCorefMap.containsKey(corefId2)) {
            ArrayList<KafTerm> eventTerms1 = this.eventCorefMap.get(corefId1);
            ArrayList<String> relatedMentions = new ArrayList<String>();
            for (int i = 0; i < eventTerms1.size(); i++) {
                KafTerm eventTerm = eventTerms1.get(i);
                String eventId = eventTerm.getTid();
                if (eventPlotLinksMap.containsKey(eventId)) {
                    ArrayList<PlotLink> plotLinks = eventPlotLinksMap.get(eventId);
                    for (int j = 0; j < plotLinks.size(); j++) {
                        PlotLink plotLink = plotLinks.get(j);
                        String otherEventId = "";
                        if (!plotLink.getSource().equals(eventId)) {
                            otherEventId = plotLink.getSource();
                        } else {
                            otherEventId = plotLink.getTarget();
                        }
                        if (!relatedMentions.contains(otherEventId)) {
                            relatedMentions.add(otherEventId);
                        }
                    }
                }
                if (eventTimeLinksMap.containsKey(eventId)) {
                    ArrayList<TimeLink> tlinks = eventTimeLinksMap.get(eventId);
                    for (int j = 0; j < tlinks.size(); j++) {
                        TimeLink timeLink = tlinks.get(j);
                        String otherEventId = "";
                        if (!timeLink.getSource().equals(eventId)) {
                            otherEventId = timeLink.getSource();
                        } else {
                            otherEventId = timeLink.getTarget();
                        }
                        if (!relatedMentions.contains(otherEventId)) {
                            relatedMentions.add(otherEventId);
                        }
                    }
                }
                if (eventLocationLinksMap.containsKey(eventId)) {
                    ArrayList<LocationLink> lLinks = eventLocationLinksMap.get(eventId);
                    for (int j = 0; j < lLinks.size(); j++) {
                        LocationLink locationLink = lLinks.get(j);
                        String otherEventId = "";
                        if (!locationLink.getSource().equals(eventId)) {
                            otherEventId = locationLink.getSource();
                        } else {
                            otherEventId = locationLink.getTarget();
                        }
                        if (!relatedMentions.contains(otherEventId)) {
                            relatedMentions.add(otherEventId);
                        }
                    }
                }
            }
            if (relatedMentions.size() > 0) {
                ArrayList<KafTerm> eventTerms2 = this.eventCorefMap.get(corefId2);
                for (int i = 0; i < eventTerms2.size(); i++) {
                    KafTerm term = eventTerms2.get(i);
                    if (relatedMentions.contains(term.getTid())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }



/*    public void makeInstanceGroups () {
        *//*
            We determine the climax from the Plot links
            - if relType="FALLING_ACTION" then source is climax event
            - if relType="PRECONDITION" then target is climax
         *//*
        ArrayList<String> coveredEvents = new ArrayList<String>();

        Set keySet = eventCorefMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String corefId = keys.next();
            ArrayList<String> group = new ArrayList<String>();
            makeInstanceGroup(coveredEvents,group, corefId);
        }

        keySet = eventToStoryMap.keySet();
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String eventId = keys.next();
            String storyId = eventToStoryMap.get(eventId);
            if (storyMap.containsKey(storyId)) {
                ArrayList<String> eventIds = storyMap.get(storyId);
                if (!eventIds.contains(eventId)) {
                    eventIds.add(eventId);
                    storyMap.put(storyId, eventIds);
                }
            }
            else {
                ArrayList<String> eventIds = new ArrayList<String>();
                eventIds.add(eventId);
                storyMap.put(storyId, eventIds);
            }
        }
    }

    public String makeInstanceGroup (ArrayList<String> coveredEvents, ArrayList<String> group, String corefId) {
        String storyId = corefId;
        if (this.eventCorefMap.containsKey(corefId)) {
            ArrayList<KafTerm> eventTerms = this.eventCorefMap.get(corefId);
            ArrayList<String> relatedMentions = new ArrayList<String>();
            for (int i = 0; i < eventTerms.size(); i++) {
                KafTerm eventTerm = eventTerms.get(i);
                String eventId = eventTerm.getTid();
                if (!group.contains(eventId) && !coveredEvents.contains(eventId)) {
                    coveredEvents.add(eventId);
                    group.add(eventId);
                    System.out.println("extending group = " + group);
                    if (eventPlotLinksMap.containsKey(eventId)) {
                        ArrayList<PlotLink> plotLinks = eventPlotLinksMap.get(eventId);
                        for (int j = 0; j < plotLinks.size(); j++) {
                            PlotLink plotLink = plotLinks.get(j);
                            String otherEventId = "";
                            if (!plotLink.getSource().equals(eventId)) {
                                otherEventId = plotLink.getSource();
                            } else {
                                otherEventId = plotLink.getTarget();
                            }
                            if (!relatedMentions.contains(otherEventId)) {
                                relatedMentions.add(otherEventId);
                            }
                        }
                    }
                    if (eventTimeLinksMap.containsKey(eventId)) {
                        ArrayList<TimeLink> tlinks = eventTimeLinksMap.get(eventId);
                        for (int j = 0; j < tlinks.size(); j++) {
                            TimeLink timeLink = tlinks.get(j);
                            String otherEventId = "";
                            if (!timeLink.getSource().equals(eventId)) {
                                otherEventId = timeLink.getSource();
                            } else {
                                otherEventId = timeLink.getTarget();
                            }
                            if (!relatedMentions.contains(otherEventId)) {
                                relatedMentions.add(otherEventId);
                            }
                        }
                    }
                    if (eventLocationLinksMap.containsKey(eventId)) {
                        ArrayList<LocationLink> lLinks = eventLocationLinksMap.get(eventId);
                        for (int j = 0; j < lLinks.size(); j++) {
                            LocationLink locationLink = lLinks.get(j);
                            String otherEventId = "";
                            if (!locationLink.getSource().equals(eventId)) {
                                otherEventId = locationLink.getSource();
                            } else {
                                otherEventId = locationLink.getTarget();
                            }
                            if (!relatedMentions.contains(otherEventId)) {
                                relatedMentions.add(otherEventId);
                            }
                        }
                    }
                }
                else {
                    System.out.println("Covered eventId = " + eventId);
                }
            }
            System.out.println("BEFORE RECURSING");
            System.out.println("storyId = " + storyId);
            System.out.println("group = " + group);
            if (relatedMentions.size()==0) {
                //// no relations, group is just the coref terms
                System.out.println("NO RELATED MENTIONS");
            }
            else {
                System.out.println("relatedMentions = " + relatedMentions);
                boolean TAKEN = false;
                for (int i = 0; i < relatedMentions.size(); i++) {
                    String eventId = relatedMentions.get(i);
                    if (eventToStoryMap.containsKey(eventId)) {
                        //// related event is already assigned
                        storyId = eventToStoryMap.get(eventId);
                        System.out.println("already exists storyId = " + storyId);
                        TAKEN = true;
                        break; /// this means we take the first
                    }
                }
                if (!TAKEN) {
                    /// related events were not considered, we recurse to extend the group
                    for (int i = 0; i < relatedMentions.size(); i++) {
                        String eventId = relatedMentions.get(i);
                        System.out.println("eventId = " + eventId);
*//*
                        coveredEvents.add(eventId);
                        group.add(eventId);
*//*
                        if (termToCorefMap.containsKey(eventId)) {
                            String relatedCorefId = termToCorefMap.get(eventId);
                            System.out.println("relatedCorefId = " + relatedCorefId);
                            storyId = makeInstanceGroup(coveredEvents, group, relatedCorefId);
                            System.out.println("storyId = " + storyId);
                            //// this means we take the last instance
                        }
                        else {
                            System.out.println("could not map eventId to coref Map= " + eventId);
                        }
                    }
                    System.out.println("AFTER RECURSING");
                    System.out.println("group = " + group);
                    System.out.println("storyId = " + storyId);
                }
                else {
                    System.out.println("ALL NEW");
                }
            }
            System.out.println("Adding complete group = " + group);
            System.out.println("To the storyId = " + storyId);
            for (int i = 0; i < group.size(); i++) {
                String eventId = group.get(i);
                eventToStoryMap.put(eventId, storyId);
            }
        }
        else {
            System.out.println("Cannot find the corefId = " + corefId);
        }
        return storyId;
    }



    public void makeGroups () {
        *//*
            We determine the climax from the Plot links
            - if relType="FALLING_ACTION" then source is climax event
            - if relType="PRECONDITION" then target is climax
         *//*
        ArrayList<String> group = new ArrayList<String>();
        ArrayList<String> coveredEvents = new ArrayList<String>();
        Set keySet = eventPlotLinksMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!coveredEvents.contains(key)) {
                group = new ArrayList<String>();
                group.add(key);
                coveredEvents.add(key);
                makeGroup(coveredEvents, group, key);
                //System.out.println("group.size() = " + group.size());
               // System.out.println("coveredEvents = " + coveredEvents.size());
                storyMap.put(key, group);
            }
        }


        *//*
            Now we have story entries based on the climax event, we see if we can attach more through the tlinks
         *//*
*//*        for (int i = 0; i < timeLinks.size(); i++) {
            TimeLink timeLink = timeLinks.get(i);
            if (!timeLink.getRelType().equalsIgnoreCase("includes")) {
                //// any other relation than "includes"; check if target or source is a climaxEvent
                //// if so add the other identifier
                String climaxEvent = timeLink.getTarget();
                if (storyMap.containsKey(climaxEvent)) {
                    ArrayList<String> storyEvents = storyMap.get(climaxEvent);
                    if (!storyEvents.contains(timeLink.getSource())) {
                        storyEvents.add(timeLink.getSource());
                        storyMap.put(climaxEvent, storyEvents);
                    }
                }
                else {
                    climaxEvent = timeLink.getSource();
                    if (storyMap.containsKey(climaxEvent)) {
                        ArrayList<String> storyEvents = storyMap.get(climaxEvent);
                        if (!storyEvents.contains(timeLink.getTarget())) {
                            storyEvents.add(timeLink.getSource());
                            storyMap.put(climaxEvent, storyEvents);
                        }
                    }
                }
            }
        }


            Finally if an event occurs at the same location as the climax event we add it as well

         HashMap<String, ArrayList<String>> locationMap = new HashMap<String, ArrayList<String>>();
         for (int i = 0; i < locationLinks.size(); i++) {
            LocationLink locationLink = locationLinks.get(i);
             String event = locationLink.getSource();
             String location = locationLink.getTarget();
             if (locationMap.containsKey(location)) {
                 ArrayList<String> storyEvents = locationMap.get(location);
                 if (!storyEvents.contains(event)) {
                     storyEvents.add(event);
                     locationMap.put(location, storyEvents);
                 }
             }
             else {
                 ArrayList<String> storyEvents = new ArrayList<String>();
                 storyEvents.add(event);
                 locationMap.put(location, storyEvents);
             }
         }
        keySet = locationMap.keySet();
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            ArrayList<String> events = locationMap.get(key);
            for (int i = 0; i < events.size(); i++) {
                String eventId = events.get(i);
                if (storyMap.containsKey(eventId)) {
                    ArrayList<String> storyEvents = storyMap.get(eventId);
                    for (int j = 0; j < events.size(); j++) {
                        String coLocatedEventId = events.get(j);
                        if (!coLocatedEventId.equals(eventId) && !storyEvents.contains(coLocatedEventId)) {
                             storyEvents.add(coLocatedEventId);
                            storyMap.put(eventId, storyEvents);
                        }
                    }
                }
            }
        }*//*
        keySet = storyMap.keySet();
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String storyKey = keys.next();
            ArrayList<String> eventIds = storyMap.get(storyKey);
            for (int i = 0; i < eventIds.size(); i++) {
                String eventId = eventIds.get(i);
              //  System.out.println("eventId = " + eventId+":"+storyKey);
                eventToStoryMap.put(eventId, storyKey);
            }
        }
    }


    public void makeGroup (ArrayList<String> coveredEvents, ArrayList<String> group, String eventId) {
        if (eventPlotLinksMap.containsKey(eventId)) {
            ArrayList<PlotLink> plotLinks = eventPlotLinksMap.get(eventId);
            for (int j = 0; j < plotLinks.size(); j++) {
                PlotLink plotLink = plotLinks.get(j);
                String otherEventId = "";
                if (!plotLink.getSource().equals(eventId)) {
                    otherEventId = plotLink.getSource();
                }
                else {
                    otherEventId = plotLink.getTarget();
                }
                if (!group.contains(otherEventId)) {
                    group.add(otherEventId);
                    coveredEvents.add(otherEventId);
                    makeGroup(coveredEvents, group,otherEventId);
                }
            }
        }
        if (eventTimeLinksMap.containsKey(eventId)) {
            ArrayList<TimeLink> tlinks = eventTimeLinksMap.get(eventId);
            for (int i = 0; i < tlinks.size(); i++) {
                TimeLink timeLink = tlinks.get(i);
                String otherEventId = "";
                if (!timeLink.getSource().equals(eventId)) {
                    otherEventId = timeLink.getSource();
                }
                else {
                    otherEventId = timeLink.getTarget();
                }
                if (!group.contains(otherEventId)) {
                    group.add(otherEventId);
                    coveredEvents.add(otherEventId);
                    makeGroup(coveredEvents, group,otherEventId);
                }
            }
        }
        if (eventLocationLinksMap.containsKey(eventId)) {
            ArrayList<LocationLink> lLinks = eventLocationLinksMap.get(eventId);
            for (int i = 0; i < lLinks.size(); i++) {
                LocationLink locationLink = lLinks.get(i);
                String otherEventId = "";
                if (!locationLink.getSource().equals(eventId)) {
                    otherEventId = locationLink.getSource();
                }
                else {
                    otherEventId = locationLink.getTarget();
                }
                if (!group.contains(otherEventId)) {
                    group.add(otherEventId);
                    coveredEvents.add(otherEventId);
                    makeGroup(coveredEvents, group,otherEventId);
                }
            }
        }
    }



    public void makeGroupsOrg () {
        *//*
            We determine the climax from the Plot links
            - if relType="FALLING_ACTION" then source is climax event
            - if relType="PRECONDITION" then target is climax
         *//*

        for (int i = 0; i < plotLinks.size(); i++) {
            PlotLink plotLink = plotLinks.get(i);
            if (plotLink.getRelType().equalsIgnoreCase("falling_action")) {
                String climaxEvent = plotLink.getSource();
                if (storyMap.containsKey(climaxEvent)) {
                    ArrayList<String> storyEvents = storyMap.get(climaxEvent);
                    if (!storyEvents.contains(plotLink.getTarget())) {
                        storyEvents.add(plotLink.getTarget());
                        storyMap.put(climaxEvent, storyEvents);
                    }
                }
                else {
                    ArrayList<String> storyEvents = new ArrayList<String>();
                    storyEvents.add(plotLink.getTarget());
                    storyMap.put(climaxEvent, storyEvents);
                }
            }
            else if (plotLink.getRelType().equalsIgnoreCase("precondition")) {
                /// source/target reversed
                String climaxEvent = plotLink.getTarget();
                if (storyMap.containsKey(climaxEvent)) {
                    ArrayList<String> storyEvents = storyMap.get(climaxEvent);
                    if (!storyEvents.contains(plotLink.getSource())) {
                        storyEvents.add(plotLink.getSource());
                        storyMap.put(climaxEvent, storyEvents);
                    }
                }
                else {
                    ArrayList<String> storyEvents = new ArrayList<String>();
                    storyEvents.add(plotLink.getSource());
                    storyMap.put(climaxEvent, storyEvents);
                }
            }
        }

        *//*
            Now we have story entries based on the climax event, we see if we can attach more through the tlinks
         *//*
        for (int i = 0; i < timeLinks.size(); i++) {
            TimeLink timeLink = timeLinks.get(i);
            if (!timeLink.getRelType().equalsIgnoreCase("includes")) {
                //// any other relation than "includes"; check if target or source is a climaxEvent
                //// if so add the other identifier
                String climaxEvent = timeLink.getTarget();
                if (storyMap.containsKey(climaxEvent)) {
                    ArrayList<String> storyEvents = storyMap.get(climaxEvent);
                    if (!storyEvents.contains(timeLink.getSource())) {
                        storyEvents.add(timeLink.getSource());
                        storyMap.put(climaxEvent, storyEvents);
                    }
                }
                else {
                    climaxEvent = timeLink.getSource();
                    if (storyMap.containsKey(climaxEvent)) {
                        ArrayList<String> storyEvents = storyMap.get(climaxEvent);
                        if (!storyEvents.contains(timeLink.getTarget())) {
                            storyEvents.add(timeLink.getSource());
                            storyMap.put(climaxEvent, storyEvents);
                        }
                    }
                }
            }
        }

        *//*
            Finally if an event occurs at the same location as the climax event we add it as well
         *//*
         HashMap<String, ArrayList<String>> locationMap = new HashMap<String, ArrayList<String>>();
         for (int i = 0; i < locationLinks.size(); i++) {
            LocationLink locationLink = locationLinks.get(i);
             String event = locationLink.getSource();
             String location = locationLink.getTarget();
             if (locationMap.containsKey(location)) {
                 ArrayList<String> storyEvents = locationMap.get(location);
                 if (!storyEvents.contains(event)) {
                     storyEvents.add(event);
                     locationMap.put(location, storyEvents);
                 }
             }
             else {
                 ArrayList<String> storyEvents = new ArrayList<String>();
                 storyEvents.add(event);
                 locationMap.put(location, storyEvents);
             }
         }
        Set keySet = locationMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            ArrayList<String> events = locationMap.get(key);
            for (int i = 0; i < events.size(); i++) {
                String eventId = events.get(i);
                if (storyMap.containsKey(eventId)) {
                    ArrayList<String> storyEvents = storyMap.get(eventId);
                    for (int j = 0; j < events.size(); j++) {
                        String coLocatedEventId = events.get(j);
                        if (!coLocatedEventId.equals(eventId) && !storyEvents.contains(coLocatedEventId)) {
                             storyEvents.add(coLocatedEventId);
                            storyMap.put(eventId, storyEvents);
                        }
                    }
                }
            }
        }
        keySet = storyMap.keySet();
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String storyKey = keys.next();
            ArrayList<String> eventIds = storyMap.get(storyKey);
            for (int i = 0; i < eventIds.size(); i++) {
                String eventId = eventIds.get(i);
                System.out.println("eventId = " + eventId+":"+storyKey);
                eventToStoryMap.put(eventId, storyKey);
            }
        }
    }*/


    void initAll() {
        REFERSTO = false;
        value = "";
        target = "";
        source = "";
        span = "";
        fileName = "";
        TIME = false;
        singletonId = "";
        crossDocId = "";
        kafWordForm = new KafWordForm();
        kafTerm = new KafTerm();
        timeTerm = new TimeTerm();
        spans = new ArrayList<String>();
        termToCorefMap = new HashMap<String, String>();
        eventToStoryMap = new HashMap<String, String>();
        instanceToStoryMap = new HashMap<String, String>();
        eventCorefMap = new HashMap<String, ArrayList<KafTerm>>();
        storyMap = new HashMap<String, ArrayList<String>>();
        kafWordFormArrayList = new ArrayList<KafWordForm>();
        actorTermArrayList = new ArrayList<KafTerm>();
        eventTermArrayList = new ArrayList<KafTerm>();
        locationTermArrayList = new ArrayList<KafTerm>();
        timeTermArrayList = new ArrayList<TimeTerm>();
        timeLinks = new ArrayList<TimeLink>();
        locationLinks = new ArrayList<LocationLink>();
        actorLinks = new ArrayList<ActorLink>();
        plotLinks = new ArrayList<PlotLink>();
        eventActorLinksMap = new HashMap<String, ArrayList<ActorLink>>();
        eventTimeLinksMap = new HashMap<String, ArrayList<TimeLink>>();
        eventLocationLinksMap = new HashMap<String, ArrayList<LocationLink>>();
        eventPlotLinksMap = new HashMap<String, ArrayList<PlotLink>>();
        eventMentionMap = new HashMap<String, JSONObject>();
        actorMap = new HashMap<String, KafTerm>();
        locationMap = new HashMap<String, KafTerm>();
        timeMap = new HashMap<String, TimeTerm>();
    }

    void initPart() {
        REFERSTO = false;
        value = "";
        target = "";
        source = "";
        span = "";
        fileName = "";
        TIME = false;
        singletonId = "";
        crossDocId = "";
        kafWordForm = new KafWordForm();
        kafTerm = new KafTerm();
        timeTerm = new TimeTerm();
        spans = new ArrayList<String>();
        timeLinks = new ArrayList<TimeLink>();
        locationLinks = new ArrayList<LocationLink>();
        actorLinks = new ArrayList<ActorLink>();
        plotLinks = new ArrayList<PlotLink>();
        eventTermArrayList = new ArrayList<KafTerm>();
        actorTermArrayList = new ArrayList<KafTerm>();
        locationTermArrayList = new ArrayList<KafTerm>();
        timeTermArrayList = new ArrayList<TimeTerm>();
        kafWordFormArrayList = new ArrayList<KafWordForm>();
    }

    public void buildMentionMap() throws JSONException {
        for (int i = 0; i < eventTermArrayList.size(); i++) {
            KafTerm eventTerm = eventTermArrayList.get(i);
            ///make snippet and mention
            //System.out.println("eventTerm.getTid() = " + eventTerm.getTid());
            //System.out.println("getPhrase(eventTerm) = " + eventTerm.getTokenString());
            String sentenceId = getSentenceId(eventTerm);
            //System.out.println("sentenceId = " + sentenceId);
            String sentence = getSentence(sentenceId);
            //System.out.println("sentence = " + sentence);
            JSONObject mentionObject = new JSONObject();
            mentionObject.append("snippet", sentence);
            Integer offsetBegin = sentence.indexOf(eventTerm.getTokenString());
            Integer offsetEnd = offsetBegin + eventTerm.getTokenString().length();
            // mentionObject.put("uri", fileName);
            // mentionObject.append("char", offsetBegin);
            // mentionObject.append("char", offsetBegin);
            mentionObject.append("snippet_char", offsetBegin);
            mentionObject.append("snippet_char", offsetEnd);
            //eventObject.append("mentions", mentionObject);

            PerspectiveJsonObject perspectiveJsonObject = new PerspectiveJsonObject(new ArrayList<String>(), "", "", "", "", "", fileName, null);
            perspectiveJsonObject.setDefaultPerspectiveValue();
            JSONObject attribution = perspectiveJsonObject.getJSONObject();
            JSONObject perspective = new JSONObject();
            perspective.put("attribution", attribution);
            perspective.put("source", "author:" + fileName);
            mentionObject.append("perspective", perspective);
            eventMentionMap.put(eventTerm.getTid(), mentionObject);
        }
    }

    public void buildTermMaps() {
        for (int i = 0; i < actorTermArrayList.size(); i++) {
            KafTerm term = actorTermArrayList.get(i);
            actorMap.put(term.getTid(), term);
        }
        actorTermArrayList = new ArrayList<KafTerm>();
        for (int i = 0; i < locationTermArrayList.size(); i++) {
            KafTerm term = locationTermArrayList.get(i);
            locationMap.put(term.getTid(), term);
        }
        locationTermArrayList = new ArrayList<KafTerm>();
        for (int i = 0; i < timeTermArrayList.size(); i++) {
            TimeTerm term = timeTermArrayList.get(i);
            timeMap.put(term.getTid(), term);
        }
        timeTermArrayList = new ArrayList<TimeTerm>();
    }

    public void buildLinkMaps() {
        for (int i = 0; i < actorLinks.size(); i++) {
            ActorLink actorLink = actorLinks.get(i);
            String eventId = actorLink.getSource();
            if (eventActorLinksMap.containsKey(eventId)) {
                ArrayList<ActorLink> aLinks = eventActorLinksMap.get(eventId);
                aLinks.add(actorLink);
                eventActorLinksMap.put(eventId, aLinks);
            } else {
                ArrayList<ActorLink> aLinks = new ArrayList<ActorLink>();
                aLinks.add(actorLink);
                eventActorLinksMap.put(eventId, aLinks);
            }
            //System.out.println("eventActorLinksMap = " + eventActorLinksMap.size());
        }
        for (int i = 0; i < locationLinks.size(); i++) {
            LocationLink locationLink = locationLinks.get(i);
            String eventId = locationLink.getSource();
            if (eventLocationLinksMap.containsKey(eventId)) {
                ArrayList<LocationLink> locLinks = eventLocationLinksMap.get(eventId);
                locLinks.add(locationLink);
                eventLocationLinksMap.put(eventId, locLinks);
            } else {
                ArrayList<LocationLink> locLinks = new ArrayList<LocationLink>();
                locLinks.add(locationLink);
                eventLocationLinksMap.put(eventId, locLinks);
            }
        }
        for (int i = 0; i < timeLinks.size(); i++) {
            TimeLink timeLink = timeLinks.get(i);
            String eventId = timeLink.getSource();
            if (eventTimeLinksMap.containsKey(eventId)) {
                ArrayList<TimeLink> tLinks = eventTimeLinksMap.get(eventId);
                tLinks.add(timeLink);
                eventTimeLinksMap.put(eventId, tLinks);
            } else {
                ArrayList<TimeLink> tLinks = new ArrayList<TimeLink>();
                tLinks.add(timeLink);
                eventTimeLinksMap.put(eventId, tLinks);
            }
        }

        for (int i = 0; i < plotLinks.size(); i++) {
            PlotLink plotLink = plotLinks.get(i);
            String eventId = plotLink.getSource();
            if (eventPlotLinksMap.containsKey(eventId)) {
                ArrayList<PlotLink> tLinks = eventPlotLinksMap.get(eventId);
                tLinks.add(plotLink);
                eventPlotLinksMap.put(eventId, tLinks);
            } else {
                ArrayList<PlotLink> tLinks = new ArrayList<PlotLink>();
                tLinks.add(plotLink);
                eventPlotLinksMap.put(eventId, tLinks);
            }
            eventId = plotLink.getTarget();
        }
    }


    public void getPhrases() {
        for (int i = 0; i < eventTermArrayList.size(); i++) {
            KafTerm term = eventTermArrayList.get(i);
            setPhrase(term);
        }
        for (int i = 0; i < actorTermArrayList.size(); i++) {
            KafTerm term = actorTermArrayList.get(i);
            setPhrase(term);
        }
        for (int i = 0; i < locationTermArrayList.size(); i++) {
            KafTerm term = locationTermArrayList.get(i);
            setPhrase(term);
        }
    }

    public void parseFile(String filePath) {
        initPart();
        fileName = new File(filePath).getName();
        initSingletonId(filePath);
        String myerror = "";
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            SAXParser parser = factory.newSAXParser();
            InputSource inp = new InputSource(new FileReader(filePath));
            parser.parse(inp, this);
/*
            System.out.println("eventTermArrayList.size(); = " + eventTermArrayList.size());
            System.out.println("timeLinks = " + timeLinks.size());
            System.out.println("locationLinks = " + locationLinks.size());
            System.out.println("plotLinks = " + plotLinks.size());
*/

        } catch (SAXParseException err) {
            myerror = "\n** Parsing error" + ", line " + err.getLineNumber()
                    + ", uri " + err.getSystemId();
            myerror += "\n" + err.getMessage();
            System.out.println("myerror = " + myerror);
        } catch (SAXException e) {
            Exception x = e;
            if (e.getException() != null)
                x = e.getException();
            myerror += "\nSAXException --" + x.getMessage();
            System.out.println("myerror = " + myerror);
        } catch (Exception eee) {
            eee.printStackTrace();
            myerror += "\nException --" + eee.getMessage();
            System.out.println("myerror = " + myerror);
        }
        //System.out.println("myerror = " + myerror);
    }//--c

    public void initSingletonId(String filePath) {
        singletonId = getIntFromFileName(filePath);
    }

    public String getIntFromFileName(String filePath) {
        String intString = "";
        String dig = "1234567890";
        File file = new File(filePath);
        String name = file.getName();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (dig.indexOf(c) > -1) {
                intString += c;
            }
        }
        if (intString.length() < 4) {
            for (int i = intString.length(); i < 4; i++) {
                intString += "0";
            }
        }
        if (name.indexOf("plus") > -1) {
            intString += "99";
        } else {
            intString += "88";
        }
        //  System.out.println("intString = " + intString);
        return intString;
    }

    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {

        if (qName.equalsIgnoreCase("token")) {
            kafWordForm = new KafWordForm();
            kafWordForm.setWid(fileName + "_" + attributes.getValue("t_id"));
            Integer sentenceInt = Integer.parseInt(attributes.getValue("sentence"));
            /// HACK TO MAKE SENTENCE ID EQUAL TO SENTENCE ID OF WIKINEWS NAF GENERATED BY FBK
            sentenceInt++;
            kafWordForm.setSent(sentenceInt.toString());
        } else if (qName.equalsIgnoreCase("CROSS_DOC_COREF") ||
                qName.equalsIgnoreCase("INTRA_DOC_COREF")) {
            REFERSTO = true;
            if (qName.equalsIgnoreCase("CROSS_DOC_COREF")) {
                crossDocId = fixCrossDocId(attributes.getValue("note"));
            } else if (qName.equalsIgnoreCase("INTRA_DOC_COREF")) {
                crossDocId = singletonId + fixCrossDocId(attributes.getValue("r_id"));
            }
        } else if (qName.equalsIgnoreCase("ACTION_OCCURRENCE")
                || qName.equalsIgnoreCase("ACTION_CAUSATIVE")
                || qName.equalsIgnoreCase("ACTION_REPORTING")
                || qName.equalsIgnoreCase("ACTION_STATE")
                || qName.equalsIgnoreCase("HUMAN_PART_ORG")
                || qName.equalsIgnoreCase("HUMAN_PART_PER")
                || qName.equalsIgnoreCase("LOC_GEO")
                || qName.equalsIgnoreCase("LOC_FAC")
                ) {
            kafTerm = new KafTerm();
            kafTerm.setType(qName);
            kafTerm.setTid(fileName + "_" + attributes.getValue("m_id"));
            TIME = false;
        } else if (qName.equalsIgnoreCase("TIME_DATE")) {
            timeTerm = new TimeTerm();
            timeTerm.setType(qName);
            timeTerm.setTid(fileName + "_" + attributes.getValue("m_id"));
            timeTerm.setDct(attributes.getValue("DCT").equalsIgnoreCase("true"));
            timeTerm.setValue(attributes.getValue("value"));
            timeTerm.setAnchorTimeId(attributes.getValue("anchorTimeID"));
            TIME = true;
        } else if (qName.equalsIgnoreCase("token_anchor")) {
            span = fileName + "_" + attributes.getValue("t_id");
            if (!TIME) {
                kafTerm.addSpans(span);
            } else {
                timeTerm.addSpans(span);
            }
        } else if (qName.equalsIgnoreCase("source")) {
            source = fileName + "_" + attributes.getValue("m_id");
            //// sources are mentions that match with the same target. The same target can occur in different refers to mappings
            if (REFERSTO) {
                /// sources and targets can also occur for other relations than refersto
                String termId = fileName + "_" + attributes.getValue("m_id");
                if (!crossDocId.isEmpty()) {
                    termToCorefMap.put(termId, crossDocId);
                }
            }
        } else if (qName.equalsIgnoreCase("target")) {
            target = fileName + "_" + attributes.getValue("m_id");
        } else if (qName.equalsIgnoreCase("tlink")) {
            relType = attributes.getValue("relType");
        } else if (qName.equalsIgnoreCase("plot_link")) {
            relType = attributes.getValue("relType");
            causes = attributes.getValue("causes");
            causedBy = attributes.getValue("causedBy");
            signal = attributes.getValue("signal");
            contextualModality = attributes.getValue("contextualModality");
        }
        value = "";
    }//--startElement

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equalsIgnoreCase("token")) {
            kafWordForm.setWf(value.trim());
            kafWordFormArrayList.add(kafWordForm);
            kafWordForm = new KafWordForm();
        } else if (qName.equalsIgnoreCase("ACTION_OCCURRENCE") || qName.equalsIgnoreCase("ACTION_STATE")) {
            eventTermArrayList.add(kafTerm);
            kafTerm = new KafTerm();
        } else if (qName.equalsIgnoreCase("HUMAN_PART_ORG") || qName.equalsIgnoreCase("HUMAN_PART_PER")) {
            actorTermArrayList.add(kafTerm);
            kafTerm = new KafTerm();
        } else if (qName.equalsIgnoreCase("LOC_GEO") || qName.equalsIgnoreCase("LOC_FAC")) {
            locationTermArrayList.add(kafTerm);
            kafTerm = new KafTerm();
        } else if (qName.equalsIgnoreCase("ACTION_CAUSATIVE") || qName.equalsIgnoreCase("ACTION_REPORTING")) {
            locationTermArrayList.add(kafTerm);
            kafTerm = new KafTerm();
        } else if (qName.equalsIgnoreCase("TIME_DATE")) {
            timeTermArrayList.add(timeTerm);
            timeTerm = new TimeTerm();
            TIME = false;
        } else if (qName.equalsIgnoreCase("LOCATION_LINK")) {
            LocationLink locationLink = new LocationLink();
            locationLink.setSource(source);
            locationLink.setTarget(target);
            locationLinks.add(locationLink);
            source = "";
            target = "";
        } else if (qName.equalsIgnoreCase("TLINK")) {
            TimeLink timeLink = new TimeLink();
            //// we need to switch between source and targets for includes links due to way it is annotated
            if (relType != null && relType.equalsIgnoreCase("includes")) {
                timeLink.setSource(target);
                timeLink.setTarget(source);
            } else {
                timeLink.setSource(source);
                timeLink.setTarget(target);
            }
            timeLink.setRelType(relType);
            // System.out.println("timeLink.getTarget() = " + timeLink.getTarget());
            // System.out.println("relType = " + relType);
            relType = "";
            source = "";
            target = "";
            timeLinks.add(timeLink);
        } else if (qName.equalsIgnoreCase("PLOT_LINK")) {
            PlotLink plotLink = new PlotLink();
            plotLink.setSource(source);
            plotLink.setTarget(target);
            plotLink.setRelType(relType);
            plotLink.setCaused_by(causedBy);
            plotLink.setCauses(causes);
            plotLink.setSignal(signal);
            relType = "";
            source = "";
            target = "";
            plotLinks.add(plotLink);
        } else if (qName.equalsIgnoreCase("CROSS_DOC_COREF") ||
                qName.equalsIgnoreCase("INTRA_DOC_COREF")) {
            REFERSTO = false;
            source = "";
            target = "";
        }
    }

    public void characters(char ch[], int start, int length)
            throws SAXException {
        value += new String(ch, start, length);
        // System.out.println("tagValue:"+value);
    }


    String fixCrossDocId(String id) {
        final String charString = "abcdefghijklmnopqrstuvwxyz";
        String str = "";
        for (int i = 0; i < id.length(); i++) {
            char c = id.toLowerCase().charAt(i);
            int idx = charString.indexOf(c);
            if (idx > -1) {
                str += idx + 1;
            } else {
                str += c;
            }
        }
        //System.out.println("str = " + str);
        return str;
    }

    public void buildEventCorefMap() {
        for (int i = 0; i < eventTermArrayList.size(); i++) {
            KafTerm eventTerm = eventTermArrayList.get(i);
            String corefId = eventTerm.getTid(); /// this is for singletons
            if (termToCorefMap.containsKey(eventTerm.getTid())) {
                corefId = termToCorefMap.get(eventTerm.getTid());
            } else {
                termToCorefMap.put(corefId, corefId); //// singleton
            }
            if (eventCorefMap.containsKey(corefId)) {
                ArrayList<KafTerm> events = eventCorefMap.get(corefId);
                events.add(eventTerm);
                eventCorefMap.put(corefId, events);
            } else {
                ArrayList<KafTerm> events = new ArrayList<KafTerm>();
                events.add(eventTerm);
                eventCorefMap.put(corefId, events);
            }
        }
    }

    public void getActorLinks() {
        for (int i = 0; i < eventTermArrayList.size(); i++) {
            KafTerm eventTerm = eventTermArrayList.get(i);
            String sentence1 = getSentenceId(eventTerm);
            for (int j = 0; j < actorTermArrayList.size(); j++) {
                KafTerm actorTerm = actorTermArrayList.get(j);
                String sentence2 = getSentenceId(actorTerm);
                if (sentence1.equals(sentence2)) {
                    ActorLink actorLink = new ActorLink();
                    actorLink.setSource(eventTerm.getTid());
                    actorLink.setTarget(actorTerm.getTid());
                    actorLinks.add(actorLink);
                }
            }
        }
    }

    public String getSentenceId(KafTerm kafTerm) {
        String sentence = "";
        for (int j = 0; j < kafTerm.getSpans().size(); j++) {
            String s = kafTerm.getSpans().get(j);
            for (int k = 0; k < kafWordFormArrayList.size(); k++) {
                KafWordForm wordForm = kafWordFormArrayList.get(k);
                if (wordForm.getWid().equals(s)) {
                    sentence = wordForm.getSent();
                    break;
                }
            }
        }
        return sentence;
    }

/*    public String getPhrase (KafTerm kafTerm) {
        String phrase = "";
        for (int j = 0; j < kafTerm.getSpans().size(); j++) {
            String s = kafTerm.getSpans().get(j);
            for (int k = 0; k < kafWordFormArrayList.size(); k++) {
                KafWordForm wordForm = kafWordFormArrayList.get(k);
                if (wordForm.getWid().equals(s)) {
                    phrase += " "+wordForm.getWf();
                }
            }
        }
        //System.out.println("phrase = " + phrase);
        phrase = phrase.trim();
        return phrase;
    }*/

    public void setPhrase(KafTerm kafTerm) {
        String phrase = "";
        for (int j = 0; j < kafTerm.getSpans().size(); j++) {
            String s = kafTerm.getSpans().get(j);
            for (int k = 0; k < kafWordFormArrayList.size(); k++) {
                KafWordForm wordForm = kafWordFormArrayList.get(k);
                if (wordForm.getWid().equals(s)) {
                    phrase += " " + wordForm.getWf();
                }
            }
        }
        kafTerm.setTokenString(phrase.trim());
    }

    public String getSentence(String sentenceId) {
        String sentence = "";
        for (int k = 0; k < kafWordFormArrayList.size(); k++) {
            KafWordForm wordForm = kafWordFormArrayList.get(k);
            if (wordForm.getSent().equals(sentenceId)) {
                sentence += " " + wordForm.getWf();
            }
        }
        return sentence.trim();
    }


    public ArrayList<JSONObject> getJsonObjectsFromCorefsets() throws JSONException {
        ArrayList<JSONObject> events = new ArrayList<JSONObject>();
        Set keySet = eventCorefMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String eventCorefKey = keys.next();
            // System.out.println("eventCorefKey = " + eventCorefKey);
            JSONObject eventObject = new JSONObject();
            JSONObject actors = new JSONObject();
            ArrayList<String> actorStrings = new ArrayList<String>();
            ArrayList<String> phraseStrings = new ArrayList<String>();
            ArrayList<String> timeStrings = new ArrayList<String>();
            ArrayList<KafTerm> eventTerms = eventCorefMap.get(eventCorefKey);
            Map<String, Integer> storyCount = new HashMap<String, Integer>();
            for (int i = 0; i < eventTerms.size(); i++) {
                KafTerm eventTerm = eventTerms.get(i);
                String eventPhrase = eventTerm.getTokenString().replaceAll(" ", "_");
                if (!eventPhrase.isEmpty()) {
                    if (eventMentionMap.containsKey(eventTerm.getTid())) {
                        JSONObject mention = eventMentionMap.get(eventTerm.getTid());
                        eventObject.append("mentions", mention);
                    }
                    if (!phraseStrings.contains(eventPhrase)) {
                        eventObject.append("labels", eventPhrase);
                        eventObject.append("prefLabel", eventPhrase);
                        phraseStrings.add(eventPhrase);
                    }

                    if (eventActorLinksMap.containsKey(eventTerm.getTid())) {
                        ArrayList<ActorLink> actorLinks = eventActorLinksMap.get(eventTerm.getTid());
                        for (int j = 0; j < actorLinks.size(); j++) {
                            ActorLink actorLink = actorLinks.get(j);
                            String targetId = actorLink.getTarget();
                            if (actorMap.containsKey(targetId)) {
                                KafTerm actor = actorMap.get(targetId);
                                String actorString = "act:" + actor.getTokenString().replaceAll(" ", "_");
                                if (!actorString.isEmpty()) {
                                    if (!actorStrings.contains(actorString)) {
                                        // System.out.println("actorString = " + actorString);
                                        actors.append("actor:", actorString);
                                        actorStrings.add(actorString);
                                    }
                                }
                            } else {
                                //  System.out.println("Could not find targetId = " + targetId);
                            }
                        }
                    }
                    if (eventLocationLinksMap.containsKey(eventTerm.getTid())) {
                        ArrayList<LocationLink> locationLinks = eventLocationLinksMap.get(eventTerm.getTid());
                        for (int j = 0; j < locationLinks.size(); j++) {
                            LocationLink locationLink = locationLinks.get(j);
                            String targetId = locationLink.getTarget();
                            if (locationMap.containsKey(targetId)) {
                                KafTerm location = locationMap.get(targetId);
                                String locationString = "loc:" + location.getTokenString().replaceAll(" ", "_");
                                if (!locationString.isEmpty()) {
                                    if (!actorStrings.contains(locationString)) {
                                        actors.append("actor:", locationString);
                                        actorStrings.add(locationString);
                                    }
                                }
                            }
                        }
                    }

                    if (eventTimeLinksMap.containsKey(eventTerm.getTid())) {
                        ArrayList<TimeLink> timeLinks = eventTimeLinksMap.get(eventTerm.getTid());
                        String timeString = "";
                        for (int j = 0; j < timeLinks.size(); j++) {
                            TimeLink timeLink = timeLinks.get(j);
                            if (timeLink.getRelType() != null && timeLink.getRelType().equalsIgnoreCase("INCLUDES")) {
                                String targetId = timeLink.getTarget();
                                if (timeMap.containsKey(targetId)) {
                                    TimeTerm time = timeMap.get(targetId);
                                    timeString = time.getValue().replaceAll("-", "");
                                    if (timeString.length() == 4) timeString += "0101";
                                    if (timeString.length() == 6) timeString += "01";
                                    if (!timeString.isEmpty() && !timeStrings.contains(timeString)) {
                                        timeStrings.add(timeString);
                                    }
                                }
                            }
                        }

                        if (timeStrings.isEmpty()) {
                            for (int j = 0; j < timeLinks.size(); j++) {
                                TimeLink timeLink = timeLinks.get(j);
                                String targetId = timeLink.getTarget();
                                if (timeMap.containsKey(targetId)) {
                                    TimeTerm time = timeMap.get(targetId);
                                    timeString = time.getValue().replaceAll("-", "");
                                    if (timeString.length() == 4) timeString += "0101";
                                    if (timeString.length() == 6) timeString += "01";
                                    if (!timeString.isEmpty() && !timeStrings.contains(timeString)) {
                                        timeStrings.add(timeString);
                                    }
                                }
                            }
                        }
                    }
                }
            }
/*
            System.out.println("phraseStrings = " + phraseStrings.toString());
            System.out.println("actorStrings = " + actorStrings.toString());
            System.out.println("timeStrings = " + timeStrings.toString());
*/
            if (actors.length() == 0) {
                actors.append("actor:", "noactors:");
            } else {
                eventObject.put("actors", actors);
                String timeString = "";
                Collections.sort(timeStrings);
                if (timeStrings.size() > 0) timeString = timeStrings.get(timeStrings.size() - 1);
                if (timeString.isEmpty()) {
                    //////
                    timeString = "20160101";
                    //System.out.println("No timeString, timeStrings = " + timeStrings.toString());
                } else {
                    String group = instanceToStoryMap.get(eventCorefKey);
                    int groupScore = getClimaxScore(group);
                    int score = getClimaxScore(eventCorefKey);
                    eventObject.put("time", timeString);
                    eventObject.put("event", eventCorefKey);
                    eventObject.put("group", groupScore + ":[" + group + "]");
                    eventObject.put("groupName", "[" + group + "]");
                    eventObject.put("groupScore", groupScore);
                    eventObject.put("climax", score);
                    events.add(eventObject);
                }
            }

        }

        JsonStoryUtil.minimalizeActors(events);

        return events;
    }


    /*

     */
    static void writeJsonObjectArrayWithStructuredData(String pathToFolder,
                                                       String project,
                                                       ArrayList<JSONObject> objects,
                                                       String structuredName,
                                                       ArrayList<JSONObject> structured) {
        try {
            try {
                JSONObject timeLineObject = JsonEvent.createTimeLineProperty(new File(pathToFolder).getName(), project);
                timeLineObject.append("event_cnt", objects.size());
                for (int j = 0; j < structured.size(); j++) {
                    JSONObject jsonObject = structured.get(j);
                    timeLineObject.append(structuredName, jsonObject);
                }

                for (int j = 0; j < objects.size(); j++) {
                    JSONObject jsonObject = objects.get(j);
                    timeLineObject.append("events", jsonObject);
                }

                File folder = new File(pathToFolder);
                String outputFile = folder.getAbsolutePath() + "/" + "contextual.timeline.json";
                OutputStream jsonOut = new FileOutputStream(outputFile);

                String str = "{ \"timeline\":\n";
                jsonOut.write(str.getBytes());
                jsonOut.write(timeLineObject.toString(0).getBytes());
                str = "}\n";
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

    static void writeJsonObjectArray(String pathToFolder,
                                     String project,
                                     ArrayList<JSONObject> objects) {
        try {
            try {
                JSONObject timeLineObject = JsonEvent.createTimeLineProperty(new File(pathToFolder).getName(), project);
                timeLineObject.append("event_cnt", objects.size());

                for (int j = 0; j < objects.size(); j++) {
                    JSONObject jsonObject = objects.get(j);
                    timeLineObject.append("events", jsonObject);
                }

                File folder = new File(pathToFolder);
                String outputFile = folder.getAbsolutePath() + "/" + "contextual.timeline.json";
                OutputStream jsonOut = new FileOutputStream(outputFile);

                String str = "{ \"timeline\":\n";
                jsonOut.write(str.getBytes());
                jsonOut.write(timeLineObject.toString(0).getBytes());
                str = "}\n";
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
}

