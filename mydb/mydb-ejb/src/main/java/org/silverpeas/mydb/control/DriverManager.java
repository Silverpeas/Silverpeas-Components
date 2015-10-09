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

package org.silverpeas.mydb.control;

import com.silverpeas.mydb.data.datatype.DataType;
import com.silverpeas.mydb.data.datatype.DataTypeList;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.XmlSettingBundle;
import org.w3c.dom.Node;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Database driver manager. All available drivers are described in MyDB setting file.
 * @author Antoine HEDIN
 */
public class DriverManager {

  private static final String XML_SETTING_PATH =
      "org.silverpeas.mydb.control.settings.myDBSettings";

  // Current driver.
  private Driver driver = null;

  // Available drivers descriptions.
  private String[] driversNames;
  private String[] driversDisplayNames;
  private String[] driversClassNames;
  private String[] driversDescriptions;
  private List<String[]> driversUrls;

  // Number of available drivers.
  private int driversCount = 0;

  // Lists of keywords.
  private ArrayList<String>[] databaseKeywordsLists;

  // Lists of data types.
  private DataTypeList[] dataTypesLists;

  public DriverManager() {
    loadDrivers();
  }

  /**
   * Loads available drivers from the MyDB setting file.
   */
  private void loadDrivers() {
    try {
      XmlSettingBundle xmlConfig = ResourceLocator.getXmlSettingBundle(XML_SETTING_PATH);
      driversNames = xmlConfig.getStringArray("MyDB-configuration.Drivers");
      driversCount = driversNames.length;
      driversDisplayNames = new String[driversCount];
      driversClassNames = new String[driversCount];
      driversDescriptions = new String[driversCount];
      databaseKeywordsLists = new ArrayList[driversCount];
      dataTypesLists = new DataTypeList[driversCount];
      driversUrls = new ArrayList<>();

      String[] dataTypesKeys = { "name", "sqlType", "javaType", "length" };
      int dataTypesKeysCount = dataTypesKeys.length;

      for (int i = 0; i < driversCount; i++) {
        String sectionName = driversNames[i] + "-configuration";
        XmlSettingBundle.SettingSection section = xmlConfig.getSettingSection(sectionName);
        driversDisplayNames[i] = section.getString("DriverName");
        driversClassNames[i] = section.getString("ClassName");
        driversDescriptions[i] = section.getString("Description");
        driversUrls.add(section.getStringArray("JDBCUrls"));
        String[] keywords = section.getStringArray("DatabaseKeywords");
        databaseKeywordsLists[i] = new ArrayList<>(Arrays.asList(keywords));

        List<XmlSettingBundle.SettingSection> dataTypes = section.getAllSettingSection("DataType");
        dataTypesLists[i] = new DataTypeList(dataTypes.size());
        for (XmlSettingBundle.SettingSection dataType : dataTypes) {
          String[] attrValues = new String[dataTypesKeysCount];
          for(int j = 0; j < dataTypesKeysCount; j++) {
            attrValues[j] = dataType.getAttribute(dataTypesKeys[j]);
          }
          dataTypesLists[i].add(new DataType(attrValues));
        }
      }
    } catch (Exception e) {
      SilverTrace.warn("myDB", "DriverManager.loadDrivers()",
          "myDB.MSG_DRIVERS_LOADING_FAILED", e);
    }
  }

  /**
   * @param driverName The name of searched driver.
   * @return The driver corresponding to the name given as parameter.
   */
  public Driver getDriver(final String driverName) {
    String currentDriver = driverName;
    if (driver == null) {
      try {
        if (!StringUtil.isDefined(currentDriver)) {
          currentDriver = driversNames[0];
          SilverTrace
              .info("myDB", "DriverManager.getDriver()", "myDB.MSG_DRIVER_UNDEFINED",
              "driverName undefined ! default one used instead = " + currentDriver);
        }
        driver = registerAndInstanciateDriver(currentDriver);
      } catch (Exception e) {
        SilverTrace.warn("myDB", "DriverManager.getDriver()",
            "myDB.MSG_DRIVER_INIT_FAILED", "DriverName=" + currentDriver, e);
      }
    }
    return driver;
  }

  /**
   * Reset the current driver.
   */
  public void resetDriver() {
    driver = null;
  }

  /**
   * @return the descriptions of the available drivers.
   */
  public Collection<String> getDriversDescriptions() {
    return Arrays.asList(driversDescriptions);
  }

  /**
   * @return the names of the available drivers.
   */
  public Collection<String> getAvailableDriversNames() {
    return Arrays.asList(driversNames);
  }

  /**
   * @return the display names of the available drivers.
   */
  public Collection<String> getAvailableDriversDisplayNames() {
    return Arrays.asList(driversDisplayNames);
  }

  /**
   * @param driverName The name of the driver.
   * @return the JDBC URLs corresponding to the name given as parameter.
   */
  public Collection<String> getJdbcUrlsForDriver(String driverName) {
    String[] str = driversUrls.get(searchDriverIndex(driverName));
    return Arrays.asList(str);
  }

  /**
   * @param driverName The name of the driver.
   * @return the description of the driver corresponding to the name given as parameter.
   */
  public String getDescriptionForDriver(String driverName) {
    return driversDescriptions[searchDriverIndex(driverName)];
  }

  /**
   * @param driverName The name of the driver.
   * @return the driver class name of the driver corresponding to the name given as parameter.
   */
  public String getDriverClassName(String driverName) {
    return driversClassNames[searchDriverIndex(driverName)];
  }

  /**
   * @param driverName The name of the driver.
   * @return the list of keywords of the driver corresponding to the name given as parameter.
   */
  public ArrayList<String> getDatabaseKeywordsListForDriver(String driverName) {
    return databaseKeywordsLists[searchDriverIndex(driverName)];
  }

  /**
   * @param driverName The name of the driver.
   * @return the list of data types of the driver corresponding to the name given as parameter.
   */
  public DataTypeList getDataTypeListForDriver(String driverName) {
    return dataTypesLists[searchDriverIndex(driverName)];
  }

  /**
   * @param driverName The name of the driver.
   * @return An instance of the driver corresponding to the name given as parameter.
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  private Driver registerAndInstanciateDriver(String driverName)
      throws ClassNotFoundException, InstantiationException,
      IllegalAccessException {
    SilverTrace.info("myDB",
        "MyDBSessionController.registerAndInstanciateDriver()",
        "root.MSG_GEN_ENTER_METHOD", "driverName=" + driverName);
    SilverTrace.info("myDB",
        "MyDBSessionController.registerAndInstanciateDriver()",
        "root.MSG_GEN_PARAM_VALUE", "driversClassNames.length=" + driversCount);

    return (Driver) Class.forName(getDriverClassName(driverName)).newInstance();
  }

  /**
   * @param driverName The name of the driver.
   * @return The index of the driver corresponding to the name given as parameter.
   */
  private int searchDriverIndex(String driverName) {
    SilverTrace.info("myDB", "DriverManager.searchDriverIndex()",
        "myDB.MSG_DRIVER_NAME", "DriverName=" + driverName);
    int i = 0;
    while (i < driversCount) {
      if (driversNames[i].equals(driverName)) {
        return i;
      }
      i++;
    }
    SilverTrace.warn("myDB", "DriverManager.searchDriverIndex()",
        "myDB.MSG_DRIVER_NOT_FOUND", "DriverName=" + driverName);
    return -1;
  }

}
