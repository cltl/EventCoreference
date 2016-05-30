package eu.newsreader.eventcoreference.input;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.newsreader.eventcoreference.util.Util;
import org.apache.jena.riot.RDFDataMgr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 15/06/15.
 */
public class GetEventStats {


    static final String provenanceGraph = "http://www.newsreader-project.eu/provenance";
    static final String instanceGraph = "http://www.newsreader-project.eu/instances";
    static HashMap<String, ArrayList<Statement>> eventMap = new HashMap<String, ArrayList<Statement>>();
    static String ACTORNAMESPACES = "";
    static EsoReader esoReader = new EsoReader();
    static Dataset dataset = null;
    static public void main (String[] args) {

        String trigfolderPath = "";

        String esoFile = "/Users/piek/Desktop/NWR/timeline/vua-naf2jsontimeline_2015/resources/ESO_version_0.6.owl";
        // trigfolderPath = "/Users/piek/Desktop/tweede-kamer/events/contextual";
        trigfolderPath = "/Users/piek/Desktop/tweede-kamer/events/contextual";

        String esoEvent = "";

        esoEvent = "Destroying";
        ArrayList<String> eventTypes = new ArrayList<String>();
        //esoEvent = "Escaping";
        //esoEvent = "Stealing";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--trig-folder") && args.length>(i+1)) {
                trigfolderPath = args[i+1];
            }
            else if (arg.equals("--actors") && args.length>(i+1)) {
                ACTORNAMESPACES = args[i+1];
                // System.out.println("ACTORNAMESPACES = " + ACTORNAMESPACES);
            }
            else if (arg.equals("--event-type") && args.length>(i+1)) {
                esoEvent = args[i+1];
            }
            else if (arg.equals("--eso-owl") && args.length>(i+1)) {
                esoFile = args[i+1];
                // System.out.println("ACTORNAMESPACES = " + ACTORNAMESPACES);
            }
        }
        eventTypes.add(esoEvent);
        if (!esoFile.isEmpty()) {
            System.out.println("esoFile = " + esoFile);
            esoReader.parseFile(esoFile);
            esoReader.simpleTaxonomy.getDescendants(esoEvent, eventTypes);
        }
        System.out.println("eventTypes.toString() = " + eventTypes.toString());

        File trigfolder = new File(trigfolderPath);
        dataset = TDBFactory.createDataset();
        ArrayList<File> trigFiles = Util.makeRecursiveFileList(trigfolder, ".trig");
        try {
            OutputStream fos = new FileOutputStream(trigfolder.getParentFile()+"/"+trigfolder.getName()+"."+esoEvent+".stats.csv");
            System.out.println("trigFiles.size() = " + trigFiles.size());
            for (int i = 0; i < trigFiles.size(); i++) {
                File file = trigFiles.get(i);
                if (i%500==0) {
                    System.out.println("i = " + i);
                   // if (i>500) break;
                }
               // System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
                eventMap = new HashMap<String, ArrayList<Statement>>();
                dataset = RDFDataMgr.loadDataset(file.getAbsolutePath());
                ArrayList<String> events = getAllEsoEvents(dataset, eventTypes);
                //System.out.println("events.size() = " + events.size());
                getAllEventRoleTriples(dataset, events, eventMap);
                //System.out.println("eventMap = " + eventMap.size());
                Model namedModel = dataset.getNamedModel(instanceGraph);
                StmtIterator siter = namedModel.listStatements();
                while (siter.hasNext()) {
                    Statement s = siter.nextStatement();
                    String subject = s.getSubject().getURI();
                    if (events.contains(subject)) {
                        if (TrigUtil.validLabelTriple(s)) {
                            if (eventMap.containsKey(subject)) {
                                ArrayList<Statement> triples = eventMap.get(subject);
                                if (!hasStatement(triples, s)) {
                                    triples.add(s);
                                    eventMap.put(subject, triples);
                                }
                            } else {
                                ArrayList<Statement> triples = new ArrayList<Statement>();
                                if (!hasStatement(triples, s)) {
                                    triples.add(s);
                                }
                                eventMap.put(subject, triples);
                            }
                        }
                    }
                }
                dataset.close();
                dataset = null;
              //  System.out.println("eventMap.size() = " + eventMap.size());

                String str = "";
                Set keySet = eventMap.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    ArrayList<Statement> statements = eventMap.get(key);
                    str = TrigUtil.triplesToString(statements);
                    fos.write(str.getBytes());
                }
            }

            fos.close();



        } catch (IOException e) {
            e.printStackTrace();
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

    static ArrayList<String> getAllEsoEvents (Dataset dataset, ArrayList<String> esoTypes) {
        ArrayList<String> events = new ArrayList<String>();
        Iterator<String> it = dataset.listNames();
        while (it.hasNext()) {
            String name = it.next();
            if (name.equals(instanceGraph)) {
                Model namedModel = dataset.getNamedModel(name);
                StmtIterator siter = namedModel.listStatements();
                while (siter.hasNext()) {
                    Statement s = siter.nextStatement();
                    //  System.out.println("s.toString() = " + s.toString());
                    if (s.getPredicate().toString().endsWith("#type")) {
                        for (int i = 0; i < esoTypes.size(); i++) {
                            String esoType = esoTypes.get(i);
                            if (s.getObject().toString().endsWith(esoType)) {
                                String subject = s.getSubject().getURI();
                                if (!events.contains(subject)) {
                                    events.add(subject);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        return events;
    }

    static void getAllEventRoleTriples (Dataset dataset,
                                          ArrayList<String> events,
                                        HashMap<String, ArrayList<Statement>> eventMap) {
        Iterator<String> it = dataset.listNames();
        while (it.hasNext()) {
            String name = it.next();
            if (!name.equals(instanceGraph) && (!name.equals(provenanceGraph))) {
                Model namedModel = dataset.getNamedModel(name);
                StmtIterator siter = namedModel.listStatements();
                while (siter.hasNext()) {
                    Statement s = siter.nextStatement();
                    if (TrigUtil.validRoleTriple(s)) {
                        String subject = s.getSubject().getURI();
                        if (events.contains(subject)) {
                            if (eventMap.containsKey(subject)) {
                                ArrayList<Statement> statements = eventMap.get(subject);
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



}
