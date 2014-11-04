/**
 * Project: review-mining-single
 * 
 * File Created at 2012-10-13
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
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.bson.BSONObject;

import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.datastruct.CoPair;
import com.dianping.algorithm.dp_review_mining.utility.FReader;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;
import com.dianping.algorithm.dp_review_mining.utility.FileSystemOperation;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * TODO Comment of CoPairPMI
 * @author rui.xie
 *
 */
public class CoPairInfo
{
	private static Logger LOGGER = Logger.getLogger(CoPairInfo.class.getName());
	private MongoDB mongo;
	private double numReview;
	
	// int idPos, 0 is the feature, 1 is the opinion
	private HashMap<Integer,Integer> idDf;
	private HashMap<Integer,String> idWpos;
	private HashMap<Integer,HashMap<Integer,CoPair>> coPairInfoMap;
	
	public CoPairInfo(MongoDB mongo)
	{
		this.mongo = mongo;	
		idDf = new HashMap<Integer, Integer>();
		idWpos = new HashMap<Integer, String>();
		coPairInfoMap = new HashMap<Integer, HashMap<Integer,CoPair>>();
	}
	
	public void preLoad()
	{
		getNumReview();
		getWordDocumentFreq();
	}
	
	public void calcPMI()
	{
		preLoad();
		mongo.useCollection("dpFoodCoPair");
		
		DBCursor cursor = mongo.find(null);
		try 
		{
			int i=0;
            while(cursor.hasNext()) 
            {
                DBObject entry = cursor.next();
                //System.out.println(entry);
                int hid = (Integer) entry.get("hid");
                int tid = (Integer) entry.get("tid");
                int cc = (Integer) entry.get("dfpair");
                int hc = this.idDf.get(hid);
                int tc = this.idDf.get(tid);
                
                
                
            }
        
            System.out.println(idDf.size());
        } 
		finally 
		{
            cursor.close();
        }
	}
	public void calcNum(String original, String filtered)
	{
		preLoad();
		
		FReader fr = new FReader(original);//"D:/pairInfo.txt"
		FWriter fw = new FWriter(filtered);// "D:/pairInfoFilter.txt"
		String line = null;
		int i=0;
		int has_lhr = 0;
		int num[] = new int[11];
        for(int j=0;j<11;++j)
       	 num[j] = 0;
		while ((line = fr.readLine()) != null)
		{
			//to-do with the line;
			String tokens[] = line.split(",");
			 int hid = Integer.parseInt(tokens[0]);
	         String h_wpos = idWpos.get(hid);
	         int tid = Integer.parseInt(tokens[2]);
	         String t_wpos = idWpos.get(tid);
	         int cc = Integer.parseInt(tokens[4]);
	         double lhr_h = Double.parseDouble(tokens[tokens.length-1]);
	         double lhr_t = Double.parseDouble(tokens[tokens.length-2]);
	         if((IsHit(h_wpos)>0)&&(IsHit(t_wpos)>0))
	         {
	        	
	        	++i;
	        	if(lhr_h!=0d||lhr_t!=0d)
	        		has_lhr++;
	        	fw.println(line+","+h_wpos+","+t_wpos);
	         }
		}
		fr.close();
		fw.close();
		
		
		System.out.println("has lhr:"+has_lhr);
        System.out.println("total pair:"+i);
           
        
		
            
        
	}
	
	
	/**
	 * @param h_wpos
	 * @return
	 */
	private int IsHit(String h_wpos)
	{
		// TODO Auto-generated method stub
		if(h_wpos.contains("/a"))
			return 1;
		if(h_wpos.contains("/n"))
			return 4;
		if(h_wpos.contains("/v"))
			return 2;
		return 0;
	}

