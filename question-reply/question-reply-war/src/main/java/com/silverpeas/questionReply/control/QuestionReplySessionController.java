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

package com.silverpeas.questionReply.control;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.silverpeas.attachment.importExport.AttachmentImportExport;
import com.silverpeas.importExport.report.ExportReport;
import com.silverpeas.questionReply.QuestionReplyException;
import com.silverpeas.questionReply.model.Category;
import com.silverpeas.questionReply.model.Question;
import com.silverpeas.questionReply.model.Recipient;
import com.silverpeas.questionReply.model.Reply;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.ZipManager;
import com.silverpeas.whitePages.control.CardManager;
import com.silverpeas.whitePages.model.Card;
import com.stratelia.silverpeas.containerManager.ContainerContext;
import com.stratelia.silverpeas.containerManager.ContainerPositionInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.genericPanel.GenericPanel;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.viewGenerator.html.Encode;

public class QuestionReplySessionController extends AbstractComponentSessionController {
  private String userProfil;
  private Question currentQuestion;
  private Reply currentReply;
  private Question newQuestion;
  private Reply newReply;
  private QuestionManager questionManager = null;
  private NotificationSender notifSender = null;
  // attributs utiles a l'intégration du PDC
  private ContainerContext containerContext;
  private String returnURL = "";

  private QuestionManager getQuestionManager() {
    if (questionManager == null)
      questionManager = QuestionManager.getInstance();
    return questionManager;
  }

  /*
   * Recupère la liste des questions selon le profil de l'utilisateur courant
   */
  public Collection getQuestions() throws QuestionReplyException {
    Collection questions = new ArrayList();
    if (userProfil.equals("user"))
      questions = getUserQuestions();
    else if (userProfil.equals("writer"))
      questions = getWriterQuestions();
    else if (userProfil.equals("publisher"))
      questions = getPublisherQuestions();
    else if (userProfil.equals("admin"))
      questions = getAdminQuestions();
    return questions;
  }

  public Collection<Question> getQuestionsByCategory(String categoryId)
      throws QuestionReplyException {
    Collection<Question> questions = getQuestionManager().getAllQuestionsByCategory(
        getComponentId(), categoryId);
    return questions;
  }

  public Collection getAllQuestions() throws QuestionReplyException {
    Collection questions = getQuestionManager().getAllQuestions(
        getComponentId());
    return questions;
  }

  /*
   * Recupère la question et ses réponses selon le profil de l'utilisateur courant, ainsi que ses
   * destinataires met la question en session
   */
  public Question getQuestion(long questionId) throws QuestionReplyException {
    Question question = getQuestionManager().getQuestion(questionId);
    setCurrentQuestion(question);
    Collection replies = new ArrayList();
    Collection recipients = getQuestionManager().getQuestionRecipients(
        questionId);
    if (userProfil.equals("user"))
      replies = getCurrentQuestionPublicReplies();
    else if (userProfil.equals("publisher"))
      replies = getCurrentQuestionPrivateReplies();
    else if ((userProfil.equals("writer")) || (userProfil.equals("admin")))
      replies = getCurrentQuestionReplies();
    getCurrentQuestion().writeReplies(replies);
    getCurrentQuestion().writeRecipients(recipients);
    return getCurrentQuestion();
  }

  /*
   * retourne la question courante
   */
  public Question getCurrentQuestion() {
    return this.currentQuestion;
  }

  public void setCurrentQuestion(Question question) {
    this.currentQuestion = question;
  }

  private void reSetCurrentQuestion() {
    setCurrentQuestion(null);
  }

  /*
   * Récupère une réponse met la réponse en session
   */
  public Reply getReply(long replyId) throws QuestionReplyException {
    Reply reply = getQuestionManager().getReply(replyId);
    setCurrentReply(reply);
    return reply;
  }

  /*
   * Récupère la réponse courante
   */
  public Reply getCurrentReply() {
    return this.currentReply;
  }

  public void setCurrentReply(Reply reply) {
    WAPrimaryKey pk = reply.getPK();
    pk.setComponentName(getComponentId());
    reply.setPK(pk);
    this.currentReply = reply;
  }

  /*
   * Retourne une nouvelle question (instanceId, creatorId, creationDate) met la question en session
   * : newQuestion
   */
  public Question getNewQuestion() {
    Question question = new Question(getUserId(), getComponentId());
    newQuestion = question;
    return newQuestion;
  }

  /*
   * initialise les destinataires de la question à créer
   */
  public void setNewQuestionRecipients(Collection userIds) {
    Collection recipients = new ArrayList();
    if (userIds != null) {
      Iterator it = userIds.iterator();
      while (it.hasNext()) {
        String userId = (String) it.next();
        Recipient recipient = new Recipient(userId);
        recipients.add(recipient);
      }
    }
    newQuestion.writeRecipients(recipients);
  }

  /*
   * initialise le contenu de la question à créer
   */
  public void setNewQuestionContent(String title, String content) {
    newQuestion.setTitle(title);
    newQuestion.setContent(content);
  }

  public void setNewQuestionContent(String title, String content,
      String categoryId) {
    newQuestion.setTitle(title);
    newQuestion.setContent(content);
    newQuestion.setCategoryId(categoryId);
  }

  /*
   * Enregistre la nouvelle question
   */
  public long saveNewQuestion() throws QuestionReplyException {
    // notifier les experts associés à cette question
    notifyQuestion(newQuestion);
    // notifier la question à tous les experts du composant
    notifyQuestionFromExpert(newQuestion);
    return getQuestionManager().createQuestion(newQuestion);
  }

