package com.silverpeas.silvercrawler.control;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.silverpeas.silvercrawler.model.SilverCrawlerRuntimeException;
import com.stratelia.silverpeas.scheduler.SchedulerEvent;
import com.stratelia.silverpeas.scheduler.SchedulerEventHandler;
import com.stratelia.silverpeas.scheduler.SimpleScheduler;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.RepositoryIndexer;

public class ScheduledIndexFiles implements SchedulerEventHandler {

  public static final String SILVERCRAWLERENGINE_JOB_NAME = "SilverCrawlerEngineJob";

  private ResourceLocator resources = new ResourceLocator(
      "com.silverpeas.silvercrawler.settings.silverCrawlerSettings", "");

  public void initialize() {
    try {
      String cron = resources.getString("cronScheduledIndex");
      Vector jobList = SimpleScheduler.getJobList(this);
      if (jobList != null && jobList.size() > 0)
        SimpleScheduler.removeJob(this, SILVERCRAWLERENGINE_JOB_NAME);
      SimpleScheduler.getJob(this, SILVERCRAWLERENGINE_JOB_NAME, cron, this,
          "doScheduledIndex");
    } catch (Exception e) {
      SilverTrace.error("silverCrawler", "ScheduledIndexFiles.initialize()",
          "silverCrawler.EX_CANT_INIT_SCHEDULED_INDEX_FILES", e);
    }
  }

  public void handleSchedulerEvent(SchedulerEvent aEvent) {
    switch (aEvent.getType()) {
      case SchedulerEvent.EXECUTION_NOT_SUCCESSFULL:
        SilverTrace.error("silverCrawler",
            "ScheduledIndexFiles.handleSchedulerEvent", "The job '"
                + aEvent.getJob().getJobName() + "' was not successfull");
        break;
      case SchedulerEvent.EXECUTION_SUCCESSFULL:
        SilverTrace.debug("silverCrawler",
            "ScheduledIndexFiles.handleSchedulerEvent", "The job '"
                + aEvent.getJob().getJobName() + "' was successfull");
        break;
      default:
        SilverTrace.error("silverCrawler",
            "ScheduledIndexFiles.handleSchedulerEvent", "Illegal event type");
        break;
    }
  }

  public void doScheduledIndex(Date date) {
    SilverTrace.info("silverCrawler", "ScheduledIndexFiles.doScheduledIndex()",
        "root.MSG_GEN_ENTER_METHOD");

    try {
      // indexation des fichiers du composant
      OrganizationController orga = new OrganizationController();
      String[] instanceIds = orga.getCompoId("silverCrawler");
      for (int i = 0; instanceIds != null && i < instanceIds.length; i++) {
        ComponentInst instance = orga.getComponentInst("silverCrawler"
            + instanceIds[i]);
        boolean periodicIndex = "yes".equals(instance
            .getParameterValue("periodicIndex"));
        if (periodicIndex) {
          RepositoryIndexer repositoryIndexer = new RepositoryIndexer(null,
              instance.getId());

          List profiles = new ArrayList();
          profiles.add("admin");
          String[] adminIds = orga.getUsersIdsByRoleNames(instance.getId(),
              profiles);

          String adminId = "0";
          if (adminIds != null && adminIds.length > 0)
            adminId = adminIds[0];

          String pathRepository = instance.getParameterValue("directory");

          if (!pathRepository.endsWith(File.separator))
            pathRepository += File.separator;
          Date dateIndex = new Date();
          repositoryIndexer.pathIndexer(pathRepository, dateIndex.toString(),
              adminId, "add");
        }
      }
    } catch (Exception e) {
      throw new SilverCrawlerRuntimeException(
          "ScheduledIndexFiles.doScheduledIndex()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }

    SilverTrace.info("silverCrawler", "ScheduledIndexFiles.doScheduledIndex()",
        "root.MSG_GEN_EXIT_METHOD");
  }
}