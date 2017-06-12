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

package org.silverpeas.components.quizz.servlets;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.questioncontainer.question.model.Question;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerDetail;
import org.silverpeas.components.quizz.control.QuizzSessionController;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   */
  public QuizzSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new QuizzSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param quizzSC The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex : "/quizz/jsp/quizz.jsp?flag=user")
   */
  public String getDestination(String function, QuizzSessionController quizzSC,
      HttpRequest request) {
    String destination = "";
    String rootDest = "/quizz/jsp/";

    String flag = quizzSC.getHighestSilverpeasUserRole().getName();
    request.setAttribute("Profile", flag);

    try {
      boolean profileError = false;
      if (function.startsWith("Main")) {
        // the flag is the best user's profile
        if ("publisher".equals(flag) || "admin".equals(flag)) {
          destination = rootDest + "quizzAdmin.jsp";
        } else {
          destination = rootDest + "quizzUser.jsp";
        }
      } else if (function.startsWith("portlet")) {
        if ("publisher".equals(flag) || "admin".equals(flag)) {
          destination = rootDest + "quizzPortlet.jsp";
        } else {
          destination = rootDest + "quizzUserPortlet.jsp";
        }
      } else if (function.startsWith("quizzCreator")) {
        if ("publisher".equals(flag) || "admin".equals(flag)) {

          quizzSC.createTemporaryQuizz(request);

          destination = rootDest + "quizzCreator.jsp";
        } else {
          profileError = true;
        }
      } else if ("ExportCSV".equals(function)) {
        String quizzId = request.getParameter("QuizzId");
        String csvFilename = quizzSC.exportQuizzCSV(quizzId);

        request.setAttribute("CSVFilename", csvFilename);
        if (StringUtil.isDefined(csvFilename)) {
          File file = new File(FileRepositoryManager.getTemporaryPath() + csvFilename);
          request.setAttribute("CSVFileSize", Long.valueOf(file.length()));
          request.setAttribute("CSVFileURL", FileServerUtils.getUrlToTempDir(csvFilename));
        }
        destination = rootDest + "downloadCSV.jsp";
      } else if (function.equals("copy")) {
        String quizzId = request.getParameter("Id");
        try {
          quizzSC.copySurvey(quizzId);
        } catch (Exception e) {
          SilverTrace.warn("Quizz", "QuizzRequestRouter.getDestination()", "root.EX_CLIPBOARD_COPY_FAILED",
              "function = " + function, e);
        }
        destination = URLUtil.getURL(URLUtil.CMP_CLIPBOARD, null, null) +
            "Idle.jsp?message=REFRESHCLIPBOARD";
      } else if (function.startsWith("paste")) {
        try {
          quizzSC.paste();
        } catch (Exception e) {
          SilverTrace.warn("Quizz", "QuizzRequestRouter.getDestination()", "root.EX_CLIPBOARD_PASTE_FAILED",
              "function = " + function, e);
        }
        destination = getDestination("Main", quizzSC, request);
      } else if (function.startsWith("searchResult")) {
        String id = request.getParameter("Id");



        if ("publisher".equals(flag) || "admin".equals(flag)) {
          destination = rootDest + "quizzQuestionsNew.jsp?Action=ViewQuizz&QuizzId=" + id;
        } else {
          if (quizzSC.isParticipationAllowed(id)) {
            destination = rootDest + "quizzQuestionsNew.jsp?Action=ViewCurrentQuestions&QuizzId="
                + id;
          } else {
            destination = rootDest + "quizzResultUser.jsp";
          }
        }
      } else if (function.equals("SubmitQuizz")) {
        HttpSession session = request.getSession(false);
        QuestionContainerDetail quizzDetail = (QuestionContainerDetail) session.getAttribute("quizzUnderConstruction");

        //Vector 2 Collection
        List questionsV = (List) session.getAttribute("questionsVector");
        List<Question> q = new ArrayList<Question>();
        for (int j = 0; j < questionsV.size(); j++) {
          q.add((Question) questionsV.get(j));
        }
        quizzDetail.setQuestions(q);
        quizzSC.createQuizz(quizzDetail);
        session.removeAttribute("quizzUnderConstruction");
        quizzSC.setPositions(null);
        destination = getDestination("Main", quizzSC, request);
      } else {
        destination = rootDest + function;
      }

      if (profileError) {
        String sessionTimeout =
            ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
        destination = sessionTimeout;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpage.jsp";
    }

    return destination;
  }

}
