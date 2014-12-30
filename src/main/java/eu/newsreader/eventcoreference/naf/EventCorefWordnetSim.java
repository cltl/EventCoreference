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
            "   --wn-lmf        <path to wordnet file in lmf format\n" +
            "   --method        <one of the following methods can be used leacock-chodorow, path, wu-palmer>\n"+
            "   --sim           <similarity threshold below which no coreference is no coreference relation is determined >\n" +
            "   --relations     <synsets relations that are used for the distance measurement >\n"+
            "   --drift-max     <maximum number of lowest-common-subsumers allowed >\n"
            ;

    static final String layer = "coreferences";
    static final String name = "vua-event-coref-intradoc-wn-sim";
    static final String version = "2.1";
    static Vector<String> communicationFrame = new Vector<String>();
    static WordnetData wordnetData = null;
    static String method = "";
    static int proportionalthreshold = -1;
    static double simthreshold = -1;
    static ArrayList<String> relations = new ArrayList<String>();
    static int DRIFTMAX = -1;

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
                          } else if (arg.equals("--drift-max") && args.length > (i + 1)) {
                              try {
                                  DRIFTMAX = Integer.parseInt(args[i + 1]);
                              } catch (NumberFormatException e) {
                                  // e.printStackTrace();
                              }
                          } else if (arg.equals("--proportion") && args.length > (i + 1)) {
                              try {
                                  proportionalthreshold = Integer.parseInt(args[i + 1]);
                              } catch (NumberFormatException e) {
                                  // e.printStackTrace();
                              }
                          } else if (arg.equals("--com-frames") && args.length > (i + 1)) {
                                  String frameFilePath = args[i+1];
                                  communicationFrame =Util.ReadFileToStringVector(frameFilePath);
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
                          }
                          else if (arg.equals("--method") && args.length > (i + 1)) {
                              method = args[i + 1];
                          }
                          else if (arg.equals("--wn-lmf") && args.length > (i + 1)) {
                              pathToWNLMF = args[i + 1];
                          }
                          else if (arg.equals("--drift-max") && args.length > (i + 1)) {
                              try {
                                  DRIFTMAX = Integer.parseInt(args[i + 1]);
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
                          else if (arg.equals("--proportion") && args.length > (i + 1)) {
                              try {
                                  proportionalthreshold = Integer.parseInt(args[i + 1]);
                              } catch (NumberFormatException e) {
                                   e.printStackTrace();
                              }
                          }
                          else if (arg.equals("--com-frames") && args.length > (i + 1)) {
                              String frameFilePath = args[i+1];
                              communicationFrame =Util.ReadFileToStringVector(frameFilePath);
                          }
                      }
                      if (!pathToWNLMF.isEmpty()) {
                          WordnetLmfSaxParser wordnetLmfSaxParser = new WordnetLmfSaxParser();
                          wordnetLmfSaxParser.setRelations(relations);
                          wordnetLmfSaxParser.parseFile(pathToWNLMF);
                          wordnetData = wordnetLmfSaxParser.wordnetData;
                          System.out.println("wordnetData relations = " + wordnetData.hyperRelations.size());
                          System.out.println("simthreshold = " + simthreshold);
                          System.out.println("proportionalthreshold = " + proportionalthreshold);
                          System.out.println("method = " + method);
                          System.out.println("DRIFTMAX = " + DRIFTMAX);
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
