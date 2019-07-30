package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.kyotoproject.kaf.KafSense;
import eu.newsreader.eventcoreference.coref.ComponentMatch;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.output.JenaSerialization;
import eu.newsreader.eventcoreference.output.SimpleTaxonomy;
import eu.newsreader.eventcoreference.util.EventTypes;
import eu.newsreader.eventcoreference.util.Util;
import org.apache.jena.atlas.logging.Log;

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
        Boolean perspective = false;
        Boolean all = false;

        Log.setLog4j("jena-log4j.properties");
        NafSemParameters nafSemParameters = new NafSemParameters((args));
        String pathToNafFolder = "";
        String pathToRdfFolder = "";
        String extension = "";
        String pathToHierarchyFile = "";
        String pathToRenameFile = "";

        // pathToRenameFile = "/Code/vu/newsreader/vua-resources/NERC_DBpediaHierarchy_mapping.tsv";
        pathToNafFolder = "/Users/piek/Desktop/Deloitte/wikinews/";
       // pathToNafFolder = "/Users/piek/Desktop/DigHum-2018/4775434/OBO_XML_7-2/vu-ob-text-rdf/example/naf-dom";
        pathToRdfFolder = "/Users/piek/Desktop/Deloitte/nwr-rdf";
        extension = ".bz2";
        all = true;
       // pathToNafFolder = "/Users/piek/Desktop/Dasym/wikinews-en/wikinews_english_pipelinev3_20150727/corpus_gm_chrysler_ford/";
       // extension = ".naf";
       // pathToHierarchyFile = "/Code/vu/newsreader/vua-resources/dbpedia_nl_types.tsv.gz";   // Dutch
        pathToHierarchyFile = "/Code/vu/newsreader/vua-resources/instance_types_en.ttl.gz";     // English
        //<http://dbpedia.org/resource/Actrius> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Film> .
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--naf-folder") && args.length > (i + 1)) {
                pathToNafFolder = args[i + 1];
            }
            else if (arg.equals("--rdf-folder") && args.length > (i + 1)) {
                pathToRdfFolder = args[i + 1];
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
            else if (arg.equals("--all")) {
                all = true;
            }
            else if (arg.equals("--perspective")) {
                perspective = true;
            }
        }

        File rdfFolder = new File(pathToRdfFolder);
        if (!rdfFolder.exists()) {
            rdfFolder.mkdir();
        }

        if (!rdfFolder.exists()) {
            System.out.println("rdfFolder = " + rdfFolder.exists());
            System.out.println("rdfFolder.getAbsolutePath() = " + rdfFolder.getAbsolutePath());
            return;
        }

        File nafFolder = new File(pathToNafFolder);
        if (!nafFolder.exists()) {
            System.out.println("nafFolder = " + nafFolder.exists());
            System.out.println("nafFolder.getAbsolutePath() = " + nafFolder.getAbsolutePath());
            return;
        }

        System.out.println("pathToHierarchyFile = " + pathToHierarchyFile);
        System.out.println("pathToNafFolder = " + pathToNafFolder);
        System.out.println("pathToRdfFolder = " + pathToRdfFolder);
        System.out.println("extension = " + extension);


        if (!pathToRenameFile.isEmpty()) {
            rename = readRename(pathToRenameFile);
        }
        if (!pathToHierarchyFile.isEmpty()) {
            /// if Dutch
            //simpleTaxonomy.readSimpleTaxonomyFromFile(pathToHierarchyFile);
            /// if English
            simpleTaxonomy.readSimpleTaxonomyFromTtlFile(pathToHierarchyFile);
        }
        int count = 0;
        ArrayList<File> files = new ArrayList<File>();
        File file = null;

        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemTime> semTimes = new ArrayList<SemTime>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();
        ArrayList<CompositeEvent> compositeEventArraylist = new ArrayList<CompositeEvent>();

        files = Util.makeRecursiveFileList(new File(pathToNafFolder), extension);
        System.out.println("input files.size() = " + files.size());
       // files.add(new File ("/Users/piek/Desktop/DigHum-2018/4775434/OBO_XML_7-2/sessions/naf-out/naf16/1699-12-13_t16991213-9-verdict45.txt.naf.dom"));
        KafSaxParser kafSaxParser = new KafSaxParser();
        for (int i = 0; i < files.size(); i++) {
            file = files.get(i);
            kafSaxParser = new KafSaxParser();
            //System.out.println("file.getName() = " + file.getName());

            count++;
            if (files.size()<500) {
                System.out.println("file.getName() = " + file.getName());
            }
            if (count%500==0) {
                System.out.println("Nr. naf files processed = " + count+ " out of:"+files.size());
            }
            String pathToTrigFile = rdfFolder.getAbsolutePath()+"/"+file.getName() + ".trig";
            if (new File (pathToTrigFile).exists()) {
                System.out.println("Skipping file.getName(), already present in the output-folder = " + file.getName());
                continue;
            }
            semEvents = new ArrayList<SemObject>();
            semActors = new ArrayList<SemObject>();
            semTimes = new ArrayList<SemTime>();
            semRelations = new ArrayList<SemRelation>();
           // System.out.println("files.get(i).getName() = " + files.get(i).getName());
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
                   // BufferedReader br2 = new BufferedReader(new InputStreamReader(fileStream));

                   // InputStream gzipStream = new CBZip2InputStream(fileStream);
                   // InputStream gzipStream = new GZIPInputStream(fileStream);

                    FileInputStream fin = new FileInputStream(file);
                    org.apache.tools.bzip2.CBZip2InputStream gzipStream = new org.apache.tools.bzip2.CBZip2InputStream(fileStream);

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

                compositeEventArraylist = new ArrayList<CompositeEvent>();
                // System.out.println("semEvents = " + semEvents.size());
                ArrayList<SemTime> myTimes = null;
                ArrayList<SemActor> myActors = null;
                ArrayList<SemRelation> myRelations = null;
                for (int j = 0; j < semEvents.size(); j++) {
                    SemEvent mySemEvent = (SemEvent) semEvents.get(j);
                    if (all || isContextual(mySemEvent)) {
                        mySemEvent.setType(SemObject.EVENT);
                        myTimes = ComponentMatch.getMySemTimes(mySemEvent, semRelations, semTimes);
                        myActors = ComponentMatch.getMySemActors(mySemEvent, semRelations, semActors);
                        for (int k = 0; k < myActors.size(); k++) {
                            SemActor semActor = myActors.get(k);
                            ArrayList<KafSense> concepts = new ArrayList<KafSense>();
                            String uri = "<" + semActor.getURI() + ">";
                            // System.out.println("semActor.getURI() = " + semActor.getURI());
                            if (simpleTaxonomy.subToSuper.containsKey(uri)) {
                                String superSense = simpleTaxonomy.subToSuper.get(uri);
                                superSense = superSense.substring(1, superSense.length() - 1);
                                KafSense kafSense = new KafSense();
                                kafSense.setSensecode(superSense);
                                concepts.add(kafSense);
                            }
                            else if (semActor.getURI().startsWith("http://dbpedia")) {
                                // System.out.println("semActor.getURI() = " + semActor.getURI());
                            }
                            else {

                            }
                            for (int l = 0; l < semActor.getConcepts().size(); l++) {
                                KafSense kafSense = semActor.getConcepts().get(l);
                                if (!kafSense.getSensecode().startsWith("http://dbpedia") || semActor.getType().equals(SemObject.NONENTITY)) {
                                    concepts.add(kafSense);
                                }
                            }
                            semActor.setConcept(new ArrayList<KafSense>());
                            for (int l = 0; l < concepts.size(); l++) {
                                KafSense kafSense = concepts.get(l);
                                semActor.addConcept(kafSense);
                            }
                        }
                        myRelations = ComponentMatch.getMySemRelations(mySemEvent, semRelations);
                        CompositeEvent compositeEvent = new CompositeEvent(mySemEvent, myActors, myTimes, myRelations);
                        if (compositeEvent.isValid() || nafSemParameters.isALL()) {
                            compositeEventArraylist.add(compositeEvent);
                        } else {
                            System.out.println("Skipping EVENT due to no time anchor and/or no participant");
                            System.out.println("compositeEvent = " + compositeEvent.getEvent().getURI());
                            System.out.println("myTimes = " + myTimes.size());
                            System.out.println("myActors = " + myActors.size());
                            System.out.println("myRelations = " + myRelations.size());
                        }
                    }
                }
                OutputStream fos = new FileOutputStream(pathToTrigFile);
                JenaSerialization.serializeJenaSimpleCompositeEvents(fos, compositeEventArraylist, simpleTaxonomy, rename);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }

    static boolean isContextual (SemEvent semEvent) {
        for (int i = 0; i < semEvent.getConcepts().size(); i++) {
            KafSense kafSense = semEvent.getConcepts().get(i);
            if (EventTypes.isCONTEXTUAL(kafSense.getSensecode())) {
                return true;
            }
        }
        return false;
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
