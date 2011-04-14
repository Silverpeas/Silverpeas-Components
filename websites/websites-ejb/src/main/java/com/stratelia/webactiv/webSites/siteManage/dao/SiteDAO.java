/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

/*
 * SiteDAO.java
 *
 * Created on 18 avril 2001, 11:55
 */
package com.stratelia.webactiv.webSites.siteManage.dao;

/**
 *
 * @author  cbonin
 * @version
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.webSites.siteManage.model.IconDetail;
import com.stratelia.webactiv.webSites.siteManage.model.SiteDetail;
import com.stratelia.webactiv.webSites.siteManage.model.SitePK;

public class SiteDAO {

  private Connection dbConnection;
  private String TableSiteName;
  private String TableIconsName;
  private String TableSiteIconsName;
  private String TablePublicationName;

  private String prefixTableName;
  private String componentId;

  public SiteDAO(String prefixTableName, String componentId) {
    this.prefixTableName = prefixTableName;
    this.componentId = componentId;
    TableSiteName = "SC_WebSites_Site";
    TableIconsName = "SC_WebSites_Icons";
    TableSiteIconsName = "SC_WebSites_SiteIcons";
    TablePublicationName = "SB_Publication_Publi";
  }

  /* DBConnection methods */
  private Connection openConnection() throws SQLException, UtilException {
    return DBUtil.makeConnection(JNDINames.BOOKMARK_DATASOURCE);
  }

  private void closeConnection(Connection dbConnect) {
    try {
      dbConnect.close();
    } catch (SQLException se) {
      SilverTrace.error("webSites", "SiteDAO.closeConnection()",
          "root.EX_CONNECTION_CLOSE_FAILED", "", se);
    }
  }

  /**
   * getIdPublication
   */
  public String getIdPublication(String idSite) throws SQLException,
      UtilException {
    try {
      dbConnection = openConnection();
      return (DAOgetIdPublication(idSite));
    } finally {
      closeConnection(dbConnection);
    }
  }

  /**
   * getAllWebSite
   */
  public Collection<SiteDetail> getAllWebSite() throws SQLException, UtilException {
    try {
      dbConnection = openConnection();
      return (DAOgetAllWebSite());
    } finally {
      closeConnection(dbConnection);
    }
  }

  /**
   * getWebSite
   */
  public SiteDetail getWebSite(SitePK pk) throws SQLException, UtilException {
    try {
      dbConnection = openConnection();
      return (DAOgetWebSite(pk));
    } finally {
      closeConnection(dbConnection);
    }
  }

  /**
   * getWebSites
   */
  public List<SiteDetail> getWebSites(List<String> ids) throws SQLException, UtilException {
    try {
      dbConnection = openConnection();
      return (DAOgetWebSites(ids));
    } finally {
      closeConnection(dbConnection);
    }
  }

  /**
   * getIcons
   */
  public Collection<IconDetail> getIcons(SitePK pk) throws SQLException, UtilException {
    try {
      dbConnection = openConnection();
      return (DAOgetIcons(pk));
    } finally {
      closeConnection(dbConnection);
    }
  }

  /**
   * getNextId
   */
  public String getNextId() throws SQLException, UtilException {
    try {
      dbConnection = openConnection();
      return (DAOgetNextId());
    } finally {
      closeConnection(dbConnection);
    }
  }

  /**
   * getAllIcons
   */
  public Collection<IconDetail> getAllIcons() throws SQLException, UtilException {
    try {
      dbConnection = openConnection();
      return (DAOgetAllIcons());
    } finally {
      closeConnection(dbConnection);
    }
  }

  /**
   * createWebSite
   */
  public void createWebSite(SiteDetail description) throws SQLException,
      UtilException {
    try {
      dbConnection = openConnection();
      DAOcreateWebSite(description);
    } finally {
      closeConnection(dbConnection);
    }
  }

  /**
   * associateIcons
   */
  public void associateIcons(String id, Collection<String> liste) throws SQLException,
      UtilException {
    try {
      dbConnection = openConnection();
      DAOassociateIcons(id, liste);
    } finally {
      closeConnection(dbConnection);
    }
  }

  /**
   * publish
   */
  public void publish(Collection<String> liste) throws SQLException, UtilException {
    try {
      dbConnection = openConnection();
      DAOpublish(liste);
    } finally {
      closeConnection(dbConnection);
    }
  }

  /**
   * dePublish
   */
  public void dePublish(Collection<String> liste) throws SQLException, UtilException {
    try {
      dbConnection = openConnection();
      DAOdePublish(liste);
    } finally {
      closeConnection(dbConnection);
    }
  }

  /**
   * deleteWebSites
   */
  public void deleteWebSites(Collection<String> liste) throws SQLException,
      UtilException {
    try {
      dbConnection = openConnection();
      DAOdeleteWebSites(liste);
    } finally {
      closeConnection(dbConnection);
    }
  }

  /**
   * deleteWebSites
   */
  public void updateWebSite(SiteDetail description) throws SQLException,
      UtilException {
    try {
      dbConnection = openConnection();
      DAOupdateWebSite(description);
    } finally {
      closeConnection(dbConnection);
    }
  }

  /*--------------------------------------------------Methodes de la DAO ------------------------------------------------------------------------------------*/

  /**
   * DAOgetIdPublication
   */
  private String DAOgetIdPublication(String idSite) throws SQLException {
    String idPub = null;

    String queryStr1 = "select pubId from " + TablePublicationName
        + " where instanceId = '" + componentId + "' AND pubVersion = '"
        + idSite + "'";
    Statement stmt = null;
    ResultSet rs1 = null;
    try {
      stmt = dbConnection.createStatement();
      SilverTrace.info("webSites", "SiteDAO.DAOgetIdPublication()",
          "root.MSG_GEN_PARAM_VALUE", "queryStr1 = " + queryStr1);

      rs1 = stmt.executeQuery(queryStr1);
      if (rs1.next()) {
        idPub = Integer.toString(rs1.getInt(1));
      }
    } finally {
      DBUtil.close(rs1);
    }
    return idPub;
  }

  /**
   * DAOgetAllWebSite
   */
  private Collection<SiteDetail> DAOgetAllWebSite() throws SQLException {

    ArrayList<SiteDetail> theSiteList = new ArrayList<SiteDetail>();

    Statement stmt = null;
    ResultSet rs1 = null;

    String queryStr1 = "SELECT siteId, siteName, siteDescription, sitePage, siteType, siteAuthor, "
        + "siteDate, siteState, popup FROM "
        + TableSiteName
        + " where instanceId = '" + componentId + "'" + " order by siteid";

    try {
      SilverTrace.info("webSites", "SiteDAO.DAOgetAllWebSite()",
          "root.MSG_GEN_PARAM_VALUE", "queryStr1 = " + queryStr1);
      stmt = dbConnection.createStatement();
      rs1 = stmt.executeQuery(queryStr1);

      String idSite = "";
      String name = "";
      String description = "";
      String page = "";
      String author = "";
      String date = "";
      int type;
      int state;
      int popup;

      while (rs1.next()) {
        idSite = new Integer(rs1.getInt(1)).toString();
        name = rs1.getString(2);
        description = rs1.getString(3);
        page = rs1.getString(4);
        type = rs1.getInt(5);
        author = rs1.getString(6);
        date = rs1.getString(7);
        state = rs1.getInt(8);

        popup = rs1.getInt(9);

        SiteDetail sitedetail = new SiteDetail(idSite, name, description, page,
            type, author, date, state, popup);

        theSiteList.add(sitedetail);
      } // fin while
    } finally {
      DBUtil.close(rs1, stmt);
    }

    return theSiteList;
  }

  /**
   * DAOgetWebSite
   */
  private SiteDetail DAOgetWebSite(SitePK pk) throws SQLException {
    SiteDetail sitedetail;
    Statement stmt = null;
    ResultSet rs1 = null;
    String queryStr1 = "select siteId, siteName, siteDescription, sitePage, siteType, siteAuthor, "
        + "siteDate, siteState, popup from "
        + TableSiteName
        + " where siteId = " + pk.getId();

    try {

      SilverTrace.info("webSites", "SiteDAO.DAOgetWebSite()",
          "root.MSG_GEN_PARAM_VALUE", "queryStr1 = " + queryStr1);
      stmt = dbConnection.createStatement();
      rs1 = stmt.executeQuery(queryStr1);

      if (!rs1.next()) {
        SilverTrace.error("webSites", "SiteDAO.DAOgetWebSite()",
            "root.EX_RECORD_NOT_FOUND", "IDSITE = " + pk.getId());
      }
      String idSite = new Integer(rs1.getInt(1)).toString();
      String name = rs1.getString(2);
      String description = rs1.getString(3);
      String page = rs1.getString(4);
      int type = rs1.getInt(5);
      String author = rs1.getString(6);
      String date = rs1.getString(7);
      int state = rs1.getInt(8);

      int popup = rs1.getInt(9);

      sitedetail = new SiteDetail(idSite, name, description, page, type,
          author, date, state, popup);
    } finally {
      DBUtil.close(rs1, stmt);
    }

    return sitedetail;
  }

  private List<SiteDetail> DAOgetWebSites(List<String> ids) throws SQLException {
    ArrayList<SiteDetail> theSiteList = new ArrayList<SiteDetail>();
    Statement stmt = null;
    ResultSet rs = null;
    int i = 0;
    try {
      String param = "";
      StringBuffer paramBuffer = new StringBuffer();
      String id = null;
      for (int j = 0; j < ids.size(); j++) {
        if (paramBuffer.length() == 0)
          param = " siteId = ";
        else
          param = " or siteId = ";
        id = ids.get(j);
        paramBuffer.append(param).append(id);
      }
      if (ids.size() > 0) {
        StringBuffer queryStr1 = new StringBuffer();
        queryStr1
            .append("select siteId, siteName, siteDescription, sitePage, siteType, siteAuthor, "
            + "siteDate, siteState, popup");

        queryStr1.append(" from ").append(TableSiteName);
        queryStr1.append(" where").append("(").append(paramBuffer).append(")");
        queryStr1.append(" and instanceId = '").append(componentId).append("'");

        SilverTrace.info("webSites", "SiteDAO.DAOgetWebSites()",
            "root.MSG_GEN_PARAM_VALUE", "queryStr1 = " + queryStr1.toString());

        stmt = dbConnection.createStatement();
        rs = stmt.executeQuery(queryStr1.toString());

        String idSite = "";
        String name = "";
        String description = "";
        String page = "";
        int type;
        String author = "";
        String date = "";
        int state;
        int popup;

        while (rs.next()) {
          i = 1;
          idSite = new Integer(rs.getInt(i++)).toString();
          name = rs.getString(i++);
          description = rs.getString(i++);
          page = rs.getString(i++);
          type = rs.getInt(i++);
          author = rs.getString(i++);
          date = rs.getString(i++);
          state = rs.getInt(i++);
          popup = rs.getInt(i++);
          theSiteList.add(new SiteDetail(idSite, name, description, page, type,
              author, date, state, popup));
        } // fin while
      } // fin if
    } finally {
      DBUtil.close(rs, stmt);
    }
    return theSiteList;
  }

  /**
   * DAOgetIcons
   */
  private Collection<IconDetail> DAOgetIcons(SitePK pk) throws SQLException {
    ArrayList<IconDetail> resultat = new ArrayList<IconDetail>();
    IconDetail icondetail;
    Statement stmt = null;
    ResultSet rs1 = null;
    StringBuffer queryStr1 = new StringBuffer();
    queryStr1.append("select ").append(TableIconsName).append(".iconsId, ")
        .append(TableIconsName).append(".iconsName, ").append(TableIconsName)
        .append(".iconsDescription, ").append(TableIconsName).append(
        ".iconsAddress");
    queryStr1.append(" from ").append(TableSiteIconsName).append(", ").append(
        TableIconsName);
    queryStr1.append(" where ").append(TableSiteIconsName).append(".siteId = ")
        .append(pk.getId());
    queryStr1.append(" and ").append(TableIconsName).append(".iconsId = ")
        .append(TableSiteIconsName).append(".iconsId");

    try {
      SilverTrace.info("webSites", "SiteDAO.DAOgetIcons()",
          "root.MSG_GEN_PARAM_VALUE", "queryStr1 = " + queryStr1.toString());
      stmt = dbConnection.createStatement();
      rs1 = stmt.executeQuery(queryStr1.toString());
      String idIcon = "";
      String name = "";
      String description = "";
      String address = "";
      while (rs1.next()) {
        idIcon = new Integer(rs1.getInt(1)).toString();
        name = rs1.getString(2);
        description = rs1.getString(3);
        address = rs1.getString(4);

        icondetail = new IconDetail(idIcon, name, description, address);
        resultat.add(icondetail);
      }
    } finally {
      DBUtil.close(rs1, stmt);
    }
    return resultat;
  }

  /**
   * DAOgetNextId
   */
  private String DAOgetNextId() throws SQLException, UtilException {

    int nextid = DBUtil.getNextId(TableSiteName, "siteId");

    return new Integer(nextid).toString();
  }

  /**
   * DAOgetAllIcons
   */
  private Collection<IconDetail> DAOgetAllIcons() throws SQLException {
    ArrayList<IconDetail> resultat = new ArrayList<IconDetail>();
    IconDetail icondetail;
    Statement stmt = null;
    ResultSet rs1 = null;

    String queryStr1 = "SELECT iconsId, iconsName, iconsDescription, iconsAddress FROM "
        + TableIconsName;

    try {

      SilverTrace.info("webSites", "SiteDAO.DAOgetAllIcons()",
          "root.MSG_GEN_PARAM_VALUE", "queryStr1 = " + queryStr1);
      stmt = dbConnection.createStatement();
      rs1 = stmt.executeQuery(queryStr1);
      String idIcon = "";
      String name = "";
      String description = "";
      String address = "";
      while (rs1.next()) {
        idIcon = new Integer(rs1.getInt(1)).toString();
        name = rs1.getString(2);
        description = rs1.getString(3);
        address = rs1.getString(4);

        icondetail = new IconDetail(idIcon, name, description, address);
        resultat.add(icondetail);
      }
    } finally {
      DBUtil.close(rs1, stmt);
    }

    return resultat;
  }

  /**
   * DAOcreateWebSite
   */
  private void DAOcreateWebSite(SiteDetail site) throws SQLException {
    SilverTrace.info("webSites", "SiteDAO.DAOcreateWebSite()",
        "root.MSG_GEN_PARAM_VALUE", "site = " + site.toString());

    String queryStr = "insert into " + TableSiteName
        + " values (?,?,?,?,?,?,?,?,?,?)";

    PreparedStatement stmt = null;
    try {
      stmt = dbConnection.prepareStatement(queryStr);
      stmt.setInt(1, new Integer(site.getSitePK().getId()).intValue());
      stmt.setString(2, site.getName());
      stmt.setString(3, site.getDescription());
      stmt.setString(4, site.getContent());
      stmt.setInt(5, site.getType());
      stmt.setString(6, site.getCreatorId());
      stmt.setString(7, DateUtil.date2SQLDate(site.getCreationDate()));
      stmt.setInt(8, site.getState());
      stmt.setString(9, componentId); // insert the instanceId
      stmt.setInt(10, site.getPopup());

      int resultCount = stmt.executeUpdate();
      if (resultCount != 1)
        SilverTrace.error("webSites", "SiteDAO.DAOcreateWebSite()",
            "webSites.EX_RECORD_INSERTION_PROBLEM", "query = " + queryStr
            + "resultCount = " + resultCount);
    } finally {
      DBUtil.close(stmt);
    }
  }

  /**
   * DAOassociateIcons
   */
  private void DAOassociateIcons(String id, Collection<String> liste)
      throws SQLException {

    String queryStr = "INSERT INTO " + TableSiteIconsName + " VALUES (?,?)";
    PreparedStatement stmt = null;

    try {
      stmt = dbConnection.prepareStatement(queryStr);
      stmt.setInt(1, new Integer(id).intValue());
      String idIcon = "";
      Iterator<String> i = liste.iterator();
      while (i.hasNext()) {
        idIcon = i.next();
        stmt.setInt(2, new Integer(idIcon).intValue());
        SilverTrace.info("webSites", "SiteDAO.DAOassociateIcons()",
            "root.MSG_GEN_PARAM_VALUE", "queryStr= " + queryStr + ", idSite= "
            + id + ", idIcon= " + idIcon + " instanceId= " + componentId);
        int resultCount = stmt.executeUpdate();
        if (resultCount != 1) {
          SilverTrace.error("webSites", "SiteDAO.DAOassociateIcons()",
              "webSites.EX_RECORD_INSERTION_PROBLEM", "query = " + queryStr
              + "resultCount = " + resultCount);
        }
      }
    } finally {
      DBUtil.close(stmt);
    }
  }

  /**
   * DAOpublishDepublishSite
   */
  private void DAOpublishDepublishSite(String id, int state)
      throws SQLException {

    String queryStr = "update " + TableSiteName
        + " set siteState=? where siteId= ?";

    PreparedStatement stmt = null;
    try {
      stmt = dbConnection.prepareStatement(queryStr);
      stmt.setInt(1, state);
      stmt.setInt(2, (new Integer(id)).intValue());

      SilverTrace.info("webSites", "SiteDAO.DAOpublishDepublishSite()",
          "root.MSG_GEN_PARAM_VALUE", "queryStr = " + queryStr + ", idSite= "
          + id);
      int resultCount = stmt.executeUpdate();
      if (resultCount != 1)
        SilverTrace.error("webSites", "SiteDAO.DAOpublishDepublishSite()",
            "webSites.EX_RECORD_UPDATE_PROBLEM", "query = " + queryStr
            + "resultCount = " + resultCount);
    } finally {
      DBUtil.close(stmt);
    }
  }

  /**
   * DAOpublish
   */
  private void DAOpublish(Collection<String> liste) throws SQLException {
    Iterator<String> i = liste.iterator();
    while (i.hasNext()) {
      DAOpublishDepublishSite(i.next(), 1);
    }
  }

  /**
   * DAOdePublish
   */
  private void DAOdePublish(Collection<String> liste) throws SQLException {
    Iterator<String> i = liste.iterator();
    while (i.hasNext()) {
      DAOpublishDepublishSite(i.next(), 0);
    }
  }

  /**
   * DAOdeleteAssociateIcons
   */
  private void DAOdeleteAssociateIcons(SitePK pk) throws SQLException {

    String deleteStr = "delete from " + TableSiteIconsName
        + " where siteId = ?";

    SilverTrace.info("webSites", "SiteDAO.DAOdeleteAssociateIcons()",
        "root.MSG_GEN_PARAM_VALUE", "queryStr = " + deleteStr + ", idSite= "
        + pk.getId());
    PreparedStatement prepStmt = null;

    try {
      prepStmt = dbConnection.prepareStatement(deleteStr);
      prepStmt.setInt(1, new Integer(pk.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * DAOdeleteWebSite
   */
  private void DAOdeleteWebSite(SitePK pk) throws SQLException {

    DAOdeleteAssociateIcons(pk);

    String deleteStr = "delete from " + TableSiteName + " where siteId = ?";

    SilverTrace.info("webSites", "SiteDAO.DAOdeleteWebSite()",
        "root.MSG_GEN_PARAM_VALUE", "deleteStr= " + deleteStr + ", idSite= "
        + pk.getId());
    PreparedStatement prepStmt = null;
    try {
      prepStmt = dbConnection.prepareStatement(deleteStr);
      prepStmt.setInt(1, new Integer(pk.getId()).intValue());
      int resultCount = prepStmt.executeUpdate();
      if (resultCount != 1)
        SilverTrace.error("webSites", "SiteDAO.DAOdeleteWebSite()",
            "webSites.EX_RECORD_DELETE_PROBLEM", "deleteStr = " + deleteStr
            + ", resultCount = " + resultCount);
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * DAOdeleteWebSites
   */
  private void DAOdeleteWebSites(Collection<String> liste) throws SQLException {
    ArrayList<String> array = new ArrayList<String>(liste);
    int i = 0;
    String id = "";
    while (i < array.size()) {
      id = array.get(i);
      SilverTrace.info("webSites", "SiteDAO.DAOdeleteWebSites()",
          "root.MSG_GEN_PARAM_VALUE", "id = " + id);
      SitePK s = new SitePK(id, prefixTableName, componentId);
      DAOdeleteWebSite(s);
      i++;
    }
  }

  /**
   * DAOupdateWebSite
   */
  private void DAOupdateWebSite(SiteDetail description) throws SQLException {
    DAOdeleteAssociateIcons(description.getSitePK());

    String updateStr = "update " + TableSiteName + " set " + "siteName = ?, "
        + "siteDescription = ?, " + "sitePage = ?, " + "siteAuthor = ?, "
        + "siteDate = ?, " + "siteState = ?, " + "popup = ? "
        + " where siteId = ?";

    SilverTrace.info("webSites", "SiteDAO.DAOupdateWebSite()",
        "root.MSG_GEN_PARAM_VALUE", "updateStr= " + updateStr + ", Site= "
        + description.toString());
    PreparedStatement prepStmt = null;
    try {
      int i = 1;
      prepStmt = dbConnection.prepareStatement(updateStr);
      prepStmt.setString(i++, description.getName());
      prepStmt.setString(i++, description.getDescription());
      prepStmt.setString(i++, description.getContent());
      prepStmt.setString(i++, description.getCreatorId());
      prepStmt.setString(i++, DateUtil.date2SQLDate(description
          .getCreationDate()));
      prepStmt.setInt(i++, description.getState());
      prepStmt.setInt(i++, description.getPopup());
      prepStmt.setInt(i++, new Integer(description.getSitePK().getId())
          .intValue());

      int resultCount = prepStmt.executeUpdate();
      if (resultCount != 1)
        SilverTrace.error("webSites", "SiteDAO.DAOupdateWebSite()",
            "webSites.webSites.EX_RECORD_UPDATE_PROBLEM", "updateStr = "
            + updateStr + ", Site= " + description.toString()
            + ", resultCount = " + resultCount);
    } finally {
      DBUtil.close(prepStmt);
    }
  }
}