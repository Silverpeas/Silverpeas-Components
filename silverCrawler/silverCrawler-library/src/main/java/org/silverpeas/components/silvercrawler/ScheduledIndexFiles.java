/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.silvercrawler;

import org.silverpeas.components.silvercrawler.model.SilverCrawlerRuntimeException;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.index.indexing.model.RepositoryIndexer;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ScheduledIndexFiles implements SchedulerEventListener {

  public static final String SILVERCRAWLERENGINE_JOB_NAME = "SilverCrawlerEngineJob";

  public void initialize() {
    try {
      SettingBundle resources = ResourceLocator.getSettingBundle(
          "org.silverpeas.silvercrawler.settings.silverCrawlerSettings");
      String cron = resources.getString("cronScheduledIndex");
      Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
      scheduler.unscheduleJob(SILVERCRAWLERENGINE_JOB_NAME);
      JobTrigger trigger = JobTrigger.triggerAt(cron);
      scheduler.scheduleJob(SILVERCRAWLERENGINE_JOB_NAME, trigger, this);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  public void doScheduledIndex() {
    try {
      // indexation des fichiers du composant
      OrganizationController orga = OrganizationController.get();
      List<ComponentInstLight> instances = orga.getComponentsWithParameterValue("periodicIndex", "yes");
      for (ComponentInstLight instance : instances) {
        RepositoryIndexer repositoryIndexer = new RepositoryIndexer(null, instance.getId());

        List<String> profiles = new ArrayList<>();
        profiles.add("admin");
        String[] adminIds = orga.getUsersIdsByRoleNames(instance.getId(), profiles);

        String adminId = "0";
        if (adminIds != null && adminIds.length > 0) {
          adminId = adminIds[0];
        }

        Path pathRepository = Paths.get(instance.getParameterValue("directory"));
        repositoryIndexer.addPath(pathRepository, adminId);
      }
    } catch (Exception e) {
      throw new SilverCrawlerRuntimeException(e);
    }
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) {
    doScheduledIndex();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    // nothing to do
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverLogger.getLogger(this)
        .error(
            "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was not successfull");
  }
}
