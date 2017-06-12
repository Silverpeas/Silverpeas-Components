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
package com.stratelia.webactiv.survey.control;

import com.stratelia.webactiv.survey.SurveyException;
import com.stratelia.webactiv.survey.notification.SurveyUserNotification;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.exception.DecodingException;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.questioncontainer.answer.model.Answer;
import org.silverpeas.core.questioncontainer.answer.model.AnswerPK;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerDetail;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerSelection;
import org.silverpeas.core.questioncontainer.container.service.QuestionContainerService;
import org.silverpeas.core.questioncontainer.question.model.Question;
import org.silverpeas.core.questioncontainer.question.model.QuestionPK;
import org.silverpeas.core.questioncontainer.result.model.QuestionResult;
import org.silverpeas.core.questioncontainer.result.service.QuestionResultService;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.Link;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.util.AlertUser;
import org.silverpeas.core.webapi.pdc.PdcClassificationEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static org.silverpeas.core.pdc.pdc.model.PdcClassification.aPdcClassificationOfContent;

/**
 * This class contains business layer of survey component
 */
public class SurveySessionController extends AbstractComponentSessionController {

  private QuestionContainerService questionContainerService;
  private QuestionResultService questionResultService;
  private QuestionContainerDetail sessionSurveyUnderConstruction = null;
  private QuestionContainerDetail sessionSurvey = null;
  private List<Question> sessionQuestions = null;
  private Map<String, List<String>> sessionResponses = null;
  private String sessionSurveyId = null;
  private String sessionSurveyName = null;
  public final static int OPENED_SURVEYS_VIEW = 1;
  public final static int CLOSED_SURVEYS_VIEW = 2;
  public final static int INWAIT_SURVEYS_VIEW = 3;
  private int viewType = OPENED_SURVEYS_VIEW;
  private boolean pollingStationMode = false;
  private boolean participationMultipleAllowedForUser = false;
  private boolean hasAlreadyParticipated = false;
  public static final String COOKIE_NAME = "surpoll";
  private List<PdcPosition> newSurveyPositions = null;

