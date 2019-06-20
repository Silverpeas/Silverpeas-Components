/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.components.organizationchart.service;

import org.silverpeas.components.organizationchart.model.OrganizationalChart;
import org.silverpeas.components.organizationchart.model.OrganizationalChartType;
import org.silverpeas.components.organizationchart.model.OrganizationalPerson;
import org.silverpeas.components.organizationchart.model.OrganizationalPersonComparator;
import org.silverpeas.components.organizationchart.model.OrganizationalUnit;
import org.silverpeas.components.organizationchart.model.PersonCategory;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
    if (!groupId.equals(config.getRoot())) {
      if (!group.isRoot()) {
        Group parent = OrganizationControllerProvider.getOrganisationController().getGroup(group.
            getSuperGroupId());
        if (parent != null) {
          root.setParentName(parent.getName());
          root.setParentOu(parent.getId());
        }
      }
    }

    List<OrganizationalPerson> ouMembers = getMembers(groupId, group.getUserIds(), type);
    root.setHasMembers(ouMembers != null && !ouMembers.isEmpty());

    OrganizationalChart chart;
    switch (type) {
      case TYPE_UNITCHART:
        List<OrganizationalUnit> units = getSubOrganizationUnits(group);
        chart = new OrganizationalChart(root, units, ouMembers, true);
        break;

      default:
        Set<PersonCategory> categories = getCategories(ouMembers);
        chart = new OrganizationalChart(root, ouMembers, categories, true);
        break;
    }

    return chart;
  }

  private List<OrganizationalPerson> getMembers(String groupId, String[] userIds,
      OrganizationalChartType type) {

    List<OrganizationalPerson> personList = new ArrayList<>(userIds.length);

    for (String userId : userIds) {
      OrganizationalPerson person = loadOrganizationalPerson(userId, type);
      if (person != null) {
        personList.add(person);
      }
    }
    Collections.sort(personList, new OrganizationalPersonComparator());
    return personList;
  }

  private OrganizationalPerson loadOrganizationalPerson(String id, OrganizationalChartType type) {
    UserFull user = OrganizationControllerProvider.getOrganisationController().getUserFull(id);

    if (!user.isActivatedState()) {
      return null;
    }

    String userFunction = user.getValue(config.getAttTitle());
    String userDescription = user.getValue(config.getAttDesc());
    String userService = user.getValue(config.getAttUnit());

    OrganizationalPerson person =
        new OrganizationalPerson(Integer.parseInt(id), -1, user.getDisplayedName(), userFunction,
            userDescription, userService, null);

    // Determines attributes to be returned
    Map<String, String> attributesToReturn;
    switch (type) {
      case TYPE_UNITCHART:
        attributesToReturn = config.getUnitsChartOthersInfosKeys();
        break;

      default:
        attributesToReturn = config.getPersonnsChartOthersInfosKeys();
    }

    Map<String, String> details = new HashMap<>();
    for (Entry<String, String> attribute : attributesToReturn.entrySet()) {
      details.put(attribute.getValue(), user.getValue(attribute.getKey()));
    }
    person.setDetail(details);

    // defined the boxes with persons inside
    if (userFunction != null) {
      switch (type) {
        case TYPE_UNITCHART:
          defineUnitChartRoles(person, userFunction, config);
          break;

        default:
          defineDetailledChartRoles(person, userFunction, config);
          break;
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
      // set responsible of subunit
      List<OrganizationalPerson> users = getMembers(subgroup.getId(), subgroup.getUserIds(),
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
