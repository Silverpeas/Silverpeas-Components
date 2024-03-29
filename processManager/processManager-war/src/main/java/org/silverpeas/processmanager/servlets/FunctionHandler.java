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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.processmanager.servlets;

import org.silverpeas.processmanager.ProcessManagerException;
import org.silverpeas.processmanager.ProcessManagerSessionController;

import javax.servlet.http.HttpServletRequest;

/**
 * A function handler is associated to a peas function and is called by the request router when this
 * function has to be processed.
 */
@FunctionalInterface
public interface FunctionHandler {
  /**
   * Process the request and returns the response url.
   * @param function the user request name
   * @param session the user request context
   * @param request the user request params
   */
  String getDestination(String function,
      ProcessManagerSessionController session, HttpServletRequest request)
      throws ProcessManagerException;
}
