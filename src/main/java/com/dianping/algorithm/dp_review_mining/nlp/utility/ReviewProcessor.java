/**
 * Project: FeatureLib
 * 
 * File Created at 2012-8-30
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

import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.utility.FReader;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;



/**
 * TODO Comment of ReviewProcessor
 * @author rui.xie
 *
 */
public class ReviewProcessor
{
	private static Logger LOGGER = Logger.getLogger(ReviewProcessor.class.getName());
	//public static String punct = "[.。？?!！,，、;；:：(（)）─-—…·《 <>》　﹄﹃﹂﹁～～﹏\\[\\]〔〕【】“‘’”\"\"'']";
	public static String punct = "\\pP";
	public static String space = "\\s";
	public static String filtingBeforeTagging(String reviewText)
	{
		// TODO Auto-generated method stub
		reviewText = reviewText.replaceAll("["+space+",\u3000]", "，");
		reviewText = reviewText.replaceAll("("+punct+")"+"{2,}", "$1");
		reviewText = reviewText.replaceAll("(.+?)\\1{2,}", "$1");
		reviewText = filtNotHanziAsciiPunct(reviewText);
		return reviewText;
	}
	
	public static String getTextWithSimpleTag(String line)
	{
		if(line.length()==0) return null;
		int pos = 0;
		StringBuilder sb = new StringBuilder(); 
		int length = line.length();
		while(pos<length)
		{
			char c = line.charAt(pos);
			if(c =='/')
			{
				sb.append('/');
				pos++;
				sb.append(line.charAt(pos));
				pos++;
				while(pos<length&&line.charAt(pos)!=' ')
				{
					pos++;
				}
				if(pos!=length)
				{
					sb.append(' ');
					pos++;
				}
				
			}
			else
			{
				sb.append(c);
				pos++;
			}
		}
		return sb.toString();
		
	}
	
	public static String filtNotHanziAsciiPunct(String review)
	{
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<review.length();++i)
		{
			char c = review.charAt(i);
			boolean isPunct = IsPunct(c);
			if(IsHanzi(c))
			{
				sb.append(c);
			}
			else if(IsAsciiPrintable(c)&&!isPunct)
			{
				sb.append(c);
			}
			else if(isPunct)
			{
				int value = (int)(c+65248);
				if(value<0xffff)
				{
					sb.append((char)value);
				}
				else
				{
					sb.append(c);
				}
						
			}
				
		}
		return sb.toString();
	}
	
	/**
	 * @param c
	 * @return
	 */
	private static boolean IsAsciiPrintable(char c)
	{
		// TODO Auto-generated method stub
		if ((c >= 32)&&(c < 127))
		{  
	        return true;
	    }       
			
		return false;
	}

	/**
	 * @param c
	 * @return
	 */
	private static boolean IsPunct(char c)
	{
		// TODO Auto-generated method stub
		
		if(c=='+'||c=='<'||c=='>'||c=='|'||c=='~')
			return true;
		
		
		boolean isPunct = false;
		int type = Character.getType(c);
		switch (type)
		{
		case Character.OTHER_PUNCTUATION:
		case Character.INITIAL_QUOTE_PUNCTUATION:
		case Character.FINAL_QUOTE_PUNCTUATION:
		case Character.START_PUNCTUATION:
		case Character.END_PUNCTUATION:
		case Character.CONNECTOR_PUNCTUATION:
		case Character.DASH_PUNCTUATION:
			isPunct = true;
			break;
		default:
			break;
		}
		return isPunct;
	}

	/**
	 * @param c
	 * @return
	 */
	public static boolean IsHanzi(char c)
	{
		// TODO Auto-generated method stub
		if ((c >= 0x4e00)&&(c <= 0x9fbb)){  
           return true;
         }       
		return false;
	}
	
	public static boolean IsChineseWord(String word)
	{
		boolean default_value = false;
		for(int i=0;i<word.length();++i)
		{
			default_value = true;
			if(IsHanzi(word.charAt(i))!=true)
				return false;
		}
		return default_value;
	}
	
	public static void filteringDict(String ori_dict, String dict)
	{
		FReader fr = new FReader(ori_dict);
		FWriter fw = new FWriter(dict);
		String line = null;
		while((line=fr.readLine())!=null)
		{
			String word = line.substring(0,line.indexOf("|"));
			if(word.length()>0&&ReviewProcessor.IsChineseWord(word))
			{
				fw.println(line);
			}
		}
		fr.close();
		fw.close();
	}
	
	public String[] splitSentence(String reviewText)
	{
		String sentences[] = reviewText.split("[.!?;。！？；]");
		return sentences;
	}
	/**
	 * @param string
	 * @return
	 */
	public static String reviseContentWithTag(String string)
	{
		// TODO Auto-generated method stub
		string = string.replaceAll("好/a 多/m","好多/a");
		string = string.replaceAll("很多/m", "很多/a");
		string = string.replaceAll("(让|令|使)/v 人/n", "$1人/v");
		string = string.replaceAll("无/v 语/ng", "无语/a");
		string = string.replaceAll("很小/a 资/ng", "很/d 小资/a");
		
		
		return string;
	} 
	
	
	public static void main(String args[])
	{
		int a=2;
		switch (a)
		{
		case 1:
		case 2:
			System.out.println(a);
		case 4:
			System.out.println(a);
			break;
		default:
			break;
		}
			
				
		
	}

	
}
