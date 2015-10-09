package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.KafFactuality;
import eu.kyotoproject.kaf.KafOpinion;
import eu.kyotoproject.kaf.KafSaxParser;
import eu.newsreader.eventcoreference.objects.*;
import eu.newsreader.eventcoreference.util.RoleLabels;
import eu.newsreader.eventcoreference.util.Util;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 11/30/13
 * Time: 7:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class GetPerspectiveStatsFromNafFile {

    static public class PerspectiveStats {
        HashMap<String, ArrayList<Integer>> valueCounts = new HashMap<String, ArrayList<Integer>>();
        ArrayList<String> names = new ArrayList<String>();

        public PerspectiveStats() {
            names = new ArrayList<String>();
            this.valueCounts = new HashMap<String, ArrayList<Integer>>();
        }

        void extendArray(ArrayList<Integer> cnts, int n) {
            if (n>cnts.size()) {
                for (int i = cnts.size(); i < n; i++) {
                    cnts.add(0);
                }
            }
        }


        public void updateStats (String name, String value) {
            String nameValue = getSimpleNameSpaceName(name);
            if (!names.contains(nameValue)) {
                names.add(nameValue);
                if (valueCounts.containsKey(value)) {
                    ArrayList<Integer> cnts = valueCounts.get(value);
                    extendArray(cnts, names.size()-1);
                    cnts.add(1);
                    valueCounts.put(value, cnts);
                }
                else {
                    ArrayList<Integer> cnts = new ArrayList<Integer>();
                    extendArray(cnts, names.size() - 1);
                    cnts.add(1);
                    valueCounts.put(value, cnts);
                }
            }
            else {
                int i = names.indexOf(nameValue);
                if (valueCounts.containsKey(value)) {
                    ArrayList<Integer> cnts = valueCounts.get(value);
                  //  System.out.println("names.toString() = " + names.toString());
                  //  System.out.println("cnts.toString() = " + cnts.toString());
                    if (i>=cnts.size()) {
                        extendArray(cnts, names.size()-1);
                        cnts.add(1);
                    }
                    else {
                        Integer cnt = cnts.get(i);
                        cnt++;
                        cnts.set(i, cnt);
                    }
                    valueCounts.put(value, cnts);
                } else {
                    ArrayList<Integer> cnts = new ArrayList<Integer>();
                    extendArray(cnts, names.size()-1);
                    cnts.add(1);
                    valueCounts.put(value, cnts);
                }
            }
        }
    }

    static public String getSimpleNameSpaceName(String name) {
        String nameValue = name;
        String nameSpace = "dbp:";
        if (name.indexOf("newsreader")>-1) {
            nameSpace = "nwr:";
        }
        int idx = name.lastIndexOf("/");
        if (idx>-1){
            nameValue = nameSpace+name.substring(idx+1);
        }
        return nameValue;
    }

    static public Vector<String> sourceVector = null;
    static public Vector<String> grammaticalVector = null;
    static public Vector<String> contextualVector = null;


    static final String USAGE = "This program processes a single NAF file and generates SEM RDF-TRiG results" +
            "The program has the following arguments:\n" +
            "--naf                  <path> <The path to the NAF file or folder with NAF files>\n" +
            "--extension            <string> <The file extension for the NAF files>\n" +
            "--project              <string> <The name of the project for creating URIs>\n" +
            "--contextual-frames    <path>   <Path to a file with the FrameNet frames considered contextual>\n" +
            "--communication-frames <path>   <Path to a file with the FrameNet frames considered source>\n" +
            "--grammatical-frames   <path>   <Path to a file with the FrameNet frames considered grammatical>"
    ;

    static PerspectiveStats sourceValues = new PerspectiveStats();
    static PerspectiveStats documentValues = new PerspectiveStats();

    static public void main(String[] args) {
        String pathToNafFile = "";
        String sourceFrameFile = "";
        String contextualFrameFile = "";
        String grammaticalFrameFile = "";
        String project = "";
        String extension = "";
        String query = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--naf") && args.length > (i + 1)) {
                pathToNafFile = args[i + 1];
            } else if (arg.equals("--extension") && args.length > (i + 1)) {
                extension = args[i + 1];
            } else if (arg.equals("--project") && args.length > (i + 1)) {
                project = args[i + 1];
            } else if (arg.equals("--query") && args.length > (i + 1)) {
                query = args[i + 1];
            } else if (arg.equals("--source-frames") && args.length > (i + 1)) {
                sourceFrameFile = args[i + 1];
            } else if (arg.equals("--grammatical-frames") && args.length > (i + 1)) {
                grammaticalFrameFile = args[i + 1];
            } else if (arg.equals("--contextual-frames") && args.length > (i + 1)) {
                contextualFrameFile = args[i + 1];
            }
        }


