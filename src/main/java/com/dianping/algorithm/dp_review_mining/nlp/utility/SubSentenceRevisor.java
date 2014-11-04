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
package com.dianping.algorithm.dp_review_mining.nlp.utility;

import java.util.HashSet;

import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.datastruct.SubSentence;


/**
 * TODO Comment of SubSentenceRevisor
 * @author rui.xie
 *
 */
public class SubSentenceRevisor
{
	private static Logger LOGGER = Logger.getLogger(SubSentenceRevisor.class
			.getName());
	
	public static String wrongAdjWordsI[] = {"好","实在","良","贼","老","异常","绝","弱","蛮","偏","满","酷"};
	public static String wrongAdjWordsII[] = {"不得了","够戗","完全"};
	public static String negtiveWords[] = {"不","唔","没","没有","无"};
	
	public static HashSet<String> typeI;
	public static HashSet<String> typeII;
	public static HashSet<String> neg;
	
	static
	{
		typeI = new HashSet<String>();
		for(int i=0;i<wrongAdjWordsI.length;++i)
		{
			typeI.add(wrongAdjWordsI[i]);
		}
		typeII = new HashSet<String>();
		for(int i=0;i<wrongAdjWordsII.length;++i)
		{
			typeII.add(wrongAdjWordsII[i]);
		}
		neg = new HashSet<String>();
		for(int i=0;i<negtiveWords.length;++i)
		{
			neg.add(negtiveWords[i]);
		}
	}
	
	
	public static void ReviseWrongAdj(SubSentence s)
	{
		for(int k=0;k<s.numWord;++k)
		{
			String word = s.words[k];
			
				if(typeI.contains(word))
				{
					int next = k+1;
					
					if(next<s.numWord)
					{
						if (neg.contains(s.words[next]))
						{
							s.tagsIctclas[k]="d";
							s.tagsIctclasSimple[k] = 'd';
						}
						if (s.tagsIctclasSimple[next]=='a')
						{
							s.tagsIctclas[k]="d";
							s.tagsIctclasSimple[k] = 'd';
						}
					}
					
				}
				else if(typeII.contains(word))
				{
					s.tagsIctclas[k]="d";
					s.tagsIctclasSimple[k] = 'd';
				}
			
			
			}
	}
	
	/**
	 * @param word
	 * @return
	 */
	private static boolean InTypeI(String word)
	{
		// TODO Auto-generated method stub
		return false;
	}

//	public static void ReviseWord(SubSentence s)
//	{
//		
//		for(int i=0;i<s.numWord;++i)
//		{
//			StringBuilder sb = new StringBuilder();
//			String word = s.words[i];
//			for(int k=0;k<word.length();++k)
//			{
//				if(ReviewProcessor.IsHanzi(word.charAt(k)))
//					sb.append(word.charAt(k));
//			}
//			s.words[i] = sb.toString();
//			
//		}
//	}
	
	public static void ReviseTag(SubSentence s)
	{
		for(int i=0;i<s.numWord;++i)
		{
			StringBuilder sb = new StringBuilder();
			char tag = s.tagsIctclas[i].charAt(0);
			if(s.tagsIctclas[i].equals("vn"))
			{
				s.tagsIctclas[i] = "n";
			}
			if(tag=='b')
			{
				s.tagsIctclas[i] = s.tagsIctclas[i].replace('b', 'a');
			}
			// 一般/uyy全部改成 一般/a
			if(s.words[i].equals("一般"))
			{
				if(s.tagsIctclas[i].equals("uyy"))
				{
					s.tagsIctclas[i] = "a";
				}
			}
			
			//可以 方便
			if(s.words[i].equals("可以")||s.words[i].equals("行")||s.words[i].equals("赞")||s.words[i].equals("腻")||s.words[i].equals("值")||s.words[i].equals("错"))
			{
				if(s.tagsIctclas[i].startsWith("v"))
				{
					s.tagsIctclas[i] = "a";
				}
			}
			if(s.tagsIctclas[i].startsWith("z"))
				s.tagsIctclas[i] = "a";
			if(s.words[i].equals("停车")||s.words[i].equals("服务")||s.words[i].equals("竞争")||s.words[i].equals("等位")||s.words[i].equals("装修"))
			{
				if(s.tagsIctclas[i].startsWith("v"))
				{
					s.tagsIctclas[i] = "n";
				}
			}
		}
	}
	
}
