/**
 * Project: SpringSecurity
 * 
 * File Created at 2012-12-17
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.review.actions;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.dianping.algorithm.dp_review_mining.associate_helper.AttributeOpinionPair;
import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.datastruct.AnalyzedReview;
import com.dianping.algorithm.dp_review_mining.summary.ReviewSummaryTree;
import com.dianping.algorithm.dp_review_mining.summary.Summarizer;
import com.dianping.review.business.LabelObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * TODO Comment of ShopAction
 * @author rui.xie
 *
 */
public class ShopAction
{
	private int shopId;
	private ArrayList<LabelObject> labels;
	
	
	public ArrayList<LabelObject> getLabels() {
		return labels;
	}

	public void setLabels(ArrayList<LabelObject> labels) {
		this.labels = labels;
	}

	public int getShopId()
	{
		return shopId;
	}

	public void setShopId(int shopId)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.shopId = shopId;
	}
	
	public String execute()
	{
		System.out.println("excute ShopAction.java");
		System.out.println("Summarizer.summarzierSingle is "+(Summarizer.summarzierSingle==null));
		List<HashMap<String, List<AttributeOpinionPair>>> list = Summarizer.summarzierSingle.getDishLabelFromMongo(shopId);
		System.out.println("size:"+ list.size());
		labels = new ArrayList<LabelObject>();
		int i=0;
		for(HashMap<String,List<AttributeOpinionPair>> labelList:list)
		{
			int ori = i==0?1:-1;
			++i;
			for(Entry<String,List<AttributeOpinionPair>> entry:labelList.entrySet())
			{
				if(entry.getValue().size()>=10)
					labels.add(new LabelObject(entry.getKey(), entry.getValue().get(0).opinion.opinion, ori, entry.getValue().size(), shopId));
			}
				
		}
		
		return "success";
		
	}
}
