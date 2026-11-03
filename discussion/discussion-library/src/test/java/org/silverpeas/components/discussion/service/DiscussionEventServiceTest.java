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

import jakarta.transaction.Transactional;
import java.time.ZoneId;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.components.discussion.model.Conversation;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.repository.CalendarRepository;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionEventServiceTest {
  private static final ZoneId ZONE = ZoneId.of("Europe/Paris");
  @TestManagedMock private DiscussionAccess access;
  @TestManagedMock private OrganizationController organization;
  @TestManagedMock private ComponentAccessController componentAccess;
  @TestManagedMock private CalendarRepository calendars;
  @TestManagedMock private ConversationRepository conversations;
  @TestManagedMock private ConversationService discussion;
  @TestManagedMock private DiscussionCalendarPlanner planner;
  @TestedBean private DiscussionEventService service;
  private User actor;
  private Calendar calendar;

  @BeforeEach
  void prepare() {
    actor = mock(User.class, RETURNS_DEEP_STUBS);
    when(actor.getId()).thenReturn("actor");
    when(actor.getUserPreferences().getLanguage()).thenReturn("fr");
    var conversation = mock(Conversation.class);
    when(conversations.current(eq("discussion1"), anyBoolean())).thenReturn(conversation);
    var origin = component("discussion1", "discussion", "space1");
    var almanach = component("almanach1", "almanach", "space1");
    var foreign = component("almanach2", "almanach", "space2");
    var reader = component("almanach3", "almanach", "space1");
    when(organization.getComponentInstLight("discussion1")).thenReturn(origin);
    when(organization.getComponentInstLight("almanach1")).thenReturn(almanach);
    when(organization.getComponentInstLight("almanach2")).thenReturn(foreign);
    when(organization.getComponentInstLight("almanach3")).thenReturn(reader);
    when(organization.getAllComponentIds("space1")).thenReturn(new String[]{"discussion1", "almanach1", "almanach2", "almanach3"});
    when(componentAccess.isUserAuthorized("actor", "almanach1")).thenReturn(true);
    when(componentAccess.isUserAuthorized("actor", "almanach3")).thenReturn(true);
    when(componentAccess.getUserRoles(eq("actor"), eq("almanach1"), any())).thenReturn(Set.of(SilverpeasRole.PUBLISHER));
    when(componentAccess.getUserRoles(eq("actor"), eq("almanach3"), any())).thenReturn(Set.of(SilverpeasRole.READER));
    calendar = mock(Calendar.class);
    when(calendar.getId()).thenReturn("calendar1");
    when(calendar.getComponentInstanceId()).thenReturn("almanach1");
    when(calendar.isMain()).thenReturn(true);
    when(calendars.getByComponentInstanceIds(anyCollection())).thenReturn(List.of(calendar));
    when(organization.<User>getAllUsers("discussion1")).thenReturn(new User[]{actor});
  }

  @Test
  void onlyTheWritableMainAlmanachInTheExactSameSpaceIsOffered() throws Exception {
    var imported = mock(Calendar.class);
    when(imported.isMain()).thenReturn(true);
    when(imported.getExternalCalendarUrl()).thenReturn(java.net.URI.create("https://external.test/calendar.ics").toURL());
    var secondary = mock(Calendar.class);
    when(calendars.getByComponentInstanceIds(anyCollection())).thenReturn(List.of(calendar, imported, secondary));
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      var options = service.options("discussion1", ZONE, "fr");
      assertEquals(List.of(new DiscussionEventOptions.CalendarChoice("calendar1", "almanach1")), options.calendars());
      assertEquals("Europe/Paris", options.defaults().zone());
      assertEquals(0, options.inviteCount());
      verify(calendars).getByComponentInstanceIds(Set.of("almanach1"));
      verify(componentAccess, never()).getUserRoles(eq("actor"), eq("almanach2"), any());
    }
  }

  @Test
  void creationInvitesAllAuthorizedMembersIncludingThoseWhoHaveNeverWrittenAndPublishesTheNativePermalink() {
    User member = member("member");
    User neverPosted = member("neverPosted");
    User removed = member("removed");
    when(removed.isRemovedState()).thenReturn(true);
    User guest = member("guest");
    when(guest.isAccessGuest()).thenReturn(true);
    User blocked = mock(User.class);
    when(blocked.getId()).thenReturn("blocked");
    when(organization.<User>getAllUsers("discussion1")).thenReturn(new User[]{actor, member, member, neverPosted, removed, guest, blocked});
    var event = event();
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      var input = input("calendar1");
      assertEquals(new DiscussionEventService.CreatedEvent("event1", "https://silverpeas.test/silverpeas/CalendarEvent/event1"),
          service.create("discussion1", "", input, ZONE));
      ArgumentCaptor<List<User>> attendees = ArgumentCaptor.captor();
      var period = ArgumentCaptor.forClass(Period.class);
      verify(planner).plan(eq(calendar), period.capture(), eq(input), attendees.capture());
      assertEquals(Set.of(member, neverPosted), Set.copyOf(attendees.getValue()));
      assertEquals(Instant.parse("2026-11-03T09:00:00Z"), Instant.from(period.getValue().getStartDate()));
      var body = ArgumentCaptor.forClass(String.class);
      verify(discussion).publish(eq("discussion1"), body.capture());
      assertTrue(body.getValue().contains("Réunion"));
      assertTrue(body.getValue().contains("Europe/Paris"));
      assertTrue(body.getValue().endsWith(event.getPermalink()));
      verify(discussion, never()).participantIds(anyString());
    }
  }

  @Test
  void threadEventsAreAnnouncedAsRepliesAndForeignRootsAreRejectedBeforeCreation() {
    var root = mock(org.silverpeas.components.discussion.model.DiscussionMessage.class);
    when(root.getId()).thenReturn("root");
    when(discussion.threadRoot("discussion1", "root")).thenReturn(root);
    event();
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      service.create("discussion1", "root", input("calendar1"), ZONE);
      verify(discussion).reply(eq("discussion1"), eq("root"), contains("CalendarEvent/event1"), eq(List.of()), eq(List.of()));
      verify(discussion, never()).publish(anyString(), anyString());
      when(root.getId()).thenReturn("differentRoot");
      clearInvocations(planner);
      assertThrows(IllegalArgumentException.class, () -> service.create("discussion1", "root", input("calendar1"), ZONE));
      verifyNoInteractions(planner);
    }
  }

  @Test
  void missingAlmanachForeignCalendarAndArchivedChannelsNeverCreateAnEvent() {
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      assertThrows(SecurityException.class, () -> service.create("discussion1", "", input("foreignCalendar"), ZONE));
      when(organization.getAllComponentIds("space1")).thenReturn(new String[0]);
      assertTrue(service.options("discussion1", ZONE, "fr").calendars().isEmpty());
      assertThrows(SecurityException.class, () -> service.create("discussion1", "", input("calendar1"), ZONE));
      when(conversations.current("discussion1", true).isArchived()).thenReturn(true);
      assertThrows(IllegalArgumentException.class, () -> service.create("discussion1", "", input("calendar1"), ZONE));
      verifyNoInteractions(planner);
    }
  }

  @Test
  void plannerAndAnnouncementShareARequiredTransactionAndFailuresPropagate() {
    assertEquals(Transactional.TxType.REQUIRED, DiscussionEventService.class.getAnnotation(Transactional.class).value());
    assertEquals(Transactional.TxType.MANDATORY, DiscussionCalendarPlanner.class.getAnnotation(Transactional.class).value());
    event();
    var failure = new IllegalStateException("Message could not be persisted");
    doThrow(failure).when(discussion).publish(eq("discussion1"), anyString());
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      assertSame(failure, assertThrows(IllegalStateException.class, () -> service.create("discussion1", "", input("calendar1"), ZONE)));
    }
  }

  @Test
  void datesAndTitlesAreValidatedIncludingDaylightSavingGaps() {
    assertThrows(IllegalArgumentException.class, () -> input("bad/calendar"));
    assertThrows(IllegalArgumentException.class, () -> new DiscussionEventInput("calendar1", " ", "", new DiscussionEventInput.Timing("", "")));
    assertThrows(IllegalArgumentException.class, () -> new DiscussionEventInput("calendar1", "x".repeat(161), "", new DiscussionEventInput.Timing("", "")));
    assertThrows(IllegalArgumentException.class, () -> new DiscussionEventInput.Timing("invalid", "invalid").in(ZONE));
    assertThrows(IllegalArgumentException.class, () -> new DiscussionEventInput.Timing("2026-11-03T11:00", "2026-11-03T10:00").in(ZONE));
    assertThrows(IllegalArgumentException.class, () -> new DiscussionEventInput.Timing("2027-03-28T02:30", "2027-03-28T04:00").in(ZONE));
  }

  @Test
  void revokedDiscussionAccessStopsBeforeLoadingCalendarsOrCreatingInvitations() {
    doThrow(SecurityException.class).when(access).require("discussion1", SilverpeasRole.WRITER);
    clearInvocations(organization, calendars, conversations);
    assertThrows(SecurityException.class, () -> service.options("discussion1", ZONE, "fr"));
    assertThrows(SecurityException.class, () -> service.create("discussion1", "", input("calendar1"), ZONE));
    verifyNoInteractions(organization, calendars, conversations, planner);
  }

  @Test
  void calendarFailureNeverPublishesAnAnnouncement() {
    var failure = new IllegalStateException("Calendar save failed");
    when(planner.plan(any(), any(), any(), anyList())).thenThrow(failure);
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      assertSame(failure, assertThrows(IllegalStateException.class, () -> service.create("discussion1", "", input("calendar1"), ZONE)));
      verify(discussion, never()).publish(anyString(), anyString());
    }
  }

  private DiscussionEventInput input(String calendarId) {
    return new DiscussionEventInput(calendarId, "Réunion", "Description", new DiscussionEventInput.Timing("2026-11-03T10:00", "2026-11-03T11:00"));
  }

  private CalendarEvent event() {
    var event = mock(CalendarEvent.class);
    when(event.getId()).thenReturn("event1");
    when(event.getPermalink()).thenReturn("https://silverpeas.test/silverpeas/CalendarEvent/event1");
    when(planner.plan(any(), any(), any(), anyList())).thenReturn(event);
    return event;
  }

  private User member(String id) {
    var user = mock(User.class);
    when(user.getId()).thenReturn(id);
    when(componentAccess.isUserAuthorized(id, "discussion1")).thenReturn(true);
    return user;
  }

  private ComponentInstLight component(String id, String name, String space) {
    var component = mock(ComponentInstLight.class);
    when(component.getId()).thenReturn(id);
    when(component.getName()).thenReturn(name);
    when(component.getSpaceId()).thenReturn(space);
    when(component.getLabel("fr")).thenReturn(id);
    return component;
  }
}
