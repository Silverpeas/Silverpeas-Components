/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.dataWarning.control;

import com.silverpeas.dataWarning.model.DataWarning;
import com.silverpeas.dataWarning.model.DataWarningGroup;
import com.silverpeas.dataWarning.model.DataWarningQueryResult;
import com.silverpeas.dataWarning.model.DataWarningResult;
import com.silverpeas.dataWarning.model.DataWarningUser;
import com.silverpeas.scheduler.ScheduledJob;
import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerEvent;
import com.silverpeas.scheduler.SchedulerEventListener;
import com.silverpeas.scheduler.SchedulerProvider;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.core.admin.OrganizationControllerProvider;
import org.silverpeas.util.ResourceLocator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class DataWarningSchedulerImpl implements SchedulerEventListener {

  public static final String DATAWARNING_JOB_NAME = "DataWarning";
  private String instanceId = "";
  private DataWarningEngine dataWarningEngine = null;
  private String[] idAllUniqueUsers = new String[0];
  private ScheduledJob theJob = null;
  private Scheduler scheduler = null;
  private String jobName = null;
  private ResourceLocator messages =
      new ResourceLocator("com.silverpeas.dataWarning.multilang.dataWarning", "");

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

      this.scheduler = SchedulerProvider.getScheduler();
    } catch (Exception e) {
      SilverTrace.error("dataWarning", "DataWarning_TimeoutManagerImpl.initialize()", "", e);
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
      theJob = scheduler.scheduleJob(jobName, trigger, this);
      dataWarningEngine.updateSchedulerWakeUp(theJob.getNexExecutionTimeInMillis());
    } catch (Exception e) {
      SilverTrace.error("dataWarning", "DataWarning_TimeoutManagerImpl.start()", "", e);
    }
  }

  /**
   * Stops this scheduler. No scans on data warning will be more performed.
   */
  public void stop() {
    try {
      scheduler.unscheduleJob(jobName);
    } catch (Exception ex) {
      SilverTrace.error("dataWarning", "DataWarning_TimeoutManagerImpl.stop()", "", ex);
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
    SilverTrace.info("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()",
        "root.MSG_GEN_ENTER_METHOD", "hasError=" + dwr.hasError() + "-" + dwr.getQueryResult().
            getErrorFullText());
    if (!dwr.hasError()) {
      try {
        OrganizationController oc = OrganizationControllerProvider.getOrganisationController();
        StringBuilder msgForManager = new StringBuilder();
        DataWarningQueryResult dwqr = dwr.getQueryResult();
        String descriptionRequete = dwr.getDataQuery().getDescription();
        SilverTrace.info("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()",
            "root.MSG_GEN_PARAM_VALUE", "descriptionRequete=" + descriptionRequete);
        StringBuilder msgToSend = new StringBuilder();
        int nbRowMax = dataWarningEngine.getDataWarning().getRowLimit();
        //Request Description
        if (!descriptionRequete.equals("")) {
          msgToSend.append(descriptionRequete).append("\n\n");
        }

        //Notification for the Managers:
        List<String> managerDestIds = new ArrayList<>();
        List<String> profilesList = new ArrayList<>();
        profilesList.add("admin");
        profilesList.add("publisher");
        String[] managerIds = oc.getUsersIdsByRoleNames(instanceId, profilesList);
        SilverTrace.info("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()",
            "root.MSG_GEN_PARAM_VALUE", "managerIds :" + managerIds.length + " - " +
                Arrays.toString(managerIds));

        //Inconditional Query Type
        if (dataWarningEngine.getDataWarning().getAnalysisType() ==
            DataWarning.INCONDITIONAL_QUERY) {
          for (final String idAllUniqueUser : idAllUniqueUsers) {
            String resultForMessage = buildResultForMessage(dwqr, nbRowMax, idAllUniqueUser);
            if (!resultForMessage.equals("")) {
              //Personalized Query
              if (dwqr.isPersoEnabled()) {
                String userPersoValue = dwqr.returnPersoValue(idAllUniqueUser);
                UserDetail userDetail = oc.getUserDetail(idAllUniqueUser);
                msgForManager.append(messages.getString("separateurUserMail")).append(userDetail.
                    getDisplayedName()).append(" (").append(userPersoValue).append(") :\n\n");
                msgForManager.append(resultForMessage).append("\n\n");
              }
              sendMessage(messages.getString("titreMail"), msgToSend.toString() + resultForMessage,
                  idAllUniqueUser);
            }
            //We only send a notification for managers who have subscribed.
            for (final String managerId : managerIds) {
              if (managerId.equals(idAllUniqueUser)) {
                managerDestIds.add(managerId);
              }
              SilverTrace
                  .info("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()",
                      "root.MSG_GEN_PARAM_VALUE", "managerIds[i]=" + managerId);
            }
          }
        } else if (dataWarningEngine.getDataWarning().getAnalysisType() ==
            DataWarning.TRIGGER_ANALYSIS) {
          //Conditional Query Type (Trigger)
          for (final String idAllUniqueUser : idAllUniqueUsers) {
            StringBuilder msgByUser = new StringBuilder();
            SilverTrace
                .debug("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()",
                    "root.MSG_GEN_PARAM_VALUE", "Nb Rows = " + dwqr.getNbRows(idAllUniqueUser));
            if (dwr.getTriggerEnabled(idAllUniqueUser)) {
              SilverTrace
                  .debug("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()",
                      "root.MSG_GEN_PARAM_VALUE", "idAllUniqueUsers[j]=" + idAllUniqueUser);
              msgByUser.append(messages.getString("resultatSeuilValeur")).append(" : ").append(dwr.
                  getTriggerActualValue(idAllUniqueUser)).append("\n\n");
              msgByUser.append(buildResultForMessage(dwqr, nbRowMax, idAllUniqueUser));
              sendMessage(messages.getString("titreMail"),
                  msgToSend.toString() + msgByUser.toString(), idAllUniqueUser);
              //For Managers only:
              String userPersoValue = dwqr.returnPersoValue(idAllUniqueUser);
              UserDetail userDetail = oc.getUserDetail(idAllUniqueUser);
              msgForManager.append(messages.getString("separateurUserMail")).append(userDetail.
                  getDisplayedName()).append(" (").append(userPersoValue).append(") :");
              msgForManager.append(msgByUser).append("\n\n");
            }
            for (final String managerId : managerIds) {
              if (managerId.equals(idAllUniqueUser)) {
                managerDestIds.add(managerId);
              }
              SilverTrace
                  .info("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()",
                      "root.MSG_GEN_PARAM_VALUE", "managerIds[i]=" + managerId);
            }
          }
        }

        // Notification for the Managers:
        for (int i = 0; i < managerDestIds.size(); i++) {
          SilverTrace.info("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()",
              "root.MSG_GEN_PARAM_VALUE", "managerId :" + managerIds[i]);
          sendMessage(messages.getString("titreMail"),
              msgToSend.toString() + msgForManager.toString(), managerDestIds.get(i));
        }

        // Re-init the WakeUp time to the next wake time
        dataWarningEngine.updateSchedulerWakeUp(theJob.getNexExecutionTimeInMillis());
      } catch (Exception e) {
        SilverTrace.warn("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()",
            "root.MSG_GEN_ENTER_METHOD", "hasError", e);
      }
    }
  }

  private String buildResultForMessage(DataWarningQueryResult dwqr, int nbRowMax, String userId) {
    StringBuilder msgToSend = new StringBuilder();
    String userPersoValue = dwqr.returnPersoValue(userId);

    List cols = dwqr.getColumns(userId);
    int nbCols = cols.size();
    SilverTrace.info("dataWarning", "DataWarningSchedulerImpl.buildResultForMessage()",
        "root.MSG_GEN_ENTER_METHOD", "nbCols=" + nbCols);
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
    SilverTrace.info("dataWarning", "DataWarningSchedulerImpl.buildResultForMessage()",
        "root.MSG_GEN_PARAM_VALUE", "msgToSend=" + msgToSend);
    List vals = dwqr.getValues(userId);
    for (int j = 0; (j < vals.size()) && ((nbRowMax <= 0) || (j < nbRowMax)); j++) {
      ArrayList theRow = (ArrayList) vals.get(j);
      //Do not send persoColumn if necessary
      if (dwqr.isPersoEnabled() && theRow.get(dwqr.getPersoColumnNumber()).equals(userPersoValue)) {
        SilverTrace.debug("dataWarning", "DataWarningSchedulerImpl.buildResultForMessage()",
            "root.MSG_GEN_PARAM_VALUE", "dwqr.getPersoColumnNumber()=" + dwqr.
                getPersoColumnNumber() + " userPersoValue=" + userPersoValue);
        theRow.remove(dwqr.getPersoColumnNumber());
      }
      for (int k = 0; k < nbCols; k++) {
        SilverTrace.debug("dataWarning", "DataWarningSchedulerImpl.buildResultForMessage()",
            "root.MSG_GEN_PARAM_VALUE", "theRow=" + theRow);
        msgToSend.append((String) theRow.get(k));
        SilverTrace.debug("dataWarning", "DataWarningSchedulerImpl.buildResultForMessage()",
            "root.MSG_GEN_PARAM_VALUE", "msgToSend boucle=" + msgToSend);
        if (k + 1 < nbCols) {
          msgToSend.append(" | ");
        }
      }
      msgToSend.append("\n");
    }
    if (vals.isEmpty()) {
      return "";
    } else {
      return msgToSend.toString();
    }
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
      SilverTrace.error("dataWarning", "DataWarning_TimeoutManagerImpl.sendMessage()",
          "Envoi impossible de la notification pour l'instanceId " + instanceId, e);
    }
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) throws Exception {
    SilverTrace.debug("dataWarning", "DataWarning_TimeoutManagerImpl.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' is starting");
    doDataWarningSchedulerImpl();
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    SilverTrace.debug("dataWarning", "DataWarning_TimeoutManagerImpl.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was successfull");
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    SilverTrace.error("dataWarning", "DataWarning_TimeoutManagerImpl.handleSchedulerEvent",
        "The job '" + anEvent.getJobExecutionContext().getJobName() + "' was not successfull");
  }
}