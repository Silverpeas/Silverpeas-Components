/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.index.indexing.model.RepositoryIndexer;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ScheduledIndexFiles implements SchedulerEventListener {

  public static final String SILVERCRAWLERENGINE_JOB_NAME = "SilverCrawlerEngineJob";
  private SettingBundle resources = ResourceLocator.getSettingBundle(
      "org.silverpeas.silvercrawler.settings.silverCrawlerSettings");

  public void initialize() {
    try {
      String cron = resources.getString("cronScheduledIndex");
      Scheduler scheduler = SchedulerProvider.getScheduler();
      scheduler.unscheduleJob(SILVERCRAWLERENGINE_JOB_NAME);
      JobTrigger trigger = JobTrigger.triggerAt(cron);
      scheduler.scheduleJob(SILVERCRAWLERENGINE_JOB_NAME, trigger, this);
    } catch (Exception e) {
      SilverTrace.error("silverCrawler", "ScheduledIndexFiles.initialize()",
          "silverCrawler.EX_CANT_INIT_SCHEDULED_INDEX_FILES", e);
    }
  }

  public void doScheduledIndex() {
    try {
      // indexation des fichiers du composant
      OrganizationController orga = OrganizationControllerProvider.getOrganisationController();
      String[] instanceIds = orga.getCompoId("silverCrawler");
      for (int i = 0; instanceIds != null && i < instanceIds.length; i++) {
        ComponentInst instance = orga.getComponentInst("silverCrawler" + instanceIds[i]);
        boolean periodicIndex = "yes".equals(instance.getParameterValue("periodicIndex"));
        if (periodicIndex) {
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
      }
    } catch (Exception e) {
      throw new SilverCrawlerRuntimeException("ScheduledIndexFiles.doScheduledIndex()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) throws Exception {
    doScheduledIndex();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverTrace.error("silverCrawler", "ScheduledIndexFiles.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was not successfull");
  }
}
