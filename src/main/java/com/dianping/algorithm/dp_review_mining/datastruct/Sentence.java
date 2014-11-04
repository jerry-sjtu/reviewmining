/**
 * Project: FeatureLib
 * 
 * File Created at 2012-9-24
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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.mongodb.DBObject;

/**
 * TODO Comment of Sentence
 * @author rui.xie
 *
 */
public class Sentence
{
	
	private static Logger LOGGER = Logger.getLogger(Sentence.class.getName());
	public List<SubSentence> subSents;
	
	public int numSubsent;
	public int numWord;
	public int numNVAD;
	
	public int rId;
	
	
	public Sentence()
	{
		subSents = new ArrayList<SubSentence>();
	}
	
	public void loadFromDBOjbect(DBObject obj, int reviewId)
	{
		subSents.clear();
		rId = reviewId;
		
		numSubsent = (Integer) obj.get("ssNum");
		numWord = (Integer) obj.get("wNum");
		numNVAD = (Integer) obj.get("ewNum");
		
		ArrayList<DBObject> subsents = (ArrayList) obj.get("subsents");
		for(DBObject subSentObj:subsents)
		{
			SubSentence subsent = new SubSentence();
			subsent.loadFromDBOjbect(subSentObj,rId);
			subSents.add(subsent);
		}
		
	}
	
	/**
	 * @param words
	 * @param tags
	 */
	public void loadFromWordsAndTags(List<String> words, List<String> tags, int rId)
	{
		// TODO Auto-generated constructor stub
		this.rId = rId;
		subSents.clear();
		numSubsent = 0;
		numWord = 0;
		numNVAD = 0;
		int pos_end;
		int pos_start = pos_end = 0;
		while(pos_start<tags.size())
		{
			pos_end = findNextComma(pos_start,words,tags);
			if(pos_end-pos_start>=1)
			{
				SubSentence ss = new SubSentence(pos_start,pos_end-1,words,tags,rId);
				subSents.add(ss);
				
				++numSubsent;
				numWord += ss.numWord;
				numNVAD += ss.numNVAD;
			}
			pos_start = pos_end+1;
		}
	}

	/**
	 * @param pos_start
	 * @param tags 
	 * @param words 
	 * @return
	 */
	private int findNextComma(int pos_start, List<String> words, List<String> tags)
	{
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		int pos_end = pos_start;
		while(pos_end<tags.size() && !words.get(pos_end).equals("，"))
			++pos_end;
		return pos_end;	
		
	}
	public String toString()
	{
		String str = "subSentences("+this.numSubsent+" "+this.numWord+" "+this.numNVAD+")\n";
		for(SubSentence ss:this.subSents)
		{
			str+="\t"+ss.toString();
		}
		return str;
	}

	/**
	 * @return
	 */
	public String getSimpleTagContent()
	{
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		for(SubSentence ss:this.subSents)
		{
			sb.append(ss.getSimpleTagContent());
			sb.append(" ，/w ");
		}
		if(sb.length()==0)
			return "";
		sb.deleteCharAt(sb.length()-1);
		sb.deleteCharAt(sb.length()-1);
		sb.deleteCharAt(sb.length()-1);
		sb.deleteCharAt(sb.length()-1);
		sb.deleteCharAt(sb.length()-1);
		
		return sb.toString();
		
	}
}
