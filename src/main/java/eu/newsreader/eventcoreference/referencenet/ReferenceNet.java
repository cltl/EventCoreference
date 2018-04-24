package eu.newsreader.eventcoreference.referencenet;

import eu.newsreader.eventcoreference.objects.PhraseCount;
import eu.newsreader.eventcoreference.pwn.ILIReader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by piek on 20/09/2017.
 */
public class ReferenceNet {

    public static class ReferenceData {
        private Integer mentions;
        private ArrayList<PhraseCount> labels;
        private ArrayList<PhraseCount> synonyms;
        private ArrayList<PhraseCount> esoTypes;
        private ArrayList<PhraseCount> fnTypes;
        private ArrayList<PhraseCount> ilis;


        public ReferenceData() {
            this.mentions = 0;
            this.labels = new ArrayList<PhraseCount>();
            this.synonyms = new ArrayList<PhraseCount>();
            this.esoTypes = new ArrayList<PhraseCount>();
            this.fnTypes = new ArrayList<PhraseCount>();
            this.ilis = new ArrayList<PhraseCount>();
        }

        public void merge(ReferenceData referenceData) {
               this.addMentions(referenceData.getMentions());
               this.addLabels(referenceData.getLabels());
               this.addSynonyms(referenceData.getSynonyms());
               this.addEso(referenceData.getEsoTypes());
               this.addFn(referenceData.getFnTypes());
               this.addIli(referenceData.getIlis());
        }


        public PhraseCount getPhraseCount(String label) {
            for (int i = 0; i < labels.size(); i++) {
                PhraseCount phraseCount = labels.get(i);
                if (phraseCount.getPhrase().equals(label)) {
                    return phraseCount;
                }
            }
            return null;
        }
        public boolean mutualLabelMatch (ReferenceData data, Integer threshold) {
            return mutualDataMatch(this.labels, data.labels, threshold);
        }

        public boolean mutualSynonymMatch (ReferenceData data, Integer threshold) {
            return mutualDataMatch(this.synonyms, data.synonyms, threshold);
        }

        public boolean mutualEsoMatch (ReferenceData data, Integer threshold) {
            return mutualDataMatch(this.esoTypes, data.esoTypes, threshold);
        }

        public boolean mutualFnMatch (ReferenceData data, Integer threshold) {
            return mutualDataMatch(this.fnTypes, data.fnTypes, threshold);
        }

        public boolean mutualIliMatch (ReferenceData data, Integer threshold) {
            return mutualDataMatch(this.ilis, data.ilis, threshold);
        }

        private boolean mutualDataMatch (ArrayList<PhraseCount> phrases1, ArrayList<PhraseCount> phrases2, int threshold) {
            Integer cnt = 0;
            for (int i = 0; i < phrases1.size(); i++) {
                PhraseCount phraseCount1 = phrases1.get(i);
                for (int j = 0; j < phrases2.size(); j++) {
                    PhraseCount phraseCount2 = phrases2.get(j);
                    if (phraseCount1.getPhrase().equals(phraseCount2.getPhrase())) {
                        cnt++;
                        break;
                    }
                }
            }
            Integer prop1 = (100*cnt/(phrases1.size()+1));
            Integer prop2 = (100*cnt/(phrases2.size()+1));
            Integer prop = (prop1+prop2)/2;
            if (prop>=threshold) {
                return true;
            }
            return false;
        }

