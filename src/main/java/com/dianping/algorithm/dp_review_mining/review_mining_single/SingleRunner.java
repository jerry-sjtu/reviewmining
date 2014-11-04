/**   
* @Description: TODO
* @author weifu.du 
* @project review-mining-single
* @date 2012-9-28 
* @version V1.0
* 
* Copyright (c) 2010-2015 by Shanghai HanTao Information Co., Ltd.
 * All rights reserved.
*/
package com.dianping.algorithm.dp_review_mining.review_mining_single;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Scanner;

import com.dianping.algorithm.dp_review_mining.associate.AttributeOpinionAssociator;
import com.dianping.algorithm.dp_review_mining.associate.OldAttributeOpinionAssociator;
import com.dianping.algorithm.dp_review_mining.associate_helper.Attribute;
import com.dianping.algorithm.dp_review_mining.associate_helper.AttributeOpinionPair;
import com.dianping.algorithm.dp_review_mining.datastruct.AnalyzedReview;
import com.dianping.algorithm.dp_review_mining.datastruct.OriginalTaggedReview;
import com.dianping.algorithm.dp_review_mining.nlp.utility.FileTagger;

public class SingleRunner 
{
    public static void main( String[] args ) throws IOException
    {
    	AttributeOpinionAssociator aoa = new OldAttributeOpinionAssociator();
    	
    	OldAttributeOpinionAssociator.init();
        String reviewContent = "外婆家是我特别喜欢的一个店铺了，菜式种类繁多，价格实惠，环境整洁，服务员的素质也挺高的，每次同学聚会，同事会餐，亲戚聚餐都会来这里的。麻婆豆腐是必点的，每次都要点一道麻婆豆腐开胃。茶香鸡，鸡肉的茶味很重，有茶的清醇。话梅花生，很有创意的煮法，话梅祛痰止咳，刚好不会上火，而且店家做出来的味道也不错，不会。太酸。绿茶饼，也是必不可少的饭后甜点，这里的绿茶饼真不是盖的，清甜爽口，又不会太甜，个人很喜欢";
        String reviewId = "38107116";
        String shopId = "5429278";
        System.out.println(reviewContent);
       
        while(true)
		{
        	//System.out.println("请输入整条评论（exit）:");
        	//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			
			
			//reviewContent = br.readLine();
			System.out.println(reviewContent);
			
			if(reviewContent.equals("exit"))
				break;
			analyzeSingleReview(aoa,getAnalyzeReviewFromOriginalText(reviewId,shopId,reviewContent));
			break;
		}
		
    }
    
    public static AnalyzedReview getAnalyzeReviewFromOriginalText(String reviewId,String shopId,String content)
    {
    	 FileTagger  ft = new FileTagger();
         String taggReview = ft.tagReview(content, reviewId);
         
         OriginalTaggedReview otr = new OriginalTaggedReview();
 		 String review = reviewId+","+shopId+","+taggReview;
 		 otr.loadReviewFromText(review, ",");
 		 otr.analyzeReview();
 		 return otr.analyzedReview;
    }
    
    public static void analyzeSingleReview(AttributeOpinionAssociator aoa, AnalyzedReview ar)
    {
    	List<AttributeOpinionPair> aopairs = aoa.associate(ar);
		for(AttributeOpinionPair aop:aopairs)
		{
			System.out.println(aop.toString());
		}
    }
}
