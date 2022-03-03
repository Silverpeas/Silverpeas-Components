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
import org.silverpeas.components.scheduleevent.service.model.beans.DateOption;
import org.silverpeas.components.scheduleevent.service.model.beans.ScheduleEvent;
import org.silverpeas.core.util.DateUtil;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class ScheduleEventAddDateRequestHandler extends ScheduleEventActionDateRequestHandler {
  private final DateFormat jsDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
  private ScheduleEventRequestHandler forwardRequestHandler = null;

  public void setForwardRequestHandler(ScheduleEventRequestHandler forwardRequestHandler) {
    this.forwardRequestHandler = forwardRequestHandler;
  }

  @Override
  public String getDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws Exception {
    if (forwardRequestHandler != null) {
      return addSelectedDateAndForwardRequestHandler(function, scheduleeventSC, request);
    } else {
      throw undefinedForwardRequestHandlerException();
    }
  }

  private String addSelectedDateAndForwardRequestHandler(String function,
      ScheduleEventSessionController scheduleeventSC, HttpServletRequest request) throws Exception {
    ScheduleEvent current = scheduleeventSC.getCurrentScheduleEvent();
    Set<DateOption> dates = current.getDates();
    Date dateToAdd =
        DateUtil.stringToDate(request.getParameter("dateToAdd"), scheduleeventSC.getLanguage());
    String dateIdSearch = FORMATTER_TMP_ID.format(dateToAdd);
    DateOption dateOption = getExistingDateOption(current.getDates(), dateIdSearch);
    if (dateOption == null) {
      DateOption option = new DateOption();
      option.setDay(dateToAdd);
      dates.add(option);
    }
    request.setAttribute(LAST_DATE, jsDateFormatter.format(dateToAdd));
    return forwardRequestHandler.getDestination(function, scheduleeventSC, request);
  }

  private Exception undefinedForwardRequestHandlerException() {
    return new Exception(
        "No forward request defines for" + this.getClass());
  }
}
