package eu.newsreader.eventcoreference.output;

import com.hp.hpl.jena.rdf.model.Statement;
import eu.newsreader.eventcoreference.input.TrigTripleData;
import eu.newsreader.eventcoreference.input.TrigTripleReader;
import eu.newsreader.eventcoreference.objects.PhraseCount;
import eu.newsreader.eventcoreference.pwn.ILIReader;
import eu.newsreader.eventcoreference.storyline.JsonFromRdf;
import eu.newsreader.eventcoreference.storyline.JsonStoryUtil;
import eu.newsreader.eventcoreference.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static eu.newsreader.eventcoreference.storyline.JsonFromRdf.getValue;

/**
 * Created by piek on 12/05/16.
 */
public class TrigStats {

    static public String STAT = "";
    static public boolean uselocalurl = false;

    static HashMap<String, PhraseCount> dbpMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> evMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> enMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> neMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> topicMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> authMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> citeMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> eventLabelMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> eventLabelWithoutESOMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> eventLabelWithoutFNMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> eventLabelWithoutILIMap = new HashMap<String, PhraseCount>();
    static HashMap<String, ArrayList<String>> eventTypeMap = new HashMap<String, ArrayList<String>>();
    static HashMap<String, ArrayList<String>> lightEntityMap = new HashMap<String, ArrayList<String>>();
    static HashMap<String, ArrayList<String>> darkEntityMap = new HashMap<String, ArrayList<String>>();
    static HashMap<String, ArrayList<PhraseCount>> lightEntityPhraseCountMap = new HashMap<String, ArrayList<PhraseCount>>();
    static HashMap<String, ArrayList<PhraseCount>> darkEntityPhraseCountMap = new HashMap<String, ArrayList<PhraseCount>>();
    static HashMap<String, ArrayList<PhraseCount>> nonEntityPhraseCountMap = new HashMap<String, ArrayList<PhraseCount>>();
    static HashMap<String, ArrayList<PhraseCount>> eventPhraseCountMap = new HashMap<String, ArrayList<PhraseCount>>();
    static HashMap<String, PhraseCount> esoMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> fnMap = new HashMap<String, PhraseCount>();
    static HashMap<String, PhraseCount> iliMap = new HashMap<String, PhraseCount>();

    static public ILIReader iliReader = null;

    static void updateMap (String key, int nr, HashMap<String, PhraseCount> map) {
        if (map.containsKey(key)) {
            PhraseCount cnt = map.get(key);
            cnt.addCount(nr);
            map.put(key, cnt);
        }
        else {
            map.put(key, new PhraseCount(key, nr));
        }
    }
    static void updateMentionMap (String key,  HashMap<String, ArrayList<String>> map, ArrayList<String> mentions) {
        if (map.containsKey(key)) {
            ArrayList<String> mMentions = map.get(key);
            for (int i = 0; i < mentions.size(); i++) {
                String mention = mentions.get(i);
                if (!mMentions.contains(mention)) {
                    mMentions.add(mention);
                    map.put(key, mMentions);
                }
            }
        }
        else {
            map.put(key, mentions);
        }
    }

    static void updateCountMap (String key,  HashMap<String, ArrayList<PhraseCount>> map, ArrayList<String> mentions) {
      //  System.out.println("key = " + key);
      //  System.out.println("mentions.toString() = " + mentions.toString());
        if (map.containsKey(key)) {
            ArrayList<PhraseCount> mMentions = map.get(key);
            for (int i = 0; i < mentions.size(); i++) {
                String mention = mentions.get(i);
                PhraseCount mentionCount = mentionStringToPhraseCount(mention);
                boolean match = false;
                for (int j = 0; j < mMentions.size(); j++) {
                    PhraseCount phraseCount = mMentions.get(j);
                  //  System.out.println("phraseCount.getPhrase() = " + phraseCount.getPhrase());
                  //  System.out.println("mention = " + mention);
                    if (phraseCount.getPhrase().equals(mentionCount.getPhrase())) {
                        phraseCount.addCount(mentionCount.getCount());
                     //   System.out.println("phraseCount.getPhraseCount() = " + phraseCount.getPhraseCount());
                        match = true;
                        break;
                    }
                }
                if (!match) {
                 //   PhraseCount phraseCount = new PhraseCount(mention, 1);
                    mMentions.add(mentionCount);
                }
            }
            map.put(key, mMentions);
        }
        else {
            ArrayList<PhraseCount> mMentions = new ArrayList<PhraseCount>();
            for (int i = 0; i < mentions.size(); i++) {
                String mention = mentions.get(i);
                PhraseCount mentionCount = mentionStringToPhraseCount(mention);
                boolean match = false;
                for (int j = 0; j < mMentions.size(); j++) {
                    PhraseCount phraseCount = mMentions.get(j);
                    if (phraseCount.getPhrase().equals(mentionCount.getPhrase())) {
                        phraseCount.addCount(mentionCount.getCount());
                        match = true;
                        break;
                    }
                }
                if (!match) {
                   // PhraseCount phraseCount = new PhraseCount(mention, 1);
                    mMentions.add(mentionCount);
                }
            }
            map.put(key, mMentions);
        }
    }