	public void calcAllPairMetric(String pairPath)
	{
//		try
//		{
//			System.setOut(new PrintStream(new File("d:\\stdout.txt")));
//		} 
//		catch (FileNotFoundException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}


		preLoad();
		
		mongo.useCollection("dpFoodCoPair");
		int lhr_size = 0;
		DBCursor cursor = mongo.find(null);
		try 
		{
			FWriter fw = new FWriter(pairPath);
			int i=0;
            while(cursor.hasNext()) 
            {
                DBObject entry = cursor.next();
                //System.out.println(entry);
                int hid = (Integer) entry.get("hid");
                String h_wpos = idWpos.get(hid);
                int tid = (Integer) entry.get("tid");
                String t_wpos = idWpos.get(tid);
                int cc = (Integer) entry.get("dfpair");
                int hc = this.idDf.get(hid);
                int tc = this.idDf.get(tid);
                double c12 = hc - cc;
                double c21 = tc - cc;
                double c22 = numReview-cc-c12-c21;
                assert c22>0;
                double pmi = Math.log(cc*numReview/hc/tc);
                //post_prob_h计算的是当f为t的时候即h是opinion,h是形容词
                double post_prob_h = cc/(double)hc;
                double post_prob_t = cc/(double)tc;
                double lhr_h = calcLHR(cc,c12,c21,c22);
                double lhr_t = calcLHR(cc,c21,c12,c22);
                if(lhr_h!=0d||lhr_t!=0d)
                {
                	//System.out.println(hid+","+h_wpos+","+hc+","+tid+","+t_wpos+","+tc+","+cc+","+pmi+","+post_prob_h+","+post_prob_t+","+lhr_h+","+lhr_t);
                	++lhr_size;
                }
                
                fw.println(hid+","+hc+","+tid+","+tc+","+cc+","+pmi+","+post_prob_h+","+post_prob_t+","+lhr_h+","+lhr_t);
                
                
            }
            fw.close();
            System.out.println(lhr_size);
            System.out.println(idDf.size());
        } 
		finally 
		{
            cursor.close();
        }
	}
	/**
	 * @param cc
	 * @param c12
	 * @param c21
	 * @param c22
	 * @return
	 */
	private double calcLHR(double numOfFW, double numOfNotFW, double numOfFNotW, double numOfNotFNotW)
	{
		// TODO Auto-generated method stub
		double numOfF = numOfFW+numOfFNotW; // c11+c21
		double numOfW = numOfFW+numOfNotFW; // c11+c12
		double numOfNotF = numOfNotFW+numOfNotFNotW; //c12+c22
		double numOfNotW = numOfFNotW+numOfNotFNotW;//c21+c22
		double numOfAll = numOfF+numOfNotF;
		
		double r_F_W = numOfFW/numOfW; //  may be 0;
		double r_F_NotW = numOfFNotW/numOfNotW; // may be 0
		double r_F = numOfF/numOfAll; // may be 0
		if(r_F_W < r_F_NotW)
			return 0;
		double F_Entropy = 0;
		if(r_F != 0d && r_F!=1.0)
			F_Entropy = numOfF*Math.log(r_F)+numOfNotF*Math.log(1-r_F);
		double F_Entropy_Under_W = 0;
		if(r_F_W != 0d && r_F_W!=1.0)
			F_Entropy_Under_W = numOfFW*Math.log(r_F_W)+numOfNotFW*Math.log(1-r_F_W);
		double F_Entropy_Under_NotW = 0;
		if(r_F_NotW != 0d && r_F_NotW!=1.0)
			F_Entropy_Under_NotW = numOfFNotW*Math.log(r_F_NotW)+numOfNotFNotW*Math.log(1-r_F_NotW);
		double LR = F_Entropy-F_Entropy_Under_W-F_Entropy_Under_NotW;
			return -2*LR;
	}

	public void getNumReview()
	{
		mongo.useCollection("dpFoodReview");
		numReview = mongo.count();
		
	}
	
