/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.silverpeas.components.organizationchart.service;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;

import com.silverpeas.components.organizationchart.model.OrganizationalRole;
import com.silverpeas.util.StringUtil;

public class OrganizationChartConfiguration {
  Hashtable<String, String> env = null;

  private String ldapRoot = null;
  private String ldapClassPerson = null;
  private String ldapClassUnit = null;
  private String ldapAttUnit = null;
  private String ldapAttName = null;
  private String ldapAttTitle = null; // champ LDAP du titre
  private String ldapAttDesc = null; // champ ldap de la description
  private String ldapAttAccount = null; // champ ldap de l'identifiant de compte Silverpeas
  private String ldapAttCSSClass = null;

  private List<OrganizationalRole> unitsChartCentralLabel = null;
  private List<OrganizationalRole> unitsChartRightLabel = null;
  private List<OrganizationalRole> unitsChartLeftLabel = null;

  private List<OrganizationalRole> personnsChartCentralLabel = null;
  private List<OrganizationalRole> personnsChartCategoriesLabel = null;
  private Map<String, String> unitsChartOthersInfosKeys = null;
  private Map<String, String> personnsChartOthersInfosKeys = null;
  private String ldapAttActif = null;
  private String domainId = null;

  public OrganizationChartConfiguration() {
    this.env = new Hashtable<String, String>();
    env.put(Context.REFERRAL, "ignore");
  }

  /**
   * @return the env
   */
  public Hashtable<String, String> getEnv() {
    return env;
  }

  /**
   * @param env the env to set
   */
  public void setEnv(Hashtable<String, String> env) {
    this.env = env;
  }

  /**
   * @return the domainId
   */
  public String getDomainId() {
    return domainId;
  }

  /**
   * @param domainId the domainId to set
   */
  public void setDomainId(String domainId) {
    this.domainId = domainId;
  }

  /**
   * @param serverURL the serverURL to set
   */
  public void setServerURL(String serverURL) {
    env.put(Context.PROVIDER_URL, serverURL);
  }

  /**
   * @param initialContextFactory the initialContextFactory to set
   */
  public void setInitialContextFactory(String initialContextFactory) {
    this.env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
  }

  /**
   * @param authenticationMode the authenticationMode to set
   */
  public void setAuthenticationMode(String authenticationMode) {
    env.put(Context.SECURITY_AUTHENTICATION, authenticationMode);
  }

  /**
   * @param principal the principal to set
   */
  public void setPrincipal(String principal) {
    env.put(Context.SECURITY_PRINCIPAL, principal);
  }

  /**
   * @param credentials the credentials to set
   */
  public void setCredentials(String credentials) {
    env.put(Context.SECURITY_CREDENTIALS, credentials);
  }

  /**
   * @return the ldapRoot
   */
  public String getLdapRoot() {
    return ldapRoot;
  }

  /**
   * @param ldapRoot the ldapRoot to set
   */
  public void setLdapRoot(String ldapRoot) {
    this.ldapRoot = ldapRoot;
  }

  /**
   * @return the ldapClassPerson
   */
  public String getLdapClassPerson() {
    return ldapClassPerson;
  }

  /**
   * @param ldapClassPerson the ldapClassPerson to set
   */
  public void setLdapClassPerson(String ldapClassPerson) {
    this.ldapClassPerson = ldapClassPerson;
  }

  /**
   * @return the ldapClassUnit
   */
  public String getLdapClassUnit() {
    return ldapClassUnit;
  }

  /**
   * @param ldapClassUnit the ldapClassUnit to set
   */
  public void setLdapClassUnit(String ldapClassUnit) {
    this.ldapClassUnit = ldapClassUnit;
  }

  /**
   * @return the ldapAttUnit
   */
  public String getLdapAttUnit() {
    return ldapAttUnit;
  }

  /**
   * @param ldapAttUnit the ldapAttUnit to set
   */
  public void setLdapAttUnit(String ldapAttUnit) {
    this.ldapAttUnit = ldapAttUnit;
  }

  /**
   * @return the ldapAttName
   */
  public String getLdapAttName() {
    return ldapAttName;
  }

  /**
   * @param ldapAttName the ldapAttName to set
   */
  public void setLdapAttName(String ldapAttName) {
    this.ldapAttName = ldapAttName;
  }

  /**
   * @return the ldapAttTitle
   */
  public String getLdapAttTitle() {
    return ldapAttTitle;
  }

  /**
   * @param ldapAttTitle the ldapAttTitle to set
   */
  public void setLdapAttTitle(String ldapAttTitle) {
    this.ldapAttTitle = ldapAttTitle;
  }

