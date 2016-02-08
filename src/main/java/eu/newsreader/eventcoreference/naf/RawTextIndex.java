package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafSaxParser;
import eu.newsreader.eventcoreference.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by piek on 01/02/16.
 */
public class RawTextIndex {


    static public void main(String[] args) {

        String pathToNafFolder = "";
        String extension = "";
        String project = "";

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--naf-folder") && args.length > (i + 1)) {
                pathToNafFolder = args[i + 1];
            }
            else if (arg.equals("--extension") && args.length > (i + 1)) {
                extension = args[i + 1];
            }
            else if (arg.equals("--project") && args.length > (i + 1)) {
                project = args[i + 1];
            }
        }
        String rawTextIndexFilePath = pathToNafFolder+"/"+"rawtext.idx";

        ArrayList<File> files = Util.makeRecursiveFileList(new File(pathToNafFolder), extension);
        for (int i = 0; i < files.size(); i++) {
            String pathToNafFile = files.get(i).getAbsolutePath();
            KafSaxParser kafSaxParser = new KafSaxParser();
            kafSaxParser.parseFile(pathToNafFile);
            if (kafSaxParser.getKafMetaData().getUrl().isEmpty()) {
                System.out.println("file.getName() = " + new File(pathToNafFile).getName());
                kafSaxParser.getKafMetaData().setUrl(new File(pathToNafFile).getName());
                System.out.println("WARNING! Replacing empty url in header NAF with the file name!");
            }
            try {
                addRawText(rawTextIndexFilePath, kafSaxParser, project);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    static void addRawText (String rawTextIndexFilePath, KafSaxParser kafSaxParser, String project) throws IOException {

        String rawText = kafSaxParser.rawText;
        // System.out.println("rawText = " + rawText);
        rawText = rawText.replaceAll("\n", "_");
        rawText = rawText.replaceAll("\"", "^");

        String uri = kafSaxParser.getKafMetaData().getUrl();
        if (!uri.toLowerCase().startsWith("http")) {
            //  System.out.println("uri = " + uri);
            uri = ResourcesUri.nwrdata + project + "/" + uri.substring(uri.indexOf("/")+1);
        }

        String str = uri+"\t"+rawText+"\n";
        File rawTextIndexFile = new File(rawTextIndexFilePath);
        OutputStream os = null;
        if (rawTextIndexFile!=null && rawTextIndexFile.exists()) {
            os = new FileOutputStream(rawTextIndexFile, true);
        }
        else if (rawTextIndexFile!=null) {
            os = new FileOutputStream(rawTextIndexFile);
        }
        try {
            os.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        os.flush();
        os.close();
    }

}
