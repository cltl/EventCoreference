package eu.newsreader.eventcoreference.input;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by piek on 23/06/15.
 */
public class TrigUtil {

    static final String provenanceGraph = "http://www.newsreader-project.eu/provenance";
    static final String instanceGraph = "http://www.newsreader-project.eu/instances";

    static ArrayList<String> getAllEntityEvents (Dataset dataset, String entity) {
        ArrayList<String> events = new ArrayList<String>();
        Iterator<String> it = dataset.listNames();
        while (it.hasNext()) {
            String name = it.next();
            if (!name.equals(instanceGraph) && (!name.equals(provenanceGraph))) {
                Model namedModel = dataset.getNamedModel(name);
                StmtIterator siter = namedModel.listStatements();
                while (siter.hasNext()) {
                    Statement s = siter.nextStatement();
                    String object = getObjectValue(s);
                    if (object.indexOf(entity) > -1) {
                        String subject = s.getSubject().getURI();
                        if (!events.contains(subject)) {
                            events.add(subject);
                        }
                    }
                }
            }
        }
        return events;
    }

    static void getAllEntityEventTriples (Dataset dataset,
                                          ArrayList<String> events,
                                          HashMap<String, ArrayList<Statement>> eventMap) {
        HashMap<String, ArrayList<Statement>> triples = new HashMap<String, ArrayList<Statement>>();
        Iterator<String> it = dataset.listNames();
        while (it.hasNext()) {
            String name = it.next();
            if (!name.equals(instanceGraph) && (!name.equals(provenanceGraph))) {
                Model namedModel = dataset.getNamedModel(name);
                StmtIterator siter = namedModel.listStatements();
                while (siter.hasNext()) {
                    Statement s = siter.nextStatement();
                    if (validTriple (s)) {
                        String subject = s.getSubject().getURI();
                        if (events.contains(subject)) {
                            if (triples.containsKey(subject)) {
                                ArrayList<Statement> statements = triples.get(subject);
                                if (!hasStatement(statements, s)) {
                                    statements.add(s);
                                    eventMap.put(subject, statements);
                                }
                            } else {
                                ArrayList<Statement> statements = new ArrayList<Statement>();
                                statements.add(s);
                                eventMap.put(subject, statements);
                            }
                        }
                    }
                }
            }
        }
    }


    static String triplesToString (ArrayList<Statement> statements, String entity) {
        String str = "";
        String eventLabels = "";
        String roles = "";
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (!getObjectValue(statement).contains(entity)) {
                if (statement.getPredicate().toString().toLowerCase().contains("#label")) {
                    if (!eventLabels.isEmpty()) {
                        eventLabels += ",";
                    }
                    eventLabels += getObjectValue(statement);
                } else {
                    roles += "\t" + getValue(statement.getPredicate().toString())
                            + ":" + getObjectValue(statement);
                }
            }
            str = eventLabels+roles+"\n";
        }
        return str;
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


    static boolean hasStatement (ArrayList<Statement> statements, Statement s) {
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getSubject().equals(s.getSubject()) &&
                    statement.getPredicate().equals(s.getPredicate()) &&
                    statement.getObject().equals(s.getObject())) {
                return true;
            }
        }

        return false;
    }

    static boolean validTriple (Statement s) {
        if (s.getPredicate().toString().toLowerCase().contains("propbank")) {
            return true;
        }/*
        else if (s.getPredicate().toString().toLowerCase().contains("hastime")) {
            return true;
        }*/
        else if (s.getPredicate().toString().toLowerCase().contains("#label")) {
            return true;
        }
        else if (s.getPredicate().toString().toLowerCase().contains("#denotedby")) {
            return true;
        }/*
        else if (s.getPredicate().toString().toLowerCase().contains("#type") &&
                 s.getObject().toString().toLowerCase().contains("framenet")) {
            return true;
        }*/
        /*else if (s.getPredicate().toString().toLowerCase().contains("#type") &&
                 s.getObject().toString().toLowerCase().contains("domain-ontology")) {
            return true;
        }*/
        else {
            return false;
        }
    }



    static boolean gafTriple (Statement s) {
        if (s.getPredicate().toString().toLowerCase().contains("#denotedby")) {
            return true;
        }
        else {
            return false;
        }
    }

    static String getObjectValue (Statement statement) {
        String object = "";
        String value = "";
        if (statement.getObject().isLiteral()) {
            value = statement.getObject().asLiteral().toString();
        } else if (statement.getObject().isURIResource()) {
            value = statement.getObject().asResource().getLocalName();
        }
/*        int idx = value.lastIndexOf("/");
        if (idx>-1) {
            value = value.substring(value.lastIndexOf("/"));
        }*/
        value = getValue(statement.getObject().toString());
        String nameSpace =  getNameSpaceString(statement.getObject().toString());
        if (nameSpace.isEmpty()) {
            object = value;
        }
        else {
            object = nameSpace+":" + value;
        }
        //  System.out.println("object = " + object);
        return object;
    }

/*
    static String getObjectValue (Statement statement) {
        String object = "";
        if (statement.getObject().isLiteral()) {
            object = statement.getObject().asLiteral().toString();
        } else if (statement.getObject().isURIResource()) {
            object = statement.getObject().asResource().getURI();
        }
        return object;
    }
*/



    static String triplesToString (ArrayList<Statement> statements) {
        String str = "";
        String eventLabels = "";
        String roles = "";
        String gaf = "";
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getPredicate().toString().toLowerCase().contains("#label")) {
                if (!eventLabels.isEmpty()) {
                    eventLabels += ",";
                }
                eventLabels += getObjectValue(statement);
            } else {
                if (gafTriple(statement)) {
                    if (!gaf.isEmpty()) {
                        gaf+= "; ";
                    }
                    gaf += getMention(statement.getObject().toString());
                }
                else {
                    roles += "\t" + getValue(statement.getPredicate().toString())
                            + ":" + getObjectValue(statement);
                }
            }
            str = eventLabels+roles+"\t"+gaf+"\n";
        }
        return str;
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
        else if (value.indexOf("/domain-ontology/") > -1) {
            property = "eso";
        }
        else if (value.indexOf("/domain-ontology") > -1) {
            property = "eso";
        }
        else if (value.indexOf("/dbpedia") > -1) {
            property = "dbp";
        }
        else if (value.indexOf("http://www.newsreader-project.eu/data/") > -1) {
            property = "nwr";
        }
        else if (value.indexOf("/non-entities") > -1) {
            property = "nwr-non-entity";
        }
        else if (value.indexOf("/entities/") > -1) {
            property = "nwr-entity";
        }
        else if (value.indexOf("ili-30") > -1) {
            property = "wn";
        }
        return property;
    }

    static String getMention(String mention) {
        String mentionValue = getNameSpaceString(mention);
        String value = mention;
        int idx = mention.lastIndexOf("/");
        if (idx>-1) {
            value = mention.substring(idx + 1);
        }
        mentionValue += ":"+value;
        return mentionValue;
    }


}