  /*
   * Retourne une nouvelle réponse (questionId, creatorId, creationDate) pour la questionCourante
   * met la question en session : newReply
   */
  public Reply getNewReply() {
    Reply reply;
    if ((getCurrentQuestion() != null)
        && (getCurrentQuestion().getPK() != null))
      reply = new Reply(((IdPK) getCurrentQuestion().getPK()).getIdAsLong(),
          getUserId());
    else
      reply = new Reply(getUserId());
    newReply = reply;
    return newReply;
  }

  /*
   * initialise le contenu de la réponse à créer
   */
  public void setNewReplyContent(String title, String content, int publicReply,
      int privateReply) {
    newReply.setTitle(title);
    newReply.setContent(content);
    newReply.setPublicReply(publicReply);
    newReply.setPrivateReply(privateReply);
  }

  /*
   * Enregistre une FAQ
   */
  public long saveNewFAQ() throws QuestionReplyException {
    newQuestion.setStatus(2); // close
    newQuestion.setReplyNumber(1);
    newQuestion.setPublicReplyNumber(1);
    newQuestion.setPrivateReplyNumber(0);
    newReply.setPublicReply(1);
    newReply.setPrivateReply(0);

    WAPrimaryKey pk = newReply.getPK();
    pk.setComponentName(getComponentId());
    newReply.setPK(pk);

    return getQuestionManager().createQuestionReply(newQuestion, newReply);
  }

  /*
   * enregistre la nouvelle réponse de la question courante met en session la question modifiée
   */
  public void saveNewReply() throws QuestionReplyException {
    WAPrimaryKey pk = newReply.getPK();
    pk.setComponentName(getComponentId());
    newReply.setPK(pk);
    getQuestionManager().createReply(newReply, getCurrentQuestion());
    getQuestion(((IdPK) getCurrentQuestion().getPK()).getIdAsLong());
    notifyReply(newReply);
  }

  /*
   * Modifie et enregistre la question courante
   */
  public void updateCurrentQuestion(String title, String content)
      throws QuestionReplyException {
    getCurrentQuestion().setTitle(title);
    getCurrentQuestion().setContent(content);
    getQuestionManager().updateQuestion(getCurrentQuestion());
  }

  public void updateCurrentQuestion(String title, String content,
      String categoryId) throws QuestionReplyException {
    getCurrentQuestion().setTitle(title);
    getCurrentQuestion().setContent(content);
    getCurrentQuestion().setCategoryId(categoryId);
    getQuestionManager().updateQuestion(getCurrentQuestion());
  }

  /*
   * Modifie la réponse courante => supprime la réponse publique => deletePublicReplies() => crée
   * une nouvelle réponse publique et privée met à jour en session la question courante
   */
  public void updateCurrentReplyOLD(String title, String content)
      throws QuestionReplyException {
    getNewReply();
    setNewReplyContent(title, content, getCurrentReply().getPublicReply(), 1);
    WAPrimaryKey pk = newReply.getPK();
    pk.setComponentName(getComponentId());
    newReply.setPK(pk);
    long replyId = getQuestionManager().createReply(newReply,
        getCurrentQuestion());
    Collection replyIds = new ArrayList();
    replyIds.add(new Long(((IdPK) getCurrentReply().getPK()).getIdAsLong()));
    deletePublicReplies(replyIds);
    getReply(replyId);
    getQuestion(((IdPK) getCurrentQuestion().getPK()).getIdAsLong());
    notifyReply(newReply);
  }

  public void updateCurrentReply(String title, String content)
      throws QuestionReplyException {
    Reply reply = getCurrentReply();
    reply.setTitle(title);
    reply.setContent(content);
    WAPrimaryKey pk = reply.getPK();
    pk.setComponentName(getComponentId());
    reply.setPK(pk);
    getQuestionManager().updateReply(reply);
    getQuestion(((IdPK) getCurrentQuestion().getPK()).getIdAsLong());
  }

  /*
   * Supprime une liste de questions selon le profil de l'utilisateur courant i.e. suppression de
   * toutes les réponses publiques ou privées des questions
   */
  public void deleteQuestions(Collection questionsIds)
      throws QuestionReplyException {
    try {
      getQuestionManager().deleteQuestionAndReplies(questionsIds);
      /*
       * if (userProfil.equals("publisher")) deletePrivateQuestions(questionsIds); else if
       * ((userProfil.equals("writer")) || (userProfil.equals("admin")))
       * deletePublicQuestions(questionsIds);
       */
    } catch (QuestionReplyException e) {
      throw new QuestionReplyException(
          "QuestionReplySessionController.deleteQuestions",
          SilverpeasException.ERROR, "questionReply.EX_DELETE_QUESTION_FAILED",
          "", e);
    }
  }

  /*
   * Supprime une liste de reponses selon le profil de l'utilisateur courant i.e. suppression des
   * réponses publiques ou privées si ReplyNumber =0 et que la question est close, la question sera
   * supprimée => reSetCurrentQuestion appel de deletePublicReplies ou deletePrivateReplies si le
   * nombre de R publiques ou privées restantes est egal à 0 et que la question est close, la
   * question n'est plus visible => reSetCurrentQuestion sinon met en session la question
   */
  public void deleteReplies(Collection replyIds) throws QuestionReplyException {
    try {
      int rest = 0;
      if (userProfil.equals("publisher"))
        rest = deletePrivateReplies(replyIds);
      else if ((userProfil.equals("writer")) || (userProfil.equals("admin")))
        rest = deletePublicReplies(replyIds);
      if ((((getCurrentQuestion().getReplyNumber()) == 0) || (rest == 0))
          && (getCurrentQuestion().getStatus() == 2))
        reSetCurrentQuestion();
      else
        getQuestion(((IdPK) getCurrentQuestion().getPK()).getIdAsLong());
    } catch (QuestionReplyException e) {
      throw new QuestionReplyException(
          "QuestionReplySessionController.deleteReplies",
          SilverpeasException.ERROR, "questionReply.EX_DELETE_REPLY_FAILED",
          "", e);
    }
  }

