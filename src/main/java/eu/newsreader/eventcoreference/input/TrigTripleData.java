package eu.newsreader.eventcoreference.input;

import com.hp.hpl.jena.rdf.model.Statement;

import java.util.ArrayList;
import java.util.HashMap;

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

    public TrigTripleData() {
        tripleMapGrasp = new HashMap<String, ArrayList<Statement>>();
        tripleMapProvenance = new HashMap<String, ArrayList<Statement>>();
        tripleMapInstances = new HashMap<String, ArrayList<Statement>>();
        tripleMapOthers = new HashMap<String, ArrayList<Statement>>();
    }
}
