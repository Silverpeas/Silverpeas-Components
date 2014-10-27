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

import com.silverpeas.personalization.UserMenuDisplay;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.personalization.service.PersonalizationService;
import org.silverpeas.cache.service.CacheServiceProvider;
import org.silverpeas.util.CollectionUtil;
import org.silverpeas.util.StringUtil;
import com.silverpeas.web.TestResources;
import com.silverpeas.web.mock.OrganizationControllerMockWrapper;
import com.silverpeas.web.mock.PersonalizationServiceMockWrapper;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.NavigationContext;
import com.stratelia.silverpeas.peasCore.servlets.WebMessager;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.cache.service.InMemoryCacheService;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionCollection;
import org.silverpeas.components.suggestionbox.web.SuggestionEntity;
import org.silverpeas.contribution.ContributionStatus;
import org.silverpeas.contribution.model.ContributionValidation;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.rating.ContributionRating;
import org.silverpeas.rating.ContributionRatingPK;
import org.silverpeas.servlet.HttpRequest;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javax.ws.rs.WebApplicationException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


/**
 * Unit test on some operations of the SuggestionBoxWebController instance.
 * @author mmoquillon
 */
public class SuggestionBoxWebControllerTest {

  private AbstractApplicationContext appContext;
  private SuggestionBoxWebController controller;

  private static final String COMPONENT_INSTANCE_ID = "suggestionBox1";
  private static final String SUGGESTIONBOX_ID = "suggestionBox_1";
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
    InMemoryCacheService sessionCache = new InMemoryCacheService();
    sessionCache.put(UserDetail.CURRENT_REQUESTER_KEY, new UserDetail());
    CacheServiceProvider.getRequestCacheService().put("@SessionCache@", sessionCache);

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
    SuggestionBox box = context.getSuggestionBox();

    controller.addSuggestion(context);

