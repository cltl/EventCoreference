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
public class GetEsoCounts {


    static HashMap<String, Integer> esoCounts = new HashMap<String, Integer>();
    static HashMap<String, Integer> fnCounts = new HashMap<String, Integer>();
    static String ACTORNAMESPACES = "";
    static EsoReader esoReader = new EsoReader();
    static HashMap<String, Integer> eventCountsPerDate = new HashMap<String, Integer>();
    static HashMap<String, Integer> eventMentionCountsPerDate = new HashMap<String, Integer>();
    static Dataset dataset = null;

    static public void main (String[] args) {

        String trigfolderPath = "";
        String esoFile = "/Users/piek/Desktop/NWR/timeline/vua-naf2jsontimeline_2015/resources/ESO_version_0.6.owl";
        //trigfolderPath = "/Users/piek/Desktop/tweede-kamer/events/source";
        //trigfolderPath = "/Users/piek/Desktop/tweede-kamer/test-trig";
        trigfolderPath = "/Users/piek/Desktop/tweede-kamer/events";

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

        if (!esoFile.isEmpty()) {
            esoReader.parseFile(esoFile);
        }
        File trigfolder = new File(trigfolderPath);
        dataset = TDBFactory.createDataset();
        ArrayList<File> trigFiles = Util.makeRecursiveFileList(trigfolder, ".trig");
        System.out.println("trigFiles.size() = " + trigFiles.size());
        for (int i = 0; i < trigFiles.size(); i++) {
            File file = trigFiles.get(i);
            if (i%500==0) {
                System.out.println("i = " + i);
                //if (i>500) break;
            }
            String timeDescription = file.getParentFile().getName();
            int idx = timeDescription.indexOf("-");
            if (idx>-1) {
                timeDescription = timeDescription.substring(idx+1);
                if (timeDescription.length()>=4) {
                    timeDescription = timeDescription.substring(0,4);
                }
            }
            dataset = RDFDataMgr.loadDataset(file.getAbsolutePath());
            Iterator<String> it = dataset.listNames();
            while (it.hasNext()) {
                String name = it.next();
                if (name.equals(TrigUtil.instanceGraph)) {
                    Model namedModel = dataset.getNamedModel(name);
                    StmtIterator siter = namedModel.listStatements();
                    while (siter.hasNext()) {
                        Statement s = siter.nextStatement();
                        if (TrigUtil.isEventInstance(s)) {
                            if (eventCountsPerDate.containsKey(timeDescription)) {
                               Integer cnt = eventCountsPerDate.get(timeDescription);
                               cnt++;
                               eventCountsPerDate.put(timeDescription, cnt);
                            }
                            else {
                               eventCountsPerDate.put(timeDescription, 1);
                            }
                        }
                        updateEsoCounts(s);
                        updateFramenNetCounts(s);
                    }
                }
            }
            dataset.close();
            dataset = null;
        }
        try {
            OutputStream fos = new FileOutputStream(trigfolder.getParentFile()+"/"+trigfolder.getName()+".eso-stats.csv");

            String str = "esoCounts\t" + esoCounts.size()+"\n";
            fos.write(str.getBytes());
            Set keySet = esoCounts.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                Integer cnt = esoCounts.get(key);
                str = key+"\t"+cnt+"\n";
                fos.write(str.getBytes());
            }
            fos.close();

            fos = new FileOutputStream(trigfolder.getParentFile()+"/"+trigfolder.getName()+".eso-hierarchy.txt");
            ArrayList<String> topNodes = esoReader.getTops();
            printTree(esoReader, topNodes, 0, fos);
            fos.close();

            fos = new FileOutputStream(trigfolder.getParentFile()+"/"+trigfolder.getName()+".fn-stats.csv");
            str = "fnCounts\t" + fnCounts.size()+"\n";
            fos.write(str.getBytes());
            keySet = fnCounts.keySet();
            keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                Integer cnt = fnCounts.get(key);
                str = key+"\t"+cnt+"\n";
                fos.write(str.getBytes());
            }
            fos.close();

            fos = new FileOutputStream(trigfolder.getParentFile()+"/"+trigfolder.getName()+".event-date.csv");
            keySet = eventCountsPerDate.keySet();
            keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                Integer cnt = eventCountsPerDate.get(key);
                str = key+"\t"+cnt+"\n";
                fos.write(str.getBytes());
            }
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void updateEsoCounts (Statement statement) {
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
                String property = TrigUtil.getNameSpaceString(value);
                if (property.equals("eso")) {
                    value = TrigUtil.getValue(value);
                    if (esoCounts.containsKey(value)) {
                        Integer cnts = esoCounts.get(value);
                        cnts++;
                        esoCounts.put(value, cnts);

                    } else {
                        esoCounts.put(value, 1);
                    }
                }
            }
        }
    }

    static void updateFramenNetCounts (Statement statement) {
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
                String property = TrigUtil.getNameSpaceString(value);
                if (!property.isEmpty() && !property.equalsIgnoreCase("sem")) {
                    value = TrigUtil.getValue(value);
                    if (property.equals("fn")) {
                        if (fnCounts.containsKey(value)) {
                            Integer cnts = fnCounts.get(value);
                            cnts++;
                            fnCounts.put(value, cnts);

                        } else {
                            fnCounts.put(value, 1);
                        }
                    }
                }
            }
        }
    }


    public static void printTree(EsoReader esoReader, ArrayList<String> tops, int level, OutputStream fos) throws IOException {
        level++;
        for (int i = 0; i < tops.size(); i++) {
            String top = tops.get(i);
            int count = 0;
            if (esoCounts.containsKey(top)) {
                count = esoCounts.get(top);
            }
            String str = "";
            for (int j = 0; j < level; j++) {
                str += "  ";

            }
            if (esoReader.superToSub.containsKey(top)) {
                ArrayList<String> children = esoReader.superToSub.get(top);


                str += top + ":" + count+"\n";
                fos.write(str.getBytes());
               // System.out.println(str);
                printTree(esoReader, children, level, fos);
            }
            else {
                str += top + ":" + count+"\n";
                fos.write(str.getBytes());
            }
        }
    }

}
