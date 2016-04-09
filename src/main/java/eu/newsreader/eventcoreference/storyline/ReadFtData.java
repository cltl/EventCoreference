package eu.newsreader.eventcoreference.storyline;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 07/04/16.
 */
public class ReadFtData {
    static public class DataFt {
        private String date;
        private String stay;
        private String leave;
        private String undecided;
        private String source;
        private String sample;

        public DataFt() {
            this.date = "";
            this.leave = "";
            this.sample = "";
            this.source = "";
            this.stay = "";
            this.undecided = "";
        }

        /*
        Stay#Leave#Undecided#Date#Source#Sample
        47#49#4#Mar 14, 2016#ORB#823
        43#41#16#Mar 13, 2016#ICM#2,031
         */

        public DataFt(String line) {
            this.date = "";
            this.leave = "";
            this.sample = "";
            this.source = "";
            this.stay = "";
            this.undecided = "";
            String [] fields = line.split("#");
            if (fields.length==6) {
                this.stay = fields[0].trim();
                this.leave = fields[1].trim();
                this.undecided = fields[2].trim();
                this.date = normaliseDate(fields[3].trim());
                this.source = fields[4].trim();
                this.sample = fields[5].trim();
            }
        }

        public DataFt (ArrayList<DataFt> data) {
            this.date = "";
            this.leave = "";
            this.sample = "";
            this.source = "";
            this.stay = "";
            this.undecided = "";
            int leave = 0;
            int stay = 0;
            int undecided = 0;
            int sample = 0;

            for (int i = 0; i < data.size(); i++) {
                DataFt dataFt = data.get(i);
                stay += Integer.parseInt(dataFt.getStay());
                leave += Integer.parseInt(dataFt.getLeave());
                try {
                    undecided += Integer.parseInt(dataFt.getUndecided());
                } catch (NumberFormatException e) {
                  //  e.printStackTrace();
                }
                if (!dataFt.getSample().equals("-")) {
                    sample += Integer.parseInt(dataFt.getSample().replace(",", ""));
                }
                if (!this.source.isEmpty()) {
                    this.source+=";";
                }
                this.source += dataFt.getSource();
                this.date = dataFt.getDate();
            }
            this.stay = new Integer (stay/data.size()).toString();
            this.leave = new Integer (leave/data.size()).toString();
            this.undecided = new Integer (undecided/data.size()).toString();
            this.sample = new Integer (sample).toString();
        }

        public String getBrexit () {
            String outcome = "";
            int stay = Integer.parseInt(this.getStay());
            int leave = Integer.parseInt(this.getLeave());
            int undecided = Integer.parseInt(this.getUndecided());
           // System.out.println("stay = " + stay+" leave = "+leave+" undecided = "+undecided);
            if (stay>leave && stay>undecided) {
                outcome = "Stay";
            }
            else if (leave>stay && leave>undecided) {
                outcome = "Leave";
            }
/*
            else if (undecided>stay && undecided>leave) {
                outcome = "Undecided";
            }
            else if (leave==stay) {
                outcome = "Equal";
            }
*/
            return outcome;
        }

        public String getLabel() {
            String label = "";
            //label +=this.getBrexit()+":"+stay+":"+leave+":"+undecided+":"+sample;
            label +=stay+":"+leave+":"+undecided;
            return label;
        }


        public int getDifference () {
            int dif = 0;
            int stay = Integer.parseInt(this.getStay());
            int leave = Integer.parseInt(this.getLeave());
            int undecided = Integer.parseInt(this.getUndecided());
            String outcome = this.getBrexit();
            if (outcome.equalsIgnoreCase("stay")) {
                dif = 100-stay;
            }
            else if (outcome.equalsIgnoreCase("leave")) {
                dif = 100-leave;
            }
            else {
                dif = 100 - undecided;
            }
            return dif;
        }

        public int getDifferenceStayLeave () {
            int dif = 0;
            int stay = Integer.parseInt(this.getStay());
            int leave = Integer.parseInt(this.getLeave());
            String outcome = this.getBrexit();
            if (outcome.equalsIgnoreCase("stay")) {
                dif = stay-leave;
            }
            else if (outcome.equalsIgnoreCase("leave")) {
                dif = leave-stay;
            }
            else {
                dif = 1;
            }
            if (dif>1) {
                dif = dif/2;
            }
           /* if (dif>10) { dif = dif/10; }
            else { dif = 1; }*/
            return dif;
        }

