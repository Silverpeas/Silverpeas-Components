/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.components.survey.control;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.survey.SurveyException;
import org.silverpeas.components.survey.notification.SurveyUserNotification;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasException;
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
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
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
import org.silverpeas.core.questioncontainer.result.model.Results;
import org.silverpeas.core.questioncontainer.result.service.QuestionResultService;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.csv.CSVRow;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.export.ExportCSVBuilder;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.webapi.pdc.PdcClassificationEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.pdc.pdc.model.PdcClassification.aPdcClassificationOfContent;

/**
 * This class contains business layer of survey component
 */
public class SurveySessionController extends AbstractComponentSessionController {

  private static final String IMAGES_SUB_DIRECTORY_KEY = "imagesSubDirectory";
  private static final String ACTION_PARAM = "Action";
  private static final String SEND_NEW_QUESTION_ACTION = "SendNewQuestion";
  private static final String SEND_UPDATE_QUESTION_ACTION = "SendUpdateQuestion";
  private transient QuestionContainerService questionContainerService;
  private transient QuestionResultService questionResultService;
  private QuestionContainerDetail sessionSurveyUnderConstruction = null;
  private QuestionContainerDetail sessionSurvey = null;
  private List<Question> sessionQuestions = null;
  private Map<String, List<String>> sessionResponses = null;
  private String sessionSurveyId = null;
  private String sessionSurveyName = null;
  public static final int OPENED_SURVEYS_VIEW = 1;
  public static final int CLOSED_SURVEYS_VIEW = 2;
  public static final int INWAIT_SURVEYS_VIEW = 3;
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
    questionContainerService = QuestionContainerService.get();
    questionResultService = QuestionResultService.get();
  }

  public QuestionContainerService getQuestionContainerService() {
    if (questionContainerService == null) {
      questionContainerService = QuestionContainerService.get();
    }
    return questionContainerService;
  }

  public QuestionResultService getQuestionResultService() {
    if (questionResultService == null) {
      questionResultService = QuestionResultService.get();
    }
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
        getOrganisationController().isComponentAvailableToUser(getComponentId(), user.getId()))) {
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
      return getQuestionContainerService().getOpenedQuestionContainers(qcPK);
    } catch (Exception e) {
      throw new SurveyException("No Opened surveys", e);
    }
  }

  public Collection<QuestionContainerHeader> getClosedSurveys() throws SurveyException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(null, getSpaceId(), getComponentId());
      return getQuestionContainerService().getClosedQuestionContainers(qcPK);
    } catch (Exception e) {
      throw new SurveyException("No closed surveys", e);
    }
  }

  public Collection<QuestionContainerHeader> getInWaitSurveys() throws SurveyException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(null, getSpaceId(), getComponentId());
      return getQuestionContainerService().getInWaitQuestionContainers(qcPK);
    } catch (Exception e) {
      throw new SurveyException("No in-waiting surveys", e);
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
      return getQuestionContainerService().getQuestionContainer(qcPK, getUserId());
    } catch (Exception e) {
      throw new SurveyException("No survey " + surveyId, e);
    }
  }

  public QuestionContainerPK createSurvey(QuestionContainerDetail surveyDetail)
      throws SurveyException {

    QuestionContainerPK qcPK = new QuestionContainerPK(null, getSpaceId(), getComponentId());
    try {
      qcPK = getQuestionContainerService().createQuestionContainer(qcPK, surveyDetail, getUserId());
    } catch (Exception e) {
      throw new SurveyException(
          "Cannot create survey with title " + surveyDetail.getHeader().getTitle(), e);
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
      qcPK = getQuestionContainerService().createQuestionContainer(qcPK, surveyDetail, getUserId());
    } catch (Exception e) {
      throw new SurveyException(
          "Cannot create survey with title " + surveyDetail.getHeader().getTitle(), e);
    }

    return qcPK;
  }

  public void updateSurveyHeader(QuestionContainerHeader surveyHeader, String surveyId)
      throws SurveyException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      surveyHeader.setPK(qcPK);
      getQuestionContainerService().updateQuestionContainerHeader(surveyHeader);
    } catch (Exception e) {
      throw new SurveyException(
          "Cannot update survey of id " + surveyId + " and title " + surveyHeader.getTitle(), e);
    }
  }

  public void updateQuestions(Collection<Question> questions, String surveyId)
      throws SurveyException {

    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      getQuestionContainerService().updateQuestions(qcPK, questions);
    } catch (Exception e) {
      throw new SurveyException("Cannot update the questions of the suvery " + surveyId, e);
    }
  }

  public void deleteSurvey(String surveyId) throws SurveyException {

    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      getQuestionContainerService().deleteQuestionContainer(qcPK);
    } catch (Exception e) {
      throw new SurveyException("Cannot delete survey " + surveyId, e);
    }
  }

  public void deleteVotes(String surveyId) throws SurveyException {

    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      getQuestionContainerService().deleteVotes(qcPK);
    } catch (Exception e) {
      throw new SurveyException("Cannot delete votes of the survey " + surveyId, e);
    }
  }

  public void deleteResponse(String surveyId) throws SurveyException {

    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      getQuestionContainerService().deleteQuestionContainer(qcPK);
    } catch (Exception e) {
      throw new SurveyException("Cannot delete the responses of the survey " + surveyId, e);
    }
  }

  public Collection<String> getUserByQuestion(ResourceReference questionPK) {
    Collection<String> users = new ArrayList<>();
    Collection<QuestionResult> results = getQuestionResultService().getQuestionResultToQuestion(questionPK); for (final QuestionResult result : results) {
      if (result != null) {
        users.add(result.getUserId() + "/" + result.getParticipationId());
      }
    }
    return users;
  }

  public Collection<QuestionResult> getResultByUser(String userId, ResourceReference questionPK) {
    return getQuestionResultService().getUserQuestionResultsToQuestion(userId, questionPK);
  }

  public Collection<String> getResultByUser(String userId) {
    Collection<QuestionResult> result = new ArrayList<>();
    Collection<String> resultId = new ArrayList<>();
    QuestionContainerDetail survey = getSessionSurvey();
    Collection<Question> questions = survey.getQuestions();
    for (final Question question : questions) {
      Collection<QuestionResult> questionResult = getQuestionResultService().getUserQuestionResultsToQuestion(userId, new ResourceReference(question.getPK()));
      result.addAll(questionResult);
    }
    // Only retrieve response identifiers
    for (final QuestionResult question : result) {
      resultId.add(question.getAnswerPK().getId());
    }
    return resultId;
  }

  public Results getResults() {
    QuestionContainerDetail survey = getSessionSurvey();
    Collection<Question> questions = survey.getQuestions();
    List<ResourceReference> pks = new ArrayList<>();
    for (Question question : questions) {
      pks.add(new ResourceReference(question.getPK()));
    }
    return getQuestionResultService().getResultsOfQuestions(pks);
  }

  public Collection<String> getUserByAnswer(String answerId) {
    return getQuestionResultService().getUsersByAnswer(answerId);
  }

  public void closeSurvey(String surveyId) throws SurveyException {

    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      getQuestionContainerService().closeQuestionContainer(qcPK);
    } catch (Exception e) {
      throw new SurveyException("Cannot close the survey " + surveyId, e);
    }
  }

  public void openSurvey(String surveyId) throws SurveyException {

    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      getQuestionContainerService().openQuestionContainer(qcPK);
    } catch (Exception e) {
      throw new SurveyException("Cannot open the survey " + surveyId, e);
    }
  }

  public void recordReply(String surveyId, Map<String, List<String>> reply) throws SurveyException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      getQuestionContainerService().recordReplyToQuestionContainerByUser(qcPK, getUserId(), reply);
    } catch (Exception e) {

      throw new SurveyException("Cannot record a reply for the survey " + surveyId, e);
    }
  }

  public void recordReply(String surveyId, Map<String, List<String>> reply, String comment,
      boolean isAnonymousComment) throws SurveyException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      getQuestionContainerService()
          .recordReplyToQuestionContainerByUser(qcPK, getUserId(), reply, comment,
              isAnonymousComment);
    } catch (Exception e) {
      throw new SurveyException("Cannot record a reply for the survey " + surveyId, e);
    }
  }

  public Collection<QuestionResult> getSuggestions(String surveyId) throws SurveyException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      return getQuestionContainerService().getSuggestions(qcPK);
    } catch (Exception e) {
      throw new SurveyException("Cannot get the suggestions for the survey " + surveyId, e);
    }
  }

  public Collection<String> getUsersByAnswer(String answerId) {
    return getUserByAnswer(answerId);
  }

  public Collection<String> getUsersBySurvey(String surveyId) throws SurveyException {
    Collection<String> users = new LinkedHashSet<>();
    QuestionContainerDetail survey = getSurvey(surveyId);
    Collection<Question> questions = survey.getQuestions();
    for (Question question : questions) {
      ResourceReference questionPK = new ResourceReference(question.getPK());
      users.addAll(getUniqueParticipantIdsByQuestion(questionPK));
    }
    return users;
  }

  private Collection<String> getUniqueParticipantIdsByQuestion(ResourceReference questionPK) {
    Collection<String> users = new LinkedHashSet<>();
    Collection<QuestionResult> results = getQuestionResultService().getQuestionResultToQuestion(questionPK); for (final QuestionResult result : results) {
      if (result != null) {
        users.add(result.getUserId());
      }
    }
    return users;
  }

  @Override
  public UserDetail getUserDetail(String userId) {
    return getOrganisationController().getUserDetail(userId);
  }

  public int getSilverObjectId(String objectId) {
    int silverObjectId = -1;
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(objectId, getSpaceId(), getComponentId());
      silverObjectId = getQuestionContainerService().getSilverObjectId(qcPK);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
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
    return OrganizationController.get().getComponentsWithParameterValue("viewInWysiwyg", "yes");
  }

  public ExportCSVBuilder exportSurveyCSV(String surveyId) {
    ExportCSVBuilder csvBuilder = new ExportCSVBuilder();
    try {
      QuestionContainerDetail survey = getSurvey(surveyId);
      List<CSVRow> rows = getQuestionContainerService().exportCSV(survey, false);
      csvBuilder.addLines(rows);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("CSV export of survey " + surveyId + " failed", e);
    }
    return csvBuilder;
  }

  public void copySurvey(String surveyId) throws SilverpeasException {
    try {
      QuestionContainerDetail survey = getSurvey(surveyId);
      QuestionContainerSelection questionContainerSelect = new QuestionContainerSelection(survey);
      addClipboardSelection(questionContainerSelect);
    } catch (ClipboardException | SurveyException e) {
      throw new SilverpeasException(e);
    }
  }

  /**
   * Paste surveys which are in the clipboard selection
   * @throws Exception
   */
  public void paste() throws SilverpeasException {
    try {
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
    } catch (Exception e) {
      throw new SilverpeasException(e.getMessage(), e);
    }
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
                absolutePath + getSettings().getString(IMAGES_SUB_DIRECTORY_KEY) + File.separator;
            FileRepositoryManager.copyFile(dir + physicalName, dir + newPhysicalName);
          } else {
            // in other component
            String fromAbsolutePath =
                FileRepositoryManager.getAbsolutePath(survey.getHeader().getInstanceId());
            String toAbsolutePath = FileRepositoryManager.getAbsolutePath(componentId);
            String fromDir = fromAbsolutePath + getSettings().getString(IMAGES_SUB_DIRECTORY_KEY) +
                File.separator;
            String toDir =
                toAbsolutePath + getSettings().getString(IMAGES_SUB_DIRECTORY_KEY) + File.separator;
            FileRepositoryManager.copyFile(fromDir + physicalName, toDir + newPhysicalName);
          }
          // update answer
          answer.setImage(newPhysicalName);
        }
      }
    }

    QuestionContainerPK toQuestionContainerPk = createSurvey(survey, componentId);

    // Paste positions on Pdc
    final QuestionContainerService service = getQuestionContainerService();
    final int fromSilverObjectId = service.getSilverObjectId(surveyPk);
    final int toSilverObjectId = service.getSilverObjectId(toQuestionContainerPk);

    PdcManager.get()
        .copyPositions(fromSilverObjectId, survey.getHeader().getInstanceId(), toSilverObjectId,
            componentId);
  }

  /**
   * prepare data inside session controller before sending them to the view layer
   * @param request the current HttpServletRequest
   */
  public void questionsUpdateBusinessModel(HttpServletRequest request) {
    String action = request.getParameter(ACTION_PARAM);
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
            append(", questions list size=").append(qV == null ? "null": qV.size());
        SilverLogger.getLogger(this).warn(message.toString());
      }

    } else if ("SendQuestions".equals(action)) {
      List<Question> qV = this.getSessionQuestions();
      surveyId = this.getSessionSurveyId();
      try {
        this.updateQuestions(qV, surveyId);
        request.setAttribute("UpdateSucceed", Boolean.TRUE);
      } catch (SurveyException e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
        request.setAttribute("UpdateSucceed", Boolean.FALSE);
      }
    }

    if (StringUtil.isDefined(surveyId)) {
      setUpSessionSurvey(surveyId);
    }
  }

  private void setUpSessionSurvey(final String surveyId) {
    this.removeSessionSurveyId();
    this.removeSessionQuestions();
    this.removeSessionSurveyName();
    String surveyName = "";
    List<Question> questionsV = new ArrayList<>();
    QuestionContainerDetail survey = null;
    try {
      survey = this.getSurvey(surveyId);
      Collection<Question> questions = survey.getQuestions();
      // Cast Collection to List
      questionsV.addAll(questions);
      surveyName = survey.getHeader().getTitle();
    } catch (SurveyException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    this.setSessionQuestions(questionsV);
    this.setSessionSurveyId(surveyId);
    this.setSessionSurveyName(surveyName);
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
            getSettings().getString(IMAGES_SUB_DIRECTORY_KEY));
    // Retrieve all the parameter from request
    Parameters parameters = fetchAllParameters(request);

    if (SEND_UPDATE_QUESTION_ACTION.equals(parameters.getAction()) ||
        SEND_NEW_QUESTION_ACTION.equals(parameters.getAction())) {
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
    if (SEND_NEW_QUESTION_ACTION.equals(parameters.getAction()) ||
        SEND_UPDATE_QUESTION_ACTION.equals(parameters.getAction())) {
      request.setAttribute(ACTION_PARAM, "UpdateQuestions");
      request.setAttribute("SurveyName", this.getSessionSurveyName());
      view = "questionsUpdate.jsp";
    } else {
      request.setAttribute("Suggestion", parameters.getSuggestion());
      request.setAttribute("Style", parameters.getStyle());
      request.setAttribute(ACTION_PARAM, parameters.getAction());
      view = function;
    }
    return view;
  }

  /**
   * Refactoring : get code from view JSP and add it inside controller
   * @param request the HttpServletRequest which contains all the request parameter
   */
  public void sendNewSurveyAction(HttpServletRequest request) {
    String action = request.getParameter(ACTION_PARAM);
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
      beginDate = date2SQLDate(beginDate);
      if (StringUtil.isDefined(endDate)) {
        endDate = date2SQLDate(endDate);
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

  private String date2SQLDate(String beginDate) {
    if (StringUtil.isDefined(beginDate)) {
      try {
        beginDate = DateUtil.date2SQLDate(beginDate, this.getLanguage());
      } catch (ParseException e) {
        SilverLogger.getLogger(this).error("Impossible to parse start date " + beginDate, e);
        beginDate = null;
      }
    }
    return beginDate;
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
        SilverLogger.getLogger(this).error("Problem to read JSON, positions: " + positions, e);
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
    UserNotificationHelper.buildAndSend(
        new SurveyUserNotification(surveyDetail, getUserDetail(), participants));
  }

  //pour la notification des résultats
  public void initAlertResultUsers(QuestionContainerDetail surveyDetail) {
    UserDetail[] users = getOrganisationController().getAllUsers(getComponentId());
    UserNotificationHelper.buildAndSend(
        new SurveyUserNotification(surveyDetail, getUserDetail(), users));
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
      throw new SurveyException(
          "Cannot save into a file the synthesis of the survey " + survey.getId(), e);
    }
  }

  public void updateSynthesisFile(FileItem newFileSynthesis, String idDocument)
      throws SurveyException {
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
        .listDocumentsByForeignKey(surveyForeignKey.toResourceReference(), I18NHelper.defaultLanguage);
  }

  public QuestionResult getSuggestion(String userId, String questionId, String answerId)
      throws SurveyException {
    try {
      QuestionPK qPK = new QuestionPK(questionId, getSpaceId(), getComponentId());
      AnswerPK aPK = new AnswerPK(answerId, getSpaceId(), getComponentId());
      return getQuestionContainerService().getSuggestion(userId, qPK, aPK);
    } catch (Exception e) {
      throw new SurveyException(
          "Cannot get the suggestion for user" + userId + ", question " + questionId +
              ", and answer " + answerId, e);
    }
  }

  private Parameters fetchAllParameters(final HttpServletRequest request) {
    Parameters parameters = new Parameters();
    try {
      List<FileItem> items = HttpRequest.decorate(request).getFileItems();
      Answer answer = null;
      boolean file = false;
      int attachmentSuffix = 0;
      for (FileItem item : items) {
        if (item.isFormField()) {
          answer = fetchFormFieldParameters(parameters, answer, file, item);
        } else if (FileHelper.isCorrectFile(item)) {
          // it's a file part

          // the part actually contained a file
          String physicalName = getFilePhysicalName(attachmentSuffix, item);
          attachmentSuffix = attachmentSuffix + 1;
          long size = saveFile(item, physicalName);
          if (size > 0 && answer != null) {
            answer.setImage(physicalName);
            file = true;
          }
        } else {
          // the field did not contain a file
          file = false;
        }
      }
    } catch (RuntimeException | IOException e) {
      SilverLogger.getLogger(this).error(e);
    }
    return parameters;
  }

  private long saveFile(final FileItem item, final String physicalName) throws IOException {
    File dir = new File(FileRepositoryManager.getAbsolutePath(this.getComponentId()) +
        getSettings().getString(IMAGES_SUB_DIRECTORY_KEY) + File.separator + physicalName);
    FileUploadUtil.saveToFile(dir, item);
    return item.getSize();
  }

  private String getFilePhysicalName(final int attachmentSuffix, final FileItem item) {
    String logicalName = item.getName();
    String type = logicalName.substring(logicalName.indexOf('.') + 1, logicalName.length());
    return Long.toString(new Date().getTime()) + attachmentSuffix + "." + type;
  }

  private Answer fetchFormFieldParameters(final Parameters parameters, Answer answer,
      final boolean file, final FileItem item) throws UnsupportedEncodingException {
    String mpName = item.getFieldName();
    if (ACTION_PARAM.equals(mpName)) {
      parameters.setAction(item.getString());
    } else if ("question".equals(mpName)) {
      parameters.setQuestion(item.getString(FileUploadUtil.DEFAULT_ENCODING));
    } else if ("nbAnswers".equals(mpName)) {
      parameters.setAnswerCount(item.getString(FileUploadUtil.DEFAULT_ENCODING));
    } else if ("SuggestionAllowed".equals(mpName)) {
      parameters.setSuggestion(item.getString(FileUploadUtil.DEFAULT_ENCODING));
      answer.setIsOpened(StringUtil.getBooleanValue(parameters.getSuggestion()));
    } else if ("questionStyle".equals(mpName)) {
      parameters.setStyle(item.getString(FileUploadUtil.DEFAULT_ENCODING));
    } else if (mpName.startsWith("answer") || "suggestionLabel".equals(mpName)) {
      parameters.setAnswerInput(item.getString(FileUploadUtil.DEFAULT_ENCODING));
      answer = new Answer(null, null, parameters.getAnswerInput(), 0, false, "", 0, false, null, null);
      parameters.addAnswer(answer);
    } else if (mpName.startsWith("valueImageGallery") &&
        StringUtil.isDefined(item.getString(FileUploadUtil.DEFAULT_ENCODING)) && !file) {
      // traiter les images venant de la gallery si pas d'image externe
      answer.setImage(item.getString(FileUploadUtil.DEFAULT_ENCODING));
    } else if ("QuestionId".equals(mpName)) {
      parameters.setQuestionId(item.getString(FileUploadUtil.DEFAULT_ENCODING));
    }
    return answer;
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
      parameters.addAnswer(new Answer(null, null, "", 0, false, "", 0, false, null, null));
    }
    // Remove the suggestion answer from the list
    if ("0".equals(parameters.getSuggestion())) {
      parameters.getAnswers()
          .stream()
          .filter(Answer::isOpened)
          .findFirst()
          .ifPresent(parameters::removeAnswer);
    }
    if (SEND_NEW_QUESTION_ACTION.equals(parameters.getAction())) {
      Question questionObject =
          new Question(null, null, parameters.getQuestion(), "", "", null, parameters.getStyle(),
              0);
      questionObject.setAnswers(parameters.getAnswers());
      List<Question> questionsV = this.getSessionQuestions();
      questionsV.add(questionObject);
      this.setSessionQuestions(questionsV);
    } else if (SEND_UPDATE_QUESTION_ACTION.equals(parameters.getAction())) {
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