  public void deleteR(Collection replyIds) throws QuestionReplyException {
    try {
      int rest = 0;
      rest = deletePrivateReplies(replyIds);
      rest = deletePublicReplies(replyIds);
      if ((((getCurrentQuestion().getReplyNumber()) == 0) || (rest == 0))
          && (getCurrentQuestion().getStatus() == 2))
        reSetCurrentQuestion();
      else
        getQuestion(((IdPK) getCurrentQuestion().getPK()).getIdAsLong());
    } catch (QuestionReplyException e) {
      throw new QuestionReplyException(
          "QuestionReplySessionController.deleteReplies",
          SilverpeasException.ERROR, "questionReply.EX_DELETE_REPLY_FAILED",
          "", e);
    }
  }

  /*
   * Clos une liste de questions
   */
  public void closeQuestions(Collection questionIds)
      throws QuestionReplyException {
    getQuestionManager().closeQuestions(questionIds);
  }

  /*
   * Clos une question si replyNumber = 0, la question sera supprimée => reSetCurrentQuestion sinon
   * met en session la question
   */
  public void closeQuestion(long questionId) throws QuestionReplyException {
    Collection questionIds = new ArrayList();
    questionIds.add(new Long(questionId));
    getQuestionManager().closeQuestions(questionIds);
  }

  public void openQuestion(long questionId) throws QuestionReplyException {
    Collection questionIds = new ArrayList();
    questionIds.add(new Long(questionId));
    getQuestionManager().openQuestions(questionIds);
  }

  /*
   * Supprime les réponses publiques => getQuestionManager().updateRepliesPublicStatus() retourne le
   * nombre de réponses publiques restantes
   */
  private int deletePublicReplies(Collection replyIds)
      throws QuestionReplyException {
    getQuestionManager().updateRepliesPublicStatus(replyIds,
        getCurrentQuestion());
    return getCurrentQuestion().getPublicReplyNumber();
  }

  /*
   * Supprime les réponses privées => getQuestionManager().updateRepliesPrivateStatus() retourne le
   * nombre de réponses privées restantes
   */
  private int deletePrivateReplies(Collection replyIds)
      throws QuestionReplyException {
    getQuestionManager().updateRepliesPrivateStatus(replyIds,
        getCurrentQuestion());
    return getCurrentQuestion().getPrivateReplyNumber();
  }

  /*
   * Retourne la liste des questions de l'utilisateur de rôle User i.e. liste des questions avec
   * réponses publiques => getQuestionManager().getPublicQuestions()
   */
  private Collection getUserQuestions() throws QuestionReplyException {
    return getQuestionManager().getPublicQuestions(getComponentId());
  }

  /*
   * Retourne la liste des questions de l'utilisateur de rôle Writer (expert) i.e. liste des
   * questions dont il est le destinataire non close => getQuestionManager().getReceiveQuestions()
   */
  private Collection getWriterQuestions() throws QuestionReplyException {
    return getQuestionManager().getReceiveQuestions(getUserId(),
        getComponentId());
  }

  /*
   * Retourne la liste des questions de l'utilisateur de rôle Publisher (demandeur) i.e. liste des
   * questions dont il est l'auteur non close ou close avec réponses privées =>
   * getQuestionManager().getSendQuestions()
   */
  private Collection getPublisherQuestions() throws QuestionReplyException {
    return getQuestionManager().getSendQuestions(getUserId(), getComponentId());
  }

  /*
   * Retourne la liste des questions de l'utilisateur de rôle Admin (animateur) i.e. liste des
   * questions non close ou close avec réponses publiques => getQuestionManager().getQuestions()
   */
  private Collection getAdminQuestions() throws QuestionReplyException {
    return getQuestionManager().getQuestions(getComponentId());
  }

  /*
   * liste les réponses publiques d'une question
   */
  private Collection getCurrentQuestionPublicReplies()
      throws QuestionReplyException {
    return getQuestionManager().getQuestionPublicReplies(
        ((IdPK) getCurrentQuestion().getPK()).getIdAsLong());
  }

  /*
   * liste les réponses privées d'une question
   */
  private Collection getCurrentQuestionPrivateReplies()
      throws QuestionReplyException {
    return getQuestionManager().getQuestionPrivateReplies(
        ((IdPK) getCurrentQuestion().getPK()).getIdAsLong());
  }

  /*
   * liste les réponses à une question
   */
  private Collection getCurrentQuestionReplies() throws QuestionReplyException {
    return getQuestionManager().getQuestionReplies(
        ((IdPK) getCurrentQuestion().getPK()).getIdAsLong());
  }

  public void setUserProfil() {
    String[] profiles = getUserRoles();
    String flag = "user";

    for (int i = 0; i < profiles.length; i++) {
      // if admin, return it, we won't find a better profile
      if (profiles[i].equals("admin")) {
        flag = profiles[i];
        break;
      }
      if (profiles[i].equals("writer")) {
        flag = profiles[i];
        break;
      }
      if (profiles[i].equals("publisher")) {
        flag = profiles[i];
      }
    }
    this.userProfil = flag;
  }

  public void setUserProfil(String profil) {
    this.userProfil = profil;
  }

