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
import org.silverpeas.util.DBUtil;
import org.silverpeas.util.DateUtil;
import org.silverpeas.util.exception.UtilException;
import com.stratelia.webactiv.webSites.siteManage.model.IconDetail;
import com.stratelia.webactiv.webSites.siteManage.model.SiteDetail;
import com.stratelia.webactiv.webSites.siteManage.model.SitePK;

public class SiteDAO {

  private Connection dbConnection;
  private static final String tableSiteName = "SC_WebSites_Site";
  private static final String tableIconsName = "SC_WebSites_Icons";
  private static final String tableSiteIconsName = "SC_WebSites_SiteIcons";
  private static final String tablePublicationName = "SB_Publication_Publi";

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
      return (daoGetIdPublication(idSite));
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
      return (daoGetAllWebSite());
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
      return (daoGetWebSite(pk));
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
      return (daoGetWebSites(ids));
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
  public void createWebSite(SiteDetail description) throws SQLException,
      UtilException {
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
  public void associateIcons(String id, Collection<String> liste) throws SQLException,
      UtilException {
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
  public void deleteWebSites(Collection<String> liste) throws SQLException,
      UtilException {
    try {
      dbConnection = openConnection();
      daoDeleteWebSites(liste);
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
      daoUpdateWebSite(description);
    } finally {
      closeConnection(dbConnection);
    }
  }

  /*--------------------------------------------------Methodes de la DAO ------------------------------------------------------------------------------------*/

  /**
   * @param idSite
   * @return
   * @throws SQLException
   */
  private String daoGetIdPublication(String idSite) throws SQLException {
    String idPub = null;

    String queryStr1 = "select pubId from " + tablePublicationName
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
   * @return
   * @throws SQLException
   */
  private Collection<SiteDetail> daoGetAllWebSite() throws SQLException {

    List<SiteDetail> theSiteList = new ArrayList<SiteDetail>();

    Statement stmt = null;
    ResultSet rs1 = null;

    String queryStr1 = "SELECT siteId, siteName, siteDescription, sitePage, siteType, siteAuthor, "
        + "siteDate, siteState, popup FROM "
        + tableSiteName
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
        idSite = Integer.toString(rs1.getInt(1));
        name = rs1.getString(2);
        description = rs1.getString(3);
        page = rs1.getString(4);
        type = rs1.getInt(5);
        author = rs1.getString(6);
        date = rs1.getString(7);
        state = rs1.getInt(8);

        popup = rs1.getInt(9);

        SiteDetail sitedetail =
            new SiteDetail(idSite, componentId, name, description, page, type, author, date, state, popup);

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
    String queryStr1 = "select siteId, siteName, siteDescription, sitePage, siteType, siteAuthor, "
        + "siteDate, siteState, popup from "
        + tableSiteName
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
          new SiteDetail(idSite, componentId, name, description, page, type, author, date, state, popup);
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
    ArrayList<SiteDetail> theSiteList = new ArrayList<SiteDetail>();
    Statement stmt = null;
    ResultSet rs = null;
    int i = 0;
    try {
      String param = "";
      StringBuffer paramBuffer = new StringBuffer();
      String id = null;
      for (int j = 0; j < ids.size(); j++) {
        if (paramBuffer.length() == 0) {
          param = " siteId = ";
        } else {
          param = " or siteId = ";
        }
        id = ids.get(j);
        paramBuffer.append(param).append(id);
      }
      if (ids.size() > 0) {
        StringBuffer queryStr1 = new StringBuffer();
        queryStr1
            .append("select siteId, siteName, siteDescription, sitePage, siteType, siteAuthor, "
                + "siteDate, siteState, popup");

        queryStr1.append(" from ").append(tableSiteName);
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
          idSite = Integer.toString(rs.getInt(i++));
          name = rs.getString(i++);
          description = rs.getString(i++);
          page = rs.getString(i++);
          type = rs.getInt(i++);
          author = rs.getString(i++);
          date = rs.getString(i++);
          state = rs.getInt(i++);
          popup = rs.getInt(i++);
          theSiteList.add(new SiteDetail(idSite, componentId, name, description, page, type, author,
              date, state, popup));
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
    ArrayList<IconDetail> resultat = new ArrayList<IconDetail>();
    IconDetail icondetail;
    Statement stmt = null;
    ResultSet rs1 = null;
    StringBuffer queryStr1 = new StringBuffer();
    queryStr1.append("select ").append(tableIconsName).append(".iconsId, ")
        .append(tableIconsName).append(".iconsName, ").append(tableIconsName)
        .append(".iconsDescription, ").append(tableIconsName).append(
            ".iconsAddress");
    queryStr1.append(" from ").append(tableSiteIconsName).append(", ").append(
        tableIconsName);
    queryStr1.append(" where ").append(tableSiteIconsName).append(".siteId = ")
        .append(pk.getId());
    queryStr1.append(" and ").append(tableIconsName).append(".iconsId = ")
        .append(tableSiteIconsName).append(".iconsId");

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
        idIcon = Integer.valueOf(rs1.getInt(1)).toString();
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
   * @return
   * @throws SQLException
   * @throws UtilException
   */
  private String daoGetNextId() throws SQLException {

    int nextid = DBUtil.getNextId(tableSiteName, "siteId");

    return Integer.valueOf(nextid).toString();
  }

  /**
   * @return
   * @throws SQLException
   */
  private Collection<IconDetail> daoGetAllIcons() throws SQLException {
    ArrayList<IconDetail> resultat = new ArrayList<IconDetail>();
    IconDetail icondetail;
    Statement stmt = null;
    ResultSet rs1 = null;

    String queryStr1 = "SELECT iconsId, iconsName, iconsDescription, iconsAddress FROM "
        + tableIconsName;

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
        idIcon = Integer.valueOf(rs1.getInt(1)).toString();
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
   * @param site
   * @throws SQLException
   */
  private void daoCreateWebSite(SiteDetail site) throws SQLException {
    SilverTrace.info("webSites", "SiteDAO.DAOcreateWebSite()",
        "root.MSG_GEN_PARAM_VALUE", "site = " + site.toString());

    String queryStr = "insert into " + tableSiteName
        + " values (?,?,?,?,?,?,?,?,?,?)";

    PreparedStatement stmt = null;
    try {
      stmt = dbConnection.prepareStatement(queryStr);
      stmt.setInt(1, Integer.parseInt(site.getSitePK().getId()));
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
      if (resultCount != 1) {
        SilverTrace.error("webSites", "SiteDAO.DAOcreateWebSite()",
            "webSites.EX_RECORD_INSERTION_PROBLEM", "query = " + queryStr
                + "resultCount = " + resultCount);
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
  private void daoAssociateIcons(String id, Collection<String> liste)
      throws SQLException {

    String queryStr = "INSERT INTO " + tableSiteIconsName + " VALUES (?,?)";
    PreparedStatement stmt = null;

    try {
      stmt = dbConnection.prepareStatement(queryStr);
      stmt.setInt(1, Integer.parseInt(id));
      String idIcon = "";
      Iterator<String> i = liste.iterator();
      while (i.hasNext()) {
        idIcon = i.next();
        stmt.setInt(2, Integer.parseInt(idIcon));
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
   * @param id
   * @param state
   * @throws SQLException
   */
  private void daoPublishDepublishSite(String id, int state)
      throws SQLException {

    String queryStr = "update " + tableSiteName
        + " set siteState=? where siteId= ?";

    PreparedStatement stmt = null;
    try {
      stmt = dbConnection.prepareStatement(queryStr);
      stmt.setInt(1, state);
      stmt.setInt(2, Integer.parseInt(id));

      SilverTrace.info("webSites", "SiteDAO.DAOpublishDepublishSite()",
          "root.MSG_GEN_PARAM_VALUE", "queryStr = " + queryStr + ", idSite= "
              + id);
      int resultCount = stmt.executeUpdate();
      if (resultCount != 1) {
        SilverTrace.error("webSites", "SiteDAO.DAOpublishDepublishSite()",
            "webSites.EX_RECORD_UPDATE_PROBLEM", "query = " + queryStr
                + "resultCount = " + resultCount);
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
    Iterator<String> i = liste.iterator();
    while (i.hasNext()) {
      daoPublishDepublishSite(i.next(), 1);
    }
  }

  /**
   * @param liste
   * @throws SQLException
   */
  private void daoDePublish(Collection<String> liste) throws SQLException {
    Iterator<String> i = liste.iterator();
    while (i.hasNext()) {
      daoPublishDepublishSite(i.next(), 0);
    }
  }

  /**
   * @param pk
   * @throws SQLException
   */
  private void daoDeleteAssociateIcons(SitePK pk) throws SQLException {

    String deleteStr = "delete from " + tableSiteIconsName
        + " where siteId = ?";

    SilverTrace.info("webSites", "SiteDAO.DAOdeleteAssociateIcons()",
        "root.MSG_GEN_PARAM_VALUE", "queryStr = " + deleteStr + ", idSite= "
            + pk.getId());
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

    String deleteStr = "delete from " + tableSiteName + " where siteId = ?";

    SilverTrace.info("webSites", "SiteDAO.DAOdeleteWebSite()",
        "root.MSG_GEN_PARAM_VALUE", "deleteStr= " + deleteStr + ", idSite= "
            + pk.getId());
    PreparedStatement prepStmt = null;
    try {
      prepStmt = dbConnection.prepareStatement(deleteStr);
      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      int resultCount = prepStmt.executeUpdate();
      if (resultCount != 1) {
        SilverTrace.error("webSites", "SiteDAO.DAOdeleteWebSite()",
            "webSites.EX_RECORD_DELETE_PROBLEM", "deleteStr = " + deleteStr
                + ", resultCount = " + resultCount);
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
    ArrayList<String> array = new ArrayList<String>(liste);
    int i = 0;
    String id = "";
    while (i < array.size()) {
      id = array.get(i);
      SilverTrace.info("webSites", "SiteDAO.DAOdeleteWebSites()",
          "root.MSG_GEN_PARAM_VALUE", "id = " + id);
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

    String updateStr = "update " + tableSiteName + " set " + "siteName = ?, "
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
      prepStmt.setInt(i++, Integer.parseInt(description.getSitePK().getId()));

      int resultCount = prepStmt.executeUpdate();
      if (resultCount != 1) {
        SilverTrace.error("webSites", "SiteDAO.DAOupdateWebSite()",
            "webSites.webSites.EX_RECORD_UPDATE_PROBLEM", "updateStr = "
                + updateStr + ", Site= " + description.toString()
                + ", resultCount = " + resultCount);
      }
    } finally {
      DBUtil.close(prepStmt);
    }
  }
}