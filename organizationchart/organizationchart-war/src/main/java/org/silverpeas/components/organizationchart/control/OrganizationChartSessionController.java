/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.organizationchart.control;

import org.silverpeas.components.organizationchart.model.OrganizationalChart;
import org.silverpeas.components.organizationchart.model.OrganizationalChartType;
import org.silverpeas.components.organizationchart.model.OrganizationalPerson;
import org.silverpeas.components.organizationchart.model.OrganizationalRole;
import org.silverpeas.components.organizationchart.model.OrganizationalUnit;
import org.silverpeas.components.organizationchart.model.PersonCategory;
import org.silverpeas.components.organizationchart.service.AbstractOrganizationChartConfiguration;
import org.silverpeas.components.organizationchart.service.GroupOrganizationChartConfiguration;
import org.silverpeas.components.organizationchart.service.LdapOrganizationChartConfiguration;
import org.silverpeas.components.organizationchart.service.OrganizationChartService;
import org.silverpeas.components.organizationchart.view.CategoryBox;
import org.silverpeas.components.organizationchart.view.ChartPersonnVO;
import org.silverpeas.components.organizationchart.view.ChartUnitVO;
import org.silverpeas.components.organizationchart.view.ChartVO;
import org.silverpeas.components.organizationchart.view.OrganizationBox;
import org.silverpeas.components.organizationchart.view.UserVO;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.util.viewgenerator.html.UserNameGenerator;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
  // champ LDAP du titre
  private static final String PARAM_LDAP_ATT_TITLE = "ldapAttTitle";
  // champ ldap de la description
  private static final String PARAM_LDAP_ATT_DESC = "ldapAttDesc";
  // champ ldap de l'identifiant de compte Silverpeas
  private static final String PARAM_LDAP_ATT_ACCOUNT = "ldapAttAccount";
  private static final String PARAM_LDAP_ATT_CSS = "ldapAttCSS";
  private static final String PARAM_UNITSCHART_CENTRAL_LABEL = "unitsChartCentralLabel";
  private static final String PARAM_UNITSCHART_RIGHT_LABEL = "unitsChartRightLabel";
  private static final String PARAM_UNITSCHART_LEFT_LABEL = "unitsChartLeftLabel";
  private static final String PARAM_PERSONNSCHART_CENTRAL_LABEL = "personnsChartCentralLabel";
  private static final String PARAM_PERSONNSCHART_CATEGORIES_LABEL = "personnsChartCategoriesLabel";
  private static final String PARAM_UNITSCHART_OTHERSINFOS_KEYS = "unitsChartOthersInfosKeys";
  private static final String PARAM_PERSONNSCHART_OTHERSINFOS_KEYS = "personnsChartOthersInfosKeys";
  private static final String PARAM_LDAP_ATT_ACTIF = "ldapAttActif";
  private static final String PARAM_DOMAIN_ID = "chartDomainSilverpeas";
  private static final String PARAM_LABELS = "labels";
  private static final String PARAM_AVATARS = "avatars";

  private final AbstractOrganizationChartConfiguration config;

  private List<OrganizationBox> breadcrumb = new ArrayList<>();

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public OrganizationChartSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.components.organizationchart.multilang.OrganizationChartBundle",
        "org.silverpeas.components.organizationchart.settings.OrganizationChartIcons",
        "org.silverpeas.components.organizationchart.settings.OrganizationChartSettings");

    if (isLDAP()) {
      config = loadLDAPConfiguration();
    } else {
      config = loadGroupConfiguration();
    }
  }

  public final boolean isLDAP() {
    return getComponentId().startsWith("organizationchart");
  }

  public ChartVO getChart(String baseDN, OrganizationalChartType chartType) {
    String root = baseDN;
    if (StringUtil.isNotDefined(root)) {
      root = getComponentParameterValue(PARAM_LDAP_ROOT);
    }
    OrganizationalChart chart = getService().getOrganizationChart(getConfig(), root, chartType);
    ChartVO chartVO = null;

    if (chart != null) {
      if ((chart.getUnits() != null) && (chart.getUnits().isEmpty())) {
        chart = getService()
            .getOrganizationChart(getConfig(), root, OrganizationalChartType.TYPE_PERSONNCHART);
        chartType = OrganizationalChartType.TYPE_PERSONNCHART;
      }

      switch (chartType) {
        case TYPE_UNITCHART:
          chartVO = buildChartUnitVO(chart);
          processBreadcrumb(chartVO.getRootOrganization());
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

  private void processBreadcrumb(OrganizationBox currentOU) {
    try {
      boolean isInBreadcrumb = false;
      Iterator<OrganizationBox> breadcrumbIt = breadcrumb.iterator();
      while (breadcrumbIt.hasNext()) {
        OrganizationBox element = breadcrumbIt.next();
        if (element.getUrl().equalsIgnoreCase(currentOU.getUrl())) {
          isInBreadcrumb = true;
        }
        if (isInBreadcrumb) {
          breadcrumbIt.remove();
        }
      }
      breadcrumb.add(currentOU);
    } catch (UnsupportedEncodingException e) {
      SilverLogger.getLogger(this).error(e.getLocalizedMessage(), e);
    }
  }

  private UserVO organizationalPerson2UserVO(OrganizationalPerson person, OrganizationalRole role) {
    String displayedRole = (role == null) ? "" : role.getLabel();
    String loginOrId = person.getSilverpeasAccount();
    String avatar = null;
    String name = person.getName();
    UserDetail user;
    if (!isLDAP()) {
      loginOrId = String.valueOf(person.getId());
      user = UserDetail.getById(loginOrId);
    } else {
      user = UserDetail.getById(getUserIdFromLogin(loginOrId));
    }
    if (user != null) {
      if (displayAvatars()) {
        avatar = user.getSmallAvatar();
      }
      name = UserNameGenerator.toString(user, getUserId());
    }

    UserVO userVO = new UserVO(name, loginOrId, displayedRole, person.getDetail());
    userVO.setAvatar(avatar);
    return userVO;
  }

  private ChartVO buildChartUnitVO(OrganizationalChart chart) {
    ChartUnitVO chartVO = new ChartUnitVO();

    // rootOrganization
    OrganizationBox rootOrganization = new OrganizationBox();
    chartVO.setRootOrganization(rootOrganization);
    rootOrganization.setName(chart.getRoot().getName());
    rootOrganization.setDetailLinkActive(chart.getRoot().hasMembers());
    rootOrganization.setDn(chart.getRoot().getCompleteName());
    rootOrganization.setSpecificCSSClass(chart.getRoot().getSpecificCSSClass());
    rootOrganization.setDetails(chart.getRoot().getDetail());

    // Prevents user to go upper that the base DN
    if (!isRoot(chart)) {
      rootOrganization.setParentDn(chart.getRoot().getParentOu());
    }

    // Looks for specific users
    List<UserVO> leftUsers = new ArrayList<>();
    List<UserVO> rightUsers = new ArrayList<>();
    List<UserVO> mainActors = new ArrayList<>();

    for (OrganizationalPerson person : chart.getPersonns()) {
      if (person.isVisibleOnCenter()) {
        mainActors.add(organizationalPerson2UserVO(person, person.getVisibleCenterRole()));
      } else if (person.isVisibleOnLeft()) {
        leftUsers.add(organizationalPerson2UserVO(person, person.getVisibleLeftRole()));
      } else if (person.isVisibleOnRight()) {
        rightUsers.add(organizationalPerson2UserVO(person, person.getVisibleRightRole()));
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
    List<OrganizationBox> subOrganizations = new ArrayList<>();
    for (OrganizationalUnit subOrganization : chart.getUnits()) {
      OrganizationBox subUnit = new OrganizationBox();
      subUnit.setDn(subOrganization.getCompleteName());
      subUnit.setParentDn(subOrganization.getParentOu());
      subUnit.setName(subOrganization.getName());
      subUnit.setCenterLinkActive(subOrganization.hasSubUnits());
      subUnit.setDetailLinkActive(subOrganization.hasMembers());
      subUnit.setSpecificCSSClass(subOrganization.getSpecificCSSClass());
      subUnit.setDetails(subOrganization.getDetail());
      // setting main actors of subunit
      List<UserVO> subUnitMainActors = new ArrayList<>();
      for (OrganizationalPerson person : subOrganization.getMainActors()) {
        if (person.isVisibleOnCenter()) {
          subUnitMainActors.add(organizationalPerson2UserVO(person, person.getVisibleCenterRole()));
        }
      }
      subUnit.setMainActors(subUnitMainActors);
      subOrganizations.add(subUnit);
    }
    if (getSettings().getBoolean("sort.units.name", false)) {
      Collections.sort(subOrganizations, UnitComparator.comparator);
    }
    chartVO.setSubOrganizations(subOrganizations);

    return chartVO;
  }

  private boolean isRoot(OrganizationalChart chart) {
    if (isLDAP()) {
      return getConfig().getRoot().equalsIgnoreCase(chart.getRoot().getCompleteName());
    }
    return getConfig().getRoot().equalsIgnoreCase(chart.getRoot().getCompleteName());
  }

  private ChartVO buildChartPersonsVO(OrganizationalChart chart) {
    ChartPersonnVO chartVO = new ChartPersonnVO();

    // rootOrganization
    OrganizationBox rootOrganization = new OrganizationBox();
    chartVO.setRootOrganization(rootOrganization);
    rootOrganization.setName(chart.getRoot().getName());
    rootOrganization.setParentDn(chart.getRoot().getParentOu());
    rootOrganization.setSpecificCSSClass(chart.getRoot().getSpecificCSSClass());
    rootOrganization.setDetails(chart.getRoot().getDetail());

    // Looks for specific users
    List<UserVO> mainActors = new ArrayList<>();

    for (OrganizationalPerson person : chart.getPersonns()) {
      if (person.isVisibleOnCenter()) {
        mainActors.add(organizationalPerson2UserVO(person, person.getVisibleCenterRole()));
      }
    }

    if (!mainActors.isEmpty()) {
      rootOrganization.setMainActors(mainActors);
    }

    // Sort people by category
    Map<String, List<UserVO>> usersByCategory = new HashMap<>();
    for (OrganizationalPerson person : chart.getPersonns()) {
      PersonCategory category = person.getVisibleCategory();
      if (category != null) {
        List<UserVO> usersOfCurrentCategory = usersByCategory.get(category.getName());
        if (usersOfCurrentCategory == null) {
          usersOfCurrentCategory = new ArrayList<>();
          usersByCategory.put(category.getName(), usersOfCurrentCategory);
        }
        usersOfCurrentCategory.add(organizationalPerson2UserVO(person, null));
      }
    }

    // create category boxes
    List<CategoryBox> categories = new ArrayList<>();
    for (PersonCategory category : chart.getCategories()) {
      CategoryBox categoryBox = new CategoryBox();
      categoryBox.setName(category.getName());
      categoryBox.setUsers(usersByCategory.get(category.getName()));
      categories.add(categoryBox);
    }
    chartVO.setCategories(categories);

    return chartVO;
  }

  private OrganizationChartService getService() {
    return OrganizationChartService.get();
  }

  private AbstractOrganizationChartConfiguration getConfig() {
    return config;
  }

  private LdapOrganizationChartConfiguration loadLDAPConfiguration() {
    LdapOrganizationChartConfiguration config = new LdapOrganizationChartConfiguration();

    config.setServerURL(getComponentParameterValue(PARAM_SERVERURL));
    config.setInitialContextFactory(getComponentParameterValue(PARAM_CTXFACTORY));
    config.setAuthenticationMode(getComponentParameterValue(PARAM_AUTHMODE));
    config.setPrincipal(getComponentParameterValue(PARAM_PRINCIPAL));
    config.setCredentials(getComponentParameterValue(PARAM_CREDENTIAL));

    config.setRoot(getComponentParameterValue(PARAM_LDAP_ROOT));
    config.setLdapClassPerson(getComponentParameterValue(PARAM_LDAP_CLASS_PERSON));
    config.setLdapClassUnit(getComponentParameterValue(PARAM_LDAP_CLASS_UNIT));
    config.setAttUnit(getComponentParameterValue(PARAM_LDAP_ATT_UNIT));
    config.setAttName(getComponentParameterValue(PARAM_LDAP_ATT_NAME));
    // champ LDAP du titre
    config.setAttTitle(getComponentParameterValue(PARAM_LDAP_ATT_TITLE));
    // champ ldap de la description
    config.setAttDesc(getComponentParameterValue(PARAM_LDAP_ATT_DESC));
    // champ ldap del'identifiant de compte Silverpeas
    config.setLdapAttAccount(getComponentParameterValue(PARAM_LDAP_ATT_ACCOUNT));
    config.setLdapAttCSSClass(getComponentParameterValue(PARAM_LDAP_ATT_CSS));

    config.setUnitsChartCentralLabel(
        getRoles(getComponentParameterValue(PARAM_UNITSCHART_CENTRAL_LABEL)));
    config.setUnitsChartRightLabel(
        getRoles(getComponentParameterValue(PARAM_UNITSCHART_RIGHT_LABEL)));
    config
        .setUnitsChartLeftLabel(getRoles(getComponentParameterValue(PARAM_UNITSCHART_LEFT_LABEL)));

    config.setPersonnsChartCentralLabel(
        getRoles(getComponentParameterValue(PARAM_PERSONNSCHART_CENTRAL_LABEL)));
    config.setPersonnsChartCategoriesLabel(
        getRoles(getComponentParameterValue(PARAM_PERSONNSCHART_CATEGORIES_LABEL)));
    config.setUnitsChartOthersInfosKeys(
        getKeysAndLabel(getComponentParameterValue(PARAM_UNITSCHART_OTHERSINFOS_KEYS)));
    config.setPersonnsChartOthersInfosKeys(
        getKeysAndLabel(getComponentParameterValue(PARAM_PERSONNSCHART_OTHERSINFOS_KEYS)));
    config.setLdapAttActif(getComponentParameterValue(PARAM_LDAP_ATT_ACTIF));

    config.setDomainId(getComponentParameterValue(PARAM_DOMAIN_ID));

    return config;
  }

  private GroupOrganizationChartConfiguration loadGroupConfiguration() {
    GroupOrganizationChartConfiguration config = new GroupOrganizationChartConfiguration();
    config.setRoot(getComponentParameterValue(PARAM_LDAP_ROOT));
    config.setAttUnit(getComponentParameterValue(PARAM_LDAP_ATT_UNIT));
    config.setAttName(getComponentParameterValue(PARAM_LDAP_ATT_NAME));
    // champ LDAP du titre
    config.setAttTitle(getComponentParameterValue(PARAM_LDAP_ATT_TITLE));
    // champ ldap de la description
    config.setAttDesc(getComponentParameterValue(PARAM_LDAP_ATT_DESC));

    config.setUnitsChartCentralLabel(
        getRoles(getComponentParameterValue(PARAM_UNITSCHART_CENTRAL_LABEL)));
    config.setUnitsChartRightLabel(
        getRoles(getComponentParameterValue(PARAM_UNITSCHART_RIGHT_LABEL)));
    config
        .setUnitsChartLeftLabel(getRoles(getComponentParameterValue(PARAM_UNITSCHART_LEFT_LABEL)));
    config.setPersonnsChartCentralLabel(
        getRoles(getComponentParameterValue(PARAM_PERSONNSCHART_CENTRAL_LABEL)));
    config.setPersonnsChartCategoriesLabel(
        getRoles(getComponentParameterValue(PARAM_PERSONNSCHART_CATEGORIES_LABEL)));
    config.setUnitsChartOthersInfosKeys(
        getKeysAndLabel(getComponentParameterValue(PARAM_UNITSCHART_OTHERSINFOS_KEYS)));
    config.setPersonnsChartOthersInfosKeys(
        getKeysAndLabel(getComponentParameterValue(PARAM_PERSONNSCHART_OTHERSINFOS_KEYS)));
    return config;
  }

  /**
   * Transformed a flat list of Roles to a List of OrganizationalRole objects.
   * @param rolesAsString flat list of Roles ("labelRole1=keyRole1;labelRole2=keyRole2")
   * @return a List of OrganizationalRole objects
   */
  private List<OrganizationalRole> getRoles(String rolesAsString) {
    List<OrganizationalRole> listRoles = new ArrayList<>();

    if (StringUtil.isDefined(rolesAsString)) {

      String[] roles = rolesAsString.split(";");

      for (String role : roles) {
        String[] roleDetails = role.split("=");
        if (roleDetails.length == 2) {
          listRoles.add(new OrganizationalRole(roleDetails[0], roleDetails[1]));
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
    Map<String, String> map = new HashMap<>();

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
    String label = getString(attName);
    if (label == null || label.equalsIgnoreCase(attName)) {
      return null;
    } else {
      return label;
    }
  }

  public String getDomainId() {
    return getComponentParameterValue(PARAM_DOMAIN_ID);
  }

  public String getUserIdFromLogin(String login) {
    String userId = null;
    String domainId = getDomainId();

    if (StringUtil.isDefined(domainId)) {
      AdminController adminController = ServiceProvider.getService(AdminController.class);
      userId = adminController.getUserIdByLoginAndDomain(login, domainId);
    }

    return userId;
  }

  private boolean displayAvatars() {
    return getParameterValue(PARAM_AVATARS, true);
  }

  public boolean displayLabels() {
    return getParameterValue(PARAM_LABELS, true);
  }

  private boolean getParameterValue(String paramName, boolean defaultValue) {
    String value = getComponentParameterValue(paramName);
    if (!StringUtil.isDefined(value)) {
      return defaultValue;
    }
    return StringUtil.getBooleanValue(value);
  }

  public List<OrganizationBox> getBreadcrumb() {
    return breadcrumb;
  }
}