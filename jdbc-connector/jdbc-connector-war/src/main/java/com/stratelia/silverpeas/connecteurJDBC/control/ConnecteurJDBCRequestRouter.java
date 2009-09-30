/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.connecteurJDBC.control;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.connecteurJDBC.model.ConnecteurJDBCRuntimeException;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Title: Connecteur JDBC Description: Ce composant a pour objet de permettre de
 * récupérer rapidement et simplement des données du système d'information de
 * l'entreprise. Copyright: Copyright (c) 2001 Company: Stratélia
 */

public class ConnecteurJDBCRequestRouter extends ComponentRequestRouter {

  /**
   * Method declaration
   * 
   * 
   * @param mainSessionCtrl
   * @param componentContext
   * 
   * @return
   * 
   * @throws ConnecteurJDBCRuntimeException
   * 
   * @see
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext)
      throws ConnecteurJDBCRuntimeException {
    ComponentSessionController component = (ComponentSessionController) new ConnecteurJDBCSessionController(
        mainSessionCtrl, componentContext);

    return component;
  }

  /**
   * getSessionControlBeanName
   * 
   * @return the component name, must begin with lowercase.
   */
  public String getSessionControlBeanName() {

    /**
     * This method has to be implemented in the component request rooter class.
     * 
     * @returns the session control bean name. ex : for almanach, returns
     *          "almanach"
     */
    return "connecteurJDBC";
  }

  /**
   * The rooter compute a destination page given : the function called by the
   * client, the current state of the component (embbeded in the session
   * controleur) and parameters from the request object.
   * 
   * @param function
   *          The entering request function (ex : "Main.jsp")
   * @param componentSC
   *          The component Session Control, build and initialised.
   * @param request
   *          current http request
   * @return The complete destination URL for a forward (ex :
   *         "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    String destination = null;
    ConnecteurJDBCSessionController connecteurJDBC = (ConnecteurJDBCSessionController) componentSC;
    String rootDest = "/connecteurJDBC/jsp/";

    String flag = connecteurJDBC.getUserRoleLevel();
    request.setAttribute("flag", flag);

    if ((function.startsWith("Main"))
        || (function.startsWith("connecteurJDBC"))) {
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
      if (StringUtil.isDefined(request.getParameter("RowLimit")))
        rowLimit = Integer.parseInt(request.getParameter("RowLimit"));
      try {
        connecteurJDBC.setJDBCdriverName(JDBCdriverName);
        connecteurJDBC.updateConnection(JDBCdriverName, JDBCurl, login,
            password, rowLimit);
      } catch (Exception e) {
        SilverTrace.warn("connecteurJDBC",
            "ConnecteurJDBCRequestRouter.getDestination()",
            "connecteurJDBC.MSG_CONNECTION_NOT_STARTED", e);
      }
      return getDestination("ParameterConnection", componentSC, request);
    } else if (function.startsWith("processForm")) {
      destination = "processForm.jsp";
    } else {
      destination = function;
    }

    return rootDest + destination;

  }

}
