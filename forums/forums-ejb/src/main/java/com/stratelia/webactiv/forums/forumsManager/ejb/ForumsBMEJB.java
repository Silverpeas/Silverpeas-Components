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
package com.stratelia.webactiv.forums.forumsManager.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.NamingException;

import com.silverpeas.notation.ejb.NotationBm;
import com.silverpeas.notation.ejb.NotationBmHome;
import com.silverpeas.notation.ejb.NotationRuntimeException;
import com.silverpeas.notation.model.Notation;
import com.silverpeas.notation.model.NotationPK;
import com.silverpeas.tagcloud.ejb.TagCloudBm;
import com.silverpeas.tagcloud.ejb.TagCloudBmHome;
import com.silverpeas.tagcloud.ejb.TagCloudRuntimeException;
import com.silverpeas.tagcloud.model.TagCloud;
import com.silverpeas.tagcloud.model.TagCloudPK;
import com.silverpeas.tagcloud.model.TagCloudUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.forums.ForumsContentManager;
import com.stratelia.webactiv.forums.forumEntity.ejb.ForumPK;
import com.stratelia.webactiv.forums.forumsException.ForumsRuntimeException;
import com.stratelia.webactiv.forums.messageEntity.ejb.MessagePK;
import com.stratelia.webactiv.forums.models.Category;
import com.stratelia.webactiv.forums.models.Forum;
import com.stratelia.webactiv.forums.models.Message;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * Cette classe est le Business Manager qui gere les forums
 *
 * @author frageade
 * @since September 2000
 */
public class ForumsBMEJB implements SessionBean {
  private ForumsContentManager forumsContentManager = null;

