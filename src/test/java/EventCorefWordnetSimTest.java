import eu.newsreader.eventcoreference.naf.EventCorefWordnetSim;

/**
 * Created by piek on 03/03/16.
 */
public class EventCorefWordnetSimTest {
    //REL="has_hyperonym#event#has_hypernym"

    static public void test() {

        //String [] args = {"--method", "leacock-chodorow","--wn-lmf", "/Code/vu/newsreader/EventCoreference/resources/wneng-30.lmf.xml.xpos.gz", "--sim","2.0","--sim-ont","0.6","--wsd","0.8","--wn-prefix",
        //                  "eng","--relations","has_hyperonym#event#has_hypernym","--naf-folder","$INPUT","--extension",".xml","--replace","--output-tag",".coref","--wn-prefix","eng"};
        String [] args = {"--ignore-false","--method", "leacock-chodorow","--wn-lmf", "/Code/vu/newsreader/EventCoreference/resources/wneng-30.lmf.xml.xpos.gz", "--sim","2.0","--sim-ont","0.6","--wsd","0.8","--wn-prefix",
                          "eng","--relations","has_hyperonym#event#has_hypernym","--naf-file","/Users/piek/Desktop/NWR/benchmark/ecb/nwr/data/ecb_pip.gold/33/33_1ecb.xml.naf.fix.xml","--replace","--output-tag",".coref","--wn-prefix","eng"};
        EventCorefWordnetSim.processArgs(args);
        EventCorefWordnetSim.printSettings();
        EventCorefWordnetSim.processNafFile(EventCorefWordnetSim.pathToNafFile);
    }

    static public void main (String [] args) {
        test();
    }
}
