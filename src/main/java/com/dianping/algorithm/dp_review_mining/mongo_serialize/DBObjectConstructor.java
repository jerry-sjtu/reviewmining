/**
 * Project: review-mining-single
 * 
 * File Created at 2012-10-10
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
package com.dianping.algorithm.dp_review_mining.mongo_serialize;

import com.mongodb.DBObject;

/**
 * TODO Comment of ObjectMongoSerializer
 * @author rui.xie
 *
 */
public interface DBObjectConstructor
{
	DBObject constructMongoObj();
}
