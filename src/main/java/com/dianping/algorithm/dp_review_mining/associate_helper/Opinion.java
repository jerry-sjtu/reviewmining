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
public class Opinion
{
	// 评价词
	public String opinion;
	// 评价词所在句子index
	public int opinionSentIndex;
	// 评价词所在子句index
	public int opinionSubSentIndex;
	// 评价词所在子句的位置index
	public int opinionWordIndex;
	
	public char pos;
	public Opinion(String string) 
	{
		// TODO Auto-generated constructor stub
		String tokens[] = string.split("_");
		this.opinion = tokens[0];
		this.opinionSentIndex = Integer.parseInt(tokens[1]);
		this.opinionSubSentIndex = Integer.parseInt(tokens[2]);
		this.opinionWordIndex = Integer.parseInt(tokens[3]);

	}
	public Opinion() 
	{
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param opinElement
	 */
	public void attachContent(Element opinElement)
	{
		// TODO Auto-generated method stub
		opinElement.addElement("W").addText(opinion);
		opinElement.addElement("SI").addText(String.valueOf(opinionSentIndex));
		opinElement.addElement("SSI").addText(String.valueOf(opinionSubSentIndex));
		opinElement.addElement("WI").addText(String.valueOf(opinionWordIndex));
	}
	public String toString()
	{
		return opinion+"_"+opinionSentIndex+"_"+opinionSubSentIndex+"_"+opinionWordIndex;
	}
}