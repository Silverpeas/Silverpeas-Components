package com.silverpeas.dataWarning;

import com.stratelia.silverpeas.silverpeasinitialize.IInitialize;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.silverpeas.dataWarning.model.*;
import com.silverpeas.dataWarning.control.*;
import java.util.*;

public class DataWarningInitialize implements IInitialize
{
    public DataWarningInitialize()
	{
	}

    public boolean Initialize()
    {
        try
		{
        	DataWarningDataManager dwdm = new DataWarningDataManager();
        	Collection instCol = dwdm.getDataWarningSchedulerInstances();
        	Iterator it = instCol.iterator();
        	
        	while (it.hasNext())
        	{
				DataWarningSchedulerTable.addScheduler((String)it.next());
        	}
		}
		catch (Exception e)
		{
			SilverTrace.error("dataWarning", "DataWarningInitialize.initialize()", "", e);
		}
		return true;
    }
}
