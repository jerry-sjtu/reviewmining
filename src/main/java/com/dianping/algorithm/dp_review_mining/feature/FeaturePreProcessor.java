/**
 * Project: review-mining-single
 * 
 * File Created at 2012-12-11
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
package com.dianping.algorithm.dp_review_mining.feature;

import java.util.ArrayList;
import java.util.HashSet;

import javax.sound.sampled.Line;

import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.utility.FReader;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;

/**
 * TODO Comment of FeaturePreProcessor
 * @author rui.xie
 *
 */
public class FeaturePreProcessor
{
	private static Logger LOGGER = Logger.getLogger(FeaturePreProcessor.class
			.getName());
	
	private static String wrongWords[] = {"很不错","丁香","日本","永安","下午茶套餐","面包蛋糕","建筑","北海道","脆皮","小凉菜","咖喱系列","中辣","很差","蛋糕面包","至尊系列","家乐福","饭前饭后","米奇","酒香","刷锅","小海鲜","院长","爽滑","帝王","各种海鲜","室内装潢","双人套餐","美邦","和牛肉","特饮","虾线","运动服","各种沙拉","超值午餐","各种面","汤菜","和菜","新世界","鸡比","小玩意","强烈推荐","我们的沙拉","先锋","香浓","腿肉","瑶柱","大牌","麻辣鲜","青岛","可口","开胃","地理环境","选择性","西单","免费冰激凌","情侣团","灯光音乐","情侣","免费的酸姜","免费红茶","独元","别克新君威","贴钻","串锅","水货","永乐曲","明朗","红利商品","多人沙发","莫妮卡","黄瓜味","整体软装","全内","抗氧化","马克","双腋下","马达","世界最大彩灯展","裸钻","娇韵诗","徐先进","荣荣","和美","戒指","ＩＴ","托摆","导脂"};
	private static String containWords[] = {"各种","都喜欢","全都","都很"};
	
	public void convertToChildParentPair(String ontologyPath)
	{
		ArrayList<String> stackWord = new ArrayList<String>();
		ArrayList<Integer> stackLevel = new ArrayList<Integer>();
		
		FReader fr = new FReader(ontologyPath);
		
		String line = null;
		line = fr.readLine();
		
		
		
		String lastWord = line;
		int lastLevel = 0;
		
		while ((line = fr.readLine()) != null)
		{
			if(line.length()==0)
				continue;
			if(line.contains("/"))
				line=line.substring(0,line.indexOf('/'));
			int numOfTab = 0;
			for(int i=0;i<line.length();++i)
			{
				if(line.charAt(i)=='\t')
					++numOfTab;
				else
					break;
			}
			if(numOfTab==lastLevel+1)
			{
				stackWord.add(lastWord);
				stackLevel.add(lastLevel);
				System.out.println(line.substring(numOfTab)+"\t"+lastWord);
				lastWord = line.substring(numOfTab);
				++lastLevel;
			}
			else if(numOfTab==lastLevel)
			{
				lastWord = line.substring(numOfTab);
				System.out.println(line.substring(numOfTab)+"\t"+stackWord.get(stackWord.size()-1));
			}
			else if(numOfTab<lastLevel)
			{
				int internal = lastLevel-numOfTab;
				for(int i=0;i<internal;++i)
				{
					stackWord.remove(stackWord.size()-1);
					lastLevel = stackLevel.remove(stackLevel.size()-1);
				}
				System.out.println(line.substring(numOfTab)+"\t"+stackWord.get(stackWord.size()-1));
				lastWord = line.substring(numOfTab);
			}
			
		}
		fr.close();
	}
	
	public void getSynonym(String ontologyPath)
	{
		FReader fr = new FReader(ontologyPath);
		String line = null;
		while ((line = fr.readLine()) != null)
		{
			//to-do with the line;
			line = line.replaceAll("\t*", "");
			if(line.contains("/"))
			{
				String tokens[] = line.split("/");
				int i=0;
				for(String token:tokens)
				{
					++i;
					System.out.print(token);
					if(i!=tokens.length)
						System.out.print(" ");
				}
				System.out.println();
			}
		}
		fr.close();
	}
	
	public void getUncheckedWord(String originTree, String newTree, String outPath)
	{
		HashSet<String> originWord = new HashSet<String>();
		HashSet<String> checkedWord = new HashSet<String>();
		
		FReader fr = new FReader(newTree);
		String line = null;
		while ((line = fr.readLine()) != null)
		{
			//to-do with the line;
			line = line.replaceAll("\t*", "");
			if(line.contains("/"))
			{
				String tokens[] = line.split("/");
				for(String token:tokens)
					checkedWord.add(token);
			}
			else
			{
				checkedWord.add(line);
			}
		}
		fr.close();
		
		FWriter fw = new FWriter(outPath);
		fr = new FReader(originTree);
		line = null;
		while ((line = fr.readLine()) != null)
		{
			//to-do with the line;
			String tokens[] = line.split("\t");
			if(tokens.length==2)
			{
				if(checkedWord.contains(tokens[0]))
				{
					
				}
				else if(hasContainWords(tokens[0]))
				{
					
				}
				else if(inWrongWords(tokens[0]))
				{
					
				}
				else
				{
					fw.println(line);
				}
			}
		}
		fr.close();
		fw.close();
	}
	
	/**
	 * @param string
	 * @return
	 */
	private boolean inWrongWords(String string)
	{
		// TODO Auto-generated method stub
		for(String word:wrongWords)
		{
			if(string.equals(word))
				return true;
		}
		return false;
	}

	/**
	 * @param string
	 * @return
	 */
	private boolean hasContainWords(String string)
	{
		// TODO Auto-generated method stub
		for(String word:containWords)
		{
			if(string.contains(word))
				return true;
		}
		return false;
	}

	public static void main(String[] args)
	{
		FeaturePreProcessor  fpp = new FeaturePreProcessor();
		String ontologyPath = "C:\\Users\\rui.xie\\Desktop\\ontology.txt";
		//fpp.getUncheckedWord("./data/feature/food-feature-tree-file.txt", "C:\\Users\\rui.xie\\Desktop\\ontology.txt","C:\\Users\\rui.xie\\Desktop\\toRemove.txt");
		//fpp.convertToChildParentPair(ontologyPath);
		//fpp.getSynonym(ontologyPath);
	}
	
	
}
