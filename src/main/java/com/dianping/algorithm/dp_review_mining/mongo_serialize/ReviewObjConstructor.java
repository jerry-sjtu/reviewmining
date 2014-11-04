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

import com.dianping.algorithm.dp_review_mining.datastruct.AnalyzedReview;
import com.dianping.algorithm.dp_review_mining.datastruct.OriginalTaggedReview;
import com.dianping.algorithm.dp_review_mining.datastruct.Sentence;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * TODO Comment of ReviewObjConstructor
 * @author rui.xie
 *
 */
public class ReviewObjConstructor implements DBObjectConstructor
{
	private static Logger LOGGER = Logger.getLogger(ReviewObjConstructor.class
			.getName());

	public OriginalTaggedReview otr;
	public AnalyzedReview ar;
	public ReviewObjConstructor(OriginalTaggedReview otr)
	{
		this.otr = otr;
		ar = otr.analyzedReview;
	}
	
	
	/* (non-Javadoc)
	 * @see com.dianping.algorithm.dp_review_mining.mongo_serialize.DBObjectConstructor#constructMongoObj()
	 */
	public DBObject constructMongoObj()
	{
		// TODO Auto-generated method stub
		if(otr.id==-1) return null;
		
		DBObject obj = new BasicDBObject();
		obj.put("rId", otr.id);
		obj.put("shopId", otr.shopId);
		
		obj.put("sNum", ar.numSentence);
		obj.put("ssNum",ar.numSubSentence);
		obj.put("wNum", ar.numWord);
		obj.put("ewNum", ar.numNVAD);
		
		ArrayList<DBObject> sents = new ArrayList<DBObject>();

		int i=0;
		for(Sentence s:ar.sentences)
		{
			
			DBObject sentenceObj = new SentenceObjConstructor(s).constructMongoObj();
			sentenceObj.put("sId", i);
			sents.add(sentenceObj);
			i++;
		}
		obj.put("sents", sents);
		return obj;
	}
}