/*
        pathToNafFile = "/Code/vu/newsreader/EventCoreference/carheaderexample";
        project = "cars";
        extension = ".xml";
        sourceFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v4_2015/resources/source.txt";
        grammaticalFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v4_2015/resources/grammatical.txt";
        contextualFrameFile = "/Code/vu/newsreader/EventCoreference/newsreader-vm/vua-naf2sem_v4_2015/resources/contextual.txt";
*/



        sourceVector = Util.ReadFileToStringVector(sourceFrameFile);
        grammaticalVector = Util.ReadFileToStringVector(grammaticalFrameFile);
        contextualVector = Util.ReadFileToStringVector(contextualFrameFile);
        try {
            File nafFile = new File(pathToNafFile);
            OutputStream valueStats = new FileOutputStream(pathToNafFile+".values"+".xls");
            OutputStream valueSourceStats = new FileOutputStream(pathToNafFile+".source-values"+".xls");
            OutputStream valueAuthorStats = new FileOutputStream(pathToNafFile+".author-values"+".xls");
            OutputStream fosAuthor = new FileOutputStream(pathToNafFile+".author."+query+".xls");
            OutputStream fosSource = new FileOutputStream(pathToNafFile+".source."+query+".xls");
            String str = "Event\tSource\tCue\tAttribution\n";
            fosAuthor.write(str.getBytes());
            fosSource.write(str.getBytes());
            if (nafFile.isDirectory()) {
                ArrayList<File> files = Util.makeRecursiveFileList(nafFile, extension);
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                   // System.out.println("file.getName() = " + file.getName());
                    getPerspectiveStatsFromFile(fosAuthor, fosSource, file.getAbsolutePath(),project, query);
                }
            }
            else {
                getPerspectiveStatsFromFile(fosAuthor, fosSource, pathToNafFile,project, query);
            }
            fosAuthor.close();
            fosSource.close();

            HashMap<String, ArrayList<Integer>> crossCounts = new HashMap<String, ArrayList<Integer>>();
            str = "Attribution";
            for (int i = 0; i < sourceValues.names.size(); i++) {
                String name = sourceValues.names.get(i);
                str += "\t"+name;
            }
            str += "\n";
            valueSourceStats.write(str.getBytes());
            ArrayList<Integer> totals = new ArrayList<Integer>();
            for (int i = 0; i < sourceValues.names.size(); i++) {
                totals.add(0);
            }

            Set keySet = sourceValues.valueCounts.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                str = key;
                ArrayList<Integer> cnts = sourceValues.valueCounts.get(key);
                int cnt = 0;
                for (int i = 0; i < cnts.size(); i++) {
                    Integer integer = cnts.get(i);
                    Integer total = totals.get(i);
                    total+= integer;
                    totals.set(i, total);
                    str += "\t"+integer;
                    cnt+=integer;
                }
                str += "\n";
                valueSourceStats.write(str.getBytes());
                ArrayList<Integer> matrix = new ArrayList<Integer>();
                matrix.add(cnt);
                crossCounts.put(key, matrix);
            }
            str = "TOTAL";
            for (int i = 0; i < totals.size(); i++) {
                Integer integer = totals.get(i);
                str +="\t"+integer;
            }
            str += "\n";
            valueSourceStats.write(str.getBytes());

            str = "Attribution";
            for (int i = 0; i < documentValues.names.size(); i++) {
                String name = documentValues.names.get(i);
                str += "\t"+name;
            }
            str += "\n";
            valueAuthorStats.write(str.getBytes());

            totals = new ArrayList<Integer>();
            for (int i = 0; i < documentValues.names.size(); i++) {
                totals.add(0);
            }
            keySet = documentValues.valueCounts.keySet();
            keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                str = key;
                ArrayList<Integer> cnts = documentValues.valueCounts.get(key);
                int cnt = 0;
                for (int i = 0; i < cnts.size(); i++) {
                    Integer integer = cnts.get(i);
                    Integer total = totals.get(i);
                    total+= integer;
                    totals.set(i, total);
                    str += "\t"+integer;
                    cnt+=integer;
                }
                if (crossCounts.containsKey(key)) {
                    ArrayList<Integer> matrix = crossCounts.get(key);
                    matrix.add(cnt);
                    crossCounts.put(key,matrix);
                }
                else {
                    ArrayList<Integer> matrix = new ArrayList<Integer>();
                    matrix.add(0); matrix.add(cnt);
                    crossCounts.put(key, matrix);
                }
                str += "\n";
                valueAuthorStats.write(str.getBytes());
            }

            str = "TOTAL";
            for (int i = 0; i < totals.size(); i++) {
                Integer integer = totals.get(i);
                str +="\t"+integer;
            }
            str += "\n";
            valueAuthorStats.write(str.getBytes());

            totals = new ArrayList<Integer>();
            for (int i = 0; i < 2; i++) {
                totals.add(0);
            }
            str = "Attribution\tSource\tAuthor\n";
            valueStats.write(str.getBytes());
            keySet = crossCounts.keySet();
            keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                ArrayList<Integer> cnts = crossCounts.get(key);
                str = key+"\t";
                for (int i = 0; i < cnts.size(); i++) {
                    Integer integer = cnts.get(i);
                    Integer total = totals.get(i);
                    total+= integer;
                    totals.set(i, total);
                    str += "\t"+integer;
                }
                str +="\n";
                valueStats.write(str.getBytes());
            }
            str = "TOTAL";
            for (int i = 0; i < totals.size(); i++) {
                Integer integer = totals.get(i);
                str +="\t"+integer;
            }
            str += "\n";
            valueStats.write(str.getBytes());

            valueAuthorStats.close();
            valueSourceStats.close();
            valueStats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static public void getPerspectiveStatsFromFile (OutputStream fosAuthor,
                                                    OutputStream fosSource,
                                                    String pathToNafFile,
                                                    String project, String query) throws IOException {
        ArrayList<SemObject> semActors = new ArrayList<SemObject>();
        ArrayList<SemObject> semEvents = new ArrayList<SemObject>();
        ArrayList<SemTime> semTimes = new ArrayList<SemTime>();
        ArrayList<SemRelation> semRelations = new ArrayList<SemRelation>();

        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(pathToNafFile);
        if (kafSaxParser.getKafMetaData().getUrl().isEmpty()) {
            System.out.println("file.getName() = " + new File(pathToNafFile).getName());
            kafSaxParser.getKafMetaData().setUrl(new File (pathToNafFile).getName());
            System.out.println("WARNING! Replacing empty url in header NAF with the file name!");
        }

        String baseUrl = kafSaxParser.getKafMetaData().getUrl() + GetSemFromNaf.ID_SEPARATOR;
        String entityUri = ResourcesUri.nwrdata + project + "/entities/";
        if (!baseUrl.toLowerCase().startsWith("http")) {
            baseUrl = ResourcesUri.nwrdata + project + "/" + kafSaxParser.getKafMetaData().getUrl() + GetSemFromNaf.ID_SEPARATOR;
        }

        GetSemFromNaf.processNafFileForEntityCoreferenceSets(entityUri, baseUrl, kafSaxParser, semActors);
        GetSemFromNaf.processSrlForRemainingFramenetRoles(project, kafSaxParser, semActors);
        GetSemFromNaf.processNafFileForTimeInstances(baseUrl, kafSaxParser, semTimes);

        GetSemFromNaf.processNafFileForEventCoreferenceSets(baseUrl, kafSaxParser, semEvents);

        //// THIS FIX IS NEEDED BECAUSE SOMETIMES SRL GENERATES IDENTICAL SPANS FOR PREDICATES AND ACTORS. WE REMOVE EVENTS THAT ARE IDENTICAL WITH ACTORS
        Util.filterOverlapEventsEntities(semEvents, semActors);

        GetSemFromNaf.processNafFileForRelations(baseUrl,
                kafSaxParser, semEvents, semActors, semTimes, semRelations);
        ArrayList<PerspectiveObject> sourcePerspectives = GetPerspectiveRelations.getSourcePerspectives(kafSaxParser,
                project,
                semActors,
                contextualVector,
                sourceVector,
                grammaticalVector);
        ArrayList<PerspectiveObject> documentPerspectives = GetPerspectiveRelations.getAuthorPerspectives(kafSaxParser,
                project, sourcePerspectives);

    //    System.out.println("sourcePerspectives.size() = " + sourcePerspectives.size());
    //    System.out.println("documentPerspectives.size() = " + documentPerspectives.size());

        for (int i = 0; i < sourcePerspectives.size(); i++) {
            PerspectiveObject perspectiveObject = sourcePerspectives.get(i);
            updateStats(sourceValues, perspectiveObject);
            outputPerspectiveToXls(fosSource, query, semEvents, semRelations, perspectiveObject);

        }
        for (int i = 0; i < documentPerspectives.size(); i++) {
            PerspectiveObject perspectiveObject = documentPerspectives.get(i);
            String author = kafSaxParser.getKafMetaData().getAuthor();
            if (!author.isEmpty()) {
                try {
                    author = URLEncoder.encode(author, "UTF-8");
                    perspectiveObject.getSourceEntity().setId(author);
                } catch (UnsupportedEncodingException e) {
                    //  e.printStackTrace();
                }
            }
            updateStats(documentValues, perspectiveObject);
            outputPerspectiveToXls(fosAuthor, query, semEvents, semRelations, perspectiveObject);
        }
    }

    static void updateStats (PerspectiveStats stats, PerspectiveObject perspectiveObject) throws IOException {

        if ((perspectiveObject.getTargetEventMentions().size()>0)) {
            for (int i = 0; i < perspectiveObject.getTargetEventMentions().size(); i++) {
                NafMention mention = perspectiveObject.getTargetEventMentions().get(i);
                String target = mention.getPhrase();
                String cue = "";
                String sourceUri = "";

                if (!perspectiveObject.getCueMention().toString().isEmpty()) {
                    cue = perspectiveObject.getCueMention().getPhrase();
                }

                /// the mention of the target event is the subject
                if (perspectiveObject.getSourceEntity().getURI().isEmpty()) {
                    try {
                        sourceUri = URLEncoder.encode(perspectiveObject.getSource().getTokenString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        //  e.printStackTrace();
                    }
                }
                else {
                    sourceUri = perspectiveObject.getSourceEntity().getURI();
                }
                if (mention.getFactuality().size() > 0) {
                    for (int j = 0; j < mention.getFactuality().size(); j++) {
                        KafFactuality kafFactuality = mention.getFactuality().get(j);
                        String factuality = kafFactuality.getPrediction();
                        stats.updateStats(sourceUri, factuality);

                    }
                }
                if (mention.getOpinions().size()>0) {
                    for (int j = 0; j < mention.getOpinions().size(); j++) {
                        KafOpinion kafOpinion = mention.getOpinions().get(j);
                        String sentiment = kafOpinion.getOpinionSentiment().getPolarity();
                        stats.updateStats(sourceUri, sentiment);
                    }
                }
            }
        }

    }

    static void outputPerspectiveToXls (OutputStream fos, String query, ArrayList<SemObject> semEvents, ArrayList<SemRelation> semRelations, PerspectiveObject perspectiveObject) throws IOException {
        HashMap<String, ArrayList<String>> eventPerspectives = new HashMap<String, ArrayList<String>>();
        if ((perspectiveObject.getTargetEventMentions().size()>0)) {
            for (int i = 0; i < perspectiveObject.getTargetEventMentions().size(); i++) {
                NafMention mention = perspectiveObject.getTargetEventMentions().get(i);
                String roles = "";
                for (int j = 0; j < semEvents.size(); j++) {
                    SemObject semEvent = semEvents.get(j);
                    for (int k = 0; k < semEvent.getNafMentions().size(); k++) {
                        NafMention nafMention = semEvent.getNafMentions().get(k);
                        if (nafMention.sameMention(mention)) {
                            for (int l = 0; l < semRelations.size(); l++) {
                                SemRelation semRelation = semRelations.get(l);
                                String subject = semRelation.getSubject();
                                if (subject.equals(semEvent.getId())) {
                                    String propbank = "";
                                    for (int m = 0; m < semRelation.getPredicates().size(); m++) {
                                        String s = semRelation.getPredicates().get(m);
                                        if (RoleLabels.isPARTICIPANT(s)) {
                                            propbank = s;
                                            break;
                                        }
                                    }
                                    if (!propbank.isEmpty()) {
                                        String participant = "["+getSimpleNameSpaceName(semRelation.getObject())+"]";
                                        if (roles.indexOf(participant)==-1) {
                                            roles += participant;
                                        }
                                    }
/*
                                    System.out.println("semEvent.getPhrase() = " + semEvent.getPhrase());
                                    System.out.println("propbank = " + propbank);
                                    System.out.println("roles = " + roles);
*/
                                }
                            }
                        }
                    }
                }
                if (roles.isEmpty()) {
                    continue;
                }

                if (!query.isEmpty()) {
                    String [] fields = query.split(";");
                    boolean match = false;
                    for (int j = 0; j < fields.length; j++) {
                        String field = fields[j];
                        if (roles.toLowerCase().indexOf(field.toLowerCase())>-1) {
                            match = true;
                        }
                    }
                    if (!match) {
                        continue;
                    }
                }
                String target = mention.getPhrase()+"\t"+roles;
                String cue = "";
                String sourceUri = "";

                if (!perspectiveObject.getCueMention().toString().isEmpty()) {
                    cue = perspectiveObject.getCueMention().getPhrase();
                }

                /// the mention of the target event is the subject
                if (perspectiveObject.getSourceEntity().getURI().isEmpty()) {
                    try {
                        sourceUri = URLEncoder.encode(perspectiveObject.getSource().getTokenString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        //  e.printStackTrace();
                    }
                }
                else {
                    sourceUri = perspectiveObject.getSourceEntity().getURI();
                }
                String attribution = sourceUri+"\t"+cue;
                if (mention.getFactuality().size() > 0) {
                    for (int j = 0; j < mention.getFactuality().size(); j++) {
                        KafFactuality kafFactuality = mention.getFactuality().get(j);
                        String factuality = kafFactuality.getPrediction();
                        String str = attribution+"\t"+factuality;
                        //fos.write(str.getBytes());
                        if (eventPerspectives.containsKey(target)) {
                            ArrayList<String> values = eventPerspectives.get(target);
                            if (!values.contains(str)) {
                                values.add(str);
                                eventPerspectives.put(target, values);
                            }
                        }
                        else {
                            ArrayList<String> values = new ArrayList<String>();
                            values.add(str);
                            eventPerspectives.put(target, values);
                        }
                    }
                }
                if (mention.getOpinions().size()>0) {
                    for (int j = 0; j < mention.getOpinions().size(); j++) {
                        KafOpinion kafOpinion = mention.getOpinions().get(j);
                        String sentiment = kafOpinion.getOpinionSentiment().getPolarity();
                        String str = attribution+"\t"+sentiment;
                        //fos.write(str.getBytes());
                        if (eventPerspectives.containsKey(target)) {
                            ArrayList<String> values = eventPerspectives.get(target);
                            if (!values.contains(str)) {
                                values.add(str);
                                eventPerspectives.put(target, values);
                            }
                        }
                        else {
                            ArrayList<String> values = new ArrayList<String>();
                            values.add(str);
                            eventPerspectives.put(target, values);
                        }
                    }
                }
            }
            Set keySet = eventPerspectives.keySet();
            Iterator<String> keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                ArrayList<String> values = eventPerspectives.get(key);
                String str = key;
                for (int i = 0; i < values.size(); i++) {
                    String value = values.get(i);
                    str += "\t"+value;
                }
                str += "\n";
                fos.write(str.getBytes());
            }
        }

    }
}
