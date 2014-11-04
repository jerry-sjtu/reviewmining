/**   
* @Description: 以树状结构表达评论中的产品特征，以及与其相关的用户评价 
* @author weifu.du 
* @project review-mining-single
* @date 2012-9-28 
* @version V1.0
* 
* Copyright (c) 2010-2015 by Shanghai HanTao Information Co., Ltd.
 * All rights reserved.
*/
package com.dianping.algorithm.dp_review_mining.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.dom4j.Branch;
import org.dom4j.Element;



import com.dianping.algorithm.dp_review_mining.associate_helper.AttributeOpinionPair;
import com.dianping.algorithm.dp_review_mining.utility.Queue;

@SuppressWarnings({ "serial", "rawtypes" })
public class Feature extends Tree implements Cloneable, Comparable {
	//特征同义词
	public Set<String> synonymSet = new HashSet<String>(); 
	//用于评论摘要
	public List<AttributeOpinionPair> pairList = null;
	//以该节点为根的子树中，是否包含评论对；用于图形化打印
	public boolean containPairList = false;
	
	public int positivePairs = 0;
	public int negativePairs = 0;
	
	//用于demo中的摘要显示，日后如不需要，可考虑重构或删除
	public Map<String, Integer> opinionMap = new HashMap<String, Integer>();
	
	@SuppressWarnings({ "unchecked" })
	public Feature(String label) {
		super(label);
		children = new ArrayList<Feature>();
		parTree  = null;
	}
	
	@SuppressWarnings("unchecked")
	public Feature(String label, ArrayList<Feature> children) {
		// TODO Auto-generated constructor stub
		super(label, children);
	}
	
	@SuppressWarnings("unchecked")
	public Feature(Object label, ArrayList<Feature> newChildren) {
		// TODO Auto-generated constructor stub
		super(label);
		children = newChildren;
	}

	/**
	 * 给定父亲节点，将孩子结点插入到树中
	 * @param parConcept
	 * @param feature
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean addChild(Feature feature, int occurNum){		
		feature.parTree = this;
		children.add(feature);
		return true;
	}
	
	/**
	 * 返回当前节点的所有孩子结点名字列表
	 * @param nodeName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> getNodeNameList(){	
		List<String> nodeList = new ArrayList<String>();	
		Queue queue = new Queue();
		queue.add(this);
		
		while(!queue.isEmpty()){
			Feature tmpFeature = (Feature) queue.front();
			nodeList.add((String)tmpFeature.label);
			
			for (int i = 0; i < tmpFeature.getChildren().size(); i++)
				queue.add(tmpFeature.getChildren().get(i));
			
			queue.deq();
		}
		if(nodeList.size() < 1) return null;
		else return nodeList;
	}
	
	/**
	 * 根据节点的名字寻找节点
	 * @param feature 起始节点
	 * @param name
	 * @param bSynonym 是否考虑同义词
	 * @return feature
	 */
	public static Feature findNode(Feature feature, String label, boolean bSynonym){
		if(feature == null || label == null) return null;
		boolean bFound = bSynonym ? (feature.label.equals(label) || feature.synonymSet.contains(label)) : feature.label.equals(label);
		if(bFound) return feature;
		
		if(feature.isLeaf()) return null;
		Feature choiceFeature = null;
		for (int i = 0; i < feature.children.size(); i++) {
			if((choiceFeature = findNode((Feature) feature.children.get(i), label, bSynonym)) != null)
				return choiceFeature;
		}
		return null;
	}
	
	/**
	 * 根据节点的名字寻找节点,结果保存在List中
	 * 同一个节点可以挂载多处，保存的是一个List
	 * @param feature 起始节点
	 * @param name
	 */
	public static void findNodeAll(Feature feature, String label, List<Feature> outList){
		if(feature == null || label == null || outList == null) return;
		if(feature.label.equals(label)) outList.add(feature);
		if(feature.isLeaf()) return;
		for (int i = 0; i < feature.children.size(); i++)
			findNodeAll((Feature) feature.children.get(i), label, outList);
	}
	