  /**
   * @return the ldapAttDesc
   */
  public String getLdapAttDesc() {
    return ldapAttDesc;
  }

  /**
   * @param ldapAttDesc the ldapAttDesc to set
   */
  public void setLdapAttDesc(String ldapAttDesc) {
    this.ldapAttDesc = ldapAttDesc;
  }

  /**
   * @return the ldapAttAccount
   */
  public String getLdapAttAccount() {
    return ldapAttAccount;
  }

  /**
   * @param ldapAttAccount the ldapAttAccount to set
   */
  public void setLdapAttAccount(String ldapAttAccount) {
    this.ldapAttAccount = ldapAttAccount;
  }

  /**
   * @return the unitsChartCentralLabel
   */
  public List<OrganizationalRole> getUnitsChartCentralLabel() {
    return unitsChartCentralLabel;
  }

  /**
   * @param unitsChartCentralLabel the unitsChartCentralLabel to set
   */
  public void setUnitsChartCentralLabel(List<OrganizationalRole> unitsChartCentralLabel) {
    this.unitsChartCentralLabel = unitsChartCentralLabel;
  }

  /**
   * @return the unitsChartRightLabel
   */
  public List<OrganizationalRole> getUnitsChartRightLabel() {
    return unitsChartRightLabel;
  }

  /**
   * @param unitsChartRightLabel the unitsChartRightLabel to set
   */
  public void setUnitsChartRightLabel(List<OrganizationalRole> unitsChartRightLabel) {
    this.unitsChartRightLabel = unitsChartRightLabel;
  }

  /**
   * @return the unitsChartLeftLabel
   */
  public List<OrganizationalRole> getUnitsChartLeftLabel() {
    return unitsChartLeftLabel;
  }

  /**
   * @param unitsChartLeftLabel the unitsChartLeftLabel to set
   */
  public void setUnitsChartLeftLabel(List<OrganizationalRole> unitsChartLeftLabel) {
    this.unitsChartLeftLabel = unitsChartLeftLabel;
  }

  /**
   * @return the personnsChartCentralLabel
   */
  public List<OrganizationalRole> getPersonnsChartCentralLabel() {
    return personnsChartCentralLabel;
  }

  /**
   * @param personnsChartCentralLabel the personnsChartCentralLabel to set
   */
  public void setPersonnsChartCentralLabel(List<OrganizationalRole> personnsChartCentralLabel) {
    this.personnsChartCentralLabel = personnsChartCentralLabel;
  }

  /**
   * @return the personnsChartCategoriesLabel
   */
  public List<OrganizationalRole> getPersonnsChartCategoriesLabel() {
    return personnsChartCategoriesLabel;
  }

  /**
   * @param personnsChartCategoriesLabel the personnsChartCategoriesLabel to set
   */
  public void setPersonnsChartCategoriesLabel(List<OrganizationalRole> personnsChartCategoriesLabel) {
    this.personnsChartCategoriesLabel = personnsChartCategoriesLabel;
  }

  /**
   * @return the unitsChartOthersInfosKeys
   */
  public Map<String, String> getUnitsChartOthersInfosKeys() {
    return unitsChartOthersInfosKeys;
  }

  /**
   * @param unitsChartOthersInfosKeys the unitsChartOthersInfosKeys to set
   */
  public void setUnitsChartOthersInfosKeys(Map<String, String> unitsChartOthersInfosKeys) {
    this.unitsChartOthersInfosKeys = unitsChartOthersInfosKeys;
  }

  /**
   * @return the personnsChartOthersInfosKeys
   */
  public Map<String, String> getPersonnsChartOthersInfosKeys() {
    return personnsChartOthersInfosKeys;
  }

  /**
   * @param personnsChartOthersInfosKeys the personnsChartOthersInfosKeys to set
   */
  public void setPersonnsChartOthersInfosKeys(Map<String, String> personnsChartOthersInfosKeys) {
    this.personnsChartOthersInfosKeys = personnsChartOthersInfosKeys;
  }

  /**
   * @return the ldapAttActif
   */
  public String getLdapAttActif() {
    return ldapAttActif;
  }

  /**
   * @param ldapAttActif the ldapAttActif to set
   */
  public void setLdapAttActif(String ldapAttActif) {
    this.ldapAttActif = StringUtil.isDefined(ldapAttActif) ? ldapAttActif : null;
  }

  public void setLdapAttCSSClass(String ldapAttCSSClass) {
    this.ldapAttCSSClass = StringUtil.isDefined(ldapAttCSSClass) ? ldapAttCSSClass : null;
  }

  public String getLdapAttCSSClass() {
    return ldapAttCSSClass;
  }
  
}