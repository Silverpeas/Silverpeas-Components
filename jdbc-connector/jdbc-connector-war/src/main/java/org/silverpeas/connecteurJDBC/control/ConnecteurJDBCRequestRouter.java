/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package org.silverpeas.connecteurJDBC.control;

import org.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.servlet.HttpRequest;

/**
 * Title: Connecteur JDBC Description: Ce composant a pour objet de permettre de recuperer
 * rapidement et simplement des donnees du systeme d'information de l'entreprise.
 */

public class ConnecteurJDBCRequestRouter
    extends ComponentRequestRouter<ConnecteurJDBCSessionController> {
  private static final long serialVersionUID = -7624316436905170997L;

  /**
   * Method declaration
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @throws org.silverpeas.connecteurJDBC.control.ConnecteurJDBCException
   */
  public ConnecteurJDBCSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new ConnecteurJDBCSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * getSessionControlBeanName
   * @return the component name, must begin with lowercase.
   */
  public String getSessionControlBeanName() {

    /**
     * This method has to be implemented in the component request rooter class.
     * @returns the session control bean name. ex : for almanach, returns "almanach"
     */
    return "connecteurJDBC";
  }

  /**
   * The rooter compute a destination page given : the function called by the client, the current
   * state of the component (embbeded in the session controleur) and parameters from the request
   * object.
   * @param function The entering request function (ex : "Main.jsp")
   * @param connecteurJDBC The component Session Control, build and initialised.
   * @param request current http request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function, ConnecteurJDBCSessionController connecteurJDBC,
      HttpRequest request) {
    String destination = null;
    String rootDest = "/connecteurJDBC/jsp/";

    String flag = connecteurJDBC.getUserRoleLevel();
    request.setAttribute("flag", flag);

    if ((function.startsWith("Main")) || (function.startsWith("connecteurJDBC"))) {
      // the flag is the best user's profile
      destination = "connecteurJDBC.jsp";
    } else if (function.startsWith("portlet")) {
      destination = "portlet.jsp";
    } else if (function.startsWith("ParameterRequest")) {
      destination = "requestParameters.jsp";
    } else if (function.startsWith("DoRequest")) {
      destination = "connecteurJDBC.jsp";
    } else if (function.startsWith("ParameterConnection")) {
      connecteurJDBC.loadDrivers();
      destination = "connectionParameters.jsp";
    } else if (function.startsWith("UpdateConnection")) {
      String JDBCdriverName = request.getParameter("JDBCdriverName");
      String JDBCurl = request.getParameter("JDBCurl");
      String login = request.getParameter("Login");
      String password = request.getParameter("Password");
      int rowLimit = 0;
      if (StringUtil.isDefined(request.getParameter("RowLimit"))) {
        rowLimit = Integer.parseInt(request.getParameter("RowLimit"));
      }
      try {
        connecteurJDBC.setJDBCdriverName(JDBCdriverName);
        connecteurJDBC.updateConnection(JDBCdriverName, JDBCurl, login, password, rowLimit);
        connecteurJDBC.loadDrivers();
      } catch (Exception e) {
        SilverTrace.warn("connecteurJDBC", "ConnecteurJDBCRequestRouter.getDestination()",
            "connecteurJDBC.MSG_CONNECTION_NOT_STARTED", e);
      }
      destination = "connecteurJDBC.jsp";
    } else if (function.startsWith("processForm")) {
      destination = "processForm.jsp";
    } else {
      destination = function;
    }
    return rootDest + destination;
  }

}
