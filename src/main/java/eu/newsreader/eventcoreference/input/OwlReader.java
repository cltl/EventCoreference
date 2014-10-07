package eu.newsreader.eventcoreference.input;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.Filter;
import eu.newsreader.eventcoreference.naf.ResourcesUri;

import java.util.Iterator;

/**
 * Created by piek on 10/5/14.
 */
public class OwlReader {


    static public String createSparql(String variable, String typeUri) {
        String q = "SELECT ?"+variable+" " +
                "WHERE{" +
                "?"+variable+" a  "+typeUri +
                "}";
        return q;
    }

    static public String createSparqlTriple(String variable, String subj, String pred, String obj) {
        String q = "SELECT ?"+variable+" \n" +
                "WHERE { " +
                "  "+subj+ " \n"+
                "  "+pred+ " \n"+
                "  "+obj+
                "}";
        return q;
    }

    static void readOwlFile (String pathToOwlFile) {
        OntModel ontologyModel =
                ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        ontologyModel.read(pathToOwlFile, "RDF/XML-ABBREV");
       // OntClass myClass = ontologyModel.getOntClass("namespace+className");

        OntClass myClass = ontologyModel.getOntClass(ResourcesUri.nwr+"domain-ontology#Motion");
        System.out.println("myClass.toString() = " + myClass.toString());
        System.out.println("myClass.getSuperClass().toString() = " + myClass.getSuperClass().toString());

        //List list =
              //  namedHierarchyRoots(ontologyModel);


       Iterator i = ontologyModel.listHierarchyRootClasses()
                .filterDrop( new Filter() {
                    public boolean accept( Object o ) {
                        return ((Resource) o).isAnon();
                    }} ); ///get all top nodes and excludes anonymous classes

       // Iterator i = ontologyModel.listHierarchyRootClasses();
        while (i.hasNext()) {
            System.out.println(i.next().toString());
/*            OntClass ontClass = ontologyModel.getOntClass(i.next().toString());
            if (ontClass.hasSubClass()) {

            }*/
        }

        String q = createSparql("event", "<http://www.newsreader-project.eu/domain-ontology#Motion>");
        System.out.println("q = " + q);
        QueryExecution qe = QueryExecutionFactory.create(q,
                ontologyModel);
        for (ResultSet rs = qe.execSelect() ; rs.hasNext() ; ) {
            QuerySolution binding = rs.nextSolution();
            System.out.println("binding = " + binding.toString());
            System.out.println("Event: " + binding.get("event"));
        }

        ontologyModel.close();
    }

    static public void main (String [] args) {
        String pathToOwlOntology = "/Users/piek/Desktop/NWR/NWR-ontology/version-0.6/ESO_version_0.6.owl";
        readOwlFile (pathToOwlOntology);

    }
}
