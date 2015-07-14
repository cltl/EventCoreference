package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.objects.CorefMatch;
import eu.newsreader.eventcoreference.objects.CorefResultSet;
import eu.newsreader.eventcoreference.util.FrameTypes;
import eu.newsreader.eventcoreference.util.Util;
import vu.wntools.wnsimilarity.WordnetSimilarityApi;
import vu.wntools.wnsimilarity.measures.SimilarityPair;
import vu.wntools.wordnet.WordnetData;
import vu.wntools.wordnet.WordnetLmfSaxParser;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 10/16/13
 * Time: 11:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventCorefWordnetSim {
    static final String usage = "\nCompares predicates using one of the WN similarity function.\n"+
            "   --wn-lmf                <path to wordnet file in lmf format\n" +
            "   --method                <one of the following methods can be used leacock-chodorow, path, wu-palmer>\n"+
            "   --sim                   <similarity threshold (double, e.g. 2.5) below which no coreference is no coreference relation is determined >\n" +
            "   --sim-ont               <fall back similarity threshold (double, e.g. 1.5) if sim is below threshold but there is still an ontology match (ESO or FrameNet) below which no coreference is no coreference relation is determined. This threshold needs to be lower than sim and wordnet-lmf need to have the ESO and FrameNet ontology layer>\n" +
            "   --relations             <synsets relations that are used for the distance measurement >\n"+
            "   --drift-max             <maximum number of lowest-common-subsumers allowed >\n"+
            "   --wsd                   <all senses from WSD with proportional score above threshold (double) are used, e.g. 0.8 means all senses proportionally scoring 80% of the best scoring sense>\n" +
            "   --wn-prefix             <three letter prefix of the wordnet synset identifier in the >\n" +
            "   --wn-source             <if terms have been scored by different systems, e.g. ukb or ims, you can restrict the wsd to a system by giving the name. This will match the source field in the external reference of the term.>\n"+
            "   --source-frames         <List of framenet frames to be treates as source events. No coreference is applied>\n"+
            "   --contexual-frames      <List of framenet frames to be treates as contexual events. Coreference is applied>\n"+
            "   --grammatical-frames    <List of framenet frames to be treates as grammatical events. No coreference is applied>\n"+
            "   --replace               <Using this flag previous output of event-coreference is removed first>\n"
            ;

    static final String layer = "coreferences";
    static final String name = "vua-event-coref-intradoc-wn-sim";
    static final String version = "3.0";
    static Vector<String> sourceFrames = new Vector<String>();
    static Vector<String> grammaticalFrames = new Vector<String>();
    static Vector<String> contextualFrames = new Vector<String>();
    static WordnetData wordnetData = null;
    static String method = "";
    static int proportionalthreshold = -1;
    static double simthreshold = -1;
    static double simOntthreshold = -1;
    static ArrayList<String> relations = new ArrayList<String>();
    static int DRIFTMAX = -1;
    static boolean USEWSD = false;
    static double BESTSENSETHRESHOLD = 0.8;
    static String WNPREFIX = "";
    static String WNSOURCE = "";
    static boolean REMOVEEVENTCOREFS = false;
    static int DISTANCE = -1;

    static public void main (String [] args) {
              if (args.length == 0) {
                  System.out.println("usage = " + usage);
              }
              else {
                  boolean STREAM = true;
                  for (int i = 0; i < args.length; i++) {
                      String arg = args[i];
                      if (arg.equals("--naf-file") || arg.equals("--naf-folder")) {
                          STREAM = false;
                      }

                  }
                  if (STREAM) {
                      /// input and output stream
                      String pathToWNLMF = "";
                      for (int i = 0; i < args.length; i++) {
                          String arg = args[i];
                          if (arg.equals("--wn-lmf") && args.length > (i + 1)) {
                              pathToWNLMF = args[i + 1];
                          } else if (arg.equals("--method") && args.length > (i + 1)) {
                              method = args[i + 1];
                          } else if (arg.equals("--relations") && args.length > (i + 1)) {
                              String[] relationString = args[i + 1].split("#");
                              for (int j = 0; j < relationString.length; j++) {
                                  String s = relationString[j];
                                  relations.add(s);
                              }
                          }
                          if (arg.equals("--distance") && args.length>(i+1)) {
                              try {
                                  DISTANCE = Integer.parseInt(args[i+1]);
                              } catch (NumberFormatException e) {
                                  e.printStackTrace();
                              }
                          }
                          else if (arg.equals("--sim") && args.length > (i + 1)) {
                              try {
                                  simthreshold = Double.parseDouble(args[i + 1]);
                              } catch (NumberFormatException e) {
                                  // e.printStackTrace();
                              }
                          } else if (arg.equals("--sim-ont") && args.length > (i + 1)) {
                              try {
                                  simOntthreshold = Double.parseDouble(args[i + 1]);
                              } catch (NumberFormatException e) {
                                  // e.printStackTrace();
                              }
                          } else if (arg.equals("--drift-max") && args.length > (i + 1)) {
                              try {
                                  DRIFTMAX = Integer.parseInt(args[i + 1]);
                              } catch (NumberFormatException e) {
                                  // e.printStackTrace();
                              }
                          } else if (arg.equals("--wsd")&& args.length > (i + 1)) {
                              USEWSD = true;
                              try {
                                  BESTSENSETHRESHOLD = Double.parseDouble(args[i + 1]);
                              } catch (NumberFormatException e) {
                                  // e.printStackTrace();
                              }
                          } else if (arg.equals("--wn-prefix")&& args.length > (i + 1)) {
                              WNPREFIX = args[i+1].trim();
                          } else if (arg.equals("--wn-source")&& args.length > (i + 1)) {
                              WNSOURCE = args[i+1].trim().toLowerCase();
                          } else if (arg.equals("--proportion") && args.length > (i + 1)) {
                              try {
                                  proportionalthreshold = Integer.parseInt(args[i + 1]);
                              } catch (NumberFormatException e) {
                                  // e.printStackTrace();
                              }
                          }
                          else if (arg.equals("--source-frames") && args.length > (i + 1)) {
                                  String frameFilePath = args[i+1];
                                  sourceFrames =Util.ReadFileToStringVector(frameFilePath);
                          }
                          else if (arg.equals("--contextual-frames") && args.length > (i + 1)) {
                                  String frameFilePath = args[i+1];
                                  contextualFrames =Util.ReadFileToStringVector(frameFilePath);
                          }
                          else if (arg.equals("--grammatical-frames") && args.length > (i + 1)) {
                                  String frameFilePath = args[i+1];
                                  grammaticalFrames =Util.ReadFileToStringVector(frameFilePath);
                          }
                          else if (arg.equalsIgnoreCase("--replace")) {
                              REMOVEEVENTCOREFS = true;
                          }
                      }
                      if (!pathToWNLMF.isEmpty()) {
                          WordnetLmfSaxParser wordnetLmfSaxParser = new WordnetLmfSaxParser();
                          wordnetLmfSaxParser.setRelations(relations);
                          wordnetLmfSaxParser.parseFile(pathToWNLMF);
                          wordnetData = wordnetLmfSaxParser.wordnetData;
                          //System.out.println("wordnetData = " + wordnetData.hyperRelations.size());
                          //processNafStream(System.in);
                          KafSaxParser kafSaxParser = new KafSaxParser();
                          kafSaxParser.parseFile(System.in);
                          processNaf(kafSaxParser);
                          kafSaxParser.writeNafToStream(System.out);

                      }
                  } else {
                      String pathToNafFile = "";
                      String pathToWNLMF = "";
                      String extension = "";
                      String folder = "";
                      //String pathToNafFile = "/Users/piek/Desktop/NWR/en_pipeline2.0/v21_out-no-event-coref.naf";
                      // String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-DATA/cars/2013-03-04/57WD-M601-F06S-P370.xml_9af408976df898b707d008cbe1f81372.naf.coref";
                      // String folder = "/Users/piek/Desktop/NWR/NWR-DATA/cars/2013-03-04";
                      // String pathToWNLMF = "/Tools/wordnet-tools.0.1/resources/wneng-30.lmf.xml";
                      for (int i = 0; i < args.length; i++) {
                          String arg = args[i];
                          if (arg.equals("--naf-file") && args.length > (i + 1)) {
                              pathToNafFile = args[i + 1];
                          }
                          else if (arg.equals("--naf-folder") && args.length > (i + 1)) {
                              folder = args[i + 1];
                          }
                          else if (arg.equals("--extension") && args.length > (i + 1)) {
                              extension = args[i + 1];
                          }
                          else if (arg.equals("--relations") && args.length > (i + 1)) {
                              String[] relationString = args[i + 1].split("#");
                              for (int j = 0; j < relationString.length; j++) {
                                  String s = relationString[j];
                                  relations.add(s);
                              }
                          } else if (arg.equals("--wsd") && args.length > (i + 1)) {
                              USEWSD = true;
                              try {
                                  BESTSENSETHRESHOLD = Double.parseDouble(args[i + 1]);
                              } catch (NumberFormatException e) {
                                  // e.printStackTrace();
                              }
                          }
                          else if (arg.equals("--method") && args.length > (i + 1)) {
                              method = args[i + 1];
                          }
                          else if (arg.equals("--wn-lmf") && args.length > (i + 1)) {
                              pathToWNLMF = args[i + 1];
                          } else if (arg.equals("--wn-prefix")&& args.length > (i + 1)) {
                              WNPREFIX = args[i+1].trim();
                          } else if (arg.equals("--wn-source")&& args.length > (i + 1)) {
                              WNSOURCE = args[i+1].trim().toLowerCase();
                          }
                          else if (arg.equals("--drift-max") && args.length > (i + 1)) {
                              try {
                                  DRIFTMAX = Integer.parseInt(args[i + 1]);
                              } catch (NumberFormatException e) {
                                   e.printStackTrace();
                              }
                          }
                          if (arg.equals("--distance") && args.length>(i+1)) {
                              try {
                                  DISTANCE = Integer.parseInt(args[i+1]);
                              } catch (NumberFormatException e) {
                                  e.printStackTrace();
                              }
                          }
                          else if (arg.equals("--sim") && args.length > (i + 1)) {
                              try {
                                  simthreshold = Double.parseDouble(args[i + 1]);
                              } catch (NumberFormatException e) {
                                   e.printStackTrace();
                              }
                          }
                          else if (arg.equals("--sim-ont") && args.length > (i + 1)) {
                              try {
                                  simOntthreshold = Double.parseDouble(args[i + 1]);
                              } catch (NumberFormatException e) {
                                   e.printStackTrace();
                              }
                          }
                          else if (arg.equals("--proportion") && args.length > (i + 1)) {
                              try {
                                  proportionalthreshold = Integer.parseInt(args[i + 1]);
                              } catch (NumberFormatException e) {
                                   e.printStackTrace();
                              }
                          }
                          else if (arg.equals("--source-frames") && args.length > (i + 1)) {
                              String frameFilePath = args[i+1];
                              sourceFrames =Util.ReadFileToStringVector(frameFilePath);
                          }
                          else if (arg.equals("--contextual-frames") && args.length > (i + 1)) {
                              String frameFilePath = args[i+1];
                              contextualFrames =Util.ReadFileToStringVector(frameFilePath);
                          }
                          else if (arg.equals("--grammatical-frames") && args.length > (i + 1)) {
                              String frameFilePath = args[i+1];
                              grammaticalFrames =Util.ReadFileToStringVector(frameFilePath);
                          }
                          else if (arg.equalsIgnoreCase("--replace")) {
                              REMOVEEVENTCOREFS = true;
                          }
                      }
                      if (!pathToWNLMF.isEmpty()) {
                          WordnetLmfSaxParser wordnetLmfSaxParser = new WordnetLmfSaxParser();
                          wordnetLmfSaxParser.setRelations(relations);
                          wordnetLmfSaxParser.parseFile(pathToWNLMF);
                          wordnetData = wordnetLmfSaxParser.wordnetData;
                          System.out.println("wordnetData relations = " + wordnetData.hyperRelations.size());
                          System.out.println("simthreshold = " + simthreshold);
                          System.out.println("simthreshold-ont = " + simOntthreshold);
                          System.out.println("best-sense-threshold = " + BESTSENSETHRESHOLD);
                          System.out.println("proportionalthreshold = " + proportionalthreshold);
                          System.out.println("method = " + method);
                          System.out.println("wn-prefix = " + WNPREFIX);
                          System.out.println("wn-resource = " + WNSOURCE);
                          System.out.println("DRIFTMAX = " + DRIFTMAX);
                          System.out.println("DISTANCE = " + DISTANCE);
                          System.out.println("relations = " + relations.toString());
                          if (!folder.isEmpty()) {
                              processNafFolder(new File(folder), extension);
                          } else {
                              processNafFile(pathToNafFile);
                          }
                      }
                  }
              }
          }



          static public void processNaf (KafSaxParser kafSaxParser) {
              if (REMOVEEVENTCOREFS) {
                  Util.removeEventCoreferences(kafSaxParser);
              }
              process(kafSaxParser, USEWSD);
          }

          static public void processNafStream (InputStream nafStream) {
              KafSaxParser kafSaxParser = new KafSaxParser();
              kafSaxParser.parseFile(nafStream);
              if (REMOVEEVENTCOREFS) {
                  Util.removeEventCoreferences(kafSaxParser);
              }
              process(kafSaxParser, USEWSD);
              kafSaxParser.writeNafToStream(System.out);
          }

          static public void processNafFile (String pathToNafFile) {
              KafSaxParser kafSaxParser = new KafSaxParser();
              kafSaxParser.parseFile(pathToNafFile);
              if (REMOVEEVENTCOREFS) {
                  Util.removeEventCoreferences(kafSaxParser);
              }
              process(kafSaxParser, USEWSD);
              try {
                  OutputStream fos = new FileOutputStream(pathToNafFile+".event-coref.naf");
                  kafSaxParser.writeNafToStream(fos);
                  fos.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }

          static public void processNafFolder (File pathToNafFolder, String extension) {

              ArrayList<File> files = Util.makeRecursiveFileList(pathToNafFolder, extension);
              for (int i = 0; i < files.size(); i++) {
                  File file = files.get(i);
                 // System.out.println("file.getName() = " + file.getName());
                  KafSaxParser kafSaxParser = new KafSaxParser();
                  kafSaxParser.parseFile(file);
                  if (REMOVEEVENTCOREFS) {
                      Util.removeEventCoreferences(kafSaxParser);
                  }

                  process(kafSaxParser, USEWSD);

                  try {
                      FileOutputStream fos = new FileOutputStream(file.getAbsolutePath()+".coref");
                      kafSaxParser.writeNafToStream(fos);
                      fos.close();
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
              }

          }

          static String getLemmaFromKafEvent(KafEvent kafEvent) {
              String lemma = "";
              for (int j = 0; j < kafEvent.getSpans().size(); j++) {
                  CorefTarget corefTarget = kafEvent.getSpans().get(j);
                  lemma += corefTarget.getLemma()+" ";
              }
              return lemma.trim();
          }


      static void processNEW (KafSaxParser kafSaxParser, boolean USEWSD) {
              String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
              String strEndDate = null;
              ArrayList<CorefResultSet> corefMatchList = new ArrayList<CorefResultSet>();
              ArrayList<CorefResultSet> nocorefMatchList = new ArrayList<CorefResultSet>();


              /// we create coreference sets for lemmas
              ArrayList<String> lemmaList = new ArrayList<String>();
              for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
                  KafEvent theKafEvent = kafSaxParser.kafEventArrayList.get(i);
                  if (!Util.validPosEvent(theKafEvent, kafSaxParser)) {
                     continue; //// we only accept predicates with valid POS
                  }
                  Integer sentenceId = -1;
                  try {
                      KafTerm kafTerm = kafSaxParser.getTerm(theKafEvent.getSpanIds().get(0)); /// get the term from the first span
                      sentenceId = Integer.parseInt(kafSaxParser.getSentenceId(kafTerm));
                  } catch (NumberFormatException e) {
                      e.printStackTrace();
                  }
                  theKafEvent.addTermDataToSpans(kafSaxParser); /// this adds lemma, pos and sentence identifiers to the CorefTargets of the event
                  String eventType = FrameTypes.getEventTypeString(theKafEvent.getExternalReferences(), contextualFrames, sourceFrames, grammaticalFrames);
                  String lemma = getLemmaFromKafEvent(theKafEvent);
                  if (DISTANCE >-1) {
                          //// we get the last lemma resul set and check the sentencId
                          //// we iterate over the list in reverse order
                          boolean MATCH = false;
                          for (int j = corefMatchList.size()-1; j>0;j--) {
                              CorefResultSet corefResultSet = corefMatchList.get(j);
                              if (corefResultSet.getSourceLemma().equals(lemma)) {
                                  CorefTarget lastCorefTarget = corefResultSet.getLastSource();
                                  try {
                                      Integer lastSentenceId = Integer.parseInt(lastCorefTarget.getSentenceId());
                                      if (sentenceId-lastSentenceId<=DISTANCE) {
                                          MATCH = true;
                                          ArrayList<CorefTarget> anEventCorefTargetList = theKafEvent.getSpans(); /// getSpans returns CorefTargets with all data
                                          corefResultSet.addSource(anEventCorefTargetList);
                                          break;
                                      }
                                  } catch (NumberFormatException e) {
                                      e.printStackTrace();
                                  }
                              }
                          }
                          if (!MATCH) {
                              ////  lemmas are not considered for coreference if their distance is too far apart
                              CorefResultSet corefResultSet = new CorefResultSet(lemma, theKafEvent.getSpans()); /// getSpans returns CorefTargets with all data
                              corefMatchList.add(corefResultSet);
                          }
                  }
                  else {
                      /// IF DISTANCE DOES NOT COUNT WE LUMP ALL LEMMA REFERENCES
                      boolean MATCH = false;
                      for (int j = corefMatchList.size()-1; j>0;j--) {
                          CorefResultSet corefResultSet = corefMatchList.get(j);
                          if (corefResultSet.getSourceLemma().equals(lemma)) {
                              MATCH = true;
                              //// we add the spans to the lemme set
                              ArrayList<CorefTarget> anEventCorefTargetList = theKafEvent.getSpans();
                              corefResultSet.addSource(anEventCorefTargetList);
                              break;
                          }
                      }
                      if (!MATCH) {
                          ////  lemmas are not considered for coreference if their distance is too far apart
                          CorefResultSet corefResultSet = new CorefResultSet(lemma, theKafEvent.getSpans());
                          corefMatchList.add(corefResultSet);
                      }
                  }
              }

              //// now we need to compare these lemma lists
              for (int i = 0; i < corefMatchList.size(); i++) {
                  CorefResultSet corefResultSet = corefMatchList.get(i);
                  if (corefResultSet!=null) {
                      if (!USEWSD) {
                          corefResultSet.getAllSenses(kafSaxParser, WNSOURCE);
                      }
                      /// we determine the best senses for the lemma sets according to WSD

                      corefResultSet.getBestSensesAfterCumulation(kafSaxParser, BESTSENSETHRESHOLD, WNSOURCE);
                      /// now we check all the other candidate sets to see if any can be merged
                      for (int j = i + 1; j < corefMatchList.size(); j++) {
                          CorefResultSet aCorefResultSet = corefMatchList.get(j);
                          if (aCorefResultSet==null) {
                              continue;
                          }
                          if (!withinDistance(corefResultSet, aCorefResultSet)) {
                              continue;
                          }

                          CorefMatch corefMatch = null;
                          //SimilarityPair similarityPair = getSimScoreForCorefSets(corefResultSet, aCorefResultSet);
                          SimilarityPair similarityPair = getSimScoreForCorefSetsIgnoreOntology(corefResultSet, aCorefResultSet);
                          if (similarityPair!=null && similarityPair.getScore()>-1) {

                              if (similarityPair.getScore() > simthreshold) {
                                  /// merge the sets
                                  corefMatch = new CorefMatch();
                                  corefMatch.setTargetLemma(aCorefResultSet.getSourceLemma());
                                  corefMatch.setScore(similarityPair.getScore());
                                  corefMatch.setLowestCommonSubsumer(similarityPair.getMatch());
                                  corefMatch.setCorefTargets(aCorefResultSet.getSources());
                              }
                          }
                          else {
                              similarityPair = getSimScoreForCorefSets(corefResultSet, aCorefResultSet);
                              if (similarityPair!=null) {
                                  if ((similarityPair.getMatch().indexOf("fn:") > -1) || (similarityPair.getMatch().indexOf("eso:") > -1)) {
                                      if (similarityPair.getScore() > simOntthreshold) {
                                          corefMatch = new CorefMatch();
                                          corefMatch.setTargetLemma(aCorefResultSet.getSourceLemma());
                                          corefMatch.setScore(similarityPair.getScore());
                                          corefMatch.setLowestCommonSubsumer(similarityPair.getMatch());
                                          corefMatch.setCorefTargets(aCorefResultSet.getSources());
                                      }
                                  }
                              }
                          }


                          // if we have a match, we add it as a target and we set the merged set to null since we do not need to handle it anymore
                          if (corefMatch!=null) {
                           //   System.out.println(similarityPair.getMatch()+" = " + similarityPair.getScore() + ":" + simthreshold + ":" + SynsetSim.match);

                              corefResultSet.addTarget(corefMatch);
                              ///// we empty this set now
                              //aCorefResultSet = null;
                              corefMatchList.set(j, null);
                          }
                      }
                    //  Scoring.normalizeCorefMatch(corefResultSet.getTargets());
                    //  corefResultSet.setTargets(Scoring.pruneCoref(corefResultSet.getTargets(), proportionalthreshold));
                  }
              }
              /// chaining is only necessary for the non-greedy version
              /// corefMatchList = ChainCorefSets.chainResultSets(corefMatchList);

              for (int i = 0; i < corefMatchList.size(); i++) {
                  CorefResultSet corefResultSet = corefMatchList.get(i);
                  if (corefResultSet==null) {
                      continue;
                  }
                  KafCoreferenceSet kafCoreferenceSet = corefResultSet.castToKafCoreferenceSet(wordnetData.getResource());
                  String corefId = "coevent" + (kafSaxParser.kafCorefenceArrayList.size() + 1);
                  kafCoreferenceSet.setCoid(corefId);
                  kafCoreferenceSet.setType("event");
                  kafSaxParser.kafCorefenceArrayList.add(kafCoreferenceSet);
              }

              if (DRIFTMAX>-1) {
                  fixEventCoreferenceSets(kafSaxParser);
              }
              strEndDate = eu.kyotoproject.util.DateUtil.createTimestamp();
              String host = "";
              try {
                  host = InetAddress.getLocalHost().getHostName();
              } catch (UnknownHostException e) {
                  e.printStackTrace();
              }
              LP lp = new LP(name,version, strBeginDate, strBeginDate, strEndDate, host);
              kafSaxParser.getKafMetaData().addLayer(layer, lp);

      }


      static boolean withinDistance(CorefResultSet corefResultSet1, CorefResultSet corefResultSet2) {
          if (DISTANCE==-1) {
              return true;
          }
          for (int i = 0; i < corefResultSet1.getSources().size(); i++) {
              ArrayList<CorefTarget> corefTargets1 = corefResultSet1.getSources().get(i);
              for (int j = 0; j < corefTargets1.size(); j++) {
                  CorefTarget corefTarget1 = corefTargets1.get(j);
                  Integer sentenceNr1 = Integer.parseInt(corefTarget1.getSentenceId());
                  for (int k = 0; k < corefResultSet2.getSources().size(); k++) {
                      ArrayList<CorefTarget> corefTargets2 = corefResultSet2.getSources().get(k);
                      for (int l = 0; l < corefTargets2.size(); l++) {
                          CorefTarget corefTarget2 = corefTargets2.get(l);
                          Integer sentenceNr2 = Integer.parseInt(corefTarget2.getSentenceId());
                          if (Math.abs(sentenceNr1-sentenceNr2)<=DISTANCE) {
/*
                              System.out.println("corefResultSet1 = " + corefResultSet1.getSourceLemma());
                              System.out.println("corefResultSet2 = " + corefResultSet2.getSourceLemma());
                              System.out.println("sentenceNr1 +\":\"+ sentenceNr2 = " + sentenceNr1 + ":" + sentenceNr2);
*/
                              return true;
                          }
                      }
                  }
              }
          }
          return false;
      }

        static void process(KafSaxParser kafSaxParser, boolean USEWSD) {
              String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
              String strEndDate = null;
              ArrayList<CorefResultSet> corefMatchList = new ArrayList<CorefResultSet>();
              ArrayList<CorefResultSet> grammaticalCorefMatchList = new ArrayList<CorefResultSet>();
              ArrayList<CorefResultSet> sourceCorefMatchList = new ArrayList<CorefResultSet>();


              /// we create coreference sets for lemmas
              ArrayList<String> lemmaList = new ArrayList<String>();
              for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
                  KafEvent theKafEvent = kafSaxParser.kafEventArrayList.get(i);
                  if (!Util.validPosEvent(theKafEvent, kafSaxParser)) {
                     continue; //// we only accept predicates with valid POS
                  }
                  Integer sentenceId = -1;
                  try {
                      KafTerm kafTerm = kafSaxParser.getTerm(theKafEvent.getSpanIds().get(0)); /// get the term from the first span
                      sentenceId = Integer.parseInt(kafSaxParser.getSentenceId(kafTerm));
                  } catch (NumberFormatException e) {
                      e.printStackTrace();
                  }
                  theKafEvent.addTermDataToSpans(kafSaxParser);
                  String lemma = getLemmaFromKafEvent(theKafEvent);
                  String eventType = FrameTypes.getEventTypeString(theKafEvent.getExternalReferences(), contextualFrames, sourceFrames, grammaticalFrames);
                  if (eventType.equals(FrameTypes.GRAMMATICAL)) {
                      //// grammatical events are not considered for coreference on just the lemma or wnsim
                      CorefResultSet corefResultSet = new CorefResultSet(lemma, theKafEvent.getSpans());
                      grammaticalCorefMatchList.add(corefResultSet);
                  }
                  else if (eventType.equals(FrameTypes.SOURCE)) {
                      if (sourceCorefMatchList.isEmpty())   {
                          CorefResultSet corefResultSet = new CorefResultSet(lemma, theKafEvent.getSpans());
                          sourceCorefMatchList.add(corefResultSet);
                      }
                      else if (DISTANCE ==-1) {
                          CorefResultSet corefResultSet = new CorefResultSet(lemma, theKafEvent.getSpans());
                          sourceCorefMatchList.add(corefResultSet);
                      }
                      else {
                          //// we get the last lemma resul set and check the sentencId
                          //// we iterate over the list in reverse order
                          boolean MATCH = false;
                          for (int j = sourceCorefMatchList.size()-1; j>0;j--) {
                              CorefResultSet corefResultSet = sourceCorefMatchList.get(j);
                              if (corefResultSet.getSourceLemma().equals(lemma)) {
                                  CorefTarget lastCorefTarget = corefResultSet.getLastSource();
                                  try {
                                      Integer lastSentenceId = Integer.parseInt(lastCorefTarget.getSentenceId());
                                      if (sentenceId-lastSentenceId<= DISTANCE) {
                                          MATCH = true;
                                          ArrayList<CorefTarget> anEventCorefTargetList = theKafEvent.getSpans();
                                          corefResultSet.addSource(anEventCorefTargetList);
                                          break;
                                      }
                                  } catch (NumberFormatException e) {
                                      e.printStackTrace();
                                  }
                              }
                          }
                          if (!MATCH) {
                              //// source events are not considered for coreference on just the lemma or wnsim if their distance is too far apart
                              CorefResultSet corefResultSet = new CorefResultSet(lemma, theKafEvent.getSpans());
                              sourceCorefMatchList.add(corefResultSet);
                          }
                      }
                  }
                  else {
                      //// for all event lemmas that are not grammatical and not source or that are source but within sentence boundary
                      if (lemmaList.contains(lemma)) {
                          continue;
                      }
                      lemmaList.add(lemma);
                      CorefResultSet corefResultSet = new CorefResultSet(lemma, theKafEvent.getSpans());
                      for (int j = i + 1; j < kafSaxParser.kafEventArrayList.size(); j++) {
                          KafEvent aKafEvent = kafSaxParser.kafEventArrayList.get(j);
                          aKafEvent.addTermDataToSpans(kafSaxParser);
                          String aLemma = getLemmaFromKafEvent(aKafEvent);
                          if (lemma.equals(aLemma)) {
                              ArrayList<CorefTarget> anEventCorefTargetList = aKafEvent.getSpans();
                              corefResultSet.addSource(anEventCorefTargetList);
                          }
                      }

                      if (!USEWSD) {
                          //// ALL SENSE ARE USED TO CREATE SIMILARITY MAPPINGS
                         corefResultSet.getAllSenses(kafSaxParser, WNSOURCE);
                      }
                      else {
                          //// NOW WE BUILD THE COREFSETS AND CONSIDER THE SENSES OF ALL THE TARGETS (sources) TO TAKE THE HIGHEST SCORING ONES
                          corefResultSet.getBestSensesAfterCumulation(kafSaxParser, BESTSENSETHRESHOLD, WNSOURCE);
                      }

                      corefMatchList.add(corefResultSet);
                  }
              }

              //// now we need to compare these lemma lists but not the source and grammatical sets
              for (int i = 0; i < corefMatchList.size(); i++) {
                  CorefResultSet corefResultSet = corefMatchList.get(i);
                  if (corefResultSet!=null) {
                      /// now we check all the other candidate sets to see if any can be merged
                      for (int j = i + 1; j < corefMatchList.size(); j++) {
                          CorefResultSet aCorefResultSet = corefMatchList.get(j);
                          if (aCorefResultSet==null) {
                              continue;
                          }
                          /// first try the source lemma
                          CorefMatch corefMatch = null;
                          //SimilarityPair similarityPair = getSimScoreForCorefSets(corefResultSet, aCorefResultSet);
                          SimilarityPair similarityPair = getSimScoreForCorefSetsIgnoreOntology(corefResultSet, aCorefResultSet);
                          if (similarityPair!=null && similarityPair.getScore()>-1) {

                              if (similarityPair.getScore() > simthreshold) {
                                  /// merge the sets
                                  corefMatch = new CorefMatch();
                                  corefMatch.setTargetLemma(aCorefResultSet.getSourceLemma());
                                  corefMatch.setScore(similarityPair.getScore());
                                  corefMatch.setLowestCommonSubsumer(similarityPair.getMatch());
                                  corefMatch.setCorefTargets(aCorefResultSet.getSources());
                              }
                          }
                          else {
                              //// if there is no sim match at all we try the ontology layer with a lower threshold
                              similarityPair = getSimScoreForCorefSets(corefResultSet, aCorefResultSet);
                              if (similarityPair!=null) {
                                  if ((similarityPair.getMatch().indexOf("fn:") > -1) || (similarityPair.getMatch().indexOf("eso:") > -1)) {
                                      if (similarityPair.getScore() > simOntthreshold) {
                                          corefMatch = new CorefMatch();
                                          corefMatch.setTargetLemma(aCorefResultSet.getSourceLemma());
                                          corefMatch.setScore(similarityPair.getScore());
                                          corefMatch.setLowestCommonSubsumer(similarityPair.getMatch());
                                          corefMatch.setCorefTargets(aCorefResultSet.getSources());
                                      }
                                  }
                              }
                          }


                          // if we have a match, we add it as a target and we set the merged set to null since we do not need to handle it anymore
                          if (corefMatch!=null) {
                           //   System.out.println(similarityPair.getMatch()+" = " + similarityPair.getScore() + ":" + simthreshold + ":" + SynsetSim.match);

                              corefResultSet.addTarget(corefMatch);
                              ///// we empty this set now
                              //aCorefResultSet = null;
                              corefMatchList.set(j, null);
                          }
                      }
                    //  Scoring.normalizeCorefMatch(corefResultSet.getTargets());
                    //  corefResultSet.setTargets(Scoring.pruneCoref(corefResultSet.getTargets(), proportionalthreshold));
                  }
              }
              /// chaining is only necessary for the non-greedy version
              /// corefMatchList = ChainCorefSets.chainResultSets(corefMatchList);

              for (int i = 0; i < corefMatchList.size(); i++) {
                  CorefResultSet corefResultSet = corefMatchList.get(i);
                  if (corefResultSet==null) {
                      continue;
                  }
                  KafCoreferenceSet kafCoreferenceSet = corefResultSet.castToKafCoreferenceSet(wordnetData.getResource());
                  String corefId = "coevent" + (kafSaxParser.kafCorefenceArrayList.size() + 1);
                  kafCoreferenceSet.setCoid(corefId);
                  kafCoreferenceSet.setType("event");
                  kafSaxParser.kafCorefenceArrayList.add(kafCoreferenceSet);
              }

              for (int i = 0; i < grammaticalCorefMatchList.size(); i++) {
                  CorefResultSet corefResultSet = grammaticalCorefMatchList.get(i);
                  if (corefResultSet==null) {
                      continue;
                  }
                  KafCoreferenceSet kafCoreferenceSet = corefResultSet.castToKafCoreferenceSet(wordnetData.getResource());
                  String corefId = "coevent" + (kafSaxParser.kafCorefenceArrayList.size() + 1);
                  kafCoreferenceSet.setCoid(corefId);
                  kafCoreferenceSet.setType("event");
                  kafSaxParser.kafCorefenceArrayList.add(kafCoreferenceSet);
              }
              for (int i = 0; i < sourceCorefMatchList.size(); i++) {
                  CorefResultSet corefResultSet = sourceCorefMatchList.get(i);
                  if (corefResultSet==null) {
                      continue;
                  }
                  KafCoreferenceSet kafCoreferenceSet = corefResultSet.castToKafCoreferenceSet(wordnetData.getResource());
                  String corefId = "coevent" + (kafSaxParser.kafCorefenceArrayList.size() + 1);
                  kafCoreferenceSet.setCoid(corefId);
                  kafCoreferenceSet.setType("event");
                  kafSaxParser.kafCorefenceArrayList.add(kafCoreferenceSet);
              }
              if (DRIFTMAX>-1) {
                  fixEventCoreferenceSets(kafSaxParser);
              }
              strEndDate = eu.kyotoproject.util.DateUtil.createTimestamp();
              String host = "";
              try {
                  host = InetAddress.getLocalHost().getHostName();
              } catch (UnknownHostException e) {
                  e.printStackTrace();
              }
              LP lp = new LP(name,version, strBeginDate, strBeginDate, strEndDate, host);
              kafSaxParser.getKafMetaData().addLayer(layer, lp);

      }

    static SimilarityPair getSimScoreForCorefSetsIgnoreOntology(CorefResultSet corefResultSet1, CorefResultSet corefResultSet2) {
        SimilarityPair topPair = null;
        for (int i = 0; i < corefResultSet1.getBestSenses().size(); i++) {
            KafSense kafSense1 = corefResultSet1.getBestSenses().get(i);
            for (int j = 0; j < corefResultSet2.getBestSenses().size(); j++) {
                KafSense kafSense2 = corefResultSet2.getBestSenses().get(j);
                String sense1 = kafSense1.getSensecode();
                String sense2 = kafSense2.getSensecode();
                if (!WNPREFIX.isEmpty()) {
                    int idx = sense1.indexOf("-");
                    if (idx>-1) {
                        sense1 = WNPREFIX+sense1.substring(idx);
                    }
                    idx = sense2.indexOf("-");
                    if (idx>-1) {
                        sense2 = WNPREFIX + sense2.substring(idx);
                    }
                }
                // System.out.println("sense1 = " + sense1);
                // System.out.println("sense2 = " + sense2);
                SimilarityPair pair = WordnetSimilarityApi.synsetLeacockChodorowSimilarityIgnoreOntology(wordnetData, sense1, sense2);
                if (topPair==null)  {
                    /// this is the first result
                    topPair = pair;
                }
                else if (pair.getScore()>topPair.getScore()) {
                    // this is a better result
                    topPair = pair;
                }
/*
                System.out.println("score = " + topPair.getScore());
                System.out.println("topPair.getMatch() = " + topPair.getMatch());
*/
            }
        }
        return topPair;
    }

    static SimilarityPair getSimScoreForCorefSets(CorefResultSet corefResultSet1, CorefResultSet corefResultSet2) {
        SimilarityPair topPair = null;
        for (int i = 0; i < corefResultSet1.getBestSenses().size(); i++) {
            KafSense kafSense1 = corefResultSet1.getBestSenses().get(i);
            for (int j = 0; j < corefResultSet2.getBestSenses().size(); j++) {
                KafSense kafSense2 = corefResultSet2.getBestSenses().get(j);
                String sense1 = kafSense1.getSensecode();
                String sense2 = kafSense2.getSensecode();
                if (!WNPREFIX.isEmpty()) {
                    int idx = sense1.indexOf("-");
                    if (idx>-1) {
                        sense1 = WNPREFIX+sense1.substring(idx);
                    }
                    idx = sense2.indexOf("-");
                    if (idx>-1) {
                        sense2 = WNPREFIX + sense2.substring(idx);
                    }
                }
                // System.out.println("sense1 = " + sense1);
                // System.out.println("sense2 = " + sense2);
                SimilarityPair pair = WordnetSimilarityApi.synsetLeacockChodorowSimilarity(wordnetData, sense1, sense2);
                if (topPair==null)  {
                    /// this is the first result
                    topPair = pair;
                }
                else if (pair.getScore()>topPair.getScore()) {
                    /// this is a better result
                    topPair = pair;
                }
/*
                System.out.println("score = " + topPair.getScore());
                System.out.println("topPair.getMatch() = " + topPair.getMatch());
*/
            }
        }
        return topPair;
    }

    static void fixEventCoreferenceSets (KafSaxParser kafSaxParser) {
            ArrayList<KafCoreferenceSet> fixedSets = new ArrayList<KafCoreferenceSet>();
            for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
                KafCoreferenceSet kafCoreferenceSet = kafSaxParser.kafCorefenceArrayList.get(i);
                if (kafCoreferenceSet.getType().toLowerCase().startsWith("event")) {
                    if (kafCoreferenceSet.getExternalReferences().size()>DRIFTMAX) {
                        HashMap<String, KafCoreferenceSet> corefMap = new HashMap<String, KafCoreferenceSet>();
                        int nSubSets = 0;
                        for (int j = 0; j < kafCoreferenceSet.getSetsOfSpans().size(); j++) {
                            ArrayList<CorefTarget> corefTargets = kafCoreferenceSet.getSetsOfSpans().get(j);
                            String lemma = "";
                            for (int k = 0; k < corefTargets.size(); k++) {
                                CorefTarget corefTarget = corefTargets.get(k);
                                KafTerm kafTerm = kafSaxParser.getTerm(corefTarget.getId());
                                if (kafTerm!=null) {
                                    lemma += kafTerm.getLemma()+" ";
                                }
                            }
                            lemma = lemma.trim();
                            if (corefMap.containsKey(lemma)) {
                                KafCoreferenceSet kafCoreferenceSetNew = corefMap.get(lemma);
                                kafCoreferenceSetNew.addSetsOfSpans(corefTargets);
                                corefMap.put(lemma, kafCoreferenceSetNew);
                            }
                            else {
                                nSubSets++;
                                KafCoreferenceSet kafCoreferenceSetNew = new KafCoreferenceSet();
                                String corefId = kafCoreferenceSet.getCoid()+"_"+nSubSets;
                                kafCoreferenceSetNew.setCoid(corefId);
                                kafCoreferenceSetNew.setType(kafCoreferenceSet.getType());
                                kafCoreferenceSetNew.addSetsOfSpans(corefTargets);
                                corefMap.put(lemma, kafCoreferenceSetNew);
                            }
                        }
                        Set keySet = corefMap.keySet();
                        Iterator<String> keys = keySet.iterator();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            KafCoreferenceSet kafCoreferenceSetNew = corefMap.get(key);
                            fixedSets.add(kafCoreferenceSetNew);
                        }
                    }
                    else {
                        fixedSets.add(kafCoreferenceSet);
                    }
                }
                else {
                    fixedSets.add(kafCoreferenceSet);
                }
            }
            kafSaxParser.kafCorefenceArrayList = fixedSets;
        }
}
