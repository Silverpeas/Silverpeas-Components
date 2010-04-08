package com.silverpeas.dataWarning.control;

import java.util.Hashtable;
import com.stratelia.silverpeas.scheduler.SimpleScheduler;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class DataWarningSchedulerTable
{
	private static Hashtable schedulers = new Hashtable();

	/**
	 * Add a scheduler
	 */
	public static void addScheduler(String instanceId)
	{
		SilverTrace.info("dataWarning", "DataWarningSchedulerTable.addScheduler()", "root.MSG_GEN_PARAM_VALUE", "Add Scheduler : " + instanceId);
		//test if connecteur already exist
		DataWarningSchedulerImpl theScheduler = getScheduler(instanceId);
		if (theScheduler != null)
			removeScheduler(instanceId);
		else
			theScheduler = new DataWarningSchedulerImpl(instanceId);
		
		//add the scheduler
		schedulers.put(instanceId, theScheduler);
		
		//launch the scheduler
		theScheduler.initialize();
	}
	
	/**
	 * Remove a scheduler with the instance id
	 */
	public static void removeScheduler(String instanceId)
	{
		SilverTrace.info("dataWarning", "DataWarningSchedulerTable.removeScheduler()", "root.MSG_GEN_PARAM_VALUE", "Remove Scheduler : " + instanceId);
		try
		{
			DataWarningSchedulerImpl theScheduler = getScheduler(instanceId);
			if (theScheduler != null)
			{
				SimpleScheduler.removeJob(getScheduler(instanceId), DataWarningSchedulerImpl.DATAWARNING_JOB_NAME + instanceId);
				schedulers.remove(instanceId);
			}
		}
		catch(Exception e)
		{
			SilverTrace.error("dataWarning", "DataWarningSchedulerTable.removeScheduler()", "", e);
		}
	}
	
	/**
	 * Get the scheduler associated with an instance
	 */
	public static DataWarningSchedulerImpl getScheduler(String instanceId)
	{
		return (DataWarningSchedulerImpl) schedulers.get(instanceId);
	}
};