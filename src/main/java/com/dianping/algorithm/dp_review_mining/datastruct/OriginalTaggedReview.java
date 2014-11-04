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
package com.dianping.algorithm.dp_review_mining.datastruct;

import java.util.Scanner;

import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.nlp.utility.ReviewProcessor;

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
	
	public void loadReviewFromText(String line, String sep)
	{
		if(line==null||line.length()==0)
			return;
		String tokens[] = line.split(sep);
		if(tokens.length!=3)
			return;
		try
		{
			id =  Integer.parseInt(tokens[0]);
		}
		catch(NumberFormatException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			shopId =  Integer.parseInt(tokens[1]);
		}
		catch(NumberFormatException e)
		{
			e.printStackTrace();
		}
		reviewTaggedContent = ReviewProcessor.reviseContentWithTag(tokens[2]);
		reviewOriginalContent = getOriginalContent(tokens[2]);
		
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
		analyzedReview.loadFromOriginal(this);
	}
	
	
	
	
	
	public String toString()
	{
		return id+"|"+shopId+"|"+reviewTaggedContent;
	}
	
	
	public static void main(String args[])
	{
		OriginalTaggedReview otr = new OriginalTaggedReview();
		String review = "31456959,500000,这家/r 店/n 开/v 了/ul 很多年/m 了/ul ，/w 也/d 吃/v 过/ug 很/d 多次/m 了/ul ，/w 新疆人/n 比较/d 多/m ，/w 看样子/v 是/v 蛮/d 正宗/b 的/uj ，/w 环境/n 就/d 一般/a 了/ul ，/w 地下室/n ，/w 洗手间/n 还要/v 到/v 一楼/ns ，/w 以前/f 的/uj 服务员/n 态度/n 还/d 不错/a ，/w 这次/r 的/uj 就/d 一般/a 了/ul ，/w 有/v 表演/vn ，/w 但是/c 音乐/n 开/v 得/ud 太/d 吵/v 了/ul ，/w 还/d 是/v 靠/v 后座/n 比较/d 好/a 。/wj 羊肉/n 窜/v ：/w 5/m ／/w 窜/v ，/w 肥瘦/n 相/d 间/f ，/w 肉/n 还/d 算/v 多/m ，/w 味道/n 不错/a 。/wj 大盘鸡/n ：/w 分/q 大中/nr 小/a ，/w 40/m —/w 60元/m 左右/m ，/w 一/m 大盘/n 配/v 着/uz 宽面/n ，/w 蛮/d 实在/d 的/uj 。/wj 烤羊排/n ：/w 20/m ／/w 份/q ，/w 比较/d 肥/a 的/uj 大概/d 有/v 4块/m ，/w 瘦/a 的/uj 话/n 就/d 只/d 有/v 三块/m 了/ul ，/w 味道/n 一般/a ，/w 烤/v 得/ud 偏/d 干/v 。/wj 拔丝苹果/n ：/w 26/m ／/w 份/q ，/w 个人/n 比较/d 喜欢/v 。/wj 奶茶/n ：/w 这个/r 是/v 每次/r 必/d 点/q 的/uj ，/w 一/m 壶/q 25/m 好像/v ，/w 我/r 喜欢/v 甜味/n 的/uj ，/w 三个/m 人/n 一/m 壶/q 也/d 足够/v 吃/v 了/ul 。/wj 囊/ng ：/w 3/m ／/w 个/q ，/w 一般/a 。/wj ";
		otr.loadReviewFromText(review, ",");
		otr.analyzeReview();
		System.out.println(otr.getOriginalContent(review));
		
	
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
