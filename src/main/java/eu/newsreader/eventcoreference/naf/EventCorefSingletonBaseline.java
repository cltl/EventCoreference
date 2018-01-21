package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.objects.CorefResultSet;
import eu.newsreader.eventcoreference.util.Util;
import vu.wntools.wordnet.WordnetData;
import vu.wntools.wordnet.WordnetLmfSaxParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class EventCorefSingletonBaseline {
    static final String layer = "coreferences";
    static final String name = "vua-event-coref-intradoc-singleton-baseline";
    static final String version = "1.0";
    static boolean REMOVEEVENTCOREFS = false;
    static String outputTag = ".coref";
    static boolean REMOVEFALSEPREDICATES = false;
    static WordnetData wordnetData = null;
    static double BESTSENSETHRESHOLD = 0.8;
    static String WNPREFIX = "";
    static String WNSOURCETAG = "";
    static boolean USEWSD = false;
    static String pathToWNLMF = "";
    static ArrayList<String> relations = new ArrayList<String>();



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
                      if (arg.equals("--wn-lmf") && args.length > (i + 1)) {
                          pathToWNLMF = args[i + 1];
                      } else if (arg.equals("--relations") && args.length > (i + 1)) {
                          String[] relationString = args[i + 1].split("#");
                          for (int j = 0; j < relationString.length; j++) {
                              String s = relationString[j];
                              relations.add(s);
                          }
                      }
                      else if (arg.equals("--naf-file") && args.length>(i+1)) {
                          pathToNafFile = args[i+1];
                      }
                      else if (arg.equals("--naf-folder") && args.length>(i+1)) {
                          folder = args[i+1];
                      }
                      else if (arg.equals("--extension") && args.length>(i+1)) {
                          extension = args[i+1];
                      }
                      else if (arg.equals("--output-tag") && args.length>(i+1)) {
                          outputTag = args[i+1];
                      }
                      else if (arg.equalsIgnoreCase("--replace")) {
                          REMOVEEVENTCOREFS = true;
                      }
                      else if (arg.equalsIgnoreCase("--ignore-false")) {
                          REMOVEFALSEPREDICATES = true;
                      }
                      else if (arg.equals("--wsd") && args.length > (i + 1)) {
                          USEWSD = true;
                          try {
                              BESTSENSETHRESHOLD = Double.parseDouble(args[i + 1]);
                          } catch (NumberFormatException e) {
                              // e.printStackTrace();
                          }
                      }
                      else if (arg.equals("--wn-prefix") && args.length > (i + 1)) {
                          WNPREFIX = args[i + 1].trim();
                      }
                      else if (arg.equals("--wn-source") && args.length > (i + 1)) {
                          WNSOURCETAG = args[i + 1].trim().toLowerCase();
                      }
                  }

                  if (!pathToWNLMF.isEmpty()) {
                      if (!new File(pathToWNLMF).exists()) {
                         System.out.println("Cannot find the wordnet file pathToWNLMF = " + pathToWNLMF);
                         return;
                      }
                     else {
                          WordnetLmfSaxParser wordnetLmfSaxParser = new WordnetLmfSaxParser();
                          wordnetLmfSaxParser.setRelations(relations);
                          wordnetLmfSaxParser.parseFile(pathToWNLMF);
                          wordnetData = wordnetLmfSaxParser.wordnetData;
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
                  process(kafSaxParser);
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

          static void process (KafSaxParser kafSaxParser) {
              if (USEWSD) {
                  processWithWSD(kafSaxParser);
              }
              else {
                  processWithoutWSD(kafSaxParser);
              }
          }

          static void processWithoutWSD(KafSaxParser kafSaxParser) {
              String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
              String strEndDate = null;

              int corefCounter = 0;
              for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
                  KafEvent kafEvent = kafSaxParser.kafEventArrayList.get(i);
                  kafEvent.addTermDataToSpans(kafSaxParser);

                  corefCounter++;
                  CorefTarget corefTarget = new CorefTarget();
                  KafTerm kafTerm = kafSaxParser.getTerm(kafEvent.getSpanIds().get(0));  /// first span reference
                  corefTarget.setId(kafTerm.getTid());
                  corefTarget.setTokenString(kafTerm.getLemma());
                  ArrayList<CorefTarget> corefTargetArrayList = new ArrayList<CorefTarget>();
                  corefTargetArrayList.add(corefTarget);
                  KafCoreferenceSet kafCoreferenceSet = new KafCoreferenceSet();
                  String corefId = "coevent"+corefCounter;
                  kafCoreferenceSet.setCoid(corefId);
                  kafCoreferenceSet.setType("event");
                  kafCoreferenceSet.addSetsOfSpans(corefTargetArrayList);
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

        static void processWithWSD (KafSaxParser kafSaxParser) {
            String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
            String strEndDate = null;

            for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
                KafEvent kafEvent = kafSaxParser.kafEventArrayList.get(i);
                kafEvent.addTermDataToSpans(kafSaxParser);
                String lemma = getLemmaFromKafEvent(kafEvent);
                makeKafCoreferenceSet(kafSaxParser, kafEvent, lemma, wordnetData, BESTSENSETHRESHOLD, WNSOURCETAG, WNPREFIX);
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

        static void makeKafCoreferenceSet (KafSaxParser kafSaxParser,
                                           KafEvent aKafEvent,
                                           String lemma,
                                           WordnetData wordnetData,
                                           double BESTSENSETHRESHOLD,
                                           String WNSOURCETAG,
                                           String WNPREFIX) {
            ArrayList<CorefTarget> anEventCorefTargetList = aKafEvent.getSpans();
            CorefResultSet corefResultSet = new CorefResultSet(lemma,anEventCorefTargetList);
            //// NOW WE BUILD THE COREFSETS AND CONSIDER THE SENSES OF ALL THE TARGETS (sources) TO TAKE THE HIGHEST SCORING ONES
            corefResultSet.getBestSensesAfterCumulation(kafSaxParser, BESTSENSETHRESHOLD, WNSOURCETAG, WNPREFIX);
            corefResultSet.addHypernyms(wordnetData, WNPREFIX);
            KafCoreferenceSet kafCoreferenceSet = corefResultSet.castToKafCoreferenceSetResource(wordnetData);
            String corefId = "coevent" + (kafSaxParser.kafCorefenceArrayList.size() + 1);
            kafCoreferenceSet.setCoid(corefId);
            kafCoreferenceSet.setType("event");
            kafSaxParser.kafCorefenceArrayList.add(kafCoreferenceSet);
        }

        static String getLemmaFromKafEvent(KafEvent kafEvent) {
            String lemma = "";
            for (int j = 0; j < kafEvent.getSpans().size(); j++) {
                CorefTarget corefTarget = kafEvent.getSpans().get(j);
                lemma += corefTarget.getLemma()+" ";
            }
            return lemma.trim();
        }


}
