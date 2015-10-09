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
package com.silverpeas.dataWarning;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.XmlSettingBundle;

import java.util.HashMap;
import java.util.Map;

public final class DataWarningDBDrivers extends Object {

  // This one is there for optimized research
  static protected Map<String, DataWarningDBDriver> allDBDrivers = new HashMap<>();
  static protected DataWarningDBDriver[] sortedDBDrivers = null;

  private static final String XML_SETTING_PATH =
      "org.silverpeas.dataWarning.settings.dataWarningSettings";

  public DataWarningDBDrivers() {
    loadDrivers();
  }

  public void loadDrivers() {
    try {
      XmlSettingBundle xmlConfig = ResourceLocator.getXmlSettingBundle(XML_SETTING_PATH);
      String[] driversUniqueIds = xmlConfig.getStringArray("DataWarning-configuration.Drivers");

      sortedDBDrivers = new DataWarningDBDriver[driversUniqueIds.length];
      for (int j = 0; j < driversUniqueIds.length; j++) {
        XmlSettingBundle.SettingSection section =
            xmlConfig.getSettingSection(driversUniqueIds[j] + "-configuration");
        sortedDBDrivers[j] =
            new DataWarningDBDriver(driversUniqueIds[j], section.getString("DriverName"),
                section.getString("ClassName"), section.getString("Description"),
                section.getString("JDBCUrl"));
        allDBDrivers.put(driversUniqueIds[j], sortedDBDrivers[j]);
      }
    } catch (Exception e) {
      SilverTrace.error("dataWarning", "DataWarningDBDrivers.loadDrivers",
          "DataWarning.MSG_load_DRIVERS_FAIL", null, e);
    }
  }

  public DataWarningDBDriver[] getDBDrivers() {
    return sortedDBDrivers;
  }

  public DataWarningDBDriver getDBDriver(String driverUniqueId) {

    if ((driverUniqueId == null) || (driverUniqueId.length() <= 0)) {
      return sortedDBDrivers[0];
    } else {
      return allDBDrivers.get(driverUniqueId);
    }
  }
}
