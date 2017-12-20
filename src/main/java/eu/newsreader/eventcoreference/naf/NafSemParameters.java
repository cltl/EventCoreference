package eu.newsreader.eventcoreference.naf;

import eu.newsreader.eventcoreference.output.JenaSerialization;
import eu.newsreader.eventcoreference.util.Util;

import java.util.Vector;

/**
 * Created by piek on 20/01/2017.
 */
public class NafSemParameters {


    static final String USAGE = "The NAF2SEM functions processes NAF files and generate SEM RDF-TRiG results" +
            "The functions use the following arguments:\n" +
            "--perspective          <(OPTIONAL) GRaSP layer is included in the output>\n" +
            "--project              <The name of the project for creating project-specific URIs within the NewsReader domain>\n" +
            "--non-entities         <(OPTIONAL) If used, additional FrameNet roles and non-entity phrases are included>\n" +
            "--contextual-frames    <Path to a file with the FrameNet frames considered contextual>\n" +
            "--communication-frames <Path to a file with the FrameNet frames considered source>\n" +
            "--grammatical-frames   <Path to a file with the FrameNet frames considered grammatical>" +
            "--time-max             <(OPTIONAL) Maximum number of time-expressions allows for an event to be included in the output. Excessive time links are problematic. The defeault value is 5" +
            "--ili                  <(OPTIONAL) Path to ILI.ttl file to convert wordnet-synsets identifiers to ILI identifiers>\n" +
            "--ili-uri              <(OPTIONAL) If used, the ILI-identifiers are used to represents events. This is necessary for cross-lingual extraction>\n" +
            "--verbose              <(OPTIONAL) representation of mentions is extended with token ids, terms ids and sentence number\n" +
            "--no-additional-role   <(OPTIONAL) only roles for entities are extracted" +
            "--max-time-span        <(OPTIONAL) set maximal nr of sentences to look for time expressions: default = 5" +
            "--max-role-phrases     <(OPTIONAL) set maximal nr of words for participant roles" +
            "--min-role-phrases     <(OPTIONAL) set minimal nr of words for participant roles" +
            "--no-nomcoref          <(OPTIONAL) nominal coreference layer is ignored\n" +
            "--no-eventcoref        <(OPTIONAL) event coreference layer is ignored\n" +
            "--no-doc-time          <(OPTIONAL) document creation time is not considered\n" +
            "--local-context        <(OPTIONAL) Default is false. Using this option sets local context to true which means that entities without external reference URI such as DBpedia are made unique per NAF input file\n" +
            "--no-context-time      <(OPTIONAL) time expressions of preceding and following sentences are not associated with events\n"+
            "--all                  <(OPTIONAL) All events are extracted, including events without time and without participants>\n" +
            "--eurovoc-en, --eurovoc-nl, --eurovoc-es, --eurovoc_es" +
            "                       <(OPTIONAL) Eurovoc resource to map topic labels to topic concept identifiers>\n"
    ;


    private Vector<String> sourceVector = null;
    private Vector<String> grammaticalVector = null;
    private Vector<String> contextualVector = null;
    private int TIMEEXPRESSIONMAX = 5;
    private boolean DOMINANTYEAR = false;
    private boolean ALL = true;
    private boolean NONENTITIES = false;
    private boolean ILIURI = false;
    private boolean VERBOSE = false;
    private boolean PERSPECTIVE = false;
    private String PROJECT = "";
    private boolean DOCTIME = true;
    private boolean CONTEXTTIME = true;
    private boolean PARAGRAPHTIME = true;
    private boolean ADDITIONALROLES = true;
    private boolean LOCALCONTEXT = false;
    private boolean NOMCOREF = true;
    private boolean EVENTCOREF = true;
    private  int SPANMATCHTHRESHOLD = 50;
    private int MAXYEAR = 0;
    private int MINYEAR = 0;
    private int SPANMAXTIME = 10;
    private int SPANMAXLOCATION= 10;
    private int SPANMINLOCATION = 2;
    private int SPANMAXPARTICIPANT = 6;
    private int SPANMINPARTICIPANT = 2;
    private int SPANMAXCOREFERENTSET = 5;