    static void dumpSortedMap (HashMap<String, PhraseCount> map, String outputPath) {
        SortedSet<PhraseCount> treeSet = freqSortPhraseCountMap(map);
        try {
            OutputStream fos = new FileOutputStream(outputPath);
            for (PhraseCount pcount : treeSet) {
                String str = "";
                String instanceId = pcount.getPhrase();
                String docId = pcount.getPhrase();
                int idx = instanceId.indexOf("---");
                if (idx>-1) {
                    instanceId = instanceId.substring(0, idx);
                    int idx_s = instanceId.lastIndexOf("/");
                    if (idx_s>-1) instanceId = instanceId.substring(idx_s);
                    docId = docId.substring(idx+3);
                    str = instanceId+"\t"+docId + "\t" + pcount.getCount()+"\n"; /// sum of all phrases

                }
                else {
                    str = "\t"+pcount.getPhrase() + "\t" + pcount.getCount()+"\n"; /// sum of all phrases
                }
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void dumpSortedMap (HashMap<String, ArrayList<String>> mentionMap, HashMap<String, PhraseCount> map, String outputPath) {
        SortedSet<PhraseCount> treeSet = freqSortPhraseCountMap(map);
        try {
            OutputStream fos = new FileOutputStream(outputPath);
            for (PhraseCount pcount : treeSet) {
                String str = pcount.getPhrase()+"\t"+pcount.getCount();
                if (mentionMap.containsKey(pcount.getPhrase())) {
                    ArrayList<String> mentions = mentionMap.get(pcount.getPhrase());
                    for (int i = 0; i < mentions.size(); i++) {
                        String mention = mentions.get(i);
                        str += "\t"+mention;
                    }
                }
                str +="\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    static void dumpSortedCountMap (HashMap<String, ArrayList<PhraseCount>> mentionMap, HashMap<String, PhraseCount> map, String outputPath) {
        SortedSet<PhraseCount> treeSet = freqSortPhraseCountMap(map);
        try {
            OutputStream fos = new FileOutputStream(outputPath);
            for (PhraseCount pcount : treeSet) {
                String str = "";
                String instanceId = pcount.getPhrase();
                String docId = pcount.getPhrase();
                int idx = instanceId.indexOf("---");
                if (idx>-1) {
                    instanceId = instanceId.substring(0, idx);
                    int idx_s = instanceId.lastIndexOf("/");
                    if (idx_s>-1) instanceId = instanceId.substring(idx_s);
                    docId = docId.substring(idx+3);
                    str = instanceId+"\t"+docId + "\t" + pcount.getCount(); /// sum of all phrases

                }
                else {
                    str = "\t"+pcount.getPhrase() + "\t" + pcount.getCount(); /// sum of all phrases
                }
                if (mentionMap.containsKey(pcount.getPhrase())) {
                    ArrayList<PhraseCount> mentions = mentionMap.get(pcount.getPhrase());
                    SortedSet<PhraseCount> mentionSet = freqSortPhraseCountArrayList(mentions);
                    for (PhraseCount cumMention : mentionSet) {
                        str += "\t"+cumMention.getPhraseCount();
                    }
                }
                str +="\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static void dumpTypeMap (HashMap<String, ArrayList<String>> map, HashMap<String, PhraseCount> mentionMap, String outputPath) {
        try {
            OutputStream fos = new FileOutputStream(outputPath);
            Set keySet = map.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                ArrayList<String> types = map.get(key);
                Collections.sort(types);
                Integer cnt = 0;
                if (mentionMap.containsKey(key)) {
                    PhraseCount pcount = mentionMap.get(key);
                    cnt = pcount.getCount();
                }
                String str = key+"\t"+cnt;
                for (int i = 0; i < types.size(); i++) {
                    String type = types.get(i);
                    str += "\t"+type;
                }
                str += "\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static void dumpEventCountTypeMap (HashMap<String, ArrayList<String>> typemap,
                                       HashMap<String, ArrayList<PhraseCount>> instancePhraseMap,
                                       HashMap<String, PhraseCount> countMap, String outputPath) {
        SortedSet<PhraseCount> treeSet = new TreeSet<PhraseCount>(new PhraseCount.Compare());
        Set keySet = countMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            PhraseCount cnt = countMap.get(key);
            treeSet.add(cnt);
        }
        try {
            OutputStream fos = new FileOutputStream(outputPath);
            for (PhraseCount pcount : treeSet) {
                String str = "";
                String instanceId = pcount.getPhrase();
                String docId = pcount.getPhrase();
                int idx = instanceId.indexOf("---");
                if (pcount.getPhrase().isEmpty()) {
                    continue;
                }
                if (idx>-1) {
                    instanceId = instanceId.substring(0, idx);
                    int idx_s = instanceId.lastIndexOf("/");
                    if (idx_s>-1) instanceId = instanceId.substring(idx_s);
                    docId = docId.substring(idx+3);
                    str = instanceId+"\t"+docId + "\t" + pcount.getCount() + "\t"; /// sum of all phrases

                }
                else {
                    str = "\t"+pcount.getPhrase() + "\t" + pcount.getCount() + "\t"; /// sum of all phrases
                }
                // get all phrases
                if (instancePhraseMap.containsKey(pcount.getPhrase())) {
                    ArrayList<PhraseCount> mentions = instancePhraseMap.get(pcount.getPhrase());
                    SortedSet<PhraseCount> mentionSet =  freqSortPhraseCountArrayList(mentions);
                    if (mentionSet.size()==0) {
                        continue;
                    }
                    for (PhraseCount mcount : mentionSet) {
                        str += mcount.getPhraseCount()+";";
                    }
                }
                else {
                    continue;
                }
                str += "\t";
                if (typemap.containsKey(pcount.getPhrase())) {
                    ArrayList<String> types = typemap.get(pcount.getPhrase());
                    String iliString = "";

                    //we get the synonym counts
                    HashMap<String, PhraseCount> synMap = new HashMap<String, PhraseCount>();
                    for (int i = 0; i < types.size(); i++) {
                        String type = types.get(i);
                        if (type.startsWith("ili:")) {
                            iliString += type+";";
                            if (iliReader!=null) {
                                String iliId = type;
                                int idxNS = type.lastIndexOf(":");
                                if (idxNS>-1) {
                                    iliId = type.substring(idxNS+1);
                                    if (iliId.startsWith(":")) System.out.println("iliId = " + iliId);
                                }
                                if (iliReader.iliToSynsetMap.containsKey(iliId)) {
                                    String wnSynset = iliReader.iliToSynsetMap.get(iliId);
                                    if (iliReader.synsetToSynonymMap.containsKey(wnSynset)) {
                                        ArrayList<String> wnSynArray = iliReader.synsetToSynonymMap.get(wnSynset);
                                        for (int j = 0; j < wnSynArray.size(); j++) {
                                            String s = wnSynArray.get(j).trim();
                                            updateMap(s, 1, synMap);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    SortedSet<PhraseCount> sortedSynonyms = freqSortPhraseCountMap(synMap);
                    for (PhraseCount phraseCount : sortedSynonyms) {
                        str += phraseCount.getPhraseCount()+";";
                    }
                    str += "\t";
                    for (int i = 0; i < types.size(); i++) {
                        String type = types.get(i);
                        if (type.startsWith("eso:")) str += type+";";

                    }
                    str += "\t";
                    for (int i = 0; i < types.size(); i++) {
                        String type = types.get(i);
                        if (type.startsWith("fn:")) str += type+";";

                    }
                    str += "\t"+iliString;
                }
                str +="\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Integer countPhrases(ArrayList<PhraseCount> phrases) {
        Integer count = 0;
        for (int i = 0; i < phrases.size(); i++) {
            PhraseCount phraseCount = phrases.get(i);
            count += phraseCount.getCount();
        }
        return count;
    }

    static void dumpLemmaLexiconMap (HashMap<String, ArrayList<String>> typemap,
                                       HashMap<String, ArrayList<PhraseCount>> instancePhraseMap, String outputPath) {
        HashMap<String, ArrayList<String>> lemmaToInstance = new HashMap<String, ArrayList<String>>();
        for (String instance : instancePhraseMap.keySet()) {
            ArrayList<PhraseCount> phrases = instancePhraseMap.get(instance);
            for (int i = 0; i < phrases.size(); i++) {
                PhraseCount phraseCount = phrases.get(i);
                if (lemmaToInstance.containsKey(phraseCount.getPhrase())) {
                    ArrayList<String> instances = lemmaToInstance.get(phraseCount.getPhrase());
                    instances.add(instance);
                    lemmaToInstance.put(phraseCount.getPhrase(), instances);
                }
                else {
                    ArrayList<String> instances = new ArrayList<String>();
                    instances.add(instance);
                    lemmaToInstance.put(phraseCount.getPhrase(), instances);
                }
            }
        }
        try {
            OutputStream fos = new FileOutputStream(outputPath);
            String str = "lemma\tnr. mentions\tlabels\tsupersynset\teso\tframenet\tsynsets\n";
            fos.write(str.getBytes());
            for (String lemma : lemmaToInstance.keySet()) {
                str = lemma+"\t";
                ArrayList<String>  instances = lemmaToInstance.get(lemma);
                ArrayList<PhraseCount> instancePhrases = new ArrayList<PhraseCount>();
                ArrayList<String> instanceTypes = new ArrayList<String>();
                for (int in = 0; in < instances.size(); in++) {
                    String instance = instances.get(in);
                    ArrayList<PhraseCount> phrases = instancePhraseMap.get(instance);
                    instancePhrases.addAll(phrases);
                    ArrayList<String> types = typemap.get(instance);
                    instanceTypes.addAll(types);
                }
                Integer mentionCount = countPhrases(instancePhrases);
                str += mentionCount+"\t";
                SortedSet<PhraseCount> mentionSet =  freqSortPhraseCountArrayList(instancePhrases);
                for (PhraseCount mcount : mentionSet) {
                    str += mcount.getPhraseCount()+";";
                }
                str += "\t";
                 //we get the synonym counts
                HashMap<String, PhraseCount> synMap = new HashMap<String, PhraseCount>();
                HashMap<String, PhraseCount> iliMap = new HashMap<String, PhraseCount>();
                HashMap<String, PhraseCount> esoMap = new HashMap<String, PhraseCount>();
                HashMap<String, PhraseCount> fnMap = new HashMap<String, PhraseCount>();
                for (int i = 0; i < instanceTypes.size(); i++) {
                    String type = instanceTypes.get(i);
                    if (type.startsWith("ili:")) {
                        updateMap(type, 1, iliMap);
                        if (iliReader != null) {
                            String iliId = type;
                            int idxNS = type.lastIndexOf(":");
                            if (idxNS > -1) {
                                iliId = type.substring(idxNS + 1);
                                if (iliId.startsWith(":")) System.out.println("iliId = " + iliId);
                            }
                            if (iliReader.iliToSynsetMap.containsKey(iliId)) {
                                String wnSynset = iliReader.iliToSynsetMap.get(iliId);
                                if (iliReader.synsetToSynonymMap.containsKey(wnSynset)) {
                                    ArrayList<String> wnSynArray = iliReader.synsetToSynonymMap.get(wnSynset);
                                    for (int j = 0; j < wnSynArray.size(); j++) {
                                        String s = wnSynArray.get(j).trim();
                                        updateMap(s, 1, synMap);
                                    }
                                }
                            }
                        }
                    }
                    else if (type.startsWith("eso:")) {
                        updateMap(type, 1, esoMap);
                    }
                    else if (type.startsWith("fn:")) {
                        updateMap(type, 1, fnMap);
                    }
                }
                SortedSet<PhraseCount> sortedSynonyms = freqSortPhraseCountMap(synMap);
                for (PhraseCount phraseCount : sortedSynonyms) {
                    str += phraseCount.getPhraseCount()+";";
                }
                str += "\t";
                SortedSet<PhraseCount> sortedEso = freqSortPhraseCountMap(esoMap);
                for (PhraseCount phraseCount : sortedEso) {
                    str += phraseCount.getPhraseCount()+";";
                }
                str += "\t";
                SortedSet<PhraseCount> sortedFn = freqSortPhraseCountMap(fnMap);
                for (PhraseCount phraseCount : sortedFn) {
                    str += phraseCount.getPhraseCount()+";";
                }
                str += "\t";
                SortedSet<PhraseCount> sortedIli = freqSortPhraseCountMap(iliMap);
                for (PhraseCount phraseCount : sortedIli) {
                    str += phraseCount.getPhraseCount()+";";
                }
                str +="\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void dumpFnLexiconMap (HashMap<String, ArrayList<String>> typemap,
                                       HashMap<String, ArrayList<PhraseCount>> instancePhraseMap, String outputPath) {
        HashMap<String, ArrayList<String>> fnToInstance = new HashMap<String, ArrayList<String>>();
        for (String instance : typemap.keySet()) {
            ArrayList<String> types = typemap.get(instance);
            for (int i = 0; i < types.size(); i++) {
                String type = types.get(i);
                if (type.startsWith("fn:")) {
                    if (fnToInstance.containsKey(type)) {
                        ArrayList<String> instances = fnToInstance.get(type);
                        instances.add(instance);
                        fnToInstance.put(type, instances);
                    }
                    else {
                        ArrayList<String> instances = new ArrayList<String>();
                        instances.add(instance);
                        fnToInstance.put(type, instances);
                    }
                }
            }
        }
        try {
            OutputStream fos = new FileOutputStream(outputPath);
            String str = "frame\tnr. mentions\tlabels\tsupersynset\teso\tframenet\tsynsets\n";
            fos.write(str.getBytes());
            for (String lemma : fnToInstance.keySet()) {
                str = lemma+"\t";
                ArrayList<String>  instances = fnToInstance.get(lemma);
                ArrayList<PhraseCount> instancePhrases = new ArrayList<PhraseCount>();
                ArrayList<String> instanceTypes = new ArrayList<String>();
                for (int in = 0; in < instances.size(); in++) {
                    String instance = instances.get(in);
                    ArrayList<PhraseCount> phrases = instancePhraseMap.get(instance);
                    instancePhrases.addAll(phrases);
                    ArrayList<String> types = typemap.get(instance);
                    instanceTypes.addAll(types);
                }
                Integer mentionCount = countPhrases(instancePhrases);
                str += mentionCount+"\t";
                SortedSet<PhraseCount> mentionSet =  freqSortPhraseCountArrayList(instancePhrases);
                for (PhraseCount mcount : mentionSet) {
                    str += mcount.getPhraseCount()+";";
                }
                str += "\t";
                 //we get the synonym counts
                HashMap<String, PhraseCount> synMap = new HashMap<String, PhraseCount>();
                HashMap<String, PhraseCount> iliMap = new HashMap<String, PhraseCount>();
                HashMap<String, PhraseCount> esoMap = new HashMap<String, PhraseCount>();
                HashMap<String, PhraseCount> fnMap = new HashMap<String, PhraseCount>();
                for (int i = 0; i < instanceTypes.size(); i++) {
                    String type = instanceTypes.get(i);
                    if (type.startsWith("ili:")) {
                        updateMap(type, 1, iliMap);
                        if (iliReader != null) {
                            String iliId = type;
                            int idxNS = type.lastIndexOf(":");
                            if (idxNS > -1) {
                                iliId = type.substring(idxNS + 1);
                                if (iliId.startsWith(":")) System.out.println("iliId = " + iliId);
                            }
                            if (iliReader.iliToSynsetMap.containsKey(iliId)) {
                                String wnSynset = iliReader.iliToSynsetMap.get(iliId);
                                if (iliReader.synsetToSynonymMap.containsKey(wnSynset)) {
                                    ArrayList<String> wnSynArray = iliReader.synsetToSynonymMap.get(wnSynset);
                                    for (int j = 0; j < wnSynArray.size(); j++) {
                                        String s = wnSynArray.get(j).trim();
                                        updateMap(s, 1, synMap);
                                    }
                                }
                            }
                        }
                    }
                    else if (type.startsWith("eso:")) {
                        updateMap(type, 1, esoMap);
                    }
                    else if (type.startsWith("fn:")) {
                        updateMap(type, 1, fnMap);
                    }
                }
                SortedSet<PhraseCount> sortedSynonyms = freqSortPhraseCountMap(synMap);
                for (PhraseCount phraseCount : sortedSynonyms) {
                    str += phraseCount.getPhraseCount()+";";
                }
                str += "\t";
                SortedSet<PhraseCount> sortedEso = freqSortPhraseCountMap(esoMap);
                for (PhraseCount phraseCount : sortedEso) {
                    str += phraseCount.getPhraseCount()+";";
                }
                str += "\t";
                SortedSet<PhraseCount> sortedFn = freqSortPhraseCountMap(fnMap);
                for (PhraseCount phraseCount : sortedFn) {
                    str += phraseCount.getPhraseCount()+";";
                }
                str += "\t";
                SortedSet<PhraseCount> sortedIli = freqSortPhraseCountMap(iliMap);
                for (PhraseCount phraseCount : sortedIli) {
                    str += phraseCount.getPhraseCount()+";";
                }
                str +="\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void dumpEsoLexiconMap (HashMap<String, ArrayList<String>> typemap,
                                       HashMap<String, ArrayList<PhraseCount>> instancePhraseMap, String outputPath) {
        HashMap<String, ArrayList<String>> fnToInstance = new HashMap<String, ArrayList<String>>();
        for (String instance : typemap.keySet()) {
            ArrayList<String> types = typemap.get(instance);
            for (int i = 0; i < types.size(); i++) {
                String type = types.get(i);
                if (type.startsWith("eso:")) {
                    if (fnToInstance.containsKey(type)) {
                        ArrayList<String> instances = fnToInstance.get(type);
                        instances.add(instance);
                        fnToInstance.put(type, instances);
                    }
                    else {
                        ArrayList<String> instances = new ArrayList<String>();
                        instances.add(instance);
                        fnToInstance.put(type, instances);
                    }
                }
            }
        }
        try {
            OutputStream fos = new FileOutputStream(outputPath);
            String str = "frame\tnr. mentions\tlabels\tsupersynset\teso\tframenet\tsynsets\n";
            fos.write(str.getBytes());
            for (String lemma : fnToInstance.keySet()) {
                str = lemma+"\t";
                ArrayList<String>  instances = fnToInstance.get(lemma);
                ArrayList<PhraseCount> instancePhrases = new ArrayList<PhraseCount>();
                ArrayList<String> instanceTypes = new ArrayList<String>();
                for (int in = 0; in < instances.size(); in++) {
                    String instance = instances.get(in);
                    ArrayList<PhraseCount> phrases = instancePhraseMap.get(instance);
                    instancePhrases.addAll(phrases);
                    ArrayList<String> types = typemap.get(instance);
                    instanceTypes.addAll(types);
                }
                Integer mentionCount = countPhrases(instancePhrases);
                str += mentionCount+"\t";
                SortedSet<PhraseCount> mentionSet =  freqSortPhraseCountArrayList(instancePhrases);
                for (PhraseCount mcount : mentionSet) {
                    str += mcount.getPhraseCount()+";";
                }
                str += "\t";
                 //we get the synonym counts
                HashMap<String, PhraseCount> synMap = new HashMap<String, PhraseCount>();
                HashMap<String, PhraseCount> iliMap = new HashMap<String, PhraseCount>();
                HashMap<String, PhraseCount> esoMap = new HashMap<String, PhraseCount>();
                HashMap<String, PhraseCount> fnMap = new HashMap<String, PhraseCount>();
                for (int i = 0; i < instanceTypes.size(); i++) {
                    String type = instanceTypes.get(i);
                    if (type.startsWith("ili:")) {
                        updateMap(type, 1, iliMap);
                        if (iliReader != null) {
                            String iliId = type;
                            int idxNS = type.lastIndexOf(":");
                            if (idxNS > -1) {
                                iliId = type.substring(idxNS + 1);
                                if (iliId.startsWith(":")) System.out.println("iliId = " + iliId);
                            }
                            if (iliReader.iliToSynsetMap.containsKey(iliId)) {
                                String wnSynset = iliReader.iliToSynsetMap.get(iliId);
                                if (iliReader.synsetToSynonymMap.containsKey(wnSynset)) {
                                    ArrayList<String> wnSynArray = iliReader.synsetToSynonymMap.get(wnSynset);
                                    for (int j = 0; j < wnSynArray.size(); j++) {
                                        String s = wnSynArray.get(j).trim();
                                        updateMap(s, 1, synMap);
                                    }
                                }
                            }
                        }
                    }
                    else if (type.startsWith("eso:")) {
                        updateMap(type, 1, esoMap);
                    }
                    else if (type.startsWith("fn:")) {
                        updateMap(type, 1, fnMap);
                    }
                }
                SortedSet<PhraseCount> sortedSynonyms = freqSortPhraseCountMap(synMap);
                for (PhraseCount phraseCount : sortedSynonyms) {
                    str += phraseCount.getPhraseCount()+";";
                }
                str += "\t";
                SortedSet<PhraseCount> sortedEso = freqSortPhraseCountMap(esoMap);
                for (PhraseCount phraseCount : sortedEso) {
                    str += phraseCount.getPhraseCount()+";";
                }
                str += "\t";
                SortedSet<PhraseCount> sortedFn = freqSortPhraseCountMap(fnMap);
                for (PhraseCount phraseCount : sortedFn) {
                    str += phraseCount.getPhraseCount()+";";
                }
                str += "\t";
                SortedSet<PhraseCount> sortedIli = freqSortPhraseCountMap(iliMap);
                for (PhraseCount phraseCount : sortedIli) {
                    str += phraseCount.getPhraseCount()+";";
                }
                str +="\n";
                fos.write(str.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void main (String[] args) {
        String folderpath = "";
        String type = ""; //"instance";
        STAT= ""; //"event";
        int n = 0;
        folderpath = "";
        folderpath="/Users/piek/Desktop/NNIP/2005-01-18/S-1/A/";
        STAT="event";
        type ="instance";
                //"/Users/piek/Desktop/CLTL-onderwijs/EnvironmentalAndDigitalHumanities/london/ob.trig";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--trig-folder") && args.length>(i+1)) {
                folderpath = args[i+1];
            }
            else if (arg.equals("--type") && args.length>(i+1)) {
                type = args[i+1];
            }
            else if (arg.equals("--n") && args.length>(i+1)) {
                n = Integer.parseInt(args[i+1]);
            }
            else if (arg.equals("--stat") && args.length>(i+1)) {
                STAT = args[i+1];
            }

            else if (arg.equals("--ili") && args.length > (i + 1)) {
                String pathToILIFile = args[i+1];
                iliReader = new ILIReader();
                iliReader.readILIFile(pathToILIFile);
            }
        }
        File inputFolder = new File(folderpath);
        System.out.println("inputFolder = " + inputFolder);
        ArrayList<File> files = Util.makeRecursiveFileList(inputFolder, ".trig");
        System.out.println(".trig files size() = " + files.size());
        if (type.equals("instance")) {
            processInstances(inputFolder, files, n);
            outputInstances(inputFolder);
        }
        if (type.equals("grasp")) processGrasp(inputFolder, files);
    }

    static void outputInstances (File inputFolder) {
        File folderParent = inputFolder.getParentFile();
        String outputFile = "";

        if (STAT.isEmpty() || STAT.equals("en")) {
            outputFile = folderParent.getAbsolutePath() + "/" + inputFolder.getName() + ".entities.xls";
            // dumpSortedMap(darkEntityMap, enMap, outputFile);
            dumpSortedCountMap(darkEntityPhraseCountMap, enMap, outputFile);

        }
        if (STAT.isEmpty() || STAT.equals("ne")) {
            outputFile = folderParent.getAbsolutePath() + "/" + inputFolder.getName() + ".nonentities.xls";
            dumpSortedCountMap(nonEntityPhraseCountMap, neMap, outputFile);
        }
        if (STAT.isEmpty() || STAT.equals("dbp")) {
            outputFile = folderParent.getAbsolutePath() + "/" + inputFolder.getName() + ".dbp.xls";
            //   dumpSortedMap(lightEntityMap, dbpMap, outputFile);
            dumpSortedCountMap(lightEntityPhraseCountMap, dbpMap, outputFile);
        }
        if (STAT.isEmpty() || STAT.equals("event")) {
            outputFile = folderParent.getAbsolutePath() + "/" + inputFolder.getName() + ".eventlabels.xls";
            dumpSortedMap(eventLabelMap, outputFile);
            outputFile = folderParent.getAbsolutePath() + "/" + inputFolder.getName() + ".eventlabelsNOeso.xls";
            dumpSortedMap(eventLabelWithoutESOMap, outputFile);
            outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".eventlabelsNOfn.xls";
            dumpSortedMap(eventLabelWithoutFNMap, outputFile);
            outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".eventlabelsNOili.xls";
            dumpSortedMap(eventLabelWithoutILIMap, outputFile);
            outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".eso.xls";
            dumpSortedMap(esoMap, outputFile);
            outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".fn.xls";
            dumpSortedMap(fnMap, outputFile);
            outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".ili.xls";
            dumpSortedMap(iliMap, outputFile);
            outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".event.xls";
            dumpEventCountTypeMap(eventTypeMap, eventPhraseCountMap, evMap, outputFile);
            outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".event-lemma-lex.xls";
            dumpLemmaLexiconMap(eventTypeMap, eventPhraseCountMap, outputFile);
            outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".event-frame-lex.xls";
            dumpFnLexiconMap(eventTypeMap, eventPhraseCountMap, outputFile);
            outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".event-eso-lex.xls";
            dumpEsoLexiconMap(eventTypeMap, eventPhraseCountMap, outputFile);

            // outputFile = folderParent.getAbsolutePath() + "/" + inputFolder.getName() + ".eventInstancelabels.xls";
            // dumpSortedCountMap(eventPhraseCountMap, eventTypeMap, outputFile);

        }
        if (STAT.isEmpty() || STAT.equals("topic")) {
            outputFile = folderParent.getAbsolutePath() + "/" + inputFolder.getName() + ".topics.xls";
            dumpSortedMap(topicMap, outputFile);
        }
    }

    static void processInstances (File inputFolder, ArrayList<File> files, int n) {

        TrigTripleData trigTripleData = TrigTripleReader.readInstanceTripleFromTrigFiles(STAT, files, n);
        Set keySet = trigTripleData.tripleMapInstances.keySet();
        //System.out.println("trigTripleData.tripleMapInstances.size() = " + trigTripleData.tripleMapInstances.size());
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next(); //// this is the subject of the triple which should point to an event
            ArrayList<Statement> instanceTriples = trigTripleData.tripleMapInstances.get(key);
            int m = countMentions(instanceTriples);
            String type = getInstanceType(key);
           // System.out.println("type = " + type);
            if (STAT.isEmpty() || STAT.equals("dbp")) {
                if (type.equalsIgnoreCase("dbp") || type.toLowerCase().endsWith(".dbp")) {
                    updateMap(key, m, dbpMap);
                    //ArrayList<String> mentionLabels = getLabelsFromInstanceStatement(instanceTriples);
                    ArrayList<String> mentionLabels = getPhraseCountFromInstanceStatement(instanceTriples, trigTripleData);
                    //updateMentionMap(key, lightEntityMap, mentionLabels);
                    updateCountMap(key, lightEntityPhraseCountMap, mentionLabels);

                }
            }
            if (STAT.isEmpty() || STAT.equals("en")) {
                if (type.equalsIgnoreCase("en")) {
                    updateMap(key, m, enMap);
                    //ArrayList<String> mentionLabels = getLabelsFromInstanceStatement(instanceTriples);
                    ArrayList<String> mentionLabels = getPhraseCountFromInstanceStatement(instanceTriples, trigTripleData);
                    //updateMentionMap(key, darkEntityMap, mentionLabels);
                    updateCountMap(key, darkEntityPhraseCountMap, mentionLabels);

                }
            }

            if (STAT.isEmpty() || STAT.equals("ne")) {
                if (type.equalsIgnoreCase("ne")) {
                    updateMap(key, m, neMap);
                   // ArrayList<String> types = getSkosRelatedValuesFromInstanceStatement(instanceTriples);
                   // updateCountMap(key, nonEntityPhraseCountMap, types);
                    //ArrayList<String> mentionLabels = getLabelsFromInstanceStatement(instanceTriples);
                    ArrayList<String> mentionLabels = getPhraseCountFromInstanceStatement(instanceTriples, trigTripleData);
                    updateCountMap(key, nonEntityPhraseCountMap, mentionLabels);
                }
            }
            if (STAT.isEmpty() || STAT.equals("event")) {
                if (key.indexOf("#ev") > -1) {
                    // counting types
                    ArrayList<String> eso = getTypeValuesFromInstanceStatement(instanceTriples, "eso");
                    ArrayList<String> fn = getTypeValuesFromInstanceStatement(instanceTriples, "fn");
                    ArrayList<String> ili = getTypeValuesFromInstanceStatement(instanceTriples, "ili");
                    for (int i = 0; i < eso.size(); i++) {
                        String s = eso.get(i);
                        updateMap(s, m, esoMap);
                    }
                    for (int i = 0; i < fn.size(); i++) {
                        String s = fn.get(i);
                        updateMap(s, m, fnMap);
                    }
                    for (int i = 0; i < ili.size(); i++) {
                        String s = ili.get(i);
                        updateMap(s, m, iliMap);
                    }
                    ArrayList<String> labels = getLabelsFromInstanceStatement(instanceTriples,trigTripleData);
                    if (labels.isEmpty()) {
                        labels = getLabelsFromInstanceStatement(instanceTriples);
                    }
                    /// we first build the data for each event instance
                    ArrayList<String> mentionLabels = getPhraseCountFromInstanceStatement(instanceTriples, trigTripleData);
                    if (mentionLabels.isEmpty()) {
                       mentionLabels = getLabelsFromInstanceStatement(instanceTriples);
                    }
                    updateCountMap(key, eventPhraseCountMap, mentionLabels);
                    updateMap(key, m, evMap);
                   // System.out.println("mentionLabels.toString() = " + mentionLabels.toString());

                    /// now we get the data for labels and types
                    for (int i = 0; i < labels.size(); i++) {
                        String s = labels.get(i).trim();
                        if (!s.isEmpty()) {
                            updateMap(s, m, eventLabelMap);
                            if (ili.size() == 0) {
                                updateMap(s, m, eventLabelWithoutILIMap);
                            }
                            if (eso.size() == 0) {
                                updateMap(s, m, eventLabelWithoutESOMap);
                            }
                            if (fn.size() == 0) {
                                updateMap(s, m, eventLabelWithoutFNMap);
                            }
                            ArrayList<String> types = new ArrayList<String>();
                            if (eventTypeMap.containsKey(s)) {
                                types = eventTypeMap.get(s);
                            }
                            //// add the types in order
                            for (int j = 0; j < ili.size(); j++) {
                                String s1 = ili.get(j);
                                if (!types.contains(s1)) {
                                    types.add(s1);
                                }
                            }
                            for (int j = 0; j < eso.size(); j++) {
                                String s1 = eso.get(j);
                                if (!types.contains(s1)) {
                                    types.add(s1);
                                }
                            }
                            for (int j = 0; j < fn.size(); j++) {
                                String s1 = fn.get(j);
                                if (!types.contains(s1)) {
                                    types.add(s1);
                                }
                            }
                            Collections.sort(types);
                            eventTypeMap.put(key, types);
                        }
                    }
                }
            }
            if (STAT.isEmpty() || STAT.equals("topic")) {
                if (key.indexOf("#ev") > -1) {
                    ///this is an event
                    ArrayList<String> skos = getSkosValuesFromInstanceStatement(instanceTriples);
                    for (int i = 0; i < skos.size(); i++) {
                        String s = skos.get(i);
                        updateMap(s, m, topicMap);
                    }
                }
            }
        }
    }


    static SortedSet<PhraseCount> freqSortPhraseCountArrayList(ArrayList<PhraseCount> counts) {
        SortedSet<PhraseCount> treeSet = new TreeSet<PhraseCount>(new PhraseCount.Compare());
        for (PhraseCount phraseCount : counts) {
            boolean match = false;
            for (PhraseCount cumMention : treeSet) {
                if (phraseCount.getPhrase().equals(cumMention.getPhrase())) {
                    cumMention.addCount(phraseCount.getCount());
                    match = true;
                    break;
                }
            }
            if (!match) {
                treeSet.add(phraseCount);
            }
        }
        return treeSet;
    }

    static SortedSet<PhraseCount> freqSortPhraseCountMap(HashMap<String, PhraseCount> countMap) {
        SortedSet<PhraseCount> treeSet = new TreeSet<PhraseCount>(new PhraseCount.Compare());
        Set keySet = countMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            PhraseCount cnt = countMap.get(key);
            boolean match = false;
            for (PhraseCount phraseCount : treeSet) {
                if (phraseCount.getPhrase().equals(cnt.getPhrase())) {
                    phraseCount.addCount(cnt.getCount());
                    match = true;
                    break;
                }
            }
            if (!match) {
                treeSet.add(cnt);
            }
        }
        return treeSet;
    }

    static PhraseCount mentionStringToPhraseCount (String mentionString) {
        PhraseCount p = new PhraseCount(mentionString, 1);
        int idx = mentionString.lastIndexOf(":");
        if (idx>-1) {
            String nr = mentionString.substring(idx+1);
            Integer cnt = null;
            try {
                cnt = Integer.parseInt(nr);
                p.setPhrase(mentionString.substring(0, idx));
                p.setCount(cnt);
            } catch (NumberFormatException e) {
                System.out.println("mentionString = " + mentionString);
                // e.printStackTrace();
            }
        }
        return p;
    }

    static void processGrasp (File inputFolder, ArrayList<File> files) {
        TrigTripleData trigTripleData = TrigTripleReader.readGraspTripleFromTrigFiles(files);
        getSourceStats(trigTripleData);
        normaliseMap(citeMap);
        File folderParent = inputFolder.getParentFile();
        String outputFile = "";
        outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".cited.xls";
        dumpSortedMap(citeMap, outputFile);
        outputFile = folderParent.getAbsolutePath()+"/"+inputFolder.getName()+".author.xls";
        dumpSortedMap(authMap, outputFile);
    }

    static int countMentions (ArrayList<Statement> statements) {
        int n = 0;
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            String predicate = statement.getPredicate().getURI();
            if (predicate.endsWith("#denotedBy")) {
                n++;
            }
        }
        return n;
    }

    static String getInstanceType (String key) {
        String type = JsonFromRdf.getNameSpaceString(key);
        if (type.isEmpty()) {
            type = "event";
        }
        return type;
    }

    static ArrayList<String> getTypeValuesFromInstanceStatement (ArrayList<Statement> statements, String type) {
        ArrayList<String> result = new ArrayList<String>();
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
                String[] values = object.split(",");
                for (int j = 0; j < values.length; j++) {
                    String value = values[j];
                    String property = JsonFromRdf.getNameSpaceString(value);
                    if (property.equalsIgnoreCase(type)) {
                        value = property + ":" + getValue(value);
                        if (!result.contains(value)) {
                            result.add(value);
                        }
                    }
                }
            }
        }
        return result;
    }

    static ArrayList<String> getSkosValuesFromInstanceStatement (ArrayList<Statement> statements) {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            String predicate = statement.getPredicate().getURI();
            if (predicate.endsWith("#relatedMatch")) {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String[] values = object.split(",");
                for (int j = 0; j < values.length; j++) {
                    String value = values[j];
                    String property = JsonFromRdf.getNameSpaceString(value);
                    value = property + ":" + getValue(value);
                    if (!result.contains(value)) {
                        result.add(value);
                    }
                }
            }
        }
        return result;
    }

    static ArrayList<String> getSkosRelatedValuesFromInstanceStatement (ArrayList<Statement> statements) {
            ArrayList<String> result = new ArrayList<String>();
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                String predicate = statement.getPredicate().getURI();
                if (predicate.endsWith("#relatedMatch")) {
                    String object = "";
                    if (statement.getObject().isLiteral()) {
                        object = statement.getObject().asLiteral().toString();
                    } else if (statement.getObject().isURIResource()) {
                        object = statement.getObject().asResource().getURI();
                    }
                    String[] values = object.split(",");
                    for (int j = 0; j < values.length; j++) {
                        String value = values[j];
                        String property = JsonFromRdf.getNameSpaceString(value);
                        value = property + ":" + getValue(value);
                        if (!result.contains(value)) {
                            result.add(value);
                        }
                    }
                }
            }
            return result;
    }


    /**
     *     <http://cltl.nl/old_bailey/sessionpaper/t19130304-29#ev1>
     a                  sem:Event , nwrontology:sourceEvent , ili:i25498 , ili:i26089 , ili:i26660 , ili:i25493 , fn:Request ;
     gaf:denotedBy      <http://cltl.nl/old_bailey/sessionpaper/t19130304-29#char=32,39> ;
     nwr:phrasecount    <http://cltl.nl/old_bailey/sessionpaper/t19130304-29#ev1#0> ;
     skos:prefLabel     "plead" ;
     skos:relatedMatch  <http://eurovoc.europa.eu/1474> , <http://eurovoc.europa.eu/1810> , <http://eurovoc.europa.eu/1460> , <http://eurovoc.europa.eu/5138> , <http://eurovoc.europa.eu/3821> , <http://eurovoc.europa.eu/2734> .

     <http://cltl.nl/old_bailey/sessionpaper/t19130304-29#ev1#0>
     rdfs:label  "plead" ;
     nwr:count   1 .
     *
     *
     * @param statements
     * @return
     */
    static ArrayList<String>  getLabelsFromInstanceStatement (ArrayList<Statement> statements, TrigTripleData trigTripleData) {
            ArrayList<String> result = new ArrayList<String>();
            ArrayList<String> phraseCountObjects = new ArrayList<String>();
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                String predicate = statement.getPredicate().getLocalName();
                if (predicate.equals("phrasecount")) {
                    String object = "";
                    //System.out.println("statement = " + statement.toString());
                    if (statement.getObject().isLiteral()) {
                        object = statement.getObject().asLiteral().toString();
                    } else if (statement.getObject().isURIResource()) {
                        object = statement.getObject().asResource().getURI();
                    }
                    String [] values = object.split(",");
                    for (int j = 0; j < values.length; j++) {
                        String value = values[j].trim();
                        if (!value.isEmpty()) {
                            if (!phraseCountObjects.contains(value)) {
                                phraseCountObjects.add(value);
                            }
                        }
                    }
                }
            }
      //  System.out.println("phraseCountObjects.toString() = " + phraseCountObjects.toString());
        for (int p = 0; p < phraseCountObjects.size(); p++) {
            String s = phraseCountObjects.get(p);
            String label = "";
            if (trigTripleData.tripleMapLabels.containsKey(s)) {
                ArrayList<Statement> phraseCountTriples = trigTripleData.tripleMapLabels.get(s);
                for (int i = 0; i < phraseCountTriples.size(); i++) {
                    Statement statement = phraseCountTriples.get(i);
                    String predicate = statement.getPredicate().getLocalName();
                    if (predicate.equals("label")) {
                        String object = "";
                        if (statement.getObject().isLiteral()) {
                            object = statement.getObject().asLiteral().toString();
                        } else if (statement.getObject().isURIResource()) {
                            object = statement.getObject().asResource().getURI();
                        }
                        String[] values = object.split(",");
                        for (int j = 0; j < values.length; j++) {
                            String value = values[j].trim();
                            if (!value.isEmpty()) {
                                label = value;
                            }
                        }
                    }
                }
                if (!label.isEmpty()) {
                        result.add(label);
                }
            }
            else {
               // System.out.println("cannot find s = " + s);
            }
        }
        return result;
    }

    static ArrayList<String>  getPhraseCountFromInstanceStatement (ArrayList<Statement> statements, TrigTripleData trigTripleData) {
            ArrayList<String> result = new ArrayList<String>();
            ArrayList<String> phraseCountObjects = new ArrayList<String>();
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                String predicate = statement.getPredicate().getLocalName();
                if (predicate.equals("phrasecount")) {
                    String object = "";
                    //System.out.println("statement = " + statement.toString());
                    if (statement.getObject().isLiteral()) {
                        object = statement.getObject().asLiteral().toString();
                    } else if (statement.getObject().isURIResource()) {
                        object = statement.getObject().asResource().getURI();
                    }
                    String [] values = object.split(",");
                    for (int j = 0; j < values.length; j++) {
                        String value = values[j].trim();
                        if (!value.isEmpty()) {
                            if (!phraseCountObjects.contains(value)) {
                                phraseCountObjects.add(value);
                            }
                        }
                    }
                }
            }
      //  System.out.println("phraseCountObjects.toString() = " + phraseCountObjects.toString());
        for (int p = 0; p < phraseCountObjects.size(); p++) {
            String s = phraseCountObjects.get(p);
            String label = "";
            String count = "";
            if (trigTripleData.tripleMapLabels.containsKey(s)) {
                ArrayList<Statement> phraseCountTriples = trigTripleData.tripleMapLabels.get(s);
                for (int i = 0; i < phraseCountTriples.size(); i++) {
                    Statement statement = phraseCountTriples.get(i);
                    String predicate = statement.getPredicate().getLocalName();
                    if (predicate.equals("label")) {
                        String object = "";
                        if (statement.getObject().isLiteral()) {
                            object = statement.getObject().asLiteral().toString();
                        } else if (statement.getObject().isURIResource()) {
                            object = statement.getObject().asResource().getURI();
                        }
                        String[] values = object.split(",");
                        for (int j = 0; j < values.length; j++) {
                            String value = values[j].trim();
                            if (!value.isEmpty()) {
                                label = value;
                            }
                        }
                    }
                    else if (predicate.equals("count")) {
                        String object = "";
                        if (statement.getObject().isLiteral()) {
                            object = statement.getObject().asLiteral().toString();
                        } else if (statement.getObject().isURIResource()) {
                            object = statement.getObject().asResource().getURI();
                        }
                        String[] values = object.split(",");
                        for (int j = 0; j < values.length; j++) {
                            String value = values[j].trim();
                            if (!value.isEmpty()) {
                                int idx = value.indexOf("^^");
                                count = value.substring(0, idx);
                            }
                        }
                    }
                }
                if (!label.isEmpty()) {
                    if (!count.isEmpty()) {
                        label += ":"+count;
                        result.add(label);
                    }
                    else {
                        label += ":"+"0";
                        result.add(label);
                    }
                }
            }
            else {
               // System.out.println("cannot find s = " + s);
            }
        }
        return result;
    }

    static ArrayList<String>  getLabelsFromInstanceStatement (ArrayList<Statement> statements) {
            ArrayList<String> result = new ArrayList<String>();
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                //System.out.println("statement = " + statement);
                String predicate = statement.getPredicate().getURI();
                if (predicate.endsWith("#label") || predicate.endsWith("#prefLabel")) {
                    String object = "";
                    if (statement.getObject().isLiteral()) {
                        object = statement.getObject().asLiteral().toString();
                    } else if (statement.getObject().isURIResource()) {
                        object = statement.getObject().asResource().getURI();
                    }
                    String [] values = object.split(",");
                    for (int j = 0; j < values.length; j++) {
                        String value = values[j].trim();
                        if (!value.isEmpty()) {
                            //if (value.indexOf(":")==-1) value +=":"+countMentions; /// make it backward compatible
                            if (!result.contains(value)) {
                                result.add(value);
                            }
                        }
                    }
                }
            }
            return result;
    }



    public static void getSourceStats(TrigTripleData trigTripleData) {
        Set keySet = trigTripleData.tripleMapGrasp.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            ArrayList<Statement> perspectiveTriples = trigTripleData.tripleMapGrasp.get(key);
            for (int i = 0; i < perspectiveTriples.size(); i++) {
                Statement statement = perspectiveTriples.get(i);
                String predicate = statement.getPredicate().getURI();
                String object = statement.getObject().toString();
                if (predicate.endsWith("#hasAttribution")) {
                    if (trigTripleData.tripleMapGrasp.containsKey(object)) {
                        ArrayList<Statement> perspectiveValues = trigTripleData.tripleMapGrasp.get(object);
                        for (int j = 0; j < perspectiveValues.size(); j++) {
                            Statement statement1 = perspectiveValues.get(j);
                            if (statement1.getPredicate().getURI().endsWith("#wasAttributedTo")) {
                                if (trigTripleData.tripleMapGrasp.containsKey(statement1.getObject().toString())) {
                                    //// this means the source has properties so it is likely to be the document with an author
                                    ArrayList<Statement> provStatements = trigTripleData.tripleMapGrasp.get(statement1.getObject().toString());
                                    for (int k = 0; k < provStatements.size(); k++) {
                                        Statement statement2 = provStatements.get(k);
                                        String authorString = statement2.getObject().toString();
                                        int idx = authorString.lastIndexOf("/");
                                        if (idx > -1) {
                                            authorString = authorString.substring(idx + 1);
                                        }
                                       // System.out.println("authorString = " + authorString);
                                        ArrayList<String> authorAnd = Util.splitSubstring(authorString, "+and+");
                                        for (int l = 0; l < authorAnd.size(); l++) {
                                            String subauthor = authorAnd.get(l);
                                            ArrayList<String> subauthorfields= Util.splitSubstring(subauthor, "%2C+");
                                            for (int m = 0; m < subauthorfields.size(); m++) {
                                                String subfield = subauthorfields.get(m);
                                               // System.out.println("subfield = " + subfield);
                                                if (!subfield.toLowerCase().endsWith("correspondent")
                                                        && !subfield.toLowerCase().endsWith("reporter")
                                                        && !subfield.toLowerCase().endsWith("editor")
                                                        && !subfield.toLowerCase().startsWith("in+")
                                                        ) {
                                                    String author = JsonStoryUtil.normalizeSourceValue(subfield);
                                                    author =  JsonStoryUtil.cleanAuthor(author);
                                                    updateMap(author, 1, authMap);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    //// it is not the document so a cited source
                                    String cite = statement1.getObject().toString();
                                    int idx = cite.lastIndexOf("/");
                                    if (idx > -1) {
                                        cite = cite.substring(idx + 1);
                                    }
/*                                  THIS DOES NOT WORK: PRONOUNS, RECEPTIONIST, ETC...
                                    //// There can be source documents without meta data.
                                    //// In that case, there are no triples for in tripleMapGrasp with this subject but it is still a document
                                    //// The next hack checks for upper case characters in the URI
                                    //// If they are present, we assume it is somebody otherwise we assume it is a document and we assign it to the meta string

                                    if (cite.toLowerCase().equals(cite)) {
                                        //// no uppercase characters
                                        cite = meta;
                                    }
*/
                                    if (cite.toLowerCase().equals(cite)) {
                                        //// no uppercase characters
                                        cite = "someone";
                                    }
                                    cite= JsonStoryUtil.normalizeSourceValue(cite);
                                    cite = JsonStoryUtil.cleanCite(cite);
                                    updateMap(cite, 1, citeMap);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static public HashMap<String, String> getFullNameMap (HashMap<String, PhraseCount> map) {
        HashMap<String, String> fullNameMap = new HashMap<String, String>();
        Vector<String> singleNames = new Vector<String>();
        Set keySet = map.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            String [] fields = key.split("_");
            if (fields.length==1) {
                String lastName = Util.getCamelName(key);
                if (lastName.isEmpty()) {
                    singleNames.add(key);
                }
            }
        }
        System.out.println("singleNames = " + singleNames.toString());
        /// we now have last name only entries
        /// we iterate again
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            String [] fields = key.split("_");
            if (fields.length>1) {
                String lastField = fields[fields.length-1];
                if (singleNames.contains(lastField)) {
                    fullNameMap.put(lastField, key);
                }
            }
            else {
                String lastName = Util.getCamelName(key);
                if (!lastName.isEmpty()) {
                    if (singleNames.contains(lastName)) {
                        fullNameMap.put(lastName, key);
                    }
                }
            }
        }
        return fullNameMap;
    }

    static public void normaliseMap (HashMap<String, PhraseCount> map) {
        System.out.println("map.size() = " + map.size());
        HashMap<String, PhraseCount> newMap = new HashMap<String, PhraseCount>();
        Vector<String> normalisedKey = new Vector<String>();
        HashMap<String, String> fullNameMap = getFullNameMap(map);
        Set keySet = map.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            PhraseCount phraseCount = map.get(key);
            if (fullNameMap.containsKey(key)) {
                String fullName = fullNameMap.get(key);
                PhraseCount fullPhraseCount = map.get(fullName);
                fullPhraseCount.addCount(phraseCount.getCount());
                map.put(fullName, fullPhraseCount);
                normalisedKey.add(key);
            }
        }
        /// we now have last name only entries
        /// we iterate again
        keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            PhraseCount phraseCount = map.get(key);
            if (!normalisedKey.contains(key)) {
                newMap.put(key, phraseCount);
            }
        }
        System.out.println("newMap.size() = " + newMap.size());
        map = newMap;
    }

}
