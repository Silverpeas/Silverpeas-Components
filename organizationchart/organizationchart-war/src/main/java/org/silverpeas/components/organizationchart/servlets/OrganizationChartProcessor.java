/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.organizationchart.servlets;

import jakarta.servlet.http.HttpServletRequest;

import org.silverpeas.components.organizationchart.control.OrganizationChartSessionController;
import org.silverpeas.components.organizationchart.model.OrganizationalChartType;
import org.silverpeas.components.organizationchart.view.ChartVO;

public class OrganizationChartProcessor {

  public static final String JSP_BASE = "/organizationchart/jsp/";
  public static final String DESTINATION_DISPLAY_CHART = "chart.jsp";
  public static final String DESTINATION_PERSON = "person.jsp";
  public static final String DESTINATION_ERROR = "check.jsp";

  public static final String PARAM_DOMAINID = "chartDomainSilverpeas";
  private static final String ERROR = "error";

  private OrganizationChartProcessor() {
  }

  public static String processOrganizationChart(HttpServletRequest request,
      OrganizationChartSessionController controller) {
    request.removeAttribute(ERROR);
    String rootOu = request.getParameter("baseOu");
    String chartType = request.getParameter("chartType");
    ChartVO chart = controller.getChart(rootOu, OrganizationalChartType.fromString(chartType));
    request.getSession().setAttribute("organigramme", chart);
    return JSP_BASE + DESTINATION_DISPLAY_CHART;
  }

  public static String processSilverpeasUser(HttpServletRequest request,
      OrganizationChartSessionController organizationchartSC) {
    String login = request.getParameter("login");

    if (login != null) {
      String userId = login;
      if (organizationchartSC.isLDAP()) {
        userId = organizationchartSC.getUserIdFromLogin(login);
      }

      if (userId != null) {
        return "/Rprofil/jsp/Main?userId=" + userId;
      } else {
        request.setAttribute(ERROR, "impossible de trouver cette personne '" + login +
                                    "' dans le domaine Silverpeas '" + organizationchartSC.getDomainId() + "'");
        return JSP_BASE + DESTINATION_ERROR;
      }
    } else {
      request.setAttribute(ERROR, "impossible de recupérer les bons paramètres - pas de login");
      return JSP_BASE + DESTINATION_ERROR;
    }
  }

}
