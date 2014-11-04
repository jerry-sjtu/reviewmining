/**   
* @Description: TODO
* @author weifu.du 
* @project review-mining-single
* @date 2012-9-28 
* @version V1.0
* 
* Copyright (c) 2010-2015 by Shanghai HanTao Information Co., Ltd.
 * All rights reserved.
*/
package com.dianping.algorithm.dp_review_mining.feature;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.utility.Const;
import com.dianping.algorithm.dp_review_mining.utility.FReader;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;

public class FeatureTree {
	
	public Feature root = null;  
    protected HashMap<String, String> instanceMap = null;
	public Set<String> treeNodeNameSet = new HashSet<String>();
	public List<String> subNodeOfRootList = null;
	private static Properties properties = null;
	private static Logger logger = Logger.getLogger(FeatureTree.class.getName());
	private String featureTreeFileName = null;
	private String featureSynonymFileName = null;
	
	public FeatureTree(String category) 
	{
		logger.info("readConfig");
		readConfigFile(category);
		logger.info("loadSingleOrignTreeFromFile");
		loadSingleOrignTreeFromFile(Const.path+featureTreeFileName);
		logger.info("loadSynonymFile");
		loadSynonymFile(Const.path+featureSynonymFileName);
		logger.info("loadSynonymFile finished");
	}
	
	public FeatureTree(Configuration conf) {
		
	}
	
	public FeatureTree(String featureFile, String synonymFile)
	{
		loadSingleOrignTreeFromFile(featureFile);
		loadSynonymFile(synonymFile);
	}
	
	public void readConfigFile(String category) {
		//load configuration
		properties = new Properties();
		try {
			Field[] fields = Const.class.getFields();
			String path = null; 
			for (Field field : fields) {
				if (field.getName().startsWith(category.trim().toUpperCase())) {
					path = field.get(null).toString();
					break;
				}
			}
			if (path == null) {
				logger.fatal("配置文件路径为空");
				return;
			}
			InputStream in = new FileInputStream(path);
			properties.load(in);
			featureTreeFileName = properties.getProperty(Const.FEATURE_TREE_FILE_PROPERTY);
			featureSynonymFileName = properties.getProperty(Const.SYNONYM_FILE_PROPERTY);
			in.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 从文件中加载初始树，每个特征只有一个父结点
	 */
	@SuppressWarnings("unchecked")
	private void loadSingleOrignTreeFromFile(String fileName) {
		if(fileName == null || fileName .isEmpty()) return;
		FReader reader = new FReader(fileName);
		String line = reader.readLine();
		while(line != null){			
			if(line.length() < 1) {
				line = reader.readLine();
				continue;
			}
			
			String[] termList = line.split("\\s+");
			if(termList == null || termList.length != 2) {
				System.out.println(line);
				System.err.println("特征树文件格式错误！");
				System.err.println(line);
				line = reader.readLine();
				continue;
			}
			
			Feature feature = new Feature(termList[0]);
			if(termList[1].equals("null")){
				root = feature;
				treeNodeNameSet.add(termList[0]);
			} else {
				Feature parFeature = Feature.findNode(root, termList[1], true);
				if(parFeature != null) {
					feature.parTree = parFeature;
					parFeature.getChildren().add(feature);
					treeNodeNameSet.add(termList[0]);
				} else {
					System.out.println(line);
					System.err.println("无法找到" + termList[1] + "的父结点!");
				}
			}
			line = reader.readLine();
		}
		reader.close();	
	}
	
	
	/**
	 * 从文件中加载特征的同义词
	 * @param fileName
	 */
	private void loadSynonymFile(String fileName) {
		if(fileName == null || fileName.isEmpty()) {
			System.err.println("feature synonym filename is empty.");
			return;
		}
		instanceMap = new HashMap<String, String>();
		FReader reader = new FReader(fileName);
		String line = null;
		while((line = reader.readLine()) != null){
			if((line = line.trim()).isEmpty())
				continue;
			
			String[] termList = line.split("\\s");
			if(null == termList || termList.length < 2)
				continue;
			
			Feature feature = Feature.findNode(root, termList[0], true);
			if(feature != null){
				if (feature.synonymSet == null) {
					feature.synonymSet = new HashSet<String>();
				}
				for (int i = 1; i < termList.length; i++) {
					feature.synonymSet.add(termList[i]);
					instanceMap.put(termList[i], termList[0]);
				}
			}
		}
		reader.close();	
	}
	
	public static void main(String[] args) 
	{
		String dir = "E:\\workspace\\rankingdata\\reviewmeta\\";
		String featureName = "target-feature.txt";
		String synonymName = "food-feature-synonym-file.txt";
		FeatureTree tree = new FeatureTree(dir+featureName,dir+synonymName);
		System.out.println("debug");
//		List<Feature> featureList = tree.root.getFeatureOfLayer(4);
//		for (Feature feature : featureList) {
//			System.out.print(feature.label + "\t");
//		}
//		System.out.println();
//		HashSet<String> caiyao = new HashSet<String>();
// 		FReader fr = new FReader("data/feature/food-feature-tree-file.txt");
//		String line = null;
//		while ((line = fr.readLine()) != null)
//		{
//			//to-do with the line;
//			String tokens[] = line.split("\t");
//			if(tokens.length==2)
//			{
//				if(tokens[1].equals("菜肴"))
//				{
//					caiyao.add(tokens[0]);
//				}
//			}
//		}
//		fr.close();
//		fr = new FReader("data/feature/food-feature-tree-file.txt");
//		FWriter fw = new FWriter("data/feature/food-feature-tree-file-NEW.txt");
//		line = null;
//		while ((line = fr.readLine()) != null)
//		{
//			//to-do with the line;
//			String tokens[] = line.split("\t");
//			if(tokens.length==2)
//			{
//				if(tokens[1].equals("食物"))
//				{
//					if(caiyao.contains(tokens[0]))
//					{
//						continue;
//					}
//					
//				}
//			}
//			fw.println(line);
//		}
//		fr.close();
//		fw.close();
	}
}


