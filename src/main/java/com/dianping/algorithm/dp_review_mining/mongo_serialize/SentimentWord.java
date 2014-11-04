/**
 * Project: review-mining-single
 * 
 * File Created at 2012-10-15
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
package com.dianping.algorithm.dp_review_mining.mongo_serialize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.dianping.algorithm.dp_review_mining.utility.FReader;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * TODO Comment of SentimentWord
 * @author rui.xie
 *
 */
public class SentimentWord
{
	private static Logger LOGGER = Logger.getLogger(SentimentWord.class
			.getName());
	private MongoDB mongo;
	private HashMap<String,Integer> wordId;
	private HashMap<Integer,String> idWord;
	
	public static String seedpos[] = {"不错/a","好吃/a","新鲜/a","香/a","便宜/a","实惠/a","鲜/a","干净/a","新/a","热情/a","舒服/a","脆/a","爽/a","精致/a","旺/a","地道/a","鲜美/a","棒/a","方便/a","开心/a","卫生/a","安静/a","周到/a","漂亮/a","爽口/a","可爱/a","合适/a","健康/a","可口/a","主动/a","温馨/a","热闹/a","清楚/a","优雅/a","惊喜/a","有意思/a","合理/a","有名/a","惬意/a","好看/a","亲切/a","诱人/a","舒适/a","细腻/a","高兴/a","灵/a","纯正/a","香甜/a","完美/a","幸福/a","美/a","整洁/a","佳/a","清新/a","愉快/a","恰到好处/a","礼貌/a","明亮/a","豪华/a","用心/a","齐全/a"};
	public static String seedneg[] = {"差/a","失望/a","烂/a","油腻/a","难吃/a","不行/a","脏/a","郁闷/a","腥/a","嘈杂/a","遗憾/a","恶心/a","旧/a","拥挤/a","麻烦/a","可怜/a","乱/a","严重/a","假/a","冷清/a","傻/a","恶劣/a","勉强/a","懒/a","糟糕/a","糟/a","差劲/a","混乱/a","不对/a","一塌糊涂/a","乱七八糟/a","吓人/a","简陋/a","费劲/a","无味/a","无聊/a","可惜/a","恐怖/a","尴尬/a","难受/a","粗糙/a","气愤/a","破/a","困难/a","陈旧/a","生气/a","混沌/a","马马虎虎/a","凶/a","寡/a","惊讶/a","可怕/a","冷淡/a","骚/a","着急/a","悲/a","伤心/a","恶/a","辣手/a","笨/a","单调/a","扫兴/a","破旧/a","烦/a","穷/a","小气/a","生硬/a","没意思/a","次/a","懒散/a","可恶/a","别扭/a","残/a","俗/a","神秘/a","刁/a","不伦不类/a","黑暗/a","平淡/a","慵懒/a","不满/a","丑/a","闷/a","冷漠/a","马虎/a","嚣张/a","霸道/a","杂乱/a","臊/a","苦涩/a","不便/a","危险/a","迟钝/a","昂贵/a","乌烟瘴气/a","吝啬/a","贪心/a","愤怒/a","惊奇/a","烦恼/a","费力/a","吃力/a","丢脸/a","不幸/a","傲慢/a","惭愧/a","恼火/a","难看/a","麻木/a","凌乱/a","糊涂/a","土/a","不良/a","杂七杂八/a","没劲/a","烦躁/a","悲哀/a","懦/a","苛刻/a","残忍/a","蛮横/a","狼狈/a","错误/a","没出息/a","单薄/a","模糊/a","不可收拾/a","糙/a","伪/a","说不过去/a","邪/a","败兴/a","阴暗/a","绝望/a","散漫/a","奢/a","可恨/a","窝火/a","惨不忍睹/a","古怪/a","难闻/a","横/a","死板/a","寂寞/a","难过/a","寒酸/a","拘束/a","艰难/a","头痛/a","僵硬/a","过时/a","僻静/a","俗气/a","落后/a","可悲/a","萧条/a","费事/a","刺鼻/a","虚假/a","荒凉/a","可耻/a","脏乱/a","反感/a","无理/a","无知/a","害羞/a","缺德/a","粗心/a","不像话/a","邋遢/a","蹩脚/a","平淡无奇/a","低下/a","不周/a","猥琐/a","无能/a","不快/a","心寒/a","邪恶/a","委屈/a","虐/a","粗鲁/a","拙/a","野蛮/a","浮躁/a","盲目/a","低级/a","乏味/a","为难/a","悲惨/a","不利/a","无情/a","钝/a","腥臭/a","懈怠/a"};
	
