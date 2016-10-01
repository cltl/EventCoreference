package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.newsreader.eventcoreference.coref.ComponentMatch;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.output.JenaSerialization;
import eu.newsreader.eventcoreference.util.FrameTypes;
import eu.newsreader.eventcoreference.util.Util;
import org.apache.jena.atlas.logging.Log;
import org.apache.tools.bzip2.CBZip2InputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

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
    static public int TIMEEXPRESSIONMAX = 0;
    static public boolean NONENTITIES = true;
    static public boolean ILIURI = false;
    static public boolean VERBOSE = true;
    static public boolean ALL = true;
    static public boolean PERSPECTIVE = true;

    static boolean DOCTIME = true;
    static boolean CONTEXTTIME = true;


    static final String USAGE = "This program processes a single NAF file and generates SEM RDF-TRiG results" +
            "The program has the following arguments:\n" +
            "--naf-file              <path> <The path to the NAF file>\n" +
            "--project              <string> <The name of the project for creating URIs>\n" +
            "--non-entities                  <If used, additional FrameNet roles and non-entity phrases are included>\n" +
            "--contextual-frames    <path>   <Path to a file with the FrameNet frames considered contextual>\n" +
            "--communication-frames <path>   <Path to a file with the FrameNet frames considered source>\n" +
            "--grammatical-frames   <path>   <Path to a file with the FrameNet frames considered grammatical>" +
            "--time-max   <string int>   <Maximum number of time-expressions allows for an event to be included in the output. Excessive time links are problematic. The defeault value is 5" +
            "--ili                  <(OPTIONAL) Path to ILI.ttl file to convert wordnet-synsets identifiers to ILI identifiers>\n" +
            "--ili-uri                  <(OPTIONAL) If used, the ILI-identifiers are used to represents events. This is necessary for cross-lingual extraction>\n" +
            "--verbose                  <(OPTIONAL) representation of mentions is extended with token ids, terms ids and sentence number\n"
    ;

    static public void main(String[] args) {
        Log.setLog4j("jena-log4j.properties");
        String pathToNafFile = "";
        ALL = true;
        pathToNafFile = "/Users/piek/Desktop/NWR-INC/dasym/test/426115_relink_dominant.naf";
        String sourceFrameFile = "";
        sourceFrameFile = "/Code/vu/newsreader/vua-resources/source-nl.txt";
        sourceFrameFile = "/Code/vu/newsreader/vua-resources/source.txt";
        String contextualFrameFile = "";
        String grammaticalFrameFile = "";
        grammaticalFrameFile = "/Code/vu/newsreader/vua-resources/grammatical-nl.txt";
        grammaticalFrameFile = "/Code/vu/newsreader/vua-resources/grammatical.txt";
        String project = "";
        project = "test";
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
            else if (arg.equals("--all")) {
                ALL = true;
            }

            else if (arg.equals("--perspective")) {
                PERSPECTIVE = true;
            }
            else if (arg.equals("--ili") && args.length > (i + 1)) {
                String pathToILIFile = args[i+1];
                JenaSerialization.initILI(pathToILIFile);
            }
            else if (arg.equals("--eurovoc-en") && args.length > (i + 1)) {
                String pathToEurovocFile = args[i+1];
                GetSemFromNaf.initEurovoc(pathToEurovocFile, "en");
            }
            else if (arg.equals("--eurovoc-nl") && args.length > (i + 1)) {
                String pathToEurovocFile = args[i+1];
                GetSemFromNaf.initEurovoc(pathToEurovocFile, "nl");
            }
            else if (arg.equals("--eurovoc-es") && args.length > (i + 1)) {
                String pathToEurovocFile = args[i+1];
                GetSemFromNaf.initEurovoc(pathToEurovocFile, "es");
            }
            else if (arg.equals("--eurovoc-it") && args.length > (i + 1)) {
                String pathToEurovocFile = args[i+1];
                GetSemFromNaf.initEurovoc(pathToEurovocFile, "it");
            }
            else if (arg.equals("--ili-uri")) {
                ILIURI = true;
            }

            else if (arg.equals("--no-doc-time")) {
                DOCTIME = false;
            }
            else if (arg.equals("--no-context-time")) {
                CONTEXTTIME = false;
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
      //  System.out.println("pathToNafFile = " + pathToNafFile);
        sourceVector = Util.ReadFileToStringVector(sourceFrameFile);
        grammaticalVector = Util.ReadFileToStringVector(grammaticalFrameFile);
        contextualVector = Util.ReadFileToStringVector(contextualFrameFile);

        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemTime> semTimes = new ArrayList<SemTime>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        KafSaxParser kafSaxParser = new KafSaxParser();
       // kafSaxParser.parseFile(pathToNafFile);
        if (pathToNafFile.toLowerCase().endsWith(".gz")) {
            try {
                InputStream fileStream = new FileInputStream(pathToNafFile);
                InputStream gzipStream = new GZIPInputStream(fileStream);
                kafSaxParser.parseFile(gzipStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (pathToNafFile.toLowerCase().endsWith(".bz2")) {
            try {
                InputStream fileStream = new FileInputStream(pathToNafFile);
                InputStream gzipStream = new CBZip2InputStream(fileStream);
                kafSaxParser.parseFile(gzipStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            kafSaxParser.parseFile(pathToNafFile);
        }
        if (kafSaxParser.getKafMetaData().getUrl().isEmpty()) {
            System.out.println("file.getName() = " + new File(pathToNafFile).getName());
            kafSaxParser.getKafMetaData().setUrl(new File (pathToNafFile).getName());
            System.out.println("WARNING! Replacing empty url in header NAF with the file name!");
        }
        GetSemFromNaf.processNafFile(project, kafSaxParser, semEvents, semActors, semTimes,
                semRelations, NONENTITIES, DOCTIME, CONTEXTTIME);
        try {
           // System.out.println("semEvents = " + semEvents.size());
           // System.out.println("semActors = " + semActors.size());
          //  System.out.println("semRelations = " + semRelations.size());

/*            for (int i = 0; i < semActors.size(); i++) {
                SemObject semObject = semActors.get(i);
                System.out.println("semObject.getId() = " + semObject.getId());
                System.out.println("semObject.getURI() = " + semObject.getURI());
            }*/
/*            for (int i = 0; i < semRelations.size(); i++) {
                SemRelation semRelation = semRelations.get(i);
                if (semRelation.getObject().indexOf("#tmx")==-1 && semRelation.getObject().indexOf("#mdct")==-1)
                    System.out.println("semRelation.getObject() = " + semRelation.getObject());
            }*/

            ArrayList<CompositeEvent> compositeEventArraylist = new ArrayList<CompositeEvent>();
            for (int j = 0; j < semEvents.size(); j++) {
                SemEvent mySemEvent = (SemEvent) semEvents.get(j);
                ArrayList<SemTime> myTimes = ComponentMatch.getMySemTimes(mySemEvent, semRelations, semTimes);
                ArrayList<SemActor> myActors = ComponentMatch.getMySemActors(mySemEvent, semRelations, semActors);
                ArrayList<SemRelation> myRelations = ComponentMatch.getMySemRelations(mySemEvent, semRelations);
              //  System.out.println("myActors = " + myActors.size());
                CompositeEvent compositeEvent = new CompositeEvent(mySemEvent, myActors, myTimes, myRelations);
                if (myTimes.size()<=ClusterEventObjects.TIMEEXPRESSIONMAX || ALL) {
                    if (compositeEvent.isValid() || ALL) {
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
            if (!PERSPECTIVE) {
                JenaSerialization.serializeJenaCompositeEvents(fos, compositeEventArraylist, null, ILIURI, VERBOSE);
            }
            else {
                ArrayList<PerspectiveObject> sourcePerspectives = GetPerspectiveRelations.getSourcePerspectives(kafSaxParser,
                        project,
                        semActors,
                        semEvents,
                        contextualVector,
                        sourceVector,
                        grammaticalVector);
                ArrayList<PerspectiveObject> documentPerspectives = GetPerspectiveRelations.getAuthorPerspectives(
                        kafSaxParser, project, sourcePerspectives, semEvents);
                JenaSerialization.serializeJenaCompositeEventsAndPerspective(fos, compositeEventArraylist, kafSaxParser, sourcePerspectives, documentPerspectives);
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