  public String getUserProfil() {
    return this.userProfil;
  }

  /**
   * Redefinition method de abstractComponentSessionController car 4 rôles Return the highest user's
   * role (admin, publisher or user)
   */
  public String getUserRoleLevel() {
    String[] profiles = getUserRoles();
    String flag = "user";

    for (int i = 0; i < profiles.length; i++) {
      // if admin, return it, we won't find a better profile
      if (profiles[i].equals("admin")) {
        return profiles[i];
      }
      if (profiles[i].equals("writer")) {
        flag = profiles[i];
      }
      if (profiles[i].equals("publisher")) {
        flag = profiles[i];
      }
    }
    return flag;
  }

  /*
   * Retourne true si la liste contient deja le user
   */
  private boolean exist(UserDetail user, Collection listUser) {
    int i = 0;
    ArrayList arrayUser = new ArrayList(listUser);
    if (user != null) {
      String idUser = user.getId();
      while (i < arrayUser.size()) {
        UserDetail theUser = (UserDetail) arrayUser.get(i);
        String theId = theUser.getId();
        if (theId.equals(idUser))
          return true;
        i++;
      }
    } else {
      return true;
    }
    return false;
  }

  /*
   * Récupère la liste des positions d'une question
   */
  public ContainerPositionInterface getSilverContentIdPosition()
      throws QuestionReplyException {
    ContainerPositionInterface position;
    try {

      position = (ContainerPositionInterface) containerContext
          .getSilverContentIdSearchContext(new Integer(
          getCurrentQuestionContentId()).intValue(), getComponentId());
    } catch (Exception e) {
      throw new QuestionReplyException(
          "QuestionReplySessionController.getCurrentQuestionWriters()",
          SilverpeasException.ERROR, "questionReply.EX_CANT_GET_EXPERTS", "", e);
    }

    return position;

  }

  public String genericWriters() throws QuestionReplyException {
    GenericPanel gp = new GenericPanel();
    String context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    String theURL = context + "/RquestionReply/" + getComponentId()
        + "/EffectiveRelaunch";
    String cancelURL = context + "/RquestionReply/" + getComponentId()
        + "/ConsultQuestionQuery?questionId="
        + ((IdPK) getCurrentQuestion().getPK()).getId();
    PairObject hostComponentName = new PairObject(getComponentLabel(), context
        + "/RquestionReply/" + getComponentId() + "/Main");
    PairObject hostPath1 = new PairObject(getCurrentQuestion().getTitle(),
        "/RquestionReply/" + getComponentId()
        + "/ConsultQuestionQuery?questionId="
        + ((IdPK) getCurrentQuestion().getPK()).getId());
    PairObject[] hostPath = { hostPath1 };

    gp.resetAll();

    gp.setHostSpaceName(getSpaceLabel());
    gp.setHostComponentName(hostComponentName);
    gp.setHostPath(hostPath);

    gp.setCancelURL(cancelURL);

    gp.setGoBackURL(theURL);

    gp.setPanelProvider(new ExpertPanel(getLanguage(),
        getCurrentQuestionWriters()));

    gp.setPopupMode(false);
    gp.setMultiSelect(true);
    gp.setSelectable(true);
    setGenericPanel("QR", gp);

    return GenericPanel.getGenericPanelURL("QR");
  }

  /*
   * Relance et modifie la question courante
   */
  public void relaunchRecipients() throws QuestionReplyException {
    GenericPanel gp = getGenericPanel("QR");
    String[] uids = gp.getSelectedElements();
    Collection recipients = new ArrayList();

    if (uids != null) {
      for (int i = 0; i < uids.length; i++) {
        Recipient recipient = new Recipient(((IdPK) getCurrentQuestion()
            .getPK()).getIdAsLong(), uids[i]);
        recipients.add(recipient);
      }
    }
    getCurrentQuestion().writeRecipients(recipients);
    getQuestionManager().updateQuestionRecipients(getCurrentQuestion());
    notifyQuestion(getCurrentQuestion());
  }

  /*
   * Récupère la liste des experts du domaine de la question
   */
  public Collection getCurrentQuestionWriters() throws QuestionReplyException {
    OrganizationController orga = getOrganizationController();
    Collection arrayUsers = new ArrayList();

    try {
      ContentManager contentManager = new ContentManager();
      // recupere la liste de toutes les instances d'annuaire
      String[] instances = orga.getCompoId("whitePages");
      ArrayList listeInstanceId = new ArrayList();
      int i = 0;
      while (i < instances.length) {
        listeInstanceId.add("whitePages" + instances[i]);
        i++;
      }

      // recupere la liste de tous les experts du domaine de classement de la
      // question
      ContainerPositionInterface position = getSilverContentIdPosition();
      if (position != null) {
        if (!position.isEmpty()) {
          List liste = containerContext.getSilverContentIdByPosition(position,
              listeInstanceId);
          i = 0;
          ArrayList arraySilverContentId = new ArrayList();
          if (liste != null) {
            arraySilverContentId = new ArrayList(liste);
          }

          CardManager cardManager = CardManager.getInstance();
          while (i < arraySilverContentId.size()) {
            int silverContentId = ((Integer) arraySilverContentId.get(i))
                .intValue();
            String internalContentId = contentManager
                .getInternalContentId(silverContentId);
            Long userCardIdLong = new Long(internalContentId);
            long userCardId = userCardIdLong.longValue();
            Card card = cardManager.getCard(userCardId);
            if (card != null) {
              String idUser = card.getUserId();
              UserDetail user = orga.getUserDetail(idUser);
              if (!exist(user, arrayUsers)) {
                arrayUsers.add(user);
              }
            }
            i++;
          }
        }
      }

    } catch (Exception e) {
      throw new QuestionReplyException(
          "QuestionReplySessionController.getCurrentQuestionWriters()",
          SilverpeasException.ERROR, "questionReply.EX_CANT_GET_EXPERTS", "", e);
    }

    return arrayUsers;

  }

