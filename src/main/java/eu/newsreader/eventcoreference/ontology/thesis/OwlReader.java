package eu.newsreader.eventcoreference.ontology.thesis;

import java.util.ArrayList;

/**
 * Created by piek on 9/12/14.
 */
public class OwlReader {


    private static Tree<NodeData> tree;
    private static int PARENT 	= 0;
    private static int ABOUT 	= 1;
    private static int LABEL	= 2;
    private static int COMMENT	= 3;
    private static String pathToRdfFile = "/Owled/rdfTester.owl";

    static public void main (String[] args) {
        String pathToKafFile = "/Users/michakemeling/Dropbox/TH@SIS/eigen_test.kaf";
        RdfSense data = new RdfSense(pathToRdfFile);
        createTree(data.getRdfData());

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ((arg.equalsIgnoreCase("--kaf-file")) && (args.length>(i+1))) {
                pathToKafFile = args[i+1];
            }
            else if ((arg.equalsIgnoreCase("--eu.kyotoproject.rdf-file")) && (args.length>(i+1))) {
                pathToRdfFile = args[i+1];
            }
        }

    }

    //todo: to fix parent stuff above, ArrayList<String[]>, where each String[] is the parent line.
    public static ArrayList<String> getLemmaParents(String s){
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<TreeNode<NodeData>> found = tree.find(s);
        System.out.println(found.get(0).getData().getLabel());
        System.out.println(found.size());
        for(int i = 0; i < found.size(); i++){
            //if finding more than 1 candidate, use null as a seperator in the list.
            if(i != 0){ result.add(null); }
            TreeNode<NodeData> temp = found.get(i);
            while(temp.getParent() != null){
                result.add(temp.getData().getLabel());
                temp = temp.getParent();
            }
        }
        return result;
    }

    private static void createTree(ArrayList<String[]> rdfData){
        tree = new Tree<NodeData>();
        //Micha-Comment:
        //don't know if rdfList.size changes correctly when removing items...
        //so for now I'm not doing it
        for(int i = 0; i < rdfData.size(); i++){
            String[] sA = rdfData.get(i);
            if(sA[PARENT] == null){
                NodeData d = new NodeData(sA[ABOUT], sA[LABEL], sA[COMMENT]);
                tree.addChild(d);
                addSubChild(d.getUrl(), rdfData);
            }
        }
        tree.toParent();
    }

    private static void addSubChild(String parentUrl, ArrayList<String[]> rdfData){
        for(int i = 0; i < rdfData.size(); i++){
            String[] sA = rdfData.get(i);
            if(sA[PARENT] != null && sA[PARENT].equals(parentUrl)){
                NodeData d = new NodeData(sA[ABOUT], sA[LABEL], sA[COMMENT]);
                tree.addChild(d);
                addSubChild(d.getUrl(), rdfData);
            }
        }
        tree.toParent();
    }
}