	private HashSet<String> pos;
	private HashSet<String> neg;
	private HashSet<String> force;
	
	private List<DBObject> cachedObj;
	private int nextId = 0;
	private HashMap<String,Integer> allwords;
	
	public SentimentWord(MongoDB mongo)
	{
		this.mongo = mongo;
		wordId = new HashMap<String, Integer>();
		idWord = new HashMap<Integer,String>();
		pos = new HashSet<String>();
		neg = new HashSet<String>();
		force = new HashSet<String>();
		allwords = new HashMap<String, Integer>();
		cachedObj = new ArrayList<DBObject>();
	}
	
	public void loadWordIdFromMongo()
	{
		mongo.useCollection("dpFoodWord");
		DBCursor cursor = mongo.find(null, new String[]{"wId","wpos"});
		try 
		{
            while(cursor.hasNext()) 
            {
                DBObject entry = cursor.next();
                wordId.put((String)entry.get("wpos"), (Integer)entry.get("wId"));
                idWord.put((Integer)entry.get("wId"), (String)entry.get("wpos"));
            }
        } 
		finally 
		{
            cursor.close();
        }
	}
	
	public void loadFromCSV(String filepath)
	{
		FReader fr = new FReader(filepath);
		String line = null;
		int i=0;
		while ((line = fr.readLine()) != null)
		{
			//to-do with the line;
			++i;
			if(i==1)
				continue;
			String tokens[] = line.split(",");
//			if(tokens.length<5)
//				System.out.println("<5:"+line);
			if(tokens[2].equals("贬义"))
			{
				this.neg.add(tokens[0]);
				if(tokens.length>=5&&tokens[4].length()>0)
				{
					String synonyms[] = tokens[4].split("/");
					for(String word:synonyms)
					{
						this.neg.add(word);
					}
				}
			}
			else if(tokens[2].equals("褒义"))
			{
				this.pos.add(tokens[0]);
				if(tokens.length>=5&&tokens[4].length()>0)
				{
					String synonyms[] = tokens[4].split("/");
					for(String word:synonyms)
					{
						this.pos.add(word);
					}
				}
			}
			else
			{
				System.out.println(line);
			}
			
		}
		System.out.println(i+" lines");
		fr.close();
	}
	
	public void loadDegreeFile(String filename)
	{
		FReader fr = new FReader(filename);
		String line = null;
		while ((line = fr.readLine()) != null)
		{
			//to-do with the line;
			this.force.add(line);
		}
		fr.close();
	}
	
	public void loadSentiWordFromFile(String csvfile,String degreefile)
	{
		loadDegreeFile(degreefile);
		loadFromCSV(csvfile);
		System.out.println("pos size:"+pos.size());
		System.out.println("neg size:"+neg.size());
		System.out.println("force size:"+force.size());
	}
	
