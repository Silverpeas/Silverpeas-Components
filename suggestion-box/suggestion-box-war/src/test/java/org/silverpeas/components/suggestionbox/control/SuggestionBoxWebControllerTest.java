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
import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.web.TestResources;
import com.silverpeas.web.mock.OrganizationControllerMockWrapper;
import com.silverpeas.web.mock.PersonalizationServiceMockWrapper;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.WebMessager;
import com.stratelia.webactiv.SilverpeasRole;
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
import org.silverpeas.contribution.model.ContributionValidation;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
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
@RunWith(PowerMockRunner.class)
@PrepareForTest({UserNotificationHelper.class})
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
    UserPreferences preferences = new UserPreferences(TestResources.DEFAULT_LANGUAGE, "", "", false,
        true, true,
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
  public void viewASuggestion() throws URISyntaxException {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    Suggestion theSuggestion = aSuggestion();
    ReflectionTestUtils.setField(theSuggestion, "suggestionBox", box);
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(theSuggestion);

    controller.viewSuggestion(context);

    verify(suggestionBoxService, times(1)).findSuggestionById(box, SUGGESTION_ID);

    verify(context.getRequest(), times(0)).setAttribute("edit", "edit");
  }

  @Test(expected = WebApplicationException.class)
  public void viewAnUnexistingSuggestionInAGivenSuggestionBox() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(Suggestion.NONE);

    controller.viewSuggestion(context);
  }

  @Test
  public void viewANonDraftSuggestionInAGivenSuggestionBoxUserIsNotModerator() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    Suggestion theSuggestion = aSuggestionWithStatus(ContributionStatus.PENDING_VALIDATION);
    ReflectionTestUtils.setField(theSuggestion, "suggestionBox", box);
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(theSuggestion);
    when(getOrganisationController().getUsersIdsByRoleNames(box.getComponentInstanceId(),
        CollectionUtil.asList(SilverpeasRole.admin.name(), SilverpeasRole.publisher.name())))
        .thenReturn(new String[]{"otherId"});

    controller.viewSuggestion(context);

    verify(suggestionBoxService, times(1)).findSuggestionById(box, SUGGESTION_ID);

    verify(context.getRequest(), times(0)).setAttribute("edit", "edit");
  }

  @Test
  public void viewANonDraftSuggestionInAGivenSuggestionBoxUserIsModerator() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    Suggestion theSuggestion = aSuggestionWithStatus(ContributionStatus.PENDING_VALIDATION);
    ReflectionTestUtils.setField(theSuggestion, "suggestionBox", box);
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(theSuggestion);
    String userId = context.getUser().getId();
    when(getOrganisationController().getUsersIdsByRoleNames(box.getComponentInstanceId(),
        CollectionUtil.asList(SilverpeasRole.admin.name(), SilverpeasRole.publisher.name())))
        .thenReturn(new String[]{userId});

    controller.viewSuggestion(context);

    verify(suggestionBoxService, times(1)).findSuggestionById(box, SUGGESTION_ID);

    verify(context.getRequest(), times(0)).setAttribute("edit", "edit");
  }

  @Test
  public void viewAValidatedSuggestionInAGivenSuggestionBoxUserIsModerator() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    Suggestion theSuggestion = aSuggestionWithStatus(ContributionStatus.VALIDATED);
    ReflectionTestUtils.setField(theSuggestion, "suggestionBox", box);
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(theSuggestion);
    String userId = context.getUser().getId();
    when(getOrganisationController().getUsersIdsByRoleNames(box.getComponentInstanceId(),
        CollectionUtil.asList(SilverpeasRole.admin.name(), SilverpeasRole.publisher.name())))
        .thenReturn(new String[]{userId});

    controller.viewSuggestion(context);

    verify(suggestionBoxService, times(1)).findSuggestionById(box, SUGGESTION_ID);

    verify(context.getRequest(), times(0)).setAttribute("edit", "edit");
  }

  @Test
  public void editASuggestion() throws URISyntaxException {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    Suggestion theSuggestion = aSuggestion();
    ReflectionTestUtils.setField(theSuggestion, "suggestionBox", box);
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(theSuggestion);

    controller.editSuggestion(context);

    verify(suggestionBoxService, times(1)).findSuggestionById(box, SUGGESTION_ID);

    verify(context.getRequest(), times(1)).setAttribute("edit", "edit");
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
  public void editANonDraftSuggestionInAGivenSuggestionBoxUserIsNotModerator() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID))
        .thenReturn(aSuggestionWithStatus(ContributionStatus.PENDING_VALIDATION));
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
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    Suggestion theSuggestion = aSuggestionWithStatus(ContributionStatus.PENDING_VALIDATION);
    ReflectionTestUtils.setField(theSuggestion, "suggestionBox", box);
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(theSuggestion);
    String userId = context.getUser().getId();
    when(getOrganisationController().getUsersIdsByRoleNames(box.getComponentInstanceId(),
        CollectionUtil.asList(SilverpeasRole.admin.name(), SilverpeasRole.publisher.name())))
        .thenReturn(new String[]{userId});

    controller.editSuggestion(context);

    verify(suggestionBoxService, times(1)).findSuggestionById(box, SUGGESTION_ID);

    verify(context.getRequest(), times(1)).setAttribute("edit", "edit");
  }

  @Test(expected = WebApplicationException.class)
  public void editAValidatedSuggestionInAGivenSuggestionBoxUserIsModerator() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID))
        .thenReturn(aSuggestionWithStatus(ContributionStatus.VALIDATED));
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
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    Suggestion suggestion = aSuggestionWithStatus(withStatus);
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(suggestion);
    when(suggestionBoxService.validateSuggestion(eq(box), eq(suggestion), any(
        ContributionValidation.class))).thenReturn(suggestion);
    String userId = context.getUser().getId();
    when(getOrganisationController().getUsersIdsByRoleNames(box.getComponentInstanceId(),
        CollectionUtil.asList(SilverpeasRole.admin.name(), SilverpeasRole.publisher.name())))
        .thenReturn(new String[]{
          (userRoleAccess.isGreaterThanOrEquals(SilverpeasRole.publisher) ? userId : "otherId")});

    PowerMockito.mockStatic(UserNotificationHelper.class);
    controller.approveSuggestion(context);

    verify(suggestionBoxService, times(1)).validateSuggestion(eq(box), eq(suggestion), any(
        ContributionValidation.class));
  }

  @Test(expected = WebApplicationException.class)
  public void approveAnUnexistingSuggestionInAGivenSuggestionBox() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(Suggestion.NONE);

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
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    Suggestion suggestion = aSuggestionWithStatus(withStatus);
    if (StringUtil.isDefined(withComment)) {
      when(context.getRequest().getParameter("comment")).thenReturn(withComment);
    }
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(suggestion);
    when(suggestionBoxService.validateSuggestion(eq(box), eq(suggestion), any(
        ContributionValidation.class))).thenReturn(suggestion);
    String userId = context.getUser().getId();
    when(getOrganisationController().getUsersIdsByRoleNames(box.getComponentInstanceId(),
        CollectionUtil.asList(SilverpeasRole.admin.name(), SilverpeasRole.publisher.name())))
        .thenReturn(new String[]{
          (userRoleAccess.isGreaterThanOrEquals(SilverpeasRole.publisher) ? userId : "otherId")});

    PowerMockito.mockStatic(UserNotificationHelper.class);
    controller.refuseSuggestion(context);

    verify(suggestionBoxService, times(1)).validateSuggestion(eq(box), eq(suggestion), any(
        ContributionValidation.class));
  }

  @Test(expected = WebApplicationException.class)
  public void refuseAnUnexistingSuggestionInAGivenSuggestionBox() {
    SuggestionBoxWebRequestContext context = aSuggestionBoxWebRequestContext();
    when(context.getPathVariables().get("id")).thenReturn(SUGGESTION_ID);
    SuggestionBox box = context.getSuggestionBox();
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    when(suggestionBoxService.findSuggestionById(box, SUGGESTION_ID)).thenReturn(Suggestion.NONE);

    controller.refuseSuggestion(context);
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

  private OrganisationController getOrganisationController() {
    OrganizationControllerMockWrapper mockWrapper = appContext.getBean(
        OrganizationControllerMockWrapper.class);
    return mockWrapper.getOrganizationControllerMock();
  }

  private SuggestionBoxService getSuggestionBoxService() {
    SuggestionBoxServiceMockWrapper mockWrapper = appContext.getBean(
        SuggestionBoxServiceMockWrapper.class);
    return mockWrapper.getMock();
  }

  private PersonalizationService getPersonalizationService() {
    PersonalizationServiceMockWrapper mockWrapper = appContext.
        getBean(PersonalizationServiceMockWrapper.class);
    return mockWrapper.getPersonalizationServiceMock();
  }

  private SuggestionBox aSuggestionBox() {
    SuggestionBox box = new SuggestionBox(COMPONENT_INSTANCE_ID);
    ReflectionTestUtils.setField(box, "id", new UuidIdentifier().fromString(SUGGESTIONBOX_ID));
    box.setCreator(aUser());
    return box;
  }

  private Suggestion aSuggestionWithStatus(ContributionStatus status) {
    Suggestion suggestion = aSuggestion();
    suggestion.getValidation().setStatus(status);
    return suggestion;
  }

  private Suggestion aSuggestion() {
    Suggestion suggestion = new Suggestion("A suggestion title");
    ReflectionTestUtils.setField(suggestion, "id", new UuidIdentifier().fromString(SUGGESTION_ID));
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
