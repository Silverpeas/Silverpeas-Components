/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.scheduleevent.servlets.handlers;

import org.silverpeas.components.scheduleevent.control.ScheduleEventSessionController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;

public interface ScheduleEventRequestHandler {

  String LAST_DATE = "scheduleEventLastDate";
  String SCHEDULE_EVENT_DETAIL = "scheduleEventDetail";
  String CURRENT_SCHEDULE_EVENT = "currentScheduleEvent";
  String LIST_SCHEDULE_EVENT = "scheduleEventList";

  SimpleDateFormat FORMATTER_TMP_ID = new SimpleDateFormat("ddMMyy");

  String getDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws Exception;

}
