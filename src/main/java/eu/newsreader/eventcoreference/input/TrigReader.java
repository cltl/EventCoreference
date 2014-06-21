package eu.newsreader.eventcoreference.input;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.newsreader.eventcoreference.util.Util;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by piek on 1/3/14.
 */
public class TrigReader {


    static void readTrigFile (ArrayList<File> files) {
        Dataset dataset = TDBFactory.createDataset();
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);


        }
    }

    static Dataset readTrigFile (String pathToTrigFile) {
        Dataset dataset = RDFDataMgr.loadDataset(pathToTrigFile) ;
        return dataset;
    }

    static public void main (String[] args) {
      //  String pathToTrigFile = args[0];
        //String pathToTrigFile = "/Users/piek/Desktop/example.trig";
        String trigfolder = "/Users/piek/Desktop/NWR-DATA/worldcup/events/other/";
        String datafolder = "/Users/piek/Desktop/NWR-DATA/worldcup/events/dataset/";
      //  String trigfolder = "/Users/piek/Desktop/NWR-DATA/trig/entitycoref/";
      //  String datafolder = "/Users/piek/Desktop/NWR-DATA/trig/entitycoref/";
        Dataset dataset = TDBFactory.createDataset(datafolder);
        Model m = dataset.getDefaultModel();
        ArrayList<File> trigFiles = Util.makeRecursiveFileList(new File(trigfolder), ".trig");
        for (int i = 0; i < trigFiles.size(); i++) {
            File file = trigFiles.get(i);
            m = ModelFactory.createDefaultModel() ;
            RDFDataMgr.read(m, "D.trig") ;
            RDFDataMgr.write(System.out, m, Lang.TTL) ;
           // System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
            Iterator<String> it = dataset.listNames();
            while (it.hasNext()) {
                String name = it.next();
                System.out.println("name = " + name);
                Context context = dataset.getContext();
                String str = context.getAsString(Symbol.create("sem:hasActor"));
                System.out.println("str = " + str);
                //System.out.println("context.toString() = " + context.toString());
            }
            dataset.close();
            break;
        }
       // String pathToTrigFile = "/Users/piek/Desktop/NWR-DATA/trig/entitycoref/2012-11-10/sem.trig";
    }


    /*
    static public void main (String[] args) {
      //  String pathToTrigFile = args[0];
        //String pathToTrigFile = "/Users/piek/Desktop/example.trig";
        String folder = "/Users/piek/Desktop/NWR-DATA/trig/entitycoref/";
        ArrayList<File> trigFiles = Util.makeRecursiveFileList(new File(folder), ".trig");
        for (int i = 0; i < trigFiles.size(); i++) {
            File file = trigFiles.get(i);
            System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
            Dataset dataset = readTrigFile(file.getAbsolutePath());
            Iterator<String> it = dataset.listNames();
            while (it.hasNext()) {
                String name = it.next();
                System.out.println("name = " + name);
                Context context = dataset.getContext();
                String str = context.getAsString(Symbol.create("sem:hasActor"));
                System.out.println("str = " + str);
                //System.out.println("context.toString() = " + context.toString());
            }
            dataset.close();
            break;
        }
       // String pathToTrigFile = "/Users/piek/Desktop/NWR-DATA/trig/entitycoref/2012-11-10/sem.trig";
    }*/
}
