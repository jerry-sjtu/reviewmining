/**
 * Project: review-mining-single
 * 
 * File Created at 2012-12-4
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

import java.util.HashMap;


import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.datastruct.Vocubulary;
import com.dianping.algorithm.dp_review_mining.datastruct.Word;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * TODO Comment of CoPairAnalysis
 * @author rui.xie
 *
 */
public class CoPairAnalysis
{
	private static Logger LOGGER = Logger.getLogger(CoPairAnalysis.class
			.getName());
	
	HashMap<Integer,HashMap<Integer,Integer>> features;
	HashMap<Integer,HashMap<Integer,Integer>> opinions;
	Vocubulary voc;
	
	public CoPairAnalysis()
	{
		features = new HashMap<Integer, HashMap<Integer,Integer>>();
		opinions = new HashMap<Integer, HashMap<Integer,Integer>>();
		voc = new Vocubulary();
		voc.loadVocubuarly(new MongoDB());
		
	}
	
	public void dumpFOMap(String feature,String opinion)
	{
		FWriter fw = new FWriter(feature);
		for(Integer fid:features.keySet())
		{
			HashMap<Integer, Integer> oidMap = features.get(fid);
			String featureWpos = voc.getWposById(fid);
			for(Integer oid:oidMap.keySet())
			{
				Integer count = oidMap.get(oid);
				String opinionWpos = voc.getWposById(oid);
				fw.println(fid+","+featureWpos+","+oid+","+opinionWpos+","+count);
			}
		}
		fw.close();
		
		fw = new FWriter(opinion);
		for(Integer oid:opinions.keySet())
		{
			HashMap<Integer, Integer> fidMap = opinions.get(oid);
			String opinionWpos = voc.getWposById(oid);
			for(Integer fid:fidMap.keySet())
			{
				Integer count = fidMap.get(fid);
				String featureWpos = voc.getWposById(fid);
				fw.println(oid+","+opinionWpos+","+fid+","+featureWpos+","+count);
			}
		}
		fw.close();
	}
	
	
	public void loadFeaturesAndOpinions()
	{
		MongoDB mongo = new MongoDB();
		mongo.useCollection("dpFoodCoPair");
		DBCursor cursor = mongo.find(null);
		try
		{
			while (cursor.hasNext())
			{
				DBObject entry = cursor.next();
				Integer hid = (Integer) entry.get("hid");
				Integer tid = (Integer) entry.get("tid");
				Integer c = (Integer) entry.get("dfpair");
				Word h = voc.getWordById(hid);
				Word t = voc.getWordById(tid);
				int hc = h.df;
				int tc = t.df;
				if(hc>5&&tc>5)
				{
					int fid = -1;
					int oid = -1;
					char hpos = h.pos;
					char tpos = t.pos;
					if(hpos=='n')
						fid = hid;
					else if(hpos=='a'||hpos=='v')
						oid = hid;
					
					if(tpos=='n')
						fid = tid;
					else if(tpos=='a'||tpos=='v')
						oid = tid;
					
					if(oid<0||fid<0)
					{
						continue;
					}
					else
					{
						if(c>10)
						{
							addToHashMap(fid,oid,c,features);
							addToHashMap(oid,fid,c,opinions);
						}
					}
					
				}

			}
			System.out.println(features.size());
			System.out.println(opinions.size());
		} 
		finally
		{
			cursor.close();
		}
	}

	/**
	 * @param fid
	 * @param oid
	 * @param features2
	 */
	private void addToHashMap(int fid, int sid, int count,HashMap<Integer, HashMap<Integer, Integer>> targetMap)
	{
		// TODO Auto-generated method stub
		
		HashMap<Integer, Integer> entry = targetMap.get(fid);
		if (entry == null)
		{
			entry = new HashMap<Integer, Integer>();
			entry.put(sid, count);
			targetMap.put(fid, entry);
		} 
		else
		{
			Integer num = entry.get(sid);
			if (num == null)
			{
				entry.put(sid, count);
			} 
			else
			{
				LOGGER.error("it already has value");
			}
		}

	}
	public static void main(String[] args)
	{
		CoPairAnalysis cpa = new CoPairAnalysis();
		cpa.loadFeaturesAndOpinions();
		String featurePath = "D:/featureMap.csv";
		String opinionPath = "D:/opinionMap.csv";
		cpa.dumpFOMap(featurePath, opinionPath);
	}
}
