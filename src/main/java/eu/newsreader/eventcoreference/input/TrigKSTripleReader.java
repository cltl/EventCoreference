package eu.newsreader.eventcoreference.input;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import java.util.HashSet;
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
    public static String limit = "10";
    //public static String authStr = user + ":" + pass;

    HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());


    static TrigTripleData readTriplesFromKS(String entityLabel){
        return readTriplesFromKS(entityLabel, "");
    }

    static TrigTripleData readTriplesFromKS(String entityLabel, String filter){
        TrigTripleData trigTripleData = new TrigTripleData();

        HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());

        System.out.println("Filter  " +  filter);
        String eventFilter = "";
        if (filter.equals("eso")){
            eventFilter = "FILTER EXISTS { ?event rdf:type ?type .\n" +
                    "?type <http://www.w3.org/2000/01/rdf-schema#isDefinedBy> <http://www.newsreader-project.eu/domain-ontology#> . }\n";
        } else if (filter.equals("fn")){
            eventFilter = "FILTER EXISTS { ?event rdf:type ?type .\n" +
                    "?type <http://www.w3.org/2000/01/rdf-schema#isDefinedBy> <http://www.newsreader-project.eu/ontologies/framenet/> . }\n";
        }

        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT ?event ?relation ?object \n" +
                "WHERE {\n" +
                "{SELECT distinct ?event WHERE { \n" +
                "FILTER regex(str(?entlabel), \"^" + entityLabel + "$\") .\n" +
                "?ent rdfs:label ?entlabel .\n" +
                "?event sem:hasActor ?ent .\n" +
                eventFilter +
                "} LIMIT "+limit+" }\n" +
                "?event ?relation ?object \n" +
                "} ORDER BY ?event";
        System.out.println(sparqlQuery);

        QueryExecution x = QueryExecutionFactory.sparqlService(serviceEndpoint, sparqlQuery, authenticator);
        ResultSet resultset = x.execSelect();
        String oldEvent="";
        ArrayList<Statement> instanceRelations = new ArrayList<Statement>();
        ArrayList<Statement> otherRelations = new ArrayList<Statement>();
        HashSet<String> objectSet = new HashSet<String>();
        while (resultset.hasNext()) {
            QuerySolution solution = resultset.nextSolution();
            String relString = solution.get("relation").toString();
            String currentEvent = solution.get("event").toString();
            Statement s = createStatement((Resource) solution.get("event"), ResourceFactory.createProperty(relString), solution.get("object"));
          //  System.out.println("s.toString() = " + s.toString());
            if (isSemRelation(relString) || isESORelation(relString) || isFNRelation(relString) || isPBRelation(relString))
            {
                otherRelations.add(s);
                if (isSemTimeRelation(relString)) {
                    objectSet.add(solution.get("object").toString());
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

        //System.out.println("Level 1 done");
        // Now we go to the second level!!! //

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