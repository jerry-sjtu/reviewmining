/**
 * Project: review-mining-single
 * 
 * File Created at 2012-10-31
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
package com.dianping.algorithm.dp_review_mining.statistics;

import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.datastruct.OriginalTaggedReview;
import com.dianping.algorithm.dp_review_mining.datastruct.Sentence;
import com.dianping.algorithm.dp_review_mining.datastruct.SubSentence;
import com.dianping.algorithm.dp_review_mining.mongo_serialize.ReviewToMongo;
import com.dianping.algorithm.dp_review_mining.utility.FReader;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;
import com.dianping.algorithm.dp_review_mining.utility.FileSystemOperation;

import java.util.HashMap;
/**
 * TODO Comment of CorpusStatisticsInfo
 * @author rui.xie
 *
 */
public class CorpusStatisticsInfo
{
	private static Logger LOGGER = Logger.getLogger(CorpusStatisticsInfo.class
			.getName());
	
	// word info
	
	// different tag with different word count
	public HashMap<String,HashMap<String,Integer>> tagWordCount;
	
	public FWriter fw;
	
	int reviewNum;
	int reviewSentenceNum;
	int reviewSubSentneceNum;
	long reviewWordNum;
	int countANMatrix[][];
	FWriter fileMatrix[][][];
	String differentNASubDir;
	public CorpusStatisticsInfo(String dir)
	{
		differentNASubDir =  dir;
		tagWordCount = new HashMap<String, HashMap<String,Integer>>();
		reviewNum = reviewSentenceNum = reviewSubSentneceNum = 0;
		reviewWordNum = 0;
		countANMatrix = new int[3][3];
		fileMatrix = new FWriter[3][3][10];
		for(int i=0;i<3;++i)
			for(int j=0;j<3;++j)
			{
				countANMatrix[i][j]=0;
				for(int k=0;k<10;++k)
				{
					fileMatrix[i][j][k]= new FWriter(dir+"A"+i+"N"+j+"part"+k);
				}
			}
	}
	
	public void processingDir(String ori_dir, String sep)
	{
		fw = new FWriter("E:\\workspace\\rankingdata\\statistics\\NACountSub");
		String filenames[] = FileSystemOperation.listFilenames(ori_dir);
		for(int i=filenames.length-1; i>=0; --i)
		{
			processingFile(ori_dir+filenames[i], sep);
		}
		fw.close();
		for(int i=0;i<3;++i)
			for(int j=0;j<3;++j)
			{
				
				for(int k=0;k<10;++k)
				{
					fileMatrix[i][j][k].close();
				}
			}
	}
	
	
	public void processingFile(String ori,String sep)
	{
		FReader fr = new FReader(ori);
		
		String line = null;
		
		while((line=fr.readLine())!=null)
		{
			processingLine(line, sep);
		}
		fr.close();
		System.out.println(ori);
		
	}


	/**
	 * @param line
	 */
	private void processingLine(String line, String sep)
	{
		// TODO Auto-generated method stub
		OriginalTaggedReview otr = new OriginalTaggedReview();
		otr.loadReviewFromText(line, sep);
		otr.analyzeReview();
		int sId = 0;
		++this.reviewNum;
		for(Sentence s:otr.analyzedReview.sentences)
		{
			++this.reviewSentenceNum;
			int ssId = 0;
			for(SubSentence ss:s.subSents)
			{
				++this.reviewSubSentneceNum;
				int numN = 0;
				int numA = 0;
				StringBuilder sb = new StringBuilder();
				for(int i=0;i<ss.numWord;++i)
				{
					sb.append(ss.words[i]+"/"+ss.tagsIctclas[i]+" ");
					++reviewWordNum;
					if(ss.tagsIctclasSimple[i]=='a')
						++numA;
					else if(ss.tagsIctclasSimple[i]=='n')
						++numN;
//					String tag = ss.tagsIctclas[i];
//					String word = ss.words[i];
//					addPairCount(tag, word, 1);
				}
				int indexA = 2;
				int indexN = 2;
				if(numA<2)
					indexA = numA;
				if(numN<2)
					indexN = numN;
				this.countANMatrix[indexA][indexN]++;
				int k = otr.id%10;
				
				fileMatrix[indexA][indexN][k].println(otr.id+"|"+sb.toString());
				++ssId;
			}
			++sId;
		}
		
	}
	private void addPairCount(String tag, String word,
			Integer count)
	{
		// TODO Auto-generated method stub

		HashMap<String, Integer> entry = this.tagWordCount.get(tag);
		if (entry == null)
		{
			entry = new HashMap<String, Integer>();
			entry.put(word, count);
			this.tagWordCount.put(tag, entry);
		} 
		else
		{
			Integer num = entry.get(word);
			if (num == null)
			{
				entry.put(word, count);
			} 
			else
			{
				entry.put(word, num+count);
			}
		}
		

	}
	public static void main(String[] args)
	{
		String dir = "E:\\workspace\\rankingdata\\reviewContentTagged0903_MYSQL\\";
		String subDir = "E:\\workspace\\rankingdata\\differentANSubSentences\\";
		CorpusStatisticsInfo ftm = new CorpusStatisticsInfo(subDir);
		ftm.processingDir(dir, ",");
		ftm.dumpInfo();
		//ftm.saveTagWordCount("E:\\workspace\\rankingdata\\statistics\\TagWordCount");
	}


	/**
	 * @param string
	 */
	private void saveTagWordCount(String filepath)
	{
		// TODO Auto-generated method stub
		FWriter fr = new FWriter(filepath);
		
		for(String tag:tagWordCount.keySet())
		{
			HashMap<String, Integer> entry = tagWordCount.get(tag);
			for(String word:entry.keySet())
			{
				Integer num = entry.get(word);
				fr.println(tag+","+word+","+num);
			}
		}
		
		fr.close();
	}
	
	public void dumpInfo()
	{
		System.out.println("reviewNum           :"+this.reviewNum);
		System.out.println("reviewSentenceNum   :"+this.reviewSentenceNum);
		System.out.println("reviewSubSentneceNum:"+this.reviewSubSentneceNum);
		System.out.println("reviewWordNum       :"+this.reviewWordNum);
		
		for(int i=0;i<3;++i)
		{
			System.out.printf("%15d\t%15d\t%15d\n", countANMatrix[i][0], countANMatrix[i][1], countANMatrix[i][2]);
		}
	}
	
	
}
