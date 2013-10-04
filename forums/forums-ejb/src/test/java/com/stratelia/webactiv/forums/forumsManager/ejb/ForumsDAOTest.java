/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.forums.forumsManager.ejb;

import com.silverpeas.components.model.AbstractJndiCase;
import com.silverpeas.components.model.SilverpeasJndiCase;
import com.silverpeas.jcrutil.RandomGenerator;
import com.silverpeas.util.CollectionUtil;
import com.stratelia.webactiv.forums.models.Forum;
import com.stratelia.webactiv.forums.models.ForumDetail;
import com.stratelia.webactiv.forums.models.ForumPK;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import org.dbunit.database.IDatabaseConnection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
public class ForumsDAOTest extends AbstractJndiCase {
  private IDatabaseConnection dbConnection;
  private Connection con;

  public ForumsDAOTest() {
  }

  @BeforeClass
  public static void generalSetUp() throws IOException, NamingException, Exception {
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
  }

  @Before
  public void bootstrapDatabase() throws Exception {
    dbConnection = baseTest.getDatabaseTester().getConnection();
    con = dbConnection.getConnection();
    DBUtil.getInstanceForTest(con);
  }

  @After
  public void shutdownDatabase() throws Exception {
    baseTest.getDatabaseTester().closeConnection(dbConnection);
  }

  /**
   * Test of selectByForumPKs method, of class ForumsDAO.
   */
  @Test
  public void testSelectByForumPKs() throws Exception {
    Collection<ForumPK> forumPKs = CollectionUtil.asList(new ForumPK("forums130", "8"));
    Collection<ForumDetail> result = ForumsDAO.selectByForumPKs(con, forumPKs);
    assertThat(result, hasSize(1));
    ForumDetail expectedForum = new ForumDetail(new ForumPK("forums130", "8"),
        "Utilisation de Silverpeas", "Les applications de Silverpeas sont riches et paramétrables. "
        + "Ceci vous permet d'en faire une utilisation multiple et parfois détournée de leur "
        + "vocation initial. Partagez vos expériences à ce sujet...", "1",
        DateUtil.parse("2004/03/26"));
    assertThat(result, containsInAnyOrder(expectedForum));
  }

  /**
   * Test of getForumsByKeys method, of class ForumsDAO.
   */
  @Test
  public void testGetForumsByKeys() throws Exception {
    Collection<ForumPK> forumPKs = CollectionUtil.asList(new ForumPK("forums130", "8"));
    Collection<Forum> result = ForumsDAO.getForumsByKeys(con, forumPKs);
    assertThat(result, hasSize(1));
    Forum expectedForum = new Forum(8, "Utilisation de Silverpeas",
        "Les applications de Silverpeas sont riches et paramétrables. "
        + "Ceci vous permet d'en faire une utilisation multiple et parfois détournée de leur "
        + "vocation initial. Partagez vos expériences à ce sujet...", true, 0, "2",
        "2004/03/26", "forums130");
    expectedForum.setPk(new ForumPK("forums130", "8"));
    assertThat(result, containsInAnyOrder(expectedForum));
  }

  /**
   * Test of getForumsList method, of class ForumsDAO.
   */
  @Test
  public void testGetForumsList() throws Exception {
    List<Forum> result = ForumsDAO.getForumsList(con, new ForumPK("forums122"));
    assertThat(result, hasSize(3));
    Forum expectedForum1 = new Forum(1, "Forum racine", "Forum racine frère du forum père", true, 0,
        null, "2004/03/26", "forums122");
    Forum expectedForum2 = new Forum(2, "Forum Père", "Forum père du forum fils", true, 0, null,
        "2004/03/26", "forums122");
    Forum expectedForum3 = new Forum(3, "Forum Fils", "Forum fils du forum  père", true, 2, "2",
        "2004/03/26", "forums122");
    assertThat(result, contains(expectedForum1, expectedForum2, expectedForum3));
  }

