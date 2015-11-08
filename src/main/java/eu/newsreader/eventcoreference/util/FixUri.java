package eu.newsreader.eventcoreference.util;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by piek on 08/11/15.
 */
public class FixUri {

    static public void main (String[] args) {
        String pathToFile = "";
        String extension = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--input") && args.length>(i+1)) {
                pathToFile = args[i+1];
            }
            else if (arg.equals("--extension") && args.length>(i+1)) {
                extension = args[i+1];
            }
        }
        pathToFile = args[0];
       // pathToFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v4_2015/test/4KJ5-2R90-TX51-F3C4.xml.1a0sdakjs.xml.trig.gz";
       File file = new File(pathToFile);
        if (file.isDirectory()) {
            ArrayList<File> files = Util.makeRecursiveFileList(file, extension);
            for (int i = 0; i < files.size(); i++) {
                File file1 = files.get(i);
                processFile(file1);
            }
        }
        else {
            processFile(file);
        }
    }

    static void processFile (File file) {
        try {
            InputStream fis = null;

            if (!file.getName().toLowerCase().endsWith(".gz")) {
                fis = new FileInputStream(file);
            }
            else {
                InputStream fileStream = new FileInputStream(file);
                fis = new GZIPInputStream(fileStream);
            }

            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine;
            String str = "";
            while (in.ready() && (inputLine = in.readLine()) != null) {
                // System.out.println(inputLine);
                inputLine = inputLine.trim();
                //            pb:A1                    <http://www.newsreader-project.eu/data//entities/SchreyerSchreyer> .
                int idx = inputLine.indexOf("/entities/");
                if (idx>-1) {
                    idx = inputLine.lastIndexOf("/");
                    int idx_e = inputLine.lastIndexOf(">");
                    String s1 = inputLine.substring(0, idx+1);
                    String s2 = inputLine.substring(idx+1, idx_e);
                    String s2new = Util.fixUri(s2);
                    if (!s2.equals(s2new)) {
                        String s3 = s1 + s2new + inputLine.substring(idx_e)+"\n";
                        str += s3;
                    }
                    else {
                        inputLine+="\n";
                        str += inputLine;
                    }
                }
                else {
                    inputLine+="\n";
                    str += inputLine;
                }
            }
            fis.close();

            OutputStream fos = null;
            if (!file.getName().toLowerCase().endsWith(".gz")) {
                fos = new FileOutputStream(file);
            }
            else {
                OutputStream fileOutStream = new FileOutputStream(file);
                fos = new GZIPOutputStream(fileOutStream);
            }
            fos.write(str.getBytes());
            fos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
