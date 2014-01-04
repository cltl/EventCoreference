package eu.newsreader.eventcoreference.input;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by piek on 1/3/14.
 */
public class TrigReader {

    static void readTrigFile (String pathToTrigFile) {
        try {
            FileInputStream fis = new FileInputStream(pathToTrigFile);
            Dataset ds = TDBFactory.createDataset();
            Model model = ds.getDefaultModel();
            //defaultModel.read(fis, RDFFormat.TRIG_PRETTY);
            model.read(fis, "TRIG");
            ResIterator iter = model.listSubjectsWithProperty(RDF.type);
            while (iter.hasNext()) {
                Resource r = iter.nextResource();
                System.out.println("r.getURI() = " + r.getURI());
            //    System.out.println("r.getProperty() = " + r.getProperty());

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    static public void main (String[] args) {
      //  String pathToTrigFile = args[0];
        String pathToTrigFile = "/Users/piek/Desktop/NWR-DATA/2004-04-26/sem.trig";
        readTrigFile(pathToTrigFile);
    }
}
