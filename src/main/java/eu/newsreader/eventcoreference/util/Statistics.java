package eu.newsreader.eventcoreference.util;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 1/4/14.
 */
public class Statistics {

    public static class TotalStats {
        private String year;
        private Integer instances;
        private Integer mentions;
        private Integer sources;
        private Integer labels;
        private Integer singletons;

        public TotalStats() {
            this.year = "";
            this.instances = 0;
            this.mentions = 0;
            this.sources = 0;
            this.labels = 0;
            this.singletons = 0;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public Integer getInstances() {
            return instances;
        }

        public void setInstances(Integer instances) {
            this.instances = instances;
        }

        public void addInstances(Integer instances) {
            this.instances += instances;
        }

        public Integer getMentions() {
            return mentions;
        }

        public Double getMentionsPerInstance() {
            Double rate = 0.0;

            if (this.getInstances()>0) {
              rate = new Double ((double) this.getMentions()/(double) this.getInstances());
            }
            return rate;
        }

        public String getMentionsPerInstanceString() {
            Double rate = 0.0;

            if (this.getInstances()>0) {
              rate = new Double ((double) this.getMentions()/(double) this.getInstances());
            }
            DecimalFormat df = new DecimalFormat("#.00");
            return df.format(rate);
        }

        public void setMentions(Integer mentions) {
            this.mentions = mentions;
        }

        public void addMentions(Integer mentions) {
            this.mentions += mentions;
        }

        public Integer getSources() {
            return sources;
        }

        public void setSources(Integer sources) {
            this.sources = sources;
        }


        public void addSources(Integer sources) {
            this.sources += sources;
        }

        public Double getSourcesPerInstance() {
            Double rate = 0.0;
            if (this.getInstances()>0) {
             rate = new Double ((double) this.getSources()/(double) this.getInstances());
            }
            return rate;
        }

        public String getSourcesPerInstanceString() {
            Double rate = 0.0;

            if (this.getInstances()>0) {
                rate = new Double ((double) this.getSources()/(double) this.getInstances());
            }
            DecimalFormat df = new DecimalFormat("#.00");
            return df.format(rate);
        }

        public Integer getLabels() {
            return labels;
        }

        public void setLabels(Integer labels) {
            this.labels = labels;
        }

        public void addLabels(Integer labels) {
            this.labels += labels;
        }
        public Double getLabelsPerInstance() {
            Double rate = 0.0;
            if (this.getInstances()>0) {
                rate = new Double ((double) this.getLabels()/(double)this.getInstances());
            }
            return rate;
        }

        public String getLabelsPerInstanceString() {
            Double rate = 0.0;

            if (this.getInstances()>0) {
                rate = new Double ((double) this.getLabels()/(double) this.getInstances());
            }
            DecimalFormat df = new DecimalFormat("#.00");
            return df.format(rate);
        }

        public Integer getSingletons() {
            return singletons;
        }

        public void setSingletons(Integer singletons) {
            this.singletons = singletons;
        }
        public void addSingletons(Integer singletons) {
            this.singletons += singletons;
        }

        public Double getSingletonsPerMention() {
            Double rate = 0.0;
            if (this.getInstances()>0) {
                rate = new Double ((double) this.getSingletons()/(double)this.getMentions());
            }
            return rate;
        }

        public String getSingletonsPerMentionString() {
            Double rate = 0.0;

            if (this.getInstances()>0) {
                rate = new Double ((double) this.getSingletons()/(double) this.getMentions());
            }
            DecimalFormat df = new DecimalFormat("#.00");
            return df.format(rate);
        }
    }

        static HashMap<String, Integer> eventLabelMap = new HashMap<String, Integer>();
    static HashMap<String, Integer> actorLabelMap = new HashMap<String, Integer>();
    static HashMap<String, Integer> placeLabelMap = new HashMap<String, Integer>();
    static HashMap<String, Integer> timeLabelMap = new HashMap<String, Integer>();
    static HashMap<String, SemUtilObject> timeMap = new HashMap<String, SemUtilObject>();
    static HashMap<String, SemUtilObject> eventMap = new HashMap<String, SemUtilObject>();
    static HashMap<String, SemUtilObject> placeMap = new HashMap<String, SemUtilObject>();
    static HashMap<String, SemUtilObject> actorMap = new HashMap<String, SemUtilObject>();
    static HashMap<String, SemUtilObject> dbpMap = new HashMap<String, SemUtilObject>();

