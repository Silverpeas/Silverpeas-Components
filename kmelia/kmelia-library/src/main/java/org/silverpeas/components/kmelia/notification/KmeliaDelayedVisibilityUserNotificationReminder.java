/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.components.kmelia.notification;

import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
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
 * {@link org.silverpeas.core.contribution.publication.model.PublicationDetail} entities provided
 * by kmelia component instances.
 * @author silveryocha
 */
@Named(KmeliaDelayedVisibilityUserNotificationReminder.PROCESS_NAME + PROCESS_NAME_SUFFIX)
@Singleton
public class KmeliaDelayedVisibilityUserNotificationReminder implements BackgroundReminderProcess {

  static final String PROCESS_NAME = "KmeliaDelayedVisibilityUserNotification";
  public static final ReminderProcessName KMELIA_DELAYED_VISIBILITY_USER_NOTIFICATION = () -> PROCESS_NAME;

  @Inject
  private KmeliaService kmeliaService;

  public static KmeliaDelayedVisibilityUserNotificationReminder get() {
    return ServiceProvider.getService(KmeliaDelayedVisibilityUserNotificationReminder.class);
  }

  @Override
  public ReminderProcessName getName() {
    return KMELIA_DELAYED_VISIBILITY_USER_NOTIFICATION;
  }

  /**
   * Sends the user notification about a specified reminder.
   * @param reminder a reminder.
   */
  @Override
  public void performWith(final Reminder reminder) {
    kmeliaService.performReminder(reminder);
  }

  /**
   * Initialize the reminder for the given publication if possible.
   * @param pubDetail the publication for which the reminder is initialized.
   * @return true if the reminder has been set successfully, false otherwise.
   */
  public boolean setAbout(final PublicationDetail pubDetail) {
    final Temporal delayedVisibilityTemporal = pubDetail.getModel()
        .getProperty(DELAYED_VISIBILITY_AT_MODEL_PROPERTY);
    if (delayedVisibilityTemporal != null) {
      final ContributionIdentifier contributionId = pubDetail.getIdentifier();
      if (Reminder.getByContribution(contributionId).stream().noneMatch(
          r -> KMELIA_DELAYED_VISIBILITY_USER_NOTIFICATION.asString().equals(r.getProcessName()))) {
        try {
          // Creating the reminder if it is not yet existing
          new DateTimeReminder(contributionId, KMELIA_DELAYED_VISIBILITY_USER_NOTIFICATION)
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
