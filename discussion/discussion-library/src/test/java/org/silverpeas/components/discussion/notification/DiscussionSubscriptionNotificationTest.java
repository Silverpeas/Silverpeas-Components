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
package org.silverpeas.components.discussion.notification;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.components.discussion.model.DiscussionMessage;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationBehavior;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionSubscriptionNotificationTest {
  @TestedBean
  private DiscussionSubscriptionDispatcher dispatcher;
  @TestManagedMock
  private Event<DiscussionSubscriptionEvent> events;

  @Test
  void schedulingDoesNotSendBeforeCommit() {
    var dispatcher = new DiscussionSubscriptionDispatcher(events);
    var event = event(NotifAction.CREATE);
    try (var helper = mockStatic(UserNotificationHelper.class)) {
      dispatcher.schedule(event);
      verify(events).fire(event);
      helper.verifyNoInteractions();
      dispatcher.afterCommit(event);
      var builder = ArgumentCaptor.forClass(DiscussionSubscriptionNotification.class);
      helper.verify(() -> UserNotificationHelper.buildAndSend(builder.capture()));
      assertInstanceOf(UserSubscriptionNotificationBehavior.class, builder.getValue());
      assertEquals(NotifAction.CREATE, builder.getValue().getAction());
    }
  }

  @Test
  void subscriberUsersAndGroupsUseNativeComponentSubscriptions() {
    var user = mock(SubscriptionSubscriber.class);
    when(user.getId()).thenReturn("2");
    when(user.getType()).thenReturn(SubscriberType.USER);
    var group = mock(SubscriptionSubscriber.class);
    when(group.getId()).thenReturn("10");
    when(group.getType()).thenReturn(SubscriberType.GROUP);
    try (var provider = mockStatic(ResourceSubscriptionProvider.class)) {
      provider.when(() -> ResourceSubscriptionProvider.getSubscribersOfComponent("discussion1"))
          .thenReturn(new SubscriptionSubscriberList(List.of(user, group)));
      var builder = new DiscussionSubscriptionNotification(event(NotifAction.UPDATE));
      builder.initialize();
      assertEquals(List.of("2"), List.copyOf(builder.getUserIdsToNotify()));
      assertEquals(List.of("10"), List.copyOf(builder.getGroupIdsToNotify()));
      assertEquals("discussion.subscriptionUpdated", builder.getBundleSubjectKey());
      assertEquals("subscriptionUpdateNotification", builder.getTemplateFileName());
      assertEquals("1", builder.getSender());
      assertEquals("discussion1", builder.getComponentInstanceId());
    }
  }

  @Test
  void deliveryRunsOnlyAfterSuccessfulCommitWithFreshTransaction() throws Exception {
    var method = DiscussionSubscriptionDispatcher.class.getMethod("afterCommit", DiscussionSubscriptionEvent.class);
    assertEquals(Transactional.TxType.REQUIRES_NEW, method.getAnnotation(Transactional.class).value());
    assertEquals(TransactionPhase.AFTER_SUCCESS, method.getParameters()[0].getAnnotation(Observes.class).during());
  }

  private DiscussionSubscriptionEvent event(NotifAction action) {
    var sender = mock(User.class);
    when(sender.getId()).thenReturn("1");
    return new DiscussionSubscriptionEvent("discussion1", new DiscussionMessage("c", 1, "Hello"), sender, action);
  }
}
