/**
 * Project: review-mining-single
 * 
 * File Created at 2012-10-12
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
package com.dianping.algorithm.dp_review_mining.mongo_serialize;


import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;



import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.datastruct.OriginalTaggedReview;

import com.dianping.algorithm.dp_review_mining.nlp.utility.ReviewProcessor;
import com.dianping.algorithm.dp_review_mining.utility.FReader;
import com.dianping.algorithm.dp_review_mining.utility.FileSystemOperation;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * TODO Comment of WordToMongo
 * @author rui.xie
 *
 */
public class WordToMongo
{
	private static Logger LOGGER = Logger
			.getLogger(WordToMongo.class.getName());
	

	private HashMap<Integer,Integer> idWordCount = new HashMap<Integer, Integer>();
	private HashMap<Integer,Integer> idDocumentFrequency = new HashMap<Integer, Integer>();
	private HashMap<String,Integer> wordId = new HashMap<String, Integer>();
	private List<DBObject> cachedObj;
	private int nextId = 0;
	
	private MongoDB mongo;
	
	public WordToMongo(MongoDB m, String collection)
	{
		mongo = m;
		m.useCollection(collection);
		
		cachedObj = new ArrayList<DBObject>();
	}
	
	public void processingDir(String ori_dir, String sep)
	{
		String filenames[] = FileSystemOperation.listFilenames(ori_dir);
		for(int i=filenames.length-1; i>=0; --i)
		{
			System.out.println(filenames[i]);
			
			processingFile(ori_dir+filenames[i], sep);
			
			// to comment;
			// break;
			
		}
		//to save hashmap to mongo
		for(String wordpos:wordId.keySet())
		{
			
			int indexOfSlash = wordpos.indexOf('/');
			if(indexOfSlash!=-1)
			{
				int wpid = wordId.get(wordpos);
				int wf = idWordCount.get(wpid);
				int df = idDocumentFrequency.get(wpid);
				String word = wordpos.substring(0,indexOfSlash);
				String pos = wordpos.substring(indexOfSlash+1);
				if(word.length()>0)
				{
					DBObject obj = new BasicDBObject();
					obj.put("wId", wpid);
					obj.put("wpos", wordpos);
					obj.put("w", word);
					obj.put("pos", pos);
					obj.put("df", df);
					obj.put("wf", wf);
					cachedObj.add(obj);
					if(cachedObj.size()==10000)
					{
						mongo.batchSaveDBObject(cachedObj);
						cachedObj.clear();
					}
				}
				
			}
			
		}
		if(cachedObj.size()>0)
		{
			mongo.batchSaveDBObject(cachedObj);
			cachedObj.clear();
		}
		
		mongo.createIndex(new BasicDBObject("wId",1));
		mongo.createIndex(new BasicDBObject("wpos",1));
	}

	
	
	public void processingFile(String ori, String sep)
	{
		FReader fr = new FReader(ori);
		
		String line = null;
		
		while((line=fr.readLine())!=null)
		{
			processingLine(line,ori,sep);
		}
		fr.close();
		
		
		
		
	}
	/**
	 * @param line
	 * @param fw
	 * @return
	 */
	protected void processingLine(String line,String ori, String sep)
	{
		
		String tokens[] = line.split(",");
		if(tokens.length==3)
		{
			OriginalTaggedReview otr = new OriginalTaggedReview();
			otr.loadReviewFromText(line, sep);
			otr.analyzeReview();
			
			if(otr.id!=-1)
			{
				String content = otr.getSimpleTagContent();
				String words[] = content.split(" ");
				HashSet<Integer> wordsInAReview = new HashSet<Integer>();
				for(String word:words)
				{
					int index = word.lastIndexOf('/');
					String withoutPos = "";
					if(index==-1)
					{
						
						System.out.println(word);
						System.out.println(otr.id);
						
						continue;
					}
					else
					{
						withoutPos = word.substring(0,index);
						String pos = word.substring(index+1);
						if(pos.equals(""))
						{
							System.out.println(word);
							System.out.println(otr.id);
						}
					}
					if(ReviewProcessor.IsChineseWord(withoutPos))
					{
						int wid;
						if(wordId.containsKey(word))
						{
							wid = wordId.get(word);
						}
						else
						{
							wordId.put(word, nextId);
							wid = nextId++;
						}
						wordsInAReview.add(wid);
						if(idWordCount.containsKey(wid))
						{
							int count = idWordCount.get(wid);
							idWordCount.put(wid, count+1);
						}
						else
						{
							idWordCount.put(wid, 1);
						}
					}
				}
				for(Integer wid:wordsInAReview)
				{
					if(idDocumentFrequency.containsKey(wid))
					{
						int count = idDocumentFrequency.get(wid);
						idDocumentFrequency.put(wid, count+1);
					}
					else
					{
						idDocumentFrequency.put(wid, 1);
					}
				}
			}
			
		}
			
			
		
	}
	
	public static void main(String args[])
	{
		String dir = "E:\\workspace\\rankingdata\\reviewContentTagged0903_MYSQL\\";
		MongoDB mongodb = new MongoDB();
		WordToMongo ftm = new WordToMongo(mongodb,"dpFoodWord");
		ftm.processingDir(dir, ",");
//		String line = "32709044,530211,一般/a ，/w 非/d 贵/a ，/w 还/d 没/d 得/ud 我们/r 菜市场/n 的/uj 烤兔/n 好吃/a ，/w 没意思/a ，/w 还/d 跑/v 那么/r 远/a ，/w 贵/a ，/w 不值/a 价格/n 、/w 唉/e ，/w 简直/d 有/v 点/q 后悔/v ，/w 兔头/n 一般/a ，/w 兔腿/n 巨/ng 贵/a ，/w 简直/d 不想/v 因/p （/w 拼/v 字数/n ）/w 。/wj ";
//		ftm.processingLine(line, "");
		
	}
}
