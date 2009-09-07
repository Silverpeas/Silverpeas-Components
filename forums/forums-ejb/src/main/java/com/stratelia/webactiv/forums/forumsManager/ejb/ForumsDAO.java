package com.stratelia.webactiv.forums.forumsManager.ejb;

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
import java.util.Vector;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.forums.forumEntity.ejb.ForumDetail;
import com.stratelia.webactiv.forums.forumEntity.ejb.ForumPK;
import com.stratelia.webactiv.forums.forumsException.ForumsRuntimeException;
import com.stratelia.webactiv.forums.messageEntity.ejb.MessagePK;
import com.stratelia.webactiv.forums.models.Forum;
import com.stratelia.webactiv.forums.models.Message;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * Class managing database accesses for forums.
 */
public class ForumsDAO
{

	// Forums table.
	private static String FORUM_TABLE = 						"SC_Forums_Forum";
	private static String FORUM_COLUMN_FORUM_ID = 				"forumId";
	private static String FORUM_COLUMN_FORUM_NAME = 			"forumName";
	private static String FORUM_COLUMN_FORUM_DESCRIPTION = 		"forumDescription";
	private static String FORUM_COLUMN_FORUM_ACTIVE = 			"forumActive";
	private static String FORUM_COLUMN_FORUM_PARENT = 			"forumParent";
	private static String FORUM_COLUMN_CATEGORY_ID = 			"categoryId";
	private static String FORUM_COLUMN_INSTANCE_ID =			"instanceId";
	private static String FORUM_COLUMN_FORUM_CREATION_DATE =	"forumCreationDate";
	private static String FORUM_COLUMN_FORUM_CREATOR =			"forumCreator";
	private static String FORUM_COLUMN_FORUM_LOCK_LEVEL =		"forumLockLevel";
	private static String FORUM_COLUMN_FORUM_CLOSE_DATE =		"forumCloseDate";
	
	private static String FORUM_COLUMNS = 
		FORUM_COLUMN_FORUM_ID + ", "
		+ FORUM_COLUMN_FORUM_NAME + ", "
		+ FORUM_COLUMN_FORUM_DESCRIPTION + ", "
		+ FORUM_COLUMN_FORUM_ACTIVE + ", "
		+ FORUM_COLUMN_FORUM_PARENT + ", "
		+ FORUM_COLUMN_CATEGORY_ID + ", "
		+ FORUM_COLUMN_INSTANCE_ID + ", "
		+ FORUM_COLUMN_FORUM_CREATION_DATE + ", "
		+ FORUM_COLUMN_FORUM_CREATOR;
	
	// Messages table.
	private static String MESSAGE_TABLE = 						"SC_Forums_Message";
	private static String MESSAGE_COLUMN_MESSAGE_ID = 			"messageId";
	private static String MESSAGE_COLUMN_MESSAGE_TITLE = 		"messageTitle";
	private static String MESSAGE_COLUMN_MESSAGE_AUTHOR = 		"messageAuthor";
	private static String MESSAGE_COLUMN_FORUM_ID = 			"forumId";
	private static String MESSAGE_COLUMN_MESSAGE_PARENT_ID = 	"messageParentId";
	private static String MESSAGE_COLUMN_MESSAGE_DATE = 		"messageDate";
	private static String MESSAGE_COLUMNS = 
		MESSAGE_COLUMN_MESSAGE_ID + ", "
		+ MESSAGE_COLUMN_MESSAGE_TITLE + ", "
		+ MESSAGE_COLUMN_MESSAGE_AUTHOR + ", "
		+ MESSAGE_COLUMN_FORUM_ID + ", "
		+ MESSAGE_COLUMN_MESSAGE_PARENT_ID + ", "
		+ MESSAGE_COLUMN_MESSAGE_DATE;
	
	// Subscriptions table.
	private static String SUBSCRIPTION_TABLE = 					"SC_Forums_Subscription";
	private static String SUBSCRIPTION_COLUMN_USER_ID =			"userId";
	private static String SUBSCRIPTION_COLUMN_MESSAGE_ID = 		"messageId";
	private static String SUBSCRIPTION_COLUMNS =
		SUBSCRIPTION_COLUMN_USER_ID + ", "
		+ SUBSCRIPTION_COLUMN_MESSAGE_ID;
	
	// Rights table.
	private static String RIGHTS_TABLE = 						"SC_Forums_Rights";
	private static String RIGHTS_COLUMN_USER_ID =				"userId";
	private static String RIGHTS_COLUMN_FORUM_ID =				"forumId";
	private static String RIGHTS_COLUMNS =
		RIGHTS_COLUMN_USER_ID + ", "
		+ RIGHTS_COLUMN_FORUM_ID;
	
	// History table.
	private static String HISTORY_TABLE = 						"SC_Forums_HistoryUser";
	private static String HISTORY_COLUMN_USER_ID =				"userId";
	private static String HISTORY_COLUMN_MESSAGE_ID = 			"messageId";
	private static String HISTORY_COLUMN_LAST_ACCESS = 			"lastAccess";
	private static String HISTORY_COLUMNS =
		HISTORY_COLUMN_USER_ID + ", "
		+ HISTORY_COLUMN_MESSAGE_ID + ", "
		+ HISTORY_COLUMN_LAST_ACCESS;

	/**
	 * Private constructor to avoid instantiation since all methods of the class are static.
	 */
	private ForumsDAO()
	{
	}

