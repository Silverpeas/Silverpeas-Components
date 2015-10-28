/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.forums.control;

import com.silverpeas.notification.builder.helper.UserNotificationHelper;
import com.silverpeas.pdc.PdcServiceFactory;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.pdc.service.PdcClassificationService;
import com.silverpeas.pdc.web.PdcClassificationEntity;
import com.silverpeas.subscribe.SubscriptionServiceProvider;
import com.silverpeas.subscribe.service.ComponentSubscription;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.CollectionUtil;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.forums.bean.ForumModeratorBean;
import com.stratelia.webactiv.forums.forumsException.ForumsException;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBM;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsServiceProvider;
import com.stratelia.webactiv.forums.models.Forum;
import com.stratelia.webactiv.forums.models.ForumDetail;
import com.stratelia.webactiv.forums.models.ForumPK;
import com.stratelia.webactiv.forums.models.Message;
import com.stratelia.webactiv.forums.models.MessagePK;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.statistic.control.StatisticBm;
import com.stratelia.webactiv.util.statistic.model.StatisticRuntimeException;
import org.silverpeas.components.forum.notification.ForumsForumSubscriptionUserNotification;
import org.silverpeas.components.forum.notification.ForumsMessagePendingValidationUserNotification;
import org.silverpeas.components.forum.notification.ForumsMessageSubscriptionUserNotification;
import org.silverpeas.components.forum.notification.ForumsMessageValidationUserNotification;
import org.silverpeas.upload.UploadedFile;
import org.silverpeas.util.error.SilverpeasTransverseErrorUtil;

import javax.ejb.EJBException;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.silverpeas.pdc.model.PdcClassification.aPdcClassificationOfContent;
import static com.stratelia.webactiv.SilverpeasRole.*;
import static com.stratelia.webactiv.forums.models.Message.*;

/**
 * Cette classe gere la session de l'acteur durant sa navigation dans les forums
 *
 * @author frageade
 * @since September 2000
 */
public class ForumsSessionController extends AbstractComponentSessionController {

  public static final String STAT_TYPE = "ForumMessage";
  /**
   * Le Business Manager
   */
  private ForumsBM forumsBM;
  /**
   * ids des forums deployes
   */
  private List<Integer> deployedForums;
  /**
   * ids des messages deployes
   */
  private List<Integer> deployedMessages;
  /**
   * utilise pour notifier les utilisateurs
   */
  private NotificationSender notifSender = null;
  public String typeMessages = "Messages";
  public String typeSubjects = "Subjects";
  private ResourceLocator settings = null;
  private PublicationBm publicationBm = null;
  private StatisticBm statisticBm = null;
  private boolean displayAllMessages = true;
  private boolean external = false;
  private boolean resizeFrame = false;
  private List<PdcPosition> positions = null;
  private boolean componentSubscriptionInfoDisplayed = false;

  // Constructeur
  public ForumsSessionController(MainSessionController mainSessionCtrl, ComponentContext context) {
    super(mainSessionCtrl, context, "org.silverpeas.forums.multilang.forumsBundle",
        "org.silverpeas.forums.settings.forumsIcons");
    deployedMessages = new ArrayList<Integer>();
    deployedForums = new ArrayList<Integer>();
  }

  public NotificationSender getNotificationSender() {
    if (notifSender == null) {
      notifSender = new NotificationSender(getComponentId());
    }
    return notifSender;
  }

  public Forum[] getForumsList() {
    List<Forum> forums = getForumsService().getForums(new ForumPK(getComponentId(), getSpaceId()));
    return forums.toArray(new Forum[forums.size()]);
  }

  public Forum[] getForumsListByCategory(String categoryId) {
    SilverTrace.debug("forums", "ForumsSessionController.getForumsListByCategory()", "",
        "categoryId = " + categoryId);
    ForumPK forumPK = new ForumPK(getComponentId(), getSpaceId());
    List<Forum> forums = getForumsService().getForumsByCategory(forumPK, categoryId);
    Forum[] result = forums.toArray(new Forum[forums.size()]);
    SilverTrace.debug("forums", "ForumsSessionController.getForumsListByCategory()", "",
        "retour = " + result);
    return result;
  }

