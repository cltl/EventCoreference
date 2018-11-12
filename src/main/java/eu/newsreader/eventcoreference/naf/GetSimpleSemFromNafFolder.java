package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import eu.newsreader.eventcoreference.coref.ComponentMatch;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.output.JenaSerialization;
import eu.newsreader.eventcoreference.output.SimpleTaxonomy;
import eu.newsreader.eventcoreference.util.Util;
import org.apache.jena.atlas.logging.Log;
import org.apache.tools.bzip2.CBZip2InputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/30/13
 * Time: 7:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class GetSimpleSemFromNafFolder {

    static final String USAGE = "This program processes a single NAF file and generates SEM RDF-TRiG results" +
            "The program has the following arguments:\n" +
            "--naf-folder              <path> <The path to the NAF file>\n" +
            "--extension              <string> <The file extension>\n";

    ;

    static public void main(String[] args) {
        HashMap<String, String> rename = new HashMap<String, String>();
        SimpleTaxonomy simpleTaxonomy = new SimpleTaxonomy();

        Log.setLog4j("jena-log4j.properties");
        NafSemParameters nafSemParameters = new NafSemParameters((args));
        String pathToNafFolder = "";
        String extension = "";
        String pathToHierarchyFile = "";
        String pathToRenameFile = "/Code/vu/newsreader/vua-resources/NERC_DBpediaHierarchy_mapping.tsv";

        pathToNafFolder = "/Users/piek/Desktop/DigHum-2018/4775434/OBO_XML_7-2/sessions/naf-out/naf16/";
        extension = ".dom";
        pathToNafFolder = "/Users/piek/Desktop/Dasym/wikinews-en/wikinews_english_pipelinev3_20150727/corpus_gm_chrysler_ford/";
        extension = ".naf";
        pathToHierarchyFile = "/Code/vu/newsreader/vua-resources/dbpedia_nl_types.tsv.gz";   // Dutch
        pathToHierarchyFile = "/Code/vu/newsreader/vua-resources/instance_types_en.ttl.gz";     // English
        //<http://dbpedia.org/resource/Actrius> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Film> .
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--naf-folder") && args.length > (i + 1)) {
                pathToNafFolder = args[i + 1];
            }
            else if (arg.equals("--extension") && args.length > (i + 1)) {
                extension = args[i + 1];
            }
            else if (arg.equals("--ontology") && args.length > (i + 1)) {
                pathToHierarchyFile = args[i + 1];
            }
            else if (arg.equals("--rename") && args.length > (i + 1)) {
                pathToRenameFile = args[i + 1];
            }
        }
        if (!pathToRenameFile.isEmpty()) {
            rename = readRename(pathToRenameFile);
        }
        if (!pathToHierarchyFile.isEmpty()) {
            /// if Dutch
            //simpleTaxonomy.readSimpleTaxonomyFromFile(pathToHierarchyFile);
            /// if English
            simpleTaxonomy.readSimpleTaxonomyFromTtlFile(pathToHierarchyFile);
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
                    mySemEvent.setType(SemObject.EVENT);
                    ArrayList<SemTime> myTimes = ComponentMatch.getMySemTimes(mySemEvent, semRelations, semTimes);
                    ArrayList<SemActor> myActors = ComponentMatch.getMySemActors(mySemEvent, semRelations, semActors);
                    for (int k = 0; k < myActors.size(); k++) {
                        SemActor semActor = myActors.get(k);
                        semActor.setConcept(new ArrayList<KafSense>());
                        String uri = "<"+semActor.getURI()+">";
                       // System.out.println("semActor.getURI() = " + semActor.getURI());
                        if (simpleTaxonomy.subToSuper.containsKey(uri)) {
                            String superSense = simpleTaxonomy.subToSuper.get(uri);
                            superSense = superSense.substring(1, superSense.length()-1);
                            KafSense kafSense = new KafSense();
                            kafSense.setSensecode(superSense);
                            semActor.addConcept(kafSense);
                        }
                        else if (semActor.getURI().startsWith("http://dbpedia")) {
                           // System.out.println("semActor.getURI() = " + semActor.getURI());
                        }
                    }
                    ArrayList<SemRelation> myRelations = ComponentMatch.getMySemRelations(mySemEvent, semRelations);
                    CompositeEvent compositeEvent = new CompositeEvent(mySemEvent, myActors, myTimes, myRelations);
                    if (compositeEvent.isValid() || nafSemParameters.isALL()) {
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
                JenaSerialization.serializeJenaSimpleCompositeEvents(fos, compositeEventArraylist, rename);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }

    static HashMap<String, String> readRename (String filePath) {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            FileInputStream fis = new FileInputStream(filePath);
            InputStreamReader isr = new InputStreamReader(fis);

            if (isr!=null) {
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                while (in.ready() && (inputLine = in.readLine()) != null) {
                    inputLine = inputLine.trim();
                    if (inputLine.trim().length() > 0) {
                        String[] fields = inputLine.split("\t");
                        if (fields.length == 2) {
                            map.put(fields[0].trim(), fields[1].trim());
                        }
                    }
                }
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  map;
    }
}
