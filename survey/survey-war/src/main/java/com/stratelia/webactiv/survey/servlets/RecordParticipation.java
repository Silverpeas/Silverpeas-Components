/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.stratelia.webactiv.survey.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class RecordParticipation extends HttpServlet {
  private static final long serialVersionUID = -1833168544559333059L;

  /**
   * Method invoked when called from a form or directly by URL
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    // Cookie Validity
    int cookieDuration = 3650;
    if (request.getParameter("duration") != null) {
      cookieDuration = Integer.parseInt(request.getParameter("duration"));
    }

    String componentId = request.getParameter("cid");
    String surveyId = request.getParameter("sid");

    // write cookie for this vote or survey
    Cookie cookieIp = new Cookie("surpoll" + surveyId, request.getRemoteAddr());
    cookieIp.setMaxAge(86400 * cookieDuration);
    cookieIp.setPath("/");
    response.addCookie(cookieIp);

    // Get the context
    String sRequestURL = request.getRequestURL().toString();
    String urlAbsolute =
        sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());

    SilverTrace.info("Survey", "RecordParticipation.doPost", "Survey.MSG_GEN_PARAM_VALUE",
        urlAbsolute + URLManager.getApplicationURL() + URLManager.getURL(null, null, componentId)
            + "surveyDetail.jsp&action=ViewResult&SurveyId=" + surveyId);
    response.sendRedirect(response.encodeRedirectURL(urlAbsolute + URLManager.getApplicationURL()
        + URLManager.getURL(null, null, componentId) +
        "surveyDetail.jsp?Action=ViewResult&SurveyId=" + surveyId));
  }

  /**
   * Method invoked when called from a form or directly by URL
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
  }
}