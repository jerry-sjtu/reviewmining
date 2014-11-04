/**
 * Project: review-mining-single
 * 
 * File Created at 2012-10-12
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.datastruct.OriginalTaggedReview;
import com.dianping.algorithm.dp_review_mining.utility.FReader;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;
import com.dianping.algorithm.dp_review_mining.utility.FileSystemOperation;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * TODO Comment of CoPairToMongo
 * @author rui.xie
 *
 */
public class CoPairStaticstics
{
	private static Logger LOGGER = Logger.getLogger(CoPairStaticstics.class
			.getName());
	
	private HashMap<String,Integer> wordId;
	private HashMap<Integer,String> idWord;
	
	private HashMap<String,PairInfo> wordPairInfo;
	
	private MongoDB mongo;
	private int windowSize;
	
	private String shuffleDir;
	private int shuffleSize;
	
	
	public CoPairStaticstics(MongoDB mongo,String wordCollection, String shuffleDir,/*String copairCollection,*/ int windowsSize, int shuffleSize)
	{
		this.windowSize = windowsSize;
		this.shuffleDir = shuffleDir;
		this.shuffleSize = shuffleSize;
		wordId = new HashMap<String, Integer>();
		idWord = new HashMap<Integer, String>();
		
		wordPairInfo = new HashMap<String, PairInfo>();
		this.mongo = mongo;
		mongo.useCollection(wordCollection);
		loadWordIdFromMongo();
		//mongo.useCollection(copairCollection);
	}
	
	public void loadWordIdFromMongo()
	{
		DBCursor cursor = mongo.find(null, new String[]{"wId","wpos"});
		try 
		{
            while(cursor.hasNext()) 
            {
                DBObject entry = cursor.next();
                wordId.put((String)entry.get("wpos"), (Integer)entry.get("wId"));
                idWord.put((Integer)entry.get("wId"), (String)entry.get("wpos"));
            }
//            for(String key:wordId.keySet())
//            {
//            	System.out.println(key+":"+wordId.get(key));
//            }
            System.out.println(wordId.size());
        } 
		finally 
		{
            cursor.close();
        }
	}
	
	public void processingDir(String ori_dir, String sep)
	{
//		DBObject indexObj = new BasicDBObject();
//		indexObj.put("hid",1);
//		indexObj.put("tid",1);
//		mongo.createIndex(indexObj);
		
		String filenames[] = FileSystemOperation.listFilenames(ori_dir);
		for(int i=filenames.length-1; i>=0; --i)
		{
			System.out.println(filenames[i]);
			processingFile(ori_dir+filenames[i], sep);
			
		}
	}
	
	public void processingFile(String ori, String sep)
	{
		
		wordPairInfo.clear();
		FReader fr = new FReader(ori);
		
		String line = null;
		
		int i = 0;
		
		
		while((line=fr.readLine())!=null)
		{
			++i;
			processingLine(line, sep);
			
		}
		fr.close();
		
		FWriter fw[] = new FWriter[16];
		for(int m=0;m<this.shuffleSize;++m)
		{
			fw[m] = new FWriter(shuffleDir+m,true);
		}
		
		for(String pair:wordPairInfo.keySet())
		{
			int indexToWrite = Math.abs(pair.hashCode()%16);
			PairInfo pInfo = wordPairInfo.get(pair);
			fw[indexToWrite].println(pair+"|"+pInfo.count+"|"+pInfo.sumDistance+"|"+pInfo.sumPunctNum);
			
			
//			String ids[] = pair.split("\\|");
//			int hid = Integer.parseInt(ids[0]);
//			int tid = Integer.parseInt(ids[1]);
//			String hwpos = idWord.get(hid);
//			String twpos = idWord.get(tid);
//			
//			
//			
//			DBObject queryObj = new BasicDBObject();
//			queryObj.put("hid",hid);
//			queryObj.put("tid", tid);
//			
//			DBObject setObj = new BasicDBObject();
//			
//			setObj.put("hwpos", hwpos);
//			setObj.put("twpos", twpos);
//			setObj.put("pmi", 0);
//			
//			DBObject incObj = new BasicDBObject();
//			incObj.put("dfpair", count);
//			
//			DBObject updateObj = new BasicDBObject();
//			updateObj.put("$set", setObj);
//			updateObj.put("$inc", incObj);
//			
//			mongo.update(queryObj, updateObj, true, false);
		
		}
		for(int m=0;m<this.shuffleSize;++m)
		{
			fw[m].close();
		}
	}
	
