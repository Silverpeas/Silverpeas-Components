/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.classifieds.control;

import java.util.Collection;
import java.util.Iterator;

import com.silverpeas.classifieds.control.ejb.ClassifiedsBm;
import com.silverpeas.classifieds.control.ejb.ClassifiedsBmHome;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.classifieds.model.ClassifiedsRuntimeException;
import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerFactory;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class ScheduledUnpublishExpiredClassifieds
    implements SchedulerEventListener {

  public static final String CLASSIFIEDSENGINE_JOB_NAME = "ClassifiedsEngineJobDelete";
  private ResourceLocator resources =
      new ResourceLocator("com.silverpeas.classifieds.settings.classifiedsSettings", "");

  public void initialize() {
    try {
      String cron = resources.getString("cronScheduledDeleteClassifieds");
      SchedulerFactory schedulerFactory = SchedulerFactory.getFactory();
      Scheduler scheduler = schedulerFactory.getScheduler();
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
      OrganizationController controller = new OrganizationController();
      String[] instanceIds = controller.getCompoId("classifieds");

      // Get default expiration delay from properties
      int defaultExpirationDelay = Integer.parseInt(resources.getString("nbDaysForDeleteClassifieds"));
      SilverTrace.info("classifieds", "ScheduledUnpublishExpiredClassifieds.doScheduledDeleteClassifieds()",
          "root.MSG_GEN_PARAM_VALUE", "defaultExpirationDelay = " + defaultExpirationDelay);

      // Iterate over all instances
      for (String instanceId : instanceIds) {
        // take default expiration delay if none is defined in instance setup
        int expirationDelay = defaultExpirationDelay;
        instanceId = "classifieds"+instanceId;
        String specificExpirationDelay = controller.getComponentParameterValue(instanceId, "expirationDelay");
        if (StringUtil.isDefined(specificExpirationDelay) && StringUtil.isInteger(specificExpirationDelay)) {
          expirationDelay = Integer.parseInt(specificExpirationDelay);
        }

        // search for expired classifieds
        Collection<ClassifiedDetail> classifieds =
            getClassifiedsBm().getAllClassifiedsToUnpublish(expirationDelay, instanceId);
        SilverTrace.info("classifieds", "ScheduledUnpublishExpiredClassifieds.doScheduledDeleteClassifieds()",
            "root.MSG_GEN_PARAM_VALUE", "Petites annonces = " + classifieds.toString());

        // iterate over classified to unpublished them
        if (classifieds != null) {
          Iterator<ClassifiedDetail> it = classifieds.iterator();
          while (it.hasNext()) {
            // unpublish classified
            ClassifiedDetail classified = (ClassifiedDetail) it.next();
            int classifiedId = classified.getClassifiedId();
            getClassifiedsBm().unpublishClassified(Integer.toString(classifiedId));
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

  private ClassifiedsBm getClassifiedsBm() {
    ClassifiedsBm classifiedsBm = null;
    try {
      ClassifiedsBmHome classifiedsBmHome =
          (ClassifiedsBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.CLASSIFIEDSBM_EJBHOME,
          ClassifiedsBmHome.class);
      classifiedsBm = classifiedsBmHome.create();
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifedsSessionController.getClassifiedsBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return classifiedsBm;
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
