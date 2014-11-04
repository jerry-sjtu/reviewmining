/**
 * Project: SpringSecurity
 * 
 * File Created at 2012-12-18
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
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.dianping.review.business.Comment;
import com.dianping.review.dao.MongoDB;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.opensymphony.xwork2.ActionSupport;

/**
 * TODO Comment of ShopAjaxAction
 * @author rui.xie
 *
 */
public class ShopAjaxAction
{
	
		private int shopId;
		private String summaryText;
	
		

	    public String loadInfo() 
	    {
	    	MongoDB mongo = new MongoDB();
	    	
			if(shopId==-1)
			{
				
				shopId = randomShop(mongo);
				
			}
			
				mongo.useCollection("dpShopSummary");
				DBObject query = new BasicDBObject("shopId", shopId);
				summaryText = "";
				DBObject obj = mongo.findOne(query);
				if(obj!=null)
				{
					StringBuilder sb = new StringBuilder();
					
					sb.append((String) obj.get("t"));
					
					//summaryText = sb.substring(39);
					summaryText = sb.toString();
				}
				System.out.println("here");
				System.out.println(shopId);
				System.out.println(summaryText);
				
				HttpServletResponse response = ServletActionContext.getResponse(); 
				response.setContentType("text/xml;charset=UTF-8");  
			     PrintWriter out;
				try {
					out = response.getWriter();
					out.write(summaryText); 
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}         
			     
				return null;
			
			

			
			 
	    }


		/**
		 * @param mongo 
		 * @return
		 */
		public int randomShop(MongoDB mongo)
		{
			// TODO Auto-generated method stub
			int targetShopId = 0;
			HashSet<Integer> alreadyShops = new HashSet<Integer>();
			System.out.println("dpShopLog");
			mongo.useCollection("dpShopLog");
			System.out.println("dpShopLog");
			DBCursor cursor = mongo.find(null);
			if(cursor!=null)
			{
				while (cursor.hasNext())
				{
					
					DBObject entry = cursor.next();
					alreadyShops.add((Integer) entry.get("shopId"));

				}
				cursor.close(); 
			}
			mongo.useCollection("dpShopSummary");
			System.out.println("dpShopSummary");
			int allShopNum = mongo.count();
			double random = Math.random();
			
			System.out.println(random);
			System.out.println(allShopNum-alreadyShops.size());
			int target = (int)((allShopNum-alreadyShops.size())*random);
			System.out.println(target);
			int index = -1;
			cursor = mongo.find(null);
			try
			{
				while (cursor.hasNext())
					
				{
					
					DBObject entry = cursor.next();
					int shopCurId = (Integer) entry.get("shopId");
					if(alreadyShops.contains(shopCurId)==false)
					{
						if(index%100==0)
							System.out.println(index);
						index++;
					}
					if(index==target)
					{
						System.out.println(shopCurId);
						targetShopId = shopCurId;
						break;
					}
						
					
					

				}
			} 
			finally
			{
				cursor.close();
			}
			
			
			return targetShopId;
		}


		public int getShopId()
		{
			return shopId;
		}


		public void setShopId(int shopId)
		{
			//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
		
			this.shopId = shopId;
		}


		public String getSummaryText()
		{
			return summaryText;
		}


		public void setSummaryText(String summaryText)
		{
			//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
		
			this.summaryText = summaryText;
		}

	    /**
	     * 加载留言信息
	     */
	  
}
