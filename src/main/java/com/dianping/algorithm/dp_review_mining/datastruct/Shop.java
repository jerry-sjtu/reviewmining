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
package com.dianping.algorithm.dp_review_mining.datastruct;

import org.apache.log4j.Logger;

/**
 * TODO Comment of Shop
 * @author rui.xie
 *
 */
public class Shop
{
	private static Logger LOGGER = Logger.getLogger(Shop.class.getName());
	public int shopId;
	public int numReview;
	public int shopType; // 10 is 美食
	public String shopName;
	public int popularity; // 热度
	public int shopPower; //50是五星 35是三星半
	public int categoryId;
	public int districtId;
	public int power; // 5代表正常营业
	public int cityId;
	
	
}
