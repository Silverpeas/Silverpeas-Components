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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.quickinfo.notification;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.reminder.BackgroundReminderProcess;
import org.silverpeas.core.reminder.DateTimeReminder;
import org.silverpeas.core.reminder.Reminder;
import org.silverpeas.core.reminder.ReminderProcessName;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.time.temporal.Temporal;

import static org.silverpeas.core.contribution.publication.model.PublicationDetail.DELAYED_VISIBILITY_AT_MODEL_PROPERTY;
import static org.silverpeas.core.reminder.BackgroundReminderProcess.Constants.PROCESS_NAME_SUFFIX;

/**
 * Implementation in charge of handling data about
 * {@link org.silverpeas.components.quickinfo.model.News} entities provided
 * by quickinfo component instances.
 * @author silveryocha
 */
@Named(QuickInfoDelayedVisibilityUserNotificationReminder.PROCESS_NAME + PROCESS_NAME_SUFFIX)
@Singleton
public class QuickInfoDelayedVisibilityUserNotificationReminder implements BackgroundReminderProcess {

  static final String PROCESS_NAME = "quickinfoDelayedVisibilityUserNotification";
  public static final ReminderProcessName QUICKINFO_DELAYED_VISIBILITY_USER_NOTIFICATION = () -> PROCESS_NAME;

  @Inject
  private QuickInfoService newsService;

  public static QuickInfoDelayedVisibilityUserNotificationReminder get() {
    return ServiceProvider.getService(QuickInfoDelayedVisibilityUserNotificationReminder.class);
  }

  @Override
  public ReminderProcessName getName() {
    return QUICKINFO_DELAYED_VISIBILITY_USER_NOTIFICATION;
  }

  /**
   * Sends the user notification about a specified reminder.
   * @param reminder a reminder.
   */
  @Override
  public void performWith(final Reminder reminder) {
    newsService.performReminder(reminder);
  }

  /**
   * Initialize the reminder for the given news if possible.
   * @param news the news for which the reminder is initialized.
   * @return true if the reminder has been set successfully, false otherwise.
   */
  public boolean setAbout(final News news) {
    final Temporal delayedVisibilityTemporal = news.getModel()
        .getProperty(DELAYED_VISIBILITY_AT_MODEL_PROPERTY);
    if (delayedVisibilityTemporal != null) {
      final ContributionIdentifier contributionId = news.getContributionId();
      if (Reminder.getByContribution(contributionId).stream().noneMatch(
          r -> QUICKINFO_DELAYED_VISIBILITY_USER_NOTIFICATION.asString().equals(r.getProcessName()))) {
        try {
          // Creating the reminder if it is not yet existing
          new DateTimeReminder(contributionId, QUICKINFO_DELAYED_VISIBILITY_USER_NOTIFICATION)
              .triggerFrom(DELAYED_VISIBILITY_AT_MODEL_PROPERTY)
              .schedule();
          return true;
        } catch (IllegalStateException e) {
          SilverLogger.getLogger(this).error(e);
        }
      }
    }
    return false;
  }
}
