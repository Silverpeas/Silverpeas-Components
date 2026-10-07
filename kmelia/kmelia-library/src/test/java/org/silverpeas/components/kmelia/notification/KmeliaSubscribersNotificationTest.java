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
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.silverpeas.components.kmelia.service.DefaultKmeliaService;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.notification.user.builder.UserNotificationBuilder;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.reminder.Reminder;
import org.silverpeas.core.subscription.ResourceSubscriptionService;
import org.silverpeas.core.subscription.SubscriberDirective;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.util.Pair;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.silverpeas.components.kmelia.notification.KmeliaDelayedVisibilityUserNotificationReminder.KMELIA_DELAYED_VISIBILITY_USER_NOTIFICATION;

/**
 * Unit tests on the notifications the Kmelia service asks to send to the subscribers about a
 * publication: one notification by location of the publication (its main folder and each of its
 * aliases), the subscribers to a position on the PdC on which the publication is classified being
 * notified with those of the main folder.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class KmeliaSubscribersNotificationTest {

  private static final String COMPONENT_NAME = "kmelia";
  private static final String COMPONENT_ID = "kmelia42";
  private static final String AUTHOR = "1";
  private static final PublicationPK PUBLICATION_PK = new PublicationPK("23", COMPONENT_ID);
  private static final ContributionIdentifier PUBLICATION =
      ContributionIdentifier.from(COMPONENT_ID, "23", PublicationDetail.getResourceType());
  private static final String MAIN_FOLDER = "4";
  private static final String AN_ALIAS_FOLDER = "8";
  private static final String ANOTHER_ALIAS_FOLDER = "9";

  @TestManagedMock
  PublicationService publicationService;
  @TestManagedMock
  PdcManager pdcManager;
  @TestManagedMock
  KmeliaDelayedVisibilityUserNotificationReminder reminder;
  @TestManagedMock
  ResourceSubscriptionService subscriptionService;
  @TestedBean
  DefaultKmeliaService service;

  private Map<String, ResourceSubscriptionService> subscriptionServicesByComponent;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpTheSubscriptionsOfTheApplication(
      @TestManagedMock OrganizationController organizationController) throws Exception {
    final ComponentInstLight componentInstance = new ComponentInstLight();
    componentInstance.setLocalId(42);
    componentInstance.setName(COMPONENT_NAME);
    when(organizationController.getComponentInstance(COMPONENT_ID)).thenReturn(
        Optional.of(componentInstance));
    subscriptionServicesByComponent = (Map<String, ResourceSubscriptionService>) FieldUtils
        .readDeclaredStaticField(ResourceSubscriptionProvider.class, "componentImplementations",
            true);
    subscriptionServicesByComponent.put(COMPONENT_NAME, subscriptionService);
    when(subscriptionService.getSubscribersOfSubscriptionResource(any(SubscriptionResource.class),
        any(SubscriberDirective[].class))).thenReturn(new SubscriptionSubscriberList());
  }

  @BeforeEach
  void setUpUsers(@TestManagedMock UserProvider userProvider) {
    final User author = mock(User.class);
    when(author.getId()).thenReturn(AUTHOR);
    when(author.getDisplayedName()).thenReturn("The author");
    when(userProvider.getUser(AUTHOR)).thenReturn(author);
  }

  @BeforeEach
  @AfterEach
  void clearRequestContext() {
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
  }

  @AfterEach
  void clearTheSubscriptionsOfTheApplication() {
    subscriptionServicesByComponent.clear();
  }

  @Test
  void theSubscribersOfEachLocationOfAPublicationBecomingVisibleAreNotified() {
    aPublication(PublicationDetail.VALID_STATUS).in(MAIN_FOLDER)
        .withAliasesIn(AN_ALIAS_FOLDER, ANOTHER_ALIAS_FOLDER);

    final List<KmeliaSubscriptionPublicationUserNotification> notifications =
        notificationsSentBy(() -> service.performReminder(aReminderOfVisibility()));

    assertThat(notifications, hasSize(3));
    assertThat(notifications.stream().map(n -> n.getNodePK().getId()).toList(),
        contains(MAIN_FOLDER, AN_ALIAS_FOLDER, ANOTHER_ALIAS_FOLDER));
    notifications.forEach(n -> assertThat(n.getAction(), is(NotifAction.PUBLISHED)));
  }

  /**
   * The subscribers on the PdC are notified only once: with the subscribers of the main folder.
   */
  @Test
  void onlyTheNotificationAboutTheMainFolderConcernsTheSubscribersOnThePdc() {
    aPublication(PublicationDetail.VALID_STATUS).in(MAIN_FOLDER).withAliasesIn(AN_ALIAS_FOLDER);

    final List<KmeliaSubscriptionPublicationUserNotification> notifications =
        notificationsSentBy(() -> service.performReminder(aReminderOfVisibility()));

    assertThat(notifications, hasSize(2));
    assertThat(notifications.get(0)
        .getSubscribedContribution()
        .map(ContributionIdentifier::asString), is(Optional.of(PUBLICATION.asString())));
    assertThat(notifications.get(1).getSubscribedContribution().isPresent(), is(false));
  }

  /**
   * The subscribers on the PdC are notified with the other subscribers by the notification of the
   * application: the service has no more to ask the PdC for notifying them.
   */
  @Test
  void theServiceDoesNotAskThePdcForNotifyingItsSubscribers() throws Exception {
    aPublication(PublicationDetail.VALID_STATUS).in(MAIN_FOLDER).withAliasesIn(AN_ALIAS_FOLDER);

    notificationsSentBy(() -> service.performReminder(aReminderOfVisibility()));

    verify(pdcManager, never()).getPositions(anyInt(), anyString());
  }

  @Test
  void onlyTheSubscribersOfTheAliasesAreNotifiedWhenAliasesAreSetToAPublication() {
    final PublicationInFolders publication =
        aPublication(PublicationDetail.VALID_STATUS).in(MAIN_FOLDER);
    final List<Location> newAliases = List.of(anAliasIn(AN_ALIAS_FOLDER));
    when(publicationService.setAliases(PUBLICATION_PK, newAliases)).thenReturn(
        Pair.of(newAliases, List.of()));

    final List<KmeliaSubscriptionPublicationUserNotification> notifications =
        notificationsSentBy(() -> service.setAliases(PUBLICATION_PK, newAliases));

    assertThat(notifications, hasSize(1));
    assertThat(notifications.getFirst().getNodePK().getId(), is(AN_ALIAS_FOLDER));
    assertThat(notifications.getFirst().getAction(), is(NotifAction.PUBLISHED));
    assertThat(notifications.getFirst().getSubscribedContribution().isPresent(), is(false));
    assertThat(publication.detail.isAlias(), is(false));
  }

  @Test
  void theNotificationOfAPublicationNotYetValidIsPostponed() {
    final PublicationInFolders publication =
        aPublication(PublicationDetail.TO_VALIDATE_STATUS).in(MAIN_FOLDER)
            .withAliasesIn(AN_ALIAS_FOLDER);

    final List<KmeliaSubscriptionPublicationUserNotification> notifications =
        notificationsSentBy(() -> service.performReminder(aReminderOfVisibility()));

    assertThat(notifications, is(empty()));
    verify(reminder).setAbout(publication.detail);
  }

  private PublicationInFolders aPublication(final String status) {
    final PublicationDetail detail = PublicationDetail.builder()
        .setPk(PUBLICATION_PK)
        .created(new Date(), AUTHOR)
        .setNameAndDescription("A publication", "")
        .build();
    detail.setStatus(status);
    when(publicationService.getDetail(PUBLICATION_PK)).thenReturn(detail);
    return new PublicationInFolders(detail);
  }

  private static Location anAliasIn(final String folderId) {
    final Location alias = new Location(folderId, COMPONENT_ID);
    alias.setAsAlias(AUTHOR);
    return alias;
  }

  private static Reminder aReminderOfVisibility() {
    final Reminder aReminder = mock(Reminder.class);
    when(aReminder.getProcessName()).thenReturn(
        KMELIA_DELAYED_VISIBILITY_USER_NOTIFICATION.asString());
    when(aReminder.getContributionId()).thenReturn(PUBLICATION);
    return aReminder;
  }

  /**
   * Gets the notifications that were asked to be sent to subscribers by the specified treatment.
   */
  private static List<KmeliaSubscriptionPublicationUserNotification> notificationsSentBy(
      final Runnable treatment) {
    final ArgumentCaptor<UserNotificationBuilder> builders =
        ArgumentCaptor.forClass(UserNotificationBuilder.class);
    try (MockedStatic<UserNotificationHelper> helper = mockStatic(UserNotificationHelper.class)) {
      treatment.run();
      helper.verify(() -> UserNotificationHelper.buildAndSend(builders.capture()), atLeast(0));
    }
    return builders.getAllValues()
        .stream()
        .filter(KmeliaSubscriptionPublicationUserNotification.class::isInstance)
        .map(KmeliaSubscriptionPublicationUserNotification.class::cast)
        .toList();
  }

  /**
   * The locations of a publication in the Kmelia application.
   */
  private class PublicationInFolders {

    private final PublicationDetail detail;

    PublicationInFolders(final PublicationDetail detail) {
      this.detail = detail;
    }

    PublicationInFolders in(final String mainFolderId) {
      final List<Location> locations = List.of(new Location(mainFolderId, COMPONENT_ID));
      when(publicationService.getLocationsInComponentInstance(PUBLICATION_PK, COMPONENT_ID))
          .thenReturn(locations);
      when(publicationService.getAllLocations(PUBLICATION_PK)).thenReturn(locations);
      return this;
    }

    PublicationInFolders withAliasesIn(final String... folderIds) {
      final List<Location> aliases =
          Stream.of(folderIds).map(KmeliaSubscribersNotificationTest::anAliasIn).toList();
      when(publicationService.getAllAliases(PUBLICATION_PK)).thenReturn(aliases);
      return this;
    }
  }
}
