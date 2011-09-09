/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.silverpeas.components.saasmanager.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.components.saasmanager.exception.SaasManagerException;
import com.silverpeas.components.saasmanager.handler.Handler;
import com.silverpeas.components.saasmanager.handler.SaasAccessActivationHandler;
import com.silverpeas.components.saasmanager.handler.SaasAccessRequestHandler;
import com.silverpeas.components.saasmanager.handler.SaasManagementHandler;
import com.silverpeas.components.saasmanager.handler.SaasServicesListHandler;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * SAAS management servlet
 * @author ahedin
 */
public class SaasManagerServlet extends HttpServlet {

  private static final long serialVersionUID = -7285465704069370660L;

  private Map<String, Handler> handlers = new HashMap<String, Handler>();

  public SaasManagerServlet() {
    handlers.put("servicesList", new SaasServicesListHandler());
    handlers.put("accessRequest", new SaasAccessRequestHandler());
    handlers.put("accessActivation", new SaasAccessActivationHandler());
    handlers.put("management", new SaasManagementHandler());
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {
    doPost(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {
    executeRequest(request, response);
  }

  /**
   * Executes the request by calling the handler corresponding to the action coming from the
   * request.
   * @param request The HTTP request.
   * @param response The HTTP response
   * @throws ServletException
   * @throws IOException
   */
  private void executeRequest(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {
    String page = "";
    String action = request.getParameter("action");
    Handler handler = handlers.get(action);
    if (handler != null) {
      page = handler.getPage(request);
    } else {
      request.setAttribute("javax.servlet.jsp.jspException",
        new SaasManagerException("SaasManagerServlet.executeRequest()", SilverpeasException.ERROR,
          "saasmanager.EX_UNKNOWN_ACTION", "action=" + action));
      page = "/admin/jsp/errorpageMain.jsp";
    }

    if (page.indexOf(".jsp") != -1) {
      if (page.indexOf("/") == -1) {
        page = "/saasmanager/jsp/" + page;
      }
      getServletConfig().getServletContext().getRequestDispatcher(page).forward(request, response);
    } else {
      response.getWriter().write(page);
    }
  }

}