    ArgumentCaptor<Suggestion> suggestionArgument = ArgumentCaptor.forClass(Suggestion.class);
    verify(box.getSuggestions(), times(1)).
        add(suggestionArgument.capture(), anyCollection());
    Suggestion suggestion = suggestionArgument.getValue();
    assertThat(suggestion.getTitle(), is(title));
    assertThat(suggestion.getContent(), is(content));
  }

  @Test(expected = WebApplicationException.class)
  public void viewAnUnexistingSuggestionInAGivenSuggestionBox() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(Suggestion.NONE);

    controller.viewSuggestion(context);
  }

  @Test
  public void viewAInDraftSuggestionByUnknownUser() throws URISyntaxException {
    SuggestionBoxWebRequestContext context =
        prepareViewSuggestionTest(null, ContributionStatus.DRAFT);

    controller.viewSuggestion(context);

    SuggestionBox box = context.getSuggestionBox();
    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
    verify(context.getRequest(), times(1)).setAttribute("isModeratorView", false);
    verify(context.getRequest(), times(1)).setAttribute("isPublishable", false);
    verify(context.getRequest(), times(1)).setAttribute("isEditable", false);
  }

  @Test
  public void viewAInDraftSuggestionByReaderUser() throws URISyntaxException {
    SuggestionBoxWebRequestContext context =
        prepareViewSuggestionTest(SilverpeasRole.reader, ContributionStatus.DRAFT);

    controller.viewSuggestion(context);

    SuggestionBox box = context.getSuggestionBox();
    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
    verify(context.getRequest(), times(1))
        .setAttribute(eq("suggestion"), any(SuggestionEntity.class));
    verify(context.getRequest(), times(1)).setAttribute("isModeratorView", false);
    verify(context.getRequest(), times(1)).setAttribute("isPublishable", false);
    verify(context.getRequest(), times(1)).setAttribute("isEditable", false);
  }

  @Test
  public void viewAInDraftSuggestionByParticipantUser() throws URISyntaxException {
    SuggestionBoxWebRequestContext context =
        prepareViewSuggestionTest(SilverpeasRole.writer, ContributionStatus.DRAFT);

    controller.viewSuggestion(context);

    SuggestionBox box = context.getSuggestionBox();
    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
    verify(context.getRequest(), times(1))
        .setAttribute(eq("suggestion"), any(SuggestionEntity.class));
    verify(context.getRequest(), times(1)).setAttribute("isModeratorView", false);
    verify(context.getRequest(), times(1)).setAttribute("isPublishable", true);
    verify(context.getRequest(), times(1)).setAttribute("isEditable", true);
  }

  @Test
  public void viewAInDraftSuggestionByModeratorUser() throws URISyntaxException {
    SuggestionBoxWebRequestContext context =
        prepareViewSuggestionTest(SilverpeasRole.publisher, ContributionStatus.DRAFT);

    controller.viewSuggestion(context);

    SuggestionBox box = context.getSuggestionBox();
    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
    verify(context.getRequest(), times(1))
        .setAttribute(eq("suggestion"), any(SuggestionEntity.class));
    verify(context.getRequest(), times(1)).setAttribute("isModeratorView", false);
    verify(context.getRequest(), times(1)).setAttribute("isPublishable", true);
    verify(context.getRequest(), times(1)).setAttribute("isEditable", true);
  }

  @Test
  public void viewARefusedSuggestionByUnknownUser() throws URISyntaxException {
    SuggestionBoxWebRequestContext context =
        prepareViewSuggestionTest(null, ContributionStatus.REFUSED);

    controller.viewSuggestion(context);

    SuggestionBox box = context.getSuggestionBox();
    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
    verify(context.getRequest(), times(1)).setAttribute("isModeratorView", false);
    verify(context.getRequest(), times(1)).setAttribute("isPublishable", false);
    verify(context.getRequest(), times(1)).setAttribute("isEditable", false);
  }

  @Test
  public void viewARefusedSuggestionByReaderUser() throws URISyntaxException {
    SuggestionBoxWebRequestContext context =
        prepareViewSuggestionTest(SilverpeasRole.reader, ContributionStatus.REFUSED);

    controller.viewSuggestion(context);

    SuggestionBox box = context.getSuggestionBox();
    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
    verify(context.getRequest(), times(1)).setAttribute("isModeratorView", false);
    verify(context.getRequest(), times(1)).setAttribute("isPublishable", false);
    verify(context.getRequest(), times(1)).setAttribute("isEditable", false);
  }

  @Test
  public void viewARefusedSuggestionByParticipantUser() throws URISyntaxException {
    SuggestionBoxWebRequestContext context =
        prepareViewSuggestionTest(SilverpeasRole.writer, ContributionStatus.REFUSED);

    controller.viewSuggestion(context);

    SuggestionBox box = context.getSuggestionBox();
    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
    verify(context.getRequest(), times(1))
        .setAttribute(eq("suggestion"), any(SuggestionEntity.class));
    verify(context.getRequest(), times(1)).setAttribute("isModeratorView", false);
    verify(context.getRequest(), times(1)).setAttribute("isPublishable", true);
    verify(context.getRequest(), times(1)).setAttribute("isEditable", true);
  }

  @Test
  public void viewARefusedSuggestionByModeratorUser() throws URISyntaxException {
    SuggestionBoxWebRequestContext context =
        prepareViewSuggestionTest(SilverpeasRole.publisher, ContributionStatus.REFUSED);

    controller.viewSuggestion(context);

    SuggestionBox box = context.getSuggestionBox();
    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
    verify(context.getRequest(), times(1))
        .setAttribute(eq("suggestion"), any(SuggestionEntity.class));
    verify(context.getRequest(), times(1)).setAttribute("isModeratorView", false);
    verify(context.getRequest(), times(1)).setAttribute("isPublishable", true);
    verify(context.getRequest(), times(1)).setAttribute("isEditable", true);
  }

  @Test
  public void viewAPendingValidationSuggestionByUnknownUser() throws URISyntaxException {
    SuggestionBoxWebRequestContext context =
        prepareViewSuggestionTest(null, ContributionStatus.PENDING_VALIDATION);

    controller.viewSuggestion(context);

    SuggestionBox box = context.getSuggestionBox();
    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
    verify(context.getRequest(), times(1)).setAttribute("isModeratorView", false);
    verify(context.getRequest(), times(1)).setAttribute("isPublishable", false);
    verify(context.getRequest(), times(1)).setAttribute("isEditable", false);
  }

  @Test
  public void viewAPendingValidationSuggestionByReaderUser() throws URISyntaxException {
    SuggestionBoxWebRequestContext context =
        prepareViewSuggestionTest(SilverpeasRole.reader, ContributionStatus.PENDING_VALIDATION);

    controller.viewSuggestion(context);

    SuggestionBox box = context.getSuggestionBox();
    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
    verify(context.getRequest(), times(1)).setAttribute("isModeratorView", false);
    verify(context.getRequest(), times(1)).setAttribute("isPublishable", false);
    verify(context.getRequest(), times(1)).setAttribute("isEditable", false);
  }

  @Test
  public void viewAPendingValidationSuggestionByParticipantUser() throws URISyntaxException {
    SuggestionBoxWebRequestContext context =
        prepareViewSuggestionTest(SilverpeasRole.writer, ContributionStatus.PENDING_VALIDATION);

    controller.viewSuggestion(context);

    SuggestionBox box = context.getSuggestionBox();
    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
    verify(context.getRequest(), times(1)).setAttribute("isModeratorView", false);
    verify(context.getRequest(), times(1)).setAttribute("isPublishable", false);
    verify(context.getRequest(), times(1)).setAttribute("isEditable", false);
  }

  @Test
  public void viewAPendingValidationSuggestionByModeratorUser() throws URISyntaxException {
    SuggestionBoxWebRequestContext context =
        prepareViewSuggestionTest(SilverpeasRole.publisher, ContributionStatus.PENDING_VALIDATION);

    controller.viewSuggestion(context);

    SuggestionBox box = context.getSuggestionBox();
    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
    verify(context.getRequest(), times(1)).setAttribute("isModeratorView", true);
    verify(context.getRequest(), times(1)).setAttribute("isPublishable", false);
    verify(context.getRequest(), times(1)).setAttribute("isEditable", true);
  }

  @Test
  public void viewValidatedSuggestionByUnknownUser() throws URISyntaxException {
    SuggestionBoxWebRequestContext context =
        prepareViewSuggestionTest(null, ContributionStatus.VALIDATED);

    controller.viewSuggestion(context);

    SuggestionBox box = context.getSuggestionBox();
    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
    verify(context.getRequest(), times(1)).setAttribute("isModeratorView", false);
    verify(context.getRequest(), times(1)).setAttribute("isPublishable", false);
    verify(context.getRequest(), times(1)).setAttribute("isEditable", false);
  }

  @Test
  public void viewValidatedSuggestionByReaderUser() throws URISyntaxException {
    SuggestionBoxWebRequestContext context =
        prepareViewSuggestionTest(SilverpeasRole.reader, ContributionStatus.VALIDATED);

    controller.viewSuggestion(context);

    SuggestionBox box = context.getSuggestionBox();
    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
    verify(context.getRequest(), times(1)).setAttribute("isModeratorView", false);
    verify(context.getRequest(), times(1)).setAttribute("isPublishable", false);
    verify(context.getRequest(), times(1)).setAttribute("isEditable", false);
  }

  @Test
  public void viewValidatedSuggestionByParticipantUser() throws URISyntaxException {
    SuggestionBoxWebRequestContext context =
        prepareViewSuggestionTest(SilverpeasRole.writer, ContributionStatus.VALIDATED);

    controller.viewSuggestion(context);

    SuggestionBox box = context.getSuggestionBox();
    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
    verify(context.getRequest(), times(1)).setAttribute("isModeratorView", false);
    verify(context.getRequest(), times(1)).setAttribute("isPublishable", false);
    verify(context.getRequest(), times(1)).setAttribute("isEditable", false);
  }

  @Test
  public void viewValidatedSuggestionByModeratorUser() throws URISyntaxException {
    SuggestionBoxWebRequestContext context =
        prepareViewSuggestionTest(SilverpeasRole.publisher, ContributionStatus.VALIDATED);

    controller.viewSuggestion(context);

    SuggestionBox box = context.getSuggestionBox();
    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
    verify(context.getRequest(), times(1)).setAttribute("isModeratorView", false);
    verify(context.getRequest(), times(1)).setAttribute("isPublishable", false);
    verify(context.getRequest(), times(1)).setAttribute("isEditable", false);
  }

  @Test
  public void editASuggestion() throws URISyntaxException {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    Suggestion theSuggestion = aSuggestion();
    ReflectionTestUtils.setField(theSuggestion, "suggestionBox", box);
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(theSuggestion);

    controller.editSuggestion(context);

    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
  }

  @Test(expected = WebApplicationException.class)
  public void editAnUnexistingSuggestionInAGivenSuggestionBox() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(Suggestion.NONE);

    controller.editSuggestion(context);
  }

  @Test(expected = WebApplicationException.class)
  public void editANonDraftSuggestionInAGivenSuggestionBoxUserIsNotModerator() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    Suggestion suggestion = aSuggestionWithStatus(ContributionStatus.PENDING_VALIDATION);
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(suggestion);
    when(getOrganisationController().getUsersIdsByRoleNames(box.getComponentInstanceId(),
        CollectionUtil.asList(SilverpeasRole.admin.name(), SilverpeasRole.publisher.name())))
        .thenReturn(new String[]{"otherId"});

    controller.editSuggestion(context);
  }

  @Test
  public void editANonDraftSuggestionInAGivenSuggestionBoxUserIsModerator() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    Suggestion theSuggestion = aSuggestionWithStatus(ContributionStatus.PENDING_VALIDATION);
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(theSuggestion);
    String userId = context.getUser().getId();
    when(getOrganisationController().getUsersIdsByRoleNames(box.getComponentInstanceId(),
        CollectionUtil.asList(SilverpeasRole.admin.name(), SilverpeasRole.publisher.name())))
        .thenReturn(new String[]{userId});

    controller.editSuggestion(context);

    verify(box.getSuggestions(), times(1)).get(SUGGESTION_ID);
  }

  @Test(expected = WebApplicationException.class)
  public void editAValidatedSuggestionInAGivenSuggestionBoxUserIsModerator() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    Suggestion suggestion = aSuggestionWithStatus(ContributionStatus.VALIDATED);
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(suggestion);
    String userId = context.getUser().getId();
    when(getOrganisationController().getUsersIdsByRoleNames(box.getComponentInstanceId(),
        CollectionUtil.asList(SilverpeasRole.admin.name(), SilverpeasRole.publisher.name())))
        .thenReturn(new String[]{userId});

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
    Suggestion suggestion = aSuggestionWithStatus(withStatus);
    UserDetail user = context.getUser();
    when(suggestion.getCreator()).thenReturn(user);
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(suggestion);

    controller.updateSuggestion(context);

    verify(suggestion, times(1)).save();
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
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(Suggestion.NONE);

    controller.updateSuggestion(context);
  }

  @Test
  public void deleteASuggestion() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    Suggestion suggestion = aSuggestion();
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(suggestion);

    controller.deleteSuggestion(context);

    verify(box.getSuggestions(), times(1)).remove(suggestion);
  }

  @Test(expected = WebApplicationException.class)
  public void deleteAnUnexistingSuggestionInSuggestionBox() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(Suggestion.NONE);

    controller.deleteSuggestion(context);
  }

  @Test(expected = WebApplicationException.class)
  public void deleteANonDraftSuggestionInSuggestionBox() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    Suggestion suggestion = aSuggestionWithStatus(ContributionStatus.VALIDATED);
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(suggestion);

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
    Suggestion suggestion = aSuggestionWithStatus(withStatus);
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(suggestion);
    when(box.getSuggestions().publish(eq(suggestion))).thenReturn(suggestion);
    UserDetail user = context.getUser();
    when(suggestion.getCreator()).thenReturn(user);

    controller.publishSuggestion(context);

    verify(box.getSuggestions(), times(1)).publish(eq(suggestion));
  }

  @Test(expected = WebApplicationException.class)
  public void publishAnUnexistingSuggestionInAGivenSuggestionBox() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(Suggestion.NONE);

    controller.publishSuggestion(context);
  }

  @Test(expected = WebApplicationException.class)
  public void approveASuggestionRefusedWithWriterRoleAccess() {
    assertApproveASuggestion(SilverpeasRole.writer, ContributionStatus.REFUSED);
  }

  @Test(expected = WebApplicationException.class)
  public void approveASuggestionInDraftWithWriterRoleAccess() {
    assertApproveASuggestion(SilverpeasRole.writer, ContributionStatus.DRAFT);
  }

  @Test(expected = WebApplicationException.class)
  public void approveANonDraftOrRefusedSuggestionInAGivenSuggestionBoxWithWriterRoleAccess() {
    assertApproveASuggestion(SilverpeasRole.writer, ContributionStatus.UNKNOWN);
  }

  @Test(expected = WebApplicationException.class)
  public void approveASuggestionPendingValidationWithWriterRoleAccess() {
    assertApproveASuggestion(SilverpeasRole.writer, ContributionStatus.PENDING_VALIDATION);
  }

  @Test(expected = WebApplicationException.class)
  public void approveASuggestionRefusedWithPublisherRoleAccess() {
    assertApproveASuggestion(SilverpeasRole.publisher, ContributionStatus.REFUSED);
  }

  @Test(expected = WebApplicationException.class)
  public void approveASuggestionInDraftWithPublisherRoleAccess() {
    assertApproveASuggestion(SilverpeasRole.publisher, ContributionStatus.DRAFT);
  }

  @Test
  public void approveASuggestionPendingValidationWithPublisherRoleAccess() {
    assertApproveASuggestion(SilverpeasRole.publisher, ContributionStatus.PENDING_VALIDATION);
  }

  private void assertApproveASuggestion(SilverpeasRole userRoleAccess,
      ContributionStatus withStatus) {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    Suggestion suggestion = aSuggestionWithStatus(withStatus);
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(suggestion);
    when(box.getSuggestions().validate(eq(suggestion), any(ContributionValidation.class)))
        .thenReturn(suggestion);
    String userId = context.getUser().getId();
    when(getOrganisationController().getUsersIdsByRoleNames(box.getComponentInstanceId(),
        CollectionUtil.asList(SilverpeasRole.admin.name(), SilverpeasRole.publisher.name())))
        .thenReturn(new String[]{
            (userRoleAccess.isGreaterThanOrEquals(SilverpeasRole.publisher) ? userId : "otherId")});

    controller.approveSuggestion(context);

    verify(box.getSuggestions(), times(1))
        .validate(eq(suggestion), any(ContributionValidation.class));
  }

  @Test(expected = WebApplicationException.class)
  public void approveAnUnexistingSuggestionInAGivenSuggestionBox() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(Suggestion.NONE);

    controller.approveSuggestion(context);
  }

  @Test(expected = WebApplicationException.class)
  public void refuseASuggestionRefusedWithWriterRoleAccess() {
    assertRefuseASuggestion(SilverpeasRole.writer, ContributionStatus.REFUSED, null);
  }

  @Test(expected = WebApplicationException.class)
  public void refuseASuggestionInDraftWithWriterRoleAccess() {
    assertRefuseASuggestion(SilverpeasRole.writer, ContributionStatus.DRAFT, null);
  }

  @Test(expected = WebApplicationException.class)
  public void refuseANonDraftOrRefusedSuggestionInAGivenSuggestionBoxWithWriterRoleAccess() {
    assertRefuseASuggestion(SilverpeasRole.writer, ContributionStatus.UNKNOWN, null);
  }

  @Test(expected = WebApplicationException.class)
  public void refuseASuggestionPendingValidationWithWriterRoleAccess() {
    assertRefuseASuggestion(SilverpeasRole.writer, ContributionStatus.PENDING_VALIDATION, null);
  }

  @Test(expected = WebApplicationException.class)
  public void refuseASuggestionRefusedWithPublisherRoleAccess() {
    assertRefuseASuggestion(SilverpeasRole.publisher, ContributionStatus.REFUSED, null);
  }

  @Test(expected = WebApplicationException.class)
  public void refuseASuggestionInDraftWithPublisherRoleAccess() {
    assertRefuseASuggestion(SilverpeasRole.publisher, ContributionStatus.DRAFT, null);
  }

  @Test(expected = WebApplicationException.class)
  public void refuseWithoutCommentASuggestionPendingValidationWithPublisherRoleAccess() {
    assertRefuseASuggestion(SilverpeasRole.publisher, ContributionStatus.PENDING_VALIDATION, null);
  }

  @Test
  public void refuseWithCommentASuggestionPendingValidationWithPublisherRoleAccess() {
    assertRefuseASuggestion(SilverpeasRole.publisher, ContributionStatus.PENDING_VALIDATION,
        "A comment.");
  }

  private void assertRefuseASuggestion(SilverpeasRole userRoleAccess, ContributionStatus withStatus,
      final String withComment) {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    Suggestion suggestion = aSuggestionWithStatus(withStatus);
    if (StringUtil.isDefined(withComment)) {
      when(context.getRequest().getParameter("comment")).thenReturn(withComment);
    }
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(suggestion);
    when(box.getSuggestions().validate(eq(suggestion), any(ContributionValidation.class)))
        .thenReturn(suggestion);
    String userId = context.getUser().getId();
    when(getOrganisationController().getUsersIdsByRoleNames(box.getComponentInstanceId(),
        CollectionUtil.asList(SilverpeasRole.admin.name(), SilverpeasRole.publisher.name())))
        .thenReturn(new String[]{
            (userRoleAccess.isGreaterThanOrEquals(SilverpeasRole.publisher) ? userId : "otherId")});

    controller.refuseSuggestion(context);

    verify(box.getSuggestions(), times(1))
        .validate(eq(suggestion), any(ContributionValidation.class));
  }

  @Test(expected = WebApplicationException.class)
  public void refuseAnUnexistingSuggestionInAGivenSuggestionBox() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(Suggestion.NONE);

    controller.refuseSuggestion(context);
  }

  private SuggestionBoxWebRequestContext prepareViewSuggestionTest(SilverpeasRole greaterUserRole,
      ContributionStatus suggestionStatus) {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    when(context.getGreaterUserRole())
        .thenReturn(greaterUserRole != null ? greaterUserRole : SilverpeasRole.reader);
    SuggestionBox box = context.getSuggestionBox();
    Suggestion theSuggestion = aSuggestionWithStatus(suggestionStatus);
    when(box.getSuggestions().get(SUGGESTION_ID)).thenReturn(theSuggestion);
    boolean isPublishable = greaterUserRole != null &&
        (theSuggestion.getValidation().isInDraft() || theSuggestion.getValidation().isRefused()) &&
        greaterUserRole.isGreaterThanOrEquals(SilverpeasRole.writer);
    when(theSuggestion.isPublishableBy(any(UserDetail.class))).thenReturn(isPublishable);

    final String[] roles;
    if (greaterUserRole != null) {
      roles = new String[]{greaterUserRole.getName()};
    } else {
      roles = new String[0];
    }
    when(getOrganisationController()
        .getUserProfiles(context.getUser().getId(), box.getComponentInstanceId()))
        .thenReturn(roles);
    return context;
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
    when(context.getComponentUriBase()).thenReturn("/" + COMPONENT_INSTANCE_ID);
    when(context.getMessager()).thenReturn(WebMessager.getInstance());
    SuggestionBox box = aSuggestionBox();
    when(context.getSuggestionBox()).thenReturn(box);
    NavigationContext navigationContext = NavigationContext.get(context);
    when(context.getNavigationContext()).thenReturn(navigationContext);
    return context;
  }

  private OrganizationController getOrganisationController() {
    OrganizationControllerMockWrapper mockWrapper =
        appContext.getBean(OrganizationControllerMockWrapper.class);
    return mockWrapper.getOrganizationControllerMock();
  }

  private PersonalizationService getPersonalizationService() {
    PersonalizationServiceMockWrapper mockWrapper = appContext.
        getBean(PersonalizationServiceMockWrapper.class);
    return mockWrapper.getPersonalizationServiceMock();
  }

  private SuggestionBox aSuggestionBox() {
    UserDetail author = aUser();
    SuggestionBox box = mock(SuggestionBox.class);
    when(box.getComponentInstanceId()).thenReturn(COMPONENT_INSTANCE_ID);
    when(box.getId()).thenReturn(SUGGESTIONBOX_ID);
    when(box.getCreator()).thenReturn(author);
    when(box.getLastUpdater()).thenReturn(author);

    SuggestionCollection suggestions = mock(SuggestionCollection.class);
    when(box.getSuggestions()).thenReturn(suggestions);
    return box;
  }

  private Suggestion aSuggestionWithStatus(ContributionStatus status) {
    Suggestion suggestion = mock(Suggestion.class);
    ContributionValidation validation = mock(ContributionValidation.class);
    when(suggestion.isDefined()).thenReturn(true);
    when(suggestion.getValidation()).thenReturn(validation);
    when(validation.getStatus()).thenReturn(status);
    switch (status) {
      case DRAFT:
        when(validation.isInDraft()).thenReturn(true);
        break;
      case PENDING_VALIDATION:
        when(validation.isPendingValidation()).thenReturn(true);
        break;
      case REFUSED:
        when(validation.isRefused()).thenReturn(true);
        break;
      case VALIDATED:
        when(validation.isValidated()).thenReturn(true);
        break;
    }

    return suggestion;
  }

  private Suggestion aSuggestion() {
    Suggestion suggestion = new Suggestion("A suggestion title");
    ReflectionTestUtils.setField(suggestion, "id", new UuidIdentifier().fromString(SUGGESTION_ID));
    suggestion.setCreator(aUser());
    suggestion.setContent("A suggestion content");
    suggestion.setRating(new ContributionRating(
        new ContributionRatingPK(SUGGESTION_ID, SUGGESTIONBOX_ID, Suggestion.TYPE)));
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
