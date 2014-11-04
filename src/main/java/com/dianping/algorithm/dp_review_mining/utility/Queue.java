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

import java.util.Vector;

@SuppressWarnings("serial")
public class Queue extends Vector {
	
	@SuppressWarnings("serial")
	public static class EmptyQueueException extends RuntimeException {
		public EmptyQueueException() {
			super();
		}
	}
	
	public Queue() {
		super();
	}
	
	/**
	 * 在队首插入一个元素
	 * @param x
	 */
	@SuppressWarnings("unchecked")
	public synchronized void enq(Object x) {
		super.addElement(x);
	}
	
	/**
	 * 删除队首元素
	 * @return
	 */
	public synchronized Object deq() {
		/* 队列若为空，引发EmptyQueueException异常 */
		if(this.empty())
			throw new EmptyQueueException();
		Object x = super.elementAt(0);
		super.removeElementAt(0);
		return x;
	}
	
	/**
	 * 取队首的一个元素
	 * @return
	 */
	public synchronized Object front() {
		if(this.empty())
			throw new EmptyQueueException();
		return super.elementAt(0);
	}
	
	/**
	 * 判断队列是否为空
	 * @return
	 */
	public boolean empty() {
		return super.isEmpty();
	}
	
	/**
	 * 清空队列
	 */
	public synchronized void clear() {
		super.removeAllElements();
	}
	
	/**
	 * 寻找元素，返回其相对于队首的位置
	 * -1 表示为找到
	 * @param x
	 * @return
	 */
	public int search(Object x) {
		return super.indexOf(x);
	}
}

	

