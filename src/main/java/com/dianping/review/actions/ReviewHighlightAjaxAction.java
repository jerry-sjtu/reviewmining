package com.dianping.review.actions;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.dianping.review.business.AnalyzedReview;
import com.dianping.review.dao.MongoDB;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
/**
 * Project: SpringSecurity
 * 
 * File Created at 2012-12-24
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

/**
 * TODO Comment of ReviewHighlightAjaxAction
 * @author rui.xie
 *
 */
public class ReviewHighlightAjaxAction
{
	private String pairs;
	public String getPairs()
	{
		return pairs;
	}
	public void setPairs(String pairs)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.pairs = pairs;
	}
	public String execute()
	{
		int firstBottom = pairs.indexOf('_');
		int secondBottom = pairs.indexOf('_', firstBottom+1);
		int rid = Integer.parseInt(pairs.substring(firstBottom+1, secondBottom));
		
		MongoDB mongo = new MongoDB();
		mongo.useCollection("dpFoodReview");
		DBObject obj = mongo.findOne(new BasicDBObject("rId", rid));
		AnalyzedReview reviewObj = new AnalyzedReview();
		reviewObj.loadFromDBOjbect(obj);
		String fullHigh = reviewObj.getContentWithFullHighlight(pairs);
		System.out.println(fullHigh);
		
		
		HttpServletResponse response = ServletActionContext.getResponse(); 
		response.setContentType("text/plain;charset=UTF-8");  
	     PrintWriter out;
		try {
			out = response.getWriter();
			out.write(fullHigh); 
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(pairs);
		return null;
	}
}
