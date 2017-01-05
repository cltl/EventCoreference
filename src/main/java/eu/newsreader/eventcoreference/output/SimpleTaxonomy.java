package eu.newsreader.eventcoreference.output;

import eu.newsreader.eventcoreference.objects.PhraseCount;
import eu.newsreader.eventcoreference.util.EuroVoc;
import eu.newsreader.eventcoreference.util.TreeStaticHtml;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by piek on 29/05/16.
 */
@Deprecated
public class SimpleTaxonomy {
    static final int colmax = 650;
    static final int colmaxevents = 150;
    public HashMap<String, ArrayList<String>> conceptToLabels = new HashMap<String, ArrayList<String>>();
    public HashMap<String, String> labelToConcept = new HashMap<String, String>();
    public HashMap<String, String> conceptToPrefLabel = new HashMap<String, String>();
    public HashMap<String, String> subToSuper = new HashMap<String, String>();
    public HashMap<String, ArrayList<String>> superToSub = new HashMap<String, ArrayList<String>>();
    static final String buttons1 = "<button type=\"button\" onclick=\"document.getElementById('cell2').style.display='table-cell'\">Show</button>\n";
    static public final String accordion = "<div class=\"accordionItem\">";
    public String makeToggle (String id) {
        String str = "<a data-toggle=\"collapse\" href=\"#collapse"+id+"\">Collapsible</a>\n";
        return str;
    }


    static public void main (String[] args) {
        String hierarchyPath = "/Users/piek/Desktop/NWR-INC/dasym/stats-4-normalised/DBpediaHierarchy_parent_child.tsv";
        SimpleTaxonomy simpleTaxonomy = new SimpleTaxonomy();
        simpleTaxonomy.readSimpleTaxonomyFromDbpFile(hierarchyPath);
    }

    public SimpleTaxonomy () {
        subToSuper = new HashMap<String, String>();
        superToSub = new HashMap<String, ArrayList<String>>();
    }

