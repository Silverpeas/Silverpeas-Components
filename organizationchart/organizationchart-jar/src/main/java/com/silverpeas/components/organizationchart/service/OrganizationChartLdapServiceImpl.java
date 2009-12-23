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
package com.silverpeas.components.organizationchart.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;

import com.silverpeas.components.organizationchart.model.OrganizationalPerson;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;

public class OrganizationChartLdapServiceImpl implements OrganizationChartService {

  public static final String PARAM_SERVERURL = "serverURL";
  public static final String PARAM_CTXFACTORY = "initialContextFactory";
  public static final String PARAM_AUTHMODE = "authenticationMode";
  public static final String PARAM_PRINCIPAL = "principal";
  public static final String PARAM_CREDENTIAL = "credentials";
  public static final String PARAM_LDAP_ROOT = "ldapRoot";
  public static final String PARAM_LDAP_CLASS_PERSON = "ldapClassPerson";
  public static final String PARAM_LDAP_CLASS_UNIT = "ldapClassUnit";
  public static final String PARAM_LDAP_ATT_UNIT = "ldapAttUnit";
  public static final String PARAM_LDAP_ATT_NAME = "ldapAttName";
  public static final String PARAM_LDAP_ATT_TITLE = "ldapAttTitle";
  public static final String PARAM_LDAP_ATT_DESC = "ldapAttDesc";
  public static final String PARAM_LDAP_ATT_TEL = "ldapAttTel";
  public static final String PARAM_RESPONSABLE_LABEL = "responsableLabel";
  public static final String PARAM_FIRSTLEVEL_LABEL = "firstLevelLabel";
  public static final String PARAM_LDAP_ATT_ACTIF = "ldapAttActif";

  private static final String LIB_TEL = "Tel : ";

  private String LDAP_ROOT;
  private String LDAP_CLASS_PERSON;
  private String LDAP_CLASS_UNIT;
  private String LDAP_ATT_UNIT;
  private String LDAP_ATT_NAME;
  private String LDAP_ATT_TITLE;
  private String LDAP_ATT_DESC;
  private String LDAP_ATT_TEL;
  private String[] RESPONSABLE_LABEL;
  private String[] FIRSTLEVEL_LABEL;
  private String LDAP_ATT_ACTIF;

  private OrganizationController controller;

  @Override
  public OrganizationalPerson[] getOrganizationChart(String componentId) {
    SilverTrace.info("organizationchart",
        "OrganizationChartLdapServiceImpl.getOrganizationChart()", "root.MSG_GEN_ENTER_METHOD",
        "componentId=" + componentId);
    Hashtable<String, String> env = initEnv(componentId);
    Map<String, OrganizationalPerson> mapPerson = new HashMap<String, OrganizationalPerson>();
    try {
      DirContext ctx = new InitialDirContext(env);
      SearchControls ctls = new SearchControls();
      ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      ctls.setCountLimit(0);

      try {
        // recupere les personnes
        NamingEnumeration<SearchResult> results = ctx.search(LDAP_ROOT,
            "(objectclass=" + LDAP_CLASS_PERSON + ")", ctls);
        SilverTrace.info("organizationchart",
            "OrganizationChartLdapServiceImpl.getOrganizationChart()", "root.MSG_GEN_PARAM_VALUE",
            "users retrieved !");
        int i = 0;
        Map<String, Integer> listResponsable = new HashMap<String, Integer>();
        while (results != null && results.hasMore()) {
          SearchResult entry = (SearchResult) results.next();
          if (entry.getName() != null && !entry.getName().isEmpty()) {
            Attributes attrs = entry.getAttributes();
            if (isActif(attrs)) {
              OrganizationalPerson pers = createPerson(i, attrs,
                  entry.getNameInNamespace(), listResponsable);
              mapPerson.put(pers.getName() + pers.getId(), pers);
              i++;
            }
          }
        }

        // recupère les services
        Map<String, String> listOu = new HashMap<String, String>();
        results = ctx.search(LDAP_ROOT, "(objectclass=" + LDAP_CLASS_UNIT + ")", ctls);
        SilverTrace.info("organizationchart",
            "OrganizationChartLdapServiceImpl.getOrganizationChart()", "root.MSG_GEN_PARAM_VALUE",
            "services retrieved !");
        while (results != null && results.hasMore()) {
          SearchResult entry = (SearchResult) results.next();
          Attributes attrs = entry.getAttributes();
          String ou = getAttributValue(attrs.get(LDAP_ATT_UNIT));
          if (!LDAP_ROOT.contains(ou)) {
            String[] ous = entry.getNameInNamespace().split(",");
            if (ous.length > 1) {
              for (int j = 0; j < ous.length; j++) {
                String[] atr = ous[j].split("=");
                if (atr.length > 1 && atr[0].equalsIgnoreCase(LDAP_ATT_UNIT) &&
                    !atr[1].equalsIgnoreCase(ou)) {
                  listOu.put(ou, atr[1]);
                  break;
                }
              }
            }
          }
        }

        // détermination du chef de chaque personne
        List<OrganizationalPerson> listService = new ArrayList<OrganizationalPerson>();
        for (Iterator<OrganizationalPerson> it = mapPerson.values().iterator(); it.hasNext();) {
          OrganizationalPerson pers = it.next();
          if (pers.isResponsable()) {
            if (listOu.containsKey(pers.getService())) {
              String ouRoot = listOu.get(pers.getService());
              i = setResponsable(pers, ouRoot, i, listOu, listResponsable, listService);
            }
          } else {
            i = setResponsable(pers, pers.getService(), i, listOu, listResponsable, listService);
          }
        }
        for (OrganizationalPerson pers : listService) {
          mapPerson.put(pers.getName() + pers.getId(), pers);
        }
      } catch (Exception ex) {
        ctx.close();
        ex.printStackTrace();
        SilverTrace.error("organizationchart",
            "OrganizationChartLdapServiceImpl.getOrganizationChart",
            "organizationChart.ldap.search.error", ex);
        return null;
      }

      ctx.close();
    } catch (NamingException e) {
      e.printStackTrace();
      SilverTrace.error("organizationchart",
          "OrganizationChartLdapServiceImpl.getOrganizationChart",
          "organizationChart.ldap.conection.error", e);
      return null;
    }

    // classement alphabétique
    OrganizationalPerson[] arrayPerson = null;
    if (mapPerson.size() > 0) {
      arrayPerson = new OrganizationalPerson[mapPerson.size()];
      List<String> keys = new ArrayList<String>(mapPerson.keySet());
      Collections.sort(keys);
      int i = 0;
      for (String id : keys) {
        OrganizationalPerson pers = mapPerson.get(id);
        arrayPerson[i++] = pers;
      }
    }
    return arrayPerson;
  }

