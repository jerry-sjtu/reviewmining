/**   
* @Description: TODO
* @author weifu.du 
* @project review-mining-single
* @date Nov 22, 2012 
* @version V1.0
* 
* Copyright (c) 2010-2015 by Shanghai HanTao Information Co., Ltd.
 * All rights reserved.
*/
package com.dianping.algorithm.dp_review_mining.summary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.dianping.algorithm.dp_review_mining.associate_helper.AttributeOpinionPair;
import com.dianping.algorithm.dp_review_mining.datastruct.AnalyzedReview;
import com.dianping.algorithm.dp_review_mining.feature.Feature;
import com.dianping.algorithm.dp_review_mining.sentiment.SentimentLexicon;

public class ReviewSummaryTree
{
	
	public Feature root = null;
	public List<AttributeOpinionPair> pairList;
	public HashMap<Integer,AnalyzedReview> reviewMap;
	
	public ReviewSummaryTree(Feature featureTree, List<AttributeOpinionPair> pairList, List<AnalyzedReview> reviewList) 
	{
		// TODO Auto-generated constructor stub
		//root = featureTree;
		root = (Feature)featureTree.clone();
		root.clean();
		this.pairList = pairList;
//		reviewMap = new HashMap<Integer, AnalyzedReview>();
//		for(AnalyzedReview ar:reviewList)
//		{
//			reviewMap.put(ar.rId, ar);
//		}
	}
	
	public List<List<AttributeOpinionPair>> getReviewPairByLable(String label)
	{
		List<AttributeOpinionPair> resutlPairs = root.getSummaryOfLabel(root, label);
		List<AttributeOpinionPair> posPairs =  new ArrayList<AttributeOpinionPair>();
		List<AttributeOpinionPair> negPairs =  new ArrayList<AttributeOpinionPair>();
		for(AttributeOpinionPair aop:resutlPairs)
		{
			if(aop.orieantation == SentimentLexicon.POSITIVE)
				posPairs.add(aop);
			else if(aop.orieantation == SentimentLexicon.NEGATIVE)
				negPairs.add(aop);
		}
		List<List<AttributeOpinionPair>> res =  new ArrayList<List<AttributeOpinionPair>>();
		res.add(posPairs);
		res.add(negPairs);
		return res;
	}
	
	public Document SummaryTreeToXML()
	{
		Document document = DocumentHelper.createDocument();
		if(root.containPairList)
		{
			Element rootElement = document.addElement((String) root.label);
			root.attachChildren(rootElement);
		}
		return document;
	}

	public AnalyzedReview getReviewByAOPair(AttributeOpinionPair aopir) 
	{
		// TODO Auto-generated method stub
		return reviewMap.get(aopir.rId);
	}
	
	
}
