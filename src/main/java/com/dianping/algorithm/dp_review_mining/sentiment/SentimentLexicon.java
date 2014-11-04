/**   
* @Description: TODO
* @author weifu.du 
* @project review-mining-single
* @date 2012-9-28 
* @version V1.0
* 
* Copyright (c) 2010-2015 by Shanghai HanTao Information Co., Ltd.
 * All rights reserved.
*/
package com.dianping.algorithm.dp_review_mining.sentiment;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.utility.FReader;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class SentimentLexicon 
{

	
	/**
	 * TODO Comment of SentimentLexicon
	 * @author rui.xie
	 *
	 */
	
		private static Logger LOGGER = Logger.getLogger(SentimentLexicon.class
				.getName());
		public static int POSITIVE = 1;
		public static int NEGATIVE = 2;
		public static int FORCE = 4;
		public static int PRIVATIVE = 8;
		public static int NOTSURE = -1;
		private HashMap<String,Integer> sl;
	
		
		public SentimentLexicon()
		{
			sl = new HashMap<String, Integer>(); 
					
		}
		public void loadLexicon(String sentiment, String privative)
		{
			FReader fr = new FReader(privative);
			String line = null;
			while ((line = fr.readLine()) != null)
			{
				//to-do with the line;
				sl.put(line, SentimentLexicon.PRIVATIVE);
			}
			fr.close();
			
			fr = new FReader(sentiment);
			line = null;
			while ((line = fr.readLine()) != null)
			{
				//to-do with the line;
				String tokens[] = line.split("\t");
				if(tokens.length==2)
				{
					if(tokens[1].equals("positive"))
					{
						sl.put(tokens[0], this.POSITIVE);
					}
					else if(tokens[1].equals("negative"))
					{
						sl.put(tokens[0], this.NEGATIVE);
					}
					else if(tokens[1].equals("degree"))
					{
						sl.put(tokens[0], this.FORCE);
					}
					else
					{
						System.out.println("error");
					}
				}
			}
			fr.close();
		}
		
		public void dump()
		{
			for(String key:sl.keySet())
			{
				System.out.println(key+","+sl.get(key));
			}
		}
		
		public int getOrientation(String word)
		{
			Integer ori = this.sl.get(word);
			if(ori==null)
				return -1;
			return ori;
		}
	

	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		SentimentLexicon sl = new SentimentLexicon();
		try
		{
			System.out.println(new File("data/sentiment/dp-sentiment-lexicon.txt").getCanonicalPath());
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sl.loadLexicon("data/sentiment/dp-sentiment-lexicon.txt", "data/sentiment/dp-privative-lexicon.txt");
		sl.dump();
	}
	/**
	 * @param string
	 * @return
	 */
	public boolean contains(String word)
	{
		// TODO Auto-generated method stub
		Integer ori = this.sl.get(word);
		if(ori==null)
			return false;
		return true;
	}
}
