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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.datawarning.service;

import org.silverpeas.components.datawarning.model.DataWarning;
import org.silverpeas.components.datawarning.model.DataWarningGroup;
import org.silverpeas.components.datawarning.model.DataWarningQueryResult;
import org.silverpeas.components.datawarning.model.DataWarningResult;
import org.silverpeas.components.datawarning.model.DataWarningUser;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.ScheduledJob;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class DataWarningSchedulerImpl {

  public static final String DATAWARNING_JOB_NAME = "DataWarning";
  private static final String MAIL_TITLE_KEY = "titreMail";
  private String instanceId = "";
  private DataWarningEngine dataWarningEngine = null;
  private String[] idAllUniqueUsers = new String[0];
  private ScheduledJob theJob = null;
  private Scheduler scheduler = null;
  private String jobName = null;
  private LocalizationBundle messages =
      ResourceLocator.getLocalizationBundle("org.silverpeas.dataWarning.multilang.dataWarning");

  public DataWarningSchedulerImpl(String instanceId) {
    OrganizationController oc = OrganizationControllerProvider.getOrganisationController();
    this.instanceId = instanceId;
    this.jobName = DATAWARNING_JOB_NAME + this.instanceId;
    HashSet<String> hs = new HashSet<>();
    try {
      // Get main classes
      dataWarningEngine = new DataWarningEngine(instanceId);

      //load user identifiers from group
      Collection<DataWarningGroup> dataWarningGroups = dataWarningEngine.getDataWarningGroups();
      for (DataWarningGroup dataWarningGroup : dataWarningGroups) {
        String idGroup = Integer.toString(dataWarningGroup.getGroupId());
        Group gr = oc.getGroup(idGroup);
        hs.addAll(Arrays.asList(gr.getUserIds()));
      }

      // load user identifiers
      Collection<DataWarningUser> dataWarningUsers = dataWarningEngine.getDataWarningUsers();
      for (DataWarningUser dataWarningUser : dataWarningUsers) {
        hs.add(Integer.toString(dataWarningUser.getUserId()));
      }
      idAllUniqueUsers = hs.toArray(new String[idAllUniqueUsers.length]);

      this.scheduler = SchedulerProvider.getVolatileScheduler();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  /**
   * Starts the schedule of the scan and processing of data warning.
   */
  public void start() {
    try {
      if (scheduler.isJobScheduled(jobName)) {
        scheduler.unscheduleJob(jobName);
      }
      String cronExpression = dataWarningEngine.getDataWarningScheduler().createCronString();
      Date startDate = new Date(dataWarningEngine.getDataWarningScheduler().getWakeUp());
      JobTrigger trigger = JobTrigger.triggerAt(cronExpression);
      if (startDate.after(new Date())) {
        trigger.startAt(startDate);
      }
      theJob = scheduler.scheduleJob(new Job(jobName) {
        @Override
        public void execute(final JobExecutionContext context) {
          doDataWarningSchedulerImpl();
        }
      }, trigger);
      dataWarningEngine.updateSchedulerWakeUp(theJob.getNextExecutionTime().getTime());
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  /**
   * Stops this scheduler. No scans on data warning will be more performed.
   */
  public void stop() {
    try {
      scheduler.unscheduleJob(jobName);
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex);
    }
  }

  /**
   * This method is called periodically by the scheduler,
   * it test for each peas of type DataWarning
   * if associated model contains states with timeout events
   * If so, all the instances of these peas that have the "timeout" states actives
   * are read to check if timeout interval has been reached.
   * In that case, the administrator can be notified, the active state and the instance are marked
   * as timeout.
   */
  public synchronized void doDataWarningSchedulerImpl() {
    DataWarningResult dwr = dataWarningEngine.run();
    if (!dwr.hasError()) {
      try {
        OrganizationController oc = OrganizationControllerProvider.getOrganisationController();
        StringBuilder msgForManager = new StringBuilder();
        DataWarningQueryResult dwqr = dwr.getQueryResult();
        String descriptionRequete = dwr.getDataQuery().getDescription();

        StringBuilder msgToSend = new StringBuilder();
        int nbRowMax = dataWarningEngine.getDataWarning().getRowLimit();
        //Request Description
        if (!descriptionRequete.isEmpty()) {
          msgToSend.append(descriptionRequete).append("\n\n");
        }

        //Notification for the Managers:
        List<String> managerDestIds = new ArrayList<>();
        List<String> profilesList = new ArrayList<>();
        profilesList.add("admin");
        profilesList.add("publisher");
        String[] managerIds = oc.getUsersIdsByRoleNames(instanceId, profilesList);
        //Inconditional Query Type
        if (dataWarningEngine.getDataWarning().getAnalysisType() ==
            DataWarning.INCONDITIONAL_QUERY) {
          processInconditionalQuery(msgForManager, dwqr, msgToSend, nbRowMax, managerDestIds,
              managerIds);
        } else if (dataWarningEngine.getDataWarning().getAnalysisType() ==
            DataWarning.TRIGGER_ANALYSIS) {
          processTriggerAnalysis(dwr, msgForManager, dwqr, msgToSend, nbRowMax, managerDestIds,
              managerIds);

        }

        // Notification for the Managers:
        for (int i = 0; i < managerDestIds.size(); i++) {

          sendMessage(messages.getString(MAIL_TITLE_KEY),
              msgToSend.toString() + msgForManager.toString(), managerDestIds.get(i));
        }

        // Re-init the WakeUp time to the next wake time
        dataWarningEngine.updateSchedulerWakeUp(theJob.getNextExecutionTime().getTime());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  private void processTriggerAnalysis(final DataWarningResult dwr,
      final StringBuilder msgForManager, final DataWarningQueryResult dwqr,
      final StringBuilder msgToSend, final int nbRowMax, final List<String> managerDestIds,
      final String[] managerIds) {
    //Conditional Query Type (Trigger)
    for (final String idAllUniqueUser : idAllUniqueUsers) {
      StringBuilder msgByUser = new StringBuilder();
      if (dwr.getTriggerEnabled(idAllUniqueUser)) {
        msgByUser.append(messages.getString("resultatSeuilValeur")).append(" : ").append(dwr.
            getTriggerActualValue(idAllUniqueUser)).append("\n\n");
        msgByUser.append(buildResultForMessage(dwqr, nbRowMax, idAllUniqueUser));
        sendMessage(messages.getString(MAIL_TITLE_KEY), msgToSend.toString() + msgByUser.toString(),
            idAllUniqueUser);
        //For Managers only:
        String userPersoValue = dwqr.returnPersoValue(idAllUniqueUser);
        UserDetail userDetail = UserDetail.getById(idAllUniqueUser);
        msgForManager.append(messages.getString("separateurUserMail")).append(userDetail.
            getDisplayedName()).append(" (").append(userPersoValue).append(") :");
        msgForManager.append(msgByUser).append("\n\n");
      }
      for (final String managerId : managerIds) {
        if (managerId.equals(idAllUniqueUser)) {
          managerDestIds.add(managerId);
        }
      }
    }
  }

  private void processInconditionalQuery(final StringBuilder msgForManager,
      final DataWarningQueryResult dwqr, final StringBuilder msgToSend, final int nbRowMax,
      final List<String> managerDestIds, final String[] managerIds) {
    for (final String idAllUniqueUser : idAllUniqueUsers) {
      String resultForMessage = buildResultForMessage(dwqr, nbRowMax, idAllUniqueUser);
      if (!resultForMessage.isEmpty()) {
        //Personalized Query
        if (dwqr.isPersoEnabled()) {
          String userPersoValue = dwqr.returnPersoValue(idAllUniqueUser);
          UserDetail userDetail = UserDetail.getById(idAllUniqueUser);
          msgForManager.append(messages.getString("separateurUserMail")).append(userDetail.
              getDisplayedName()).append(" (").append(userPersoValue).append(") :\n\n");
          msgForManager.append(resultForMessage).append("\n\n");
        }
        sendMessage(messages.getString(MAIL_TITLE_KEY), msgToSend.toString() + resultForMessage,
            idAllUniqueUser);
      }
      //We only send a notification for managers who have subscribed.
      for (final String managerId : managerIds) {
        if (managerId.equals(idAllUniqueUser)) {
          managerDestIds.add(managerId);
        }
      }
    }
  }

  private String buildResultForMessage(DataWarningQueryResult dwqr, int nbRowMax, String userId) {
    StringBuilder msgToSend = new StringBuilder();
    String userPersoValue = dwqr.returnPersoValue(userId);

    List cols = dwqr.getPersoColumns();
    int nbCols = cols.size();

    setMessageColumns(msgToSend, cols);

    List vals = dwqr.getValues(userId);
    setMessageValues(dwqr, nbRowMax, msgToSend, userPersoValue, nbCols, vals);
    if (vals.isEmpty()) {
      return "";
    } else {
      return msgToSend.toString();
    }
  }

  private void setMessageValues(final DataWarningQueryResult dwqr, final int nbRowMax,
      final StringBuilder msgToSend, final String userPersoValue, final int nbCols,
      final List vals) {
    for (int j = 0; (j < vals.size()) && ((nbRowMax <= 0) || (j < nbRowMax)); j++) {
      ArrayList theRow = (ArrayList) vals.get(j);
      //Do not send persoColumn if necessary
      if (dwqr.isPersoEnabled() && theRow.get(dwqr.getPersoColumnNumber()).equals(userPersoValue)) {
        theRow.remove(dwqr.getPersoColumnNumber());
      }
      for (int k = 0; k < nbCols; k++) {
        msgToSend.append((String) theRow.get(k));
        if (k + 1 < nbCols) {
          msgToSend.append(" | ");
        }
      }
      msgToSend.append("\n");
    }
  }

  private void setMessageColumns(final StringBuilder msgToSend, final List cols) {
    Iterator it = cols.iterator();

    while (it.hasNext()) {
      msgToSend.append((String) it.next());
      if (it.hasNext()) {
        msgToSend.append(" | ");
      }
    }
    msgToSend.append("\n");

    int msgToSendLength = msgToSend.toString().length();
    for (int i = 0; i < msgToSendLength; i++) {
      msgToSend.append("-");
    }

    msgToSend.append("\n");
  }

  private void sendMessage(String title, String msgToSend, String uid) {
    try {
      NotificationMetaData notificationMetaData =
          new NotificationMetaData(NotificationParameters.NORMAL, title, msgToSend);
      notificationMetaData.addUserRecipient(new UserRecipient(uid));
      notificationMetaData.setSender("0");
      NotificationSender notificationSender = new NotificationSender(instanceId);
      notificationSender.notifyUser(notificationMetaData);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }
}