/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.silverpeas.components.gallery.model.AlbumDetail;
import org.silverpeas.components.gallery.model.AlbumMedia;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author silveryocha
 */
@Service
public class AlbumMediaNotificationManager {

  private static final ConcurrentMap<String, NotificationAlbumJob> contextCache =
      new ConcurrentHashMap<>();

  AlbumMediaNotificationManager() {
  }

  private static String buildKey(final User user, final AlbumMedia albumMedia) {
    return buildKey(user, albumMedia.getMedia().getInstanceId(), albumMedia.getAlbumId());
  }

  private static String buildKey(final User user, final String instanceId, String albumId) {
    return user.getId() + "@" + instanceId + "@" + albumId;
  }

  public static AlbumMediaNotificationManager get() {
    return ServiceProvider.getService(AlbumMediaNotificationManager.class);
  }

  /**
   * Puts a creation of an {@link AlbumMedia} into the context.
   * @param albumMedia the couple of an album and a media.
   * @param by the user associated to {@link AlbumMedia}.
   */
  public void putCreationOf(final AlbumMedia albumMedia, final User by) {
    final NotificationAlbumJob job = getNotificationAlbumJob(by, albumMedia);
    job.registerCreation(albumMedia.getMedia());
  }

  /**
   * Puts a deletion of an {@link AlbumMedia} into the context.
   * @param albumMedia the couple of an album and a media.
   * @param by the user associated to {@link AlbumMedia}.
   */
  public void putDeletionOf(final AlbumMedia albumMedia, final User by) {
    final NotificationAlbumJob job = getNotificationAlbumJob(by, albumMedia);
    job.registerDeletion(albumMedia.getMedia());
  }

  private NotificationAlbumJob getNotificationAlbumJob(final User user,
      final AlbumMedia albumMedia) {
    final String key = buildKey(user, albumMedia);
    return contextCache
        .computeIfAbsent(key, k -> new NotificationAlbumJob(user, albumMedia.getAlbum()));
  }

  /**
   * Context of {@link Media} movements according to an album.
   */
  static class NotificationAlbumJob extends Job {
    final Set<Media> creations = Collections.synchronizedSet(new HashSet<>());
    final User sender;
    final String instanceId;
    final AlbumDetail album;
    private final Object mutex;

    private NotificationAlbumJob(final User sender, final AlbumDetail album) {
      super("JOB_NAME_" + UUID.randomUUID().toString());
      this.mutex = creations;
      this.sender = sender;
      this.instanceId = album.getNodePK().getInstanceId();
      this.album = album;
    }

    private void setJobDelay() {
      final SettingBundle settings = ResourceLocator
          .getSettingBundle("org.silverpeas.gallery.settings.gallerySettings");
      final int delayInSeconds = settings.getInteger("subscription.notification.delay", 180);
      boolean sendImmediately = delayInSeconds <= 0;
      if (!sendImmediately) {
        final Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
        final OffsetDateTime executionTime = OffsetDateTime.now().plusSeconds(delayInSeconds);
        try {
          scheduler.unscheduleJob(getName());
          if (isMediaToProcess()) {
            scheduler.scheduleJob(this, JobTrigger.triggerAt(executionTime));
          } else {
            clearJobCache();
          }
        } catch (SchedulerException e) {
          SilverLogger.getLogger(this).error(e);
          sendImmediately = true;
        }
      }
      if (sendImmediately) {
        execute(null);
      }
    }

    void registerCreation(final Media media) {
      synchronized (mutex) {
        creations.add(media);
        setJobDelay();
      }
      putIntoCache();
    }

    void registerDeletion(final Media media) {
      synchronized (mutex) {
        creations.remove(media);
        setJobDelay();
      }
      putIntoCache();
    }

    @Override
    public void execute(final JobExecutionContext context) {
      clearJobCache();
      final Media[] creationsToProcess;
      synchronized (mutex) {
        creationsToProcess = creations.toArray(new Media[0]);
        creations.clear();
      }
      if (creationsToProcess.length > 0) {
        final GalleryAlbumMediaSubscriptionNotificationBuilder builder =
            new GalleryAlbumMediaSubscriptionNotificationBuilder(album, sender)
            .aboutMedia(creationsToProcess);
        UserNotificationHelper.buildAndSend(builder);
      }
    }

    private void clearJobCache() {
      final String cacheKey = buildKey(sender, instanceId, String.valueOf(album.getId()));
      contextCache.remove(cacheKey);
    }

    private boolean isMediaToProcess() {
      return !creations.isEmpty();
    }

    private void putIntoCache() {
      if (isMediaToProcess()) {
        contextCache.put(buildKey(sender, instanceId, String.valueOf(album.getId())),
            NotificationAlbumJob.this);
      }
    }
  }
}
