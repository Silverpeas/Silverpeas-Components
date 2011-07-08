/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

package com.silverpeas.scheduleevent.servlets.handlers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.scheduleevent.control.ScheduleEventSessionController;
import com.silverpeas.scheduleevent.service.model.beans.DateOption;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEvent;

public class ScheduleEventAddDateRequestHandler extends ScheduleEventActionDateRequestHandler {
  private final static DateFormat JS_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
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
      throw UndefinedForwardRequestHandlerException();
    }
  }

  private String addSelectedDateAndForwardRequestHandler(String function,
      ScheduleEventSessionController scheduleeventSC, HttpServletRequest request) throws Exception {
    ScheduleEvent current = scheduleeventSC.getCurrentScheduleEvent();
    Set<DateOption> dates = current.getDates();
    SimpleDateFormat formatter = getSimpleDateFormat(scheduleeventSC);
    Date dateToAdd = formatter.parse(request.getParameter("dateToAdd"));
    String dateIdSearch = formatterTmpId.format(dateToAdd);
    DateOption dateOption = getExistingDateOption(current.getDates(), dateIdSearch);
    if (dateOption == null) {
      DateOption option = new DateOption();
      option.setDay(dateToAdd);
      dates.add(option);
    }
    request.setAttribute(LAST_DATE, JS_DATE_FORMATTER.format(dateToAdd));
    return forwardRequestHandler.getDestination(function, scheduleeventSC, request);
  }

  private SimpleDateFormat getSimpleDateFormat(ScheduleEventSessionController scheduleeventSC){
    String pattern = scheduleeventSC.getString("scheduleevent.form.dateformat");
    if(pattern == null){
      pattern = "dd/MM/yy";
    }
    return new SimpleDateFormat(pattern);
  }

  private Exception UndefinedForwardRequestHandlerException() {
    return new Exception(
        "No forward request defines for" + this.getClass());
  }
}
