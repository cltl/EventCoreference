package eu.newsreader.eventcoreference.output;

import com.hp.hpl.jena.rdf.model.Statement;
import eu.newsreader.eventcoreference.input.TrigTripleData;
import eu.newsreader.eventcoreference.input.TrigTripleReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 27/03/16.
 */
public class GetEventCentricGraph {



    static public void main (String[]args) {
        try {
            //String pathToTrigFile = args[0];
            String pathToTrigFile = "/Users/piek/Desktop/CICLing/sem.trig";
            TrigTripleData trigTripleData = TrigTripleReader.readTripleFromTrigFile (new File(pathToTrigFile));
            OutputStream fos = new FileOutputStream(pathToTrigFile+".eckg");

            HashMap<String, ArrayList<String>> properties = new HashMap<String, ArrayList<String>>();
            Set keySet = trigTripleData.tripleMapInstances.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                String event = nameSpaceObject(shortenObject(key));
                String str = event+"\n";
                boolean EVENT = false;
                HashMap<String, ArrayList<String>> predMap = new HashMap<String, ArrayList<String>>();
                ArrayList<Statement> statements = trigTripleData.tripleMapInstances.get(key);
                for (int i = 0; i < statements.size(); i++) {
                    Statement statement = statements.get(i);
                    String subject = statement.getSubject().toString();
                    subject = shortenObject(subject);
                    subject = nameSpaceObject(subject);
                    String predicate = statement.getPredicate().toString();
                    String object = statement.getObject().toString();
                    object = shortenObject(object);
                    object = nameSpaceObject(object);
                    predicate = normalisePredicate(predicate);
                    if (!predicate.isEmpty()) {
                        if (statement.getObject().toString().endsWith("sem/Event")) {
                           EVENT = true;
                        }
                        if (predicate.startsWith("time:inDateTime")) {
                            updatePredicateMap(properties,subject, object);
                            //time:inDateTime
                        }
                        else if (predicate.startsWith("rdfs:label")) {
                            updatePredicateMap(properties,subject, object);
                            //time:inDateTime
                        }
                        updatePredicateMap(predMap, predicate, object);
                    }
                }
                if (trigTripleData.tripleMapOthers.containsKey(key)) {
                    ArrayList<Statement> relations = trigTripleData.tripleMapOthers.get(key);
                    for (int i = 0; i < relations.size(); i++) {
                        Statement statement = relations.get(i);
                        String predicate = statement.getPredicate().toString();
                        String object = statement.getObject().toString();
                        object = nameSpaceObject(object);
                        object = shortenObject(object);
                        if (!object.startsWith("nwr:non-entities")) {
                            predicate = normalisePredicate(predicate);
                            if (!predicate.isEmpty()) {
                                updatePredicateMap(predMap, predicate, object);
                            }
                        }
                    }
                }
                if (EVENT) {
                    Set predSet = predMap.keySet();
                    Iterator<String> predKeys = predSet.iterator();
                    while (predKeys.hasNext()) {
                        String predKey = predKeys.next();
                        ArrayList<String> objs = predMap.get(predKey);
                        str += "\t"+predKey+"\t";
                        for (int i = 0; i < objs.size(); i++) {
                            String s = objs.get(i);
                            str += s;

                            String normValue = "";
                            //System.out.println("s = " + s);
                            if (properties.containsKey(s)) {
                                ArrayList<String> entityProperties = properties.get(s);
                                for (int j = 0; j < entityProperties.size(); j++) {
                                    String eProperty =  entityProperties.get(j);
                                    if (!normValue.isEmpty()) {
                                        normValue += " , "+eProperty;
                                    }
                                    else {
                                        normValue += eProperty;

                                    }

                                }
                              //  System.out.println("normValue = " + normValue);
                            }
                            else {
                               // System.out.println("no properties for s = " + s);
                            }
                            if (!normValue.isEmpty()) {
                               str += " ("+normValue+")";
                                System.out.println("normValue = " + normValue);
                            }

                            if (i<objs.size()-1) {
                                str += " , ";
                            }
                        }
                        str += " ;\n";
                    }
                    str +="\n";
                    fos.write(str.getBytes());
                }
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void updatePredicateMap (HashMap<String, ArrayList<String>> predMap, String predicate, String object) {
        if (predMap.containsKey(predicate)) {
            ArrayList<String> objs = predMap.get(predicate);
            objs.add(object);
            predMap.put(predicate, objs);
        }
        else {
            ArrayList<String> objs = new ArrayList<String>();
            objs.add(object);
            predMap.put(predicate, objs);
        }
    }

    static public String normalisePredicate (String predicate) {
        String normPred = "";
        if (predicate.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))  {
            normPred = "a";
        }
        else if (predicate.equals("http://semanticweb.cs.vu.nl/2009/11/sem/hasTime"))  {
            normPred = "sem:hasTime";
        }
        else if (predicate.equals("http://semanticweb.cs.vu.nl/2009/11/sem/hasActor"))  {
            normPred = "sem:hasActor";
        }
        else if (predicate.equals("http://semanticweb.cs.vu.nl/2009/11/sem/hasPlace"))  {
            normPred = "sem:hasActor";
        }
        else if (predicate.equals("http://www.w3.org/2000/01/rdf-schema#label"))  {
            normPred = "rdfs:label";
        }
        else if (predicate.equals("http://www.w3.org/2004/02/skos/core#prefLabel"))  {
            normPred = "skos:prefLabel";
        }
        else if (predicate.equals("http://groundedannotationframework.org/gaf#denotedBy"))  {
            normPred = "gaf:denotedBy";
        }
        else if (predicate.equals("http://www.w3.org/TR/owl-time#inDateTime"))  {
            normPred = "time:inDateTime";
        }
        else {
           // System.out.println("predicate = " + predicate);
        }
        return normPred;
    }

    static public String nameSpaceObject (String object) {
        String prefacedObject = object;
        if (object.startsWith("http://www.newsreader-project.eu/data/ecb/"))  {
            int idx = "http://www.newsreader-project.eu/data/ecb/".length();
            prefacedObject = "nwr:"+object.substring(idx);
        }
        else if (object.startsWith("http://globalwordnet.org/ili/"))  {
            int idx = "http://globalwordnet.org/ili/".length();
            prefacedObject = "ili:"+object.substring(idx);
        }
        else if (object.startsWith("http://www.newsreader-project.eu/ontologies/framenet/"))  {
            int idx = "http://www.newsreader-project.eu/ontologies/framenet/".length();
            prefacedObject = "fn:"+object.substring(idx);
        }
        else if (object.startsWith("http://semanticweb.cs.vu.nl/2009/11/sem/"))  {
            int idx = "http://semanticweb.cs.vu.nl/2009/11/sem/".length();
            prefacedObject = "sem:"+object.substring(idx);
        }
        else if (object.startsWith("http://dbpedia.org/resource"))  {
            int idx = "hhttp://dbpedia.org/resource".length();
            prefacedObject = "dbp:"+object.substring(idx);
        }
        else if (object.startsWith("http://www.newsreader-project.eu/ontologies/"))  {
            int idx = "http://www.newsreader-project.eu/ontologies/".length();
            prefacedObject = "nwr:"+object.substring(idx);
        }
        else if (object.startsWith("http://www.newsreader-project.eu/domain-ontology#"))  {
            int idx = "http://www.newsreader-project.eu/domain-ontology#".length();
            prefacedObject = "nwr:"+object.substring(idx);
        }
        else if (object.startsWith("http://www.newsreader-project.eu/time/"))  {
            int idx = "http://www.newsreader-project.eu/time/".length();
            prefacedObject = "time:"+object.substring(idx);
        }
        return prefacedObject;
    }


    static public String shortenObject (String object) {
        String cleanString = object;
        cleanString = cleanString.replace(".xml.naf.fix.coref", "");
        cleanString = cleanString.replace(".xml.naf.fix.xml.coref", "");
        cleanString = cleanString.replace(".xml.naf.fix.xml.newpred.coref", "");
        //nrw:45_5ecbplus#char=475,483&word=w89&term=t89&sentence=3
        int idx = cleanString.indexOf("&");
        if (idx>-1) {
            cleanString = cleanString.substring(0, idx);
        }
        return cleanString;
    }
}
