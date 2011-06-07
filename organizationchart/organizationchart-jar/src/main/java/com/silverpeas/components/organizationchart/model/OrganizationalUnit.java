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

package com.silverpeas.components.organizationchart.model;

import java.util.List;

public class OrganizationalUnit {

  private String name;

  // case organizational unit is a category of personn
  private String key;
  private boolean otherCategory = false;

  private boolean hasSubUnits = false;
  private boolean hasMembers = false;
  private List<OrganizationalPerson> mainActors = null;

  // case organizational unit is a ldap unit
  private String parentName;
  private String parentOu;
  private String completeName;

  public OrganizationalUnit(String name, String completeName) {
    this.name = name;
    this.key = null;
    this.completeName = completeName;
    this.parentName = null;
  }

  public String getName() {
    return name;
  }

  public String getCompleteName() {
    return completeName;
  }

  public void setParentName(String parentName) {
    this.parentName = parentName;
  }

  public String getParentName() {
    return parentName;
  }

  public void setParentOu(String parentOu) {
    this.parentOu = parentOu;
  }

  public String getParentOu() {
    return parentOu;
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

  public void setHasSubUnits(boolean hasSubUnits) {
    this.hasSubUnits = hasSubUnits;
  }

  public boolean hasSubUnits() {
    return hasSubUnits;
  }

  public void setHasMembers(boolean hasMembers) {
    this.hasMembers = hasMembers;
  }

  public boolean hasMembers() {
    return hasMembers;
  }

  public void setMainActors(List<OrganizationalPerson> mainActors) {
    this.mainActors = mainActors;
  }

  public List<OrganizationalPerson> getMainActors() {
    return mainActors;
  }

}
