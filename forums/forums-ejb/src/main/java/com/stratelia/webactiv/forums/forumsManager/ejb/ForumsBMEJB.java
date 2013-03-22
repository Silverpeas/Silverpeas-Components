/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.webactiv.forums.forumsManager.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;
import org.silverpeas.wysiwyg.control.WysiwygController;

import com.silverpeas.notation.ejb.NotationBm;
import com.silverpeas.notation.model.Notation;
import com.silverpeas.notation.model.NotationPK;
import com.silverpeas.tagcloud.ejb.TagCloudBm;
import com.silverpeas.tagcloud.model.TagCloud;
import com.silverpeas.tagcloud.model.TagCloudPK;
import com.silverpeas.tagcloud.model.TagCloudUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.forums.ForumsContentManager;
import com.stratelia.webactiv.forums.forumsException.ForumsRuntimeException;
import com.stratelia.webactiv.forums.models.Forum;
import com.stratelia.webactiv.forums.models.ForumDetail;
import com.stratelia.webactiv.forums.models.ForumPK;
import com.stratelia.webactiv.forums.models.Message;
import com.stratelia.webactiv.forums.models.MessagePK;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

import static com.silverpeas.util.i18n.I18NHelper.defaultLanguage;

/**
 * Cette classe est le Business Manager qui gere les forums
 *
 */
