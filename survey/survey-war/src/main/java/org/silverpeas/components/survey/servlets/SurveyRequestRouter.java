/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.components.survey.servlets;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.survey.SurveyException;
import org.silverpeas.components.survey.control.SurveySessionController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerDetail;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.web.export.ExportCSVBuilder;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Stream;

public class SurveyRequestRouter extends ComponentRequestRouter<SurveySessionController> {

  private static final long serialVersionUID = -1921269596127652643L;

  private static final String COMPONENT_NAME = "survey";
  private static final String SURVEY_ID = "SurveyId";
  private static final String SURVEY = "Survey";
  private static final String PROFILE = "Profile";
  private static final String LIST_DOCUMENT = "ListDocument";

  /**
   * @param profiles current profiles.
   * @return string representation of current user flag
   */
  private String getFlag(String[] profiles) {
    String flag = SilverpeasRole.USER.toString();
    for (String profile : profiles) {
      if (SilverpeasRole.PUBLISHER.isInRole(profile) || ("userMultiple".equals(profile) &&
          !flag.equals(SilverpeasRole.PUBLISHER.toString()))) {
        flag = profile;
      }
      // if admin, return it, we won't find a better profile
      if (SilverpeasRole.ADMIN.isInRole(profile)) {
        return profile;
      }
    }
    return flag;
  }

