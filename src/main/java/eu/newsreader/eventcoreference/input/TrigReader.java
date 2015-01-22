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
import java.util.*;

/**
 * Created by piek on 1/3/14.
 */
public class TrigReader {

    static final String provenanceGraph = "http://www.newsreader-project.eu/provenance";
    static final String instanceGraph = "http://www.newsreader-project.eu/instances";
    static HashMap<String, Integer> predicateMapInstances = new HashMap<String, Integer>();
    static HashMap<String, Integer> subjectMapInstances = new HashMap<String, Integer>();
    static HashMap<String, Integer> objectMapInstances = new HashMap<String, Integer>();
    static HashMap<String, Integer> predicateObjectMapInstances = new HashMap<String, Integer>();
    static HashMap<String, Integer> predicateSubjectMapInstances = new HashMap<String, Integer>();
    static HashMap<String, Integer> predicateMapOthers = new HashMap<String, Integer>();
    static HashMap<String, Integer> subjectMapOthers = new HashMap<String, Integer>();
    static HashMap<String, Integer> objectMapOthers = new HashMap<String, Integer>();
    static HashMap<String, Integer> predicateObjectMapOthers = new HashMap<String, Integer>();
    static HashMap<String, Integer> predicateSubjectMapOthers = new HashMap<String, Integer>();

    static void updateMap (HashMap<String, Integer> map, String s) {
        if (map.containsKey(s)) {
            Integer cnt = map.get(s);
            cnt++;
            map.put(s, cnt);
        }
        else {
            map.put(s, 1);
        }

    }
    static void updateOtherStats (Statement s) {
        String predicate = s.getPredicate().getURI();
        String subject = s.getSubject().getURI();
        String object = "";
        if (s.getObject().isLiteral()) {
            object = s.getObject().asLiteral().toString();
        }
        else if (s.getObject().isURIResource()) {
            object = s.getObject().asResource().getURI();
        }
        String predicateSubject = subject +"\t"+predicate;
        String predicateObject = predicate+"\t"+object;
        updateMap(predicateMapOthers, predicate);
        updateMap(subjectMapOthers, subject);
        updateMap(objectMapOthers, object);
        updateMap(predicateSubjectMapOthers, predicateSubject);
        updateMap(predicateObjectMapOthers, predicateObject);
    }

    static void updateInstanceStats (Statement s) {
        String predicate = s.getPredicate().getURI();
        String subject = s.getSubject().getURI();
        String object = "";
        if (s.getObject().isLiteral()) {
            object = s.getObject().asLiteral().toString();
        }
        else if (s.getObject().isURIResource()) {
            object = s.getObject().asResource().getURI();
        }
        String predicateSubject = subject +"\t"+predicate;
        String predicateObject = predicate+"\t"+object;
        updateMap(predicateMapInstances, predicate);
        updateMap(subjectMapInstances, subject);
        updateMap(objectMapInstances, object);
        updateMap(predicateSubjectMapInstances, predicateSubject);
        updateMap(predicateObjectMapInstances, predicateObject);
    }

