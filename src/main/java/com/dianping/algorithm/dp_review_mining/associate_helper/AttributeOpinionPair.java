/**   
* @Description: TODO
* @author weifu.du 
* @project review-mining-single
* @date Nov 23, 2012 
* @version V1.0
* 
* Copyright (c) 2010-2015 by Shanghai HanTao Information Co., Ltd.
 * All rights reserved.
*/
package com.dianping.algorithm.dp_review_mining.associate_helper;

import java.util.ArrayList;

import org.dom4j.Element;

import com.dianping.algorithm.dp_review_mining.sentiment.SentimentLexicon;

public class AttributeOpinionPair implements Comparable<AttributeOpinionPair>
{

	public ArrayList<AttributeModifier> attrModifiers;
	public Attribute attr;
	public Opinion opinion;
	public int orieantation;
	public double score;
	public boolean tobeDelete =false;
	
	public ArrayList<OpinionModifier> opinModifiers;
	public int rId;
	
	public AttributeOpinionPair(int id)
	{
		this.attr = null;
		this.opinion = null;
		this.attrModifiers = null;
		this.opinModifiers = null;
		this.orieantation = -1;
		this.tobeDelete = false;
		rId = id;
	}
	public AttributeOpinionPair(String token) 
	{
		// TODO Auto-generated constructor stub
		String tokens[] = token.split("@");
		this.opinion = new Opinion(tokens[1]);
		this.attr = new Attribute(tokens[0]);
		this.orieantation = Integer.parseInt(tokens[2]);
		this.rId = Integer.parseInt(tokens[3]);
		this.score = Double.parseDouble(tokens[4]);
	}
	
	public String toString()
	{
		String ori = "";
		if(orieantation==SentimentLexicon.POSITIVE)
			ori = ",+";
		else if(orieantation==SentimentLexicon.NEGATIVE)
			ori = ",-";
			
		return "["+getAttrModifierString()+attr.attribute+","+getOpinModifierString()+opinion.opinion+ori+","+this.score+","+this.rId+"]";
	}

	/**
	 * @return
	 */
	public String getOpinModifierString()
	{
		// TODO Auto-generated method stub
		if(opinModifiers==null)
			return "";
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for(OpinionModifier am:opinModifiers)
		{
			sb.append(am.opinionModifier);
			sb.append(' ');
		}
		sb.append(')');
		return sb.toString();
	}
	/**
	 * @return
	 */
	private String getAttrModifierString()
	{
		// TODO Auto-generated method stub
		if(attrModifiers==null)
			return "";
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for(AttributeModifier am:attrModifiers)
		{
			sb.append(am.attributeModifier);
			sb.append(' ');
		}
		sb.append(')');
		return sb.toString();
	}
	/**
	 * @param element
	 */
	public void attachAOPair(Element element)
	{
		// TODO Auto-generated method stub
		element.addText(this.toString());
		element.addElement("RID").addText(String.valueOf(this.rId));
		
		Element attrElement = element.addElement("AW");
		this.attr.attachContent(attrElement);
		
		if(this.attrModifiers!=null)
		{
			if(this.attrModifiers.size()!=0)
			{
				Element attrModiList = element.addElement("AML");
				for(AttributeModifier attrModifier:attrModifiers)
				{
					attrModifier.attachContent(attrModiList.addElement("AM"));
				}
			}
		}
		
		
		Element opinElement = element.addElement("OW");
		this.opinion.attachContent(opinElement);
		
		if(this.opinModifiers!=null)
		{
			if(this.opinModifiers.size()!=0)
			{
				Element opinModiList = element.addElement("OML");
				for(OpinionModifier opinModifier:opinModifiers)
				{
					opinModifier.attachContent(opinModiList.addElement("OM"));
				}
			}
		}
		
		
		
		Element orienElement = element.addElement("O");
		orienElement.addText(String.valueOf(this.orieantation));
	
		
	}
	public int compareTo(AttributeOpinionPair o) {
		// TODO Auto-generated method stub
		return -Double.compare(this.score, o.score);
	}
	
	public String toMongoString(String fieldSeperator) 
	{
		// TODO Auto-generated method stub
		return attr.toString()+fieldSeperator+opinion.toString()+fieldSeperator+this.orieantation+fieldSeperator+this.rId+fieldSeperator+this.score;
	}

	
}
