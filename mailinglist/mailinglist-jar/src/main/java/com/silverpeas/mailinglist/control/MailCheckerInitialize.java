/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.silverpeas.mailinglist.control;

import java.util.List;

import com.silverpeas.mailinglist.model.MailingListComponent;
import com.silverpeas.mailinglist.service.job.MessageChecker;
import com.silverpeas.mailinglist.service.model.MailingListService;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.stratelia.silverpeas.scheduler.trigger.JobTrigger;
import com.stratelia.silverpeas.scheduler.SchedulerException;
import com.stratelia.silverpeas.scheduler.SimpleScheduler;
import com.stratelia.silverpeas.scheduler.TimeUnit;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class MailCheckerInitialize {

  public static final String MAILING_LIST_JOB_NAME = "mailingListScheduler";
  private MessageChecker messageChecker;
  private MailingListService mailingListService;
  private int frequency;

  public int getFrequency() {
    return frequency;
  }

  public void setFrequency(int frequency) {
    this.frequency = frequency;
  }

  public MessageChecker getMessageChecker() {
    return messageChecker;
  }

  public void setMessageChecker(MessageChecker messageChecker) {
    this.messageChecker = messageChecker;
  }

  public MailingListService getMailingListService() {
    return mailingListService;
  }

  public void setMailingListService(MailingListService mailingListService) {
    this.mailingListService = mailingListService;
  }

  public void registerAll() {
    SilverTrace.info("mailingList", "MailCheckerInitialize.Initialize",
        "mailinglist.initialization.start");
    MessageChecker checker = getMessageChecker();
    try {
      SilverTrace.info("mailingList", "MailCheckerInitialize.Initialize",
          "mailinglist.initialization.start", " " + checker);
      //@SuppressWarnings("unchecked")
      //Collection<SchedulerJob> jobList = SimpleScheduler.getJobList(checker);
      //SilverTrace.info("mailingList", "MailCheckerInitialize.Initialize",
      //    "mailinglist.initialization.joblist", " " + jobList);
      if (SimpleScheduler.isJobScheduled(MAILING_LIST_JOB_NAME)) {
        SimpleScheduler.unscheduleJob(MAILING_LIST_JOB_NAME);
      }
      //SimpleScheduler.scheduleJob(checker, MAILING_LIST_JOB_NAME, getFrequency());
      JobTrigger trigger = JobTrigger.triggerEvery(getFrequency(), TimeUnit.MINUTE);
      SimpleScheduler.scheduleJob(MAILING_LIST_JOB_NAME, trigger, checker);
      List<MailingList> mailingLists = getMailingListService().listAllMailingLists();
      SilverTrace.info("mailingList", "MailCheckerInitialize.Initialize",
          "mailinglist.initialization.existing.lists", " " + mailingLists.size());
      for (MailingList list : mailingLists) {
        SilverTrace.info("mailingList", "MailCheckerInitialize.Initialize",
            "mailinglist.initialization.start", " : " + list.getSubscribedAddress() + " " 
            + list.getDescription());
        MailingListComponent component = new MailingListComponent(list.getComponentId());
        checker.addMessageListener(component);
      }
    } catch (SchedulerException e) {
      SilverTrace.error("mailingList", "MailCheckerInitialize.Initialize",
          "mailinglist.initialization.error", e);
    }
  }
}
