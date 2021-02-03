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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.classifieds.service;

import org.silverpeas.components.classifieds.model.ClassifiedDetail;
import org.silverpeas.components.classifieds.model.ClassifiedsRuntimeException;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import java.util.Collection;

@Service
public class ScheduledUnpublishExpiredClassifieds implements SchedulerEventListener,
    Initialization {

  @Inject
  private Scheduler scheduler;

  @Inject
  private OrganizationController organizationController;

  @Inject
  private ClassifiedService classifiedService;

  public static final String CLASSIFIEDSENGINE_JOB_NAME = "ClassifiedsEngineJobDelete";

  @Override
  public void init() throws Exception{
    try {
      SettingBundle settings =
          ResourceLocator.getSettingBundle("org.silverpeas.classifieds.settings.classifiedsSettings");
      String cron = settings.getString("cronScheduledDeleteClassifieds");
      scheduler.unscheduleJob(CLASSIFIEDSENGINE_JOB_NAME);
      JobTrigger trigger = JobTrigger.triggerAt(cron);
      scheduler.scheduleJob(CLASSIFIEDSENGINE_JOB_NAME, trigger, this);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  public void doScheduledDeleteClassifieds() {

    try {
      // Retrieves all classifieds instances
      String[] instanceIds = organizationController.getCompoId("classifieds");

      // Get default expiration delay from properties
      SettingBundle settings =
          ResourceLocator.getSettingBundle("org.silverpeas.classifieds.settings.classifiedsSettings");
      int defaultExpirationDelay = settings.getInteger("nbDaysForDeleteClassifieds");

      // Iterate over all instances
      for (String instanceId : instanceIds) {
        // take default expiration delay if none is defined in instance setup
        int expirationDelay = defaultExpirationDelay;
        instanceId = "classifieds"+instanceId;
        String specificExpirationDelay = organizationController.getComponentParameterValue(instanceId, "expirationDelay");
        if (StringUtil.isDefined(specificExpirationDelay) && StringUtil.isInteger(specificExpirationDelay)) {
          expirationDelay = Integer.parseInt(specificExpirationDelay);
        }

        // search for expired classifieds
        Collection<ClassifiedDetail> classifieds =
            classifiedService.getAllClassifiedsToUnpublish(expirationDelay, instanceId);


        // iterate over classified to unpublished them
        if (classifieds != null) {
          for (final ClassifiedDetail classified : classifieds) {
            // unpublish classified
            int classifiedId = classified.getClassifiedId();
            classifiedService.unpublishClassified(Integer.toString(classifiedId));
          }
        }
      }
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException(e.getMessage(), e);
    }

  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) {
    doScheduledDeleteClassifieds();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    // nothing to do when the job has succeeded
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    // nothing to do when the job has failed
  }
}