	public void getWordDocumentFreq()
	{
		mongo.useCollection("dpFoodWord");
		DBCursor cursor = mongo.find(null,new String[]{"wId","df","wpos"});
		
		try 
		{
            while(cursor.hasNext()) 
            {
                DBObject entry = cursor.next();
                idDf.put((Integer)entry.get("wId"), (Integer)entry.get("df"));
                idWpos.put((Integer)entry.get("wId"),(String)entry.get("wpos"));
               
                
            }
        
            System.out.println(idDf.size());
        } 
		finally 
		{
            cursor.close();
        }
	}
	
	public void mergePairInfo(String distancePartDirectory, String lhrPartFile)
	{
		FReader fr = new FReader(lhrPartFile);
		String line = null;
		while ((line = fr.readLine()) != null)
		{
			//to-do with the line;
			CoPair cp = new CoPair();
			cp.loadLhrPart(line);
			int head = cp.getHead();
			int tid = cp.getTail();
			addPair(head,tid,cp);
		}
		fr.close();
		String filenames[] = FileSystemOperation.listFilenames(distancePartDirectory);
		for(String filename:filenames)
		{
			System.out.println(distancePartDirectory+filename);
			fr = new FReader(distancePartDirectory+filename);
			line = null;
			while ((line = fr.readLine()) != null)
			{
				//to-do with the line;
				String tokens[] = line.split("\\|");
				int head = Integer.parseInt(tokens[0]);
				int tail = Integer.parseInt(tokens[1]);
				CoPair cp = getPair(head,tail);
				if(cp!=null)
					cp.loadDistancePart(line);
			}
			fr.close();
		}
		
	}
	
	public void mergedPairInfoToFile( String mergedFile)
	{
		FWriter fw = new FWriter(mergedFile);
		for(Integer head:coPairInfoMap.keySet())
		{
			HashMap<Integer, CoPair> entry = coPairInfoMap.get(head);
			for(Integer tail:entry.keySet())
			{
				CoPair cp = entry.get(tail);
				fw.println(cp.toString());
			}
		}
		fw.close();
	}
	
	public void mergePairInfoToMongo()
	{
		mongo.useCollection("dpFoodCoPairInfo");
		for(Integer head:coPairInfoMap.keySet())
		{
			HashMap<Integer, CoPair> entry = coPairInfoMap.get(head);
			for(Integer tail:entry.keySet())
			{
				CoPair cp = entry.get(tail);
				mongo.saveDBObject(cp.toMongoObject());
			}
		}
	}
	
	
	/**
	 * @param head
	 * @param tail
	 * @return
	 */
	private CoPair getPair(int head, int tail)
	{
		// TODO Auto-generated method stub
		HashMap<Integer, CoPair> entry = this.coPairInfoMap.get(head);
		if (entry != null)
		{
			CoPair cp = entry.get(tail);
			return cp;
		} 
		return null;
	}

	private void addPair(Integer first, Integer second, CoPair cp)
	{
		// TODO Auto-generated method stub

		HashMap<Integer, CoPair> entry = this.coPairInfoMap.get(first);
		if (entry == null)
		{
			entry = new HashMap<Integer, CoPair>();
			entry.put(second, cp);
			this.coPairInfoMap.put(first, entry);
		} 
		else
		{
			CoPair value = entry.get(second);
			if(value==null)
			{
				entry.put(second, cp);
			}
			else
			{
				System.err.println("it already has value");
			}
		}

	}
	
	
	public static void main(String args[])
	{
		MongoDB mongo = new MongoDB();
		CoPairInfo cppi = new CoPairInfo(mongo);
		
		String pairOri = "D:/pairInfo.txt";
		String pairFilt = "D:/pairInfoFilter.txt";
		
		
		cppi.calcAllPairMetric(pairOri);
		cppi.calcNum(pairOri,pairFilt);
		String distancePartDirectory="E:\\workspace\\rankingdata\\reviewShuffle\\";
		String lhrPartFile = "D:\\pairInfoFilter.txt";
		//String mergedFile = "D:\\mergedPairInfoFilter.txt";
		
		cppi.mergePairInfo(distancePartDirectory, lhrPartFile);
		cppi.mergePairInfoToMongo();
	}
}
