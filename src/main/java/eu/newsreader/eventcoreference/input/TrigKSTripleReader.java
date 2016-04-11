package eu.newsreader.eventcoreference.input;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.*;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;

import java.util.ArrayList;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createStatement;

/**
 * Created by filipilievski on 2/7/16.
 */
public class TrigKSTripleReader {


    //final static String serviceEndpoint = "https://knowledgestore2.fbk.eu/nwr/wikinews/sparql";
    public static String serviceEndpoint = "https://knowledgestore2.fbk.eu/nwr/wikinews/sparql";
    //public static String serviceEndpoint = "https://knowledgestore2.fbk.eu/nwr/cars3/sparql";
    public static String user = "nwr_partner";
    public static String pass = "ks=2014!";
    public static String limit = "500";
    //public static String authStr = user + ":" + pass;

    HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());

    static public void setServicePoint (String ks) {
        serviceEndpoint = "https://knowledgestore2.fbk.eu/"+ks+"/sparql";
    }

    static public void setServicePoint (String ks, String username, String password) {
        serviceEndpoint = "https://knowledgestore2.fbk.eu/"+ks+"/sparql";
        user = username;
        pass = password;
    }


    static public ArrayList<Statement> getSubjectProperties (String subjectUri) {
        //SELECT ?s ?rel ?obj WHERE {<http://www.ft.com/thing/0013db8a-e6db-11e5-a09b-1f8b0d268c39#char=18,24> ?rel ?obj} LIMIT 50
        //SELECT ?s ?rel ?obj WHERE {<http://www.ft.com/thing/0013db8a-e6db-11e5-a09b-1f8b0d268c39/doc_attribution/Attr0> ?rel ?obj} LIMIT 50
        ArrayList<Statement> triples = new ArrayList<Statement>();

      //  System.out.println("subject = " + subject);
        String sparqlQuery = makeTripleQuery(subjectUri);
        triples = readTriplesFromKs (subjectUri, sparqlQuery);
        for (int i = 0; i < triples.size(); i++) {
            Statement statement = triples.get(i);
            String relString = statement.getPredicate().toString();
            String objUri = statement.getObject().toString();
            if (isAttributionRelation(relString)) {
                sparqlQuery = makeTripleQuery(objUri);
                ArrayList<Statement> attrTriples = readTriplesFromKs(objUri, sparqlQuery);
                for (int j = 0; j < attrTriples.size(); j++) {
                    Statement attrStatement =  attrTriples.get(j);
                    triples.add(attrStatement);
                }
            }
            else if (isProvRelation(relString)) {
                /// in this case the object is a document URI
                sparqlQuery = makeTripleQuery(objUri);
                ArrayList<Statement> docTriples = readTriplesFromKs(objUri, sparqlQuery);
                for (int j = 0; j < docTriples.size(); j++) {
                    Statement docStatement =  docTriples.get(j);
                    triples.add(docStatement);
                }
            }
        }
        return triples;
    }

    static public String makeTripleQuery (String subjectUri) {
        String subject = subjectUri;
        if (!subjectUri.startsWith("<")) subject = "<"+subject+">";
        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT ?subject ?predicate ?object WHERE { "+subject+" ?predicate ?object} LIMIT 10";
        return sparqlQuery;
    }


    public static ArrayList<Statement> readTriplesFromKs(String subjectUri, String sparqlQuery){

        ArrayList<Statement> triples = new ArrayList<Statement>();
        HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());
        try {
            QueryExecution x = QueryExecutionFactory.sparqlService(serviceEndpoint, sparqlQuery, authenticator);
            ResultSet resultset = x.execSelect();
            while (resultset.hasNext()) {
                QuerySolution solution = resultset.nextSolution();
                String relString = solution.get("predicate").toString();
                RDFNode obj = solution.get("object");
                Model model = ModelFactory.createDefaultModel();
                Resource subj = model.createResource(subjectUri);
                Statement s = createStatement(subj, ResourceFactory.createProperty(relString), obj);
                triples.add(s);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return triples;
    }



    static public TrigTripleData readTriplesFromKSforEventType(String eventType){
        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX eso: <http://www.newsreader-project.eu/domain-ontology#> \n" +
                "PREFIX fn: <http://www.newsreader-project.eu/ontologies/framenet/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT ?event ?relation ?object ?indatetime ?begintime ?endtime \n" +
                "WHERE {\n" +
                "{SELECT distinct ?event WHERE { \n" +
                "?event rdf:type " + eventType + " .\n" +
                "} LIMIT "+limit+" }\n" +
                "?event ?relation ?object .\n" +
                "OPTIONAL { ?object rdf:type owltime:Instant ; owltime:inDateTime ?indatetime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasBeginning ?begintime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasEnd ?endtime }" +
                "} ORDER BY ?event";
       // System.out.println("sparqlQuery = " + sparqlQuery);
        return readTriplesFromKs(sparqlQuery);
    }

    static public TrigTripleData readTriplesFromKSforTopic(String topic){
        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n" +
                "PREFIX eurovoc: <http://eurovoc.europa.eu/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT ?event ?relation ?object ?indatetime ?begintime ?endtime \n" +
                "WHERE {\n" +
                "{SELECT distinct ?event WHERE { \n" +
                "?event skos:relatedMatch eurovoc:" + topic + " .\n" +
                "} LIMIT "+limit+" }\n" +
                "?event ?relation ?object .\n" +
                "OPTIONAL { ?object rdf:type owltime:Instant ; owltime:inDateTime ?indatetime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasBeginning ?begintime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasEnd ?endtime }" +
                "} ORDER BY ?event";
       // System.out.println("sparqlQuery = " + sparqlQuery);
        return readTriplesFromKs(sparqlQuery);
    }

    static public TrigTripleData readTriplesFromKSforEntity(String entityLabel){
        return readTriplesFromKSforEntity(entityLabel, "");
    }

    static public TrigTripleData readTriplesFromKSforEventEntityType(String eventType, String entityType){
        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX eso: <http://www.newsreader-project.eu/domain-ontology#> \n" +
                "PREFIX fn: <http://www.newsreader-project.eu/ontologies/framenet/> \n" +
                "PREFIX dbp: <http://dbpedia.org/ontology/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT ?event ?relation ?object ?indatetime ?begintime ?endtime \n" +
                "WHERE {\n" +
                "{SELECT distinct ?event WHERE { \n" +
                "?event rdf:type " + eventType + " .\n" +
                "?event sem:hasActor ?ent .\n" +
                "?ent rdf:type " + entityType + " .\n" +
                "} LIMIT "+limit+" }\n" +
                "?event ?relation ?object .\n" +
                "OPTIONAL { ?object rdf:type owltime:Instant ; owltime:inDateTime ?indatetime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasBeginning ?begintime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasEnd ?endtime }" +
                "} ORDER BY ?event";
        //System.out.println("sparqlQuery = " + sparqlQuery);
        return readTriplesFromKs(sparqlQuery);
    }

    static public TrigTripleData readTriplesFromKSforEntity(String entityLabel, String filter){


        String eventFilter = "";
        if (filter.equals("eso")){
            eventFilter = "FILTER EXISTS { ?event rdf:type ?type .\n" +
                    "?type <http://www.w3.org/2000/01/rdf-schema#isDefinedBy> <http://www.newsreader-project.eu/domain-ontology#> . }\n";
        } else if (filter.equals("fn")){
            eventFilter = "FILTER EXISTS { ?event rdf:type ?type .\n" +
                    "?type <http://www.w3.org/2000/01/rdf-schema#isDefinedBy> <http://www.newsreader-project.eu/ontologies/framenet/> . }\n";
        }

        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT ?event ?relation ?object ?indatetime ?begintime ?endtime \n" +
                "WHERE {\n" +
                "{SELECT distinct ?event WHERE { \n" +
                "FILTER regex(str(?entlabel), \"^" + entityLabel + "$\") .\n" +
                "?ent rdfs:label ?entlabel .\n" +
                "?event sem:hasActor ?ent .\n" +
                eventFilter +
                "} LIMIT "+limit+" }\n" +
                "?event ?relation ?object .\n" +
                "OPTIONAL { ?object rdf:type owltime:Instant ; owltime:inDateTime ?indatetime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasBeginning ?begintime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasEnd ?endtime }" +
                "} ORDER BY ?event";
        //System.out.println("sparqlQuery = " + sparqlQuery);
        return readTriplesFromKs(sparqlQuery);
    }


    public static TrigTripleData readTriplesFromKs(String sparqlQuery){

        TrigTripleData trigTripleData = new TrigTripleData();
        HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());

        Property inDateTimeProperty = ResourceFactory.createProperty("http://www.w3.org/TR/owl-time#inDateTime");
        Property beginTimeProperty = ResourceFactory.createProperty("http://www.w3.org/TR/owl-time#hasBeginning");
        Property endTimeProperty = ResourceFactory.createProperty("http://www.w3.org/TR/owl-time#hasEnd");


        QueryExecution x = QueryExecutionFactory.sparqlService(serviceEndpoint, sparqlQuery, authenticator);
        ResultSet resultset = x.execSelect();
        String oldEvent="";
        ArrayList<Statement> instanceRelations = new ArrayList<Statement>();
        ArrayList<Statement> otherRelations = new ArrayList<Statement>();
        while (resultset.hasNext()) {
            QuerySolution solution = resultset.nextSolution();
            String relString = solution.get("relation").toString();
            String currentEvent = solution.get("event").toString();
            RDFNode obj = solution.get("object");
            String objUri = obj.toString();
            Statement s = createStatement((Resource) solution.get("event"), ResourceFactory.createProperty(relString), obj);
            if (isSemRelation(relString) || isESORelation(relString) || isFNRelation(relString) || isPBRelation(relString))
            {
                otherRelations.add(s);
                if (isSemTimeRelation(relString)) {
                    if (solution.get("indatetime")!=null){
//                            System.out.println("in " + solution.get("indatetime"));
                        String uri = ((Resource) obj).getURI();
                        Statement s2 = createStatement((Resource) obj, inDateTimeProperty, solution.get("indatetime"));
                        if (trigTripleData.tripleMapInstances.containsKey(uri)) {
                            ArrayList<Statement> triples = trigTripleData.tripleMapInstances.get(uri);
                            triples.add(s2);
                            trigTripleData.tripleMapInstances.put(uri, triples);
                        } else {
                            ArrayList<Statement> triples = new ArrayList<Statement>();
                            triples.add(s2);
                            trigTripleData.tripleMapInstances.put(uri, triples);
                        }
                    }
                    else {
                        if (solution.get("begintime")!=null){
//                            System.out.println("begin " + solution.get("begintime"));
                            String uri = ((Resource) obj).getURI();
                            Statement s2 = createStatement((Resource) obj, beginTimeProperty, solution.get("begintime"));
                            if (trigTripleData.tripleMapInstances.containsKey(uri)) {
                                ArrayList<Statement> triples = trigTripleData.tripleMapInstances.get(uri);
                                triples.add(s2);
                                trigTripleData.tripleMapInstances.put(uri, triples);
                            } else {
                                ArrayList<Statement> triples = new ArrayList<Statement>();
                                triples.add(s2);
                                trigTripleData.tripleMapInstances.put(uri, triples);
                            }
                        }
                        else if (solution.get("endtime")!=null) {
//                            System.out.println("end " + solution.get("endtime"));
                            String uri = ((Resource) obj).getURI();
                            Statement s2 = createStatement((Resource) solution.get("object"), endTimeProperty, solution.get("endtime"));
                            if (trigTripleData.tripleMapInstances.containsKey(uri)) {
                                ArrayList<Statement> triples = trigTripleData.tripleMapInstances.get(uri);
                                triples.add(s2);
                                trigTripleData.tripleMapInstances.put(uri, triples);
                            } else {
                                ArrayList<Statement> triples = new ArrayList<Statement>();
                                triples.add(s2);
                                trigTripleData.tripleMapInstances.put(uri, triples);
                            }
                        }
                    }
                }
            }
            else // Instances
            {
                instanceRelations.add(s);
/*
                if (isEventUri(currentEvent)) {
                    if (isDenotedByRelation(relString)) {
                        getTenSubjectProperties(objUri, trigTripleData);
                    }
                }
                else {
                   // System.out.println("currentEvent = " + currentEvent);
                }
*/

            }

            if (!oldEvent.equals("")) {
                if (!currentEvent.equals(oldEvent)){
                    if (instanceRelations.size()>0){
                        trigTripleData.tripleMapInstances.put(oldEvent, instanceRelations);
                    }
                    if (otherRelations.size()>0){
                        trigTripleData.tripleMapOthers.put(oldEvent, otherRelations);
                    }
                    instanceRelations = new ArrayList<Statement>();
                    otherRelations = new ArrayList<Statement>();
                }
            }
            oldEvent=currentEvent;
        }
        System.out.println("instance statements = "+trigTripleData.tripleMapInstances.size());
        System.out.println("sem statements = " + trigTripleData.tripleMapOthers.size());

        return trigTripleData;

    }

    private static boolean isEventUri (String subject) {
        String name = subject;
        int idx = subject.lastIndexOf("#");
        if (idx>-1) name = name.substring(idx);
        return name.toLowerCase().startsWith("#ev");
    }

    private static boolean isSemRelation(String relation) {
        return relation.startsWith("http://semanticweb.cs.vu.nl/2009/11/sem/");
    }

    private static boolean isSemTimeRelation(String relation) {
        return relation.startsWith("http://semanticweb.cs.vu.nl/2009/11/sem/hasTime");
    }

    private static boolean isDenotedByRelation(String relation) {
        //http://groundedannotationframework.org/gaf#denotedBy
        return relation.endsWith("denotedBy");
    }

    public static boolean isAttributionRelation(String relation) {
        return relation.endsWith("hasAttribution");
    }

    public static boolean isProvRelation(String relation) {
        return relation.equals("http://www.w3.org/ns/prov#wasAttributedTo");
    }

    private static boolean isFNRelation(String relation) {
        return relation.startsWith("http://www.newsreader-project.eu/ontologies/framenet/");
    }
    private static boolean isPBRelation(String relation) {
        return relation.startsWith("http://www.newsreader-project.eu/ontologies/propbank/");
    }
    private static boolean isESORelation(String relation) {
        if (relation.indexOf("hasPreSituation")>-1  ||
            relation.indexOf("quantity-attribute")>-1 ||
            relation.indexOf("hasPostSituation")>-1 ||
            relation.indexOf("hasDuring")>-1) {
            return false;
        }
        return relation.startsWith("http://www.newsreader-project.eu/domain-ontology");
    }

    static public void main(String[] args) {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("--u") && args.length>(i+1)) {
                user = args[i+1];
            }
            else if (arg.equalsIgnoreCase("--p") && args.length>(i+1)) {
                pass = args[i+1];
            }
        }

        readTriplesFromKSforEventEntityType("fn:Arriving", "dbp:Company");
       // readTriplesFromKSforEventType("fn:Arriving");

        //readTriplesFromKSforEntity("Airbus", "");
        long estimatedTime = System.currentTimeMillis() - startTime;

        System.out.println("Time elapsed:");
        System.out.println(estimatedTime/1000.0);
    }

}
