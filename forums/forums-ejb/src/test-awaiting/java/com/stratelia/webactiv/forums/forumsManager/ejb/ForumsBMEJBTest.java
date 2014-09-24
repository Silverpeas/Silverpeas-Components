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
import org.silverpeas.util.MapUtil;
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

import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
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
    assertThat(forumsBMEJB.isSubscriber(INSTANCE_ID, USER_ID), is(false));
  }

  /**
   * Test of {@link ForumsBM#isSubscriber(String, String)}
   */
  @Test
  public void isSubscriberOfComponent() {
    setUpComponentUserSubscription(USER_ID);
    assertThat(forumsBMEJB.isSubscriber(INSTANCE_ID, USER_ID), is(true));
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
    assertThat(forumsBMEJB.isSubscriber(forumPK, USER_ID), is(false));
    assertThat(forumsBMEJB.isSubscriberByInheritance(forumPK, USER_ID), is(false));
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
    assertThat(forumsBMEJB.isSubscriber(forumPK, USER_ID), is(true));
    assertThat(forumsBMEJB.isSubscriberByInheritance(forumPK, USER_ID), is(false));
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
    assertThat(forumsBMEJB.isSubscriber(forumPK, USER_ID), is(false));
    assertThat(forumsBMEJB.isSubscriberByInheritance(forumPK, USER_ID), is(true));
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
    assertThat(forumsBMEJB.isSubscriber(forumPK, USER_ID), is(false));
    assertThat(forumsBMEJB.isSubscriberByInheritance(forumPK, USER_ID), is(true));
  }

  /**
   * Test of {@link ForumsBM#isSubscriber(com.stratelia.webactiv.forums.models.MessagePK, String)}
   * and {@link ForumsBM#isSubscriberByInheritance(com.stratelia.webactiv.forums.models.MessagePK,
   * String)}
   */
  @Test
  public void isNotSubscriberOfForumMessage() {
    MessagePK messagePK = new MessagePK(INSTANCE_ID, FORUM_RACINE_RE_RE_MESSAGE_ID);
    assertThat(forumsBMEJB.isSubscriber(messagePK, USER_ID), is(false));
    assertThat(forumsBMEJB.isSubscriberByInheritance(messagePK, USER_ID), is(false));
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
    assertThat(forumsBMEJB.isSubscriber(messagePK, USER_ID), is(true));
    assertThat(forumsBMEJB.isSubscriberByInheritance(messagePK, USER_ID), is(false));
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
    assertThat(forumsBMEJB.isSubscriber(messagePK, USER_ID), is(false));
    assertThat(forumsBMEJB.isSubscriberByInheritance(messagePK, USER_ID), is(true));
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
    assertThat(forumsBMEJB.isSubscriber(messagePK, USER_ID), is(false));
    assertThat(forumsBMEJB.isSubscriberByInheritance(messagePK, USER_ID), is(true));
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
    assertThat(forumsBMEJB.isSubscriber(messagePK, USER_ID), is(false));
    assertThat(forumsBMEJB.isSubscriberByInheritance(messagePK, USER_ID), is(true));
  }


  /**
   * Test of {@link ForumsBM#listAllSubscribers(String)}
   */
  @Test
  public void listAllSubscribersOfComponent() {
    Map<SubscriberType, Collection<String>> subscribersByType =
        forumsBMEJB.listAllSubscribers(INSTANCE_ID);
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to messages, subjects and forums
    setUpForumMessageUserSubscription("500", FORUM_RACINE_RE_RE_MESSAGE_ID);
    setUpForumMessageUserSubscription("501", FORUM_RACINE_RE_RE_MESSAGE_ID);
    setUpForumMessageUserSubscription("400", FORUM_RACINE_SUBJECT_ID);
    setUpForumMessageUserSubscription("401", FORUM_RACINE_SUBJECT_ID);
    setUpForumMessageUserSubscription("402", FORUM_RACINE_SUBJECT_ID);
    setUpForumUserSubscription("503", FORUM_RACINE_ID);
    subscribersByType = forumsBMEJB.listAllSubscribers(INSTANCE_ID);
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding component subscribers
    setUpComponentUserSubscription("600");
    setUpComponentUserSubscription("601");
    subscribersByType = forumsBMEJB.listAllSubscribers(INSTANCE_ID);
    assertThat(subscribersByType.get(SubscriberType.USER), containsInAnyOrder("600", "601"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());
  }

  /**
   * Test of {@link ForumsBM#listAllSubscribers(com.stratelia.webactiv.forums.models.ForumPK)}
   */
  @Test
  public void listAllSubscribersOfForum() {
    ForumPK forumPK = new ForumPK(INSTANCE_ID, FORUM_RACINE_ID);
    Map<SubscriberType, Collection<String>> subscribersByType =
        forumsBMEJB.listAllSubscribers(forumPK);
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to messages
    setUpForumMessageUserSubscription("500", FORUM_RACINE_RE_RE_MESSAGE_ID);
    setUpForumMessageUserSubscription("501", FORUM_RACINE_RE_RE_MESSAGE_ID);
    subscribersByType = forumsBMEJB.listAllSubscribers(forumPK);
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to subjects
    setUpForumMessageUserSubscription("400", FORUM_RACINE_SUBJECT_ID);
    setUpForumMessageUserSubscription("401", FORUM_RACINE_SUBJECT_ID);
    setUpForumMessageUserSubscription("402", FORUM_RACINE_SUBJECT_ID);
    subscribersByType = forumsBMEJB.listAllSubscribers(forumPK);
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to forums
    setUpForumUserSubscription("503", FORUM_RACINE_ID);
    subscribersByType = forumsBMEJB.listAllSubscribers(forumPK);
    assertThat(subscribersByType.get(SubscriberType.USER), containsInAnyOrder("503"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding component subscribers
    setUpComponentUserSubscription("600");
    setUpComponentUserSubscription("601");
    subscribersByType = forumsBMEJB.listAllSubscribers(forumPK);
    assertThat(subscribersByType.get(SubscriberType.USER), containsInAnyOrder("503", "600", "601"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());
  }

  /**
   * Test of {@link ForumsBM#listAllSubscribers(com.stratelia.webactiv.forums.models.ForumPK)}
   */
  @Test
  public void listAllSubscribersOfChildForum() {
    ForumPK forumPK = new ForumPK(INSTANCE_ID, FORUM_FILS_ID);
    Map<SubscriberType, Collection<String>> subscribersByType =
        forumsBMEJB.listAllSubscribers(forumPK);
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to child forums
    setUpForumUserSubscription("503", FORUM_FILS_ID);
    subscribersByType = forumsBMEJB.listAllSubscribers(forumPK);
    assertThat(subscribersByType.get(SubscriberType.USER), containsInAnyOrder("503"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to parent forums
    setUpForumUserSubscription("704", FORUM_PERE_ID);
    setUpForumUserSubscription("705", FORUM_PERE_ID);
    subscribersByType = forumsBMEJB.listAllSubscribers(forumPK);
    assertThat(subscribersByType.get(SubscriberType.USER), containsInAnyOrder("704", "705", "503"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding component subscribers
    setUpComponentUserSubscription("600");
    setUpComponentUserSubscription("601");
    subscribersByType = forumsBMEJB.listAllSubscribers(forumPK);
    assertThat(subscribersByType.get(SubscriberType.USER),
        containsInAnyOrder("704", "705", "503", "600", "601"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());
  }

  /**
   * Test of {@link ForumsBM#listAllSubscribers(com.stratelia.webactiv.forums.models.MessagePK)}
   */
  @Test
  public void listAllSubscribersOfSubject() {
    MessagePK messagePK = new MessagePK(INSTANCE_ID, FORUM_RACINE_SUBJECT_ID);
    Map<SubscriberType, Collection<String>> subscribersByType =
        forumsBMEJB.listAllSubscribers(messagePK);
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to messages
    setUpForumMessageUserSubscription("500", FORUM_RACINE_RE_RE_MESSAGE_ID);
    setUpForumMessageUserSubscription("501", FORUM_RACINE_RE_RE_MESSAGE_ID);
    subscribersByType = forumsBMEJB.listAllSubscribers(messagePK);
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to subjects
    setUpForumMessageUserSubscription("400", FORUM_RACINE_SUBJECT_ID);
    setUpForumMessageUserSubscription("401", FORUM_RACINE_SUBJECT_ID);
    setUpForumMessageUserSubscription("402", FORUM_RACINE_SUBJECT_ID);
    subscribersByType = forumsBMEJB.listAllSubscribers(messagePK);
    assertThat(subscribersByType.get(SubscriberType.USER), containsInAnyOrder("400", "401", "402"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to forums
    setUpForumUserSubscription("503", FORUM_RACINE_ID);
    subscribersByType = forumsBMEJB.listAllSubscribers(messagePK);
    assertThat(subscribersByType.get(SubscriberType.USER),
        containsInAnyOrder("400", "401", "402", "503"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding component subscribers
    setUpComponentUserSubscription("600");
    setUpComponentUserSubscription("601");
    subscribersByType = forumsBMEJB.listAllSubscribers(messagePK);
    assertThat(subscribersByType.get(SubscriberType.USER),
        containsInAnyOrder("400", "401", "402", "503", "600", "601"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());
  }

  /**
   * Test of {@link ForumsBM#listAllSubscribers(com.stratelia.webactiv.forums.models.MessagePK)}
   */
  @Test
  public void listAllSubscribersOfMessage() {
    MessagePK messagePK = new MessagePK(INSTANCE_ID, FORUM_RACINE_RE_RE_MESSAGE_ID);
    Map<SubscriberType, Collection<String>> subscribersByType =
        forumsBMEJB.listAllSubscribers(messagePK);
    assertThat(subscribersByType.get(SubscriberType.USER), empty());
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to messages
    setUpForumMessageUserSubscription("500", FORUM_RACINE_RE_RE_MESSAGE_ID);
    setUpForumMessageUserSubscription("501", FORUM_RACINE_RE_RE_MESSAGE_ID);
    subscribersByType = forumsBMEJB.listAllSubscribers(messagePK);
    assertThat(subscribersByType.get(SubscriberType.USER), containsInAnyOrder("500", "501"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to subjects
    setUpForumMessageUserSubscription("400", FORUM_RACINE_SUBJECT_ID);
    setUpForumMessageUserSubscription("401", FORUM_RACINE_SUBJECT_ID);
    setUpForumMessageUserSubscription("402", FORUM_RACINE_SUBJECT_ID);
    subscribersByType = forumsBMEJB.listAllSubscribers(messagePK);
    assertThat(subscribersByType.get(SubscriberType.USER),
        containsInAnyOrder("500", "501", "400", "401", "402"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding subscribers to forums
    setUpForumUserSubscription("503", FORUM_RACINE_ID);
    subscribersByType = forumsBMEJB.listAllSubscribers(messagePK);
    assertThat(subscribersByType.get(SubscriberType.USER),
        containsInAnyOrder("500", "501", "400", "401", "402", "503"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());

    // Adding component subscribers
    setUpComponentUserSubscription("600");
    setUpComponentUserSubscription("601");
    subscribersByType = forumsBMEJB.listAllSubscribers(messagePK);
    assertThat(subscribersByType.get(SubscriberType.USER),
        containsInAnyOrder("500", "501", "400", "401", "402", "503", "600", "601"));
    assertThat(subscribersByType.get(SubscriberType.GROUP), empty());
  }

  /*
  ################################################################################################
  TECHNICAL PART
  ################################################################################################
   */

  private IDatabaseConnection dbConnection;
  private ForumsEJBEJBForTest forumsBMEJB;
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
    dbConnection = baseTest.getDatabaseTester().getConnection();
    Connection connection = dbConnection.getConnection();
    DBUtil.getInstanceForTest(connection);
    forumsBMEJB = new ForumsEJBEJBForTest();
    subscriptionServiceMock = mock(SubscriptionService.class);
    componentSubscribers.clear();
    forumSubscribers.clear();
    forumMessageSubscribers.clear();
    when(subscriptionServiceMock.getSubscribers(any(SubscriptionResource.class)))
        .thenAnswer(new Answer<Collection<SubscriptionSubscriber>>() {

          @Override
          public Collection<SubscriptionSubscriber> answer(final InvocationOnMock invocation)
              throws Throwable {
            if (invocation.getArguments()[0] instanceof ComponentSubscriptionResource) {
              return componentSubscribers
                  .get(((SubscriptionResource) invocation.getArguments()[0]).getInstanceId());
            } else if (invocation.getArguments()[0] instanceof ForumSubscriptionResource) {
              return forumSubscribers
                  .get(((SubscriptionResource) invocation.getArguments()[0]).getId());
            } else if (invocation.getArguments()[0] instanceof ForumMessageSubscriptionResource) {
              return forumMessageSubscribers
                  .get(((SubscriptionResource) invocation.getArguments()[0]).getId());
            }
            return null;
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

  /**
   * An ForumsBMEJB instance dedicated to tests. It overrides some methods in order to set a
   * context adapted to the tests.
   */
  private class ForumsEJBEJBForTest extends ForumsBMEJB {

    @Override
    protected String getWysiwygContent(final String componentId, final String messageId) {
      return "componentId=" + componentId + ",messageId=" + messageId;
    }

    @Override
    protected SubscriptionService getSubscribeBm() {
      return subscriptionServiceMock;
    }
  }
}
