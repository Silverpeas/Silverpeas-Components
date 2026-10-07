/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.infoletter.control;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.silverpeas.components.infoletter.model.InfoLetter;
import org.silverpeas.components.infoletter.model.InfoLetterPublicationPdC;
import org.silverpeas.components.infoletter.model.InfoLetterService;
import org.silverpeas.components.infoletter.notification.InfoLetterSubscriptionPublicationUserNotification;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.notification.user.builder.UserNotificationBuilder;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.service.PdcClassificationService;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.subscription.ContributionSubscribersProvider;
import org.silverpeas.core.subscription.ResourceSubscriptionService;
import org.silverpeas.core.subscription.SubscriberDirective;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.extension.LocalizationBundleStub;
import org.silverpeas.kernel.test.extension.SettingBundleStub;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the sending of an issue of a newsletter to its recipients, by mail or by
 * notification according to the setting of the application. Whatever the way the issue is sent,
 * the subscribers to a position on the PdC on which the issue is classified receive it with the
 * subscribers of the newsletter, provided they can access the application.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class InfoLetterIssueSendingTest {

  private static final String COMPONENT_ID = "infoLetter5";
  private static final String ISSUE_ID = "23";
  private static final String MANAGER = "1";
  private static final String A_SUBSCRIBER = "3";
  private static final String ANOTHER_SUBSCRIBER = "5";
  private static final String A_SUBSCRIBER_ON_THE_PDC = "21";
  private static final String ANOTHER_SUBSCRIBER_ON_THE_PDC = "23";
  private static final String AN_EXTERNAL_SUBSCRIBER = "someone@elsewhere.org";
  private static final String A_POSITION_ON_THE_PDC =
      "{\"positions\":[{\"values\":[{\"id\":\"/0/12/\",\"axisId\":3,\"treeId\":\"1\"}]}]}";

  @RegisterExtension
  static LocalizationBundleStub infoLetterBundle = new LocalizationBundleStub(
      "org.silverpeas.infoLetter.multilang.infoLetterBundle", LocalizationBundleStub.LANGUAGE_ALL);
  @RegisterExtension
  static SettingBundleStub infoLetterSettings =
      new SettingBundleStub("org.silverpeas.infoLetter.settings.infoLetterSettings");

  @TestManagedMock
  InfoLetterService service;
  @TestManagedMock
  Administration administration;
  @TestManagedMock
  ComponentAccessControl accessControl;
  @TestManagedMock
  PdcClassificationService pdcClassificationService;
  @TestManagedBean
  SubscribersOnThePdcProvider pdc;

  private MainSessionController mainController;
  private InfoLetterSessionController controller;
  private final SubscriptionSubscriberList subscribers = new SubscriptionSubscriberList();
  private final Set<String> externalSubscribers = new HashSet<>();

  @BeforeEach
  void setUpTheController() throws Exception {
    infoLetterBundle.put("infoLetter.emailSubject", "Newsletter: ");
    infoLetterSettings.put("SMTPMimeMultipart", "related");

    final UserDetail manager = aUser(MANAGER);
    mainController = mock(MainSessionController.class);
    when(mainController.getFavoriteLanguage()).thenReturn("fr");
    when(mainController.getCurrentUserDetail()).thenReturn(manager);
    final ComponentContext context = mock(ComponentContext.class);
    when(context.getCurrentComponentName()).thenReturn("infoLetter");
    when(context.getCurrentComponentId()).thenReturn(COMPONENT_ID);
    controller = new InfoLetterSessionController(mainController, context);

    when(administration.getUserDetail(anyString())).thenAnswer(
        invocation -> aUser(invocation.getArgument(0)));
    when(accessControl.isUserAuthorized(anyString(), eq(COMPONENT_ID))).thenReturn(true);

    final InfoLetter newsletter = mock(InfoLetter.class);
    when(service.getInfoLetters(COMPONENT_ID)).thenReturn(List.of(newsletter));
    when(service.getInternalSubscribers(COMPONENT_ID)).thenReturn(subscribers);
    when(service.getEmailsExternalsSubscribers(any())).thenReturn(externalSubscribers);
    when(service.sendLetterByMail(any(), anyString(), anySet(), anyString(), any())).thenReturn(
        Set.of());
  }

  @Test
  void theIssueIsMailedToTheSubscribersOfTheNewsletter() {
    theIssuesAreSentByMail(true);
    subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));
    subscribers.add(UserSubscriptionSubscriber.from(ANOTHER_SUBSCRIBER));

    controller.notifyInternalSubscribers(anIssue());

    assertThat(recipientsOfTheMail(),
        containsInAnyOrder(emailOf(A_SUBSCRIBER), emailOf(ANOTHER_SUBSCRIBER)));
  }

  @Test
  void theIssueIsAlsoMailedToTheSubscribersOnThePdc() {
    theIssuesAreSentByMail(true);
    subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));
    pdc.subscribe(A_SUBSCRIBER_ON_THE_PDC, ANOTHER_SUBSCRIBER_ON_THE_PDC);

    controller.notifyInternalSubscribers(anIssue());

    assertThat(recipientsOfTheMail(),
        containsInAnyOrder(emailOf(A_SUBSCRIBER), emailOf(A_SUBSCRIBER_ON_THE_PDC),
            emailOf(ANOTHER_SUBSCRIBER_ON_THE_PDC)));
  }

  @Test
  void theIssueIsMailedToTheSubscribersOnThePdcEvenIfTheNewsletterHasNoSubscriber() {
    theIssuesAreSentByMail(true);
    pdc.subscribe(A_SUBSCRIBER_ON_THE_PDC);

    controller.notifyInternalSubscribers(anIssue());

    assertThat(recipientsOfTheMail(), containsInAnyOrder(emailOf(A_SUBSCRIBER_ON_THE_PDC)));
  }

  @Test
  void theIssueIsNotMailedToASubscriberOnThePdcWithoutAccessToTheApplication() {
    theIssuesAreSentByMail(true);
    subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));
    pdc.subscribe(A_SUBSCRIBER_ON_THE_PDC, ANOTHER_SUBSCRIBER_ON_THE_PDC);
    when(accessControl.isUserAuthorized(ANOTHER_SUBSCRIBER_ON_THE_PDC, COMPONENT_ID)).thenReturn(
        false);

    controller.notifyInternalSubscribers(anIssue());

    assertThat(recipientsOfTheMail(),
        containsInAnyOrder(emailOf(A_SUBSCRIBER), emailOf(A_SUBSCRIBER_ON_THE_PDC)));
  }

  /**
   * The external subscribers whose email address is the one of a user that has already received
   * the issue are left out.
   */
  @Test
  void theIssueIsNotMailedTwiceToASubscriberOnThePdcThatIsAlsoAnExternalSubscriber() {
    theIssuesAreSentByMail(true);
    pdc.subscribe(A_SUBSCRIBER_ON_THE_PDC);
    externalSubscribers.add(AN_EXTERNAL_SUBSCRIBER);
    externalSubscribers.add(emailOf(A_SUBSCRIBER_ON_THE_PDC));

    controller.sendByMailToExternalSubscribers(anIssue());

    assertThat(recipientsOfTheMail(), containsInAnyOrder(AN_EXTERNAL_SUBSCRIBER));
  }

  @Test
  void theIssueIsSentByNotificationWhenTheNewsletterIsNotSentByMail(
      @TestManagedMock OrganizationController organizationController,
      @TestManagedMock ResourceSubscriptionService subscriptionService) throws Exception {
    // the notification asks itself for the subscribers of the application
    final ComponentInstLight componentInstance = new ComponentInstLight();
    componentInstance.setLocalId(5);
    componentInstance.setName("infoLetter");
    when(organizationController.getComponentInstance(COMPONENT_ID)).thenReturn(
        Optional.of(componentInstance));
    when(subscriptionService.getSubscribersOfComponentAndTypedResource(anyString(), any(), any(),
        any(SubscriberDirective[].class))).thenReturn(subscribers);
    subscriptionServices().put("infoLetter", subscriptionService);
    theIssuesAreSentByMail(false);
    subscribers.add(UserSubscriptionSubscriber.from(A_SUBSCRIBER));

    final List<UserNotificationBuilder> notifications =
        notificationsSentBy(() -> controller.notifyInternalSubscribers(anIssue()));

    assertThat(notifications, hasSize(1));
    assertThat(notifications.getFirst(),
        instanceOf(InfoLetterSubscriptionPublicationUserNotification.class));
    verify(service, never()).sendLetterByMail(any(), anyString(), anySet(), anyString(), any());
  }

  /**
   * An issue is visible, and then sent, only once validated: its subscribers on the PdC receive
   * it at that time. So its classification at its creation mustn't alert them.
   */
  @Test
  void theClassificationOfAnIssueAtItsCreationDoesNotAlertTheSubscribersOnThePdc() {
    final InfoLetterPublicationPdC issue = anIssue();
    when(issue.getPositions()).thenReturn(A_POSITION_ON_THE_PDC);

    final List<UserNotificationBuilder> notifications =
        notificationsSentBy(() -> controller.createInfoLetterPublication(issue));

    assertThat(notifications, is(empty()));
    verify(pdcClassificationService).classifyContent(any(Contribution.class),
        any(PdcClassification.class), eq(false));
    verify(pdcClassificationService, never()).classifyContent(any(), any(), eq(true));
  }

  @AfterEach
  void clearTheSubscriptionServices() throws Exception {
    subscriptionServices().clear();
  }

  @SuppressWarnings("unchecked")
  private static Map<String, ResourceSubscriptionService> subscriptionServices()
      throws IllegalAccessException {
    return (Map<String, ResourceSubscriptionService>) FieldUtils.readDeclaredStaticField(
        ResourceSubscriptionProvider.class, "componentImplementations", true);
  }

  private void theIssuesAreSentByMail(final boolean byMail) {
    when(mainController.getComponentParameterValue(COMPONENT_ID, "sendNewsletter")).thenReturn(
        byMail ? "yes" : "no");
  }

  @SuppressWarnings("unchecked")
  private Set<String> recipientsOfTheMail() {
    final ArgumentCaptor<Set<String>> emails = ArgumentCaptor.forClass(Set.class);
    verify(service).sendLetterByMail(any(), anyString(), emails.capture(), anyString(), any());
    return emails.getValue();
  }

  private static List<UserNotificationBuilder> notificationsSentBy(final Runnable treatment) {
    final ArgumentCaptor<UserNotificationBuilder> builders =
        ArgumentCaptor.forClass(UserNotificationBuilder.class);
    try (MockedStatic<UserNotificationHelper> helper = mockStatic(UserNotificationHelper.class)) {
      treatment.run();
      helper.verify(() -> UserNotificationHelper.buildAndSend(builders.capture()), atLeast(0));
    }
    return builders.getAllValues();
  }

  private static InfoLetterPublicationPdC anIssue() {
    final InfoLetterPublicationPdC issue = mock(InfoLetterPublicationPdC.class);
    when(issue.getId()).thenReturn(ISSUE_ID);
    when(issue.getInstanceId()).thenReturn(COMPONENT_ID);
    when(issue.getComponentInstanceId()).thenReturn(COMPONENT_ID);
    when(issue.getIdentifier()).thenReturn(
        ContributionIdentifier.from(COMPONENT_ID, ISSUE_ID, InfoLetterPublicationPdC.TYPE));
    when(issue.getName()).thenReturn("An issue");
    return issue;
  }

  private static UserDetail aUser(final String userId) {
    final UserDetail user = mock(UserDetail.class);
    when(user.getId()).thenReturn(userId);
    when(user.getEmailAddress()).thenReturn(emailOf(userId));
    return user;
  }

  private static String emailOf(final String userId) {
    return "user" + userId + "@silverpeas.org";
  }

  /**
   * A provider of the users concerned by a contribution in another way than by a subscription to
   * a resource of the application, like the subscribers to a position on the PdC on which the
   * contribution is classified. As the PdC does, the contribution is identified by its local
   * identifier among the contributions of its application.
   */
  static class SubscribersOnThePdcProvider implements ContributionSubscribersProvider {

    private final List<SubscriptionSubscriber> subscribers = new ArrayList<>();
    private String classifiedContentId;

    void subscribe(final String... userIds) {
      this.classifiedContentId = InfoLetterIssueSendingTest.ISSUE_ID;
      List.of(userIds).forEach(u -> subscribers.add(UserSubscriptionSubscriber.from(u)));
    }

    @Override
    public SubscriptionSubscriberList getSubscribersOf(final ContributionIdentifier contribution) {
      return contribution.getComponentInstanceId().equals(COMPONENT_ID) &&
          contribution.getLocalId().equals(classifiedContentId) ?
          new SubscriptionSubscriberList(subscribers) : new SubscriptionSubscriberList();
    }
  }
}
