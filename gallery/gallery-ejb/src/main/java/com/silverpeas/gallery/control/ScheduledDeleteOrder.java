/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.control;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.GalleryBmHome;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.Order;
import com.stratelia.silverpeas.scheduler.SchedulerEvent;
import com.stratelia.silverpeas.scheduler.SchedulerEventHandler;
import com.stratelia.silverpeas.scheduler.SimpleScheduler;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class ScheduledDeleteOrder implements SchedulerEventHandler {

  public static final String GALLERYENGINE_JOB_NAME = "GalleryEngineJobOrder";
  private ResourceLocator resources = new ResourceLocator(
      "com.silverpeas.gallery.settings.gallerySettings", "");

  public void initialize() {
    SilverTrace.info("gallery", "ScheduledDeleteOrder.initialize()",
        "Initializing the scheduler", "ENTREE");
    try {
      String cron = resources.getString("cronScheduledDeleteOrder");
      SimpleScheduler.unscheduleJob(this, GALLERYENGINE_JOB_NAME);
      SimpleScheduler.scheduleJob(this, GALLERYENGINE_JOB_NAME, cron, this,
          "doScheduledDeleteOrder");
    } catch (Exception e) {
      SilverTrace.error("gallery", "ScheduledDeleteOrder.initialize()",
          "gallery.EX_CANT_INIT_SCHEDULED_DELETE_ORDER", e);
    }
  }

  @Override
  public void handleSchedulerEvent(SchedulerEvent aEvent) {
    switch (aEvent.getType()) {
      case SchedulerEvent.EXECUTION_NOT_SUCCESSFULL:
        SilverTrace.error("gallery",
            "ScheduledDeleteOrder.handleSchedulerEvent", "The job '"
            + aEvent.getJob().getJobName() + "' was not successfull");
        break;
      case SchedulerEvent.EXECUTION_SUCCESSFULL:
        SilverTrace.debug("gallery",
            "ScheduledDeleteOrder.handleSchedulerEvent", "The job '"
            + aEvent.getJob().getJobName() + "' was successfull");
        break;
      default:
        SilverTrace.error("gallery",
            "ScheduledDeleteOrder.handleSchedulerEvent", "Illegal event type");
        break;
    }
  }

  public void doScheduledDeleteOrder(Date date) {
    SilverTrace.info("gallery",
        "ScheduledDeleteOrder.doScheduledDeleteOrder()",
        "root.MSG_GEN_ENTER_METHOD");

    try {
      // recherche du nombre de jours avant suppression
      int nbDays = Integer.parseInt(resources.getString("nbDaysForDeleteOrder"));

      // rechercher toutes les demandes arrivant à échéance
      Collection orders = getGalleryBm().getAllOrderToDelete(nbDays);
      SilverTrace.info("gallery",
          "ScheduledAlertUser.doScheduledDeleteOrder()",
          "root.MSG_GEN_PARAM_VALUE", "Demandes = " + orders.toString());

      if (orders != null) {
        Iterator it = orders.iterator();
        while (it.hasNext()) {
          // pour chaque demande, la supprimer
          Order order = (Order) it.next();
          int orderId = order.getOrderId();
          getGalleryBm().deleteOrder(Integer.toString(orderId));
        }
      }
    } catch (Exception e) {
      throw new GalleryRuntimeException(
          "ScheduledDeleteOrder.doScheduledDeleteOrder()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }

    SilverTrace.info("gallery", "ScheduledAlertUser.doScheduledAlertUser()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private GalleryBm getGalleryBm() {
    GalleryBm galleryBm = null;
    try {
      GalleryBmHome galleryBmHome = (GalleryBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBmHome.class);
      galleryBm = galleryBmHome.create();
    } catch (Exception e) {
      throw new GalleryRuntimeException("ScheduledDeleteOrder.getGalleryBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return galleryBm;
  }
}