	/**
	 * @param con The connection to the database.
	 * @param forumPKs The list of forums primary keys.
	 * @return The list of forums corresponding to the primary keys (ForumDetail).
	 * @throws SQLException An SQL exception.
	 */
	public static Collection selectByForumPKs(Connection con, Collection forumPKs)
		throws SQLException
	{
		ArrayList forumDetails = new ArrayList();
		Iterator iterator = forumPKs.iterator();
		ForumPK forumPK;
		while (iterator.hasNext())
		{
			forumPK = (ForumPK) iterator.next();
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
	public static Collection getForumsByKeys(Connection con, Collection forumPKs)
		throws SQLException
	{
		ArrayList forums = new ArrayList();
		Iterator iterator = forumPKs.iterator();
		ForumPK forumPK;
		Forum forum;
		while (iterator.hasNext())
		{
			forumPK = (ForumPK) iterator.next();
			forum = getForum(con, forumPK);
			if (forum != null)
			{
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
	public static Collection getMessagesByKeys(Connection con, Collection messagePKs)
		throws SQLException
	{
		return getMessagesByKeys(con, messagePKs, false);
	}
	
	/**
	 * @param con The connection to the database.
	 * @param messagePKs The list of messages primary keys.
	 * @return The list of threads corresponding to the primary keys (Message).
	 * @throws SQLException An SQL exception.
	 */
	public static Collection getThreadsByKeys(Connection con, Collection messagePKs)
		throws SQLException
	{
		return getMessagesByKeys(con, messagePKs, true);
	}
	
	/**
	 * @param con The connection to the database.
	 * @param messagePKs The list of messages primary keys.
	 * @param onlyThreads Indicates if only threads messages are searched.
	 * @return The list of messages (or only threads depending on onlyThreads) corresponding to the
	 *         primary keys (Message).
	 * @throws SQLException An SQL exception.
	 */
	private static Collection getMessagesByKeys(Connection con, Collection messagePKs,
			boolean onlyThreads)
		throws SQLException
	{
		ArrayList messages = new ArrayList();
		Iterator iterator = messagePKs.iterator();
		MessagePK messagePK;
		Message message;
		while (iterator.hasNext())
		{
			messagePK = (MessagePK) iterator.next();
			message = (onlyThreads ? getThread(con, messagePK) : getMessage(con, messagePK));
			if (message != null)
			{
				String instanceId = messagePK.getComponentName();
				if (instanceId != null && instanceId.length() > 0)
				{
					// Vérification que le message retourné fait partie d'un forum dont l'instanceid
					// correspond à celui de la clé du message.
					String forumInstanceId = getForumInstanceId(con, message.getForumId());
					if (instanceId.equals(forumInstanceId))
					{
						message.setInstanceId(instanceId);
						messages.add(message);
					}
				}
				else
				{
					// Ajout systématique si l'instanceid de la clé du message n'est pas renseignée.
					messages.add(message);
				}
			}
		}
		return messages;
	}
	
	private static final String QUERY_GET_FORUMS_LIST =
		"SELECT " + FORUM_COLUMNS
		+ " FROM " + FORUM_TABLE
		+ " WHERE " + FORUM_COLUMN_INSTANCE_ID + " = ?";

	/**
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @return The list of forums corresponding to the primary key (Forum).
	 * @throws SQLException An SQL exception.
	 */
	public static ArrayList getForumsList(Connection con, ForumPK forumPK)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getForumsList()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery = " + QUERY_GET_FORUMS_LIST);
		
		ArrayList forums = new ArrayList();
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_FORUMS_LIST);
			selectStmt.setString(1, forumPK.getComponentName());
			rs = selectStmt.executeQuery();
			while (rs.next())
			{
				forums.add(resultSet2Forum(rs));
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return forums;
	}
	
	private static final String QUERY_GET_FORUMS_IDS =
		"SELECT " + FORUM_COLUMN_FORUM_ID
		+ " FROM " + FORUM_TABLE
		+ " WHERE " + FORUM_COLUMN_INSTANCE_ID + " = ?";

	/**
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @return The list of ids of forums corresponding to the primary key.
	 * @throws SQLException An SQL exception.
	 */
	public static ArrayList getForumsIds(Connection con, ForumPK forumPK)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getForumsIds()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery = " + QUERY_GET_FORUMS_IDS);
		
		ArrayList forumsIds = new ArrayList();
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_FORUMS_IDS);
			selectStmt.setString(1, forumPK.getComponentName());
			rs = selectStmt.executeQuery();
			while (rs.next())
			{
				forumsIds.add(String.valueOf(rs.getInt(FORUM_COLUMN_FORUM_ID)));
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return forumsIds;
	}
	
	private static final String QUERY_GET_FORUMS_LIST_BY_CATEGORY_WITH_NOT_NULL_CATEGORY =
		"SELECT " + FORUM_COLUMNS
		+ " FROM " + FORUM_TABLE
		+ " WHERE " + FORUM_COLUMN_INSTANCE_ID + " = ?"
		+ " AND " + FORUM_COLUMN_CATEGORY_ID + " = ?";
	
	private static final String QUERY_GET_FORUMS_LIST_BY_CATEGORY_WITH_NULL_CATEGORY =
		"SELECT " + FORUM_COLUMNS
		+ " FROM " + FORUM_TABLE
		+ " WHERE " + FORUM_COLUMN_INSTANCE_ID + " = ?"
		+ " AND " + FORUM_COLUMN_CATEGORY_ID + " IS NULL";
	
	/**
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @param categoryId The id of the category.
	 * @return The list of forums corresponding to the primary key and the category id.
	 * @throws SQLException An SQL exception.
	 */
	public static ArrayList getForumsListByCategory(Connection con, ForumPK forumPK, String categoryId)
		throws SQLException
	{
		String selectQuery =(StringUtil.isDefined(categoryId)
			? QUERY_GET_FORUMS_LIST_BY_CATEGORY_WITH_NOT_NULL_CATEGORY
			: QUERY_GET_FORUMS_LIST_BY_CATEGORY_WITH_NULL_CATEGORY);
		SilverTrace.debug("forums", "ForumsDAO.getForumsListByCategory()",
			"root.MSG_GEN_PARAM_VALUE", "categoryId = " + categoryId);
		SilverTrace.info("forums", "ForumsDAO.getForumsListByCategory()",
			"root.MSG_GEN_PARAM_VALUE", "selectQuery = " + selectQuery);
		
		ArrayList forums = new ArrayList();
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(selectQuery);
			selectStmt.setString(1, forumPK.getComponentName());
			if (StringUtil.isDefined(categoryId))
			{
				selectStmt.setString(2, categoryId);
			}
			rs = selectStmt.executeQuery();
			while (rs.next())
			{
				forums.add(resultSet2Forum(rs));
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return forums;
	}
	
	private static final String QUERY_GET_FORUM_SONS =
		"SELECT " + FORUM_COLUMN_FORUM_ID + " FROM " + FORUM_TABLE
		+ " WHERE " + FORUM_COLUMN_FORUM_PARENT + " = ?"
		+ " AND " + FORUM_COLUMN_INSTANCE_ID + " = ?";

	/**
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @return The list of ids of forums which parent is the forum corresponding to the primary key.
	 * @throws SQLException An SQL exception.
	 */
	public static ArrayList getForumSonsIds(Connection con, ForumPK forumPK)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getForumSonsIds()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery = " + QUERY_GET_FORUM_SONS);
		
		ArrayList forumIds = new ArrayList();
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_FORUM_SONS);
			selectStmt.setInt(1, Integer.parseInt(forumPK.getId()));
			selectStmt.setString(2, forumPK.getComponentName());
			rs = selectStmt.executeQuery();
			while (rs.next())
			{
				forumIds.add(String.valueOf(rs.getInt(FORUM_COLUMN_FORUM_ID)));
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return forumIds;
	}
	
	private static final String QUERY_GET_FORUM =
		"SELECT " + FORUM_COLUMNS
		+ " FROM " + FORUM_TABLE
		+ " WHERE " + FORUM_COLUMN_FORUM_ID + " = ?"
		+ " AND " + FORUM_COLUMN_INSTANCE_ID + " = ?";
	
	/**
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @return The forum corresponding to the primary key (Forum).
	 * @throws SQLException An SQL exception.
	 */
	public static Forum getForum(Connection con, ForumPK forumPK)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getForum()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery = " + QUERY_GET_FORUM);
		
		int forumId = Integer.parseInt(forumPK.getId());
		String instanceId = forumPK.getComponentName();
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_FORUM);
			selectStmt.setInt(1, forumId);
			selectStmt.setString(2, instanceId);
			rs = selectStmt.executeQuery();
			if (rs.next())
			{
				Forum forum = resultSet2Forum(rs);
				forum.setPk(forumPK);
				return forum;
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return null;
	}
	
	private static final String QUERY_GET_FORUM_NAME =
		"SELECT " + FORUM_COLUMN_FORUM_NAME
		+ " FROM " + FORUM_TABLE
		+ " WHERE " + FORUM_COLUMN_FORUM_ID + " = ?";
	
	/**
	 * @param con The connection to the database.
	 * @param forumId The id of the forum.
	 * @return The name corresponding to the forum id.
	 * @throws SQLException An SQL exception.
	 */
	public static String getForumName(Connection con, int forumId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getForumName()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery = " + QUERY_GET_FORUM_NAME);
		
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_FORUM_NAME);
			selectStmt.setInt(1, forumId);
			rs = selectStmt.executeQuery();
			if (rs.next())
			{
				return rs.getString(FORUM_COLUMN_FORUM_NAME);
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return null;
	}
	
	private static final String QUERY_IS_FORUM_ACTIVE =
		"SELECT " + FORUM_COLUMN_FORUM_ACTIVE
		+ " FROM " + FORUM_TABLE
		+ " WHERE " + FORUM_COLUMN_FORUM_ID + " = ?";
	
	/**
	 * @param con The connection to the database.
	 * @param forumId The id of the forum.
	 * @return True if the forum is active.
	 * @throws SQLException An SQL exception.
	 */
	public static boolean isForumActive(Connection con, int forumId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.isForumActive()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery = " + QUERY_IS_FORUM_ACTIVE);
		
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_IS_FORUM_ACTIVE);
			selectStmt.setInt(1, forumId);
			rs = selectStmt.executeQuery();
			if (rs.next())
			{
				return (rs.getInt(FORUM_COLUMN_FORUM_ACTIVE) == 1);
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return false;
	}
	
	private static final String QUERY_GET_FORUM_PARENT_ID =
		"SELECT " + FORUM_COLUMN_FORUM_PARENT
		+ " FROM " + FORUM_TABLE
		+ " WHERE " + FORUM_COLUMN_FORUM_ID + " = ?";
	
	/**
	 * @param con The connection to the database.
	 * @param forumId The id of the forum.
	 * @return The id of the parent of the forum.
	 * @throws SQLException An SQL exception.
	 */
	public static int getForumParentId(Connection con, int forumId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getForumParentId()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery = " + QUERY_GET_FORUM_PARENT_ID);
		
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_FORUM_PARENT_ID);
			selectStmt.setInt(1, forumId);
			rs = selectStmt.executeQuery();
			if (rs.next())
			{
				return rs.getInt(FORUM_COLUMN_FORUM_PARENT);
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return -1;
	}
	
	private static final String QUERY_GET_FORUM_INSTANCE_ID =
		"SELECT " + FORUM_COLUMN_INSTANCE_ID
		+ " FROM " + FORUM_TABLE
		+ " WHERE " + FORUM_COLUMN_FORUM_ID + " = ?";
	
	/**
	 * @param con The connection to the database.
	 * @param forumId The id of the forum.
	 * @return The instance id corresponding to the forum id.
	 * @throws SQLException An SQL exception.
	 */
	public static String getForumInstanceId(Connection con, int forumId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getForumInstanceId()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery = " + QUERY_GET_FORUM_INSTANCE_ID);
		
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_FORUM_INSTANCE_ID);
			selectStmt.setInt(1, forumId);
			rs = selectStmt.executeQuery();
			if (rs.next())
			{
				return rs.getString(FORUM_COLUMN_INSTANCE_ID);
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return null;
	}
	
	private static final String QUERY_GET_FORUM_CREATOR_ID =
		"SELECT " + FORUM_COLUMN_FORUM_CREATOR
		+ " FROM " + FORUM_TABLE
		+ " WHERE " + FORUM_COLUMN_FORUM_ID + " = ?";

	/**
	 * @param con The connection to the database.
	 * @param forumId The id of the forum.
	 * @return The id of the creator of the forum.
	 * @throws SQLException An SQL exception.
	 */
	public static String getForumCreatorId(Connection con, int forumId)
		throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = con.prepareStatement(QUERY_GET_FORUM_CREATOR_ID);
			stmt.setInt(1, forumId);
			rs = stmt.executeQuery();
			if (rs.next())
			{
				return rs.getString(FORUM_COLUMN_FORUM_CREATOR);
			}
			else
			{
				throw new ForumsRuntimeException("ForumsDAO.getForumCreatorId()",
					SilverpeasRuntimeException.ERROR, "root.EX_CANT_LOAD_ENTITY_ATTRIBUTES",
					"ForumId = " + forumId + " not found in database !");
			}
		}
		finally
		{
			DBUtil.close(rs, stmt);
		}
	}
	
	private static final String QUERY_LOCK_FORUM =
		"UPDATE " + FORUM_TABLE
		+ " SET " + FORUM_COLUMN_FORUM_LOCK_LEVEL + " = ?, "
		+ FORUM_COLUMN_FORUM_ACTIVE + " = ?, "
		+ FORUM_COLUMN_FORUM_CLOSE_DATE + " = ?,"
		+ FORUM_COLUMN_INSTANCE_ID + " = ?"
		+ " WHERE " + FORUM_COLUMN_FORUM_ID + " = ?";

