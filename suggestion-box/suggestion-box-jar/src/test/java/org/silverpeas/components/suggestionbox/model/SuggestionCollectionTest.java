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

import com.silverpeas.notification.builder.UserNotificationBuider;
import com.silverpeas.notification.builder.helper.UserNotificationHelper;
import com.silverpeas.util.CollectionUtil;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.stubbing.answers.Returns;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.silverpeas.components.suggestionbox.mock.OrganisationControllerMockWrapper;
import org.silverpeas.components.suggestionbox.mock.SuggestionBoxRepositoryMockWrapper;
import org.silverpeas.components.suggestionbox.mock.SuggestionRepositoryMockWrapper;
import org.silverpeas.components.suggestionbox.notification
    .SuggestionBoxSubscriptionUserNotification;
import org.silverpeas.components.suggestionbox.notification
    .SuggestionPendingValidationUserNotification;
import org.silverpeas.components.suggestionbox.notification.SuggestionValidationUserNotification;
import org.silverpeas.components.suggestionbox.repository.SuggestionBoxRepository;
import org.silverpeas.components.suggestionbox.repository.SuggestionRepository;
import org.silverpeas.contribution.ContributionStatus;
import org.silverpeas.contribution.model.ContributionValidation;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.repository.OperationContext;
import org.silverpeas.wysiwyg.control.WysiwygController;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


