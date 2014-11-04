/**
 * Project: SpringSecurity
 * 
 * File Created at 2012-12-31
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

import java.io.IOException;
import java.io.PrintWriter;

import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.dianping.review.business.AnalyzedReview;
import com.dianping.review.dao.MongoDB;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * TODO Comment of AlreadyPairs
 * @author rui.xie
 *
 */
public class AlreadyPairs
{
	private static TreeSet<String> alreadyPairs = new TreeSet<String>();
	public String execute()
	{
		
		MongoDB mongo = new MongoDB();
		mongo.useCollection("dpAOPairRepair");
		DBCursor cursor = mongo.find(null, new String[] { "reAttr", "reOpin", "reOri" });
		StringBuilder sb = new StringBuilder();
		try
		{
			while (cursor.hasNext())
			{
				DBObject entry = cursor.next();
				sb.append((String)entry.get("reAttr"));
				sb.append(" ");
				sb.append((String)entry.get("reOpin"));
				sb.append(" ");
				sb.append((String)entry.get("reOri"));
				alreadyPairs.add(sb.toString());
				sb.delete(0, sb.length());
			}
		} 
		finally
		{
			cursor.close();
		}
		mongo.useCollection("dpSupplementPair");
		cursor = mongo.find(null, new String[] { "attr", "opin", "ori" });
		
		try
		{
			while (cursor.hasNext())
			{
				DBObject entry = cursor.next();
				sb.append((String)entry.get("attr"));
				sb.append(" ");
				sb.append((String)entry.get("opin"));
				sb.append(" ");
				sb.append((String)entry.get("ori"));
				alreadyPairs.add(sb.toString());
				sb.delete(0, sb.length());
			}
		} 
		finally
		{
			cursor.close();
		}
		
		
		for(String item:alreadyPairs)
			sb.append("<a onclick=\"autoInput(this)\">").append(item).append("</a><br/>");
		HttpServletResponse response = ServletActionContext.getResponse(); 
		response.setContentType("text/plain;charset=UTF-8");  
	    PrintWriter out;
		try 
		{
			out = response.getWriter();
			out.write(sb.toString()); 
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return null;
	}
}
