/**
 * Project: review-mining-single
 * 
 * File Created at 2012-10-25
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
import java.util.List;

import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.utility.FReader;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * TODO Comment of SentimentWordComplement
 * @author rui.xie
 *
 */
public class SentimentWordComplement
{
	private static Logger LOGGER = Logger
			.getLogger(SentimentWordComplement.class.getName());
	
	private MongoDB mongo;
	private List<DBObject> cachedList;

	private int nextId;
	
	public SentimentWordComplement(MongoDB mongo)
	{
		this.mongo = mongo;
		cachedList = new ArrayList<DBObject>();
		nextId = getMaxId()+1;
		System.out.println("nextId:"+nextId);
	}
	
	/**
	 * @return
	 */
	private int getMaxId()
	{
		// TODO Auto-generated method stub
		mongo.useCollection("dpFoodSentimentLexicon");
		DBCursor cursor = mongo.find(null, new String[] { "wid", "wpos" });
		int max = -1;
		try
		{
			while (cursor.hasNext())
			{
				DBObject entry = cursor.next();
				Integer id = (Integer)entry.get("wid");
				if(id>max)
				{
					max = id;
				}

			}
			return max;
		} 
		finally
		{
			cursor.close();
		}
	}

	public void loadFromCSV(String filepath)
	{
		FReader fr = new FReader(filepath);
		String line = null;
		fr.readLine();
		while ((line = fr.readLine()) != null)
		{
			//to-do with the line;
			String tokens[] = line.split(",");
			System.out.println(tokens.length);
			System.out.println(line);
			int id = Integer.parseInt(tokens[0]);
			String wpos = tokens[1];
			int polarity = Integer.parseInt(tokens[2]);
			int human = 0;
			if(tokens.length==4)
			{
				human = 1;
			}
			addSentimentWord(wpos, human, id, polarity);
		}
		fr.close();
	}
	
	public void addSentimentWord(String wpos,int human,int id, int polarity)
	{
		DBObject obj = new BasicDBObject();
		obj.put("wid", nextId++);
		obj.put("wpos", wpos);
		int flashIndex = wpos.indexOf('/');
		String word = wpos.substring(0,flashIndex);
		String pos = wpos.substring(flashIndex+1);
		
		obj.put("word", word);
		obj.put("pos", pos);
		
		obj.put("human", human);
		obj.put("polarity", polarity);
		obj.put("idindic", id);
		System.out.println(obj);
		cachedList.add(obj);
	}
	
	public void batchSave()
	{
		mongo.useCollection("dpFoodSentimentLexicon");
		mongo.batchSaveDBObject(cachedList);
	}
	
	public static void main(String[] args)
	{
		MongoDB mongo = new MongoDB();
		SentimentWordComplement swc = new SentimentWordComplement(mongo);
		swc.loadFromCSV("C:\\Users\\rui.xie\\Desktop\\sentimentlexicon.csv");
		swc.batchSave();
	}
}
