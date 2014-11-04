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

import java.io.PrintStream;

import com.dianping.algorithm.dp_review_mining.feature.Feature;

public class FeatureTreePrinter extends TextTreeViewPrinter {
	
	
    protected  void printTree(String prefix, Feature feature, int index, PrintStream ps) {  
        if (null == feature) {  
            return;  
        }  
        String parentPrefixBody = prefix.substring(0, prefix.length() - 2);  
        String thisPrefix = "";  
        String tmpStr = "";  
        // get the first prefix char  
        String parentPrefixKey = prefix.substring(prefix.length() - 2);  
        if (parentPrefixKey.startsWith(PREFIX[0])) {  
            thisPrefix = PREFIX[4]; // _  
        } else {  
            thisPrefix = PREFIX[2]; // │  
        }  
        // get the second prefix char  
        if (index < 0 || index == feature.parTree.children.size() - 1) {  
            tmpStr += PREFIX[0]; // └  
        } else {  
            tmpStr += PREFIX[1]; // ├  
        }  
        // get the third prefix char  
        tmpStr += PREFIX[3]; // ─  
        //print feature
		print(parentPrefixBody + thisPrefix + tmpStr + feature.label, ps);  
  
        if (hasChild(feature)) {  
            for (int i = 0; i < feature.children.size(); i++) {  
                printTree(parentPrefixBody + thisPrefix + tmpStr,  
                        (Feature)feature.children.get(i), i, ps);  
            }  
        }  
    }  

   
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