    static public void main (String[] args) {
        System.out.println("USAGE = " + USAGE);
    }
    public NafSemParameters () {
        init();
    }

    void init () {
        Vector<String> sourceVector = null;
        Vector<String> grammaticalVector = null;
        Vector<String> contextualVector = null;
        ALL = true;
        PROJECT = "";
        DOMINANTYEAR = false;
        TIMEEXPRESSIONMAX = 5;
        NONENTITIES = false;
        ILIURI = false;
        VERBOSE = false;
        PERSPECTIVE = false;
        DOCTIME = true;
        CONTEXTTIME = true;
        PARAGRAPHTIME = true;
        NOMCOREF = true;
        EVENTCOREF = true;
        ADDITIONALROLES = true;
        LOCALCONTEXT = false;
        MAXYEAR = 0;
        MINYEAR = 0;
    }

    public NafSemParameters (String [] args) {
        init();
        readParameters(args);
    }

    public int getMAXYEAR() {
        return MAXYEAR;
    }

    public void setMAXYEAR(int MAXYEAR) {
        this.MAXYEAR = MAXYEAR;
    }

    public int getMINYEAR() {
        return MINYEAR;
    }

    public void setMINYEAR(int MINYEAR) {
        this.MINYEAR = MINYEAR;
    }

    public boolean isDOMINANTYEAR() {
        return DOMINANTYEAR;
    }

    public void setDOMINANTYEAR(boolean DOMINANTYEAR) {
        this.DOMINANTYEAR = DOMINANTYEAR;
    }

    public static String getUSAGE() {
        return USAGE;
    }

    public Vector<String> getSourceVector() {
        return sourceVector;
    }

    public void setSourceVector(Vector<String> sourceVector) {
        this.sourceVector = sourceVector;
    }

    public Vector<String> getGrammaticalVector() {
        return grammaticalVector;
    }

    public void setGrammaticalVector(Vector<String> grammaticalVector) {
        this.grammaticalVector = grammaticalVector;
    }

    public Vector<String> getContextualVector() {
        return contextualVector;
    }

    public void setContextualVector(Vector<String> contextualVector) {
        this.contextualVector = contextualVector;
    }

    public int getTIMEEXPRESSIONMAX() {
        return TIMEEXPRESSIONMAX;
    }

    public void setTIMEEXPRESSIONMAX(int TIMEEXPRESSIONMAX) {
        this.TIMEEXPRESSIONMAX = TIMEEXPRESSIONMAX;
    }

    public boolean isNONENTITIES() {
        return NONENTITIES;
    }

    public void setNONENTITIES(boolean NONENTITIES) {
        this.NONENTITIES = NONENTITIES;
    }

    public boolean isILIURI() {
        return ILIURI;
    }

    public void setILIURI(boolean ILIURI) {
        this.ILIURI = ILIURI;
    }

    public boolean isVERBOSE() {
        return VERBOSE;
    }

    public void setVERBOSE(boolean VERBOSE) {
        this.VERBOSE = VERBOSE;
    }

    public boolean isPERSPECTIVE() {
        return PERSPECTIVE;
    }

    public void setPERSPECTIVE(boolean PERSPECTIVE) {
        this.PERSPECTIVE = PERSPECTIVE;
    }

    public boolean isDOCTIME() {
        return DOCTIME;
    }

    public void setDOCTIME(boolean DOCTIME) {
        this.DOCTIME = DOCTIME;
    }

    public boolean isPARAGRAPHTIME() {
        return PARAGRAPHTIME;
    }

    public void setPARAGRAPHTIME(boolean PARAGRAPHTIME) {
        this.PARAGRAPHTIME = PARAGRAPHTIME;
    }

    public boolean isCONTEXTTIME() {
        return CONTEXTTIME;
    }

    public void setCONTEXTTIME(boolean CONTEXTTIME) {
        this.CONTEXTTIME = CONTEXTTIME;
    }

    public boolean isNOMCOREF() {
        return NOMCOREF;
    }

    public void setNOMCOREF(boolean NOMCOREF) {
        this.NOMCOREF = NOMCOREF;
    }

    public boolean isEVENTCOREF() {
        return EVENTCOREF;
    }

