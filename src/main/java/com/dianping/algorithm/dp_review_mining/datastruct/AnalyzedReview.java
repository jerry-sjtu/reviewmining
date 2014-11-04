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

import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;



/**
 * TODO Comment of Review
 * @author rui.xie
 *
 */
public class AnalyzedReview
{
	private static Logger LOGGER = Logger.getLogger(AnalyzedReview.class.getName());
	
	
	public List<Sentence> sentences;
	public int numSentence;
	public int numSubSentence;
	public int numWord;
	public int numNVAD; 
	public int rId;
	
	public AnalyzedReview()
	{
		sentences = new ArrayList<Sentence>();
	}
	
	public void loadFromDBOjbect(DBObject obj)
	{
		rId = (Integer) obj.get("rId");
		sentences.clear();
		numSentence = (Integer) obj.get("sNum");
		numSubSentence = (Integer) obj.get("ssNum");
		numWord = (Integer) obj.get("wNum");
		numNVAD = (Integer) obj.get("ewNum");
		ArrayList<DBObject> sents = (ArrayList) obj.get("sents");
		for(DBObject sentObj:sents)
		{
			Sentence sent = new Sentence();
			sent.loadFromDBOjbect(sentObj,rId);
			sentences.add(sent);
		}
		
	}
	
	
	public void loadFromOriginal(OriginalTaggedReview otr)
	{
		rId = otr.id;
		sentences.clear();
		numSentence = 0;
		numSubSentence = 0;
		numWord = 0;
		numNVAD = 0;
		
		String text = otr.reviewTaggedContent;
		
		List<String> words = new ArrayList<String>();
		List<String> tags = new ArrayList<String>();
		
		for(int i=0; i<text.length();++i)
		{
			if(text.charAt(i)=='/'&&text.charAt(i+1)!='/')
			{
				if(i+1<text.length()&&Character.isLetter(text.charAt(i+1)))
				{
					int j=i;
					while(j>=0&&text.charAt(j)!=' ')
					{
						--j;
					}
					String word = text.substring(j+1, i);
					j=i+1;
					while(j<text.length()&&text.charAt(j)!=' ')
					{
						++j;
					}
					String tag = text.substring(i+1,j);
					if(word.equals("。")||word.equals("？")||word.equals("！")||word.equals("；")||word.equals("．")||word.equals("～"))
					{
						if(words.size()>0)
						{
							Sentence rs = new Sentence();
							rs.loadFromWordsAndTags(words,tags,rId);
							sentences.add(rs);
							++numSentence;
							numSubSentence+=rs.numSubsent;
							numNVAD += rs.numNVAD;
							numWord +=  rs.numWord;
						}
						words.clear();
						tags.clear();
					}
					else
					{
						words.add(word);
						tags.add(tag);
					}
					
				}
			}
		}
		if(words.isEmpty()!=true)
		{
			if(words.size()>0)
			{
				Sentence rs = new Sentence();
				rs.loadFromWordsAndTags(words,tags,rId);
				sentences.add(rs);
				++numSentence;
				numSubSentence+=rs.numSubsent;
				numNVAD += rs.numNVAD;
				numWord += rs.numWord;
			}
			words.clear();
			tags.clear();
		}
	}

	
	public String toString()
	{
		String str = "total sentences("+this.numSentence+" "+ this.numSubSentence + " "+ this.numWord + " "+ this.numNVAD + "):\n";
		for(Sentence sent:this.sentences)
		{
			str += sent.toString();
		}
		return str;
	}
	
	public static void main(String args[])
	{
		MongoDB mongodb = new MongoDB();
		mongodb.useCollection("dpFoodReview");
		Integer rId = 30161674 ;
		DBObject obj = mongodb.findOne(new BasicDBObject("rId", rId));
		AnalyzedReview ar = new AnalyzedReview();
		ar.loadFromDBOjbect(obj);
		System.out.println(ar.getContentWithoutTag());
	}


	/**
	 * @return
	 */
	public String getSimpleTagContent()
	{
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		for(Sentence s:this.sentences)
		{
			sb.append(s.getSimpleTagContent());
			sb.append(" 。/wj ");
		}
		if(sb.length()==0)
		{
			return "";
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}

	/**
	 * @return
	 */
	public String getContentWithoutTag()
	{
		// TODO Auto-generated method stub
		String string = getSimpleTagContent();
		if(string!=null)
			return string.replaceAll("/[^ ]* ?", "");
		return "";
		
	}
}
	
	
	
	

