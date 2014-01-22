package eu.newsreader.eventcoreference.input;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

import java.io.InputStream;

/**
 * Created by piek on 1/3/14.
 */
public class RdfReader {


    static void readRdfFile (String pathToRdfFile) {
        // create an empty model
        Model model = ModelFactory.createDefaultModel();

        // use the FileManager to find the input file
        InputStream in = FileManager.get().open( pathToRdfFile );
        if (in == null) {
            throw new IllegalArgumentException(
                    "File: " + pathToRdfFile + " not found");
        }

// read the RDF/XML file
        model.read(in, null);

// write it to standard out
        model.write(System.out);
    }


    static public void main (String[] args) {
      //  String pathToRdfFile = args[0];
        String pathToRdfFile = "/Users/piek/Desktop/example.rdf";
        readRdfFile(pathToRdfFile);
    }
}
