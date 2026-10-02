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
package org.silverpeas.components.kmelia.notification;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
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
import org.silverpeas.core.contribution.publication.subscription.PublicationAliasSubscriptionResource;
import org.silverpeas.core.contribution.publication.subscription.PublicationSubscriptionResource;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodePath;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.notification.user.NullUserNotification;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationSendingHandler;
import org.silverpeas.core.notification.user.client.GroupRecipient;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.security.authorization.NodeAccessControl;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the recipients of the notifications sent to the subscribers of a Kmelia
 * application when a publication is created, updated or published into a folder. The subscribers
 * to a position on the PdC on which the publication is classified are notified with those of its
 * main folder.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class KmeliaSubscriptionPublicationUserNotificationTest {

  private static final String COMPONENT_NAME = "kmelia";
  private static final String COMPONENT_ID = "kmelia42";
  private static final String PUBLICATION_ID = "23";
  private static final String AUTHOR = "1";
  private static final String A_SUBSCRIBER = "3";
  private static final String ANOTHER_SUBSCRIBER = "5";
  private static final String AN_ALIAS_SUBSCRIBER = "7";
  private static final String A_SUBSCRIBED_GROUP = "12";
  private static final String A_SUBSCRIBER_ON_THE_PDC = "21";
  private static final String ANOTHER_SUBSCRIBER_ON_THE_PDC = "23";
  private static final ContributionIdentifier PUBLICATION =
      ContributionIdentifier.from(COMPONENT_ID, PUBLICATION_ID,
          PublicationDetail.getResourceType());

  private static final NodePK FOLDER = new NodePK("4", COMPONENT_ID);
  private static final NodePK ALIAS_FOLDER = new NodePK("8", COMPONENT_ID);

  @RegisterExtension
  static LocalizationBundleStub kmeliaBundle = new LocalizationBundleStub(
      "org.silverpeas.kmelia.multilang.kmeliaBundle", LocalizationBundleStub.LANGUAGE_ALL);

  @TestManagedMock
  private NodeAccessControl accessControl;
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
  private final SubscriptionSubscriberList aliasSubscribers = new SubscriptionSubscriberList();

  @BeforeEach
  void setUpBundle() {
    kmeliaBundle.put("Subscription", "Subscription");
    kmeliaBundle.put("GML.st.notification.subject", "Notification");
    kmeliaBundle.put("kmelia.notifPublicationLinkLabel", "Go to the publication");
  }

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpMocks(@TestManagedMock OrganizationController organizationController,
      @TestManagedMock SilverpeasComponentInstanceProvider componentInstanceProvider,
      @TestManagedMock UserProvider userProvider, @TestManagedMock NodeService nodeService)
      throws Exception {
    final ComponentInstLight componentInstance = new ComponentInstLight();
    componentInstance.setLocalId(42);
    componentInstance.setName(COMPONENT_NAME);
    final Optional<SilverpeasComponentInstance> instance = Optional.of(componentInstance);
    when(organizationController.getComponentInstLight(COMPONENT_ID)).thenReturn(componentInstance);
    when(organizationController.getComponentInstance(COMPONENT_ID)).thenReturn(instance);
    when(organizationController.getPathToComponent(COMPONENT_ID)).thenReturn(List.of());
    when(componentInstanceProvider.getById(COMPONENT_ID)).thenReturn(instance);
    when(nodeService.getPath(any(NodePK.class))).thenAnswer(invocation -> new NodePath());

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
    when(subscriptionService.getSubscribersOfSubscriptionResource(
        any(PublicationSubscriptionResource.class), any(SubscriberDirective[].class))).thenReturn(
        subscribers);
    when(subscriptionService.getSubscribersOfSubscriptionResource(
        any(PublicationAliasSubscriptionResource.class),
        any(SubscriberDirective[].class))).thenReturn(aliasSubscribers);

    when(accessControl.isUserAuthorized(anyString(), any(NodePK.class))).thenReturn(true);
    when(accessControl.isGroupAuthorized(anyString(), any(NodePK.class))).thenReturn(true);
  }

  @AfterEach
  void clear() {
    subscriptionServicesByComponent.clear();
  }

  @Test
  void theSubscribersOfTheFolderAreNotifiedAboutACreatedPublication() {
    subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));
    subscribers.add(UserSubscriptionSubscriber.from(ANOTHER_SUBSCRIBER));
    subscribers.add(GroupSubscriptionSubscriber.from(A_SUBSCRIBED_GROUP));

    final UserNotification notification = new KmeliaSubscriptionPublicationUserNotification(FOLDER,
        aPublication(false), NotifAction.CREATE).build();

    final NotificationMetaData metaData = notification.getNotificationMetaData();
    assertThat(metaData.getAction(), is(NotifAction.CREATE));
    assertThat(metaData.getComponentId(), is(COMPONENT_ID));
    assertThat(metaData.isSendImmediately(), is(false));
    assertThat(usersIn(metaData.getUserRecipients()),
        containsInAnyOrder(A_SUBSCRIBER, ANOTHER_SUBSCRIBER));
    assertThat(groupsIn(metaData.getGroupRecipients()), contains(A_SUBSCRIBED_GROUP));
  }

  @Test
  void theSubscribersOfTheFolderAreNotifiedAboutAnUpdatedPublication() {
    subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));

    final UserNotification notification = new KmeliaSubscriptionPublicationUserNotification(FOLDER,
        aPublication(false), NotifAction.UPDATE).build();

    final NotificationMetaData metaData = notification.getNotificationMetaData();
    assertThat(metaData.getAction(), is(NotifAction.UPDATE));
    assertThat(usersIn(metaData.getUserRecipients()), contains(A_SUBSCRIBER));
  }

  @Test
  void theAuthorOfThePublicationIsExcludedFromTheRecipients() {
    subscribers.add(UserSubscriptionSubscriber.from(AUTHOR));
    subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));

    final UserNotification notification = new KmeliaSubscriptionPublicationUserNotification(FOLDER,
        aPublication(false), NotifAction.CREATE).build();

    final NotificationMetaData metaData = notification.getNotificationMetaData();
    assertThat(metaData.getSender(), is(AUTHOR));
    assertThat(usersIn(metaData.getUserRecipientsToExclude()), contains(AUTHOR));
  }

  @Test
  void aSubscriberWithoutAccessToTheFolderIsNotNotified() {
    subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));
    subscribers.add(UserSubscriptionSubscriber.from(ANOTHER_SUBSCRIBER));
    when(accessControl.isUserAuthorized(ANOTHER_SUBSCRIBER, FOLDER)).thenReturn(false);

    final UserNotification notification = new KmeliaSubscriptionPublicationUserNotification(FOLDER,
        aPublication(false), NotifAction.CREATE).build();

    assertThat(usersIn(notification.getNotificationMetaData().getUserRecipients()),
        contains(A_SUBSCRIBER));
  }

  @Test
  void nothingIsNotifiedWhenTheFolderHasNoSubscriber() {
    final UserNotification notification = new KmeliaSubscriptionPublicationUserNotification(FOLDER,
        aPublication(false), NotifAction.CREATE).build();

    assertThat(notification, instanceOf(NullUserNotification.class));
  }

  @Test
  void onlyTheSubscribersOfTheAliasFolderAreNotifiedAboutAnAliasOfThePublication() {
    subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));
    aliasSubscribers.add(UserSubscriptionSubscriber.from(AN_ALIAS_SUBSCRIBER));

    final UserNotification notification =
        new KmeliaSubscriptionPublicationUserNotification(ALIAS_FOLDER, aPublication(true),
            NotifAction.CREATE).build();

    assertThat(usersIn(notification.getNotificationMetaData().getUserRecipients()),
        contains(AN_ALIAS_SUBSCRIBER));
  }

  @Test
  void theSubscribersOnThePdcAreNotifiedWithTheSubscribersOfTheFolder() {
    subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));
    pdc.subscribe(PUBLICATION, A_SUBSCRIBER_ON_THE_PDC, ANOTHER_SUBSCRIBER_ON_THE_PDC);

    final UserNotification notification = new KmeliaSubscriptionPublicationUserNotification(FOLDER,
        aPublication(false), NotifAction.CREATE).build();

    final NotificationMetaData metaData = notification.getNotificationMetaData();
    assertThat(metaData.getAction(), is(NotifAction.CREATE));
    assertThat(usersIn(metaData.getUserRecipients()),
        containsInAnyOrder(A_SUBSCRIBER, A_SUBSCRIBER_ON_THE_PDC, ANOTHER_SUBSCRIBER_ON_THE_PDC));
  }

  @Test
  void theSubscribersOnThePdcAreNotifiedEvenIfTheFolderHasNoSubscriber() {
    pdc.subscribe(PUBLICATION, A_SUBSCRIBER_ON_THE_PDC);

    final UserNotification notification = new KmeliaSubscriptionPublicationUserNotification(FOLDER,
        aPublication(false), NotifAction.CREATE).build();

    assertThat(usersIn(notification.getNotificationMetaData().getUserRecipients()),
        contains(A_SUBSCRIBER_ON_THE_PDC));
  }

  @Test
  void aSubscriberOnThePdcWithoutAccessToTheFolderIsNotNotified() {
    pdc.subscribe(PUBLICATION, A_SUBSCRIBER_ON_THE_PDC, ANOTHER_SUBSCRIBER_ON_THE_PDC);
    when(accessControl.isUserAuthorized(ANOTHER_SUBSCRIBER_ON_THE_PDC, FOLDER)).thenReturn(false);

    final UserNotification notification = new KmeliaSubscriptionPublicationUserNotification(FOLDER,
        aPublication(false), NotifAction.CREATE).build();

    assertThat(usersIn(notification.getNotificationMetaData().getUserRecipients()),
        contains(A_SUBSCRIBER_ON_THE_PDC));
  }

  /**
   * A notification is sent for the main folder of the publication and for each of its aliases:
   * the subscribers on the PdC, already notified with those of the main folder, mustn't be
   * notified again with those of each alias.
   */
  @Test
  void theSubscribersOnThePdcAreNotNotifiedAboutAnAliasOfThePublication() {
    aliasSubscribers.add(UserSubscriptionSubscriber.from(AN_ALIAS_SUBSCRIBER));
    pdc.subscribe(PUBLICATION, A_SUBSCRIBER_ON_THE_PDC);

    final UserNotification notification =
        new KmeliaSubscriptionPublicationUserNotification(ALIAS_FOLDER, aPublication(true),
            NotifAction.CREATE).build();

    assertThat(usersIn(notification.getNotificationMetaData().getUserRecipients()),
        contains(AN_ALIAS_SUBSCRIBER));
  }

  private static PublicationDetail aPublication(final boolean alias) {
    final PublicationDetail publication = mock(PublicationDetail.class);
    when(publication.getPK()).thenReturn(new PublicationPK(PUBLICATION_ID, COMPONENT_ID));
    when(publication.getId()).thenReturn(PUBLICATION_ID);
    when(publication.getInstanceId()).thenReturn(COMPONENT_ID);
    when(publication.getContributionType()).thenReturn(PublicationDetail.getResourceType());
    when(publication.getIdentifier()).thenReturn(PUBLICATION);
    when(publication.isAlias()).thenReturn(alias);
    when(publication.getName(anyString())).thenReturn("A publication");
    when(publication.getDescription(anyString())).thenReturn("The description");
    when(publication.getKeywords(anyString())).thenReturn("");
    when(publication.getCreatorId()).thenReturn(AUTHOR);
    when(publication.getUpdaterId()).thenReturn(AUTHOR);
    return publication;
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
   * contribution is classified.
   */
  static class SubscribersOnThePdcProvider implements ContributionSubscribersProvider {

    private final List<SubscriptionSubscriber> subscribers = new ArrayList<>();
    private ContributionIdentifier classified;

    void subscribe(final ContributionIdentifier classified, final String... userIds) {
      this.classified = classified;
      List.of(userIds).forEach(u -> subscribers.add(UserSubscriptionSubscriber.from(u)));
    }

    @Override
    public SubscriptionSubscriberList getSubscribersOf(final ContributionIdentifier contribution) {
      return contribution.equals(classified) ? new SubscriptionSubscriberList(subscribers) :
          new SubscriptionSubscriberList();
    }
  }
}
