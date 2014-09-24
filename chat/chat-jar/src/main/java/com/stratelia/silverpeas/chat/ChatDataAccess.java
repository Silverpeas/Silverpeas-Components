/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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

/*
 * ChatDataAccess.java
 *
 * Created on 22/02/2002 by PHiL
 */

package com.stratelia.silverpeas.chat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.DBUtil;
import org.silverpeas.util.JNDINames;
import org.silverpeas.util.exception.SilverpeasException;
import org.silverpeas.util.exception.UtilException;

public class ChatDataAccess {
  private static final String DATASOURCE = JNDINames.CHAT_DATASOURCE;
  private static final String TBL_ROOM = "SC_chat_chatroom";
  private static final String TBL_BANNED = "SC_chat_banned";

  /** use for the PDC utilization */
  private ChatContentManager chatContentManager = null;

  private String componentId = "";

  // constructor
  public ChatDataAccess(String component) {
    componentId = component;
  }

  public Connection CreateConnexion() throws ChatException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(DATASOURCE);
    } catch (Exception e) {
      throw new ChatException("ChatDataAccess.CreateConnexion()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", null, e);
    }
    return con;
  }

  public void CloseConnexion(Connection con, boolean bThrowException)
      throws ChatException {
    try {
      if (con != null) {
        con.close();
      }
    } catch (SQLException se) {
      ChatException oe = new ChatException("ChatDataAccess.CloseConnexion()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", null,
          se);
      SilverTrace.warn("outlook", "ChatDataAccess.CloseConnexion()",
          "root.EX_CONNECTION_CLOSE_FAILED", null, oe);
      if (bThrowException) {
        throw oe;
      }
    }
  }

  // Chatroom handling:

  // retreive the chatroom list of the current instance
  public Vector RetreiveChatroom() throws ChatException {

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    StringBuffer selectStatement = new StringBuffer("");
    Connection con = null;
    boolean haveToThrow = true;

    Vector ListChatroom = new Vector();

    try {
      selectStatement.append("SELECT chatRoomID ");
      selectStatement.append("FROM " + TBL_ROOM + " WHERE instanceID = '"
          + componentId + "' ORDER BY chatRoomID");
      con = CreateConnexion();
      prepStmt = con.prepareStatement(selectStatement.toString());
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        ListChatroom.add(rs.getString(1));
      }
    } catch (SQLException se) {
      haveToThrow = false;
      throw new ChatException("ChatDataAccess.RetreiveListChatroom()",
          SilverpeasException.ERROR, "root.EX_RECORD_NOT_FOUND",
          "selectStatement = " + selectStatement, se);
    } finally {
      try {
        if (rs != null) {
          rs.close();
          rs = null;
        }
        if (prepStmt != null) {
          prepStmt.close();
          prepStmt = null;
        }
      } catch (SQLException ex) {
        SilverTrace.warn("chat", "ChatDataAccess.RetreiveListChatroom()",
            "root.EX_RESOURCE_CLOSE_FAILED", null, ex);
      }
      CloseConnexion(con, haveToThrow);
    }

    return ListChatroom;
  }

  // Insert a chatRoom
  public void InsertChatroom(ChatRoomDetail chatRoom) throws ChatException {

    PreparedStatement prepStmt = null;
    StringBuffer insertStatement = new StringBuffer("");
    Connection con = null;
    boolean haveToThrow = true;
    int id;

    try {
      con = CreateConnexion();
      id = DBUtil.getNextId(TBL_ROOM, "id");
      insertStatement.append("INSERT into " + TBL_ROOM + " values ( " + id
          + " , '" + componentId + "' , " + chatRoom.getId() + " ) ");

      prepStmt = con.prepareStatement(insertStatement.toString());
      prepStmt.executeUpdate();

      // Do classification stuff
      getChatContentManager().createSilverContent(con, chatRoom);
    } catch (SQLException se) {
      haveToThrow = false;
      throw new ChatException("ChatDataAccess.InsertChatroom()",
          SilverpeasException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "InsertStatement = " + insertStatement, se);
    } catch (UtilException ue) {
      haveToThrow = false;
      throw new ChatException("ChatDataAccess.InsertChatroom()",
          SilverpeasException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "InsertStatement = " + insertStatement, ue);
    } catch (ContentManagerException cme) {
      haveToThrow = false;
      throw new ChatException("ChatDataAccess.InsertChatroom()",
          SilverpeasException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "InsertStatement = " + insertStatement, cme);
    }

    finally {
      try {
        if (prepStmt != null) {
          prepStmt.close();
          prepStmt = null;
        }
      } catch (SQLException ex) {
        SilverTrace.warn("chat", "ChatDataAccess.InsertChatroom()",
            "root.EX_RESOURCE_CLOSE_FAILED", null, ex);
      }
      CloseConnexion(con, haveToThrow);
    }

  }

  // Delete a chatRoom
  public void DeleteChatroom(Integer chatRoomId) throws ChatException {

    PreparedStatement prepStmt = null;
    StringBuffer deleteStatement = new StringBuffer("");
    Connection con = null;
    boolean haveToThrow = true;

    try {
      con = CreateConnexion();
      deleteStatement.append("DELETE from " + TBL_ROOM
          + " WHERE instanceID = '" + componentId + "' AND chatRoomID = "
          + chatRoomId + " ");

      prepStmt = con.prepareStatement(deleteStatement.toString());
      prepStmt.executeUpdate();

      // Do classification stuff
      getChatContentManager().deleteSilverContent(con,
          String.valueOf(chatRoomId), componentId);
    } catch (SQLException se) {
      haveToThrow = false;
      throw new ChatException("ChatDataAccess.DeleteChatroom()",
          SilverpeasException.ERROR, "root.EX_RECORD_DELETE_FAILED",
          "DeleteStatement = " + deleteStatement, se);
    } catch (ContentManagerException cme) {
      haveToThrow = false;
      throw new ChatException("ChatDataAccess.DeleteChatroom()",
          SilverpeasException.ERROR, "root.EX_RECORD_DELETE_FAILED",
          "DeleteStatement = " + deleteStatement, cme);
    } finally {
      try {
        if (prepStmt != null) {
          prepStmt.close();
          prepStmt = null;
        }
      } catch (SQLException ex) {
        SilverTrace.warn("chat", "ChatDataAccess.DeleteChatroom()",
            "root.EX_RESOURCE_CLOSE_FAILED", null, ex);
      }
      CloseConnexion(con, haveToThrow);
    }

  }

  // Delete an instance of chat
  public void DeleteChatInstance() throws ChatException {

    jChatBox.Chat.ChatroomManager ChatroomManager = null;
    ChatroomManager = jChatBox.Chat.ChatroomManager.getInstance();
    Vector listChatroom = new Vector();

    PreparedStatement prepStmt = null;
    StringBuffer deleteStatement = new StringBuffer("");
    StringBuffer deleteStatement2 = new StringBuffer("");
    Connection con = null;
    boolean haveToThrow = true;

    // before destroy the data in base, must call the original chat delete
    // function
    listChatroom = RetreiveChatroom();
    for (int i = 0; i < listChatroom.size(); i++) {
      try {
        ChatroomManager.removeChatroom(Integer.parseInt((String) listChatroom
            .elementAt(i)));
      } catch (Exception ce) {
        throw new ChatException("ChatDataAccess.DeleteChatInstance()",
            SilverpeasException.ERROR, "root.DELETING_DATA_OF_INSTANCE_FAILED",
            "ID: " + listChatroom.elementAt(i), ce);

      }

    }

    try {
      con = CreateConnexion();
      deleteStatement.append("DELETE from " + TBL_BANNED
          + " WHERE chatRoomID = (SELECT distinct chatRoomID FROM " + TBL_ROOM
          + " WHERE instanceID = '" + componentId + "' ) ");

      prepStmt = con.prepareStatement(deleteStatement.toString());
      prepStmt.executeUpdate();

    } catch (SQLException se) {
      haveToThrow = false;
      throw new ChatException("ChatDataAccess.DeleteChatInstance()",
          SilverpeasException.ERROR, "root.EX_RECORD_DELETE_FAILED",
          "DeleteStatement = " + deleteStatement, se);
    } finally {
      try {
        if (prepStmt != null) {
          prepStmt.close();
          prepStmt = null;
        }
      } catch (SQLException ex) {
        SilverTrace.warn("chat", "ChatDataAccess.DeleteChatInstance()",
            "root.EX_RESOURCE_CLOSE_FAILED", null, ex);
      }
      CloseConnexion(con, haveToThrow);
    }

    try {
      con = CreateConnexion();
      deleteStatement2.append("DELETE from " + TBL_ROOM
          + " WHERE instanceID = '" + componentId + "' ");

      prepStmt = con.prepareStatement(deleteStatement2.toString());
      prepStmt.executeUpdate();

    } catch (SQLException se) {
      haveToThrow = false;
      throw new ChatException("ChatDataAccess.DeleteChatInstance()",
          SilverpeasException.ERROR, "root.EX_RECORD_DELETE_FAILED",
          "DeleteStatement = " + deleteStatement2, se);
    } finally {
      try {
        if (prepStmt != null) {
          prepStmt.close();
          prepStmt = null;
        }
      } catch (SQLException ex) {
        SilverTrace.warn("chat", "ChatDataAccess.DeleteChatInstance()",
            "root.EX_RESOURCE_CLOSE_FAILED", null, ex);
      }
      CloseConnexion(con, haveToThrow);
    }

  }

  // Banned people handling:

  // Insert a banned people
  public void InsertBanned(String user, int chatRoomId) throws ChatException {

    PreparedStatement prepStmt = null;
    StringBuffer insertStatement = new StringBuffer("");
    Connection con = null;
    boolean haveToThrow = true;
    int id;

    try {
      con = CreateConnexion();
      id = DBUtil.getNextId(TBL_ROOM, "id");
      insertStatement.append("INSERT into " + TBL_BANNED + " values ( " + id
          + " , " + chatRoomId + " , '" + user + "' ) ");

      prepStmt = con.prepareStatement(insertStatement.toString());
      prepStmt.executeUpdate();

    } catch (SQLException se) {
      haveToThrow = false;
      throw new ChatException("ChatDataAccess.InsertBanned()",
          SilverpeasException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "InsertStatement = " + insertStatement, se);
    } catch (UtilException ue) {
      haveToThrow = false;
      throw new ChatException("ChatDataAccess.InsertBanned()",
          SilverpeasException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "InsertStatement = " + insertStatement, ue);
    }

    finally {
      try {
        if (prepStmt != null) {
          prepStmt.close();
          prepStmt = null;
        }
      } catch (SQLException ex) {
        SilverTrace.warn("chat", "ChatDataAccess.InsertBanned()",
            "root.EX_RESOURCE_CLOSE_FAILED", null, ex);
      }
      CloseConnexion(con, haveToThrow);
    }

  }

  // Delete a banned people
  public void DeleteBanned(String user, int chatRoomId) throws ChatException {
    PreparedStatement prepStmt = null;
    StringBuffer deleteStatement = new StringBuffer("");
    Connection con = null;
    boolean haveToThrow = true;

    try {
      con = CreateConnexion();
      deleteStatement.append("DELETE from " + TBL_BANNED
          + " WHERE chatroomID = " + chatRoomId + " AND userID ='" + user
          + "' ");

      prepStmt = con.prepareStatement(deleteStatement.toString());
      prepStmt.executeUpdate();

    } catch (SQLException se) {
      haveToThrow = false;
      throw new ChatException("ChatDataAccess.DeleteBanned()",
          SilverpeasException.ERROR, "root.EX_RECORD_DELETE_FAILED",
          "DeleteStatement = " + deleteStatement, se);
    } finally {
      try {
        if (prepStmt != null) {
          prepStmt.close();
          prepStmt = null;
        }
      } catch (SQLException ex) {
        SilverTrace.warn("chat", "ChatDataAccess.DeleteBanned()",
            "root.EX_RESOURCE_CLOSE_FAILED", null, ex);
      }
      CloseConnexion(con, haveToThrow);
    }

  }

  // Delete all banned people from a channel
  public void DeleteBannedAll(int chatRoomId) throws ChatException {

    PreparedStatement prepStmt = null;
    StringBuffer deleteStatement = new StringBuffer("");
    Connection con = null;
    boolean haveToThrow = true;

    try {
      con = CreateConnexion();
      deleteStatement.append("DELETE from " + TBL_BANNED
          + " WHERE chatroomID = " + chatRoomId + " ");

      prepStmt = con.prepareStatement(deleteStatement.toString());
      prepStmt.executeUpdate();

    } catch (SQLException se) {
      haveToThrow = false;
      throw new ChatException("ChatDataAccess.DeleteBannedAll()",
          SilverpeasException.ERROR, "root.EX_RECORD_DELETE_FAILED",
          "DeleteStatement = " + deleteStatement, se);
    } finally {
      try {
        if (prepStmt != null) {
          prepStmt.close();
          prepStmt = null;
        }
      } catch (SQLException ex) {
        SilverTrace.warn("chat", "ChatDataAccess.DeleteBannedAll()",
            "root.EX_RESOURCE_CLOSE_FAILED", null, ex);
      }
      CloseConnexion(con, haveToThrow);
    }

  }

  // Look if the guyz is banned, return true if so.
  public boolean RetreiveBanned(String user, int chatRoomId)
      throws ChatException {

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    StringBuffer selectStatement = new StringBuffer("");
    Connection con = null;
    boolean haveToThrow = true;
    boolean exist = false;

    try {
      selectStatement.append("SELECT chatRoomID ");
      selectStatement.append("FROM " + TBL_BANNED + " WHERE chatroomID = "
          + chatRoomId + " AND userID ='" + user + "' ");
      con = CreateConnexion();
      prepStmt = con.prepareStatement(selectStatement.toString());
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        exist = true;
      }
    } catch (SQLException se) {
      haveToThrow = false;
      throw new ChatException("ChatDataAccess.RetreiveBanned()",
          SilverpeasException.ERROR, "root.EX_RECORD_NOT_FOUND",
          "selectStatement = " + selectStatement, se);
    } finally {
      try {
        if (rs != null) {
          rs.close();
          rs = null;
        }
        if (prepStmt != null) {
          prepStmt.close();
          prepStmt = null;
        }
      } catch (SQLException ex) {
        SilverTrace.warn("chat", "ChatDataAccess.RetreiveListBanned()",
            "root.EX_RESOURCE_CLOSE_FAILED", null, ex);
      }
      CloseConnexion(con, haveToThrow);
    }

    return exist;
  }

  // récupère la liste des chatRooms
  public Vector RetreiveListBanned(int chatRoomId) throws ChatException {

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    StringBuffer selectStatement = new StringBuffer("");
    Connection con = null;
    boolean haveToThrow = true;

    Vector ListBanned = new Vector();

    try {
      selectStatement.append("SELECT userID ");
      selectStatement.append("FROM " + TBL_BANNED + " WHERE chatroomID = '"
          + chatRoomId + "' ORDER BY userID");
      con = CreateConnexion();
      prepStmt = con.prepareStatement(selectStatement.toString());
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        ListBanned.add(rs.getString(1));
      }
    } catch (SQLException se) {
      haveToThrow = false;
      throw new ChatException("ChatDataAccess.RetreiveListBanned()",
          SilverpeasException.ERROR, "root.EX_RECORD_NOT_FOUND",
          "selectStatement = " + selectStatement, se);
    } finally {
      try {
        if (rs != null) {
          rs.close();
          rs = null;
        }
        if (prepStmt != null) {
          prepStmt.close();
          prepStmt = null;
        }
      } catch (SQLException ex) {
        SilverTrace.warn("chat", "ChatDataAccess.RetreiveListBanned()",
            "root.EX_RESOURCE_CLOSE_FAILED", null, ex);
      }
      CloseConnexion(con, haveToThrow);
    }

    return ListBanned;
  }

  public int getSilverObjectId(String chatRoomId) throws ChatException {
    SilverTrace.info("chat", "ChatDataAccess.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "chatRoomId = " + chatRoomId);
    int silverObjectId = -1;
    ChatRoomDetail chatRoom = null;
    try {
      silverObjectId = getChatContentManager().getSilverObjectId(chatRoomId,
          componentId);
      if (silverObjectId == -1) {
        chatRoom = new ChatRoomDetail(componentId, chatRoomId,
            "Default chatroom name", "Default chatroom comment", null);
        silverObjectId = getChatContentManager().createSilverContent(null,
            chatRoom);
      }
    } catch (Exception e) {
      throw new ChatException("ChatDataAccess.getSilverObjectId()",
          SilverpeasException.ERROR,
          "chat.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
    return silverObjectId;
  }

  private ChatContentManager getChatContentManager() {
    if (chatContentManager == null) {
      chatContentManager = new ChatContentManager();
    }
    return chatContentManager;
  }

}