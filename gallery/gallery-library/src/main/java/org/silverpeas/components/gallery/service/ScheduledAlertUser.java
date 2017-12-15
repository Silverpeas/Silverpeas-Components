/*
 * Copyright (C) 2000 - 2017 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.service;

import org.silverpeas.components.gallery.model.GalleryRuntimeException;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.Link;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.util.logging.SilverLogger;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.silverpeas.components.gallery.service.MediaServiceProvider.getMediaService;
import static org.silverpeas.core.notification.user.client.NotificationParameters.NORMAL;
import static org.silverpeas.core.admin.service.OrganizationControllerProvider.getOrganisationController;

public class ScheduledAlertUser implements SchedulerEventListener {

  public static final String GALLERYENGINE_JOB_NAME = "GalleryEngineJob";

  public void initialize() {
    try {
      SettingBundle resources =
          ResourceLocator.getSettingBundle("org.silverpeas.gallery.settings.gallerySettings");
      String cron = resources.getString("cronScheduledAlertUser");
      Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
      scheduler.unscheduleJob(GALLERYENGINE_JOB_NAME);
      JobTrigger trigger = JobTrigger.triggerAt(cron);
      scheduler.scheduleJob(GALLERYENGINE_JOB_NAME, trigger, this);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(
          "can not initialize successfully the batch in charge of alerting administrators about " +
              "the end of visibility of media", e);
    }
  }

  public void doScheduledAlertUser() {
    try {
      // Finding media for which the visibility will soon end
      SettingBundle resources =
          ResourceLocator.getSettingBundle("org.silverpeas.gallery.settings.gallerySettings");
      int nbDays = resources.getInteger("nbDaysForAlertUser");
      Collection<Media> mediaList = getMediaService().getAllMediaThatWillBeNotVisible(nbDays);

      String currentInstanceId = null;
      LocalizedContent messageContent = new LocalizedContent();
      for (Media media : mediaList) {
        if (!media.getInstanceId().equals(currentInstanceId)) {
          if (currentInstanceId != null) {
            // Sending the notification
            createMessage(messageContent, currentInstanceId);
            messageContent = new LocalizedContent();
          }
          currentInstanceId = media.getInstanceId();
        }
        // Adding the media header information
        messageContent.appendFromBundleKey("gallery.notifName").append(" : ")
            .append(media.getName()).append("\n");
      }

      // Finally send the last message
      if (currentInstanceId != null) {
        createMessage(messageContent, currentInstanceId);
      }
    } catch (Exception e) {
      throw new GalleryRuntimeException("ScheduledAlertUser.doScheduledAlertUser()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  private void createMessage(LocalizedContent localizedContent, String componentInstanceId) {
    UserDetail[] admins = getOrganisationController().getUsers("-1", componentInstanceId, "admin");
    if (admins == null || admins.length == 0) {
      return;
    }

    NotificationMetaData notificationMetaData = new NotificationMetaData(NORMAL, "", "");

    // Preparing the notification content
    localizedContent.append("\n").appendFromBundleKey("gallery.notifUserInfo").append("\n\n");
    mergeLocalizedContentIntoNotificationMetaData(componentInstanceId, localizedContent,
        notificationMetaData);
    notificationMetaData.setComponentId(componentInstanceId);

    // Sending the notification to the component instance administrators
    for (UserDetail admin : admins) {
      notificationMetaData.addUserRecipient(new UserRecipient(admin));
    }
    NotificationSender notifSender = new NotificationSender(componentInstanceId);
    try {
      notifSender.notifyUser(notificationMetaData);
    } catch (NotificationManagerException e) {
      SilverLogger.getLogger(this)
          .error("can not send the notification message about media which will be no more visible",
              e);
    }
  }

  private void mergeLocalizedContentIntoNotificationMetaData(final String componentInstanceId,
      LocalizedContent localizedContent, NotificationMetaData notificationMetaData) {
    String nameInstance =
        getOrganisationController().getComponentInst(componentInstanceId).getLabel();
    String url = URLUtil.getComponentInstanceURL(componentInstanceId) + "Main";
    for (String language : DisplayI18NHelper.getLanguages()) {
      LocalizationBundle bundle = LocalizedContent.getBundle(language);
      String subject = bundle.getString("gallery.notifSubject");
      String body = MessageFormat
          .format("{0} <b>{1}</b>\n\n{2}", bundle.getString("gallery.notifTitle"), nameInstance,
              localizedContent.get(language));
      notificationMetaData.addLanguage(language, subject, body);
      Link link = new Link(url, nameInstance);
      notificationMetaData.setLink(link, language);
    }
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) {
    doScheduledAlertUser();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    // nothing to do
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverLogger.getLogger(this).error(
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was not successfull",
        anEvent.getJobThrowable());
  }

  private static class LocalizedContent {
    private Map<String, StringBuilder> localizedContents = new HashMap<>();

    public LocalizedContent appendFromBundleKey(String key) {
      for (String language : DisplayI18NHelper.getLanguages()) {
        append(localizedContents, language, getBundle(language).getString(key));
      }
      return this;
    }

    public LocalizedContent append(String content) {
      for (String language : DisplayI18NHelper.getLanguages()) {
        append(localizedContents, language, content);
      }
      return this;
    }

    static LocalizationBundle getBundle(String language) {
      return ResourceLocator
          .getLocalizationBundle("org.silverpeas.gallery.multilang.galleryBundle", language);
    }

    private static void append(Map<String, StringBuilder> container, String language,
        String message) {
      StringBuilder sb =  container.computeIfAbsent(language, l -> new StringBuilder());
      sb.append(message);
    }

    public String get(final String language) {
      StringBuilder sb = localizedContents.get(language);
      return sb != null ? sb.toString() : "";
    }
  }
}