  /*
   * Récupère la liste des experts du domaine de la question qui ne sont pas déjà destinataires
   */
  public Collection getCurrentQuestionAvailableWriters()
      throws QuestionReplyException {
    Collection users = getCurrentQuestionWriters();
    Collection availableUsers = new ArrayList();
    Collection recipients = getCurrentQuestion().readRecipients();
    Iterator it = users.iterator();
    while (it.hasNext()) {
      UserDetail user = (UserDetail) it.next();
      Iterator itR = recipients.iterator();
      boolean isRecipient = false;
      while (itR.hasNext()) {
        Recipient recipient = (Recipient) itR.next();
        if (user.getId().equals(recipient.getUserId()))
          isRecipient = true;
      }
      if (!isRecipient)
        availableUsers.add(user);
    }
    return availableUsers;
  }

  /*
	*
	*/
  private void notify(String intro, String content, UserDetail[] users)
      throws QuestionReplyException {
    try {
      UserDetail user = getUserDetail(getUserId());
      String subject = getString("questionReply.notification")
          + getComponentLabel();
      String message = user.getFirstName() + " " + user.getLastName() + intro
          + " \n" + content + "\n \n";

      NotificationMetaData notifMetaData = new NotificationMetaData(
          NotificationParameters.NORMAL, subject, message);
      notifMetaData.setSender(getUserId());
      notifMetaData.addUserRecipients(users);
      notifMetaData.setSource(getSpaceLabel() + " - " + getComponentLabel());
      getNotificationSender().notifyUser(notifMetaData);
    } catch (Exception e) {
      throw new QuestionReplyException(
          "QuestionReplySessionController.notify()", SilverpeasException.ERROR,
          "questionReply.EX_NOTIFICATION_MANAGER_FAILED", "", e);
    }
  }

  /*
	*
	*/
  private void notifyQuestion(Question question) throws QuestionReplyException {
    String message = getString("questionReply.Question") + question.getTitle()
        + "\n" + question.getContent() + "\n";
    Collection recipients = question.readRecipients();

    UserDetail[] users = new UserDetail[recipients.size()];
    Iterator it = recipients.iterator();
    int i = 0;
    while (it.hasNext()) {
      Recipient recipient = (Recipient) it.next();
      users[i] = getUserDetail(recipient.getUserId());
      i++;
    }
    notify(getString("questionReply.msgQuestion"), message, users);
  }

  private void notifyQuestionFromExpert(Question question)
      throws QuestionReplyException {
    String message = getString("questionReply.Question") + question.getTitle()
        + "\n" + question.getContent() + "\n";

    List profils = new ArrayList();
    profils.add("writer");
    String[] usersIds = getOrganizationController().getUsersIdsByRoleNames(
        getComponentId(), profils);
    UserDetail[] users = new UserDetail[usersIds.length];
    for (int i = 0; i < usersIds.length; i++) {
      users[i] = getUserDetail(usersIds[i]);
    }
    notify(getString("questionReply.msgQuestion"), message, users);
  }

  /*
	*
	*/
  private void notifyReply(Reply reply) throws QuestionReplyException {
    String message = getString("questionReply.Question")
        + getCurrentQuestion().getTitle() + "\n"
        + getCurrentQuestion().getContent() + "\n\n"
        + getString("questionReply.Reply") + reply.getTitle() + "\n"
        + reply.getContent() + "\n";

    UserDetail user = getOrganizationController().getUserDetail(
        getCurrentQuestion().getCreatorId());
    UserDetail[] users = new UserDetail[1];
    users[0] = user;
    notify(getString("questionReply.msgReply"), message, users);
  }

  /*
	*
	*/
  public NotificationSender getNotificationSender() {
    if (notifSender == null)
      notifSender = new NotificationSender(getComponentId());
    return notifSender;
  }

  /*-------------- Methodes de la classe ------------------*/

  /*
	*
	*/
  public QuestionReplySessionController(MainSessionController mainSessionCtrl,
      ComponentContext context, String multilangBaseName, String iconBaseName) {
    super(mainSessionCtrl, context, multilangBaseName, iconBaseName);
    setUserProfil();
  }

  /*-------------- Methodes utiles a l'integration du PDC ------------------*/

  /*
	*
	*/
  public String getCurrentQuestionContentId() {
    String contentId = null;

    if (currentQuestion != null) {
      try {
        ContentManager contentManager = new ContentManager();

        contentId = ""
            + contentManager.getSilverContentId(
            currentQuestion.getPK().getId(), currentQuestion
            .getInstanceId());
      } catch (ContentManagerException ignored) {
        SilverTrace.error("questionReply", "QuestionReplySessionController",
            "questionReply.EX_UNKNOWN_CONTENT_MANAGER", ignored);
        contentId = null;
      }
    }

    return contentId;
  }

  public boolean isPrivateRepliesEnabled() {
    return "yes"
        .equalsIgnoreCase(getComponentParameterValue("privateRepliesUsed"));
  }

  /*
	*
	*/
  public void setContainerContext(ContainerContext containerContext) {
    this.containerContext = containerContext;
  }

  /*
	*
	*/
  public ContainerContext getContainerContext() {
    return containerContext;
  }

