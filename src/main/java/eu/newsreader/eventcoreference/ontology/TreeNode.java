package eu.newsreader.eventcoreference.ontology;

import java.util.ArrayList;

/**
 * Created by piek on 9/12/14.
 */
public class TreeNode<T> {
    private ArrayList<TreeNode<T>> children;
    private TreeNode<T> parent;
    private int numChildren;
    private T data;
    private TreeNode<T> root;

    public TreeNode(){
        this.children = new ArrayList<TreeNode<T>>();
        this.parent = null;
        this.root = null;
        this.data = null;
        this.numChildren = 0;
    }

    public TreeNode(T d){
        this.children = new ArrayList<TreeNode<T>>();
        this.parent = null;
        this.root = null;
        this.data = d;
        this.numChildren = 0;
    }

    public TreeNode(TreeNode<T> p, TreeNode<T> r){
        this.children = new ArrayList<TreeNode<T>>();
        this.parent = p;
        this.root = r;
        this.data = null;
        this.numChildren = 0;
    }

    public TreeNode(T d, TreeNode<T> p, TreeNode<T> r){
        this.children = new ArrayList<TreeNode<T>>();
        this.parent = p;
        this.root = r;
        this.data = d;
        this.numChildren = 0;
    }

    public void addChild(T d){
        children.add(new TreeNode<T>(d, this, this.root));
        numChildren++;
    }

    public int getNumChildren(){
        return numChildren;
    }

    public TreeNode<T> getParent(){
        return this.parent;
    }

    public void setParent(TreeNode<T> t){
        this.parent = t;
    }

    public ArrayList<TreeNode<T>> getChildren(){
        return this.children;
    }

    public T getData(){
        return this.data;
    }

    public void setData(T d){
        this.data = d;
    }
}
