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
package com.silverpeas.components.suggestionbox;

import com.silverpeas.admin.components.InstanciationException;
import com.silverpeas.components.suggestionbox.mock.SuggestionBoxServiceMockWrapper;
import com.silverpeas.components.suggestionbox.model.SuggestionBox;
import com.silverpeas.components.suggestionbox.model.SuggestionBoxService;
import com.silverpeas.components.suggestionbox.model.SuggestionBoxServiceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.sql.Connection;


/**
 * Test the creation and the remove of the application Suggestion Box.
 * @author mmoquillon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-suggestionbox.xml", "/spring-empty-datasource.xml"})
public class SuggestionBoxInstanciatorTest {

  public SuggestionBoxInstanciatorTest() {
  }

  @Before
  public void setUp() {

  }

  @After
  public void tearDown() {
  }

  /**
   * Validates a SuggestionBox instance is created and persisted at the application creation.
   * @throws InstanciationException
   */
  @Test
  public void createASuggestionBoxInstance() throws InstanciationException {
    Connection connection = getConnection();
    SuggestionBoxService service = getSuggestionServiceBox();
    SuggestionBoxInstanciator instanciator = new SuggestionBoxInstanciator();
    instanciator.create(connection, "WA1", "suggestion-box1", "0");

    SuggestionBox box = new SuggestionBox("suggestion-box1", "");
    box.setCreatedBy("0");
    verify(service).saveSuggestionBox(eq(box));
  }

  private Connection getConnection() {
    return mock(Connection.class);
  }

  private SuggestionBoxService getSuggestionServiceBox() {
    SuggestionBoxServiceMockWrapper mockWrapper
        = (SuggestionBoxServiceMockWrapper) SuggestionBoxServiceFactory.getServiceInstance();
    return mockWrapper.getMock();
  }
}
