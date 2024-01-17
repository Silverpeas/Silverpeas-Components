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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.silverpeas.components.organizationchart.control.OrganizationChartSessionController;
import org.silverpeas.components.organizationchart.model.OrganizationalChartType;
import org.silverpeas.components.organizationchart.view.CategoryBox;
import org.silverpeas.components.organizationchart.view.ChartPersonnVO;
import org.silverpeas.components.organizationchart.view.ChartUnitVO;
import org.silverpeas.components.organizationchart.view.ChartVO;
import org.silverpeas.components.organizationchart.view.OrganizationBox;
import org.silverpeas.components.organizationchart.view.UserVO;

public class OrganizationChartProcessor {

  public static final String JSP_BASE = "/organizationchart/jsp/";
  public static final String DESTINATION_DISPLAY_CHART = "chart.jsp";
  public static final String DESTINATION_PERSON = "person.jsp";
  public static final String DESTINATION_ERROR = "check.jsp";

  public static final String PARAM_DOMAINID = "chartDomainSilverpeas";

  public static String processOrganizationChart(HttpServletRequest request,
      OrganizationChartSessionController controller) {
    request.removeAttribute("error");
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
        request.setAttribute("error", "impossible de trouver cette personne '" + login +
            "' dans le domaine Silverpeas '" + organizationchartSC.getDomainId() + "'");
        return JSP_BASE + DESTINATION_ERROR;
      }
    } else {
      request.setAttribute("error", "impossible de recupèrer les bons paramètres - pas de login");
      return JSP_BASE + DESTINATION_ERROR;
    }
  }

  private static ChartUnitVO buildFakeChartUnit() {
    ChartUnitVO chart = new ChartUnitVO();

    OrganizationBox rootOrganization = new OrganizationBox();
    List<UserVO> mainActors = new ArrayList<>();
    mainActors.add(new UserVO("Laurent Morel", "l.morel", "Directeur"));
    mainActors.add(new UserVO("Dupond Jean", "j.dupond", "Directeur associé"));
    rootOrganization.setName("DGS");
    rootOrganization.setCenterLinkActive(false);
    rootOrganization.setDetailLinkActive(true);
    rootOrganization.setDn("OU=DGS,OU=Issy,dc=mondomain,dc=com");
    rootOrganization.setMainActors(mainActors);
    rootOrganization.setParentDn("OU=Issy,dc=mondomain,dc=com");
    chart.setRootOrganization(rootOrganization);

    CategoryBox leftRole = new CategoryBox();
    leftRole.setName("Secrétaire");
    List<UserVO> letusers = new ArrayList<>();
    letusers.add(new UserVO("Murielle Dus", "m.duc", null));
    letusers.add(new UserVO("Camille Bet", "c.bet", null));
    leftRole.setUsers(letusers);
    chart.setLeftRole(leftRole);

    CategoryBox rightRole = new CategoryBox();
    rightRole.setName("Adjoints");
    List<UserVO> rightusers = new ArrayList<>();
    rightusers.add(new UserVO("Jeanne Calment", "m.duc", null));
    rightusers.add(new UserVO("Pierre Le Bon", "p.lebon", null));
    rightRole.setUsers(rightusers);
    chart.setRightRole(rightRole);

    List<OrganizationBox> subOrganizations = new ArrayList<>();
    OrganizationBox firstOrganization = new OrganizationBox();
    List<UserVO> mainActors1 = new ArrayList<>();
    mainActors1.add(new UserVO("Laurent1 Morel", "l.morel1", "Directeur1"));
    mainActors1.add(new UserVO("Dupond1 Jean", "j.dupond1", "Directeur associé1"));
    firstOrganization.setName("Elus");
    firstOrganization.setCenterLinkActive(true);
    firstOrganization.setDetailLinkActive(true);
    firstOrganization.setDn("OU=Elus,OU=DGS,OU=Issy,dc=mondomain,dc=com");
    firstOrganization.setMainActors(mainActors);
    firstOrganization.setParentDn("OU=DGS,OU=Issy,dc=mondomain,dc=com");
    subOrganizations.add(firstOrganization);
    subOrganizations.add(firstOrganization);
    subOrganizations.add(firstOrganization);
    subOrganizations.add(firstOrganization);
    subOrganizations.add(firstOrganization);
    subOrganizations.add(firstOrganization);
    subOrganizations.add(firstOrganization);
    subOrganizations.add(firstOrganization);
    chart.setSubOrganizations(subOrganizations);

    return chart;
  }

  private static ChartPersonnVO buildFakePersonUnit() {
    ChartPersonnVO chart = new ChartPersonnVO();

    OrganizationBox rootOrganization = new OrganizationBox();
    List<UserVO> mainActors = new ArrayList<>();
    mainActors.add(new UserVO("Laurent Morel", "l.morel", "Directeur"));
    mainActors.add(new UserVO("Dupond Jean", "j.dupond", "Directeur associé"));
    rootOrganization.setName("DGS");
    rootOrganization.setDn("OU=DGS,OU=Issy,dc=mondomain,dc=com");
    rootOrganization.setMainActors(mainActors);
    rootOrganization.setParentDn("OU=Issy,dc=mondomain,dc=com");
    chart.setRootOrganization(rootOrganization);

    CategoryBox category1 = new CategoryBox();
    category1.setName("Secrétaire");
    List<UserVO> letusers = new ArrayList<>();
    letusers.add(new UserVO("Murielle Dus", "m.duc", null));
    letusers.add(new UserVO("Camille Bet", "c.bet", null));
    category1.setUsers(letusers);

    CategoryBox category2 = new CategoryBox();
    category2.setName("Adjoints");
    List<UserVO> rightusers = new ArrayList<>();
    rightusers.add(new UserVO("Jeanne Calment", "m.duc", null));
    rightusers.add(new UserVO("Pierre Le Bon", "p.lebon", null));
    category2.setUsers(rightusers);

    List<CategoryBox> categories = new ArrayList<>();
    categories.add(category1);
    categories.add(category2);
    chart.setCategories(categories);

    return chart;
  }
}
