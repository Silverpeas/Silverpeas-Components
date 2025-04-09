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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply.control;

import org.silverpeas.components.questionreply.QuestionReplyException;
import org.silverpeas.components.questionreply.model.Category;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.QuestionDetail;
import org.silverpeas.components.questionreply.model.Recipient;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.components.questionreply.service.QuestionManagerProvider;
import org.silverpeas.components.questionreply.service.notification.QuestionNotifier;
import org.silverpeas.components.questionreply.service.notification.ReplyNotifier;
import org.silverpeas.components.whitepages.control.CardManager;
import org.silverpeas.components.whitepages.model.Card;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.model.Attachments;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagementEngine;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagementEngineProvider;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.exception.DecodingException;
import org.silverpeas.core.importexport.report.ExportReport;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.pdc.pdc.model.SearchContext;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.ZipUtil;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.webapi.pdc.PdcClassificationEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper.buildAndSend;
import static org.silverpeas.core.pdc.pdc.model.PdcClassification.aPdcClassificationOfContent;
import static org.silverpeas.core.util.Charsets.UTF_8;

public class QuestionReplySessionController extends AbstractComponentSessionController {
  private static final long serialVersionUID = -4956263179309397997L;

  private SilverpeasRole userProfil;
  private Question currentQuestion;
  private Reply currentReply;
  private Question newQuestion;
  private Reply newReply;

  /*
   * Recupère la liste des questions selon le profil de l'utilisateur courant
   */
  public Collection<Question> getQuestions() throws QuestionReplyException {
    switch (userProfil) {
      case USER:
        return getUserQuestions();
      case WRITER:
        return getWriterQuestions();
      case PUBLISHER:
        return getPublisherQuestions();
      case ADMIN:
        return getAdminQuestions();
      default:
        break;
    }
    return new ArrayList<>();
  }

  public Collection<Question> getQuestionsByCategory(String categoryId)
      throws QuestionReplyException {
    return QuestionManagerProvider.getQuestionManager()
        .getAllQuestionsByCategory(getComponentId(), categoryId);
  }

  /*
   * Recupère la question et ses réponses selon le profil de l'utilisateur courant, ainsi que ses
   * destinataires met la question en session
   */
  public Question getQuestion(long questionId) throws QuestionReplyException {
    Question question = QuestionManagerProvider.getQuestionManager().getQuestion(questionId);
    setCurrentQuestion(question);
    question.writeRecipients(
        QuestionManagerProvider.getQuestionManager().getQuestionRecipients(questionId));
    question.writeReplies(getRepliesForQuestion(questionId));
    return question;
  }

  public Collection<Reply> getRepliesForQuestion(long id) throws QuestionReplyException {
    switch (userProfil) {
      case USER:
        return getPublicRepliesForQuestion(id);
      case PUBLISHER:
        return getPrivateRepliesForQuestion(id);
      case WRITER:
      case ADMIN:
        return getAllRepliesForQuestion(id);
      default:
        break;
    }
    return new ArrayList<>();
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
    Reply reply = QuestionManagerProvider.getQuestionManager().getReply(replyId);
    setCurrentReply(reply);
    return reply;
  }

  /*
   * Retrieve current reply
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
    newQuestion = new Question(getUserId(), getComponentId());
    return newQuestion;
  }

  public void setNewQuestionContent(String title, String content, String categoryId) {
    newQuestion.setTitle(title);
    newQuestion.setContent(content);
    newQuestion.setCategoryId(categoryId);
  }

  /*
   * Store the new question and notify subscribers and experts.
   */
  public long saveNewQuestion() throws QuestionReplyException {
    long questionId = QuestionManagerProvider.getQuestionManager().createQuestion(newQuestion);
    newQuestion.getPK().setId(String.valueOf(questionId));
    notifyQuestion(newQuestion);
    notifyQuestionFromExpert(newQuestion);
    return questionId;
  }

  /*
   * Retourne une nouvelle réponse (questionId, creatorId, creationDate) pour la questionCourante
   * met la question en session : newReply
   */
  public Reply getNewReply() {
    Reply reply;
    if ((getCurrentQuestion() != null) && (getCurrentQuestion().getPK() != null)) {
      reply = new Reply(((IdPK) getCurrentQuestion().getPK()).getIdAsLong(), getUserId());
    } else {
      reply = new Reply(getUserId());
    }
    newReply = reply;
    return newReply;
  }

