/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.classifieds.control;

import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.model.ClassifiedsRuntimeException;
import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.initialization.Initialization;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.SettingBundle;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

import javax.inject.Inject;
import java.util.Collection;

public class ScheduledUnpublishExpiredClassifieds implements SchedulerEventListener,
    Initialization {

  @Inject
  private Scheduler scheduler;

  @Inject
  private OrganizationController organizationController;

  @Inject
  private ClassifiedService classifiedService;

  public static final String CLASSIFIEDSENGINE_JOB_NAME = "ClassifiedsEngineJobDelete";
  private SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.classifieds.settings.classifiedsSettings");

  @Override
  public void init() throws Exception{
    try {
      String cron = settings.getString("cronScheduledDeleteClassifieds");
      scheduler.unscheduleJob(CLASSIFIEDSENGINE_JOB_NAME);
      JobTrigger trigger = JobTrigger.triggerAt(cron);
      scheduler.scheduleJob(CLASSIFIEDSENGINE_JOB_NAME, trigger, this);
    } catch (Exception e) {
      SilverTrace.error("classifieds", "ScheduledUnpublishExpiredClassifieds.initialize()",
          "classifieds.EX_CANT_INIT_SCHEDULED_DELETE_CLASSIFIEDS", e);
    }
  }

  public void doScheduledDeleteClassifieds() {
    SilverTrace.info("classifieds", "ScheduledUnpublishExpiredClassifieds.doScheduledDeleteClassifieds()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      // Retrieves all classifieds instances
      String[] instanceIds = organizationController.getCompoId("classifieds");

      // Get default expiration delay from properties
      int defaultExpirationDelay = settings.getInteger("nbDaysForDeleteClassifieds");
      SilverTrace.info("classifieds", "ScheduledUnpublishExpiredClassifieds.doScheduledDeleteClassifieds()",
          "root.MSG_GEN_PARAM_VALUE", "defaultExpirationDelay = " + defaultExpirationDelay);

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
        SilverTrace.info("classifieds", "ScheduledUnpublishExpiredClassifieds.doScheduledDeleteClassifieds()",
            "root.MSG_GEN_PARAM_VALUE", "Petites annonces = " + classifieds.toString());

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
      throw new ClassifiedsRuntimeException(
          "ScheduledUnpublishExpiredClassifieds.doScheduledDeleteClassifieds()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    SilverTrace.info("classifieds", "ScheduledDeleteClassifieds.doScheduledDeleteClassifieds()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) throws Exception {
    SilverTrace.debug("classifieds", "ScheduledUnpublishExpiredClassifieds.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' is executed");
    doScheduledDeleteClassifieds();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    SilverTrace.debug("classifieds", "ScheduledUnpublishExpiredClassifieds.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was successfull");
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverTrace.error("classifieds", "ScheduledUnpublishExpiredClassifieds.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was not successfull");
  }
}
