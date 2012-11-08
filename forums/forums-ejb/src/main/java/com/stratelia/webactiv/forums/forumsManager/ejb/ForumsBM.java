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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.forums.forumsManager.ejb;

import com.stratelia.webactiv.forums.models.ForumDetail;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.ejb.EJBObject;

import com.stratelia.webactiv.forums.models.ForumPK;
import com.stratelia.webactiv.forums.models.MessagePK;
import com.stratelia.webactiv.forums.models.Forum;
import com.stratelia.webactiv.forums.models.Message;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * Cette classe est l'interface Remote du Business Manager qui gere les forums
 * @author frageade
 * @since September 2000
 */
public interface ForumsBM extends EJBObject {

  public Forum getForum(ForumPK forumPK) throws RemoteException;

  public String getForumName(int forumId) throws RemoteException;

  public boolean isForumActive(int forumId) throws RemoteException;

  public int getForumParentId(int forumId) throws RemoteException;

  public String getForumInstanceId(int forumId) throws RemoteException;

  public Collection<ForumDetail> getForums(Collection<ForumPK> forumPKs) throws RemoteException;

  /**
   * 
   * @param forumPK forum primary key
   * @return a ForumDetail from the forum primary key identifier
   * @throws RemoteException
   */
  public ForumDetail getForumDetail(ForumPK forumPK) throws RemoteException;

  public Collection<Forum> getForumsList(Collection<ForumPK> forumPKs) throws RemoteException;

  public Collection<Message> getThreadsList(Collection<MessagePK> messagePKs)
      throws RemoteException;

  public List<Forum> getForums(ForumPK forumPK) throws RemoteException;

  public List<Forum> getForumsByCategory(ForumPK forumPK, String categoryId)
      throws RemoteException;

  public List<String> getForumSonsIds(ForumPK forumPK) throws RemoteException;

  public int createForum(ForumPK forumPK, String forumName,
      String forumDescription, String forumCreator, int forumParent,
      String categoryId, String keywords) throws RemoteException;

  public void updateForum(ForumPK forumPK, String forumName,
      String forumDescription, int forumParent, String categoryId,
      String keywords) throws RemoteException;

  public void lockForum(ForumPK forumPK, int level) throws RemoteException;

  public int unlockForum(ForumPK forumPK, int level) throws RemoteException;

  public void deleteForum(ForumPK forumPK) throws RemoteException;

  public Collection<Message> getMessages(ForumPK forumPK) throws RemoteException;

  public Message getMessage(MessagePK messagePK) throws RemoteException;

  public String getMessageTitle(int messageId) throws RemoteException;

  public int getMessageParentId(int messageId) throws RemoteException;

  public Message getLastMessage(ForumPK forumPK, String status) throws RemoteException;

  public Message getLastMessage(ForumPK forumPK, int messageParentId, String status)
      throws RemoteException;

  public Message getLastMessage(ForumPK forumPK, List<String> messageParentId, String status)
      throws RemoteException;

  public Collection getLastMessageRSS(String instanceId, int nbReturned)
      throws RemoteException;

  public int getNbMessages(int forumId, String type, String status) throws RemoteException;

  public int getAuthorNbMessages(String userId, String status) throws RemoteException;

  public int getNbResponses(int forumId, int messageId, String status) throws RemoteException;

  public boolean isNewMessageByForum(String userId, ForumPK forumPK, String status)
      throws RemoteException;

  public boolean isNewMessage(String userId, ForumPK forumPK, int messageId, String status)
      throws RemoteException;

  public void setLastVisit(String userId, int messageId) throws RemoteException;

  public int createMessage(MessagePK messagePK, String messageTitle,
      String messageAuthor, Date messageCreationdate, int messageForum,
      int messageParent, String messageText, String keywords, String status)
      throws RemoteException;

  public void updateMessage(MessagePK messagePK, String title, String message,
      String userId, String status) throws RemoteException;

  public void updateMessageKeywords(MessagePK messagePK, String keywords)
      throws RemoteException;

  public void deleteMessage(MessagePK messagePK) throws RemoteException;

  public boolean isModerator(String userId, ForumPK forumPK)
      throws RemoteException;

  public void addModerator(ForumPK forumPK, String userId)
      throws RemoteException;

  public void removeModerator(ForumPK forumPK, String userId)
      throws RemoteException;

  public void removeAllModerators(ForumPK forumPK) throws RemoteException;

  public List<String> getModerators(int forumId) throws RemoteException;

  public void moveMessage(MessagePK messagePK, ForumPK forumPK)
      throws RemoteException;

  public void subscribeMessage(MessagePK messagePK, String userId)
      throws RemoteException;

  public void unsubscribeMessage(MessagePK messagePK, String userId)
      throws RemoteException;

  public void removeAllSubscribers(MessagePK messagePK) throws RemoteException;

  public Vector<String> listAllSubscribers(MessagePK messagePK) throws RemoteException;

  public boolean isSubscriber(MessagePK messagePK, String userId)
      throws RemoteException;

  public void createIndex(ForumPK forumPK) throws RemoteException;

  public void createIndex(MessagePK messagePK) throws RemoteException;

  public int getSilverObjectId(ForumPK forumPK) throws RemoteException;

  public String createCategory(NodeDetail category) throws RemoteException;

  public void deleteCategory(String categoryId, String instanceId)
      throws RemoteException;

  public void updateCategory(NodeDetail category) throws RemoteException;

  public NodeDetail getCategory(NodePK nodePK) throws RemoteException;

  public Collection<NodeDetail> getAllCategories(String instanceId) throws RemoteException;

  public String getForumTags(ForumPK forumPK) throws RemoteException;

  public String getMessageTags(MessagePK messagePK) throws RemoteException;

  public Collection getLastThreads(ForumPK forumPK, int count)
      throws RemoteException;

  public Collection getNotAnsweredLastThreads(ForumPK forumPK, int count)
      throws RemoteException;
}