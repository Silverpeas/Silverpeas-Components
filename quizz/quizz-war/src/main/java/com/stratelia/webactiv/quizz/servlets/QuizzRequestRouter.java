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

package com.stratelia.webactiv.quizz.servlets;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.quizz.control.QuizzSessionController;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

public class QuizzRequestRouter extends ComponentRequestRouter<QuizzSessionController> {

  private static final long serialVersionUID = -8909826089973730380L;

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for quizz, returns "quizz"
   */
  public String getSessionControlBeanName() {
    return "quizz";
  }

  /**
   * Method declaration
   *
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  public QuizzSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new QuizzSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param quizzSC  The component Session Control, build and initialised.
   * @param request  The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex : "/quizz/jsp/quizz.jsp?flag=user")
   */
  public String getDestination(String function, QuizzSessionController quizzSC,
      HttpServletRequest request) {
    SilverTrace
        .info("Quizz", "QuizzRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", function);
    String destination = "";

    String flag = quizzSC.getUserRoleLevel();
    request.setAttribute("Profile", flag);

    try {
      boolean profileError = false;
      if (function.startsWith("Main")) {
        // the flag is the best user's profile
        if ("publisher".equals(flag) || "admin".equals(flag)) {
          destination = "quizzAdmin.jsp";
        } else {
          destination = "quizzUser.jsp";
        }
      } else if (function.startsWith("portlet")) {
        if ("publisher".equals(flag) || "admin".equals(flag)) {
          destination = "quizzPortlet.jsp";
        } else {
          destination = "quizzUserPortlet.jsp";
        }
      } else if (function.startsWith("quizzCreator")) {
        if ("publisher".equals(flag) || "admin".equals(flag)) {
          
          quizzSC.createTemporaryQuizz(request);
          
          destination = "quizzCreator.jsp";
        } else {
          profileError = true;
        }
      } else if (function.equals("ExportCSV")) {
        String quizzId = request.getParameter("QuizzId");
        String csvFilename = quizzSC.exportQuizzCSV(quizzId);

        request.setAttribute("CSVFilename", csvFilename);
        if (StringUtil.isDefined(csvFilename)) {
          File file = new File(FileRepositoryManager.getTemporaryPath() + csvFilename);
          request.setAttribute("CSVFileSize", Long.valueOf(file.length()));
          request.setAttribute("CSVFileURL", FileServerUtils.getUrlToTempDir(csvFilename));
          file = null;
        }
        destination = "downloadCSV.jsp";
      } else if (function.equals("copy")) {
        String quizzId = request.getParameter("Id");
        try {
          quizzSC.copySurvey(quizzId);
        } catch (Exception e) {
          SilverTrace.warn("Quizz", "QuizzRequestRouter.getDestination()", "root.EX_COPY_FAILED",
              "function = " + function, e);
        }
        destination = URLManager.getURL(URLManager.CMP_CLIPBOARD, null, null)
            + "Idle.jsp?message=REFRESHCLIPBOARD";
      } else if (function.startsWith("paste")) {
        try {
          quizzSC.paste();
        } catch (Exception e) {
          SilverTrace.warn("Quizz", "QuizzRequestRouter.getDestination()", "root.EX_CUT_FAILED",
              "function = " + function, e);
        }
        destination = URLManager.getURL(URLManager.CMP_CLIPBOARD, null, null) + "Idle.jsp";
      } else if (function.startsWith("searchResult")) {
        String id = request.getParameter("Id");

        SilverTrace.info("Quizz", "QuizzRequestRouter.getDestination()", "",
            "id = " + id);

        if ("publisher".equals(flag) || "admin".equals(flag)) {
          destination = "quizzQuestionsNew.jsp?Action=ViewQuizz&QuizzId=" + id;
        } else {
          if (quizzSC.isParticipationAllowed(id)) {
            destination = "quizzQuestionsNew.jsp?Action=ViewCurrentQuestions&QuizzId="
                + id;
          } else {
            destination = "quizzResultUser.jsp";
          }
        }
      } else {
        destination = function;
      }

      if (profileError) {
        String sessionTimeout =
            GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");

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
