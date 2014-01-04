package eu.newsreader.eventcoreference.util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 1/4/14.
 */
public class Statistics {

    static HashMap<String, Integer> eventLabelMap = new HashMap<String, Integer>();
    static HashMap<String, Integer> actorLabelMap = new HashMap<String, Integer>();
    static HashMap<String, Integer> placeLabelMap = new HashMap<String, Integer>();
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
                                        for (int i = 0; i < mentions.length; i++) {
                                            String mention = mentions[i].trim();
                                            //System.out.println("mention = " + mention);
                                            int idx_m = mention.indexOf("#");
                                            if (idx_m>-1) {
                                                String source = mention.substring(0, idx);
                                                if (!sources.contains(source)) {
                                                    sources.add(source);
                                                }
                                            }
                                        }
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

    static void getStatsOrg(String fileName) {
        /*
        2013-04-30/sem.trig:            a                dbp:India , dbp:Jeep_Grand_Cherokee , dbp:Chrysler , sem:Place ,
        2013-04-30/sem.trig:            a                sem:Place , <nwr:location> , <pb:locate.01> , dbp:New_Jersey ;
         */
        HashMap<String, Integer> anyMap = new HashMap<String, Integer>();
        HashMap<String, Integer> timeMap = new HashMap<String, Integer>();
        HashMap<String, Integer> eventMap = new HashMap<String, Integer>();
        HashMap<String, Integer> placeMap = new HashMap<String, Integer>();
        HashMap<String, Integer> actorMap = new HashMap<String, Integer>();
        //HashMap<String, ArrayList<String>> timeMap = new HashMap<String, ArrayList<String>>();

        if (new File(fileName).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        int idx_s = inputLine.indexOf("@prefix");
                        if (idx_s==-1) {
                            String typeString = inputLine.substring(49).trim();
                            String [] fields = typeString.split(",");
                            boolean p = false;
                            boolean a = false;
                            for (int i = 0; i < fields.length; i++) {
                                String field = fields[i].trim();
                                if (field.startsWith("sem:Place")) {
                                    p = true;
                                }
                                else if (field.startsWith("sem:Actor")) {
                                    a = true;
                                }
                            }
                            for (int i = 0; i < fields.length; i++) {
                                String field = fields[i].trim();
                                if (field.endsWith(";")) {
                                    field = field.substring(0, field.length()-1).trim();
                                    // System.out.println("field = " + field);
                                }
                                if (field.startsWith("dbp:")) {
                                    if (anyMap.containsKey(field)) {
                                        Integer cnt = anyMap.get(field);
                                        cnt++;
                                        anyMap.put(field, cnt);
                                    }
                                    else {
                                        anyMap.put(field, 1);
                                    }
                                    if (a) {
                                        if (actorMap.containsKey(field)) {
                                            Integer cnt = actorMap.get(field);
                                            cnt++;
                                            actorMap.put(field, cnt);
                                        }
                                        else {
                                            actorMap.put(field, 1);
                                        }
                                    }
                                    if (p) {
                                        if (placeMap.containsKey(field)) {
                                            Integer cnt = placeMap.get(field);
                                            cnt++;
                                            placeMap.put(field, cnt);
                                        }
                                        else {
                                            placeMap.put(field, 1);
                                        }
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

            try {
                FileOutputStream fos = new FileOutputStream(fileName+".xls");
                String str = "\tDBP\tsem:Actor\tsem:Place\n";
                str += "ALL ENTITIES\t"+anyMap.size()+"\t"+actorMap.size()+"\t"+placeMap.size()+"\n";
                fos.write(str.getBytes());
                Set keySet = anyMap.keySet();
                Iterator keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    Integer cnt = anyMap.get(key);
                    Integer aCnt = 0;
                    Integer pCnt = 0;
                    if (actorMap.containsKey(key)) {
                        aCnt = actorMap.get(key);
                    }
                    if (placeMap.containsKey(key)) {
                        pCnt = placeMap.get(key);
                    }
                    str = key+"\t"+cnt+"\t"+aCnt+"\t"+pCnt+"\n";
                    fos.write(str.getBytes());
                }
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void saveDBPStats (String fileName) {
        try {
            System.out.println("dbpMap = " + dbpMap.size());
            FileOutputStream fos = new FileOutputStream(fileName);
            Integer nMentions = 0;
            Integer nSources = 0;
            String str = "DBP\tMentions\tSources\tLabels\tTypes\tsem:Actor\tsem:Place\n";
            Set keySet = dbpMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                SemUtilObject semUtilObject = dbpMap.get(key);
                str += semUtilObject.toStatString();
                nMentions += semUtilObject.getMentions();
                nSources += semUtilObject.getDispersion();
                if (actorMap.containsKey(key)) {
                    semUtilObject = actorMap.get(key);
                    str += "\t"+semUtilObject.getMentions();
                }
                if (placeMap.containsKey(key)) {
                    semUtilObject = placeMap.get(key);
                    str += "\t"+semUtilObject.getMentions();
                }
                str += "\n";
            }

            String totalStr = "ALL ENTITIES\t"+dbpMap.size()+"\n";
            totalStr += "\t"+"mentions\t"+nMentions+"\n";
            totalStr += "\t"+"source\t"+nSources+"\n";
            totalStr += "\t"+"dbp size\t"+dbpMap.size()+"\n";
            totalStr += "\t"+"actors\t"+actorMap.size()+"\n";
            totalStr += "\t"+"places\t"+placeMap.size()+"\n";
            totalStr += "\t"+"events\t"+eventMap.size()+"\n";
            totalStr += "\t"+"times\t"+timeMap.size()+"\n\n";
            fos.write(totalStr.getBytes());
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void saveEventStats (String fileName) {
        try {
            System.out.println("eventMap.size() = " + eventMap.size());
            FileOutputStream fos = new FileOutputStream(fileName);
            Integer nMentions = 0;
            Integer nSources = 0;
            String str = "";

            str += "Events\tMentions\tSources\tLabels\tTypes\n";
            Set keySet = eventMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                SemUtilObject semUtilObject = eventMap.get(key);
                str += semUtilObject.toStatString();
                nMentions += semUtilObject.getMentions();
                nSources += semUtilObject.getDispersion();
                str += "\n";
            }

            str += "Event label\t"+"Frequency\n";
            keySet = eventLabelMap.keySet();
            keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Integer cnt = eventLabelMap.get(key);
                str  += key+"\t"+cnt+"\n";
            }

            String totalStr = "ALL EVENTS\t"+ eventMap.size()+"\n";
            totalStr += "\t"+"mentions\t"+nMentions+"\n";
            totalStr += "\t"+"source\t"+nSources+"\n\n";
            fos.write(totalStr.getBytes());
            fos.write(str.getBytes());
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
            Integer nMentions = 0;
            Integer nSources = 0;
            String str = "";
            str +="Actors\tMentions\tSources\tLabels\tTypes\n";
            Set keySet = actorMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                SemUtilObject semUtilObject = actorMap.get(key);
                str += semUtilObject.toStatString();
                nMentions += semUtilObject.getMentions();
                nSources += semUtilObject.getDispersion();
                str += "\n";
            }

            str += "Actor label\t"+"Frequency\n";
            keySet = actorLabelMap.keySet();
            keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Integer cnt = actorLabelMap.get(key);
                str  += key+"\t"+cnt+"\n";
            }


            String totalStr = "ALL ACTORS\t"+actorMap.size()+"\n";
            totalStr += "\t"+"mentions\t"+nMentions+"\n";
            totalStr += "\t"+"source\t"+nSources+"\n\n";
            fos.write(totalStr.getBytes());
            fos.write(str.getBytes());
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
            Integer nMentions = 0;
            Integer nSources = 0;
            String str = "";
            str +="Places\tMentions\tSources\tLabels\tTypes\n";
            Set keySet = placeMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                SemUtilObject semUtilObject = placeMap.get(key);
                str += semUtilObject.toStatString();
                nMentions += semUtilObject.getMentions();
                nSources += semUtilObject.getDispersion();
                str += "\n";
            }

            str += "Place label\t"+"Frequency\n";
            keySet = placeLabelMap.keySet();
            keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Integer cnt = placeLabelMap.get(key);
                str  += key+"\t"+cnt+"\n";
            }



            String totalStr = "ALL PLACES\t"+placeMap.size()+"\n";
            totalStr += "\t"+"mentions\t"+nMentions+"\n";
            totalStr += "\t"+"source\t"+nSources+"\n\n";
            fos.write(totalStr.getBytes());
            fos.write(str.getBytes());
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
            Integer nMentions = 0;
            Integer nSources = 0;
            String str = "Time\tMentions\tSources\tLabels\tTypes\n";
            Set keySet = timeMap.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                SemUtilObject semUtilObject = timeMap.get(key);
                str += semUtilObject.toStatString();
                nMentions += semUtilObject.getMentions();
                nSources += semUtilObject.getDispersion();
                str += "\n";
            }

            String totalStr = "ALL TIMES\t"+timeMap.size()+"\n";
            totalStr += "\t"+"mentions\t"+nMentions+"\n";
            totalStr += "\t"+"source\t"+nSources+"\n\n";
            fos.write(totalStr.getBytes());
            fos.write(str.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void main (String [] args) {
        timeMap = new HashMap<String, SemUtilObject>();
        eventMap = new HashMap<String, SemUtilObject>();
        placeMap = new HashMap<String, SemUtilObject>();
        actorMap = new HashMap<String, SemUtilObject>();
        dbpMap = new HashMap<String, SemUtilObject>();
        eventLabelMap = new HashMap<String, Integer>();
        actorLabelMap = new HashMap<String, Integer>();
        placeLabelMap = new HashMap<String, Integer>();
        String folder = "/Users/piek/Desktop/NWR-DATA/trig/entitycoref";
        EVENT = true;
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
        ArrayList<File> trigFiles = Util.makeRecursiveFileList(new File(folder), ".trig");
        //for (int i = 0; i < trigFiles.size(); i++) {
        for (int i = 0; i < 50; i++) {
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
