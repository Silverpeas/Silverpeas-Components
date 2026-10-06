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
package org.silverpeas.components.questionreply.service;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.silverpeas.components.questionreply.QuestionReplyException;
import org.silverpeas.components.questionreply.index.QuestionIndexer;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.QuestionDetail;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.notification.user.builder.UserNotificationBuilder;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.pdc.pdc.service.PdcClassificationService;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.subscription.ResourceSubscriptionService;
import org.silverpeas.core.subscription.SubscriberDirective;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the notification of the subscribers at the creation of a FAQ: a question created
 * with its public reply. When the question is classified on the PdC at its creation, the
 * subscribers on the PdC are notified about the reply with the other subscribers and not about
 * the classification: for doing, the question has to be classified, without any alert, before the
 * subscribers are notified.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class FaqCreationSubscribersNotificationTest {

  private static final String COMPONENT_NAME = "questionReply";
  private static final String COMPONENT_ID = "questionReply7";
  private static final long QUESTION_ID = 23L;
  private static final String CLASSIFICATION = "the question is classified";
  private static final String NOTIFICATION = "the subscribers are notified";

  @TestManagedMock
  PdcClassificationService pdcClassificationService;
  @TestManagedMock
  ResourceSubscriptionService subscriptionService;

  private SilverpeasQuestionManager manager;
  private final Question question = new Question("10", COMPONENT_ID);
  private final List<String> events = new ArrayList<>();

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUpTheQuestionManager() throws Exception {
    final SilverpeasBeanDAO<Question> questionDao = mock(SilverpeasBeanDAO.class);
    when(questionDao.add(any(Connection.class), any(Question.class))).thenReturn(
        new IdPK(String.valueOf(QUESTION_ID)));
    final SilverpeasBeanDAO<Reply> replyDao = mock(SilverpeasBeanDAO.class);
    when(replyDao.add(any(Connection.class), any(Reply.class))).thenReturn(new IdPK("31"));

    // the question is got back from the data source once created: the manager is spied to
    // provide the one of the tests
    manager = spy(new SilverpeasQuestionManager());
    FieldUtils.writeField(manager, "questionDao", questionDao, true);
    FieldUtils.writeField(manager, "replyDao", replyDao, true);
    FieldUtils.writeField(manager, "questionIndexer", mock(QuestionIndexer.class), true);
    FieldUtils.writeField(manager, "contentManager", mock(QuestionReplyContentManager.class),
        true);
    FieldUtils.writeField(manager, "i18n", mock(I18n.class), true);
    question.getPK().setId(String.valueOf(QUESTION_ID));
    doReturn(question).when(manager).getQuestion(QUESTION_ID);

    doAnswer(invocation -> events.add(CLASSIFICATION)).when(pdcClassificationService)
        .classifyContent(any(Contribution.class), any(PdcClassification.class), anyBoolean());
  }

  @BeforeEach
  void setUpTheSubscriptionsOfTheApplication(
      @TestManagedMock OrganizationController organizationController) throws Exception {
    final ComponentInstLight componentInstance = new ComponentInstLight();
    componentInstance.setLocalId(7);
    componentInstance.setName(COMPONENT_NAME);
    when(organizationController.getComponentInstance(COMPONENT_ID)).thenReturn(
        Optional.of(componentInstance));
    subscriptionServices().put(COMPONENT_NAME, subscriptionService);
    when(subscriptionService.getSubscribersOfComponentAndTypedResource(anyString(), any(), any(),
        any(SubscriberDirective[].class))).thenReturn(new SubscriptionSubscriberList());
  }

  @AfterEach
  void clearTheSubscriptionsOfTheApplication() throws Exception {
    subscriptionServices().clear();
  }

  @Test
  void theSubscribersAreNotifiedAboutThePublicReplyOfANewQuestion() {
    createAFaq(aPublicReply(), List.of());

    assertThat(events, contains(NOTIFICATION));
    verify(pdcClassificationService, never()).classifyContent(any(), any(), anyBoolean());
  }

  @Test
  void aNewQuestionIsClassifiedOnThePdcBeforeTheSubscribersAreNotifiedAboutItsReply() {
    createAFaq(aPublicReply(), List.of(new PdcPosition()));

    assertThat(events, contains(CLASSIFICATION, NOTIFICATION));
  }

  /**
   * The subscribers on the PdC are notified about the reply by the notification of the
   * application: they mustn't be also alerted about the classification of the question.
   */
  @Test
  void theClassificationOfANewQuestionDoesNotAlertTheSubscribersOnThePdc() {
    createAFaq(aPublicReply(), List.of(new PdcPosition()));

    verify(pdcClassificationService).classifyContent(any(QuestionDetail.class),
        any(PdcClassification.class), eq(false));
    verify(pdcClassificationService, never()).classifyContent(any(), any(), eq(true));
  }

  @Test
  void aNewQuestionIsClassifiedEvenIfNobodyIsNotifiedAboutItsPrivateReply() {
    createAFaq(aPrivateReply(), List.of(new PdcPosition()));

    assertThat(events, contains(CLASSIFICATION));
  }

  private void createAFaq(final Reply reply, final List<PdcPosition> positions) {
    try (MockedStatic<UserNotificationHelper> helper = mockStatic(UserNotificationHelper.class);
         MockedStatic<DBUtil> dbUtil = mockStatic(DBUtil.class);
         MockedStatic<WysiwygController> ignored = mockStatic(WysiwygController.class)) {
      dbUtil.when(DBUtil::openConnection).thenReturn(mock(Connection.class));
      helper.when(() -> UserNotificationHelper.buildAndSend(any(UserNotificationBuilder.class)))
          .thenAnswer(invocation -> events.add(NOTIFICATION));
      manager.createQuestionReply(question, reply, positions);
    } catch (QuestionReplyException e) {
      throw new AssertionError(e);
    }
  }

  private static Reply aPublicReply() {
    return aReply(1);
  }

  private static Reply aPrivateReply() {
    return aReply(0);
  }

  private static Reply aReply(final int publicReply) {
    final Reply reply = mock(Reply.class);
    when(reply.getPK()).thenReturn(new IdPK());
    when(reply.getPublicReply()).thenReturn(publicReply);
    when(reply.readAuthor()).thenReturn(mock(UserDetail.class));
    return reply;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, ResourceSubscriptionService> subscriptionServices()
      throws IllegalAccessException {
    return (Map<String, ResourceSubscriptionService>) FieldUtils.readDeclaredStaticField(
        ResourceSubscriptionProvider.class, "componentImplementations", true);
  }
}
