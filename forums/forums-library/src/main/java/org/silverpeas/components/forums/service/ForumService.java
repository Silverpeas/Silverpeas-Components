/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.forums.service;

import com.silverpeas.subscribe.util.SubscriptionSubscriberList;
import org.silverpeas.components.forums.model.Forum;
import org.silverpeas.components.forums.model.ForumDetail;
import org.silverpeas.components.forums.model.ForumPK;
import org.silverpeas.components.forums.model.Message;
import org.silverpeas.components.forums.model.MessagePK;
import org.silverpeas.components.forums.model.Moderator;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import org.silverpeas.util.ServiceProvider;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Forums service layer interface
 * @author frageade
 */
public interface ForumService {

  static ForumService get() {
    return ServiceProvider.getService(ForumService.class);
  }

  Forum getForum(ForumPK forumPK);

  String getForumName(int forumId);

  void deleteAll(String instanceId);

  boolean isForumActive(int forumId);

  int getForumParentId(int forumId);

  String getForumInstanceId(int forumId);

  Collection<ForumDetail> getForums(Collection<ForumPK> forumPKs);

  /**
   * @param forumPK forum primary key
   * @return a ForumDetail from the forum primary key identifier
   */
  ForumDetail getForumDetail(ForumPK forumPK);

  Collection<Forum> getForumRootList(String instanceId);

  Collection<Forum> getForumsList(Collection<ForumPK> forumPKs);

  Collection<Message> getThreadsList(Collection<MessagePK> messagePKs);

  List<Forum> getForums(ForumPK forumPK);

  List<Forum> getForumsByCategory(ForumPK forumPK, String categoryId);

  List<String> getForumSonsIds(ForumPK forumPK);

  int createForum(ForumPK forumPK, String forumName, String forumDescription,
      String forumCreator, int forumParent, String categoryId, String keywords);

  void updateForum(ForumPK forumPK, String forumName, String forumDescription,
      int forumParent, String categoryId, String keywords);

  void lockForum(ForumPK forumPK, int level);

  int unlockForum(ForumPK forumPK, int level);

  void deleteForum(ForumPK forumPK);

  Collection<Message> getMessages(ForumPK forumPK);

  Message getMessage(MessagePK messagePK);

  String getMessageTitle(int messageId);

  int getMessageParentId(int messageId);

  Message getLastMessage(ForumPK forumPK, String status);

  Message getLastMessage(ForumPK forumPK, int messageParentId, String status);

  Message getLastMessage(ForumPK forumPK, List<String> messageParentId, String status);

  Collection getLastMessageRSS(String instanceId, int nbReturned);

  int getNbMessages(int forumId, String type, String status);

  int getAuthorNbMessages(String userId, String status);

  int getNbResponses(int forumId, int messageId, String status);

  boolean isNewMessageByForum(String userId, ForumPK forumPK, String status);

  boolean isNewMessage(String userId, ForumPK forumPK, int messageId, String status);

  void setLastVisit(String userId, int messageId);

  int createMessage(MessagePK messagePK, String messageTitle, String messageAuthor,
      Date messageCreationdate, int messageForum, int messageParent, String messageText,
      String keywords, String status);

  void updateMessage(MessagePK messagePK, String title, String message, String userId,
      String status);

  void updateMessageKeywords(MessagePK messagePK, String keywords);

  void deleteMessage(MessagePK messagePK);

  boolean isModerator(String userId, ForumPK forumPK);

  void addModerator(ForumPK forumPK, String userId);

  void removeModerator(ForumPK forumPK, String userId);

  void removeAllModerators(ForumPK forumPK);

  List<Moderator> getModerators(int forumId);

  void moveMessage(MessagePK messagePK, ForumPK forumPK);

  void subscribeMessage(MessagePK messagePK, String userId);

  void unsubscribeMessage(MessagePK messagePK, String userId);

  void subscribeForum(ForumPK forumPK, String userId);

  void unsubscribeForum(ForumPK forumPK, String userId);

  SubscriptionSubscriberList listAllSubscribers(MessagePK messagePK);

  SubscriptionSubscriberList listAllSubscribers(ForumPK forumPK);

  SubscriptionSubscriberList listAllSubscribers(String instanceId);

  boolean isSubscriber(MessagePK messagePK, String userId);

  boolean isSubscriberByInheritance(MessagePK messagePK, String userId);

  boolean isSubscriber(ForumPK forumPK, String userId);

  boolean isSubscriberByInheritance(ForumPK forumPK, String userId);

  boolean isSubscriber(String instanceId, String userId);

  void createIndex(ForumPK forumPK);

  void createIndex(MessagePK messagePK);

  int getSilverObjectId(ForumPK forumPK);

  String createCategory(NodeDetail category);

  void deleteCategory(String categoryId, String instanceId);

  void updateCategory(NodeDetail category);

  NodeDetail getCategory(NodePK nodePK);

  Collection<NodeDetail> getAllCategories(String instanceId);

  String getForumTags(ForumPK forumPK);

  String getMessageTags(MessagePK messagePK);


  Collection<Message> getLastThreads(ForumPK forumPK, int count);

  Collection<Message> getNotAnsweredLastThreads(ForumPK forumPK, int count);
}