  /*
   * initialise le contenu de la réponse à créer
   */
  public void setNewReplyContent(String title, String content, int publicReply, int privateReply) {
    newReply.setTitle(title);
    newReply.writeWysiwygContent(content);
    newReply.setPublicReply(publicReply);
    newReply.setPrivateReply(privateReply);
  }

  /**
   * Persist new FAQ inside database and add positions
   * @return question identifier
   * @throws QuestionReplyException
   */
  public long saveNewFAQ(Collection<UploadedFile> uploadedFiles) throws QuestionReplyException {
    newQuestion.setStatus(Question.CLOSED); // close
    newQuestion.setReplyNumber(1);
    newQuestion.setPublicReplyNumber(1);
    newQuestion.setPrivateReplyNumber(0);
    newReply.setPublicReply(1);
    newReply.setPrivateReply(0);
    WAPrimaryKey pk = newReply.getPK();
    pk.setComponentName(getComponentId());
    newReply.setPK(pk);

    long questionId =
        QuestionManagerProvider.getQuestionManager().createQuestionReply(newQuestion, newReply);

    addFilesToReply(uploadedFiles, newReply);

    return questionId;
  }

  private void addFilesToReply(Collection<UploadedFile> uploadedFiles, Reply reply) {
    Attachments.from(uploadedFiles).attachTo(LocalizedContribution.from(reply));
  }

  /*
   * enregistre la nouvelle réponse de la question courante met en session la question modifiée
   */
  public void saveNewReply(Collection<UploadedFile> uploadedFiles) throws QuestionReplyException {
    WAPrimaryKey pk = newReply.getPK();
    pk.setComponentName(getComponentId());
    newReply.setPK(pk);
    QuestionManagerProvider.getQuestionManager().createReply(newReply, getCurrentQuestion());

    addFilesToReply(uploadedFiles, newReply);

    getQuestion(((IdPK) getCurrentQuestion().getPK()).getIdAsLong());
    notifyReply(newReply);
  }

  public void updateCurrentQuestion(String title, String content, String categoryId)
      throws QuestionReplyException {
    getCurrentQuestion().setTitle(title);
    getCurrentQuestion().setContent(content);
    getCurrentQuestion().setCategoryId(categoryId);
    QuestionManagerProvider.getQuestionManager().updateQuestion(getCurrentQuestion());
  }

  public void updateCurrentReply(String title, String content) throws QuestionReplyException {
    Reply reply = getCurrentReply();
    reply.setTitle(title);
    reply.writeWysiwygContent(content);
    WAPrimaryKey pk = reply.getPK();
    pk.setComponentName(getComponentId());
    reply.setPK(pk);
    QuestionManagerProvider.getQuestionManager().updateReply(reply);
    getQuestion(((IdPK) getCurrentQuestion().getPK()).getIdAsLong());
  }

  /*
   * Supprime une liste de questions selon le profil de l'utilisateur courant i.e. suppression de
   * toutes les réponses publiques ou privées des questions
   */
  public void deleteQuestions(Collection<Long> questionsIds) throws QuestionReplyException {
    try {
      QuestionManagerProvider.getQuestionManager().deleteQuestionAndReplies(questionsIds);
    } catch (QuestionReplyException e) {
      throw new QuestionReplyException(e);
    }
  }

  public void deleteR(Collection<Long> replyIds) throws QuestionReplyException {
    try {
      deletePrivateReplies(replyIds);
      int rest = deletePublicReplies(replyIds);
      if (isQuestionClosedWithoutAnyReply(rest)) {
        reSetCurrentQuestion();
      } else {
        getQuestion(((IdPK) getCurrentQuestion().getPK()).getIdAsLong());
      }
    } catch (QuestionReplyException e) {
      throw new QuestionReplyException(e);
    }
  }

  private boolean isQuestionClosedWithoutAnyReply(int rest) {
    return (((getCurrentQuestion().getReplyNumber()) == 0) || (rest == 0)) &&
        (getCurrentQuestion().hasClosedStatus());
  }

