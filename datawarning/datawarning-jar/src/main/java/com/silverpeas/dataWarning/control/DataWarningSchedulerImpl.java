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

package com.silverpeas.dataWarning.control;

import java.util.*;

import com.silverpeas.dataWarning.model.*;
import com.stratelia.silverpeas.scheduler.*;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.notificationManager.*;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;

public class DataWarningSchedulerImpl implements SchedulerEventHandler
{
    public static final String DATAWARNING_JOB_NAME = "DataWarning";

	private String instanceId = "";
	private DataWarningEngine dataWarningEngine = null;
	private String[] idAllUniqueUsers = new String[0];
	private String[] idUsers = new String[0];
	private String[] idGroups = new String[0];
	private SchedulerJob theJob = null;

	private ResourceLocator messages = new ResourceLocator("com.silverpeas.dataWarning.multilang.dataWarning", "");

	public DataWarningSchedulerImpl(String compoId)
	{
		OrganizationController oc = new OrganizationController();
		this.instanceId = compoId;
		HashSet hs = new HashSet();
		try
		{
			Collection theCol;
			Iterator it;
			int i;
			Group gr;
			String[] uids;

			// Get main classes
			dataWarningEngine = new DataWarningEngine(compoId);
			//load idGroups
			theCol = dataWarningEngine.getDataWarningGroups();
			idGroups = new String[theCol.size()];
			it = theCol.iterator();
			for (i = 0; i < theCol.size(); i++)
			{
				idGroups[i] = Integer.toString(((DataWarningGroup)it.next()).getGroupId());
				gr = oc.getGroup(idGroups[i]);
				uids = gr.getUserIds();
				for (int j = 0; j < uids.length; j++)
				{
					hs.add(uids[j]);
				}
			}
			//load idUsers
			theCol = dataWarningEngine.getDataWarningUsers();
			idUsers = new String[theCol.size()];
			it = theCol.iterator();
			for (i = 0; i < theCol.size(); i++)
			{
				idUsers[i] = Integer.toString(((DataWarningUser)it.next()).getUserId());
				hs.add(idUsers[i]);
			}
			idAllUniqueUsers = (String[])hs.toArray(new String[0]);
		}
		catch (Exception e)
		{
			SilverTrace.error("dataWarning", "DataWarning_TimeoutManagerImpl.initialize()", "", e);
		}
	}

	public void initialize()
	{
		try
		{
			Vector jobList = SimpleScheduler.getJobList(this);
			if (jobList != null && jobList.size() > 0)
			{
				SimpleScheduler.removeJob(this, DATAWARNING_JOB_NAME + instanceId);
			}
			theJob = SimpleScheduler.getJob(this, DATAWARNING_JOB_NAME + instanceId, dataWarningEngine.getDataWarningScheduler().createCronString(), this, "doDataWarningSchedulerImpl", dataWarningEngine.getDataWarningScheduler().getWakeUp());
			if (dataWarningEngine.getDataWarningScheduler().getWakeUp() == 0)
			{
				// Re-init the WakeUp time to the next wake time
				dataWarningEngine.updateSchedulerWakeUp(theJob.readNextTimeStamp());
			}
		}
		catch (Exception e)
		{
			SilverTrace.error("dataWarning", "DataWarning_TimeoutManagerImpl.initialize()", "", e);
		}
	}

    public void handleSchedulerEvent(SchedulerEvent aEvent)
    {
        switch (aEvent.getType())
        {
			case SchedulerEvent.EXECUTION_NOT_SUCCESSFULL:
				SilverTrace.error("dataWarning", "DataWarning_TimeoutManagerImpl.handleSchedulerEvent", "The job '" + aEvent.getJob().getJobName() + "' was not successfull");
	            break;

			case SchedulerEvent.EXECUTION_SUCCESSFULL:
	            SilverTrace.debug("dataWarning", "DataWarning_TimeoutManagerImpl.handleSchedulerEvent", "The job '" + aEvent.getJob().getJobName() + "' was successfull");
	            break;

			default:
	            SilverTrace.error("dataWarning", "DataWarning_TimeoutManagerImpl.handleSchedulerEvent", "Illegal event type");
	            break;
        }
    }

