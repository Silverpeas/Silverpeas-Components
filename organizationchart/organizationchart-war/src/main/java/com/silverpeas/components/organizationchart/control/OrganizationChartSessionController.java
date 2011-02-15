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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.components.organizationchart.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.silverpeas.components.organizationchart.model.OrganizationalChart;
import com.silverpeas.components.organizationchart.model.OrganizationalChartType;
import com.silverpeas.components.organizationchart.model.OrganizationalPerson;
import com.silverpeas.components.organizationchart.model.OrganizationalRole;
import com.silverpeas.components.organizationchart.model.OrganizationalUnit;
import com.silverpeas.components.organizationchart.model.PersonCategory;
import com.silverpeas.components.organizationchart.service.OrganizationChartConfiguration;
import com.silverpeas.components.organizationchart.service.OrganizationChartService;
import com.silverpeas.components.organizationchart.service.ServicesFactory;
import com.silverpeas.components.organizationchart.view.CategoryBox;
import com.silverpeas.components.organizationchart.view.ChartPersonnVO;
import com.silverpeas.components.organizationchart.view.ChartUnitVO;
import com.silverpeas.components.organizationchart.view.ChartVO;
import com.silverpeas.components.organizationchart.view.OrganizationBox;
import com.silverpeas.components.organizationchart.view.UserVO;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;

public class OrganizationChartSessionController extends AbstractComponentSessionController {

