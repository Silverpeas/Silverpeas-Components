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

import org.silverpeas.components.organizationchart.model.*;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.kernel.util.StringUtil;

import java.util.*;
import java.util.Map.Entry;

class GroupOrganizationChartBuilder extends AbstractOrganizationChartBuilder {

  private final GroupOrganizationChartConfiguration config;

  static GroupOrganizationChartBuilder from(GroupOrganizationChartConfiguration config) {
    return new GroupOrganizationChartBuilder(config);
  }

  private GroupOrganizationChartBuilder(final GroupOrganizationChartConfiguration config) {
    this.config = config;
  }

  OrganizationalChart buildFor(String groupId, OrganizationalChartType type) {
    // check rootId
    if (!StringUtil.isDefined(groupId)) {
      groupId = config.getRoot();
    }

    Group group = OrganizationControllerProvider.getOrganisationController().getGroup(groupId);

    OrganizationalUnit root = new OrganizationalUnit(group.getName(), groupId);
    if (!groupId.equals(config.getRoot()) && !group.isRoot()) {
        Group parent = OrganizationControllerProvider.getOrganisationController().getGroup(group.
            getSuperGroupId());
        if (parent != null) {
          root.setParentName(parent.getName());
          root.setParentOu(parent.getId());
        }
      }


    List<OrganizationalPerson> ouMembers = getMembers(group.getUserIds(), type);
    root.setHasMembers(!ouMembers.isEmpty());

    OrganizationalChart chart;
    if (Objects.requireNonNull(type) == OrganizationalChartType.TYPE_UNITCHART) {
      List<OrganizationalUnit> units = getSubOrganizationUnits(group);
      chart = new OrganizationalChart(root, units, ouMembers);
    } else {
      Set<PersonCategory> categories = getCategories(ouMembers);
      chart = new OrganizationalChart(root, ouMembers, categories);
    }

    return chart;
  }

  private List<OrganizationalPerson> getMembers(String[] userIds,
      OrganizationalChartType type) {

    List<OrganizationalPerson> personList = new ArrayList<>(userIds.length);

    for (String userId : userIds) {
      OrganizationalPerson person = loadOrganizationalPerson(userId, type);
      if (person != null) {
        personList.add(person);
      }
    }
    personList.sort(new OrganizationalPersonComparator());
    return personList;
  }

  private OrganizationalPerson loadOrganizationalPerson(String id, OrganizationalChartType type) {
    UserFull user = OrganizationControllerProvider.getOrganisationController().getUserFull(id);

    if (!user.isActivatedState()) {
      return null;
    }

    String userFunction = user.getValue(config.getAttTitle());
    String userService = user.getValue(config.getAttUnit());

    OrganizationalPerson person =
        new OrganizationalPerson(Integer.parseInt(id), -1, user.getDisplayedName(), userFunction,
            userService, null);

    // Determines attributes to be returned
    Map<String, String> attributesToReturn;
    if (Objects.requireNonNull(type) == OrganizationalChartType.TYPE_UNITCHART) {
      attributesToReturn = config.getUnitsChartOthersInfosKeys();
    } else {
      attributesToReturn = config.getPersonsChartOthersInfosKeys();
    }

    Map<String, String> details = new HashMap<>();
    for (Entry<String, String> attribute : attributesToReturn.entrySet()) {
      details.put(attribute.getValue(), user.getValue(attribute.getKey()));
    }
    person.setDetail(details);

    // defined the boxes with persons inside
    if (userFunction != null) {
      if (type == OrganizationalChartType.TYPE_UNITCHART) {
        defineUnitChartRoles(person, userFunction, config);
      } else {
        defineDetailedChartRoles(person, userFunction, config);
      }
    } else {
      person.setVisibleCategory(new PersonCategory("Personnel"));
    }

    return person;
  }

  /**
   * Get sub organization units of a given OU.
   * @param group the silverpeas group
   * @return a List of OrganizationalUnit objects.
   */
  private List<OrganizationalUnit> getSubOrganizationUnits(Group group) {
    List<? extends Group> subgroups = group.getSubGroups();

    List<OrganizationalUnit> units = new ArrayList<>(subgroups.size());
    for (Group subgroup : subgroups) {
      OrganizationalUnit unit = new OrganizationalUnit(subgroup.getName(), subgroup.getId());
      unit.setParentName(group.getName());
      unit.setParentOu(group.getId());
      unit.setHasSubUnits(!subgroup.getSubGroups().isEmpty());
      // set responsible for subunit
      List<OrganizationalPerson> users = getMembers(subgroup.getUserIds(),
          OrganizationalChartType.TYPE_UNITCHART);
      List<OrganizationalPerson> mainActors = getMainActors(users);
      unit.setMainActors(mainActors);
      // check if subunit have more people
      unit.setHasMembers(users.size() > mainActors.size());

      units.add(unit);
    }

    return units;
  }
}
