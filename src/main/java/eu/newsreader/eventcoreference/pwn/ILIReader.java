package eu.newsreader.eventcoreference.pwn;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by piek on 16/09/15.
 */
public class ILIReader {
    static public HashMap<String, String> synsetToILIMap;


    public static void readILIFile (String pathToILIfile) {
        try {
            synsetToILIMap = new HashMap<String, String>();
            FileInputStream fis = new FileInputStream(pathToILIfile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader in = new BufferedReader(isr);
            String inputLine;
            String ili = "";
            String target = "";
            /*
            <i29>	a	<Concept> ;
	owl:sameAs	pwn30:eng-00006336-a ; # absorbent, absorptive
	skos:definition	"having power or capacity or tendency to absorb or soak up something (liquids or energy etc.)"@en ;
	dc:source	pwn30:eng-00006336-a .


             */

            while (in.ready()&&(inputLine = in.readLine()) != null) {
                if (!inputLine.trim().isEmpty()) {
                    // System.out.println("inputLine = " + inputLine);
                    if (inputLine.startsWith("<")) {
                        ili = "";
                        int idx = inputLine.indexOf(">");
                        if (idx>-1) {
                            ili = inputLine.substring(1, idx);
                        }
                        // System.out.println("ili = " + ili);
                    }
                    else if (inputLine.trim().indexOf("owl:sameAs")>-1) {
                        //  System.out.println("inputLine = " + inputLine);
                        String [] fields = inputLine.trim().split("\t");
                        if (fields.length>1) {
                            target = fields[1];
                            int idx = target.indexOf(":");
                            if (idx>-1) {
                                target = target.substring(idx+1);
                                int idx_e = target.indexOf(";");
                                if (idx_e>-1) {
                                    target = target.substring(0, idx_e).trim();
                                }
                            }
                            //  System.out.println("target = " + target);
                        }
                        if (!ili.isEmpty() && !target.isEmpty()) {
                            synsetToILIMap.put(target, ili);
                            target = "";
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
