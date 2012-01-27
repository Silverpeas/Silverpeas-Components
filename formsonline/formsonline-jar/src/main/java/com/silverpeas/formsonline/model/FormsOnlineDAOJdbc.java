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

package com.silverpeas.formsonline.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

public class FormsOnlineDAOJdbc implements FormsOnlineDAO {

  // General infos
  private static final String DB_NAME = JNDINames.SILVERPEAS_DATASOURCE;
  private static final String FORMS_TABLENAME = "SC_FormsOnline_Forms";
  private static final String FORMS_INSTANCES_TABLENAME = "SC_FormsOnline_FormInstances";
  private static final String USER_RIGHTS_TABLENAME = "SC_FormsOnline_UserRights";
  private static final String GROUP_RIGHTS_TABLENAME = "SC_FormsOnline_GroupRights";

  // Queries about Forms
  private static final String QUERY_FIND_FORMS =
      "select * from " + FORMS_TABLENAME + " where instanceId = ?";
  private static final String QUERY_LOAD_FORM =
      "select * from " + FORMS_TABLENAME + " where instanceId = ? and id = ?";
  private static final String QUERY_INSERT_FORM =
      "INSERT INTO " +
          FORMS_TABLENAME +
          "( "
          +
          "id, xmlFormName, name, description, title, creatorId, creationDate, state, alreadyUsed, instanceId) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  private static final String QUERY_UPDATE_FORM =
      "update " +
          FORMS_TABLENAME
          +
          " set xmlFormName = ?, name = ?, description = ?, title = ?, creatorId = ?, creationDate = ?, state = ?, alreadyUsed = ? where instanceId = ? and id= ? ";
  private static final String QUERY_DELETE_FORM = "delete from " + FORMS_TABLENAME
      + " where instanceId = ? and id = ? ";

  // Queries about Forms instances
  private static final String QUERY_FIND_FORMS_INSTANCES =
      "select * from " + FORMS_INSTANCES_TABLENAME +
      " where instanceId = ? and formId = ? and creatorId = ? and not state = " +
      FormInstance.STATE_ARCHIVED + " order by creationDate desc";
  private static final String QUERY_LOAD_FORM_INSTANCE =
      "select * from " + FORMS_INSTANCES_TABLENAME + " where instanceId = ? and id = ?";
  private static final String QUERY_UPDATE_FORM_INSTANCE =
      "update " +
          FORMS_INSTANCES_TABLENAME
          +
          " set formId = ?, state = ?, creatorId = ?, creationDate = ?, validatorId = ?, validationDate = ?, comments = ?, instanceId = ? where id = ? ";
  private static final String QUERY_INSERT_FORMINSTANCE =
      "INSERT INTO " +
          FORMS_INSTANCES_TABLENAME +
          "( "
          +
          "id, formId, state, creatorId, creationDate, validatorId, validationDate, comments, instanceId) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";;
  private static final String QUERY_DELETE_FORM_INSTANCE =
      "delete from " + FORMS_INSTANCES_TABLENAME + " where instanceId = ? and id = ? ";

  // Queries about Rights
  private static final String QUERY_LOAD_USER_RIGHTS =
      "select * from " + USER_RIGHTS_TABLENAME +
      " where instanceId = ? and formId = ? and rightType = ?";
  private static final String QUERY_LOAD_GROUP_RIGHTS =
      "select * from " + GROUP_RIGHTS_TABLENAME +
      " where instanceId = ? and formId = ? and rightType = ?";
  private static final String QUERY_REMOVE_USER_RIGHTS =
      "delete from " + USER_RIGHTS_TABLENAME +
      " where instanceId = ? and formId = ? and rightType = ?";
  private static final String QUERY_REMOVE_GROUP_RIGHTS =
      "delete from " + GROUP_RIGHTS_TABLENAME +
      " where instanceId = ? and formId = ? and rightType = ?";
  private static final String QUERY_INSERT_USER_RIGHTS =
      "INSERT INTO " + USER_RIGHTS_TABLENAME + "(formId, instanceId, rightType, userId) "
      + "VALUES (?, ?, ?, ?)";
  private static final String QUERY_INSERT_GROUP_RIGHTS =
      "INSERT INTO " + GROUP_RIGHTS_TABLENAME + "(formId, instanceId, rightType, groupId) "
      + "VALUES (?, ?, ?, ?)";