  public Forum getForum(int forumId) {
    return getForumsService().getForum(getForumPK(forumId));
  }

  public String getForumName(int forumId) {
    return getForumsService().getForumName(forumId);
  }

  public boolean isForumActive(int forumId) {
    return getForumsService().isForumActive(forumId);
  }

  public int getForumParentId(int forumId) {
    return getForumsService().getForumParentId(forumId);
  }

  public int[] getForumSonsIds(int forumId) {
    List<String> ids = getForumsService().getForumSonsIds(getForumPK(forumId));
    int[] sonsIds = new int[ids.size()];
    for (int i = 0; i < ids.size(); i++) {
      sonsIds[i] = Integer.parseInt(ids.get(i));
    }
    return sonsIds;
  }

  public int getForumSonsNb(int forumId) {
    return getForumsService().getForumSonsIds(getForumPK(forumId)).size();
  }

  public void deployForum(int id) {
    deployedForums.add(id);
  }

  public void deployAllMessages(int forumId) {
    Message[] messages = getMessagesList(forumId);
    for (Message message : messages) {
      deployMessage(message.getId());
    }
  }

  public void undeployForum(int id) {
    deployedForums.remove(id);
  }

  public boolean forumIsDeployed(int id) {
    return deployedForums.contains(id);
  }

  public void lockForum(int id, int level) {
    ForumPK forumPK = new ForumPK(getComponentId(), String.valueOf(id));
    getForumsService().lockForum(forumPK, level);
  }

  public int unlockForum(int id, int level) {
    ForumPK forumPK = new ForumPK(getComponentId(), String.valueOf(id));
    return getForumsService().unlockForum(forumPK, level);
  }

  /**
   * Create a new forum and persist it inside datasource
   *
   * @param forumName forum name
   * @param forumDescription forum description
   * @param forumCreator creator user identifier
   * @param forumParent parent forum identifier
   * @param keywords the keywords.
   * @return identifier of the new forum
   * @author frageade
   * @since 02 Octobre 2000
   */
  public int createForum(String forumName, String forumDescription, String forumCreator,
      int forumParent, String keywords) {
    return createForum(forumName, forumDescription, forumCreator, forumParent, "0", keywords);
  }

  public int createForum(String forumName, String forumDescription, String forumCreator,
      int forumParent, String categoryId, String keywords) {
    ForumPK forumPK = new ForumPK(getComponentId(), getSpaceId());
    String currentCategoryId = categoryId;
    if (!StringUtil.isDefined(categoryId)) {
      currentCategoryId = null;
    }
    int forumId = getForumsService().createForum(forumPK, truncateTextField(forumName),
        truncateTextArea(forumDescription), forumCreator, forumParent, currentCategoryId, keywords);

    // Send notification
    sendForumNotification(getForumsService().getForumDetail(getForumPK(forumId)), NotifAction.CREATE);

    // Classify content here
    classifyContent(forumPK);
    return forumId;
  }

  /**
   * Met a jour les informations sur un forum dans la datasource
   *
   * @param forumId l'ID du forum dans la datasource
   * @param forumName forum name
   * @param forumDescription forum description
   * @param forumParent parent forum identifier
   * @param keywords the keywords.
   * @author frageade
   * @since 03 Octobre 2000
   */
  public void updateForum(int forumId, String forumName, String forumDescription, int forumParent,
      String keywords) {
    updateForum(forumId, forumName, forumDescription, forumParent, null, keywords);
  }

  public void updateForum(int forumId, String forumName, String forumDescription, int forumParent,
      String categoryId, String keywords) {
    getForumsService().updateForum(getForumPK(forumId), truncateTextField(forumName),
        truncateTextArea(forumDescription), forumParent, categoryId, keywords);

    // Send notification
    sendForumNotification(getForumsService().getForumDetail(getForumPK(forumId)), NotifAction.UPDATE);
  }