/**
 * Unit test on the SuggestionCollection features. For doing, the test mocks all of the
 * service dependencies.
 * @author mmoquillon
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({WysiwygController.class, UserNotificationHelper.class})
public class SuggestionCollectionTest {

  private static AbstractApplicationContext context;
  private SuggestionBox suggestionBox;

  private static final String appInstanceId = "suggestionBox1";
  private static final String userId = "0";

  public SuggestionCollectionTest() {
  }

  @BeforeClass
  public static void bootstrapSpringContext() {
    context = new ClassPathXmlApplicationContext("/spring-suggestion-box-mock.xml",
        "/spring-suggestion-box-embedded-datasource.xml");
  }

  @AfterClass
  public static void shutdownSpringContext() {
    context.close();
  }

  @Before
  public void setUp() {
    suggestionBox = prepareASuggestionBox();
    SuggestionBoxRepository boxRepository = getSuggestionBoxRepository();
    when(boxRepository.getByComponentInstanceId(suggestionBox.getComponentInstanceId()))
        .thenReturn(suggestionBox);
    PowerMockito.mockStatic(WysiwygController.class);
    PowerMockito.mockStatic(UserNotificationHelper.class);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void addASuggestionIntoASuggestionBox() {
    Suggestion suggestion = new Suggestion("My suggestion");
    suggestion.setContent("the content of my suggestion");
    suggestion.setCreator(suggestionBox.getCreator());

    suggestionBox.getSuggestions().add(suggestion);

    SuggestionRepository suggestionRepository = getSuggestionRepository();
    verify(suggestionRepository, times(1)).save(any(OperationContext.class), eq(suggestion));
    verify(suggestionRepository, times(0)).index(suggestion);
  }

  @Test
  public void publishASuggestionOfASuggestionBoxWithUserAccessRole() {
    Suggestion suggestion = new Suggestion("My suggestion");
    suggestion.setSuggestionBox(suggestionBox);
    suggestion.setContent("the content of my suggestion");
    suggestion.setCreator(suggestionBox.getCreator());

    OrganisationController organisationController = getOrganisationController();
    when(organisationController.getUserProfiles(suggestionBox.getCreator().getId(),
        suggestionBox.getComponentInstanceId()))
        .thenReturn(new String[]{SilverpeasRole.user.name()});

    SuggestionRepository suggestionRepository = getSuggestionRepository();
    when(suggestionRepository.findByCriteria(any(SuggestionCriteria.class)))
        .then(new Returns(CollectionUtil.asList(suggestion)));

    Suggestion actual = suggestionBox.getSuggestions().publish(suggestion);

    verify(suggestionRepository, times(0)).save(any(OperationContext.class), eq(actual));
    assertThat(actual.getValidation().getStatus(), is(ContributionStatus.DRAFT));

    PowerMockito.verifyStatic(times(0));
    UserNotificationHelper.buildAndSend(any(UserNotificationBuider.class));

    verify(suggestionRepository, times(0)).index(actual);
  }

  @Test
  public void publishASuggestionOfASuggestionBoxWithWriterAccessRole() {
    Suggestion suggestion = new Suggestion("My suggestion");
    suggestion.setSuggestionBox(suggestionBox);
    suggestion.setContent("the content of my suggestion");
    suggestion.setCreator(suggestionBox.getCreator());

    OrganisationController organisationController = getOrganisationController();
    when(organisationController.getUserProfiles(suggestionBox.getCreator().getId(),
        suggestionBox.getComponentInstanceId()))
        .thenReturn(new String[]{SilverpeasRole.writer.name()});

    SuggestionRepository suggestionRepository = getSuggestionRepository();
    when(suggestionRepository.findByCriteria(any(SuggestionCriteria.class)))
        .then(new Returns(CollectionUtil.asList(suggestion)));

    Suggestion actual = suggestionBox.getSuggestions().publish(suggestion);

    verify(suggestionRepository, times(1)).save(any(OperationContext.class), eq(actual));
    assertThat(actual.getValidation().getStatus(), is(ContributionStatus.PENDING_VALIDATION));

    PowerMockito.verifyStatic(times(1));
    UserNotificationHelper.buildAndSend(any(SuggestionPendingValidationUserNotification.class));

    verify(suggestionRepository, times(0)).index(actual);
  }

  @Test
  public void publishASuggestionOfASuggestionBoxWithPublisherAccessRole() {
    Suggestion suggestion = new Suggestion("My suggestion");
    suggestion.setSuggestionBox(suggestionBox);
    suggestion.setContent("the content of my suggestion");
    suggestion.setCreator(suggestionBox.getCreator());

    OrganisationController organisationController = getOrganisationController();
    when(organisationController.getUserProfiles(suggestionBox.getCreator().getId(),
        suggestionBox.getComponentInstanceId()))
        .thenReturn(new String[]{SilverpeasRole.publisher.name()});

    SuggestionRepository suggestionRepository = getSuggestionRepository();
    when(suggestionRepository.findByCriteria(any(SuggestionCriteria.class)))
        .then(new Returns(CollectionUtil.asList(suggestion)));

    Suggestion actual = suggestionBox.getSuggestions().publish(suggestion);

    verify(suggestionRepository, times(1)).save(any(OperationContext.class), eq(actual));
    assertThat(actual.getValidation().getStatus(), is(ContributionStatus.VALIDATED));

    PowerMockito.verifyStatic(times(1));
    UserNotificationHelper.buildAndSend(any(SuggestionBoxSubscriptionUserNotification.class));

    verify(suggestionRepository, times(1)).index(actual);
  }

  @Test
  public void approveASuggestionInDraftOfASuggestionBox() {
    Suggestion suggestion = new Suggestion("My suggestion");
    suggestion.setSuggestionBox(suggestionBox);
    suggestion.setContent("the content of my suggestion");
    suggestion.setCreator(suggestionBox.getCreator());
    suggestion.getValidation().setStatus(ContributionStatus.DRAFT);

    OrganisationController organisationController = getOrganisationController();
    when(organisationController.getUserProfiles(suggestionBox.getCreator().getId(),
        suggestionBox.getComponentInstanceId()))
        .thenReturn(new String[]{SilverpeasRole.writer.name()});

    SuggestionRepository suggestionRepository = getSuggestionRepository();
    when(suggestionRepository.findByCriteria(any(SuggestionCriteria.class)))
        .then(new Returns(CollectionUtil.asList(suggestion)));

    ContributionValidation validation =
        new ContributionValidation(ContributionStatus.VALIDATED, aUser(), new Date(), "A comment");

    Suggestion actual = suggestionBox.getSuggestions().validate(suggestion, validation);

    verify(suggestionRepository, times(0)).save(any(OperationContext.class), eq(actual));
    assertThat(actual.getValidation().getStatus(), is(ContributionStatus.DRAFT));

    PowerMockito.verifyStatic(times(0));
    UserNotificationHelper.buildAndSend(any(UserNotificationBuider.class));

    verify(suggestionRepository, times(0)).index(actual);
  }

  @Test
  public void approveASuggestionRefusedOfASuggestionBox() {
    Suggestion suggestion = new Suggestion("My suggestion");
    suggestion.setSuggestionBox(suggestionBox);
    suggestion.setContent("the content of my suggestion");
    suggestion.setCreator(suggestionBox.getCreator());
    suggestion.getValidation().setStatus(ContributionStatus.REFUSED);

    OrganisationController organisationController = getOrganisationController();
    when(organisationController.getUserProfiles(suggestionBox.getCreator().getId(),
        suggestionBox.getComponentInstanceId()))
        .thenReturn(new String[]{SilverpeasRole.writer.name()});

    SuggestionRepository suggestionRepository = getSuggestionRepository();
    when(suggestionRepository.findByCriteria(any(SuggestionCriteria.class)))
        .then(new Returns(CollectionUtil.asList(suggestion)));

    ContributionValidation validation =
        new ContributionValidation(ContributionStatus.VALIDATED, aUser(), new Date(), "A comment");

    Suggestion actual = suggestionBox.getSuggestions().validate(suggestion, validation);

    verify(suggestionRepository, times(0)).save(any(OperationContext.class), eq(actual));
    assertThat(actual.getValidation().getStatus(), is(ContributionStatus.REFUSED));

    PowerMockito.verifyStatic(times(0));
    UserNotificationHelper.buildAndSend(any(UserNotificationBuider.class));

    verify(suggestionRepository, times(0)).index(actual);
  }

  @Test
  public void approveASuggestionPendingValidationOfASuggestionBox() {
    Suggestion suggestion = new Suggestion("My suggestion");
    suggestion.setSuggestionBox(suggestionBox);
    suggestion.setContent("the content of my suggestion");
    suggestion.setCreator(suggestionBox.getCreator());
    suggestion.getValidation().setStatus(ContributionStatus.PENDING_VALIDATION);

    OrganisationController organisationController = getOrganisationController();
    when(organisationController.getUserProfiles(suggestionBox.getCreator().getId(),
        suggestionBox.getComponentInstanceId()))
        .thenReturn(new String[]{SilverpeasRole.publisher.name()});

    SuggestionRepository suggestionRepository = getSuggestionRepository();
    when(suggestionRepository.findByCriteria(any(SuggestionCriteria.class)))
        .then(new Returns(CollectionUtil.asList(suggestion)));

    ContributionValidation validation =
        new ContributionValidation(ContributionStatus.VALIDATED, aUser(), new Date(), "A comment");

    Suggestion actual = suggestionBox.getSuggestions().validate(suggestion, validation);

    verify(suggestionRepository, times(1)).save(any(OperationContext.class), eq(actual));
    assertThat(actual.getValidation().getStatus(), is(ContributionStatus.VALIDATED));

    PowerMockito.verifyStatic(times(2));
    UserNotificationHelper.buildAndSend(any(SuggestionBoxSubscriptionUserNotification.class));
    UserNotificationHelper.buildAndSend(any(SuggestionValidationUserNotification.class));

    verify(suggestionRepository, times(1)).index(actual);
  }

  @Test
  public void refuseASuggestionPendingValidationOfASuggestionBox() {
    Suggestion suggestion = new Suggestion("My suggestion");
    suggestion.setSuggestionBox(suggestionBox);
    suggestion.setContent("the content of my suggestion");
    suggestion.setCreator(suggestionBox.getCreator());
    suggestion.getValidation().setStatus(ContributionStatus.PENDING_VALIDATION);

    OrganisationController organisationController = getOrganisationController();
    when(organisationController.getUserProfiles(suggestionBox.getCreator().getId(),
        suggestionBox.getComponentInstanceId()))
        .thenReturn(new String[]{SilverpeasRole.publisher.name()});

    SuggestionRepository suggestionRepository = getSuggestionRepository();
    when(suggestionRepository.findByCriteria(any(SuggestionCriteria.class)))
        .then(new Returns(CollectionUtil.asList(suggestion)));

    ContributionValidation validation =
        new ContributionValidation(ContributionStatus.REFUSED, aUser(), new Date(), "A comment");

    Suggestion actual = suggestionBox.getSuggestions().validate(suggestion, validation);

    verify(suggestionRepository, times(1)).save(any(OperationContext.class), eq(actual));
    assertThat(actual.getValidation().getStatus(), is(ContributionStatus.REFUSED));

    PowerMockito.verifyStatic(times(1));
    UserNotificationHelper.buildAndSend(any(SuggestionValidationUserNotification.class));

    verify(suggestionRepository, times(0)).index(actual);
  }

  private OrganisationController getOrganisationController() {
    OrganisationControllerMockWrapper mockWrapper = context.
        getBean(OrganisationControllerMockWrapper.class);
    return mockWrapper.getMock();
  }

  private SuggestionRepository getSuggestionRepository() {
    SuggestionRepositoryMockWrapper mockWrapper = context.
        getBean(SuggestionRepositoryMockWrapper.class);
    return mockWrapper.getMock();
  }

  private SuggestionBox prepareASuggestionBox() {
    SuggestionBox suggestionBox = new SuggestionBox(appInstanceId);
    suggestionBox.setCreator(aUser());
    ReflectionTestUtils
        .setField(suggestionBox, "id", new UuidIdentifier().fromString("suggestionBox2"));
    return suggestionBox;
  }

  private UserDetail aUser() {
    UserDetail user = new UserDetail();
    user.setId(userId);
    user.setFirstName("Toto");
    user.setLastName("Chez-les-papoos");
    return user;
  }

  private SuggestionBoxRepository getSuggestionBoxRepository() {
    SuggestionBoxRepositoryMockWrapper mockWrapper =
        context.getBean(SuggestionBoxRepositoryMockWrapper.class);
    return mockWrapper.getMock();
  }
}