  public FormDetail createForm(FormDetail formDetail) throws FormsOnlineDatabaseException {
    Connection con = getConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(QUERY_INSERT_FORM);
      int id = DBUtil.getNextId(FORMS_TABLENAME, "id");
      stmt.setInt(1, id);
      stmt.setString(2, formDetail.getXmlFormName());
      stmt.setString(3, formDetail.getName());
      stmt.setString(4, formDetail.getDescription());
      stmt.setString(5, formDetail.getTitle());
      stmt.setString(6, formDetail.getCreatorId());
      prepareDateStatement(stmt, 7, formDetail.getCreationDate());
      stmt.setInt(8, formDetail.getState());
      stmt.setInt(9, (formDetail.isAlreadyUsed()) ? 1 : 0);
      stmt.setString(10, formDetail.getInstanceId());

      stmt.executeUpdate();
      formDetail.setId(id);

      return formDetail;
    } catch (UtilException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.createForm()",
          SilverpeasException.ERROR, "formsOnline.INSERTING_FORM_FAILED", se);
    } catch (SQLException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.createForm()",
          SilverpeasException.ERROR, "formsOnline.INSERTING_FORM_FAILED", se);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(con);
    }
  }

  public FormDetail deleteForm(String instanceId, int formId) throws FormsOnlineDatabaseException {
    FormDetail form = getForm(instanceId, formId);
    Connection con = getConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      removeGroupRights(con, formId, instanceId, "S");
      removeUserRights(con, formId, instanceId, "S");
      removeGroupRights(con, formId, instanceId, "R");
      removeUserRights(con, formId, instanceId, "R");

      stmt = con.prepareStatement(QUERY_DELETE_FORM);
      stmt.setString(1, instanceId);
      stmt.setInt(2, formId);

      stmt.executeUpdate();
      return form;
    } catch (SQLException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.deleteForm()",
          SilverpeasException.ERROR, "formsOnline.DELETE_FORM_FAILED", "instanceId=" + instanceId +
          ", formId=" + formId, se);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(con);
    }
  }

  public FormDetail getForm(String instanceId, int formId) throws FormsOnlineDatabaseException {
    Connection con = getConnection();
    FormDetail form = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(QUERY_LOAD_FORM);
      stmt.setString(1, instanceId);
      stmt.setInt(2, formId);
      rs = stmt.executeQuery();

      if (rs.next()) {
        form = fetchFormDetail(rs);
      }
    } catch (SQLException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.getForm()",
          SilverpeasException.ERROR, "formsOnline.GET_FORM_FAILED", "instanceId = " + instanceId +
          ", formId=" + formId, se);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(con);
    }
    return form;
  }

  public List<FormDetail> findAllForms(String instanceId) throws FormsOnlineDatabaseException {
    Connection con = getConnection();
    List<FormDetail> forms = new ArrayList<FormDetail>();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(QUERY_FIND_FORMS);
      stmt.setString(1, instanceId);
      rs = stmt.executeQuery();

      while (rs.next()) {
        FormDetail form = fetchFormDetail(rs);
        forms.add(form);
      }
    } catch (SQLException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.findAllForms()",
          SilverpeasException.ERROR, "formsOnline.GET_ALL_FORMS_FAILED", "instanceId = " +
          instanceId, se);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(con);
    }
    return forms;
  }

  public void updateForm(FormDetail formDetail) throws FormsOnlineDatabaseException {
    Connection con = getConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(QUERY_UPDATE_FORM);
      stmt.setString(1, formDetail.getXmlFormName());
      stmt.setString(2, formDetail.getName());
      stmt.setString(3, formDetail.getDescription());
      stmt.setString(4, formDetail.getTitle());
      stmt.setString(5, formDetail.getCreatorId());
      prepareDateStatement(stmt, 6, formDetail.getCreationDate());
      stmt.setInt(7, formDetail.getState());
      stmt.setInt(8, (formDetail.isAlreadyUsed()) ? 1 : 0);
      stmt.setString(9, formDetail.getInstanceId());
      stmt.setInt(10, formDetail.getId());

      stmt.executeUpdate();
    } catch (SQLException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.updateForm()",
          SilverpeasException.ERROR, "formsOnline.UPDATE_FORM_FAILED", se);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(con);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.formsonline.model.FormsOnlineDAO#getReceiversAsGroups(int,
   * java.lang.String)
   */
  public List<String> getReceiversAsGroups(int id, String instanceId)
      throws FormsOnlineDatabaseException {
    return getGroupRights(id, instanceId, "R");
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.formsonline.model.FormsOnlineDAO#getReceiversAsUsers(int, java.lang.String)
   */
  public List<String> getReceiversAsUsers(int id, String instanceId)
      throws FormsOnlineDatabaseException {
    return getUserRights(id, instanceId, "R");
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.formsonline.model.FormsOnlineDAO#getSendersAsGroups(int, java.lang.String)
   */
  public List<String> getSendersAsGroups(int id, String instanceId)
      throws FormsOnlineDatabaseException {
    return getGroupRights(id, instanceId, "S");
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.formsonline.model.FormsOnlineDAO#getSendersAsUsers(int, java.lang.String)
   */
  public List<String> getSendersAsUsers(int id, String instanceId)
      throws FormsOnlineDatabaseException {
    return getUserRights(id, instanceId, "S");
  }

  private List<String> getGroupRights(int formId, String instanceId, String rightType)
      throws FormsOnlineDatabaseException {
    Connection con = getConnection();
    List<String> groupsIds = new ArrayList<String>();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(QUERY_LOAD_GROUP_RIGHTS);
      stmt.setString(1, instanceId);
      stmt.setInt(2, formId);
      stmt.setString(3, rightType);
      rs = stmt.executeQuery();

      while (rs.next()) {
        groupsIds.add(rs.getString("groupId"));
      }
    } catch (SQLException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.getGroupRights()",
          SilverpeasException.ERROR, "formsOnline.FIND_GROUP_RIGHTS", "instanceId = " + instanceId +
          ", formId = " + formId, se);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(con);
    }
    return groupsIds;
  }

  private List<String> getUserRights(int formId, String instanceId, String rightType)
      throws FormsOnlineDatabaseException {
    Connection con = getConnection();
    List<String> userIds = new ArrayList<String>();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(QUERY_LOAD_USER_RIGHTS);
      stmt.setString(1, instanceId);
      stmt.setInt(2, formId);
      stmt.setString(3, rightType);
      rs = stmt.executeQuery();

      while (rs.next()) {
        userIds.add(rs.getString("userId"));
      }
    } catch (SQLException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.getUserRights()",
          SilverpeasException.ERROR, "formsOnline.FIND_USER_RIGHTS", "instanceId = " + instanceId +
          ", formId = " + formId, se);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(con);
    }
    return userIds;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.formsonline.model.FormsOnlineDAO#updateReceivers(int, java.lang.String,
   * java.lang.String[], java.lang.String[])
   */
  public void updateReceivers(int id, String componentId,
      String[] newUserReceiverIds, String[] newGroupReceiverIds)
      throws FormsOnlineDatabaseException {
    updateRights(id, componentId, newUserReceiverIds, newGroupReceiverIds, "R");
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.formsonline.model.FormsOnlineDAO#updateSenders(int, java.lang.String,
   * java.lang.String[], java.lang.String[])
   */
  public void updateSenders(int id, String instanceId,
      String[] newUserSenderIds, String[] newGroupSenderIds) throws FormsOnlineDatabaseException {
    updateRights(id, instanceId, newUserSenderIds, newGroupSenderIds, "S");
  }

  private void updateRights(int id, String instanceId,
      String[] newUserIds, String[] newGroupIds, String rightType)
      throws FormsOnlineDatabaseException {
    Connection con = getConnection();
    try {
      con.setAutoCommit(false);
      removeGroupRights(con, id, instanceId, rightType);
      removeUserRights(con, id, instanceId, rightType);
      for (int i = 0; i < newUserIds.length; i++) {
        addUserRights(con, id, instanceId, newUserIds[i], rightType);
      }
      for (int i = 0; i < newGroupIds.length; i++) {
        addGroupRights(con, id, instanceId, newGroupIds[i], rightType);
      }
      con.commit();
    } catch (Exception e) {
      try {
        con.rollback();
      } catch (SQLException e1) {
      }
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.updateRights()",
          SilverpeasException.ERROR, "formsOnline.UPDATE_RIGHTS_FAILED", e);
    } finally {
      try {
        con.setAutoCommit(true);
      } catch (SQLException e1) {
      }
      freeConnection(con);
    }
  }

  private void addUserRights(Connection con, int formId, String instanceId,
      String userId, String rightType) throws SQLException {
    PreparedStatement stmt = null;
    try {
      stmt = con.prepareStatement(QUERY_INSERT_USER_RIGHTS);
      stmt.setInt(1, formId);
      stmt.setString(2, instanceId);
      stmt.setString(3, rightType);
      stmt.setString(4, userId);

      stmt.executeUpdate();
    } finally {
      DBUtil.close(stmt);
    }
  }

  private void addGroupRights(Connection con, int formId, String instanceId,
      String groupId, String rightType) throws SQLException {
    PreparedStatement stmt = null;
    try {
      stmt = con.prepareStatement(QUERY_INSERT_GROUP_RIGHTS);
      stmt.setInt(1, formId);
      stmt.setString(2, instanceId);
      stmt.setString(3, rightType);
      stmt.setString(4, groupId);

      stmt.executeUpdate();
    } finally {
      DBUtil.close(stmt);
    }
  }

  private void removeGroupRights(Connection con, int formId, String instanceId,
      String rightType) throws SQLException {
    PreparedStatement stmt = null;
    try {
      stmt = con.prepareStatement(QUERY_REMOVE_GROUP_RIGHTS);
      stmt.setString(1, instanceId);
      stmt.setInt(2, formId);
      stmt.setString(3, rightType);

      stmt.executeUpdate();
    } finally {
      DBUtil.close(stmt);
    }
  }

  private void removeUserRights(Connection con, int formId, String instanceId,
      String rightType) throws SQLException {
    PreparedStatement stmt = null;
    try {
      stmt = con.prepareStatement(QUERY_REMOVE_USER_RIGHTS);
      stmt.setString(1, instanceId);
      stmt.setInt(2, formId);
      stmt.setString(3, rightType);

      stmt.executeUpdate();
    } finally {
      DBUtil.close(stmt);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.formsonline.model.FormsOnlineDAO#getUserAvailableForms(java.lang.String,
   * java.lang.String, java.lang.String[])
   */
  public List<FormDetail> getUserAvailableForms(String instanceId, String userId,
      String[] userGroupIds) throws FormsOnlineDatabaseException {

    /* Build query */
    StringBuffer query = new StringBuffer();

    query.append("select * from ").append(FORMS_TABLENAME);

    /* 1st criteria : correct instanceId */
    query.append(" where instanceId = '").append(instanceId).append("'");

    /* 2nd criteria : state published or old forms that user has sent before unpublish */
    query.append(" and (")
        .append(" state = ").append(FormDetail.STATE_PUBLISHED)
        .append(" or id in (select formId from ").append(FORMS_INSTANCES_TABLENAME).append(
        " where creatorId = '").append(userId).append("')")
        .append(" )");

    /* 3rd criteria : user has receiver rights (directly or from a group) */
    query.append(" and (")
        .append(" id in (select formId from ").append(USER_RIGHTS_TABLENAME).append(
        " where rightType='S' and userId = '").append(userId).append("') ");
    if ((userGroupIds != null) && (userGroupIds.length > 0)) {
      query.append("or id in (select formId from " + GROUP_RIGHTS_TABLENAME +
          " where rightType='S' and groupId in ( ");
      for (int i = 0; i < userGroupIds.length; i++) {
        if (i != 0) {
          query.append(", ");
        }
        query.append("'").append(userGroupIds[i]).append("'");
      }
      query.append(") )");
    }
    query.append(" )");

    /* launch query */
    Connection con = getConnection();
    List<FormDetail> forms = new ArrayList<FormDetail>();
    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(query.toString());

      while (rs.next()) {
        FormDetail form = fetchFormDetail(rs);
        forms.add(form);
      }
    } catch (SQLException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.getUserAvailableForms()",
          SilverpeasException.ERROR, "formsOnline.FIND_USER_AVAILABLE_FORMS", "instanceId = " +
          instanceId + ",userId = " + userId, se);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(con);
    }
    return forms;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.formsonline.model.FormsOnlineDAO#getForms(java.util.List)
   */
  public List<FormDetail> getForms(List<String> formIds) throws FormsOnlineDatabaseException {

    List<FormDetail> forms = new ArrayList<FormDetail>();
    if ((formIds == null) || (formIds.isEmpty())) {
      return forms;
    }

    /* Build query */
    StringBuffer query = new StringBuffer("select * from " + FORMS_TABLENAME + " where id in (");
    int pos = 0;
    Iterator<String> it = formIds.iterator();
    while (it.hasNext()) {
      if (pos++ != 0) {
        query.append(", ");
      }
      query.append(it.next());
    }
    query.append(")");

    /* launch query */
    Connection con = getConnection();
    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(query.toString());

      while (rs.next()) {
        FormDetail form = fetchFormDetail(rs);
        forms.add(form);
      }
    } catch (SQLException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.getForms()",
          SilverpeasException.ERROR, "formsOnline.FIND_LOAD_FORMS", se);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(con);
    }
    return forms;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.formsonline.model.FormsOnlineDAO#getSentFormInstances(java.lang.String,
   * int, java.lang.String)
   */
  public List<FormInstance> getSentFormInstances(String instanceId, int formId, String userId)
      throws FormsOnlineDatabaseException {
    Connection con = getConnection();
    List<FormInstance> formInstances = new ArrayList<FormInstance>();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(QUERY_FIND_FORMS_INSTANCES);
      stmt.setString(1, instanceId);
      stmt.setInt(2, formId);
      stmt.setString(3, userId);
      rs = stmt.executeQuery();

      while (rs.next()) {
        FormInstance instance = fetchFormInstance(rs);
        formInstances.add(instance);
      }
    } catch (SQLException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.getSentFormInstances()",
          SilverpeasException.ERROR, "formsOnline.FIND_SENT_FORM_INSTANCE_FAILED", "instanceId = " +
          instanceId + ", userId = " + userId, se);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(con);
    }
    return formInstances;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.formsonline.model.FormsOnlineDAO#getSentFormInstances(java.lang.String,
   * java.lang.String)
   */
  public List<FormInstance> getReceivedFormInstances(String instanceId, String userId, int formId)
      throws FormsOnlineDatabaseException {

    /*
     * then retrieve instances where : - user has been the validator - no validation has been done
     * yet and formid in available form ids
     */
    Connection con = getConnection();
    List<FormInstance> formInstances = new ArrayList<FormInstance>();
    Statement stmt = null;
    ResultSet rs = null;

    /* first get forms whose user can be receiver */
    StringBuffer query = new StringBuffer("select * from ");
    query.append(FORMS_INSTANCES_TABLENAME).append(" where instanceId = '").append(instanceId)
        .append("' and formId = ").append(formId).append(" and (validatorId ='").append(userId)
        .append("' ");
    query.append(" or ( validatorId is null )");
    query.append(") order by creationDate desc");

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(query.toString());

      while (rs.next()) {
        FormInstance instance = fetchFormInstance(rs);
        formInstances.add(instance);
      }
    } catch (SQLException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.getReceivedFormInstances()",
          SilverpeasException.ERROR, "formsOnline.FIND_RECEIVED_FORM_INSTANCE_FAILED",
          "instanceId = " + instanceId + ", userId = " + userId, se);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(con);
    }

    return formInstances;
  }

  public List<String> getAvailableFormIdsAsReceiver(String instanceId, String userId,
      String[] userGroupIds) throws FormsOnlineDatabaseException {
    Connection con = getConnection();
    List<String> availableFormIds = new ArrayList<String>();
    Statement stmt = null;
    ResultSet rs = null;

    /* build query */
    StringBuffer query =
        new StringBuffer("select distinct id from " + FORMS_TABLENAME + " where instanceId = '" +
        instanceId + "' and id in (select formId from " + USER_RIGHTS_TABLENAME +
        " where rightType='R' and userId = '" + userId + "') ");
    if ((userGroupIds != null) && (userGroupIds.length > 0)) {
      query.append("or id in (select formId from " + GROUP_RIGHTS_TABLENAME +
          " where rightType='R' and groupId in ( ");
      for (int i = 0; i < userGroupIds.length; i++) {
        if (i != 0) {
          query.append(", ");
        }
        query.append("'").append(userGroupIds[i]).append("'");
      }
      query.append(") )");
    }

    try {
      stmt = con.createStatement();
      rs = stmt.executeQuery(query.toString());

      while (rs.next()) {
        availableFormIds.add(String.valueOf(rs.getInt("id")));
      }
    } catch (SQLException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.getAvailableFormIds()",
          SilverpeasException.ERROR, "formsOnline.FIND_AVAILABLE_FORM_IDS_FAILED", "instanceId = " +
          instanceId + ", userId = " + userId, se);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(con);
    }

    return availableFormIds;
  }

  private FormDetail fetchFormDetail(ResultSet rs) throws SQLException {
    FormDetail form = new FormDetail();
    form.setId(rs.getInt("id"));
    form.setXmlFormName(rs.getString("xmlFormName"));
    form.setName(rs.getString("name"));
    form.setDescription(rs.getString("description"));
    form.setTitle(rs.getString("title"));
    form.setCreatorId(rs.getString("creatorId"));
    form.setCreationDate(rs.getTimestamp("creationDate"));
    form.setInstanceId(rs.getString("instanceId"));
    form.setState(rs.getInt("state"));
    form.setAlreadyUsed((rs.getInt("alreadyUsed") == 0) ? false : true);

    return form;
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.formsonline.model.FormsOnlineDAO#createInstance(com.silverpeas.formsonline.model
   * .FormInstance)
   */
  public FormInstance createInstance(FormInstance instance)
      throws FormsOnlineDatabaseException {
    Connection con = getConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(QUERY_INSERT_FORMINSTANCE);
      int id = DBUtil.getNextId(FORMS_INSTANCES_TABLENAME, "id");
      stmt.setInt(1, id);
      stmt.setInt(2, instance.getFormId());
      stmt.setInt(3, instance.getState());
      stmt.setString(4, instance.getCreatorId());
      prepareDateStatement(stmt, 5, instance.getCreationDate());
      stmt.setString(6, instance.getValidatorId());
      prepareDateStatement(stmt, 7, instance.getValidationDate());
      stmt.setString(8, instance.getComments());
      stmt.setString(9, instance.getInstanceId());

      stmt.executeUpdate();
      instance.setId(id);

      return instance;
    } catch (UtilException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.createInstance()",
          SilverpeasException.ERROR, "formsOnline.INSERTING_FORMINSTANCE_FAILED", se);
    } catch (SQLException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.createInstance()",
          SilverpeasException.ERROR, "formsOnline.INSERTING_FORMINSTANCE_FAILED", se);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(con);
    }
  }

  public FormInstance getFormInstance(String instanceId, int formInstanceId)
      throws FormsOnlineDatabaseException {
    Connection con = getConnection();
    FormInstance formInstance = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(QUERY_LOAD_FORM_INSTANCE);
      stmt.setString(1, instanceId);
      stmt.setInt(2, formInstanceId);
      rs = stmt.executeQuery();

      if (rs.next()) {
        formInstance = fetchFormInstance(rs);
      }
    } catch (SQLException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.getFormInstance()",
          SilverpeasException.ERROR, "formsOnline.CREATE_FORM_FAILED", "instanceId = " +
          instanceId + ", formInstanceId=" + formInstanceId, se);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(con);
    }
    return formInstance;
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.formsonline.model.FormsOnlineDAO#updateFormInstance(com.silverpeas.formsonline
   * .model.FormInstance)
   */
  public void updateFormInstance(FormInstance formInstance)
      throws FormsOnlineDatabaseException {
    Connection con = getConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(QUERY_UPDATE_FORM_INSTANCE);
      stmt.setInt(1, formInstance.getFormId());
      stmt.setInt(2, formInstance.getState());
      stmt.setString(3, formInstance.getCreatorId());
      prepareDateStatement(stmt, 4, formInstance.getCreationDate());
      stmt.setString(5, formInstance.getValidatorId());
      prepareDateStatement(stmt, 6, formInstance.getValidationDate());
      stmt.setString(7, formInstance.getComments());
      stmt.setString(8, formInstance.getInstanceId());
      stmt.setInt(9, formInstance.getId());

      stmt.executeUpdate();
    } catch (SQLException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.updateFormInstance()",
          SilverpeasException.ERROR, "formsOnline.UPDATE_FORM_INSTANCE_FAILED", se);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(con);
    }
  }

  public void deleteFormInstance(String instanceId, int formInstanceId)
      throws FormsOnlineDatabaseException {
    Connection con = getConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.prepareStatement(QUERY_DELETE_FORM_INSTANCE);
      stmt.setString(1, instanceId);
      stmt.setInt(2, formInstanceId);

      stmt.executeUpdate();
    } catch (SQLException se) {
      throw new FormsOnlineDatabaseException("FormsOnlineDAOJdbc.deleteFormInstance()",
          SilverpeasException.ERROR, "formsOnline.DELETE_FORM_FAILED", "instanceId=" + instanceId +
          ", formInstanceId=" + formInstanceId, se);
    } finally {
      DBUtil.close(rs, stmt);
      freeConnection(con);
    }
  }

  private FormInstance fetchFormInstance(ResultSet rs) throws SQLException {
    FormInstance formInstance = new FormInstance();

    formInstance.setId(rs.getInt("id"));
    formInstance.setFormId(rs.getInt("formId"));
    formInstance.setState(rs.getInt("state"));
    formInstance.setCreatorId(rs.getString("creatorId"));
    formInstance.setCreationDate(rs.getTimestamp("creationDate"));
    formInstance.setValidatorId(rs.getString("validatorId"));
    formInstance.setValidationDate(rs.getTimestamp("validationDate"));
    formInstance.setComments(rs.getString("comments"));
    formInstance.setInstanceId(rs.getString("instanceId"));

    return formInstance;
  }

  /**
   * Get a new connection.
   * @return the initialized connection.
   * @throws FormsOnlineDatabaseException if a database error occured while getting connection
   */
  protected Connection getConnection() throws FormsOnlineDatabaseException {
    try {
      return DBUtil.makeConnection(DB_NAME);
    } catch (UtilException e) {
      throw new FormsOnlineDatabaseException(
          "FormsOnlineDAOJdbc.getConnection()",
          SilverpeasException.FATAL,
          "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  /**
   * Free given connection
   * @param con connection to be freed.
   */
  private final void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception exception) {
      }
    }
  }

  public static void prepareDateStatement(PreparedStatement stat, int pos,
      Date dateValue) throws SQLException {

    if (dateValue == null) {
      stat.setNull(pos, Types.DATE);
    } else {
      stat.setTimestamp(pos, new Timestamp(dateValue.getTime()));
    }
  }

}
