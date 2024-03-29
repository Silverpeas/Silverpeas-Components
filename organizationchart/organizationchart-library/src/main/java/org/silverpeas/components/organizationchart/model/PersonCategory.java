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
package org.silverpeas.components.organizationchart.model;

public class PersonCategory implements Comparable<PersonCategory> {

  /**
   * @return the order
   */
  public int getOrder() {
    return order;
  }

  /**
   * @param order the order to set
   */
  public void setOrder(int order) {
    this.order = order;
  }

  private String name;
  private String key;
  private int order;
  private boolean otherCategory = false;

  private boolean underOrganizationalUnitExists = false;
  private boolean underPersonnsExists = false;

  public PersonCategory(String name) {
    this.name = name;
    this.otherCategory = true;
    this.key = null;
  }

  public PersonCategory(String name, String key, int order) {
    this.name = name;
    this.key = key;
    this.order = order;
  }

  public String getName() {
    return name;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public boolean isOtherCategory() {
    return otherCategory;
  }

  public void setUnderOrganizationalUnitExists(boolean underOrganizationalUnitExists) {
    this.underOrganizationalUnitExists = underOrganizationalUnitExists;
  }

  public boolean isUnderOrganizationalUnitExists() {
    return underOrganizationalUnitExists;
  }

  public void setUnderPersonnsExists(boolean underPersonnsExists) {
    this.underPersonnsExists = underPersonnsExists;
  }

  public boolean isUnderPersonnsExists() {
    return underPersonnsExists;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PersonCategory other = (PersonCategory) obj;
    if (key == null) {
      if (other.key != null) {
        return false;
      }
    } else if (!key.equals(other.key)) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(PersonCategory otherCategory) {
    return Integer.valueOf(this.order).compareTo(otherCategory.getOrder());
  }

}
