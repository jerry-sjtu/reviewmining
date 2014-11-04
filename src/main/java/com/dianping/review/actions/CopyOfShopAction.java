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
import java.util.List;

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
public class CopyOfShopAction
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
		MongoDB mongodb = new MongoDB();
		
		List<AnalyzedReview> reviewList = new ArrayList<AnalyzedReview>(); 
		mongodb.useCollection("dpShopSummaryPairsLabel");
		DBCursor cursor = mongodb.find(new BasicDBObject("shopId", shopId));
		labels = new ArrayList<LabelObject>();
		try
		{
			while (cursor.hasNext())
			{
				DBObject entry = cursor.next();
				String label = (String)entry.get("label");
				String word = (String)entry.get("word");
				int ori = (Integer)entry.get("ori");
				String str = (String)entry.get("aop");
				int num = str.split("#").length;
				labels.add(new LabelObject(label,word,ori,num,shopId));
			}
		} 
		finally
		{
			cursor.close();
		}
		
		return "success";
		
	}
}
