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
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * TODO Comment of CoPairFeatures
 * @author rui.xie
 *
 */
public class CoPairFeatures
{
	private static Logger LOGGER = Logger.getLogger(CoPairFeatures.class
			.getName());
	
	private HashMap<Integer,HashMap<Integer,CoPair>> coPairInfoMap;
	
	public CoPairFeatures()
	{
		coPairInfoMap = new HashMap<Integer, HashMap<Integer,CoPair>>();
	}
	
	public CoPair getCoPair(int head,int tail)
	{
		if(coPairInfoMap.containsKey(head))
		{
			HashMap<Integer, CoPair> entry = coPairInfoMap.get(head);
			if(entry.containsKey(tail))
			{
				return entry.get(tail);
			}
		}
		return null;
	}
	
	public void loadCoPairFeatures(MongoDB mongo)
	{
		LOGGER.info("start to load copairfeature");
		mongo.useCollection("dpFoodCoPairInfo");
		DBCursor cursor = mongo.find(null);
		try
		{
			while (cursor.hasNext())
			{
				DBObject entry = cursor.next();
				CoPair cp = new CoPair();
				cp.loadFromDBObjectByCoPairInfo(entry);
				addPair(cp.getHead(), cp.getTail(), cp);
			}
		} 
		finally
		{
			cursor.close();
		}
		LOGGER.info("loading copairfeature finishes");
	}
	
	private void addPair(Integer first, Integer second, CoPair cp)
	{
		// TODO Auto-generated method stub

		HashMap<Integer, CoPair> entry = this.coPairInfoMap.get(first);
		if (entry == null)
		{
			entry = new HashMap<Integer, CoPair>();
			entry.put(second, cp);
			this.coPairInfoMap.put(first, entry);
		} 
		else
		{
			CoPair value = entry.get(second);
			if(value==null)
			{
				entry.put(second, cp);
			}
			else
			{
				System.err.println("it already has value");
			}
		}

	}
	
}