        public boolean checkFilter(ArrayList<PhraseCount> counts, String filter) {
            if (filter.isEmpty()) {
                return false;
            }
            if (counts.size()==0) {
                return false;
            }
            String [] types = filter.split(";");
            for (int i = 0; i < types.length; i++) {
                String type = types[i];
                for (int j = 0; j < counts.size(); j++) {
                    PhraseCount phraseCount = counts.get(j);
                    if (phraseCount.getPhrase().equals(type)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean checkWordFilter (String filter) {
            return checkFilter(labels, filter);
        }

        public boolean checkEsoFilter (String filter) {
            return checkFilter(esoTypes, filter);
        }

        public boolean checkFnFilter (String filter) {
            return checkFilter(fnTypes, filter);
        }

        public Integer getMentions() {
            return mentions;
        }

        public void setMentions(Integer mentions) {
            this.mentions = mentions;
        }

        public void addMentions(Integer mentions) {
            this.mentions += mentions;
        }

        public ArrayList<PhraseCount> getLabels() {
            return labels;
        }

        public void addPhrase(ArrayList<PhraseCount> phrases, PhraseCount phrase) {
            boolean match = false;
            for (int i = 0; i < phrases.size(); i++) {
                PhraseCount phraseCount = phrases.get(i);
                if (phrase.getPhrase().equals(phraseCount.getPhrase())) {
                    phraseCount.addCount(phrase.getCount());
                    match = true;
                    break;
                }
            }
            if (!match) {
                phrases.add(phrase);
            }
        }
        public void addPhrase(PhraseCount phrase) {
            boolean match = false;
            for (int i = 0; i < labels.size(); i++) {
                PhraseCount phraseCount = labels.get(i);
                if (phrase.getPhrase().equals(phraseCount.getPhrase())) {
                    phraseCount.addCount(phrase.getCount());
                    match = true;
                    break;
                }
            }
            if (!match) {
                labels.add(phrase);
            }
        }

        public void addPhrases(ArrayList<PhraseCount> phrases, String [] fields) {
            for (int i = 0; i < fields.length; i++) {
                String field = fields[i].trim();
                if (!field.isEmpty()) {
                    PhraseCount phraseCount = new PhraseCount(field);
                    addPhrase(phrases, phraseCount);
                }
            }
        }

        public void addNameSpacePhrases(ArrayList<PhraseCount> phrases, String [] fields) {
            for (int i = 0; i < fields.length; i++) {
                String field = fields[i].trim();
                if (!field.isEmpty()) {
                    PhraseCount phraseCount = new PhraseCount();
                    phraseCount.addNameSpaceString(field);
                    addPhrase(phrases, phraseCount);
                }
            }
        }

        public void addPhrases(ArrayList<PhraseCount> phrases, ArrayList<PhraseCount> fields) {
            for (int i = 0; i < fields.size(); i++) {
                PhraseCount phraseCount = fields.get(i);
                addPhrase(phrases, phraseCount);
            }
        }


        public void addLabels (String [] fields) {
            addPhrases(this.labels, fields);
        }
        public void addSynonyms (String [] fields) {
            addPhrases(this.synonyms, fields);
        }
        public void addEso (String [] fields) {
            addNameSpacePhrases(this.esoTypes, fields);
        }
        public void addFn (String [] fields) {
            addNameSpacePhrases(this.fnTypes, fields);
        }
        public void addIli (String [] fields) {
            addNameSpacePhrases(this.ilis, fields);
        }
        public void addLabels (ArrayList<PhraseCount> fields) {
            addPhrases(this.labels, fields);
        }
        public void addSynonyms (ArrayList<PhraseCount> fields) {
            addPhrases(this.synonyms, fields);
        }
        public void addEso (ArrayList<PhraseCount> fields) {
            addPhrases(this.esoTypes, fields);
        }
        public void addFn (ArrayList<PhraseCount> fields) {
            addPhrases(this.fnTypes, fields);
        }
        public void addIli (ArrayList<PhraseCount> fields) {
            addPhrases(this.ilis, fields);
        }

        public ArrayList<PhraseCount> getSynonyms() {
            return synonyms;
        }

        public void setSynonyms(ArrayList<PhraseCount> synonyms) {
            this.synonyms = synonyms;
        }


        public ArrayList<PhraseCount> getEsoTypes() {
            return esoTypes;
        }


        public ArrayList<PhraseCount> getFnTypes() {
            return fnTypes;
        }

        public ArrayList<PhraseCount> getIlis() {
            return ilis;
        }

        public boolean isMostFrequentPhrase (String phrase) {
            SortedSet<PhraseCount> sortedSet = Util.sortPhraseCounts(labels);
            for (PhraseCount phraseCount : sortedSet) {
                if (phraseCount.getPhrase().equals(phrase)) {
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }

        public PhraseCount getMostFrequentFilterPhrase (String wordFilter) {
            SortedSet<PhraseCount> sortedSet = Util.sortPhraseCounts(labels);
            for (PhraseCount phraseCount : sortedSet) {
                if (wordFilter.indexOf(phraseCount.getPhrase())>-1) {
                    return phraseCount;
                }
            }
            return null;
        }

        public void setIlis(ArrayList<PhraseCount> ilis) {
            this.ilis = ilis;
        }

        static public String toCsvHeader () {
            String str = "Nr. mentions\tReference set\tSuper synset\tEso types\tFrameNet types\tILI concepts";
            return str;
        }

        public String toCsvString () {
            String str = "";
            str += this.mentions+"\t";
            SortedSet<PhraseCount> sortedSet = Util.sortPhraseCounts(labels);
            for (PhraseCount phraseCount : sortedSet) {
                str += phraseCount.getPhraseCount()+";";

            }
            str += "\t";
            str += synonyms.size()+":";
            int cnt = 0;
            sortedSet = Util.sortPhraseCounts(synonyms);
            for (PhraseCount phraseCount : sortedSet) {
                cnt++;
                if (cnt==10) break;
                str += phraseCount.getPhraseCount()+";";

            }
            str += "\t";
            str += esoTypes.size()+":";
            cnt = 0;
            sortedSet = Util.sortPhraseCounts(esoTypes);
            for (PhraseCount phraseCount : sortedSet) {
                cnt++;
                if (cnt==10) break;
                str += phraseCount.getPhraseCount()+";";

            }
            str += "\t";
            str += fnTypes.size()+":";
            cnt = 0;
            sortedSet = Util.sortPhraseCounts(fnTypes);
            for (PhraseCount phraseCount : sortedSet) {
                cnt++;
                if (cnt==10) break;
                str += phraseCount.getPhraseCount()+";";

            }
            str += "\t";
            str += ilis.size()+":";
            cnt = 0;
            sortedSet = Util.sortPhraseCounts(ilis);
            for (PhraseCount phraseCount : sortedSet) {
/*                try {

                    //getText(phraseCount.getPhrase());
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                str += phraseCount.getPhraseCount();
                cnt++;
                if (cnt==10) break;
                String iliGloss = "";
                String iliSynset = "";
                String iliSynonym = "";
                String iliId = phraseCount.getPhrase().substring(4);
                if (iliReader.iliToGlossMap.containsKey(iliId)) {
                    iliGloss = iliReader.iliToGlossMap.get(iliId);
                }
                if (iliReader.iliToSynsetMap.containsKey(iliId)) {
                    iliSynset = iliReader.iliToSynsetMap.get(iliId);
                    if (iliReader.synsetToSynonymMap.containsKey(iliSynset)) {
                        ArrayList<String> syns = iliReader.synsetToSynonymMap.get(iliSynset);
                        for (int i = 0; i < syns.size(); i++) {
                            String s = syns.get(i);
                            iliSynonym+= s+",";
                        }
                    }

                }
                str += "["+iliSynset+":"+iliSynonym+":"+iliGloss+"]";
                str +="\t";

            }
            str += "\n";

            return str;
        }

        public String toCsvString (String wordFilter) {
            String str = "";
            str += this.mentions+"\t";
            SortedSet<PhraseCount> sortedSet = Util.sortPhraseCounts(labels);
            for (PhraseCount phraseCount : sortedSet) {
                if (wordFilter.indexOf(phraseCount.getPhrase())>-1) {
                    str += "\\textbf{"+phraseCount.getPhraseCount() +"}"+ ";";
                }
                else {
                    str += phraseCount.getPhraseCount() + ";";
                }

            }
            str += "\t";
            str += synonyms.size()+":";
            int cnt = 0;
            sortedSet = Util.sortPhraseCounts(synonyms);
            for (PhraseCount phraseCount : sortedSet) {
                cnt++;
                if (cnt==10) break;
                str += phraseCount.getPhraseCount()+";";

            }
            str += "\t";
            str += esoTypes.size()+":";
            cnt = 0;
            sortedSet = Util.sortPhraseCounts(esoTypes);
            for (PhraseCount phraseCount : sortedSet) {
                cnt++;
                if (cnt==10) break;
                str += phraseCount.getPhraseCount()+";";

            }
            str += "\t";
            str += fnTypes.size()+":";
            cnt = 0;
            sortedSet = Util.sortPhraseCounts(fnTypes);
            for (PhraseCount phraseCount : sortedSet) {
                cnt++;
                if (cnt==10) break;
                str += phraseCount.getPhraseCount()+";";

            }
            str += "\t";
            str += ilis.size()+":";
            cnt = 0;
            sortedSet = Util.sortPhraseCounts(ilis);
            for (PhraseCount phraseCount : sortedSet) {
/*                try {

                    //getText(phraseCount.getPhrase());
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                str += phraseCount.getPhraseCount();
                cnt++;
                if (cnt==10) break;
                String iliGloss = "";
                String iliSynset = "";
                String iliSynonym = "";
                String iliId = phraseCount.getPhrase().substring(4);
                if (iliReader.iliToGlossMap.containsKey(iliId)) {
                    iliGloss = iliReader.iliToGlossMap.get(iliId);
                }
                if (iliReader.iliToSynsetMap.containsKey(iliId)) {
                    iliSynset = iliReader.iliToSynsetMap.get(iliId);
                    if (iliReader.synsetToSynonymMap.containsKey(iliSynset)) {
                        ArrayList<String> syns = iliReader.synsetToSynonymMap.get(iliSynset);
                        for (int i = 0; i < syns.size(); i++) {
                            String s = syns.get(i);
                            iliSynonym+= s+",";
                        }
                    }

                }
                str += "["+iliSynset+":"+iliSynonym+":"+iliGloss+"]";
                str +="\t";

            }
            str += "\n";

            return str;
        }


        public static String getText(String ili) throws Exception {
            String address = "http://ili.globalwordnet.org/ili/"+ili.substring(5);
            URL url = new URL(address);
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // connection.setRequestProperty("Accept", "application/xml");
            connection.setRequestProperty("Accept", "application/octet-stream");
            InputStream xml = connection.getInputStream();
            String rawText =  getStringFromInputStream(xml);
            return rawText;
        }

        // convert InputStream to String
        private static String getStringFromInputStream(InputStream is) {

            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();

            String line;
            try {

                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return sb.toString();

        }

        String getIliData (String ili) throws Exception {
            final String USER_AGENT = "Mozilla/5.0";
            String address = "http://ili.globalwordnet.org/ili/"+ili.substring(5);
            URL url = new URL(address);
            HttpURLConnection connection =  (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            //System.out.println("connection.getContentType() = " + connection.getContentType());
            //connection.getContentType();
            /*connection.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = connection.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);*/
            connection.setRequestProperty("Accept", "text/plain; charset=utf-8");
            //connection.setRequestProperty("Content-Type", "text/plain");
            //connection.setRequestProperty("Accept", "application/octet-stream");

            //InputStreamReader in = new InputStreamReader((InputStream) connection.getContent());
            InputStreamReader in = new InputStreamReader((InputStream) connection.getInputStream());
            BufferedReader buff = new BufferedReader(in);
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = buff.readLine()) != null) {
                response.append(inputLine);
            }
            buff.close();
            return response.toString();
        }
    }
    static ILIReader iliReader = new ILIReader();

    //static StanfordCoreNLP pipeline;

    static public void main (String[] args) {
        //String filePath = "/Users/piek/Desktop/SemEval2018/sim15wsd6/events.event.xls";
        //String filePath = "/Users/piek/Desktop/gwc-referencenet/data/annotation-sample-20/withincidents/nafsim2wsd0.8.event.xls";
        String filePath = "/Users/piek/Desktop/gwc-referencenet/data/annotation-sample-20/withoutincidents/naf.event.xls";
        //String filePath = "/Users/piek/Desktop/SemEval2018/sim2wsd8/2-events-loose-global/events.event.xls";
        String esoFilterFile = "";
        String fnFilterFile = "";
        String esoFilter = "";
        String fnFilter = "";
        String wordFilter = "";
        wordFilter = "shot;shooting;death;die;kill;dead;accident;wound;incident;it;fire;this;discharge;strike;murder;reckless;hunting;surgery;fatal;go;tragedy;lose;happen;pull;felony;fatally;loss;gunman;autopsy;body;treat;gun;manslaughter;what;hit;gunshot;injure;homicide;firing;blast;victim;stable;assault;take;shoot;bodily;crime;send;violence;that;case;endanger;endangerment;striking;injurious;turn;stabilize;hurt;return;act;shootout;life;use;handle;shotgun;put;critical;unresponsive;deadly;action;at;event;'s;on;situation;mistake;accidental;into;claim;hospitalize;injury";
        //esoFilter = "eso:Killing;eso:Damaging;eso:Destroying";
        //fnFilter = "fn:Killing;fn:Cause_harm;fn:Use_firearm;fn:Shoot_projectiles;fn:Hit_target;fn:Attack;fn:Death;fn:Firing;fn:Destroying;fn:Committing_crime;fn:Damaging;fn:Dodging";
        String iliFile = "/Code/vu/newsreader/vua-resources/ili.ttl.gz";
        Integer threshold = 50;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--ili") && args.length>i+1) {
                iliFile = args[i+1];
            }
            else if (arg.equals("--threshold") && args.length>i+1) {
                try {
                    threshold = Integer.parseInt(args[i+1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else if (arg.equals("--eso-filter") && args.length>i+1) {
                esoFilter = args[i+1];
            }
            else if (arg.equals("--word-filter") && args.length>i+1) {
                wordFilter = args[i+1];
            }
            else if (arg.equals("--fn-filter") && args.length>i+1) {
                fnFilter = args[i+1];
            }
            else if (arg.equals("--file") && args.length>i+1) {
                filePath = args[i+1];
            }
        }
        iliReader = new ILIReader();
        iliReader.readILIFile(iliFile);
      //  Util.initStandfordCoreNLP(pipeline);

        /*if (!esoFilterFile.isEmpty()) {
            ArrayList<String> array = Util.readTextToWordArray(esoFilterFile);
            for (int i = 0; i < array.size(); i++) {
                String s = array.get(i);
                esoFilter+=s+";";
            }
        }
        if (!fnFilterFile.isEmpty()) {
            ArrayList<String> array = Util.readTextToWordArray(esoFilterFile);
            for (int i = 0; i < array.size(); i++) {
                String s = array.get(i);
                fnFilter+=s+";";
            }
        }*/
        ArrayList<ReferenceData> data = readCSVfile(filePath, wordFilter, esoFilter, fnFilter, threshold);
       // ArrayList<ReferenceData> mergedData = merge(data, threshold);
        HashMap<String, ReferenceData> dictionary = getReferenceDataDictionary(data);

        try {
            saveCsv(filePath+".ref", data, data.size());
            saveDic(filePath+".dic", dictionary, data.size(), wordFilter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static HashMap<String, ReferenceData> getReferenceDataDictionary(ArrayList<ReferenceData> data) {
        HashMap<String, ReferenceData> dic = new HashMap<String, ReferenceData>();
        for (int i = 0; i < data.size(); i++) {
            ReferenceData referenceData = data.get(i);
            ArrayList<PhraseCount> labels = referenceData.labels;
            for (int j = 0; j < labels.size(); j++) {
                PhraseCount phraseCount = labels.get(j);
                //System.out.println("phraseCount.getPhrase() = " + phraseCount.getPhrase());
                if (dic.containsKey(phraseCount.getPhrase())) {
                    ReferenceData referenceDataDic = dic.get(phraseCount.getPhrase());
                    referenceDataDic.merge(referenceData);
                    dic.put(phraseCount.getPhrase(), referenceDataDic);
                }
                else {
                    dic.put(phraseCount.getPhrase(), referenceData);
                }
            }
        }
        return dic;
    }

    // /390526	8f4c85d79cbc2bd695161acd95b40097.coref#ev98	35	kill:35;	kill:4;	eso:Killing;
    // 	fn:Killing;	ili:i28306;ili:i28310;ili:i28311;ili:i35417;
    ///597756	f8c367ed5518abe6fc3e0bddcbaa18b1.coref#ev96	103
    // say:38;get:8;tell:8;work:7;want:6;bring:4;take:4;cover:3;need:3;statement:3;action:2;become:2;make:2;candidate:1;cause:1;deal:1;direct:1;director:1;needAWOL:1;state:1;
    // take:33;cover:23;get:19;make:17;do:13;direct:10;deal:9;need:7;have:6;want:6;work:6;say:5;tell:5;aim:4;bring:4;nominate:4;acquire:3;act:3;become:3;catch:3;come:3;director:3;manage:3;accept:2;assume:2;candidate:2;cause:2;choose:2;conduct:2;constitute:2;convey:2;covering:2;create:2;demand:2;earn:2;exercise:2;fix:2;go:2;handle:2;lead:2;make_out:2;name:2;occupy:2;perform:2;produce:2;read:2;require:2;serve:2;take_in:2;take_on:2;action:1;add_up:1;address:1;administer:1;admit:1;adopt:1;allege:1;allot:1;amount:1;answer:1;appoint:1;arrange:1;arrive:1;ask:1;assure:1;aver:1;back:1;behave:1;binding:1;blanket:1;book_binding:1;bring_in:1;build:1;calculate:1;call_for:1;campaigner:1;capture:1;care:1;carry:1;charter:1;claim:1;clear:1;coif:1;coiffe:1;coiffure:1;comprehend:1;concealment:1;consider:1;construct:1;consume:1;contend:1;continue:1;cook:1;cope:1;cover_up:1;covering_fire:1;covert:1;cross:1;cut_across:1;cut_through:1;deal_out:1;desire:1;develop:1;dish_out:1;dispense:1;distribute:1;do_work:1;dole_out:1;dress:1;drive:1;embrace:1;encompass:1;engage:1;enjoin:1;exact:1;execute:1;experience:1;extend:1;fare:1;fetch:1;fill:1;film:1;form:1;function:1;gain:1;garner:1;get_across:1;get_along:1;get_by:1;get_hold_of:1;get_over:1;grapple:1;grow:1;guide:1;hire:1;indigence:1;induce:1;ingest:1;insure:1;involve:1;learn:1;lease:1;let:1;look_at:1;lot:1;make_do:1;make_for:1;manager:1;managing_director:1;masking:1;mete_out:1;motivation:1;motive:1;move:1;narrate:1;natural_covering:1;necessitate:1;nominee:1;number:1;operate:1;opt:1;order:1;overlay:1;pack:1;parcel_out:1;pass_over:1;pauperism:1;pauperization:1;pay_back:1;pay_off:1;penury:1;pick_out:1;place:1;play:1;plow:1;point:1;postulate:1;practice:1;practise:1;prefer:1;prepare:1;propose:1;prospect:1;pull_in:1;put_forward:1;put_up:1;ready:1;realise:1;realize:1;receive:1;recite:1;recount:1;rent:1;report:1;run:1;screen:1;screening:1;select:1;sell:1;send:1;set:1;shell_out:1;shoot:1;spread_over:1;state:1;statement:1;stimulate:1;strike:1;study:1;submit:1;subscribe:1;subscribe_to:1;suffice:1;take_aim:1;take_up:1;target:1;theater_director:1;theatre_director:1;top:1;total:1;track:1;trade:1;train:1;traverse:1;treat:1;turn:1;underwrite:1;use_up:1;work_out:1;wrap_up:1;wreak:1;	eso:BeingEmployed;eso:Creating;eso:HavingAValue;eso:HavingInPossession;eso:Increasing;eso:IntentionalEvent;eso:Placing;eso:Transportation;eso:Working;	fn:Adorning;fn:Amounting_to;fn:Arriving;fn:Becoming;fn:Behind_the_scenes;fn:Being_employed;fn:Bringing;fn:Building;fn:Causation;fn:Cause_change;fn:Change_of_leadership;fn:Change_of_quantity_of_possession;fn:Choosing;fn:Conduct;fn:Cooking_creation;fn:Desiring;fn:Earnings_and_losses;fn:Eclipse;fn:Filling;fn:Getting;fn:Grasp;fn:Intentionally_act;fn:Intentionally_create;fn:Manufacturing;fn:Motion;fn:Needing;fn:Possession;fn:Removing;fn:Reporting;fn:Request;fn:Required_event;fn:Resolve_problem;fn:Self_motion;fn:Statement;fn:Taking;fn:Telling;fn:Text_creation;fn:Topic;fn:Work;fn:Working_a_post;	ili:i112940;ili:i113169;ili:i21819;ili:i21960;ili:i22011;ili:i22243;ili:i22276;ili:i22361;ili:i22363;ili:i22460;ili:i24334;ili:i24342;ili:i24531;ili:i24714;ili:i24760;ili:i24872;ili:i24934;ili:i25071;ili:i25092;ili:i25115;ili:i25251;ili:i25270;ili:i25370;ili:i25432;ili:i25444;ili:i25445;ili:i25476;ili:i25546;ili:i26017;ili:i26075;ili:i26226;ili:i26362;ili:i26365;ili:i26432;ili:i26433;ili:i26486;ili:i26598;ili:i26632;ili:i26659;ili:i26675;ili:i26709;ili:i26742;ili:i26918;ili:i27248;ili:i27365;ili:i27377;ili:i27379;ili:i27393;ili:i27410;ili:i27561;ili:i27563;ili:i27662;ili:i27693;ili:i27699;ili:i28269;ili:i28352;ili:i28369;ili:i28726;ili:i28864;ili:i28895;ili:i28899;ili:i29358;ili:i29849;ili:i29854;ili:i29865;ili:i29902;ili:i29966;ili:i30008;ili:i30054;ili:i30295;ili:i30309;ili:i30403;ili:i30518;ili:i30852;ili:i30918;ili:i30924;ili:i31275;ili:i31486;ili:i31647;ili:i31738;ili:i31766;ili:i32124;ili:i32134;ili:i32302;ili:i32306;ili:i32496;ili:i32759;ili:i32778;ili:i32785;ili:i32790;ili:i32791;ili:

 /*   static ReferenceData csvToData (StanfordCoreNLP pipeline, String line) {
        ReferenceData referenceData = new ReferenceData();
        String [] fields = line.split("\t");
        if (fields.length==8) {
           // System.out.println("fields.length = " + fields.length);
            Integer cnt = Integer.parseInt(fields[2]);
            referenceData.addMentions(cnt);
            String labelsString = fields[3];
            String synonymsString = fields[4];
            String esoString = fields[5];
            String fnString = fields[6];
            String iliString = fields[7];
            if (!labelsString.isEmpty()) {
                String[] labels = labelsString.split(";");
                for (int i = 0; i < labels.length; i++) {
                    String label = labels[i];
                    if (label.length()>1) {
                        PhraseCount phraseCount = new PhraseCount(label);
                        String lemma = Util.getLemmaCoreNLP(pipeline, phraseCount.getPhrase().toLowerCase());
                        phraseCount.setPhrase(lemma);
                        referenceData.addPhrase(phraseCount);
                    }
                }
            }
            if (!synonymsString.isEmpty()) {
                String[] synonyms = synonymsString.split(";");
                referenceData.addSynonyms(synonyms);
            }
            if (!esoString.isEmpty()) {
                String[] eso = esoString.split(";");
                referenceData.addEso(eso);
            }
            if (!fnString.isEmpty()) {
                String[] fn = fnString.split(";");
                referenceData.addFn(fn);
            }
            if (!iliString.isEmpty()) {
                String[] ili = iliString.split(";");
                referenceData.addIli(ili);
            }
           // System.out.println("referenceData = " + referenceData.toCsvString());
        }
        else {
          //  System.out.println("fields.length = " + fields.length);
        }
        return referenceData;
    }*/

    static ArrayList<ReferenceData> readCSVfile (String filePath, String wordFilter, String esoFilter, String fnFilter, Integer threshold)  {
        ArrayList<ReferenceData> data = new ArrayList<ReferenceData>();
        System.out.println("filePath = " + filePath);
        if (new File(filePath).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(filePath);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                int cnt = 0;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    cnt++;
                    //if (cnt==10) break;
                    //System.out.println(inputLine);
                    if (inputLine.trim().length()>0) {
                        ReferenceData referenceData = null;
                        //ReferenceData referenceData = csvToData(pipeline, inputLine.trim());
                        if (wordFilter.isEmpty()) {
                            if (esoFilter.isEmpty() && fnFilter.isEmpty()) {
                                data.add(referenceData);
                            } else if (referenceData.checkEsoFilter(esoFilter) || referenceData.checkFnFilter(fnFilter)) {
                                data.add(referenceData);
                            }
                        }
                        else if (referenceData.checkWordFilter(wordFilter)) {
                            data.add(referenceData);
                        }
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    static ArrayList<ReferenceData> merge(ArrayList<ReferenceData> data , Integer threshold) {
        ArrayList<ReferenceData> mergedData = new ArrayList<ReferenceData>();
        for (int i = 0; i < data.size(); i++) {
            ReferenceData referenceData = data.get(i);
            boolean merged = false;
            for (int j = 0; j < mergedData.size(); j++) {
                ReferenceData mergedReferendeData = mergedData.get(j);
                if (referenceData.mutualLabelMatch(mergedReferendeData, threshold)) {
                    mergedReferendeData.merge(referenceData);
                    merged = true;
                    break;
                }/*
                if (referenceData.mutualEsoMatch(mergedReferendeData, threshold)) {
                    mergedReferendeData.merge(referenceData);
                    merged = true;
                    break;
                }
                if (referenceData.mutualFnMatch(mergedReferendeData, threshold)) {
                    mergedReferendeData.merge(referenceData);
                    merged = true;
                    break;
                }*/
                if (referenceData.mutualIliMatch(mergedReferendeData, threshold)) {
                    mergedReferendeData.merge(referenceData);
                    merged = true;
                    break;
                }
            }
            if (!merged) mergedData.add(referenceData);
        }
        return mergedData;
    }

    static void saveCsv(String filePath, ArrayList<ReferenceData> data, Integer incidents) throws IOException {
        OutputStream fos = new FileOutputStream(filePath);
        String str = new File (filePath).getName()+"\n";
        str += "Total nr of events\t"+incidents+"\n";
        str += "Total nr of merged events\t"+data.size()+"\n";
        fos.write(str.getBytes());
        for (int i = 0; i < data.size(); i++) {
            ReferenceData referenceData = data.get(i);
            fos.write(referenceData.toCsvString().getBytes());
        }
        fos.close();
    }

    static void saveDic(String filePath, HashMap<String, ReferenceData> dic, Integer incidents, String wordFilter) throws IOException {
        OutputStream fos = new FileOutputStream(filePath);
        String str = new File (filePath).getName()+"\n";
        str += "Total nr of events\t"+incidents+"\n";
        str += "Total nr of labels\t"+dic.keySet().size()+"\n";
        str += "Word forms\tFrequency\t"+ReferenceData.toCsvHeader()+"\n";
        fos.write(str.getBytes());
        SortedSet<PhraseCount> treeSet = new TreeSet<PhraseCount>(new PhraseCount.Compare());
        Set keySet = dic.keySet();
        Iterator<String> keys = keySet.iterator();
        if (wordFilter.isEmpty()) {
            while (keys.hasNext()) {
                String label = keys.next();
                ReferenceData referenceData = dic.get(label);
                PhraseCount phraseCount = referenceData.getPhraseCount(label);
                if (phraseCount != null) {
                    treeSet.add(phraseCount);
                }
            }
            for (PhraseCount pcount : treeSet) {
                ReferenceData referenceData = dic.get(pcount.getPhrase());
                if (referenceData.isMostFrequentPhrase(pcount.getPhrase())) {
                    str = pcount.getPhrase() + "\t" + pcount.getCount() + "\t" + referenceData.toCsvString();
                    fos.write(str.getBytes());
                }
            }
        }
        else {
            while (keys.hasNext()) {
                String label = keys.next();
                ReferenceData referenceData = dic.get(label);
                PhraseCount phraseCount = referenceData.getMostFrequentFilterPhrase(wordFilter);
                str = phraseCount.getPhrase() + "\t" + phraseCount.getCount() + "\t" + referenceData.toCsvString(wordFilter);
                fos.write(str.getBytes());
            }

        }
        fos.close();
    }
}