    public void readSimpleTaxonomyFromSkosFile (String filePath) {
        //<rdf:Description rdf:about="http://eurovoc.europa.eu/8404">
        // <skos:broader rdf:resource="http://eurovoc.europa.eu/2467"/>
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
                String subClass = "";
                String superClass= "";
                while (in.ready() && (inputLine = in.readLine()) != null) {
                    // System.out.println(inputLine);
                    inputLine = inputLine.trim();
                    if (inputLine.trim().length() > 0) {

                        //<rdf:Description rdf:about="http://eurovoc.europa.eu/8404">
                        // <skos:broader rdf:resource="http://eurovoc.europa.eu/2467"/>
                        int idx_s = inputLine.indexOf("<rdf:Description rdf:about=");
                        int idx_e = -1;
                        if (idx_s>-1) {
                            idx_s = inputLine.indexOf("\"");
                            idx_e = inputLine.lastIndexOf("\"");
                            subClass = inputLine.substring(idx_s+1, idx_e);
                           // System.out.println("subClass = " + subClass);
                        }
                        else {
                            idx_s = inputLine.indexOf("<skos:broader rdf:resource=");
                            idx_e = -1;
                            if (idx_s>-1) {
                                idx_s = inputLine.indexOf("\"");
                                idx_e = inputLine.lastIndexOf("\"");
                                superClass = inputLine.substring(idx_s+1, idx_e);
                                //System.out.println("parent = " + superClass);
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
                            else {
                                //<skos:prefLabel xml:lang="en">
                                //<skos:altLabel xml:lang="en">resolution of the European Parliament</skos:altLabel>
                                idx_s = inputLine.indexOf("skos:prefLabel xml:lang=\"en\">");
                                idx_e = -1;
                                if (idx_s>-1) {
                                    idx_s = inputLine.indexOf(">");
                                    idx_e = inputLine.lastIndexOf("</");
                                    String label = inputLine.substring(idx_s+1, idx_e);
                                    labelToConcept.put(label, subClass);
                                    conceptToPrefLabel.put(subClass, label);
                                    if (conceptToLabels.containsKey(subClass)) {
                                        ArrayList<String> labels = conceptToLabels.get(subClass);
                                        labels.add(label);
                                        conceptToLabels.put(subClass, labels);
                                    }
                                    else {
                                        ArrayList<String> labels = new ArrayList<String>();
                                        labels.add(label);
                                        conceptToLabels.put(subClass, labels);
                                    }
                                }
                                else {
                                    idx_s = inputLine.indexOf("skos:altLabel xml:lang=\"en\">");
                                    idx_e = -1;
                                    if (idx_s > -1) {
                                        idx_s = inputLine.indexOf(">");
                                        idx_e = inputLine.lastIndexOf("</");
                                        String label = inputLine.substring(idx_s + 1, idx_e);
                                        labelToConcept.put(label, subClass);
                                        if (conceptToLabels.containsKey(subClass)) {
                                            ArrayList<String> labels = conceptToLabels.get(subClass);
                                            labels.add(label);
                                            conceptToLabels.put(subClass, labels);
                                        }
                                        else {
                                            ArrayList<String> labels = new ArrayList<String>();
                                            labels.add(label);
                                            conceptToLabels.put(subClass, labels);
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
        //printTree();
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
                       // System.out.println("inputLine = " + inputLine);
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
                                    //System.out.println("subClass = " + subClass);
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
                        else {
                            System.out.println("Skipping line:"+inputLine);
                        }
                    }
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
        //printTree();
    }

    public void readSimpleTaxonomyFromDbpFile (String filePath, Set<String> keySet) {
        try {
            InputStreamReader isr = null;
            System.out.println("filePath = " + filePath);
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
<http://dbpedia.org/resource/Abraham_Lincoln__1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/TimePeriod> .
<http://dbpedia.org/resource/Abraham_Lincoln__2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/TimePeriod> .
<http://dbpedia.org/resource/Abraham_Lincoln__3> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/TimePeriod> .
<http://dbpedia.org/resource/Austroasiatic_languages> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Thing> .
<http://dbpedia.org/resource/Afroasiatic_languages> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Thing> .
     */
                        // System.out.println("inputLine = " + inputLine);
                        String[] fields = inputLine.split("\t");
                        if (fields.length == 3) {
                            String className = fields[0];
                            className = className.substring(className.lastIndexOf("/"));
                            System.out.println("className = " + className);
                            if (keySet.contains(className)) {
                                String subClass = "dbp:" + className;
                                className = fields[2];
                                className = className.substring(className.lastIndexOf("/"));
                                String superClass = "dbp:" + className;
                                if (!subClass.equals(superClass)) {
                                    subToSuper.put(subClass, superClass);
                                    if (superToSub.containsKey(superClass)) {
                                        ArrayList<String> subs = superToSub.get(superClass);
                                        if (!subs.contains(subClass)) {
                                            subs.add(subClass);
                                            superToSub.put(superClass, subs);
                                        }
                                    } else {
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
        } catch (IOException e) {
            //e.printStackTrace();
        }
        //printTree();
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

    public void printTree () {
        ArrayList<String> tops = this.getTops();
        printTree(tops, 0);
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
        ArrayList<PhraseCount> countedTops = new ArrayList<PhraseCount>();
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            Integer cnt = 0;
            if (typeCounts.containsKey(top)) {
                cnt = typeCounts.get(top);
            }
            PhraseCount phraseCount = new PhraseCount(top, cnt);
            countedTops.add(phraseCount);
        }
        Collections.sort(countedTops, new Comparator<PhraseCount>() {
            @Override
            public int compare(PhraseCount p1, PhraseCount p2) {

                return p2.getCount().compareTo(p1.getCount());
            }
        });

        for (int i = 0; i < countedTops.size(); i++) {
            PhraseCount topCount = countedTops.get(i);
            String top = topCount.getPhrase();
            String topName = "";
            str  = "";
            if (top.startsWith(ns) || ns.isEmpty()) {
                Integer cnt = topCount.getCount();
              //  System.out.println(top+ ":" + cnt);
                if (cnt>0) {
                   // if (top.indexOf("Agent") > -1) {
                    if (top.indexOf("NOTSKIP") > -1) {
                        level--;
                        level--;
                    } else {
                        str += accordion+ "<h2>\n";
                        for (int j = 2; j < level; j++) {
                            str += "<div id=\"cell\"></div>";
                        }
                        String ref = top;
                        String tb = TreeStaticHtml.makeTickBox(type, top);
                        if (top.startsWith("http")) {
                            int idx = top.lastIndexOf("/");
                            //String name = top;
                            if (idx > -1) {
                                topName = top.substring(idx + 1);
                            }
                            tb = TreeStaticHtml.makeTickBox(type, topName);
                            //ref = "<a href=\"" + top + "\">" + name + "</a>";
                            ref = topName;
                        } else if (top.startsWith("dbp:")) {
                            int idx = top.lastIndexOf(":");
                            //String name = top;
                            if (idx > -1) {
                                topName = top.substring(idx + 1);
                            }
                            tb = TreeStaticHtml.makeTickBox(type, topName, top);
                            ref = "<a href=\"http://dbpedia.org/ontology/" + topName + "\">" + topName;
                        }
                        int instances = 0;
                        if (phrases.containsKey(top)) {
                            ArrayList<PhraseCount> phraseCounts = phrases.get(top);
                            instances = phraseCounts.size();
                        }
                       // String toggle = makeToggle(topName);

                        if (cnt > 0) {

                            //str += "<div id=\"cell\">" + ref + ":" + instances + ";" + cnt +"</a>";
                            str += "<div id=\"cell\">" + ref + "</a></div><div id=\"cell7\">" + instances + ";"+cnt;
                            if (instances>0) str += tb;
                            str +=  "</div>";
                        } else {
                           // str += "<div id=\"cell\">" + ref + tb + "</div>";
                            str += "<div id=\"cell\">" + ref + "</div>";
                        }
                        str += "\n</h2>\n";
                        for (int j = 2; j < level; j++) {
                            str += "<div id=\"cell\"></div>";

                        }
                        fos.write(str.getBytes());
                        str = "";

                        int children = 0;
                        if (phrases.containsKey(top)) {
                            ArrayList<PhraseCount> phraseCounts = phrases.get(top);
                            if (top.toLowerCase().indexOf("disease")>-1) System.out.println("phrases = " + phraseCounts.toString());
                            Collections.sort(phraseCounts, new Comparator<PhraseCount>() {
                                @Override
                                public int compare(PhraseCount p1, PhraseCount p2) {

                                    return p2.getCount().compareTo(p1.getCount());
                                }
                            });
                            String phraseString = "[";
                            int collength = 0;
                            int max = phraseCounts.get(0).getCount();
                            for (int j = 0; j < phraseCounts.size(); j++) {
                                PhraseCount phraseCount = phraseCounts.get(j);
                                //if ((phraseCount.getCount()*100)/max>=0) {
                                if (phraseCount.getCount() > 0) {
                                    children++;
                                    int idx = phraseCount.getPhrase().lastIndexOf("/");
                                    String name = phraseCount.getPhrase();
                                    if (idx > -1) {
                                        name = phraseCount.getPhrase().substring(idx + 1);
                                    }
                                    if (name.length() > 50) {
                                        int pos = name.indexOf(" ", 50);
                                        if (pos > 0) {
                                            name = name.substring(0, pos) + " etc.";
                                        }
                                    }
                                    tb = TreeStaticHtml.makeTickBox(type, name);
                                    if (phraseCount.getPhrase().indexOf("dbpedia") > -1) {
                                        tb = TreeStaticHtml.makeTickBox(type, name, "dbpedia:" + name);
                                        ref = "<a href=\"" + phraseCount.getPhrase() + "\">" + name + ":" + phraseCount.getCount() + tb + "</a>";
                                    } else {

                                        ref = name.replace("+","_") + ":" + phraseCount.getCount() + tb;
                                    }


                                    collength += ref.length();
                                    phraseString += ref;
                                    if (j < phraseCounts.size() - 1) {
                                        phraseString += ", ";
                                    }
                                    if (collength > colmax) {
                                        phraseString += "\n";
                                        collength = 0;
                                    }
                                }
                            }
                            phraseString += "]";
                            str =   "<div id=\"cell2\"  class=\"collapse\"><p>" + phraseString + "</p></div>\n";
                            fos.write(str.getBytes());
                        }
                        else {
                        }
                        str = "</div>\n"; // closing accordion
                        fos.write(str.getBytes());
                    }
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

    public void  htmlTableTopicTree (OutputStream fos, String type, ArrayList<String> tops,
                                int level,
                                HashMap<String, Integer> typeCounts,
                                HashMap<String, ArrayList<PhraseCount>> phrases) throws IOException {
        String str = "";
        level++;
        ArrayList<PhraseCount> countedTops = new ArrayList<PhraseCount>();
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            Integer cnt = 0;
            if (typeCounts.containsKey(top)) {
                cnt = typeCounts.get(top);
            }
            PhraseCount phraseCount = new PhraseCount(top, cnt);
            countedTops.add(phraseCount);
        }
        Collections.sort(countedTops, new Comparator<PhraseCount>() {
            @Override
            public int compare(PhraseCount p1, PhraseCount p2) {

                return p2.getCount().compareTo(p1.getCount());
            }
        });

        for (int i = 0; i < countedTops.size(); i++) {
                PhraseCount topCount = countedTops.get(i);
                String top = topCount.getPhrase();
                String topName = "";
                str  = "";
                Integer cnt = topCount.getCount();
            if (cnt>0) {
                //  System.out.println(top+ ":" + cnt);
                // if (top.indexOf("Agent") > -1) {
                if (top.indexOf("NOTSKIP") > -1) {
                    level--;
                    level--;
                } else {
                    str += accordion + "<h2>";
                    for (int j = 2; j < level; j++) {
                        str += "<div id=\"cell4\"></div>";
                    }
                    String ref = top;
                    if (conceptToPrefLabel.containsKey(top)) {
                        ref = conceptToPrefLabel.get(top);
                    }

                    String tb = TreeStaticHtml.makeTickBox(type, top);
                    //<div class="accordionItem"><h2><div id="cell4"><a href="http://eurovoc.europa.eu/3581">restriction on competition</a></div><div id="cell4">175671<INPUT TYPE="checkbox" NAME="topic" VALUE="http://eurovoc.europa.eu/3581"></div></h2></div>
                    //<div class="accordionItem"><h2><div id="cell4"><a href="http://eurovoc.europa.eu/2488">import policy</a></div><div id="cell4">6070<INPUT TYPE="checkbox" NAME="topic" VALUE="http://eurovoc.europa.eu/2488"></div></h2></div>

                    if (cnt > 0) {
                        str += "<div id=\"cell4\">" +"<a href=\""+top+"\">"+ ref + "</a></div><div id=\"cell7\">" + cnt;
                        str += tb;
                        str += "</div>";
                    } else {
                        // str += "<div id=\"cell\">" + ref + tb + "</div>";
                        str += "<div id=\"cell4\">" + ref + "</div>";
                    }
                    str += "</h2>";
                    str += "</div>\n"; // closing accordion
                    fos.write(str.getBytes());
                }
                if (superToSub.containsKey(top)) {
                    ArrayList<String> children = superToSub.get(top);
                    htmlTableTopicTree(fos, type, children, level, typeCounts, phrases);
                } else {
                    //  System.out.println("has no children top = " + top);
                }
            }
        }
    }



    public void  htmlTableTreeSupressEmptyLeaves (OutputStream fos, String type, String ns, ArrayList<String> tops,
                                  int level,
                                  HashMap<String, Integer> typeCounts,
                                  HashMap<String, ArrayList<PhraseCount>> phrases) throws IOException {
        level++;
        ArrayList<PhraseCount> countedTops = new ArrayList<PhraseCount>();
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            Integer cnt = 0;
            if (typeCounts.containsKey(top)) {
                cnt = typeCounts.get(top);
            }
            PhraseCount phraseCount = new PhraseCount(top, cnt);
            countedTops.add(phraseCount);
        }
        Collections.sort(countedTops, new Comparator<PhraseCount>() {
            @Override
            public int compare(PhraseCount p1, PhraseCount p2) {

                return p2.getCount().compareTo(p1.getCount());
            }
        });

        for (int i = 0; i < countedTops.size(); i++) {
            PhraseCount topCount = countedTops.get(i);
            String top = topCount.getPhrase();
            String topName = "";
            String parent = "";
            if (top.startsWith(ns) || ns.isEmpty()) {
                Integer cnt = topCount.getCount();
              //  System.out.println(top+ ":" + cnt);
                if (cnt>0) {
                    if (top.indexOf("Agent") > -1) {
                        level--;
                        level--;
                    } else {
                        parent += accordion+ "<h2>\n";

                        //str += "<div id=\"row\">";
                        for (int j = 2; j < level; j++) {
                            parent += "<div id=\"cell\"></div>";

                        }
                        String ref = top;
                        String tb = TreeStaticHtml.makeTickBox(type, top);
                        if (top.startsWith("http")) {
                            int idx = top.lastIndexOf("/");
                            //String name = top;
                            if (idx > -1) {
                                topName = top.substring(idx + 1);
                            }
                            tb = TreeStaticHtml.makeTickBox(type, topName);
                            //ref = "<a href=\"" + top + "\">" + name + "</a>";
                            ref = topName;
                        } else if (top.startsWith("dbp:")) {
                            int idx = top.lastIndexOf(":");
                            //String name = top;
                            if (idx > -1) {
                                topName = top.substring(idx + 1);
                            }
                            tb = TreeStaticHtml.makeTickBox(type, topName, top);
                            ref = "<a href=\"http://dbpedia.org/ontology/" + topName + "\">" + topName;
                        }
                        int instances = 0;
                        if (phrases.containsKey(top)) {
                            ArrayList<PhraseCount> phraseCounts = phrases.get(top);
                            instances = phraseCounts.size();
                        }
                       // String toggle = makeToggle(topName);

                        if (cnt > 0) {
                            parent += "<div id=\"cell\">" + ref + ":" + instances + ";" + cnt +"</a>"+ tb + "</div>";
                        } else {
                            parent += "<div id=\"cell\">" + ref + tb + "</div>";
                        }
                        parent += "\n</h2>\n";
                        for (int j = 2; j < level; j++) {
                            parent += "<div id=\"cell\"></div>";

                        }
/*
                        fos.write(str.getBytes());
                        str = "";
*/

                        int children = 0;
                        if (phrases.containsKey(top)) {
                            ArrayList<PhraseCount> phraseCounts = phrases.get(top);
                            Collections.sort(phraseCounts, new Comparator<PhraseCount>() {
                                @Override
                                public int compare(PhraseCount p1, PhraseCount p2) {

                                    return p2.getCount().compareTo(p1.getCount());
                                }
                            });
                            String phraseString = "[";
                            int collength = 0;
                            int max = phraseCounts.get(0).getCount();
                            for (int j = 0; j < phraseCounts.size(); j++) {
                                PhraseCount phraseCount = phraseCounts.get(j);
                                //if ((phraseCount.getCount()*100)/max>=0) {
                                if (phraseCount.getCount() > 1) {
                                    children++;
                                    int idx = phraseCount.getPhrase().lastIndexOf("/");
                                    String name = phraseCount.getPhrase();
                                    if (idx > -1) {
                                        name = phraseCount.getPhrase().substring(idx + 1);
                                    }
                                    if (name.length() > 50) {
                                        int pos = name.indexOf(" ", 50);
                                        if (pos > 0) {
                                            name = name.substring(0, pos) + " etc.";
                                        }
                                    }
                                    tb = TreeStaticHtml.makeTickBox(type, name);
                                    if (phraseCount.getPhrase().indexOf("dbpedia") > -1) {
                                        tb = TreeStaticHtml.makeTickBox(type, name, "dbpedia:" + name);
                                        ref = "<a href=\"" + phraseCount.getPhrase() + "\">" + name + ":" + phraseCount.getCount() + tb + "</a>";
                                    } else {

                                        ref = name + ":" + phraseCount.getCount() + tb;
                                    }

    /*                                if (ref.length()>(collength/2)) {
                                        ref = ref.substring(0, collength/2)+"\n"+ref.substring(collength/2);
                                    }*/

                                    collength += ref.length();
                                    phraseString += ref;
                                    if (j < phraseCounts.size() - 1) {
                                        phraseString += ", ";
                                    }
                                    if (collength > colmax) {
                                        phraseString += "\n";
                                        collength = 0;
                                    }
                                }
                            }
                            phraseString += "]";
                            // str = "<div id=\"cell\" style=\"white-space: pre;\"><p>" + phraseString + "</p></div>";
/*                            str =   "<div id=\""+"collapse"+topName+"\">\n" +
                                    "<div id=\"cell2\"  class=\"collapse\" style=\"white-space: pre;\"><p>" + phraseString + "</p></div>\n";
                            str += "</div>\n";*/
                            if (children>0) {
                                parent +=   "<div id=\"cell2\"  class=\"collapse\"><p>" + phraseString + "</p></div>\n";
                                //parent += "<div id=\"cell2\"  class=\"collapse\" style=\"white-space: pre;\"><p>" + phraseString + "</p></div>\n";
                                //fos.write(parent.getBytes());
                                fos.write(parent.getBytes());
                                //str += "<div id=\"cell\"><p>" + phraseCounts.toString()+ "</p></div>";
                            }
                        }
                        else {
                            //fos.write(parent.getBytes());
                        }
                        //str = "</div>\n"; //closing row
                        parent = "</div>\n"; // closing accordion
                        fos.write(parent.getBytes());
                    }
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

    public void  htmlTableTreeORG (OutputStream fos, String type, String ns, ArrayList<String> tops,
                                  int level,
                                  HashMap<String, Integer> typeCounts,
                                  HashMap<String, ArrayList<PhraseCount>> phrases) throws IOException {
        String str = "";
        level++;
        ArrayList<PhraseCount> countedTops = new ArrayList<PhraseCount>();
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            Integer cnt = 0;
            if (typeCounts.containsKey(top)) {
                cnt = typeCounts.get(top);
            }
            PhraseCount phraseCount = new PhraseCount(top, cnt);
            countedTops.add(phraseCount);
        }
        Collections.sort(countedTops, new Comparator<PhraseCount>() {
            @Override
            public int compare(PhraseCount p1, PhraseCount p2) {

                return p2.getCount().compareTo(p1.getCount());
            }
        });

        for (int i = 0; i < countedTops.size(); i++) {
            PhraseCount topCount = countedTops.get(i);
            String top = topCount.getPhrase();
            String topName = "";
            str  = "";
            if (top.startsWith(ns) || ns.isEmpty()) {
                Integer cnt = topCount.getCount();
              //  System.out.println(top+ ":" + cnt);
                if (cnt>0) {
                    if (top.indexOf("Agent") > -1) {
                        level--;
                        level--;
                    } else {
                        str += "<div id=\"row\">";
                        for (int j = 2; j < level; j++) {
                            str += "<div id=\"cell\"></div>";

                        }
                        String ref = top;
                        String tb = TreeStaticHtml.makeTickBox(type, top);
                        if (top.startsWith("http")) {
                            int idx = top.lastIndexOf("/");
                            //String name = top;
                            if (idx > -1) {
                                topName = top.substring(idx + 1);
                            }
                            tb = TreeStaticHtml.makeTickBox(type, topName);
                            //ref = "<a href=\"" + top + "\">" + name + "</a>";
                            ref = topName;
                        } else if (top.startsWith("dbp:")) {
                            int idx = top.lastIndexOf(":");
                            //String name = top;
                            if (idx > -1) {
                                topName = top.substring(idx + 1);
                            }
                            tb = TreeStaticHtml.makeTickBox(type, topName, top);
                            ref = "<a href=\"http://dbpedia.org/ontology/" + topName + "\">" + topName + "</a>";
                        }
                        int instances = 0;
                        if (phrases.containsKey(top)) {
                            ArrayList<PhraseCount> phraseCounts = phrases.get(top);
                            instances = phraseCounts.size();
                        }
                       // String toggle = makeToggle(topName);
                        str += accordion+ "<h2>\n";

                        if (cnt > 0) {
                            str += "<div id=\"cell\"><p>" + ref + ":" + instances + ";" + cnt + tb + "</p></div>";
                        } else {
                            str += "<div id=\"cell\"><p>" + ref + tb + "</p></div>";
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
                            int collength = 0;
                            int max = phraseCounts.get(0).getCount();
                            for (int j = 0; j < phraseCounts.size(); j++) {
                                PhraseCount phraseCount = phraseCounts.get(j);
                                //if ((phraseCount.getCount()*100)/max>=0) {
                                if (phraseCount.getCount() > 1) {
                                    int idx = phraseCount.getPhrase().lastIndexOf("/");
                                    String name = phraseCount.getPhrase();
                                    if (idx > -1) {
                                        name = phraseCount.getPhrase().substring(idx + 1);
                                    }
                                    if (name.length() > 50) {
                                        int pos = name.indexOf(" ", 50);
                                        if (pos > 0) {
                                            name = name.substring(0, pos) + " etc.";
                                        }
                                    }
                                    tb = TreeStaticHtml.makeTickBox(type, name);
                                    if (phraseCount.getPhrase().indexOf("dbpedia") > -1) {
                                        tb = TreeStaticHtml.makeTickBox(type, name, "dbpedia:" + name);
                                        ref = "<a href=\"" + phraseCount.getPhrase() + "\">" + name + ":" + phraseCount.getCount() + tb + "</a>";
                                    } else {

                                        ref = name + ":" + phraseCount.getCount() + tb;
                                    }

    /*                                if (ref.length()>(collength/2)) {
                                        ref = ref.substring(0, collength/2)+"\n"+ref.substring(collength/2);
                                    }*/

                                    collength += ref.length();
                                    phraseString += ref;
                                    if (j < phraseCounts.size() - 1) {
                                        phraseString += ", ";
                                    }
                                    if (collength > colmax) {
                                        phraseString += "\n";
                                        collength = 0;
                                    }
                                }
                            }
                            phraseString += "]";
                            // str = "<div id=\"cell\" style=\"white-space: pre;\"><p>" + phraseString + "</p></div>";
/*                            str =   "<div id=\""+"collapse"+topName+"\">\n" +
                                    "<div id=\"cell2\"  class=\"collapse\" style=\"white-space: pre;\"><p>" + phraseString + "</p></div>\n";
                            str += "</div>\n";*/
                            str =   "<div id=\"cell2\"  class=\"collapse\"><p>" + phraseString + "</p></div>\n";
                            fos.write(str.getBytes());
                            //str += "<div id=\"cell\"><p>" + phraseCounts.toString()+ "</p></div>";

                        }
                        str = "</div>\n"; //closing row
                        str = "</div>\n"; // closing accordion
                        fos.write(str.getBytes());
                    }
                    if (superToSub.containsKey(top)) {
                        ArrayList<String> children = superToSub.get(top);
                       // System.out.println(top+ ":" + cnt+", children:"+children.size());
                        htmlTableTreeORG(fos, type, ns, children, level, typeCounts, phrases);
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
        ArrayList<PhraseCount> countedTops = new ArrayList<PhraseCount>();
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            Integer cnt = 0;
            if (typeCounts.containsKey(top)) {
                cnt = typeCounts.get(top);
            }
            PhraseCount phraseCount = new PhraseCount(top, cnt);
            countedTops.add(phraseCount);
        }
        Collections.sort(countedTops, new Comparator<PhraseCount>() {
            @Override
            public int compare(PhraseCount p1, PhraseCount p2) {

                return p2.getCount().compareTo(p1.getCount());
            }
        });

        for (int i = 0; i < countedTops.size(); i++) {
            PhraseCount topCount = countedTops.get(i);
            String top = topCount.getPhrase();
            String topName = top;
            str  = "";
            if (top.startsWith(ns) || ns.isEmpty()) {
                Integer cnt = topCount.getCount();
                //  System.out.println(top+ ":" + cnt);
                if (cnt>0) {
                    str += accordion+ "<h2>\n";

                    for (int j = 2; j < level; j++) {
                        str += "<div id=\"cell\"></div>";

                    }
                    String ref = top;
                    String tb = TreeStaticHtml.makeTickBox(type, top);
                    if (top.startsWith("http")) {
                        int idx = top.lastIndexOf("/");
                        if (idx > -1) {
                            topName = top.substring(idx + 1);
                        }
                        tb = TreeStaticHtml.makeTickBox(type, topName);
                        //ref = "<a href=\"" + top + "\">" + name + "</a>";
                        ref =  "<a href=\"" + top + "\">" + topName + "\n";
                    } else if (top.startsWith("dbp:")) {
                        int idx = top.lastIndexOf(":");
                        if (idx > -1) {
                            topName = top.substring(idx + 1);
                        }
                        tb = TreeStaticHtml.makeTickBox(type, topName, top);
                        ref = "<a href=\"http://dbpedia.org/ontology/" + topName + "\">" + topName + "\n";
                    }
                    else if (top.startsWith("eso:")) {
                        int idx = top.lastIndexOf(":");
                        if (idx > -1) {
                            topName = top.substring(idx + 1);
                        }
                        tb = TreeStaticHtml.makeTickBox(type, topName, top);
                        ref = "<a href=\"http://www.newsreader-project/ontology/eso/" + topName + "\">" + topName + "\n";
                    }
                    int instances = 0;
                    if (phrases.containsKey(top)) {
                        ArrayList<PhraseCount> phraseCounts = phrases.get(top);
                        instances = phraseCounts.size();
                    }

                    if (cnt > 0) {
                        str += "<div id=\"cell\">" + ref + "</a></div><div id=\"cell7\">" + instances + ";"+cnt+tb+"</div>";
                       // str += "<div id=\"cell\">" + ref + ":" + instances+";"+ cnt+"</a>"+tb+"</div>";
                    } else {
                        str += "<div id=\"cell\">" + ref +"</a>"+ tb+"</div>";
                    }
                    str += "\n</h2>\n";
                    for (int j = 2; j < level; j++) {
                        str += "<div id=\"cell\"></div>";
                    }

                    fos.write(str.getBytes());
                    str = "";

                    if (phrases.containsKey(top)) {
                        ArrayList<PhraseCount> phraseCounts = phrases.get(top);
                        if (top.toLowerCase().indexOf("disease")>-1) {
                            System.out.println("phraseCounts.toString() = " + phraseCounts.toString());
                        }
                        Collections.sort(phraseCounts, new Comparator<PhraseCount>() {
                            @Override
                            public int compare(PhraseCount p1, PhraseCount p2) {

                                return p2.getCount().compareTo(p1.getCount());
                            }
                        });
                        int collength = 0;
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
                                /*if (iliMap.containsKey(name)) {
                                    ArrayList<String> ilis = iliMap.get(name);
                                    for (int k = 0; k < ilis.size(); k++) {
                                        String ili = ilis.get(k);
                                        iliString +=";"+ili;
                                    }
                                }
                                else {
                                    //System.out.println("could not find iliString = " + iliString);
                                }*/
                                tb = TreeStaticHtml.makeTickBox(type, iliString);
                                if (phraseCount.getPhrase().indexOf("dbpedia")>-1) {
                                    tb = TreeStaticHtml.makeTickBox(type, name, "dbpedia:"+name);
                                    ref = "<a href=\"" + phraseCount.getPhrase() + "\">" + name + ":" + phraseCount.getCount() + tb + "</a>";
                                }
                                else {
                                    ref =  name + ":" + phraseCount.getCount() + tb;
                                }
                                collength += ref.length();
                                phraseString += ref;
                                if (j < phraseCounts.size() - 1) {
                                    phraseString += ", ";
                                }
                                if (collength>colmaxevents) {
                                    phraseString+="\n";
                                    collength =0;
                                }
                            }
                        }
                        phraseString += "]";
                        //str = "<div id=\"cell\"><p>" + phraseString + "</p></div>";
                        str =   "<div id=\"cell2\"  class=\"collapse\"><p>" + phraseString + "</p></div>\n";
                        fos.write(str.getBytes());
                        //str += "<div id=\"cell\"><p>" + phraseCounts.toString()+ "</p></div>";

                    }
                    str = "</div>\n"; // end of accordion
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

    public void  htmlTableTreeORGEVENT (OutputStream fos, String type, String ns, ArrayList<String> tops,
                                 int level,
                                 HashMap<String, Integer> typeCounts,
                                 HashMap<String, ArrayList<PhraseCount>> phrases,
                                 HashMap<String, ArrayList<String>> iliMap) throws IOException {
        String str = "";
        level++;
        ArrayList<PhraseCount> countedTops = new ArrayList<PhraseCount>();
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            Integer cnt = 0;
            if (typeCounts.containsKey(top)) {
                cnt = typeCounts.get(top);
            }
            PhraseCount phraseCount = new PhraseCount(top, cnt);
            countedTops.add(phraseCount);
        }
        Collections.sort(countedTops, new Comparator<PhraseCount>() {
            @Override
            public int compare(PhraseCount p1, PhraseCount p2) {

                return p2.getCount().compareTo(p1.getCount());
            }
        });

        for (int i = 0; i < countedTops.size(); i++) {
            PhraseCount topCount = countedTops.get(i);
            String top = topCount.getPhrase();
            String topName = top;
            str  = "";
            if (top.startsWith(ns) || ns.isEmpty()) {
                Integer cnt = topCount.getCount();
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
                        if (idx > -1) {
                            topName = top.substring(idx + 1);
                        }
                        tb = TreeStaticHtml.makeTickBox(type, topName);
                        //ref = "<a href=\"" + top + "\">" + name + "</a>";
                        ref =  topName;
                    } else if (top.startsWith("dbp:")) {
                        int idx = top.lastIndexOf(":");
                        if (idx > -1) {
                            topName = top.substring(idx + 1);
                        }
                        tb = TreeStaticHtml.makeTickBox(type, topName, top);
                        ref = "<a href=\"http://dbpedia.org/ontology/" + topName + "\">" + topName + "</a>\n";
                    }
                    int instances = 0;
                    if (phrases.containsKey(top)) {
                        ArrayList<PhraseCount> phraseCounts = phrases.get(top);
                        instances = phraseCounts.size();
                    }
                    String toggle = makeToggle(top);

                    if (cnt > 0) {
                        str += "<div id=\"cell\"><p>" + ref + ":" + instances+";"+ cnt+tb+toggle+"</p></div>";
                    } else {
                        str += "<div id=\"cell\"><p>" + ref + tb+toggle+"</p></div>";
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
                        int collength = 0;
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
                                collength += ref.length();
                                phraseString += ref;
                                if (j < phraseCounts.size() - 1) {
                                    phraseString += ", ";
                                }
                                if (collength>colmaxevents) {
                                    phraseString+="\n";
                                    collength =0;
                                }
                            }
                        }
                        phraseString += "]";
                        //str = "<div id=\"cell\"><p>" + phraseString + "</p></div>";
                        str =   "<div id=\""+"collapse"+topName+"\">\n" +
                                "<div id=\"cell2\"  class=\"collapse\"><p>" + phraseString + "</p></div>\n";
                        str += "</div>\n";
                        fos.write(str.getBytes());
                        //str += "<div id=\"cell\"><p>" + phraseCounts.toString()+ "</p></div>";

                    }
                    str = "</div>\n";
                    fos.write(str.getBytes());
                    if (superToSub.containsKey(top)) {
                        ArrayList<String> children = superToSub.get(top);
                        // System.out.println(top+ ":" + cnt+", children:"+children.size());
                        htmlTableTreeORGEVENT(fos, type, ns, children, level, typeCounts, phrases, iliMap);
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

    public void  jsonTree (JSONObject tree, String gType, String ns, ArrayList<String> tops,
                                 int level,
                                 HashMap<String, Integer> typeCounts,
                                 HashMap<String, ArrayList<PhraseCount>> phrases,
                                 HashMap<String, ArrayList<String>> iliMap) throws IOException, JSONException {


        level++;
        ArrayList<PhraseCount> countedTops = new ArrayList<PhraseCount>();
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            Integer cnt = 0;
            if (typeCounts.containsKey(top)) {
                cnt = typeCounts.get(top);
            }
            PhraseCount phraseCount = new PhraseCount(top, cnt);
            countedTops.add(phraseCount);
        }
        Collections.sort(countedTops, new Comparator<PhraseCount>() {
            @Override
            public int compare(PhraseCount p1, PhraseCount p2) {

                return p2.getCount().compareTo(p1.getCount());
            }
        });
        for (int i = 0; i < countedTops.size(); i++) {
            PhraseCount topCount = countedTops.get(i);
            if (topCount.getPhrase().startsWith(ns) || ns.isEmpty()) {
                if (topCount.getCount()>0) {
                    String ref = "";
                    String type = gType;

                    String name = topCount.getPhrase();
                    if (topCount.getPhrase().indexOf("dbpedia")>-1 && topCount.getPhrase().indexOf("resource")>-1) {
                        int idx = topCount.getPhrase().lastIndexOf("/");
                        if (idx > -1) {
                            name = topCount.getPhrase().substring(idx + 1);
                        }
                        ref =  name;
                        type =gType+"Instance";
                    }
                    else if (topCount.getPhrase().startsWith("dbp:")) {
                        int idx = topCount.getPhrase().lastIndexOf(":");
                        if (idx > -1) {
                            name = topCount.getPhrase().substring(idx + 1);
                        }
                        ref = "http://dbpedia.org/ontology/" + name;
                        type = gType+"Type";
                    }
                    else if (topCount.getPhrase().indexOf("ontology")>-1) {
                        int idx = topCount.getPhrase().lastIndexOf(":");
                        if (idx > -1) {
                            name = topCount.getPhrase().substring(idx + 1);
                        }
                        ref = "http://dbpedia.org/ontology/" + name;
                        type = gType+"Type";
                    }
                    else if (topCount.getPhrase().indexOf("eso")>-1) {
                        int idx = topCount.getPhrase().lastIndexOf(":");
                        if (idx > -1) {
                            name = topCount.getPhrase().substring(idx + 1);
                        }
                        ref = "eso:" + name;
                        type = gType+"Type";
                    }
                    else if (topCount.getPhrase().indexOf("eurovoc")>-1) {
                        ref = name;
                        type = gType+"Type";
                    }

                    int instances = 0;
                    if (phrases.containsKey(topCount.getPhrase())) {
                        ArrayList<PhraseCount> phraseCounts = phrases.get(topCount.getPhrase());
                        instances = phraseCounts.size();
                    }

                    JSONObject node = new JSONObject();
                   // node.put("level", new Integer(level).toString());
                    if (!name.isEmpty()) node.put("name", name);
                    node.put("query", topCount.getPhrase());
                    if (!type.isEmpty()) node.put("type", type);
                    if (!ref.isEmpty()) node.put("url", ref);
                    node.put("child_count", instances);
                    node.put("mention_count", topCount.getCount());

                    if (phrases.containsKey(topCount.getPhrase())) {

                        ArrayList<PhraseCount> phraseCounts = phrases.get(topCount.getPhrase());
                        Collections.sort(phraseCounts, new Comparator<PhraseCount>() {
                            @Override
                            public int compare(PhraseCount p1, PhraseCount p2) {
                                return p2.getCount().compareTo(p1.getCount());
                            }
                        });
                        for (int j = 0; j < phraseCounts.size(); j++) {
                            PhraseCount phraseCount = phraseCounts.get(j);
                            if (phraseCount.getCount()>0) {
                                JSONObject phraseCountJsonObject = new JSONObject();
                                int idx = phraseCount.getPhrase().lastIndexOf("/");
                                name = phraseCount.getPhrase().trim();
                                if (idx > -1) {
                                    name = phraseCount.getPhrase().substring(idx + 1);
                                }
                                phraseCountJsonObject.put("name", name.replace("+","_"));
                                phraseCountJsonObject.put("type", gType+"Phrase");
                                if (!name.isEmpty()) phraseCountJsonObject.put("query", name);
                                phraseCountJsonObject.put("mention_count", phraseCount.getCount());
                                /*if (iliMap!=null) {
                                    if (iliMap.containsKey(name)) {
                                        ArrayList<String> ilis = iliMap.get(name);
                                        for (int k = 0; k < ilis.size(); k++) {
                                            String ili = ilis.get(k);
                                            phraseCountJsonObject.append("ili", ili);
                                        }
                                    } else {
                                        //System.out.println("could not find iliString = " + iliString);
                                    }
                                }*/
                                if (!ref.isEmpty()) phraseCountJsonObject.put("parent", ref);
/*                                if (phraseCount.getPhrase().indexOf("dbpedia")>-1) {
                                    phraseCountJsonObject.put("url", phraseCount.getPhrase());
                                }
                                else {
                                }*/
                                node.append("instances", phraseCountJsonObject);
                            }
                        }
                    };
                    if (superToSub.containsKey(topCount.getPhrase())) {
                        ArrayList<String> children = superToSub.get(topCount.getPhrase());
                        // System.out.println(top+ ":" + cnt+", children:"+children.size());
                        jsonTree(node, gType, ns, children, level, typeCounts, phrases, iliMap);
                    }
                    else {
                        //  System.out.println("has no children top = " + top);
                    }
                    tree.append("children", node);
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

    public void  jsonTreeBU (JSONObject tree, String type, String ns, ArrayList<String> tops,
                                 int level,
                                 HashMap<String, Integer> typeCounts,
                                 HashMap<String, ArrayList<PhraseCount>> phrases,
                                 HashMap<String, ArrayList<String>> iliMap) throws IOException, JSONException {


        level++;
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            if (top.startsWith(ns) || ns.isEmpty()) {
                Integer cnt = 0;
                if (typeCounts.containsKey(top)) {
                    cnt = typeCounts.get(top);
                }
                if (cnt>0) {
                    String ref = "";
                    String name = top;
                    if (top.startsWith("http")) {
                        int idx = top.lastIndexOf("/");
                        if (idx > -1) {
                            name = top.substring(idx + 1);
                        }
                        ref =  name;
                    }
                    else if (top.startsWith("dbp:")) {
                        int idx = top.lastIndexOf(":");
                        if (idx > -1) {
                            name = top.substring(idx + 1);
                        }
                        ref = "http://dbpedia.org/ontology/" + name;
                    }

                    int instances = 0;
                    if (phrases.containsKey(top)) {
                        ArrayList<PhraseCount> phraseCounts = phrases.get(top);
                        instances = phraseCounts.size();
                    }

                    JSONObject node = new JSONObject();
                   // node.put("level", new Integer(level).toString());
                    node.put("name", name);
                    node.put("query", top);
                    node.put("type", type);
                    node.put("url", ref);
                    node.put("instance_count", instances);
                    node.put("mention_count", cnt);

                    if (phrases.containsKey(top)) {

                        ArrayList<PhraseCount> phraseCounts = phrases.get(top);
                        Collections.sort(phraseCounts, new Comparator<PhraseCount>() {
                            @Override
                            public int compare(PhraseCount p1, PhraseCount p2) {

                                return p2.getCount().compareTo(p1.getCount());
                            }
                        });
                        for (int j = 0; j < phraseCounts.size(); j++) {
                            PhraseCount phraseCount = phraseCounts.get(j);
                            if (phraseCount.getCount()>0) {
                                JSONObject phraseCountJsonObject = new JSONObject();
                                int idx = phraseCount.getPhrase().lastIndexOf("/");
                                name = phraseCount.getPhrase().trim();
                                if (idx > -1) {
                                    name = phraseCount.getPhrase().substring(idx + 1);
                                }
                                phraseCountJsonObject.put("name", name);
                                phraseCountJsonObject.put("type", type);
                                phraseCountJsonObject.put("query", name);
                                phraseCountJsonObject.put("mention_count", phraseCount.getCount());
                                if (iliMap!=null) {
                                    if (iliMap.containsKey(name)) {
                                        ArrayList<String> ilis = iliMap.get(name);
                                        for (int k = 0; k < ilis.size(); k++) {
                                            String ili = ilis.get(k);
                                            phraseCountJsonObject.append("ili", ili);
                                        }
                                    } else {
                                        //System.out.println("could not find iliString = " + iliString);
                                    }
                                }
                                phraseCountJsonObject.put("parent", ref);
/*                                if (phraseCount.getPhrase().indexOf("dbpedia")>-1) {
                                    phraseCountJsonObject.put("url", phraseCount.getPhrase());
                                }
                                else {
                                }*/
                                node.append("instances", phraseCountJsonObject);
                            }
                        }
                    };
                    if (superToSub.containsKey(top)) {
                        ArrayList<String> children = superToSub.get(top);
                        // System.out.println(top+ ":" + cnt+", children:"+children.size());
                        jsonTree(node, type, ns, children, level, typeCounts, phrases, iliMap);
                    }
                    else {
                        //  System.out.println("has no children top = " + top);
                    }
                    tree.append("children", node);
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
                                HashMap<String, Integer> eventCounts, EuroVoc euroVoc) {
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i).trim();
            if (top.startsWith(ns) || ns.isEmpty()) {
                if (superToSub.containsKey(top)) {
                    ArrayList<String> children = superToSub.get(top);
                    cumulateScores(ns, children, eventCounts, euroVoc);
                    int cCount = 0;
                    for (int j = 0; j < children.size(); j++) {
                        String child =  children.get(j);
                        if (conceptToLabels.containsKey(child)) {
                            ArrayList<String> labels = conceptToLabels.get(child);
                            for (int k = 0; k < labels.size(); k++) {
                                String label =  labels.get(k);
                                if (euroVoc.labelUriMap.containsKey(label)) {
                                    String id = euroVoc.labelUriMap.get(label);
                                    if (eventCounts.containsKey(id)) {
                                        cCount += eventCounts.get(id);
                                    }
                                }
                            }
                        }
                    }
                    if (conceptToLabels.containsKey(top)) {
                        ArrayList<String> labels = conceptToLabels.get(top);
                        for (int k = 0; k < labels.size(); k++) {
                            String label =  labels.get(k);
                            if (euroVoc.labelUriMap.containsKey(label)) {
                                String id = euroVoc.labelUriMap.get(label);
                                if (eventCounts.containsKey(id)) {
                                    cCount += eventCounts.get(id);
                                }
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
                    System.out.println("top count = " + cCount);
                }
                else {
                    //  System.out.println("top = " + top);
                }
            }
            else {
                // System.out.println("ns = " + ns);
                // System.out.println("top = " + top);
            }
        }
    }

    public void cumulateScores (String ns, ArrayList<String> tops,
                                HashMap<String, Integer> eventCounts) {
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i).trim();
            if (top.startsWith(ns) || ns.isEmpty()) {
                if (superToSub.containsKey(top)) {
                    ArrayList<String> children = superToSub.get(top);
                    cumulateScores(ns, children, eventCounts);
                    int cCount = 0;
                    for (int j = 0; j < children.size(); j++) {
                        String child =  children.get(j);
                        if (eventCounts.containsKey(child)) {
                            cCount += eventCounts.get(child);
                        }
                        else {
                           // System.out.println("no counts for child = " + child);
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
                  //  System.out.println("top count = " + cCount);
                }
                else {
                  //  System.out.println("top = " + top);
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