	/**
	 * Locks the forum corresponding to the primary key.
	 * 
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @param level The lock level.
	 * @throws SQLException An SQL exception.
	 */
	public static void lockForum(Connection con, ForumPK forumPK, int level)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.lockForum()", "root.MSG_GEN_PARAM_VALUE",
			"updateQuery = " + QUERY_LOCK_FORUM);
		
		PreparedStatement updateStmt = null;
		try
		{
			updateStmt = con.prepareStatement(QUERY_LOCK_FORUM);
			updateStmt.setInt(1, level);
			updateStmt.setInt(2, 0);
			updateStmt.setString(3, DateUtil.date2SQLDate(new Date()));
			updateStmt.setString(4, forumPK.getComponentName());
			updateStmt.setInt(5, Integer.parseInt(forumPK.getId()));
			updateStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(updateStmt);
		}
	}
	
	private static final String QUERY_UNLOCK_FORUM_GET_LEVEL =
		"SELECT " + FORUM_COLUMN_FORUM_LOCK_LEVEL
		+ " FROM " + FORUM_TABLE
		+ " WHERE " + FORUM_COLUMN_FORUM_ID + " = ?";
	
	private static final String QUERY_UNLOCK_FORUM_SET_ACTIVE =
		"UPDATE " + FORUM_TABLE
		+ " SET " + FORUM_COLUMN_FORUM_ACTIVE + " = ?"
		+ " WHERE " + FORUM_COLUMN_FORUM_ID + " = ?";
	
	/**
	 * Unlocks the forum corresponding to the primary key.
	 * 
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @param level The lock level.
	 * @return
	 * @throws SQLException An SQL exception.
	 */
	public static int unlockForum(Connection con, ForumPK forumPK, int level)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.unlockForum()", "root.MSG_GEN_PARAM_VALUE",
			"Query1 = " + QUERY_UNLOCK_FORUM_GET_LEVEL);
		
		int result = 0;
		int forumLocklevel = 0;
		PreparedStatement declareStmt = null;
		ResultSet rs = null;
		try
		{
			declareStmt = con.prepareStatement(QUERY_UNLOCK_FORUM_GET_LEVEL);
			declareStmt.setInt(1, Integer.parseInt(forumPK.getId()));
			rs = declareStmt.executeQuery();
			if (rs.next())
			{
				forumLocklevel = rs.getInt(FORUM_COLUMN_FORUM_LOCK_LEVEL);
			}
			
			if (forumLocklevel >= level)
			{
				SilverTrace.info("forums", "ForumsDAO.unlockForum()", "root.MSG_GEN_PARAM_VALUE",
					"Query2 = " + QUERY_UNLOCK_FORUM_SET_ACTIVE);
				
				declareStmt = con.prepareStatement(QUERY_UNLOCK_FORUM_SET_ACTIVE);
				declareStmt.setInt(1, 1);
				declareStmt.setInt(2, Integer.parseInt(forumPK.getId()));
				declareStmt.executeUpdate();
				result = 1;
			}
			else
			{
				result = 0;
			}
		}
		finally
		{
			DBUtil.close(rs, declareStmt);
		}
		return result;
	}
	
	private static final String QUERY_CREATE_FORUM =
		"INSERT INTO " + FORUM_TABLE
		+ " (" + FORUM_COLUMNS + ")"
		+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

	/**
	 * Creates a forum.
	 * 
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @param forumName The name of the forum.
	 * @param forumDescription The description of the forum.
	 * @param forumCreator The creator of the forum.
	 * @param forumParent The id of the forum's parent forum.
	 * @param categoryId The id of the category.
	 * @return The id of the newly created forum.
	 * @throws SQLException An SQL exception.
	 * @throws UtilException
	 */
	public static int createForum(Connection con, ForumPK forumPK, String forumName,
			String forumDescription, String forumCreator, int forumParent, String categoryId)
		throws SQLException, UtilException
	{
		SilverTrace.info("forums", "ForumsDAO.createForum()", "root.MSG_GEN_PARAM_VALUE",
			"insertQuery = " + QUERY_CREATE_FORUM);
		
		PreparedStatement insertStmt = null;
		try
		{
			int forumId = DBUtil.getNextId(FORUM_TABLE, FORUM_COLUMN_FORUM_ID);

			insertStmt = con.prepareStatement(QUERY_CREATE_FORUM);
			insertStmt.setInt(1, forumId);
			insertStmt.setString(2, forumName);
			insertStmt.setString(3, forumDescription);
			insertStmt.setInt(4, 1);
			insertStmt.setInt(5, forumParent);
			if (StringUtil.isDefined(categoryId))
			{
				insertStmt.setString(6, categoryId);
			}
			else
			{
				insertStmt.setNull(6, Types.VARCHAR);
			}
			insertStmt.setString(7, forumPK.getComponentName());
			insertStmt.setString(8, DateUtil.date2SQLDate(new Date()));
			insertStmt.setString(9, forumCreator);
			insertStmt.executeUpdate();

			return forumId;
		}
		finally
		{
			DBUtil.close(insertStmt);
		}
	}
	
	private static final String QUERY_UPDATE_FORUM =
		"UPDATE " + FORUM_TABLE
		+ " SET " + FORUM_COLUMN_FORUM_NAME + " = ?, "
		+ FORUM_COLUMN_FORUM_DESCRIPTION + " = ?, "
		+ FORUM_COLUMN_FORUM_PARENT + " = ?, "
		+ FORUM_COLUMN_INSTANCE_ID + " = ?, "
		+ FORUM_COLUMN_CATEGORY_ID + " = ?"
		+ " WHERE " + FORUM_COLUMN_FORUM_ID + " = ? ";

	/**
	 * Updates the forum corresponding to the primary key.
	 * 
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @param forumName The name of the forum.
	 * @param forumDescription The description of the forum.
	 * @param forumParent The id of the forum's parent forum.
	 * @param categoryId The id of the category.
	 * @throws SQLException An SQL exception.
	 */
	public static void updateForum(Connection con, ForumPK forumPK,  String forumName,
			String forumDescription, int forumParent, String categoryId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.updateForum()", "root.MSG_GEN_PARAM_VALUE",
			"updateQuery = " + QUERY_UPDATE_FORUM);
		
		PreparedStatement updateStmt = null;
		try
		{
			updateStmt = con.prepareStatement(QUERY_UPDATE_FORUM);
			updateStmt.setString(1, forumName);
			updateStmt.setString(2, forumDescription);
			updateStmt.setInt(3, forumParent);
			updateStmt.setString(4, forumPK.getComponentName());
			if (StringUtil.isDefined(categoryId))
			{
				updateStmt.setString(5, categoryId);
			}
			else
			{
				updateStmt.setNull(5, Types.VARCHAR);
			}
			updateStmt.setInt(6, Integer.parseInt(forumPK.getId()));
			updateStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(updateStmt);
		}
	}
	
	private static final String QUERY_DELETE_FORUM_SUBSCRIPTION =
		"DELETE FROM " + SUBSCRIPTION_TABLE
		+ " WHERE " + SUBSCRIPTION_COLUMN_MESSAGE_ID
		+ " IN (SELECT DISTINCT CAST(" + MESSAGE_COLUMN_MESSAGE_ID + " AS VARCHAR(255))"
			+ " FROM " + MESSAGE_TABLE
			+ " WHERE " + MESSAGE_COLUMN_FORUM_ID + " = ?)";
	
	private static final String QUERY_DELETE_FORUM_RIGHTS =
		"DELETE FROM " + RIGHTS_TABLE
		+ " WHERE " + RIGHTS_COLUMN_FORUM_ID + " = ?";
	
	private static final String QUERY_DELETE_FORUM_MESSAGE =
		"DELETE FROM " + MESSAGE_TABLE
		+ " WHERE " + MESSAGE_COLUMN_FORUM_ID + " = ?";
	
	private static final String QUERY_DELETE_FORUM_FORUM =
		"DELETE FROM " + FORUM_TABLE
		+ " WHERE " + FORUM_COLUMN_FORUM_ID + " = ?";

	/**
	 * Deletes the forum corresponding to the primary key.
	 * 
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @throws SQLException An SQL exception.
	 */
	public static void deleteForum(Connection con, ForumPK forumPK)
		throws SQLException
	{
		String sForumId = forumPK.getId();
		int forumId = Integer.parseInt(sForumId);
		PreparedStatement deleteStmt = null;
		try
		{
			SilverTrace.info("forums", "ForumsDAO.deleteForum()", "root.MSG_GEN_PARAM_VALUE",
				"deleteQuery  = " + QUERY_DELETE_FORUM_SUBSCRIPTION);
			deleteStmt = con.prepareStatement(QUERY_DELETE_FORUM_SUBSCRIPTION);
			deleteStmt.setInt(1, forumId);
			deleteStmt.executeUpdate();

			deleteStmt = con.prepareStatement(QUERY_DELETE_FORUM_RIGHTS);
			deleteStmt.setString(1, sForumId);
			deleteStmt.executeUpdate();

			deleteStmt = con.prepareStatement(QUERY_DELETE_FORUM_MESSAGE);
			deleteStmt.setInt(1, forumId);
			deleteStmt.executeUpdate();

			deleteStmt = con.prepareStatement(QUERY_DELETE_FORUM_FORUM);
			deleteStmt.setInt(1, forumId);
			deleteStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(deleteStmt);
		}
	}
	
	private static final String QUERY_GET_MESSAGES_LIST_BY_FORUM =
		"SELECT " + MESSAGE_COLUMNS
		+ " FROM " + MESSAGE_TABLE
		+ " WHERE " + MESSAGE_COLUMN_FORUM_ID + " = ?";

	/**
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @return The list of messages of the forum corresponding to the primary key (Message).
	 * @throws SQLException An SQL exception.
	 */
	public static ArrayList getMessagesList(Connection con, ForumPK forumPK)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getMessagesList()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + QUERY_GET_MESSAGES_LIST_BY_FORUM);
		
		ArrayList messages = new ArrayList();
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_MESSAGES_LIST_BY_FORUM);
			selectStmt.setInt(1, Integer.parseInt(forumPK.getId()));
			rs = selectStmt.executeQuery();
			while (rs.next())
			{
				messages.add(resultSet2Message(rs));
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return messages;
	}
	
	private static final String QUERY_GET_MESSAGES_IDS_BY_FORUM =
		"SELECT " + MESSAGE_COLUMN_MESSAGE_ID
		+ " FROM " + MESSAGE_TABLE
		+ " WHERE " + MESSAGE_COLUMN_FORUM_ID + " = ?";
	
	private static final String QUERY_GET_MESSAGES_IDS_BY_FORUM_AND_MESSAGE =
		"SELECT " + MESSAGE_COLUMN_MESSAGE_ID
		+ " FROM " + MESSAGE_TABLE
		+ " WHERE " + MESSAGE_COLUMN_FORUM_ID + " = ?"
		+ " AND " + MESSAGE_COLUMN_MESSAGE_PARENT_ID + " = ?";
	
	/**
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @param messageParentId The id of the message's parent message.
	 * @return The list of ids of messages of the forum corresponding to the primary key and which
	 *         parent message corresponds to the message id (if it is valued).
	 * @throws SQLException An SQL exception.
	 */
	public static ArrayList getMessagesIds(Connection con, ForumPK forumPK, int messageParentId)
		throws SQLException
	{
		String query = (messageParentId != -1
			? QUERY_GET_MESSAGES_IDS_BY_FORUM_AND_MESSAGE : QUERY_GET_MESSAGES_IDS_BY_FORUM);
		SilverTrace.info("forums", "ForumsDAO.getMessagesIds()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + query);
		
		ArrayList messagesIds = new ArrayList();
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(query);
			selectStmt.setInt(1, Integer.parseInt(forumPK.getId()));
			if (messageParentId != -1)
			{
				selectStmt.setInt(2, messageParentId);
			}
			rs = selectStmt.executeQuery();
			while (rs.next())
			{
				messagesIds.add(String.valueOf(rs.getInt(MESSAGE_COLUMN_MESSAGE_ID)));
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return messagesIds;
	}
	
	/**
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @return The list of ids of messages of the forum corresponding to the primary key.
	 * @throws SQLException An SQL exception.
	 */
	public static ArrayList getMessagesIds(Connection con, ForumPK forumPK)
		throws SQLException
	{
		return getMessagesIds(con, forumPK, -1);
	}
	
	/**
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @return The list of ids of threads of the forum corresponding to the primary key.
	 * @throws SQLException An SQL exception.
	 */
	public static ArrayList getSubjectsIds(Connection con, ForumPK forumPK)
		throws SQLException
	{
		return getMessagesIds(con, forumPK, 0);
	}
	
	private static final String QUERY_GET_NB_MESSAGES_SUBJECTS =
		"SELECT COUNT(" + MESSAGE_COLUMN_MESSAGE_ID + ") FROM " + MESSAGE_TABLE
		+ " WHERE " + MESSAGE_COLUMN_FORUM_ID + " = ?"
		+ " AND " + MESSAGE_COLUMN_MESSAGE_PARENT_ID + " = 0";
	
	private static final String QUERY_GET_NB_MESSAGES_NOT_SUBJECTS =
		"SELECT COUNT(" + MESSAGE_COLUMN_MESSAGE_ID + ") FROM " + MESSAGE_TABLE
		+ " WHERE " + MESSAGE_COLUMN_FORUM_ID + " = ?"
		+ " AND " + MESSAGE_COLUMN_MESSAGE_PARENT_ID + " != 0";
	
	/**
	 * @param con The connection to the database.
	 * @param forumId The id of the forum.
	 * @param type The type of the searched messages.
	 * @return The number of messages corresponding to the forum id and the type (threads or not).
	 * @throws SQLException An SQL exception.
	 */
	public static int getNbMessages(Connection con, int forumId, String type)
		throws SQLException
	{
		String selectQuery = (type.equals("Subjects")
			? QUERY_GET_NB_MESSAGES_SUBJECTS : QUERY_GET_NB_MESSAGES_NOT_SUBJECTS);
		SilverTrace.info("forums", "ForumsDAO.getNbSubjects()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + selectQuery + " ; forumId = " + forumId);
		
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try
		{
			prepStmt = con.prepareStatement(selectQuery);
			prepStmt.setInt(1, forumId);
			rs = prepStmt.executeQuery();
			if (rs.next())
			{
				return rs.getInt(1);
			}
		}
		finally
		{
			DBUtil.close(rs, prepStmt);
		}
		return 0;
	}
	
	private static final String QUERY_GET_AUTHOR_NB_MESSAGES =
		"SELECT COUNT(" + MESSAGE_COLUMN_MESSAGE_ID + ") FROM " + MESSAGE_TABLE
		+ " WHERE " + MESSAGE_COLUMN_MESSAGE_AUTHOR + " = ?";
	
	/**
	 * @param con The connection to the database.
	 * @param userId The user's id.
	 * @return The number of messages written by the author corresponding to the user id.
	 * @throws SQLException An SQL exception.
	 */
	public static int getAuthorNbMessages(Connection con, String userId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getAuthorNbMessages()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + QUERY_GET_AUTHOR_NB_MESSAGES + " ; messageAuthor = " + userId);
		
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try
		{
			prepStmt = con.prepareStatement(QUERY_GET_AUTHOR_NB_MESSAGES);
			prepStmt.setString(1, userId);
			rs = prepStmt.executeQuery();
			if (rs.next())
			{
				return rs.getInt(1);
			}
		}
		finally
		{
			DBUtil.close(rs, prepStmt);
		}
		return 0;
	}
	
	private static final String QUERY_GET_NB_RESPONSES =
		"SELECT " + MESSAGE_COLUMN_MESSAGE_ID + " FROM " + MESSAGE_TABLE
		+ " WHERE " + MESSAGE_COLUMN_FORUM_ID + " = ?"
		+ " AND " + MESSAGE_COLUMN_MESSAGE_PARENT_ID + " = ?";
	
	/**
	 * @param con The connection to the database.
	 * @param forumId The id of the forum.
	 * @param messageId The id of the message.
	 * @return The number of responses to the message corresponding to the message id and the forum
	 *         id.
	 */
	public static int getNbResponses(Connection con, int forumId, int messageId)
	{
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		ArrayList nextMessageIds = new ArrayList();
		try
		{
			prepStmt = con.prepareStatement(QUERY_GET_NB_RESPONSES);
			prepStmt.setInt(1, forumId);
			prepStmt.setInt(2, messageId);
			rs = prepStmt.executeQuery();
			while (rs.next())
			{
				nextMessageIds.add(new Integer(rs.getInt(MESSAGE_COLUMN_MESSAGE_ID)));
			}
		}
		catch (SQLException sqle)
		{
			return 0;
		}
		finally
		{
			DBUtil.close(rs, prepStmt);
		}
		int nb = nextMessageIds.size();
		int nextMessageId;
		for (int i = 0, n = nextMessageIds.size(); i < n; i++)
		{
			nextMessageId = ((Integer)nextMessageIds.get(i)).intValue();
			nb += getNbResponses(con, forumId, nextMessageId);
		}
		return nb;
	}
	
	private static final String QUERY_GET_LAST_MESSAGE =
		"SELECT " + MESSAGE_COLUMN_MESSAGE_ID
		+ " FROM " + MESSAGE_TABLE
		+ " WHERE " + MESSAGE_COLUMN_FORUM_ID + " = ?"
		+ " AND " + MESSAGE_COLUMN_MESSAGE_PARENT_ID + " != 0"
		+ " ORDER BY " + MESSAGE_COLUMN_MESSAGE_DATE + " DESC, "
			+ MESSAGE_COLUMN_MESSAGE_ID + " DESC";
	
	/**
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @return The last message of the forum corresponding to the forum id.
	 * @throws SQLException An SQL exception.
	 */
	public static Message getLastMessage(Connection con, ForumPK forumPK)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getLastMessage()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + QUERY_GET_LAST_MESSAGE);
		
		int messageId = -1;
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_LAST_MESSAGE);
			selectStmt.setInt(1, Integer.parseInt(forumPK.getId()));
			rs = selectStmt.executeQuery();
			if (rs.next())
			{
				messageId = rs.getInt(1);
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		if (messageId != -1)
		{
			MessagePK messagePK = new MessagePK(
				forumPK.getComponentName(), forumPK.getDomain(), String.valueOf(messageId));
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
	public static ArrayList getLastThreads(Connection con, ForumPK[] forumPKs, int count)
		throws SQLException
	{
		ArrayList messages = new ArrayList();
		if (forumPKs.length > 0)
		{
			String selectQuery = "SELECT " + MESSAGE_COLUMN_MESSAGE_ID
				+ " FROM " + MESSAGE_TABLE
				+ " WHERE " + MESSAGE_COLUMN_MESSAGE_PARENT_ID + " = ?"
				+ " AND " + MESSAGE_COLUMN_FORUM_ID + " IN(";
			for (int i = 0, n = forumPKs.length; i < n; i++){
				if (i > 0)
				{
					selectQuery += ", ";
				}
				selectQuery += forumPKs[i].getId();
			}
			selectQuery += ") ORDER BY " + MESSAGE_COLUMN_MESSAGE_DATE + " DESC";
			SilverTrace.info("forums", "ForumsDAO.getLastTheads()", "root.MSG_GEN_PARAM_VALUE",
				"selectQuery  = " + selectQuery);
			
			ArrayList messageIds = new ArrayList(count);
			int messagesCount = 0;
			PreparedStatement selectStmt = null;
			ResultSet rs = null;
			try
			{
				selectStmt = con.prepareStatement(selectQuery);
				selectStmt.setInt(1, 0);
				rs = selectStmt.executeQuery();
				while (rs.next() && messagesCount < count)
				{
					messageIds.add(String.valueOf(rs.getInt(MESSAGE_COLUMN_MESSAGE_ID)));
					messagesCount++;
				}
			}
			finally
			{
				DBUtil.close(rs, selectStmt);
			}
			
			String componentName = forumPKs[0].getComponentName();
			for (int i = 0; i < messagesCount; i++)
			{
				MessagePK messagePK = new MessagePK(componentName, "", (String)messageIds.get(i));
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
	 *         keys.
	 * @throws SQLException An SQL exception.
	 */
	public static Collection getNotAnsweredLastThreads(Connection con, ForumPK[] forumPKs,
			int count)
		throws SQLException
	{
		ArrayList messages = new ArrayList();
		if (forumPKs.length > 0)
		{
			String selectQuery = "SELECT " + MESSAGE_COLUMN_MESSAGE_ID + ", "
					+ MESSAGE_COLUMN_FORUM_ID
				+ " FROM " + MESSAGE_TABLE
				+ " WHERE " + MESSAGE_COLUMN_MESSAGE_PARENT_ID + " = ?"
				+ " AND " + MESSAGE_COLUMN_FORUM_ID + " IN(";
			for (int i = 0, n = forumPKs.length; i < n; i++){
				if (i > 0)
				{
					selectQuery += ", ";
				}
				selectQuery += forumPKs[i].getId();
			}
			selectQuery += ") ORDER BY " + MESSAGE_COLUMN_MESSAGE_DATE + " DESC";
			SilverTrace.info("forums", "ForumsDAO.getLastTheads()", "root.MSG_GEN_PARAM_VALUE",
				"selectQuery  = " + selectQuery);
			
			ArrayList messageIds = new ArrayList(count);
			int messageId;
			int forumId;
			int messagesCount = 0;
			PreparedStatement selectStmt = null;
			ResultSet rs = null;
			
			String query = "SELECT COUNT(" + MESSAGE_COLUMN_MESSAGE_ID + ") FROM " + MESSAGE_TABLE
				+ " WHERE " + MESSAGE_COLUMN_FORUM_ID + " = ?"
				+ " AND " + MESSAGE_COLUMN_MESSAGE_PARENT_ID + " = ?";
			PreparedStatement prepStmt = null;
			ResultSet rs2 = null;
			int sonsCount;
			try
			{
				selectStmt = con.prepareStatement(selectQuery);
				selectStmt.setInt(1, 0);
				rs = selectStmt.executeQuery();
				while (rs.next() && messagesCount < count)
				{
					messageId = rs.getInt(MESSAGE_COLUMN_MESSAGE_ID);
					forumId = rs.getInt(MESSAGE_COLUMN_FORUM_ID);
					
					prepStmt = con.prepareStatement(query);
					prepStmt.setInt(1, forumId);
					prepStmt.setInt(2, messageId);
					rs2 = prepStmt.executeQuery();
					if (rs2.next())
					{
						sonsCount = rs2.getInt(1);
						if (sonsCount == 0)
						{
							messageIds.add(String.valueOf(messageId));
							messagesCount++;
						}
					}
				}
			}
			finally
			{
				DBUtil.close(rs, selectStmt);
				DBUtil.close(rs2, prepStmt);
			}
			
			String componentName = forumPKs[0].getComponentName();
			for (int i = 0; i < messagesCount; i++)
			{
				MessagePK messagePK = new MessagePK(componentName, "", (String)messageIds.get(i));
				messages.add(getMessage(con, messagePK));
			}
		}
		return messages;
	}
	
	/**
	 * @param con The connection to the database.
	 * @param instanceId The id of the forums instance.
	 * @return The list of ids of messages from the forums corresponding to the instance id.
	 * @throws SQLException An SQL exception.
	 */
	public static Collection getLastMessageRSS(Connection con, String instanceId)
		throws SQLException
	{
		Collection messageIds = new ArrayList();
		Collection forumIds = getAllForumsByInstanceId(con, instanceId);
		Iterator it = forumIds.iterator();
		int forumId;
		while (it.hasNext())
		{
			forumId = ((Integer) it.next()).intValue();
			messageIds.addAll(getAllMessageByForum(con, forumId));
		}
		return messageIds;
	}
	
	private static final String QUERY_GET_ALL_FORUMS_BY_INSTANCE_ID =
		"SELECT " + FORUM_COLUMN_FORUM_ID
		+ " FROM " + FORUM_TABLE
		+ " WHERE " + FORUM_COLUMN_INSTANCE_ID + " = ?";
	
	/**
	 * @param con The connection to the database.
	 * @param instanceId The id of the forums instance.
	 * @return The list of ids of forums corresponding to the instance id.
	 * @throws SQLException An SQL exception.
	 */
	private static Collection getAllForumsByInstanceId(Connection con, String instanceId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getAllForumsByInstanceId()",
			"root.MSG_GEN_PARAM_VALUE", "selectQuery  = " + QUERY_GET_ALL_FORUMS_BY_INSTANCE_ID);
		
		Collection forumIds = new ArrayList();
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_ALL_FORUMS_BY_INSTANCE_ID);
			selectStmt.setString(1, instanceId);
			rs = selectStmt.executeQuery();
			while (rs.next())
			{
				forumIds.add(new Integer(rs.getInt(1)));   
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return forumIds;
	}
	
	private static final String QUERY_GET_ALL_MESSAGES_BY_FORUM =
		"SELECT " + MESSAGE_COLUMN_MESSAGE_ID
		+ " FROM " + MESSAGE_TABLE
		+ " WHERE " + MESSAGE_COLUMN_FORUM_ID + " = ?"
		+ " ORDER BY " + MESSAGE_COLUMN_MESSAGE_DATE + " DESC, "
			+ MESSAGE_COLUMN_MESSAGE_ID + " DESC";
	
	/**
	 * @param con The connection to the database.
	 * @param forumId The id of the forum.
	 * @return The list of ids of messages from the forum corresponding to the forum id.
	 * @throws SQLException An SQL exception.
	 */
	private static Collection getAllMessageByForum(Connection con, int forumId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getAllMessageByForum()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + QUERY_GET_ALL_MESSAGES_BY_FORUM);
		
		Collection messageIds = new ArrayList();
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_ALL_MESSAGES_BY_FORUM);
			selectStmt.setInt(1, forumId);
			rs = selectStmt.executeQuery();

			while (rs.next())
			{
				messageIds.add(String.valueOf(rs.getInt(1)));
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return messageIds;
	}
	
	/**
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @param messageParentIds The ids of the parent messages.
	 * @return The last message from the forum corresponding to the primary key among the messages
	 *         which id or parent message id belong to the list.
	 * @throws SQLException An SQL exception.
	 */
	public static Message getLastMessage(Connection con, ForumPK forumPK, List messageParentIds)
		throws SQLException
	{
		String selectQuery = "SELECT " + MESSAGE_COLUMN_MESSAGE_ID + " FROM " + MESSAGE_TABLE;
		
		int messageParentIdsCount = (messageParentIds != null ? messageParentIds.size() : 0);
		if (messageParentIdsCount > 0)
		{
			SilverTrace.info("forums", "ForumsDAO.getLastMessage()", "root.MSG_GEN_PARAM_VALUE",
				"messageParentIds = " + messageParentIds);
			
			selectQuery += " WHERE (";
			for (int i = 0; i < messageParentIdsCount; i++)
			{
				if (i > 0)
				{
					selectQuery += " OR ";
				}
				selectQuery += MESSAGE_COLUMN_MESSAGE_PARENT_ID + " = ?"
					+ " OR " + MESSAGE_COLUMN_MESSAGE_ID + " = ?";
			}
			selectQuery += ")";
		}
		selectQuery += " ORDER BY " + MESSAGE_COLUMN_MESSAGE_DATE + " DESC, "
			+ MESSAGE_COLUMN_MESSAGE_ID + " DESC";
		SilverTrace.info("forums", "ForumsDAO.getLastMessage()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + selectQuery);
		
		PreparedStatement selectStmt = null;
		ResultSet rs = null;

		String messageId = "";
		try
		{
			selectStmt = con.prepareStatement(selectQuery);
			if (messageParentIdsCount > 0)
			{
				int index = 1;
				int messageParentId;
				for (int i = 0; i < messageParentIdsCount; i++)
				{
					messageParentId = Integer.parseInt((String)messageParentIds.get(i));
					selectStmt.setInt(index++, messageParentId);
					selectStmt.setInt(index++, messageParentId);
				}
			}
			rs = selectStmt.executeQuery();
			if (rs.next())
			{
				messageId = String.valueOf(rs.getInt(1));
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		SilverTrace.debug("forums", "ForumsDAO.getLastMessage()", "root.MSG_GEN_PARAM_VALUE",
			"messageId = " + messageId);
		
		Message message = null;
		if (!messageId.equals(""))
		{
			MessagePK messagePK = new MessagePK(
				forumPK.getComponentName(), forumPK.getDomain(), messageId);
			message = getMessage(con, messagePK);
		}
		SilverTrace.debug("forums", "ForumsDAO.getLastMessage()", "root.MSG_GEN_PARAM_VALUE",
			"message = " + message);
		return message;
	}
	
	private static final String QUERY_GET_MESSAGE_INFOS =
		"SELECT " + MESSAGE_COLUMNS
		+ " FROM " + MESSAGE_TABLE
		+ " WHERE " + MESSAGE_COLUMN_MESSAGE_ID + " = ?";

	/**
	 * @param con The connection to the database.
	 * @param messagePK The primary key of the message.
	 * @return The message corresponding to the primary key (Vector).
	 * @throws SQLException An SQL exception.
	 */
	public static Vector getMessageInfos(Connection con, MessagePK messagePK)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getMessageInfos()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + QUERY_GET_MESSAGE_INFOS);
		
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_MESSAGE_INFOS);
			selectStmt.setInt(1, Integer.parseInt(messagePK.getId()));
			rs = selectStmt.executeQuery();
			if (rs.next())
			{
				return resultSet2VectorMessage(rs);
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return new Vector();
	}
	
	private static final String QUERY_GET_MESSAGE =
		"SELECT " + MESSAGE_COLUMNS
		+ " FROM " + MESSAGE_TABLE
		+ " WHERE " + MESSAGE_COLUMN_MESSAGE_ID + " = ?";
	
	/**
	 * @param con The connection to the database.
	 * @param messagePK The primary key of the message.
	 * @return The message corresponding to the primary key (Message).
	 * @throws SQLException An SQL exception.
	 */
	public static Message getMessage(Connection con, MessagePK messagePK)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getMessage()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + QUERY_GET_MESSAGE);
		
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_MESSAGE);
			selectStmt.setInt(1, Integer.parseInt(messagePK.getId()));
			rs = selectStmt.executeQuery();
			if (rs.next())
			{
				Message message = resultSet2Message(rs);
				message.setInstanceId(messagePK.getComponentName());
				message.setPk(messagePK);
				return message;
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return null;
	}
	
	private static final String QUERY_GET_MESSAGE_TITLE =
		"SELECT " + MESSAGE_COLUMN_MESSAGE_TITLE
		+ " FROM " + MESSAGE_TABLE
		+ " WHERE " + MESSAGE_COLUMN_MESSAGE_ID + " = ?";
	
	/**
	 * @param con The connection to the database.
	 * @param messageId The id of the message.
	 * @return The title of the message..
	 * @throws SQLException An SQL exception.
	 */
	public static String getMessageTitle(Connection con, int messageId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getMessageTitle()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + QUERY_GET_MESSAGE_TITLE);
		
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_MESSAGE_TITLE);
			selectStmt.setInt(1, messageId);
			rs = selectStmt.executeQuery();
			if (rs.next())
			{
				return rs.getString(MESSAGE_COLUMN_MESSAGE_TITLE);
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return "";
	}
	
	private static final String QUERY_GET_MESSAGE_PARENT_ID =
		"SELECT " + MESSAGE_COLUMN_MESSAGE_PARENT_ID
		+ " FROM " + MESSAGE_TABLE
		+ " WHERE " + MESSAGE_COLUMN_MESSAGE_ID + " = ?";
	
	/**
	 * @param con The connection to the database.
	 * @param messageId The id of the message.
	 * @return The id of the parent of the message..
	 * @throws SQLException An SQL exception.
	 */
	public static int getMessageParentId(Connection con, int messageId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getMessageParentId()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + QUERY_GET_MESSAGE_PARENT_ID);
		
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_MESSAGE_PARENT_ID);
			selectStmt.setInt(1, messageId);
			rs = selectStmt.executeQuery();
			if (rs.next())
			{
				return rs.getInt(MESSAGE_COLUMN_MESSAGE_PARENT_ID);
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return -1;
	}
	
	private static final String QUERY_GET_THREAD =
		"SELECT " + MESSAGE_COLUMNS + " FROM " + MESSAGE_TABLE
		+ " WHERE " + MESSAGE_COLUMN_MESSAGE_ID + " = ?"
		+ " AND " + MESSAGE_COLUMN_MESSAGE_PARENT_ID + " = ?";
	
	/**
	 * @param con The connection to the database.
	 * @param messagePK The primary key of the message.
	 * @return The thread corresponding to the primary key (Message).
	 * @throws SQLException An SQL exception.
	 */
	public static Message getThread(Connection con, MessagePK messagePK)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getThread()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + QUERY_GET_THREAD);
		
		int messageId = Integer.parseInt(messagePK.getId());
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_THREAD);
			selectStmt.setInt(1, messageId);
			selectStmt.setInt(2, 0);
			rs = selectStmt.executeQuery();
			if (rs.next())
			{
				Message message = resultSet2Message(rs);
				message.setPk(messagePK);
				return message;
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return null;
	}
	
	private static final String QUERY_CREATE_MESSAGE =
		"INSERT INTO " + MESSAGE_TABLE
		+ " (" + MESSAGE_COLUMNS + ")"
		+ " VALUES (?, ?, ?, ?, ?, ?)";

	/**
	 * Creates a message.
	 * 
	 * @param con The connection to the database.
	 * @param messageTitle The title of the message.
	 * @param messageAuthor The author of the message.
	 * @param messageDate The date of creation of the message.
	 * @param forumId The id of the parent forum.
	 * @param messageParent The id of the parent message.
	 * @return The id of the newly created message.
	 * @throws SQLException An SQL exception.
	 * @throws UtilException
	 */
	public static int createMessage(Connection con, String messageTitle, String messageAuthor,
			Date messageDate, int forumId, int messageParent)
		throws SQLException, UtilException
	{
		if (messageDate == null)
		{
			messageDate = new Date();
		}
		
		SilverTrace.info("forums", "ForumsDAO.createMessage()", "root.MSG_GEN_PARAM_VALUE",
			"insertQuery  = " + QUERY_CREATE_MESSAGE);
		SilverTrace.info("forums", "ForumsDAO.createMessage()", "root.MSG_GEN_PARAM_VALUE",
			"date de création = " + messageDate);

		PreparedStatement insertStmt = null;
		try
		{
			int messageId = DBUtil.getNextId(MESSAGE_TABLE, MESSAGE_COLUMN_MESSAGE_ID);

			insertStmt = con.prepareStatement(QUERY_CREATE_MESSAGE);
			insertStmt.setInt(1, messageId);
			insertStmt.setString(2, messageTitle);
			insertStmt.setString(3, messageAuthor);
			insertStmt.setInt(4, forumId);
			insertStmt.setInt(5, messageParent);
			insertStmt.setTimestamp(6, new Timestamp(messageDate.getTime()));
			insertStmt.executeUpdate();
			
			// ajout pour ce message d'une date de visite
			addLastVisit(con, messageAuthor, messageId);
			
			return messageId;
		}
		finally
		{
			DBUtil.close(insertStmt);
		}
	}
	
	private static final String QUERY_UPDATE_MESSAGE =
		"UPDATE " + MESSAGE_TABLE
		+ " SET " + MESSAGE_COLUMN_MESSAGE_TITLE + " = ?"
		+ " WHERE " + MESSAGE_COLUMN_MESSAGE_ID + " = ?";
	
	/**
	 * Updates the message corresponding to the primary key.
	 * 
	 * @param con The connection to the database.
	 * @param messagePK The primary key of the message.
	 * @param title The title of the message.
	 * @throws SQLException An SQL exception.
	 */
	public static void updateMessage(Connection con, MessagePK messagePK, String title)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.updateMessage()", "root.MSG_GEN_PARAM_VALUE",
			"updateQuery  = " + QUERY_UPDATE_MESSAGE);
		
		PreparedStatement updateStmt = null;
		try
		{
			updateStmt = con.prepareStatement(QUERY_UPDATE_MESSAGE);
			updateStmt.setString(1, title);
			updateStmt.setInt(2, Integer.parseInt(messagePK.getId()));
			updateStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(updateStmt);
		}
	}
	
	private static final String QUERY_DELETE_MESSAGE_MESSAGE =
		"DELETE FROM " + MESSAGE_TABLE
		+ " WHERE " + MESSAGE_COLUMN_MESSAGE_ID + " = ?";
	
	private static final String QUERY_DELETE_MESSAGE_SUBSCRIPTION =
		"DELETE FROM " + SUBSCRIPTION_TABLE
		+ " WHERE " + SUBSCRIPTION_COLUMN_MESSAGE_ID + " = ?";

	/**
	 * Deletes the message corresponding to the primary key.
	 * 
	 * @param con The connection to the database.
	 * @param messagePK The primary key of the message.
	 * @throws SQLException An SQL exception.
	 */
	public static void deleteMessage(Connection con, MessagePK messagePK)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.deleteMessage()", "root.MSG_GEN_PARAM_VALUE",
			"deleteQuery  = " + QUERY_DELETE_MESSAGE_MESSAGE);
		
		PreparedStatement deleteStmt = null;
		try
		{
			deleteStmt = con.prepareStatement(QUERY_DELETE_MESSAGE_MESSAGE);
			deleteStmt.setInt(1, Integer.parseInt(messagePK.getId()));
			deleteStmt.executeUpdate();

			deleteStmt = con.prepareStatement(QUERY_DELETE_MESSAGE_SUBSCRIPTION);
			deleteStmt.setString(1, messagePK.getId());
			deleteStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(deleteStmt);
		}
	}
	
	private static final String QUERY_GET_MESSAGE_SONS =
		"SELECT " + MESSAGE_COLUMN_MESSAGE_ID
		+ " FROM " + MESSAGE_TABLE
		+ " WHERE " + MESSAGE_COLUMN_MESSAGE_PARENT_ID + " = ?";

	/**
	 * @param con The connection to the database.
	 * @param messagePK The primary key of the message.
	 * @return The list of ids of the messages which parent is the message corresponding to the
	 *         primary key.
	 * @throws SQLException An SQL exception.
	 */
	public static Vector getMessageSons(Connection con, MessagePK messagePK)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getMessageSons()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + QUERY_GET_MESSAGE_SONS);
		
		Vector messagesIds = new Vector();
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_MESSAGE_SONS);
			selectStmt.setInt(1, Integer.parseInt(messagePK.getId()));
			rs = selectStmt.executeQuery();
			while (rs.next())
			{
				messagesIds.add(String.valueOf(rs.getInt(MESSAGE_COLUMN_MESSAGE_ID)));
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return messagesIds;
	}
	
	private static final String QUERY_IS_MODERATOR =
		"SELECT " + RIGHTS_COLUMN_FORUM_ID
		+ " FROM " + RIGHTS_TABLE
		+ " WHERE " + RIGHTS_COLUMN_USER_ID + " = ?";

	/**
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @param userId The user's id.
	 * @return True if the user owns the role of moderator on the forum corresponding to the
	 *         primary key, else false.
	 * @throws SQLException An SQL exception.
	 */
	public static boolean isModerator(Connection con, ForumPK forumPK, String userId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.isModerator()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + QUERY_IS_MODERATOR + " ; userId=" + userId);
		
		PreparedStatement prepStmt = null;
		ResultSet rs = null;
		try
		{
			prepStmt = con.prepareStatement(QUERY_IS_MODERATOR);
			prepStmt.setString(1, userId);
			rs = prepStmt.executeQuery();
			String forumId = forumPK.getId();
			while (rs.next())
			{
				if (rs.getString(RIGHTS_COLUMN_FORUM_ID).trim().equals(forumId))
				{
					return true;
				}
			}
		}
		finally
		{
			DBUtil.close(rs, prepStmt); 
		}
		return false;
	}
	
	private static final String QUERY_ADD_MODERATOR =
		"INSERT INTO " + RIGHTS_TABLE
		+ " (" + RIGHTS_COLUMNS + ")"
		+ " VALUES (?, ?)";

	/**
	 * Adds the role of moderator to the user on the forum corresponding to the primary key.
	 * 
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @param userId The user's id.
	 * @throws SQLException An SQL exception.
	 */
	public static void addModerator(Connection con, ForumPK forumPK, String userId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.addModerator()", "root.MSG_GEN_PARAM_VALUE",
			"insertQuery  = " + QUERY_ADD_MODERATOR);
		
		PreparedStatement insertStmt = null;
		try
		{
			insertStmt = con.prepareStatement(QUERY_ADD_MODERATOR);
			insertStmt.setString(1, userId);
			insertStmt.setString(2, forumPK.getId());
			insertStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(insertStmt);
		}
	}
	
	private static final String QUERY_REMOVE_MODERATOR =
		"DELETE FROM " + RIGHTS_TABLE
		+ " WHERE " + RIGHTS_COLUMN_USER_ID + " = ?"
		+ " AND " + RIGHTS_COLUMN_FORUM_ID + " = ?";

	/**
	 * Removes the role of moderator to the user on the forum corresponding to the primary key.
	 * 
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @param userId The user's id.
	 * @throws SQLException An SQL exception.
	 */
	public static void removeModerator(Connection con, ForumPK forumPK, String userId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.removeModerator()", "root.MSG_GEN_PARAM_VALUE",
			"deleteQuery  = " + QUERY_REMOVE_MODERATOR);
		
		PreparedStatement deleteStmt = null;
		try
		{
			deleteStmt = con.prepareStatement(QUERY_REMOVE_MODERATOR);
			deleteStmt.setString(1, userId);
			deleteStmt.setString(2, forumPK.getId());
			deleteStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(deleteStmt);
		}
	}
	
	private static final String QUERY_REMOVE_ALL_MODERATORS =
		"DELETE FROM " + RIGHTS_TABLE
		+ " WHERE " + RIGHTS_COLUMN_FORUM_ID + " = ?";

	/**
	 * Removes the role of moderator to all users on the forum corresponding to the primary key.
	 * 
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @throws SQLException An SQL exception.
	 */
	public static void removeAllModerators(Connection con, ForumPK forumPK)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.removeAllModerators()", "root.MSG_GEN_PARAM_VALUE",
			"deleteQuery  = " + QUERY_REMOVE_ALL_MODERATORS);
		
		PreparedStatement deleteStmt = null;
		try
		{
			deleteStmt = con.prepareStatement(QUERY_REMOVE_ALL_MODERATORS);
			deleteStmt.setString(1, forumPK.getId());
			deleteStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(deleteStmt);
		}
	}
	
	private static final String QUERY_MOVE_MESSAGE =
		"UPDATE " + MESSAGE_TABLE
		+ " SET " + MESSAGE_COLUMN_FORUM_ID + " = ?"
		+ " WHERE " + MESSAGE_COLUMN_MESSAGE_ID + " = ?";

	/**
	 * Moves the message corresponding to the message primary key from a previous forum to the one
	 * corresponding to the forum primary key.
	 * 
	 * @param con The connection to the database.
	 * @param messagePK The primary key of the message.
	 * @param forumPK The primary key of the forum.
	 * @throws SQLException An SQL exception.
	 */
	public static void moveMessage(Connection con, MessagePK messagePK, ForumPK forumPK)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.moveMessage()", "root.MSG_GEN_PARAM_VALUE",
			"updateQuery  = " + QUERY_MOVE_MESSAGE);
		
		PreparedStatement updateStmt = null;
		try
		{
			updateStmt = con.prepareStatement(QUERY_MOVE_MESSAGE);
			updateStmt.setInt(1, Integer.parseInt(forumPK.getId()));
			updateStmt.setInt(2, Integer.parseInt(messagePK.getId()));
			updateStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(updateStmt);
		}
	}

	/**
	 * @param con The connection to the database.
	 * @param messagePK The primary key of the message.
	 * @return The list of ids of messages which parent is the message corresponding to the primary
	 *         key.
	 * @throws SQLException An SQL exception.
	 */
	public static Vector getAllMessageSons(Connection con, MessagePK messagePK)
		throws SQLException
	{
		Vector messagesIds = new Vector();
		Vector currentMessagesIds = getMessageSons(con, messagePK);
		String messageId;
		for (int i = 0; i < currentMessagesIds.size(); i++)
		{
			messageId = (String) currentMessagesIds.elementAt(i);
			messagesIds.add(messageId);
			messagesIds.addAll(getAllMessageSons(
				con, new MessagePK(messagePK.getDomain(), messageId)));
		}
		return messagesIds;
	}
	
	private static final String QUERY_SUBSCRIBE_MESSAGE =
		"INSERT INTO " + SUBSCRIPTION_TABLE
		+ " (" + SUBSCRIPTION_COLUMNS + ")"
		+ " VALUES (?, ?)";

	/**
	 * Adds to the user the subscription to the message corresponding to the primary key.
	 * 
	 * @param con The connection to the database.
	 * @param messagePK The primary key of the message.
	 * @param userId The user's id.
	 * @throws SQLException An SQL exception.
	 */
	public static void subscribeMessage(Connection con, MessagePK messagePK, String userId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.subscribeMessage()", "root.MSG_GEN_PARAM_VALUE",
			"insertQuery  = " + QUERY_SUBSCRIBE_MESSAGE);
		
		PreparedStatement insertStmt = null;
		try
		{
			insertStmt = con.prepareStatement(QUERY_SUBSCRIBE_MESSAGE);
			insertStmt.setString(1, userId);
			insertStmt.setString(2, messagePK.getId());
			insertStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(insertStmt);
		}
	}
	
	private static final String QUERY_UNSUBSCRIBE_MESSAGE =
		"DELETE FROM " + SUBSCRIPTION_TABLE
		+ " WHERE " + SUBSCRIPTION_COLUMN_USER_ID + " = ?"
		+ " AND " + SUBSCRIPTION_COLUMN_MESSAGE_ID + " = ?";

	/**
	 * Removes from the user the subscription to the message corresponding to the primary key.
	 * 
	 * @param con The connection to the database.
	 * @param messagePK The primary key of the message.
	 * @param userId The user's id.
	 * @throws SQLException An SQL exception.
	 */
	public static void unsubscribeMessage(Connection con, MessagePK messagePK, String userId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.unsubscribeMessage()", "root.MSG_GEN_PARAM_VALUE",
			"deleteQuery  = " + QUERY_UNSUBSCRIBE_MESSAGE);
		
		PreparedStatement deleteStmt = null;
		try
		{
			deleteStmt = con.prepareStatement(QUERY_UNSUBSCRIBE_MESSAGE);
			deleteStmt.setString(1, userId);
			deleteStmt.setString(2, messagePK.getId());
			deleteStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(deleteStmt);
		}
	}
	
	private static final String QUERY_REMOVE_ALL_SUBSCRIBERS =
		"DELETE FROM " + SUBSCRIPTION_TABLE
		+ " WHERE " + SUBSCRIPTION_COLUMN_MESSAGE_ID + " = ?";

	/**
	 * Removes from all users the subscription to the message corresponding to the primary key.
	 * 
	 * @param con The connection to the database.
	 * @param messagePK The primary key of the message.
	 * @throws SQLException An SQL exception.
	 */
	public static void removeAllSubscribers(Connection con, MessagePK messagePK)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.removeAllSubscribers()", "root.MSG_GEN_PARAM_VALUE",
			"deleteQuery  = " + QUERY_REMOVE_ALL_SUBSCRIBERS);
		
		PreparedStatement deleteStmt = null;
		try
		{
			deleteStmt = con.prepareStatement(QUERY_REMOVE_ALL_SUBSCRIBERS);
			deleteStmt.setString(1, messagePK.getId());
			deleteStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(deleteStmt);
		}
	}
	
	private static final String QUERY_LIST_ALL_SUBSCRIBERS =
		"SELECT " + SUBSCRIPTION_COLUMN_USER_ID
		+ " FROM " + SUBSCRIPTION_TABLE
		+ " WHERE " + SUBSCRIPTION_COLUMN_MESSAGE_ID + " = ?";

	/**
	 * @param con The connection to the database.
	 * @param messagePK The primary key of the message.
	 * @return The list of ids of users who subscribe to the message corresponding to the primary
	 *         key.
	 * @throws SQLException An SQL exception.
	 */
	public static Vector listAllSubscribers(Connection con, MessagePK messagePK)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.listAllSubscribers()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + QUERY_LIST_ALL_SUBSCRIBERS);
		
		Vector userIds = new Vector();
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_LIST_ALL_SUBSCRIBERS);
			selectStmt.setString(1, messagePK.getId());
			rs = selectStmt.executeQuery();
			while (rs.next())
			{
				userIds.add(rs.getString(SUBSCRIPTION_COLUMN_USER_ID));
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return userIds;
	}
	
	private static final String QUERY_IS_SUBSCRIBER =
		"SELECT " + SUBSCRIPTION_COLUMN_USER_ID
		+ " FROM " + SUBSCRIPTION_TABLE
		+ " WHERE " + SUBSCRIPTION_COLUMN_MESSAGE_ID + " = ?"
		+ " AND " + SUBSCRIPTION_COLUMN_USER_ID + " = ?";

	/**
	 * @param con The connection to the database.
	 * @param messagePK The primary key of the message.
	 * @param userId The user's id.
	 * @return True if the user has subscribed to the message corresponding to the primary key.
	 * @throws SQLException An SQL exception.
	 */
	public static boolean isSubscriber(Connection con, MessagePK messagePK, String userId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.isSubscriber()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + QUERY_IS_SUBSCRIBER);
		
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_IS_SUBSCRIBER);
			selectStmt.setString(1, messagePK.getId());
			selectStmt.setString(2, userId);
			rs = selectStmt.executeQuery();
			return (rs.next());
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
	}
	
	private static final String QUERY_GET_FORUM_DETAIL =
		"SELECT " + FORUM_COLUMNS
		+ " FROM " + FORUM_TABLE
		+ " WHERE " + FORUM_COLUMN_FORUM_ID + " = ?";

	/**
	 * @param con The connection to the database.
	 * @param forumPK The primary key of the forum.
	 * @return The forum corresponding to the primary key (ForumDetail).
	 * @throws SQLException An SQL exception.
	 */
	private static ForumDetail getForumDetail(Connection con, ForumPK forumPK)
		throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = con.prepareStatement(QUERY_GET_FORUM_DETAIL);
			stmt.setInt(1, Integer.parseInt(forumPK.getId()));
			rs = stmt.executeQuery();
			if (rs.next())
			{
				return resultSet2ForumDetail(rs, forumPK);
			}
			else
			{
				throw new ForumsRuntimeException("ForumsDAO.getForumDetail()",
					SilverpeasRuntimeException.ERROR, "root.EX_CANT_LOAD_ENTITY_ATTRIBUTES",
					"ForumId = " + forumPK.getId() + " not found in database !");
			}
		}
		finally
		{
			DBUtil.close(rs, stmt);
		}
	}
	
	private static final String QUERY_GET_LAST_VISIT =
		"SELECT " + HISTORY_COLUMN_LAST_ACCESS
		+ " FROM " + HISTORY_TABLE
		+ " WHERE " + HISTORY_COLUMN_USER_ID + " = ?"
		+ " AND " + HISTORY_COLUMN_MESSAGE_ID + " = ?"
		+ " ORDER BY " + HISTORY_COLUMN_LAST_ACCESS + " DESC";

	/**
	 * @param con The connection to the database.
	 * @param userId The user's id.
	 * @param messageId The id of the message.
	 * @return The last access date to the message corresponding to the message id.
	 * @throws SQLException An SQL exception.
	 */
	public static Date getLastVisit(Connection con, String userId, int messageId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.getLastVisit()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + QUERY_GET_LAST_VISIT);
		
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(QUERY_GET_LAST_VISIT);
			selectStmt.setString(1, userId);
			selectStmt.setInt(2, messageId);
			rs = selectStmt.executeQuery();
			if (rs.next())
			{
				SilverTrace.info("forums", "ForumsDAO.getLastVisit()", "root.MSG_GEN_PARAM_VALUE",
					" lastAccess  = " + rs.getString("lastAccess") + " today = " + new Date());
				return new Date(Long.parseLong(rs.getString(HISTORY_COLUMN_LAST_ACCESS)));
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		return null;
	}
	
	/**
	 * @param con The connection to the database.
	 * @param userId The user's id.
	 * @param messageIds The list of ids of the messages.
	 * @return The last access date of the user to the messages corresponding to the list of primary
	 *         keys.
	 * @throws SQLException An SQL exception.
	 */
	public static Date getLastVisit(Connection con, String userId, List messageIds)
		throws SQLException
	{
		String selectQuery = "SELECT " + HISTORY_COLUMN_LAST_ACCESS
			+ " FROM " + HISTORY_TABLE
			+ " WHERE " + HISTORY_COLUMN_USER_ID + " = ?";
		
		int messageIdsCount = (messageIds != null ? messageIds.size() : 0);
		if (messageIdsCount > 0)
		{
			SilverTrace.info("forums", "ForumsDAO.getLastVisit()", "root.MSG_GEN_PARAM_VALUE",
				"messageIds  = " + messageIds);
			selectQuery += " AND (";
			for (int i = 0; i < messageIdsCount; i++)
			{
				if (i > 0)
				{
					selectQuery += " OR ";
				}
				selectQuery += HISTORY_COLUMN_MESSAGE_ID + " = ?";
			}
			selectQuery += ") ORDER BY " + HISTORY_COLUMN_LAST_ACCESS + " DESC";
		}
		SilverTrace.info("forums", "ForumsDAO.getLastVisit()", "root.MSG_GEN_PARAM_VALUE",
			"selectQuery  = " + selectQuery);
		
		Date lastVisit = null;
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		try
		{
			selectStmt = con.prepareStatement(selectQuery);
			int index = 1;
			int messageId;
			selectStmt.setString(index++, userId);
			for (int i = 0; i < messageIdsCount; i++)
			{
				messageId = Integer.parseInt((String)messageIds.get(i));
				selectStmt.setInt(index++, messageId);
			}
			rs = selectStmt.executeQuery();
			if (rs.next())
			{
				lastVisit = new Date(Long.parseLong(rs.getString(1)));
			}
		}
		finally
		{
			DBUtil.close(rs, selectStmt);
		}
		SilverTrace.info("forums", "ForumsDAO.getLastVisit()", "root.MSG_GEN_PARAM_VALUE",
			"date de dernière visite = " + lastVisit);
		return lastVisit;
	}
	
	private static final String QUERY_ADD_LAST_VISIT =
		"INSERT INTO " + HISTORY_TABLE
		+ " (" + HISTORY_COLUMNS + ")"
		+ " VALUES (?, ?, ?)";
	
	/**
	 * Adds an access date to the message corresponding to the message id by the user.
	 * 
	 * @param con The connection to the database.
	 * @param userId The user's id.
	 * @param messageId The id of the message.
	 * @throws SQLException An SQL exception.
	 */
	public static void addLastVisit(Connection con, String userId, int messageId)
		throws SQLException
	{
		// supprimer la ligne correspondante à ce user et ce message si elle existe
		deleteVisit(con, userId, messageId);
		
		Date date = new Date();
		
		SilverTrace.info("forums", "ForumsDAO.addLastVisit()", "root.MSG_GEN_PARAM_VALUE",
			"date de dernière visite = " + date);
		SilverTrace.info("forums", "ForumsDAO.addLastVisit()", "root.MSG_GEN_PARAM_VALUE",
			"insertQuery  = " + QUERY_ADD_LAST_VISIT);
		
		PreparedStatement insertStmt = null;
		try
		{
			insertStmt = con.prepareStatement(QUERY_ADD_LAST_VISIT);
			insertStmt.setString(1, userId);
			insertStmt.setInt(2, messageId);
			insertStmt.setString(3, Long.toString(date.getTime()));
			insertStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(insertStmt);
		}
	}
	
	private static final String QUERY_DELETE_VISIT =
		"DELETE FROM " + HISTORY_TABLE
		+ " WHERE " + HISTORY_COLUMN_USER_ID + " = ?"
		+ " AND " + HISTORY_COLUMN_MESSAGE_ID + " = ?";
	
	/**
	 * Deletes the access date of the user to the message corresponding to the message id.
	 * 
	 * @param con The connection to the database.
	 * @param userId The user's id.
	 * @param messageId The id of the message.
	 * @throws SQLException An SQL exception.
	 */
	public static void deleteVisit(Connection con, String userId, int messageId)
		throws SQLException
	{
		SilverTrace.info("forums", "ForumsDAO.deleteVisit()", "root.MSG_GEN_PARAM_VALUE",
			"deleteQuery  = " + QUERY_DELETE_VISIT);
		
		PreparedStatement deleteStmt = null;
		try
		{
			deleteStmt = con.prepareStatement(QUERY_DELETE_VISIT);
			deleteStmt.setString(1, userId);
			deleteStmt.setInt(2, messageId);
			deleteStmt.executeUpdate();
		}
		finally
		{
			DBUtil.close(deleteStmt);
		}
	}
	
	/**
	 * @param rs The database result set.
	 * @param forumPK The primary key of the forum.
	 * @return The forum corresponding to the primary key (ForumDetail).
	 * @throws SQLException An SQL exception.
	 */
	private static ForumDetail resultSet2ForumDetail(ResultSet rs, ForumPK forumPK)
		throws SQLException
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		Date creationDate;
		try
		{
			creationDate = formatter.parse(rs.getString(FORUM_COLUMN_FORUM_CREATION_DATE));
		}
		catch (ParseException e)
		{
			throw new SQLException("ForumsDAO : resultSet2ForumDetail() : internal error : "
				+ "creationDate format unknown for forumPK = " + forumPK + " : " + e.toString());
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
	private static Forum resultSet2Forum(ResultSet rs)
		throws SQLException
	{
		String category = rs.getString(FORUM_COLUMN_CATEGORY_ID);
		category = (category != null ? category.trim() : null);
		return new Forum(
			rs.getInt(FORUM_COLUMN_FORUM_ID),
			rs.getString(FORUM_COLUMN_FORUM_NAME).trim(),
			rs.getString(FORUM_COLUMN_FORUM_DESCRIPTION),
			(rs.getInt(FORUM_COLUMN_FORUM_ACTIVE) == 1),
			rs.getInt(FORUM_COLUMN_FORUM_PARENT),
			category,
			rs.getString(FORUM_COLUMN_FORUM_CREATION_DATE),
			rs.getString(FORUM_COLUMN_INSTANCE_ID));
	}

	/**
	 * @param rs The database result set.
	 * @return The message corresponding to the primary key (Vector).
	 * @throws SQLException An SQL exception.
	 */
	private static Vector resultSet2VectorMessage(ResultSet rs)
		throws SQLException
	{
		Vector message = new Vector();
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
	private static Message resultSet2Message(ResultSet rs)
		throws SQLException
	{
		Timestamp timestamp = rs.getTimestamp(MESSAGE_COLUMN_MESSAGE_DATE);
		Date date = (timestamp != null ? new Date(timestamp.getTime()) : null);
		return new Message(
			rs.getInt(MESSAGE_COLUMN_MESSAGE_ID),
			rs.getString(MESSAGE_COLUMN_MESSAGE_TITLE).trim(),
			rs.getString(MESSAGE_COLUMN_MESSAGE_AUTHOR),
			date,
			rs.getInt(MESSAGE_COLUMN_FORUM_ID),
			rs.getInt(MESSAGE_COLUMN_MESSAGE_PARENT_ID));
	}
	
}