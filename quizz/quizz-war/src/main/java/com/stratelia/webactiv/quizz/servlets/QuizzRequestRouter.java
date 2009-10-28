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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.quizz.servlets;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.quizz.control.QuizzSessionController;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

public class QuizzRequestRouter extends ComponentRequestRouter {

  /**
   * This method has to be implemented in the component request rooter class.
   * returns the session control bean name to be put in the request object ex :
   * for quizz, returns "quizz"
   */
  public String getSessionControlBeanName() {
    return "quizz";
  }

  /**
   * Method declaration
   * 
   * 
   * @param mainSessionCtrl
   * @param componentContext
   * 
   * @return
   * 
   * @see
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    ComponentSessionController component = (ComponentSessionController) new QuizzSessionController(
        mainSessionCtrl, componentContext);

    return component;
  }

  /**
   * This method has to be implemented by the component request rooter it has to
   * compute a destination page
   * 
   * @param function
   *          The entering request function (ex : "Main.jsp")
   * @param componentSC
   *          The component Session Control, build and initialised.
   * @param request
   *          The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex :
   *         "/quizz/jsp/quizz.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    SilverTrace.info("Quizz", "QuizzRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", function);
    QuizzSessionController quizzSC = (QuizzSessionController) componentSC;
    String destination = "";

    try {
      boolean profileError = false;
      if (function.startsWith("Main")) {
        // the flag is the best user's profile
        String flag = componentSC.getUserRoleLevel();
        if ("publisher".equals(flag) || "admin".equals(flag)) {
          destination = "quizzAdmin.jsp";
        } else {
          destination = "quizzUser.jsp";
        }
      } else if (function.startsWith("portlet")) {
        String flag = componentSC.getUserRoleLevel();
        if ("publisher".equals(flag) || "admin".equals(flag))
          destination = "quizzPortlet.jsp";
        else
          destination = "quizzUserPortlet.jsp";
      } else if (function.startsWith("quizzCreator")) {
        String flag = componentSC.getUserRoleLevel();

        if ("publisher".equals(flag) || "admin".equals(flag)) {
          destination = "quizzCreator.jsp";
        } else {
          profileError = true;
        }
      } else if (function.startsWith("searchResult")) {
        String flag = componentSC.getUserRoleLevel();
        String id = request.getParameter("Id");

        SilverTrace.info("Quizz", "QuizzRequestRouter.getDestination()", "",
            "id = " + id);

        if ("publisher".equals(flag) || "admin".equals(flag)) {
          destination = "quizzQuestionsNew.jsp?Action=ViewQuizz&QuizzId=" + id;
        } else {
          if (quizzSC.isParticipationAllowed(id))
            destination = "quizzQuestionsNew.jsp?Action=ViewCurrentQuestions&QuizzId="
                + id;
          else
            destination = "quizzResultUser.jsp";
        }
      } else {
        destination = function;
      }

      if (profileError) {
        String sessionTimeout = GeneralPropertiesManager
            .getGeneralResourceLocator().getString("sessionTimeout");

        destination = sessionTimeout;
      } else {
        destination = "/quizz/jsp/" + destination;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpage.jsp";
    }

    return destination;
  }

}