    /**
     * This method is called periodically by the scheduler,
     * it test for each peas of type DataWarning
     * if associated model contains states with timeout events
     * If so, all the instances of these peas that have the "timeout" states actives
	 * are read to check if timeout interval has been reached.
     * In that case, the administrator can be notified, the active state and the instance are marked as timeout
     *
     * @param currentDate   the date when the method is called by the scheduler
     *
     * @see SimpleScheduler for parameters,
     */
	public synchronized void doDataWarningSchedulerImpl(java.util.Date date)
	{
		DataWarningResult dwr = dataWarningEngine.run();
        SilverTrace.info("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()","root.MSG_GEN_ENTER_METHOD","hasError=" + dwr.hasError() + "-" + dwr.getQueryResult().getErrorFullText());
		if (!dwr.hasError())
		{
			try
			{
				OrganizationController oc = new OrganizationController();
				StringBuffer msgForManager = new StringBuffer();
				DataWarningQueryResult dwqr = dwr.getQueryResult();
				String descriptionRequete = dwr.getDataQuery().getDescription();
				SilverTrace.info("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()","root.MSG_GEN_PARAM_VALUE","descriptionRequete="+descriptionRequete);
				StringBuffer msgToSend = new StringBuffer();
				int nbRowMax = dataWarningEngine.getDataWarning().getRowLimit();
				//Request Description
				if (!descriptionRequete.equals(""))
					msgToSend.append(descriptionRequete + "\n\n");

				//Notification for the Managers:
				ArrayList managerDestIds = new ArrayList();
				ArrayList profilesList = new ArrayList();
				profilesList.add("admin");
				profilesList.add("publisher");
				String[] managerIds = oc.getUsersIdsByRoleNames(instanceId, profilesList);
				SilverTrace.info("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()","root.MSG_GEN_PARAM_VALUE","managerIds :"+managerIds.length+" - "+managerIds.toString());

				//Inconditional Query Type
				if(dataWarningEngine.getDataWarning().getAnalysisType() == DataWarning.INCONDITIONAL_QUERY)
				{
					for (int j = 0; j < idAllUniqueUsers.length; j++)
					{
						String resultForMessage = buildResultForMessage(dwqr, nbRowMax, idAllUniqueUsers[j]);
						if (!resultForMessage.equals(""))
						{
							//Personalized Query
							if (dwqr.isPersoEnabled())
							{
								String userPersoValue = dwqr.returnPersoValue(idAllUniqueUsers[j]);
								UserDetail userDetail = oc.getUserDetail(idAllUniqueUsers[j]);
								msgForManager.append(messages.getString("separateurUserMail")+userDetail.getDisplayedName()+" ("+userPersoValue+") :\n\n");
								msgForManager.append(resultForMessage+"\n\n");
							}
							sendMessage(messages.getString("titreMail"), msgToSend.toString() + resultForMessage , idAllUniqueUsers[j]);
						}
						//We only send a notification for managers who have subscribed.
						for (int i=0; i<managerIds.length; i++)
						{
							if (managerIds[i].equals(idAllUniqueUsers[j]))
								managerDestIds.add(managerIds[i]);
							SilverTrace.info("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()","root.MSG_GEN_PARAM_VALUE","managerIds[i]="+managerIds[i]);
						}
					}
				}
				//Conditional Query Type (Trigger)
				else if(dataWarningEngine.getDataWarning().getAnalysisType() == DataWarning.TRIGGER_ANALYSIS)
				{
					for (int j = 0; j < idAllUniqueUsers.length; j++)
					{
						StringBuffer msgByUser = new StringBuffer();
						SilverTrace.debug("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()","root.MSG_GEN_PARAM_VALUE","Nb Rows = "+dwqr.getNbRows(idAllUniqueUsers[j]));
						if (dwr.getTriggerEnabled(idAllUniqueUsers[j]))
						{
							SilverTrace.debug("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()","root.MSG_GEN_PARAM_VALUE","idAllUniqueUsers[j]="+idAllUniqueUsers[j]);
							//msgByUser.append("\n"+messages.getString("resultatSeuil") + " " + dwr.getConditionDisplayedString(messages) + " " + dwr.getTrigger() + ".\n\n");
							msgByUser.append(messages.getString("resultatSeuilValeur") + " : " + dwr.getTriggerActualValue(idAllUniqueUsers[j]) + "\n\n");
							msgByUser.append(buildResultForMessage(dwqr, nbRowMax, idAllUniqueUsers[j]));
							sendMessage(messages.getString("titreMail"), msgToSend.toString() + msgByUser.toString(), idAllUniqueUsers[j]);
							//For Managers only:
							String userPersoValue = dwqr.returnPersoValue(idAllUniqueUsers[j]);
							UserDetail userDetail = oc.getUserDetail(idAllUniqueUsers[j]);
							msgForManager.append(messages.getString("separateurUserMail")+userDetail.getDisplayedName()+" ("+userPersoValue+") :");
							msgForManager.append(msgByUser+"\n\n");
						}
						for (int i=0; i<managerIds.length; i++)
						{
							if (managerIds[i].equals(idAllUniqueUsers[j]))
								managerDestIds.add(managerIds[i]);
							SilverTrace.info("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()","root.MSG_GEN_PARAM_VALUE","managerIds[i]="+managerIds[i]);
						}
					}
				}


				//Notification for the Managers:
				for (int i=0; i<managerDestIds.size(); i++)
				{
					SilverTrace.info("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()","root.MSG_GEN_PARAM_VALUE","managerId :"+managerIds[i]);
					sendMessage(messages.getString("titreMail"), msgToSend.toString() + msgForManager.toString(), (String) managerDestIds.get(i));
				}

				// Re-init the WakeUp time to the next wake time
				dataWarningEngine.updateSchedulerWakeUp(theJob.readNextTimeStamp());
			}
			catch (Exception e)
			{
		        SilverTrace.warn("dataWarning", "DataWarningSchedulerImpl.doDataWarningSchedulerImpl()","root.MSG_GEN_ENTER_METHOD","hasError",e);
			}
		}
	}

