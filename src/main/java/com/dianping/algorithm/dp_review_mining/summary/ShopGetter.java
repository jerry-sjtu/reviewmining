package com.dianping.algorithm.dp_review_mining.summary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.dianping.algorithm.dp_review_mining.associate_helper.AttributeOpinionPair;
import com.dianping.algorithm.dp_review_mining.dataAccessLayer.MongoDB;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class ShopGetter {

	public static MongoDB mongodb = new MongoDB();
	
	public static void main(String[] args) {
		
		
		mongodb.useCollection("dpShopSummaryDishPairs");
		DBCursor cursor = mongodb.find(null);
		int largeShopNum = 0;
		try
		{
		
			List<AttributeOpinionPair> aopairs = new ArrayList<AttributeOpinionPair>();
			while (cursor.hasNext())
			{
				DBObject entry = cursor.next();
				int shopId = (Integer) entry.get("shopId");
				String aops = (String)entry.get("aop");
				String tokens[] = aops.split("#");
				HashSet<String> names = new HashSet<String>();
				for(String token:tokens)
				{
					if(token.length()<=0)
						continue;
					//System.out.println(token);
					AttributeOpinionPair aop = new AttributeOpinionPair(token);
					names.add(aop.attr.attribute);
				}
				
				if(names.size() >=10 )
				{
					System.out.println(shopId+"\t"+names.size());
					largeShopNum ++;
				}//System.out.println(review.getSimpleTagContent());
			}
			System.out.println(largeShopNum);
			
		}
		finally
		{
			cursor.close();
		}
		 
	}
}
