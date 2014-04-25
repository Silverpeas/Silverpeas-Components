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
package org.silverpeas.components.suggestionbox.model;

import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.personalization.service.PersonalizationService;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.silverpeas.components.suggestionbox.mock.PersonalizationServiceMockWrapper;
import org.silverpeas.components.suggestionbox.repository.RepositoryBasedTest;
import org.silverpeas.wysiwyg.control.WysiwygController;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * Unit test on the business operations of the SuggestionBox objects.
 * @author mmoquillon
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(WysiwygController.class)
public class SuggestionBoxTest extends RepositoryBasedTest {

  private final static String SUGGESTION_BOX_INSTANCE_ID = "suggestionBox1";

  @Override
  public String getDataSetPath() {
    return "org/silverpeas/components/suggestionbox/suggestion-box-dataset.xml";
  }

  @Override
  public String getApplicationContextPath() {
    return "spring-suggestion-box.xml";
  }

  @Test
  public void testAddASuggestionIntoASuggestionBox() throws Exception {
    PowerMockito.mockStatic(WysiwygController.class);
    UserDetail author = aUser();
    PersonalizationService personalizationService = getPersonalizationService();
    UserPreferences preferences = new UserPreferences();
    preferences.setLanguage("fr");
    when(personalizationService.getUserSettings(author.getId())).thenReturn(preferences);
    SuggestionBox box = SuggestionBox.getByComponentInstanceId(SUGGESTION_BOX_INSTANCE_ID);
    Suggestion newSuggestion = new Suggestion("This is my suggestion");
    newSuggestion.setContent("This is the content of my suggestion");
    newSuggestion.setCreator(author);
    box.getSuggestions().add(newSuggestion);

    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("sc_suggestion");
    assertThat(table.getRowCount(), is(2));
    String actualTitle = (String) table.getValue(0, "title");
    assertThat(actualTitle, is(newSuggestion.getTitle()));
  }

  private PersonalizationService getPersonalizationService() {
    PersonalizationServiceMockWrapper mockWrapper = getApplicationContext().getBean(
        PersonalizationServiceMockWrapper.class);
    return mockWrapper.getMock();
  }
}
