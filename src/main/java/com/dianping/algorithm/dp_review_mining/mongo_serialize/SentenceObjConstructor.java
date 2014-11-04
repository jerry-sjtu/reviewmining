/**
 * Project: review-mining-single
 * 
 * File Created at 2012-10-10
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

import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.datastruct.Sentence;
import com.dianping.algorithm.dp_review_mining.datastruct.SubSentence;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * TODO Comment of SentenceObjConstruction
 * @author rui.xie
 *
 */
public class SentenceObjConstructor implements DBObjectConstructor
{
	private static Logger LOGGER = Logger
			.getLogger(SentenceObjConstructor.class.getName());

	
	private Sentence sentence;
	
	public SentenceObjConstructor(Sentence sentence)
	{
		this.sentence = sentence;
	}
	
	
	/* (non-Javadoc)
	 * @see com.dianping.algorithm.dp_review_mining.mongo_serialize.DBObjectConstructor#constructMongoObj()
	 */
	public DBObject constructMongoObj()
	{
		// TODO Auto-generated method stub
		DBObject obj = new BasicDBObject();
		
		obj.put("ssNum",sentence.numSubsent);
		obj.put("wNum", sentence.numWord);
		obj.put("ewNum", sentence.numNVAD);
		
		ArrayList<DBObject> subsents = new ArrayList<DBObject>();

		int i=0;
		for(SubSentence s:sentence.subSents)
		{
			
			DBObject subSentenceObj = new SubSentenceObjConstructor(s).constructMongoObj();
			subSentenceObj.put("ssId", i);
			subsents.add(subSentenceObj);
			i++;
		}
		obj.put("subsents", subsents);
		return obj;
	}
}
