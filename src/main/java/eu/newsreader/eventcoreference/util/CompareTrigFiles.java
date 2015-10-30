package eu.newsreader.eventcoreference.util;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import eu.newsreader.eventcoreference.input.TrigUtil;
import org.apache.jena.riot.RDFDataMgr;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by piek on 29/10/15.
 */
public class CompareTrigFiles {

    static public void main (String[] args) {
        String trigfolder = "/Users/piek/Desktop/NWR/contextualEvent/e-2007-03-24";
        Dataset dataset = TDBFactory.createDataset();
        HashMap<String, ArrayList<String>> eventMap = new HashMap<String, ArrayList<String>>();
        ArrayList<File> trigFiles = Util.makeRecursiveFileList(new File(trigfolder), ".trig");
        String str = "";
        for (int i = 0; i < trigFiles.size(); i++) {
            File file = trigFiles.get(i);
            str += "\t"+file.getName();
            dataset = RDFDataMgr.loadDataset(file.getAbsolutePath());
            Model namedModel = dataset.getNamedModel(TrigUtil.instanceGraph);
            StmtIterator siter = namedModel.listStatements();
            while (siter.hasNext()) {
                Statement statement = siter.nextStatement();
                if (statement.getPredicate().toString().toLowerCase().contains("#label")) {
                    String subject = statement.getSubject().getURI();
                    if (subject.indexOf("#ev") > -1) {
                        subject +=  TrigUtil.getObjectValue(statement);
                        if (eventMap.containsKey(subject)) {
                            ArrayList<String> fileNames = eventMap.get(subject);
                            if (!fileNames.contains(file.getName())) {
                                fileNames.add(file.getName());
                                eventMap.put(subject, fileNames);
                            }
                        } else {
                            ArrayList<String> fileNames = new ArrayList<String>();
                            fileNames.add(file.getName());
                            eventMap.put(subject, fileNames);
                        }
                }
            }
            }
        }
        str += "\n";
        Set keySet = eventMap.keySet();
        Iterator<String> keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            ArrayList<String> fileNames = eventMap.get(key);
            str += key;
            for (int i = 0; i < trigFiles.size(); i++) {
                File file = trigFiles.get(i);
                if (fileNames.contains(file.getName())) {
                    str += "\t"+1;
                }
                else {
                    str += "\t"+0;
                }
            }
            str += "\n";
        }
        System.out.println(str);
    }
}
