/**
 * Project: review-mining-single
 * 
 * File Created at 2012-11-9
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
package com.dianping.algorithm.dp_review_mining.summary;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.datastruct.AnalyzedReview;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * TODO Comment of ReviewSampler
 * @author rui.xie
 *
 */
public class MongoUtility
{
	private static Logger LOGGER = Logger.getLogger(MongoUtility.class.getName());
	private MongoDB mongo;
	
	private HashMap<Integer,AnalyzedReview> sampleReviews;
	

	private HashSet<Integer> sampleIds;
	
	
	public MongoUtility(MongoDB mongodb)
	{
		mongo = mongodb;
		sampleReviews = new HashMap<Integer, AnalyzedReview>();
		sampleIds = new HashSet<Integer>();
	}
	
	public HashMap<Integer, AnalyzedReview> getSampleReviews()
	{
		return sampleReviews;
	}
	
	public void loadSampleIds(String ids)
	{
		sampleIds.clear();
		String idSets[] = ids.split(" ");
		for(String id:idSets)
			sampleIds.add(Integer.parseInt(id));
		
	}
	
	public String dumpSampleId()
	{
		StringBuilder sb = new StringBuilder();
		for(Integer rId:this.sampleIds)
		{
			
			sb.append(rId+" ");
		}
		sb.deleteCharAt(sb.length()-1);
		System.out.println(sb.toString());
		return sb.toString();
	}
	
	public void getReviewIdsByShopId(int shopId)
	{
		sampleIds.clear();
		mongo.useCollection("dpFoodReview");
		DBObject obj = new BasicDBObject();
		obj.put("shopId", shopId);
		DBCursor cursor = mongo.find(obj, new String[] { "rId" });
		try
		{
			while (cursor.hasNext())
			{
				DBObject entry = cursor.next();
				sampleIds.add((Integer)entry.get("rId"));
			}
		} 
		finally
		{
			cursor.close();
		}
		
	}
	
	
	public void getSampledIds(int sampleSize)
	{
		sampleIds.clear();
		mongo.useCollection("dpFoodReview");
		ArrayList<Integer> allReviewIds = new ArrayList<Integer>();
		DBCursor cursor = mongo.find(null, new String[] { "rId" });
		try
		{
			while (cursor.hasNext())
			{
				DBObject entry = cursor.next();
				allReviewIds.add((Integer)entry.get("rId"));
			}
		} 
		finally
		{
			cursor.close();
		}
		while(sampleIds.size()!=sampleSize)
		{
			double r = Math.random();
			int allSize = allReviewIds.size();
			int index = (int)(allSize*r);
			if(index<allSize)
			{
				Integer rId = allReviewIds.get(index);
				sampleIds.add(rId);
				System.out.println(sampleIds.size());
				
			}
		}
	}
	public void getSampledReviewBySampleId()
	{
		mongo.useCollection("dpFoodReview");
		for(Integer rId:this.sampleIds)
		{
			DBObject obj = mongo.findOne(new BasicDBObject("rId", rId));
			AnalyzedReview ar = new AnalyzedReview();
			ar.loadFromDBOjbect(obj);
			sampleReviews.put(rId, ar);
		}
	}
	public void dumpSampleReview()
	{
		for(Integer rId:this.sampleReviews.keySet())
		{
			AnalyzedReview review = sampleReviews.get(rId);
			System.out.println(review);
		}
	}
	
	public static Map<Integer,Integer> getShopsInfo()
	{
		MongoDB mongodb = new MongoDB();
		mongodb.useCollection("dpFoodReview");
		
		Map<Integer, Integer> shopReview = new HashMap<Integer,Integer>();
		
		DBCursor cursor = mongodb.find(null,new String[]{"shopId"});
		try
		{
			
			while (cursor.hasNext())
			{
				
				DBObject entry = cursor.next();
				int shopId = (Integer)entry.get("shopId");
				if(shopReview.containsKey(shopId))
				{
					int num = shopReview.get(shopId);
					shopReview.put(shopId, num+1);
				}
				else
				{
					shopReview.put(shopId, 1);
				}
			}
		} 
		finally
		{
			cursor.close();
		}
		return shopReview;
	
	}
	
	/**
	 * @return
	 */
	public static Set<Integer> getShopSumarized()
	{
		// TODO Auto-generated method stub
		MongoDB mongodb = new MongoDB();
		mongodb.useCollection("dpShopSummary");
		
		Set<Integer> shops = new HashSet<Integer>();
		
		DBCursor cursor = mongodb.find(null,new String[]{"shopId"});
		try
		{
			
			while (cursor.hasNext())
			{
				
				DBObject entry = cursor.next();
				int shopId = (Integer)entry.get("shopId");
				shops.add(shopId);
			}
		} 
		finally
		{
			cursor.close();
		}
		return shops;
	}
	
	
	public static void main(String[] args)
	{
		Map<Integer, Integer> map = MongoUtility.getShopsInfo();
		FWriter fw = new FWriter("D:/shopInfo.csv");
		for(Integer shopId:map.keySet())
		{
			int numReview = map.get(shopId);
			if(numReview>10)
				fw.println(""+shopId+','+numReview);
		}
		fw.close();
		
		
	}

	
	
}
