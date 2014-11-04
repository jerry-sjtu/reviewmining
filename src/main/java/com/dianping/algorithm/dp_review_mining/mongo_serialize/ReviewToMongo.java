/**
 * Project: FeatureLib
 * 
 * File Created at 2012-9-11
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
import java.util.Date;

import org.apache.log4j.Logger;




import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.datastruct.OriginalTaggedReview;
import com.dianping.algorithm.dp_review_mining.utility.FReader;

import com.dianping.algorithm.dp_review_mining.utility.FileSystemOperation;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.apache.log4j.Logger;


/**
 * TODO Comment of DirectoryProcess
 * @author rui.xie
 *
 */
public class ReviewToMongo
{
	
	
	private static Logger logger = Logger.getLogger(ReviewToMongo.class.getName());
	
	private ArrayList<DBObject> cachedObj;
	private MongoDB mongo;
	private int numDoc;
	
	public ReviewToMongo(MongoDB m, String collection)
	{
		cachedObj = new ArrayList<DBObject>();
		mongo = m;
		mongo.useCollection(collection);
		numDoc = 0;
	}
	
	public void processingDir(String ori_dir, String sep)
	{
		String filenames[] = FileSystemOperation.listFilenames(ori_dir);
		for(int i=filenames.length-1; i>=0; --i)
		{
			processingFile(ori_dir+filenames[i], sep);
		}
		if(cachedObj.size()>0)
		{
			System.out.println("test");
			mongo.batchSaveDBObject(cachedObj);
			cachedObj.clear();
		}
		mongo.createIndex("rId");
		mongo.createIndex("shopId");
	}

	
	
	public void processingFile(String ori, String sep)
	{
		FReader fr = new FReader(ori);
		
		String line = null;
		
		while((line=fr.readLine())!=null)
		{
			processingLine(line,sep);
		}
		fr.close();
		
		
	}
	/**
	 * @param line
	 * @param fw
	 * @return
	 */
	protected void processingLine(String line, String sep)
	{
		
		OriginalTaggedReview otr = new OriginalTaggedReview();
		otr.loadReviewFromText(line,sep);
		otr.analyzeReview();
		ReviewObjConstructor roc = new ReviewObjConstructor(otr);
		DBObject obj = roc.constructMongoObj();
		
		if(obj!=null)
		{
			obj.put("origin", otr.reviewOriginalContent);
			cachedObj.add(obj);
			numDoc++;
		}
		if(cachedObj.size() == 50000)
		{
			mongo.batchSaveDBObject(cachedObj);
			cachedObj.clear();
			System.out.println(new Date()+"--"+numDoc);
		}
	}
	
	public static void main(String args[])
	{
		String dir = "E:\\workspace\\rankingdata\\reviewContentTagged0903_MYSQL\\";
		MongoDB mongodb = new MongoDB();
		
		
		ReviewToMongo ftm = new ReviewToMongo(mongodb,"dpFoodReview");
		ftm.processingDir(dir, ",");
		
	}
	
}
