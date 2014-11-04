/**   
* @Description: TODO
* @author weifu.du 
* @project review-mining-single
* @date Dec 25, 2012 
* @version V1.0
* 
* Copyright (c) 2010-2015 by Shanghai HanTao Information Co., Ltd.
 * All rights reserved.
*/
package com.dianping.algorithm.dp_review_mining.associate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dianping.algorithm.dp_review_mining.associate_helper.AttributeOpinionPair;
import com.dianping.algorithm.dp_review_mining.datastruct.AnalyzedReview;
import com.dianping.algorithm.dp_review_mining.datastruct.Sentence;
import com.dianping.algorithm.dp_review_mining.datastruct.SubSentence;

/**
 * @author weifu
 *
 */
public class NAAssociator extends AttributeOpinionAssociator {//N+A
	
	private Pattern pattern = Pattern.compile("([^\\s]*?)/n.*?([^\\s]*?)/a");
	private List<AttributeOpinionAssociator> aoaList = new ArrayList<AttributeOpinionAssociator>();
	public List<AttributeOpinionPair> associate(AnalyzedReview ar) {
		List<AttributeOpinionPair> aopList = new ArrayList<AttributeOpinionPair>();
//		for (Sentence sentence : ar.sentences) {
//			for (SubSentence subSentence : sentence.subSents) {
//				String subsentWithPos = subSentence.originalSentenceWithPOS;
//				Matcher matcher = pattern.matcher(subsentWithPos);
//				while (matcher.find()) {
//					System.out.println("=================feature: " +matcher.group(1) + "\tsentiment: " + matcher.group(2));
//					subsentWithPos = subsentWithPos.substring(matcher.end(1) + 2);
//					matcher = pattern.matcher(subsentWithPos);
//				}
//			}
//		}
		return aopList;
	}
	
	public static void main(String[] args) {
		
	}
	
}
