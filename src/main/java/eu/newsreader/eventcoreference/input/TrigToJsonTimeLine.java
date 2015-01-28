package eu.newsreader.eventcoreference.input;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.newsreader.eventcoreference.objects.JsonEvent;
import eu.newsreader.eventcoreference.util.Util;
import org.apache.jena.riot.RDFDataMgr;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

/**
 * Created by piek on 1/3/14.
 */
public class TrigToJsonTimeLine {


    static final String provenanceGraph = "http://www.newsreader-project.eu/provenance";
    static final String instanceGraph = "http://www.newsreader-project.eu/instances";
    static HashMap<String, ArrayList<Statement>> tripleMapProvenance = new HashMap<String, ArrayList<Statement>>();
    static HashMap<String, ArrayList<Statement>> tripleMapInstances = new HashMap<String, ArrayList<Statement>>();
    static HashMap<String, ArrayList<Statement>> tripleMapOthers = new HashMap<String, ArrayList<Statement>>();
    static HashMap<String, ArrayList<String>> iliMap = new HashMap<String, ArrayList<String>>();



    static void updateOtherStats (Statement s) {
        String predicate = s.getPredicate().getURI();
        String subject = s.getSubject().getURI();
        String instanceTypeSubject = getInstanceType(subject);
        String object = "";
        if (s.getObject().isLiteral()) {
            object = s.getObject().asLiteral().toString();
        }
        else if (s.getObject().isURIResource()) {
            object = s.getObject().asResource().getURI();
        }

        int idx = predicate.lastIndexOf("/");
        if (idx>-1) predicate = predicate.substring(idx+1);
        idx = subject.lastIndexOf("/");
        if (idx>-1) subject = subject.substring(idx+1);
        idx = object.lastIndexOf("/");
        if (idx>-1) object = object.substring(idx+1);

        String predicateSubject = subject +"\t"+predicate;
        String predicateObject = predicate+"\t"+object;



        if (instanceTypeSubject.equals("IEV") && !predicate.equalsIgnoreCase("hastime")) {
            String [] fields = subject.split("-and-");
            for (int i = 0; i < fields.length; i++) {
                String field = fields[i];
                String iliReference = field+"[";
                if (iliMap.containsKey(field)) {
                    ArrayList<String> syns = iliMap.get(field);
                    for (int j = 0; j < syns.size(); j++) {
                        String s1 = syns.get(j);
                        iliReference+=s1;
                    }
                }
                iliReference+="]";
                String crossLingualTriple = iliReference+"\t"+predicate+"\t"+object;
            }
        }
    }


    static String getInstanceType (String subject) {
        String type = "";
        if (subject.indexOf("dbpedia.org")>-1) {
            type = "DBP";
        }
        else if (subject.indexOf("/entities/")>-1) {
            type = "ENT";
        }
        else if (subject.indexOf("ili-30")>-1) {
            type = "IEV";
        }
        else if (subject.indexOf("#ev")>-1) {
            type = "LEV";
        }
        else if (subject.indexOf("http://www.newsreader-project.eu/time/")>-1) {
            type = "DATE";
        }

        return type;
    }


    static boolean hasActor (ArrayList<Statement> triples) {
        for (int i = 0; i < triples.size(); i++) {
            Statement s = triples.get(i);
            String predicate = s.getPredicate().getURI();
            if (predicate.toLowerCase().endsWith("hasactor")) {
                return true;
            }
            else {
              //  System.out.println("predicate = " + predicate);
            }
        }
        return false;
    }

    static String getTimeAnchor (ArrayList<Statement> triples) {
        for (int i = 0; i < triples.size(); i++) {
            Statement statement = triples.get(i);
            String predicate = statement.getPredicate().getURI();
            if (predicate.toLowerCase().endsWith("hastime")) {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                if (tripleMapInstances.containsKey( object)) {
                    ArrayList<Statement> instanceTriples = tripleMapInstances.get(object);
                    for (int j = 0; j < instanceTriples.size(); j++) {
                        Statement timeStatement = instanceTriples.get(j);
                        if (timeStatement.getPredicate().getURI().toLowerCase().endsWith("indatetime")) {
                            if (timeStatement.getObject().isLiteral()) {
                                return timeStatement.getObject().asLiteral().toString();
                            } else if (statement.getObject().isURIResource()) {
                                return timeStatement.getObject().asResource().getURI();
                            }
                        }
                    }
                }

            }
        }
        return "NOTIMEANCHOR";
    }

    //        TimeLanguage.setLanguage(kafSaxParser.getLanguage());

