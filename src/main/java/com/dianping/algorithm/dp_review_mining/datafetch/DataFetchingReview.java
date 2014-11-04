/**
 * Project: rankReview
 * 
 * File Created at 2012-7-26
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
package com.dianping.algorithm.dp_review_mining.datafetch;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;


import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.dataAccessLayer.RDBConnection;
import com.dianping.algorithm.dp_review_mining.nlp.utility.ReviewProcessor;
import com.dianping.algorithm.dp_review_mining.nlp.utility.Review_Filter_Ads;
import com.dianping.algorithm.dp_review_mining.nlp.utility.Review_Filter_Few_Words;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;




/**
 * TODO Comment of DataFetchingReview
 * @author rui.xie
 *
 */
public class DataFetchingReview
{

	
	private static Logger LOGGER = Logger.getLogger(DataFetchingReview.class.getName());
	
	
	
	
	public void fetchReviewContentToSplitFiles(String baseFileName, String dir, String shopType)
	{
		RDBConnection dbConn = null;
		try
		{
			
			
			HashSet<String> words= new HashSet<String>();
	         HashSet<String> regs= new HashSet<String>();
	         words.add("http:");
	         words.add("www.");
	         words.add(".com");
	         words.add(".org");
	         words.add(".net");
	         words.add(".cn");
	         words.add(".taobao");
	         words.add(".html");
	         words.add("电话:");
	         words.add("qq:");
	         words.add("msn:");
	         words.add("网址:");
	         words.add("手机:");
	         words.add("邮箱:");
	         words.add("联系人:");
	         Review_Filter_Ads rfa = new Review_Filter_Ads(words,regs);
	         Review_Filter_Few_Words rffw = new Review_Filter_Few_Words(15,15,0.7);
			
			
			
			
			dbConn = new RDBConnection();
			int offset = 0;
			int num = 0;
			FWriter fw = new FWriter(dir+baseFileName+offset);
			while(true)
			{
				LOGGER.info("offset:"+offset);
				String sql = "SELECT ReviewID, ShopID, REPLACE(REPLACE(REPLACE(REPLACE(ReviewBody,'\n','。'),'\r','。'),',','，'),' ','，') AS ReviewText FROM DP_Review WHERE ShopType = " + shopType +" AND LENGTH(ReviewBody)> 15 order by ShopID limit 80000 offset "+ offset ;
				ResultSet rs = dbConn.executeQuery(sql);
				if(rs == null)
				{
					System.err.println("Result Set is null!");
					break;
				}
				LOGGER.info("query finished offset:"+offset);
				
				int resultNO=0;
				while(rs.next())
				{
					
					++offset;
					++resultNO;
					int reviewId=rs.getInt("ReviewID");
				    String reviewText=rs.getString("ReviewText");
				    int shopId = rs.getInt("ShopID");
				   
		           
				    if(reviewText.length()>15)
				    {
				    	
				    	reviewText = ReviewProcessor.filtingBeforeTagging(reviewText);
				    	boolean fewWords = rffw.doFilter(reviewText);
				    	boolean ad = rfa.doFilter(reviewText);
				    	if(!(ad||fewWords))
				    	{
				    		++num;
				    		fw.println(reviewId+","+shopId+","+reviewText);
				    	}
				    	
				    	if(num%80000==0)
				    	{
				    		fw.close();
				    		fw = new FWriter(dir+baseFileName+offset);
				    		LOGGER.info("80000 reviews have been recorded");
				    	}
				    	
				    }
				}
				if(resultNO==0) break;
				
				
			}
			fw.close();
			dbConn.sqlclose();
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			dbConn.sqlclose();
		}
		
		
		
	}
	
	
	
	
	

	public static void main(String args[])
	{	
		DataFetchingReview dfr =  new DataFetchingReview();
		String shopType = "10";
		String dir = "E:\\workspace\\rankingdata\\reviewContent1127_MYSQL\\";
		String baseFileName = "id_reviewBody_";
		dfr.fetchReviewContentToSplitFiles(baseFileName, dir, shopType);
	}
}
