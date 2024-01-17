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
package org.silverpeas.components.forums.control;

import org.silverpeas.components.forums.bean.ForumModeratorBean;
import org.silverpeas.components.forums.model.Forum;
import org.silverpeas.components.forums.model.ForumDetail;
import org.silverpeas.components.forums.model.ForumPK;
import org.silverpeas.components.forums.model.Message;
import org.silverpeas.components.forums.model.MessagePK;
import org.silverpeas.components.forums.notification.ForumsForumSubscriptionUserNotification;
import org.silverpeas.components.forums.notification.ForumsMessagePendingValidationUserNotification;
import org.silverpeas.components.forums.notification.ForumsMessageSubscriptionUserNotification;
import org.silverpeas.components.forums.notification.ForumsMessageValidationUserNotification;
import org.silverpeas.components.forums.service.ForumService;
import org.silverpeas.components.forums.service.ForumsException;
import org.silverpeas.components.forums.service.ForumsServiceProvider;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.model.Attachments;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.exception.DecodingException;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.subscription.SubscriptionServiceProvider;
import org.silverpeas.core.subscription.service.ComponentSubscription;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.error.SilverpeasTransverseErrorUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.webapi.pdc.PdcClassificationEntity;

import javax.ejb.EJBException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.admin.user.model.SilverpeasRole.*;
import static org.silverpeas.core.admin.user.model.UserDetail.OnFirstNameAndLastName;
import static org.silverpeas.core.pdc.pdc.model.PdcClassification.aPdcClassificationOfContent;

/**
 * This class manage user session when working with forums application
 * @author frageade
 */
public class ForumsSessionController extends AbstractComponentSessionController {

  public static final String STAT_TYPE = "ForumMessage";
  /**
   * ids of deployed forums
   */
  private List<Integer> deployedForums;
  /**
   * ids of deployed messages
   */
  private List<Integer> deployedMessages;
  /**
   * Used to send notification
   */
  private NotificationSender notifSender = null;
  private static final String TYPE_MESSAGES = "Messages";
  private static final String TYPE_SUBJECTS = "Subjects";
  private SettingBundle settings = null;
  private boolean displayAllMessages = true;
  private boolean external = false;
  private boolean resizeFrame = false;
  private List<PdcPosition> positions = null;
  private boolean componentSubscriptionInfoDisplayed = false;

  // Constructor
  public ForumsSessionController(MainSessionController mainSessionCtrl, ComponentContext context) {
    super(mainSessionCtrl, context, "org.silverpeas.forums.multilang.forumsBundle",
        "org.silverpeas.forums.settings.forumsIcons");
    deployedMessages = new ArrayList<>();
    deployedForums = new ArrayList<>();
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
    ForumPK forumPK = new ForumPK(getComponentId(), getSpaceId());
    List<Forum> forums = getForumsService().getForumsByCategory(forumPK, categoryId);
    return forums.toArray(new Forum[forums.size()]);
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
   * @param forumName forum name
   * @param forumDescription forum description
   * @param forumCreator creator user identifier
   * @param forumParent parent forum identifier
   * @param keywords the keywords.
   * @return identifier of the new forum
   * @author frageade
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
    int forumId = getForumsService()
        .createForum(forumPK, truncateTextField(forumName), truncateTextArea(forumDescription),
            forumCreator, forumParent, currentCategoryId, keywords);

    // Send notification
    sendForumNotification(getForumsService().getForumDetail(getForumPK(forumId)), NotifAction.CREATE);

    // Classify content here
    classifyContent(forumPK);
    return forumId;
  }

  /**
   * Update Forum
   * @param forumId l'ID du forum dans la datasource
   * @param forumName forum name
   * @param forumDescription forum description
   * @param forumParent parent forum identifier
   * @param keywords the keywords.
   * @author frageade
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
   * @param forumId l'ID du forum dans la datasource
   * @author frageade
   */
  public void deleteForum(int forumId) {
    getForumsService().deleteForum(getForumPK(forumId));
  }

