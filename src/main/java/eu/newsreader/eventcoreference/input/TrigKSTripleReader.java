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
    public static String limit = "200";
    //public static String authStr = user + ":" + pass;

    HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());

    static public void setServicePoint (String ks) {
        serviceEndpoint = "https://knowledgestore2.fbk.eu/"+ks+"/sparql";
    }
    static public TrigTripleData readTriplesFromKS(String entityLabel){
        return readTriplesFromKS(entityLabel, "");
    }


    static String makeEntityQuery (String entityLabel, String eventFilter) {
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
        return sparqlQuery;
    }

    static String makeEventQuery (String entityLabel, String eventFilter) {
        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT ?event ?relation ?object ?indatetime ?begintime ?endtime \n" +
                "WHERE {\n" +
                "{SELECT distinct ?event WHERE { \n" +
                "FILTER regex(str(?eventlabel), \"^" + entityLabel + "$\") .\n" +
                "?event rdfs:label ?eventlabel .\n" +
                "?event sem:hasActor ?ent .\n" +
                eventFilter +
                "} LIMIT "+limit+" }\n" +
                "?event ?relation ?object .\n" +
                "OPTIONAL { ?object rdf:type owltime:Instant ; owltime:inDateTime ?indatetime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasBeginning ?begintime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasEnd ?endtime }" +
                "} ORDER BY ?event";
        return sparqlQuery;
    }

    static public TrigTripleData readTriplesFromKS(String entityLabel, String filter){
        TrigTripleData trigTripleData = new TrigTripleData();

        HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());

        Property inDateTimeProperty = ResourceFactory.createProperty("http://www.w3.org/TR/owl-time#inDateTime");
        Property beginTimeProperty = ResourceFactory.createProperty("http://www.w3.org/TR/owl-time#hasBeginning");
        Property endTimeProperty = ResourceFactory.createProperty("http://www.w3.org/TR/owl-time#hasEnd");


        String eventFilter = "";
        if (filter.equals("eso")){
            eventFilter = "FILTER EXISTS { ?event rdf:type ?type .\n" +
                    "?type <http://www.w3.org/2000/01/rdf-schema#isDefinedBy> <http://www.newsreader-project.eu/domain-ontology#> . }\n";
        } else if (filter.equals("fn")){
            eventFilter = "FILTER EXISTS { ?event rdf:type ?type .\n" +
                    "?type <http://www.w3.org/2000/01/rdf-schema#isDefinedBy> <http://www.newsreader-project.eu/ontologies/framenet/> . }\n";
        }

        String sparqlQuery = makeEntityQuery(entityLabel, eventFilter);

        System.out.println("sparqlQuery = " + sparqlQuery);
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

        //System.out.println("Level 1 done");
        // Now we go to the second level!!! //

        /*
        //// get owltime:inDateTime
        for(String rsrc : objectSet) {
            instanceRelations = new ArrayList<Statement>();
            System.out.println("rsrc = " + rsrc);
            String sparqlQuery2 =  "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                    "SELECT ?object2 \n" +
                    "WHERE {\n" +
                    "<" + rsrc + "> owltime:inDateTime ?object2 .\n" +
                    "}";
            QueryExecution x2 = QueryExecutionFactory.sparqlService(serviceEndpoint, sparqlQuery2, authenticator);
            ResultSet resultset2 = x2.execSelect();
            while (resultset2.hasNext()) {
                QuerySolution solution2 = resultset2.nextSolution();
                String relString2 = "http://www.w3.org/TR/owl-time#inDateTime";
                Statement s2 = createStatement(ResourceFactory.createResource(rsrc), ResourceFactory.createProperty(relString2), solution2.get("object2"));
                instanceRelations.add(s2);
            }
            if (instanceRelations.size()>0){
                trigTripleData.tripleMapInstances.put(rsrc, instanceRelations);
            }
        }
*/

   /*     for(String rsrc : objectSet) {
            instanceRelations = new ArrayList<Statement>();
            otherRelations = new ArrayList<Statement>();
            String sparqlQuery2 = "SELECT * \n" +
                    "WHERE {\n" +
                    "<" + rsrc + "> ?relation2 ?object2 .\n" +
                    "}";
            QueryExecution x2 = QueryExecutionFactory.sparqlService(serviceEndpoint, sparqlQuery2, authenticator);
            ResultSet resultset2 = x2.execSelect();
            while (resultset2.hasNext()) {
                QuerySolution solution2 = resultset2.nextSolution();
                String relString2 = solution2.get("relation2").toString();
                Statement s2 = createStatement(ResourceFactory.createResource(rsrc), ResourceFactory.createProperty(solution2.get("relation2").toString()), solution2.get("object2"));
            //    if (isSemRelation(relString2) || isESORelation(relString2) || isFNRelation(relString2) || isPBRelation(relString2))
                if (isSemRelation(relString2))
                {
                    otherRelations.add(s2);
                }
                else // Instances
                {
                    instanceRelations.add(s2);
                }

            }
            if (instanceRelations.size()>0){
                trigTripleData.tripleMapInstances.put(rsrc, instanceRelations);
            }
            if (otherRelations.size()>0){
                trigTripleData.tripleMapOthers.put(rsrc, otherRelations);
            }
        }*/
        System.out.println("instance statements = "+trigTripleData.tripleMapInstances.size());
        System.out.println("sem statements = " + trigTripleData.tripleMapOthers.size());
        return trigTripleData;
    }


    private static boolean isSemRelation(String relation) {
        return relation.startsWith("http://semanticweb.cs.vu.nl/2009/11/sem/");
    }

    private static boolean isSemTimeRelation(String relation) {
        return relation.startsWith("http://semanticweb.cs.vu.nl/2009/11/sem/hasTime");
    }

    private static boolean isFNRelation(String relation) {
        return relation.startsWith("http://www.newsreader-project.eu/ontologies/framenet/");
    }
    private static boolean isPBRelation(String relation) {
        return relation.startsWith("http://www.newsreader-project.eu/ontologies/propbank/");
    }
    private static boolean isESORelation(String relation) {
        return relation.startsWith("http://www.newsreader-project.eu/ontologies/domain-ontology#");
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

        readTriplesFromKS("Airbus", "");
        long estimatedTime = System.currentTimeMillis() - startTime;

        System.out.println("Time elapsed:");
        System.out.println(estimatedTime/1000.0);
    }

}
