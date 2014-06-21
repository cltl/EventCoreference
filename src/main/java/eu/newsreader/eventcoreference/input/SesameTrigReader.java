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
            FileOutputStream fosNode1 = new FileOutputStream(datafolder+"/"+name+".node1.csv");
            FileOutputStream fosEdge1 = new FileOutputStream(datafolder+"/"+name+".edge1.csv");
            FileOutputStream fosNode2 = new FileOutputStream(datafolder+"/"+name+".node2.csv");
            FileOutputStream fosEdge2 = new FileOutputStream(datafolder+"/"+name+".edge2.csv");
            String str = "Source\tLabel\tTarget\n";
            fosEdge1.write(str.getBytes());
            fosEdge2.write(str.getBytes());
            str = "ID\tLabel\n";
            fosNode1.write(str.getBytes());
            fosNode2.write(str.getBytes());
            trigParser = new TriGParser();
            inputCollection = new LinkedHashSet<Statement>();
            inputCollector = new StatementCollector(inputCollection);

            trigParser.setRDFHandler(inputCollector);
            //for (int i = 0; i < 10; i++) {
            for (int i = 0; i < trigFiles.size(); i++) {
                File file = trigFiles.get(i);
                System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
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
                if (i==200) {break;}
            }
            System.out.println("total nr. of statements = " + inputCollection.size());
            str = "";
            Statement statement = null;
            Object [] statements = inputCollection.toArray();
            trigWriter = new TriGWriter();
            HashMap<String, ArrayList<String>> eventTargets = new HashMap<String, ArrayList<String>>();
            for (int i = 0; i < statements.length; i++) {
                statement = (Statement) statements[i];
                if (i%100000 ==0) {
                    System.out.println("Nr of written statements = " + i);
                }
                if (( statement.getPredicate().stringValue().endsWith("sem/hasActor") && statement.getObject().stringValue().startsWith("http://dbpedia.org"))
                    // || ( statement.getPredicate().stringValue().endsWith("sem/hasPlace") && statement.getObject().stringValue().startsWith("http://dbpedia.org"))
                     //|| ( statement.getPredicate().stringValue().endsWith("sem/hasTime"))
                   )
                {
                    String event = getLabelFromElement(statement.getSubject().toString());
                    String target = getLabelFromElement(statement.getObject().toString());
                    if (eventTargets.containsKey(event)) {
                        ArrayList<String> targets = eventTargets.get(event);
                        targets.add(target);
                        eventTargets.put(event, targets);
                    }
                    else {
                        ArrayList<String> targets = new ArrayList<String>();
                        targets.add(target);
                        eventTargets.put(event, targets);
                    }
                    str = statement.getSubject() + "\t"
                            +  getLabelFromElement(statement.getPredicate().toString()) + " \t"
                            + statement.getObject() +"\n";
                    fosEdge1.write(str.getBytes());
                    str = statement.getSubject()+"\t"+ getLabelFromElement(statement.getSubject().toString())+"\n";
                    str += statement.getObject()+"\t"+ getLabelFromElement(statement.getObject().toString())+"\n";
                    fosNode1.write(str.getBytes());
                }
            }
            fosNode1.close();
            fosEdge1.close();
            Set keySet = eventTargets.keySet();
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
               // str = key+"\t"+key+"\n";
               // fosNode2.write(str.getBytes());
                ArrayList<String> targets = eventTargets.get(key);
                if (targets.size()==1) {
/*
                    for (int i = 0; i < targets.size(); i++) {
                        String t = targets.get(i);
                        str = t+"\t"+t+"\n";
                        fosNode2.write(str.getBytes());
                        str = key+"\t"+"hasActor"+"\t"+t+"\n";
                        fosEdge2.write(str.getBytes());
                    }
*/
                }
                else {
                    for (int i = 0; i < targets.size(); i++) {
                        String t1 = targets.get(i);
                        str = t1 + "\t" + t1 + "\n";
                        fosNode2.write(str.getBytes());
                        for (int j = i + 1; j < targets.size(); j++) {
                            String t2 = targets.get(j);
                            str = t1 + "\t" + key + "\t" + t2 + "\n";
                            fosEdge2.write(str.getBytes());
                        }
                    }
                }
            }
            fosNode2.close();
            fosEdge2.close();



        } catch (IOException e) {
            e.printStackTrace();
        } catch (RDFParseException e) {
            e.printStackTrace();
        } catch (RDFHandlerException e) {
            e.printStackTrace();
        }
    }

    static String getLabelFromElement (String element) {
        String label = element;
        int idx = element.lastIndexOf("#");
        if (idx>-1) {
            label = element.substring(idx+1);
        }
        else {
            idx = element.lastIndexOf("/");
            if (idx > -1) {
                label = element.substring(idx + 1);
            }
        }
        return label;
    }
    static public void main (String[] args) {
            //String trigfolder = args[0];
            //String datafolder = args[1];
            //String trigfolder = "/Users/piek/Desktop/NWR-DATA/trig/entitycoref/";
            //String datafolder = "/Users/piek/Desktop/NWR-DATA/trig/dataset/";
            String trigfolder = "/Users/piek/Desktop/NWR-DATA/worldcup/events-2/other/";
            String datafolder = "/Users/piek/Desktop/NWR-DATA/worldcup/events-2/dataset/";
            ArrayList<File> files = Util.makeRecursiveFileList(new File(trigfolder), ".trig");
            String name = "worldcup";
            processTrigFiles(name, datafolder, files);

            //ArrayList<File> trigFiles = Util.makeRecursiveFileList(new File(trigfolder), ".trig");
            //processTrigFiles(datafolder, trigFiles);
            //processSeparateTrigFiles(datafolder, trigFiles);
    }
}
