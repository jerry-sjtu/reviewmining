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
package com.dianping.review.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.dianping.review.dao.MongoDB;
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
		Integer rId = 34452852 ;
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

	
	public String getContentWithSubSentenceHighlight(int sid, int ssid)
	{
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		boolean isSid = false;
		boolean isSsid = false;
		
		int sc = 0;
		for(Sentence s:this.sentences)
		{
			if(sc==sid)
				isSid = true;
			else
				isSid = false;
			int ssc = 0;
			for(SubSentence ss:s.subSents)
			{
				if(ssc==ssid)
					isSsid = true;
				else
					isSsid = false;
				
				if(isSid&&isSsid)
				{
					sb.append("<b>");
				}
				for(int i=0;i<ss.numWord;++i)
				{
					sb.append(ss.words[i]);
				}
				if(isSid&&isSsid)
				{
					sb.append("</b>");
				}
				
				if(ssc!=s.subSents.size()-1)
					sb.append("，");
				ssc++;
			}
			sb.append("。");
			sc++;
		}
		return sb.toString();	
		
	}
	
	
	
	/**
	 * @param owi 
	 * @param awi 
	 * @param ssid 
	 * @param sid 
	 * @param ori 
	 * @return
	 */
	public String getContentWithHighlight(int sid, int ssid, int awi, int owi, int ori)
	{
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		boolean isSid = false;
		boolean isSsid = false;
		boolean isAid = false;
		boolean isOid = false;
		int sc = 0;
		for(Sentence s:this.sentences)
		{
			if(sc==sid)
				isSid = true;
			else
				isSid = false;
			int ssc = 0;
			for(SubSentence ss:s.subSents)
			{
				if(ssc==ssid)
					isSsid = true;
				else
					isSsid = false;
				int wc = 0;
				for(int i=0;i<ss.numWord;++i)
				{
					if(wc==awi)
						isAid = true;
					else
						isAid = false;
					if(wc==owi)
						isOid = true;
					else
						isOid = false;
					
					if(isSid&&isSsid&&(isAid||isOid))
					{
						if(ori==1)
						{
							sb.append("<span class=\"highlightPOS\">");
							sb.append(ss.words[i]);
							sb.append("</span>");
						}
						else if(ori==2)
						{
							sb.append("<span class=\"highlightNEG\">");
							sb.append(ss.words[i]);
							sb.append("</span>");
						}
					}
					else
					{
						sb.append(ss.words[i]);
					}
					wc++;
				}
				if(ssc!=s.subSents.size()-1)
					sb.append("，");
				ssc++;
			}
			sb.append("。");
			sc++;
			
				
		}
			
		int posSpanStart = sb.indexOf("<span");
		boolean hasOne = false;
		int i=0;
		for(i=posSpanStart;i>=0;--i)
		{
			
			if(sb.charAt(i)=='，'||sb.charAt(i)=='。')
			{
				if(hasOne==false)
					hasOne=true;
				else
					break;
			}
		}
		int startPos = i+1;
		
		hasOne = false;
		int posSpanEnd = sb.indexOf("span>")+5;
		for(i=posSpanEnd;i<sb.length();++i)
		{
			
			if(sb.charAt(i)=='，'||sb.charAt(i)=='。')
			{
				if(hasOne==false)
					hasOne=true;
				else
					break;
			}
		}
		int endPos = i;
		return sb.substring(startPos, endPos);
	}

	/**
	 * @param pairs
	 * @return
	 */
	public String getContentWithFullHighlight(String pairs)
	{
		// TODO Auto-generated method stub
		HashMap<String,Integer> tokenKey = new HashMap<String, Integer>();
		String tokens[] = pairs.split("id_");
		for(String token:tokens)
		{
			if(token.length()>0)
			{
				String subTokens[] = token.split("_");
				if(subTokens.length==6)
				{
					tokenKey.put(subTokens[1]+"_"+subTokens[2]+"_"+subTokens[3],Integer.parseInt(subTokens[5]));
					tokenKey.put(subTokens[1]+"_"+subTokens[2]+"_"+subTokens[4],Integer.parseInt(subTokens[5]));
				}
			}
		}
		int sIndex = 0;
		StringBuilder sb = new StringBuilder();
		for(Sentence s:this.sentences)
		{
			int ssIndex = 0;
			
			for(SubSentence ss:s.subSents)
			{
				
				for(int i=0;i<ss.numWord;++i)
				{
					String keyStum = ""+sIndex+"_"+ssIndex+"_"+i;
					
					if(tokenKey.containsKey(keyStum))
					{
						if(tokenKey.get(keyStum)==1)
						{
							sb.append("<span class=\"highlightPOS\">");
							sb.append(ss.words[i]);
							sb.append("</span>");
						}
						else if(tokenKey.get(keyStum)==2)
						{
							sb.append("<span class=\"highlightNEG\">");
							sb.append(ss.words[i]);
							sb.append("</span>");
						}
					}
					else
					{
						sb.append(ss.words[i]);
					}
					
				}
				if(ssIndex!=s.subSents.size()-1)
					sb.append("，");
				ssIndex++;
			}
			sb.append("。");
			sIndex++;
		}
		return sb.toString();
	}
}
	
	
	
	