  /**
   * Test of getForumsIds method, of class ForumsDAO.
   */
  @Test
  public void testGetForumsIds() throws Exception {
    List<String> result = ForumsDAO.getForumsIds(con, new ForumPK("forums122"));
    assertThat(result, hasSize(3));
    assertThat(result, contains("1", "2", "3"));
  }

  /**
   * Test of getForumsListByCategory method, of class ForumsDAO.
   */
  @Test
  public void testGetForumsListByCategory() throws Exception {
    List<Forum> result = ForumsDAO.getForumsListByCategory(con, new ForumPK("forums122"), "2");
    assertThat(result, hasSize(1));
    Forum expectedForum = new Forum(3, "Forum Fils", "Forum fils du forum  père", true, 2, "2",
        "2004/03/26", "forums122");
    assertThat(result, contains(expectedForum));
  }

  /**
   * Test of getForumSonsIds method, of class ForumsDAO.
   */
  @Test
  public void testGetForumSonsIds() throws Exception {
    List<String> result = ForumsDAO.getForumSonsIds(con, new ForumPK("forums122", "1"));
    assertThat(result, hasSize(0));
    result = ForumsDAO.getForumSonsIds(con, new ForumPK("forums122", "2"));
    assertThat(result, hasSize(1));
    assertThat(result, contains("3"));
  }

  /**
   * Test of getForum method, of class ForumsDAO.
   */
  @Test
  public void testGetForum() throws Exception {
    Forum result = ForumsDAO.getForum(con, new ForumPK("forums130", "8"));
    Forum expectedForum = new Forum(8, "Utilisation de Silverpeas",
        "Les applications de Silverpeas sont riches et paramétrables. "
        + "Ceci vous permet d'en faire une utilisation multiple et parfois détournée de leur "
        + "vocation initial. Partagez vos expériences à ce sujet...", true, 0, "2",
        "2004/03/26", "forums130");
    expectedForum.setPk(new ForumPK("forums130", "8"));
    assertThat(result, equalTo(expectedForum));
  }

  /**
   * Test of getForumName method, of class ForumsDAO.
   */
  @Test
  public void testGetForumName() throws Exception {
    String result = ForumsDAO.getForumName(con, 8);
    String expectedName = "Utilisation de Silverpeas";
    assertThat(result, equalTo(expectedName));
  }

  /**
   * Test of isForumActive method, of class ForumsDAO.
   */
  @Test
  public void testIsForumActive() throws Exception {
    boolean result = ForumsDAO.isForumActive(con, 8);
    assertThat(result, equalTo(true));
    result = ForumsDAO.isForumActive(con, 5);
    assertThat(result, equalTo(false));

  }

  /**
   * Test of getForumParentId method, of class ForumsDAO.
   */
  @Test
  public void testGetForumParentId() throws Exception {
    int result = ForumsDAO.getForumParentId(con, 8);
    assertThat(result, equalTo(0));
    result = ForumsDAO.getForumParentId(con, 3);
    assertThat(result, equalTo(2));
  }

  /**
   * Test of getForumInstanceId method, of class ForumsDAO.
   */
  @Test
  public void testGetForumInstanceId() throws Exception {
    String result = ForumsDAO.getForumInstanceId(con, 8);
    assertThat(result, equalTo("forums130"));
    result = ForumsDAO.getForumInstanceId(con, 3);
    assertThat(result, equalTo("forums122"));
  }

  /**
   * Test of getForumCreatorId method, of class ForumsDAO.
   */
  @Test
  public void testGetForumCreatorId() throws Exception {
    String result = ForumsDAO.getForumCreatorId(con, 5);
    assertThat(result, equalTo("10"));
    result = ForumsDAO.getForumCreatorId(con, 3);
    assertThat(result, equalTo("1"));
  }

