/*
 * Create Author  : dan.shen
 * Create Date     : 2011-9-22
 * Project            : dp-application-task
 * File Name        : FileReader.java
 *
 * Copyright (c) 2010-2015 by Shanghai HanTao Information Co., Ltd.
 * All rights reserved.
 *
 */
package com.dianping.review.analysis.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;



/**
 * 功能描述:  <p>
 * 
 *
 * @author : dan.shen <p>
 *
 * @version 2.2 2011-9-22
 *
 * @since dp-application-task 2.2
 */
public class FReader {
	
	private static final Logger LOGGER = Logger.getLogger(FReader.class.getName());
	
	public BufferedReader br;
	
	public FReader(String filename) {
		try{
			br = new BufferedReader( new InputStreamReader(	new FileInputStream(filename),"utf-8"));
		}
		catch(IOException e){
			LOGGER.error("error opening the file for reading, cause:",e);
		}
	}
	
	public FReader(File file) {
		try{
			br = new BufferedReader( new InputStreamReader(	new FileInputStream(file),"utf-8"));
		}
		catch(IOException e){
			LOGGER.error("error opening the file for reading, cause:",e);
		}
	}
	public String readLine(){
		try{
			return br.readLine();
		}
		catch(IOException e){
			LOGGER.error("error reading the file, cause:",e);
			return "";
		}
	}
	
	public void close(){
		try{
			br.close();
		}
		catch(IOException e){}
	}
}
