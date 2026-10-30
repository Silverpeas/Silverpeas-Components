/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.discussion.service;

import java.util.List;
import java.util.Map;
import org.silverpeas.components.discussion.model.DiscussionLike;
import org.silverpeas.components.discussion.repository.DiscussionLikeRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.persistence.OptimisticLockException;
import org.silverpeas.components.discussion.model.Conversation;
import org.silverpeas.components.discussion.model.DiscussionMessage;
import org.silverpeas.components.discussion.model.DiscussionReadState;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.components.discussion.repository.DiscussionMessageRepository;
import org.silverpeas.components.discussion.repository.DiscussionReadStateRepository;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;

@Service
@Transactional
public class DefaultConversationService implements ConversationService {
  @Inject
  private ConversationRepository conversations;
  @Inject
  private DiscussionMessageRepository messages;
  @Inject
  private DiscussionReadStateRepository readStates;
  @Inject
  private DiscussionAccess access;

  @Inject
  private DiscussionLikeRepository likes;
  @Inject
  private DiscussionMentionService mentions;

  @Override
  public List<String> participantIds(String instanceId) {
    access.require(instanceId, SilverpeasRole.READER);
    Conversation conversation = conversations.main(instanceId, false);
    return messages.authorIds(conversation.getId());
  }

  @Override
  public MessagePage history(String instanceId, long before, int limit) {
    access.require(instanceId, SilverpeasRole.READER);
    if (before <= 0 || limit < 1 || limit > 100) {
      throw new IllegalArgumentException("Invalid history page");
    }
    Conversation conversation = conversations.main(instanceId, false);
    var found = messages.history(conversation.getId(), before, limit + 1);
    boolean more = found.size() > limit;
    var page = found.subList(0, Math.min(limit, found.size()));
    long next = page.isEmpty() ? before : page.getLast().getSequenceNumber();
    String userId = User.getCurrentRequester().getId();
    long lastRead = readStates.find(conversation.getId(), userId)
        .map(DiscussionReadState::getLastReadSequence).orElse(0L);
    return new MessagePage(page, next, more, conversation.getLastSequence(),
        messages.unread(conversation.getId(), userId, lastRead));
  }

  @Override
  public void publish(String instanceId, String body) {
    access.require(instanceId, SilverpeasRole.WRITER);
    Conversation conversation = conversations.main(instanceId, true);
    var message = new DiscussionMessage(conversation.getId(), conversation.nextSequence(), mentions.normalize(instanceId, body));
    message.createdBy(User.getCurrentRequester());
    messages.save(message);
    mentions.notifyNewMentions(instanceId, message, "");
  }

  @Override
  public void edit(String instanceId, String messageId, long version, String body) {
    DiscussionMessage message = editable(instanceId, messageId, version);
    String previousBody = message.getBody();
    message.edit(mentions.normalize(instanceId, body));
    message.updatedBy(User.getCurrentRequester());
    messages.save(message);
    mentions.notifyNewMentions(instanceId, message, previousBody);
  }

  @Override
  public void delete(String instanceId, String messageId, long version) {
    DiscussionMessage message = editable(instanceId, messageId, version);
    likes.deleteMessage(messageId);
    message.delete();
    message.updatedBy(User.getCurrentRequester());
    messages.save(message);
  }

  @Override
  public Map<String, LikeState> likeStates(String instanceId, List<String> messageIds) {
    access.require(instanceId, SilverpeasRole.READER);
    if (messageIds.size() > 100) {
      throw new IllegalArgumentException("Too many message identifiers");
    }
    Conversation conversation = conversations.main(instanceId, false);
    return likes.states(conversation.getId(), messageIds, User.getCurrentRequester().getId());
  }

  @Override
  public LikeState setLike(String instanceId, String messageId, boolean liked) {
    access.require(instanceId, SilverpeasRole.READER);
    User user = User.getCurrentRequester();
    if (user.isAnonymous() || user.isAccessGuest()) {
      throw new SecurityException("A user account is required to react");
    }
    Conversation conversation = conversations.main(instanceId, true);
    DiscussionMessage message = messageIn(conversation, messageId);
    if (message.isDeleted()) {
      throw new IllegalArgumentException("Deleted message");
    }
    var previous = likes.find(messageId, user.getId());
    if (liked && previous.isEmpty()) {
      var reaction = new DiscussionLike(conversation.getId(), messageId, user.getId());
      reaction.createdBy(user);
      likes.save(reaction);
    } else if (!liked && previous.isPresent()) {
      likes.delete(previous.orElseThrow());
    }
    likes.flush();
    return likes.states(conversation.getId(), List.of(messageId), user.getId())
        .getOrDefault(messageId, new LikeState(0, false));
  }

  @Override
  public MessagePage historyAround(String instanceId, String messageId) {
    access.require(instanceId, SilverpeasRole.READER);
    Conversation conversation = conversations.main(instanceId, false);
    DiscussionMessage message = messageIn(conversation, messageId);
    return history(instanceId, message.getSequenceNumber() + 1, 30);
  }

  private DiscussionMessage messageIn(Conversation conversation, String messageId) {
    DiscussionMessage message = messages.getById(messageId);
    if (message == null || !conversation.getId().equals(message.getConversationId())) {
      throw new IllegalArgumentException("Unknown discussion message");
    }
    return message;
  }

  private DiscussionMessage editable(String instanceId, String messageId, long version) {
    SilverpeasRole role = access.require(instanceId, SilverpeasRole.WRITER);
    Conversation conversation = conversations.main(instanceId, true);
    DiscussionMessage message = messageIn(conversation, messageId);
    if (role != SilverpeasRole.ADMIN &&
        !User.getCurrentRequester().getId().equals(message.getCreatorId())) {
      throw new SecurityException("Only the author or a manager can change this message");
    }
    if (message.isDeleted() || message.getVersion() != version) {
      throw new OptimisticLockException("Discussion message has changed");
    }
    return message;
  }

  @Override
  public void markRead(String instanceId, long sequence) {
    access.require(instanceId, SilverpeasRole.READER);
    User user = User.getCurrentRequester();
    if (user.isAccessGuest() || user.isAnonymous()) {
      return;
    }
    Conversation conversation = conversations.main(instanceId, true);
    DiscussionReadState state = readStates.find(conversation.getId(), user.getId())
        .orElseGet(() -> {
          var newState = new DiscussionReadState(conversation.getId(), user.getId());
          newState.createdBy(user);
          return newState;
        });
    state.advanceTo(sequence, conversation.getLastSequence());
    state.updatedBy(user);
    readStates.save(state);
  }
}
