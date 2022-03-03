/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.websites.siteManage.dao;

/**
 *
 * @author cbonin
 * @version
 */

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.components.websites.siteManage.model.IconDetail;
import org.silverpeas.components.websites.siteManage.model.SiteDetail;
import org.silverpeas.components.websites.siteManage.model.SitePK;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.exception.UtilException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SiteDAO {

  private Connection dbConnection;
  private static final String TABLE_SITE_NAME = "SC_WebSites_Site";
  private static final String TABLE_ICONS_NAME = "SC_WebSites_Icons";
  private static final String TABLE_SITE_ICONS_NAME = "SC_WebSites_SiteIcons";
  private static final String TABLE_PUBLICATION_NAME = "SB_Publication_Publi";

  private String componentId;

  public SiteDAO(String componentId) {
    this.componentId = componentId;
  }

  /* DBConnection methods */
  private Connection openConnection() throws SQLException {
    return DBUtil.openConnection();
  }

  private void closeConnection(Connection dbConnect) {
    try {
      dbConnect.close();
    } catch (SQLException se) {
      SilverTrace
          .error("webSites", "SiteDAO.closeConnection()", "root.EX_CONNECTION_CLOSE_FAILED", "",
              se);
    }
  }

  /**
   * getIdPublication
   */
  public String getIdPublication(String idSite) throws SQLException, UtilException {
    try {
      dbConnection = openConnection();
      return daoGetIdPublication(idSite);
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
      return daoGetAllWebSite();
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
      return daoGetWebSite(pk);
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
      return daoGetWebSites(ids);
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
      return (daoGetIcons(pk));
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
      return (daoGetNextId());
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
      return (daoGetAllIcons());
    } finally {
      closeConnection(dbConnection);
    }
  }

  /**
   * createWebSite
   */
  public void createWebSite(SiteDetail description) throws SQLException, UtilException {
    try {
      dbConnection = openConnection();
      daoCreateWebSite(description);
    } finally {
      closeConnection(dbConnection);
    }
  }

  /**
   * associateIcons
   */
  public void associateIcons(String id, Collection<String> liste)
      throws SQLException, UtilException {
    try {
      dbConnection = openConnection();
      daoAssociateIcons(id, liste);
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
      daoPublish(liste);
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
      daoDePublish(liste);
    } finally {
      closeConnection(dbConnection);
    }
  }

  /**
   * deleteWebSites
   */
  public void deleteWebSites(Collection<String> liste) throws SQLException, UtilException {
    try {
      dbConnection = openConnection();
      daoDeleteWebSites(liste);
    } finally {
      closeConnection(dbConnection);
    }
  }

  public void deleteAllWebSites() throws SQLException {
    final String siteIcons =
        "DELETE FROM " + TABLE_SITE_ICONS_NAME + " WHERE siteId in (SELECT siteId FROM " +
            TABLE_SITE_NAME + " WHERE instanceId = ?)";
    final String sites = "DELETE FROM " + TABLE_SITE_NAME + " WHERE instanceId =?";
    try (Connection connection = openConnection()) {
      try (PreparedStatement deletion = connection.prepareStatement(siteIcons)) {
        deletion.setString(1, componentId);
        deletion.execute();
      }
      try (PreparedStatement deletion = connection.prepareStatement(sites)) {
        deletion.setString(1, componentId);
        deletion.execute();
      }
    }
  }

  /**
   * deleteWebSites
   */
  public void updateWebSite(SiteDetail description) throws SQLException, UtilException {
    try {
      dbConnection = openConnection();
      daoUpdateWebSite(description);
    } finally {
      closeConnection(dbConnection);
    }
  }

  /**
   * @param idSite
   * @return
   * @throws SQLException
   */
  private String daoGetIdPublication(String idSite) throws SQLException {
    String idPub = null;
    String queryStr1 =
        "select pubId from " + TABLE_PUBLICATION_NAME + " where instanceId = '" + componentId +
            "' AND pubVersion = '" + idSite + "'";

    try (Statement stmt = dbConnection.createStatement()) {

      try (ResultSet rs1 = stmt.executeQuery(queryStr1)) {
        if (rs1.next()) {
          idPub = Integer.toString(rs1.getInt(1));
        }
      }
    }
    return idPub;
  }

  /**
   * @return
   * @throws SQLException
   */
  private Collection<SiteDetail> daoGetAllWebSite() throws SQLException {

    List<SiteDetail> theSiteList = new ArrayList<>();

    Statement stmt = null;
    ResultSet rs1 = null;

    String queryStr1 =
        "SELECT siteId, siteName, siteDescription, sitePage, siteType, siteAuthor, " +
            "siteDate, siteState, popup FROM " + TABLE_SITE_NAME + " where instanceId = '" +
            componentId + "'" + " order by siteid";

    try {

      stmt = dbConnection.createStatement();
      rs1 = stmt.executeQuery(queryStr1);

      while (rs1.next()) {
        String idSite = Integer.toString(rs1.getInt(1));
        String name = rs1.getString(2);
        String description = rs1.getString(3);
        String page = rs1.getString(4);
        int type = rs1.getInt(5);
        String author = rs1.getString(6);
        String date = rs1.getString(7);
        int state = rs1.getInt(8);
        int popup = rs1.getInt(9);

        SiteDetail sitedetail =
            new SiteDetail(idSite, componentId, name, description, page, type, author, date, state,
                popup);

        theSiteList.add(sitedetail);
      } // fin while
    } finally {
      DBUtil.close(rs1, stmt);
    }

    return theSiteList;
  }

  /**
   * @param pk
   * @return
   * @throws SQLException
   */
  private SiteDetail daoGetWebSite(SitePK pk) throws SQLException {
    SiteDetail sitedetail;
    Statement stmt = null;
    ResultSet rs1 = null;
    String queryStr1 =
        "select siteId, siteName, siteDescription, sitePage, siteType, siteAuthor, " +
            "siteDate, siteState, popup from " + TABLE_SITE_NAME + " where siteId = " + pk.getId();

    try {


      stmt = dbConnection.createStatement();
      rs1 = stmt.executeQuery(queryStr1);

      if (!rs1.next()) {
        SilverTrace.error("webSites", "SiteDAO.DAOgetWebSite()", "root.EX_RECORD_NOT_FOUND",
            "IDSITE = " + pk.getId());
      }
      String idSite = Integer.toString(rs1.getInt(1));
      String name = rs1.getString(2);
      String description = rs1.getString(3);
      String page = rs1.getString(4);
      int type = rs1.getInt(5);
      String author = rs1.getString(6);
      String date = rs1.getString(7);
      int state = rs1.getInt(8);

      int popup = rs1.getInt(9);

      sitedetail =
          new SiteDetail(idSite, componentId, name, description, page, type, author, date, state,
              popup);
    } finally {
      DBUtil.close(rs1, stmt);
    }

    return sitedetail;
  }

  /**
   * @param ids
   * @return
   * @throws SQLException
   */
  private List<SiteDetail> daoGetWebSites(List<String> ids) throws SQLException {
    ArrayList<SiteDetail> theSiteList = new ArrayList<>();
    Statement stmt = null;
    ResultSet rs = null;
    try {
      String param;
      StringBuilder paramBuffer = new StringBuilder();
      for (String id : ids) {
        if (paramBuffer.length() == 0) {
          param = " siteId = ";
        } else {
          param = " or siteId = ";
        }
        paramBuffer.append(param).append(id);
      }
      if (ids.size() > 0) {
        StringBuilder queryStr1 = new StringBuilder();
        queryStr1.append(
            "select siteId, siteName, siteDescription, sitePage, siteType, siteAuthor, " +
                "siteDate, siteState, popup");

        queryStr1.append(" from ").append(TABLE_SITE_NAME);
        queryStr1.append(" where").append("(").append(paramBuffer).append(")");
        queryStr1.append(" and instanceId = '").append(componentId).append("'");



        stmt = dbConnection.createStatement();
        rs = stmt.executeQuery(queryStr1.toString());

        while (rs.next()) {
          int i = 1;
          String idSite = Integer.toString(rs.getInt(i++));
          String name = rs.getString(i++);
          String description = rs.getString(i++);
          String page = rs.getString(i++);
          int type = rs.getInt(i++);
          String author = rs.getString(i++);
          String date = rs.getString(i++);
          int state = rs.getInt(i++);
          int popup = rs.getInt(i++);
          theSiteList.add(
              new SiteDetail(idSite, componentId, name, description, page, type, author, date,
                  state, popup));
        } // fin while
      } // fin if
    } finally {
      DBUtil.close(rs, stmt);
    }
    return theSiteList;
  }

  /**
   * @param pk
   * @return
   * @throws SQLException
   */
  private Collection<IconDetail> daoGetIcons(SitePK pk) throws SQLException {
    ArrayList<IconDetail> resultat = new ArrayList<>();
    IconDetail icondetail;
    Statement stmt = null;
    ResultSet rs1 = null;
    StringBuilder queryStr1 = new StringBuilder();
    queryStr1.append("select ").append(TABLE_ICONS_NAME).append(".iconsId, ").append(
        TABLE_ICONS_NAME)
        .append(".iconsName, ").append(TABLE_ICONS_NAME).append(".iconsDescription, ")
        .append(TABLE_ICONS_NAME).append(".iconsAddress");
    queryStr1.append(" from ").append(TABLE_SITE_ICONS_NAME).append(", ").append(TABLE_ICONS_NAME);
    queryStr1.append(" where ").append(TABLE_SITE_ICONS_NAME).append(".siteId = ").append(pk.getId());
    queryStr1.append(" and ").append(TABLE_ICONS_NAME).append(".iconsId = ")
        .append(TABLE_SITE_ICONS_NAME).append(".iconsId");

    try {

      stmt = dbConnection.createStatement();
      rs1 = stmt.executeQuery(queryStr1.toString());
      while (rs1.next()) {
        String idIcon = Integer.valueOf(rs1.getInt(1)).toString();
        String name = rs1.getString(2);
        String description = rs1.getString(3);
        String address = rs1.getString(4);

        icondetail = new IconDetail(idIcon, name, description, address);
        resultat.add(icondetail);
      }
    } finally {
      DBUtil.close(rs1, stmt);
    }
    return resultat;
  }

  /**
   * @return
   * @throws SQLException
   */
  private String daoGetNextId() throws SQLException {
    int nextid = DBUtil.getNextId(TABLE_SITE_NAME, "siteId");
    return Integer.toString(nextid);
  }

  /**
   * @return
   * @throws SQLException
   */
  private Collection<IconDetail> daoGetAllIcons() throws SQLException {
    ArrayList<IconDetail> resultat = new ArrayList<>();
    IconDetail icondetail;
    Statement stmt = null;
    ResultSet rs1 = null;
    String queryStr1 =
        "SELECT iconsId, iconsName, iconsDescription, iconsAddress FROM " + TABLE_ICONS_NAME;

    try {


      stmt = dbConnection.createStatement();
      rs1 = stmt.executeQuery(queryStr1);
      while (rs1.next()) {
        String idIcon = Integer.valueOf(rs1.getInt(1)).toString();
        String name = rs1.getString(2);
        String description = rs1.getString(3);
        String address = rs1.getString(4);
        icondetail = new IconDetail(idIcon, name, description, address);
        resultat.add(icondetail);
      }
    } finally {
      DBUtil.close(rs1, stmt);
    }

    return resultat;
  }

  /**
   * @param site
   * @throws SQLException
   */
  private void daoCreateWebSite(SiteDetail site) throws SQLException {


    String queryStr = "insert into " + TABLE_SITE_NAME + " values (?,?,?,?,?,?,?,?,?,?)";

    PreparedStatement stmt = null;
    try {
      stmt = dbConnection.prepareStatement(queryStr);
      stmt.setInt(1, Integer.parseInt(site.getSitePK().getId()));
      stmt.setString(2, site.getName());
      stmt.setString(3, site.getDescription());
      stmt.setString(4, site.getContentPagePath());
      stmt.setInt(5, site.getSiteType());
      stmt.setString(6, site.getCreatorId());
      stmt.setString(7, DateUtil.date2SQLDate(site.getCreationDate()));
      stmt.setInt(8, site.getState());
      stmt.setString(9, componentId); // insert the instanceId
      stmt.setInt(10, site.getPopup());

      int resultCount = stmt.executeUpdate();
      if (resultCount != 1) {
        SilverTrace
            .error("webSites", "SiteDAO.DAOcreateWebSite()", "webSites.EX_RECORD_INSERTION_PROBLEM",
                "query = " + queryStr + "resultCount = " + resultCount);
      }
    } finally {
      DBUtil.close(stmt);
    }
  }

  /**
   * @param id
   * @param liste
   * @throws SQLException
   */
  private void daoAssociateIcons(String id, Collection<String> liste) throws SQLException {
    String queryStr = "INSERT INTO " + TABLE_SITE_ICONS_NAME + " VALUES (?,?)";
    PreparedStatement stmt = null;

    try {
      stmt = dbConnection.prepareStatement(queryStr);
      stmt.setInt(1, Integer.parseInt(id));
      for (final String idIcon : liste) {
        stmt.setInt(2, Integer.parseInt(idIcon));
        int resultCount = stmt.executeUpdate();
        if (resultCount != 1) {
          SilverTrace.error("webSites", "SiteDAO.DAOassociateIcons()",
              "webSites.EX_RECORD_INSERTION_PROBLEM",
              "query = " + queryStr + "resultCount = " + resultCount);
        }
      }
    } finally {
      DBUtil.close(stmt);
    }
  }

  /**
   * @param id
   * @param state
   * @throws SQLException
   */
  private void daoPublishDepublishSite(String id, int state) throws SQLException {
    String queryStr = "update " + TABLE_SITE_NAME + " set siteState=? where siteId= ?";

    PreparedStatement stmt = null;
    try {
      stmt = dbConnection.prepareStatement(queryStr);
      stmt.setInt(1, state);
      stmt.setInt(2, Integer.parseInt(id));


      int resultCount = stmt.executeUpdate();
      if (resultCount != 1) {
        SilverTrace.error("webSites", "SiteDAO.DAOpublishDepublishSite()",
            "webSites.EX_RECORD_UPDATE_PROBLEM",
            "query = " + queryStr + "resultCount = " + resultCount);
      }
    } finally {
      DBUtil.close(stmt);
    }
  }

  /**
   * @param liste
   * @throws SQLException
   */
  private void daoPublish(Collection<String> liste) throws SQLException {
    for (final String id : liste) {
      daoPublishDepublishSite(id, 1);
    }
  }

  /**
   * @param liste
   * @throws SQLException
   */
  private void daoDePublish(Collection<String> liste) throws SQLException {
    for (final String id : liste) {
      daoPublishDepublishSite(id, 0);
    }
  }

  /**
   * @param pk
   * @throws SQLException
   */
  private void daoDeleteAssociateIcons(SitePK pk) throws SQLException {
    String deleteStr = "delete from " + TABLE_SITE_ICONS_NAME + " where siteId = ?";


    PreparedStatement prepStmt = null;

    try {
      prepStmt = dbConnection.prepareStatement(deleteStr);
      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * @param pk
   * @throws SQLException
   */
  private void daoDeleteWebSite(SitePK pk) throws SQLException {
    daoDeleteAssociateIcons(pk);
    String deleteStr = "delete from " + TABLE_SITE_NAME + " where siteId = ?";


    PreparedStatement prepStmt = null;
    try {
      prepStmt = dbConnection.prepareStatement(deleteStr);
      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      int resultCount = prepStmt.executeUpdate();
      if (resultCount != 1) {
        SilverTrace
            .error("webSites", "SiteDAO.DAOdeleteWebSite()", "webSites.EX_RECORD_DELETE_PROBLEM",
                "deleteStr = " + deleteStr + ", resultCount = " + resultCount);
      }
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * @param liste
   * @throws SQLException
   */
  private void daoDeleteWebSites(Collection<String> liste) throws SQLException {
    ArrayList<String> array = new ArrayList<>(liste);
    int i = 0;
    while (i < array.size()) {
      String id = array.get(i);

      SitePK s = new SitePK(id, componentId);
      daoDeleteWebSite(s);
      i++;
    }
  }

  /**
   * @param description
   * @throws SQLException
   */
  private void daoUpdateWebSite(SiteDetail description) throws SQLException {
    daoDeleteAssociateIcons(description.getSitePK());

    String updateStr =
        "update " + TABLE_SITE_NAME + " set " + "siteName = ?, " + "siteDescription = ?, " +
            "sitePage = ?, " + "siteAuthor = ?, " + "siteDate = ?, " + "siteState = ?, " +
            "popup = ? " + " where siteId = ?";


    PreparedStatement prepStmt = null;
    try {
      int i = 1;
      prepStmt = dbConnection.prepareStatement(updateStr);
      prepStmt.setString(i++, description.getName());
      prepStmt.setString(i++, description.getDescription());
      prepStmt.setString(i++, description.getContentPagePath());
      prepStmt.setString(i++, description.getCreatorId());
      prepStmt.setString(i++, DateUtil.date2SQLDate(description.getCreationDate()));
      prepStmt.setInt(i++, description.getState());
      prepStmt.setInt(i++, description.getPopup());
      prepStmt.setInt(i++, Integer.parseInt(description.getSitePK().getId()));

      int resultCount = prepStmt.executeUpdate();
      if (resultCount != 1) {
        SilverTrace.error("webSites", "SiteDAO.DAOupdateWebSite()",
            "webSites.webSites.EX_RECORD_UPDATE_PROBLEM",
            "updateStr = " + updateStr + ", Site= " + description.toString() + ", resultCount = " +
                resultCount);
      }
    } finally {
      DBUtil.close(prepStmt);
    }
  }
}