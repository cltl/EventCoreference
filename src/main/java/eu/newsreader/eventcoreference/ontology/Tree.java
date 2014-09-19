package eu.newsreader.eventcoreference.ontology;

import java.util.ArrayList;

/**
 * Created by piek on 9/12/14.
 */
public class Tree<T> {

    private TreeNode<T> root;
    private TreeNode<T> currentNode;
    ArrayList<TreeNode<T>> searchResult;

    public Tree(){
        root = new TreeNode<T>();
        root.setParent(null);
        root.setData(null);
        currentNode = root;
        searchResult = new ArrayList<TreeNode<T>>();
    }

    public Tree(T data){
        root = new TreeNode<T>(data);
        root.setParent(null);
        currentNode = root;
        searchResult = new ArrayList<TreeNode<T>>();
    }

    public void setRoot(){
        currentNode = root;
    }

    public void setCurrentNode(TreeNode<T> node){
        currentNode = node;
    }

    public TreeNode<T> getRoot(){
        return this.root;
    }

    public void addChild(T d){
        currentNode.addChild(d);
        currentNode = currentNode.getChildren().get(currentNode.getChildren().size()-1);
    }

    public void toParent(){
        currentNode = currentNode.getParent();
    }

    //root node is simply a node to hold the tree together. data.label is 'Object' or something.
    public ArrayList<TreeNode<T>> find(String s){
        searchResult.clear();
        ArrayList<TreeNode<T>> children = root.getChildren();
        for(int i = 0; i< root.getNumChildren(); i++){
            search(children.get(i), s.toLowerCase());
        }
        if(searchResult.size() == 0){
            return null;
        }
        return searchResult;
    }

    private void search(TreeNode<T> node, String s){
        ArrayList<TreeNode<T>> children = node.getChildren();
        for(int i = 0; i < node.getNumChildren(); i++){
            search(children.get(i), s);
        }
        //when used with RdfSesnse, the data in the node is a NodeData class.
        //if you want to use other data classes, create that class and add
        //your find criteria below.
        if(node.getData() instanceof NodeData){
            NodeData d = (NodeData) node.getData();
            if(d.getComment().toLowerCase().equals(s)
                    || d.getUrl().toLowerCase().equals(s)
                    || d.getLabel().toLowerCase().equals(s)){
                searchResult.add(node);
            }
        }
    }
}
