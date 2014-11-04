/**   
* @Description: TODO
* @author weifu.du 
* @project review-mining-single
* @date Nov 12, 2012 
* @version V1.0
* 
* Copyright (c) 2010-2015 by Shanghai HanTao Information Co., Ltd.
 * All rights reserved.
*/
package com.dianping.algorithm.dp_review_mining.summary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.utility.FReader;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import JGibbsLDA.JGibbsLDA.Estimator;
import JGibbsLDA.JGibbsLDA.Inferencer;
import JGibbsLDA.JGibbsLDA.LDACmdOption;
import JGibbsLDA.JGibbsLDA.Model;

/************************************************************************************************
 *                                    JGibbsLDA的参数说明
 ************************************************************************************************
 * boolean est= false;                     是否开始训练模型
 * boolean estc= false;                    决定是否是基于先前已有的模型基础上继续用新数据训练模型
 * boolean inf= true;                      是否使用先前已经训练好的模型进行推断
 * String dir= "";                         数据结果（模型数据）保存位置
 * String dfile= "";                       训练数据或原始数据文件名
 * String modelName= "";                   选择使用哪一个迭代的模型结果来进行推断
 * int K= 100;                             类簇数目
 * double alpha= 0.2;                      平滑系数
 * double beta= 0.1;                       平滑系数 
 * int niters= 1000;                       迭代数目
 * int savestep= 10;                       指定把迭代结果模型保存到硬盘上的迭代跨度，即每迭代10次保存一次。
 * int twords= 100;                        对每一个类别（话题）选前多少个最大概率词项
 * boolean withrawdata= false;             是否使用原始文件  
 * String wordMapFileName= "wordmap.txt";  词语和ID的映射文件 
 */

public class ClulsterringByLDA {
	private MongoDB mongo = new MongoDB();
	private Set<String> stopwordSet = new HashSet<String>();
	
	
	//辅助数据结构，由于JGibbsLDA不支持中文
	Map<String, Integer> chinese2EnglishMap = new HashMap<String, Integer>();
	Map<Integer, String> english2ChineseMap = new HashMap<Integer, String>();
	
	public void parseTWordFile(String inputFile, String outputFile) {
		FReader reader = new FReader(inputFile);
		FWriter writer = new FWriter(outputFile);
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("Topic")) {
				writer.println(line);
			}else {
				String[] termList = line.split("\\s");
				writer.println(english2ChineseMap.get(Integer.valueOf(termList[1])) + "\t" + termList[2]);
			}
		}
		reader.close();
		writer.close();
	}
	
	public void loadStopword(String fileName) {
		FReader reader = new FReader(fileName);
		String line = null;
		while ((line = reader.readLine()) != null) {
			line = line.replace("\n", "");
			System.out.println(line);
			stopwordSet.add(line);
		}
		reader.close();
	}
	
	public void generateLDAInputFile(String outputFile) {
		mongo.useCollection("dpFoodReview");
		FWriter writer = new FWriter(outputFile);
//		int reviewCount = mongo.find(null).count();
//		System.out.println("reviewCount: " + reviewCount);
		writer.println(String.valueOf(700000));
		DBCursor cursor = mongo.find(null, new String[]{"origin"});
		
		try 
		{ 
			int i = 0;
            while(cursor.hasNext()) 
            {
            	if (i++ > 700000) {
					break;
				}
                DBObject entry = cursor.next();
                String originReivew = entry.get("origin").toString();
                List<Term> terms = ToAnalysis.paser(originReivew);
                for (Term term : terms) {
                	String termString = term.toString();
					if (stopwordSet.contains(termString)) {
						continue;
					}
					writer.print(termString.hashCode()+ " ");
					chinese2EnglishMap.put(termString, termString.hashCode());
					english2ChineseMap.put(termString.hashCode(), termString);
				}
                writer.println("");
                System.out.println(i + "\t" + chinese2EnglishMap.size() + "\t" + english2ChineseMap.size());
            }
        } 
		finally 
		{
            cursor.close();
            writer.close();
        }
	}

	
	public void clusterring() {
		LDACmdOption ldaOption = new LDACmdOption(); 
		ldaOption.est = true;
		ldaOption.estc = false;
		ldaOption.inf = true; 
		ldaOption.dir = "./model"; 
		ldaOption.dfile = "lda-input-file.txt"; 
		ldaOption.modelName = "model-final"; 
		ldaOption.withrawdata = true;
		ldaOption.niters = 100;
		ldaOption.savestep = 100;
		ldaOption.K = 1000;
		ldaOption.twords = 10;
		Estimator estimator = new Estimator();
		estimator.init(ldaOption);
		estimator.estimate();
		parseTWordFile("./model/model-final.twords", "./model/model-final-human.twords");
		Inferencer inferencer = new Inferencer(); 
		inferencer.init(ldaOption);
		Model newModel = inferencer.inference();
		parseTWordFile("./model/lda-input-file.txt.model-final.twords", "./model/lda-input-file.txt.model-final-human.twords");
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ClulsterringByLDA clulsterringByLDA = new ClulsterringByLDA();
		clulsterringByLDA.loadStopword("stopwords.txt");
		clulsterringByLDA.generateLDAInputFile("./model/lda-input-file.txt");
		clulsterringByLDA.clusterring();
	}

}
