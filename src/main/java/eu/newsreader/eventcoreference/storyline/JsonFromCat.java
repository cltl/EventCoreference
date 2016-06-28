package eu.newsreader.eventcoreference.storyline;

import eu.kyotoproject.kaf.KafTerm;
import eu.kyotoproject.kaf.KafWordForm;
import eu.newsreader.eventcoreference.objects.JsonEvent;
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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by piek on 27/06/16.
 */
public class JsonFromCat extends DefaultHandler {
    public class CatLink {
        private String source;
        private String target;
        private String linkId;
        private String relType;

        public CatLink() {
            this.linkId = "";
            this.source = "";
            this.relType = "";
            this.target = "";
        }

        public String getRelType() {
            return relType;
        }

        public void setRelType(String relType) {
            this.relType = relType;
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
            this.source = source;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
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

        public PlotLink () {
            caused_by="FALSE";
            causes="FALSE";
            signal="";
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
        private  String contextualModality;
        public TimeLink () {
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
        public TimeTerm () {
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
    KafWordForm kafWordForm;
    KafTerm kafTerm;
    TimeTerm timeTerm;
    boolean TIME;
    ArrayList<String> spans;
    static public ArrayList<KafWordForm> kafWordFormArrayList;
    static public ArrayList<KafTerm> eventTermArrayList;

    static public ArrayList<KafTerm> actorTermArrayList;
    static public ArrayList<KafTerm> locationTermArrayList;
    static public ArrayList<TimeTerm> timeTermArrayList;
    public ArrayList<TimeLink> timeLinks;
    public ArrayList<LocationLink> locationLinks;
    public ArrayList<PlotLink> plotLinks;
    public ArrayList<ActorLink> actorLinks;
    public HashMap<String, String> sourceEventHashMap;
    String fileName;

    public JsonFromCat () {
        init();
    }

    void init() {
        value = "";
        target = "";
        source = "";
        span = "";
        fileName ="";
        TIME = false;
        kafWordForm = new KafWordForm();
        kafTerm = new KafTerm();
        timeTerm = new TimeTerm();
        spans = new ArrayList<String>();
        kafWordFormArrayList = new ArrayList<KafWordForm>();
        actorTermArrayList = new ArrayList<KafTerm>();
        eventTermArrayList = new ArrayList<KafTerm>();
        locationTermArrayList = new ArrayList<KafTerm>();
        timeTermArrayList = new ArrayList<TimeTerm>();
        sourceEventHashMap = new HashMap<String, String>();
        timeLinks = new ArrayList<TimeLink>();
        locationLinks = new ArrayList<LocationLink>();
        actorLinks = new ArrayList<ActorLink>();
        plotLinks = new ArrayList<PlotLink>();
    }

    public void parseFile(String filePath) {
        fileName = new File(filePath).getName();
        String myerror = "";
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            SAXParser parser = factory.newSAXParser();
            InputSource inp = new InputSource (new FileReader(filePath));
            parser.parse(inp, this);
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



    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {

        if (qName.equalsIgnoreCase("token")) {
            kafWordForm = new KafWordForm();
            kafWordForm.setWid(attributes.getValue("t_id"));
            Integer sentenceInt = Integer.parseInt(attributes.getValue("sentence"));
            /// HACK TO MAKE SENTENCE ID EQUAL TO SENTENCE ID OF WIKINEWS NAF GENERATED BY FBK
            sentenceInt++;
            kafWordForm.setSent(sentenceInt.toString());
        }
        else if (qName.equalsIgnoreCase("ACTION_OCCURRENCE")
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
            kafTerm.setTid(fileName+"_"+attributes.getValue("m_id"));
            TIME = false;
        }
        else if (qName.equalsIgnoreCase("TIME_DATE")) {
            timeTerm = new TimeTerm();
            timeTerm.setType(qName);
            timeTerm.setTid(fileName+"_"+attributes.getValue("m_id"));
            timeTerm.setDct(attributes.getValue("DCT").equalsIgnoreCase("true"));
            timeTerm.setValue(attributes.getValue("value"));
            timeTerm.setAnchorTimeId(attributes.getValue("anchorTimeID"));
            TIME = true;
        }
        else if (qName.equalsIgnoreCase("token_anchor")) {
            span = attributes.getValue("t_id");
            if (!TIME) {
                kafTerm.addSpans(span);
            }
            else {
                timeTerm.addSpans(span);
            }
        }
        else if (qName.equalsIgnoreCase("source")) {
            source = fileName+"_"+attributes.getValue("m_id");
        }
        else if (qName.equalsIgnoreCase("target")) {
                target = fileName+"_"+attributes.getValue("m_id");
        }
        else if (qName.equalsIgnoreCase("tlink")) {
            relType =attributes.getValue("relType");
        }
        else if (qName.equalsIgnoreCase("plot_link")) {
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
        }
        else if (qName.equalsIgnoreCase("ACTION_OCCURRENCE") || qName.equalsIgnoreCase("ACTION_STATE") ) {
            eventTermArrayList.add(kafTerm);
            kafTerm = new KafTerm();
        }
        else if (qName.equalsIgnoreCase("HUMAN_PART_ORG") || qName.equalsIgnoreCase("HUMAN_PART_PER") ) {
            actorTermArrayList.add(kafTerm);
            kafTerm = new KafTerm();
        }
        else if (qName.equalsIgnoreCase("LOC_GEO") || qName.equalsIgnoreCase("LOC_FAC") ) {
            locationTermArrayList.add(kafTerm);
            kafTerm = new KafTerm();
        }
        else if (qName.equalsIgnoreCase("ACTION_CAUSATIVE") || qName.equalsIgnoreCase("ACTION_REPORTING") ) {
            locationTermArrayList.add(kafTerm);
            kafTerm = new KafTerm();
        }
        else if (qName.equalsIgnoreCase("TIME_DATE")) {
            timeTermArrayList.add(timeTerm);
            timeTerm = new TimeTerm();
            TIME = false;
        }
        else if (qName.equalsIgnoreCase("LOCATION_LINK")) {
            LocationLink locationLink = new LocationLink();
            locationLink.setSource(source);
            locationLink.setTarget(target);
            locationLinks.add(locationLink);
        }
        else if (qName.equalsIgnoreCase("TLINK")) {
            TimeLink timeLink = new TimeLink();
            if (relType!=null && relType.equalsIgnoreCase("includes")) {
                timeLink.setSource(target);
                timeLink.setTarget(source);
            }
            else {
                timeLink.setSource(source);
                timeLink.setTarget(target);
            }
            timeLink.setRelType(relType);
           // System.out.println("timeLink.getTarget() = " + timeLink.getTarget());
           // System.out.println("relType = " + relType);
            relType = "";
            timeLinks.add(timeLink);
        }
        else if (qName.equalsIgnoreCase("PLOT_LINK")) {
            PlotLink plotLink = new PlotLink();
            plotLink.setSource(source);
            plotLink.setTarget(target);
            plotLink.setRelType(relType);
            plotLink.setCaused_by(causedBy);
            plotLink.setCauses(causes);
            plotLink.setSignal(signal);
            relType = "";

            plotLinks.add(plotLink);
        }
    }

    public void characters(char ch[], int start, int length)
            throws SAXException {
        value += new String(ch, start, length);
        // System.out.println("tagValue:"+value);
    }

    public void getActorLinks () {
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

    public String getSentenceId (KafTerm kafTerm) {
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

    public String getPhrase (KafTerm kafTerm) {
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
    }

    public String getSentence (String sentenceId) {
        String sentence = "";
        for (int k = 0; k < kafWordFormArrayList.size(); k++) {
            KafWordForm wordForm = kafWordFormArrayList.get(k);
            if (wordForm.getSent().equals(sentenceId)) {
                sentence += " "+wordForm.getWf();
            }
        }
        return sentence.trim();
    }

    public ArrayList<JSONObject> getJsonObject () throws JSONException {
        ArrayList<JSONObject> events = new ArrayList<JSONObject>();
        for (int i = 0; i < eventTermArrayList.size(); i++) {
            JSONObject eventObject = new JSONObject();
            KafTerm eventTerm = eventTermArrayList.get(i);
            String eventPhrase = getPhrase(eventTerm).replaceAll(" ", "_");
            if (!eventPhrase.isEmpty()) {
                eventObject.append("labels", eventPhrase);
                eventObject.append("prefLabel", eventPhrase);
                ///make snippet and mention
                String sentenceId = getSentenceId(eventTerm);
                String sentence = getSentence(sentenceId);
                JSONObject mentionObject = new JSONObject();
                mentionObject.append("snippet", sentence);
                Integer offsetBegin = sentence.indexOf(getPhrase(eventTerm));
                Integer offsetEnd = offsetBegin + getPhrase(eventTerm).length();
                // mentionObject.put("uri", fileName);
                // mentionObject.append("char", offsetBegin);
                // mentionObject.append("char", offsetBegin);
                mentionObject.append("snippet_char", offsetBegin);
                mentionObject.append("snippet_char", offsetEnd);
                eventObject.append("mentions", mentionObject);

                PerspectiveJsonObject perspectiveJsonObject = new PerspectiveJsonObject(new ArrayList<String>(), "", "", "", "", "", fileName, null);
                perspectiveJsonObject.setDefaultPerspectiveValue();
                JSONObject attribution = perspectiveJsonObject.getJSONObject();
                JSONObject perspective = new JSONObject();
                perspective.put("attribution", attribution);
                perspective.put("source", "author:"+fileName);
                mentionObject.append("perspective", perspective);

                JSONObject actors = new JSONObject();
                // System.out.println("actorLinks = " + actorLinks.size());
                for (int j = 0; j < actorLinks.size(); j++) {
                    ActorLink actorLink = actorLinks.get(j);
                    if (actorLink.getSource().equals(eventTerm.getTid())) {
                        for (int k = 0; k < actorTermArrayList.size(); k++) {
                            KafTerm actor = actorTermArrayList.get(k);
                            if (!getPhrase(actor).isEmpty()) {
                                if (actor.getTid().equals(actorLink.getTarget())) {
                                    actors.append("actor:", "ac:" + getPhrase(actor).replaceAll(" ", "_"));
                                }
                            }
                        }
                    }
                }
                for (int j = 0; j < locationLinks.size(); j++) {
                    LocationLink locationLink = locationLinks.get(j);
                    if (locationLink.getSource().equals(eventTerm.getTid())) {
                        for (int k = 0; k < locationTermArrayList.size(); k++) {
                            KafTerm location = locationTermArrayList.get(k);
                            if (location.getTid().equals(locationLink.getTarget())) {
                                actors.append("actor:", "lo:" + getPhrase(location).replaceAll(" ", "_"));
                            }
                        }
                    }
                }
                if (actors.length() == 0) {
                    actors.append("actor:", "noactors:");
                }
                eventObject.put("actors", actors);

                String timeString = "";
                for (int j = 0; j < timeLinks.size(); j++) {
                    TimeLink timeLink = timeLinks.get(j);
                    if (timeLink.getRelType()!=null && timeLink.getRelType().equalsIgnoreCase("INCLUDES")) {
                        if (timeLink.getSource().equals(eventTerm.getTid())) {
                            for (int k = 0; k < timeTermArrayList.size(); k++) {
                                TimeTerm time = timeTermArrayList.get(k);
                                if (time.getTid().equals(timeLink.getTarget())) {
                                    timeString = time.getValue().replaceAll("-", "");
                                    if (timeString.length() == 4) timeString += "0101";
                                    if (timeString.length() == 6) timeString += "01";
                                    break;
                                    /// we stop after the first
                                }
                            }
                        }
                    }
                }

                if (timeString.isEmpty()) {
                    for (int j = 0; j < timeLinks.size(); j++) {
                        TimeLink timeLink = timeLinks.get(j);
                        if (timeLink.getSource().equals(eventTerm.getTid())) {
                            for (int k = 0; k < timeTermArrayList.size(); k++) {
                                TimeTerm time = timeTermArrayList.get(k);
                                if (time.getTid().equals(timeLink.getTarget())) {
                                    timeString = time.getValue().replaceAll("-", "");
                                    if (timeString.length() == 4) timeString += "0101";
                                    if (timeString.length() == 6) timeString += "01";
                                    break;
                                    /// we stop after the first
                                }
                            }
                        }
                    }
                }

                if (timeString.isEmpty()) {
                    //////
                }
                else {
                    eventObject.put("time", timeString);
                    eventObject.put("event", "ev" + i);
                    eventObject.put("group", "100:[" + fileName + "]");
                    eventObject.put("groupName", "[" + fileName + "]");
                    eventObject.put("groupScore", "100");
                    eventObject.put("climax", 4);
                    events.add(eventObject);
                }
            }
        }
        JsonStoryUtil.minimalizeActors(events);

        return events;
    }


    /*

     */
    static void writeJsonObjectArrayWithStructuredData (String pathToFolder,
                                                        String project,
                                                        ArrayList<JSONObject> objects,
                                                        String structuredName,
                                                        ArrayList<JSONObject> structured) {
        try {
            try {
                JSONObject timeLineObject = JsonEvent.createTimeLineProperty(new File(pathToFolder).getName(), project);
                timeLineObject.append("event_cnt", eventTermArrayList.size());
                timeLineObject.append("actor_cnt", actorTermArrayList.size());
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

    static public void main (String[] args) {
        String folder = "";
        String pathToCatFile = "";
        String pathToFtDataFile = "";
        String fileExtension = "";
        //pathToCatFile = "/Users/piek/Desktop/ECBplus_Topic37CAT/37_1ecbplus.xml.xml";
        pathToFtDataFile = "/Users/piek/Desktop/NWR-INC/financialtimes/data/poll.data";
        folder = "/Users/piek/Desktop/ECBplus_Topic37CAT/";
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
        JsonFromCat jsonFromCat = new JsonFromCat();
        ArrayList<JSONObject> structuredEvents = null;
        if (!pathToFtDataFile.isEmpty()) {
            HashMap<String, ArrayList<ReadFtData.DataFt>> dataFtMap = ReadFtData.readData(pathToFtDataFile);
            structuredEvents = ReadFtData.convertFtDataToJsonEventArray(dataFtMap);
        }

        if (!pathToCatFile.isEmpty()) {
            File catFile = new File (pathToCatFile);
            folder = catFile.getParent();
            jsonFromCat.parseFile(pathToCatFile);
            jsonFromCat.getActorLinks();
            ArrayList<JSONObject> events = null;
            try {
                events = jsonFromCat.getJsonObject();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonFromCat.writeJsonObjectArrayWithStructuredData(folder, "ecb*", events,"polls", structuredEvents);
        }
        else if (!folder.isEmpty()) {
            ArrayList<File> files = Util.makeFlatFileList(new File(folder), fileExtension);
            ArrayList<JSONObject> events = new ArrayList<JSONObject>();
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                System.out.println("file.getName() = " + file.getName());
                jsonFromCat.parseFile(file.getAbsolutePath());
                jsonFromCat.getActorLinks();
                try {
                    ArrayList<JSONObject> localEvents = jsonFromCat.getJsonObject();
                    for (int j = 0; j < localEvents.size(); j++) {
                        JSONObject event = localEvents.get(j);
                        events.add(event);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("events.size() = " + events.size());
            jsonFromCat.writeJsonObjectArrayWithStructuredData(folder, "ecb*", events,"polls", structuredEvents);
        }
        System.out.println("DONE.");
    }
}

/*
{ "timeline":
{
"actor_cnt": [686],
"event_cnt": [5238],
"events": [
{
"actors": {"actor:": [
"dbp:European_Union",
"dbp:United_Kingdom",
"co:eurozone",
"co:financial",
"dbp:London"
]},
"climax": 77,
"event": "ev85",
"group": "100:[European Investment Bank]",
"groupName": "[European Investment Bank]",
"groupScore": "100",
"instance": "http://www.ft.com/thing/e5aeef74-8180-11e5-8095-ed1a37d1e096#ev85",
"labels": ["aim"],
"mentions": [{
"snippet": [" of the currency [area] in which they reside\u201d.\nThat clause is specifically aimed at protecting British financial groups from protectionist measures by the eurozone,"],
"snippet_char": [
75,
80
],
"uri": "http://www.ft.com/thing/e5aeef74-8180-11e5-8095-ed1a37d1e096"
}],
"prefLabel": ["aim"],
"time": "20151102"
}
],
polls:{}
"headline": "ECBplus_Topic37CAT",
"text": "ecb*",
"type": "default"
}}
 */