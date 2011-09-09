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

package com.silverpeas.components.saasmanager.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * SAAS access
 * @author ahedin
 */
public class SaasAccess {

  private static final int UNKNOWN_ID = -1;
  
  // Elements separator.
  private static final String SEPARATOR = ";";

  private int id;
  private String uid;
  private String lastName;
  private String firstName;
  private String email;
  private String phone;
  private String company;
  private String companyWebSite;
  private String lang;
  private int usersCount;
  private boolean conditionsAgreement;
  private String remarks;
  private String services;
  private String remoteAddress;
  private Date requestDate;
  private Date achievementDate;
  private String domainId;
  private String userId;
  private String userLogin;
  private String userPassword;
  private String spaceId;
  private String componentIds;
  private String homeComponentId;
  private String managementComponentId;

  public SaasAccess() {
    id = UNKNOWN_ID;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getUid() {
    return uid;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getPhone() {
    return phone;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getCompany() {
    return company;
  }

  public void setCompanyWebSite(String companyWebSite) {
    this.companyWebSite = companyWebSite;
  }

  public String getCompanyWebSite() {
    return companyWebSite;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public String getLang() {
    return lang;
  }

  public void setUsersCount(int usersCount) {
    this.usersCount = usersCount;
  }

  public int getUsersCount() {
    return usersCount;
  }

  public void setConditionsAgreement(boolean conditionsAgreement) {
    this.conditionsAgreement = conditionsAgreement;
  }

  public boolean isConditionsAgreement() {
    return conditionsAgreement;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setServices(String services) {
    this.services = services;
  }

  public String getServices() {
    return services;
  }

  /**
   * @return services as a list.
   */
  public ArrayList<String> getServicesList() {
    return getElementsList(services);
  }

  public void setRemoteAddress(String remoteAddress) {
    this.remoteAddress = remoteAddress;
  }

  public String getRemoteAddress() {
    return remoteAddress;
  }

  public void setRequestDate(Date requestDate) {
    this.requestDate = requestDate;
  }

  public Date getRequestDate() {
    return requestDate;
  }

  public void setAchievementDate(Date achievementDate) {
    this.achievementDate = achievementDate;
  }

  public Date getAchievementDate() {
    return achievementDate;
  }

  public void setDomainId(String domainId) {
    this.domainId = domainId;
  }

  public String getDomainId() {
    return domainId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserLogin(String userLogin) {
    this.userLogin = userLogin;
  }

  public String getUserLogin() {
    return userLogin;
  }

  public void setUserPassword(String userPassword) {
    this.userPassword = userPassword;
  }

  public String getUserPassword() {
    return userPassword;
  }

  public void setSpaceId(String spaceId) {
    this.spaceId = spaceId;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public void setComponentIds(String componentIds) {
    this.componentIds = componentIds;
  }

  /**
   * Sets components ids with the list of ids
   * @param componentIdsList
   */
  public void setComponentIds(ArrayList<String> componentIdsList) {
    componentIds = getElementsListString(componentIdsList);
  }

  public String getComponentIds() {
    return componentIds;
  }

  public ArrayList<String> getComponentIdsList() {
    return getElementsList(componentIds);
  }

  public void setHomeComponentId(String homeComponentId) {
    this.homeComponentId = homeComponentId;
  }

  public String getHomeComponentId() {
    return homeComponentId;
  }

  public void setManagementComponentId(String managementComponentId) {
    this.managementComponentId = managementComponentId;
  }

  public String getManagementComponentId() {
    return managementComponentId;
  }

  /**
   * @param elements A string of elements separated from each other with the separator SEPARATOR.
   * @return A list of elements
   */
  private ArrayList<String> getElementsList(String elements) {
    ArrayList<String> elementsList = new ArrayList<String>();
    StringTokenizer servicesSt = new StringTokenizer(elements, SEPARATOR);
    while (servicesSt.hasMoreTokens()) {
      elementsList.add(servicesSt.nextToken());
    }
    return elementsList;
  }

  /**
   * @param elements The list of elements.
   * @return A string composed of the elements separated by the separator SEPARATOR
   */
  private String getElementsListString(ArrayList<String> elements) {
    StringBuilder sb = new StringBuilder();
    for (String element : elements) {
      if (sb.length() > 0) {
        sb.append(SEPARATOR);
      }
      sb.append(element);
    }
    return sb.toString();
  }

}