    private String buildResultForMessage(DataWarningQueryResult dwqr, int nbRowMax, String userId)
    {
		StringBuffer msgToSend = new StringBuffer();
		String userPersoValue = dwqr.returnPersoValue(userId);

		ArrayList cols = dwqr.getColumns(userId);
		int nbCols = cols.size();
		SilverTrace.info("dataWarning", "DataWarningSchedulerImpl.buildResultForMessage()","root.MSG_GEN_ENTER_METHOD","nbCols="+nbCols);
		Iterator it = cols.iterator();

		while (it.hasNext())
		{
			msgToSend.append((String)it.next());
			if (it.hasNext())
				msgToSend.append(" | ");
		}
		msgToSend.append("\n");

		int msgToSendLength = msgToSend.toString().length();
		for (int i=0; i<msgToSendLength; i++)
			msgToSend.append("-");

		msgToSend.append("\n");
		SilverTrace.info("dataWarning", "DataWarningSchedulerImpl.buildResultForMessage()","root.MSG_GEN_PARAM_VALUE","msgToSend="+msgToSend);
		ArrayList vals = dwqr.getValues(userId);
		for (int j=0; (j < vals.size()) && ((nbRowMax <= 0) || (j < nbRowMax)); j++)
		{
			ArrayList theRow = (ArrayList)vals.get(j);
			//Do not send persoColumn if necessary
			if (dwqr.isPersoEnabled() && theRow.get(dwqr.getPersoColumnNumber()).equals(userPersoValue))
			{
				SilverTrace.debug("dataWarning", "DataWarningSchedulerImpl.buildResultForMessage()","root.MSG_GEN_PARAM_VALUE","dwqr.getPersoColumnNumber()="+dwqr.getPersoColumnNumber()+" userPersoValue="+userPersoValue);
				theRow.remove(dwqr.getPersoColumnNumber());
			}
			for (int k=0; k<nbCols; k++)
			{
				SilverTrace.debug("dataWarning", "DataWarningSchedulerImpl.buildResultForMessage()","root.MSG_GEN_PARAM_VALUE","theRow="+theRow);
				msgToSend.append((String)theRow.get(k));
				SilverTrace.debug("dataWarning", "DataWarningSchedulerImpl.buildResultForMessage()","root.MSG_GEN_PARAM_VALUE","msgToSend boucle="+msgToSend);
				if (k+1 < nbCols)
					msgToSend.append(" | ");
			}
			msgToSend.append("\n");
		}
		if (vals.isEmpty())
			return "";
    	else
		    return msgToSend.toString();
    }

	private void sendMessage(String title, String msgToSend, String uid)	{
		try
		{
			NotificationMetaData notificationMetaData = new NotificationMetaData(NotificationParameters.NORMAL, title, msgToSend);
			notificationMetaData.addUserRecipient(uid);
			notificationMetaData.setSender("0");

			NotificationSender notificationSender = new NotificationSender(instanceId);
			notificationSender.notifyUser(notificationMetaData);
		}
		catch (Exception e)
		{
			SilverTrace.error("dataWarning", "DataWarning_TimeoutManagerImpl.sendMessage()", "Envoi impossible de la notification pour l'instanceId " + instanceId, e);
		}
	}
}