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
       // trigfolderPath = "/Users/piek/Desktop/tweede-kamer/events/contextual";
        trigfolderPath = "/Users/piek/Desktop/KIEM/events/contextual";
        String entity = "";
        entity = "Joseph_Stalin";
        entity = "E._H._Carr";
        entity = "Geraint_Jones";
        entity = "Silvio_Berlusconi";
        entity = "Traynor";
        entity = "Geurin";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--trig-folder") && args.length>(i+1)) {
                trigfolderPath = args[i+1];
            }
            else if (arg.equals("--actors") && args.length>(i+1)) {
                ACTORNAMESPACES = args[i+1];
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
                //break;
            }
           // System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
            dataset = RDFDataMgr.loadDataset(file.getAbsolutePath());
            ArrayList<String> events = TrigUtil.getAllEntityEvents(dataset, entity);
            //System.out.println("events.size() = " + events.size());
            TrigUtil.getAllEntityEventTriples(dataset, events, eventMap);
            //System.out.println("eventMap = " + eventMap.size());
            Model namedModel = dataset.getNamedModel(TrigUtil.instanceGraph);
            StmtIterator siter = namedModel.listStatements();
            while (siter.hasNext()) {
                Statement s = siter.nextStatement();
                if (TrigUtil.validTriple(s)) {
                    String subject = s.getSubject().getURI();
                    if (eventMap.containsKey(subject)) {
                        ArrayList<Statement> triples = eventMap.get(subject);
                        if (!TrigUtil.hasStatement(triples, s)) {
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
            OutputStream fos = new FileOutputStream(trigfolder.getParentFile()+"/"+trigfolder.getName()+"."+entity+".stats.csv");

            String str = "";
            Set keySet = eventMap.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                ArrayList<Statement> statements = eventMap.get(key);
                str = TrigUtil.triplesToString(statements, entity);
                fos.write(str.getBytes());
            }
            fos.close();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