    static boolean EVENT = false;
    static boolean DBP = false;
    static boolean ACTOR = false;
    static boolean PLACE = false;
    static boolean TIME = false;


    static void storeLabels (HashMap<String, Integer> map, ArrayList<String> labels) {
        for (int i = 0; i < labels.size(); i++) {
            String s = labels.get(i);
            int idx = s.lastIndexOf(":");
            if (idx>-1) {
                String label = s.substring(0, idx);
                Integer cnt = Integer.parseInt(s.substring(idx + 1));
                if (map.containsKey(label)) {
                    Integer labelCnt = map.get(label);
                    labelCnt += cnt;
                    map.put(label, labelCnt);
                }
                else {
                    map.put(label, cnt);
                }
            }
        }
    }
    
    static void storeObject (SemUtilObject semUtilObject, String theType) {
        if (semUtilObject!=null) {
            if (DBP) {
                if (semUtilObject.getUri().startsWith("dbp:") || semUtilObject.getUri().indexOf("dbpedia")>-1) {
                    if (dbpMap.containsKey(semUtilObject.getUri())) {
                        SemUtilObject utilObject = dbpMap.get(semUtilObject.getUri());
                        utilObject.mergeObject(semUtilObject);
                        dbpMap.put(utilObject.getUri(), utilObject);
                    }
                    else {
                        dbpMap.put(semUtilObject.getUri(), semUtilObject);
                    }
                }
            }
            if (EVENT) {
                if (theType.equals("sem:Event")) {
                    if (eventMap.containsKey(semUtilObject.getUri())) {
                        SemUtilObject utilObject = eventMap.get(semUtilObject.getUri());
                        utilObject.mergeObject(semUtilObject);
                        eventMap.put(utilObject.getUri(), utilObject);
                    }
                    else {
                        eventMap.put(semUtilObject.getUri(), semUtilObject);
                    }
                    storeLabels(eventLabelMap, semUtilObject.getLabels());
                }
            }
            if (ACTOR) {
               if (theType.equals("sem:Actor")) {
                    if (actorMap.containsKey(semUtilObject.getUri())) {
                        SemUtilObject utilObject = actorMap.get(semUtilObject.getUri());
                        utilObject.mergeObject(semUtilObject);
                        actorMap.put(utilObject.getUri(), utilObject);
                    }
                    else {
                        actorMap.put(semUtilObject.getUri(), semUtilObject);
                    }
                    storeLabels(actorLabelMap, semUtilObject.getLabels());
                }
            }
            if (PLACE) {
                if (theType.equals("sem:Place")) {
                    if (placeMap.containsKey(semUtilObject.getUri())) {
                        SemUtilObject utilObject = placeMap.get(semUtilObject.getUri());
                        utilObject.mergeObject(semUtilObject);
                        placeMap.put(utilObject.getUri(), utilObject);
                    }
                    else {
                        placeMap.put(semUtilObject.getUri(), semUtilObject);
                    }
                    storeLabels(placeLabelMap, semUtilObject.getLabels());
                }
            }
            if (TIME) {
                if (theType.equals("sem:Time")) {
                    if (timeMap.containsKey(semUtilObject.getUri())) {
                        SemUtilObject utilObject = timeMap.get(semUtilObject.getUri());
                        utilObject.mergeObject(semUtilObject);
                        timeMap.put(utilObject.getUri(), utilObject);
                    }
                    else {
                        timeMap.put(semUtilObject.getUri(), semUtilObject);
                    }
                    storeLabels(timeLabelMap, semUtilObject.getLabels());
                }
            }
        }
    }

