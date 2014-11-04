/**
 * Project: FeatureLib
 * 
 * File Created at 2012-8-27
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

import java.util.HashMap;

/**
 * TODO Comment of ICTCLASTagToPenntreeTagAdaptor
 * @author rui.xie
 *
 */
public class ICTCLASTagToPenntreeTagAdaptor
{
	private static HashMap<String,String> tagMap;

	static
	{
		tagMap = new HashMap<String, String>();
		// NOUN
		tagMap.put("n", "NN");
		tagMap.put("nr", "NR");
		tagMap.put("nr1", "NR");
		tagMap.put("nr2", "NR");
		tagMap.put("nrj", "FW");
		tagMap.put("nrf", "FW");
		tagMap.put("ns", "NR");
		tagMap.put("nsf", "FW");
		tagMap.put("nt", "NR");
		tagMap.put("nz", "NR");
		tagMap.put("nl", "NN");
		tagMap.put("ng", "NN");
		
		tagMap.put("t", "NT");
		tagMap.put("tg", "NT");
		
		tagMap.put("s", "LC");
		
		tagMap.put("f", "LC");
		
		tagMap.put("v", "VV");
		tagMap.put("vd", "VV");
		tagMap.put("vn", "VV");
		tagMap.put("vshi", "VC");
		tagMap.put("vyou", "VE");
		tagMap.put("vf", "VV");
		tagMap.put("vx", "VV");
		tagMap.put("vi", "VV");
		tagMap.put("vl", "VV");
		tagMap.put("vg", "VV");
		
		tagMap.put("z", "VA");
		
		tagMap.put("mq", "M");
		
		tagMap.put("d", "AD");
		
		tagMap.put("pba","BA");
		tagMap.put("p", "P");
		
		tagMap.put("c", "CC");
		tagMap.put("cc", "CC");
		
		tagMap.put("u", "AD");
		tagMap.put("uzhe", "AS");
		tagMap.put("ule", "AS");
		tagMap.put("uguo", "AS");
		tagMap.put("ude1", "DEC");
		tagMap.put("ude2", "DEV");
		tagMap.put("ude3", "DER");
		tagMap.put("usuo", "MSP");
		tagMap.put("udeng", "ETC");
		tagMap.put("uyy", "VA");
		tagMap.put("udh", "SP");
		tagMap.put("uls", "LC");
		tagMap.put("uzhi", "AD");
		tagMap.put("ulian", "AD");
		
		tagMap.put("e", "SP");
		
		tagMap.put("y", "SP");
		
		tagMap.put("o", "ON");
		
		tagMap.put("h", "AD");
		
		tagMap.put("k", "AD");

		
		
	}
	public static String getPenntreeTag(String ictclasWord, String ictclasTag, String nextTag)
	{
		assert ictclasTag!=null&&ictclasTag.length()>=1;
		char C = ictclasTag.charAt(0);
		if(C=='w'|| C=='x')
		{
			return "PU";
		}
		else if(C=='q')
		{
			return "M";
		}
		else if(C=='r')
		{
			return "PN";
		}
		else if(C=='b')
		{
			return "JJ";
		}
		else if(C=='a')
		{
			if(nextTag!=null && nextTag.equals("ude1"))
				return "VA";
			else
				return "JJ";
		}
		else if(C=='p')
		{
			if(nextTag!=null && nextTag.startsWith("v"))
				return "SB";
			else 
				return "LB";
		}
		else if(C=='m')
		{
			if(ictclasTag.equals("m")&&ictclasWord.contains("第"))
				return "OD";
			else
				return "CD";
		}
		else
		{
			String tag = tagMap.get(ictclasTag);
			if(tag!=null)
				return tag;
			else
				return "error";
		}
	}
	
}
