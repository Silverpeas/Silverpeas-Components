/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.servlets;

import org.silverpeas.components.kmelia.control.KmeliaSessionController;
import org.silverpeas.components.kmelia.servlets.ajax.AjaxOperation;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

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
  private static final String POST_METHOD = "POST";

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
    // the check is performed before the processing below, whose catch-all would swallow the error
    if (isWritingRequestedByGet(req)) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN,
          "A writing operation has to be requested by POST");
      return;
    }
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

  /**
   * Is the requested operation writing something whereas it is requested by a GET? This servlet
   * answers the GET as the POST, and no URL of it holds any of the keywords making the
   * synchronizer token required on a GET. Such an operation could hence be requested from another
   * site on behalf of the user being lured, so it is refused.
   * @param req the incoming request.
   * @return true if the operation has to be refused, false otherwise. An unknown operation is
   * taken in charge as before, by the processing itself.
   */
  private boolean isWritingRequestedByGet(final HttpServletRequest req) {
    if (POST_METHOD.equals(req.getMethod())) {
      return false;
    }
    try {
      return AjaxOperation.valueOf(getAction(req)).isWriting();
    } catch (IllegalArgumentException | NullPointerException e) {
      return false;
    }
  }

  private KmeliaSessionController createSessionController(HttpSession session, String componentId) {
    MainSessionController msc =
        (MainSessionController) session.getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    if (msc != null) {
      ComponentContext componentContext = msc.createComponentContext(null, componentId);
      if (organizationController.isComponentAvailableToUser(componentId, msc.getUserId())) {
        return new KmeliaSessionController(msc, componentContext);
      }
    }
    return null;
  }
}
