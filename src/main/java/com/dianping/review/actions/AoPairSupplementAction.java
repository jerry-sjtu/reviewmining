/**
 * Project: SpringSecurity
 * 
 * File Created at 2012-12-21
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
package com.dianping.review.actions;

import com.dianping.review.dao.MongoDB;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * TODO Comment of AoPairSupplementAction
 * @author rui.xie
 *
 */
public class AoPairSupplementAction
{
	private int id;
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.id = id;
	}
	public String getPairs()
	{
		return pairs;
	}
	public void setPairs(String pairs)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.pairs = pairs;
	}
	private String pairs;
	
	public String execute()
	{
		MongoDB mongo = new MongoDB();
		mongo.useCollection("dpSupplementPair");
		String tokens[] = pairs.split("\n");
		for(int i=0;i<tokens.length;++i)
		{
			if(tokens[i].length()>0)
			{
				String tokenPairs[] = tokens[i].split(" ");
				
				DBObject obj = new BasicDBObject();
				obj.put("rid", id);
				obj.put("attr", tokenPairs[0]);
				obj.put("opin", tokenPairs[1]);
				obj.put("ori", tokenPairs[2]);
				mongo.saveDBObject(obj);
			}
			
		}
		
		return null;
	}
}
