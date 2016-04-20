package eu.newsreader.eventcoreference.input;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.*;
import eu.newsreader.eventcoreference.storyline.JsonStoryUtil;
import eu.newsreader.eventcoreference.storyline.PerspectiveJsonObject;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createStatement;

/**
 * Created by filipilievski on 2/7/16.
 */
public class TrigKSTripleReader {

    public static int qCount = 0;
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


    static public String makeMentionQuery(ArrayList<String> uris) {
        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT distinct ?mention ?object WHERE { VALUES ?event {";
        for (int i = 0; i < uris.size(); i++) {
            String uri = uris.get(i);
            if (!uri.startsWith("<")) uri = "<"+uri+">";
            sparqlQuery +=  uri+" ";

        }
        sparqlQuery += "}\n" +
                "    ?event gaf:denotedBy ?mention\n" +
                "}";
        return sparqlQuery;
    }

    /**
     PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/>
     PREFIX owltime: <http://www.w3.org/TR/owl-time#>
     PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
     PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
     SELECT distinct ?event ?mention ?attribution ?author ?cite ?label ?comment WHERE { VALUES ?event {<http://www.ft.com/thing/b0d8195c-eb8d-11e5-888e-2eadd5fbc4a4#ev101> <http://www.ft.com/thing/bb5a999a-c67b-11e5-808f-8231cd71622e#ev129> <http://www.ft.com/thing/e5c73d14-92cd-11e5-94e6-c5413829caa5#ev57>}
     ?event <http://groundedannotationframework.org/gaf#denotedBy> ?mention
     OPTIONAL { ?mention rdfs:label ?label}
     OPTIONAL { ?mention rdfs:comment ?comment}
     OPTIONAL { ?mention <http://groundedannotationframework.org/grasp#hasAttribution> [ rdf:value ?attribution]}
     OPTIONAL { ?mention <http://groundedannotationframework.org/grasp#hasAttribution> [ <http://groundedannotationframework.org/grasp#wasAttributedTo> ?cite ]}
     OPTIONAL { ?mention <http://groundedannotationframework.org/grasp#hasAttribution> [ <http://www.w3.org/ns/prov#wasAttributedTo> [<http://www.w3.org/ns/prov#wasAttributedTo> ?author] ]}
     }
     <http://www.ft.com/thing/00937ca4-cffd-11e5-92a1-c5e23ef99c77#char=1207,1211>
     rdfs:comment          "said" ;
     rdfs:label            "loan" ;
     grasp:generatedBy     <http://www.ft.com/thing/00937ca4-cffd-11e5-92a1-c5e23ef99c77#char=1097,1101> ;
     grasp:hasAttribution  <http://www.ft.com/thing/00937ca4-cffd-11e5-92a1-c5e23ef99c77/source_attribution/Attr1> .

     <http://www.ft.com/thing/00937ca4-cffd-11e5-92a1-c5e23ef99c77/source_attribution/Attr1>
     rdf:value              grasp:CERTAIN_FUTURE_POS , grasp:u_u_u , grasp:CERTAIN_u_POS ,
     grasp:CERTAIN_NON_FUTURE_POS , grasp:PROBABLE_u_POS ;
     grasp:wasAttributedTo  <http://dbpedia.org/resource/Institute_for_Fiscal_Studies> .

     <http://www.ft.com/thing/00937ca4-cffd-11e5-92a1-c5e23ef99c77/doc_attribution/Attr0>
     rdf:value             grasp:u_NON_FUTURE_u ;
     prov:wasAttributedTo  <http://www.ft.com/thing/00937ca4-cffd-11e5-92a1-c5e23ef99c77> .

     <http://www.ft.com/thing/00937ca4-cffd-11e5-92a1-c5e23ef99c77>
     prov:wasAttributedTo  <http://www.newsreader-project.eu/provenance/author/Gonzalo+Vi%C3%B1a%2C+Public+Policy+Reporter> .

     * @param uris
     * @return
     */
    static public String makeAttributionQuery(ArrayList<String> uris) {
        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT distinct ?event ?mention ?attribution ?author ?cite ?label ?comment" +
                " WHERE { VALUES ?event {";
        for (int i = 0; i < uris.size(); i++) {
            sparqlQuery += uris.get(i);
        }
        sparqlQuery += "}\n" +
                "    ?event <http://groundedannotationframework.org/gaf#denotedBy> ?mention\n" +
                "     OPTIONAL { ?mention rdfs:label ?label}\n" +
                "     OPTIONAL { ?mention rdfs:comment ?comment}\n" +
                "     OPTIONAL { ?mention <http://groundedannotationframework.org/grasp#hasAttribution> [rdf:value  ?attribution]}\n" +
                "     OPTIONAL { ?mention <http://groundedannotationframework.org/grasp#hasAttribution> [<http://groundedannotationframework.org/grasp#wasAttributedTo> ?cite]}\n" +
                "     OPTIONAL { ?mention <http://groundedannotationframework.org/grasp#hasAttribution> [<http://www.w3.org/ns/prov#wasAttributedTo>  [<http://www.w3.org/ns/prov#wasAttributedTo> ?author]]}\n" +
                "}";
        return sparqlQuery;
    }


    public static ArrayList<JSONObject> readAttributionFromKs(ArrayList<JSONObject> targetEvents){
        long startTime = System.currentTimeMillis();
        HashMap<String, PerspectiveJsonObject> perspectiveMap = new HashMap<String, PerspectiveJsonObject>();

        ArrayList<JSONObject> pEvents = new ArrayList<JSONObject>();
        ArrayList<String> uris = new ArrayList<String>();
        HashMap<String, JSONObject> eventMap = new HashMap<String, JSONObject>();
        for (int i = 0; i < targetEvents.size(); i++) {
            JSONObject targetEvent = targetEvents.get(i);
            try {
                String eventUri = targetEvent.getString("instance");
                eventMap.put(eventUri, targetEvent);
                if (!eventUri.startsWith("<")) eventUri = "<"+eventUri+">";
                uris.add(eventUri);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        String sparqlQuery = makeAttributionQuery(uris);
      //  System.out.println("sparqlQuery = " + sparqlQuery);
        HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());
        try {
            QueryExecution x = QueryExecutionFactory.sparqlService(serviceEndpoint, sparqlQuery, authenticator);
            ResultSet resultset = x.execSelect();
            while (resultset.hasNext()) {
                QuerySolution solution = resultset.nextSolution();
                String event = "";
                String mention = "";
                String attribution = "";
                String cite = "";
                String author = "";
                String label = "";
                String comment = "";
                try { event = solution.get("event").toString(); } catch (Exception e) { }
                try { mention = solution.get("mention").toString(); } catch (Exception e) { }
                try { attribution = solution.get("attribution").toString(); } catch (Exception e) { }
                try { cite = solution.get("cite").toString(); } catch (Exception e) { }
                try { author = solution.get("author").toString(); } catch (Exception e) { }
                try { label = solution.get("label").toString(); } catch (Exception e) { }
                try { comment = solution.get("comment").toString(); } catch (Exception e) { }

                ArrayList<String> perspectives = JsonStoryUtil.normalizePerspectiveValue(attribution);
                if (!perspectives.isEmpty()) {
                    JSONObject targetEvent = eventMap.get(event);
                    if (targetEvent != null) {
                        if (perspectiveMap.containsKey(mention)) {
                            PerspectiveJsonObject perspectiveJsonObject = perspectiveMap.get(mention);
                            perspectiveJsonObject.addAttribution(perspectives);
                            perspectiveMap.put(mention, perspectiveJsonObject);
                        }
                        else {
                            PerspectiveJsonObject perspectiveJsonObject = new PerspectiveJsonObject(perspectives, author, cite, comment, event, label, mention, targetEvent);
                            perspectiveMap.put(mention, perspectiveJsonObject);
                        }

                    } else {
                        System.out.println("mention no target event = " + mention);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Set keySet = perspectiveMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            PerspectiveJsonObject perspectiveJsonObject = perspectiveMap.get(key);
            try {
                JSONObject perspectiveEvent = JsonStoryUtil.createSourcePerspectiveEvent(perspectiveJsonObject);
                pEvents.add(perspectiveEvent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Perspective Time elapsed:");
        System.out.println(estimatedTime/1000.0);
        System.out.println("pEvents = " + pEvents.size());
        return pEvents;
    }


    public static void integrateAttributionFromKs(ArrayList<JSONObject> targetEvents){
        long startTime = System.currentTimeMillis();
        HashMap<String, PerspectiveJsonObject> perspectiveMap = new HashMap<String, PerspectiveJsonObject>();

        ArrayList<JSONObject> pEvents = new ArrayList<JSONObject>();
        ArrayList<String> uris = new ArrayList<String>();
        HashMap<String, JSONObject> eventMap = new HashMap<String, JSONObject>();
        for (int i = 0; i < targetEvents.size(); i++) {
            JSONObject targetEvent = targetEvents.get(i);
            try {
                String eventUri = targetEvent.getString("instance");
                eventMap.put(eventUri, targetEvent);
                if (!eventUri.startsWith("<")) eventUri = "<"+eventUri+">";
                uris.add(eventUri);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        String sparqlQuery = makeAttributionQuery(uris);
      //  System.out.println("sparqlQuery = " + sparqlQuery);
        HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());
        try {
            QueryExecution x = QueryExecutionFactory.sparqlService(serviceEndpoint, sparqlQuery, authenticator);
            ResultSet resultset = x.execSelect();
            while (resultset.hasNext()) {
                QuerySolution solution = resultset.nextSolution();
                String event = "";
                String mention = "";
                String attribution = "";
                String cite = "";
                String author = "";
                String label = "";
                String comment = "";
                try { event = solution.get("event").toString(); } catch (Exception e) { }
                try { mention = "<"+solution.get("mention").toString()+">"; } catch (Exception e) { }
                try { attribution = solution.get("attribution").toString(); } catch (Exception e) { }
                try { cite = solution.get("cite").toString(); } catch (Exception e) { }
                try { author = solution.get("author").toString(); } catch (Exception e) { }
                try { label = solution.get("label").toString(); } catch (Exception e) { }
                try { comment = solution.get("comment").toString(); } catch (Exception e) { }

                ArrayList<String> perspectives = JsonStoryUtil.normalizePerspectiveValue(attribution);
                if (!perspectives.isEmpty()) {
                    JSONObject targetEvent = eventMap.get(event);
                    if (targetEvent != null) {
                       // System.out.println("mention input = " + mention);
                        if (perspectiveMap.containsKey(mention)) {
                            PerspectiveJsonObject perspectiveJsonObject = perspectiveMap.get(mention);
                            perspectiveJsonObject.addAttribution(perspectives);
                            perspectiveMap.put(mention, perspectiveJsonObject);
                        }
                        else {
                            PerspectiveJsonObject perspectiveJsonObject = new PerspectiveJsonObject(perspectives, author, cite, comment, event, label, mention, targetEvent);
                            perspectiveMap.put(mention, perspectiveJsonObject);
                        }

                    } else {
                        System.out.println("mention no target event = " + mention);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < targetEvents.size(); i++) {
            JSONObject mEvent = targetEvents.get(i);
            JSONArray mMentions = null;
            try {
                mMentions = (JSONArray) mEvent.get("mentions");
            } catch (JSONException e) {
            }
            if (mMentions!=null) {
                for (int m = 0; m < mMentions.length(); m++) {
                    try {
                        JSONObject mentionObject = (JSONObject) mMentions.get(m);
                        JSONArray uriObject = mentionObject.getJSONArray("uri");
                        JSONArray offsetArray = mentionObject.getJSONArray("char");
                        String mention = JsonStoryUtil.getURIforMention(uriObject, offsetArray);
                        //System.out.println("mention event = " + mention);
                        if (perspectiveMap.containsKey(mention)) {
                            JSONObject perpective = new JSONObject();
                            PerspectiveJsonObject perspectiveJsonObject = perspectiveMap.get(mention);
                            for (int n = 0; n < perspectiveJsonObject.getAttribution().size(); n++) {
                                String a =  perspectiveJsonObject.getAttribution().get(n);
                               // System.out.println("a = " + a);
                                perpective.append("attribution", a);
                            }
                            String source = JsonStoryUtil.normalizeSourceValue(perspectiveJsonObject.getSource());
                            if (!source.isEmpty()) {
                                perpective.put("source", source);
                                mentionObject.put("perspective", perpective);
                            }
                        }
                    } catch (JSONException e) {
                       // e.printStackTrace();
                    }
                }
            }
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Perspective Time elapsed:");
        System.out.println(estimatedTime/1000.0);
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
            qCount++;
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
    //<http://groundedannotationframework.org/grasp#hasAttribution>
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


/*    static public HashMap<String, Integer> getStats(String type) {
        //SELECT (COUNT (?label) AS ?no_labels) WHERE {?s rdf:type eso:Motion. ?s rdfs:label ?label } LIMIT 1000
    }*/

   /*SELECT ?label (COUNT(?label) AS ?count) where
    {?s rdf:type eso:Motion. ?s rdfs:label ?label }
    GROUP BY ?label LIMIT 200
    */

    /*
    SELECT ?label (COUNT(?label) AS ?count) where
 {?s rdf:type eso:Motion. ?s rdfs:label ?label }
GROUP BY ?label ORDER BY DESC(?count) LIMIT 1000
     */

    /*
    SELECT ?label (COUNT(?label) AS ?count) where
 {?s rdf:type eso:ChangeOfPossession .
  ?s rdfs:label ?label }
GROUP BY ?label ORDER BY DESC(?count)
     */
}
