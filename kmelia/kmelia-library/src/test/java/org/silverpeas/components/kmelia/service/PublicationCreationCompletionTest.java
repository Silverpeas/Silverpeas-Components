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
package org.silverpeas.components.kmelia.service;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.silverpeas.components.kmelia.model.KmeliaRuntimeException;
import org.silverpeas.components.kmelia.notification.KmeliaSubscriptionPublicationUserNotification;
import org.silverpeas.components.kmelia.notification.KmeliaSupervisorPublicationUserNotification;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodePath;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.notification.user.builder.UserNotificationBuilder;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.service.PdcClassificationService;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.subscription.ResourceSubscriptionService;
import org.silverpeas.core.subscription.SubscriberDirective;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the completion of a publication at its creation. Some properties of a publication
 * can be set only once the publication is created: its thumbnail for example, that is attached to
 * the identifier of the publication. For such properties to be taken into account by the
 * notifications about the creation of the publication, the publication has to be completed before
 * the supervisors and the subscribers are notified.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class PublicationCreationCompletionTest {

  private static final String COMPONENT_NAME = "kmelia";
  private static final String COMPONENT_ID = "kmelia42";
  private static final String AUTHOR = "1";
  private static final String PUBLICATION_ID = "23";
  private static final NodePK FOLDER = new NodePK("4", COMPONENT_ID);
  private static final String COMPLETION = "the publication is completed";
  private static final String SUPERVISORS_NOTIFICATION = "the supervisors are notified";
  private static final String SUBSCRIBERS_NOTIFICATION = "the subscribers are notified";

  @TestManagedMock
  PublicationService publicationService;
  @TestManagedMock
  NodeService nodeService;
  @TestManagedMock
  PdcClassificationService pdcClassificationService;
  @TestManagedMock
  ResourceSubscriptionService subscriptionService;
  @TestedBean
  DefaultKmeliaService service;

  private final PdcClassification classification = mock(PdcClassification.class);
  private final List<String> events = new ArrayList<>();
  private PublicationDetail publication;
  private MockedStatic<UserNotificationHelper> notificationHelper;
  private MockedStatic<DBUtil> dbUtil;

  @BeforeEach
  void setUpThePublicationToCreate(@TestManagedMock UserProvider userProvider) {
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
    final User author = mock(User.class);
    when(author.getId()).thenReturn(AUTHOR);
    when(userProvider.getUser(AUTHOR)).thenReturn(author);

    publication = PublicationDetail.builder()
        .setPk(new PublicationPK("unknown", COMPONENT_ID))
        .created(new Date(), AUTHOR)
        .setNameAndDescription("A publication", "")
        .build();
    publication.setStatus(PublicationDetail.VALID_STATUS);

    final PublicationPK createdPublicationPK = new PublicationPK(PUBLICATION_ID, COMPONENT_ID);
    final List<Location> locations = List.of(new Location(FOLDER.getId(), COMPONENT_ID));
    when(nodeService.getPath(FOLDER)).thenReturn(new NodePath());
    when(publicationService.createPublication(publication)).thenReturn(createdPublicationPK);
    when(publicationService.getDetail(createdPublicationPK)).thenReturn(publication);
    when(publicationService.getLocationsInComponentInstance(createdPublicationPK, COMPONENT_ID))
        .thenReturn(locations);
    when(publicationService.getAllLocations(createdPublicationPK)).thenReturn(locations);
    when(pdcClassificationService.findAPreDefinedClassification(FOLDER.getId(), COMPONENT_ID))
        .thenReturn(classification);
  }

  @BeforeEach
  void setUpTheSubscriptionsOfTheApplication(
      @TestManagedMock OrganizationController organizationController) throws Exception {
    final ComponentInstLight componentInstance = new ComponentInstLight();
    componentInstance.setLocalId(42);
    componentInstance.setName(COMPONENT_NAME);
    when(organizationController.getComponentInstance(COMPONENT_ID)).thenReturn(
        Optional.of(componentInstance));
    subscriptionServices().put(COMPONENT_NAME, subscriptionService);
    when(subscriptionService.getSubscribersOfSubscriptionResource(any(SubscriptionResource.class),
        any(SubscriberDirective[].class))).thenReturn(new SubscriptionSubscriberList());
  }

  @BeforeEach
  void setUpTheStaticMocks() {
    dbUtil = mockStatic(DBUtil.class);
    dbUtil.when(DBUtil::openConnection).thenReturn(mock(Connection.class));
    notificationHelper = mockStatic(UserNotificationHelper.class);
    notificationHelper.when(
            () -> UserNotificationHelper.buildAndSend(any(UserNotificationBuilder.class)))
        .thenAnswer(invocation -> {
          final UserNotificationBuilder builder = invocation.getArgument(0);
          if (builder instanceof KmeliaSupervisorPublicationUserNotification) {
            events.add(SUPERVISORS_NOTIFICATION);
          } else if (builder instanceof KmeliaSubscriptionPublicationUserNotification) {
            events.add(SUBSCRIBERS_NOTIFICATION);
          }
          return null;
        });
  }

  @AfterEach
  void clearTheSubscriptionsOfTheApplication() throws Exception {
    subscriptionServices().clear();
  }

  @AfterEach
  void releaseTheStaticMocks() {
    notificationHelper.close();
    dbUtil.close();
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
  }

  @Test
  void thePublicationIsCompletedBeforeAnyoneIsNotifiedAboutItsCreation() {
    final String publicationId = service.createPublicationIntoTopic(publication, FOLDER,
        classification, p -> events.add(COMPLETION));

    assertThat(publicationId, is(PUBLICATION_ID));
    assertThat(events, contains(COMPLETION, SUPERVISORS_NOTIFICATION, SUBSCRIBERS_NOTIFICATION));
  }

  /**
   * The identifier of the publication is required to attach to it its thumbnail for example.
   */
  @Test
  void thePublicationIsCompletedOnceCreated() {
    final List<String> completedPublicationIds = new ArrayList<>();

    service.createPublicationIntoTopic(publication, FOLDER, classification,
        p -> completedPublicationIds.add(p.getPK().getId()));

    assertThat(completedPublicationIds, contains(PUBLICATION_ID));
    verify(classification).classifyContent(publication, false);
  }

  @Test
  void thePublicationClassifiedAsPredefinedForItsFolderIsAlsoCompletedBeforeTheNotifications() {
    service.createPublicationIntoTopic(publication, FOLDER, p -> events.add(COMPLETION));

    assertThat(events, contains(COMPLETION, SUPERVISORS_NOTIFICATION, SUBSCRIBERS_NOTIFICATION));
    verify(classification).classifyContent(publication, false);
  }

  @Test
  void aPublicationWithoutAnythingToCompleteIsJustNotified() {
    service.createPublicationIntoTopic(publication, FOLDER, classification);

    assertThat(events, contains(SUPERVISORS_NOTIFICATION, SUBSCRIBERS_NOTIFICATION));
  }

  @Test
  void nobodyIsNotifiedAboutAPublicationWhoseCompletionFailed() {
    assertThrows(KmeliaRuntimeException.class,
        () -> service.createPublicationIntoTopic(publication, FOLDER, classification, p -> {
          throw new IllegalStateException("The thumbnail cannot be set");
        }));

    verify(publicationService).createPublication(publication);
    assertThat(events.isEmpty(), is(true));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, ResourceSubscriptionService> subscriptionServices()
      throws IllegalAccessException {
    return (Map<String, ResourceSubscriptionService>) FieldUtils.readDeclaredStaticField(
        ResourceSubscriptionProvider.class, "componentImplementations", true);
  }
}
