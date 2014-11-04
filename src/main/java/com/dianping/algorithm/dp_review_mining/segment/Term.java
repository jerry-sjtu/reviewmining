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
package com.dianping.algorithm.dp_review_mining.segment;

public class Term {
	private String word;   //词语
	private String pos;    //词性
	private int position;  //该词语在句子中的位置
	
	public Term(String word) {
		this.word = word;
		this.pos = null;
		this.position = -1;
	}
	
	public Term(String word, String pos) {
		this.word = word;
		this.pos = pos;
		this.position = -1;
	}
	
	public Term(String word, String pos, int position) {
		this.word = word;
		this.pos = pos;
		this.position = position;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
}