	protected void processingLine(String line, String sep)
	{
		
		String tokens[] = line.split(",");
		if(tokens.length==3)
		{
			OriginalTaggedReview otr = new OriginalTaggedReview();
			otr.loadReviewFromText(line, sep);
			otr.analyzeReview();
			
			if(otr.id!=-1)
			{
				String content = otr.getSimpleTagContent();
				String words[] = content.split(" ");
				HashSet<String> wordPairInAReview = new HashSet<String>();
				HashMap<String,Integer> wordPairSumDistanceInAReview = new HashMap<String, Integer>();
				HashMap<String,Integer> wordPairNumOfPunctInAReview = new HashMap<String, Integer>();
				
				HashMap<String,Integer> wordPairTimesInAReview = new HashMap<String, Integer>();
				
				
				
				for(int i=0;i<words.length-1;++i)
				{
					
					int j = i+1;
					int num = windowSize;
					
					if(i+windowSize >= words.length)
						num = words.length-i-1;
					
					if(words[i].contains("/w"))
					{
						continue;
					}
					Integer wid_i = wordId.get(words[i]);
					if (wid_i == null)
						continue;
					int numOfPunct = 0;
					for(int k=0;k<num;++k)
					{
						if(words[j+k].contains("/w"))
						{
							++numOfPunct;
							continue;
						}
						Integer wid_j_k = wordId.get(words[j+k]);
						if (wid_j_k == null)
							continue;
						int small;
						int big;
						if(wid_i<wid_j_k)
						{
							small = wid_i;
							big = wid_j_k;
						}
						else
						{
							small = wid_j_k;
							big = wid_i;
						}
						String key = ""+small+"|"+big;
						if(wordPairInAReview.contains(key))
						{
							int times = wordPairTimesInAReview.get(key);
							wordPairTimesInAReview.put(key, times+1);
							int distance = wordPairSumDistanceInAReview.get(key);
							wordPairSumDistanceInAReview.put(key, distance+k+1);
							int numPunct = wordPairNumOfPunctInAReview.get(key);
							wordPairNumOfPunctInAReview.put(key, numPunct+numOfPunct);
						}
						else
						{
							wordPairInAReview.add(""+small+"|"+big);
							wordPairTimesInAReview.put(key, 1);
							wordPairSumDistanceInAReview.put(key, k+1);
							wordPairNumOfPunctInAReview.put(key, numOfPunct);
						}
					}
				}
					
				
				for(String wordpair:wordPairInAReview)
				{
					int times = wordPairTimesInAReview.get(wordpair);
					int numOfPunct = wordPairNumOfPunctInAReview.get(wordpair);
					int sumDistance = wordPairSumDistanceInAReview.get(wordpair); 
					if(wordPairInfo.containsKey(wordpair))
					{
						PairInfo pInfo = wordPairInfo.get(wordpair);
						pInfo.count++;
						
						assert times>0;
						pInfo.sumDistance += sumDistance/(double)times;
						pInfo.sumPunctNum += numOfPunct/(double)times;
					}
					else
					{
						PairInfo p = new PairInfo();
						p.count = 1;
						p.sumDistance = sumDistance/(double)times;
						p.sumPunctNum = numOfPunct/(double)times;
						wordPairInfo.put(wordpair, p);
					}
				}
				
			}
			
		}
	}
	public void mergeShuffleDir()
	{
		for(int m=0;m<this.shuffleSize;++m)
		{
			this.wordPairInfo.clear();
			FReader fr = new FReader(shuffleDir+m);
			
			String line = null;
			int size = 0;
			while((line=fr.readLine())!=null)
			{
				
				String tokens[] = line.split("\\|");
				if(tokens.length==5)
				{
					String key = tokens[0]+"|"+tokens[1];
					int count = Integer.parseInt(tokens[2]);
					double sumDistance = Double.parseDouble(tokens[3]);
					double sumPunctNum = Double.parseDouble(tokens[4]);
					if(this.wordPairInfo.containsKey(key))
					{
						PairInfo pInfo = wordPairInfo.get(key);
						pInfo.count+=count;
						pInfo.sumDistance+=sumDistance;
						pInfo.sumPunctNum+=sumPunctNum;
					}
					else
					{
						PairInfo p = new PairInfo();
						p.count = 1;
						p.sumDistance = sumDistance;
						p.sumPunctNum = sumPunctNum;
						wordPairInfo.put(key, p);
					}
					
				}
				
				

			}
			fr.close();
			new File(shuffleDir+m).delete();
			FWriter fw = new FWriter(shuffleDir+m+"_merge");
			System.out.println("size of "+m+" is "+size);
			System.out.println("after merged, size is "+ wordPairInfo.size());
			for(String pair:wordPairInfo.keySet())
			{
				
				PairInfo pInfo = wordPairInfo.get(pair);
				fw.println(pair+"|"+pInfo.count+"|"+pInfo.sumDistance/pInfo.count+"|"+pInfo.sumPunctNum/pInfo.count);
			}
			fw.close();
		}
	}
	public static void main(String args[])
	{
		MongoDB mongodb = new MongoDB();
		CoPairStaticstics cptm = new CoPairStaticstics(mongodb,"dpFoodWord","E:/workspace/rankingdata/reviewShuffle/_",10,16);
		String dir = "E:\\workspace\\rankingdata\\reviewContentTagged0903_MYSQL\\";
		cptm.processingDir(dir, ",");
		cptm.mergeShuffleDir();
		
		
	}
}
class PairInfo
{
	int count;
	double sumDistance;
	double sumPunctNum;
}
