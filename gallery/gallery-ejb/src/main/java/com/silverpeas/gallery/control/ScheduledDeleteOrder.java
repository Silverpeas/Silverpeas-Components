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

import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.Order;
import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerFactory;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.EJBUtilitaire;
import org.silverpeas.util.JNDINames;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

import java.util.List;

public class ScheduledDeleteOrder
    implements SchedulerEventListener {

  public static final String GALLERYENGINE_JOB_NAME = "GalleryEngineJobOrder";
  private ResourceLocator resources = new ResourceLocator(
      "org.silverpeas.gallery.settings.gallerySettings", "");

  public void initialize() {
    SilverTrace.info("gallery", "ScheduledDeleteOrder.initialize()",
        "Initializing the scheduler", "ENTREE");
    try {
      String cron = resources.getString("cronScheduledDeleteOrder");
      SchedulerFactory schedulerFactory = SchedulerFactory.getFactory();
      Scheduler scheduler = schedulerFactory.getScheduler();
      scheduler.unscheduleJob(GALLERYENGINE_JOB_NAME);
      JobTrigger trigger = JobTrigger.triggerAt(cron);
      scheduler.scheduleJob(GALLERYENGINE_JOB_NAME, trigger, this);
    } catch (Exception e) {
      SilverTrace.error("gallery", "ScheduledDeleteOrder.initialize()",
          "gallery.EX_CANT_INIT_SCHEDULED_DELETE_ORDER", e);
    }
  }

  public void doScheduledDeleteOrder() {
    SilverTrace.info("gallery", "ScheduledDeleteOrder.doScheduledDeleteOrder()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      // recherche du nombre de jours avant suppression
      int nbDays = Integer.parseInt(resources.getString("nbDaysForDeleteOrder"));
      // rechercher toutes les demandes arrivant à échéance
      List<Order> orders = getGalleryBm().getAllOrderToDelete(nbDays);
      SilverTrace.info("gallery", "ScheduledAlertUser.doScheduledDeleteOrder()",
          "root.MSG_GEN_PARAM_VALUE", "Demandes = " + orders);
      getGalleryBm().deleteOrders(orders);
    } catch (Exception e) {
      throw new GalleryRuntimeException("ScheduledDeleteOrder.doScheduledDeleteOrder()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }

    SilverTrace.info("gallery", "ScheduledAlertUser.doScheduledAlertUser()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private GalleryBm getGalleryBm() {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBm.class);
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) throws Exception {
    SilverTrace.debug("gallery",
        "ScheduledDeleteOrder.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' is executing");
    doScheduledDeleteOrder();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    SilverTrace.debug("gallery",
        "ScheduledDeleteOrder.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' was successfull");
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverTrace.error("gallery",
        "ScheduledDeleteOrder.handleSchedulerEvent", "The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' was not successfull");
  }
}
