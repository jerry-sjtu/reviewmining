/**
 * Project: FeatureLib
 * 
 * File Created at 2012-8-23
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

import org.apache.log4j.Logger;


import com.dianping.algorithm.dp_review_mining.utility.FReader;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;
import com.dianping.algorithm.dp_review_mining.utility.FileSystemOperation;


/**
 * TODO Comment of FileTagger
 * @author rui.xie
 *
 */
public class FileTagger
{
	private static Logger LOGGER = Logger.getLogger(FileTagger.class.getName());
 
	public String tagReview(String review, String reviewId)
	{
		review = ReviewProcessor.filtingBeforeTagging(review);
		String sentences[] = review.split("[。！？；~]");
		String result_str = "";
		
		
		for(int i=0; i<sentences.length;++i)
		{
			//System.err.println(reviewId+":"+i);
			//System.err.flush();
			if(sentences[i].length()>2 /*&& sentences[i].length()<500*/)
			{
				
				String tagged = ChinesePOSTagger.pos_tag(sentences[i]);
				result_str = result_str + tagged +" 。/wj ";
			}
//			else if(sentences[i].length()>=500)
//			{
//				LOGGER.error(reviewId+":"+sentences[i]);
//			}
		}
		
		return result_str;
	}
	
	public void tagFile(String original, String tokenized)
	{
		LOGGER.info("tagger "+original+" to "+tokenized);
		
		FReader fr = new FReader(original);
		FWriter fw = new FWriter(tokenized);
		String line = null;
		int num = 0;
		while((line=fr.readLine())!=null)
		{
			if(line.length()>0)
			{
				String tokens[] = line.split("\t");
				if(tokens.length==4)
				{
					String tagged = tagReview(tokens[3],tokens[2]);
					fw.println(tokens[2]+"\t"+tokens[1]+"\t"+tagged);
					fw.flush();
				}
			}
		}
		fr.close();
		fw.close();
		LOGGER.info("tagger "+original+" has finished");
		
		//FileSystemOperation.move(original,"E:/workspace/rankingdata/tagged_success_1128/");
	}
	public void tagDir(String dir, String dir_res)
	{
		String filenames[] = FileSystemOperation.listFilenames(dir);
		for(int i=filenames.length-1; i>=0; --i)
		{
			tagFile(dir+filenames[i], dir_res+filenames[i]+"_tagged");
		}
	}
	
	public static void main(String args[])
	{
		FileTagger ft = new FileTagger();
		String dir = "E:/workspace/rankingdata/reviewContent1127_MYSQL/";
		String dir_res = "E:/workspace/rankingdata/reviewContentTagged1127_MYSQL/";
		ft.tagDir(dir,dir_res);
		//String file = "E:/workspace/rankingdata/reviewContent/id_reviewBody_5918287_2";
		//ft.tagFile(file, file+"tagged");
	}
}
