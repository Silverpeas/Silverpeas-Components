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
