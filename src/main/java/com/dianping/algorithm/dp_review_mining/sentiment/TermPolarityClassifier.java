/**
 * Project: review-mining-single
 * 
 * File Created at 2012-10-16
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
package com.dianping.algorithm.dp_review_mining.sentiment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;
import org.bson.BasicBSONObject;

import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.statistics.CoPairInfo;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * TODO Comment of TermPolarityClassifier
 * @author rui.xie
 *
 */
public class TermPolarityClassifier
{
	private static Logger LOGGER = Logger
			.getLogger(TermPolarityClassifier.class.getName());
	
	private int constant; // prob of smoothing. e.g. 6 = 10e-6
	private HashMap<Integer,Integer> posSeeds;
	private HashMap<Integer,String> posWord;
	private HashMap<Integer,Integer> negSeeds;
	private HashMap<Integer,String> negWord;
	
	private HashMap<Integer,PriorityQueue<Pair>> seedWordCloseWordMap;
	
	private HashMap<Integer,HashMap<Integer,Integer>> coPair;
	
	
	private HashMap<Integer,String> toClassifiedTerms;
	private HashMap<Integer,Integer> toClassfiedTermCount;
	private MongoDB mongo;
	
	
	public void classifyTerms()
	{
//		System.out.println("posseed");
//		for(Integer id:this.posWord.keySet())
//		{
//			System.out.println(posWord.get(id)+":"+posSeeds.get(id));
//			
//		}
//		System.out.println("negseed");
//		for(Integer id:this.negWord.keySet())
//		{
//			System.out.println(negWord.get(id)+":"+posSeeds.get(id));
//		}
		String filepath = "C:\\Users\\rui.xie\\Desktop\\termclassifyresult1";
		FWriter fr = new FWriter(filepath);
		for(Integer id:toClassifiedTerms.keySet())
		{
			
			fr.println(classifyTermWithTopN(id,1));
		}
		fr.close();
		
		filepath = "C:\\Users\\rui.xie\\Desktop\\termclassifyresult10";
		fr = new FWriter(filepath);
		for(Integer id:toClassifiedTerms.keySet())
		{
			
			fr.println(classifyTermWithTopN(id,10));
		}
		fr.close();
		
	}
	public void expandByPMI()
	{
		for(Integer id:posWord.keySet())
		{
			expandByPMI(id, posWord.get(id), 10);
		}
		for(Integer id:negWord.keySet())
		{
			expandByPMI(id, negWord.get(id), 10);
		}
	}
	
