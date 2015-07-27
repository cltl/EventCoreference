package eu.newsreader.eventcoreference.naf;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Created by piek on 12/26/13.
 */
public class ResourcesUri {

    final static public String nwr = "http://www.newsreader-project.eu/";
    final static public String nwrtime = "http://www.newsreader-project.eu/time/";
    final static public String nwrdata = "http://www.newsreader-project.eu/data/";
    final static public String nwrauthor = "http://www.newsreader-project.eu/provenance/author/";
    final static public String nwrsourceowner = "http://www.newsreader-project.eu/provenance/sourceowner/";
    final static public String nwrvalue = "http://www.newsreader-project.eu/ontologies/value#";
    final static public String nwrontology = "http://www.newsreader-project.eu/ontologies/";
    //final static public String eso = "http://www.newsreader-project.eu/ontologies/eso/";
    final static public String eso = "http://www.newsreader-project.eu/domain-ontology#";
    final static public String wn = "http://www.newsreader-project.eu/ontologies/wordnet3.0/";
    final static public String cornetto = "http://www.newsreader-project.eu/ontologies/cornetto2.1/";
    final static public String fn = "http://www.newsreader-project.eu/ontologies/framenet/";
    final static public String vn = "http://www.newsreader-project.eu/ontologies/verbnet/";
    final static public String pb = "http://www.newsreader-project.eu/ontologies/propbank/";
    final static public String nb = "http://www.newsreader-project.eu/ontologies/nombank/";
    final static public String gaf = "http://groundedannotationframework.org/gaf#";
    final static public String sem = "http://semanticweb.cs.vu.nl/2009/11/sem/";
    final static public String dbp = "http://dbpedia.org/resource/";
    final static public String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    final static public String rdfs= "http://www.w3.org/2000/01/rdf-schema#";
    final static public String tl = "http://purl.org/NET/c4dm/timeline.owl#";
    final static public String owl= "http://www.w3.org/2002/07/owl#";
    final static public String prov= "http://www.w3.org/ns/prov#";
    final static public String owltime= "http://www.w3.org/TR/owl-time#";
    //final static public String owltime= "http://www.w3.org/2002/07/owl#";


    static public void prefixModel (Model model) {
        model.setNsPrefix("wn", ResourcesUri.wn);
      //  model.setNsPrefix("cornetto", ResourcesUri.cornetto);
        model.setNsPrefix("fn", ResourcesUri.fn);
        model.setNsPrefix("nwrdata", ResourcesUri.nwrdata);
        model.setNsPrefix("nwrontology", ResourcesUri.nwrontology);
        model.setNsPrefix("eso", ResourcesUri.eso);
/*      //REMOVED DUE TO ILLEGAL CHARACTERS
        model.setNsPrefix("wn", ResourcesUri.wn);
        model.setNsPrefix("vn", ResourcesUri.vn);
        model.setNsPrefix("pb", ResourcesUri.pb);
        model.setNsPrefix("nb", ResourcesUri.nb);
        model.setNsPrefix("dbp", ResourcesUri.dbp);

*/
        model.setNsPrefix("sem", ResourcesUri.sem);
        model.setNsPrefix("owl", ResourcesUri.owl);
        model.setNsPrefix("time", ResourcesUri.owltime);
        model.setNsPrefix("rdf", ResourcesUri.rdf);
        model.setNsPrefix("rdfs", ResourcesUri.rdfs);
    }

    static public void prefixModelNwr (Model model) {
        model.setNsPrefix("nwr", ResourcesUri.nwr);
        model.setNsPrefix("nwrauthor", ResourcesUri.nwrauthor);
        model.setNsPrefix("nwrsourceowner", ResourcesUri.nwrsourceowner);
    }

    static public void prefixModelGaf (Model model) {
        model.setNsPrefix("gaf", ResourcesUri.gaf);
        model.setNsPrefix("prov", ResourcesUri.prov);
    }

}
