package com.dianping.algorithm.dp_review_mining.framework;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.algorithm.dp_review_mining.associate_helper.AttributeOpinionPair;
import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.mongo_serialize.CoPairToMongo;
import com.dianping.algorithm.dp_review_mining.mongo_serialize.ReviewToMongo;
import com.dianping.algorithm.dp_review_mining.mongo_serialize.WordToMongo;
import com.dianping.algorithm.dp_review_mining.nlp.utility.FileTagger;
import com.dianping.algorithm.dp_review_mining.statistics.CoPairStaticstics;
import com.dianping.algorithm.dp_review_mining.summary.ReviewSummaryTree;
import com.dianping.algorithm.dp_review_mining.summary.Summarizer;
import com.dianping.algorithm.dp_review_mining.utility.FReader;
import com.dianping.algorithm.dp_review_mining.utility.FWriter;
import com.dianping.algorithm.dp_review_mining.utility.FileSystemOperation;

public class MiningProcessor {
	
	
	public MiningProcessor()
	{
		
	}
	public static void main(String[] args) {
		MiningProcessor mp = new MiningProcessor();
		mp.miningReviewInc();
	}
	
	public void miningReviewFull()
	{
		String originDir = "E:/workspace/rankingdata/reviewContent1127_MYSQL/";
		String taggedDir = "E:/workspace/rankingdata/reviewContentTagged0903_MYSQL/";
		String middleDir = "E:/workspace/rankingdata/reviewShuffle/";
			
		segment(originDir, taggedDir);
		processWord(taggedDir);
		processPair(taggedDir,middleDir, "\t");
		saveReview(taggedDir, "\t");
		Map<Integer, Integer> map = getShopReviewNoMap(taggedDir, "\t");
		summarize(taggedDir, map, 10);
		
	}
	
	public void miningReviewInc()
	{
		String originDir = "E:/workspace/rankingdata/reviewContent20141103_HIVE/";
		String taggedDir = "E:/workspace/rankingdata/reviewContentTagged20141103_HIVE/";
		
		String dir = "E:\\workspace\\rankingdata\\reviewmeta\\";
		String featureName = "target-feature.txt";
		String synonymName = "food-feature-synonym-file.txt";
		String resultName = "target-feature-pair-tmp.txt";
		
		//segment(originDir, taggedDir);
		//saveReview(taggedDir,"\t");
		Map<Integer, Integer> map = getShopReviewNoMap(taggedDir, "\t");
		//summarize(taggedDir, map, 10);
		summarizeFilter(dir+featureName, dir+synonymName, map, 10, dir+resultName);
		
	}
	
	public void summarizeFilter(String featureFile, String featureSynonym, Map<Integer,Integer> map, int reviewNo, String outputFile )
	{
		System.out.println(map.size());
		Summarizer sumarizer = new Summarizer(featureFile, featureSynonym);
		FWriter fw =  new FWriter(outputFile);
		for(Integer shopId:map.keySet())
		{
			
			//System.out.println(shopId);
			int reviewNum = map.get(shopId);
			if(reviewNum > reviewNo)
			{
				ReviewSummaryTree tree = sumarizer.getSummaryTreeFromMongo(shopId);
				fw.println(shopId+"\t"+reviewNum);
				String lables[] = {"消费","环境","服务","食物","口味"};
				
				StringBuilder sb = new StringBuilder();
				int i = 0;
				for(String label:lables)
				{
					fw.println(label+":");
					List<List<AttributeOpinionPair>> list = tree.getReviewPairByLable(label);
					List<AttributeOpinionPair> posList = list.get(0);
					List<AttributeOpinionPair> negList = list.get(1);
					
					Collections.sort(posList);
					//String word = sumarizer.getWord(posList);
					fw.print("+:");
					for(AttributeOpinionPair aop:posList)
					{
						fw.print(aop.toString()+"\t");
					}
					fw.println("");
					//saveSummaryLabelPairs2Mongo(posList, label, word, 1, shopId);
					Collections.sort(negList);
					fw.print("-:");
					for(AttributeOpinionPair aop:negList)
					{
						fw.print(aop.toString()+"\t");
					}
					fw.println("");
					//word = sumarizer.getWord(negList);
					//saveSummaryLabelPairs2Mongo(negList, label, word, -1, shopId);
					++i;
				}
			}
		}
	}
	
	private void segment(String ori, String target) {
		// TODO Auto-generated method stub
		FileTagger ft = new FileTagger();
		//String dir = "E:/workspace/rankingdata/reviewContent1127_MYSQL/";
		//String dir_res = "E:/workspace/rankingdata/reviewContentTagged1127_MYSQL/";
		ft.tagDir(ori,target);
	}

	private void summarize(String taggedDir, Map<Integer, Integer> map, int reviewNo) {
		// TODO Auto-generated method stub
		Summarizer summarizer = new Summarizer("food");
		summarizer.summarizeShopPairsToMongo(reviewNo, map);
		
	}

	private Map<Integer,Integer> getShopReviewNoMap(String taggedDir, String sep) {
		// TODO Auto-generated method stub
		
		String filenames[] = FileSystemOperation.listFilenames(taggedDir);
		Map<Integer, Integer> map = new HashMap<Integer,Integer>();
		for(int i=filenames.length-1; i>=0; --i)
		{
			String filename = taggedDir+filenames[i];
			FReader fr = new FReader(filename);
			String line = null;
			while((line=fr.readLine())!=null)
			{
				String fields[] = line.trim().split(sep);
				if(fields.length!=3)
					continue;
				int shopId = Integer.parseInt(fields[1]);
				if(map.containsKey(shopId))
				{
					Integer cnt = map.get(shopId);
					map.put(shopId, cnt+1);
				}
				else
				{
					map.put(shopId, 1);
				}
			}
			fr.close();
		}
		return map;
	}
	private void saveReview(String targetDir, String sep) {
		// TODO Auto-generated method stub
		
		//String dir = "E:\\workspace\\rankingdata\\reviewContentTagged0903_MYSQL\\";
		MongoDB mongodb = new MongoDB();
		ReviewToMongo ftm = new ReviewToMongo(mongodb,"dpFoodReview");
		ftm.processingDir(targetDir, sep);
		
	}

	private void processPair(String targetDir, String middleDir, String sep) {
		// TODO Auto-generated method stub
		
		MongoDB mongodb = new MongoDB();
		CoPairStaticstics cps = new CoPairStaticstics(mongodb,"dpFoodWord",middleDir+"_",10,16);
		// String dir = "E:\\workspace\\rankingdata\\reviewContentTagged0903_MYSQL\\";
		cps.processingDir(targetDir, sep);
		cps.mergeShuffleDir();
		
		CoPairToMongo cptm = new CoPairToMongo(mongodb,"dpFoodWord","dpFoodCoPair");
		cptm.loadWordIdFromMongo();
		cptm.processingDir(middleDir);
	}

	//  
	private void processWord(String taggedDir) {
		// TODO Auto-generated method stub
		//String dir = "E:\\workspace\\rankingdata\\reviewContentTagged0903_MYSQL\\";
		MongoDB mongodb = new MongoDB();
		WordToMongo ftm = new WordToMongo(mongodb,"dpFoodWord");
		ftm.processingDir(taggedDir, "\t");
	}
	

}
