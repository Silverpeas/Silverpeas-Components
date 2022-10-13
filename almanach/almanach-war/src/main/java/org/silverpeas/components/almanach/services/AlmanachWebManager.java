/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.almanach.services;

import org.silverpeas.components.almanach.AlmanachSettings;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.SpaceWithSubSpacesAndComponents;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.webapi.calendar.CalendarWebManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.silverpeas.components.almanach.AlmanachSettings.*;
import static org.silverpeas.core.admin.service.OrganizationControllerProvider.getOrganisationController;
import static org.silverpeas.core.util.StringUtil.getBooleanValue;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * @author Yohann Chastagnier
 */
@Service
@Named("almanach" + CalendarWebManager.NAME_SUFFIX)
public class AlmanachWebManager extends CalendarWebManager {

  @Inject
  private ComponentAccessControl componentAccessController;

  protected AlmanachWebManager() {
  }

  @Override
  public List<Calendar> getCalendarsHandledBy(final Collection<String> componentInstanceIds) {
    final List<Calendar> calendars = super.getCalendarsHandledBy(componentInstanceIds);
    final List<String> componentInstanceIdsToAggregateWith = getComponentInstanceIdsToAggregateWith(componentInstanceIds);
    if (!componentInstanceIdsToAggregateWith.isEmpty()) {
      calendars.addAll(Calendar.getByComponentInstanceIds(componentInstanceIdsToAggregateWith));
    }
    return calendars;
  }

  /**
   * Gets list of component instances aggregated to those represented by given identifiers.
   * @param componentIds identifiers of component instance.
   * @return list of component instance identifier which does not contain the given ones.
   */
  private List<String> getComponentInstanceIdsToAggregateWith(final Collection<String> componentIds) {
    final Set<String> indexedComponentIds = new HashSet<>(componentIds);
    final Map<String, Map<String, String>> parameterValues = getOrganisationController()
        .getParameterValuesByComponentIdThenByParamName(indexedComponentIds,
            asList("useAgregation", "customAggregation"));
    final Set<String> componentsUsingAggregation = new HashSet<>(indexedComponentIds.size());
    parameterValues.forEach((i, p) -> {
      if (getBooleanValue(p.get("useAgregation"))) {
        componentsUsingAggregation.add(i);
      }
    });
    // if the parameter is not activated, the empty list is returned immediately
    if (componentsUsingAggregation.isEmpty()) {
      return emptyList();
    }
    final Set<String> customAggregations = new HashSet<>(componentsUsingAggregation.size());
    final Set<String> componentsWithDefaultAggregation = new HashSet<>(componentsUsingAggregation.size());
    componentsUsingAggregation.forEach(i -> {
      final String customAggregation = parameterValues.getOrDefault(i, emptyMap()).get("customAggregation");
      if (isDefined(customAggregation)) {
        customAggregations.add(customAggregation);
      } else {
        componentsWithDefaultAggregation.add(i);
      }
    });
    final List<String> result = new ArrayList<>(indexedComponentIds.size());
    final List<String> customAggregationComponentIds = customAggregations.stream()
        .flatMap(a -> Stream.of(a.split(",")))
        .filter(StringUtil::isDefined)
        .map(String::trim)
        .filter(i -> !indexedComponentIds.contains(i))
        .filter(i -> COMPONENT_NAME.equals(SilverpeasComponentInstance.getComponentName(i)))
        .collect(Collectors.toList());
    componentAccessController
        .filterAuthorizedByUser(customAggregationComponentIds, User.getCurrentRequester().getId())
        .forEach(result::add);
    result.addAll(getAlmanachIdsByDefaultAggregation(componentsWithDefaultAggregation).stream()
        .map(SilverpeasComponentInstance::getId)
        .collect(Collectors.toList()));
    return result;
  }

  private List<SilverpeasComponentInstance> getAlmanachIdsByDefaultAggregation(final Set<String> componentIds) {
    final SpaceWithSubSpacesAndComponents allAlmanachsFromRootSpaceView;
    try {
      allAlmanachsFromRootSpaceView = getOrganisationController()
          .getFullTreeviewOnComponentName(User.getCurrentRequester().getId(), COMPONENT_NAME);
    } catch (AdminException e) {
      throw new SilverpeasRuntimeException(e);
    }
    final String aggregationMode = getAggregationMode();
    if (ALMANACH_IN_SPACE_AND_SUBSPACES.equals(aggregationMode)) {
      return allAlmanachsFromRootSpaceView.componentInstanceSelector()
          .fromSpaces(getAlmanachSpaceIdsOf(componentIds))
          .excludingComponentInstances(componentIds)
          .select();
    } else if (ALL_ALMANACHS.equals(aggregationMode)) {
      return allAlmanachsFromRootSpaceView.componentInstanceSelector()
          .fromAllSpaces()
          .excludingComponentInstances(componentIds)
          .select();
    } else {
      return allAlmanachsFromRootSpaceView.componentInstanceSelector()
          .fromSubSpacesOfSpaces(getAlmanachSpaceIdsOf(componentIds))
          .excludingComponentInstances(componentIds)
          .select();
    }
  }

  private Set<String> getAlmanachSpaceIdsOf(final Set<String> componentIds) {
    return componentIds.stream().map(i -> SilverpeasComponentInstance.getById(i)
        .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND)).getSpaceId())
        .collect(Collectors.toSet());
  }

  @Override
  protected Integer[] getNextEventTimeWindows() {
    return AlmanachSettings.getNextEventTimeWindows();
  }
}
