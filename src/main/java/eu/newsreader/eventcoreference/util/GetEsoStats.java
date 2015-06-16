package eu.newsreader.eventcoreference.util;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.newsreader.eventcoreference.input.EsoReader;
import org.apache.jena.riot.RDFDataMgr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 15/06/15.
 */
public class GetEsoStats {



    static final String provenanceGraph = "http://www.newsreader-project.eu/provenance";
    static final String instanceGraph = "http://www.newsreader-project.eu/instances";
    static HashMap<String, ArrayList<Statement>> tripleMapProvenance = new HashMap<String, ArrayList<Statement>>();
    static HashMap<String, ArrayList<Statement>> tripleMapInstances = new HashMap<String, ArrayList<Statement>>();
    static HashMap<String, ArrayList<Statement>> tripleMapOthers = new HashMap<String, ArrayList<Statement>>();
    static HashMap<String, Integer> esoCounts = new HashMap<String, Integer>();
    static HashMap<String, Integer> fnCounts = new HashMap<String, Integer>();
    static String ACTORNAMESPACES = "";
    static EsoReader esoReader = new EsoReader();
    static HashMap<String, Integer> eventCountsPerDate = new HashMap<String, Integer>();
    static HashMap<String, Integer> eventMentionCountsPerDate = new HashMap<String, Integer>();