    static void getStats(File file) {
        /*
          nwr:instances {

            <http://www.newsreader-project.eu/2004_4_26_4C7M-RB90-01K9-42PW.xml#coe91>
            a              sem:Event ;
            rdfs:label     "discussion:1" ;
            gaf:denotedBy  <http://www.newsreader-project.eu/2004_4_26_4C7M-RB90-01K9-42PW.xml#char=3138,3148&word=w576&term=t576> .

            <http://www.newsreader-project.eu/2004_4_26_4C7V-T4D0-0015-K1B1.xml#coentity9>
            a              sem:Actor , nwr:organization ;
            rdfs:label     "Mitsubishi the Chrysler:1" ;
            gaf:denotedBy  <http://www.newsreader-project.eu/2004_4_26_4C7V-T4D0-0015-K1B1.xml#char=1621,1631&word=w288&term=t288> , <http://www.newsreader-project.eu/2004_4_26_4C7V-T4D0-0015-K1B1.xml#char=1632,1635&word=w289&term=t289> , <http://www.newsreader-project.eu/2004_4_26_4C7V-T4D0-0015-K1B1.xml#char=1636,1644&word=w290&term=t290> .

            dbp:Chrysler_PT_Cruiser
            a              sem:Actor , nwr:organization ;
            rdfs:label     "PT Cruiser:2" ;
            gaf:denotedBy  <http://www.newsreader-project.eu/2004_4_26_4C7V-T4D0-0015-K1B1.xml#char=1930,1932&word=w341&term=t341> , <http://www.newsreader-project.eu/2004_4_26_4C7V-T4D0-0015-K1B1.xml#char=1933,1940&word=w342&term=t342> , <http://www.newsreader-project.eu/2004_4_26_4C7V-T4D0-0015-K1B1.xml#char=5159,5161&word=w974&term=t974> , <http://www.newsreader-project.eu/2004_4_26_4C7V-T4D0-0015-K1B1.xml#char=5162,5169&word=w975&term=t975> .

            dbp:India  a           sem:Actor , nwr:misc , sem:Place , nwr:location ;
            rdfs:label     "indian:1" , "India:2" ;
            gaf:denotedBy  <http://www.newsreader-project.eu/2004_4_26_4C7V-T4D0-0015-K1B6.xml#char=1404,1410&word=w261&term=t261> , <http://www.newsreader-project.eu/2004_4_26_4C7N-GTG0-0002-M1S5.xml#char=364,369&word=w69&term=t69> .


            }
         */
        if (file.exists() ) {
            try {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                String theType = "";
                boolean read = false;
                SemUtilObject semUtilObject = null;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                   // System.out.println(inputLine);
                    inputLine = inputLine.trim();
                    if (inputLine.trim().length()>0) {
                        int idx_s = inputLine.indexOf("nwr:instances {");
                        if (idx_s>-1) {
                            read = true;
                        }
                        else if (read) {
                            if (inputLine.indexOf("}")>-1) {
                                storeObject(semUtilObject, theType);
                                break;
                            }
                            else {
                                if (inputLine.toLowerCase().startsWith("<http")) {
                                    storeObject(semUtilObject, theType);
                                    semUtilObject = new SemUtilObject();
                                    semUtilObject.setUri(inputLine.trim());
                                }
                                else if (inputLine.toLowerCase().startsWith("nwr:")) {
                                    storeObject(semUtilObject, theType);
                                    semUtilObject = new SemUtilObject();
                                    semUtilObject.setUri(inputLine.trim());
                                }
                                else if (inputLine.toLowerCase().startsWith("dbp:")) {
                                    //dbp:India  a           sem:Actor , nwr:misc , sem:Place , nwr:location ;
                                    storeObject(semUtilObject, theType);
                                    semUtilObject = new SemUtilObject();
                                    int idx = inputLine.indexOf(" a ");
                                    if (idx>-1) {
                                        semUtilObject.setUri(inputLine.substring(0, idx).trim());
                                        String[] types = inputLine.substring(idx+3).trim().split(",");
                                        for (int i = 0; i < types.length; i++) {
                                            String type = types[i].trim();
                                            semUtilObject.addTypes(type);
                                            if (type.equals("sem:Event")) {
                                                theType = type;
                                            }
                                            else if (type.equals("sem:Place")) {
                                                theType = type;
                                            }
                                            else if (type.equals("sem:Actor")) {
                                                theType = type;
                                            }
                                            else if (type.equals("sem:Time")) {
                                                theType = type;
                                            }
                                        }
                                    }
                                }
                                else if (inputLine.startsWith("a       ")) {
                                    //a              sem:Actor , nwr:organization ;
                                    String[] types = inputLine.substring(3).trim().split(",");
                                    for (int i = 0; i < types.length; i++) {
                                        String type = types[i].trim();
                                        semUtilObject.addTypes(type);
                                        if (type.equals("sem:Event")) {
                                            theType = type;
                                        }
                                        else if (type.equals("sem:Place")) {
                                            theType = type;
                                        }
                                        else if (type.equals("sem:Actor")) {
                                            theType = type;
                                        }
                                        else if (type.equals("sem:Time")) {
                                            theType = type;
                                        }
                                    }
                                }
                                else if (inputLine.toLowerCase().startsWith("rdfs:label")) {
                                    //rdfs:label     "indian:1" , "India:2" ;
                                    int idx = inputLine.indexOf("\"");
                                    if (idx >-1) {
                                        String[] labels = inputLine.substring(idx).trim().split("\" , \"");
                                        for (int i = 0; i < labels.length; i++) {
                                            String label = labels[i].trim();
                                            if (label.startsWith("\"")) {
                                                label = label.substring(1);
                                            }
                                            if (label.endsWith("\" ;")) {
                                                label = label.substring(0, label.length()-3);
                                            }
                                            label = label.replaceAll("\t", " ");
                                            //System.out.println("label = " + label);
                                            semUtilObject.addLabels(label);
                                        }
                                    }
                                }
                                else if (inputLine.startsWith("gaf:denotedBy")) {
                                    //gaf:denotedBy  <http://www.newsreader-project.eu/2004_4_26_4C7V-T4D0-0015-K1B6.xml#char=1404,1410&word=w261&term=t261> , <http://www.newsreader-project.eu/2004_4_26_4C7N-GTG0-0002-M1S5.xml#char=364,369&word=w69&term=t69> .
                                    int idx = inputLine.indexOf(" <");
                                    if (idx >-1) {
                                        String[] mentions = inputLine.substring(idx).trim().split("<");
                                        semUtilObject.setMentions(mentions.length);
                                        ArrayList<String> sources = new ArrayList<String>();
                                        ArrayList<String> dupsources = new ArrayList<String>();
                                        for (int i = 0; i < mentions.length; i++) {
                                            String mention = mentions[i].trim();
                                            //System.out.println("mention = " + mention);
                                            int idx_m = mention.indexOf("#");
                                            if (idx_m>-1) {
                                                String source = mention.substring(0, idx);
                                                if (!sources.contains(source)) {
                                                    sources.add(source);
                                                }
                                                else {
                                                    if (!dupsources.contains(source)) {
                                                        dupsources.add(source);
                                                    }
                                                }
                                            }
                                        }
                                        Integer singletons = 0;
                                        for (int i = 0; i < sources.size(); i++) {
                                            String s = sources.get(i);
                                            if (dupsources.contains(s)) {
                                                singletons++;
                                            }
                                        }
                                       // System.out.println("singletons = " + singletons);
                                        semUtilObject.setSingletons(singletons);
                                        semUtilObject.setDispersion(sources.size());
                                    }
                                    else {
                                      //  System.out.println("inputLine = " + inputLine);
                                    }
                                }
                            }
                        }
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     *             String totalStr = "ALL ENTITIES\t"+dbpMap.size()+"\n";
     totalStr += "\t"+"mentions\t"+nMentions+"\n";
     totalStr += "\t"+"source\t"+nSources+"\n";
     totalStr += "\t"+"dbp size\t"+dbpMap.size()+"\n";
     totalStr += "\t"+"actors\t"+actorMap.size()+"\n";
     totalStr += "\t"+"places\t"+placeMap.size()+"\n";
     totalStr += "\t"+"events\t"+eventMap.size()+"\n";
     totalStr += "\t"+"times\t"+timeMap.size()+"\n\n";
     fos.write(totalStr.getBytes());

     */

    static void addToInstanceOverviewStats (HashMap<String, ArrayList<TotalStats>> totalMap,  HashMap<String, SemUtilObject> aMap, String year) {
        Set keySet = aMap.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            SemUtilObject semUtilObject = aMap.get(key);
            TotalStats totalStats = new TotalStats();
            totalStats.setYear(year);
            totalStats.addMentions(semUtilObject.getMentions());
            totalStats.addSources(semUtilObject.getDispersion());
            totalStats.addLabels(semUtilObject.getLabels().size());

            if (totalMap.containsKey(key)) {
                ArrayList<TotalStats> totalStatsArray = totalMap.get(key);
                totalStatsArray.add(totalStats);
                totalMap.put(key, totalStatsArray);
            }
            else {

                ArrayList<TotalStats> totalStatsArray = new ArrayList<TotalStats>();
                totalStatsArray.add(totalStats);
                totalMap.put(key, totalStatsArray);
            }
        }
    }

    static void addToLabelOverviewStats (HashMap<String, ArrayList<Integer>> totalMap,  HashMap<String, Integer> aMap) {
        Set keySet = aMap.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            Integer cnt = aMap.get(key);
            if (totalMap.containsKey(key)) {
                ArrayList<Integer> totalStatsArray = totalMap.get(key);
                totalStatsArray.add(cnt);
                totalMap.put(key, totalStatsArray);
            }
            else {

                ArrayList<Integer> totalStatsArray = new ArrayList<Integer>();
                totalStatsArray.add(cnt);
                totalMap.put(key, totalStatsArray);
            }
        }
    }

    static void saveDBPStats (String fileName) {
        try {
            System.out.println("dbpMap = " + dbpMap.size());
            FileOutputStream fos = new FileOutputStream(fileName);
            String str = "DBP\tMentions\tSources\tLabels\tTypes\tsem:Actor\tsem:Place\n";
            fos.write(str.getBytes());
            Set keySet = dbpMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                SemUtilObject semUtilObject = dbpMap.get(key);
                str = semUtilObject.toStatString();
                if (actorMap.containsKey(key)) {
                    semUtilObject = actorMap.get(key);
                    str += "\t"+semUtilObject.getMentions();
                }
                if (placeMap.containsKey(key)) {
                    semUtilObject = placeMap.get(key);
                    str += "\t"+semUtilObject.getMentions();
                }
                str += "\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void saveEventStats (String fileName) {
        try {
            System.out.println("eventMap.size() = " + eventMap.size());
            FileOutputStream fos = new FileOutputStream(fileName);
            String str = "";
            str = "Events\tMentions\tSources\tLabels\tTypes\n";
            fos.write(str.getBytes());

            Set keySet = eventMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                SemUtilObject semUtilObject = eventMap.get(key);
                str = semUtilObject.toStatString();
                str += "\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void saveEventLabelStats (String fileName) {
        try {
            System.out.println("eventLabelMap.size() = " + eventLabelMap.size());
            FileOutputStream fos = new FileOutputStream(fileName);
            String str = "Event label\t"+"Frequency\n";
            fos.write(str.getBytes());
            Set keySet = eventLabelMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Integer cnt = eventLabelMap.get(key);
                str  = key+"\t"+cnt+"\n";
                fos.write(str.getBytes());

            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void saveActorStats  (String fileName) {
        try {
            System.out.println("actorMap.size() = " + actorMap.size());
            FileOutputStream fos = new FileOutputStream(fileName);
            String str = "";
            str ="Actors\tMentions\tSources\tLabels\tTypes\n";
            fos.write(str.getBytes());

            Set keySet = actorMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                SemUtilObject semUtilObject = actorMap.get(key);
                str = semUtilObject.toStatString();
                str += "\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static void saveActorLabelStats  (String fileName) {
        try {
            System.out.println("actorLabelMap.size() = " + actorLabelMap.size());
            FileOutputStream fos = new FileOutputStream(fileName);

            String str = "";
            str += "Actor label\t"+"Frequency\n";
            fos.write(str.getBytes());
            Set keySet = actorLabelMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Integer cnt = actorLabelMap.get(key);
                str  = key+"\t"+cnt+"\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    static void savePlacesStats  (String fileName) {
        try {
            System.out.println("placeMap.size() = " + placeMap.size());
            FileOutputStream fos = new FileOutputStream(fileName);
            String str = "";
            str = "ALL PLACES\t"+placeMap.size()+"\n";
            str +="Places\tMentions\tSources\tLabels\tTypes\n";
            fos.write(str.getBytes());

            Set keySet = placeMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                SemUtilObject semUtilObject = placeMap.get(key);
                str = semUtilObject.toStatString();
                str += "\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void savePlaceLabelStats  (String fileName) {
        try {
            System.out.println("placeLabelMap.size() = " + placeLabelMap.size());
            FileOutputStream fos = new FileOutputStream(fileName);

            String str = "";
            str += "Place label\t"+"Frequency\n";
            fos.write(str.getBytes());
            Set keySet = placeLabelMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Integer cnt = placeLabelMap.get(key);
                str  = key+"\t"+cnt+"\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void saveTimeStats  (String fileName) {
        try {
            System.out.println("timeMap.size() = " + timeMap.size());
            FileOutputStream fos = new FileOutputStream(fileName);
            String str = "ALL TIMES\t"+timeMap.size()+"\n";
            str += "Time\tMentions\tSources\tLabels\tTypes\n";
            fos.write(str.getBytes());

            Set keySet = timeMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                SemUtilObject semUtilObject = timeMap.get(key);
                str += semUtilObject.toStatString();
                str += "\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static public void main (String [] args) {
        String folder = "/Users/piek/Desktop/NWR-DATA/trig/entitycoref";
        EVENT = true;
        DBP = true;
        ACTOR = true;
        PLACE = true;
        TIME = true;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--event")) {
                EVENT = true;
            }
            else if (arg.equals("--dbp")) {
                DBP = true;
            }
            else if (arg.equals("--actor")) {
                ACTOR = true;
            }
            else if (arg.equals("--place")) {
                PLACE = true;
            }
            else if (arg.equals("--time")) {
                TIME = true;
            }
            else if (arg.equals("--folder") && args.length>(i+1)) {
                folder =args[i+1];
            }
        }
        //statsForAll(folder);
        statsPerYear(folder);
    }


    static void statsPerYear (String folder) {

        try {
            Integer eventLabelMax = 0;
            Integer actorLabelMax = 0;
            Integer placeLabelMax = 0;
            Integer timeLabelMax = 0;
            ArrayList<String> yearStrings = new ArrayList<String>();
            yearStrings.add("Total");
            HashMap<String, ArrayList<String>> yearMap = new HashMap<String, ArrayList<String>>();
            ArrayList<File> years = Util.makeFolderList(new File(folder));
            for (int i = 0; i < years.size(); i++) {
                File file =  years.get(i);
                int idx = file.getName().indexOf("-");
                if (idx>-1) {
                    String year = file.getName().substring(0, idx);
                    if (!yearStrings.contains(year)) {
                        yearStrings.add(year);
                    }
                    if (yearMap.containsKey(year)) {
                        ArrayList<String> folders = yearMap.get(year);
                        folders.add(file.getAbsolutePath());
                        yearMap.put(year, folders);
                    }
                    else {
                        ArrayList<String> folders = new ArrayList<String>();
                        folders.add(file.getAbsolutePath());
                        yearMap.put(year, folders);
                    }
                }
            }
            HashMap<String, Integer[]> eventLabelTotalMap = new HashMap<String, Integer[]>();
            HashMap<String, Integer[]> actorLabelTotalMap = new HashMap<String, Integer[]>();
            HashMap<String, Integer[]> placeLabelTotalMap = new HashMap<String, Integer[]>();
            HashMap<String, Integer[]> timeLabelTotalMap = new HashMap<String, Integer[]>();
            String statFile = folder+"/"+"yearOverview.xls";
            FileOutputStream fos = new FileOutputStream(statFile);
            String str = "\t"+"DBP\t\t\t\t\t\t\t"+"Events\t\t\t\t\t\t\t"+ "Actors\t\t\t\t\t\t\t"+ "Places\t\t\t\t\t\t\t"+ "Times\t\t\t\t\t\t\t"+"\n";
            str += "YEAR\t"+"Instances\tMentions\tM/I\tSources\tS/I\tLabels\tL/I\t"
                    +"Instances\tMentions\tM/I\tSources\tS/I\tLabels\tL/I\t"
                    +"Instances\tMentions\tM/I\tSources\tS/I\tLabels\tL/I\t"
                    +"Instances\tMentions\tM/I\tSources\tS/I\tLabels\tL/I\t"
                    +"Instances\tMentions\tM/I\tSources\tS/I\tLabels\tL/I\t"+"\n";
            fos.write(str.getBytes());
            for (int y = 1; y < yearStrings.size(); y++) { // skip the first [0] which holds the total
                String year =  yearStrings.get(y);
               // System.out.println("year = " + year);
                ArrayList<String> folders = yearMap.get(year);
                timeMap = new HashMap<String, SemUtilObject>();
                eventMap = new HashMap<String, SemUtilObject>();
                placeMap = new HashMap<String, SemUtilObject>();
                actorMap = new HashMap<String, SemUtilObject>();
                dbpMap = new HashMap<String, SemUtilObject>();
                eventLabelMap = new HashMap<String, Integer>();
                actorLabelMap = new HashMap<String, Integer>();
                placeLabelMap = new HashMap<String, Integer>();
                timeLabelMap = new HashMap<String, Integer>();
                for (int i = 0; i < folders.size(); i++) {
                    String nextFolder =  folders.get(i);
                    ArrayList<File> trigFiles = Util.makeRecursiveFileList(new File(nextFolder), ".trig");
                    for (int f = 0; f < trigFiles.size(); f++) {
                        File file = trigFiles.get(f);
                        getStats(file);
                    }
                }
                str = year;
                TotalStats totalStats = null;
                totalStats = getTotalStatsString(dbpMap, year);
                str += "\t"+totalStats.getInstances()+"\t"+totalStats.getMentions()+"\t"+totalStats.getMentionsPerInstanceString()+
                        "\t"+totalStats.getSources() +"\t"+totalStats.getSourcesPerInstanceString()+
                        "\t"+totalStats.getLabels() +"\t"+totalStats.getLabelsPerInstanceString();
                totalStats = getTotalStatsString(eventMap, year);
                System.out.println(year+"\t"+totalStats.getMentions()+"\t" + totalStats.getSingletons()+"\t"+totalStats.getSingletonsPerMentionString());
                totalStats.setLabels(eventLabelMap.size());
                str += "\t"+totalStats.getInstances()+"\t"+totalStats.getMentions()+"\t"+totalStats.getMentionsPerInstanceString()+
                        "\t"+totalStats.getSources() +"\t"+totalStats.getSourcesPerInstanceString()+
                        "\t"+totalStats.getLabels() +"\t"+totalStats.getLabelsPerInstanceString();
                totalStats = getTotalStatsString(actorMap, year);
                totalStats.setLabels(actorLabelMap.size());
                str += "\t"+totalStats.getInstances()+"\t"+totalStats.getMentions()+"\t"+totalStats.getMentionsPerInstanceString()+
                        "\t"+totalStats.getSources() +"\t"+totalStats.getSourcesPerInstanceString()+
                        "\t"+totalStats.getLabels() +"\t"+totalStats.getLabelsPerInstanceString();
                totalStats = getTotalStatsString(placeMap, year);
                totalStats.setLabels(placeLabelMap.size());
                str += "\t"+totalStats.getInstances()+"\t"+totalStats.getMentions()+"\t"+totalStats.getMentionsPerInstanceString()+
                        "\t"+totalStats.getSources() +"\t"+totalStats.getSourcesPerInstanceString()+
                        "\t"+totalStats.getLabels() +"\t"+totalStats.getLabelsPerInstanceString();
                totalStats = getTotalStatsString(timeMap, year);
                str += "\t"+totalStats.getInstances()+"\t"+totalStats.getMentions()+"\t"+totalStats.getMentionsPerInstanceString()+
                        "\t"+totalStats.getSources() +"\t"+totalStats.getSourcesPerInstanceString()+
                        "\t"+totalStats.getLabels() +"\t"+totalStats.getLabelsPerInstanceString();
                str += "\n";
                fos.write(str.getBytes());
                eventLabelMax = updateTotalLabelMap(eventLabelTotalMap, eventLabelMap, y, yearStrings.size());
                actorLabelMax = updateTotalLabelMap(actorLabelTotalMap, actorLabelMap, y, yearStrings.size());
                placeLabelMax = updateTotalLabelMap(placeLabelTotalMap, placeLabelMap, y, yearStrings.size());
                timeLabelMax = updateTotalLabelMap(timeLabelTotalMap, timeLabelMap, y, yearStrings.size());
            }
            fos.close();

            String labelFile = folder+"/"+"labelUsageEvents.xls";
            fos = new FileOutputStream(labelFile);
            str = "Event labels\t"+eventLabelTotalMap.size()+"\n";
            fos.write(str.getBytes());
            str = "Events";
            for (int i = 0; i < yearStrings.size(); i++) {
                String s = yearStrings.get(i);
                str += "\t"+s;
            }
            str += "\n";
            fos.write(str.getBytes());
            writeLabelsStats(eventLabelTotalMap, eventLabelMax, fos);
            fos.close();

            labelFile = folder+"/"+"labelUsageActors.xls";
            fos = new FileOutputStream(labelFile);
            str = "Actor labels\t"+actorLabelTotalMap.size()+"\n";
            fos.write(str.getBytes());
            str = "Actors";
            for (int i = 0; i < yearStrings.size(); i++) {
                String s = yearStrings.get(i);
                str += "\t"+s;
            }
            str += "\n";
            fos.write(str.getBytes());
            writeLabelsStats(actorLabelTotalMap, actorLabelMax, fos);
            fos.close();

            labelFile = folder+"/"+"labelUsagePlaces.xls";
            fos = new FileOutputStream(labelFile);
            str = "Places labels\t"+placeLabelTotalMap.size()+"\n";
            fos.write(str.getBytes());
            str = "Places";
            for (int i = 0; i < yearStrings.size(); i++) {
                String s = yearStrings.get(i);
                str += "\t"+s;
            }
            str += "\n";
            fos.write(str.getBytes());
            writeLabelsStats(placeLabelTotalMap, placeLabelMax, fos);
            fos.close();


            labelFile = folder+"/"+"labelUsageTimes.xls";
            fos = new FileOutputStream(labelFile);
            str = "Times labels\t"+timeLabelTotalMap.size()+"\n";
            fos.write(str.getBytes());
            str = "Times";
            for (int i = 0; i < yearStrings.size(); i++) {
                String s = yearStrings.get(i);
                str += "\t"+s;
            }
            str += "\n";
            fos.write(str.getBytes());
            writeLabelsStats(timeLabelTotalMap, timeLabelMax, fos);
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Integer updateTotalLabelMap (HashMap<String, Integer[]> totalLabelMap, HashMap<String, Integer> labelMap, int y, int maxYears) {
        Integer max = 0;
        Set labelSet = labelMap.keySet();
        Iterator labels = labelSet.iterator();
        while (labels.hasNext()) {
            String label = (String) labels.next();
            Integer cnt = labelMap.get(label);
            if (totalLabelMap.containsKey(label)) {
                Integer[] cnts = totalLabelMap.get(label);
                cnts[y]=cnt;
                cnts[0] += cnt;
                if (cnts[0]>max) {
                    max = cnts[0];
                }
                totalLabelMap.put(label, cnts);
            }
            else {
                Integer[] cnts = new Integer[maxYears];
                for (int i = 0; i < cnts.length; i++) {
                    cnts[i] = 0;
                }
                cnts[0]=cnt; // index 0 represents the total
                cnts[y]=cnt; // index 1 to represent the first year;
                if (cnt>max) {
                    max = cnt;
                }
                totalLabelMap.put(label, cnts);
            }
        }
        return max;
    }

    static void writeLabelsStats(HashMap<String, Integer[]> totalLabelMap, Integer max, FileOutputStream fos) {
        Set labelSet = totalLabelMap.keySet();
        Iterator labels = labelSet.iterator();
        while (labels.hasNext()) {
            String label = (String) labels.next();
            Integer[] cnts = totalLabelMap.get(label);
            if ((cnts[0]%max)>=95) {
                String str = label;
                for (int i = 0; i < cnts.length; i++) {
                    Integer cnt = cnts[i];
                    str += "\t"+cnt;

                }
                str += "\n";
                try {
                    fos.write(str.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static TotalStats getTotalStatsString (HashMap<String, SemUtilObject> totalMap, String year){
            TotalStats totalStats = new TotalStats();
            Set keySet = totalMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                SemUtilObject semUtilObject = totalMap.get(key);
                totalStats.addMentions(semUtilObject.getMentions());
                totalStats.addSources(semUtilObject.getDispersion());
                totalStats.addLabels(semUtilObject.getLabels().size());
                totalStats.addSingletons(semUtilObject.getSingletons());
            }
            totalStats.setInstances(totalMap.size());
            totalStats.setYear(year);
            return totalStats;
    }


    static public void statsForAll (String folder) {
        timeMap = new HashMap<String, SemUtilObject>();
        eventMap = new HashMap<String, SemUtilObject>();
        placeMap = new HashMap<String, SemUtilObject>();
        actorMap = new HashMap<String, SemUtilObject>();
        dbpMap = new HashMap<String, SemUtilObject>();
        eventLabelMap = new HashMap<String, Integer>();
        actorLabelMap = new HashMap<String, Integer>();
        placeLabelMap = new HashMap<String, Integer>();
        ArrayList<File> trigFiles = Util.makeRecursiveFileList(new File(folder), ".trig");
        for (int i = 0; i < trigFiles.size(); i++) {
        //for (int i = 0; i < 50; i++) {
            File file = trigFiles.get(i);
           /// System.out.println("file = " + file.getAbsolutePath());
            getStats(file);
        }
        String statFile = "";
        if (DBP) {
            statFile = folder+"/"+"stats-dbp.xls";
            saveDBPStats(statFile);
        }
        if (EVENT) {
            statFile = folder+"/"+"stats-event.xls";
            saveEventStats(statFile);
            statFile = folder+"/"+"stats-event-labels.xls";
            saveEventLabelStats(statFile);
        }
        if (ACTOR) {
            statFile = folder+"/"+"stats-actor.xls";
            saveActorStats(statFile);
            statFile = folder+"/"+"stats-actor-labels.xls";
            saveActorLabelStats(statFile);
        }
        if (PLACE) {
            statFile = folder+"/"+"stats-place.xls";
            savePlacesStats(statFile);
            statFile = folder+"/"+"stats-place-labels.xls";
            savePlaceLabelStats(statFile);
        }
        if (TIME) {
            statFile = folder+"/"+"stats-time.xls";
            saveTimeStats(statFile);
        }
    }

}
