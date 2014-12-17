package eu.newsreader.eventcoreference.util;

import eu.kyotoproject.kaf.KafCoreferenceSet;
import eu.kyotoproject.kaf.KafSaxParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by piek on 12/17/14.
 */
public class RemoveEventCoreferences {

    static public void main (String [] args) {
        if (args.length==0) {
            processNafStream(System.in);
        }
        else {
            String pathToNafFile = args[0];
            String extension = "";
            String folder = "";
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals("--naf-file") && args.length>(i+1)) {
                    pathToNafFile = args[i+1];
                }
                else if (arg.equals("--naf-folder") && args.length>(i+1)) {
                    folder = args[i+1];
                }
                if (arg.equals("--extension") && args.length>(i+1)) {
                    extension = args[i+1];
                }
            }
            if (!folder.isEmpty()) {
                processNafFolder (new File(folder), extension);
            }
            else {
                processNafFile(pathToNafFile);
            }
        }
    }

    static public void processNafStream (InputStream nafStream) {
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(nafStream);
        process(kafSaxParser);
        kafSaxParser.writeNafToStream(System.out);
    }

    static public void processNafFile (String pathToNafFile) {
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(pathToNafFile);
        process(kafSaxParser);
        kafSaxParser.writeNafToStream(System.out);
    }

    static public void processNafFolder (File pathToNafFolder, String extension) {
        ArrayList<File> files = Util.makeFlatFileList(pathToNafFolder, extension);
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            KafSaxParser kafSaxParser = new KafSaxParser();
            kafSaxParser.parseFile(file);
            process(kafSaxParser);
            try {
                FileOutputStream fos = new FileOutputStream(file.getAbsolutePath()+".nocoref");
                kafSaxParser.writeNafToStream(fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void process(KafSaxParser kafSaxParser) {
        ArrayList<KafCoreferenceSet> newSet = new ArrayList<KafCoreferenceSet>();
        for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
            KafCoreferenceSet kafCoreferenceSet = kafSaxParser.kafCorefenceArrayList.get(i);
            if (!kafCoreferenceSet.getType().toLowerCase().startsWith("event")) {
               newSet.add(kafCoreferenceSet);
            }
        }
        kafSaxParser.kafCorefenceArrayList = newSet;
    }

}
