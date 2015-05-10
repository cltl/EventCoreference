package eu.newsreader.eventcoreference.input;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by piek on 09/05/15.
 */
public class NarrativeChains {
    //Events: sell buy own acquire operate purchase spin_off build plan pay merge announce

    public HashMap<String, ArrayList<Integer>> chainMap;
    public HashMap<Integer, ArrayList<String>> verbMap;

    public NarrativeChains (String chainFilePath) {
        chainMap = new HashMap<String, ArrayList<Integer>>();
        verbMap = new HashMap<Integer, ArrayList<String>>();
        if (new File(chainFilePath).exists() ) {
            try {
                FileInputStream fis = new FileInputStream(chainFilePath);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                Integer chainId = 0;
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    //System.out.println(inputLine);
                    if (inputLine.trim().startsWith("Events:")) {
                        chainId++;
                        ArrayList<String> verbsArrayList = new ArrayList<String>();
                        String [] verbs = inputLine.split(" ");
                        for (int i = 1; i < verbs.length; i++) { /// skipping Events:
                            String verb = verbs[i];
                            verbsArrayList.add(verb);
                            if (chainMap.containsKey(verb)) {
                                ArrayList<Integer> chains = chainMap.get(verb);
                                chains.add(chainId);
                                chainMap.put(verb, chains);
                            }
                            else {
                                ArrayList<Integer> chains = new ArrayList<Integer>();
                                chains.add(chainId);
                                chainMap.put(verb, chains);
                            }
                        }
                        verbMap.put(chainId, verbsArrayList);
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
