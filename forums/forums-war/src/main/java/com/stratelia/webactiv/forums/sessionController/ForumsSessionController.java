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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.forums.sessionController;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.RemoveException;

import com.silverpeas.notation.ejb.NotationBm;
import com.silverpeas.notation.ejb.NotationBmHome;
import com.silverpeas.notation.ejb.NotationRuntimeException;
import com.silverpeas.notation.model.Notation;
import com.silverpeas.notation.model.NotationDetail;
import com.silverpeas.notation.model.NotationPK;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.CollectionUtil;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.forums.forumEntity.ejb.ForumPK;
import com.stratelia.webactiv.forums.forumsException.ForumsException;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBM;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBMHome;
import com.stratelia.webactiv.forums.messageEntity.ejb.MessagePK;
import com.stratelia.webactiv.forums.models.Category;
import com.stratelia.webactiv.forums.models.Forum;
import com.stratelia.webactiv.forums.models.Message;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.statistic.control.StatisticBm;
import com.stratelia.webactiv.util.statistic.control.StatisticBmHome;
import com.stratelia.webactiv.util.statistic.model.StatisticRuntimeException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Cette classe gere la session de l'acteur durant sa navigation dans les forums
 * @author frageade
 * @since September 2000
 */
public class ForumsSessionController extends AbstractComponentSessionController {

  public static final String MAIL_TYPE = "default";
  public static final String STAT_TYPE = "ForumMessage";
  /** Le Business Manager */
  private ForumsBM forumsBM;
  /** ids des forums deployes */
  private List<Integer> deployedForums;
  /** ids des messages deployes */
  private List<Integer> deployedMessages;
  /** utilise pour notifier les utilisateurs */
  private NotificationSender notifSender = null;
  public String typeMessages = "Messages";
  public String typeSubjects = "Subjects";
  private ResourceLocator settings = null;
  private PublicationBm publicationBm = null;
  private StatisticBm statisticBm = null;
  private NotationBm notationBm = null;
  private boolean displayAllMessages = false;
  private boolean external = false;
  private String mailType = MAIL_TYPE;
  private boolean resizeFrame = false;
  public static final String STATUS_VALIDATE = "V";
  public static final String STATUS_FOR_VALIDATION = "A";
  public static final String STATUS_REFUSED = "R";

