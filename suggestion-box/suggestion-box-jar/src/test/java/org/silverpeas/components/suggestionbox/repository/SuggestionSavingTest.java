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
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.persistence.repository.OperationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit test on the saving of a suggestion into an existing suggestion box in Silverpeas.
 * This unit test tests only
 * @author mmoquillon
 */
public class SuggestionSavingTest extends RepositoryBasedTest {

  private final static String SUGGESTION_BOX_ID_1 = "suggestion-box_1";

  @Override
  public String getDataSetPath() {
    return "org/silverpeas/components/suggestionbox/suggestion-box-dataset.xml";
  }

  @Test
  public void saveASuggestionIntoAnExistingSuggestionBox() throws Exception {
    UserDetail author = aUser();
    // create a new suggestion
    Suggestion suggestion = new Suggestion("My suggestion");
    suggestion.setCreator(author);
    suggestion.setContent("This is my content");
    // add the suggestion into the suggestion box
    SuggestionBox box = getPersistenceService().getById(SUGGESTION_BOX_ID_1);

    // save the suggestion
    getPersistenceService().save(OperationContext.fromUser(author), box, suggestion);

    // check the suggestion in database
    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sc_suggestion");
    assertThat(table.getRowCount(), is(2));
    String actualTitle = (String) table.getValue(0, "title");
    assertThat(actualTitle, is(suggestion.getTitle()));
  }
}
