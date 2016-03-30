/**
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
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
package org.silverpeas.components.kmelia.service;

import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerProvider;
import com.silverpeas.scheduler.trigger.JobTrigger;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.components.kmelia.model.KmeliaRuntimeException;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

public class AutomaticDraftOut implements SchedulerEventListener {

  public static final String AUTOMATICDRAFTOUT_JOB_NAME = "KmeliaAutomaticDraftOutJob";
  private SettingBundle resources =
      ResourceLocator.getSettingBundle("org.silverpeas.kmelia.settings.kmeliaSettings");

  public void initialize() {

    try {
      String cron = resources.getString("cronAutomaticDraftOut", "");
      Scheduler scheduler = SchedulerProvider.getScheduler();
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

    getKmeliaBm().doAutomaticDraftOut();

  }

  private KmeliaService getKmeliaBm() {
    try {
      return ServiceProvider.getService(KmeliaService.class);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("AutomaticDraftOut.getKmeliaService()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) throws Exception {
    doAutomaticDraftOut();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverTrace.error("kmelia", "AutomaticDraftOut.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was not successfull");
  }
}