  // Constructeur
  public ForumsSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context,
        "com.stratelia.webactiv.forums.multilang.forumsBundle",
        "com.stratelia.webactiv.forums.settings.forumsIcons");

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
    try {
      @SuppressWarnings("unchecked")
      List<Forum> forums = getForumsBM().getForums(new ForumPK(getComponentId(), getSpaceId()));
      return forums.toArray(new Forum[forums.size()]);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public Forum[] getForumsListByCategory(String categoryId) {
    SilverTrace.debug("forums", "ForumsSessionController.getForumsListByCategory()", "",
        "categoryId = " + categoryId);
    Forum[] result = new Forum[0];
    try {
      ForumPK forumPK = new ForumPK(getComponentId(), getSpaceId());
      @SuppressWarnings("unchecked")
      List<Forum> forums = getForumsBM().getForumsByCategory(forumPK, categoryId);
      result = forums.toArray(new Forum[forums.size()]);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
    SilverTrace.debug("forums", "ForumsSessionController.getForumsListByCategory()", "", "retour = "
        + result);
    return result;
  }

  public Forum getForum(int forumId) {
    try {
      return getForumsBM().getForum(getForumPK(forumId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public String getForumName(int forumId) {
    try {
      return getForumsBM().getForumName(forumId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public boolean isForumActive(int forumId) {
    try {
      return getForumsBM().isForumActive(forumId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public int getForumParentId(int forumId) {
    try {
      return getForumsBM().getForumParentId(forumId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public int[] getForumSonsIds(int forumId) {
    int[] sonsIds = new int[0];
    try {
      List<String> ids = getForumsBM().getForumSonsIds(getForumPK(forumId));
      int n = ids.size();
      sonsIds = new int[n];
      for (int i = 0; i < n; i++) {
        sonsIds[i] = Integer.parseInt((String) ids.get(i));
      }
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
    return sonsIds;
  }

  public int getForumSonsNb(int forumId) {
    try {
      return getForumsBM().getForumSonsIds(getForumPK(forumId)).size();
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void deployForum(int id) {
//    deployedForums.add(Integer.valueOf(id));
    deployedForums.add(id);
  }

  public void deployAllMessages(int forumId) {
    Message[] messages = getMessagesList(forumId);
    for (Message message : messages) {
      deployMessage(message.getId());
    }
  }

  public void undeployForum(int id) {
//    if (!deployedForums.isEmpty()) {
//      Iterator<Integer> iter = deployedForums.iterator();
//      while (iter.hasNext()) {
//        Integer value = iter.next();
//        if (value.intValue() == id) {
//          iter.remove();
//          return;
//        }
//      }
//    }
    deployedForums.remove(id);
  }

  public boolean forumIsDeployed(int id) {
//    if (!deployedForums.isEmpty()) {
//      for (Integer forum : deployedForums) {
//        if (forum.intValue() == id) {
//          return true;
//        }
//      }
//    }
//    return false;
    return deployedForums.contains(id);
  }

  public void lockForum(int id, int level) {
    ForumPK forumPK = new ForumPK(getComponentId(), getSpaceId(), String.valueOf(id));
    try {
      getForumsBM().lockForum(forumPK, level);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public int unlockForum(int id, int level) {
    ForumPK forumPK = new ForumPK(getComponentId(), getSpaceId(), String.valueOf(id));
    try {
      return getForumsBM().unlockForum(forumPK, level);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  /**
   * Cree un nouveau forum dans la datasource
   * @param String nom du forum
   * @param String description du forum
   * @param String l'id du createur du forum
   * @param int l'id du forum parent
   * @return l'id du forum nouvellement cree
   * @author frageade
   * @since 02 Octobre 2000
   */
  public int createForum(String forumName, String forumDescription,
      String forumCreator, int forumParent, String keywords) {
    return createForum(forumName, forumDescription, forumCreator, forumParent,
        "0", keywords);
  }

  public int createForum(String forumName, String forumDescription,
      String forumCreator, int forumParent, String categoryId, String keywords) {
    ForumPK forumPK = new ForumPK(getComponentId(), getSpaceId());
    String currentCategoryId = categoryId;
    try {
      if (!StringUtil.isDefined(categoryId)) {
        currentCategoryId = null;
      }
      return getForumsBM().createForum(forumPK, truncateTextField(forumName), truncateTextArea(
          forumDescription), forumCreator, forumParent, currentCategoryId, keywords);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  /**
   * Met a jour les informations sur un forum dans la datasource
   * @param int l'ID du forum dans la datasource
   * @param String nom du forum
   * @param String description du forum
   * @param int l'id du forum parent
   * @author frageade
   * @since 03 Octobre 2000
   */
  public void updateForum(int forumId, String forumName,
      String forumDescription, int forumParent, String keywords) {
    updateForum(forumId, forumName, forumDescription, forumParent, null,
        keywords);
  }

  public void updateForum(int forumId, String forumName,
      String forumDescription, int forumParent, String categoryId,
      String keywords) {
    try {
      getForumsBM().updateForum(getForumPK(forumId), truncateTextField(forumName),
          truncateTextArea(forumDescription), forumParent, categoryId,
          keywords);
    } catch (RemoteException re) {
      SilverTrace.error("forums", "ForumsSessionController.updateForum()",
          "forums.EXE_UPDATE_FORUM_FAILED", re.getMessage());
    }
  }

  /**
   * Supprime un forum et tous ses sous-forums a partir de son ID
   * @param int l'ID du forum dans la datasource
   * @author frageade
   * @since 3 Octobre 2000
   */
  public void deleteForum(int forumId) {
    try {
      getForumsBM().deleteForum(getForumPK(forumId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  /**
   * Indexe un forum a partir de son ID
   * @param int l'ID du forum dans la datasource
   * @author frageade
   * @since 23 Aout 2001
   */
  public void indexForum(int forumId) {
    try {
      getForumsBM().createIndex(getForumPK(forumId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  // Methodes messages
  /**
   * Liste les messages d'un forum
   * @param String id du forum
   * @return Vector la liste des messages
   * @author frageade
   * @since 04 Octobre 2000
   */
  @SuppressWarnings("unchecked")
  public Message[] getMessagesList(int forumId) {
    try {
      Collection<Message> messages =
          (Collection<Message>) getForumsBM().getMessages(getForumPK(forumId));
      return messages.toArray(new Message[messages.size()]);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
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
   * @param String id du forum
   * @return String les champs du dernier message
   * @author sfariello
   * @since
   */
  public Object[] getLastMessage(int forumId) {
    return getLastMessage(forumId, -1);
  }

  public Object[] getLastMessage(int forumId, int messageId) {
    try {
      Message message = (messageId != -1 ? getForumsBM().getLastMessage(
          getForumPK(forumId), messageId, STATUS_VALIDATE) : getForumsBM().getLastMessage(getForumPK(
          forumId), STATUS_VALIDATE));
      if (message != null) {
        UserDetail user = getUserDetail(message.getAuthor());
        SilverTrace.debug("forums", "ForumsSessioncontroller.getLastMessage()",
            "root.MSG_GEN_ENTER_METHOD", "message = " + message.toString());
        return new Object[]{String.valueOf(message.getId()),
              message.getDate(),
              (user != null ? user.getDisplayedName() : "Unknown")};
      }
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
    return null;
  }

  /**
   * Nombre de sujets d'un forum
   * @param String id du forum
   * @return int le nombre de sujets
   * @author sfariello
   * @since 07 Décembre 2007
   */
  public int getNbSubjects(int forumId) {
    try {
      return getForumsBM().getNbMessages(forumId, typeSubjects, STATUS_VALIDATE);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  /**
   * Nombre de messages d'un forum
   * @param String id du forum
   * @return int le nombre de messages
   * @author sfariello
   * @since 07 Décembre 2007
   */
  public int getNbMessages(int forumId) {
    try {
      return getForumsBM().getNbMessages(forumId, typeMessages, STATUS_VALIDATE);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public int getAuthorNbMessages(String userId) {
    try {
      return getForumsBM().getAuthorNbMessages(userId, STATUS_VALIDATE);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public int getNbResponses(int forumId, int messageId) {
    try {
      return getForumsBM().getNbResponses(forumId, messageId, STATUS_VALIDATE);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  /**
   * Recupere les infos d'un message
   * @param String id du message
   * @return Vector la liste des champs du message
   * @author frageade
   * @since 04 Octobre 2000
   */
  public Message getMessage(int messageId) {
    try {
      return getForumsBM().getMessage(getMessagePK(messageId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public String getMessageTitle(int messageId) {
    try {
      return getForumsBM().getMessageTitle(messageId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public int getMessageParentId(int messageId) {
    try {
      return getForumsBM().getMessageParentId(messageId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  /**
   * Cree un nouveau message dans la datasource
   * @param String titre du message
   * @param String id de l'auteur du message
   * @param Strinf id du forum
   * @param String id du message parent
   * @param String texte du message
   * @return String l'id du message créé
   * @author frageade
   * @throws ForumsException
   * @since 04 Octobre 2000
   */
  public int createMessage(String title, String author, int forumId,
      int parentId, String text, String keywords) {
    String status = STATUS_FOR_VALIDATION;

    MessagePK messagePK = new MessagePK(getComponentId(), getSpaceId());
    int messageId = 0;

    try {
      if (!isValidationActive()
          || (getNbModerator(forumId) == 0 || isModerator(getUserId(), forumId)
          || "admin".equals(getUserRoleLevel()))) {
        status = STATUS_VALIDATE;
      }
      // creation du message dans la base
      messageId = getForumsBM().createMessage(messagePK, truncateTextField(title),
          author, null, forumId, parentId, text, keywords, status);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    } catch (ForumsException e) {
      throw new EJBException(e.getMessage());
    }

    // Send notification to subscribers
    try {
      // seulement si le message est valide
      if (status.equals(STATUS_VALIDATE) && parentId != 0) {
        sendNotification(title, text, parentId, messageId);
      }
      // envoie notification si demande de validation
      if (status.equals(STATUS_FOR_VALIDATION)) {
        sendNotificationToValidate(title, text, parentId, messageId, forumId);
      }
    } catch (Exception e) {
      SilverTrace.warn("forums", "ForumsSessionController.createMessage()",
          "forums.MSG_NOTIFY_USERS_FAILED", null, e);
    }
    return messageId;
  }

  public void updateMessage(int messageId, int parentId, String title,
      String text) {
    updateMessage(messageId, parentId, title, text, null);
  }

  public void updateMessage(int messageId, int parentId, String title,
      String text, String status) {
    MessagePK messagePK = getMessagePK(messageId);
    Message message = getMessage(messageId);
    String currentStatus = status;
    try {
      if (currentStatus == null) {
        currentStatus = STATUS_FOR_VALIDATION;
        if (!isValidationActive() || (getNbModerator(message.getForumId()) == 0
            || isModerator(getUserId(), message.getForumId()) || "admin".equals(getUserRoleLevel()))) {
          currentStatus = STATUS_VALIDATE;
        }
      }
      getForumsBM().updateMessage(messagePK, truncateTextField(title), text,
          getUserId(), currentStatus);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    } catch (ForumsException e) {
      throw new EJBException(e.getMessage());
    }

    // Send notification to subscribers
    try {
      if (parentId != 0) {
        sendNotification(title, text, parentId, messageId);
      }
      // envoie notification si demande de validation
      if (!status.equals(STATUS_VALIDATE)) {
        sendNotificationToValidate(title, text, parentId, messageId, message.getForumId());
      }
    } catch (Exception e) {
      SilverTrace.warn("forums", "ForumsSessionController.createMessage()",
          "forums.MSG_NOTIFY_USERS_FAILED", null, e);
    }
  }

  public void updateMessageKeywords(int messageId, String keywords) {
    try {
      getForumsBM().updateMessageKeywords(getMessagePK(messageId), keywords);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void setMailType(String mailType) {
    this.mailType = mailType;
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

  public void sendNotification(String title, String text, int parentId,
      int messageId) throws NotificationManagerException {
    List<String> subscribers = listAllSubscribers(parentId);
    if (subscribers.size() > 0) {
      ResourceLocator resource = new ResourceLocator(
          "com.stratelia.webactiv.forums.settings.forumsMails", getLanguage());
      // Preparation des donnees
      String[] targetUserIds = (String[]) subscribers.toArray(new String[0]);

      Map<String, String> values = new HashMap<String, String>();
      values.put("title", title);
      values.put("text", text);
      values.put("originTitle", getMessageTitle(parentId));
      values.put("componentId", getComponentId());
      values.put("messageId", String.valueOf(messageId));

      String mailSubject = StringUtil.format(resource.getString(mailType + ".subject"), values);
      String mailBody = StringUtil.format(resource.getString(mailType + ".body"), values);
      String url = StringUtil.format(resource.getString(mailType + ".link"),
          values);

      // envoi des mails de notification
      NotificationMetaData notifMetaData = new NotificationMetaData(
          NotificationParameters.NORMAL, mailSubject, mailBody);
      notifMetaData.setSender(getUserId());
      notifMetaData.addUserRecipients(targetUserIds);
      notifMetaData.setSource(getSpaceLabel() + " - " + getComponentLabel());
      notifMetaData.setLink(url);

      getNotificationSender().notifyUser(notifMetaData);
    }
  }

  public void sendNotificationToValidate(String title, String text, int parentId,
      int messageId, int forumId) throws NotificationManagerException {
    List<String> moderators = getModerators(forumId);
    if (moderators.size() > 0) {
      ResourceLocator resource = new ResourceLocator(
          "com.stratelia.webactiv.forums.settings.forumsMails", getLanguage());
      // Preparation des donnees
      String[] targetUserIds = moderators.toArray(new String[moderators.size()]);

      Map<String, String> values = new HashMap<String, String>();
      values.put("title", title);
      values.put("text", text);
      values.put("originTitle", getMessageTitle(parentId));
      values.put("componentId", getComponentId());
      values.put("messageId", String.valueOf(messageId));

      String mailSubject = StringUtil.format(resource.getString(mailType + ".subjectToValidate"),
          values);
      String mailBody = StringUtil.format(resource.getString(mailType + ".bodyToValidate"), values);
      String url = StringUtil.format(resource.getString(mailType + ".link"), values);

      // envoi des mails de notification
      NotificationMetaData notifMetaData = new NotificationMetaData(
          NotificationParameters.NORMAL, mailSubject, mailBody);
      notifMetaData.setSender(getUserId());
      notifMetaData.addUserRecipients(targetUserIds);
      notifMetaData.setSource(getSpaceLabel() + " - " + getComponentLabel());
      notifMetaData.setLink(url);

      getNotificationSender().notifyUser(notifMetaData);
    }
  }

  public void sendNotificationAfterValidation(String title, String text, int parentId,
      int messageId, int forumId) throws NotificationManagerException {
    ResourceLocator resource = new ResourceLocator(
        "com.stratelia.webactiv.forums.settings.forumsMails", getLanguage());
    // Preparation des donnees
    Message message = getMessage(messageId);
    String[] targetUserIds = new String[1];
    targetUserIds[0] = message.getAuthor();

    Map<String, String> values = new HashMap<String, String>();
    values.put("title", title);
    values.put("text", text);
    values.put("originTitle", getMessageTitle(parentId));
    values.put("componentId", getComponentId());
    values.put("messageId", String.valueOf(messageId));

    String mailSubject = StringUtil.format(resource.getString(mailType + ".subjectValidation"),
        values);
    String mailBody = StringUtil.format(resource.getString(mailType + ".bodyValidation"), values);
    String url = StringUtil.format(resource.getString(mailType + ".link"), values);

    // envoi des mails de notification
    NotificationMetaData notifMetaData = new NotificationMetaData(
        NotificationParameters.NORMAL, mailSubject, mailBody);
    notifMetaData.setSender(getUserId());
    notifMetaData.addUserRecipients(targetUserIds);
    notifMetaData.setSource(getSpaceLabel() + " - " + getComponentLabel());
    notifMetaData.setLink(url);

    getNotificationSender().notifyUser(notifMetaData);
  }

  public void sendNotificationRefused(String title, String text, int parentId,
      int messageId, int forumId, String motive) throws NotificationManagerException {
    ResourceLocator resource = new ResourceLocator(
        "com.stratelia.webactiv.forums.settings.forumsMails", getLanguage());
    // Preparation des donnees
    Message message = getMessage(messageId);
    String[] targetUserIds = new String[1];
    targetUserIds[0] = message.getAuthor();

    Map<String, String> values = new HashMap<String, String>();
    values.put("title", title);
    values.put("text", text);
    values.put("originTitle", getMessageTitle(parentId));
    values.put("componentId", getComponentId());
    values.put("messageId", String.valueOf(messageId));
    values.put("motive", motive);

    String mailSubject = StringUtil.format(resource.getString(mailType + ".subjectRefused"), values);
    String mailBody = StringUtil.format(resource.getString(mailType + ".bodyRefused"), values);
    String url = StringUtil.format(resource.getString(mailType + ".link"), values);

    // envoi des mails de notification
    NotificationMetaData notifMetaData = new NotificationMetaData(
        NotificationParameters.NORMAL, mailSubject, mailBody);
    notifMetaData.setSender(getUserId());
    notifMetaData.addUserRecipients(targetUserIds);
    notifMetaData.setSource(getSpaceLabel() + " - " + getComponentLabel());
    notifMetaData.setLink(url);

    getNotificationSender().notifyUser(notifMetaData);
  }

  /**
   * Indexe un message a partir de son ID
   * @param String l'ID du message dans la datasource
   * @author frageade
   * @since 23 Aout 2001
   */
  public void indexMessage(int messageId) {
    try {
      getForumsBM().createIndex(getMessagePK(messageId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  /**
   * Supprime un message et tous ses sous-messages a partir de son ID
   * @param String l'ID du message dans la datasource
   * @author frageade
   * @since 04 Octobre 2000
   */
  public void deleteMessage(int messageId) {
    try {
      getForumsBM().deleteMessage(getMessagePK(messageId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void deployMessage(int id) {
    deployedMessages.add(Integer.valueOf(id));
  }

  public void undeployMessage(int id) {
    if (!deployedMessages.isEmpty()) {
      Iterator<Integer> iter = deployedMessages.iterator();
      while (iter.hasNext()) {
        Integer value = iter.next();
        if (value.intValue() == id) {
          iter.remove();
          return;
        }
      }
    }
  }

  public boolean messageIsDeployed(int id) {
    for (Integer value : deployedMessages) {
      if (value.intValue() == id) {
        return true;
      }
    }
    return false;
  }

  public boolean isReader() {
    if (!isAdmin() || isUser()) {
      String[] profiles = getUserRoles();
      for (String profile : profiles) {
        if (SilverpeasRole.reader == SilverpeasRole.valueOf(profile)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isUser() {
    String[] profiles = getUserRoles();
    for (String profile : profiles) {
      if (SilverpeasRole.user == SilverpeasRole.valueOf(profile)) {
        return true;
      }
    }
    return false;
  }

  public boolean isAdmin() {
    String[] profiles = getUserRoles();
    for (String profile : profiles) {
      if (SilverpeasRole.admin == SilverpeasRole.valueOf(profile)) {
        return true;
      }
    }
    return false;
  }

  public boolean isModerator(String userId, int forumId) throws ForumsException {
    boolean result = false;
    try {
      result = getForumsBM().isModerator(userId, getForumPK(forumId));
      int parentId = getForumParentId(forumId);
      while ((!result) && (parentId != 0)) {
        result = (result || getForumsBM().isModerator(userId, getForumPK(parentId)));
        parentId = getForumParentId(parentId);
      }
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
    return result;
  }

  public void addModerator(int forumId, String userId) {
    try {
      getForumsBM().addModerator(getForumPK(forumId), userId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void removeModerator(int forumId, String userId) {
    try {
      getForumsBM().removeModerator(getForumPK(forumId), userId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void removeAllModerators(int forumId) {
    try {
      getForumsBM().removeAllModerators(getForumPK(forumId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public List<String> getModerators(int forumId) {
    try {
      return getForumsBM().getModerators(forumId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public int getNbModerator(int forumId) {
    return getModerators(forumId).size();
  }

  public void moveMessage(int messageId, int forumId) {
    try {
      getForumsBM().moveMessage(getMessagePK(messageId), getForumPK(forumId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void subscribeMessage(int messageId, String userId) {
    try {
      getForumsBM().subscribeMessage(getMessagePK(messageId), userId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void unsubscribeMessage(int messageId, String userId) {
    try {
      getForumsBM().unsubscribeMessage(getMessagePK(messageId), userId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void removeAllSubscribers(int messageId) {
    try {
      getForumsBM().removeAllSubscribers(getMessagePK(messageId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public boolean isSubscriber(int messageId, String userId) {
    try {
      return getForumsBM().isSubscriber(getMessagePK(messageId), userId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public List<String> listAllSubscribers(int messageId) {
    List<String> subscribers = new ArrayList<String>();
    try {
      @SuppressWarnings("unchecked")
      List<String> forumSubscribers = getForumsBM().listAllSubscribers(getMessagePK(messageId));
      subscribers.addAll(forumSubscribers);
      int parentId = getMessageParentId(messageId);
      while (parentId != 0) {
        @SuppressWarnings("unchecked")
        List<String> parentForumSubscribers = getForumsBM().listAllSubscribers(
            getMessagePK(parentId));
        subscribers.addAll(forumSubscribers);
        parentId = getMessageParentId(parentId);
      }
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
    return subscribers;
  }

  public boolean isNewMessageByForum(String userId, int forumId) {
    boolean isNewMessage = false;
    try {
      isNewMessage =
          getForumsBM().isNewMessageByForum(userId, getForumPK(forumId), STATUS_VALIDATE);
      SilverTrace.info("forums",
          "ForumsSessionController.isNewMessageByForum()",
          "root.MSG_GEN_PARAM_VALUE", "isNewMessageByForum = " + isNewMessage);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
    return isNewMessage;
  }

  public boolean isNewMessage(String userId, int forumId, int messageId) {
    boolean isNewMessage = false;
    try {
      isNewMessage = getForumsBM().isNewMessage(userId, getForumPK(forumId),
          messageId, STATUS_VALIDATE);
      SilverTrace.info("forums", "ForumsSessionController.isNewMessage()",
          "root.MSG_GEN_PARAM_VALUE", "isNewMessage = " + isNewMessage);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
    return isNewMessage;
  }

  public void setLastVisit(String userId, int messageId) {
    try {
      getForumsBM().setLastVisit(userId, messageId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public UserDetail[] listUsers() {
    UserDetail[] userDetails = CollectionUtil.sortUserDetailArray(getOrganizationController().
        getAllUsers(
        getComponentId()));
    return (userDetails != null ? userDetails : new UserDetail[0]);
  }

  public String getAuthorName(String userId) {
    UserDetail userDetail = getOrganizationController().getUserDetail(userId);
    return (userDetail != null ? (userDetail.getFirstName() + " " + userDetail.getLastName()).trim() : null);
  }

  public String getAdminIds() {
    return NotificationSender.getIdsLineFromUserArray(getOrganizationController().getUsers(
        getSpaceId(), getComponentId(), "admin"));
  }

  private String truncateTextField(String s) {
    return (s.length() >= DBUtil.TextFieldLength ? s.substring(0,
        DBUtil.TextFieldLength - 1) : s);
  }

  private String truncateTextArea(String s) {
    return (s.length() >= DBUtil.TextAreaLength ? s.substring(0,
        DBUtil.TextAreaLength - 1) : s);
  }

  public boolean isPdcUsed() {
    String value = getComponentParameterValue("usePdc");
    return (value != null && "yes".equals(value.toLowerCase()));
  }

  public boolean isUseRss() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("rss"));
  }

  public boolean forumInsideForum() {
    String value = getComponentParameterValue("forumInsideForum");
    return (value != null && "yes".equals(value.toLowerCase()));
  }

  public int getSilverObjectId(int objectId) {
    try {
      return getForumsBM().getSilverObjectId(getForumPK(objectId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  @Override
  public void close() {
    try {
      if (getForumsBM() != null) {
        getForumsBM().remove();
      }
    } catch (RemoteException e) {
      SilverTrace.error("forums", "ForumsSessionController.close", "", e);
    } catch (RemoveException e) {
      SilverTrace.error("forums", "ForumsSessionController.close", "", e);
    }
  }

  public Collection getAllCategories() {
    try {
      return getForumsBM().getAllCategories(getComponentId());
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public synchronized void createCategory(Category category) {
    try {
      category.setCreationDate(DateUtil.date2SQLDate(new Date()));
      category.setCreatorId(getUserId());
      category.getNodePK().setComponentName(getComponentId());
      getForumsBM().createCategory(category);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public Category getCategory(String categoryId) {
    try {
      // rechercher la catégorie
      NodePK nodePK = new NodePK(categoryId, getComponentId());
      return getForumsBM().getCategory(nodePK);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public synchronized void updateCategory(Category category) {
    try {
      SilverTrace.error("forums", "ForumsSessionController.updateCategory", "",
          "category = " + category.getName());
      getForumsBM().updateCategory(category);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public synchronized void deleteCategory(String categoryId) {
    try {
      SilverTrace.error("forums", "ForumsSessionController.deleteCategory", "",
          "categoryId = " + categoryId);
      getForumsBM().deleteCategory(categoryId, getComponentId());
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  @Override
  public ResourceLocator getSettings() {
    if (settings == null) {
      settings = new ResourceLocator(
          "com.stratelia.webactiv.forums.settings.forumsSettings", "");
    }
    return settings;
  }

  public PublicationDetail getDetail(String id) throws RemoteException {
    return getPublicationBm().getDetail(
        new PublicationPK(id, getSpaceId(), getComponentId()));
  }

  public void addMessageStat(int messageId, String userId)
      throws RemoteException {
    getStatisticBm().addStat(userId,
        new ForeignPK(String.valueOf(messageId), getComponentId()), 1,
        STAT_TYPE);
  }

  public int getMessageStat(int messageId) throws RemoteException {
    return getStatisticBm().getCount(
        new ForeignPK(String.valueOf(messageId), getComponentId()), STAT_TYPE);
  }

  public boolean isDisplayAllMessages() {
    return displayAllMessages;
  }

  public void changeDisplayAllMessages() {
    displayAllMessages = !displayAllMessages;
  }

  public void resetDisplayAllMessages() {
    displayAllMessages = false;
  }

  public String getForumKeywords(int forumId) {
    try {
      return getForumsBM().getForumTags(getForumPK(forumId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public String getMessageKeywords(int messageId) {
    try {
      return getForumsBM().getMessageTags(getMessagePK(messageId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public NotationDetail getForumNotation(int forumId) {
    try {
      NotationDetail notation = getNotationBm().getNotation(getForumNotationPk(forumId));
      return notation;
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public NotationDetail getMessageNotation(int messageId) {
    try {
      return getNotationBm().getNotation(getMessageNotationPk(messageId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void updateForumNotation(int forumId, int note) {
    try {
      getNotationBm().updateNotation(getForumNotationPk(forumId), note);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void updateMessageNotation(int messageId, int note) {
    try {
      getNotationBm().updateNotation(getMessageNotationPk(messageId), note);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void validateMessage(int messageId) {
    Message message = getMessage(messageId);
    updateMessage(messageId, message.getParentId(), message.getTitle(), message.getText(),
        STATUS_VALIDATE);
    try {
      // envoie d'une notification au créateur du message
      sendNotificationAfterValidation(message.getTitle(), message.getText(), message.getParentId(),
          messageId, message.getForumId());
      // envoie une notification aux abonnés
      if (message.getStatus().equals(STATUS_VALIDATE) && message.getParentId() != 0) {
        sendNotification(message.getTitle(), message.getText(), message.getParentId(), messageId);
      }
    } catch (NotificationManagerException e) {
      throw new EJBException(e.getMessage());
    }
  }

  public void refuseMessage(int messageId, String motive) {
    Message message = getMessage(messageId);
    updateMessage(messageId, message.getParentId(), message.getTitle(), message.getText(),
        STATUS_REFUSED);
    try {
      sendNotificationRefused(message.getTitle(), message.getText(), message.getParentId(),
          messageId, message.getForumId(), motive);
    } catch (NotificationManagerException e) {
      throw new EJBException(e.getMessage());
    }
  }

  public boolean isValidationActive() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("isValidationActive"));
  }

  private PublicationBm getPublicationBm() {
    if (publicationBm == null) {
      try {
        publicationBm = ((PublicationBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class)).create();
      } catch (Exception e) {
        SilverTrace.error("quickinfo",
            "QuickInfoSessionController.getPublicationBm()",
            "root.MSG_EJB_CREATE_FAILED", JNDINames.PUBLICATIONBM_EJBHOME, e);
        throw new EJBException(e);
      }
    }
    return publicationBm;
  }

  protected StatisticBm getStatisticBm() {
    if (statisticBm == null) {
      try {
        StatisticBmHome statisticHome = (StatisticBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.STATISTICBM_EJBHOME,
            StatisticBmHome.class);
        statisticBm = statisticHome.create();
      } catch (Exception e) {
        throw new StatisticRuntimeException(
            "KmeliaSessionController.getStatisticBm()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }

    return statisticBm;
  }

  protected NotationBm getNotationBm() {
    if (notationBm == null) {
      try {
        NotationBmHome notationHome = (NotationBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.NOTATIONBM_EJBHOME, NotationBmHome.class);
        notationBm = notationHome.create();
      } catch (Exception e) {
        throw new NotationRuntimeException(
            "KmeliaSessionController.getNotationBm()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return notationBm;
  }

  protected ForumsBM getForumsBM() {
    if (forumsBM == null) {
      try {
        ForumsBMHome forumsBMHome = (ForumsBMHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.FORUMSBM_EJBHOME, ForumsBMHome.class);
        forumsBM = forumsBMHome.create();
      } catch (Exception e) {
        throw new EJBException(e.getMessage());
      }
    }
    return forumsBM;
  }

  protected void setForumsBM(ForumsBM forumsBM) {
    this.forumsBM = forumsBM;
  }

  private ForumPK getForumPK(int forumId) {
    return new ForumPK(getComponentId(), getSpaceId(), String.valueOf(forumId));
  }

  private MessagePK getMessagePK(int messageId) {
    return new MessagePK(getComponentId(), getSpaceId(), String.valueOf(messageId));
  }

  private NotationPK getForumNotationPk(int forumId) {
    return new NotationPK(String.valueOf(forumId), getComponentId(),
        Notation.TYPE_FORUM, getUserId());
  }

  private NotationPK getMessageNotationPk(int messageId) {
    return new NotationPK(String.valueOf(messageId), getComponentId(),
        Notation.TYPE_MESSAGE, getUserId());
  }

  public List<Forum> getForumAncestors(int forumId) {
    int currentForumId = forumId;
    List<Forum> ancestors = new ArrayList<Forum>();
    try {
      String instanceId = getForumsBM().getForumInstanceId(forumId);
      while (currentForumId > 0) {
        currentForumId = getForumsBM().getForumParentId(currentForumId);
        ancestors.add(
            getForumsBM().getForum(new ForumPK(instanceId, null, String.valueOf(currentForumId))));

      }
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
    Collections.reverse(ancestors);
    return ancestors;
  }
}
