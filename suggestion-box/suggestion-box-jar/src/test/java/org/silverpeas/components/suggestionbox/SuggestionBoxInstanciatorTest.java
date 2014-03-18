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
package org.silverpeas.components.suggestionbox;

import com.silverpeas.admin.components.InstanciationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.components.suggestionbox.mock.SuggestionBoxServiceMockWrapper;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionBoxService;
import org.silverpeas.components.suggestionbox.model.SuggestionBoxServiceFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Connection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


/**
 * Test the creation and the remove of the application Suggestion Box.
 * @author mmoquillon
 */
public class SuggestionBoxInstanciatorTest {

  private static AbstractApplicationContext context;

  public SuggestionBoxInstanciatorTest() {
  }

  @BeforeClass
  public static void bootstrapSpringContext() {
    context = new ClassPathXmlApplicationContext("/spring-suggestion-box-mock-service.xml");
  }

  @AfterClass
  public static void shutdownSpringContext() {
    context.close();
  }

  /**
   * Validates a SuggestionBox instance is created and persisted at the application creation.
   * @throws InstanciationException
   */
  @Test
  public void createASuggestionBoxInstance() throws InstanciationException {
    Connection connection = getConnection();
    final String appInstanceId = "suggestion-box1";
    final String userId = "0";
    SuggestionBoxService service = getSuggestionBoxService();
    SuggestionBoxInstanciator instanciator = new SuggestionBoxInstanciator();
    instanciator.create(connection, "WA1", appInstanceId, userId);

    ArgumentCaptor<SuggestionBox> box = ArgumentCaptor.forClass(SuggestionBox.class);
    verify(service).saveSuggestionBox(box.capture());
    SuggestionBox actualBox = box.getValue();
    assertThat(actualBox.getCreatedBy(), is(userId));
    assertThat(actualBox.getComponentInstanceId(), is(appInstanceId));
  }

  /**
   * Validates a SuggestionBox instance is deleted from persistence at the application deletion.
   * @throws InstanciationException
   */
  @Test
  public void deleteASuggestionBoxInstance() throws InstanciationException {
    Connection connection = getConnection();
    final String appInstanceId = "suggestion-box1";
    final String userId = "0";
    final SuggestionBox suggestionBoxToDelete = new SuggestionBox(appInstanceId);
    SuggestionBoxService service = getSuggestionBoxService();
    when(service.getByComponentInstanceId(appInstanceId)).thenReturn(suggestionBoxToDelete);

    SuggestionBoxInstanciator instanciator = new SuggestionBoxInstanciator();
    instanciator.delete(connection, "WA1", appInstanceId, userId);

    ArgumentCaptor<SuggestionBox> box = ArgumentCaptor.forClass(SuggestionBox.class);
    verify(service).deleteSuggestionBox(box.capture());
    SuggestionBox actualBox = box.getValue();
    assertThat(actualBox, is(suggestionBoxToDelete));
  }

  private Connection getConnection() {
    return mock(Connection.class);
  }

  private SuggestionBoxService getSuggestionBoxService() {
    SuggestionBoxServiceFactory serviceFactory = SuggestionBoxServiceFactory.getFactory();
    SuggestionBoxServiceMockWrapper mockWrapper
        = (SuggestionBoxServiceMockWrapper) serviceFactory.getSuggestionBoxService();
    return mockWrapper.getMock();
  }

}
