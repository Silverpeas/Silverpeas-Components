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
package com.stratelia.webactiv.survey.control;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.ejb.EJBException;
import javax.ejb.RemoveException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.clipboard.ClipboardSelection;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.alertUser.AlertUser;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.survey.SurveyException;
import com.stratelia.webactiv.survey.servlets.SurveyRequestRouter;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.answer.model.Answer;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.question.model.Question;
import com.stratelia.webactiv.util.questionContainer.control.QuestionContainerBm;
import com.stratelia.webactiv.util.questionContainer.control.QuestionContainerBmHome;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerPK;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerSelection;
import com.stratelia.webactiv.util.questionResult.control.QuestionResultBm;
import com.stratelia.webactiv.util.questionResult.control.QuestionResultBmHome;
import com.stratelia.webactiv.util.questionResult.model.QuestionResult;

/**
 * This class contains business layer of survey component
 */
public class SurveySessionController extends AbstractComponentSessionController {

  private QuestionContainerBm questionContainerBm = null;
  private QuestionResultBm questionResultBm = null;
  private QuestionContainerDetail sessionSurveyUnderConstruction = null;
  private QuestionContainerDetail sessionSurvey = null;
  private List<Question> sessionQuestions = null;
  private Hashtable<String, Vector<String>> sessionResponses = null;
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

