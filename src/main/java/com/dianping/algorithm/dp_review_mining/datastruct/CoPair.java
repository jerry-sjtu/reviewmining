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
package com.dianping.algorithm.dp_review_mining.datastruct;

import java.util.Comparator;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * TODO Comment of CoPair
 * @author rui.xie
 *
 */
public class CoPair
{
	private static Logger LOGGER = Logger.getLogger(CoPair.class.getName());
	
	private int head;
	private String h_wpos;
	private int tail;
	private String t_wpos;
	private int headCount;
	private int tailCount;
	private int num;
	private double pmi;
	private double aveDistance;
	private double avePunctNum;
	private double h_post_prob;
	private double t_post_prob;
	private double h_lhr;
	private double t_lhr;
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(head+","+h_wpos+","+headCount+","+h_post_prob+","+h_lhr+",");
		sb.append(tail+","+t_wpos+","+tailCount+","+t_post_prob+","+t_lhr+",");
		sb.append(num+","+aveDistance+","+avePunctNum+","+pmi);
		return sb.toString();
	}
	
	public double getAveDistance()
	{
		return aveDistance;
	}

	public void setAveDistance(double aveDistance)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.aveDistance = aveDistance;
	}

	public double getAvePunctNum()
	{
		return avePunctNum;
	}

	public void setAvePunctNum(double avePunctNum)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
		this.avePunctNum = avePunctNum;
	}

	public double getH_post_prob()
	{
		return h_post_prob;
	}

	public void setH_post_prob(double h_post_prob)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.h_post_prob = h_post_prob;
	}

	public double getT_post_prob()
	{
		return t_post_prob;
	}

	public void setT_post_prob(double t_post_prob)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.t_post_prob = t_post_prob;
	}

	public double getH_lhr()
	{
		return h_lhr;
	}

	public void setH_lhr(double h_lhr)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.h_lhr = h_lhr;
	}

	public double getT_lhr()
	{
		return t_lhr;
	}

	public void setT_lhr(double t_lhr)
	{
		//LOGGER.debug(new Exception().getStackTrace()[0].getMethodName());
	
		this.t_lhr = t_lhr;
	}

	
	public int getHead()
	{
		return head;
	}

	public int getTail()
	{
		return tail;
	}

	public int getNum()
	{
		return num;
	}

	public double getPmi()
	{
		return pmi;
	}

	public void loadFromDBObjectByCoPair(DBObject obj)
	{
		head = (Integer) obj.get("hid");
		tail = (Integer) obj.get("tid");
		num = (Integer) obj.get("dfpair");
		pmi = (Double) obj.get("pmi");
	}
	public void loadFromDBObjectByCoPairInfo(DBObject obj)
	{
		// TODO Auto-generated method stub
				
				head = (Integer) obj.get("h");
				h_post_prob = (Double) obj.get("hpp");
				h_lhr = (Double) obj.get("hlhr");
				tail = (Integer) obj.get("t");
				t_post_prob = (Double) obj.get("tpp");
				t_lhr = (Double) obj.get("tlhr");
				num = (Integer) obj.get("num");
				aveDistance = (Double) obj.get("aveDis");
				avePunctNum = (Double) obj.get("avePunc");
				pmi = (Double) obj.get("pmi");
	}
	public static CoPairNumComparator getNumComparator(int inverse)
	{
		return new CoPairNumComparator(inverse);
	}
	public static CoPairPmiComparator getPmiComparator(int inverse)
	{
		return new CoPairPmiComparator(inverse);
	}
	
	public void loadDistancePart(String line)
	{
		String tokens[] = line.split("\\|");
		if(tokens.length!=5)
		{
			System.err.println("format error");
		}
		this.aveDistance = Double.parseDouble(tokens[3]);
		this.avePunctNum = Double.parseDouble(tokens[4]);
	}
	
	public void loadLhrPart(String line)
	{
		String tokens[] = line.split(",");
		if(tokens.length!=12)
		{
			System.err.println("format error");
		}
		this.head = Integer.parseInt(tokens[0]);
		this.headCount = Integer.parseInt(tokens[1]);
		this.tail = Integer.parseInt(tokens[2]);
		this.tailCount = Integer.parseInt(tokens[3]);
		this.num = Integer.parseInt(tokens[4]);
		this.pmi = Double.parseDouble(tokens[5]);
		this.h_post_prob = Double.parseDouble(tokens[6]);
		this.t_post_prob = Double.parseDouble(tokens[7]);
		this.h_lhr = Double.parseDouble(tokens[8]);
		this.t_lhr = Double.parseDouble(tokens[9]);
		this.h_wpos = tokens[10];
		this.t_wpos = tokens[11];
	}

	/**
	 * @return
	 */
	public DBObject toMongoObject()
	{
		// TODO Auto-generated method stub
		DBObject obj = new BasicDBObject();
		obj.put("h", head);
		obj.put("hpp", h_post_prob);
		obj.put("hlhr", h_lhr);
		obj.put("t", tail);
		obj.put("tpp", t_post_prob);
		obj.put("tlhr", t_lhr);
		obj.put("num", num);
		obj.put("aveDis", aveDistance);
		obj.put("avePunc", avePunctNum);
		obj.put("pmi", pmi);
		
		return obj;
	}
	
}
class CoPairNumComparator implements Comparator<CoPair>
{
	int inversed = 1;
	/**
	 * @param inversed
	 */
	public CoPairNumComparator(int inversed)
	{
		super();
		this.inversed = inversed;
	}



	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(CoPair o1, CoPair o2)
	{
		// TODO Auto-generated method stub
		double res = o1.getNum()-o2.getNum();
		if(res>0) return 1*inversed;
		else if(res<0) return -1*inversed;
		else return 0;
	}
}

class CoPairPmiComparator implements Comparator<CoPair>
{
	int inversed = 1;

	
	
	/**
	 * @param inversed
	 */
	public CoPairPmiComparator(int inversed)
	{
		super();
		this.inversed = inversed;
	}



	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(CoPair o1, CoPair o2)
	{
		// TODO Auto-generated method stub
		double res = o1.getPmi()-o2.getPmi();
		if(res>0) return 1*inversed;
		else if(res<0) return -1*inversed;
		else return 0;
	}
	
	
	
}