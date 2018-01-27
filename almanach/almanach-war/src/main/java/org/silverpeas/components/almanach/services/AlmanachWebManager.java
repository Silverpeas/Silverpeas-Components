/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.almanach.services;

import org.silverpeas.components.almanach.AlmanachSettings;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.webapi.calendar.CalendarWebManager;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.silverpeas.components.almanach.AlmanachSettings.*;
import static org.silverpeas.core.admin.service.OrganizationControllerProvider
    .getOrganisationController;

/**
 * @author Yohann Chastagnier
 */
@Singleton
@Named("almanach" + CalendarWebManager.NAME_SUFFIX)
public class AlmanachWebManager extends CalendarWebManager {

  protected AlmanachWebManager() {
  }

  @Override
  public List<Calendar> getCalendarsHandledBy(final String componentInstanceId) {
    final List<Calendar> calendars = super.getCalendarsHandledBy(componentInstanceId);
    getComponentInstanceIdsToAggregateWith(componentInstanceId)
        .forEach(i -> calendars.addAll(Calendar.getByComponentInstanceId(i)));
    return calendars;
  }

  /**
   * Gets list of component instance aggregated to the one represented by the given identifier.
   * @param componentId identifier of a component instance.
   * @return list of component instance identifier which does not contain the given one.
   */
  private List<String> getComponentInstanceIdsToAggregateWith(final String componentId) {
    final List<String> componentInstanceIdsToAggregate = new ArrayList<>();

    // if the parameter is not activated, the empty list is returned immediately
    if (!StringUtil.getBooleanValue(getOrganisationController()
        .getComponentParameterValue(componentId, "useAgregation"))) {
      return componentInstanceIdsToAggregate;
    }

    final String aggregationMode = getAggregationMode();

    boolean inCurrentSpace = false;
    boolean inAllSpaces = false;
    if (ALMANACH_IN_SPACE_AND_SUBSPACES.equals(aggregationMode)) {
      inCurrentSpace = true;
    } else if (ALL_ALMANACHS.equals(aggregationMode)) {
      inCurrentSpace = true;
      inAllSpaces = true;
    }

    final SilverpeasComponentInstance componentInstance =
        SilverpeasComponentInstance.getById(componentId)
            .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

    Arrays.stream(getOrganisationController()
        .getAllComponentIdsRecur(componentInstance.getSpaceId(), User.getCurrentRequester().getId(),
            componentInstance.getName(), inCurrentSpace, inAllSpaces))
        .filter(i -> !i.equals(componentId)).forEach(componentInstanceIdsToAggregate::add);

    return componentInstanceIdsToAggregate;
  }

  @Override
  protected Integer[] getNextEventTimeWindows() {
    return AlmanachSettings.getNextEventTimeWindows();
  }
}
