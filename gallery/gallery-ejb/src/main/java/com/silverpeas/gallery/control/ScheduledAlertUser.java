package com.silverpeas.gallery.control;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.GalleryBmHome;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.PhotoDetail;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.scheduler.SchedulerEvent;
import com.stratelia.silverpeas.scheduler.SchedulerEventHandler;
import com.stratelia.silverpeas.scheduler.SimpleScheduler;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class ScheduledAlertUser implements SchedulerEventHandler {

  public static final String GALLERYENGINE_JOB_NAME = "GalleryEngineJob";

  private ResourceLocator resources = new ResourceLocator(
      "com.silverpeas.gallery.settings.gallerySettings", "");

  public void initialize() {
    try {
      String cron = resources.getString("cronScheduledAlertUser");
      Vector jobList = SimpleScheduler.getJobList(this);
      if (jobList != null && jobList.size() > 0)
        SimpleScheduler.removeJob(this, GALLERYENGINE_JOB_NAME);
      SimpleScheduler.getJob(this, GALLERYENGINE_JOB_NAME, cron, this,
          "doScheduledAlertUser");
    } catch (Exception e) {
      SilverTrace.error("gallery", "ScheduledAlertUser.initialize()",
          "gallery.EX_CANT_INIT_SCHEDULED_ALERT_USER", e);
    }
  }

  public void handleSchedulerEvent(SchedulerEvent aEvent) {
    switch (aEvent.getType()) {
      case SchedulerEvent.EXECUTION_NOT_SUCCESSFULL:
        SilverTrace.error("gallery", "ScheduledAlertUser.handleSchedulerEvent",
            "The job '" + aEvent.getJob().getJobName()
                + "' was not successfull");
        break;
      case SchedulerEvent.EXECUTION_SUCCESSFULL:
        SilverTrace.debug("gallery", "ScheduledAlertUser.handleSchedulerEvent",
            "The job '" + aEvent.getJob().getJobName() + "' was successfull");
        break;
      default:
        SilverTrace.error("gallery", "ScheduledAlertUser.handleSchedulerEvent",
            "Illegal event type");
        break;
    }
  }

  public void doScheduledAlertUser(Date date) {
    SilverTrace.info("gallery", "ScheduledAlertUser.doScheduledAlertUser()",
        "root.MSG_GEN_ENTER_METHOD");

    try {
      // recherche du nombre de jours
      int nbDays = Integer.parseInt(resources.getString("nbDaysForAlertUser"));

      // rechercher la liste des photos arrivant à échéance
      Collection photos = getGalleryBm().getAllPhotoEndVisible(nbDays);
      SilverTrace.info("gallery", "ScheduledAlertUser.doScheduledAlertUser()",
          "root.MSG_GEN_PARAM_VALUE", "Photos=" + photos.toString());

      OrganizationController orga = new OrganizationController();

      // pour chaque photo, construction d'une ligne ...
      String currentInstanceId = null;

      ResourceLocator message = new ResourceLocator(
          "com.silverpeas.gallery.multilang.galleryBundle", "fr");
      ResourceLocator message_en = new ResourceLocator(
          "com.silverpeas.gallery.multilang.galleryBundle", "en");

      StringBuffer messageBody = new StringBuffer();
      StringBuffer messageBody_en = new StringBuffer();
      PhotoDetail nextPhoto = new PhotoDetail();

      Iterator it = photos.iterator();
      while (it.hasNext()) {
        PhotoDetail photo = (PhotoDetail) it.next();
        nextPhoto = photo;
        if (photo.getInstanceId().equals(currentInstanceId)) {
          // construire la liste des images pour cette instance (a mettre dans
          // le corps du message)
          messageBody.append(message.getString("gallery.notifName")).append(
              " : ").append(photo.getName()).append("\n");
          messageBody_en.append(message_en.getString("gallery.notifName"))
              .append(" : ").append(photo.getName()).append("\n");
          SilverTrace.info("gallery",
              "ScheduledAlertUser.doScheduledAlertUser()",
              "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
        } else {
          if (currentInstanceId != null) {
            // Création du message à envoyer aux admins
            UserDetail[] admins = orga.getUsers("useless", currentInstanceId,
                "admin");
            createMessage(message, messageBody, message_en, messageBody_en,
                photo, admins);
            messageBody = new StringBuffer();
            messageBody_en = new StringBuffer();
          }
          currentInstanceId = photo.getInstanceId();
          String nameInstance = orga.getComponentInst(currentInstanceId)
              .getLabel();
          SilverTrace.info("gallery",
              "ScheduledAlertUser.doScheduledAlertUser()",
              "root.MSG_GEN_PARAM_VALUE", "currentInstanceId = "
                  + currentInstanceId);

          // initialisation du corps du message avec la première photo de
          // l'instance en cours
          messageBody.append(message.getString("gallery.notifTitle")).append(
              nameInstance).append("\n").append("\n");
          messageBody.append(message.getString("gallery.notifName")).append(
              " : ").append(photo.getName()).append("\n");
          messageBody_en.append(message.getString("gallery.notifTitle"))
              .append(nameInstance).append("\n").append("\n");
          messageBody_en.append(message_en.getString("gallery.notifName"))
              .append(" : ").append(photo.getName()).append("\n");

          SilverTrace.info("gallery",
              "ScheduledAlertUser.doScheduledAlertUser()",
              "root.MSG_GEN_PARAM_VALUE", "body=" + messageBody.toString());
        }
      }
      // Création du message à envoyer aux admins pour la dernière instance en
      // cours
      UserDetail[] admins = orga
          .getUsers("useless", currentInstanceId, "admin");
      createMessage(message, messageBody, message_en, messageBody_en,
          nextPhoto, admins);
      messageBody = new StringBuffer();
      messageBody_en = new StringBuffer();
    } catch (Exception e) {
      throw new GalleryRuntimeException(
          "ScheduledAlertUser.doScheduledAlertUser()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }

    SilverTrace.info("gallery", "ScheduledAlertUser.doScheduledAlertUser()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private void createMessage(ResourceLocator message, StringBuffer messageBody,
      ResourceLocator message_en, StringBuffer messageBody_en,
      PhotoDetail photo, UserDetail[] admins) {
    // 1. création du message

    // french notifications
    String subject = message.getString("gallery.notifSubject");
    String body = messageBody.append("\n").append(
        message.getString("gallery.notifUserInfo")).append("\n\n").toString();

    // english notifications
    String subject_en = message_en.getString("gallery.notifSubject");
    String body_en = messageBody_en.append("\n").append(
        message.getString("gallery.notifUserInfo")).append("\n\n").toString();

    NotificationMetaData notifMetaData = new NotificationMetaData(
        NotificationParameters.NORMAL, subject, body);
    notifMetaData.addLanguage("en", subject_en, body_en);

    notifMetaData.addUserRecipients(admins);
    // notifMetaData.setLink(photo.getPermalink());
    notifMetaData.setLink(getPhotoUrl(photo));
    notifMetaData.setComponentId(photo.getInstanceId());

    // 2. envoie de la notification aux admin
    try {
      getGalleryBm().notifyUsers(notifMetaData, photo.getCreatorId(),
          photo.getInstanceId());
    } catch (RemoteException e) {
      throw new GalleryRuntimeException("ScheduledAlertUser.createMessage()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  private String getPhotoUrl(PhotoDetail photoDetail) {
    return URLManager.getURL(null, photoDetail.getInstanceId())
        + photoDetail.getURL();
  }

  private GalleryBm getGalleryBm() {
    GalleryBm galleryBm = null;
    try {
      GalleryBmHome galleryBmHome = (GalleryBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBmHome.class);
      galleryBm = galleryBmHome.create();
    } catch (Exception e) {
      throw new GalleryRuntimeException("ScheduledAlertUser.getGalleryBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return galleryBm;
  }
}