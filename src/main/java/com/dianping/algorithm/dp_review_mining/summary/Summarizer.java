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
package com.dianping.algorithm.dp_review_mining.summary;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;

import com.dianping.algorithm.dp_review_mining.associate.AttributeOpinionAssociator;
import com.dianping.algorithm.dp_review_mining.associate.OldAttributeOpinionAssociator;
import com.dianping.algorithm.dp_review_mining.associate_helper.AttributeOpinionPair;
import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.datastruct.AnalyzedReview;
import com.dianping.algorithm.dp_review_mining.dish.DishReviewExtractor;
import com.dianping.algorithm.dp_review_mining.evaluate.AOPairEvaluater;
import com.dianping.algorithm.dp_review_mining.feature.Feature;
import com.dianping.algorithm.dp_review_mining.feature.FeatureTree;
import com.dianping.algorithm.dp_review_mining.sentiment.SentimentLexicon;
import com.dianping.algorithm.dp_review_mining.utility.SummaryTreePrinter;
import com.dianping.algorithm.dp_review_mining.utility.TextTreeViewPrinter;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;


public class Summarizer 
{
	
	private String category = null;
	public static MongoDB mongodb = new MongoDB();
	private FeatureTree featureTree = null;
//	private AttributeOpinionAssociator associator = new OldAttributeOpinionAssociator();
	private Set<Integer> shopidSet = new HashSet<Integer>();
	private List<DBObject> cachedTree =  new ArrayList<DBObject>();
	
	public List<AttributeOpinionAssociator> associatorList = new ArrayList<AttributeOpinionAssociator>();
	
	public List<AOPairEvaluater> evaluaterList = new ArrayList<AOPairEvaluater>();
	
	private static Logger LOGGER = Logger.getLogger(Summarizer.class.getName());
	
	/**
	 * 外部接口，返回商户的评论摘要
	 * @param shopID
	 * @return
	 */
	public String getSummary(int shopID) 
	{
		return null;
	}

