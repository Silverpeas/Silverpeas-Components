/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.components.suggestionbox.repository;

import org.silverpeas.components.suggestionbox.model.PersistenceService;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.silverpeas.components.suggestionbox.mock.OrganisationControllerMockWrapper;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;

import static org.mockito.Mockito.when;

/**
 * Abstract class for tests that are based on the behavior of a JPA repository. These tests are not
 * about the repository itself but on the persistence characteristics of a business object using a
 * JPA repository.
 */
public abstract class RepositoryBasedTest {

  // Spring context
  private ClassPathXmlApplicationContext context;

  private PersistenceService persistanceService;
  private DataSource dataSource;

  @Before
  public void setUp() throws Exception {

    // Spring
    context = new ClassPathXmlApplicationContext(getApplicationContextPath(),
        "spring-suggestion-box-embedded-datasource.xml");

    // Beans
    dataSource = (DataSource) context.getBean("jpaDataSource");
    persistanceService = context.getBean(PersistenceService.class);

    // Database
    DatabaseOperation.INSERT
        .execute(new DatabaseConnection(dataSource.getConnection()), getDataSet());
  }

  public ReplacementDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        RepositoryBasedTest.class.getClassLoader().getResourceAsStream(getDataSetPath())));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  @After
  public void tearDown() throws Exception {
    context.close();
  }

  /**
   * Gets the path of the XML file in which are defined the data to insert into the database
   * before the running of a test.
   * @return the path of the XML data set.
   */
  public abstract String getDataSetPath();

  /**
   * Gets the XML Spring configuration file from which the context will be bootstrapped for the
   * test. By default, the context is loaded from the XML file spring-suggestion-box-jpa.xml.
   * Overrides this method to specify another XML configuration file.
   * @return the location of the Spring XML configuration file.
   */
  public String getApplicationContextPath() {
    return "spring-suggestion-box-jpa.xml";
  }

  public OrganisationController getOrganisationController() {
    OrganisationController organisationController = OrganisationControllerFactory.
        getOrganisationController();
    return ((OrganisationControllerMockWrapper) organisationController).getMock();
  }

  public UserDetail aUser() {
    UserDetail user = new UserDetail();
    user.setId("1");
    OrganisationController organisationController = getOrganisationController();
    when(organisationController.getUserDetail("1")).thenReturn(user);

    return user;
  }

  public IDataSet getActualDataSet() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    return connection.createDataSet();
  }

  public PersistenceService getPersistenceService() {
    return this.persistanceService;
  }

  public ApplicationContext getApplicationContext() {
    return this.context;
  }
}
