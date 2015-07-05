package eu.newsreader.eventcoreference.input;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.newsreader.eventcoreference.util.Util;
import org.apache.jena.riot.RDFDataMgr;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by piek on 15/06/15.
 */
public class SelectEntityTrig {


    static Dataset dataset = null;

    static public void main (String[] args) {

        String trigfolderPath = "";
        trigfolderPath = "/Users/piek/Desktop/tweede-kamer/events";
        String entity = "";
        String date = "";
        entity = "lippens";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--trig-folder") && args.length>(i+1)) {
                trigfolderPath = args[i+1];
            }
            else if (arg.equals("--entity") && args.length>(i+1)) {
                entity = args[i+1];
            }
            else if (arg.equals("--event-date") && args.length>(i+1)) {
                date = args[i+1];
            }
        }
        if (entity.isEmpty()) {
            System.out.println("Entity is empty");
            return;
        }
        File trigfolder = new File(trigfolderPath);
        String trigEntityPath = trigfolder.getParent()+"/"+entity;
        File entityTrigFolder = new File (trigEntityPath);
        if (!entityTrigFolder.exists()) {
            entityTrigFolder.mkdir();
        }
        if (!entityTrigFolder.exists()) {
            System.out.println("Could not create entity trig folder");
            return;
        }
        dataset = TDBFactory.createDataset();
        ArrayList<File> trigFiles = Util.makeRecursiveFileList(trigfolder, ".trig");
        System.out.println(trigfolder.getName() + " trigFiles.size() = " + trigFiles.size());
        int cnt = 1;
        for (int i = 0; i < trigFiles.size(); i++) {
            File file = trigFiles.get(i);
            if (!file.getParentFile().getName().startsWith(date)) {
                continue;
            }
            if (i%500==0) {
                System.out.println("i = " + i);
               // if (i>1000) break;
            }
            ArrayList<String> events = new ArrayList<String>();
            dataset = RDFDataMgr.loadDataset(file.getAbsolutePath());
            Model namedModel = dataset.getNamedModel(TrigUtil.instanceGraph);
            StmtIterator siter = namedModel.listStatements();
            while (siter.hasNext()) {
                Statement s = siter.nextStatement();
                String subject = s.getSubject().getURI().toLowerCase();
                if (subject.indexOf(entity.toLowerCase())>-1) {
                    String trigName = trigEntityPath+"/"+cnt+"_"+file.getName();
                    File trigCopy = new File(trigName);
                    copyFile(file, trigCopy);
                    cnt++;
                    break;
                }
            }
            dataset = null;
        }
    }

    static public void copyFile(File inputFile, File outputFile) {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(inputFile));
            byte[] buffer = new byte[(int) inputFile.length()];
            in.readFully(buffer);
            in.close();
            DataOutputStream out = new DataOutputStream(new FileOutputStream(outputFile));
            out.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
