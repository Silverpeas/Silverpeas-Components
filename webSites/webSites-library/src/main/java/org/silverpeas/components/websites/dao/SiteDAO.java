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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.websites.dao;

import org.silverpeas.components.websites.model.IconDetail;
import org.silverpeas.components.websites.model.SiteDetail;
import org.silverpeas.components.websites.model.SiteDetailBuilder;
import org.silverpeas.components.websites.model.SitePK;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.Nonnull;
import javax.persistence.EntityNotFoundException;
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
  private static final String WHERE_SITE_ID_CLAUSE = " where siteId = ?";
  private static final String RESULT_COUNT_MSG_PART = ": result count = ";
  private static final String TABLE_SITE_NAME = "SC_WebSites_Site";
  private static final String TABLE_ICONS_NAME = "SC_WebSites_Icons";
  private static final String TABLE_SITE_ICONS_NAME = "SC_WebSites_SiteIcons";
  private static final String TABLE_PUBLICATION_NAME = "SB_Publication_Publi";

  private final String componentId;

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
      SilverLogger.getLogger(this).error(se);
    }
  }

  public String getIdPublication(String siteId) throws SQLException {
    try {
      dbConnection = openConnection();
      return daoGetIdPublication(siteId);
    } finally {
      closeConnection(dbConnection);
    }
  }

  public Collection<SiteDetail> getAllWebSite() throws SQLException {
    try {
      dbConnection = openConnection();
      return daoGetAllWebSite();
    } finally {
      closeConnection(dbConnection);
    }
  }

  public SiteDetail getWebSite(SitePK pk) throws SQLException {
    try {
      dbConnection = openConnection();
      return daoGetWebSite(pk);
    } finally {
      closeConnection(dbConnection);
    }
  }

  public List<SiteDetail> getWebSites(List<String> siteIds) throws SQLException {
    try {
      dbConnection = openConnection();
      return daoGetWebSites(siteIds);
    } finally {
      closeConnection(dbConnection);
    }
  }

  public Collection<IconDetail> getIcons(SitePK pk) throws SQLException {
    try {
      dbConnection = openConnection();
      return daoGetIcons(pk);
    } finally {
      closeConnection(dbConnection);
    }
  }

  public String getNextId() throws SQLException {
    try {
      dbConnection = openConnection();
      return daoGetNextId();
    } finally {
      closeConnection(dbConnection);
    }
  }

  public Collection<IconDetail> getAllIcons() throws SQLException {
    try {
      dbConnection = openConnection();
      return daoGetAllIcons();
    } finally {
      closeConnection(dbConnection);
    }
  }

  public void createWebSite(SiteDetail description) throws SQLException {
    try {
      dbConnection = openConnection();
      daoCreateWebSite(description);
    } finally {
      closeConnection(dbConnection);
    }
  }

  public void associateIcons(String id, Collection<String> iconIds) throws SQLException {
    try {
      dbConnection = openConnection();
      daoAssociateIcons(id, iconIds);
    } finally {
      closeConnection(dbConnection);
    }
  }

  public void publish(Collection<String> siteIds) throws SQLException {
    try {
      dbConnection = openConnection();
      daoPublish(siteIds);
    } finally {
      closeConnection(dbConnection);
    }
  }

  public void dePublish(Collection<String> siteIds) throws SQLException {
    try {
      dbConnection = openConnection();
      daoDePublish(siteIds);
    } finally {
      closeConnection(dbConnection);
    }
  }

  public void deleteWebSites(Collection<String> siteIds) throws SQLException {
    try {
      dbConnection = openConnection();
      daoDeleteWebSites(siteIds);
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

  public void updateWebSite(SiteDetail description) throws SQLException {
    try {
      dbConnection = openConnection();
      daoUpdateWebSite(description);
    } finally {
      closeConnection(dbConnection);
    }
  }

  private String daoGetIdPublication(String siteId) throws SQLException {
    String idPub = null;
    String queryStr1 =
        "select pubId from " + TABLE_PUBLICATION_NAME + " where instanceId = ? AND pubVersion = ?";

    try (PreparedStatement stmt = dbConnection.prepareStatement(queryStr1)) {
      stmt.setString(1, componentId);
      stmt.setString(2, siteId);
      try (ResultSet rs1 = stmt.executeQuery()) {
        if (rs1.next()) {
          idPub = Integer.toString(rs1.getInt(1));
        }
      }
    }
    return idPub;
  }

  private Collection<SiteDetail> daoGetAllWebSite() throws SQLException {
    List<SiteDetail> theSiteList = new ArrayList<>();
    String queryStr1 =
        "SELECT siteId, siteName, siteDescription, sitePage, siteType, siteAuthor, siteDate, " +
            "siteState, popup FROM " + TABLE_SITE_NAME + " where instanceId = ? order by siteid";

    try (PreparedStatement stmt = dbConnection.prepareStatement(queryStr1)) {
      stmt.setString(1, componentId);
      try (ResultSet rs1 = stmt.executeQuery()) {

        while (rs1.next()) {
          SiteDetail sitedetail = getSiteDetail(rs1);
          theSiteList.add(sitedetail);
        } // fin while
      }
    }

    return theSiteList;
  }

  private SiteDetail daoGetWebSite(SitePK pk) throws SQLException {
    SiteDetail sitedetail;
    String queryStr1 =
        "select siteId, siteName, siteDescription, sitePage, siteType, siteAuthor, " +
            "siteDate, siteState, popup from " + TABLE_SITE_NAME + WHERE_SITE_ID_CLAUSE;

    try (PreparedStatement stmt = dbConnection.prepareStatement(queryStr1)) {
      stmt.setInt(1, Integer.parseInt(pk.getId()));
      try (ResultSet rs1 = stmt.executeQuery()) {
        if (rs1.next()) {
          sitedetail = getSiteDetail(rs1);
        } else {
          throw new EntityNotFoundException("No site found with id " + pk.getId());
        }
      }
    }

    return sitedetail;
  }

  @Nonnull
  private SiteDetail getSiteDetail(final ResultSet rs1) throws SQLException {
    SiteDetail sitedetail;
    String idSite = Integer.toString(rs1.getInt(1));
    String name = rs1.getString(2);
    String description = rs1.getString(3);
    String page = rs1.getString(4);
    int type = rs1.getInt(5);
    String author = rs1.getString(6);
    String date = rs1.getString(7);
    int state = rs1.getInt(8);
    int popup = rs1.getInt(9);

    sitedetail = new SiteDetailBuilder().setSiteId(idSite)
        .setApplicationId(componentId)
        .setName(name)
        .setDescription(description)
        .setPage(page)
        .setType(type)
        .setCreatorId(author)
        .setDate(date)
        .setState(state)
        .setPopup(popup)
        .createSiteDetail();
    return sitedetail;
  }

  private List<SiteDetail> daoGetWebSites(List<String> siteIds) throws SQLException {
    ArrayList<SiteDetail> theSiteList = new ArrayList<>();
    String param;
    StringBuilder paramBuffer = new StringBuilder();
    for (int i = 0; i < siteIds.size(); i++) {
      if (paramBuffer.length() == 0) {
        param = " siteId = ?";
      } else {
        param = " or siteId = ?";
      }
      paramBuffer.append(param);
    }
    if (!siteIds.isEmpty()) {
      String queryStr1 =
          "select siteId, siteName, siteDescription, sitePage, siteType, siteAuthor, " +
              "siteDate, siteState, popup from " + TABLE_SITE_NAME + " where (" + paramBuffer +
              ") and instanceId = ?";

      try (PreparedStatement stmt = dbConnection.prepareStatement(queryStr1)) {
        int i;
        for (i = 0; i < siteIds.size(); i++) {
          stmt.setInt(i + 1, Integer.parseInt(siteIds.get(i)));
        }
        stmt.setString(i+1, componentId);

        try (ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            int j = 1;
            String idSite = Integer.toString(rs.getInt(j++));
            String name = rs.getString(j++);
            String description = rs.getString(j++);
            String page = rs.getString(j++);
            int type = rs.getInt(j++);
            String author = rs.getString(j++);
            String date = rs.getString(j++);
            int state = rs.getInt(j++);
            int popup = rs.getInt(j);
            theSiteList.add(new SiteDetailBuilder().setSiteId(idSite)
                .setApplicationId(componentId)
                .setName(name)
                .setDescription(description)
                .setPage(page)
                .setType(type)
                .setCreatorId(author)
                .setDate(date)
                .setState(state)
                .setPopup(popup)
                .createSiteDetail());
          } // fin while
        } // fin if
      }
    }
    return theSiteList;
  }

  private Collection<IconDetail> daoGetIcons(SitePK pk) throws SQLException {
    StringBuilder queryStr1 = new StringBuilder();
    queryStr1.append("select ")
        .append(TABLE_ICONS_NAME)
        .append(".iconsId, ")
        .append(TABLE_ICONS_NAME)
        .append(".iconsName, ")
        .append(TABLE_ICONS_NAME)
        .append(".iconsDescription, ")
        .append(TABLE_ICONS_NAME)
        .append(".iconsAddress");
    queryStr1.append(" from ").append(TABLE_SITE_ICONS_NAME).append(", ").append(TABLE_ICONS_NAME);
    queryStr1.append(" where ")
        .append(TABLE_SITE_ICONS_NAME)
        .append(".siteId = ")
        .append(pk.getId());
    queryStr1.append(" and ")
        .append(TABLE_ICONS_NAME)
        .append(".iconsId = ")
        .append(TABLE_SITE_ICONS_NAME)
        .append(".iconsId");

    return executeQuery(queryStr1.toString());
  }

  private List<IconDetail> executeQuery(final String query) throws SQLException {
    final List<IconDetail> result = new ArrayList<>();
    try (Statement stmt = dbConnection.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {
      while (rs.next()) {
        String idIcon = Integer.toString(rs.getInt(1));
        String name = rs.getString(2);
        String description = rs.getString(3);
        String address = rs.getString(4);

        IconDetail icondetail = new IconDetail(idIcon, name, description, address);
        result.add(icondetail);
      }
    }
    return result;
  }

  private String daoGetNextId() {
    int nextid = DBUtil.getNextId(TABLE_SITE_NAME, "siteId");
    return Integer.toString(nextid);
  }

  private Collection<IconDetail> daoGetAllIcons() throws SQLException {
    String queryStr1 =
        "SELECT iconsId, iconsName, iconsDescription, iconsAddress FROM " + TABLE_ICONS_NAME;

    return executeQuery(queryStr1);
  }

  private void daoCreateWebSite(SiteDetail site) throws SQLException {
    String queryStr = "insert into " + TABLE_SITE_NAME + " values (?,?,?,?,?,?,?,?,?,?)";
    try(PreparedStatement stmt = dbConnection.prepareStatement(queryStr)) {
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
        SilverLogger.getLogger(this)
            .error("Cannot save data with query " + queryStr + RESULT_COUNT_MSG_PART + resultCount);
      }
    }
  }

  private void daoAssociateIcons(String id, Collection<String> iconIds) throws SQLException {
    String queryStr = "INSERT INTO " + TABLE_SITE_ICONS_NAME + " VALUES (?,?)";
    try(PreparedStatement stmt = dbConnection.prepareStatement(queryStr)) {
      stmt.setInt(1, Integer.parseInt(id));
      for (final String iconId : iconIds) {
        stmt.setInt(2, Integer.parseInt(iconId));
        int resultCount = stmt.executeUpdate();
        if (resultCount != 1) {
          SilverLogger.getLogger(this)
              .error(
                  "Cannot save data with query " + queryStr + RESULT_COUNT_MSG_PART + resultCount);
        }
      }
    }
  }

  private void daoPublishDepublishSite(String id, int state) throws SQLException {
    String queryStr = "update " + TABLE_SITE_NAME + " set siteState=? where siteId= ?";

    try(PreparedStatement stmt = dbConnection.prepareStatement(queryStr)) {
      stmt.setInt(1, state);
      stmt.setInt(2, Integer.parseInt(id));


      int resultCount = stmt.executeUpdate();
      if (resultCount != 1) {
        SilverLogger.getLogger(this)
            .error(
                "Cannot update data with query " + queryStr + RESULT_COUNT_MSG_PART + resultCount);
      }
    }
  }

  private void daoPublish(Collection<String> siteIds) throws SQLException {
    for (final String id : siteIds) {
      daoPublishDepublishSite(id, 1);
    }
  }

  private void daoDePublish(Collection<String> siteIds) throws SQLException {
    for (final String id : siteIds) {
      daoPublishDepublishSite(id, 0);
    }
  }

  private void daoDeleteAssociateIcons(SitePK pk) throws SQLException {
    String deleteStr = "delete from " + TABLE_SITE_ICONS_NAME + WHERE_SITE_ID_CLAUSE;
    try(PreparedStatement prepStmt = dbConnection.prepareStatement(deleteStr)) {
      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      prepStmt.executeUpdate();
    }
  }

  private void daoDeleteWebSite(SitePK pk) throws SQLException {
    daoDeleteAssociateIcons(pk);
    String deleteStr = "delete from " + TABLE_SITE_NAME + WHERE_SITE_ID_CLAUSE;

    try(PreparedStatement prepStmt = dbConnection.prepareStatement(deleteStr)) {
      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      int resultCount = prepStmt.executeUpdate();
      if (resultCount != 1) {
        SilverLogger.getLogger(this)
            .error(
                "Cannot delete data with query " + deleteStr + RESULT_COUNT_MSG_PART + resultCount);
      }
    }
  }

  private void daoDeleteWebSites(Collection<String> siteIds) throws SQLException {
    ArrayList<String> array = new ArrayList<>(siteIds);
    int i = 0;
    while (i < array.size()) {
      String id = array.get(i);

      SitePK s = new SitePK(id, componentId);
      daoDeleteWebSite(s);
      i++;
    }
  }

  private void daoUpdateWebSite(SiteDetail description) throws SQLException {
    daoDeleteAssociateIcons(description.getSitePK());

    String updateStr =
        "update " + TABLE_SITE_NAME + " set " + "siteName = ?, " + "siteDescription = ?, " +
            "sitePage = ?, " + "siteAuthor = ?, " + "siteDate = ?, " + "siteState = ?, " +
            "popup = ? " + WHERE_SITE_ID_CLAUSE;

    try(PreparedStatement prepStmt = dbConnection.prepareStatement(updateStr)) {
      int i = 1;
      prepStmt.setString(i++, description.getName());
      prepStmt.setString(i++, description.getDescription());
      prepStmt.setString(i++, description.getContentPagePath());
      prepStmt.setString(i++, description.getCreatorId());
      prepStmt.setString(i++, DateUtil.date2SQLDate(description.getCreationDate()));
      prepStmt.setInt(i++, description.getState());
      prepStmt.setInt(i++, description.getPopup());
      prepStmt.setInt(i, Integer.parseInt(description.getSitePK().getId()));

      int resultCount = prepStmt.executeUpdate();
      if (resultCount != 1) {
        SilverLogger.getLogger(this)
            .error("Cannot update site " + description.toString() + " with query " + updateStr +
                RESULT_COUNT_MSG_PART + resultCount);
      }
    }
  }
}
