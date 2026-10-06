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
package org.silverpeas.components.questionreply.service.notification;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.notification.user.NullUserNotification;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.client.GroupRecipient;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.subscription.ContributionSubscribersProvider;
import org.silverpeas.core.subscription.ResourceSubscriptionService;
import org.silverpeas.core.subscription.SubscriberDirective;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.service.GroupSubscriptionSubscriber;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.extension.LocalizationBundleStub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.COMPONENT;

/**
 * Unit tests on the recipients of the notification sent to the subscribers of a Questions/Replies
 * application when a public reply is given to a question. A question can be classified on the
 * PdC: the subscribers to a position on which it is classified are then notified about its
 * replies with the subscribers of the application.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class SubscriptionNotifierTest {

  private static final String COMPONENT_NAME = "questionReply";
  private static final String COMPONENT_ID = "questionReply7";
  private static final String QUESTION_ID = "23";
  private static final String AUTHOR_OF_THE_REPLY = "1";
  private static final String A_SUBSCRIBER = "3";
  private static final String ANOTHER_SUBSCRIBER = "5";
  private static final String A_SUBSCRIBED_GROUP = "12";
  private static final String A_SUBSCRIBER_ON_THE_PDC = "21";
  private static final String ANOTHER_SUBSCRIBER_ON_THE_PDC = "23";

  @RegisterExtension
  static LocalizationBundleStub questionReplyBundle = new LocalizationBundleStub(
      "org.silverpeas.questionReply.multilang.questionReplyBundle",
      LocalizationBundleStub.LANGUAGE_ALL);

  @TestManagedMock
  private ComponentAccessControl accessControl;
  @TestManagedMock
  private ResourceSubscriptionService subscriptionService;
  @TestManagedBean
  private SubscribersOnThePdcProvider pdc;

  private Map<String, ResourceSubscriptionService> subscriptionServicesByComponent;
  private final SubscriptionSubscriberList subscribers = new SubscriptionSubscriberList();

  @BeforeEach
  void setUpBundle() {
    questionReplyBundle.put("questionReply.subscription.title", "Answer to %1$s");
    questionReplyBundle.put("GML.st.notification.subject", "Notification");
    questionReplyBundle.put("questionReply.notifLinkLabel", "Go to the question");
  }

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpMocks(@TestManagedMock OrganizationController organizationController,
      @TestManagedMock SilverpeasComponentInstanceProvider componentInstanceProvider,
      @TestManagedMock UserProvider userProvider) throws Exception {
    final ComponentInstLight componentInstance = new ComponentInstLight();
    componentInstance.setLocalId(7);
    componentInstance.setName(COMPONENT_NAME);
    final Optional<SilverpeasComponentInstance> instance = Optional.of(componentInstance);
    when(organizationController.getComponentInstLight(COMPONENT_ID)).thenReturn(componentInstance);
    when(organizationController.getComponentInstance(COMPONENT_ID)).thenReturn(instance);
    when(componentInstanceProvider.getById(COMPONENT_ID)).thenReturn(instance);

    when(userProvider.getUser(anyString())).thenAnswer(invocation -> aUser(invocation.getArgument(0)));

    subscriptionServicesByComponent = (Map<String, ResourceSubscriptionService>) FieldUtils
        .readDeclaredStaticField(ResourceSubscriptionProvider.class, "componentImplementations",
            true);
    subscriptionServicesByComponent.put(COMPONENT_NAME, subscriptionService);
    when(subscriptionService.getSubscribersOfComponentAndTypedResource(eq(COMPONENT_ID),
        eq(COMPONENT), any(), any(SubscriberDirective[].class))).thenReturn(subscribers);

    when(accessControl.isUserAuthorized(anyString(), eq(COMPONENT_ID))).thenReturn(true);
    when(accessControl.isGroupAuthorized(anyString(), eq(COMPONENT_ID))).thenReturn(true);
  }

  @AfterEach
  void clear() {
    subscriptionServicesByComponent.clear();
  }

  @Test
  void theSubscribersOfTheApplicationAreNotifiedAboutAPublicReply() {
    subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));
    subscribers.add(UserSubscriptionSubscriber.from(ANOTHER_SUBSCRIBER));
    subscribers.add(GroupSubscriptionSubscriber.from(A_SUBSCRIBED_GROUP));

    final UserNotification notification = aNotificationOfAReply().build();

    final NotificationMetaData metaData = notification.getNotificationMetaData();
    assertThat(metaData.getComponentId(), is(COMPONENT_ID));
    assertThat(metaData.getSender(), is(AUTHOR_OF_THE_REPLY));
    assertThat(usersIn(metaData.getUserRecipients()),
        containsInAnyOrder(A_SUBSCRIBER, ANOTHER_SUBSCRIBER));
    assertThat(groupsIn(metaData.getGroupRecipients()), contains(A_SUBSCRIBED_GROUP));
  }

  @Test
  void nothingIsNotifiedWhenTheApplicationHasNoSubscriber() {
    final UserNotification notification = aNotificationOfAReply().build();

    assertThat(notification, instanceOf(NullUserNotification.class));
  }

  @Test
  void theSubscribersOnThePdcAreNotifiedWithTheSubscribersOfTheApplication() {
    subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));
    pdc.subscribe(QUESTION_ID, A_SUBSCRIBER_ON_THE_PDC, ANOTHER_SUBSCRIBER_ON_THE_PDC);

    final UserNotification notification = aNotificationOfAReply().build();

    assertThat(usersIn(notification.getNotificationMetaData().getUserRecipients()),
        containsInAnyOrder(A_SUBSCRIBER, A_SUBSCRIBER_ON_THE_PDC, ANOTHER_SUBSCRIBER_ON_THE_PDC));
  }

  @Test
  void theSubscribersOnThePdcAreNotifiedEvenIfTheApplicationHasNoSubscriber() {
    pdc.subscribe(QUESTION_ID, A_SUBSCRIBER_ON_THE_PDC);

    final UserNotification notification = aNotificationOfAReply().build();

    assertThat(usersIn(notification.getNotificationMetaData().getUserRecipients()),
        contains(A_SUBSCRIBER_ON_THE_PDC));
  }

  @Test
  void aSubscriberOnThePdcWithoutAccessToTheApplicationIsNotNotified() {
    pdc.subscribe(QUESTION_ID, A_SUBSCRIBER_ON_THE_PDC, ANOTHER_SUBSCRIBER_ON_THE_PDC);
    when(accessControl.isUserAuthorized(ANOTHER_SUBSCRIBER_ON_THE_PDC, COMPONENT_ID)).thenReturn(
        false);

    final UserNotification notification = aNotificationOfAReply().build();

    assertThat(usersIn(notification.getNotificationMetaData().getUserRecipients()),
        contains(A_SUBSCRIBER_ON_THE_PDC));
  }

  private static SubscriptionNotifier aNotificationOfAReply() {
    final Question question = new Question("10", COMPONENT_ID);
    question.getPK().setId(QUESTION_ID);
    question.setTitle("A question");
    question.setContent("The content of a question");
    final Reply reply = mock(Reply.class);
    when(reply.getTitle()).thenReturn("A reply");
    when(reply.loadWysiwygContent()).thenReturn("The content of a reply");
    return new SubscriptionNotifier(aUser(AUTHOR_OF_THE_REPLY), question, reply);
  }

  private static User aUser(final String userId) {
    final UserDetail user = mock(UserDetail.class);
    when(user.getId()).thenReturn(userId);
    when(user.isActivatedState()).thenReturn(true);
    when(user.getDisplayedName()).thenReturn("User " + userId);
    return user;
  }

  private static List<String> usersIn(final Collection<UserRecipient> recipients) {
    return recipients.stream().map(UserRecipient::getUserId).toList();
  }

  private static List<String> groupsIn(final Collection<GroupRecipient> recipients) {
    return recipients.stream().map(GroupRecipient::getGroupId).toList();
  }

  /**
   * A provider of the users concerned by a contribution in another way than by a subscription to
   * a resource of the application, like the subscribers to a position on the PdC on which the
   * contribution is classified. As the PdC does, the contribution is identified by its local
   * identifier among the contributions of its application.
   */
  static class SubscribersOnThePdcProvider implements ContributionSubscribersProvider {

    private final List<SubscriptionSubscriber> subscribers = new ArrayList<>();
    private String classifiedContentId;

    void subscribe(final String classifiedContentId, final String... userIds) {
      this.classifiedContentId = classifiedContentId;
      List.of(userIds).forEach(u -> subscribers.add(UserSubscriptionSubscriber.from(u)));
    }

    @Override
    public SubscriptionSubscriberList getSubscribersOf(final ContributionIdentifier contribution) {
      return contribution.getComponentInstanceId().equals(COMPONENT_ID) &&
          contribution.getLocalId().equals(classifiedContentId) ?
          new SubscriptionSubscriberList(subscribers) : new SubscriptionSubscriberList();
    }
  }
}
