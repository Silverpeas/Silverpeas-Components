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

/*
 * QuizzSessionController.java
 * 
 */
package com.stratelia.webactiv.quizz.control;

import static com.silverpeas.pdc.model.PdcClassification.aPdcClassificationOfContent;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.RemoveException;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;

import com.silverpeas.pdc.PdcServiceFactory;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.pdc.service.PdcClassificationService;
import com.silverpeas.pdc.web.PdcClassificationEntity;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.clipboard.ClipboardSelection;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.quizz.QuizzException;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.answer.model.Answer;
import com.stratelia.webactiv.util.question.model.Question;
import com.stratelia.webactiv.util.questionContainer.control.QuestionContainerBm;
import com.stratelia.webactiv.util.questionContainer.control.QuestionContainerBmHome;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerPK;
import com.stratelia.webactiv.util.questionContainer.model.QuestionContainerSelection;
import com.stratelia.webactiv.util.questionResult.model.QuestionResult;
import com.stratelia.webactiv.util.score.control.ScoreBm;
import com.stratelia.webactiv.util.score.model.ScoreDetail;

/**
 * @author dle&sco
 * @version
 */
public class QuizzSessionController extends AbstractComponentSessionController {
  private QuestionContainerBm questionContainerBm = null;
  private ResourceLocator settings = null;
  private ScoreBm scoreBm = null;
  private int nbTopScores = 0;
  private boolean isAllowedTopScores = false;
  private List<PdcPosition> positions = null;