	public void generateSentimentLexcion(String csvfile,String degreefile)
	{
		loadWordIdFromMongo();
		loadSentiWordFromFile(csvfile,degreefile);
		int forcein = 0;
		int nomatch = 0;
		int onematch = 0;
		int multimatch = 0;
		for(String word:force)
		{
			int wid;
			String key = word+"/d";
			String pos = "d";
			int human = 1;
			int idindic = -1;
			int polarity = 2;
			if(wordId.containsKey(key))
			{
				idindic = wordId.get(key);
				++forcein;
			}
			if(allwords.containsKey(key))
			{
				wid = allwords.get(key);
			}
			else
			{
				allwords.put(key, nextId);
				wid = nextId++;
				
			}
			
			DBObject obj = new BasicDBObject();
			obj.put("wid", wid);
			obj.put("wpos", key);
			obj.put("word", word);
			obj.put("pos", pos);
			obj.put("human", human);
			obj.put("polarity", polarity);
			obj.put("idindic", idindic);
			cachedObj.add(obj);
			
		}
		System.out.println("forcein "+forcein);
		System.out.println(force.size());
		for(String word:this.pos)
		{
			int totalDiffPosMatch = 0;
			String poses[] = {"n","v","a"};
			for(String pos:poses)
			{
				int wid;
				String key = word+"/"+pos;
				
				int human = 1;
				int idindic = -1;
				int polarity = 0;
				if(wordId.containsKey(key))
				{
					idindic = wordId.get(key);
					totalDiffPosMatch++;
				}
				if(allwords.containsKey(key))
				{
					wid = allwords.get(key);
				}
				else
				{
					allwords.put(key, nextId);
					wid = nextId++;
					
				}
				
				if(idindic!=-1)
				{
					DBObject obj = new BasicDBObject();
					obj.put("wid", wid);
					obj.put("wpos", key);
					obj.put("word", word);
					obj.put("pos", pos);
					obj.put("human", human);
					obj.put("polarity", polarity);
					obj.put("idindic", idindic);
					cachedObj.add(obj);
				}
				
			}
			if(totalDiffPosMatch==0)
			{
				++nomatch;
				System.out.println("no"+word);
			}
			else if(totalDiffPosMatch==1)
			{
				++onematch;
				System.out.println("one"+word);
			}
			else
			{
				++multimatch;
				System.out.println("multi"+word);
			}
		}
		System.out.println("nomatch"+nomatch);
		System.out.println("onematch"+onematch);
		System.out.println("multimatch"+multimatch);
		System.out.println(pos.size());
		
		
		for(String word:this.neg)
		{
			int totalDiffPosMatch = 0;
			String poses[] = {"n","v","a"};
			for(String pos:poses)
			{
				int wid;
				String key = word+"/"+pos;
				
				int human = 1;
				int idindic = -1;
				int polarity = 1;
				if(wordId.containsKey(key))
				{
					idindic = wordId.get(key);
					totalDiffPosMatch++;
				}
				if(allwords.containsKey(key))
				{
					wid = allwords.get(key);
				}
				else
				{
					allwords.put(key, nextId);
					wid = nextId++;
					
				}
				
				if(idindic!=-1)
				{
					DBObject obj = new BasicDBObject();
					obj.put("wid", wid);
					obj.put("wpos", key);
					obj.put("word", word);
					obj.put("pos", pos);
					obj.put("human", human);
					obj.put("polarity", polarity);
					obj.put("idindic", idindic);
					cachedObj.add(obj);
				}
				
			}
			if(totalDiffPosMatch==0)
			{
				++nomatch;
				System.out.println("no"+word);
			}
			else if(totalDiffPosMatch==1)
			{
				++onematch;
				System.out.println("one"+word);
			}
			else
			{
				++multimatch;
				System.out.println("multi"+word);
			}
		}
		System.out.println("nomatch"+nomatch);
		System.out.println("onematch"+onematch);
		System.out.println("multimatch"+multimatch);
		System.out.println(neg.size());
		
		for(String key:SentimentWord.seedpos)
		{
			
				int wid;
				String word = key.substring(0,key.indexOf('/'));
				
				int human = 1;
				int idindic = -1;
				int polarity = 0;
				if(wordId.containsKey(key))
				{
					idindic = wordId.get(key);
					
				}
				if(allwords.containsKey(key))
				{
					wid = allwords.get(key);
				}
				else
				{
					allwords.put(key, nextId);
					wid = nextId++;
					
				}
				if(idindic!=-1)
				{
					DBObject obj = new BasicDBObject();
					obj.put("wid", wid);
					obj.put("wpos", key);
					obj.put("word", word);
					obj.put("pos", "a");
					obj.put("human", human);
					obj.put("polarity", polarity);
					obj.put("idindic", idindic);
					cachedObj.add(obj);
				}
			
			
		}
		System.out.println("seedpos size "+seedpos.length);
		for(String key:SentimentWord.seedneg)
		{
			
				int wid;
				String word = key.substring(0,key.indexOf('/'));
				
				int human = 1;
				int idindic = -1;
				int polarity = 1;
				if(wordId.containsKey(key))
				{
					idindic = wordId.get(key);
					
				}
				if(allwords.containsKey(key))
				{
					wid = allwords.get(key);
				}
				else
				{
					allwords.put(key, nextId);
					wid = nextId++;
					
				}
				if(idindic!=-1)
				{
					DBObject obj = new BasicDBObject();
					obj.put("wid", wid);
					obj.put("wpos", key);
					obj.put("word", word);
					obj.put("pos", "a");
					obj.put("human", human);
					obj.put("polarity", polarity);
					obj.put("idindic", idindic);
					cachedObj.add(obj);
				}
		}
		System.out.println("seedneg size "+seedneg.length);
		System.out.println(cachedObj.size());
		mongo.useCollection("dpFoodSentimentLexicon");
		mongo.batchSaveDBObject(cachedObj);
		mongo.createIndex("idindic");
		mongo.createIndex("wpos");
	}
	
	public static void main(String args[])
	{
		MongoDB mongo = new MongoDB();
		SentimentWord sw = new SentimentWord(mongo);
		String csvfile ="E:\\workspace\\rankingdata\\sentilexicon\\褒贬词典.csv";
		String degreefile = "E:\\workspace\\rankingdata\\sentilexicon\\Hownet\\chengdu.txt";
		sw.generateSentimentLexcion(csvfile, degreefile);
		
	}
	
}