    public void setEVENTCOREF(boolean EVENTCOREF) {
        this.EVENTCOREF = EVENTCOREF;
    }

    public String getPROJECT() {
        return PROJECT;
    }

    public void setPROJECT(String PROJECT) {
        this.PROJECT = PROJECT;
    }

    public boolean isALL() {
        return ALL;
    }

    public void setALL(boolean ALL) {
        this.ALL = ALL;
    }

    public boolean isADDITIONALROLES() {
        return ADDITIONALROLES;
    }

    public void setADDITIONALROLES(boolean ADDITIONALROLES) {
        this.ADDITIONALROLES = ADDITIONALROLES;
    }

    public boolean isLOCALCONTEXT() {
        return LOCALCONTEXT;
    }

    public void setLOCALCONTEXT(boolean LOCALCONTEXT) {
        this.LOCALCONTEXT = LOCALCONTEXT;
    }

    public int getSPANMATCHTHRESHOLD() {
        return SPANMATCHTHRESHOLD;
    }

    public void setSPANMATCHTHRESHOLD(int SPANMATCHTHRESHOLD) {
        this.SPANMATCHTHRESHOLD = SPANMATCHTHRESHOLD;
    }

    public int getSPANMAXTIME() {
        return SPANMAXTIME;
    }

    public void setSPANMAXTIME(int SPANMAXTIME) {
        this.SPANMAXTIME = SPANMAXTIME;
    }

    public int getSPANMAXLOCATION() {
        return SPANMAXLOCATION;
    }

    public void setSPANMAXLOCATION(int SPANMAXLOCATION) {
        this.SPANMAXLOCATION = SPANMAXLOCATION;
    }

    public int getSPANMINLOCATION() {
        return SPANMINLOCATION;
    }

    public void setSPANMINLOCATION(int SPANMINLOCATION) {
        this.SPANMINLOCATION = SPANMINLOCATION;
    }

    public int getSPANMAXPARTICIPANT() {
        return SPANMAXPARTICIPANT;
    }

    public void setSPANMAXPARTICIPANT(int SPANMAXPARTICIPANT) {
        this.SPANMAXPARTICIPANT = SPANMAXPARTICIPANT;
    }

    public int getSPANMINPARTICIPANT() {
        return SPANMINPARTICIPANT;
    }

    public void setSPANMINPARTICIPANT(int SPANMINPARTICIPANT) {
        this.SPANMINPARTICIPANT = SPANMINPARTICIPANT;
    }

    public int getSPANMAXCOREFERENTSET() {
        return SPANMAXCOREFERENTSET;
    }

    public void setSPANMAXCOREFERENTSET(int SPANMAXCOREFERENTSET) {
        this.SPANMAXCOREFERENTSET = SPANMAXCOREFERENTSET;
    }

