package com.silverpeas.blog.model;

import java.io.Serializable;


public class Archive implements Serializable
{
	private String name;
	private String monthId; //between 0 and 11
	private String year;
	private String beginDate;
	private String endDate;
	
	public Archive()
	{}
	
	public Archive(String name, String beginningDate, String endDate) {
		super();
		this.name = name;
		this.beginDate = beginningDate;
		this.endDate = endDate;
	}

	public String getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(String beginDate) {
		this.beginDate = beginDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getMonthId() {
		return monthId;
	}

	public void setMonthId(String monthId) {
		this.monthId = monthId;
	}

}
