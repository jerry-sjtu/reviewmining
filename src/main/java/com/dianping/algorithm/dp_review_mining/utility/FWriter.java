/*
 * Create Author  : dan.shen
 * Create Date     : 2011-9-22
 * Project            : dp-application-task
 * File Name        : FileWriter.java
 *
 * Copyright (c) 2010-2015 by Shanghai HanTao Information Co., Ltd.
 * All rights reserved.
 *
 */
package com.dianping.algorithm.dp_review_mining.utility;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

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
public class FWriter {
	public OutputStreamWriter osw;
	
	public FWriter(String filename){
		try{
			osw = new OutputStreamWriter( new BufferedOutputStream(new  FileOutputStream(filename)), "utf-8");
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * @param string
	 * @param b
	 */
	public FWriter(String filename, boolean b)
	{
		// TODO Auto-generated constructor stub
		try{
			osw = new OutputStreamWriter( new BufferedOutputStream(new  FileOutputStream(filename,b)), "utf-8");
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	

	public void println(String str){
		try{
//			osw.write(str + "\r\n");
			osw.write(str + "\n");
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void print(String str){
		try{
			osw.write(str);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void close(){
		try{
			osw.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void flush(){
		try{
			osw.flush();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}