  public Collection getForums(Collection forumPKs) {
    Connection con = openConnection();
    try {
      return ForumsDAO.selectByForumPKs(con, forumPKs);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getForums()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUMS_LIST_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public Forum getForum(ForumPK forumPK) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getForum(con, forumPK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getForum()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUM_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public Collection getForumsList(Collection forumPKs) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getForumsByKeys(con, forumPKs);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getForumsList()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUMS_LIST_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public Collection getThreadsList(Collection messagePKs) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getThreadsByKeys(con, messagePKs);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getThreadsList()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_THREADS_LIST_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public String getForumName(int forumId) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getForumName(con, forumId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getForumName()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUM_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public boolean isForumActive(int forumId) {
    Connection con = openConnection();
    try {
      return ForumsDAO.isForumActive(con, forumId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.isForumActive()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUM_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public int getForumParentId(int forumId) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getForumParentId(con, forumId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getForumParentId()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUM_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public String getForumInstanceId(int forumId) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getForumInstanceId(con, forumId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getForumInstanceId()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUM_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public String getForumCreatorId(int forumId) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getForumCreatorId(con, forumId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getForumCreatorId()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUM_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * @param forumPK
   * @return
   */
  public ArrayList getForums(ForumPK forumPK) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getForumsList(con, forumPK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getForums()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUMS_LIST_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public ArrayList getForumsByCategory(ForumPK forumPK, String categoryId) {
    Connection con = openConnection();
    SilverTrace.debug("forums", "ForumsBMEJB.getForumsByCategory()", "",
        "categoryId = " + categoryId);
    try {
      return ForumsDAO.getForumsListByCategory(con, forumPK, categoryId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getForumsByCategory()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUMS_LIST_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * @param forumPK
   * @return
   */
  public ArrayList getForumSonsIds(ForumPK forumPK) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getForumSonsIds(con, forumPK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getForumSons()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUMS_SONS_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Verrouille recursivement l'arborescence d'un forum en ecriture a partir de
   * sa primary key
   *
   * @param ForumPK
   *          la primary key du forum
   * @param int le niveau de verrouillage
   * @author frageade
   * @since 29 Septembre 2000
   */
  public void lockForum(ForumPK forumPK, int level) {
    ArrayList sonsIds = getForumSonsIds(forumPK);
    for (int i = 0; i < sonsIds.size(); i++) {
      lockForum(new ForumPK(forumPK.getComponentName(), forumPK.getDomain(),
          (String) sonsIds.get(i)), level);
    }
    Connection con = openConnection();
    try {
      ForumsDAO.lockForum(con, forumPK, level);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.lockForum()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_LOCK_FORUM_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Deverrouille recursivement un forum en ecriture a partir de sa primary key
   *
   * @param ForumPK
   *          la primary key du forum
   * @param int le niveau de verrouillage
   * @return int le code d'erreur
   * @author frageade
   * @since 29 Septembre 2000
   */
  public int unlockForum(ForumPK forumPK, int level) {
    ArrayList sonsIds = getForumSonsIds(forumPK);
    for (int i = 0; i < sonsIds.size(); i++) {
      unlockForum(new ForumPK(forumPK.getComponentName(), forumPK.getDomain(),
          (String) sonsIds.get(i)), level);
    }

    int result = 0;
    Connection con = openConnection();
    try {
      result = ForumsDAO.unlockForum(con, forumPK, level);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.unlockForum()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_UNLOCK_FORUM_FAILED", e);
    } finally {
      closeConnection(con);
    }
    return result;
  }

  /**
   * Supprime un forum et tous ses sous-forums a partir de sa primary key
   *
   * @param ForumPK
   *          la primary key du forum
   * @author frageade
   * @since 3 Octobre 2000
   */
  public void deleteForum(ForumPK forumPK) {
    ArrayList sonsIds = getForumSonsIds(forumPK);
    for (int i = 0; i < sonsIds.size(); i++) {
      deleteForum(new ForumPK(forumPK.getComponentName(), forumPK.getDomain(),
          (String) sonsIds.get(i)));
    }

    Connection con = openConnection();
    try {
      // Recuperation des ids de messages
      ArrayList messagesIds = getMessagesIds(forumPK);

      // Suppression du forum et de ses messages
      ForumsDAO.deleteForum(con, forumPK);

      // Suppression de l'index du forum dans le moteur de recherches
      deleteIndex(forumPK);

      // Suppression de l'index de chaque message dans le moteur de recherches
      for (int i = 0; i < messagesIds.size(); i++) {
        deleteMessage(new MessagePK(forumPK.getComponentName(), forumPK
            .getDomain(), (String) messagesIds.get(i)));
      }

      getForumsContentManager().deleteSilverContent(con, forumPK);
      deleteTagCloud(forumPK);
      deleteNotation(forumPK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.deleteForum()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_DELETE_FORUM_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Cree un nouveau forum dans la datasource
   *
   * @param ForumPK
   *          la primary key
   * @param String
   *          nom du forum
   * @param String
   *          description du forum
   * @param String
   *          l'id du createur du forum
   * @param int l'id du forum parent
   * @param String
   *          l'id de la categorie
   * @return String l'id du nouveau forum
   * @author frageade
   * @since 02 Octobre 2000
   */
  public int createForum(ForumPK forumPK, String forumName,
      String forumDescription, String forumCreator, int forumParent,
      String categoryId, String keywords) {
    Connection con = openConnection();
    try {
      int forumId = ForumsDAO.createForum(con, forumPK, forumName,
          forumDescription, forumCreator, forumParent, categoryId);
      forumPK.setId(String.valueOf(forumId));
      createIndex(forumPK);

      getForumsContentManager().createSilverContent(con, forumPK, forumCreator);
      createTagCloud(forumPK, keywords);
      return forumId;
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.createForum()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_CREATE_FORUM_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Met a jour les informations sur un forum dans la datasource
   *
   * @param ForumPK
   *          la primary key du forum
   * @param String
   *          nom du forum
   * @param String
   *          description du forum
   * @param int l'id du forum parent
   * @param String
   *          l'id de la catégorie
   * @author frageade
   * @since 03 Octobre 2000
   */
  public void updateForum(ForumPK forumPK, String forumName,
      String forumDescription, int forumParent, String categoryId,
      String keywords) {
    updateForum(forumPK, forumName, forumDescription, forumParent, categoryId,
        keywords, true);
  }

  private void updateForum(ForumPK forumPK, String forumName,
      String forumDescription, int forumParent, String categoryId,
      String keywords, boolean updateTagCloud) {
    Connection con = openConnection();
    try {
      ForumsDAO.updateForum(con, forumPK, forumName, forumDescription,
          forumParent, categoryId);
      deleteIndex(forumPK);
      createIndex(forumPK);
      if (updateTagCloud) {
        updateTagCloud(forumPK, keywords);
      }
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.updateForum()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_UPDATE_FORUM_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * @param forumPK
   * @return
   */
  private ArrayList getMessagesList(ForumPK forumPK) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getMessagesList(con, forumPK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getMessagesList()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public Collection getMessages(ForumPK forumPK) {
    ArrayList messages = getMessagesList(forumPK);
    String componentId = forumPK.getInstanceId();
    for (int i = 0, n = messages.size(); i < n; i++) {
      Message message = (Message) messages.get(i);
      message.setText(getWysiwygContent(componentId, String.valueOf(message
          .getId())));
    }
    return messages;
  }

  private ArrayList getSubjectsIds(ForumPK forumPK) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getSubjectsIds(con, forumPK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getSubjectsIds()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUM_MESSAGE_IDS_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  private ArrayList getMessagesIds(ForumPK forumPK, int messageParentId) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getMessagesIds(con, forumPK, messageParentId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getMessagesIds()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUM_MESSAGE_IDS_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  private ArrayList getMessagesIds(ForumPK forumPK) {
    return getMessagesIds(forumPK, -1);
  }

  public int getNbMessages(int forumId, String type) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getNbMessages(con, forumId, type);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getNbMessages()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public int getAuthorNbMessages(String userId) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getAuthorNbMessages(con, userId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getAuthorNbMessages()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public int getNbResponses(int forumId, int messageId) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getNbResponses(con, forumId, messageId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getNbResponses()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Retourne le dernier message d'un forum
   *
   * @param ForumPK
   *          la primary key du forum
   * @return Vector la liste des champs du dernier message
   * @author sfariello
   * @since
   */
  public Message getLastMessage(ForumPK forumPK) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getLastMessage(con, forumPK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getLastMessage()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public Collection getLastMessageRSS(String instanceId, int nbReturned) {
    // retourne les nbReturned messages des forums de l'instance instanceId
    Connection con = openConnection();
    Collection messages = new ArrayList();

    try {
      // récupère la liste des id des messages
      Collection allMessagesIds = ForumsDAO.getLastMessageRSS(con, instanceId);
      Iterator it = allMessagesIds.iterator();
      // prendre que les nbReturned derniers
      String messageId;
      while (it.hasNext() && nbReturned != 0) {
        messageId = (String) it.next();
        MessagePK messagePK = new MessagePK(instanceId, null, messageId);
        messages.add(getMessageInfos(messagePK));
        nbReturned--;
      }
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getLastMessageRSS()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      closeConnection(con);
    }
    return messages;
  }

  public Message getLastMessage(ForumPK forumPK, int messageParentId) {
    Connection con = openConnection();
    try {
      // liste de tous les messages de la discussion
      ArrayList messagesIds = getMessagesIds(forumPK, messageParentId);

      // ajouter la "racine" du message dans la liste de ses réponses
      messagesIds.add(String.valueOf(messageParentId));

      // récupération de la date du dernier message du forum
      return getLastMessage(forumPK, messagesIds);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getLastMessage()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public Message getLastMessage(ForumPK forumPK, List messageParentIds) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getLastMessage(con, forumPK, messageParentIds);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getLastMessage()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Retourne vrai s'il y a des messages non lus sur ce forum depuis la dernière
   * visite
   *
   * @param forumId
   *          l'id du forum
   * @param userId
   *          l'id de l'utilisateur
   * @return String la date de la dernière visite
   * @author sfariello
   * @since
   */
  public boolean isNewMessageByForum(String userId, ForumPK forumPK) {
    // liste de tous les sujets du forum
    ArrayList messagesIds = getSubjectsIds(forumPK);
    int messageParentId;
    for (int i = 0, n = messagesIds.size(); i < n; i++) {
      // pour ce message on recherche la date de la dernière visite
      messageParentId = Integer.parseInt((String) messagesIds.get(i));
      SilverTrace.info("forums", "ForumsBMEJB.isNewMessageByForum()",
          "root.MSG_GEN_PARAM_VALUE", "messageParentId = " + messageParentId);
      if (isNewMessage(userId, forumPK, messageParentId)) {
        return true;
      }
    }
    return false;
  }

  public boolean isNewMessage(String userId, ForumPK forumPK,
      int messageParentId) {
    Connection con = openConnection();
    try {
      // liste de tous les messages de la discussion
      ArrayList messagesIds = getMessagesIds(forumPK, messageParentId);
      // ajouter la "racine" du message dans la liste de ses réponses
      messagesIds.add(String.valueOf(messageParentId));

      // récupération de la date du dernier message du forum
      Message message = getLastMessage(forumPK, messagesIds);
      // date du dernier message de la discussion
      Date dateLastMessageBySubject = (message != null ? message.getDate()
          : null);
      SilverTrace.info("forums", "ForumsBMEJB.isNewMessage()",
          "root.MSG_GEN_PARAM_VALUE", "date du dernier message du sujet = "
              + dateLastMessageBySubject);

      // recherche sur tous les messages de la date de visite la plus ancienne
      // date de la dernière visite pour un message
      Date dateLastVisit = ForumsDAO.getLastVisit(con, userId, messagesIds);

      if (dateLastMessageBySubject == null
          || dateLastVisit == null
          || (dateLastMessageBySubject != null && dateLastVisit != null && dateLastVisit
              .before(dateLastMessageBySubject))) {
        // la date de dernière visite de ce message est antérieure à la date du
        // dernier
        // message, il y a donc des réponses non lues pour ce message
        return true;
      }
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.isNewMessage()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      closeConnection(con);
    }
    return false;
  }

  /**
   * enregistre la date de la dernière visite d'un utilisateur sur un forum
   *
   * @param messageId
   *          l'id du message
   * @param userId
   *          l'id de l'utilisateur
   * @author sfariello
   * @since
   */
  public void setLastVisit(String userId, int messageId) {
    Connection con = openConnection();
    try {
      ForumsDAO.addLastVisit(con, userId, messageId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.setLastVisit()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Recupere les infos d'un message
   *
   * @param MessagePK
   *          la primary key du message
   * @return Vector la liste des champs du message
   * @author frageade
   * @since 04 Octobre 2000
   */
  private Vector getMessageInfos(MessagePK messagePK) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getMessageInfos(con, messagePK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getMessageInfos()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_MESSAGE_INFOS_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public Message getMessage(MessagePK messagePK) {
    Connection con = openConnection();
    try {
      Message message = ForumsDAO.getMessage(con, messagePK);
      message.setText(getWysiwygContent(messagePK.getInstanceId(), messagePK
          .getId()));
      return message;
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getMessage()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_MESSAGE_INFOS_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public String getMessageTitle(int messageId) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getMessageTitle(con, messageId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getMessageTitle()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_MESSAGE_INFOS_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  public int getMessageParentId(int messageId) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getMessageParentId(con, messageId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getMessageParentId()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_MESSAGE_INFOS_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Cree un nouveau message dans la datasource
   *
   * @param MessagePK
   *          la primary key du message
   * @param String
   *          titre du message
   * @param String
   *          id de l'auteur du message
   * @param String
   *          date de creation
   * @param Strinf
   *          id du forum
   * @param String
   *          id du message parent
   * @param String
   *          texte du message
   * @return String l'id du nouveau message
   * @author frageade
   * @since 04 Octobre 2000
   */
  public int createMessage(MessagePK messagePK, String messageTitle,
      String messageAuthor, Date messageCreationdate, int messageForum,
      int messageParent, String messageText, String keywords) {
    Connection con = openConnection();
    try {
      int messageId = ForumsDAO.createMessage(con, messageTitle, messageAuthor,
          messageCreationdate, messageForum, messageParent);
      messagePK.setId(String.valueOf(messageId));
      createIndex(messagePK);
      createTagCloud(messagePK, keywords);
      createWysiwyg(messagePK, messageText);
      return messageId;
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.createMessage()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_CREATE_MESSAGE_FAILED",
          e);
    } finally {
      closeConnection(con);
    }
  }

  public void updateMessage(MessagePK messagePK, String title, String message,
      String userId) {
    Connection con = openConnection();
    try {
      ForumsDAO.updateMessage(con, messagePK, title);
      deleteIndex(messagePK);
      createIndex(messagePK);
      updateWysiwyg(messagePK, message, userId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.updateMessage()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_CREATE_MESSAGE_FAILED",
          e);
    } finally {
      closeConnection(con);
    }
  }

  public void updateMessageKeywords(MessagePK messagePK, String keywords) {
    try {
      updateTagCloud(messagePK, keywords);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.updateMessage()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_CREATE_MESSAGE_FAILED",
          e);
    }
  }

  /**
   * Supprime un message et tous ses sous-messages a partir de sa primary key
   *
   * @param MessagePK
   *          la primary key du message
   * @author frageade
   * @since 04 Octobre 2000
   */
  public void deleteMessage(MessagePK messagePK) {
    Connection con = openConnection();
    Vector v = new Vector();

    try {
      v = ForumsDAO.getMessageSons(con, messagePK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.deleteMessage()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_MESSAGE_SONS_FAILED", e);
    } finally {
      closeConnection(con);
    }
    if (v.size() > 0) {
      for (int i = 0; i < v.size(); i++) {
        deleteMessage(new MessagePK(messagePK.getComponentName(), messagePK
            .getDomain(), (String) v.elementAt(i)));
      }
    }
    con = openConnection();
    try {
      ForumsDAO.deleteMessage(con, messagePK);
      deleteIndex(messagePK);
      deleteTagCloud(messagePK);
      deleteNotation(messagePK);
      deleteWysiwyg(messagePK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.deleteMessage()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_DELETE_MESSAGE_FAILED",
          e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Method declaration
   *
   *
   * @param userId
   * @param forumPK
   *
   * @return
   *
   * @see
   */
  public boolean isModerator(String userId, ForumPK forumPK) {
    if (!(forumPK.getId().equals("0"))) {
      Connection con = openConnection();
      try {
        return ForumsDAO.isModerator(con, forumPK, userId);
      } catch (Exception e) {
        throw new ForumsRuntimeException("ForumsBmEJB.isModerator()",
            SilverpeasRuntimeException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
      } finally {
        closeConnection(con);
      }
    }
    return false;
  }

  /**
   * Method declaration
   *
   *
   * @param forumPK
   * @param userId
   *
   * @see
   */
  public void addModerator(ForumPK forumPK, String userId) {
    Connection con = openConnection();
    try {
      ForumsDAO.addModerator(con, forumPK, userId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.addModerator()",
          SilverpeasRuntimeException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Method declaration
   *
   *
   * @param forumPK
   * @param userId
   *
   * @see
   */
  public void removeModerator(ForumPK forumPK, String userId) {
    Connection con = openConnection();
    try {
      ForumsDAO.removeModerator(con, forumPK, userId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.removeModerator()",
          SilverpeasRuntimeException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Method declaration
   *
   *
   * @param forumPK
   *
   * @see
   */
  public void removeAllModerators(ForumPK forumPK) {
    Connection con = openConnection();
    try {
      ForumsDAO.removeAllModerators(con, forumPK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.removeAllModerators()",
          SilverpeasRuntimeException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Method declaration
   *
   *
   * @param messagePK
   * @param forumPK
   *
   * @see
   */
  public void moveMessage(MessagePK messagePK, ForumPK forumPK) {
    Connection con = openConnection();
    Vector v = new Vector();

    try {
      v = ForumsDAO.getMessageSons(con, messagePK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.moveMessage()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_MESSAGE_SONS_FAILED", e);
    } finally {
      closeConnection(con);
    }
    if (v.size() > 0) {
      for (int i = 0; i < v.size(); i++) {
        moveMessage(new MessagePK(messagePK.getComponentName(), messagePK
            .getDomain(), (String) v.elementAt(i)), forumPK);
      }
    }
    con = openConnection();
    try {
      ForumsDAO.moveMessage(con, messagePK, forumPK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.moveMessage()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_MOVE_MESSAGE_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Liste tous les sous-messages d'un message
   *
   * @param MessagePK
   *          la primary key du message pere
   * @return Vector liste des ids fils
   * @author frageade
   * @since 11 Octobre 2000
   */
  public Vector getMessageSons(MessagePK messagePK) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getMessageSons(con, messagePK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getMessageSons()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_MESSAGE_SONS_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Liste tous les sous-messages d'un message récursivement
   *
   * @param MessagePK
   *          la primary key du message pere
   * @return Vector liste des ids fils
   * @author frageade
   * @since 11 Octobre 2000
   */
  public Vector getAllMessageSons(MessagePK messagePK) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getAllMessageSons(con, messagePK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getAllMessageSons()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_ALL_MESSAGE_SONS_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Method declaration
   *
   *
   * @param messagePK
   * @param userId
   *
   * @see
   */
  public void subscribeMessage(MessagePK messagePK, String userId) {
    Connection con = openConnection();
    try {
      ForumsDAO.subscribeMessage(con, messagePK, userId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.subscribeMessage()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_SUBSCEIBE_MESSAGE_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Method declaration
   *
   *
   * @param messagePK
   * @param userId
   *
   * @see
   */
  public void unsubscribeMessage(MessagePK messagePK, String userId) {
    Connection con = openConnection();
    try {
      ForumsDAO.unsubscribeMessage(con, messagePK, userId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.unsubscribeMessage()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_UNSUBSCRIBE_MESSAGE_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Method declaration
   *
   *
   * @param messagePK
   *
   * @see
   */
  public void removeAllSubscribers(MessagePK messagePK) {
    Connection con = openConnection();
    try {
      ForumsDAO.removeAllSubscribers(con, messagePK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.removeAllSubscribers()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_DELETE_ALL_SUSCRIBER_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Method declaration
   *
   *
   * @param messagePK
   *
   * @return
   *
   * @see
   */
  public Vector listAllSubscribers(MessagePK messagePK) {
    Connection con = openConnection();
    try {
      return ForumsDAO.listAllSubscribers(con, messagePK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.listAllSubscribers()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_LIST_ALL_SUSCRIBER_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Method declaration
   *
   *
   * @param messagePK
   * @param userId
   *
   * @return
   *
   * @see
   */
  public boolean isSubscriber(MessagePK messagePK, String userId) {
    Connection con = openConnection();
    boolean result = false;

    try {
      result = ForumsDAO.isSubscriber(con, messagePK, userId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.isSubscriber()",
          SilverpeasRuntimeException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      closeConnection(con);
    }
    return result;
  }

  /**
   * Method declaration
   *
   *
   * @param messagePK
   *
   * @see
   */
  public void createIndex(MessagePK messagePK) {
    FullIndexEntry indexEntry = null;

    if (messagePK != null) {
      Message message = getMessage(messagePK);
      String componentId = messagePK.getComponentName();
      String messageId = messagePK.getId();

      indexEntry = new FullIndexEntry(componentId, "Message", messageId);
      indexEntry.setTitle(message.getTitle());
      indexEntry.setCreationDate(message.getDate());
      indexEntry.setCreationUser(message.getAuthor());

      String wysiwygPath = getWysiwygPath(componentId, messageId);
      if (StringUtil.isDefined(wysiwygPath)) {
        indexEntry.addFileContent(wysiwygPath, null, "text/html", null);
      }
    }
    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  /**
   * Method declaration
   *
   *
   * @param messagePK
   *
   * @see
   */
  private void deleteIndex(MessagePK messagePK) {
    IndexEngineProxy.removeIndexEntry(new IndexEntryPK(messagePK
        .getComponentName(), "Message", messagePK.getId()));
  }

  /**
   * Method declaration
   *
   *
   * @param forumPK
   *
   * @see
   */
  public void createIndex(ForumPK forumPK) {
    if (forumPK != null) {
      Forum forum = getForum(forumPK);
      FullIndexEntry indexEntry = new FullIndexEntry(
          forumPK.getComponentName(), "Forum", forumPK.getId());
      indexEntry.setTitle(forum.getName());
      indexEntry.setPreView(forum.getDescription());
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  /**
   * Method declaration
   *
   *
   * @param forumPK
   *
   * @see
   */
  private void deleteIndex(ForumPK forumPK) {
    IndexEngineProxy.removeIndexEntry(new IndexEntryPK(forumPK
        .getComponentName(), "Forum", forumPK.getId()));
  }

  // Implementation des methodes de l'interface SessionBean

  /**
   * Method declaration
   *
   *
   * @see
   */
  public void ejbActivate() {
  }

  /**
   * Method declaration
   *
   *
   * @see
   */
  public void ejbPassivate() {
  }

  /**
   * Method declaration
   *
   *
   * @see
   */
  public void ejbRemove() {
  }

  /**
   * Method declaration
   *
   *
   * @param sc
   *
   * @see
   */
  public void setSessionContext(SessionContext sc) {
  }

  // Implementation de l'interface Home

  /**
   * Method declaration
   *
   *
   * @throws CreateException
   *
   * @see
   */
  public void ejbCreate() throws CreateException {
  }

  // Methodes internes

  /**
   * Ouverture de la connection vers la source de donnees
   *
   * @return Connection la connection
   * @exception RemoteException
   * @exception SQLException
   * @exception NamingException
   * @author frageade
   * @since 28 Septembre 2000
   */
  public Connection openConnection() {
    try {
      return DBUtil.makeConnection(JNDINames.FORUMS_DATASOURCE);
    } catch (com.stratelia.webactiv.util.exception.UtilException ue) {
      throw new ForumsRuntimeException("ForumsBmEJB.openConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED",
          ue);
    }
  }

  private void closeConnection(Connection con) {
    try {
      if (con != null) {
        con.close();
      }
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.closeConnection()",
          SilverpeasRuntimeException.ERROR, "root.EXE_CONNECTION_CLOSE_FAILED",
          e);
    }
  }

  private ForumsContentManager getForumsContentManager() {
    if (forumsContentManager == null) {
      forumsContentManager = new ForumsContentManager();
    }
    return forumsContentManager;
  }

  public int getSilverObjectId(ForumPK forumPK) {
    SilverTrace.info("forums", "ForumsBmEJB.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "forumPK = " + forumPK.toString());
    int silverObjectId = -1;
    try {
      int forumId = Integer.parseInt(forumPK.getId());
      String instanceId = forumPK.getComponentName();
      if (instanceId == null || instanceId.length() == 0) {
        instanceId = getForumInstanceId(forumId);
        forumPK.setComponentName(instanceId);
      }
      silverObjectId = getForumsContentManager().getSilverObjectId(
          forumPK.getId(), instanceId);
      if (silverObjectId == -1) {
        String creatorId = getForumCreatorId(forumId);
        silverObjectId = getForumsContentManager().createSilverContent(null,
            forumPK, creatorId);
      }
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR,
          "forums.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
    return silverObjectId;
  }

  public String createCategory(Category category) {
    try {
      NodePK nodePK = getNodeBm().createNode((NodeDetail) category,
          new NodeDetail());
      return nodePK.getId();
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.createCategory()",
          SilverpeasRuntimeException.ERROR, "forums.MSG_CATEGORY_NOT_CREATE", e);
    }
  }

  public void updateCategory(Category category) {
    try {
      SilverTrace.error("forums", "ForumsBMEJB.updateCategory", "",
          "category = " + category.getName());
      getNodeBm().setDetail((NodeDetail) category);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.updateCategory()",
          SilverpeasRuntimeException.ERROR, "forums.MSG_CATEGORY_NOT_UPDATE", e);
    }
  }

  public void deleteCategory(String categoryId, String instanceId) {
    try {
      // pour cette catégorie, rechercher les forums et mettre '0' dans la
      // catégorie
      ArrayList forums = getForumsByCategory(new ForumPK(instanceId, null),
          categoryId);
      Forum forum;
      int forumId;
      for (int i = 0, n = forums.size(); i < n; i++) {
        forum = (Forum) forums.get(i);
        forumId = forum.getId();
        ForumPK forumPK = new ForumPK(instanceId, null, Integer
            .toString(forumId));
        updateForum(forumPK, forum.getName(), forum.getDescription(), forum
            .getParentId(), "0", null, false);
      }

      // suppression de la catégorie
      NodePK nodePk = new NodePK(categoryId, instanceId);
      getNodeBm().removeNode(nodePk);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.deleteCategory()",
          SilverpeasRuntimeException.ERROR, "forums.MSG_CATEGORY_NOT_DELETE", e);
    }
  }

  public Category getCategory(NodePK pk) {
    try {
      return new Category(getNodeBm().getDetail(pk));
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getCategory()",
          SilverpeasRuntimeException.ERROR, "forums.MSG_CATEGORY_NOT_EXIST", e);
    }
  }

  public Collection getAllCategories(String instanceId) {
    try {
      return getNodeBm().getChildrenDetails(new NodePK("0", instanceId));
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getAllCategories()",
          SilverpeasRuntimeException.ERROR, "forums.MSG_CATEGORIES_NOT_EXIST",
          e);
    }
  }

  private NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getNodeBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return nodeBm;
  }

  public Collection getLastThreads(ForumPK forumPK, int count) {
    return getLastThreads(forumPK, count, false);
  }

  public Collection getNotAnsweredLastThreads(ForumPK forumPK, int count) {
    return getLastThreads(forumPK, count, true);
  }

  private Collection getLastThreads(ForumPK forumPK, int count,
      boolean notAnswered) {
    Connection con = openConnection();
    try {
      ForumPK[] forumPKs;
      if (forumPK.getId().equals("0")) {
        // Derniers threads des forums du composant.
        ArrayList forumsIds = ForumsDAO.getForumsIds(con, forumPK);
        int forumsCount = forumsIds.size();
        forumPKs = new ForumPK[forumsCount];
        String componentId = forumPK.getComponentName();
        for (int i = 0; i < forumsCount; i++) {
          forumPKs[i] = new ForumPK(componentId, "", (String) forumsIds.get(i));
        }
      } else {
        // Derniers threads du forum.
        forumPKs = new ForumPK[] { forumPK };
      }

      if (notAnswered) {
        // Threads non répondus.
        return ForumsDAO.getNotAnsweredLastThreads(con, forumPKs, count);
      } else {
        // Tous les threads.
        return ForumsDAO.getLastThreads(con, forumPKs, count);
      }
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getLastTheads()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_LIST_ALL_SUSCRIBER_FAILED", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * Create the tagclouds corresponding to the forum detail.
   *
   * @param forumDetail
   *          The detail of the forum.
   * @throws RemoteException
   */
  private void createTagCloud(ForumPK forumPK, String keywords)
      throws RemoteException {
    TagCloud tagCloud = new TagCloud(forumPK.getComponentName(), forumPK
        .getId(), TagCloud.TYPE_FORUM);
    createTagCloud(tagCloud, keywords);
  }

  private void createTagCloud(MessagePK messagePK, String keywords)
      throws RemoteException {
    TagCloud tagCloud = new TagCloud(messagePK.getComponentName(), messagePK
        .getId(), TagCloud.TYPE_MESSAGE);
    createTagCloud(tagCloud, keywords);
  }

  private void createTagCloud(TagCloud tagCloud, String keywords)
      throws RemoteException {
    if (keywords != null) {
      TagCloudBm tagCloudBm = getTagCloudBm();
      StringTokenizer st = new StringTokenizer(keywords, " ");
      String tag;
      String tagKey;
      ArrayList tagList = new ArrayList();
      while (st.hasMoreElements()) {
        tag = (String) st.nextElement();
        tagKey = TagCloudUtil.getTag(tag);
        if (!tagList.contains(tagKey)) {
          tagCloud.setTag(tagKey);
          tagCloud.setLabel(tag.toLowerCase());
          tagCloudBm.createTagCloud(tagCloud);
          tagList.add(tagKey);
        }
      }
    }
  }

  /**
   * Delete the tagclouds corresponding to the publication key.
   *
   * @param pubPK
   *          The primary key of the publication.
   * @throws RemoteException
   */
  private void deleteTagCloud(ForumPK forumPK) throws RemoteException {
    getTagCloudBm().deleteTagCloud(
        new TagCloudPK(forumPK.getId(), forumPK.getComponentName()),
        TagCloud.TYPE_FORUM);
  }

  private void deleteTagCloud(MessagePK messagePK) throws RemoteException {
    getTagCloudBm().deleteTagCloud(
        new TagCloudPK(messagePK.getId(), messagePK.getComponentName()),
        TagCloud.TYPE_MESSAGE);
  }

  /**
   * Update the tagclouds corresponding to the publication detail.
   *
   * @param forumDetail
   *          The detail of the forum.
   * @throws RemoteException
   */
  private void updateTagCloud(ForumPK forumPK, String keywords)
      throws RemoteException {
    deleteTagCloud(forumPK);
    createTagCloud(forumPK, keywords);
  }

  private void updateTagCloud(MessagePK messagePK, String keywords)
      throws RemoteException {
    deleteTagCloud(messagePK);
    createTagCloud(messagePK, keywords);
  }

  public String getForumTags(ForumPK forumPK) throws RemoteException {
    Collection tagClouds = getTagCloudBm().getTagCloudsByElement(
        forumPK.getComponentName(), forumPK.getId(), TagCloud.TYPE_FORUM);
    return getTags(tagClouds);
  }

  public String getMessageTags(MessagePK messagePK) throws RemoteException {
    Collection tagClouds = getTagCloudBm().getTagCloudsByElement(
        messagePK.getComponentName(), messagePK.getId(), TagCloud.TYPE_MESSAGE);
    return getTags(tagClouds);
  }

  private String getTags(Collection tagClouds) {
    Iterator iter = tagClouds.iterator();
    StringBuffer sb = new StringBuffer();
    TagCloud tagCloud;
    while (iter.hasNext()) {
      tagCloud = (TagCloud) iter.next();
      if (sb.length() > 0) {
        sb.append(" ");
      }
      sb.append(tagCloud.getLabel());
    }
    return sb.toString();
  }

  /**
   * @return The bean managing tagclouds.
   */
  private TagCloudBm getTagCloudBm() {
    try {
      TagCloudBmHome tagCloudBmHome = (TagCloudBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.TAGCLOUDBM_EJBHOME, TagCloudBmHome.class);
      TagCloudBm tagCloudBm = tagCloudBmHome.create();
      return tagCloudBm;
    } catch (Exception e) {
      throw new TagCloudRuntimeException(
          "KmeliaSessionController.getTagCloudBm()", SilverpeasException.ERROR,
          "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  private void deleteNotation(ForumPK forumPK) throws RemoteException {
    getNotationBm().deleteNotation(
        new NotationPK(forumPK.getId(), forumPK.getComponentName(),
            Notation.TYPE_FORUM));
  }

  private void deleteNotation(MessagePK messagePK) throws RemoteException {
    getNotationBm().deleteNotation(
        new NotationPK(messagePK.getId(), messagePK.getComponentName(),
            Notation.TYPE_MESSAGE));
  }

  /**
   * @return The bean managing tagclouds.
   */
  private NotationBm getNotationBm() {
    try {
      NotationBmHome notationBmHome = (NotationBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.NOTATIONBM_EJBHOME, NotationBmHome.class);
      NotationBm notationBm = notationBmHome.create();
      return notationBm;
    } catch (Exception e) {
      throw new NotationRuntimeException(
          "KmeliaSessionController.getNotationBm()", SilverpeasException.ERROR,
          "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  private String getWysiwygContent(String componentId, String messageId) {
    String text = "";
    if (WysiwygController.haveGotWysiwyg(null, componentId, messageId)) {
      try {
        text = WysiwygController.loadFileAndAttachment(null, componentId,
            messageId);
      } catch (WysiwygException e) {
        SilverTrace.error("forums", "ForumsBMEJB.getWysiwygContent()",
            "componentId = " + componentId + "messageId = " + messageId);
      }
    }
    return text;
  }

  private String getWysiwygPath(String componentId, String messageId) {
    String path = null;
    try {
      String wysiwygContent = WysiwygController.load(componentId, messageId,
          null);
      if (StringUtil.isDefined(wysiwygContent)) {
        path = WysiwygController.getWysiwygPath(componentId, messageId, null);
      }
    } catch (WysiwygException e) {
      SilverTrace.error("forums", "ForumsBMEJB.getWysiwygContent()",
          "componentId = " + componentId + " ; messageId = " + messageId);
    }
    return path;
  }

  private void createWysiwyg(MessagePK messagePK, String text) {
    try {
      WysiwygController.createFileAndAttachment(text, messagePK.getSpaceId(),
          messagePK.getComponentName(), messagePK.getId());
    } catch (WysiwygException e) {
      SilverTrace.error("forums", "ForumsBMEJB.createWysiwyg()", "spaceId = "
          + messagePK.getSpaceId() + " ; componentId = "
          + messagePK.getComponentName() + " ; messageId = "
          + messagePK.getId());
    }
  }

  private void updateWysiwyg(MessagePK messagePK, String text, String userId) {
    String spaceId = messagePK.getSpaceId();
    String componentId = messagePK.getComponentName();
    String messageId = messagePK.getId();
    try {
      if (WysiwygController.haveGotWysiwyg(spaceId, componentId, messageId)) {
        WysiwygController.updateFileAndAttachment(text, spaceId, componentId,
            messageId, userId);
      } else {
        WysiwygController.createFileAndAttachment(text, spaceId, componentId,
            messageId);
      }
    } catch (WysiwygException e) {
      SilverTrace.error("forums", "ForumsBMEJB.updateWysiwyg()", "spaceId = "
          + messagePK.getSpaceId() + " ; componentId = "
          + messagePK.getComponentName() + " ; messageId = "
          + messagePK.getId());
    }
  }

  private void deleteWysiwyg(MessagePK messagePK) {
    try {
      WysiwygController.deleteFileAndAttachment(messagePK.getComponentName(),
          messagePK.getId());
    } catch (WysiwygException e) {
      SilverTrace.error("forums", "ForumsBMEJB.deleteWysiwyg()",
          "componentId = " + messagePK.getComponentName() + " ; messageId = "
              + messagePK.getId());
    }
  }

}