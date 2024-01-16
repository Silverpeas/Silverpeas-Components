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
package org.silverpeas.components.organizationchart.service;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;

import org.silverpeas.core.util.StringUtil;

public class LdapOrganizationChartConfiguration extends AbstractOrganizationChartConfiguration {
  private Map<String, String> env = null;

  private String ldapClassPerson = null;
  private String ldapClassUnit = null;
  // champ ldap de l'identifiant de compte Silverpeas
  private String ldapAttAccount = null;
  private String ldapAttCSSClass = null;

  private String ldapAttActif = null;
  private String domainId = null;

  public LdapOrganizationChartConfiguration() {
    this.env = new Hashtable<>();
    env.put(Context.REFERRAL, "ignore");
  }

  /**
   * @return the env
   */
  Map<String, String> getEnv() {
    return env;
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
   * @return the ldapClassPerson
   */
  String getLdapClassPerson() {
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
  String getLdapClassUnit() {
    return ldapClassUnit;
  }

  /**
   * @param ldapClassUnit the ldapClassUnit to set
   */
  public void setLdapClassUnit(String ldapClassUnit) {
    this.ldapClassUnit = ldapClassUnit;
  }

  /**
   * @return the ldapAttAccount
   */
  String getLdapAttAccount() {
    return ldapAttAccount;
  }

  /**
   * @param ldapAttAccount the ldapAttAccount to set
   */
  public void setLdapAttAccount(String ldapAttAccount) {
    this.ldapAttAccount = ldapAttAccount;
  }

  /**
   * @return the ldapAttActif
   */
  String getLdapAttActif() {
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

  String getLdapAttCSSClass() {
    return ldapAttCSSClass;
  }

}