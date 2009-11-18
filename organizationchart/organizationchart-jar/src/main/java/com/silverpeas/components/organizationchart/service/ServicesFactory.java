package com.silverpeas.components.organizationchart.service;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ServicesFactory implements ApplicationContextAware {
	public static final String ORGANIZATION_CHART_SERVICE = "organizationChartService";
	
	private ApplicationContext context;
	private static ServicesFactory instance;
	
	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.context = ctx;
	}

	private ServicesFactory() {
	}
	
	protected static ServicesFactory getInstance() {
		synchronized (ServicesFactory.class) {
			if (ServicesFactory.instance == null) {
				ServicesFactory.instance = new ServicesFactory();
			}
		}
		return ServicesFactory.instance;
	}
	
	public static OrganizationChartService getOrganizationChartService() {
		return (OrganizationChartService) getInstance().context.getBean(ORGANIZATION_CHART_SERVICE);
	  }
}
