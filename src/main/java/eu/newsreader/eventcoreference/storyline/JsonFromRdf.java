package eu.newsreader.eventcoreference.storyline;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import eu.newsreader.eventcoreference.input.EsoReader;
import eu.newsreader.eventcoreference.input.FrameNetReader;
import eu.newsreader.eventcoreference.input.TrigTripleData;
import eu.newsreader.eventcoreference.input.TrigUtil;
import eu.newsreader.eventcoreference.objects.Triple;
import eu.newsreader.eventcoreference.util.RoleLabels;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import static eu.newsreader.eventcoreference.storyline.JsonStoryUtil.normalizeSourceValue;

/**
 * Created by piek on 17/02/16.
 */
@Deprecated
public class JsonFromRdf {

    static public String getTimeAnchor (HashMap<String, ArrayList<Statement>> tripleMapInstances,
                                 ArrayList<Statement> triples) {
        for (int i = 0; i < triples.size(); i++) {
            Statement statement = triples.get(i);
            String predicate = statement.getPredicate().getURI();
            if (predicate.toLowerCase().endsWith("hasattime")) {
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
                        /**    WHAT TO DO WITH PERIODS????
                         *             time:hasBeginning  <http://www.newsreader-project.eu/time/20100125> ;
                         time:hasEnd        <http://www.newsreader-project.eu/time/20100125> .

                         */
                        if (timeStatement.getPredicate().getURI().toLowerCase().endsWith("indatetime")
                                ||timeStatement.getPredicate().getURI().toLowerCase().endsWith("hasbeginning")
                                || timeStatement.getPredicate().getURI().toLowerCase().endsWith("hasend")) {
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

    static public void getFrameNetSuperFramesJSONObjectFromInstanceStatement (FrameNetReader frameNetReader,
                                                                              ArrayList<String> topFrames,
                                                                              JSONObject parent,
                                                                              ArrayList<Statement> statements
    ) throws JSONException {
        ArrayList<String> coveredValues = new ArrayList<String>();
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
                    if (property.equalsIgnoreCase("fn")) {
                        value = getValue(value);
                        // System.out.println("value = " + value);
                        ArrayList<String> parents = new ArrayList<String>();

                        frameNetReader.getParentChain(value, parents, topFrames);
                        if (parents.size()==0) {
                            parent.append("fnsuperframes", value);
                        }
                        else {
                            for (int k = 0; k < parents.size(); k++) {
                                String parentFrame = parents.get(k);
                                if (!coveredValues.contains(parentFrame)) {
                                    coveredValues.add(parentFrame);
                                    parent.append("fnsuperframes", parentFrame);
                                }
                            }
                        }
                        //  System.out.println("value = " + value);
                        //  System.out.println("\tparents = " + parents.toString());

                       /*   String superFrame = "";
                            if (frameNetReader.subToSuperFrame.containsKey(value)) {
                            ArrayList<String> superFrames = frameNetReader.subToSuperFrame.get(value);
                            for (int k = 0; k < superFrames.size(); k++) {
                                superFrame =  superFrames.get(k);
                                if (!coveredValues.contains(superFrame)) {
                                    coveredValues.add(superFrame);
                                    parent.append("fnsuperframes", superFrame);
                                }

                            }
                        }*/
                    }
                }
            }
        }
    }

    static JSONObject getClassesJSONObjectFromInstanceStatement (ArrayList<Statement> statements) throws JSONException {
        JSONObject jsonClassesObject = new JSONObject();
        ArrayList<String> coveredValues = new ArrayList<String>();

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
                        if (!coveredValues.contains(property+value)) {
                            coveredValues.add(property+value);
                            jsonClassesObject.append(property, value);
                        }
                    }
                }
            }
        }
        return jsonClassesObject;
    }

    static boolean matchEventType (ArrayList<Statement> statements, String eventTypes) throws JSONException {

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
                        if (eventTypes.toLowerCase().indexOf(property)>-1) {
                           return true;
                        }
                    }
                }
            }
        }
        return false;
    }



    static void getEsoSuperClassesJSONObjectFromInstanceStatement (EsoReader esoReader,
                                                                   int esoLevel,
                                                                   JSONObject parent,
                                                                   ArrayList<Statement> statements
    ) throws JSONException {
        ArrayList<String> coveredValues = new ArrayList<String>();
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
                    if (property.equalsIgnoreCase("eso")) {
                        value = getValue(value);
                        ArrayList<String> parents = new ArrayList<String>();
                        String parentClass = "";
                        esoReader.simpleTaxonomy.getParentChain(value, parents);
                        if (parents.size()==0) {
                            parentClass = value;
                        }
                        else {
                            if (esoLevel>parents.size()) {
                                // parentClass = parents.get(parents.size()-1)+"_"+value;
                                parentClass = value;
                            }
                            else {
                                int p = parents.size()-esoLevel;
                                parentClass = parents.get(p)+"_"+value;
                            }

                            //  parent.append("esosuperclasses", parents.get(parents.size()));

/*
                            for (int k = 0; k < parents.size(); k++) {
                                String parentFrame = parents.get(k);
                                if (!coveredValues.contains(parentFrame)) {
                                    coveredValues.add(parentFrame);
                                    parent.append("esosuperclasses", parentFrame);
                                }
                            }
*/
                        }
/*
                        System.out.println("value = " + value);
                        System.out.println("parents.toString() = " + parents.toString());
                        System.out.println("parentClass = " + parentClass);
*/
                        parent.append("esosuperclasses", parentClass);

                    }
                }
            }
        }
    }

    static JSONObject getActorsJSONObjectFromInstanceStatementORG (ArrayList<Statement> statements,
                                                                   String ACTORNAMESPACES) throws JSONException {
        JSONObject jsonActorsObject = new JSONObject();
        ArrayList<String> esoActors = new ArrayList<String>();
        ArrayList<String> fnActors = new ArrayList<String>();
        ArrayList<String> pbActors = new ArrayList<String>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            if (predicate.toLowerCase().endsWith("time")) {
                ///
            }
            else if (predicate.toLowerCase().endsWith("hasactor")) {
                ///
            }
            else {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String property = getNameSpaceString(predicate);
                if (!property.isEmpty()) {


                    if (ACTORNAMESPACES.indexOf(property)>-1 || ACTORNAMESPACES.isEmpty()) {
                        if (property.equalsIgnoreCase("pb")) {
                            predicate = property + "/" + RoleLabels.normalizeProbBankValue(getValue(predicate));
                        }
                        else {
                            predicate = property + "/" + getValueWithoutFrame(getValue(predicate));
                            // System.out.println("property = " + property);
                        }
                        String[] values = object.split(",");
                        ArrayList<String> coveredValues = new ArrayList<String>();
                        for (int j = 0; j < values.length; j++) {
                            String value = values[j];
                            String ns = getNameSpaceString(value);
                            if (!ns.isEmpty()) {
                                value = ns+":"+ getValue(value);
                            }
                            value = value.replace("+", "_"); //// just to make it look nicer
                            if (!coveredValues.contains(value)) {
                                coveredValues.add(value);
                                jsonActorsObject.append(predicate, value);
                                /*if (actorCount.containsKey(value)) {
                                    Integer cnt = actorCount.get(value);
                                    cnt++;
                                    actorCount.put(value, cnt);
                                }
                                else {
                                    actorCount.put(value,1);
                                }*/
                            }
                        }
                    }
                }
            }
        }
        return jsonActorsObject;
    }

    static JSONObject getActorsJSONObjectFromInstanceStatementSimple (ArrayList<Statement> statements) throws JSONException {
        ArrayList<String> coveredActors = new ArrayList<String>();
        JSONObject jsonActorsObject = new JSONObject();
        ArrayList<Triple> eventTriples = new ArrayList<Triple>();
        ArrayList<String> esoActors = new ArrayList<String>();
        ArrayList<String> fnActors = new ArrayList<String>();
        ArrayList<String> pbActors = new ArrayList<String>();
        //System.out.println("statements = " + statements.size());
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            String predicate = statement.getPredicate().getURI();
            if (predicate.toLowerCase().endsWith("time")) {
                ///
            }
            else {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String property = getNameSpaceString(predicate);
                if (!property.isEmpty()) {
                    //System.out.println("property = " + property);
                    if (property.equalsIgnoreCase("pb")) {
                        predicate = property + "/" + RoleLabels.normalizeProbBankValue(getValue(predicate));
                    }
                    else {
                        predicate = property + "/" + getValueWithoutFrame(getValue(predicate));
                        // System.out.println("property = " + property);
                    }
                    String[] values = object.split(",");
                    for (int j = 0; j < values.length; j++) {
                        String value = values[j];
                        String ns = getNameSpaceString(value);
                        if (!ns.isEmpty()) {
                            value = ns + ":" + getValue(value);
                        }
                        value = value.replace("+", "_"); //// just to make it look nicer
                        if (property.equalsIgnoreCase("pb")) {
                            if (!pbActors.contains(value)) {
                                pbActors.add(value);
                            }
                        }
                        else if (property.equalsIgnoreCase("fn")) {
                            if (!fnActors.contains(value)) {
                                fnActors.add(value);
                            }
                        }
                        else if (property.equalsIgnoreCase("eso")) {
                            if (!esoActors.contains(value)) {
                                esoActors.add(value);
                            }
                        }
                        Triple triple = new Triple();
                        triple.setPredicate(predicate);
                        triple.setObject(value);
                        eventTriples.add(triple);
                    }
                }
                else {
                    System.out.println("predicate = " + predicate);
                }
            }
        }
        //// ESO takes priority, then FN then all other roles
        /// we only add actors with pb if the object does not have a fn relation
        /// and we add actors with fn relations only if they do not have a eso relation
        /// finally add all others
        for (int i = 0; i < eventTriples.size(); i++) {
            Triple triple = eventTriples.get(i);
            if (triple.getPredicate().startsWith("eso")) {
                if (!coveredActors.contains(triple.getObject())) {
                    jsonActorsObject.append(triple.getPredicate(), triple.getObject());
                    coveredActors.add(triple.getObject());
                }
            }
            else if (!esoActors.contains((triple.getObject())) && triple.getPredicate().startsWith("fn")) {
                if (!coveredActors.contains(triple.getObject())) {
                    jsonActorsObject.append(triple.getPredicate(), triple.getObject());
                    coveredActors.add(triple.getObject());
                }
            }
            else if (!esoActors.contains((triple.getObject())) && !fnActors.contains((triple.getObject()))) {
                if (!coveredActors.contains(triple.getObject())) {
                    jsonActorsObject.append(triple.getPredicate(), triple.getObject());
                    coveredActors.add(triple.getObject());
                }
            }
        }
        return jsonActorsObject;
    }

    static JSONObject getActorsJSONObjectFromInstanceStatement (ArrayList<Statement> statements,
                                                                ArrayList<String> blacklist) throws JSONException {
        ArrayList<String> coveredActors = new ArrayList<String>();
        JSONObject jsonActorsObject = new JSONObject();
        ArrayList<Triple> eventTriples = new ArrayList<Triple>();
        ArrayList<String> esoActors = new ArrayList<String>();
        ArrayList<String> fnActors = new ArrayList<String>();
        ArrayList<String> pbActors = new ArrayList<String>();
       // System.out.println("statements = " + statements.size());
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            String predicate = statement.getPredicate().getURI();
            if (predicate.toLowerCase().endsWith("time")) {
                ///
            }
            else if (predicate.toLowerCase().endsWith("hasactor")) {
                ///
            }
            else {
              //  System.out.println("statement.asTriple().toString() = " + statement.asTriple().toString());

                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String property = getNameSpaceString(predicate);
                if (!property.isEmpty()) {
                    //System.out.println("property = " + property);
                    if (property.equalsIgnoreCase("pb")) {
                        predicate = property + "/" + RoleLabels.normalizeProbBankValue(getValue(predicate));
                    }
                    else {
                        predicate = property + "/" + getValueWithoutFrame(getValue(predicate));
                        // System.out.println("property = " + property);
                    }
                  //  System.out.println("property = " + property);

                    String[] values = object.split(",");
                    for (int j = 0; j < values.length; j++) {
                        String value = values[j];
                        String ns = getNameSpaceString(value);

                        /// hack
                        /// skip non entities of single words
                        if (ns.equalsIgnoreCase("ne")) {
                            String label = value;
                            int idx = value.lastIndexOf("/");
                            if (idx>-1) {
                                label = value.substring(idx+1);
                            }
                           // System.out.println("label = " + label);

                            if (blacklist.contains(label.toLowerCase())) {
                                continue;
                            }
                            ns = "co";
                        }

/*                        if (!tooTinyValue(getValue(value))) {
                            ///// HACK to remove very short lower case actors
                            if (!ns.isEmpty()) {
                                value = ns + ":" + getValue(value);
                            }

                            value = value.replace("+", "_"); //// just to make it look nicer
                            if (property.equalsIgnoreCase("pb")) {
                                if (!pbActors.contains(value)) {
                                    pbActors.add(value);
                                }
                            } else if (property.equalsIgnoreCase("fn")) {
                                if (!fnActors.contains(value)) {
                                    fnActors.add(value);
                                }
                            } else if (property.equalsIgnoreCase("eso")) {
                                if (!esoActors.contains(value)) {
                                    esoActors.add(value);
                                }
                            }
                            Triple triple = new Triple();
                            triple.setPredicate(predicate);
                            triple.setObject(value);
                            eventTriples.add(triple);
                        }*/

                        if (!ns.isEmpty()) {
                            value = ns + ":" + getValue(value);
                        }

                        value = value.replace("+", "_"); //// just to make it look nicer
                        if (property.equalsIgnoreCase("pb")) {
                            if (!pbActors.contains(value)) {
                                pbActors.add(value);
                            }
                        } else if (property.equalsIgnoreCase("fn")) {
                            if (!fnActors.contains(value)) {
                                fnActors.add(value);
                            }
                        } else if (property.equalsIgnoreCase("eso")) {
                            if (!esoActors.contains(value)) {
                                esoActors.add(value);
                            }
                        }
                        Triple triple = new Triple();
                        triple.setPredicate(predicate);
                        triple.setObject(value);
                        eventTriples.add(triple);
                    }
                }
                else {
                    System.out.println("predicate = " + predicate);
                }
            }
        }
        //// ESO takes priority, then FN then all other roles
        /// we only add actors with pb if the object does not have a fn relation
        /// and we add actors with fn relations only if they do not have a eso relation
        /// finally add all others
        for (int i = 0; i < eventTriples.size(); i++) {
            Triple triple = eventTriples.get(i);
            String actor = triple.getObject();
            actor = normalizeSourceValue(actor);

            // System.out.println("triple.getPredicate() = " + triple.getPredicate());
            if (triple.getPredicate().startsWith("eso")) {
                if (!coveredActors.contains(actor)) {
                    jsonActorsObject.append(triple.getPredicate(),actor);
                    coveredActors.add(actor);
                }
            }
            else if (!esoActors.contains((actor)) &&
                     triple.getPredicate().startsWith("fn")) {
                if (!coveredActors.contains(actor)) {
                    jsonActorsObject.append(triple.getPredicate(), actor);
                    coveredActors.add(actor);
                }
            }
            else if (!esoActors.contains((actor)) &&
                    !fnActors.contains((actor))) {
                if (!coveredActors.contains(actor)) {
                    jsonActorsObject.append(triple.getPredicate(), actor);
                    coveredActors.add(actor);
                }
            }
        }
        return jsonActorsObject;
    }

    static JSONObject getActorsJSONObjectFromInstanceStatementBU (ArrayList<Statement> statements) throws JSONException {
        ArrayList<String> coveredActors = new ArrayList<String>();
        JSONObject jsonActorsObject = new JSONObject();
        ArrayList<Triple> eventTriples = new ArrayList<Triple>();
        ArrayList<String> esoActors = new ArrayList<String>();
        ArrayList<String> fnActors = new ArrayList<String>();
        ArrayList<String> pbActors = new ArrayList<String>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            if (predicate.toLowerCase().endsWith("time")) {
                ///
            }
            else if (predicate.toLowerCase().endsWith("hasactor")) {
                ///
            }
            else {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String property = getNameSpaceString(predicate);
                if (!property.isEmpty()) {
                    if (property.equalsIgnoreCase("pb")) {
                        predicate = property + "/" + RoleLabels.normalizeProbBankValue(getValue(predicate));
                    }
                    else {
                        predicate = property + "/" + getValueWithoutFrame(getValue(predicate));
                        // System.out.println("property = " + property);
                    }
                    String[] values = object.split(",");
                    for (int j = 0; j < values.length; j++) {
                        String value = values[j];
                        String ns = getNameSpaceString(value);
                        if (!ns.isEmpty()) {
                            value = ns + ":" + getValue(value);
                        }
                        value = value.replace("+", "_"); //// just to make it look nicer
                        if (property.equalsIgnoreCase("pb")) {
                            if (!pbActors.contains(value)) {
                                pbActors.add(value);
                            }
                        }
                        else if (property.equalsIgnoreCase("fn")) {
                            if (!fnActors.contains(value)) {
                                fnActors.add(value);
                            }
                        }
                        else if (property.equalsIgnoreCase("eso")) {
                            if (!esoActors.contains(value)) {
                                esoActors.add(value);
                            }
                        }
                        Triple triple = new Triple();
                        triple.setPredicate(predicate);
                        triple.setObject(value);
                        eventTriples.add(triple);

                        /*if (actorCount.containsKey(value)) {
                            Integer cnt = actorCount.get(value);
                            cnt++;
                            actorCount.put(value, cnt);
                        }
                        else {
                            actorCount.put(value,1);
                        }*/
                    }
                }
            }
        }
        //// ESO takes priority, then FN then all other roles
        /// we only add actors with pb if the object does not have a fn relation
        /// and we add actors with fn relations only if they do not have a eso relation
        /// finally add all others
        for (int i = 0; i < eventTriples.size(); i++) {
            Triple triple = eventTriples.get(i);
            if (triple.getPredicate().startsWith("eso")) {
                if (!coveredActors.contains(triple.getObject())) {
                    jsonActorsObject.append(triple.getPredicate(), triple.getObject());
                    coveredActors.add(triple.getObject());
                }
            }
            else if (!esoActors.contains((triple.getObject())) && triple.getPredicate().startsWith("fn")) {
                if (!coveredActors.contains(triple.getObject())) {
                    jsonActorsObject.append(triple.getPredicate(), triple.getObject());
                    coveredActors.add(triple.getObject());
                }
            }
            else if (!esoActors.contains((triple.getObject())) && !fnActors.contains((triple.getObject()))) {
                if (!coveredActors.contains(triple.getObject())) {
                    jsonActorsObject.append(triple.getPredicate(), triple.getObject());
                    coveredActors.add(triple.getObject());
                }
            }
        }

        /// we first add eso roles, then fn roles finally al others
        /// currently makes no difference since Maarten sorts in his own way
 /*       for (int i = 0; i < eventTriples.size(); i++) {
            Triple triple = eventTriples.get(i);
            if (triple.getPredicate().startsWith("eso")) {
                jsonActorsObject.append(triple.getPredicate(), triple.getObject());
            }
        }
        for (int i = 0; i < eventTriples.size(); i++) {
            Triple triple = eventTriples.get(i);
            if (!esoActors.contains((triple.getObject())) && triple.getPredicate().startsWith("fn")) {
                jsonActorsObject.append(triple.getPredicate(), triple.getObject());
            }
        } for (int i = 0; i < eventTriples.size(); i++) {
            Triple triple = eventTriples.get(i);
            if (!esoActors.contains((triple.getObject())) && !fnActors.contains((triple.getObject()))) {
                jsonActorsObject.append(triple.getPredicate(), triple.getObject());
            }
        }*/

        return jsonActorsObject;
    }


    static JSONObject getUniqueActorsJSONObjectFromInstanceStatement (ArrayList<Statement> statements,
                                                                      String ACTORNAMESPACES) throws JSONException {
        JSONObject jsonActorsObject = new JSONObject();

        HashMap<String, ArrayList<Statement>> actorMap = new HashMap<String, ArrayList<Statement>>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            if (predicate.toLowerCase().endsWith("hastime")) {
                ///
            }
            else if (predicate.toLowerCase().endsWith("hasactor")) {
                ///
            }
            else {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                if (actorMap.containsKey(object)) {
                    ArrayList<Statement> actorStatements = actorMap.get(object);
                    actorStatements.add(statement);
                    actorMap.put(object, actorStatements);
                }
                else {
                    ArrayList<Statement> actorStatements = new ArrayList<Statement>();
                    actorStatements.add(statement);
                    actorMap.put(object, actorStatements);
                }
            }
        }
        Set keySet = actorMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            ArrayList<Statement> actorStatements = actorMap.get(key);
            String combinedPredicate = "";
            for (int i = 0; i < actorStatements.size(); i++) {
                Statement statement = actorStatements.get(i);
                String predicate = statement.getPredicate().getURI();
                String property = getNameSpaceString(predicate);
                if (!property.isEmpty()) {
                    if (ACTORNAMESPACES.indexOf(property)>-1 || ACTORNAMESPACES.isEmpty()) {
                        if (property.equalsIgnoreCase("pb")) {
                            predicate = property + "/" + RoleLabels.normalizeProbBankValue(getValue(predicate));
                        }
                        else {
                            predicate = property + "/" + getValue(predicate);
                        }
                        if (!combinedPredicate.isEmpty()) {
                            combinedPredicate+="+";
                        }
                        combinedPredicate += predicate;
                        /*String[] values = key.split(",");
                        ArrayList<String> coveredValues = new ArrayList<String>();
                        for (int j = 0; j < values.length; j++) {
                            String value = values[j];
                            if (!coveredValues.contains(value)) {
                                coveredValues.add(value);
                                jsonActorsObject.append(predicate, value);
                            }
                        }*/
                    }
                }
                //  break; //// only takes the first
            }
            if (!combinedPredicate.isEmpty()) {
                String[] values = key.split(",");
                ArrayList<String> coveredValues = new ArrayList<String>();
                for (int j = 0; j < values.length; j++) {
                    String value = values[j];
                    if (!coveredValues.contains(value)) {
                        coveredValues.add(value);
                        jsonActorsObject.append(combinedPredicate, value);
                    }
                }
            }
        }
        return jsonActorsObject;
    }

    static JSONObject getMentionsJSONObjectFromInstanceStatement (ArrayList<Statement> statements) throws JSONException {
        JSONObject jsonClassesObject = new JSONObject();
        String predicate = "";
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            predicate = statement.getPredicate().getURI();
            if (predicate.endsWith("#denotedBy")) {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                jsonClassesObject.append("mentions", getMentionObjectFromMentionURI(object));
            }
        }
        /*try {
            jsonClassesObject.get("mentions");
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("No mentions for predicate = "+predicate);

        }*/
        return jsonClassesObject;
    }

    static JSONObject getMentionObjectFromMentionURI (String mentionUri) {
        JSONObject mObject = new JSONObject();
        try {
          //  System.out.println("mentionUri = " + mentionUri);
            int idx = mentionUri.lastIndexOf("#");
            if (idx > -1) {
                String uri = mentionUri.substring(0, idx);
                String[] values = mentionUri.split("&");
                if (values.length == 4) {
                    String charOffset = values[0];
                    String tokens = values[1];
                    String terms = values[2];
                    String sentence = values[3];
                    mObject.put("uri", uri);
                    addValuesFromMention(mObject, "char", charOffset);
                    addValuesFromMention(mObject, "tokens", tokens);
                    addValuesFromMention(mObject, "terms", terms);
                    addValuesFromMention(mObject, "sentence", sentence);
                } else if (values.length == 1) {
                    String charOffset = values[0];
                    mObject.put("uri", uri);
                    addValuesFromMention(mObject, "char", charOffset);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mObject;
    }


    static public ArrayList<JSONObject> getJSONObjectArrayRDF(TrigTripleData trigTripleData) throws JSONException {
        ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<JSONObject>();

        Set keySet = trigTripleData.tripleMapOthers.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next(); //// this is the subject of the triple which should point to an event
            ArrayList<Statement> otherTriples = trigTripleData.tripleMapOthers.get(key);
            if (!hasActor(otherTriples)) {
                /// we ignore events without actors.....
            }
            else {

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("event", key);
                String timeAnchor = getTimeAnchor(trigTripleData.tripleMapInstances, otherTriples);
                // String timeString = semTime.getOwlTime().toString().replaceAll("-", ",");

                jsonObject.put("time", timeAnchor);
                if (trigTripleData.tripleMapInstances.containsKey( key)) {
                    ArrayList<Statement> instanceTriples = trigTripleData.tripleMapInstances.get(key);
                    for (int i = 0; i < instanceTriples.size(); i++) {
                        Statement statement = instanceTriples.get(i);
                        String predicate = statement.getPredicate().getURI();
                        //  if (predicate.)
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

    static void addValuesFromMention (JSONObject object, String key, String str) throws JSONException {
        int idx_s = str.lastIndexOf("=");
        String [] v = str.substring(idx_s+1).split(",");
        for (int i = 0; i < v.length; i++) {
            String s = v[i];
            object.append(key, s);
        }
    }

    static ArrayList<String> getValuesFromMention (String str) {
        ArrayList<String> values = new ArrayList<String>();
        int idx_s = str.indexOf("=");
        String [] v = str.substring(idx_s).split(",");
        for (int i = 0; i < v.length; i++) {
            String s = v[i];
            values.add(s);
        }
        return values;
    }

    static JSONObject getLabelsJSONObjectFromInstanceStatement (ArrayList<Statement> statements) throws JSONException {
        JSONObject jsonClassesObject = new JSONObject();
        ArrayList<String> coveredValues = new ArrayList<String>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            if (predicate.endsWith("#label")) {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String [] values = object.split(",");
                for (int j = 0; j < values.length; j++) {
                    String value = values[j];
                    if (!coveredValues.contains(value)) {
                        coveredValues.add(value);
                        jsonClassesObject.append("labels", value);
                    }
                }
            }
        }
        return jsonClassesObject;
    }

    static JSONObject getPrefLabelsJSONObjectFromInstanceStatement (ArrayList<Statement> statements) throws JSONException {
        JSONObject jsonClassesObject = new JSONObject();
        ArrayList<String> coveredValues = new ArrayList<String>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            if (predicate.endsWith("#prefLabel")) {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String [] values = object.split(",");
                for (int j = 0; j < values.length; j++) {
                    String value = values[j];
                    if (!coveredValues.contains(value)) {
                        coveredValues.add(value);
                        jsonClassesObject.append("prefLabel", value);
                    }
                }
            }
        }
        return jsonClassesObject;
    }

    static boolean prefLabelInList (ArrayList<Statement> statements, ArrayList<String> blacklist) throws JSONException {
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            //System.out.println("predicate = " + predicate);
           // http://www.w3.org/2004/02/skos/core#prefLabel
            if (predicate.toLowerCase().endsWith("#preflabel")) {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String [] values = object.split(",");
                for (int j = 0; j < values.length; j++) {
                    String value = values[j];
                    //System.out.println("value = " + value);
                    if (blacklist.contains(value)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static boolean mentionInList (ArrayList<Statement> statements, Vector<String> perspectiveVector) throws JSONException {
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            if (predicate.endsWith("#denotedBy")) {
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                if (perspectiveVector.contains(object)) {
                    return true;
                }
            }
        }
        return false;
    }

static JSONObject getTopicsJSONObjectFromInstanceStatement (ArrayList<Statement> statements) throws JSONException {

        // skos:related    "air transport" , "aeronautical industry" , "transport regulations" , "air law" , "carrier" , "air safety" .
        JSONObject jsonClassesObject = new JSONObject();
        ArrayList<String> coveredValues = new ArrayList<String>();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);

            String predicate = statement.getPredicate().getURI();
            // System.out.println("predicate = " + predicate);
            if ((predicate.toLowerCase().endsWith("skos/core#relatedmatch")) ||
                    (predicate.toLowerCase().endsWith("skos/core#related"))) {
                // System.out.println("predicate = " + predicate);
                String object = "";
                if (statement.getObject().isLiteral()) {
                    object = statement.getObject().asLiteral().toString();
                } else if (statement.getObject().isURIResource()) {
                    object = statement.getObject().asResource().getURI();
                }
                String [] values = object.split(",");
                for (int j = 0; j < values.length; j++) {
                    String value = values[j];
                    if (!coveredValues.contains(value)) {
                        coveredValues.add(value);
                        jsonClassesObject.append("topics", value);
                    }
                }
            }
        }
        return jsonClassesObject;
    }



    static void getEntityEvents (Dataset dataset, String name, String entityFilter, TrigTripleData trigTripleData) {
        ArrayList<String> events = new ArrayList<String>();
        Model namedModel = dataset.getNamedModel(name);
        StmtIterator siter = namedModel.listStatements();
        while (siter.hasNext()) {
            Statement s = siter.nextStatement();
            String subject = s.getSubject().getURI();
            if (TrigUtil.getObjectValue(s).toLowerCase().indexOf(entityFilter.toLowerCase()) >-1) {
                if (!events.contains(subject)) {
                    events.add(subject);
                }
            }
        }
        siter = namedModel.listStatements();
        while (siter.hasNext()) {
            Statement s = siter.nextStatement();
            String subject = s.getSubject().getURI();
            if (events.contains(subject)) {
                if (trigTripleData.tripleMapOthers.containsKey(subject)) {
                    ArrayList<Statement> triples = trigTripleData.tripleMapOthers.get(subject);
                    triples.add(s);
                    trigTripleData.tripleMapOthers.put(subject, triples);
                } else {

                    ArrayList<Statement> triples = new ArrayList<Statement>();
                    triples.add(s);
                    trigTripleData.tripleMapOthers.put(subject, triples);
                }
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

    static boolean hasILI (ArrayList<Statement> triples) {
        for (int i = 0; i < triples.size(); i++) {
            Statement statement = triples.get(i);
            String object = "";
            if (statement.getObject().isLiteral()) {
                object = statement.getObject().asLiteral().toString();
            } else if (statement.getObject().isURIResource()) {
                object = statement.getObject().asResource().getURI();
            }
            if (object.indexOf("ili-")>-1) {
                return true;
            }
            if (object.indexOf("ili:")>-1) {
                return true;
            }
            if (object.indexOf("globalwordnet.org")>-1) {
                return true;
            }
        }
        return false;
    }

    static boolean hasFrameNet (ArrayList<Statement> triples) {
        for (int i = 0; i < triples.size(); i++) {
            Statement statement = triples.get(i);
            String object = "";
            if (statement.getObject().isLiteral()) {
                object = statement.getObject().asLiteral().toString();
            } else if (statement.getObject().isURIResource()) {
                object = statement.getObject().asResource().getURI();
            }
            if (object.indexOf("framenet")>-1) {
                return true;
            }
        }
        return false;
    }



    //        TimeLanguage.setLanguage(kafSaxParser.getLanguage());

    static public String getSynsetsFromIli (String key, HashMap<String, ArrayList<String>> iliMap) {
        String synset = "";
        int idx = key.lastIndexOf("/");
        if (idx>-1) key = key.substring(idx+1);
        synset = key;
        ArrayList<String> synsetArray = new ArrayList<String>();
        String [] ilis = key.split("-and-");
        for (int i = 0; i < ilis.length; i++) {
            String ili = ilis[i];
            if (iliMap.containsKey(ili)) {
                ArrayList<String> syns = iliMap.get(ili);
                for (int j = 0; j < syns.size(); j++) {
                    String s = syns.get(j);
                    s = s.substring(0, s.indexOf("%"));
                    if (!synsetArray.contains(s)) {
                        synsetArray.add(s);
                    }
                }
            }
        }
        if (synsetArray.size()>0) {
            synset = "";
            for (int i = 0; i < synsetArray.size(); i++) {
                String s = synsetArray.get(i);
                if (!synset.isEmpty()) {
                    synset+= ";";
                }
                synset += s;
            }
        }
        return synset;
    }

    static public String getNameSpaceString (String value) {
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
        else if (value.indexOf("/ili/") > -1) {
            property = "ili";
        }
        else if (value.indexOf("wordnet") > -1) {
            property = "wn";
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
        else if (value.indexOf("/dbpedia") > -1) {
            property = "dbp";
        }
        else if (value.indexOf("es.dbpedia") > -1) {
            property = "es.dbp";
        }
        else if (value.indexOf("nl.dbpedia") > -1) {
            property = "nl.dbp";
        }
        else if (value.indexOf("it.dbpedia") > -1) {
            property = "it.dbp";
        }
        else if (value.indexOf("/entities/") > -1) {
            property = "en";
        }
        else if (value.indexOf("/non-entities/") > -1) {
            property = "ne";
        }
        else if (value.indexOf("/eurovoc.europa.eu/") > -1) {
            property = "eurovoc";
        }
        else {
           // System.out.println("value = " + value);
        }
        return property;
    }

    static public String getValue (String predicate) {
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

    static public boolean tooTinyValue (String predicate) {
        if (predicate.length()>3) return true;
        else if (!predicate.toLowerCase().equals(predicate)) return true;
        return false;
    }

    static public String getValueWithoutFrame (String predicate) {
        int idx = predicate.lastIndexOf("@");
        if (idx>-1) {
            return predicate.substring(idx + 1);
        }
        else {
            return predicate;
        }
    }
}
