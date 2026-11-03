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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.util.URLUtil;

/** Native single-question vote creation and announcement in the current channel or thread. */
@Service
@Transactional
public class DiscussionVoteService {
  @Inject private DiscussionAccess access;
  @Inject private OrganizationController organization;
  @Inject private ComponentAccessController componentAccess;
  @Inject private ConversationRepository conversations;
  @Inject private ConversationService discussion;
  @Inject private DiscussionVotePlanner planner;

  @Inject private DiscussionVoteRollbackDispatcher rollback;

  public record Choice(String id, String label, boolean forcedAnonymous) {
    public String getId() { return id; }
    public String getLabel() { return label; }
    public boolean isForcedAnonymous() { return forcedAnonymous; }
  }
  public record Created(String id, String url) {
    public String getId() { return id; }
    public String getUrl() { return url; }
  }

  public List<Choice> options(String instanceId, String language) {
    requireWritable(instanceId, false);
    return targets(instanceId, language);
  }

  public Created create(String instanceId, String rootId, DiscussionVoteInput input) {
    requireWritable(instanceId, true);
    if (input == null) { throw new IllegalArgumentException("Missing vote input"); }
    if (rootId != null && !rootId.isBlank()
        && !rootId.equals(discussion.threadRoot(instanceId, rootId).getId())) {
      throw new IllegalArgumentException("Invalid discussion thread");
    }
    var user = User.getCurrentRequester();
    var target = targets(instanceId, user.getUserPreferences().getLanguage()).stream()
        .filter(vote -> vote.id().equals(input.instanceId())).findFirst()
        .orElseThrow(() -> new SecurityException("No writable Vote in this space"));
    var origin = organization.getComponentInstLight(instanceId);
    var pk = planner.create(input, origin.getSpaceId(), target.forcedAnonymous());
    rollback.schedule(pk);
    String url = URLUtil.getAbsoluteApplicationURL() + "/RpollingStation/" + input.instanceId()
        + "/searchResult?Type=QuestionContainer&Id=" + pk.getId();
    String body = "📊 " + input.question() + "\n" + String.join(" · ", input.answers()) + "\n" + url;
    // Choices are bounded individually; the announcement keeps enough room for the native URL.
    if (body.length() > 2000) { body = "📊 " + input.question() + "\n" + url; }
    if (rootId == null || rootId.isBlank()) { discussion.publish(instanceId, body); }
    else { discussion.reply(instanceId, rootId, body, List.of(), List.of()); }
    return new Created(pk.getId(), url);
  }

  private void requireWritable(String instanceId, boolean lock) {
    access.require(instanceId, SilverpeasRole.WRITER);
    if (conversations.current(instanceId, lock).isArchived()) {
      throw new IllegalArgumentException("This channel is archived");
    }
  }

  private List<Choice> targets(String instanceId, String language) {
    var origin = organization.getComponentInstLight(instanceId);
    if (origin == null || origin.getSpaceId() == null || origin.getSpaceId().isBlank()) { return List.of(); }
    var user = User.getCurrentRequester();
    return Arrays.stream(organization.getAllComponentIds(origin.getSpaceId()))
        .map(organization::getComponentInstLight).filter(java.util.Objects::nonNull)
        .filter(component -> "pollingStation".equals(component.getName())
            && origin.getSpaceId().equals(component.getSpaceId()) && writable(user, component))
        .map(component -> new Choice(component.getId(), component.getLabel(language),
            "yes".equalsIgnoreCase(organization.getComponentParameterValue(component.getId(), "useAnonymousMode"))))
        .distinct().sorted(Comparator.comparing(Choice::label, String.CASE_INSENSITIVE_ORDER)).toList();
  }

  private boolean writable(User user, ComponentInstLight component) {
    if (!componentAccess.isUserAuthorized(user.getId(), component.getId())) { return false; }
    var role = SilverpeasRole.getHighestFrom(componentAccess.getUserRoles(user.getId(), component.getId(), AccessControlContext.init()));
    return role != null && role.isGreaterThanOrEquals(SilverpeasRole.PUBLISHER);
  }
}