  /** Creates new sessionClientController */
  public QuizzSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context, "com.stratelia.webactiv.quizz.multilang.quizz");
    setQuestionContainerBm();
    String nbTop = getSettings().getString("nbTopScores");
    String isAllowedTop = getSettings().getString("isAllowedTopScores");
    setNbTopScores(Integer.parseInt(nbTop));
    setIsAllowedTopScores(Boolean.parseBoolean(isAllowedTop));
  }

  public int getNbTopScores() {
    return this.nbTopScores;
  }

  public void setNbTopScores(int nbTopScores) {
    this.nbTopScores = nbTopScores;
  }

  public boolean getIsAllowedTopScores() {
    return this.isAllowedTopScores;
  }

  public void setIsAllowedTopScores(boolean isAllowedTopScores) {
    this.isAllowedTopScores = isAllowedTopScores;
  }

  public QuestionContainerBm getQuestionContainerBm() {
    return questionContainerBm;
  }

  private void setQuestionContainerBm() {
    if (questionContainerBm == null) {
      try {
        QuestionContainerBmHome questionContainerBmHome = (QuestionContainerBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.QUESTIONCONTAINERBM_EJBHOME,
                QuestionContainerBmHome.class);

        this.questionContainerBm = questionContainerBmHome.create();
      } catch (Exception e) {
        throw new EJBException(e.getMessage(), e);
      }
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public ResourceLocator getSettings() {
    if (settings == null) {
      try {
        String langue = getLanguage();
        settings = new ResourceLocator("com.stratelia.webactiv.quizz.quizzSettings", langue);
      } catch (Exception e) {
        if (settings == null) {
          settings = new ResourceLocator("com.stratelia.webactiv.quizz.quizzSettings", "fr");
        }
      }
    }
    return settings;
  }

  public UserDetail getUserDetail(String userId) {
    return getOrganizationController().getUserDetail(userId);
  }

  /**
   * Method declaration
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public Collection<QuestionContainerHeader> getUserQuizzList() throws QuizzException {
    try {
      QuestionContainerPK questionContainerPK = new QuestionContainerPK(null,
          getSpaceId(), getComponentId());

      return questionContainerBm.getOpenedQuestionContainersAndUserScores(
          questionContainerPK, getUserId());
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.getUserQuizzList",
          QuizzException.WARNING, "Quizz.EX_PROBLEM_TO_OBTAIN_LIST_QUIZZ", e);
    }
  }

  /**
   * Method declaration
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public Collection<QuestionContainerHeader> getAdminQuizzList() throws QuizzException {
    try {
      QuestionContainerPK questionContainerPK = new QuestionContainerPK(null,
          getSpaceId(), getComponentId());

      return questionContainerBm
          .getNotClosedQuestionContainers(questionContainerPK);
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.getUserQuizzList",
          QuizzException.WARNING, "Quizz.EX_PROBLEM_TO_OBTAIN_LIST_QUIZZ", e);
    }
  }

  /**
   * Method declaration
   * @param id
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public QuestionContainerDetail getQuizzDetail(String id)
      throws QuizzException {
    try {
      QuestionContainerPK questionContainerPK = new QuestionContainerPK(id,
          getSpaceId(), getComponentId());

      return questionContainerBm.getQuestionContainer(questionContainerPK,
          getUserId());
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.getUserQuizzList",
          QuizzException.WARNING, "Quizz.EX_PROBLEM_TO_OBTAIN_QUIZZ", e);
    }
  }

  /**
   * Method declaration
   * @param quizzDetail
   * @throws QizzException
   * @see
   */
  public void createQuizz(QuestionContainerDetail quizzDetail) throws QuizzException {
    QuestionContainerPK qcPK = new QuestionContainerPK(null, getSpaceId(), getComponentId());
    try {
      qcPK = questionContainerBm.createQuestionContainer(qcPK, quizzDetail, getUserId());
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.createQuizz",
          QuizzException.ERROR, "Quizz.EX_PROBLEM_TO_CREATE", e);
    }
    // persist positions after quiz creation
    classifyContent(quizzDetail, qcPK);
  }

  /**
   * @param quizzDetail
   * @param componentId
   * @throws QuizzException
   */
  public void createQuizz(QuestionContainerDetail quizzDetail, String componentId)
      throws QuizzException {
    QuestionContainerPK qcPK = new QuestionContainerPK(null, null, componentId);
    try {
      qcPK = questionContainerBm.createQuestionContainer(qcPK, quizzDetail, getUserId());
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.createQuizz", QuizzException.ERROR,
          "Quizz.EX_PROBLEM_TO_CREATE", e);
    }
    // persist positions after quiz creation
    classifyContent(quizzDetail, qcPK);
  }

  /**
   * this method classify content only when new quiz is created Check if a position has been defined
   * in header form then persist it
   * @param quizDetail the current quiz QuestionContainerDetail
   * @param qcPK the QuestionContainerPK with content identifier
   */
  private void classifyContent(QuestionContainerDetail quizDetail, QuestionContainerPK qcPK) {
    List<PdcPosition> positions = this.getPositions();
    if (positions != null && !positions.isEmpty()) {
      PdcClassification classification =
             aPdcClassificationOfContent(qcPK.getId(), qcPK.getInstanceId()).withPositions(
                 this.getPositions());
      if (!classification.isEmpty()) {
        PdcClassificationService service =
               PdcServiceFactory.getFactory().getPdcClassificationService();
        classification.ofContent(qcPK.getId());
        service.classifyContent(quizDetail, classification);
      }
    }
  }

  /**
   * Method declaration
   * @param quizzId
   * @param reply
   * @throws QuizzException
   * @see
   */
  public void recordReply(String quizzId, Hashtable<String, Vector<String>> reply)
      throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      questionContainerBm.recordReplyToQuestionContainerByUser(qcPK, getUserId(), reply);
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.recordReply", QuizzException.ERROR,
          "Quizz.EX_RECORD_REPLY_FAILED", e);
    }
  }

  /**
   * Method declaration
   * @param quizzId
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public Collection<QuestionResult> getSuggestions(String quizzId) throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      return questionContainerBm.getSuggestions(qcPK);
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.getSuggestions", QuizzException.ERROR,
          "Quizz.EX_PROBLEM_TO_RETURN_SUGGESTION", e);
    }
  }

  /**
   * Method declaration
   * @param quizzId
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public void closeQuizz(String quizzId) throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      questionContainerBm.deleteIndex(qcPK);
      questionContainerBm.closeQuestionContainer(qcPK);
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.closeQuizz", QuizzException.ERROR,
          "Quizz.EX_PROBLEM_TO_CLOSE_QUIZZ", e);
    }
  }

  /**
   * Method declaration
   * @param quizzId
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public int getNbVoters(String quizzId) throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      return questionContainerBm.getNbVotersByQuestionContainer(qcPK);
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.getNbVoters", QuizzException.ERROR,
          "Quizz.EX_PROBLEM_OBTAIN_NB_VOTERS", e);
    }
  }

  /**
   * Method declaration
   * @param quizzId
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public float getAveragePoints(String quizzId) throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      return questionContainerBm.getAverageScoreByFatherId(qcPK);
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.getAveragePoints", QuizzException.ERROR,
          "Quizz.EX_PROBLEM_OBTAIN_AVERAGE", e);
    }
  }

  /**
   * Method declaration
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public Collection<QuestionContainerHeader> getAdminResults() throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK("unknown", getSpaceId(), getComponentId());
      return questionContainerBm.getQuestionContainersWithScores(qcPK);
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.getAdminResults", QuizzException.ERROR,
          "Quizz.EX_PROBLEM_OBTAIN_RESULT", e);
    }
  }

  /**
   * Method declaration
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public Collection<QuestionContainerHeader> getUserResults() throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK("unknown", getSpaceId(), getComponentId());
      return questionContainerBm.getQuestionContainersWithUserScores(qcPK, getUserId());
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.getUserResults", QuizzException.ERROR,
          "Quizz.EX_PROBLEM_OBTAIN_RESULT", e);
    }
  }

  /**
   * Method declaration
   * @param quizzId
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public Collection<ScoreDetail> getUserScoresByFatherId(String quizzId) throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      return questionContainerBm.getUserScoresByFatherId(qcPK, getUserId());
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.getUserScoresByFatherId",
          QuizzException.ERROR, "Quizz.EX_PROBLEM_TO_OBTAIN_SCORE", e);
    }
  }

  /**
   * Method declaration
   * @param quizzId
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public Collection<ScoreDetail> getUserPalmares(String quizzId) throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      return questionContainerBm.getBestScoresByFatherId(qcPK, nbTopScores);
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.getUserPalmares", QuizzException.ERROR,
          "Quizz.EX_PROBLEM_OBTAIN_USERPALMARES", e);
    }
  }

  /**
   * Method declaration
   * @param quizzId
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public Collection<ScoreDetail> getAdminPalmares(String quizzId) throws QuizzException {
    Collection<ScoreDetail> scores = null;
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      scores = questionContainerBm.getScoresByFatherId(qcPK);
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.getAdminPalmares", QuizzException.ERROR,
          "Quizz.EX_PROBLEM_OBTAIN_PALMARES", e);
    }
    return scores;
  }

  /**
   * Method declaration
   * @param quizzId
   * @param getUserId ()
   * @param participationId
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public QuestionContainerDetail getQuestionContainerByParticipationId(String quizzId,
      String userId, int participationId) throws QuizzException {
    QuestionContainerDetail questionContainerDetail = null;
    QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
    try {
      questionContainerDetail =
          questionContainerBm.getQuestionContainerByParticipationId(qcPK, userId, participationId);
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.getQuestionContainerByParticipationId",
          QuizzException.ERROR, "Quizz.EX_PROBLEM_TO_OBTAIN_QUIZZ", e);
    }
    return questionContainerDetail;
  }

  /**
   * Method declaration
   * @param quizzId
   * @param participationId
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public QuestionContainerDetail getQuestionContainerForCurrentUserByParticipationId(
      String quizzId, int participationId) throws QuizzException {
    QuestionContainerDetail questionContainerDetail = null;
    QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
    try {
      questionContainerDetail =
          questionContainerBm.getQuestionContainerByParticipationId(qcPK, getUserId(),
              participationId);
    } catch (Exception e) {
      throw new QuizzException(
          "QuizzSessionController.getQuestionContainerForCurrentUserByParticipationId",
          QuizzException.ERROR, "Quizz.EX_PROBLEM_TO_OBTAIN_QUIZZ", e);
    }
    return questionContainerDetail;
  }

  /**
   * Method declaration
   * @param quizzId
   * @param getUserId ()
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public int getUserNbParticipationsByFatherId(String quizzId, String userId) throws QuizzException {
    QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
    int nbPart = 0;
    try {
      nbPart = questionContainerBm.getUserNbParticipationsByFatherId(qcPK, userId);
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.getUserNbParticipationsByFatherId",
          QuizzException.ERROR, "Quizz.EX_PROBLEM_OBTAIN_NB_VOTERS", e);
    }
    return nbPart;
  }

  /**
   * Method declaration
   * @param quizzId
   * @param getUserId ()
   * @param participationId
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public ScoreDetail getUserScoreByFatherIdAndParticipationId(String quizzId, String userId,
      int participationId) throws QuizzException {
    QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
    ScoreDetail scoreDetail = null;
    try {
      scoreDetail =
          questionContainerBm.getUserScoreByFatherIdAndParticipationId(qcPK, userId,
              participationId);
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.getUserScoreByFatherIdAndParticipationId",
          QuizzException.ERROR, "Quizz.EX_PROBLEM_TO_OBTAIN_SCORE", e);
    }
    return scoreDetail;
  }

  /**
   * Method declaration
   * @param quizzId
   * @param participationId
   * @return
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public ScoreDetail getCurrentUserScoreByFatherIdAndParticipationId(String quizzId,
      int participationId) throws QuizzException {
    QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
    ScoreDetail scoreDetail = null;
    try {
      scoreDetail =
          questionContainerBm.getUserScoreByFatherIdAndParticipationId(qcPK, getUserId(),
              participationId);
    } catch (Exception e) {
      throw new QuizzException(
          "QuizzSessionController.getCurrentUserScoreByFatherIdAndParticipationId",
          QuizzException.ERROR, "Quizz.EX_PROBLEM_TO_OBTAIN_SCORE", e);
    }
    return scoreDetail;
  }

  /**
   * Method declaration
   * @param scoreDetail
   * @throws CreateException
   * @throws NamingException
   * @throws RemoteException
   * @throws SQLException
   * @see
   */
  public void updateScore(ScoreDetail scoreDetail) throws QuizzException {
    QuestionContainerPK qcPK = new QuestionContainerPK("", getSpaceId(), getComponentId());
    try {
      questionContainerBm.updateScore(qcPK, scoreDetail);
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.updateScore", QuizzException.ERROR,
          "Quizz.EX_PROBLEM_TO_UPDATE_SCORE", e);
    }
  }

  /**
   * Method declaration
   * @param quizzHeader
   * @param quizzId
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public void updateQuizzHeader(QuestionContainerHeader quizzHeader, String quizzId)
      throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      quizzHeader.setPK(qcPK);
      questionContainerBm.updateQuestionContainerHeader(quizzHeader);
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.updateQuizzHeader", QuizzException.ERROR,
          "Quizz.EX_PROBLEM_TO_UPDATE_QUIZZHEADER", e);
    }
  }

  /**
   * Method declaration
   * @param questions
   * @param quizzId
   * @throws CreateException
   * @throws NamingException
   * @throws SQLException
   * @see
   */
  public void updateQuestions(Collection<Question> questions, String quizzId)
      throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      questionContainerBm.updateQuestions(qcPK, questions);
    } catch (Exception e) {
      throw new QuizzException("QuizzSessionController.updateQuestions", QuizzException.ERROR,
          "Quizz.EX_PROBLEM_TO_UPDATE_QUESTIONS", e);
    }
  }

  public List<ComponentInstLight> getGalleries() {
    List<ComponentInstLight> galleries = null;
    OrganizationController orgaController = new OrganizationController();
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

  public boolean isPdcUsed() {
    String value = getComponentParameterValue("usePdc");
    if (value != null) {
      return "yes".equals(value.toLowerCase());
    }
    return false;
  }

  public void copySurvey(String quizzId) throws RemoteException, QuizzException {
    QuestionContainerDetail quizz = getQuizzDetail(quizzId);
    QuestionContainerSelection questionContainerSelect = new QuestionContainerSelection(quizz);
    getClipboardObjects().add((ClipboardSelection) questionContainerSelect);
  }

  public void paste() throws Exception {
    Collection<ClipboardSelection> clipObjects = getClipboardSelectedObjects();
    Iterator<ClipboardSelection> clipObjectIterator = clipObjects.iterator();
    while (clipObjectIterator.hasNext()) {
      ClipboardSelection clipObject = (ClipboardSelection) clipObjectIterator.next();
      if (clipObject != null) {
        if (clipObject
            .isDataFlavorSupported(QuestionContainerSelection.QuestionContainerDetailFlavor)) {
          QuestionContainerDetail quizz = (QuestionContainerDetail) clipObject
              .getTransferData(QuestionContainerSelection.QuestionContainerDetailFlavor);
          pasteQuizz(quizz);
        }
      }
    }
    clipboardPasteDone();
  }

  private void pasteQuizz(QuestionContainerDetail quizz) throws Exception {
    String componentId = "";
    if (quizz.getHeader().getInstanceId().equals(getComponentId())) {
      // in the same component
      componentId = quizz.getHeader().getInstanceId();
    } else {
      componentId = getComponentId();
    }
    Collection<Question> questions = quizz.getQuestions();
    Iterator<Question> itQ = questions.iterator();
    while (itQ.hasNext()) {
      Question question = itQ.next();
      Collection<Answer> answers = question.getAnswers();
      Iterator<Answer> itA = answers.iterator();
      int attachmentSuffix = 0;
      while (itA.hasNext()) {
        Answer answer = itA.next();
        String physicalName = answer.getImage();
        SilverTrace.debug("Quizz", "QuizzSessionController.pasteQuizz()", "root.MSG_PAST",
            "physicalName = " + physicalName + " answer = " + answer.getLabel());

        if (StringUtil.isDefined(physicalName)) {
          // copy image
          ResourceLocator srvSettings =
              new ResourceLocator("com.stratelia.webactiv.survey.surveySettings", "");
          String type =
              physicalName.substring(physicalName.indexOf('.') + 1, physicalName.length());
          String newPhysicalName =
              new Long(new Date().getTime()).toString() + attachmentSuffix + "." + type;
          SilverTrace.debug("Quizz", "QuizzSessionController.pasteQuizz()", "root.MSG_PAST",
              "newPhysicalName = " + newPhysicalName + " answer = " + answer.getLabel());
          attachmentSuffix = attachmentSuffix + 1;

          if (quizz.getHeader().getInstanceId().equals(getComponentId())) {
            // in the same component
            String absolutePath = FileRepositoryManager.getAbsolutePath(componentId);
            String dir =
                absolutePath + srvSettings.getString("imagesSubDirectory") + File.separator;
            FileRepositoryManager.copyFile(dir + physicalName, dir + newPhysicalName);
            SilverTrace.debug("Quizz", "QuizzSessionController.pasteQuizz()", "root.MSG_PAST",
                    " same component : from = " + dir + physicalName + " to = " + dir +
                        newPhysicalName);
          } else {
            // in other component
            String fromAbsolutePath =
                FileRepositoryManager.getAbsolutePath(quizz.getHeader().getInstanceId());
            String toAbsolutePath = FileRepositoryManager.getAbsolutePath(componentId);
            String fromDir =
                fromAbsolutePath + srvSettings.getString("imagesSubDirectory") + File.separator;
            String toDir =
                toAbsolutePath + srvSettings.getString("imagesSubDirectory") + File.separator;
            FileRepositoryManager.copyFile(fromDir + physicalName, toDir + newPhysicalName);
            SilverTrace.debug("Quizz", "QuizzSessionController.pasteQuizz()", "root.MSG_PAST",
                " other component : from = " + fromDir + physicalName + " to = " + toDir +
                    newPhysicalName);
          }
          // update answer
          answer.setImage(newPhysicalName);
          SilverTrace.debug("Quizz", "QuizzSessionController.pasteQuizz()", "root.MSG_PAST",
              " newPhysicalName = " + newPhysicalName + " answer = " + answer.getLabel());
        }
      }
    }
    createQuizz(quizz, componentId);
  }

  public int getSilverObjectId(String objectId) {
    int silverObjectId = -1;
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(objectId, getSpaceId(), getComponentId());
      silverObjectId = questionContainerBm.getSilverObjectId(qcPK);
    } catch (Exception e) {
      SilverTrace.error("quizz", "QuizzSessionController.getSilverObjectId()",
          "root.EX_CANT_GET_LANGUAGE_RESOURCE", "objectId=" + objectId, e);
    }
    return silverObjectId;
  }

  public String exportQuizzCSV(String quizzId) {
    QuestionContainerDetail quizz = null;
    try {
      quizz = getQuizzDetail(quizzId);
      return questionContainerBm.exportCSV(quizz, true);
    } catch (Exception e) {
      SilverTrace.error("quizzSession", "QuizzSessionController.exportQuizzCSV", "quizzId=" +
          quizzId, e);
    }
    return null;
  }

  public boolean isParticipationAllowed(String id) throws QuizzException {
    QuestionContainerDetail quizz = getQuizzDetail(id);
    int nbParticipations = getUserNbParticipationsByFatherId(id, getUserId());
    return nbParticipations < quizz.getHeader().getNbMaxParticipations();
  }

  public void close() {
    try {
      if (questionContainerBm != null) {
        questionContainerBm.remove();
      }
    } catch (RemoteException e) {
      SilverTrace.error("quizzSession", "QuizzSessionController.close", "", e);
    } catch (RemoveException e) {
      SilverTrace.error("quizzSession", "QuizzSessionController.close", "", e);
    }
    try {
      if (scoreBm != null) {
        scoreBm.remove();
      }
    } catch (RemoteException e) {
      SilverTrace.error("quizzSession", "QuizzSessionController.close", "", e);
    } catch (RemoveException e) {
      SilverTrace.error("quizzSession", "QuizzSessionController.close", "", e);
    }
  }

  /**
   * @param request
   * @throws ParseException
   */
  public void createTemporaryQuizz(HttpServletRequest request)
      throws ParseException {
    String action = request.getParameter("Action");
    if ("SendNewQuizz".equals(action)) {
      String title = request.getParameter("title");
      String description = request.getParameter("description");
      String beginDate = request.getParameter("beginDate");
      String endDate = request.getParameter("endDate");
      String nbQuestions = request.getParameter("nbQuestions");
      String notice = request.getParameter("notice");
      String nbAnswersNeeded = request.getParameter("nbAnswersNeeded");
      String nbAnswersMax = request.getParameter("nbAnswersMax");

      if (StringUtil.isDefined(beginDate)) {
        beginDate = DateUtil.date2SQLDate(beginDate, this.getLanguage());
      }
      if (StringUtil.isDefined(endDate)) {
        endDate = DateUtil.date2SQLDate(endDate, this.getLanguage());
      }

      QuestionContainerHeader questionContainerHeader =
          new QuestionContainerHeader(null, title, description, notice, null, null, beginDate,
              endDate, false, 0, Integer.parseInt(nbQuestions), Integer.parseInt(nbAnswersMax),
              Integer.parseInt(nbAnswersNeeded), 0);
      HttpSession session = request.getSession();
      QuestionContainerDetail questionContainerDetail = new QuestionContainerDetail();
      questionContainerDetail.setHeader(questionContainerHeader);
      session.setAttribute("quizzUnderConstruction", questionContainerDetail);
      // create the positions of the new quiz on the PdC
      String positions = request.getParameter("Positions");
      setQuizPositionsFromJSON(positions);
    }
  }

  /**
   * Set new survey positions (axis classification) from JSON string
   * @param positions: the JSON string positions
   */
  public void setQuizPositionsFromJSON(String positions) {
    if (StringUtil.isDefined(positions)) {
      PdcClassificationEntity surveyClassification = null;
      try {
        surveyClassification = PdcClassificationEntity.fromJSON(positions);
      } catch (JAXBException e) {
        SilverTrace.error("Survey", "SurveySessionController.sendNewSurveyAction",
            "PdcClassificationEntity error", "Problem to read JSON", e);
      }
      if (surveyClassification != null && !surveyClassification.isUndefined()) {
        List<PdcPosition> pdcPositions = surveyClassification.getPdcPositions();
        this.setPositions(pdcPositions);
      }
    } else {
      this.setPositions(null);
    }
  }

  /**
   * @return the positions
   */
  public List<PdcPosition> getPositions() {
    return positions;
  }

  /**
   * @param positions the positions to set
   */
  public void setPositions(List<PdcPosition> positions) {
    this.positions = positions;
  }

}