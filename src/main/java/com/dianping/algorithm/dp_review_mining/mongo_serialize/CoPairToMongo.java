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
import com.dianping.algorithm.dp_review_mining.utility.FReader;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;
import com.dianping.algorithm.dp_review_mining.utility.FileSystemOperation;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * TODO Comment of CoPairToMongo
 * @author rui.xie
 *
 */
public class CoPairToMongo
{
	private static Logger LOGGER = Logger.getLogger(CoPairToMongo.class
			.getName());
	
	
	private HashMap<Integer,String> idWord;
	
	
	private MongoDB mongo;
	
	private List<DBObject> cachedObj;
	
	
	public CoPairToMongo(MongoDB mongo,String wordCollection, String copairCollection)
	{
		
		idWord = new HashMap<Integer, String>();
		cachedObj = new ArrayList<DBObject>();
		
		this.mongo = mongo;
		mongo.useCollection(wordCollection);
		loadWordIdFromMongo();
		mongo.useCollection(copairCollection);
	}
	
	public void loadWordIdFromMongo()
	{
		DBCursor cursor = mongo.find(null, new String[]{"wId","wpos"});
		try 
		{
            while(cursor.hasNext()) 
            {
                DBObject entry = cursor.next();
                idWord.put((Integer)entry.get("wId"), (String)entry.get("wpos"));
            }
        } 
		finally 
		{
            cursor.close();
        }
	}
	
	public void processingDir(String ori_dir)
	{
		String filenames[] = FileSystemOperation.listFilenames(ori_dir);
		for(int i=0;i<filenames.length;++i)
		{
			
			System.out.println(filenames[i]);
			processingFile(ori_dir+filenames[i]);
			
		}
		if(cachedObj.size()>0)
		{
			mongo.batchSaveDBObject(cachedObj);
			System.out.println("last "+cachedObj.size()+" inserted");
			cachedObj.clear();
			
		}
		
		DBObject indexObj = new BasicDBObject();
		indexObj.put("hid",1);
		indexObj.put("tid",1);
		mongo.createIndex(indexObj);
		mongo.createIndex("hid");
		mongo.createIndex("tid");
		mongo.createIndex("dfpair");
		
	}
	
	public void processingFile(String ori)
	{
		
		
		FReader fr = new FReader(ori);
		
		String line = null;
		
		int i = 0;
		
		
		while((line=fr.readLine())!=null)
		{
			
			processingLine(line);
			
		}
		fr.close();
		
		
			
			

		
		
	}
	
	protected void processingLine(String line)
	{
		
		String ids[] = line.split("\\|");
		if(ids.length>=3)
		{
			int hid = Integer.parseInt(ids[0]);
			int tid = Integer.parseInt(ids[1]);
			int count = Integer.parseInt(ids[2]);
			if(count>20&&this.idWord.containsKey(hid)&&idWord.containsKey(tid))
			//if(count<=100&&count>5)
			{
				DBObject insertObj = new BasicDBObject();
				insertObj.put("hid",hid);
				insertObj.put("tid", tid);
				insertObj.put("pmi", 0);
				insertObj.put("dfpair", count);
				cachedObj.add(insertObj);
				if(cachedObj.size()==50000)
				{
					mongo.batchSaveDBObject(cachedObj);
					cachedObj.clear();
					System.out.println("50000 inserted");
				}
			}
			
			
		}
		
	}
	
	public static void main(String args[])
	{
		MongoDB mongodb = new MongoDB();
		CoPairToMongo cptm = new CoPairToMongo(mongodb,"dpFoodWord","dpFoodCoPair");
		String dir = "E:\\workspace\\rankingdata\\reviewShuffle\\";
		cptm.loadWordIdFromMongo();
		cptm.processingDir(dir);
		
	}
}
