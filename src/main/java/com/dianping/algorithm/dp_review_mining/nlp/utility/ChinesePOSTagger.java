/**
 * Project: FeatureLib
 * 
 * File Created at 2012-8-22
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



import java.util.List;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.recognition.NatureRecognition;



/**
 * TODO Comment of ChinesePOSTagger
 * @author rui.xie
 *
 */
public class ChinesePOSTagger
{

	/**
	 * @param string
	 * @return
	 */
	public static String pos_tag(String string)
	{
		// TODO Auto-generated method stub
		
		List<Term> terms = ToAnalysis.paser(string);
		NatureRecognition natureRecognition = new NatureRecognition(terms);
		natureRecognition.recogntion() ;
		StringBuilder sb = new StringBuilder();
		int i=0;
		for(Term term:terms)
		{
			String termStr = term.toString();
			if(termStr.indexOf('/')==-1)
				termStr = termStr.concat("/null");
			sb.append(termStr);
			if(i!=terms.size()-1)
				sb.append(' ');
			++i;
		}
		return sb.toString();
	}
	
	public static void main(String[] args) 
	{
		String str = "很热情，并没有因为我是第一次去只了解下而没消费而冷漠。人不多不少";
		System.out.println(ChinesePOSTagger.pos_tag(str));
	}


	
}
