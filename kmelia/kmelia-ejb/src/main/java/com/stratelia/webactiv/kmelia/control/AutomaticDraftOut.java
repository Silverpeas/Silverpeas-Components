/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.webactiv.kmelia.control;

import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerFactory;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBmHome;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

import java.rmi.RemoteException;

public class AutomaticDraftOut
    implements SchedulerEventListener {

  public static final String AUTOMATICDRAFTOUT_JOB_NAME = "KmeliaAutomaticDraftOutJob";
  private ResourceLocator resources =
      new ResourceLocator("com.stratelia.webactiv.kmelia.settings.kmeliaSettings", "");

  public void initialize() {
    SilverTrace.info("kmelia", "AutomaticDraftOut.initialize()", "root.MSG_GEN_ENTER_METHOD");
    try {
      String cron = resources.getString("cronAutomaticDraftOut");
      SchedulerFactory schedulerFactory = SchedulerFactory.getFactory();
      Scheduler scheduler = schedulerFactory.getScheduler();
      scheduler.unscheduleJob(AUTOMATICDRAFTOUT_JOB_NAME);
      if (StringUtil.isDefined(cron)) {
        JobTrigger trigger = JobTrigger.triggerAt(cron);
        scheduler.scheduleJob(AUTOMATICDRAFTOUT_JOB_NAME, trigger, this);
      }
    } catch (Exception e) {
      SilverTrace.error("kmelia", "AutomaticDraftOut.initialize()",
          "kmelia.EX_CANT_INIT_AUTOMATIC_DRAFT_OUT", e);
    }
  }

  public void doAutomaticDraftOut() {
    SilverTrace.info("kmelia", "AutomaticDraftOut.doAutomaticDraftOut()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      getKmeliaBm().doAutomaticDraftOut();
    } catch (RemoteException e) {
      throw new KmeliaRuntimeException(
          "AutomaticDraftOut.doAutomaticDraftOut()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    SilverTrace.info("kmelia", "AutomaticDraftOut.doAutomaticDraftOut()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private KmeliaBm getKmeliaBm() {
    try {
      KmeliaBmHome kscEjbHome =
          EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME,
          KmeliaBmHome.class);
      return kscEjbHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("AutomaticDraftOut.getKmeliaBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) throws Exception {
    SilverTrace.debug("kmelia", "AutomaticDraftOut.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' is executing");
    doAutomaticDraftOut();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    SilverTrace.debug("kmelia", "AutomaticDraftOut.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was successfull");
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverTrace.error("kmelia", "AutomaticDraftOut.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was not successfull");
  }
}
