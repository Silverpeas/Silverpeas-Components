package com.silverpeas.components.organizationchart.model;


public class OrganizationalChart {
	
	private OrganizationalUnit parent;
	private OrganizationalPerson[] personns;
	private OrganizationalUnit[] categories;
	private OrganizationalUnit[] units;
	
	private int chartType;
	
	public static final int UNITCHART = 0;
	public static final int PERSONNCHART = 1;
	
	public OrganizationalChart(OrganizationalUnit parent, OrganizationalUnit[] units, OrganizationalPerson[] personns){
		// case unit chart: you needs personns and units
		this.chartType = UNITCHART;
		this.parent = parent;
		this.units = units;
		this.personns = personns;
		this.categories =null;
	}
	
	public OrganizationalChart(OrganizationalUnit parent, OrganizationalPerson[] personns, OrganizationalUnit[] categories){
		// case personns chart: needs personns and categories
		this.chartType = PERSONNCHART;
		this.parent = parent;
		this.categories = categories;
		this.personns = personns;
		this.units = null;
	}

	public void setParent(OrganizationalUnit parent) {
		this.parent = parent;
	}

	public OrganizationalUnit getParent() {
		return parent;
	}

	public void setChartType(int chartType) {
		this.chartType = chartType;
	}

	public int getChartType() {
		return chartType;
	}
	
	public void setPersonns(OrganizationalPerson[] personns) {
		this.personns = personns;
	}
	
	public OrganizationalPerson[] getPersonns() {
		return personns;
	}
	
	public void setOus(OrganizationalUnit[] units) {
		this.units = units;
	}
	
	public OrganizationalUnit[] getUnits() {
		return units;
	}

	public void setCategories(OrganizationalUnit[] categories) {
		this.categories = categories;
	}

	public OrganizationalUnit[] getCategories() {
		return categories;
	}
}
