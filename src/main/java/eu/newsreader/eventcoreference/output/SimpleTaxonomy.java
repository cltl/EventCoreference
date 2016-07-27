package eu.newsreader.eventcoreference.output;

import eu.newsreader.eventcoreference.objects.PhraseCount;
import eu.newsreader.eventcoreference.util.TreeStaticHtml;
import org.apache.tools.bzip2.CBZip2InputStream;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by piek on 29/05/16.
 */
public class SimpleTaxonomy {

    public HashMap<String, String> subToSuper = new HashMap<String, String>();
    public HashMap<String, ArrayList<String>> superToSub = new HashMap<String, ArrayList<String>>();


    public SimpleTaxonomy () {
        subToSuper = new HashMap<String, String>();
        superToSub = new HashMap<String, ArrayList<String>>();
    }

    public void readSimpleTaxonomyFromDbpFile (String filePath) {
        try {
            InputStreamReader isr = null;
            if (filePath.toLowerCase().endsWith(".gz")) {
                try {
                    InputStream fileStream = new FileInputStream(filePath);
                    InputStream gzipStream = new GZIPInputStream(fileStream);
                    isr = new InputStreamReader(gzipStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (filePath.toLowerCase().endsWith(".bz2")) {
                try {
                    InputStream fileStream = new FileInputStream(filePath);
                    InputStream gzipStream = new CBZip2InputStream(fileStream);
                    isr = new InputStreamReader(gzipStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                FileInputStream fis = new FileInputStream(filePath);
                isr = new InputStreamReader(fis);
            }
            if (isr!=null) {
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready() && (inputLine = in.readLine()) != null) {
                    // System.out.println(inputLine);
                    inputLine = inputLine.trim();
                    if (inputLine.trim().length() > 0) {
                             /*
                             	Colour	9
	Currency	189
	Disease	8
	EthnicGroup	2
	Holiday	15
	Language	106
    Agent	Agent	9722
Agent	Family	Family	99
Agent	Organisation	Broadcaster	BroadcastNetwork	1
Agent	Organisation	Broadcaster	Broadcaster	15

www.w3.org/2002/07/owl#Thing	Agent	Person	Monarch
www.w3.org/2002/07/owl#Thing	Agent	Person	MovieDirector
www.w3.org/2002/07/owl#Thing	Agent	Person	Noble
www.w3.org/2002/07/owl#Thing	Agent	Person	OfficeHolder
www.w3.org/2002/07/owl#Thing	Agent	Person	OrganisationMember	SportsTeamMember
www.w3.org/2002/07/owl#Thing	Agent	Person	Orphan
www.w3.org/2002/07/owl#Thing	Agent	Person	Philosopher
     */
                        String[] fields = inputLine.split("\t");
                        if (fields.length > 1) {
                            for (int i = 0; i < fields.length-1; i++) {
                                String subClass = "dbp:"+fields[i+1];
                                Integer cnt = -1;
                                try {
                                    cnt = Integer.parseInt(subClass);
                                } catch (NumberFormatException e) {
                                   // e.printStackTrace();
                                    //So only if fields[i+1] is not a count!
                                    String superClass = "dbp:"+fields[i];
                                    //System.out.println("subClass = " + subClass);
                                    //System.out.println("superClass = " + superClass);
                                    if (!subClass.equals(superClass)) {
                                        subToSuper.put(subClass, superClass);
                                        if (superToSub.containsKey(superClass)) {
                                            ArrayList<String> subs = superToSub.get(superClass);
                                            if (!subs.contains(subClass)) {
                                                subs.add(subClass);
                                                superToSub.put(superClass, subs);
                                            }
                                        }
                                        else {
                                            ArrayList<String> subs = new ArrayList<String>();
                                            subs.add(subClass);
                                            superToSub.put(superClass, subs);
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    public ArrayList<String> getTops () {
        ArrayList<String> tops = new ArrayList<String>();
        Set keySet = superToSub.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!key.equals("eso:SituationRuleAssertion")) {
                if (!subToSuper.containsKey(key)) {
                    if (!tops.contains(key)) tops.add(key);
                }
            }
        }
        return tops;
    }

    public void getParentChain (String c, ArrayList<String> parents) {
        if (subToSuper.containsKey(c)) {
            String p = subToSuper.get(c);
            if (!parents.contains(p)) {
                parents.add(p);
                getParentChain(p, parents);
            }
        }
    }


    public void getDescendants (String c, ArrayList<String> decendants) {
        if (superToSub.containsKey(c)) {
            ArrayList<String> subs = superToSub.get(c);
            for (int i = 0; i < subs.size(); i++) {
                String sub = subs.get(i);
                if (!decendants.contains(sub)) {
                    decendants.add(sub);
                    getDescendants(sub, decendants);
                }
            }
        }
    }

    public String getMostSpecificChild (ArrayList<String> types) {
        String child = "";
        if (types.size()==1) {
            child = types.get(0);
        }
        else {
            ArrayList<String> parents = new ArrayList<String>();
            for (int i = 0; i < types.size(); i++) {
                String t = types.get(i);
                if (subToSuper.containsKey(t)) {
                    for (int j = 0; j < types.size(); j++) {
                        if (j!=i) {
                            String t2 = types.get(j);
                            if (subToSuper.get(t).equals(t2)) {
                                parents.add(t2);
                                if (!parents.contains(t)) {
                                    child = t;
                                }
                            }
                        }
                    }
                }
            }
        }
        return child;
    }


    public void printTree (ArrayList<String> tops, int level) {
        level++;
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            String str = "";
            for (int j = 0; j < level; j++) {
                str += "  ";

            }
            if (superToSub.containsKey(top)) {
                ArrayList<String> children = superToSub.get(top);
                str += top + ":" + children.size();
                System.out.println(str);
                printTree(children, level);
            }
            else {
                str += top;
                System.out.println(str);
            }
        }
    }

    public void printTree (ArrayList<String> tops, int level, HashMap<String, Integer> eventCounts) {
        level++;
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            Integer cnt = 0;
            if (eventCounts.containsKey(top)) {
                cnt = eventCounts.get(top);
            }
            String str = "";
            for (int j = 0; j < level; j++) {
                str += "  ";

            }
            if (superToSub.containsKey(top)) {
                ArrayList<String> children = superToSub.get(top);
                str += top + ":" + cnt;
                System.out.println(str);
                printTree(children, level, eventCounts);
            }
            else {
                str += top;
                System.out.println(str);
            }
        }
    }


    /*
    <div class="Row">
        <div class="Cell">
            <p>Row 1 Column 1</p>
        </div>
        <div class="Cell">
            <p>Row 1 Column 2</p>
        </div>
        <div class="Cell">
            <p>Row 1 Column 3</p>
        </div>
    </div>
     */
    public String  htmlTableTree (String ns, ArrayList<String> tops,
                                  int level,
                                  HashMap<String, Integer> eventCounts ) {
        String str = "";
        level++;
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            if (top.startsWith(ns)) {
                Integer cnt = 0;
                if (eventCounts.containsKey(top)) {
                    cnt = eventCounts.get(top);
                }
                if (cnt>0) {
                    str += "<div id=\"row\">";

                    for (int j = 2; j < level; j++) {
                        str += "<div id=\"cell\"></div>";

                    }
                    String ref = top;
                    if (top.startsWith("http")) {
                        int idx = top.lastIndexOf("/");
                        String name = top;
                        if (idx > -1) {
                            name = top.substring(idx + 1);
                        }
                        ref = "<a href=\"" + top + "\">" + name + "</a>";
                    } else if (top.startsWith("dbp:")) {
                        int idx = top.lastIndexOf(":");
                        String name = top;
                        if (idx > -1) {
                            name = top.substring(idx + 1);
                        }
                        ref = "<a href=\"http://dbpedia.org/ontology/" + name + "\">" + name + "</a>";
                    }


                    if (cnt > 0) {
                        str += "<div id=\"cell\"><p>" + ref + ":" + cnt + "</p></div>";
                    } else {
                        str += "<div id=\"cell\"><p>" + ref + "</p></div>";

                        //str += "<div id=\"cell\">" + "</div>";
                    }/*
                for (int j = level; j < maxDepth; j++) {
                    str += "<div id=\"cell\"></div>";

                }*/
                    str += "</div>\n";
                    System.out.println("top = " + top);
                    if (superToSub.containsKey(top)) {
                        ArrayList<String> children = superToSub.get(top);
                        str += htmlTableTree(ns, children, level, eventCounts);
                    }
                }
            }
        }
        return str;
    }
    public String  htmlTableTreeOverview (String ns, ArrayList<String> tops,
                                  int level,
                                  HashMap<String, Integer> eventCounts,
                                          HashMap<String, ArrayList<PhraseCount>> phrases ) {
        String str = "";
        level++;
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            if (top.startsWith(ns)) {
                Integer cnt = 0;
                if (eventCounts.containsKey(top)) {
                    cnt = eventCounts.get(top);
                }
                if (cnt>0) {
                    str += "<div id=\"row\">";

                    for (int j = 2; j < level; j++) {
                        str += "<div id=\"cell\"></div>";

                    }
                    String ref = top;
                    if (top.startsWith("http")) {
                        int idx = top.lastIndexOf("/");
                        String name = top;
                        if (idx > -1) {
                            name = top.substring(idx + 1);
                        }
                        ref = "<a href=\"" + top + "\">" + name + "</a>";
                    } else if (top.startsWith("dbp:")) {
                        int idx = top.lastIndexOf(":");
                        String name = top;
                        if (idx > -1) {
                            name = top.substring(idx + 1);
                        }
                        ref = "<a href=\"http://dbpedia.org/ontology/" + name + "\">" + name + "</a>";
                    }



                    int instances = 0;
                    if (phrases.containsKey(top)) {
                        ArrayList<PhraseCount> phraseCounts = phrases.get(top);
                        instances = phraseCounts.size();
                    }
                    if (cnt > 0) {
                        str += "<div id=\"cell\"><p>" + ref + ":" + instances+";"+ cnt+"</p></div>";
                    } else {
                        str += "<div id=\"cell\"><p>" + ref + "</p></div>";
                    }

                    str += "</div>\n";
                  //  System.out.println("top = " + top);
                    if (superToSub.containsKey(top)) {
                        ArrayList<String> children = superToSub.get(top);
                        str += htmlTableTreeOverview(ns, children, level, eventCounts,phrases);
                    }
                }
            }
        }
        return str;
    }

    public void  htmlTableTree (OutputStream fos, String type, String ns, ArrayList<String> tops,
                                  int level,
                                  HashMap<String, Integer> typeCounts,
                                  HashMap<String, ArrayList<PhraseCount>> phrases) throws IOException {
        String str = "";
        level++;
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            str  = "";
            if (top.startsWith(ns) || ns.isEmpty()) {
                Integer cnt = 0;
                if (typeCounts.containsKey(top)) {
                    cnt = typeCounts.get(top);
                }
              //  System.out.println(top+ ":" + cnt);
                if (cnt>0) {
                    str += "<div id=\"row\">";
                    for (int j = 2; j < level; j++) {
                        str += "<div id=\"cell\"></div>";

                    }
                    String ref = top;
                    String tb = TreeStaticHtml.makeTickBox(type, top);
                    if (top.startsWith("http")) {
                        int idx = top.lastIndexOf("/");
                        String name = top;
                        if (idx > -1) {
                            name = top.substring(idx + 1);
                        }
                        tb = TreeStaticHtml.makeTickBox(type, name);
                        //ref = "<a href=\"" + top + "\">" + name + "</a>";
                        ref =  name;
                    } else if (top.startsWith("dbp:")) {
                        int idx = top.lastIndexOf(":");
                        String name = top;
                        if (idx > -1) {
                            name = top.substring(idx + 1);
                        }
                        tb = TreeStaticHtml.makeTickBox(type, name, top);
                        ref = "<a href=\"http://dbpedia.org/ontology/" + name + "\">" + name + "</a>";
                    }
                    int instances = 0;
                    if (phrases.containsKey(top)) {
                        ArrayList<PhraseCount> phraseCounts = phrases.get(top);
                        instances = phraseCounts.size();
                    }
                    if (cnt > 0) {
                        str += "<div id=\"cell\"><p>" + ref + ":" + instances+";"+ cnt+tb+"</p></div>";
                    } else {
                        str += "<div id=\"cell\"><p>" + ref + tb+"</p></div>";
                    }
                    str += "</div>\n";
                    str += "<div id=\"row\">";
                    for (int j = 2; j < level; j++) {
                        str += "<div id=\"cell\"></div>";

                    }
                    fos.write(str.getBytes());
                    str = "";

                    if (phrases.containsKey(top)) {
                        ArrayList<PhraseCount> phraseCounts = phrases.get(top);
                        Collections.sort(phraseCounts, new Comparator<PhraseCount>() {
                            @Override
                            public int compare(PhraseCount p1, PhraseCount p2) {

                                return p2.getCount().compareTo(p1.getCount());
                            }
                        });
                        String phraseString = "[";
                        int max = phraseCounts.get(0).getCount();
                        for (int j = 0; j < phraseCounts.size(); j++) {
                            PhraseCount phraseCount = phraseCounts.get(j);
                            //if ((phraseCount.getCount()*100)/max>=0) {
                            if (phraseCount.getCount()>0) {
                                int idx = phraseCount.getPhrase().lastIndexOf("/");
                                String name = phraseCount.getPhrase();
                                if (idx > -1) {
                                    name = phraseCount.getPhrase().substring(idx + 1);
                                }
                                tb = TreeStaticHtml.makeTickBox(type, name);
                                if (phraseCount.getPhrase().indexOf("dbpedia")>-1) {
                                    tb = TreeStaticHtml.makeTickBox(type, name, "dbpedia:"+name);
                                    ref = "<a href=\"" + phraseCount.getPhrase() + "\">" + name + ":" + phraseCount.getCount() + tb + "</a>";
                                }
                                else {
                                    ref =  name + ":" + phraseCount.getCount() + tb;
                                }
                                phraseString += ref;
                                if (j < phraseCounts.size() - 1) {
                                    phraseString += ", ";
                                }
                            }
                        }
                        phraseString += "]";
                        str = "<div id=\"cell\"><p>" + phraseString + "</p></div>";
                        fos.write(str.getBytes());
                        //str += "<div id=\"cell\"><p>" + phraseCounts.toString()+ "</p></div>";

                    }
                    str = "</div>\n";
                    fos.write(str.getBytes());
                    if (superToSub.containsKey(top)) {
                        ArrayList<String> children = superToSub.get(top);
                       // System.out.println(top+ ":" + cnt+", children:"+children.size());
                        htmlTableTree(fos, type, ns, children, level, typeCounts, phrases);
                    }
                    else {
                      //  System.out.println("has no children top = " + top);
                    }
                }
                else {
                    //// no use for this class
                }
            }
            else {
             //   System.out.println("ns = " + ns);
             //   System.out.println("top = " + top);
            }
        }
    }

    public void  htmlTableTree (OutputStream fos, String type, String ns, ArrayList<String> tops,
                                  int level,
                                  HashMap<String, Integer> typeCounts,
                                  HashMap<String, ArrayList<PhraseCount>> phrases,
                                  HashMap<String, ArrayList<String>> iliMap) throws IOException {
        String str = "";
        level++;
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            str  = "";
            if (top.startsWith(ns) || ns.isEmpty()) {
                Integer cnt = 0;
                if (typeCounts.containsKey(top)) {
                    cnt = typeCounts.get(top);
                }
              //  System.out.println(top+ ":" + cnt);
                if (cnt>0) {
                    str += "<div id=\"row\">";
                    for (int j = 2; j < level; j++) {
                        str += "<div id=\"cell\"></div>";

                    }
                    String ref = top;
                    String tb = TreeStaticHtml.makeTickBox(type, top);
                    if (top.startsWith("http")) {
                        int idx = top.lastIndexOf("/");
                        String name = top;
                        if (idx > -1) {
                            name = top.substring(idx + 1);
                        }
                        tb = TreeStaticHtml.makeTickBox(type, name);
                        //ref = "<a href=\"" + top + "\">" + name + "</a>";
                        ref =  name;
                    } else if (top.startsWith("dbp:")) {
                        int idx = top.lastIndexOf(":");
                        String name = top;
                        if (idx > -1) {
                            name = top.substring(idx + 1);
                        }
                        tb = TreeStaticHtml.makeTickBox(type, name, top);
                        ref = "<a href=\"http://dbpedia.org/ontology/" + name + "\">" + name + "</a>";
                    }
                    int instances = 0;
                    if (phrases.containsKey(top)) {
                        ArrayList<PhraseCount> phraseCounts = phrases.get(top);
                        instances = phraseCounts.size();
                    }
                    if (cnt > 0) {
                        str += "<div id=\"cell\"><p>" + ref + ":" + instances+";"+ cnt+tb+"</p></div>";
                    } else {
                        str += "<div id=\"cell\"><p>" + ref + tb+"</p></div>";
                    }
                    str += "</div>\n";
                    str += "<div id=\"row\">";
                    for (int j = 2; j < level; j++) {
                        str += "<div id=\"cell\"></div>";

                    }
                    fos.write(str.getBytes());
                    str = "";

                    if (phrases.containsKey(top)) {
                        ArrayList<PhraseCount> phraseCounts = phrases.get(top);
                        Collections.sort(phraseCounts, new Comparator<PhraseCount>() {
                            @Override
                            public int compare(PhraseCount p1, PhraseCount p2) {

                                return p2.getCount().compareTo(p1.getCount());
                            }
                        });
                        String phraseString = "[";
                        int max = phraseCounts.get(0).getCount();
                        for (int j = 0; j < phraseCounts.size(); j++) {
                            PhraseCount phraseCount = phraseCounts.get(j);
                            //if ((phraseCount.getCount()*100)/max>=0) {
                            if (phraseCount.getCount()>0) {
                                int idx = phraseCount.getPhrase().lastIndexOf("/");
                                String name = phraseCount.getPhrase();
                                if (idx > -1) {
                                    name = phraseCount.getPhrase().substring(idx + 1);
                                }
                                String iliString = name;
                                if (iliMap.containsKey(name)) {
                                    ArrayList<String> ilis = iliMap.get(name);
                                    for (int k = 0; k < ilis.size(); k++) {
                                        String ili = ilis.get(k);
                                        iliString +=";"+ili;
                                    }
                                }
                                else {
                                    //System.out.println("could not find iliString = " + iliString);
                                }
                                tb = TreeStaticHtml.makeTickBox(type, iliString);
                                if (phraseCount.getPhrase().indexOf("dbpedia")>-1) {
                                    tb = TreeStaticHtml.makeTickBox(type, name, "dbpedia:"+name);
                                    ref = "<a href=\"" + phraseCount.getPhrase() + "\">" + name + ":" + phraseCount.getCount() + tb + "</a>";
                                }
                                else {
                                    ref =  name + ":" + phraseCount.getCount() + tb;
                                }
                                phraseString += ref;
                                if (j < phraseCounts.size() - 1) {
                                    phraseString += ", ";
                                }
                            }
                        }
                        phraseString += "]";
                        str = "<div id=\"cell\"><p>" + phraseString + "</p></div>";
                        fos.write(str.getBytes());
                        //str += "<div id=\"cell\"><p>" + phraseCounts.toString()+ "</p></div>";

                    }
                    str = "</div>\n";
                    fos.write(str.getBytes());
                    if (superToSub.containsKey(top)) {
                        ArrayList<String> children = superToSub.get(top);
                       // System.out.println(top+ ":" + cnt+", children:"+children.size());
                        htmlTableTree(fos, type, ns, children, level, typeCounts, phrases, iliMap);
                    }
                    else {
                      //  System.out.println("has no children top = " + top);
                    }
                }
                else {
                    //// no use for this class
                }
            }
            else {
             //   System.out.println("ns = " + ns);
             //   System.out.println("top = " + top);
            }
        }
    }

    public void cumulateScores (String ns, ArrayList<String> tops,
                                HashMap<String, Integer> eventCounts ) {
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            if (top.startsWith(ns)) {
                if (superToSub.containsKey(top)) {
                    ArrayList<String> children = superToSub.get(top);
                    cumulateScores(ns, children, eventCounts);
                    int cCount = 0;
                    for (int j = 0; j < children.size(); j++) {
                        String child =  children.get(j);
                        if (eventCounts.containsKey(child)) {
                            cCount += eventCounts.get(child);
                        }
                    }
                    if (eventCounts.containsKey(top)) {
                        Integer cnt = eventCounts.get(top);
                        cnt+= cCount;
                        eventCounts.put(top, cnt);
                    }
                    else {
                        eventCounts.put(top, cCount);
                    }
                }
            }
            else {
               // System.out.println("ns = " + ns);
               // System.out.println("top = " + top);
            }
        }
    }

/*    public void cumulateScores (String ns, ArrayList<String> tops,
                                  HashMap<String, ArrayList<PhraseCount>> eventCounts ) {
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            if (top.startsWith(ns)) {
                if (superToSub.containsKey(top)) {
                    ArrayList<String> children = superToSub.get(top);
                    cumulateScores(ns, children, eventCounts);
                    int cCount = 0;
                    for (int j = 0; j < children.size(); j++) {
                        String child =  children.get(j);
                        if (eventCounts.containsKey(child)) {
                            ArrayList<PhraseCount> phrases = eventCounts.get(child);
                            for (int k = 0; k < phrases.size(); k++) {
                                PhraseCount phraseCount = phrases.get(k);
                                cCount += phraseCount.getCount();
                            }
                        }
                    }
                    if (eventCounts.containsKey(top)) {
                        Integer cnt = eventCounts.get(top);
                        cnt+= cCount;
                        eventCounts.put(top, cnt);
                    }
                    else {
                        eventCounts.put(top, cCount);
                    }
                }
            }
        }
    }*/

    public int getMaxDepth (ArrayList<String> tops, int level) {
        int maxDepth = 0;
        level++;
        maxDepth = level;
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            if (superToSub.containsKey(top)) {
                ArrayList<String> children = superToSub.get(top);
                int depth = getMaxDepth(children, level);
                if (depth>maxDepth) {
                    maxDepth = depth;
                }
            }
        }
        return maxDepth;
    }

    /*
    <div class="Table">
    <div class="Title">
        <p>This is a Table</p>
    </div>
    <div class="Heading">
        <div class="Cell">
            <p>Heading 1</p>
        </div>
        <div class="Cell">
            <p>Heading 2</p>
        </div>
        <div class="Cell">
            <p>Heading 3</p>
        </div>
    </div>
     */

}
