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
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/30/13
 * Time: 7:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class GetSemFromNafFolder {

    static final String USAGE = "This program processes a single NAF file and generates SEM RDF-TRiG results" +
            "The program has the following arguments:\n" +
            "--naf-folder              <path> <The path to the NAF file>\n" +
            "--extension              <string> <The file extension>\n";

    ;

    static public void main(String[] args) {
        Log.setLog4j("jena-log4j.properties");
        NafSemParameters nafSemParameters = new NafSemParameters((args));
        String pathToNafFolder = "";
        String extension = "";

        pathToNafFolder = "/Users/piek/Desktop/NWR/timeline/demo/data/naf/";
        extension = ".naf";


        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--naf-folder") && args.length > (i + 1)) {
                pathToNafFolder = args[i + 1];
            }
            else if (arg.equals("--extension") && args.length > (i + 1)) {
                extension = args[i + 1];
            }
        }

        ArrayList<File> files = Util.makeRecursiveFileList(new File(pathToNafFolder), extension);
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
          //  String pathToNafFile = files.get(i).getAbsolutePath();
            ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
            ArrayList<SemObject> semActors = new ArrayList<SemObject>();
            ArrayList<SemTime> semTimes = new ArrayList<SemTime>();
            ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
           // System.out.println("files.get(i).getName() = " + files.get(i).getName());
            KafSaxParser kafSaxParser = new KafSaxParser();
            if (file.getName().toLowerCase().endsWith(".gz")) {
                try {
                    InputStream fileStream = new FileInputStream(file);
                    InputStream gzipStream = new GZIPInputStream(fileStream);
                    kafSaxParser.parseFile(gzipStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //    BufferedReader br2 = new BufferedReader(new InputStreamReader(input));
            //InputStream is = new CBZip2InputStream(new ByteArrayInputStream(bzip2));

            else if (file.getName().toLowerCase().endsWith(".bz2")) {
                try {
                    InputStream fileStream = new FileInputStream(file);
                    InputStream gzipStream = new CBZip2InputStream(fileStream);
                  //  InputStream gzipStream = new GZIPInputStream(fileStream);

                    kafSaxParser.parseFile(gzipStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
               kafSaxParser.parseFile(file);
            }
            if (kafSaxParser.getKafMetaData().getUrl().isEmpty()) {
                System.out.println("file.getName() = " + file.getName());
                kafSaxParser.getKafMetaData().setUrl(file.getName());
                System.out.println("WARNING! Replacing empty url in header NAF with the file name!");
            }
            GetSemFromNaf.processNafFile(nafSemParameters, kafSaxParser, semEvents, semActors, semTimes, semRelations);
            try {

                ArrayList<CompositeEvent> compositeEventArraylist = new ArrayList<CompositeEvent>();
                // System.out.println("semEvents = " + semEvents.size());
                for (int j = 0; j < semEvents.size(); j++) {
                    SemEvent mySemEvent = (SemEvent) semEvents.get(j);
                    ArrayList<SemTime> myTimes = ComponentMatch.getMySemTimes(mySemEvent, semRelations, semTimes);
                    ArrayList<SemActor> myActors = ComponentMatch.getMySemActors(mySemEvent, semRelations, semActors);
                    ArrayList<SemRelation> myRelations = ComponentMatch.getMySemRelations(mySemEvent, semRelations);
                    CompositeEvent compositeEvent = new CompositeEvent(mySemEvent, myActors, myTimes, myRelations);
                    if (compositeEvent.isValid() || nafSemParameters.isALL()) {
                        FrameTypes.setEventTypeString(compositeEvent.getEvent(), nafSemParameters);
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
                String pathToTrigFile = file.getAbsolutePath() + ".trig";
                OutputStream fos = new FileOutputStream(pathToTrigFile);
                if (!nafSemParameters.isPERSPECTIVE()) {
                    JenaSerialization.serializeJenaCompositeEvents(fos, compositeEventArraylist, null, nafSemParameters.isILIURI(), nafSemParameters.isVERBOSE());
                }
                else {
                    ArrayList<PerspectiveObject> sourcePerspectives = GetPerspectiveRelations.getSourcePerspectives(kafSaxParser,
                            semActors,
                            semEvents,
                            nafSemParameters);
                    ArrayList<PerspectiveObject> documentPerspectives = GetPerspectiveRelations.getAuthorPerspectives(
                            kafSaxParser, nafSemParameters.getPROJECT(), sourcePerspectives, semEvents);
                    JenaSerialization.serializeJenaCompositeEventsAndPerspective(fos, compositeEventArraylist, kafSaxParser, nafSemParameters.getPROJECT(), sourcePerspectives, documentPerspectives);
                }
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }


}
