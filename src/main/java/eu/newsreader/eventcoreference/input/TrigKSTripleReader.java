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
    public static String service = "https://knowledgestore2.fbk.eu";
    public static String serviceEndpoint = "https://knowledgestore2.fbk.eu/nwr/wikinews-new/sparql";
    //public static String serviceEndpoint = "https://knowledgestore2.fbk.eu/nwr/cars3/sparql";
    public static String user = "nwr_partner";
    public static String pass = "ks=2014!";
    public static String limit = "500";
    //public static String authStr = user + ":" + pass;

    HttpAuthenticator authenticator = new SimpleAuthenticator(user, pass.toCharArray());
    public static TrigTripleData trigTripleData = new TrigTripleData();

    static public void setServicePoint (String service, String ks) {
        if (ks.isEmpty()) {
            serviceEndpoint = service+ "/sparql";
        }
        else {
            serviceEndpoint = service + "/" + ks + "/sparql";
        }
    }

    static public void setServicePoint (String service, String ks, String username, String password) {
        //serviceEndpoint = "https://knowledgestore2.fbk.eu/"+ks+"/sparql";
        setServicePoint(service, ks);
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
        //System.out.println("sparqlQuery = " + sparqlQuery);
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

                ArrayList<String> perspectives = PerspectiveJsonObject.normalizePerspectiveValue(attribution);
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
                      //  System.out.println("mention no target event = " + mention);
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
        System.out.println(" * Perspective Time elapsed:"+estimatedTime/1000.0);
        //System.out.println("pEvents = " + pEvents.size());
        return pEvents;
    }


    public static void integrateAttributionFromKs(ArrayList<JSONObject> targetEvents){
        long startTime = System.currentTimeMillis();
        HashMap<String, ArrayList<PerspectiveJsonObject>> perspectiveMap = new HashMap<String, ArrayList<PerspectiveJsonObject>>();

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
       // System.out.println("sparqlQuery = " + sparqlQuery);
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

                if (author.isEmpty() && cite.isEmpty()) {
                    author = "unknown";
                }
                ArrayList<String> perspectives = PerspectiveJsonObject.normalizePerspectiveValue(attribution);
                if (!perspectives.isEmpty()) {
                    JSONObject targetEvent = eventMap.get(event);
                    if (targetEvent != null) {
                        PerspectiveJsonObject perspectiveJsonObject = new PerspectiveJsonObject(perspectives, author, cite, comment, event, label, mention, targetEvent);
                        if (perspectiveMap.containsKey(mention)) {
                            ArrayList<PerspectiveJsonObject> perspectiveJsonObjects = perspectiveMap.get(mention);
                            perspectiveJsonObjects.add(perspectiveJsonObject);
                            perspectiveMap.put(mention, perspectiveJsonObjects);
                        }
                        else {
                            ArrayList<PerspectiveJsonObject> perspectiveJsonObjects = new ArrayList<PerspectiveJsonObject>();
                            perspectiveJsonObjects.add(perspectiveJsonObject);
                            perspectiveMap.put(mention, perspectiveJsonObjects);
                        }

                    } else {
                      //  System.out.println("Error: mention without target event = " + mention);
                    }
                }
                else {
                  //  System.out.println("No perspectives for this event.");
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
                e.printStackTrace();
            }
            if (mMentions!=null) {
                for (int m = 0; m < mMentions.length(); m++) {
                    try {
                        JSONObject mentionObject = (JSONObject) mMentions.get(m);
                        String uriString = mentionObject.getString("uri");
                        JSONArray offsetArray = mentionObject.getJSONArray("char");
                        String mention = JsonStoryUtil.getURIforMention(uriString, offsetArray);
                        if (perspectiveMap.containsKey(mention)) {
                            ArrayList<PerspectiveJsonObject> perspectiveJsonObjects = perspectiveMap.get(mention);
                            for (int j = 0; j < perspectiveJsonObjects.size(); j++) {
                                PerspectiveJsonObject perspectiveJsonObject = perspectiveJsonObjects.get(j);
                                JsonStoryUtil.addPerspectiveToMention(mentionObject, perspectiveJsonObject);

                            }
                        }
                        else {
                            PerspectiveJsonObject dymmyPerspective = new PerspectiveJsonObject();
                            JsonStoryUtil.addPerspectiveToMention(mentionObject, dymmyPerspective);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                /*try {
                    System.out.println("No mentions for target = "+mEvent.getString("instance"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
            }
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(" * Perspective Time elapsed:"+estimatedTime/1000.0);
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

    static public String makeLabelFilter(String variable, String query) {
        //FILTER ( regex(str(?entlabel), "Bank") || regex(str(?entlabel), "Dank")) .

        String filter = "FILTER (";
        String[] fields = query.split(";");
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].replace('^', ' ');;
            if (i>0)  filter +=" || ";
            filter += "regex(str("+variable+"), \"^"+field+"$\")";
        }
        filter += ") .\n" ;
        return filter;
    }

    static public String makeSubStringLabelFilter(String variable, String query) {
        //FILTER ( regex(str(?entlabel), "Bank") || regex(str(?entlabel), "Dank")) .

        String filter = "FILTER (";
        String[] fields = query.split(";");
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].replace('^', ' ');;
            if (i>0)  filter +=" || ";
            filter += "regex(str("+variable+"), \""+field+"\")";
        }
        filter += ") .\n" ;
        return filter;
    }

    static public String makeTypeFilter(String variable, String query) {
        //FILTER ( regex(str(?entlabel), "Bank") || regex(str(?entlabel), "Dank")) .
        // "?event rdf:type " + eventType + " .\n" +
        // { ?event rdf:type eso:Buying } UNION {?event rdf:type eso:Selling }
        String filter = "{ ";
        String[] fields = query.split(";");
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].replace('^', ' ');;
            if (i>0)  filter +=" UNION ";
            filter += " { "+variable+" rdf:type "+field+" } ";
        }
        filter += " }\n" ;
        return filter;
    }

    static public String makeInstanceFilter(String variable, String query) {
        // "?event sem:hasActor ?ent .\n" +
        String filter = "{ ";
        String[] fields = query.split(";");
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].replace('^', ' ');;
            if (i>0)  filter +=" UNION ";
            filter += " { "+variable+" sem:hasActor "+field+" } ";
        }
        filter += " }\n" ;
        return filter;
    }

    static public void readTriplesFromKSforEntity(String entityQuery){
        String types = "";
        String instances = "";
        String labels = "";
        String [] fields = entityQuery.split(";");
       // System.out.println("entityQuery = " + entityQuery);
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].trim().replace('^', ' ');
           // field = multiwordFix(field);
            if (field.indexOf("dbp:")>-1) {
                if (!types.isEmpty()) types += ";";
                types += field;
            }
            else if (field.indexOf("dbpedia:")>-1) {
                if (!instances.isEmpty()) instances += ";";
                instances += field;
            }
            else {
                if (!labels.isEmpty()) labels += ";";
                labels += field;
            }
        }
        if (!labels.isEmpty()) readTriplesFromKSforEntityLabel(labels);
        if (!types.isEmpty()) readTriplesFromKSforEntityType(types);
        if (!instances.isEmpty()) readTriplesFromKSforEntityInstance(instances);
    }

    static public void readTriplesFromKSforSource(String sourceQuery){
        String [] fields = sourceQuery.split(";");
       // System.out.println("entityQuery = " + entityQuery);
        String sources = "";
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].trim().replace('^', ' ');
            if (!sources.isEmpty()) sources += ";";
            sources += field;
        }
        if (!sources.isEmpty()) {
            readTriplesFromKSforCitedSurfaceForm(sources);
            readTriplesFromKSforAuthorSurfaceForm(sources);
        }
    }

    static public void readTriplesFromKSforEvents(String eventQuery){
        String types = "";
        String labels = "";
        String [] fields = eventQuery.split(";");
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].trim().replace('^', ' ');
            //field = multiwordFix(field);
            if (field.indexOf(":")>-1) {
                if (!types.isEmpty()) types += ";";
                types += field;
            }
            else {
                if (!labels.isEmpty()) labels += ";";
                labels += field;
            }
        }
        if (!labels.isEmpty()) readTriplesFromKSforEventLabel(labels);
        if (!types.isEmpty()) readTriplesFromKSforEventType(types);
    }


    static public void readTriplesFromKSforEntityLabel(String entityLabel){
        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT ?event ?relation ?object ?indatetime ?begintime ?endtime \n" +
                "WHERE {\n" +
                "{SELECT distinct ?event WHERE { \n" +
                //makeLabelFilter("?entlabel",entityLabel) +
                makeSubStringLabelFilter("?entlabel",entityLabel) +
                "?ent rdfs:label ?entlabel .\n" +
                "?event sem:hasActor ?ent .\n" +
                "} LIMIT "+limit+" }\n" +
                "?event ?relation ?object .\n" +
                "OPTIONAL { ?object rdf:type owltime:Instant ; owltime:inDateTime ?indatetime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasBeginning ?begintime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasEnd ?endtime }" +
                "} ORDER BY ?event";
        //System.out.println("sparqlQuery = " + sparqlQuery);
        readTriplesFromKs(sparqlQuery);
    }

    static public void readTriplesFromKSforEntityType(String entityType){
        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX eso: <http://www.newsreader-project.eu/domain-ontology#> \n" +
                "PREFIX fn: <http://www.newsreader-project.eu/ontologies/framenet/> \n" +
                "PREFIX dbp: <http://dbpedia.org/ontology/> \n" +
                "PREFIX dbpedia: <http://dbpedia.org/resource/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT ?event ?relation ?object ?indatetime ?begintime ?endtime \n" +
                "WHERE {\n" +
                "{SELECT distinct ?event WHERE { \n" +
                "?event sem:hasActor ?ent .\n" +
                makeTypeFilter("?ent", entityType) +
                //"?ent rdf:type " + entityType + " .\n" +
                "} LIMIT "+limit+" }\n" +
                "?event ?relation ?object .\n" +
                "OPTIONAL { ?object rdf:type owltime:Instant ; owltime:inDateTime ?indatetime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasBeginning ?begintime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasEnd ?endtime }" +
                "} ORDER BY ?event";
        //System.out.println("sparqlQuery = " + sparqlQuery);
        readTriplesFromKs(sparqlQuery);
    }

    static public void readTriplesFromKSforEntityInstance(String entityType){
        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX eso: <http://www.newsreader-project.eu/domain-ontology#> \n" +
                "PREFIX fn: <http://www.newsreader-project.eu/ontologies/framenet/> \n" +
                "PREFIX dbp: <http://dbpedia.org/ontology/> \n" +
                "PREFIX dbpedia: <http://dbpedia.org/resource/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT ?event ?relation ?object ?indatetime ?begintime ?endtime \n" +
                "WHERE {\n" +
                "{SELECT distinct ?event WHERE { \n" +
                //"?event sem:hasActor ?ent .\n" +
                makeInstanceFilter("?event", entityType) +
                //"?ent rdf:type " + entityType + " .\n" +
                "} LIMIT "+limit+" }\n" +
                "?event ?relation ?object .\n" +
                "OPTIONAL { ?object rdf:type owltime:Instant ; owltime:inDateTime ?indatetime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasBeginning ?begintime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasEnd ?endtime }" +
                "} ORDER BY ?event";
        //System.out.println("sparqlQuery = " + sparqlQuery);
        readTriplesFromKs(sparqlQuery);
    }

    static public void readTriplesFromKSforEventType(String eventType){
        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX eso: <http://www.newsreader-project.eu/domain-ontology#> \n" +
                "PREFIX fn: <http://www.newsreader-project.eu/ontologies/framenet/> \n" +
                "PREFIX dbp: <http://dbpedia.org/ontology/> \n" +
                "PREFIX dbpedia: <http://dbpedia.org/resource/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT ?event ?relation ?object ?indatetime ?begintime ?endtime \n" +
                "WHERE {\n" +
                "{SELECT distinct ?event WHERE { \n" +
                makeTypeFilter("?event", eventType) +
                //"?event rdf:type " + eventType + " .\n" +
                "} LIMIT "+limit+" }\n" +
                "?event ?relation ?object .\n" +
                "OPTIONAL { ?object rdf:type owltime:Instant ; owltime:inDateTime ?indatetime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasBeginning ?begintime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasEnd ?endtime }" +
                "} ORDER BY ?event";
        // System.out.println("sparqlQuery = " + sparqlQuery);
        readTriplesFromKs(sparqlQuery);
    }

    static public void readTriplesFromKSforEventLabel(String eventLabel){
        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX eso: <http://www.newsreader-project.eu/domain-ontology#> \n" +
                "PREFIX fn: <http://www.newsreader-project.eu/ontologies/framenet/> \n" +
                "PREFIX dbp: <http://dbpedia.org/ontology/> \n" +
                "PREFIX dbpedia: <http://dbpedia.org/resource/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT ?event ?relation ?object ?indatetime ?begintime ?endtime \n" +
                "WHERE {\n" +
                "{SELECT distinct ?event WHERE { \n" +
                makeLabelFilter("?eventlabel",eventLabel) +
                "?event rdfs:label ?eventlabel .\n" +
                "} LIMIT "+limit+" }\n" +
                "?event ?relation ?object .\n" +
                "OPTIONAL { ?object rdf:type owltime:Instant ; owltime:inDateTime ?indatetime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasBeginning ?begintime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasEnd ?endtime }" +
                "} ORDER BY ?event";
        // System.out.println("sparqlQuery = " + sparqlQuery);
        readTriplesFromKs(sparqlQuery);
    }


    static public void readTriplesFromKSforEntityAndEvent(String entityType, String eventType){
        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX eso: <http://www.newsreader-project.eu/domain-ontology#> \n" +
                "PREFIX fn: <http://www.newsreader-project.eu/ontologies/framenet/> \n" +
                "PREFIX dbp: <http://dbpedia.org/ontology/> \n" +
                "PREFIX dbpedia: <http://dbpedia.org/resource/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT ?event ?relation ?object ?indatetime ?begintime ?endtime \n" +
                "WHERE {\n" +
                "{SELECT distinct ?event WHERE { \n" +
                makeTypeFilter("?event", eventType) +
                //"?event rdf:type " + eventType + " .\n" +
                "?event sem:hasActor ?ent .\n" +
                makeTypeFilter("?ent", entityType) +
                //"?ent rdf:type " + entityType + " .\n" +
                "} LIMIT "+limit+" }\n" +
                "?event ?relation ?object .\n" +
                "OPTIONAL { ?object rdf:type owltime:Instant ; owltime:inDateTime ?indatetime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasBeginning ?begintime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasEnd ?endtime }" +
                "} ORDER BY ?event";
        //System.out.println("sparqlQuery = " + sparqlQuery);
        readTriplesFromKs(sparqlQuery);
    }

    static public void readTriplesFromKSforEntityEventType(String entityLabel, String filter){


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
                "PREFIX dbp: <http://dbpedia.org/ontology/> \n" +
                "PREFIX dbpedia: <http://dbpedia.org/resource/> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT ?event ?relation ?object ?indatetime ?begintime ?endtime \n" +
                "WHERE {\n" +
                "{SELECT distinct ?event WHERE { \n" +
                makeLabelFilter("?entlabel",entityLabel) +
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
        readTriplesFromKs(sparqlQuery);
    }

    static public void readTriplesFromKSforSurfaceSubForm(String entityLabel, String filter){
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
                "PREFIX dbp: <http://dbpedia.org/ontology/> \n" +
                "PREFIX dbpedia: <http://dbpedia.org/resource/> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "SELECT ?event ?relation ?object ?indatetime ?begintime ?endtime \n" +
                "WHERE {\n" +
                "{SELECT distinct ?event WHERE { \n" +
                makeSubStringLabelFilter("?entlabel", entityLabel) +
                "?ent rdfs:label ?entlabel .\n" +
                "?event sem:hasActor ?ent .\n" +
                eventFilter +
                "} LIMIT "+limit+" }\n" +
                "?event ?relation ?object .\n" +
                "OPTIONAL { ?object rdf:type owltime:Instant ; owltime:inDateTime ?indatetime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasBeginning ?begintime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasEnd ?endtime }" +
                "} ORDER BY ?event";
        // System.out.println("sparqlQuery = " + sparqlQuery);
        readTriplesFromKs(sparqlQuery);
    }

    static public void readTriplesFromKSforCitedSurfaceForm(String citedLabel){

        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX grasp: <http://groundedannotationframework.org/grasp#>\n" +
                "PREFIX gaf:   <http://groundedannotationframework.org/gaf#>\n" +
                "SELECT ?event ?relation ?object ?indatetime ?begintime ?endtime \n" +
                "WHERE {\n" +
                "{SELECT distinct ?event WHERE { \n" +
                "?event gaf:denotedBy ?mention.\n" +
                "?mention grasp:hasAttribution ?attribution.\n" +
                "?attribution grasp:wasAttributedTo ?cite.\n" +
                makeSubStringLabelFilter("?cite", citedLabel) +
               // "FILTER (regex(str(?cite), \"Agral\")) .\n" +
                "} LIMIT 1000 }\n" +
                "?event ?relation ?object .\n" +
                "OPTIONAL { ?object rdf:type owltime:Instant ; owltime:inDateTime ?indatetime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasBeginning ?begintime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasEnd ?endtime }} ORDER BY ?event";
        // System.out.println("sparqlQuery = " + sparqlQuery);
        readTriplesFromKs(sparqlQuery);
    }

    static public void readTriplesFromKSforAuthorSurfaceForm(String authorLabel){
        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX grasp: <http://groundedannotationframework.org/grasp#>\n" +
                "PREFIX gaf:   <http://groundedannotationframework.org/gaf#>\n" +
                "PREFIX prov:  <http://www.w3.org/ns/prov#>\n" +
                "SELECT ?event ?relation ?object ?indatetime ?begintime ?endtime \n" +
                "WHERE {\n" +
                "{SELECT distinct ?event WHERE { \n" +
                "?event gaf:denotedBy ?mention.\n" +
                "?mention grasp:hasAttribution ?attribution.\n" +
                "?attribution prov:wasAttributedTo ?author .\n" +
                makeSubStringLabelFilter("?author", authorLabel) +
                // "FILTER (regex(str(?author), \"u\"))\n" +
                "} LIMIT 1000 }\n" +
                "?event ?relation ?object .\n" +
                "OPTIONAL { ?object rdf:type owltime:Instant ; owltime:inDateTime ?indatetime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasBeginning ?begintime }\n" +
                "OPTIONAL { ?object rdf:type owltime:Interval ; owltime:hasEnd ?endtime }} ORDER BY ?event";
        // System.out.println("sparqlQuery = " + sparqlQuery);
        readTriplesFromKs(sparqlQuery);
    }




    static public void readTriplesFromKSforTopic(String topic){
        String sparqlQuery = "PREFIX sem: <http://semanticweb.cs.vu.nl/2009/11/sem/> \n" +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n" +
                "PREFIX eurovoc: <http://eurovoc.europa.eu/> \n" +
                "PREFIX owltime: <http://www.w3.org/TR/owl-time#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "PREFIX dbp: <http://dbpedia.org/ontology/> \n" +
                "PREFIX dbpedia: <http://dbpedia.org/resource/> \n" +
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
        // readTriplesFromKs(sparqlQuery);
    }


    public static void readTriplesFromKs(String sparqlQuery){
        //System.out.println("serviceEndpoint = " + serviceEndpoint);
        //System.out.println("sparqlQuery = " + sparqlQuery);
        //System.out.println("user = " + user);
        //System.out.println("pass = " + pass);
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
        System.out.println(" * instance statements = "+trigTripleData.tripleMapInstances.size());
        System.out.println(" * sem statements = " + trigTripleData.tripleMapOthers.size());
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

        readTriplesFromKSforEntityAndEvent("dbp:Company","fn:Arriving");
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
