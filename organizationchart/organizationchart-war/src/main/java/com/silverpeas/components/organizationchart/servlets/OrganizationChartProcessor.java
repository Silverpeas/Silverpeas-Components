package com.silverpeas.components.organizationchart.servlets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.components.organizationchart.control.OrganizationChartSessionController;
import com.silverpeas.components.organizationchart.model.OrganizationalPerson;
import com.silverpeas.components.organizationchart.service.ServicesFactory;

public class OrganizationChartProcessor {

	public static String JSP_BASE = "/organizationchart/jsp/";
	public static final String DESTINATION_DISPLAY_CHART = "chart.jsp";
	public static final String DESTINATION_PERSON = "person.jsp";
	public static final String DESTINATION_ERROR = "check.jsp";
	
	public static String processOrganizationChart(HttpServletRequest request, String componentId) { 
		request.removeAttribute("error");
		if(request.getSession().getAttribute("organigramme") == null || 
				request.getSession().getAttribute("orgId") != componentId) {
			OrganizationalPerson[] result = ServicesFactory.getOrganizationChartService().
				getOrganizationChart(componentId);
			if(result != null) {
				request.getSession().setAttribute("organigramme", result);
				request.getSession().setAttribute("orgId", componentId);
			}
			else request.setAttribute("error", "une erreur s'est produite lors du chargement des données");
		}
		return JSP_BASE + DESTINATION_DISPLAY_CHART;
	}
	
	public static String processPerson(HttpServletRequest request, String idStr, 
			OrganizationChartSessionController  organizationchartSC) {
		request.removeAttribute("error");
		try {
			OrganizationalPerson[] org = (OrganizationalPerson[])request.getSession().getAttribute("organigramme");
			if(org != null) {
				int id = new Integer(idStr.substring(2)).intValue();
				Map<String, String> persDetail = ServicesFactory.getOrganizationChartService().getOrganizationalPerson(org, id);
				if(persDetail != null) {
					List<String> details = new ArrayList<String>();
					for (Map.Entry<String, String> det : persDetail.entrySet()){
					    String lib = organizationchartSC.getLibelleAttribut(det.getKey());
					    if(lib != null) 
					    	details.add(lib + " : " + det.getValue());
					}
					String[] dets = new String[details.size()];
					details.toArray(dets);
					request.setAttribute("person",dets);
				}
				return JSP_BASE + DESTINATION_PERSON;
			}
			else {
				request.setAttribute("error", "impossible d'aficher le détail de cette personne" );
				return JSP_BASE + DESTINATION_ERROR;
			}
		} catch (Exception ex) {
			request.setAttribute("error", "impossible d'aficher le détail de cette personne : \n" + ex.getMessage());
			return JSP_BASE + DESTINATION_ERROR;
		}
	}
}
