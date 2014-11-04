/**   
* @Description: TODO
* @author rui.xie 
* @project review-mining-single
* @date 2012-12-17 
* @version V1.0
* 
* Copyright (c) 2010-2015 by Shanghai HanTao Information Co., Ltd.
 * All rights reserved.
*/
package com.dianping.review.dao;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.UnknownHostException;

import java.util.List;

import java.util.Properties;
import org.apache.log4j.Logger;
import org.springframework.web.context.ContextLoader;


import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoDB {

	//  mongodb server ip:port
	private static Mongo mongo = null; 
	//mongodb server database name
	private DB db = null; 
	//mongodb server collection name
	private DBCollection collection = null;
	static private String mongoAddresStr;
	static private String mongoDBStr;
	static private String mongoCollectionStr;
	static private Properties properties = null;
	private static Logger logger = Logger.getLogger(MongoDB.class.getName());
	
	
	
	public MongoDB()
	{
		try
		{
			if(mongo==null)
				mongo = new Mongo(mongoAddresStr);
			db = mongo.getDB(mongoDBStr);
			collection = db.getCollection(mongoCollectionStr);
			
		} catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
//	static 
//	{
//		//load configuration
//		System.out.println("loaded");
//		properties = new Properties();
//		try 
//		{
//
//			String path = ContextLoader.getCurrentWebApplicationContext().getServletContext().getRealPath("/")+"WEB-INF/resources/mongo_conf.properties";
//			System.out.println( path );
//			InputStream in = new FileInputStream(path); 
//			properties.load(in);
//			mongoAddresStr = properties.getProperty("mongoAddresStr") ;
//			assert mongoAddresStr != null && !mongoAddresStr.trim().isEmpty() : "mongoAddresStr is null";
//			
//			mongoDBStr = properties.getProperty("mongoDBStr");
//			assert mongoDBStr != null && !mongoDBStr.trim().isEmpty() : "mongoDBStr is null";
//			
//			mongoCollectionStr = properties.getProperty("mongoCollectionStr");
//			assert mongoCollectionStr != null && !mongoCollectionStr.trim().isEmpty() : "mongoCollectionStr is null";			
//			in.close();
//			
//			mongo = new Mongo(mongoAddresStr);
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//			logger.fatal("could not find mongo property file");
//		}
//	}
	
	public void useCollection(String name)
	{
		collection = db.getCollection(name);
		
	}
	
	public void saveDBObject(DBObject obj)
	{
		collection.save(obj);
	}
	
	public void batchSaveDBObject(List<DBObject> objlist)
	{
		collection.insert(objlist);
	}
	
	public DBObject findOne(DBObject query)
	{
		if(query==null)
			return collection.findOne();
		else
			return collection.findOne(query);	
	}
	
	public DBCursor find(DBObject query)
	{
		if(query==null)
			return collection.find();
		else
			return collection.find(query);
	}
	
	public DBCursor find(DBObject query, String keys[])
	{
		DBObject keyObj = new BasicDBObject();
		for(String key:keys)
			keyObj.put(key, 1);
		return collection.find(query, keyObj);
	}
	
	public void update(DBObject q,DBObject o,boolean upsert,boolean multi)
	{
		collection.update(q, o, upsert, multi);
	}
	
	
	
	public static void main(String args[])
	{
		String line = "23682957,535299,和/cc 朋友/n 来/vf 过/uguo 很/d 多/m 次/qv 了/y ，/wd 停车/vi 还/d 算/v 方便/an ，/wd 中午/t 来时/t 居多/vi ，/wd 基本/a 每次/r 都/d 有/vyou 位置/n ，/wd 大部分/m 点/qt 的/ude1 都/d 是/vshi 商务套餐/nz ，/wd 也/d 有/vyou 果盘/n +/q 咖啡/n ，/wd 感觉/v 环境/n 的确/d 不/d 错/v ，/wd 单间/n 也/d 很/d 大/a ，/wd 谈话/vi 很/d 适合/v ，/wd 菜/n 品味/v 道/qv 谈不上/v 出众/a ，/wd 但/c 都/d 过得去/vi ，/wd 大家/rr 来/vf 这/rzv 毕竟/d 不/d 都/d 是/vshi 为/p 填/v 饱/a 肚子/n ，/wd 一/m 款/q 雪/n 鱼/n 的/ude1 套餐/n 不错/a 的/ude1 ，/wd 推荐/v  。/wj 咖啡味/nz 道/qv 还/d 凑合/v  。/wj ";
		String tokens[]= line.split(",");
		int pos = 0;
		StringBuilder sb = new StringBuilder(); 
		int length = tokens[2].length();
		while(pos<length)
		{
			char c = tokens[2].charAt(pos);
			if(c =='/')
			{
				sb.append('/');
				pos++;
				sb.append(tokens[2].charAt(pos));
				pos++;
				while(pos<length&&tokens[2].charAt(pos)!=' ')
				{
					pos++;
				}
				if(pos!=length)
				{
					sb.append(' ');
					pos++;
				}
				
			}
			else
			{
				sb.append(c);
				pos++;
			}
		}
		System.out.println(sb.toString());
	}

	/**
	 * @param basicDBObject
	 */
	public void createIndex(DBObject obj)
	{
		// TODO Auto-generated method stub
		this.collection.createIndex(obj);
		
	}

	/**
	 * @param copairCollection
	 * @param collSize 
	 * @param b
	 * @return
	 */
	public DBCollection[] createCollection(String copairCollection, int collSize, boolean b)
	{
		// TODO Auto-generated method stub
		DBCollection colls[] = new DBCollection[collSize];
		String invert = "";
		if(b)
			invert = "Invert";
		for(int i=0;i<collSize;++i)
		{
			
			colls[i] = db.getCollection(copairCollection+invert+i);
			
		}
		return colls;
	}

	/**
	 * @param string
	 */
	public void createIndex(String string)
	{
		// TODO Auto-generated method stub
		this.collection.createIndex(new BasicDBObject(string, 1));
	}

	/**
	 * @return
	 */
	public int count()
	{
		// TODO Auto-generated method stub
		return (int) collection.count();
	}
}
