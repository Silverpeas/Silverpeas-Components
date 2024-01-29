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

import org.silverpeas.components.organizationchart.model.OrganizationalPerson;
import org.silverpeas.components.organizationchart.model.OrganizationalRole;
import org.silverpeas.components.organizationchart.model.PersonCategory;
import org.silverpeas.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

abstract class AbstractOrganizationChartBuilder {

  AbstractOrganizationChartBuilder() {
    super();
  }

  /**
   * Determine if person has a specific Role in organization Chart.
   * @param person person
   * @param function person's function
   */
  void defineUnitChartRoles(OrganizationalPerson person, String function,
      AbstractOrganizationChartConfiguration config) {

    // Priority to avoid conflicted syntaxes : right, left and then central
    boolean roleDefined = false;

    // right
    for (OrganizationalRole role : config.getUnitsChartRightLabel()) {
      if (isFunctionMatchingRole(function, role)) {
        person.setVisibleOnRight(true);
        person.setVisibleRightRole(role);
        roleDefined = true;
        break;
      }
    }

    // left
    if (!roleDefined) {
      for (OrganizationalRole role : config.getUnitsChartLeftLabel()) {
        if (isFunctionMatchingRole(function, role)) {
          person.setVisibleOnLeft(true);
          person.setVisibleLeftRole(role);
          roleDefined = true;
          break;
        }
      }
    }

    // central
    if (!roleDefined) {
      for (OrganizationalRole role : config.getUnitsChartCentralLabel()) {
        if (isFunctionMatchingRole(function, role)) {
          person.setVisibleOnCenter(true);
          person.setVisibleCenterRole(role);
          break;
        }
      }
    }
  }

  /**
   * Determine if person has a specific Role in organization person Chart.
   * @param pers the oraganizational person
   * @param function person's function
   */
  void defineDetailledChartRoles(OrganizationalPerson pers, String function,
      AbstractOrganizationChartConfiguration config) {

    // Priority to avoid conflicted syntaxes : central then categories
    boolean roleDefined = false;

    // central
    for (OrganizationalRole role : config.getPersonnsChartCentralLabel()) {
      if (isFunctionMatchingRole(function, role)) {
        pers.setVisibleOnCenter(true);
        pers.setVisibleCenterRole(role);
        roleDefined = true;
        break;
      }
    }

    // categories
    if (!roleDefined) {
      int order = 0;
      for (OrganizationalRole role : config.getPersonnsChartCategoriesLabel()) {
        if (isFunctionMatchingRole(function, role)) {
          pers.setVisibleCategory(new PersonCategory(role.getLabel(), role.getLdapKey(), order));
          roleDefined = true;
          break;
        }
        order++;
      }
    }

    if (!roleDefined) {
      pers.setVisibleCategory(new PersonCategory("Personnel"));
    }
  }

  /**
   * Checks if given function is matching given role
   * @param function function to check
   * @param role role
   * @return true if function is matching given role
   */
  private boolean isFunctionMatchingRole(String function, OrganizationalRole role) {
    return ((role != null) && StringUtil.isDefined(role.getLdapKey()) &&
        function.toLowerCase().contains(role.getLdapKey().toLowerCase()));
  }

  List<OrganizationalPerson> getMainActors(List<OrganizationalPerson> users) {
    List<OrganizationalPerson> mainActors = new ArrayList<>();
    for (OrganizationalPerson person : users) {
      if (person.isVisibleOnCenter()) {
        mainActors.add(person);
      }
    }
    return mainActors;
  }

  /**
   * Get all distinct person categories represented by a given person list.
   * @param personList the person list
   * @return a Set of PersonCategory
   */
  Set<PersonCategory> getCategories(List<OrganizationalPerson> personList) {
    Set<PersonCategory> categories = new TreeSet<>();

    for (OrganizationalPerson person : personList) {
      // if person is a key Actor of organizationUnit, it will appear in main Cell, so ignore that
      // actor's category
      if (!person.isVisibleOnCenter() && person.getVisibleCategory() != null) {
        categories.add(person.getVisibleCategory());
      }
    }

    return categories;
  }

}