  /*
   * Clos une liste de questions
   */
  public void closeQuestions(Collection<Long> questionIds) throws QuestionReplyException {
    QuestionManagerProvider.getQuestionManager().closeQuestions(questionIds);
  }

  /*
   * Clos une question si replyNumber = 0, la question sera supprimée => reSetCurrentQuestion sinon
   * met en session la question
   */
  public void closeQuestion(long questionId) throws QuestionReplyException {
    Collection<Long> questionIds = new ArrayList<>();
    questionIds.add(questionId);
    QuestionManagerProvider.getQuestionManager().closeQuestions(questionIds);
  }

  public void openQuestion(long questionId) throws QuestionReplyException {
    Collection<Long> questionIds = new ArrayList<>();
    questionIds.add(questionId);
    QuestionManagerProvider.getQuestionManager().openQuestions(questionIds);
  }

  /*
   * Supprime les réponses publiques et retourne le nombre de réponses publiques restantes.
   */
  private int deletePublicReplies(Collection<Long> replyIds) throws QuestionReplyException {
    QuestionManagerProvider.getQuestionManager()
        .updateRepliesPublicStatus(replyIds, getCurrentQuestion());
    return getCurrentQuestion().getPublicReplyNumber();
  }

  /*
   * Supprime les réponses privées =>
   * QuestionManagerProvider.getQuestionManager().updateRepliesPrivateStatus() retourne le nombre de
   * réponses privées restantes
   */
  private int deletePrivateReplies(Collection<Long> replyIds) throws QuestionReplyException {
    QuestionManagerProvider.getQuestionManager()
        .updateRepliesPrivateStatus(replyIds, getCurrentQuestion());
    return getCurrentQuestion().getPrivateReplyNumber();
  }

  /*
   * Retourne la liste des questions de l'utilisateur de rôle User i.e. liste des questions avec
   * réponses publiques => QuestionManagerProvider.getQuestionManager().getPublicQuestions()
   */
  private Collection<Question> getUserQuestions() throws QuestionReplyException {
    return QuestionManagerProvider.getQuestionManager().getPublicQuestions(getComponentId());
  }

  /*
   * Retourne la liste des questions de l'utilisateur de rôle Writer (expert) i.e. liste des
   * questions dont il est le destinataire non close =>
   * QuestionManagerProvider.getQuestionManager().getReceiveQuestions()
   */
  private Collection<Question> getWriterQuestions() throws QuestionReplyException {
    return QuestionManagerProvider.getQuestionManager()
        .getReceiveQuestions(getUserId(), getComponentId());
  }

  /*
   * Retourne la liste des questions de l'utilisateur de rôle Publisher (demandeur) i.e. liste des
   * questions dont il est l'auteur non close ou close avec réponses privées =>
   * QuestionManagerProvider.getQuestionManager().getSendQuestions()
   */
  private Collection<Question> getPublisherQuestions() throws QuestionReplyException {
    return QuestionManagerProvider.getQuestionManager()
        .getSendQuestions(getUserId(), getComponentId());
  }

  /*
   * Retourne la liste des questions de l'utilisateur de rôle Admin (animateur) i.e. liste des
   * questions non close ou close avec réponses publiques =>
   * QuestionManagerProvider.getQuestionManager().getQuestions()
   */
  private Collection<Question> getAdminQuestions() throws QuestionReplyException {
    return QuestionManagerProvider.getQuestionManager().getQuestions(getComponentId());
  }

  /*
   * liste les réponses publiques d'une question
   */
  private Collection<Reply> getPublicRepliesForQuestion(long id) throws QuestionReplyException {
    return QuestionManagerProvider.getQuestionManager()
        .getQuestionPublicReplies(id, getComponentName());
  }

  /*
   * liste les réponses privées d'une question
   */
  private Collection<Reply> getPrivateRepliesForQuestion(long id) throws QuestionReplyException {
    return QuestionManagerProvider.getQuestionManager()
        .getQuestionPrivateReplies(id, getComponentName());
  }

  /*
   * liste les réponses à une question
   */
  private Collection<Reply> getAllRepliesForQuestion(long id) throws QuestionReplyException {
    return QuestionManagerProvider.getQuestionManager().getQuestionReplies(id, getComponentName());
  }

  public final void setUserProfil() {
    this.userProfil = getHighestSilverpeasUserRole();
  }

