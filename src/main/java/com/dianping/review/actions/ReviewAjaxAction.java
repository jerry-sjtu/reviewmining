/**
 * Project: SpringSecurity
 * 
 * File Created at 2012-12-19
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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;


import com.dianping.review.business.AnalyzedReview;
import com.dianping.review.dao.MongoDB;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * TODO Comment of ReviewAjaxAction
 * @author rui.xie
 *
 */
public class ReviewAjaxAction
{
	private String review;
	
	private int rid;
	private int sid;
	private int ssid;
	private int awi;
	private int owi;
	private int ori;
	
	
	public int getOri()
	{
		return ori;
	}

	public void setOri(int ori)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.ori = ori;
	}

	public String getReview()
	{
		return review;
	}

	public void setReview(String review)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.review = review;
	}
	
	public String execute()
	{
		System.out.println("reviewId is:"+this.rid);
		System.out.println(this.sid);
		
		MongoDB mongo = new MongoDB();
		mongo.useCollection("dpFoodReview");
		DBObject obj = mongo.findOne(new BasicDBObject("rId", rid));
		AnalyzedReview reviewObj = new AnalyzedReview();
		reviewObj.loadFromDBOjbect(obj);
		System.out.println(reviewObj.getContentWithHighlight(sid, ssid, awi, owi,ori));
		System.out.println(this.ssid);
		
		mongo.useCollection("dpReviewLog");
		DBObject objLog = new BasicDBObject("rId", rid);
		objLog.put("pairId", ""+sid+"_"+ssid+"_"+awi+"_"+owi+"_");
		
		mongo.saveDBObject(objLog);
		
		HttpServletResponse response = ServletActionContext.getResponse(); 
		response.setContentType("text/plain;charset=UTF-8");  
	     PrintWriter out;
		try {
			out = response.getWriter();
			out.write(reviewObj.getContentWithHighlight(sid, ssid, awi, owi,ori)); 
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return null;
		
	
	}

	public int getRid()
	{
		return rid;
	}

	public void setRid(int rid)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.rid = rid;
	}

	public int getSid()
	{
		return sid;
	}

	public void setSid(int sid)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.sid = sid;
	}

	public int getSsid()
	{
		return ssid;
	}

	public void setSsid(int ssid)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.ssid = ssid;
	}

	public int getAwi()
	{
		return awi;
	}

	public void setAwi(int awi)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.awi = awi;
	}

	public int getOwi()
	{
		return owi;
	}

	public void setOwi(int owi)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.owi = owi;
	}
}
