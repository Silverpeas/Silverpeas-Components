/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.gallery.notification.user;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.silverpeas.components.gallery.model.AlbumDetail;
import org.silverpeas.components.gallery.model.AlbumMedia;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.notification.user.AlbumMediaNotificationManager.NotificationAlbumJob;
import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationManager;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.SettingBundleStub;
import org.silverpeas.core.test.extention.TestManagedBean;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.test.extention.TestedBean;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.reflect.FieldUtils.readDeclaredStaticField;
import static org.apache.commons.lang3.reflect.FieldUtils.readField;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class AlbumMediaNotificationManagerTest {

  private static final ComponentInstLight componentInstance = new ComponentInstLight();
  private static final ComponentInstLight otherComponentInstance = new ComponentInstLight();
  private static final AlbumDetail albumDetail1 = new AlbumDetail(new NodeDetail());
  private static final AlbumDetail albumDetail2 = new AlbumDetail(new NodeDetail());
  private static final AlbumDetail albumDetail3 = new AlbumDetail(new NodeDetail());
  private static final User senderX = mock(User.class);
  private static final User senderY = mock(User.class);

  @RegisterExtension
  static SettingBundleStub gallerySettings = new SettingBundleStub(
      "org.silverpeas.gallery.settings.gallerySettings");

  @TestManagedMock
  private Scheduler scheduler;

  @TestManagedMock
  private UserNotificationManager userNotificationManager;

  @TestManagedBean
  private UserNotificationHelper userNotificationHelper;

  @TestedBean
  private AlbumMediaNotificationManager testedManager;

  private AlbumMedia albumMediaA;
  private AlbumMedia albumMediaB;
  private AlbumMedia albumMediaC;
  private AlbumMedia albumMediaD;

  @BeforeAll
  static void setupStaticData() throws IllegalAccessException {
    componentInstance.setLocalId(38);
    componentInstance.setName("gallery");
    otherComponentInstance.setLocalId(39);
    otherComponentInstance.setName("gallery");
    albumDetail1.setId(26);
    albumDetail1.getNodePK().setComponentName(componentInstance.getId());
    albumDetail2.setId(27);
    albumDetail2.getNodePK().setComponentName(componentInstance.getId());
    albumDetail3.setId(28);
    albumDetail3.getNodePK().setComponentName(otherComponentInstance.getId());
    when(senderX.getId()).thenReturn("1");
    when(senderY.getId()).thenReturn("2");
  }

  @BeforeEach
  void setup(@TestManagedMock GalleryService galleryService) throws Exception {
    getContextCache().clear();
    // settings
    gallerySettings.put("subscription.notification.delay", "0");
    // Album media instances
    albumMediaA = new AlbumMedia(albumDetail1.getId(), aMedia());
    when(albumMediaA.getMedia().getInstanceId()).thenReturn(componentInstance.getId());
    albumMediaB = new AlbumMedia(albumDetail2.getId(), aMedia());
    when(albumMediaB.getMedia().getInstanceId()).thenReturn(componentInstance.getId());
    albumMediaC = new AlbumMedia(albumDetail1.getId(), aMedia());
    when(albumMediaC.getMedia().getInstanceId()).thenReturn(componentInstance.getId());
    albumMediaD = new AlbumMedia(albumDetail3.getId(), aMedia());
    when(albumMediaD.getMedia().getInstanceId()).thenReturn(otherComponentInstance.getId());
    // services
    when(galleryService.getAlbum(albumDetail1.getNodePK())).thenReturn(albumDetail1);
    when(galleryService.getAlbum(albumDetail2.getNodePK())).thenReturn(albumDetail2);
    when(galleryService.getAlbum(albumDetail3.getNodePK())).thenReturn(albumDetail3);
  }

  @Test
  void putCreation() throws Exception {
    testedManager.putCreationOf(albumMediaA, senderX);
    assertThat(getContextCache().size(), is(0));
    verify(scheduler, times(0)).unscheduleJob(anyString());
    verify(scheduler, times(0)).scheduleJob(any(NotificationAlbumJob.class), any(JobTrigger.class));
    final ArgumentCaptor<GalleryAlbumMediaSubscriptionNotificationBuilder> captor = forClass(
        GalleryAlbumMediaSubscriptionNotificationBuilder.class);
    verify(userNotificationManager, times(1)).buildAndSend(captor.capture());
    final GalleryAlbumMediaSubscriptionNotificationBuilder builder = captor.getValue();
    assertUserNotificationBuilder(builder, albumDetail1);
  }

  @Test
  void putCreationWithDelay() throws Exception {
    final OffsetDateTime now = OffsetDateTime.now();
    gallerySettings.put("subscription.notification.delay", "10");
    testedManager.putCreationOf(albumMediaA, senderX);
    assertThat(getContextCache().size(), is(1));
    final String expectedKey = "1@gallery38@26";
    assertThat(getContextCache(), hasKey(expectedKey));
    final NotificationAlbumJob notificationAlbumJob = getContextCache().get(expectedKey);
    assertNotificationAlbumJob(notificationAlbumJob, 1, albumDetail1, senderX);
    verify(scheduler, times(1)).unscheduleJob(anyString());
    final ArgumentCaptor<JobTrigger> captor = forClass(JobTrigger.class);
    verify(scheduler, times(1)).scheduleJob(any(NotificationAlbumJob.class), captor.capture());
    final JobTrigger jobTrigger = captor.getValue();
    assertJobScheduled(jobTrigger, now, 0);
  }

  @Test
  void putCreationWithDelayButUnscheduleError() throws Exception {
    doThrow(new SchedulerException("Error test")).when(scheduler).unscheduleJob(anyString());
    gallerySettings.put("subscription.notification.delay", "10");
    testedManager.putCreationOf(albumMediaA, senderX);
    assertThat(getContextCache().size(), is(0));
    verify(scheduler, times(1)).unscheduleJob(anyString());
    verify(scheduler, times(0)).scheduleJob(any(NotificationAlbumJob.class), any(JobTrigger.class));
    final ArgumentCaptor<GalleryAlbumMediaSubscriptionNotificationBuilder> captor = forClass(
        GalleryAlbumMediaSubscriptionNotificationBuilder.class);
    verify(userNotificationManager, times(1)).buildAndSend(captor.capture());
    final GalleryAlbumMediaSubscriptionNotificationBuilder builder = captor.getValue();
    assertUserNotificationBuilder(builder, albumDetail1);
  }

  @Test
  void putCreationWithDelayButScheduleError() throws Exception {
    doThrow(new SchedulerException("Error test")).when(scheduler)
        .scheduleJob(any(NotificationAlbumJob.class), any(JobTrigger.class));
    gallerySettings.put("subscription.notification.delay", "10");
    testedManager.putCreationOf(albumMediaA, senderX);
    assertThat(getContextCache().size(), is(0));
    verify(scheduler, times(1)).unscheduleJob(anyString());
    verify(scheduler, times(1)).scheduleJob(any(NotificationAlbumJob.class), any(JobTrigger.class));
    final ArgumentCaptor<GalleryAlbumMediaSubscriptionNotificationBuilder> captor = forClass(
        GalleryAlbumMediaSubscriptionNotificationBuilder.class);
    verify(userNotificationManager, times(1)).buildAndSend(captor.capture());
    final GalleryAlbumMediaSubscriptionNotificationBuilder builder = captor.getValue();
    assertUserNotificationBuilder(builder, albumDetail1);
  }

  @Test
  void putCreationOnSeveralAlbums() throws Exception {
    testedManager.putCreationOf(albumMediaA, senderX);
    testedManager.putCreationOf(albumMediaB, senderX);
    testedManager.putCreationOf(albumMediaC, senderX);
    assertThat(getContextCache().size(), is(0));
    verify(scheduler, times(0)).unscheduleJob(anyString());
    verify(scheduler, times(0)).scheduleJob(any(NotificationAlbumJob.class), any(JobTrigger.class));
    final ArgumentCaptor<GalleryAlbumMediaSubscriptionNotificationBuilder> captor = forClass(
        GalleryAlbumMediaSubscriptionNotificationBuilder.class);
    verify(userNotificationManager, times(3)).buildAndSend(captor.capture());
    final List<GalleryAlbumMediaSubscriptionNotificationBuilder> builders = captor.getAllValues();
    assertThat(builders, hasSize(3));
    assertUserNotificationBuilder(builders.get(0), albumDetail1);
    assertUserNotificationBuilder(builders.get(1), albumDetail2);
    assertUserNotificationBuilder(builders.get(2), albumDetail1);
  }

  @Test
  void putCreationWithDelayOnSeveralAlbums() throws Exception {
    gallerySettings.put("subscription.notification.delay", "10");
    final OffsetDateTime now = OffsetDateTime.now();
    testedManager.putCreationOf(albumMediaA, senderX);
    testedManager.putCreationOf(albumMediaB, senderX);
    testedManager.putCreationOf(albumMediaC, senderX);
    assertThat(getContextCache().size(), is(2));
    final String expectedKey1 = "1@gallery38@26";
    assertThat(getContextCache(), hasKey(expectedKey1));
    NotificationAlbumJob notificationAlbumJob = getContextCache().get(expectedKey1);
    assertNotificationAlbumJob(notificationAlbumJob, 2, albumDetail1, senderX);
    final String expectedKey2 = "1@gallery38@27";
    assertThat(getContextCache(), hasKey(expectedKey2));
    notificationAlbumJob = getContextCache().get(expectedKey2);
    assertNotificationAlbumJob(notificationAlbumJob, 1, albumDetail2, senderX);
    verify(scheduler, times(3)).unscheduleJob(anyString());
    final ArgumentCaptor<JobTrigger> captor = forClass(JobTrigger.class);
    verify(scheduler, times(3)).scheduleJob(any(NotificationAlbumJob.class), captor.capture());
    final List<JobTrigger> jobTriggers = captor.getAllValues();
    assertThat(jobTriggers, hasSize(3));
    assertJobScheduled(jobTriggers.get(0), now, 0);
    assertJobScheduled(jobTriggers.get(1), now, 0);
    assertJobScheduled(jobTriggers.get(2), now, 0);
  }

  @Test
  void putCreationOnSeveralAlbumsWithSeveralUsers() throws Exception {
    testedManager.putCreationOf(albumMediaA, senderX);
    testedManager.putCreationOf(albumMediaB, senderX);
    testedManager.putCreationOf(albumMediaC, senderY);
    assertThat(getContextCache().size(), is(0));
    verify(scheduler, times(0)).unscheduleJob(anyString());
    verify(scheduler, times(0)).scheduleJob(any(NotificationAlbumJob.class), any(JobTrigger.class));
    final ArgumentCaptor<GalleryAlbumMediaSubscriptionNotificationBuilder> captor = forClass(
        GalleryAlbumMediaSubscriptionNotificationBuilder.class);
    verify(userNotificationManager, times(3)).buildAndSend(captor.capture());
    final List<GalleryAlbumMediaSubscriptionNotificationBuilder> builders = captor.getAllValues();
    assertThat(builders, hasSize(3));
    assertUserNotificationBuilder(builders.get(0), albumDetail1);
    assertUserNotificationBuilder(builders.get(1), albumDetail2);
    assertUserNotificationBuilder(builders.get(2), albumDetail1);
  }

  @Test
  void putCreationWithDelayOnSeveralAlbumsWithSeveralUsers() throws Exception {
    gallerySettings.put("subscription.notification.delay", "10");
    final OffsetDateTime now = OffsetDateTime.now();
    testedManager.putCreationOf(albumMediaA, senderX);
    testedManager.putCreationOf(albumMediaB, senderX);
    testedManager.putCreationOf(albumMediaC, senderY);
    testedManager.putCreationOf(albumMediaD, senderX);
    assertThat(getContextCache().size(), is(4));
    final String expectedKey1 = "1@gallery38@26";
    assertThat(getContextCache(), hasKey(expectedKey1));
    NotificationAlbumJob notificationAlbumJob = getContextCache().get(expectedKey1);
    assertNotificationAlbumJob(notificationAlbumJob, 1, albumDetail1, senderX);
    final String expectedKey2 = "1@gallery38@27";
    assertThat(getContextCache(), hasKey(expectedKey2));
    notificationAlbumJob = getContextCache().get(expectedKey2);
    assertNotificationAlbumJob(notificationAlbumJob, 1, albumDetail2, senderX);
    final String expectedKey3 = "2@gallery38@26";
    assertThat(getContextCache(), hasKey(expectedKey3));
    notificationAlbumJob = getContextCache().get(expectedKey3);
    assertNotificationAlbumJob(notificationAlbumJob, 1, albumDetail1, senderY);
    final String expectedKey4 = "1@gallery39@28";
    assertThat(getContextCache(), hasKey(expectedKey4));
    notificationAlbumJob = getContextCache().get(expectedKey4);
    assertNotificationAlbumJob(notificationAlbumJob, 1, albumDetail3, senderX);
    verify(scheduler, times(4)).unscheduleJob(anyString());
    final ArgumentCaptor<JobTrigger> captor = forClass(JobTrigger.class);
    verify(scheduler, times(4)).scheduleJob(any(NotificationAlbumJob.class), captor.capture());
    final List<JobTrigger> jobTriggers = captor.getAllValues();
    assertThat(jobTriggers, hasSize(4));
    assertJobScheduled(jobTriggers.get(0), now, 0);
    assertJobScheduled(jobTriggers.get(1), now, 0);
    assertJobScheduled(jobTriggers.get(2), now, 0);
    assertJobScheduled(jobTriggers.get(3), now, 0);
  }

  @Test
  void putCreationWithDelayOnSeveralAlbumsWithLatency() throws Exception {
    gallerySettings.put("subscription.notification.delay", "10");
    final OffsetDateTime now = OffsetDateTime.now();
    testedManager.putCreationOf(albumMediaA, senderX);
    testedManager.putCreationOf(albumMediaB, senderX);
    await().pollInterval(5, TimeUnit.SECONDS).until(() -> true);
    testedManager.putCreationOf(albumMediaC, senderX);
    testedManager.putCreationOf(albumMediaD, senderX);
    assertThat(getContextCache().size(), is(3));
    final String expectedKey1 = "1@gallery38@26";
    assertThat(getContextCache(), hasKey(expectedKey1));
    NotificationAlbumJob notificationAlbumJob = getContextCache().get(expectedKey1);
    assertNotificationAlbumJob(notificationAlbumJob, 2, albumDetail1, senderX);
    final String expectedKey2 = "1@gallery38@27";
    assertThat(getContextCache(), hasKey(expectedKey2));
    notificationAlbumJob = getContextCache().get(expectedKey2);
    assertNotificationAlbumJob(notificationAlbumJob, 1, albumDetail2, senderX);
    final String expectedKey3 = "1@gallery39@28";
    assertThat(getContextCache(), hasKey(expectedKey3));
    notificationAlbumJob = getContextCache().get(expectedKey3);
    assertNotificationAlbumJob(notificationAlbumJob, 1, albumDetail3, senderX);
    verify(scheduler, times(4)).unscheduleJob(anyString());
    final ArgumentCaptor<JobTrigger> captor = forClass(JobTrigger.class);
    verify(scheduler, times(4)).scheduleJob(any(NotificationAlbumJob.class), captor.capture());
    final List<JobTrigger> jobTriggers = captor.getAllValues();
    assertThat(jobTriggers, hasSize(4));
    assertJobScheduled(jobTriggers.get(0), now, 0);
    assertJobScheduled(jobTriggers.get(1), now, 0);
    assertJobScheduled(jobTriggers.get(2), now, 5000);
    assertJobScheduled(jobTriggers.get(3), now, 5000);
  }

  @Test
  void putCreationAndThenDeletionOfSameMedia() throws Exception {
    testedManager.putCreationOf(albumMediaA, senderX);
    testedManager.putDeletionOf(albumMediaA, senderX);
    assertThat(getContextCache().size(), is(0));
    verify(scheduler, times(0)).unscheduleJob(anyString());
    verify(scheduler, times(0)).scheduleJob(any(NotificationAlbumJob.class), any(JobTrigger.class));
    final ArgumentCaptor<GalleryAlbumMediaSubscriptionNotificationBuilder> captor = forClass(
        GalleryAlbumMediaSubscriptionNotificationBuilder.class);
    verify(userNotificationManager, times(1)).buildAndSend(captor.capture());
    final GalleryAlbumMediaSubscriptionNotificationBuilder builder = captor.getValue();
    assertUserNotificationBuilder(builder, albumDetail1);
  }

  @Test
  void putCreationAndThenDeletionOfSameMediaWithDelay() throws Exception {
    final OffsetDateTime now = OffsetDateTime.now();
    gallerySettings.put("subscription.notification.delay", "10");
    testedManager.putCreationOf(albumMediaA, senderX);
    testedManager.putDeletionOf(albumMediaA, senderX);
    assertThat(getContextCache().size(), is(0));
    verify(scheduler, times(2)).unscheduleJob(anyString());
    final ArgumentCaptor<JobTrigger> captor = forClass(JobTrigger.class);
    verify(scheduler, times(1)).scheduleJob(any(NotificationAlbumJob.class), captor.capture());
    final JobTrigger jobTrigger = captor.getValue();
    assertJobScheduled(jobTrigger, now, 0);
  }

  @Test
  void putCreationThenDeletionAndThenCreationOfSameMediaWithDelay() throws Exception {
    final OffsetDateTime now = OffsetDateTime.now();
    gallerySettings.put("subscription.notification.delay", "10");
    testedManager.putCreationOf(albumMediaA, senderX);
    testedManager.putDeletionOf(albumMediaA, senderX);
    testedManager.putCreationOf(albumMediaA, senderX);
    assertThat(getContextCache().size(), is(1));
    final String expectedKey = "1@gallery38@26";
    assertThat(getContextCache(), hasKey(expectedKey));
    final NotificationAlbumJob notificationAlbumJob = getContextCache().get(expectedKey);
    assertNotificationAlbumJob(notificationAlbumJob, 1, albumDetail1, senderX);
    verify(scheduler, times(3)).unscheduleJob(anyString());
    final ArgumentCaptor<JobTrigger> captor = forClass(JobTrigger.class);
    verify(scheduler, times(2)).scheduleJob(any(NotificationAlbumJob.class), captor.capture());
    final List<JobTrigger> jobTriggers = captor.getAllValues();
    assertThat(jobTriggers, hasSize(2));
    assertJobScheduled(jobTriggers.get(0), now, 0);
    assertJobScheduled(jobTriggers.get(1), now, 0);
  }

  @Test
  void putDeletion() throws Exception {
    testedManager.putDeletionOf(albumMediaA, senderX);
    assertThat(getContextCache().size(), is(0));
    verify(scheduler, times(0)).unscheduleJob(anyString());
    verify(scheduler, times(0)).scheduleJob(any(NotificationAlbumJob.class), any(JobTrigger.class));
    verify(userNotificationManager, times(0))
        .buildAndSend(any(GalleryAlbumMediaSubscriptionNotificationBuilder.class));
  }

  private void assertUserNotificationBuilder(
      final GalleryAlbumMediaSubscriptionNotificationBuilder builder,
      final AlbumDetail expectedAlbum) throws IllegalAccessException {
    assertThat(builder.concernedMedia, hasSize(1));
    assertThat(builder.getAction(), is(NotifAction.POPULATED));
    final AlbumDetail concernedAlbum = (AlbumDetail) readField(builder, "resource", true);
    assertThat(concernedAlbum, is(expectedAlbum));
  }

  private void assertNotificationAlbumJob(final NotificationAlbumJob notificationAlbumJob,
      final int nbExpectedMedia, final AlbumDetail expectedAlbum, final User expectedSender) {
    assertThat(notificationAlbumJob.album, is(expectedAlbum));
    assertThat(notificationAlbumJob.sender, is(expectedSender));
    assertThat(notificationAlbumJob.instanceId, is(expectedAlbum.getNodePK().getInstanceId()));
    assertThat(notificationAlbumJob.creations, hasSize(nbExpectedMedia));
  }

  private void assertJobScheduled(final JobTrigger jobTrigger, final OffsetDateTime nowReference,
      final long delayOffset) {
    final long delayInMilliseconds =
        jobTrigger.getStartDate().getTime() - Date.from(nowReference.toInstant()).getTime() -
            delayOffset;
    assertThat(delayInMilliseconds, greaterThanOrEqualTo(10000L));
    assertThat(delayInMilliseconds, lessThanOrEqualTo(11000L));
    verify(userNotificationManager, times(0))
        .buildAndSend(any(GalleryAlbumMediaSubscriptionNotificationBuilder.class));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, NotificationAlbumJob> getContextCache() throws IllegalAccessException {
    return (Map) readDeclaredStaticField(AlbumMediaNotificationManager.class, "contextCache", true);
  }

  private Media aMedia() {
    return mock(Media.class);
  }
}