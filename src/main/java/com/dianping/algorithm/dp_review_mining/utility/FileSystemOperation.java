/**
 * Project: FeatureLib
 * 
 * File Created at 2012-8-23
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
package com.dianping.algorithm.dp_review_mining.utility;

import java.io.File;
import java.io.IOException;

/**
 * TODO Comment of FileSystemOperation
 * @author rui.xie
 *
 */
public class FileSystemOperation
{
	public static String[] listFilenames(String dir)
	{
		File directory = new File(dir);
	    File[] files = directory.listFiles();
	    String[] filenames = new String[files.length];
	    for(int i=0;i<files.length;++i)
	    {
	    	filenames[i] = files[i].getName();
	    }
	    return filenames;
	}
	
	public static void main(String args[])
	{
		String ori = "E:/workspace/rankingdata/reviewContentClean0829_MYSQL/id_reviewBody_960014";
		String des = "E:/workspace/rankingdata/tagged_success/";
		move(ori,des);
	}

	/**
	 * @param original
	 * @param string
	 */
	public static void move(String srcFile, String destPath)
	{
		// TODO Auto-generated method stub
		//System.out.println(srcFile);
		//System.out.println(destPath);
		File file = new File(srcFile); 
		  
		  // Destination directory 
		  File dir = new File(destPath); 
		  
		  // Move file to new directory 
		  boolean success = file.renameTo(new File(dir, file.getName())); 
		  
		  
	}
}
