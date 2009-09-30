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
import java.util.Vector;

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

/**
 * Cette classe gere la session de l'acteur durant sa navigation dans les forums
 * 
 * @author frageade
 * @since September 2000
 */
public class ForumsSessionController extends AbstractComponentSessionController {

  private static final String MAIL_TYPE = "default";

  private static final String STAT_TYPE = "ForumMessage";

  /** Le Business Manager */
  private ForumsBM forumsBM;

  /** ids des forums deployes */
  private Vector deployedForums;

  /** ids des messages deployes */
  private Vector deployedMessages;

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

  // Constructeur
  public ForumsSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context,
        "com.stratelia.webactiv.forums.multilang.forumsBundle",
        "com.stratelia.webactiv.forums.settings.forumsIcons");

    deployedMessages = new Vector();
    deployedForums = new Vector();

    if (forumsBM == null) {
      try {
        ForumsBMHome forumsBMHome = (ForumsBMHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.FORUMSBM_EJBHOME, ForumsBMHome.class);
        forumsBM = forumsBMHome.create();
      } catch (Exception e) {
        throw new EJBException(e.getMessage());
      }
    }
  }

  public NotificationSender getNotificationSender() {
    if (notifSender == null) {
      notifSender = new NotificationSender(getComponentId());
    }
    return notifSender;
  }

  public Forum[] getForumsList() {
    try {
      ArrayList forums = forumsBM.getForums(new ForumPK(getComponentId(),
          getSpaceId()));
      return (Forum[]) forums.toArray(new Forum[forums.size()]);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public Forum[] getForumsListByCategory(String categoryId) {
    SilverTrace.debug("forums",
        "ForumsSessionController.getForumsListByCategory()", "",
        "categoryId = " + categoryId);

    Forum[] result = new Forum[0];
    ForumPK forumPK = new ForumPK(getComponentId(), getSpaceId());
    try {
      ArrayList forums = forumsBM.getForumsByCategory(forumPK, categoryId);
      result = (Forum[]) forums.toArray(new Forum[forums.size()]);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
    SilverTrace.debug("forums",
        "ForumsSessionController.getForumsListByCategory()", "", "retour = "
            + result);
    return result;
  }

  public Forum getForum(int forumId) {
    try {
      return forumsBM.getForum(getForumPK(forumId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public String getForumName(int forumId) {
    try {
      return forumsBM.getForumName(forumId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public boolean isForumActive(int forumId) {
    try {
      return forumsBM.isForumActive(forumId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public int getForumParentId(int forumId) {
    try {
      return forumsBM.getForumParentId(forumId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public int[] getForumSonsIds(int forumId) {
    int[] sonsIds = new int[0];
    try {
      ArrayList ids = forumsBM.getForumSonsIds(getForumPK(forumId));
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
      return forumsBM.getForumSonsIds(getForumPK(forumId)).size();
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void deployForum(int id) {
    deployedForums.add(new Integer(id));
  }

  public void undeployForum(int id) {
    if (deployedForums.size() > 0) {
      int i = 0;
      boolean loop = true;
      while ((i < deployedForums.size()) && (loop)) {
        if (((Integer) deployedForums.elementAt(i)).intValue() == id) {
          loop = false;
          deployedForums.removeElementAt(i);
        }
        i++;
      }
    }
  }

  public boolean forumIsDeployed(int id) {
    if (deployedForums.size() > 0) {
      int i = 0;
      while (i < deployedForums.size()) {
        if (((Integer) deployedForums.elementAt(i)).intValue() == id) {
          return true;
        }
        i++;
      }
    }
    return false;
  }

  public void lockForum(int id, int level) {
    ForumPK forumPK = new ForumPK(getComponentId(), getSpaceId(), String
        .valueOf(id));
    try {
      forumsBM.lockForum(forumPK, level);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public int unlockForum(int id, int level) {
    ForumPK forumPK = new ForumPK(getComponentId(), getSpaceId(), String
        .valueOf(id));
    try {
      return forumsBM.unlockForum(forumPK, level);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  /**
   * Cree un nouveau forum dans la datasource
   * 
   * @param String
   *          nom du forum
   * @param String
   *          description du forum
   * @param String
   *          l'id du createur du forum
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
    try {
      if (!StringUtil.isDefined(categoryId)) {
        categoryId = null;
      }
      return forumsBM.createForum(forumPK, truncateTextField(forumName),
          truncateTextArea(forumDescription), forumCreator, forumParent,
          categoryId, keywords);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  /**
   * Met a jour les informations sur un forum dans la datasource
   * 
   * @param int l'ID du forum dans la datasource
   * @param String
   *          nom du forum
   * @param String
   *          description du forum
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
      forumsBM
          .updateForum(getForumPK(forumId), truncateTextField(forumName),
              truncateTextArea(forumDescription), forumParent, categoryId,
              keywords);
    } catch (RemoteException re) {
      SilverTrace.error("forums", "ForumsSessionController.updateForum()",
          "forums.EXE_UPDATE_FORUM_FAILED", re.getMessage());
    }
  }

  /**
   * Supprime un forum et tous ses sous-forums a partir de son ID
   * 
   * @param int l'ID du forum dans la datasource
   * @author frageade
   * @since 3 Octobre 2000
   */
  public void deleteForum(int forumId) {
    try {
      forumsBM.deleteForum(getForumPK(forumId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  /**
   * Indexe un forum a partir de son ID
   * 
   * @param int l'ID du forum dans la datasource
   * @author frageade
   * @since 23 Aout 2001
   */
  public void indexForum(int forumId) {
    try {
      forumsBM.createIndex(getForumPK(forumId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  // Methodes messages

  /**
   * Liste les messages d'un forum
   * 
   * @param String
   *          id du forum
   * @return Vector la liste des messages
   * @author frageade
   * @since 04 Octobre 2000
   */
  public Message[] getMessagesList(int forumId) {
    try {
      Collection messages = forumsBM.getMessages(getForumPK(forumId));
      return (Message[]) messages.toArray(new Message[messages.size()]);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public Message[] getMessagesList(int forumId, int messageId) {
    Vector messageList = new Vector();
    Message[] messages = getMessagesList(forumId);
    int i = 0;
    boolean parentMessageFound = false;
    int currentMessageId;
    while (i < messages.length && !parentMessageFound) {
      Message message = messages[i];
      currentMessageId = message.getId();
      if (messageId == currentMessageId) {
        messageList.add(message);
        parentMessageFound = true;
      }
      i++;
    }
    fillMessageList(messageList, messages, messageId);
    return (Message[]) messageList.toArray(new Message[messageList.size()]);
  }

  private void fillMessageList(Vector messageList, Message[] messages,
      int messageId) {
    for (int i = 0; i < messages.length; i++) {
      Message message = messages[i];
      if (message.getParentId() == messageId) {
        messageList.add(message);
        fillMessageList(messageList, messages, message.getId());
      }
    }
  }

  /**
   * Récupère le dernier message d'un forum
   * 
   * @param String
   *          id du forum
   * @return String les champs du dernier message
   * @author sfariello
   * @since
   */
  public Object[] getLastMessage(int forumId) {
    return getLastMessage(forumId, -1);
  }

  public Object[] getLastMessage(int forumId, int messageId) {
    try {
      Message message = (messageId != -1 ? forumsBM.getLastMessage(
          getForumPK(forumId), messageId) : forumsBM
          .getLastMessage(getForumPK(forumId)));
      if (message != null) {
        UserDetail user = getUserDetail(message.getAuthor());
        SilverTrace.debug("forums", "ForumsSessioncontroller.getLastMessage()",
            "root.MSG_GEN_ENTER_METHOD", "message = " + message.toString());
        return new Object[] { String.valueOf(message.getId()),
            message.getDate(),
            (user != null ? user.getDisplayedName() : "Unknown") };
      }
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
    return null;
  }

  /**
   * Nombre de sujets d'un forum
   * 
   * @param String
   *          id du forum
   * @return int le nombre de sujets
   * @author sfariello
   * @since 07 Décembre 2007
   */
  public int getNbSubjects(int forumId) {
    try {
      return forumsBM.getNbMessages(forumId, typeSubjects);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  /**
   * Nombre de messages d'un forum
   * 
   * @param String
   *          id du forum
   * @return int le nombre de messages
   * @author sfariello
   * @since 07 Décembre 2007
   */
  public int getNbMessages(int forumId) {
    try {
      return forumsBM.getNbMessages(forumId, typeMessages);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public int getAuthorNbMessages(String userId) {
    try {
      return forumsBM.getAuthorNbMessages(userId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public int getNbResponses(int forumId, int messageId) {
    try {
      return forumsBM.getNbResponses(forumId, messageId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  /**
   * Recupere les infos d'un message
   * 
   * @param String
   *          id du message
   * @return Vector la liste des champs du message
   * @author frageade
   * @since 04 Octobre 2000
   */
  public Message getMessage(int messageId) {
    try {
      return forumsBM.getMessage(getMessagePK(messageId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public String getMessageTitle(int messageId) {
    try {
      return forumsBM.getMessageTitle(messageId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public int getMessageParentId(int messageId) {
    try {
      return forumsBM.getMessageParentId(messageId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  /**
   * Cree un nouveau message dans la datasource
   * 
   * @param String
   *          titre du message
   * @param String
   *          id de l'auteur du message
   * @param Strinf
   *          id du forum
   * @param String
   *          id du message parent
   * @param String
   *          texte du message
   * @return String l'id du message créé
   * @author frageade
   * @since 04 Octobre 2000
   */
  public int createMessage(String title, String author, int forumId,
      int parentId, String text, String keywords) {
    MessagePK messagePK = new MessagePK(getComponentId(), getSpaceId());
    int messageId = 0;

    try {
      // creation du message dans la base
      messageId = forumsBM.createMessage(messagePK, truncateTextField(title),
          author, null, forumId, parentId, text, keywords);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }

    // Send notification to subscribers
    try {
      if (parentId != 0) {
        sendNotification(title, text, parentId, messageId);
      }
    } catch (Exception e) {
      SilverTrace.warn("forums", "ForumsSessionController.createMessage()",
          "forums.MSG_NOTIFY_USERS_FAILED", null, e);
    }
    return messageId;
  }

  public void updateMessage(int messageId, int parentId, String title,
      String text) {
    MessagePK messagePK = getMessagePK(messageId);
    try {
      forumsBM.updateMessage(messagePK, truncateTextField(title), text,
          getUserId());
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }

    // Send notification to subscribers
    try {
      if (parentId != 0) {
        sendNotification(title, text, parentId, messageId);
      }
    } catch (Exception e) {
      SilverTrace.warn("forums", "ForumsSessionController.createMessage()",
          "forums.MSG_NOTIFY_USERS_FAILED", null, e);
    }
  }

  public void updateMessageKeywords(int messageId, String keywords) {
    try {
      forumsBM.updateMessageKeywords(getMessagePK(messageId), keywords);
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
    Vector subscribers = listAllSubscribers(parentId);
    if (subscribers.size() > 0) {
      ResourceLocator resource = new ResourceLocator(
          "com.stratelia.webactiv.forums.settings.forumsMails", getLanguage());
      // Preparation des donnees
      String[] targetUserIds = (String[]) subscribers.toArray(new String[0]);

      HashMap values = new HashMap();
      values.put("title", title);
      values.put("text", text);
      values.put("originTitle", getMessageTitle(parentId));
      values.put("componentId", getComponentId());
      values.put("messageId", String.valueOf(messageId));

      String mailSubject = StringUtil.format(resource.getString(mailType
          + ".subject"), values);
      String mailBody = StringUtil.format(resource
          .getString(mailType + ".body"), values);
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

  /**
   * Indexe un message a partir de son ID
   * 
   * @param String
   *          l'ID du message dans la datasource
   * @author frageade
   * @since 23 Aout 2001
   */
  public void indexMessage(int messageId) {
    try {
      forumsBM.createIndex(getMessagePK(messageId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  /**
   * Supprime un message et tous ses sous-messages a partir de son ID
   * 
   * @param String
   *          l'ID du message dans la datasource
   * @author frageade
   * @since 04 Octobre 2000
   */
  public void deleteMessage(int messageId) {
    try {
      forumsBM.deleteMessage(getMessagePK(messageId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void deployMessage(int id) {
    deployedMessages.add(new Integer(id));
  }

  public void undeployMessage(int id) {
    if (deployedMessages.size() > 0) {
      int i = 0;
      boolean loop = true;
      while ((i < deployedMessages.size()) && (loop)) {
        if (((Integer) deployedMessages.elementAt(i)).intValue() == id) {
          loop = false;
          deployedMessages.removeElementAt(i);
        }
        i++;
      }
    }
  }

  public boolean messageIsDeployed(int id) {
    int i = 0;
    while (i < deployedMessages.size()) {
      if (((Integer) deployedMessages.elementAt(i)).intValue() == id) {
        return true;
      }
      i++;
    }
    return false;
  }

  public boolean isModerator(String userId, int forumId) throws ForumsException {
    boolean result = false;
    try {
      result = forumsBM.isModerator(userId, getForumPK(forumId));
      int parentId = getForumParentId(forumId);
      while ((!result) && (parentId != 0)) {
        result = (result || forumsBM.isModerator(userId, getForumPK(parentId)));
        parentId = getForumParentId(parentId);
      }
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
    return result;
  }

  public void addModerator(int forumId, String userId) {
    try {
      forumsBM.addModerator(getForumPK(forumId), userId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void removeModerator(int forumId, String userId) {
    try {
      forumsBM.removeModerator(getForumPK(forumId), userId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void removeAllModerators(int forumId) {
    try {
      forumsBM.removeAllModerators(getForumPK(forumId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void moveMessage(int messageId, int forumId) {
    try {
      forumsBM.moveMessage(getMessagePK(messageId), getForumPK(forumId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void subscribeMessage(int messageId, String userId) {
    try {
      forumsBM.subscribeMessage(getMessagePK(messageId), userId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void unsubscribeMessage(int messageId, String userId) {
    try {
      forumsBM.unsubscribeMessage(getMessagePK(messageId), userId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void removeAllSubscribers(int messageId) {
    try {
      forumsBM.removeAllSubscribers(getMessagePK(messageId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public boolean isSubscriber(int messageId, String userId) {
    try {
      return forumsBM.isSubscriber(getMessagePK(messageId), userId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public Vector listAllSubscribers(int messageId) {
    Vector subscribers = new Vector();
    try {
      subscribers = forumsBM.listAllSubscribers(getMessagePK(messageId));
      int parentId = getMessageParentId(messageId);
      while (parentId != 0) {
        subscribers.addAll(forumsBM.listAllSubscribers(getMessagePK(parentId)));
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
      isNewMessage = forumsBM.isNewMessageByForum(userId, getForumPK(forumId));
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
      isNewMessage = forumsBM.isNewMessage(userId, getForumPK(forumId),
          messageId);
      SilverTrace.info("forums", "ForumsSessionController.isNewMessage()",
          "root.MSG_GEN_PARAM_VALUE", "isNewMessage = " + isNewMessage);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
    return isNewMessage;
  }

  public void setLastVisit(String userId, int messageId) {
    try {
      forumsBM.setLastVisit(userId, messageId);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public UserDetail[] listUsers() {
    UserDetail[] userDetails = CollectionUtil
        .sortUserDetailArray(getOrganizationController().getAllUsers(
            getComponentId()));
    return (userDetails != null ? userDetails : new UserDetail[0]);
  }

  public String getAuthorName(String userId) {
    UserDetail userDetail = getOrganizationController().getUserDetail(userId);
    return (userDetail != null ? (userDetail.getFirstName() + " " + userDetail
        .getLastName()).trim() : null);
  }

  public String getAdminIds() {
    return NotificationSender
        .getIdsLineFromUserArray(getOrganizationController().getUsers(
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
      return forumsBM.getSilverObjectId(getForumPK(objectId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public void close() {
    try {
      if (forumsBM != null) {
        forumsBM.remove();
      }
    } catch (RemoteException e) {
      SilverTrace.error("forums", "ForumsSessionController.close", "", e);
    } catch (RemoveException e) {
      SilverTrace.error("forums", "ForumsSessionController.close", "", e);
    }
  }

  public Collection getAllCategories() {
    try {
      return forumsBM.getAllCategories(getComponentId());
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public synchronized void createCategory(Category category) {
    try {
      category.setCreationDate(DateUtil.date2SQLDate(new Date()));
      category.setCreatorId(getUserId());
      category.getNodePK().setComponentName(getComponentId());
      forumsBM.createCategory(category);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public Category getCategory(String categoryId) {
    try {
      // rechercher la catégorie
      NodePK nodePK = new NodePK(categoryId, getComponentId());
      return forumsBM.getCategory(nodePK);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public synchronized void updateCategory(Category category) {
    try {
      SilverTrace.error("forums", "ForumsSessionController.updateCategory", "",
          "category = " + category.getName());
      forumsBM.updateCategory(category);
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public synchronized void deleteCategory(String categoryId) {
    try {
      SilverTrace.error("forums", "ForumsSessionController.deleteCategory", "",
          "categoryId = " + categoryId);
      forumsBM.deleteCategory(categoryId, getComponentId());
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

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
      return forumsBM.getForumTags(getForumPK(forumId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public String getMessageKeywords(int messageId) {
    try {
      return forumsBM.getMessageTags(getMessagePK(messageId));
    } catch (RemoteException re) {
      throw new EJBException(re.getMessage());
    }
  }

  public NotationDetail getForumNotation(int forumId) {
    try {
      return getNotationBm().getNotation(getForumNotationPk(forumId));
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

  private StatisticBm getStatisticBm() {
    if (statisticBm == null) {
      try {
        StatisticBmHome statisticHome = (StatisticBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME,
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

  private NotationBm getNotationBm() {
    if (notationBm == null) {
      try {
        NotationBmHome notationHome = (NotationBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.NOTATIONBM_EJBHOME, NotationBmHome.class);
        notationBm = notationHome.create();
      } catch (Exception e) {
        throw new NotationRuntimeException(
            "KmeliaSessionController.getNotationBm()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return notationBm;
  }

  private ForumPK getForumPK(int forumId) {
    return new ForumPK(getComponentId(), getSpaceId(), String.valueOf(forumId));
  }

  private MessagePK getMessagePK(int messageId) {
    return new MessagePK(getComponentId(), getSpaceId(), String
        .valueOf(messageId));
  }

  private NotationPK getForumNotationPk(int forumId) {
    return new NotationPK(String.valueOf(forumId), getComponentId(),
        Notation.TYPE_FORUM, getUserId());
  }

  private NotationPK getMessageNotationPk(int messageId) {
    return new NotationPK(String.valueOf(messageId), getComponentId(),
        Notation.TYPE_MESSAGE, getUserId());
  }

}