    static public void main (String[] args) {

        String trigfolderPath = "";
        String esoFile = "/Users/piek/Desktop/NWR/timeline/vua-naf2jsontimeline_2015/resources/ESO_version_0.6.owl";
       // trigfolderPath = "/Users/piek/Desktop/tweede-kamer/events/contextual";
        trigfolderPath = "/Users/piek/Desktop/KIEM/events/contextual";

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--trig-folder") && args.length>(i+1)) {
                trigfolderPath = args[i+1];
            }
            else if (arg.equals("--actors") && args.length>(i+1)) {
                ACTORNAMESPACES = args[i+1];
                // System.out.println("ACTORNAMESPACES = " + ACTORNAMESPACES);
            }
        }

        if (!esoFile.isEmpty()) {
            esoReader.parseFile(esoFile);
        }
        File trigfolder = new File(trigfolderPath);
        Dataset dataset = TDBFactory.createDataset();
        ArrayList<File> trigFiles = Util.makeRecursiveFileList(trigfolder, ".trig");
        System.out.println("trigFiles.size() = " + trigFiles.size());
        ArrayList<String> provenanceTriples = new ArrayList<String>();
        ArrayList<String> instanceTriples = new ArrayList<String>();
        ArrayList<String> otherTriples = new ArrayList<String>();
        for (int i = 0; i < trigFiles.size(); i++) {
            File file = trigFiles.get(i);
            if (i%500==0) {
                System.out.println("i = " + i);
               // break;
            }
            String timeDescription = file.getParentFile().getName();
            int idx = timeDescription.indexOf("-");
            if (idx>-1) {
                timeDescription = timeDescription.substring(idx+1);
                if (timeDescription.length()>=4) {
                    timeDescription = timeDescription.substring(0,4);
                }
            }
           // System.out.println("timeDescription = " + timeDescription);
          //  System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
            dataset = RDFDataMgr.loadDataset(file.getAbsolutePath());
            Iterator<String> it = dataset.listNames();
            while (it.hasNext()) {
                String name = it.next();

/*                if (name.equals(provenanceGraph)) {
                    Model namedModel = dataset.getNamedModel(name);
                    StmtIterator siter = namedModel.listStatements();
                    while (siter.hasNext()) {
                        Statement s = siter.nextStatement();
                        String subject = s.getSubject().getURI();
                        if (tripleMapProvenance.containsKey(subject)) {
                            ArrayList<Statement> triples = tripleMapProvenance.get(subject);
                            triples.add(s);
                            tripleMapProvenance.put(subject, triples);
                        }
                        else {
                            ArrayList<Statement> triples = new ArrayList<Statement>();
                            triples.add(s);
                            tripleMapProvenance.put(subject, triples);
                        }
                    }
                }
                else */
                if (name.equals(instanceGraph)) {
                    Model namedModel = dataset.getNamedModel(name);
                    StmtIterator siter = namedModel.listStatements();
                    while (siter.hasNext()) {
                        Statement s = siter.nextStatement();
                        if (isEventInstance(s)) {
                           if (eventCountsPerDate.containsKey(timeDescription)) {
                               Integer cnt = eventCountsPerDate.get(timeDescription);
                               cnt++;
                               eventCountsPerDate.put(timeDescription, cnt);
                           }
                           else {
                               eventCountsPerDate.put(timeDescription, 1);
                           }
                        }
                        String subject = s.getSubject().getURI();
                        if (tripleMapInstances.containsKey(subject)) {
                            ArrayList<Statement> triples = tripleMapInstances.get(subject);
                            triples.add(s);
                            tripleMapInstances.put(subject, triples);
                        }
                        else {

                            ArrayList<Statement> triples = new ArrayList<Statement>();
                            triples.add(s);
                            tripleMapInstances.put(subject, triples);
                        }
                    }
                }/*
                else {
                    Model namedModel = dataset.getNamedModel(name);
                    StmtIterator siter = namedModel.listStatements();
                    while (siter.hasNext()) {
                        Statement s = siter.nextStatement();
                        String subject = s.getSubject().getURI();
                        if (tripleMapOthers.containsKey(subject)) {
                            ArrayList<Statement> triples = tripleMapOthers.get(subject);
                            triples.add(s);
                            tripleMapOthers.put(subject, triples);
                        }
                        else {

                            ArrayList<Statement> triples = new ArrayList<Statement>();
                            triples.add(s);
                            tripleMapOthers.put(subject, triples);
                        }
                    }
                }*/
            }
            dataset.close();
            dataset = null;
        }
        getClassStats();
        try {
            OutputStream fos = new FileOutputStream(trigfolder.getParentFile()+"/"+trigfolder.getName()+".eso-stats.csv");

            String str = "esoCounts\t" + esoCounts.size()+"\n";
            fos.write(str.getBytes());
            Set keySet = esoCounts.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                Integer cnt = esoCounts.get(key);
                str = key+"\t"+cnt+"\n";
                fos.write(str.getBytes());
            }
            fos.close();

            fos = new FileOutputStream(trigfolder.getParentFile()+"/"+trigfolder.getName()+".eso-hierarchy.txt");
            ArrayList<String> topNodes = esoReader.getTops();
            printTree(esoReader, topNodes, 0, fos);
            fos.close();

            fos = new FileOutputStream(trigfolder.getParentFile()+"/"+trigfolder.getName()+".fn-stats.csv");
            str = "fnCounts\t" + fnCounts.size()+"\n";
            fos.write(str.getBytes());
            keySet = fnCounts.keySet();
            keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                Integer cnt = fnCounts.get(key);
                str = key+"\t"+cnt+"\n";
                fos.write(str.getBytes());
            }
            fos.close();

            fos = new FileOutputStream(trigfolder.getParentFile()+"/"+trigfolder.getName()+".event-date.csv");
            keySet = eventCountsPerDate.keySet();
            keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                Integer cnt = eventCountsPerDate.get(key);
                str = key+"\t"+cnt+"\n";
                fos.write(str.getBytes());
            }
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static boolean isEventInstance (Statement statement) {
        String predicate = statement.getPredicate().getURI();
       // System.out.println("predicate = " + predicate);
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
                String property = getNameSpaceString(value);
              //  System.out.println("value = " + value);
             //   System.out.println("property = " + property);
                if (value.endsWith("Event") && property.equalsIgnoreCase("sem")) {
                    return true;
                }
            }
        }
        return false;
    }

    static int mentionCounts (Statement statement) {
        int cnt = 0;
        String predicate = statement.getPredicate().getURI();
        if (predicate.endsWith("#denotedBy")) {
            String object = "";
            if (statement.getObject().isLiteral()) {
                object = statement.getObject().asLiteral().toString();
            } else if (statement.getObject().isURIResource()) {
                object = statement.getObject().asResource().getURI();
            }
            String[] values = object.split(",");
            cnt = values.length;

        }
        return cnt;
    }

    static void getClassStats () {
        Set keySet = tripleMapInstances.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next(); //// this is the subject of the triple which should point to an event
            ArrayList<Statement> statements = tripleMapInstances.get(key);

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
                        String [] values = object.split(",");
                        for (int j = 0; j < values.length; j++) {
                            String value = values[j];
                            String property = getNameSpaceString(value);
                            if (!property.isEmpty() && !property.equalsIgnoreCase("sem")) {
                                value = getValue(value);
                                if (property.equals("eso")) {
                                    if (esoCounts.containsKey(value)) {
                                        Integer cnts = esoCounts.get(value);
                                        cnts++;
                                        esoCounts.put(value, cnts);

                                    } else {
                                       esoCounts.put(value, 1);
                                    }
                                }
                                else if (property.equals("fn")) {
                                    if (fnCounts.containsKey(value)) {
                                        Integer cnts = fnCounts.get(value);
                                        cnts++;
                                        fnCounts.put(value, cnts);

                                    } else {
                                        fnCounts.put(value, 1);
                                    }
                                }
                            }
                        }
                    }
                }
        }

    }



    static String getNameSpaceString (String value) {
        String property = "";
        if (value.indexOf("/framenet/") > -1) {
            property = "fn";
        }
        else if (value.indexOf("/propbank/") > -1) {
            property = "pb";
        }
        else if (value.indexOf("/sem/") > -1) {
            property = "sem";
        }
        else if (value.indexOf("/sumo/") > -1) {
            property = "sumo";
        }
        else if (value.indexOf("/eso/") > -1) {
            property = "eso";
        }
        else if (value.indexOf("/domain-ontology") > -1) {
            property = "eso";
        }
        else if (value.indexOf("ili-30") > -1) {
            property = "wn";
        }
        return property;
    }

    static String getValue (String predicate) {
        int idx = predicate.lastIndexOf("#");
        if (idx>-1) {
            return predicate.substring(idx + 1);
        }
        else {
            idx = predicate.lastIndexOf("/");
            if (idx>-1) {
                return predicate.substring(idx + 1);
            }
            else {
                return predicate;
            }
        }
    }


    public static void printTree(EsoReader esoReader, ArrayList<String> tops, int level, OutputStream fos) throws IOException {
        level++;
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            int count = 0;
            if (esoCounts.containsKey(top)) {
                count = esoCounts.get(top);
            }
            String str = "";
            for (int j = 0; j < level; j++) {
                str += "  ";

            }
            if (esoReader.superToSub.containsKey(top)) {
                ArrayList<String> children = esoReader.superToSub.get(top);


                str += top + ":" + count+"\n";
                fos.write(str.getBytes());
               // System.out.println(str);
                printTree(esoReader, children, level, fos);
            }
            else {
                str += top + ":" + count+"\n";
                fos.write(str.getBytes());
            }
        }
    }

}
