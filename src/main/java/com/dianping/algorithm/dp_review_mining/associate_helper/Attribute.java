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

public class Attribute
{
	// 属性词
	public String attribute;
	// 属性词所在句子index
	public int attributeSentIndex;
	// 属性词所在子句index
	public int attributeSubSentIndex;
	// 属性词所在子句的位置index
	public int attributeWordIndex;
	
	public char pos;
	
	public Attribute(String string) 
	{
		// TODO Auto-generated constructor stub
		String tokens[] = string.split("_");
		this.attribute = tokens[0];
		this.attributeSentIndex = Integer.parseInt(tokens[1]);
		this.attributeSubSentIndex = Integer.parseInt(tokens[2]);
		this.attributeWordIndex = Integer.parseInt(tokens[3]);
	}

	public Attribute() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param attrElement
	 */
	public void attachContent(Element attrElement)
	{
		// TODO Auto-generated method stub
		attrElement.addElement("W").addText(attribute);
		attrElement.addElement("SI").addText(String.valueOf(attributeSentIndex));
		attrElement.addElement("SSI").addText(String.valueOf(attributeSubSentIndex));
		attrElement.addElement("WI").addText(String.valueOf(attributeWordIndex));
	}
	
	public String toString()
	{
		return attribute+"_"+attributeSentIndex+"_"+attributeSubSentIndex+"_"+attributeWordIndex;
	}
	
}
