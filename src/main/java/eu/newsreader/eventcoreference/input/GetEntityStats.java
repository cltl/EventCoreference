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
public class GetEntityStats {


    static HashMap<String, ArrayList<Statement>> eventMap = new HashMap<String, ArrayList<Statement>>();
    static String ACTORNAMESPACES = "";
    static Dataset dataset = null;

    static public void main (String[] args) {

        String trigfolderPath = "";
        trigfolderPath = "/Users/piek/Desktop/tweede-kamer/events/contextual";
        String entity = "";
        entity = "wouter";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--trig-folder") && args.length>(i+1)) {
                trigfolderPath = args[i+1];
            }
            else if (arg.equals("--actors") && args.length>(i+1)) {
                ACTORNAMESPACES = args[i+1];
            }
            else if (arg.equals("--entity") && args.length>(i+1)) {
                entity = args[i+1];
            }
        }

        File trigfolder = new File(trigfolderPath);
        dataset = TDBFactory.createDataset();
        ArrayList<File> trigFiles = Util.makeRecursiveFileList(trigfolder, ".trig");
        System.out.println(trigfolder.getName() + " trigFiles.size() = " + trigFiles.size());

        try {
            OutputStream fos = new FileOutputStream(trigfolder.getParentFile()+"/"+trigfolder.getName()+"."+entity+".stats.csv");

            for (int i = 0; i < trigFiles.size(); i++) {
            File file = trigFiles.get(i);
            if (i%500==0) {
                System.out.println("i = " + i);
               // if (i>1000) break;
            }
           // System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
            ArrayList<String> events = new ArrayList<String>();
            dataset = RDFDataMgr.loadDataset(file.getAbsolutePath());
            eventMap = new HashMap<String, ArrayList<Statement>>();
            Iterator<String> it = dataset.listNames();
            while (it.hasNext()) {
                String name = it.next();
                if (!name.equals(TrigUtil.provenanceGraph) && !name.equals(TrigUtil.instanceGraph)) {
                    //// sem relations
                    Model namedModel = dataset.getNamedModel(name);
                    StmtIterator siter = namedModel.listStatements();
                    while (siter.hasNext()) {
                        Statement s = siter.nextStatement();
                        if (TrigUtil.validRoleTriple(s)) {
                            String subject = s.getSubject().getURI();
                            if (TrigUtil.getObjectValue(s).toLowerCase().indexOf(entity.toLowerCase()) >-1) {
                                if (!events.contains(subject)) {
                                    events.add(subject);
                                }
                            }
                            if (eventMap.containsKey(subject)) {
                                ArrayList<Statement> triples = eventMap.get(subject);
                                if (!TrigUtil.hasStatement(triples, s)) {
                                    triples.add(s);
                                    eventMap.put(subject, triples);
                                }
                            } else {
                                ArrayList<Statement> triples = new ArrayList<Statement>();
                                triples.add(s);
                                eventMap.put(subject, triples);
                            }
                        } else {
                            //  System.out.println("s.toString() = " + s.toString());
                        }
                    }
                }
            }
            Model namedModel = dataset.getNamedModel(TrigUtil.instanceGraph);
            StmtIterator siter = namedModel.listStatements();
            while (siter.hasNext()) {
                Statement s = siter.nextStatement();
                String subject = s.getSubject().getURI();
                if (events.contains(subject)) {
                    if (TrigUtil.validLabelTriple(s)) {
                        if (eventMap.containsKey(subject)) {
                            ArrayList<Statement> triples = eventMap.get(subject);
                            if (!TrigUtil.hasStatement(triples, s)) {
                                triples.add(s);
                                eventMap.put(subject, triples);
                            }
                        } else {
                            ArrayList<Statement> triples = new ArrayList<Statement>();
                            triples.add(s);
                            eventMap.put(subject, triples);
                        }
                    }
                }
            }
            String str = "";

            Set keySet = eventMap.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                ArrayList<Statement> statements = eventMap.get(key);
                //System.out.println("statements.size() = " + statements.size());
                str = TrigUtil.triplesToString(statements, entity);
                fos.write(str.getBytes());
            }
        }
        fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
