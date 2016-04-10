package eu.newsreader.eventcoreference.naf;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Created by piek on 12/26/13.
 */
public class ResourcesUri {

    final static public String eurovoc = "http://eurovoc.europa.eu/";
    final static public String skos = "http://www.w3.org/2004/02/skos/core#";
    final static public String nwr = "http://www.newsreader-project.eu/";
    final static public String nwrtime = "http://www.newsreader-project.eu/time/";
    final static public String nwrdata = "http://www.newsreader-project.eu/data/";
    final static public String nwrauthor = "http://www.newsreader-project.eu/provenance/author/";
    final static public String nwrpublisher = "http://www.newsreader-project.eu/provenance/publisher/";
    final static public String nwrmagazine = "http://www.newsreader-project.eu/provenance/magazine/";
    final static public String nwrsourceowner = "http://www.newsreader-project.eu/provenance/sourceowner/";
    final static public String nwrvalue = "http://www.newsreader-project.eu/ontologies/value#";
    final static public String nwrontology = "http://www.newsreader-project.eu/ontologies/";
    //final static public String eso = "http://www.newsreader-project.eu/ontologies/eso#";
    final static public String eso = "http://www.newsreader-project.eu/domain-ontology#";
    final static public String wn = "http://www.newsreader-project.eu/ontologies/pwn3.0/";
    final static public String ili = "http://globalwordnet.org/ili/";
    final static public String cornetto = "http://www.newsreader-project.eu/ontologies/cornetto2.1/";
    final static public String fn = "http://www.newsreader-project.eu/ontologies/framenet/";
    final static public String vn = "http://www.newsreader-project.eu/ontologies/verbnet/";
    final static public String pb = "http://www.newsreader-project.eu/ontologies/propbank/";
    final static public String nb = "http://www.newsreader-project.eu/ontologies/nombank/";
    final static public String gaf = "http://groundedannotationframework.org/gaf#";
    final static public String gafAttribution = "http://groundedannotationframework.org/gaf/attribution#";
    final static public String gafSentiment = "http://groundedannotationframework.org/gaf/sentiment#";
    final static public String grasp = "http://groundedannotationframework.org/grasp#";
    final static public String graspAttribution = "http://groundedannotationframework.org/grasp/attribution#";
    final static public String graspSentiment = "http://groundedannotationframework.org/grasp/sentiment#";
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
        model.setNsPrefix("ili", ResourcesUri.ili);
      //  model.setNsPrefix("cornetto", ResourcesUri.cornetto);
        model.setNsPrefix("fn", ResourcesUri.fn);
        model.setNsPrefix("nwrdata", ResourcesUri.nwrdata);
        model.setNsPrefix("nwrontology", ResourcesUri.nwrontology);
        model.setNsPrefix("eso", ResourcesUri.eso);
        /// we need to take out the dbp ns because the URIs from dbp are not valid. They contain e.g. dots "Apple_Inc."
      //  model.setNsPrefix("dbp", ResourcesUri.dbp);
        model.setNsPrefix("pb", ResourcesUri.pb);

/*      //REMOVED DUE TO ILLEGAL CHARACTERS
        model.setNsPrefix("vn", ResourcesUri.vn);
        model.setNsPrefix("pb", ResourcesUri.pb);
        model.setNsPrefix("nb", ResourcesUri.nb);
*/
        model.setNsPrefix("sem", ResourcesUri.sem);
        model.setNsPrefix("owl", ResourcesUri.owl);
        model.setNsPrefix("time", ResourcesUri.owltime);
        model.setNsPrefix("skos", ResourcesUri.skos);
        model.setNsPrefix("eurovoc", ResourcesUri.eurovoc);
        model.setNsPrefix("rdf", ResourcesUri.rdf);
        model.setNsPrefix("rdfs", ResourcesUri.rdfs);
    }

    static public void prefixModelNwr (Model model) {
        model.setNsPrefix("nwr", ResourcesUri.nwr);
        model.setNsPrefix("nwrauthor", ResourcesUri.nwrauthor);
        model.setNsPrefix("nwrpublisher", ResourcesUri.nwrpublisher);
        model.setNsPrefix("nwrmagazine", ResourcesUri.nwrmagazine);
       // model.setNsPrefix("nwrsourceowner", ResourcesUri.nwrsourceowner);
    }

    static public void prefixModelGaf (Model model) {
        model.setNsPrefix("gaf", ResourcesUri.gaf);
        model.setNsPrefix("gafAttribution", ResourcesUri.gafAttribution);
        model.setNsPrefix("gafSentiment", ResourcesUri.gafSentiment);
        model.setNsPrefix("grasp", ResourcesUri.grasp);
        model.setNsPrefix("graspAttribution", ResourcesUri.graspAttribution);
        model.setNsPrefix("graspSentiment", ResourcesUri.graspSentiment);
        model.setNsPrefix("prov", ResourcesUri.prov);
    }

}
