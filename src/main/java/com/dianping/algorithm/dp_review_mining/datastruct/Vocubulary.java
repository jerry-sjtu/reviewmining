/**
 * Project: review-mining-single
 * 
 * File Created at 2012-11-13
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
package com.dianping.algorithm.dp_review_mining.datastruct;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * TODO Comment of Vocubulary
 * @author rui.xie
 *
 */
public class Vocubulary
{
	private static Logger LOGGER = Logger.getLogger(Vocubulary.class.getName());
	
	private HashMap<String,Integer> wposToId;
	private HashMap<Integer,Word> idToWord;
	
	
	public Vocubulary()
	{
		wposToId = new HashMap<String, Integer>();
		idToWord = new HashMap<Integer, Word>();
	}
	
	public int getTermId(String wpos)
	{
		if(wposToId.containsKey(wpos))
		{
			return wposToId.get(wpos);
		}
		return -1;
	}
	
	public Word getWordById(int id)
	{
		if(idToWord.containsKey(id))
		{
			return idToWord.get(id);
		}
		return null;
	}
	
	public String getWposById(int id)
	{
		if(idToWord.containsKey(id))
		{
			return idToWord.get(id).wpos;
		}
		return null;
	}
	public void dumpVocubulary(String filepath)
	{
		FWriter fw = new FWriter(filepath);
		for(Integer id:idToWord.keySet())
		{
			fw.println(idToWord.get(id).toString());
		}
		fw.close();
	}
	
	public void loadVocubuarly(MongoDB mongo)
	{
		LOGGER.info("start to loading vocubualry");
		mongo.useCollection("dpFoodWord");
		DBCursor cursor = mongo.find(null);
		try
		{
			while (cursor.hasNext())
			{
				DBObject entry = cursor.next();
				Word word = new Word();
				word.wpos = (String) entry.get("wpos");
				word.wId = (Integer) entry.get("wId");
				word.wf =  (Integer) entry.get("wf");
				word.df = (Integer) entry.get("df");
				word.pos = ((String)entry.get("pos")).charAt(0);
				this.idToWord.put(word.wId, word);
				this.wposToId.put(word.wpos, word.wId);
			}
		} 
		finally
		{
			cursor.close();
		}
		LOGGER.info("loading vocubualry finish");
	}
	public static void main(String[] args)
	{
		Vocubulary voc = new Vocubulary();
		MongoDB mongo = new MongoDB();
		voc.loadVocubuarly(mongo);
		voc.dumpVocubulary("D:/vocabulary.txt");
	}
}
