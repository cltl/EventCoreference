

/**
 * Created by filipilievski on 9/12/15.
 */

package eu.newsreader.eventcoreference.naf;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import org.apache.commons.codec.binary.Base64;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/**
 * Created with IntelliJ IDEA.
 * User: Filip
 * Date: 09/12/15
 * Time: 3:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessEventObjectsStream {



    /*
        @TODO
        1. proper reference to the ontologies (even if not there yet)
        7. parametrize the module to get high-precision or high-recall TriG
        8. entities that are not part of events are not in the output
     */


    static final String USAGE = "This program processes NAF files and stores binary objects for events with all related data in different object files based on the event type and the date\n" +
            "The program has the following arguments:\n" +
            "--concept-match <int>      <threshold for conceptual matches of events, default is 50>\n" +
            "--phrase-match  <int>      <threshold for phrase matches of events, default is 50>\n" +
            "--contextual-match-type    <path>   <Indicates what is used to match events across resources. Default value is \"ILILEMMA\". Values:\"LEMMA\", \"ILI\", \"ILILEMMA\">\n" +
            "--contextual-lcs                    <Use lowest-common-subsumers. Default value is ON.>\n" +
            "--contextual-roles  <path>   <String with roles for which there must be a match, e.g. \"pb:A1, sem:hasActor\">\n" +
            "--source-match-type        <path>   <Indicates what is used to match events across resources. Default value is \"ILILEMMA\". Values:\"LEMMA\", \"ILI\", \"ILILEMMA\">\n" +
            "--source-lcs                        <Use lowest-common-subsumers. Default value is OFF.>\n" +
            "--source-roles      <path>   <String with roles for which there must be a match, e.g. \"pb:A1, sem:hasActor\">\n" +
            "--grammatical-match-type   <path>   <Indicates what is used to match events across resources. Default value is \"LEMMA\". Values:\"LEMMA\", \"ILI\", \"ILILEMMA\">\n" +
            "--grammatical-lcs                    <Use lowest-common-subsumers. Default value is OFF.>\n" +
            "--grammatical-roles <path>   <String with roles for which there must be a match, e.g. \"pb:A1, sem:hasActor\">\n" +
            "--future-match-type        <path>   <Indicates what is used to match events across resources. Default value is \"LEMMA\". Values:\"LEMMA\", \"ILI\", \"ILILEMMA\">\n" +
            "--future-lcs                        <Use lowest-common-subsumers. Default value is OFF.>\n" +
            "--recent-span              <int>    <Amount of past days which are still considered recent and are treated differently>\n";

    static public String filename = "";
    static public String projectName = "cars";

    static public String CONTEXTUALMATCHTYPE = "ILILEMMA";
    static public boolean CONTEXTUALLCS = true;

    static public String SOURCEMATCHTYPE = "ILILEMMA";
    static public boolean SOURCELCS = false;

    static public String GRAMMATICALMATCHTYPE = "LEMMA";
    static public boolean GRAMMATICALLCS = false;

    static public String FUTUREMATCHTYPE = "LEMMA";
    static public boolean FUTURELCS = false;

    static public ArrayList<String> contextualNeededRoles = new ArrayList<String>();
    static public ArrayList<String> sourceNeededRoles = new ArrayList<String>();
    static public ArrayList<String> grammaticalNeededRoles = new ArrayList<String>();

    static boolean DEBUG = false;

    static public int recentDays = 0;

    static public String done = "";