	public void expandByPMI(Integer wId,String wpos, int N)
	{
		PriorityQueue<Pair> queue = new PriorityQueue<Pair>();
		for(Integer id:toClassifiedTerms.keySet())
		{
			int num = toClassfiedTermCount.get(id);
			int conum = 0;
			
			
			if(id<wId)
			{
				conum = getFromCoPair(id, wId);
			}
			else
			{
				conum = getFromCoPair(id, wId);
			}
			
			double postProb = conum/(double)num;
			if(conum==0)
			{
				continue;
				
			}
			if(queue.size()<N)
			{
				queue.add(new Pair(id,postProb));
			}
			else
			{
				Pair p = queue.poll();
				if(postProb>p.getValue())
				{
					queue.add(new Pair(id,postProb));
				}
				else
				{
					queue.add(p);
				}
			}
		}
		if(queue.size()!=0)
		{
			System.out.print(wpos+":");
			for(Pair p:queue)
			{
				System.out.print(toClassifiedTerms.get(p.wid)+"("+p.getValue()+")");
			}
			System.out.println();
			this.seedWordCloseWordMap.put(wId, queue);
		}
	}
	
	
	public String classifyTermWithTopN(Integer wId,int N)
	{
		/**
		 * try different method
		 * 
		 * @one top pos minus top neg
		 * @two top N pos minus top N neg
		 * @three get top N pair and add all pos minus all neg
		 */
		
		String wpos = this.toClassifiedTerms.get(wId);
		PriorityQueue<Pair> posQueue = new PriorityQueue<Pair>();
		for(Integer pId:this.posSeeds.keySet())
		{
			int num = posSeeds.get(pId);
			int conum = 0;
			if(pId<wId)
			{
				conum = getFromCoPair(pId, wId);
			}
			else
			{
				conum = getFromCoPair(wId, pId);
			}
			double postProb = conum/(double)num;
			if(conum==0)
			{
				continue;
				
			}
			if(posQueue.size()<N)
			{
				posQueue.add(new Pair(pId,postProb));
			}
			else
			{
				Pair p = posQueue.poll();
				if(postProb>p.getValue())
				{
					//System.out.println(postProb+" in and "+p.getValue()+"out");
					posQueue.add(new Pair(pId,postProb));
					
				}
				else
				{
					posQueue.add(p);
				}
			}
		}
		
		PriorityQueue<Pair> negQueue = new PriorityQueue<Pair>();
		for(Integer nId:this.negSeeds.keySet())
		{
			int num = negSeeds.get(nId);
			int conum = 0;
			if(nId<wId)
			{
				conum = getFromCoPair(nId, wId);
			}
			else
			{
				conum = getFromCoPair(wId, nId);
			}
			double postProb = conum/(double)num;
			if(conum==0)
			{
				continue;
			}
			if(negQueue.size()<N)
			{
				negQueue.add(new Pair(nId,postProb));
			}
			else
			{
				Pair p = negQueue.poll();
				if(postProb>p.getValue())
				{
					negQueue.add(new Pair(nId,postProb));
				}
				else
				{
					negQueue.add(p);
				}
			}
		}
		
		
		double result = 0;
		StringBuilder sb = new StringBuilder();
		sb.append(wId);
		sb.append(","+wpos+","+posQueue.size()+",");
		for(Pair p:posQueue)
		{
			result += Math.log10(p.getValue());
			sb.append(this.posWord.get(p.wid)+" ");
		}
		sb.append(","+negQueue.size()+",");
		
		for(Pair p:negQueue)
		{
			result -= Math.log10(p.getValue());
			sb.append(this.negWord.get(p.wid)+" ");
		}
		
		result += this.constant*(posQueue.size()-negQueue.size());
		sb.append(","+result);
		
		return sb.toString();
	}
	
	
	
	public TermPolarityClassifier(MongoDB mongo, int smoothing)
	{
		this.mongo = mongo;
		posSeeds = new HashMap<Integer,Integer>();
		posWord = new HashMap<Integer, String>();
		negSeeds = new HashMap<Integer,Integer>();
		negWord = new HashMap<Integer, String>();
		toClassifiedTerms = new HashMap<Integer,String>();
		coPair = new HashMap<Integer, HashMap<Integer,Integer>>();
		seedWordCloseWordMap = new HashMap<Integer, PriorityQueue<Pair>>();
		toClassfiedTermCount = new HashMap<Integer, Integer>();
		this.constant = smoothing;
		
	}
	// 0 for positive 1 for negative
	public void loadSeeds(int polarity)
	{
		
		mongo.useCollection("dpFoodWord");
		HashMap<Integer,Integer> idDf = new HashMap<Integer, Integer>();
		DBCursor cursor = mongo.find(null, new String[]{"wId","df"});
		try 
		{
            while(cursor.hasNext()) 
            {
                DBObject entry = cursor.next();
                idDf.put((Integer)entry.get("wId"), (Integer)entry.get("df"));
            }
        } 
		finally 
		{
            cursor.close();
        }
		
		// 选择在dpFoodWord中出现过的种子词
		mongo.useCollection("dpFoodSentimentLexicon");
		DBObject query = new BasicDBObject();
		query.put("human",1);
		query.put("polarity",polarity);
		query.put("idindic", new BasicDBObject("$ne", -1));
		
		cursor = mongo.find(query, new String[] { "wpos","idindic" });
		try
		{
			while (cursor.hasNext())
			{
				DBObject entry = cursor.next();
				int wid = (Integer) entry.get("idindic");
				String wpos = (String) entry.get("wpos");
				int num = idDf.get(wid);
				if(num>=500)
				{
					if(polarity==0)
					{
						this.posSeeds.put(wid,num);
						this.posWord.put(wid, wpos);
					}
					else if(polarity==1)
					{
						this.negSeeds.put(wid,num);
						this.negWord.put(wid, wpos);
					}
					else
					{
						LOGGER.error("only load pos and neg seed words");
					}
				}

			}
		} 
		finally
		{
			cursor.close();
		}
	}

