package com.dianping.algorithm.dp_review_mining.segment;

import java.util.Comparator;

public class LongerStringFirst implements Comparator<Object> {

	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		String str1 = (String) o1;
		String str2 = (String) o2;
		if (str1.startsWith(str2) && str1.length() > str2.length()) {
			return -1;
		}
		if (str2.startsWith(str1) && str2.length() > str1.length()) {
			return 1;
		}
		return str1.compareTo(str2);
	}

}
