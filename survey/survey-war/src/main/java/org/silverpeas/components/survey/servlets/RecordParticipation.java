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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

package org.silverpeas.components.survey.servlets;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RecordParticipation extends HttpServlet {
  private static final long serialVersionUID = -1833168544559333059L;

  /**
   * Method invoked when called from a form or directly by URL
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
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
      cookieIp.setSecure(request.isSecure());
      response.addCookie(cookieIp);

      // Get the context
      String sRequestURL = request.getRequestURL().toString();
      String urlAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());

      response.sendRedirect(response.encodeRedirectURL(
          urlAbsolute + URLUtil.getApplicationURL() + URLUtil.getURL(null, null, componentId) +
              "surveyDetail.jsp?Action=ViewResult&SurveyId=" + surveyId));
    } catch (NumberFormatException | IOException e) {
      SilverLogger.getLogger(this).error(e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Method invoked when called from a form or directly by URL
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    doPost(request, response);
  }
}