  private static final String PARAM_SERVERURL = "serverURL";
  private static final String PARAM_CTXFACTORY = "initialContextFactory";
  private static final String PARAM_AUTHMODE = "authenticationMode";
  private static final String PARAM_PRINCIPAL = "principal";
  private static final String PARAM_CREDENTIAL = "credentials";
  private static final String PARAM_LDAP_ROOT = "ldapRoot";
  private static final String PARAM_LDAP_CLASS_PERSON = "ldapClassPerson";
  private static final String PARAM_LDAP_CLASS_UNIT = "ldapClassUnit";
  private static final String PARAM_LDAP_ATT_UNIT = "ldapAttUnit";
  private static final String PARAM_LDAP_ATT_NAME = "ldapAttName";
  private static final String PARAM_LDAP_ATT_TITLE = "ldapAttTitle"; // champ LDAP du titre
  private static final String PARAM_LDAP_ATT_DESC = "ldapAttDesc"; // champ ldap de la description
  private static final String PARAM_LDAP_ATT_ACCOUNT = "ldapAttAccount"; // champ ldap de
  // l'identifiant de compte
  // Silverpeas
  private static final String PARAM_UNITSCHART_CENTRAL_LABEL = "unitsChartCentralLabel";
  private static final String PARAM_UNITSCHART_RIGHT_LABEL = "unitsChartRightLabel";
  private static final String PARAM_UNITSCHART_LEFT_LABEL = "unitsChartLeftLabel";
  private static final String PARAM_PERSONNSCHART_CENTRAL_LABEL = "personnsChartCentralLabel";
  private static final String PARAM_PERSONNSCHART_CATEGORIES_LABEL = "personnsChartCategoriesLabel";
  private static final String PARAM_UNITSCHART_OTHERSINFOS_KEYS = "unitsChartOthersInfosKeys";
  private static final String PARAM_PERSONNSCHART_OTHERSINFOS_KEYS = "personnsChartOthersInfosKeys";
  private static final String PARAM_LDAP_ATT_ACTIF = "ldapAttActif";
  private static final String PARAM_DOMAIN_ID = "chartDomainSilverpeas";

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public OrganizationChartSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.components.organizationchart.multilang.OrganizationChartBundle",
        "com.silverpeas.components.organizationchart.settings.OrganizationChartIcons");

    OrganizationChartConfiguration config = loadConfiguration();
    service = ServicesFactory.getOrganizationChartService();
    service.configure(config);
  }

  public ChartVO getChart(String baseDN, OrganizationalChartType chartType) {
    OrganizationalChart chart = service.getOrganizationChart(baseDN, chartType);
    ChartVO chartVO = null;

    if (chart != null) {
      if ((chart.getUnits() != null) && (chart.getUnits().isEmpty())) {
        chart = service.getOrganizationChart(baseDN, OrganizationalChartType.TYPE_PERSONNCHART);
        chartType = OrganizationalChartType.TYPE_PERSONNCHART;
      }

      switch (chartType) {
        case TYPE_UNITCHART:
          chartVO = buildChartUnitVO(chart);
          break;

        case TYPE_PERSONNCHART:
          chartVO = buildChartPersonsVO(chart);
          break;

        default:
          chartVO = null;
      }
    }

    return chartVO;
  }

  private UserVO OrganizationalPerson2UserVO(OrganizationalPerson person, OrganizationalRole role) {
    String displayedRole = (role == null) ? "" : role.getLabel();
    return new UserVO(person.getName(), person.getSilverpeasAccount(), displayedRole, person
        .getDetail());
  }

  private ChartVO buildChartUnitVO(OrganizationalChart chart) {
    ChartUnitVO chartVO = new ChartUnitVO();

    // rootOrganization
    OrganizationBox rootOrganization = new OrganizationBox();
    chartVO.setRootOrganization(rootOrganization);
    rootOrganization.setName(chart.getRoot().getName());
    rootOrganization.setParentDn(chart.getRoot().getParentOu());

    // Looks for specific users
    List<UserVO> leftUsers = new ArrayList<UserVO>();
    List<UserVO> rightUsers = new ArrayList<UserVO>();
    List<UserVO> mainActors = new ArrayList<UserVO>();

    for (OrganizationalPerson person : chart.getPersonns()) {
      if (person.isVisibleOnCenter()) {
        mainActors.add(OrganizationalPerson2UserVO(person, person.getVisibleCenterRole()));
      }

      else if (person.isVisibleOnLeft()) {
        leftUsers.add(OrganizationalPerson2UserVO(person, person.getVisibleLeftRole()));
      }

      else if (person.isVisibleOnRight()) {
        rightUsers.add(OrganizationalPerson2UserVO(person, person.getVisibleRightRole()));
      }
    }

    if (!mainActors.isEmpty()) {
      rootOrganization.setMainActors(mainActors);
    }

    if (!leftUsers.isEmpty()) {
      CategoryBox leftRole = new CategoryBox();
      leftRole.setName(leftUsers.get(0).getRole());
      leftRole.setUsers(leftUsers);
      chartVO.setLeftRole(leftRole);
    }

    if (!rightUsers.isEmpty()) {
      CategoryBox rightRole = new CategoryBox();
      rightRole.setName(rightUsers.get(0).getRole());
      rightRole.setUsers(rightUsers);
      chartVO.setRightRole(rightRole);
    }

    // Looks for sub organizations
    List<OrganizationBox> subOrganizations = new ArrayList<OrganizationBox>();
    for (OrganizationalUnit subOrganization : chart.getUnits()) {
      OrganizationBox subUnit = new OrganizationBox();
      subUnit.setDn(subOrganization.getCompleteName());
      subUnit.setParentDn(subOrganization.getParentOu());
      subUnit.setName(subOrganization.getName());
      subUnit.setCenterLinkActive(subOrganization.hasSubUnits());
      subUnit.setDetailLinkActive(subOrganization.hasMembers());
      subOrganizations.add(subUnit);
    }
    chartVO.setSubOrganizations(subOrganizations);

    return chartVO;
  }

  private ChartVO buildChartPersonsVO(OrganizationalChart chart) {
    ChartPersonnVO chartVO = new ChartPersonnVO();

    // rootOrganization
    OrganizationBox rootOrganization = new OrganizationBox();
    chartVO.setRootOrganization(rootOrganization);
    rootOrganization.setName(chart.getRoot().getName());
    rootOrganization.setParentDn(chart.getRoot().getParentOu());

    // Looks for specific users
    List<UserVO> mainActors = new ArrayList<UserVO>();

    for (OrganizationalPerson person : chart.getPersonns()) {
      if (person.isVisibleOnCenter()) {
        mainActors.add(OrganizationalPerson2UserVO(person, person.getVisibleCenterRole()));
      }
    }

    if (!mainActors.isEmpty()) {
      rootOrganization.setMainActors(mainActors);
    }

    // Sort people by category
    Map<String, List<UserVO>> usersByCategory = new HashMap<String, List<UserVO>>();
    for (OrganizationalPerson person : chart.getPersonns()) {
      PersonCategory category = person.getVisibleCategory();
      if (category != null) {
        List<UserVO> usersOfCurrentCategory = usersByCategory.get(category.getName());
        if (usersOfCurrentCategory == null) {
          usersOfCurrentCategory = new ArrayList<UserVO>();
          usersByCategory.put(category.getName(), usersOfCurrentCategory);
        }
        usersOfCurrentCategory.add(new UserVO(person.getName(), person.getSilverpeasAccount(),
            person.getFonction(), person.getDetail()));
      }
    }

    // create category boxes
    List<CategoryBox> categories = new ArrayList<CategoryBox>();
    for (PersonCategory category : chart.getCategories()) {
      CategoryBox categoryBox = new CategoryBox();
      categoryBox.setName(category.getName());
      categoryBox.setUsers(usersByCategory.get(category.getName()));
      categories.add(categoryBox);
    }
    chartVO.setCategories(categories);

    return chartVO;
  }

  public OrganizationalChart getOrganizationChart(String baseDN, OrganizationalChartType chartType) {
    OrganizationalChart chart = service.getOrganizationChart(baseDN, chartType);

    if (chart != null) {
      if ((chart.getUnits() != null) && (chart.getUnits().isEmpty())) {
        // if no sub-units, force to personn chart
        chartType = OrganizationalChartType.TYPE_PERSONNCHART;
        chart = service.getOrganizationChart(baseDN, chartType);
      }
    }

    return chart;
  }

  private OrganizationChartService service = null;

  private OrganizationChartConfiguration loadConfiguration() {
    OrganizationChartConfiguration config = new OrganizationChartConfiguration();

    config.setServerURL(getComponentParameterValue(PARAM_SERVERURL));
    config.setInitialContextFactory(getComponentParameterValue(PARAM_CTXFACTORY));
    config.setAuthenticationMode(getComponentParameterValue(PARAM_AUTHMODE));
    config.setPrincipal(getComponentParameterValue(PARAM_PRINCIPAL));
    config.setCredentials(getComponentParameterValue(PARAM_CREDENTIAL));

    config.setLdapRoot(getComponentParameterValue(PARAM_LDAP_ROOT));
    config.setLdapClassPerson(getComponentParameterValue(PARAM_LDAP_CLASS_PERSON));
    config.setLdapClassUnit(getComponentParameterValue(PARAM_LDAP_CLASS_UNIT));
    config.setLdapAttUnit(getComponentParameterValue(PARAM_LDAP_ATT_UNIT));
    config.setLdapAttName(getComponentParameterValue(PARAM_LDAP_ATT_NAME));
    config.setLdapAttTitle(getComponentParameterValue(PARAM_LDAP_ATT_TITLE)); // champ LDAP du titre
    config.setLdapAttDesc(getComponentParameterValue(PARAM_LDAP_ATT_DESC)); // champ ldap de la
    // description
    config.setLdapAttAccount(getComponentParameterValue(PARAM_LDAP_ATT_ACCOUNT)); // champ ldap de
    // l'identifiant
    // de compte
    // Silverpeas

    config
        .setUnitsChartCentralLabel(getRoles(getComponentParameterValue(PARAM_UNITSCHART_CENTRAL_LABEL)));
    config
        .setUnitsChartRightLabel(getRoles(getComponentParameterValue(PARAM_UNITSCHART_RIGHT_LABEL)));
    config
        .setUnitsChartLeftLabel(getRoles(getComponentParameterValue(PARAM_UNITSCHART_LEFT_LABEL)));

    config
        .setPersonnsChartCentralLabel(getRoles(getComponentParameterValue(PARAM_PERSONNSCHART_CENTRAL_LABEL)));
    config
        .setPersonnsChartCategoriesLabel(getRoles(getComponentParameterValue(PARAM_PERSONNSCHART_CATEGORIES_LABEL)));
    config
        .setUnitsChartOthersInfosKeys(getKeysAndLabel(getComponentParameterValue(PARAM_UNITSCHART_OTHERSINFOS_KEYS)));
    config
        .setPersonnsChartOthersInfosKeys(getKeysAndLabel(getComponentParameterValue(PARAM_PERSONNSCHART_OTHERSINFOS_KEYS)));
    config.setLdapAttActif(getComponentParameterValue(PARAM_LDAP_ATT_ACTIF));

    config.setDomainId(getComponentParameterValue(PARAM_DOMAIN_ID));

    return config;
  }

  /**
   * Transformed a flat list of Roles to a List of OrganizationalRole objects.
   * @param rolesAsString flat list of Roles ("labelRole1=keyRole1;labelRole2=keyRole2")
   * @return a List of OrganizationalRole objects
   */
  private List<OrganizationalRole> getRoles(String rolesAsString) {
    List<OrganizationalRole> listRoles = new ArrayList<OrganizationalRole>();

    if (StringUtil.isDefined(rolesAsString)) {

      String[] roles = rolesAsString.split(";");

      for (String role : roles) {
        String[] roleDetails = role.split("=");
        if (roleDetails.length == 2) {
          listRoles.add(new OrganizationalRole(roleDetails[0], roleDetails[1]));
        } else {
          SilverTrace.info("organizationchart",
              "OrganizationChartLdapServiceImpl.getRole()", "root.MSG_GEN_PARAM_VALUE",
              "bad format for a role " + role);
        }
      }
    }
    return listRoles;
  }

  /**
   * Transformed a flat list of label/attributeName to a Map<key, value>.
   * @param attributesAsString flat list of Roles ("labelChamp1=keyLdap1;labelChamp2=keyLdap2")
   * @return a Map<key, value>.
   */
  private Map<String, String> getKeysAndLabel(String attributesAsString) {
    Map<String, String> map = new HashMap<String, String>();

    if (StringUtil.isDefined(attributesAsString)) {
      String[] attributes = attributesAsString.split(";");

      for (String attribute : attributes) {
        String[] details = attribute.split("=");
        map.put(details[1], details[0]);
      }
    }
    return map;
  }

  public String getLibelleAttribut(String attId) {
    String attName = "organizationchart.attribut." + attId;
    String libelle = getString(attName);
    if (libelle == null || libelle.equalsIgnoreCase(attName))
      return null;
    else
      return libelle;
  }

  public String getDomainId() {
    return getComponentParameterValue(PARAM_DOMAIN_ID);
  }

  public String getUserIdFromLogin(String login) {
    String userId = null;
    String domainId = getDomainId();

    if (StringUtil.isDefined(domainId)) {
      AdminController adminController = new AdminController(getUserId());
      userId = adminController.getUserIdByLoginAndDomain(login, domainId);
    }

    return userId;
  }
}