  /**
   * Creates new sessionClientController
   * @param mainSessionCtrl
   * @param componentContext
   */
  public SurveySessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "com.stratelia.webactiv.survey.multilang.surveyBundle");
    setQuestionContainerBm();
    setQuestionResultBm();

  }

  /**
   * Instantiate a question Container Bean Manager
   */
  private void setQuestionContainerBm() {
    if (questionContainerBm == null) {
      try {
        QuestionContainerBmHome questionContainerBmHome =
            (QuestionContainerBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.QUESTIONCONTAINERBM_EJBHOME, QuestionContainerBmHome.class);
        this.questionContainerBm = questionContainerBmHome.create();
      } catch (Exception e) {
        throw new EJBException(e.getMessage(), e);
      }
    }
  }

  public QuestionContainerBm getQuestionContainerBm() {
    return questionContainerBm;
  }

  private void setQuestionResultBm() {
    if (questionResultBm == null) {
      try {
        QuestionResultBmHome questionResultBmHome =
            (QuestionResultBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.QUESTIONRESULTBM_EJBHOME,
            QuestionResultBmHome.class);
        this.questionResultBm = questionResultBmHome.create();
      } catch (Exception e) {
        throw new EJBException(e.getMessage(), e);
      }
    }
  }

  public QuestionResultBm getQuestionResultBm() {
    return questionResultBm;
  }

  public boolean isPdcUsed() {
    String value = getComponentParameterValue("usePdc");
    if (value != null) {
      return "yes".equals(value.toLowerCase());
    }
    return false;
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
    List<String> userMultipleRole = new ArrayList<String>();
    userMultipleRole.add("userMultiple");
    // if we have people on userMultiple role, multiple participation is used
    if (getOrganizationController().getUsersIdsByRoleNames(getComponentId(), userMultipleRole).length > 0) {
      participationMultipleUsed = true;
    }
    return participationMultipleUsed;
  }

  /**
   * Return if anonymous mode is authorized
   * @return
   */
  public boolean isAnonymousModeAuthorized() {
    return isAnonymousModeEnabled() && userIsAnonymous();
  }

  /**
   * Return if anonymous mode is enabled
   * @return
   */
  public boolean isAnonymousModeEnabled() {
    String value = getComponentParameterValue("useAnonymousMode");
    if (value != null) {
      return "yes".equals(value.toLowerCase());
    }
    return false;
  }

  /**
   * Return if anonymous user has already participated with this ip
   * @return
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
    if (user.isAnonymous()) {
      if (getComponentId() != null
          && getOrganizationController().isComponentAvailable(getComponentId(), user.getId())) {
        userIsAnonymous = true;
      }
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
    SilverTrace.info("Survey", "SurveySessionController.getSurveys",
        "Survey.MSG_ENTRY_METHOD");
    if (getViewType() == OPENED_SURVEYS_VIEW) {
      return getOpenedSurveys();
    }
    if (getViewType() == CLOSED_SURVEYS_VIEW) {
      return getClosedSurveys();
    }
    if (getViewType() == INWAIT_SURVEYS_VIEW) {
      return getInWaitSurveys();
    }
    return null;
  }

  public Collection<QuestionContainerHeader> getOpenedSurveys() throws SurveyException {
    SilverTrace.info("Survey", "SurveySessionController.getOpenedSurveys",
        "Survey.MSG_ENTRY_METHOD");
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(null, getSpaceId(), getComponentId());
      return questionContainerBm.getOpenedQuestionContainers(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.getOpenedSurveys",
          SurveyException.WARNING, "Survey.EX_NO_OPENED_SURVEY", e);
    }
  }

  public Collection<QuestionContainerHeader> getClosedSurveys() throws SurveyException {
    SilverTrace.info("Survey", "SurveySessionController.getClosedSurveys",
        "Survey.MSG_ENTRY_METHOD");
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(null, getSpaceId(), getComponentId());
      return questionContainerBm.getClosedQuestionContainers(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.getClosedSurveys",
          SurveyException.WARNING, "Survey.EX_NO_CLOSED_SURVEY", e);
    }
  }

  public Collection<QuestionContainerHeader> getInWaitSurveys() throws SurveyException {
    SilverTrace.info("Survey", "SurveySessionController.getInWaitSurveys",
        "Survey.MSG_ENTRY_METHOD");
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(null, getSpaceId(), getComponentId());
      return questionContainerBm.getInWaitQuestionContainers(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.getInWaitSurveys",
          SurveyException.WARNING, "Survey.EX_NO_WAIT_SURVEY", e);
    }
  }

  /**
   * @param surveyId the survey identifier
   * @return the question container detail of the survey given in parameter
   * @throws SurveyException
   */
  public QuestionContainerDetail getSurvey(String surveyId) throws SurveyException {
    SilverTrace.info("Survey", "SurveySessionController.getSurvey",
        "Survey.MSG_ENTRY_METHOD", "id = " + surveyId);
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      QuestionContainerDetail qc = questionContainerBm.getQuestionContainer(qcPK, getUserId());
      qc.getHeader().setNbRegistered(getNbRegistered());
      return qc;
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.getSurvey", SurveyException.WARNING,
          "Survey.EX_NO_SURVEY", "id = " + surveyId, e);
    }
  }

  public QuestionContainerPK createSurvey(QuestionContainerDetail surveyDetail)
      throws SurveyException {
    SilverTrace.info("Survey", "SurveySessionController.createSurvey", "Survey.MSG_ENTRY_METHOD",
        "title = " + surveyDetail.getHeader().getTitle());
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(null, getSpaceId(), getComponentId());
      return questionContainerBm.createQuestionContainer(qcPK, surveyDetail, getUserId());
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.createSurvey", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_CREATE", "title = " + surveyDetail.getHeader().getTitle(), e);
    }
  }

  public QuestionContainerPK createSurvey(QuestionContainerDetail surveyDetail, String componentId)
      throws SurveyException {
    SilverTrace.info("Survey", "SurveySessionController.createSurvey",
        "Survey.MSG_ENTRY_METHOD", "title = " + surveyDetail.getHeader().getTitle());
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(null, null, componentId);
      return questionContainerBm.createQuestionContainer(qcPK, surveyDetail, getUserId());
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.createSurvey", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_CREATE", "title = " + surveyDetail.getHeader().getTitle(), e);
    }
  }

  public void updateSurveyHeader(QuestionContainerHeader surveyHeader, String surveyId)
      throws SurveyException {
    SilverTrace.info("Survey", "SurveySessionController.updateSurveyHeader",
        "Survey.MSG_ENTRY_METHOD", "id = " + surveyId + ", title = " + surveyHeader.getTitle());
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      surveyHeader.setPK(qcPK);
      questionContainerBm.updateQuestionContainerHeader(surveyHeader);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.updateSurveyHeader",
          SurveyException.WARNING, "Survey.EX_PROBLEM_TO_UPDATE_SURVEY", "id = " + surveyId
          + ", title = " + surveyHeader.getTitle(), e);
    }
  }

  public void updateQuestions(Collection<Question> questions, String surveyId)
      throws SurveyException {
    SilverTrace.info("Survey", "SurveySessionController.updateQuestions",
        "Survey.MSG_ENTRY_METHOD", "id = " + surveyId);
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      questionContainerBm.updateQuestions(qcPK, questions);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.updateQuestions", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_UPDATE_QUESTION", "id = " + surveyId, e);
    }
  }

  public void deleteSurvey(String surveyId) throws SurveyException {
    SilverTrace.info("Survey", "SurveySessionController.deleteSurvey",
        "Survey.MSG_ENTRY_METHOD", "id = " + surveyId);
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      questionContainerBm.deleteQuestionContainer(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.deleteSurvey", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_DELETE_SURVEY", "id = " + surveyId, e);
    }
  }

  public void deleteVotes(String surveyId) throws SurveyException {
    SilverTrace.info("Survey", "SurveySessionController.deleteVotes",
        "Survey.MSG_ENTRY_METHOD", "id = " + surveyId);
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      questionContainerBm.deleteVotes(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.deleteSurvey", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_DELETE_SURVEY", "id = " + surveyId, e);
    }
  }

  public void deleteResponse(String surveyId) throws SurveyException {
    SilverTrace.info("Survey", "SurveySessionController.deleteQuestions",
        "Survey.MSG_ENTRY_METHOD", "id = " + surveyId);
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      questionContainerBm.deleteQuestionContainer(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.deleteSurvey", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_DELETE_SURVEY", "id = " + surveyId, e);
    }
  }

  public Collection<String> getUserByQuestion(ForeignPK questionPK) throws RemoteException {
    return getUserByQuestion(questionPK, true);
  }

  public Collection<String> getUserByQuestion(ForeignPK questionPK, boolean withName)
      throws RemoteException {
    // return list declaration
    Collection<String> users = new LinkedHashSet<String>();
    Collection<QuestionResult> results =
        getQuestionResultBm().getQuestionResultToQuestion(questionPK);
    Iterator<QuestionResult> it = results.iterator();
    while (it.hasNext()) {
      QuestionResult result = it.next();

      if (result != null) {
        String userName = "";
        if (withName) {
          userName = getUserDetail(result.getUserId()).getDisplayedName();
          users.add(result.getUserId() + "/" + userName);
        } else {
          users.add(result.getUserId());
        }
      }
    }
    return users;
  }

  public Collection<QuestionResult> getResultByUser(String userId, ForeignPK questionPK)
      throws RemoteException {
    return getQuestionResultBm().getUserQuestionResultsToQuestion(userId, questionPK);
  }

  public Collection<String> getResultByUser(String userId) throws RemoteException {
    Collection<QuestionResult> result = new ArrayList<QuestionResult>();
    Collection<String> resultId = new ArrayList<String>();
    QuestionContainerDetail survey = getSessionSurvey();
    Collection<Question> questions = survey.getQuestions();
    Iterator<Question> it = questions.iterator();
    while (it.hasNext()) {
      Question question = it.next();
      Collection<QuestionResult> questionResult =
          getQuestionResultBm().getUserQuestionResultsToQuestion(userId,
          new ForeignPK(question.getPK()));
      result.addAll(questionResult);
    }
    // Only retrieve response identifiers
    Iterator<QuestionResult> itR = result.iterator();
    while (itR.hasNext()) {
      QuestionResult question = itR.next();
      resultId.add(question.getAnswerPK().getId());
    }
    return resultId;
  }

  public Collection<String> getUserByAnswer(String answerId) throws RemoteException {
    return getQuestionResultBm().getUsersByAnswer(answerId);
  }

  public void closeSurvey(String surveyId) throws SurveyException {
    SilverTrace.info("Survey", "SurveySessionController.closeSurvey",
        "Survey.MSG_ENTRY_METHOD", "id = " + surveyId);
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      questionContainerBm.closeQuestionContainer(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.closeSurvey", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_CLOSE_SURVEY", "id = " + surveyId, e);
    }
  }

  public void openSurvey(String surveyId) throws SurveyException {
    SilverTrace.info("Survey", "SurveySessionController.openSurvey",
        "Survey.MSG_ENTRY_METHOD", "id = " + surveyId);
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      questionContainerBm.openQuestionContainer(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.openSurvey", SurveyException.WARNING,
      "Survey.EX_PROBLEM_TO_OPEN_SURVEY", "id = " + surveyId, e);
    }
  }

  public void recordReply(String surveyId, Hashtable<String, Vector<String>> reply) throws
      SurveyException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      questionContainerBm.recordReplyToQuestionContainerByUser(qcPK, getUserId(), reply);
    } catch (Exception e) {

      throw new SurveyException("SurveySessionController.recordReply", SurveyException.WARNING,
          "Survey.EX_RECORD_REPLY_FAILED", "id = " + surveyId, e);
    }
  }

  public void recordReply(String surveyId, Hashtable<String, Vector<String>> reply, String comment,
      boolean isAnonymousComment) throws SurveyException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      questionContainerBm.recordReplyToQuestionContainerByUser(qcPK, getUserId(), reply, comment,
          isAnonymousComment);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.recordReply", SurveyException.WARNING,
          "Survey.EX_RECORD_REPLY_FAILED", "id = " + surveyId, e);
    }
  }

  public Collection<QuestionResult> getSuggestions(String surveyId) throws SurveyException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(surveyId, getSpaceId(), getComponentId());
      return questionContainerBm.getSuggestions(qcPK);
    } catch (Exception e) {
      throw new SurveyException("SurveySessionController.getSuggestions", SurveyException.WARNING,
          "Survey.EX_PROBLEM_TO_RETURN_SUGGESTION", "id = " + surveyId, e);
    }
  }

  public Collection<String> getUsersByAnswer(String answerId) throws RemoteException {
    return getUserByAnswer(answerId);
  }

  public Collection<String> getUsersBySurvey(String surveyId) throws RemoteException,
      SurveyException {
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
    return getOrganizationController().getUserDetail(userId);
  }

  private int getNbRegistered() {
    ComponentInst component = getOrganizationController().getComponentInst(getComponentId());
    if (component.isPublic()) {
      String[] allUserIds = getOrganizationController().getAllUsersIds();
      return allUserIds.length;
    } else {
      UserDetail[] registered = getOrganizationController().getAllUsers(getComponentId());
      return registered.length;
    }
  }

  public int getSilverObjectId(String objectId) {
    int silverObjectId = -1;
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(objectId, getSpaceId(), getComponentId());
      silverObjectId = questionContainerBm.getSilverObjectId(qcPK);
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

  public Hashtable<String, Vector<String>> getSessionResponses() {
    return this.sessionResponses;
  }

  public void setSessionResponses(Hashtable<String, Vector<String>> responses) {
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

  public void close() {
    try {
      if (questionContainerBm != null) {
        questionContainerBm.remove();
      }
    } catch (RemoteException e) {
      SilverTrace.error("surveySession", "SurveySessionController.close", "", e);
    } catch (RemoveException e) {
      SilverTrace.error("surveySession", "SurveySessionController.close", "", e);
    }
  }

  // pour la notification
  public String initAlertUser(String surveyId) throws RemoteException, SurveyException {
    AlertUser sel = getAlertUser();
    // Initialisation de AlertUser
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel()); // set nom de l'espace pour browsebar
    sel.setHostComponentId(getComponentId()); // set id du composant pour appel selectionPeas (extra
    // param permettant de filtrer les users ayant acces
    // au composant)
    PairObject hostComponentName = new PairObject(getComponentLabel(), null); // set nom du
    // composant pour
    // browsebar
    // (PairObject(nom_composant,
    // lien_vers_composant))
    // NB : seul le 1er
    // element est
    // actuellement
    // utilisé
    // (alertUserPeas est
    // toujours présenté
    // en popup => pas de
    // lien sur nom du
    // composant)
    sel.setHostComponentName(hostComponentName);
    SilverTrace.debug("Survey", "SurveySessionController.initAlertUser()",
        "root.MSG_GEN_PARAM_VALUE", "name = " + hostComponentName + " componentId="
        + getComponentId());
    sel.setNotificationMetaData(getAlertNotificationMetaData(surveyId)); // set NotificationMetaData
    // contenant les
    // informations à notifier
    // fin initialisation de AlertUser
    // l'url de nav vers alertUserPeas et demandée à AlertUser et retournée

    return AlertUser.getAlertUserURL();
  }

  private synchronized NotificationMetaData getAlertNotificationMetaData(String surveyId)
      throws RemoteException, SurveyException {
    QuestionContainerPK pk = new QuestionContainerPK(surveyId);
    UserDetail curUser = getUserDetail();
    String senderName = curUser.getDisplayedName();
    QuestionContainerDetail questionDetail = getSurvey(surveyId);
    SilverTrace.debug("Survey", "SurveySessionController.getAlertNotificationMetaData()",
        "root.MSG_GEN_PARAM_VALUE", "survey = " + questionDetail.toString());
    String htmlPath = getQuestionContainerBm().getHTMLQuestionPath(questionDetail);

    // Get default resource bundle
    String resource = "com.stratelia.webactiv.survey.multilang.surveyBundle";
    ResourceLocator message = new ResourceLocator(resource, I18NHelper.defaultLanguage);

    Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
    String subject = message.getString("survey.notifSubject");

    SilverTrace.debug("Survey", "SurveySessionController.getAlertNotificationMetaData()",
        "root.MSG_GEN_PARAM_VALUE", "sujet = " + subject);

    NotificationMetaData notifMetaData =
        new NotificationMetaData(NotificationParameters.NORMAL, subject, templates, "alertSurvey");

    List<String> languages = DisplayI18NHelper.getLanguages();
    for (String language : languages) {
      // initialize new resource locator
      message = new ResourceLocator(resource, language);

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
      // template.setAttribute("message", message);
      template.setAttribute("htmlPath", htmlPath);
      templates.put(language, template);
      notifMetaData.addLanguage(language, message.getString("survey.notifSubject", subject), "");
    }
    notifMetaData.setSource(getSpaceLabel() + " - " + getComponentLabel());
    notifMetaData.setLink(getSurveyUrl(questionDetail));
    notifMetaData.setComponentId(pk.getInstanceId());
    notifMetaData.setSender(getUserId());
    return notifMetaData;
  }

  private String getSurveyUrl(QuestionContainerDetail questionDetail) {
    return URLManager.getURL(null, getComponentId()) + questionDetail.getHeader().getURL();
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
    List<ComponentInstLight> galleries = null;
    OrganizationController orgaController = getOrganizationController();
    String[] compoIds = orgaController.getCompoId("gallery");
    for (int c = 0; c < compoIds.length; c++) {
      if ("yes".equalsIgnoreCase(orgaController.getComponentParameterValue("gallery" + compoIds[c],
          "viewInWysiwyg"))) {
        if (galleries == null) {
          galleries = new ArrayList<ComponentInstLight>();
        }

        ComponentInstLight gallery = orgaController.getComponentInstLight("gallery" + compoIds[c]);
        galleries.add(gallery);
      }
    }
    return galleries;
  }

  public String exportSurveyCSV(String surveyId) {
    QuestionContainerDetail survey = null;
    try {
      survey = getSurvey(surveyId);
      return questionContainerBm.exportCSV(survey, false);
    } catch (Exception e) {
      SilverTrace.error("Survey", SurveySessionController.class.getName(),
          "exportSurveyCSV error surveyId=" + surveyId, e);
    }
    return null;
  }

  public void copySurvey(String surveyId) throws RemoteException, SurveyException {
    QuestionContainerDetail survey = getSurvey(surveyId);
    QuestionContainerSelection questionContainerSelect = new QuestionContainerSelection(survey);

    addClipboardSelection((ClipboardSelection) questionContainerSelect);
  }

  /**
   * Paste surveys which are in the clipboard selection
   * @throws Exception
   */
  public void paste() throws Exception {
    Collection<ClipboardSelection> clipObjects = getClipboardSelectedObjects();
    Iterator<ClipboardSelection> clipObjectIterator = clipObjects.iterator();
    while (clipObjectIterator.hasNext()) {
      ClipboardSelection clipObject = clipObjectIterator.next();
      if (clipObject != null) {
        if (clipObject.isDataFlavorSupported(
            QuestionContainerSelection.QuestionContainerDetailFlavor)) {
          QuestionContainerDetail survey =
              (QuestionContainerDetail) clipObject.getTransferData(
              QuestionContainerSelection.QuestionContainerDetailFlavor);
          pasteSurvey(survey);
        }
      }
    }
    clipboardPasteDone();
  }

  /**
   * Paste a survey
   * @param survey the QuestionContanerDetail to paste
   * @throws Exception
   */
  private void pasteSurvey(QuestionContainerDetail survey) throws Exception {
    String componentId = "";
    if (survey.getHeader().getInstanceId().equals(getComponentId())) {
      // in the same component
      componentId = survey.getHeader().getInstanceId();
    } else {
      componentId = getComponentId();
    }
    Collection<Question> questions = survey.getQuestions();
    Iterator<Question> itQ = questions.iterator();
    while (itQ.hasNext()) {
      Question question = itQ.next();
      Collection<Answer> answers = question.getAnswers();
      Iterator<Answer> itA = answers.iterator();
      int attachmentSuffix = 0;
      while (itA.hasNext()) {
        Answer answer = itA.next();
        String physicalName = answer.getImage();

        if (StringUtil.isDefined(physicalName)) {
          // copy image
          ResourceLocator settings =
              new ResourceLocator("com.stratelia.webactiv.survey.surveySettings", "");
          String type =
              physicalName.substring(physicalName.indexOf('.') + 1, physicalName.length());
          String newPhysicalName =
              new Long(new Date().getTime()).toString() + attachmentSuffix + "." + type;
          attachmentSuffix = attachmentSuffix + 1;

          if (survey.getHeader().getInstanceId().equals(getComponentId())) {
            // in the same component
            String absolutePath = FileRepositoryManager.getAbsolutePath(componentId);
            String dir = absolutePath + settings.getString("imagesSubDirectory") + File.separator;
            FileRepositoryManager.copyFile(dir + physicalName, dir + newPhysicalName);
          } else {
            // in other component
            String fromAbsolutePath =
                FileRepositoryManager.getAbsolutePath(survey.getHeader().getInstanceId());
            String toAbsolutePath = FileRepositoryManager.getAbsolutePath(componentId);
            String fromDir =
                fromAbsolutePath + settings.getString("imagesSubDirectory") + File.separator;
            String toDir =
                toAbsolutePath + settings.getString("imagesSubDirectory") + File.separator;
            FileRepositoryManager.copyFile(fromDir + physicalName, toDir + newPhysicalName);
          }
          // update answer
          answer.setImage(newPhysicalName);
        }
      }
    }
    createSurvey(survey, componentId);
  }

  /**
   * @return a SilverpeasTemplate
   */
  protected SilverpeasTemplate getNewTemplate() {
    ResourceLocator rs =
        new ResourceLocator("com.stratelia.webactiv.survey.surveySettings", "");
    Properties templateConfiguration = new Properties();
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, rs.getString(
        "templatePath"));
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, rs.getString(
        "customersTemplatePath"));
    return SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfiguration);
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
      action = "UpdateQuestions";
    } else if ("DownQuestion".equals(action)) {
      // Move a question down
      int qId = Integer.parseInt(request.getParameter("QId"));
      List<Question> qV = this.getSessionQuestions();
      Question q1 = qV.get(qId);
      Question q2 = qV.get(qId + 1);
      qV.set(qId + 1, q1);
      qV.set(qId, q2);
      this.setSessionQuestions(qV);
      action = "UpdateQuestions";
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
        SilverTrace.warn("Survey", "SurveySessionController.questionsUpdateBusinessModel", message.
            toString());
      }
      action = "UpdateQuestions";
    } else if ("SendQuestions".equals(action)) {
      List<Question> qV = this.getSessionQuestions();
      surveyId = this.getSessionSurveyId();
      try {
        this.updateQuestions(qV, surveyId);
        request.setAttribute("UpdateSucceed", Boolean.TRUE);
      } catch (SurveyException e) {
        SilverTrace.error(this.getComponentName(), SurveyRequestRouter.class.getName(),
            "update question error", e);
        request.setAttribute("UpdateSucceed", Boolean.FALSE);
      }
      action = "UpdateQuestions";
    }

    if (StringUtil.isDefined(surveyId)) {
      this.removeSessionSurveyId();
      this.removeSessionQuestions();
      this.removeSessionSurveyName();
      List<Question> questionsV = new ArrayList<Question>();
      QuestionContainerDetail survey = null;
      try {
        survey = this.getSurvey(surveyId);
        Collection<Question> questions = survey.getQuestions();
        // Cast Collection to List
        questionsV = new ArrayList<Question>(questions);
      } catch (SurveyException e) {
        SilverTrace.error("survey", SurveyRequestRouter.class.getName(),
            "getDestination error when retrieving a survey", e);
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
    List<String> questionStyles = new ArrayList<String>();
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
    String view = "";
    // Get component settings
    ResourceLocator surveySettings =
        new ResourceLocator("com.stratelia.webactiv.survey.surveySettings", this.getLanguage());

    String surveyImageDirectory =
        FileServerUtils.getUrl(this.getSpaceId(), this.getComponentId(), "REPLACE_FILE_NAME",
        "REPLACE_FILE_NAME", "image/gif", surveySettings.getString("imagesSubDirectory"));

    request.setAttribute("ImageDirectory", surveyImageDirectory);
    // Parameter variable declaration
    String action = "";
    String question = "";
    String nbAnswers = "";
    String answerInput = "";
    String suggestion = "";
    String style = "";
    String questionId = null;
    File dir = null;
    String logicalName = null;
    String type = null;
    String physicalName = null;
    boolean file = false;
    long size = 0;
    int attachmentSuffix = 0;
    List<Answer> answers = new ArrayList<Answer>();
    Answer answer = null;

    // Retrieve all the parameter from request
    try {
      List<FileItem> items = FileUploadUtil.parseRequest(request);
      for (FileItem item : items) {
        if (item.isFormField()) {
          String mpName = item.getFieldName();
          if ("Action".equals(mpName)) {
            action = item.getString();
          } else if ("question".equals(mpName)) {
            question = item.getString(FileUploadUtil.DEFAULT_ENCODING);
          } else if ("nbAnswers".equals(mpName)) {
            nbAnswers = item.getString(FileUploadUtil.DEFAULT_ENCODING);
          } else if ("SuggestionAllowed".equals(mpName)) {
            suggestion = item.getString(FileUploadUtil.DEFAULT_ENCODING);
          } else if ("questionStyle".equals(mpName)) {
            style = item.getString(FileUploadUtil.DEFAULT_ENCODING);
          } else if (mpName.startsWith("answer")) {
            answerInput = item.getString(FileUploadUtil.DEFAULT_ENCODING);
            answer = new Answer(null, null, answerInput, 0, 0, false, "", 0, false, null);
            answers.add(answer);
          } else if ("suggestionLabel".equals(mpName)) {
            answerInput = item.getString(FileUploadUtil.DEFAULT_ENCODING);
            answer = new Answer(null, null, answerInput, 0, 0, false, "", 0, true, null);
            answers.add(answer);
          } else if (mpName.startsWith("valueImageGallery")) {
            if (StringUtil.isDefined(item.getString(FileUploadUtil.DEFAULT_ENCODING))) {
              // traiter les images venant de la gallery si pas d'image externe
              if (!file) {
                answer.setImage(item.getString(FileUploadUtil.DEFAULT_ENCODING));
              }
            }
          } else if ("QuestionId".equals(mpName)) {
            questionId = item.getString(FileUploadUtil.DEFAULT_ENCODING);
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
            dir =
                new File(FileRepositoryManager.getAbsolutePath(this.getComponentId())
                + surveySettings.getString("imagesSubDirectory") + File.separator
                + physicalName);
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
          // out.flush();
        }
      }
    } catch (UtilException e) {
      SilverTrace.error("Survey", SurveyRequestRouter.class.getName(),
          "getDestination error with updateQuestion branch", e);
    } catch (UnsupportedEncodingException e) {
      SilverTrace.error("Survey", SurveyRequestRouter.class.getName(),
          "getDestination error with updateQuestion branch", e);
    } catch (IOException e) {
      SilverTrace.error("Survey", SurveyRequestRouter.class.getName(),
          "getDestination error with updateQuestion branch", e);
    }

    if ("SendUpdateQuestion".equals(action) || "SendNewQuestion".equals(action)) {
      // Remove answer for open question
      if ("open".equals(style)) {
        answers.clear();
      }
      // Remove the suggestion answer from the list
      if ("0".equals(suggestion)) {
        for (Answer curAnswer : answers) {
          if (curAnswer.isOpened()) {
            answers.remove(curAnswer);
            break;
          }
        }
      }
      if ("SendNewQuestion".equals(action)) {
        Question questionObject = new Question(null, null, question, "", "", null, style, 0);
        questionObject.setAnswers(answers);
        List<Question> questionsV = this.getSessionQuestions();
        questionsV.add(questionObject);
        this.setSessionQuestions(questionsV);
      } else if (action.equals("SendUpdateQuestion")) {
        Question questionObject = new Question(null, null, question, "", "", null, style, 0);
        questionObject.setAnswers(answers);
        List<Question> questionsV = this.getSessionQuestions();
        questionsV.set(Integer.parseInt(questionId), questionObject);
        this.setSessionQuestions(questionsV);
      }
    } else if (action.equals("End")) {
      QuestionContainerDetail surveyDetail = this.getSessionSurveyUnderConstruction();
      // Vector 2 Collection
      List<Question> questionsV = this.getSessionQuestions();
      surveyDetail.setQuestions(questionsV);
    }
    if ((action.equals("SendQuestionForm")) || "UpdateQuestion".equals(action)) {
      if (action.equals("SendQuestionForm")) {
        request.setAttribute("NbAnswers", nbAnswers);
      } else if ("UpdateQuestion".equals(action)) {
        request.setAttribute("QuestionId", questionId);
      }
    }
    // Prepare destination request attribute
    if ("SendNewQuestion".equals(action) || "SendUpdateQuestion".equals(action)) {
      request.setAttribute("Action", "UpdateQuestions");
      request.setAttribute("SurveyName", this.getSessionSurveyName());
      view = "questionsUpdate.jsp";
    } else {
      request.setAttribute("Suggestion", suggestion);
      request.setAttribute("Style", style);
      request.setAttribute("Action", action);
      view = function;
    }
    return view;
  }
}