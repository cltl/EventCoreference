package eu.newsreader.eventcoreference.input;

import eu.newsreader.eventcoreference.util.Util;
import org.apache.jena.riot.writer.TriGWriter;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.trig.TriGParser;

import java.io.*;
import java.util.*;

/**
 * Created by piek on 1/20/14.
 */
public class SesameTrigReader {
    static Set<Statement> inputCollection = new LinkedHashSet<Statement>();
    static StatementCollector inputCollector = new StatementCollector(inputCollection);
    static TriGParser trigParser;
    static TriGWriter trigWriter;

    // Parse expected output data
      /*  RDFFormat outputFormat = Rio.getParserFormatForFileName(outputURL.toExternalForm());
        RDFParser outputParser = Rio.createParser(outputFormat);
        outputParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

        Set<Statement> outputCollection = new LinkedHashSet<Statement>();
        StatementCollector outputCollector = new StatementCollector(outputCollection);
        outputParser.setRDFHandler(outputCollector);

        in = outputURL.openStream();
        try {
            outputParser.parse(in, base(baseURL));
        }
        finally {
            in.close();
        }*/

    static void processTrigFiles (String name, String datafolder, ArrayList<File> trigFiles) {
        try {
            System.out.println("name = " + name);
            trigParser = new TriGParser();
            inputCollection = new LinkedHashSet<Statement>();
            inputCollector = new StatementCollector(inputCollection);

            trigParser.setRDFHandler(inputCollector);
            //for (int i = 0; i < 10; i++) {
            for (int i = 0; i < trigFiles.size(); i++) {
                File file = trigFiles.get(i);
               // System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
                InputStream fis = new FileInputStream(file);
                trigParser.parse(fis, datafolder);
                /*Map<String, String> entrySet = inputCollector.getNamespaces();
                System.out.println("entrySet.size() = " + entrySet.size());
                Iterator<String> keys = entrySet.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    System.out.println(" key = " +  key);
                }*/
                fis.close();
            }
            System.out.println("total nr. of statements = " + inputCollection.size());
            String str = "";
            Statement statement = null;
            Object [] statements = inputCollection.toArray();
            FileOutputStream fos = new FileOutputStream(datafolder+"/"+name+".dump");
            trigWriter = new TriGWriter();
           // trigWriter.write(fos, inputCollection.);
            for (int i = 0; i < statements.length; i++) {
                statement = (Statement) statements[i];
                if (i%100000 ==0) {
                    System.out.println("Nr of written statements = " + i);
                }
                str = statement.getSubject()+" : "+statement.getPredicate()+" : "+statement.getObject()+"\n";

                fos.write(str.getBytes());
            }
            fos.close();



        } catch (IOException e) {
            e.printStackTrace();
        } catch (RDFParseException e) {
            e.printStackTrace();
        } catch (RDFHandlerException e) {
            e.printStackTrace();
        }
    }

    static void processSeparateTrigFiles (String datafolder, ArrayList<File> trigFiles) {
        try {
            //for (int f = 0; f < 10; f++) {
            for (int f = 0; f < trigFiles.size(); f++) {
                File file = trigFiles.get(f);
                TriGParser trigParser = new TriGParser();
                Set<Statement> inputCollection = new LinkedHashSet<Statement>();
                StatementCollector inputCollector = new StatementCollector(inputCollection);
                trigParser.setRDFHandler(inputCollector);
                System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
                InputStream fis = new FileInputStream(file);
                trigParser.parse(fis, datafolder);
                fis.close();
                FileOutputStream fos = new FileOutputStream(datafolder+"/"+f+"_car.dump");
                for (int i = 0; i < inputCollection.size(); i++) {
                    Statement statement = (Statement) inputCollection.toArray()[i];
                    if (i%1000 ==0) {
                        System.out.println("i = " + i);
                    }
                    //String str = statement.getSubject()+" : "+statement.getPredicate()+" : "+statement.getObject()+"\n";
                    //fos.write(str.getBytes());
                }
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RDFParseException e) {
            e.printStackTrace();
        } catch (RDFHandlerException e) {
            e.printStackTrace();
        }
    }

    static void processYears (File trigFolder, String dataFolder) {
        HashMap<String, ArrayList<File>> years = Util.makeFolderGroupList(trigFolder, 4, ".trig");
        Set keySet = years.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            //if (key.equals("2008")) continue;
            ArrayList<File> trigFiles = years.get(key);
            processTrigFiles(key, dataFolder, trigFiles);
        }
    }
    static public void main (String[] args) {
        String trigfolder = args[0];
        String datafolder = args[1];
            //String trigfolder = "/Users/piek/Desktop/NWR-DATA/trig/entitycoref/";
            //String datafolder = "/Users/piek/Desktop/NWR-DATA/trig/dataset/";
            processYears(new File (trigfolder), datafolder);

            //ArrayList<File> trigFiles = Util.makeRecursiveFileList(new File(trigfolder), ".trig");
            //processTrigFiles(datafolder, trigFiles);
            //processSeparateTrigFiles(datafolder, trigFiles);
    }

    static public void main_org (String[] args) {
        String trigfolder = args[0];
        String datafolder = args[1];
        try {
            trigfolder = "/Users/piek/Desktop/NWR-DATA/trig/entitycoref/";
            datafolder = "/Users/piek/Desktop/NWR-DATA/trig/dataset/";
            ArrayList<File> trigFiles = Util.makeRecursiveFileList(new File(trigfolder), ".trig");
            for (int i = 0; i < trigFiles.size(); i++) {
                File file = trigFiles.get(i);
                InputStream fis = new FileInputStream(file);
                TriGParser trigParser = new TriGParser();
                trigParser.parse(fis, datafolder);
               // System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
               // System.out.println("trigParser.getRDFFormat() = " + trigParser.getRDFFormat());
                trigParser.getRDFHandler().startRDF();
                break;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (RDFHandlerException e) {
            e.printStackTrace();
        } catch (RDFParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
