package eu.newsreader.eventcoreference.storyline;

import eu.newsreader.eventcoreference.objects.JsonEvent;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by piek on 17/02/16.
 */
public class JsonSerialization {

    static void writeJsonObjectArray (String pathToFolder, String project, ArrayList<JSONObject> objects) {
        try {
            try {
                File folder = new File(pathToFolder);
                OutputStream jsonOut = new FileOutputStream(folder.getAbsoluteFile() + "/" + "contextual.timeline.json");
                // OutputStream jsonOut = new FileOutputStream(folder.getParentFile() + "/" + folder.getName()+".timeline.json");
                JSONObject timeLineObject = JsonEvent.createTimeLineProperty(new File(pathToFolder).getName(), project);
                /*Set keySet = actorCount.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Integer cnt = actorCount.get(key);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", key);
                    jsonObject.put("event_count", cnt);
                    timeLineObject.append("actors", jsonObject);
                }*/
                for (int j = 0; j < objects.size(); j++) {
                    JSONObject jsonObject = objects.get(j);
                    timeLineObject.append("events", jsonObject);
                }

                String str = "{ \"timeline\":\n";
                jsonOut.write(str.getBytes());
                jsonOut.write(timeLineObject.toString(1).getBytes());
                str ="}\n";
                jsonOut.write(str.getBytes());
                jsonOut.close();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void writeJsonObjectArrayForQuery (String SERVICE, String KS, String query,
                                              String project,
                                              ArrayList<JSONObject> objects,
                                              int nEvents,
                                              int nStories,
                                              int nActors,
                                              int nMentions,
                                              String KSuser,
                                              String KSpass) {
        try {
            try {
                JSONObject timeLineObject = JsonEvent.createTimeLineProperty(query, project);
                /*Set keySet = actorCount.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Integer cnt = actorCount.get(key);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", key);
                    jsonObject.put("event_count", cnt);
                    timeLineObject.append("actors", jsonObject);
                }*/
                if (KSuser.isEmpty()) {
                    MentionResolver.createRawTextIndexFromMentions(objects, timeLineObject, KS);
                }
                else {
                    MentionResolver.createRawTextIndexFromMentions(objects, timeLineObject, SERVICE, KS, KSuser, KSpass);
                }
                timeLineObject.append("event_cnt", nEvents);
                timeLineObject.append("story_cnt", nStories);
                timeLineObject.append("actor_cnt", nActors);
                timeLineObject.append("mention_cnt", nMentions);
                for (int j = 0; j < objects.size(); j++) {
                    JSONObject jsonObject = objects.get(j);
                    try {
                        timeLineObject.append("events", jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
               // OutputStream jsonOut = new FileOutputStream( "./"+query + ".timeline.json");
                OutputStream jsonOut = new FileOutputStream( "./"+"contextual" + ".timeline.json");

                String str = "{ \"timeline\":\n";
                jsonOut.write(str.getBytes());
                jsonOut.write(timeLineObject.toString(0).getBytes());
                str ="}\n";
                jsonOut.write(str.getBytes());
                //// OR simply
                //jsonOut.write(timeLineObject.toString().getBytes());
                jsonOut.close();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void writeJsonObjectArray (String pathToFolder,
                                      String project,
                                      ArrayList<JSONObject> objects,
                                      ArrayList<JSONObject> rawTextArrayList,
                                      int nEvents,
                                      int nStories,
                                      int nActors,
                                      int nMentions) {
        try {
            try {
                // OutputStream jsonOut = new FileOutputStream(folder.getParentFile() + "/" + folder.getName()+".timeline.json");
                JSONObject timeLineObject = JsonEvent.createTimeLineProperty(new File(pathToFolder).getName(), project);
                /*Set keySet = actorCount.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Integer cnt = actorCount.get(key);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", key);
                    jsonObject.put("event_count", cnt);
                    timeLineObject.append("actors", jsonObject);
                }*/
                timeLineObject.append("event_cnt", nEvents);
                timeLineObject.append("story_cnt", nStories);
                timeLineObject.append("actor_cnt", nActors);
                timeLineObject.append("mention_cnt", nMentions);
                for (int j = 0; j < objects.size(); j++) {
                    JSONObject jsonObject = objects.get(j);
                    timeLineObject.append("events", jsonObject);
                }
                if (rawTextArrayList.size()>0) {
                    for (int i = 0; i < rawTextArrayList.size(); i++) {
                        JSONObject jsonObject = rawTextArrayList.get(i);
                        timeLineObject.append("sources", jsonObject);
                    }
                }
                File folder = new File(pathToFolder);
                OutputStream jsonOut = new FileOutputStream(folder.getAbsolutePath() + "/" + "contextual.timeline.json");

                String str = "{ \"timeline\":\n";
                jsonOut.write(str.getBytes());
                jsonOut.write(timeLineObject.toString(0).getBytes());
                str ="}\n";
                jsonOut.write(str.getBytes());
                //// OR simply
                // jsonOut.write(timeLineObject.toString().getBytes());
                jsonOut.close();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void writeJsonObjectArray (String pathToFolder,
                                      String project,
                                      ArrayList<JSONObject> objects,
                                      int nEvents,
                                      int nStories,
                                      int nActors,
                                      int nMentions) {
        try {
            try {
                // OutputStream jsonOut = new FileOutputStream(folder.getParentFile() + "/" + folder.getName()+".timeline.json");
                JSONObject timeLineObject = JsonEvent.createTimeLineProperty(new File(pathToFolder).getName(), project);
                /*Set keySet = actorCount.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Integer cnt = actorCount.get(key);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", key);
                    jsonObject.put("event_count", cnt);
                    timeLineObject.append("actors", jsonObject);
                }*/
                timeLineObject.append("event_cnt", nEvents);
                timeLineObject.append("story_cnt", nStories);
                timeLineObject.append("actor_cnt", nActors);
                timeLineObject.append("mention_cnt", nMentions);
                for (int j = 0; j < objects.size(); j++) {
                    JSONObject jsonObject = objects.get(j);
                    timeLineObject.append("events", jsonObject);
                }

                File folder = new File(pathToFolder);
                OutputStream jsonOut = new FileOutputStream(folder.getAbsolutePath() + "/" + "contextual.timeline.json");

                String str = "{ \"timeline\":\n";
                jsonOut.write(str.getBytes());
                jsonOut.write(timeLineObject.toString(0).getBytes());
                str ="}\n";
                jsonOut.write(str.getBytes());
                //// OR simply
                // jsonOut.write(timeLineObject.toString().getBytes());
                jsonOut.close();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void writeJsonPerspectiveArray (String pathToFolder,
                                      String project,
                                      ArrayList<JSONObject> objects) {
        try {
            try {
                JSONObject timeLineObject = JsonEvent.createTimeLineProperty(new File(pathToFolder).getName(), project);
               // timeLineObject.append("event_cnt", nEvents);
               // timeLineObject.append("story_cnt", nStories);
               // timeLineObject.append("actor_cnt", nActors);
               // timeLineObject.append("mention_cnt", nMentions);
                for (int j = 0; j < objects.size(); j++) {
                    JSONObject jsonObject = objects.get(j);
                    timeLineObject.append("events", jsonObject);
                }

                File folder = new File(pathToFolder);
                OutputStream jsonOut = new FileOutputStream(folder.getAbsolutePath() + "/" + "perspective.timeline.json");

                String str = "{ \"timeline\":\n";
                jsonOut.write(str.getBytes());
                jsonOut.write(timeLineObject.toString(0).getBytes());
                str ="}\n";
                jsonOut.write(str.getBytes());
                //// OR simply
                // jsonOut.write(timeLineObject.toString().getBytes());
                jsonOut.close();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void writeJsonStructuredArray (String pathToFolder,
                                      String project,
                                      ArrayList<JSONObject> objects) {
        try {
            try {
                JSONObject timeLineObject = JsonEvent.createTimeLineProperty(new File(pathToFolder).getName(), project);
               // timeLineObject.append("event_cnt", nEvents);
               // timeLineObject.append("story_cnt", nStories);
               // timeLineObject.append("actor_cnt", nActors);
               // timeLineObject.append("mention_cnt", nMentions);
                for (int j = 0; j < objects.size(); j++) {
                    JSONObject jsonObject = objects.get(j);
                    timeLineObject.append("events", jsonObject);
                }

                File folder = new File(pathToFolder);
                OutputStream jsonOut = new FileOutputStream(folder.getAbsolutePath() + "/" + "structured.timeline.json");

                String str = "{ \"timeline\":\n";
                jsonOut.write(str.getBytes());
                jsonOut.write(timeLineObject.toString(0).getBytes());
                str ="}\n";
                jsonOut.write(str.getBytes());
                //// OR simply
                // jsonOut.write(timeLineObject.toString().getBytes());
                jsonOut.close();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static void writeJsonObjectArrayWithStructuredData (String pathToFolder,
                                                        String name,
                                                        String project,
                                                        ArrayList<JSONObject> objects,
                                                        ArrayList<JSONObject> mentions,
                                                        int nEvents, int nStories, int nActors, int nMentions,
                                                        String structuredName,
                                                        ArrayList<JSONObject> structured) {
        try {
            try {
                // OutputStream jsonOut = new FileOutputStream(folder.getParentFile() + "/" + folder.getName()+".timeline.json");
                JSONObject timeLineObject = JsonEvent.createTimeLineProperty(new File(pathToFolder).getName(), project);
                /*Set keySet = actorCount.keySet();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Integer cnt = actorCount.get(key);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", key);
                    jsonObject.put("event_count", cnt);
                    timeLineObject.append("actors", jsonObject);
                }*/
                timeLineObject.append("event_cnt", nEvents);
                timeLineObject.append("story_cnt", nStories);
                timeLineObject.append("actor_cnt", nActors);
                timeLineObject.append("mention_cnt", nMentions);


                for (int j = 0; j < structured.size(); j++) {
                    JSONObject jsonObject = structured.get(j);
                    timeLineObject.append(structuredName, jsonObject);
                }

                for (int j = 0; j < objects.size(); j++) {
                    JSONObject jsonObject = objects.get(j);
/*
                    String instance = jsonObject.getString("instance");
                    if (instance.equalsIgnoreCase("http://www.newsreader-project.eu/data/dasym/426985.naf.topic#ev8")) {
                        JSONObject actors = (JSONObject) jsonObject.get("actors");
                        System.out.println("actors = " + actors.toString()  );
                    }
*/

                    timeLineObject.append("events", jsonObject);
                }

                for (int j = 0; j < mentions.size(); j++) {
                    JSONObject jsonObject = mentions.get(j);
                    timeLineObject.append("sources", jsonObject);
                }
                File folder = new File(pathToFolder);
                OutputStream jsonOut = new FileOutputStream(folder.getAbsolutePath() + "/" + name+ "contextual.timeline.json");

                String str = "{ \"timeline\":\n";
                jsonOut.write(str.getBytes());
                jsonOut.write(timeLineObject.toString(0).getBytes());
                str ="}\n";
                jsonOut.write(str.getBytes());
                //// OR simply
                // jsonOut.write(timeLineObject.toString().getBytes());
                jsonOut.close();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