	/**
	 * 内部接口，对所有商户进行摘要
	 */
	public void summarizeALlShop() {
		for (Integer shopID: shopidSet) {
			try {
				summarize(shopID, "./summary-results/" + shopID +  "-batch.txt");
				//System.out.println(shopID + " has been summarized.");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void summarize(int shopID, String outputFile) throws FileNotFoundException {
		List<AnalyzedReview> reviewList = getAllReviews(shopID);
		List<AttributeOpinionPair> pairList = getAllPairs(reviewList);
		List<AttributeOpinionPair> qualifiedPairList = filter(pairList);
		ReviewSummaryTree summary = getReviewSummary(qualifiedPairList,reviewList);
//		saveSummary2Mongo(summary);
		TextTreeViewPrinter printer = new SummaryTreePrinter();

		File file = new File(outputFile);
		
		PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
		printer.printTreeXML(summary.root, out);
		
		out.close();
		
	}
	
	public List<AttributeOpinionPair> filter(List<AttributeOpinionPair> aopList) {
		List<AttributeOpinionPair> cleanedList = new LinkedList<AttributeOpinionPair>();
		Set<String> set = new HashSet<String>();
		for (AttributeOpinionPair aop : aopList) {
			String key = aop.attr.attribute+"_"+aop.opinion.opinion+"_"+aop.orieantation+"_"+aop.rId;
			if(set.contains(key) || aop.score < 0.5)
			{
				continue;
			}
			else
			{
				set.add(key);
				cleanedList.add(aop);
			}
		}
		return cleanedList;
	}
	
	public Document summarize(int shopID)
	{
		List<AnalyzedReview> reviewList = getAllReviews(shopID);
		List<AttributeOpinionPair> pairList = getAllPairs(reviewList);
		List<AttributeOpinionPair> qualifiedPairList = filter(pairList);
		ReviewSummaryTree summary = getReviewSummary(qualifiedPairList,reviewList);
		return summary.SummaryTreeToXML();
	}
	
	public List<AttributeOpinionPair> summarizePairs(int shopID)
	{
		List<AnalyzedReview> reviewList = getAllReviews(shopID);
		List<AttributeOpinionPair> pairList = getAllPairs(reviewList);
		List<AttributeOpinionPair> qualifiedPairList = filter(pairList);
		return qualifiedPairList;
	}
	
	public ReviewSummaryTree getSummaryTree(int shopID)
	{
		List<AnalyzedReview> reviewList = getAllReviews(shopID);
		List<AttributeOpinionPair> pairList = getAllPairs(reviewList);
		List<AttributeOpinionPair> qualifiedPairList = filter(pairList);
		
		ReviewSummaryTree summary = getReviewSummary(qualifiedPairList,reviewList);
		return summary;
	}
	public ReviewSummaryTree getSummaryTreeFromMongo(int shopID)
	{
		//List<AnalyzedReview> reviewList = getAllReviews(shopID);
		//System.out.println(shopID);
		List<AttributeOpinionPair> pairList = getAllPairsFromMongo(shopID);
		List<AttributeOpinionPair> qualifiedPairList = filter(pairList);
		
		ReviewSummaryTree summary = getReviewSummary(qualifiedPairList,null);
		return summary;
	}  
	private List<AttributeOpinionPair> getAllPairsFromMongo(int shopID) 
	{
		// TODO Auto-generated method stub
		mongodb.useCollection("dpShopSummaryPairs");
		DBCursor cursor = mongodb.find(new BasicDBObject("shopId", shopID));
		try
		{
			List<AttributeOpinionPair> aopairs = new ArrayList<AttributeOpinionPair>();
			while (cursor.hasNext())
			{
				DBObject entry = cursor.next();
				String aops = (String)entry.get("aop");
				String tokens[] = aops.split("#");
				for(String token:tokens)
				{
					AttributeOpinionPair aop = new AttributeOpinionPair(token);
					aopairs.add(aop);
				}
				//System.out.println(review.getSimpleTagContent());
			}
			return aopairs;
			
		}
		finally
		{
			cursor.close();
		}
		 

		
	}
	
	
	private List<AttributeOpinionPair> getDishPairsFromMongo(int shopID) 
	{
		// TODO Auto-generated method stub
		DBCursor cursor = null;
		try
		{
			mongodb.useCollection("dpShopSummaryDishPairs");
			cursor = mongodb.find(new BasicDBObject("shopId", shopID));
			List<AttributeOpinionPair> aopairs = new ArrayList<AttributeOpinionPair>();
			while (cursor.hasNext())
			{
				DBObject entry = cursor.next();
				String aops = (String)entry.get("aop");
				String tokens[] = aops.split("#");
				for(String token:tokens)
				{
					AttributeOpinionPair aop = new AttributeOpinionPair(token);
					aopairs.add(aop);
				}
				//System.out.println(review.getSimpleTagContent());
			}
			
			mongodb.useCollection("dpShopSummaryPairs");
			cursor = mongodb.find(new BasicDBObject("shopId", shopID));
			while (cursor.hasNext())
			{
				DBObject entry = cursor.next();
				String aops = (String)entry.get("aop");
				String tokens[] = aops.split("#");
				for(String token:tokens)
				{
					AttributeOpinionPair aop = new AttributeOpinionPair(token);
					aopairs.add(aop);
				}
			}
			return aopairs;
			
		}
		finally
		{
			cursor.close();
		}
		
		
		 

		
	}
	
	
	public void summarizeLabel(int shopId)
	{
		
	}
	

	private List<AnalyzedReview> getAllReviews(int shopID) 
	{
		List<AnalyzedReview> reviewList = new ArrayList<AnalyzedReview>(); 
		mongodb.useCollection("dpFoodReview");
		DBCursor cursor = mongodb.find(new BasicDBObject("shopId", shopID));
		try
		{
			while (cursor.hasNext())
			{
				DBObject entry = cursor.next();
				AnalyzedReview review = new AnalyzedReview();
				review.loadFromDBOjbect(entry);
				reviewList.add(review);
//				System.out.println(review.getSimpleTagContent());
			}
		} 
		finally
		{
			cursor.close();
		}
		return reviewList;
	}
	
	private List<AttributeOpinionPair> getAllPairs(List<AnalyzedReview> reviewList) {
		List<AttributeOpinionPair> pairList = new ArrayList<AttributeOpinionPair>();
		for (AnalyzedReview review: reviewList) 
		{
			//System.out.println(review.rId);
			for (AttributeOpinionAssociator associator : associatorList) {
				List<AttributeOpinionPair> tmpPairList = associator.associate(review);
				if(tmpPairList.size()>0)
					pairList.addAll(tmpPairList);
			}
		}
		return pairList;
	}
	
	private ReviewSummaryTree getReviewSummary(List<AttributeOpinionPair> pairList, List<AnalyzedReview> reviewList) 
	{
		ReviewSummaryTree summary = new ReviewSummaryTree(featureTree.root,pairList,reviewList);
		int num = 0;
		for (AttributeOpinionPair attributeOpinionPair : pairList) 
		{
			++num;
			if ((attributeOpinionPair.orieantation != SentimentLexicon.POSITIVE) && (attributeOpinionPair.orieantation != SentimentLexicon.NEGATIVE)) {
				continue;
			}
			String featureStr = attributeOpinionPair.attr.attribute;
			Feature feature = Feature.findNode(summary.root, featureStr, true);
			if (feature == null) {
				continue;
			}
			if (feature.pairList == null) {
				feature.pairList = new ArrayList<AttributeOpinionPair>();
			}
			feature.pairList.add(attributeOpinionPair);
			String opinion = attributeOpinionPair.getOpinModifierString() + attributeOpinionPair.opinion.opinion;
			if (feature.opinionMap.containsKey(opinion)) {
				feature.opinionMap.put(opinion, feature.opinionMap.get(opinion) + 1);
			} else {
				feature.opinionMap.put(opinion, 1);
			}
			while (feature != null) {
				if (attributeOpinionPair.orieantation == SentimentLexicon.POSITIVE) {
					feature.positivePairs++;
				} else if (attributeOpinionPair.orieantation == SentimentLexicon.NEGATIVE) {
					feature.negativePairs++;
				}
				feature.containPairList = true;
				feature = (Feature)feature.parTree;
			}
		}
		return summary;
	}
	
	private void saveSummary2Mongo(Document treeDom,int shopId) 
	{
		
		DBObject obj = new BasicDBObject();
		obj.put("shopId", shopId);
		obj.put("t", treeDom.asXML());
		cachedTree.add(obj);
	}
	
	public void getAllShopIDs() 
	{
		mongodb.useCollection("dpFoodReview");
		
		DBCursor cursor = mongodb.find(null);
		try
		{
			while (cursor.hasNext() && shopidSet.size() < 500)
			{
				DBObject entry = cursor.next();
				int shopID = (Integer)entry.get("shopId");
				shopidSet.add(shopID);
//				System.out.println("shopID: " + shopID);
			}
		} 
		finally
		{
			cursor.close();
		}
	
	}
	
	public Summarizer(String category) {
		// TODO Auto-generated constructor stub
		this.category = category;
		featureTree = new FeatureTree(category);
		initAssociatorList();
		initEvaluterList();
	}
	
	public Summarizer(String feature, String featureSynonym) {
		// TODO Auto-generated constructor stub
		this.category = category;
		featureTree = new FeatureTree(feature, featureSynonym);
		initAssociatorList();
		initEvaluterList();
	}
	
	private void initEvaluterList() 
	{
			
			//getAllClassWithinSamePackage(new AOPairEvaluater(), evaluaterList);
			evaluaterList.add(new AOPairEvaluater());
	}
	
	private void initAssociatorList() 
	{
			associatorList.add(new OldAttributeOpinionAssociator());
			OldAttributeOpinionAssociator.init();
			//getAllClassWithinSamePackage(new AttributeOpinionAssociator(), associatorList);
	}

	private static void getAllClassWithinSamePackage(Object obj, List destList) {
		String currentPath = obj.getClass().getResource(".").getPath();
		String classFullName = obj.getClass().getName();
		String classSimpleName = obj.getClass().getSimpleName();
		String packageName = classFullName.substring(0, classFullName.length() - classSimpleName.length());
		File root = new File(currentPath);
		String[] allClasses = root.list();
		for (String filename : allClasses) {
			String classname = filename.substring(0, filename.length() - ".class".length());
			classname = packageName + classname;
			try {
				Class currClass = Class.forName(classname);
				Object cls_obj = currClass.newInstance();
//				obj.initialize();
				destList.add(cls_obj);
				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void summarizeShopToMongo(int reviewNo)
	{
		Map<Integer, Integer> map = MongoUtility.getShopsInfo();
		Set<Integer> alreadyShop = MongoUtility.getShopSumarized();
		
		System.out.println(map.size());
		cachedTree.clear();
		
		for(Integer shopId:map.keySet())
		{
			if(alreadyShop.contains(shopId))
				continue;
			//System.out.println(shopId);
			int reviewNum = map.get(shopId);
			if(reviewNum > reviewNo)
			{
				Document domTree = this.summarize(shopId);
				saveSummary2Mongo(domTree, shopId);
			}
			if(cachedTree.size()==100)
			{
				mongodb.useCollection("dpShopSummary");
				mongodb.batchSaveDBObject(cachedTree);
				cachedTree.clear();
				LOGGER.info("another 100 shops have been summarized");
			}
		}
		if(cachedTree.size()!=0)
		{
			mongodb.useCollection("dpShopSummary");
			mongodb.batchSaveDBObject(cachedTree);
			LOGGER.info("last "+cachedTree.size()+ " shops have been summarized");
			cachedTree.clear();
		}
	}
	
	public void summarizeShopPairsToMongo(int reviewNo, Map<Integer, Integer> map)
	{
		System.out.println(map.size());
		cachedTree.clear();
		
		for(Integer shopId:map.keySet())
		{
			
			//System.out.println(shopId);
			int reviewNum = map.get(shopId);
			if(reviewNum > reviewNo)
			{
				List<AttributeOpinionPair> paris = this.summarizePairs(shopId);
				DBObject obj = saveSummaryPairs2Mongo(paris, shopId);
				System.out.println(obj);
			}
			if(cachedTree.size()==100)
			{
				mongodb.useCollection("dpShopSummaryPairs");
				mongodb.batchSaveDBObject(cachedTree);
				cachedTree.clear();
				LOGGER.info("another 100 shops have been summarized");
			}
		}
		if(cachedTree.size()!=0)
		{
			mongodb.useCollection("dpShopSummaryPairs");
			mongodb.batchSaveDBObject(cachedTree);
			LOGGER.info("last "+cachedTree.size()+ " shops have been summarized");
			cachedTree.clear();
		}
		mongodb.createIndex("shopId");
	}
	
	
	
	public void summarizeShopPairsToMongo(int reviewNo)
	{
		Map<Integer, Integer> map = MongoUtility.getShopsInfo();
		//Set<Integer> alreadyShop = MongoUtility.getShopSumarized();
		
		
		//for dish tags
		
		
		
		
		
		System.out.println(map.size());
		cachedTree.clear();
		
		for(Integer shopId:map.keySet())
		{
			
			//System.out.println(shopId);
			int reviewNum = map.get(shopId);
			if(reviewNum > reviewNo)
			{
				List<AttributeOpinionPair> paris = this.summarizePairs(shopId);
				saveSummaryPairs2Mongo(paris, shopId);
			}
			if(cachedTree.size()==100)
			{
				mongodb.useCollection("dpShopSummaryPairs");
				mongodb.batchSaveDBObject(cachedTree);
				cachedTree.clear();
				LOGGER.info("another 100 shops have been summarized");
			}
		}
		if(cachedTree.size()!=0)
		{
			mongodb.useCollection("dpShopSummaryPairs");
			mongodb.batchSaveDBObject(cachedTree);
			LOGGER.info("last "+cachedTree.size()+ " shops have been summarized");
			cachedTree.clear();
		}
		mongodb.createIndex("shopId");
	}
	
	
	public void summarizeShopPairsLabelToMongo(int reviewNo)
	{
		Map<Integer, Integer> map = MongoUtility.getShopsInfo();
		//Set<Integer> alreadyShop = MongoUtility.getShopSumarized();
		
		System.out.println(map.size());
		cachedTree.clear();
		
		for(Integer shopId:map.keySet())
		{
			
			//System.out.println(shopId);
			int reviewNum = map.get(shopId);
			if(reviewNum > reviewNo)
			{
				ReviewSummaryTree tree = this.getSummaryTreeFromMongo(shopId);
				String lables[] = {"价格","环境","装修","交通","服务","量","口味","菜品","甜品"};
				
				StringBuilder sb = new StringBuilder();
				int i = 0;
				for(String label:lables)
				{
					
					List<List<AttributeOpinionPair>> list = tree.getReviewPairByLable(label);
					List<AttributeOpinionPair> posList = list.get(0);
					List<AttributeOpinionPair> negList = list.get(1);
					
					if(posList.size()>10)
					{
						Collections.sort(posList);
						String word = getWord(posList);
						saveSummaryLabelPairs2Mongo(posList, label, word, 1, shopId);
					}
					if(negList.size()>10)
					{
						Collections.sort(negList);
						String word = getWord(negList);
						saveSummaryLabelPairs2Mongo(negList, label, word, -1, shopId);
					}
					++i;
				}
				
				
				
				
			}
			if(cachedTree.size()>=100)
			{
				int size = cachedTree.size();
				mongodb.useCollection("dpShopSummaryPairsLabel");
				mongodb.batchSaveDBObject(cachedTree);
				cachedTree.clear();
				LOGGER.info("another "+size+" shops have been summarized");
			}
		}
		if(cachedTree.size()!=0)
		{
			mongodb.useCollection("dpShopSummaryPairsLabel");
			mongodb.batchSaveDBObject(cachedTree);
			LOGGER.info("last "+cachedTree.size()+ " shops have been summarized");
			cachedTree.clear();
		}
		mongodb.createIndex("shopId");
	}
	
	
	public List<HashMap<String, List<AttributeOpinionPair>>> getDishLabelFromMongo(int shopId)
	{
		System.out.println(shopId);
		List<AttributeOpinionPair> aopList = getDishPairsFromMongo(shopId);
		HashMap<String,List<AttributeOpinionPair>> dishPospair = new HashMap<String, List<AttributeOpinionPair>>();
		HashMap<String,List<AttributeOpinionPair>> dishNegpair = new HashMap<String, List<AttributeOpinionPair>>();
		List<HashMap<String,List<AttributeOpinionPair>>> list = new ArrayList<HashMap<String,List<AttributeOpinionPair>>>();
		for(AttributeOpinionPair aop:aopList)
		{
			if(aop.orieantation==SentimentLexicon.POSITIVE)
				addToMap(dishPospair,aop);
			else if(aop.orieantation==SentimentLexicon.NEGATIVE)
				addToMap(dishNegpair,aop);
		}
		list.add(dishPospair);
		list.add(dishNegpair);
		return list;
	}
	
	
	
	
	
	private void addToMap(
			HashMap<String, List<AttributeOpinionPair>> dishPair,
			AttributeOpinionPair aop) 
	{
		// TODO Auto-generated method stub
		List<AttributeOpinionPair> aoplist = dishPair.get(aop.attr.attribute);
		if(aoplist==null)
		{
			aoplist = new ArrayList<AttributeOpinionPair>();
			dishPair.put(aop.attr.attribute, aoplist);
		}
		aoplist.add(aop);
	}

	public void summarizeShopPairsDishToMongo(int reviewNo)
	{
		Map<Integer, Integer> map = MongoUtility.getShopsInfo();
		//Set<Integer> alreadyShop = MongoUtility.getShopSumarized();
		DishReviewExtractor dre = new DishReviewExtractor();
		String filepath =  "E:\\workspace\\review-analysis\\onlinebjandsh\\shoptags.csv";
		dre.loadDishTags(filepath);
		
		
		
		System.out.println(map.size());
		cachedTree.clear();
		
		int noOfShop = 0;
		int noOfTargetShop = 0;
		for(Integer shopId:map.keySet())
		{
			
			//System.out.println(shopId);
			int reviewNum = map.get(shopId);
			if(reviewNum > reviewNo)
			{
				++noOfShop;
				System.out.println("noOfShop:"+noOfShop);
				List<AttributeOpinionPair> aopList = this.getAllPairsFromMongo(shopId);
				Set<String> labels = dre.getDishTags(shopId);
				if(labels==null)
					continue;
				
				++noOfTargetShop;
				System.out.println("noOfTargetShop:"+noOfTargetShop);
				
				List<AttributeOpinionPair> filteredAopList = new ArrayList<AttributeOpinionPair>();
				StringBuilder sb = new StringBuilder();
				int i = 0;
				for(AttributeOpinionPair aop:aopList)
				{
					if(labels.contains(aop.attr.attribute))
					{
						filteredAopList.add(aop);
					}
				}
				saveSummaryPairs2Mongo(filteredAopList,shopId);
				
			}
			if(cachedTree.size()>=100)
			{
				int size = cachedTree.size();
				mongodb.useCollection("dpShopSummaryDishPairs");
				mongodb.batchSaveDBObject(cachedTree);
				cachedTree.clear();
				LOGGER.info("another "+size+" shops have been summarized");
			}
		}
		if(cachedTree.size()!=0)
		{
			mongodb.useCollection("dpShopSummaryDishPairs");
			mongodb.batchSaveDBObject(cachedTree);
			LOGGER.info("last "+cachedTree.size()+ " shops have been summarized");
			cachedTree.clear();
		}
		mongodb.createIndex("shopId");
	}
	
	
	public String getWord(List<AttributeOpinionPair> list) 
	{
		// TODO Auto-generated method stub
		HashMap<String,Integer> wordCount = new HashMap<String, Integer>();
		for(AttributeOpinionPair aop:list)
		{
			if(wordCount.containsKey(aop.opinion.opinion))
			{
				int wc = wordCount.get(aop.opinion.opinion);
				wordCount.put(aop.opinion.opinion, wc+1);
			}
			else
			{
				wordCount.put(aop.opinion.opinion, 1);
			}
		}
		String maxkey = "";
		int maxNum = Integer.MIN_VALUE;
		for(Entry<String,Integer> entry:wordCount.entrySet())
		{
			String key = entry.getKey();
			int count =  entry.getValue();
			if(count > maxNum)
			{
				maxNum = count;
				maxkey = key;
			}
		}
		return maxkey;
	}

	private DBObject saveSummaryPairs2Mongo(List<AttributeOpinionPair> paris,
			Integer shopId) {
		// TODO Auto-generated method stub
		DBObject obj = new BasicDBObject();
		obj.put("shopId", shopId);
		
		String fieldSeperator = "@";
		String pairSeperator = "#";
		StringBuilder sb = new StringBuilder();
		
		for(AttributeOpinionPair pair:paris)
		{
			sb.append(pair.toMongoString(fieldSeperator)+pairSeperator);
		}
		
		obj.put("aop", sb.toString());
		cachedTree.add(obj);
		return obj;
	}
	
	private void saveSummaryLabelPairs2Mongo(List<AttributeOpinionPair> paris, String label, String word,int orientation, 
			Integer shopId) {
		// TODO Auto-generated method stub
		DBObject obj = new BasicDBObject();
		obj.put("shopId", shopId);
		obj.put("label", label);
		obj.put("word", word);
		obj.put("ori", orientation);
		String fieldSeperator = "@";
		String pairSeperator = "#";
		StringBuilder sb = new StringBuilder();
		
		for(AttributeOpinionPair pair:paris)
		{
			sb.append(pair.toMongoString(fieldSeperator)+pairSeperator);
		}
		
		obj.put("aop", sb.toString());
		cachedTree.add(obj);
	}
	
	

	public static void main(String[] args) throws FileNotFoundException 
	{
		
		//================= test summarizer ===============================================================
		Summarizer summarizer = new Summarizer("food");
		summarizer.summarizeShopPairsToMongo(20);
		summarizer.summarizeShopPairsDishToMongo(20);
		
//				int shopID = 535345;
//////		Document domTree = summarizer.summarize(535345);
		//summarizer.summarizeShopToMongo(10);
//////		System.out.println(domTree.asXML());
////
//////		summarizer.summarizeALlShop();
////		
//		ReviewSummaryTree tree = summarizer.getSummaryTreeFromMongo(shopID);
////		
//		Scanner sc = new Scanner(System.in);
//		while(true)
//		{
//			System.out.println("shopid(-1 to exit)");
//			shopID = sc.nextInt();
//			if(shopID==-1)
//				break;
//			Summarizer sm = new Summarizer("food");
//			tree = sm.getSummaryTreeFromMongo(shopID);
//			String lables[] = {"价格","环境","装修","交通","服务","量","口味","菜品","甜品"};
//			for(String label:lables)
//			{
//				List<List<AttributeOpinionPair>> list = tree.getReviewPairByLable(label);
//				List<AttributeOpinionPair> posList = list.get(0);
//				List<AttributeOpinionPair> negList = list.get(1);
//				Collections.sort(posList);
//				Collections.sort(negList);
//				System.out.println(label+"good");
//				for(AttributeOpinionPair aopir:posList)
//				{
//					System.out.println("\t"+aopir.toString()+tree.getReviewByAOPair(aopir).getContentWithoutTag());
//				}
//				System.out.println(label+"bad");
//				for(AttributeOpinionPair aopir:negList)
//				{
//					System.out.println("\t"+aopir.toString()+tree.getReviewByAOPair(aopir).getContentWithoutTag());
//				}
//				
//			}
//		}
//		================================================================================
//		List aopeList = new ArrayList<AOPairEvaluater>();
//		Summarizer.getAllClassWithinSamePackage(new AOPairEvaluater(), aopeList);
	}
	
	public static Summarizer summarzierSingle = new Summarizer("food");

	public List<AttributeOpinionPair> getTargetDishPairFromMongo(int shopId,
			String labelTag, int ori) 
	{
		// TODO Auto-generated method stub
		
		List<AttributeOpinionPair> aopList = getDishPairsFromMongo(shopId);
		List<AttributeOpinionPair> filterAopList = new ArrayList<AttributeOpinionPair>();
		if(ori==1)
			ori=SentimentLexicon.POSITIVE;
		else if(ori==-1)
			ori=SentimentLexicon.NEGATIVE;
		for(AttributeOpinionPair aop:aopList)
		{
			if(aop.attr.attribute.equals(labelTag) &&aop.orieantation==ori )
				filterAopList.add(aop);
			
		}
		return filterAopList;
	}
}
