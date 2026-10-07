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
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.silverpeas.components.blog.model.PostDetail;
import org.silverpeas.components.blog.service.DefaultBlogService;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.notification.user.builder.UserNotificationBuilder;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.subscription.ResourceSubscriptionService;
import org.silverpeas.core.subscription.SubscriberDirective;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the notifications the blog service asks to send to the subscribers about a post:
 * a single notification is sent, whatever the way the users are subscribed (to the blog or to a
 * position on the PdC on which the post is classified), and only once the post is published.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class BlogSubscribersNotificationTest {

  private static final String COMPONENT_NAME = "blog";
  private static final String COMPONENT_ID = "blog7";
  private static final String AUTHOR = "1";
  private static final PublicationPK POST_PK = new PublicationPK("23", COMPONENT_ID);

  @TestManagedMock
  PublicationService publicationService;
  @TestManagedMock
  ResourceSubscriptionService subscriptionService;
  @TestedBean
  DefaultBlogService service;

  private Map<String, ResourceSubscriptionService> subscriptionServicesByComponent;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpTheSubscriptionsOfTheBlog(
      @TestManagedMock OrganizationController organizationController,
      @TestManagedMock UserProvider userProvider) throws Exception {
    final ComponentInstLight componentInstance = new ComponentInstLight();
    componentInstance.setLocalId(7);
    componentInstance.setName(COMPONENT_NAME);
    when(organizationController.getComponentInstance(COMPONENT_ID)).thenReturn(
        Optional.of(componentInstance));
    subscriptionServicesByComponent = (Map<String, ResourceSubscriptionService>) FieldUtils
        .readDeclaredStaticField(ResourceSubscriptionProvider.class, "componentImplementations",
            true);
    subscriptionServicesByComponent.put(COMPONENT_NAME, subscriptionService);
    when(subscriptionService.getSubscribersOfComponentAndTypedResource(anyString(), any(), any(),
        any(SubscriberDirective[].class))).thenReturn(new SubscriptionSubscriberList());

    final UserDetail author = mock(UserDetail.class);
    when(author.getId()).thenReturn(AUTHOR);
    when(userProvider.getUser(AUTHOR)).thenReturn(author);

    when(publicationService.createPublication(any(PublicationDetail.class))).thenReturn(POST_PK);
  }

  @AfterEach
  void clearTheSubscriptionsOfTheBlog() {
    subscriptionServicesByComponent.clear();
  }

  @Test
  void theSubscribersAreNotifiedOnceAboutThePublishingOfAPost() {
    final PostDetail post = aPost(PublicationDetail.DRAFT_STATUS);

    final List<UserNotificationBuilder> notifications =
        notificationsSentBy(() -> service.draftOutPost(post));

    assertThat(notifications, hasSize(1));
    assertThat(notifications.getFirst(), instanceOf(BlogUserSubscriptionNotification.class));
    final BlogUserSubscriptionNotification notification =
        (BlogUserSubscriptionNotification) notifications.getFirst();
    assertThat(notification.getAction(), is(NotifAction.CREATE));
    assertThat(notification.getComponentInstanceId(), is(COMPONENT_ID));
  }

  @Test
  void theSubscribersAreNotifiedOnceAboutTheUpdateOfAPublishedPost() {
    final PostDetail post = aPost(PublicationDetail.VALID_STATUS);

    final List<UserNotificationBuilder> notifications =
        notificationsSentBy(() -> service.updatePost(post, null));

    assertThat(notifications, hasSize(1));
    assertThat(((BlogUserSubscriptionNotification) notifications.getFirst()).getAction(),
        is(NotifAction.UPDATE));
  }

  @Test
  void nobodyIsNotifiedAboutTheUpdateOfADraft() {
    final PostDetail post = aPost(PublicationDetail.DRAFT_STATUS);

    final List<UserNotificationBuilder> notifications =
        notificationsSentBy(() -> service.updatePost(post, null));

    assertThat(notifications, is(empty()));
  }

  /**
   * A post is created as a draft: its subscribers, those on the PdC included, are notified later,
   * at its publishing. So the classification of the post at its creation mustn't alert the
   * subscribers on the PdC.
   */
  @Test
  void nobodyIsNotifiedAboutTheCreationOfAPostEvenIfItIsClassifiedOnThePdc() {
    final PostDetail post = aPost(PublicationDetail.DRAFT_STATUS);
    final PdcClassification classification = mock(PdcClassification.class);

    final List<UserNotificationBuilder> notifications =
        notificationsSentBy(() -> service.createPost(post, classification));

    assertThat(notifications, is(empty()));
    verify(classification).classifyContent(post.getPublication(), false);
    verify(classification, never()).classifyContent(post.getPublication());
    verify(classification, never()).classifyContent(post.getPublication(), true);
  }

  @Test
  void theClassificationOfAnUpdatedPostDoesNotAlertTheSubscribersOnThePdc() {
    final PostDetail post = aPost(PublicationDetail.VALID_STATUS);
    final PdcClassification classification = mock(PdcClassification.class);

    final List<UserNotificationBuilder> notifications =
        notificationsSentBy(() -> service.updatePost(post, classification));

    assertThat(notifications, hasSize(1));
    verify(classification).classifyContentOrClearClassificationIfEmpty(post.getPublication(),
        false);
    verify(classification, never()).classifyContent(any(), anyBoolean());
  }

  private static PostDetail aPost(final String status) {
    final PublicationDetail publication = PublicationDetail.builder()
        .setPk(POST_PK)
        .created(new Date(), AUTHOR)
        .updated(new Date(), AUTHOR)
        .setNameAndDescription("A post", "")
        .build();
    publication.setStatus(status);
    return new PostDetail(publication, null, new Date());
  }

  /**
   * Gets the notifications that were asked to be sent by the specified treatment. The accesses to
   * the data source and to the WYSIWYG content of the post are neutralized.
   */
  private static List<UserNotificationBuilder> notificationsSentBy(final Runnable treatment) {
    final ArgumentCaptor<UserNotificationBuilder> builders =
        ArgumentCaptor.forClass(UserNotificationBuilder.class);
    try (MockedStatic<UserNotificationHelper> helper = mockStatic(UserNotificationHelper.class);
         MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class);
         MockedStatic<WysiwygController> ignored = mockStatic(WysiwygController.class)) {
      dbUtil.when(DBUtil::openConnection).thenReturn(mock(Connection.class));
      treatment.run();
      helper.verify(() -> UserNotificationHelper.buildAndSend(builders.capture()), atLeast(0));
    }
    return builders.getAllValues();
  }
}