  public Map<String, String> getOrganizationalPerson(OrganizationalPerson[] org, int id) {
    for (OrganizationalPerson pers : org) {
      if (pers.getId() == id) {
        Map<String, String> details = pers.getDetail();
        return details;
      }
    }
    return null;
  }

  private OrganizationalPerson createPerson(int id, Attributes attrs, String dn,
      Map<String, Integer> listResponsable) {
    Attribute cn = attrs.get(LDAP_ATT_NAME);
    Attribute title = attrs.get(LDAP_ATT_TITLE);
    String service = "";
    Attribute ou = attrs.get(LDAP_ATT_UNIT);
    if (ou == null) {
      // on parse le dn pour récupérer l'ou
      String[] ous = dn.split(",");
      if (ous.length > 1) {
        for (int j = 0; j < ous.length; j++) {
          String[] atr = ous[j].split("=");
          if (atr.length > 1 && atr[0].equalsIgnoreCase(LDAP_ATT_UNIT)) {
            service = atr[1];
            break;
          }
        }
      }
    } else
      service = getAttributValue(ou);

    String fonction = getAttributValue(title);
    boolean responsable = false;
    for (String label : RESPONSABLE_LABEL) {
      if (fonction != null && label != null && fonction.toLowerCase().contains(label)) {
        responsable = true;
        fonction = service;
        listResponsable.put(fonction, id);
        break;
      }
    }
    Attribute desc = attrs.get(LDAP_ATT_DESC);
    Attribute tel = attrs.get(LDAP_ATT_TEL);
    OrganizationalPerson pers =
        new OrganizationalPerson(id, -1,
            getAttributValue(cn), fonction, getLibTel(tel), getAttributValue(desc), service,
            responsable);
    for (String label : FIRSTLEVEL_LABEL) {
      if (fonction != null && label != null && !label.isEmpty() &&
          fonction.toLowerCase().contains(label)) {
        pers.setFirstLevel(true);
        break;
      }
    }
    NamingEnumeration<?> attributs = attrs.getAll();
    Map<String, String> details = new HashMap<String, String>();
    try {
      while (attributs.hasMore()) {
        Attribute att = (Attribute) attributs.next();
        if (att != null && !att.getID().equalsIgnoreCase("objectClass")) {
          String detail = "";
          if (att.size() > 1) {
            NamingEnumeration<?> vals = att.getAll();
            while (vals.hasMore()) {
              String val = (String) vals.next();
              detail += val + ", ";
            }
            detail = detail.substring(0, detail.length() - 2);
          } else
            detail = getAttributValue(att);
          details.put(att.getID(), detail);
        }
      }
    } catch (NamingException e) {
      SilverTrace.warn("organizationchart", "OrganizationChartLdapServiceImpl.createPerson",
          "organizationChart.ldap.search.error", e);
    }
    pers.setDetail(details);
    return pers;
  }

