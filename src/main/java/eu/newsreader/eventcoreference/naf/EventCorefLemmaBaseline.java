package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.objects.CorefResultSet;
import eu.newsreader.eventcoreference.util.FrameTypes;
import eu.newsreader.eventcoreference.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    public class EventCorefLemmaBaseline {
    static final String layer = "coreferences";
    static final String name = "vua-event-coref-intradoc-lemma-baseline";
    static final String version = "2.0";
    static String outputTag = ".coref";
    static Vector<String> sourceFrames = new Vector<String>();
    static Vector<String> grammaticalFrames = new Vector<String>();
    static Vector<String> contextualFrames = new Vector<String>();
    static int SOURCEDISTANCE = -1;
    static boolean REMOVEEVENTCOREFS = false;
    static boolean REMOVEFALSEPREDICATES = false;



    static public void main (String [] args) {
              if (args.length==0) {
                  processNafStream(System.in);
              }
              else {
                  String pathToNafFile = args[0];
                  String extension = "";
                  String folder = "";
                  for (int i = 0; i < args.length; i++) {
                      String arg = args[i];
                      if (arg.equals("--naf-file") && args.length>(i+1)) {
                          pathToNafFile = args[i+1];
                      }
                      else if (arg.equals("--naf-folder") && args.length>(i+1)) {
                          folder = args[i+1];
                      }
                      if (arg.equals("--extension") && args.length>(i+1)) {
                          extension = args[i+1];
                      }
                      else if (arg.equals("--output-tag") && args.length>(i+1)) {
                          outputTag = args[i+1];
                      }
                      if (arg.equals("--distance") && args.length>(i+1)) {
                          try {
                              SOURCEDISTANCE = Integer.parseInt(args[i+1]);
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

                      } else if (arg.equalsIgnoreCase("--ignore-false")) {
                          REMOVEFALSEPREDICATES = true;
                      }

                  }
                  if (!folder.isEmpty()) {
                      processNafFolder (new File (folder), extension);
                  }
                  else {
                      processNafFile(pathToNafFile);
                  }
              }
          }

          static public void processNafStream (InputStream nafStream) {
              KafSaxParser kafSaxParser = new KafSaxParser();
              kafSaxParser.parseFile(nafStream);
              if (REMOVEEVENTCOREFS) {
                  Util.removeEventCoreferences(kafSaxParser);
              }
              if (REMOVEFALSEPREDICATES) {
                  Util.removeFalsePredicatesSrl(kafSaxParser);
              }
              process(kafSaxParser);
              kafSaxParser.writeNafToStream(System.out);
          }

          static public void processNafFile (String pathToNafFile) {
              KafSaxParser kafSaxParser = new KafSaxParser();
              kafSaxParser.parseFile(pathToNafFile);
              if (REMOVEEVENTCOREFS) {
                  Util.removeEventCoreferences(kafSaxParser);
              }
              if (REMOVEFALSEPREDICATES) {
                  Util.removeFalsePredicatesSrl(kafSaxParser);
              }
              process(kafSaxParser);
              kafSaxParser.writeNafToStream(System.out);
          }

          static public void processNafFolder (File pathToNafFolder, String extension) {
              ArrayList<File> files = Util.makeRecursiveFileList(pathToNafFolder, extension);
              for (int i = 0; i < files.size(); i++) {
                  File file = files.get(i);
                  KafSaxParser kafSaxParser = new KafSaxParser();
                  kafSaxParser.parseFile(file);
                  if (REMOVEEVENTCOREFS) {
                      Util.removeEventCoreferences(kafSaxParser);
                  }
                  if (REMOVEFALSEPREDICATES) {
                      Util.removeFalsePredicatesSrl(kafSaxParser);
                  }
                  if (SOURCEDISTANCE ==-1) {
                      process(kafSaxParser);
                  }
                  else {
                      processWithFrames(kafSaxParser);
                  }
                  try {
                      String filePath = file.getAbsolutePath().substring(0,file.getAbsolutePath().lastIndexOf("."));
                      FileOutputStream fos = new FileOutputStream(filePath+outputTag);
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

              int corefCounter = 0;
              HashMap<String, KafCoreferenceSet> kafCoreferenceSetHashMap = new HashMap<String, KafCoreferenceSet>();
              for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
                  KafEvent kafEvent = kafSaxParser.kafEventArrayList.get(i);
                  kafEvent.addTermDataToSpans(kafSaxParser);
                  KafTerm kafTerm = kafSaxParser.getTerm(kafEvent.getSpanIds().get(0));  /// first span reference
                  CorefTarget corefTarget = new CorefTarget();
                  corefTarget.setId(kafTerm.getTid());
                  corefTarget.setTokenString(kafTerm.getLemma());
                  ArrayList<CorefTarget> corefTargetArrayList = new ArrayList<CorefTarget>();
                  corefTargetArrayList.add(corefTarget);
                  if (kafCoreferenceSetHashMap.containsKey(kafTerm.getLemma())) {
                      KafCoreferenceSet kafCoreferenceSet = kafCoreferenceSetHashMap.get(kafTerm.getLemma());
                      kafCoreferenceSet.addSetsOfSpans(corefTargetArrayList);
                      kafCoreferenceSetHashMap.put(kafTerm.getLemma(), kafCoreferenceSet);
                  } else {
                      corefCounter++;
                      KafCoreferenceSet kafCoreferenceSet = new KafCoreferenceSet();
                      String corefId = "coevent" + corefCounter;
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
              String host = "";
              try {
                  host = InetAddress.getLocalHost().getHostName();
              } catch (UnknownHostException e) {
                  e.printStackTrace();
              }
              LP lp = new LP(name,version, strBeginDate, strBeginDate, strEndDate, host);
              kafSaxParser.getKafMetaData().addLayer(layer, lp);

          }

          static void processWithFrames(KafSaxParser kafSaxParser) {
              String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
              String strEndDate = null;

              ArrayList<CorefResultSet> grammaticalCorefMatchList = new ArrayList<CorefResultSet>();
              ArrayList<CorefResultSet> sourceCorefMatchList = new ArrayList<CorefResultSet>();

              int corefCounter = 0;
              HashMap<String, KafCoreferenceSet> kafCoreferenceSetHashMap = new HashMap<String, KafCoreferenceSet>();
              for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
                  KafEvent kafEvent = kafSaxParser.kafEventArrayList.get(i);
                  kafEvent.addTermDataToSpans(kafSaxParser);
                  KafTerm kafTerm = kafSaxParser.getTerm(kafEvent.getSpanIds().get(0));  /// first span reference
                  if (!Util.validPosEvent(kafEvent, kafSaxParser)) {
                      continue; //// we only accept predicates with valid POS
                  }
                  Integer sentenceId = -1;
                  try {
                      sentenceId = Integer.parseInt(kafSaxParser.getSentenceId(kafTerm));
                  } catch (NumberFormatException e) {
                      e.printStackTrace();
                  }
                  String lemma = kafTerm.getLemma();
                  String eventType = FrameTypes.getEventTypeString(kafEvent.getExternalReferences(), contextualFrames, sourceFrames, grammaticalFrames);
                  if (eventType.equals(FrameTypes.GRAMMATICAL)) {
                      //// grammatical events are not considered for coreference on just the lemma or wnsim
                      CorefResultSet corefResultSet = new CorefResultSet(lemma, kafEvent.getSpans());
                      grammaticalCorefMatchList.add(corefResultSet);
                  }
                  else if (eventType.equals(FrameTypes.SOURCE)) {
                      if (sourceCorefMatchList.isEmpty())   {
                          CorefResultSet corefResultSet = new CorefResultSet(lemma, kafEvent.getSpans());
                          sourceCorefMatchList.add(corefResultSet);
                      }
                      else if (SOURCEDISTANCE==-1) {
                          CorefResultSet corefResultSet = new CorefResultSet(lemma, kafEvent.getSpans());
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
                                      if (sentenceId-lastSentenceId>SOURCEDISTANCE) {
                                          MATCH = true;
                                          ArrayList<CorefTarget> anEventCorefTargetList = kafEvent.getSpans();
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
                              CorefResultSet corefResultSet = new CorefResultSet(lemma, kafEvent.getSpans());
                              sourceCorefMatchList.add(corefResultSet);
                          }
                      }
                  }
                  else {
                      CorefTarget corefTarget = new CorefTarget();
                      corefTarget.setId(kafTerm.getTid());
                      corefTarget.setTokenString(kafTerm.getLemma());
                      ArrayList<CorefTarget> corefTargetArrayList = new ArrayList<CorefTarget>();
                      corefTargetArrayList.add(corefTarget);
                      if (kafCoreferenceSetHashMap.containsKey(kafTerm.getLemma())) {
                          KafCoreferenceSet kafCoreferenceSet = kafCoreferenceSetHashMap.get(kafTerm.getLemma());
                          kafCoreferenceSet.addSetsOfSpans(corefTargetArrayList);
                          kafCoreferenceSetHashMap.put(kafTerm.getLemma(), kafCoreferenceSet);
                      } else {
                          corefCounter++;
                          KafCoreferenceSet kafCoreferenceSet = new KafCoreferenceSet();
                          String corefId = "coevent" + corefCounter;
                          kafCoreferenceSet.setCoid(corefId);
                          kafCoreferenceSet.setType("event");
                          kafCoreferenceSet.addSetsOfSpans(corefTargetArrayList);
                          kafCoreferenceSetHashMap.put(kafTerm.getLemma(), kafCoreferenceSet);
                      }
                  }
              }
              Set keySet = kafCoreferenceSetHashMap.keySet();
              Iterator keys = keySet.iterator();
              while (keys.hasNext()) {
                  String key = (String) keys.next();
                  KafCoreferenceSet kafCoreferenceSet = kafCoreferenceSetHashMap.get(key);
                  kafSaxParser.kafCorefenceArrayList.add(kafCoreferenceSet);
              }
              for (int i = 0; i < grammaticalCorefMatchList.size(); i++) {
                  CorefResultSet corefResultSet = grammaticalCorefMatchList.get(i);
                  if (corefResultSet==null) {
                      continue;
                  }
                  KafCoreferenceSet kafCoreferenceSet = corefResultSet.castToKafCoreferenceSet();
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
                  KafCoreferenceSet kafCoreferenceSet = corefResultSet.castToKafCoreferenceSet();
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


          static void fixEventCoreferenceSets (KafSaxParser kafSaxParser) {
            ArrayList<KafCoreferenceSet> fixedSets = new ArrayList<KafCoreferenceSet>();
            for (int i = 0; i < kafSaxParser.kafCorefenceArrayList.size(); i++) {
                KafCoreferenceSet kafCoreferenceSet = kafSaxParser.kafCorefenceArrayList.get(i);
                if (kafCoreferenceSet.getType().toLowerCase().startsWith("event")) {
                    if (kafCoreferenceSet.getExternalReferences().size()>3) {
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