  /**
   * Supprime un forum et tous ses sous-forums a partir de son ID
   *
   * @param forumId l'ID du forum dans la datasource
   * @author frageade
   * @since 3 Octobre 2000
   */
  public void deleteForum(int forumId) {
    getForumsService().deleteForum(getForumPK(forumId));
  }

  /**
   * Indexe un forum a partir de son ID
   *
   * @param forumId l'ID du forum dans la datasource
   * @author frageade
   * @since 23 Aout 2001
   */
  public void indexForum(int forumId) {
    getForumsService().createIndex(getForumPK(forumId));
  }

  // Methodes messages
  /**
   * Liste les messages d'un forum
   *
   * @param forumId id du forum
   * @return Vector la liste des messages
   * @author frageade
   * @since 04 Octobre 2000
   */
  public Message[] getMessagesList(int forumId) {
    Collection<Message> messages = getForumsService().getMessages(getForumPK(forumId));
    return messages.toArray(new Message[messages.size()]);
  }

  public Message[] getMessagesList(int forumId, int messageId) throws ForumsException {
    List<Message> messageList = new ArrayList<Message>();
    Message[] messages = getMessagesList(forumId);
    int i = 0;
    boolean parentMessageFound = false;
    while (i < messages.length && !parentMessageFound) {
      Message message = messages[i];
      int currentMessageId = message.getId();
      parentMessageFound = isVisible(message.getStatus(), forumId) && messageId == currentMessageId;
      if (parentMessageFound) {
        messageList.add(message);
      }
      i++;
    }
    fillMessageList(messageList, messages, messageId);
    return messageList.toArray(new Message[messageList.size()]);
  }

  protected boolean isVisible(String status, int forumId) throws ForumsException {
    return isModerator(getUserId(), forumId) || STATUS_VALIDATE.equals(status);
  }

  private void fillMessageList(List<Message> messageList, Message[] messages,
      int messageId) throws ForumsException {
    for (int i = 0; i < messages.length; i++) {
      Message message = messages[i];
      int forumId = message.getForumId();
      if (isVisible(message.getStatus(), forumId) || "admin".equals(getUserRoleLevel())
          || message.getAuthor().equals(getUserId())) {
        if (message.getParentId() == messageId) {
          messageList.add(message);
          fillMessageList(messageList, messages, message.getId());
        }
      }
    }
  }

  /**
   * Récupère le dernier message d'un forum
   *
   * @param forumId id du forum
   * @return String les champs du dernier message
   * @author sfariello
   * @since
   */
  public Object[] getLastMessage(int forumId) {
    return getLastMessage(forumId, -1);
  }

  public Object[] getLastMessage(int forumId, int messageId) {
    Message message;
    if (messageId != -1) {
      message = getForumsService().getLastMessage(getForumPK(forumId), messageId, STATUS_VALIDATE);
    } else {
      message = getForumsService().getLastMessage(getForumPK(forumId), STATUS_VALIDATE);
    }
    if (message != null) {
      UserDetail user = getUserDetail(message.getAuthor());
      SilverTrace.debug("forums", "ForumsSessioncontroller.getLastMessage()",
          "root.MSG_GEN_ENTER_METHOD", "message = " + message.toString());
      return new Object[]{String.valueOf(message.getId()), message.getDate(),
        (user != null ? user.getDisplayedName() : "Unknown")};
    }
    return null;
  }

  /**
   * Nombre de sujets d'un forum
   *
   * @param forumId id du forum
   * @return int le nombre de sujets
   * @author sfariello
   * @since 07 Décembre 2007
   */
  public int getNbSubjects(int forumId) {
    return getForumsService().getNbMessages(forumId, typeSubjects, STATUS_VALIDATE);
  }

  /**
   * Nombre de messages d'un forum
   *
   * @param forumId id du forum
   * @return int le nombre de messages
   * @author sfariello
   * @since 07 Décembre 2007
   */
  public int getNbMessages(int forumId) {
    return getForumsService().getNbMessages(forumId, typeMessages, STATUS_VALIDATE);
  }

