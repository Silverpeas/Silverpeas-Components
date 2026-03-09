/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.quizz.control;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.silverpeas.components.quizz.QuizzException;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.exception.DecodingException;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.questioncontainer.answer.model.Answer;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerDetail;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerSelection;
import org.silverpeas.core.questioncontainer.container.service.QuestionContainerService;
import org.silverpeas.core.questioncontainer.question.model.Question;
import org.silverpeas.core.questioncontainer.score.model.ScoreDetail;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.csv.CSVRow;
import org.silverpeas.core.util.file.FileItem;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.web.export.ExportCSVBuilder;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.webapi.pdc.PdcClassificationEntity;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import java.io.File;
import java.text.ParseException;
import java.util.*;

import static org.silverpeas.core.pdc.pdc.model.PdcClassification.aPdcClassificationOfContent;

public final class QuizzSessionController extends AbstractComponentSessionController {

  private final QuestionContainerService questionContainerService =
      QuestionContainerService.get();
  private SettingBundle settings = null;
  private int nbTopScores = 0;
  private boolean isAllowedTopScores = false;
  private List<PdcPosition> positions = null;

  /**
   * Creates new sessionClientController
   */
  public QuizzSessionController(MainSessionController mainSessionCtrl, ComponentContext context) {
    super(mainSessionCtrl, context, "org.silverpeas.quizz.multilang.quizz");
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

  public QuestionContainerService getQuestionContainerService() {
    return questionContainerService;
  }

  @Override
  public SettingBundle getSettings() {
    if (settings == null) {
      settings = ResourceLocator.getSettingBundle("org.silverpeas.quizz.quizzSettings");
    }
    return settings;
  }

  public Collection<QuestionContainerHeader> getUserQuizzList() throws QuizzException {
    try {
      QuestionContainerPK questionContainerPK =
          new QuestionContainerPK(null, getSpaceId(), getComponentId());

      return questionContainerService
          .getOpenedQuestionContainersAndUserScores(questionContainerPK, getUserId());
    } catch (Exception e) {
      throw new QuizzException(e);
    }
  }

  public Collection<QuestionContainerHeader> getAdminQuizzList() throws QuizzException {
    try {
      QuestionContainerPK questionContainerPK =
          new QuestionContainerPK(null, getSpaceId(), getComponentId());

      return questionContainerService.getNotClosedQuestionContainers(questionContainerPK);
    } catch (Exception e) {
      throw new QuizzException(e);
    }
  }

  /**
   * @param id the quizz identifier
   * @return the question container detail of the quizz identified by given parameter
   * @throws QuizzException if the quizz getting fails.
   */
  public QuestionContainerDetail getQuizzDetail(String id) throws QuizzException {
    try {
      QuestionContainerPK questionContainerPK =
          new QuestionContainerPK(id, getSpaceId(), getComponentId());

      return questionContainerService.getQuestionContainer(questionContainerPK, getUserId());
    } catch (Exception e) {
      throw new QuizzException(e);
    }
  }

  /**
   * Saves the specified quizz in Silverpeas.
   *
   * @param quizzDetail the question container detail to create
   * @throws QuizzException if the quizz creation fails.
   */
  public void createQuizz(QuestionContainerDetail quizzDetail) throws QuizzException {
    QuestionContainerPK qcPK = new QuestionContainerPK(null, getSpaceId(), getComponentId());
    try {
      qcPK = questionContainerService.createQuestionContainer(qcPK, quizzDetail, getUserId());
    } catch (Exception e) {
      throw new QuizzException(e);
    }
    // persist positions after quiz creation
    classifyContent(quizzDetail, qcPK);
  }

  /**
   * @param quizzDetail the question container detail to create
   * @param componentId the component instance identifier
   * @throws QuizzException if the creation fails.
   */
  public QuestionContainerPK createQuizz(QuestionContainerDetail quizzDetail, String componentId)
      throws QuizzException {
    QuestionContainerPK qcPK = new QuestionContainerPK(null, null, componentId);
    try {
      qcPK = questionContainerService.createQuestionContainer(qcPK, quizzDetail, getUserId());
    } catch (Exception e) {
      throw new QuizzException(e);
    }

    return qcPK;
  }

  /**
   * this method classify content only when new quiz is created Check if a position has been defined
   * in header form then persist it
   *
   * @param quizDetail the current quiz QuestionContainerDetail
   * @param qcPK the QuestionContainerPK with content identifier
   */
  private void classifyContent(QuestionContainerDetail quizDetail, QuestionContainerPK qcPK) {
    List<PdcPosition> thePositions = this.getPositions();
    if (thePositions != null && !thePositions.isEmpty()) {
      PdcClassification classification =
          aPdcClassificationOfContent(qcPK.getId(), qcPK.getInstanceId())
              .withPositions(this.getPositions());
      classification.classifyContent(quizDetail);
    }
  }

  /**
   * Saves the specified answers to the given quizz.
   *
   * @param quizzId the quizz identifier
   * @param reply the reply to record
   * @throws QuizzException if the saving fails.
   */
  public void recordReply(String quizzId, Map<String, List<String>> reply) throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      questionContainerService.recordReplyToQuestionContainerByUser(qcPK, getUserId(), reply);
    } catch (Exception e) {
      throw new QuizzException(e);
    }
  }

  /**
   * Close the quizz identified by parameter
   *
   * @param quizzId the quizz identifier to close
   * @throws QuizzException if the closing fails.
   */
  public void closeQuizz(String quizzId) throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      questionContainerService.deleteIndex(qcPK);
      questionContainerService.closeQuestionContainer(qcPK);
    } catch (Exception e) {
      throw new QuizzException(e);
    }
  }

  /**
   * Gets the numbers of voters for the specified quizz.
   *
   * @param quizzId the quizz identifier
   * @return the number of voters
   * @throws QuizzException if the getting of the voters count fails.
   */
  public int getNbVoters(String quizzId) throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      return questionContainerService.getNbVotersByQuestionContainer(qcPK);
    } catch (Exception e) {
      throw new QuizzException(e);
    }
  }

  public float getAveragePoints(String quizzId) throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      return questionContainerService.getAverageScoreByFatherId(qcPK);
    } catch (Exception e) {
      throw new QuizzException(e);
    }
  }

  public Collection<QuestionContainerHeader> getAdminResults() throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK("unknown", getSpaceId(), getComponentId());
      return questionContainerService.getQuestionContainersWithScores(qcPK);
    } catch (Exception e) {
      throw new QuizzException(e);
    }
  }

  public Collection<QuestionContainerHeader> getUserResults() throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK("unknown", getSpaceId(), getComponentId());
      return questionContainerService.getQuestionContainersWithUserScores(qcPK, getUserId());
    } catch (Exception e) {
      throw new QuizzException(e);
    }
  }

  public Collection<ScoreDetail> getUserScoresByFatherId(String quizzId) throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      return questionContainerService.getUserScoresByFatherId(qcPK, getUserId());
    } catch (Exception e) {
      throw new QuizzException(e);
    }
  }

  public Collection<ScoreDetail> getUserPalmares(String quizzId) throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      return questionContainerService.getBestScoresByFatherId(qcPK, nbTopScores);
    } catch (Exception e) {
      throw new QuizzException(e);
    }
  }

  public Collection<ScoreDetail> getAdminPalmares(String quizzId) throws QuizzException {
    Collection<ScoreDetail> scores;
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      scores = questionContainerService.getScoresByFatherId(qcPK);
    } catch (Exception e) {
      throw new QuizzException(e);
    }
    return scores;
  }

  public QuestionContainerDetail getQuestionContainerByParticipationId(String quizzId,
      String userId, int participationId) throws QuizzException {
    QuestionContainerDetail questionContainerDetail;
    QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
    try {
      questionContainerDetail = questionContainerService
          .getQuestionContainerByParticipationId(qcPK, userId, participationId);
    } catch (Exception e) {
      throw new QuizzException(e);
    }
    return questionContainerDetail;
  }

  public QuestionContainerDetail getQuestionContainerForCurrentUserByParticipationId(String quizzId,
      int participationId) throws QuizzException {
    QuestionContainerDetail questionContainerDetail;
    QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
    try {
      questionContainerDetail = questionContainerService
          .getQuestionContainerByParticipationId(qcPK, getUserId(), participationId);
    } catch (Exception e) {
      throw new QuizzException(e);
    }
    return questionContainerDetail;
  }

  /**
   * @param quizzId the quizz identifier
   * @param userId the user identifier
   * @return the number of participation of the quizz
   * @throws QuizzException if the getting fails.
   */
  public int getUserNbParticipationsByFatherId(String quizzId, String userId)
      throws QuizzException {
    QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
    int nbPart;
    try {
      nbPart = questionContainerService.getUserNbParticipationsByFatherId(qcPK, userId);
    } catch (Exception e) {
      throw new QuizzException(e);
    }
    return nbPart;
  }

  public ScoreDetail getUserScoreByFatherIdAndParticipationId(String quizzId, String userId,
      int participationId) throws QuizzException {
    QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
    ScoreDetail scoreDetail;
    try {
      scoreDetail = questionContainerService
          .getUserScoreByFatherIdAndParticipationId(qcPK, userId, participationId);
    } catch (Exception e) {
      throw new QuizzException(e);
    }
    return scoreDetail;
  }

  public ScoreDetail getCurrentUserScoreByFatherIdAndParticipationId(String quizzId,
      int participationId) throws QuizzException {
    QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
    ScoreDetail scoreDetail;
    try {
      scoreDetail = questionContainerService
          .getUserScoreByFatherIdAndParticipationId(qcPK, getUserId(), participationId);
    } catch (Exception e) {
      throw new QuizzException(e);
    }
    return scoreDetail;
  }

  public void updateScore(ScoreDetail scoreDetail) throws QuizzException {
    QuestionContainerPK qcPK = new QuestionContainerPK("", getSpaceId(), getComponentId());
    try {
      questionContainerService.updateScore(qcPK, scoreDetail);
    } catch (Exception e) {
      throw new QuizzException(e);
    }
  }

  public void updateQuizzHeader(QuestionContainerHeader quizzHeader, String quizzId)
      throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      quizzHeader.setPK(qcPK);
      questionContainerService.updateQuestionContainerHeader(quizzHeader);
    } catch (Exception e) {
      throw new QuizzException(e);
    }
  }

  public void updateQuestions(Collection<Question> questions, String quizzId)
      throws QuizzException {
    try {
      QuestionContainerPK qcPK = new QuestionContainerPK(quizzId, getSpaceId(), getComponentId());
      questionContainerService.updateQuestions(qcPK, questions);
    } catch (Exception e) {
      throw new QuizzException(e);
    }
  }

  public List<ComponentInstLight> getGalleries() {
    return OrganizationController.get().getComponentsWithParameterValue("viewInWysiwyg", "yes");
  }

  public boolean isPdcUsed() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("usePdc"));
  }

  public void copySurvey(String quizzId) throws ClipboardException, QuizzException {
    QuestionContainerDetail quizz = getQuizzDetail(quizzId);
    QuestionContainerSelection questionContainerSelect = new QuestionContainerSelection(quizz);
    addClipboardSelection(questionContainerSelect);
  }

  public void paste() throws Exception {
    Collection<ClipboardSelection> clipObjects = getClipboardSelectedObjects();
    for (ClipboardSelection clipObject : clipObjects) {
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
    String componentId;
    QuestionContainerPK quizzPk = quizz.getHeader().getPK();

    if (quizz.getHeader().getInstanceId().equals(getComponentId())) {
      // in the same component
      componentId = quizz.getHeader().getInstanceId();
    } else {
      componentId = getComponentId();
    }
    Collection<Question> questions = quizz.getQuestions();
    for (final Question question : questions) {
      Collection<Answer> answers = question.getAnswers();
      Iterator<Answer> itA = answers.iterator();
      int attachmentSuffix = 0;
      while (itA.hasNext()) {
        Answer answer = itA.next();
        String physicalName = answer.getImage();
        if (StringUtil.isDefined(physicalName)) {
          // copy image
          SettingBundle srvSettings =
              ResourceLocator.getSettingBundle("org.silverpeas.survey.surveySettings");
          String type =
              physicalName.substring(physicalName.indexOf('.') + 1);
          String newPhysicalName =
              Long.toString(new Date().getTime()) + attachmentSuffix + "." + type;
          attachmentSuffix = attachmentSuffix + 1;

          if (quizz.getHeader().getInstanceId().equals(getComponentId())) {
            // in the same component
            String absolutePath = FileRepositoryManager.getAbsolutePath(componentId);
            String dir =
                absolutePath + srvSettings.getString("imagesSubDirectory") + File.separator;
            FileRepositoryManager.copyFile(dir + physicalName, dir + newPhysicalName);
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
          }
          // update answer
          answer.setImage(newPhysicalName);
        }
      }
    }

    QuestionContainerPK toQuestionContainerPk = createQuizz(quizz, componentId);

    // Paste positions on Pdc
    final int fromSilverObjectId = getQuestionContainerService().getSilverObjectId(quizzPk);
    final int toSilverObjectId =
        getQuestionContainerService().getSilverObjectId(toQuestionContainerPk);

    PdcManager.get()
        .copyPositions(fromSilverObjectId, quizz.getHeader().getInstanceId(), toSilverObjectId,
            componentId);
  }

  public ExportCSVBuilder exportQuizzCSV(String quizzId) {
    ExportCSVBuilder csvBuilder = new ExportCSVBuilder();
    try {
      QuestionContainerDetail quizz = getQuizzDetail(quizzId);

      List<CSVRow> rows = questionContainerService.exportCSV(quizz, true);
      csvBuilder.addLines(rows);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return csvBuilder;
  }

  public boolean isParticipationAllowed(String id) throws QuizzException {
    QuestionContainerDetail quizz = getQuizzDetail(id);
    int nbParticipations = getUserNbParticipationsByFatherId(id, getUserId());
    return nbParticipations < quizz.getHeader().getNbMaxParticipations();
  }

  public void createTemporaryQuizz(HttpServletRequest request) throws ParseException {
    List<FileItem> items = HttpRequest.decorate(request).getFileItems();
    String title = FileUploadUtil.getParameter(items, "title");
    String description = FileUploadUtil.getParameter(items, "description");
    String beginDate = FileUploadUtil.getParameter(items, "beginDate");
    String endDate = FileUploadUtil.getParameter(items, "endDate");
    String nbQuestions = FileUploadUtil.getParameter(items, "nbQuestions");
    String notice = FileUploadUtil.getParameter(items, "notice");
    String nbAnswersNeeded = FileUploadUtil.getParameter(items, "nbAnswersNeeded");
    String nbAnswersMax = FileUploadUtil.getParameter(items, "nbAnswersMax");

    if (StringUtil.isDefined(beginDate)) {
      beginDate = DateUtil.date2SQLDate(beginDate, this.getLanguage());
    }
    if (StringUtil.isDefined(endDate)) {
      endDate = DateUtil.date2SQLDate(endDate, this.getLanguage());
    }

    QuestionContainerHeader questionContainerHeader =
        new QuestionContainerHeader(null, title, description, notice, null, null, beginDate,
            endDate, false, 0, Integer.parseInt(nbQuestions), Integer.parseInt(nbAnswersMax),
            Integer.parseInt(nbAnswersNeeded), 0, QuestionContainerHeader.IMMEDIATE_RESULTS,
            QuestionContainerHeader.TWICE_DISPLAY_RESULTS);
    HttpSession session = request.getSession();
    QuestionContainerDetail questionContainerDetail = new QuestionContainerDetail();
    questionContainerDetail.setHeader(questionContainerHeader);
    session.setAttribute("quizzUnderConstruction", questionContainerDetail);
    // create the positions of the new quiz on the PdC
    String thePositions = request.getParameter("Positions");
    setQuizPositionsFromJSON(thePositions);
  }

  /**
   * Set new survey positions (axis classification) from JSON string
   *
   * @param positions: the JSON string positions
   */
  public void setQuizPositionsFromJSON(String positions) {
    if (StringUtil.isDefined(positions)) {
      PdcClassificationEntity surveyClassification = null;
      try {
        surveyClassification = PdcClassificationEntity.fromJSON(positions);
      } catch (DecodingException e) {
        SilverLogger.getLogger(this).error(e);
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
