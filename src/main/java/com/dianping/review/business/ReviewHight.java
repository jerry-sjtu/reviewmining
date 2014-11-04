package com.dianping.review.business;

import com.dianping.algorithm.dp_review_mining.associate_helper.AttributeOpinionPair;
import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.review.business.AnalyzedReview;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class ReviewHight 
{
	String reviewContent;
	public String getReviewContent() 
	{
		return reviewContent;
	}
	public void setReviewContent(String reviewContent) 
	{
		this.reviewContent = reviewContent;
	}
	public ReviewHight(AttributeOpinionPair aop, MongoDB mongodb) 
	{
		// TODO Auto-generated constructor stub
		int reviewId = aop.rId;
		int sent = aop.attr.attributeSentIndex;
		int subSent = aop.attr.attributeSubSentIndex;
		System.out.println(reviewId);
		mongodb.useCollection("dpFoodReview");
		DBObject reviewObj = mongodb.findOne(new BasicDBObject("rId", reviewId));
		AnalyzedReview review = new AnalyzedReview();
		review.loadFromDBOjbect(reviewObj);
		reviewContent = review.getContentWithSubSentenceHighlight(sent, subSent);
		//System.out.println(reviewContent);
	}

}