  /**
   * @param profiles current profiles.
   * @return boolean isParticipationMultipleUser
   */
  public boolean isParticipationMultipleUser(final String[] profiles) {
    return Stream.of(profiles).anyMatch("userMultiple"::equals);
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
   * @param request
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, SurveySessionController surveySC,
      HttpRequest request) {
    String destination = "";
    try {
      String flag = getFlag(surveySC.getUserRoles());
      String rootDest = "/survey/jsp/";
      surveySC.setParticipationMultipleAllowedForUser(isParticipationMultipleUser(surveySC.getUserRoles()));

      if ("pollingStation".equals(surveySC.getComponentRootName())) {
        surveySC.setPollingStationMode(true);
      }
      request.setAttribute("PollingStationMode", surveySC.isPollingStationMode());

      // Set status for this vote or survey
      setAnonymousParticipationStatus(request, surveySC);

      boolean profileError = false;
      if (function.startsWith("portlet")) {
        destination = rootDest + "portlet.jsp?Profile=" + flag;
      } else if (function.startsWith("Main") || function.startsWith("surveyList")) {
        request.setAttribute("PasteEnabled", surveySC.isPasteEnabled());
        destination = rootDest + "surveyList.jsp?Profile=" + flag;
      } else if (function.startsWith("SurveyCreation") || function.startsWith("surveyCreator")) {
        if (flag.equals(SilverpeasRole.ADMIN.toString()) ||
            flag.equals(SilverpeasRole.PUBLISHER.toString())) {
          surveySC.sendNewSurveyAction(request);
          destination = rootDest + "surveyCreator.jsp";
        } else {
          profileError = true;
        }
      } else if ("UpdateSurvey".equals(function)) {
        String surveyId = request.getParameter(SURVEY_ID);
        destination = rootDest + "surveyUpdate.jsp?Action=UpdateSurveyHeader&SurveyId=" + surveyId;
      } else if ("ViewListResult".equals(function)) {
        String answerId = request.getParameter("AnswerId");
        request.setAttribute("Users", surveySC.getUsersByAnswer(answerId));
        request.setAttribute(SURVEY, surveySC.getSessionSurvey());
        destination = rootDest + "answerResult.jsp";
      } else if ("ViewAllUsers".equals(function)) {
        QuestionContainerDetail survey = surveySC.getSessionSurvey();
        request.setAttribute("Users", surveySC.getUsersBySurvey(survey.getId()));
        request.setAttribute(SURVEY, survey);
        destination = rootDest + "answerResult.jsp";
      } else if ("UserResult".equals(function)) {
        String userId = request.getParameter("UserId");
        request.setAttribute("ResultUser", surveySC.getResultByUser(userId));
        request.setAttribute("UserId", userId);
        request.setAttribute(SURVEY, surveySC.getSessionSurvey());
        request.setAttribute(PROFILE, flag);

        destination = rootDest + "resultByUser.jsp";
      } else if (function.startsWith("searchResult")) {
        String id = request.getParameter("Id");
        request.setAttribute(PROFILE, flag);
        setCommonDataToDisplayResult(id, request, surveySC);
        destination = rootDest + "surveyDetail.jsp?Action=ViewCurrentQuestions&SurveyId=" + id;
      } else if ("ExportCSV".equals(function)) {
        String surveyId = request.getParameter(SURVEY_ID);
        ExportCSVBuilder csvBuilder = surveySC.exportSurveyCSV(surveyId);

        destination = csvBuilder.setupRequest(request);
      } else if ("copy".equals(function)) {
        String surveyId = request.getParameter("Id");
        surveySC.copySurvey(surveyId);
        destination = URLUtil.getURL(URLUtil.CMP_CLIPBOARD, null, null) +
            "Idle.jsp?message=REFRESHCLIPBOARD";
      } else if (function.startsWith("paste")) {
        surveySC.paste();
        destination = URLUtil.getURL(URLUtil.CMP_CLIPBOARD, null, null) + "Idle.jsp";
      } else if ("QuestionsUpdate".equals(function) || "questionsUpdate.jsp".equals(function)) {
        String surveyId = request.getParameter(SURVEY_ID);

        if ("QuestionsUpdate".equals(function)) {
          // vérouiller l'enquête
          surveySC.closeSurvey(surveyId);
          // supprimer les participations
          surveySC.deleteVotes(surveyId);
        }

        // Retrieve current action
        surveySC.questionsUpdateBusinessModel(request);

        request.setAttribute("SurveyName", surveySC.getSessionSurveyName());
        request.setAttribute("Questions", surveySC.getSessionQuestions());
        request.setAttribute(PROFILE, flag);
        destination = rootDest + "questionsUpdate.jsp?Action=UpdateQuestions&SurveyId=" + surveyId;
      } else if ("questionCreatorBis.jsp".equals(function) ||
          "manageQuestions.jsp".equals(function)) {
        request.setAttribute("Gallery", surveySC.getGalleries());
        request.setAttribute("QuestionStyles", surveySC.getListQuestionStyle());
        request.setAttribute(PROFILE, flag);
        String view = surveySC.manageQuestionBusiness(function, request);
        request.setAttribute("Questions", surveySC.getSessionQuestions());
        request.setAttribute("SurveyName", surveySC.getSessionSurveyName());
        destination = rootDest + view;
      } else if ("PublishResult".equals(function)) {
        // récupération des paramètres
        List<FileItem> items = request.getFileItems();

        String checkedViewC = FileUploadUtil.getParameter(items, "checkedViewC");
        String checkedViewD = FileUploadUtil.getParameter(items, "checkedViewD");
        String notification = FileUploadUtil.getParameter(items, "notification");
        String destinationUser = FileUploadUtil.getParameter(items, "destination");
        String idSynthesisFile = FileUploadUtil.getParameter(items, "idSynthesisFile");
        String removeSynthesisFile =
            FileUploadUtil.getParameter(items, "removeSynthesisFile");  //yes | no
        FileItem fileSynthesis = FileUploadUtil.getFile(items, "synthesisNewFile");
        if (idSynthesisFile == null && fileSynthesis != null &&
            StringUtil.isDefined(fileSynthesis.getName())) {//Create Document
          surveySC.saveSynthesisFile(fileSynthesis);
        } else if (idSynthesisFile != null && fileSynthesis != null &&
            StringUtil.isDefined(fileSynthesis.getName())) {//Update Document
          surveySC.updateSynthesisFile(fileSynthesis, idSynthesisFile);
        } else if (idSynthesisFile != null && fileSynthesis != null &&
            !StringUtil.isDefined(fileSynthesis.getName()) &&
            "yes".equals(removeSynthesisFile)) {//Delete Document
          surveySC.removeSynthesisFile(idSynthesisFile);
        }

        QuestionContainerDetail survey = surveySC.getSessionSurvey();
        String surveyId = survey.getId();
        QuestionContainerHeader surveyHeader = survey.getHeader();

        if (checkedViewC == null && checkedViewD == null) {
          surveyHeader.setResultView(QuestionContainerHeader.NOTHING_DISPLAY_RESULTS);
        } else if ("on".equals(checkedViewC) && "on".equals(checkedViewD)) {
          //C && D
          surveyHeader.setResultView(QuestionContainerHeader.TWICE_DISPLAY_RESULTS);
        } else {
          //C || D
          if ("on".equals(checkedViewC)) {
            surveyHeader.setResultView(QuestionContainerHeader.CLASSIC_DISPLAY_RESULTS);
          } else if ("on".equals(checkedViewD)) {
            surveyHeader.setResultView(QuestionContainerHeader.DETAILED_DISPLAY_RESULTS);
          }
        }
        surveySC.updateSurveyHeader(surveyHeader, surveyId);

        if ("1".equals(notification)) {
          //notifier uniquement les utilisateurs ayant participé
          surveySC.initAlertResultParticipants(survey);
        } else if ("2".equals(notification)) {
          //notifier tous les utilisateurs qui pouvaient participer
          surveySC.initAlertResultUsers(survey);
        }
        request.setAttribute(PROFILE, flag);
        setCommonDataToDisplayResult(surveyId, request, surveySC);
        destination = rootDest + destinationUser;
      } else if (function.startsWith("surveyDetail")) {
        String surveyId = request.getParameter(SURVEY_ID);
        request.setAttribute(PROFILE, flag);
        setCommonDataToDisplayResult(surveyId, request, surveySC);
        destination = rootDest + function;
      } else {
        request.setAttribute(PROFILE, flag);
        destination = rootDest + function;
      }

      if (profileError) {
        destination = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
      }

    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
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
    String surveyId = request.getParameter(SURVEY_ID);
    if (surveyId != null) {
      Cookie[] cookies = request.getCookies();
      String cookieName = SurveySessionController.COOKIE_NAME + surveyId;
      for (Cookie currentCookie : cookies) {
        if (currentCookie.getName().equals(cookieName)) {
          surveySC.hasAlreadyParticipated(true);
          break;
        }
      }
    }
  }

  private void setCommonDataToDisplayResult(String surveyId, HttpServletRequest request,
      SurveySessionController surveySC) throws SurveyException{
    if (StringUtil.isDefined(surveyId)) {
      request.setAttribute(LIST_DOCUMENT, surveySC.getAllSynthesisFile(surveyId));

      String resultDisplayMode = request.getParameter("Choice");
      if (StringUtil.isDefined(resultDisplayMode)) {
        request.setAttribute("ResultDisplayMode", resultDisplayMode);
      } else {
        request.setAttribute("ResultDisplayMode", "D");
        if (surveySC.getSurvey(surveyId).getHeader().getNbVoters() > 50) {
          request.setAttribute("ResultDisplayMode", "C");
        }
      }
    }
  }
}
