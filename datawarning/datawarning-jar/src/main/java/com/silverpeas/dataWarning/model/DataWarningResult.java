/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.dataWarning.model;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

public class DataWarningResult extends Object
{
    // Trigger static values
    private boolean triggerRequired = false;
    private long trigger = 0;
    private int triggerCondition = DataWarningQuery.TRIGGER_CONDITION_SUP;
    // Trigger dynamic values
    private long triggerActualValue = 0;
    private boolean triggerEnabled = false;
    // Trigger Query
    private DataWarningQuery triggerQuery = null;
    // Trigger Result
    private DataWarningQueryResult triggerResult = null;
    // Data Query
    private DataWarningQuery dataQuery = null;
    // Query Result
    private DataWarningQueryResult queryResult = null;

    public DataWarningResult(boolean tr)
    {
        triggerRequired = tr;
        triggerEnabled = !tr;
    }

    public boolean hasError()
    {
    	if (queryResult != null)
    		return queryResult.hasError();
    	else
    		return false;
    }

    // Trigger functions
    // -----------------

    public void setTriggerQuery(DataWarningQuery dwq)
    {
    	triggerQuery = dwq;
        setTrigger(triggerQuery.getTheTrigger());
        setTriggerCondition(triggerQuery.getTheTriggerCondition());
    }

    public DataWarningQuery getTriggerQuery()
    {
    	return triggerQuery;
    }

    public long getTrigger()
    {
        return trigger;
    }

    public void setTrigger(long t)
    {
        trigger = t;
    }

    public int getTriggerCondition()
    {
        return triggerCondition;
    }

    public void setTriggerCondition(int tc)
    {
        triggerCondition = tc;
    }

    public long getTriggerActualValue()
    {
        return triggerActualValue;
    }

    public long getTriggerActualValue(String userId)
    {
		SilverTrace.info("dataWarning", "DataWarningResult.getTriggerActualValue(userId)", "root.MSG_GEN_PARAM_VALUE", "userId = " + userId);
    	if (triggerResult.isPersoEnabled())
    	{
        	try
			{
//        		return triggerResult.returnTriggerValueFromResult();
				return triggerResult.returnTriggerValueFromResult(userId);
			}
	        catch (Exception e)
			{
	            SilverTrace.warn("dataWarning", "DataWarningResult.getTriggerActualValue()","root.MSG_GEN_ENTER_METHOD");
	            return 0;
			}
    	}
    	else
    		return getTriggerActualValue();
    }

    public void setTriggerActualValue(long tav)
    {
        triggerActualValue = tav;
    }

    public boolean getTriggerEnabled()
    {
        return triggerEnabled;
    }

    public boolean getTriggerEnabled(String userId)
    {
		SilverTrace.info("dataWarning", "DataWarningResult.getTriggerEnabled(userId)", "root.MSG_GEN_PARAM_VALUE", "userId = " + userId);
    	if (triggerResult.isPersoEnabled())
    	{
    		if  (triggerResult.getNbRows(userId)>0)
    		{
	        	try
				{
					if (getQueryResult().isPersoEnabled())
						return triggerQuery.checkTriggerSatisfied(triggerResult.returnTriggerValueFromResult(userId));
					else
						return triggerQuery.checkTriggerSatisfied(triggerResult.returnTriggerValueFromResult());
				}
		        catch (Exception e)
				{
		            SilverTrace.warn("dataWarning", "DataWarningResult.getTriggerEnabled()","root.MSG_GEN_ENTER_METHOD");
		            return false;
				}
			}
			else
				return false;
    	}
    	else
    		return getTriggerEnabled();
    }

    public void setTriggerEnabled(boolean te)
    {
        triggerEnabled = te;
    }

    public boolean getTriggerRequired()
    {
        return triggerRequired;
    }

    public DataWarningQueryResult getTriggerResult()
    {
        return triggerResult;
    }

    public void computeTriggerResult(DataWarning dataWarning)
    {
		SilverTrace.info("dataWarning", "DataWarningResult.computeTriggerResult(datawarning)", "root.MSG_GEN_ENTER_METHOD");
        triggerResult = triggerQuery.executeQuery(dataWarning);

		SilverTrace.info("dataWarning", "DataWarningResult.computeTriggerResult(datawarning)", "root.MSG_GEN_PARAM_VALUE","triggerResult nb de lignes ="+triggerResult.getNbRows());

        if (!triggerResult.hasError())
        {
        	try
			{
				setTriggerActualValue(triggerResult.returnTriggerValueFromResult());
			}
            catch (Exception e)
			{
                SilverTrace.warn("dataWarning", "DataWarningResult.setTriggerResult()","root.MSG_GEN_ENTER_METHOD");
                triggerResult.addError(e,"Value = "+triggerResult.getValue(0,0));
			}
        }
        if (!triggerResult.hasError())
        	setTriggerEnabled(triggerQuery.checkTriggerSatisfied(getTriggerActualValue()));
        else
        	setTriggerEnabled(false);
    }

    // Query Result functions
    // ----------------------

    public void setDataQuery(DataWarningQuery dwq)
    {
    	dataQuery = dwq;
    }

    public DataWarningQuery getDataQuery()
    {
    	return dataQuery;
    }

    public DataWarningQueryResult getQueryResult()
    {
        return queryResult;
    }

    public void setQueryResult(DataWarningQueryResult dwqr)
    {
        queryResult = dwqr;
    }


    public String getConditionDisplayedString(ResourceLocator rl)
    {
    	String valret = null;
    	switch (getTriggerCondition())
		{
    	case DataWarningQuery.TRIGGER_CONDITION_SUP :
    		valret = rl.getString("triggerCondition0");
    		break;
        case DataWarningQuery.TRIGGER_CONDITION_SUP_OU_EG  :
    		valret = rl.getString("triggerCondition1");
            break;
        case DataWarningQuery.TRIGGER_CONDITION_INF		:
    		valret = rl.getString("triggerCondition2");
            break;
        case DataWarningQuery.TRIGGER_CONDITION_INF_OU_EG  :
    		valret = rl.getString("triggerCondition3");
            break;
        case DataWarningQuery.TRIGGER_CONDITION_EG		    :
    		valret = rl.getString("triggerCondition4");
            break;
        case DataWarningQuery.TRIGGER_CONDITION_DIF	    :
    		valret = rl.getString("triggerCondition5");
            break;
    	default :
    		valret = "";
    		break;
    	}
    	return valret;
    }
}
