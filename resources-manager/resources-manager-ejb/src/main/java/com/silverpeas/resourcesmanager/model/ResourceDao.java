/*
 *  Copyright (C) 2000 - 2011 Silverpeas
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 * 
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.com/legal/licensing"
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.silverpeas.resourcesmanager.model;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResourceDao {

  public void deleteResourceFromCategory(Connection con, String idCategory) throws
          SQLException {
    List<ResourceDetail> listOfResources =
            getResourcesByCategory(con, idCategory);
    try {
      for (ResourceDetail resource : listOfResources) {
        String idResource = resource.getId();
        deleteResource(con, idResource);
      }
    } catch (Exception e) {
      // TODO: handle exception
    }
  }

  /**
   * * Gestion des Ressources **
   */
  ResourceDetail resultSetToResourceDetail(ResultSet rs) throws SQLException {

    boolean book = false;
    int id = rs.getInt("id");
    String instanceId = rs.getString("instanceId");
    String categoryId = rs.getString("categoryid");
    String name = rs.getString("name");
    Date creationDate = new Date(Long.parseLong(rs.getString("creationDate")));
    Date updateDate = new Date(Long.parseLong(rs.getString("updateDate")));
    int bookable = rs.getInt("bookable");
    if (bookable == 1) {
      book = true;
    }
    int responsibleId = rs.getInt("responsibleId");
    int createrId = rs.getInt("createrId");
    int updaterId = rs.getInt("updaterId");
    String description = rs.getString("description");

    ResourceDetail resource = new ResourceDetail(Integer.toString(id),
            categoryId, name, creationDate, updateDate, description, Integer.toString(responsibleId),
            Integer.toString(createrId), Integer.toString(updaterId), instanceId, book);
    return resource;
  }

  public String createResource(Connection con, ResourceDetail resource)
          throws SQLException {
    String query =
            "INSERT INTO SC_Resources_Resource (id, instanceId, categoryId, name, "
            + "creationdate, updatedate, bookable, createrid, updaterid, description) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?)";
    PreparedStatement prepStmt = null;
    String instanceId = resource.getInstanceId();
    String name = resource.getName();
    Date creationdate = resource.getCreationDate();
    Date updatedate = resource.getUpdateDate();
    boolean bookable = resource.getBookable();
    // String responsibleid = resource.getResponsibleId();
    String createrid = resource.getCreaterId();
    String updaterid = resource.getUpdaterId();
    String description = resource.getDescription();
    String idCategory = resource.getCategoryId();
    int id = 0;
    int book = 0;
    if (bookable) {
      book = 1;
    }
    try {
      id = DBUtil.getNextId("SC_Resources_Resource", "id");
      // Preparation de la requête
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, id);
      prepStmt.setString(2, instanceId);
      prepStmt.setInt(3, Integer.parseInt(idCategory));
      prepStmt.setString(4, name);
      prepStmt.setString(5, Long.toString(creationdate.getTime()));
      prepStmt.setString(6, Long.toString(updatedate.getTime()));
      prepStmt.setInt(7, book);
      // prepStmt.setInt(8, Integer.parseInt(responsibleid));
      prepStmt.setString(8, createrid);
      prepStmt.setString(9, updaterid);
      prepStmt.setString(10, description);
      prepStmt.executeUpdate();
      addManagers(con, id, resource.getManagers());
    } catch (UtilException e) {
      throw new SQLException();
    } finally {
      DBUtil.close(prepStmt);
    }
    return Integer.toString(id);
  }

  public void updateResource(Connection con, ResourceDetail resource)
          throws SQLException {
    String query =
            "UPDATE SC_Resources_Resource SET instanceId=?, name=?, updatedate=?, bookable=?, "
            + "updaterid=?, description=?, categoryid=? WHERE id=?";
    PreparedStatement prepStmt = null;

    String id = resource.getId();
    String instanceId = resource.getInstanceId();
    String name = resource.getName();
    Date updatedate = resource.getUpdateDate();
    boolean bookable = resource.getBookable();
    String updaterid = resource.getUpdaterId();
    String description = resource.getDescription();
    String categoryId = resource.getCategoryId();
    int idresource = Integer.parseInt(id);

    try {
      // Preparation de la requête
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, name);
      prepStmt.setString(3, Long.toString(updatedate.getTime()));
      if (bookable) {
        prepStmt.setInt(4, 1);
      } else {
        prepStmt.setInt(4, 0);
      }
      prepStmt.setString(5, updaterid);
      prepStmt.setString(6, description);
      prepStmt.setInt(7, Integer.parseInt(categoryId));
      prepStmt.setInt(8, idresource);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public List<ResourceDetail> getResourcesByCategory(Connection con, String categoryId)
          throws SQLException {
    List<ResourceDetail> list = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    String query =
            "select id,name,creationDate,updateDate,bookable,responsibleId,"
            + "createrId,updaterId,description,categoryid "
            + "from SC_Resources_Resource where categoryid = ?";
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(categoryId));
      rs = prepStmt.executeQuery();
      list = returnArrayListofReservationResource(con, rs, false);
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }


  private static final String SELECT_RESOURCE = "SELECT * FROM sc_resources_resource WHERE id = ?";

  public ResourceDetail getResource(Connection con, String id) throws SQLException {
    PreparedStatement prepStmt = con.prepareStatement(SELECT_RESOURCE);
    ResultSet rs = null;
    try {
      prepStmt.setInt(1, Integer.parseInt(id));
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        ResourceDetail resource = resultSetToResourceDetail(rs);
        List<String> managers = getManagers(con, Integer.parseInt(id));
        resource.setManagers(managers);
        return resource;
      }
      return null;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  private static final String DELETE_RESOURCE = "DELETE FROM sc_resources_resource WHERE id = ?";

  public void deleteResource(Connection con, String id) throws SQLException {
    ResourcesManagerDAO.deleteReservedResource(con, id);
    PreparedStatement prepStmt = con.prepareStatement(DELETE_RESOURCE);
    try {
      prepStmt.setInt(1, Integer.parseInt(id));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  private List<ResourceDetail> returnArrayListofReservationResource(Connection con,
          ResultSet rs, boolean resa) throws SQLException {
    int id;
    String instanceId = "";
    String name = "";
    int categoryId = 0;
    Date creationDate = null;
    Date updateDate = null;
    String description = "";
    int responsibleId = 0;
    int createrId = 0;
    int updaterId = 0;
    String status = "";
    List<ResourceDetail> list = new ArrayList<ResourceDetail>();
    boolean book = false;
    while (rs.next()) {
      id = rs.getInt("id");
      name = rs.getString("name");
      creationDate = new Date(Long.parseLong(rs.getString("creationDate")));
      updateDate = new Date(Long.parseLong(rs.getString("updateDate")));
      int bookable = rs.getInt("bookable");
      responsibleId = rs.getInt("responsibleId");
      createrId = rs.getInt("createrId");
      updaterId = rs.getInt("updaterId");
      description = rs.getString("description");
      categoryId = rs.getInt("categoryid");
      if (resa) {
        status = rs.getString("status");
      }
      book = (bookable == 1);

      ResourceDetail resource = new ResourceDetail(Integer.toString(id),
              Integer.toString(categoryId), name, creationDate, updateDate,
              description, Integer.toString(responsibleId), Integer.toString(createrId), Integer.
              toString(updaterId), instanceId,
              book, status);
      List<String> managers = getManagers(con, id);
      resource.setManagers(managers);
      list.add(resource);
    }
    return list;
  }

  public List<ResourceDetail> getResourcesofReservation(Connection con,
          String instanceId, String reservationId) throws SQLException {
    List<ResourceDetail> list = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String query =
            "select A.id, A.categoryid, A.name, A.creationdate, A.updatedate, A.bookable, "
            + "A.responsibleid, A.createrid, A.updaterid, A.description, B.status "
            + "from SC_Resources_Resource A, SC_Resources_ReservedResource B "
            + "where A.id=B.resourceId AND B.reservationId=? AND A.instanceId=?";
    int idReservation = Integer.parseInt(reservationId);
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, idReservation);
      prepStmt.setString(2, instanceId);
      rs = prepStmt.executeQuery();
      list = returnArrayListofReservationResource(con, rs, true);

    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public void addManagers(Connection con, int resourceId, List<String> managerIds)
          throws SQLException {
    if (managerIds != null && !managerIds.isEmpty()) {
      for (String managerId : managerIds) {
        addManager(con, resourceId, Integer.parseInt(managerId));
      }
    }
  }

  private static final String ADD_MANAGER = "INSERT INTO sc_resources_managers (resourceId, managerId) VALUES (?, ?)";
  public void addManager(Connection con, int resourceId, int managerId) throws SQLException {
    PreparedStatement prepStmt = con.prepareStatement(ADD_MANAGER);
    try {
      prepStmt.setInt(1, resourceId);
      prepStmt.setInt(2, managerId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  private static final String REMOVE_RESOURCE_MANAGERS = "DELETE FROM sc_resources_managers WHERE resourceId = ?";
  public void removeAllManagers(Connection con, int resourceId) throws SQLException {
    PreparedStatement prepStmt = con.prepareStatement(REMOVE_RESOURCE_MANAGERS);
    try {
      prepStmt.setInt(1, resourceId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public void removeManager(Connection con, int resourceId, int managerId)
          throws SQLException {
    String query = "DELETE FROM SC_Resources_Managers WHERE resourceId = ? AND managerId = ?";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, resourceId);
      prepStmt.setInt(2, managerId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public List<String> getManagers(Connection con, int resourceId) throws SQLException {
    List<String> managers = new ArrayList<String>();
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String query = "SELECT managerId FROM SC_Resources_Managers WHERE resourceId = ? ";
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, resourceId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        String managerId = rs.getString("managerId");
        managers.add(managerId);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return managers;
  }
}