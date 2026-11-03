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

import java.net.URI;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.container.service.QuestionContainerService;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionLinkedVoteServiceTest {
  private static final String URL = "https://silverpeas.test/silverpeas/RpollingStation/pollingStation1/searchResult?Type=QuestionContainer&Id=123";
  @TestManagedMock private QuestionContainerService containers;
  @TestManagedMock private ComponentAccessController componentAccess;
  @TestedBean private DiscussionLinkedVoteService service;
  private User user;
  private QuestionContainerHeader header;
  private QuestionContainerPK pk;

  @BeforeEach
  void prepare() {
    user = mock(User.class);
    when(user.getId()).thenReturn("actor");
    header = mock(QuestionContainerHeader.class);
    pk = new QuestionContainerPK("123", "space1", "pollingStation1");
    when(header.getPK()).thenReturn(pk);
    when(header.getInstanceId()).thenReturn("pollingStation1");
    when(header.getTitle()).thenReturn("Quel jour ?");
    when(containers.getQuestionContainerHeader(any())).thenReturn(header);
    when(componentAccess.isUserAuthorized("actor", "pollingStation1")).thenReturn(true);
    when(componentAccess.getUserRoles(eq("actor"), eq("pollingStation1"), any())).thenReturn(Set.of(SilverpeasRole.PUBLISHER));
  }

  @Test
  void onlyNativeManagersCanDeleteAndRightsAreCheckedAgainAtDeletion() {
    try (var urls = mockStatic(URLUtil.class); var users = mockStatic(User.class)) {
      urls.when(URLUtil::getAbsoluteApplicationURL).thenReturn("https://silverpeas.test/silverpeas");
      users.when(User::getCurrentRequester).thenReturn(user);
      assertEquals(new DiscussionMessageDeletion("vote", true, "Quel jour ?", false), service.describe(URL));
      verify(containers, never()).deleteQuestionContainer(any());
      service.delete(URL);
      verify(containers).deleteQuestionContainer(pk);
      when(componentAccess.getUserRoles(eq("actor"), eq("pollingStation1"), any())).thenReturn(Set.of(SilverpeasRole.USER));
      assertFalse(service.describe(URL).canDeleteLinked());
      assertThrows(SecurityException.class, () -> service.delete(URL));
      when(componentAccess.isUserAuthorized("actor", "pollingStation1")).thenReturn(false);
      assertEquals("", service.describe(URL).title());
    }
  }

  @Test
  void mismatchedMissingAndSurveyHeadersNeverCauseDeletion() {
    try (var urls = mockStatic(URLUtil.class)) {
      urls.when(URLUtil::getAbsoluteApplicationURL).thenReturn("https://silverpeas.test/silverpeas");
      when(header.getInstanceId()).thenReturn("survey1");
      assertEquals(DiscussionMessageDeletion.messageOnly(), service.describe(URL));
      service.delete(URL);
      when(containers.getQuestionContainerHeader(any())).thenReturn(null);
      assertDoesNotThrow(() -> service.delete(URL));
      verify(containers, never()).deleteQuestionContainer(any());
    }
  }

  @Test
  void nativeVoteReferencesRejectForeignCredentialsOtherApplicationsAndAmbiguousMessages() {
    var application = URI.create("https://silverpeas.test/silverpeas");
    assertEquals("123", DiscussionContributionReference.find(URL, application).orElseThrow().getLocalId());
    assertTrue(DiscussionContributionReference.find(URL + "\n" + URL, application).isPresent());
    for (String invalid : new String[]{URL.replace("silverpeas.test", "foreign.test"), URL.replace("https://", "https://user:pass@"),
        URL.replace("/silverpeas/", "/other/"), URL.replace("pollingStation1", "survey1"), URL + "&unexpected=1", URL.replace("Id=123", "Id=invalid"),
        URL + "\n" + URL.replace("Id=123", "Id=456")}) {
      assertTrue(DiscussionContributionReference.find(invalid, application).isEmpty());
    }
  }
}
