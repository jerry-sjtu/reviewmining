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
package com.dianping.algorithm.dp_review_mining.utility;

import org.springframework.web.context.ContextLoader;

public class Const {
	public static final int SUCCESS = 0;
	public static final int FAILURE = 1;
	
	//BI configuration
	public static final String BI_CONFIG_FILE = "bi_conf.properties";
	public static final String BI_DBDRIVER_PROPERTY = "db_driver";	
	public static final String BI_URL_PROPERTY = "bi_url";	
	public static final String BI_LOGIN_PROPERTY = "bi_login";
	public static final String BI_PASSWORD_PROPERTY = "bi_password";
	
	//mongodb configuration
//	public static final String MONGO_CONFIG_FILE = "C:\\Users\\rui.xie\\workspace\\dp-review-mining\\review-mining-single\\src\\main\\resources\\mongo_conf.properties";
	//public static final String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
	//public static final String path = ContextLoader.getCurrentWebApplicationContext().getServletContext().getRealPath("/")+"WEB-INF/resources/";
	public static final String path = "E:\\workspace\\ReviewSummarizer\\target\\ReviewSummarizer-0.1\\WEB-INF\\resources\\";
	public static final String MONGO_CONFIG_FILE = path + "mongo_conf.properties";
	public static final String MONGO_ADDRESSSTR_PROPERTY = "mongoAddresStr";
	public static final String MONGO_DBSTR_PROPERTY = "mongoDBStr";
	public static final String MONGO_COLLECTIONSTR_PROPERTY = "mongoCollectionStr";
	
	//data file configuration
	public static final String FOOD_CONFIG_FILE = path + "food_conf.properties";
	public static final String FEATURE_TREE_FILE_PROPERTY = "featureTreeFile";
	public static final String SYNONYM_FILE_PROPERTY = "synonymFile";
	public static final String MYSQL_CONFIG_FILE = path + "mysql_conf.properties";
	public static final String SENTIMENT_FILE_PROPERTY = "sentimentFile";
	public static final String SENTIMENT_FILE_FILE_PROPERTY = "sentimentNegFile";
	
	public static void main(String[] args) {
		String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		System.out.println(path);
	}
}