	/**
	 * 判断label代表的节点是否在feature的直接祖先节点上
	 * @param feature
	 * @param label
	 * @return true means yes; false means no
	 */
	public static boolean isDirectAncestorConcept(Feature feature, String label){
		if(feature == null || label == null) return false;
		if(label.equals(feature.getLabel())) return true;
		Feature tmpfeature = (Feature) feature.parTree;
		while(tmpfeature != null){
			if(label.equals(tmpfeature.getLabel())) 
				return true;
			else 
				tmpfeature = (Feature) tmpfeature.parTree;
		}
		return false;
	}

	/**
	 * 寻找公共祖先节点
	 * @param featureList 子节点列表
	 * @return feature 返回祖先节点
	 */
	public static Feature searchAncestorNode(List<Feature> featureList){
		if(featureList == null || featureList.size() == 0) return null;
		if(featureList.size() == 1) return featureList.get(0);
		
		Feature parNode = featureList.get(0);
		for (int i = 1; i < featureList.size(); i++) 
			parNode = searchParNode(parNode, featureList.get(i));
		
		return parNode;
	}
	
	/**
	 * 寻找两个节点的公共父节点
	 * @param node1
	 * @param node2
	 * @return feature 返回公共父节点
	 */
	public static Feature searchParNode(Feature node1, Feature node2){
		if(node1 == null || node2 == null) return null;
		
		Feature parNode1 = node1;
		Feature parNode2 = node2;
		while(parNode1 != null){
			while(parNode2 != null){
				if(parNode1 == parNode2) return parNode1;
				parNode2 = (Feature) parNode2.parTree;
			}
			parNode1 = (Feature) parNode1.parTree;
		}
		return null;
	}
	
	/**
	 * 寻找两个节点的公共父节点
	 * @param node1
	 * @param node2
	 * @return feature 返回公共父节点
	 */
	public static Feature searchParNode(Feature root, String str1, String str2){
		if(str1 == null || str2 == null) return null;
		
		Feature node1 = findNode(root, str1, false);
		Feature node2 = findNode(root, str2, false);	
		if(node1 == null || node2 == null) return null;

		Feature parNode1 = node1;
		Feature parNode2 = node2;
		while(parNode1 != null){
			while(parNode2 != null){
				if(parNode1 == parNode2) return parNode1;
				parNode2 = (Feature) parNode2.parTree;
			}
			parNode1 = (Feature) parNode1.parTree;
			parNode2 = node2;
		}
		return null;
	}
	
	/**
	 * 返回当前节点的路径字符串，节点间以空格隔开
	 * @param feature
	 * @return String
	 */
	public static String getTreePathStr(Feature feature){
		if(feature == null) return null;
		
		StringBuffer sb = new StringBuffer();
		List<Feature> cList = new ArrayList<Feature>();
		Feature tmpFeature = (Feature) feature.parTree;
		while(tmpFeature != null){
			cList.add(tmpFeature);
			tmpFeature = (Feature) tmpFeature.parTree;
		}
		for (int i = cList.size() -1 ; i >= 0; i--) 
			sb.append(cList.get(i).label + " ");
		sb.append(feature.label);
		return sb.toString();
	}
	
	/**
	 * 判断当前词是不是该特征的同义词
	 * @param instanceName
	 * @return boolean
	 */
	public boolean isInstance(String instanceName){
		if(synonymSet == null || instanceName == null)
			return false;
		if(synonymSet.contains(instanceName))
			return true;
		return false;
	}
	
	/**
	 * 得到某个名称为laebl的特征的评论摘要
	 * @param root
	 * @param label
	 * @return
	 */
	public static List<AttributeOpinionPair> getSummaryOfLabel(Feature root, String label) {
		List<AttributeOpinionPair> resultList = new ArrayList<AttributeOpinionPair>();
		Feature feature = findNode(root, label, false);
		if(feature!=null)
			feature.getSummary(resultList);
		return resultList;
	}
	
