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
package org.silverpeas.components.discussion.web;

import java.util.List;
import java.util.stream.Stream;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.components.discussion.repository.DiscussionMessageRepository;
import org.silverpeas.components.discussion.model.Conversation;
import org.silverpeas.components.discussion.service.DiscussionAccess;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionPublicationServiceTest {
  @TestManagedMock
  private DiscussionAccess access;
  @TestManagedMock
  private ConversationRepository conversations;
  @TestManagedMock
  private DiscussionMessageRepository messages;
  @TestManagedMock
  private PublicationService publications;
  @TestManagedMock
  private PublicationAccessControl publicationAccess;
  @Inject
  private UserProvider users;
  @TestedBean
  private DiscussionPublicationService service;

  @Test
  void listsDistinctAuthorizedPublicationsWithLocalizedTitlesFromTheWholeConversation() {
    var conversation = mock(Conversation.class);
    when(conversation.getId()).thenReturn("conversation");
    when(conversations.main("discussion1", false)).thenReturn(conversation);
    when(messages.publicationLinkBodies("conversation")).thenReturn(Stream.of(
        "https://silverpeas.test/silverpeas/Publication/12 /silverpeas/Publication/12",
        "/silverpeas/Publication/34 /silverpeas/Publication/56 /silverpeas/Publication/99"));
    var user = mock(User.class);
    when(user.getId()).thenReturn("user");
    when(users.getCurrentRequester()).thenReturn(user);
    var allowed = publication("12", "Projet IA");
    var second = publication("34", "Architecture");
    var forbidden = publication("56", "Confidentiel");
    when(publications.getByIds(List.of("12", "34", "56", "99"))).thenReturn(List.of(allowed, second, forbidden));
    when(publicationAccess.isUserAuthorized("user", allowed)).thenReturn(true);
    when(publicationAccess.isUserAuthorized("user", second)).thenReturn(true);
    try (var urls = mockStatic(URLUtil.class)) {
      urls.when(URLUtil::getAbsoluteApplicationURL).thenReturn("https://silverpeas.test/silverpeas");
      urls.when(() -> URLUtil.getSimpleURL(anyInt(), anyString()))
          .thenAnswer(call -> "/silverpeas/Publication/" + call.getArgument(1));
      var result = service.list("discussion1", "fr");
      assertEquals(List.of("34", "12"), result.stream().map(DiscussionPublication::id).toList());
      assertEquals("Architecture", result.getFirst().title());
      assertEquals("/silverpeas/Publication/12", result.getLast().url());
      verify(access).require("discussion1", SilverpeasRole.WRITER);
    }
  }

  @Test
  void deniedConversationAccessNeverQueriesReferencesOrPublicationMetadata() {
    when(access.require("discussion1", SilverpeasRole.WRITER)).thenThrow(SecurityException.class);
    assertThrows(SecurityException.class, () -> service.list("discussion1", "fr"));
    verifyNoInteractions(conversations, messages, publications, publicationAccess);
  }

  @Test
  void resolvesLongReferenceListsInBoundedBatches() {
    var conversation = mock(Conversation.class);
    when(conversation.getId()).thenReturn("conversation");
    when(conversations.main("discussion1", false)).thenReturn(conversation);
    when(messages.publicationLinkBodies("conversation")).thenReturn(
        java.util.stream.IntStream.rangeClosed(1, 205).mapToObj(id -> "/silverpeas/Publication/" + id));
    var user = mock(User.class);
    when(user.getId()).thenReturn("user");
    when(users.getCurrentRequester()).thenReturn(user);
    try (var urls = mockStatic(URLUtil.class)) {
      urls.when(URLUtil::getAbsoluteApplicationURL).thenReturn("https://silverpeas.test/silverpeas");
      assertTrue(service.list("discussion1", "fr").isEmpty());
      var batches = org.mockito.ArgumentCaptor.forClass(java.util.Collection.class);
      verify(publications, times(3)).getByIds(batches.capture());
      assertEquals(List.of(100, 100, 5), batches.getAllValues().stream().map(java.util.Collection::size).toList());
    }
  }

  private PublicationDetail publication(String id, String title) {
    var publication = mock(PublicationDetail.class);
    when(publication.getId()).thenReturn(id);
    when(publication.getName("fr")).thenReturn(title);
    return publication;
  }
}
