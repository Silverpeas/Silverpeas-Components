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
package org.silverpeas.components.almanach;

import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.silverstatistics.volume.model.UserIdCountVolumeCouple;
import org.silverpeas.core.silverstatistics.volume.service.ComponentStatisticsProvider;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Statistics about almanach application instances.
 */
@Singleton
@Named("almanach" + ComponentStatisticsProvider.QUALIFIER_SUFFIX)
public class AlmanachStatistics implements ComponentStatisticsProvider {

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId) {
    return getEvents(componentId).map(e -> {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId(e.getLastUpdater().getId());
      myCouple.setCountVolume(1);
      return myCouple;
    }).collect(Collectors.toList());
  }

  private Stream<CalendarEvent> getEvents(String componentId) {
    return Calendar.getByComponentInstanceId(componentId).stream()
        .flatMap(c -> Calendar.getEvents().filter(f -> f.onCalendar(c)).stream());
  }
}
