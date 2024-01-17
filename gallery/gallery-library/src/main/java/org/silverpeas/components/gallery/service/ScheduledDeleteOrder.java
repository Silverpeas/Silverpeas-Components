/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.service;

import org.silverpeas.components.gallery.model.GalleryRuntimeException;
import org.silverpeas.components.gallery.model.Order;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.List;

public class ScheduledDeleteOrder implements SchedulerEventListener {

  public static final String GALLERYENGINE_JOB_NAME = "GalleryEngineJobOrder";

  public void initialize() {
    try {
      SettingBundle resources =
          ResourceLocator.getSettingBundle("org.silverpeas.gallery.settings.gallerySettings");
      String cron = resources.getString("cronScheduledDeleteOrder");
      Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
      scheduler.unscheduleJob(GALLERYENGINE_JOB_NAME);
      JobTrigger trigger = JobTrigger.triggerAt(cron);
      scheduler.scheduleJob(GALLERYENGINE_JOB_NAME, trigger, this);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  public void doScheduledDeleteOrder() {

    try {
      // recherche du nombre de jours avant suppression
      SettingBundle resources =
          ResourceLocator.getSettingBundle("org.silverpeas.gallery.settings.gallerySettings");
      int nbDays = resources.getInteger("nbDaysForDeleteOrder");
      // rechercher toutes les demandes arrivant à échéance
      List<Order> orders = getGalleryService().getAllOrderToDelete(nbDays);

      getGalleryService().deleteOrders(orders);
    } catch (Exception e) {
      throw new GalleryRuntimeException(e);
    }


  }

  private GalleryService getGalleryService() {
    return ServiceProvider.getService(GalleryService.class);
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) {
    doScheduledDeleteOrder();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    // nothing to do
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverLogger.getLogger(this).error("The job '"
        + anEvent.getJobExecutionContext().getJobName() + "' was not successfull");
  }
}