  /**
   * Creates new sessionClientController
   * @param mainSessionCtrl
   * @param componentContext
   */
  public SurveySessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "org.silverpeas.survey.multilang.surveyBundle", null,
        "org.silverpeas.survey.surveySettings");
    questionContainerService = QuestionContainerService.getInstance();
    questionResultService = QuestionResultService.get();
  }

  public QuestionContainerService getQuestionContainerService() {
    if (questionContainerService == null) {
      SilverTrace.fatal("survey", "SurveySessionController.getQuestionContainerService()",
          "cannot inject question container BM");
    }
    return questionContainerService;
  }

  public QuestionResultService getQuestionResultService() {
    return questionResultService;
  }

  public boolean isPdcUsed() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("usePdc"));
  }

  /**
   * Return if user can participate more than one time
   * @return true or false
   */
  public boolean isParticipationMultipleAllowedForUser() {
    return participationMultipleAllowedForUser;
  }

  /**
   * Set user status to know if he can participate more than one time
   * @param state
   */
  public void setParticipationMultipleAllowedForUser(boolean state) {
    this.participationMultipleAllowedForUser = state;
  }

  /**
   * Return if participationMultiple is used
   * @return true or false
   */
  public boolean isParticipationMultipleUsed() {
    boolean participationMultipleUsed = false;
    List<String> userMultipleRole = new ArrayList<>();
    userMultipleRole.add("userMultiple");
    // if we have people on userMultiple role, multiple participation is used
    if (getOrganisationController()
        .getUsersIdsByRoleNames(getComponentId(), userMultipleRole).length > 0) {
      participationMultipleUsed = true;
    }
    return participationMultipleUsed;
  }

  /**
   * @return if anonymous mode is authorized
   */
  public boolean isAnonymousModeAuthorized() {
    return isAnonymousModeEnabled() && userIsAnonymous();
  }

  /**
   * @return if anonymous mode is enabled
   */
  public boolean isAnonymousModeEnabled() {
    String value = getComponentParameterValue("useAnonymousMode");
    return value != null && "yes".equalsIgnoreCase(value);
  }

  /**
   * @return if anonymous user has already participated with this ip
   */
  public boolean hasAlreadyParticipated() {
    return hasAlreadyParticipated;
  }

  /**
   * Set status of anonymous user (if he has already participated with this ip)
   * @param state
   */
  public void hasAlreadyParticipated(boolean state) {
    this.hasAlreadyParticipated = state;
  }

  /**
   * Return if currentUser is anonymous
   * @return true or false
   */
  private boolean userIsAnonymous() {
    boolean userIsAnonymous = false;

    UserDetail user = getUserDetail();
    if (user.isAnonymous() && (getComponentId() != null &&
        getOrganisationController().isComponentAvailable(getComponentId(), user.getId()))) {
      userIsAnonymous = true;
    }
    return userIsAnonymous;
  }

  public void setViewType(int viewType) {
    this.viewType = viewType;
  }

  public int getViewType() {
    return viewType;
  }

  /**
   * @return a list of survey of the current instance of component.
   * @throws SurveyException
   */
  public Collection<QuestionContainerHeader> getSurveys() throws SurveyException {

    if (getViewType() == OPENED_SURVEYS_VIEW) {
      return getOpenedSurveys();
    }
    if (getViewType() == CLOSED_SURVEYS_VIEW) {
      return getClosedSurveys();
    }
    if (getViewType() == INWAIT_SURVEYS_VIEW) {
      return getInWaitSurveys();
    }
    return Collections.emptyList();
  }

  public Collection<QuestionContainerHeader> getOpenedSurveys() throws SurveyException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(null, getSpaceId(), getComponentId());
      return questionContainerService.getOpenedQuestionContainers(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.getOpenedSurveys", SurveyException.WARNING,
          "Survey.EX_NO_OPENED_SURVEY", e);
    }
  }

  public Collection<QuestionContainerHeader> getClosedSurveys() throws SurveyException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(null, getSpaceId(), getComponentId());
      return questionContainerService.getClosedQuestionContainers(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.getClosedSurveys", SurveyException.WARNING,
          "Survey.EX_NO_CLOSED_SURVEY", e);
    }
  }

  public Collection<QuestionContainerHeader> getInWaitSurveys() throws SurveyException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(null, getSpaceId(), getComponentId());
      return questionContainerService.getInWaitQuestionContainers(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.getInWaitSurveys", SurveyException.WARNING,
          "Survey.EX_NO_WAIT_SURVEY", e);
    }
  }

  /**
   * @param surveyId the survey identifier
   * @return the question container detail of the survey given in parameter
   * @throws SurveyException
   */
  public QuestionContainerDetail getSurvey(String surveyId) throws SurveyException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      QuestionContainerDetail qc = questionContainerService.getQuestionContainer(qcPK, getUserId());
      qc.getHeader().setNbRegistered(getNbRegistered());
      return qc;
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.getSurvey", SurveyException.WARNING,
          "Survey.EX_NO_SURVEY", "id = " + surveyId, e);
    }
  }

  public QuestionContainerPK createSurvey(QuestionContainerDetail surveyDetail)
      throws SurveyException {

    QuestionContainerPK qcPK = new QuestionContainerPK(null, getSpaceId(), getComponentId());
    try {
      qcPK = questionContainerService.createQuestionContainer(qcPK, surveyDetail, getUserId());
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.createSurvey", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_CREATE", "title = " + surveyDetail.getHeader().getTitle(), e);
    }

    // Classify content if needed
    classifyContent(surveyDetail, qcPK);

    return qcPK;
  }

  /**
   * this method clasify content only when new survey is created Check if a position has been
   * defined in header form then persist it
   * @param surveyDetail the current QuestionContainerDetail
   * @param qcPK the QuestionContainerPK with content identifier
   */
  private void classifyContent(QuestionContainerDetail surveyDetail, QuestionContainerPK qcPK) {
    List<PdcPosition> positions = this.getNewSurveyPositions();
    if (positions != null && !positions.isEmpty()) {
      PdcClassification classification =
          aPdcClassificationOfContent(qcPK.getId(), qcPK.getInstanceId())
              .withPositions(this.getNewSurveyPositions());
      classification.classifyContent(surveyDetail);
    }
  }

  public QuestionContainerPK createSurvey(QuestionContainerDetail surveyDetail, String componentId)
      throws SurveyException {

    QuestionContainerPK qcPK = new QuestionContainerPK(null, null, componentId);
    try {
      qcPK = questionContainerService.createQuestionContainer(qcPK, surveyDetail, getUserId());
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.createSurvey", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_CREATE", "title = " + surveyDetail.getHeader().getTitle(), e);
    }

    return qcPK;
  }

  public void updateSurveyHeader(QuestionContainerHeader surveyHeader, String surveyId)
      throws SurveyException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      surveyHeader.setPK(qcPK);
      questionContainerService.updateQuestionContainerHeader(surveyHeader);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.updateSurveyHeader",
          SurveyException.WARNING, "Survey.EX_PROBLEM_TO_UPDATE_SURVEY",
          "id = " + surveyId + ", title = " + surveyHeader.getTitle(), e);
    }
  }

  public void updateQuestions(Collection<Question> questions, String surveyId)
      throws SurveyException {

    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      questionContainerService.updateQuestions(qcPK, questions);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.updateQuestions", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_UPDATE_QUESTION", "id = " + surveyId, e);
    }
  }

  public void deleteSurvey(String surveyId) throws SurveyException {

    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      questionContainerService.deleteQuestionContainer(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.deleteSurvey", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_DELETE_SURVEY", "id = " + surveyId, e);
    }
  }

  public void deleteVotes(String surveyId) throws SurveyException {

    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      questionContainerService.deleteVotes(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.deleteVotes", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_DELETE_SURVEY", "id = " + surveyId, e);
    }
  }

  public void deleteResponse(String surveyId) throws SurveyException {

    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      questionContainerService.deleteQuestionContainer(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.deleteResponse", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_DELETE_SURVEY", "id = " + surveyId, e);
    }
  }

  public Collection<String> getUserByQuestion(ForeignPK questionPK) {
    return getUserByQuestion(questionPK, true);
  }

  public Collection<String> getUserByQuestion(ForeignPK questionPK, boolean withName) {
    // return list declaration
    Collection<String> users = new LinkedHashSet<>();
    Collection<QuestionResult> results =
        getQuestionResultService().getQuestionResultToQuestion(questionPK);
    for (final QuestionResult result : results) {
      if (result != null) {
        if (withName) {
          String userName = getUserDetail(result.getUserId()).getDisplayedName();
          users.add(result.getUserId() + "/" + userName);
        } else {
          users.add(result.getUserId());
        }
      }
    }
    return users;
  }

  public Collection<QuestionResult> getResultByUser(String userId, ForeignPK questionPK) {
    return getQuestionResultService().getUserQuestionResultsToQuestion(userId, questionPK);
  }

  public Collection<String> getResultByUser(String userId) {
    Collection<QuestionResult> result = new ArrayList<>();
    Collection<String> resultId = new ArrayList<>();
    QuestionContainerDetail survey = getSessionSurvey();
    Collection<Question> questions = survey.getQuestions();
    for (final Question question : questions) {
      Collection<QuestionResult> questionResult = getQuestionResultService()
          .getUserQuestionResultsToQuestion(userId, new ForeignPK(question.getPK()));
      result.addAll(questionResult);
    }
    // Only retrieve response identifiers
    for (final QuestionResult question : result) {
      resultId.add(question.getAnswerPK().getId());
    }
    return resultId;
  }

  public Collection<String> getUserByAnswer(String answerId) {
    return getQuestionResultService().getUsersByAnswer(answerId);
  }

  public void closeSurvey(String surveyId) throws SurveyException {

    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      questionContainerService.closeQuestionContainer(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.closeSurvey", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_CLOSE_SURVEY", "id = " + surveyId, e);
    }
  }

  public void openSurvey(String surveyId) throws SurveyException {

    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      questionContainerService.openQuestionContainer(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.openSurvey", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_OPEN_SURVEY", "id = " + surveyId, e);
    }
  }

  public void recordReply(String surveyId, Map<String, List<String>> reply) throws SurveyException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      questionContainerService.recordReplyToQuestionContainerByUser(qcPK, getUserId(), reply);
    } catch (Exception e) {

      throw new SurveyException("SurveySessionController.recordReply", SurveyException.WARNING,
          "Survey.EX_RECORD_REPLY_FAILED", "id = " + surveyId, e);
    }
  }

  public void recordReply(String surveyId, Map<String, List<String>> reply, String comment,
      boolean isAnonymousComment) throws SurveyException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      questionContainerService
          .recordReplyToQuestionContainerByUser(qcPK, getUserId(), reply, comment,
              isAnonymousComment);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.recordReply", SurveyException.WARNING,
          "Survey.EX_RECORD_REPLY_FAILED", "id = " + surveyId, e);
    }
  }

  public Collection<QuestionResult> getSuggestions(String surveyId) throws SurveyException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      return questionContainerService.getSuggestions(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.getSuggestions", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_RETURN_SUGGESTION", "id = " + surveyId, e);
    }
  }

  public Collection<String> getUsersByAnswer(String answerId) {
    return getUserByAnswer(answerId);
  }

  public Collection<String> getUsersBySurvey(String surveyId) throws SurveyException {
    Collection<String> users = new LinkedHashSet<String>();
    QuestionContainerDetail survey = getSurvey(surveyId);
    Collection<Question> questions = survey.getQuestions();
    for (Question question : questions) {
      ForeignPK questionPK = new ForeignPK(question.getPK());
      users.addAll(getUserByQuestion(questionPK, false));
    }
    return users;
  }

  @Override
  public UserDetail getUserDetail(String userId) {
    return getOrganisationController().getUserDetail(userId);
  }

  private int getNbRegistered() {
    ComponentInst component = getOrganisationController().getComponentInst(getComponentId());
    if (component.isPublic()) {
      String[] allUserIds = getOrganisationController().getAllUsersIds();
      return allUserIds.length;
    } else {
      UserDetail[] registered = getOrganisationController().getAllUsers(getComponentId());
      return registered.length;
    }
  }

  public int getSilverObjectId(String objectId) {
    int silverObjectId = -1;
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(objectId, getSpaceId(), getComponentId());
      silverObjectId = questionContainerService.getSilverObjectId(qcPK);
    } catch (Exception e) {
      SilverTrace.error("survey", "SurveySessionClientController.getSilverObjectId()",
          "root.EX_CANT_GET_LANGUAGE_RESOURCE", "objectId=" + objectId, e);
    }
    return silverObjectId;
  }

  public QuestionContainerDetail getSessionSurveyUnderConstruction() {
    return this.sessionSurveyUnderConstruction;
  }

  public void setSessionSurveyUnderConstruction(QuestionContainerDetail surveyUnderConstruction) {
    this.sessionSurveyUnderConstruction = surveyUnderConstruction;
  }

  public void removeSessionSurveyUnderConstruction() {
    setSessionSurveyUnderConstruction(null);
    setNewSurveyPositions(null);
  }

  public QuestionContainerDetail getSessionSurvey() {
    return this.sessionSurvey;
  }

  public void setSessionSurvey(QuestionContainerDetail survey) {
    this.sessionSurvey = survey;
  }

  public void removeSessionSurvey() {
    setSessionSurvey(null);
  }

  public List<Question> getSessionQuestions() {
    return this.sessionQuestions;
  }

  public void setSessionQuestions(List<Question> questions) {
    this.sessionQuestions = questions;
  }

  public void removeSessionQuestions() {
    setSessionQuestions(null);
  }

  public Map<String, List<String>> getSessionResponses() {
    return this.sessionResponses;
  }

  public void setSessionResponses(Map<String, List<String>> responses) {
    this.sessionResponses = responses;
  }

  public void removeSessionResponses() {
    setSessionResponses(null);
  }

  public String getSessionSurveyId() {
    return this.sessionSurveyId;
  }

  public void setSessionSurveyId(String surveyId) {
    this.sessionSurveyId = surveyId;
  }

  public void removeSessionSurveyId() {
    setSessionSurveyId(null);
  }

  public String getSessionSurveyName() {
    return this.sessionSurveyName;
  }

  public void setSessionSurveyName(String surveyName) {
    this.sessionSurveyName = surveyName;
  }

  public void removeSessionSurveyName() {
    setSessionSurveyName(null);
  }

  private void setNewSurveyPositions(List<PdcPosition> positions) {
    this.newSurveyPositions = positions;
  }

  /**
   * @return the newSurveyPositions
   */
  private List<PdcPosition> getNewSurveyPositions() {
    return newSurveyPositions;
  }

  @Override
  public void close() {
    if (questionContainerService != null) {
      questionContainerService = null;
    }
  }

  // pour la notification
  public String initAlertUser(String surveyId) throws SurveyException {
    AlertUser sel = getAlertUser();
    // Initialisation de AlertUser
    sel.resetAll();
    // set nom de l'espace pour browsebar
    sel.setHostSpaceName(getSpaceLabel());
    // set id du composant pour appel selectionPeas (extra param permettant de filtrer les users
    // ayant acces au composant)
    sel.setHostComponentId(getComponentId());
    // set nom du composant pour browsebar (PairObject(nom_composant, lien_vers_composant))
    // NB : seul le 1er element est actuellement utilisé (alertUserPeas est toujours présenté en
    // popup => pas de lien sur nom du composant)
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), null);
    sel.setHostComponentName(hostComponentName);
    sel.setNotificationMetaData(getAlertNotificationMetaData(surveyId)); // set NotificationMetaData
    // contenant les informations à notifier
    // fin initialisation de AlertUser l'url de nav vers alertUserPeas et demandée à AlertUser et
    // retournée

    return AlertUser.getAlertUserURL();
  }

  private synchronized NotificationMetaData getAlertNotificationMetaData(String surveyId)
      throws SurveyException {
    QuestionContainerPK pk = new QuestionContainerPK(surveyId);
    UserDetail curUser = getUserDetail();
    String senderName = curUser.getDisplayedName();
    QuestionContainerDetail questionDetail = getSurvey(surveyId);
    String url = getSurveyUrl(questionDetail);
    String htmlPath = getQuestionContainerService().getHTMLQuestionPath(questionDetail);

    // Get default resource bundle
    String resource = "org.silverpeas.survey.multilang.surveyBundle";
    LocalizationBundle message =
        ResourceLocator.getLocalizationBundle(resource, DisplayI18NHelper.getDefaultLanguage());

    Map<String, SilverpeasTemplate> templates = new HashMap<>();
    String subject = message.getString("survey.notifSubject");

    NotificationMetaData notifMetaData =
        new NotificationMetaData(NotificationParameters.NORMAL, subject, templates, "alertSurvey");

    List<String> languages = DisplayI18NHelper.getLanguages();
    for (String language : languages) {
      // initialize new resource locator
      message = ResourceLocator.getLocalizationBundle(resource, language);

      // Create a new silverpeas template
      SilverpeasTemplate template = getNewTemplate();
      template.setAttribute("UserDetail", curUser);
      template.setAttribute("userName", senderName);
      template.setAttribute("SurveyDetail", questionDetail);
      template.setAttribute("surveyName", questionDetail.getHeader().getName());
      String surveyDesc = questionDetail.getHeader().getDescription();
      if (StringUtil.isDefined(surveyDesc)) {
        template.setAttribute("surveyDesc", surveyDesc);
      }
      template.setAttribute("htmlPath", htmlPath);
      templates.put(language, template);
      String translation;
      try {
        translation = message.getString("survey.notifSubject");
      } catch (MissingResourceException ex) {
        translation = subject;
      }
      notifMetaData.addLanguage(language, translation, "");

      Link link = new Link(url, message.getString("survey.notifSurveyLinkLabel"));
      notifMetaData.setLink(link, language);
    }
    notifMetaData.setSource(getSpaceLabel() + " - " + getComponentLabel());
    notifMetaData.setComponentId(pk.getInstanceId());
    notifMetaData.setSender(getUserId());
    notifMetaData.displayReceiversInFooter();

    return notifMetaData;
  }

  private String getSurveyUrl(QuestionContainerDetail questionDetail) {
    return URLUtil.getURL(null, getComponentId()) + questionDetail.getHeader().getURL();
  }

  public boolean isPollingStationMode() {
    return pollingStationMode;
  }

  public void setPollingStationMode(boolean pollingStationMode) {
    this.pollingStationMode = pollingStationMode;
  }

  /**
   * @return a list of component instance light of the silverpeas gallery
   */
  public List<ComponentInstLight> getGalleries() {
    List<ComponentInstLight> galleries = new ArrayList<>();
    OrganizationController orgaController = getOrganisationController();
    String[] compoIds = orgaController.getCompoId("gallery");
    for (final String compoId : compoIds) {
      if ("yes".equalsIgnoreCase(
          orgaController.getComponentParameterValue("gallery" + compoId, "viewInWysiwyg"))) {
        ComponentInstLight gallery = orgaController.getComponentInstLight("gallery" + compoId);
        galleries.add(gallery);
      }
    }
    return galleries;
  }

  public String exportSurveyCSV(String surveyId) {
    try {
      QuestionContainerDetail survey = getSurvey(surveyId);
      return questionContainerService.exportCSV(survey, false);
    } catch (Exception e) {
      SilverTrace.error("survey", SurveySessionController.class.getName(),
          "exportSurveyCSV error surveyId=" + surveyId, e);
    }
    return null;
  }

  public void copySurvey(String surveyId) throws ClipboardException, SurveyException {
    QuestionContainerDetail survey = getSurvey(surveyId);
    QuestionContainerSelection questionContainerSelect = new QuestionContainerSelection(survey);
    addClipboardSelection(questionContainerSelect);
  }

  /**
   * Paste surveys which are in the clipboard selection
   * @throws Exception
   */
  public void paste() throws Exception {
    Collection<ClipboardSelection> clipObjects = getClipboardSelectedObjects();
    for (ClipboardSelection clipObject : clipObjects) {
      if (clipObject != null && clipObject.isDataFlavorSupported(
          QuestionContainerSelection.QuestionContainerDetailFlavor)) {
        QuestionContainerDetail survey = (QuestionContainerDetail) clipObject.getTransferData(
            QuestionContainerSelection.QuestionContainerDetailFlavor);
        pasteSurvey(survey);
      }
    }
    clipboardPasteDone();
  }

  /**
   * Paste a survey
   * @param survey the QuestionContanerDetail to paste
   * @throws Exception
   */
  private void pasteSurvey(QuestionContainerDetail survey)
      throws IOException, SurveyException, PdcException {
    String componentId;
    QuestionContainerPK surveyPk = survey.getHeader().getPK();

    if (survey.getHeader().getInstanceId().equals(getComponentId())) {
      // in the same component
      componentId = survey.getHeader().getInstanceId();
    } else {
      componentId = getComponentId();
    }
    Collection<Question> questions = survey.getQuestions();
    for (final Question question : questions) {
      Collection<Answer> answers = question.getAnswers();
      Iterator<Answer> itA = answers.iterator();
      int attachmentSuffix = 0;
      while (itA.hasNext()) {
        Answer answer = itA.next();
        String physicalName = answer.getImage();

        if (StringUtil.isDefined(physicalName)) {
          // copy image
          String type =
              physicalName.substring(physicalName.indexOf('.') + 1, physicalName.length());
          String newPhysicalName =
              Long.toString(new Date().getTime()) + attachmentSuffix + "." + type;
          attachmentSuffix = attachmentSuffix + 1;

          if (survey.getHeader().getInstanceId().equals(getComponentId())) {
            // in the same component
            String absolutePath = FileRepositoryManager.getAbsolutePath(componentId);
            String dir =
                absolutePath + getSettings().getString("imagesSubDirectory") + File.separator;
            FileRepositoryManager.copyFile(dir + physicalName, dir + newPhysicalName);
          } else {
            // in other component
            String fromAbsolutePath =
                FileRepositoryManager.getAbsolutePath(survey.getHeader().getInstanceId());
            String toAbsolutePath = FileRepositoryManager.getAbsolutePath(componentId);
            String fromDir =
                fromAbsolutePath + getSettings().getString("imagesSubDirectory") + File.separator;
            String toDir =
                toAbsolutePath + getSettings().getString("imagesSubDirectory") + File.separator;
            FileRepositoryManager.copyFile(fromDir + physicalName, toDir + newPhysicalName);
          }
          // update answer
          answer.setImage(newPhysicalName);
        }
      }
    }

    QuestionContainerPK toQuestionContainerPk = createSurvey(survey, componentId);

    // Paste positions on Pdc
    final int fromSilverObjectId = getQuestionContainerService().getSilverObjectId(surveyPk);
    final int toSilverObjectId = getQuestionContainerService().getSilverObjectId(toQuestionContainerPk);

    PdcManager.get()
        .copyPositions(fromSilverObjectId, survey.getHeader().getInstanceId(), toSilverObjectId,
            componentId);
  }

  /**
   * @return a SilverpeasTemplate
   */
  protected SilverpeasTemplate getNewTemplate() {
    return SilverpeasTemplateFactory.createSilverpeasTemplateOnComponents("survey");
  }

  /**
   * prepare data inside session controller before sending them to the view layer
   * @param request the current HttpServletRequest
   */
  public void questionsUpdateBusinessModel(HttpServletRequest request) {
    String action = request.getParameter("Action");
    String surveyId = request.getParameter("SurveyId");

    if ("UpQuestion".equals(action)) {
      // Move a question up
      int qId = Integer.parseInt(request.getParameter("QId"));
      List<Question> qV = this.getSessionQuestions();
      Question q1 = qV.get(qId);
      Question q2 = qV.get(qId - 1);
      qV.set(qId - 1, q1);
      qV.set(qId, q2);
      this.setSessionQuestions(qV);
    } else if ("DownQuestion".equals(action)) {
      // Move a question down
      int qId = Integer.parseInt(request.getParameter("QId"));
      List<Question> qV = this.getSessionQuestions();
      Question q1 = qV.get(qId);
      Question q2 = qV.get(qId + 1);
      qV.set(qId + 1, q1);
      qV.set(qId, q2);
      this.setSessionQuestions(qV);
    } else if ("DeleteQuestion".equals(action)) {
      // Delete a question
      int qId = Integer.parseInt(request.getParameter("QId"));
      List<Question> qV = this.getSessionQuestions();
      if (qV != null && qV.size() >= qId + 1) {
        qV.remove(qId);
        this.setSessionQuestions(qV);
      } else {
        StringBuilder message = new StringBuilder();
        message.append("Trying to delete a wrong question, questionIndexToDelete=").append(qId).
            append(", questions list size=").append(qV.size());
        SilverTrace.warn("survey", "SurveySessionController.questionsUpdateBusinessModel", message.
            toString());
      }

    } else if ("SendQuestions".equals(action)) {
      List<Question> qV = this.getSessionQuestions();
      surveyId = this.getSessionSurveyId();
      try {
        this.updateQuestions(qV, surveyId);
        request.setAttribute("UpdateSucceed", Boolean.TRUE);
      } catch (SurveyException e) {
        SilverTrace.error("survey", "SurveySessionController.questionsUpdateBusinessModel",
            "Survey.EX_PROBLEM_TO_UPDATE_QUESTION", e);
        request.setAttribute("UpdateSucceed", Boolean.FALSE);
      }
    }

    if (StringUtil.isDefined(surveyId)) {
      this.removeSessionSurveyId();
      this.removeSessionQuestions();
      this.removeSessionSurveyName();
      List<Question> questionsV = new ArrayList<>();
      QuestionContainerDetail survey = null;
      try {
        survey = this.getSurvey(surveyId);
        Collection<Question> questions = survey.getQuestions();
        // Cast Collection to List
        questionsV = new ArrayList<>(questions);
      } catch (SurveyException e) {
        SilverTrace.error("survey", "SurveySessionController.questionsUpdateBusinessModel",
            "Survey.EX_PROBLEM_TO_OPEN_SURVEY", e);
      }
      this.setSessionQuestions(questionsV);
      this.setSessionSurveyId(surveyId);
      this.setSessionSurveyName(survey.getHeader().getTitle());
    }
  }

  /**
   * Change this code with an enum list of question style using for
   * @return a list of question styles
   */
  public List<String> getListQuestionStyle() {
    List<String> questionStyles = new ArrayList<>();
    if (!this.isPollingStationMode()) {
      questionStyles.add("open");
    }
    questionStyles.add("radio");
    questionStyles.add("checkbox");
    questionStyles.add("list");
    return questionStyles;
  }

  /**
   * @param function
   * @param request
   * @return the view to display
   */
  public String manageQuestionBusiness(String function, HttpServletRequest request) {
    String surveyImageDirectory = FileServerUtils
        .getUrl(this.getComponentId(), "REPLACE_FILE_NAME", "REPLACE_FILE_NAME", "image/gif",
            getSettings().getString("imagesSubDirectory"));
    // Retrieve all the parameter from request
    Parameters parameters = fetchAllParameters(request);

    if ("SendUpdateQuestion".equals(parameters.getAction()) ||
        "SendNewQuestion".equals(parameters.getAction())) {
      processQuestionModification(parameters);
    } else if ("End".equals(parameters.getAction())) {
      processEndSurvey();
    }

    request.setAttribute("ImageDirectory", surveyImageDirectory);
    if (("SendQuestionForm".equals(parameters.getAction())) ||
        "UpdateQuestion".equals(parameters.getAction())) {
      if ("SendQuestionForm".equals(parameters.getAction())) {
        request.setAttribute("NbAnswers", parameters.getAnswerCount());
      } else if ("UpdateQuestion".equals(parameters.getAction())) {
        request.setAttribute("QuestionId", parameters.getQuestionId());
      }
    }

    // Prepare destination request attribute
    String view;
    if ("SendNewQuestion".equals(parameters.getAction()) ||
        "SendUpdateQuestion".equals(parameters.getAction())) {
      request.setAttribute("Action", "UpdateQuestions");
      request.setAttribute("SurveyName", this.getSessionSurveyName());
      view = "questionsUpdate.jsp";
    } else {
      request.setAttribute("Suggestion", parameters.getSuggestion());
      request.setAttribute("Style", parameters.getStyle());
      request.setAttribute("Action", parameters.getAction());
      view = function;
    }
    return view;
  }

  /**
   * Refactoring : get code from view JSP and add it inside controller
   * @param request the HttpServletRequest which contains all the request parameter
   */
  public void sendNewSurveyAction(HttpServletRequest request) {
    String action = request.getParameter("Action");
    if ("SendNewSurvey".equals(action)) {
      String title = request.getParameter("title");
      String description = request.getParameter("description");
      String beginDate = request.getParameter("beginDate");
      String endDate = request.getParameter("endDate");
      String nbQuestions = request.getParameter("nbQuestions");
      String anonymousString = request.getParameter("anonymous");
      String resultMode = request.getParameter("resultMode");
      int resultModeInt = Integer.parseInt(resultMode);
      int resultView = QuestionContainerHeader.TWICE_DISPLAY_RESULTS;
      if (resultModeInt == QuestionContainerHeader.IMMEDIATE_RESULTS) {
        resultView = QuestionContainerHeader.TWICE_DISPLAY_RESULTS;
      } else if (resultModeInt == QuestionContainerHeader.DELAYED_RESULTS) {
        resultView = QuestionContainerHeader.NOTHING_DISPLAY_RESULTS;
      }

      // Anonymous mode -> force all the survey to be anonymous
      if (this.isAnonymousModeEnabled()) {
        anonymousString = "on";
      }
      boolean anonymous =
          StringUtil.isDefined(anonymousString) && "on".equalsIgnoreCase(anonymousString);
      if (StringUtil.isDefined(beginDate)) {
        try {
          beginDate = DateUtil.date2SQLDate(beginDate, this.getLanguage());
        } catch (ParseException e) {
          SilverTrace.error("survey", "SurveySessionControler.sendNewSurveyAction",
              "root.EX_CANT_PARSE_DATE", "impossible to parse begin date, beginDate=" + beginDate,
              e);
          beginDate = null;
        }
      }
      if (StringUtil.isDefined(endDate)) {
        try {
          endDate = DateUtil.date2SQLDate(endDate, this.getLanguage());
        } catch (ParseException e) {
          endDate = null;
        }
      }

      QuestionContainerHeader surveyHeader =
          new QuestionContainerHeader(null, title, description, null, null, beginDate, endDate,
              false, 0, Integer.parseInt(nbQuestions), anonymous, resultModeInt, resultView);
      QuestionContainerDetail surveyDetail = new QuestionContainerDetail();
      surveyDetail.setHeader(surveyHeader);
      // create the positions of the new survey onto the PdC
      String positions = request.getParameter("Positions");
      setNewSurveyPositionsFromJSON(positions);
      this.setSessionSurveyUnderConstruction(surveyDetail);
    }
  }

  /**
   * Set new survey positions (axis classification) from JSON string
   * @param positions: the JSON string positions
   */
  public void setNewSurveyPositionsFromJSON(String positions) {
    if (StringUtil.isDefined(positions)) {
      PdcClassificationEntity surveyClassification = null;
      try {
        surveyClassification = PdcClassificationEntity.fromJSON(positions);
      } catch (DecodingException e) {
        SilverTrace.error("survey", "SurveySessionController.sendNewSurveyPositionsFromJSON",
            "root.EX_IGNORED", "Problem to read JSON, positions=" + positions, e);
      }
      if (surveyClassification != null && !surveyClassification.isUndefined()) {
        List<PdcPosition> pdcPositions = surveyClassification.getPdcPositions();
        this.setNewSurveyPositions(pdcPositions);
      }
    } else {
      this.setNewSurveyPositions(null);
    }
  }

  //pour la notification des résultats
  public void initAlertResultParticipants(QuestionContainerDetail surveyDetail)
      throws SurveyException {
    Collection<String> users = getUsersBySurvey(surveyDetail.getId());
    UserDetail[] participants = new UserDetail[users.size()];
    UserDetail userDetail;
    int i = 0;
    for (String idUser : users) {
      userDetail = getOrganisationController().getUserDetail(idUser);
      participants[i++] = userDetail;
    }
    String htmlPath = getQuestionContainerService().getHTMLQuestionPath(surveyDetail);
    UserNotificationHelper.buildAndSend(
        new SurveyUserNotification(getComponentId(), surveyDetail, htmlPath, getUserDetail(),
            participants));
  }

  //pour la notification des résultats
  public void initAlertResultUsers(QuestionContainerDetail surveyDetail) {
    UserDetail[] users = getOrganisationController().getAllUsers(getComponentId());
    String htmlPath = getQuestionContainerService().getHTMLQuestionPath(surveyDetail);
    UserNotificationHelper.buildAndSend(
        new SurveyUserNotification(getComponentId(), surveyDetail, htmlPath, getUserDetail(),
            users));
  }

  public void saveSynthesisFile(FileItem fileSynthesis) throws SurveyException {
    QuestionContainerDetail survey = this.getSessionSurvey();
    try {
      Date creationDate = new Date();
      String filename = fileSynthesis.getName();
      SimpleAttachment file =
          new SimpleAttachment(FileUtil.getFilename(filename), I18NHelper.defaultLanguage, filename,
              "", fileSynthesis.getSize(), FileUtil.getMimeType(filename), this.getUserId(),
              creationDate, null);
      SimpleDocument document = new SimpleDocument(new SimpleDocumentPK(null, survey.
          getComponentInstanceId()), survey.getId(), 0, false, file);
      AttachmentServiceProvider.getAttachmentService().createAttachment(document, fileSynthesis.
          getInputStream(), true);
    } catch (IOException e) {
      throw new SurveyException("SurveySessionController.saveSynthesisFile",
          SurveyException.WARNING, "Survey.EX_PROBLEM_TO_UPDATE_SURVEY", "id = " + survey.getId(),
          e);
    }
  }

  public void updateSynthesisFile(FileItem newFileSynthesis, String idDocument)
      throws SurveyException {
    SilverTrace
        .info("survey", "SurveySessionController.updateSynthesisFile", "Survey.MSG_ENTRY_METHOD");
    removeSynthesisFile(idDocument);
    saveSynthesisFile(newFileSynthesis);
  }

  public void removeSynthesisFile(String idDocument) {
    SimpleDocumentPK primaryKey = new SimpleDocumentPK(idDocument);
    SimpleDocument document = AttachmentServiceProvider.getAttachmentService()
        .searchDocumentById(primaryKey, I18NHelper.defaultLanguage);
    AttachmentServiceProvider.getAttachmentService().deleteAttachment(document);
  }

  public List<SimpleDocument> getAllSynthesisFile(String surveyId) {
    SimpleDocumentPK surveyForeignKey = new SimpleDocumentPK(surveyId, this.getComponentId());
    return AttachmentServiceProvider.getAttachmentService()
        .listDocumentsByForeignKey(surveyForeignKey, I18NHelper.defaultLanguage);
  }

  public QuestionResult getSuggestion(String userId, String questionId, String answerId)
      throws SurveyException {
    try {
      QuestionPK qPK = new QuestionPK(questionId, getSpaceId(), getComponentId());
      AnswerPK aPK = new AnswerPK(answerId, getSpaceId(), getComponentId());
      return questionContainerService.getSuggestion(userId, qPK, aPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.getSuggestion", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_RETURN_SUGGESTION",
          "userId=" + userId + ", questionId=" + questionId + ", answerId=" + answerId, e);
    }
  }

  private Parameters fetchAllParameters(final HttpServletRequest request) {
    Parameters parameters = new Parameters();
    try {
      List<FileItem> items = HttpRequest.decorate(request).getFileItems();
      Answer answer = null;
      boolean file = false;
      File dir;
      String logicalName;
      String type;
      String physicalName;
      long size;
      int attachmentSuffix = 0;
      for (FileItem item : items) {
        if (item.isFormField()) {
          String mpName = item.getFieldName();
          if ("Action".equals(mpName)) {
            parameters.setAction(item.getString());
          } else if ("question".equals(mpName)) {
            parameters.setQuestion(item.getString(FileUploadUtil.DEFAULT_ENCODING));
          } else if ("nbAnswers".equals(mpName)) {
            parameters.setAnswerCount(item.getString(FileUploadUtil.DEFAULT_ENCODING));
          } else if ("SuggestionAllowed".equals(mpName)) {
            parameters.setSuggestion(item.getString(FileUploadUtil.DEFAULT_ENCODING));
          } else if ("questionStyle".equals(mpName)) {
            parameters.setStyle(item.getString(FileUploadUtil.DEFAULT_ENCODING));
          } else if (mpName.startsWith("answer")) {
            parameters.setAnswerInput(item.getString(FileUploadUtil.DEFAULT_ENCODING));
            answer = new Answer(null, null, parameters.getAnswerInput(), 0, 0, false, "", 0, false,
                null);
            parameters.addAnswer(answer);
          } else if ("suggestionLabel".equals(mpName)) {
            parameters.setAnswerInput(item.getString(FileUploadUtil.DEFAULT_ENCODING));
            answer =
                new Answer(null, null, parameters.getAnswerInput(), 0, 0, false, "", 0, true, null);
            parameters.addAnswer(answer);
          } else if (mpName.startsWith("valueImageGallery")) {
            if (StringUtil.isDefined(item.getString(FileUploadUtil.DEFAULT_ENCODING))) {
              // traiter les images venant de la gallery si pas d'image externe
              if (!file) {
                answer.setImage(item.getString(FileUploadUtil.DEFAULT_ENCODING));
              }
            }
          } else if ("QuestionId".equals(mpName)) {
            parameters.setQuestionId(item.getString(FileUploadUtil.DEFAULT_ENCODING));
          }
          // String value = paramPart.getStringValue();
        } else {
          // it's a file part
          if (FileHelper.isCorrectFile(item)) {
            // the part actually contained a file
            logicalName = item.getName();
            type = logicalName.substring(logicalName.indexOf('.') + 1, logicalName.length());
            physicalName = Long.toString(new Date().getTime()) + attachmentSuffix + "." + type;
            attachmentSuffix = attachmentSuffix + 1;
            dir = new File(FileRepositoryManager.getAbsolutePath(this.getComponentId()) +
                getSettings().getString("imagesSubDirectory") + File.separator + physicalName);
            FileUploadUtil.saveToFile(dir, item);
            size = item.getSize();
            if (size > 0) {
              answer.setImage(physicalName);
              file = true;
            }
          } else {
            // the field did not contain a file
            file = false;
          }
        }
      }
    } catch (UtilException | IOException e) {
      SilverTrace.error("survey", "SurveySessionController.manageQuestionBusiness",
          "root.EX_IGNORED", e);
    }
    return parameters;
  }

  private void processEndSurvey() {
    QuestionContainerDetail surveyDetail = this.getSessionSurveyUnderConstruction();
    // Vector 2 Collection
    List<Question> questionsV = this.getSessionQuestions();
    surveyDetail.setQuestions(questionsV);
  }

  private void processQuestionModification(final Parameters parameters) {
    // Remove answer for open question
    if ("open".equals(parameters.getStyle())) {
      parameters.removeAllAnswers();
      parameters.addAnswer(new Answer(null, null, "", 0, 0, false, "", 0, false, null));
    }
    // Remove the suggestion answer from the list
    if ("0".equals(parameters.getSuggestion())) {
      parameters.getAnswers()
          .stream()
          .filter(Answer::isOpened)
          .findFirst()
          .ifPresent(parameters::removeAnswer);
    }
    if ("SendNewQuestion".equals(parameters.getAction())) {
      Question questionObject =
          new Question(null, null, parameters.getQuestion(), "", "", null, parameters.getStyle(),
              0);
      questionObject.setAnswers(parameters.getAnswers());
      List<Question> questionsV = this.getSessionQuestions();
      questionsV.add(questionObject);
      this.setSessionQuestions(questionsV);
    } else if (parameters.getAction().equals("SendUpdateQuestion")) {
      Question questionObject =
          new Question(null, null, parameters.getQuestion(), "", "", null, parameters.getStyle(),
              0);
      questionObject.setAnswers(parameters.getAnswers());
      List<Question> questionsV = this.getSessionQuestions();
      questionsV.set(Integer.parseInt(parameters.getQuestionId()), questionObject);
      this.setSessionQuestions(questionsV);
    }
  }

  private static class Parameters {

    private String action;
    private String question;
    private String answerCount;
    private String suggestion;
    private String style;
    private String answerInput;
    private List<Answer> answers = new ArrayList<>();
    private String questionId;

    public void setAction(final String action) {
      this.action = action;
    }

    public void setQuestion(final String question) {
      this.question = question;
    }

    public void setAnswerCount(final String answerCount) {
      this.answerCount = answerCount;
    }

    public void setSuggestion(final String suggestion) {
      this.suggestion = suggestion;
    }

    public void setStyle(final String style) {
      this.style = style;
    }

    public void setQuestionId(final String questionId) {
      this.questionId = questionId;
    }

    public void setAnswerInput(final String answerInput) {
      this.answerInput = answerInput;
    }

    public void addAnswer(final Answer answer) {
      this.answers.add(answer);
    }

    public void removeAnswer(final Answer answer) {
      this.answers.remove(answer);
    }

    public void removeAllAnswers() {
      this.answers.clear();
    }

    public List<Answer> getAnswers() {
      return this.answers;
    }

    public String getAction() {
      return action;
    }

    public String getQuestion() {
      return question;
    }

    public String getAnswerCount() {
      return answerCount;
    }

    public String getSuggestion() {
      return suggestion;
    }

    public String getStyle() {
      return style;
    }

    public String getAnswerInput() {
      return answerInput;
    }

    public String getQuestionId() {
      return questionId;
    }
  }
}
