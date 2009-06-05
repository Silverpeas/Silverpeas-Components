package com.silverpeas.mailinglist.control;

import java.util.List;
import java.util.Vector;

import com.silverpeas.mailinglist.model.MailingListComponent;
import com.silverpeas.mailinglist.service.job.MessageChecker;
import com.silverpeas.mailinglist.service.model.MailingListService;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.stratelia.silverpeas.scheduler.SchedulerException;
import com.stratelia.silverpeas.scheduler.SimpleScheduler;
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

  @SuppressWarnings("unchecked")
  public void registerAll() {
    SilverTrace.info("mailingList", "MailCheckerInitialize.Initialize",
        "mailinglist.initialization.start");
    MessageChecker checker = getMessageChecker();
    try {
      SilverTrace.info("mailingList", "MailCheckerInitialize.Initialize",
          "mailinglist.initialization.start", " " + checker);
      Vector jobList = SimpleScheduler.getJobList(checker);
      SilverTrace.info("mailingList", "MailCheckerInitialize.Initialize",
          "mailinglist.initialization.joblist", " " + jobList);
      if (jobList != null && jobList.size() > 0) {
        SimpleScheduler.removeJob(checker, MAILING_LIST_JOB_NAME);
      }
      SimpleScheduler.getJob(checker, MAILING_LIST_JOB_NAME, getFrequency());
      List<MailingList> mailingLists = getMailingListService().listAllMailingLists();
      SilverTrace.info("mailingList", "MailCheckerInitialize.Initialize",
          "mailinglist.initialization.existing.lists", " "
              + mailingLists.size());
      for (MailingList list : mailingLists) {
        SilverTrace.info("mailingList", "MailCheckerInitialize.Initialize",
            "mailinglist.initialization.start", " : "
                + list.getSubscribedAddress() + " " + list.getDescription());
        MailingListComponent component = new MailingListComponent(list
            .getComponentId());
        checker.addMessageListener(component);
      }
    } catch (SchedulerException e) {
      SilverTrace.error("mailingList", "MailCheckerInitialize.Initialize",
          "mailinglist.initialization.error", e);
    }
  }
}
