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
package org.silverpeas.components.suggestionbox.control;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.silverpeas.components.suggestionbox.mock.SuggestionBoxServiceMockWrapper;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionBoxService;
import org.silverpeas.servlet.HttpRequest;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test on some operations of the SuggestionBoxWebController instance.
 * @author mmoquillon
 */
public class SuggestionBoxWebControllerTest {

  private static AbstractApplicationContext appContext;
  private SuggestionBoxWebController controller;

  private static final String COMPONENT_INSTANCE_ID = "suggestionBox1";

  @BeforeClass
  public static void bootstrapSpringContext() {
    appContext = new ClassPathXmlApplicationContext("/spring-suggestion-box.xml");
  }

  @AfterClass
  public static void shutdownSpringContext() {
    appContext.close();
  }

  @Before
  public void setUp() {
    MainSessionController sessionController = mock(MainSessionController.class);
    ComponentContext componentContext = mock(ComponentContext.class);
    controller = new SuggestionBoxWebController(sessionController, componentContext);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void addANewSuggestionToAGivenSUggestionBox() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getRequest().getParameter("title")).thenReturn("A suggestion title");
    when(context.getRequest().getParameter("content")).thenReturn("A suggestion content");
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    when(suggestionBoxService.getByComponentInstanceId(COMPONENT_INSTANCE_ID)).thenReturn(
        aSuggestionBox());

    controller.addSuggestion(context);

  }

  private SuggestionBoxWebRequestContext aSuggestionBoxWebRequestContext() {
    SuggestionBoxWebRequestContext context = mock(SuggestionBoxWebRequestContext.class);
    HttpRequest request = mock(HttpRequest.class);
    when(context.getRequest()).thenReturn(request);
    when(context.getUser()).thenReturn(aUser());
    when(context.getComponentInstanceId()).thenReturn(COMPONENT_INSTANCE_ID);
    return context;
  }

  private SuggestionBoxService getSuggestionBoxService() {
    SuggestionBoxServiceMockWrapper mockWrapper = appContext.getBean(
        SuggestionBoxServiceMockWrapper.class);
    return mockWrapper.getMock();
  }

  private SuggestionBox aSuggestionBox() {
    SuggestionBox box = new SuggestionBox(COMPONENT_INSTANCE_ID);
    box.setCreator(aUser());
    return box;
  }

  private UserDetail aUser() {
    UserDetail user = new UserDetail();
    user.setId("1");
    user.setFirstName("Bart");
    user.setLastName("Simpson");
    return user;
  }
}
