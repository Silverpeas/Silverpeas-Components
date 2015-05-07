/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.components.organizationchart.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import com.silverpeas.components.organizationchart.model.OrganizationalChart;
import com.silverpeas.components.organizationchart.model.OrganizationalChartType;
import com.silverpeas.components.organizationchart.model.OrganizationalPerson;
import com.silverpeas.components.organizationchart.model.OrganizationalPersonComparator;
import com.silverpeas.components.organizationchart.model.OrganizationalUnit;
import com.silverpeas.components.organizationchart.model.PersonCategory;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class OrganizationChartLdapServiceImpl extends AbstractOrganizationChartServiceImpl
    implements OrganizationChartService {

  private OrganizationChartLDAPConfiguration config = null;

  public void configure(OrganizationChartLDAPConfiguration config) {
    this.config = config;
  }

  @Override
  public void configure(OrganizationChartConfiguration config) {
    throw new UnsupportedOperationException();
  }

  @Override
  public OrganizationalChart getOrganizationChart(String baseOu, OrganizationalChartType type) {
    SilverTrace.info("organizationchart",
        "OrganizationChartLdapServiceImpl.getOrganizationChart()", "root.MSG_GEN_ENTER_METHOD",
        "baseOu = " + baseOu + ", type = " + type);

    Hashtable<String, String> env = config.getEnv();

    List<OrganizationalPerson> ouMembers = null;
    List<OrganizationalUnit> units = null;

    // beginning node of the search
    String rootOu = (StringUtil.isDefined(baseOu)) ? baseOu : config.getRoot();

    // Parent definition = top of the chart
    String[] ous = rootOu.split(",");
    String[] firstOu = ous[0].split("=");
    OrganizationalUnit parent = new OrganizationalUnit(firstOu[1], rootOu);
    setParents(parent, config.getAttUnit(), rootOu);

    DirContext ctx = null;
    try {
      ctx = new InitialDirContext(env);
      SearchControls ctls = new SearchControls();
      ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
      ctls.setCountLimit(0);

      if (StringUtil.isDefined(config.getLdapAttCSSClass())) {
        OrganizationalUnit root = getOrganizationalUnit(ctx, ctls, rootOu);
        String cssClass = getSpecificCSSClass(ctx, ctls, root);
        parent.setSpecificCSSClass(cssClass);
        parent.setDetail(root.getDetail());
      }

      // get organization unit members
      ouMembers = getOUMembers(ctx, ctls, rootOu, type);
      parent.setHasMembers(ouMembers != null && ouMembers.size() > 1);

      // get sub organization units
      if (type == OrganizationalChartType.TYPE_UNITCHART) {
        units = getSubOrganizationUnits(ctx, ctls, rootOu);
      }

    } catch (NamingException e) {
      SilverTrace.error("organizationchart",
          "OrganizationChartLdapServiceImpl.getOrganizationChart",
          "organizationChart.ldap.closing.context.error", e);
      return null;
    } finally {
      if (ctx != null) {
        try {
          ctx.close();
        } catch (NamingException e) {
          SilverTrace.error("organizationchart",
              "OrganizationChartLdapServiceImpl.getOrganizationChart",
              "organizationChart.ldap.conection.error", e);
        }
      }
    }

    boolean silverpeasUserLinkable = StringUtil.isDefined(config.getDomainId());

    OrganizationalChart chart = null;
    switch (type) {
      case TYPE_UNITCHART:
        chart = new OrganizationalChart(parent, units, ouMembers, silverpeasUserLinkable);
        break;

      default:
        Set<PersonCategory> categories = getCategories(ouMembers);
        chart = new OrganizationalChart(parent, ouMembers, categories, silverpeasUserLinkable);
        break;
    }

    return chart;
  }

  private boolean isRoot(String ou) {
    return !StringUtil.isDefined(ou) || ou.equalsIgnoreCase(config.getRoot());
  }

  private void setParents(OrganizationalUnit unit, String ou, String baseOu) {
    if (isRoot(baseOu)) {
      return;
    }

    String parentName = null;

    String[] ous = unit.getCompleteName().split(",");
    if (ous.length > 1) {
      String[] values = ous[1].split("=");
      if (values != null && values.length > 1 && values[0].equalsIgnoreCase(ou)) {
        parentName = values[1];
      }
    }
    unit.setParentName(parentName);

    if (parentName != null) {
      // there is a parent so define is path for return to top level OU
      int indexStart =
          unit.getCompleteName().toUpperCase()
              .lastIndexOf(ou.toUpperCase() + "=" + parentName.toUpperCase());
      String parentOu = unit.getCompleteName().substring(indexStart);
      unit.setParentOu(parentOu);
    }
  }

  private OrganizationalUnit getParentOU(String ou) {
    SilverTrace.info("organizationchart",
        "OrganizationChartLdapServiceImpl.getParentOU()", "root.MSG_GEN_ENTER_METHOD", "ou = "+ou);

    String parentOu = ou.substring(ou.indexOf(",")+1); //OU=DGA2,OU=DGS,OU=Ailleurs
    String parentName = parentOu.substring(3, parentOu.indexOf(",")); // DGA2

    SilverTrace.info("organizationchart",
        "OrganizationChartLdapServiceImpl.getParentOU()", "root.MSG_GEN_EXIT_METHOD",
        "parentOu = " + parentOu + ", parentName = " + parentName);
    return new OrganizationalUnit(parentName, parentOu);
  }

  /**
   * Get person list for a given OU.
   *
   * @param ctx Search context
   * @param ctls Search controls
   * @param rootOu rootOu
   * @param type type
   * @return a List of OrganizationalPerson objects.
   * @throws NamingException
   */
  private List<OrganizationalPerson> getOUMembers(DirContext ctx,
      SearchControls ctls, String rootOu,
      OrganizationalChartType type) throws NamingException {

    List<OrganizationalPerson> personList = new ArrayList<OrganizationalPerson>();

    NamingEnumeration<SearchResult> results = ctx.search(rootOu, "(objectclass=" + config
        .getLdapClassPerson() + ")", ctls);
    SilverTrace.info("organizationchart",
        "OrganizationChartLdapServiceImpl.getOrganizationChart()", "root.MSG_GEN_PARAM_VALUE",
        "users retrieved !");

    int i = 0;

    while (results != null && results.hasMore()) {
      SearchResult entry = results.next();
      if (StringUtil.isDefined(entry.getName())) {
        Attributes attrs = entry.getAttributes();
        if (isUserActive(config.getLdapAttActif(), attrs)) {
          OrganizationalPerson person = loadOrganizationalPerson(i, attrs, entry
              .getNameInNamespace(), type);
          personList.add(person);
          i++;
        }
      }
    }
    Collections.sort(personList, new OrganizationalPersonComparator());
    return personList;
  }

  /**
   * Get sub organization units of a given OU.
   *
   * @param ctx Search context
   * @param ctls Search controls
   * @param rootOu rootOu
   * @return a List of OrganizationalUnit objects.
   * @throws NamingException
   */
  private List<OrganizationalUnit> getSubOrganizationUnits(DirContext ctx,
      SearchControls ctls, String rootOu) throws NamingException {

    ArrayList<OrganizationalUnit> units = new ArrayList<OrganizationalUnit>();

    NamingEnumeration<SearchResult> results = ctx.search(rootOu, "(objectclass=" + config
        .getLdapClassUnit() + ")", ctls);

    SilverTrace.info("organizationchart",
        "OrganizationChartLdapServiceImpl.getOrganizationChart()", "root.MSG_GEN_PARAM_VALUE",
        "services retrieved !");

    while (results != null && results.hasMore()) {
      SearchResult entry = (SearchResult) results.next();
      Attributes attrs = entry.getAttributes();
      String ou = getFirstAttributeValue(attrs.get(config.getAttUnit()));
      String completeOu = entry.getNameInNamespace();
      OrganizationalUnit unit = new OrganizationalUnit(ou, completeOu);
      setParents(unit, config.getAttUnit(), completeOu);
      // build details map
      Map<String, String> attributesToReturn = config.getUnitsChartOthersInfosKeys();
      Map<String, String> details = getDetails(attributesToReturn, attrs);
      unit.setDetail(details);
      if (StringUtil.isDefined(config.getLdapAttCSSClass())) {
        unit.setSpecificCSSClass(getFirstAttributeValue(attrs.get(config.getLdapAttCSSClass())));
      }
      units.add(unit);
    }

    for (OrganizationalUnit unit : units) {
      boolean hasSubOrganizations = hasResults(unit.getCompleteName(), "(objectclass=" + config
          .getLdapClassUnit() + ")",
          ctx, ctls);
      unit.setHasSubUnits(hasSubOrganizations);

      try {
        // set responsible of subunit
        List<OrganizationalPerson> users = getOUMembers(ctx, ctls, unit.getCompleteName(),
            OrganizationalChartType.TYPE_UNITCHART);
        List<OrganizationalPerson> mainActors = getMainActors(users);
        unit.setMainActors(mainActors);

        // check if subunit have more people
        unit.setHasMembers(users.size() > mainActors.size());

        // set css class
        if (StringUtil.isDefined(config.getLdapAttCSSClass())) {
          String cssClass = getSpecificCSSClass(ctx, ctls, unit);
          unit.setSpecificCSSClass(cssClass);
        }
      } catch (Exception e) {
        SilverTrace.error("organizationchart",
            "OrganizationChartLdapServiceImpl.getSubOrganizationUnits",
            "organizationchart.ERROR_GET_SUBUNIT_MAINACTORS", "dn : " + unit.getCompleteName(), e);
      }

    }

    return units;
  }

  private OrganizationalUnit getOrganizationalUnit(DirContext ctx,
      SearchControls ctls, String rootOu) throws NamingException {
    SilverTrace.info("organizationchart",
        "OrganizationChartLdapServiceImpl.getOrganizationalUnit()", "root.MSG_GEN_ENTER_METHOD",
        "rootOu = " + rootOu);
    Attributes attrs = ctx.getAttributes(rootOu);

    SilverTrace.info("organizationchart",
        "OrganizationChartLdapServiceImpl.getOrganizationalUnit()", "root.MSG_GEN_PARAM_VALUE",
        "OU retrieved !");

    String ou = getFirstAttributeValue(attrs.get(config.getAttUnit()));
    OrganizationalUnit unit = new OrganizationalUnit(ou, rootOu);
    SilverTrace.info("organizationchart",
        "OrganizationChartLdapServiceImpl.getOrganizationalUnit()", "root.MSG_GEN_PARAM_VALUE",
        "ou = " + ou);
    if (StringUtil.isDefined(config.getLdapAttCSSClass())) {
      unit.setSpecificCSSClass(getFirstAttributeValue(attrs.get(config.getLdapAttCSSClass())));
    }

    // build details map
    Map<String, String> attributesToReturn = config.getUnitsChartOthersInfosKeys();
    Map<String, String> details = getDetails(attributesToReturn, attrs);
    unit.setDetail(details);

    return unit;
  }

  private String getSpecificCSSClass(DirContext ctx, SearchControls ctls, OrganizationalUnit unit)
      throws NamingException {
    String cssClass = unit.getSpecificCSSClass();
    SilverTrace.info("organizationchart",
        "OrganizationChartLdapServiceImpl.getSpecificCSSClass()", "root.MSG_GEN_PARAM_VALUE",
        "ou = " + unit.getCompleteName() + ", cssClass = " + cssClass);
    if (!StringUtil.isDefined(cssClass) && !isRoot(unit.getCompleteName())) {
      // get specific CSS class on parents
      String ou = unit.getCompleteName();
      while (StringUtil.isDefined(ou) && !StringUtil.isDefined(cssClass) && !isRoot(ou)) {
        OrganizationalUnit parent = getParentOU(ou);
        if (parent.getCompleteName() != null) {
          OrganizationalUnit fullParent = getOrganizationalUnit(ctx, ctls, parent.getCompleteName());
          if (fullParent != null) {
            cssClass = fullParent.getSpecificCSSClass();
            ou = parent.getCompleteName();
          }
        } else {
          ou = null;
        }
      }
    }
    SilverTrace.info("organizationchart",
        "OrganizationChartLdapServiceImpl.getSpecificCSSClass()", "root.MSG_GEN_EXIT_METHOD",
        "ou = " + unit.getCompleteName() + ", cssClass = " + cssClass);
    return cssClass;
  }

  /**
   * Launch a search and return true if there are results.
   *
   * @param baseDN baseDN for search
   * @param filter search filter
   * @param ctx search context
   * @param ctls search controls
   * @return true is at least one result is found.
   * @throws NamingException
   */
  private boolean hasResults(String baseDN, String filter, DirContext ctx, SearchControls ctls)
      throws NamingException {
    NamingEnumeration<SearchResult> results = getResults(baseDN, filter, ctx, ctls);
    return (results != null && results.hasMoreElements());
  }

  private NamingEnumeration<SearchResult> getResults(String baseDN, String filter, DirContext ctx,
      SearchControls ctls) throws NamingException {
    return ctx.search(baseDN, filter, ctls);
  }

  /**
   * Build a OrganizationalPerson object by retrieving attributes values
   *
   * @param id person Id
   * @param attrs ldap attributes
   * @param dn dn
   * @param type organizationChart type
   * @return a loaded OrganizationalPerson object
   */
  private OrganizationalPerson loadOrganizationalPerson(int id,
      Attributes attrs, String dn, OrganizationalChartType type) {

    // Full Name
    String fullName = getFirstAttributeValue(attrs.get(config.getAttName()));

    // Function
    String function = getFirstAttributeValue(attrs.get(config.getAttTitle()));

    // Description
    String description = getFirstAttributeValue(attrs.get(config.getAttDesc()));

    // login
    String login = getFirstAttributeValue(attrs.get(config.getLdapAttAccount()));

    // service
    String service = getFirstAttributeValue(attrs.get(config.getAttUnit()));
    try {
      if (service == null) {
        LdapName ldapName = new LdapName(dn);
        for (Rdn rdn : ldapName.getRdns()) {
          if (rdn.getType().equalsIgnoreCase("ou")) {
            service = rdn.getValue().toString();
            break;
          }
        }
      }
    } catch (InvalidNameException e1) {
      SilverTrace.warn("organizationchart",
          "OrganizationChartLdapServiceImpl.getOrganizationalPersonDetails",
          "organizationchart.ERROR_GET_SERVICE", "dn : " + dn, e1);
    }

    // build OrganizationalPerson object
    OrganizationalPerson person = new OrganizationalPerson(id, -1, fullName, function, description,
        service, login);

    // Determines attributes to be returned
    Map<String, String> attributesToReturn = null;
    switch (type) {
      case TYPE_UNITCHART:
        attributesToReturn = config.getUnitsChartOthersInfosKeys();
        break;

      default:
        attributesToReturn = config.getPersonnsChartOthersInfosKeys();
    }

    Map<String, String> details = getDetails(attributesToReturn, attrs);
    person.setDetail(details);

    // defined the boxes with personns inside
    if (function != null) {
      switch (type) {
        case TYPE_UNITCHART:
          defineUnitChartRoles(person, function, config);
          break;

        default:
          defineDetailledChartRoles(person, function, config);
          break;
      }
    } else {
      person.setVisibleCategory(new PersonCategory("Personnel"));
    }

    return person;
  }

  private Map<String, String> getDetails(Map<String, String> attributesToReturn, Attributes attrs) {
    Map<String, String> details = new HashMap<String, String>();
    // get only the attributes defined in the organizationChart parameters
    for (Entry<String, String> attribute : attributesToReturn.entrySet()) {
      Attribute att = attrs.get(attribute.getKey());
      if (att != null) {
        try {
          String detail = "";
          if (att.size() > 1) {
            NamingEnumeration<?> vals = att.getAll();
            while (vals.hasMore()) {
              String val = (String) vals.next();
              detail += val + ", ";
            }
            detail = detail.substring(0, detail.length() - 2);
          } else {
            detail = getFirstAttributeValue(att);
          }

          // convert characters
          detail = escapeHTML(detail);

          details.put(attribute.getValue(), detail);
        } catch (NamingException e) {
          SilverTrace.warn("organizationchart",
              "OrganizationChartLdapServiceImpl.getDetails",
              "organizationchart.ERROR_GET_DETAIL", "attribute : " + attribute.getKey(), e);
        }
      }
    }
    return details;
  }

  private String escapeHTML(String s) {
    StringBuffer sb = new StringBuffer();
    int n = s.length();
    for (int i = 0; i < n; i++) {
      char c = s.charAt(i);
      switch (c) {
        case '<':
          sb.append("&lt;");
          break;
        case '>':
          sb.append("&gt;");
          break;
        case '&':
          sb.append("&amp;");
          break;
        case '\'':
          sb.append("&apos;");
          break;
        case '"':
          sb.append("&quot;");
          break;
        case '/':
          sb.append("&#47;");
          break;
        case '\\':
          sb.append("&#92;");
          break;
        case ' ':
          sb.append("&nbsp;");
          break;

        default:
          sb.append(c);
          break;
      }
    }
    return sb.toString();
  }

  /**
   * Get first Ldap attribute value.
   *
   * @param att Ldap attribute
   * @return the first attribute value as String
   */
  private String getFirstAttributeValue(Attribute att) {
    String result = null;

    try {
      if (att != null) {
        String val = (String) att.get();
        if (StringUtil.isDefined(val)) {
          result = val;
        }
      }
    } catch (NamingException e1) {
      SilverTrace.warn("organizationchart",
          "OrganizationChartLdapServiceImpl.getFirstAttributeValue",
          "organizationchart.ERROR_GET_ATTRIBUTE_VALUE", "attribute : " + att.getID(), e1);
    }

    return result;
  }

  /**
   * Check if a user is active
   *
   * @param activeAttribute ldap attribute to check f user is active
   * @param attrs ldap attributes
   * @return true is user is active or no activeAttribute is specified.
   */
  private boolean isUserActive(String activeAttribute, Attributes attrs) {
    // if no ldap attribute specified in configuration, let's consider user is always valid
    if (activeAttribute == null) {
      return true;
    }

    String actif = getFirstAttributeValue(attrs.get(activeAttribute));
    return StringUtil.getBooleanValue(actif);
  }

}
