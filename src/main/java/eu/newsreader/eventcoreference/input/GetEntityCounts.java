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
public class GetEntityCounts {


    static HashMap<String, Integer> dbpEntityCounts = new HashMap<String, Integer>();
    static HashMap<String, Integer> nwrEntityCounts = new HashMap<String, Integer>();
    static HashMap<String, Integer> nonEntityCounts = new HashMap<String, Integer>();
    static Dataset dataset = null;

    static public void main (String[] args) {

        String trigfolderPath = "";
        //trigfolderPath = "/Users/piek/Desktop/tweede-kamer/events/source";
        //trigfolderPath = "/Users/piek/Desktop/tweede-kamer/test-trig";
        trigfolderPath = "/Users/piek/Desktop/tweede-kamer/events";
        boolean DBP = false;
        boolean NWR = true;
        boolean NON = true;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--trig-folder") && args.length>(i+1)) {
                trigfolderPath = args[i+1];
            }
            else if (arg.equals("--dbp")) {
                DBP = true;
            }
            else if (arg.equals("--nwr")) {
                NWR = true;
            }
            else if (arg.equals("--non")) {
                NON = true;
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
               // if (i>500) break;
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
                        if (DBP) updateDBPCounts(s);
                        if (NWR) updateNWRCounts(s);
                        if (NON) updateNONCounts(s);
                    }
                }
            }
            dataset.close();
            dataset = null;
        }
        try {
            if (DBP) {
                OutputStream fos = new FileOutputStream(trigfolder.getParentFile() + "/" + trigfolder.getName() + ".dbpEntity-stats.csv");
                String str = "dbpEntityCounts\t" + dbpEntityCounts.size() + "\n";
                fos.write(str.getBytes());
                Set keySet = dbpEntityCounts.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Integer cnt = dbpEntityCounts.get(key);
                    str = key + "\t" + cnt + "\n";
                    fos.write(str.getBytes());
                }
                fos.close();
            }

            if (NWR) {
                OutputStream fos = new FileOutputStream(trigfolder.getParentFile() + "/" + trigfolder.getName() + ".nwrEntity-stats.csv");
                String str = "nwrEntityCounts\t" + nwrEntityCounts.size() + "\n";
                fos.write(str.getBytes());
                Set keySet = nwrEntityCounts.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Integer cnt = nwrEntityCounts.get(key);
                    int idx = key.lastIndexOf("/");
                    if (idx>-1) {
                         key = key.substring(idx);
                    }
                    str = key + "\t" + cnt + "\n";
                    fos.write(str.getBytes());
                }
                fos.close();
            }

            if (NON) {
                OutputStream fos = new FileOutputStream(trigfolder.getParentFile() + "/" + trigfolder.getName() + ".nonEntity-stats.csv");
                String str = "nonEntityCounts\t" + nonEntityCounts.size() + "\n";
                fos.write(str.getBytes());
                Set keySet = nonEntityCounts.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Integer cnt = nonEntityCounts.get(key);
                    int idx = key.lastIndexOf("/");
                    if (idx>-1) {
                        key = key.substring(idx);
                    }
                    str = key + "\t" + cnt + "\n";
                    fos.write(str.getBytes());
                }
                fos.close();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void updateDBPCounts (Statement statement) {
        String subject = statement.getSubject().toString();
        if (subject.contains("dbpedia")) {
            if (TrigUtil.isGafTriple(statement)) {
                if (dbpEntityCounts.containsKey(subject)) {
                    Integer cnt = dbpEntityCounts.get(subject);
                    cnt++;
                    dbpEntityCounts.put(subject, cnt);
                }
                else {
                    dbpEntityCounts.put(subject,1);
                }
            }
        }
    }

    static void updateNWRCounts (Statement statement) {
        String subject = statement.getSubject().toString();
        //System.out.println("subject = " + subject);
        if (subject.contains("/entities/")) {
            if (TrigUtil.isGafTriple(statement)) {
                if (nwrEntityCounts.containsKey(subject)) {
                    Integer cnt = nwrEntityCounts.get(subject);
                    cnt++;
                    nwrEntityCounts.put(subject, cnt);
                }
                else {
                    nwrEntityCounts.put(subject,1);
                }
            }
        }
    }

    static void updateNONCounts (Statement statement) {
        String subject = statement.getSubject().toString();
        if (subject.contains("/non-entities/")) {
            if (TrigUtil.isGafTriple(statement)) {
                if (nonEntityCounts.containsKey(subject)) {
                    Integer cnt = nonEntityCounts.get(subject);
                    cnt++;
                    nonEntityCounts.put(subject, cnt);
                }
                else {
                    nonEntityCounts.put(subject,1);
                }
            }
        }
    }


}
