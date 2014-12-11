/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.forums.forumsManager.ejb;

import com.silverpeas.components.model.AbstractJndiCase;
import com.silverpeas.components.model.SilverpeasJndiCase;
import com.silverpeas.jndi.SimpleMemoryContextFactory;
import com.silverpeas.subscribe.SubscriptionResource;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.service.ComponentSubscriptionResource;
import com.silverpeas.subscribe.service.UserSubscriptionSubscriber;
import com.silverpeas.subscribe.util.SubscriptionSubscriberList;
import com.silverpeas.subscribe.util.SubscriptionSubscriberMapBySubscriberType;
import com.silverpeas.util.MapUtil;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.forums.forumsManager.ejb.mock.OrganizationControllerMock;
import com.stratelia.webactiv.forums.forumsManager.ejb.mock.SubscriptionServiceMock;
import com.stratelia.webactiv.forums.models.ForumPK;
import com.stratelia.webactiv.forums.models.MessagePK;
import com.stratelia.webactiv.util.DBUtil;
import org.dbunit.database.IDatabaseConnection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.components.forum.subscription.ForumMessageSubscriptionResource;
import org.silverpeas.components.forum.subscription.ForumSubscriptionResource;
import org.silverpeas.core.admin.OrganisationController;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * User: Yohann Chastagnier
 * Date: 20/07/13
 */
public class ForumsBMEJBTest extends AbstractJndiCase {

  private static final String INSTANCE_ID = "forums122";
  private static final String USER_ID = "26";
  private static final String FORUM_RACINE_ID = "1";
  private static final String FORUM_RACINE_SUBJECT_ID = "10";
  private static final String FORUM_RACINE_RE_RE_MESSAGE_ID = "12";
  private static final String FORUM_PERE_ID = "2";
  private static final String FORUM_FILS_ID = "3";

  /**
   * Test of {@link ForumsBM#isSubscriber(String, String)}
   */
  @Test
  public void isNotSubscriberOfComponent() {
    assertThat(getTestedForumsService().isSubscriber(INSTANCE_ID, USER_ID), is(false));
  }

  /**
   * Test of {@link ForumsBM#isSubscriber(String, String)}
   */
  @Test
  public void isSubscriberOfComponent() {
    setUpComponentUserSubscription(USER_ID);
    assertThat(getTestedForumsService().isSubscriber(INSTANCE_ID, USER_ID), is(true));
  }

  /**
   * Test of {@link ForumsBM#isSubscriber(com.stratelia.webactiv.forums.models.ForumPK, String)}
   * and
   * {@link ForumsBM#isSubscriberByInheritance(com.stratelia.webactiv.forums.models.ForumPK,
   * String)}
   */
  @Test
  public void isNotSubscriberOfForum() {
    ForumPK forumPK = new ForumPK(INSTANCE_ID, FORUM_FILS_ID);
    assertThat(getTestedForumsService().isSubscriber(forumPK, USER_ID), is(false));
    assertThat(getTestedForumsService().isSubscriberByInheritance(forumPK, USER_ID), is(false));
  }

  /**
   * Test of {@link ForumsBM#isSubscriber(com.stratelia.webactiv.forums.models.ForumPK, String)}
   * and
   * {@link ForumsBM#isSubscriberByInheritance(com.stratelia.webactiv.forums.models.ForumPK,
   * String)}
   */
  @Test
  public void isSubscriberOfForum() {
    setUpForumUserSubscription(USER_ID, FORUM_FILS_ID);
    ForumPK forumPK = new ForumPK(INSTANCE_ID, FORUM_FILS_ID);
    assertThat(getTestedForumsService().isSubscriber(forumPK, USER_ID), is(true));
    assertThat(getTestedForumsService().isSubscriberByInheritance(forumPK, USER_ID), is(false));
  }

  /**
   * Test of {@link ForumsBM#isSubscriber(com.stratelia.webactiv.forums.models.ForumPK, String)}
   * and
   * {@link ForumsBM#isSubscriberByInheritance(com.stratelia.webactiv.forums.models.ForumPK,
   * String)}
   */
  @Test
  public void isSubscriberOfForumByComponentInheritance() {
    setUpComponentUserSubscription(USER_ID);
    ForumPK forumPK = new ForumPK(INSTANCE_ID, FORUM_FILS_ID);
    assertThat(getTestedForumsService().isSubscriber(forumPK, USER_ID), is(false));
    assertThat(getTestedForumsService().isSubscriberByInheritance(forumPK, USER_ID), is(true));
  }

