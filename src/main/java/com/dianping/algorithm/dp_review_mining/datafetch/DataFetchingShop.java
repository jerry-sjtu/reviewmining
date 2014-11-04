/**
 * Project: review-mining-single
 * 
 * File Created at 2012-12-27
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
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.dataAccessLayer.RDBConnection;
import com.dianping.algorithm.dp_review_mining.datastruct.Shop;
import com.dianping.algorithm.dp_review_mining.nlp.utility.ReviewProcessor;
import com.dianping.algorithm.dp_review_mining.nlp.utility.Review_Filter_Ads;
import com.dianping.algorithm.dp_review_mining.nlp.utility.Review_Filter_Few_Words;
import com.dianping.algorithm.dp_review_mining.utility.FReader;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;

/**
 * TODO Comment of DataFetchingShop
 * @author rui.xie
 *
 */
public class DataFetchingShop
{
	private static Logger LOGGER = Logger.getLogger(DataFetchingShop.class.getName());
	
	public HashMap<Integer,Shop> map = new HashMap<Integer, Shop>();
	
	
	public HashMap<Integer,String> cityMap = new HashMap<Integer, String>();
	public HashMap<Integer,String> categoryMap = new HashMap<Integer, String>();
	public DataFetchingShop()
	{
		RDBConnection dbConn = null;
		try
		{
			dbConn = new RDBConnection();
			
			
		
			String sql = "SELECT DP_CityID,CityName FROM DP_CityNew";
			LOGGER.info("START QUERY");
			ResultSet rs = dbConn.executeQuery(sql);
			LOGGER.info("QUERY END");
				
				
				int resultNO=0;
				while(rs.next())
				{
					
					
					++resultNO;
					int cityId=rs.getInt("DP_CityID");
					String cityName = rs.getString("CityName");
					cityMap.put(cityId, cityName);
				   
				}  
			
			dbConn.sqlclose();
			
			
			dbConn = new RDBConnection();
			
			
			
			sql = "SELECT CategoryID,CategoryName FROM DP_CategoryList";
			LOGGER.info("START QUERY");
			rs = dbConn.executeQuery(sql);
			LOGGER.info("QUERY END");
				
				
				resultNO=0;
				while(rs.next())
				{
					
					
					++resultNO;
					int CategoryID=rs.getInt("CategoryID");
					String CategoryName = rs.getString("CategoryName");
					categoryMap.put(CategoryID, CategoryName);
				   
				}  
			
			dbConn.sqlclose();
			
		
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			dbConn.sqlclose();
		}
	}
	
	
	public void fetchShopBasicInfo()
	{
		loadShopReviewNum();
		RDBConnection dbConn = null;
		try
		{
			dbConn = new RDBConnection();
			
			
			
				String sql = "SELECT ShopID,ShopName, Popularity,CityID,ShopPower,Power FROM DP_Shop WHERE ShopType=10 AND `Power`=5";
				LOGGER.info("START QUERY");
				ResultSet rs = dbConn.executeQuery(sql);
				LOGGER.info("QUERY END");
				
				
				int resultNO=0;
				while(rs.next())
				{
					
					
					++resultNO;
					int shopId=rs.getInt("ShopId");
					if(map.containsKey(shopId))
					{
						Shop shop = map.get(shopId);
						shop.shopName = rs.getString("ShopName");
						shop.cityId = rs.getInt("CityID");
						shop.popularity = rs.getInt("Popularity");
						shop.power = rs.getInt("Power");
						shop.shopPower = rs.getInt("ShopPower");
					}
					if(resultNO%10000==0)
						System.out.println(resultNO);
				   
				}  
				    
				
				
			
			
			dbConn.sqlclose();
		
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			dbConn.sqlclose();
		}
	}

	/**
	 * 
	 */
	private void loadShopReviewNum()
	{
		// TODO Auto-generated method stub
		FReader fr = new FReader("D:/shopReviewNum.csv");
		String line = null;
		while ((line = fr.readLine()) != null)
		{
			//to-do with the line;
			String tokens[] = line.split(",");
			Shop shop = new Shop();
			shop.shopId = Integer.parseInt(tokens[0]);
			shop.numReview = Integer.parseInt(tokens[1]);
			shop.shopType = 10;
			shop.shopPower = -1;
			map.put(shop.shopId, shop);
		}
		fr.close();
	}
	
	public void dumpShop(String path)
	{
		FWriter fw = new FWriter(path);
	
		for(Integer shopId:map.keySet())
		{
			Shop shop = map.get(shopId);
			if(shop.power==5)
			{
				fw.println(""+shop.shopId+","+cityMap.get(shop.cityId)+","+shop.numReview+","+shop.shopName+","+shop.shopPower+","+shop.popularity);
				
			}
		}
		fw.close();
	}
	public static void main(String[] args)
	{
		DataFetchingShop dfs = new DataFetchingShop();
		dfs.fetchShopBasicInfo();
		dfs.dumpShop("D:/shopBasicInfo.csv");
	}
}