	public void loadToClassifiedTerms()
	{
		mongo.useCollection("dpFoodWord");
		DBObject query = new BasicDBObject();
		query.put("pos", "a");
		query.put("df", new BasicDBObject("$gt",5));
		DBCursor cursor = mongo.find(query, new String[] { "wId","wpos","df"});
		try
		{
			int posNum = 0;
			int negNum = 0;
			while (cursor.hasNext())
			{
				DBObject entry = cursor.next();
				int wid = (Integer) entry.get("wId");
				String wpos = (String) entry.get("wpos");
				int count = (Integer) entry.get("df");
				if(posSeeds.containsKey(wid)||negSeeds.containsKey(wid))
				{
					if(posSeeds.containsKey(wid))
						posNum++;
					else
						negNum++;
					continue;
				}
				else
				{
					this.toClassifiedTerms.put(wid,wpos);
					this.toClassfiedTermCount.put(wid, count);
				}

			}
			System.out.println("pos num in to classify"+posNum);
			System.out.println("neg num in to classify"+negNum);
		} finally
		{
			cursor.close();
		}
	}
	
	public void loadCoPair()
	{
		int i = 0;
		int notUsed = 0;
		mongo.useCollection("dpFoodCoPair");
		DBCursor cursor = mongo.find(null, new String[] { "hid", "tid", "dfpair" });
		try
		{
			while (cursor.hasNext())
			{
				++i;
				DBObject entry = cursor.next();
				int hid = (Integer) entry.get("hid");
				int tid = (Integer) entry.get("tid");
				int num = (Integer) entry.get("dfpair");
				if(toClassifiedTerms.containsKey(hid))
				{
					if(this.posWord.containsKey(tid)||this.negWord.containsKey(tid))
					{
						addToCoPair(hid, tid, num);
					}
				}
				else if(toClassifiedTerms.containsKey(tid))
				{
					if(this.posWord.containsKey(hid)||this.negWord.containsKey(hid))
					{
						addToCoPair(hid, tid, num);
					}
				}
				else
				{
					notUsed++;
				}
				if(i%500000==0)
				{
					LOGGER.info("load "+i+" pair and have "+notUsed+" not used");
				}
			}
		} 
		finally
		{
			cursor.close();
		}
		
	}
	private void addToCoPair(Integer first, Integer second, Integer count)
	{
		// TODO Auto-generated method stub

		HashMap<Integer, Integer> entry = this.coPair.get(first);
		if (entry == null)
		{
			entry = new HashMap<Integer, Integer>();
			entry.put(second, count);
			this.coPair.put(first, entry);
		} 
		else
		{
			Integer num = entry.get(second);
			if (num == null)
			{
				entry.put(second, count);
			} else
			{
				LOGGER.error("it already has value");
			}
		}

	}
	
	private Integer getFromCoPair(Integer first, Integer second)
	{
		// TODO Auto-generated method stub

		HashMap<Integer, Integer> entry = this.coPair.get(first);
		if (entry == null)
		{
			return 0;
		} 
		else
		{
			Integer num = entry.get(second);
			if (num == null)
			{
				return 0;
			} 
			else
			{
				return num;
			}
		}

	}
	
	public static void main(String[] args)
	{
		MongoDB mongo = new MongoDB();
		TermPolarityClassifier tpc = new TermPolarityClassifier(mongo,6);
		tpc.loadSeeds(0);
		tpc.loadSeeds(1);
		tpc.loadToClassifiedTerms();
		System.out.println("posSeeds"+tpc.posSeeds.size());
		System.out.println("negSeeds"+tpc.negSeeds.size());
		System.out.println("total to classifed"+tpc.toClassifiedTerms.size());
		
		tpc.loadCoPair();
		int totalPair = 0;
		for(Integer key:tpc.coPair.keySet())
		{
			HashMap<Integer, Integer> entry = tpc.coPair.get(key);
			totalPair+=entry.size();
		}
		System.out.println("hasmap first key"+tpc.coPair.size());
		System.out.println("hasmap total"+totalPair);
		
		tpc.classifyTerms();
		//tpc.expandByPMI();
	}
}
class Pair implements Comparable<Pair>
{
	int wid;
	public double getValue()
	{
		return value;
	}
	double value;
	
	public Pair(int wid, double value)
	{
		this.wid = wid;
		this.value = value;
	}
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Pair o)
	{
		// TODO Auto-generated method stub
		return (int) (this.value-o.getValue());
	}
}