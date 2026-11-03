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
  private DiscussionLinkedEventService linkedEvents;
  @Inject
  private DiscussionAccess access;

  @Inject
  private org.silverpeas.components.discussion.repository.DiscussionSeenRepository seen;
  @Inject
  private DiscussionLikeRepository likes;
  @Inject
  private DiscussionAttachmentService imageService;
  @Inject
  private DiscussionMentionService mentions;
  @Inject
  private org.silverpeas.components.discussion.search.DiscussionMessageIndex messageIndex;
  @Inject
  private org.silverpeas.components.discussion.notification.DiscussionSubscriptionDispatcher subscriptions;

  @Inject
  private org.silverpeas.components.discussion.notification.DiscussionChangeDispatcher changes;

  @Override
  public List<String> participantIds(String instanceId) {
    access.require(instanceId, SilverpeasRole.WRITER);
    Conversation conversation = conversations.current(instanceId, false);
    return messages.authorIds(conversation.getId());
  }

  @Override
  public MessagePage history(String instanceId, long before, int limit) {
    access.require(instanceId, SilverpeasRole.WRITER);
    if (before <= 0 || limit < 1 || limit > 100) {
      throw new IllegalArgumentException("Invalid history page");
    }
    Conversation conversation = conversations.current(instanceId, false);
    var found = messages.history(conversation.getId(), before, limit + 1);
    boolean more = found.size() > limit;
    var page = found.subList(0, Math.min(limit, found.size()));
    long next = page.isEmpty() ? before : page.getLast().getSequenceNumber();
    return readablePage(conversation, page, next, more);
  }

  @Override
  public MessagePage updates(String instanceId, long after, List<String> messageIds) {
    access.require(instanceId, SilverpeasRole.WRITER);
    if (after < 0 || messageIds.size() > 100) {
      throw new IllegalArgumentException("Invalid update range");
    }
    Conversation conversation = conversations.current(instanceId, false);
    var selected = messages.selected(conversation.getId(), messageIds).stream()
        .filter(message -> message.getThreadRootId() == null).toList();
    var newer = messages.newer(conversation.getId(), after);
    return updatedPage(conversation, selected, newer);
  }

  private MessagePage updatedPage(Conversation conversation, List<DiscussionMessage> selected,
      List<DiscussionMessage> newer) {
    var page = java.util.stream.Stream.concat(selected.stream(), newer.stream())
        .collect(java.util.stream.Collectors.toMap(DiscussionMessage::getId, message -> message,
            (first, second) -> second)).values().stream()
        .sorted(java.util.Comparator.comparingLong(DiscussionMessage::getSequenceNumber).reversed())
        .toList();
    var readable = readablePage(conversation, page, 0, false);
    long cursor = newer.size() == 100 ? newer.getLast().getSequenceNumber() : conversation.getLastSequence();
    return new MessagePage(readable.messages(), 0, false, readable.lastSequence(), readable.unreadCount(), cursor);
  }

  private MessagePage readablePage(Conversation conversation, List<DiscussionMessage> page,
      long next, boolean more) {
    String userId = User.getCurrentRequester().getId();
    long lastRead = readStates.find(conversation.getId(), userId)
        .map(DiscussionReadState::getLastReadSequence).orElse(0L);
    var viewed = seen.seen(conversation.getId(), userId, page.stream().map(DiscussionMessage::getId).toList());
    page.forEach(message -> message.setUnread(!message.isDeleted()
        && message.getSequenceNumber() > lastRead && !userId.equals(message.getCreatorId())
        && !viewed.contains(message.getId())));
    var rootIds = page.stream().filter(message -> message.getThreadRootId() == null)
        .map(DiscussionMessage::getId).toList();
    var statistics = messages.threadStatistics(conversation.getId(), rootIds, userId, lastRead);
    page.forEach(message -> {
      long[] counts = statistics.getOrDefault(message.getId(), new long[2]);
      message.setThreadStatistics(counts[0], counts[1]);
    });
    return new MessagePage(page, next, more, conversation.getLastSequence(),
        messages.unread(conversation.getId(), userId, lastRead));
  }

  @Override
  public void publish(String instanceId, String body) {
    publish(instanceId, body, List.of());
  }

  @Override
  public void publish(String instanceId, String body, List<DiscussionImageUpload> images) {
    publish(instanceId, body, images, List.of());
  }

  @Override
  public void publish(String instanceId, String body, List<DiscussionImageUpload> images, List<DiscussionFileUpload> files) {
    post(instanceId, null, body, images, files);
  }

  @Override
  public void reply(String instanceId, String rootId, String body, List<DiscussionImageUpload> images,
      List<DiscussionFileUpload> files) {
    if (rootId == null || rootId.isBlank()) {
      throw new IllegalArgumentException("A thread root is required");
    }
    post(instanceId, rootId, body, images, files);
  }

  private void post(String instanceId, String rootId, String body, List<DiscussionImageUpload> images,
      List<DiscussionFileUpload> files) {
    access.require(instanceId, SilverpeasRole.WRITER);
    if (images == null || images.size() > DiscussionImageUpload.MAX_IMAGES || images.stream().anyMatch(java.util.Objects::isNull)) {
      throw new IllegalArgumentException("Too many images");
    }
    if (files == null || files.size() > DiscussionFileUpload.MAX_FILES || files.stream().anyMatch(java.util.Objects::isNull)) {
      throw new IllegalArgumentException("Too many attachments");
    }
    Conversation conversation = conversations.current(instanceId, true);
    if (conversation.isArchived()) {
      throw new IllegalArgumentException("This channel is archived");
    }
    var message = new DiscussionMessage(conversation.getId(), conversation.nextSequence(),
        mentions.normalize(instanceId, body == null ? "" : body), !images.isEmpty() || !files.isEmpty());
    if (rootId != null) {
      message.attachToThread(messageIn(conversation, rootId));
    }
    message.setFilesAttached(!files.isEmpty());
    message.createdBy(User.getCurrentRequester());
    messages.save(message);
    messages.flush();
    messageIndex.schedule(instanceId, message.getId());
    imageService.store(instanceId, message.getId(), images);
    imageService.storeFiles(instanceId, message.getId(), files);
    mentions.notifyNewMentions(instanceId, message, "");
    subscriptions.schedule(new org.silverpeas.components.discussion.notification.DiscussionSubscriptionEvent(
        instanceId, message, User.getCurrentRequester(),
        org.silverpeas.core.notification.user.client.constant.NotifAction.CREATE));
    changes.changed(instanceId);
    changes.published(instanceId, message.getId(), User.getCurrentRequester().getId(),
        User.getCurrentRequester().getDisplayedName(), message.getBody());
  }

  @Override
  public Map<String, List<DiscussionImage>> images(String instanceId, List<String> messageIds) {
    access.require(instanceId, SilverpeasRole.WRITER);
    if (messageIds.size() > 200) {
      throw new IllegalArgumentException("Too many message identifiers");
    }
    var conversation = conversations.current(instanceId, false);
    var result = new java.util.HashMap<String, List<DiscussionImage>>();
    messages.selected(conversation.getId(), messageIds).stream().filter(message -> !message.isDeleted())
        .forEach(message -> result.put(message.getId(), imageService.list(instanceId, message.getId())));
    return result;
  }

  @Override
  public Map<String, List<DiscussionFile>> files(String instanceId, List<String> messageIds) {
    access.require(instanceId, SilverpeasRole.WRITER);
    if (messageIds.size() > 200) {
      throw new IllegalArgumentException("Too many message identifiers");
    }
    var conversation = conversations.current(instanceId, false);
    var result = new java.util.HashMap<String, List<DiscussionFile>>();
    messages.selected(conversation.getId(), messageIds).stream()
        .filter(message -> !message.isDeleted() && message.isFilesAttached())
        .forEach(message -> result.put(message.getId(), imageService.files(instanceId, message.getId())));
    return result;
  }

  @Override
  public List<DiscussionFile> sharedFiles(String instanceId) {
    access.require(instanceId, SilverpeasRole.WRITER);
    var conversation = conversations.current(instanceId, false);
    return messages.fileMessageIds(conversation.getId()).stream()
        .flatMap(id -> imageService.files(instanceId, id).stream()).toList();
  }

  @Override
  public void edit(String instanceId, String messageId, long version, String body) {
    DiscussionMessage message = editable(instanceId, messageId, version);
    String previousBody = message.getBody();
    message.edit(mentions.normalize(instanceId, body), message.isFilesAttached() || !imageService.list(instanceId, messageId).isEmpty());
    message.updatedBy(User.getCurrentRequester());
    messages.save(message);
    messageIndex.schedule(instanceId, message.getId());
    mentions.notifyNewMentions(instanceId, message, previousBody);
    if (!java.util.Objects.equals(previousBody, message.getBody())) {
      subscriptions.schedule(new org.silverpeas.components.discussion.notification.DiscussionSubscriptionEvent(
          instanceId, message, User.getCurrentRequester(),
          org.silverpeas.core.notification.user.client.constant.NotifAction.UPDATE));
    }
    changes.changed(instanceId);
  }

  @Override
  public void delete(String instanceId, String messageId, long version) {
    delete(instanceId, messageId, version, false);
  }

  @Override
  public DiscussionEventDeletion deletion(String instanceId, String messageId, long version) {
    return linkedEvents.describe(editable(instanceId, messageId, version).getBody());
  }

  @Override
  public void delete(String instanceId, String messageId, long version, boolean deleteEvent) {
    DiscussionMessage message = editable(instanceId, messageId, version);
    if (deleteEvent) { linkedEvents.delete(message.getBody()); }
    likes.deleteMessage(messageId);
    imageService.removeAfterCommit(instanceId, messageId);
    message.delete();
    message.updatedBy(User.getCurrentRequester());
    messages.save(message);
    messageIndex.schedule(instanceId, message.getId());
    changes.changed(instanceId);
  }

  @Override
  public Map<String, LikeState> likeStates(String instanceId, List<String> messageIds) {
    access.require(instanceId, SilverpeasRole.WRITER);
    if (messageIds.size() > 200) {
      throw new IllegalArgumentException("Too many message identifiers");
    }
    Conversation conversation = conversations.current(instanceId, false);
    return likes.states(conversation.getId(), messageIds, User.getCurrentRequester().getId());
  }

  @Override
  public List<PinnedMessage> pinnedMessages(String instanceId) {
    access.require(instanceId, SilverpeasRole.WRITER);
    return messages.pinned(conversations.current(instanceId, false).getId()).stream()
        .map(PinnedMessage::from).toList();
  }

  @Override
  public DiscussionMessage setPinned(String instanceId, String messageId, boolean pinned) {
    access.require(instanceId, SilverpeasRole.WRITER);
    var conversation = conversations.current(instanceId, true);
    var message = messageIn(conversation, messageId);
    if (message.isDeleted()) {
      throw new IllegalArgumentException("Deleted message");
    }
    if (message.isPinned() != pinned) {
      message.setPinned(pinned);
      messages.save(message);
      messages.flush();
      changes.changed(instanceId);
    }
    return message;
  }

  @Override
  public LikeState setLike(String instanceId, String messageId, boolean liked) {
    access.require(instanceId, SilverpeasRole.WRITER);
    User user = User.getCurrentRequester();
    if (user.isAnonymous() || user.isAccessGuest()) {
      throw new SecurityException("A user account is required to react");
    }
    Conversation conversation = conversations.current(instanceId, true);
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
    if (liked != previous.isPresent()) {
      changes.changed(instanceId);
    }
    return likes.states(conversation.getId(), List.of(messageId), user.getId())
        .getOrDefault(messageId, new LikeState(0, false));
  }

  @Override
  public MessagePage historyAround(String instanceId, String messageId) {
    access.require(instanceId, SilverpeasRole.WRITER);
    Conversation conversation = conversations.current(instanceId, false);
    DiscussionMessage message = messageIn(conversation, messageId);
    return message.getThreadRootId() == null
        ? history(instanceId, message.getSequenceNumber() + 1, 30)
        : threadHistory(instanceId, message.getThreadRootId(), message.getSequenceNumber() + 1, 30);
  }

  @Override
  public DiscussionMessage threadRoot(String instanceId, String messageId) {
    access.require(instanceId, SilverpeasRole.WRITER);
    var conversation = conversations.current(instanceId, false);
    var message = messageIn(conversation, messageId);
    return message.getThreadRootId() == null ? message : messageIn(conversation, message.getThreadRootId());
  }

  private DiscussionMessage requireRoot(Conversation conversation, String rootId) {
    var root = messageIn(conversation, rootId);
    if (root.getThreadRootId() != null) {
      throw new IllegalArgumentException("Nested discussion threads are not supported");
    }
    return root;
  }

  @Override
  public MessagePage threadHistory(String instanceId, String rootId, long before, int limit) {
    access.require(instanceId, SilverpeasRole.WRITER);
    if (before <= 0 || limit < 1 || limit > 100) {
      throw new IllegalArgumentException("Invalid thread history page");
    }
    var conversation = conversations.current(instanceId, false);
    requireRoot(conversation, rootId);
    var found = messages.threadHistory(conversation.getId(), rootId, before, limit + 1);
    boolean more = found.size() > limit;
    var page = found.subList(0, Math.min(limit, found.size()));
    return readablePage(conversation, page, page.isEmpty() ? before : page.getLast().getSequenceNumber(), more);
  }

  @Override
  public MessagePage threadUpdates(String instanceId, String rootId, long after, List<String> messageIds) {
    access.require(instanceId, SilverpeasRole.WRITER);
    if (after < 0 || messageIds.size() > 100) {
      throw new IllegalArgumentException("Invalid thread update range");
    }
    var conversation = conversations.current(instanceId, false);
    requireRoot(conversation, rootId);
    var selected = messages.selected(conversation.getId(), messageIds).stream()
        .filter(message -> rootId.equals(message.getId()) || rootId.equals(message.getThreadRootId())).toList();
    return updatedPage(conversation, selected, messages.threadNewer(conversation.getId(), rootId, after));
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
    Conversation conversation = conversations.current(instanceId, true);
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
  public long markVisible(String instanceId, List<String> messageIds) {
    access.require(instanceId, SilverpeasRole.WRITER);
    if (messageIds.isEmpty() || messageIds.size() > 100) {
      throw new IllegalArgumentException("Invalid visible message batch");
    }
    String userId = User.getCurrentRequester().getId();
    Conversation conversation = conversations.current(instanceId, true);
    var ids = messageIds.stream().distinct().toList();
    var selected = messages.selected(conversation.getId(), ids);
    if (selected.size() != ids.size()) {
      throw new IllegalArgumentException("Unknown discussion message");
    }
    long lastRead = readStates.find(conversation.getId(), userId)
        .map(DiscussionReadState::getLastReadSequence).orElse(0L);
    var alreadySeen = seen.seen(conversation.getId(), userId, ids);
    var newlySeen = selected.stream().filter(message -> !message.isDeleted()
        && !userId.equals(message.getCreatorId()) && message.getSequenceNumber() > lastRead
        && !alreadySeen.contains(message.getId())).toList();
    newlySeen.forEach(message -> {
      var receipt = new org.silverpeas.components.discussion.model.DiscussionSeen(
          conversation.getId(), message.getId(), userId);
      receipt.createdBy(User.getCurrentRequester());
      seen.save(receipt);
    });
    if (!newlySeen.isEmpty()) {
      seen.flush();
      changes.read(instanceId, userId);
    }
    return messages.unread(conversation.getId(), userId, lastRead);
  }

  @Override
  public void markRead(String instanceId, long sequence) {
    access.require(instanceId, SilverpeasRole.WRITER);
    User user = User.getCurrentRequester();
    if (user.isAccessGuest() || user.isAnonymous()) {
      return;
    }
    Conversation conversation = conversations.current(instanceId, true);
    DiscussionReadState state = readStates.find(conversation.getId(), user.getId())
        .orElseGet(() -> {
          var newState = new DiscussionReadState(conversation.getId(), user.getId());
          newState.createdBy(user);
          return newState;
        });
    state.advanceTo(sequence, conversation.getLastSequence());
    state.updatedBy(user);
    readStates.save(state);
    changes.read(instanceId, user.getId());
  }
}
