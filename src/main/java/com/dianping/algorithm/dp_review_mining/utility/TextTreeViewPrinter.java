/**   
* @Description: TODO
* @author weifu.du 
* @project review-mining-single
* @date Nov 26, 2012 
* @version V1.0
* 
* Copyright (c) 2010-2015 by Shanghai HanTao Information Co., Ltd.
 * All rights reserved.
*/
package com.dianping.algorithm.dp_review_mining.utility;

import java.io.BufferedOutputStream;
import java.io.PrintStream;

import com.dianping.algorithm.dp_review_mining.feature.Feature;

public class TextTreeViewPrinter {
	
    protected static final String[] PREFIX = { "└", // last child  
            "├", // middle child  
            "│", // parent brother  
            "─", // bar  
            " " // blank  
    };  
    
    
    public  void printTree(Feature feature, PrintStream ps, boolean detailed) {
    	printTree(PREFIX[0] + PREFIX[4], feature, -1, ps, detailed);
	}
  
  
    public void printTreeXML(Feature feature,PrintStream ps)
	{
		
	}
	
    
    protected  void printTree(String prefix, Feature feature, int index, PrintStream ps, boolean detailed) {  
    	System.out.println("this is a virtual function");
    }  
 
  
    protected  void print(String str, PrintStream ps) {  
        ps.println(str);  
    }  
  
    protected  boolean hasChild(Feature feature) {  
    	return (feature.children != null && !feature.children.isEmpty());
    }  
}