  public void setReturnURL(String returnURL) {
    this.returnURL = returnURL;
  }

  public String getReturnURL() {
    return returnURL;
  }

  // Gestion des catégories
  // ----------------------

  public Collection getAllCategories() throws QuestionReplyException {
    try {
      NodePK nodePK = new NodePK("0", getComponentId());
      Collection categories = getNodeBm().getChildrenDetails(nodePK);
      return categories;
    } catch (Exception e) {
      throw new QuestionReplyException(
          "QuestionReplySessioncontroller.getAllCategories()",
          SilverpeasRuntimeException.ERROR,
          "QuestionReply.MSG_CATEGORIES_NOT_EXIST", e);
    }
  }

  public synchronized void createCategory(Category category)
      throws QuestionReplyException {
    try {
      category.setCreationDate(DateUtil.date2SQLDate(new Date()));
      category.setCreatorId(getUserId());
      category.getNodePK().setComponentName(getComponentId());

      getNodeBm().createNode((NodeDetail) category, new NodeDetail());
    } catch (Exception e) {
      throw new QuestionReplyException(
          "QuestionReplySessioncontroller.createCategory()",
          SilverpeasRuntimeException.ERROR,
          "QuestionReply.MSG_CATEGORIES_NOT_CREATE", e);
    }
  }

  public Category getCategory(String categoryId) throws QuestionReplyException {
    try {
      // rechercher la catégorie
      NodePK nodePK = new NodePK(categoryId, getComponentId());
      Category category = new Category(getNodeBm().getDetail(nodePK));
      return category;
    } catch (Exception e) {
      throw new QuestionReplyException(
          "QuestionReplySessioncontroller.getCategory()",
          SilverpeasRuntimeException.ERROR,
          "QuestionReply.MSG_CATEGORY_NOT_EXIST", e);
    }
  }

  public synchronized void updateCategory(Category category)
      throws QuestionReplyException {
    try {
      getNodeBm().setDetail((NodeDetail) category);
    } catch (Exception e) {
      throw new QuestionReplyException(
          "QuestionReplySessioncontroller.updateCategory()",
          SilverpeasRuntimeException.ERROR,
          "QuestionReply.MSG_CATEGORY_NOT_EXIST", e);
    }
  }

  public synchronized void deleteCategory(String categoryId)
      throws QuestionReplyException {
    try {
      // pour cette catégorie, rechercher les questions et mettre "" dans la
      // catégorie
      Collection questions = getQuestionsByCategory(categoryId);
      Iterator it = questions.iterator();
      while (it.hasNext()) {
        Question question = (Question) it.next();
        question.setCategoryId("");
        getQuestionManager().updateQuestion(question);
      }

      // suppression de la catégorie
      NodePK nodePk = new NodePK(categoryId, getComponentId());
      getNodeBm().removeNode(nodePk);
    } catch (Exception e) {
      throw new QuestionReplyException(
          "QuestionReplySessioncontroller.deleteCategory()",
          SilverpeasRuntimeException.ERROR,
          "QuestionReply.MSG_CATEGORY_NOT_EXIST", e);
    }
  }

  public ExportReport export(ResourcesWrapper resource) throws QuestionReplyException,
      ParseException {
    StringBuilder sb = new StringBuilder("exportFAQ");
    Date date = new Date();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH'H'mm'm'ss's'");
    String dateFormatee = dateFormat.format(date);
    sb.append("_").append(dateFormatee);
    sb.append("_").append(getUserDetail().getId());
    ExportReport exportReport = new ExportReport();
    // Stockage de la date de démarage de l'export dans l'objet rapport
    exportReport.setDateDebut(new Date());
    String thisExportDir = sb.toString();

    // Création du dossier d'export exportFAQ_aaaa-mm-jj-hhHmmmsss_userId.zip
    String tempDir = FileRepositoryManager.getTemporaryPath();
    File fileExportDir = new File(tempDir + thisExportDir);
    if (!fileExportDir.exists()) {
      try {
        FileFolderManager.createFolder(fileExportDir);
      } catch (UtilException ex) {
        throw new QuestionReplyException("QuestionReplySessionController.export()",
            SilverpeasRuntimeException.ERROR,
            "root.MSG_FOLDER_NOT_CREATE", ex);
      }
    }

    // création du dossier "files"
    String dir = tempDir + thisExportDir;
    String nameForFiles = "files";
    File forFiles = new File(dir + File.separator + nameForFiles);
    try {
      FileFolderManager.createFolder(forFiles);
    } catch (UtilException ex) {
      throw new QuestionReplyException("QuestionReplySessionController.export()",
          SilverpeasRuntimeException.ERROR,
          "root.MSG_FOLDER_NOT_CREATE", ex);
    }

    // intégrer la css du disque dans "files"
    ResourceLocator settings =
        new ResourceLocator("com.silverpeas.questionReply.settings.questionReplySettings", "");
    try {
      String chemin = (settings.getString("mappingDir"));
      if (chemin.startsWith("file:")) {
        chemin = chemin.substring(8);
      }
      Collection files = FileFolderManager.getAllFile(chemin);
      Iterator itFiles = files.iterator();
      while (itFiles.hasNext()) {
        File file = (File) itFiles.next();
        File newFile =
            new File(dir + File.separator + nameForFiles + File.separator + file.getName());
        FileRepositoryManager.copyFile(file.getPath(), newFile.getPath());
      }
    } catch (Exception ex) {
      throw new QuestionReplyException("QuestionReplySessionController.export()",
          SilverpeasRuntimeException.ERROR,
          "QuestionReply.EX_CANT_COPY_FILE", ex);
    }

    // création du fichier html
    File fileHTML = new File(dir + File.separator + thisExportDir + ".html");
    FileWriter fileWriter = null;
    try {
      fileHTML.createNewFile();
      fileWriter = new FileWriter(fileHTML.getPath());
      fileWriter.write(toHTML(fileHTML, resource));
    } catch (IOException ex) {
      throw new QuestionReplyException("QuestionReplySessioncontroller.export()",
          SilverpeasRuntimeException.ERROR,
          "QuestionReply.MSG_CAN_WRITE_FILE", ex);
    } finally {
      try {
        fileWriter.close();
      } catch (Exception ex) {
      }
    }

    // Création du zip
    try {
      String zipFileName = fileExportDir.getName() + ".zip";
      long zipFileSize = ZipManager.compressPathToZip(fileExportDir.getPath(), tempDir
          + zipFileName);
      exportReport.setZipFileName(zipFileName);
      exportReport.setZipFileSize(zipFileSize);
      exportReport.setZipFilePath(FileServerUtils.getUrlToTempDir(zipFileName, zipFileName,
          "application/zip"));
    } catch (Exception ex) {
      throw new QuestionReplyException("QuestionReplySessioncontroller.export()",
          SilverpeasRuntimeException.ERROR,
          "QuestionReply.MSG_CAN_CREATE_ZIP", ex);
    }
    // Stockage de la date de fin de l'export dans l'objet rapport
    exportReport.setDateFin(new Date());
    return exportReport;
  }