  public int getAuthorNbMessages(String userId) {
    return getForumsService().getAuthorNbMessages(userId, STATUS_VALIDATE);
  }

  public int getNbResponses(int forumId, int messageId) {
    return getForumsService().getNbResponses(forumId, messageId, STATUS_VALIDATE);
  }

  /**
   * Recupere les infos d'un message
   *
   * @param messageId id du message
   * @return Vector la liste des champs du message
   * @author frageade
   * @since 04 Octobre 2000
   */
  public Message getMessage(int messageId) {
    return getForumsService().getMessage(getMessagePK(messageId));
  }

  public String getMessageTitle(int messageId) {
    return getForumsService().getMessageTitle(messageId);
  }

  public int getMessageParentId(int messageId) {
    return getForumsService().getMessageParentId(messageId);
  }

  /**
   * Cree un nouveau message dans la datasource
   *
   *
   *
   * @param title titre du message
   * @param author id de l'auteur du message
   * @param forumId id du forum
   * @param parentId id du message parent
   * @param text texte du message
   * @param keywords the keywords
   * @return String l'id du message créé
   * @author frageade
   * @since 04 Octobre 2000
   */
  public int createMessage(String title, String author, int forumId, int parentId, String text,
      String keywords, Collection<UploadedFile> uploadedFiles) {
    String status = STATUS_FOR_VALIDATION;

    MessagePK messagePK = new MessagePK(getComponentId(), getSpaceId());
    int messageId = 0;

    try {
      if (!isValidationActive() || admin.isInRole(getUserRoleLevel()) ||
          isModerator(getUserId(), forumId)) {
        status = STATUS_VALIDATE;
      }
      // creation du message dans la base
      messageId = getForumsService().createMessage(messagePK, truncateTextField(title), author, null,
          forumId, parentId, text, keywords, status);
    } catch (Exception e) {
      SilverpeasTransverseErrorUtil.stopTransverseErrorIfAny(new EJBException(e.getMessage(), e));
      return messageId;
    }
    messagePK.setId(String.valueOf(messageId));

    // Send notification to subscribers
    if (STATUS_VALIDATE.equals(status)) {
      // seulement si le message est valide
      sendMessageNotification(getMessage(messageId), NotifAction.CREATE);
    } else {
      // envoie notification si demande de validation
      sendMessageNotificationToValidate(getMessage(messageId));
    }

    // Attach uploaded files
    try {
      if (com.silverpeas.util.CollectionUtil.isNotEmpty(uploadedFiles)) {
        for (UploadedFile uploadedFile : uploadedFiles) {
          // Register attachment
          uploadedFile.registerAttachment(messagePK, I18NHelper.defaultLanguage, false);
        }
      }
    } catch (RuntimeException re) {
      SilverpeasTransverseErrorUtil.stopTransverseErrorIfAny(re);
    }

    return messageId;
  }

  public void updateMessage(int messageId, String title, String text) {
    updateMessage(messageId, title, text, null);
  }

  public void updateMessage(int messageId, String title, String text, String status) {
    MessagePK messagePK = getMessagePK(messageId);
    Message message = getMessage(messageId);
    String currentStatus = status;
    try {
      if (currentStatus == null) {
        currentStatus = STATUS_FOR_VALIDATION;
        if (!isValidationActive() || admin.isInRole(getUserRoleLevel()) ||
            isModerator(getUserId(), message.getForumId())) {
          currentStatus = STATUS_VALIDATE;
        }
      }
      getForumsService().updateMessage(messagePK, truncateTextField(title), text, getUserId(),
          currentStatus);
    } catch (Exception e) {
      SilverpeasTransverseErrorUtil.stopTransverseErrorIfAny(new EJBException(e.getMessage(), e));
      return;
    }

    // Send notification to subscribers
    if (STATUS_VALIDATE.equals(currentStatus)) {
      // seulement si le message est valide
      sendMessageNotification(getMessage(messageId), NotifAction.UPDATE);
    } else if (STATUS_FOR_VALIDATION.equals(currentStatus)) {
      // envoie notification si demande de validation
      sendMessageNotificationToValidate(getMessage(messageId));
    }
  }

