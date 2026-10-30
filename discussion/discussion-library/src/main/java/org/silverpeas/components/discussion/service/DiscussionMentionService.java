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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import jakarta.inject.Inject;
import org.silverpeas.components.discussion.model.DiscussionMessage;
import org.silverpeas.components.discussion.notification.DiscussionMentionDispatcher;
import org.silverpeas.components.discussion.notification.DiscussionMentionEvent;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.authorization.ComponentAccessController;

/** Canonical mention identities, scoped suggestions and native notifications. */
@Service
public class DiscussionMentionService {
  private static final Pattern MENTION = Pattern.compile("@\\[([^]\\r\\n]{1,300})]\\(([0-9]{1,40})\\)");
  @Inject
  private OrganizationController organization;
  @Inject
  private ComponentAccessController componentAccess;
  @Inject
  private DiscussionAccess access;

  @Inject
  private DiscussionMentionDispatcher notifications;

  public List<MentionCandidate> candidates(String instanceId) {
    access.require(instanceId, SilverpeasRole.WRITER);
    return allowedUsers(instanceId).values().stream()
        .map(user -> new MentionCandidate(user.getId(), user.getDisplayedName()))
        .sorted(Comparator.comparing(MentionCandidate::name, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  public String normalize(String instanceId, String body) {
    if (body != null && body.length() > DiscussionMessage.MAX_BODY_LENGTH) {
      throw new IllegalArgumentException("Message is too long");
    }
    if (body == null || !MENTION.matcher(body).find()) {
      return body;
    }
    Map<String, User> allowed = allowedUsers(instanceId);
    return MENTION.matcher(body).replaceAll(match -> {
      User user = allowed.get(match.group(2));
      if (user == null) {
        throw new IllegalArgumentException("Mentioned user cannot access this discussion");
      }
      String name = user.getDisplayedName().replace('[', ' ').replace(']', ' ')
          .replace('\n', ' ').replace('\r', ' ');
      return Matcher.quoteReplacement("@[" + name + "](" + user.getId() + ")");
    });
  }

  public void notifyNewMentions(String instanceId, DiscussionMessage message, String previousBody) {
    User sender = User.getCurrentRequester();
    Set<String> recipients = ids(message.getBody());
    recipients.removeAll(ids(previousBody));
    recipients.remove(sender.getId());
    if (!recipients.isEmpty()) {
      recipients.retainAll(allowedUsers(instanceId).keySet());
      if (!recipients.isEmpty()) {
        notifications.schedule(new DiscussionMentionEvent(instanceId, message, sender, recipients));
      }
    }
  }

  private Set<String> ids(String body) {
    return MENTION.matcher(body == null ? "" : body).results()
        .map(match -> match.group(2)).collect(Collectors.toSet());
  }

  private Map<String, User> allowedUsers(String instanceId) {
    User[] users = organization.getAllUsers(instanceId);
    return Arrays.stream(users).filter(user -> !user.isRemovedState() && !user.isDeletedState()
        && !user.isAnonymous() && !user.isAccessGuest()
        && componentAccess.isUserAuthorized(user.getId(), instanceId))
        .collect(Collectors.toMap(User::getId, user -> user, (first, second) -> first));
  }
}
