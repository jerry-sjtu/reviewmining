/**   
* @Description: TODO
* @author weifu.du 
* @project review-mining-single
* @date 2012-9-28 
* @version V1.0
* 
* Copyright (c) 2010-2015 by Shanghai HanTao Information Co., Ltd.
 * All rights reserved.
*/
package com.dianping.algorithm.dp_review_mining.feature;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Tree<L> implements Serializable {
  
	private static final long serialVersionUID = 1L;

	public L label;
	public List<Tree<L>> children;
	public Tree<L> parTree;
	
	public Tree(L label, List<Tree<L>> children) {
		this.label = label;
		this.children = children;
	}

	public Tree(L label) {
		this.label = label;
		this.children = Collections.emptyList();
	}
	
	public void setChildren(List<Tree<L>> c) {
		this.children= c;
	}
	
	public List<Tree<L>> getChildren() {
	    return children;
	}
	
	public L getLabel() {
	   return label;
	}
	
	/**
	 * 判断其是否是叶子节点
	 * @return
	 */
	public boolean isLeaf() {
	   return getChildren().isEmpty();
	}
  
	/**
	 * 先序遍历树
	 * @return
	 */
	public List<Tree<L>> getPreOrderTraversal() {
		ArrayList<Tree<L>> traversal = new ArrayList<Tree<L>>();
		traversalHelper(this, traversal, true);
		return traversal;
	}

	/**
	 * 后序遍历树
	 * @return
	 */
	public List<Tree<L>> getPostOrderTraversal() {
		ArrayList<Tree<L>> traversal = new ArrayList<Tree<L>>();
		traversalHelper(this, traversal, false);
		return traversal;
	}

	/*
	 * 遍历过程
	 */
	private static <L> void traversalHelper(Tree<L> tree, List<Tree<L>> traversal, boolean preOrder) {
		if (preOrder) traversal.add(tree);
		for (Tree<L> child : tree.getChildren()) {
			traversalHelper(child, traversal, preOrder);
		}
		if (!preOrder) traversal.add(tree);
	}
  
	/**
	 * 得到当前树的深度
	 * @return
	 */
	public int getDepth() {
	  	int maxDepth = 0;
	  	for (Tree<L> child : children) {
	  		int depth = child.getDepth();
	  		if (depth>maxDepth)
	  			maxDepth = depth;
	  	}
	  	return maxDepth + 1;
	}
  
	public void setLabel(L label) {
		this.label = label;
	}
	
	/** 
	 * 复制树
	 * @return
	 */
	public synchronized Tree<L> shallowClone() {
	  	ArrayList<Tree<L>> newChildren = new ArrayList<Tree<L>>(children.size());
	  	for(Tree<L> t : children)
	  		newChildren.add(t.shallowClone());
	  	return new Tree<L>(label, newChildren);
	}
	
	/**
	 * 序列化
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toStringBuilder(sb);
		return sb.toString();
	}
	
	/**
	 * 用于序列化的遍历
	 * @param sb
	 */
	public void toStringBuilder(StringBuilder sb) {
		if (! isLeaf()) sb.append('(');
		if (getLabel() != null) 
			sb.append(getLabel());
		if (! isLeaf()) {
			for (Tree<L> child : getChildren()) {
				sb.append(' ');
				child.toStringBuilder(sb);
			}
			sb.append(')');
		}
	}
	
	/**
	 * 以间隔的形式打印该节点下面的节点
	 */
	public void printSubNode(){
		toPrintSubNode(0);
	}
	
	/*
	 * 用于打印当前节点下的所有节点
	 */
	private void toPrintSubNode(int blankNum){
		for (int i = 0; i < blankNum; i++) 
			System.out.print(" ");
		System.out.println(getLabel());
		if (isLeaf()) return;
		for (Tree<L> child : getChildren()) 
			child.toPrintSubNode(blankNum+1);
	}

	/**
	 * 迭代器
	 * @return
	 */
	public Iterator iterator() {
		return new TreeIterator();
	}
  
	private class TreeIterator implements Iterator {

		private List<Tree<L>> treeStack;

		private TreeIterator() {
			treeStack = new ArrayList<Tree<L>>();
			treeStack.add(Tree.this);
		}

		public boolean hasNext() {
			return (!treeStack.isEmpty());
		}

		public Object next() {
	 	    int lastIndex = treeStack.size() - 1;
	 	    Tree<L> tr = treeStack.remove(lastIndex);
	 	    List<Tree<L>> kids = tr.getChildren();
	 	    for (int i = kids.size() - 1; i >= 0; i--) {
	 	    	treeStack.add(kids.get(i));
	 	    }
	 	    return tr;
		}

		/**
		 * Not supported
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}

