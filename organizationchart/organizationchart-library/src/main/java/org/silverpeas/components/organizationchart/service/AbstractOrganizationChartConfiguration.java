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

import org.silverpeas.components.organizationchart.model.OrganizationalRole;

import java.util.List;
import java.util.Map;

public abstract class AbstractOrganizationChartConfiguration {

  private String root = null;

  private String attUnit = null;
  private String attName = null;
  // title LDAP field
  private String attTitle = null;
  // description LDAP field
  private String attDesc = null;

  private List<OrganizationalRole> unitsChartCentralLabel = null;
  private List<OrganizationalRole> unitsChartRightLabel = null;
  private List<OrganizationalRole> unitsChartLeftLabel = null;

  private List<OrganizationalRole> personsChartCentralLabel = null;
  private List<OrganizationalRole> personsChartCategoriesLabel = null;
  private Map<String, String> unitsChartOthersInfosKeys = null;
  private Map<String, String> personsChartOthersInfosKeys = null;

  AbstractOrganizationChartConfiguration() {
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
   * @return the personsChartCentralLabel
   */
  public List<OrganizationalRole> getPersonsChartCentralLabel() {
    return personsChartCentralLabel;
  }

  /**
   * @param personsChartCentralLabel the personsChartCentralLabel to set
   */
  public void setPersonsChartCentralLabel(List<OrganizationalRole> personsChartCentralLabel) {
    this.personsChartCentralLabel = personsChartCentralLabel;
  }

  /**
   * @return the personsChartCategoriesLabel
   */
  public List<OrganizationalRole> getPersonsChartCategoriesLabel() {
    return personsChartCategoriesLabel;
  }

  /**
   * @param personsChartCategoriesLabel the personsChartCategoriesLabel to set
   */
  public void setPersonsChartCategoriesLabel(
      List<OrganizationalRole> personsChartCategoriesLabel) {
    this.personsChartCategoriesLabel = personsChartCategoriesLabel;
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
   * @return the personsChartOthersInfosKeys
   */
  public Map<String, String> getPersonsChartOthersInfosKeys() {
    return personsChartOthersInfosKeys;
  }

  /**
   * @param personsChartOthersInfosKeys the personsChartOthersInfosKeys to set
   */
  public void setPersonsChartOthersInfosKeys(Map<String, String> personsChartOthersInfosKeys) {
    this.personsChartOthersInfosKeys = personsChartOthersInfosKeys;
  }

}