  public void setUserProfil(String profil) {
    this.userProfil = SilverpeasRole.fromString(profil);
  }

  public String getUserProfil() {
    return this.userProfil.getName();
  }

  public SilverpeasRole getUserRole() {
    return this.userProfil;
  }

  @Override
  public SilverpeasRole getHighestSilverpeasUserRole() {
    SilverpeasRole highestUserRole = SilverpeasRole.getHighestFrom(getSilverpeasUserRoles());
    if (highestUserRole == null || SilverpeasRole.USER.isGreaterThanOrEquals(highestUserRole)) {
      highestUserRole = SilverpeasRole.USER;
    }
    return highestUserRole;
  }

  public boolean isUserExpert() {
    Collection<SilverpeasRole> userRoles = getSilverpeasUserRoles();
    return userRoles.contains(SilverpeasRole.WRITER) || userRoles.contains(SilverpeasRole.ADMIN);
  }

  /*
   * Retourne true si la liste contient deja le user
   */
  private boolean exist(UserDetail user, Collection<UserDetail> listUser) {
    if (user != null) {
      String idUser = user.getId();
      for (UserDetail currentUser : listUser) {
        if (currentUser.getId().equals(idUser)) {
          return true;
        }
      }
      return false;
    }
    return true;
  }

  /*
   * Récupère la liste des positions d'une question
   */
  public SearchContext getSilverContentIdPosition() throws QuestionReplyException {
    try {
      return PdcManager.get()
          .getSilverContentIdSearchContext(Integer.parseInt(getCurrentQuestionContentId()),
              getComponentId());
    } catch (Exception e) {
      throw new QuestionReplyException(e);
    }
  }

  public String genericWriters() throws QuestionReplyException {
    // This method is no more used at the moment (Silverpeas 6.0)
    return "";
  }

  /*
   * Récupère la liste des experts du domaine de la question
   */
  public Collection<UserDetail> getCurrentQuestionWriters() throws QuestionReplyException {
    OrganizationController orga = getOrganisationController();
    List<UserDetail> arrayUsers = new ArrayList<>();

    try {
      ContentManagementEngine contentMgtEngine = ContentManagementEngineProvider.getContentManagementEngine();
      // recupere la liste de toutes les instances d'annuaire
      String[] instances = orga.getCompoId("whitePages");
      List<String> listeInstanceId = new ArrayList<>();
      for(String id : instances) {
        listeInstanceId.add("whitePages" + id);
      }

      // recupere la liste de tous les experts du domaine de classement de la
      // question
      SearchContext position = getSilverContentIdPosition();
      if (position != null && !position.isEmpty()) {
        PdcManager pdc = PdcManager.get();
        List<Integer> liste = pdc.findSilverContentIdByPosition(position, listeInstanceId);

        CardManager cardManager = CardManager.getInstance();
        for (Integer silverContentId : liste) {
          String internalContentId = contentMgtEngine.getInternalContentId(silverContentId);
          long userCardId = Long.parseLong(internalContentId);
          Card card = cardManager.getCard(userCardId);
          if (card != null) {
            String idUser = card.getUserId();
            UserDetail user = orga.getUserDetail(idUser);
            if (!exist(user, arrayUsers)) {
              arrayUsers.add(user);
            }
          }
        }
      }
    } catch (Exception e) {
      throw new QuestionReplyException(e);
    }

    return arrayUsers;

  }

  /**
   * @param question the current question-reply question
   * @param recipientIds list of users to notify
   */
  private void notifyTemplateQuestion(Question question, Collection<String> recipientIds) {
    final User sender = getUserDetail(getUserId());
    buildAndSend(new QuestionNotifier(sender, question, recipientIds));
  }

  /**
   * @param question
   */
  private void notifyQuestion(Question question) {
    notifyTemplateQuestion(question,
        question.readRecipients().stream().map(Recipient::getUserId).collect(Collectors.toSet()));
  }

  /**
   * @param question
   */
  private void notifyQuestionFromExpert(Question question) {
    final List<String> profiles = singletonList(SilverpeasRole.WRITER.getName());
    String[] usersIds = getOrganisationController().getUsersIdsByRoleNames(getComponentId(), profiles);
    notifyTemplateQuestion(question, Stream.of(usersIds).collect(Collectors.toSet()));
  }