	/**
	 * 得到该特征（及其子孙）的所有评论摘要
	 * @return
	 */
	public void getSummary(List<AttributeOpinionPair> resultList) {
		if (pairList != null) {
			resultList.addAll(pairList);
		}
		if (children != null) {
			for (Object child : children) {
				Feature childFeature = (Feature)child;
				childFeature.getSummary(resultList);
			}
		}
	}
	
	/**
	 * 找到以当前结点为根节点的子树的第nlayer层的所有结点（当前结点为第1层）
	 * @param nLayer
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Feature> getFeatureOfLayer(int nLayer) {
		if (nLayer < 1 || nLayer > getDepth()) {
			return null;
		}
		int beginOfNextLayer = 0;
		int beginOfCurrentLayer = 0;
		List<Feature> resultList = new LinkedList<Feature>();
		resultList.add(this);
		beginOfNextLayer++;
		
		for (int currLayer = 1; currLayer < nLayer; currLayer++) {
			int offspringInThisLayer = 0;
			for (int currFeature = beginOfCurrentLayer; currFeature < beginOfNextLayer; currFeature++) {
				resultList.addAll(resultList.get(currFeature).children);
				offspringInThisLayer += resultList.get(currFeature).children.size();
			}
			beginOfCurrentLayer = beginOfNextLayer;
			beginOfNextLayer += offspringInThisLayer;
		}
		
		for (int i = 0; i < beginOfCurrentLayer; i++) {
			resultList.remove(0);
		}
		return resultList;
	}
	
	/**
	 * 获得以当前结点为根节点的子树的第nlayer层的所有摘要（当前结点为第1层）
	 * @param nLayer
	 * @return
	 */
	public Map<String, List<AttributeOpinionPair>> getSummaryOfLayer(int nLayer) {
		if (nLayer < 1 || nLayer > getDepth()) {
			return null;
		}
		Map<String, List<AttributeOpinionPair>> resultMap = new HashMap<String, List<AttributeOpinionPair>>();
		List<Feature> featuresInThisLayer = getFeatureOfLayer(nLayer);
		for (Feature feature : featuresInThisLayer) {
			List<AttributeOpinionPair> value = new ArrayList<AttributeOpinionPair>();
			String key = String.valueOf(feature.label);
			value.clear();
			feature.getSummary(value);
			resultMap.put(key, value);
		}
		return resultMap;
	}
	
	
	/**
	 * below are getters and setters
	 */

	public Set<String> getSynonymSet() {
		return synonymSet;
	}

	public void setSynonymSet(HashSet<String> synonymSet) {
		this.synonymSet = synonymSet;
	}
	
	public static void main(String[] args) {
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		Feature other = (Feature)o;
		Integer thisPairCount = Integer.valueOf(positivePairs + negativePairs);
		Integer otherPairCount = Integer.valueOf(other.positivePairs + other.negativePairs);
		return otherPairCount.compareTo(thisPairCount);
	}
	
	public Object clone() {

//		Feature obj = null;
//		try {
//			obj = (Feature)super.clone();
//		} catch (CloneNotSupportedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return this;

	}
	
	public void clean() {
		if (pairList != null && !pairList.isEmpty()) {
			pairList.clear();
		}
		if (opinionMap != null && !opinionMap.isEmpty()) {
			opinionMap.clear();
		}
		containPairList = false;
		positivePairs = 0;
		negativePairs = 0;
		for (Object child : children) {
			Feature featureChild = (Feature)child;
			featureChild.clean();
		}
	}



	/**
	 * @param rootElement
	 */
	public void attachChildren(Element element)
	{
		// TODO Auto-generated method stub
		if(this.pairList!=null)
		{
			for(AttributeOpinionPair aop:pairList)
			{
				Element elementAOPair = element.addElement("AOPair");
				aop.attachAOPair(elementAOPair);
				
			}
		}
		if (children != null) 
		{
			for (Object child : children) 
			{
				Feature childFeature = (Feature)child;
				if(childFeature.containPairList)
				{
					Element subElement = element.addElement((String) childFeature.label);
					childFeature.attachChildren(subElement);
				}
			}
		}
		
	}

}

