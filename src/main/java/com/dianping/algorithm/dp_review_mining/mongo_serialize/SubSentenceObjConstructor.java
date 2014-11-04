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

import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.datastruct.SubSentence;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * TODO Comment of SubSentenceObjConstructor
 * @author rui.xie
 *
 */
public class SubSentenceObjConstructor implements DBObjectConstructor
{
	private static Logger LOGGER = Logger
			.getLogger(SubSentenceObjConstructor.class.getName());

	private SubSentence subSentence;
	public SubSentenceObjConstructor(SubSentence subSentence)
	{
		this.subSentence = subSentence;
	}
	
	/* (non-Javadoc)
	 * @see com.dianping.algorithm.dp_review_mining.mongo_serialize.DBObjectConstructor#constructMongoObj()
	 */
	public DBObject constructMongoObj()
	{
		// TODO Auto-generated method stub
		DBObject obj = new BasicDBObject();
		
		
		obj.put("wNum", subSentence.numWord);
		obj.put("ewNum", subSentence.numNVAD);
		StringBuilder content = new StringBuilder();
		for(int i=0;i<subSentence.numWord;++i)
		{
			content.append(subSentence.words[i]);
			content.append('/');
			content.append(subSentence.tagsIctclas[i]);
			if(i!=subSentence.numWord-1) content.append(' ');
		}
		obj.put("body", content.toString());
		return obj;
	}
}
