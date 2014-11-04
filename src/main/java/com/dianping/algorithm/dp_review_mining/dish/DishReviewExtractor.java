package com.dianping.algorithm.dp_review_mining.dish;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.summary.Summarizer;
import com.dianping.algorithm.dp_review_mining.utility.FReader;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class DishReviewExtractor 
{
	private Map<Integer,Set<String>> shopDishTags;
	private Map<Integer,Set<Integer>> shopReviewIds;
	private Map<Integer,Integer> reviewIdShopIds;
	private Set<Integer> allInMongoReviewIds;
	
	public DishReviewExtractor()
	{
		shopDishTags =  new HashMap<Integer,Set<String>>();
		shopReviewIds = new HashMap<Integer, Set<Integer>>();
		reviewIdShopIds = new HashMap<Integer, Integer>();
		allInMongoReviewIds =  new HashSet<Integer>();
	}
	
	public void loadMongoReviewId()
	{
		MongoDB mongodb = Summarizer.mongodb;
		mongodb.useCollection("dpFoodReview");
		int i=0;
		DBCursor cursor = mongodb.find(null,new String[]{"rId","shopId"});
		try
		{
			while (cursor.hasNext())
			{
				++i;
				if(i%80000==0)
					System.out.println("mongo:"+i);
				DBObject entry = cursor.next();
				int shopId = (Integer)entry.get("shopId");
				int reviewId = (Integer)entry.get("rId");
				if(this.shopReviewIds.containsKey(shopId))
				{
					this.allInMongoReviewIds.add(reviewId);
				}
				else
				{
					continue;
				}
				
//				System.out.println("shopID: " + shopID);
			}
		} 
		finally
		{
			cursor.close();
		}
	}
	
	public void loadDishTags(String filepath)
	{
		FReader fr = new FReader(filepath);
		String line = "";
		fr.readLine();
		while((line=fr.readLine())!=null)
		{
			String tokens[] = line.split(",");
			int shopid = Integer.parseInt(tokens[0]);
			Set<String> tagSet = shopDishTags.get(shopid);
			if(tagSet==null)
			{
				tagSet = new HashSet<String>();
				shopDishTags.put(shopid, tagSet);
			}
			for(int i=1;i<tokens.length;++i)
			{
				tagSet.add(tokens[i]);
			}
		}
		fr.close();
	}
	
	public void loadShopReviewIds(String filepath)
	{
		FReader fr = new FReader(filepath);
		String line = "";
		int i=0;
		
		while((line=fr.readLine())!=null)
		{
			++i;
			if(i%1000000==0)
				System.out.println(i);
			String tokens[] = line.split("\t");
			
			int reviewid = Integer.parseInt(tokens[0]);
			int shopid = Integer.parseInt(tokens[1]);
			
			if(!shopDishTags.containsKey(shopid))
				continue;
			
			Set<Integer> reviewIdSet = shopReviewIds.get(shopid);
			if(reviewIdSet==null)
			{
				reviewIdSet = new HashSet<Integer>();
				shopReviewIds.put(shopid, reviewIdSet);
			}
			reviewIdSet.add(reviewid);
			reviewIdShopIds.put(reviewid,shopid);
		}
		fr.close();
	}
	
	public void printDishTags()
	{
		System.out.println(shopDishTags.size());
		
//		for(Entry<Integer,Set<String>> entry:shopDishTags.entrySet())
//		{
//			System.out.print(entry.getKey()+":");
//			for(String tag:entry.getValue())
//			{
//				System.out.print(tag+",");
//			}
//			System.out.println();
//		}
	}
	
	public void printShopReviewIds()
	{
		System.out.println(shopReviewIds.size());
		System.out.println(reviewIdShopIds.size());
		System.out.println(allInMongoReviewIds.size());
//		for(Entry<Integer,Set<Integer>> entry:shopReviewIds.entrySet())
//		{
//			System.out.print(entry.getKey()+":");
//			for(Integer tag:entry.getValue())
//			{
//				System.out.print(tag+",");
//			}
//			System.out.println();
//		}
	}
	
	// 两个条件，一是在reviewIds里面，二是时间在2010年1月1日之后
	public void extractReview(String originPath, String outputDir, String name)
	{
		FReader fr = new FReader(originPath);
		String line = "";
		int i=0;
		int part = 1;
		FWriter fw = new FWriter(outputDir+name+part);
		while((line=fr.readLine())!=null)
		{
			
			String tokens[] = line.split("\t");
			int reviewid = Integer.parseInt(tokens[0]);
			if(!reviewIdShopIds.containsKey(reviewid))
				continue;
			String dateStr = tokens[2];
			if("2010-01-01".compareTo(dateStr)>0)
				continue;
			int shopid = reviewIdShopIds.get(reviewid);
			Set<String> tags = shopDishTags.get(shopid);
			boolean hasTag = false;
			for(String tag:tags)
			{
				if(tokens[1].contains(tag))
					hasTag = true;
			}
			if(!hasTag)
				continue;
			
			if(this.allInMongoReviewIds.contains(reviewid))
				continue;
			
			
			++i;
			String reviewContent = tokens[1].replaceAll("\ufffd\ufffd+", "。");
			reviewContent = reviewContent.replaceAll("\ufffd\ufffd", "，");
			fw.println(shopid+","+reviewid+","+reviewContent);
			if(i%100000==0)
			{
				part++;
				fw.close();
				fw = new FWriter(outputDir+name+part);
			}
		}
		System.out.println("No. of review:"+i);
		System.out.println("No. of file:"+part);
		fw.close();
		fr.close();
	}
	
	
	public static void main(String[] args) 
	{
		DishReviewExtractor dre = new DishReviewExtractor();
		String filepath =  "E:\\workspace\\review-analysis\\onlinebjandsh\\shoptags.csv";
		String fp = "E:\\workspace\\review-analysis\\onlinebjandsh\\reviewid_shopid.txt";
		String originPath = "E:\\workspace\\review-analysis\\onlinebjandsh\\review.txt";
		String outputDir = "E:\\workspace\\review-analysis\\onlinebjandsh\\dishtagreviews\\";
		dre.loadDishTags(filepath);
		dre.printDishTags();
		dre.loadShopReviewIds(fp);
		dre.loadMongoReviewId();
		dre.printShopReviewIds();
		dre.extractReview(originPath, outputDir, "review.part.");
	}

	public Set<String> getDishTags(Integer shopId) 
	{
		// TODO Auto-generated method stub
		Set<String> tags = this.shopDishTags.get(shopId);
		return tags;
		
	}

}
