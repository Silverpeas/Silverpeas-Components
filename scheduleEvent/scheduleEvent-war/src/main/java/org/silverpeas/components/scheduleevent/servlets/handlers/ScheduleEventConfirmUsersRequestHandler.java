/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

import javax.servlet.http.HttpServletRequest;

import org.silverpeas.components.scheduleevent.control.ScheduleEventSessionController;
import org.silverpeas.components.scheduleevent.control.ScheduleEventSessionController;

public class ScheduleEventConfirmUsersRequestHandler implements
		ScheduleEventRequestHandler {

	private ScheduleEventRequestHandler forwardRequestHandler = null;
	private boolean creationMode;

	public void setForwardRequestHandler(
			ScheduleEventRequestHandler forwardRequestHandler) {
		this.forwardRequestHandler = forwardRequestHandler;
	}

	public ScheduleEventConfirmUsersRequestHandler(boolean creationMode) {
		this.creationMode = creationMode;
	}

	@Override
	public String getDestination(String function,
			ScheduleEventSessionController scheduleeventSC,
			HttpServletRequest request) throws Exception {
		if (forwardRequestHandler != null) {
			return addUsersToCurrentScheduleEventAndForwardRequestHandler(
					function, scheduleeventSC, request);
		} else {
			throw UndefinedForwardRequestHandlerException();
		}
	}

	private String addUsersToCurrentScheduleEventAndForwardRequestHandler(
			String function, ScheduleEventSessionController scheduleeventSC,
			HttpServletRequest request) throws Exception {
	  if (creationMode) {
	    scheduleeventSC.setIdUsersAndGroups();
	  } else {
	    scheduleeventSC.updateIdUsersAndGroups();
	  }
		return forwardRequestHandler.getDestination(function, scheduleeventSC,
				request);
	}

	private Exception UndefinedForwardRequestHandlerException() {
		return new Exception("No forward request defines for" + this.getClass());
	}

}