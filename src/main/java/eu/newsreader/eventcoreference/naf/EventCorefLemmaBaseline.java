package eu.newsreader.eventcoreference.naf;

import eu.kyotoproject.kaf.*;

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
public class EventCorefLemmaBaseline {

          static public void main (String [] args) {
              if (args.length==0) {
                  processNafStream(System.in);
              }
              else {
                  String pathToNafFile = args[0];
                  for (int i = 0; i < args.length; i++) {
                      String arg = args[i];
                      if (arg.equals("--naf-file") && args.length>(i+1)) {
                          pathToNafFile = args[i+1];
                      }
                  }
                  //String pathToNafFile = "/Code/vu/kyotoproject/Coreference/example/enPM.naf";
                  processNafFile(pathToNafFile);
              }
          }

          static public void processNafStream (InputStream nafStream) {
              KafSaxParser kafSaxParser = new KafSaxParser();
              kafSaxParser.parseFile(nafStream);
              int corefCounter = 0;
              HashMap<String, KafCoreferenceSet> kafCoreferenceSetHashMap = new HashMap<String, KafCoreferenceSet>();
              for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
                  KafEvent kafEvent = kafSaxParser.kafEventArrayList.get(i);
                  CorefTarget corefTarget = new CorefTarget();
                  KafTerm kafTerm = kafSaxParser.getTerm(kafEvent.getSpans().get(0));  /// first span reference
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
                      String corefId = "coe"+corefCounter;
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
              kafSaxParser.writeNafToStream(System.out);
/*
              try {
                 // kafSaxParser.writeNafToStream(System.out);
                  FileOutputStream fos = new FileOutputStream(pathToNafFile+".coref");
                  kafSaxParser.writeNafToStream(fos);
                  fos.close();
              } catch (IOException e) {
                  e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
              }
*/
          }

          static public void processNafFile (String pathToNafFile) {
              KafSaxParser kafSaxParser = new KafSaxParser();
              kafSaxParser.parseFile(pathToNafFile);
              int corefCounter = 0;
              HashMap<String, KafCoreferenceSet> kafCoreferenceSetHashMap = new HashMap<String, KafCoreferenceSet>();
              for (int i = 0; i < kafSaxParser.kafEventArrayList.size(); i++) {
                  KafEvent kafEvent = kafSaxParser.kafEventArrayList.get(i);
                  CorefTarget corefTarget = new CorefTarget();
                  KafTerm kafTerm = kafSaxParser.getTerm(kafEvent.getSpans().get(0));  /// first span reference
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
                      String corefId = "coe"+corefCounter;
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
              kafSaxParser.writeNafToStream(System.out);
/*
              try {
                 // kafSaxParser.writeNafToStream(System.out);
                  FileOutputStream fos = new FileOutputStream(pathToNafFile+".coref");
                  kafSaxParser.writeNafToStream(fos);
                  fos.close();
              } catch (IOException e) {
                  e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
              }
*/
          }

}
