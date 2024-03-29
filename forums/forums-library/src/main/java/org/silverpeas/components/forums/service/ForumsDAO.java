/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.forums.service;

import org.silverpeas.components.forums.model.Forum;
import org.silverpeas.components.forums.model.ForumDetail;
import org.silverpeas.components.forums.model.ForumPK;
import org.silverpeas.components.forums.model.Message;
import org.silverpeas.components.forums.model.MessagePK;
import org.silverpeas.components.forums.model.Moderator;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;

/**
 * Class managing database accesses for forums.
 */
public class ForumsDAO {

  private static final String DELETE_FROM = "DELETE FROM ";
  private static final String INSERT_INTO = "INSERT INTO ";
  private static final String SELECT = "SELECT ";
  private static final String WHERE = " WHERE ";
  private static final String SELECT_COUNT = "SELECT COUNT(";
  private static final String EQUAL_TO_PARAM = " = ? ";
  private static final String AND = " AND ";
  private static final String ORDER_BY = " ORDER BY ";
  private static final String DESC = " DESC";

  // Forums table.
  private static final String FORUM_COLUMN_FORUM_ID = "forumId";
  private static final String FORUM_COLUMN_FORUM_NAME = "forumName";
  private static final String FORUM_COLUMN_FORUM_DESCRIPTION = "forumDescription";
  private static final String FORUM_COLUMN_FORUM_ACTIVE = "forumActive";
  private static final String FORUM_COLUMN_FORUM_PARENT = "forumParent";
  private static final String FORUM_COLUMN_CATEGORY_ID = "categoryId";
  private static final String FORUM_COLUMN_INSTANCE_ID = "instanceId";
  private static final String FORUM_COLUMN_FORUM_CREATION_DATE = "forumCreationDate";
  private static final String FORUM_COLUMN_FORUM_CREATOR = "forumCreator";
  private static final String FORUM_COLUMN_FORUM_LOCK_LEVEL = "forumLockLevel";
  private static final String FORUM_COLUMNS =
      FORUM_COLUMN_FORUM_ID + ", " + FORUM_COLUMN_FORUM_NAME + ", " +
          FORUM_COLUMN_FORUM_DESCRIPTION + ", " + FORUM_COLUMN_FORUM_ACTIVE + ", " +
          FORUM_COLUMN_FORUM_PARENT + ", " + FORUM_COLUMN_CATEGORY_ID + ", " +
          FORUM_COLUMN_INSTANCE_ID + ", " + FORUM_COLUMN_FORUM_CREATION_DATE + ", " +
          FORUM_COLUMN_FORUM_CREATOR;
  // Messages table.
  private static final String MESSAGE_TABLE = "SC_Forums_Message";
  private static final String MESSAGE_COLUMN_MESSAGE_ID = "messageId";
  private static final String MESSAGE_COLUMN_MESSAGE_TITLE = "messageTitle";
  private static final String MESSAGE_COLUMN_MESSAGE_AUTHOR = "messageAuthor";
  private static final String MESSAGE_COLUMN_FORUM_ID = "forumId";
  private static final String MESSAGE_COLUMN_MESSAGE_PARENT_ID = "messageParentId";
  private static final String MESSAGE_COLUMN_MESSAGE_DATE = "messageDate";
  private static final String MESSAGE_COLUMN_STATUS = "status";
  private static final String MESSAGE_COLUMNS =
      MESSAGE_COLUMN_MESSAGE_ID + ", " + MESSAGE_COLUMN_MESSAGE_TITLE + ", " +
          MESSAGE_COLUMN_MESSAGE_AUTHOR + ", " + MESSAGE_COLUMN_FORUM_ID + ", " +
          MESSAGE_COLUMN_MESSAGE_PARENT_ID + ", " + MESSAGE_COLUMN_MESSAGE_DATE + " , " +
          MESSAGE_COLUMN_STATUS;
  // Rights table.
  private static final String RIGHTS_TABLE = "SC_Forums_Rights";
  private static final String RIGHTS_COLUMN_USER_ID = "userId";
  private static final String RIGHTS_COLUMN_FORUM_ID = "forumId";
  private static final String RIGHTS_COLUMNS =
      RIGHTS_COLUMN_USER_ID + ", " + RIGHTS_COLUMN_FORUM_ID;
  // History table.
  private static final String HISTORY_TABLE = "SC_Forums_HistoryUser";
  private static final String HISTORY_COLUMN_USER_ID = "userId";
  private static final String HISTORY_COLUMN_MESSAGE_ID = "messageId";
  private static final String HISTORY_COLUMN_LAST_ACCESS = "lastAccess";
  private static final String HISTORY_COLUMNS =
      HISTORY_COLUMN_USER_ID + ", " + HISTORY_COLUMN_MESSAGE_ID + ", " + HISTORY_COLUMN_LAST_ACCESS;
  private static final String QUERY_GET_FORUMS_LIST =
      SELECT + FORUM_COLUMNS + " FROM sc_forums_forum WHERE instanceId = ?";
  private static final String QUERY_GET_FORUMS_IDS =
      "SELECT forumId FROM sc_forums_forum WHERE instanceId = ?";
  private static final String QUERY_GET_FORUMS_LIST_BY_CATEGORY_WITH_NOT_NULL_CATEGORY =
      SELECT + FORUM_COLUMNS + " FROM sc_forums_forum WHERE instanceId = ? AND categoryId = ?";
  private static final String QUERY_GET_FORUMS_LIST_BY_CATEGORY_WITH_NULL_CATEGORY =
      SELECT + FORUM_COLUMNS +
          " FROM sc_forums_forum WHERE instanceId = ? AND categoryId IS NULL";
  private static final String QUERY_GET_FORUM_SONS =
      "SELECT forumId FROM sc_forums_forum WHERE forumParent = ? AND instanceId = ?";
  private static final String QUERY_GET_FORUM =
      SELECT + FORUM_COLUMNS + " FROM sc_forums_forum WHERE forumId = ? AND instanceId = ?";
  private static final String QUERY_GET_FORUM_NAME =
      "SELECT forumName FROM sc_forums_forum WHERE forumId = ?";
  private static final String QUERY_IS_FORUM_ACTIVE =
      "SELECT forumActive FROM sc_forums_forum WHERE forumId = ?";
  private static final String QUERY_GET_FORUM_PARENT_ID =
      "SELECT forumParent FROM sc_forums_forum WHERE forumId = ?";
  private static final String QUERY_GET_FORUM_INSTANCE_ID =
      "SELECT instanceId FROM sc_forums_forum WHERE forumId = ?";
  private static final String QUERY_GET_FORUM_CREATOR_ID =
      "SELECT forumCreator FROM sc_forums_forum WHERE forumId = ?";
  private static final String QUERY_LOCK_FORUM =
      "UPDATE sc_forums_forum SET  forumLockLevel = ?, " +
          "forumActive = ?, forumCloseDate = ?,instanceId = ? WHERE forumId = ?";
  private static final String QUERY_UNLOCK_FORUM_GET_LEVEL =
      "SELECT forumLockLevel FROM sc_forums_forum WHERE forumId = ?";
  private static final String QUERY_UNLOCK_FORUM_SET_ACTIVE =
      "UPDATE sc_forums_forum SET forumActive = ?" + " WHERE forumId = ?";
  private static final String QUERY_CREATE_FORUM =
      "INSERT INTO sc_forums_forum (" + FORUM_COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
  private static final String QUERY_UPDATE_FORUM = "UPDATE sc_forums_forum SET forumName = ?, " +
      "forumDescription = ?, forumParent = ?, instanceId = ?, categoryId = ? WHERE forumId = ? ";
  private static final String QUERY_DELETE_FORUM_RIGHTS =
      DELETE_FROM + RIGHTS_TABLE + WHERE + RIGHTS_COLUMN_FORUM_ID + " = ?";
  private static final String QUERY_DELETE_FORUM_MESSAGE =
      DELETE_FROM + MESSAGE_TABLE + WHERE + MESSAGE_COLUMN_FORUM_ID + " = ?";
  private static final String QUERY_DELETE_FORUM_FORUM =
      "DELETE FROM sc_forums_forum WHERE forumId = ?";
  private static final String QUERY_GET_ALL_FORUMS_BY_INSTANCE_ID =
      "SELECT forumId FROM sc_forums_forum WHERE instanceId = ?";
  private static final String QUERY_GET_FORUM_DETAIL =
      SELECT + FORUM_COLUMNS + " FROM sc_forums_forum WHERE forumId = ?";

