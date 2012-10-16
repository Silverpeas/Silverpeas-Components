/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
import java.util.Set;

public class OrganizationalChart {

  private OrganizationalUnit root = null;
  private List<OrganizationalPerson> personns = null;
  private Set<PersonCategory> categories = null;
  private List<OrganizationalUnit> units = null;
  private boolean silverpeasUserLinkable = false;

  private OrganizationalChartType chartType;

  public static final int UNITCHART = 0;
  public static final int PERSONNCHART = 1;

  public OrganizationalChart(OrganizationalUnit root, List<OrganizationalUnit> units,
      List<OrganizationalPerson> personns, boolean silverpeasUserLinkable) {
    // case unit chart: you needs personns and units
    this.chartType = OrganizationalChartType.TYPE_UNITCHART;
    this.root = root;
    this.units = units;
    this.personns = personns;
    this.categories = null;
    this.silverpeasUserLinkable = silverpeasUserLinkable;
  }

  public OrganizationalChart(OrganizationalUnit root, List<OrganizationalPerson> personns,
      Set<PersonCategory> categories, boolean silverpeasUserLinkable) {
    // case personns chart: needs personns and categories
    this.chartType = OrganizationalChartType.TYPE_PERSONNCHART;
    this.root = root;
    this.categories = categories;
    this.personns = personns;
    this.units = null;
    this.silverpeasUserLinkable = silverpeasUserLinkable;
  }

  public void setRoot(OrganizationalUnit root) {
    this.root = root;
  }

  public OrganizationalUnit getRoot() {
    return root;
  }

  public void setChartType(OrganizationalChartType chartType) {
    this.chartType = chartType;
  }

  public OrganizationalChartType getChartType() {
    return chartType;
  }

  public void setPersonns(List<OrganizationalPerson> personns) {
    this.personns = personns;
  }

  public List<OrganizationalPerson> getPersonns() {
    return personns;
  }

  public void setUnits(List<OrganizationalUnit> units) {
    this.units = units;
  }

  public List<OrganizationalUnit> getUnits() {
    return units;
  }

  public void setCategories(Set<PersonCategory> categories) {
    this.categories = categories;
  }

  public Set<PersonCategory> getCategories() {
    return categories;
  }

  public void setSilverpeasUserLinkable(boolean silverpeasUserLinkable) {
    this.silverpeasUserLinkable = silverpeasUserLinkable;
  }

  public boolean isSilverpeasUserLinkable() {
    return silverpeasUserLinkable;
  }
}