    static ArrayList<JSONObject> getJSONObjectArray() throws JSONException {
        ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<JSONObject>();

        Set keySet = tripleMapOthers.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next(); //// this is the subject of the triple which should point to an event
            ArrayList<Statement> otherTriples = tripleMapOthers.get(key);
            if (!hasActor(otherTriples)) {
               /// we ignore events without actors.....
            }
            else {

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("event", key);
                String timeAnchor = getTimeAnchor(otherTriples);
                // String timeString = semTime.getOwlTime().toString().replaceAll("-", ",");

                jsonObject.put("startDate", timeAnchor);
                jsonObject.put("endDate", timeAnchor);
                if (tripleMapInstances.containsKey( key)) {
                    ArrayList<Statement> instanceTriples = tripleMapInstances.get(key);
                    for (int i = 0; i < instanceTriples.size(); i++) {
                        Statement statement = instanceTriples.get(i);
                        String predicate = statement.getPredicate().getURI();
                        String object = "";
                        if (statement.getObject().isLiteral()) {
                            object = statement.getObject().asLiteral().toString();
                        } else if (statement.getObject().isURIResource()) {
                            object = statement.getObject().asResource().getURI();
                        }
                        jsonObject.put(predicate, object);
                    }
                }
                for (int i = 0; i < otherTriples.size(); i++) {
                    Statement statement = otherTriples.get(i);
                    String predicate = statement.getPredicate().getURI();
                    if (!predicate.toLowerCase().endsWith("hastime")) {
                        String object = "";
                        if (statement.getObject().isLiteral()) {
                            object = statement.getObject().asLiteral().toString();
                        } else if (statement.getObject().isURIResource()) {
                            object = statement.getObject().asResource().getURI();
                        }
                        jsonObject.put(predicate, object);
                    }
                }
                jsonObjectArrayList.add(jsonObject);
            }
        }
        return jsonObjectArrayList;
    }


    static void writeJsonObjectArray (String pathToFolder, String project, ArrayList<JSONObject> objects) {
        try {
            try {
                OutputStream jsonOut = new FileOutputStream(pathToFolder + "/" + "timeline.json");
                JSONObject timeLineObject = JsonEvent.createTimeLineProperty(new File(pathToFolder).getName(), project);

                for (int j = 0; j < objects.size(); j++) {
                    JSONObject jsonObject = objects.get(j);
                    timeLineObject.append("event", jsonObject);
                }
                String str = "{ \"timeline\":\n";
                jsonOut.write(str.getBytes());
                StringWriter out = new StringWriter();
                timeLineObject.write(out);
                jsonOut.write(out.toString().getBytes());
                str ="}\n";
                jsonOut.write(str.getBytes());
                jsonOut.close();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    static public void main (String[] args) {
            String project = "NewsReader timeline";
            String pathToILIfile = "";
            String trigfolder = "";
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals("--trig-folder") && args.length>(i+1)) {
                    trigfolder = args[i+1];
                }
                else if (arg.equals("--ili") && args.length>(i+1)) {
                    pathToILIfile = args[i+1];
                }

            }


            trigfolder = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_airbus/events/contextual";
            pathToILIfile = "/Users/piek/Desktop/NWR/Cross-lingual/wn3-ili-synonyms.txt";
            iliMap = Util.ReadFileToStringHashMap(pathToILIfile);
            Dataset dataset = TDBFactory.createDataset();
            ArrayList<File> trigFiles = Util.makeRecursiveFileList(new File(trigfolder), ".trig");
            ArrayList<String> provenanceTriples = new ArrayList<String>();
            ArrayList<String> instanceTriples = new ArrayList<String>();
            ArrayList<String> otherTriples = new ArrayList<String>();
            for (int i = 0; i < trigFiles.size(); i++) {
                File file = trigFiles.get(i);
                //System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
                dataset = RDFDataMgr.loadDataset(file.getAbsolutePath());
                Iterator<String> it = dataset.listNames();
                while (it.hasNext()) {
                    String name = it.next();
                   // System.out.println("name = " + name);
                    if (name.equals(provenanceGraph)) {
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
                    else if (name.equals(instanceGraph)) {
                        Model namedModel = dataset.getNamedModel(name);
                        StmtIterator siter = namedModel.listStatements();
                        while (siter.hasNext()) {
                            Statement s = siter.nextStatement();
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
                    }
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
                    }
                }
                dataset.close();
            }
        try {
            ArrayList<JSONObject> jsonObjects = getJSONObjectArray();
            writeJsonObjectArray(trigfolder, project, jsonObjects);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }




    static void writeSortedStringArrayList(String fileName, ArrayList<String> result) throws IOException {
        OutputStream fos = new FileOutputStream(fileName);
        TreeSet<String> tree = new TreeSet<String>();
        for (int i = 0; i < result.size(); i++) {
            String s = result.get(i)+"\n";
            tree.add(s);
        }
        Iterator<String> t = tree.iterator();
        while(t.hasNext()) {
            String s = t.next();
            fos.write(s.getBytes());
        }
        fos.close();
    }



    static void writesStats (OutputStream fos, HashMap<String, Integer> map) throws IOException {

        TreeSet<String> tree = new TreeSet<String>();
        Set keySet = map.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            tree.add(key);
        }
        Iterator<String> t = tree.iterator();
        while(t.hasNext()) {
            String s = t.next();
            Integer cnt = map.get(s);
            s += "\t" + cnt.toString() + "\n";
            fos.write(s.getBytes());
        }
    }

    static String getStatementString (Statement s) {
        String str = s.getPredicate().getURI()+"\t"+s.getSubject().getURI()+"\t";
        if (s.getObject().isLiteral()) {
            str += s.getObject().asLiteral();
        }
        else if (s.getObject().isURIResource()) {
            str += s.getObject().asResource().getURI();
        }
        return str;
    }


}
