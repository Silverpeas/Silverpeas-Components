/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.almanach;

import com.silverpeas.components.model.AbstractJndiCase;
import com.silverpeas.components.model.SilverpeasJndiCase;
import com.stratelia.webactiv.util.DBUtil;
import java.io.IOException;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import javax.naming.NamingException;
import org.dbunit.database.IDatabaseConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Base class for tests in the almanach component.
 * It prepares the database to use in tests.
 */
public abstract class BaseAlmanachTest extends AbstractJndiCase {

  /**
   * The month to use in tests: april.
   */
  public static final int TEST_MONTH = 4;
  /**
   * The year to use in tests: 2011
   */
  public static final int TEST_YEAR = 2011;
  /**
   * The all available almanach instances in tests.
   */
  public static final String[] almanachIds = {"almanach272", "almanach509", "almanach701"};
  private IDatabaseConnection dbConnection;
  private Connection connection;

  @BeforeClass
  public static void generalSetUp() throws IOException, NamingException, Exception {
    baseTest = new SilverpeasJndiCase("com/stratelia/webactiv/almanach/model/events-dataset.xml",
        "create-database.ddl");
    baseTest.configureJNDIDatasource();
    IDatabaseConnection databaseConnection = baseTest.getDatabaseTester().getConnection();
    executeDDL(databaseConnection, baseTest.getDdlFile());
    baseTest.getDatabaseTester().closeConnection(databaseConnection);
  }

  @Before
  public void bootstrapDatabase() throws Exception {
    dbConnection = baseTest.getDatabaseTester().getConnection();
    connection = dbConnection.getConnection();
    DBUtil.getInstanceForTest(connection);
  }

  @After
  public void shutdownDatabase() throws Exception {
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }

  public Connection getConnection() {
    return connection;
  }
  
  public static Date dateToUseInTests() {
    return getDate(TEST_YEAR, TEST_MONTH, 14);
  }
  
  public static Date getDate(int year, int month, int day) {
    Calendar date = Calendar.getInstance();
    date.set(Calendar.YEAR, year);
    date.set(Calendar.MONTH, month - 1);
    date.set(Calendar.DAY_OF_MONTH, day);
    return date.getTime();
  }
}
