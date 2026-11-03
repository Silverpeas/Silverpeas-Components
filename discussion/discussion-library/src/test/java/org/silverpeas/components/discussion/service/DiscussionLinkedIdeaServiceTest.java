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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionCollection;
import org.silverpeas.components.suggestionbox.model.SuggestionBoxService;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import java.util.Optional;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.ContributionValidation;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionLinkedIdeaServiceTest {
  @TestManagedMock private SuggestionBoxService boxes;
  @TestManagedMock private ComponentAccessController componentAccess;
  @TestedBean private DiscussionLinkedIdeaService service;
  private Suggestion idea;
  private SuggestionBox box;
  private SuggestionCollection collection;
  private ContributionValidation validation;
  private User user;
  private String body;

  @BeforeEach
  void prepare() {
    idea = mock(Suggestion.class);
    box = mock(SuggestionBox.class);
    collection = mock(SuggestionCollection.class);
    validation = mock(ContributionValidation.class);
    user = mock(User.class);
    when(user.getId()).thenReturn("actor");
    when(idea.isDefined()).thenReturn(true);
    when(idea.getTitle()).thenReturn("Une idée");
    when(idea.getCreatorId()).thenReturn("actor");
    when(idea.getComponentInstanceId()).thenReturn("suggestionBox1");
    when(idea.getSuggestionBox()).thenReturn(box);
    when(idea.getValidation()).thenReturn(validation);
    when(box.getSuggestions()).thenReturn(collection);
    when(box.getHighestUserRole(user)).thenReturn(SilverpeasRole.WRITER);
    when(componentAccess.isUserAuthorized("actor", "suggestionBox1")).thenReturn(true);
    when(boxes.getContributionById(ContributionIdentifier.from("suggestionBox1", "idea1", "Suggestion"))).thenReturn(Optional.of(idea));
    body = link("suggestionBox1:Suggestion:idea1");
  }

  @Test
  void publishedAndPendingIdeasRequireBoxManagementEvenWhenTheAuthorDeletesTheirMessage() {
    try (var urls = mockStatic(URLUtil.class); var users = mockStatic(User.class)) {
      urls.when(URLUtil::getAbsoluteApplicationURL).thenReturn("https://silverpeas.test/silverpeas");
      users.when(User::getCurrentRequester).thenReturn(user);
      when(validation.isValidated()).thenReturn(true);
      assertEquals(new DiscussionMessageDeletion("idea", false, "Une idée", false), service.describe(body));
      assertThrows(SecurityException.class, () -> service.delete(body));
      when(validation.isValidated()).thenReturn(false);
      when(validation.isPendingValidation()).thenReturn(true);
      assertFalse(service.describe(body).canDeleteLinked());
      when(box.getHighestUserRole(user)).thenReturn(SilverpeasRole.PUBLISHER);
      assertFalse(service.describe(body).canDeleteLinked());
      when(box.getHighestUserRole(user)).thenReturn(SilverpeasRole.ADMIN);
      assertTrue(service.describe(body).canDeleteLinked());
      verify(collection, never()).remove(idea);
      service.delete(body);
      verify(collection).remove(idea);
    }
  }

  @Test
  void draftsAndRefusedIdeasFollowNativeCreatorOrManagerRulesAndRightsAreRechecked() {
    try (var urls = mockStatic(URLUtil.class); var users = mockStatic(User.class)) {
      urls.when(URLUtil::getAbsoluteApplicationURL).thenReturn("https://silverpeas.test/silverpeas");
      users.when(User::getCurrentRequester).thenReturn(user);
      when(validation.isInDraft()).thenReturn(true);
      assertTrue(service.describe(body).canDeleteLinked());
      when(idea.getCreatorId()).thenReturn("another");
      assertFalse(service.describe(body).canDeleteLinked());
      assertEquals("", service.describe(body).title());
      when(validation.isInDraft()).thenReturn(false);
      when(validation.isRefused()).thenReturn(true);
      when(user.isAccessAdmin()).thenReturn(true);
      assertTrue(service.describe(body).canDeleteLinked());
      when(componentAccess.isUserAuthorized("actor", "suggestionBox1")).thenReturn(false);
      assertThrows(SecurityException.class, () -> service.delete(body));
      verify(collection, never()).remove(idea);
    }
  }

  @Test
  void missingMismatchedForeignAndAmbiguousContributionsNeverDeleteAnIdea() {
    try (var urls = mockStatic(URLUtil.class); var users = mockStatic(User.class)) {
      urls.when(URLUtil::getAbsoluteApplicationURL).thenReturn("https://silverpeas.test/silverpeas");
      users.when(User::getCurrentRequester).thenReturn(user);
      assertEquals(DiscussionMessageDeletion.messageOnly(), service.describe(body.replace("silverpeas.test", "foreign.test")));
      assertEquals(DiscussionMessageDeletion.messageOnly(), service.describe(body + "\n" + link("almanach1:CalendarEvent:event1")));
      assertEquals(DiscussionMessageDeletion.messageOnly(), service.describe(body + "\n" + link("suggestionBox1:Suggestion:idea2")));
      when(idea.getComponentInstanceId()).thenReturn("suggestionBox2");
      assertEquals(DiscussionMessageDeletion.messageOnly(), service.describe(body));
      service.delete(body);
      when(boxes.getContributionById(ContributionIdentifier.from("suggestionBox1", "idea1", "Suggestion"))).thenReturn(Optional.empty());
      assertEquals(DiscussionMessageDeletion.messageOnly(), service.describe(body));
      assertDoesNotThrow(() -> service.delete(body));
      verify(collection, never()).remove(idea);
    }
  }

  private String link(String identifier) {
    return "https://silverpeas.test/silverpeas/Contribution/"
        + Base64.getEncoder().encodeToString(identifier.getBytes(StandardCharsets.UTF_8));
  }
}
