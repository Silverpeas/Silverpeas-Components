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

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import org.silverpeas.components.discussion.model.DiscussionMessage;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.components.discussion.repository.DiscussionMessageRepository;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.util.URLUtil;

/** Native suggestion creation and announcement in the current channel or thread. */
@Service
@Transactional
public class DiscussionIdeaService {
  @Inject private DiscussionAccess access;
  @Inject private OrganizationController organization;
  @Inject private ComponentAccessController componentAccess;
  @Inject private ConversationRepository conversations;
  @Inject private DiscussionMessageRepository messages;
  @Inject private ConversationService discussion;
  @Inject private DiscussionIdeaPublisher publisher;

  public record Choice(String id, String label) {
    public String getId() { return id; }
    public String getLabel() { return label; }
  }
  public record Options(List<Choice> boxes, String body) {
    public List<Choice> getBoxes() { return boxes; }
    public String getBody() { return body; }
  }
  public record Created(String id, String url, boolean pending) {
    public String getId() { return id; }
    public String getUrl() { return url; }
    public boolean isPending() { return pending; }
  }

  public Options options(String instanceId, String sourceId, String language) {
    requireWritable(instanceId, false);
    var source = source(instanceId, sourceId);
    return new Options(targets(instanceId, language), source == null ? "" : DiscussionMentionService.displayText(source.getBody()));
  }

  public Created create(String instanceId, String rootId, DiscussionIdeaInput input) {
    requireWritable(instanceId, true);
    if (input == null) { throw new IllegalArgumentException("Missing idea input"); }
    source(instanceId, input.sourceId());
    if (rootId != null && !rootId.isBlank()
        && !rootId.equals(discussion.threadRoot(instanceId, rootId).getId())) {
      throw new IllegalArgumentException("Invalid discussion thread");
    }
    var user = User.getCurrentRequester();
    if (targets(instanceId, user.getUserPreferences().getLanguage()).stream()
        .noneMatch(box -> box.id().equals(input.boxId()))) {
      throw new SecurityException("No writable suggestion box in this space");
    }
    var suggestion = publisher.publish(input);
    String identifier = suggestion.getIdentifier().asString();
    String url = URLUtil.getAbsoluteApplicationURL() + "/Contribution/"
        + Base64.getEncoder().encodeToString(identifier.getBytes(StandardCharsets.UTF_8));
    String body = "💡 " + input.title() + "\n" + url;
    if (rootId == null || rootId.isBlank()) { discussion.publish(instanceId, body); }
    else { discussion.reply(instanceId, rootId, body, List.of(), List.of()); }
    return new Created(suggestion.getId(), url, suggestion.getValidation().isPendingValidation());
  }

  private void requireWritable(String instanceId, boolean lock) {
    access.require(instanceId, SilverpeasRole.WRITER);
    if (conversations.current(instanceId, lock).isArchived()) {
      throw new IllegalArgumentException("This channel is archived");
    }
  }

  private DiscussionMessage source(String instanceId, String id) {
    if (id == null || id.isBlank()) { return null; }
    var message = messages.getById(id);
    if (message == null || message.isDeleted()
        || !conversations.current(instanceId, false).getId().equals(message.getConversationId())) {
      throw new IllegalArgumentException("Unknown source message");
    }
    return message;
  }

  private List<Choice> targets(String instanceId, String language) {
    var origin = organization.getComponentInstLight(instanceId);
    if (origin == null || origin.getSpaceId() == null || origin.getSpaceId().isBlank()) { return List.of(); }
    var user = User.getCurrentRequester();
    return Arrays.stream(organization.getAllComponentIds(origin.getSpaceId()))
        .map(organization::getComponentInstLight).filter(java.util.Objects::nonNull)
        .filter(component -> "suggestionBox".equals(component.getName())
            && origin.getSpaceId().equals(component.getSpaceId()) && writable(user, component))
        .map(component -> new Choice(component.getId(), component.getLabel(language))).distinct()
        .sorted(Comparator.comparing(Choice::label, String.CASE_INSENSITIVE_ORDER)).toList();
  }

  private boolean writable(User user, ComponentInstLight component) {
    if (!componentAccess.isUserAuthorized(user.getId(), component.getId())) { return false; }
    var role = SilverpeasRole.getHighestFrom(componentAccess.getUserRoles(user.getId(), component.getId(), AccessControlContext.init()));
    return role != null && role.isGreaterThanOrEquals(SilverpeasRole.WRITER);
  }
}
