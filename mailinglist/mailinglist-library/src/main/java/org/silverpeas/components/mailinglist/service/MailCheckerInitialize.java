/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package org.silverpeas.components.mailinglist.service;

import java.util.List;

import org.silverpeas.components.mailinglist.model.MailingListComponent;
import org.silverpeas.components.mailinglist.service.job.MessageChecker;
import org.silverpeas.components.mailinglist.service.model.MailingListService;
import org.silverpeas.components.mailinglist.service.model.beans.MailingList;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.trigger.TimeUnit;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;

public class MailCheckerInitialize implements Initialization {

  public static final String MAILING_LIST_JOB_NAME = "mailingListScheduler";
  @Inject
  private MessageChecker messageChecker;
  @Inject
  private MailingListService mailingListService;
  private static SettingBundle settings;
  private static final int DEFAULT_FREQUENCY = 1;

  static {
    settings = ResourceLocator.getSettingBundle("org.silverpeas.mailinglist.notification");
  }

  public int getFrequency() {
    return settings.getInteger("mail.check.frequency", DEFAULT_FREQUENCY);
  }

  public MessageChecker getMessageChecker() {
    return messageChecker;
  }

  public MailingListService getMailingListService() {
    return mailingListService;
  }

  @Override
  public void init() throws Exception {
    try {
      Scheduler scheduler = SchedulerProvider.getScheduler();
      if (scheduler.isJobScheduled(MAILING_LIST_JOB_NAME)) {
        scheduler.unscheduleJob(MAILING_LIST_JOB_NAME);
      }
      if (hasToCheckForNewMails()) {
        SilverLogger.getLogger("mailinglist").info("Check mails from mailing lists every "
        + getFrequency() + " minutes");
        MessageChecker checker = getMessageChecker();
        JobTrigger trigger = JobTrigger.triggerEvery(getFrequency(), TimeUnit.MINUTE);
        scheduler.scheduleJob(MAILING_LIST_JOB_NAME, trigger, checker);
        List<MailingList> mailingLists = getMailingListService().listAllMailingLists();
        for (MailingList list : mailingLists) {
          MailingListComponent component = new MailingListComponent(list.getComponentId());
          checker.addMessageListener(component);
        }
      }
    } catch (SchedulerException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
  }

  /**
   * Does new mails have to be checked regularly? Mails checking is performed only if a frequency
   * is defined at startup time.
   * @return true if new mails from mailing lists has to be checked regularly, false otherwise.
   */
  public boolean hasToCheckForNewMails() {
    return getFrequency() > 0;
  }
}