  private int setResponsable(OrganizationalPerson pers, String service,
      int i, Map<String, String> listOu, Map<String, Integer> listResponsable,
      List<OrganizationalPerson> listService) {
    if (listResponsable.containsKey(service))
      pers.setParentId(listResponsable.get(service));
    else { // pas de responsable
      OrganizationalPerson respService = new OrganizationalPerson(i, -1,
          service, "", "", "", service, true);
      respService.setDetailed(false);
      listResponsable.put(service, i);
      pers.setParentId(i);
      i++;
      if (listOu.containsKey(service))
        i =
            setResponsable(respService, listOu.get(service), i, listOu, listResponsable,
                listService);
      listService.add(respService);
      return i;
    }
    return i;
  }

  private String getAttributValue(Attribute att) {
    if (att == null)
      return null;
    try {
      String val = (String) att.get();
      if (val == null || val.isEmpty())
        return null;
      return val;
    } catch (Exception e) {
      return null;
    }
  }

  private String getLibTel(Attribute telAtt) {
    String tel = getAttributValue(telAtt);
    if (tel == null)
      return "";
    return LIB_TEL + tel;
  }

  private boolean isActif(Attributes attrs) {
    if (LDAP_ATT_ACTIF == null)
      return true;
    String actif = getAttributValue(attrs.get(LDAP_ATT_ACTIF));
    if (actif == null || actif.equalsIgnoreCase("false"))
      return true;
    return false;
  }

  private Hashtable<String, String> initEnv(String componentId) {
    SilverTrace.info("organizationchart", "OrganizationChartLdapServiceImpl.initEnv()",
        "root.MSG_GEN_ENTER_METHOD", "componentId=" + componentId);
    Hashtable<String, String> env = new Hashtable<String, String>();
    // initialise connexion ldap
    String jndiURL = controller.getComponentParameterValue(componentId, PARAM_SERVERURL);// "ldap://localhost:389/";
    String initialContextFactory =
        controller.getComponentParameterValue(componentId, PARAM_CTXFACTORY);// "com.sun.jndi.ldap.LdapCtxFactory";
    String authenticationMode = controller.getComponentParameterValue(componentId, PARAM_AUTHMODE);// "simple";
    String principal = controller.getComponentParameterValue(componentId, PARAM_PRINCIPAL);// "cn=Manager,dc=mondomain,dc=com";
    String credentials = controller.getComponentParameterValue(componentId, PARAM_CREDENTIAL);
    String contextReferral = "ignore";

    env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
    env.put(Context.PROVIDER_URL, jndiURL);
    env.put(Context.SECURITY_AUTHENTICATION, authenticationMode);
    env.put(Context.SECURITY_PRINCIPAL, principal);
    env.put(Context.SECURITY_CREDENTIALS, credentials);
    env.put(Context.REFERRAL, contextReferral);

    // initialisation des attribut LDAP pour la recherche et l'affichage
    LDAP_ROOT = controller.getComponentParameterValue(componentId, PARAM_LDAP_ROOT);
    LDAP_CLASS_PERSON = controller.getComponentParameterValue(componentId, PARAM_LDAP_CLASS_PERSON);// "organizationalPerson";
    LDAP_CLASS_UNIT = controller.getComponentParameterValue(componentId, PARAM_LDAP_CLASS_UNIT);// "organizationalUnit";
    LDAP_ATT_UNIT = controller.getComponentParameterValue(componentId, PARAM_LDAP_ATT_UNIT);// "ou";
    LDAP_ATT_NAME = controller.getComponentParameterValue(componentId, PARAM_LDAP_ATT_NAME);// "cn";
    LDAP_ATT_TITLE = controller.getComponentParameterValue(componentId, PARAM_LDAP_ATT_TITLE);// "title";
    LDAP_ATT_DESC = controller.getComponentParameterValue(componentId, PARAM_LDAP_ATT_DESC);// "description";
    LDAP_ATT_TEL = controller.getComponentParameterValue(componentId, PARAM_LDAP_ATT_TEL);// telephoneNumber
    String resp = controller.getComponentParameterValue(componentId, PARAM_RESPONSABLE_LABEL);
    RESPONSABLE_LABEL = resp.toLowerCase().split(",");
    String firstLevel = controller.getComponentParameterValue(componentId, PARAM_FIRSTLEVEL_LABEL);
    FIRSTLEVEL_LABEL = firstLevel.toLowerCase().split(",");
    LDAP_ATT_ACTIF = controller.getComponentParameterValue(componentId, PARAM_LDAP_ATT_ACTIF);
    if (LDAP_ATT_ACTIF == null || LDAP_ATT_ACTIF.isEmpty())
      LDAP_ATT_ACTIF = null;
    SilverTrace.info("organizationchart", "OrganizationChartLdapServiceImpl.initEnv()",
        "root.MSG_GEN_EXIT_METHOD");
    return env;
  }

  public void setController(OrganizationController controller) {
    this.controller = controller;
  }
}