//    final static String serviceEndpoint = "https://knowledgestore2.fbk.eu/nwr/cars2/sparql";
    final static String serviceEndpoint = "https://knowledgestore2.fbk.eu/nwr/aitor/sparql";
    public static String user = "nwr_partner";
    public static String pass = "ks=2014!";
    public static String authStr = user + ":" + pass;
    public static byte[] authEncoded = Base64.encodeBase64(authStr.getBytes());

    public static final String NL = System.getProperty("line.separator");

    public static final Node identityNode = NodeFactory.createURI("http://www.w3.org/2002/07/owl#sameAs");
    public static final Node identityGraphNode = NodeFactory.createURI("http://www.newsreader-project.eu/identity");
    public static final Node lemmaNode = NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label");
    public static final Node timeNode = NodeFactory.createURI("http://semanticweb.cs.vu.nl/2009/11/sem/hasTime");
    public static final Node typeNode = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    public static final Node instantNode = NodeFactory.createURI("http://www.w3.org/TR/owl-time#Instant");
    public static final Node intervalNode = NodeFactory.createURI("http://www.w3.org/TR/owl-time#Interval");
    public static final Node specificTimeNode = NodeFactory.createURI("http://www.w3.org/TR/owl-time#inDateTime");
    public static final Node begintimeNode = NodeFactory.createURI("http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestBeginTimeStamp");
    public static final Node endtimeNode = NodeFactory.createURI("http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestEndTimeStamp");

    public static int conceptMatchThreshold = 10;
    public static int phraseMatchThreshold = 10;

    static public String matchSingleTmx(Node tmx, DatasetGraph g, Model m){
        String sq="";
        if (g.contains(null, tmx, typeNode, instantNode)) { // One Instant
            sq += "?ev <http://semanticweb.cs.vu.nl/2009/11/sem/hasTime> ?t . ?t a <http://www.w3.org/TR/owl-time#Instant> . ";
            for (Iterator<Quad> iter = g.find(null, tmx, specificTimeNode, null); iter.hasNext(); ) {
                Quad q = iter.next();
                sq += "?t <http://www.w3.org/TR/owl-time#inDateTime> <" + q.asTriple().getObject() + "> . ";
            }
        } else { // One Interval

            String intervalQuery = "SELECT ?begin ?end WHERE { <" + tmx + ">  <http://www.w3.org/TR/owl-time#hasBeginning> ?begin ; <http://www.w3.org/TR/owl-time#hasEnd> ?end . }";

            Query inQuery = QueryFactory.create(intervalQuery);

            // Create a single execution of this query, apply to a model
            // which is wrapped up as a Dataset
            QueryExecution inQexec = QueryExecutionFactory.create(inQuery, m);

            try {
                // Assumption: it’s a SELECT query.
                ResultSet inrs = inQexec.execSelect();
                // The order of results is undefined.
                for (; inrs.hasNext(); ) {
                    QuerySolution evrb = inrs.nextSolution();
                    // Get title - variable names do not include the ’?’
                    String begin = evrb.get("begin").toString();
                    String end = evrb.get("end").toString();

                    String unionQuery = "{ ?ev <http://semanticweb.cs.vu.nl/2009/11/sem/hasTime> ?t . ?t a <http://www.w3.org/TR/owl-time#Interval> . ?t <http://www.w3.org/TR/owl-time#hasBeginning> <" + begin + "> ; <http://www.w3.org/TR/owl-time#hasEnd> <" + end + "> . } ";
                    unionQuery += "UNION ";
                    unionQuery += "{ ?ev <http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestBeginTimeStamp> ?t1 . ?t1 a <http://www.w3.org/TR/owl-time#Instant> . ?t1 <http://www.w3.org/TR/owl-time#inDateTime> <" + begin + "> . ?ev <http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestEndTimeStamp> ?t2 . ?t2 a <http://www.w3.org/TR/owl-time#Instant> . ?t2 <http://www.w3.org/TR/owl-time#inDateTime> <" + end + "> . } ";
                    sq += unionQuery;
                }
            } finally {
                inQexec.close();
            }
        }
        return sq;
    }

    static public void main(String[] args) {

        // 1. CLUSTER

        if (args.length == 0) {
            System.out.println(USAGE);
            System.out.println("NOW RUNNING WITH DEFAULT SETTINGS");
            //  return;
        }

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--filename") && args.length > (i + 1)) {
                filename = args[i + 1];
            } else if (arg.equals("--project") && args.length > (i + 1)) {
                projectName = args[i + 1];
            }
            else if (arg.equals("--concept-match") && args.length>(i+1)) {
                try {
                    conceptMatchThreshold = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else if (arg.equals("--phrase-match") && args.length>(i+1)) {
                try {
                    phraseMatchThreshold = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }else if (arg.equals("--contextual-match-type") && args.length > (i + 1)) {
                CONTEXTUALMATCHTYPE = args[i + 1];
            } else if (arg.equals("--contextual-lcs")) {
                CONTEXTUALLCS = true;
            } else if (arg.equals("--contextual-roles") && args.length > (i + 1)) {
                String[] fields = args[i + 1].split(",");
                for (int j = 0; j < fields.length; j++) {
                    String field = fields[j].trim();
                    contextualNeededRoles.add(field);
                }
            } else if (arg.equals("--source-match-type") && args.length > (i + 1)) {
                SOURCEMATCHTYPE = args[i + 1];
            } else if (arg.equals("--source-lcs")) {
                SOURCELCS = true;
            } else if (arg.equals("--source-roles") && args.length > (i + 1)) {
                String[] fields = args[i + 1].split(",");
                for (int j = 0; j < fields.length; j++) {
                    String field = fields[j].trim();
                    sourceNeededRoles.add(field);
                }
            } else if (arg.equals("--grammatical-match-type") && args.length > (i + 1)) {
                GRAMMATICALMATCHTYPE = args[i + 1];
            } else if (arg.equals("--grammatical-lcs")) {
                GRAMMATICALLCS = true;
            } else if (arg.equals("--grammatical-roles") && args.length > (i + 1)) {
                String[] fields = args[i + 1].split(",");
                for (int j = 0; j < fields.length; j++) {
                    String field = fields[j].trim();
                    grammaticalNeededRoles.add(field);
                }
            } else if (arg.equals("--future-match-type") && args.length > (i + 1)) {
                FUTUREMATCHTYPE = args[i + 1];
            } else if (arg.equals("--future-lcs")) {
                FUTURELCS = true;
            } else if (arg.equals("--recent-span")) {
                recentDays = Integer.parseInt(args[i + 1]);
            }
        }



        Dataset ds = TDBFactory.createDataset();
        Dataset dsnew = TDBFactory.createDataset();

        if (filename.isEmpty()){ // If empty filename, read from stream!
            readTrigFromStream(ds, dsnew);
        } else {
            RDFDataMgr.read(ds, filename);
            RDFDataMgr.read(dsnew, filename);
        }
        Model m = ds.getNamedModel("http://www.newsreader-project.eu/instances");

        Model m2 = ds.getDefaultModel();
        m2.setNsPrefix("sem", "http://semanticweb.cs.vu.nl/2009/11/sem/");
        m2.setNsPrefix("pb", "http://www.newsreader-project.eu/ontologies/propbank/");
        m2.setNsPrefix("eso", "http://www.newsreader-project.eu/ontologies/domain-ontology#");
        m2.setNsPrefix("fn", "http://www.newsreader-project.eu/ontologies/framenet/");

        DatasetGraph g = ds.asDatasetGraph();

        DatasetGraph gnew = dsnew.asDatasetGraph();
        //System.out.println(m.size());


        final String prolog1 = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/>";
        final String prolog2 = "PREFIX rdf: <" + RDF.getURI() + ">";
        final String prolog3 = "PREFIX nwr: <http://www.newsreader-project.eu/> ";
        final String rolePrefixes = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/>" + NL + "PREFIX eso: <http://www.newsreader-project.eu/ontologies/domain-ontology#>" + NL + "PREFIX pb: <http://www.newsreader-project.eu/ontologies/propbank/>" + NL + "PREFIX fn: <http://www.newsreader-project.eu/ontologies/framenet/>" + NL;

        final String allEventsQuery = "SELECT distinct ?ev WHERE { ?ev a <http://semanticweb.cs.vu.nl/2009/11/sem/Event> }";

        final Query query = QueryFactory.create(allEventsQuery);

        // Create a single execution of this query, apply to a model
        // which is wrapped up as a Dataset
        final QueryExecution qexec = QueryExecutionFactory.create(query, m);
        // Or QueryExecutionFactory.create(queryString, model) ;


        try {

            // Assumption: it’s a SELECT query.
            final ResultSet rs = qexec.execSelect();
            // The order of results is undefined.
            for (; rs.hasNext(); ) {
                final QuerySolution rb = rs.nextSolution();
                // Get title - variable names do not include the ’?’
                final RDFNode eventId = rb.get("ev");
                final Resource z = rb.getResource("ev");

                // Now get the details for each event

//                ?ev rdf:type ?type . FILTER (regex(STR(?type), "^http://www.newsreader-project.eu/ontologies/.*Event"))

                String eventDetailsQuery = prolog1 + NL + prolog2 + NL + prolog3 + NL + "SELECT ?type WHERE { <" + eventId + "> rdf:type ?type }";

                Query evQuery = QueryFactory.create(eventDetailsQuery);

                String MATCHTYPE = "";
                // Process the event now ;)
                String sparqlSelectQuery = rolePrefixes + "SELECT distinct ?ev ";
                String sparqlQuery = "WHERE { ?ev a sem:Event , ";
                ArrayList<String> neededRoles = new ArrayList<String>();
                String nwrtype = "";
                ArrayList<String> myILIs = new ArrayList<String>();
                ArrayList<String> myFrames = new ArrayList<String>();

                // Create a single execution of this query, apply to a model
                // which is wrapped up as a Dataset
                QueryExecution evQexec = QueryExecutionFactory.create(evQuery, m);

                try {
                    // Assumption: it’s a SELECT query.
                    ResultSet evrs = evQexec.execSelect();
                    // The order of results is undefined.
                    for (; evrs.hasNext(); ) {
                        QuerySolution evrb = evrs.nextSolution();
                        // Get title - variable names do not include the ’?’
                        String rdfType = evrb.get("type").toString();

                        if (rdfType.equals("http://www.newsreader-project.eu/ontologies/grammaticalEvent")) {
                            nwrtype = rdfType;
                            MATCHTYPE = GRAMMATICALMATCHTYPE;
                            neededRoles = grammaticalNeededRoles;
                        } else if (rdfType.equals("http://www.newsreader-project.eu/ontologies/sourceEvent")) {
                            nwrtype = rdfType;
                            MATCHTYPE = SOURCEMATCHTYPE;
                            neededRoles = sourceNeededRoles;
                        } else if (rdfType.equals("http://www.newsreader-project.eu/ontologies/contextualEvent")) {
                            nwrtype = rdfType;
                            MATCHTYPE = CONTEXTUALMATCHTYPE;
                            neededRoles = contextualNeededRoles;
                        } else if (rdfType.contains("http://www.newsreader-project.eu/ontologies/framenet/")) {
                            myFrames.add(rdfType);
                        } else if (rdfType.contains("http://globalwordnet.org/ili/")) { // wordnet ILIs
                            myILIs.add(rdfType);
                        }

                    }
                } finally {
                    evQexec.close();
                }

                if (eventId.toString().contains("http://www.newsreader-project.eu/data/cars/2004/10/08/4DH5-8HM0-00BT-N3MS.xml#ev192"))
                    System.out.println("Our event: " + myILIs);
                if (!myFrames.isEmpty()) {
                    for (int i = 0; i < myFrames.size(); i++) {
                        String et = myFrames.get(i);
                        sparqlQuery += "<" + et + "> ";
                        if (i < myFrames.size() - 1)
                            sparqlQuery += ", ";
                        else
                            sparqlQuery += ". ";
                    }
                } else {
                    sparqlQuery += "<" + nwrtype + "> . ";
                }

                Node eventNode = NodeFactory.createURI(eventId.toString());
                System.out.println(eventId);
                System.out.println(neededRoles);
                if (!neededRoles.isEmpty()) {
                    boolean skip = false;

                    if (neededRoles.size()==1 && neededRoles.get(0).equals("all")){//match all roles there are for this event
                        Map<String, String> rolesToActors = new HashMap<String, String>();
                        for (Iterator<Quad> iter = g.find(null, eventNode, null, null); iter.hasNext(); ) {
                            Quad q = iter.next();
                            String predicate = q.asTriple().getPredicate().toString();
                            if (predicate.startsWith(m2.getNsPrefixURI("sem") + "hasActor") || predicate.startsWith(m2.getNsPrefixURI("eso")) || predicate.startsWith(m2.getNsPrefixURI("fn")) || predicate.startsWith(m2.getNsPrefixURI("pb")))
                                rolesToActors.put(q.asTriple().getPredicate().toString(), q.asTriple().getObject().toString());
                        }
                        for (Map.Entry<String, String> entry : rolesToActors.entrySet())
                        {
                            sparqlQuery += "?ev <" + entry.getKey() + "> <" + entry.getValue() + "> . ";
                        }

                    } else {

                        for (int i = 0; i < neededRoles.size(); i++) {
                            String role = neededRoles.get(i);
                            Node roleNode = NodeFactory.createURI(role);
                            ArrayList<Node> allActors = new ArrayList<Node>();
                            for (Iterator<Quad> iter = g.find(null, eventNode, NodeFactory.createURI(m2.expandPrefix(roleNode.getURI())), null); iter.hasNext(); ) {
                                Quad q = iter.next();
                                allActors.add(q.asTriple().getObject());
                            }
                            if (allActors.isEmpty()) {
                                break;
                            } else if (allActors.size() == 1) {
                                sparqlQuery += "?ev " + role + " <" + allActors.get(0) + "> . ";
                            } else {
                                String rolevar = "?role" + i;

                                String filter = " ?ev " + role + " " + rolevar + " . FILTER ( " + rolevar + " IN (";
                                for (int j = 0; j < allActors.size(); j++) {
                                    filter += "<" + allActors.get(j) + ">, ";
                                }
                                sparqlQuery += filter.substring(0, filter.length() - 2) + ") ) . ";
                            }
                        }
                        if (skip)
                            continue;
                    }
                }
                // Roles done!

                // Match Type now:
                boolean matchMultiple=false;
                boolean matchLemma=false;
                boolean matchILI=false;
                ArrayList<Node> allLemmas = new ArrayList<Node>();
                if (MATCHTYPE.equals("ILILEMMA") && myILIs.size() > 0) {
                    matchILI=true;
                    if (myILIs.size() == 1) {
                        sparqlSelectQuery+="(COUNT(distinct ?allilis) as ?conceptcount) ";
                        sparqlQuery += "?ev a <" + myILIs.get(0) + "> . ?ev a ?allilis . FILTER strstarts(str(?allilis), \"http://www.newsreader-project.eu/ontologies/ili-30-\") . ";
                    } else {
                        matchMultiple=true;
                        sparqlSelectQuery+="(COUNT(distinct ?allilis) as ?conceptcount) (COUNT(distinct ?myilis) as ?myconceptcount) ";
                        sparqlQuery += "?ev a ?allilis . FILTER strstarts(str(?allilis), \"http://www.newsreader-project.eu/ontologies/ili-30-\") . ";

                        String iliFilter = "?ev a ?myilis . FILTER ( ?myilis IN (";
                        for (int i = 0; i < myILIs.size(); i++) {
                            iliFilter += "<" + myILIs.get(i) + "> , ";
                        }
                        sparqlQuery += iliFilter.substring(0, iliFilter.length() - 2) + ") ) . ";

                    }
                } else {

                    for (Iterator<Quad> iter = g.find(null, eventNode, lemmaNode, null); iter.hasNext(); ) {
                        Quad q = iter.next();
                        allLemmas.add(q.asTriple().getObject());
                    }

                    if (!allLemmas.isEmpty()) {
                        matchLemma=true;
                        if (allLemmas.size() == 1) {
                            sparqlSelectQuery+="(COUNT(distinct ?lbl) as ?conceptcount) ";
                            sparqlQuery += "?ev <http://www.w3.org/2000/01/rdf-schema#label> ?l. FILTER (STR(?l) = STR(" + allLemmas.get(0) + ")) . ?ev <http://www.w3.org/2000/01/rdf-schema#label> ?lbl . ";
                        } else {
                            matchMultiple=true;
                            sparqlSelectQuery+="(COUNT(distinct ?lbl) as ?conceptcount) (COUNT(distinct ?mylbls) as ?myconceptcount) ";
                            sparqlQuery += " ?ev <http://www.w3.org/2000/01/rdf-schema#label> ?lbl . ";
                            sparqlQuery += " ?ev <http://www.w3.org/2000/01/rdf-schema#label> ?mylbls . ";

                            String lemmaFilter = " ?ev <http://www.w3.org/2000/01/rdf-schema#label> ?mylbls . FILTER ( ?mylbls IN (";
                            for (int i = 0; i < allLemmas.size(); i++) {
                                lemmaFilter += allLemmas.get(i) + ", ";
                            }
                            sparqlQuery += lemmaFilter.substring(0, lemmaFilter.length() - 2) + ") ) . ";

                        }
                    }
                }

                // Times :

                ArrayList<Node> allTmx = new ArrayList<Node>();
                ArrayList<String> allAssociatedBeginTimes = new ArrayList<String>();
                ArrayList<String> allAssociatedEndTimes = new ArrayList<String>();
                for (Iterator<Quad> iter = g.find(null, eventNode, timeNode, null); iter.hasNext(); ) {
                    Quad q = iter.next();
                    allTmx.add(q.asTriple().getObject());
                }

                ResultSet solutions=getAssociatedTimes(eventId, m);
                for (; solutions.hasNext(); ) {
                    QuerySolution mprb = solutions.nextSolution();
                    // Get title - variable names do not include the ’?’
                    allAssociatedBeginTimes.add(mprb.get("begin").toString());
                    allAssociatedEndTimes.add(mprb.get("end").toString());
                }

                if (allTmx.size() == 0 && allAssociatedBeginTimes.size()==0) {
                    sparqlQuery += "} ";
                    sparqlQuery = sparqlSelectQuery + sparqlQuery;
                    inferIdentityRelations(sparqlQuery, matchILI, matchLemma, matchMultiple, myILIs.size(), eventNode, gnew);

                    // System.out.println(sparqlQuery);

                }
                else if (allTmx.size() == 1 && allAssociatedBeginTimes.size()==0) {
                    Node tmx = allTmx.get(0);

                    sparqlQuery += matchSingleTmx(tmx, g, m);
                    sparqlQuery += "} ";
                    sparqlQuery = sparqlSelectQuery + sparqlQuery;
                    inferIdentityRelations(sparqlQuery, matchILI, matchLemma, matchMultiple, myILIs.size(), eventNode, gnew);

                    //System.out.println(sparqlQuery);

                } else if (allTmx.size() == 0 && allAssociatedBeginTimes.size()==1) {
                    String begin = allAssociatedBeginTimes.get(0);
                    String end = allAssociatedEndTimes.get(0);

                    sparqlQuery += matchAssociatedPair(begin, end);
                    sparqlQuery += "}";
                    sparqlQuery = sparqlSelectQuery + sparqlQuery;
                    inferIdentityRelations(sparqlQuery, matchILI, matchLemma, matchMultiple, myILIs.size(), eventNode, gnew);

                } else { //2+ times associated
                    sparqlQuery = sparqlSelectQuery + sparqlQuery;
                    boolean baseEventEmpty=true;

                    if (allAssociatedBeginTimes.size()==1){
                        String begin = allAssociatedBeginTimes.get(0);
                        String end = allAssociatedEndTimes.get(0);

                        String assocSparqlQuery = sparqlQuery + matchAssociatedPair(begin, end);
                        assocSparqlQuery += "}";

                        ArrayList<Node> ksevents = inferMultitimesIdentityRelations(assocSparqlQuery, matchILI, matchLemma, matchMultiple, myILIs.size(), eventId.toString());

                        if(ksevents.size()>0){ // If there are events for this tmx, make identity statements

                            // give it a new id and put mappings
                            Node assocEventId = NodeFactory.createURI(eventId + "_ass1");
                            moveAssocRelations(gnew, eventNode, assocEventId, NodeFactory.createURI(begin), NodeFactory.createURI(end));
                            for (int l = 0; l < ksevents.size(); l++) {
                                insertIdentity(gnew, assocEventId, ksevents.get(l));
                            }
                        } else{
                            baseEventEmpty=false;
                        }

                        //System.out.println(sparqlQuery);
                    }

                    for (int j=0; j<allTmx.size(); j++) {
                        String tmxQuery = sparqlQuery;

                        Node tmx = allTmx.get(j);
                        tmxQuery += matchSingleTmx(tmx, g, m);
                        tmxQuery +="}";

                        ArrayList<Node> ksevents = inferMultitimesIdentityRelations(tmxQuery, matchILI, matchLemma, matchMultiple, myILIs.size(), eventId.toString());

                        if(ksevents.size()>0){ // If there are events for this tmx, make identity statements
                            if (baseEventEmpty && j==allTmx.size()-1){
                                for (int l = 0; l < ksevents.size(); l++) {
                                    insertIdentity(gnew, eventNode, ksevents.get(l));
                                }
                            }
                            else{// give it a new id and put mappings
                                Node tmxEventId = NodeFactory.createURI(eventId + "_" + tmx.getLocalName());
                                moveRelations(gnew, eventNode, tmxEventId, tmx);
                                for (int l = 0; l < ksevents.size(); l++) {
                                    insertIdentity(gnew, tmxEventId, ksevents.get(l));
                                }
                            }
                        } else{
                            baseEventEmpty=false;
                        }
                    }
                }



                /////////////////////////////////////////////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////////////
                /////////////////////////////////// MATCHING SPARQL /////////////////////////////////////////////////
                /////////////////////////////////// MATCHING SPARQL /////////////////////////////////////////////////
                /////////////////////////////////// MATCHING SPARQL /////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////////////




            }

        }



        finally {
            // QueryExecution objects should be closed to free any system
            // resources
            qexec.close();
            RDFDataMgr.write(System.out, gnew, RDFLanguages.TRIG); // or NQUADS
        }


    }

    private static ByteArrayOutputStream cloneInputStream(InputStream input){

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Fake code simulating the copy
        // You can generally do better with nio if you need...
        //    And please, unlike me, do something about the Exceptions :D
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = input.read(buffer)) > -1 ) {
                baos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos;
    }

    private static void readTrigFromStream(Dataset ds, Dataset dsnew) {
        InputStream is = null;
        try {
            is = System.in;

            if (is==null){
                throw new IllegalArgumentException(
                        "No stream input!");
            }

            ByteArrayOutputStream b = cloneInputStream(is);
            InputStream is1 = new ByteArrayInputStream(b.toByteArray());
            InputStream is2 = new ByteArrayInputStream(b.toByteArray());

            RDFDataMgr.read(ds, is1, RDFLanguages.TRIG);
            RDFDataMgr.read(dsnew, is2, RDFLanguages.TRIG);

        }
        finally {
            // close the streams using close method

        }

    }

    private static void moveRelations(DatasetGraph g, Node eventId, Node tmxEventId, Node tmx) {

        Node graphURI=null;
        Quad td=null;
        for (Iterator<Quad> iter = g.find(null, eventId, timeNode, tmx); iter.hasNext(); ) {
            Quad q = iter.next();
            graphURI = q.getGraph();
            //System.out.println("ev " + graphURI);
            td=q;
        }
        g.delete(td);
        g.add(new Quad(graphURI, tmxEventId, timeNode, tmx));
        //System.out.println(g.contains(null, eventId, timeNode, tmx));
        //System.out.println(g.contains(null, tmxEventId, timeNode, tmx));

        // The event-to-tmx has been cleaned (the tmx is transfered to the new event).
        //
        // Now we need to copy the non-time-related features to the new event

        ArrayList<Quad> quadsToAdd = new ArrayList<Quad>();
        for (Iterator<Quad> iter = g.find(null, eventId, null, null); iter.hasNext(); ) {
            Quad q = iter.next();
            if (!q.getPredicate().toString().equalsIgnoreCase("http://semanticweb.cs.vu.nl/2009/11/sem/hasTime") && !q.getPredicate().toString().equalsIgnoreCase("http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestBeginTimeStamp") && !q.getPredicate().toString().equalsIgnoreCase("http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestEndTimeStamp")) {
                quadsToAdd.add(new Quad(q.getGraph(), tmxEventId, q.getPredicate(), q.getObject()));
            }
        }
        //System.out.println(quadsToAdd.size());
        for (int qn=0; qn<quadsToAdd.size(); qn++){
            //System.out.println(quadsToAdd.get(qn));
            g.add(quadsToAdd.get(qn));
        }
    }

    private static void moveAssocRelations(DatasetGraph g, Node eventId, Node tmxEventId, Node begin, Node end) {

        Node graphURI=null;
        Quad td=null;

        //begin
        for (Iterator<Quad> iter = g.find(null, eventId, begintimeNode, begin); iter.hasNext(); ) {
            Quad q = iter.next();
            graphURI = q.getGraph();
            //System.out.println("ev " + graphURI);
            td=q;
        }
        g.delete(td);
        g.add(new Quad(graphURI, tmxEventId, begintimeNode, begin));

        // end

        for (Iterator<Quad> iter = g.find(null, eventId, endtimeNode, end); iter.hasNext(); ) {
            Quad q = iter.next();
            graphURI = q.getGraph();
            //System.out.println("ev " + graphURI);
            td=q;
        }
        g.delete(td);
        g.add(new Quad(graphURI, tmxEventId, endtimeNode, end));

        // The event-to-tmx has been cleaned (the tmx is transfered to the new event).
        //
        // Now we need to copy the non-time-related features to the new event

        ArrayList<Quad> quadsToAdd = new ArrayList<Quad>();
        for (Iterator<Quad> iter = g.find(null, eventId, null, null); iter.hasNext(); ) {
            Quad q = iter.next();
            if (!q.getPredicate().toString().equalsIgnoreCase("http://semanticweb.cs.vu.nl/2009/11/sem/hasTime") && !q.getPredicate().toString().equalsIgnoreCase("http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestBeginTimeStamp") && !q.getPredicate().toString().equalsIgnoreCase("http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestEndTimeStamp")) {
                quadsToAdd.add(new Quad(q.getGraph(), tmxEventId, q.getPredicate(), q.getObject()));
            }
        }
        //System.out.println(quadsToAdd.size());
        for (int qn=0; qn<quadsToAdd.size(); qn++){
            //System.out.println(quadsToAdd.get(qn));
            g.add(quadsToAdd.get(qn));
        }
    }

    private static String matchAssociatedPair(String begin, String end) {
        if (!begin.isEmpty() && !end.isEmpty()) {
            String unionQuery = "{ ?ev <http://semanticweb.cs.vu.nl/2009/11/sem/hasTime> ?t . ?t a <http://www.w3.org/TR/owl-time#Interval> . ?t <http://www.w3.org/TR/owl-time#hasBeginning> <" + begin + "> ; <http://www.w3.org/TR/owl-time#hasEnd> <" + end + "> . } ";
            unionQuery += "UNION ";
            unionQuery += "{ ?ev <http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestBeginTimeStamp> ?t1 . ?t1 a <http://www.w3.org/TR/owl-time#Instant> . ?t1 <http://www.w3.org/TR/owl-time#inDateTime> <" + begin + "> . ?ev <http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestEndTimeStamp> ?t2 . ?t2 a <http://www.w3.org/TR/owl-time#Instant> . ?t2 <http://www.w3.org/TR/owl-time#inDateTime> <" + end + "> . } ";
            return unionQuery;
        } else if (!begin.isEmpty()) {
            return "{ ?ev <http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestBeginTimeStamp> ?t1 . ?t1 a <http://www.w3.org/TR/owl-time#Instant> . ?t1 <http://www.w3.org/TR/owl-time#inDateTime> <" + begin + "> . } ";
        } else {
            return "{ ?ev <http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestEndTimeStamp> ?t2 . ?t2 a <http://www.w3.org/TR/owl-time#Instant> . ?t2 <http://www.w3.org/TR/owl-time#inDateTime> <" + end + "> . } ";
        }
    }


    private static ResultSet getAssociatedTimes(RDFNode eventId, Model m) {
        String multiPointQuery = "SELECT ?begin ?end WHERE { { <" + eventId + ">  <http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestBeginTimeStamp> ?t1 . ?t1 <http://www.w3.org/TR/owl-time#inDateTime> ?begin . OPTIONAL { <" + eventId + ">  <http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestEndTimeStamp> ?t2 . ?t2 <http://www.w3.org/TR/owl-time#inDateTime> ?end . } } UNION ";
        multiPointQuery += "{ <" + eventId + ">  <http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestEndTimeStamp> ?t2 . ?t2 <http://www.w3.org/TR/owl-time#inDateTime> ?end . OPTIONAL { <" + eventId + ">  <http://semanticweb.cs.vu.nl/2009/11/sem/hasEarliestBeginTimeStamp> ?t1 . ?t1 <http://www.w3.org/TR/owl-time#inDateTime> ?begin . } } }";

        Query mpQuery = QueryFactory.create(multiPointQuery);

        // Create a single execution of this query, apply to a model
        // which is wrapped up as a Dataset
        QueryExecution mpQexec = QueryExecutionFactory.create(mpQuery, m);

        try {
            // Assumption: it’s a SELECT query.
            ResultSet mprs = mpQexec.execSelect();
            mpQexec.close();
            return mprs;
        } finally {

        }

    }

    private static void inferIdentityRelations(String sparqlQuery, boolean matchILI, boolean matchLemma, boolean matchMultiple, int iliSize, Node eventId, DatasetGraph g) {
        if (matchILI || matchLemma) {
            sparqlQuery += "GROUP BY ?ev";
        }
        HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());
        System.out.println(sparqlQuery);
        QueryExecution x = QueryExecutionFactory.sparqlService(serviceEndpoint, sparqlQuery, authenticator);
        ResultSet resultset = x.execSelect();
        int threshold;
        while (resultset.hasNext()) {
            QuerySolution solution = resultset.nextSolution();
            if (matchILI || matchLemma) {
                if (matchILI)
                    threshold = conceptMatchThreshold;
                else
                    threshold=phraseMatchThreshold;

                if (matchMultiple) {
                    //System.out.println(solution);
                    if (checkIliLemmaThreshold(iliSize, solution.get("conceptcount").asLiteral().getInt(), solution.get("myconceptcount").asLiteral().getInt(), threshold)) {
                        insertIdentity(g, eventId, solution.get("ev").asNode());
                    }
                } else {
                    if (checkIliLemmaThreshold(1, solution.get("conceptcount").asLiteral().getInt(), 1, threshold)) {
                        insertIdentity(g, eventId, solution.get("ev").asNode());
                    }
                }
            } else {
                insertIdentity(g, eventId, solution.get("ev").asNode());
            }
        }
    }

    private static void insertIdentity (DatasetGraph g, Node e1, Node e2){
        g.add(identityGraphNode, e1, identityNode, e2);

    }

    private static ArrayList<Node> inferMultitimesIdentityRelations (String sparqlQuery, boolean matchILI, boolean matchLemma, boolean matchMultiple, int iliSize, String eventId) {
        if (matchILI || matchLemma) {
            sparqlQuery += "GROUP BY ?ev";
        }
        HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());
        QueryExecution x = QueryExecutionFactory.sparqlService(serviceEndpoint, sparqlQuery, authenticator);
        ResultSet resultset = x.execSelect();
        int threshold;
        ArrayList<Node> rslt = new ArrayList<Node>();
        while (resultset.hasNext()) {
            QuerySolution solution = resultset.nextSolution();
            if (matchILI || matchLemma) {
                if (matchILI)
                    threshold = conceptMatchThreshold;
                else
                    threshold=phraseMatchThreshold;

                if (matchMultiple) {
                    //System.out.println(solution);
                    if (checkIliLemmaThreshold(iliSize, solution.get("conceptcount").asLiteral().getInt(), solution.get("myconceptcount").asLiteral().getInt(), threshold)) {
                        rslt.add(solution.get("ev").asNode());
                    }
                } else {
                    if (checkIliLemmaThreshold(1, solution.get("conceptcount").asLiteral().getInt(), 1, threshold)) {
                        rslt.add(solution.get("ev").asNode());
                    }
                }
            } else {
                rslt.add(solution.get("ev").asNode());
            }
        }
        return rslt;
    }

    private static boolean checkIliLemmaThreshold(int mysize, int kssize, int nMatches, int matchThreshold) {

        //System.out.println(mysize);
        //System.out.println(kssize);
        //System.out.println(nMatches);
        //System.out.println(matchThreshold);
        if ((nMatches * 100 / mysize >= matchThreshold) &&
                (nMatches * 100 / kssize >= matchThreshold)) {
            return true;
        } else {
            return false;
        }
    }
}
