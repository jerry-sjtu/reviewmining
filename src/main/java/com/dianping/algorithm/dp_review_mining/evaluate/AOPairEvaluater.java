/**   
* @Description: TODO
* @author weifu.du 
* @project review-mining-single
* @date Jan 5, 2013 
* @version V1.0
* 
* Copyright (c) 2010-2015 by Shanghai HanTao Information Co., Ltd.
 * All rights reserved.
*/
package com.dianping.algorithm.dp_review_mining.evaluate;


import java.util.Iterator;
import java.util.LinkedList;

import java.util.List;

import com.dianping.algorithm.dp_review_mining.associate_helper.AttributeOpinionPair;



/**
 * @author weifu
 *
 */
public class AOPairEvaluater {
	
	public void filter(List<AttributeOpinionPair> aopList) {


		Iterator<AttributeOpinionPair> it = aopList.iterator();
		while (it.hasNext()) {
			AttributeOpinionPair pair = it.next();
			if (unQualified(pair)) {
				it.remove();
			}
		}
	}
	
	public boolean unQualified(AttributeOpinionPair aopair) {
		return false;

	}

}