  public void updateMessageKeywords(int messageId, String keywords) {
    getForumsService().updateMessageKeywords(getMessagePK(messageId), keywords);
  }

  public void setResizeFrame(boolean resizeFrame) {
    this.resizeFrame = resizeFrame;
  }

  public boolean isResizeFrame() {
    return resizeFrame;
  }

  public void setExternal(boolean external) {
    this.external = external;
  }

  public boolean isExternal() {
    return external;
  }

  private void sendForumNotification(ForumDetail forum, NotifAction action) {
    UserNotificationHelper.buildAndSend(new ForumsForumSubscriptionUserNotification(forum, action));
  }

  private void sendMessageNotification(Message message, NotifAction action) {
    UserNotificationHelper.buildAndSend(new ForumsMessageSubscriptionUserNotification(message, action));
  }

  private void sendMessageNotificationToValidate(Message message) {
    UserNotificationHelper
        .buildAndSend(new ForumsMessagePendingValidationUserNotification(message));
  }

  private void sendMessageNotificationAfterValidation(Message message) {
    UserNotificationHelper
        .buildAndSend(new ForumsMessageValidationUserNotification(message, getUserId()));
  }

  private void sendMessageNotificationRefused(Message message, String motive) {
    UserNotificationHelper.buildAndSend(
        new ForumsMessageValidationUserNotification(message, getUserId(), motive));
  }

  /**
   * Indexe un message a partir de son ID
   *
   * @param messageId l'ID du message dans la datasource
   * @author frageade
   * @since 23 Aout 2001
   */
  public void indexMessage(int messageId) {
    getForumsService().createIndex(getMessagePK(messageId));
  }

  /**
   * Supprime un message et tous ses sous-messages a partir de son ID
   *
   * @param messageId l'ID du message dans la datasource
   * @author frageade
   * @since 04 Octobre 2000
   */
  public void deleteMessage(int messageId) {
    getForumsService().deleteMessage(getMessagePK(messageId));
  }

  public void deployMessage(int id) {
    deployedMessages.add(id);
  }

  public void undeployMessage(int id) {
    if (!deployedMessages.isEmpty()) {
      Iterator<Integer> iter = deployedMessages.iterator();
      while (iter.hasNext()) {
        Integer value = iter.next();
        if (value == id) {
          iter.remove();
          return;
        }
      }
    }
  }

  public boolean messageIsDeployed(int id) {
    for (Integer value : deployedMessages) {
      if (value == id) {
        return true;
      }
    }
    return false;
  }

