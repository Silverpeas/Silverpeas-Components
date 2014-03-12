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

import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * User: Yohann Chastagnier
 * Date: 11/03/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    locations = {"/spring-suggestion-box-jpa.xml", "/spring-suggestion-box-embedded-datasource.xml"})
public class SuggestionBoxRepositoryTest {

  private final static String SUGGESTION_BOX_ID_1 = "suggestion-box_1";

  @Inject
  private SuggestionBoxRepository suggestionBoxRepository;

  @Inject
  private SuggestionRepository suggestionRepository;

  @Inject
  @Named("jpaDataSource")
  private DataSource dataSource;

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

  /**
   * Deletion of a suggestion box, at a repository level, must delete all associated suggestions.
   */
  @Test
  @Transactional
  public void deleteSuggestionBox() {
    SuggestionBox existentSuggestionBox = suggestionBoxRepository.getById(SUGGESTION_BOX_ID_1);
    assertThat(existentSuggestionBox, notNullValue());
    assertThat(existentSuggestionBox.getId(), is(SUGGESTION_BOX_ID_1));

    List<Suggestion> associatedSuggestions = suggestionRepository.listBySuggestionBox(
        existentSuggestionBox);
    assertThat(associatedSuggestions, hasSize(1));

    // The suggestion box deletion
    suggestionBoxRepository.delete(existentSuggestionBox);

    // Verifications
    assertThat(suggestionBoxRepository.getById(SUGGESTION_BOX_ID_1), nullValue());
    assertThat(suggestionRepository.listBySuggestionBox(existentSuggestionBox), hasSize(0));
  }
}
