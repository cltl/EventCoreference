package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.objects.CorefMatch;
import eu.newsreader.eventcoreference.objects.CorefResultSet;
import eu.newsreader.eventcoreference.util.Util;
import vu.wntools.wnsimilarity.main.WordSim;
import vu.wntools.wordnet.WordnetData;
import vu.wntools.wordnet.WordnetLmfSaxParser;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: kyoto
 * Date: 10/16/13
 * Time: 11:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventCorefWordnetSim {
    static final String usage = "\nCompares predicates using one of the WN similarity function.\n"+
            "   --wn-lmf        <path to wordnet file in lmf format\n" +
            "   --method        <one of the following methods can be used leacock-chodorow, path, wu-palmer>\n"+
            "   --sim           <similarity threshold below which no coreference is no coreference relation is determined >\n";
//    "   --subsumers     <path to a file with subsumer frequencies, required for resnik, lin, jiang-conrath or all>\n"+
//    "   --method        <one of the following methods can be used leacock-chodorow, resnik, path, wu-palmer, jiang-conrath, lin>\n"+

    static final String layer = "coreferences";
    static final String name = "vua-event-coref-intradoc-wn-sim";
    static final String version = "2.0";
    static WordnetData wordnetData = null;
    static String method = "leacock-chodorow";
    static int proportionalthreshold = 80;
    static double simthreshold = 2.0;
    static ArrayList<String> relations = new ArrayList<String>();

    static public void main (String [] args) {
              if (args.length==0) {
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
                          } else if (arg.equals("--sim") && args.length > (i + 1)) {
                              try {
                                  simthreshold = Double.parseDouble(args[i + 1]);
                              } catch (NumberFormatException e) {
                                  // e.printStackTrace();
                              }
                          } else if (arg.equals("--proportion") && args.length > (i + 1)) {
                              try {
                                  proportionalthreshold = Integer.parseInt(args[i + 1]);
                              } catch (NumberFormatException e) {
                                  // e.printStackTrace();
                              }
                          }
                      }
                      if (!pathToWNLMF.isEmpty()) {
                          WordnetLmfSaxParser wordnetLmfSaxParser = new WordnetLmfSaxParser();
                          wordnetLmfSaxParser.setRelations(relations);
                          wordnetLmfSaxParser.parseFile(pathToWNLMF);
                          wordnetData = wordnetLmfSaxParser.wordnetData;
                          //System.out.println("wordnetData = " + wordnetData.hyperRelations.size());
                          processNafStream(System.in);
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
                          } else if (arg.equals("--naf-folder") && args.length > (i + 1)) {
                              folder = args[i + 1];
                          } else if (arg.equals("--extension") && args.length > (i + 1)) {
                              extension = args[i + 1];
                          } else if (arg.equals("--relations") && args.length > (i + 1)) {
                              String[] relationString = args[i + 1].split(";");
                              for (int j = 0; j < relationString.length; j++) {
                                  String s = relationString[j];
                                  relations.add(s);
                              }
                          } else if (arg.equals("--method") && args.length > (i + 1)) {
                              method = args[i + 1];
                          } else if (arg.equals("--wn-lmf") && args.length > (i + 1)) {
                              pathToWNLMF = args[i + 1];
                          } else if (arg.equals("--sim") && args.length > (i + 1)) {
                              try {
                                  simthreshold = Integer.parseInt(args[i + 1]);
                              } catch (NumberFormatException e) {
                                  // e.printStackTrace();
                              }
                          } else if (arg.equals("--proportion") && args.length > (i + 1)) {
                              try {
                                  proportionalthreshold = Integer.parseInt(args[i + 1]);
                              } catch (NumberFormatException e) {
                                  // e.printStackTrace();
                              }
                          }
                      }
                      if (!pathToWNLMF.isEmpty()) {
                          WordnetLmfSaxParser wordnetLmfSaxParser = new WordnetLmfSaxParser();
                          wordnetLmfSaxParser.setRelations(relations);
                          wordnetLmfSaxParser.parseFile(pathToWNLMF);
                          wordnetData = wordnetLmfSaxParser.wordnetData;
                          //System.out.println("wordnetData hyperrelations = " + wordnetData.hyperRelations.size());
                          if (!folder.isEmpty()) {
                              processNafFolder(new File(folder), extension);
                          } else {
                              processNafFile(pathToNafFile);
                          }
                      }
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
              try {
                  OutputStream fos = new FileOutputStream(pathToNafFile+".event-coref.naf");
                  kafSaxParser.writeNafToStream(fos);
                  fos.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }

          static public void processNafFolder (File pathToNafFolder, String extension) {
              ArrayList<File> files = Util.makeFlatFileList(pathToNafFolder, extension);
              for (int i = 0; i < files.size(); i++) {
                  File file = files.get(i);
                  KafSaxParser kafSaxParser = new KafSaxParser();
                  kafSaxParser.parseFile(file);
                  process(kafSaxParser);
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

          static void process(KafSaxParser kafSaxParser) {
              String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
              String strEndDate = null;
              ArrayList<CorefResultSet> corefMatchList = new ArrayList<CorefResultSet>();

              /// we create coreference sets for lemmas
              ArrayList<String> lemmaList = new ArrayList<String>();

              for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
                  KafEvent theKafEvent = kafSaxParser.kafEventArrayList.get(i);
                  theKafEvent.addTermDataToSpans(kafSaxParser);
                  String lemma = getLemmaFromKafEvent(theKafEvent);
                  if (lemmaList.contains(lemma)) {
                      continue;
                  }
                  lemmaList.add(lemma);
                  CorefResultSet corefResultSet = new CorefResultSet(lemma, theKafEvent.getSpans());
                  for (int j = i+1; j < kafSaxParser.kafEventArrayList.size(); j++) {
                          KafEvent aKafEvent = kafSaxParser.kafEventArrayList.get(j);
                          aKafEvent.addTermDataToSpans(kafSaxParser);
                          String aLemma = getLemmaFromKafEvent(aKafEvent);
                          if (lemma.equals(aLemma)) {
                              ArrayList<CorefTarget> anEventCorefTargetList = aKafEvent.getSpans();
                              corefResultSet.addSource(anEventCorefTargetList);
                          }
                  }
                  corefMatchList.add(corefResultSet);
              }

              //// now we need to compare these lemma lists
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
                          double topScore = 0;
                          double score = WordSim.getWordSimLC(wordnetData, corefResultSet.getSourceLemma(),
                                  aCorefResultSet.getSourceLemma());
                          if (score > simthreshold) {
                              topScore = score;
                              /// merge the sets
                              corefMatch = new CorefMatch();
                              corefMatch.setTargetLemma(aCorefResultSet.getSourceLemma());
                              corefMatch.setScore(score);
                              corefMatch.setLowestCommonSubsumer(WordSim.match);
                              corefMatch.setCorefTargets(aCorefResultSet.getSources());
                          }

                          /// next try previous matches; greedy version
                          /// we do not need to chain any more
                          /// if the previous matches provide a better score, we take that one.
                          /// so if the source did result in match, the targets can still generate a match and of the targets provide a better match we take that one
                          for (int k = 0; k < corefResultSet.getTargets().size(); k++) {
                              CorefMatch previousCorefMatch = corefResultSet.getTargets().get(k);
                              score = WordSim.getWordSimLC(wordnetData, previousCorefMatch.getTargetLemma(),
                                      aCorefResultSet.getSourceLemma());
                              if ((score > topScore) && (score > simthreshold)) {
                                  /// merge the sets
                                  corefMatch = new CorefMatch();
                                  corefMatch.setTargetLemma(aCorefResultSet.getSourceLemma());
                                  corefMatch.setScore(score);
                                  corefMatch.setLowestCommonSubsumer(WordSim.match);
                                  corefMatch.setCorefTargets(aCorefResultSet.getSources());
                              }
                          }

                          // if we have a match, we add it as a target and we set the merged set to null since we do not need to handle it anymore
                          if (corefMatch!=null) {
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

/*          static void processOrg(KafSaxParser kafSaxParser) {
              boolean DEBUG = false;
              String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
              String strEndDate = null;
              ArrayList<CorefResultSet> corefMatchList = new ArrayList<CorefResultSet>();
              for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
                  KafEvent theKafEvent = kafSaxParser.kafEventArrayList.get(i);
                  theKafEvent.addTermDataToSpans(kafSaxParser);
                  String lemma = getLemmaFromKafEvent(theKafEvent);
                  DEBUG = false;
                  for (int j = 0; j < theKafEvent.getSpans().size(); j++) {
                      CorefTarget corefTarget = theKafEvent.getSpans().get(j);
                      if (corefTarget.getLemma().equals("honor") ||
                          corefTarget.getLemma().equals("celebrate"))  {
                          DEBUG = true;
                      }
                  }
                  if (!DEBUG) {
                      continue;
                  }
                  /// we need to create a mapping from every event to all other events
                  /// we create a set with the first element as the source and all the others as the scored targets
                  /// next we prune each set by normalizing the scores and applying the threshold
                  /// next we chain sets (there may be singletons)

                  /// We add the event as the first of its coreference set
                  CorefResultSet corefResultSet = new CorefResultSet(lemma, theKafEvent.getSpans());

                  /// we are going to compare it with all the others, which creates a full matrix
                  for (int j = 0; j < kafSaxParser.kafEventArrayList.size(); j++) {
                      if (j!=i) {
                          KafEvent aKafEvent = kafSaxParser.kafEventArrayList.get(j);
                          aKafEvent.addTermDataToSpans(kafSaxParser);
                          ArrayList<CorefTarget> anEventCorefTargetList = aKafEvent.getSpans();
                          /// we compare all the targets with all the other targets and keep the highest score
                          double topScore = 0;
                          String match = "";
                          for (int k = 0; k < corefResultSet.getSources().size(); k++) {
                              CorefTarget corefTarget = corefResultSet.getSources().get(k);
                              for (int l = 0; l < anEventCorefTargetList.size(); l++) {
                                  CorefTarget aCorefTarget =  anEventCorefTargetList.get(l);
                                  double score =  WordSim.getWordSimLC(wordnetData, corefTarget.getLemma(), aCorefTarget.getLemma());
                                  if (aCorefTarget.getLemma().equals("honor") || aCorefTarget.getLemma().equals("celebrate")) {
                                      System.out.println("corefTarget.getLemma(), aCorefTarget.getLemma( = " + corefTarget.getLemma() + ":" + aCorefTarget.getLemma());
                                      System.out.println("score = " + score);
                                      System.out.println("WordSim.match = " + WordSim.match);
                                  }
                                  if (score>topScore) {
                                      topScore = score;
                                      match = WordSim.match;
                                  }
                                  else {
                                      if (corefTarget.getLemma().equals(aCorefTarget.getLemma())) {
                                          /// this is a lemma match without a wordnet match so we force a top score
                                          topScore = simTopScore;
                                          match = corefTarget.getLemma();
                                      }
                                  }
                              }
                          }
                          if (topScore>simthreshold) {
                              CorefMatch corefMatch = new CorefMatch();
                              corefMatch.setScore(topScore);
                              corefMatch.setLowestCommonSubsumer(match);
                              corefMatch.setCorefTargets(anEventCorefTargetList);
                              corefResultSet.addTarget(corefMatch);
                          }
                      }
                  }
                  for (int j = 0; j < corefResultSet.getSources().size(); j++) {
                      CorefTarget corefTarget = corefResultSet.getSources().get(j);
                      System.out.println("source.getLemma() = " + corefTarget.getLemma());
                  }
                  for (int j = 0; j < corefResultSet.getTargets().size(); j++) {
                      CorefMatch corefMatch = corefResultSet.getTargets().get(j);
                      System.out.println("corefMatch.getLowestCommonSubsumer() = " + corefMatch.getLowestCommonSubsumer());
                      System.out.println("corefMatch.getScore() = " + corefMatch.getScore());
                      for (int k = 0; k < corefMatch.getCorefTargets().size(); k++) {
                          CorefTarget corefTarget = corefMatch.getCorefTargets().get(k);
                          System.out.println("corefTarget.getLemma() = " + corefTarget.getLemma());
                      }
                  }
                  Scoring.normalizeCorefMatch(corefResultSet.getTargets());
                  corefResultSet.setTargets(Scoring.pruneCoref(corefResultSet.getTargets(), proportionalthreshold));
                  corefMatchList.add(corefResultSet);
                 // break;
              }

              //corefMatchList = ChainCorefSets.chainResultSets(corefMatchList);
              for (int i = 0; i < corefMatchList.size(); i++) {
                  CorefResultSet corefResultSet = corefMatchList.get(i);
                  KafCoreferenceSet kafCoreferenceSet = corefResultSet.castToKafCoreferenceSet(wordnetData);
                  String corefId = "coevent" + (kafSaxParser.kafCorefenceArrayList.size() + 1);
                  kafCoreferenceSet.setCoid(corefId);
                  kafCoreferenceSet.setType("event");
                  kafSaxParser.kafCorefenceArrayList.add(kafCoreferenceSet);
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

          }*/

}
