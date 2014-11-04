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
package com.dianping.algorithm.dp_review_mining.associate_helper;

import org.dom4j.Element;

/**
 * @author weifu
 *
 */


public class AttributeModifier
{
	// 属性修饰词
	public String attributeModifier;
	// 属性修饰词所在句子index
	public int sentIndex;
	// 属性修饰词所在子句index
	public int subSentIndex;
	// 属性修饰词所在子句的位置index
	public int wordIndex;
	/**
	 * @param attrModiList
	 */
	public void attachContent(Element element)
	{
		// TODO Auto-generated method stub
		element.addElement("W").addText(attributeModifier);
		element.addElement("SI").addText(String.valueOf(sentIndex));
		element.addElement("SSI").addText(String.valueOf(subSentIndex));
		element.addElement("WI").addText(String.valueOf(wordIndex));
	}
}
