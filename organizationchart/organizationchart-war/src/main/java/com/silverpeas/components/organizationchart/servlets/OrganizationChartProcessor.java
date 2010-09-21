/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.components.organizationchart.servlets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.components.organizationchart.control.OrganizationChartSessionController;
import com.silverpeas.components.organizationchart.model.OrganizationalChart;
import com.silverpeas.components.organizationchart.model.OrganizationalPerson;
import com.silverpeas.components.organizationchart.service.ServicesFactory;

public class OrganizationChartProcessor {

  public static String JSP_BASE = "/organizationchart/jsp/";
  public static final String DESTINATION_DISPLAY_CHART = "chart.jsp";
  public static final String DESTINATION_PERSON = "person.jsp";
  public static final String DESTINATION_ERROR = "check.jsp";

  public static String processOrganizationChart(HttpServletRequest request, String componentId) {
	  request.removeAttribute("error");
      String rootOu = request.getParameter("baseOu");
      
      String chartType = request.getParameter("chartType");
      int type = OrganizationalChart.UNITCHART;
      if(chartType != null){
    	  try{
    		  type = Integer.valueOf(chartType);
    	  }catch(NumberFormatException e){
    		  type = OrganizationalChart.UNITCHART;
    	  }
      }
      
      OrganizationalChart result = ServicesFactory.getOrganizationChartService().
          getOrganizationChart(componentId, rootOu, type);
      if (result != null) {
        if(result.getUnits() == null || result.getUnits().length == 0){
        	// if no sub-units, force to personn chart
        	type = OrganizationalChart.PERSONNCHART;
        	result = ServicesFactory.getOrganizationChartService().getOrganizationChart(componentId, rootOu, type);
        	 if (result == null) {
        		 request.setAttribute("error", "Une erreur s'est produite lors du chargement des donnees (redirection automatique organigramme personnes)");
        	 }
        } 
    	request.getSession().setAttribute("organigramme", result);
        request.getSession().setAttribute("orgId", componentId);
        request.getSession().setAttribute("chartType", type);
      } else {
        request.setAttribute("error", "une erreur s'est produite lors du chargement des donnÃ©es");
      }   
      return JSP_BASE + DESTINATION_DISPLAY_CHART;
  }

  public static String processPerson(HttpServletRequest request, String idStr,
      OrganizationChartSessionController organizationchartSC) {
    request.removeAttribute("error");
    try {
      OrganizationalPerson[] org =
          (OrganizationalPerson[]) request.getSession().getAttribute("organigramme");
      if (org != null) {
        int id = new Integer(idStr.substring(2)).intValue();
        Map<String, String> persDetail =
            ServicesFactory.getOrganizationChartService().getOrganizationalPerson(org, id);
        if (persDetail != null) {
          List<String> details = new ArrayList<String>();
          for (Map.Entry<String, String> det : persDetail.entrySet()) {
            String lib = organizationchartSC.getLibelleAttribut(det.getKey());
            if (lib != null) {
              details.add(lib + " : ");
              details.add(det.getValue());
            }
          }
          String[] dets = new String[details.size()];
          details.toArray(dets);
          request.setAttribute("person", dets);
        }
        return JSP_BASE + DESTINATION_PERSON;
      } else {
        request.setAttribute("error", "impossible d'aficher le dÃ©tail de cette personne");
        return JSP_BASE + DESTINATION_ERROR;
      }
    } catch (Exception ex) {
      request.setAttribute("error", "impossible d'aficher le dÃ©tail de cette personne : \n" +
          ex.getMessage());
      return JSP_BASE + DESTINATION_ERROR;
    }
  }
}
