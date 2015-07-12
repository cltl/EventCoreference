package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.util.Util;

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
                      else if (arg.equals("--extension") && args.length>(i+1)) {
                          extension = args[i+1];
                      }
                      else if (arg.equalsIgnoreCase("--replace")) {
                          REMOVEEVENTCOREFS = true;
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
              process(kafSaxParser);
              kafSaxParser.writeNafToStream(System.out);
          }

          static public void processNafFile (String pathToNafFile) {
              KafSaxParser kafSaxParser = new KafSaxParser();
              kafSaxParser.parseFile(pathToNafFile);
              if (REMOVEEVENTCOREFS) {
                  Util.removeEventCoreferences(kafSaxParser);
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

              int corefCounter = 0;
              for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
                  KafEvent kafEvent = kafSaxParser.kafEventArrayList.get(i);
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

}