@Stateless (name="Forums" , description = "Stateless session EJB to manage forums.")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class ForumsBMEJB implements ForumsBM {
  @EJB private NodeBm nodeBm;
  @EJB private TagCloudBm tagCloudBm;
  @EJB private NotationBm notationBm;

  private static final long serialVersionUID = -6809840977338911593L;
  private final ForumsContentManager forumsContentManager = new ForumsContentManager();

  public Collection<ForumDetail> getForums(Collection<ForumPK> forumPKs) {
    Connection con = openConnection();
    try {
      return ForumsDAO.selectByForumPKs(con, forumPKs);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getForums()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUMS_LIST_FAILED", e);
    } finally {
      DBUtil.close(con);
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
      DBUtil.close(con);
    }
  }

  public Collection<Forum> getForumsList(Collection<ForumPK> forumPKs) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getForumsByKeys(con, forumPKs);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getForumsList()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUMS_LIST_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public Collection<Message> getThreadsList(Collection<MessagePK> messagePKs) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getThreadsByKeys(con, messagePKs);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getThreadsList()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_THREADS_LIST_FAILED", e);
    } finally {
      DBUtil.close(con);
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
      DBUtil.close(con);
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
      DBUtil.close(con);
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
      DBUtil.close(con);
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
      DBUtil.close(con);
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
      DBUtil.close(con);
    }
  }

  /**
   * @param forumPK
   * @return
   */
  public List<Forum> getForums(ForumPK forumPK) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getForumsList(con, forumPK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getForums()", SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUMS_LIST_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public ForumDetail getForumDetail(ForumPK forumPK) throws RemoteException {
    Connection con = openConnection();
    try {
      return ForumsDAO.getForumDetail(con, forumPK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getForumDetail",
          SilverpeasRuntimeException.ERROR, "problem to load forum detail pk=" + forumPK.getId(), e);
    } finally {
      DBUtil.close(con);
    }
  }

  public List<Forum> getForumsByCategory(ForumPK forumPK, String categoryId) {
    Connection con = openConnection();
    SilverTrace.debug("forums", "ForumsBMEJB.getForumsByCategory()", "",
        "categoryId = " + categoryId);
    try {
      return ForumsDAO.getForumsListByCategory(con, forumPK, categoryId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getForumsByCategory()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUMS_LIST_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @param forumPK
   * @return
   */
  public List<String> getForumSonsIds(ForumPK forumPK) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getForumSonsIds(con, forumPK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getForumSons()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUMS_SONS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Verrouille recursivement l'arborescence d'un forum en ecriture a partir de sa primary key
   *
   * @param ForumPK la primary key du forum
   * @param int le niveau de verrouillage
   * @author frageade
   * @since 29 Septembre 2000
   */
  public void lockForum(ForumPK forumPK, int level) {
    List<String> sonsIds = getForumSonsIds(forumPK);
    for (String sonsId : sonsIds) {
      lockForum(new ForumPK(forumPK.getComponentName(), sonsId), level);
    }
    Connection con = openConnection();
    try {
      ForumsDAO.lockForum(con, forumPK, level);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.lockForum()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_LOCK_FORUM_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Deverrouille recursivement un forum en ecriture a partir de sa primary key
   *
   * @param ForumPK la primary key du forum
   * @param int le niveau de verrouillage
   * @return int le code d'erreur
   * @author frageade
   * @since 29 Septembre 2000
   */
  public int unlockForum(ForumPK forumPK, int level) {
    List<String> sonsIds = getForumSonsIds(forumPK);
    for (String sonsId : sonsIds) {
      unlockForum(new ForumPK(forumPK.getComponentName(), sonsId), level);
    }

    int result = 0;
    Connection con = openConnection();
    try {
      result = ForumsDAO.unlockForum(con, forumPK, level);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.unlockForum()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_UNLOCK_FORUM_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
    return result;
  }

  /**
   * Supprime un forum et tous ses sous-forums a partir de sa primary key
   *
   * @param ForumPK la primary key du forum
   * @author frageade
   * @since 3 Octobre 2000
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void deleteForum(ForumPK forumPK) {
    List<String> sonsIds = getForumSonsIds(forumPK);
    for (String sonsId : sonsIds) {
      deleteForum(new ForumPK(forumPK.getComponentName(), sonsId));
    }

    Connection con = openConnection();
    try {
      // Recuperation des ids de messages
      List<String> messagesIds = getMessagesIds(forumPK);

      // Suppression du forum et de ses messages
      ForumsDAO.deleteForum(con, forumPK);

      // Suppression de l'index du forum dans le moteur de recherches
      deleteIndex(forumPK);

      // Suppression de l'index de chaque message dans le moteur de recherches
      for (String messagesId : messagesIds) {
        deleteMessage(new MessagePK(forumPK.getComponentName(), messagesId));
      }
      forumsContentManager.deleteSilverContent(con, forumPK);
      deleteTagCloud(forumPK);
      deleteNotation(forumPK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.deleteForum()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_DELETE_FORUM_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Cree un nouveau forum dans la datasource
   *
   * @param ForumPK la primary key
   * @param String nom du forum
   * @param String description du forum
   * @param String l'id du createur du forum
   * @param int l'id du forum parent
   * @param String l'id de la categorie
   * @return String l'id du nouveau forum
   * @author frageade
   * @since 02 Octobre 2000
   */
  public int createForum(ForumPK forumPK, String forumName, String forumDescription,
      String forumCreator, int forumParent, String categoryId, String keywords) {
    Connection con = openConnection();
    try {
      int forumId = ForumsDAO.createForum(con, forumPK, forumName, forumDescription, forumCreator,
          forumParent, categoryId);
      forumPK.setId(String.valueOf(forumId));
      createIndex(forumPK);
      forumsContentManager.createSilverContent(con, forumPK, forumCreator);
      createTagCloud(forumPK, keywords);
      return forumId;
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.createForum()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_CREATE_FORUM_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Met a jour les informations sur un forum dans la datasource
   *
   * @param ForumPK la primary key du forum
   * @param String nom du forum
   * @param String description du forum
   * @param int l'id du forum parent
   * @param String l'id de la catégorie
   * @author frageade
   * @since 03 Octobre 2000
   */
  public void updateForum(ForumPK forumPK, String forumName, String forumDescription,
      int forumParent, String categoryId, String keywords) {
    updateForum(forumPK, forumName, forumDescription, forumParent, categoryId, keywords, true);
  }

  private void updateForum(ForumPK forumPK, String forumName,
      String forumDescription, int forumParent, String categoryId,
      String keywords, boolean updateTagCloud) {
    Connection con = openConnection();
    try {
      ForumsDAO.updateForum(con, forumPK, forumName, forumDescription, forumParent, categoryId);
      deleteIndex(forumPK);
      createIndex(forumPK);
      if (updateTagCloud) {
        updateTagCloud(forumPK, keywords);
      }
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.updateForum()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_UPDATE_FORUM_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * @param forumPK
   * @return
   */
  private ArrayList<Message> getMessagesList(ForumPK forumPK) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getMessagesList(con, forumPK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getMessagesList()",
          SilverpeasRuntimeException.ERROR,
          "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public Collection<Message> getMessages(ForumPK forumPK) {
    ArrayList<Message> messages = getMessagesList(forumPK);
    String componentId = forumPK.getInstanceId();
    for (int i = 0, n = messages.size(); i < n; i++) {
      Message message = (Message) messages.get(i);
      message.setText(getWysiwygContent(componentId, String.valueOf(message
          .getId())));
    }
    return messages;
  }

  private List<String> getSubjectsIds(ForumPK forumPK) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getSubjectsIds(con, forumPK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getSubjectsIds()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUM_MESSAGE_IDS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private List<String> getMessagesIds(ForumPK forumPK, int messageParentId) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getMessagesIds(con, forumPK, messageParentId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getMessagesIds()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUM_MESSAGE_IDS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  private List<String> getMessagesIds(ForumPK forumPK) {
    return getMessagesIds(forumPK, -1);
  }

  public int getNbMessages(int forumId, String type, String status) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getNbMessages(con, forumId, type, status);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getNbMessages()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public int getAuthorNbMessages(String userId, String status) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getAuthorNbMessages(con, userId, status);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getAuthorNbMessages()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public int getNbResponses(int forumId, int messageId, String status) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getNbResponses(con, forumId, messageId, status);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getNbResponses()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Retourne le dernier message d'un forum
   *
   * @param ForumPK la primary key du forum
   * @return Vector la liste des champs du dernier message
   * @author sfariello
   * @since
   */
  public Message getLastMessage(ForumPK forumPK, String status) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getLastMessage(con, forumPK, status);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getLastMessage()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public Collection getLastMessageRSS(String instanceId, int nbReturned) {
    // retourne les nbReturned messages des forums de l'instance instanceId
    Connection con = openConnection();
    Collection messages = new ArrayList();

    try {
      // récupère la liste des id des messages
      Collection<String> allMessagesIds = ForumsDAO.getLastMessageRSS(con, instanceId);
      Iterator<String> it = allMessagesIds.iterator();
      // prendre que les nbReturned derniers
      while (it.hasNext() && nbReturned != 0) {
        String messageId = (String) it.next();
        MessagePK messagePK = new MessagePK(instanceId, messageId);
        messages.add(getMessageInfos(messagePK));
        nbReturned--;
      }
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getLastMessageRSS()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
    return messages;
  }

  public Message getLastMessage(ForumPK forumPK, int messageParentId, String status) {
    Connection con = openConnection();
    try {
      // liste de tous les messages de la discussion
      List<String> messagesIds = getMessagesIds(forumPK, messageParentId);

      // ajouter la "racine" du message dans la liste de ses réponses
      messagesIds.add(String.valueOf(messageParentId));

      // récupération de la date du dernier message du forum
      return getLastMessage(forumPK, messagesIds, status);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getLastMessage()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public Message getLastMessage(ForumPK forumPK, List<String> messageParentIds, String status) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getLastMessage(con, forumPK, messageParentIds, status);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getLastMessage()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Retourne vrai s'il y a des messages non lus sur ce forum depuis la dernière visite
   *
   * @param forumId l'id du forum
   * @param userId l'id de l'utilisateur
   * @return String la date de la dernière visite
   * @author sfariello
   * @since
   */
  public boolean isNewMessageByForum(String userId, ForumPK forumPK, String status) {
    // liste de tous les sujets du forum
    List<String> messagesIds = getSubjectsIds(forumPK);
    int messageParentId;
    for (int i = 0, n = messagesIds.size(); i < n; i++) {
      // pour ce message on recherche la date de la dernière visite
      messageParentId = Integer.parseInt((String) messagesIds.get(i));
      SilverTrace.info("forums", "ForumsBMEJB.isNewMessageByForum()",
          "root.MSG_GEN_PARAM_VALUE", "messageParentId = " + messageParentId);
      if (isNewMessage(userId, forumPK, messageParentId, status)) {
        return true;
      }
    }
    return false;
  }

  public boolean isNewMessage(String userId, ForumPK forumPK,
      int messageParentId, String status) {
    Connection con = openConnection();
    try {
      // liste de tous les messages de la discussion
      List<String> messagesIds = getMessagesIds(forumPK, messageParentId);
      // ajouter la "racine" du message dans la liste de ses réponses
      messagesIds.add(String.valueOf(messageParentId));

      // récupération de la date du dernier message du forum
      Message message = getLastMessage(forumPK, messagesIds, status);
      // date du dernier message de la discussion
      Date dateLastMessageBySubject = (message != null ? message.getDate() : null);
      SilverTrace.info("forums", "ForumsBMEJB.isNewMessage()", "root.MSG_GEN_PARAM_VALUE",
          "date du dernier message du sujet = " + dateLastMessageBySubject);

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
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
    return false;
  }

  /**
   * enregistre la date de la dernière visite d'un utilisateur sur un forum
   *
   * @param messageId l'id du message
   * @param userId l'id de l'utilisateur
   * @author sfariello
   * @since
   */
  public void setLastVisit(String userId, int messageId) {
    Connection con = openConnection();
    try {
      ForumsDAO.addLastVisit(con, userId, messageId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.setLastVisit()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_FORUM_MESSAGE_LIST_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Recupere les infos d'un message
   *
   * @param MessagePK la primary key du message
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
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_MESSAGE_INFOS_FAILED", e);
    } finally {
      DBUtil.close(con);
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
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_MESSAGE_INFOS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public String getMessageTitle(int messageId) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getMessageTitle(con, messageId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getMessageTitle()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_MESSAGE_INFOS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public int getMessageParentId(int messageId) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getMessageParentId(con, messageId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getMessageParentId()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_MESSAGE_INFOS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Cree un nouveau message
   *
   * @param messagePK la primary key du message
   * @param title titre du message
   * @param authorId id de l'auteur du message
   * @param creationDate date de creation
   * @param forumId id du forum
   * @param parentId id du message parent
   * @param content texte du message
   * @param keywords the message keywords
   * @param status the message status
   * @return l'id du nouveau
   */
  public int createMessage(MessagePK messagePK, String title, String authorId, Date creationDate,
      int forumId, int parentId, String content, String keywords, String status) {
    Connection con = openConnection();
    try {
      int messageId = ForumsDAO.createMessage(con, title, authorId,
          creationDate, forumId, parentId, status);
      messagePK.setId(String.valueOf(messageId));
      createIndex(messagePK);
      createTagCloud(messagePK, keywords);
      createWysiwyg(messagePK, content, authorId);
      return messageId;
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.createMessage()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_CREATE_MESSAGE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public void updateMessage(MessagePK messagePK, String title, String message,
      String userId, String status) {
    Connection con = openConnection();
    try {
      ForumsDAO.updateMessage(con, messagePK, title, status);
      deleteIndex(messagePK);
      createIndex(messagePK);
      updateWysiwyg(messagePK, message, userId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.updateMessage()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_CREATE_MESSAGE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  public void updateMessageKeywords(MessagePK messagePK, String keywords) {
    try {
      updateTagCloud(messagePK, keywords);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.updateMessage()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_CREATE_MESSAGE_FAILED", e);
    }
  }

  /**
   * Supprime un message et tous ses sous-messages a partir de sa primary key
   *
   * @param MessagePK la primary key du message
   * @author frageade
   * @since 04 Octobre 2000
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void deleteMessage(MessagePK messagePK) {
    Connection con = openConnection();
    Vector<String> v = new Vector<String>();

    try {
      v = ForumsDAO.getMessageSons(con, messagePK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.deleteMessage()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_MESSAGE_SONS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
    if (v.size() > 0) {
      for (String messageId : v) {
        deleteMessage(new MessagePK(messagePK.getComponentName(), messageId));
      }
    }
    con = openConnection();
    try {
      ForumsDAO.deleteMessage(con, messagePK);
      deleteIndex(messagePK);
      deleteTagCloud(messagePK);
      deleteNotation(messagePK);
      deleteAllAttachments(messagePK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.deleteMessage()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_DELETE_MESSAGE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param userId
   * @param forumPK
   * @return
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
        DBUtil.close(con);
      }
    }
    return false;
  }

  /**
   * Method declaration
   *
   * @param forumPK
   * @param userId
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
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param forumPK
   * @param userId
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
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param forumPK
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
      DBUtil.close(con);
    }
  }

  public List<String> getModerators(int forumId) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getModerators(con, forumId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getModerators()",
          SilverpeasRuntimeException.ERROR, "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param messagePK
   * @param forumPK
   * @see
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void moveMessage(MessagePK messagePK, ForumPK forumPK) {
    Connection con = openConnection();
    Vector<String> v = new Vector<String>();

    try {
      v = ForumsDAO.getMessageSons(con, messagePK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.moveMessage()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_MESSAGE_SONS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
    if (!v.isEmpty()) {
      for (String messageId : v) {
        moveMessage(new MessagePK(messagePK.getComponentName(), messageId), forumPK);
      }
    }
    con = openConnection();
    try {
      ForumsDAO.moveMessage(con, messagePK, forumPK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.moveMessage()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_MOVE_MESSAGE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Liste tous les sous-messages d'un message
   *
   * @param MessagePK la primary key du message pere
   * @return Vector liste des ids fils
   * @author frageade
   * @since 11 Octobre 2000
   */
  public Vector<String> getMessageSons(MessagePK messagePK) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getMessageSons(con, messagePK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getMessageSons()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_MESSAGE_SONS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Liste tous les sous-messages d'un message récursivement
   *
   * @param MessagePK la primary key du message pere
   * @return Vector liste des ids fils
   * @author frageade
   * @since 11 Octobre 2000
   */
  public Vector<String> getAllMessageSons(MessagePK messagePK) {
    Connection con = openConnection();
    try {
      return ForumsDAO.getAllMessageSons(con, messagePK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getAllMessageSons()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_GET_ALL_MESSAGE_SONS_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param messagePK
   * @param userId
   * @see
   */
  public void subscribeMessage(MessagePK messagePK, String userId) {
    Connection con = openConnection();
    try {
      ForumsDAO.subscribeMessage(con, messagePK, userId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.subscribeMessage()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_SUBSCEIBE_MESSAGE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param messagePK
   * @param userId
   * @see
   */
  public void unsubscribeMessage(MessagePK messagePK, String userId) {
    Connection con = openConnection();
    try {
      ForumsDAO.unsubscribeMessage(con, messagePK, userId);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.unsubscribeMessage()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_UNSUBSCRIBE_MESSAGE_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param messagePK
   * @see
   */
  public void removeAllSubscribers(MessagePK messagePK) {
    Connection con = openConnection();
    try {
      ForumsDAO.removeAllSubscribers(con, messagePK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.removeAllSubscribers()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_DELETE_ALL_SUSCRIBER_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param messagePK
   * @return
   * @see
   */
  public Vector<String> listAllSubscribers(MessagePK messagePK) {
    Connection con = openConnection();
    try {
      return ForumsDAO.listAllSubscribers(con, messagePK);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.listAllSubscribers()",
          SilverpeasRuntimeException.ERROR, "forums.EXE_LIST_ALL_SUSCRIBER_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Method declaration
   *
   * @param messagePK
   * @param userId
   * @return
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
      DBUtil.close(con);
    }
    return result;
  }

  /**
   * Method declaration
   *
   * @param messagePK
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
      WysiwygController.index(componentId, messageId);
    }
    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  /**
   * Method declaration
   *
   * @param messagePK
   * @see
   */
  private void deleteIndex(MessagePK messagePK) {
    IndexEngineProxy.removeIndexEntry(new IndexEntryPK(messagePK
        .getComponentName(), "Message", messagePK.getId()));
  }

  /**
   * Method declaration
   *
   * @param forumPK
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
   * @param forumPK
   * @see
   */
  private void deleteIndex(ForumPK forumPK) {
    IndexEngineProxy.removeIndexEntry(new IndexEntryPK(forumPK
        .getComponentName(), "Forum", forumPK.getId()));
  }

  /**
   * Ouverture de la connection vers la source de donnees
   *
   * @return Connection la connection
   */
  public Connection openConnection() {
    try {
      return DBUtil.makeConnection(JNDINames.FORUMS_DATASOURCE);
    } catch (com.stratelia.webactiv.util.exception.UtilException ue) {
      throw new ForumsRuntimeException("ForumsBmEJB.openConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", ue);
    }
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
      silverObjectId = forumsContentManager.getSilverObjectId(forumPK.getId(), instanceId);
      if (silverObjectId == -1) {
        String creatorId = getForumCreatorId(forumId);
        silverObjectId = forumsContentManager.createSilverContent(null, forumPK, creatorId);
      }
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR, "forums.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
    return silverObjectId;
  }

  public String createCategory(NodeDetail category) {
    try {
      NodePK nodePK = nodeBm.createNode(category, new NodeDetail());
      return nodePK.getId();
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.createCategory()",
          SilverpeasRuntimeException.ERROR, "forums.MSG_CATEGORY_NOT_CREATE", e);
    }
  }

  public void updateCategory(NodeDetail category) {
    try {
      SilverTrace.error("forums", "ForumsBMEJB.updateCategory", "",
          "category = " + category.getName());
      nodeBm.setDetail(category);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.updateCategory()",
          SilverpeasRuntimeException.ERROR, "forums.MSG_CATEGORY_NOT_UPDATE", e);
    }
  }

  public void deleteCategory(String categoryId, String instanceId) {
    try {
      // pour cette catégorie, rechercher les forums et mettre '0' dans la
      // catégorie
      List<Forum> forums = getForumsByCategory(new ForumPK(instanceId, null), categoryId);
      Forum forum;
      int forumId;
      for (int i = 0, n = forums.size(); i < n; i++) {
        forum = forums.get(i);
        forumId = forum.getId();
        ForumPK forumPK = new ForumPK(instanceId, String.valueOf(forumId));
        updateForum(forumPK, forum.getName(), forum.getDescription(), forum.getParentId(), "0",
            null, false);
      }

      // suppression de la catégorie
      NodePK nodePk = new NodePK(categoryId, instanceId);
      nodeBm.removeNode(nodePk);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.deleteCategory()",
          SilverpeasRuntimeException.ERROR, "forums.MSG_CATEGORY_NOT_DELETE", e);
    }
  }

  public NodeDetail getCategory(NodePK pk) {
    try {
      return nodeBm.getDetail(pk);
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getCategory()",
          SilverpeasRuntimeException.ERROR, "forums.MSG_CATEGORY_NOT_EXIST", e);
    }
  }

  public Collection<NodeDetail> getAllCategories(String instanceId) {
    try {
      return nodeBm.getChildrenDetails(new NodePK("0", instanceId));
    } catch (Exception e) {
      throw new ForumsRuntimeException("ForumsBmEJB.getAllCategories()",
          SilverpeasRuntimeException.ERROR, "forums.MSG_CATEGORIES_NOT_EXIST",
          e);
    }
  }

  public Collection<Message> getLastThreads(ForumPK forumPK, int count) {
    return getLastThreads(forumPK, count, false);
  }

  public Collection<Message> getNotAnsweredLastThreads(ForumPK forumPK, int count) {
    return getLastThreads(forumPK, count, true);
  }

  private Collection<Message> getLastThreads(ForumPK forumPK, int count,
      boolean notAnswered) {
    Connection con = openConnection();
    try {
      ForumPK[] forumPKs;
      if (forumPK.getId().equals("0")) {
        // Derniers threads des forums du composant.
        ArrayList<String> forumsIds = ForumsDAO.getForumsIds(con, forumPK);
        int forumsCount = forumsIds.size();
        forumPKs = new ForumPK[forumsCount];
        String componentId = forumPK.getComponentName();
        for (int i = 0; i < forumsCount; i++) {
          forumPKs[i] = new ForumPK(componentId, forumsIds.get(i));
        }
      } else {
        // Derniers threads du forum.
        forumPKs = new ForumPK[]{forumPK};
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
          SilverpeasRuntimeException.ERROR, "forums.EXE_LIST_ALL_SUSCRIBER_FAILED", e);
    } finally {
      DBUtil.close(con);
    }
  }

  /**
   * Create the tagclouds corresponding to the forum detail.
   *
   * @param forumDetail The detail of the forum.
   * @throws RemoteException
   */
  private void createTagCloud(ForumPK forumPK, String keywords)
      throws RemoteException {
    TagCloud tagCloud =
        new TagCloud(forumPK.getComponentName(), forumPK.getId(), TagCloud.TYPE_FORUM);
    createTagCloud(tagCloud, keywords);
  }

  private void createTagCloud(MessagePK messagePK, String keywords)
      throws RemoteException {
    TagCloud tagCloud =
        new TagCloud(messagePK.getComponentName(), messagePK.getId(), TagCloud.TYPE_MESSAGE);
    createTagCloud(tagCloud, keywords);
  }

  private void createTagCloud(TagCloud tagCloud, String keywords) throws RemoteException {
    if (keywords != null) {
      String[] words = StringUtil.split(keywords, ' ');
      List<String> tagList = new ArrayList<String>(words.length);
      for (String tag : words) {
        String tagKey = TagCloudUtil.getTag(tag);
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
   * @param pubPK The primary key of the publication.
   * @throws RemoteException
   */
  private void deleteTagCloud(ForumPK forumPK) throws RemoteException {
    tagCloudBm.deleteTagCloud(new TagCloudPK(forumPK.getId(), forumPK.getComponentName()),
        TagCloud.TYPE_FORUM);
  }

  private void deleteTagCloud(MessagePK messagePK) throws RemoteException {
    tagCloudBm.deleteTagCloud(new TagCloudPK(messagePK.getId(), messagePK.getComponentName()),
        TagCloud.TYPE_MESSAGE);
  }

  /**
   * Update the tagclouds corresponding to the publication detail.
   *
   * @param forumDetail The detail of the forum.
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
    Collection<TagCloud> tagClouds = tagCloudBm.getTagCloudsByElement(
        forumPK.getComponentName(), forumPK.getId(), TagCloud.TYPE_FORUM);
    return getTags(tagClouds);
  }

  public String getMessageTags(MessagePK messagePK) throws RemoteException {
    Collection<TagCloud> tagClouds = tagCloudBm.getTagCloudsByElement(
        messagePK.getComponentName(), messagePK.getId(), TagCloud.TYPE_MESSAGE);
    return getTags(tagClouds);
  }

  private String getTags(Collection<TagCloud> tagClouds) {
    StringBuilder sb = new StringBuilder();
    for (TagCloud tagCloud : tagClouds) {
      if (sb.length() > 0) {
        sb.append(' ');
      }
      sb.append(tagCloud.getLabel());
    }
    return sb.toString();
  }


  private void deleteNotation(ForumPK forumPK) throws RemoteException {
    notationBm.deleteNotation(new NotationPK(forumPK.getId(), forumPK.getComponentName(),
        Notation.TYPE_FORUM));
  }

  private void deleteNotation(MessagePK messagePK) throws RemoteException {
    notationBm.deleteNotation(new NotationPK(messagePK.getId(), messagePK.getComponentName(),
        Notation.TYPE_MESSAGE));
  }

  private String getWysiwygContent(String componentId, String messageId) {
    String text = "";
    if (WysiwygController.haveGotWysiwyg(componentId, messageId, defaultLanguage)) {
      text = WysiwygController.load(componentId, messageId, defaultLanguage);
    }
    return text;
  }

  private void createWysiwyg(MessagePK messagePK, String text, String userId) {
    WysiwygController.createFileAndAttachment(text, messagePK, userId, defaultLanguage);
  }

  private void updateWysiwyg(MessagePK messagePK, String text, String userId) {
    String componentId = messagePK.getComponentName();
    String messageId = messagePK.getId();
    if (WysiwygController.haveGotWysiwyg(componentId, messageId, defaultLanguage)) {
      WysiwygController.updateFileAndAttachment(text, componentId, messageId, userId,
          defaultLanguage);
    } else {
      WysiwygController.createFileAndAttachment(text, messagePK, userId, defaultLanguage);
    }
  }

  private void deleteAllAttachments(MessagePK messagePK) {
    ForeignPK foreignKey = new ForeignPK(messagePK);
    List<SimpleDocument> documents =
        AttachmentServiceFactory.getAttachmentService().listAllDocumentsByForeignKey(foreignKey, null);
    for (SimpleDocument doc : documents) {
      AttachmentServiceFactory.getAttachmentService().deleteAttachment(doc);
    }
  }
}