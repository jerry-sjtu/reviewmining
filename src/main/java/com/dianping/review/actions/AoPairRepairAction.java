/**
 * Project: SpringSecurity
 * 
 * File Created at 2012-12-20
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
 * TODO Comment of AOPairRepairAction
 * @author rui.xie
 *
 */
public class AoPairRepairAction
{
	private String id;
	private String attr;
	private String opin;
	private String ori;
	
	public String getOri()
	{
		return ori;
	}

	public void setOri(String ori)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.ori = ori;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.id = id;
	}

	public String getAttr()
	{
		return attr;
	}

	public void setAttr(String attr)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.attr = attr;
	}

	public String getOpin()
	{
		return opin;
	}

	public void setOpin(String opin)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.opin = opin;
	}

	public String execute()
	{
        System.out.println(id+attr+" "+opin);
        String tokens[] = id.split("_");
        if(tokens.length==7)
        {
        	MongoDB mongo = new MongoDB();
        	mongo.useCollection("dpAOPairRepair");
        	DBObject obj = mongo.findOne(new BasicDBObject("idStr", id));
        	if(obj==null)
        		obj = new BasicDBObject();
        	obj.put("idStr", id);
        	obj.put("rId", Integer.parseInt(tokens[1]));
        	obj.put("sId", Integer.parseInt(tokens[2]));
        	obj.put("ssId", Integer.parseInt(tokens[3]));
        	obj.put("attrId", Integer.parseInt(tokens[4]));
        	obj.put("opinId", Integer.parseInt(tokens[5]));
        	obj.put("ori", Integer.parseInt(tokens[6]));
        	obj.put("reAttr", attr);
        	obj.put("reOpin", opin);
        	obj.put("reOri", ori);
        	mongo.saveDBObject(obj);
        }
        
		return "";
	}
}
