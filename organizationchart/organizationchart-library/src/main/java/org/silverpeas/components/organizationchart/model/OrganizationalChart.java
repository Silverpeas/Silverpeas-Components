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

import java.util.List;
import java.util.Set;

public class OrganizationalChart {

  private final OrganizationalUnit root;
  private final List<OrganizationalPerson> persons;
  private final Set<PersonCategory> categories;
  private final List<OrganizationalUnit> units;

  public OrganizationalChart(OrganizationalUnit root, List<OrganizationalUnit> units,
      List<OrganizationalPerson> persons) {
    // case unit chart: you need persons and units
    this.root = root;
    this.units = units;
    this.persons = persons;
    this.categories = null;
  }

  public OrganizationalChart(OrganizationalUnit root, List<OrganizationalPerson> persons,
      Set<PersonCategory> categories) {
    // case persons chart: needs persons and categories
    this.root = root;
    this.categories = categories;
    this.persons = persons;
    this.units = null;
  }

  public OrganizationalUnit getRoot() {
    return root;
  }

  public List<OrganizationalPerson> getPersons() {
    return persons;
  }

  public List<OrganizationalUnit> getUnits() {
    return units;
  }

  public Set<PersonCategory> getCategories() {
    return categories;
  }

}
