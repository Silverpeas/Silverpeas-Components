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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.repository.CalendarRepository;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.ComponentAccessController;

/** Creates an Almanach event and its announcement in the same platform transaction. */
@Service
@Transactional
public class DiscussionEventService {
  @Inject
  private DiscussionAccess access;
  @Inject
  private OrganizationController organization;
  @Inject
  private ComponentAccessController componentAccess;
  @Inject
  private CalendarRepository calendars;
  @Inject
  private ConversationRepository conversations;
  @Inject
  private ConversationService discussion;
  @Inject
  private DiscussionCalendarPlanner planner;

  private record Target(Calendar calendar, String label) { }

  public record CreatedEvent(String id, String url) {
    public String getId() { return id; }
    public String getUrl() { return url; }
  }

  public DiscussionEventOptions options(String instanceId, ZoneId zone, String language) {
    requireWritableChannel(instanceId, false);
    var choices = targets(instanceId, language).stream()
        .map(target -> new DiscussionEventOptions.CalendarChoice(target.calendar().getId(), target.label())).toList();
    var start = ZonedDateTime.now(zone).plusMinutes(15).withSecond(0).withNano(0);
    var defaults = new DiscussionEventOptions.Defaults(zone.getId(),
        start.toLocalDateTime().toString(), start.plusHours(1).toLocalDateTime().toString());
    return new DiscussionEventOptions(choices, invitees(instanceId).size(), defaults);
  }

  public CreatedEvent create(String instanceId, String rootId, DiscussionEventInput input, ZoneId zone) {
    requireWritableChannel(instanceId, true);
    if (input == null) { throw new IllegalArgumentException("Missing event input"); }
    if (rootId != null && !rootId.isBlank()
        && !rootId.equals(discussion.threadRoot(instanceId, rootId).getId())) {
      throw new IllegalArgumentException("Invalid discussion thread");
    }
    var period = input.timing().in(zone);
    var user = User.getCurrentRequester();
    String language = user.getUserPreferences().getLanguage();
    var calendar = targets(instanceId, language).stream()
        .filter(target -> input.calendarId().equals(target.calendar().getId())).findFirst()
        .orElseThrow(() -> new SecurityException("No writable Almanach in this space"))
        .calendar();
    var event = planner.plan(calendar, period, input, invitees(instanceId));
    String url = event.getPermalink();
    var formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(Locale.forLanguageTag(language));
    String body = "📅 " + input.title() + "\n"
        + formatter.format(java.time.LocalDateTime.parse(input.timing().start())) + " – "
        + formatter.format(java.time.LocalDateTime.parse(input.timing().end()))
        + " (" + zone.getId() + ")\n" + url;
    if (rootId == null || rootId.isBlank()) {
      discussion.publish(instanceId, body);
    } else {
      discussion.reply(instanceId, rootId, body, List.of(), List.of());
    }
    return new CreatedEvent(event.getId(), url);
  }

  private void requireWritableChannel(String instanceId, boolean lock) {
    access.require(instanceId, SilverpeasRole.WRITER);
    if (conversations.current(instanceId, lock).isArchived()) {
      throw new IllegalArgumentException("This channel is archived");
    }
  }

  private List<Target> targets(String instanceId, String language) {
    var origin = organization.getComponentInstLight(instanceId);
    if (origin == null || origin.getSpaceId() == null || origin.getSpaceId().isBlank()) { return List.of(); }
    var user = User.getCurrentRequester();
    Map<String, ComponentInstLight> allowed = Arrays.stream(organization.getAllComponentIds(origin.getSpaceId()))
        .map(organization::getComponentInstLight).filter(java.util.Objects::nonNull)
        .filter(component -> "almanach".equals(component.getName()) && origin.getSpaceId().equals(component.getSpaceId()))
        .filter(component -> canCreate(user, component.getId()))
        .collect(Collectors.toMap(ComponentInstLight::getId, Function.identity(), (first, second) -> first));
    if (allowed.isEmpty()) { return List.of(); }
    return calendars.getByComponentInstanceIds(allowed.keySet()).stream()
        .filter(calendar -> calendar.isMain() && calendar.getExternalCalendarUrl() == null
            && allowed.containsKey(calendar.getComponentInstanceId()))
        .map(calendar -> new Target(calendar, allowed.get(calendar.getComponentInstanceId()).getLabel(language)))
        .sorted(Comparator.comparing(Target::label, String.CASE_INSENSITIVE_ORDER)).toList();
  }

  private boolean canCreate(User user, String instanceId) {
    if (!componentAccess.isUserAuthorized(user.getId(), instanceId)) { return false; }
    var role = SilverpeasRole.getHighestFrom(componentAccess.getUserRoles(user.getId(), instanceId, AccessControlContext.init()));
    return role != null && role.isGreaterThanOrEquals(SilverpeasRole.PUBLISHER);
  }

  private List<User> invitees(String instanceId) {
    String organizer = User.getCurrentRequester().getId();
    return Arrays.stream(organization.<User>getAllUsers(instanceId))
        .filter(user -> !user.isAnonymous() && !user.isAccessGuest() && !user.isRemovedState() && !user.isDeletedState())
        .filter(user -> !organizer.equals(user.getId()) && componentAccess.isUserAuthorized(user.getId(), instanceId))
        .collect(Collectors.toMap(User::getId, Function.identity(), (first, second) -> first)).values().stream().toList();
  }
}
