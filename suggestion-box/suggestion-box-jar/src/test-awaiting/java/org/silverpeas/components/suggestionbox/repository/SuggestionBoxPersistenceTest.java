/*
 * Copyright (C) 2000-2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
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
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.junit.Test;
import org.silverpeas.components.suggestionbox.model.PersistenceService;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.persistence.repository.OperationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

/**
 * Unit test on the persistence of a suggestion box in Silverpeas using the corresponding JPA
 * repository.
 * @author mmoquillon
 */
public class SuggestionBoxPersistenceTest extends RepositoryBasedTest {

  private final static String SUGGESTION_BOX_ID_1 = "suggestion-box_1";
  private final static String SUGGESTION_BOX_INSTANCE_ID = "suggestionBox1";

  @Override
  public String getDataSetPath() {
    return "org/silverpeas/components/suggestionbox/suggestion-box-dataset.xml";
  }

  @Test
  public void getByComponentInstanceId() {
    PersistenceService persister = getPersistenceService();
    assertThat(persister.getSuggestionBoxByComponentInstanceId("dummyId"), nullValue());
    assertThat(persister.getSuggestionBoxByComponentInstanceId(SUGGESTION_BOX_INSTANCE_ID),
        notNullValue());
  }


  @Test
  public void saveSuggestionBox() throws Exception {
    UserDetail creator = aUser();
    SuggestionBox box = new SuggestionBox(SUGGESTION_BOX_INSTANCE_ID);
    OperationContext ctx = OperationContext.fromUser(creator.getId());
    getPersistenceService().saveSuggestionBox(ctx, box);

    // Verification
    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sc_suggestion_box");
    assertThat(table.getRowCount(), is(3));
    String createdBy = (String) table.getValue(0, "createdBy");
    String instanceId = (String) table.getValue(0, "instanceId");
    assertThat(createdBy, is(creator.getId()));
    assertThat(instanceId, is(SUGGESTION_BOX_INSTANCE_ID));
  }

  @Test(expected = IllegalArgumentException.class)
  public void saveSuggestionBoxWithAnInvalidOperationContext() throws Exception {
    SuggestionBox box = new SuggestionBox(SUGGESTION_BOX_INSTANCE_ID);
    OperationContext ctx = OperationContext.createInstance();
    getPersistenceService().saveSuggestionBox(ctx, box);

    fail("An exception should be raised!");
  }

  /**
   * Deletion of a suggestion box, at a repository level, must deleteSuggestionBox all associated
   * suggestions.
   * @throws java.lang.Exception
   */
  @Test
  public void deleteSuggestionBox() throws Exception {
    PersistenceService persister = getPersistenceService();
    SuggestionBox existentSuggestionBox = persister.getSuggestionBoxById(SUGGESTION_BOX_ID_1);
    assertThat(existentSuggestionBox, notNullValue());
    assertThat(existentSuggestionBox.getId(), is(SUGGESTION_BOX_ID_1));

    // The suggestion box deletion
    persister.deleteSuggestionBox(existentSuggestionBox);

    // Verifications
    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sc_suggestion_box");
    assertThat(table.getRowCount(), is(1));
    table = actualDataSet.getTable("sc_suggestion");
    assertThat(table.getRowCount(), is(1));
  }
}
