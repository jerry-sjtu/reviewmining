package com.dianping.review.business;

import com.mongodb.DBObject;

public class LabelObject 
{
	public String label;
	public String word;
	public int ori;
	public int num;
	public int shopId;
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getOri() {
		return ori;
	}
	public void setOri(int ori) {
		this.ori = ori;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public int getShopId() {
		return shopId;
	}
	public void setShopId(int shopId) {
		this.shopId = shopId;
	}
	
	public LabelObject(String label2, String word2, int ori2, int num2,
			int shopId2) 
	{
		// TODO Auto-generated constructor stub
		this.label = label2;
		this.word = word2;
		this.ori = ori2;
		this.num = num2;
		this.shopId = shopId2;
	}
	
	
}