  /**
   * Private constructor to avoid instantiation since all methods of the class are static.
   */
  private ForumsDAO() {
  }

  /**
   * @param con The connection to the database.
   * @param forumPKs The list of forums primary keys.
   * @return The list of forums corresponding to the primary keys (ForumDetail).
   * @throws SQLException An SQL exception.
   */
  public static Collection<ForumDetail> selectByForumPKs(Connection con,
      Collection<ForumPK> forumPKs) throws SQLException {
    List<ForumDetail> forumDetails = new ArrayList<>(forumPKs.size());
    for (ForumPK forumPK : forumPKs) {
      forumDetails.add(getForumDetail(con, forumPK));
    }
    return forumDetails;
  }

  /**
   * @param con The connection to the database.
   * @param forumPKs The list of forums primary keys.
   * @return The list of forums corresponding to the primary keys (Forum).
   * @throws SQLException An SQL exception.
   */
  public static Collection<Forum> getForumsByKeys(Connection con, Collection<ForumPK> forumPKs)
      throws SQLException {
    ArrayList<Forum> forums = new ArrayList<>();
    Iterator<ForumPK> iterator = forumPKs.iterator();
    ForumPK forumPK;
    Forum forum;
    while (iterator.hasNext()) {
      forumPK = iterator.next();
      forum = getForum(con, forumPK);
      if (forum != null) {
        forums.add(forum);
      }
    }
    return forums;
  }

  /**
   * @param con The connection to the database.
   * @param messagePKs The list of messages primary keys.
   * @return The list of messages corresponding to the primary keys (Message).
   * @throws SQLException An SQL exception.
   */
  public static Collection<Message> getMessagesByKeys(Connection con,
      Collection<MessagePK> messagePKs) throws SQLException {
    return getMessagesByKeys(con, messagePKs, false);
  }

  /**
   * @param con The connection to the database.
   * @param messagePKs The list of messages primary keys.
   * @return The list of threads corresponding to the primary keys (Message).
   * @throws SQLException An SQL exception.
   */
  public static Collection<Message> getThreadsByKeys(Connection con,
      Collection<MessagePK> messagePKs) throws SQLException {
    return getMessagesByKeys(con, messagePKs, true);
  }

  /**
   * @param con The connection to the database.
   * @param messagePKs The list of messages primary keys.
   * @param onlyThreads Indicates if only threads messages are searched.
   * @return The list of messages (or only threads depending on onlyThreads) corresponding to the
   * primary keys (Message).
   * @throws SQLException An SQL exception.
   */
  private static Collection<Message> getMessagesByKeys(Connection con,
      Collection<MessagePK> messagePKs, boolean onlyThreads) throws SQLException {
    ArrayList<Message> messages = new ArrayList<>();
    for (MessagePK messagePK : messagePKs) {
      Message message = (onlyThreads ? getThread(con, messagePK) : getMessage(con, messagePK));
      if (message != null) {
        String instanceId = messagePK.getComponentName();
        if (StringUtil.isDefined(instanceId)) {
          // Vérification que le message retourné fait partie d'un forum dont
          // l'instanceid correspond à celui de la clé du message.
          String forumInstanceId = getForumInstanceId(con, message.getForumId());
          if (instanceId.equals(forumInstanceId)) {
            message.setInstanceId(instanceId);
            messages.add(message);
          }
        } else {
          // Ajout systématique si l'instanceid de la clé du message n'est pas renseignée.
          messages.add(message);
        }
      }
    }
    return messages;
  }

