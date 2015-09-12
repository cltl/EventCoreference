package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.newsreader.eventcoreference.coref.ComponentMatch;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.output.JenaSerialization;
import eu.newsreader.eventcoreference.util.FrameTypes;
import eu.newsreader.eventcoreference.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/30/13
 * Time: 7:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class GetSemFromNafFile {

    static public Vector<String> sourceVector = null;
    static public Vector<String> grammaticalVector = null;
    static public Vector<String> contextualVector = null;
    static public int TIMEEXPRESSIONMAX = 5;
    static public boolean NONENTITIES = false;
    static public boolean ILIURI = false;
    static public boolean VERBOSE = false;


    static final String USAGE = "This program processes a single NAF file and generates SEM RDF-TRiG results" +
            "The program has the following arguments:\n" +
            "--naf-file              <path> <The path to the NAF file>\n" +
            "--project              <string> <The name of the project for creating URIs>\n" +
            "--non-entities                  <If used, additional FrameNet roles and non-entity phrases are included>\n" +
            "--contextual-frames    <path>   <Path to a file with the FrameNet frames considered contextual>\n" +
            "--communication-frames <path>   <Path to a file with the FrameNet frames considered source>\n" +
            "--grammatical-frames   <path>   <Path to a file with the FrameNet frames considered grammatical>" +
            "--time-max   <string int>   <Maximum number of time-expressions allows for an event to be included in the output. Excessive time links are problematic. The defeault value is 5" +
            "--ili-uri                  <(OPTIONAL) If used, the ILI-identifiers are used to represents events. This is necessary for cross-lingual extraction>\n" +
            "--verbose                  <(OPTIONAL) representation of mentions is extended with token ids, terms ids and sentence number\n"
    ;

    static public void main(String[] args) {
        String pathToNafFile = "";
        String sourceFrameFile = "";
        String contextualFrameFile = "";
        String grammaticalFrameFile = "";
        String project = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--naf-file") && args.length > (i + 1)) {
                pathToNafFile = args[i + 1];
            } else if (arg.equals("--project") && args.length > (i + 1)) {
                project = args[i + 1];
            }
            else if (arg.equals("--non-entities")) {
                NONENTITIES = true;
            }
            else if (arg.equals("--verbose")) {
                VERBOSE = true;
            }
            else if (arg.equals("--ili-uri")) {
                ILIURI = true;
            }
            else if (arg.equals("--time-max")  && args.length > (i + 1)) {
                try {
                    TIMEEXPRESSIONMAX = Integer.parseInt(args[i+1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else if (arg.equals("--source-frames") && args.length>(i+1)) {
                sourceFrameFile = args[i+1];
            }
            else if (arg.equals("--grammatical-frames") && args.length>(i+1)) {
                grammaticalFrameFile = args[i+1];
            }
            else if (arg.equals("--contextual-frames") && args.length>(i+1)) {
                contextualFrameFile = args[i+1];
            }
        }
        sourceVector = Util.ReadFileToStringVector(sourceFrameFile);
        grammaticalVector = Util.ReadFileToStringVector(grammaticalFrameFile);
        contextualVector = Util.ReadFileToStringVector(contextualFrameFile);

        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemTime> semTimes = new ArrayList<SemTime>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(pathToNafFile);
        if (kafSaxParser.getKafMetaData().getUrl().isEmpty()) {
            System.out.println("file.getName() = " + new File(pathToNafFile).getName());
            kafSaxParser.getKafMetaData().setUrl(new File (pathToNafFile).getName());
            System.out.println("WARNING! Replacing empty url in header NAF with the file name!");
        }
        GetSemFromNaf.processNafFile(project, kafSaxParser, semEvents, semActors, semTimes, semRelations, NONENTITIES);
        try {

            ArrayList<CompositeEvent> compositeEventArraylist = new ArrayList<CompositeEvent>();
           // System.out.println("semEvents = " + semEvents.size());
            for (int j = 0; j < semEvents.size(); j++) {
                SemEvent mySemEvent = (SemEvent) semEvents.get(j);
                ArrayList<SemTime> myTimes = ComponentMatch.getMySemTimes(mySemEvent, semRelations, semTimes);
                ArrayList<SemActor> myActors = ComponentMatch.getMySemActors(mySemEvent, semRelations, semActors);
                ArrayList<SemRelation> myRelations = ComponentMatch.getMySemRelations(mySemEvent, semRelations);
                CompositeEvent compositeEvent = new CompositeEvent(mySemEvent, myActors, myTimes, myRelations);
                if (myTimes.size()<=ClusterEventObjects.TIMEEXPRESSIONMAX) {
                    if (compositeEvent.isValid()) {
                        FrameTypes.setEventTypeString(compositeEvent.getEvent(),contextualVector, sourceVector, grammaticalVector);
                        compositeEventArraylist.add(compositeEvent);
                    }
                    else {
                        System.out.println("Skipping EVENT due to no time anchor and/or no participant");
                        System.out.println("compositeEvent = " + compositeEvent.getEvent().getURI());
                        System.out.println("myTimes = " + myTimes.size());
                        System.out.println("myActors = " + myActors.size());
                        System.out.println("myRelations = " + myRelations.size());
                    }
                }
                else {
                    System.out.println("Skipping EVENT due to excessive time expressions linked to it");
                    System.out.println("compositeEvent = " + compositeEvent.getEvent().getURI());
                    System.out.println("myTimes.size() = " + myTimes.size());
                }
            }
            String pathToTrigFile = pathToNafFile + ".trig";
            OutputStream fos = new FileOutputStream(pathToTrigFile);
            JenaSerialization.serializeJenaCompositeEvents(fos, compositeEventArraylist, null, ILIURI, VERBOSE);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
        pathToNafFile = "/Users/piek/Desktop/English Pipeline/naf3.0/obama_v3_event_coref_in.xml";
        pathToNafFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v3_2015/test/obama_v3_event_coref_out.xml";
        pathToNafFile = "/Users/piek/Desktop/English Pipeline/timex-duration/89284_Apple_to_lower_UK_iTunes.naf.coref";
        pathToNafFile = "/Users/piek/Desktop/English Pipeline/timex-duration/116834_Japan_enters.naf.coref";
        pathToNafFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v3_2015/test/58T2-K531-JCDY-Y0X2.xml";
        pathToNafFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v3_2015/test/4M1J-3MC0-TWKJ-V1W8.xml";
        pathToNafFile = "/Users/piek/Desktop/NWR/en_pipeline3.0/v3_test_dataset/test_out/597K-JHX1-JCK4-01WV.xml";
        pathToNafFile = "/Users/piek/Desktop/NWR/en_pipeline3.0/v3_test_dataset/test_out/58PV-TK71-DXDT-62KX.xml";
        pathToNafFile = "/Users/piek/Desktop/NWR/NWR-ontology/wikinews_v3_out/corpus_airbus/71426_Aer_Lingus_buys_twelve_new_long-haul_Airbus_jets.naf";
        pathToNafFile = "/Users/piek/Desktop/NWR/en_pipeline3.0/v3_test_dataset/out/5302-J0G1-JBRC-V1BP.xml";
        pathToNafFile = "/Users/piek/Desktop/NWR/NWR-ontology/wikinews_v3_out/corpus_airbus/87805_Indonesia_transport_minister.naf";
        pathToNafFile = "/Users/piek/Desktop/NWR/NWR-ontology/wikinews_v3_out/corpus_gm/120578_Automobile_sales_in_the_United_States_down_sharply.naf";

     */

}
