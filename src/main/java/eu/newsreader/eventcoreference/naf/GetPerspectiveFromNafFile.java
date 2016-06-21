package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.newsreader.eventcoreference.objects.PerspectiveObject;
import eu.newsreader.eventcoreference.objects.SemObject;
import eu.newsreader.eventcoreference.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class GetPerspectiveFromNafFile {

    static public Vector<String> sourceVector = null;
    static public Vector<String> grammaticalVector = null;
    static public Vector<String> contextualVector = null;


    static final String USAGE = "This program processes a single NAF file and generates SEM RDF-TRiG results" +
            "The program has the following arguments:\n" +
            "--naf                  <path> <The path to the NAF file or folder with NAF files>\n" +
            "--extension            <string> <The file extension for the NAF files>\n" +
            "--project              <string> <The name of the project for creating URIs>\n" +
            "--not-filter-source      <Source can be any a0 role\n"+
            "--contextual-frames    <path>   <Path to a file with the FrameNet frames considered contextual>\n" +
            "--communication-frames <path>   <Path to a file with the FrameNet frames considered source>\n" +
            "--grammatical-frames   <path>   <Path to a file with the FrameNet frames considered grammatical>"
    ;

    static public void main(String[] args) {
        String pathToNafFile = "";
        String sourceFrameFile = "";
        String contextualFrameFile = "";
        String grammaticalFrameFile = "";
        String project = "";
        String extension = "";




        pathToNafFile = "/Users/piek/Desktop/NWR/timeline/demo/data/naf/";
        project = "wikinews";
        extension = ".naf";
        sourceFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v4_2015/resources/source.txt";
        grammaticalFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v4_2015/resources/grammatical.txt";
        contextualFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v4_2015/resources/contextual.txt";



        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--naf") && args.length > (i + 1)) {
                pathToNafFile = args[i + 1];
            } else if (arg.equals("--extension") && args.length > (i + 1)) {
                extension = args[i + 1];
            } else if (arg.equals("--project") && args.length > (i + 1)) {
                project = args[i + 1];
            } else if (arg.equals("--source-frames") && args.length > (i + 1)) {
                sourceFrameFile = args[i + 1];
            } else if (arg.equals("--not-filter-source")) {
                GetPerspectiveRelations.FILTERA0 = false;
            } else if (arg.equals("--grammatical-frames") && args.length > (i + 1)) {
                grammaticalFrameFile = args[i + 1];
            } else if (arg.equals("--contextual-frames") && args.length > (i + 1)) {
                contextualFrameFile = args[i + 1];
            }
        }





        sourceVector = Util.ReadFileToStringVector(sourceFrameFile);
        grammaticalVector = Util.ReadFileToStringVector(grammaticalFrameFile);
        contextualVector = Util.ReadFileToStringVector(contextualFrameFile);
        try {
            File nafFile = new File(pathToNafFile);
            if (nafFile.isDirectory()) {
                ArrayList<File> files = Util.makeRecursiveFileList(nafFile, extension);
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                   // System.out.println("file.getName() = " + file.getName());
                    getPerspectiveFromFile(file.getAbsolutePath(), project);
                }
            }
            else {
                getPerspectiveFromFile(pathToNafFile, project);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static public void getPerspectiveFromFile (String pathToNafFile, String project) throws IOException {
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        KafSaxParser kafSaxParser = new KafSaxParser();
        if (!pathToNafFile.toLowerCase().endsWith(".gz")) {
            kafSaxParser.parseFile(pathToNafFile);
        }
        else {
            InputStream fileStream = new FileInputStream(pathToNafFile);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            kafSaxParser.parseFile(gzipStream);
        }

        if (kafSaxParser.getKafMetaData().getUrl().isEmpty()) {
            System.out.println("file.getName() = " + new File(pathToNafFile).getName());
            kafSaxParser.getKafMetaData().setUrl(new File (pathToNafFile).getName());
            System.out.println("WARNING! Replacing empty url in header NAF with the file name!");
        }

        String baseUrl = kafSaxParser.getKafMetaData().getUrl() + GetSemFromNaf.ID_SEPARATOR;
        String entityUri = ResourcesUri.nwrdata + project + "/entities/";
        if (!baseUrl.toLowerCase().startsWith("http")) {
            baseUrl = ResourcesUri.nwrdata + project + "/" + kafSaxParser.getKafMetaData().getUrl() + GetSemFromNaf.ID_SEPARATOR;
        }

        GetSemFromNaf.processNafFileForEventCoreferenceSets(baseUrl, kafSaxParser, semEvents);
        GetSemFromNaf.processNafFileForEntityCoreferenceSets(entityUri, baseUrl, kafSaxParser, semActors);
        GetSemFromNaf.processSrlForRemainingFramenetRoles(project, kafSaxParser, semActors);

        ArrayList<PerspectiveObject> sourcePerspectives = GetPerspectiveRelations.getSourcePerspectives(kafSaxParser,
                project,
                semActors,
                semEvents,
                contextualVector,
                sourceVector,
                grammaticalVector);
        ArrayList<PerspectiveObject> documentPerspectives = GetPerspectiveRelations.getAuthorPerspectives(kafSaxParser,
                project, sourcePerspectives, semEvents);
        String perspectiveFilePath = pathToNafFile + ".perspective.trig";
        GetPerspectiveRelations.perspectiveRelationsToTrig(perspectiveFilePath, kafSaxParser, sourcePerspectives, documentPerspectives);
    }
}
