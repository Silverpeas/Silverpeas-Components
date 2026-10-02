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
package org.silverpeas.components.blog.notification;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.components.blog.model.PostDetail;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.notification.user.NullUserNotification;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationSendingHandler;
import org.silverpeas.core.notification.user.client.GroupRecipient;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
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
import java.util.Date;
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
 * Unit tests on the recipients of the notifications sent to the subscribers of a blog when a post
 * is published or updated. The subscribers to a position on the PdC on which the post is
 * classified are notified with them.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class BlogUserSubscriptionNotificationTest {

  private static final String COMPONENT_NAME = "blog";
  private static final String COMPONENT_ID = "blog7";
  private static final String POST_ID = "23";
  private static final String AUTHOR = "1";
  private static final String A_SUBSCRIBER = "3";
  private static final String ANOTHER_SUBSCRIBER = "5";
  private static final String A_SUBSCRIBED_GROUP = "12";
  private static final String A_SUBSCRIBER_ON_THE_PDC = "21";
  private static final String ANOTHER_SUBSCRIBER_ON_THE_PDC = "23";

  @RegisterExtension
  static LocalizationBundleStub blogBundle = new LocalizationBundleStub(
      "org.silverpeas.blog.multilang.blogBundle", LocalizationBundleStub.LANGUAGE_ALL);

  @TestManagedMock
  private ComponentAccessControl accessControl;
  @TestManagedMock
  private ResourceSubscriptionService subscriptionService;
  @TestManagedMock
  private Administration administration;
  @TestManagedBean
  private UserSubscriptionNotificationSendingHandler sendingHandler;
  @TestManagedBean
  private SubscribersOnThePdcProvider pdc;

  private Map<String, ResourceSubscriptionService> subscriptionServicesByComponent;
  private final SubscriptionSubscriberList subscribers = new SubscriptionSubscriberList();

  @BeforeEach
  void setUpBundle() {
    blogBundle.put("blog.subjectSubscription", "Subscription");
    blogBundle.put("GML.st.notification.subject", "Notification");
    blogBundle.put("blog.notifPostLinkLabel", "Go to the post");
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

    when(userProvider.getUser(anyString())).thenAnswer(invocation -> {
      final String userId = invocation.getArgument(0);
      final UserDetail user = mock(UserDetail.class);
      when(user.getId()).thenReturn(userId);
      when(user.isActivatedState()).thenReturn(true);
      when(user.getDisplayedName()).thenReturn("User " + userId);
      return user;
    });

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
  void theSubscribersOfTheBlogAreNotifiedAboutAPublishedPost() {
    subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));
    subscribers.add(UserSubscriptionSubscriber.from(ANOTHER_SUBSCRIBER));
    subscribers.add(GroupSubscriptionSubscriber.from(A_SUBSCRIBED_GROUP));

    final UserNotification notification =
        new BlogUserSubscriptionNotification(aPost(), null, "create", AUTHOR).build();

    final NotificationMetaData metaData = notification.getNotificationMetaData();
    assertThat(metaData.getAction(), is(NotifAction.CREATE));
    assertThat(metaData.getComponentId(), is(COMPONENT_ID));
    assertThat(metaData.isSendImmediately(), is(false));
    assertThat(usersIn(metaData.getUserRecipients()),
        containsInAnyOrder(A_SUBSCRIBER, ANOTHER_SUBSCRIBER));
    assertThat(groupsIn(metaData.getGroupRecipients()), contains(A_SUBSCRIBED_GROUP));
  }

  @Test
  void theSubscribersOfTheBlogAreNotifiedAboutAnUpdatedPost() {
    subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));

    final UserNotification notification =
        new BlogUserSubscriptionNotification(aPost(), null, "update", AUTHOR).build();

    final NotificationMetaData metaData = notification.getNotificationMetaData();
    assertThat(metaData.getAction(), is(NotifAction.UPDATE));
    assertThat(usersIn(metaData.getUserRecipients()), contains(A_SUBSCRIBER));
  }

  @Test
  void theAuthorOfTheModificationIsExcludedFromTheRecipients() {
    subscribers.add(UserSubscriptionSubscriber.from(AUTHOR));
    subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));

    final UserNotification notification =
        new BlogUserSubscriptionNotification(aPost(), null, "create", AUTHOR).build();

    final NotificationMetaData metaData = notification.getNotificationMetaData();
    assertThat(metaData.getSender(), is(AUTHOR));
    assertThat(usersIn(metaData.getUserRecipientsToExclude()), contains(AUTHOR));
  }

  @Test
  void nothingIsNotifiedWhenTheBlogHasNoSubscriber() {
    final UserNotification notification =
        new BlogUserSubscriptionNotification(aPost(), null, "create", AUTHOR).build();

    assertThat(notification, instanceOf(NullUserNotification.class));
  }

  /**
   * The content classified on the PdC is the publication behind the post, and the post has the
   * identifier of its publication: the subscribers on the PdC are found without any peculiar
   * treatment.
   */
  @Test
  void theSubscribersOnThePdcAreNotifiedWithTheSubscribersOfTheBlog() {
    subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));
    pdc.subscribe(POST_ID, A_SUBSCRIBER_ON_THE_PDC, ANOTHER_SUBSCRIBER_ON_THE_PDC);

    final UserNotification notification =
        new BlogUserSubscriptionNotification(aPost(), null, "create", AUTHOR).build();

    final NotificationMetaData metaData = notification.getNotificationMetaData();
    assertThat(metaData.getAction(), is(NotifAction.CREATE));
    assertThat(usersIn(metaData.getUserRecipients()),
        containsInAnyOrder(A_SUBSCRIBER, A_SUBSCRIBER_ON_THE_PDC, ANOTHER_SUBSCRIBER_ON_THE_PDC));
  }

  @Test
  void theSubscribersOnThePdcAreNotifiedEvenIfTheBlogHasNoSubscriber() {
    pdc.subscribe(POST_ID, A_SUBSCRIBER_ON_THE_PDC);

    final UserNotification notification =
        new BlogUserSubscriptionNotification(aPost(), null, "create", AUTHOR).build();

    assertThat(usersIn(notification.getNotificationMetaData().getUserRecipients()),
        contains(A_SUBSCRIBER_ON_THE_PDC));
  }

  @Test
  void aSubscriberOnThePdcWithoutAccessToTheBlogIsNotNotified() {
    pdc.subscribe(POST_ID, A_SUBSCRIBER_ON_THE_PDC, ANOTHER_SUBSCRIBER_ON_THE_PDC);
    when(accessControl.isUserAuthorized(ANOTHER_SUBSCRIBER_ON_THE_PDC, COMPONENT_ID)).thenReturn(
        false);

    final UserNotification notification =
        new BlogUserSubscriptionNotification(aPost(), null, "create", AUTHOR).build();

    assertThat(usersIn(notification.getNotificationMetaData().getUserRecipients()),
        contains(A_SUBSCRIBER_ON_THE_PDC));
  }

  private static PostDetail aPost() {
    final PublicationDetail publication = PublicationDetail.builder()
        .setPk(new PublicationPK(POST_ID, COMPONENT_ID))
        .created(new Date(), AUTHOR)
        .setNameAndDescription("A post", "")
        .build();
    publication.setStatus(PublicationDetail.VALID_STATUS);
    return new PostDetail(publication, (String) null, new Date());
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
