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
package org.silverpeas.components.silvercrawler.servlets.handlers;

import org.silverpeas.components.silvercrawler.control.ProfileHelper;
import org.silverpeas.components.silvercrawler.control.SilverCrawlerSessionController;

import javax.servlet.http.HttpServletRequest;

/**
 * A function handler is associated to a peas function and is called by the request router when
 * this
 * function has to be processed.
 */
public abstract class FunctionHandler {

  protected static final String ROOT_DESTINATION = "/silverCrawler/jsp/";

  public String computeDestination(SilverCrawlerSessionController session,
      HttpServletRequest request) {
    try {
      String destination = getDestination(session, request);
      if (destination.startsWith("/")) {
        return destination;
      } else {
        return ROOT_DESTINATION + destination;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }
  }

  /**
   * Process the request and returns the response url.
   * @param session the user request context
   * @param request the user request params
   */
  public abstract String getDestination(SilverCrawlerSessionController session,
      HttpServletRequest request) throws Exception;

  /**
   * Return user's highest role
   * @param sessionController
   * @return
   */
  protected String getUserHighestRole(SilverCrawlerSessionController sessionController) {
    String[] profiles = sessionController.getUserRoles();
    return ProfileHelper.getBestProfile(profiles);
  }
}
