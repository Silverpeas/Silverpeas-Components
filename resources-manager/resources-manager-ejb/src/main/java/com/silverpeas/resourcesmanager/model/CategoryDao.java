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

/**
 *
 * @author ehugonnet
 */
public class CategoryDao {

  /**
   *
   * @param rs
   * @return
   * @throws SQLException
   */
  CategoryDetail resultSetToCategoryDetail(ResultSet rs) throws SQLException {
    int id = rs.getInt("id");
    String name = rs.getString("name");
    Date creationDate = new Date(Long.parseLong(rs.getString("creationDate")));
    Date updateDate = new Date(Long.parseLong(rs.getString("updateDate")));
    String instanceId = rs.getString("instanceId");
    boolean bookable = (rs.getInt("bookable") == 1);
    String form = rs.getString("form");
    int responsibleId = rs.getInt("responsibleId");
    int createrId = rs.getInt("createrId");
    int updaterId = rs.getInt("updaterId");
    String description = rs.getString("description");
    CategoryDetail category = new CategoryDetail(Integer.toString(id), instanceId, name,
            creationDate, updateDate, bookable, form, Integer.toString(responsibleId), Integer.
            toString(createrId), Integer.toString(updaterId), description);
    return category;
  }

  public int createCategory(Connection con, CategoryDetail category)
          throws SQLException {
    String query =
            "INSERT INTO SC_Resources_Category (id, instanceId, name, creationdate, updatedate,"
            + "bookable, form, responsibleid, createrid, updaterid, description) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    PreparedStatement prepStmt = null;
    String instanceId = category.getInstanceId();
    String name = category.getName();
    Date creationdate = category.getCreationDate();
    if (creationdate == null) {
      creationdate = new Date();
    }
    Date updatedate = category.getUpdateDate();
    if (updatedate == null) {
      updatedate = creationdate;
    }
    boolean bookable = category.getBookable();
    String form = category.getForm();
    String responsibleid = category.getResponsibleId();
    String createrid = category.getCreaterId();
    String updaterid = category.getUpdaterId();
    String description = category.getDescription();

    try {
      int id = DBUtil.getNextId("SC_Resources_Category", "id");
      // Preparation de la requête
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, id);
      prepStmt.setString(2, instanceId);
      prepStmt.setString(3, name);
      prepStmt.setString(4, Long.toString(creationdate.getTime()));
      prepStmt.setString(5, Long.toString(updatedate.getTime()));
      if (!bookable) {
        prepStmt.setInt(6, 0);
      } else {
        prepStmt.setInt(6, 1);
      }
      prepStmt.setString(7, form);
      prepStmt.setInt(8, Integer.parseInt(responsibleid));
      prepStmt.setString(9, createrid);
      prepStmt.setString(10, updaterid);
      prepStmt.setString(11, description);
      prepStmt.executeUpdate();
      return id;
    } catch (UtilException e) {
      throw new SQLException();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public void updateCategory(Connection con, CategoryDetail category)
          throws SQLException {
    String query =
            "UPDATE SC_Resources_Category SET instanceId=?, name=?, updatedate=?, bookable=?, "
            + "form=?, responsibleid=?, updaterid=?, description=? WHERE id=?";
    PreparedStatement prepStmt = null;
    boolean bookable = category.getBookable();
    int book = 0;
    if (bookable) {
      book = 1;
    }
    try {
      // Preparation de la requête
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, category.getInstanceId());
      prepStmt.setString(2, category.getName());
      prepStmt.setString(3, Long.toString(category.getUpdateDate().getTime()));
      prepStmt.setInt(4, book);
      prepStmt.setString(5, category.getForm());
      prepStmt.setInt(6, Integer.parseInt(category.getResponsibleId()));
      prepStmt.setString(7, category.getUpdaterId());
      prepStmt.setString(8, category.getDescription());
      prepStmt.setInt(9, Integer.parseInt(category.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public List<CategoryDetail> getCategories(Connection con, String instanceId)
          throws SQLException {
    List<CategoryDetail> list = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    String query = "select * from SC_Resources_Category where instanceId = ?";
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);

      rs = prepStmt.executeQuery();
      list = new ArrayList<CategoryDetail>();
      while (rs.next()) {
        CategoryDetail category = resultSetToCategoryDetail(rs);
        list.add(category);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return list;
  }

  public CategoryDetail getCategory(Connection con, String id) throws SQLException {
    String query = "select * from SC_Resources_Category WHERE id= ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    CategoryDetail category = null;
    try {

      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(id));
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        category = resultSetToCategoryDetail(rs);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return category;
  }

  public void deleteCategory(Connection con, String id)
          throws SQLException {
    PreparedStatement prepStmt = null;
    String query = "DELETE FROM SC_Resources_Category WHERE ID=?";
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(id));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }
}
