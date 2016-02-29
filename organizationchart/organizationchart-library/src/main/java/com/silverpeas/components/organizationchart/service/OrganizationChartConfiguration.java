/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package com.silverpeas.components.organizationchart.service;

import com.silverpeas.components.organizationchart.model.OrganizationalRole;

import java.util.List;
import java.util.Map;

public class OrganizationChartConfiguration {

  private String root = null;

  private String attUnit = null;
  private String attName = null;
  // champ LDAP du titre
  private String attTitle = null;
  // champ ldap de la description
  private String attDesc = null;

  private List<OrganizationalRole> unitsChartCentralLabel = null;
  private List<OrganizationalRole> unitsChartRightLabel = null;
  private List<OrganizationalRole> unitsChartLeftLabel = null;

  private List<OrganizationalRole> personnsChartCentralLabel = null;
  private List<OrganizationalRole> personnsChartCategoriesLabel = null;
  private Map<String, String> unitsChartOthersInfosKeys = null;
  private Map<String, String> personnsChartOthersInfosKeys = null;

  public OrganizationChartConfiguration() {
  }

  /**
   * @return the ldapRoot
   */
  public String getRoot() {
    return root;
  }

  /**
   * @param root the ldapRoot to set
   */
  public void setRoot(String root) {
    this.root = root;
  }

  /**
   * @return the ldapAttUnit
   */
  public String getAttUnit() {
    return attUnit;
  }

  /**
   * @param attUnit the ldapAttUnit to set
   */
  public void setAttUnit(String attUnit) {
    this.attUnit = attUnit;
  }

  /**
   * @return the ldapAttName
   */
  public String getAttName() {
    return attName;
  }

  /**
   * @param attName the ldap attName to set
   */
  public void setAttName(String attName) {
    this.attName = attName;
  }

  /**
   * @return the ldapAttTitle
   */
  public String getAttTitle() {
    return attTitle;
  }

  /**
   * @param attTitle the ldap AttTitle to set
   */
  public void setAttTitle(String attTitle) {
    this.attTitle = attTitle;
  }

  /**
   * @return the ldapAttDesc
   */
  public String getAttDesc() {
    return attDesc;
  }

  /**
   * @param attDesc the ldap AttDesc to set
   */
  public void setAttDesc(String attDesc) {
    this.attDesc = attDesc;
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
  public void setPersonnsChartCategoriesLabel(
      List<OrganizationalRole> personnsChartCategoriesLabel) {
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

}