    static public void main (String[] args) {
        try {
            String format = "NT";
          //  String format = "Turle";
          //  String format = "N3";

            String trigfolder = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_airbus/events/contextual";
            String outputFileInstances = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_airbus_contextualInstances.trp";
            String outputFileProvenance = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_airbus_contextualProvenance.trp";
            String outputFileOthers = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_airbus_contextualOthers.trp";
            String statsFile = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_airbus.stats";



/*
            String trigfolder = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_apple/events/contextual";
            String outputFileInstances = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_apple_contextualInstances.trp";
            String outputFileProvenance = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_apple_contextualProvenance.trp";
            String outputFileOthers = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_apple_contextualOthers.trp";
            String statsFile = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_apple.stats";
*/



/*
            String trigfolder = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_gm_chrysler_ford/events/contextual";
            String outputFileInstances = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_gm_chrysler_ford_contextualInstances.trp";
            String outputFileProvenance = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_gm_chrysler_ford_contextualProvenance.trp";
            String outputFileOthers = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_gm_chrysler_ford_contextualOthers.trp";
            String statsFile = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_gm_chrysler_ford.stats";
*/


/*
            String trigfolder = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_stock_market/events/contextual";
            String outputFileInstances = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_stock_market_contextualInstances.trp";
            String outputFileProvenance = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_stock_market_contextualProvenance.trp";
            String outputFileOthers = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_stock_market_contextualOthers.trp";
            String statsFile = "/Users/piek/Desktop/NWR/Cross-lingual/corpus_NAF_output_141214-lemma/corpus_stock_market.stats";
*/


            Dataset dataset = TDBFactory.createDataset();
            ArrayList<File> trigFiles = Util.makeRecursiveFileList(new File(trigfolder), ".trig");
            ArrayList<String> provenanceTriples = new ArrayList<String>();
            ArrayList<String> instanceTriples = new ArrayList<String>();
            ArrayList<String> otherTriples = new ArrayList<String>();
            for (int i = 0; i < trigFiles.size(); i++) {
                File file = trigFiles.get(i);
               // System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
                dataset = RDFDataMgr.loadDataset(file.getAbsolutePath());
                Iterator<String> it = dataset.listNames();
                while (it.hasNext()) {
                    String name = it.next();
                   // System.out.println("name = " + name);
                    if (name.equals(provenanceGraph)) {
                        Model namedModel = dataset.getNamedModel(name);
                        StmtIterator siter = namedModel.listStatements();
                        while (siter.hasNext()) {
                            Statement s = siter.nextStatement();
                            String str = getStatementString(s);
                            if (!provenanceTriples.contains(str)) {
                                provenanceTriples.add(str);
                            }
                        }
                    }
                    else if (name.equals(instanceGraph)) {
                        Model namedModel = dataset.getNamedModel(name);
                        StmtIterator siter = namedModel.listStatements();
                        while (siter.hasNext()) {
                            Statement s = siter.nextStatement();
                            updateInstanceStats(s);
                            String str = getStatementString(s);
                            if (!instanceTriples.contains(str)) {
                                instanceTriples.add(str);
                            }
                        }
                    }
                    else {
                        Model namedModel = dataset.getNamedModel(name);
                        StmtIterator siter = namedModel.listStatements();
                        while (siter.hasNext()) {
                            Statement s = siter.nextStatement();
                            updateOtherStats(s);
                            String str = getStatementString(s);
                            if (!otherTriples.contains(str)) {
                                otherTriples.add(str);
                            }
                        }
                    }
                }
                dataset.close();
            }

            writeSortedStringArrayList(outputFileProvenance, provenanceTriples);
            writeSortedStringArrayList(outputFileInstances, instanceTriples);
            writeSortedStringArrayList(outputFileOthers, otherTriples);
            String str = "";

            OutputStream fosStats = new FileOutputStream(statsFile+".instances.xls");
            str += trigfolder+"\n\n";
            str += "INSTANCES\n\n";
            fosStats.write(str.getBytes());
            str = "PREDICATES\n";
            fosStats.write(str.getBytes());
            writesStats(fosStats, predicateMapInstances);
            str = "\nSUBJECTS\n";
            fosStats.write(str.getBytes());
            writesStats(fosStats, subjectMapInstances);
            str = "\nOBJECTS\n";
            fosStats.write(str.getBytes());
            writesStats(fosStats, objectMapInstances);
            str = "\nSUBJECTS & PREDICATES\n";
            fosStats.write(str.getBytes());
            writesStats(fosStats, predicateSubjectMapInstances);
            str = "\nPREDICATE & OBJECTS\n";
            fosStats.write(str.getBytes());
            writesStats(fosStats, predicateObjectMapInstances);
            str = "\n";
            fosStats.write(str.getBytes());
            fosStats.close();

            fosStats = new FileOutputStream(statsFile+".relations.xls");
            str = trigfolder+"\n\n";
            str += "\nRELATIONS\n\n";
            fosStats.write(str.getBytes());
            str = "PREDICATES\n";
            fosStats.write(str.getBytes());
            writesStats(fosStats, predicateMapOthers);
            str = "\nSUBJECTS\n";
            fosStats.write(str.getBytes());
            writesStats(fosStats, subjectMapOthers);
            str = "\nOBJECTS\n";
            fosStats.write(str.getBytes());
            writesStats(fosStats, objectMapOthers);
            str = "\nSUBJECTS & PREDICATES\n";
            fosStats.write(str.getBytes());
            writesStats(fosStats, predicateSubjectMapOthers);
            str = "\nPREDICATE & OBJECTS\n";
            fosStats.write(str.getBytes());
            writesStats(fosStats, predicateObjectMapOthers);
            str = "\n";
            fosStats.write(str.getBytes());
            fosStats.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void writeSortedStringArrayList(String fileName, ArrayList<String> result) throws IOException {
        OutputStream fos = new FileOutputStream(fileName);
        TreeSet<String> tree = new TreeSet<String>();
        for (int i = 0; i < result.size(); i++) {
            String s = result.get(i)+"\n";
            tree.add(s);
        }
        Iterator<String> t = tree.iterator();
        while(t.hasNext()) {
            String s = t.next();
            fos.write(s.getBytes());
        }
        fos.close();
    }

    static void writesStats (OutputStream fos, HashMap<String, Integer> map) throws IOException {
        TreeSet<String> tree = new TreeSet<String>();
        Set keySet = map.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            tree.add(key);
        }
        Iterator<String> t = tree.iterator();
        while(t.hasNext()) {
            String s = t.next();
            Integer cnt = map.get(s);
            s += "\t" + cnt.toString() + "\n";
            fos.write(s.getBytes());
        }
    }

    static String getStatementString (Statement s) {
        String str = s.getPredicate().getURI()+"\t"+s.getSubject().getURI()+"\t";
        if (s.getObject().isLiteral()) {
            str += s.getObject().asLiteral();
        }
        else if (s.getObject().isURIResource()) {
            str += s.getObject().asResource().getURI();
        }
        return str;
    }

}
