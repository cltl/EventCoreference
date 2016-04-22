package eu.newsreader.eventcoreference.input;

import com.hp.hpl.jena.rdf.model.Statement;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by piek on 05/02/16.
 */
public class TrigTripleData {


    static final String provenanceGraph = "http://www.newsreader-project.eu/provenance";
    static final String instanceGraph = "http://www.newsreader-project.eu/instances";
    static final String graspGraph = "http://www.newsreader-project.eu/grasp";
    public HashMap<String, ArrayList<Statement>> tripleMapGrasp = new HashMap<String, ArrayList<Statement>>();
    public HashMap<String, ArrayList<Statement>> tripleMapProvenance = new HashMap<String, ArrayList<Statement>>();
    public HashMap<String, ArrayList<Statement>> tripleMapInstances = new HashMap<String, ArrayList<Statement>>();
    public HashMap<String, ArrayList<Statement>> tripleMapOthers = new HashMap<String, ArrayList<Statement>>();
    public Vector<String> perspectiveMentions = new Vector<String>();

    public TrigTripleData() {
        tripleMapGrasp = new HashMap<String, ArrayList<Statement>>();
        tripleMapProvenance = new HashMap<String, ArrayList<Statement>>();
        tripleMapInstances = new HashMap<String, ArrayList<Statement>>();
        tripleMapOthers = new HashMap<String, ArrayList<Statement>>();
        perspectiveMentions = new Vector<String>();
    }

    public void dumpTriples (OutputStream fos, HashMap<String, ArrayList<Statement>> map) throws IOException {
        Set keyset = map.keySet();
        Iterator<String> keys = keyset.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            ArrayList<Statement> statements = map.get(key);
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                String str = statement.toString()+"\n";
                fos.write(str.getBytes());
            }
        }
    }
}
