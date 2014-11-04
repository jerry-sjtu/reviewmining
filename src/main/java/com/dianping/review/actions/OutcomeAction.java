package com.dianping.review.actions;


import org.apache.struts2.dispatcher.DefaultActionSupport;

import com.dianping.review.business.OutcomeDeterminer;

public class OutcomeAction extends DefaultActionSupport {
	private static final long serialVersionUID = 4794228472575863567L;

	private OutcomeDeterminer outcomeDeterminer;

	public String getOutcome() {
		return outcomeDeterminer.getOutcome();
	}

	public OutcomeDeterminer getOutcomeDeterminer() {
		return outcomeDeterminer;
	}

	public void setOutcomeDeterminer(OutcomeDeterminer outcomeDeterminer) {
		this.outcomeDeterminer = outcomeDeterminer;
	}
}
