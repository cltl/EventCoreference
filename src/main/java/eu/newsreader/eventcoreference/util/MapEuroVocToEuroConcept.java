package eu.newsreader.eventcoreference.util;

import eu.newsreader.eventcoreference.output.SimpleTaxonomy;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 25/11/2016.
 */
public class MapEuroVocToEuroConcept {
    static EuroVoc euroVoc = new EuroVoc();


    static public void main (String[] args) {
        String euroVocFile = "";
        String hierarchyPath = "";
        hierarchyPath = "/Code/vu/newsreader/vua-resources/eurovoc_in_skos_core_concepts.rdf.gz";
        euroVocFile = "/Code/vu/newsreader/vua-resources/mapping_eurovoc_skos.csv.gz";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--eurovoc") && args.length>(i+1)) {
                euroVocFile = args[i+1];
            }
            else if (arg.equals("--skos") && args.length>(i+1)) {
                hierarchyPath = args[i+1];
            }
        }


        euroVoc.readEuroVoc(euroVocFile, "en");

        SimpleTaxonomy simpleTaxonomy = new SimpleTaxonomy();
        simpleTaxonomy.readSimpleTaxonomyFromSkosFile(hierarchyPath);

        try {
            OutputStream fos1 = new FileOutputStream(euroVocFile+".label.concept");
            OutputStream fos2 = new FileOutputStream(euroVocFile+".concept.concept");
            Set keySet = euroVoc.labelUriMap.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                String concept = euroVoc.labelUriMap.get(key);
                if (simpleTaxonomy.labelToConcept.containsKey(key)) {
                    String str = key +"\t"+"en"+"\t"+simpleTaxonomy.labelToConcept.get(key)+"\n";
                    fos1.write(str.getBytes());
                    str = concept +"\t"+simpleTaxonomy.labelToConcept.get(key)+"\n";
                    fos2.write(str.getBytes());
                }
            }
            fos1.close();
            fos2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
