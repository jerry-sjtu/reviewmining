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
import com.dianping.review.business.ReviewHight;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * TODO Comment of ShopAction
 * @author rui.xie
 *
 */
public class CopyOfShopReviewAction
{
	private int shopId;
	private String labelTag;
	private int ori;
	
	
	
	public int getOri() {
		return ori;
	}

	public void setOri(int ori) {
		this.ori = ori;
	}

	private ArrayList<ReviewHight> reviews;
	
	
	public ArrayList<ReviewHight> getReviews() {
		return reviews;
	}

	public void setReviews(ArrayList<ReviewHight> reviews) {
		this.reviews = reviews;
	}

	public String getLabelTag() {
		return labelTag;
	}

	public void setLabelTag(String labelTag) {
		this.labelTag = labelTag;
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
		System.out.println(shopId);
		System.out.println(labelTag);
		System.out.println(ori);
		List<AnalyzedReview> reviewList = new ArrayList<AnalyzedReview>(); 
		mongodb.useCollection("dpShopSummaryPairsLabel");
		DBObject obj = new BasicDBObject();
		obj.put("shopId", shopId);
		obj.put("label", labelTag);
		obj.put("ori", ori);
		DBCursor cursor = mongodb.find(obj);
		reviews = new ArrayList<ReviewHight>();
		ArrayList<AttributeOpinionPair> aopList = new ArrayList<AttributeOpinionPair>();
				
		try
		{
			while (cursor.hasNext())
			{
				DBObject entry = cursor.next();
				String label = (String)entry.get("label");
				String word = (String)entry.get("word");
				int ori = (Integer)entry.get("ori");
				String str = (String)entry.get("aop");
				String tokens[] = str.split("#");
				for(String token:tokens)
				{
					aopList.add(new AttributeOpinionPair(token));
					System.out.println("adding a pair");
					if(aopList.size()>=50)
						break;
				}
				if(aopList.size()>=50)
					break;
			}
			
			for(AttributeOpinionPair aop:aopList)
			{
				reviews.add(new ReviewHight(aop,mongodb));
			}
			
			
		} 
		finally
		{
			cursor.close();
		}
		
		return "success";
		
	}
}
