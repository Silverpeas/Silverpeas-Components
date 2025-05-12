/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.kmelia.service;

import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.ComponentInstManager;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Date;
import java.util.stream.Stream;

import static org.silverpeas.core.util.DateUtil.toLocalDate;

/**
 * A service scheduled in time to clean out periodically the bin in each Kmelia instances of the
 * items (both topics and publications) that have been removed since a given number of days. The
 * purge scheduling and the delay in days can be parameterized in the
 * <code>org/silverpeas/kmelia/settings/kmeliaSettings.properties</code> configuration file.
 *
 * @author mmoquillon
 */
@Service
public class KmeliaBinsScheduledPurger implements Initialization {

  private static final String COMPONENT_NAME = "kmelia";
  private static final String SETTINGS_NAME = "org.silverpeas.kmelia.settings.kmeliaSettings";
  private static final String JOB_NAME = "BinOlderItemsDeleter";

  @Inject
  private KmeliaDeleter deleter;
  @Inject
  private ComponentInstManager componentInstManager;
  @Inject
  private NodeService nodeService;
  @Inject
  private PublicationService publicationService;

  @Override
  public void init() throws Exception {
    String cron = getSchedulingCron();
    if (!cron.isEmpty()) {
      final Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
      scheduler.unscheduleJob(JOB_NAME);
      scheduler.scheduleJob(new BinOlderItemsDeleter(), JobTrigger.triggerAt(cron));
    }
    new BinOlderItemsDeleter().execute(JobExecutionContext.createWith(JOB_NAME, new Date()));
  }

  @Override
  public void release() throws Exception {
    Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
    if (scheduler.isJobScheduled(JOB_NAME)) {
      scheduler.unscheduleJob(JOB_NAME);
    }
  }

  private String getSchedulingCron() {
    SettingBundle settings = ResourceLocator.getSettingBundle(SETTINGS_NAME);
    return settings.getString("kmelia.autoDeletionCron", "");
  }

  private class BinOlderItemsDeleter extends Job {

    BinOlderItemsDeleter() {
      super(JOB_NAME);
    }

    @Override
    public void execute(final JobExecutionContext context) {
      int delay = getDeletionDelay();
      if (delay > 0) {
        SilverLogger.getLogger(this)
            .info("Purge Kmelia bins from contributions removed more than " + delay + " days");
        try {
          // browse all the root items in each Kmelia bins and for each of them apply the purge algo
          Transaction.performInOne(() -> {
            final LocalDate now = LocalDate.now();
            WAComponent.getByName(COMPONENT_NAME)
                .ifPresent(component ->
                    getAllInstanceIds(component)
                        .map(id -> COMPONENT_NAME + id)
                        .forEach(instanceId -> {
                          NodePK bin = new NodePK(NodePK.BIN_NODE_ID, instanceId);
                          nodeService.getChildrenDetails(bin).stream()
                              .filter(node -> isOlder(node, now))
                              .forEach(topic ->
                                  deleter.deleteTopic(topic.getNodePK()));
                          publicationService.getDetailsByFatherPK(bin).stream()
                              .filter(publication -> isOlder(publication, now))
                              .forEach(publication ->
                                  deleter.deletePublication(publication.getPK()));
                        }));
            return null;
          });
        } catch (SilverpeasRuntimeException e) {
          SilverLogger.getLogger(this).error(e.getMessage(), e);
        }
      }
    }

    private int getDeletionDelay() {
      SettingBundle settings = ResourceLocator.getSettingBundle(SETTINGS_NAME);
      int delay = settings.getInteger("kmelia.autoDeletionDelay", 0);
      return Math.max(delay, 0);
    }

    private boolean isOlder(PublicationDetail publication, LocalDate date) {
      // old publication in bin before the adding of the removal properties. In such situation, it
      // is automatically deleted
      if (!publication.isRemoved()) {
        return true;
      }
      return isOlder(publication.getRemovalDate(), date);
    }

    private boolean isOlder(NodeDetail topic, LocalDate date) {
      // old topic in bin before the adding of the removal properties. In such situation, it is
      // automatically deleted
      if (!topic.isRemoved()) {
        return true;
      }
      return isOlder(topic.getRemovalDate(), date);
    }

    private boolean isOlder(Date removalDate, LocalDate date) {
      final LocalDate removeDayDateWithDelay = toLocalDate(removalDate)
          .plusDays(getDeletionDelay());
      return removeDayDateWithDelay.isBefore(date) ||
          removeDayDateWithDelay.isEqual(date);
    }

    private Stream<String> getAllInstanceIds(WAComponent component) {
      try {
        return Stream.of(componentInstManager.getAllCompoIdsByComponentName(component.getName()));
      } catch (AdminException e) {
        throw new SilverpeasRuntimeException(e);
      }
    }
  }
}
  