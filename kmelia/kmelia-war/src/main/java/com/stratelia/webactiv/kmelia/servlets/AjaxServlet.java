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

package com.stratelia.webactiv.kmelia.servlets;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.servlets.ajax.AjaxOperation;

public class AjaxServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpSession session = req.getSession(true);

    String componentId = req.getParameter("ComponentId");

    KmeliaSessionController kmeliaSC =
        (KmeliaSessionController) session
        .getAttribute("Silverpeas_" + "kmelia" + "_" + componentId);

    if (kmeliaSC == null) {
      kmeliaSC = createSessionController(session, componentId);
    }

    String result = "nok";
    if (kmeliaSC != null) {
      AjaxOperation action = AjaxOperation.valueOf(getAction(req));
      result = action.handleRequest(req, kmeliaSC);
    }

    Writer writer = resp.getWriter();
    writer.write(result);
  }

  private String getAction(HttpServletRequest req) {
    return req.getParameter("Action");
  }

  private KmeliaSessionController createSessionController(HttpSession session, String componentId) {
    MainSessionController msc =
        (MainSessionController) session
        .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    if (msc != null) {
      ComponentContext componentContext = msc.createComponentContext(null, componentId);
      if (msc.getOrganizationController().isComponentAvailable(componentId, msc.getUserId())) {
        return new KmeliaSessionController(msc, componentContext);
      }
    }
    return null;
  }

}