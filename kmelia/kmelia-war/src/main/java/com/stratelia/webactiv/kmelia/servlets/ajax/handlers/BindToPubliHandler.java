/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.kmelia.servlets.ajax.handlers;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.kmelia.KmeliaConstants;
import com.silverpeas.util.StringUtil;

import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.servlets.ajax.AjaxHandler;

public class BindToPubliHandler implements AjaxHandler {

  @Override
  public String handleRequest(HttpServletRequest request, KmeliaSessionController controller) {

    if (StringUtil.isDefined(request.getParameter("TopicToLinkId"))) {
      @SuppressWarnings("unchecked")
      Set<String> list = (Set<String>) request.getSession().getAttribute(
          KmeliaConstants.PUB_TO_LINK_SESSION_KEY);
      if (list == null) {
        list = new HashSet<String>(0);
        request.getSession().setAttribute(KmeliaConstants.PUB_TO_LINK_SESSION_KEY, list);
      }
      list.add(request.getParameter("TopicToLinkId"));
    }
    return "ok";
  }
}
