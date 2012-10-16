/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.wiki.control;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.silverpeas.wiki.control.model.PageDetail;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

public class WikiPageDAO {
  private static final String DATASOURCE = JNDINames.WIKI_DATASOURCE;
  private static final String TABLE_WIKIPAGE = "sc_wiki_page";

  public Connection openConnection() throws WikiException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(DATASOURCE);
    } catch (Exception e) {
      throw new WikiException("WikiPageDAO.CreateConnexion()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", null, e);
    }
    return con;
  }

  public void closeConnection(Connection con) throws WikiException {
    try {
      if (con != null) {
        con.close();
      }
    } catch (SQLException se) {
      WikiException oe = new WikiException("WikiPageDAO.CloseConnexion()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", null,
          se);
      SilverTrace.warn("wiki", "WikiPageDAO.CloseConnexion()",
          "root.EX_CONNECTION_CLOSE_FAILED", null, oe);
    }
  }

  // retreive the chatroom list of the current instance
  public int createPage(PageDetail page) throws WikiException {
    PreparedStatement prepStmt = null;
    StringBuffer insertStatement = new StringBuffer("");
    Connection con = null;
    int id;
    try {
      con = openConnection();
      id = DBUtil.getNextId(TABLE_WIKIPAGE, "id");
      insertStatement.append("INSERT INTO " + TABLE_WIKIPAGE
          + "(id, pagename, instanceid) values ( ?, ?, ? ) ");
      prepStmt = con.prepareStatement(insertStatement.toString());
      prepStmt.setInt(1, id);
      prepStmt.setString(2, page.getPageName());
      prepStmt.setString(3, page.getInstanceId());
      prepStmt.executeUpdate();
      return id;
    } catch (UtilException e) {
      throw new WikiException("WikiPageDAO.createPage()",
          SilverpeasException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "InsertStatement = " + insertStatement, e);
    } catch (SQLException e) {
      throw new WikiException("WikiPageDAO.createPage()",
          SilverpeasException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "InsertStatement = " + insertStatement, e);
    } finally {
      DBUtil.close(prepStmt);
      closeConnection(con);
    }
  }

  public int getNbOfPages(String instanceId) throws WikiException {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Connection con = null;
    int nbPages = 0;
    try {
      con = openConnection();
      prepStmt = con.prepareStatement("select count(*) as nbpages from " + TABLE_WIKIPAGE
          + " where instanceId = ?");
      prepStmt.setString(1, instanceId);
      prepStmt.executeQuery();
       rs = prepStmt.executeQuery();
      if (rs.next()) {
       nbPages = rs.getInt("nbpages");
      }
       return nbPages;
    } catch (SQLException e) {
      throw new WikiException("WikiPageDAO.getNbOfPages()",
          SilverpeasException.ERROR, "root.EX_RECORD_NOT_FOUND", "Wiki id = " + instanceId, e);
    } finally {
      DBUtil.close(prepStmt);
      closeConnection(con);
    }
  }

  public PageDetail getPage(String pageName, String instanceId)
      throws WikiException {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Connection con = null;
    try {
      con = openConnection();
      prepStmt = con.prepareStatement("select id from " + TABLE_WIKIPAGE
          + " where pageName = ? and instanceId = ?");
      prepStmt.setString(1, pageName);
      prepStmt.setString(2, instanceId);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        PageDetail page = new PageDetail();
        page.setId(rs.getInt("id"));
        page.setPageName(pageName);
        page.setInstanceId(instanceId);
        return page;
      }
      return null;
    } catch (SQLException e) {
      throw new WikiException("WikiPageDAO.getPage()",
          SilverpeasException.ERROR, "root.EX_RECORD_NOT_FOUND", "page name = "
          + pageName, e);
    } finally {
      DBUtil.close(rs, prepStmt);
      closeConnection(con);
    }
  }

  public PageDetail getPage(int id, String instanceId) throws WikiException {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Connection con = null;
    try {
      con = openConnection();
      prepStmt = con.prepareStatement("select pageName from " + TABLE_WIKIPAGE
          + " where id = ? and instanceId = ?");
      prepStmt.setInt(1, id);
      prepStmt.setString(2, instanceId);
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        PageDetail page = new PageDetail();
        page.setId(id);
        page.setPageName(rs.getString("pageName"));
        page.setInstanceId(instanceId);
        return page;
      } else {
        throw new WikiException("WikiPageDAO.getPage()",
            SilverpeasException.ERROR, "root.EX_RECORD_NOT_FOUND", "page id = "
            + id);
      }
    } catch (SQLException e) {
      throw new WikiException("WikiPageDAO.getPage()",
          SilverpeasException.ERROR, "root.EX_RECORD_NOT_FOUND", "page id = "
          + id, e);
    } finally {
      DBUtil.close(rs, prepStmt);
      closeConnection(con);
    }
  }

  public void deletePage(String page, String instanceId) throws WikiException {
    PreparedStatement prepStmt = null;
    StringBuffer deleteStatement = new StringBuffer("");
    Connection con = null;
    try {
      con = openConnection();
      deleteStatement.append("DELETE from ").append(TABLE_WIKIPAGE).append(
          " where pageName = ? and instanceId = ? ");

      prepStmt = con.prepareStatement(deleteStatement.toString());
      prepStmt.setString(1, page);
      prepStmt.setString(2, instanceId);
      prepStmt.executeUpdate();
    } catch (SQLException e) {
      throw new WikiException("WikiPageDAO.deletePage()",
          SilverpeasException.ERROR, "root.EX_RECORD_DELETE_FAILED",
          "DeleteStatement = " + deleteStatement, e);
    } finally {
      DBUtil.close(prepStmt);
      closeConnection(con);
    }
  }

  public void deleteAllPages(String instanceId) throws WikiException {
    PreparedStatement prepStmt = null;
    StringBuffer deleteStatement = new StringBuffer("");
    Connection con = null;

    try {
      con = openConnection();
      deleteStatement.append("DELETE from ").append(TABLE_WIKIPAGE).append(
          " where instanceId = ? ");
      prepStmt = con.prepareStatement(deleteStatement.toString());
      prepStmt.setString(1, instanceId);
      prepStmt.executeUpdate();
    } catch (SQLException e) {
      throw new WikiException("WikiPageDAO.deleteAllPages()",
          SilverpeasException.ERROR, "root.EX_RECORD_DELETE_FAILED",
          "DeleteStatement = " + deleteStatement, e);
    } finally {
      DBUtil.close(prepStmt);
      closeConnection(con);
    }
  }

  public void renamePage(String from, String to, String instanceId)
      throws WikiException {
    PreparedStatement prepStmt = null;
    StringBuffer renameStatement = new StringBuffer("");
    Connection con = null;
    try {
      con = openConnection();
      renameStatement.append("UPDATE ").append(TABLE_WIKIPAGE).append(
          " set pageName = ? where pageName = ? and instanceId = ? ");
      prepStmt = con.prepareStatement(renameStatement.toString());
      prepStmt.setString(1, to);
      prepStmt.setString(2, from);
      prepStmt.setString(3, instanceId);
      prepStmt.executeUpdate();
    } catch (SQLException e) {
      throw new WikiException("WikiPageDAO.renamePage()",
          SilverpeasException.ERROR, "root.EX_RECORD_RENAME_FAILED",
          "DeleteStatement = " + renameStatement, e);
    } finally {
      DBUtil.close(prepStmt);
      closeConnection(con);
    }
  }

}