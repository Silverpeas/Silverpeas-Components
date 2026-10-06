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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.forums.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.MockedStatic;
import org.silverpeas.components.forums.model.ForumDetail;
import org.silverpeas.components.forums.model.ForumPK;
import org.silverpeas.components.forums.notification.ForumsForumSubscriptionUserNotification;
import org.silverpeas.components.forums.service.ForumService;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.notification.user.builder.UserNotificationBuilder;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.service.PdcClassificationService;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.extension.LocalizationBundleStub;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the notification of the subscribers at the creation of a forum. When the forum is
 * classified on the PdC at its creation, the subscribers on the PdC are notified about the
 * creation of the forum with the other subscribers and not about its classification: for doing,
 * the forum has to be classified, without any alert, before the subscribers are notified.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class ForumCreationSubscribersNotificationTest {

  private static final String COMPONENT_ID = "forums3";
  private static final int FORUM_ID = 23;
  private static final String CREATOR = "1";
  private static final String A_POSITION_ON_THE_PDC =
      "{\"positions\":[{\"values\":[{\"id\":\"/0/12/\",\"axisId\":3,\"treeId\":\"1\"}]}]}";
  private static final String CLASSIFICATION = "the forum is classified";
  private static final String NOTIFICATION = "the subscribers are notified";

  @RegisterExtension
  static LocalizationBundleStub forumsBundle = new LocalizationBundleStub(
      "org.silverpeas.forums.multilang.forumsBundle", LocalizationBundleStub.LANGUAGE_ALL);

  @TestManagedMock
  ForumService forumService;
  @TestManagedMock
  PdcClassificationService pdcClassificationService;

  private ForumsSessionController controller;
  private final List<String> events = new ArrayList<>();
  private final List<UserNotificationBuilder> notifications = new ArrayList<>();

  @BeforeEach
  void setUpTheController() {
    final MainSessionController mainController = mock(MainSessionController.class);
    when(mainController.getFavoriteLanguage()).thenReturn("fr");
    final ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentName()).thenReturn("forums");
    when(context.getCurrentComponentId()).thenReturn(COMPONENT_ID);
    when(context.getCurrentSpaceId()).thenReturn("WA1");
    controller = new ForumsSessionController(mainController, context);
  }

  @BeforeEach
  void setUpTheServices() {
    when(forumService.createForum(any(ForumPK.class), any(), any(), any(), anyInt(), any(),
        any())).thenAnswer(invocation -> {
      invocation.getArgument(0, ForumPK.class).setId(String.valueOf(FORUM_ID));
      return FORUM_ID;
    });
    when(forumService.getForumDetail(any(ForumPK.class))).thenAnswer(
        invocation -> new ForumDetail(invocation.getArgument(0), "A forum", "", CREATOR,
            new Date()));
    doAnswer(invocation -> events.add(CLASSIFICATION)).when(pdcClassificationService)
        .classifyContent(any(Contribution.class), any(PdcClassification.class), anyBoolean());
  }

  @Test
  void theSubscribersAreNotifiedAboutTheCreationOfAForum() {
    createAForum();

    assertThat(events, contains(NOTIFICATION));
    assertThat(notifications.get(0).getClass().getName(),
        is(ForumsForumSubscriptionUserNotification.class.getName()));
    verify(pdcClassificationService, never()).classifyContent(any(), any(), anyBoolean());
  }

  @Test
  void aForumIsClassifiedOnThePdcBeforeItsSubscribersAreNotified() {
    controller.setForumPositions(A_POSITION_ON_THE_PDC);

    createAForum();

    assertThat(events, contains(CLASSIFICATION, NOTIFICATION));
  }

  /**
   * The subscribers on the PdC are notified about the creation of the forum by the notification
   * of the application: they mustn't be also alerted about its classification.
   */
  @Test
  void theClassificationOfAForumAtItsCreationDoesNotAlertTheSubscribersOnThePdc() {
    controller.setForumPositions(A_POSITION_ON_THE_PDC);

    createAForum();

    verify(pdcClassificationService).classifyContent(any(ForumDetail.class),
        any(PdcClassification.class), eq(false));
    verify(pdcClassificationService, never()).classifyContent(any(), any(), eq(true));
  }

  private void createAForum() {
    try (MockedStatic<UserNotificationHelper> helper = mockStatic(UserNotificationHelper.class)) {
      helper.when(() -> UserNotificationHelper.buildAndSend(any(UserNotificationBuilder.class)))
          .thenAnswer(invocation -> {
            events.add(NOTIFICATION);
            notifications.add(invocation.getArgument(0));
            return null;
          });
      controller.createForum("A forum", "", CREATOR, 0, null, "");
    }
  }
}
