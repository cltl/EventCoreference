package eu.newsreader.eventcoreference.timeline;

import eu.newsreader.eventcoreference.objects.NafMention;

import java.util.*;

/**
 * Created by piek on 10/29/14.
 */
@Deprecated
public class EntityTimeLine {

    private String entityId;
    private ArrayList<NafMention> entityNafMentions;
    private HashMap<String, ArrayList<ArrayList<NafMention>>> timeEventNafMentions;

    public EntityTimeLine() {
        this.entityId = "";
        this.entityNafMentions = new ArrayList<NafMention>();
        this.timeEventNafMentions = new HashMap<String, ArrayList<ArrayList<NafMention>>>();
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public ArrayList<NafMention> getEntityNafMentions() {
        return entityNafMentions;
    }

    public void setEntityNafMentions(ArrayList<NafMention> entityNafMentions) {
        this.entityNafMentions = entityNafMentions;
    }

    public void addEntityNafMentions(ArrayList<NafMention> entityNafMentions) {
        for (int i = 0; i < entityNafMentions.size(); i++) {
            NafMention nafMention = entityNafMentions.get(i);
            addNafMention(this.entityNafMentions, nafMention);
        }
    }

    public void addNafMention(ArrayList<NafMention> nafMentionArrayList, NafMention nafMention) {
        boolean HASMENTION = false;
        for (int i = 0; i < nafMentionArrayList.size(); i++) {
            NafMention mention = nafMentionArrayList.get(i);
            if (nafMention.toString().equalsIgnoreCase(mention.toString())) {
                HASMENTION = true;
                break;
            }
        }
        if (!HASMENTION) {
            nafMentionArrayList.add(nafMention);
        }
    }

    public boolean hasMentions (ArrayList<ArrayList<NafMention>> mentions1, ArrayList<NafMention> mentions2) {
        for (int i = 0; i < mentions1.size(); i++) {
            ArrayList<NafMention> nafMentions = mentions1.get(i);
            int countMatches = 0;
            for (int j = 0; j < nafMentions.size(); j++) {
                NafMention nafMention = nafMentions.get(j);
                for (int k = 0; k < mentions2.size(); k++) {
                    NafMention mention = mentions2.get(k);
                    if (mention.toString().equals(nafMention.toString())) {
                        countMatches++;
                    }
                }
            }
            if (countMatches==mentions2.size()) {
                return true;
            }
        }
        return false;
    }

    public void addTimeEventNafMentions(String timeString, ArrayList<NafMention> eventNafMentions) {

        if (this.timeEventNafMentions.containsKey(timeString)) {
            ArrayList<ArrayList<NafMention>> events = this.timeEventNafMentions.get(timeString);
            if (!hasMentions(events, eventNafMentions)) {
                events.add(eventNafMentions);
                this.timeEventNafMentions.put(timeString, events);
            }
        }
        else {
            ArrayList<ArrayList<NafMention>> events = new ArrayList<ArrayList<NafMention>>();
            events.add(eventNafMentions);
            timeEventNafMentions.put(timeString, events);
        }
    }

    public void addTimeLine (EntityTimeLine entityTimeLine) {
        this.addEntityNafMentions(entityTimeLine.getEntityNafMentions());
        Set keySet = entityTimeLine.timeEventNafMentions.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            ArrayList<ArrayList<NafMention>> eventNafMentions = entityTimeLine.timeEventNafMentions.get(key);
            for (int i = 0; i < eventNafMentions.size(); i++) {
                ArrayList<NafMention> nafMentions = eventNafMentions.get(i);
                this.addTimeEventNafMentions(key, nafMentions);
            }
        }
    }

    public String toString () {
        String timeLine = "";
        timeLine += entityId+"\n";
        timeLine +="<dl>\n";
/*
        timeLine += "\t";
        for (int i = 0; i < entityNafMentions.size(); i++) {
            NafMention nafMention = entityNafMentions.get(i);
            timeLine += nafMention.getBaseUri()+"["+nafMention.getPhrase()+"]"+nafMention.getTermsIds().toString();
        }
        timeLine += "\n";
*/
        TreeSet tree = new TreeSet();
        Set keySet = timeEventNafMentions.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String timeString = keys.next();
            tree.add(timeString);
        }
        keys = tree.iterator();
        while (keys.hasNext()) {
            String timeString = keys.next();
            timeLine += "<dt><font color=\"blue\">" +timeString+"</font>";
            ArrayList<ArrayList<NafMention>> allEvents = timeEventNafMentions.get(timeString);
            for (int i = 0; i < allEvents.size(); i++) {
                //timeLine += "\t" +timeString;
                ArrayList<NafMention> nafMentions = allEvents.get(i);
                timeLine += "<dd>";
                for (int j = 0; j < nafMentions.size(); j++) {
                    NafMention nafMention = nafMentions.get(j);
                    //timeLine += "\t" +nafMention.getBaseUri()+ "[" +nafMention.getPhrase().trim()+"]"+nafMention.getTermsIds().toString();
                    timeLine += "\t" +"[" +nafMention.getPhrase().trim()+"]"+nafMention.getTermsIds().toString();
                    if (j==nafMentions.size()-1) {
                        timeLine+="\t"+nafMention.getBaseUri();
                    }
                }
                timeLine += "\n";
            }
        }
        timeLine+="</dl>\n";
        return timeLine;
    }
}