  public String toHTML(File file, ResourcesWrapper resource) throws QuestionReplyException,
      ParseException {
    String fileName = file.getName();
    StringBuilder sb = new StringBuilder();

    sb.append("<HTML>\n");
    sb.append("<HEAD>\n");

    sb.append("<TITLE>").append(fileName).append("</TITLE>\n");
    sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">\n");
    sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"files/ExportFAQ.css\">\n");
    sb.append("\n");
    sb.append(addFunction());
    sb.append("\n");
    sb.append("</HEAD>\n");

    sb.append("<BODY>\n");
    sb.append("\n");
    sb.append(addBody(resource, file));
    sb.append("\n");
    sb.append("</BODY>\n");
    sb.append("</HTML>\n");

    return sb.toString();
  }

  public String addFunction() {
    StringBuilder sb = new StringBuilder();
    sb.append("<script language=\"javascript\">\n");
    sb.append("function showHideAnswer() { \n");
    sb.append("  var numericID = this.id.replace(/[^\\d]/g,'');\n");
    sb.append("  var obj = document.getElementById('a' + numericID);\n");
    sb.append("  if(obj.style.display=='block'){\n");
    sb.append("    obj.style.display='none';\n");
    sb.append("  }else{\n");
    sb.append("    obj.style.display='block';\n");
    sb.append("  }   \n");
    sb.append("}\n");

    sb.append("function initShowHideContent()\n");
    sb.append("{\n");
    sb.append("  var divs = document.getElementsByTagName('div');\n");
    sb.append("  for(var no=0;no<divs.length;no++)\n");
    sb.append("  {\n");
    sb.append("    if(divs[no].className=='question')\n");
    sb.append("    {\n");
    sb.append("      divs[no].onclick = showHideAnswer;\n");
    sb.append("    }\n");
    sb.append("  }\n");
    sb.append("}\n");

    sb.append("window.onload = initShowHideContent;\n");
    sb.append("</script>\n");
    return sb.toString();
  }

  public String addBody(ResourcesWrapper resource, File file) throws QuestionReplyException,
      ParseException {
    StringBuilder sb = new StringBuilder();
    sb.append("<table width=\"100%\">\n");
    Collection<NodeDetail> categories = getAllCategories();
    Iterator<NodeDetail> itC = categories.iterator();
    while (itC.hasNext()) {
      NodeDetail category = itC.next();
      String categoryId = Integer.toString(category.getId());
      // titre de la catégorie
      sb.append("<tr>\n");
      sb.append("<td class=\"titreCateg\" width=\"91%\">").append(category.getName()).append(
          "</td>\n");
      sb.append("</tr>\n");
      // contenu de la catégorie
      sb.append("<tr>\n");
      sb.append("<td colspan=\"2\">\n");
      Collection<Question> questions = getQuestionsByCategory(categoryId);
      Iterator<Question> itQ = questions.iterator();
      while (itQ.hasNext()) {
        Question question = itQ.next();
        String questionId = question.getPK().getId();
        String qId = "q" + questionId;
        sb
            .append("<table class=\"question\" width=\"98%\" align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
        sb.append("<tr>\n");
        sb.append("<td>\n");
        sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"2\">\n");
        sb.append("<tr>\n");
        sb.append("<td></td>\n");
        sb.append("<td class=\"titreQuestionReponse\" width=\"100%\">\n");
        sb.append("<div id=").append(qId).append(" class=\"question\">");
        sb.append(EncodeHelper.javaStringToHtmlParagraphe(question.getTitle()));
        sb.append("</div>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");
        sb.append("<td colspan=\"2\">\n");
        sb.append("<span class=\"txtBaseline\">");
        sb.append("Question de").append(question.readCreatorName()).append(" - ").append(
            resource.getOutputDate(question.getCreationDate()));
        sb.append("</span>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");
        sb.append("</table>\n");
        sb.append("</td>\n");
        sb.append("</tr>\n");
        sb.append("</table>\n");

        // affichage des réponses
        String aId = "a" + questionId;
        Collection replies = question.readReplies();
        Iterator itR = replies.iterator();
        boolean existe = false;
        if (itR.hasNext())
          existe = true;
        existe = true;
        if (existe) {
          sb.append("<table width=\"98%\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\">\n");
          sb.append("<tr>\n");
          sb.append("<td class=\"answers\">\n");
          sb.append("<div id=").append(aId).append(" class=\"answer\">\n");
          // contenu de la question
          sb.append("<table>\n");
          sb.append("<tr>\n");
          sb.append("<td>");
          sb.append(EncodeHelper.javaStringToHtmlParagraphe(question.getContent()));
          sb.append("</td>\n");
          sb.append("</tr>\n");
          sb.append("</table>\n");
        }
        while (itR.hasNext()) {
          Reply reply = (Reply) itR.next();
          sb.append("<br>\n");
          sb.append("<center>\n");
          sb
              .append("<table class=\"tableBoard\" width=\"98%\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\">\n");
          sb.append("<tr>\n");
          sb.append("<td nowrap=\"nowrap\">\n");
          sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"2\">\n");
          sb.append("<tr>\n");
          sb.append("<td class=\"titreQuestionReponse\" width=\"100%\">\n");
          sb.append(" <span class=\"titreQuestionReponse\">").append(
              EncodeHelper.javaStringToHtmlParagraphe(reply.getTitle())).append(
              "</span>\n");
          sb.append("</td>\n");
          sb.append("</tr>\n");
          sb.append("</table>\n");
          sb.append("<br>\n");
          sb.append("<table>\n");
          sb.append("<tr>\n");
          sb.append("<td width=\"90%\">");
          sb.append(EncodeHelper.javaStringToHtmlParagraphe(reply.getContent()));
          sb.append("</td>\n");

          // récupération des fichiers joints : copie de ces fichiers dans le dossier "files"
          AttachmentImportExport attachmentIE = new AttachmentImportExport();
          Vector<AttachmentDetail> attachments = null;
          try {
            String filePath = file.getParentFile().getPath() + File.separator + "files";
            String relativeFilePath = file.getParentFile().getPath();
            WAPrimaryKey replyPk = reply.getPK();
            replyPk.setComponentName(question.getInstanceId());
            attachments = attachmentIE.getAttachments(replyPk, filePath, relativeFilePath, null);
          } catch (Exception ex) {
            // En cas d"objet non trouvé: pas d'exception gérée par le système
            throw new QuestionReplyException("QuestionReplySessioncontroller.export()",
                0, "root.EX_CANT_GET_ATTACHMENTS", ex);
          }

          if (attachments != null && attachments.size() > 0) {
            // les fichiers joints : création du lien dans la page
            sb.append("<td valign=\"top\" align=\"left\">\n");
            sb.append("<a name=\"attachments\"></a>\n");
            sb.append("<td valign=\"top\" align=\"left\">\n");
            sb.append("<center>\n");
            sb
                .append("<table class=\"tableBoard\" width=\"98%\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\">\n");
            sb.append("<tr>\n");
            sb.append("<td nowrap=\"nowrap\">\n");
            sb.append("<table width=\"150\">\n");

            Iterator<AttachmentDetail> it = attachments.iterator();
            // pour chaque fichier
            while (it.hasNext()) {
              AttachmentDetail attachment = it.next();
              // attachment
              sb.append("<tr>\n");
              sb.append("<td align=\"center\"></td>\n");
              sb.append("</tr>\n");
              sb.append("<tr>\n");
              sb.append("<td valign=\"top\">\n");
              sb.append("<nobr>\n");
              sb.append("<a href=\"files/");
              sb.append(attachment.getLogicalName());
              sb.append("\" target=\"_blank\">");
              sb.append(attachment.getLogicalName());
              sb.append("</a>\n");
              sb.append("</nobr>\n");
              sb.append("<br>\n");
              sb.append(attachment.getAttachmentFileSize(attachment.getLanguage())).append("  ");
              sb.append(attachment.getAttachmentDownloadEstimation(attachment.getLanguage()));
              sb.append("</td>\n");
              sb.append("</tr>\n");
            }
            sb.append("</table>\n");
            sb.append("</td>\n");
            sb.append("</tr>\n");
            sb.append("</table>\n");
            sb.append("</center>\n");
            sb.append("</td>\n");
          }
          sb.append("</tr>\n");
          sb.append("</table>\n");
          sb.append("<br>\n");
          sb.append("<span class=\"txtBaseline\">");
          sb.append("Réponse de ").append(reply.readCreatorName()).append(" - ").append(
              resource.getOutputDate(reply.getCreationDate()));
          sb.append("</span>\n");
          sb.append("</td>\n");
          sb.append("</tr>\n");
          sb.append("</table>\n");
          sb.append("</center>\n");

        }
        if (existe) {
          sb.append("<br>\n");
          sb.append("</div>\n");
          sb.append("</td>\n");
          sb.append("</tr>\n");
          sb.append("</table>\n");
        }
      }
      sb.append("</td>\n");
      sb.append("</tr>\n");
    }
    sb.append("</table>\n");
    return sb.toString();
  }

  public boolean isVersionControlled() {
    String strVersionControlled = this
        .getComponentParameterValue("versionControl");
    return ((strVersionControlled != null)
        && !("").equals(strVersionControlled) && !("no")
        .equals(strVersionControlled.toLowerCase()));
  }

  private NodeBm getNodeBm() throws QuestionReplyException {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      throw new QuestionReplyException(
          "QuestionReplySessioncontroller.getNodeBm()",
          SilverpeasRuntimeException.ERROR,
          "QuestionReply.MSG_NODEBM_NOT_EXIST", e);
    }
    return nodeBm;
  }
}