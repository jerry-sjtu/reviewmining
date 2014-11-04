/**
 * Project: review-mining-single
 * 
 * File Created at 2012-11-12
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
package com.dianping.algorithm.dp_review_mining.associate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.dianping.algorithm.dp_review_mining.associate_helper.Attribute;
import com.dianping.algorithm.dp_review_mining.associate_helper.AttributeModifier;
import com.dianping.algorithm.dp_review_mining.associate_helper.AttributeOpinionPair;
import com.dianping.algorithm.dp_review_mining.associate_helper.Opinion;
import com.dianping.algorithm.dp_review_mining.associate_helper.OpinionModifier;
import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.datastruct.AnalyzedReview;
import com.dianping.algorithm.dp_review_mining.datastruct.CoPair;
import com.dianping.algorithm.dp_review_mining.datastruct.CoPairFeatures;
import com.dianping.algorithm.dp_review_mining.datastruct.Sentence;
import com.dianping.algorithm.dp_review_mining.datastruct.SubSentence;
import com.dianping.algorithm.dp_review_mining.datastruct.Vocubulary;
import com.dianping.algorithm.dp_review_mining.sentiment.SentimentLexicon;
import com.dianping.algorithm.dp_review_mining.summary.MongoUtility;
import com.dianping.algorithm.dp_review_mining.utility.Const;
import com.dianping.algorithm.dp_review_mining.utility.FReader;


/**
 * TODO Comment of AttributeOpinionAssociator
 * @author rui.xie
 *
 */
public class OldAttributeOpinionAssociator extends AttributeOpinionAssociator
{
	private static Logger LOGGER = Logger
			.getLogger(OldAttributeOpinionAssociator.class.getName());
	
	//private ArrayDeque<String> feaures;
	//private ArrayDeque<String> opinion;
	
	//当前评论
	private AnalyzedReview review;
	//存储属性、评价对的列表
	private ArrayList<AttributeOpinionPair> aoPairList;
	//当前句子
	private Sentence currentSentence;
	//当前句子的子句
	private SubSentence currentSubSentence;
	//当前句子在评论中的index
	private int currentSentenceIndex;
	//当前句子的子句数
	private int currentSentenceSubSentenceNum;
	//当前子句在当前句子中的index
	private int currentSubSentenceIndex;
	//词汇表
	private static Vocubulary vocubulary;
	//词对特征表
	private static CoPairFeatures coPairFeatures;
	//候选的属性集
	private static HashSet<String> candidateFeatures;
	
