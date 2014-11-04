/**
 * Project: review-mining-single
 * 
 * File Created at 2012-11-13
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

import org.apache.log4j.Logger;

/**
 * TODO Comment of Word
 * @author rui.xie
 *
 */
public class Word
{
	private static Logger LOGGER = Logger.getLogger(Word.class.getName());
	
	public String wpos;
	public int wId;
	public int df;
	public int wf;
	public char pos;
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		sb.append(wId);
		sb.append(',');
		sb.append(wpos);
		sb.append(',');
		sb.append(pos);
		sb.append(',');
		sb.append(df);
		sb.append(',');
		sb.append(wf);
		return sb.toString();
	}
}
