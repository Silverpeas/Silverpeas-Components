package com.silverpeas.components.organizationchart.service;

import java.util.Map;
import com.silverpeas.components.organizationchart.model.OrganizationalPerson;

public interface OrganizationChartService {

	public OrganizationalPerson[] getOrganizationChart(String componentId);
	
	public Map<String, String> getOrganizationalPerson(OrganizationalPerson[] org, int id);
}