    public void readParameters (String [] args) {
        String sourceFrameFile = "";
        String contextualFrameFile = "";
        String grammaticalFrameFile = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--project") && args.length > (i + 1)) {
                PROJECT = args[i + 1];
            } else if (arg.equals("--non-entities")) {
                NONENTITIES = true;
            }
            else if (arg.equals("--verbose")) {
                VERBOSE = true;
            }
            else if (arg.equals("--perspective")) {
                PERSPECTIVE = true;
            }
            else if (arg.equals("--no-doc-time")) {
                DOCTIME = false;
            }
            else if (arg.equals("--dominant-year")) {
                DOMINANTYEAR = true;
            }
            else if (arg.equals("--no-context-time")) {
                CONTEXTTIME = false;
            }
            else if (arg.equals("--no-paragraph-time")) {
                PARAGRAPHTIME = false;
            }
            else if (arg.equals("--no-nomcoref")) {
                NOMCOREF = false;
            }
            else if (arg.equals("--no-eventcoref")) {
                EVENTCOREF = false;
            }
            else if (arg.equals("--local-context")) {
                LOCALCONTEXT = true;
            }
            else if (arg.equals("--no-additional-roles")) {
                ADDITIONALROLES = false;
            }
            else if (arg.equals("--all")) {
                ALL = true;
            }
            else if (arg.equals("--eurovoc-en") && args.length > (i + 1)) {
                String pathToEurovocFile = args[i+1];
                GetSemFromNaf.initEurovoc(pathToEurovocFile, "en");
            }
            else if (arg.equals("--eurovoc-nl") && args.length > (i + 1)) {
                String pathToEurovocFile = args[i+1];
                GetSemFromNaf.initEurovoc(pathToEurovocFile, "nl");
            }
            else if (arg.equals("--eurovoc-es") && args.length > (i + 1)) {
                String pathToEurovocFile = args[i+1];
                GetSemFromNaf.initEurovoc(pathToEurovocFile, "es");
            }
            else if (arg.equals("--eurovoc-it") && args.length > (i + 1)) {
                String pathToEurovocFile = args[i+1];
                GetSemFromNaf.initEurovoc(pathToEurovocFile, "it");
            }
            else if (arg.equals("--ili") && args.length > (i + 1)) {
                String pathToILIFile = args[i+1];
                JenaSerialization.initILI(pathToILIFile);
            }
            else if (arg.equals("--ili-uri")) {
                ILIURI = true;
            }
            else if (arg.equals("--time-max") && args.length > (i + 1)) {
                try {
                    TIMEEXPRESSIONMAX = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else if (arg.equals("--min-year") && args.length > (i + 1)) {
                try {
                    MINYEAR = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else if (arg.equals("--max-year") && args.length > (i + 1)) {
                try {
                    MAXYEAR = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else if (arg.equals("--source-frames") && args.length > (i + 1)) {
                sourceFrameFile = args[i + 1];
            } else if (arg.equals("--grammatical-frames") && args.length > (i + 1)) {
                grammaticalFrameFile = args[i + 1];
            } else if (arg.equals("--contextual-frames") && args.length > (i + 1)) {
                contextualFrameFile = args[i + 1];
            }
        }
        sourceVector = Util.ReadFileToStringVector(sourceFrameFile);
        grammaticalVector = Util.ReadFileToStringVector(grammaticalFrameFile);
        contextualVector = Util.ReadFileToStringVector(contextualFrameFile);
    }

    public void readSourceVector(String sourceFrameFile) {
        sourceVector = Util.ReadFileToStringVector(sourceFrameFile);
        //System.out.println("sourceVector = " + sourceVector.size());
    }
    public void readGrammaticalVector(String grammaticalFrameFile) {
        grammaticalVector = Util.ReadFileToStringVector(grammaticalFrameFile);
    }
    public void readContextVector(String contextualFrameFile) {
        contextualVector = Util.ReadFileToStringVector(contextualFrameFile);
    }

    public void setEuroVoc (String pathToEurovocFile, String language) {
        GetSemFromNaf.initEurovoc(pathToEurovocFile, language);

    }

    public void printSettings() {
        System.out.println("sourceVector = " + sourceVector.toString());
        System.out.println("grammaticalVector = " + grammaticalVector.toString());
        System.out.println("grammaticalVector = " + grammaticalVector.toString());
        System.out.println("TIMEEXPRESSIONMAX = " + TIMEEXPRESSIONMAX);
        System.out.println("ALL = " + ALL);
        System.out.println("NONENTITIES = " + NONENTITIES);
        System.out.println("ILIURI = " + ILIURI);
        System.out.println("VERBOSE = " + VERBOSE);
        System.out.println("PERSPECTIVE = " + PERSPECTIVE);
        System.out.println("PROJECT = " + PROJECT);
        System.out.println("DOCTIME = " + DOCTIME);
        System.out.println("CONTEXTTIME = " + CONTEXTTIME);
        System.out.println("PARAGRAPHTIME = " + PARAGRAPHTIME);
        System.out.println("ADDITIONALROLES = " + ADDITIONALROLES);
        System.out.println("NOMCOREF = " + NOMCOREF);
        System.out.println("EVENTCOREF = " + EVENTCOREF);
        System.out.println("LOCALCONTEXT = " + LOCALCONTEXT);
        System.out.println("DOMINANTYEAR = " + DOMINANTYEAR);
        System.out.println("MINYEAR = " + MINYEAR);
        System.out.println("MAXYEAR = " + MAXYEAR);
    }
}