	//情感词典
	private static SentimentLexicon sl;
	
	
	public static void init() 
	{
		
		vocubulary = new Vocubulary();
		coPairFeatures = new CoPairFeatures();
		candidateFeatures = new HashSet<String>();
		
		MongoDB mongo = new MongoDB();
		// 加载词汇表
		System.out.println("load vocubulary");
		vocubulary.loadVocubuarly(mongo);
		
		// 加载词表特征表
		System.out.println("load copairFeature");
		coPairFeatures.loadCoPairFeatures(mongo);
		System.out.println("load copairFeature finish");
		LOGGER.info("start to load attibutes");
		
		String featureTreeFileName = "";
		String featureSynonymFileName = "";
		String sentimentFileName = "";
		String sentimentNegFileName = "";
		Properties properties = new Properties();
		try {

			String path = Const.FOOD_CONFIG_FILE;
			InputStream in = new FileInputStream(path);
			properties.load(in);
			
			
			featureTreeFileName = Const.path+properties.getProperty(Const.FEATURE_TREE_FILE_PROPERTY);
			featureSynonymFileName = Const.path+properties.getProperty(Const.SYNONYM_FILE_PROPERTY);
			
			sentimentFileName = Const.path+properties.getProperty(Const.SENTIMENT_FILE_PROPERTY);
			sentimentNegFileName = Const.path+properties.getProperty(Const.SENTIMENT_FILE_FILE_PROPERTY);
			
			in.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		// 加载候选属性
		FReader fr = new FReader(featureTreeFileName);
		String line = null;
		while ((line = fr.readLine()) != null)
		{
			//to-do with the line;
			candidateFeatures.add(line.split("\\s")[0]);
		}
		fr.close();
		fr = new FReader(featureSynonymFileName);
		line = null;
		while ((line = fr.readLine()) != null)
		{
			//to-do with the line;
			String tokensOfFeatures[] = line.split("\\s");
			for(String tokenOfF:tokensOfFeatures)
			{
				if(tokenOfF.length()>0)
				{
					candidateFeatures.add(tokenOfF);
				}
			}
			
		}
		fr.close();
		LOGGER.info("load attibutes finished");
		LOGGER.info("start to load dishes");
		
		sl = new SentimentLexicon();
		sl.loadLexicon(sentimentFileName, sentimentNegFileName);
		LOGGER.info("loading sentiment finished");
	}
	
	
	// 是否是debug模式 方便调试
	private boolean isDebug;
	
	public OldAttributeOpinionAssociator()
	{
		aoPairList = new ArrayList<AttributeOpinionPair>();
		currentSentenceIndex = -1;
		currentSubSentenceIndex = -1;
		currentSentence = null;
		currentSubSentence = null;
		
		isDebug = false;
		
	}
	
	
	
	public List<AttributeOpinionPair> associate(AnalyzedReview ar)
	{
		List<AttributeOpinionPair> pairList = new ArrayList<AttributeOpinionPair>();
		review = ar;
		//LOGGER.info(review.rId);

		//System.out.println(review.rId+":"+review.getSimpleTagContent());

		
		
		/**associate是类的核心方法，对外API
			@parameter AnalyzedReview ar : 带词性标注的评价
			
			首先调用initialize初始化。将当前句子，当前子句等变量批向第一句第一子句
			然后对子句调用doCurrent依次处理，如果还有未处理完，既hasNextSubSentence返回true
			则调用nextSubSentence对currentSubSentence赋值
		
		*/
		if(initialize())
		{
			while(true)
			{
				doCurrent();
				if(hasNextSubSentence())
				{
					nextSubSentence();
				}
				else
				{
					break;
				}
			}
		}
		else
		{
			LOGGER.error(review.rId+" is not initialized sucessfully");
		}
		for(AttributeOpinionPair aoPair:aoPairList)
		{

//			System.out.print(aoPair.toString()+" ");
			pairList.add(aoPair);
		}
//		System.out.println();

	
		if(isDebug)
		{
			System.out.println("enter");
			Scanner scanner = new Scanner(System.in);
			scanner.nextLine();
		}
		return pairList;
	}
	

	/**
	 * 先统计当前子句属性词和评论词的数目，然后调用generateAOPair处理
	 */
	private void doCurrent()
	{
		// TODO Auto-generated method stub
		if(currentSubSentence==null)
		{
			return;
		}
		int numA = 0;
		int numN = 0;
		
		
		for(int i=0;i<currentSubSentence.numWord;++i)
		{
			if(currentSubSentence.words[i].length()>0)
			{
				switch (currentSubSentence.tagsIctclasSimple[i])
				{
				case 'a':
					++numA;
					break;
				case 'n':
					++numN;
					break;
				case 'v':
				{
					if(sl.contains(currentSubSentence.words[i]))
					{
						++numA;
					}
					else if(candidateFeatures.contains(currentSubSentence.words[i]))
					{
						++numN;
					}
					break;
				}
				default:
					break;
				}
			}
		}
		ArrayList<AttributeOpinionPair> pairList = generateAOPair(numA,numN);
		if(pairList!=null)
			aoPairList.addAll(pairList);
	}

	/**
	 * @param numA 评价词数量
	 * @param numN 属性词数量
	 * @return
	 */
	private ArrayList<AttributeOpinionPair> generateAOPair(int numA,int numN)
	{
		// TODO Auto-generated method stub
		//System.out.println(currentSubSentence.getSimpleTagContent());
		ArrayList<AttributeOpinionPair> aoList;
		Attribute attribute = null;
		Opinion opinion = null;
		
		
		//没有属性词或评价词的情况
		if(numA*numN==0)
		{
			aoList = implicitAssociate();
			return aoList;
		}
		else
		{
			/**
			 * 一个评价词和一个属性词的情况，，
			 * 通过计算出evaluate计算出两者搭配的分值 ，
			 * 分值大于0则作为搭配抽取出来
			 */
			if(numA==1&&numN==1)
			{
				aoList = new ArrayList<AttributeOpinionPair>();
				AttributeOpinionPair aoPair = new AttributeOpinionPair(review.rId);
				
				for(int i=0;i<currentSubSentence.numWord;++i)
				{
					if(currentSubSentence.tagsIctclasSimple[i]=='n')
					{
						Attribute attr = new Attribute();
						attr.attribute = currentSubSentence.words[i];
						attr.attributeWordIndex = i;
						attr.attributeSubSentIndex = currentSubSentenceIndex;
						attr.attributeSentIndex = currentSentenceIndex;
						attr.pos = 'n';
						attribute = attr;
					}
					else if(currentSubSentence.tagsIctclasSimple[i]=='a')
					{
						Opinion opin = new Opinion();
						opin.opinion = currentSubSentence.words[i];
						opin.opinionWordIndex = i;
						opin.opinionSubSentIndex = currentSubSentenceIndex;
						opin.opinionSentIndex = currentSentenceIndex;
						opin.pos = 'a';
						opinion =  opin;
					}
					else if(currentSubSentence.tagsIctclasSimple[i]=='v')
					{
						if(sl.contains(currentSubSentence.words[i]))
						{
							Opinion opin = new Opinion();
							opin.opinion = currentSubSentence.words[i];
							opin.opinionWordIndex = i;
							opin.opinionSubSentIndex = currentSubSentenceIndex;
							opin.opinionSentIndex = currentSentenceIndex;
							opin.pos = 'v';
							opinion =  opin;
						}
						else if(candidateFeatures.contains(currentSubSentence.words[i]))
						{
							Attribute attr = new Attribute();
							attr.attribute = currentSubSentence.words[i];
							attr.attributeWordIndex = i;
							attr.attributeSubSentIndex = currentSubSentenceIndex;
							attr.attributeSentIndex = currentSentenceIndex;
							attr.pos = 'v';
							attribute = attr;
						}
					}
				}
				double score = evaluate(attribute,opinion);
				if(score>0)
				{
					aoPair.attr = attribute;
					aoPair.opinion = opinion;
					aoPair.score = score;
					miningModifier(aoPair);
					
					//System.out.println("["+aoPair.attr.attribute+","+aoPair.opinion.opinion+"]");
					
					aoList.add(aoPair);
					return aoList;
				}
				
			}

			/**
			 * 一个评价词和多个属性词的情况，遍历属性词，将每个属性词与评价词搭配，
			 * 通过计算出evaluate计算出两者搭配的分值 ，取分值最大的且分值大于0的组合
			 * 作为抽取出的搭配
			 */
			else if(numA==1)
			{
				
				// to be conintue;
				aoList = new ArrayList<AttributeOpinionPair>();
				
				ArrayList<Attribute> candidateAttribute = new ArrayList<Attribute>();
				for(int i=0;i<currentSubSentence.numWord;++i)
				{
					if(currentSubSentence.tagsIctclasSimple[i]=='n')
					{
						Attribute attr = new Attribute();
						attr.attribute = currentSubSentence.words[i];
						attr.attributeWordIndex = i;
						attr.attributeSubSentIndex = currentSubSentenceIndex;
						attr.attributeSentIndex = currentSentenceIndex;
						attr.pos = 'n';
						candidateAttribute.add(attr);
					}
					else if(currentSubSentence.tagsIctclasSimple[i]=='a')
					{
						Opinion opin = new Opinion();
						opin.opinion = currentSubSentence.words[i];
						opin.opinionWordIndex = i;
						opin.opinionSubSentIndex = currentSubSentenceIndex;
						opin.opinionSentIndex = currentSentenceIndex;
						opin.pos = 'a';
						opinion = opin;
					}
					else if(currentSubSentence.tagsIctclasSimple[i]=='v')
					{
						if(sl.contains(currentSubSentence.words[i]))
						{
							Opinion opin = new Opinion();
							opin.opinion = currentSubSentence.words[i];
							opin.opinionWordIndex = i;
							opin.opinionSubSentIndex = currentSubSentenceIndex;
							opin.opinionSentIndex = currentSentenceIndex;
							opin.pos = 'v';
							opinion =  opin;
						}
						else if(candidateFeatures.contains(currentSubSentence.words[i]))
						{
							
							Attribute attr = new Attribute();
							attr.attribute = currentSubSentence.words[i];
							attr.attributeWordIndex = i;
							attr.attributeSubSentIndex = currentSubSentenceIndex;
							attr.attributeSentIndex = currentSentenceIndex;
							attr.pos = 'v';
							candidateAttribute.add(attr);
							
							
						}
					}
					
				}
				
				double scores[] = new double[candidateAttribute.size()];
				int index = 0;
				
				for(Attribute attr:candidateAttribute)
				{
					scores[index++] = evaluate(attr, opinion);
				}
				double maxScore = Double.NEGATIVE_INFINITY;
				int maxIndex = -1;
				for(index=0;index<scores.length;++index)
				{
                    if(scores[index]>maxScore)
					{
						maxScore = scores[index];
						maxIndex = index;
					}
				}
				if(maxIndex>=0 && maxScore>0)
				{
					AttributeOpinionPair aoPair = new AttributeOpinionPair(review.rId);
					aoPair.attr = candidateAttribute.get(maxIndex);
					aoPair.opinion = opinion;
					miningModifier(aoPair);
					aoList.add(aoPair);
					return aoList;
				}
				
			}
			/**
			 * 多个评价词和一个属性词的情况，遍历属性词，将每个评价词与属性词搭配，
			 * 通过计算出evaluate计算出两者搭配的分值 ，取分值最大的且分值大于0的组合
			 * 作为抽取出的搭配
			 */
			else if(numN==1)
			{
				aoList = new ArrayList<AttributeOpinionPair>();
				ArrayList<Opinion> candidateOpinion = new ArrayList<Opinion>();
				for(int i=0;i<currentSubSentence.numWord;++i)
				{
					if(currentSubSentence.tagsIctclasSimple[i]=='n')
					{
						Attribute attr = new Attribute();
						attr.attribute = currentSubSentence.words[i];
						attr.attributeWordIndex = i;
						attr.attributeSubSentIndex = currentSubSentenceIndex;
						attr.attributeSentIndex = currentSentenceIndex;
						attr.pos = 'n';
						attribute = attr;
					}
					else if(currentSubSentence.tagsIctclasSimple[i]=='a')
					{
						Opinion opin = new Opinion();
						opin.opinion = currentSubSentence.words[i];
						opin.opinionWordIndex = i;
						opin.opinionSubSentIndex = currentSubSentenceIndex;
						opin.opinionSentIndex = currentSentenceIndex;
						opin.pos = 'a';
						candidateOpinion.add(opin);
					}
					else if(currentSubSentence.tagsIctclasSimple[i]=='v')
					{
						if(sl.contains(currentSubSentence.words[i]))
						{
							Opinion opin = new Opinion();
							opin.opinion = currentSubSentence.words[i];
							opin.opinionWordIndex = i;
							opin.opinionSubSentIndex = currentSubSentenceIndex;
							opin.opinionSentIndex = currentSentenceIndex;
							opin.pos = 'v';
							candidateOpinion.add(opin);
						}
						else if(candidateFeatures.contains(currentSubSentence.words[i]))
						{
							
							Attribute attr = new Attribute();
							attr.attribute = currentSubSentence.words[i];
							attr.attributeWordIndex = i;
							attr.attributeSubSentIndex = currentSubSentenceIndex;
							attr.attributeSentIndex = currentSentenceIndex;
							attr.pos = 'v';
							attribute = attr;
							
							
						}
					}
					
				}
				double scores[] = new double[candidateOpinion.size()];
				int index = 0;
				
				for(Opinion opin:candidateOpinion)
				{
					scores[index++] = evaluate(attribute, opin);
				}
				double maxScore = Double.NEGATIVE_INFINITY;
				int maxIndex = -1;
				for(index=0;index<scores.length;++index)
				{
                    if(scores[index]>maxScore)
					{
						maxScore = scores[index];
						maxIndex = index;
					}
				}
				if(maxIndex>=0 && maxScore>0)
				{
					AttributeOpinionPair aoPair = new AttributeOpinionPair(review.rId);
					aoPair.attr = attribute;
					aoPair.opinion = candidateOpinion.get(maxIndex);
					miningModifier(aoPair);
					aoList.add(aoPair);
					return aoList;
				}
				
			}
			
			/**
			 * 多个评价词和多个属性词的情况，以评价词为中心，对于每一个评价词，遍历属性词，
			 * 通过计算出evaluate计算出两者搭配的分值 ，取分值最大的且分值大于0的组合，
			 * 然后将评价词和属性词移出，继续处理，真到所有的评价词都处理完毕
			 */
			
			else
			{
				aoList = new ArrayList<AttributeOpinionPair>();
				ArrayList candidateAttributeOpinion = new ArrayList();
				for(int i=0;i<currentSubSentence.numWord;++i)
				{
					if(currentSubSentence.tagsIctclasSimple[i]=='n')
					{
						Attribute attr = new Attribute();
						attr.attribute = currentSubSentence.words[i];
						attr.attributeWordIndex = i;
						attr.attributeSubSentIndex = currentSubSentenceIndex;
						attr.attributeSentIndex = currentSentenceIndex;
						attr.pos = 'n';
						candidateAttributeOpinion.add(attr);
					}
					else if(currentSubSentence.tagsIctclasSimple[i]=='a')
					{
						Opinion opin = new Opinion();
						opin.opinion = currentSubSentence.words[i];
						opin.opinionWordIndex = i;
						opin.opinionSubSentIndex = currentSubSentenceIndex;
						opin.opinionSentIndex = currentSentenceIndex;
						opin.pos = 'a';
						candidateAttributeOpinion.add(opin);
					}
					else if(currentSubSentence.tagsIctclasSimple[i]=='v')
					{
						if(sl.contains(currentSubSentence.words[i]))
						{
							Opinion opin = new Opinion();
							opin.opinion = currentSubSentence.words[i];
							opin.opinionWordIndex = i;
							opin.opinionSubSentIndex = currentSubSentenceIndex;
							opin.opinionSentIndex = currentSentenceIndex;
							opin.pos = 'v';
							candidateAttributeOpinion.add(opin);
						}
						else if(candidateFeatures.contains(currentSubSentence.words[i]))
						{
							
							Attribute attr = new Attribute();
							attr.attribute = currentSubSentence.words[i];
							attr.attributeWordIndex = i;
							attr.attributeSubSentIndex = currentSubSentenceIndex;
							attr.attributeSentIndex = currentSentenceIndex;
							attr.pos = 'v';
							candidateAttributeOpinion.add(attr);
							
							
						}
					}
					
				}

				while(numA!=0)
				{
					
					for(int i=0;i<candidateAttributeOpinion.size();++i)
					{
						Object targetOpinion = candidateAttributeOpinion.get(i);
						if( targetOpinion instanceof Opinion)
						{
							ArrayList<Attribute> candidateAttribute = new ArrayList<Attribute>();
							for(int point=i-1;point>=0;--point)
							{
								Object attr = candidateAttributeOpinion.get(point);
								if ( attr instanceof Attribute)
								{
									candidateAttribute.add((Attribute)attr);
								}
							}
							for(int point=i+1;point<candidateAttributeOpinion.size();++point)
							{
								Object attr = candidateAttributeOpinion.get(point);
								if ( attr instanceof Attribute)
								{
									candidateAttribute.add((Attribute)attr);
								}
							}
							double scores[] = new double[candidateAttribute.size()];
							int index = 0;
							
							for(Attribute attr:candidateAttribute)
							{
								scores[index++] = evaluate(attr, (Opinion)targetOpinion);
							}
							
							
							double maxScore = Double.NEGATIVE_INFINITY;
							int maxIndex = -1;
							for(index=0;index<scores.length;++index)
							{
			                    if(scores[index]>maxScore)
								{
									maxScore = scores[index];
									maxIndex = index;
								}
							}
							if(maxIndex>=0 && maxScore>0)
							{
								AttributeOpinionPair aoPair = new AttributeOpinionPair(review.rId);
								aoPair.attr = candidateAttribute.get(maxIndex);
								aoPair.opinion = (Opinion) candidateAttributeOpinion.get(i);
								miningModifier(aoPair);
								aoList.add(aoPair);
								candidateAttributeOpinion.remove(aoPair.attr);
							}
							candidateAttributeOpinion.remove(targetOpinion);
							break;
						}
						
						
					}
					--numA;
					
				}
				if(aoList.isEmpty())
					return null;
				return aoList;
			}
			return null;
		}
	}

	/**
	 * @param aoPair
	 */
	private void miningModifier(AttributeOpinionPair aoPair)
	{
		// TODO Auto-generated method stub
		miningAttributeModifer(aoPair);
		miningOpinionModifer(aoPair);
		miningOrientation(aoPair);
	}



	/**
	 * @param aoPair
	 */
	private void miningOrientation(AttributeOpinionPair aoPair)
	{
		// TODO Auto-generated method stub
		
		String opin = aoPair.opinion.opinion;
		int ori = sl.getOrientation(opin);
		int negaFactor = 1;
		if(aoPair.opinModifiers!=null)
		{
			for(OpinionModifier modifier:aoPair.opinModifiers)
			{
				if(sl.getOrientation(modifier.opinionModifier)==sl.PRIVATIVE)
				{
					negaFactor*=-1;
				}
			}
		}
		if(ori==sl.POSITIVE&&negaFactor<0)
			ori=sl.NEGATIVE;
		else if(ori==sl.NEGATIVE&&negaFactor<0)
			ori=sl.POSITIVE;
		else if(ori==sl.FORCE||ori==sl.PRIVATIVE)
			ori=sl.NOTSURE;
		aoPair.orieantation= ori;
		
	}



	/**
	 * @param aoPair
	 */
	private void miningOpinionModifer(AttributeOpinionPair aoPair)
	{
		// TODO Auto-generated method stub
		SubSentence subSent = getSubSentence(aoPair.opinion.opinionSentIndex,aoPair.opinion.opinionSubSentIndex);
		int numWord = subSent.numWord;
		
		int opinIndex = aoPair.opinion.opinionWordIndex;
		
		int lastIndex = opinIndex-1;
		int modifierNum = 0;
		while(lastIndex>0)
		{
			if(subSent.tagsIctclasSimple[lastIndex]=='d')
			{
				if(modifierNum==0)
				{
					modifierNum++;
					aoPair.opinModifiers = new ArrayList<OpinionModifier>();
				}
				OpinionModifier e = new OpinionModifier();
				e.wordIndex = lastIndex;
				e.sentIndex = aoPair.attr.attributeSentIndex;
				e.subSentIndex = aoPair.attr.attributeSubSentIndex;
				e.opinionModifier = subSent.words[lastIndex];
				aoPair.opinModifiers.add(e);
				lastIndex--;
			}
			else 
			{
				break;
			}
		}
		lastIndex = opinIndex+1;
		while(lastIndex<numWord)
		{
			if(subSent.tagsIctclasSimple[lastIndex]=='d')
			{
				if(modifierNum==0)
				{
					modifierNum++;
					aoPair.opinModifiers = new ArrayList<OpinionModifier>();
				}
				OpinionModifier e = new OpinionModifier();
				e.wordIndex = lastIndex;
				e.sentIndex = aoPair.attr.attributeSentIndex;
				e.subSentIndex = aoPair.attr.attributeSubSentIndex;
				e.opinionModifier = subSent.words[lastIndex];
				aoPair.opinModifiers.add(e);
				lastIndex++;
			}
			else 
			{
				break;
			}
		}
	}



	/**
	 * @param aoPair
	 */
	private void miningAttributeModifer(AttributeOpinionPair aoPair)
	{
		// TODO Auto-generated method stub
		
		SubSentence subSent = getSubSentence(aoPair.attr.attributeSentIndex,aoPair.attr.attributeSubSentIndex);
		
		int attrIndex = aoPair.attr.attributeWordIndex;
		int opinIndex = aoPair.opinion.opinionWordIndex;
		if(attrIndex>opinIndex)
			return;
		int lastIndex = attrIndex-1;
		int modifierNum = 0;
		while(lastIndex>0)
		{
			if(subSent.words[lastIndex].equals("的"))
			{
				lastIndex--;
			}
			else if(subSent.tagsIctclasSimple[lastIndex]=='n')
			{
				if(modifierNum==0)
				{
					modifierNum++;
					aoPair.attrModifiers = new ArrayList<AttributeModifier>();
				}
				AttributeModifier e = new AttributeModifier();
				e.wordIndex = lastIndex;
				e.sentIndex = aoPair.attr.attributeSentIndex;
				e.subSentIndex = aoPair.attr.attributeSubSentIndex;
				e.attributeModifier = subSent.words[lastIndex];
				aoPair.attrModifiers.add(e);
				lastIndex--;
			}
			else
			{
				break;
			}
			
		}
	}



	/**
	 * @param attributeSentIndex
	 * @param attributeSubSentIndex
	 * @return
	 */
	private SubSentence getSubSentence(int sentIndex,
			int subSentIndex)
	{
		// TODO Auto-generated method stub
		return review.sentences.get(sentIndex).subSents.get(subSentIndex);
	}



	/**
	 * @param attribute
	 * @param opinion
	 * @return
	 */
	private double evaluate(Attribute attribute, Opinion opinion)
	{
		// TODO Auto-generated method stub
		Integer opin_id = vocubulary.getTermId(opinion.opinion+"/"+opinion.pos);
		if(opin_id==-1)
		{
			return -1;
		}
		int opin_count = vocubulary.getWordById(opin_id).df;
		Integer attr_id = vocubulary.getTermId(attribute.attribute+"/"+attribute.pos);
		if(attr_id==-1)
			return -1;
		int attr_count = vocubulary.getWordById(attr_id).df;
		
		
		CoPair cp = null;
		double score = -1;
		boolean isHeadNoun = false;
		if(opin_id<attr_id)
		{
			// 当前配对的特征
			cp = coPairFeatures.getCoPair(opin_id, attr_id);
		}
		else
		{
			cp = coPairFeatures.getCoPair(attr_id, opin_id);
			isHeadNoun = true;
		}
		int currentDistance = Math.abs(attribute.attributeWordIndex-opinion.opinionWordIndex);
		
		if(cp!=null)
		{
			
			double pmi = cp.getPmi();
			double aveDist = cp.getAveDistance();
			double avePunct = cp.getAvePunctNum();
			int coNum = cp.getNum();
			int attribute_length = attribute.attribute.length();
			if(attribute_length>2)
				attribute_length = 2;
			int opinion_length = opinion.opinion.length();
			if(opinion_length>2)
				opinion_length = 2;
			double length_factor = attribute_length*opinion_length/2.0;
			
			// 共现次数越多，分值越高，且属性词和评价词越短，分值越衰减
			score += Math.log10(coNum)*length_factor;
			double postProb = 0;
			double lhr = 0;
			if(isHeadNoun)
			{
				postProb = cp.getT_post_prob();
				lhr = cp.getT_lhr();
			}
			else
			{
				lhr = cp.getH_lhr();
				postProb = cp.getH_post_prob();
			}
			// 后验概率越大，分值越高
			score += postProb;
			// pmi值越高，分值越高
			if(pmi<-10)
				pmi = 0;
			else
				pmi+=10;
			score += pmi;
			// LHR越高，分值越大，长度衰减与coNum相同
			if(lhr>0)
				score+= Math.log10(lhr+1)*length_factor;
			
			// 平均距离越大，平均间隔标点越多，分值扣的越多，以当前距离作为衰减因子
			score -= 2*avePunct;
			score -= 0.5*aveDist;
			score *= 1.0/currentDistance;
			
			// 如果属性出现在属性表（表明是我们感兴趣的），加上相应奖励分
			if(candidateFeatures.contains(attribute.attribute))
				score+=2;
			
			if(isDebug)
			{
				//System.out.printf("%5s,%5d,%5s,%5d,CoCount:%5d,Dis:%2d,PMI:%4.2f,aveD:%4.2f,aveP:%4.2f,post:%4.2f,lhr:%4.2f\n",attribute.attribute,attr_count, opinion.opinion,opin_count,coNum,currentDistance,pmi,aveDist,avePunct,postProb,lhr); 
				//System.out.println("score:"+score);
				System.out.println("enter");
				Scanner scanner = new Scanner(System.in);
				scanner.nextLine();
				
				
			}
			
			
		}
		// 如果没有对应的Pair信息，属性词是候选属性，则看形容词是否为常用词且当前距离是否足够小
		else if(candidateFeatures.contains(attribute.attribute))
		{
			score+=1;
			score+=Math.log10(opin_count);
			score-=currentDistance;
			if(isDebug)
			{
				//System.out.printf("%5s,%5d,%5s,%5d,Dis:%2d\n",attribute.attribute,attr_count, opinion.opinion,opin_count,currentDistance); 
				
				System.out.println("enter");
				Scanner scanner = new Scanner(System.in);
				scanner.nextLine();
			}
		}
		
		return score;
	}



	/**
	 * @return
	 */
	private ArrayList<AttributeOpinionPair> implicitAssociate()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 */
	public boolean initialize()
	{
		// TODO Auto-generated method stub
		
		
		if(review.sentences.size()>0)
		{
			aoPairList.clear();
			currentSentenceIndex = 0;
			currentSubSentenceIndex = 0;
			currentSentence = review.sentences.get(0);
			currentSentenceSubSentenceNum = currentSentence.subSents.size();
			if(currentSentenceSubSentenceNum>0)
				currentSubSentence = currentSentence.subSents.get(0);
			else
				currentSubSentence = null;
			return true;
		}
		return false;
	}

	/**
	 * @return
	 */
	private boolean hasNextSubSentence()
	{
		// TODO Auto-generated method stub
		if((currentSentenceIndex==(review.numSentence-1))&&(currentSubSentenceIndex>=(currentSentenceSubSentenceNum-1)))
		{
			return false;
		}
		return true;
	}

	/**
	 * @return
	 */
	private void nextSubSentence()
	{
		// TODO Auto-generated method stub
		if(currentSubSentenceIndex>=(currentSentenceSubSentenceNum-1))
		{
			++currentSentenceIndex;
			currentSubSentenceIndex = 0;
			currentSentence = review.sentences.get(currentSentenceIndex);
			currentSentenceSubSentenceNum = currentSentence.subSents.size();
		}
		else
		{
			++currentSubSentenceIndex; 
			
		}
		if(currentSubSentenceIndex>currentSentence.subSents.size()-1)
			currentSubSentence = null;
		else
			currentSubSentence = currentSentence.subSents.get(currentSubSentenceIndex);
	}
	public static void main(String[] args)
	{
		
		MongoDB mongodb = new MongoDB();
		MongoUtility rs = new MongoUtility(mongodb);
		//String sampleIds = "30720299 30886589 26401201 25149482 23976368 35264141 24073546 22616930 30992340 27328137 32312236 24925260 26515831 31856702 33952298 26872455 30485055 30864650 35094062 28374026 28325545 22807226 25036233 34402670 25911140 30378463 25840668 32566956 27271845 23158116 29713164 23747237 26277485 25815580 35092790 27809666 26090114 28636466 29592671 31016405 33413028 28735254 32817256 34759158 30369475 27507437 23889765 27578146 33840348 34444372 23262834 28795779 24047810 35060436 32965998 25283709 32845947 25425817 24964629 23243570 29824330 29995190 32386621 28997136 29183163 24172069 22874812 30462932 23359562 27828469 25153977 29667683 31805246 23385864 32105313 24168842 32693322 23338783 31479497 26288426 29006866 34888854 29798939 29678459 25564522 29882900 26407347 34599613 23070668 22997289 34990471 23653207 27359780 33504837 27953226 29612844 34146999 33302586 29806084 31748790";
		AttributeOpinionAssociator aoa = new OldAttributeOpinionAssociator();
		
		while(true)
		{
			Scanner scanner = new Scanner(System.in);
			String sampleIds = scanner.nextLine();
			if(sampleIds.equals("exit"))
				break;
			rs.loadSampleIds(sampleIds);
			//rs.getReviewIdsByShopId(535234);
			rs.getSampledReviewBySampleId();
			HashMap<Integer, AnalyzedReview> sampleReviews = rs.getSampleReviews();
			
	//		try
	//		{
	//			System.setOut(new PrintStream(new File("D:/fopair.txt")));
	//		} catch (FileNotFoundException e)
	//		{
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
			long start = System.currentTimeMillis();
			for(Integer rId:sampleReviews.keySet())
			{
				if(rId==Integer.parseInt(sampleIds))
				{
					AnalyzedReview ar = sampleReviews.get(rId);
					List<AttributeOpinionPair> aopairs = aoa.associate(ar);
					for(AttributeOpinionPair aop:aopairs)
					{
						System.out.println(aop.toString());
					}
				}
			}
			long end = System.currentTimeMillis();
			double seconds = ((end-start)/1000.0);
			System.out.println("seconds:"+seconds);
		}
	}
}