  /**
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @return The list of forums corresponding to the primary key (Forum).
   * @throws SQLException An SQL exception.
   */
  public static List<Forum> getForumsList(Connection con, ForumPK forumPK) throws SQLException {
    List<Forum> forums = new ArrayList<>();
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_FORUMS_LIST)) {
      selectStmt.setString(1, forumPK.getComponentName());
      try (ResultSet rs = selectStmt.executeQuery()) {
        while (rs.next()) {
          forums.add(resultSet2Forum(rs));
        }
      }
    }
    return forums;
  }

  /**
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @return The list of ids of forums corresponding to the primary key.
   * @throws SQLException An SQL exception.
   */
  public static List<String> getForumsIds(Connection con, ForumPK forumPK)
      throws SQLException {
    ArrayList<String> forumsIds = new ArrayList<>();
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_FORUMS_IDS)) {
      selectStmt.setString(1, forumPK.getComponentName());
      try (ResultSet rs = selectStmt.executeQuery()) {
        while (rs.next()) {
          forumsIds.add(String.valueOf(rs.getInt(FORUM_COLUMN_FORUM_ID)));
        }
      }
    }
    return forumsIds;
  }

  /**
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @param categoryId The id of the category.
   * @return The list of forums corresponding to the primary key and the category id.
   * @throws SQLException An SQL exception.
   */
  public static List<Forum> getForumsListByCategory(Connection con, ForumPK forumPK,
      String categoryId) throws SQLException {
    String selectQuery = (StringUtil.isDefined(categoryId) ?
        QUERY_GET_FORUMS_LIST_BY_CATEGORY_WITH_NOT_NULL_CATEGORY :
        QUERY_GET_FORUMS_LIST_BY_CATEGORY_WITH_NULL_CATEGORY);
    List<Forum> forums = new ArrayList<>();
    try (PreparedStatement selectStmt = con.prepareStatement(selectQuery)) {
      selectStmt.setString(1, forumPK.getComponentName());
      if (StringUtil.isDefined(categoryId)) {
        selectStmt.setString(2, categoryId);
      }
      try (ResultSet rs = selectStmt.executeQuery()) {
        while (rs.next()) {
          forums.add(resultSet2Forum(rs));
        }
      }
    }
    return forums;
  }

  /**
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @return The list of ids of forums which parent is the forum corresponding to the primary key.
   * @throws SQLException An SQL exception.
   */
  public static List<String> getForumSonsIds(Connection con, ForumPK forumPK) throws SQLException {
    List<String> forumIds = new ArrayList<>();
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_FORUM_SONS)) {
      selectStmt.setInt(1, Integer.parseInt(forumPK.getId()));
      selectStmt.setString(2, forumPK.getComponentName());
      try (ResultSet rs = selectStmt.executeQuery()) {
        while (rs.next()) {
          forumIds.add(String.valueOf(rs.getInt(FORUM_COLUMN_FORUM_ID)));
        }
      }
    }
    return forumIds;
  }

  /**
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @return The forum corresponding to the primary key (Forum).
   * @throws SQLException An SQL exception.
   */
  public static Forum getForum(Connection con, ForumPK forumPK) throws SQLException {
    int forumId = Integer.parseInt(forumPK.getId());
    String instanceId = forumPK.getComponentName();
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_FORUM)) {
      selectStmt.setInt(1, forumId);
      selectStmt.setString(2, instanceId);
      try (ResultSet rs = selectStmt.executeQuery()) {
        if (rs.next()) {
          Forum forum = resultSet2Forum(rs);
          forum.setPk(forumPK);
          return forum;
        }
      }
    }
    return null;
  }

  /**
   * @param con The connection to the database.
   * @param forumId The id of the forum.
   * @return The name corresponding to the forum id.
   * @throws SQLException An SQL exception.
   */
  public static String getForumName(Connection con, int forumId) throws SQLException {
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_FORUM_NAME)) {
      selectStmt.setInt(1, forumId);
      try (ResultSet rs = selectStmt.executeQuery()) {
        if (rs.next()) {
          return rs.getString(FORUM_COLUMN_FORUM_NAME);
        }
      }
    }
    return null;
  }

  /**
   * @param con The connection to the database.
   * @param forumId The id of the forum.
   * @return True if the forum is active.
   * @throws SQLException An SQL exception.
   */
  public static boolean isForumActive(Connection con, int forumId) throws SQLException {
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_IS_FORUM_ACTIVE)) {
      selectStmt.setInt(1, forumId);
      try (ResultSet rs = selectStmt.executeQuery()) {
        if (rs.next()) {
          return (rs.getInt(FORUM_COLUMN_FORUM_ACTIVE) == 1);
        }
      }
    }
    return false;
  }

  /**
   * @param con The connection to the database.
   * @param forumId The id of the forum.
   * @return The id of the parent of the forum.
   * @throws SQLException An SQL exception.
   */
  public static int getForumParentId(Connection con, int forumId) throws SQLException {
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_FORUM_PARENT_ID)) {
      selectStmt.setInt(1, forumId);
      try (ResultSet rs = selectStmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(FORUM_COLUMN_FORUM_PARENT);
        }
      }
    }
    return -1;
  }

  /**
   * @param con The connection to the database.
   * @param forumId The id of the forum.
   * @return The instance id corresponding to the forum id.
   * @throws SQLException An SQL exception.
   */
  public static String getForumInstanceId(Connection con, int forumId) throws SQLException {
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_FORUM_INSTANCE_ID)) {
      selectStmt.setInt(1, forumId);
      try (ResultSet rs = selectStmt.executeQuery()) {
        if (rs.next()) {
          return rs.getString(FORUM_COLUMN_INSTANCE_ID);
        }
      }
    }
    return null;
  }

  /**
   * @param con The connection to the database.
   * @param forumId The id of the forum.
   * @return The id of the creator of the forum.
   * @throws SQLException An SQL exception.
   */
  public static String getForumCreatorId(Connection con, int forumId) throws SQLException {
    try (PreparedStatement stmt = con.prepareStatement(QUERY_GET_FORUM_CREATOR_ID)) {
      stmt.setInt(1, forumId);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getString(FORUM_COLUMN_FORUM_CREATOR);
        } else {
          throw new ForumsRuntimeException(failureOnGetting("forum", forumId));
        }
      }
    }
  }

  /**
   * Locks the forum corresponding to the primary key.
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @param level The lock level.
   * @throws SQLException An SQL exception.
   */
  public static void lockForum(Connection con, ForumPK forumPK, int level) throws SQLException {
    try (PreparedStatement updateStmt = con.prepareStatement(QUERY_LOCK_FORUM)) {
      updateStmt.setInt(1, level);
      updateStmt.setInt(2, 0);
      updateStmt.setString(3, DateUtil.date2SQLDate(new Date()));
      updateStmt.setString(4, forumPK.getComponentName());
      updateStmt.setInt(5, Integer.parseInt(forumPK.getId()));
      updateStmt.executeUpdate();
    }
  }

  /**
   * Unlocks the forum corresponding to the primary key.
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @param level The lock level.
   * @return
   * @throws SQLException An SQL exception.
   */
  public static int unlockForum(Connection con, ForumPK forumPK, int level) throws SQLException {
    int result = 0;
    int forumLocklevel = 0;
    try (PreparedStatement declareStmt = con.prepareStatement(QUERY_UNLOCK_FORUM_GET_LEVEL)) {
      declareStmt.setInt(1, Integer.parseInt(forumPK.getId()));
      try (ResultSet rs = declareStmt.executeQuery()) {
        if (rs.next()) {
          forumLocklevel = rs.getInt(FORUM_COLUMN_FORUM_LOCK_LEVEL);
        }

        if (forumLocklevel >= level) {
          try (PreparedStatement declareStmt1 = con.prepareStatement(
              QUERY_UNLOCK_FORUM_SET_ACTIVE)) {
            declareStmt1.setInt(1, 1);
            declareStmt1.setInt(2, Integer.parseInt(forumPK.getId()));
            declareStmt1.executeUpdate();
            result = 1;
          }
        } else {
          result = 0;
        }
      }
    }
    return result;
  }

  /**
   * Creates a forum.
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @param forumName The name of the forum.
   * @param forumDescription The description of the forum.
   * @param forumCreator The creator of the forum.
   * @param forumParent The id of the forum's parent forum.
   * @param categoryId The id of the category.
   * @return The id of the newly created forum.
   * @throws SQLException An SQL exception.
   */
  public static int createForum(Connection con, ForumPK forumPK, String forumName,
      String forumDescription, String forumCreator, int forumParent, String categoryId)
      throws SQLException {
    try (PreparedStatement insertStmt = con.prepareStatement(QUERY_CREATE_FORUM)) {
      int forumId = DBUtil.getNextId("sc_forums_forum", FORUM_COLUMN_FORUM_ID);
      insertStmt.setInt(1, forumId);
      insertStmt.setString(2, forumName);
      insertStmt.setString(3, forumDescription);
      insertStmt.setInt(4, 1);
      insertStmt.setInt(5, forumParent);
      if (StringUtil.isDefined(categoryId)) {
        insertStmt.setString(6, categoryId);
      } else {
        insertStmt.setNull(6, Types.VARCHAR);
      }
      insertStmt.setString(7, forumPK.getComponentName());
      insertStmt.setString(8, DateUtil.date2SQLDate(new Date()));
      insertStmt.setString(9, forumCreator);
      insertStmt.executeUpdate();

      return forumId;
    }
  }

  /**
   * Updates the forum corresponding to the primary key.
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @param forumName The name of the forum.
   * @param forumDescription The description of the forum.
   * @param forumParent The id of the forum's parent forum.
   * @param categoryId The id of the category.
   * @throws SQLException An SQL exception.
   */
  public static void updateForum(Connection con, ForumPK forumPK, String forumName,
      String forumDescription, int forumParent, String categoryId) throws SQLException {
    try (PreparedStatement updateStmt = con.prepareStatement(QUERY_UPDATE_FORUM)) {
      updateStmt.setString(1, forumName);
      updateStmt.setString(2, forumDescription);
      updateStmt.setInt(3, forumParent);
      updateStmt.setString(4, forumPK.getComponentName());
      if (StringUtil.isDefined(categoryId)) {
        updateStmt.setString(5, categoryId);
      } else {
        updateStmt.setNull(5, Types.VARCHAR);
      }
      updateStmt.setInt(6, Integer.parseInt(forumPK.getId()));
      updateStmt.executeUpdate();
    }
  }

  /**
   * Deletes the forum corresponding to the primary key.
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @throws SQLException An SQL exception.
   */
  public static void deleteForum(Connection con, ForumPK forumPK) throws SQLException {
    String sForumId = forumPK.getId();
    int forumId = Integer.parseInt(sForumId);
    try (PreparedStatement deleteStmt1 = con.prepareStatement(QUERY_DELETE_FORUM_RIGHTS)) {
      deleteStmt1.setString(1, sForumId);
      deleteStmt1.executeUpdate();

      try (PreparedStatement deleteStmt2 = con.prepareStatement(QUERY_DELETE_FORUM_MESSAGE)) {
        deleteStmt2.setInt(1, forumId);
        deleteStmt2.executeUpdate();

        try (PreparedStatement deleteStmt3 = con.prepareStatement(QUERY_DELETE_FORUM_FORUM)){
          deleteStmt3.setInt(1, forumId);
          deleteStmt3.executeUpdate();
        }
      }
    }
  }

  private static final String FORUM_RIGHTS_DELETION = DELETE_FROM + RIGHTS_TABLE + " where " +
      FORUM_COLUMN_FORUM_ID + " in ";
  private static final String FORUM_HISTORY_DELETION = DELETE_FROM + HISTORY_TABLE +
      " where messageId in (select messageId from " + MESSAGE_TABLE +
      " as m JOIN sc_forums_forum as f on m." + FORUM_COLUMN_FORUM_ID + " = f." +
      FORUM_COLUMN_FORUM_ID + " and " + FORUM_COLUMN_INSTANCE_ID + "= ?)";
  private static final String FORUM_MESSAGES_DELETION = DELETE_FROM + MESSAGE_TABLE + " where " +
      FORUM_COLUMN_FORUM_ID + " in (select " + FORUM_COLUMN_FORUM_ID +
      " from sc_forums_forum where " + FORUM_COLUMN_INSTANCE_ID + "= ?)";
  private static final String FORUMS_DELETION = "delete from sc_forums_forum where " +
      FORUM_COLUMN_INSTANCE_ID + "= ?";


  public static void deleteAllForums(Connection con, String instanceId) throws SQLException {
    Collection<Integer> forumIds = getAllForumsByInstanceId(con, instanceId);
    if (!forumIds.isEmpty()) {
      String listOfIds =
          forumIds.stream().map(i -> "'" + i + "'").collect(Collectors.joining(",", "(", ")"));
      try (PreparedStatement statement = con.prepareStatement(FORUM_RIGHTS_DELETION + listOfIds)) {
        statement.execute();
      }
    }
    try(PreparedStatement statement = con.prepareStatement(FORUM_HISTORY_DELETION)) {
      statement.setString(1, instanceId);
      statement.execute();
    }
    try(PreparedStatement statement = con.prepareStatement(FORUM_MESSAGES_DELETION)) {
      statement.setString(1, instanceId);
      statement.execute();
    }
    try(PreparedStatement statement = con.prepareStatement(FORUMS_DELETION)) {
      statement.setString(1, instanceId);
      statement.execute();
    }
  }

  private static final String FROM = " FROM ";
  private static final String QUERY_GET_MESSAGES_LIST_BY_FORUM =
      SELECT + MESSAGE_COLUMNS + FROM + MESSAGE_TABLE + WHERE + MESSAGE_COLUMN_FORUM_ID +
          " = ?";

  /**
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @return The list of messages of the forum corresponding to the primary key (Message).
   * @throws SQLException An SQL exception.
   */
  public static List<Message> getMessagesList(Connection con, ForumPK forumPK)
      throws SQLException {
    ArrayList<Message> messages = new ArrayList<>();
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_MESSAGES_LIST_BY_FORUM)) {
      selectStmt.setInt(1, Integer.parseInt(forumPK.getId()));
      try (ResultSet rs = selectStmt.executeQuery()) {
        while (rs.next()) {
          messages.add(resultSet2Message(rs, forumPK.getInstanceId()));
        }
      }
    }
    return messages;
  }

  private static final String QUERY_GET_MESSAGES_IDS_BY_FORUM =
      SELECT + MESSAGE_COLUMN_MESSAGE_ID + FROM + MESSAGE_TABLE + WHERE + MESSAGE_COLUMN_FORUM_ID +
          EQUAL_TO_PARAM;

  private static final String QUERY_GET_MESSAGES_IDS_BY_FORUM_AND_MESSAGE =
      SELECT + MESSAGE_COLUMN_MESSAGE_ID + FROM + MESSAGE_TABLE + WHERE + MESSAGE_COLUMN_FORUM_ID +
          " = ?" + AND + MESSAGE_COLUMN_MESSAGE_PARENT_ID + " = ?";

  /**
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @param messageParentId The id of the message's parent message.
   * @return The list of ids of messages of the forum corresponding to the primary key and which
   * parent message corresponds to the message id (if it is valued).
   * @throws SQLException An SQL exception.
   */
  public static List<String> getMessagesIds(Connection con, ForumPK forumPK, int messageParentId)
      throws SQLException {
    String query = (messageParentId != -1 ? QUERY_GET_MESSAGES_IDS_BY_FORUM_AND_MESSAGE :
        QUERY_GET_MESSAGES_IDS_BY_FORUM);
    ArrayList<String> messagesIds = new ArrayList<>();
    try (PreparedStatement selectStmt = con.prepareStatement(query)) {
      selectStmt.setInt(1, Integer.parseInt(forumPK.getId()));
      if (messageParentId != -1) {
        selectStmt.setInt(2, messageParentId);
      }
      try (ResultSet rs = selectStmt.executeQuery()) {
        while (rs.next()) {
          messagesIds.add(String.valueOf(rs.getInt(MESSAGE_COLUMN_MESSAGE_ID)));
        }
      }
    }
    return messagesIds;
  }

  /**
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @return The list of ids of messages of the forum corresponding to the primary key.
   * @throws SQLException An SQL exception.
   */
  public static List<String> getMessagesIds(Connection con, ForumPK forumPK) throws SQLException {
    return getMessagesIds(con, forumPK, -1);
  }

  /**
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @return The list of ids of threads of the forum corresponding to the primary key.
   * @throws SQLException An SQL exception.
   */
  public static List<String> getSubjectsIds(Connection con, ForumPK forumPK) throws SQLException {
    return getMessagesIds(con, forumPK, 0);
  }

  private static final String QUERY_GET_NB_MESSAGES_SUBJECTS =
      SELECT_COUNT + MESSAGE_COLUMN_MESSAGE_ID + ")" + FROM + MESSAGE_TABLE + WHERE +
          MESSAGE_COLUMN_FORUM_ID + " = ?" + AND + MESSAGE_COLUMN_MESSAGE_PARENT_ID + " = 0 AND " +
          MESSAGE_COLUMN_STATUS + EQUAL_TO_PARAM;
  private static final String QUERY_GET_NB_MESSAGES_NOT_SUBJECTS =
      SELECT_COUNT + MESSAGE_COLUMN_MESSAGE_ID + ")" + FROM + MESSAGE_TABLE + WHERE +
          MESSAGE_COLUMN_FORUM_ID + " = ?" + AND + MESSAGE_COLUMN_MESSAGE_PARENT_ID + " != 0 AND " +
          MESSAGE_COLUMN_STATUS + EQUAL_TO_PARAM;

  /**
   * @param con The connection to the database.
   * @param forumId The id of the forum.
   * @param type The type of the searched messages.
   * @return The number of messages corresponding to the forum id and the type (threads or not).
   * @throws SQLException An SQL exception.
   */
  public static int getNbMessages(Connection con, int forumId, String type, String status)
      throws SQLException {
    String selectQuery = ("Subjects".equals(type) ? QUERY_GET_NB_MESSAGES_SUBJECTS :
        QUERY_GET_NB_MESSAGES_NOT_SUBJECTS);
    try (PreparedStatement prepStmt = con.prepareStatement(selectQuery)) {
      prepStmt.setInt(1, forumId);
      prepStmt.setString(2, status);
      try (ResultSet rs = prepStmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1);
        }
      }
    }
    return 0;
  }

  private static final String QUERY_GET_AUTHOR_NB_MESSAGES =
      SELECT_COUNT + MESSAGE_COLUMN_MESSAGE_ID + ")" + FROM + MESSAGE_TABLE + WHERE +
          MESSAGE_COLUMN_MESSAGE_AUTHOR + " = ?" + AND + MESSAGE_COLUMN_STATUS + EQUAL_TO_PARAM;

  /**
   * @param con The connection to the database.
   * @param userId The user's id.
   * @return The number of messages written by the author corresponding to the user id.
   * @throws SQLException An SQL exception.
   */
  public static int getAuthorNbMessages(Connection con, String userId, String status)
      throws SQLException {
    try (PreparedStatement prepStmt = con.prepareStatement(QUERY_GET_AUTHOR_NB_MESSAGES)) {
      prepStmt.setString(1, userId);
      prepStmt.setString(2, status);
      try (ResultSet rs = prepStmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1);
        }
      }
    }
    return 0;
  }

  private static final String QUERY_GET_NB_RESPONSES =
      SELECT + MESSAGE_COLUMN_MESSAGE_ID + FROM + MESSAGE_TABLE + WHERE + MESSAGE_COLUMN_FORUM_ID +
          " = ?" + AND + MESSAGE_COLUMN_MESSAGE_PARENT_ID +
          " = ? AND " + MESSAGE_COLUMN_STATUS + " = ?";

  /**
   * @param con The connection to the database.
   * @param forumId The id of the forum.
   * @param messageId The id of the message.
   * @return The number of responses to the message corresponding to the message id and the forum
   * id.
   */
  public static int getNbResponses(Connection con, int forumId, int messageId, String status) {
    ArrayList<Integer> nextMessageIds = new ArrayList<>();
    try (PreparedStatement prepStmt = con.prepareStatement(QUERY_GET_NB_RESPONSES)) {
      prepStmt.setInt(1, forumId);
      prepStmt.setInt(2, messageId);
      prepStmt.setString(3, status);
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          nextMessageIds.add(rs.getInt(MESSAGE_COLUMN_MESSAGE_ID));
        }
      }
    } catch (SQLException sqle) {
      SilverLogger.getLogger(ForumsDAO.class).error(sqle.getMessage(), sqle);
      return 0;
    }
    int nb = nextMessageIds.size();
    int nextMessageId;
    for (int i = 0, n = nextMessageIds.size(); i < n; i++) {
      nextMessageId = (Integer) nextMessageIds.get(i);
      nb += getNbResponses(con, forumId, nextMessageId, status);
    }
    return nb;
  }

  private static final String QUERY_GET_LAST_MESSAGE =
      SELECT + MESSAGE_COLUMN_MESSAGE_ID + FROM + MESSAGE_TABLE + WHERE + MESSAGE_COLUMN_FORUM_ID +
          " = ?" + AND + MESSAGE_COLUMN_MESSAGE_PARENT_ID + " != 0 AND " + MESSAGE_COLUMN_STATUS +
          EQUAL_TO_PARAM + ORDER_BY + MESSAGE_COLUMN_MESSAGE_DATE + DESC + ", " +
          MESSAGE_COLUMN_MESSAGE_ID + DESC;

  /**
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @return The last message of the forum corresponding to the forum id.
   * @throws SQLException An SQL exception.
   */
  public static Message getLastMessage(Connection con, ForumPK forumPK, String status)
      throws SQLException {
    int messageId = -1;
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_LAST_MESSAGE)) {
      selectStmt.setInt(1, Integer.parseInt(forumPK.getId()));
      selectStmt.setString(2, status);
      try (ResultSet rs = selectStmt.executeQuery()) {
        if (rs.next()) {
          messageId = rs.getInt(1);
        }
      }
    }
    if (messageId != -1) {
      MessagePK messagePK = new MessagePK(forumPK.getComponentName(), String.valueOf(messageId));
      return getMessage(con, messagePK);
    }
    return null;
  }

  /**
   * @param con The connection to the database.
   * @param forumPKs The list of forums primary keys.
   * @param count The maximum number of returned threads.
   * @return The last 'count' threads from the forums corresponding to the primary keys.
   * @throws SQLException An SQL exception.
   */
  public static List<Message> getLastThreads(Connection con, ForumPK[] forumPKs, int count)
      throws SQLException {
    ArrayList<Message> messages = new ArrayList<>();
    if (forumPKs.length > 0) {
      StringBuilder selectQuery = new StringBuilder(
          SELECT + MESSAGE_COLUMN_MESSAGE_ID + FROM + MESSAGE_TABLE + WHERE +
              MESSAGE_COLUMN_MESSAGE_PARENT_ID + " = ?" + AND + MESSAGE_COLUMN_FORUM_ID +
              " IN(");
      for (int i = 0, n = forumPKs.length; i < n; i++) {
        if (i > 0) {
          selectQuery.append(", ");
        }
        selectQuery.append(forumPKs[i].getId());
      }
      selectQuery.append(")").append(ORDER_BY).append(MESSAGE_COLUMN_MESSAGE_DATE).append(DESC);


      ArrayList<String> messageIds = new ArrayList<>(count);
      int messagesCount = 0;
      try (PreparedStatement selectStmt = con.prepareStatement(selectQuery.toString())) {
        selectStmt.setInt(1, 0);
        try (ResultSet rs = selectStmt.executeQuery()) {
          while (rs.next() && messagesCount < count) {
            messageIds.add(String.valueOf(rs.getInt(MESSAGE_COLUMN_MESSAGE_ID)));
            messagesCount++;
          }
        }
      }

      String componentName = forumPKs[0].getComponentName();
      for (int i = 0; i < messagesCount; i++) {
        MessagePK messagePK = new MessagePK(componentName, messageIds.get(i));
        messages.add(getMessage(con, messagePK));
      }
    }
    return messages;
  }

  /**
   * @param con The connection to the database.
   * @param forumPKs The list of forums primary keys.
   * @param count The maximum number of returned threads.
   * @return The last not answered 'count' threads from the forums corresponding to the primary
   * keys.
   * @throws SQLException An SQL exception.
   */
  public static Collection<Message> getNotAnsweredLastThreads(Connection con, ForumPK[] forumPKs,
      int count) throws SQLException {
    ArrayList<Message> messages = new ArrayList<>();
    if (forumPKs.length > 0) {
      StringBuilder selectQuery = new StringBuilder(
          SELECT + MESSAGE_COLUMN_MESSAGE_ID + ", " + MESSAGE_COLUMN_FORUM_ID + FROM +
              MESSAGE_TABLE + WHERE + MESSAGE_COLUMN_MESSAGE_PARENT_ID + " = ?" + AND +
              MESSAGE_COLUMN_FORUM_ID + " IN(");
      for (int i = 0, n = forumPKs.length; i < n; i++) {
        if (i > 0) {
          selectQuery.append(", ");
        }
        selectQuery.append(forumPKs[i].getId());
      }
      selectQuery.append(") ORDER BY " + MESSAGE_COLUMN_MESSAGE_DATE + DESC);


      ArrayList<String> messageIds = new ArrayList<>(count);
      int messageId;
      int forumId;
      int messagesCount = 0;
      try (PreparedStatement selectStmt = con.prepareStatement(selectQuery.toString())) {
        selectStmt.setInt(1, 0);
        try (ResultSet rs = selectStmt.executeQuery()) {
          while (rs.next() && messagesCount < count) {
            messageId = rs.getInt(MESSAGE_COLUMN_MESSAGE_ID);
            forumId = rs.getInt(MESSAGE_COLUMN_FORUM_ID);

            messagesCount += fillMessageIds(con, messageIds, messageId, forumId);
          }
        }
      }

      String componentName = forumPKs[0].getComponentName();
      for (int i = 0; i < messagesCount; i++) {
        MessagePK messagePK = new MessagePK(componentName, messageIds.get(i));
        messages.add(getMessage(con, messagePK));
      }
    }
    return messages;
  }

  private static int fillMessageIds(final Connection con, final ArrayList<String> messageIds,
      final int messageId, final int forumId) throws SQLException {
    String query = SELECT_COUNT + MESSAGE_COLUMN_MESSAGE_ID + ") FROM " + MESSAGE_TABLE + WHERE +
        MESSAGE_COLUMN_FORUM_ID + " = ?" + AND + MESSAGE_COLUMN_MESSAGE_PARENT_ID + " = ?";
    int messagesCount = 0;
    try (PreparedStatement prepStmt = con.prepareStatement(query)) {
      prepStmt.setInt(1, forumId);
      prepStmt.setInt(2, messageId);
      try (ResultSet rs2 = prepStmt.executeQuery()) {
        if (rs2.next()) {
          int sonsCount = rs2.getInt(1);
          if (sonsCount == 0) {
            messageIds.add(String.valueOf(messageId));
            messagesCount++;
          }
        }
      }
    }
    return messagesCount;
  }

  /**
   * @param con The connection to the database.
   * @param instanceId The id of the forums instance.
   * @return The list of ids of messages from the forums corresponding to the instance id.
   * @throws SQLException An SQL exception.
   */
  public static Collection<String> getLastMessageRSS(Connection con, String instanceId)
      throws SQLException {
    Collection<String> messageIds = new ArrayList<>();
    Collection<Integer> forumIds = getAllForumsByInstanceId(con, instanceId);
    Iterator<Integer> it = forumIds.iterator();
    while (it.hasNext()) {
      int forumId = it.next().intValue();
      messageIds.addAll(getAllMessageByForum(con, forumId));
    }
    return messageIds;
  }

  /**
   * @param con The connection to the database.
   * @param instanceId The id of the forums instance.
   * @return The list of ids of forums corresponding to the instance id.
   * @throws SQLException An SQL exception.
   */
  private static Collection<Integer> getAllForumsByInstanceId(Connection con, String instanceId)
      throws SQLException {
    Collection<Integer> forumIds = new ArrayList<>();
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_ALL_FORUMS_BY_INSTANCE_ID)) {
      selectStmt.setString(1, instanceId);
      try (ResultSet rs = selectStmt.executeQuery()) {
        while (rs.next()) {
          forumIds.add(Integer.valueOf(rs.getInt(1)));
        }
      }
    }
    return forumIds;
  }

  private static final String QUERY_GET_ALL_MESSAGES_BY_FORUM =
      SELECT + MESSAGE_COLUMN_MESSAGE_ID + FROM + MESSAGE_TABLE + WHERE + MESSAGE_COLUMN_FORUM_ID +
          " = ?" + ORDER_BY + MESSAGE_COLUMN_MESSAGE_DATE + " DESC, " + MESSAGE_COLUMN_MESSAGE_ID +
          DESC;

  /**
   * @param con The connection to the database.
   * @param forumId The id of the forum.
   * @return The list of ids of messages from the forum corresponding to the forum id.
   * @throws SQLException An SQL exception.
   */
  private static Collection<String> getAllMessageByForum(Connection con, int forumId)
      throws SQLException {
    Collection<String> messageIds = new ArrayList<>();
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_ALL_MESSAGES_BY_FORUM)) {
      selectStmt.setInt(1, forumId);
      try (ResultSet rs = selectStmt.executeQuery()) {

        while (rs.next()) {
          messageIds.add(String.valueOf(rs.getInt(1)));
        }
      }
    }
    return messageIds;
  }

  /**
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @param messageParentIds The ids of the parent messages.
   * @return The last message from the forum corresponding to the primary key among the messages
   * which id or parent message id belong to the list.
   * @throws SQLException An SQL exception.
   */
  public static Message getLastMessage(Connection con, ForumPK forumPK,
      List<String> messageParentIds, String status) throws SQLException {
    StringBuilder selectQuery =
        new StringBuilder(SELECT + MESSAGE_COLUMN_MESSAGE_ID + FROM + MESSAGE_TABLE);

    int messageParentIdsCount = (messageParentIds != null ? messageParentIds.size() : 0);
    if (messageParentIdsCount > 0) {
      selectQuery.append(WHERE).append(MESSAGE_COLUMN_STATUS).append(" = ? AND ");
      selectQuery.append(" (");
      for (int i = 0; i < messageParentIdsCount; i++) {
        if (i > 0) {
          selectQuery.append(" OR ");
        }
        selectQuery.append(MESSAGE_COLUMN_MESSAGE_PARENT_ID)
            .append(" = ?")
            .append(" OR ")
            .append(MESSAGE_COLUMN_MESSAGE_ID)
            .append(" = ?");
      }
      selectQuery.append(")");
    }
    selectQuery.append(ORDER_BY)
        .append(MESSAGE_COLUMN_MESSAGE_DATE)
        .append(" DESC, ")
        .append(MESSAGE_COLUMN_MESSAGE_ID)
        .append(DESC);

    String messageId = "";
    try (PreparedStatement selectStmt = con.prepareStatement(selectQuery.toString())) {
      selectStmt.setString(1, status);
      if (messageParentIdsCount > 0) {
        int index = 2;
        int messageParentId;
        for (int i = 0; i < messageParentIdsCount; i++) {
          messageParentId = Integer.parseInt((String) messageParentIds.get(i));
          selectStmt.setInt(index++, messageParentId);
          selectStmt.setInt(index++, messageParentId);
        }
      }
      try (ResultSet rs = selectStmt.executeQuery()) {
        if (rs.next()) {
          messageId = String.valueOf(rs.getInt(1));
        }
      }
    }

    Message message = null;
    if (!"".equals(messageId)) {
      MessagePK messagePK = new MessagePK(forumPK.getComponentName(), messageId);
      message = getMessage(con, messagePK);
    }
    return message;
  }

  private static final String QUERY_GET_MESSAGE_INFOS =
      SELECT + MESSAGE_COLUMNS + FROM + MESSAGE_TABLE + WHERE +
          MESSAGE_COLUMN_MESSAGE_ID + " = ?";

  /**
   * @param con The connection to the database.
   * @param messagePK The primary key of the message.
   * @return The message corresponding to the primary key (Vector).
   * @throws SQLException An SQL exception.
   */
  public static List getMessageInfos(Connection con, MessagePK messagePK) throws SQLException {
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_MESSAGE_INFOS)) {
      selectStmt.setInt(1, Integer.parseInt(messagePK.getId()));
      try (ResultSet rs = selectStmt.executeQuery()) {
        if (rs.next()) {
          return resultSet2VectorMessage(rs);
        }
      }
    }
    return new ArrayList();
  }

  private static final String QUERY_GET_MESSAGE =
      SELECT + MESSAGE_COLUMNS + FROM + MESSAGE_TABLE + WHERE +
          MESSAGE_COLUMN_MESSAGE_ID + " = ?";

  /**
   * @param con The connection to the database.
   * @param messagePK The primary key of the message.
   * @return The message corresponding to the primary key (Message).
   * @throws SQLException An SQL exception.
   */
  public static Message getMessage(Connection con, MessagePK messagePK) throws SQLException {
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_MESSAGE)) {
      selectStmt.setInt(1, Integer.parseInt(messagePK.getId()));
      try (ResultSet rs = selectStmt.executeQuery()) {
        if (rs.next()) {
          Message message = resultSet2Message(rs, messagePK.getInstanceId());
          message.setInstanceId(messagePK.getComponentName());
          message.setPk(messagePK);
          return message;
        }
      }
    }
    return null;
  }

  private static final String QUERY_GET_MESSAGE_TITLE =
      SELECT + MESSAGE_COLUMN_MESSAGE_TITLE + FROM + MESSAGE_TABLE + WHERE +
          MESSAGE_COLUMN_MESSAGE_ID + " = ?";

  /**
   * @param con The connection to the database.
   * @param messageId The id of the message.
   * @return The title of the message..
   * @throws SQLException An SQL exception.
   */
  public static String getMessageTitle(Connection con, int messageId) throws SQLException {
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_MESSAGE_TITLE)) {
      selectStmt.setInt(1, messageId);
      try (ResultSet rs = selectStmt.executeQuery()) {
        if (rs.next()) {
          return rs.getString(MESSAGE_COLUMN_MESSAGE_TITLE);
        }
      }
    }
    return "";
  }

  private static final String QUERY_GET_MESSAGE_PARENT_ID =
      SELECT + MESSAGE_COLUMN_MESSAGE_PARENT_ID + FROM + MESSAGE_TABLE + WHERE +
          MESSAGE_COLUMN_MESSAGE_ID + " = ?";

  /**
   * @param con The connection to the database.
   * @param messageId The id of the message.
   * @return The id of the parent of the message..
   * @throws SQLException An SQL exception.
   */
  public static int getMessageParentId(Connection con, int messageId) throws SQLException {
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_MESSAGE_PARENT_ID)) {
      selectStmt.setInt(1, messageId);
      try (ResultSet rs = selectStmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(MESSAGE_COLUMN_MESSAGE_PARENT_ID);
        }
      }
    }
    return -1;
  }

  private static final String QUERY_GET_THREAD =
      SELECT + MESSAGE_COLUMNS + FROM + MESSAGE_TABLE + WHERE + MESSAGE_COLUMN_MESSAGE_ID + " = ?" +
          AND + MESSAGE_COLUMN_MESSAGE_PARENT_ID + " = ?";

  /**
   * @param con The connection to the database.
   * @param messagePK The primary key of the message.
   * @return The thread corresponding to the primary key (Message).
   * @throws SQLException An SQL exception.
   */
  public static Message getThread(Connection con, MessagePK messagePK) throws SQLException {
    int messageId = Integer.parseInt(messagePK.getId());
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_THREAD)) {
      selectStmt.setInt(1, messageId);
      selectStmt.setInt(2, 0);
      try (ResultSet rs = selectStmt.executeQuery()) {
        if (rs.next()) {
          Message message = resultSet2Message(rs, messagePK.getInstanceId());
          message.setPk(messagePK);
          return message;
        }
      }
    }
    return null;
  }

  private static final String QUERY_CREATE_MESSAGE =
      INSERT_INTO + MESSAGE_TABLE + " (" + MESSAGE_COLUMNS + ")" +
          " VALUES (?, ?, ?, ?, ?, ?, ?)";

  /**
   * Creates a message.
   * @param con The connection to the database.
   * @param messageTitle The title of the message.
   * @param messageAuthor The author of the message.
   * @param messageDate The date of creation of the message.
   * @param forumId The id of the parent forum.
   * @param messageParent The id of the parent message.
   * @return The id of the newly created message.
   * @throws SQLException An SQL exception.
   */
  public static int createMessage(Connection con, String messageTitle, String messageAuthor,
      Date messageDate, int forumId, int messageParent, String status) throws SQLException {
    Date finalMessageDate = messageDate;
    if (finalMessageDate == null) {
      finalMessageDate = new Date();
    }

    try (PreparedStatement insertStmt = con.prepareStatement(QUERY_CREATE_MESSAGE)) {
      int messageId = DBUtil.getNextId(MESSAGE_TABLE, MESSAGE_COLUMN_MESSAGE_ID);
      insertStmt.setInt(1, messageId);
      insertStmt.setString(2, messageTitle);
      insertStmt.setString(3, messageAuthor);
      insertStmt.setInt(4, forumId);
      insertStmt.setInt(5, messageParent);
      insertStmt.setTimestamp(6, new Timestamp(finalMessageDate.getTime()));
      insertStmt.setString(7, status);
      insertStmt.executeUpdate();

      // ajout pour ce message d'une date de visite
      addLastVisit(con, messageAuthor, messageId);
      return messageId;
    }
  }

  private static final String QUERY_UPDATE_MESSAGE =
      "UPDATE " + MESSAGE_TABLE + " SET " + MESSAGE_COLUMN_MESSAGE_TITLE + " = ? , " +
          MESSAGE_COLUMN_STATUS + EQUAL_TO_PARAM + WHERE + MESSAGE_COLUMN_MESSAGE_ID + " = ?";

  /**
   * Updates the message corresponding to the primary key.
   * @param con The connection to the database.
   * @param messagePK The primary key of the message.
   * @param title The title of the message.
   * @throws SQLException An SQL exception.
   */
  public static void updateMessage(Connection con, MessagePK messagePK, String title, String status)
      throws SQLException {
    try (PreparedStatement updateStmt = con.prepareStatement(QUERY_UPDATE_MESSAGE)) {
      updateStmt.setString(1, title);
      updateStmt.setString(2, status);
      updateStmt.setInt(3, Integer.parseInt(messagePK.getId()));
      updateStmt.executeUpdate();
    }
  }

  private static final String QUERY_DELETE_MESSAGE_MESSAGE =
      DELETE_FROM + MESSAGE_TABLE + WHERE + MESSAGE_COLUMN_MESSAGE_ID + " = ?";

  /**
   * Deletes the message corresponding to the primary key.
   * @param con The connection to the database.
   * @param messagePK The primary key of the message.
   * @throws SQLException An SQL exception.
   */
  public static void deleteMessage(Connection con, MessagePK messagePK) throws SQLException {
    try (PreparedStatement deleteStmt = con.prepareStatement(QUERY_DELETE_MESSAGE_MESSAGE)) {
      deleteStmt.setInt(1, Integer.parseInt(messagePK.getId()));
      deleteStmt.executeUpdate();
    }
  }

  private static final String QUERY_GET_MESSAGE_SONS =
      SELECT + MESSAGE_COLUMN_MESSAGE_ID + FROM + MESSAGE_TABLE + WHERE +
          MESSAGE_COLUMN_MESSAGE_PARENT_ID + " = ?";

  /**
   * @param con The connection to the database.
   * @param messagePK The primary key of the message.
   * @return The list of ids of the messages which parent is the message corresponding to the
   * primary key.
   * @throws SQLException An SQL exception.
   */
  public static Collection<String> getMessageSons(Connection con, MessagePK messagePK)
      throws SQLException {
    Collection<String> messagesIds = new ArrayList<>();
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_MESSAGE_SONS)) {
      selectStmt.setInt(1, Integer.parseInt(messagePK.getId()));
      try (ResultSet rs = selectStmt.executeQuery()) {
        while (rs.next()) {
          messagesIds.add(String.valueOf(rs.getInt(MESSAGE_COLUMN_MESSAGE_ID)));
        }
      }
    }
    return messagesIds;
  }

  private static final String QUERY_IS_MODERATOR =
      SELECT + RIGHTS_COLUMN_FORUM_ID + FROM + RIGHTS_TABLE + WHERE +
          RIGHTS_COLUMN_USER_ID + " = ?";

  /**
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @param userId The user's id.
   * @return True if the user owns the role of moderator on the forum corresponding to the primary
   * key, else false.
   * @throws SQLException An SQL exception.
   */
  public static boolean isModerator(Connection con, ForumPK forumPK, String userId)
      throws SQLException {
    try (PreparedStatement prepStmt = con.prepareStatement(QUERY_IS_MODERATOR)) {
      prepStmt.setString(1, userId);
      try (ResultSet rs = prepStmt.executeQuery()) {
        String forumId = forumPK.getId();
        while (rs.next()) {
          if (rs.getString(RIGHTS_COLUMN_FORUM_ID).trim().equals(forumId)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static final String QUERY_ADD_MODERATOR =
      INSERT_INTO + RIGHTS_TABLE + " (" + RIGHTS_COLUMNS + ")" + " VALUES (?, ?)";

  /**
   * Adds the role of moderator to the user on the forum corresponding to the primary key.
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @param userId The user's id.
   * @throws SQLException An SQL exception.
   */
  public static void addModerator(Connection con, ForumPK forumPK, String userId)
      throws SQLException {
    try (PreparedStatement insertStmt = con.prepareStatement(QUERY_ADD_MODERATOR)) {
      insertStmt.setString(1, userId);
      insertStmt.setString(2, forumPK.getId());
      insertStmt.executeUpdate();
    }
  }

  private static final String QUERY_REMOVE_MODERATOR =
      DELETE_FROM + RIGHTS_TABLE + WHERE + RIGHTS_COLUMN_USER_ID + " = ?" + AND +
          RIGHTS_COLUMN_FORUM_ID + " = ?";

  /**
   * Removes the role of moderator to the user on the forum corresponding to the primary key.
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @param userId The user's id.
   * @throws SQLException An SQL exception.
   */
  public static void removeModerator(Connection con, ForumPK forumPK, String userId)
      throws SQLException {
    try (PreparedStatement deleteStmt = con.prepareStatement(QUERY_REMOVE_MODERATOR)) {
      deleteStmt.setString(1, userId);
      deleteStmt.setString(2, forumPK.getId());
      deleteStmt.executeUpdate();
    }
  }

  private static final String QUERY_REMOVE_ALL_MODERATORS =
      DELETE_FROM + RIGHTS_TABLE + WHERE + RIGHTS_COLUMN_FORUM_ID + " = ?";

  /**
   * Removes the role of moderator to all users on the forum corresponding to the primary key.
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @throws SQLException An SQL exception.
   */
  public static void removeAllModerators(Connection con, ForumPK forumPK) throws SQLException {
    try (PreparedStatement deleteStmt = con.prepareStatement(QUERY_REMOVE_ALL_MODERATORS)){
      deleteStmt.setString(1, forumPK.getId());
      deleteStmt.executeUpdate();
    }
  }

  private static final String QUERY_GET_MODERATORS =
      SELECT + RIGHTS_COLUMNS + FROM + RIGHTS_TABLE + WHERE + RIGHTS_COLUMN_FORUM_ID +
          " = ?";

  public static List<Moderator> getModerators(Connection con, int forumId) throws SQLException {
    List<Moderator> moderators = new ArrayList<>();
    try (PreparedStatement stmt = con.prepareStatement(QUERY_GET_MODERATORS)) {
      stmt.setString(1, Integer.toString(forumId));
      try(ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          moderators.add(Moderator.from(rs.getString(RIGHTS_COLUMN_USER_ID),
              Integer.valueOf(rs.getString(RIGHTS_COLUMN_FORUM_ID))));
        }
      }
    }
    return moderators;
  }

  private static final String QUERY_MOVE_MESSAGE =
      "UPDATE " + MESSAGE_TABLE + " SET " + MESSAGE_COLUMN_FORUM_ID + " = ?" + WHERE +
          MESSAGE_COLUMN_MESSAGE_ID + " = ?";

  /**
   * Moves the message corresponding to the message primary key from a previous forum to the one
   * corresponding to the forum primary key.
   * @param con The connection to the database.
   * @param messagePK The primary key of the message.
   * @param forumPK The primary key of the forum.
   * @throws SQLException An SQL exception.
   */
  public static void moveMessage(Connection con, MessagePK messagePK, ForumPK forumPK)
      throws SQLException {
    try (PreparedStatement updateStmt = con.prepareStatement(QUERY_MOVE_MESSAGE)) {
      updateStmt.setInt(1, Integer.parseInt(forumPK.getId()));
      updateStmt.setInt(2, Integer.parseInt(messagePK.getId()));
      updateStmt.executeUpdate();
    }
  }

  /**
   * @param con The connection to the database.
   * @param messagePK The primary key of the message.
   * @return The list of ids of messages which parent is the message corresponding to the primary
   * key.
   * @throws SQLException An SQL exception.
   */
  public static Collection<String> getAllMessageSons(Connection con, MessagePK messagePK)
      throws SQLException {
    Collection<String> messagesIds = new ArrayList<>();
    Collection<String> currentMessagesIds = getMessageSons(con, messagePK);
    for (String messageId : currentMessagesIds) {
      messagesIds.add(messageId);
      messagesIds
          .addAll(getAllMessageSons(con, new MessagePK(messagePK.getInstanceId(), messageId)));
    }
    return messagesIds;
  }

  /**
   * @param con The connection to the database.
   * @param forumPK The primary key of the forum.
   * @return The forum corresponding to the primary key (ForumDetail).
   * @throws SQLException An SQL exception.
   */
  public static ForumDetail getForumDetail(Connection con, ForumPK forumPK) throws SQLException {
    try (PreparedStatement stmt = con.prepareStatement(QUERY_GET_FORUM_DETAIL)) {
      stmt.setInt(1, Integer.parseInt(forumPK.getId()));
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return resultSet2ForumDetail(rs, forumPK);
        } else {
          throw new ForumsRuntimeException(failureOnGetting("forum", forumPK.getId()));
        }
      }
    }
  }

  private static final String QUERY_GET_LAST_VISIT =
      SELECT + HISTORY_COLUMN_LAST_ACCESS + FROM + HISTORY_TABLE + WHERE + HISTORY_COLUMN_USER_ID +
          " = ?" + AND + HISTORY_COLUMN_MESSAGE_ID + " = ?" + ORDER_BY +
          HISTORY_COLUMN_LAST_ACCESS + DESC;

  /**
   * @param con The connection to the database.
   * @param userId The user's id.
   * @param messageId The id of the message.
   * @return The last access date to the message corresponding to the message id.
   * @throws SQLException An SQL exception.
   */
  public static Date getLastVisit(Connection con, String userId, int messageId)
      throws SQLException {
    try (PreparedStatement selectStmt = con.prepareStatement(QUERY_GET_LAST_VISIT)) {
      selectStmt.setString(1, userId);
      selectStmt.setInt(2, messageId);
      try (ResultSet rs = selectStmt.executeQuery()) {
        if (rs.next()) {
          return new Date(Long.parseLong(rs.getString(HISTORY_COLUMN_LAST_ACCESS)));
        }
      }
    }
    return null;
  }

  /**
   * @param con The connection to the database.
   * @param userId The user's id.
   * @param messageIds The list of ids of the messages.
   * @return The last access date of the user to the messages corresponding to the list of primary
   * keys.
   * @throws SQLException An SQL exception.
   */
  public static Date getLastVisit(Connection con, String userId, List<String> messageIds)
      throws SQLException {
    StringBuilder selectQuery = new StringBuilder(
        SELECT + HISTORY_COLUMN_LAST_ACCESS + FROM + HISTORY_TABLE + WHERE +
            HISTORY_COLUMN_USER_ID + " = ?");

    int messageIdsCount = (messageIds != null ? messageIds.size() : 0);
    if (messageIdsCount > 0) {

      selectQuery.append(" AND (");
      for (int i = 0; i < messageIdsCount; i++) {
        if (i > 0) {
          selectQuery.append(" OR ");
        }
        selectQuery.append(HISTORY_COLUMN_MESSAGE_ID).append(" = ?");
      }
      selectQuery.append(") ORDER BY ").append(HISTORY_COLUMN_LAST_ACCESS).append(DESC);
    }


    Date lastVisit = null;
    try (PreparedStatement selectStmt = con.prepareStatement(selectQuery.toString())) {
      int index = 1;
      int messageId;
      selectStmt.setString(index++, userId);
      for (int i = 0; i < messageIdsCount; i++) {
        messageId = Integer.parseInt((String) messageIds.get(i));
        selectStmt.setInt(index++, messageId);
      }
      try (ResultSet rs = selectStmt.executeQuery()) {
        if (rs.next()) {
          lastVisit = new Date(Long.parseLong(rs.getString(1)));
        }
      }
    }

    return lastVisit;
  }

  private static final String QUERY_ADD_LAST_VISIT =
      INSERT_INTO + HISTORY_TABLE + " (" + HISTORY_COLUMNS + ")" + " VALUES (?, ?, ?)";

  /**
   * Adds an access date to the message corresponding to the message id by the user.
   * @param con The connection to the database.
   * @param userId The user's id.
   * @param messageId The id of the message.
   * @throws SQLException An SQL exception.
   */
  public static void addLastVisit(Connection con, String userId, int messageId)
      throws SQLException {
    // supprimer la ligne correspondante à ce user et ce message si elle existe
    deleteVisit(con, userId, messageId);

    Date date = new Date();
    try (PreparedStatement insertStmt = con.prepareStatement(QUERY_ADD_LAST_VISIT)) {
      insertStmt.setString(1, userId);
      insertStmt.setInt(2, messageId);
      insertStmt.setString(3, Long.toString(date.getTime()));
      insertStmt.executeUpdate();
    }
  }

  private static final String QUERY_DELETE_VISIT =
      DELETE_FROM + HISTORY_TABLE + WHERE + HISTORY_COLUMN_USER_ID + " = ?" + AND +
          HISTORY_COLUMN_MESSAGE_ID + " = ?";

  /**
   * Deletes the access date of the user to the message corresponding to the message id.
   * @param con The connection to the database.
   * @param userId The user's id.
   * @param messageId The id of the message.
   * @throws SQLException An SQL exception.
   */
  public static void deleteVisit(Connection con, String userId, int messageId) throws SQLException {
    try (PreparedStatement deleteStmt = con.prepareStatement(QUERY_DELETE_VISIT)) {
      deleteStmt.setString(1, userId);
      deleteStmt.setInt(2, messageId);
      deleteStmt.executeUpdate();
    }
  }

  /**
   * @param rs The database result set.
   * @param forumPK The primary key of the forum.
   * @return The forum corresponding to the primary key (ForumDetail).
   * @throws SQLException An SQL exception.
   */
  private static ForumDetail resultSet2ForumDetail(ResultSet rs, ForumPK forumPK)
      throws SQLException {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
    Date creationDate;
    try {
      creationDate = formatter.parse(rs.getString(FORUM_COLUMN_FORUM_CREATION_DATE));
    } catch (ParseException e) {
      throw new SQLException("ForumsDAO : resultSet2ForumDetail() : internal error : " +
          "creationDate format unknown for forumPK = " + forumPK + " : " + e.toString(), e);
    }

    String name = rs.getString(FORUM_COLUMN_FORUM_NAME);
    String description = rs.getString(FORUM_COLUMN_FORUM_DESCRIPTION);
    String creatorId = rs.getString(FORUM_COLUMN_FORUM_CREATOR);
    forumPK.setComponentName(rs.getString(FORUM_COLUMN_INSTANCE_ID));

    return new ForumDetail(forumPK, name, description, creatorId, creationDate);
  }

  /**
   * @param rs The database result set.
   * @return The forum corresponding to the primary key (Forum).
   * @throws SQLException An SQL exception.
   */
  private static Forum resultSet2Forum(ResultSet rs) throws SQLException {
    String category = rs.getString(FORUM_COLUMN_CATEGORY_ID);
    category = (category != null ? category.trim() : null);
    try {
      Date date = DateUtil.parseDate(rs.getString(FORUM_COLUMN_FORUM_CREATION_DATE));
      Forum forum = new Forum(rs.getInt(FORUM_COLUMN_FORUM_ID),
          rs.getString(FORUM_COLUMN_INSTANCE_ID),
          rs.getString(FORUM_COLUMN_FORUM_NAME).trim(),
          rs.getString(FORUM_COLUMN_FORUM_DESCRIPTION),
          (rs.getInt(FORUM_COLUMN_FORUM_ACTIVE) == 1),
          rs.getInt(FORUM_COLUMN_FORUM_PARENT),
          category);
      forum.setCreationDate(date);
      return forum;
    } catch (ParseException e) {
      throw new SQLException(e);
    }
  }

  /**
   * @param rs The database result set.
   * @return The message corresponding to the primary key (Vector).
   * @throws SQLException An SQL exception.
   */
  private static List resultSet2VectorMessage(ResultSet rs) throws SQLException {
    List message = new ArrayList();
    Timestamp timestamp = rs.getTimestamp(MESSAGE_COLUMN_MESSAGE_DATE);
    Date date = (timestamp != null ? new Date(timestamp.getTime()) : null);

    message.add(String.valueOf(rs.getInt(MESSAGE_COLUMN_MESSAGE_ID)));
    message.add(rs.getString(MESSAGE_COLUMN_MESSAGE_TITLE).trim());
    message.add(rs.getString(MESSAGE_COLUMN_MESSAGE_AUTHOR));
    message.add(date);
    message.add(String.valueOf(rs.getInt(MESSAGE_COLUMN_FORUM_ID)));
    message.add(String.valueOf(rs.getInt(MESSAGE_COLUMN_MESSAGE_PARENT_ID)));
    return message;
  }

  /**
   * @param rs The database result set.
   * @return The message corresponding to the primary key (Message).
   * @throws SQLException An SQL exception.
   */
  private static Message resultSet2Message(ResultSet rs, String instanceId) throws SQLException {
    Timestamp timestamp = rs.getTimestamp(MESSAGE_COLUMN_MESSAGE_DATE);
    Date date = (timestamp != null ? new Date(timestamp.getTime()) : null);
    Message message = new Message(rs.getInt(MESSAGE_COLUMN_MESSAGE_ID),
        instanceId,
        rs.getString(MESSAGE_COLUMN_MESSAGE_TITLE).trim(),
        rs.getString(MESSAGE_COLUMN_MESSAGE_AUTHOR),
        date,
        rs.getInt(MESSAGE_COLUMN_FORUM_ID),
        rs.getInt(MESSAGE_COLUMN_MESSAGE_PARENT_ID));
    message.setStatus(rs.getString(MESSAGE_COLUMN_STATUS));
    return message;
  }
}