  /**
   * Test of lockForum method, of class ForumsDAO.
   */
  @Test
  public void testLockForum() throws Exception {
    ForumsDAO.lockForum(con, new ForumPK("forums130", "8"), 1);
    Forum result = ForumsDAO.getForum(con, new ForumPK("forums130", "8"));
    Forum expectedForum = new Forum(8, "Utilisation de Silverpeas",
        "Les applications de Silverpeas sont riches et paramétrables. "
        + "Ceci vous permet d'en faire une utilisation multiple et parfois détournée de leur "
        + "vocation initial. Partagez vos expériences à ce sujet...", false, 0, "2",
        "2004/03/26", "forums130");
    assertThat(result, equalTo(expectedForum));
    ForumsDAO.lockForum(con, new ForumPK("forums100", "6"), 1);
    result = ForumsDAO.getForum(con, new ForumPK("forums100", "6"));
    expectedForum = new Forum(6, "Forum Modéré", "forum fermé par modérateur", false, 0, null,
        "2004/03/26", "forums100");
    assertThat(result, equalTo(expectedForum));
  }

  /**
   * Test of unlockForum method, of class ForumsDAO.
   */
  @Test
  public void testUnlockForum() throws Exception {
    int result = ForumsDAO.unlockForum(con, new ForumPK("forums100", "5"), 2);
    assertThat(result, equalTo(0));
    Forum forum = ForumsDAO.getForum(con, new ForumPK("forums100", "5"));
    Forum expectedForum = new Forum(5, "Forum Fermé", "forum fermé", false, 0, null,
        "2004/03/26", "forums100");
    assertThat(forum, equalTo(expectedForum));
    result = ForumsDAO.unlockForum(con, new ForumPK("forums100", "5"), 1);
    assertThat(result, equalTo(1));
    forum = ForumsDAO.getForum(con, new ForumPK("forums100", "5"));
    expectedForum = new Forum(5, "Forum Fermé", "forum fermé", true, 0, null,
        "2004/03/26", "forums100");
    assertThat(forum, equalTo(expectedForum));
  }

  /**
   * Test of createForum method, of class ForumsDAO.
   */
  @Test
  public void testCreateForum() throws Exception {
    String forumName = RandomGenerator.getRandomString();
    String forumDescription = RandomGenerator.getRandomString();
    String forumCreator = String.valueOf(RandomGenerator.getRandomInt());
    int forumParent = 0;
    String categoryId = String.valueOf(RandomGenerator.getRandomInt());
    int result = ForumsDAO.createForum(con, new ForumPK("forums100"), forumName, forumDescription,
        forumCreator, forumParent, categoryId);
    assertThat(result, equalTo(11));
    Forum forum = ForumsDAO.getForum(con, new ForumPK("forums100", "11"));
    Forum expectedForum = new Forum(11, forumName, forumDescription, true, 0, categoryId,
        DateUtil.formatDate(Calendar.getInstance()), "forums100");
    assertThat(forum, equalTo(expectedForum));
  }

  /**
   * Test of updateForum method, of class ForumsDAO.
   */
  @Test
  public void testUpdateForum() throws Exception {
    ForumPK forumPK = new ForumPK("forums130", "8");
    String forumName = RandomGenerator.getRandomString();
    String forumDescription = RandomGenerator.getRandomString();
    int forumParent = RandomGenerator.getRandomInt();
    String categoryId = String.valueOf(RandomGenerator.getRandomInt());
    ForumsDAO.updateForum(con, forumPK, forumName, forumDescription, forumParent, categoryId);
    Forum forum = ForumsDAO.getForum(con, forumPK);
    Forum expectedForum = new Forum(8, forumName, forumDescription, true, forumParent, categoryId,
        "2004/03/26", "forums130");
    assertThat(forum, equalTo(expectedForum));
  }

  /**
   * Test of deleteForum method, of class ForumsDAO.
   */
  @Test
  public void testDeleteForum() throws Exception {
    ForumPK forumPK = new ForumPK("forums130", "8");
    ForumsDAO.deleteForum(con, forumPK);
    Forum forum = ForumsDAO.getForum(con, forumPK);
    assertNull(forum);
  }
}
