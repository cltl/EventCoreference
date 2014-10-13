package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.coref.ChainCorefSets;
import eu.newsreader.eventcoreference.coref.Scoring;
import eu.newsreader.eventcoreference.objects.CorefMatch;
import eu.newsreader.eventcoreference.objects.CorefResultSet;
import eu.newsreader.eventcoreference.util.Util;
import vu.wntools.wnsimilarity.main.WordSim;
import vu.wntools.wordnet.WordnetData;
import vu.wntools.wordnet.WordnetLmfSaxParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

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
    static final String version = "1.0";
    static WordnetData wordnetData = null;
    static String method = "leacock-chodorow";
    static int proportionalthreshold = 80;
    static double simthreshold = 1.2;

    //    pathToNafFile = Users/piek/Desktop/NWR/NWR-DATA/cars/2013-03-04;
    static public void main (String [] args) {
              if (args.length!=0) {
                  System.out.println("usage = " + usage);
              }
              else
              if (args.length==3) {
                  String pathToWNLMF = "";
                  for (int i = 0; i < args.length; i++) {
                      String arg = args[i];
                      if (arg.equals("--wn-lmf") && args.length>(i+1)) {
                          pathToWNLMF = args[i+1];
                      }
                      else if (arg.equals("--method") && args.length>(i+1)) {
                          method = args[i+1];
                      }
                      else if (arg.equals("--sim") && args.length>(i+1)) {
                          try {
                              simthreshold = Double.parseDouble(args[i + 1]);
                          } catch (NumberFormatException e) {
                              // e.printStackTrace();
                          }
                      }
                      else if (arg.equals("--proportion") && args.length>(i+1)) {
                          try {
                              proportionalthreshold = Integer.parseInt(args[i+1]);
                          } catch (NumberFormatException e) {
                              // e.printStackTrace();
                          }
                      }
                  }
                  if (!pathToWNLMF.isEmpty()) {
                      WordnetLmfSaxParser wordnetLmfSaxParser = new WordnetLmfSaxParser();
                      wordnetLmfSaxParser.parseFile(pathToWNLMF);
                      wordnetData = wordnetLmfSaxParser.wordnetData;
                      System.out.println("wordnetData = " + wordnetData.hyperRelations.size());
                      processNafStream(System.in);
                  }
              }
              else {
                  String pathToNafFile = "/Users/piek/Desktop/NWR/NWR-DATA/cars/2013-03-04/57WD-M601-F06S-P370.xml_9af408976df898b707d008cbe1f81372.naf.coref";
                  String extension = "naf.coref";
                  String folder = "/Users/piek/Desktop/NWR/NWR-DATA/cars/2013-03-04";
                  String pathToWNLMF = "/Tools/wordnet-tools.0.1/resources/wneng-30.lmf.xml";
                  for (int i = 0; i < args.length; i++) {
                      String arg = args[i];
                      if (arg.equals("--naf-file") && args.length>(i+1)) {
                          pathToNafFile = args[i+1];
                      }
                      else if (arg.equals("--naf-folder") && args.length>(i+1)) {
                          folder = args[i+1];
                      }
                      else if (arg.equals("--extension") && args.length>(i+1)) {
                          extension = args[i+1];
                      }
                      else if (arg.equals("--method") && args.length>(i+1)) {
                          method = args[i+1];
                      }
                      else if (arg.equals("--wn-lmf") && args.length>(i+1)) {
                          pathToWNLMF = args[i+1];
                      }
                      else if (arg.equals("--sim") && args.length>(i+1)) {
                          try {
                              simthreshold = Integer.parseInt(args[i+1]);
                          } catch (NumberFormatException e) {
                              // e.printStackTrace();
                          }
                      }
                      else if (arg.equals("--proportion") && args.length>(i+1)) {
                          try {
                              proportionalthreshold = Integer.parseInt(args[i+1]);
                          } catch (NumberFormatException e) {
                              // e.printStackTrace();
                          }
                      }
                  }
                  if (!pathToWNLMF.isEmpty()) {
                      WordnetLmfSaxParser wordnetLmfSaxParser = new WordnetLmfSaxParser();
                      wordnetLmfSaxParser.parseFile(pathToWNLMF);
                      wordnetData = wordnetLmfSaxParser.wordnetData;
                      System.out.println("wordnetData hyperrelations = " + wordnetData.hyperRelations.size());
                      if (!folder.isEmpty()) {
                          processNafFile(pathToNafFile);

//                      processNafFolder (new File (folder), extension);
                      }
                      else {
                          processNafFile(pathToNafFile);
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
             // kafSaxParser.writeNafToStream(System.out);
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


          static void process(KafSaxParser kafSaxParser) {
              String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
              String strEndDate = null;
              ArrayList<CorefResultSet> corefMatchList = new ArrayList<CorefResultSet>();
              for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
                  KafEvent theKafEvent = kafSaxParser.kafEventArrayList.get(i);
                  theKafEvent.addTermDataToSpans(kafSaxParser);
                  /// we need to create a mapping from every event to all other events
                  /// we create a set with the first element as the source and all the others as the scored targets
                  /// next we prune each set by normalizing the scores and applying the threshold
                  /// next we chain sets (there may be singletons)

                  /// We add the event as the first of its coreference set
                  CorefResultSet corefResultSet = new CorefResultSet(theKafEvent.getSpans());

                  /// we are going to compare it with all the others, which creates a full matrix
                  for (int j = 0; j < kafSaxParser.kafEventArrayList.size(); j++) {
                      if (j!=i) {
                          KafEvent aKafEvent = kafSaxParser.kafEventArrayList.get(j);
                          aKafEvent.addTermDataToSpans(kafSaxParser);
                          ArrayList<CorefTarget> anEventCorefTargetList = aKafEvent.getSpans();
                          /// we compare all the targets with all the other targets and keep the highest score
                          double topScore = 0;
                          String match = "";
                          for (int k = 0; k < corefResultSet.getSource().size(); k++) {
                              CorefTarget corefTarget = corefResultSet.getSource().get(k);
                              for (int l = 0; l < anEventCorefTargetList.size(); l++) {
                                  CorefTarget aCorefTarget =  anEventCorefTargetList.get(l);
                                  double score = WordSim.getWordSimLC(wordnetData, corefTarget.getLemma(), aCorefTarget.getLemma());
                                  if (score>topScore) {
                                      topScore = score;
                                      match = WordSim.match;
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
                  Scoring.normalizeCorefMatch(corefResultSet.getTargets());
                  corefResultSet.setTargets(Scoring.pruneCoref(corefResultSet.getTargets(), proportionalthreshold));
                  corefMatchList.add(corefResultSet);
                 // break;
              }

              corefMatchList = ChainCorefSets.chainResultSets(corefMatchList);
              String str ="";
              for (int j = 0; j < corefMatchList.size(); j++) {
                  str = j+":";
                  CorefResultSet resultSet = corefMatchList.get(j);
                  for (int k = 0; k < resultSet.getSource().size(); k++) {
                      CorefTarget corefTarget = resultSet.getSource().get(k);
                      str+=corefTarget.getLemma()+":"+corefTarget.getId()+";";
                  }
                  for (int k = 0; k < resultSet.getTargets().size(); k++) {
                      CorefMatch corefMatch = resultSet.getTargets().get(k);
                      str+= corefMatch.getScore() +":"+ corefMatch.getLowestCommonSubsumer()+":";
                      for (int l = 0; l < corefMatch.getCorefTargets().size(); l++) {
                          CorefTarget corefTarget = corefMatch.getCorefTargets().get(l);
                          str+= corefTarget.getLemma()+":"+corefTarget.getId()+":";
                      }
                  }
                  str+="\n";
                  System.out.println(str);
              }
              for (int i = 0; i < corefMatchList.size(); i++) {
                  CorefResultSet corefResultSet = corefMatchList.get(i);
                  KafCoreferenceSet kafCoreferenceSet = corefResultSet.castToKafCoreferenceSet();
                  String corefId = "coevent"+i;
                  kafCoreferenceSet.setCoid(corefId);
                  kafCoreferenceSet.setType("event");
                  kafSaxParser.kafCorefenceArrayList.add(kafCoreferenceSet);
              }
              strEndDate = eu.kyotoproject.util.DateUtil.createTimestamp();
              LP lp = new LP(name,version, strBeginDate, strBeginDate, strEndDate);
              kafSaxParser.getKafMetaData().addLayer(name, lp);

          }

          static void processOrg(KafSaxParser kafSaxParser, int simThreshold) {
              String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
              String strEndDate = null;
              int corefCounter = 0;
              HashMap<String, KafCoreferenceSet> kafCoreferenceSetHashMap = new HashMap<String, KafCoreferenceSet>();
              for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
                  KafEvent kafEvent = kafSaxParser.kafEventArrayList.get(i);
                  CorefTarget corefTarget = new CorefTarget();
                  KafTerm kafTerm = kafSaxParser.getTerm(kafEvent.getSpanIds().get(0));  /// first span reference
                  corefTarget.setId(kafTerm.getTid());
                  corefTarget.setTokenString(kafTerm.getLemma());
                  ArrayList<CorefTarget> corefTargetArrayList = new ArrayList<CorefTarget>();
                  corefTargetArrayList.add(corefTarget);
                  if (kafCoreferenceSetHashMap.containsKey(kafTerm.getLemma())) {
                      KafCoreferenceSet kafCoreferenceSet = kafCoreferenceSetHashMap.get(kafTerm.getLemma());
                      kafCoreferenceSet.addSetsOfSpans(corefTargetArrayList);
                      kafCoreferenceSetHashMap.put(kafTerm.getLemma(), kafCoreferenceSet);
                  }
                  else {
                      corefCounter++;
                      KafCoreferenceSet kafCoreferenceSet = new KafCoreferenceSet();
                      String corefId = "coevent"+corefCounter;
                      kafCoreferenceSet.setCoid(corefId);
                      kafCoreferenceSet.setType("event");
                      kafCoreferenceSet.addSetsOfSpans(corefTargetArrayList);
                      kafCoreferenceSetHashMap.put(kafTerm.getLemma(), kafCoreferenceSet);
                  }
              }
              Set keySet = kafCoreferenceSetHashMap.keySet();
              Iterator keys = keySet.iterator();
              while (keys.hasNext()) {
                  String key = (String) keys.next();
                  KafCoreferenceSet kafCoreferenceSet = kafCoreferenceSetHashMap.get(key);
                  kafSaxParser.kafCorefenceArrayList.add(kafCoreferenceSet);
              }
              strEndDate = eu.kyotoproject.util.DateUtil.createTimestamp();
              LP lp = new LP(name,version, strBeginDate, strBeginDate, strEndDate);
              kafSaxParser.getKafMetaData().addLayer(name, lp);

          }

}
