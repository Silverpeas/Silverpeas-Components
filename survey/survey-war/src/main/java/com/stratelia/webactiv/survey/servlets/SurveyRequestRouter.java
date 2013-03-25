/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.survey.servlets;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.survey.control.SurveySessionController;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail;

public class SurveyRequestRouter extends ComponentRequestRouter<SurveySessionController> {

  private static final long serialVersionUID = -1921269596127652643L;

  private static final String COMPONENT_NAME = "survey";

  /**
   * @param profiles
   * @return string representation of current user flag
   */
  public String getFlag(String[] profiles) {
    String flag = SilverpeasRole.user.toString();
    for (String profile : profiles) {
      if (SilverpeasRole.publisher.isInRole(profile)) {
        flag = profile;
      } else if (profile.equals("userMultiple")) {
        if (!flag.equals(SilverpeasRole.publisher.toString())) {
          flag = profile;
        }
      }
      // if admin, return it, we won't find a better profile
      if (SilverpeasRole.admin.isInRole(profile)) {
        return profile;
      }
    }
    
    return flag;
  }

  @Override
  public SurveySessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new SurveySessionController(mainSessionCtrl, componentContext);
  }

  @Override
  public String getSessionControlBeanName() {
    return COMPONENT_NAME;
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param surveySC The component Session Control, build and initialized.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, SurveySessionController surveySC,
      HttpServletRequest request) {
    SilverTrace.info(COMPONENT_NAME, "SurveyRequestRouter.getDestination",
        "Survey.MSG_ENTRY_METHOD");

    String flag = getFlag(surveySC.getUserRoles());
    String rootDest = "/survey/jsp/";
    if (flag.equals("userMultiple")) {
      surveySC.setParticipationMultipleAllowedForUser(true);
    }

    SilverTrace.info(COMPONENT_NAME, "SurveyRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "surveyId=" + surveySC.getSessionSurveyId());
    SilverTrace.info(COMPONENT_NAME, "SurveyRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "surveyId=" + request.getParameter("SurveyId"));

    surveySC.setPollingStationMode(false);
    SilverTrace.info(COMPONENT_NAME, "SurveyRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE",
        "getComponentRootName() = " + surveySC.getComponentRootName());
    if ("pollingStation".equals(surveySC.getComponentRootName())) {
      surveySC.setPollingStationMode(true);
    }
    request.setAttribute("PollingStationMode", Boolean.valueOf(surveySC.isPollingStationMode()));

    // Set status for this vote or survey
    setAnonymousParticipationStatus(request, surveySC);

    String destination = "";
    boolean profileError = false;
    if (function.startsWith("portlet")) {
      destination = rootDest + "portlet.jsp?Profile=" + flag;
    } else if (function.startsWith("Main") || function.startsWith("surveyList")) {
      // the flag is the best user's profile
      destination = rootDest + "surveyList.jsp?Profile=" + flag;
    } else if (function.startsWith("SurveyCreation") || function.startsWith("surveyCreator")) {
      if (flag.equals(SilverpeasRole.admin.toString()) || flag.equals(SilverpeasRole.publisher.toString())) {
        surveySC.sendNewSurveyAction(request);
        destination = rootDest + "surveyCreator.jsp";
      } else {
        profileError = true;
      }
    } else if (function.equals("UpdateSurvey")) {
      String surveyId = request.getParameter("SurveyId");
      try {
        // vérouiller l'enquête
        surveySC.closeSurvey(surveyId);
        // supprimer les participations
        surveySC.deleteVotes(surveyId);
      } catch (Exception e) {
        SilverTrace.warn(COMPONENT_NAME, "SurveyRequestRouter.getDestination()",
            "root.EX_USERPANEL_FAILED", "function = " + function, e);
      }
      destination = rootDest + "surveyUpdate.jsp?Action=UpdateSurveyHeader&SurveyId=" + surveyId;
    } else if (function.equals("ViewListResult")) {
      String answerId = request.getParameter("AnswerId");
      Collection<String> users = new ArrayList<String>();
      try {
        users = surveySC.getUsersByAnswer(answerId);
      } catch (Exception e) {
        SilverTrace.warn(COMPONENT_NAME, "SurveyRequestRouter.getDestination()",
            "root.EX_USERPANEL_FAILED", "function = " + function, e);
      }
      request.setAttribute("Users", users);
      destination = rootDest + "answerResult.jsp";
    } else if (function.equals("ViewAllUsers")) {
      QuestionContainerDetail survey = surveySC.getSessionSurvey();
      Collection<String> users = new ArrayList<String>();
      try {
        users = surveySC.getUsersBySurvey(survey.getId());
      } catch (Exception e) {
        SilverTrace.warn(COMPONENT_NAME, "SurveyRequestRouter.getDestination()",
            "root.EX_USERPANEL_FAILED", "function = " + function, e);
      }
      request.setAttribute("Users", users);
      request.setAttribute("Survey", survey);
      destination = rootDest + "answerResult.jsp";
    } else if (function.equals("UserResult")) {
      String userId = request.getParameter("UserId");
      String userName = request.getParameter("UserName");
      Collection<String> result = new ArrayList<String>();
      try {
        result = surveySC.getResultByUser(userId);
      } catch (Exception e) {
        SilverTrace.warn(COMPONENT_NAME, "SurveyRequestRouter.getDestination()",
            "root.EX_USERPANEL_FAILED", "function = " + function, e);
      }
      request.setAttribute("ResultUser", result);
      request.setAttribute("UserName", userName);
      request.setAttribute("UserId", userId);
      request.setAttribute("Survey", surveySC.getSessionSurvey());
      request.setAttribute("Profile", flag);

      destination = rootDest + "resultByUser.jsp";
    } else if (function.startsWith("searchResult")) {
      String id = request.getParameter("Id");
      request.setAttribute("Profile", flag);
      destination = rootDest + "surveyDetail.jsp?Action=ViewCurrentQuestions&SurveyId=" + id;
    } else if (function.equals("ToAlertUser")) {
      SilverTrace.debug(COMPONENT_NAME, "SurveyRequestRouter.getDestination()",
          "root.MSG_GEN_PARAM_VALUE", "ToAlertUser: function = " + function + " spaceId=" +
              surveySC.getSpaceId() + " componentId=" + surveySC.getComponentId());
      String surveyId = request.getParameter("SurveyId");
      try {
        destination = surveySC.initAlertUser(surveyId);
      } catch (Exception e) {
        SilverTrace.warn(COMPONENT_NAME, "SurveyRequestRouter.getDestination()",
            "root.EX_USERPANEL_FAILED", "function = " + function, e);
      }

      SilverTrace.debug(COMPONENT_NAME, "SurveyRequestRouter.getDestination()",
          "root.MSG_GEN_PARAM_VALUE", "ToAlertUser: function = " + function + "=> destination=" +
              destination);
    } else if (function.equals("ExportCSV")) {
      String surveyId = request.getParameter("SurveyId");
      String csvFilename = surveySC.exportSurveyCSV(surveyId);

      request.setAttribute("CSVFilename", csvFilename);
      if (StringUtil.isDefined(csvFilename)) {
        File file = new File(FileRepositoryManager.getTemporaryPath() + csvFilename);
        request.setAttribute("CSVFileSize", Long.valueOf(file.length()));
        request.setAttribute("CSVFileURL", FileServerUtils.getUrlToTempDir(csvFilename));
        file = null;
      }
      destination = rootDest + "downloadCSV.jsp";
    } else if (function.equals("copy")) {
      String surveyId = request.getParameter("Id");
      try {
        surveySC.copySurvey(surveyId);
      } catch (Exception e) {
        SilverTrace.warn(COMPONENT_NAME, "SurveyRequestRouter.getDestination()",
            "root.EX_COPY_FAILED", "function = " + function, e);
      }
      destination =
          URLManager.getURL(URLManager.CMP_CLIPBOARD, null, null) + "Idle.jsp?message=REFRESHCLIPBOARD";
    } else if (function.startsWith("paste")) {
      try {
        surveySC.paste();
      } catch (Exception e) {
        SilverTrace.warn(COMPONENT_NAME, "SurveyRequestRouter.getDestination()",
            "root.EX_CUT_FAILED", "function = " + function, e);
      }
      destination = URLManager.getURL(URLManager.CMP_CLIPBOARD, null, null) + "Idle.jsp";
    } else if ("questionsUpdate.jsp".equals(function)) {
      // Retrieve current action
      surveySC.questionsUpdateBusinessModel(request);

      request.setAttribute("SurveyName", surveySC.getSessionSurveyName());
      request.setAttribute("Questions", surveySC.getSessionQuestions());
      request.setAttribute("Profile", flag);
      destination = rootDest + function;
    } else if ("questionCreatorBis.jsp".equals(function) || "manageQuestions.jsp".equals(function)) {
      request.setAttribute("Gallery", surveySC.getGalleries());
      request.setAttribute("QuestionStyles", surveySC.getListQuestionStyle());
      request.setAttribute("Profile", flag);
      String view = surveySC.manageQuestionBusiness(function, request);
      request.setAttribute("Questions", surveySC.getSessionQuestions());
      request.setAttribute("SurveyName", surveySC.getSessionSurveyName());
      destination = rootDest + view;
    } else {
      request.setAttribute("Profile", flag);
      destination = rootDest + function;
    }

    if (profileError) {
      String sessionTimeout =
          GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
      destination = sessionTimeout;
    }
    return destination;
  }

  /**
   * Read cookie from anonymous user and set status of anonymous user to allow him to vote or not
   * @param request the current HttpServletRequest
   * @param surveySC the survey session controller
   */
  private void setAnonymousParticipationStatus(HttpServletRequest request,
      SurveySessionController surveySC) {
    surveySC.hasAlreadyParticipated(false);
    String surveyId = request.getParameter("SurveyId");
    if (surveyId != null) {
      Cookie[] cookies = request.getCookies();
      String cookieName = SurveySessionController.COOKIE_NAME + surveyId;
      for (int i = 0; i < cookies.length; i++) {
        Cookie currentCookie = cookies[i];
        if (currentCookie.getName().equals(cookieName)) {
          surveySC.hasAlreadyParticipated(true);
          break;
        }
      }
    }
  }
}