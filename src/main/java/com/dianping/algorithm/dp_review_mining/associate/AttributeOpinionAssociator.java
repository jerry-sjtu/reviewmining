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

import java.util.ArrayList;
import java.util.List;


import com.dianping.algorithm.dp_review_mining.associate_helper.AttributeOpinionPair;
import com.dianping.algorithm.dp_review_mining.datastruct.AnalyzedReview;

/**
 * @author weifu
 *
 */
public class AttributeOpinionAssociator {
	public List<AttributeOpinionPair> associate(AnalyzedReview ar) {
		return new ArrayList<AttributeOpinionPair>();
	};
	
	public boolean initialize() {
		return true;
	}
	
}

