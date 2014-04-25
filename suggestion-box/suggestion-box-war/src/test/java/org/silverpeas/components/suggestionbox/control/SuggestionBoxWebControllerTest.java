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

import com.silverpeas.notification.builder.helper.UserNotificationHelper;
import com.silverpeas.personalization.UserMenuDisplay;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.personalization.service.PersonalizationService;
import com.silverpeas.web.TestResources;
import com.silverpeas.web.mock.PersonalizationServiceMockWrapper;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.WebMessager;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.silverpeas.components.suggestionbox.mock.SuggestionBoxServiceMockWrapper;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionBoxService;
import org.silverpeas.contribution.ContributionStatus;
import org.silverpeas.servlet.HttpRequest;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.ws.rs.WebApplicationException;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


/**
 * Unit test on some operations of the SuggestionBoxWebController instance.
 * @author mmoquillon
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({UserNotificationHelper.class})
public class SuggestionBoxWebControllerTest {

  private AbstractApplicationContext appContext;
  private SuggestionBoxWebController controller;

  private static final String COMPONENT_INSTANCE_ID = "suggestionBox1";
  private static final String SUGGESTION_ID = "suggestion_1";

  @Before
  public void setUp() {
    appContext = new ClassPathXmlApplicationContext("/spring-suggestion-box.xml");
    MainSessionController sessionController = mock(MainSessionController.class);
    ComponentContext componentContext = mock(ComponentContext.class);
    controller = new SuggestionBoxWebController(sessionController, componentContext);
    PersonalizationService mock = getPersonalizationService();
    UserPreferences preferences =
        new UserPreferences(TestResources.DEFAULT_LANGUAGE, "", "", false, true, true,
            UserMenuDisplay.DISABLE);
    when(mock.getUserSettings(anyString())).thenReturn(preferences);
  }

  @After
  public void tearDown() {
    appContext.close();
  }

  @Test
  public void addANewSuggestionToAGivenSuggestionBox() {
    final String title = "A suggestion title";
    final String content = "A suggestion content";
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getRequest().getParameter("title")).thenReturn(title);
    when(context.getRequest().getParameter("content")).thenReturn(content);
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    SuggestionBox box = context.getSuggestionBox();

    controller.addSuggestion(context);

    ArgumentCaptor<Suggestion> suggestionArgument = ArgumentCaptor.forClass(Suggestion.class);
    verify(suggestionBoxService, times(1)).
        addSuggestion(eq(box), suggestionArgument.capture());
    Suggestion suggestion = suggestionArgument.getValue();
    assertThat(suggestion.getTitle(), is(title));
    assertThat(suggestion.getContent(), is(content));
  }

  @Test
  public void editASuggestion() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(aSuggestion());

    controller.editSuggestion(context);

    verify(suggestionBoxService, times(1)).findSuggestionById(box, SUGGESTION_ID);
  }

  @Test(expected = WebApplicationException.class)
  public void editAnUnexistingSuggestionInAGivenSuggestionBox() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(Suggestion.NONE);

    controller.editSuggestion(context);
  }

  @Test(expected = WebApplicationException.class)
  public void editANonDraftSuggestionInAGivenSuggestionBox() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID))
        .thenReturn(aSuggestionWithStatus(ContributionStatus.PENDING_VALIDATION));

    controller.editSuggestion(context);
  }

  @Test
  public void updateASuggestionInDraft() {
    assertUpdateASuggestion(ContributionStatus.DRAFT);
  }

  @Test
  public void updateASuggestionRefused() {
    assertUpdateASuggestion(ContributionStatus.REFUSED);
  }

  @Test(expected = WebApplicationException.class)
  public void updateANonDraftOrRefusedSuggestionInAGivenSuggestionBox() {
    assertUpdateASuggestion(ContributionStatus.UNKNOWN);
  }

  private void assertUpdateASuggestion(ContributionStatus withStatus) {
    final String modifiedTitle = "A modified title";
    final String modifiedContent = "A modified content";
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    when(context.getRequest().getParameter("title")).thenReturn(modifiedTitle);
    when(context.getRequest().getParameter("content")).thenReturn(modifiedContent);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID))
        .thenReturn(aSuggestionWithStatus(withStatus));

    controller.updateSuggestion(context);

    ArgumentCaptor<Suggestion> suggestionArgument = ArgumentCaptor.forClass(Suggestion.class);
    verify(suggestionBoxService, times(1)).updateSuggestion(suggestionArgument.capture());
    Suggestion suggestion = suggestionArgument.getValue();
    assertThat(suggestion.getTitle(), is(modifiedTitle));
    assertThat(suggestion.getContent(), is(modifiedContent));
  }

  @Test(expected = WebApplicationException.class)
  public void updateAnUnexistingSuggestionInAGivenSuggestionBox() {
    final String modifiedTitle = "A modified title";
    final String modifiedContent = "A modified content";
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    when(context.getRequest().getParameter("title")).thenReturn(modifiedTitle);
    when(context.getRequest().getParameter("content")).thenReturn(modifiedContent);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(Suggestion.NONE);

    controller.updateSuggestion(context);
  }

  @Test
  public void deleteASuggestion() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    Suggestion suggestion = aSuggestion();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(suggestion);

    controller.deleteSuggestion(context);

    verify(suggestionBoxService, times(1)).removeSuggestion(box, suggestion);
  }

  @Test(expected = WebApplicationException.class)
  public void deleteAnUnexistingSuggestionInSuggestionBox() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(Suggestion.NONE);

    controller.deleteSuggestion(context);
  }

  @Test(expected = WebApplicationException.class)
  public void deleteANonDraftSuggestionInSuggestionBox() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID))
        .thenReturn(aSuggestionWithStatus(ContributionStatus.VALIDATED));

    controller.deleteSuggestion(context);
  }

  @Test
  public void publishASuggestionInDraftWithUserRoleAccess() {
    assertPublishASuggestion(ContributionStatus.DRAFT);
  }

  @Test
  public void publishASuggestionRefusedWithUserRoleAccess() {
    assertPublishASuggestion(ContributionStatus.REFUSED);
  }

  @Test(expected = WebApplicationException.class)
  public void publishANonDraftOrRefusedSuggestionInAGivenSuggestionBoxWithUserRoleAccess() {
    assertPublishASuggestion(ContributionStatus.UNKNOWN);
  }

  private void assertPublishASuggestion(ContributionStatus withStatus) {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    Suggestion suggestion = aSuggestionWithStatus(withStatus);
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(suggestion);
    when(suggestionBoxService.publishSuggestion(eq(box), eq(suggestion))).thenReturn(suggestion);

    PowerMockito.mockStatic(UserNotificationHelper.class);
    controller.publishSuggestion(context);

    verify(suggestionBoxService, times(1)).publishSuggestion(eq(box), eq(suggestion));
  }

  @Test(expected = WebApplicationException.class)
  public void publishAnUnexistingSuggestionInAGivenSuggestionBox() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(Suggestion.NONE);

    controller.publishSuggestion(context);
  }

  @SuppressWarnings("unchecked")
  private SuggestionBoxWebRequestContext aSuggestionBoxWebRequestContext() {
    SuggestionBoxWebRequestContext context = mock(SuggestionBoxWebRequestContext.class);
    HttpRequest request = mock(HttpRequest.class);
    Map<String, String> pathVariables = mock(Map.class);
    when(context.getRequest()).thenReturn(request);
    when(context.getPathVariables()).thenReturn(pathVariables);
    when(context.getUser()).thenReturn(aUser());
    when(context.getComponentInstanceId()).thenReturn(COMPONENT_INSTANCE_ID);
    when(context.getMessager()).thenReturn(WebMessager.getInstance());
    when(context.getSuggestionBox()).thenReturn(aSuggestionBox());
    return context;
  }

  private SuggestionBoxService getSuggestionBoxService() {
    SuggestionBoxServiceMockWrapper mockWrapper =
        appContext.getBean(SuggestionBoxServiceMockWrapper.class);
    return mockWrapper.getMock();
  }

  private PersonalizationService getPersonalizationService() {
    PersonalizationServiceMockWrapper mockWrapper = appContext.
        getBean(PersonalizationServiceMockWrapper.class);
    return mockWrapper.getPersonalizationServiceMock();
  }

  private SuggestionBox aSuggestionBox() {
    SuggestionBox box = new SuggestionBox(COMPONENT_INSTANCE_ID);
    box.setCreator(aUser());
    return box;
  }

  private Suggestion aSuggestionWithStatus(ContributionStatus status) {
    Suggestion suggestion = aSuggestion();
    suggestion.setStatus(status);
    return suggestion;
  }

  private Suggestion aSuggestion() {
    Suggestion suggestion = new Suggestion("A suggestion title");
    suggestion.setCreator(aUser());
    suggestion.setContent("A suggestion content");
    return suggestion;
  }

  private UserDetail aUser() {
    UserDetail user = new UserDetail();
    user.setId("1");
    user.setFirstName("Bart");
    user.setLastName("Simpson");
    return user;
  }
}