  public boolean isReader() {
    if (!isAdmin() || isUser()) {
      String[] profiles = getUserRoles();
      for (String profile : profiles) {
        if (reader.isInRole(profile)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isUser() {
    String[] profiles = getUserRoles();
    for (String profile : profiles) {
      if (user.isInRole(profile)) {
        return true;
      }
    }
    return false;
  }

  public boolean isAdmin() {
    String[] profiles = getUserRoles();
    for (String profile : profiles) {
      if (admin.isInRole(profile)) {
        return true;
      }
    }
    return false;
  }

  public boolean isModerator(String userId, int forumId) throws ForumsException {
    boolean result = getForumsService().isModerator(userId, getForumPK(forumId));
    int parentId = getForumParentId(forumId);
    while ((!result) && (parentId != 0)) {
      result = (getForumsService().isModerator(userId, getForumPK(parentId)));
      parentId = getForumParentId(parentId);
    }
    return result;
  }

  public void addModerator(int forumId, String userId) {
    getForumsService().addModerator(getForumPK(forumId), userId);
  }

  public void removeModerator(int forumId, String userId) {
    getForumsService().removeModerator(getForumPK(forumId), userId);
  }

  public void removeAllModerators(int forumId) {
    getForumsService().removeAllModerators(getForumPK(forumId));
  }

  public ForumModeratorBean getModerators(int forumId) {
    return ForumModeratorBean.from(forumId, getForumsService().getModerators(forumId));
  }

  public void moveMessage(int messageId, int forumId) {
    getForumsService().moveMessage(getMessagePK(messageId), getForumPK(forumId));
  }

  public Message subscribeMessage(int messageId) {
    MessagePK messagePK = getMessagePK(messageId);
    getForumsService().subscribeMessage(messagePK, getUserId());
    return getForumsService().getMessage(messagePK);
  }

  public Message unsubscribeMessage(int messageId) {
    MessagePK messagePK = getMessagePK(messageId);
    getForumsService().unsubscribeMessage(messagePK, getUserId());
    return getForumsService().getMessage(messagePK);
  }

  public Forum subscribeForum(int forumId) {
    ForumPK forumPK = getForumPK(forumId);
    getForumsService().subscribeForum(forumPK, getUserId());
    return getForumsService().getForum(forumPK);
  }

  public Forum unsubscribeForum(int forumId) {
    ForumPK forumPK = getForumPK(forumId);
    getForumsService().unsubscribeForum(forumPK, getUserId());
    return getForumsService().getForum(forumPK);
  }

  public void subscribeComponent() {
    SubscriptionServiceProvider.getSubscribeService()
        .subscribe(new ComponentSubscription(getUserId(), getComponentId()));
  }

  public void unsubscribeComponent() {
    SubscriptionServiceProvider.getSubscribeService()
        .unsubscribe(new ComponentSubscription(getUserId(), getComponentId()));
    setComponentSubscriptionInfoDisplayed(false);
  }

  public boolean isMessageSubscriber(int messageId) {
    return getForumsService().isSubscriber(getMessagePK(messageId), getUserId());
  }

  public boolean isMessageSubscriberByInheritance(int messageId) {
    return getForumsService().isSubscriberByInheritance(getMessagePK(messageId), getUserId());
  }

  public boolean isForumSubscriber(int forumId) {
    return getForumsService().isSubscriber(getForumPK(forumId), getUserId());
  }

  public boolean isForumSubscriberByInheritance(int forumId) {
    return getForumsService().isSubscriberByInheritance(getForumPK(forumId), getUserId());
  }

  public boolean isComponentSubscriber() {
    return getForumsService().isSubscriber(getComponentId(), getUserId());
  }

  public boolean isNewMessageByForum(String userId, int forumId) {
    boolean isNewMessage = getForumsService().isNewMessageByForum(userId, getForumPK(forumId),
        STATUS_VALIDATE);
    SilverTrace.info("forums", "ForumsSessionController.isNewMessageByForum()",
        "root.MSG_GEN_PARAM_VALUE", "isNewMessageByForum = " + isNewMessage);
    return isNewMessage;
  }

  public boolean isNewMessage(String userId, int forumId, int messageId) {
    boolean isNewMessage = getForumsService().isNewMessage(userId, getForumPK(forumId), messageId,
        STATUS_VALIDATE);
    SilverTrace.info("forums", "ForumsSessionController.isNewMessage()",
        "root.MSG_GEN_PARAM_VALUE", "isNewMessage = " + isNewMessage);
    return isNewMessage;
  }

  public void setLastVisit(String userId, int messageId) {
    getForumsService().setLastVisit(userId, messageId);
  }

  public UserDetail[] listUsers() {
    UserDetail[] userDetails = CollectionUtil.sortUserDetailArray(getOrganisationController().
        getAllUsers(getComponentId()));
    return (userDetails != null ? userDetails : new UserDetail[0]);
  }

  public String getAuthorName(String userId) {
    UserDetail userDetail = getOrganisationController().getUserDetail(userId);
    return (userDetail != null ? (userDetail.getFirstName() + " " + userDetail.getLastName())
        .trim() : null);
  }

  public UserDetail getAuthor(String userId) {
    return getOrganisationController().getUserDetail(userId);
  }

  public String getAdminIds() {
    return NotificationSender.getIdsLineFromUserArray(getOrganisationController().getUsers(
        getSpaceId(), getComponentId(), "admin"));
  }

  private String truncateTextField(String s) {
    return (s.length() >= DBUtil.getTextFieldLength() ? s.substring(0,
        DBUtil.getTextFieldLength() - 1) : s);
  }

  private String truncateTextArea(String s) {
    return (s.length() >= DBUtil.getTextAreaLength() ? s.substring(0,
        DBUtil.getTextAreaLength() - 1) : s);
  }

  public boolean isPdcUsed() {
    String value = getComponentParameterValue("usePdc");
    return (value != null && "yes".equals(value.toLowerCase()));
  }

  public boolean isUseRss() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("rss"));
  }

  public boolean isForumInsideForum() {
    String value = getComponentParameterValue("forumInsideForum");
    return (value != null && "yes".equals(value.toLowerCase()));
  }

  public int getSilverObjectId(int objectId) {
    return getForumsService().getSilverObjectId(getForumPK(objectId));
  }

  public Collection<NodeDetail> getAllCategories() {
    return getForumsService().getAllCategories(getComponentId());
  }

  public synchronized void createCategory(NodeDetail category) {
    category.setCreationDate(DateUtil.date2SQLDate(new Date()));
    category.setCreatorId(getUserId());
    category.getNodePK().setComponentName(getComponentId());
    getForumsService().createCategory(category);
  }

  public NodeDetail getCategory(String categoryId) {
    // rechercher la catégorie
    NodePK nodePK = new NodePK(categoryId, getComponentId());
    return getForumsService().getCategory(nodePK);
  }

  public synchronized void updateCategory(NodeDetail category) {
    SilverTrace.error("forums", "ForumsSessionController.updateCategory", "",
        "category = " + category.getName());
    getForumsService().updateCategory(category);
  }

  public synchronized void deleteCategory(String categoryId) {
    SilverTrace.error("forums", "ForumsSessionController.deleteCategory", "",
        "categoryId = " + categoryId);
    getForumsService().deleteCategory(categoryId, getComponentId());
  }

  @Override
  public ResourceLocator getSettings() {
    if (settings == null) {
      settings = new ResourceLocator("org.silverpeas.forums.settings.forumsSettings", "");
    }
    return settings;
  }

  public PublicationDetail getDetail(String id) {
    return getPublicationBm().getDetail(new PublicationPK(id, getSpaceId(), getComponentId()));
  }

  public void addMessageStat(int messageId, String userId) {
    getStatisticBm().addStat(userId, new ForeignPK(String.valueOf(messageId), getComponentId()), 1,
        STAT_TYPE);
  }

  public int getMessageStat(int messageId) {
    return getStatisticBm().getCount(new ForeignPK(String.valueOf(messageId), getComponentId()),
        STAT_TYPE);
  }

  public boolean isDisplayAllMessages() {
    return displayAllMessages;
  }

  public void changeDisplayAllMessages() {
    displayAllMessages = !displayAllMessages;
  }

  public void resetDisplayAllMessages() {
    displayAllMessages = true;
  }

  public String getForumKeywords(int forumId) {
    return getForumsService().getForumTags(getForumPK(forumId));
  }

  public String getMessageKeywords(int messageId) {
    return getForumsService().getMessageTags(getMessagePK(messageId));
  }

  public void validateMessage(int messageId) {
    Message message = getMessage(messageId);
    String statusBeforeUpdate = message.getStatus();
    updateMessage(messageId, message.getTitle(), message.getText(), STATUS_VALIDATE);

    // envoie d'une notification au créateur du message
    sendMessageNotificationAfterValidation(message);
    // envoie une notification aux abonnés si le message vient juste de passer à l'état validé
    if (!STATUS_VALIDATE.equals(statusBeforeUpdate)) {
      sendMessageNotification(message, NotifAction.CREATE);
    }
  }

  public void refuseMessage(int messageId, String motive) {
    Message message = getMessage(messageId);
    updateMessage(messageId, message.getTitle(), message.getText(), STATUS_REFUSED);

    sendMessageNotificationRefused(message, motive);
  }

  public boolean isValidationActive() {
    return StringUtil.getBooleanValue(getComponentParameterValue("isValidationActive"));
  }

  private PublicationBm getPublicationBm() {
    if (publicationBm == null) {
      try {
        publicationBm = EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
            PublicationBm.class);
      } catch (Exception e) {
        SilverTrace.error("forum", "ForumSessionController.getPublicationBm()",
            "root.MSG_EJB_CREATE_FAILED", JNDINames.PUBLICATIONBM_EJBHOME, e);
        throw new EJBException(e);
      }
    }
    return publicationBm;
  }

  protected StatisticBm getStatisticBm() {
    if (statisticBm == null) {
      try {
        statisticBm = EJBUtilitaire.
            getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME, StatisticBm.class);
      } catch (Exception e) {
        throw new StatisticRuntimeException("KmeliaSessionController.getStatisticBm()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return statisticBm;
  }

  protected ForumsBM getForumsService() {
    return ForumsServiceProvider.getForumsService();
  }

  protected void setForumsBM(ForumsBM forumsBM) {
    this.forumsBM = forumsBM;
  }

  private ForumPK getForumPK(int forumId) {
    return new ForumPK(getComponentId(), String.valueOf(forumId));
  }

  private MessagePK getMessagePK(int messageId) {
    return new MessagePK(getComponentId(), String.valueOf(messageId));
  }

  public List<Forum> getForumAncestors(int forumId) {
    int currentForumId = forumId;
    List<Forum> ancestors = new ArrayList<Forum>();
    String instanceId = getForumsService().getForumInstanceId(forumId);
    while (currentForumId > 0) {
      currentForumId = getForumsService().getForumParentId(currentForumId);
      ancestors.add(
          getForumsService().getForum(new ForumPK(instanceId, String.valueOf(currentForumId))));

    }
    Collections.reverse(ancestors);
    return ancestors;
  }

  /**
   * this method clasify content only when new forum is created. Check if a position has been
   * defined in header formulary then persist it
   *
   * @param forumPK the current ForumDetail
   */
  private void classifyContent(ForumPK forumPK) {
    List<PdcPosition> positions = this.getPositions();
    if (positions != null && !positions.isEmpty()) {
      ForumDetail forumDetail = getForumsService().getForumDetail(forumPK);
      String forumId = forumDetail.getPK().getId();
      PdcClassification classification =
          aPdcClassificationOfContent(forumId, forumDetail.getInstanceId()).withPositions(
          this.getPositions());
      if (!classification.isEmpty()) {
        PdcClassificationService service =
            PdcServiceFactory.getFactory().getPdcClassificationService();
        classification.ofContent(forumId);
        service.classifyContent(forumDetail, classification);
      }
    }
  }

  public void setForumPositions(String positions) {
    if (StringUtil.isDefined(positions)) {
      PdcClassificationEntity surveyClassification = null;
      try {
        surveyClassification = PdcClassificationEntity.fromJSON(positions);
      } catch (JAXBException e) {
        SilverTrace.error("Forum", "ForumActionHelper.actionManagement",
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
  private List<PdcPosition> getPositions() {
    return positions;
  }

  /**
   * @param positions the positions to set
   */
  private void setPositions(List<PdcPosition> positions) {
    this.positions = positions;
  }

  public boolean isComponentSubscriptionInfoDisplayed() {
    return componentSubscriptionInfoDisplayed;
  }

  public void setComponentSubscriptionInfoDisplayed(
      final boolean componentSubscriptionInfoDisplayed) {
    this.componentSubscriptionInfoDisplayed = componentSubscriptionInfoDisplayed;
  }
}
