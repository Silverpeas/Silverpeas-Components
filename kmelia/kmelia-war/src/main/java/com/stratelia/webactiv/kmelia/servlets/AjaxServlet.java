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
package com.stratelia.webactiv.kmelia.servlets;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.servlets.ajax.AjaxOperation;
import org.silverpeas.core.admin.OrganizationController;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.Writer;

public class AjaxServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Inject
  private OrganizationController organizationController;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType(MediaType.TEXT_HTML);
    HttpSession session = req.getSession(true);
    String componentId = req.getParameter("ComponentId");
    KmeliaSessionController kmeliaSC =
        (KmeliaSessionController) session.getAttribute("Silverpeas_kmelia_" + componentId);
    if (kmeliaSC == null) {
      kmeliaSC = createSessionController(session, componentId);
    }
    String result = "nok";
    try {
      AjaxOperation action = AjaxOperation.valueOf(getAction(req));
      if (action.requiresController()) {
        if (kmeliaSC != null) {
          result = action.handleRequest(req, kmeliaSC);
        }
      } else {
        result = action.handleRequest(req, kmeliaSC);
      }
    } catch (Exception ignored) {
      result = "";
    }
    Writer writer = resp.getWriter();
    writer.write(result);
  }

  private String getAction(HttpServletRequest req) {
    return req.getParameter("Action");
  }

  private KmeliaSessionController createSessionController(HttpSession session, String componentId) {
    MainSessionController msc =
        (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    if (msc != null) {
      ComponentContext componentContext = msc.createComponentContext(null, componentId);
      if (organizationController.isComponentAvailable(componentId, msc.getUserId())) {
        return new KmeliaSessionController(msc, componentContext);
      }
    }
    return null;
  }
}
