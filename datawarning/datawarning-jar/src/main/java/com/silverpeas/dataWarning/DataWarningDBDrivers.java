package com.silverpeas.dataWarning;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.*;
import com.silverpeas.dataWarning.control.*;
import java.io.*;
import java.util.*;

public class DataWarningDBDrivers extends Object
{
    static protected Hashtable allDBDrivers = new Hashtable(); // This one is there for optimized research
    static protected DataWarningDBDriver[] sortedDBDrivers = null;

    static 
    {
		try
        {
			InputStream ConfigFileInputStream;
			String configFileStr ="settings/dataWarningSettings";
        	String[] driversUniqueIds;

            ConfigFileInputStream = ResourceLocator.getResourceAsStream(new DataWarningSchedulerTable(),null,configFileStr,".xml");
			XMLConfigurationStore m_XMLConfig= new XMLConfigurationStore(null,ConfigFileInputStream,"DataWarning-configuration");
			driversUniqueIds = m_XMLConfig.getValues("Drivers");
			ConfigFileInputStream.close();

            sortedDBDrivers = new DataWarningDBDriver[driversUniqueIds.length];
			for(int j=0;j<driversUniqueIds.length;j++)
			{
				SilverTrace.info("dataWarning", "DataWarningDBDrivers.static","DataWarning.MSG_DRIVER_NAME","DriverUniqueId="+driversUniqueIds[j]);

				ConfigFileInputStream = ResourceLocator.getResourceAsStream(new DataWarningSchedulerTable(),null,configFileStr,".xml");
				m_XMLConfig= new XMLConfigurationStore(null,ConfigFileInputStream,driversUniqueIds[j]+"-configuration");
                sortedDBDrivers[j] = new DataWarningDBDriver(driversUniqueIds[j], m_XMLConfig.getString("DriverName"),m_XMLConfig.getString("ClassName"),m_XMLConfig.getString("Description"),m_XMLConfig.getString("JDBCUrl"));
                allDBDrivers.put(driversUniqueIds[j],sortedDBDrivers[j]);
				ConfigFileInputStream.close();
			}
		}
		catch  (Exception e) 
		{
			SilverTrace.error("dataWarning", "DataWarningDBDrivers.static", "DataWarning.MSG_load_DRIVERS_FAIL", null, e);
		}
    }

    public static DataWarningDBDriver[] getDBDrivers()
	{
        return sortedDBDrivers;
	}

    public static DataWarningDBDriver getDBDriver(String driverUniqueId)
    {
        if ((driverUniqueId == null) || (driverUniqueId.length() <= 0))
        {
            return sortedDBDrivers[0];
        }
        else
        {
            return (DataWarningDBDriver)allDBDrivers.get(driverUniqueId);
        }
    }
}