        public String normaliseDate (String date) {
            String normDate = date.replace("  ", " ");
            String [] fields = normDate.split(" ");
            if (fields.length==3) {
                normDate = fields[2].trim();
                String month = fields[0].trim();
                if (month.equalsIgnoreCase("jan")) { normDate+="01";}
                else if (month.equalsIgnoreCase("feb")) { normDate+="02"; }
                else if (month.equalsIgnoreCase("mar")) { normDate+="03"; }
                else if (month.equalsIgnoreCase("apr")) { normDate+="04"; }
                else if (month.equalsIgnoreCase("may")) { normDate+="05"; }
                else if (month.equalsIgnoreCase("jun")) { normDate+="06"; }
                else if (month.equalsIgnoreCase("jul")) { normDate+="07"; }
                else if (month.equalsIgnoreCase("aug")) { normDate+="08"; }
                else if (month.equalsIgnoreCase("sep")) { normDate+="09"; }
                else if (month.equalsIgnoreCase("oct")) { normDate+="10"; }
                else if (month.equalsIgnoreCase("nov")) { normDate+="11"; }
                else if (month.equalsIgnoreCase("dec")) { normDate+="12"; }
                String day = fields[1].trim();
                if (day.endsWith(",")) {
                    day = day.substring(0, day.length()-1);
                }
                if (day.length()==1) {
                    day = "0"+day;
                }
                normDate += day;
            }
            else {
                System.out.println("fields.toString() = " + fields.toString());
            }
            return normDate;
        }
        public String getDate() {
            return date;
        }

        public String getMonth() {
            String month = date.substring(0,6)+"01";
            return month;
        }

        public String getWeek() {
            String week = date.substring(0,6);
            int dayInt = Integer.parseInt(date.substring(6));
            if (dayInt<8) week += "01";
            if (dayInt>7 && dayInt<15) week += "08";
            if (dayInt>14 && dayInt<22) week += "15";
            if (dayInt>21) week += "22";
            return week;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getLeave() {
            return leave;
        }

        public void setLeave(String leave) {
            this.leave = leave;
        }

        public String getSample() {
            return sample;
        }

        public void setSample(String sample) {
            this.sample = sample;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getStay() {
            return stay;
        }

        public void setStay(String stay) {
            this.stay = stay;
        }

        public String getUndecided() {
            return undecided;
        }

        public void setUndecided(String undecided) {
            this.undecided = undecided;
        }
    }

    static public HashMap<String, ArrayList<DataFt>> readData (String fileName) {
        HashMap<String, ArrayList<DataFt>> map = new HashMap<String, ArrayList<DataFt>>();
        if (new File(fileName).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                if (in.ready()&&(inputLine = in.readLine()) != null) {
                    //"skip header line";
                }
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        DataFt dataFt = new DataFt(inputLine);
                        if(map.containsKey(dataFt.getWeek())) {
                            ArrayList<DataFt> data = map.get(dataFt.getWeek());
                            data.add(dataFt);
                            map.put(dataFt.getWeek(), data);
                        }
                        else {
                            ArrayList<DataFt> data = new ArrayList<DataFt>();
                            data.add(dataFt);
                            map.put(dataFt.getWeek(), data);
                        }
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Cannot find fileName = " + fileName);
        }
        return map;
    }


    static public ArrayList<JSONObject> convertFtDataToJsonEventArray(HashMap<String, ArrayList<DataFt>> dataFtMap) {
        ArrayList<JSONObject> ftEvents = new ArrayList<JSONObject>();
        Set keySet = dataFtMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String dateString = keys.next();
            ArrayList<DataFt> dataFt = dataFtMap.get(dateString);
            DataFt averageFt = new DataFt(dataFt);
            String outcome = averageFt.getBrexit();
            Integer diff = averageFt.getDifferenceStayLeave();
            if (!outcome.isEmpty()) {
                JSONObject event = new JSONObject();
                try {
                    JSONObject actorObject = new JSONObject();
                    // actorObject.append("-", "Brexit");
                    actorObject.append("-", "brexit:" + outcome);
                    //actorObject.append("-", "brexit:Stay");
                    //actorObject.append("-", "brexit:Leave");
                    //actorObject.append("-", "brexit:Undecided");
                    JSONObject mentionObject = createMentionForPoll(averageFt.source);
                    event.append("mentions", mentionObject);
                    event.put("actors", actorObject);
                    event.put("climax", diff.toString());
                    event.put("time", dateString);
                    event.put("group", "100:[" + outcome + "]");
                    event.put("groupName", outcome);
                    event.append("prefLabel", averageFt.getLabel());
                    event.append("labels", averageFt.getLabel());
                    ftEvents.add(event);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return ftEvents;
    }

    static JSONObject createMentionForPoll (String source) {
        JSONObject mObject = new JSONObject();
        try {
            mObject.append("uri", source);
            mObject.append("char", "0, 0");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mObject;
    }

}
