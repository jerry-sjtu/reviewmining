/**
 * Project: review-mining-single
 * 
 * File Created at 2012-10-9
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
package com.dianping.review.business;

import java.util.Scanner;

import org.apache.log4j.Logger;



/**
 * TODO Comment of Review
 * @author rui.xie
 *
 */
public class OriginalTaggedReview
{
	private static Logger LOGGER = Logger.getLogger(OriginalTaggedReview.class.getName());
	public int id;
	public int shopId;
	public String reviewTaggedContent;
	public String reviewOriginalContent;
	
	public AnalyzedReview analyzedReview;
	
	public OriginalTaggedReview()
	{
		id = -1;
		shopId = -1;
		reviewTaggedContent = "";
		analyzedReview = null;
		reviewOriginalContent = null;
	}
	
	
	
	/**
	 * @param string
	 * @return
	 */
	public String getOriginalContent(String string)
	{
		// TODO Auto-generated method stub
		if(string!=null)
			return string.replaceAll("/[^ ]* ?", "");
		return "";
				
		
	}

	public void loadReviewFromMongoDB()
	{
		
	}
	
	public void analyzeReview()
	{
		analyzedReview = new AnalyzedReview();
		//analyzedReview.loadFromOriginal(this);
	}
	
	
	
	
	
	public String toString()
	{
		return id+"|"+shopId+"|"+reviewTaggedContent;
	}
	
	
	public static void main(String args[])
	{
		
		
	
	}

	/**
	 * @return
	 */
	public String getSimpleTagContent()
	{
		// TODO Auto-generated method stub
		return this.analyzedReview.getSimpleTagContent();
	}
	
}
