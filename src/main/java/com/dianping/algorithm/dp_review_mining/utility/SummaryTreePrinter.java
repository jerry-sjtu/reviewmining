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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dianping.algorithm.dp_review_mining.feature.Feature;

public class SummaryTreePrinter extends TextTreeViewPrinter {
	
	public void printTreeXML(Feature feature,PrintStream ps)
	{
		if (null == feature) {  
            return;  
        }  
      	if (feature.pairList == null) 
      	{
				print("<"+feature.label+">" + "\n", ps);  
			
      	} 
      	else 
      	{
				print("<"+feature.label+">" + "\n" + feature.pairList, ps);  
		}
		 
  
        if (hasChild(feature)) {  
        	List<Feature> validChildren = validChildren(feature); 
            for (int i = 0; i < validChildren.size(); i++) {  
                printTreeXML(validChildren.get(i),  ps);  
            }  
        }
        print("</"+feature.label+">" + "\n", ps);
	}
	
    protected  void printTree(String prefix, Feature feature, int index, PrintStream ps, boolean detailed) {  
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
        if (index < 0 || index == validChildren((Feature)feature.parTree).size() - 1) {  
            tmpStr += PREFIX[0]; // └  
        } else {  
            tmpStr += PREFIX[1]; // ├  
        }  
        // get the third prefix char  
        tmpStr += PREFIX[3]; // ─  
        //print feature
        if (detailed) {
			if (feature.pairList == null) {
				print(parentPrefixBody + thisPrefix + tmpStr + feature.label, ps);  
			} else {
				print(parentPrefixBody + thisPrefix + tmpStr + feature.label + ": " + feature.pairList, ps);  
			}
		} else {
			print(parentPrefixBody + thisPrefix + tmpStr + feature.label + ": +" + feature.positivePairs + "/-" + feature.negativePairs + " " + feature.opinionMap, ps);  
		}
  
        if (hasChild(feature)) {  
        	List<Feature> validChildren = validChildren(feature); 
            for (int i = 0; i < validChildren.size(); i++) {  
                printTree(parentPrefixBody + thisPrefix + tmpStr,  
                        validChildren.get(i), i, ps, detailed);  
            }  
        }  
    }  

    protected  boolean hasChild(Feature feature) {  
    	if (feature.children == null || !feature.containPairList) {
			return false;
		} 
    	return true;
    }
    
    @SuppressWarnings("unchecked")
	protected List<Feature> validChildren(Feature feature) {
    	List<Feature> featureList = new ArrayList<Feature>();
    	for (Object child : feature.children) {
			Feature featureChild = (Feature)child;
			if (featureChild.containPairList) {
				featureList.add(featureChild);
			}
		}
    	Collections.sort(featureList);
    	return featureList;
	}
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
