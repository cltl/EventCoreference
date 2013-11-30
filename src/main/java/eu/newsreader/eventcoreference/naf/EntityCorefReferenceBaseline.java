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
public class EntityCorefReferenceBaseline {
    static final String layer = "coreferences";
    static final String name = "vua-entity-coref-intradoc-reference-baseline";
    static final String version = "1.0";

    static public void main (String [] args) {
          if (args.length==0) {
              processNafStream(System.in);
          }
          else {
              String pathToNafFile = args[0];
              String extension = "";
              for (int i = 0; i < args.length; i++) {
                  String arg = args[i];
                  if (arg.equals("--naf-file") && args.length>(i+1)) {
                      pathToNafFile = args[i+1];
                  }
                  else if (arg.equals("--extension") && args.length>(i+1)) {
                      extension = args[i+1];
                  }
              }
              //File inpFile = new File("/Users/kyoto/Desktop/NWR-DATA/50_docs_test");
              File inpFile = new File(pathToNafFile);
              if (inpFile.isDirectory()) {
                  ArrayList<File> files = Util.makeRecursiveFileList(inpFile, extension);
                  for (int i = 0; i < files.size(); i++) {
                      File file = files.get(i);
                      try {
                          FileOutputStream fos = new FileOutputStream(file.getAbsolutePath()+".coref");
                          processNafFile(fos, file.getAbsolutePath());
                          fos.close();
                      } catch (IOException e) {
                          e.printStackTrace();  //To change body of catch statement ßuse File | Settings | File Templates.
                      }
                  }
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

    static public void processNafFile (FileOutputStream fos, String pathToNafFile) {
        KafSaxParser kafSaxParser = new KafSaxParser();
        kafSaxParser.parseFile(pathToNafFile);
        process(kafSaxParser);
        kafSaxParser.writeNafToStream(fos);
    }


    static void process (KafSaxParser kafSaxParser) {
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
              for (int i = 0; i < kafSaxParser.kafEntityArrayList.size(); i++) {
                  KafEntity kafEntity = kafSaxParser.kafEntityArrayList.get(i);
                  String referenceString = "";
                  ArrayList<CorefTarget> corefTargetArrayList = new ArrayList<CorefTarget>();
                  for (int j = 0; j < kafEntity.getSetsOfSpans().size(); j++) {
                      ArrayList<CorefTarget> spans = kafEntity.getSetsOfSpans().get(j);
                      for (int k = 0; k < spans.size(); k++) {
                          CorefTarget target = spans.get(k);
                          KafTerm kafTerm = kafSaxParser.getTerm(target.getId());
                           /// first span reference
                          target.setTokenString(kafTerm.getLemma());
                          if (referenceString.indexOf(kafTerm.getLemma())==-1) {
                              referenceString += " "+kafTerm.getLemma();
                          }
                          corefTargetArrayList.add(target);
                      }

                  }
                  if (kafEntity.getExternalReferences().size()>0) {
                          referenceString = kafEntity.getExternalReferences().get(0).getSensecode();
                  }
                  if (kafCoreferenceSetHashMap.containsKey(referenceString)) {
                      KafCoreferenceSet kafCoreferenceSet = kafCoreferenceSetHashMap.get(referenceString);
                      kafCoreferenceSet.addSetsOfSpans(corefTargetArrayList);
                      kafCoreferenceSetHashMap.put(referenceString, kafCoreferenceSet);
                  }
                  else {
                      corefCounter++;
                      KafCoreferenceSet kafCoreferenceSet = new KafCoreferenceSet();
                      String corefId = "coentity"+corefCounter;
                      kafCoreferenceSet.setCoid(corefId);
                      kafCoreferenceSet.setType(kafEntity.getType());
                      kafCoreferenceSet.addSetsOfSpans(corefTargetArrayList);
                      kafCoreferenceSetHashMap.put(referenceString, kafCoreferenceSet);
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