  /**
   * Liste les messages d'un forum
   * @param forumId id du forum
   * @return Vector la liste des messages
   * @author frageade
   */
  public Message[] getMessagesList(int forumId) {
    Collection<Message> messages = getForumsService().getMessages(getForumPK(forumId));
    return messages.toArray(new Message[messages.size()]);
  }

  public Message[] getMessagesList(int forumId, int messageId) throws ForumsException {
    List<Message> messageList = new ArrayList<>();
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

  protected boolean isVisible(String status, int forumId) {
    return isModerator(getUserId(), forumId) || Message.STATUS_VALIDATE.equals(status);
  }

  private void fillMessageList(List<Message> messageList, Message[] messages, int messageId)
      throws ForumsException {
    for (Message message : messages) {
      int forumId = message.getForumId();
      if (message.getParentId() == messageId && (isVisible(message.getStatus(), forumId) ||
          "admin".equals(getHighestSilverpeasUserRole().getName()) ||
          message.getAuthor().equals(getUserId()))) {
        messageList.add(message);
        fillMessageList(messageList, messages, message.getId());
      }
    }
  }

  /**
   * Récupère le dernier message d'un forum
   * @param forumId id du forum
   * @return String les champs du dernier message
   * @author sfariello
   */
  public Object[] getLastMessage(int forumId) {
    return getLastMessage(forumId, -1);
  }

  public Object[] getLastMessage(int forumId, int messageId) {
    Message message;
    if (messageId != -1) {
      message = getForumsService().getLastMessage(getForumPK(forumId), messageId, Message.STATUS_VALIDATE);
    } else {
      message = getForumsService().getLastMessage(getForumPK(forumId), Message.STATUS_VALIDATE);
    }
    if (message != null) {
      UserDetail user = getUserDetail(message.getAuthor());
      return new Object[]{String.valueOf(message.getId()), message.getDate(),
          (user != null ? user.getDisplayedName() : "Unknown")};
    }
    return new Object[0];
  }

  /**
   * @param forumId forum identifier
   * @return number of forum subjects
   * @author sfariello
   */
  public int getNbSubjects(int forumId) {
    return getForumsService().getNbMessages(forumId, TYPE_SUBJECTS, Message.STATUS_VALIDATE);
  }

  /**
   * @param forumId forum identifier
   * @return number of forum messages
   * @author sfariello
   */
  public int getNbMessages(int forumId) {
    return getForumsService().getNbMessages(forumId, TYPE_MESSAGES, Message.STATUS_VALIDATE);
  }

  public int getAuthorNbMessages(String userId) {
    return getForumsService().getAuthorNbMessages(userId, Message.STATUS_VALIDATE);
  }

  public int getNbResponses(int forumId, int messageId) {
    return getForumsService().getNbResponses(forumId, messageId, Message.STATUS_VALIDATE);
  }

  /**
   * Retrieve a message bean by identifier given in parameter
   * @param messageId message identifier
   * @return a Message Bean
   * @author frageade
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
   * Create a new message
   * @param title message title
   * @param author author identifier
   * @param forumId forum identifier
   * @param parentId parent message identifier
   * @param text message content
   * @param keywords the keywords
   * @param uploadedFiles the files to attach to the message
   * @return new message identifier
   * @author frageade
   */
  public int createMessage(String title, String author, int forumId, int parentId, String text,
      String keywords, Collection<UploadedFile> uploadedFiles) {
    String status = Message.STATUS_FOR_VALIDATION;

    MessagePK messagePK = new MessagePK(getComponentId(), getSpaceId());
    int messageId = 0;

    try {
      if (!isValidationActive() || ADMIN.isInRole(getHighestSilverpeasUserRole().getName()) ||
          isModerator(getUserId(), forumId)) {
        status = Message.STATUS_VALIDATE;
      }
      // creation du message dans la base
      messageId = getForumsService()
          .createMessage(messagePK, truncateTextField(title), author, null, forumId, parentId, text,
              keywords, status);
    } catch (Exception e) {
      SilverpeasTransverseErrorUtil.stopTransverseErrorIfAny(new EJBException(e.getMessage(), e));
      return messageId;
    }
    messagePK.setId(String.valueOf(messageId));

    // Send notification to subscribers
    Message message = getMessage(messageId);
    if (Message.STATUS_VALIDATE.equals(status)) {
      sendMessageNotification(message, NotifAction.CREATE);
    } else {
      // Send notification only if message to validate
      sendMessageNotificationToValidate(message);
    }

    // Attach uploaded files
    try {
      Attachments.from(uploadedFiles).attachTo(message);
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
        currentStatus = Message.STATUS_FOR_VALIDATION;
        if (!isValidationActive() || ADMIN.isInRole(getHighestSilverpeasUserRole().getName()) ||
            isModerator(getUserId(), message.getForumId())) {
          currentStatus = Message.STATUS_VALIDATE;
        }
      }
      getForumsService()
          .updateMessage(messagePK, truncateTextField(title), text, getUserId(), currentStatus);
    } catch (Exception e) {
      SilverpeasTransverseErrorUtil.stopTransverseErrorIfAny(new EJBException(e.getMessage(), e));
      return;
    }

    // Send notification to subscribers
    if (Message.STATUS_VALIDATE.equals(currentStatus)) {
      sendMessageNotification(getMessage(messageId), NotifAction.UPDATE);
    } else if (Message.STATUS_FOR_VALIDATION.equals(currentStatus)) {
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
    UserNotificationHelper
        .buildAndSend(new ForumsMessageValidationUserNotification(message, getUserId(), motive));
  }

  /**
   * Supprime un message et tous ses sous-messages a partir de son ID
   * @param messageId l'ID du message dans la datasource
   * @author frageade
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
        if (READER.isInRole(profile)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isUser() {
    String[] profiles = getUserRoles();
    for (String profile : profiles) {
      if (USER.isInRole(profile)) {
        return true;
      }
    }
    return false;
  }

  public boolean isAdmin() {
    String[] profiles = getUserRoles();
    for (String profile : profiles) {
      if (ADMIN.isInRole(profile)) {
        return true;
      }
    }
    return false;
  }

  public boolean isModerator(String userId, int forumId) {
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
    return
        getForumsService().isNewMessageByForum(userId, getForumPK(forumId), Message.STATUS_VALIDATE);
  }

  public boolean isNewMessage(String userId, int forumId, int messageId) {
    return
        getForumsService().isNewMessage(userId, getForumPK(forumId), messageId, Message.STATUS_VALIDATE);
  }

  public void setLastVisit(String userId, int messageId) {
    getForumsService().setLastVisit(userId, messageId);
  }

  public List<UserDetail> listUsers() {
    List<UserDetail> userDetailList = new ArrayList<>();
    UserDetail[] userDetailArray = getOrganisationController().getAllUsers(getComponentId());
    if (userDetailArray != null) {
      Collections.addAll(userDetailList, userDetailArray);
      Collections.sort(userDetailList, new OnFirstNameAndLastName());
    }
    return userDetailList;
  }

  public String getAuthorName(String userId) {
    UserDetail userDetail = getOrganisationController().getUserDetail(userId);
    return (userDetail != null ?
        (userDetail.getFirstName() + " " + userDetail.getLastName()).trim() : null);
  }

  public UserDetail getAuthor(String userId) {
    return getOrganisationController().getUserDetail(userId);
  }

  public String getAdminIds() {
    User[] adminIds = getOrganisationController().getUsers(getSpaceId(), getComponentId(), "admin");
    return Stream.of(adminIds).map(User::getId).collect(Collectors.joining(","));
  }

  private String truncateTextField(String s) {
    return (s.length() >= DBUtil.getTextFieldLength() ?
        s.substring(0, DBUtil.getTextFieldLength() - 1) : s);
  }

  private String truncateTextArea(String s) {
    return (s.length() >= DBUtil.getTextAreaLength() ?
        s.substring(0, DBUtil.getTextAreaLength() - 1) : s);
  }

  public boolean isPdcUsed() {
    String value = getComponentParameterValue("usePdc");
    return (value != null && "yes".equalsIgnoreCase(value));
  }

  public boolean isUseRss() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("rss"));
  }

  public boolean isForumInsideForum() {
    String value = getComponentParameterValue("forumInsideForum");
    return (value != null && "yes".equalsIgnoreCase(value));
  }

  public int getSilverObjectId(int objectId) {
    return getForumsService().getSilverObjectId(getForumPK(objectId));
  }

  public Collection<NodeDetail> getAllCategories() {
    return getForumsService().getAllCategories(getComponentId());
  }

  public synchronized void createCategory(NodeDetail category) {
    category.setCreationDate(new Date());
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
    getForumsService().updateCategory(category);
  }

  public synchronized void deleteCategory(String categoryId) {
    getForumsService().deleteCategory(categoryId, getComponentId());
  }

  @Override
  public SettingBundle getSettings() {
    if (settings == null) {
      settings = ResourceLocator.getSettingBundle("org.silverpeas.forums.settings.forumsSettings");
    }
    return settings;
  }

  public PublicationDetail getDetail(String id) {
    return getPublicationService().getDetail(new PublicationPK(id, getSpaceId(), getComponentId()));
  }

  public void addMessageStat(int messageId, String userId) {
    getStatisticService()
        .addStat(userId, new ResourceReference(String.valueOf(messageId), getComponentId()), 1, STAT_TYPE);
  }

  public int getMessageStat(int messageId) {
    return getStatisticService()
        .getCount(new ResourceReference(String.valueOf(messageId), getComponentId()), STAT_TYPE);
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
    updateMessage(messageId, message.getTitle(), message.getText(), Message.STATUS_VALIDATE);

    // envoie d'une notification au créateur du message
    sendMessageNotificationAfterValidation(message);
    // envoie une notification aux abonnés si le message vient juste de passer à l'état validé
    if (!Message.STATUS_VALIDATE.equals(statusBeforeUpdate)) {
      sendMessageNotification(message, NotifAction.CREATE);
    }
  }

  public void refuseMessage(int messageId, String motive) {
    Message message = getMessage(messageId);
    updateMessage(messageId, message.getTitle(), message.getText(), Message.STATUS_REFUSED);

    sendMessageNotificationRefused(message, motive);
  }

  public boolean isValidationActive() {
    return StringUtil.getBooleanValue(getComponentParameterValue("isValidationActive"));
  }

  private PublicationService getPublicationService() {
    return PublicationService.get();
  }

  protected StatisticService getStatisticService() {
    return StatisticService.get();
  }

  protected ForumService getForumsService() {
    return ForumsServiceProvider.getForumsService();
  }

  private ForumPK getForumPK(int forumId) {
    return new ForumPK(getComponentId(), String.valueOf(forumId));
  }

  private MessagePK getMessagePK(int messageId) {
    return new MessagePK(getComponentId(), String.valueOf(messageId));
  }

  public List<Forum> getForumAncestors(int forumId) {
    int currentForumId = forumId;
    List<Forum> ancestors = new ArrayList<>();
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
   * @param forumPK the current ForumDetail
   */
  private void classifyContent(ForumPK forumPK) {
    List<PdcPosition> pdcPositions = this.getPositions();
    if (pdcPositions != null && !pdcPositions.isEmpty()) {
      ForumDetail forumDetail = getForumsService().getForumDetail(forumPK);
      PdcClassification classification =
          aPdcClassificationOfContent(forumDetail).withPositions(this.getPositions());
      classification.classifyContent(forumDetail);
    }
  }

  public void setForumPositions(String positions) {
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
