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

        if (!esoFile.isEmpty()) {
            esoReader.parseFile(esoFile);
        }

        String esoEvent = "";

        esoEvent = "Destroying";
        esoEvent = "Escaping";
        esoEvent = "Stealing";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--trig-folder") && args.length>(i+1)) {
                trigfolderPath = args[i+1];
            }
            else if (arg.equals("--actors") && args.length>(i+1)) {
                ACTORNAMESPACES = args[i+1];
                // System.out.println("ACTORNAMESPACES = " + ACTORNAMESPACES);
            }
            else if (arg.equals("--eso") && args.length>(i+1)) {
                esoFile = args[i+1];
                // System.out.println("ACTORNAMESPACES = " + ACTORNAMESPACES);
            }
        }

        File trigfolder = new File(trigfolderPath);
        dataset = TDBFactory.createDataset();
        ArrayList<File> trigFiles = Util.makeRecursiveFileList(trigfolder, ".trig");
        System.out.println("trigFiles.size() = " + trigFiles.size());
        for (int i = 0; i < trigFiles.size(); i++) {
            File file = trigFiles.get(i);
            if (i%500==0) {
                System.out.println("i = " + i);
               //if (i>0) break;
            }
           // System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
            dataset = RDFDataMgr.loadDataset(file.getAbsolutePath());
            ArrayList<String> events = getAllEsoEvents(dataset, esoEvent);
            //System.out.println("events.size() = " + events.size());
            getAllEventTriples(dataset, events);
            //System.out.println("eventMap = " + eventMap.size());
            Model namedModel = dataset.getNamedModel(instanceGraph);
            StmtIterator siter = namedModel.listStatements();
            while (siter.hasNext()) {
                Statement s = siter.nextStatement();
                if (TrigUtil.validTriple(s)) {
                    String subject = s.getSubject().getURI();
                    if (eventMap.containsKey(subject)) {
                        ArrayList<Statement> triples = eventMap.get(subject);
                        if (!hasStatement(triples, s)) {
                            triples.add(s);
                        }
                    }
                }
            }
            dataset.close();
            dataset = null;
        }
        System.out.println("eventMap.size() = " + eventMap.size());
        try {
            OutputStream fos = new FileOutputStream(trigfolder.getParentFile()+"/"+trigfolder.getName()+"."+esoEvent+".stats.csv");

            String str = "";
            Set keySet = eventMap.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                ArrayList<Statement> statements = eventMap.get(key);
                str = TrigUtil.triplesToString(statements);
                fos.write(str.getBytes());
/*
                for (int i = 0; i < statements.size(); i++) {
                    Statement statement = statements.get(i);
                    str = tripleToString(statement)+"\n";
                    System.out.println(str);
                    fos.write(str.getBytes());
                }
*/
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

    static ArrayList<String> getAllEsoEvents (Dataset dataset, String type) {
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
                    if (s.getPredicate().toString().endsWith("#type") &&
                            s.getObject().toString().endsWith(type)) {
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

    static void getAllEventTriples (Dataset dataset,
                                          ArrayList<String> events) {
        HashMap<String, ArrayList<Statement>> triples = new HashMap<String, ArrayList<Statement>>();
        Iterator<String> it = dataset.listNames();
        while (it.hasNext()) {
            String name = it.next();
            if (!name.equals(instanceGraph) && (!name.equals(provenanceGraph))) {
                Model namedModel = dataset.getNamedModel(name);
                StmtIterator siter = namedModel.listStatements();
                while (siter.hasNext()) {
                    Statement s = siter.nextStatement();
                    if (TrigUtil.validTriple(s)) {
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



}
