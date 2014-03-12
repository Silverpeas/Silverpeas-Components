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

import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.suggestionbox.mock.OrganisationControllerMockWrapper;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.persistence.repository.OperationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * User: Yohann Chastagnier
 * Date: 11/03/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    locations = {"/spring-suggestion-box-jpa.xml", "/spring-suggestion-box-embedded-datasource.xml"})
public class SuggestionBoxRepositoryTest {

  private final static String SUGGESTION_BOX_ID_1 = "suggestion-box_1";
  private final static String SUGGESTION_BOX_INSTANCE_ID = "suggestion-box1";

  @Inject
  private SuggestionBoxRepository suggestionBoxRepository;

  @Inject
  private SuggestionRepository suggestionRepository;

  @Inject
  SuggestionBoxPersister persister;

  @Inject
  @Named("jpaDataSource")
  private DataSource dataSource;

  @PersistenceContext
  private EntityManager entityManager;

  private static ReplacementDataSet dataSet;

  @BeforeClass
  public static void prepareDataSet() throws Exception {
    dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        SuggestionBoxRepositoryTest.class.getClassLoader().getResourceAsStream(
            "org/silverpeas/components/suggestionbox/suggestion-box-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
  }

  /**
   * Because of the need of transaction, complete reload of Spring context is not possible.
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    final IDatabaseConnection myConnection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(myConnection, dataSet);
  }

  @Test
  public void saveSuggestionBox() throws Exception {
    UserDetail creator = aUser();
    SuggestionBox box
        = new SuggestionBox(SUGGESTION_BOX_INSTANCE_ID, null);
    OperationContext ctx = OperationContext.fromUser(creator.getId());
    persister.save(ctx, box);

    // Verification
    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sc_suggestion_box");
    assertThat(table.getRowCount(), is(2));
    String createdBy = (String) table.getValue(0, "createdBy");
    String instanceId = (String) table.getValue(0, "instanceId");
    assertThat(createdBy, is(creator.getId()));
    assertThat(instanceId, is(SUGGESTION_BOX_INSTANCE_ID));
  }

  @Test(expected = IllegalArgumentException.class)
  public void saveSuggestionBoxWithAnInvalidOperationContext() throws Exception {
    SuggestionBox box
        = new SuggestionBox(SUGGESTION_BOX_INSTANCE_ID, null);
    OperationContext ctx = OperationContext.createInstance();
    persister.save(ctx, box);

    fail("An exception should be raised!");
  }

  /**
   * Deletion of a suggestion box, at a repository level, must delete all associated suggestions.
   * @throws java.lang.Exception
   */
  @Test
  public void deleteSuggestionBox() throws Exception {
    SuggestionBox existentSuggestionBox = persister.getById(SUGGESTION_BOX_ID_1);
    assertThat(existentSuggestionBox, notNullValue());
    assertThat(existentSuggestionBox.getId(), is(SUGGESTION_BOX_ID_1));

    // The suggestion box deletion
    persister.delete(existentSuggestionBox);

    // Verifications
    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sc_suggestion_box");
    assertThat(table.getRowCount(), is(0));
    table = actualDataSet.getTable("sc_suggestion");
    assertThat(table.getRowCount(), is(0));
  }

  private OrganisationController getOrganisationController() {
    OrganisationController organisationController = OrganisationControllerFactory.
        getOrganisationController();
    return ((OrganisationControllerMockWrapper) organisationController).getMock();
  }

  private UserDetail aUser() {
    UserDetail user = new UserDetail();
    user.setId("1");
    OrganisationController organisationController = getOrganisationController();
    when(organisationController.getUserDetail("1")).thenReturn(user);

    return user;
  }

  private IDataSet getActualDataSet() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    return connection.createDataSet();
  }
}
