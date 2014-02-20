package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.*;
import eu.newsreader.eventcoreference.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
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
    static final String version = "1.0";

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
              process(kafSaxParser);
              kafSaxParser.writeNafToStream(System.out);
          }

          static public void processNafFile (String pathToNafFile) {
              KafSaxParser kafSaxParser = new KafSaxParser();
              kafSaxParser.parseFile(pathToNafFile);
              process(kafSaxParser);
              kafSaxParser.writeNafToStream(System.out);
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
              Calendar date = Calendar.getInstance();
              date.setTimeInMillis(System.currentTimeMillis());
              String strdate = null;
              SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

              if (date != null) {
                  strdate = sdf.format(date.getTime());
              }

              kafSaxParser.getKafMetaData().addLayer(layer, name, version, strdate);

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
          }

}