  /**
   * Test of {@link ForumsBM#isSubscriber(com.stratelia.webactiv.forums.models.ForumPK, String)}
   * and
   * {@link ForumsBM#isSubscriberByInheritance(com.stratelia.webactiv.forums.models.ForumPK,
   * String)}
   */
  @Test
  public void isSubscriberOfForumByParentForumInheritance() {
    setUpForumUserSubscription(USER_ID, FORUM_PERE_ID);
    ForumPK forumPK = new ForumPK(INSTANCE_ID, FORUM_FILS_ID);
    assertThat(getTestedForumsService().isSubscriber(forumPK, USER_ID), is(false));
    assertThat(getTestedForumsService().isSubscriberByInheritance(forumPK, USER_ID), is(true));
  }

  /**
   * Test of {@link ForumsBM#isSubscriber(com.stratelia.webactiv.forums.models.MessagePK, String)}
   * and {@link ForumsBM#isSubscriberByInheritance(com.stratelia.webactiv.forums.models.MessagePK,
   * String)}
   */
  @Test
  public void isNotSubscriberOfForumMessage() {
    MessagePK messagePK = new MessagePK(INSTANCE_ID, FORUM_RACINE_RE_RE_MESSAGE_ID);
    assertThat(getTestedForumsService().isSubscriber(messagePK, USER_ID), is(false));
    assertThat(getTestedForumsService().isSubscriberByInheritance(messagePK, USER_ID), is(false));
  }

  /**
   * Test of {@link ForumsBM#isSubscriber(com.stratelia.webactiv.forums.models.MessagePK, String)}
   * and {@link ForumsBM#isSubscriberByInheritance(com.stratelia.webactiv.forums.models.MessagePK,
   * String)}
   */
  @Test
  public void isSubscriberOfForumMessage() {
    setUpForumMessageUserSubscription(USER_ID, FORUM_RACINE_RE_RE_MESSAGE_ID);
    MessagePK messagePK = new MessagePK(INSTANCE_ID, FORUM_RACINE_RE_RE_MESSAGE_ID);
    assertThat(getTestedForumsService().isSubscriber(messagePK, USER_ID), is(true));
    assertThat(getTestedForumsService().isSubscriberByInheritance(messagePK, USER_ID), is(false));
  }

  /**
   * Test of {@link ForumsBM#isSubscriber(com.stratelia.webactiv.forums.models.MessagePK, String)}
   * and {@link ForumsBM#isSubscriberByInheritance(com.stratelia.webactiv.forums.models.MessagePK,
   * String)}
   */
  @Test
  public void isSubscriberOfForumMessageByComponentInheritance() {
    setUpComponentUserSubscription(USER_ID);
    MessagePK messagePK = new MessagePK(INSTANCE_ID, FORUM_RACINE_RE_RE_MESSAGE_ID);
    assertThat(getTestedForumsService().isSubscriber(messagePK, USER_ID), is(false));
    assertThat(getTestedForumsService().isSubscriberByInheritance(messagePK, USER_ID), is(true));
  }

  /**
   * Test of {@link ForumsBM#isSubscriber(com.stratelia.webactiv.forums.models.MessagePK, String)}
   * and {@link ForumsBM#isSubscriberByInheritance(com.stratelia.webactiv.forums.models.MessagePK,
   * String)}
   */
  @Test
  public void isSubscriberOfForumMessageByParentForumInheritance() {
    setUpForumUserSubscription(USER_ID, FORUM_RACINE_ID);
    MessagePK messagePK = new MessagePK(INSTANCE_ID, FORUM_RACINE_RE_RE_MESSAGE_ID);
    assertThat(getTestedForumsService().isSubscriber(messagePK, USER_ID), is(false));
    assertThat(getTestedForumsService().isSubscriberByInheritance(messagePK, USER_ID), is(true));
  }

  /**
   * Test of {@link ForumsBM#isSubscriber(com.stratelia.webactiv.forums.models.MessagePK, String)}
   * and {@link ForumsBM#isSubscriberByInheritance(com.stratelia.webactiv.forums.models.MessagePK,
   * String)}
   */
  @Test
  public void isSubscriberOfForumMessageByParentForumMessageInheritance() {
    setUpForumMessageUserSubscription(USER_ID, FORUM_RACINE_SUBJECT_ID);
    MessagePK messagePK = new MessagePK(INSTANCE_ID, FORUM_RACINE_RE_RE_MESSAGE_ID);
    assertThat(getTestedForumsService().isSubscriber(messagePK, USER_ID), is(false));
    assertThat(getTestedForumsService().isSubscriberByInheritance(messagePK, USER_ID), is(true));
  }


