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

import java.util.List;

import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.nlp.utility.ICTCLASTagToPenntreeTagAdaptor;
import com.dianping.algorithm.dp_review_mining.nlp.utility.SubSentenceRevisor;
import com.mongodb.DBObject;



/**
 * TODO Comment of SubSentence
 * @author rui.xie
 *
 */
public class SubSentence
{
	
	private static Logger LOGGER = Logger
			.getLogger(SubSentence.class.getName());
	
	//added by weifu
	public String originalSentenceWithPOS;
	
	public int rId;
	public int numWord; 
	public int numNVAD; 
	public String[] words;
	public String[] tagsIctclas;
	public String[] tagsPennbank;
	public char[] tagsIctclasSimple;
	
	public SubSentence()
	{
		
	}
	/**
	 * @param pos_start
	 * @param i
	 * @param words2
	 * @param tags
	 */
	public SubSentence(int pos_start, int i, List<String> words2,
			List<String> tags, int rId)
	{
		this.rId = rId;
		numWord = i-pos_start+1;
		
		words = new String [numWord];
		tagsIctclas = new String[numWord];
		tagsPennbank = new String[numWord];
		tagsIctclasSimple = new char[numWord];
		numNVAD = 0;
		
		
		for(int k=0;k<numWord;++k)
		{
			words[k] = words2.get(pos_start+k);
			tagsIctclas[k] = tags.get(pos_start+k);
		}
		//SubSentenceRevisor.ReviseWord(this);
		SubSentenceRevisor.ReviseTag(this);
		SubSentenceRevisor.ReviseWrongAdj(this);
		
		for(int k=0;k<numWord;++k)
		{
			tagsIctclasSimple[k] = tagsIctclas[k].charAt(0);
			if(tagsIctclasSimple[k]=='a'||tagsIctclasSimple[k]=='n'||tagsIctclasSimple[k]=='d'||tagsIctclasSimple[k]=='v')
				++numNVAD;
			if(k<numWord-1)
				tagsPennbank[k] = ICTCLASTagToPenntreeTagAdaptor.getPenntreeTag(words[k], tagsIctclas[k], tags.get(pos_start+k+1));
			else
				tagsPennbank[k] = ICTCLASTagToPenntreeTagAdaptor.getPenntreeTag(words[k], tagsIctclas[k], null);	
		}
	}
	public String toString()
	{
		String str = "wordNum:"+this.numWord+" wordNumOfADVN:"+numNVAD+" ";
		for(int i=0;i<numWord;++i)
		{
			str +=words[i]+":"+this.tagsIctclas[i]+":"+this.tagsIctclasSimple[i]+":"+this.tagsPennbank[i]+" ";
		}
		return str+"\n";
	}
	/**
	 * @return
	 */
	public String getSimpleTagContent()
	{
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<this.numWord;++i)
		{
			sb.append(this.words[i]);
			sb.append('/');
			sb.append(this.tagsIctclasSimple[i]);
			sb.append(' ');
		}
		if(sb.length()==0)
			return "";
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	/**
	 * @param subSentObj
	 * @param rId2
	 */
	public void loadFromDBOjbect(DBObject subSentObj, int reviewId)
	{
		// TODO Auto-generated method stub
		rId = reviewId;
		numWord = (Integer) subSentObj.get("wNum");
		numNVAD = (Integer) subSentObj.get("ewNum"); 
		words = new String [numWord];
		tagsIctclas = new String[numWord];
		tagsPennbank = new String[numWord];
		tagsIctclasSimple = new char[numWord];
		originalSentenceWithPOS = (String)subSentObj.get("body");
		String tokens[] = ((String)subSentObj.get("body")).split(" ");
		assert tokens.length == numWord;
		for(int i=0;i<numWord;++i)
		{
			String wpos = tokens[i];
			int index = wpos.indexOf('/');
			assert index!=-1;
			words[i] = wpos.substring(0, index);
			tagsIctclas[i] = wpos.substring(index+1);
			tagsIctclasSimple[i] = tagsIctclas[i].charAt(0);
			
		}
		for(int i=0;i<numWord;++i)
		{
			if(i<numWord-1)
				tagsPennbank[i] = ICTCLASTagToPenntreeTagAdaptor.getPenntreeTag(words[i], tagsIctclas[i], tagsIctclas[i+1]);
			else
				tagsPennbank[i] = ICTCLASTagToPenntreeTagAdaptor.getPenntreeTag(words[i], tagsIctclas[i], null);	
		}
	}
	
	
}
