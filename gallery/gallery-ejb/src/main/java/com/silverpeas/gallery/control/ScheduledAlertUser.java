/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.control;

import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerFactory;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.util.Link;

import java.util.Collection;

import static com.silverpeas.gallery.control.ejb.MediaServiceFactory.getMediaService;

public class ScheduledAlertUser implements SchedulerEventListener {

  public static final String GALLERYENGINE_JOB_NAME = "GalleryEngineJob";
  private ResourceLocator resources = new ResourceLocator(
      "org.silverpeas.gallery.settings.gallerySettings", "");

  public void initialize() {
    try {
      String cron = resources.getString("cronScheduledAlertUser");
      SchedulerFactory schedulerFactory = SchedulerFactory.getFactory();
      Scheduler scheduler = schedulerFactory.getScheduler();
      scheduler.unscheduleJob(GALLERYENGINE_JOB_NAME);
      JobTrigger trigger = JobTrigger.triggerAt(cron);
      scheduler.scheduleJob(GALLERYENGINE_JOB_NAME, trigger, this);
    } catch (Exception e) {
      SilverTrace.error("gallery", "ScheduledAlertUser.initialize()",
          "gallery.EX_CANT_INIT_SCHEDULED_ALERT_USER", e);
    }
  }

  public void doScheduledAlertUser() {
    SilverTrace.info("gallery", "ScheduledAlertUser.doScheduledAlertUser()",
        "root.MSG_GEN_ENTER_METHOD");

    try {
      // recherche du nombre de jours
      int nbDays = Integer.parseInt(resources.getString("nbDaysForAlertUser"));

      // rechercher la liste des photos arrivant à échéance
      Collection<Media> mediaList = getMediaService().getAllMediaThatWillBeNotVisible(nbDays);
      SilverTrace.info("gallery", "ScheduledAlertUser.doScheduledAlertUser()",
          "root.MSG_GEN_PARAM_VALUE", "MediaList=" + mediaList.toString());

      OrganisationController orga = new OrganizationController();

      // pour chaque photo, construction d'une ligne ...
      String currentInstanceId = null;

      ResourceLocator message =
          new ResourceLocator("org.silverpeas.gallery.multilang.galleryBundle", DisplayI18NHelper.
              getDefaultLanguage());

      StringBuilder messageBody = new StringBuilder();
      Media nextMedia = null;

      for (Media media : mediaList) {
        nextMedia = media;

        if (media.getInstanceId().equals(currentInstanceId)) {
          // construire la liste des images pour cette instance (a mettre dans
          // le corps du message)
          messageBody.append(message.getString("gallery.notifName")).append(" : ").append(media.
              getName()).append("\n");
          SilverTrace.info("gallery", "ScheduledAlertUser.doScheduledAlertUser()",
              "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
        } else {
          if (currentInstanceId != null) {
            // Création du message à envoyer aux admins
            UserDetail[] admins = orga.getUsers("useless", currentInstanceId, "admin");
            String subject = message.getString("gallery.notifSubject");
            String body = messageBody.append("\n").append(
                message.getString("gallery.notifUserInfo")).append("\n\n").toString();
            NotificationMetaData notifMetaData = new NotificationMetaData(
                NotificationParameters.NORMAL, subject, body);
            createMessage(notifMetaData, media, admins);
            messageBody = new StringBuilder();
          }
          currentInstanceId = media.getInstanceId();
          String nameInstance = orga.getComponentInst(currentInstanceId).getLabel();
          SilverTrace.info("gallery", "ScheduledAlertUser.doScheduledAlertUser()",
              "root.MSG_GEN_PARAM_VALUE", "currentInstanceId = " + currentInstanceId);

          // initialisation du corps du message avec la première photo de
          // l'instance en cours
          messageBody.append(message.getString("gallery.notifTitle")).append(
              nameInstance).append("\n").append("\n");
          messageBody.append(message.getString("gallery.notifName")).append(" : ")
              .append(media.getName()).append("\n");

          SilverTrace.info("gallery", "ScheduledAlertUser.doScheduledAlertUser()",
              "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
        }
      }

      // Création du message à envoyer aux admins pour la dernière instance en
      // cours
      if (currentInstanceId != null) {
        UserDetail[] admins = orga.getUsers("useless", currentInstanceId, "admin");
        String subject = message.getString("gallery.notifSubject");
        String body = messageBody.append("\n").append(
            message.getString("gallery.notifUserInfo")).append("\n\n").toString();
        NotificationMetaData notifMetaData = new NotificationMetaData(
            NotificationParameters.NORMAL, subject, body);
        createMessage(notifMetaData, nextMedia, admins);
      }
    } catch (Exception e) {
      throw new GalleryRuntimeException("ScheduledAlertUser.doScheduledAlertUser()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }

    SilverTrace.info("gallery", "ScheduledAlertUser.doScheduledAlertUser()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private void createMessageByLanguage(String url, NotificationMetaData notifMetaData) {
    for (String language : DisplayI18NHelper.getLanguages()) {
      ResourceLocator message =
          new ResourceLocator("org.silverpeas.gallery.multilang.galleryBundle", language);
      String subject = message.getString("gallery.notifSubject");
      StringBuilder messageBody = new StringBuilder();
      String body = messageBody.append("\n").append(
          message.getString("gallery.notifUserInfo")).append("\n\n").toString();
      notifMetaData.addLanguage(language, subject, body);
      Link link = new Link(url, message.getString("gallery.notifLinkLabel"));
      notifMetaData.setLink(link, language);
    }
  }

  private void createMessage(NotificationMetaData notifMetaData, Media media, UserDetail[] admins) {
    if (admins == null || admins.length == 0) {
      return;
    }

    // 1. création du message
    String url = getPhotoUrl(media);
    createMessageByLanguage(url, notifMetaData);

    for (UserDetail admin : admins) {
      notifMetaData.addUserRecipient(new UserRecipient(admin));
    }
    notifMetaData.setComponentId(media.getInstanceId());

    // 2. envoie de la notification aux admin
    if (StringUtil.isDefined(media.getCreatorId())) {
      notifMetaData.setSender(media.getCreatorId());
    }
    NotificationSender notifSender = new NotificationSender(media.getInstanceId());
    try {
      notifSender.notifyUser(notifMetaData);
    } catch (NotificationManagerException e) {
      SilverTrace.error("gallery", "ScheduledAlertUser.ScheduledAlertUser",
          "gallery.CANT_NOTIFY_USERS", e);
    }
  }

  private String getPhotoUrl(Media media) {
    return URLManager.getURL(null, media.getInstanceId()) + media.getURL();
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) throws Exception {
    SilverTrace.debug("gallery", "ScheduledAlertUser.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' is executing");
    doScheduledAlertUser();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    SilverTrace.debug("gallery", "ScheduledAlertUser.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was successfull");
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverTrace.error("gallery", "ScheduledAlertUser.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName()
        + "' was not successfull", anEvent.getJobThrowable());
  }
}
