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
package com.stratelia.webactiv.forums.forumsManager.ejb;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.util.SubscriptionSubscriberList;
import com.silverpeas.subscribe.util.SubscriptionSubscriberMapBySubscriberType;
import com.stratelia.webactiv.forums.models.Forum;
import com.stratelia.webactiv.forums.models.ForumDetail;
import com.stratelia.webactiv.forums.models.ForumPK;
import com.stratelia.webactiv.forums.models.Message;
import com.stratelia.webactiv.forums.models.MessagePK;
import com.stratelia.webactiv.forums.models.Moderator;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import org.silverpeas.util.ServiceProvider;

/**
 * Cette classe est l'interface Remote du Business Manager qui gere les forums
 *
 * @author frageade
 * @since September 2000
 */
@Local
public interface ForumsBM {

  static ForumsBM get() {
    return ServiceProvider.getService(ForumsBM.class);
  }

  public Forum getForum(ForumPK forumPK);

  public String getForumName(int forumId);

  public boolean isForumActive(int forumId);

  public int getForumParentId(int forumId);

  public String getForumInstanceId(int forumId);

  public Collection<ForumDetail> getForums(Collection<ForumPK> forumPKs);

  /**
   *
   * @param forumPK forum primary key
   * @return a ForumDetail from the forum primary key identifier
   * @
   */
  public ForumDetail getForumDetail(ForumPK forumPK);

  public Collection<Forum> getForumRootList(String instanceId);

  public Collection<Forum> getForumsList(Collection<ForumPK> forumPKs);

  public Collection<Message> getThreadsList(Collection<MessagePK> messagePKs);

  public List<Forum> getForums(ForumPK forumPK);

  public List<Forum> getForumsByCategory(ForumPK forumPK, String categoryId);

  public List<String> getForumSonsIds(ForumPK forumPK);

  public int createForum(ForumPK forumPK, String forumName, String forumDescription,
      String forumCreator, int forumParent, String categoryId, String keywords);

  public void updateForum(ForumPK forumPK, String forumName, String forumDescription,
      int forumParent, String categoryId, String keywords);

  public void lockForum(ForumPK forumPK, int level);

  public int unlockForum(ForumPK forumPK, int level);

  public void deleteForum(ForumPK forumPK);

  public Collection<Message> getMessages(ForumPK forumPK);

  public Message getMessage(MessagePK messagePK);

  public String getMessageTitle(int messageId);

  public int getMessageParentId(int messageId);

  public Message getLastMessage(ForumPK forumPK, String status);

  public Message getLastMessage(ForumPK forumPK, int messageParentId, String status);

  public Message getLastMessage(ForumPK forumPK, List<String> messageParentId, String status);

  public Collection getLastMessageRSS(String instanceId, int nbReturned);

  public int getNbMessages(int forumId, String type, String status);

  public int getAuthorNbMessages(String userId, String status);

  public int getNbResponses(int forumId, int messageId, String status);

  public boolean isNewMessageByForum(String userId, ForumPK forumPK, String status);

  public boolean isNewMessage(String userId, ForumPK forumPK, int messageId, String status);

  public void setLastVisit(String userId, int messageId);

  public int createMessage(MessagePK messagePK, String messageTitle, String messageAuthor,
      Date messageCreationdate, int messageForum, int messageParent, String messageText,
      String keywords, String status);

  public void updateMessage(MessagePK messagePK, String title, String message,
      String userId, String status);

  public void updateMessageKeywords(MessagePK messagePK, String keywords);

  public void deleteMessage(MessagePK messagePK);

  public boolean isModerator(String userId, ForumPK forumPK);

  public void addModerator(ForumPK forumPK, String userId);

  public void removeModerator(ForumPK forumPK, String userId);

  public void removeAllModerators(ForumPK forumPK);

  public List<Moderator> getModerators(int forumId);

  public void moveMessage(MessagePK messagePK, ForumPK forumPK);

  public void subscribeMessage(MessagePK messagePK, String userId);

  public void unsubscribeMessage(MessagePK messagePK, String userId);

  public void subscribeForum(ForumPK forumPK, String userId);

  public void unsubscribeForum(ForumPK forumPK, String userId);

  public SubscriptionSubscriberList listAllSubscribers(MessagePK messagePK);

  public SubscriptionSubscriberList listAllSubscribers(ForumPK forumPK);

  public SubscriptionSubscriberList listAllSubscribers(String instanceId);

  public boolean isSubscriber(MessagePK messagePK, String userId);

  public boolean isSubscriberByInheritance(MessagePK messagePK, String userId);

  public boolean isSubscriber(ForumPK forumPK, String userId);

  public boolean isSubscriberByInheritance(ForumPK forumPK, String userId);

  public boolean isSubscriber(String instanceId, String userId);

  public void createIndex(ForumPK forumPK);

  public void createIndex(MessagePK messagePK);

  public int getSilverObjectId(ForumPK forumPK);

  public String createCategory(NodeDetail category);

  public void deleteCategory(String categoryId, String instanceId);

  public void updateCategory(NodeDetail category);

  public NodeDetail getCategory(NodePK nodePK);

  public Collection<NodeDetail> getAllCategories(String instanceId);

  public String getForumTags(ForumPK forumPK);

  public String getMessageTags(MessagePK messagePK);


  public Collection<Message> getLastThreads(ForumPK forumPK, int count);

  public Collection<Message> getNotAnsweredLastThreads(ForumPK forumPK, int count);
}