  /**
   * @param reply
   */
  private void notifyReply(final Reply reply) {
    final User sender = getUserDetail(getUserId());
    final String recipientId = getCurrentQuestion().getCreatorId();
    buildAndSend(new ReplyNotifier(sender, getCurrentQuestion(), reply, recipientId));
  }

  public QuestionReplySessionController(MainSessionController mainSessionCtrl,
      ComponentContext context, String multilangBaseName, String iconBaseName) {
    super(mainSessionCtrl, context, multilangBaseName, iconBaseName);
    setUserProfil();
  }

  public String getCurrentQuestionContentId() {
    String contentId = null;

    if (currentQuestion != null) {
      try {
        ContentManagementEngine contentMgtEngine = ContentManagementEngineProvider.getContentManagementEngine();
        contentId = String.valueOf(contentMgtEngine
            .getSilverContentId(currentQuestion.getPK().getId(), currentQuestion.getInstanceId()));
      } catch (ContentManagerException ignored) {
        SilverLogger.getLogger(this).error(ignored);
        contentId = null;
      }
    }

    return contentId;
  }

  public boolean isPrivateRepliesEnabled() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("privateRepliesUsed"));
  }

  public boolean isPDCUsed() {
    if (!StringUtil.isDefined(getComponentParameterValue("usePdc"))) {
      return true;
    }
    return "yes".equalsIgnoreCase(getComponentParameterValue("usePdc"));
  }

  public Collection<NodeDetail> getAllCategories() throws QuestionReplyException {
    try {
      NodePK nodePK = new NodePK(NodePK.ROOT_NODE_ID, getComponentId());
      return getNodeService().getChildrenDetails(nodePK);
    } catch (Exception e) {
      throw new QuestionReplyException(e);
    }
  }

  public synchronized void createCategory(Category category) throws QuestionReplyException {
    try {
      category.setCreationDate(new Date());
      category.setCreatorId(getUserId());
      category.getNodePK().setComponentName(getComponentId());
      getNodeService().createNode(category, new NodeDetail());
    } catch (Exception e) {
      throw new QuestionReplyException(e);
    }
  }

  public Category getCategory(String categoryId) throws QuestionReplyException {
    try {
      NodePK nodePK = new NodePK(categoryId, getComponentId());
      return new Category(getNodeService().getDetail(nodePK));
    } catch (Exception e) {
      throw new QuestionReplyException(e);
    }
  }

  public synchronized void updateCategory(Category category) throws QuestionReplyException {
    try {
      getNodeService().setDetail(category);
    } catch (Exception e) {
      throw new QuestionReplyException(e);
    }
  }

  public synchronized void deleteCategory(String categoryId) throws QuestionReplyException {
    try {
      Collection<Question> questions = getQuestionsByCategory(categoryId);
      for (Question question : questions) {
        question.setCategoryId("");
        QuestionManagerProvider.getQuestionManager().updateQuestion(question);
      }
      NodePK nodePk = new NodePK(categoryId, getComponentId());
      getNodeService().deleteNode(nodePk);
    } catch (Exception e) {
      throw new QuestionReplyException(e);
    }
  }

  public ExportReport export(MultiSilverpeasBundle resource)
      throws QuestionReplyException, ParseException {
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
      } catch (org.silverpeas.core.util.UtilException ex) {
        throw new QuestionReplyException(ex);
      }
    }

    // création du dossier "files"
    String dir = tempDir + thisExportDir;
    String nameForFiles = "files";
    File forFiles = new File(dir + File.separator + nameForFiles);
    try {
      FileFolderManager.createFolder(forFiles);
    } catch (org.silverpeas.core.util.UtilException ex) {
      throw new QuestionReplyException(ex);
    }

    // intégrer la css du disque dans "files"
    SettingBundle settings = ResourceLocator.getSettingBundle(
        "org.silverpeas.questionReply.settings.questionReplySettings");
    try {
      String chemin = settings.getString("mappingDir");
      if (chemin.startsWith("file:")) {
        chemin = chemin.substring(8);
      }
      Collection<File> files = FileFolderManager.getAllFile(chemin);
      for (File file : files) {
        File newFile =
            new File(dir + File.separator + nameForFiles + File.separator + file.getName());
        FileRepositoryManager.copyFile(file.getPath(), newFile.getPath());
      }
    } catch (Exception ex) {
      throw new QuestionReplyException(ex);
    }

    // création du fichier html
    final File fileHTML = new File(dir + File.separator + thisExportDir + ".html");
    try {
      if (fileHTML.createNewFile()) {
        try(Writer fileWriter = new OutputStreamWriter(new FileOutputStream(fileHTML.getPath()), UTF_8)) {
          fileWriter.write(toHTML(fileHTML, resource));
        }
      }
    } catch (IOException ex) {
      throw new QuestionReplyException(ex);
    }

    // Création du zip
    try {
      String zipFileName = fileExportDir.getName() + ".zip";
      long zipFileSize = ZipUtil.compressPathToZip(fileExportDir.getPath(), tempDir + zipFileName);
      exportReport.setZipFileName(zipFileName);
      exportReport.setZipFileSize(zipFileSize);
      exportReport.setZipFilePath(FileServerUtils.getUrlToTempDir(zipFileName));
    } catch (Exception ex) {
      throw new QuestionReplyException(ex);
    }
    // Stockage de la date de fin de l'export dans l'objet rapport
    exportReport.setDateFin(new Date());
    return exportReport;
  }

  private String toHTML(File file, MultiSilverpeasBundle resource)
      throws QuestionReplyException, ParseException {
    String fileName = file.getName();
    StringBuilder sb = new StringBuilder();

    sb.append("<HTML>\n");
    sb.append("<HEAD>\n");

    sb.append("<TITLE>").append(fileName).append("</TITLE>\n");
    sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
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

  private String addFunction() {
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

  private String addBody(MultiSilverpeasBundle resource, File file)
      throws QuestionReplyException, ParseException {
    StringBuilder sb = new StringBuilder();
    sb.append("<table width=\"100%\">\n");
    Collection<NodeDetail> categories = getAllCategories();
    QuestionReplyExport exporter = new QuestionReplyExport(resource, file);
    for (NodeDetail category : categories) {
      String categoryId = category.getId();
      exportCategory(exporter, category, categoryId, sb);
    }
    NodeDetail fakeCategory = new NodeDetail();
    fakeCategory.setName("");
    exportCategory(exporter, fakeCategory, null, sb);

    sb.append("</table>\n");
    return sb.toString();
  }

  private void exportCategory(QuestionReplyExport exporter, NodeDetail category, String categoryId,
      StringBuilder sb) throws QuestionReplyException, ParseException {
    // titre de la catégorie
    sb.append("<tr>\n");
    sb.append("<td class=\"titreCateg\" width=\"91%\">").append(category.getName())
        .append("</td>\n");
    sb.append("</tr>\n");
    // contenu de la catégorie
    sb.append("<tr>\n");
    sb.append("<td colspan=\"2\">\n");
    Collection<Question> questions = getQuestionsByCategory(categoryId);
    for (Question question : questions) {
      exporter.exportQuestion(question, sb, this);
    }
    sb.append("</td>\n");
    sb.append("</tr>\n");
  }

  private NodeService getNodeService() {
    return NodeService.get();
  }

  /**
   * Classify the question reply FAQ on the PdC only if the positions parameter is filled
   * @param questionId the question identifier
   * @param positions the json string positions
   */
  public void classifyQuestionReply(long questionId, String positions) {
    // First get the questionSilverpeasContent
    QuestionDetail questionDetail = null;
    try {
      questionDetail = new QuestionDetail(getQuestion(questionId));
    } catch (QuestionReplyException e1) {
      SilverLogger.getLogger(this).error(e1);
    }

    if (StringUtil.isDefined(positions) && questionDetail != null) {
      PdcClassificationEntity qiClassification = null;
      try {
        qiClassification = PdcClassificationEntity.fromJSON(positions);
      } catch (DecodingException e) {
        SilverLogger.getLogger(this).error(e);
      }
      if (qiClassification != null && !qiClassification.isUndefined()) {
        List<PdcPosition> pdcPositions = qiClassification.getPdcPositions();
        PdcClassification classification =
            aPdcClassificationOfContent(questionDetail).withPositions(pdcPositions);
        classification.classifyContent(questionDetail);
      }
    }
  }

}
