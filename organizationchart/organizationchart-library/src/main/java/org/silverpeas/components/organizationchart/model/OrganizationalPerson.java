/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.components.organizationchart.model;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

public class OrganizationalPerson implements Comparable<OrganizationalPerson> {

  private int id;
  private int parentId;
  // =cn
  private String name;
  // =titre
  private String fonction;
  // =tooltip
  private String description;
  // ou
  private String service;
  // sAMAccountName
  private String silverpeasAccount;
  private String firstName;
  private String lastName;
  private Map<String, String> detail;

  // visible on the top central unit
  private boolean visibleOnCenter = false;
  private OrganizationalRole visibleCenterRole = null;
  // visible on the right unit (units organizational chart only)
  private boolean visibleOnRight = false;
  private OrganizationalRole visibleRightRole = null;
  // visible on the right unit (units organizational chart only)
  private boolean visibleOnLeft = false;
  private OrganizationalRole visibleLeftRole = null;
  // visible category name (detailled organizational chart only)
  private PersonCategory visibleCategory = null;

  public OrganizationalPerson(int id, int parentId, String name, String fonction,
      String description, String service, String silverpeasAccount) {
    this.id = id;
    this.parentId = parentId;
    this.name = name;
    this.fonction = fonction;
    this.description = description;
    this.service = service;
    this.setSilverpeasAccount(silverpeasAccount);
    // root par défaut
    this.parentId = -1;
  }

  public String toString() {
    return "cn = " + this.name + ", service = " + this.service + ", fonction = " + this.fonction;
  }

  public void setParentId(int parentId) {
    this.parentId = parentId;
  }

  public String getService() {
    return service;
  }

  public int getId() {
    return id;
  }

  public int getParentId() {
    return parentId;
  }

  public String getName() {
    return name;
  }

  public String getFonction() {
    return fonction;
  }

  public String getDescription() {
    return description;
  }

  public void setDetail(Map<String, String> detail) {
    this.detail = detail;
  }

  public Map<String, String> getDetail() {
    return detail;
  }

  public boolean isVisibleOnCenter() {
    return visibleOnCenter;
  }

  public void setVisibleOnCenter(boolean visibleOnCenter) {
    this.visibleOnCenter = visibleOnCenter;
  }

  public boolean isVisibleOnRight() {
    return visibleOnRight;
  }

  public void setVisibleOnRight(boolean visibleOnRight) {
    this.visibleOnRight = visibleOnRight;
  }

  public boolean isVisibleOnLeft() {
    return visibleOnLeft;
  }

  public void setVisibleOnLeft(boolean visibleOnLeft) {
    this.visibleOnLeft = visibleOnLeft;
  }

  public PersonCategory getVisibleCategory() {
    return visibleCategory;
  }

  public void setVisibleCategory(PersonCategory visibleCategory) {
    this.visibleCategory = visibleCategory;
  }

  public OrganizationalRole getVisibleCenterRole() {
    return visibleCenterRole;
  }

  public void setVisibleCenterRole(OrganizationalRole visibleCenterRole) {
    this.visibleCenterRole = visibleCenterRole;
  }

  public OrganizationalRole getVisibleRightRole() {
    return visibleRightRole;
  }

  public void setVisibleRightRole(OrganizationalRole visibleRightRole) {
    this.visibleRightRole = visibleRightRole;
  }

  public OrganizationalRole getVisibleLeftRole() {
    return visibleLeftRole;
  }

  public void setVisibleLeftRole(OrganizationalRole visibleLeftRole) {
    this.visibleLeftRole = visibleLeftRole;
  }

  public void setSilverpeasAccount(String silverpeasAccount) {
    this.silverpeasAccount = silverpeasAccount;
  }

  public String getSilverpeasAccount() {
    return silverpeasAccount;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Compares this person with the specified one by their respective name. So, as the name cannot
   * be unique, the method breaks the property <code>(x.compareTo(y)==0) == (x.equals(y))</code>
   * @param other the other person.
   * @return the comparing between their last names or, if the last names are equal, between their
   * first names.
   */
  @Override
  public int compareTo(OrganizationalPerson other) {
    int compare = lastName.compareToIgnoreCase(other.getLastName());
    if (compare != 0) {
      return compare;
    } else {
      return firstName.compareToIgnoreCase(other.getFirstName());
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OrganizationalPerson)) {
      return false;
    }

    final OrganizationalPerson that = (OrganizationalPerson) o;

    if (id != that.id) {
      return false;
    }
    return parentId == that.parentId;

  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getId()).append(getParentId()).toHashCode();
  }
}