  /**
   * Test of {@link ForumsBM#listAllSubscribers(String)}
   */
  @Test
  public void listAllSubscribersOfComponent() {
    SubscriptionSubscriberMapBySubscriberType subscribersByType =
        getTestedForumsService().listAllSubscribers(INSTANCE_ID).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to messages, subjects and forums
    setUpForumMessageUserSubscription("500", FORUM_RACINE_RE_RE_MESSAGE_ID);
    setUpForumMessageUserSubscription("501", FORUM_RACINE_RE_RE_MESSAGE_ID);
    setUpForumMessageUserSubscription("400", FORUM_RACINE_SUBJECT_ID);
    setUpForumMessageUserSubscription("401", FORUM_RACINE_SUBJECT_ID);
    setUpForumMessageUserSubscription("402", FORUM_RACINE_SUBJECT_ID);
    setUpForumUserSubscription("503", FORUM_RACINE_ID);
    subscribersByType = getTestedForumsService().listAllSubscribers(INSTANCE_ID).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding component subscribers
    setUpComponentUserSubscription("600");
    setUpComponentUserSubscription("601");
    subscribersByType = getTestedForumsService().listAllSubscribers(INSTANCE_ID).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER).getAllIds(),
        containsInAnyOrder("600", "601"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());
  }

  /**
   * Test of {@link ForumsBM#listAllSubscribers(com.stratelia.webactiv.forums.models.ForumPK)}
   */
  @Test
  public void listAllSubscribersOfForum() {
    ForumPK forumPK = new ForumPK(INSTANCE_ID, FORUM_RACINE_ID);
    SubscriptionSubscriberMapBySubscriberType subscribersByType =
        getTestedForumsService().listAllSubscribers(forumPK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to messages
    setUpForumMessageUserSubscription("500", FORUM_RACINE_RE_RE_MESSAGE_ID);
    setUpForumMessageUserSubscription("501", FORUM_RACINE_RE_RE_MESSAGE_ID);
    subscribersByType = getTestedForumsService().listAllSubscribers(forumPK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to subjects
    setUpForumMessageUserSubscription("400", FORUM_RACINE_SUBJECT_ID);
    setUpForumMessageUserSubscription("401", FORUM_RACINE_SUBJECT_ID);
    setUpForumMessageUserSubscription("402", FORUM_RACINE_SUBJECT_ID);
    subscribersByType = getTestedForumsService().listAllSubscribers(forumPK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to forums
    setUpForumUserSubscription("503", FORUM_RACINE_ID);
    subscribersByType = getTestedForumsService().listAllSubscribers(forumPK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER).getAllIds(), containsInAnyOrder("503"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding component subscribers
    setUpComponentUserSubscription("600");
    setUpComponentUserSubscription("601");
    subscribersByType = getTestedForumsService().listAllSubscribers(forumPK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER).getAllIds(),
        containsInAnyOrder("503", "600", "601"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());
  }

  /**
   * Test of {@link ForumsBM#listAllSubscribers(com.stratelia.webactiv.forums.models.ForumPK)}
   */
  @Test
  public void listAllSubscribersOfChildForum() {
    ForumPK forumPK = new ForumPK(INSTANCE_ID, FORUM_FILS_ID);
    SubscriptionSubscriberMapBySubscriberType subscribersByType =
        getTestedForumsService().listAllSubscribers(forumPK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to child forums
    setUpForumUserSubscription("503", FORUM_FILS_ID);
    subscribersByType = getTestedForumsService().listAllSubscribers(forumPK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER).getAllIds(), containsInAnyOrder("503"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to parent forums
    setUpForumUserSubscription("704", FORUM_PERE_ID);
    setUpForumUserSubscription("705", FORUM_PERE_ID);
    subscribersByType = getTestedForumsService().listAllSubscribers(forumPK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER).getAllIds(),
        containsInAnyOrder("704", "705", "503"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding component subscribers
    setUpComponentUserSubscription("600");
    setUpComponentUserSubscription("601");
    subscribersByType = getTestedForumsService().listAllSubscribers(forumPK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER).getAllIds(),
        containsInAnyOrder("704", "705", "503", "600", "601"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());
  }

  /**
   * Test of {@link ForumsBM#listAllSubscribers(com.stratelia.webactiv.forums.models.MessagePK)}
   */
  @Test
  public void listAllSubscribersOfSubject() {
    MessagePK messagePK = new MessagePK(INSTANCE_ID, FORUM_RACINE_SUBJECT_ID);
    SubscriptionSubscriberMapBySubscriberType subscribersByType =
        getTestedForumsService().listAllSubscribers(messagePK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to messages
    setUpForumMessageUserSubscription("500", FORUM_RACINE_RE_RE_MESSAGE_ID);
    setUpForumMessageUserSubscription("501", FORUM_RACINE_RE_RE_MESSAGE_ID);
    subscribersByType = getTestedForumsService().listAllSubscribers(messagePK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to subjects
    setUpForumMessageUserSubscription("400", FORUM_RACINE_SUBJECT_ID);
    setUpForumMessageUserSubscription("401", FORUM_RACINE_SUBJECT_ID);
    setUpForumMessageUserSubscription("402", FORUM_RACINE_SUBJECT_ID);
    subscribersByType = getTestedForumsService().listAllSubscribers(messagePK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER).getAllIds(),
        containsInAnyOrder("400", "401", "402"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to forums
    setUpForumUserSubscription("503", FORUM_RACINE_ID);
    subscribersByType = getTestedForumsService().listAllSubscribers(messagePK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER).getAllIds(),
        containsInAnyOrder("400", "401", "402", "503"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding component subscribers
    setUpComponentUserSubscription("600");
    setUpComponentUserSubscription("601");
    subscribersByType = getTestedForumsService().listAllSubscribers(messagePK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER).getAllIds(),
        containsInAnyOrder("400", "401", "402", "503", "600", "601"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());
  }

  /**
   * Test of {@link ForumsBM#listAllSubscribers(com.stratelia.webactiv.forums.models.MessagePK)}
   */
  @Test
  public void listAllSubscribersOfMessage() {
    MessagePK messagePK = new MessagePK(INSTANCE_ID, FORUM_RACINE_RE_RE_MESSAGE_ID);
    SubscriptionSubscriberMapBySubscriberType subscribersByType =
        getTestedForumsService().listAllSubscribers(messagePK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to messages
    setUpForumMessageUserSubscription("500", FORUM_RACINE_RE_RE_MESSAGE_ID);
    setUpForumMessageUserSubscription("501", FORUM_RACINE_RE_RE_MESSAGE_ID);
    subscribersByType = getTestedForumsService().listAllSubscribers(messagePK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER).getAllIds(),
        containsInAnyOrder("500", "501"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to subjects
    setUpForumMessageUserSubscription("400", FORUM_RACINE_SUBJECT_ID);
    setUpForumMessageUserSubscription("401", FORUM_RACINE_SUBJECT_ID);
    setUpForumMessageUserSubscription("402", FORUM_RACINE_SUBJECT_ID);
    subscribersByType = getTestedForumsService().listAllSubscribers(messagePK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER).getAllIds(),
        containsInAnyOrder("500", "501", "400", "401", "402"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to forums
    setUpForumUserSubscription("503", FORUM_RACINE_ID);
    subscribersByType = getTestedForumsService().listAllSubscribers(messagePK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER).getAllIds(),
        containsInAnyOrder("500", "501", "400", "401", "402", "503"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding component subscribers
    setUpComponentUserSubscription("600");
    setUpComponentUserSubscription("601");
    subscribersByType = getTestedForumsService().listAllSubscribers(messagePK).indexBySubscriberType();
    assertThat(subscribersByType.get(SubscriberType.USER).getAllIds(),
        containsInAnyOrder("500", "501", "400", "401", "402", "503", "600", "601"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());
  }

  /*
  ################################################################################################
  TECHNICAL PART
  ################################################################################################
   */

  // Spring context
  private ClassPathXmlApplicationContext context;
  private IDatabaseConnection dbConnection;
  private SubscriptionService subscriptionServiceMock;

  private final Map<String, List<SubscriptionSubscriber>> componentSubscribers =
      new HashMap<String, List<SubscriptionSubscriber>>();
  private final Map<String, List<SubscriptionSubscriber>> forumSubscribers =
      new HashMap<String, List<SubscriptionSubscriber>>();
  private final Map<String, List<SubscriptionSubscriber>> forumMessageSubscribers =
      new HashMap<String, List<SubscriptionSubscriber>>();

  @BeforeClass
  public static void generalSetUp() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    baseTest = new SilverpeasJndiCase("com/silverpeas/forums/dao/forums-dataset.xml",
        "create-database.ddl");
    baseTest.configureJNDIDatasource();
    IDatabaseConnection databaseConnection = baseTest.getDatabaseTester().getConnection();
    executeDDL(databaseConnection, baseTest.getDdlFile());
    baseTest.getDatabaseTester().closeConnection(databaseConnection);
  }

  @AfterClass
  public static void generalTearDown() throws Exception {
    baseTest.shudown();
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }

  @Before
  public void bootstrapDatabase() throws Exception {
    context = new ClassPathXmlApplicationContext("spring-subscription.xml");
    dbConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dbConnection.getConnection();
    DBUtil.getInstanceForTest(connection);
    subscriptionServiceMock = context.getBean(SubscriptionServiceMock.class).getMock();
    componentSubscribers.clear();
    forumSubscribers.clear();
    forumMessageSubscribers.clear();
    when(subscriptionServiceMock.getSubscribers(any(SubscriptionResource.class)))
        .thenAnswer(new Answer<Collection<SubscriptionSubscriber>>() {

          @Override
          public Collection<SubscriptionSubscriber> answer(final InvocationOnMock invocation)
              throws Throwable {
            Collection<SubscriptionSubscriber> subscribers = null;
            if (invocation.getArguments()[0] instanceof ComponentSubscriptionResource) {
              subscribers = componentSubscribers
                  .get(((SubscriptionResource) invocation.getArguments()[0]).getInstanceId());
            } else if (invocation.getArguments()[0] instanceof ForumSubscriptionResource) {
              subscribers = forumSubscribers
                  .get(((SubscriptionResource) invocation.getArguments()[0]).getId());
            } else if (invocation.getArguments()[0] instanceof ForumMessageSubscriptionResource) {
              subscribers = forumMessageSubscribers
                  .get(((SubscriptionResource) invocation.getArguments()[0]).getId());
            }
            if (subscribers == null) {
              subscribers = new HashSet<SubscriptionSubscriber>();
            }
            return new SubscriptionSubscriberList(subscribers);
          }
        });

    OrganisationController mockedOrganisationController =
        (OrganizationControllerMock) context.getBean("organizationController");
    when(mockedOrganisationController.getComponentInstLight(anyString()))
        .thenAnswer(new Answer<ComponentInstLight>() {
          @Override
          public ComponentInstLight answer(final InvocationOnMock invocation) throws Throwable {
            String componentId = (String) invocation.getArguments()[0];
            ComponentInstLight componentInstLight = new ComponentInstLight();
            componentInstLight.setId(componentId);
            componentInstLight.setName(componentId.replaceAll("[0-9]", ""));
            return componentInstLight;
          }
        });
  }

  @After
  public void shutdownDatabase() throws Exception {
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }

  private void setUpComponentUserSubscription(final String userId) {
    setUserSubscription(userId, ComponentSubscriptionResource.from(INSTANCE_ID));
    MapUtil.putAddList(componentSubscribers, INSTANCE_ID, UserSubscriptionSubscriber.from(userId));
  }

  private void setUpForumUserSubscription(final String userId, String forumId) {
    setUserSubscription(userId, ForumSubscriptionResource.from(new ForumPK(INSTANCE_ID, forumId)));
    MapUtil.putAddList(forumSubscribers, forumId, UserSubscriptionSubscriber.from(userId));
  }

  private void setUpForumMessageUserSubscription(final String userId, String messageId) {
    setUserSubscription(userId,
        ForumMessageSubscriptionResource.from(new MessagePK(INSTANCE_ID, messageId)));
    MapUtil.putAddList(forumMessageSubscribers, messageId, UserSubscriptionSubscriber.from(userId));
  }

  private void setUserSubscription(final String userId, SubscriptionResource subscriptionResource) {
    when(subscriptionServiceMock.isUserSubscribedToResource(userId, subscriptionResource))
        .thenReturn(true);
  }

  private ForumsBM getTestedForumsService() {
    return ForumsServiceProvider.getForumsService();
  }

  /**
   * An getTestedForumsService() instance dedicated to tests. It overrides some methods in order to set a
   * context adapted to the tests.
   */
  public static class ForumsBMEJBForTest extends ForumsBMEJB {

    @Override
    protected String getWysiwygContent(final String componentId, final String messageId) {
      return "componentId=" + componentId + ",messageId=" + messageId;
    }
  }
}
